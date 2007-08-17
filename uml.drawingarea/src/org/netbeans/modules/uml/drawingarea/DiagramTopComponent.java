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

package org.netbeans.modules.uml.drawingarea;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.netbeans.modules.uml.common.ui.SaveNotifierYesNo;
import org.openide.NotifyDescriptor;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;
import org.openide.cookies.PrintCookie;
import org.openide.util.NbBundle;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.drawingarea.IUIDiagram;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaSelectionEventsSink;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IToolTipData;
import org.netbeans.modules.uml.ui.swing.drawingarea.ICreateNodeContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDraggingNodeContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaAddNodeEventsSink;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaDropContext;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaEventsSink;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaPropertyKind;
import org.netbeans.modules.uml.core.support.umlsupport.FileExtensions;
import org.netbeans.modules.uml.drawingarea.dataobject.DiagramDataObject;
import org.netbeans.modules.uml.project.ui.nodes.AbstractModelElementNode;
import org.netbeans.modules.uml.project.ui.nodes.UMLModelElementNode;
import org.netbeans.modules.uml.palette.PaletteSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node.Cookie;
import org.openide.windows.CloneableTopComponent;


/**
 *
 * @author Trey Spiva
 */
public class DiagramTopComponent extends CloneableTopComponent
        implements IDrawingAreaSelectionEventsSink,
        IDrawingAreaEventsSink, IDrawingAreaAddNodeEventsSink
{
    transient private ADDrawingAreaControl mControl = null;
    transient private String m_PreferredID = ""; // NOI18N
    transient private LocalUMLModelElementNode node = null;
    
    transient private DiagramDataObject diagramDO;
    
    private boolean bCancelSaveDialog = false;
    private boolean projectClosing = false;
    transient private static boolean overviewOpen = false;
    transient private boolean listenersRegistered = false;
    transient private boolean isHidden = true;
    
    private final static String SPACE_STAR = " *";
    
    DispatchHelper helper = new DispatchHelper();
    private DiagramChangeListener listener = new DiagramChangeListener();
    
    private PaletteController paletteContrl;
    private PaletteSupport paletteSupport;
 
    /**
     * Creates a new drawing area TopComponent.  The drawing area component is
     * initialized with the information in the file.
     */
    public DiagramTopComponent(String filename)
    {
        super();
        initializeUI();
        loadDrawingArea(filename);
        initialize();
    }
    
    public DiagramTopComponent(INamespace owener, String name, int kind)
    {
        super();
        initializeUI();
        loadDrawingArea(owener, name, kind);
        initialize();
    }
    
    
    private void initialize()
    {
        paletteContrl = getAssociatedPalette();
        
        // cvc - CR 6300399
        // use the diagrams fully qualified namespace as the tooltip for the
        // drawing area tab at the top
        Runnable r = new Runnable()
        {
            public void run()
            {
                setToolTipText(getFullNamespace());
            }
        };
        SwingUtilities.invokeLater(r);
        
        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke("shift F10"),
                "SHOW_CONTEXT_MENU");
        getActionMap().put("SHOW_CONTEXT_MENU", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e)
            {
                mControl.showAccessiblePopupMenu();
            }
        });
        
    }
    
    
    private DiagramDataObject getDiagramDO()
    {  
        if (diagramDO == null)
        {
            String file = getDrawingAreaControl().getFilename();   
            String etld = file;
            String etlp = file;
            try
            {
                if (file != null && !file.equals(""))
                {
                    int index = file.lastIndexOf(".");
                    if (index > -1)
                    {
                        etlp = file.substring(0, index) +
                                FileExtensions.DIAGRAM_PRESENTATION_EXT;
                        etld = file.substring(0, index) +
                                FileExtensions.DIAGRAM_LAYOUT_EXT;
                    }
                    
                    File etlpF = new File(etlp);
                    File etldF = new File(etld);
                    
                    
                    if (!etlpF.exists())
                        etlpF.createNewFile();
                    if (!etldF.exists())
                        etldF.createNewFile();
                    
                    FileObject etlpFO = FileUtil.toFileObject(etlpF);

                    if (etlpFO != null)
                        diagramDO = (DiagramDataObject) DataObject.find(etlpFO);
                }
            }   
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        
        return diagramDO;
    }
    
    private String getFullNamespace()
    {
        if (mControl == null)
            return ""; // NOI18N
        
        StringBuffer fullNameSpace = new StringBuffer();
        IProject project = mControl.getNamespace().getProject();
        IElement owner = mControl.getDiagram().getOwner();
        
        fullNameSpace.append(mControl.getProxyDiagram().toString());
        
        while (owner != null && !owner.toString().equals(project.toString()))
        {
            fullNameSpace.insert(0, owner + "::"); // NOI18N
            owner = owner.getOwner();
        }
        
        fullNameSpace.insert(0, project + "::"); // NOI18N
        
        return fullNameSpace.toString();
    }
    
    
    public void loadDrawingArea(INamespace namespace, String name, int kind)
    {
        ADDrawingAreaControl retVal = null;
        // Fixed issue 96474.
        // Modified to call addControl() before calling mControl.initializeNewDiagram(namespace, name, kind).
        // This is because mControl needs to be set to this DiagramTopComponent object
        // before it is referred in a later code to avoid NPE.
        
        //        mControl = createNewDiagram(namespace, name, kind);
        //        setDiagramProperties(mControl);
        //        addControl();
        try
        {
            mControl = new ADDrawingAreaControl();
            addControl();
            mControl.addPropertyChangeListener(listener);
            mControl.initializeNewDiagram(namespace, name, kind);
            setDiagramProperties(mControl);
        }
        
        catch(Throwable t)
        {
            t.printStackTrace();
        }
        
    }
    
    /**
     * @param filename
     */
    public void loadDrawingArea(String filename)
    {
        if (filename != null)
        {
            ICoreProduct coreProduct = ProductRetriever.retrieveProduct();
            IDiagram dia = null;
            IProduct product = null;
            
            if (coreProduct instanceof IProduct)
            {
                product = (IProduct)coreProduct;
                dia = product.getDiagram(filename);
            }
            
            if (dia != null)
            {
                if (dia instanceof IUIDiagram)
                {
                    IUIDiagram uiDiagram = (IUIDiagram)dia;
                    ADDrawingAreaControl ctrl =
                            (ADDrawingAreaControl)uiDiagram.getDrawingArea();
                    
                    if(ctrl != null)
                    {
                        setDiagramProperties(ctrl);
                        mControl = ctrl;
                    }
                    
                    else
                    {
                        if (product != null)
                            product.removeDiagram(dia);
                        
                        mControl = createNewDiagram(filename);
                        setDiagramProperties(mControl);
                    }
                }
            }
            
            else
            {
                mControl = createNewDiagram(filename);
                setDiagramProperties(mControl);
            }
        }
        
        else
            mControl = createNewDiagram(null);
        
        addControl();
    }
    
    
    private void addControl()
    {
        if (mControl != null)
        {
            add(mControl, BorderLayout.CENTER);
            
            if (getDiagramDO() != null)
                getDiagramDO().addPropertyChangeListener(listener);
        }
    }
    
    
    // the presence of save cookie in activated nodes enables/disables "Save" button
    private void addSaveCookie()
    {
        Node[] nodes = getActivatedNodes();
        if (nodes != null &&
                nodes.length > 0 &&
                nodes[0] instanceof LocalUMLModelElementNode)
        {
            ((LocalUMLModelElementNode)nodes[0]).addSaveCookie();
        }
        if (node != null)
            node.addSaveCookie();
        
    }
    
    private void removeSaveCookie()
    {
        Node[] nodes = getActivatedNodes();
        for (int i=0; i<nodes.length; i++)
        {
            if (nodes[i] instanceof LocalUMLModelElementNode)
                ((LocalUMLModelElementNode)nodes[i]).removeSaveCookie();
        }
        if (node != null)
            node.removeSaveCookie();
    }
    
    
    public int getPersistenceType()
    {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    public IDiagram getAssociatedDiagram()
    {
        IDiagram retVal = null;
        IDrawingAreaControl control = getDrawingAreaControl();
        
        if (control != null)
            retVal = control.getDiagram();
        
        return retVal;
    }
    
    //**************************************************
    // Helper Methods
    //**************************************************
    
    protected void registerListeners()
    {
        if (isListenersRegistered())
            return;
        
        helper.registerDrawingAreaSelectionEvents(this);
        helper.registerDrawingAreaEvents(this);
        helper.registerDrawingAreaAddNodeEvents(this);
        
        setListenersRegistered(true);
    }
    
    
    protected void unregisterListeners()
    {
        // cvc - so much registering/revoking, might be best as a member var
        // DispatchHelper helper = new DispatchHelper();
        helper.revokeDrawingAreaSelectionSink(this);
        helper.revokeDrawingAreaSink(this);
        helper.revokeDrawingAreaAddNodeSink(this);
        setListenersRegistered(false);
    }
    
    
    protected ADDrawingAreaControl createNewDiagram(
            INamespace owner, String name, int kind)
    {
        ADDrawingAreaControl retVal = null;
        try
        {
            retVal = new ADDrawingAreaControl();
            retVal.addPropertyChangeListener(listener);
            retVal.initializeNewDiagram(owner, name, kind);
        }
        
        catch(Throwable t)
        {
            t.printStackTrace();
        }
        
        return retVal;
    }
    
    
    /**
     * @param filename
     * @return
     */
    private ADDrawingAreaControl createNewDiagram(String filename)
    {
        ADDrawingAreaControl retVal = null;
        
        try
        {
            retVal = new ADDrawingAreaControl();
            retVal.addPropertyChangeListener(listener);
            mControl = retVal;
            retVal.load(filename);
        }
        
        catch(Throwable t)
        {
            t.printStackTrace();
        }
        
        return retVal;
    }
    
    /**
     * Sets the top components properties based on a drawing area control.
     *
     * @param ctrl
     */
    protected void setDiagramProperties(final IDrawingAreaControl ctrl)
    {
        if (ctrl != null)
        {
            CommonResourceManager resource = CommonResourceManager.instance();
            final String details =
                    resource.getIconDetailsForElementType(ctrl.getDiagramKind2());
            
            // following methods can only be called in the AWT Event Dispatch
            // Thread, therefore, I am wrapping the call in a invokeLater.
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    setDisplayName(ctrl.getNameWithAlias());
                    setName(ctrl.getName());
                    setIcon(Utilities.loadImage( details, true ));
                }
            });
        }
    }
    
    protected void initializeUI()
    {
        setLayout(new BorderLayout());
    }
    
    public boolean canClose()
    {
        // 106147, no need to popup save dialog in case the diagram is to be deleted
        if (!getDrawingAreaControl().getIsDirty() || 
             getDrawingAreaControl().getGraphWindow() == null)
            return true;
        
        DiagramDataObject obj = getDiagramDO();
        if (obj != null)
        {
            // Fixes Issue 96133.  The problem is that NetBeans is calling
            // canClose more than one time (I do not know why).  However if the
            // user said do not save the first time, the diagram will still be
            // marked as dirty.  So we will try to save again the second time.
            // Therefore the dialog will be displayed again.
            //
            // However when the user says to not save, we remove the SaveCookie
            // from the DataObject.  So, check if the save cookie is present.
            // If the save cookie is not present, we can not save anyway.  So,
            // let the user close the diagram.
            if(obj.getCookie(SaveCookie.class) == null)
                return true;
        }
        
        //Jyothi: prompt to save the diagram before closing
        
        IDiagram diagToClose = getDrawingAreaControl().getDiagram();
        return save(diagToClose, false, null);
    }
    
    protected void componentClosed()
    {
        IProductDiagramManager pDiaMgr =
                ProductHelper.getProductDiagramManager();
        
        if (pDiaMgr != null)
        {
            pDiaMgr.closeDiagram(
                    getDrawingAreaControl().getDiagram().getFilename());
        }
        
        super.componentClosed();
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                selectedElements(null, null, null);
		remove(mControl);
		mControl = null;
		if (getDiagramDO() != null)
		    getDiagramDO().removePropertyChangeListener(listener);
            }
        });
        
        removeBasicActionCallbacks();
        isHidden = true;
        unregisterListeners();
	detachAssociatedPalette();
    }
    
    
    /**
     *
     * @param diag the diagram to save
     * @param autoSave if true, then save without prompting the user
     *					for confirmation
     * @param msg custom message for dialog; if null, default message is used
     * @return true means ok to close
     */
    protected boolean save(IDiagram diag, boolean autoSave, String msg)
    {
        boolean safeToClose = true;
        
        // no need to continue if diagram is null or isn't dirty
        if (diag == null || !diag.getIsDirty())
            return safeToClose;
        
        String name = diag.getFilename();
        int saveAction = RESULT_CANCEL;
        
        // prompt user to confirm saving the diagram
        if (autoSave)
            saveAction = RESULT_YES;
        
        // otherwise, save without permission
        else
            saveAction = saveDiagram();
        
        switch (saveAction)
        {
        case RESULT_YES:
            SaveCookie cookie = (SaveCookie) getDiagramDO().getCookie(SaveCookie.class);
            try
            {
                if (cookie != null)
                    cookie.save();
            }
            catch (IOException e)
            {
                ErrorManager.getDefault().notify(e);
            }
            break;
            
        case RESULT_NO:
            DiagramDataObject obj = getDiagramDO();
            if (obj != null)
            {
                obj.removeSaveCookie();
                obj.setModified(false);
            }
            removeSaveCookie();
            break;
            
        case RESULT_CANCEL:
            safeToClose = false;
            break;
        }
        
        return safeToClose;
    }
    
    private static final int RESULT_CANCEL = 0;
    private static final int RESULT_NO = 1;
    private static final int RESULT_YES = 2;
    
    
    private int saveDiagram()
    {
        //kris richards - pref PromptToSaveDiagram removed - set to PSK_YES
        String prefVal = "PSK_YES";
        

        
        String title = NbBundle.getMessage(DiagramTopComponent.class,
                "LBL_DIALOG_TITLE_SaveDiagram"); // NOI18N
        
        String objType = NbBundle.getMessage(
                DiagramTopComponent.class,
                "LBL_DIALOG_MSG_Diagram",  // NOI18N
                getName());
        
        int result = RESULT_CANCEL;
        
        Object response = SaveNotifierYesNo.getDefault().displayNotifier(
                title, // NOI18N
                objType, // NOI18N
                getFullNamespace());
        
        if (response == SaveNotifierYesNo.SAVE_ALWAYS_OPTION)
        {
            result = RESULT_YES;
        }
        
        else if (response == NotifyDescriptor.YES_OPTION)
            result = RESULT_YES;
        
        else if (response == NotifyDescriptor.NO_OPTION)
            result = RESULT_NO;
        
        else // cancel or closed (x button)
        {
            result = RESULT_CANCEL;
            bCancelSaveDialog = true;
        }
        
        return result;
    }
    
    
    protected void componentActivated()
    {
        super.componentActivated();
        isHidden = false;
        registerListeners();
        
        getDrawingAreaControl().setFocus();
        
        TopComponentGroup group = WindowManager.getDefault()
                .findTopComponentGroup("modeling-diagrams"); // NOI18N
        
        if (group != null)
        {
            group.open();
            TopComponent tc = WindowManager.getDefault().findTopComponent("documentation");
            if (!Boolean.TRUE.equals(tc.getClientProperty("isSliding")))
                tc.requestVisible();
            
            // Jyothi: Fix for Bug#6252301 - overview window should listen
            // to diagram tab changes
            if (overviewOpen)
            {
                if (mControl != null)
                    mControl.overviewWindow(true);
            }
            
            else
            {
                if (mControl != null)
                {
                    mControl.overviewWindow(false);
                    overviewOpen = false;
                }
            }
        }
        else // workaround for #98334 then, should not happen now
        {
            TopComponent tc = WindowManager.getDefault().findTopComponent("documentation");
            
            if (tc != null && !tc.isOpened())
            {
                tc.open();
            }
        }
        
        selectedElements(getDrawingAreaControl().getDiagram(),
                getDrawingAreaControl().getSelected(), null);
        
        addBasicActionCallbacks();
    }
    
    
    protected void componentHidden()
    {
        super.componentHidden();
        isHidden = true;
        TopComponentGroup group = WindowManager.getDefault()
                .findTopComponentGroup("modeling-diagrams"); // NOI18N
        
        if (group != null)
        {
            group.close();
            
            //Jyothi: Fix for Bug#6252301 - overview window should listen
            // to diagram tab changes
            if ((mControl != null) && (mControl.getIsOverviewWindowOpen()))
            {
                mControl.overviewWindow(false);
                overviewOpen = true;
            }
            
            else
                overviewOpen = false;
        }
        else // workaround for #98334, should not happen now
        {
            TopComponent tc = WindowManager.getDefault().findTopComponent("documentation");
            if (tc!=null && tc.isOpened())
                tc.close();
        }
        
        mControl.selectAll(false);
        removeBasicActionCallbacks();
        removeSelectedActionCallbacks();
    }
    
    public String preferredID()
    {
        String retVal = m_PreferredID;
        
        if (retVal.length() <= 0)
        {
            if (getDrawingAreaControl() != null)
            {
                IDiagram diagram = getDrawingAreaControl().getDiagram();
                
                if (diagram != null)
                    m_PreferredID = preferredIDForDiagram(diagram);
            }
            
            else
                m_PreferredID = super.preferredID();
            
            retVal = m_PreferredID;
        }
        
        return retVal;
    }
    
    
    public static String preferredIDForDiagram(IDiagram diagram)
    {
        String retVal = "";
        
        if (diagram != null)
        {
            String fileName = diagram.getFilename();
            File file = new File(fileName);
            String name = file.getName();
            int dotIndex = name.lastIndexOf('.');
            
            if (dotIndex > 0)
                name = name.substring(0, dotIndex);
            
            retVal = name.toUpperCase();
        }
        
        return retVal;
    }
    
    
    
    ////////////////////////////////////////////////////////////////////////////
    // Drawing Area Event Handlers
    
    private boolean processingProperties = false;
    
    /**
     * Fired after the select list has been modified.  If it was a compartment
     * select that caused this event then the compartment is also provided,
     * otherwise it is NULL
     */
    public void onSelect(
            IDiagram pParentDiagram,
            ETList<IPresentationElement> selectedItems,
            ICompartment pCompartment,
            IResultCell cell)
    {
        IDiagram myDiagram = getDrawingAreaControl().getDiagram();
        
        if(pParentDiagram.isSame(myDiagram) == true)
            selectedElements(pParentDiagram, selectedItems, pCompartment);
        
        // Jyothi:
        // Fix for Bug#6258627-Naming a component doesn't update the property sheet Name to the new value.
        // Fired a Selection Event in ETEditableCompartment's save method..
        // This is a temporary fix.. need a better way to do this..
        
        //Debug.out.println(" DiagramTopComponent : onSelect.....!!!!!!!!!!!");
        // cvc needed the property sheet refresh so I made it a separate method
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                refreshPropertySet();
            }
        });
    }
    
    private void refreshPropertySet()
    {
        Node[] activatedNodes = TopComponent.getRegistry().getActivatedNodes();
        
        if (activatedNodes == null)
            return;
        
        int arrayLength = activatedNodes.length;
        
        for (int i = 0; i<arrayLength; i++)
        {
            Node myNode = activatedNodes[i];
            
            if (myNode instanceof AbstractModelElementNode)
            {
                AbstractModelElementNode abstractNode =
                        (AbstractModelElementNode)myNode;
                
                abstractNode.notifyPropertySetsChange();
            }
        }
    }
    
    /**
     * Fired after the select list has been modified.
     */
    public void onUnselect(
            IDiagram pParentDiagram,
            IPresentationElement[] unselectedItems,
            IResultCell cell )
    {
        IDiagram myDiagram = getDrawingAreaControl().getDiagram();
        
        if (pParentDiagram.isSame(myDiagram) == true)
        {
            ETList<IPresentationElement> elements =
                    pParentDiagram.getSelected();
            
            selectedElements(pParentDiagram, elements, null);
        }
    }
    
    protected void selectedElements(
            IDiagram diagram,
            ETList<IPresentationElement> selectedItems,
            ICompartment pCompartment)
    {
        IElement pEle = null;
        
        if (pCompartment != null)
        {
            pEle = pCompartment.getModelElement();
            addSelectedActionCallbacks();
        }
        
        else if (selectedItems != null && selectedItems.size() > 0)
        {
            IPresentationElement presEle = selectedItems.get(0);
            
            if (presEle != null) {
                pEle = presEle.getFirstSubject();
                }
            addSelectedActionCallbacks();
        }
        
        else if (diagram != null)
            pEle = diagram;
        
        if (pEle != null)
        {   
            node = new LocalUMLModelElementNode();
            node.setElement(pEle);
            // if the element has not been named by users, the default element 
            // name is the element type; Otherwise, the customed name is used.
            node.setName(pEle.getElementType());
            
            if (pEle instanceof INamedElement)
            {
                String name = ((INamedElement)pEle).getName();
                
                if (name != null && !name.trim().equals(""))
                {
                    node.setName(name);
                }
                else
                {   // Fixed issue 78484. Display the expanded name as the default
                    // name for elements that extend IAssociation, namely, 
                    // aggregation and composition.
                    if (pEle instanceof IAssociation)
                    {
                        String expandedName = pEle.getExpandedElementType();
                        if (expandedName != null && expandedName.trim().length() > 0)
                        {
                            node.setName(expandedName.replace('_', ' '));
                        }
                    }
                }
            }
            else if (pEle instanceof IDiagram)
                node.setName(((IDiagram)pEle).getName());
            
            Node[] nodes = new Node[1];
            nodes[0] = node;
            setActivatedNodes(nodes);
        }
        
        else
        {
            Node[] nodes = new Node[0];
            setActivatedNodes(nodes);
            removeSelectedActionCallbacks();
        }
        
        // The below method call is redundant. It does exact the same sequence
        // of code as the above again. Thus, commenting it out.
        // configureLocalNode(pEle);
    }
    
    
    private void configureLocalNode(final IElement element)
    {
        if (element != null)
        {
            node = new LocalUMLModelElementNode();
            node.setElement(element);
            node.setName(element.getElementType());
            
            if (element instanceof INamedElement)
            {
                String name = ((INamedElement)element).getName();
                
                if (!name.trim().equals(""))
                    node.setName(((INamedElement)element).getName());
            }
            
            else if (element instanceof IDiagram)
                node.setName(((IDiagram)element).getName());
            
            Node[] nodes = new Node[1];
            nodes[0] = node;
            setActivatedNodes(nodes);
        }
        
        else
        {
            Node[] nodes = new Node[0];
            setActivatedNodes(nodes);
            removeSelectedActionCallbacks();
        }
    }
    
    public IDrawingAreaControl getDrawingAreaControl()
    {
        return mControl;
    }
    
    private PaletteController getAssociatedPalette()
    {
        if (paletteSupport == null) {
	    paletteSupport = new PaletteSupport();
        }
	if (getDrawingAreaControl() != null) {
        PaletteController pController =
                paletteSupport.getPalette(getDrawingAreaControl());
        
        return pController;
	} 
	return null;
    }

    private void detachAssociatedPalette()
    {
        if (paletteSupport != null) {
	    paletteSupport.unregisterListeners();
	}
    }
    
    
    public void onDrawingAreaTooltipPreDisplay(
            IDiagram pParentDiagram,
            IPresentationElement pPE,
            IToolTipData pTooltip,
            IResultCell cell)
    {}
    
    public void onDrawingAreaPreSave(
            IProxyDiagram pParentDiagram, IResultCell cell)
    {}
    
    public void onDrawingAreaPreFileRemoved(
            String sFilename, IResultCell cell)
    {}
    
    public void onDrawingAreaPreDrop(
            IDiagram pParentDiagram,
            IDrawingAreaDropContext pContext,
            IResultCell cell)
    {
    }
    
    public void onDrawingAreaPreCreated(
            IDrawingAreaControl pDiagramControl, IResultCell cell)
    {}
    
    public void onDrawingAreaPostSave(
            IProxyDiagram pParentDiagram, IResultCell cell)
    {
        
    }
    
    
    public void onDrawingAreaPrePropertyChange(
            IProxyDiagram proxyDiagram,
            int propertyKindChanged,
            IResultCell cell)
    {
        if (propertyKindChanged == IDrawingAreaPropertyKind.DAPK_NAME &&
                proxyDiagram.getDiagram().isSame(
                getDrawingAreaControl().getDiagram()) &&
                getDrawingAreaControl().getIsDirty())
        {
            // cvc - CR 6300910
            // see if the diagram is dirty, if so we must prompt the user
            //  to save the diagram before the rename, because after it is
            //  change renamed, we will autosave it for them so that the
            //  name will persist properly, otherwise the user will be
            //  prompted to save when closing the diagram after
            //  a name change.
            // This result of this event determines whether on not the
            //  following event below (onDrawingArea"Post"PropertyChange)
            //  get invoked, so be sure you understand the CR above
            //  and all that is involved before you modify this code.
            
            String msg = NbBundle.getMessage(
                    DiagramTopComponent.class,
                    "LBL_DIALOG_MSG_RenamePreSaveDiagram",  // NOI18N
                    getDrawingAreaControl().getDiagram().getName());
            
            switch (saveDiagram())
            {
            case RESULT_YES:
                // everything is good, diagram saved, proceed with rename
                return;
                
            case RESULT_NO:
            case RESULT_CANCEL:
                // modified diagram not saved by user
                // when the user clicked "No" on the Save dialog, this
                //  results in the diagram being set to not dirty, but in
                //  this case we just want to leave it dirty and prevent
                //  the rename from happening
                getDrawingAreaControl().setIsDirty(true);
                
                // prevent rename from happening
                cell.setContinue(false);
                
                // the user just changed the name
                // in the property sheet, and even though they said
                // No or Cancel to the Save diagram dialog, the Save is
                // aborted, and the rename for the tree node and diagram tab
                // is aborted, but the property sheet property will keep the
                // new value, but it should revert back to the old value.
                // refreshing the property sheet makes it revert back.
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        refreshPropertySet();
                    }
                });
            }
        }
    }
    
    public void onDrawingAreaPostPropertyChange(
            IProxyDiagram proxyDiagram,
            int propertyKindChanged,
            IResultCell cell)
    {
        IDiagram diag = getDrawingAreaControl().getDiagram();
        IDiagram affDiag = proxyDiagram.getDiagram();
        
        if (affDiag != null && affDiag.isSame(diag))
        {
            // cvc - CR 6275795
            // when the diagram node's name is change, we must update the
            //  drawing tab's name/displayName
            if (propertyKindChanged == IDrawingAreaPropertyKind.DAPK_NAME &&
                    proxyDiagram.getDiagram().isSame(
                    getDrawingAreaControl().getDiagram()))
            {
                // cvc - CR 6300910
                // this code is highly dependent on the code in the event
                //  above: onDrawingArea"Pre"PropertyChange
                // make sure you understand that code and everything in
                // the CR above before you modify this code.
                setName(proxyDiagram.getName());
                setDisplayName(proxyDiagram.getName());
            }
        }
    }
    
    public void onDrawingAreaPostDrop(
            IDiagram pParentDiagram,
            IDrawingAreaDropContext pContext,
            IResultCell cell)
    {
    }
    
    public void onDrawingAreaPostCreated(
            IDrawingAreaControl pDiagramControl, IResultCell cell)
    {}
    
    public void onDrawingAreaOpened(IDiagram parentDiagram, IResultCell cell)
    {}
    
    public void onDrawingAreaKeyDown(
            IDiagram pParentDiagram,
            int nKeyCode,
            boolean bControlIsDown,
            boolean bShiftIsDown,
            boolean bAltIsDown,
            IResultCell cell)
    {
    }
    
    public void onDrawingAreaFileRemoved(String sFilename, IResultCell cell)
    {
    }
    
    public void onDrawingAreaClosed(
            IDiagram parentDiagram, boolean isDirty, IResultCell cell)
    {}
    
    public void onDrawingAreaActivated(
            IDiagram pParentDiagram, IResultCell cell)
    {
    }
    
    
    
    // IDrawingAreaAddNodeEventsSink implementations
    
    public void onDrawingAreaCreateNode(
            IDiagram pParentDiagram,
            ICreateNodeContext pContext,
            IResultCell cell)
    {
    }
    
    public void onDrawingAreaDraggingNode(
            IDiagram pParentDiagram,
            IDraggingNodeContext pContext,
            IResultCell cell)
    {
    }
    
    public void addSelectedActionCallbacks()
    {
        ((CallbackSystemAction) SystemAction.get(CopyAction.class))
                .setActionPerformer(new DiagramCopyCookie());
        
        ((CallbackSystemAction) SystemAction.get(CutAction.class))
                .setActionPerformer(new DiagramCutCookie());
        
        ((CallbackSystemAction) SystemAction.get(DeleteAction.class))
                .setActionPerformer(new DeletePerformer());
    }
    
    public void removeSelectedActionCallbacks()
    {
        ((CallbackSystemAction) SystemAction.get(CopyAction.class))
                .setActionPerformer(null);
        
        ((CallbackSystemAction) SystemAction.get(CutAction.class))
                .setActionPerformer(null);
        
        ((CallbackSystemAction) SystemAction.get(DeleteAction.class))
                .setActionPerformer(null);
    }
    
    public void addBasicActionCallbacks()
    {
        getActionMap().put(javax.swing.text.DefaultEditorKit.pasteAction,
                new DiagramPasteAction());
    }
    
    public void removeBasicActionCallbacks()
    {
        getActionMap().put(javax.swing.text.DefaultEditorKit.pasteAction,
                new DiagramPasteAction());
    }
    
    
    public Lookup getLookup()
    {
        Lookup superLookup = super.getLookup();
        Lookup thisLookup = getThisLookup();
        
        return new ProxyLookup(new Lookup[] {superLookup, thisLookup});
    }
    
    public Lookup getThisLookup()
    {
        if ( paletteContrl == null)
            paletteContrl = this.getAssociatedPalette();
        //return Lookups.fixed(new Object[] {mControl, paletteContrl, diagramDO});
        return Lookups.fixed(new Object[] {mControl, paletteContrl, getDiagramDO() } );
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Cookie Classes
    
    public class DiagramPrintCookie implements PrintCookie
    {
        public void print()
        {
            IProductDiagramManager pDiaMgr =
                    ProductHelper.getProductDiagramManager();
            
            IDiagram diagram = getDrawingAreaControl().getDiagram();
            if (diagram != null)
            {
                diagram.printGraph(true);
            }
        }
    }
    
    
    private class LocalUMLModelElementNode extends UMLModelElementNode
    {
        private DiagramPrintCookie printCookie;
       
        public LocalUMLModelElementNode()
        {
            super();
            addPrintCookie();
            
            if (mControl.getIsDirty())
                addSaveCookie();
        }
        
        public void setName(String val)
        {
            setDisplayName(val);
            getData().setItemText(val);
        }
        
        
        public void addSaveCookie()
        {
            Cookie cookie = getDiagramDO().getCookie(SaveCookie.class);
            getCookieSet().add(cookie);
        }
        
        public void removeSaveCookie()
        {
            Cookie cookie = getCookie(SaveCookie.class);
            if (cookie != null)
                getCookieSet().remove(cookie);
        }
        
        public DiagramPrintCookie getDiagramPrintCookie()
        {
            if (printCookie == null)
                printCookie = new DiagramPrintCookie();
            
            return printCookie;
        }
        
        public void addPrintCookie()
        {
            if (getCookieSet().getCookie(DiagramPrintCookie.class) == null)
            {
                getCookieSet().add(getDiagramPrintCookie());
            }
        }
        
        public void removePrintCookie()
        {
            getCookieSet().remove(getDiagramPrintCookie());
        }
	
	public boolean equals(Object obj) {
	    if (this.hashCode() == obj.hashCode())
		return true;
	    return false;	    
	}
	
    }
    
    
    public class DiagramCopyCookie implements ActionPerformer
    {
        public void performAction(SystemAction action)
        {
            mControl.copy();
        }
    }
    
    public class DiagramCutCookie implements ActionPerformer
    {
        public void performAction(SystemAction action)
        {
            mControl.cut();
        }
    }
    
    public class DeletePerformer implements ActionPerformer
    {
        public void performAction(SystemAction action)
        {
            mControl.deleteSelected(true);
        }
    }
    
    public class DiagramPasteAction extends AbstractAction
    {
        public void actionPerformed(ActionEvent e)
        {
            mControl.paste();
        }
    }
    
    public boolean isListenersRegistered()
    {
        return listenersRegistered;
    }
    
    public void setListenersRegistered(boolean listenersRegistered)
    {
        this.listenersRegistered = listenersRegistered;
    }
    
    
    private SaveCookie getSaveCookie()
    {
        if (node == null)
            return null;
        
        Cookie cookie = node.getCookie(SaveCookie.class);
        if (cookie != null)
            return (SaveCookie)cookie;
        return null;
    }
    
    private void setDiagramDisplayName(final String name)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                setDisplayName(name);
            }
        });
    }
    
    private class DiagramChangeListener implements PropertyChangeListener
    {
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (evt.getPropertyName().equals(ADDrawingAreaControl.DIRTYSTATE))
            {
                boolean modified = ((Boolean)evt.getNewValue()).booleanValue();
                DiagramDataObject dobj = getDiagramDO();
                if (modified)
                {
                    dobj.addSaveCookie();
                    setDiagramDisplayName(mControl.getNameWithAlias() + SPACE_STAR);
                    addSaveCookie();
                    dobj.setModified(modified);
                }
                else
                {
                    dobj.removeSaveCookie();
                    dobj.setModified(modified);
                    setDiagramDisplayName(mControl.getNameWithAlias());
                    removeSaveCookie();
                }
            }
            else if (evt.getPropertyName().equals(DataObject.PROP_MODIFIED))
            {
                if (evt.getNewValue() == Boolean.FALSE)
                {
                    setDiagramDisplayName(mControl.getNameWithAlias());
                    removeSaveCookie();
                }
            }
        }
    }
}
