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
 * ContextDisplay.java
 *
 *
 * Created: Wed Jan 16 14:44:11 PST 2002
 *
 * @author Ana von Klopp 
 * @version
 */

package org.netbeans.modules.web.monitor.client;

import javax.swing.table.*;     // widgets
import org.netbeans.modules.web.monitor.data.*;
import org.openide.util.NbBundle;
import java.util.*;


public class ContextDisplay extends DataDisplay {
    
    private final static boolean debug = false;
    
    DisplayTable contextTable;

    public ContextDisplay() {
	super();
    }

    // We're treating these as if they are all strings at the
    // moment. In reality they can be of different types, though maybe 
    // that does not matter...
    public void setData(DataRecord md) {

	//boolean iplanet = false;
	boolean servlet = true;
	boolean failed = false; 
	
	if(debug) System.out.println("in ContextDisplay.setData()"); //NOI18N
	this.removeAll();
	if (md == null)
	    return;
	
	ContextData cd = md.getContextData();
	if(cd != null) displayContextData(cd);
	else {
	    ServletData sd = md.getServletData();
	    displayServletData(sd);
	    
	}
    }
    
    private void displayContextData(ContextData cd) 
    {
	if(debug) System.out.println(cd.dumpBeanNode());

	String[] props = { 
	    NbBundle.getBundle(ClientDisplay.class).getString("MON_Context_name"),
	    NbBundle.getBundle(ClientDisplay.class).getString("MON_Absolute_path"),
	};	
    
	contextTable = new DisplayTable(props);

	int gridy = -1;

	addGridBagComponent(this, createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    topSpacerInsets,
			    0, 0);

	// context data 
	addGridBagComponent(this, 
			    createHeaderLabel
			    (NbBundle.getBundle(ClientDisplay.class).getString("MON_Servlet_context"), NbBundle.getBundle(ClientDisplay.class).getString("MON_Servlet_context_Mnemonic").charAt(0), NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Servlet_contextA11yDesc"), contextTable), 
			    0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);

	
	contextTable.setValueAt(cd.getAttributeValue("contextName"), 0, 1);  //NOI18N
	contextTable.setValueAt(cd.getAttributeValue("absPath"), 1, 1);  //NOI18N
        contextTable.getAccessibleContext().setAccessibleName(NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Servlet_contextTableA11yName"));
        contextTable.setToolTipText(NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Servlet_contextTableA11yDesc"));
	addGridBagComponent(this, contextTable, 0, ++gridy,
			    fullGridWidth, 1, tableWeightX, tableWeightY, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    tableInsets,
			    0, 0);
	
	
	Param[] ctxtparam = null;
	try {
	    ctxtparam = cd.getContextAttributes().getParam();
	}
	catch(Exception ex) {
	}

	if(ctxtparam != null && ctxtparam.length > 0) {

	    DisplayTable dt = new DisplayTable(ctxtparam);
	    addGridBagComponent(this,
				createHeaderLabel
				(NbBundle.getBundle(ClientDisplay.class).getString("MON_Context_att"),
				 NbBundle.getBundle(ClientDisplay.class).getString("MON_Context_att_Mnemonic").charAt(0), 
				 NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Context_att_A11yDesc"),
				 dt),
				0, ++gridy,
				fullGridWidth, 1, 0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.NONE,
				labelInsets,
				0, 0);
	    dt.getAccessibleContext().setAccessibleName(NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Context_att_TableA11yName"));
	    dt.setToolTipText(NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Context_att_TableA11yDesc"));
	    addGridBagComponent(this, dt, 0, ++gridy,
				fullGridWidth, 1, tableWeightX, tableWeightY, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.BOTH,
				tableInsets,
				0, 0);	
	}

	// Init parameters
	Param[] param = cd.getParam();
	if(param == null || param.length == 0) 
	    addGridBagComponent(this, 
				createDataLabel
				(NbBundle.getBundle(ClientDisplay.class).getString("MON_No_init")),
				0, 
				++gridy, 
				fullGridWidth, 
				1, 0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.NONE,
				labelInsets,
				0, 0);
	else {

	    DisplayTable paramTable = new DisplayTable(param); 
	    paramTable.getAccessibleContext().setAccessibleName(NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Init_parametersTableA11yName"));
	    paramTable.setToolTipText(NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Init_parametersTableA11yDesc"));
	    addGridBagComponent(this, 
				createHeaderLabel
				(NbBundle.getBundle(ClientDisplay.class).getString("MON_Init_parameters"), 
				 NbBundle.getBundle(ClientDisplay.class).getString("MON_Init_parameters_Mnemonic").charAt(0),
				 NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Init_parametersA11yDesc"),
				 paramTable),
				0, ++gridy,
				fullGridWidth, 1, 0, 0, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.NONE,
				labelInsets,
				0, 0);
	    addGridBagComponent(this, paramTable, 0, ++gridy,
				fullGridWidth, 1, tableWeightX,
				tableWeightY,  
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.BOTH,
				tableInsets,
				0, 0);
	}

	addGridBagComponent(this, createGlue(), 0, ++gridy,
			    1, 1, 1.0, 1.0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    zeroInsets,
			    0, 0);
    }

    // This method is for backwards compatibility only, it will
    // display context data recorded with FFJ 3.0
    private void displayServletData(ServletData sd) 
    {
	if(debug) System.out.println(sd.dumpBeanNode());

	String[] props = { 
	    NbBundle.getBundle(ClientDisplay.class).getString("MON_Context_name"),
	    NbBundle.getBundle(ClientDisplay.class).getString("MON_Absolute_path"),
	};	

	contextTable = new DisplayTable(props);
	
	int gridy = -1;

	addGridBagComponent(this, createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    topSpacerInsets,
			    0, 0);

	// context data 
	addGridBagComponent(this, 
			    createHeaderLabel
			    (NbBundle.getBundle(ClientDisplay.class).getString("MON_Servlet_context"), NbBundle.getBundle(ClientDisplay.class).getString("MON_Servlet_context_Mnemonic").charAt(0), NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Servlet_contextA11yDesc"), contextTable), 
			    0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);

	
	contextTable.setValueAt(sd.getAttributeValue("contextName"), 0, 1);  //NOI18N
	contextTable.setValueAt(sd.getAttributeValue("absPath"), 1, 1);  //NOI18N
        contextTable.getAccessibleContext().setAccessibleName(NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Servlet_contextTableA11yName"));
        contextTable.setToolTipText(NbBundle.getBundle(ClientDisplay.class).getString("ACS_MON_Servlet_contextTableA11yDesc"));
	addGridBagComponent(this, contextTable, 0, ++gridy,
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
} // ContextDisplay
