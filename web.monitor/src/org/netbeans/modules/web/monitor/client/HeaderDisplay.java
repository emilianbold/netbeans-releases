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
 * HeaderDisplay.java
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

import org.netbeans.modules.web.monitor.data.*;
import org.openide.util.NbBundle;
import java.util.*;


public class HeaderDisplay extends JPanel {
    
    private final static boolean debug = false;
    private static final ResourceBundle msgs =
	NbBundle.getBundle(TransactionView.class);
    
    private DisplayTable dt = null; 
        
    public HeaderDisplay() {

	super();
    }


    // We're treating these as if they are all strings at the
    // moment. In reality they can be of different types, though maybe 
    // that does not matter...
    public void setData(MonitorData md) {

	if(debug) System.out.println("in HeaderDisplay.setData()");
	this.removeAll();
	if (md == null)
	    return;
	
	this.setLayout(new GridBagLayout());

	int gridy = -1;
	double tableWeightX = 1.0;
	double tableWeightY = 0;
	int fullGridWidth = java.awt.GridBagConstraints.REMAINDER;

	// add the headers 
	RequestData rd = md.getRequestData();
	Param[] params = rd.getHeaders().getParam();
	String msg;
	Component hLabel;
	DisplayTable headerTable = null;

	if(params == null || params.length == 0) {
	    msg = msgs.getString("MON_No_headers");
	    hLabel = TransactionView.createDataLabel(msg);
	} else {
	    msg = msgs.getString("MON_HTTP_Headers");
	    headerTable = new DisplayTable(params);
	    hLabel = TransactionView.createSortButtonLabel(msg, headerTable);
	}

	addGridBagComponent(this, TransactionView.createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    TransactionView.topSpacerInsets,
			    0, 0);

	addGridBagComponent(this, hLabel, 0, ++gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    TransactionView.labelInsets,
			    0, 0);


	if(params != null && params.length > 0) {
	    addGridBagComponent(this, headerTable, 0, ++gridy,
				fullGridWidth, 1, tableWeightX, tableWeightY, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.HORIZONTAL,
				TransactionView.tableInsets,
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


} // HeaderDisplay
