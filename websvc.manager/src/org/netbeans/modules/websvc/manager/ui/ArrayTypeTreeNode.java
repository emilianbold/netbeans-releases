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


package org.netbeans.modules.websvc.manager.ui;

import com.sun.tools.ws.processor.model.java.JavaArrayType;
import com.sun.tools.ws.processor.model.java.JavaStructureType;
import com.sun.tools.ws.processor.model.java.JavaType;
import javax.swing.tree.*;
import java.util.ArrayList;
import java.util.Iterator;

import java.net.URLClassLoader;

/**
 *
 * @author  David Botterill
 */
public class ArrayTypeTreeNode extends AbstractParameterTreeNode {
    private URLClassLoader urlClassLoader;
    private String packageName;
    
    public ArrayTypeTreeNode(Object userObject,URLClassLoader inClassLoader,String inPackageName) {
        super(userObject);
        urlClassLoader = inClassLoader;
        packageName = inPackageName;
        
    }
    
    
    public void updateValueFromChildren(TypeNodeData inData) {
        /**
         * create a new ArrayList from all of the child values.
         */
        TypeNodeData data = (TypeNodeData)this.getUserObject();
        ArrayList newList = new ArrayList();
        for(int ii=0; ii < this.getChildCount(); ii++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)this.getChildAt(ii);
            TypeNodeData childData = (TypeNodeData)childNode.getUserObject();
            if(null != childData.getParameterValue()) {
                newList.add(childData.getParameterValue());
            }
        }
        
        data.setParameterValue(newList);
        /**
         * If this node is a member of a structure type, update it's parent.
         */
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) this.getParent();
        if(null != parentNode && parentNode instanceof ArrayTypeTreeNode) {
            ((ArrayTypeTreeNode)parentNode).updateValueFromChildren(data);
        } else if(null != parentNode && parentNode instanceof StructureTypeTreeNode) {
            /**
             * This type should be a JavaStructureMember
             */
            ((StructureTypeTreeNode)parentNode).updateValueFromChildren(data);
        }
        
        
    }
    /**
     * Update the child nodes based on the value of this UserObject.
     * Fix for Bug: 5059732
     * - David Botterill 8/12/2004
     *
     */
    public void updateChildren() {
        /**
         * First get the ArrayList for this node.
         */
        ArrayList nodeArrayList = (ArrayList)((TypeNodeData)this.getUserObject()).getNodeValue();
        /**
         * Next we need to delete all of the child nodes since the
         */
        this.removeAllChildren();
        /**
         * For each entry in the array, make a child node of the JavaArrayType.getElementType() type and
         * add it to this node.
         */
        
        Iterator arrayIterator = nodeArrayList.iterator();
        JavaArrayType structureType = (JavaArrayType) ((TypeNodeData)this.getUserObject()).getNodeType();
        JavaType elementType = structureType.getElementType();
        TypeNodeData data = null;
        DefaultMutableTreeNode childNode = null;
        for(int ii=0; arrayIterator.hasNext();ii++) {
            data = new TypeNodeData(elementType,"[" + ii + "]",arrayIterator.next());
            if(elementType instanceof JavaStructureType) {
                childNode = new StructureTypeTreeNode(data,urlClassLoader,packageName);
                 ((StructureTypeTreeNode)childNode).updateChildren();
            } else if(elementType instanceof JavaArrayType) {
                childNode = new ArrayTypeTreeNode(data,urlClassLoader,packageName);
                ((ArrayTypeTreeNode)childNode).updateChildren();
            } else {
                childNode = new DefaultMutableTreeNode(data);
            }
            this.add(childNode);
        }
    }
    
}
