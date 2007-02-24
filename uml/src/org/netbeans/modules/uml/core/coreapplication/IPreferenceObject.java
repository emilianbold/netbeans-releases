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


package org.netbeans.modules.uml.core.coreapplication;

public interface IPreferenceObject
{
	/**
	 * property Heading
	*/
	public String getHeading();

	/**
	 * property Heading
	*/
	public void setHeading( String value );

	/**
	 * property Name
	*/
	public String getName();

	/**
	 * property Name
	*/
	public void setName( String value );

	/**
	 * property DefaultValue
	*/
	public String getDefaultValue();

	/**
	 * property DefaultValue
	*/
	public void setDefaultValue( String value );

	/**
	 * property Key
	*/
	public String getKey();

	/**
	 * property Key
	*/
	public void setKey( String value );

	/**
	 * property Icon
	*/
	public String getIcon();

	/**
	 * property Icon
	*/
	public void setIcon( String value );

	/**
	 * property HelpText
	*/
	public String getHelpText();

	/**
	 * property HelpText
	*/
	public void setHelpText( String value );

	/**
	 * property DisplayName
	*/
	public String getDisplayName();

	/**
	 * property DisplayName
	*/
	public void setDisplayName( String value );

	/**
	 * property Values
	*/
	public String getValues();

	/**
	 * property Values
	*/
	public void setValues( String value );

	/**
	 * Whether or not this preference is an advanced preference
	*/
	public boolean getAdvanced();

	/**
	 * Whether or not this preference is an advanced preference
	*/
	public void setAdvanced( boolean value );

	/**
	 * 
	*/
	public String getProgID();

	/**
	 * 
	*/
	public void setProgID( String value );

	/**
	 * 
	*/
	public String getTranslator();

	/**
	 * 
	*/
	public void setTranslator( String value );

}
