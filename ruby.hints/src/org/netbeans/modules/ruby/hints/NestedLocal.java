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
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.LocalVarNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;
import org.jruby.ast.types.INameNode;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.EditRegions;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.hints.spi.AstRule;
import org.netbeans.modules.ruby.hints.spi.Description;
import org.netbeans.modules.ruby.hints.spi.EditList;
import org.netbeans.modules.ruby.hints.spi.Fix;
import org.netbeans.modules.ruby.hints.spi.HintSeverity;
import org.netbeans.modules.ruby.hints.spi.PreviewableFix;
import org.netbeans.modules.ruby.hints.spi.RuleContext;
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
public class NestedLocal implements AstRule {

    public NestedLocal() {
    }

    public boolean appliesTo(CompilationInfo info) {
        return true;
    }

    public Set<Integer> getKinds() {
        return Collections.singleton(NodeTypes.FORNODE);
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

    public void run(RuleContext context, List<Description> result) {
        Node node = context.node;
        AstPath path = context.path;
        CompilationInfo info = context.compilationInfo;

        if (node.nodeId == NodeTypes.FORNODE) {
            // Check the children and see if we have a LocalAsgnNode; tbese are the
            // loop variables which are NOT local to the for block; if found, go and see
            // if it's a reuse!
            @SuppressWarnings(value = "unchecked")
            List<Node> list = node.childNodes();

            for (Node child : list) {
                if (child instanceof LocalAsgnNode) {
                    String name = ((INameNode)child).getName();
                    Node method = AstUtilities.findLocalScope(null, path);
                    if (method != null && isUsed(method, name, child, new boolean[1])) {
                        OffsetRange range = AstUtilities.getNameRange(child);
                        List<Fix> fixList = new ArrayList<Fix>(2);
                        Node root = AstUtilities.getRoot(info);
                        AstPath childPath = new AstPath(root, child);
                        fixList.add(new RenameVarFix(info, childPath, node, false));
                        fixList.add(new RenameVarFix(info, childPath, node, true));

                        // TODO - add a hint to turn off this hint?
                        // Should be a utility or infrastructure option!
                        String displayName = NbBundle.getMessage(NestedLocal.class, "NestedLocalName", name);

                        
                        range = LexUtilities.getLexerOffsets(info, range);
                        if (range != OffsetRange.NONE) {
                            Description desc = new Description(this, displayName, info.getFileObject(), range, fixList, 100);
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

        if (node.nodeId == NodeTypes.LOCALVARNODE || node.nodeId == NodeTypes.LOCALASGNNODE) {
            if (name.equals(((INameNode)node).getName())) {
                return true;
            }
        }

        @SuppressWarnings(value = "unchecked")
        List<Node> list = node.childNodes();

        for (Node child : list) {
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

        private CompilationInfo info;

        private AstPath path;
        private Node target;

        private boolean renameOuter;

        RenameVarFix(CompilationInfo info, AstPath path, Node target, boolean renameOuter) {
            this.info = info;
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
            BaseDocument doc = (BaseDocument) info.getDocument();
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
            EditRegions.getInstance().edit(info.getFileObject(), ranges, caretOffset);
        }

        private void addNonBlockRefs(Node node, String name, Set<OffsetRange> ranges) {
            if (((node instanceof LocalAsgnNode) || (node instanceof LocalVarNode)) && name.equals(((INameNode)node).getName())) {
                OffsetRange range = AstUtilities.getNameRange(node);
                range = LexUtilities.getLexerOffsets(info, range);
                if (range != OffsetRange.NONE) {
                    ranges.add(range);
                }
            }

            @SuppressWarnings(value = "unchecked")
            List<Node> list = node.childNodes();

            for (Node child : list) {

                // Skip inline method defs
                if (child instanceof MethodDefNode) {
                    continue;
                }
                
                if (child == target) {
                    // prune the block
                    continue;
                }

                addNonBlockRefs(child, name, ranges);
            }
        }

        private Set<OffsetRange> findRegionsToEdit() {
            Set<OffsetRange> ranges = new HashSet<OffsetRange>();

            assert path.leaf() instanceof INameNode;
            String name = ((INameNode)path.leaf()).getName();

            if (renameOuter) {
                Node scope = AstUtilities.findLocalScope(path.leaf(), path);
                addNonBlockRefs(scope, name, ranges);
            } else {
                addNonBlockRefs(target, name, ranges);
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
