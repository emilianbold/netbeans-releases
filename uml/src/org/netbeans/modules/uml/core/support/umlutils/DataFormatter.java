/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.uml.core.support.umlutils;

import java.io.File;
import java.net.URI;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.support.umlsupport.FileSysManip;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.URIHelper;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class DataFormatter implements IDataFormatter
{
   public DataFormatter()
   {
		ICoreProduct prod = ProductRetriever.retrieveProduct();
		if (prod != null)
		{
			IConfigManager conMan = prod.getConfigManager();
			if (conMan != null)
			{
				String home = conMan.getDefaultConfigLocation();
				if (home != null && home.length() > 0)
				{
					String format = home;
					format += "LifelineFormat.xsl";
					m_Formatters.put("Lifeline", format);

					format = home;
					format += "InteractionConstraintFormat.xsl";
					m_Formatters.put("InteractionConstraint", format);

					format = home;
					format += "MultiplicityRangeFormat.xsl";
					m_Formatters.put( "MultiplicityRange", format);

					format = home;
					format += "MultiplicityFormat.xsl";
					m_Formatters.put("Multiplicity", format);

					format = home;
					format += "ParameterableElementFormat.xsl";
					m_Formatters.put("ParameterableElement", format);
					
				}
			}
		}
   }

   //hashtable to store scriptname and corresponding Transformer
   private Hashtable < String, XslTransformer > m_Procs = new Hashtable < String, XslTransformer > ();

   //hashtable to store scriptname and corresponding path to xslt script.
   private Hashtable < String, String > m_Formatters = new Hashtable < String, String > ();

   //hashtable to store language name and corresponding property definition factory.
   private Hashtable < String, IPropertyDefinitionFactory > m_Factories = 
      new Hashtable < String, IPropertyDefinitionFactory > ();

   private boolean m_bIsAlias = false;
   private IPropertyElementManager m_ElementManager = null;
   private String m_ConfigDirectory = "";
   
   /**
    *
    * Formats the passed-in element against an XSL script of the appropriate
    * type.
    *
    * @param element[in] The element to format
    * @param format[out] The result of the XSL process
    *
    * @return HRESULT
    *
    */
   public String formatElement(IElement elem)
   {
      FormatterHelper helper = new FormatterHelper(elem);
      String format = "";
      
      // HACK: I know that this is a hack.  But, JDK 1.5 way that XSLT calls java procedure
      //       I do not have time to fix how the our XSLT processor works, so I am 
      //       having to go down an alternative path for elements that have type.
      if(!(elem instanceof org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IOperation) && 
         !(elem instanceof org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.ITypedElement))
      {         
         format = formatUsingXSLT(helper);
      }
      
		if( format == null || format.length() == 0 )
		{
			IPropertyElement pElem = null;
			if (elem != null)
			{
			   IPropertyDefinition pDef = getDefinition(helper);
			   if (pDef != null)
			   {
				   pElem = getPropertyElement(helper, pDef);
				   format = formatElement(helper);
			   }
			}
		}
      	return format;
   }

   /**
    * Formats @a element using a script named @a scriptName.  The script must have
    * been added to the data formatter prior to calling this function.
    *
    * @param element[in] The element to format
    * @param scriptName[in] The XSLT script name (not the file name) to format
    *                       @a element with
    * @param format[out] The formatted XML
    *
    * @return HRESULT
    */
   public String formatElement(IElement elem, String scriptName)
   {
      String format = null;
      XslTransformer proc = retrieveProcessor(scriptName);
      if (proc != null)
      {
		FormatterHelper helper = new FormatterHelper( elem );
		format = helper.formatWithProcessor( proc );
      }
      return format;
   }
   /**
    * Formats the data elements data.  The property elements used to
    * format the elements data and the formated string is returned.
    *
    * @param element [in] The element to be formatted.
    * @param propElement [out] The property elements used to format
    *                          the data.  If PropertyElements are
    *                          not used to produce the formatted
    *                          value NULL will be returned.
    * @param format [out] The formatted value.
    */
   public String formatElement(IElement elem, IPropertyElement propElem)
   {
		FormatterHelper helper = new FormatterHelper(elem);
		return formatElement( helper);
   }
   protected String formatElement(FormatterHelper helper)
   {
	  String format = null;
	  format = formatUsingDefinition(helper);
	  if(format == null || format.length() == 0)
	  {
	     format = formatUsingXSLT( helper );
	  }
	  return format;
   }
   /**

   /**
    *
    * Retrieves the appropriate processor given the script name. The processor
    * may be created upon execution of this method. If it has already been created,
    * the cached version of the processor is returned.
    *
    * @param scriptName[in] The name of the script to retrieve
    * @param proc[out] The processor associated with the element
    *
    * @return Will return DFR_S_NO_FORMATTER if no XSL script has been associated with
    *         the particular element type, else HRESULT
    *
    */
   protected XslTransformer retrieveProcessor(String scriptName)
   {
      XslTransformer xsl = null;
      if (scriptName != null && scriptName.length() > 0)
      {
         // Retrieve the processor from our map. If one doesn't exist, attempt to create
         // from our list of formatters. If that fails, we have no choice but to give up
         xsl = m_Procs.get(scriptName);
         if (xsl == null)
         {
            // We haven't created a processor yet, so let's see if we have one that
            // is registered to be created.
            String obj1 = m_Formatters.get(scriptName);
            if (obj1 != null && obj1.length() > 0)
            {
               xsl = createProcessor(obj1);
               if (xsl != null)
               {
                  m_Procs.put(scriptName, xsl);
               }
            }
            else
            {
               //throw an error - DFR_S_NO_FORMATTER
            }
         }
      }
      return xsl;
   }

   protected XslTransformer retrieveProcessor(FormatterHelper helper)
   {
	  	XslTransformer trans = null;
	  	String key = helper.getFormatterKey();
		String obj1 = m_Formatters.get(key);
		if (obj1 == null)
		{
			String formatFile = getFormatStringFile( helper);
			if (formatFile != null && formatFile.length() > 0)
			{
				m_Formatters.put(key, formatFile);
			}
		}
		trans = retrieveProcessor(key);
	  	return trans;
   }

   /**
    *
    * Creates a processor against a script that is referenced in fileName.
    *
    * @param fileName[in] The absolute path to the XSL script
    * @param proc[out] The created processor, else 0 on error
    *
    * @return HRESULT
    *
    */
   protected XslTransformer createProcessor(final String fileName)
   {
	XslTransformer proc = null;
        String uriFileName = null;
	if (fileName != null && fileName.length() > 0)
	{
            try
            {
                File aFile = new File(fileName);
                if (aFile.exists() && aFile.isFile()) {
                    URI uri = aFile.toURI();
                    if (uri != null) {
                        uriFileName = uri.toString();
                        proc = new XslTransformer(uriFileName);
                    }
                }
            }
            catch(TransformerConfigurationException e)
            {
                 // This exception is handled later when the XslTransformer returned as null
                 // UPDATE:  Send a message to the user messaging system
                 System.out.println("Error in file :" + uriFileName);
                 e.printStackTrace();
            }
	}
	return proc;
   }
   

   /**
    *
    * Associates the specified XSL script with a particular element type.
    *
    * @param scriptName[in] The name of the script to add
    * @param xslFileName[in] The absolute path to the XSL script
    *
    * @return HRESULT
    *
    */
   public void addScript(String scriptName, String xslFileName)
   {
      if (scriptName != null && scriptName.length() > 0)
         m_Formatters.put(scriptName, xslFileName);
   }


   /**
    * Retrieve a particular value from the DataFormatter's map given the key.
    *
    * @param key[in]	Key to find
    * @param scriptName[out] Value found in map
    *
    * @return HRESULT
    *
    */
   public String getScriptFromMap(String key)
   {
      String scriptName = m_Formatters.get(key);
      return scriptName;
   }

   /**
    * Remove a particular value from the DataFormatter's map given the key.
    *
    * @param key[in] Key to find
    *
    * @return HRESULT
    */
   public void removeScriptFromMap(String key)
   {
      String obj = m_Formatters.get(key);
      if (obj != null)
      {
         m_Formatters.remove(key);
      }
   }

   /**
    * Remove all values from the DataFormatter's map.
    *
    *
    * @return HRESULT
    */
   public void clearMap()
   {
      m_Formatters.clear();
   }

   public boolean isAliasOn()
   {
      return m_bIsAlias;
   }

   public void setAlias(boolean alias)
   {
      m_bIsAlias = alias;
   }

   /**
    *
    * Formats the passed-in xml node against an XSL script of the appropriate
    * type.
    *
    * @param pNode[in]	The xml node to format
    * @param format[out]	The result of the XSL process
    *
    * @return HRESULT
    *
    */
   public String formatNode(Node node)
   {
      String format = null;
      if (node != null)
      {
		FormatterHelper helper = new FormatterHelper( node );
		XslTransformer trans = retrieveProcessor( helper );
		if( trans != null )
		{
		   format = helper.formatWithProcessor( trans );
		}
      }
      return format;
   }

   /**
    * Formats @a element using a script named @a scriptName.  The script must have
    * been added to the data formatter prior to calling this function.
    *
    * @param element[in] The element to format
    * @param scriptName[in] The XSLT script name (not the file name) to format
    *                       @a element with
    * @param format[out] The formatted XML
    *
    * @return HRESULT
    */
   public String formatNode(Node node, String scriptName)
   {
		String format = null;
		XslTransformer trans = retrieveProcessor( scriptName );
		if( trans != null )
		{
		   FormatterHelper helper = new FormatterHelper( node );
		   format = helper.formatWithProcessor( trans );
		}
	    return format;
   }
   public String formatNode(org.w3c.dom.Node node, String scriptName)
   {
		String format = null;
		XslTransformer trans = retrieveProcessor( scriptName );
		if( trans != null )
		{
		   FormatterHelper helper = new FormatterHelper( node );
		   format = helper.formatWithProcessor( trans );
		}
		return format;
   }

   /**
    *
    * Adds the passed in Object to all Processors that this Formatter currently
    * knows about. If no processors have been created, this method does nothing.
    *
    * @param namespaceURI[in] The uri to use when referring to methods in the
    *                         passed in object from an xslt script. For example:
    *
    *                         "urn:uriHelper"
    *
    * @param pDisp[in]        The actual COM object to be placed on the Transformer
    *
    * @return HRESULT
    *
    */
   public void addObject(String namespaceURI, Object obj)
   {
      Enumeration < XslTransformer > enumVal = m_Procs.elements();
      while (enumVal.hasMoreElements())
      {
         XslTransformer item = enumVal.nextElement();
         if (item instanceof XslTransformer)
         {
            XslTransformer trans = (XslTransformer)item;
            //add the object to this transformer.

         }
      }
   }

   /**
    *
    * Adds the passed in Object to the Processor that is associated with the passed
    * in script location. If no processors have been created for that script,
    * this method does nothing.
    *
    * @param
    * @param namespaceURI[in] The uri to use when referring to methods in the
    *                         passed in object from an xslt script. For example:
    *
    *                         "urn:uriHelper"
    *
    * @param pDisp[in]        The actual COM object to be placed on the Transformer
    *
    * @return HRESULT
    *
    */
   public void addObject(String scriptName, String namespaceURI, Object obj)
   {
      if (scriptName != null && scriptName.length() > 0)
      {
         XslTransformer obj1 = m_Procs.get(scriptName);
         if (obj1 != null)
         {
            XslTransformer trans = (XslTransformer)obj1;
            //add the object to this transformer
         }
         else
         {
            XslTransformer proc = retrieveProcessor(scriptName);
            if (proc != null)
            {
               XslTransformer trans = (XslTransformer)proc;
               //add the object to this transformer
            }
         }
      }
      else
         addObject(namespaceURI, obj);
   }

   public IPropertyElement processEnumeration(IPropertyElement pData)
   {
      IPropertyElementManager man = getElementManager();
      if (man != null)
      {
         man.processEnumeration(pData);
      }
      return pData;
   }

   /**
    * Retrieves the property elements that represent the elements data.
    * the property elements structure will be dictated by the specified
    * property elements.
    *
    * @param pElement [in] The element that is being processed.
    * @param pVal [out] The propety element.
    */
   public IPropertyElement getPropertyElement(IElement pElement)
   {
   		IPropertyElement pEle = null;
		FormatterHelper helper = new FormatterHelper( pElement );
		IPropertyDefinition pDefinition = getDefinition( helper );
		if(pDefinition != null)
		{
		   pEle = getPropertyElement(helper, pDefinition);
		}
   		return pEle;
   }

   /**
    * Retrieves the property elements that represent the elements data.
    * the property elements structure will be dictated by the specified
    * property elements.
    *
    * @param pElement [in] The element that is being processed.
    * @param pDefinition [in] The property defintions.
    * @param pVal [out] The propety element.
    */
   private IPropertyElement getPropertyElement(FormatterHelper helper, IPropertyDefinition def)
   {
      IPropertyElement pEle = null;
      IPropertyElementManager man = getElementManager();
      if (man != null)
      {
         man.setModelElement(helper.getElement());
         man.setCreateSubs(true);
         pEle = man.buildTopPropertyElement(def);
      }
      return pEle;
   }

   public IPropertyElementManager getElementManager()
   {
      if (m_ElementManager == null)
      {
         IPropertyDefinitionFactory fact = new PropertyDefinitionFactory();
         m_ElementManager = new PropertyElementManager();
		if (fact != null)
		{
			ICoreProduct prod = ProductRetriever.retrieveProduct();
			if (prod != null)
			{
				IConfigManager conMan = prod.getConfigManager();
				if (conMan != null)
				{
					String home = conMan.getDefaultConfigLocation();
					String file = home + "JavaLanguage.etc";
					fact.setDefinitionFile(file);
					//fact.buildDefinitionsUsingFile();
				}
			}
		}
         m_ElementManager.setPDFactory(fact);
      }
      return m_ElementManager;
   }

   // *********************************************************************
   // Property Definitions Format Methods.
   // *********************************************************************

   /**
    * Formates the element data according to a property definition.  The
    * pDefinitionsNode is transformed into a IPropertyDefinition.  The
    * property defintion is then used to retrieve property elements.
    * Finally, the property elements are used to format the elements value.
    *
    * If at any point a failure occurs then pElements and formattedValue
    * will not be filled in.
    *
    * @param elementType [in] The element type to format.
    * @param pDefinitions [in] The definitions to use.
    * @param formatedValue [out] The value after it has been formated.
    * @param pElement [out] The property element that represents the
    *                       formated data.
    */
   protected String formatUsingDefinition(FormatterHelper helper)
   {
   	    String retStr = "";
		IPropertyDefinition pDefinition = getDefinition(helper);
		if(pDefinition != null)
		{
		   IPropertyElement pPropEle = getPropertyElement( helper, pDefinition);
		   if(pPropEle != null)
		   {
			  retStr = formatData( helper, pPropEle);
		   }
		}
   		return retStr;
   }

   /**
    * Uses an XSLT script to format the value of the model element.
    *
    * @param pElement [in] The element that is being processed.
    * @param pVal [in] The formated value.
    */
   protected String formatUsingXSLT(FormatterHelper helper)
   {
      String retStr = null;
      if (helper != null)
      {
		XslTransformer trans = retrieveProcessor(helper);
		if( trans != null )
		{
		   retStr = helper.formatWithProcessor( trans );
		}
      }
      return retStr;
   }

   /**
    * Retrieves the property definitions used to format the elements data.
    * The definitions can be used to retrieve the property elements that
    * represents the elements data.
    *
    * @param pElement [in] The element being processed.
    * @param pVal [out] The property definitions for the element.
    */
   protected IPropertyDefinition getDefinition(FormatterHelper helper)
   {
      IPropertyDefinition pDef = null;
      // Get the property definition factory for this language from our cache, note the
      // language can be NULL.
      IPropertyDefinitionFactory pFactory = getFactoryForLanguage(helper);
      if (pFactory != null)
      {
		 Node pNode = helper.getProDefNode();
		 // Now build the definition
		 if( pNode != null )
		 {
			// before we go and build the definition we want to check that the element
			// inherits from the id stored in the unknown node
			if (pNode != null && pNode instanceof org.dom4j.Element)
			{
			   org.dom4j.Element ele = (org.dom4j.Element)pNode;
			   // before we go and build the definition we want to check that the element
			   // inherits from the id stored in the unknown node
			   Node n = ele.attribute("id");
			   if (n == null)
			   {
				  pNode = null;
			   }
			}
		 }
         if (pNode != null)
         {
            pDef = pFactory.buildDefinitionFromNode(pNode);
         }
      }
      return pDef;
   }

   /**
    * Returns a factory for the given language
    *
    * @param pLang [in] The language that should match the returned factory.
    * @param pVal [out] The factory for this language.
    */
   private IPropertyDefinitionFactory getFactoryForLanguage(FormatterHelper helper)
   {
      	IPropertyDefinitionFactory fact = null;
      
		String langName = helper != null ? helper.getLanguageName() : null;
		if (langName != null && langName.length() > 0)
		{
	   		// Find the name in our cache of factories, if we can't find it then create a
	   		// new factory.
			fact = m_Factories.get(langName);
			if (fact == null)
		 	{
				fact = new PropertyDefinitionFactory();
				if (fact != null)
				{
					ICoreProduct prod = ProductRetriever.retrieveProduct();
					if (prod != null)
					{
						IConfigManager conMan = prod.getConfigManager();
						if (conMan != null)
						{
							String home = conMan.getDefaultConfigLocation();
							String file = home + "JavaLanguage.etc";
							fact.setDefinitionFile(file);
							//fact.buildDefinitionsUsingFile();
						}
					}
				}
				m_Factories.put(langName, fact);
		 	}
		}
		return fact;
   }

   /**
    * Retrieves the node that defines the property definiton for the
    * specified element.
    *
    * @param pElement [in] The element being processed.
    * @param pLanguage [in] The language that is being represented.
    * @param pVal [out] The XML node that represent the element type.
    */
   protected Node getDefinitionNode(IElement pElem, ILanguage lang)
   {
      String type = "";
      if (pElem != null)
         type = pElem.getElementType();
      else
         type = "Unknown";

      Node n = null;
      if (lang != null)
      {
         n = lang.getFormatDefinition(type);
      }
      return n;
   }

   /**
    * Formats the data.  The format of the data will be dictated by
    * the property elements.
    *
    * @param pElement [in] The element that'll be used to figure out the language
    * @param pData [in] The data that needs to be formatted.
    * @param pVal [in] The formatted value.
    */
   private String formatData(FormatterHelper helper, IPropertyElement pEle)
   {
      String retData = null;
      if (pEle != null)
      {
         IPropertyDefinitionFactory fact = getFactoryForLanguage(helper);
         // get the model element on the property element
         Object mEle = pEle.getElement();
         if (mEle != null)
         {
            IPropertyElementManager man = getElementManager();
            IPropertyDefinition def = fact.getPropertyDefinitionForElement("", mEle);
            if (def != null && man != null)
            {
               man.reloadElement(mEle, def, pEle);
            }
         }
         Vector elems = pEle.getSubElements();
         if (elems != null && elems.size() > 0)
         {
            String temp = "";
            for (int i = 0; i < elems.size(); i++)
            {
               IPropertyElement item = (IPropertyElement)elems.get(i);
               temp = formatData2(item, temp);
            }
            retData = temp;
         }
      }
      return retData;
   }

   private String formatData2(IPropertyElement pEle, String str)
   {
      String sValue = "";
      if (pEle != null)
      {
         String name = pEle.getName();
         // test for alias mode, only process alias definition if in alias mode
         // and don't process if not in alias mode
         if (((m_bIsAlias == false)
            && (name.compareToIgnoreCase("alias") == 0))
            || ((m_bIsAlias == true) && (name.compareToIgnoreCase("alias") == 0)))
         {
            return str;
         }

         IPropertyElementManager man = getElementManager();
         if (man != null)
         {
            // ProcessEnumeration() potentially converts the saved value from an enum into a string, if appropriate
            man.processEnumeration(pEle);
         }

         IPropertyDefinition def = pEle.getPropertyDefinition();
         String sRequired = "",
            sVisible = "",
            sMultiplicity = "",
            sLead = "",
            sTrail = "",
            sDelimitor = "";
         if (def != null)
         {
            sRequired = def.getFromAttrMap("required");
            sVisible = def.getFromAttrMap("visible");
            sMultiplicity = def.getFromAttrMap("multiplicity");
            sLead = def.getFromAttrMap("leadSeparator");
            sTrail = def.getFromAttrMap("trailSeparator");
            sDelimitor = def.getFromAttrMap("delimitor");
         }

         Vector elems = pEle.getSubElements();
         if (elems != null && elems.size() > 0)
         {
            String temp = null;
            for (int i = 0; i < elems.size(); i++)
            {
               IPropertyElement item = (IPropertyElement)elems.get(i);
               if (sValue.length() > 0 && sDelimitor != null)
               {
                  sValue += sDelimitor;
               }
               sValue = formatData2(item, sValue);
            }
         }

         // if no subelements then use this element's value
         if (elems.size() == 0)
         {
            // NOTE: parameters is returning the name of the param, shouldn't be (name is returned by a sub-element)
            sValue = pEle.getTranslatedValue();
            
            //need to convert it into displayable value if its one of the valid values
            String vals = def.getValidValues();
            if (vals != null)
            {
            	int pos = vals.indexOf("|");
            	if (pos >= 0)
            	{
            		StringTokenizer tokenizer = new StringTokenizer(vals, "|");
            		try 
            		{
            			if (sValue != null)
            			{
            				int intVal = Integer.valueOf(sValue).intValue();
            				int count = 0;
            				while (tokenizer.hasMoreTokens())
            				{
            					String token = tokenizer.nextToken();
            					if (count == intVal)
            					{
            						sValue = token;
            						break;
            					}
            					count++;
            				}
            			}
            		}
            		catch (NumberFormatException e)
            		{
            		}
            	}
            }
         }

         // we're visible if told to be, or if conditions warrant
         if (sVisible == null || sVisible.length() == 0)
            sVisible = "notEmpty";

         boolean bVisible = (sVisible.equals("true"));

         // visible if "notEmpty" and our data has a value
         if (!bVisible)
         {
			bVisible = (sVisible.equals("notEmpty") && sValue != null && sValue.length() > 0);
			if (!bVisible)
			{
				bVisible = (sVisible.equals("previousNotEmpty") && str.length() > 0);
			}
         }

         if (bVisible)
         {
         	if (sLead != null)
         	{
				str += sLead;
         	}
			
			if (sValue != null)
			{
				str += sValue;
			}
			if (sTrail != null)
			{
				str += sTrail;
			}
         }
      }
      return str;
   }
   public String getFormatStringFile(Object obj)
   {
   		FormatterHelper helper = new FormatterHelper(obj);
   		return getFormatStringFile(helper);
   }
   protected String getFormatStringFile(FormatterHelper helper)
   {
		String tempFile = "";
		String bsFormatFile = helper.getElementsXSLTFile();
		if (bsFormatFile != null && bsFormatFile.length() > 0)
		{
			String dir = getConfigDirectory();
			if(dir != null &&  dir.length() > 0)
			{
			  tempFile = dir + bsFormatFile;
			}
		}
		return tempFile;
   }
   
   protected String getConfigDirectory()
   {
	  if(m_ConfigDirectory == null ||  m_ConfigDirectory.length() == 0)
	  {
		  ICoreProduct prod = ProductRetriever.retrieveProduct();
		  if( prod != null )
		  {
			  IConfigManager configMgr = prod.getConfigManager();
			  if (configMgr != null)
			  {
				m_ConfigDirectory = configMgr.getDefaultConfigLocation();
			  }
		  }
	  }
	  return m_ConfigDirectory;
   }
  
   public ILanguage getActiveLanguage(Object pDisp)
   {
		FormatterHelper helper = new FormatterHelper( pDisp );
		return helper.getLanguage();
   }
  
   public IPropertyElement getPropertyElementByContext(IElement pElement, String context)
   {
   		IPropertyElement pEle = null;
		FormatterHelper helper = new FormatterHelper(pElement);
		helper.setContext(context);
		IPropertyDefinition pDefinition = getDefinition( helper );
		if(pDefinition != null)
		{
		   pEle = getPropertyElement(helper, pDefinition);
		}
		return pEle;
   }

}