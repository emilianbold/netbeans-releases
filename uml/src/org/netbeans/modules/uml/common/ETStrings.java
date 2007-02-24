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

package org.netbeans.modules.uml.common;

/**
 * @author Embarcadero Technologies Inc.
 *
 *
 */
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
