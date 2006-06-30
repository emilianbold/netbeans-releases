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

/**
 * NameValueCellEditor.java
 *
 *
 * Created: Thursday Feb  15
 *
 * @author Simran Gleason
 * @author Ana von Klopp
 * @version
 */


package org.netbeans.modules.web.monitor.client;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.event.*;
import java.util.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.*;
import org.openide.util.HelpCtx;

import org.openide.util.NbBundle;

/* Should this one get the events from the Param editor? MAO, far
 * Param editorn vara non-modal? */
public class NameValueCellEditor extends DefaultCellEditor  {

    private final static boolean debug = false;
    private static String editNameAndValueTitle;
    private static String editValueOnlyTitle;

    private JTable table;
    private Object[][] data;
    private boolean nameEditable;
    private int row;
    private int type;

    public static NameValueCellEditor createCellEditor(JTable table,
						       Object data [][],
						       boolean nameEditable,
						       int row, final int type)  {

	JButton b = new JButton(NbBundle.getMessage(NameValueCellEditor.class, "MON_Edit_dots")); // NOI18N
	if(type == DisplayTable.UNEDITABLE) 
	    b.setToolTipText(NbBundle.getMessage(NameValueCellEditor.class, "MON_DisplayValue")) ;
	else 
	    b.setToolTipText(NbBundle.getMessage(NameValueCellEditor.class, "MON_EditAttribute")) ;
	final NameValueCellEditor ed = new NameValueCellEditor(b,
							       table,
							       data,
							       nameEditable,
							       row, type);

	b.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent evt) {
		ed.showParamEditor();
	    }
	});

	return ed;
    }

					
    public NameValueCellEditor(JButton b,
			       JTable table,
			       Object data [][],
			       boolean nameEditable,
			       int row, 
			       int type)  {
	super(new JCheckBox());
	editorComponent = b;
	setClickCountToStart(1); 

	this.table = table;
	this.data = data;
	this.nameEditable = nameEditable;
	this.row = row;    
	this.type = type;
    }

    
    protected void fireEditingStopped() {
	super.fireEditingStopped();
    }
    
    public Object getCellEditorValue() {
	return NbBundle.getMessage(NameValueCellEditor.class, "MON_Edit_dots");
    }
 
    public Component getTableCellEditorComponent(JTable table, 
						 Object value,
						 boolean isSelected,
						 int row,
						 int column) {
	((JButton)editorComponent).setText(value.toString());
	return editorComponent;
    }


    public void showParamEditor() {

	int currentRow = table.getSelectedRow();
	TableModel model = table.getModel();
	String name =  (String)model.getValueAt(currentRow, 0);
	String value = (String)model.getValueAt(currentRow, 1);

	ParamEditor.Condition condition = ParamEditor.Condition.NONE; 
	ParamEditor.Editable editable = ParamEditor.Editable.BOTH; 
	String title = null; 
	
	if(debug) 
	    System.out.println("type = " + String.valueOf(type)); //NOI18N

	if(type == DisplayTable.UNEDITABLE) {
	    editable = ParamEditor.Editable.NEITHER;
	    title = NbBundle.getMessage(NameValueCellEditor.class, 
					"MON_ParamValue"); 
	}
	else if(type == DisplayTable.HEADERS) {
	    title = NbBundle.getMessage(NameValueCellEditor.class, 
					"MON_Edit_header"); 
	    condition = ParamEditor.Condition.HEADER; 
	}
	else if(type == DisplayTable.PARAMS) 
	    title = NbBundle.getMessage(NameValueCellEditor.class, 
					"MON_Edit_param");  
	else if(type == DisplayTable.REQUEST) {
	    editable = ParamEditor.Editable.VALUE;
	    title = NbBundle.getMessage(NameValueCellEditor.class, 
					"MON_Edit_request"); 
	    condition = ParamEditor.Condition.VALUE;
	}
	else if(type == DisplayTable.COOKIES) {
	    title = NbBundle.getMessage(NameValueCellEditor.class, 
					"MON_Edit_cookie"); 
	    condition = ParamEditor.Condition.COOKIE; 
	}
	else if(type == DisplayTable.SERVER) {
	    title = NbBundle.getMessage(NameValueCellEditor.class, 
					"MON_Edit_server"); 
	    condition = ParamEditor.Condition.VALUE; 
	    editable = ParamEditor.Editable.VALUE;
	}
	// This should not happen
	else 
	    title = NbBundle.getMessage(NameValueCellEditor.class, "MON_Edit_value"); 
	


	ParamEditor pe = new ParamEditor(name, value, editable, condition,
					 title); 

	pe.showDialog(); 

	if(debug) 
	    System.out.println("NameValueCellEditor::has " + //NOI18N
			       pe.getName() + " " + pe.getValue());//NOI18N

	if ((type > DisplayTable.UNEDITABLE) && pe.getDialogOK()) {
	    if(debug) System.out.println("Updating the model");//NOI18N
	    
	    if (nameEditable) {
		model.setValueAt(pe.getName(), currentRow, 0);
		if(debug) System.out.println("Updated the name");//NOI18N
	    }
	    model.setValueAt(pe.getValue(), currentRow, 1);
	    if(debug) System.out.println("Updated the value");//NOI18N
	}
    }
} // NameValueCellEditor
