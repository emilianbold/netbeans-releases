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

import java.util.Collection;
import javax.swing.tree.*;
import java.util.Iterator;

import java.net.URLClassLoader;

/**
 * Node for Collection and List types
 * 
 * @author  quynguyen
 */
public class ListTypeTreeNode extends AbstractParameterTreeNode {
    private URLClassLoader urlClassLoader;
    private String packageName;
    
    public ListTypeTreeNode(TypeNodeData userObject,URLClassLoader inClassLoader,String inPackageName) {
        super(userObject);
        urlClassLoader = inClassLoader;
        packageName = inPackageName;
        
    }
    
    
    public void updateValueFromChildren(TypeNodeData inData) {
        /**
         * create a new ArrayList from all of the child values.
         */
        TypeNodeData data = (TypeNodeData)this.getUserObject();
        
        Collection c = (Collection)data.getTypeValue();
        if (c == null) return;
        
        c.clear();
        for(int ii=0; ii < this.getChildCount(); ii++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)this.getChildAt(ii);
            TypeNodeData childData = (TypeNodeData)childNode.getUserObject();
            if(null != childData.getTypeValue()) {
                c.add(childData.getTypeValue());
            }
        }
        
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) this.getParent();
        if (parentNode != null && parentNode instanceof ParameterTreeNode) {
            ((ParameterTreeNode)parentNode).updateValueFromChildren(data);
        }
    }
    /**
     * Update the child nodes based on the value of this UserObject.
     * Fix for Bug: 5059732
     * - David Botterill 8/12/2004
     *
     */
    public void updateChildren() {
        TypeNodeData thisData = (TypeNodeData)this.getUserObject();
        /**
         * First get the Collection for this node.
         */
        Collection childCollection = (Collection)thisData.getTypeValue();
        /**
         * Next we need to delete all of the child nodes
         */
        this.removeAllChildren();
        /**
         * For each entry in the array, make a child node
         */
        String structureType = thisData.getGenericType();
        if (structureType == null || structureType.length() == 0) {
            structureType = "java.lang.Object"; // NOI18N
        }
        
        Iterator iter = childCollection.iterator();
        for (int i = 0; iter.hasNext(); i++) {
            TypeNodeData data = ReflectionHelper.createTypeData(structureType, "[" + i + "]", iter.next());
            data.setAssignable(thisData.isAssignable());
            if (ReflectionHelper.isComplexType(data.getTypeClass(), urlClassLoader)) {
                StructureTypeTreeNode childNode = new StructureTypeTreeNode(data,urlClassLoader,packageName);
                childNode.updateChildren();
                this.add(childNode);
            }else if (ReflectionHelper.isCollection(data.getTypeClass(), urlClassLoader)) {
                ListTypeTreeNode childNode = new ListTypeTreeNode(data,urlClassLoader,packageName);
                childNode.updateChildren();
                this.add(childNode);
            }else {
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(data);
                this.add(childNode);
            }
        }
    }
    
}
