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
 * @author Ana von Klopp Lemon
 * @author Simran Gleason
 * @version
 */

/**
 * Contains the Request sub-panel for the EditPanel
 */

package org.netbeans.modules.web.monitor.client; 

import java.util.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.openide.util.NbBundle;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;


import org.netbeans.modules.web.monitor.data.*;

public class EditPanelHeaders extends javax.swing.JPanel {

    private final static boolean debug = false;
    private static final ResourceBundle msgs =
	NbBundle.getBundle(TransactionView.class);
    
    private static final Dimension size = new Dimension(500, 550);
    private static final Dimension reqSize = new Dimension(450, 100);
    private static final Dimension tableSize = new Dimension(450, 100);
   
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
	
	if(debug) System.out.println("in RequestDisplay.setData()"); // NOI18N
	 
	this.removeAll();
	this.setLayout(new GridBagLayout());
	
	// Headers
	String msg = msgs.getString("MON_HTTP_Headers"); 
	final RequestData rd = monitorData.getRequestData();
	Param[] params = rd.getHeaders().getParam();

	int gridy = -1;
	int fullGridWidth = java.awt.GridBagConstraints.REMAINDER;

	if(params == null) params = new Param[0];
	setHeaders(params);

	addGridBagComponent(this, TransactionView.createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    TransactionView.topSpacerInsets,
			    0, 0);

	addGridBagComponent(this, TransactionView.createSortButtonLabel(msg, headerTable), 0, ++gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    TransactionView.labelInsets,
			    0, 0);

	JScrollPane scrollpane = new JScrollPane(headerTable);
	//headerTable.setPreferredScrollableViewportSize(tableSize);
	/*
	Dimension d = headerTable.getPreferredSize();
	if(debug) {
	    System.out.println("EditPanelHeaders:: table size is " +
			       String.valueOf(d));
	    System.out.println("EditPanelHeaders:: real size is " +
			       String.valueOf(headerTable.getSize()));
	    System.out.println("EditPanelHeaders:: default size is " +
			       String.valueOf(tableSize));
	}
	Dimension useD = null;
	if(d.getHeight() < tableSize.getHeight())
	    useD = d;
	else useD = tableSize;
	scrollpane.setSize(useD); 
	if(debug) {
	    System.out.println("EditPanelHeaders:: table size is " +
			       String.valueOf(scrollpane.getSize()));
	}
	*/
	addGridBagComponent(this, scrollpane, 0, ++gridy,
			    fullGridWidth, 1, 1.0, 1.0, 
			    java.awt.GridBagConstraints.WEST,
			    //java.awt.GridBagConstraints.HORIZONTAL, 
			    java.awt.GridBagConstraints.BOTH,
			    TransactionView.tableInsets,
			    0, 0);

	newHeaderB = new JButton(msgs.getString("MON_New_header"));
	newHeaderB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    String title = msgs.getString("MON_New_header"); 
		    ParamEditor pe = new ParamEditor("", "", true, title);

		    if(debug) System.out.println("Now showing dialog");// NOI18N
		    
		    pe.showDialog(true);

		    if(debug) System.out.println("Dialog closed"); // NOI18N

		    if (pe.getDialogOK()) {

			if(debug) System.out.println("Dialog returned OK"); // NOI18N
			String name = pe.getName();
			String value = pe.getValue();
			Param newParam = new Param(name, value);
			Headers hd = monitorData.getRequestData().getHeaders();
			//
			// PENDING: Check for duplicate headers.
			//   Headers cannot be duplicated.
			//   if we find one, we put up a warning dialog.
			//   
			int nth = hd.addParam(newParam);
			if(debug) 
			    System.out.println("Headers are " + hd.toString()); // NOI18N
			redisplayData();
		    }
		}});

	deleteHeaderB = new JButton(msgs.getString("MON_Delete_header"));

	deleteHeaderB.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {

		    int numRows = headerTable.getRowCount();
		    Headers hd = rd.getHeaders();

		    StringBuffer buf = new StringBuffer
			(msgs.getString("MON_Confirm_Delete_Headers")); 
		    buf.append("\n");

		    for(int i=0; i<numRows; ++i) {

			if(headerTable.isRowSelected(i)) {
			    buf.append(headerTable.getValueAt(i, 0));
			    buf.append(" ");
			    buf.append(headerTable.getValueAt(i, 1));
			    buf.append("\n");
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
	addGridBagComponent(this, Box.createGlue(), ++gridx, ++gridy,
			    1, 1, 1.0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    TransactionView.buttonInsets,
			    0, 0);
	addGridBagComponent(this, newHeaderB, ++gridx, gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.EAST,
			    java.awt.GridBagConstraints.NONE,
			    TransactionView.buttonInsets,
			    0, 0);
	/*
	addGridBagComponent(this, editHeaderB, ++gridx, gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.EAST,
			    java.awt.GridBagConstraints.NONE,
			    TransactionView.buttonInsets,
			    0, 0);
	*/
	addGridBagComponent(this, deleteHeaderB, ++gridx, gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.EAST,
			    java.awt.GridBagConstraints.NONE,
			    TransactionView.buttonInsets,
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

    private void addGridBagComponent(Container parent,
				     Component comp,
				     int gridx, int gridy,
				     int gridwidth, int gridheight,
				     double weightx, double weighty,
				     int anchor, int fill,
				     Insets insets,
				     int ipadx, int ipady) {
	GridBagConstraints cons = new GridBagConstraints();
	cons.gridx = gridx;
	cons.gridy = gridy;
	cons.gridwidth = gridwidth;
	cons.gridheight = gridheight;
	cons.weightx = weightx;
	cons.weighty = weighty;
	cons.anchor = anchor;
	cons.fill = fill;
	cons.insets = insets;
	cons.ipadx = ipadx;
	cons.ipady = ipady;
	parent.add(comp,cons);
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
			if(name.equals("")) {
			    headerTable.setValueAt(params[i].getName(), i, 0);
			    inputOK = false;
			}
			String value = (String)headerTable.getValueAt(i, 1);
			value = value.trim();
			if(value.equals("")) {
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

