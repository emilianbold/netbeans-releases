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

public interface ITokenDescriptor
{
	/**
	 * The line number that contains the token descriptor.
	*/
	public void setLine( int value );

	/**
	 * The line number that contains the token descriptor.
	*/
	public int getLine();

	/**
	 * The column that contains the token descriptor.
	*/
	public void setColumn( int value );

	/**
	 * The column that contains the token descriptor.
	*/
	public int getColumn();

	/**
	 * The stream position that contains the token descriptor.
	*/
	public void setPosition( long value );

	/**
	 * The stream position that contains the token descriptor.
	*/
	public long getPosition();

	/**
	 * The type of the token descriptor.
	*/
	public void setType( String value );

	/**
	 * The type of the token descriptor.
	*/
	public String getType();

	/**
	 * The value of the token descriptor.
	*/
	public void setValue( String value );

	/**
	 * The value of the token descriptor.
	*/
	public String getValue();

	/**
	 * The length of the token.
	*/
	public void setLength( int value );

	/**
	 * The length of the token.
	*/
	public int getLength();

	/**
	 * Adds a new property to the token descriptor.  Properties can be used to extend the information about the token.
	*/
	public void addProperty( String name, String value );

	/**
	 * 
	*/
	public String getProperty( String name );
}
