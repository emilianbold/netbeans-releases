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

package org.netbeans.modules.uml.core.support.umlutils;

import java.util.List;
import java.util.Hashtable;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.coreapplication.CoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * @author sumitabhk
 *
 */
public class PropertyDefinitionFilter implements IPropertyDefinitionFilter
{
	private String m_FilterFile = null; 
        private Hashtable<String, Document> m_LoadedDocs = new Hashtable<String, Document>();

	/**
	 * 
	 */
	public PropertyDefinitionFilter()
	{
		super();
		m_FilterFile = "";
	}

	/**
	 * Apply language specific filters to the passed in property element structure.
	 * This may remove some property elements, rename some, change their values, etc.
	 *
	 * The filters that are used will be based on the element on this property element.
	 *
	 * @param pEle[in]	The property element to apply the filter to
	 *
	 * @return HRESULT
	 */
	public long filterPropertyElement(IPropertyElement pEle)
	{
		Object obj = pEle.getElement();
		if (obj != null)
		{
			// is it a model element
			if (obj instanceof IElement)
			{
				IElement pElement = (IElement)obj;
				
				// from the element we should be able to tell what language it is
				ILanguage lang = getActiveLanguage(pElement);
				if (lang != null)
				{
					// get the language name
					String langName = lang.getName();
					if (langName.length() > 0)
					{
						filterPropertyElement(pEle, langName);
					}
				}
			}
		}
		return 0;
	}

	/**
	 * Apply language specific filters to the passed in property element structure.
	 * This may remove some property elements, rename some, change their values, etc.
	 * 
	 * The filters that are used will be based on the passed in language
	 *
	 * @param pEle[in]	The property element to apply the filter to
	 * @param sLang[in]	The language to use as the filter
	 *
	 * @return HRESULT
	 *
	 */
	private void filterPropertyElement(IPropertyElement pEle, String langName)
	{
		// what filter file should we use
		String file = getFilterFile();
		if (file.length() > 0)
		{
			// get the dom document for this file
			Document doc = getDOMDocument(file);
			if (doc != null)
			{
				// get the node in the filter structure that matches this language
				Node pLangNode = getLanguageFilterNode(doc, langName);
				if (pLangNode != null)
				{
					Object pDisp = pEle.getElement();
					// is it a model element
					if (pDisp != null && pDisp instanceof IElement)
					{
						IElement pElement = (IElement)pDisp;

						// get all filter nodes that apply to the model element
						// that we have
						List list = getElementFilterNodes(pLangNode, pElement);
						if (list != null)
						{
							for (int i=0; i<list.size(); i++)
							{
								Node pEleNode = (Node)list.get(i);
								
								// process anything that should be removed from the structure
								processRemove(pEle, pEleNode);
								// process anything that needs to be changed
								processModify(pEle, pEleNode);
							}
						}
					}
				}
			}
		}
	}



	/**
	 *  caching reference to parsed file locally in the instance hash,
	 *  so the lifecycle of the reference follow that of the instance
	 */
	public Document getDOMDocument(String fileName)
        {
	        Document doc = null;
		if (fileName != null && fileName.length() > 0)
		{
		        doc = m_LoadedDocs.get(fileName);
			if (doc == null)
			{
				doc = XMLManip.getDOMDocument(fileName);
				m_LoadedDocs.put(fileName, doc);
			}
		}
		return doc;
	}


	/**
	 * Find the nodes in the passed in xml node that represent property elements that need to
	 * be changed in the current property element
	 * 
	 *
	 * @param pEle[in]			The current property element
	 * @param pNode[in]			The node that houses any "modify" nodes
	 *
	 * @return HRESULT
	 *
	 */
	private void processModify(IPropertyElement pEle, Node pEleNode)
	{
		// find all of the filter op nodes of type "modify"
		String pattern = "FilterOp[@name = \'modify\']";
		List nodeList = pEleNode.selectNodes(pattern);
		
		if (nodeList != null)
		{
			int count = nodeList.size();
			for (int i=0; i<count; i++)
			{
				Node pModifyNode = (Node)nodeList.get(i);

				// get the attribute on this filter op node of type xpath
				// this represents the name of the property element
				String pattern2 = "@xpath";
				Node nameNode = pModifyNode.selectSingleNode(pattern2);
				if (nameNode != null)
				{
					// have the "name" of the property element that needs to be changed
					String name = nameNode.getText();
					
					//
					// loop through the sub elements of the passed in element
					// and when we find a match on name, change it
					//
					Vector<IPropertyElement> subElems = pEle.getSubElements();
					if (subElems != null)
					{
						int eleCount = subElems.size();
						for (int j=0; j<eleCount; j++)
						{
							IPropertyElement subEle = subElems.elementAt(j);
							String defName = subEle.getName();
							if (defName != null && defName.equals(name))
							{
								// right now the only change that we are doing is changing
								// the picklist values, which are housed on the property definition
								// so reset that
								IPropertyDefinition pDef = subEle.getPropertyDefinition();
								if (pDef != null)
								{
									String pattern3 = "@values";
									Node valueNode = pModifyNode.selectSingleNode(pattern3);
									if (valueNode != null)
									{
										String value = valueNode.getText();
										if (value != null)
										{
											pDef.setValidValues(value);
										}
									}
								}
								break;
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Find the nodes in the passed in xml node that represent property elements that need to
	 * be removed from the current property element
	 * 
	 *
	 * @param pEle[in]			The current property element
	 * @param pNode[in]			The node that houses any "remove" nodes
	 *
	 * @return HRESULT
	 *
	 */
	private void processRemove(IPropertyElement pEle, Node pEleNode)
	{
		// find all of the filter op nodes of type "remove"
		String pattern = "FilterOp[@name = \'remove\']";
		
		List list = pEleNode.selectNodes(pattern);
		if (list != null)
		{
			int count = list.size();
			for (int i=0; i<count; i++)
			{
				Node pRemoveNode = (Node)list.get(i);

				// get the attribute on this filter op node of type xpath
				// this represents the name of the property element
				String pattern2 = "@xpath";
				
				Node pNameNode = pRemoveNode.selectSingleNode(pattern2);
				if (pNameNode != null)
				{
					// have the "name" of the property element that needs to be removed
					String name = pNameNode.getText();
		
					//
					// loop through the sub elements of the passed in element
					// and when we find a match on name, remove it
					//
					Vector<IPropertyElement> subEles = pEle.getSubElements();
					if (subEles != null)
					{
						int subCount = subEles.size();
						for (int j=0; j<subCount; j++)
						{
							IPropertyElement elem = subEles.elementAt(j);
							String defName = elem.getName();
							if (defName.equals(name))
							{
								subEles.remove(j);
								break;
							}
						}
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinitionFilter#filterPropertyElementBasedOnLanguage(org.netbeans.modules.uml.core.support.umlutils.IPropertyElement, java.lang.String)
	 */
	public long filterPropertyElementBasedOnLanguage(IPropertyElement pEle, String sLang)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinitionFilter#filterPropertyElementBasedOnModelElement(org.netbeans.modules.uml.core.support.umlutils.IPropertyElement, org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
	 */
	public long filterPropertyElementBasedOnModelElement(IPropertyElement pEle, IElement pElement)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Gets the xml file that defines the property definition filters
	 *
	 * @param pVal
	 *
	 * @return HRESULT
	 *
	 */
	public String getFilterFile()
	{
		String file = null;
		if (m_FilterFile.length() > 0)
		{
			file = m_FilterFile;
		}
		else
		{
			// no file specified, so use the one in the config location
			// will eventually go to preferences to get this, but for now just hardcode
			ICoreProduct prod = CoreProductManager.instance().getCoreProduct();
			if (prod != null)
			{
				IConfigManager conMan = prod.getConfigManager();
				if (conMan != null)
				{
					String home = conMan.getDefaultConfigLocation();
					file = home;
					file += "PropertyDefinitionFilters.etc"; 
				}
			}
		}
		return file;
	}

	/**
	 * Sets the xml file that defines the property definition filters
	 *
	 * @param newVal
	 *
	 * @return HRESULT
	 *
	 */
	public void setFilterFile(String value)
	{
		m_FilterFile = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinitionFilter#filterPropertyElementBasedOnFilterName(org.netbeans.modules.uml.core.support.umlutils.IPropertyElement, java.lang.String)
	 */
	public long filterPropertyElementBasedOnFilterName(IPropertyElement pEle, String sName)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Retrieves a model elements associated language.  If the model element
	 * is associated to more than one language then the first language is the 
	 * active language.
	 *
	 * @param pElement [in] The element being processed.
	 * @param pVal [out] The active language for the element.
	 */
	private ILanguage getActiveLanguage(IElement pElement)
	{
		ILanguage retLang = null;
		// Find the first language that is supported by the element.  If the 
		// is not supporting any languages (Should never happen) get the
		// default langauge.
		ETList<ILanguage> langs = pElement.getLanguages();
//		if (langs != null && langs.length > 0)
		if (langs != null && langs.size() > 0)
		{
			retLang = langs.get(0);
		}
		return retLang;
	}

	/**
	 * Gets a node from the xml document based on its name (language)
	 * 
	 *
	 * @param pDoc[in]			The xml document to search
	 * @param sLang[in]			The language string to look for
	 * @param pNode[out]			The found language xml node
	 *
	 * @return HRESULT
	 *
	 */
	private Node getLanguageFilterNode(Document doc, String langName)
	{
		// find the node that matches the passed in language
		String pattern = "//Filter[@name = \'";
		pattern += langName;
		pattern += "\']";
		Node n = doc.selectSingleNode(pattern);
		return n;
	}

	/**
	 * Get all of the xml nodes that apply to the passed in element
	 * 
	 *
	 * @param pNode[in]			The xml node whose children need to be searched
	 * @param pElement[in]		The model element to process
	 * @param pFrag[in]			The xml fragment housing the nodes that match the criteria for the element
	 *
	 * @return HRESULT
	 *
	 */
	private List getElementFilterNodes(Node pLangNode, IElement pElement)
	{
		List newList = new Vector();
		// find all of the filter element nodes
		String pattern = "FilterElement";
		List list = pLangNode.selectNodes(pattern);
		if (list != null)
		{
			for (int i=0; i<list.size(); i++)
			{
				Node node = (Node)list.get(i);

				// get the attribute on the node that is the type attribute
				// this will be the progID that the model element must support in order for the
				// filter element node to apply to the model element
				String pattern2 = "@type";
				Node pTypeNode = node.selectSingleNode(pattern2);
				if (pTypeNode != null)
				{
					String uuid = pTypeNode.getText();
					if (uuid.length() > 0)
					{
						try {
							Class clazz = Class.forName(uuid);
							if (clazz.isInstance(pElement))
							{
								newList.add(node);
							}
						} catch (Exception e)
						{
						}
					}
				}
			}
		}
		return newList;
	}

}


