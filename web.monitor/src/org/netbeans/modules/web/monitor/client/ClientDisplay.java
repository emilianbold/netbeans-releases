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
 * @author Ana von Klopp
 * @version
 */

package org.netbeans.modules.web.monitor.client;

import javax.swing.table.*;     // widgets
import org.netbeans.modules.web.monitor.data.*;
import org.netbeans.modules.schema2beans.BaseBean;
import org.openide.util.NbBundle;
import java.util.*;

public class ClientDisplay extends DataDisplay {
    
    private final static boolean debug = false;
    private static final ResourceBundle msgs =
	NbBundle.getBundle(TransactionView.class);
    

    private DisplayTable clientTable = null; 
    private DisplayTable engineTable = null;

    private String[] categories = { 
	    msgs.getString("MON_Protocol"), 
	    msgs.getString("MON_Remote_Address"), 
	    msgs.getString("MON_Software"), 
	    msgs.getString("MON_Locale"), 
	    msgs.getString("MON_Encodings"), 
	    msgs.getString("MON_Fileformats"), 
	    msgs.getString("MON_Charsets")
	};

    private static final String[] props = { 
	msgs.getString("MON_Java_version"),
	msgs.getString("MON_Platform"),
	msgs.getString("MON_Server_name"),
	msgs.getString("MON_Server_port"),

    };

    public ClientDisplay() {
	super();
    }

    private void createPanelWidgets() {

	int gridy = -1;

	addGridBagComponent(this, createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    topSpacerInsets,
			    0, 0);

	clientTable = new DisplayTable(categories);
        clientTable.getAccessibleContext().setAccessibleName(msgs.getString("ACS_MON_ClientTable_3A11yName"));
        clientTable.setToolTipText(msgs.getString("ACS_MON_ClientTable_3A11yDesc"));
	addGridBagComponent(this, 
			    createHeaderLabel
			    (msgs.getString("MON_Client_3"), 
			     msgs.getString("MON_Client_3_Mnemonic").charAt(0), 
			     msgs.getString("ACS_MON_Client_3A11yDesc"), 
			     clientTable),
                            0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);

	addGridBagComponent(this, clientTable, 0, ++gridy,
			    fullGridWidth, 1, tableWeightX, tableWeightY, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    tableInsets,
			    0, 0);

	engineTable = new DisplayTable(props);
        engineTable.getAccessibleContext().setAccessibleName(msgs.getString("ACS_MON_Servlet_engineTableA11yName"));
        engineTable.setToolTipText(msgs.getString("ACS_MON_Servlet_engineTableA11yDesc"));
	addGridBagComponent(this, 
			    createHeaderLabel
			    (msgs.getString("MON_Servlet_engine"), 
			     msgs.getString("MON_Servlet_engine_Mnemonic").charAt(0), 
			     msgs.getString("ACS_MON_Servlet_engineA11yDesc"),
			     engineTable),
                            0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);


	addGridBagComponent(this, engineTable, 0, ++gridy,
			    fullGridWidth, 1, tableWeightX, tableWeightY, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    tableInsets,
			    0, 0);

	addGridBagComponent(this, createGlue(), 0, ++gridy,
			    1, 1, 1.0, 1.0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    zeroInsets,
			    0, 0);



    }

    // We're treating these as if they are all strings at the
    // moment. In reality they can be of different types, though maybe 
    // that does not matter...
    public void setData(DataRecord md) {

	if(debug) System.out.println("in ClientDisplay.setData()"); //NOI18N
	this.removeAll();
	if (md == null)
	    return;

	createPanelWidgets();

	ClientData cd = md.getClientData();
	clientTable.setValueAt(cd.getAttributeValue("protocol"), 0,1); // NOI18N
	clientTable.setValueAt(cd.getAttributeValue("remoteAddress"),1,1); // NOI18N
	clientTable.setValueAt(cd.getAttributeValue("software"), 2,1); // NOI18N
	clientTable.setValueAt(cd.getAttributeValue("locale"), 3,1); // NOI18N
	clientTable.setValueAt(cd.getAttributeValue("encodingsAccepted"), 4,1); // NOI18N
	clientTable.setValueAt(cd.getAttributeValue("formatsAccepted"), 5,1); // NOI18N
	clientTable.setValueAt(cd.getAttributeValue("charsetsAccepted"), 6,1); // NOI18N

	EngineData ed = md.getEngineData();
	if(ed != null) {
	    engineTable.setValueAt(ed.getAttributeValue("jre"), 0, 1); //NOI18N
	    engineTable.setValueAt(ed.getAttributeValue("platform"), 1, 1); //NOI18N
	    engineTable.setValueAt(ed.getAttributeValue("serverName"), 2, 1); //NOI18N
	    engineTable.setValueAt(ed.getAttributeValue("serverPort"), 3, 1); //NOI18N
	}
	// This is only for backwards compatibility with data
	// collected under FFJ 3.0
	else {
	    ServletData sd = md.getServletData();
	    engineTable.setValueAt(sd.getAttributeValue("jre"), 0, 1); //NOI18N
	    engineTable.setValueAt(sd.getAttributeValue("platform"), 1, 1); //NOI18N
	    engineTable.setValueAt(sd.getAttributeValue("serverName"), 2, 1); //NOI18N
	    engineTable.setValueAt(sd.getAttributeValue("serverPort"), 3, 1); //NOI18N
	}
    }

} // ClientDisplay
