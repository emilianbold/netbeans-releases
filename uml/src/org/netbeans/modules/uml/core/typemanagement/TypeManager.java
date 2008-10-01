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

package org.netbeans.modules.uml.core.typemanagement;

import org.netbeans.modules.uml.core.QueryManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.IDResolver;
import org.dom4j.Node;


import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ElementCollector;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ExternalFileManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementChangeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespaceModifiedEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IPart;
import org.netbeans.modules.uml.core.metamodel.profiles.Profile;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.FileManip;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.PathManip;
import org.netbeans.modules.uml.core.support.umlsupport.PreventReEntrance;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.Strings;
import org.netbeans.modules.uml.core.support.umlsupport.URILocator;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.core.workspacemanagement.WorkspaceManagementException;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.openide.util.Exceptions;

/**
 */
public class TypeManager implements ITypeManager, IElementLifeTimeEventsSink,
    INamedElementEventsSink, INamespaceModifiedEventsSink, 
    IExternalElementEventsSink
{
	/**
	 *
	 * Retrieves the Project this TypeManager is associated with.
	 *
	 * @return pVal[out] The IProject.
	 */
    public IProject getProject()
    {
        return m_Project;
    }

	/**
	 *
	 * Associates this TypeManager to the passed in Project.
	 *
	 * @param newVal[in] The Project to associate with.
	 */
    public void setProject(IProject project)
    {
        revokeEventSinks();
        clearDeletedIDs();
        
        m_Project = project;
        if (m_Project != null)
        {
            m_ProjectID         = m_Project.getXMIID();
            m_ProjectFileName   = m_Project.getFileName();
            
            if (!verifyTypeFile(m_Project))
            {
                establishTypeFile(m_Project);
                m_Project.setChildrenDirty(true);
            }
            else
            {
                connectToTypeFile();
            }
            
            loadUnresolvedTypes();
            registerForEvents();
            
            // pick list manager was removed by revokeEventSinks method - a new instance will be created
            String name = project.getFileName();
            name = StringUtilities.getPath(name) + QueryManager.QUERY_CACHE;
            Document document = XMLManip.getDOMDocument(name);
            if (document != null) {
                PickListManager pickManager = new PickListManager();
                pickManager.initialize(project, document);
                m_rawPickManager = pickManager;
            }
        }
        else
        {
            saveUnresolvedTypes();
        }
    }
    
	/**
	 *
	 * Creates the .ettm file and populate it with information
	 * retrieved from the passed in Project
	 *
	 * @param proj[in] The IProject
	 */
    private void establishTypeFile(IProject project)
    {
        try
        {
            String typeXML = UMLXMLManip.retrieveXMLFragmentFromResource(
                getClass(), 
                TypeManager.IDR_TYPE_XML);
            if (typeXML != null && typeXML.length() > 0)
            {
            	Log.out("The xml to be load " + typeXML);
               
                IDResolver resolver = new IDResolver();
                resolver.addNodeTypeId("Location", "locID");
                resolver.addNodeTypeId("Type", "id");
                resolver.addNodeTypeId("TypeManagement", "projectID");
                m_Doc = XMLManip.loadXML(typeXML, resolver);
                setProjectID(project);
                gatherTypeInformation(project);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    private void setProjectID(IProject project)
    {
        Element elem = getTypeManagementElement();
        if (elem != null)
            XMLManip.setAttributeValue(elem, "projectID", project.getXMIID());
    }
    
    private Element getTypeManagementElement()
    {
        if (m_Doc != null)
        {
            Node n = m_Doc.selectSingleNode("TypeManagement");
            if (n instanceof Element)
                return (Element) n;
        }
        return null;
    }
    
	/**
	 *
	 * Gathers the necessary type information to initially populate
	 * the passed in document.
	 *
	 * @param proj[in] The Project to pull the information from.
	 * @warning m_Doc must have been established before this call.
	 */
    private void gatherTypeInformation(IProject project)
    {
        Node projectNode = project.getNode();
        if (projectNode != null)
        {
            loadAllExternalElements();
            
            // TODO: This stuff needs to be done to support external elements.
//            CComPtr< IXMLDOMNodeList > nodes;
//            ExternalFileManager manager;
//            _VH( manager.GetExternalElements( projNode, &nodes ));
//              if( nodes )
//              {
//                  USES_CONVERSION;
//
//                  long numNodes;
//                  _VH( nodes->get_length( &numNodes ));
//
//                  for( long x = 0; x < numNodes; x++ )
//                  {
//                  CComPtr< IXMLDOMNode > node;
//                      _VH( nodes->get_item( x, &node ));
//                  ATLASSERT( node );
//
//                  if( node )
//                  {
//                     _VH( PopulateFromExternalNodes( node ));
//                  }
//               }
//            }
        }
    }
    
    private void loadAllExternalElements()
    {
        // TODO: This will become relevant once we start parking elements in
        //       .etx files.
        if (m_Project != null)
        {
        	Node node = m_Project.getNode();
        	loadAllExternalElements(node);
        }
    }
    
	/**
	 *
	 * Loads all elements that contain an external reference ( @href ) to
	 * another file, most likely a .etx
	 *
	 * @param nodeToSearch[in]   The node to expand.
	 *
	 * @return HRESULT
	 *
	 */
	private void loadAllExternalElements(Node node)
	{
		if (node != null)
		{
			Node searchNode = node;
			String xmiid = XMLManip.getAttributeValue(node, "xmi.id");
			String nodeName = node.getNodeTypeName();
			ExternalFileManager man = new ExternalFileManager();
			Node resolved = null;
			if (nodeName != null)
			{
				if (nodeName.equals("UML:Project"))
				{
					resolved = node;
				}
				else
				{
					resolved = resolveExternalNode(man, node, xmiid);
				}
			}
			
			if (resolved != null)
			{
				searchNode = resolved;
			}
			
			String query = ".//*[@href]";
			List nodes = searchNode.selectNodes(query);
			if (nodes != null)
			{
				int count = nodes.size();
				for (int i=0; i<count; i++)
				{
					Node curNode = (Node)nodes.get(i);
					if (man.isImportedElement(curNode))
					{
						addExternalElement(curNode);
					}
					else
					{
						loadAllExternalElements(curNode);
					}
				}
			}
		}
	}

	/**
	 *
	 * Resolves the passed in node, loading it either from disk for from another in-memory node from another
	 * project, depending on whether or not that node is imported or not
	 *
	 * @param fileMan[in]      The file manager to use for most resolution
	 * @param pNode[in]        The node to resolve
	 * @param finalNode[out]   The resulting node. This could be pNode if no resolution was necessary. However,
	 *                         regardless, this will always return a result, unless an error occurs.
	 *
	 * @return HRESULT
	 *
	 */
	private Node resolveExternalNode( ExternalFileManager fileMan, 
										   Node pNode, 
										   final String xmiID )
	{
		Node finalNode = null;
		FactoryRetriever ret = FactoryRetriever.instance();
		IVersionableElement verEle = null;
		
		// Let's see if the object is in memory first of all 
		verEle = ret.retrieveObject(retrieveRawXMIID(xmiID));
		if (verEle != null)
		{
			Node node = verEle.getNode();
			if (node != null)
			{
				Node modifiedNode = verifyParentsLoaded(fileMan, pNode);
				
				if (modifiedNode == null)
				{
					modifiedNode = pNode;
				}
				
				finalNode = fileMan.resolveExternalNode(modifiedNode);
				if (finalNode == null)
				{
					finalNode = modifiedNode;
				}
			}
		}
		if (finalNode == null)
		{
			// First determine whether or not the node is imported or not.
			// If it is imported, we need to check the Project that the node is in, in order
			// to see if that node is in memory. We want to do this instead of just pulling
			// the node from disk, as the in-memory node could contain newer data
			ImportInfo info = null;
			if (pNode == null && xmiID.length() > 0)
			{
				info = getImportInfo(xmiID);
				if ((info != null) && (info.isImported() == true))
				{
					finalNode = loadNodeFromExternalProject(xmiID, info);
				}
			}
			else
			{
				info = getImportInfo(pNode);
				if ((info != null) && (info.isImported() == true))
				{
					// Let's see if the node is in memory
					IProject pProj = getProjectByID(info.m_ProjectID);
					if (pProj != null)
					{
						ITypeManager pTypeMan = pProj.getTypeManager();
						if (pTypeMan != null)
						{
							String xmiid = XMLManip.getAttributeValue(pNode, "xmi.id");
							IVersionableElement verElement = pTypeMan.getElementByID(xmiid);
							
							if (verElement != null)
							{
								finalNode = verElement.getNode();
							}
						}
					}
				}
				else
				{
					Node modifiedNode = verifyParentsLoaded(fileMan, pNode);
					if (modifiedNode == null)
					{
						modifiedNode = pNode;
					}
					finalNode = fileMan.resolveExternalNode(modifiedNode);
				}
			}
		}
		
		// If the node wasn't resolved and not imported,
		// The just return the node that was passed in
		if (finalNode == null && pNode != null)
		{
			finalNode = pNode;
		}
		
		return finalNode;
	}

	/**
	 * @see GetImportInfo()
	 */
	private ImportInfo getImportInfo(String id)
	{
		ImportInfo retInfo = null;
	   Node locationNode = getLocationNodeFromXMIID( id );

	   if( locationNode != null )
	   {
		  retInfo = populateImportInfo( locationNode);
	   }

	   return retInfo;
	}

	/**
	 *
	 * Retrieves the import information of the passed in elementNode. If the element
	 * is not an element that has been imported into the Project this TypeManager is
	 * associated with, then false is returned and the ImportInfo object passed in
	 * is undefined
	 *
	 * @param elementNode[in]  The elemnet to query the .ettm file with
	 * @param info[out]        The info
	 *
	 * @return true if the element has been imported, else false
	 *
	 */
	private ImportInfo getImportInfo(Node elementNode)
	{
		ImportInfo retObj = null;
		if (elementNode != null)
		{
			Node locationNode = getLocationNodeFromXMIID(elementNode);
			if (locationNode != null)
			{
				retObj = populateImportInfo(locationNode);
			}
		}
		return retObj;
	}

	/**
	 *
	 * Retrieves the node in the .ettm file that corresponds to the Location node associated
	 * with the XMI id of the element passed in.
	 *
	 * @param elementNode[in]     The element whose xmi id will be used to query
	 * @param locationNode[out]   The found Location node, else 0
	 *
	 * @return HRESULT
	 *
	 */
	private Node getLocationNodeFromXMIID(Node elementNode)
	{
		Node retNode = null;
		String xmiid = XMLManip.getAttributeValue(elementNode, "xmi.id");
		retNode = getLocationNodeFromXMIID(xmiid);
		return retNode;
	}

	/** 
	 * @see GetLocationNodeFromXMIID()
	 */
	private Node getLocationNodeFromXMIID(String id )
	{
		Node retNode = null;
		if (m_Doc != null)
		{
			Node typeNode = XMLManip.findElementByID(m_Doc, id);
			if (typeNode != null)
			{
				retNode = getLocationNode(typeNode);
			}
		}
		return retNode;
	}

	/**
	 *
	 * Adds the collection of types to the type file
	 *
	 * @param elements[in]           The types to add
	 * @param addOnlyIfVersioned[in] true to only add the type if it is already versioned, else
	 *                               false to add as long as the type is in the Project this
	 *                               TypeManager is a part of.
	 *
	 * @return HRESULT
	 *
	 */
	public void addTypes( ETList<IElement> elements, boolean addOnlyIfVersioned )
	{
		if (elements != null)
		{
			int count = elements.size();
			if (count > 0)
			{
				if (m_Project != null)
				{
					m_Project.setChildrenDirty(true);
				}
				loadAllExternalElements();
				for (int i=0; i<count; i++)
				{
					IElement elem = elements.get(i);
					addType(elem, addOnlyIfVersioned, false);
				}
			}
		}
	}

	/**
	 *
	 * Adds the passed in element to the type file
	 *
	 * @param element[in]                  The element to add
	 * @param addOnlyIfVersioned[in]       true to only add the type if it is already versioned, else
	 *                                     false to add as long as the type is in the Project this
	 *                                     TypeManager is a part of.
	 * @param loadAllExternalElements[in]  true to load all external elements before adding the element to type file, else
	 *                                     false. In most cases, this should be true unless a batch update of elements
	 *                                     are being added.
	 *
	 * @return 
	 *
	 */
	public void addType( IVersionableElement element, boolean addOnlyIfVersioned, boolean loadAllExternalElements )
	{
		boolean proceed = false;
		if (addOnlyIfVersioned)
		{
			if (processElement(element))
			{
				proceed = true;
			}
		}
		else
		{
			if (isWatchedElement(element))
			{
				proceed = true;
			}
		}
		
		// Don't add presentation elements to the type file
		if (element instanceof IPresentationElement)
		{
		}
		else
		{
			if (proceed)
			{
				resolveReferences(element, loadAllExternalElements);
				Node node = element.getNode();
				if (node != null)
				{
					addExternalElement(node);
				}
			}
		}
	}

	/**
	 *
	 * Changes all references to element's xmiid to a URI to 
	 * the extracted form of element.
	 *
	 * @param element[in]                  The element that has been extracted.
	 * @param loadAllExternalElements[in]  true ( the default ) to load all external elements, else false
	 *
	 * @return HRESULT
	 *
	 */
	private void resolveReferences( IVersionableElement element, boolean loadAllExternalElements )
	{
		//all is commented out in C++ code.
	}

	/**
	 * Retrieves the referenced library with the passed in name.
	*/
	public IProject getReferencedLibraryProjectByLocation( String refLibLoc )
	{
		return getRefLibProject(null, refLibLoc);
	}

	/**
	 *
	 * Retrieves all the projects references by the Project this TypeManager is associated with
	 *
	 * @param pProjs[out] The collection of Projects
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<IProject> getReferencedLibraryProjects()
	{
		ETList<IProject> retProjs = null;
		if (m_Project != null)
		{
			ETList<String> libraries = m_Project.getReferencedLibraries();
			if (libraries != null)
			{
				int count = libraries.size();
				if (count > 0)
				{
					retProjs = new ETArrayList<IProject>();
					IApplication app = getApplication();
					if (app != null)
					{
						for (int i=0; i<count; i++)
						{
							String libLoc = libraries.get(i);
							if (libLoc != null && libLoc.length() > 0)
							{
								IProject proj = getRefLibProject(app, libLoc);
								if (proj != null)
								{
									retProjs.add(proj);
								}
							}
						}
					}
				}
			}
		}
		return retProjs;
	}

	/**
	 *
	 * Retrieves the project referenced at the passed in location
	 *
	 * @param app[in]       The application. Can be null.
	 * @param location[in]  The absolute location of the project
	 * @param pProj[out]    The found project
	 *
	 * @return HRESULT
	 *
	 */
	private IProject getRefLibProject(IApplication app, String location)
	{
		IProject retProj = null;
		if (app == null)
		{
			app = getApplication();
		}
		if (app != null)
		{
			try
			{
				boolean proceed = true;
				retProj = app.getProjectByFileName(location);
				if (retProj == null)
				{
					// First see if the ref lib is in the current workspace...
					ICoreProduct prod = ProductRetriever.retrieveProduct();
					if (prod != null)
					{
						IWorkspace space = prod.getCurrentWorkspace();
						if (space != null)
						{
							IWSProject wsProj = space.openWSProjectByLocation(location);
							if (wsProj != null)
							{
								String projName = wsProj.getName();
								retProj = app.getProjectByName(projName);
								
								if (retProj != null)
								{
									proceed = false;
								}
							}
						}
					}
					
					if (proceed)
					{
						retProj = app.openProject(location);
					}
				}
			}
			catch (WorkspaceManagementException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return retProj;
	}

	/**
	 *
	 * Retrieves the IDs of any element found in the .QueryCache ( which the 
	 * PickListManager oversees ) and then loads those elements into memory
	 *
	 * @param name[in]   The name to resolve
	 *
	 * @return HRESULT
	 *
	 */
	private void ensureLoadedViaPickListManagement(String name )
	{
		if (m_rawPickManager != null)
		{
			IStrings ids = m_rawPickManager.getIDsByName(name);
			if (ids != null)
			{
				int num = ids.getCount();
				for (int i=0; i<num; i++)
				{
					String id = ids.item(i);
					ensureElementLoadedByID(id);
				}
			}
		}
	}

	/**
	 *
	 * Retrieves elements that match a particular name. This is mostly used for type resolution.
	 *
	 * @param nameOfType[in]      The name of the type to retrieve
	 * @param foundElements[out]  The elements with the matching name
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<INamedElement> getCachedTypesByName( String nameOfType )
	{
		ETList<INamedElement> retElems = null;
		if (m_rawPickManager != null)
		{
			IStrings ids = m_rawPickManager.getIDsByName(nameOfType);
			if (ids != null)
			{
				retElems = resolveIDs(ids);
			}
		}
		return retElems;
	}

	/**
	 *
	 * Given the collection of xmi ids, attempts to locate those elements
	 *
	 * @param ids[in]             The ids to locate
	 * @param foundElements[out]  The found elements that correspond to the IDs
	 *
	 * @return HRESULT
	 *
	 */
	private ETList<INamedElement> resolveIDs( IStrings ids )
	{
		if (ids == null)
			return null;
			
		ETList<INamedElement> nels = new ETArrayList<INamedElement>();
		int count = ids.size();
		for (int i=0; i<count; i++)
		{
			String id = ids.get(i);
			if (id == null || id.length() == 0) continue;
			
			IVersionableElement ver = getElementByID(id);
			if (ver instanceof INamedElement)
			{
				nels.add((INamedElement)ver);
			}
		}
		return nels;
	}

	/**
	 *
	 * Retrieves a Named Type most used for type resolution. If there are more than one,
	 * the first is retrieved.
	 *
	 * @param nameOfType[in]      The name of the type
	 * @param foundElement[out]   The found element
	 *
	 * @return HRESULT
	 *
	 */
	public INamedElement getCachedTypeByName( String nameOfType )
	{
		INamedElement retEle = null;
		ETList<INamedElement> elements = getCachedTypesByName(nameOfType);
		if (elements != null)
		{
			int count = elements.size();
			if (count > 0)
			{
				retEle = elements.get(0);
			}
		}
		return retEle;
	}

	/**
	 *
	 * Retrieves a type from a referenced library that matches the passed in name of matches one of the types passed in
	 *
	 * @param name[in]         The name to match
	 * @param filter[in]       A space delimited list of meta types, such as 'Class Interface DataType'
	 * @param foundElement[out] The found element, else 0
	 *
	 * @return HRESULT
	 *
	 */
	public INamedElement getElementFromLibrariesByNameAndType( String name, String filter )
	{
		INamedElement retEle = null;
		if (m_Project != null)
		{
			ETList<String> libraries = m_Project.getReferencedLibraries();
			if (libraries != null)
			{
				int num = libraries.size();
				if (num > 0)
				{
					IApplication app = getApplication();
					if (app != null)
					{
						for (int i=0; i<num; i++)
						{
							String libLoc = libraries.get(i);
							if (libLoc.length() > 0)
							{
								IProject proj = getRefLibProject(app, libLoc);
								if (proj != null)
								{
									ITypeManager typeMan = proj.getTypeManager();
									if (typeMan != null)
									{
										IPickListManager pickMan = typeMan.getPickListManager();
										if (pickMan != null)
										{
											IElement element = pickMan.getElementByNameAndStringFilter(name, filter);
											if (element != null && element instanceof INamedElement)
											{
												retEle = (INamedElement)element;
												break;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return retEle;
	}

	/**
	 *
	 * Retrieves the application off the Product
	 *
	 * @param app[out]   The application
	 *
	 * @return HRESULT
	 *
	 */
	private IApplication getApplication()
	{
		IApplication retApp = null;
		ICoreProduct prod = ProductRetriever.retrieveProduct();
		if (prod != null)
		{
			retApp = prod.getApplication();
		}
		return retApp;
	}

	/**
	 *
	 * Retrieves elements that match a particular name. This is mostly used for type resolution.
	 * The search is restricted to just the Project this TypeManager is associated with.
	 *
	 * @param nameOfType[in]      The name of the type to retrieve
	 * @param foundElements[out]  The elements with the matching name
	 *
	 * @return HRESULT
	 *
	 */
	public ETList<INamedElement> getLocalCachedTypesByName( String nameOfType )
	{
		ETList<INamedElement> retElems = null;
		if (m_rawPickManager != null)
		{
			IStrings ids = m_rawPickManager.getLocalIDsByName(nameOfType);
			if (ids != null)
			{
				retElems = resolveIDs(ids);
			}
		}
		return retElems;
	}

	/**
	 *
	 * Populates the ImportInfo object with data found in the locationNode parameter
	 *
	 * @param locationNode[in] The location node
	 * @param info[in]         The import info object
	 *
	 * @return true if the location is an actual import element, else false
	 *
	 */
	private ImportInfo populateImportInfo(Node locationNode)
	{
		ImportInfo retObj = new ImportInfo();
		if (locationNode != null)
		{
			retObj.m_Href = XMLManip.getAttributeValue(locationNode, "href");
			retObj.m_ProjLocation = XMLManip.getAttributeValue(locationNode, "importProjectLoc");
			retObj.m_ProjectID = XMLManip.getAttributeValue(locationNode, "importProjID");
		}
		return retObj;
	}

	/**
	 *
	 * Loads a node from a different project.
	 *
	 * @param xmiID[in]        The id of the element to load
	 * @param info[in]         The import info used to load the node
	 * @param finalNode[out]   The found node, else 0
	 *
	 * @return HRESULT
	 *
	 */
	private Node loadNodeFromExternalProject(String xmiID, ImportInfo info )
	{
	   return loadNodeFromExternalProject( xmiID, info.m_ProjectID );
	}

	/**
	 *
	 * Loads a node from a different project.
	 *
	 * @param xmiID[in]        The id of the element to load
	 * @param projID[in]       The id of the external Project to pull the element from
	 * @param finalNode[out]   The found node, else 0
	 *
	 * @return HRESULT
	 *
	 */
	private Node loadNodeFromExternalProject(String xmiID, String projID )
	{
		Node retNode = null;

		// Let's see if the node is in memory
		IProject proj = getProjectByID(projID);
		if (proj != null)
		{
			ITypeManager pTypeMan = proj.getTypeManager();
			if (pTypeMan != null)
			{
				IVersionableElement verEle = pTypeMan.getElementByID(xmiID);
				if (verEle != null)
				{
					retNode = verEle.getNode();
				}
			}
		}
		return retNode;
	}

	/**
	 *
	 * Makes sure that the parents of pNode are properly placed in memory.
	 *
	 * @param man[out]         The ExternalFileManager for simple resolution purposes
	 * @param pNode[in]        The node to verify
	 * @param finalNode[out]   If pNode needed to be altered 'cause a parent was brought in and connected
	 *                         to the appropriate Project document, then this will contain that modified
	 *                         node.
	 *
	 * @return true if the parents have been verified, else false
	 *
	 */
	private Node verifyParentsLoaded( ExternalFileManager man, Node pNode)
	{
		Node retNode = null;
		if (pNode != null && pNode instanceof Element)
		{
			String nodeName = ((Element)pNode).getQualifiedName();
			
			// If we've got a Project, no need to go any further
			if (nodeName != null && nodeName.equals("UML:Project"))
			{
				retNode = pNode;
			}
			else
			{
				// If a node is in memory, then make sure that it's parent is also
				// loaded
				Node loadedParent = null;
				try {
				        // manually "pre-compiled" XPath query "ancestor::*[@loadedVersion='true'][1]"
					List loadedAncestors 
					    = XMLManip.selectAncestorNodesByAttribute(pNode, "loadedVersion", "true");
					if (loadedAncestors != null && (! loadedAncestors.isEmpty())) {
					    loadedParent = (Node)loadedAncestors.get(0);
					}
				}
				catch(Exception e) {
				}
				if (loadedParent != null)
				{
					// If the parent is loaded, then we should be able to query for
					// the Project, which is the top level element
					Node projNode = null;
               try
               { 
                  projNode = loadedParent.selectSingleNode("ancestor::UML:Project");
               }
               catch (Exception e)
               {
                  //just ignore as the ancester call could throw null pointer
               }
					if (projNode != null)
					{
						retNode = pNode;
					}
					else
					{
						retNode = getParentAndVerify(man, pNode);
					}
				}
				else
				{
					String parentId = XMLManip.getAttributeValue(pNode, "owner");
					
					// Make one more check to see if we have an element being imported
					// here
					if ((parentId == null || parentId.length() == 0) && man.isImportedElement(pNode))
					{
						// We've got an imported element that is coming from a .etup file
						retNode = man.resolveExternalNode(pNode);
					}
					else
					{
						retNode = getParentAndVerify(man, pNode);
					}
				}
			}
		}
		return retNode;
	}

	private Node getParentAndVerify(ExternalFileManager man, Node pNode)
	{
		Node retNode = null;
		if (pNode != null)
		{
			Node parent = getParentNode(pNode, false);
			Node modChild = null;//getModNode(pNode, parent);
//			if (parent != null )
//			{
//				modChild = getModNode(pNode, parent);
//			}
			if (modChild != null)
			{
				retNode = verifyParentsLoaded(man, modChild);
			}
			else if (parent != null)
			{
				retNode = pNode;
			}
		}
		return retNode;
	}
	
	/**
	 *
	 * Retrieves the parent node of child, making sure the parent is loaded if necessary.
	 *
	 * @param child[in]     The child node to retrieve the parent from
	 * @param parent[out]   The found parent node, else 0
	 * @param quickGet[in]  true to attempt a quick parent retrieval, else false, the default.
	 *                      When false, this routine makes sure that all immediate and ancestor
	 *                      parents have been loaded.
	 *
	 * @return HRESULT
	 *
	 */
	private Node getParentNode( Node child, boolean quickGet)
	{
		Node retNode = null;
		if (quickGet)
		{
			retNode = child.getParent();
		}
		
		if (retNode == null)
		{
			String parentID = XMLManip.getAttributeValue(child, "owner");
			if (parentID != null && parentID.length() > 0)
			{
				ETPairT<String, String> obj = URILocator.uriparts(parentID);
				String nodeLoc = obj.getParamTwo();
				String docloc = obj.getParamOne();
				if (docloc.length() > 0 && nodeLoc.length() > 0)
				{
					retNode = getRawElementByID(parentID);
				}
				else
				{
					String actualID = retrieveTypeHref(parentID);
					if (actualID.length() > 0)
					{
						retNode = getRawElementByID(parentID);
					}
				}
				
				if (retNode != null && quickGet)
				{
					// We were told to do a quick get, but it failed, which means we've
					// got a node that was orphaned due to version control
					String childID = XMLManip.getAttributeValue(child, "xmi.id");
					Document doc = getProjectDocument();
					if (doc != null)
					{
						Node parentToReload = ensureElementLoadedByID(parentID);
						if (parentToReload == null)
						{
							parentToReload = findByID(parentID);
						}
						if (parentToReload != null)
						{
							retNode = parentToReload;
							Node tempChild = XMLManip.findElementByID(doc, childID);
							if (tempChild != null)
							{
								// Make sure the child is also properly loaded. Most
								// of the time, the nodeFromID above will be sufficient,
								// but in the case where the parent is the actual Project,
								// it is quite possible if not probable that the nodeFromID
								// will return the versioned fragment that is in the Project.
								// That fragment needs to be replaced with the contents of
								// the .etx file.
								loadExternalElement(tempChild);
							}
						}
					}
				}
			}
		}
		return retNode;
	}

	/**
	 *
	 * Loads the node described by extNode into the document
	 * encapsulated by the associated IProject.
	 *
	 * @param extNode[in] The ExternalNode element describing the
	 *                    element to load.
	 *
	 * @return HRESULT
	 *
	 */
	private void loadExternalElement( Node extNode )
	{
		// First find out if the ExternalElement node passed in has a parent node. If the 
		// owner id is actually decorated in a URI, then we need to locate the element and 
		// load it. However, if it isn't, we need to make sure that if any ancestor immediately
		// above the element has an owner id that is decorated, then we must locate that
		// node.
		Node parent = getParentNode(extNode, false);
		String id = XMLManip.getAttributeValue(extNode, "xmi.id");
		if (id != null && id.length() > 0)
		{
			Node projNode = findByID(id);
			if (projNode != null)
			{
				ExternalFileManager man = new ExternalFileManager();
				Node dummy = resolveExternalNode(man, projNode, "");
			}
		}
	}

	private Node getModNode(Node child, Node parent, boolean quickGet)
	{
		Node retNode = null;
      
      Element quickParent = null;
      if(quickGet == true)
      {
         quickParent = child.getParent();
      }
      
      if(quickParent == null)
      {
         String childID = XMLManip.getAttributeValue(child, "xmi.id");
         Document doc = getProjectDocument();
         if (doc != null)
         {
            retNode = XMLManip.findElementByID(doc, childID);
         }
      }
      
		return retNode;
	}

	/**
	 *
	 * Adds the appropriate data from the passed in node to a new element on 
	 * the type element DOM tree.
	 *
	 * @param node[in] The node to pull information from and place in the type
	 *                 file
	 *
	 * @return HRESULT
	 *
	 */
	private void addExternalElement( Node node )
	{
		String xmiid = XMLManip.getAttributeValue(node, "xmi.id");
		ETPairT<String, Boolean> hrefPair = UMLXMLManip.getVersionedURI(node);
		String href = hrefPair.getParamOne();
		String loadedVersion = XMLManip.getAttributeValue(node, "loadedVersion");
		String name = XMLManip.getAttributeValue(node, "name");
		
		// We only want to add elements that either have a parent element that is external,
		// or has valid values for the href and loadedVersion attributes. Also, we don't want
		// to add the default packages. These are always loaded into memory. They are indicated
		// with a loadedVersion setting of "false"

		if( !( loadedVersion.equals("false")) && 
			 ( href.length() > 0 || loadedVersion.length() > 0 ))
		{
			if (m_Project != null)
			{
				// We need to make sure that we tell the Project that it has
				// Children that are dirty so that this information gets 
				// saved out to the .ettm

				m_Project.setChildrenDirty(true);
			}
			
			// Make sure we don't already have the element in the type file
			Node typeNode = XMLManip.findElementByID(m_Doc, xmiid);
			if (typeNode == null)
			{
				// Need to create a new Type element in the .ettm file
				Node newNode = XMLManip.createElement(m_Doc, "Type", "");
				Node extElement = m_Doc.selectSingleNode("/TypeManagement/Types");
				if (extElement.getNodeType() == Element.ELEMENT_NODE)
				{
					((Element)extElement).add(newNode);
				}
				typeNode = newNode;
			}
			
			if (typeNode != null)
			{
				XMLManip.setAttributeValue(typeNode, "id", xmiid);

				// In an effort to keep the size of the type file down, we will
				// only store the relative path to the external file in the href
				// xml attribute
				ETPairT<String, String> obj = URILocator.uriparts(href);
				String nodeLoc = obj.getParamTwo();
				String docloc = obj.getParamOne();
				String locID = retrieveLocationID(docloc, true);
				XMLManip.setAttributeValue(typeNode, "location", locID);
				XMLManip.setAttributeValue(typeNode, "name", name);
			}
		}
	}

	/**
	 *
	 * Given the location, retrieves the ID to use when referencing the location in
	 * the future.
	 *
	 * @param location[in]        The location to retrieve the ID for
	 * @param addIfNotFound[in]   true to add the location if an entry is not found( default ),
	 *                            else false to do nothing.
	 *
	 * @return The ID of the location.
	 *
	 */
	private String retrieveLocationID( String location, boolean addIfNotFound )
	{
		String retStr = "";
		if (m_Doc != null)
		{
			String actual = location;
			if (location.length() == 0 && m_State != null)
			{
				// We've got a type being imported from
				// an external project that has never been
				// versioned and is not part of a package
				// that has been versioned
				actual = makeRelativeToProject(m_State.projectFileName());
			}
			if (actual.length() > 0)
			{
				XMLManip.checkForIllegals(actual);
				String query = "TypeManagement/Locations/Location[@href=\"";
				query += actual;
				query += "\"]";
				
				Node loc = m_Doc.selectSingleNode(query);
				if (loc != null)
				{
					String locId = XMLManip.getAttributeValue(loc, "locID");
					if (locId != null && locId.length() > 0)
					{
						retStr = locId;
					}
				}
				else if (addIfNotFound)
				{
					retStr = addLocationEntry(location);
				}
			}
		}
		return retStr;
	}

	/**
	 *
	 * Creates a new entry into the locations area of the type file
	 *
	 * @param location[in] The new location to add
	 *
	 * @return The id of the new location
	 *
	 */
	private String addLocationEntry(String location )
	{
		String retStr = "";
		if (m_Doc != null)
		{
			Node locations = m_Doc.selectSingleNode("TypeManagement/Locations");
			if (locations != null)
			{
				retStr = incrementLocId(locations);
				Node newNode = XMLManip.createElement(m_Doc, "Location", "");
				if (newNode != null)
				{
					String actualLoc = location;
					if (actualLoc.length() == 0 && m_State != null)
					{
						actualLoc = makeRelativeToProject(m_State.projectFileName());
					}
					XMLManip.setAttributeValue(newNode, "locID", retStr);
					XMLManip.setAttributeValue(newNode, "href", actualLoc);
					
					// Now if we are actually adding an external type ( that is, a type coming in via
					// a package or element import ), we need to also add the relative path to that element's
					// project file.
					if (m_State != null)
					{
						if (m_State.reCreating())
						{
							establishImportInfo(location, newNode);
						}
						else
						{
							String relPath = makeRelativeToProject(m_State.projectFileName());
							String projId = m_State.projectID();
							XMLManip.setAttributeValue(newNode, "importProjectLoc", relPath);
							XMLManip.setAttributeValue(newNode, "importProjID", projId);
						}
					}
					
					((Element)locations).add(newNode);
				}
			}
		}
		return retStr;
	}

	/**
	 *
	 * Determines whether or not the location being passed in is a path to an imported project.
	 *
	 * @param location[in]  The location to check
	 * @param newNode[in]   The location node in the type file that will be modified if the
	 *                      location is an external project
	 *
	 * @return HRESULT
	 *
	 */
	private void establishImportInfo( String location, Node newNode)
	{
		// Determine if the file is pointing at a file that is external to the project
		String actualLocation = location;
		if (actualLocation.length() == 0 && m_State != null)
		{
			String loc = m_State.projectFileName();
			if (loc.length() > 0)
			{
				actualLocation = loc;
			}
		}
		String absoluteDoc = retrieveAbsolutePathFromProject(actualLocation);
		if (absoluteDoc.length() > 0 && m_Project != null)
		{
			String fileName = m_Project.getFileName();
			if (fileName.length() > 0)
			{
				String projPath = StringUtilities.getPath(fileName);
				String elementPath = StringUtilities.getPath(absoluteDoc);
				if (!projPath.equals(elementPath))
				{
					// We have an import situation. Retrieve the project.
					String relPath = "";
					String projId = "";
					if (StringUtilities.getExtension(absoluteDoc).equals("etd"))
					{
						// We have the actual Project file
						relPath = makeRelativeToProject(actualLocation);
						Document doc = URILocator.retrieveDocument(absoluteDoc);
						if (doc != null)
						{
							Node projNode = doc.selectSingleNode("//UML:Project");
							if (projNode != null)
							{
								projId = XMLManip.getAttributeValue(projNode, "xmi.id");
							}
						} 
					}
					else
					{
						Node verNode = getVersionedElement(absoluteDoc);
						if (verNode != null)
						{
							projId = XMLManip.getAttributeValue(verNode, "projectID");
							if (projId != null && projId.length() > 0)
							{
								IProject proj = getProjectByID(projId);
								if (proj != null)
								{
									String projFileName = proj.getFileName();
									if (projFileName.length() > 0)
									{
										relPath = makeRelativeToProject(projFileName);
									}
								}
							}								
						}
					}
					XMLManip.setAttributeValue(newNode, "importProjectLoc", relPath);
					XMLManip.setAttributeValue(newNode, "importProjID", projId);
				}
			}
		}
	}

	/**
	 *
	 * Returns a relative path from the internal Project to the absolute
	 * path passed in.
	 *
	 * @param fileLoc[in]   Absolute path to any file
	 *
	 * @return The relative path.
	 *
	 */
	private String makeRelativeToProject(String fileLoc )
	{
		String retStr = "";
		if (m_Project != null)
		{
			String pathLoc = m_Project.getFileName();
			if (pathLoc.length() > 0 && fileLoc.length() > 0)
			{
				retStr = PathManip.retrieveRelativePath(fileLoc, pathLoc);
			}
		}
		return retStr;
	}

	/**
	 *
	 * Retrieves the "VersionedElement" node in the file pointed to by location
	 *
	 * @param location[in]  Absolute location of the versioned file ( .etx )
	 * @param verNode[out]  The found element, else 0
	 *
	 * @return HRESULT
	 *
	 */
	private Node getVersionedElement(String location)
	{
		Node retNode = null;
		Document doc = URILocator.retrieveDocument(location);
		if (doc != null)
		{
			// Retrieve the top level node versioned in this external file.
			retNode = doc.selectSingleNode("//VersionedElement");
		}
		return retNode;
	}

	/**
	 *
	 * Returns then increments the next ID for locations in this type file
	 *
	 * @param locationsNode[in] The dom node that represents the Locations element
	 *
	 * @return The current ID.
	 *
	 */
	private String incrementLocId( Node locationsNode )
	{
		String retStr = "";
		String nextId = XMLManip.getAttributeValue(locationsNode, "nextID");
		if (nextId != null && nextId.length() > 0)
		{
			// ID xml attributes must start with an alphanumeric character
			retStr = "T.";
			retStr += nextId;
			int nextIdNum = Integer.parseInt(nextId);
			nextIdNum++;
			String buffer = Integer.toString(nextIdNum);
			XMLManip.setAttributeValue(locationsNode, "nextID", buffer);
		}
		return retStr;
	}

	private void connectToTypeFile()
    {
        try {
            File typeFile = retrieveTypeFile(m_Project);

            IDResolver resolver = new IDResolver();
            resolver.addNodeTypeId("Location", "locID");
            resolver.addNodeTypeId("Type", "id");
            resolver.addNodeTypeId("TypeManagement", "projectID");
            m_Doc = XMLManip.getDOMDocument(typeFile.getCanonicalPath(), resolver);
            if (m_Doc == null) {
                // TODO: Complain loudly.
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
	/**
	 *
	 * Registers for the various events this TypeManager is interested in.a
	 *
	 */
    private void registerForEvents()
    {
    	EventDispatchRetriever ret = EventDispatchRetriever.instance();
		Object changeObj = ret.getDispatcher(EventDispatchNameKeeper.modifiedName());
		if (changeObj != null && changeObj instanceof IElementChangeEventDispatcher)
		{
			m_Dispatcher = (IElementChangeEventDispatcher)changeObj;
		}
		Object lifeObj = ret.getDispatcher(EventDispatchNameKeeper.lifeTime());
		if (lifeObj != null && lifeObj instanceof IElementLifeTimeEventDispatcher)
		{
			m_LifeTimeDispatcher = (IElementLifeTimeEventDispatcher)lifeObj;
		}
		
		if (m_Dispatcher != null)
		{
			m_Dispatcher.registerForNamespaceModifiedEvents(this);
			m_Dispatcher.registerForNamedElementEvents(this);
			m_Dispatcher.registerForExternalElementEventsSink(this);
		}
		if (m_LifeTimeDispatcher != null)
		{
			m_LifeTimeDispatcher.registerForLifeTimeEvents(this);
		}
    }
    
	/**
	 *
	 * Saves all the unresolved types to a ".unresolved" file in the project directory
	 *
	 * @return S_OK
	 *
	 */
    private void saveUnresolvedTypes()
    {
    	if (m_UnresolvedIDs != null && m_UnresolvedIDs.size() > 0)
    	{
    		String fileName = getUnResolveFileName();
    		if (fileName != null && fileName.length() > 0)
    		{
    			File file = new File(fileName);
    			String finalString = "";
    			Iterator iter = m_UnresolvedIDs.iterator();
    			while (iter.hasNext())
    			{
    				String id = (String)iter.next();
    				if (id.length() > 0)
    				{
    					finalString += id;
    					finalString += '\t';
    				}
    			}
    			
    			if (finalString.length() > 0)
    			{
					try
					{
						FileWriter writer = new FileWriter(file);
						writer.write(finalString);
						writer.close();
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    			}
    		}
    	}
    }
    
    private void loadUnresolvedTypes()
    {
        String fileName = getUnResolveFileName();
        if (fileName != null && fileName.length() > 0)
        {
			try
			{
				File file = new File(fileName);
				FileReader fileReader = new FileReader(file);
				BufferedReader reader = new BufferedReader(fileReader);
				String lineRead = "";//reader.readLine();
				while (true)
				{
					String str = reader.readLine();
					if (str != null)
					{
						lineRead += str;
					}
					else
					{
						break;
					}
				}
				StringTokenizer tokenizer = new StringTokenizer(lineRead, "\t");
				while (tokenizer.hasMoreTokens())
				{
					String token = tokenizer.nextToken();
					addToUnresolved(token);
				}
				reader.close();
			}
			catch (FileNotFoundException e)
			{
//				e.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

	/**
	 *
	 * Adds the passed in ID to our collection of elements that could
	 * not be resolved. We do this so that we don't waste a lot of time
	 * searching for an ID that cannot be found
	 *
	 * @param id[in]  The ID to add
	 *
	 * @return HRESULT
	 *
	 */
	private void addToUnresolved( String id )
	{
		//all code commented out in C++
	}
    
    private File getUnresolvedFile()
    {
        // TODO:
        return null;
    }

	/**
	 *
	 * Retrieves the file name for the ".unresolved" file
	 *
	 * @return the absolute path to the file.
	 *
	 */
	private String getUnResolveFileName()
	{
		String fileName = "";
		if (m_ProjectFileName != null && m_ProjectFileName.length() > 0)
		{
			fileName = StringUtilities.getPath(m_ProjectFileName);
			fileName += ".unresolved";
		}
		return fileName;
	}
    
    /**
     * Verifies that the .ettm file that should be associated with the passed-in
     * project exists.
     * 
     * @param project The project to check out.
     * @return <code>true</code> if the .ettm file exists.
     */
    private boolean verifyTypeFile(IProject project)
    {
        if (project.getFileName() == null)
            return false;
        return retrieveTypeFile(project).exists();
    }
    
    /**
     * Retrieves the .ettm file, given a project.
     * 
     * @param project The project for which we want a .ettm file.
     * @return The <code>File</code> object of the .ettm file.
     */
    private File retrieveTypeFile(IProject project)
    {
        return new File(
            StringUtilities.ensureExtension(project.getFileName(), ".ettm"));
    }

	/**
	 *
	 * Retrieves the absolute path to the .ettm file, given the particular project.
	 *
	 * @param proj[in] The IProject
	 */
	private String retrieveTypeFileName(IProject project)
	{
		String fileName = project.getFileName();
		if (fileName != null && fileName.length() > 0)
		{
			fileName = StringUtilities.ensureExtension( fileName, ".ettm" );
		}
		return fileName;
	}

    /**
     * 
     */
    public void clearDeletedIDs()
    {
        m_DeletedCache.clear();
    }
    
    /**
     * Revokes this TypeManager from the various sinks it's listening to.
     */
    private void revokeEventSinks()
    {
        if (m_Dispatcher != null && m_LifeTimeDispatcher != null)
        {
            m_Dispatcher.revokeNamespaceModifiedSink(this);
            m_Dispatcher.revokeNamedElementSink(this);
            m_Dispatcher.revokeExternalElementEventsSink(this);
            m_LifeTimeDispatcher.revokeLifeTimeSink(this);
            
            if (m_rawPickManager != null)
            {
                m_rawPickManager.deinitialize();
                m_rawPickManager = null;
            }
            
            m_Dispatcher            = null;
            m_LifeTimeDispatcher    = null;
        }
    }

	/**
	 *
	 * Retrieves an element that matches name
	 *
	 * @param name[in] The name to match against
	 * @param foundElement[out] The found elmeent, else 0
	 *
	 * @return HRESULT
	 *
	 */
    public INamedElement getElementByName(String name)
    {
    	INamedElement retEle = null;
    	ETList<INamedElement> elements = getElementsByName(name);
    	if (elements != null)
    	{
    		int count = elements.size();
    		if (count > 0)
    		{
    			retEle = elements.get(0);
    		}
    	}
        return retEle;
    }

	/**
	 *
	 * Retrieves the elements that have a name that matches name.
	 *
	 * @param name[in] The name to match against
	 * @param foundElements[out] The found elements
	 *
	 * @return HRESULTS
	 *
	 */
    public ETList<INamedElement> getElementsByName(String name)
    {
    	ETList<INamedElement> retObj = null;

		// First check the type document, making sure any
		// elements found there are loaded 
		List nodes = getListOfElementsByName(name);
		if (nodes != null)
		{
			ensureElementsAreLoaded(nodes);
		}

		// Now perform the query on the Project
		retObj = findByName(name);
    	
        return retObj;
    }

	/**
	 *
	 * Makes sure that all the nodes passed in are loaded into the Project's
	 * DOM tree
	 *
	 * @param nodes[in] The collection of Types retrieved from
	 *                  this TypeManager.
	 *
	 * @return HRESULT
	 *
	 */
	private void ensureElementsAreLoaded(List nodes)
	{
		if (nodes != null)
		{
			int count = nodes.size();
			for (int i=0; i<count; i++)
			{
				Node node = (Node)nodes.get(i);
				String id = XMLManip.getAttributeValue(node, "id");
				if (id != null && id.length() > 0)
				{
					Node foundNode = ensureElementLoadedByID(id);
				}
			}
		}
	}

	/**
	 *
	 * Retrieves all the elements in this type document that match
	 * the passed in name.
	 *
	 * @param name[in] The name to query against
	 * @param nodes[out] The found nodes
	 *
	 * @return A collection of found nodes. If no node matches name, nodes
	 *         will be 0.
	 *
	 */
	private List getListOfElementsByName( String name)
	{
		List retObj = null;
		
		String query = "//*[@name='";
		query += name;
		query += "']";
		if (m_Doc != null)
		{
			retObj = m_Doc.selectNodes(query);
		}
		if (retObj == null || retObj.size() == 0)
		{
			// If we don't have the type in the .ettm file,
			// then maybe the PickListManager knows about it...
			ensureLoadedViaPickListManagement(name);
		}
		
		return retObj;
	}

	/**
	 *
	 * Retrieves an element given the passed in ID
	 *
	 * @param xmiID[in] The ID to match against
	 * @param foundElement[out] The found element, else 0
	 *
	 * @return HRESULT
	 *
	 */
    public IVersionableElement getElementByID(String xmiID)
    {
    	IVersionableElement retEle = null;
    	Node node = getRawElementByID(xmiID);
    	if (node != null)
    	{
    		TypedFactoryRetriever ret = new TypedFactoryRetriever();
    		Object obj = ret.createTypeAndFill(node);
    		if (obj != null && obj instanceof IVersionableElement)
    		{
    			retEle = (IVersionableElement)obj;
    		}
    	}
        return retEle;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.typemanagement.ITypeManager#getRawElementByID(java.lang.String)
     */
    public Node getRawElementByID(String elementID)
    {
        if (elementID != null && m_Project != null)
        {
			// This PreventReEntranceByValue object makes
			// sure that we don't get into a type loop that causes
			// a stack overflow. This was happening when two projects
			// had imports into each other...
			
			//Commenting out to improve performance, seems to be blocking
			//PreventReEntranceByValue blocker = new PreventReEntranceByValue(elementID, m_ProjectID, 0);
			try
			{
				if (/*!blocker.isBlocking() && */ !isIDDeleted(elementID))
				{
					Node node = findByID(elementID);
					if (node == null)
					{
						// This code will be run when we encount a type that
						// is either from an element import or a type
						// that is part of a nested versioned element
						// that is currently not part of the in-memory
						// Project.
						node = ensureElementLoadedByID(elementID);
						if (node == null)
						{
							node = findByID(elementID);
						}
					}
					return node;
				}
			}
			finally
			{
				//Commenting out to improve performance, seems to be blocking
				//blocker.releaseBlock();
			}
        }
        return null;
    }
    
    private Node findByID(String id)
    {
    	Node retNode = null;
        if (!isIDDeleted(id))
        {
            Document doc = getProjectDocument();
            if (doc != null)
            {
				   String rawID = retrieveRawXMIID(id);
				
				   //In C++ version this statement returns a node, I think there we search for this id in all files -
				   //etd, ettm and etup
            	//retNode = XMLManip.findElementByID(doc, rawID);
               
               retNode = XMLManip.findElementByID(doc, rawID);
               if(retNode == null)
               {
               	retNode = testFindElementByID(doc, rawID);
//                  String projFileName = m_Project.getFileName();
//                  ExternalFileManager.setRootFileName(projFileName);
//                  ExternalFileManager man = new ExternalFileManager();
//                  Node node = resolveExternalNode(man, retNode, rawID);
//                  if (node != null)
//                  {
//                     retNode = null;
//                     retNode = node;
//                  }
               }
            	
               // Since we do not have source control or multiple projects working
               // in JUML I am removing this until we do add the above features.
               // Fix J1079:  However, this code is necessary to resolve stereotypes
               //             properly for Rose Imported projects.
               
            	String projFileName = m_Project.getFileName();
            	ExternalFileManager.setRootFileName(projFileName);
            	ExternalFileManager man = new ExternalFileManager();
   				Node node = resolveExternalNode(man, retNode, rawID);
   				if (node != null)
   				{
   					retNode = null;
   					retNode = node;
   				}
            }
        }
        return retNode;
    }
    
    private Node testFindElementByID(Document doc, String id)
    {
    	Node retNode = null;

    	//somehow findElementById returns me a node which does not have right parent and child set.
//		String pattern = ".//*[@xmi.id=\"" + id + "\"]";

 		String filename = m_Project.getFileName();
         
         int dotpos = filename.lastIndexOf('.');
         String stem = (dotpos != -1?  filename.substring(0, dotpos)
                                     : filename);
 		String etupFile = stem + ".etup";
 		Document etupDoc = XMLManip.getDOMDocumentUseWeakCache(etupFile);
 		if (etupDoc != null)
 		{
			//retNode = ettmDoc.selectSingleNode(pattern);//XMLManip.findElementByID(ettmDoc, id);
         retNode = XMLManip.findElementByID(etupDoc, id);
			if (retNode == null)
			{
				String ettmFile = stem + ".ettm";
				Document ettmDoc = XMLManip.getDOMDocumentUseWeakCache(ettmFile);
				if (ettmDoc != null)
				{
					//retNode = etupDoc.selectSingleNode(pattern);//XMLManip.findElementByID(etupDoc, id);
               retNode = XMLManip.findElementByID(ettmDoc, id);
				}
			}
 		}
    	
    	return retNode;
    }
    
	/**
	 *
	 * Makes sure that the element that exists in the type file
	 * is loaded into the Project DOM.
	 *
	 * @param id[in] The id to load
	 *
	 * @return HRESULT
	 *
	 */
    private Node ensureElementLoadedByID(String id)
    {
    	Node retObj = null;
    	String xmiId = retrieveRawXMIID(id);
    	ImportInfo info = getImportInfo(xmiId);
    	if ((info != null) && (info.isImported() == true))
    	{
    		retObj = loadNodeFromExternalProject(xmiId, info);
    	}
    	else
    	{
			// Make the lookup in the type file
    		String actualId = retrieveTypeHref(id);
			ETPairT<String, String> obj = URILocator.uriparts(actualId);
			String nodeLoc = obj.getParamTwo();
			String docLoc = obj.getParamOne();
    		if (docLoc != null && docLoc.length() > 0)
    		{
    			String absoluteURI = makeURIAbsolute(docLoc, nodeLoc);
    			if (absoluteURI != null && absoluteURI.length() > 0)
    			{
    				retObj = loadExternalFileAndReturnNode(absoluteURI);
    				if (retObj == null)
    				{
    					retObj = findUnknownType(id);
    				}
    			}
    		}
    		else
    		{
    			retObj = findUnknownType(id);
    		}
    	}
        return retObj;
    }

	private Node findUnknownType(String xmiId)
	{
		Node retNode = null;
		
		// Couldn't find the element by that ID. We need to do a manual search through all the 
		// external files. But first, lets see if the element we need is in memory. This
		// will happen most often in the case where someone is asking for an element by ID
		// that has not been added to the Project yet
		String id = retrieveRawXMIID(xmiId);
		FactoryRetriever ret = FactoryRetriever.instance();
		IVersionableElement verEle = ret.retrieveObject(id);
		if (verEle == null)
		{
			// Make a quick check to see if the ID coming in is actually a Project ID to a project that
			// contains elements or packages that we have imported.
			retNode = idIsExternalProject(id);
			if (retNode == null)
			{
				String fullID = "xmi.id=\"";
				fullID += id;
				fullID += "\"";
				if (!isIrresolvable(id))
				{
					String fileLoc = findInExternalFiles(fullID);
					if (fileLoc != null && fileLoc.length() > 0)
					{
						// We've found an external file with the id, so be sure
						// to add that element to the .ettm file so we don't have
						// to make this manual search again.
						if (loadExternalFileAndReturnNode(fileLoc) != null)
						{
							Node node = findByID(id);
							if (node != null)
							{
								addExternalElement(node);
								retNode = node;
							}
							else
							{
								addToUnresolved(id);
							}
						}
						else
						{
							retNode = retrieveAndAddFromImports(id);
						}
					}
					else
					{
						retNode = retrieveAndAddFromImports(id);
					}
				}
			}
		}
		else
		{
			retNode = verEle.getNode();
		}
		return retNode;
	}

	/**
	 *
	 * Searches for the passed in XMI.ID in all the external files
	 * associated with this Project
	 *
	 * @param id[in]  The id to find
	 *
	 * @return The absolute location of the file the ID was found in, else ""
	 *
	 */
	private String findInExternalFiles( String id )
	{
		String foundInFile = null;
		IStrings fileLocations = gatherExternalFileLocations();
		if (fileLocations != null)
		{
			int num = fileLocations.getCount();
			for (int i=0; i<num; i++)
			{
				String fileLoc = fileLocations.item(i);
				if (fileLoc.length() > 0)
				{
					if (isElementPresent(id, fileLoc))
					{
						foundInFile = fileLoc;
						break;
					}
				}
			}
		}
		return foundInFile;
	}

	/**
	 *
	 * Determines whether or not the passed in xmi id is in the file located
	 * at fileLoc
	 *
	 * @param id[in]        The ID to find
	 * @param fileLoc[in]   Absolute path to a file to look in
	 *
	 * @return true if the id was found, else false
	 *
	 */
	private boolean isElementPresent(String id, String fileLoc)
	{
		boolean isPresent = false;
		if (id != null && fileLoc != null)
		{
			isPresent = FileManip.isInFile(id, fileLoc);
		}
		return isPresent;
	}

	/**
	 *
	 * Determines whether or not a particular id has been determined
	 * irresolvable.
	 *
	 * @param id[in]  The ID to check
	 *
	 * @return HRESULT
	 *
	 */
	private boolean isIrresolvable(String id )
	{
		boolean irresolvable = false;
		Iterator iter = m_UnresolvedIDs.iterator();
		while (iter.hasNext())
		{
			Object obj = iter.next();
			if (obj.equals(id))
			{
				irresolvable = true;
				break;
			}
		}
		return irresolvable;
	}

	/**
	 *
	 * Determines whether or not the passed in xmi.id is the id of an external
	 * Project. If it is, the node of that IProject is returned.
	 *
	 * @param id[in]           The id to query against
	 * @param foundNode[out]   The node of the Project, else 0
	 *
	 * @return HRESULT
	 *
	 */
	private Node idIsExternalProject(String id)
	{
		Node retNode = null;
		if (m_Doc != null)
		{
			String query = "//Location[@importProjID=\"";
			query += id;
			query += "\"]";
			Node node = m_Doc.selectSingleNode(query);
			if (node != null)
			{
				// We've got an ID for an external project that owns some elements that this Project ( 
				// the project this TypeManager is associated with ) is importing. Make sure it is opened...
				IProject proj = getProjectByID(id);
				if (proj != null)
				{
					retNode = proj.getNode();
				}
			}
		}
		return retNode;
	}

	/**
	 *
	 * Finds the element with the passed in ID from any of the imported projects. Once found,
	 * that type will be added to this project's type file for easier retrieval when asked
	 * for that type again.
	 *
	 * @param id[in]           The ID of the element to find
	 * @param foundNode[out]   The found node, else 0. 0 Can be passed in.
	 *
	 * @return HRESULT
	 *
	 */
	private Node retrieveAndAddFromImports(String id)
	{
		Node retNode = null;
		
		// We couldn't find the type with the passed in
		// ID in any element that was specifically imported or in
		// any external file associated with this project. The ID
		// must be part of an element that was imported into this project.
		// Retrieve all the projects that we have imported elements from,
		// and query their TypeManager's for the type
		Node node = retrieveTypeFromImportedProjects(id);
		if (node != null)
		{
			TypedFactoryRetriever ret = new TypedFactoryRetriever();
			Object obj = ret.createTypeAndFill(node);
			if (obj != null && obj instanceof IVersionableElement)
			{
				IVersionableElement verNode = (IVersionableElement)obj;
				addExternalType(verNode);
			}
			retNode = node;
		}
		else
		{
			addToUnresolved(id);
		}
		return retNode;
	}

	/**
	 *
	 * Attempts to find the element that has the passed in XMI id in the Project that
	 * are being imported indirectly into the Project this TYpeManager is associated with
	 *
	 * @param id[in]           The id to locate the element with
	 * @param foundNode[out]   The found object, else 0
	 *
	 * @return HRESULT
	 *
	 */
	private Node retrieveTypeFromImportedProjects(String id)
	{
		Node retNode = null;
		ETList<IProject> projects = getImportedProjects();
		if (projects != null)
		{
			int count = projects.size();
			for (int i=0; i<count; i++)
			{
				IProject proj = projects.get(i);
				ITypeManager typeMan = proj.getTypeManager();
				if (typeMan != null)
				{
					IVersionableElement ver = typeMan.getElementByID(id);
					if (ver != null)
					{
						retNode = ver.getNode();
						break;
					}
				}
			}
		}
		return retNode;
	}

	/**
	 *
	 * Retrieves all the Projects that are being imported into the 
	 * Project associated with this TypeManager indirectly.
	 *
	 * @param pProjects[out]
	 *
	 * @return 
	 *
	 */
	private ETList<IProject> getImportedProjects()
	{
		ETList<IProject> retObj = null;
		if (m_Doc != null)
		{
			List nodes = m_Doc.selectNodes("//Location[@importProjID]");
			if (nodes != null)
			{
				int count = nodes.size();
				for (int i=0; i<count; i++)
				{
					Node node = (Node)nodes.get(i);
					String id = XMLManip.getAttributeValue(node, "importProjID");
					if (id != null && id.length() > 0)
					{
						IProject proj = getProjectByID(id);
						if (proj != null)
						{
							if (retObj == null)
							{
								retObj = new ETArrayList<IProject>();
							}
							retObj.add(proj);
						}
					}
				}
				
				ETList<IProject> refProjs = getReferencedLibraryProjects();
				if (refProjs != null)
				{
					//append these to the return collection
					int num = refProjs.size();
					for (int j=0 ; j<num; j++)
					{
						IProject proj = refProjs.get(j);
						if (retObj == null)
						{
							retObj = new ETArrayList<IProject>();
						}
						retObj.add(proj);
					}
				}
			}
		}
		return retObj;
	}

	/**
	 *
	 * Loads the first element in the passed in document with an "href" xml attribute.
	 *
	 * @param doc[in]          The document to search
	 * @param foundNode[out]   The found element
	 *
	 * @return HRESULT
	 *
	 */
	private Node loadFirstHRefElement(Document doc)
	{
		Node retNode = null;
		if (doc != null)
		{
			retNode = doc.selectSingleNode("//*[@href][1]");
			if (retNode != null)
			{
				loadExternalElement(retNode);
			}
		}
		return retNode;
	}

	/**
	 *
	 * Takes the two parts of a URI, making sure that they are absolute paths when necessary.
	 *
	 * @param docLoc[in] The location to the file
	 * @param nodeLoc[in] The xpath expression to the element in the file.
	 *
	 * @return HRESULT
	 *
	 */
	private String makeURIAbsolute(String docLoc, String nodeLoc )
	{
		String uri = "";
		String absoluteDoc = retrieveAbsolutePathFromProject(docLoc);
		if (absoluteDoc != null && absoluteDoc.length() > 0)
		{
			absoluteDoc = stripURIDeclaration(absoluteDoc);
			uri = absoluteDoc + "#" + nodeLoc;
		}
		return uri;
	}

	/**
	 *
	 * If the document location part of a URI is prefixed with a URI declaration, e.g.,
	 * "uri|", then this routine will string that off.
	 *
	 * @param docLoc[in] The string to process
	 *
	 * @return The processed string
	 *
	 */
	private String stripURIDeclaration(String docLoc)
	{
		return URILocator.stripURIDeclaration(docLoc);
	}

	/**
	 *
	 * Finds all the elements by a given name in the IProject this TypeManager
	 * is associated with.
	 *
	 * @param name[in] The name to match against
	 * @param foundElements[out] The found elements.
	 *
	 * @return HRESULT
	 *
	 */
	private ETList<INamedElement> findByName(String name)
	{
		ETList<INamedElement> retObj = null;
		Document doc = getProjectDocument();
		if (doc != null)
		{
			String nameValue =  "//*[@name='";
			nameValue += name;
			nameValue += "']";
			
			ElementCollector<INamedElement> collector = new ElementCollector<INamedElement>();
			List list = collector.selectNodes(doc, nameValue);
			if (retObj != null)
			{
				//INamedElement dummy = null;
				retObj = collector.populateCollection(list, INamedElement.class);
			}
		}
		return retObj;
	}
    
    private String retrieveRawXMIID(String id)
    {
        return URILocator.retrieveRawID(id);
    }
    
    /**
     * Retrieves the XML document associated with the IProject this TypeManager
     * is associated with.
     * 
     * @return The XML Document.
     */
    private Document getProjectDocument()
    {
        if (m_Project != null)
        {
            Node projectNode = m_Project.getNode();
            if (projectNode != null)
                return projectNode.getDocument();
        }
        return null;
    }
    
    private boolean isIDDeleted(String id)
    {
        return m_DeletedCache.contains(id);
    }

	/**
	 *
	 * Determines is the element passed in is actually a part of the IProject
	 * this TypeManager is associated with. If it isn't, then all events 
	 * coming off this element should be ignored by this TypeManager instance.
	 *
	 * @param element[in] The element to check
	 *
	 * @return true if element is part of our Project, else false.
	 *
	 */
	private boolean isWatchedElement( IVersionableElement element )
	{
		boolean isWatched = false;
		if (element != null && m_Project != null)
		{
			if (element instanceof IElement)
			{
				IElement actual = (IElement)element;
				IProject proj = actual.getProject();
				if (proj != null)
				{
					if (proj.isSame(m_Project))
					{
						isWatched = true;
					}
				}
			}
		}
		return isWatched;
	}

	/**
	 *
	 * Determines whether or not any processing should be performed on the passed
	 * in element.
	 *
	 * @param element[in] The element to potentially process for type information
	 *
	 * @return HRESULT
	 *
	 */
	private boolean processElement( IVersionableElement element )
	{
		boolean process = false;
		if (m_Project != null)
		{
			boolean isVersioned = m_Project.isVersioned();
			
			// A Project has to be versioned before anything within that project can be.
			if (isVersioned)
			{
				process = isWatchedElement(element);
			}
		}
		return process;
	}

	/**
	 *
	 * Saves the contents of this TypeManager to the specified file.
	 *
	 * @param location[in] The absolute path to a file.
	 */
    public void save(String location)
    {
        if (m_Doc != null)
        {
                XMLManip.save(m_Doc,location);
        }
    }

	/**
	 *
	 * Adds the passed in element to the type file, as long as element
	 * is part of the Project that this TypeManager is a part of.
	 *
	 * @param element[in] The element to add.
	 *
	 * @return HRESULT
	 *
	 */
    public void addType(IVersionableElement element)
    {
    	if (processElement(element))
    	{
    		addToTypes(element);
    	}
    }

	/**
	 *
	 * Adds a type that is assumed is external to this TypeManager's type list of
	 * versioned or imported types.
	 *
	 * @param element[in] The element 
	 *
	 * @return HRESULT
	 * @see AddToTypes()
	 *
	 */
    public void addExternalType(IVersionableElement element)
    {
    	if (element != null && element instanceof IElement)
    	{
    		IElement actual = (IElement)element;
    		IProject proj = actual.getProject();
    		if (proj != null || element instanceof Profile)
    		{
    			State state = new State(m_State, proj, false);
    			addToTypes(element);
                        if (element instanceof INamedElement) {
                            getPickListManager().addExternalNamedType((INamedElement) element);
                        }
                }
        }
    }
    
	/**
	 *
	 * Adds the passed in element to this TypeManager's list of types either
	 * versioned or imported.
	 *
	 * @param element[in] The element to add
	 *
	 * @return HRESULT
	 *
	 */
	private void addToTypes( IVersionableElement element )
	{
		if (element != null)
		{
			if (element instanceof IPresentationElement)
			{
				// Don't add presentation elements to the type file
			}
			else
			{
				resolveReferences(element, true);
				Node node = element.getNode();
				if (node != null)
				{
					addExternalElement(node);
					addNecessaryChildElements(element);
				}
			}
		}
	}

	/**
	 *
	 * This methods makes sure that any elements that are likely to be referenced by
	 * external elements are also added to the type file along with the parent element
	 *
	 * @param element[in]   The parent element, which should already be in the type file
	 *
	 * @return HRESULT
	 *
	 */
	private void addNecessaryChildElements( IVersionableElement element )
	{
		if (element != null)
		{
			Node verNode = element.getNode();
			if (verNode != null)
			{
				// Check to see if the element owns a Part. If it does, we need to make the 
				// part part of the type file.
				List partNodes = verNode.selectNodes("./UML:Element.ownedElement/UML:Part");
				if (partNodes != null)
				{
					TypedFactoryRetriever ret = new TypedFactoryRetriever();
					int num = partNodes.size();
					for (int i=0; i<num; i++)
					{
						Node partNode = (Node)partNodes.get(i);
						Object obj = ret.createTypeAndFill(partNode);
						if (obj != null && obj instanceof IPart)
						{
							IPart part = (IPart)obj;
							addToTypes(part);
						}
					}
				}
			}
		}
	}

	/**
	 *
	 * Removes the passed in element from the type file.
	 *
	 * @param element[in] The element to remove
	 *
	 * @return HRESULT
	 *
	 */
    public void removeType(IVersionableElement element)
    {
    	if (element != null)
    	{
			if (processElement(element))
			{
				Node node = element.getNode();
				removeElement(node);
			}
    	}
    }

	/**
	 *
	 * Removes the node that matches the xmi.id of the passed in 
	 * node.
	 *
	 * @param node[in] The node whose id entry in the types file
	 *                 must be removed
	 *
	 * @return HRESULT
	 *
	 */
	private void removeElement( Node node )
	{
		if (node != null)
		{
			Node typeNode = isTypePresent(node);
			if (typeNode != null)
			{
				removeLocationNode(typeNode);
				Node parent = typeNode.getParent();
				if (parent != null)
				{
					typeNode.detach();
				}
			}
			// Now we need to remove any elements that refer to this element as a parent.
			// Those elements must also be removed
			removeChildElements(node);
		}
	}

	/**
	 *
	 * Removes all nodes that refer to the passed in parentID. This is recursive.
	 *
	 * @param parentNode[in] The parent element
	 *
	 * @return HRESULT
	 *
	 */
	private void removeChildElements(Node parentNode )
	{
		if (parentNode != null)
		{
			// Retrieve all elements that have an xmi.id xml attribute 
			// contained in element
			String query = ".//*[@xmi.id]";
			List nodes = parentNode.selectNodes(query);
			if (nodes != null)
			{
				int num = nodes.size();
				for (int i=0; i<num; i++)
				{
					Node node = (Node)nodes.get(i);
					removeElement(node);
				}
			}
		}
	}

	/**
	 *
	 * Removes the location node from this type file
	 *
	 * @param typeNode[in] A node that is the Type element
	 *                     that is being removed.
	 *
	 * @return HRESULT
	 *
	 */
	private void removeLocationNode(Node typeNode)
	{
		if (typeNode != null)
		{
			Node locNode = getLocationNode(typeNode);
			if (locNode != null)
			{
				Node parent = locNode.getParent();
				if (parent != null)
				{
					locNode.detach();
				}
			}
		}
	}

	/**
	 *
	 * Determines whether or not a element with the xmi.id of element is already
	 * present in the .ettm file.
	 *
	 * @param element[in]      The element to check
	 * @param typeNode[out]    The found element. Can be null.
	 *
	 * @return true if the element is present, else false
	 *
	 */
	private Node isTypePresent(Node element)
	{
		Node retNode = null;
		if (element != null)
		{
			String id = XMLManip.getAttributeValue(element, "xmi.id");
			Node node = XMLManip.findElementByID(m_Doc, id);
			if (node != null)
			{
				retNode = node;
			}
		}
		return retNode;
	}

	/**
	 *
	 * Retrieves all the absolute file names that the IProject that the TypeManager is a part of
	 * is built up on.
	 *
	 * @param fileLocations[out] Collection of absolute paths to the files
	 *
	 * @return HRESULT
	 *
	 */
    public IStrings gatherExternalFileLocations()
    {
    	IStrings retObj = null;
    	if (m_Doc != null)
    	{
			List locations = m_Doc.selectNodes("TypeManagement/Locations/*");
			if (locations != null)
			{
				int numLocs = locations.size();
				if (numLocs > 0)
				{
					retObj = new Strings();
					for (int i=0; i<numLocs; i++)
					{
						Node loc = (Node)locations.get(i);
						String value = XMLManip.getAttributeValue(loc, "href");
						if (value != null && value.length() > 0)
						{
							ImportInfo info = populateImportInfo(loc);
							String absoluteDoc = null;
//							boolean isImported = false;
//							if (info.m_ProjectID != null && info.m_ProjectID.length() > 0)
//							{
//								isImported = true;
//							}
							if (info.isImported() == true)
							{
								absoluteDoc = retrieveAbsolutePathFromProject(info.m_ProjLocation);
								absoluteDoc = PathManip.retrieveAbsolutePath(info.m_Href, absoluteDoc);
							}
							else
							{
								absoluteDoc = retrieveAbsolutePathFromProject(value);
							}
    					
							retObj.add(absoluteDoc);
						}
					}
				}
			}
    	}
        return retObj;
    }

	/**
	 *
	 * Given a location to an external file, the method will attempt to locate
	 * an element in the file that had been unloaded from the main project, and reinject
	 * that element back into the Project, replacing the appropriate node in the
	 * project with the contents of the node found in the file pointed at by fileLocation.
	 *
	 * @param fileLocation[in] The location of the file
	 *
	 * @return HRESULT
	 *
	 */
    public void loadExternalFile(String fileLocation)
    {
    	loadExternalFileAndReturnNode(fileLocation);
    }

	/**
	 *
	 * Given a location to an external file, the method will attempt to locate
	 * an a VersionedElement xml element, which should then hold the specific element
	 * that is ultimately being requested. The process is a bit round about at this time,
	 * in that this routine is meant to load the versioned element, injecting back into the
	 * original document, where, at a later time, a FindById( which ultimately does a nodeById() call 
	 * ) can then be called to retrieve the actual element. 
	 *
	 * All that is fine in the strict version control case, but now that we can refer to imported
	 * elements, this routine can verify that the loaded node is part of the Project this 
	 * TypeManager is associated with. This is important in the import case, 
	 * 'cause a file could be found, and indeed and element in that file could have an href 
	 * attribute in it, but it may not belong to the project this TypeManager is associated with. 
	 * Indeed, in the import case, it WON'T.
	 *
	 * @param fileLocation[in] The location of the file
	 * @param foundNode[out]   The found node in the external file, else 0. Can be 0 when passed in.
	 *
	 * @return true if the external element located in the file specified is part of the
	 *         Project this TypeManager is associated with, else false if it is not.
	 *
	 */
	private Node loadExternalFileAndReturnNode(String fileLocation)
	{
		Node retNode = null;
		boolean sameProject = true;
		if (fileLocation != null && fileLocation.length() > 0)
		{
			String projId = locationIsExternalProject(fileLocation);
			if (projId == null || projId.length() == 0)
			{
				Document doc = URILocator.retrieveDocument(fileLocation);
				if (doc != null)
				{
					// Retrieve the top level node versioned in this external file and
					// use the XMI.id to find the element to load in the Project
					Node verNode = doc.selectSingleNode("//VersionedElement");
					if (verNode != null)
					{
						sameProject = inSameProject(verNode);
						if (sameProject)
						{
							// Don't copy to foundNode, 'cause it is quite likely
							// that the type we're looking for is NOT the one that
							// will be loaded by finding the first element in the .etx
							// with an href. Most notably, this occurs when we are
							// just versioning a package.
							Node temp = loadFirstHRefElement(doc);
						}
					}
					else
					{
						// We have a type here that is not a versioned element. Currently,
						// the only way to get to this part of the code is via the Stereotype
						// mechanism, which creates a Profile that is external to the Project.
						//
						// We don't want to return the node found from the following code
						// 'cause we really don't know if that node is the one the caller wants
						Node actual = loadFirstHRefElement(doc);
					}
				}
			}
			else
			{
				// What we have here is a type that has been found to be in an external Project.
				// This is most likely to happen when a type has been imported from a Project
				// that has not been Divided or versioned.
				//
				// I am assuming that the location coming in is a URI spec. If that
				// every changes, we'll need to pass in the XMI id of the element to locate
				// in order for this part of the code to work.
				if (projId.length() > 0)
				{
					String elementId = retrieveRawXMIID(fileLocation);
					if (elementId != null && elementId.length() > 0)
					{
						retNode = loadNodeFromExternalProject(elementId, projId);
					}
				}
			}
		}
		return retNode;
	}

	/**
	 *
	 * Determines whether or not the verNode node passed in contains
	 * elements that are part of the Project this TypeManager is 
	 * associated with. It is assumed that verNode is the "VersionedElement"
	 * element that is present in all .etx files. That element should
	 * always have a "projectID" xml attribute on it.
	 *
	 * @param verNode[in] The node to check against
	 *
	 * @return true if in same project else false
	 *
	 */
	private boolean inSameProject(Node verNode )
	{
		//To Do implement
		return false;
	}

	/**
	 *
	 * Determines whether or not the location passed in points at a Project that is being imported
	 *
	 * @param location[in]  The location to test
	 * @param projID[out]   If the location is an external project, this will be filled in with
	 *                      that Project's ID
	 *
	 * @return true if location points at an external project, else false
	 *
	 */
	private String locationIsExternalProject(String location)
	{
		String retStr = "";
		if (location != null && location.length() > 0)
		{
			ETPairT<String, String> obj = URILocator.uriparts(location);
			String nodeLoc = obj.getParamTwo();
			String docLoc = obj.getParamOne();
			String relPath = makeRelativeToProject(docLoc);
			String query = "//Location[@importProjectLoc=\"";
			query += relPath;
			query += "\"]";
			
			Node node = m_Doc.selectSingleNode(query);
			if (node != null)
			{
				retStr = XMLManip.getAttributeValue(node, "importProjID");
			}
		}
		return retStr;
	}

	public void loadExternalElements()
	{
		loadAllExternalElements();
	}

	/**
	 *
	 * Retrieves the owner of childElement that is versioned. There are many times where an element ( such as a
	 * UML:Parameter ) are not versioned themselves, but are encapsulated by an owner ( and it may not be an immediate
	 * owner. For example, in the UML:Parameter case, the owning UML:Operation may not be versioned, but the 
	 * UML:Class maybe ) who is. This method will retrieve that owner.
	 *
	 * @param childElement[in]    The element whose owner we need
	 * @param versionedOwner[out] The found owner, else 0.
	 *
	 * @return HRESULT
	 *
	 */
    public IVersionableElement getVersionedOwner(IVersionableElement childElement)
    {
    	IVersionableElement retEle = null;
    	if (childElement != null)
    	{
    		boolean isVersioned = childElement.isVersioned();
    		if (isVersioned)
    		{
				// If childElement is coming in, and that element is versioned itself, 
				// then we need to retrieve the owner of this element, checking to
				// see if it is versioned. If not, we'll call this method recursively
    			if (childElement instanceof IElement)
    			{
    				IElement elem = (IElement)childElement;
    				IElement owner = elem.getOwner();
    				if (owner != null)
    				{
    					isVersioned = owner.isVersioned();
    					if (isVersioned)
    					{
    						retEle = owner;
    					}
    					else
    					{
    						retEle = getVersionedOwner(owner);
    					}
    				}
    			}
    		}
    		else
    		{
				// We've got an element that is NOT versioned itself, but
				// is potentially owned by an element that is
    			Node childNode = childElement.getNode();
    			ETPairT<String, Boolean> versionURI = UMLXMLManip.getVersionedURI(childNode);
    			String uri = versionURI.getParamOne();
    			if (uri != null && uri.length() > 0)
    			{
    				ETPairT<String, String> obj = URILocator.uriparts(uri);
    				String docLoc = obj.getParamOne();
    				String nodeLoc = obj.getParamTwo();
    				if (docLoc != null && nodeLoc != null)
    				{
    					String absoluteURI = makeURIAbsolute(docLoc, nodeLoc);
    					if (absoluteURI != null && absoluteURI.length() > 0)
    					{
    						Document doc = URILocator.retrieveDocument(absoluteURI);
    						if (doc != null)
    						{
    							//Node node = doc.selectSingleNode("VersionedElement/child::*[1]");
                        Node verNode = doc.selectSingleNode("VersionedElement");
                        if(verNode instanceof Element)
                        {
                           Element verElement = (Element)verNode;
                           List children = verElement.elements();
                           if((children != null) && (children.size() > 0))
                           {
                              Node node = (Node)children.get(0);
                              if (node != null)
                              {
                                 String xmiIdOfOwner = XMLManip.getAttributeValue(node, "xmi.id");
                                 retEle = getElementByID(xmiIdOfOwner);
                              }
                           }
                        }
    						}
    					}
    				}
    			}
    		}
    	}
        return retEle;
    }

	/**
	 *
	 * Removes the passed in element not only from this type file, but cleans
	 * all references to the type, erasing any uri information in regards to an
	 * xmi id reference to the element, replacing it with just the raw XMI id.
	 *
	 * @param element[in] The element being cleansed
	 *
	 * @return HRESULT
	 *
	 */
    public void removeFromTypeLookup(IVersionableElement element)
    {
    	if (element != null)
    	{
    		String uri = element.getVersionedURI();
    		
			// The DCE xmiID length is 40. We know we have an actual URI if the 
			// string returned is greater than forty, due to the extra details
			// for the uri, etc.
    		if (m_Project != null && uri != null && uri.length() > 40)
    		{
    			String xmiid = element.getXMIID();
    			
				// Load all elements that are external so that 
				// all references to element can be cleaned up.
    			loadAllExternalElements();
    			Node projNode = m_Project.getNode();
    			UMLXMLManip.replaceReferences(projNode, uri, xmiid);
    			m_Project.setChildrenDirty(true);
    		}
    		Node elementNode = element.getNode();
    		removeElement(elementNode);
    	}
    }

	/**
	 *
	 * Retrieves every element in the associated Project that are version controlled.
	 *
	 * @param versionedElements[out] The elements under version control
	 *
	 * @return HRESULT
	 *
	 */
    public ETList<IVersionableElement> getAllVersionedElements()
    {
    	ETList<IVersionableElement> retObj = null;
    	if (m_Project != null)
    	{
    		loadAllExternalElements();
    		Node projNode = m_Project.getNode();
    		if (projNode != null)
    		{
    			retObj = (new ElementCollector<IVersionableElement>()).retrieveElementCollection(projNode, "//*[@isVersioned='true']", IVersionableElement.class);
    		}
    	}
        return retObj;
    }

	/**
	 *
	 * Ensures that the element passed in is situated in memory such that its immediate parent is loaded
	 * and available, and that element is properly pointing at that parent.
	 *
	 * @param element[in]         The element to verify.
	 * @param statusModified[out] - true if the node behind element has been modified, else
	 *                            - false if a modification was not necessary.
	 *
	 * @return HRESULT
	 *
	 */
    public boolean verifyInMemoryStatus(IVersionableElement element)
    {
    	boolean retVal = false;
    	if (element != null)
    	{
    		Node node = element.getNode();
    		if (node != null)
    		{
    			Node modChild = verifyInMemoryStatus(node);
    			if (modChild != null)
    			{
    				retVal = true;
    				element.setNode(modChild);
    			}
    		}
    	}
        return retVal;
    }

	/**
	 *
	 * Ensures that the node passed in is situated in memory such that its immediate parent is loaded
	 * and available, and that element is properly pointing at that parent.
	 *
	 * @param node[in]         The element to verify.
	 * @param actualNode[out]  If not 0, then the node passed in should be discarded and actualNode
	 *                         should not be used.
	 *
	 * @return HRESULT
	 *
	 */
	private Node verifyInMemoryStatus(Node node)
	{
		Node retNode = null;
		FactoryRetriever ret = FactoryRetriever.instance();
		
		// An element should not be on the cloned list if
		// we are manipulating it here.
		ret.clearClonedStatus(node);
		
		Node parent = getParentNode(node, true);
		Node modChild = null;//getModNode(node, parent);
		if (parent != null)
		{
			modChild = getModNode(node, parent, true);
		}
		if (modChild != null)
		{
			retNode = modChild;
		}
		else
		{
			String loaded = XMLManip.getAttributeValue(node, "loadedVersion");
			if (loaded != null && (loaded.length() == 0 || loaded.equals("false")))
			{
				loadExternalElement(node);
				String xmiId = XMLManip.getAttributeValue(node, "xmi.id");
				retNode = findByID(xmiId);
			}
         else
         {
            // Since loadedVersion is missing from the node lets make one more 
            // check to see if the node has been versioned.  If the node has
            // been versioned but the loadedVersion attribute is missing we 
            // we need to load the external element.
            String versioned = XMLManip.getAttributeValue(node, "isVersioned");
            if((versioned != null) && (versioned.length() == 0 || versioned.equals("true")))
            {
               loadExternalElement(node);
               String xmiId = XMLManip.getAttributeValue(node, "xmi.id");
               retNode = findByID(xmiId);
            }
         }
		}
		return retNode;
	}

	/**
	 *
	 * Retrieves the IPickListManager associated with this TypeManager
	 *
	 * @param pVal[out] The manager
	 *
	 * @return HRESULT
	 *
	 */
    public IPickListManager getPickListManager()
    {
        return m_rawPickManager;
    }

	/**
	 *
	 * Sets an IPickListManager on this TypeManager
	 *
	 * @param newVal[in] The new manager
	 *
	 * @return HRESULT
	 * @warning AddRef is NOT called on the incoming pointer.
	 *
	 */
    public void setPickListManager(IPickListManager value)
    {
		m_rawPickManager = value;
		if (m_rawPickManager != null)
		{
			m_rawPickManager.setTypeManager(this);
		}
    }

	/**
	 *
	 * Retrieves the first element found in this TypeManager's Project that matches the 
	 * passed in name. If more than one element by that name is found, the first one
	 * is returned.
	 *
	 * @param name[in]            The name of the element to find
	 * @param foundElement[out]   The found element.
	 *
	 * @return HRESULT
	 *
	 */
    public INamedElement getElementFromLibrariesByName(String name)
    {
    	INamedElement retEle = null;
    	ETList<INamedElement> elems = getElementsFromLibrariesByName(name);
    	if (elems != null)
    	{
    		if (elems.size() > 0)
    		{
    			retEle = elems.get(0);
    		}
    	}
        return retEle;
    }

	/**
	 *
	 * Attempts to find the types that have a name that matches the passed in name in one
	 * of the libraries the Project this TypeManager is associated with is referencing
	 *
	 * @param name[in]            The name to match against
	 * @param foundElements[out]  The found elements
	 *
	 * @return HRESULT
	 *
	 */
    public ETList<INamedElement> getElementsFromLibrariesByName(String name)
    {
    	ETList<INamedElement> retObj = null;
    	if (m_Project != null)
    	{
    		ETList<String> libraries = m_Project.getReferencedLibraries();
    		if (libraries != null)
    		{
    			int count = libraries.size();
    			if (count > 0)
    			{
    				IApplication app = getApplication();
    				if (app != null)
    				{
    					for (int i=0; i<count; i++)
    					{
    						String libLoc = libraries.get(i);
    						if (libLoc != null && libLoc.length() > 0)
    						{
    							IProject proj = getRefLibProject(app, libLoc);
    							if (proj != null)
    							{
    								ITypeManager typeMan = proj.getTypeManager();
    								if (typeMan != null)
    								{
    									retObj = typeMan.getCachedTypesByName(name);
    									if (retObj != null)
    									{
    										int numElems = retObj.size();
    										if (numElems > 0)
    										{
    											break;
    										}
    									}
    								}
    							}
    						}
    					}
    				}
    			}
    		}
    	}
        return retObj;
    }

	/**
	 *
	 * Adds an XMI ID to the cache of deleted IDs. This is used to prevent excessive 
	 * resolution logic on IDs that will never be found
	 *
	 * @param idOfDeletedItem[in] The ID to add to the cache
	 *
	 * @return HRESULT
	 *
	 */
    public void addToDeletedIds(String id)
    {
		if (id != null && id.length() > 0)
		{
			m_DeletedCache.add(id);
		}
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreCreate(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onElementPreCreate(String ElementType, IResultCell cell)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementCreated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onElementCreated(IVersionableElement element, IResultCell cell)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreDelete(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onElementPreDelete(IVersionableElement element, IResultCell cell)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementDeleted(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onElementDeleted(IVersionableElement element, IResultCell cell)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementPreDuplicated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onElementPreDuplicated(IVersionableElement element, IResultCell cell)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IElementLifeTimeEventsSink#onElementDuplicated(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onElementDuplicated(IVersionableElement element, IResultCell cell)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreNameModified(INamedElement element, String proposedName, IResultCell cell)
    {
        //do nothing
    }

	/**
	 *
	 * Called whenever an element's name is changing.
	 *
	 * @param element[in]
	 * @param IResultCell*[in]
	 *
	 * @return 
	 *
	 */
    public void onNameModified(INamedElement element, IResultCell cell)
    {
        if (processElement(element))
        {
        	Node node = retrieveExternalElementNode(element);
        	if (node != null)
        	{
        		String name = element.getName();
        		XMLManip.setAttributeValue(node, "name", name);
        	}
        	else
        	{
				// We got an element modified on an element that is version controlled, but
				// not in this type document. Not good.
        		node = element.getNode();
        		if (node != null)
        		{
        			addExternalElement(node);
        		}
        	}
        }
    }

	/**
	 *
	 * Given a VersionableElement, retrieves the Type node from this TypeManager based
	 * on the ID of the passed in VersionableElement
	 *
	 * @param element[in] The versionableElement to match IDs against
	 * @param node[out] The found node, else 0
	 *
	 * @return true if element has been versioned, else false
	 *
	 */
	private Node retrieveExternalElementNode( IVersionableElement element)
	{
		Node retNode = null;
		if (element != null)
		{
			Node verNode = element.getNode();
			ETPairT<String, Boolean> pair = UMLXMLManip.getVersionedURI(verNode);
			if (pair != null)
			{
				String uri = pair.getParamOne();
				if (uri != null && uri.length() > 0 && m_Doc != null)
				{
					String id = XMLManip.getAttributeValue(verNode, "xmi.id");
					if (id != null && !id.equals(uri))
					{
						Node extNode = XMLManip.findElementByID(m_Doc, id);
						if (extNode != null)
						{
							retNode = extNode;
						}
					}
				}
			}
		}
		return retNode;
	}

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreVisibilityModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, int, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreVisibilityModified(INamedElement element, int proposedValue, IResultCell cell)
    {
        //nothing to do
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onVisibilityModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onVisibilityModified(INamedElement element, IResultCell cell)
    {
        //nothing to do
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreAliasNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreAliasNameModified(INamedElement element, String proposedName, IResultCell cell)
    {
        //nothing to do
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onAliasNameModified(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onAliasNameModified(INamedElement element, IResultCell cell)
    {
        //nothing to do
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onPreNameCollision(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, java.lang.String)
     */
    public void onPreNameCollision(INamedElement element, String proposedName, 
        ETList<INamedElement> elems, IResultCell cell)
    {
        //nothing to do
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElementEventsSink#onNameCollision(org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement)
     */
    public void onNameCollision(INamedElement element, 
                                ETList<INamedElement> elems, IResultCell cell)
    {
        //nothing to do
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamespaceModifiedEventsSink#onPreElementAddedToNamespace(org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace, org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreElementAddedToNamespace(INamespace space, INamedElement elementToAdd, IResultCell cell)
    {
    	//nothing to do
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.INamespaceModifiedEventsSink#(org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace, org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onElementAddedToNamespace(INamespace space, INamedElement elementAdded, IResultCell cell)
    {
    	if (space != null && elementAdded != null)
    	{
    		if (processElement(elementAdded))
    		{
				// Makes sure the href attribute on the corresponding entry
				// in the type file stays accurate.
    			Node node = retrieveExternalElementNode(elementAdded);
    			if (node != null && m_Doc != null)
    			{
    				Node verNode = elementAdded.getNode();
    				ETPairT<String, Boolean> verURI = UMLXMLManip.getVersionedURI(verNode);
    				String uri = verURI.getParamOne();
    				ETPairT<String, String> obj = URILocator.uriparts(uri);
    				String docLoc = obj.getParamOne();
    				String nodeLoc = obj.getParamTwo();
    				setLocationValue(node, docLoc);
    			}
    		}
    	}
    }
    
	/**
	 *
	 * Sets the location value for the Location element that is referred to by the passed
	 * in Type element
	 *
	 * @param typeNode[in]        The Type element referring to the Location element we
	 *                            need to affect
	 * @param newLocation[in]     The new href value to place on the found Location element
	 *
	 * @return HRESULT
	 *
	 */
    private void setLocationValue(Node typeNode, String newLoc)
    {
    	Node locNode = getLocationNode(typeNode);
    	if (locNode != null)
    	{
    		XMLManip.setAttributeValue(locNode, "href", newLoc);
    	}
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink#onExternalElementPreLoaded(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onExternalElementPreLoaded(String uri, IResultCell cell)
    {
    	//nothing to do
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink#onExternalElementLoaded(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onExternalElementLoaded(IVersionableElement element, IResultCell cell)
    {
		// Make sure this element is part of the type file
		PreventReEntrance reenter = new PreventReEntrance();
      reenter.startBlocking(0);
		try
		{
			if (!reenter.isBlocking())
			{
				if (isWatchedElement(element))
				{
					Node node = element.getNode();
					if (node != null)
					{
						Node typeNode = isTypePresent(node);
						if (typeNode == null)
						{
							// This really should never happen, but just in case,
							// make sure this type is in our .ettm file
							addToTypes(element);
						}
					}
				}
			}
		}
		finally
		{
			reenter.releaseBlock();
		}
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink#onPreInitialExtraction(java.lang.String, org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreInitialExtraction(String fileName, IVersionableElement element, IResultCell cell)
    {
    	//nothing to do
    }

	/**
	 *
	 * Makes an entry in the Type file
	 *
	 * @param element[in]
	 * @param cell[in]
	 *
	 * @return 
	 *
	 */
    public void onInitialExtraction(IVersionableElement element, IResultCell cell)
    {
    	if (isWatchedElement(element))
    	{
    		addToTypes(element);
    	}
    }

    protected void finalize()
    {
        revokeEventSinks();
    }
 
	/**
	 *
	 * Retrieves the Project that has the passed in ID from the Application
	 *
	 * @param id[in]        The ID of the Project
	 * @param pProj[out]    The found Project, else 0
	 *
	 * @return HRESULT
	 *
	 */
	private IProject getProjectByID(String id)
	{
		IProject retProj = null;
		ICoreProduct prod = ProductRetriever.retrieveProduct();
		if (prod != null)
		{
			IApplication pApp = prod.getApplication();
			if (pApp != null)
			{
				retProj = pApp.getProjectByID(id);
			}
		}
		return retProj;
	}

	/**
	 *
	 * Retrieves the absolute path of the relative path passed in. It is assumed
	 * that the relative path is to the Project directory.
	 *
	 * @param relativeLoc[in] The relative path to the file. Relative to the Project.
	 *
	 * @return The absolute path, else ""
	 *
	 */
	private String retrieveAbsolutePathFromProject( String relativeLoc )
	{
		String retStr = "";
		if (m_Project != null)
		{
			String projFileName = m_Project.getFileName();
			if (projFileName != null && projFileName.length() > 0)
			{
				if (relativeLoc != null && relativeLoc.length() > 0)
				{
					retStr = PathManip.retrieveAbsolutePath(relativeLoc, projFileName);
				}
			}
		}
		return retStr;
	}

	/**
	 *
	 * Looks up an entry in the Type file, returning the HREF of the entry.
	 *
	 * @param id[in] The actual xmi.id to look up. This is an id that is not
	 *               wrapped in a URI.
	 * @param actualID[out] The HREF of the id passed in. This is the HREF
	 *                      value to be resolved. It is a fully decorated
	 *                      href in the form < doc >#< xpath to node >.
	 *
	 * @return HRESULT
	 *
	 */
	private String retrieveTypeHref( String id )
	{
		String retStr = id;
		String projId = retrieveRawXMIID(id);
		Node node = XMLManip.findElementByID(m_Doc, projId);
		if (node != null)
		{
			Node locationNode = getLocationNode(node);
			if (locationNode != null)
			{
				String xpath = XMLManip.getAttributeValue(locationNode, "href");

				// The href attribute in the type file only contains the relative
				// path to the .etx file. Need to create the actual xpath expression
				xpath += "#id( '";
				xpath += id;
				xpath += "' )";
				retStr = xpath;
			}
		}
		return retStr;
	}

	/**
	 *
	 * Given the passed in node that represents a Type element, return the Location element
	 * given the locID on the TYpe element.
	 *
	 * @param typeNode[in]        The incoming Type element that holds the id of the Location
	 *                            element we want
	 * @param locationNode[out]   The found Location element
	 *
	 * @return HRESULT
	 *
	 */
	private Node getLocationNode( Node typeNode)
	{
		Node retNode = null;
		String locId = XMLManip.getAttributeValue(typeNode, "location");
		if (locId != null && locId.length() > 0)
		{
			retNode = XMLManip.findElementByID(m_Doc, locId);
		}
		return retNode;
	}

    private static final String IDR_TYPE_XML = "type_fil.htm";
    
    private IProject m_Project;
    private Document m_Doc;
    
    private IPickListManager                m_rawPickManager;
    private IElementChangeEventDispatcher   m_Dispatcher;
    private IElementLifeTimeEventDispatcher m_LifeTimeDispatcher;
    private String                          m_ProjectID;
    private String                          m_ProjectFileName;
    private State m_State = null;
    
    /**
     * XMI IDs of deleted elements.
     */
    private HashSet<String> m_DeletedCache = new HashSet<String>();

    /**
     * XMI IDs that could not be resolved?
     */
    private HashSet<String> m_UnresolvedIDs = new HashSet<String>();
    
    private class ImportInfo
    {
    	private String m_Href = "";
    	private String m_ProjLocation = "";
    	private String m_ProjectID = "";
      
      public boolean isImported()
      {
         return (m_ProjectID != null) && (m_ProjectID.length() > 0);
      }
    }
    
    private class State
    {
    	private State m_MemberState = null;
    	private boolean m_RecreatingTypeFile = false;
    	private IProject m_Project = null;
    	
    	public State(State member, IProject proj, boolean recreating)
    	{
    		m_Project = proj;
    		m_RecreatingTypeFile = recreating;
    		m_MemberState = this;
    	}
    	
    	public String projectFileName()
    	{
    		String fileName = "";
    		if (m_Project != null)
    		{
    			fileName = m_Project.getFileName();
    		}
    		return fileName;
    	}
    	
    	public String projectID()
    	{
    		String retStr = "";
    		if (m_Project != null)
    		{
    			retStr = m_Project.getXMIID();
    		}
    		return retStr;
    	}
    	
    	public boolean reCreating()
    	{
    		return m_RecreatingTypeFile;
    	}
    }
}
