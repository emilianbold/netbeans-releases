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
 * CookieDisplay.java
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
import javax.swing.border.*;     // widgets

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Dimension;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import org.netbeans.modules.web.monitor.data.*;
import org.openide.util.NbBundle;
import java.util.*;

// PENDING: can be more helpful with what the cookie data means. Like
// I had the expires at the end of this session before, that was kind
// of useful. Could also show the actual date that the cookie
// expires. 

public class CookieDisplay extends JPanel {
    
    private final static boolean debug = false;
    private static final ResourceBundle msgs =
	NbBundle.getBundle(TransactionView.class);
    
    private static final String[] categoriesIn = { 
	msgs.getString("MON_Name"),
	msgs.getString("MON_Value"),
    };


    private static final String[] categoriesOut = { 
	msgs.getString("MON_Name"),
	msgs.getString("MON_Value"),
	msgs.getString("MON_Domain"),
	msgs.getString("MON_Path"),
	msgs.getString("MON_Max_age"),
	msgs.getString("MON_Version"),
	msgs.getString("MON_Secure"),
	msgs.getString("MON_Comment"),
    };


    private DisplayTable dt = null; 
    private JPanel top = null;
        
    public CookieDisplay() {
	super();
    }

    // We're treating these as if they are all strings at the
    // moment. In reality they can be of different types, though maybe 
    // that does not matter...
    public void setData(MonitorData md) {

	if(debug) System.out.println("in CookieDisplay.setData()");
	this.removeAll();
	if (md == null)
	    return;
	 
	CookiesData cd = md.getCookiesData();
	CookieIn[] in = cd.getCookieIn();
	CookieOut[] out = cd.getCookieOut();

	this.setLayout(new GridBagLayout());
	//this.setBorder(BorderFactory.createLineBorder(Color.red)); // debugging

	int gridy = -1;
	Insets labelInsets = TransactionView.labelInsets;
	Insets tableInsets = TransactionView.tableInsets;
	Insets buttonInsets = TransactionView.buttonInsets;
	double tableWeightX = 1.0;
	double tableWeightY = 0;
	int fullGridWidth = java.awt.GridBagConstraints.REMAINDER;

	String headerIn;
	JLabel incomingLabel;
	if(in == null || in.length == 0) {
	    headerIn = msgs.getString("MON_No_incoming");
	    incomingLabel = TransactionView.createDataLabel(headerIn);

	} else {
	    headerIn = msgs.getString("MON_Incoming_cookie");
	    incomingLabel = TransactionView.createHeaderLabel(headerIn);
	}

	addGridBagComponent(this, TransactionView.createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    TransactionView.topSpacerInsets,
			    0, 0);

	addGridBagComponent(this, incomingLabel, 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.HORIZONTAL,
			    labelInsets,
			    0, 0);
	

	if(in != null && in.length > 0) {

	    for(int i=0; i<in.length; ++i) {
		String[] data = {
		    in[i].getAttributeValue("name"), 
		    in[i].getAttributeValue("value")
		};
		DisplayTable dt = new DisplayTable(categoriesIn, data);
		addGridBagComponent(this, dt, 0, ++gridy,
			    fullGridWidth, 1, tableWeightX, tableWeightY, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    tableInsets,
			    0, 0);
	    }
	}

	String headerOut;
	JLabel outgoingLabel;
	if(out == null || out.length == 0) {
	    headerOut = msgs.getString("MON_No_outgoing");
	    outgoingLabel = TransactionView.createDataLabel(headerOut);
	} else {
	    headerOut = msgs.getString("MON_Outgoing_cookie");
	    outgoingLabel = TransactionView.createHeaderLabel(headerOut);
	}
	addGridBagComponent(this, outgoingLabel, 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);

	if(out != null && out.length > 0) {

	    for(int i=0; i<out.length; ++i) {
		String cookieMaxAge = out[i].getAttributeValue("maxAge");
		if(cookieMaxAge.equals("-1"))
		    cookieMaxAge = msgs.getString("MON_this_session");
		
		String[] data = {
		    out[i].getAttributeValue("name"), 
		    out[i].getAttributeValue("value"),
		    out[i].getAttributeValue("domain"),
		    out[i].getAttributeValue("path"),
		    cookieMaxAge,
		    out[i].getAttributeValue("version"),
		    out[i].getAttributeValue("secure"),
		    out[i].getAttributeValue("comment")
		};
		DisplayTable dt = new DisplayTable(categoriesOut, data);
		addGridBagComponent(this, dt, 0, ++gridy,
			    fullGridWidth, 1, tableWeightX, tableWeightY, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    tableInsets,
			    0, 0);
	    }
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

} // CookieDisplay
