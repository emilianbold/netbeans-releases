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
 * SessionDisplay.java
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


public class SessionDisplay extends JPanel {
    
    private final static boolean debug = false;
    private static final ResourceBundle msgs =
	NbBundle.getBundle(TransactionView.class);
    
    private final static String[] props = { 
		msgs.getString("MON_Session_ID"),
		msgs.getString("MON_Created"),
		msgs.getString("MON_Last_accessed"),
		msgs.getString("MON_Max_inactive"),
	    };

    private final static String[] props2 = { 
		msgs.getString("MON_Session_ID"),
		msgs.getString("MON_Created"),
		msgs.getString("MON_Last_accessed"),
		msgs.getString("MON_Max_inactive_before"),
		msgs.getString("MON_Max_inactive_after"),
	    };


    public SessionDisplay() {
	super();
    }

    // We're treating these as if they are all strings at the
    // moment. In reality they can be of different types, though maybe 
    // that does not matter...
    public void setData(MonitorData md) {

	if(debug) System.out.println("in SessionDisplay.setData()");
	this.removeAll();
	if (md == null)
	    return;
	 
	SessionData sd = md.getSessionData();
	
	String headerIn;
	if(sd == null ||
	   ("false".equals(sd.getAttributeValue("before")) &&
	    "false".equals(sd.getAttributeValue("after")))) {
	    
	    this.setLayout(new FlowLayout(FlowLayout.LEFT));
	    this.add(TransactionView.createDataLabel(msgs.getString("MON_No_session")));
	    return;
	}
	
	String lastAccessed = null;
	String maxInactiveBefore = null;
	String maxInactiveAfter = null;

	this.setLayout(new GridBagLayout());

	int gridy = -1;
	Insets labelInsets = TransactionView.labelInsets;
	Insets tableInsets = TransactionView.tableInsets;
	Insets buttonInsets = TransactionView.buttonInsets;
	double tableWeightX = 1.0;
	double tableWeightY = 0;
	int fullGridWidth = java.awt.GridBagConstraints.REMAINDER;

	// PENDING - adjust the font... 

	addGridBagComponent(this, TransactionView.createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    TransactionView.topSpacerInsets,
			    0, 0);

	addGridBagComponent(this, TransactionView.createHeaderLabel(msgs.getString("MON_Session_24")), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);
	
	String msg;
	if("true".equals(sd.getAttributeValue("before"))) {
	    msg = msgs.getString("MON_Session_existed");
	    lastAccessed = 
		sd.getSessionIn().getAttributeValue("lastAccessed");
	    maxInactiveBefore =
		sd.getSessionIn().getAttributeValue("inactiveInterval"); 
	}
	else {
	    msg = msgs.getString("MON_Session_created");
	    lastAccessed = 
		sd.getSessionOut().getAttributeValue("lastAccessed");
	}
	
	addGridBagComponent(this, TransactionView.createDataLabel(msg), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    TransactionView.indentInsets,
			    0, 0);


	if("false".equals(sd.getAttributeValue("after"))) {
	    msg = msgs.getString("MON_Session_invalidated");
	    addGridBagComponent(this, TransactionView.createDataLabel(msg), 0, ++gridy,
				fullGridWidth, 1, 0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.NONE,
				TransactionView.indentInsets,
				0, 0);
	}
	else {
	    maxInactiveAfter =
		sd.getSessionOut().getAttributeValue("inactiveInterval"); 
	}
	

	boolean inactiveChanged = false;
	
	if(maxInactiveBefore == null || maxInactiveBefore.equals("")) {
	    if(maxInactiveAfter != null) 
		maxInactiveBefore = maxInactiveAfter;
	    // Should not happen
	    else maxInactiveBefore = "";
	}
	else if(maxInactiveAfter != null && 
		!maxInactiveBefore.equals(maxInactiveAfter)) 
	    inactiveChanged = true;
    
	// Add session properties header
	addGridBagComponent(this, TransactionView.createHeaderLabel(msgs.getString("MON_Session_properties")), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);

	DisplayTable dt = null;
       
	if(!inactiveChanged) {
	    String data[] = {
		sd.getAttributeValue("id"), 
		sd.getAttributeValue("created"),
		lastAccessed,
		maxInactiveBefore,
	    };
	    dt = new DisplayTable(props, data);
	}
	else {
	    String data[] = {
		sd.getAttributeValue("id"), 
		sd.getAttributeValue("created"),
		lastAccessed,
		maxInactiveBefore,
		maxInactiveAfter,
	    };
	    dt = new DisplayTable(props2, data);
	}


 	addGridBagComponent(this, dt, 0, ++gridy,
			    fullGridWidth, 1, tableWeightX, tableWeightY, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    tableInsets,
			    0, 0);

	// Attributes before and after 
	if("true".equals(sd.getAttributeValue("before"))) {
	    Param[] param = null;
	    try {
		param = sd.getSessionIn().getParam();
	    }
	    catch(Exception ex) {
	    }
	    
	    if(param == null || param.length == 0) {
		addGridBagComponent(this, TransactionView.createDataLabel(msgs.getString("MON_Session_no_att_before")), 0, ++gridy,
				    fullGridWidth, 1, 0, 0, 
				    java.awt.GridBagConstraints.WEST,
				    java.awt.GridBagConstraints.NONE,
				    tableInsets,
				    0, 0);
	    } else {
		addGridBagComponent(this, TransactionView.createHeaderLabel(msgs.getString("MON_Session_att_before")), 0, ++gridy,
				    fullGridWidth, 1, 0, 0, 
				    java.awt.GridBagConstraints.WEST,
				    java.awt.GridBagConstraints.NONE,
				    labelInsets,
				    0, 0);
		dt = new DisplayTable(param);
		addGridBagComponent(this, dt, 0, ++gridy,
				    fullGridWidth, 1, tableWeightX, tableWeightY, 
				    java.awt.GridBagConstraints.WEST,
				    java.awt.GridBagConstraints.BOTH,
				    tableInsets,
				    0, 0);
	    }
	}
	

	if("true".equals(sd.getAttributeValue("after"))) {
	    Param[] param = null;
	    try {
		param = sd.getSessionOut().getParam();
	    }
	    catch(Exception ex) {
	    }

	    if(param == null || param.length == 0) {
		addGridBagComponent(this, TransactionView.createDataLabel(msgs.getString("MON_Session_no_att_after")), 0, ++gridy,
				    fullGridWidth, 1, 0, 0, 
				    java.awt.GridBagConstraints.WEST,
				    java.awt.GridBagConstraints.NONE,
				    tableInsets,
				    0, 0);
	    } else {
		addGridBagComponent(this, TransactionView.createHeaderLabel(msgs.getString("MON_Session_att_after")), 0, ++gridy,
				    fullGridWidth, 1, 0, 0, 
				    java.awt.GridBagConstraints.WEST,
				    java.awt.GridBagConstraints.NONE,
				    labelInsets,
				    0, 0);
		dt = new DisplayTable(param);
		addGridBagComponent(this, dt, 0, ++gridy,
				    fullGridWidth, 1, tableWeightX, tableWeightY, 
				    java.awt.GridBagConstraints.WEST,
				    java.awt.GridBagConstraints.BOTH,
				    tableInsets,
				    0, 0);
	    }
	    this.add(Box.createRigidArea(new Dimension(0,5)));
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


} // SessionDisplay
