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
 * RequestDisplay.java
 *
 *
 * Created: Wed Jan 31 18:04:22 2001
 *
 * @author Ana von Klopp Lemon
 * @version
 */

package org.netbeans.modules.web.monitor.client;

import javax.swing.*;     // widgets
import javax.swing.table.*;     // widgets
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Dimension;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.SystemColor;

import org.netbeans.modules.web.monitor.data.*;
import org.openide.util.NbBundle;
import java.util.*;


public class RequestDisplay extends JPanel {
    
    private final static boolean debug = false;
    private static final ResourceBundle msgs =
	NbBundle.getBundle(TransactionView.class);
    
    private static final String[] requestCategories = { 
	msgs.getString("MON_Request_URI"),
	msgs.getString("MON_Method"),
	msgs.getString("MON_Querystring"),
	msgs.getString("MON_Protocol"),
	msgs.getString("MON_Remote_Address"),
	msgs.getString("MON_Scheme"),
	msgs.getString("MON_Status"),
    };


    private DisplayTable dt = null; 
        
    public RequestDisplay() {

	super();
    }


    // We're treating these as if they are all strings at the
    // moment. In reality they can be of different types, though maybe 
    // that does not matter...
    public void setData(MonitorData md) {

	if(debug) System.out.println("in RequestDisplay.setData()");
	this.removeAll();
	if (md == null)
	    return;
	
	RequestData rd = md.getRequestData();
	dt = new DisplayTable(requestCategories);
	dt.setValueAt(rd.getAttributeValue("uri"), 0,1); 
	dt.setValueAt(rd.getAttributeValue("method"),1,1);
	dt.setValueAt(rd.getAttributeValue("queryString"), 2,1);
	dt.setValueAt(rd.getAttributeValue("protocol"), 3,1);
	dt.setValueAt(rd.getAttributeValue("ipaddress"), 4,1);
	dt.setValueAt(rd.getAttributeValue("scheme"), 5,1);
	dt.setValueAt(rd.getAttributeValue("status"), 6,1);

	this.setLayout(new GridBagLayout());

	int gridy = -1;
	Insets labelInsets = TransactionView.labelInsets;
	Insets tableInsets = TransactionView.tableInsets;
	Insets buttonInsets = TransactionView.buttonInsets;
	double tableWeightX = 1.0;
	double tableWeightY = 0;
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
			    labelInsets,
			    0, 0);

	addGridBagComponent(this, dt, 0, ++gridy,
			    fullGridWidth, 1, tableWeightX, tableWeightY, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    tableInsets,
			    0, 0);


	String msg;
	
	// add the parameters

	Param[] params2 = rd.getParam();
	String msg2 = "";
	DisplayTable paramTable = null;
	Component queryDataLabel = null;
	boolean bad = false;
	
	if(params2 == null || params2.length == 0) {
	    if("POST".equals(rd.getAttributeValue("method"))) {

		String type = rd.getAttributeValue("urlencoded");
		
		if(type != null) {

		    if (type.equals("false")) {
			msg2 = msgs.getString("MON_Unparameterized");
		    }
		    else if (type.equals("bad")) {
			msg2 = msgs.getString("MON_Warning_param"); 
			queryDataLabel =
			    TransactionView.createHeaderLabel(msg2); 
			bad = true;
		    }
		    else msg2 = msgs.getString("MON_No_posted_data");
		}
		else msg2 = msgs.getString("MON_No_posted_data");
	    } else {
		msg2 = msgs.getString("MON_No_querystring");
	    }
	    if(queryDataLabel == null) 
		queryDataLabel = TransactionView.createDataLabel(msg2);
	    
	} else {
	    msg2 = msgs.getString("MON_Parameters");
	    paramTable = new DisplayTable(params2);
	    queryDataLabel = TransactionView.createSortButtonLabel(msg2, paramTable);
	}
	
	addGridBagComponent(this, queryDataLabel, 0, ++gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);

	if (params2 != null && params2.length > 0) {
	    addGridBagComponent(this, paramTable, 0, ++gridy,
				fullGridWidth, 1, tableWeightX, tableWeightY, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.BOTH,
				tableInsets,
				0, 0);
	}
	else if(bad) {
	    JTextArea ta = new JTextArea(msgs.getString("MON_Unparameterized_bad"));
	    ta.setEditable(false);
	    ta.setLineWrap(true);
	    ta.setBackground(this.getBackground());
	    addGridBagComponent(this, ta, 0, ++gridy,
				fullGridWidth, 1, tableWeightX, tableWeightY, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.BOTH,
				tableInsets,
				0, 0);
			
	}
	
	addGridBagComponent(this, Box.createGlue(), 0, ++gridy,
			    1, 1, 1.0, 1.0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    TransactionView.zeroInsets,
			    0, 0);

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


} // RequestDisplay
