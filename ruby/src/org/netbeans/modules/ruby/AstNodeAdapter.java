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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.ruby;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.jruby.ast.ClassNode;
import org.jruby.ast.Colon2Node;
import org.jruby.ast.ConstDeclNode;
import org.jruby.ast.DefnNode;
import org.jruby.ast.DefsNode;
import org.jruby.ast.GlobalVarNode;
import org.jruby.ast.ModuleNode;
import org.jruby.ast.NewlineNode;
import org.jruby.ast.Node;
import org.jruby.ast.types.INameNode;
import org.netbeans.modules.gsf.api.ParserResult;
import org.openide.util.Enumerations;


/** For debugging only (used by the AST Viewer */
@SuppressWarnings("unchecked")
class AstNodeAdapter implements ParserResult.AstTreeNode {
    private static final boolean HIDE_NEWLINE_NODES = false;
    private final Node node;
    private final AstNodeAdapter parent;
    private AstNodeAdapter[] children;

    AstNodeAdapter(AstNodeAdapter parent, Node node) {
        this.parent = parent;
        this.node = node;
    }

    private void ensureChildrenInitialized() {
        if (children != null) {
            return;
        }

        if (HIDE_NEWLINE_NODES) {
            List<AstNodeAdapter> childList = new ArrayList<AstNodeAdapter>();
            addChildren(childList, node);
            children = childList.toArray(new AstNodeAdapter[childList.size()]);
        } else {
            List<Node> subnodes = (List<Node>)node.childNodes();
            children = new AstNodeAdapter[subnodes.size()];

            int index = 0;

            for (Node child : subnodes) {
                children[index++] = new AstNodeAdapter(this, child);
            }
        }
    }

    private void addChildren(List<AstNodeAdapter> children, Node node) {
        List<Node> subnodes = (List<Node>)node.childNodes();

        for (Node child : subnodes) {
            if (child instanceof NewlineNode) {
                addChildren(children, child);
            } else {
                children.add(new AstNodeAdapter(this, child));
            }
        }
    }

    public TreeNode getChildAt(int i) {
        ensureChildrenInitialized();

        return children[i];
    }

    public int getChildCount() {
        ensureChildrenInitialized();

        return children.length;
    }

    public TreeNode getParent() {
        ensureChildrenInitialized();

        return parent;
    }

    public int getIndex(TreeNode treeNode) {
        ensureChildrenInitialized();

        for (int i = 0; i < children.length; i++) {
            if (children[i] == treeNode) {
                return i;
            }
        }

        return -1;
    }

    public boolean getAllowsChildren() {
        ensureChildrenInitialized();

        return children.length > 0;
    }

    public boolean isLeaf() {
        ensureChildrenInitialized();

        return children.length == 0;
    }

    public Enumeration children() {
        ensureChildrenInitialized();

        return Enumerations.array(children);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("<html>");
        sb.append(node.toString());
        sb.append("<i>");
        sb.append(" (");
        sb.append(getStartOffset());
        sb.append("-");
        sb.append(getEndOffset());
        sb.append(") ");
        sb.append("</i>");

        String name = null;

        if (node instanceof INameNode) {
            name = ((INameNode)node).getName();
        } else if (node instanceof DefnNode) {
            name = ((DefnNode)node).getName();
        } else if (node instanceof DefsNode) {
            name = ((DefsNode)node).getName();
        } else if (node instanceof ConstDeclNode) {
            name = ((ConstDeclNode)node).getName();
        } else if (node instanceof GlobalVarNode) {
            name = ((GlobalVarNode)node).getName();
        } else if (node instanceof ClassNode) {
            Node n = ((ClassNode)node).getCPath();

            if (n instanceof Colon2Node) {
                Colon2Node c2n = (Colon2Node)n;

                name = c2n.getName();
            } else {
                name = n.toString();
            }
        } else if (node instanceof ModuleNode) {
            Node n = ((ModuleNode)node).getCPath();

            if (n instanceof Colon2Node) {
                Colon2Node c2n = (Colon2Node)n;

                name = c2n.getName();
            } else {
                name = n.toString();
            }
        }

        if (name != null) {
            sb.append(" : <b>");
            sb.append(name);
            sb.append("</b>");
        }

        sb.append("</html>");

        return sb.toString();
    }

    public int getStartOffset() {
        if (node.getPosition() != null) {
            return node.getPosition().getStartOffset();
        } else {
            return -1;
        }
    }

    public int getEndOffset() {
        if (node.getPosition() != null) {
            return node.getPosition().getEndOffset();
        } else {
            return -1;
        }
    }

    public Object getAstNode() {
        return node;
    }
}
