/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.iep.editor.xsd.nodes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.visitor.DeepAXITreeVisitor;

/**
 *
 * @author radval
 */
public class AxiTreeNodeVisitor extends DeepAXITreeVisitor {

    private FileNode mFileNode;
    
    private FolderNode mElementFolderNode;
    
    private FolderNode mComplexTypesFolderNode;
    
    private JTree mTree;
    
    Stack<AbstractSchemaArtifactNode> mCurrentStack = new Stack<AbstractSchemaArtifactNode>();
    
    private List<AXIComponent> mExistingArtificatNames = new ArrayList<AXIComponent>();
    
    private List<AbstractSchemaArtifactNode> mNodesToBeExpanded = new ArrayList<AbstractSchemaArtifactNode>();
    
    public AxiTreeNodeVisitor(FileNode fileNode, 
                              List<AXIComponent> existingArtificatNames,
                              JTree tree) {
        this.mFileNode = fileNode;
        this.mTree = tree;
        mElementFolderNode = new FolderNode("Elements");
        mElementFolderNode.setBadge("org/netbeans/modules/iep/editor/xsd/nodes/images/element_badge.png");
        this.mFileNode.add(mElementFolderNode);
        mCurrentStack.push(mElementFolderNode);
        
        mComplexTypesFolderNode = new FolderNode("Complex Types");
        mComplexTypesFolderNode.setBadge("org/netbeans/modules/iep/editor/xsd/nodes/images/complexType_badge.png");
        this.mFileNode.add(mComplexTypesFolderNode);
        
        this.mExistingArtificatNames = existingArtificatNames;
        
    }
    
    public List<AbstractSchemaArtifactNode> getNodesToBeExpanded() {
        return this.mNodesToBeExpanded;
    }
    
    @Override
    public void visit(AXIDocument root) {
         List<Element> globalElements = root.getChildren(Element.class);
         Iterator<Element> it = globalElements.iterator();
         mCurrentStack.push(this.mElementFolderNode);
         while(it.hasNext()) {
             Element e = it.next();
             e.accept(this);
         }
         mCurrentStack.pop();
         
         List<ContentModel> globalTypes = root.getChildren(ContentModel.class);
         Iterator<ContentModel> itc = globalTypes.iterator();
         mCurrentStack.push(this.mComplexTypesFolderNode);
         while(itc.hasNext()) {
             ContentModel c = itc.next();
             c.accept(this);
         }
         
         mCurrentStack.pop();
    }
     
    @Override
    public void visit(Element element) { 
        SchemaElementNode eNode = new SchemaElementNode(element);
        if(this.mExistingArtificatNames.contains(element)) {
//            this.mExistingArtificatNames.remove(element);
            eNode.setSelected(true);
        }
        
        AbstractSchemaArtifactNode currentNode = mCurrentStack.peek();
        currentNode.add(eNode);
        
        if(eNode.isSelected()) {
            mNodesToBeExpanded.add(eNode);
        }
        mCurrentStack.push(eNode);
        visitChildren(element);
        mCurrentStack.pop();
        
    }

    @Override
    public void visit(ContentModel element) {
        SchemaComplexTypeNode cNode = new SchemaComplexTypeNode(element);
        if(this.mExistingArtificatNames.contains(element)) {
//            this.mExistingArtificatNames.remove(element);
            cNode.setSelected(true);
        }
        
        AbstractSchemaArtifactNode currentNode = mCurrentStack.peek();
        currentNode.add(cNode);
        
        if(cNode.isSelected()) {
            mNodesToBeExpanded.add(cNode);
        }
        
        mCurrentStack.push(cNode);
        visitChildren(element);
        mCurrentStack.pop();
    }
    
    @Override
    public void visit(Attribute attribute) {
        SchemaAttributeNode aNode = new SchemaAttributeNode(attribute);
        if(this.mExistingArtificatNames.contains(attribute.getName())) {
//            this.mExistingArtificatNames.remove(attribute.getName());
            aNode.setSelected(true);
        }
        
        AbstractSchemaArtifactNode currentNode = mCurrentStack.peek();
        currentNode.add(aNode);
        
        if(aNode.isSelected()) {
            mNodesToBeExpanded.add(aNode);
        }
//        visitChildren(attribute);
    }
}
