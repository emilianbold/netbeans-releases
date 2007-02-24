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


package org.netbeans.modules.uml.ui.support.archivesupport;

/**
 * @author Embarcadero Technologies Inc.
 *
 *
 */
public interface ETLPFormatSpec
{

	public static final String DEFAULT_EXTENSION = ".etlp";
	
	// ETLP Elements
	public static final String ELE_ENGINE = "engine";
	public static final String ELE_COMPARTMENT = "compartment";
	public static final String ELE_COMPARTMENT_NAME_TABLE = "CompartmentNameTable";
	public static final String ELE_COLORS_TABLE = "ColorsTable";
	public static final String ELE_FONTS_TABLE = "FontsTable";

	// ETLP Attributes
	public static final String ATT_NAME = "name";
	public static final String ATT_VALUE = "value";
	public static final String ATT_TABLE_INDEX = "TableIndex";

}
