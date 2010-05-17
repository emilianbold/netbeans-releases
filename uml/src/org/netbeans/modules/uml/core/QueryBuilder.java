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


package org.netbeans.modules.uml.core;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;

/**
 * @author sumitabhk
 *
 */
public class QueryBuilder implements IQueryBuilder
{
	private String m_SchemaLocation = "";
	private String m_ProjID = "";
	private String m_DefaultUpdaterProgId = "";
	private String m_ResultLocation = "";

	/**
	 *
	 * Sets the location of the QuerySchemas.etc file. The schema file
	 * does not have to be the QuerySchemas.etc file, but is must have
	 * the same format.
	 *
	 * @param newVal[in] The absolute location of the file to use
	 *                   as a schema from which queries can be built
	 *
	 * @return HRESULT
	 *
	 */
	public void setSchemaLocation(String pVal)
	{
		m_SchemaLocation = pVal;
	}

	/**
	 *
	 * Retrieves the location of the QuerySchemas.etc file
	 * that was previously set.
	 *
	 * @param pVal[out] The current value
	 *
	 * @return 
	 *
	 */
	public String getSchemaLocation()
	{
		return m_SchemaLocation;
	}

	/**
	 *
	 * Generates out new results given the set schema file and the passed
	 * in data node. This routine will create new data. No checking of existing
	 * data is made.
	 *
	 * @param dataSourceNode[in]  The node that all queries defined in the 
	 *        schema file are run against.
	 * @param results[out]        The xml document that holds the results
	 *
	 * @return HRESULT
	 *
	 */
	public Document generateResults(Node node)
	{
		Document retDoc = null;
		if (node != null)
		{
			m_DefaultUpdaterProgId = "";
			verifyBuildEnvironment();
			Document doc = loadSchema();
         
         if (doc != null)
			{
				Element root = doc.getRootElement();
				if (root != null)
				{
					m_DefaultUpdaterProgId = XMLManip.getAttributeValue(root, "defaultUpdater");
				}
				
				// Gather the Query elements in the Schema file
				List nodes = doc.selectNodes("//Query");
				if (nodes != null)
				{
					int count = nodes.size();
					if (count > 0)
					{
						// Create the document that will house the results
						Document resultDoc = XMLManip.getDOMDocument();
						if (resultDoc != null)
						{
							// Create the processing instruction to make the file an xml doc
							//resultDoc.addProcessingInstruction("xml", "version = '1.0'");
							//Element rootEle = XMLManip.createElement(resultDoc, "xml");
							//Element attr = XMLManip.createElement(rootEle, "version = '1.0'");
							//rootEle.add(attr);
							
							//resultDoc.add(rootEle);
							Element cachedQueriesNode = XMLManip.createElement(resultDoc, "CachedQueries");
							if (cachedQueriesNode != null)
							{
								for (int i=0; i<count; i++)
								{
									Node childNode = (Node)nodes.get(i);

									// OK, we've got our first Query schema, so let's process it.
									processQuery(node, childNode, cachedQueriesNode);
								}
							}
							retDoc = resultDoc;
						}
					}
				}
			}
		}
		return retDoc;
	}

	/**
	 *
	 * Executes a query defined in the schemaNode passed in against the dataSourceNode also
	 * passed in.
	 *
	 * @param dataSourceNode[in]  The node holding the data to query against
	 * @param schemaNode[in]      The node holding the query details
	 * @param resultParent[in]    The parent element that will hold the results of the executed
	 *                            queries.
	 *
	 * @return HRESULT
	 *
	 */
	private void processQuery(Node dataSourceNode, Node schemaNode, Element resultParent)
	{
		QuerySchema querySchema = buildSchemaNode(schemaNode);
		
		// Let's retrieve the query string first, execute, and if we have any results,
		// then we'll build the resultant elements
		if (querySchema.validate())
		{
			List dataNodes = dataSourceNode.selectNodes(querySchema.m_MainQuery);
			if (dataNodes != null)
			{
				int count = dataNodes.size();
				if (count > 0)
				{
					Node resultContNode = querySchema.createResultContainer(resultParent);
					if (resultContNode != null)
					{
						for (int i=0; i<count; i++)
						{
							Node dataNode = (Node)dataNodes.get(i);
							Node resultNode = querySchema.createResultNode(resultContNode, dataNode);
						}
					}
				}
			}
		}
	}

	/**
	 *
	 * Gather the details of a particular query defined in the QuerySchema file.
	 *
	 * @param rawSchemaNode[in]   The schema node pulled from the QuerySchema file
	 * @param schemaNode[out]     The QuerySchema object packed with data pulled form rawSchemaNode
	 *
	 * @return HRESULT
	 *
	 */
	private QuerySchema buildSchemaNode(Node rawSchemaNode)
	{
		QuerySchema schemaNode = new QuerySchema();
		schemaNode.m_Project = getProject();
		schemaNode.m_resultContainerName = XMLManip.getAttributeValue(rawSchemaNode, "name");
		schemaNode.m_MainQuery = XMLManip.getAttributeValue(rawSchemaNode, "locate");
		schemaNode.m_Updater = XMLManip.getAttributeValue(rawSchemaNode, "updater");
		
		// Ok, we've got the outer result element that will contain all the
		// individual results. Now let's determine the name of the individual
		// result element types, as well as the xml attributes to put on those
		// elements
		Node queryResultNode = rawSchemaNode.selectSingleNode("./QueryResult");
		if (queryResultNode != null)
		{
			String includeNodeName = XMLManip.getAttributeValue(queryResultNode, "includeNodeType");
			schemaNode.m_ResultNodeName = XMLManip.getAttributeValue(queryResultNode, "name");
			if (includeNodeName != null && includeNodeName.equals("true"))
			{
				// A simple flag that instructs us to include the name of the node as an xml attribute
				// on each of the result elements
				schemaNode.m_IncludeNodeName = true;
			}
			
			// Now determine how many xml attributes to create
			List resultAttrs = queryResultNode.selectNodes("./ResultAttr");
			if (resultAttrs != null)
			{
				int count = resultAttrs.size();
				for (int i=0; i<count; i++)
				{
					Node resultAttr = (Node)resultAttrs.get(i);
					String nameOfAttr = XMLManip.getAttributeValue(resultAttr, "name");
					String queryForAttr = XMLManip.getAttributeValue(resultAttr, "query");
					String isUnique = XMLManip.getAttributeValue(resultAttr, "isUnique");
					String isProperty = XMLManip.getAttributeValue(resultAttr, "isProperty");
					schemaNode.m_ResultAttrs.add(new ResultAttr(nameOfAttr, queryForAttr,
																isUnique, isProperty));
				}
			}
		}
		
		return schemaNode;
	}

	/**
	 *
	 * Loads the schema file that defines the queries to 
	 * execute
	 *
	 * @param doc[out] The document that contains the specified queries
	 *                 to execute
	 *
	 * @return HRESULT
	 *
	 */
	private Document loadSchema()
	{
		Document retDoc = null;
		if (m_SchemaLocation != null)
		{
			retDoc = XMLManip.getDOMDocument(m_SchemaLocation);
			if (retDoc == null)
			{
				//Throw parse exception
			}
		}
		return retDoc;
	}

	/**
	 *
	 * Checks to see if the schema and result location properties are validly
	 * set.
	 *
	 * @return QB_E_BAD_SCHEMA_LOCATION if the SchemaLocation property is not
	 *         set to a valid xml file.
	 *         QB_E_BAD_RESULT_LOCATION if the ResultLocation does not point
	 *         at a valid location to create a new results file,
	 *         else S_OK
	 *
	 */
	private void verifyBuildEnvironment()
	{
		File file = new File(m_SchemaLocation);
		if (!file.exists())
		{
			//Show an error to the user that IDS_BAD_SCHEMA_LOCATION
		}
	}

	public void setProjectId(String pVal)
	{
		m_ProjID = pVal;
	}

	public String getProjectId()
	{
		return m_ProjID;
	}

	public String getDefaultUpdaterProgId()
	{
		return m_DefaultUpdaterProgId;
	}
	
	private IProject getProject()
	{
		IProject retProj = null;
		ICoreProduct prod = ProductRetriever.retrieveProduct();
		if (prod != null)
		{
			IApplication app = prod.getApplication();
			if (app != null)
			{
				retProj = app.getProjectByID(m_ProjID);
			}
		}
		return retProj;
	}

	private class ResultAttr
	{
		//Name of the xml attribute
		private String m_AttrName = "";
		//Query to pack the attribute with( i.e., the result of the query )
		private String m_Query = "";
		//true if the particular xml attribute can be used as a search id for updates
		private boolean m_IsUnique = false;
		//true if the m_Query is the name of a Property to execute
		private boolean m_PropertyGet = false;
		
		public ResultAttr(String name, String query, String isUnique, String isProperty)
		{
			m_AttrName = name;
			m_Query = query;
			if (isUnique != null && isUnique.equals("true"))
			{
				m_IsUnique = true;
			}
			if (isProperty != null && isProperty.equals("true"))
			{
				m_PropertyGet = true;
			}
		}
	}
	
	private class QuerySchema
	{
		//Name of the outer xml element that houses the results of the query
		private String m_resultContainerName = "";
		//Name of the outer xml element that houses the results of the query
		private boolean m_IncludeNodeName = false;
		//The query to perform on the data source
		private String m_MainQuery = "";
		//Name of the element to house each result. One per found result node
		private String m_ResultNodeName = "";
		//The progid of the IQueryUpdater object that will be loaded by the QueryManager
		private String m_Updater = "";
		//The xml attributes and queries for each result node
		private Vector/*<ResultAttr>*/ m_ResultAttrs = new Vector();
		private IProject m_Project = null;
		
		/**
		 *
		 * Makes sure that the QuerySchema object is in a valid state
		 *
		 * @return true if data is good, else false
		 *
		 */
		public boolean validate()
		{
			boolean isValid = false;
			if (m_resultContainerName != null && m_resultContainerName.length() > 0)
			{
				if (m_ResultNodeName != null && m_ResultNodeName.length() > 0)
				{
					if (m_MainQuery != null && m_MainQuery.length() > 0)
					{
						isValid = true;
					}
				}
			}
			return isValid;
		}
		
		/**
		 *
		 * Creates the Xml element that is the result container for the individual result
		 * elements to be created
		 *
		 * @param resultParent[in] The parent to own the result container. The returned container
		 *                         element will be properly parented to resultParent if successful.
		 * @param container[out]   The new element
		 *
		 * @return HRESULT
		 *
		 */
		public Node createResultContainer(Element resultParent)
		{
			Node retNode = null;
			retNode = XMLManip.createElement(resultParent, m_resultContainerName);
			if (retNode != null)
			{
				XMLManip.setAttributeValue(retNode, "updater", m_Updater);
			}
			return retNode;
		}
		
		/**
		 *
		 * Creates a new node that is placed as a child on container, and fills in the appropriate xml attributes
		 * based on the data found in dataNode
		 *
		 * @param container[in]    The xml element that will own the new node
		 * @param dataNode[in]     The data node that data is getting pulled from
		 * @param resultNode[out]  The new node, properly packed with appropriate data.
		 *
		 * @return HRESULT
		 *
		 */
		private Node createResultNode(Node container, Node dataNode)
		{
			Node retNode = null;
			if (m_ResultAttrs != null && m_ResultAttrs.size() > 0)
			{
				if (container.getNodeType() == Node.ELEMENT_NODE)
				{
					retNode = XMLManip.createElement((Element)container, m_ResultNodeName);
				}
				if (retNode != null)
				{
					// If we had been told to include the node name, do it now
					if (m_IncludeNodeName)
					{
						XMLManip.setAttributeValue(retNode, "nodeType_", XMLManip.retrieveSimpleName(dataNode));
					}
					ITypeManager typeMan = null;
					if (m_Project != null)
					{
						typeMan = m_Project.getTypeManager();
					}
					
					int count = m_ResultAttrs.size();
					for (int i=0; i<count; i++)
					{
						String result = "";
						ResultAttr iter = (ResultAttr)m_ResultAttrs.get(i);
						if (typeMan != null && iter.m_PropertyGet)
						{
							String xmiid = XMLManip.getAttributeValue(dataNode, "xmi.id");
							if (xmiid != null && xmiid.length() > 0)
							{
								// We encountered a problem where system upgrade created a bogus node type and
								// we threw at this location.  So rather then abort the entire loop, just skip
								// over anything that fails to Get
								IVersionableElement ver = typeMan.getElementByID(xmiid);
								if (ver != null && ver instanceof IElement)
								{
									try
									{
										Class clazz = ver.getClass();
										Method meth = null;
										Method[] meths = clazz.getMethods();
										if (meths != null)
										{
											for (int j=0; j<meths.length; j++)
											{
												Method method1 = meths[j];
												if (method1.getName().equals(iter.m_Query))
												{
													meth = method1;
													break;
												}
											}
										}
										if (meth != null)
										{
											Object obj = meth.invoke(ver, (Object[])null);
											if (obj != null && obj instanceof String)
											{
												result = (String)obj;
											}
										}
									}
									catch (Exception e)
									{
										e.printStackTrace();
									}
								}
							}
						}
						else
						{
							Node tempNode = dataNode.selectSingleNode(iter.m_Query);
							if (tempNode != null && tempNode.getNodeType() == Node.ATTRIBUTE_NODE)
							{
								result = tempNode.getStringValue();
							}
						}
						
						if (result != null && result.length() > 0)
						{
                            String attrname=iter.m_AttrName;
							XMLManip.setAttributeValue(retNode, attrname , result);
						}
					}
				}
			}
			return retNode;
		}
	}
}


