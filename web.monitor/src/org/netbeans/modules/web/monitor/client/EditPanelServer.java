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
 * @author Ana von Klopp Lemon
 * @author Simran Gleason
 * @version
 */

/**
 * Contains the Server sub-panel for the EditPanel
 */


package org.netbeans.modules.web.monitor.client; 

import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.util.*;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.event.*;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import java.net.*;

import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.util.NbBundle;

import org.netbeans.modules.web.monitor.data.*;

public class EditPanelServer extends javax.swing.JPanel {

    private final static boolean debug = false;
    private static final ResourceBundle msgs =
	NbBundle.getBundle(TransactionView.class);
    
    private static final Dimension size = new Dimension(500, 550);
    private static final Dimension reqSize = new Dimension(450, 100);
    private static final Dimension tableSize = new Dimension(450, 100);
   
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
	
	if(debug) System.out.println("in EditPanelServer.setData()");
	 
	ServletData sd = monitorData.getServletData();
	holdTableChanges = true;
	serverTable.setValueAt(sd.getAttributeValue("serverName"), 0, 1); 
	serverTable.setValueAt(sd.getAttributeValue("serverPort"), 1, 1);
	holdTableChanges = false;

	this.removeAll();
	this.setLayout(new GridBagLayout());
	//this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	
	// use this for the server! 

	int gridy = -1;
	int fullGridWidth = java.awt.GridBagConstraints.REMAINDER;

	addGridBagComponent(this, TransactionView.createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    TransactionView.topSpacerInsets,
			    0, 0);

	addGridBagComponent(this, TransactionView.createHeaderLabel(msgs.getString("MON_Exec_server")), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    TransactionView.labelInsets,
			    0, 0);

	addGridBagComponent(this, serverTable, 0, ++gridy,
			    fullGridWidth, 1, 1.0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.HORIZONTAL,
			    TransactionView.tableInsets,
			    0, 0);
	
	addGridBagComponent(this, Box.createGlue(), 0, ++gridy,
			    1, 1, 1.0, 1.0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    TransactionView.zeroInsets,
			    0, 0);

	// Housekeeping
	this.setMaximumSize(this.getPreferredSize()); 
	this.repaint();
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
		
 
		if(server.equals("")) inputOK = false;
		if(portStr.equals("")) portStr = "80";

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
			URL url = new URL("http", server, port, "");
		    }
		    catch(MalformedURLException mue) {
			inputOK = false;
		    }
		}

		if(inputOK) {
		    ServletData sd = monitorData.getServletData();
		    sd.setAttributeValue("serverName", server);
		    sd.setAttributeValue("serverPort", portStr); 
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
