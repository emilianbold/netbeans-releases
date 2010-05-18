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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.iep.editor.ps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.swing.JTree;

import org.netbeans.modules.iep.editor.xsd.nodes.AbstractSchemaArtifactNode;
import org.netbeans.modules.iep.editor.xsd.nodes.FolderNode;
import org.netbeans.modules.iep.editor.xsd.nodes.SchemaAttributeNode;
import org.netbeans.modules.iep.editor.xsd.nodes.SchemaComplexTypeNode;
import org.netbeans.modules.iep.editor.xsd.nodes.SchemaElementNode;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.visitor.DeepAXITreeVisitor;

/**
 *
 * @author radval
 */
public class SchemaAxiTreeNodeVisitor extends DeepAXITreeVisitor {

    private SchemaFileNode mFileNode;
    
    private FolderNode mElementFolderNode;
    
    private FolderNode mComplexTypesFolderNode;
    
    private JTree mTree;
    
    Stack<AbstractSchemaArtifactNode> mCurrentStack = new Stack<AbstractSchemaArtifactNode>();
    
    private List<String> mExistingArtificatNames = new ArrayList<String>();
    
    private List<AbstractSchemaArtifactNode> mNodesToBeExpanded = new ArrayList<AbstractSchemaArtifactNode>();
    
    public SchemaAxiTreeNodeVisitor(SchemaFileNode fileNode, 
                              	    List<String> existingArtificatNames,
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
	String name = ((AXIType)element).getName();
        SchemaElementNode eNode = new SchemaElementNode(element);
        if(this.mExistingArtificatNames.contains(name)) {
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
	String name = ((AXIType)element).getName();
        SchemaComplexTypeNode cNode = new SchemaComplexTypeNode(element);
        if(this.mExistingArtificatNames.contains(name)) {
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
	String name = attribute.getType().getName();
        SchemaAttributeNode aNode = new SchemaAttributeNode(attribute);
        if(this.mExistingArtificatNames.contains(name)) {
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
