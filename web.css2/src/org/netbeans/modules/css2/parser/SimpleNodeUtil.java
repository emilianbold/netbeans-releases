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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css2.parser;

import java.util.ArrayList;

/**
 *
 * @author marek
 */
public class SimpleNodeUtil {

    public static SimpleNode findDescendant(SimpleNode node, int astOffset) {
        int so = node.startOffset();
        int eo = node.endOffset();
       
        
        if (astOffset < so || astOffset > eo) {
            //we are out of the scope - may happen just with the first client call
            return null;
        }

        if (astOffset >= so && astOffset <= eo && node.jjtGetNumChildren() == 0) {
            //if the node matches and has no children we found it
            return node;
        }

        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            SimpleNode child = (SimpleNode) node.jjtGetChild(i);

            int ch_so = child.startOffset();
            int ch_eo = child.endOffset();
            if (astOffset >= ch_so && astOffset <= ch_eo) {
                //the child is or contains the searched node
                return findDescendant(child, astOffset);
            }

        }

        return node;
    }

    
    /** @return first child of the node with the specified kind. */
    public static SimpleNode getChildByType(SimpleNode node, int kind) {
        SimpleNode[] children = getChildrenByType(node, kind);
        return children.length == 0 ? null : children[0];
    }
    
    /** @return list of children of the node with the specified kind. */
    public static SimpleNode[] getChildrenByType(SimpleNode node, int kind) {
        int childrenCount = node.children.length;
        ArrayList<SimpleNode> list = new ArrayList<SimpleNode>(childrenCount / 4);
        for(int i = 0; i < childrenCount ; i++) {
            SimpleNode child = (SimpleNode)node.children[i];
            if(child.kind() == kind) {
                list.add(child);
            }
        }
        return list.toArray(new SimpleNode[]{});
    }
    
    public static void visitChildren(SimpleNode node, NodeVisitor visitor) {
        Node[] children = node.children;
        if (children != null) {
            for (int i = 0; i < children.length; ++i) {
                SimpleNode n = (SimpleNode) children[i];
                if (n != null) {
                    visitor.visit(n);
                    n.visitChildren(visitor);
                }
            }
        }
    }

    public static void visitAncestors(SimpleNode node, NodeVisitor visitor) {
        SimpleNode parent = (SimpleNode)node.parent;
        if (parent != null) {
            visitor.visit(parent);
            visitAncestors(parent, visitor);
        }
    }
}
