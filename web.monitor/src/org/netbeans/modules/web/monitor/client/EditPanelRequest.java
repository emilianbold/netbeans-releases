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
 * EditPanelRequest.java
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


import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.*;

import java.net.*;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import java.util.*;

//import org.openide.NotifyDescriptor;
//import org.openide.TopManager;
import org.openide.util.NbBundle;

import org.netbeans.modules.web.monitor.data.*;

public class EditPanelRequest extends javax.swing.JPanel {

    private final static boolean debug = false;
    private static final ResourceBundle msgs =
	NbBundle.getBundle(TransactionView.class);
    
    private static final Dimension size = new Dimension(500, 550);
    private static final Dimension reqSize = new Dimension(450, 100);
    private static final Dimension tableSize = new Dimension(450, 100);
   

    private static final String[] requestCategories = { 
	msgs.getString("MON_Request_URI"),
	msgs.getString("MON_Method"),
	msgs.getString("MON_Protocol")
    };


    private DisplayTable requestTable = null; 
    private boolean holdTableChanges = false;

    private MonitorData monitorData = null;
    
    private EditPanel editPanel;
    
    public EditPanelRequest(MonitorData md, EditPanel editPanel) {
	super();
	this.editPanel = editPanel;
	this.monitorData = md;
    }
    
    // We're treating these as if they are all strings at the
    // moment. In reality they can be of different types, though maybe 
    // that does not matter...
    public void setData(MonitorData md) {

	this.monitorData = md;
	setRequestTable(requestCategories);
	
	if(debug) System.out.println("in EditPanelRequest.setData()");
	 
	RequestData rd = monitorData.getRequestData();
	holdTableChanges = true;
	requestTable.setValueAt(rd.getAttributeValue("uri"), 0,1); 
	requestTable.setValueAt(rd.getAttributeValue("method"),1,1);
	requestTable.setValueAt(rd.getAttributeValue("protocol"), 2,1);
	//requestTable.setValueAt(rd.getAttributeValue("ipaddress"), 3,1);
	holdTableChanges = false;

	this.removeAll();
	this.setLayout(new GridBagLayout());
	
	int gridy = -1;
	int fullGridWidth = java.awt.GridBagConstraints.REMAINDER;

	addGridBagComponent(this, TransactionView.createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    TransactionView.topSpacerInsets,
			    0, 0);

	addGridBagComponent(this, TransactionView.createHeaderLabel(msgs.getString("MON_Request_19")), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    TransactionView.labelInsets,
			    0, 0);

	addGridBagComponent(this, requestTable, 0, ++gridy,
			    fullGridWidth, 1, 1.0, 0, 
			    java.awt.GridBagConstraints.NORTHWEST,
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


    public void setRequestTable(String [] requestCategories) {
	
	requestTable = 
	    new DisplayTable(requestCategories, DisplayTable.REQUEST);
	String [] methodChoices = {"GET", "POST", "PUT"};
	requestTable.setChoices(1, 1, methodChoices, false);

	requestTable.addTableModelListener(new TableModelListener() {
		public void tableChanged(TableModelEvent evt) {

		    if (holdTableChanges) return;
		    RequestData rd = monitorData.getRequestData();

		    // The query panel depends on the value of the
		    // "method" attribute. 
		    String method = rd.getAttributeValue("method");
		    String newMethod = (String)requestTable.getValueAt(1, 1);
		    if (method != null && !method.equals(newMethod)) {
			rd.setAttributeValue("method",    newMethod);

			if(method.equals("GET") && newMethod.equals("POST")) {

			    // Set the query string to null if we got
			    // parameters from it, o/w leave it as is
			    try {
				String queryString =
				    rd.getAttributeValue("queryString"); 
				Hashtable ht =
				    javax.servlet.http.HttpUtils.parseQueryString(queryString); 
				rd.setAttributeValue("queryString", "");
			    }
			    catch(Exception ex) { }


			}
			else if(method.equals("POST") && 
				newMethod.equals("GET")) {
			    Util.addParametersToQuery(rd);
			}
			editPanel.resetQueryPanelData();
		    }

		    //
		    // Set the rest...
		    //
		    String uri =  (String)requestTable.getValueAt(0,1);
		    uri = uri.trim();

		    String protocol =  (String)requestTable.getValueAt(2,1);
		    protocol = protocol.trim();
		    rd.setAttributeValue("uri", uri);
		    rd.setAttributeValue("protocol", protocol); 
		}});
    }

    public void repaint() {
	super.repaint();
	if (editPanel != null) 
	    editPanel.repaint();
    }

} // EditPanelRequest
