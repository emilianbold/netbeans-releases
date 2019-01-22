/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
 * 
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
