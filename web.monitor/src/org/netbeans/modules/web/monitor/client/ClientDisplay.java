/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
    private DisplayTable clientTable = null;
    private DisplayTable engineTable = null;

    private String[] categories = { 
	    NbBundle.getBundle(ClientDisplay.class).getString("MON_Protocol"), 
	    NbBundle.getBundle(ClientDisplay.class).getString("MON_Remote_Address"), 
	    NbBundle.getBundle(ClientDisplay.class).getString("MON_Software"), 
	    NbBundle.getBundle(ClientDisplay.class).getString("MON_Locale"), 
	    NbBundle.getBundle(ClientDisplay.class).getString("MON_Encodings"), 
	    NbBundle.getBundle(ClientDisplay.class).getString("MON_Fileformats"), 
	    NbBundle.getBundle(ClientDisplay.class).getString("MON_Charsets")
	};

    private static final String[] props = { 
	NbBundle.getBundle(ClientDisplay.class).getString("MON_Java_version"),
	NbBundle.getBundle(ClientDisplay.class).getString("MON_Platform"),
	NbBundle.getBundle(ClientDisplay.class).getString("MON_Server_name"),
	NbBundle.getBundle(ClientDisplay.class).getString("MON_Server_port"),

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
        clientTable.getAccessibleContext().setAccessibleName(NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_ClientTable_3A11yName"));
        clientTable.setToolTipText(NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_ClientTable_3A11yDesc"));
	addGridBagComponent(this, 
			    createHeaderLabel
			    (NbBundle.getBundle(ClientDisplay.class).getString("MON_Client_3"), 
			     NbBundle.getBundle(ClientDisplay.class).getString("MON_Client_3_Mnemonic").charAt(0), 
			     NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Client_3A11yDesc"), 
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
        engineTable.getAccessibleContext().setAccessibleName(NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Servlet_engineTableA11yName"));
        engineTable.setToolTipText(NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Servlet_engineTableA11yDesc"));
	addGridBagComponent(this, 
			    createHeaderLabel
			    (NbBundle.getBundle(ClientDisplay.class).getString("MON_Servlet_engine"), 
			     NbBundle.getBundle(ClientDisplay.class).getString("MON_Servlet_engine_Mnemonic").charAt(0), 
			     NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Servlet_engineA11yDesc"),
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
