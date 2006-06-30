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
 * HeaderDisplay.java
 *
 *
 * Created: Wed Jan 31 18:04:22 2001
 *
 * @author Ana von Klopp
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


public class HeaderDisplay extends DataDisplay {
    
    private final static boolean debug = false;

    private DisplayTable dt = null; 
        
    public HeaderDisplay() {

	super();
    }


    // We're treating these as if they are all strings at the
    // moment. In reality they can be of different types, though maybe 
    // that does not matter...
    public void setData(DataRecord md) {

	if(debug) System.out.println("in HeaderDisplay.setData()"); //NOI18N

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
	    msg = NbBundle.getBundle(HeaderDisplay.class).getString("MON_No_headers");
	    hLabel = createDataLabel(msg);
	} else {
	    msg = NbBundle.getBundle(HeaderDisplay.class).getString("MON_HTTP_Headers");
	    headerTable = new DisplayTable(params, true);
            headerTable.getAccessibleContext().setAccessibleName(NbBundle.getBundle(HeaderDisplay.class).getString("ACS_MON_HTTP_HeadersTableA11yName"));
            headerTable.setToolTipText(NbBundle.getBundle(HeaderDisplay.class).getString("ACS_MON_HTTP_HeadersTableA11yDesc"));
	    hLabel = createSortButtonLabel(msg, headerTable, NbBundle.getBundle(HeaderDisplay.class).getString("MON_HTTP_Headers_Mnemonic").charAt(0), NbBundle.getBundle(HeaderDisplay.class).getString("ACS_MON_HTTP_HeadersA11yDesc"));
	}

	addGridBagComponent(this, createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    topSpacerInsets,
			    0, 0);

	addGridBagComponent(this, hLabel, 0, ++gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);


	if(params != null && params.length > 0) {
	    addGridBagComponent(this, headerTable, 0, ++gridy,
				fullGridWidth, 1, tableWeightX, tableWeightY, 
				java.awt.GridBagConstraints.WEST,
				java.awt.GridBagConstraints.HORIZONTAL,
				tableInsets,
				0, 0);
	}

	addGridBagComponent(this, Box.createGlue(), 0, ++gridy,
			    1, 1, 1.0, 1.0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    zeroInsets,
			    0, 0);

	this.setMaximumSize(this.getPreferredSize()); 
	this.repaint();
    }
} // HeaderDisplay
