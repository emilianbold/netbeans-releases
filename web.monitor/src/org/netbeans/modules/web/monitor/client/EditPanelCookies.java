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
 * @version
 */

/**
 * Contains the Cookie sub-panel for the EditPanel
 */

package org.netbeans.modules.web.monitor.client; 

import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
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

public class EditPanelCookies extends DataDisplay {

    private final static boolean debug = false;
    private static final ResourceBundle msgs =
	NbBundle.getBundle(TransactionView.class);
    
    private DisplayTable cookieTable = null;    
    private MonitorData monitorData = null;
    private EditPanel editPanel;
    private boolean setCookies = false;

    //
    // Widgets
    //
    JButton newCookieB;
    JButton editCookieB;
    JButton deleteCookieB;
    
    public EditPanelCookies(MonitorData md, EditPanel editPanel) {
	super();
	this.editPanel = editPanel;
	this.monitorData = md;
    }

    //
    // Redesign this, inefficient. 
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
	
	// Cookies
	String msg = msgs.getString("MON_Cookies_4"); 

	// We get the cookies data from the recorded cookies list
	Param[] params = monitorData.getRequestData().getCookiesAsParams(); 
	setCookies(params);
	 
	int gridy = -1;
	int fullGridWidth = java.awt.GridBagConstraints.REMAINDER;

	addGridBagComponent(this, createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    topSpacerInsets,
			    0, 0);

        cookieTable.getAccessibleContext().setAccessibleName(msgs.getString("ACS_MON_CookiesTableA11yName"));
        cookieTable.setToolTipText(msgs.getString("ACS_MON_CookiesTableA11yDesc"));
	addGridBagComponent(this, createSortButtonLabel(msg, cookieTable, msgs.getString("MON_Cookies_Mnemonic").charAt(0), msgs.getString("ACS_MON_CookiesA11yDesc")), 0, ++gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);

	JScrollPane scrollpane = new JScrollPane(cookieTable);
	addGridBagComponent(this, scrollpane, 0, ++gridy,
			    fullGridWidth, 1, 1.0, 1.0, 
			    java.awt.GridBagConstraints.WEST,
			    //java.awt.GridBagConstraints.HORIZONTAL, 
			    java.awt.GridBagConstraints.BOTH,
			    tableInsets,
			    0, 0);

	newCookieB = new JButton(msgs.getString("MON_New_cookie"));
        newCookieB.setMnemonic(msgs.getString("MON_New_cookie_Mnemonic").charAt(0));
        newCookieB.setToolTipText(msgs.getString("ACS_MON_New_cookieA11yDesc"));
	newCookieB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    String title = msgs.getString("MON_New_cookie"); 
		    ParamEditor pe = new ParamEditor("", "", //NOI18N
						     true, true, title);

		    if(debug) System.out.println("Now showing dialog");// NOI18N
		    
		    pe.showDialog(true);

		    if(debug) System.out.println("Dialog closed"); // NOI18N

		    if (pe.getDialogOK()) {

			if(debug) System.out.println("Dialog returned OK"); // NOI18N
			String name = pe.getName();
			String value = pe.getValue();
			System.out.println(name + " " + value);
			monitorData.getRequestData().addCookie(name,value);
			redisplayData();
		    }
		}});

	deleteCookieB = new JButton(msgs.getString("MON_Delete_cookie"));
        deleteCookieB.setMnemonic(msgs.getString("MON_Delete_cookie_Mnemonic").charAt(0));
        deleteCookieB.setToolTipText(msgs.getString("MON_New_cookie_Mnemonic"));

	deleteCookieB.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {

		    int numRows = cookieTable.getRowCount();
		    StringBuffer buf = new StringBuffer
			(msgs.getString("MON_Confirm_Delete_Cookies")); 
		    buf.append("\n"); // NOI18N

		    for(int i=0; i<numRows; ++i) {

			if(cookieTable.isRowSelected(i)) {
			    buf.append(cookieTable.getValueAt(i, 0));
			    buf.append(" ");  // NOI18N
			    buf.append(cookieTable.getValueAt(i, 1));
			    buf.append("\n"); // NOI18N
			}
		    }

		    showConfirmDialog(buf.toString()); 
		    if(setCookies) {
			
			for(int i=0; i<numRows; ++i) {
			    if(cookieTable.isRowSelected(i)) {
				String name =
				    (String)cookieTable.getValueAt(i, 0); 
				String value =
				    (String)cookieTable.getValueAt(i, 1); 
				monitorData.getRequestData().deleteCookie(name, value);
				redisplayData();
			    }
			}
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
	addGridBagComponent(this, newCookieB, ++gridx, gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.EAST,
			    java.awt.GridBagConstraints.NONE,
			    buttonInsets,
			    0, 0);

	addGridBagComponent(this, deleteCookieB, ++gridx, gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.EAST,
			    java.awt.GridBagConstraints.NONE,
			    buttonInsets,
			    0, 0);

	setEnablings();
	
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
	    setCookies = true;
	else 
	    setCookies = false;
    }


    public void showErrorDialog() {

	Object[] options = { NotifyDescriptor.OK_OPTION };
	
	NotifyDescriptor errorDialog = 
	    new NotifyDescriptor((Object)msgs.getString("MON_Bad_cookie"),
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
	newCookieB.setEnabled(true);

	int selectedRows[] = cookieTable.getSelectedRows();
	//
	// The edit button is enabled if exactly one row is selected
	//
	//editCookieB.setEnabled(selectedRows.length == 1);

	//
	// The delete row button is enabled if any rows are selected.
	//
	deleteCookieB.setEnabled(selectedRows.length > 0);
    }

    boolean tableModelChanging;

    public void setCookies(Param[] params) {

	cookieTable = new DisplayTable(params, DisplayTable.COOKIES);
	cookieTable.sortByName(true);
	
	ListSelectionModel selma = cookieTable.getSelectionModel();
	selma.addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent evt) {
		if(debug) System.out.println("EditPanelQuery::paramTable list selection listener"); // NOI18N
		setEnablings();
	    }
	});

	cookieTable.addTableModelListener(new TableModelListener() {
	    public void tableChanged(TableModelEvent evt) {
		if (!tableModelChanging) {
		    tableModelChanging = true;
		    //
		    // Loop through the rows and reset the params.
		    //
		    int num = cookieTable.getRowCount();
		    Param[] params = monitorData.getRequestData().getCookiesAsParams(); 
		    
		    boolean inputOK = true;
		    
		    for(int i=0; i < num; i++) {
			String name = (String)cookieTable.getValueAt(i, 0);
			name = name.trim();
			if(name.equals("")) { // NOI18N
			    cookieTable.setValueAt(params[i].getName(), i, 0);
			    inputOK = false;
			}
			String value = (String)cookieTable.getValueAt(i, 1);
			value = value.trim();
			if(value.equals("")) { // NOI18N
			    cookieTable.setValueAt(params[i].getValue(), i, 1);
			    inputOK = false;
			}
			
			if(!inputOK) {
			    showErrorDialog();
			    return;
			}
			params[i].setName(name);
			params[i].setValue(value);
		    }
		    cookieTable.sortByName();
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
} // EditPanelCookies


