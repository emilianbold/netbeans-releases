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
import java.lang.reflect.Field;
import javax.swing.tree.*;

import org.openide.ErrorManager;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author David Botterill
 */
public class HolderTypeTreeNode extends AbstractParameterTreeNode {
    private URLClassLoader urlClassLoader;
    private String packageName;
    
    /** Creates a new instance of HolderTypeTreeNode */
    public HolderTypeTreeNode(Object userObject,URLClassLoader inClassLoader,String inPackageName) {
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
        if(null != childData.getParameterValue()) {
            /**
             * Now we need to set the value of this Holder type to the value of the type being wrapped
             * by the holder.  The problem is that the "Holder" interface is only a tag interface that
             * doesn't offer a uniform way to set the value of a Holder.  So we are forced to check the
             * value type and cast to the appropriate Holder type.
             * @TODO For the JAX-RPC API, the Holder interface should offer a "setValue" method or there should
             * be an abstract Holder class that all of the Holders extend that offers a "setValue" method.
             */
//            Object holderValue = null;
//            try {
//                holderValue = HolderHelper.getHolderValue(childData.getParameterType(),childData.getParameterValue(),packageName,urlClassLoader);
//            } catch(WebServiceReflectionException wsfe) {
//                Throwable cause = wsfe.getCause();
//                ErrorManager.getDefault().notify(cause);
//                ErrorManager.getDefault().log(this.getClass().getName() +
//                ": Error trying to update Holder value from Children: " + childData.getParameterType().getFormalName() + "WebServiceReflectionException=" + cause);
//                
//            }
//            
            ((TypeNodeData)this.getUserObject()).setParameterValue(childData.getParameterValue());
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
         * We need to use the holder.value of the updated Holder value to set the Child held value.  Since the
         * "Holder" interface doesn't offer a uniform way of getting the Holder value, will have to get the value using
         * reflection.
         */
        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)this.getChildAt(0);
        TypeNodeData childData = (TypeNodeData)childNode.getUserObject();
        Object heldValue = null;
        Object userObject = this.getUserObject();
        if(null != userObject) {
            Object holder = ((NodeData)userObject).getNodeValue();
            try {                
                Field valueField = holder.getClass().getField("value");
                heldValue = valueField.get(holder);
            } catch(Exception wsfe) {
                Throwable cause = wsfe.getCause();
                ErrorManager.getDefault().notify(cause);
                ErrorManager.getDefault().log(this.getClass().getName() +
                ": Error trying to get the held value on Holder: " +((NodeData)userObject).getNodeType().getFormalName() + "WebServiceReflectionException=" + cause);
                
            }
            /**
             * set the value of the child node.
             */
            if(null != heldValue && childData.getParameterType() instanceof JavaArrayType) {
                /**
                 * Since the held value is an Array, we need to create an ArrayList for the node from the value.
                 */
                ArrayList newArrayList = new ArrayList(Arrays.asList((Object [])heldValue));
                childData.setParameterValue(newArrayList);
            } else {
                childData.setParameterValue(heldValue);
            }
            childNode.setUserObject(childData);
            
            
            /**
             * See if we need to continue to update children nodes.
             */
            if(childNode instanceof ArrayTypeTreeNode ||
            childNode instanceof StructureTypeTreeNode) {
                ((ParameterTreeNode)childNode).updateChildren();
                
            }
            
        }
        
    }
    
}
