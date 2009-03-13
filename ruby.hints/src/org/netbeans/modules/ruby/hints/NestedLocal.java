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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.hints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.jruby.nb.ast.LocalAsgnNode;
import org.jruby.nb.ast.MethodDefNode;
import org.jruby.nb.ast.Node;
import org.jruby.nb.ast.NodeType;
import org.jruby.nb.ast.types.INameNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.EditRegions;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.PreviewableFix;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.RubyUtils;
import org.netbeans.modules.ruby.hints.infrastructure.RubyAstRule;
import org.netbeans.modules.ruby.hints.infrastructure.RubyRuleContext;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.openide.util.NbBundle;

/**
 * A hint which looks for block variables that are reusing a variable
 * that already exists in scope when the block is initiated.
 * This will (-possibly- unintentionally) reassign the local variable
 * so might be something the user want to be alerted to
 * 
 * @todo Doesn't seem to work for params
 * @todo Don't warn on last lines?
 *
 * @author Tor Norbye
 */
public class NestedLocal extends RubyAstRule {

    public NestedLocal() {
    }

    public boolean appliesTo(RuleContext context) {
        return true;
    }

    public Set<NodeType> getKinds() {
        return Collections.singleton(NodeType.FORNODE);
    }

    public void cancel() {
        // Does nothing
    }

    public String getId() {
        return "Nested_Local"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(NestedLocal.class, "NestedLocal");
    }

    public String getDescription() {
        return NbBundle.getMessage(NestedLocal.class, "NestedLocalDesc");
    }

    public void run(RubyRuleContext context, List<Hint> result) {
        Node node = context.node;
        AstPath path = context.path;
        ParserResult info = context.parserResult;

        if (node.nodeId == NodeType.FORNODE) {
            // Check the children and see if we have a LocalAsgnNode; tbese are the
            // loop variables which are NOT local to the for block; if found, go and see
            // if it's a reuse!
            List<Node> list = node.childNodes();

            for (Node child : list) {
                if (child instanceof LocalAsgnNode) {
                    String name = ((INameNode)child).getName();
                    Node method = AstUtilities.findLocalScope(null, path);
                    if (method != null && isUsed(method, name, child, new boolean[1])) {
                        OffsetRange range = AstUtilities.getNameRange(child);
                        List<HintFix> fixList = new ArrayList<HintFix>(2);
                        Node root = AstUtilities.getRoot(info);
                        AstPath childPath = new AstPath(root, child);
                        fixList.add(new RenameVarFix(context, childPath, node, false));
                        fixList.add(new RenameVarFix(context, childPath, node, true));

                        // TODO - add a hint to turn off this hint?
                        // Should be a utility or infrastructure option!
                        String displayName = NbBundle.getMessage(NestedLocal.class, "NestedLocalName", name);

                        
                        range = LexUtilities.getLexerOffsets(info, range);
                        if (range != OffsetRange.NONE) {
                            Hint desc = new Hint(this, displayName, RubyUtils.getFileObject(info), range, fixList, 100);
                            result.add(desc);
                        }
                    }
                }
            }
        }
    }
    
    private boolean isUsed(Node node, String name, Node target, boolean[] done) {
        if (node == target) {
            done[0] = true;
            return false;
        }

        if (node.nodeId == NodeType.LOCALVARNODE || node.nodeId == NodeType.LOCALASGNNODE) {
            if (name.equals(((INameNode)node).getName())) {
                return true;
            }
        }

        List<Node> list = node.childNodes();

        for (Node child : list) {
            if (child.isInvisible()) {
                continue;
            }
            boolean found = isUsed(child, name, target, done);
            
            if (found) {
                return true;
            }
            
            if (done[0]) {
                return false;
            }
        }
        
        return false;
    }

    private static class RenameVarFix implements PreviewableFix {

        private final RubyRuleContext context;
        private final AstPath path;
        private final Node target;
        private final boolean renameOuter;

        RenameVarFix(RubyRuleContext context, AstPath path, Node target, boolean renameOuter) {
            this.context = context;
            this.target = target;
            this.path = path;
            this.renameOuter = renameOuter;
        }

        public String getDescription() {
            if (renameOuter) {
                return NbBundle.getMessage(NestedLocal.class, "ChangeOuter");
            } else {
                return NbBundle.getMessage(NestedLocal.class, "ChangeInner");
            }
        }

        public EditList getEditList() throws Exception {
            BaseDocument doc = context.doc;
            EditList edits = new EditList(doc);
            Set<OffsetRange> ranges = findRegionsToEdit();
            String oldName = ((INameNode)path.leaf()).getName();
            int oldLen = oldName.length();
            String newName = "new_name";
            for (OffsetRange range : ranges) {
                edits.replace(range.getStart(), oldLen, newName, false, 0);
            }
            return edits;
        }
        
        public void implement() throws Exception {
            // Refactoring isn't necessary here since local variables and block
            // variables are limited to the local scope, so we can accurately just
            // find their positions using the AST and let the user edit them synchronously.
            Set<OffsetRange> ranges = findRegionsToEdit();

            // Pick the first range as the caret offset
            int caretOffset = Integer.MAX_VALUE;
            for (OffsetRange range : ranges) {
                if (range.getStart() < caretOffset) {
                    caretOffset = range.getStart();
                }
            }

            // Initiate synchronous editing:
            EditRegions.getInstance().edit(RubyUtils.getFileObject(context.parserResult), ranges, caretOffset);
        }

        private void addNonBlockRefs(Node node, String name, Set<OffsetRange> ranges, boolean isParameter) {
            if ((node.nodeId == NodeType.LOCALASGNNODE || node.nodeId == NodeType.LOCALVARNODE) && name.equals(((INameNode)node).getName())) {
                OffsetRange range = AstUtilities.getNameRange(node);
                range = LexUtilities.getLexerOffsets(context.parserResult, range);
                if (range != OffsetRange.NONE) {
                    ranges.add(range);
                }
            } else if (isParameter && (node.nodeId == NodeType.ARGUMENTNODE && name.equals(((INameNode)node).getName()))) {
                OffsetRange range = AstUtilities.getNameRange(node);
                range = LexUtilities.getLexerOffsets(context.parserResult, range);
                if (range != OffsetRange.NONE) {
                    ranges.add(range);
                }
            } else if (node.nodeId == NodeType.ARGSNODE) {
                isParameter = true;
            }

            List<Node> list = node.childNodes();

            for (Node child : list) {
                if (child.isInvisible()) {
                    continue;
                }

                // Skip inline method defs
                if (child instanceof MethodDefNode) {
                    continue;
                }
                
                if (child == target) {
                    // prune the block
                    continue;
                }

                addNonBlockRefs(child, name, ranges, isParameter);
            }
        }

        private Set<OffsetRange> findRegionsToEdit() {
            Set<OffsetRange> ranges = new HashSet<OffsetRange>();

            assert path.leaf() instanceof INameNode;
            String name = ((INameNode)path.leaf()).getName();

            if (renameOuter) {
                Node scope = AstUtilities.findLocalScope(path.leaf(), path);
                addNonBlockRefs(scope, name, ranges, false);
            } else {
                addNonBlockRefs(target, name, ranges, false);
            }

            return ranges;
        }

        public boolean isSafe() {
            return false;
        }

        public boolean isInteractive() {
            return true;
        }

        public boolean canPreview() {
            return true;
        }
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    public boolean showInTasklist() {
        return true;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }
}
