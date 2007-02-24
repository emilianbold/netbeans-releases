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
 * Created on Apr 15, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.netbeans.modules.uml.ui.support.drawingproperties;

/**
 * @author jingmingm
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface IDrawingProps
{
	public static String IDS_PROJNAME = "DrawingProps";
	public static String IDS_AUTOMATIC = "Automatic";
	public static String IDS_OTHER = "Other";
	public static String IDS_FONTTITLE = "Fonts";
	public static String IDS_COLORTITLE = "Colors";
	public static String IDS_OTHERSTRING = "Other...";
	public static String IDS_DEFAULTSTRING = "Default";
	public static String IDS_FONTDLGTITLE = "Font";
	public static String IDS_RESETDRAWENGINE = "Reset Object Type";
	public static String IDS_RESETDRAWENGINE_MSG = "Reset all presentation elements of type '%1' on the current diagram to application default colors?";
	public static String IDS_RESETDIAGRAM = "Reset Diagram";
	public static String IDS_RESETDIAGRAM_MSG = "Reset all presentation elements on the current diagram to application default colors?";
	public static String IDS_GENERIC_DESC = "The colors and fonts for this object";
	public static String IDS_TITLEClassDrawEnginePropPage = "Class Draw Engine";
	public static String IDS_HELPFILEClassDrawEnginePropPage = "Describe.chm";
	public static String IDS_DOCSTRINGClassDrawEnginePropPage = "Colors for the Class Draw Engine";
	public static String IDS_RESETPE = "Reset all presentation elements of type '%1' on the current diagram to diagram default colors?";
	public static String IDS_RESETPE_MSG = "Reset all presentation elements on the current diagram to diagram default colors?";
	public static String IDS_TITLEGenericPropertyPage = "Generic Property Page";
	public static String IDS_HELPFILEGenericPropertyPage = "Describe.chm";
	public static String IDS_DOCSTRINGGenericPropertyPage = "Generic property page";
	public static String IDS_RESETDIAGRAMS = "Reset Diagrams";
	public static String IDS_RESETDIAGRAMS_MSG = "Do you want to reset all the fonts and colors on your open diagrams?";
	
        //public static String IDS_ACTIVITY_DIAGRAM = "Activity Diagram";
        public static String IDS_ACTIVITY_DIAGRAM = DrawingPropertyResource.getString("IDS_ACTIVITY_DIAGRAM");
        
	//public static String IDS_CLASS_DIAGRAM = "Class Diagram";
        public static String IDS_CLASS_DIAGRAM = DrawingPropertyResource.getString("IDS_CLASS_DIAGRAM");
        
	//public static String IDS_COLLABORATION_DIAGRAM = "Collaboration Diagram";
        public static String IDS_COLLABORATION_DIAGRAM = DrawingPropertyResource.getString("IDS_COLLABORATION_DIAGRAM");
        
	//public static String IDS_COMPONENT_DIAGRAM = "Component Diagram";
        public static String IDS_COMPONENT_DIAGRAM = DrawingPropertyResource.getString("IDS_COMPONENT_DIAGRAM");
        
	//public static String IDS_DEPLOYMENT_DIAGRAM = "Deployment Diagram";
        public static String IDS_DEPLOYMENT_DIAGRAM = DrawingPropertyResource.getString("IDS_DEPLOYMENT_DIAGRAM");
        
	//public static String IDS_SEQUENCE_DIAGRAM = "Sequence Diagram";
        public static String IDS_SEQUENCE_DIAGRAM = DrawingPropertyResource.getString("IDS_SEQUENCE_DIAGRAM");
        
	//public static String IDS_STATE_DIAGRAM = "State Diagram";
        public static String IDS_STATE_DIAGRAM = DrawingPropertyResource.getString("IDS_STATE_DIAGRAM");
        
	//public static String IDS_USECASE_DIAGRAM = "Use Case Diagram";
        public static String IDS_USECASE_DIAGRAM = DrawingPropertyResource.getString("IDS_USECASE_DIAGRAM");
        
	public static String IDS_ENTITY_DIAGRAM = "Entity Diagram";
        
	//public static String IDS_ALL = "All Diagrams";
        public static String IDS_ALL = DrawingPropertyResource.getString("IDS_ALL");
}
