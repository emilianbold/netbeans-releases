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

/**
 *
 * @author  David Botterill
 */

import javax.swing.DefaultCellEditor;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JTextField;

import org.netbeans.swing.outline.NodeRowModel;
import org.netbeans.swing.outline.OutlineModel;

import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;

import java.awt.Dialog;
import java.util.EventObject;

import com.sun.xml.rpc.processor.model.java.JavaType;
import com.sun.xml.rpc.processor.model.java.JavaSimpleType;
import com.sun.xml.rpc.processor.model.java.JavaEnumerationType;


/**
 *
 * @author  david
 */
public class ResultCellEditor extends DefaultCellEditor implements TableCellEditor {
    
    private Dialog dialog;
    private DialogDescriptor dlg;
    private ResultViewerDialog viewerDialog;
    private Object saveValue;
    
    /** Creates a new instance of TypeCellRenderer */
    public ResultCellEditor() {
        super(new JTextField());
        this.setClickCountToStart(1);
    }
    /**
     * return the value of the last component.
     */
    public Object getCellEditorValue() {
        return saveValue;
    }
    
    public java.awt.Component getTableCellEditorComponent(javax.swing.JTable table, Object value, boolean isSelected, int row, int column) {
        saveValue = value;
        NodeRowModel rowModel = ((OutlineModel)table.getModel()).getRowNodeModel();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)rowModel.getNodeForRow(row);
        /**
         * Now depending on the type, create a component to edit/display the type.
         */
        viewerDialog = new ResultViewerDialog();
        if(null == node.getUserObject()) {
            viewerDialog.setText((String)value);
            
        } else {
            ResultNodeData data = (ResultNodeData)node.getUserObject();
            JavaType type = data.getResultType();
            
            if(type instanceof JavaSimpleType) {
                
                viewerDialog.setText(value != null ? value.toString() : "");
            }  else if(type instanceof JavaEnumerationType) {
                viewerDialog.setText(value != null ? value.toString() : "");
            } else {
                return null;
            }
            
            dlg = new DialogDescriptor(viewerDialog, data.getResultType().getRealName(),
            true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION,
            DialogDescriptor.DEFAULT_ALIGN, viewerDialog.getHelpCtx(), null);
            dlg.setOptions(new Object[] { viewerDialog.getOkButton() });
            
            dialog = DialogDisplayer.getDefault().createDialog(dlg);
            dialog.setSize(300,300);
            dialog.show();
        }
        
        
        return null;
    }
    
}
