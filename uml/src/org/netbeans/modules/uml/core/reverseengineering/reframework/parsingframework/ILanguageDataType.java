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

package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;

public interface ILanguageDataType
{
	/**
	 * The name of the data type.
	*/
	public String getName();

	/**
	 * The name of the data type.
	*/
	public void setName( String value );

	/**
	 * The data type's kind.  The data kinds will usally be "primitive" or "user-defined".
	*/
	public int getKind();

	/**
	 * The data type's kind.  The data kinds will usally be "primitive" or "user-defined".
	*/
	public void setKind( /* DataTypeKind */ int value );

	/**
	 * The scope of the data type.  The value of scope will be "global" when the data type applies to all Describe projects, or a list of projects that applies to the data type.
	*/
	public String getScope();

	/**
	 * The scope of the data type.  The value of scope will be "global" when the data type applies to all Describe projects, or a list of projects that applies to the data type.
	*/
	public void setScope( String value );

	/**
	 * Specifies that the Data Type is the default value for attributes.
	*/
	public boolean getIsDefaultAttributeType();

	/**
	 * Specifies that the Data Type is the default value for attributes.
	*/
	public void setIsDefaultAttributeType( boolean value );

	/**
	 * Specifies that the Data Type is the default value for operations.
	*/
	public boolean getIsOperationDefaultType();

	/**
	 * Specifies that the Data Type is the default value for operations.
	*/
	public void setIsOperationDefaultType( boolean value );

	/**
	 * The default value of data type.
	*/
	public String getDefaultValue();

	/**
	 * The default value of data type.
	*/
	public void setDefaultValue( String value );

	/**
	 * The language independent name of the data type.  Only primitives have a UML name.
	*/
	public String getUMLName();

	/**
	 * The language independent name of the data type.  Only primitives have a UML name.
	*/
	public void setUMLName( String value );

}
