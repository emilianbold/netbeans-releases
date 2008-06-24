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




package org.netbeans.modules.uml.ui.support.archivesupport;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * 
 * @author Trey Spiva
 */
public class ProductArchiveImpl implements IProductArchive
{
	private static final String BUNDLE_CLASS = "org.netbeans.modules.uml.ui.support.archivesupport.Bundle_noi18n";
	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_CLASS);
	private String m_ArchiveFilename = "";
	private Document m_Document = null;
	private boolean m_Loaded = false;
	
	//Hashtable<String, Hashtable<String, Element>> m_TableCache;
	private Hashtable m_TableCache = new Hashtable();
	
	private static String VALUESTRING = "TableIndex";
	private static String MAXVALUESTRING = "MaxIndexValue";

	public ProductArchiveImpl()
	{
		createDomDocument();
	}

	public ProductArchiveImpl(String filename)
	{
		//if the file does not exist yet, this could throw.
		try
		{
			m_Loaded = load(filename);
		}
		catch (Exception e)
		{
			//consume it.
		}
	}

	private void createDomDocument()
	{
		try 
		{
			m_Document = null;
			m_Loaded = false;
		
			String fragment = BUNDLE.getString(IProductArchiveDefinitions.IDR_ARCHIVE_HEADER);

			if (fragment != null && fragment.length() > 0 )
			{
				m_Document = XMLManip.loadXML(fragment, false);
				m_Loaded = true;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive#save(java.lang.String)
	 */
	public boolean save(String sFilename)
	{
		boolean retVal = false;
                if ((sFilename != null) && (sFilename.length() > 0))
                {
                    setArchiveFilename(sFilename);
                    if ((m_Document != null) && (m_Loaded))
                    {
                            XMLManip.savePretty(m_Document, m_ArchiveFilename);
                            retVal = true;
                    }
                }
		return retVal;
	}

	/**
	 * Temporarily loads the xml file sFilename and populates our list of archive elements.
	 *
	 * @param sFilename The name of the xml file to open
	 * @return true if the file was opened ok
	 * 
	 * @see org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive#load(java.lang.String)
	 */
	public boolean load(String sFilename)
	{
		m_Document = XMLManip.getDOMDocument(sFilename);
		if (m_Document != null)
		{
			m_Loaded = true;
			setArchiveFilename(sFilename);
		}

		return m_Loaded;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive#isLoaded()
	 */
	public boolean isLoaded()
	{		
		return getArchiveFilename() != null && getArchiveFilename().length() > 0;
	}

	/**
	 * Creates an element of id sID in the archive's list.
	 *
	 * @param sID The id of the element to be created
	 * @param pElement The returned, created archive element
	 */
	public IProductArchiveElement createElement(String sID)
	{
		return createElement(sID, false);
	}

	/**
	 * Creates an element of id sID in the archive's list.
	 *
	 * @param sID The id of the element to be created
	 * @param pElement The returned, created archive element
	 */
	public IProductArchiveElement createElement(String sID, boolean bTopOfDocument)
	{
		IProductArchiveElement retEle = null;
		if (m_Document != null && m_Loaded)
		{
			org.dom4j.Element root = m_Document.getRootElement();
			if (root != null)
			{
				// Create the element
				//org.dom4j.Element createdEle = m_Document.addElement(sID);
				org.dom4j.Node addedEle = null;
				int numNodes = 0;
				List listChildren = null;
				if (bTopOfDocument)
				{
					listChildren = root.elements();
					if (listChildren != null)
					{
						numNodes = listChildren.size();
					}
				}
				
				// Add to the top of the document if the user requested it
				if (numNodes > 0)
				{
					Element ele = (Element)listChildren.get(0);
					addedEle = root.addElement(sID);
				}
				else
				{
					addedEle = root.addElement(sID);
				}
				
				if (addedEle != null && addedEle instanceof Element)
				{
					Element pAddedElement = (Element)addedEle;
					retEle = new ProductArchiveElementImpl();
					retEle.setDOMElement(pAddedElement);
				}
			}
		}
		return retEle;
	}

	/**
	 * Returns the archive element that has this id.  Note that since archive elements can contain other
	 * archive elements the user should be aware that this is not a deep search.  Only the elements
	 * directly off the archive will be searched.
	 *
	 * @param sID The id of the element to search for
	 * @return The returned archive element, if found that matches the above id
	 * @see org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive#getElement(java.lang.String)
	 */
	public IProductArchiveElement getElement(String sID)
	{
		if (m_Document != null && m_Loaded && sID != null && sID.length() > 0)
		{
			Element root = m_Document.getRootElement();
			if (root != null)
			{
				return getElement(root, sID);
			}
		}

		return null;
	}

	/**
	 *
	 * Locates and then creates an IProductArchiveElement.
	 *
	 * @param node[in] The node to begin the search from
	 * @param sID[in] The name of the node to find
	 * @param pElement[out] The found element, else 0
	 *
	 * @return HRESULT
	 *
	 */
	public IProductArchiveElement getElement(Node node, String sID)
	{
		if (node != null)
		{
			String query = "./" + sID;
			Node pNode = node.selectSingleNode(query);
			if (pNode != null)
			{
				IProductArchiveElement pElement = new ProductArchiveElementImpl();
				if (pNode.getNodeType() == Node.ELEMENT_NODE)
				{
					pElement.setDOMElement((Element)pNode);
				}
				return pElement;
			}
		}
		return null;
	}

	/**
	 * Locates and then creates an IProductArchiveElement.
	 *
	 * @param node The node to begin the search from
	 * @param sID The name of the node to find
	 * @return pElement[out] The found element, else null is returned.
	 */
	public static IProductArchiveElement getElement(Element node, String sID)
	{
		if (node != null && sID != null && sID.length() > 0)
		{
			String query = "./" + sID;
			Node foundNode = XMLManip.selectSingleNode(node, query);
			if (foundNode instanceof Element)
			{
				return new ProductArchiveElementImpl((Element) foundNode);
			}
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive#getElementsFromModelElement(java.lang.String)
	 */
	public ETList<IProductArchiveElement> getElementsFromModelElement(String sElementID)
	{
		ETList<IProductArchiveElement> retObj = null;

		// Do a query to find the elements
		String query = "*[@";
		query += IProductArchiveDefinitions.MEID_STRING;
		query += "=\"";
		query += sElementID;
		query += "\"]";
		
		if (m_Document != null)
		{
			Element root = m_Document.getRootElement();
			if (root != null)
			{
				List nodeList = root.selectNodes(query);
				if (nodeList != null)
				{
					int count = nodeList.size();
					for (int i=0; i<count; i++)
					{
						Node node = (Node)nodeList.get(i);
						IProductArchiveElement foundEle = new ProductArchiveElementImpl();
						if (node.getNodeType() == Node.ELEMENT_NODE)
						{
							foundEle.setDOMElement((Element)node);
							retObj.add(foundEle);
						}
					}
				}
			}
		}
		
		return retObj;
	}

	/**
	 * Returns the list of toplevel archive elements.  Note that archive elements could contain other
	 * archive elements.
	 *
	 * @param pVal[out] The returns list of archive elements
	 */
	public ETList<IProductArchiveElement> getElements()
	{
		ETList<IProductArchiveElement> retObj = null;
		if (m_Document != null && m_Loaded)
		{
			retObj = new ETArrayList<IProductArchiveElement>();
			Element rootEle = m_Document.getRootElement();
			if (rootEle != null)
			{
				List children = rootEle.elements();
				if (children != null)
				{
					int count = children.size();
					for (int i=0; i<count; i++)
					{
						Element node = (Element)children.get(i);
						IProductArchiveElement foundEle = new ProductArchiveElementImpl();
						foundEle.setDOMElement(node);
						retObj.add(foundEle);
					}
				}
			}
		}
		return retObj;
	}

	/**
	 * Inserts an item into an index table.
	 *
	 * @param sTableName [in] The name of the table
	 * @param sTableEntry [in] The value of the table entry
	 * @param pKey [in] The returned key value
	 * @param pCreatedElement [out,retval] The created productarchive element
	 */
	public ETPairT<IProductArchiveElement, Integer> insertIntoTable(String sTableName, 
										String sTableEntry)
	{
		int pKey = 0;
		IProductArchiveElement retElement = null;
		// See if the table already exists
		IProductArchiveElement retEle = getElement(sTableName);
		if (retEle == null)
		{
			retEle = createElement(sTableName, true);
		}
		
		if (retEle != null)
		{
			boolean created = false;
			long maxVal = retEle.getAttributeLong(MAXVALUESTRING);
			IProductArchiveElement pElement = retEle.getElement(sTableEntry);
			
			if (pElement != null)
			{
				// See if it's marked deleted
				if (pElement.getAttributeBool(IProductArchiveDefinitions.TABLE_ENTRY_DELETED))
				{
					// The entry is there, but it's deleted so undelete it
					pElement.removeAttribute(IProductArchiveDefinitions.TABLE_ENTRY_DELETED);
				}
			}
			else
			{
				pElement = retEle.createElement(sTableEntry);
				maxVal++;
				retEle.addAttributeLong(MAXVALUESTRING, (int)maxVal);
				pElement.addAttributeLong(VALUESTRING, (int)maxVal);
				created = true;
			}
			
			if (pElement != null)
			{
				IProductArchiveAttribute pAttribute = null;

				pKey = (int)pElement.getAttributeLong(VALUESTRING);
				retElement = pElement;
			}
		}
		
		return new ETPairT<IProductArchiveElement, Integer>(retElement, new Integer(pKey));
	}

	/**
	 * Inserts sTableEntry into the table sTableName and creates an attribute named 
	 * sIndexAttributeName on pElementToAddIndexTo with the key value.
	 *
	 * @param sTableName [in] The name of the table
	 * @param sTableEntry [in] The value of the table entry
	 * @param sIndexAttributeName [in] The name of the attribute to receive the index
	 * @param pElementToAddIndexTo [in] The element where the attribute key should be added.
	 * @param pCreatedElement [out,retval] The created productarchive element
	 */
	public IProductArchiveAttribute insertIntoTable(
		String sTableName,
		String sTableEntry,
		String sIndexAttributeName,
		IProductArchiveElement pElementToAddIndexTo)
	{
		IProductArchiveAttribute pCreatedAttr = null;
		
		ETPairT<IProductArchiveElement, Integer> val = insertIntoTable(sTableName, sTableEntry);
		int nKey = ((Integer)val.getParamTwo()).intValue();
		IProductArchiveElement tempEle = val.getParamOne();
		if (tempEle != null)
		{
			pCreatedAttr = pElementToAddIndexTo.addAttributeLong(sIndexAttributeName, nKey);
		}
		
		return pCreatedAttr;
	}

	/**
	 * Removes an item by index from the table.
	 * @param sTableName [in] The name of the table
	 * @param sTableEntry [in] The value of the table entry
	 * @param bSuccessfullyRemoved [out,retval] TRUE if the element was successfully removed
	 */
	public boolean removeFromTable(String sTableName, String sTableEntry)
	{
		IProductArchiveElement pArchiveElement = getTableEntry(sTableName, sTableEntry);
		if (pArchiveElement != null)
		{
			// Mark this guy as deleted
			pArchiveElement.addAttributeBool(IProductArchiveDefinitions.TABLE_ENTRY_DELETED, true);
		}
		return true;
	}

	/**
	 * Gets a string from the table based on the index.
	 *
	 * @param sTableName [in] The name of the table
	 * @param nTableEntry [in] The entry we're looking for
	 * @param bEntryDeleted [out] Has this entry been deleted (happens when a delete happens and a diagram
	 * is closed.
	 * @param pFoundElement [out] The found element
	 */
	public IProductArchiveElement getTableEntry(String sTableName, int nTableEntry)
	{
		if (m_Document != null && m_Loaded)
		{
			Element rootNode = m_Document.getRootElement();
			if (rootNode != null)
			{
				String buffer = sTableName + "/*[@" + VALUESTRING + "=" + nTableEntry + "]";
				Node node = rootNode.selectSingleNode(buffer);
				if (node != null)
				{
					IProductArchiveElement retEle = new ProductArchiveElementImpl();
					if (node.getNodeType() == Node.ELEMENT_NODE)
					{
						retEle.setDOMElement((Element)node);
						
					}
					return retEle;
				}
			}
		}
		return null;
	}
	
	public IProductArchiveElement getTableEntry(String sTableName, String sTableEntry)
	{
		IProductArchiveElement retVal = null;

		Element root = m_Document.getRootElement();
		Element tableElement = null;

		if (root != null)
		{
			for (int i = 0, rootNodeCount = root.nodeCount(); i < rootNodeCount; i++)
			{
				Node rootChildNode = root.node(i);
				if (rootChildNode.getName() != null)
				{
					if (rootChildNode.getName().equals(sTableName) && rootChildNode instanceof Element)
					{
						tableElement = (Element) rootChildNode;

						for (int x = 0, tableElementCount = tableElement.nodeCount(); x < tableElementCount; x++)
						{
							Node foundElement = tableElement.node(x);
							if (foundElement instanceof Element)
							{
								Element e = (Element) foundElement;
								String TableEntryValue = e.getName();
								if (TableEntryValue != null && TableEntryValue.equals(sTableEntry))
								{
									retVal = new ProductArchiveElementImpl(e);
									break;
								}
							}
						}
						if (retVal != null)
						{
							break;
						}
					}
				}
			}

		}

		return retVal;
	}

	/**
	 * Gets an item from an index table.
	 *
	 * @param sTableName [in] The name of the table
	 * @param nTableEntry [in] The entry we're looking for
	 * @param bEntryDeleted [out] Has this entry been deleted (happens when a delete happens and a diagram
	 * is closed.
	 * @param pKey [out] The found key
	 * @param pFoundElement [out] The found element
	 */
	public IProductArchiveElement getTableEntry(String sTableName, String sTableEntry, int pKey)
	{
		IProductArchiveElement retEle = null;
		if (m_Document != null && m_Loaded)
		{
			String query = sTableName + '/' + sTableEntry;
			Node node = m_Document.selectSingleNode(query);
			if (node != null && node.getNodeType() == Node.ELEMENT_NODE)
			{
				String key = XMLManip.getAttributeValue(node, VALUESTRING);
				retEle = new ProductArchiveElementImpl();
				retEle.setDOMElement((Element)node);
			}
		}
		return retEle;
	}

	/**
	 * Find sAttributeName in pArchiveElement, then look into the table sTableName and 
	 * return the pFoundElement.
	 * 
	 * @param pArchiveElement
	 * @param sAttributeName
	 * @param sTableName
	 * @param pFOundElement
	 * @param sFoundElementID
	 */
	public ETPairT<IProductArchiveElement,String> getTableEntry(IProductArchiveElement pArchiveElement, 
												String sAttributeName, String sTableName)
	{
		IProductArchiveElement retEle = null;
		String retStr = null;
		if (pArchiveElement != null)
		{
			Element pSelf = pArchiveElement.getDOMElement();
			if (pSelf != null)
			{
				String attrVal = XMLManip.getAttributeValue(pSelf, sAttributeName);
				if (attrVal != null && attrVal.length() > 0)
				{
					boolean foundInCache = false;
				
					// First see if the entry is in our cache
					Object obj = m_TableCache.get(sTableName);
					if (obj != null)
					{
						if (obj instanceof Hashtable)
						{
							Hashtable table = (Hashtable)obj;
							Object obj1 = table.get(attrVal);
							if (obj1 != null && obj1 instanceof Element)
							{
                        Element element = (Element)obj1;
								retEle = new ProductArchiveElementImpl();
								retEle.setDOMElement(element);
                        retStr = element.getName();
								foundInCache = true;
							}
						}
					}
				
					if (!foundInCache)
					{
						String query = "//";
						query += sTableName;
						query += "/*[@";
						query += VALUESTRING;
						query += "=";
						query += attrVal;
						query += "]";
					
						Node node = pSelf.selectSingleNode(query);
						if (node != null)
						{
							if (node.getNodeType() == Node.ELEMENT_NODE)
							{
								Element domEle = (Element)node;
								retEle = new ProductArchiveElementImpl();
								retEle.setDOMElement(domEle);

								retStr = node.getName();

								Hashtable newTable;
								Object existing = m_TableCache.get(sTableName);
								if (existing != null && existing instanceof Hashtable)
								{
								    newTable = (Hashtable)existing;
								} 
								else 
								{
								    newTable = new Hashtable();
								}
								newTable.put(attrVal, domEle);
								m_TableCache.put(sTableName, newTable);
							}
						}
					}
				}
			}
		}
		return new ETPairT<IProductArchiveElement, String>(retEle, retStr);
	}

	/**
	 * Retrieve the name of the archive file.
	 * 
	 * @return The name of the file. 
	 */
	public String getArchiveFilename()
	{
		return m_ArchiveFilename;
	}

	/**
	 * Sets the name of the archive file.
	 * 
	 * @param value The name of the file. 
	 */
	public void setArchiveFilename(String value)
	{
		m_ArchiveFilename = value;
	}

	/**
	 * Based on the table name and index this routine returns all the table entries that aren't deleted.
	 */
	public ETList<IProductArchiveElement> getAllTableEntries(String tableName)
	{
		ETList<IProductArchiveElement> retObj = new ETArrayList<IProductArchiveElement>();
		int key = 1;
		IProductArchiveElement foundEle = getTableEntry(tableName, key);
		while (foundEle != null)
		{
			if ( !IsElementDeleted( foundEle ) )
			{
				retObj.add( foundEle );
			}
			key++;
			foundEle = getTableEntry(tableName, key);
		}
		return retObj;
	}
   
   protected boolean IsElementDeleted( IProductArchiveElement element )
   {
      return element != null &&
           element.getAttribute(IProductArchiveDefinitions.TABLE_ENTRY_DELETED) != null;
   }
   
      public IProductArchiveElement getDiagramElement(String sID)
    {
        if (m_Document != null && m_Loaded && sID != null && sID.length() > 0)
        {
            Element root = m_Document.getRootElement();
            if (root != null)
            {
                String query = ".//" + sID;
                Node foundNode = XMLManip.selectSingleNode(root, query);
                if (foundNode instanceof Element)
                {
                    return new ProductArchiveElementImpl((Element) foundNode);
                }
            }
        }
        return null;
    }

}
