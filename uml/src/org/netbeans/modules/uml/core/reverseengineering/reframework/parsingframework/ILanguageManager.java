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

import java.util.Vector;

import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface ILanguageManager
{
	/**
	 * Retrieves the instantiation directive for the RequestProcessor associated with the passed in file.
	*/
	public String retrieveContextForFile( String Filename, String context );

	/**
	 * Sets / Gets the location of the language configuration file used by this manager.
	*/
	public String getConfigLocation();

	/**
	 * Sets / Gets the location of the language configuration file used by this manager.
	*/
	public void setConfigLocation( String value );

	/**
	 * Retrieves the instantiation directive for the RequestProcessor associated with the passed in language name.
	*/
	public String retrieveContextForLanguage( String language, String context );

	/**
	 * Retrieves the language defintion from the language manager.
	*/
	public ILanguage getLanguage( String langName );

	/**
	 * Retrieves a language parser for the specified langauge.
	*/
	public ILanguageParser retrieveParserForLanguage( String language, String Type );

	/**
	 * Retrieves a langauge parser that will parse the specified file.  The parser will selected based on the file extension.
	*/
	public ILanguageParser getParserForFile( String File, String Type );

	/**
	 * Retrieves the languages that are supported by the Language Manager.
	*/
	public IStrings getSupportedLanguages();

	/**
	 * Retrieves the language definition from the language manager.  The file name is used to determine the language defintion to retrieve.
	*/
	public ILanguage getLanguageForFile( String Filename );

	/**
	 * Retrieves the languages that are supported by the Language Manager
	*/
	public ETList<ILanguage> getSupportedLanguages2();

	/**
	 * Retrieves the Default data type for a new attribute.  If a data type is not defined as the default type then the default type will be retrieved from the UML language.
	*/
	public ILanguageDataType getAttributeDefaultType( Object pOwner );

	/**
	 * Retrieves the Default data type for a new operation.  If a data type is not defined as the default type then the default type will be retrieved from the UML language.
	*/
	public ILanguageDataType getOperationDefaultType( Object pOwner );

	/**
	 * Retrieves the default langauge for the project that contains the element.
	*/
	public ILanguage getDefaultLanguage( Object pElement );

	/**
	 * Retrieves a list of file extensions for the specified language.
	*/
	public IStrings getFileExtensionsForLanguage( String language );

	/**
	 * Retrieves a list of file extensions for the specified language.
	*/
	public IStrings getFileExtensionsForLanguage( ILanguage pLanguage );

	/**
	 * Retrieves the list of supported language.  The output will be a delemeted string.  The deleminter is '|'.
	*/
	public String getSupportedLanguagesAsString();

	/**
	 * Gets a Default setting
	*/
	public String getDefaultForLanguage( String language, String name );

	/**
	 * Returns the default source code file extension
	*/
	public String getDefaultSourceFileExtensionForLanguage( String language );

	/**
	 * Retrieves the languages that support Code Generation.
	*/
	public ETList<ILanguage> getLanguagesWithCodeGenSupport();

	/**
	 * Retrieves a collection of file filters for a specified language.
	*/
	public ETList<ILanguageFilter> getFileExtensionFilters( String languageName);

	/**
	 * Retrieves a collection of file filters for a specified language.
	*/
	public ETList<ILanguageFilter> getFileExtensionFilters( ILanguage language);

}
