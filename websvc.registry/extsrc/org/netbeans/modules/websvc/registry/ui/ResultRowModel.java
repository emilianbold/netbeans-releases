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
        //    case 0: return NbBundle.getMessage(this.getClass(), "PARAM_CLASS");
            case 0: return NbBundle.getMessage(this.getClass(), "PARAM_VALUE");
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
            case 0: return data.getNodeValue();
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
