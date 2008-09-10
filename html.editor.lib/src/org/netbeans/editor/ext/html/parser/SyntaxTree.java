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

package org.netbeans.editor.ext.html.parser;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class SyntaxTree {
    
    public static AstNode makeTree(List<SyntaxElement> elements) {
        SyntaxElement last = elements.size() > 0 ? elements.get(elements.size() - 1) : null;
        int lastEndOffset = last == null ? 0 : last.offset() + last.length();
        
        AstNode root = new AstNode("root", null, 0, lastEndOffset);
        LinkedList<AstNode> nodeStack = new  LinkedList<AstNode>();
        nodeStack.add(root);
        
        for (SyntaxElement element : elements){
            if (element.type() == SyntaxElement.TYPE_TAG){
                // OPENING TAG
                // create tag node, push it on stack
                // add opening tag node
                
                String tagName = ((SyntaxElement.Named)element).getName();
                int openingTagEndOffset = element.offset() + element.length();
                
                AstNode newTagNode = new AstNode(tagName, AstNode.NodeType.TAG,
                        element.offset(), openingTagEndOffset);
                        
                nodeStack.getLast().addChild(newTagNode);
                assert element instanceof SyntaxElement.Tag;
                
                if (!((SyntaxElement.Tag) element).isEmpty()){
                    nodeStack.add(newTagNode);
                }
                
                AstNode openingTagNode = new AstNode(tagName, AstNode.NodeType.OPEN_TAG,
                        element.offset(), openingTagEndOffset);
                
                newTagNode.addChild(openingTagNode);
            } else if (element.type() == SyntaxElement.TYPE_ENDTAG) {
                // CLOSING TAG
                // is it consistent with the last open tag? for now assuming 'yes'
                // add closing tag node
                // pop current node from the stack
                
                String tagName = ((SyntaxElement.Named)element).getName();        
                int lastMatchedTag = nodeStack.size() - 1;
                
                while (!tagName.equals(nodeStack.get(lastMatchedTag).name()) && lastMatchedTag > 0){
                    lastMatchedTag --;
                }
                
                int closingTagEndOffset = element.offset() + element.length();
                            
                AstNode closingTag = new AstNode(tagName, AstNode.NodeType.ENDTAG,
                       element.offset(), closingTagEndOffset);
                
                if (tagName.equals(nodeStack.get(lastMatchedTag).name())){
                    int nodesToDelete = nodeStack.size() - lastMatchedTag - 1;
                    removeNLastNodes(nodesToDelete, nodeStack);
                    
                    nodeStack.getLast().addChild(closingTag);
                    nodeStack.getLast().setEndOffset(closingTagEndOffset);
                    nodeStack.removeLast();
                } else {
                    // unmatched closing tag
                    AstNode newTagNode = new AstNode(tagName, AstNode.NodeType.TAG,
                        element.offset(), closingTagEndOffset);
                
                    newTagNode.markUnmatched();
                    nodeStack.getLast().addChild(newTagNode);
                    nodeStack.add(newTagNode);
                    newTagNode.addChild(closingTag);
                }
                
            } else {
                // add a new AST node to the last node on the stack
                AstNode.NodeType nodeType = intToNodeType(element.type());
                
                AstNode node = new AstNode(null, nodeType, element.offset(),
                        element.offset() + element.length());
                
                //hack
                if(nodeType == AstNode.NodeType.DECLARATION) {
                    node.setAttribute("public_id", ((SyntaxElement.Declaration)element).getPublicIdentifier()); //NOI18N
                }
                
                nodeStack.getLast().addChild(node);
            }
        }
        
        removeNLastNodes(nodeStack.size() - 1, nodeStack);
        return root;
    }
    
    private static void removeNLastNodes(int nodesToDelete, LinkedList<AstNode> nodeStack) {
        LinkedList<LinkedList<AstNode>> orphanMatrix = new LinkedList<LinkedList<AstNode>>();

        for (int i = 0; i < nodesToDelete; i++) {
            LinkedList<AstNode> orphans = new LinkedList<AstNode>();
            nodeStack.getLast().markUnmatched();

            for (AstNode child : nodeStack.getLast().children()) {
                if (child.type() == AstNode.NodeType.TAG || child.type() == AstNode.NodeType.UNMATCHED_TAG) {
                    orphans.add(child);
                }
            }

            nodeStack.getLast().removeTagChildren();

            nodeStack.removeLast();
            orphanMatrix.addFirst(orphans);
        }
        
        for (LinkedList<AstNode> orphans : orphanMatrix) {
            for (AstNode orphan : orphans) {
                nodeStack.getLast().addChild(orphan);
            }
        }
    }

    private static AstNode.NodeType intToNodeType(int type){
        switch (type) {
            case SyntaxElement.TYPE_COMMENT:
                return AstNode.NodeType.COMMENT;
            case SyntaxElement.TYPE_DECLARATION:
                return AstNode.NodeType.DECLARATION;
            case SyntaxElement.TYPE_ENDTAG:
                return AstNode.NodeType.ENDTAG;
            case SyntaxElement.TYPE_ENTITY_REFERENCE:
                return AstNode.NodeType.ENTITY_REFERENCE;
            case SyntaxElement.TYPE_ERROR:
                return AstNode.NodeType.ERROR;
            case SyntaxElement.TYPE_TAG:
                return AstNode.NodeType.OPEN_TAG;
            case SyntaxElement.TYPE_TEXT:
                return AstNode.NodeType.TEXT;
        }
        
        return null;
    }    
}
