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
 * ClientDisplay.java
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
import java.awt.FlowLayout;
import org.netbeans.modules.web.monitor.data.*;
import org.openide.util.NbBundle;
import java.util.*;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;


public class ClientDisplay extends JPanel {
    
    private final static boolean debug = false;
    private static final ResourceBundle msgs =
	NbBundle.getBundle(TransactionView.class);
    

    private String[] categories = { 
	    msgs.getString("MON_Protocol"), 
	    msgs.getString("MON_Remote_Address"), 
	    msgs.getString("MON_Software"), 
	    msgs.getString("MON_Locale"), 
	    msgs.getString("MON_Encodings"), 
	    msgs.getString("MON_Fileformats"), 
	    msgs.getString("MON_Charsets")
	};

    private DisplayTable dt = null; 
        
    public ClientDisplay() {

	super();
    }

    private void createPanelWidgets() {
	this.setLayout(new GridBagLayout());

	int gridy = -1;
	double tableWeightX = 1.0;
	double tableWeightY = 0;
	int fullGridWidth = java.awt.GridBagConstraints.REMAINDER;

	addGridBagComponent(this, TransactionView.createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    TransactionView.topSpacerInsets,
			    0, 0);

	addGridBagComponent(this, TransactionView.createHeaderLabel(msgs.getString("MON_Client_3")), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    TransactionView.labelInsets,
			    0, 0);

	dt = new DisplayTable(categories);
	addGridBagComponent(this, dt, 0, ++gridy,
			    fullGridWidth, 1, tableWeightX, tableWeightY, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    TransactionView.tableInsets,
			    0, 0);

	addGridBagComponent(this, Box.createGlue(), 0, ++gridy,
			    1, 1, 1.0, 1.0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    TransactionView.zeroInsets,
			    0, 0);
    }

    // We're treating these as if they are all strings at the
    // moment. In reality they can be of different types, though maybe 
    // that does not matter...
    public void setData(MonitorData md) {

	if(debug) System.out.println("in ClientDisplay.setData()");
	this.removeAll();
	if (md == null)
	    return;

	createPanelWidgets();

	ClientData cd = md.getClientData();
	dt.setValueAt(cd.getAttributeValue("protocol"), 0,1); 
	dt.setValueAt(cd.getAttributeValue("remoteAddress"),1,1);
	dt.setValueAt(cd.getAttributeValue("software"), 2,1);
	dt.setValueAt(cd.getAttributeValue("locale"), 3,1);
	dt.setValueAt(cd.getAttributeValue("encodingsAccepted"), 4,1);
	dt.setValueAt(cd.getAttributeValue("formatsAccepted"), 5,1);
	dt.setValueAt(cd.getAttributeValue("charsetsAccepted"), 6,1);
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

} // ClientDisplay
