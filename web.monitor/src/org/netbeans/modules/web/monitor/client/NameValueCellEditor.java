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

public class NameValueCellEditor extends DefaultCellEditor {

    private final static boolean debug = false;
    private static final ResourceBundle msgs =
	NbBundle.getBundle(TransactionView.class);
    private static String editNameAndValueTitle;
    private static String editValueOnlyTitle;

    private DisplayTable table;
    private Object[][] data;
    private boolean nameEditable;
    private int row;
    private int type;
    
    public static NameValueCellEditor createCellEditor(DisplayTable table,
						       Object data [][],
						       boolean nameEditable,
						       int row, int type)  {
	JButton b = new JButton(msgs.getString("MON_Edit_dots")); // NOI18N
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
			       DisplayTable table,
			       Object data [][],
			       boolean nameEditable,
			       int row, 
			       int type)  {
	super(new JCheckBox());
	editorComponent = b;
	setClickCountToStart(1); //This is usually 1 or 2.

	this.table = table;
	this.data = data;
	this.nameEditable = nameEditable;
	this.row = row;    
	this.type = type;
	
	//Must do this so that editing stops when appropriate.
	b.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		// is this screwing us up?
		//fireEditingStopped();
	    }
	});
	

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
	boolean modal = true;
	int currentRow = table.getSelectedRow();

	TableModel model = table.getModel();
	String name =  (String)model.getValueAt(currentRow, 0);
	String value = (String)model.getValueAt(currentRow, 1);
	String title;
	if (nameEditable) { 
	    if(debug) 
		System.out.println("type = " + String.valueOf(type));
	     
	    if(type == DisplayTable.HEADERS) {
		title = msgs.getString("MON_Edit_header"); 
	    }
	    else if(type == DisplayTable.PARAMS) {
		title = msgs.getString("MON_Edit_param"); 
	    }
	    else if(type == DisplayTable.REQUEST) {
		title = msgs.getString("MON_Edit_request"); 
	    }
	    else if(type == DisplayTable.SERVER) {
		title = msgs.getString("MON_Edit_server"); 
	    }
	    // This should not happen
	    else {
		title = msgs.getString("MON_Edit_value"); 
	    }
	    
	} else {
	    title = msgs.getString("MON_Edit_value"); 
	}
	pe = new ParamEditor(name, value, nameEditable, title);

	pe.showDialog(modal);
	if (pe.getDialogOK()) {
	    if (nameEditable) {
		model.setValueAt(pe.getName(), currentRow, 0);
	    }
	    model.setValueAt(pe.getValue(), currentRow, 1);
	}
    }

} // NameValueCellEditor
