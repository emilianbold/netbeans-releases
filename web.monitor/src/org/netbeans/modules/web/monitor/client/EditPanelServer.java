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
 * EditPanelServer.java
 *
 *
 * Created: Mon Feb  5 13:34:46 2001
 *
 * @author Ana von Klopp
 * @author Simran Gleason
 * @version
 */

/**
 * Contains the Server sub-panel for the EditPanel
 */


package org.netbeans.modules.web.monitor.client; 

import java.awt.event.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.util.ResourceBundle;
import java.awt.event.*;
import java.net.URL;
import java.net.MalformedURLException;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.util.NbBundle;

import org.netbeans.modules.web.monitor.data.*;

public class EditPanelServer extends DataDisplay {

    private final static boolean debug = false;
    private static final ResourceBundle msgs =
	NbBundle.getBundle(TransactionView.class);
    
    private static final String[] servercats = { 
	msgs.getString("MON_Server_name"),
	msgs.getString("MON_Server_port"),
    };

    private boolean holdTableChanges = false;
    private DisplayTable serverTable = null; 

    private MonitorData monitorData = null;
    
    private EditPanel editPanel;
    
    public EditPanelServer(MonitorData md, EditPanel editPanel) {
	super();
	this.editPanel = editPanel;
	this.monitorData = md;
    }
    
    // We're treating these as if they are all strings at the
    // moment. In reality they can be of different types, though maybe 
    // that does not matter...
    public void setData(MonitorData md) {

	this.monitorData = md;
	setServerTable(servercats);
	
	if(debug) System.out.println("in EditPanelServer.setData()"); //NOI18N 

	holdTableChanges = true;	 
	EngineData ed  = monitorData.getEngineData();
	if(ed != null) {
	     
	    serverTable.setValueAt(ed.getAttributeValue("serverName"), 0, 1); //NOI18N 
	    serverTable.setValueAt(ed.getAttributeValue("serverPort"), 1, 1); //NOI18N 
	}
	// for backwards compatibility
	else {
	    ServletData sd = monitorData.getServletData();
	    serverTable.setValueAt(sd.getAttributeValue("serverName"), 0, 1); //NOI18N 
	    serverTable.setValueAt(sd.getAttributeValue("serverPort"), 1, 1); //NOI18N 
	}
	
	holdTableChanges = false;
	this.removeAll();

	// use this for the server! 

	int gridy = -1;

	addGridBagComponent(this, createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    topSpacerInsets,
			    0, 0);

        serverTable.getAccessibleContext().setAccessibleName(msgs.getString("ACS_MON_Exec_serverTableA11yName"));
        serverTable.setToolTipText(msgs.getString("ACS_MON_Exec_serverTableA11yDesc"));
	addGridBagComponent(this, createHeaderLabel(msgs.getString("MON_Exec_server"), msgs.getString("MON_Exec_server_Mnemonic").charAt(0), msgs.getString("ACS_MON_Exec_serverA11yDesc"), serverTable),
                            0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);

	addGridBagComponent(this, serverTable, 0, ++gridy,
			    fullGridWidth, 1, 1.0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.HORIZONTAL,
			    tableInsets,
			    0, 0);
	
	addGridBagComponent(this, createGlue(), 0, ++gridy,
			    1, 1, 1.0, 1.0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    zeroInsets,
			    0, 0);

	// Housekeeping
	this.setMaximumSize(this.getPreferredSize()); 
	this.repaint();
    }

    public void setServerTable(String []servercats) {
   	serverTable = new DisplayTable(servercats, DisplayTable.SERVER);
	
	serverTable.addTableModelListener(new TableModelListener() {
	    public void tableChanged(TableModelEvent evt) {

		if (holdTableChanges) return;
		 
		boolean inputOK = true;
		
		String server = (String)serverTable.getValueAt(0, 1);
		server = server.trim();
		String portStr = (String)serverTable.getValueAt(1, 1);
		portStr = portStr.trim();
		
 
		if(server.equals("")) inputOK = false; //NOI18N 
		if(portStr.equals("")) portStr = "80"; //NOI18N 

		int port = 0;
		if(inputOK) {
		    try {
			port = Integer.parseInt(portStr);
		    }
		    catch(NumberFormatException nfe) {
			inputOK = false;
		    }
		}
		
		if(inputOK) {
		    try {
			URL url = new URL("http", server, port, ""); //NOI18N
		   }
		    catch(MalformedURLException mue) {
			inputOK = false;
		    }
		}

		if(inputOK) {
		    ServletData sd = monitorData.getServletData();
		    sd.setAttributeValue("serverName", server); //NOI18N
		    sd.setAttributeValue("serverPort", portStr); //NOI18N
		}
		else {
		    showErrorDialog();
		    setData(monitorData);
		}
	    }});
    }

    public void repaint() {
	super.repaint();
	if (editPanel != null) 
	    editPanel.repaint();
    }

    public void showErrorDialog() {

	Object[] options = { NotifyDescriptor.OK_OPTION };
	
	NotifyDescriptor errorDialog = 
	    new NotifyDescriptor((Object)msgs.getString("MON_Bad_server"),
				 msgs.getString("MON_Invalid_input"),
				 NotifyDescriptor.DEFAULT_OPTION,
				 NotifyDescriptor.ERROR_MESSAGE, 
				 options,
				 NotifyDescriptor.OK_OPTION);

	TopManager.getDefault().notify(errorDialog);
    }
} // EditPanelServer
