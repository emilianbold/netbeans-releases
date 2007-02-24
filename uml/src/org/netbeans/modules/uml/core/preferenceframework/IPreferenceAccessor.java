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


package org.netbeans.modules.uml.core.preferenceframework;

public interface IPreferenceAccessor
{
	/**
	 * property DefaultProjectName
	*/
	public String getDefaultProjectName();

	/**
	 * property IDType
	*/
	public int getIDType();

	/**
	 * property DefaultElementName
	*/
	public String getDefaultElementName();

	/**
	 * property UnknownClassifierCreate
	*/
	public boolean getUnknownClassifierCreate();

	/**
	 * property UnknownClassifierType
	*/
	public String getUnknownClassifierType();

	/**
	 * property DefaultMode
	*/
	public String getDefaultMode();

	/**
	 * property DefaultLanguage
	*/
	public String getDefaultLanguage( String mode );

	/**
	 * property DefaultRoundTripBehavior
	*/
	public String getDefaultRoundTripBehavior( String lang, String behavior );

	/**
	 * property DefaultEditorCustomizationFile
	*/
	public String getDefaultEditorCustomizationFile();

	/**
	 * property DefaultEditorFilter
	*/
	public String getDefaultEditorFilter();

	/**
	 * property DefaultEditorSelect
	*/
	public int getDefaultEditorSelect();

	/**
	 * property ExpansionVariable
	*/
	public String getExpansionVariable( String Name );

	/**
	 * property FontName
	*/
	public String getFontName( String category );

	/**
	 * property FontSize
	*/
	public String getFontSize( String category );

	/**
	 * property FontBold
	*/
	public boolean getFontBold( String category );

	/**
	 * property FontItalic
	*/
	public boolean getFontItalic( String category );

	/**
	 * property FontStrikeout
	*/
	public boolean getFontStrikeout( String category );

	/**
	 * property FontUnderline
	*/
	public boolean getFontUnderline( String category );

	/**
	 * property FontColor
	*/
	public String getFontColor( String category );

}
