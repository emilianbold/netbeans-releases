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

import org.dom4j.Node;

import org.netbeans.modules.uml.core.generativeframework.IExpansionVariable;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public interface ILanguage {

  /**
   * The name of the language that is represented by the ILanguage interface.
  */
  public String getName();

  /**
   * The name of the language that is represented by the ILanguage interface.
  */
  public void setName( String value );

  /**
   * The data types that are supported by the language.
  */
  public ETList<ILanguageDataType> getDataTypes();

  /**
   * The data types that are supported by the language.
  */
  public void setDataTypes( ETList<ILanguageDataType> value );

  /**
   * The syntax that defines the language.  A syntax is made up of a collection of tokens that together define the syntax.
  */
  public ILanguageSyntax getSyntax();

  /**
   * The syntax that defines the language.  A syntax is made up of a collection of tokens that together define the syntax.
  */
  public void setSyntax( ILanguageSyntax value );

  /**
   * The code generation scripts that will generate source code for the langauge.
  */
  public ETList<ICodeGenerationScript> getCodeGenerationScripts();

  /**
   * The code generation scripts that will generate source code for the langauge.
  */
  public void setCodeGenerationScripts( ETList<ICodeGenerationScript> value );

  /**
   * Retrieve the ILanguageParser for the specified parser type.
  */
  public ILanguageParser getParser( String Type );

  /**
   * Add a CLSID that implements the ILangaugeParser interface.  The parser will be used to parser files written in the defined langauge.
  */
  public void addParser( String Type, String clsid );

  /**
   * Retrieves the CLSID of the parser for the specified parser type.
  */
  public String getParserCLSID( String Type );

  /**
   * Adds a new context to the the language.  Contexts are used by other components to perform langauge specific operations.  The interface for each context is defined by the component.
  */
  public void addContext( String name, String clsid );

  /**
   * Retrieves the CLSID for a context.    Contexts are used by other components to perform langauge specific operations.  The interface for each context is defined by the component.
  */
  public String getContextCLSID( String context );

  /**
   * The type used when creating a new attribute.
  */
  public ILanguageDataType getAttributeDefaultType();

  /**
   * The type used when creating a new attribute.
  */
  public void setAttributeDefaultType( ILanguageDataType value );

  /**
   * The return type to use when creating a new operation.
  */
  public ILanguageDataType getOperationDefaultType();

  /**
   * The return type to use when creating a new operation.
  */
  public void setOperationDefaultType( ILanguageDataType value );

  /**
   * Is the given string the name of a predefined date type? These data types are defined in the Languages.etc file.
  */
  public boolean isDataType( String sType );

  /**
   * Is the given string the name of a primitve date type? These data types are defined in the Languages.etc file.
  */
  public boolean isPrimitive( String sType );

  /**
   * Is the given string a reserved word in this language? These reserved tokens are defined in the Languages.etc file.
  */
  public boolean isKeyword( String sWord );

  /**
   * Get the data type by its name.
  */
  public ILanguageDataType getDataType( String sName );

  /**
   * Get the token by its name.
  */
  public ISyntaxToken getSyntaxToken( String sName );

  /**
   * Adds a new library to the language definition.
  */
  public void addLibrary( String name, String definitionFile );

  /**
   * Retrieve a library definition from the language definition.  If the language definition is not registered with the language definition then an empty string is returned.
  */
  public String getLibraryDefinition( String name );

  /**
   * Retrieve all of the libraries that are registered with the langauge definition.
  */
  public IStrings getLibraryNames();

  /**
   * Specifies the DOM node that specifies how to format a model elments data.
  */
  public void setFormatDefinitions( Node value );

  /**
   * Specifies the DOM node that specifies how to format a model elments data.
  */
  public Node getFormatDefinition( String elementName );

  /**
   * The override expansion variables associated with this Language. ExpansionVariables is the collection type.
  */
  public ETList<IExpansionVariable> getExpansionVariables();

  /**
   * The override expansion variables associated with this Language. ExpansionVariables is the collection type.
  */
  public void setExpansionVariables( ETList<IExpansionVariable> value );

  /**
   * Gets a Default setting
  */
  public String getDefault( String name );

  /**
   * Sets a Default setting
  */
  public void setDefault( String name, String Value );

  /**
   * Returns the default source code file extension
  */
  public String getDefaultSourceFileExtension();

  /**
   * Specifies a specific feature is supported.
  */
  public boolean isFeatureSupported( String name );

  /**
   * Turns on or off a supported feature.
  */
  public void setFeatureSupported( String name, boolean newVal );

  public String getFormatStringFile(String name);
  
  public ILanguageManager getLanguageManager();
  
  public void setLanguageManager(ILanguageManager pManager);
}