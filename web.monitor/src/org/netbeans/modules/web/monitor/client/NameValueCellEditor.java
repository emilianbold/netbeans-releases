/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
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

import org.openide.TopManager;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;

import org.openide.util.NbBundle;

/* Should this one get the events from the Param editor? MAO, far
 * Param editorn vara non-modal? */
public class NameValueCellEditor extends DefaultCellEditor  {

    private final static boolean debug = false;
    private static final ResourceBundle msgs =
	NbBundle.getBundle(TransactionView.class);
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

	JButton b = new JButton(msgs.getString("MON_Edit_dots")); // NOI18N
	if(type == DisplayTable.UNEDITABLE) 
	    b.setToolTipText(msgs.getString("MON_DisplayValue")) ;
	else 
	    b.setToolTipText(msgs.getString("MON_EditAttribute")) ;
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
	return msgs.getString("MON_Edit_dots");
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

	ParamEditor pe;

	// The editor is modal unless it is used for viewing only
	boolean modal = (type > DisplayTable.UNEDITABLE);
	
	int currentRow = table.getSelectedRow();
	String title;
	TableModel model = table.getModel();

	if(debug) 
	    System.out.println("type = " + String.valueOf(type)); //NOI18N

	if(type == DisplayTable.UNEDITABLE) 
	    title = msgs.getString("MON_ParamValue"); 
	else if(type == DisplayTable.HEADERS) 
	    title = msgs.getString("MON_Edit_header"); 
	else if(type == DisplayTable.PARAMS) 
	    title = msgs.getString("MON_Edit_param"); 
	else if(type == DisplayTable.REQUEST) 
	    title = msgs.getString("MON_Edit_request"); 
	else if(type == DisplayTable.COOKIES) 
	    title = msgs.getString("MON_Edit_cookie"); 
	else if(type == DisplayTable.SERVER) 
	    title = msgs.getString("MON_Edit_server"); 
	// This should not happen
	else 
	    title = msgs.getString("MON_Edit_value"); 
	

	String name =  (String)model.getValueAt(currentRow, 0);
	String value = (String)model.getValueAt(currentRow, 1);
	pe = new ParamEditor(name, 
			     value, 
			     nameEditable, 
			     (type > DisplayTable.UNEDITABLE), 
			     title);

	pe.showDialog(modal);

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
