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

import java.util.ArrayList;
import java.net.URLClassLoader;
import javax.swing.tree.DefaultMutableTreeNode;
import org.openide.ErrorManager;

/**
 *
 * @author  David Botterill
 */
public class ArrayTypeTreeNode extends AbstractParameterTreeNode {
    private URLClassLoader urlClassLoader;
    
    public ArrayTypeTreeNode(Object userObject,URLClassLoader inClassLoader) {
        super(userObject);
        urlClassLoader = inClassLoader;
        
    }
    
    public void updateValueFromChildren(TypeNodeData inData) {
        /**
         * create a new ArrayList from all of the child values.
         */
        TypeNodeData data = (TypeNodeData)this.getUserObject();
        ArrayList<Object> newList = new ArrayList<Object>();
        
        for(int ii=0; ii < this.getChildCount(); ii++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)this.getChildAt(ii);
            TypeNodeData childData = (TypeNodeData)childNode.getUserObject();
            if(null != childData.getTypeValue()) {
                newList.add(childData.getTypeValue());
            }
        }
        Object[] arr = newList.toArray();
        
        data.setTypeValue(arr);
        
        // Update the parent node
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) this.getParent();
        if (parentNode != null && parentNode instanceof ParameterTreeNode) {
            ((ParameterTreeNode)parentNode).updateValueFromChildren(data);
        }
    }
    
    /**
     * Update the child nodes based on the value of this UserObject.
     *
     */
    public void updateChildren() {
        TypeNodeData thisData = (TypeNodeData)this.getUserObject();
        Object arrayObj = thisData.getTypeValue();
        
        this.removeAllChildren();
        //For each entry in the array, make a child node
        try {
            String genericType = thisData.getGenericType();
            int arrayLength = ReflectionHelper.getArrayLength(arrayObj);
            for (int i = 0; i < arrayLength; i++) {
                Object entry = ReflectionHelper.getArrayValue(arrayObj, i);
                TypeNodeData entryData = ReflectionHelper.createTypeData(genericType, "[" + i + "]", entry); // NOI18N
                
                DefaultMutableTreeNode entryNode = NodeHelper.getInstance().createNodeFromData(entryData);
                this.add(entryNode);
            }
        }catch (Exception ex) {
            Throwable cause = ex.getCause();
            ErrorManager.getDefault().notify(cause);
            ErrorManager.getDefault().log(this.getClass().getName() + 
                    ": Error using reflection on array: " + thisData.getRealTypeName() + "WebServiceReflectionException=" + cause); // NOI18N
        }
    }
    
}
