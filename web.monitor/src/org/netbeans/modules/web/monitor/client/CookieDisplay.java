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
 * @author Ana von Klopp
 * @version
 */

package org.netbeans.modules.web.monitor.client;

import javax.swing.table.*;     // widgets
import javax.swing.JLabel;
import org.netbeans.modules.web.monitor.data.*;
import org.openide.util.NbBundle;
import java.util.ResourceBundle;

// PENDING: can be more helpful with what the cookie data means. Like
// I had the expires at the end of this session before, that was kind
// of useful. Could also show the actual date that the cookie
// expires. 

public class CookieDisplay extends DataDisplay {
    
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
        
    public CookieDisplay() {
	super();
    }

    // We're treating these as if they are all strings at the
    // moment. In reality they can be of different types, though maybe 
    // that does not matter...
    public void setData(DataRecord md) {

	if(debug) System.out.println("in CookieDisplay.setData()"); //NOI18N
	this.removeAll();
	if (md == null)
	    return;
	 
	CookiesData cd = md.getCookiesData();
	CookieIn[] in = cd.getCookieIn();
	CookieOut[] out = cd.getCookieOut();

	int gridy = -1;
	String headerIn;
	JLabel incomingLabel;
	if(in == null || in.length == 0) {
	    headerIn = msgs.getString("MON_No_incoming");
	    incomingLabel = createDataLabel(headerIn);

	} else {
	    headerIn = msgs.getString("MON_Incoming_cookie");
	    incomingLabel = createHeaderLabel(headerIn, msgs.getString("MON_Incoming_cookie_Mnemonic").charAt(0), msgs.getString("ACS_MON_Incoming_cookieA11yDesc"), null);
	}

	addGridBagComponent(this, createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    topSpacerInsets,
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
		    in[i].getAttributeValue("name"), //NOI18N
		    in[i].getAttributeValue("value") //NOI18N
		};
		DisplayTable dt = new DisplayTable(categoriesIn, data);
                dt.getAccessibleContext().setAccessibleName(msgs.getString("ACS_MON_Incoming_cookieTableA11yName"));
                dt.setToolTipText(msgs.getString("ACS_MON_Incoming_cookieTableA11yDesc"));
                incomingLabel.setLabelFor(dt);
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
	    outgoingLabel = createDataLabel(headerOut);
	} else {
	    headerOut = msgs.getString("MON_Outgoing_cookie");
	    outgoingLabel = createHeaderLabel(headerOut, msgs.getString("MON_Outgoing_cookie_Mnemonic").charAt(0), msgs.getString("ACS_MON_Outgoing_cookieA11yDesc"), null);
	}
	addGridBagComponent(this, outgoingLabel, 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);

	if(out != null && out.length > 0) {

	    for(int i=0; i<out.length; ++i) {
		String cookieMaxAge =
		    out[i].getAttributeValue("maxAge"); //NOI18N
		if(cookieMaxAge.equals("-1")) //NOI18N
		    cookieMaxAge = msgs.getString("MON_this_session");
		
		String[] data = {
		    out[i].getAttributeValue("name"),    //NOI18N
		    out[i].getAttributeValue("value"),   //NOI18N
		    out[i].getAttributeValue("domain"),  //NOI18N
		    out[i].getAttributeValue("path"),    //NOI18N
		    cookieMaxAge,
		    out[i].getAttributeValue("version"), //NOI18N
		    out[i].getAttributeValue("secure"),  //NOI18N
		    out[i].getAttributeValue("comment")  //NOI18N
		};
		DisplayTable dt = new DisplayTable(categoriesOut, data);
                dt.getAccessibleContext().setAccessibleName(msgs.getString("ACS_MON_Outgoing_cookieTableA11yName"));
                dt.setToolTipText(msgs.getString("ACS_MON_Outgoing_cookieTableA11yDesc"));
                outgoingLabel.setLabelFor(dt);
		addGridBagComponent(this, dt, 0, ++gridy,
			    fullGridWidth, 1, tableWeightX, tableWeightY, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    tableInsets,
			    0, 0);
	    }
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
} // CookieDisplay
