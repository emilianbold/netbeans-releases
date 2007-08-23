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

import com.sun.xml.rpc.processor.model.java.JavaArrayType;
import java.lang.reflect.Array;
import org.netbeans.swing.outline.RowModel;
import org.openide.ErrorManager;


import com.sun.xml.rpc.processor.model.java.JavaType;
import com.sun.xml.rpc.processor.model.java.JavaSimpleType;
import com.sun.xml.rpc.processor.model.java.JavaEnumerationType;

import org.openide.util.NbBundle;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author  David Botterill
 */
public class ResultRowModel implements RowModel {
    
    /** Creates a new instance of TypeRowModel */
    public ResultRowModel() {
    }
    
    public Class getColumnClass(int column) {
        switch(column) {
         //   case 0: return String.class;
            case 0: return Object.class;
            default: return String.class;
        }
    }
    
    public int getColumnCount() {
        return 1;
    }
    
    public String getColumnName(int column) {
        switch(column) {
        //    case 0: return NbBundle.getMessage(ResultRowModel.class, "PARAM_CLASS");
            case 0: return NbBundle.getMessage(ResultRowModel.class, "PARAM_VALUE");
            default: return "";
        }
        
    }
    
    public Object getValueFor(Object inNode, int column) {
        if(null == inNode) return null;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)inNode;
        if(null == node.getUserObject()) return null;
        NodeData data = (NodeData)node.getUserObject();

        switch(column) {
       //     case 0: return data.getResultType().getRealName();
            case 0: {
                if (data.getNodeType() instanceof JavaArrayType) {
                    return "[]";
                }
                Object val = data.getNodeValue();
                if (val instanceof java.util.Calendar)
                    return java.text.DateFormat.getDateTimeInstance().format(((java.util.Calendar)val).getTime());
                return val;
            }
            default: return "";
        }
        
    }
    
    public boolean isCellEditable(Object inNode, int column) {
        return true;
    }
    
    public void setValueFor(Object inNode, int column, Object value) {
        return;
    }
}
