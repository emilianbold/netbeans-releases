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
 * EditPanelQuery.java
 *
 *
 * Created: Mon Feb  5 13:34:46 2001
 *
 * @author Ana von Klopp
 * @author Simran Gleason
 * @version
 */

/**
 * Contains the Query sub-panel for the EditPanel
 */
package org.netbeans.modules.web.monitor.client;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

import org.netbeans.modules.web.monitor.data.*;

class EditPanelQuery extends DataDisplay {

    private final static boolean debug = false;

    private DisplayTable paramTable = null; 
    private MonitorData monitorData = null;
    private boolean setParams = false;

    // Widgets
    JButton    newParamB;
    JButton    deleteParamB;
    JTextField queryStringText;
    JTextField uploadFileText;
 
    EditPanelQuery() {
	super();
    }

    // Redesign this, inefficient and prevents us from maintaining
    // sorting state
    void redisplayData() {
	if(debug) log("::redisplayData()"); 
	setData(monitorData);
	this.revalidate(); 
	this.repaint(); 
    }
    
    void setData(MonitorData md) {

	if(debug) { 
	    log("setData()"); // NOI18N
	    log("\tMonitor data is:"); // NOI18N
	    log("t" + md.dumpBeanNode()); // NOI18N
	}
	this.monitorData = md;
	this.removeAll();

	int fullGridWidth = java.awt.GridBagConstraints.REMAINDER;
	int gridy = -1;
	 

	addGridBagComponent(this, createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    topSpacerInsets,
			    0, 0);
	
	// The query parameters: PENDING
	//
	// 1. Get request - add a parameter one at a time. We should not
	//    allow the user to edit the query string. 
	// 2. POST request. Allow the user to edit the query string, 
	//    and then there are two cases: 
        //    a) add a parameter one at a time
	//    b) add data from file
	// 3. PUT request. Allow the user to enter data from a file
	//    only. They can edit the query string as well. 
	
	final RequestData rd = monitorData.getRequestData();

	String method = rd.getAttributeValue(EditPanel.METHOD);
	if (EditPanel.POST.equals(method)) {
	    queryStringText =
		new JTextField(rd.getAttributeValue("queryString")); // NOI18N

	    queryStringText.addFocusListener(new FocusListener() {
		public void focusGained(FocusEvent evt) {
		}
		public void focusLost(FocusEvent evt) {
		    // PENDING - check that this works
		    rd.setAttributeValue("queryString", //NOI18N
					 queryStringText.getText());
		}
	    });

	    addGridBagComponent(this, createHeaderLabel(NbBundle.getBundle(EditPanelQuery.class).getString("MON_Querystring"), NbBundle.getBundle(EditPanelQuery.class).getString("MON_Querystring_Mnemonic").charAt(0), NbBundle.getBundle(EditPanelQuery.class).getString("ACS_MON_QuerystringA11yDesc"), paramTable),
                                0, ++gridy,
				1, 1, 0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.NONE,
				labelInsets,
				0, 0);

	    addGridBagComponent(this, queryStringText, 0, ++gridy,
				fullGridWidth, 1, 1.0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.HORIZONTAL,
				tableInsets,
				0, 0);
	}
	    
	String msg2 = null;
	Component msg2Label;

	if (EditPanel.PUT.equals(method)) {
	    msg2 = NbBundle.getBundle(EditPanelQuery.class).getString("MON_Upload_File");
	    msg2Label = createDataLabel(msg2);
	    addGridBagComponent(this, msg2Label, 0, ++gridy,
				fullGridWidth, 1, 0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.NONE,
				labelInsets,
				0, 0);
	    

	    String uploadFileMsg = NbBundle.getBundle(EditPanelQuery.class).getString("MON_Upload_File_Not_Supported");
	    uploadFileText = new JTextField(uploadFileMsg);
	    uploadFileText.setEnabled(false);
	    addGridBagComponent(this, uploadFileText, 0, ++gridy,
				fullGridWidth, 1, 1.0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.HORIZONTAL,
				labelInsets,
				0, 0);
	    addGridBagComponent(this, createGlue(), 0, ++gridy,
				1, 1, 1.0, 1.0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.BOTH,
				labelInsets,
				0, 0);

	} else if (method != null) {  // GET or POST

	    Param[] params2 = rd.getParam();
	    if (params2 == null) params2 = new Param[0];
	    setParameters(params2);
            char mnemonic = ' ';
            String ad = null;

	    if(method.equals(EditPanel.GET)) {
		msg2 = NbBundle.getBundle(EditPanelQuery.class).getString("MON_Query_parameters");
                mnemonic = NbBundle.getBundle(EditPanelQuery.class).getString("MON_Query_parameters_Mnemonic").charAt(0);
                ad = NbBundle.getBundle(EditPanelQuery.class).getString("ACS_MON_Query_parametersA11yDesc");
                paramTable.getAccessibleContext().setAccessibleName(NbBundle.getBundle(EditPanelQuery.class).getString("ACS_MON_ParametersTableA11yDesc"));
                paramTable.setToolTipText(NbBundle.getBundle(EditPanelQuery.class).getString("ACS_MON_ParametersTableA11yDesc"));
		
	    } else if(method.equals(EditPanel.POST)) {
		msg2 = NbBundle.getBundle(EditPanelQuery.class).getString("MON_Posted_data");
                mnemonic = NbBundle.getBundle(EditPanelQuery.class).getString("MON_Posted_data_Mnemonic").charAt(0);
                ad = NbBundle.getBundle(EditPanelQuery.class).getString("ACS_MON_Posted_dataA11yDesc");
                paramTable.getAccessibleContext().setAccessibleName(NbBundle.getBundle(EditPanelQuery.class).getString("ACS_MON_Posted_dataTableA11yName"));
                paramTable.setToolTipText(NbBundle.getBundle(EditPanelQuery.class).getString("ACS_MON_Posted_dataTableA11yDesc"));
	    }

	    msg2Label = createSortButtonLabel(msg2, paramTable, mnemonic, ad);

	    addGridBagComponent(this, msg2Label, 0, ++gridy,
				1, 1, 0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.NONE,
				labelInsets,
				0, 0);

	    gridy = addParamTable(this, params2, gridy);
	}
	
	setEnablings();

	// Housekeeping
	this.setMaximumSize(this.getPreferredSize()); 
	this.repaint();

    }

    private int addParamTable(JPanel panel, Param[] params, int gridy) {
	
	JScrollPane scrollpane = new JScrollPane(paramTable);

	addGridBagComponent(panel, scrollpane, 0, ++gridy,
			    fullGridWidth, 1, 1.0, 1.0, 
			    java.awt.GridBagConstraints.WEST,
			    //java.awt.GridBagConstraints.HORIZONTAL, 
			    java.awt.GridBagConstraints.BOTH,
			    tableInsets,
			    0, 0);

	newParamB = new JButton(NbBundle.getBundle(EditPanelQuery.class).getString("MON_New_param"));
        newParamB.setMnemonic(NbBundle.getBundle(EditPanelQuery.class).getString("MON_New_param_Mnemonic").charAt(0));
        newParamB.setToolTipText(NbBundle.getBundle(EditPanelQuery.class).getString("ACS_MON_New_paramA11yDesc"));
	newParamB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {

		    String title = NbBundle.getBundle(EditPanelQuery.class).getString("MON_New_param"); 
		    ParamEditor pe = new ParamEditor("", "", //NOI18N
						     ParamEditor.Editable.BOTH, 
						     ParamEditor.Condition.NONE, 
						     title);

		    if(debug) log("Now showing dialog"); // NOI18N
		    
		    pe.showDialog();

		    if(debug) log("Dialog closed"); // NOI18N

		    if (pe.getDialogOK()) {

			if(debug) log("Dialog returned OK"); // NOI18N
			String name = pe.getName();
			String value = pe.getValue();
			Param newParam = new Param(name, value);
			monitorData.getRequestData().addParam(newParam);
			redisplayData();
		    }
		}});

	deleteParamB = new JButton(NbBundle.getBundle(EditPanelQuery.class).getString("MON_Delete_param"));
        deleteParamB.setMnemonic(NbBundle.getBundle(EditPanelQuery.class).getString("MON_Delete_param_Mnemonic").charAt(0));
        deleteParamB.setToolTipText(NbBundle.getBundle(EditPanelQuery.class).getString("ACS_MON_Delete_paramA11yDesc"));

	deleteParamB.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {
		
		    int numRows = paramTable.getRowCount();
		    RequestData rd = monitorData.getRequestData();
		
		    StringBuffer buf = new StringBuffer
			(NbBundle.getBundle(EditPanelQuery.class).getString("MON_Confirm_Delete_Params")); 
		    buf.append("\n"); //NOI18N
		
		    for(int i=0; i<numRows; ++i) {
		    
			if(paramTable.isRowSelected(i)) {
			    buf.append(paramTable.getValueAt(i, 0));
			    buf.append("="); //NOI18N
			    buf.append(paramTable.getValueAt(i, 1));
			    buf.append("\n"); //NOI18N
			}
		    }
		
		    showConfirmDialog(buf.toString()); 
		
		    if(setParams) {

			for(int i=0; i<numRows; ++i) {
			
			    if(paramTable.isRowSelected(i)) {
				String name = 
				    (String)paramTable.getValueAt(i, 0);
				String value = 
				    (String)paramTable.getValueAt(i, 1);

				// Note that we get the params each
				// time through so that we don't run
				// into null pointer exceptions. 
				Param[] params2 = rd.getParam();
				Param param = findParam(params2, name, value);
				if (param != null) 
				    rd.removeParam(param);
			    }
			}
			redisplayData();
		    }
		}});
	int gridx = -1;
	addGridBagComponent(this, createGlue(), ++gridx, ++gridy,
			    1, 1, 1.0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    buttonInsets,
			    0, 0);
	addGridBagComponent(this, newParamB, ++gridx, gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.EAST,
			    java.awt.GridBagConstraints.NONE,
			    buttonInsets,
			    0, 0);

	addGridBagComponent(this, deleteParamB, ++gridx, gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.EAST,
			    java.awt.GridBagConstraints.NONE,
			    buttonInsets,
			    0, 0);
	return gridy;
    }

    void setEnablings() {
	//
	// Always enable the Add button.
	//
	newParamB.setEnabled(true);

	// The delete row button is enabled if any rows are selected.
	int selectedRows[] = paramTable.getSelectedRows();
	deleteParamB.setEnabled(selectedRows.length > 0);
    }

    void setParameters(Param[] newParams) {

	paramTable = new DisplayTable(newParams, DisplayTable.PARAMS, true);
        paramTable.getAccessibleContext().setAccessibleName(NbBundle.getBundle(EditPanelQuery.class).getString("ACS_MON_QuerystringTableA11yName"));
        paramTable.setToolTipText(NbBundle.getBundle(EditPanelQuery.class).getString("ACS_MON_QuerystringTableA11yDesc"));

	ListSelectionModel selma = paramTable.getSelectionModel();
	selma.addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent evt) {
		if(debug) log("paramTable list selection listener"); // NOI18N
		setEnablings();
	    }
	});

	paramTable.addTableModelListener(new TableModelListener() {
	    public void tableChanged(TableModelEvent evt) {
		updateParams();
	    }
	    });
    }
    

    void updateParams() {
	int num = paramTable.getRowCount();
	RequestData rd = monitorData.getRequestData();
	Param[] params = rd.getParam();
		    
	for(int i=0; i < num; i++) {
	    String name = (String)paramTable.getValueAt(i, 0);
	    name = name.trim();
	    if(name.equals("")) { //NOI18N
		paramTable.setValueAt(params[i].getName(), i, 0);
		showErrorDialog();
		return;
	    }
	    String value = (String)paramTable.getValueAt(i, 1);
	    value = value.trim();
	    params[i].setName(name);
	    params[i].setValue(value);
	}
    }
    
    void showConfirmDialog(String msg) {

	Object[] options = { NotifyDescriptor.OK_OPTION, 
			   NotifyDescriptor.CANCEL_OPTION 
	};
	
	NotifyDescriptor confirmDialog = 
	    new NotifyDescriptor((Object)msg, 
				 NbBundle.getBundle(EditPanelQuery.class).getString("MON_Confirmation_Required"),
				 NotifyDescriptor.OK_CANCEL_OPTION,
				 NotifyDescriptor.QUESTION_MESSAGE, 
				 options,
				 NotifyDescriptor.CANCEL_OPTION);

	DialogDisplayer.getDefault().notify(confirmDialog);
	if(confirmDialog.getValue().equals(NotifyDescriptor.OK_OPTION)) 
	    setParams = true;
	else 
	    setParams = false;
    }

    void showErrorDialog() {

	Object[] options = { NotifyDescriptor.OK_OPTION };
	
	NotifyDescriptor errorDialog = 
	    new NotifyDescriptor((Object)NbBundle.getBundle(EditPanelQuery.class).getString("MON_Bad_param"),
				 NbBundle.getBundle(EditPanelQuery.class).getString("MON_Invalid_input"),
				 NotifyDescriptor.DEFAULT_OPTION,
				 NotifyDescriptor.ERROR_MESSAGE, 
				 options,
				 NotifyDescriptor.OK_OPTION);

	DialogDisplayer.getDefault().notify(errorDialog);
    }


    void log(String s) {
	System.out.println("EditPanelQuery::" + s); //NOI18N
    }

} // EditPanel
