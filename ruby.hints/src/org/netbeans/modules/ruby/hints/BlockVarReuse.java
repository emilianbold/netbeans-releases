/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
import org.jruby.ast.IterNode;
import org.jruby.ast.LocalAsgnNode;
import org.jruby.ast.LocalVarNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;
import org.jruby.ast.types.INameNode;
import org.netbeans.api.gsf.CompilationInfo;
import org.netbeans.api.gsf.EditRegions;
import org.netbeans.api.gsf.OffsetRange;
import org.netbeans.modules.ruby.AstPath;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.hints.spi.AstRule;
import org.netbeans.modules.ruby.hints.spi.Description;
import org.netbeans.modules.ruby.hints.spi.Fix;
import org.netbeans.modules.ruby.hints.spi.HintSeverity;
import org.openide.util.NbBundle;

/**
 * A hint which looks for block variables that are reusing a variable
 * that already exists in scope when the block is initiated.
 * This will (-possibly- unintentionally) reassign the local variable
 * so might be something the user want to be alerted to
 * 
 * @todo Doesn't seem to work for params
 * @todo Don't warn on last lines?
 * @todo Doesn't seem to work for comma-separated lists of arguments, e.g. { |foo,bar| }
 *   (at least not the second arg)
 *
 * @author Tor Norbye
 */
public class BlockVarReuse implements AstRule {

    public BlockVarReuse() {
    }

    public boolean appliesTo(CompilationInfo info) {
        return true;
    }

    public Set<Integer> getKinds() {
        return Collections.singleton(NodeTypes.ITERNODE);
    }

    public void cancel() {
        // Does nothing
    }

    public String getId() {
        return "Block_Var_Reuse"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(BlockVarReuse.class, "UnintentionalSideEffect");
    }

    public String getDescription() {
        return NbBundle.getMessage(BlockVarReuse.class, "UnintentionalSideEffectDesc");
    }

    public void run(CompilationInfo info, Node node, AstPath path, int caretOffset, List<Description> result) {
        if (node.nodeId == NodeTypes.ITERNODE) {
            // Check the children and see if we have a LocalAsgnNode; these are going
            // to be local variable reuses
            @SuppressWarnings(value = "unchecked")
            List<Node> list = node.childNodes();

            for (Node child : list) {
                if (child instanceof LocalAsgnNode) {

                    OffsetRange range = AstUtilities.getNameRange(child);
                    List<Fix> fixList = new ArrayList<Fix>(2);
                    Node root = AstUtilities.getRoot(info);
                    AstPath childPath = new AstPath(root, child);
                    fixList.add(new RenameVarFix(info, childPath, false));
                    fixList.add(new RenameVarFix(info, childPath, true));

                    // TODO - add a hint to turn off this hint?
                    // Should be a utility or infrastructure option!
                    Description desc = new Description(this, getDisplayName(), info.getFileObject(), range, fixList, 100);
                    result.add(desc);
                }
            }
        }
    }

    private static class RenameVarFix implements Fix {

        private CompilationInfo info;

        private AstPath path;

        private boolean renameLocal;

        RenameVarFix(CompilationInfo info, AstPath path, boolean renameLocal) {
            this.info = info;
            this.path = path;
            this.renameLocal = renameLocal;
        }

        public String getDescription() {
            if (renameLocal) {
                return NbBundle.getMessage(BlockVarReuse.class, "ChangeLocalVarName");
            } else {
                return NbBundle.getMessage(BlockVarReuse.class, "ChangeBlockVarName");
            }
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
            if ((node.nodeId == NodeTypes.LOCALASGNNODE || node.nodeId == NodeTypes.LOCALVARNODE) && name.equals(((INameNode)node).getName())) {
                ranges.add(AstUtilities.getNameRange(node));
            }

            @SuppressWarnings(value = "unchecked")
            List<Node> list = node.childNodes();

            for (Node child : list) {

                // Skip inline method defs
                if (child instanceof MethodDefNode) {
                    continue;
                }

                // Skip SOME blocks:
                if (child.nodeId == NodeTypes.ITERNODE) {
                    // Skip only the block which the fix is applying to;
                    // the local variable could be aliased in other blocks as well
                    // and we need to keep the program correct!
                    if (child == path.leafParent()) {

                        continue;
                    }
                }

                addNonBlockRefs(child, name, ranges);
            }
        }

        private Set<OffsetRange> findRegionsToEdit() {
            Set<OffsetRange> ranges = new HashSet<OffsetRange>();

            assert path.leaf() instanceof INameNode;
            String name = ((INameNode)path.leaf()).getName();

            if (renameLocal) {
                Node scope = AstUtilities.findLocalScope(path.leaf(), path);
                addNonBlockRefs(scope, name, ranges);
            } else {
                Node parent = path.leafParent();
                assert parent instanceof IterNode;
                addNonBlockRefs(parent, name, ranges);
            }

            return ranges;
        }

        public boolean isSafe() {
            return false;
        }

        public boolean isInteractive() {
            return true;
        }
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }
}
