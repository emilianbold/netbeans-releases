/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.editing;

//import com.sun.phobos.script.javascript.lang.ast.AssignmentNode;
//import com.sun.phobos.script.javascript.lang.ast.CallNode;
//import com.sun.phobos.script.javascript.lang.ast.DeclarationNode;
//import com.sun.phobos.script.javascript.lang.ast.ExprNode;
//import com.sun.phobos.script.javascript.lang.ast.FunctionNode;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

//import org.mozilla.nb.javascript.Node;
//import com.sun.phobos.script.javascript.lang.ast.Node;
//import com.sun.phobos.script.javascript.lang.ast.PropertyGetNode;
//import com.sun.phobos.script.javascript.lang.ast.TreeWalker;
//import com.sun.phobos.script.javascript.lang.ast.VariableNode;
//import com.sun.semplice.javascript.AstNodeAdapter;
//import org.netbeans.api.languages.parsing.ParserResult;
import org.mozilla.nb.javascript.FunctionNode;
import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.Token;
import org.openide.util.Enumerations;


/** For debugging purposes only */
class AstNodeAdapter implements TreeNode {
    private Node node;
    private AstNodeAdapter parent;
    private AstNodeAdapter[] children;
    private List<AstNodeAdapter> childrenList;
    int endOffset = -1;

    AstNodeAdapter(AstNodeAdapter parent, Node node) {
        this.parent = parent;
        this.node = node;
    }

    
    
    private void ensureChildrenInitialized() {
        if (children != null) {
            return;
        }
//
        //            List<AstNodeAdapter> subnodes = new ArrayList<AstNodeAdapter>();
        //            Node child = node.getFirstChild();
        //            while (child != null) {
        //                subnodes.add(new AstNodeAdapter(this, child));
        //                child = child.getNext();
        //            }
        //            children = subnodes.toArray(new AstNodeAdapter[subnodes.size()]);
        List<AstNodeAdapter> subnodes = new ArrayList<AstNodeAdapter>();

        // Functions need special treatment: Rhino has a weird thing in the AST
        // where the AST for each function is not made part of the overall tree...
        // So in these cases I've gotta go looking for it myself...
        if (node.getType() == Token.FUNCTION) {
            String name;

            if (node instanceof FunctionNode) {
                name = ((FunctionNode)node).getFunctionName();
            } else {
                name = node.getString();
            }
 
//            if (node.getParentNode() instanceof ScriptOrFnNode) {
//                ScriptOrFnNode sn = (ScriptOrFnNode)node.getParentNode();
//
//                for (int i = 0, n = sn.getFunctionCount(); i < n; i++) {
//                    FunctionNode func = sn.getFunctionNode(i);
//
//                    if (name.equals(func.getFunctionName())) {
//                        // Found the function
//                        Node current = func.getFirstChild();
//
//                        for (; current != null; current = current.getNext()) {
//                            subnodes.add(new AstNodeAdapter(this, current));
//                        }
//
//                        children = subnodes.toArray(new AstNodeAdapter[subnodes.size()]);
//
//                        return;
//                    }
//                }
//            }
//
//            System.err.println("SURPRISE! It's (" + node +
//                " not a script node... revisit code--- some kind of error");
//            children = new AstNodeAdapter[0];
//            return;
        }

        if (node.hasChildren()) {
            Node current = node.getFirstChild();

            for (; current != null; current = current.getNext()) {
                // Already added above?
                //if (current.getType() == Token.FUNCTION) {
                //    continue;
                //}
                subnodes.add(new AstNodeAdapter(this, current));
            }

            children = subnodes.toArray(new AstNodeAdapter[subnodes.size()]);
        } else {
            children = new AstNodeAdapter[0];
        }
        
    
//         children = new AstNodeAdapter[0];
        if (children == null) {
            // XXX This is likely a bug in the AST
            children = new AstNodeAdapter[0];
        }
    }

    
//    public static AstNodeAdapter createFromAst(Node root) {        
//        BuildingTreeWalker tw = new BuildingTreeWalker(root);
//        return tw.getAdapter();
//    }


    public TreeNode getChildAt(int i) {
        ensureChildrenInitialized();

        return children[i];
    }

    public int getChildCount() {
        ensureChildrenInitialized();

        return children.length;
    }

    public TreeNode getParent() {
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
        StringBuffer sb = new StringBuffer();
        
//        String type = node.getClass().getName();
//        type = type.substring(type.lastIndexOf('.')+1);
//        type = type.replace('$', '.');
//        sb.append(type);
//      
//        sb.append(":");
        sb.append(Token.fullName(node.getType()));
        
        String name = null;
        
        if (node instanceof Node.StringNode) {
            sb.append(":\"");
            sb.append(node.getString());
            sb.append("\"");
        } else if (node instanceof Node.NumberNode) {
            sb.append(":");
            sb.append(node.getDouble());
        }
//        if (node instanceof PropertyGetNode) {
//            name = ((PropertyGetNode)node).getName();
//        } else if (node instanceof CallNode) {
//            ExprNode target = ((CallNode)node).getTarget();
//            if (target instanceof PropertyGetNode) {
//                name = ((PropertyGetNode)target).getName();
//            }
//        } else if (node instanceof DeclarationNode) {
//            List<VariableNode> list = ((DeclarationNode)node).getVariables();
//            if (list != null && list.size() > 0) {
//                StringBuffer s = new StringBuffer();
//                for (VariableNode n : list) {
//                    if (s.length() > 0) {
//                        s.append(',');
//                    }
//                    s.append(n.getName());
//                }
//                name = s.toString();
//            }
//        } else if (node instanceof VariableNode) {
//            name = ((VariableNode)node).getName();
//        } else if (node instanceof FunctionNode) {
//            name = ((FunctionNode)node).getName();
//        } else if (node instanceof AssignmentNode) {
//            ExprNode lhs = ((AssignmentNode)node).getTarget();
//            if (lhs instanceof PropertyGetNode) {
//                name = ((PropertyGetNode)lhs).getName();
//            }
//        }
        if (name != null) {
            sb.append(" : ");
            sb.append(name);
            sb.append(' ');
            
        }
        
//        sb.append(Token.name(node.getType()));
        sb.append("(");
        sb.append(Integer.toString(getStartOffset()));
        sb.append("-");
        sb.append(Integer.toString(getEndOffset()));
        sb.append(") ");

//        if (node.isStringNode()) {
//            sb.append("\"");
//            sb.append(node.getString());
//            sb.append("\"");
//        } else {
//            String clz = node.getClass().getName();
//            sb.append(clz.substring(clz.lastIndexOf('.') + 1));
//        }
//
        return sb.toString();
    }

    public int getStartOffset() {
        return Math.max(0, node.getSourceStart());
    }

    public int getEndOffset() {
        return Math.max(0, node.getSourceEnd());
//        if (endOffset == -1) {
//            // Compute lazily, since it's not available in the AST.
//            // Take it to be the offset of the next sibling, or the offset of the 
//            // last child, whichever is greater
//            
//            if (parent != null) {
//                parent.ensureChildrenInitialized();
//                for (int i = 0; i < parent.children.length; i++) {
//                    if (parent.children[i] == this) {
//                        if (i < parent.children.length-1) {
//                            endOffset = parent.children[i+1].getStartOffset();
//                        }
//                        break;
//                    }
//                }
//            }
//            
//            if (endOffset == -1) {
//                ensureChildrenInitialized();
//                if (children.length > 0) {
//                    endOffset = children[children.length-1].getEndOffset(); // possibly recursive call
//                }
//            }
//            
//            if (endOffset == -1) {
//                endOffset = getStartOffset();
//            }
//        }
//        
//        return endOffset;
    }

    public Object getAstNode() {
        return node;
    }

//    static class BuildingTreeWalker implements TreeWalker {
//        private Node root;
//        private Stack<AstNodeAdapter> adapterStack = new Stack<AstNodeAdapter>();
//        private AstNodeAdapter rootAdapter;
//        
//        public BuildingTreeWalker(Node root) {
//            this.root = root;
//        }
//        
//        public AstNodeAdapter getAdapter() {
//            root.walk(this);
//            return rootAdapter;
//        }
//        
//        public void walk(Node node) {
//            //System.out.println("walking node " + node);
//            if (node != null) {
//                node.walk(this);
//            }
//        }
//
//        public void preWalk(Node node) {
//            AstNodeAdapter parent = null;
//            if (!adapterStack.empty()) {
//                parent = adapterStack.peek();
//            }
//            AstNodeAdapter adapter = new AstNodeAdapter(parent, node);
//            adapterStack.push(adapter);
//            if (node == root) {
//                rootAdapter = adapter;
//            }
//            
//            if (parent != null) {
//                if (parent.childrenList == null) {
//                    parent.childrenList = new ArrayList<AstNodeAdapter>();
//                }
//                parent.childrenList.add(adapter);
//            }
//        }
//
//        public void postWalk(Node node) {
//            adapterStack.pop();
//
//            AstNodeAdapter parent = null;
//            if (!adapterStack.empty()) {
//                parent = adapterStack.peek();
//            } else {
//                parent = rootAdapter;
//            }
//            if (parent.childrenList != null) {
//                parent.children = parent.childrenList.toArray(new AstNodeAdapter[parent.childrenList.size()]);
//            } else {
//                parent.children = new AstNodeAdapter[0];
//            }
//        }
//    };

}
