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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.groovy.editor;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.TreeNode;
import org.codehaus.groovy.ast.ASTNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.api.ParserResult;
import org.openide.util.Enumerations;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;


@SuppressWarnings("unchecked")
public class AstNodeAdapter implements ParserResult.AstTreeNode {

    private final Logger LOG = Logger.getLogger(AstNodeAdapter.class.getName());
    private static final boolean HIDE_NEWLINE_NODES = false;
    private final ASTNode node;
    private final AstNodeAdapter parent;
    private AstNodeAdapter[] children;
    private BaseDocument doc;

    public AstNodeAdapter(AstNodeAdapter parent, ASTNode node, BaseDocument doc) {
        this.parent = parent;
        this.node = node;
        this.doc = doc;

        LOG.setLevel(Level.OFF);
    }

    private void ensureChildrenInitialized() {
        if (children != null) {
            return;
        }

        children = new AstNodeAdapter[0];
        
        if (HIDE_NEWLINE_NODES) {
            List<AstNodeAdapter> childList = new ArrayList<AstNodeAdapter>();
            addChildren(childList, node);
            children = childList.toArray(new AstNodeAdapter[childList.size()]);
        } else {
            List<ASTNode> subnodes = AstUtilities.children(node);
            children = new AstNodeAdapter[subnodes.size()];

            int index = 0;

            for (ASTNode child : subnodes) {
                children[index++] = new AstNodeAdapter(this, child, doc);
            }
        }
    }

    private void addChildren(List<AstNodeAdapter> children, ASTNode node) {
        List<ASTNode> subnodes = AstUtilities.children(node);

        for (ASTNode child : subnodes) {
            children.add(new AstNodeAdapter(this, child, doc));
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
        LOG.log(Level.FINEST, "getIndex(), TreeNode : {0}", treeNode.toString()); // NOI18N
        ensureChildrenInitialized();

        for (int i = 0; i < children.length; i++) {
            if (children[i] == treeNode) {
                return i;
            }
        }

        return -1;
    }

    public boolean getAllowsChildren() {
        LOG.log(Level.FINEST, "getAllowsChildren()"); // NOI18N
        ensureChildrenInitialized();

        return children.length > 0;
    }

    public boolean isLeaf() {
        ensureChildrenInitialized();

        LOG.log(Level.FINEST, "------------------------------------------------------"); // NOI18N
        LOG.log(Level.FINEST, "isLeaf(), Name: {0}", node.getClass().getSimpleName()); // NOI18N
        LOG.log(Level.FINEST, "isLeaf(), children: {0}", children.length); // NOI18N
        LOG.log(Level.FINEST, "------------------------------------------------------"); // NOI18N
        return children.length == 0;
    }

    public Enumeration children() {
        ensureChildrenInitialized();

        return Enumerations.array(children);
    }


    private String getArtifactName(ASTNode node){
        if (node instanceof ClassNode){
            return ((ClassNode)node).getName();
        } else if (node instanceof MethodNode) {
            return ((MethodNode)node).getName();
        } else if (node instanceof VariableExpression) {
            return ((VariableExpression)node).getName();
        } else if (node instanceof ModuleNode) {
            return ((ModuleNode)node).getDescription();
        } else if (node instanceof ConstantExpression) {
            return ((ConstantExpression)node).getText();
        }

        return "";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(node.getClass().getSimpleName());
        sb.append("[");
        sb.append(getArtifactName(node));
        sb.append("]");
        sb.append(" (");
        sb.append(getStartOffset());
        sb.append("-");
        sb.append(getEndOffset());
        sb.append(") ");

        
        LOG.log(Level.FINEST, "toString() node: {0}", sb.toString()); // NOI18N
        return sb.toString();
    }

    private void printLineNumbers() {
        LOG.log(Level.FINEST, "Line   : {0}", node.getLineNumber()); // NOI18N
        LOG.log(Level.FINEST, "Column : {0}", node.getColumnNumber()); // NOI18N
    }

    public int getStartOffset() {
        printLineNumbers();
        int line = node.getLineNumber();
        if (line < 1) {
            line = 1;
        }
        int column = node.getColumnNumber();
        if (column < 1) {
            column = 1;
        }
        return AstUtilities.getOffset(doc, line, column);
    }

    public int getEndOffset() {
        printLineNumbers();
        int line = node.getLastLineNumber();
        if (line < 1) {
            line = 1;
        }
        int column = node.getLastColumnNumber();
        if (column < 1) {
            column = 1;
        }
        return AstUtilities.getOffset(doc, line, column);
    }

    public Object getAstNode() {
        return node;
    }

}
