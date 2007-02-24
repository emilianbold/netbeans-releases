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

package org.netbeans.modules.uml.core.support.umlsupport;

/**
 *
 * @author Trey Spiva
 */
public interface FileExtensions
{
	//	Diagram extensions - broke into the TomSawyer file and the presentation data file
	public final static String DIAGRAM_LAYOUT_EXT = ".etld";
	public final static String DIAGRAM_LAYOUT_EXT_NODOT = "etld";
	public final static String DIAGRAM_PRESENTATION_EXT = ".etlp";
	public final static String DIAGRAM_PRESENTATION_EXT_NODOT = "etlp";
	public final static String DIAGRAM_PRECOMMIT_LAYOUT_EXT = ".etld~";
	public final static String DIAGRAM_PRECOMMIT_LAYOUT_EXT_NODOT = "etld~";
	public final static String DIAGRAM_PRECOMMIT_PRESENTATION_EXT = ".etlp~";
	public final static String DIAGRAM_PRECOMMIT_PRESENTATION_EXT_NODOT = "etlp~";
	
	//	Used to search for diagram files
	//public final static String DIAGRAM_SEARCH_STRING = "*.etlp";
   public final static String DIAGRAM_SEARCH_STRING = DIAGRAM_PRESENTATION_EXT_NODOT;
	
	//	Workspace File
	public final static String WS_EXT = ".etw";
	public final static String WS_EXT_NODOT = "etw";
	
	//	Visual Basic Project
	public final static String VBP_EXT = ".etv";
	public final static String VBP_EXT_NODOT = "etv";
	
	//	Meta Data
	public final static String MD_EXT = ".etd";
	public final static String MD_EXT_NODOT = "etd";
	
	//	Pattern project file Data
	public final static String PATTERN_EXT = ".etpat";
	public final static String PATTERN_EXT_NODOT = "etpat";
	
	//	Versioned Files
	public final static String VER_EXT = ".etx";
	public final static String VER_EXT_NODOT = "etx";
	
	//	Configuration and Preferences
	public final static String PREF_EXT = ".etcd";
	public final static String PREF_EXT_NODOT = "etcd";
	
	//	Type Management File
	public final static String TYPE_MGMT_EXT = ".ettm";
	public final static String TYPE_MGMT_EXT_NODOT = "ettm";
	
	//	Profile File
	public final static String PROFILE_EXT = ".etup";
	public final static String PROFILE_EXT_NODOT = "etup";

}
