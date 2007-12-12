/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.hints.introduce;

import org.netbeans.modules.ruby.ParseTreeWalker;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.jruby.ast.ClassNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;
import org.jruby.lexer.yacc.ISourcePosition;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.Formatter;
import org.netbeans.modules.ruby.NbUtilities;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.hints.spi.Description;
import org.netbeans.modules.ruby.hints.spi.Fix;
import org.netbeans.modules.ruby.hints.spi.HintSeverity;
import org.netbeans.modules.ruby.hints.spi.RuleContext;
import org.netbeans.modules.ruby.hints.spi.SelectionRule;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Offer to introduce a variable for an expression
 * 
 * @todo If you just select an identifier I shouldn't offer to abstract it - how could it
 *   possibly help?
 * @todo Suggest name: leaf if attribute or method access
 * @todo If you select the RHS of an assignment, don't offer to introduce a constant, field or
 *   variable - it's already assigned!
 * @todo Support replace all duplicates
 * @todo Test hashes
 * @todo If you have comments at the beginning or end of the selection, I don't handle things right -
 *   I end up with the wrong AST offsets (and I can't just skip these; they need to be included in the 
 *   move!)
 * @todo For statements containing break/next/continue I just disable this refactoring now; fix this
 *   such that I can handle these statements by looking up the loop construct and allowing it if 
 *   it's all within the fragment!
 * @todo If I extract method, and there is only a single return value, and the last statement
 *   in the method assigns to that return value, there's no point in having an explicit return
 *   of it, just leave the statement as the last statement
 * @todo Invoke formatter via the infrastructure so that it works right in RHTML etc.
 * 
 * @author Tor Norbye
 */
public class IntroduceHint implements SelectionRule {
    /** For test infrastructure only - a way to bypass the interactive name dialog */
    static String testName;
    
    public void run(RuleContext context, List<Description> result) {
        CompilationInfo info = context.compilationInfo;
        int start = context.selectionStart;
        int end = context.selectionEnd;

        assert start < end;

        try {
            BaseDocument doc = (BaseDocument) info.getDocument();
            if (end > doc.getLength()) {
                return;
            }

            if (end-start > 1000) {
                // Avoid doing tons of work when the user does a Ctrl-A to select all in a really
                // large buffer.
                return;
            }
            
            if (Formatter.getTokenBalance(doc, start, end, true, RubyUtils.isRhtmlDocument(doc)) != 0) {
                return;
            }
            
            Node root = AstUtilities.getRoot(info);
            if (root == null) {
                return;
            }
            OffsetRange lexOffsets = adjustOffsets(info, doc, start, end);
            if (lexOffsets == OffsetRange.NONE) {
                return;
            }

            OffsetRange astOffsets = AstUtilities.getAstOffsets(info, lexOffsets);
            if (astOffsets == OffsetRange.NONE) {
                return;
            }

            int astStart = astOffsets.getStart();
            int astEnd = astOffsets.getEnd();
            Map<Integer,List<Node>> nodeDepthMap = new HashMap<Integer, List<Node>>();
            findApplicableNodes(root, astStart, astEnd, nodeDepthMap, 0);
            if (nodeDepthMap.keySet().size() != 1) {
                // Either nodes at multiple depths or no nodes at all
                return;
            }
            List<Node> nodes = nodeDepthMap.values().iterator().next();
            assert nodes.size() > 0;

            IntroduceKindFinder typeChecker = new IntroduceKindFinder();
            ParseTreeWalker walker = new ParseTreeWalker(typeChecker);
            for (Node node : nodes) {
                walker.walk(node);
            }
            List<IntroduceKind> kinds = typeChecker.getKinds();
            
            if (kinds == null || kinds.size() == 0) {
                return;
            }

            OffsetRange range = new OffsetRange(start, end);
            
            // Adjust the fix range to be right around the dot so that the light bulb ends up
            // on the same line as the caret and alt-enter works
            JTextComponent target = NbUtilities.getPaneFor(info.getFileObject());
            if (target != null) {
                int dot = target.getCaret().getDot();
                if (start == dot) {
                    range = new OffsetRange(start, start);
                } else if (end == dot) {
                    range = new OffsetRange(end, end);
                }
            }

            if (RubyUtils.isRhtmlDocument(doc)) {
                // In RHTML, only Introduce Variable is permitted
                kinds.retainAll(Collections.singleton(IntroduceKind.CREATE_VARIABLE));
            } else if (kinds.contains(IntroduceKind.CREATE_FIELD)) {
                // Also create a field? Only if we're inside a class
                ClassNode clz = AstUtilities.findClassAtOffset(root, start);
                if (clz == null) {
                    kinds.remove(IntroduceKind.CREATE_FIELD);
                    if (kinds.size() == 0) {
                        return;
                    }
                }
            }
            
            for (IntroduceKind kind : kinds) {
                IntroduceFix fix = new IntroduceFix(info, nodes, lexOffsets, astOffsets, kind);
                List<Fix> fixList = new ArrayList<Fix>(1);
                fixList.add(fix);
                String displayName = fix.getDescription();
                Description desc = new Description(this, displayName, info.getFileObject(), range,
                        fixList, 292);
                result.add(desc);
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public boolean appliesTo(CompilationInfo info) {
        return true;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(IntroduceHint.class, "IntroduceHint");
    }

    // Only used by configurable rules
    //public String getDescription() {
    //    return NbBundle.getMessage(IntroduceHint.class, "IntroduceHintDesc");
    //}

    public boolean showInTasklist() {
        return false;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.CURRENT_LINE_WARNING;
    }

    private OffsetRange adjustOffsets(CompilationInfo info, BaseDocument doc, int start, int end) throws BadLocationException {
        int startRowEnd = Utilities.getRowLastNonWhite(doc, start);
        if (startRowEnd == -1) {
            startRowEnd = Utilities.getRowEnd(doc, end);
        } else {
            startRowEnd += 1; // Points at beginning of last char rather than after it, so adjust
        }
        int adjustedStart;
        if (start >= startRowEnd) {
            // Go to the next line
            adjustedStart = Utilities.getRowEnd(doc, start)+1;
            if (adjustedStart <= doc.getLength()) {
                int nextRow = Utilities.getRowFirstNonWhite(doc, adjustedStart);
                if (nextRow != -1) {
                    adjustedStart = nextRow;
                }
            } else {
                adjustedStart = doc.getLength();
            }
        } else {
            adjustedStart = Math.max(start, Utilities.getRowFirstNonWhite(doc, start));
        }
        
        int rowBegin = Utilities.getRowFirstNonWhite(doc, end);
        int adjustedEnd;
        // Go to the previous row if you're on a blank line or the beginning of a line
        if (rowBegin == -1) {
            adjustedEnd = Math.max(0, Utilities.getRowStart(doc, end)-1);
        } else {
            if (end <= rowBegin) {
                adjustedEnd = Math.max(0, Utilities.getRowStart(doc, end)-1);
            } else {
                int rowEnd = Utilities.getRowLastNonWhite(doc, end);
                adjustedEnd = Math.min(end, rowEnd+1);
            }
        }

        adjustedStart = Math.min(adjustedStart, doc.getLength());
        adjustedEnd = Math.min(adjustedEnd, doc.getLength());
        
        if (adjustedEnd <= adjustedStart) {
            return OffsetRange.NONE;
        }

        return new OffsetRange(adjustedStart, adjustedEnd);
    }
    

    /** Compute the set of applicable AST nodes for the given selection.
     * It will find a set of continguous nodes in the AST. The result is returned in the
     * result parameter. No nodes are added if the selection does not correspond to a complete
     * expression or set of statements.
     * @return The depth of the matches, or NODESEARCH_INCONSISTENT if the result set
     * is invalid, or NODESEARCH_NOT_FOUND if no matches were found.
     */
    private void findApplicableNodes(Node node, int start, int end, Map<Integer,List<Node>> result, int depth) {
        @SuppressWarnings(value = "unchecked")
        List<Node> list = node.childNodes();
        
        for ( Node child : list) {
            if (child.nodeId == NodeTypes.NEWLINENODE || child.nodeId == NodeTypes.HASHNODE) {
                // Newlines and hasnodes have incorrect offsets, so always search their children
                // instead of applying below search pruning logic
                findApplicableNodes(child, start, end, result, depth+1);
            } else {
                boolean add = false;
                ISourcePosition pos = child.getPosition();
                if (pos.getStartOffset() >= start && pos.getEndOffset() <= end) {
                    add = true;
                } else 
                // Prune search only to nodes that can possibly contain the children
                if (pos.getStartOffset() <= start && pos.getEndOffset() >= end) {
                    if (pos.getStartOffset() == start && pos.getEndOffset() == end) {
                        add = true;
                    } else {
                        findApplicableNodes(child, start, end, result, depth+1);
                    }
                } else {
                    // Partial overlap
                    if (pos.getStartOffset() <= start && start <= pos.getEndOffset()) {
                        findApplicableNodes(child, start, end, result, depth+1);
                    } else if (pos.getStartOffset() <= end && end <= pos.getEndOffset()) {
                        findApplicableNodes(child, start, end, result, depth+1);
                    }
                }
                if (add) {
                    List<Node> l = result.get(depth);
                    if (l == null) {
                        l = new ArrayList<Node>();
                        result.put(depth, l);
                    }
                    l.add(child);
                }
            }
        }
    }
}
