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

import javax.swing.tree.*;

import org.openide.ErrorManager;
import java.net.URLClassLoader;

/**
 *
 * @author David Botterill
 */
public class HolderTypeTreeNode extends AbstractParameterTreeNode {
    private URLClassLoader urlClassLoader;
    private String packageName;
    
    /** Creates a new instance of HolderTypeTreeNode */
    public HolderTypeTreeNode(TypeNodeData userObject,URLClassLoader inClassLoader,String inPackageName) {
        super(userObject);
        urlClassLoader = inClassLoader;
        packageName = inPackageName;
        
    }
    /**
     * This method will use the parameter name of the child to update the value of this Holder.
     * @param inData - the TypeNodeData of the child that called this method.
     */
    public void updateValueFromChildren(TypeNodeData inData) {
        /**
         * create a new Holder from the child values.
         */
        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)this.getChildAt(0);
        TypeNodeData childData = (TypeNodeData)childNode.getUserObject();
        Object holderValue = childData.getTypeValue();
        if(holderValue != null) {
            Object holder = ((TypeNodeData)this.getUserObject()).getTypeValue();
            try {
                ReflectionHelper.setHolderValue(holder, holderValue);
            }catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
            
        }
    }
    
    /**
     * Update the child nodes based on the value of this UserObject.
     * Fix for Bug: 5059732
     * - David Botterill 8/12/2004
     *
     */
    
    public void updateChildren() {
        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)this.getChildAt(0);
        TypeNodeData childData = (TypeNodeData)childNode.getUserObject();
        Object heldValue = null;
        Object userObject = this.getUserObject();
        if(null != userObject) {
            Object holder = ((TypeNodeData)userObject).getTypeValue();
            try {
                heldValue = ReflectionHelper.getHolderValue(holder);
            } catch(Exception wsfe) {
                Throwable cause = wsfe.getCause();
                ErrorManager.getDefault().notify(cause);
                ErrorManager.getDefault().log(this.getClass().getName() +
                ": Error trying to get the held value on Holder: " +((TypeNodeData)userObject).getRealTypeName() + "WebServiceReflectionException=" + cause);
                
            }
            /**
             * set the value of the child node.
             */
            childData.setTypeValue(heldValue);
            childNode.setUserObject(childData);
            
            
            /**
             * See if we need to continue to update children nodes.
             */
            if (childNode instanceof ParameterTreeNode) {
                ((ParameterTreeNode)childNode).updateChildren();
            }
        }
        
    }
    
}
