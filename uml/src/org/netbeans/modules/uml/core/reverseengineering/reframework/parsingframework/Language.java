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

//			   The ILanguage interface allows easy access to the data that
//			   define a language.
//*****************************************************************************

package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;

import java.util.Enumeration;
import java.util.Hashtable;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.generativeframework.IExpansionVariable;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IDataTypeKind;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ITokenKind;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.Strings;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.openide.cookies.InstanceCookie;

/**
 * @author sumitabhk
 *
 */
public class Language implements ILanguage
{
	private String m_Name = "";
	private ETList<ILanguageDataType> m_DataTypes = null;
	private ILanguageSyntax m_Syntax = null;
	private ETList<ICodeGenerationScript> m_Scripts = null;
	private Hashtable<String, String> m_ParserMap = null;;
	private Hashtable<String, String> m_ContextMap = null;
	private ILanguageDataType m_AttributeDefaultType = null;
	private ILanguageDataType m_OperationDefaultType = null;
	private Hashtable<String, String> m_LibraryMap = 
	                        new Hashtable<String,String>();
	private Node m_FormatDefinition = null;   
	private ETList<IExpansionVariable> m_OverrideVariables = null; //ExpansionVariables is the type here
	private Hashtable<String, String> m_Defaults  = 
                            new Hashtable<String,String>();

	private Hashtable<String, Boolean> m_SupportedFeatures = null;


	/**
	 * Gets the name of the language that is represented by the ILanguage interface.
	 * 
	 * @param pVal [out] The name of the language
	 */
	public String getName()
	{
		return m_Name;
	}

	/**
	 * Sets the name of the language that is represented by the ILanguage interface.
	 * 
	 * @param newVal [int] The name of the language
	 */
	public void setName(String newVal)
	{
		m_Name = newVal;
	}

	/** 
	 * Retrieves the data types that are supported by the language.
	 * 
	 * @param pVal [out] The datatypes
	 */
	public ETList<ILanguageDataType> getDataTypes()
	{
		return m_DataTypes;
	}

	/** 
	 * Sets the data types that are supported by the language.
	 * 
	 * @param newVal [int] The datatypes
	 */
	public void setDataTypes(ETList<ILanguageDataType> newVal)
	{
		m_DataTypes = newVal;
	}

	/**
	 * Gets the syntax that defines the language.  A syntax is made up of a 
	 * collection of tokens that together define the syntax.
	 * 
	 * @param pVal [out] The lanauage syntax.
	 */
	public ILanguageSyntax getSyntax()
	{
		return m_Syntax;
	}

	/**
	 * Sets the syntax that defines the language.  A syntax is made up of a 
	 * collection of tokens that together define the syntax.
	 * 
	 * @param pVal [in] The lanauage syntax
	 */
	public void setSyntax(ILanguageSyntax newVal)
	{
		m_Syntax = newVal;
	}

	/**
	 * Gets the code generation scripts that will generate source code for the langauge.
	 *
	 * @param pVal [out] The scripts
	 */
	public ETList<ICodeGenerationScript> getCodeGenerationScripts()
	{
		return m_Scripts;
	}

	/**
	 * Sets the code generation scripts that will generate source code for the langauge.
	 *
	 * @param newVal [in] The scripts
	 */
	public void setCodeGenerationScripts(ETList<ICodeGenerationScript> newVal)
	{
		m_Scripts = newVal;
	}

	/**
	 * Retrieve the ILanguageParser for the specified parser type.
	 *
	 * @param type [in] The parser type
	 * @param pVal [out] The ILanguageParser
	 */
	public ILanguageParser getParser(String type)
	{
		ILanguageParser retVal = (ILanguageParser)getInstanceFromRegistry("parsers/uml/java");
      
      if(retVal == null)
      {
         String clsid = getParserCLSID(type);
         if (clsid != null && clsid.length() > 0)
         {
               try
               {
                   retVal = (ILanguageParser) Class.forName(clsid).newInstance();
               }
               catch (Exception e)
               {
                   e.printStackTrace();
               }
         }
      }
		
		return retVal;
	}

   /**
    * The registry information that is retrieved from layer files to build 
    * the list of actions supported by this node.
    *
    * @param path The registry path that is used for the lookup.
    * @return The list of actions in the path.  null will be used if when 
    *         seperators can be placed.   
    */
   protected Object getInstanceFromRegistry(String path)
   {
      Object retVal = null;
      org.openide.filesystems.FileSystem system = org.openide.filesystems.Repository.getDefault().getDefaultFileSystem();
      try
      {
         if(system != null)
         {
            org.openide.filesystems.FileObject lookupDir = system.findResource(path);
            if(lookupDir != null)
            {
               org.openide.filesystems.FileObject[] children = lookupDir.getChildren();
               for(org.openide.filesystems.FileObject curObj : children)
               {
                  try
                  {
                     org.openide.loaders.DataObject dObj = org.openide.loaders.DataObject.find(curObj);
                     if(dObj != null)
                     {
                        org.openide.cookies.InstanceCookie cookie = (org.openide.cookies.InstanceCookie)dObj.getCookie(org.openide.cookies.InstanceCookie.class);
                        
                        if(cookie != null)
                        {
                           Object obj = cookie.instanceCreate();
                           if(obj instanceof IParserFactory)
                           {
                              IParserFactory factory = (IParserFactory)obj;
                              retVal = factory.createParser();
                           }
//                           retVal = obj;
//                           if(obj instanceof Action)
//                           {
//                              actions.add((Action)obj);
//                           }
//                           else if(obj instanceof javax.swing.JSeparator)
//                           {
//                              actions.add(null);
//                           }
                        }
                     }
                  }
                  catch(ClassNotFoundException e)
                  {
                     // Unable to create the instance for some reason.  So the 
                     // do not worry about adding the instance to the list.
                  }
               }
            }         
         }
      }
      catch(org.openide.loaders.DataObjectNotFoundException e)
      {
         // Basically Bail at this time.
      }
      catch(java.io.IOException ioE)
      {
         
      }      
      
      return retVal;
   }
   
	/**
	 * Add a CLSID that implements the ILangaugeParser interface.  The parser
	 * will be used to parse files written in the defined langauge.
	 *
	 * @param type [in] The parser type
	 * @param clsid [in] The CLSID that implements the ILangaugeParser interface
	 */
	public void addParser(String type, String clsid)
	{
		if (m_ParserMap == null) m_ParserMap = new Hashtable<String, String>();
		m_ParserMap.put(type, clsid);
	}

	/**
	 * Retrieves the CLSID of the parser for the specified parser type.
	 * 
	 * @param type [in] The parser type
	 * @param pVal [out] The CLSID that implements the ILanguageParser interface
	 */
	public String getParserCLSID(String type)
	{
		String retVal = null;
		if (m_ParserMap != null)
		{
            retVal = m_ParserMap.get(type);
		}
		return retVal;
	}

	/**
	 * Adds a new context to the the language.  Contexts are used by other
	 * components to perform langauge specific operations.  The interface 
	 * for each context is defined by the component.
	 *
	 * @param name [in] The name of the component that defined the context
	 * @param clsid [in] The CLSID that implements the context
	 */
	public void addContext(String name, String clsid)
	{
		if (m_ContextMap == null) m_ContextMap = new Hashtable<String, String>();
		
		m_ContextMap.put(name, clsid);
	}

	/**
	 * Retrieves the CLSID for a context. Contexts are used by other components 
	 * to perform langauge specific operations.  The interface for each context is
	 * defined by the component.
	 *
	 * @param context [in] The name of the component that defined the context
	 * @param pVal [out] The CLSID that implements the context
	 */
	public String getContextCLSID(String context)
	{
		String retVal = null;
		if (m_ContextMap != null)
		{
			Object obj = m_ContextMap.get(context);
			if (obj != null)
			{
				retVal = (String)obj;
			}
		}
		return retVal;
	}

	/**
	 * Retrieves the type used when creating a new attribute.
	 *
	 * @param pVal [out] The default data type for new attributes
	 */
	public ILanguageDataType getAttributeDefaultType()
	{
		return m_AttributeDefaultType;
	}

	/**
	 * Sets the type used when creating a new attribute.
	 *
	 * @param newVal [in] The default data type for new attributes
	 */
	public void setAttributeDefaultType(ILanguageDataType newVal)
	{
		m_AttributeDefaultType = newVal;
	}

	/**
	 * Retrieves the type used when creating a new operation.
	 * @param pVal [out] The default data type for new operations
	 */
	public ILanguageDataType getOperationDefaultType()
	{
		return m_OperationDefaultType;
	}

	/**
	 * Sets the type used when creating a new operation.
	 * @param newVal [in] The default data type for new operations
	 */
	public void setOperationDefaultType(ILanguageDataType newVal)
	{
		m_OperationDefaultType = newVal;
	}

	/**
	 * Is the given string the name of a predefined date type? 
	 * These data types are defined in the Languages.etc file.
	 *
	 * @param sType[in] The type name
	 * @param pVal[out] true if the type appears as a data type in the etc file
	 */
	public boolean isDataType(String sType)
	{
		boolean retVal = false;
		
		ILanguageDataType pTemp = getDataType(sType);
		if (pTemp != null)
		{
			retVal = true;
		}
		
		return retVal;
	}

	/**
	 * Is the given string the name of a primitve date type?
	 * These data types are defined in the Languages.etc file.
	 *
	 * @param sType[in] The type name
	 * @param pVal[out] true if the type appears as a primitive type in the etc file 
	 */
	public boolean isPrimitive(String sType)
	{
		boolean retVal = false;
		
		ILanguageDataType pTemp = getDataType(sType);
		if (pTemp != null)
		{
			int kind = pTemp.getKind();
			if (kind == IDataTypeKind.PRIMITIVE)
			{
				retVal = true;
			}
		}
		
		return retVal;
	}

	/**
	 * Is the given string a reserved word in this language?
	 * These reserved tokens are defined in the Languages.etc file.
	 *
	 * @param sWord[in] The word to look for
	 * @param pVal[out] true if the word appears as a keyword in the etc file
	 */
	public boolean isKeyword(String sWord)
	{
		boolean retVal = false;
		
		ISyntaxToken pTemp = getSyntaxToken(sWord);
		if (pTemp != null)
		{
			int kind = pTemp.getKind();
			if (kind == ITokenKind.KEYWORD)
			{
				retVal = true;
			}
		}
		
		return retVal;
	}

	/**
	 * Get the data type by its name.
	 *
	 * @param sName[in] The datatype name to look for
	 * @param pVal[out] The data type if found
	 */
	public ILanguageDataType getDataType(String sName)
	{
		ILanguageDataType retVal = null;
		if (m_DataTypes != null)
		{
			int count = m_DataTypes.size();
			for (int i=0; i<count; i++)
			{
				ILanguageDataType pDataType = (ILanguageDataType)m_DataTypes.get(i);
				String name = pDataType.getName();
				if (name.equals(sName))
				{
					retVal = pDataType;
					break;
				}
			}
		}
		return retVal;
	}

	/**
	 * Get the token by its name.
	 *
	 * @param sName[in] The token name to look for
	 * @param pVal[out] The token if found
	 */
	public ISyntaxToken getSyntaxToken(String sName)
	{
		ISyntaxToken retVal = null;
		
		if (m_Syntax != null)
		{
			ETList<ISyntaxToken> tokens = m_Syntax.getSyntaxTokens();
			if (tokens != null)
			{
				int count = tokens.size();
				for (int i=0; i<count; i++)
				{
					ISyntaxToken pToken = (ISyntaxToken)tokens.get(i);
					String name = pToken.getName();
					if (name.equals(sName))
					{
						retVal = pToken;
						break;
					}
				}
			}
		}
		
		return retVal;
	}

	/**
	 * Adds a new library to the language definition.
	 *
	 * @param name [in] The name of the library
	 * @param *definitionFile [out] The path to the definition file.
	 */
	public void addLibrary(String name, String definitionFile)
	{
		if (m_LibraryMap == null) m_LibraryMap = new Hashtable<String, String>();
		m_LibraryMap.put(name, definitionFile);
	}

	/**
	 * Retrieve a library definition from the language definition.  If 
	 * the language definition is not registered with the language 
	 * definition then an empty string is returned.
	 *
	 * @param name [in] The name of the library.
	 * @param *definitionFile [out] The path to the library definition file.
	 */
	public String getLibraryDefinition(String name)
	{
		String retVal = "";
		if (m_LibraryMap != null)
		{
			Object obj = m_LibraryMap.get(name);
			if (obj != null)
			{
				retVal = (String)obj;
			}
		}
		return retVal;
	}

	/**
	 * Retrieve all of the libraries that are registered with the 
	 * langauge definition.
	 *
	 * @param **names [in] The name of the library.
	 */
	public IStrings getLibraryNames()
	{
		IStrings retVal = new Strings();
		if (m_LibraryMap != null)
		{
			Enumeration iter = m_LibraryMap.keys();
			while (iter.hasMoreElements())
			{
				String libName = (String)iter.nextElement();
				if (libName != null && libName.length() > 0)
				{
					retVal.add(libName);
				}
			}
		}
		return retVal;
	}

	/**
	 * Sets the DOM node that specifies how to format a
	 * model elments data.  The content of the DOM node allow
	 * Describe to display Model Element in a language specific
	 * mannor.
	 *
	 * @param **pVal [in] The format definition.
	 */
	public void setFormatDefinitions(Node newVal)
	{
		m_FormatDefinition = newVal;
	}

	/**
	 * Retrieve the DOM node that specifies how to format a
	 * model elements data.  The content of the DOM node allow
	 * Describe to display Model Element in a language specific
	 * mannor.
	 *
	 * @param elementName [in] The name of the element.
	 * @param pVal [out] The format definition.
	 */
	public Node getFormatDefinition(String elementName)
	{
		Node retVal = null;
		if (m_FormatDefinition != null)
		{
			String xpath = "PropertyDefinition[@name=\"";
			xpath += elementName;
			xpath += "\"]";
      
			retVal = m_FormatDefinition.selectSingleNode(xpath);
		}
		return retVal;
	}

	/**
	 *
	 * Retrieves the collection of ExpansionVariables that are used to override
	 * the default variables for this Language
	 *
	 * @param pVal[out] The collection, in an IDispatch form
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<IExpansionVariable> getExpansionVariables()
	{
		return m_OverrideVariables;
	}

	/**
	 * @see get_ExpansionVariables()
	 */
	public void setExpansionVariables(ETList<IExpansionVariable> newVal)
	{
		m_OverrideVariables = newVal;
	}

	/** 
	 * Returns a default setting
	 * 
	 * @param name[in] name of the default setting
	 * @param value[out] its value
	 */
	public String getDefault(String name)
	{
		String retVal = null;
		if (m_Defaults != null)
		{
            retVal = m_Defaults.get(name);
		}
		return retVal;
	}

	/** 
	 * Creates/Sets a default setting named @name and with a value of @value
	 * 
	 * @param name[in] the name of the default setting
	 * @param value[in] the setting's value
	 */
	public void setDefault(String name, String value)
	{
		if (m_Defaults != null)
		{
			String obj = m_Defaults.get(name);
			//need to add this new value
			m_Defaults.put(name, value);
		}
	}

	/** 
	 * Returns the default source file extension for a language.
	 * 
	 * @param extension[out] the default extension
	 */
	public String getDefaultSourceFileExtension()
	{
		String retVal = "";
		retVal = getDefault("extension");
		return retVal;
	}

	public boolean isFeatureSupported(String name)
	{
		boolean retVal = false;
		if (m_SupportedFeatures != null)
		{
            Boolean bool = m_SupportedFeatures.get(name);
			if (bool != null)
			{
				retVal = bool.booleanValue();
			}
		}
		return retVal;
	}

	public void setFeatureSupported(String name, boolean newVal)
	{
		if (m_SupportedFeatures == null) m_SupportedFeatures = new Hashtable<String, Boolean>();
        m_SupportedFeatures.put(name, Boolean.valueOf(newVal));
	}

	public String getFormatStringFile(String name)
	{
		String str = "";
		// m_FormatDefinition is actually at our level, so we need to go back one
		// it is FormatDefinitions/PropertyDefinitions
		if(m_FormatDefinition != null)
		{
			String xpath = "parent::node()/FormatDefinition[@name=\"";
			xpath += name;
			xpath += "\"]";
			Node pNode = m_FormatDefinition.selectSingleNode(xpath);
			if (pNode != null)
			{
				if (pNode instanceof org.dom4j.Element)
				{
					org.dom4j.Element pEle = (org.dom4j.Element)pNode;
					if (pEle != null)
					{
						str = pEle.attributeValue("file");
					}
				}
		    }
		}   
		return str;
	}
  
	public ILanguageManager getLanguageManager()
	{
		return null;
	}
  
	public void setLanguageManager(ILanguageManager pManager)
	{
	}

}


