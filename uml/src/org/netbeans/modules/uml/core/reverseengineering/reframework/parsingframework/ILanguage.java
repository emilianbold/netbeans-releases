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

import java.util.List;
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
   * 
   * @return The name of the language.
   */
  public String getName();

  /**
   * The name of the language that is represented by the ILanguage interface.
   * @param value The name of the language
   */
  public void setName( String value );

  /**
   * The data types that are supported by the language.
   * @return the data types
   */
  public ETList<ILanguageDataType> getDataTypes();

  /**
   * The data types that are supported by the language.
   * 
   * @param value the data types
  */
  public void setDataTypes( ETList<ILanguageDataType> value );

  /**
   * The syntax that defines the language.  A syntax is made up of a collection 
   * of tokens that together define the syntax.
   * 
   * @return The syntax object
   */
  public ILanguageSyntax getSyntax();

  /**
   * The syntax that defines the language.  A syntax is made up of a collection 
   * of tokens that together define the syntax.
   * 
  * @param value The syntax object
   */
  public void setSyntax( ILanguageSyntax value );

  /**
   * The code generation scripts that will generate source code for the langauge.
   * 
   * @return a collection of code generation scripts.
   * @depercated
   */
  public ETList<ICodeGenerationScript> getCodeGenerationScripts();

  /**
   * The code generation scripts that will generate source code for the langauge.
   * 
   * @param value A collection of code generation scripts
   * @depercated
   */
  public void setCodeGenerationScripts( ETList<ICodeGenerationScript> value );

  /**
   * Retrieve the ILanguageParser for the specified parser type.
   * @param Type The type of parser <i>examples: default, java5</i>
   * @return A parser can can be used to parse the language files.
   */
  public ILanguageParser getParser( String Type );

  /**
   * Add a CLSID that implements the ILangaugeParser interface.  The parser 
   * will be used to parser files written in the defined langauge.
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
  
  /**
   * Sets the languages collection types.
   * 
   * @param types the list of collection types.
   */
  public void setCollectionTypes(List < CollectionType > types);
  
  /**
   * Gets the languages collection types.
   * 
   * @return types the list of collection types.
   */
  public List < CollectionType > getCollectionTypes();
  
  /**
   * Checks if a type is a collection type.
   * 
   * @param typeName the type to check.
   * @return true if the type is a collection type.
   */
  public boolean isCollectionType(String typeName);
}
