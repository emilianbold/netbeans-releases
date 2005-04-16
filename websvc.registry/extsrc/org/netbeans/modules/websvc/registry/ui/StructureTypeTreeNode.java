/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.registry.ui;

import javax.swing.tree.*;

import org.openide.ErrorManager;

import java.net.URLClassLoader;


/**
 *
 * @author  David Botterill
 */
public class StructureTypeTreeNode extends DefaultMutableTreeNode {
    private URLClassLoader urlClassLoader;
    private String packageName;
    
    /** Creates a new instance of TypeTreeNode */
    public StructureTypeTreeNode(Object userObject,URLClassLoader inClassLoader,String inPackageName) {
        super(userObject);
        urlClassLoader = inClassLoader;
        packageName = inPackageName;
        
    }
    /**
     * This method will use the parameter name of the child to update the value of this Structure.
     * @param inData - the TypeNodeData of the child that called this method.
     * @param inChildParameterName - the name of the field to be updated.
     */
    public void updateValueOfChild(TypeNodeData inData) {
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
            ((ArrayTypeTreeNode)parentNode).updateValueOfChildren();
        } else if(null != parentNode && parentNode instanceof StructureTypeTreeNode) {
            /**
             * This type should be a JavaStructureMember
             */
            ((StructureTypeTreeNode)parentNode).updateValueOfChild(data);
        }
        
    }
    
}
