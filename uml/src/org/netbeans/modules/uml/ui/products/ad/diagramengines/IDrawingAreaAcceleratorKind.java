/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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


/*
 * Created on Feb 24, 2004
 *
 */
package org.netbeans.modules.uml.ui.products.ad.diagramengines;

import java.util.ResourceBundle;

/**
 * @author jingmingm
 *
 */
public interface IDrawingAreaAcceleratorKind
{
	public static final String BUNDLE_NAME = "org.netbeans.modules.uml.ui.products.ad.diagramengines.Bundle"; //$NON-NLS-1$
	public static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	
	// Compartment Navigation
	public static final String DAVK_DOWN = "VK_DOWN";
	public static final String DAVK_UP = "VK_UP";
	public static final String DAVK_TAB = "VK_TAB";
	// Creating various nodes
	public static final String DAVK_CREATE_CLASS = RESOURCE_BUNDLE.getString("IDS_ALTSHIFTC"); //(CLD and COD Only)
	public static final String DAVK_CREATE_SIMPLESTATE = RESOURCE_BUNDLE.getString("IDS_ALTSHIFTC"); //(STD Only)
	public static final String DAVK_CREATE_PACKAGE = RESOURCE_BUNDLE.getString("IDS_ALTSHIFTK"); //(CLD, UCD, DPD and COD Only)
	public static final String DAVK_CREATE_INTERFACE = RESOURCE_BUNDLE.getString("IDS_ALTSHIFTN"); //(CLD and COD Only)
	public static final String DAVK_CREATE_USECASE = RESOURCE_BUNDLE.getString("IDS_ALTSHIFTU"); //(CLD, UCD and COD Only)
	public static final String DAVK_CREATE_ATTRIBUTE = RESOURCE_BUNDLE.getString("IDS_ALTSHIFTA");
	public static final String DAVK_CREATE_OPERATION = RESOURCE_BUNDLE.getString("IDS_ALTSHIFTO");
	public static final String DAVK_CREATE_PARAMETER = RESOURCE_BUNDLE.getString("IDS_ALTSHIFTP");
	public static final String DAVK_CREATE_INVOCATION = RESOURCE_BUNDLE.getString("IDS_ALTSHIFTA"); //(ACD Only)
	public static final String DAVK_CREATE_ACTOR = RESOURCE_BUNDLE.getString("IDS_ALTSHIFTA"); //(UCD Only)
	public static final String DAVK_CREATE_DECISION = RESOURCE_BUNDLE.getString("IDS_ALTSHIFTC"); //(ACD Only)
	public static final String DAVK_CREATE_ERENTITY = RESOURCE_BUNDLE.getString("IDS_ALTSHIFTC"); //(ETD Only)
	// Layout
	public static final String DAVK_LAYOUT_CIRCULAR_STYLE = RESOURCE_BUNDLE.getString("IDS_CTRLSHIFTC");
	public static final String DAVK_LAYOUT_HIERARCHICAL_STYLE = RESOURCE_BUNDLE.getString("IDS_CTRLSHIFTH");
	public static final String DAVK_LAYOUT_INCREMENTAL_LAYOUT = RESOURCE_BUNDLE.getString("IDS_CTRLSHIFTI");
	public static final String DAVK_LAYOUT_RELAYOUT = RESOURCE_BUNDLE.getString("IDS_CTRLSHIFTL");
	public static final String DAVK_LAYOUT_ORTHOGONAL_STYLE = RESOURCE_BUNDLE.getString("IDS_CTRLSHIFTB");
	public static final String DAVK_LAYOUT_SEQUENCE_DIAGRAM_LAYOUT = RESOURCE_BUNDLE.getString("IDS_CTRLSHIFTQ");
	public static final String DAVK_LAYOUT_TREE_STYLE = RESOURCE_BUNDLE.getString("IDS_CTRLSHIFTT");
	public static final String DAVK_LAYOUT_SYMMETRIC_STYLE = RESOURCE_BUNDLE.getString("IDS_CTRLSHIFTY");
        
        // Export As Image / Fit to Window / Print Preview 
        public static final String DAVK_EXPORT_AS_IMAGE = RESOURCE_BUNDLE.getString("IDS_CTRLSHIFTX");
        public static final String DAVK_FIT_TO_WINDOW = RESOURCE_BUNDLE.getString("IDS_CTRLSHIFTF");
        public static final String DAVK_PRINT_PREVIEW = RESOURCE_BUNDLE.getString("IDS_CTRLSHIFTZ"); 
        
        // Other Toolbar Actions
        public static final String DAVK_MOVE_FORWARD = RESOURCE_BUNDLE.getString("IDS_CTRLALTSHIFTU");
        public static final String DAVK_MOVE_TO_FRONT = RESOURCE_BUNDLE.getString("IDS_CTRLALTSHIFTF");
        public static final String DAVK_MOVE_BACKWARD = RESOURCE_BUNDLE.getString("IDS_CTRLALTSHIFTD");
        public static final String DAVK_MOVE_TO_BACK = RESOURCE_BUNDLE.getString("IDS_CTRLALTSHIFTB"); 
        
        public static final String DAVK_RELATIONSHIP_DISCOVERY = RESOURCE_BUNDLE.getString("IDS_CTRLALTSHIFTH"); 
        public static final String DAVK_SELECT_MODE = RESOURCE_BUNDLE.getString("IDS_CTRLSHIFTS"); 
        public static final String DAVK_PAN_MODE = RESOURCE_BUNDLE.getString("IDS_CTRLALTSHIFTP");
        public static final String DAVK_ZOOM_WITH_MARQUEE_MODE = RESOURCE_BUNDLE.getString("IDS_CTRLALTSHIFTZ");
        public static final String DAVK_ZOOM_INTERACTIVELY_MODE = RESOURCE_BUNDLE.getString("IDS_CTRLALTSHIFTI");
        public static final String DAVK_NAVIGATE_LINK_MODE = RESOURCE_BUNDLE.getString("IDS_CTRLALTSHIFTL");
        public static final String DAVK_DIAGRAM_SYNC = RESOURCE_BUNDLE.getString("IDS_CTRLALTSHIFTR");
        
	// Creating a new operation on a seq diagram message
	public static final String DAVK_CREATE_NEW_OPERATION = "";
	// Select all
	public static final String DAVK_SELECT_ALL = RESOURCE_BUNDLE.getString("IDS_CTRLA");
	// Select all similar
	public static final String DAVK_SELECT_ALL_SIMILAR = RESOURCE_BUNDLE.getString("IDS_CTRLSHIFTA");
        //Save
        public static final String DAVK_SAVE = RESOURCE_BUNDLE.getString("IDS_CTRLS");
	// Copy/Paste/Cut/Delete
	public static final String DAVK_COPY = RESOURCE_BUNDLE.getString("IDS_CTRLC");
	public static final String DAVK_PASTE = RESOURCE_BUNDLE.getString("IDS_CTRLV");
	public static final String DAVK_CUT = RESOURCE_BUNDLE.getString("IDS_CTRLX");
	public static final String DAVK_DELETE = RESOURCE_BUNDLE.getString("IDS_DEL");
	// Resize element to contents
	public static final String DAVK_RESIZE_TO_CONTENTS = RESOURCE_BUNDLE.getString("IDS_CTRLSHIFTR");
	// Toggle Orthogonality
	public static final String DAVK_TOGGLE_ORTHOGONALITY = RESOURCE_BUNDLE.getString("IDS_CTRLSHIFTG");
	// Show friendly names
	public static final String DAVK_SHOW_FRIENDLY_NAMES = RESOURCE_BUNDLE.getString("IDS_CTRLM");
	// Create some relationship types
	public static final String DAVK_CREATE_ABSTRACTION = RESOURCE_BUNDLE.getString("IDS_ALTSHIFTB");
	public static final String DAVK_CREATE_GENERALIZATION = RESOURCE_BUNDLE.getString("IDS_ALTSHIFTG"); //(CLD Only)
	public static final String DAVK_CREATE_IMPLEMENTATION = RESOURCE_BUNDLE.getString("IDS_ALTSHIFTL");
	public static final String DAVK_CREATE_ASSOCIATION = RESOURCE_BUNDLE.getString("IDS_ALTSHIFTS");
	public static final String DAVK_SYNCHRONUS_MESSAGE = RESOURCE_BUNDLE.getString("IDS_ALTSHIFTS"); //(SQD Only)
	public static final String DAVK_CREATE_DEPENDENCY = RESOURCE_BUNDLE.getString("IDS_ALTSHIFTY");
	public static final String DAVK_CREATE_REALIZATION = ""; //Not used right now
	public static final String DAVK_CREATE_ACTIVITYEDGE = RESOURCE_BUNDLE.getString("IDS_CTRLALTSHIFTG"); //(ACD Only)
	public static final String DAVK_CREATE_STATETRANSITION = RESOURCE_BUNDLE.getString("IDS_ALTSHIFTR"); //(STD Only)
   
	public static final String DAVK_CREATE_AGGREGATION = RESOURCE_BUNDLE.getString("IDS_ALTSHIFTR");
	public static final String DAVK_CREATE_COMPOSITION = RESOURCE_BUNDLE.getString("IDS_ALTSHIFTZ");
        
        public static final String DAVK_CREATE_MESSAGE_AFTER = RESOURCE_BUNDLE.getString("IDS_ALTENTER");
}



