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
import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.Node;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductEventDispatcher;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink;
import org.netbeans.modules.uml.core.eventframework.EventBlocker;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventDispatcher;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementChangeEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.PreventElementReEntrance;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.UMLXMLManip;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.metamodel.structure.IStructureEventDispatcher;
import org.netbeans.modules.uml.core.metamodel.structure.Project;
import org.netbeans.modules.uml.core.support.umlsupport.FileExtensions;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.ResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlsupport.URILocator;
import org.netbeans.modules.uml.core.support.umlsupport.Validator;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;
import org.netbeans.modules.uml.core.workspacemanagement.ITwoPhaseCommit;
import org.netbeans.modules.uml.core.workspacemanagement.IWSElement;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventDispatcher;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceManager;
import org.netbeans.modules.uml.core.workspacemanagement.WSProjectEventDispatcher;
import org.netbeans.modules.uml.core.workspacemanagement.WorkspaceManagementException;
import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;

/**
 * @author sumitabhk
 *
 */
public class Application implements IApplication,
        IWSProjectEventsSink, IExternalElementEventsSink,
        ICoreProductInitEventsSink
{
    /**
     * Creates and initializes an Application.
     */
    public Application()
    {
        super();
        initialize();
    }
    
    protected void initialize()
    {
        connectToWorkspace();
        establishDispatchers();
        establishQueryManager();
    }
    
    /**
     * Establish the connection between this Application and the WorkspaceManager
     * and workspace dispatcher on the CoreProduct.
     */
    protected void connectToWorkspace()
    {
        ICoreProduct product = ProductRetriever.retrieveProduct();
        if (product != null)
        {
            IEventDispatcher disp =
                    (IEventDispatcher) EventDispatchRetriever.instance()
                    .getDispatcher(
                    EventDispatchNameKeeper.workspaceName());
            if (disp != null && disp instanceof IWorkspaceEventDispatcher)
            {
                m_EventDispatcher = (IWorkspaceEventDispatcher) disp;
            }
            
            if (m_EventDispatcher != null)
            {
                IWorkspaceManager man = product.getWorkspaceManager();
                try
                {
                    man.setEventDispatcher(m_EventDispatcher);
                    m_EventDispatcher.registerForWSProjectEvents(this);
                }
                catch (InvalidArguments e)
                {
                    e.printStackTrace();
                    return ;
                }
            }
        }
    }
    
    protected void establishDispatchers()
    {
        IElementChangeEventDispatcher eceDispatcher =
                (IElementChangeEventDispatcher) EventDispatchRetriever
                .instance()
                .getDispatcher(
                EventDispatchNameKeeper.modifiedName());
        if (eceDispatcher != null)
            eceDispatcher.registerForExternalElementEventsSink(this);
        ICoreProductEventDispatcher cpeDispatcher =
                (ICoreProductEventDispatcher) EventDispatchRetriever.instance()
                .getDispatcher(EventDispatchNameKeeper.coreProduct());
        if (cpeDispatcher != null)
            cpeDispatcher.registerForInitEvents(this);
    }
    
    /**
     *
     * Creates and initializes the internal QueryManager
     */
    protected void establishQueryManager()
    {
        m_QueryManager = new QueryManager();
        m_QueryManager.initialize();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.IApplication#createProject()
     */
    public IProject createProject()
    {
        return createProject("");
    }
    
    /**
     * Opens a project at the file specified.
     *
     * @param fileName[in] The file to open
     * @return project	   The IProject interface that represents the opened project
     */
    public IProject openProject(String fileName)
    {
        if (fileName == null)
            throw new IllegalArgumentException("null filename");
        
        IProject project = null;
        File projectFile = new File(fileName);
        if (projectFile.exists())
        {
            // See if we've already opened the project
            project = getProjectByFileName(projectFile.toString());
            
            if (project == null)
            {
                String openMessage = getProjectOpenMessage();   // TODO:
                if (openMessage != null && openMessage.length() > 0)
                {
                    StringUtilities.replaceSubString(openMessage, "%1", fileName);
                }
                
                IStructureEventDispatcher dispatcher = getProjectDispatcher();
                if (dispatcher != null)
                {
                    IWorkspace workspace = getCurrentWorkspace();
                    IEventPayload payload =
                            dispatcher.createPayload("ProjectPreOpen");
                    boolean proceed = dispatcher.fireProjectPreOpen(workspace, fileName, payload);
                    
                    if (proceed)
                    {
                        UMLXMLManip manip = new UMLXMLManip();
                        if (manip.verifyDTDExistence(fileName))
                        {
                            Document doc = Validator.verifyXMLFileFormat(fileName, "XMI");
                            project = establishOpenProject(doc, fileName);
                            
                            if (project ==  null)
                                return null;
                            
                            if (m_QueryManager != null)
                            {
                                // Delete the .QueryCache if it exists.  The file will be regenerated
                                // in m_QueryManager.establishCache(project).
                                // This is to eleminate .QueryCache file that created in previous releases
                                // which has references to old package name.
                                String qcFilePath = StringUtilities.getPath(project.getFileName() );
                                qcFilePath += QueryManager.QUERY_CACHE;
                                File qcFile = new File(qcFilePath);
                                if (qcFile.exists() && !qcFile.isDirectory())
                                {
                                    try
                                    {
                                        FileObject qcFO = FileUtil.toFileObject(new File(qcFile.getCanonicalPath()));
                                        if (qcFO != null) 
                                        {
                                            qcFO.delete();
                                        }
                                    }
                                    catch (Exception e)
                                    {
                                        // can not delete file; do nothing
                                    }
                                }
                                // regenerate the cache
                                m_QueryManager.establishCache(project);
                            }
                            payload = dispatcher.createPayload("ProjectOpened");
                            dispatcher.fireProjectOpened(project,payload);
                        }
                        else
                        {
                            //No DTD exists. Throw error.
                        }
                    }
                }
            }
        }
        return project;
    }
    
    /**
     * Creates a project object, connects it to the XML Document given, and adds
     * it to the list of open projects.
     *
     * @param doc The XML Document that represents the data behind the project
     * @param fileName The filename that the given doc was opened with.
     * @return The <code>IProject</code> established.
     */
    protected IProject establishOpenProject(Document doc, String fileName)
    {
        IProject project = null;
        if (m_Projects != null)
        {
            project = getProjectByFileName(fileName);
            Log.out("Establishing open project for file " + fileName);
            if (project == null && doc != null)
            {
                Node node = doc.selectSingleNode("/XMI/XMI.content/UML:Project");
                if (node != null)
                {
                    TypedFactoryRetriever<IProject> fact =
                            new TypedFactoryRetriever<IProject>();
                    project = fact.createTypeAndFill("Project", node);
                    if (project != null)
                    {
                        project.setDocument(doc);
                        project.setFileName(fileName);
                        
                        UMLXMLManip.convertRelativeHrefs(fileName, project);
                        project.loadDefaultImports();
                        attachProject(project);
                    }
                }
            }
        }
        return project;
    }
    
    /**
     * Makes sure the passed in Project is on our internal collection as well
     * as making sure that the Application is properly advised to the Project
     *
     * @param project The <code>IProject</code>.
     */
    protected void attachProject(IProject project)
    {
        FactoryRetriever fact = FactoryRetriever.instance();
        // Make sure the project is on the Object in
        // memory table
        if (fact != null)
        {
            fact.addObject(project);
        }
        if (!m_Projects.contains(project))
        {
            m_Projects.add(project);
        }
    }
    
    /**
     * Retrieves the current workspace off the Describe product.
     *
     * @return The current <code>IWorkspace</code>, possibly <code>null</code>.
     */
    protected IWorkspace getCurrentWorkspace()
    {
        ICoreProduct product = getProduct();
        return product.getCurrentWorkspace();
    }
    
    /**
     * Retrieves the current Describe product.
     *
     * @return The <code>IProduct</code>.
     */
    protected ICoreProduct getProduct()
    {
        return ProductRetriever.retrieveProduct();
    }
    
    /**
     * Retrieves the event dispatcher specific to the project.
     * @return A <code>IStructureEventDispatcher</code>, if obtained.
     */
    private IStructureEventDispatcher getProjectDispatcher()
    {
        EventDispatchRetriever ret = EventDispatchRetriever.instance();
        IStructureEventDispatcher structureEventDispatcher = null;
        Object dispatcher = ret.getDispatcher(EventDispatchNameKeeper.structure());
        if (dispatcher != null && dispatcher instanceof IStructureEventDispatcher)
        {
            structureEventDispatcher = (IStructureEventDispatcher) dispatcher;
        }
        return structureEventDispatcher;
    }
    
    /**
     * @return
     */
    private String getProjectOpenMessage()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     *
     * Closes a top level model used to house all other elements.
     *
     * @param project [in] The project to close
     *
     * @param save [in] True to save the project before closing, else false
     * to discard any changes and just close.
     *
     */
    public void closeProject(IProject project, boolean save)
    {
        PreventElementReEntrance blocker = new PreventElementReEntrance(this);
        try
        {
            if (blocker.isBlocking())
                return;
            
            IStructureEventDispatcher dispatcher = getProjectDispatcher();
            boolean proceed = true;
            if (dispatcher != null)
            {
                IEventPayload payload = dispatcher.createPayload("ProjectPreClose");
                proceed = dispatcher.fireProjectPreClose(project, payload);
            }
            if (proceed)
            {
                if (save)
                {
                    saveProject(project);
                }
                
                // Find this project in our collection and remove it.
                int count = m_Projects.size();
                
                for (int i=0; i<count; i++)
                {
                    IProject proj = m_Projects.get(i);
                    if (proj.isSame(project))
                    {
                        m_Projects.remove(i);
                        if (dispatcher != null)
                        {
                            IEventPayload payload = dispatcher.createPayload("ProjectClosed");
                            dispatcher.fireProjectClosed(project, payload);
                        }
                        break;
                    }
                }
                
                // Be sure to completely Deinitialize the Project by calling Close
                // on it. This should be the very last thing that is done to the Project
                project.close();
            }
        }
        finally
        {
            blocker.releaseBlock();
        }
    }
    
    //	/////////////////////////////////////////////////////////////////////////////
    //
    //	 HRESULT Application::SaveProject( IProject* project )
    //
    //	 Saves the project by retrieving the href attribute on the project's
    //	 node and saving to that location. If the href value does not exist,
    //	 this means the project has never been saved before. An error results.
    //
    //	 INPUT:
    //		project  -  the project we are saving.
    //
    //	 OUTPUT:
    //		None.
    //
    //	 RETURN:
    //		HRESULTs
    //
    //	 CAVEAT:
    //		None.
    //
    //	/////////////////////////////////////////////////////////////////////////////
    public void saveProject( IProject project )
    {
        if (project != null)
        {
            String fileName = project.getFileName();
            if (fileName != null && fileName.length() > 0)
            {
                if (project.getDirty())
                    project.save(fileName, true);
            }
        }
        
        // shouldn't we save all the modified diagrams as well?
        IProductDiagramManager manager = ProductHelper.getProductDiagramManager();
        ETList<IProxyDiagram> list = manager.getOpenDiagrams();
        for (IProxyDiagram diagram: list)
        {
            IDiagram dia = diagram.getDiagram();
            //krichard issue 124470 NPE from dia.getProject().equals(project)
            // added dia.getProject() != null check
            if (dia != null && dia.getProject() != null && dia.getProject().equals(project))
                if (dia.getIsDirty())
                    dia.save();
        }
    }
    
    /**
     *
     * Closes all the open projects.
     *
     * @param save [in] True to save any modified projects, else False to
     * discard any and all changes
     *
     */
    public void closeAllProjects(boolean save)
    {
        if (m_Projects != null)
        {
            // Fire the PreAllProjectClose event
            IResultCell cell = new ResultCell();
            boolean proceed = cell.canContinue();
            if (proceed)
            {
                int count = m_Projects.size();
                while (count > 0)
                {
                    IProject proj = m_Projects.get(0);
                    closeProject(proj, save);
                    count = m_Projects.size();
                }
            }
        }
    }
    
    /**
     *
     * Retrieves the collection object that holds all the projects that this
     * application has.
     *
     * @param pVal [out] The list of projects
     */
    public ETList<IProject> getProjects()
    {
        return getProjects(FileExtensions.MD_EXT_NODOT);
    }
    
    /**
     *
     * Retrieves all open Projects that have a particular file extension. The compare of the file
     * extensions is case insensitive
     *
     * @param fileExtension[in]
     * @param pVal[in]
     */
    public ETList<IProject> getProjects(String fileExtension)
    {
        ETList<IProject> retProjs = new ETArrayList<IProject>();
        try
        {
            if (m_Projects != null && fileExtension != null && fileExtension.length() > 0)
            {
                int size = m_Projects.size();
                for (int i=0;i<size;i++)
                {
                    IProject proj = m_Projects.get(i);
                    if (proj != null)
                    {
                        String fName = proj.getFileName();
                        if (fName != null && fName.length() > 0)
                        {
                            String extn = StringUtilities.getExtension(fName);
                            if (fileExtension.equalsIgnoreCase(extn))
                            {
                                retProjs.add(proj);
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            //donot do anything for now
        }
        return retProjs;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.IApplication#getProjectByName(java.lang.String)
         */
    public IProject getProjectByName(String projectName)
    {
        if (projectName != null && m_Projects != null)
        {
            for (Iterator<IProject> iter = m_Projects.iterator();
            iter.hasNext(); )
            {
                IProject proj = (IProject) iter.next();
                if (projectName.equals(proj.getName()))
                    return proj;
            }
        }
        return null;
    }
    
    /**
     *
     * Retrieves an open project that matches the passed-in name.
     *
     * @param pWorkspace [in] The workspace we're associated with
     * @param projectName [in] The name of the project to retrieve
     * @param project [out] The found project
     *
     * @return HRESULT
     *
     */
    public IProject getProjectByName(IWorkspace pWorkspace, String projectName )
    {
        IProject retProj = null;
        if (pWorkspace == null)
        {
            pWorkspace = getCurrentWorkspace();
        }
        
        if (m_Projects != null && pWorkspace != null)
        {
            int numProjs = m_Projects.size();
            for (int i=0; i<numProjs; i++)
            {
                IProject proj = m_Projects.get(i);
                String name = proj.getName();
                if (projectName.equals(name))
                {
                    // Make sure this project is in the workspace we've been asked about
                    String sFilename = proj.getFileName();
                    if (sFilename != null && sFilename.length() > 0)
                    {
                        // If we have a pattern just return the first project we find.
                        // patterns do not exist in projects
                        if (StringUtilities.hasExtension(sFilename, FileExtensions.PATTERN_EXT) )
                        {
                            retProj = proj;
                            break;
                        }
                        else
                        {
                            try
                            {
                                pWorkspace.verifyUniqueElementLocation(sFilename);
                                retProj = proj;
                                break;
                            }
                            catch (WorkspaceManagementException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        
        return retProj;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.IApplication#getProjectByFileName(java.lang.String)
         */
    public IProject getProjectByFileName(String fileName)
    {
        if (m_Projects != null)
        {
            // Compare File objects instead of Strings. This will do the right
            // thing (case insensitive compare on Windows, case sensitive on
            // Unixen) on all OSes.
            File projFile = new File(fileName);
            for (Iterator<IProject> iter = m_Projects.iterator();
            iter.hasNext(); )
            {
                IProject curr = iter.next();
                String currFile = curr.getFileName();
                if (currFile == null)
                    continue;
                if (projFile.equals(new File(curr.getFileName())))
                    return curr;
            }
        }
        return null;
    }
    
    /**
     *
     * Retrieves a Project from the Application by the ID of the project.
     *
     * @param id[in] The ID of the project to retrieve
     * @param project[out] The found Project, else 0 if not found.
     *
     * @return HRESULTs
     */
    public IProject getProjectByID(String projID)
    {
        IProject retProj = null;
        if (m_Projects != null)
        {
            int count = m_Projects.size();
            for (int i=0; i<count; i++)
            {
                IProject proj = m_Projects.get(i);
                if (proj instanceof IVersionableElement)
                {
                    IVersionableElement ver = proj;
                    Node node = ver.getNode();
                    if (node != null)
                    {
                        String temp = XMLManip.getAttributeValue(node, "xmi.id");
                        if (temp != null && temp.equals(projID))
                        {
                            retProj = proj;
                            break;
                        }
                    }
                }
            }
        }
        if (retProj == null)
        {
            retProj = resolveProjectByID(projID);
        }
        return retProj;
    }
    
    /**
     *
     * Attempts to find a Project by querying the current workspace, looking for an
     * element whose data section matches the id passed in.
     *
     * @param projID[in]          The ID of the IProject to find
     * @param foundProject[out]   The found project if found
     *
     * @return HRESULT
     *
     */
    private IProject resolveProjectByID(String projID)
    {
        IProject retProj = null;
        
        PreventElementReEntrance blocker = new PreventElementReEntrance(this);
        try
        {
            if (blocker.isBlocking())
                return retProj;
            
            // We didn't find any project's that are currently open by the passed in ID. Let's see
            // If a project exists in the current workspace by that ID. If it does, we'll open the project
            // and return it.
            IWorkspace space = getCurrentWorkspace();
            if (space != null)
            {
                ETList<IWSElement> foundElements = space.getElementsByDataValue(projID);
                if (foundElements != null)
                {
                    int count = foundElements.size();
                    if (count > 0)
                    {
                        try
                        {
                            // There should only ever be one, so grab that one
                            IWSElement wsElem = foundElements.get(0);
                            String location = wsElem.getLocation();
                            if (location.length() > 0)
                            {
                                IWSProject wsProj = space.openWSProjectByLocation(location);
                                if (wsProj != null)
                                {
                                    retProj = openProjectFromWSProject(wsProj);
                                    if (retProj != null)
                                    {
                                        String projName = retProj.getName();
                                    }
                                }
                            }
                        }
                        catch (WorkspaceManagementException e)
                        {
                        }
                    }
                }
            }
        }
        finally
        {
            blocker.releaseBlock();
        }
        return retProj;
    }
    
    /**
     *
     * Creates a new Workspace file at the location specified.
     *
     * @param name[in] The absolute path to the resultant workspace file.
     * @param space[out] The new Workspace
     *
     * @return HRESULT
     *
     */
    public IWorkspace createWorkspace(String fileName, String name)
    {
        IWorkspace retSpace = null;
        try
        {
            ICoreProduct prod = getProduct();
            if (prod != null)
            {
                retSpace = prod.createWorkspace(fileName, name);
            }
        }
        catch (InvalidArguments e)
        {
        }
        catch (WorkspaceManagementException e)
        {
        }
        return retSpace;
    }
    
    /**
     *
     * Opens a workspace file, returning the Workspace object that represents
     * the data in that file.
     *
     * @param location[in] The absolute path to the Workspace file.
     * @param space[out] The Workspace object
     *
     * @return HRESULT
     *
     */
    public IWorkspace openWorkspace(String location)
    {
        IWorkspace retSpace = null;
        try
        {
            ICoreProduct prod = getProduct();
            if (prod != null)
            {
                retSpace = prod.openWorkspace(location);
            }
        }
        catch (InvalidArguments e)
        {
        }
        catch (WorkspaceManagementException e)
        {
        }
        return retSpace;
    }
    
    /**
     *
     * Closes the passed-in Workspace. All open Projects will also be closed.
     *
     * @param space[in] The Workspace to close
     * @param save[in] True to save the contents of the Workspace, else
     *                False to discard changes since the last close and save
     *                of the workspace.
     *
     * @return HRESULT
     */
    public void closeWorkspace(IWorkspace space, String fileName, boolean save)
    {
        try
        {
            ICoreProduct prod = getProduct();
            if (prod != null)
            {
                prod.closeWorkspace(space, fileName, save);
            }
        }
        catch (InvalidArguments e)
        {
        }
        catch (WorkspaceManagementException e)
        {
        }
    }
    
    /**
     *
     * Imports an external IProject into the passed-in Workspace.
     *
     * @param space[in] The Workspace to update with the external Project
     * @param project[in] The Project to import
     * @param wsProject[out] The resultant WSProject that the Project was
     *                       imported into.
     *
     * @return HRESULT
     *
     */
    public IWSProject importProject(IWorkspace space, IProject project)
    {
        IWSProject retProj = null;
        
        // Retrieve the name of the project
        String projName = project.getName();
        if (projName.length() > 0)
        {
            boolean proceed = dispatchPreProjectImportEvent(projName, space);
            if (proceed)
            {
                String projLoc = project.getFileName();
                IWSProject newWSProj = null;
                
                // Prevent all events during the CreateWSProject call, as we don't want
                // anyone handling that event.
                try
                {
                    EventBlocker.startBlocking();
                    
                    // Create the WSProject that will encapsulate all the files associated with
                    // the imported project
                    newWSProj = space.createWSProject(projLoc, projName);
                }
                catch (Exception e)
                {}
                finally
                {
                    EventBlocker.stopBlocking(false);
                }
                
                if (newWSProj != null)
                {
                    attachProject(project);
                    retProj = newWSProj;
                    addProjectToWSProject(newWSProj, project);
                    dispatchProjectImported(newWSProj);
                }
            }
        }
        return retProj;
    }
    
    /**
     *
     * Dispatches the WSProjectInserted event
     *
     * @param wsProject[in] The WSProject that was inserted.
     *
     * @return HRESULT
     *
     */
    private void dispatchProjectImported( IWSProject wsProject )
    {
        try
        {
            IWSProjectEventDispatcher dispatcher = prepareWSProjectDispatcher();
            if (dispatcher != null)
            {
                dispatcher.dispatchWSProjectInserted(wsProject);
            }
        }
        catch (InvalidArguments e)
        {
        }
    }
    
    /**
     *
     * Prepares the WSProject event dispatcher
     *
     * @param disp[out] The dispatching object.
     *
     * @return HRESULT
     *
     */
    private IWSProjectEventDispatcher prepareWSProjectDispatcher()
    {
        IWSProjectEventDispatcher retDisp = new WSProjectEventDispatcher();
        retDisp.setEventDispatcher(m_EventDispatcher);
        return retDisp;
    }
    
    /**
     *
     * Dispatches the pre insert event.
     *
     * @param projName[in] The name of the WSProject being inserted
     * @param space[in] The Workspace being inserted into.
     *
     * @return HRESULT
     *
     */
    private boolean dispatchPreProjectImportEvent(String projName, IWorkspace space)
    {
        boolean proceed = true;
        try
        {
            if (space != null)
            {
                IWSProjectEventDispatcher dispatcher = prepareWSProjectDispatcher();
                proceed = dispatcher.dispatchWSProjectPreInsert(space, projName);
            }
        }
        catch (InvalidArguments e)
        {
        }
        
        return proceed;
    }
    
    /**
     *
     * Opens a Project by retrieving it from within the passed-in Workspace.
     *
     * @param space[in] The Workspace to pull the Project from
     * @param projName[in] Name of the Project to open
     * @param project[out] The found Project
     *
     * @return HRESULT
     *
     */
    public IProject openProject(IWorkspace space, String projName)
    {
        IProject retProj = getProjectByName(space, projName);
        try
        {
            if (retProj == null)
            {
                IWSProject wsProj = space.openWSProjectByName(projName);
                if (wsProj != null)
                {
                    retProj = openProjectFromWSProject(wsProj);
                }
            }
        }
        catch (WorkspaceManagementException e)
        {
        }
        return retProj;
    }
    
    /**
     *
     * Opens a Project by retrieving it from within the passed-in Workspace.
     *
     * @param space[in] The Workspace to pull the Project from
     * @param WSProject[in] The project we should open
     * @param project[out] The found Project
     *
     * @return HRESULT
     *
     */
    public IProject openProject(IWorkspace space, IWSProject workspaceProject)
    {
        IProject retProj = null;
        String projName = workspaceProject.getName();
        retProj = openProject(space, projName);
        return retProj;
    }
    
    /**
     *
     * Cleans up this Application
     */
    public void destroy()
    {
        if (!m_Destroyed)
        {
            m_Destroyed = true;
            try
            {
                if (m_QueryManager != null)
                {
                    m_QueryManager.deinitialize();
                    m_QueryManager = null;
                }
                closeAllProjects(false);
                if( m_EventDispatcher != null)
                {
                    m_EventDispatcher.revokeWSProjectSink(this);
                    revokeDispatchers();
                }
            }
            catch(Exception e)
            {
            }
        }
    }
    
    /**
     *
     * Revokes the dispatchers the Application is listening to.
     */
    protected void revokeDispatchers()
    {
        IElementChangeEventDispatcher eceDispatcher =
                (IElementChangeEventDispatcher) EventDispatchRetriever
                .instance()
                .getDispatcher(
                EventDispatchNameKeeper.modifiedName());
        if (eceDispatcher != null)
            eceDispatcher.revokeExternalElementEventsSink(this);
        ICoreProductEventDispatcher cpeDispatcher =
                (ICoreProductEventDispatcher) EventDispatchRetriever.instance()
                .getDispatcher(EventDispatchNameKeeper.coreProduct());
        if (cpeDispatcher != null)
            cpeDispatcher.revokeInitSink(this);
    }
    
    /**
     *
     * Returns the filename of the executing module
     *
     * @param sLocation[out] The Application's file name.
     */
    public String getInstallLocation()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     *
     * Returns the number of closed projects
     *
     * @param nNumClosed The number of closed projects
     *
     * @return
     *
     */
    public int getNumClosedProjects()
    {
        int numProjs = 0;
        try
        {
            IWorkspace space = getCurrentWorkspace();
            if (space != null)
            {
                ETList<IWSProject> wsProjs = space.getWSProjects();
                if (wsProjs != null)
                {
                    int count = wsProjs.size();
                    for (int i=0; i<count; i++)
                    {
                        IWSProject wsProj = wsProjs.get(i);
                        String projName = wsProj.getName();
                        boolean isOpen = wsProj.isOpen();
                        if (projName.length() > 0)
                        {
                            String fileName = wsProj.getLocation();
                            if (fileName.length() > 0)
                            {
                                if (!isOpen && StringUtilities.hasExtension(fileName, FileExtensions.MD_EXT))
                                {
                                    numProjs++;
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (WorkspaceManagementException e)
        {
        }
        return numProjs;
    }
    
    /**
     *
     * Are all the WSProjects owned by this element opened
     *
     * @param bAllOpened true if all the WSProjects are opened
     *
     * @return
     *
     */
    public int getNumOpenedProjects()
    {
        int numProjs = 0;
        
        // Get the number of open projects in a what that doesn't cause the project
        // to up and down the refcount.  If we don't do this then detecting leaks can
        // be a pain because this routine is called in the onidle of ADMFCGui.
        if (m_Projects != null)
        {
            int count = m_Projects.size();
            for (int i=0; i<count; i++)
            {
                // Look for ETD extensions
                IProject proj = m_Projects.get(i);
                String fileName = proj.getFileName();
                if (fileName.length() > 0)
                {
                    String ext = StringUtilities.getExtension(fileName);
                    if (ext != null && ext.equalsIgnoreCase(FileExtensions.MD_EXT_NODOT))
                    {
                        numProjs++;
                    }
                }
            }
        }
        
        return numProjs;
    }
    
    /**
     *
     * Retrieves the QueryManager object associated with this Application
     *
     * @return The QueryManager
     */
    public IQueryManager getQueryManager()
    {
        return m_QueryManager;
    }
    
    /**
     *
     * Sets a new QueryManager object on this application
     *
     * @param The QueryManager
     */
    public void setQueryManager(IQueryManager value)
    {
        m_QueryManager = value;
    }
    
    /////////////////////////////////////////////////////////////////////////////
    // Event handler functions from IWSProjectEventsSink.
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreCreate(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onWSProjectPreCreate(IWorkspace space, String projectName, IResultCell cell)
    {
        //nothing to do
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectCreated(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onWSProjectCreated(IWSProject wsproject, IResultCell cell)
    {
        if (wsproject != null)
        {
            String projectName = wsproject.getName();
            String fileName = deriveProjectFileName(wsproject, projectName);
            
            IProject project = createProject(fileName);
            
            if (project != null)
            {
                project.setName(projectName);
                try
                {
                    addProjectToWSProject(wsproject, project);
                }
                catch (WorkspaceManagementException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    
    protected IProject openProjectFromWSProject(IWSProject wsproject)
            throws WorkspaceManagementException
    {
        if (wsproject == null)
            throw new IllegalArgumentException("null wsproject");
        IWSElement element = wsproject.getElementByName("_MetaData__");
        if (element != null)
        {
            IProject proj = openProject(element.getLocation());
            if (proj != null)
                establishTwoPhaseConnection(element, proj);
            return proj;
        }
        else
        {
            // TODO:
            //            INFO_MESSAGE_ID( IDS_MESSAGINGFACILITY, IDS_NO_PROJECTS );
        }
        return null;
    }
    
    /**
     *
     * Retrieves the IProject from the IWSProject
     *
     * @param wsProject[in] The IWSProject to pull from
     * @param project[out] The IProject
     *
     * @return HRESULT
     *
     */
    protected IProject retrieveProjectFromWSProject( IWSProject wsProject)
    {
        IProject retProj = null;
        if (wsProject != null)
        {
            IWSElement elem = wsProject.getElementByName("_MetaData__");
            if (elem != null)
            {
                ITwoPhaseCommit commit = elem.getTwoPhaseCommit();
                if (commit instanceof IProject)
                {
                    retProj = (IProject)commit;
                }
            }
        }
        return retProj;
    }
    
    /**
     * Adds the given IProject to the IWSProject.
     * @param wsproj
     * @param proj
     */
    protected void addProjectToWSProject(IWSProject wsproj, IProject proj)
            throws WorkspaceManagementException
    {
        String filename = proj.getFileName();
        if (filename == null || filename.length() == 0)
            proj.setFileName(
                    filename = deriveProjectFileName(wsproj, proj.getName()));
        IWSElement element = wsproj.addElement(filename, "_MetaData__",
                proj.getXMIID());
        establishTwoPhaseConnection(element, proj);
    }
    
    protected void establishTwoPhaseConnection(IWSElement element,
            IProject project)
    {
        // Connect the IProject to the IWSElement via the ITwoPhaseCommit
        // interface so that the proper saving process can commence upon save
        element.setTwoPhaseCommit((ITwoPhaseCommit) project);
    }
    
    protected IProject createProject(String fileName)
    {
        IStructureEventDispatcher dispatcher = getProjectDispatcher();
        
        boolean proceed = true;
        if (dispatcher != null)
            proceed = fireProjectPreCreate(dispatcher);
        if (proceed)
        {
            if (m_Projects == null)
                m_Projects = new ArrayList<IProject>();
            
            try
            {
                // Create the DOM Document that will hold the new project's
                // structure.
                XMLManip manip = XMLManip.instance();
                Document doc = manip.getDOMDocument();
                
                IProject project = new Project();
                project.setDocument(doc);
                
                project.prepareNode();
                project.setFileName(fileName);
                attachProject(project);
                
                if (m_QueryManager != null)
                    m_QueryManager.establishCache(project);
                
                if (dispatcher != null)
                {
                    dispatcher.fireProjectCreated(project,
                            dispatcher.createPayload("ProjectCreated"));
                }
                
                return project;
            }
            catch (Throwable t)
            {
                t.printStackTrace();
            }
        }
        else
        {
            // TODO:
            //            INFO_MESSAGE_ID( IDS_MESSAGINGFACILITY, IDS_PROJECT_CREATE_CANCELLED );
        }
        return null;
    }
    
    /**
     * Fires the pre-create event to all interested listeners.
     *
     * @param dispatcher The event dispatcher to use.
     * @return <code>true</code> if all listeners agree that project creation
     *         can proceed.
     */
    protected boolean fireProjectPreCreate(IStructureEventDispatcher dispatcher)
    {
        boolean bProceed = true;
        IWorkspace workspace = getCurrentWorkspace();
        if (workspace != null)
        {
            IEventPayload payload =
                    dispatcher.createPayload("ProjectPreCreate");
            bProceed = dispatcher.fireProjectPreCreate(workspace, payload);
        }
        return bProceed;
    }
    
    /**
     * Returns the project file name, given the IWSProject and the name of the
     * project. The project file will be placed relative to the IWSProject's
     * base directory, and the filename will be the supplied 'projectName' with
     * the project file extension.
     *
     * @param project     The IWSProject which supplies a base directory.
     * @param projectName The name of the project file. If <code>null</code>,
     *                    "_MetaData__" is assumed.
     * @return The filename for the project file.
     */
    protected String deriveProjectFileName(IWSProject project,
            String projectName)
    {
        if (project == null)
            throw new IllegalArgumentException("null project");
        
        if (projectName == null || projectName.length() == 0)
            projectName = "_MetaData__";
        
        projectName += MD_EXT;
        
        String basedir = project.getBaseDirectory();
        if (basedir == null)
            basedir = getCurrentWorkspace().getBaseDirectory();
        return new File(basedir, projectName).toString();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectPreOpen(org.netbeans.modules.uml.core.workspacemanagement.IWorkspace, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onWSProjectPreOpen(IWorkspace space, String projName, IResultCell cell)
    {
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectOpened(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onWSProjectOpened(IWSProject wsproject, IResultCell cell)
    {
        try
        {
            openProjectFromWSProject(wsproject);
        }
        catch (WorkspaceManagementException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     *
     * Fired right before a WSProject is removed from the Workspace.
     *
     * @param project [in]
     * @param cell [in] IResultCell
     *
     * @return S_OK
     *
     */
    public void onWSProjectPreRemove(IWSProject project, IResultCell cell)
    {
        
    }
    
    /**
     *
     * FIred after a WSProject is removed from the Workspace.
     *
     * @param project [in]
     * @param cell [in] IResultCell
     *
     * @return S_OK
     *
     */
    public void onWSProjectRemoved(IWSProject project, IResultCell cell)
    {
        boolean isOpen = project.isOpen();
        if (isOpen)
        {
            IProject proj = retrieveProjectFromWSProject(project);
            if (proj != null)
            {
                closeProject(proj, false);
            }
        }
    }
    
    /**
     *
     * Fired right before a WSProject is inserted into a Workspace.
     *
     * @param project [in]
     * @param projectName [in]
     * @param cell [in] IResultCell
     *
     * @return S_OK
     *
     */
    public void onWSProjectPreInsert(IWorkspace space, String projectName, IResultCell cell)
    {
        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectInserted(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onWSProjectInserted(IWSProject project, IResultCell cell)
    {
        
    }
    
    /**
     *
     * Fired right before a WSProject is renamed.
     *
     * @param project [in]
     * @param newName [in]
     * @param cell [in] IResultCell
     *
     * @return S_OK
     *
     */
    public void onWSProjectPreRename(IWSProject project, String newName, IResultCell cell)
    {
        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.workspacemanagement.IWSProjectEventsSink#onWSProjectRenamed(org.netbeans.modules.uml.core.workspacemanagement.IWSProject, java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onWSProjectRenamed(IWSProject project, String oldName, IResultCell cell)
    {
        
    }
    
    /**
     *
     * Fired right before a WSProject is closed.
     *
     * @param project [in]
     * @param cell [in] IResultCell
     *
     * @return S_OK
     *
     */
    public void onWSProjectPreClose(IWSProject project, IResultCell cell)
    {
        
    }
    
    /**
     *
     * Fired as a result of the user or the programmatic closing of an existing WSProject.
     *
     * @param wsProject[in] The WSProject just closed
     * @param cell[in] Ignored
     *
     * @return HRESULT
     *
     */
    public void onWSProjectClosed(IWSProject project, IResultCell cell)
    {
        IProject proj = retrieveProjectFromWSProject(project);
        if (proj != null)
        {
            closeProject(proj, false);
        }
    }
    
    /**
     *
     * Fired right before a WSProject is saved.
     *
     * @param Project [in]
     * @param cell [in] IResultCell
     *
     * @return HRESULT
     *
     */
    public void onWSProjectPreSave(IWSProject project, IResultCell cell)
    {
        
    }
    
    /**
     *
     * Fired as a result of the user or the programmatic saving of an existing WSProject.
     *
     * @param wsProject[in] The WSProject just saved
     * @param cell[in] Ignored
     *
     * @return HRESULT
     *
     */
    public void onWSProjectSaved(IWSProject project, IResultCell cell)
    {
        IProject proj = retrieveProjectFromWSProject(project);
        if (proj != null)
        {
            saveProject(proj);
        }
    }
    
    //////////////////////////////////////////////////////////////////////////
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink#onExternalElementPreLoaded(java.lang.String, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onExternalElementPreLoaded(String uri, IResultCell cell)
    {
        try
        {
            if (uri != null && uri.length() > 0)
            {
                URILocator.uriparts(uri);
            }
        }
        catch(Exception e)
        {
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink#onExternalElementLoaded(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onExternalElementLoaded(IVersionableElement element, IResultCell cell)
    {
        //nothing to do
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink#onPreInitialExtraction(java.lang.String, org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onPreInitialExtraction(String fileName, IVersionableElement element, IResultCell cell)
    {
        //nothing to do
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.core.foundation.IExternalElementEventsSink#onInitialExtraction(org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onInitialExtraction(IVersionableElement element, IResultCell cell)
    {
        //nothing to do
    }
    
    /////////////////////////////////////////////////////////////////////
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreInit(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onCoreProductPreInit(ICoreProduct pVal, IResultCell cell)
    {
        //nothing to do
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductInitialized(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onCoreProductInitialized(ICoreProduct newVal, IResultCell cell)
    {
        //nothing to do
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreQuit(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onCoreProductPreQuit(ICoreProduct pVal, IResultCell cell)
    {
        //nothing to do
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductPreSaved(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onCoreProductPreSaved(ICoreProduct pVal, IResultCell cell)
    {
        //nothing to do
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.coreapplication.ICoreProductInitEventsSink#onCoreProductSaved(org.netbeans.modules.uml.core.coreapplication.ICoreProduct, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
     */
    public void onCoreProductSaved(ICoreProduct newVal, IResultCell cell)
    {
        //nothing to do
    }
    
    public String getApplicationVersion()
    {
        //To do implement
        //for the time being we want to keep it same as C++ released version.
        return "6.1.4.837";
    }
    
    private static final String MD_EXT = ".etd";
    private ArrayList<IProject> m_Projects = new ArrayList<IProject>();
    private IQueryManager       m_QueryManager;
    private IWorkspaceEventDispatcher m_EventDispatcher;
    private boolean m_Destroyed = false;
    public static final String MESSAGING_FACILITY = "UML";
}

