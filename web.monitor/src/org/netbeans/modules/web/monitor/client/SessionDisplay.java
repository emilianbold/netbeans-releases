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
 * @author Ana von Klopp
 * @version
 */

package org.netbeans.modules.web.monitor.client;

import javax.swing.JLabel;
import javax.swing.table.*;     // widgets
import org.netbeans.modules.web.monitor.data.*;
import org.openide.util.NbBundle;
import java.util.*;


public class SessionDisplay extends DataDisplay {
    
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

	if(debug) System.out.println("in SessionDisplay.setData()"); //NOI18N
	this.removeAll();
	if (md == null)
	    return;
	 
	SessionData sd = md.getSessionData();
	int gridy=-1;	

	if(sd == null ||
	   ("false".equals(sd.getAttributeValue("before")) &&  //NOI18N
	    "false".equals(sd.getAttributeValue("after")))) {  //NOI18N
	    


	    addGridBagComponent(this, createTopSpacer(), 0, ++gridy,
				fullGridWidth, 1, 0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.NONE,
				topSpacerInsets,
				0, 0);

	    addGridBagComponent(this, 
				createDataLabel
				(msgs.getString("MON_No_session")),
				0, ++gridy,
				fullGridWidth, 1, 0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.NONE,
				labelInsets,
				0, 0);

	    addGridBagComponent(this, createGlue(), 0, ++gridy,
				1, 1, 1.0, 1.0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.BOTH,
				zeroInsets,
				0, 0);
	    return;
	}

	String headerIn;
	String lastAccessed = null;
	String maxInactiveBefore = null;
	String maxInactiveAfter = null;

	addGridBagComponent(this, createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    topSpacerInsets,
			    0, 0);

	// We need to refer to this label below, don't use the other
	// createHeader method.
        JLabel sessionHeaderLabel =
	    createHeaderLabel(msgs.getString("MON_Session_24"));
	sessionHeaderLabel.getAccessibleContext().setAccessibleDescription(msgs.getString("ACS_MON_Session_24A11yDesc"));
	
	addGridBagComponent(this, 
			    sessionHeaderLabel, 
			    0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);
	
	String msg;
	if("true".equals(sd.getAttributeValue("before"))) {  //NOI18N
	    msg = msgs.getString("MON_Session_existed");
	    lastAccessed = 
		sd.getSessionIn().getAttributeValue("lastAccessed");  //NOI18N
	    maxInactiveBefore =
		sd.getSessionIn().getAttributeValue("inactiveInterval");  //NOI18N
	}
	else {
	    msg = msgs.getString("MON_Session_created");
	    lastAccessed = 
		sd.getSessionOut().getAttributeValue("lastAccessed"); //NOI18N
	}
	
        JLabel sessionDataLabel = createHeaderLabel(msg);
        sessionDataLabel.getAccessibleContext().setAccessibleDescription(msg);
        sessionHeaderLabel.setLabelFor(sessionDataLabel);
	addGridBagComponent(this, sessionDataLabel, 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    indentInsets,
			    0, 0);


	if("false".equals(sd.getAttributeValue("after"))) {  //NOI18N
	    msg = msgs.getString("MON_Session_invalidated");
	    addGridBagComponent(this, createDataLabel(msg), 0, ++gridy,
				fullGridWidth, 1, 0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.NONE,
				indentInsets,
				0, 0);
	}
	else {
	    maxInactiveAfter =
		sd.getSessionOut().getAttributeValue("inactiveInterval");  //NOI18N
	}
	

	boolean inactiveChanged = false;
	
	if(maxInactiveBefore == null || maxInactiveBefore.equals("")) { //NOI18N
	    if(maxInactiveAfter != null) 
		maxInactiveBefore = maxInactiveAfter;
	    // Should not happen
	    else maxInactiveBefore = "";
	}
	else if(maxInactiveAfter != null && 
		!maxInactiveBefore.equals(maxInactiveAfter)) 
	    inactiveChanged = true;
    
	// Add session properties header
        JLabel sessionPropertiesLabel = createHeaderLabel(msgs.getString("MON_Session_properties"));
        sessionPropertiesLabel.setDisplayedMnemonic(msgs.getString("MON_Session_properties_Mnemonic").charAt(0));
        sessionPropertiesLabel.getAccessibleContext().setAccessibleDescription(msgs.getString("ACS_MON_Session_propertiesA11yDesc"));
	addGridBagComponent(this, sessionPropertiesLabel, 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);

	DisplayTable dt = null;
       
	if(!inactiveChanged) {
	    String data[] = {
		sd.getAttributeValue("id"),  //NOI18N
		sd.getAttributeValue("created"), //NOI18N
		lastAccessed,
		maxInactiveBefore,
	    };
	    dt = new DisplayTable(props, data);
	}
	else {
	    String data[] = {
		sd.getAttributeValue("id"),  //NOI18N
		sd.getAttributeValue("created"), //NOI18N
		lastAccessed,
		maxInactiveBefore,
		maxInactiveAfter,
	    };
	    dt = new DisplayTable(props2, data);
	}


        sessionPropertiesLabel.setLabelFor(dt);
        dt.getAccessibleContext().setAccessibleName(msgs.getString("ACS_MON_Session_propertiesTableA11yName"));
        dt.setToolTipText(msgs.getString("ACS_MON_Session_propertiesTableA11yDesc"));
 	addGridBagComponent(this, dt, 0, ++gridy,
			    fullGridWidth, 1, tableWeightX, tableWeightY, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    tableInsets,
			    0, 0);

	// Attributes before and after 
	if("true".equals(sd.getAttributeValue("before"))) { //NOI18N
	    Param[] param = null;
	    try {
		param = sd.getSessionIn().getParam();
	    }
	    catch(Exception ex) {
	    }
	    
	    if(param == null || param.length == 0) {
		addGridBagComponent(this, createDataLabel(msgs.getString("MON_Session_no_att_before")), 0, ++gridy,
				    fullGridWidth, 1, 0, 0, 
				    java.awt.GridBagConstraints.WEST,
				    java.awt.GridBagConstraints.NONE,
				    tableInsets,
				    0, 0);
	    } else {
                JLabel sessionBeforeLabel = createHeaderLabel(msgs.getString("MON_Session_att_before"));
                sessionBeforeLabel.setDisplayedMnemonic(msgs.getString("MON_Session_att_before_Mnemonic").charAt(0));
                sessionBeforeLabel.getAccessibleContext().setAccessibleDescription(msgs.getString("ACS_MON_Session_att_beforeA11yDesc"));
		addGridBagComponent(this, sessionBeforeLabel, 0, ++gridy,
				    fullGridWidth, 1, 0, 0, 
				    java.awt.GridBagConstraints.WEST,
				    java.awt.GridBagConstraints.NONE,
				    labelInsets,
				    0, 0);
		dt = new DisplayTable(param);
                sessionBeforeLabel.setLabelFor(dt);
                dt.getAccessibleContext().setAccessibleName(msgs.getString("ACS_MON_Session_att_beforeTableA11yName"));
                dt.setToolTipText(msgs.getString("ACS_MON_Session_att_beforeTableA11yDesc"));
		addGridBagComponent(this, dt, 0, ++gridy,
				    fullGridWidth, 1, tableWeightX, tableWeightY, 
				    java.awt.GridBagConstraints.WEST,
				    java.awt.GridBagConstraints.BOTH,
				    tableInsets,
				    0, 0);
	    }
	}
	

	if("true".equals(sd.getAttributeValue("after"))) { //NOI18N
	    Param[] param = null;
	    try {
		param = sd.getSessionOut().getParam();
	    }
	    catch(Exception ex) {
	    }

	    if(param == null || param.length == 0) {
		addGridBagComponent(this, createDataLabel(msgs.getString("MON_Session_no_att_after")), 0, ++gridy,
				    fullGridWidth, 1, 0, 0, 
				    java.awt.GridBagConstraints.WEST,
				    java.awt.GridBagConstraints.NONE,
				    tableInsets,
				    0, 0);
	    } else {
                JLabel sessionAfterLabel = createHeaderLabel(msgs.getString("MON_Session_att_after"));
                sessionAfterLabel.setDisplayedMnemonic(msgs.getString("MON_Session_att_after_Mnemonic").charAt(0));
                sessionAfterLabel.getAccessibleContext().setAccessibleDescription(msgs.getString("ACS_MON_Session_att_afterA11yDesc"));
		addGridBagComponent(this, sessionAfterLabel, 0, ++gridy,
				    fullGridWidth, 1, 0, 0, 
				    java.awt.GridBagConstraints.WEST,
				    java.awt.GridBagConstraints.NONE,
				    labelInsets,
				    0, 0);
		dt = new DisplayTable(param);
                sessionAfterLabel.setLabelFor(dt);
                dt.getAccessibleContext().setAccessibleName(msgs.getString("ACS_MON_Session_att_afterTableA11yName"));
                dt.setToolTipText(msgs.getString("ACS_MON_Session_att_afterTableA11yDesc"));
		addGridBagComponent(this, dt, 0, ++gridy,
				    fullGridWidth, 1, tableWeightX, tableWeightY, 
				    java.awt.GridBagConstraints.WEST,
				    java.awt.GridBagConstraints.BOTH,
				    tableInsets,
				    0, 0);
	    }
	    this.add(createRigidArea());
	}

	addGridBagComponent(this, createGlue(), 0, ++gridy,
			    1, 1, 1.0, 1.0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    zeroInsets,
			    0, 0);

	this.setMaximumSize(this.getPreferredSize()); 
	this.repaint();
    }
} // SessionDisplay
