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

/*
 * Created on Apr 22, 2003
 *
 */
package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.Node;


import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.generativeframework.IExpansionVariable;
import org.netbeans.modules.uml.core.generativeframework.ITemplateManager;
import org.netbeans.modules.uml.core.generativeframework.IVariableFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.reverseengineering.reframework.IDataTypeKind;
import org.netbeans.modules.uml.core.reverseengineering.reframework.ITokenKind;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.Strings;
import org.netbeans.modules.uml.core.support.umlsupport.URILocator;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.openide.modules.InstalledFileLocator;

/**
 * @author sumitabhk
 *
 */
public class LanguageManager implements ILanguageManager
{
	private String m_ConfigLocation = "";
	
	//<LangName, ILanguage>
	private Hashtable<String, ILanguage> m_LanguageMap = new Hashtable<String, ILanguage>();
	private IStrings m_LanguageNames = null;
	
	//<LangName, Extension>
	private Hashtable<String, String> m_ExtensionMap = new Hashtable<String, String>();
	
	//<LangName, Vector<ILangaugeFilter>>
	private Hashtable<String, ETList<ILanguageFilter>> m_FilterMap = new Hashtable();

	/**
	 * 
	 */
	public LanguageManager() {
		initializeManager();
	}

	/**
	 *
	 * Retrieves the RequestProcessor progid associated with the given file.
	 *
	 * @param fileName[in] A file path. Can be relative or absolute
	 * @param procID[out] The ProgID of the appropriate RequestProcessor
	 *
	 * @return HRESULT
	 *
	 */
	public String retrieveContextForFile(String filename, String context) 
	{
		String retContext = "";
		try
		{
			String lang = getLanguageNameForFile(filename);
			if (lang != null && lang.length() > 0)
			{
				retContext = retrieveContextForLanguage(lang, context);
			}
		}
		catch (Exception e)
		{
		}
		return retContext;
	}

	/**
	 *
	 * Retrieves the current, absolute path to the Languages.etc
	 * configuration file.
	 *
	 * @param pVal [out] The current value
	 *
	 * @return HRESULT
	 *
	 */
	public String getConfigLocation() 
	{
		String retLoc = "";
		try
		{
			if (m_ConfigLocation.length() == 0)
			{
				m_ConfigLocation = getConfigHome();
			}
			
			retLoc = m_ConfigLocation;
		}
		catch (Exception e)
		{
		}
		return retLoc;
	}

	/**
	 *
	 * Sets the location of the configuration file to use. This
	 * must be an absolute path.
	 *
	 * @param newVal[in] Absolute path to the config file
	 *
	 * @return HRESULT
	 *
	 */
	public void setConfigLocation(String newVal) 
	{
		m_ExtensionMap.clear();
		m_LanguageMap.clear();
		m_LanguageNames = null;
		m_ConfigLocation = newVal;

		initializeManager();
	}

	/**
	 *
	 * Retrieves the instantiation directive for the RequestProcessor 
	 * associated with the passed-in language name.
	 *
	 * @param language[in] The name of the language
	 * @param procID[out] The RequestProcessor associated with that language
	 *
	 * @return HRESULT
	 *
	 */
	public String retrieveContextForLanguage(String language, String context) 
	{
		String retContext = null;
		try
		{
			ILanguage lang = getLanguage(language);
			if (lang != null)
			{
				retContext = lang.getContextCLSID(context);
			}
		}
		catch(Exception e)
		{
		}
		return retContext;
	}

	/** 
	 * Retrieves the language defintion from the language manager.
	 * 
	 * @param langName [in] The name of the language
	 * @param pVal [out] The language definition
	 * @return E_INVALID_ARG if pVal = NULL, IDS_E_LANG_CONFIG_MISSING if langName
	 *         is not found in the language config file.
	 */
	public ILanguage getLanguage(String langName) 
	{
		ILanguage retLang = null;
		try
		{
			// Check if I have already retrieve the ILanguage data from the 
			// configuration file.  If I have not already retrieved the langugae
			// information the retrieve it of course.
			if (m_LanguageMap.size() > 0)
			{
				if (m_LanguageMap.containsKey(langName))
				{
					retLang = m_LanguageMap.get(langName);
				}
				else
				{
					retLang = retrieveLanguage(langName);
				}
			}
			else
			{
				retLang = retrieveLanguage(langName);
			}
			
			// If I was not able to find the language information in the configuration 
			// file then the error LM_E_INVALID_LANG_NAME must reported to the caller.
			if (retLang == null)
			{
				Log.out("LM_E_INVALID_LANG_NAME");
			}
		}
		catch (Exception e)
		{
		}
		return retLang;
	}

	/**
	 * Retrieves a language parser for the specified language.  A language can support
	 * more than one way to parse a stream.  Examples of different parses
	 * are a complete parser, a parser that retrieves classes only, and a parser that
	 * only retrieves package and class defintions (no attribute or operatons).
	 * 
	 * @param language [in] The language
	 * @param type [in] The parser type 
	 * @param pVal [out] The parser
	 */
	public ILanguageParser retrieveParserForLanguage(String language, String type) 
	{
		ILanguageParser retParser = null;
		ILanguage lang = getLanguage(language);
		if (lang != null)
		{
			retParser = lang.getParser(type);
		}
		return retParser;
	}

	/**
	 * Retrieves a langauge parser that will parse the specified file.  The parser will be 
	 * selected based on the file extension.  A language can support more than one way
	 * to parse a stream.  Examples of different parses are a complete parser,
	 * a parser that retrieves classes only, and a parser that only retrieves package 
	 * and class defintions (no attribute or operations).
	 * 
	 * @param file [in] The file name
	 * @param type [in] The parser type
	 * @param pVal [out] The parser
	 */
	public ILanguageParser getParserForFile(String file, String type) 
	{
		ILanguageParser retParser = null;
		String lang = getLanguageNameForFile(file);
		if (lang != null && lang.length() > 0)
		{
			retParser = retrieveParserForLanguage(lang, type);
		}
		return retParser;
	}

	/**
	 * Retrieves the name of the languages that are supported by the Language Manager.
	 * 
	 * @param pVal [out] The list of language names
	 */ 
	public IStrings getSupportedLanguages() 
	{
		return m_LanguageNames;
	}

	/**
	 * Retrieves the language definition from the language manager.  The file name is 
	 * used to determine the language defintion to retrieve.
	 * 
	 * @param filename [in] The name of the file
	 * @param pVal [out] The language definition
	 */
	public ILanguage getLanguageForFile(String filename) 
	{
		ILanguage retLang = null;
		String lang = getLanguageNameForFile(filename);
		if (lang != null && lang.length() > 0)
		{
			retLang = getLanguage(lang);
		}
		else
		{
			Log.out("LM_E_INVALID_LANG_NAME");
		}
		return retLang;
	}

	/**
	 * Retrieves the languages that are supported by the Language Manager.
	 * 
	 * @param pVal [out] The collection of Language objects
	 */ 
	public ETList<ILanguage> getSupportedLanguages2() 
	{
		ETList<ILanguage> retVal = null;
		IStrings langs = getSupportedLanguages();
		if (langs != null)
		{
			int count = langs.getCount();
			retVal = new ETArrayList<ILanguage>();
			for (int i=0; i<count; i++)
			{
				String langName = langs.item(i);
				ILanguage language = getLanguage(langName);
				if (language != null)
				{
					retVal.add(language);
				}
			}
		}
		return retVal;
	}

	/**
	 * Retrieves the default data type for a new attribute.  If a data type
	 * is not defined as the default type then the default type will be 
	 * retrieved from the UML language.
	 * 
	 * @param pOwner [in] The element that contains the attribute
	 * @param pType [out] The data type
	 */
	public ILanguageDataType getAttributeDefaultType(Object pOwner) 
	{
		ILanguageDataType retType = null;
		if (pOwner != null && pOwner instanceof IElement)
		{
			IElement pElement = (IElement)pOwner;

			// Retrieve the default type from the language manager.
			ETList<ILanguage> pLanguages = pElement.getLanguages();
			if (pLanguages != null)
			{
				int count = pLanguages.size();
				for (int i=0; i<count && (retType == null); i++)
				{
					ILanguage lang = (ILanguage)pLanguages.get(i);
					retType = getAttributeDefaultType(lang);
				}
			}
			else
			{
				// Since no languages were defined for the element I must use the default 
				// language.
				ILanguage defLang = getDefaultLanguage(pElement);
				if (defLang != null)
				{
					retType = getAttributeDefaultType(defLang);
				}
			}
			
			// If I have not been able to find a default attribute type check the UML 
			// language.  If I can not find a default type in the UML language the 
			// user has edited the configuration file and screwed themselves.
			if (retType == null)
			{
				ILanguage pUMLLang = getLanguage("UML");
				if (pUMLLang != null)
				{
					retType = getAttributeDefaultType(pUMLLang);
				}
			}
		}
		return retType;
	}

	/**
	 * Retrieves an attributes default data type for a specified language.
	 *
	 * @param pLanguage [in] The language that specifies the default data type
	 * @param pType [out] The data type
	 */
	private ILanguageDataType getAttributeDefaultType(ILanguage pLanguage)
	{
		ILanguageDataType pDataType = pLanguage.getAttributeDefaultType();
		return pDataType;
	}

	/**
	 * Retrieves the default data type for a new operation.  If a data type is 
	 * not defined as the default type then the default type will be retrieved
	 * from the UML language.
	 * 
	 * @param pOwner [in] The element that contains the operation
	 * @param pType [out] The data type
	 */
	public ILanguageDataType getOperationDefaultType(Object pOwner) 
	{
		ILanguageDataType retType = null;
		if (pOwner != null && pOwner instanceof IElement)
		{
			IElement pElement = (IElement)pOwner;

			// Retrieve the default type from the language manager.
			ETList<ILanguage> pLangs = pElement.getLanguages();
			if (pLangs != null)
			{
				int count = pLangs.size();
				for (int i=0; i<count && (retType == null); i++)
				{
					ILanguage lang = (ILanguage)pLangs.get(i);
					retType = getOperationDefaultType(lang);
				}
			}
			else
			{
				// Since no languages were defined for the element I must use the default 
				// language.
				ILanguage defLang = getDefaultLanguage(pElement);
				if (defLang != null)
				{
					retType = getOperationDefaultType(defLang);
				}
			}

			// If I have not been able to find a default attribute type check the UML 
			// language.  If I can not find a default type in the UML language the 
			// user has edited the configuration file and screwed themselves.
			if (retType == null)
			{
				ILanguage pUMLLang = getLanguage("UML");
				if (pUMLLang != null)
				{
					retType = getOperationDefaultType(pUMLLang);
				}
			}
		}
		return retType;
	}

	/**
	 * Retrieves an operation default data type for a specified language.
	 *
	 * @param pLanguage [in] The language that specifies the default data type
	 * @param pType [out] The data type
	 */
	private ILanguageDataType getOperationDefaultType(ILanguage pLanguage )
	{
		ILanguageDataType pDataType = pLanguage.getOperationDefaultType();
		return pDataType;
	}

	/**
	 * Retrieves the default langauge information for a specified element.
	 * The name of the default language will be retrieved from the project that 
	 * contains a specified element.
	 *
	 * @param pElement [in] Must implement the IElement interface
	 * @param pLanguage [out] The default langauge
	 */
	public ILanguage getDefaultLanguage(Object pElement) 
	{
		ILanguage retLang = null;
		if (pElement != null && pElement instanceof IElement)
		{
			IElement pElem = (IElement)pElement;
			IProject proj = pElem.getProject();
			if (proj != null)
			{
				String langName = proj.getDefaultLanguage();
				retLang = getLanguage(langName);
			}
		}
		return retLang;
	}

	/** 
	 * Returns a list of file extensions for the specified language
	 * 
	 * @param language[in] the language you want file extensions for
	 * @param pVal[out] list of languages
	 * 
	 * @return HRESULT
	 */
	public IStrings getFileExtensionsForLanguage(String language) 
	{
		IStrings retVal = new Strings();
		
		// m_ExtensionMap maps extensions to languages.
		// For all entries in m_ExtensionMap where language == @a language,
		// add that language to the list of strings.
		Enumeration<String> iter = m_ExtensionMap.elements();
		Enumeration<String> iter2 = m_ExtensionMap.keys();
		while (iter.hasMoreElements())
		{
			String extension = iter2.nextElement();
			String extLang = iter.nextElement();
			if (language.equals(extLang))
			{
				retVal.add(extension);
			}
		}
		
		return retVal;
	}

	/** 
	 * Returns a list of file extensions for the specified language
	 * 
	 * @param language[in] the language you want file extensions for
	 * @param pVal[out] list of languages
	 * 
	 * @return HRESULT
	 */
	public IStrings getFileExtensionsForLanguage(ILanguage pLanguage) 
	{
		IStrings retVal = null;
		if (pLanguage != null)
		{
			String langName = pLanguage.getName();
			retVal = getFileExtensionsForLanguage(langName);
		}
		return retVal;
	}

	/**
	 * Retrieves the list of supported language.  The output will 
	 * be a delemeted string.  The deleminter is '|'.
	 *
	 * @param pVal [out] The list of language names.
	 */
	public String getSupportedLanguagesAsString() 
	{
		String retLang = "";
		IStrings langNames = getSupportedLanguages();
		if (langNames != null)
		{
			int count = langNames.getCount();
			String separator = "|";
			for (int i=0; i<count; i++)
			{
				String curName = langNames.item(i);
				if (curName.length() > 0)
				{
					if (retLang.length() > 0)
					{
						retLang += separator;
					}
					retLang += curName;
				}
			}
		}
		return retLang;
	}

	/** 
	 * returns a "default" setting for the language
	 * 
	 * @param language[in] the name of the language to get the default for
	 * @param name[in] the name of the setting
	 * @param value[out] the value of the setting
	 */
	public String getDefaultForLanguage(String language, String name) 
	{
		String retVal = "";
		ILanguage pLang = getLanguage(language);
		if (pLang != null)
		{
			retVal = pLang.getDefault(name);
		}
		return retVal;
	}

	/** 
	 * Returns the default extension for a source file for a particular language
	 * 
	 * @param language[in] the language
	 * @param extension[out] the default source file extension
	 */
	public String getDefaultSourceFileExtensionForLanguage(String language) 
	{
		String retVal = "";
		ILanguage lang = getLanguage(language);
		if (lang != null)
		{
			retVal = lang.getDefaultSourceFileExtension();
		}
		return retVal;
	}

	/**
	 *
	 * Retrieves all the languages that contain ICodeGenerationScripts
	 *
	 * @param pVal[out] The collection of languages
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<ILanguage> getLanguagesWithCodeGenSupport() 
	{
		ETList<ILanguage> retLangs = null;
		ETList<ILanguage> langs = getSupportedLanguages2();
		if (langs != null)
		{
			int count = langs.size();
			for (int i=0; i<count; i++)
			{
				ILanguage pLang = langs.get(i);
				ETList<ICodeGenerationScript> pScripts = pLang.getCodeGenerationScripts();
				if (pScripts != null)
				{
					int numScripts = pScripts.size();
					if (numScripts > 0)
					{
						if (retLangs == null)
						{
							retLangs = new ETArrayList<ILanguage>();
						}
						retLangs.add(pLang);
					}
				}
			}
		}
		return retLangs;
	}

	/**
	 * Retrieves a collection of file filters for a specified language.
	 *
	 * @param langaugeName [in] The name of the language.
	 * @param groupString [out] The extensions.
	 * @param 
	 */
	public ETList<ILanguageFilter> getFileExtensionFilters(String languageName) 
	{
		ETList<ILanguageFilter> retVal = new ETArrayList<ILanguageFilter>();
		
		// m_FilterMap maps extensions to languages.
		// For all entries in m_ExtensionMap where language == @a language,
		// add that language to the list of strings.
		Enumeration iter = m_FilterMap.keys();
		while (iter.hasMoreElements())
		{
			String langName = (String)iter.nextElement();
			if (languageName.equals(langName))
			{
                ETList<ILanguageFilter> filters = m_FilterMap.get(langName);
				if (filters != null)
				{
					retVal = filters;
				}
			}
		}
		
		return retVal;
	}

	/**
	 * Retrieves a collection of file filters for a specified language.
	 *
	 */
	public ETList<ILanguageFilter> getFileExtensionFilters(ILanguage language) 
	{
        ETList<ILanguageFilter> retVal = new ETArrayList<ILanguageFilter>();
		if (language != null)
		{
			String langName = language.getName();
			retVal = getFileExtensionFilters(langName);
		}
		return retVal;
	}

	/**
	 *
	 * Populates this manager with the contents of the given
	 * configuration file.
	 *
	 * @return HRESULT
	 *
	 */
	private void initializeManager()
	{
		try
		{
			String loc = getActualConfigLocation();
			if (loc != null && loc.length() > 0)
			{
				// For now, we'll just keep the DOM in memory.
				// We may want to eventually get rid of this,
				// as it will certainly use less memory
				Document doc = XMLManip.getDOMDocument(loc);
				if (doc != null)
				{
					loadSupportedLanguages(doc);
					loadFileExtensions(doc);
				}
			}
		}
		catch (Exception e)
		{
		}
	}

	/**
	 *
	 * Determines the default location for the Languages.etc
	 * file.
	 *
	 * @param pVal [out] The location of the config file
	 * @return HRESULT
	 *
	 */
	private String getActualConfigLocation()
	{
		String retLoc = "";
		String homeLoc = getConfigLocation();
		if (homeLoc != null && homeLoc.length() > 0)
		{
			homeLoc += "Languages.etc";
			retLoc = homeLoc;
		}
		return retLoc;
	}

	/**
	 * Retrieves the language details from the configuration file.  The configuration 
	 * file location will be retrieved from the applications perferences.  After the
	 * language is retrieved it will be added to the language lookup table.
	 *
	 * @param langName [in] The langauge to retrieved
	 * @param pVal [out] The language details
	 */
	private ILanguage retrieveLanguage(String langName)
	{
		ILanguage retLang = null;
		Node node = retrieveLanguageNode(langName);
		if (node != null)
		{
			retLang = retrieveLanguageDetails(node);
			if (retLang != null)
			{
				m_LanguageMap.put(langName, retLang);
			}
			else
			{
				Log.out("LM_E_INVALID_LANG_NAME");
			}
		}
		return retLang;
	}

	private Node retrieveLanguageNode(String langName)
	{
		Node retNode = null;
		Document configDoc = getConfigurationDocument();
		if (configDoc != null)
		{
			retNode = findLanguage(configDoc, langName);
			if (retNode == null)
			{
				// if the language was not defined in the configuration file check if  
				// the language is defined by a language referenece.  If a language 
				// referenece exist retrieve the language from the referenced file.
				Document refDoc = locateReferenced(configDoc, langName);
				if (refDoc != null)
				{
					retNode = findLanguage(refDoc, langName);
				}
			}
		}
		return retNode;
	}

	private Node findLanguage(Document pDoc, String langName)
	{
		Node retNode = null;
		String xpath = "//Language[@type=\"";
		xpath += findLanguageName(langName);
		xpath += "\"]";
		retNode = pDoc.selectSingleNode(xpath);
		return retNode;
	}

	private String findLanguageName(String langName)
	{
		String retLang = langName;
		int count = m_LanguageNames.getCount();
		for (int i=0; i<count; i++)
		{
			String realName = m_LanguageNames.item(i);
			if (realName.length() > 0)
			{
				if (realName.toLowerCase().equals(langName.toLowerCase()))
				{
					retLang = realName;
					break;
				}
			}
		}
		return retLang;
	}

	private Document locateReferenced(Document pDoc, String langName)
	{
		Document retDoc = null;

		String xpath = "//LanguageRef[@type=\"";
		xpath += findLanguageName(langName);
		xpath += "\"]";
		Node pNode = pDoc.selectSingleNode(xpath);
		if (pNode != null)
		{
			String location = XMLManip.getAttributeValue(pNode, "location");
			String home = getConfigLocation();
			if (home != null && home.length() > 0)
			{
				String docLoc = home;
				if (location != null)
				{
					docLoc += location;
				}
				
				if (docLoc != null && docLoc.length() > 0)
				{
					retDoc = URILocator.retrieveDocument(docLoc);
				}
			}
		}
		return retDoc;
	}

	/**
	 * Retrieves the XML Document that is the Language configuration.
	 * 
	 * @param pVal [out] The configuraton document
	 */
	private Document getConfigurationDocument()
	{
		Document retDoc = null;
		String configLoc = getActualConfigLocation();
		if (configLoc != null && configLoc.length() > 0)
		{
			retDoc = XMLManip.getDOMDocument(configLoc);
		}
		return retDoc;
	}

	/**
	 * Retrieves the details about a specific language.  
	 *
	 * @param pLanguageNode [in] The XML node that represents the language
	 * @param pVal [out] The language details
	 */
	private ILanguage retrieveLanguageDetails(Node pLanguageNode)
	{
		ILanguage retLang = new Language();

		getLanguageName(pLanguageNode, retLang);
		getDataTypes(pLanguageNode, retLang);
		getLanguageSyntax(pLanguageNode, retLang);
		getCodeGenScripts(pLanguageNode, retLang);
		getExpansionOverrides(pLanguageNode, retLang );
		getParsers(pLanguageNode, retLang);
		getContexts(pLanguageNode, retLang);  
		getLibraries(pLanguageNode, retLang);
		getFormatDefinitions(pLanguageNode, retLang);
		getDefaults(pLanguageNode, retLang );
		getSupportedFeatures(pLanguageNode, retLang );
                getCollectionTypes(pLanguageNode, retLang);
		
		return retLang;
	}

	/**
	 * Retrieves the syntax of a language from a XML file and 
	 * initializes the ILanguage object
	 *
	 * @param manip [out] The XMLManip use to retrieve XML information.
	 * @param pLanguageNode [in] The XMLnode that contains language information.
	 * @param *pLang [in] The ILanguage object to update.
	 */
	private void getLanguageSyntax(Node pLanguageNode, ILanguage pLang)
	{
		ETList<ISyntaxToken> pTokens = new ETArrayList<ISyntaxToken>();
		List pNodeList = pLanguageNode.selectNodes("Syntax/Token");
		if (pNodeList != null)
		{
			int count = pNodeList.size();
			for (int i=0; i<count; i++)
			{
				Node pNode = (Node)pNodeList.get(i);
				ISyntaxToken token = getSyntaxToken(pNode);
				if (token != null)
				{
					pTokens.add(token);
				}
			}
		}

		// I have to make sure that I acutally add the collection to the 
		// language defintion. 
		ILanguageSyntax pSyntax = new LanguageSyntax();
		pSyntax.setSyntaxTokens(pTokens);
		pLang.setSyntax(pSyntax);
	}

	/**
	 * Retrieves the information about a syntax token from a XML file 
	 * and initializes the ILanguagae object
	 *
	 * @param manip [out] The XMLManip use to retrieve XML information.
	 * @param pLanguageNode [in] The XMLnode that contains language information.
	 * @param *pLang [in] The ILanguage object to update.
	 */
	private ISyntaxToken getSyntaxToken(Node pNode)
	{
		ISyntaxToken retToken = new SyntaxToken();
		String name = XMLManip.getAttributeValue(pNode, "name");
		retToken.setName(name);
		
		String type = XMLManip.getAttributeValue(pNode, "kind");
		int typeKind = ITokenKind.KEYWORD;
		if (type != null && type.equals("Operator"))
		{
			typeKind = ITokenKind.OPERATOR;
		}
		else if (type != null && type.equals("Delimiter"))
		{
			typeKind = ITokenKind.DELIMITER;
		}
		
		retToken.setKind(typeKind);
		
		String category = XMLManip.getAttributeValue(pNode, "category");
		retToken.setCategory(category);
		
		String syntaxType = XMLManip.getAttributeValue(pNode, "type");
		retToken.setType(syntaxType);
		
		return retToken;
	}

	/**
	 * Retrieves the code generation scrips for a language from 
	 * a XML file and initializes the ILanguage object
	 *
	 * @param manip [out] The XMLManip use to retrieve XML information.
	 * @param pLanguageNode [in] The XMLnode that contains language information.
	 * @param *pLang [in] The ILanguage object to update.
	 */
	private void getCodeGenScripts(Node pLanguageNode, ILanguage pLang)
	{
		ETList<ICodeGenerationScript> pScripts = new ETArrayList<ICodeGenerationScript>();
		List pNodeList = pLanguageNode.selectNodes("CodeGeneration/Script");
		if (pNodeList != null)
		{
			// Find out where the scripts live.  The XML contains script paths relative
			// to the Home Location
			String home = getConfigLocation();
			if (home != null && home.length() > 0)
			{
				int count = pNodeList.size();
				for (int i=0; i<count; i++)
				{
					Node pNode = (Node)pNodeList.get(i);
					ICodeGenerationScript script = getScript(pNode, home);
					if (script != null)
					{
						// Set up the backpoint from the script to the
						// language that owns that script
						script.setLanguage(pLang);
						pScripts.add(script);
					}
				}
			}
		}

		// I have to make sure that I acutally add the collection to the 
		// language defintion. 
		pLang.setCodeGenerationScripts(pScripts);
	}

	/**
	 * Retrieves a code generation script information from a XML file 
	 * and initializes the ILanguagae object
	 *
	 * @param manip [out] The XMLManip use to retrieve XML information.
	 * @param pLanguageNode [in] The XMLnode that contains language information.
	 * @param *pLang [in] The ILanguage object to update.
	 */
	private ICodeGenerationScript getScript(Node pNode, String homeLocation)
	{
		ICodeGenerationScript retScript = new CodeGenerationScript();
		
		String name = XMLManip.getAttributeValue(pNode, "event");
		retScript.setName(name);
		
		String location = XMLManip.getAttributeValue(pNode, "location");
		String scriptLoc = homeLocation + location;
		retScript.setFile(scriptLoc);
		
		return retScript;
	}

	/**
	 *
	 * Gathers any nodes that override the Default ExpansionVariables and place those on the Language passed in.
	 *
	 * @param manip[in]           The manip object
	 * @param pLanguageNode[in]   The node representing the current language
	 * @param pLang[in]           The COM object for the Language
	 *
	 * @return HRESULT
	 *
	 */
	private void getExpansionOverrides(Node pLanguageNode, ILanguage pLang)
	{
		List pNodeList = pLanguageNode.selectNodes("ExpansionVariables/ExpansionVar");
		if (pNodeList != null)
		{
			// Find out where the scripts live.  The XML contains script paths relative
			// to the Home Location
			String home = getConfigLocation();
			if (home != null && home.length() > 0)
			{
				int count = pNodeList.size();
				if (count > 0)
				{
					ICoreProduct prod = ProductRetriever.retrieveProduct();
					if (prod != null)
					{
						ITemplateManager pMan = prod.getTemplateManager();
						if (pMan != null)
						{
							IVariableFactory pFact = pMan.getFactory();
							if (pFact != null)
							{
								ETList<IExpansionVariable> vars = new ETArrayList<IExpansionVariable>();
								for (int i=0; i<count; i++)
								{
									Node node = (Node)pNodeList.get(i);
									IExpansionVariable pVar = pFact.createVariable(node);
									if (pVar != null)
									{
										vars.add(pVar);
									}
								}
								pLang.setExpansionVariables(vars);
							}
						}
					}
				}
			}
		}
	}

	/**
	 *
	 * Retrieves the default configuration location. On a debug box,
	 * this is typically c:\\development\\uml\\config
	 *
	 * @return The absolute path to the config location
	 *
	 */
	private String getConfigHome()
	{
		String homeLoc = "";
		ICoreProduct prod = ProductRetriever.retrieveProduct();
		if (prod != null)
		{
			IConfigManager conMan = prod.getConfigManager();
			if (conMan != null)
			{
				homeLoc = conMan.getDefaultConfigLocation();
			}
		}
		return homeLoc;
	}

	/**
	 * Retrieves the parsers for a language from a XML file and 
	 * initializes the ILanguage object
	 *
	 * @param manip [out] The XMLManip use to retrieve XML information.
	 * @param pLanguageNode [in] The XMLnode that contains language information.
	 * @param *pLang [in] The ILanguage object to update.
	 */
	private void getParsers(Node pLanguageNode, ILanguage pLang)
	{
		List pNodeList = pLanguageNode.selectNodes("Parsers/Parser");
		if (pNodeList != null)
		{
			int count = pNodeList.size();
			for (int i=0; i<count; i++)
			{
				Node pNode = (Node)pNodeList.get(i);
				String name = XMLManip.getAttributeValue(pNode, "name");
				String clsid = XMLManip.getAttributeValue(pNode, "parser");
				pLang.addParser(name, clsid);
			}
		}
	}

	/**
	 * Retrieves the context for a language from a XML file and 
	 * initializes the ILanguage object
	 *
	 * @param manip [out] The XMLManip use to retrieve XML information.
	 * @param pLanguageNode [in] The XMLnode that contains language information.
	 * @param *pLang [in] The ILanguage object to update.
	 */
	private void getContexts(Node pLanguageNode, ILanguage pLang)
	{
		List pNodeList = pLanguageNode.selectNodes("Contexts/Context");
		if (pNodeList != null)
		{
			int count = pNodeList.size();
			for (int i=0; i<count; i++)
			{
				Node pNode = (Node)pNodeList.get(i);
				String name = XMLManip.getAttributeValue(pNode, "kind");
				String clsid = XMLManip.getAttributeValue(pNode, "instantiationDirective");
				pLang.addContext(name, clsid);
			}
		}
	}

	/**
	 * Retrieves the libraries for a language from a XML file and 
	 * initializes the ILanguage object
	 *
	 * @param manip [out] The XMLManip use to retrieve XML information.
	 * @param pLanguageNode [in] The XMLnode that contains language information.
	 * @param *pLang [in] The ILanguage object to update.
	 */
	private void getLibraries(Node pLanguageNode, ILanguage pLang) 
        {
            List pNodeList = pLanguageNode.selectNodes("Libraries/Library"); // NOI18N
//            String home = getConfigLocation();
//            home = InstalledFileLocator.getDefault().locate(
//                "modules/languagedefs", "org.netbeans.modules.uml", false).getAbsolutePath();
            
            if (pNodeList != null) 
            {
                int count = pNodeList.size();
                for (int i=0; i < count; i++) 
                {
                    Node pNode = (Node)pNodeList.get(i);
                    String name = XMLManip.getAttributeValue(pNode, "name"); // NOI18N
                    String path = XMLManip.getAttributeValue(pNode, "path"); // NOI18N

                    String token = null;
                    if (path != null && path.length() > 0) 
                    {
                        if (path.indexOf("\\") != -1) // NOI18N 
                            token = "\\"; // NOI18N

                        else if (path.indexOf("/") != -1) // NOI18N
                            token = "/"; // NOI18N

                        if (token != null) 
                            path = path.replace(token, File.separator ).trim();
                    }

                    String fileName = "modules" + token + path + ".etd"; // NOI18N
                    String fullPath = InstalledFileLocator.getDefault().locate(
                        fileName, "org.netbeans.modules.uml", false) // NOI18N
                        .getAbsolutePath();


//                    String fulPath = "";
//                    if (home != null && home.length() > 0) 
//                    {
//                        // Get the directory separator right:
//                        // fulPath = new File(home, path).toString();
//                        File aFile = new File(home, path);
//                        if (aFile != null) 
//                        {
//                            try {
//                                fulPath = aFile.getCanonicalPath();
//                            } catch (IOException ex) {
//                                ex.printStackTrace();
//                            }
//                        }
//                        //System.out.println("fullPath(toString)="+fulPath);
//                    }

                    pLang.addLibrary(name, fullPath);
                }
            }
        }

	/**
	 * Retrieve the DOM node that specifies how to format a
	 * model elments data.  
	 *
	 * @param manip [out] The XMLManip use to retrieve XML information.
	 * @param pLanguageNode [in] The XMLnode that contains language information.
	 * @param *pLang [in] The ILanguage object to update.
	 */
	private void getFormatDefinitions(Node pLanguageNode, ILanguage pLang)
	{
		Node pNode = pLanguageNode.selectSingleNode("FormatDefinitions/PropertyDefinitions");
		if (pNode != null)
		{
			pLang.setFormatDefinitions(pNode);
		}
	}

	/**
	 * Install of the langauge that are supported by language manager.
	 * A XML configuration file is used to define the languages supported
	 * by the language manager.
	 *
	 * @param pDoc [in] The configuration file.
	 */
	private void loadSupportedLanguages(Document pDoc)
	{
		if (m_LanguageNames == null)
		{
			m_LanguageNames = new Strings();
			retrieveLanguagesNames(pDoc, "//Language", m_LanguageNames);
			retrieveLanguagesNames(pDoc, "//LanguageRef", m_LanguageNames);
		}
	}

	private void retrieveLanguagesNames(Document pDoc, String xpath, 
										IStrings pList)
	{
		List pNodeList = pDoc.selectNodes(xpath);
		if (pNodeList != null)
		{
			int count = pNodeList.size();
			for (int i=0; i<count; i++)
			{
				Node pNode = (Node)pNodeList.get(i);
				String name = XMLManip.getAttributeValue(pNode, "type");
				//String location = XMLManip.getAttributeValue(pNode, "location");
				
				if (name != null && name.length() > 0)
				{
					pList.add(name);
				}
			}
		}
	}

	/**
	 * Retrieves all of the extensions to use when retrieving a language
	 * for a specific file.  Each extension must map to one and only one
	 * language.
	 *
	 * @param pDoc [in] The configuration file.
	 */
	private void loadFileExtensions(Document pDoc)
	{
		List pNodeList = pDoc.selectNodes("//Extensions/Extension");
		if (pNodeList != null)
		{
			int count = pNodeList.size();
			for (int i=0; i<count; i++)
			{
				Node pNode = (Node)pNodeList.get(i);
				String name = XMLManip.getAttributeValue(pNode, "type");
				String language = XMLManip.getAttributeValue(pNode, "language");
				
				if (name != null && name.length() > 0)
				{
					m_ExtensionMap.put(name, language);
				}
			}
		}
		
		m_FilterMap.clear();
		List pFilterList = pDoc.selectNodes("//Extensions/Filter");
		if (pFilterList != null)
		{
			int count = pFilterList.size();
			for (int i=0; i<count; i++)
			{
				Node pNode = (Node)pFilterList.get(i);
				String name = XMLManip.getAttributeValue(pNode, "name");
				String language = XMLManip.getAttributeValue(pNode, "language");
				String filter = XMLManip.getAttributeValue(pNode, "value");
				
				ETList<ILanguageFilter> pFilters = null;
				if (m_FilterMap.containsKey(language))
				{
					pFilters = m_FilterMap.get(language);
				}
				else
				{
					pFilters = new ETArrayList<ILanguageFilter>();
					m_FilterMap.put(language, pFilters);
				}
				
				ILanguageFilter pFilter = new LanguageFilter();
				pFilter.setName(name);
				pFilter.setFilter(filter);
				pFilters.add(pFilter);
			}
		}
	}

	/**
	 * Retrieve the language definition 
	 *
	 * @param filename [in] The file name.
	 *
	 * @return 
	 */
	private String getLanguageNameForFile(String filename)
	{
		String retVal = "";
		String ext = StringUtilities.getExtension(filename);
		retVal = (String)m_ExtensionMap.get(ext);
		return retVal;
	}

	/** 
	 * Loads "default" settings for a particular language
	 * 
	 * @param manip[in] the XML manipulation object
	 * @param pLanguageNode[in] the XML DOM Node to load the data from
	 * @param pLang[in] the ILanguage object to load defaults for.
	 */
	private void getDefaults(Node pLanguageNode, ILanguage pLang)
	{
		List pNodeList = pLanguageNode.selectNodes("Defaults/Default");
		if (pNodeList != null)
		{
			int count = pNodeList.size();
			for (int i=0; i<count; i++)
			{
				Node pNode = (Node)pNodeList.get(i);
				String name = XMLManip.getAttributeValue(pNode, "name");
				String value = XMLManip.getAttributeValue(pNode, "value");
				pLang.setDefault(name, value);
			}
		}
	}

	private void getSupportedFeatures(Node pLanguageNode, ILanguage pLang)
	{
		List pNodeList = pLanguageNode.selectNodes("SupportedFeatures/Feature");
		if (pNodeList != null)
		{
			int count = pNodeList.size();
			for (int i=0; i<count; i++)
			{
				Node pNode = (Node)pNodeList.get(i);
				String name = XMLManip.getAttributeValue(pNode, "name");
				String value = XMLManip.getAttributeValue(pNode, "value");
				boolean bValue = false;
				if (value != null && value.toLowerCase().equals("true"))
				{
					bValue = true;
				}
				pLang.setFeatureSupported(name, bValue);
			}
		}
	}

	/**
	 * Retrieves the name of the language and initializes the ILanguage
	 * object
	 *
	 * @param manip [out] The XMLManip use to retrieve XML information.
	 * @param pLanguageNode [in] The XMLnode that contains language information.
	 * @param *pLang [in] The ILanguage object to update.
	 */
	private void getLanguageName(Node pLanguageNode, ILanguage pLang)
	{
		String name = XMLManip.getAttributeValue(pLanguageNode, "type");
		pLang.setName(name);
	}

        
        private void getCollectionTypes(Node pLanguageNode, ILanguage pLang)
        {
            String xpath = "Collections/Collection";
		
            ETList<CollectionType> types = new ETArrayList<CollectionType>();
            List pNodeList = pLanguageNode.selectNodes(xpath);
            if (pNodeList != null)
            {
                    int count = pNodeList.size();
                    for (int i=0; i<count; i++)
                    {
                            Node node = (Node)pNodeList.get(i);
                            CollectionType type = getCollectionType(node);
                            if (type != null)
                            {
                                    types.add(type);
                            }
                    }
            }

            // I have to make sure that I acutally add the collection to the 
            // language defintion.
            pLang.setCollectionTypes(types); 
        }
            
        /**
	 * Retrieves a collection type information from a XML file and initializes 
	 * the ILanguage object
	 *
	 * @param pNode The XML node for the collection type data
	 */
	private CollectionType getCollectionType(Node pNode)
	{
		CollectionType retVal = new CollectionType();
		String name = XMLManip.getAttributeValue(pNode, "name");
		retVal.setName(name);
		
		String packName = XMLManip.getAttributeValue(pNode, "package");
                retVal.setPackageName(packName);
		
		boolean userdefined = XMLManip.getAttributeBooleanValue(pNode, 
                                                                        "userdefined");
		retVal.setUserDefined(userdefined);;
		
		boolean defaultValue = XMLManip.getAttributeBooleanValue(pNode, 
                                                                         "default", 
                                                                         false);
		retVal.setDefaultType(defaultValue);
		
		return retVal;
	}
        
	/**
	 * Retrieves the data types from a XML file and initializes the ILanguage
	 * object
	 *
	 * @param manip [out] The XMLManip use to retrieve XML information.
	 * @param pLanguageNode [in] The XMLnode that contains language information.
	 * @param *pLang [in] The ILanguage object to update.
	 */
	private void getDataTypes(Node pLanguageNode, ILanguage pLang)
	{
		String xpath = "DataTypes/DataType";
		
		ETList<ILanguageDataType> pDataTypes = new ETArrayList<ILanguageDataType>();
		List pNodeList = pLanguageNode.selectNodes(xpath);
		if (pNodeList != null)
		{
			int count = pNodeList.size();
			for (int i=0; i<count; i++)
			{
				Node node = (Node)pNodeList.get(i);
				ILanguageDataType pDataType = getDataType(node);
				if (pDataType != null)
				{
					pDataTypes.add(pDataType);
					boolean attrDefault = pDataType.getIsDefaultAttributeType();
					if (attrDefault)
					{
						pLang.setAttributeDefaultType(pDataType);
					}
					
					boolean opDefault = pDataType.getIsOperationDefaultType();
					if (opDefault)
					{
						pLang.setOperationDefaultType(pDataType);
					}
				}
			}
		}
		
		// I have to make sure that I acutally add the collection to the 
		// language defintion.
		pLang.setDataTypes(pDataTypes); 
	}

	/**
	 * Retrieves a data type information from a XML file and initializes 
	 * the ILanguage object
	 *
	 * @param manip [out] The XMLManip use to retrieve XML information.
	 * @param pLanguageNode [in] The XMLnode that contains language information.
	 * @param *pLang [in] The ILanguage object to update.
	 */
	private ILanguageDataType getDataType(Node pNode)
	{
		ILanguageDataType retVal = new LanguageDataType();
		String name = XMLManip.getAttributeValue(pNode, "name");
		retVal.setName(name);
		
		String type = XMLManip.getAttributeValue(pNode, "type");
		
		int typeKind = IDataTypeKind.PRIMITIVE;
		if (type != null && type.equals("user-defined"))
		{
			typeKind = IDataTypeKind.USER_DEFINIED;
		}
		else
		{
			// Only Primitive data types have a UML Name.
			String umlNameVal = XMLManip.getAttributeValue(pNode, "uml");
			if (umlNameVal != null && umlNameVal.length() > 0)
			{
				retVal.setUMLName(umlNameVal);
			}
		}
		retVal.setKind(typeKind);
		
		String defaultVal = XMLManip.getAttributeValue(pNode, "default_value");
		retVal.setDefaultValue(defaultVal);
		
		String scope = XMLManip.getAttributeValue(pNode, "scope");
		retVal.setScope(scope);
		
		boolean attrDefault = XMLManip.getAttributeBooleanValue(pNode, "attributedefault");
		retVal.setIsDefaultAttributeType(attrDefault);
		
		boolean opDefault = XMLManip.getAttributeBooleanValue(pNode, "operationdefault");
		retVal.setIsOperationDefaultType(opDefault);
		
		return retVal;
	}
}



