/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.uml.core.support.umlsupport;

/**
 *
 * @author Trey Spiva
 */
public interface FileExtensions
{
	//	Diagram extensions - broke into the TomSawyer file and the presentation data file
	public final static String DIAGRAM_LAYOUT_EXT = ".diagram";
	public final static String DIAGRAM_LAYOUT_EXT_NODOT = "diagram";
	public final static String DIAGRAM_PRESENTATION_EXT = DIAGRAM_LAYOUT_EXT;
	public final static String DIAGRAM_PRESENTATION_EXT_NODOT = DIAGRAM_LAYOUT_EXT_NODOT;

	public final static String DIAGRAM_TS_LAYOUT_EXT = ".etld";
	public final static String DIAGRAM_TS_LAYOUT_EXT_NODOT = "etld";
	public final static String DIAGRAM_TS_PRESENTATION_EXT = ".etlp";
	public final static String DIAGRAM_TS_PRESENTATION_EXT_NODOT = "etlp";

	//	Used to search for diagram files
	//public final static String DIAGRAM_SEARCH_STRING = "*.etlp";
    //   public final static String DIAGRAM_SEARCH_STRING = DIAGRAM_PRESENTATION_EXT_NODOT;
	
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
