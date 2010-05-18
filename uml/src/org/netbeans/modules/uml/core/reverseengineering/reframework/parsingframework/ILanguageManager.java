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
