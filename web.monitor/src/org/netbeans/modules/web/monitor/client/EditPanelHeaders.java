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
 * EditPanelHeaders.java
 *
 *
 * Created: Fri Feb 9 2001
 *
 * @author Ana von Klopp
 * @author Simran Gleason
 * @version
 */

/**
 * Contains the Request sub-panel for the EditPanel
 */

package org.netbeans.modules.web.monitor.client; 

import java.util.ResourceBundle;
import java.awt.event.*;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.openide.util.NbBundle;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;

import org.netbeans.modules.web.monitor.data.*;

public class EditPanelHeaders extends DataDisplay {

    private final static boolean debug = false;
    private static final ResourceBundle msgs =
	NbBundle.getBundle(TransactionView.class);
    
    private DisplayTable headerTable = null;    
    private MonitorData monitorData = null;
    private EditPanel editPanel;
    private boolean setParams = false;

    //
    // Widgets
    //
    JButton newHeaderB;
    JButton editHeaderB;
    JButton deleteHeaderB;
    
    public EditPanelHeaders(MonitorData md, EditPanel editPanel) {
	super();
	this.editPanel = editPanel;
	this.monitorData = md;
    }

    //
    // stoopid versio nof redisplayData: nuke it all & start again.
    //
    public void redisplayData() {
	setData(monitorData);
    }

    // We're treating these as if they are all strings at the
    // moment. In reality they can be of different types, though maybe 
    // that does not matter...
    public void setData(MonitorData md) {

	this.monitorData = md;
	
	if(debug) System.out.println("in EditPanelHeaders.setData()"); // NOI18N
	 
	this.removeAll();
	
	// Headers
	String msg = msgs.getString("MON_HTTP_Headers"); 
	final RequestData rd = monitorData.getRequestData();
	Param[] params = rd.getHeaders().getParam();

	int gridy = -1;
	int fullGridWidth = java.awt.GridBagConstraints.REMAINDER;

	if(params == null) params = new Param[0];
	setHeaders(params);

	addGridBagComponent(this, createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    topSpacerInsets,
			    0, 0);

        headerTable.getAccessibleContext().setAccessibleName(msgs.getString("ACS_MON_HTTP_HeadersTableA11yName"));
        headerTable.setToolTipText(msgs.getString("ACS_MON_HTTP_HeadersTableA11yDesc"));
	addGridBagComponent(this, createSortButtonLabel(msg, headerTable, msgs.getString("MON_HTTP_Headers_Mnemonic").charAt(0), msgs.getString("ACS_MON_HTTP_HeadersA11yDesc")), 0, ++gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);

	JScrollPane scrollpane = new JScrollPane(headerTable);
	addGridBagComponent(this, scrollpane, 0, ++gridy,
			    fullGridWidth, 1, 1.0, 1.0, 
			    java.awt.GridBagConstraints.WEST,
			    //java.awt.GridBagConstraints.HORIZONTAL, 
			    java.awt.GridBagConstraints.BOTH,
			    tableInsets,
			    0, 0);

	newHeaderB = new JButton(msgs.getString("MON_New_header"));
        newHeaderB.setMnemonic(msgs.getString("MON_New_header_Mnemonic").charAt(0));
        newHeaderB.setToolTipText(msgs.getString("ACS_MON_New_headerA11yDesc"));
	newHeaderB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    String title = msgs.getString("MON_New_header"); 
		    ParamEditor pe = new ParamEditor("", "", //NOI18N
						     true, true, title);

		    if(debug) System.out.println("Now showing dialog");// NOI18N
		    
		    pe.showDialog(true);

		    if(debug) System.out.println("Dialog closed"); // NOI18N

		    if (pe.getDialogOK()) {

			if(debug) System.out.println("Dialog returned OK"); // NOI18N
			String name = pe.getName();
			String value = pe.getValue();
			Param newParam = new Param(name, value);
			Headers hd = monitorData.getRequestData().getHeaders();
			int nth = hd.addParam(newParam);
			if(debug) System.out.println("Headers are " // NOI18N
						     + hd.toString()); 

			redisplayData();
		    }
		}});

	deleteHeaderB = new JButton(msgs.getString("MON_Delete_header"));
        deleteHeaderB.setMnemonic(msgs.getString("MON_Delete_header_Mnemonic").charAt(0));
        deleteHeaderB.setToolTipText(msgs.getString("MON_New_header_Mnemonic"));

	deleteHeaderB.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {

		    int numRows = headerTable.getRowCount();
		    Headers hd = rd.getHeaders();

		    StringBuffer buf = new StringBuffer
			(msgs.getString("MON_Confirm_Delete_Headers")); 
		    buf.append("\n"); // NOI18N

		    for(int i=0; i<numRows; ++i) {

			if(headerTable.isRowSelected(i)) {
			    buf.append(headerTable.getValueAt(i, 0));
			    buf.append(" ");  // NOI18N
			    buf.append(headerTable.getValueAt(i, 1));
			    buf.append("\n"); // NOI18N
			}
		    }

		    showConfirmDialog(buf.toString()); 
		    
		    if(setParams) {
			for(int i=0; i<numRows; ++i) {
			    if(headerTable.isRowSelected(i)) {

				String name =
				    (String)headerTable.getValueAt(i, 0); 
				String value =
				    (String)headerTable.getValueAt(i, 1); 
				
				// Note that we get the params each
				// time through that we don't run into
				// null pointer exceptions. 
				Param[] myParams = hd.getParam();
				Param param = findParam(myParams, name, value);
				if (param != null) 
				    hd.removeParam(param);
			    }
			}
			redisplayData();
			repaint();
		    }
		}});
	
	int gridx = -1;
	addGridBagComponent(this, 
			    editPanel.createSessionButtonPanel(),
			    ++gridx, ++gridy,
			    1, 1, 1.0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    buttonInsets,
			    0, 0);
	addGridBagComponent(this, createGlue(), ++gridx, gridy,
			    1, 1, 1.0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    buttonInsets,
			    0, 0);
	addGridBagComponent(this, newHeaderB, ++gridx, gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.EAST,
			    java.awt.GridBagConstraints.NONE,
			    buttonInsets,
			    0, 0);
	/*
	addGridBagComponent(this, editHeaderB, ++gridx, gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.EAST,
			    java.awt.GridBagConstraints.NONE,
			    buttonInsets,
			    0, 0);
	*/
	addGridBagComponent(this, deleteHeaderB, ++gridx, gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.EAST,
			    java.awt.GridBagConstraints.NONE,
			    buttonInsets,
			    0, 0);

	setEnablings();
	
	// Housekeeping
	this.setMaximumSize(this.getPreferredSize()); 
	this.repaint();
    }
    
    private Param findParam(Param [] myParams, String name, String value) {

	for (int i=0; i < myParams.length; i++) {
	
	    Param param = myParams[i];
	    if (name.equals(param.getName()) &&
		value.equals(param.getValue()) ) {
		return param;
	    }
	}
	return null;
    }

    public void showConfirmDialog(String msg) {

	Object[] options = { NotifyDescriptor.OK_OPTION, 
			   NotifyDescriptor.CANCEL_OPTION 
	};
	
	NotifyDescriptor confirmDialog = 
	    new NotifyDescriptor((Object)msg, 
				 msgs.getString("MON_Confirmation_Required"),
				 NotifyDescriptor.OK_CANCEL_OPTION,
				 NotifyDescriptor.QUESTION_MESSAGE, 
				 options,
				 NotifyDescriptor.CANCEL_OPTION);

	TopManager.getDefault().notify(confirmDialog);
	if(confirmDialog.getValue().equals(NotifyDescriptor.OK_OPTION)) 
	    setParams = true;
	else 
	    setParams = false;
    }


    public void showErrorDialog() {

	Object[] options = { NotifyDescriptor.OK_OPTION };
	
	NotifyDescriptor errorDialog = 
	    new NotifyDescriptor((Object)msgs.getString("MON_Bad_header"),
				 msgs.getString("MON_Invalid_input"),
				 NotifyDescriptor.DEFAULT_OPTION,
				 NotifyDescriptor.ERROR_MESSAGE, 
				 options,
				 NotifyDescriptor.OK_OPTION);

	TopManager.getDefault().notify(errorDialog);
    }

     
    public void setEnablings() {
	//
	// Always enable the Add button.
	//
	newHeaderB.setEnabled(true);

	int selectedRows[] = headerTable.getSelectedRows();
	//
	// The edit button is enabled if exactly one row is selected
	//
	//editHeaderB.setEnabled(selectedRows.length == 1);

	//
	// The delete row button is enabled if any rows are selected.
	//
	deleteHeaderB.setEnabled(selectedRows.length > 0);
    }

    boolean tableModelChanging;
    public void setHeaders(Param[] newParams) {
	headerTable = new DisplayTable(newParams, DisplayTable.HEADERS);
	headerTable.sortByName(true);
	
	ListSelectionModel selma = headerTable.getSelectionModel();
	selma.addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent evt) {
		if(debug) System.out.println("EditPanelQuery::paramTable list selection listener"); // NOI18N
		setEnablings();
	    }
	});

	headerTable.addTableModelListener(new TableModelListener() {
	    public void tableChanged(TableModelEvent evt) {
		
		if(debug) 
		    System.out.println("Header table got table changed event"); //NOI18N
		
		if (!tableModelChanging) {
		    tableModelChanging = true;
		    //
		    // Loop through the rows and reset the params.
		    //
		    int num = headerTable.getRowCount();
		    Headers hd = monitorData.getRequestData().getHeaders();
		    Param[] params = hd.getParam();
		    
		    boolean inputOK = true;
		    
		    for(int i=0; i < num; i++) {
			String name = (String)headerTable.getValueAt(i, 0);
			name = name.trim();

			if(debug) 
			    System.out.println("Name is " + name); //NOI18N
		       
			if(name.equals("")) { // NOI18N
			    headerTable.setValueAt(params[i].getName(), i, 0);
			    inputOK = false;
			}
			String value = (String)headerTable.getValueAt(i, 1);
			value = value.trim();

			if(debug)
			    System.out.println("Value is " + value); //NOI18N

			if(value.equals("")) { // NOI18N
			    headerTable.setValueAt(params[i].getValue(), i, 1);
			    inputOK = false;
			}
			
			if(!inputOK) {
			    showErrorDialog();
			    return;
			}
			params[i].setName(name);
			params[i].setValue(value);
		    }
		    headerTable.sortByName();
		    tableModelChanging = false;
		}
	    }
	});
    }

    public void repaint() {
	super.repaint();
	if (editPanel != null) 
	    editPanel.repaint();
    }
} // EditPanelHeader

