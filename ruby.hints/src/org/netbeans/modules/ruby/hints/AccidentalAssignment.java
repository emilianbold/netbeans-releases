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
package org.netbeans.modules.ruby.hints;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.jruby.nb.ast.IfNode;
import org.jruby.nb.ast.Node;
import org.jruby.nb.ast.NodeType;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.ParseTreeWalker;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.openide.util.NbBundle;
import org.jruby.nb.ast.types.INameNode;
import org.netbeans.modules.gsf.api.Hint;
import org.netbeans.modules.gsf.api.EditList;
import org.netbeans.modules.gsf.api.HintFix;
import org.netbeans.modules.gsf.api.HintSeverity;
import org.netbeans.modules.gsf.api.PreviewableFix;
import org.netbeans.modules.gsf.api.RuleContext;
import org.netbeans.modules.ruby.ParseTreeVisitor;
import org.netbeans.modules.ruby.hints.infrastructure.RubyAstRule;
import org.netbeans.modules.ruby.hints.infrastructure.RubyRuleContext;

/**
 * Identify "accidental" assignments of the form "if (a = b)" which should have been "if (a == b)"
 * 
 * @todo Refine this by only warning about comparisons where the LHS is an existing variable in scope
 * 
 * @author Tor Norbye
 */
public class AccidentalAssignment extends RubyAstRule {

    public Set<NodeType> getKinds() {
        return Collections.singleton(NodeType.IFNODE);
    }

    public void run(RubyRuleContext context,
            List<Hint> result) {
        Node node = context.node;
        AstPath path = context.path;
        CompilationInfo info = context.compilationInfo;
        
        IfNode ifNode = (IfNode) node;
        Node condition = ifNode.getCondition();
        if (condition != null) {
            if (condition.nodeId == NodeType.NEWLINENODE) {
                List<Node> children = condition.childNodes();
                if (children.size() == 0) {
                    return;
                }
                condition = children.get(0);
            }
            if (((condition.nodeId == NodeType.LOCALASGNNODE) && !isFirstUsage(path, condition)) ||
               (condition.nodeId == NodeType.ATTRASSIGNNODE)) {
                String displayName = NbBundle.getMessage(AccidentalAssignment.class,
                        "AccidentalAssignment");
                OffsetRange range = AstUtilities.getRange(condition);
                range = LexUtilities.getLexerOffsets(info, range);
                if (range != OffsetRange.NONE) {
                    List<HintFix> fixList = new ArrayList<HintFix>(2);
                    fixList.add(new ConvertAssignmentFix(context, condition));
                    Hint desc = new Hint(this, displayName, info.getFileObject(),
                            range, fixList, 600);
                    result.add(desc);
                }
            }
        }
    }

    private boolean isFirstUsage(AstPath path, Node node) {
        DeclarationFinder finder = new DeclarationFinder(node);
        Node method = AstUtilities.findLocalScope(node, path);
        new ParseTreeWalker(finder).walk(method);

        return finder.isDeclaration();
    }

    public String getId() {
        return "AccidentalAssignment";
    }

    public String getDescription() {
        return NbBundle.getMessage(AccidentalAssignment.class, "AccidentalAssignmentDesc");
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    public boolean appliesTo(RuleContext context) {
        return true;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(AccidentalAssignment.class, "AccidentalAssignment");
    }

    public boolean showInTasklist() {
        return true;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    private static class ConvertAssignmentFix implements PreviewableFix {

        private final RubyRuleContext context;
        private final Node assignment;

        public ConvertAssignmentFix(RubyRuleContext context, Node assignment) {
            this.context = context;
            this.assignment = assignment;
        }

        public String getDescription() {
            return NbBundle.getMessage(AccidentalAssignment.class, "AccidentalAssignmentFix");
        }

        public void implement() throws Exception {
            EditList edits = getEditList();
            if (edits != null) {
                edits.apply();
            }
        }

        public EditList getEditList() throws Exception {
            BaseDocument doc = context.doc;
            OffsetRange range = AstUtilities.getNameRange(assignment);
            int endOffset = range.getEnd();
            if (assignment.nodeId == NodeType.ATTRASSIGNNODE) {
                // Workaround: the name-range of attr nodes isn't computed 
                // correctly so just use the LHS
                endOffset = range.getStart();
            }
            int lineEnd = Utilities.getRowEnd(doc, endOffset);
            String line = doc.getText(endOffset, lineEnd - endOffset);
            int dashIndex = line.indexOf('=');
            if (dashIndex != -1) {
                return new EditList(doc).replace(endOffset+dashIndex, 0, "=", false, 0);
            }
            
            return null;
        }

        public boolean isSafe() {
            return false;
        }

        public boolean isInteractive() {
            return false;
        }

        public boolean canPreview() {
            return true;
        }
    }

    public class DeclarationFinder implements ParseTreeVisitor {

        private Node target;
        //   private boolean inArgs;
        private Set<String> names = new HashSet<String>();
        private boolean found;

        DeclarationFinder(Node target) {
            this.target = target;
        }

        public boolean isDeclaration() {
            return !found;
        }

        public boolean visit(Node node) {
            if (node == target) {
                String name = ((INameNode) node).getName();
                if (names.contains(name)) {
                    found = true;
                    return true;
                }
            }
            switch (node.nodeId) {
            case LOCALVARNODE:
                names.add(((INameNode) node).getName());
                break;
            // TODO - handle blocks properly
            case DVARNODE:
                names.add(((INameNode) node).getName());
                break;
            }
            return false;
        }

        public boolean unvisit(Node node) {
            switch (node.nodeId) {
            case LOCALASGNNODE:
                names.add(((INameNode) node).getName());
                break;
            // TODO - handle blocks properly
            case DASGNNODE:
                names.add(((INameNode) node).getName());
                break;
            }

            return node == target;
        }
    }
}
