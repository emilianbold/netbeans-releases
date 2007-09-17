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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.manager.ui;

import java.net.URLClassLoader;
import javax.swing.tree.DefaultMutableTreeNode;
import org.openide.ErrorManager;

/**
 *
 * @author quynguyen
 */
public class JAXBElementTreeNode extends AbstractParameterTreeNode {

    private URLClassLoader urlClassLoader;

    /** Creates a new instance of HolderTypeTreeNode */
    public JAXBElementTreeNode(TypeNodeData userObject, URLClassLoader inClassLoader) {
        super(userObject);
        urlClassLoader = inClassLoader;
    }

    /**
     * This method will use the parameter name of the child to update the value of this element
     * @param inData - the TypeNodeData of the child that called this method.
     */
    public void updateValueFromChildren(TypeNodeData inData) {
        
        DefaultMutableTreeNode localPartNode = (DefaultMutableTreeNode) this.getChildAt(0);
        DefaultMutableTreeNode valueNode = (DefaultMutableTreeNode)this.getChildAt(1);
        
        TypeNodeData localPartData = (TypeNodeData) localPartNode.getUserObject();
        TypeNodeData valueData = (TypeNodeData) valueNode.getUserObject();
        
        Object value = valueData.getTypeValue();
        String localPart = (String)localPartNode.getUserObject();
        if (value != null && localPart != null) {
            Object jaxBElement = ((TypeNodeData) this.getUserObject()).getTypeValue();
            try {
                if (jaxBElement != null) {
                    ReflectionHelper.setJAXBElementValue(jaxBElement, value);
                }else {
                    jaxBElement = ReflectionHelper.makeJAXBElement(valueData.getTypeClass(), localPart, value, urlClassLoader);
                    this.setUserObject(jaxBElement);
                }
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }

    /**
     * Update the child nodes based on the value of this UserObject.
     *
     */
    public void updateChildren() {
        DefaultMutableTreeNode localPartNode = (DefaultMutableTreeNode) this.getChildAt(0);
        DefaultMutableTreeNode valueNode = (DefaultMutableTreeNode)this.getChildAt(1);
        
        TypeNodeData localPartData = (TypeNodeData)localPartNode.getUserObject();
        TypeNodeData valueData = (TypeNodeData) valueNode.getUserObject();
        
        Object jaxBElement = this.getUserObject();
        if (null != jaxBElement) {
            Object holder = ((TypeNodeData) jaxBElement).getTypeValue();
            try {
                Object value = ReflectionHelper.getJAXBElementValue(jaxBElement);
                String localPart = ReflectionHelper.getQNameLocalPart(jaxBElement);
                
                localPartData.setTypeValue(localPart);
                localPartNode.setUserObject(localPartData);
                
                valueData.setTypeValue(value);
                valueNode.setUserObject(valueData);
            } catch (Exception wsfe) {
                Throwable cause = wsfe.getCause();
                ErrorManager.getDefault().notify(cause);
                ErrorManager.getDefault().log(this.getClass().getName() + ": Error trying to get values from JAXBElement: " + ((TypeNodeData) userObject).getRealTypeName() + "WebServiceReflectionException=" + cause);
                return;
            }
            



            /**
             * See if we need to continue to update children nodes.
             */
            if (valueNode instanceof ListTypeTreeNode || valueNode instanceof StructureTypeTreeNode) {
                ((ParameterTreeNode) valueNode).updateChildren();
            }
        }
    }
}