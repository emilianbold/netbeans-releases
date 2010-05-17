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

package org.netbeans.modules.uml.common;

/**
 * Handles locale dependant error messages.
 */
public interface ETStrings
{

	StringResolver rb = new StringResolver("org.netbeans.modules.uml.common.nls.Bundle");

	/**
	 * General
	 */
	
	// Error id was not present in the resource bundle
	MsgID E_CMN_UNKNOWN_MSGID = new MsgID("E_CMN_UNKNOWN_MSGID", rb);
	// Error id was not present in the resource bundle
	MsgID E_CMN_ACCESS_DENIED = new MsgID("E_CMN_ACCESS_DENIED", rb);
	// An unexpected error occurred
	MsgID E_CMN_UNEXPECTED_EXC = new MsgID("E_CMN_UNEXPECTED_EXC", rb);
	// File does not exist
	MsgID E_CMN_FILE_NO_EXIST = new MsgID("E_CMN_FILE_NO_EXIST", rb);
	// Error writing file
	MsgID E_CMN_WRITE_FILE = new MsgID("E_CMN_WRITE_FILE", rb);
	// Directory does not exist
	MsgID E_CMN_DIR_NO_EXIST = new MsgID("E_CMN_DIR_NO_EXIST", rb);
	// Initializing system
	MsgID D_CMN_INIT_START = new MsgID("D_CMN_INIT_START", rb);
	// Initialize completed
	MsgID D_CMN_INIT_DONE = new MsgID("D_CMN_INIT_DONE", rb);

	// Unknown action
	MsgID E_CMN_UNKNOWN_ACTION = new MsgID("E_CMN_UNKNOWN_ACTION", rb);

	// Error cloning attributes
	MsgID E_CMN_CLONE_ATTRIBUTES = new MsgID("E_CMN_CLONE_ATTRIBUTES", rb);
	// Instantiation Exception
	MsgID E_CMN_CREATE_INSTANCE = new MsgID("E_CMN_CREATE_INSTANCE", rb);
	// Illegal Access Exception
	MsgID E_CMN_ILLEGAL_ACCESS = new MsgID("E_CMN_ILLEGAL_ACCESS", rb);
	// Class Not Found Exception
	MsgID E_CMN_CLASS_NOT_FOUND = new MsgID("E_CMN_CLASS_NOT_FOUND", rb);

	/**
	 * XML 
	 */
	// XML Directory was not set
	MsgID E_CMN_XML_DIR_NOT_SET = new MsgID("E_CMN_XML_DIR_NOT_SET", rb);
	// XML File was not set
	MsgID E_CMN_XML_FILE_NOT_SET = new MsgID("E_CMN_XML_FILE_NOT_SET", rb);
	// Error occurred writing XML to the given stream
	MsgID E_CMN_XML_WRITE = new MsgID("E_CMN_XML_WRITE", rb);
	// Error occurred parsing XML
	MsgID E_CMN_XML_PARSE = new MsgID("E_CMN_XML_PARSE", rb);

	/**
	 * Draw Engine 
	 */

	MsgID E_ENG_INIT_FAILED = new MsgID("E_ENG_INIT_FAILED", rb);
	
	/**
	 * Compartments 
	 */

	MsgID E_CMP_CREATE_FAILED = new MsgID("E_CMP_CREATE_FAILED", rb);
	
}
