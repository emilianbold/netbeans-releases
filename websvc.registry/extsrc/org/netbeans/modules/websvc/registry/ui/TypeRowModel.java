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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.registry.ui;

import org.netbeans.swing.outline.RowModel;
import org.openide.ErrorManager;


import com.sun.xml.rpc.processor.model.java.JavaType;
import com.sun.xml.rpc.processor.model.java.JavaSimpleType;
import com.sun.xml.rpc.processor.model.java.JavaEnumerationType;

import org.openide.util.NbBundle;
import javax.swing.tree.DefaultMutableTreeNode;

import java.net.URLClassLoader;

/**
 *
 * @author  David Botterill
 */
public class TypeRowModel implements RowModel {

    private URLClassLoader urlClassLoader;
    private String packageName;
    
    /** Creates a new instance of TypeRowModel */
    public TypeRowModel(URLClassLoader inClassLoader, String inPackageName) {
        urlClassLoader = inClassLoader;
        packageName = inPackageName;
    
    }
    
    public Class getColumnClass(int column) {
        switch(column) {
         //   case 0: return String.class;
            case 0: return String.class;
            case 1: return Object.class;
            default: return String.class;
        }
    }
    
    public int getColumnCount() {
        return 2;
    }
    
    public String getColumnName(int column) {
        switch(column) {
           // case 0: return NbBundle.getMessage(TypeRowModel.class, "PARAM_CLASS");
            case 0: return NbBundle.getMessage(TypeRowModel.class, "PARAM_NAME");
            case 1: return NbBundle.getMessage(TypeRowModel.class, "PARAM_VALUE");
            default: return "";
        }
        
    }
    
    public Object getValueFor(Object inNode, int column) {
        if(null == inNode) return null;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)inNode;
        if(null == node.getUserObject()) return null;
        TypeNodeData data = (TypeNodeData)node.getUserObject();
        switch(column) {
       //     case 0: return data.getParameterType().getRealName();
            case 0: return data.getParameterName();
            case 1: {
                Object val = data.getParameterValue();
                if (val instanceof java.util.Calendar)
                    return java.text.DateFormat.getDateTimeInstance().format(((java.util.Calendar)val).getTime());
                return val;
            }
            default: return "";
        }
        
    }
    
    public boolean isCellEditable(Object inNode, int column) {
        if(null == inNode) return false;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)inNode;
        if(null == node.getUserObject()) return false;
        
        TypeNodeData data = (TypeNodeData)node.getUserObject();
        switch(column) {
         //   case 0: return false;
            case 0: return false;
            case 1: if(data.getParameterType() instanceof JavaSimpleType ||
                       data.getParameterType() instanceof JavaEnumerationType) return true;
                    else return false;
            default: return false;
        }
        
    }
    
    public void setValueFor(Object inNode, int column, Object value) {
        if(null == inNode) return;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)inNode;
        if(null == node.getUserObject()) return;
        
        TypeNodeData data = (TypeNodeData)node.getUserObject();
        /**
         * Make sure they are only trying to edit the value column
         */
        if(column != 1) {
            return;
        }
        JavaType type = data.getParameterType();
        Object newValue = null;
        if(type instanceof JavaEnumerationType) {
            try {
                newValue = ReflectionHelper.makeEnumerationType((JavaEnumerationType)type,urlClassLoader,packageName,value);
            } catch(WebServiceReflectionException wsfe) {
                Throwable cause = wsfe.getCause();
                ErrorManager.getDefault().notify(cause);
                ErrorManager.getDefault().log(TypeRowModel.class.getName() +
                ": Error trying to create an Enumeration Type: " +
                type.getFormalName() + "ClassNWebServiceReflectionExceptionotFoundException=" + cause);
                return;
            }
            
            
        } else {
            newValue = value;
            
        }
        
        data.setParameterValue(newValue);
        /**
         * If this node's parent is a JavaStructureType or JavaArrayType, update this value on the parent's
         * value.
         */
        
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.getParent();
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
