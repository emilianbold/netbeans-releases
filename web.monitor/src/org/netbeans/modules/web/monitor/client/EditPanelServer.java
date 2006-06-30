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
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

import org.netbeans.modules.web.monitor.data.*;

class EditPanelServer extends DataDisplay {

    private final static boolean debug = false;
    
    private boolean holdTableChanges = false;
    private DisplayTable serverTable = null; 

    private MonitorData monitorData = null;
    
    EditPanelServer() { 
	super();
    }
    
    void setData(MonitorData md) {

	this.monitorData = md;
	setServerTable();
	this.removeAll();

	int gridy = -1;

	addGridBagComponent(this, createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    topSpacerInsets,
			    0, 0);

	addGridBagComponent(this, createHeaderLabel(NbBundle.getBundle(EditPanelServer.class).getString("MON_Exec_server"), NbBundle.getBundle(EditPanelServer.class).getString("MON_Exec_server_Mnemonic").charAt(0), NbBundle.getBundle(EditPanelServer.class).getString("ACS_MON_Exec_serverA11yDesc"), serverTable),
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

	int gridx = -1;
	addGridBagComponent(this, createGlue(), ++gridx, ++gridy,
			    1, 1, 1.0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    buttonInsets,
			    0, 0);


	// Housekeeping
	this.setMaximumSize(this.getPreferredSize()); 
	this.repaint();
    }

    void setServerTable() {

	String[] servercats = { 
	    NbBundle.getBundle(EditPanelServer.class).getString("MON_Server_name"),
	    NbBundle.getBundle(EditPanelServer.class).getString("MON_Server_port"),
	};
   	serverTable = new DisplayTable(servercats, DisplayTable.SERVER);

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
	
        serverTable.getAccessibleContext().setAccessibleName(NbBundle.getBundle(EditPanelServer.class).getString("ACS_MON_Exec_serverTableA11yName"));
        serverTable.setToolTipText(NbBundle.getBundle(EditPanelServer.class).getString("ACS_MON_Exec_serverTableA11yDesc"));

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
		    monitorData.setServerName(server); //NOI18N
		    monitorData.setServerPort(portStr); //NOI18N
		}
		else {
		    showErrorDialog();
		    setData(monitorData);
		}
	    }});
    }

    public void repaint() {
	super.repaint();
	//if (editPanel != null) 
	//editPanel.repaint();
    }

    void showErrorDialog() {

	Object[] options = { NotifyDescriptor.OK_OPTION };
	
	NotifyDescriptor errorDialog = 
	    new NotifyDescriptor((Object)NbBundle.getBundle(EditPanelServer.class).getString("MON_Bad_server"),
				 NbBundle.getBundle(EditPanelServer.class).getString("MON_Invalid_input"),
				 NotifyDescriptor.DEFAULT_OPTION,
				 NotifyDescriptor.ERROR_MESSAGE, 
				 options,
				 NotifyDescriptor.OK_OPTION);

	DialogDisplayer.getDefault().notify(errorDialog);
    }

    void log(String s) {
	System.out.println("EditPanelServer::" + s); //NOI18N
    }

} // EditPanelServer
