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


package org.netbeans.modules.uml.ui.products.ad.applicationcore;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.coreapplication.CoreProduct;
import org.netbeans.modules.uml.core.coreapplication.ICoreMessenger;
import org.netbeans.modules.uml.core.coreapplication.IDiagramCleanupManager;
import org.netbeans.modules.uml.core.eventframework.EventDispatchNameKeeper;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.roundtripframework.codegeneration.ICodeGeneration;
import org.netbeans.modules.uml.core.roundtripframework.codegeneration.IParseInformationCache;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.support.umlutils.InvalidArguments;
import org.netbeans.modules.uml.ui.controls.editcontrol.EditControlEventDispatcher;
import org.netbeans.modules.uml.ui.controls.editcontrol.IEditControlEventDispatcher;
import org.netbeans.modules.uml.ui.controls.filter.IProjectTreeFilterDialogEventDispatcher;
import org.netbeans.modules.uml.ui.controls.filter.ProjectTreeFilterDialogEventDispatcher;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventDispatcher;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeEventDispatcherImpl;
import org.netbeans.modules.uml.ui.products.ad.diagramlisteners.DiagramBackupCleaner;
import org.netbeans.modules.uml.ui.products.ad.diagramlisteners.IDiagramBackupCleaner;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.QuestionResponse;
import org.netbeans.modules.uml.ui.support.applicationmanager.AcceleratorManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.IAcceleratorManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductProjectManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProgressCtrl;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageDialogKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageResultKindEnum;
import org.netbeans.modules.uml.ui.support.diagramsupport.IPresentationFinder;
import org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.messaging.IProgressDialog;
import org.netbeans.modules.uml.ui.support.messaging.ProgressDialog;
import org.netbeans.modules.uml.ui.support.messaging.IMessenger;
import org.netbeans.modules.uml.ui.support.messaging.Messenger;
import org.netbeans.modules.uml.ui.support.messaging.ProgressDialogNoUI;
import org.netbeans.modules.uml.core.scm.ISCMEventDispatcher;
import org.netbeans.modules.uml.core.scm.ISCMIntegrator;
import org.netbeans.modules.uml.core.scm.SCMObjectCreator;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel;
import org.netbeans.modules.uml.ui.support.diagramsupport.DrawingAreaEventDispatcherImpl;
import org.netbeans.modules.uml.ui.support.diagramsupport.IDrawingAreaEventDispatcher;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingQuestionDialogImpl;
import org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditor;
import org.openide.ErrorManager;

/**
 * @author sumitabhk
 *
 */
public class ADProduct extends CoreProduct implements IADProduct
{
    /// This is the project tree
    private IProjectTreeControl m_ProjectTree = null; 
    
    // This is the project tree model. Can be used in place of m_ProjectTree
    private IProjectTreeModel mProjectTreeModel;
    
    /// This is the design center tree
    private IProjectTreeControl m_DesignCenterTree = null;
    
    /// This is the property editor
    private IPropertyEditor m_PropertyEditor = null;
    
    /// This is the messenger used to create our various error/question/prompt dialogs
    private IMessenger m_Messenger = null;
    
    /// The controller of all AddIns for this Product
    //	private AddInControllerEx m_AddInController = null;
    
    /// The Diagram Manager which opens/closes and creates new diagrams in a gui
    private IProductDiagramManager m_DiagramManager = null;
    
    /// The Project Manager which controls the current project in the gui
    private IProductProjectManager m_ProjectManager = null;
    
    /// The addin manager
    //	private IAddInManager m_AddInManager = null;
    
    /// The presentation finder that allows you to find presentation elements on closed diagrams
    private IPresentationFinder m_PresentationFinder = null;
    
    /// The current SCC interface
    private ISCMIntegrator m_SCMIntegrator = null;
    
    /// This guy listens to WSProject closes and cleans the diagram backup directory
    private IDiagramBackupCleaner m_DiagramBackupCleaner = null;
    
    /// Allows the users to display progress to the users through a ProgressCtrl
    private IProgressCtrl m_ProgressCtrl = null;
    
    /// Allows the users to display progress to the users through a Progress Dialog
    private IProgressDialog m_ProgressDialog = null;
    
    /// Watches for specific accerator keycodes and forwards them to interested windows
    private IAcceleratorManager m_AcceleratorMgr = null;
    
    /// Allows user to perform various funtions on the gui
    private IProxyUserInterface m_ProxyUserInterface = null;
    
    /// The diagram currently being serialized
    private IDiagram m_Diagram = null;
    
    //private Hashtable<String, Object> m_Diagrams = null;
    private Hashtable m_Diagrams = new Hashtable();
    
    /// This is the guy that handles various event sinks
    private ProductEventHandler m_ButtonHandler = null;
    
    /// The cache of parse information gathered from source files
    private IParseInformationCache m_ParseInfoCache = null;
    
    /// The Application's singleton Code Generation object
    private ICodeGeneration m_CodeGeneration = null;
    
    //holds ids to IDispatch* mappings
    private Hashtable/*<Integer, Object>*/ m_ControlMap = new Hashtable();
    
    /// The cross diagram clipboard string.  Used until TS gets copy/paste between diagrams working
    private String m_CrossDiagramClipboardString = "";
    
    /// The vba integrator
    private Object m_VBAIntegrator = null;
  
    
    /**
     *
     */
    public ADProduct()
    {
        super();
    }
    
    /**
     * Returns the ParseInfoCache used for storing/caching Parsed Source Code information
     *
     * @param pVal[out] the currently used IParseInformationCache object
     *
     * @return HRESULT
     */
    public IParseInformationCache getParseInformationCache()
    {
        return m_ParseInfoCache;
    }
    
    /**
     * Sets the ParseInfoCache used for storing/caching Parsed Source Code information
     *
     * @param newVal[in] the new IParseInformationCache object
     *
     * @return HRESULT
     */
    public void setParseInformationCache(IParseInformationCache value)
    {
        m_ParseInfoCache = value;
    }
    
    /**
     * gets the ADProduct's code generation object
     */
    public ICodeGeneration getCodeGeneration()
    {
        return m_CodeGeneration;
    }
    
    /**
     * Sets the code generation object for the ADProduct
     */
    public void setCodeGeneration(ICodeGeneration value)
    {
        m_CodeGeneration = value;
    }
    
    /**
     * Creates a new Application and initializes the addins if asked.
     * If bInitializeAddins is false then call IProduct::InitializeAddIns.
     *
     * @param bInitializeAddins[in]
     * @param app[out]
     */
    public IApplication initialize2(boolean bInitializeAddins)
    {
        if (m_Application == null)
        {
            createApplication();
            registerToDispatchers();
            
            // Retrieve the SCMIntegrator, resulting in its
            // creation
            ISCMIntegrator gator = getSCMIntegrator();
            if (bInitializeAddins)
            {
                //				initializeAddIns();
            }
        }
        return m_Application;
    }
    
    /**
     * Creates an IADApplication which represents the UML Metamodel for this application.
     */
    private void createApplication()
    {
        if (m_Application == null)
        {
            IApplication app = super.initialize();
        }
    }
    
    public IApplication initialize()
    {
        return initialize2(true);
    }
    
    /**
     *
     * Creates the IADProduct, specific to the UML design domain.
     *
     * @param app[out] The application
     *
     * @return HRESULT
     *
     */
    protected IApplication createProductApplication()
    {
        m_Application = new ADApplication();
        return m_Application;
    }
    
    /**
     * Returns the project tree that's in effect for the Describe product.
     *
     * @param pVal[out]
     */
    public IProjectTreeControl getProjectTree()
    {
        return m_ProjectTree;
    }
    
    /**
     * Sets the project tree that's in effect for the Describe product.
     *
     * @param newVal[in]
     */
    public void setProjectTree(IProjectTreeControl newVal)
    {
        if (m_ProjectTree != null)
        {
            m_ProjectTree = null;
        }
        m_ProjectTree = newVal;
    }
    
     /**
     * Returns the project tree model 
     *
     * @param pVal[out]
     */
    public IProjectTreeModel getProjectTreeModel()
    {
        return mProjectTreeModel;
    }
    
    /**
     * Sets the project tree model
     *
     * @param newVal[in]
     */
    public void setProjectTreeModel(IProjectTreeModel newTreeModel)
    {
        mProjectTreeModel = newTreeModel;
    }
    
    /**
     * Returns the design center tree that's in effect for the Describe product.
     *
     * @param pVal[out]
     */
    public IProjectTreeControl getDesignCenterTree()
    {
        return m_DesignCenterTree;
    }
    
    /**
     * Sets the design center tree that's in effect for the Describe product.
     *
     * @param newVal[in]
     */
    public void setDesignCenterTree(IProjectTreeControl newVal)
    {
        if (m_DesignCenterTree != null)
        {
            m_DesignCenterTree = null;
        }
        m_DesignCenterTree = newVal;
    }
    
    /**
     * Returns the property editor that's in effect for the Describe product.  This is the one in the
     * MFC GUI.
     *
     * @param pVal[out]
     */
    public IPropertyEditor getPropertyEditor()
    {
        return m_PropertyEditor;
    }
    
    /**
     * Sets the property editor that's in effect for the Describe product.  This is the one in the
     * MFC GUI.
     *
     * @param newVal[in]
     */
    public void setPropertyEditor(IPropertyEditor newVal)
    {
        m_PropertyEditor = newVal;
    }
    
    /**
     * Returns the Diagram Manager which opens/closes and creates new diagrams in a gui.
     *
     * @param pVal[out]
     */
    public IProductDiagramManager getDiagramManager()
    {
        return m_DiagramManager;
    }
    
    /**
     * Allows users to get/set the current project.
     *
     * @param newVal[in]
     */
    public void setDiagramManager(IProductDiagramManager newVal)
    {
        m_DiagramManager = newVal;
    }
    
    /**
     * Allows users to get/set the current project.
     *
     * @param pVal[out]
     */
    public IProductProjectManager getProjectManager()
    {
        return m_ProjectManager;
    }
    
    /**
     * Sets the Diagram Manager which opens/closes and creates new diagrams in a gui.
     *
     * @param newVal[in]
     */
    public void setProjectManager(IProductProjectManager newVal)
    {
        m_ProjectManager = newVal;
    }
    
    /**
     * Allows user to perform various funtions on the gui.
     *
     * @param pVal[out]
     */
    public IProxyUserInterface getProxyUserInterface()
    {
        return m_ProxyUserInterface;
    }
    
    /**
     * Allows user to perform various funtions on the gui.
     *
     * @param newVal[in]
     */
    public void setProxyUserInterface(IProxyUserInterface newVal)
    {
        m_ProxyUserInterface = newVal;
    }
    
    /**
     * Allows user to perform various funtions on the gui.
     *
     * @param pVal[out]
     */
    //	public IAddInManager getAddInManager()
    //	{
    //		if (m_AddInManager == null && m_AddInController != null)
    //		{
    //			Object obj = m_AddInController.getAddInManager();
    //			if (obj != null && obj instanceof IAddInManager)
    //			{
    //				m_AddInManager = (IAddInManager)obj;
    //			}
    //		}
    //		return m_AddInManager;
    //	}
    
    /**
     * Allows user to perform various funtions on the gui.
     *
     * @param newVal[in]
     */
    //	public void setAddInManager(IAddInManager newVal)
    //	{
    //		m_AddInManager = newVal;
    //	}
    
    /**
     * Adds a drawing area to our list of open drawings.
     */
    public void addDiagram(IDiagram pDiagram)
    {
        if (pDiagram != null)
        {
            String fileName = pDiagram.getFilename();
            if (fileName != null && fileName.length() > 0)
            {        
                removeDiagram(pDiagram);
                String id = getDiagramFileID(fileName);
                IDiagram existingDia = getDiagram(id);
                if (existingDia == null)
                {
                    m_Diagrams.put(id, pDiagram);
                }
            }
        }
    }
    
    
    // utility to normalize diagram file name which is used as a key
    private String getDiagramFileID(String fileName)
    {
        String id = "";
        File file = new File(fileName);
        try
        {
            id = file.getCanonicalPath();
        }
        catch (IOException e)
        {
            ErrorManager.getDefault().notify(e);
        }
        return id;
    }
    
    
    /**
     * Removes a drawing area to our list of open drawings.
     */
    public void removeDiagram(IDiagram pDiagram)
    {
        if (pDiagram != null)
        {
            String fileName = pDiagram.getFilename();
            if (fileName != null && fileName.length() > 0)
            {
                String id = getDiagramFileID(fileName);
                Object obj = m_Diagrams.get(id);
                if (obj != null)
                {
                    m_Diagrams.remove(id);
                }
            }
        }
    }
    
    /**
     * Returns an open drawing area based on the filename.
     *
     * @param pDiagram[out]
     */
    public IDiagram getDiagram(String sFilename)
    {
        IDiagram retDia = null;
        if (sFilename != null && sFilename.length() > 0)
        {
            Object obj = m_Diagrams.get(getDiagramFileID(sFilename));
            if (obj != null && obj instanceof IDiagram)
            {
                retDia = (IDiagram)obj;
            }
        }
        return retDia;
    }
    
    /**
     * Returns all open drawing ares in a safe array.
     *
     * @param pDiagrams[out]
     */
    public ETList<IDiagram> getAllDrawingAreas()
    {
        ETList<IDiagram> retObj = null;
        if (m_Diagrams != null && m_Diagrams.size() > 0)
        {
            retObj = new ETArrayList<IDiagram>();
            Enumeration iter = m_Diagrams.elements();
            while (iter.hasMoreElements())
            {
                Object obj = iter.nextElement();
                if (obj != null && obj instanceof IDiagram)
                {
                    retObj.add((IDiagram)obj);
                }
            }
        }
        return retObj;
    }
    
        /* (non-Javadoc)
         * @see org.netbeans.modules.uml.ui.support.applicationmanager.IProduct#displayAddInDialog(int)
         */
    public void displayAddInDialog(int parentHwnd)
    {
        // TODO Auto-generated method stub
    }
    
    /**
     * Gets an addin based on the clsid.
     *
     * @param pAddIN[out]
     */
    //	public IAddIn getAddIn(String progID)
    //	{
    //		IAddIn retObj = null;
    //		IAddInManager pAddinMan = getAddInManager();
    //		if (pAddinMan != null)
    //		{
    //			retObj = pAddinMan.retrieveAddIn(progID);
    //		}
    //		return retObj;
    //	}
    
    /**
     * Establish additional EventDispatchers by adding them to the internal
     * EventDispatchController.
     */
    protected void establishDispatchers()
    {
        super.establishDispatchers();
        if (m_DispatchController != null)
        {
            // Add the DrawingAreaDispatcher
            IDrawingAreaEventDispatcher pDrawDisp = new DrawingAreaEventDispatcherImpl();
            m_DispatchController.addDispatcher(EventDispatchNameKeeper.drawingAreaName(), pDrawDisp);
            
            // Add the ProjectTree dispatcher
            IProjectTreeEventDispatcher pProjectDisp = new ProjectTreeEventDispatcherImpl();
            m_DispatchController.addDispatcher(EventDispatchNameKeeper.projectTreeName(), pProjectDisp);
            
            // Add the EditControl dispatcher
            IEditControlEventDispatcher pEditDisp = new EditControlEventDispatcher();
            m_DispatchController.addDispatcher(EventDispatchNameKeeper.editCtrlName(), pEditDisp);
            
            // Add the Project Tree Filter Dialog dispatcher
            IProjectTreeFilterDialogEventDispatcher pFilterDisp = new ProjectTreeFilterDialogEventDispatcher();
            m_DispatchController.addDispatcher(EventDispatchNameKeeper.projectTreeFilterDialogName(), pFilterDisp);
            
            // Add the VBA dispatcher
            //IVBAIntegrationEventDispatcher pVBADisp = new
            
            // Add the SCM Events dispatch
            ISCMEventDispatcher pSCMDisp = (ISCMEventDispatcher)SCMObjectCreator.getInstanceFromRegistry("uml/scm/SCMEventDispatcher");  //new SCMEventDispatcher();
            if(pSCMDisp != null)
            {
                m_DispatchController.addDispatcher(EventDispatchNameKeeper.SCM(), pSCMDisp);
            }
            
            // create the Accelerator Manager
            // Put the accelerator manager so that users can register their accelerator tables
            if (m_AcceleratorMgr == null)
            {
                m_AcceleratorMgr = new AcceleratorManager();
            }
        }
    }
    
    /**
     * Creates the listeners for the diagrams.
     */
    private void establishDiagramListeners()
    {
        // Create the listener that cleans up our diagrams backup area
        m_DiagramBackupCleaner = new DiagramBackupCleaner();
    }
    
    /**
     *
     * Revokes all the EventDispatchers installed by this product.
     *
     * @return
     */
    private void revokeDispatchers()
    {
        if (m_DispatchController != null)
        {
            revokeDiagramListeners();
            //super.revokeDispatchers();
            
            m_DispatchController.removeDispatcher(EventDispatchNameKeeper.drawingAreaName());
            m_DispatchController.removeDispatcher(EventDispatchNameKeeper.projectTreeName());
            m_DispatchController.removeDispatcher(EventDispatchNameKeeper.editCtrlName());
            m_DispatchController.removeDispatcher(EventDispatchNameKeeper.projectTreeFilterDialogName());
            m_DispatchController.removeDispatcher(EventDispatchNameKeeper.SCM());
            m_DispatchController.removeDispatcher(EventDispatchNameKeeper.vBA());
        }
    }
    
    /**
     * Creates the listeners for the diagrams.
     */
    private void revokeDiagramListeners()
    {
        if (m_DispatchController != null)
        {
            if (m_DiagramBackupCleaner != null)
            {
                m_DiagramBackupCleaner.revoke();
                m_DiagramBackupCleaner = null;
            }
        }
    }
    
    /**
     *
     * Deinitalizes all loaded addins.
     *
     * @return HRESULT
     *
     */
    //	public void deInitAddIns()
    //	{
    //		m_AddInController.unloadAddIns();
    //	}
    
    /**
     * Returns the IApplication that this product wraps.
     *
     * @param pVal[out] The returned IApplication
     */
    public IApplication getApplication()
    {
        IApplication retVal = null;
        if (m_Application != null)
        {
            if (m_Application instanceof IADApplication)
            {
                IADApplication adApp = (IADApplication)m_Application;
                retVal = adApp.getApplication();
            }
        }
        return retVal;
    }
    
    /**
     * Sets the IApplication that this product wraps.
     *
     * @param newVal[in] The new IApplication to wrap
     */
    public void setApplication( IApplication newVal)
    {
        if (newVal != null)
        {
            if (newVal instanceof IADApplication)
            {
                m_Application = (IADApplication)newVal;
            }
        }
    }
    
    /**
     * Returns a shared IMessenger object for the product.  Using a shared messenger allows
     * the application to be silenced during long operations.
     *
     * @param pVal[out] The returned messenger.
     */
    public IMessenger getMessenger()
    {
        if (m_Messenger == null)
        {
            m_Messenger = new Messenger();
        }
        return m_Messenger;
    }
    
    
    /**
     * Returns a shared IMessenger object for the product.  Using a shared messenger allows
     * the application to be silenced during long operations.
     *
     * @param pVal[out] The returned messenger.
     */
    public ICoreMessenger getCoreMessenger()
    {
        if (m_Messenger == null)
        {
            m_Messenger = new Messenger();
        }
        return m_Messenger;
    }
    
    /**
     *
     * Initializes all addins found in the registry.
     *
     * @return HRESULT
     *
     */
    public void initializeAddIns()
    {
        //		if (m_AddInController == null)
        //		{
        //			m_AddInController = new AddInControllerEx();
        //		}
        //
        //        if(m_AddInController != null)
        //        {
        //        	//m_AddInController.loadAddins();
        //		    m_AddInController.loadStartUps(this);
        //        }
    }
    
    /**
     * The current SCC interface.
     *
     * @param pVal[out]
     */
    public ISCMIntegrator getSCMIntegrator()
    {
        if (m_SCMIntegrator == null)
        {
            m_SCMIntegrator = (ISCMIntegrator)SCMObjectCreator.getInstanceFromRegistry("uml/scm/SCMIntegrator");//new SCMIntegrator();
        }
        return m_SCMIntegrator;
    }
    
    /**
     * Returns the Progress Controller
     *
     * @param pVal[out]
     */
    public IProgressCtrl getProgressCtrl()
    {
        return m_ProgressCtrl;
    }
    
    /**
     * Returns the Progress Dialog
     *
     * @param pVal[out]
     */
    public IProgressDialog getProgressDialog()
    {
        if (m_ProgressDialog == null)
        {
            if( isGUIProduct() )
            {
                IProxyUserInterface proxyUI = ProductHelper.getProxyUserInterface();
                if( proxyUI != null )
                {
                    Frame parent = proxyUI.getWindowHandle();
                    if ( parent != null )
                    {
                        // In Java we never display a modal progress dlg.  (Kevin).
                        m_ProgressDialog = (parent != null) ? new ProgressDialog(parent,"Progress", false): new ProgressDialog();
                    }
                }
            }
            
            if( null == m_ProgressDialog )
            {
                m_ProgressDialog = new ProgressDialogNoUI();
            }
        }
        return m_ProgressDialog;
    }
    
    /**
     * Sets the Progress controller.
     *
     * @param newVal[in]
     */
    public void setProgressCtrl(IProgressCtrl newVal)
    {
        m_ProgressCtrl = newVal;
    }
    
    /**
     * Returns the Accelerator Manager so a window can register itself.
     *
     * @param pManager[out]
     */
    public IAcceleratorManager getAcceleratorManager()
    {
        return m_AcceleratorMgr;
    }
    
    /**
     * Sets the Accelerator Manager for the application.
     *
     * @param pManager[in]
     */
    public void setAcceleratorManager(IAcceleratorManager newVal)
    {
        m_AcceleratorMgr = newVal;
    }
    
    /**
     * Returns the Accelerator Manager so a window can register itself.
     *
     * @param pDiagram[out]
     */
    public IDiagram getSerializingDiagram()
    {
        return m_Diagram;
    }
    
    /**
     * Sets the Accelerator Manager for the application.
     *
     * @param pDiagram[in]
     */
    public void setSerializingDiagram(IDiagram newVal)
    {
        m_Diagram = newVal;
    }
    
    /**
     *	AddControl(UINT ID, IDispatch* pUnk)
     *
     *	Adds key=ID and value=IDispatch* to map for future retrieval.
     *
     * @param pDisp[in]
     *
     */
    public void addControl(int nID, Object pControl)
    {
        if (nID >= 0)
        {
            //Check to see if this ID already is in the map
            Object obj = getControl(nID);
            if (obj == null)
            {
                m_ControlMap.put(new Integer(nID), pControl);
            }
        }
    }
    
        /*
         *	GetControl(UINT ID, IDispatch ** ppUnk).
         *
         *	Queries for the IDispatch pointer to a control given an Id.
         *
         * @param ppDisp[out]
         */
    public Object getControl(int nID)
    {
        Object retObj = null;
        retObj = m_ControlMap.get(new Integer(nID));
        return retObj;
    }
    
    /**
     *	RemoveControl(UINT ID)
     *
     *	Removes a key=ID and value=IDispatch* from the a map.
     *
     */
    public void removeControl(int nID)
    {
        Integer intVal = new Integer(nID);
        Object obj = m_ControlMap.get(intVal);
        if (obj != null)
        {
            m_ControlMap.remove(intVal);
        }
    }
    
    /**
     * Until TS gets copy/paste between diagrams working, this is the text string that tells what the
     * user last copied.  It's produced by CDragAndDropSupport in MFCSupport.dll.
     *
     * @param sClipString[out,retval] The clip string from another diagram
     */
    public String getCrossDiagramClipboard()
    {
        return m_CrossDiagramClipboardString;
    }
    
    /**
     * Until TS gets copy/paste between diagrams working, this is the text string that tells what the
     * user last copied.  It's produced by CDragAndDropSupport in MFCSupport.dll.
     *
     * @param sClipString [in] The clip string from another diagram
     */
    public void setCrossDiagramClipboard(String value)
    {
        m_CrossDiagramClipboardString = value;
    }
    
    /**
     * Returns the design center tree that's in effect for the Describe product.
     *
     * @param pVal[out] The VBA integrator
     */
    public Object getVBAIntegrator()
    {
        return m_VBAIntegrator;
    }
    
    /**
     * Sets the design center tree that's in effect for the Describe product.
     *
     * @param newVal[in] The VBA integrator
     */
    public void setVBAIntegrator(Object value)
    {
        if (m_VBAIntegrator != null)
        {
            m_VBAIntegrator = null;
        }
        m_VBAIntegrator = value;
    }
    
    /**
     * Register to all our various dispatchers.
     */
    private void registerToDispatchers()
    {
        if (m_ButtonHandler == null)
        {
            m_ButtonHandler = new ProductEventHandler();
            m_ButtonHandler.setProductToAdvise(this);
            
            DispatchHelper helper = new DispatchHelper();
            helper.registerElementDisposalEvents(m_ButtonHandler);
            helper.registerForPreferenceManagerEvents(m_ButtonHandler);
            helper.registerForProjectEvents(m_ButtonHandler);
        }
    }
    
    /**
     * Revokes from the various dispatchers.
     */
    private void revokeFromDispatchers()
    {
        if (m_ButtonHandler != null)
        {
            try
            {
                DispatchHelper helper = new DispatchHelper();
                helper.revokeElementDisposalEventsSink(m_ButtonHandler);
                helper.revokePreferenceManagerSink(m_ButtonHandler);
                helper.revokeProjectSink(m_ButtonHandler);
            }
            catch (InvalidArguments e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Deletes these elements from closed diagrams.
     *
     * @param pElements[in]
     */
    public void onDisposedElements(ETList<IVersionableElement> pElements)
    {
        IProxyDiagramManager pDiaMan = ProxyDiagramManager.instance();
        pDiaMan.markPresentationTargetsAsDeleted(pElements);
    }
    
    private ETList<IPropertyElement> getColors(ETList<IPropertyElement> allElems, boolean reload)
    {
        //Have To Do
        return null;
    }
    
    private ETList<IPropertyElement> getFonts(ETList<IPropertyElement> allElems, boolean reload)
    {
        //Have To Do
        return null;
    }
    
    /**
     * Handler for IPreferenceManagerEventsSink event informing us that a preference has changed.
     *
     * @param pVal[out] the currently used IParseInformationCache object
     *
     * @return HRESULT
     */
    public void onPreferencesChange( ETList<IPropertyElement> pElements)
    {
        if (pElements != null && getNumNonReadonlyDiagrams() > 0)
        {
            // Create a temporary list because we may modify it.
            ETList<IPropertyElement> pTempList = pElements;
            
            // See if any of these elements deal with fonts or colors.  If so then ask the user
            // if fonts and colors should be applied to the open diagrams.
            ETList<IPropertyElement> colorProps = getColors(pElements, false);
            ETList<IPropertyElement> fontProps = getFonts(pElements, false);
            
            int colorCount = 0;
            int fontCount = 0;
            if (colorProps != null)
            {
                colorCount = colorProps.size();
            }
            if (fontProps != null)
            {
                fontCount = fontProps.size();
            }
            
            if (colorCount > 0 || fontCount > 0)
            {
                // Ask the user if changing colors is ok
                String message = "";//loadString(IDS_CHANGEOPENDIAGRAMS);
                String title = "";//loadString(IDS_CHANGEOPENDIAGRAMSTITLE);
                IQuestionDialog pDialog = new SwingQuestionDialogImpl();
                QuestionResponse result = pDialog.displaySimpleQuestionDialogWithCheckbox(MessageDialogKindEnum.SQDK_YESNO,
                        MessageIconKindEnum.EDIK_ICONWARNING,
                        message, "", title,
                        MessageResultKindEnum.SQDRK_RESULT_YES,
                        true);
                if (result.getResult() == MessageResultKindEnum.SQDRK_RESULT_NO)
                {
                    // Remove the colors and fonts
                    colorProps = null;
                    fontProps = null;
                    colorProps = getColors(pElements, true);
                    fontProps = getFonts(pElements, true);
                }
            }
            
            // Now go over the diagrams and tell them about the changes
            if (m_Diagrams != null)
            {
                Enumeration iter = m_Diagrams.elements();
                while (iter.hasMoreElements())
                {
                    IDiagram pDiagram = (IDiagram)iter.nextElement();
                    boolean readOnly = false;
                    readOnly = pDiagram.getReadOnly();
                    
                    if (!readOnly)
                    {
                        // TODO: meteora
                        // Notify the diagram of the changes
//                        IDrawingAreaControl pDrawingAreaControl = null;
//                        if (pDiagram instanceof IUIDiagram)
//                        {
//                            pDrawingAreaControl = ((IUIDiagram)pDiagram).getDrawingArea();
//                        }
//                        if (pDrawingAreaControl != null)
//                        {
//                            IPropertyElement[] elements = new IPropertyElement[pElements.size()];
//                            for (int i = 0; i < pElements.size(); i++)
//                            {
//                                elements[i] = pElements.get(i);
//                            }
//                            
//                            pDrawingAreaControl.preferencesChanged(elements);
//                        }
                    }
                }
            }
        }
    }
    
    /**
     * Returns the number of diagrams that aren't readonly
     */
    private int getNumNonReadonlyDiagrams()
    {
        int numNonReadonly = 0;
        if (m_Diagrams != null)
        {
            Enumeration iter = m_Diagrams.elements();
            while (iter.hasMoreElements())
            {
                IDiagram dia = (IDiagram)iter.nextElement();
                boolean readOnly = dia.getReadOnly();
                if (!readOnly)
                {
                    numNonReadonly++;
                }
            }
        }
        return numNonReadonly;
    }
    
    /**
     *
     * This method will always return true in the out parameter, indicating
     * that this product is part of a larger GUI shell.
     *
     * @param pVal[out] true
     *
     * @return S_OK
     *
     */
    public boolean isGUIProduct()
    {
        return true;
    }
    
    /**
     * Gets the Products DiagramCleanupManager.
     *
     * @param pVal[out] The DiagramCleanupManager
     *
     * @return HRESULT
     */
    public IDiagramCleanupManager getDiagramCleanupManager()
    {
        return ProxyDiagramManager.instance();
    }
    
//    public IPresentationTypesMgr getPresentationTypesMgr()
//    {
//        if (m_PresentationTypesMgr == null)
//        {
//            ICreationFactory pCreationFactory = FactoryRetriever.instance().getCreationFactory();
//            if (pCreationFactory != null)
//            {
//                Object value = pCreationFactory.retrieveEmptyMetaType("PresentationTypes", "PresentationTypesMgr", null);
//                
//                if (value instanceof IPresentationTypesMgr)
//                {
//                    m_PresentationTypesMgr = (IPresentationTypesMgr)value;
//                }
//            }
//        }
//        
//        return m_PresentationTypesMgr;
//    }
    
//    public IPresentationResourceMgr getPresentationResourceMgr()
//    {
//        if (m_PresentationResourceMgr == null)
//        {
//            ICreationFactory pCreationFactory = FactoryRetriever.instance().getCreationFactory();
//            
//            if (pCreationFactory != null)
//            {
//                Object value = pCreationFactory.retrieveEmptyMetaType("PresentationTypes", "PresentationResourceMgr", null);
//                
//                if (value instanceof IPresentationResourceMgr)
//                {
//                    m_PresentationResourceMgr = (IPresentationResourceMgr)value;
//                }
//            }
//        }
//        return m_PresentationResourceMgr;
//    }
}



