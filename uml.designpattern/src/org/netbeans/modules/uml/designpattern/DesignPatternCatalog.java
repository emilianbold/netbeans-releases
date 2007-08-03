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


package org.netbeans.modules.uml.designpattern;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.tree.DefaultMutableTreeNode;

import org.netbeans.modules.uml.core.IApplication;
//import org.netbeans.modules.uml.core.addinframework.IAddIn;
//import org.netbeans.modules.uml.core.addinframework.plugins.IExtension;
//import org.netbeans.modules.uml.core.addinframework.plugins.IExtensionPoint;
import org.netbeans.modules.uml.core.coreapplication.CoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.IDesignCenterSupport;
import org.netbeans.modules.uml.core.eventframework.EventBlocker;
import org.netbeans.modules.uml.core.eventframework.EventDispatchRetriever;
import org.netbeans.modules.uml.core.eventframework.IEventDispatchController;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.DiagramDetails;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.FileSysManip;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.ITwoPhaseCommit;
import org.netbeans.modules.uml.core.workspacemanagement.IWSElement;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceEventDispatcher;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspaceManager;
import org.netbeans.modules.uml.core.workspacemanagement.WorkspaceEventDispatcher;
import org.netbeans.modules.uml.core.workspacemanagement.WorkspaceManager;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeDragVerify;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEditVerify;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel;
import org.netbeans.modules.uml.ui.products.ad.application.ApplicationView;
import org.netbeans.modules.uml.ui.products.ad.application.IMenuManager;
//import org.netbeans.modules.uml.ui.products.ad.application.action.AddinActionSetDelegate;
import org.netbeans.modules.uml.ui.products.ad.application.action.ContextMenuActionClass;
//import org.netbeans.modules.uml.ui.products.ad.application.action.IContributionItem;
import org.netbeans.modules.uml.ui.products.ad.application.action.IETContextMenuHandler;
//import org.netbeans.modules.uml.ui.products.ad.application.action.IViewActionDelegate;
//import org.netbeans.modules.uml.ui.products.ad.application.action.PluginAction;
//import org.netbeans.modules.uml.ui.products.ad.application.selection.ISelection;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.QuestionResponse;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogKind;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.netbeans.modules.uml.ui.support.commonresources.ICommonResourceManager;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenu;
import org.netbeans.modules.uml.ui.support.contextmenusupport.IProductContextMenuItem;
import org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeElement;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.wizard.IWizardSheet;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingQuestionDialogImpl;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext;
import org.netbeans.modules.uml.ui.swing.projecttree.JProjectTree;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

//public class DesignPatternCatalog implements IDesignPatternCatalog,
//												IAddIn,
//												IDesignPatternSupport,
//												IDesignCenterSupportGUI,
//												IDesignCenterSupport,
//												IETContextMenuHandler,
//												IViewActionDelegate
public class DesignPatternCatalog implements IDesignPatternCatalog, IDesignPatternSupport,
    IDesignCenterSupportGUI, IDesignCenterSupport, IETContextMenuHandler
{
    private static String 					m_Version = "1";
    private static CatalogEventSink 		m_EventsSink = null;
    private static ApplicationView m_View = null;
    
    private static IProjectTreeControl 	m_DesignCenterTree = null;
    private static IProjectTreeControl 	m_ProjectTree = null;
    
    private static IDesignPatternManager 	m_PatternManager = null;
    
    private static IWorkspace 				m_Workspace = null;
    private static IWorkspaceEventDispatcher m_WorkspaceDispatcher = null;
    private static IWorkspaceManager 		m_WorkspaceManager = null;
    
    protected static Object m_Context = null;
    
    private static boolean m_Initialized = false;
    
    /**
     *
     */
    public DesignPatternCatalog()
    {
        super();
        if (!m_Initialized)
        {
            initialize(null);
            m_Initialized = true;
        }
    }
    
    public static IDesignPatternManager getPatternManager()
    {
        if (m_PatternManager == null)
        {
            m_PatternManager = new DesignPatternManager();
        }
        
        return m_PatternManager;
    }
    
    /////////////////////////////////////////////////////////
    // IAddIn Methods
    
    /**
     * Called when the addin is initialized.
     */
    public long initialize(Object context)
    {
        //
        // The design pattern catalog is an addin manager, so it is going to
        // look for and manage any addins that are in the registry under DesignPatterns
        //
        // create the pattern manager
        getPatternManager();
        //		 if (m_PatternManager == null)
        //		 {
        //		 	m_PatternManager = new DesignPatternManager();
        //		 }
        
        // now this addin is going to be a workspace manager
        if (m_WorkspaceManager == null)
        {
            m_WorkspaceManager = new WorkspaceManager();
            if (m_WorkspaceManager != null)
            {
                // we originally designed the application for only workspace manager
                // so all of the event mechanisms are for that one, if we create a new
                // workspace manager, we want to rename the event dispatcher that it knows
                // about so that we will get the workspace events
                ICoreProduct pProduct = CoreProductManager.instance().getCoreProduct();
                if (pProduct != null)
                {
                    EventDispatchRetriever ret = EventDispatchRetriever.instance();
                    IWorkspaceEventDispatcher disp = ret.getDispatcher("WorkspaceDispatcherDP");
                    if( disp != null )
                    {
                        m_WorkspaceDispatcher = disp;
                    }
                    else
                    {
                        m_WorkspaceDispatcher = new WorkspaceEventDispatcher();
                        
                        IEventDispatchController pEventController = pProduct.getEventDispatchController();
                        if (pEventController != null)
                        {
                            pEventController.addDispatcher("WorkspaceDispatcherDP", m_WorkspaceDispatcher);
                        }
                    }
                    // store this event dispatcher on the workspace manager
                    m_WorkspaceManager.setEventDispatcher(m_WorkspaceDispatcher);
                    
                    // Create the events sink
                    if ( m_EventsSink == null)
                    {
                        m_EventsSink = new CatalogEventSink();
                        m_EventsSink.setParent(this);
                        
                        DispatchHelper dispatcherHelper = new DispatchHelper();
                        dispatcherHelper.registerProjectTreeEvents(m_EventsSink);
                        dispatcherHelper.registerDrawingAreaContextMenuEvents(m_EventsSink);
                        dispatcherHelper.registerDrawingAreaEvents(m_EventsSink);
                        dispatcherHelper.registerForInitEvents(m_EventsSink);
                        dispatcherHelper.registerForProjectEvents(m_EventsSink);
                        dispatcherHelper.registerForWorkspaceEventsDP(m_EventsSink);
                        dispatcherHelper.registerForWSProjectEventsDP(m_EventsSink);
                    }
                    //
                    // now set up the workspace manager to know about the workspace that we ship
                    //
                    IConfigManager configMgr = pProduct.getConfigManager();
                    if (configMgr != null)
                    {
                        String configLoc = configMgr.getDefaultConfigLocation();
                        String file = configLoc + "DesignCenter" + File.separator + "DesignPatterns" + File.separator + "DesignPatternCatalog.etw";
                        File theFile = new File(file);
                        if (theFile.exists())
                        {
                            m_Workspace = m_WorkspaceManager.openWorkspace(file);
                        }
                        else
                        {
                            m_Workspace = m_WorkspaceManager.createWorkspace(file, "DesignPatternCatalog");
                        }
                    }
                }
            }
        }
        return 0;
    }
    public void init(ApplicationView view)
    {
        m_View = view;
    }
    
    /**
     * Called when the addin is deinitialized.
     */
    public long deInitialize(Object context)
    {
        DispatchHelper dispatcherHelper = new DispatchHelper();
        dispatcherHelper.revokeProjectTreeSink(m_EventsSink);
        dispatcherHelper.revokeDrawingAreaContextMenuSink(m_EventsSink);
        dispatcherHelper.revokeDrawingAreaSink(m_EventsSink);
        dispatcherHelper.revokeInitSink(m_EventsSink);
        dispatcherHelper.revokeProjectSink(m_EventsSink);
        dispatcherHelper.revokeWorkspaceSinkDP(m_EventsSink);
        dispatcherHelper.revokeWSProjectSinkDP(m_EventsSink);
        
        // Delete our sink
        if (m_EventsSink != null)
        {
            m_EventsSink = null;
        }
        // Delete the pattern manager
        if (m_PatternManager != null)
        {
            m_PatternManager = null;
        }
        return 0;
    }
    /**
     * Called when the addin is unloaded.
     */
    public long unLoad(Object context)
    {
        return 0;
    }
    /**
     * The version of the addin.
     *
     * @param pVersion [out,retval] The version of this addin.
     */
    public String getVersion()
    {
        return m_Version;
    }
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.addinframework.IAddIn#getLocation()
         */
    public String getLocation()
    {
        return null;
    }
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.addinframework.IAddIn#getID()
         */
    public String getID()
    {
        return getProgID();
    }
    /**
     * Returns the progid of this addin.
     *
     * @param sProgID [out,retval] The progid of this adding (ie "DiagramCreatorAddIn.DiagCreatorAddIn");
     */
    public String getProgID()
    {
        return "org.netbeans.modules.uml.ui.products.ad.addesigncentergui.DesignPatternCatalog";
    }
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.core.addinframework.IAddIn#getName()
         */
    public String getName()
    {
        return NbBundle.getMessage(DesignPatternCatalog.class, "DESIGN_CATALOG_NAME");
    }
    /**
     * Get the workspace that this addin knows about
     *
     * @param[out]	pWork		The workspace that this addin is responsible for
     *
     * @return HRESULT
     */
    public IWorkspace getWorkspace()
    {
        return m_Workspace;
    }
    /**
     * The project tree used
     */
    public IProjectTreeControl getProjectTree()
    {
        return m_DesignCenterTree;
    }
    /**
     * The project tree used
     */
    public void setProjectTree(IProjectTreeControl newVal)
    {
        m_DesignCenterTree = newVal;
    }
    /**
     * Message from the project tree that a context menu is about to be displayed
     */
    public ETList < String > onProjectTreeContextMenuPrepare(IProductContextMenu pMenu)
    {
        ETList <String> pMenuTitles = new ETArrayList<String>();
        // Not needed for jUML - use plugin files instead
          /*
                if (pMenu != null)
                {
                        Object pDisp = pMenu.getParentControl();
                        if (pDisp != null)
                        {
                                // do we come from a tree control
                                if (pDisp instanceof IProjectTreeControl)
                                {
                                        IProjectTreeControl pControl = (IProjectTreeControl)pDisp;
                                        setProjectTree(pControl);
                                        // set up the scope that the pattern participant instances should come from
                                        // the project tree because we are right clicking in the tree to start the
                                        // process
                                        m_PatternManager.setParticipantScope(DesignPatternParticipantScopeEnum.DPPARTICIPANT_SCOPE_PROJECTTREE);
                                        m_PatternManager.setProjectTree(pControl);
                                        // which tree is being right clicked on
                                        // this addin is only responding to events fired by the project tree
                                        // because the catalog is handling the events fired by the design center tree
                                        // we check this because we don't want to do unnecessary processing
                                        if (m_EventsSink instanceof IProductContextMenuSelectionHandler)
                                        {
                                                IProductContextMenuSelectionHandler pSelectionHandler = (IProductContextMenuSelectionHandler)m_EventsSink;
                                // Since we changed the gui to be a wizard, we want the user to always be
                                // able to apply a pattern, no restrictions on what is selected
                                                DesignPatternUtilities.addApplyMenuItem(pMenu, pSelectionHandler);
                                        }
                                        String mgrName = pControl.getConfigMgrName();
                                        if (mgrName.equals("ProjectTree"))
                                        {
                                                //	check to see that a pattern is the only thing selected
                                                boolean bSel = DesignPatternUtilities.onlyCollaborationSelected(pMenu);
                                                if (bSel)
                                                {
                                                        // If we are in the project tree (not the design center tree)
                                                        // and the only thing that is selected is a pattern, then display the apply...
                                                        // we want the users to be able to promote their patterns to the design center
                                                        addPromoteMenuItem(pMenu);
                                                }
                                        }
                                        else
                                        {
                                                pMenuTitles = addDefaultMenuItems();
                                                // not done in c++ as well. Will want to move the CreateDiagramTest in here
                                        }
                                }
                        }
                }
           */
        return pMenuTitles;
    }
    /**
     * Message from the project tree that a context menu is about to be displayed
     */
    public void onProjectTreeContextMenuPrepared(IProductContextMenu pMenu )
    {
    }
    /**
     * Get the project that this addin knows about
     *
     * @param[in]	sFile			The file representing the project
     * @param[out]	pProject		The project that this addin is responsible for
     *
     * @return HRESULT
     */
    private IProject getProject(String sName)
    {
        IProject pProject = null;
        IApplication pApp = ProductHelper.getApplication();
        if (pApp != null)
        {
            // this returns an open project
            Object pDisp = getWorkspace();
            if (pDisp instanceof IWorkspace)
            {
                IWorkspace pWork = (IWorkspace)pDisp;
                IWSProject pWSProj = pWork.openWSProjectByName(sName);
                if (pWSProj != null)
                {
                    // if we didn't get one, we need to open it
                    pProject = pApp.openProject(pWork, pWSProj);
                }
            }
        }
        return pProject;
    }
    /**
     * Populate the passed in Dispatch with child nodes if necessary
     *
     * @param[in]	pDispatch		The parent in which to retrieve any children
     *
     * @return HRESULT
     */
    public void populateTreeItem(Object pDispatch)
    {
        boolean hr = true;
        if (pDispatch != null)
        {
            // Is what is passed in a project tree item
            if (pDispatch instanceof IProjectTreeItem)
            {
                IProjectTreeItem pTreeItem = (IProjectTreeItem)pDispatch;
                // get the model element off of the project tree item
                IElement pElement = pTreeItem.getModelElement();
                if (pElement != null)
                {
                    // if there is a model element, we are not going to do any further
                    // processing, because we are going to let the project tree handle
                    // the building of the child nodes
                }
                else
                {
                    // there is no element, so now check to see if what is stored in the description
                    // is a valid progID
                    // if it is and can be converted to an object, then we know we are on one of the
                    // nodes put in the tree by this addin (probably its top level node)
                    hr = true;
                    if (pTreeItem.isAddinNode())
                    {
                        // at the top level UserDefinedPatterns node
                        // so get the workspace that we know about
                        Object pDisp = getWorkspace();
                        if (pDisp instanceof IWorkspace)
                        {
                            IWorkspace pWork = (IWorkspace)pDisp;
                            // get the projects in the workspace
                            ETList <IWSProject> pProjects = pWork.getWSProjects();
                            if (pProjects != null)
                            {
                                // add them to our project tree as workspace projects so that the
                                // project tree knows how to handle it from here
                                int count = pProjects.getCount();
                                for (int x = 0; x < count; x++)
                                {
                                    IWSProject pProj = pProjects.get(x);
                                    if (pProj != null)
                                    {
                                        String name = pProj.getName();
                                        String loc = pProj.getLocation();
                                        //m_DesignCenterTree.get
                                        //ITreeElement pAdded = null;//m_DesignCenterTree.addProject(name, pTreeItem.getProjectTreeSupportTreeItem(), null);
                                        IProjectTreeItem pAdded = m_DesignCenterTree.addItem(pTreeItem,
                                            name,
                                            name,
                                            1,    // Sort Priority
                                            null, // IElement*
                                            "Workspace Project" // Description
                                            );
                                        if (pAdded != null)
                                        {
                                            //m_DesignCenterTree.setSecondaryDescription(pAdded, loc);
                                            boolean isOpen = pProj.isOpen();
                                            if (isOpen)
                                            {
                                                // get the project that matches the file location
                                                IProject pProject = getProject(name);
                                                if (pProject != null)
                                                {
                                                    // it does know about the project, so the user was probably expanding
                                                    // the project node, maybe causing it to be opened, so the only thing
                                                    // that we need to do is set the icon to an open project
                                                    ICommonResourceManager resourceMgr = CommonResourceManager.instance();
                                                    if (resourceMgr != null)
                                                    {
                                                        //m_DesignCenterTree.setModelElement(pAdded, pProject);
                                                        //pAdded.setModelElement(pProject);
                                                        int nIcon = 0;
                                                        String sLibrary = resourceMgr.getIconDetailsForElementType("Project");
                                                        if (nIcon > 0 && sLibrary.length() > 0 )
                                                        {
                                                            //m_DesignCenterTree.setImage(pAdded, sLibrary, nIcon);
                                                        }
                                                    }
                                                    // trick the project tree into expanding the node
                                                    //ATLASSERT(m_DesignCenterTree);
                                                    //hr = m_DesignCenterTree.OpenProject(pProject);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        // not a valid progID, so this may be an unopened project node
                        // (open project nodes are handled by the project tree and have model elements
                        //  so we wouldn't be in this code)
                        // the unopened project has its project file in its secondary description
                        String text = pTreeItem.getItemText();
                        // get the project that matches the file location
                        IProject pProject = getProject(text);
                        if (pProject != null)
                        {
                            // it does know about the project, so the user was probably expanding
                            // the project node, maybe causing it to be opened, so the only thing
                            // that we need to do is set the icon to an open project
                            ICommonResourceManager resourceMgr = CommonResourceManager.instance();
                            if (resourceMgr != null)
                            {
                                m_DesignCenterTree.setModelElement(pTreeItem, pProject);
                                pTreeItem.setModelElement(pProject);
                                //								int nIcon = 0;
                                //								String sLibrary = "";// TODO resourceMgr.GetIconDetailsForElementType(String("Project"),&sLibrary, &nIcon));
                                //								if (nIcon > 0 && sLibrary.length() > 0 )
                                //								{
                                //									m_DesignCenterTree.setImage(pTreeItem, sLibrary, nIcon);
                                //								}
                            }
                            // trick the project tree into expanding the node
                            m_DesignCenterTree.openProject(pProject);
                        }
                    }
                }
            }
        }
    }
    /**
     * Handles the on before edit coming from the project tree.
     */
    public void onBeforeEdit(IProjectTreeControl pParentControl, IProjectTreeItem pItem, IProjectTreeEditVerify pVerify)
    {
        if (pParentControl != null && pItem != null && pVerify != null)
        {
            // Do not allow editing of the top node - Design Pattern Catalog
            String sName = pItem.getItemText();
            String topStr = DesignPatternUtilities.translateString("IDS_DESIGNPATTERNCATALOG");
            if (sName.equals(topStr))
            {
                pVerify.setCancel(true);
                pVerify.setHandled(true);
            }
        }
    }
    /**
     * Handles the on begin drag coming from the project tree.
     */
    public void onBeginDrag(IProjectTreeControl pParentControl, IProjectTreeItem[] pItem, IProjectTreeDragVerify pVerify)
    {
        if (pParentControl != null && pItem != null && pVerify != null)
        {
            m_ProjectTree = pParentControl;
            // Do not allow dragging of the top node - Design Pattern Catalog
            // Since the user could be dragging many things, we will check them all to
            // see if the top node is among them.  If it is, bail on the whole drag.
            int count = pItem.length;
            boolean allow = true;
            for (int x = 0; x < count; x++)
            {
                IProjectTreeItem pTreeItem = pItem[x];
                if (pTreeItem != null)
                {
                    String sName = pTreeItem.getItemText();
                    String topStr = DesignPatternUtilities.translateString("IDS_DESIGNPATTERNCATALOG");
                    if (sName.equals(topStr))
                    {
                        allow = false;
                        break;
                    }
                }
            }
            if (!allow)
            {
                pVerify.setCancel(true);
                pVerify.setHandled(true);
            }
        }
    }
    /**
     * Message from the drawing area that a context menu is about to be displayed.
     *
     * @param pParentDiagram [in] The diagram where the context menu is about to appear.
     * @param contextMenu [in] The context menu to add or remove buttons from.
     */
    public void onDrawingAreaContextMenuPrepare(IDiagram pDiagram, IProductContextMenu pContextMenu)
    {
        // Not needed for jUML - use plugin files instead
                /*
           if (pDiagram != null && pContextMenu != null)
           {
                        // because the user has right clicked on the drawing area, we will want to try
                        // and fill the participants with what is selected on the diagram
                        m_PatternManager.setParticipantScope(DesignPatternParticipantScopeEnum.DPPARTICIPANT_SCOPE_PRESENTATION);
                        m_PatternManager.setProjectTree(null);
                        if (m_EventsSink instanceof IProductContextMenuSelectionHandler)
                        {
                                IProductContextMenuSelectionHandler pSelectionHandler = (IProductContextMenuSelectionHandler)m_EventsSink;
                                 // Since we changed the gui to be a wizard, we want the user to always be
                                 // able to apply a pattern, no restrictions on what is selected
                                DesignPatternUtilities.addApplyMenuItem(pContextMenu, pSelectionHandler);
                        }
           }
                 */
    }
    /**
     * Adds the "Promote..." menu item to the right click menu in the project tree
     *
     * @param pContextMenu[in]			The current context menu
     *
     * return HRESULT
     */
    public void addPromoteMenuItem(IProductContextMenu pContextMenu)
    {
        // Not needed for jUML - use plugin files instead
                /*
                if (pContextMenu != null)
                {
                        // get the selection handler
                        if (m_EventsSink instanceof IProductContextMenuSelectionHandler)
                        {
                                IProductContextMenuSelectionHandler pSelectionHandler = (IProductContextMenuSelectionHandler)m_EventsSink;
                                if (pSelectionHandler != null)
                                {
                                        String name = DesignPatternUtilities.translateString("IDS_POPUP_PROMOTE");
                                        // determine whether or not this button should be greyed out or not
                                        boolean bSensitive = true;
                                        // create the menu item that we will be adding
                                        // I'm creating a fake MBK value here so the the menu sorter
                                        // can sort the menu.
                                        IProductContextMenuItem pMenuItem = DesignCenterUtilities.createMenuItemOnMain(pContextMenu,
                                                                                name,
                                                                                "MBK_DESIGN_PATTERN_PROMOTE",
                                                                                bSensitive,
                                                                                pSelectionHandler);
                                }
                        }
           }
                 */
    }
    /**
     * Message from the sink that something has been selected
     */
    public void handleSelection(IProductContextMenu pContextMenu, IProductContextMenuItem pSelectedItem)
    {
        // Not needed for jUML - see onHandleButton
                /*
           if (pContextMenu != null && pSelectedItem != null)
           {
                        // get the string of the item that was selected (clicked on)
                        String menuStr = pSelectedItem.getMenuString();
                        // load our standard menu button text strings
                        String applyStr = DesignPatternUtilities.translateString("IDS_POPUP_DESIGNPATTERN"); // Design Patterns...
                        String promoteStr = DesignPatternUtilities.translateString("IDS_POPUP_PROMOTE"); // Promote...
                        if (menuStr.equals(applyStr))
                        {
                                handleApply(pContextMenu, pSelectedItem);
                        }
                        else if (menuStr.equals(promoteStr))
                        {
                                handlePromote(pContextMenu, pSelectedItem);
                        }
                        else
                        {
                        }
           }
                 */
    }
    /**
     * Called when the apply menu button was clicked
     *
     * @param pContextMenu[in]			The current context menu
     * @param pSelectedItem[in]		The current menu item
     *
     * return HRESULT
     */
    public void handleApply(IProductContextMenu pContextMenu, IProductContextMenuItem pSelectedItem)
    {
        // Not needed for jUML - see onHandleButton
                /*
           if (pContextMenu != null && pSelectedItem != null)
           {
                        // get the pattern that is selected
                        ICollaboration pCollab = DesignPatternUtilities.getSelectedCollaboration(pContextMenu);
                        if (pCollab != null)
                        {
                                apply(pCollab);
                        }
                        else
                        {
                                // the user has not clicked on a pattern so we will need to present them with the wizard
                                // so that they can pick one
                                apply(pCollab);
                        }
           }
                 */
    }
    /**
     * Called when the promote menu button was clicked
     *
     * @param pContextMenu[in]			The current context menu
     * @param pSelectedItem[in]		The current menu item
     *
     * return HRESULT
     */
    public void handlePromote(IProductContextMenu pContextMenu, IProductContextMenuItem pSelectedItem)
    {
        // Not needed for jUML - see onHandleButton
                /*
           if (pContextMenu != null && pSelectedItem != null)
           {
                        // get the pattern that is selected in the tree
                        ICollaboration pCollab = DesignPatternUtilities.getSelectedCollaboration(pContextMenu);
                        if (pCollab != null)
                        {
                                promote(pCollab);
                        }
           }
                 */
    }
    /**
     * Begin the process to apply ("instantiate") the pattern
     *
     * @param pDispatch[in]			The pattern to apply
     *
     * @return HRESULT
     *
     */
    public void apply(Object pDispatch)
    {
        //
        // Now begin applying the pattern, first must build the information about it
        //
        if (m_PatternManager != null)
        {
            // build the details for this pattern because that is what is used
            // to apply the pattern - the pattern details
            IDesignPatternDetails pDetails = new DesignPatternDetails();
            ICollaboration pCollab = null;
            if (pDispatch instanceof ICollaboration)
            {
                pCollab = (ICollaboration)pDispatch;
            }
            if (pCollab != null)
            {
                m_PatternManager.buildPatternDetails(pDispatch, pDetails);
            }
            // figure out if the all of the roles of the pattern have been
            // fulfilled (have instances for each of the roles)
            // because if they are, we will not show the dialog
            boolean bFulfill = m_PatternManager.isPatternFulfilled(pDetails);
            if (!bFulfill)
            {
                // Display the GUI to the user
                String title = DefaultDesignPatternResource.getString("IDS_WIZARD_TITLE");
                Wizard wiz = new Wizard(ProductHelper.getProxyUserInterface().getWindowHandle(), title, true);
                wiz.setManager(m_PatternManager);
                wiz.setDetails(pDetails);
                wiz.init(null, null, null);
                if (wiz.doModal() == IWizardSheet.PSWIZB_FINISH)
                {
                    // user has hit okay on the gui and the gui information
                    // has been validated, so begin the process of applying it
                    m_PatternManager.setDialog(wiz);
                    m_PatternManager.applyPattern(pDetails);
                }
            }
            else
            {
                // gui does not need to be displayed, so begin the process of applying
                m_PatternManager.applyPattern(pDetails);
            }
        }
    }
    /**
     * Begin the process to promote the pattern to the design center
     *
     * @param pDispatch[in]			The pattern to apply
     *
     * @return HRESULT
     */
    public void promote(Object pDispatch)
    {
        if (pDispatch != null)
        {
            ICollaboration pCollab = null;
            if (pDispatch instanceof ICollaboration)
            {
                pCollab = (ICollaboration)pDispatch;
            }
            if (pCollab != null)
            {
                // build the details for this pattern because that is what is used
                // to promote the pattern - the pattern details
                IDesignPatternDetails pDetails = new DesignPatternDetails();
                m_PatternManager.buildPatternDetails(pCollab, pDetails);
                PromotePatternPanel panel = new PromotePatternPanel(m_PatternManager, pDetails);
                if (pDetails != null)
                {
                    DialogDescriptor dialogDescriptor=new DialogDescriptor(panel,
                        NbBundle.getMessage(PromotePatternPanel.class,
                        "IDS_PROMOTETITLE")); // NOI18N
                    
                    Dialog dialog=DialogDisplayer.getDefault().createDialog(
                        dialogDescriptor);
                    try
                    {
                        dialog.setVisible(true);
                        
                        if (dialogDescriptor.getValue()==DialogDescriptor.OK_OPTION)
                        {
                            panel.promote();
                        }
                    }
                    finally
                    {
                        dialog.dispose();
                    }
                }
                //				if (pDetails != null)
                //				{
                //					// Display the GUI to the user
                //					Frame parent = ProductHelper.getProxyUserInterface().getWindowHandle();
                //					PromoteDialogUI ui = new PromoteDialogUI(parent, true);
                //					ui.doLayout();
                //					ui.setModal(true);
                //					ui.setDetails(pDetails);
                //					ui.setManager(m_PatternManager);
                //					ui.setVisible(true);
                //				}
            }
        }
    }
    /**
     * Event fired by the drawing area before an item is dropped.
     *
     *
     * @param pParentDiagram[in]		The diagram being dropped on
     * @param pContext[in]				The context representing the item being dropped on
     * @param cell[in]					The result cell
     *
     * @return HRESULT
     *
     */
    public void onDrawingAreaPreDrop(IDiagram pParentDiagram, IDrawingAreaDropContext pContext, IResultCell cell)
    {
        if (pParentDiagram != null && pContext != null)
        {
            if (m_PatternManager != null)
            {
                // ask the pattern manager if we should even be allowing a drag and drop
                boolean bAllow = m_PatternManager.allowDragAndDrop(pContext);
                if (bAllow)
                {
                    ICollaboration pCollab = m_PatternManager.getDragAndDropCollab(pContext);
                    // yes, allow the drag and drop
                    if (pCollab != null)
                    {
                        boolean handled = false;
                        // get what it was dropped on - we will first check the compartment
                        // to see if the pattern dropped applies specifically to a compartment
                        ICompartment pCompartment = pContext.getCompartmentDroppedOn();
                        if (pCompartment != null)
                        {
                            // get the model element associated with this compartment
                            IElement pElement = pCompartment.getModelElement();
                            if (pElement != null)
                            {
                                // ask the pattern manager if the pattern roles are fulfilled
                                // by this element because if they are, we will just do the apply
                                // and not display the dialog
                                boolean bFulfill = m_PatternManager.doesElementFulfillPattern(pElement, pCollab);
                                if (bFulfill)
                                {
                                    handled = true;
                                    // set up some information for the apply
                                    pContext.setCancel(true);
                                    m_PatternManager.setCollaboration(pCollab);
                                    m_PatternManager.setParticipantScope(DesignPatternParticipantScopeEnum.DPPARTICIPANT_SCOPE_COMPARTMENT);
                                    m_PatternManager.setProjectTree(null);
                                    apply(pCollab);
                                }
                            }
                        }
                        // if we are to this point and haven't done anything still more checking
                        // to do because the compartment stuff didn't pan out
                        if (!handled)
                        {
                            // get what it was dropped on
                            IPresentationElement pPresElement = pContext.getPEDroppedOn();
                            if (pPresElement != null)
                            {
                                pContext.setCancel(true);
                                // set up some information for the apply by using
                                // what was dropped on
                                m_PatternManager.setCollaboration(pCollab);
                                m_PatternManager.setParticipantScope(DesignPatternParticipantScopeEnum.DPPARTICIPANT_SCOPE_PRESENTATION);
                                m_PatternManager.setProjectTree(null);
                                apply(pCollab);
                            }
                            else
                            {
                                // probably dropping onto a diagram
                                // so now if the diagram is owned by the project of this addin
                                // we will want to treat this like the user wants to visualize the pattern
                                // so we won't do anything
                                // if it is a diagram not in the design center, we will ask the user to apply
                                // it through the GUI
                                boolean bOwned = m_PatternManager.diagramOwnedByAddInProject(null, pParentDiagram, pContext);
                                if (!bOwned)
                                {
                                    pContext.setCancel(true);
                                    m_PatternManager.setCollaboration(pCollab);
                                    m_PatternManager.setParticipantScope(DesignPatternParticipantScopeEnum.DPPARTICIPANT_SCOPE_PRESENTATION);
                                    m_PatternManager.setProjectTree(null);
                                    apply(pCollab);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    /**
     * In order to properly display and eventually "instantiate" the pattern, we need
     * to know certain pieces of information.  The design pattern details houses
     * this information.
     *
     * @param pDispatch[in]			The dispatch representing the collaboration(pattern)
     * @param pDetails[out]			The newly created pattern details
     *
     * return HRESULT
     */
    public void buildPatternDetails( Object pDispatch, IDesignPatternDetails pDetails)
    {
        if (pDispatch != null && pDetails != null)
        {
            // defer part of the process to the manager
            m_PatternManager.buildPatternDetails(pDispatch, pDetails);
        }
    }
    /**
     * Begin the process to apply ("instantiate") the pattern in the passed in details
     *
     *
     * @param pDetails[in]		The design pattern details to use in applying a pattern
     *
     * @return HRESULT
     *
     */
    public void apply2(IDesignPatternDetails pDetails)
    {
        if (pDetails != null)
        {
            ICollaboration pCollab = pDetails.getCollaboration();
            if (pCollab != null)
            {
                //
                // Now begin applying the pattern
                //
                if (m_PatternManager != null)
                {
                    // Display the GUI to the user
                    Wizard wiz = new Wizard(null, "", true);
                    wiz.setManager(m_PatternManager);
                    wiz.setDetails(pDetails);
                    wiz.init(null, null, null);
                    if (wiz.doModal() == IWizardSheet.PSWIZB_FINISH)
                    {
                        // user has hit okay on the gui and the gui information
                        // has been validated, so begin the process of applying it
                        m_PatternManager.applyPattern(pDetails);
                    }
                }
            }
        }
    }
    /**
     * Get the patterns in the project that this addin knows about (the current project)
     *
     * @param pElements[out]	The patterns
     *
     * return HRESULT
     */
    public ETList <IElement> getPatterns()
    {
        ETList <IElement> pElements = new ETArrayList<IElement>();
        if (pElements != null)
        {
            // need the projects
            Object pDisp = getWorkspace();
            if (pDisp instanceof IWorkspace)
            {
                IWorkspace pWork = (IWorkspace)pDisp;
                ETList <IWSProject> pProjects = pWork.getWSProjects();
                if (pProjects != null)
                {
                    int count = pProjects.size();
                    for (int x = 0; x < count; x++)
                    {
                        IWSProject pWSProj = pProjects.get(x);
                        if (pWSProj != null)
                        {
                            String name = pWSProj.getName();
                            IProject pProj = getProject(name);
                            if (pProj != null)
                            {
                                ETList <IElement> pTempEles = m_PatternManager.getPatternsInProject(pProj);
                                if (pTempEles != null)
                                {
                                    int eleCount = pTempEles.size();
                                    for (int y = 0; y < eleCount; y++)
                                    {
                                        IElement pEle = pTempEles.get(y);
                                        if (pEle != null)
                                        {
                                            pElements.add(pEle);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return pElements;
    }
    /**
     * Open has been called on the passed in tree item.  Respond accordingly
     *
     * @param[in]	pDispatch		The project tree item to open
     *
     * @return HRESULT
     */
    public void open(Object pTreeItem)
    {
        // This addin is not going to do anything when promote is called.
    }
    /**
     *
     * Retrieves the collaboration object by the passed in name
     *
     * @param id[in]				The id of the pattern to retrieve
     * @param pCollab[out]		The pattern
     *
     * @return HRESULT
     *
     */
    public ICollaboration getPatternByID(String id)
    {
        ICollaboration pCollab = null;
        if (id != null && id.length() > 0)
        {
            // this is not coded in c++ either
                        /*
                        IProject pProject;
                        DesignPatternUtilities DesignPatternUtilities;
                        DesignPatternUtilities.GetCurrentProject(&pProject);
                        if (pProject)
                        {
                                DesignPatternUtilities DesignPatternUtilities;
                                DesignPatternUtilities.GetPatternByID(id, pProject, pCollab);
                        }
                         */
        }
        return pCollab;
    }
    /**
     * Message from the workspace that a project has been removed
     */
    public void onWSProjectOpened(IWSProject wsProject)
    {
        if (wsProject != null)
        {
            // copied from Application::OpenProjectFromWSProject
            // All Project data is streamed to a IWSElement stream
            // by the name of "_MetaData__"
            
            IWSElement element = wsProject.getElementByName("_MetaData__");
            if( element != null )
            {
                String location = element.getLocation();
                IApplication pApp = ProductHelper.getApplication();
                if (pApp != null)
                {
                    IProject pProject = pApp.openProject( location );
                    if( pProject != null)
                    {
                        if (pProject instanceof ITwoPhaseCommit)
                        {
                            ITwoPhaseCommit commit = (ITwoPhaseCommit)pProject;
                            if( commit != null )
                            {
                                element.setTwoPhaseCommit( commit );
                            }
                        }
                    }
                }
            }
        }
    }
    /**
     * Message from the workspace that a project has been removed
     */
    public void onWSProjectRemoved(IWSProject wsProject)
    {
        if (wsProject != null)
        {
            boolean isOpen = wsProject.isOpen();
            if( isOpen )
            {
                // RetrieveProjectFromWSProject( wsProject, &project ));
                IWSElement element = wsProject.getElementByName("_MetaData__");
                if( element != null)
                {
                    ITwoPhaseCommit commit = element.getTwoPhaseCommit();
                    if (commit instanceof IProject)
                    {
                        IProject project = (IProject)commit;
                        if( project != null )
                        {
                            IApplication pApp = ProductHelper.getApplication();
                            if (pApp != null)
                            {
                                pApp.closeProject( project, false );
                            }
                        }
                    }
                }
            }
            
            //notify the project tree that a node is removed.
            if (m_DesignCenterTree != null && m_DesignCenterTree instanceof JProjectTree)
            {
                JProjectTree tree = (JProjectTree)m_DesignCenterTree;
                
                IProjectTreeModel model = tree.getProjectModel();
                if (model != null && model instanceof DesignCenterSwingModel)
                {
                    DesignCenterSwingModel desModel = (DesignCenterSwingModel)model;
                    IWorkspace space = getWorkspace();
                    ITreeItem parent = desModel.getWorkspaceNode(space, null);
                    if (space != null)
                    {
                        space.setIsDirty(true);
                    }
                    
                    IProjectTreeItem[] items = m_DesignCenterTree.getSelected();
                    if (items != null)
                    {
                        int count = items.length;
                        for (int i=0; i<count; i++)
                        {
                            IProjectTreeItem item = items[i];
                            ITreeItem child = item.getProjectTreeSupportTreeItem();
                            desModel.removeNodeFromParent(child);
                        }
                    }
                }
            }
            
            if (m_DesignCenterTree != null)
            {
                m_DesignCenterTree.refresh(true);
            }
        }
    }
    /**
     * Message from the workspace that a project has been inserted
     */
    public void onWSProjectInserted(IWSProject pProject)
    {
        if (pProject != null)
        {
            if (m_DesignCenterTree != null)
            {
                //notify the project tree that a node is removed.
                if (m_DesignCenterTree != null && m_DesignCenterTree instanceof JProjectTree)
                {
                    JProjectTree tree = (JProjectTree)m_DesignCenterTree;
                    
                    IProjectTreeModel model = tree.getProjectModel();
                    if (model != null && model instanceof DesignCenterSwingModel)
                    {
                        DesignCenterSwingModel desModel = (DesignCenterSwingModel)model;
                        ITreeItem parent = desModel.getWorkspaceNode(getWorkspace(), null);
                        
                        boolean isOpen = pProject.isOpen();
                        if( isOpen )
                        {
                            // RetrieveProjectFromWSProject( wsProject, &project ));
                            IWSElement element = pProject.getElementByName("_MetaData__");
                            if( element != null)
                            {
                                ITwoPhaseCommit commit = element.getTwoPhaseCommit();
                                if (commit instanceof IProject)
                                {
                                    IProject project = (IProject)commit;
                                    if( project != null )
                                    {
                                        desModel.addProject(pProject.getName(), parent, project);
                                    }
                                }
                            }
                        }
                    }
                }
                
                m_DesignCenterTree.refresh(true);
            }
        }
    }
    /**
     * Message from the core product that the product is saving
     */
    public void onCoreProductPreSaved(ICoreProduct pProduct)
    {
        if (pProduct != null)
        {
            Object pDisp = getWorkspace();
            if (pDisp instanceof IWorkspace)
            {
                IWorkspace pWork = (IWorkspace)pDisp;
                boolean bDirty = pWork.isDirty();
                if (bDirty)
                {
                    pWork.save();
                }
            }
        }
    }
    /**
     * Message from the core product that the product is saving
     */
    public void onCoreProductPreQuit(ICoreProduct pProduct)
    {
        if (pProduct != null)
        {
            boolean bSave = false;
            Object pDisp = getWorkspace();
            if (pDisp instanceof IWorkspace)
            {
                IWorkspace pWork = (IWorkspace)pDisp;
                if (pWork != null)
                {
                    boolean bDirty = pWork.isDirty();
                    if (bDirty)
                    {
                        IQuestionDialog pQuestionDialog = new SwingQuestionDialogImpl();
                        if (pQuestionDialog != null)
                        {
                            String title = DesignPatternUtilities.translateString("IDS_WORKSPACETITLE");
                            String msg = DesignPatternUtilities.translateString("IDS_WORKSPACESAVE");
                            QuestionResponse result = pQuestionDialog.displaySimpleQuestionDialog(SimpleQuestionDialogKind.SQDK_YESNO,
                                MessageIconKindEnum.EDIK_ICONWARNING,
                                msg,
                                SimpleQuestionDialogResultKind.SQDRK_RESULT_YES,
                                null,
                                title);
                            if (result.getResult() == SimpleQuestionDialogResultKind.SQDRK_RESULT_YES)
                            {
                                // Save the workspace
                                bSave = true;
                            }
                        }
                    }
                    if (bSave)
                    {
                        boolean origFlag = EventBlocker.startBlocking();
                        try
                        {
                            pWork.save();
                        }
                        finally
                        {
                            EventBlocker.stopBlocking(origFlag);
                        }
                    }
                }
            }
            deInitialize(null);
        }
    }
    /**
     * Message from the core product that the product is saving
     */
    public void onWorkspaceSaved(IWorkspace space)
    {
    }
    /**
     * Message from the workspace that a project has been created
     */
    public void onWSProjectCreated(IWSProject pWSProject)
    {
        if (pWSProject != null)
        {
            String name = pWSProject.getName();
            // There are a couple of things that were handled by the application in this event
            // that we will need to do manually because the application is not receiving the events
            // by this workspace manager.  See Application::OnWSProjectCreated for the specifics
            // but we are duplicating the work here.
            IApplication pApp = ProductHelper.getApplication();
            if (pApp != null)
            {
                IProject pProject = pApp.createProject();
                if (pProject != null)
                {
                    pProject.setName( name );
                    // now we need to attach the project and the workspace project
                    // unfortunately this logic is on the application, but not exposed
                    // for time's sake, duplicating it
                    addProjectToWSProject( pWSProject, pProject );
                    
                    //notify the project tree that a project node is added.
                    if (m_DesignCenterTree != null && m_DesignCenterTree instanceof JProjectTree)
                    {
                        JProjectTree tree = (JProjectTree)m_DesignCenterTree;
                        
                        IProjectTreeModel model = tree.getProjectModel();
                        if (model != null && model instanceof DesignCenterSwingModel)
                        {
                            DesignCenterSwingModel desModel = (DesignCenterSwingModel)model;
                            IWorkspace space = getWorkspace();
                            ITreeItem parent = desModel.getWorkspaceNode(space, null);
                            if (space != null)
                            {
                                space.setIsDirty(true);
                            }
                            
                            desModel.addProject(name, parent, pProject);
                        }
                    }
                    
                    //mark the ws project as dirty now, so that it gets saved
                    pWSProject.setIsDirty(true);
                }
            }
        }
    }
    /**
     * Message from the workspace that a project has been created
     */
    public void onProjectCreated( IProject project )
    {
        if (project != null)
        {
            if (m_DesignCenterTree != null)
            {
                m_DesignCenterTree.refresh(true);
                IWorkspace space = getWorkspace();
                if (space != null && space.isOpen())
                {
                    IWSProject wsProj = space.getWSProjectByName(project.getName());
                    if (wsProj != null)
                    {
                        m_DesignCenterTree.openProject(project);
                    }
                }
            }
        }
    }
    /**
     * Message from the workspace that a project has been opened
     */
    public void onProjectOpened( IProject project )
    {
        if (project != null)
        {
            if (m_DesignCenterTree != null)
            {
                //m_DesignCenterTree.refresh(true);
                IWorkspace space = getWorkspace();
                if (space != null && space.isOpen())
                {
                    IWSProject wsProj = space.getWSProjectByName(project.getName());
                    if (wsProj != null)
                    {
                        m_DesignCenterTree.openProject(project);
                    }
                }
            }
        }
    }
    
    /**
     * Message from the workspace that a project has been created
     */
    public void onProjectClosed(IProject project )
    {
        if (project != null)
        {
            if (m_DesignCenterTree != null)
            {
                String name = project.getName();
                IWorkspace pWork = getWorkspace();
                
                if (pWork != null)
                {
                    
                    boolean isOpen = pWork.isOpen();
                    if (isOpen)
                    {
                        IWSProject pWSProject = pWork.getWSProjectByName(name);
                        if (pWSProject != null)
                        {
                            // found it in the current workspace so close by our old process
                            pWork.closeWSProjectByName(name, true);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Message from the workspace that a project has been created
     */
    public void onProjectPreClosed(IProject project, IResultCell cell )
    {
        //by default we do not want to close design pattern project
        //      cell.setContinue(false);
        //      if (project != null)
        //      {
        //         if (m_DesignCenterTree != null)
        //         {
        //            String name = project.getName();
        //            IWorkspace pWork = getWorkspace();
        //
        //            if (pWork != null)
        //            {
        //
        //               boolean isOpen = pWork.isOpen();
        //               if (isOpen)
        //               {
        //                  IWSProject pWSProject = pWork.getWSProjectByName(name);
        //                  if (pWSProject != null)
        //                  {
        //                     //we want to proceed here only if the user is explicitly requested to close project.
        //                     if (m_ClosingProject)
        //                     {
        //                        cell.setContinue(true);
        //                     }
        //                  }
        //               }
        //            }
        //         }
        //      }
    }
    
    public void closeProject(IProject pProject)
    {
        boolean closed = false;
        if (pProject != null)
        {
            String projID = pProject.getXMIID();
            String projName = pProject.getName();
            IApplication pApp = ProductHelper.getApplication();
            if (pProject instanceof ITwoPhaseCommit && pApp != null)
            {
                ITwoPhaseCommit pTwoPhase = (ITwoPhaseCommit)pProject;
                boolean isDirty = pTwoPhase.isDirty();
                boolean isDiagramsDirty = false;
                if (!isDirty)
                {
                    // See if any of the diagrams are dirty
                    IProxyDiagramManager diaMgr = ProxyDiagramManager.instance();
                    isDirty = diaMgr.areAnyOpenDiagramsDirty(pProject);
                    isDiagramsDirty = isDirty;
                }
                
                boolean bSaveFirst = false;
                boolean cancel = false;
                if (isDirty)
                {
                    IQuestionDialog dialog = new SwingQuestionDialogImpl();
                    String title = DesignPatternUtilities.translateString("IDS_SAVEPROJECTTITLE");
                    String msg = DesignPatternUtilities.translateString("IDS_SAVEPROJECT");
                    QuestionResponse response = dialog.displaySimpleQuestionDialog(SimpleQuestionDialogKind.SQDK_YESNOCANCEL,
                        MessageIconKindEnum.EDIK_ICONWARNING,
                        msg,
                        SimpleQuestionDialogResultKind.SQDRK_RESULT_YES,
                        null,
                        title);
                    if (response.getResult() != QuestionResponse.SQDRK_RESULT_CANCEL)
                    {
                        if (response.getResult() == QuestionResponse.SQDRK_RESULT_YES)
                        {
                            bSaveFirst = true;
                        }
                    }
                    else
                    {
                        cancel = true;
                    }
                }
                
                if (!cancel)
                {
                    // first see if the project is in the workspace (design center projects are not)
                    IWorkspace pWorkspace = getWorkspace();
                    if (pWorkspace != null)
                    {
                        IWSProject wsProj = pWorkspace.getWSProjectByName(projName);
                        if (wsProj != null && wsProj.isOpen())
                        {
                            // Now close all open diagrams
                            closeAllOpenDiagrams(pProject, false);
                            pApp.closeProject(pProject, bSaveFirst);
                        }
                    }
                    closed = true;
                }
            }
        }
    }
    
    private void closeAllOpenDiagrams(IProject pProject, boolean isDiagramsDirty)
    {
        String topLevelId = "";
        if (pProject != null)
        {
            topLevelId = pProject.getXMIID();
        }
        
        boolean cancel = false;
        boolean bSaveFirst = false;
        if (isDiagramsDirty)
        {
            IQuestionDialog dialog = new SwingQuestionDialogImpl();
            String title = DesignPatternUtilities.translateString("IDS_SAVEDIAGRAMTITLE");
            String msg = DesignPatternUtilities.translateString("IDS_SAVEDIAGRAM");
            QuestionResponse response = dialog.displaySimpleQuestionDialog(SimpleQuestionDialogKind.SQDK_YESNOCANCEL,
                MessageIconKindEnum.EDIK_ICONWARNING,
                msg,
                SimpleQuestionDialogResultKind.SQDRK_RESULT_YES,
                null,
                title);
            if (response.getResult() != QuestionResponse.SQDRK_RESULT_CANCEL)
            {
                if (response.getResult() == QuestionResponse.SQDRK_RESULT_YES)
                {
                    bSaveFirst = true;
                }
            }
            else
            {
                cancel = true;
            }
        }
        
        IProductDiagramManager diaMgr = ProductHelper.getProductDiagramManager();
        if (diaMgr != null)
        {
            // Get the open diagrams that have not yet been saved
            ETList<IProxyDiagram> pProxyDias = diaMgr.getOpenDiagrams();
            int count = 0;
            if (pProxyDias != null)
            {
                count = pProxyDias.size();
            }
            
            for (int i=0; i<count; i++)
            {
                IProxyDiagram pDia = pProxyDias.get(i);
                boolean bContinue = true;
                if (topLevelId != null && topLevelId.length() > 0)
                {
                    String diaTopLevelId = "";
                    DiagramDetails details = pDia.getDiagramDetails();
                    if (details != null)
                    {
                        diaTopLevelId = details.getToplevelXMIID();
                    }
                    if (diaTopLevelId != null && !diaTopLevelId.equals(topLevelId))
                    {
                        bContinue = false;
                    }
                }
                
                if (bContinue)
                {
                    IDiagram dia = pDia.getDiagram();
                    if (dia != null)
                    {
                        diaMgr.closeDiagram2(dia);
                    }
                }
            }
        }
    }
    
    /**
     * Message from the workspace that a project has been created
     */
    public void onWSProjectClosed(IWSProject wsProject)
    {
        if (wsProject != null)
        {
            boolean isOpen = wsProject.isOpen();
            if( isOpen )
            {
                // RetrieveProjectFromWSProject( wsProject, &project ));
                IWSElement element = wsProject.getElementByName("_MetaData__");
                if( element != null)
                {
                    ITwoPhaseCommit commit = element.getTwoPhaseCommit();
                    if (commit instanceof IProject)
                    {
                        IProject project = (IProject)commit;
                        if( project != null )
                        {
                            closeAllOpenDiagrams(project, false);
                            if (m_DesignCenterTree != null && m_DesignCenterTree instanceof JProjectTree)
                            {
                                JProjectTree tree = (JProjectTree)m_DesignCenterTree;
                                IProjectTreeItem[] items = m_DesignCenterTree.getSelected();
                                if (items != null && items.length > 0)
                                {
                                    IProjectTreeItem item = items[0];
                                    tree.setIsExpanded(item, false);
                                    ITreeItem treeItem = item.getProjectTreeSupportTreeItem();
                                    if (treeItem != null)
                                    {
                                        //remove all children so that they can be rebuilt.
                                        if( treeItem instanceof DefaultMutableTreeNode)
                                        {
                                            ((DefaultMutableTreeNode)treeItem).removeAllChildren();
                                        }
                                        
                                        //specify that there is no element on this node.
                                        if (treeItem instanceof ITreeElement)
                                        {
                                            ((ITreeElement)treeItem).setElement(null);
                                        }
                                        treeItem.setExpanded(false);
                                        //set this node as not initialized, so that next time it is opened, it
                                        //gets properly initialized.
                                        treeItem.setIsInitalized(false);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    /**
     * Message from the workspace that a project has been created
     */
    public void onWSProjectSaved(IWSProject wsProject)
    {
        if (wsProject != null)
        {
            boolean isOpen = wsProject.isOpen();
            if( isOpen )
            {
                // RetrieveProjectFromWSProject( wsProject, &project ));
                IWSElement element = wsProject.getElementByName("_MetaData__");
                if( element != null)
                {
                    ITwoPhaseCommit commit = element.getTwoPhaseCommit();
                    if (commit instanceof IProject)
                    {
                        IProject project = (IProject)commit;
                        if( project != null )
                        {
                            String fileName = project.getFileName();
                            if( fileName == null || ( fileName.length() == 0 ))
                            {
                                // ATLASSERT( !"Project has not been saved yet. Need to return a Need to save message" );
                            }
                            else
                            {
                                project.save( fileName, true );
                            }
                        }
                    }
                }
            }
        }
    }
    /**
     *
     * Adds the IProject to the IWSProject.
     *
     * @param wsProject[in] The WSProject to add the Project to
     * @param project[in] The Project to attach to the WSProject
     *
     * @return HRESULT
     *
     */
    public void addProjectToWSProject( IWSProject wsProject, IProject project )
    {
        if (wsProject != null && project != null)
        {
            // The IProject will be referenced by the "_MetaData__" name.
            IWSElement element = null;
            String fileName = project.getFileName();
            String location = "";
            if( fileName.length() == 0 )
            {
                // If a filename has not been established, let's see if the Project has
                // a name yet. If so, use that instead of _MetaData__
                String projName = project.getName();
                if( projName.length() == 0)
                {
                    projName = "_MetaData__";
                }
                projName += ".etd";
                
                String wsLoc = wsProject.getBaseDirectory();
                if( wsLoc.length() > 0)
                {
                    location = wsLoc;
                    location = FileSysManip.addBackslash(location);
                    location += projName;
                    project.setFileName( location );
                }
            }
            else
            {
                location =  fileName;
            }
            
            // Store the XMI ID of the project in the Workspace file
            
            String projID = project.getXMIID();
            element = wsProject.addElement( location, "_MetaData__", projID);
            // EstablishTwoPhaseConnection( element, project ));
            if (project instanceof ITwoPhaseCommit)
            {
                ITwoPhaseCommit commit = (ITwoPhaseCommit)project;
                if( commit != null)
                {
                    element.setTwoPhaseCommit( commit );
                }
            }
        }
    }
    /**
     * Because we want to reuse many of the menus used by the project tree, this
     * routine is going to tell the project tree engine which menus it wants to
     * reuse based on its mbk_values
     *
     * @param pMenuTitles[out]
     *
     * @return HRESULT
     *
     */
    public ETList <String> addDefaultMenuItems()
    {
        ETList <String> pMenuTitles = new ETArrayList<String>();
        if (pMenuTitles != null)
        {
            // overridden project with "Catalog"
            String str = "MBK_OPEN_PROJECT";
            pMenuTitles.add(str);
            str = "MBK_OPEN_DIAGRAM";
            pMenuTitles.add(str);
            str = "MBK_NEW_PROJECT";
            pMenuTitles.add(str);
            str = "MBK_NEW_DIAGRAM";
            pMenuTitles.add(str);
            str = "MBK_NEW_PACKAGE";
            pMenuTitles.add(str);
            str = "MBK_NEW_ELEMENT";
            pMenuTitles.add(str);
            str = "MBK_NEW_ATTRIBUTE";
            pMenuTitles.add(str);
            str = "MBK_NEW_OPERATION";
            pMenuTitles.add(str);
            str = "MBK_SAVE";
            pMenuTitles.add(str);
            str = "MBK_CLOSE";
            pMenuTitles.add(str);
            str = "MBK_INSERT_PROJECT";
            pMenuTitles.add(str);
            str = "MBK_REMOVE_PROJECT_FROM_WORKSPACE";
            pMenuTitles.add(str);
            str = "MBK_DELETE_OBJECT";
            pMenuTitles.add(str);
            str = "MBK_RENAME";
            pMenuTitles.add(str);
            str = "MBK_MANAGE_PROJECT";
            pMenuTitles.add(str);
            str = "MBK_FILTER_DIALOG";
            pMenuTitles.add(str);
            str = "MBK_DISPLAY_PROPERTYEDITOR";
            pMenuTitles.add(str);
            str = "MBK_EXPAND_ALL_PACKAGES";
            pMenuTitles.add(str);
        }
        return pMenuTitles;
    }
    
    public boolean onHandleButton(ActionEvent e, String menuID)
    {
        boolean bHandled = false;
        String transStr = DesignPatternUtilities.translateString("IDS_POPUP_APPLY2");
        String transStr2 = DesignPatternUtilities.translateString("IDS_POPUP_PROMOTE");
        if (menuID.equals(transStr))
        {
            bHandled = true;
            // get the pattern that is selected
            ICollaboration pCollab = DesignPatternUtilities.getSelectedCollaboration(m_Context);
            if (pCollab != null)
            {
                apply(pCollab);
            }
            else
            {
                // the user has not clicked on a pattern so we will need to present them with the wizard
                // so that they can pick one
                apply(pCollab);
            }
        }
        else if (menuID.equals(transStr2))
        {
            bHandled = true;
            // get the pattern that is selected in the tree
            ICollaboration pCollab = DesignPatternUtilities.getSelectedCollaboration(m_Context);
            if (pCollab != null)
            {
                promote(pCollab);
            }
        }
        return bHandled;
    }
    public ContextMenuActionClass createMenuAction(String text, String menuID)
    {
        return new ContextMenuActionClass(this, text, menuID);
    }
    
    public boolean setSensitivityAndCheck(String menuID, ContextMenuActionClass pMenuAction)
    {
        boolean bEnable = false;
        // Not needed for jUML - see validate
                /*
                // Is it the project tree
                if (m_FromDiagram == false && m_AssociateContext instanceof IProjectTreeControl)
                {
                        IProjectTreeControl pControl = (IProjectTreeControl)m_AssociateContext;
                        // we are in the project tree, but there are two project trees, so if we are in the
                        // design center, we don't want to enable this button if we are in a shipped project
                        // vs. a user project, so all this code is checking to see if any of the selected
                        // items in the tree belong to an etpat project
                        boolean continueFlag = true;
                        String mgrName = pControl.getConfigMgrName();
                        if (mgrName.equals("DesignCenter"))
                        {
                                boolean isSel = isProjectTreeItemSelected(pControl);
                                if (isSel)
                                {
                                        IProjectTreeItem[] pTreeItems = pControl.getSelected();
                                        if (pTreeItems != null)
                                        {
                                                int count = pTreeItems.length;
                                                for (int x = 0; x < count; x++)
                                                {
                                                        IProjectTreeItem pTreeItem = pTreeItems[x];
                                                        if (pTreeItem != null)
                                                        {
                 
                                                                IProject pProject = pControl.retrieveProjectFromItem(pTreeItem);
                                                                if (pProject != null)
                                                                {
                                                                        boolean bMember = isMemberOfDesignCenterProject(pProject);
                                                                        if (bMember)
                                                                        {
                                                                                // it is a member of an etpat file so bail and do not
                                                                                // enable the associate with button
                                                                                continueFlag = false;
                                                                                break;
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }
                                else
                                {
                                        // the tree item is in the design center, but is not a designated project
                                        // tree item, so it is probably a macro catalog item or a requirement
                                        // bail
                                        continueFlag = false;
                                }
                        }
                        if (continueFlag)
                        {
                                // not enabling if workspace is selected
                                boolean isWork = isWorkspaceSelected(pControl);
                                if (!isWork)
                                {
                                        // not enabling if a folder is selected
                                        boolean isFolder = isFolderSelected(pControl);
                                        if (!isFolder)
                                        {
                                                // not enabling if a closed project is selected
                                                String sFirstSelectedClosedProject = pControl.getFirstSelectedClosedProject();
                                                if (sFirstSelectedClosedProject == null || sFirstSelectedClosedProject.length() == 0)
                                                {
                                                        IProjectTreeItem[] pTreeItems = pControl.getSelected();
                                                        if (pTreeItems != null)
                                                        {
                                                                int count = pTreeItems.length;
                                                                if (count > 0)
                                                                {
                                                                        bEnable = true;
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                }
                else
                {
                        // in diagram area, not enabling if nothing selected on diagram
                        IProduct pProduct = ProductHelper.getProduct();
                        if (pProduct != null)
                        {
                                // need the diagram manager to get the current diagram
                                IProductDiagramManager pDiagramMgr = pProduct.getDiagramManager();
                                if (pDiagramMgr != null)
                                {
                                        // get the current diagram
                                        IDiagram pDiagram = pDiagramMgr.getCurrentDiagram();
                                        if (pDiagram != null)
                                        {
                                                // get the items selected on the diagram
                                                ETList<IPresentationElement> pSelected = pDiagram.getSelected();
                                                if (pSelected != null)
                                                {
                                                        // get what is selected
                                                        int count = pSelected.size();
                                                        if (count > 0)
                                                        {
                                                                bEnable = true;
                                                        }
                                                }
                                        }
                                }
                        }
                }
                 */
        return bEnable;
    }
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.products.ad.application.action.IPlugginAction#selectionChanged(org.netbeans.modules.uml.ui.products.ad.application.action.PluginAction, org.netbeans.modules.uml.ui.products.ad.application.selection.ISelection)
         */
    //	public void selectionChanged(PluginAction action, ISelection selection) {
    //
    //	}
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.products.ad.application.action.IPlugginAction#run()
         */
    public void run(ActionEvent e)
    {
        if (m_View != null)
        {
            String id = m_View.getId();
            if (id.equals("org.netbeans.modules.uml.view.projecttree"))
            {
                JProjectTree projTree = (JProjectTree)m_View;
                m_Context = projTree;
                IProjectTreeControl pControl = (IProjectTreeControl)projTree;
                setProjectTree(pControl);
                // set up the scope that the pattern participant instances should come from
                // the project tree because we are right clicking in the tree to start the
                // process
                m_PatternManager.setParticipantScope(DesignPatternParticipantScopeEnum.DPPARTICIPANT_SCOPE_PROJECTTREE);
                m_PatternManager.setProjectTree(pControl);
                onHandleButton(e, e.getActionCommand());
            }
            else if (id.equals("org.netbeans.modules.uml.view.drawingarea"))
            {
                IDrawingAreaControl drawControl = (IDrawingAreaControl)m_View;
                m_Context = drawControl;
                m_PatternManager.setParticipantScope(DesignPatternParticipantScopeEnum.DPPARTICIPANT_SCOPE_PRESENTATION);
                m_PatternManager.setProjectTree(null);
                onHandleButton(e, e.getActionCommand());
            }
        }
    }
    
    //	public boolean validate(ApplicationView view, IContributionItem item, IMenuManager pContextMenu)
    //	{
    //		boolean valid = false;
    //      m_View = view;
    //		if (view instanceof IProjectTreeControl)
    //		{
    //			IProjectTreeControl control = (IProjectTreeControl)view;
    //
    //			setProjectTree(control);
    //			// set up the scope that the pattern participant instances should come from
    //			// the project tree because we are right clicking in the tree to start the
    //			// process
    //			m_PatternManager.setParticipantScope(DesignPatternParticipantScopeEnum.DPPARTICIPANT_SCOPE_PROJECTTREE);
    //			m_PatternManager.setProjectTree(control);
    //			// which tree is being right clicked on
    //			// this addin is only responding to events fired by the project tree
    //			// because the catalog is handling the events fired by the design center tree
    //			// we check this because we don't want to do unnecessary processing
    //			String mgrName = ((JProjectTree)control).getProjectModel().getProjectTreeName();
    //			String applyStr = DesignPatternUtilities.translateString("IDS_POPUP_APPLY2");
    //			String label = item.getLabel();
    //			if (label != null && label.equals(applyStr))
    //			{
    //				// Since we changed the gui to be a wizard, we want the user to always be
    //				// able to apply a pattern, no restrictions on what is selected
    //				valid = true;
    //			}
    //			String promStr = DesignPatternUtilities.translateString("IDS_POPUP_PROMOTE");
    //			if (label != null && label.equals(promStr))
    //			{
    //				if (mgrName.equals(ProjectTreeResources.getString("ProjectTreeSwingModel.ProjectTree_Name")))
    //				{
    //					//	check to see that a pattern is the only thing selected
    //					boolean bSel = onlyCollaborationSelected(pContextMenu);
    //					if (bSel)
    //					{
    //						// If we are in the project tree (not the design center tree)
    //						// and the only thing that is selected is a pattern, then display the apply...
    //						// we want the users to be able to promote their patterns to the design center
    //						valid = true;
    //					}
    //				}
    //			}
    //		}
    //		else if (view instanceof IDrawingAreaControl)
    //		{
    //			String applyStr = DesignPatternUtilities.translateString("IDS_POPUP_APPLY2");
    //			String label = item.getLabel();
    //			if (label != null && label.equals(applyStr))
    //			{
    //				// Since we changed the gui to be a wizard, we want the user to always be
    //				// able to apply a pattern, no restrictions on what is selected
    //				valid = true;
    //			}
    //		}
    //		return valid;
    //	}
    
    /**
     * Determines whether or not a menu item should be displayed based on whether
     * or not a collaboration is selected (and is the only thing selected)
     *
     *
     * @param pContextMenu[in]	The menu that needs the menu item
     * @param bDisplay[out]		Whether or not a collaboration is selected
     *
     * @return HRESULT
     *
     */
    public static boolean onlyCollaborationSelected(IMenuManager pContextMenu)
    {
        boolean bDisplay = false;
        if (pContextMenu != null)
        {
            Object pDisp = pContextMenu.getContextObject();
            if (pDisp != null)
            {
                // do we come from a tree control
                if (pDisp instanceof IProjectTreeControl)
                {
                    IProjectTreeControl pTree = (IProjectTreeControl)pDisp;
                    IProjectTreeItem[] pTreeItems = pTree.getSelected();
                    if (pTreeItems != null)
                    {
                        // if only one thing is selected
                        int count = pTreeItems.length;
                        if (count == 1)
                        {
                            // get the tree item
                            IProjectTreeItem pTreeItem = pTreeItems[0];
                            if (pTreeItem != null)
                            {
                                // get the model element from the tree item
                                IElement pElement = pTreeItem.getModelElement();
                                if (pElement != null)
                                {
                                    // is the model element a collaboration
                                    if (pElement instanceof ICollaboration)
                                    {
                                        ICollaboration pCollab = (ICollaboration)pElement;
                                        // now do one more check to see if the collaboration has template parameters
                                        // because it is a pattern if it does
                                        ETList<IParameterableElement> pParams = pCollab.getTemplateParameters();
                                        if (pParams != null)
                                        {
                                            int pcount = pParams.size();
                                            if (pcount > 0)
                                            {
                                                bDisplay = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else
                {
                    // came from the diagram, so do the same check but check what is selected
                    // on the diagram
                    ETList <IPresentationElement> pSelecteds = null;//getSelectedOnCurrentDiagram();
                    if (pSelecteds != null)
                    {
                        int count = pSelecteds.size();
                        for (int x = 0; x < count; x++)
                        {
                            // get the tree item
                            IPresentationElement pPres = pSelecteds.get(x);
                            if (pPres != null)
                            {
                                // get the model element from the tree item
                                IElement pElement = pPres.getFirstSubject();
                                if (pElement != null)
                                {
                                    // is the model element a classifier
                                    // because if it is not, then we are not going to enable the menu
                                    // because as of right now the only thing that can have patterns
                                    // applied to it are classifiers
                                    if (pElement instanceof ICollaboration)
                                    {
                                        bDisplay = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return bDisplay;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // IDesignCenterSupport Methods
    
    /** save the design center addin */
    public void save()
    {
        try
        {
            m_Workspace.save();
        }
        catch(Exception e)
        {
            
        }
    }
}
