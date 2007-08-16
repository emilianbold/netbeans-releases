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

import com.sun.tools.ws.processor.model.java.JavaStructureMember;
import com.sun.tools.ws.processor.model.java.JavaStructureType;
import java.net.URLClassLoader;
import java.util.Iterator;
import javax.swing.tree.DefaultMutableTreeNode;
import org.openide.ErrorManager;

/**
 *
 * @author  David Botterill
 */
public class StructureTypeTreeNode extends AbstractParameterTreeNode {
    private URLClassLoader urlClassLoader;
    private String packageName;
    
    public StructureTypeTreeNode(Object userObject,URLClassLoader inClassLoader,String inPackageName) {
        super(userObject);
        urlClassLoader = inClassLoader;
        packageName = inPackageName;
        
    }
    
    /**
     * This method will use the parameter name of the child to update the value of this Structure.
     * @param inData - the TypeNodeData of the child that called this method.
     */
    public void updateValueFromChildren(TypeNodeData inData) {
        TypeNodeData data = (TypeNodeData)this.getUserObject();
        try {
            ReflectionHelper.setStructureValue(data,inData,urlClassLoader,packageName);
        } catch(WebServiceReflectionException wsfe) {
            Throwable cause = wsfe.getCause();
            ErrorManager.getDefault().notify(cause);
            ErrorManager.getDefault().log(this.getClass().getName() +
            ": Error trying to update Children of a Structure on: " + data.getParameterType().getFormalName() + "WebServiceReflectionException=" + cause);
            
        }
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
        
        TypeNodeData data = (TypeNodeData)this.getUserObject();
        JavaStructureType structureType = (JavaStructureType)data.getNodeType();
        Iterator memberIterator = structureType.getMembers();
        JavaStructureMember currentMember = null;
        for(int ii=0; null != memberIterator && memberIterator.hasNext(); ii++) {
            /**
             * get the JavaStructureMember for this child.
             */
            currentMember = (JavaStructureMember)memberIterator.next();
            /**
             * Get the data for this child.
             */
            DefaultMutableTreeNode currentChildNode = (DefaultMutableTreeNode)this.getChildAt(ii);
            TypeNodeData childData = (TypeNodeData)currentChildNode.getUserObject();
            /**
             * Set the new value for this child.
             */
            Object newChildValue = null;
            try {
                newChildValue = ReflectionHelper.getStructureValue(data,currentMember,urlClassLoader,packageName);
            } catch(WebServiceReflectionException wsfe) {
                Throwable cause = wsfe.getCause();
                ErrorManager.getDefault().notify(cause);
                ErrorManager.getDefault().log(this.getClass().getName() +
                ": Error trying to update Children of a Structure on: " + data.getParameterType().getFormalName() + "WebServiceReflectionException=" + cause);
                
            }
            childData.setParameterValue(newChildValue);
            currentChildNode.setUserObject(childData);
            
            /**
             * See if we need to continue to update children nodes.
             */
            if(currentChildNode instanceof ArrayTypeTreeNode ||
            currentChildNode instanceof StructureTypeTreeNode) {
                ((ParameterTreeNode)currentChildNode).updateChildren();
                
            }
        }
        
        
        
    }
    
}
