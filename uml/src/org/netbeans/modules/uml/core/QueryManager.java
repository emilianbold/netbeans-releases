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
package org.netbeans.modules.uml.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductEventDispatcher;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementChangeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink;
import org.netbeans.modules.uml.core.metamodel.structure.IStructureEventDispatcher;
import org.netbeans.modules.uml.core.support.umlsupport.FileSysManip;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.IStrings;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class QueryManager implements IQueryManager, ICoreProductInitEventsSink,
                                    IExternalElementEventsSink,
                                    IProjectEventsSink
{
    private static final Logger logger = Logger.getLogger("org.netbeans.modules.uml.core");
    public static final String QUERY_CACHE = ".QueryCache"; //NOI18N
    private boolean m_deInitialized = false;
    private String m_CacheDir = "";

    //Hashtable<String, ETList<IQueryUpdater>> m_Updaters = null;
    private Hashtable m_Updaters = new Hashtable();
	
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.IQueryManager#initialize()
     */
    public void initialize()
    {
        registerForCoreProductEvents();
        registerForExternalLoadEvents();
    }

    private void registerForExternalLoadEvents()
    {
        IElementChangeEventDispatcher dispatcher =
            (IElementChangeEventDispatcher) EventDispatchRetriever
                .instance()
                .getDispatcher(
                EventDispatchNameKeeper.modifiedName());
        if (dispatcher != null)
            dispatcher.registerForExternalElementEventsSink(this);
    }

    /**
     * Registers the core product sink.  Used so we can get the prequit.
     */
    private void registerForCoreProductEvents()
    {
        ICoreProductEventDispatcher dispatcher =
            (ICoreProductEventDispatcher) EventDispatchRetriever
                .instance()
                .getDispatcher(
                EventDispatchNameKeeper.coreProduct());
        if (dispatcher != null)
            dispatcher.registerForInitEvents(this);
    }

	/**
	 *
	 * Attaches this manager as a listener to the StructureEventDispatcher
	 *
	 * @return * @return S_OK, else QM_E_NO_STRUCTURE_EVENTS if there was a problem
	 *         connecting to the Structure event dispatcher.
	 *
	 */
	private void registerForStructureEvents()
	{
		IStructureEventDispatcher disp = null;
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		Object obj = ret.getDispatcher(EventDispatchNameKeeper.structure());
		if (obj != null && obj instanceof IStructureEventDispatcher)
		{
			disp = (IStructureEventDispatcher)obj;
		}
		if (disp != null)
		{
			disp.registerForProjectEvents(this);
		}
	}

	/**
	 *
	 * Detaches from the WorkspaceEventDispatcher
	 *
	 */
	private void revokeStructureEvents()
	{
		IStructureEventDispatcher disp = null;
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		Object obj = ret.getDispatcher(EventDispatchNameKeeper.structure());
		if (obj != null && obj instanceof IStructureEventDispatcher)
		{
			disp = (IStructureEventDispatcher)obj;
		}
		if (disp != null)
		{
			disp.revokeProjectSink(this);
		}
	}

	/**
	 *
	 * Revokes the external load sink
	 *
	 */
	private void revokeExternalLoadEvents()
	{
		IElementChangeEventDispatcher dispatcher = null;
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		Object obj = ret.getDispatcher(EventDispatchNameKeeper.modifiedName());
		if (obj != null && obj instanceof IElementChangeEventDispatcher)
		{
			dispatcher = (IElementChangeEventDispatcher)obj;
		}
		if (dispatcher != null)
		{
			dispatcher.revokeExternalElementEventsSink(this);
		}
	}

	/**
	 *
	 * Revokes the core product sink.  Used so we can get the prequit
	 *
	 */
	private void revokeCoreProductEvents()
	{
		ICoreProductEventDispatcher dispatcher = null;
		EventDispatchRetriever ret = EventDispatchRetriever.instance();
		Object obj = ret.getDispatcher(EventDispatchNameKeeper.coreProduct());
		if (obj != null && obj instanceof ICoreProductEventDispatcher)
		{
			dispatcher = (ICoreProductEventDispatcher)obj;
		}
		if (dispatcher != null)
		{
			dispatcher.revokeInitSink(this);
		}
	}

	/**
	 *
	 * Retrieves the application off the ICoreProduct
	 *
	 * @param app[out] The application, else 0 on error
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
	 * Deinitializes this manager
	 *
	 * @return HRESULT
	 *
	 */
    public void deinitialize()
    {
    	if (!m_deInitialized)
    	{
    		revokeCoreProductEvents();
    		revokeExternalLoadEvents();
    		m_deInitialized = true;
    	}
    }

	/**
	 *
	 * Retrieves the updaters this manager has collected
	 *
	 * @param pVal[out] The collection of updaters
	 *
	 * @return HRESULT
	 *
	 */
    public ETList<IQueryUpdater> getUpdaters()
    {
    	ETList<IQueryUpdater> retObj = null;
    	if (m_Updaters != null)
    	{
    		Enumeration iter = m_Updaters.elements();
    		while (iter.hasMoreElements())
    		{
    			Object obj = iter.nextElement();
    			if (obj instanceof ETList)
    			{
    				ETList<IQueryUpdater> list = new ETArrayList<IQueryUpdater>((Collection)obj);
    				for (int i=0; i<list.size(); i++)
    				{
    					retObj.add(list.get(i));
    				}
    			}
    		}
    	}
        return retObj;
    }

	/**
	 *
	 * Retrieves updaters specified in the cache
	 *
	 * @param project[in]   The project to associate found updaters with
	 * @param cache[in]     The .QueryCache that specifies the updaters
	 *
	 * @return HRESULT
	 *
	 */
	private void gatherUpdaters( IProject project, Document cache)
	{
        if (project == null || cache == null) return ;
        
		// Make sure any Updaters already associated with the project are removed.
		String projId = project.getXMIID();
		Object obj = m_Updaters.get(projId);
		if (obj != null)
		{
			deinitializeUpdaters(new ETArrayList<IQueryUpdater>((Collection)obj));
			m_Updaters.remove(projId);
		}
		
		List updaterAtts = cache.selectNodes("//@updater");
		if (updaterAtts != null)
		{
			createUpdaters(project, cache, updaterAtts);
		}
	}

	/**
	 * Deinitializes the updaters found in the collection passed in
	 * 
	 * @param updaters[in]  The collection to deinit
	 * 
	 * @return HRESULT 
	 */
	private void deinitializeUpdaters( ETList<IQueryUpdater> updaters )
	{
		if (updaters != null)
		{
			int count = updaters.size();
			for (int i=0; i<count; i++)
			{
				IQueryUpdater updater = updaters.get(i);
				updater.deinitialize();
			}
		}
	}

	/**
	 *
	 * Makes sure all threads managed by this manager are done running
	 *
	 */
	private void saveCRCs(IProject proj)
	{
		// Generate new CRCs for each CachedQueries element found in the .QueryCache
		Document doc = openQueryCache(proj);
		if (doc != null)
		{
			String projFilename = proj.getFileName();
			setCRC(doc, projFilename);
			IStrings files = getExternalFiles(proj);
			if (files != null)
			{
				int count = files.getCount();
				for (int i=0; i<count; i++)
				{
					String filename = files.item(i);
					if (filename != null && filename.length() > 0)
					{
						setCRC(doc, filename);
					}
				}
			}
			updateCache(proj, doc);
			saveCache(doc, proj);
		}
	}

	/**
	 *
	 * Gives all the IQueryUpdaters associated with the passed in IProject
	 * to update the cache before the cache is saved
	 *
	 * @param project[in]      The IProject
	 * @param cache[in]        The cache
	 *
	 * @return HRESULT
	 *
	 */
	private void updateCache(IProject proj, Document doc)
	{
		if (proj != null)
		{
			String id = proj.getXMIID();
			if (m_Updaters != null)
			{
				Object obj = m_Updaters.get(id);
				if (obj != null)
				{
					ETList<IQueryUpdater> updaters = new ETArrayList<IQueryUpdater>((Collection)obj);
					int count = updaters.size();
					for (int i=0; i<count; i++)
					{
						IQueryUpdater updater = updaters.get(i);
						updater.updateCache(doc);
						updater.deinitialize();
					}
				}
			}
		}
	}

	/**
	 *
	 * Sets the CRC on the cached entry that matches the passed in filename if 
	 * the current crc does not match the new crc
	 *
	 * @param cache[in]     The xml doc that represents the .QueryCache
	 * @param fileName[in]  The file that needs to be checked
	 *
	 * @return HRESULT
	 *
	 */
	private void setCRC(Document doc, String filename)
	{
		if (filename != null && filename.length() > 0)
		{
			Element cacheQuery = getCRCValuesElement(doc, filename);
			String oldCRC = getCRCValuesOld(cacheQuery);
			String newCRC = getCRCValuesNew(filename);
			if (cacheQuery != null && 
		  	  (oldCRC != null? !oldCRC.equals(newCRC) : newCRC != null))
			{
				XMLManip.setAttributeValue(cacheQuery, "crc", newCRC);
			}
		}
	}

	/**
	 *
	 * Creates and initializes all IQueryUpdaters that are able to be CoCreated given the ProgID
	 * values found in the passed in XMLDOMAttribute list
	 *
	 * @param project[in]         The project to associate the updaters with
	 * @param cache[in]           The cache the updaters should be updating
	 * @param updaterAtts[in]     A collection of xml attributes that contain prog ids to CoClasses implementing
	 *                            the IQueryUpdater interface
	 *
	 * @return HRESULT
	 *
	 */
	private void createUpdaters(IProject project, Document cache, List updaterAtts )
	{
		ETList<IQueryUpdater> updaters = null;
		if (updaterAtts != null && updaterAtts.size() > 0)
		{
			int count = updaterAtts.size();
			updaters = new ETArrayList<IQueryUpdater>();
			//HashMap<String, boolean> foundUpdaters;
			HashMap foundUpdaters = new HashMap();
			for (int i=0; i<count; i++)
			{
				Node node = (Node)updaterAtts.get(i);
				if (node.getNodeType() == Node.ATTRIBUTE_NODE)
				{
					Attribute attr = (Attribute)node;
					String progId = attr.getValue();
					if (progId != null && progId.length() > 0)
					{
						if (!foundUpdaters.containsKey(progId))
						{
							foundUpdaters.put(progId, Boolean.valueOf(true));
							try
							{
                                                            IQueryUpdater updater = null;
                                                            //Object obj = project.getTypeManager().getPickListManager();

                                                            /*if (obj != null && obj.getClass().getName().equals(progId)) {
                                                                updater = (IQueryUpdater)obj;
                                                            } else */ {
								Object obj = Class.forName(progId).newInstance();
								if (obj instanceof IQueryUpdater)
								{
                                                                    updater = (IQueryUpdater)obj;
                                                                    updater.initialize(project, cache);
								}
                                                            }

                                                            if (updater != null) {
                                                                updaters.add(updater);
                                                            }
							}
							catch (ClassNotFoundException e1)
							{
								e1.printStackTrace();
							}
							catch (InstantiationException e2)
							{
								e2.printStackTrace();
							}
							catch (IllegalAccessException e3)
							{
								e3.printStackTrace();
							}
						}
					}
				}
			}
		}
		else
		{
			// Retrieve the default updater, if any
			if (cache != null)
			{
				Element root = cache.getRootElement();
				if (root != null)
				{
					String progId = XMLManip.getAttributeValue(root, "defaultUpdater");
					if (progId != null && progId.length() > 0)
					{
						updaters = new ETArrayList<IQueryUpdater>();
						try
						{
							Object obj = Class.forName(progId).newInstance();
							if (obj != null && obj instanceof IQueryUpdater)
							{
								IQueryUpdater updater = (IQueryUpdater)obj;
								updater.initialize(project, cache);
								updaters.add(updater);
							}
						}
						catch (ClassNotFoundException e1)
						{
							e1.printStackTrace();
						}
						catch (InstantiationException e2)
						{
							e2.printStackTrace();
						}
						catch (IllegalAccessException e3)
						{
							e3.printStackTrace();
						}
					}
				}
			}
		}
		
		if (updaters != null && project != null)
		{
			String projId = project.getXMIID();
			if (m_Updaters == null)
			{
				m_Updaters = new Hashtable();
			}
			m_Updaters.put(projId, updaters);
		}
	}

	/**
	 *
	 * Creates the .QueryCache file in the passed in location.
	 *
	 * @param pProject[in] The Project we are creating a .QueryCache file for
	 *
	 * @return HRESULT
	 *
	 */
	private void establishQueryCache( IProject pProject )
	{
		m_CacheDir = "";

		// Determine whether or not we are modifying a cache or creating a new
		// one.
		String path = getQueryCachePath(pProject);
		File file = new File(path);
		if (file.exists())
		{
			verifyCacheContents(pProject);
		}
		else
		{
			createNewQueryCache(pProject);
		}
	}

	/**
	 *
	 * Checks CRC settings, rebuilding cache results accordingly
	 *
	 * @param project[in]   The project being checked
	 *
	 * @return HRESULT
	 *
	 */
	private void verifyCacheContents(IProject pProject)
	{
		// Generate new CRCs for each CachedQueries element found in the .QueryCache
		Document doc = openQueryCache(pProject);
		IQueryBuilder builder = establishQueryBuilder(pProject);
		rebuildCacheIfNeeded(doc, builder, pProject);
		
		IStrings files = getExternalFiles(pProject);
		if (files != null)
		{
			String projFileName = pProject.getFileName();
			String projPath = "";
			if (projFileName != null && projFileName.length() > 0)
			{
				projPath = StringUtilities.getPath(projFileName);
			}
			
			int count = files.getCount();
			for (int i=0; i<count; i++)
			{
				String fileName = files.item(i);
				if (fileName != null && fileName.length() > 0)
				{
					String filePath = StringUtilities.getPath(fileName);
					if (projPath != null && filePath != null)
					{
						// We only want to check files that are directly associated
						// with this Project. Specifically, we don't want to try 
						// a rebuild QueryCache files from files that are imported
						// from other projects
						if (projPath.equals(filePath))
						{
							rebuildCacheIfNeeded(doc, builder, fileName);
						}
					}
				}
			}
		}
		gatherUpdaters(pProject, doc);
		saveCache(doc, pProject);
	}

	/**
	 *
	 * Saves the .QueryCache file
	 *
	 * @param cache[in]     The actual document to save
	 * @param project[in]   The Project associated with the document
	 *
	 * @return HRESULT
	 *
	 */
        private void saveCache(Document doc, IProject pProject) {
            if (doc == null || pProject == null) {
                return;
            }

            String path = getQueryCachePath(pProject);

            // The path to the .QueryCache file will be the absolute path to 
            // the directory that the Project is in, in every case EXCEPT when
            // the Project is created. In this case, we won't actually save the
            // .QueryCache file. We still NEED to go through the motions of establishing
            // the cache file, as necessary updaters are established etc.
            if (path != null && !path.equals(".QueryCache")) {
                try {
                    File file = new File(path);
                    FileObject fo = FileUtil.createData(file);
                    XMLManip.save(doc, path);
                } catch (IOException ex) {
                    String mesg = ex.getMessage();
                    logger.log(Level.WARNING, mesg != null ? mesg : "", ex);
                }
            }
        }

	/**
	 *
	 * Creates a new QueryCache.
	 *
	 * @param pProject[in] The project to make the cache for
	 *
	 * @return HRESULT
	 *
	 **/
	private void createNewQueryCache(IProject pProject)
	{
		// Retrieve all the external files associated with pProject, creating
		// sections in the .QueryCache file per external doc.

		// Create the .QueryCache xml file
		Document doc = createCacheFile();
		if (doc != null)
		{
			// Now generate results just for the project node, then for external files
			IQueryBuilder builder = establishQueryBuilder(pProject);
			doc = addResultsToCache(doc, builder, pProject, null);
			
			// Now gather the external files and run the QueryBuilder
			// on each of the VersionedElements in those files
			IStrings files = getExternalFiles(pProject);
			if (files != null)
			{
				int numFiles = files.getCount();
				for (int i=0; i<numFiles; i++)
				{
					String fileName = files.item(i);
					if (fileName != null && fileName.length() > 0)
					{
						processExternalFile(doc, builder, fileName, null);
					}
				}
			}
			setDefaultUpdater(builder, doc);
			gatherUpdaters(pProject, doc);
			saveCache(doc, pProject);
		}
	}

	/**
	 *
	 * Sets the progid to use to create a default updater when no results are present
	 *
	 * @param builder[in]      The builder to retrieve the info from
	 * @param queryDoc[in]     The document to set the info
	 *
	 * @return HRESULT
	 *
	 */
	private void setDefaultUpdater(IQueryBuilder builder, Document doc)
	{
		if (builder != null)
		{
			String defaultProgId = builder.getDefaultUpdaterProgId();
			if (defaultProgId != null && defaultProgId.length() > 0)
			{
				if (doc != null)
				{
					Element root = doc.getRootElement();
					if (root != null)
					{
						XMLManip.setAttributeValue(root, "defaultUpdater", defaultProgId);
					}
				}
			}
		}
	}

	/**
	 *
	 * Creates the .QueryCache document
	 *
	 * @param doc[out]   The created document
	 *
	 * @return HRESULT
	 *
	 */
	private Document createCacheFile()
	{
		Document retDoc = XMLManip.getDOMDocument();
		if (retDoc != null)
		{
			//ProcessingInstruction pi = new DefaultProcessingInstruction("xml", "version = '1.0'");
			//retDoc.add(pi);
			
			// Create the QueryCache element
			Element elem = XMLManip.createElement(retDoc, "QueryCache");
			//Here we do not need a namespaced document, so just set this element as the root element
			retDoc.setRootElement(elem);
			//retDoc.add(elem);
		}
		return retDoc;
	}

	/**
	 *
	 * Retrieves the location of the QuerySchemas.etc file
	 *
	 * @return The absolute location
	 *
	 */
	private String getSchemaLocation()
	{
		String retLoc = "";
		ICoreProduct prod = ProductRetriever.retrieveProduct();
      if (prod != null)
		{
			IConfigManager conMan = prod.getConfigManager();
      	if (conMan != null)
			{
				retLoc = conMan.getDefaultConfigLocation();
				retLoc += "QuerySchemas.etc";
			}
		}
      
      return retLoc;
	}

	/**
	 *
	 * Appends the results of a QueryBuilder process to the QueryCache document
	 *
	 * @param queryCacheDoc[in]   The final QueryCache document
	 * @param resultsDoc[in]      The results document
	 * @param fileName[in]        Name of the file the resultsDoc was generated from. What file the node that 
	 *                            the results were generated from, that is.
	 *
	 * @return HRESULT
	 *
	 */
	private Document appendResults(Document queryCacheDoc, Document resultsDoc, String fileName, String newCRC )
	{
		// Retrieve the CachedQueries element from the results doc
      if (resultsDoc != null)
      {
         Node queriesNode = resultsDoc.selectSingleNode("//CachedQueries");
         if (queriesNode != null)
         {
            // Get the root element in the queryCacheDoc, which is the QueryCache element
            Node queryCacheNode = queryCacheDoc.selectSingleNode("//QueryCache");
            if (queryCacheNode != null)
            {
               if (fileName != null && fileName.length() > 0)
               {
                  String relPath = FileSysManip.retrieveRelativePath(fileName, m_CacheDir);

                  // Set the fileName of the Project on the CachedQueries element
                  XMLManip.setAttributeValue(queriesNode, "fileName", relPath);
               
                  if (newCRC != null)
                  {
                     XMLManip.setAttributeValue(queriesNode, "crc", newCRC);
                  }
               }

               // And then append the CachedQueries node to the QueryCache element in the .QueryCache
               // XML doc
               queriesNode.detach();
               ((Element)queryCacheNode).add(queriesNode);
            }
         }
      }
		return queryCacheDoc;
	}

	/**
	 *
	 * Runs the QueryBuilder against the first child element of the external doc's VersionedElement element
	 * and appends the results to the passed in queryCacheDoc
	 *
	 * @param queryCacheDoc[in]   The document that is the .QueryCache doc
	 * @param builder[in]         The QueryBuilder to use
	 * @param fileName[in]        The filename of the external doc
	 *
	 * @return HRESULT
	 *
	 */
	private void processExternalFile(Document queryCacheDoc, IQueryBuilder builder, String fileName, String newCRC )
	{
		if (fileName != null && fileName.length() > 0)
		{
			// Load the file pointed to by filename, attempting to retrieve the VersionedElement out of it
			Document doc = XMLManip.getDOMDocument(fileName);
			if (doc != null)
			{
				Node node = doc.selectSingleNode("VersionedElement/child::*[1]");
				if (node != null)
				{
					// Now generate the Queries against the found node
					Document resultsDoc = builder.generateResults(node);
					if (resultsDoc != null)
					{
						appendResults(queryCacheDoc, resultsDoc, fileName, newCRC);
					}
				}
			}
		}
	}

	/**
	 *
	 * Retrieves the path the the passed in Project's .QueryCache file
	 *
	 * @param project[in]   The project in question
	 *
	 * @return The absolute path to the .QueryCache file
	 *
	 */
	private String getQueryCachePath( IProject project )
	{
		String fileName = "";
		if (project != null)
		{
			String name = project.getFileName();
			fileName = StringUtilities.getPath(name);
			m_CacheDir = fileName;
			fileName += QUERY_CACHE;
		}
		return fileName;
	}

	/**
	 *
	 * Opens the .QueryCache file associated with the passed in IProject
	 *
	 * @param project[in]   The project whose .QueryCache file we need to open
	 * @param doc[out]      The opened doc, else 0 on error
	 *
	 * @return HRESULT
	 *
	 */
	private Document openQueryCache( IProject project)
	{
		Document retDoc = null;
		String path = getQueryCachePath(project);
		retDoc = XMLManip.getDOMDocument(path);
		return retDoc;
	}

	/**
	 *
	 * Retrieves all the external files associated with the Project
	 *
	 * @param project[in]   The Project
	 * @param files[out]    All the absolute paths to the external files
	 *
	 * @return HRESULT
	 *
	 */
	private IStrings getExternalFiles( IProject project )
	{
		IStrings retObj = null;
		if (project != null)
		{
			ITypeManager typeMan = project.getTypeManager();
			if (typeMan != null)
			{
				retObj = typeMan.gatherExternalFileLocations();
			}
		}
		return retObj;
	}

	private void rebuildCacheIfNeeded(Document cache, IQueryBuilder builder, String fileName )
	{
		String newCRC = requiresCacheRebuild(cache, fileName);
		if (newCRC != null)
		{
			processExternalFile(cache, builder, fileName, newCRC);
		}
	}

	/**
	 *
	 * Determines whether or not the CachedQueries associated witht IProject needs to be rebuilt.
	 * If they do, the new results are added to the .QueryCache document
	 *
	 * @param cache[in]     The cache document
	 * @param builder[in]   The builder to create the results if needed
	 * @param project[in]   The project
	 *
	 * @return HRESULT
	 *
	 */
	private void rebuildCacheIfNeeded(Document cache, IQueryBuilder builder, IProject project)
	{
		if (project != null)
		{
			String filename = project.getFileName();
			String newCRC = requiresCacheRebuild(cache, filename);
			if (newCRC != null)
			{
				addResultsToCache(cache, builder, project, newCRC);
			}
		}
	}

	/**
	 *
	 * Determines whether or not a CachedQueries element needs to be rebuilt
	 *
	 * @param cache[in]     The xml doc representing the .QueryCache
	 * @param fileName[in]  The file to check against
	 *
	 * @return HRESULT
	 *
	 */
	private String requiresCacheRebuild(Document cache, String fileName)
	{
		String retStr = null;
		if (cache != null && fileName != null)
		{
			Element cacheQuery = getCRCValuesElement(cache, fileName);
			String oldCRC = getCRCValuesOld(cacheQuery);
			String newCRC = getCRCValuesNew(fileName);
			if (newCRC != null? !newCRC.equals(oldCRC) : oldCRC != null)
			{
				if (cacheQuery != null)
				{
					// Remove the current CachedQueries node. It needs to be rebuilt
					Node parent = cacheQuery.getParent();
					if (parent != null)
					{
						cacheQuery.detach();
					}
				}
				retStr = newCRC;
			}
		}
		return retStr;
	}

	/**
	 * @param cache
	 * @param fileName
	 * @return
	 */
	private String getCRCValuesNew(String fileName)
	{
		try
		{
			File f = new File(fileName);
			if (f.exists())
			{
				FileInputStream fis = new FileInputStream(f);
				byte[] buffer = new byte[1000];
				CRC32 crc = new CRC32();
				int bytesRead;
				while ((bytesRead = fis.read(buffer)) > 0)
				{
					crc.update(buffer, 0, bytesRead);
				}
				fis.close();
				return String.valueOf(crc.getValue());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param cache
	 * @param fileName
	 * @return
	 */
	private String getCRCValuesOld(Element queryElem)
	{
		String retStr = null;
		if (queryElem != null)
		{
			retStr = XMLManip.getAttributeValue(queryElem, "crc");
		}
		return retStr;
	}

	/**
	 * @param cache
	 * @param fileName
	 * @return
	 */
	private Element getCRCValuesElement(Document cache, String fileName)
	{
		Element retEle = null;
		if (fileName != null && fileName.length() > 0)
		{
			String relPath = FileSysManip.retrieveRelativePath(fileName, m_CacheDir);
			
			// Locate the CachedQueries node that contains the file name passed in
			String query = "//CachedQueries[@fileName=\"";
			query += relPath;
			query += "\"]";
			
			if (cache != null)
			{
				Node queryCache = cache.selectSingleNode(query);
				if (queryCache != null && queryCache.getNodeType() == Node.ELEMENT_NODE)
				{
					retEle = (Element)queryCache;
				}
			}
		}
		return retEle;
	}

	/**
	 *
	 * Creates a new QueryBuilder object
	 *
	 * @param builder[out] The new builder
	 *
	 * @return HRESULT
	 *
	 */
	private IQueryBuilder establishQueryBuilder( IProject pProject)
	{
		IQueryBuilder retObj = new QueryBuilder();
		String configLoc = getSchemaLocation();
		retObj.setSchemaLocation(configLoc);
		if (pProject != null)
		{
			String projId = pProject.getXMIID();
			retObj.setProjectId(projId);
		}
		return retObj;
	}

	/**
	 *
	 * Adds the results of the QueryBuilder running on the Project to the .QueryCache document
	 *
	 * @param cache[in]     The .QueryCache doc
	 * @param builder[in]   The QueryBuilder
	 * @param project[in]   The Project
	 *
	 * @return HRESULT
	 *
	 */
	private Document addResultsToCache(Document cache, IQueryBuilder builder, IProject project, String newCRC )
	{
		Document retDoc = null;
		if (project != null)
		{
			Node projNode = project.getNode();
			Document resultDoc = builder.generateResults(projNode);
			String projFileName = project.getFileName();
			retDoc = appendResults(cache, resultDoc, projFileName, newCRC);
		}
		return retDoc;
	}

	/**
	 *
	 * Allows external users to manually update the .QueryCache file of the passed in Project
	 *
	 * @param pProject[in]  An IDispatch pointer of an IProject
	 *
	 * @return HRESULT
	 *
	 */
    public void establishCache(IProject pProject)
    {
    	if (pProject != null)
    	{
    		establishQueryCache(pProject);
    	}
    }

	public void closeCache(IProject pProject)
	{
		if (pProject != null)
		{
			saveCRCs(pProject);
			removeUpdaters(pProject);
		}
	}

	/**
	 *
	 * Removes the collection of updaters associated with the passed in Project
	 *
	 * @param pProj[in]  The project
	 *
	 * @return HRESULT
	 *
	 */
	private void removeUpdaters(IProject pProject)
	{
		if (pProject != null)
		{
			String projId = pProject.getXMIID();
			if (m_Updaters != null)
			{
				Object obj = m_Updaters.get(projId);
				if (obj != null)
				{
					m_Updaters.remove(projId);
				}
			}
		}
	}

	/* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreInit(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onCoreProductPreInit(ICoreProduct pVal, IResultCell cell)
    {
        //do nothing
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductInitialized(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onCoreProductInitialized(ICoreProduct newVal, IResultCell cell)
    {
        //do nothing
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreQuit(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onCoreProductPreQuit(ICoreProduct pVal, IResultCell cell)
    {
        deinitialize();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreSaved(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onCoreProductPreSaved(ICoreProduct pVal, IResultCell cell)
    {
        //do nothing
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductSaved(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onCoreProductSaved(ICoreProduct newVal, IResultCell cell)
    {
        //do nothing
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink#onExternalElementPreLoaded(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onExternalElementPreLoaded(String uri, IResultCell cell)
    {
        //do nothing
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink#onExternalElementLoaded(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onExternalElementLoaded(IVersionableElement element, IResultCell cell)
    {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink#onPreInitialExtraction(java.lang.String, org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreInitialExtraction(String fileName, IVersionableElement element, IResultCell cell)
    {
        //do nothing
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink#onInitialExtraction(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onInitialExtraction(IVersionableElement element, IResultCell cell)
    {
        //do nothing
    }

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onPreModeModified(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreModeModified(IProject pProject, String newValue, IResultCell cell)
	{
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onModeModified(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onModeModified(IProject pProject, IResultCell cell)
	{
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onPreDefaultLanguageModified(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreDefaultLanguageModified(IProject pProject, String newValue, IResultCell cell)
	{
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onDefaultLanguageModified(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onDefaultLanguageModified(IProject pProject, IResultCell cell)
	{
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectPreCreate(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectPreCreate(IWorkspace space, IResultCell cell)
	{
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectCreated(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectCreated(IProject project, IResultCell cell)
	{
		establishQueryCache(project);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectPreOpen(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectPreOpen(IWorkspace space, String projName, IResultCell cell)
	{
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectOpened(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectOpened(IProject project, IResultCell cell)
	{
		establishQueryCache(project);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectPreRename(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectPreRename(IProject Project, String newName, IResultCell cell)
	{
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectRenamed(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectRenamed(IProject Project, String oldName, IResultCell cell)
	{
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectPreClose(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectPreClose(IProject Project, IResultCell cell)
	{
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectClosed(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectClosed(IProject project, IResultCell cell)
	{
		saveCRCs(project);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectPreSave(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectPreSave(IProject Project, IResultCell cell)
	{
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onProjectSaved(org.netbeans.modules.uml.core.metamodel.structure.IProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onProjectSaved(IProject Project, IResultCell cell)
	{
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onPreReferencedLibraryAdded(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreReferencedLibraryAdded(IProject Project, String refLibLoc, IResultCell cell)
	{
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onReferencedLibraryAdded(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onReferencedLibraryAdded(IProject Project, String refLibLoc, IResultCell cell)
	{
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onPreReferencedLibraryRemoved(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onPreReferencedLibraryRemoved(IProject Project, String refLibLoc, IResultCell cell)
	{
		//do nothing
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.core.metamodel.structure.IProjectEventsSink#onReferencedLibraryRemoved(org.netbeans.modules.uml.core.metamodel.structure.IProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
	 */
	public void onReferencedLibraryRemoved(IProject Project, String refLibLoc, IResultCell cell)
	{
		//do nothing
	}
}