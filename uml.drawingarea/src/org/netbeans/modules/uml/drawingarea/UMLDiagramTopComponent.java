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

package org.netbeans.modules.uml.drawingarea;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.common.ui.SaveNotifierYesNo;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.common.commonactivities.IActivity;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateMachine;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConstraint;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnectableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IStructuredClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.FileExtensions;
import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.drawingarea.ZoomManager.ZoomEvent;
import org.netbeans.modules.uml.drawingarea.actions.CopyPasteSupport;
import org.netbeans.modules.uml.drawingarea.actions.SceneAcceptAction;
import org.netbeans.modules.uml.drawingarea.actions.SceneAcceptProvider;
import org.netbeans.modules.uml.drawingarea.dataobject.PaletteItem;
import org.netbeans.modules.uml.drawingarea.dataobject.UMLDiagramDataNode;
import org.netbeans.modules.uml.drawingarea.dataobject.UMLDiagramDataObject;
import org.netbeans.modules.uml.drawingarea.engines.DiagramEngine;
import org.netbeans.modules.uml.drawingarea.keymap.DiagramInputkeyMapper;
import org.netbeans.modules.uml.drawingarea.palette.PaletteSupport;
import org.netbeans.modules.uml.drawingarea.palette.RelationshipFactory;
import org.netbeans.modules.uml.drawingarea.palette.context.ContextPaletteManager;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceManager;
import org.netbeans.modules.uml.drawingarea.support.DiagramTypesManager;
import org.netbeans.modules.uml.drawingarea.support.ModelElementBridge;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget;
import org.netbeans.modules.uml.ui.support.ADTransferable;
import org.netbeans.modules.uml.resources.images.ImageUtil;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.ElementDeletePanel;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.commonresources.CommonResourceManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.DiagramAreaEnumerations;
import org.netbeans.modules.uml.ui.support.diagramsupport.DrawingAreaEventsAdapter;
import org.netbeans.modules.uml.ui.support.diagramsupport.IDrawingAreaEventDispatcher;
import org.netbeans.spi.navigator.NavigatorLookupHint;
import org.netbeans.spi.palette.PaletteController;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.Toolbar;
import org.openide.cookies.SaveCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ClipboardEvent;
import org.openide.util.datatransfer.ClipboardListener;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
public class UMLDiagramTopComponent extends TopComponent implements MouseListener
{

    private static UMLDiagramTopComponent instance;
    /** path to the icon used by the component and its open action */
    //    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "UMLDiagramTopComponent";
    
    private JComponent diagramView;
    private NavigatorHint navigatorCookie = null;
    private DesignerScene scene;

    private PaletteController paletteController;
    private transient UMLDiagramDataObject diagramDO;
    private DiagramChangeListener diagramChangeListener = new DiagramChangeListener();
    
    JPanel decoratorLayer = new JPanel();
    private String preferredID = "";

    private InstanceContent lookupContent = new InstanceContent();
    private Lookup lookup = null;

    protected IDrawingAreaEventDispatcher drawingAreaDispatcher;
    protected DispatchHelper helper = new DispatchHelper();
    private boolean isNewDiagram = false;

    //private ChangeHandler changeListener = new ChangeHandler();
    private DrawingAreaChangeHandler changeListener = null;
    private EngineDrawingAreaSink drawingAreaSink = new EngineDrawingAreaSink();
    private Toolbar editorToolbar = null;
    

    // Global Actions.
    private ZoomManager zoomManager = null;
    private CopyCutActionPerformer copyActionPerformer = new CopyCutActionPerformer(true);
    private CopyCutActionPerformer cutActionPerformer = new CopyCutActionPerformer(false);
    private PasteActionPerformer pasteActionPerformer = new PasteActionPerformer();
    private DeleteActionPerformer deleteActionPerformer = new DeleteActionPerformer();
    private ClipboardListener clipboardListener;
    private ExplorerManager explorerManager;
    private SceneAcceptProvider provider;

    private static final int RESULT_CANCEL = 0;
    private static final int RESULT_NO = 1;
    private static final int RESULT_YES = 2;
    private boolean activated;
    
    private UMLDiagramTopComponent()
    {
        initComponents();
        
        explorerManager = new ExplorerManager();

        lookupContent.add(
            ExplorerUtils.createLookup(explorerManager, setupActionMap(getActionMap()))
        );

        Clipboard c = CopyPasteSupport.getClipboard();
        if (c instanceof ExClipboard) {
            ExClipboard clip = (ExClipboard) c;
            if (clipboardListener == null)
                clipboardListener = new ClipboardChangesListener();
            clip.addClipboardListener(clipboardListener);
        }        
                
        copyActionPerformer.setEnabled(false);
        cutActionPerformer.setEnabled(false);
        pasteActionPerformer.setEnabled(false);
        deleteActionPerformer.setEnabled(false);
         
        if (java.awt.EventQueue.isDispatchThread())
        {
            setToolTipText(NbBundle.getMessage(UMLDiagramTopComponent.class, "HINT_UMLDiagramTopComponent"));
        } else
        {
            java.awt.EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    setToolTipText(NbBundle.getMessage(UMLDiagramTopComponent.class, "HINT_UMLDiagramTopComponent"));
                }
            });
        }
    }

    public UMLDiagramTopComponent(String filename) throws DataObjectNotFoundException {
        this();
        
        //Jyothi: We are loading the file
        File file = new File(filename);

        PersistenceManager pMgr = new PersistenceManager();
        scene = pMgr.loadDiagram(filename, this, isEdgesGrouped());
        assert scene != null;

        FileObject fobj = FileUtil.toFileObject(file);
        diagramDO = (UMLDiagramDataObject) DataObject.find(fobj);

        if (fobj.canWrite() == false)
        {
            scene.setActiveTool(DesignerTools.READ_ONLY);
        }

        // After reading in the data from the file, we should have both the
        // scene and the diagram initialized.

        UIDiagram uiDiagram = (UIDiagram) scene.getDiagram();
        if (uiDiagram != null)
        {
            uiDiagram.setDataObject(diagramDO);
        }
        initInAWTThread();

        editorToolbar = new Toolbar("Diagram Toolbar", false);
        add(editorToolbar, BorderLayout.NORTH);
        
        zoomManager = new ZoomManager(scene);
        zoomManager.addZoomListener(new ZoomManager.ZoomListener() {
            public void zoomChanged(ZoomEvent event)
            {
                ContextPaletteManager manager = scene.getContextPaletteManager();
                if(manager != null)
                {
                    // Make sure that the palette is correctly placed for the 
                    // zoom level.
                    manager.cancelPalette();
                    manager.selectionChanged(null);
                }
            }
        });
        
        initRootNode();   
        initLookup();
        initializeToolBar();
    }

    public UMLDiagramTopComponent(INamespace owner, String name, int kind)
    {
        this();
        
        IDiagram diagram = initNewDiagram(owner, name, kind);

       initInAWTThread();
        
        editorToolbar = new Toolbar("Diagram Toolbar", false);
        add(editorToolbar, BorderLayout.NORTH);
        
        zoomManager = new ZoomManager(scene);
        zoomManager.addZoomListener(new ZoomManager.ZoomListener() {
            public void zoomChanged(ZoomEvent event)
            {
                ContextPaletteManager manager = scene.getContextPaletteManager();
                if(manager != null)
                {
                    // Make sure that the palette is correctly placed for the 
                    // zoom level.
                    manager.cancelPalette();
                    manager.selectionChanged(null);
                }
            }
        });
        
        initRootNode();
        initLookup();
        initializeToolBar();
    }

    protected boolean isEdgesGrouped()
    {
        return true;
    }
    
    private void initRootNode()
    {
        DiagramModelElementNode node = new DiagramModelElementNode(getDiagramDO());
        node.setName(getName());
        node.setElement(getDiagram().getDiagram());
        node.setScene(scene);
        getExplorerManager().setRootContext(node);
    }

    private void init()
    {
        initUI();
        setName();
        setIcon();
    }
    
    
    private void initInAWTThread()
    {
        if (java.awt.EventQueue.isDispatchThread())
        {
            init();
        } else
        {
            java.awt.EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    init();
                }
            });
        }
    }
    
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    
    ActionMap setupActionMap(javax.swing.ActionMap map)
    {
        map.put(DefaultEditorKit.copyAction, copyActionPerformer);
        map.put(DefaultEditorKit.cutAction, cutActionPerformer);
        map.put(DefaultEditorKit.pasteAction, pasteActionPerformer);
        map.put("delete", deleteActionPerformer); // NOI18N
        return map;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // TopCompnent Overrides
    
     @Override
    public boolean canClose() 
     {
         boolean safeToClose = true;
        if (getDiagramDO() != null && getDiagramDO().getCookie(SaveCookie.class) == null)
        {
            return true;            
        }
        //prompt to save before close
        switch (saveDiagram())
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
            this.setDiagramDirty(false);
            break;
            
        case RESULT_CANCEL:
            safeToClose = false;
            break;
        }
        
        return safeToClose;
    }
     
     private int saveDiagram()
    {   
        String title = NbBundle.getMessage(UMLDiagramTopComponent.class,
                "LBL_DIALOG_TITLE_SaveDiagram"); // NOI18N
        
        String objType = NbBundle.getMessage(
                UMLDiagramTopComponent.class,
                "LBL_DIALOG_MSG_Diagram",  // NOI18N
                getName());
        
        int result = RESULT_CANCEL;
        
        Object response = SaveNotifierYesNo.getDefault().displayNotifier(
                title, // NOI18N
                objType, // NOI18N
                getAssociatedDiagram().getNamespace().toString()+"::"+getName());
        
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
        }
        
        return result;
    }
    
    @Override
    protected void componentClosed()
    {
        // TODO: Do the same thing as preClosed on the ADDrawingAreaControl.
        IProduct pProd = ProductHelper.getProduct();
        if (pProd != null)
        {
            // Make sure the diagram is in the workspace
            pProd.removeDiagram(getAssociatedDiagram());
        }
        unregisterListeners();
        // remove key-action mapping
        DiagramInputkeyMapper keyActionMapper = DiagramInputkeyMapper.getInstance();
        keyActionMapper.setComponent(this);
        keyActionMapper.unRegisterKeyMap();
        keyActionMapper.unRegisterToolbarActions(editorToolbar);
    }

    @Override
    protected void componentOpened()
    {
        super.componentOpened();

        IProduct pProd = ProductHelper.getProduct();
        if (pProd != null)
        {
            // Make sure the diagram is in the workspace
            pProd.addDiagram(getAssociatedDiagram());
        }

        if (isNewDiagram == true)
        {
            IEventPayload payload = getDrawingAreaDispatcher().createPayload("FireDrawingAreaPostCreated");
            getDrawingAreaDispatcher().fireDrawingAreaPostCreated(getDiagramDO(), payload);
        }
        registerListeners();
        //set key-action mapping
        DiagramInputkeyMapper keyActionMapper = DiagramInputkeyMapper.getInstance();
        keyActionMapper.setComponent(this);
        keyActionMapper.registerKeyMap();
        keyActionMapper.registerToolbarActions(editorToolbar);
        
        if(getDiagram()!=null && getDiagram().getView()!=null)getDiagram().getView().requestFocusInWindow();
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        this.activated=true;
        if (getDiagram() != null && getDiagram().getView() != null)
        {
            getDiagram().getView().requestFocusInWindow();
        }
        
        TopComponentGroup group = WindowManager.getDefault().findTopComponentGroup("modeling-diagrams"); // NOI18N
        if (group != null)
        {
            group.open();
            TopComponent tc = WindowManager.getDefault().findTopComponent("documentation");
            if (!Boolean.TRUE.equals(tc.getClientProperty("isSliding")))
                tc.requestVisible();
        }
    }

    @Override
    protected void componentDeactivated() {
        super.componentDeactivated();
        this.activated=false;
    }

    public boolean isActivated()
    {
        return activated;
    }
    
    @Override
    public int getPersistenceType()
    {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public Lookup getLookup()
    {
        if (lookup == null)
        {
            Lookup superLookup = super.getLookup();

            Lookup[] content = {superLookup, new AbstractLookup(lookupContent)};
            lookup = new ProxyLookup(content);
        }

        //        Collection c = merge.lookupAll(DesignView.class);
        //        System.err.println(" lookup contains DesignView "+ c.size());
        return lookup;
    }

    ////////////////////////////////////////////////////////////////////////////
    // API Methods
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
            {
                name = name.substring(0, dotIndex);
            }
            retVal = name.toUpperCase();
        }

        return retVal;
    }

    public IDiagram getAssociatedDiagram()
    {
        IDiagram retVal = null;

        if (scene != null)
        {
            retVal = scene.getDiagram();
        }

        return retVal;
    }

    public DesignerScene getScene()
    {
        return scene;
    }

    @Override
    public String preferredID()
    {
        String retVal = preferredID;

        if (retVal.length() <= 0)
        {
            if (getAssociatedDiagram() != null)
            {
                IDiagram assocDiagram = getAssociatedDiagram();

                if (assocDiagram != null)
                {
                    preferredID = preferredIDForDiagram(assocDiagram);
                }
            }
            else
            {
                preferredID = super.preferredID();
            }

            retVal = preferredID;
        }

        return retVal;
    }

    ///////////////////////////////////////////////////////////////
    // Helper Methods
    //////////////////////////////////////////////////////////////
    protected void registerListeners()
    {
        if(getDiagram().isReadOnly() == false)
        {
            changeListener = new DrawingAreaChangeHandler(this);
            DrawingAreaEventHandler.addChangeListener(changeListener);
            getDiagramDO().addPropertyChangeListener(diagramChangeListener);
            helper.registerDrawingAreaEvents(drawingAreaSink);
        }
    }
    
    
    protected void unregisterListeners()
    {
        DrawingAreaEventHandler.removeChangeListener(changeListener);
        getDiagramDO().removePropertyChangeListener(diagramChangeListener);
        helper.revokeDrawingAreaSink(drawingAreaSink);
    }
    
    protected void setIcon()
    {
        CommonResourceManager resource = CommonResourceManager.instance();

        String kind = getAssociatedDiagram().getDiagramKindAsString();
        final String details = resource.getIconDetailsForElementType(kind);
        if (java.awt.EventQueue.isDispatchThread())
        {
            setIcon(ImageUtilities.loadImage(details, true));
        } else
        {
            java.awt.EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    setIcon(ImageUtilities.loadImage(details, true));
                }
            });
        }
    }

    protected void setName()
    {
        IDiagram diagram = getAssociatedDiagram();

        setDisplayName(diagram.getNameWithAlias());
        setName(diagram.getName());
        
        INamespace space = diagram.getNamespace();
        if (space != null && diagram != null)
        {
            setToolTipText(space.getFullyQualifiedName(true) + "::" + diagram.getNameWithAlias()); // NOI18N
        }
        if(diagramView != null)
        {
            diagramView.putClientProperty("print.name", diagram.getNameWithAlias()); // NOI18N
            diagramView.getAccessibleContext().setAccessibleName(diagram.getNameWithAlias());
            diagramView.getAccessibleContext().setAccessibleDescription(getToolTipText());
        }
    }

    /**
     *
     * @return
     */
    private NavigatorHint getNavigatorCookie()
    {
        if (navigatorCookie == null)
        {
            navigatorCookie = new NavigatorHint();
        }
        return navigatorCookie;
    }

    private PaletteController getAssociatedPalette()
    {

        if (paletteController == null)
        {
            PaletteSupport support = new PaletteSupport();
            paletteController = support.getPalette(getAssociatedDiagram());
        }

        paletteController.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event)
            {
                String propName = event.getPropertyName();
                if (PaletteController.PROP_SELECTED_ITEM.equals(propName)) 
                {
                    // If a node is deseleted then the new value is an empty
                    // lookup.  If a node is selected then the new value is
                    // a lookup that has a node.
                    Scene scene = getScene();
                    
                    // If the diagram is read-only do not respond to changes 
                    // in the diagram.
                    if(getDiagram().isReadOnly() == true)
                    {
                        return;
                    }
                    
                    WidgetAction.Chain actions = scene.createActions(DesignerTools.PALETTE);
                    
                    for(int index = 0; index < actions.getActions().size(); index++)
                    {
                        actions.removeAction(index);
                    }        
                    
                    if(event.getNewValue() instanceof Lookup)
                    {
                        Lookup lookup = (Lookup)event.getNewValue();
                        Node node = lookup.lookup(Node.class);
                        if(node != null)
                        {
                            Lookup nodeLookup = node.getLookup();
                             try {
                                CopyPasteSupport.getClipboard().setContents(node.clipboardCopy(), new StringSelection("")); // NOI18N
                             }catch(Exception e)
                             {}
                            INamespace space = getAssociatedDiagram().getNamespaceForCreatedElements();
                            actions.addAction(new SceneAcceptAction(space, nodeLookup.lookup(PaletteItem.class)));
//                            actions.addAction(ActionFactory.createAcceptAction(new SceneAcceptAction(space)));
                            scene.setActiveTool(DesignerTools.PALETTE);
                        }
                        else
                        {
                            if(DesignerTools.mapToolToButton.get(DesignerTools.SELECT)!=null)
                            {
                                DesignerTools.mapToolToButton.get(DesignerTools.SELECT).doClick();
                            }
                            else scene.setActiveTool(DesignerTools.SELECT);
                        }
                    }
                    else
                    {
                            if(DesignerTools.mapToolToButton.get(DesignerTools.SELECT)!=null)
                            {
                                DesignerTools.mapToolToButton.get(DesignerTools.SELECT).doClick();
                            }
                            else scene.setActiveTool(DesignerTools.SELECT);
                    }
                }
            }
        });
        
        return paletteController;
    }

    /**
     * Initializes a new diagram.  The new diagram creates a new fileobject,
     * and sends an event notifying listners that a diagram is created.  The
     * scene will also be initialied as well.
     */
    protected IDiagram initNewDiagram(INamespace owner, String name, int kind)
    {
        IDiagram retVal = null;

        if (getAssociatedDiagram() == null)
        {
            DiagramTypesManager pMgr = DiagramTypesManager.instance();
            String diaType = pMgr.getUMLType(kind);
            if (diaType == null)
            {
                diaType = "";
            }

            if (diaType.length() > 0)
            {
                UMLDiagramDataObject data = getDiagramDO(name, owner);
                UIDiagram diagram = (UIDiagram)FactoryRetriever.instance().createType("Diagram", null);
                diagram.setDataObject(data);

                // At this point the diagram has not yet been saved, so 
                // setting the notify flag to false to prevent the diagram from firing
                // name-change events
                diagram.setNotify(false);
                diagram.setName(name);
//            diagram.setNamespace(owner);
                diagram.setDiagramKind(kind);
                diagram.setNotify(true);
                setDiagramNameSpace(owner, diagram);
                lookupContent.add(diagram);   

                retVal = diagram;
                scene = new DesignerScene(diagram,this);
                scene.setEdgesGrouped(isEdgesGrouped());
            }

            isNewDiagram = true;
        }

        return retVal;
    }

    private void setDiagramNameSpace(INamespace owner, IDiagram diagram)
    {
        /*
         * For Activity, Collaboration, Sequence, and State diagrams,
         * their diagrams name space is not IProject but IActivity, IInteration,
         * and IStateMachine respectively. For Class, Component, Deployment and
         * Use Case diagram, their name name space is IProject.
         * This method is to create and set appropriate name space
         * for the above diagrams.
         * */

        String diagramKind = diagram.getDiagramKindAsString();
        INamespace newNameSpace = null;
        if (owner != null)
        {
            //modifications have sense only if occur in project, package etc
            // if user create activity diagram inside activity, sqd inside interaction we have nothing to do
            FactoryRetriever factory = FactoryRetriever.instance();
            if (diagramKind.indexOf("Activity") > -1 && !(owner instanceof IActivity))
            {
                //create Activity namespace for Activity diagram
                newNameSpace = (IActivity) factory.createType("Activity", owner);
            } 
            else if ((diagramKind.indexOf("Collaboration") > -1 ||
                    diagramKind.indexOf("Sequence") > -1) && !(owner instanceof IInteraction))
            {
                // Create Interaction nameSpace for Collaboration/Sequence diagram
                newNameSpace = (IInteraction) factory.createType("Interaction", owner);
            } 
            else if (diagramKind.indexOf("State") > -1 && !(owner instanceof IStateMachine))
            {
                // Create StateMachine nameSpace for State diagram
                newNameSpace = (IStateMachine) factory.createType("StateMachine", owner);
            } 
            
            if (newNameSpace != null) 
            {
                newNameSpace.setName(diagram.getName());
                newNameSpace.setNamespace(owner);
                owner.addOwnedElement(newNameSpace);
                diagram.setNamespace(newNameSpace);
            }
            else 
            {
                diagram.setNamespace(owner);
            }
        }
    }
    
    /*
     * getDrawingAreaDispatcher is a demand load accessor to the drawArea Event Dispatcher.
     */
    private IDrawingAreaEventDispatcher getDrawingAreaDispatcher()
    {
        if (drawingAreaDispatcher == null)
        {
            drawingAreaDispatcher = helper.getDrawingAreaDispatcher();
        }
        return drawingAreaDispatcher;
    }

    private void initUI()
    {
        if (scene != null)
        {
            JLayeredPane view = new JLayeredPane();
            view.setLayout(new OverlayLayout(view));

            if (scene.getView() == null) {
                diagramView = scene.createView();
            } else {
                diagramView = scene.getView();
            }
            scene.getView().setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
            diagramView.putClientProperty("print.printable", Boolean.TRUE); // NOI18N
            
            // If the scene component has a mouse wheel listener then the 
            // native JScrollPane scroll wheel behavior will not occur.  So
            // Since we do any special mouse wheel behavior, remove all mouse
            // wheel listeners (there should only be one) so the JScrollPane will
            // handle the events.
            //
            // Why is it important to have the native mouse wheel behavior?  The 
            // Swing mouse wheel implmentation only handles the vertical wheel
            // behavior.  However newer mouse technology include horizontal 
            // mouse wheel behaviors as well.  In fact the Mac mouse touch pad
            // also implements the horizontal mouse scroll behavior.  In fact
            // the Mac JScrollPanel has also implemented the horizontal scroll
            // behavior.
            for(MouseWheelListener listener : diagramView.getMouseWheelListeners())
            {
                diagramView.removeMouseWheelListener(listener);
            }
            
            view.add(diagramView, new Integer(1));
            
            InputMap input = new InputMap();
            ActionMap actionMap = new ActionMap();
            Util.buildActionMap(input, actionMap, "Actions/UML/GeneralDiagram");

            decoratorLayer.setOpaque(false);
            decoratorLayer.setLayout(null);
            view.add(decoratorLayer, new Integer(0));
            
            // workaround for 134994
            diagramView.addMouseListener(this);

            jScrollPane1.setViewportView(view);
            jScrollPane1.getVerticalScrollBar().setUnitIncrement(20);
            jScrollPane1.getHorizontalScrollBar().setUnitIncrement(20);
            SceneChangeListener scListener = new SceneChangeListener(getDiagramDO(), scene);
            scene.addObjectSceneListener(scListener, 
                                         ObjectSceneEventType.OBJECT_ADDED,
                                         ObjectSceneEventType.OBJECT_REMOVED);
            scene.addObjectSceneListener(new SceneSelectionListener(), 
                                         ObjectSceneEventType.OBJECT_SELECTION_CHANGED);
        }

//        SwingPaletteManager contextPalette = new SwingPaletteManager(scene, diagramView);
//        scene.setContextPaletteManager(contextPalette);
    }

    protected void initLookup()
    {
        paletteController = getAssociatedPalette();
        lookupContent.add(paletteController);
        lookupContent.add(scene);
        lookupContent.add(getDiagramDO());
        lookupContent.add(getNavigatorCookie());
        lookupContent.add(editorToolbar);
        lookupContent.add(zoomManager);
    }

    private UMLDiagramDataObject getDiagramDO(String name, INamespace space)
    {
        if (diagramDO == null)
        {
            String fileName = getFullFileName(name, space);
            try
            {
                if (fileName != null && fileName.trim().length() > 0)
                {
                    File diagramFile = new File(fileName);
                    FileObject diagramFO = FileUtil.createData(diagramFile);
                    if (diagramFO != null)
                    {
                        DataObject dobj = DataObject.find(diagramFO);
                        if (dobj != null)
                        {
                            diagramDO = (UMLDiagramDataObject) dobj;
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                Exceptions.printStackTrace(ex);
            }
        }

        return diagramDO;
    }

    private UMLDiagramDataObject getDiagramDO()
    {

        return diagramDO;
    }

    protected DesignerScene getDiagram()
    {
        return scene;
    }

    /**
     * Creates a fullpath filename for this diagram
     *
     * @param sName [out,retval] The full path filename of this diagram.
     */
    private String getFullFileName(String name, INamespace space)
    {
        String retFileName = null;
        // If the user enters a filename then use that as the proposed filename.
        // If the filename is not absolute then we use the project directory
        // as the location of the file.  If, for some reason, the filename argument
        // is 0 then we use the name of the diagram as the name of the file and the
        // workspace location for the directory.
        if (name != null && name.length() > 0)
        {
            long timeInMillis = System.currentTimeMillis();

            // To avoid conflicts between filenames, esp for large groups that are
            // using an SCC to manage their model we append a timestamp to the
            // file name.
            retFileName = name + "_" + timeInMillis + FileExtensions.DIAGRAM_LAYOUT_EXT;
        }

        // Make sure we have a legal file.  If it is just a name then add the
        // .etld extension and put it in the same spot as the workspace.
        String buffer = retFileName;
        String drive = null;
        int pos = buffer.indexOf(":");
        if (pos >= 0)
        {
            drive = buffer.substring(0, pos);
        }

        if (drive == null && space != null)
        {
            // Assume we don't have a path and create one from the project directory
            IProject proj = space.getProject();
            if (proj != null)
            {
                String fileName = proj.getFileName();
                if (fileName != null && fileName.length() > 0)
                {
                    try
                    {
                        fileName = (new File(fileName)).getCanonicalPath();
                        int posSlash = fileName.lastIndexOf(File.separator);
                        if (posSlash >= 0)
                        {
                            fileName = fileName.substring(0, posSlash + 1);
                            fileName += buffer;
                            buffer = fileName;
                        }
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        }

        //        if (!buffer.endsWith(FileExtensions.DIAGRAM_LAYOUT_EXT))
        //        {
        //            buffer += FileExtensions.DIAGRAM_LAYOUT_EXT;
        //        }
        return buffer;
    }

    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setOpaque(false);
        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    
    protected void initializeToolBar()
    {
        DiagramEngine engine = scene.getEngine();
        if(engine != null)
        {
            engine.buildToolBar(editorToolbar, getLookup());
        }
    }
    
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized UMLDiagramTopComponent getDefault()
    {
        if (instance == null)
        {
            instance = new UMLDiagramTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the UMLDiagramTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized UMLDiagramTopComponent findInstance()
    {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null)
        {
            Logger.getLogger(UMLDiagramTopComponent.class.getName()).warning("Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof UMLDiagramTopComponent)
        {
            return (UMLDiagramTopComponent) win;
        }
        Logger.getLogger(UMLDiagramTopComponent.class.getName()).warning("There seem to be multiple components with the '" + PREFERRED_ID + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }


    /** replaces this in object stream */
    @Override
    public Object writeReplace()
    {
        return new ResolvableHelper();
    }

    protected List<IPresentationElement> getPresentationElements(IElement element)
    {
        List<IPresentationElement> retVal = new ArrayList<IPresentationElement>();

        if (element != null)
        {
            // temp solution to fire events when constraint is changed
            if (element instanceof IConstraint)
            {
                ArrayList<IPresentationElement> pes = new ArrayList<IPresentationElement>();
                ETList<IElement> list = ((IConstraint) element).constrainedElements();

                for (IElement e : list)
                {
                    pes.addAll(e.getPresentationElements());
                }
                return pes;
            }
            retVal = element.getPresentationElements();

        // what's the reason for that?? in case presentation element of an element is 
        // deleted, it finds its parent's pe and then delete it, very dangerous. It needs
        // to be reviewed.
//            if((retVal == null) || (retVal.size() == 0))
//            {
//                retVal = getPresentationElements(element.getOwner());
//            }
        }

        return retVal;
    }
    
    
    public JPanel getDiagramAreaPanel()
    {
        return jPanel1;
    }
    
    protected JScrollPane getScrollPane()
    {
        return jScrollPane1;
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
    
    
    
    private void updateActions(final boolean selection)
    {
       
        if (java.awt.EventQueue.isDispatchThread())
        {
            updateActionsInAwtThread(selection);
        } else
        {
            java.awt.EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    updateActionsInAwtThread(selection);
                }
            });
        }
    }

    private void updateActionsInAwtThread(boolean selection)
    {
        if (scene == null)
        {
            return;
        }
        
        // Default to read-only mode.  If we are not read-only then the next
        // conditional statement will set it to the selection value.
        cutActionPerformer.setEnabled(false);
        deleteActionPerformer.setEnabled(false);
        pasteActionPerformer.setEnabled(false);
        
        if(getDiagram().isReadOnly() == false)
        {
            cutActionPerformer.setEnabled(selection);
            deleteActionPerformer.setEnabled(selection);
        }
        copyActionPerformer.setEnabled(selection);
        

        Widget w = null;
        Set selected = scene.getSelectedObjects();
        if (selected.size() == 0)
        {
            w = scene;
        } 
        else if (selected.size() == 1)
        {
            Object[] s = new Object[1];
            selected.toArray(s);
            w = scene.findWidget(s[0]);
        }
        
        if (w == null)
        {
            pasteActionPerformer.setEnabled(false);
        }
        Clipboard clipboard = CopyPasteSupport.getClipboard();
        Transferable trans = clipboard.getContents(this);
        
        if(getDiagram().isReadOnly() == false)
        {
            pasteActionPerformer.setEnabled(getSceneAcceptProvider().isAcceptable(w, new Point(0, 0), trans).equals(ConnectorState.ACCEPT));
        }     
    }
    
    private SceneAcceptProvider getSceneAcceptProvider()
    {
        if (provider == null)
        {
            provider = new SceneAcceptProvider(scene.getDiagram().getNamespaceForCreatedElements());
        }
        return provider;
    }
    
    private void clearClipBoard()
    {
        pasteActionPerformer.setEnabled(false);
        Clipboard clipboard = CopyPasteSupport.getClipboard();
        clipboard.setContents(new ADTransferable(""), new StringSelection(""));
    }
    
    public void setDiagramDirty(boolean dirty)
    {
        if (getDiagramDO() != null) {
            getDiagramDO().setDirty(dirty, scene);
        }
    }
    
    //////////////////////////////////////////////////////////////////
    // Support classes.
    
    static final class ResolvableHelper implements Serializable
    {
        private static final long serialVersionUID = 1L;

        public Object readResolve()
        {
            return UMLDiagramTopComponent.getDefault();
        }
    }



    public class NavigatorHint implements NavigatorLookupHint, Node.Cookie
    {

        public String getContentType()
        {
            return "diagram/uml";
        }
    }

    private class DiagramChangeListener implements PropertyChangeListener
    {
        // XXX consider using DataEditorSupport.annotateName
        private final static String SPACE_STAR = " *";
        public void propertyChange(PropertyChangeEvent evt)
        {
            UMLDiagramDataObject dobj = getDiagramDO();
            if (!dobj.isValid())
            {   
                return;
            }
            
            if (evt.getPropertyName().equals(DataObject.PROP_MODIFIED))
            {
                IDiagram uiDiagram = getAssociatedDiagram();
                int diagramKind = uiDiagram.getDiagramKind();
                String diagramName = uiDiagram.getNameWithAlias();
                String displName = "";

                UMLDiagramDataNode diagDataNode = 
                        (UMLDiagramDataNode)dobj.getNodeDelegate();
                Object newVal = evt.getNewValue(); 
                if (newVal == Boolean.TRUE)
                {
                    displName = diagramName + SPACE_STAR;
                    dobj.addSaveCookie(scene);
                    updateSaveCookie(ADD);

                } else 
                {
                    displName = diagramName;
                    updateSaveCookie(REMOVE);
                    dobj.removeSaveCookie();
                }
               
                setDiagramDisplayName(displName);
                diagDataNode.setIconBaseWithExtension(ImageUtil.instance()
                        .getDiagramTypeImageName(diagramKind));
                    
                diagDataNode.setDisplayName(diagramName);
                diagDataNode.setValue(diagramKind);
            }
        }
    }
    
    // the presence of save cookie in activated nodes enables/disables "Save" button
    private static int ADD = 0;
    private static int REMOVE = 1;
    private void updateSaveCookie(int action)
    {
        // get diagram root node
        final Node rootNode = explorerManager.getRootContext() ;
        // get selected nodes
        Node[] nodes = getActivatedNodes();
        
        List<Node> allNodes = new ArrayList<Node>();
        allNodes.add(rootNode);
        
        if ( nodes != null && nodes.length > 0)
        {
            for (int i=0; i < nodes.length; i++) 
            {
                // check to prevent duplicate root node
                if (!nodes[i].equals(rootNode))
                {
                    allNodes.add(nodes[i]);
                }
            }
        }

        for (Node aNode : allNodes)
        {
            if (aNode != null && aNode instanceof DiagramModelElementNode)
            {
                if (action == ADD)
                {
                    ((DiagramModelElementNode)aNode).addSaveCookie();
                }
                else if (action == REMOVE)
                {
                    ((DiagramModelElementNode)aNode).removeSaveCookie();
                } 
            }
        }
    }
    
    /**
     * Notifies the design patterns that are connected to the role that changed
     *
     * @param pTargets[in] Information about the event.  Add presentation elements to be notified through
     * this interface.
     * @param pDiagram[in] The diagram we're associated with
     * @param pConnectableElement [in] The role that was modified
     */
    protected void addDesignPatterns(List<IPresentationElement> presentations, IConnectableElement connect)
    {
        if ((connect != null) && (presentations != null))
        {
            // Find all the roles this guy plays a part in and notify the
            // contexts - these contexts should be the collaborations.
            List<IStructuredClassifier> classifiers = connect.getRoleContexts();
            if (classifiers != null)
            {
                for (IStructuredClassifier curClassifier : classifiers)
                {
                    if (curClassifier != null)
                    {
                        List<IPresentationElement> p 
                            = getPresentationElements(curClassifier);
                        presentations.addAll(p);
                    }
                }
            }
        }
    }


    public class SceneSelectionListener extends ObjectSceneListenerAdapter
    {
        @Override
         public void selectionChanged(ObjectSceneEvent event, 
                                     Set<Object> previousSelection, 
                                     Set<Object> newSelection)
        {
            getDiagram().getScene().getView().requestFocusInWindow();
            boolean needEnable=(newSelection != null) && (newSelection.size() > 0);
            if(needEnable)
            {
                if(newSelection.size()==1)
                {
                    //it may be selected only one special element
                    Object selectedO=newSelection.iterator().next();
                    if(selectedO instanceof IPresentationElement)
                    {
                        Widget selW=scene.findWidget((IPresentationElement) selectedO);
                        if(selW instanceof UMLNodeWidget && !((UMLNodeWidget)selW).isCopyCutDeletable())needEnable=false;
                        ContextPaletteManager manager = scene.getContextPaletteManager();
                        if(manager != null)
                        {
                            // Make sure that the palette is correctly placed for the 
                            // zoom level.
                            manager.cancelPalette();
                            manager.selectionChanged(null);
                        }
                    }
                }
            }
            if(needEnable || (newSelection != null && newSelection.size() == 1))
            {
                // enable copy/cut/delete action
                updateActions(needEnable);
               
                List < DiagramModelElementNode > nodeList = new ArrayList < DiagramModelElementNode >();
                
                for(Object curSelected : newSelection)
                {
                    DiagramModelElementNode node = null;
                    if(curSelected instanceof IPresentationElement)
                    {
                        IPresentationElement presenation = (IPresentationElement) curSelected;
                        node = new DiagramModelElementNode(getDiagramDO());

                        IElement element = presenation.getFirstSubject();
                        node.setElement(element); 
                        node.setScene(scene);
                        node.setPresentationElement(presenation);

                        // if the element has not been named by users, the default element
                        // name is the element type; Otherwise, the customed name is used.
                        node.setName(presenation.getElementType());

                        if (element instanceof INamedElement)
                        {
                            String name = ((INamedElement) element).getName(); 

                            if (name != null && !name.trim().equals(""))
                            {
                                node.setName(name);
                            }
                            else
                            {
                                // Fixed issue 78484. Display the expanded name as the default
                                // name for elements that extend IAssociation, namely,
                                // aggregation and composition.
                                if (element instanceof IAssociation)
                                {
                                    String expandedName = element.getExpandedElementType();
                                    if (expandedName != null && expandedName.trim().length() > 0)
                                    {
                                        node.setName(expandedName.replace('_', ' '));
                                    }
                                }
                                else {  // displayed the element type name, e.g. Class, Interface, InvocationNode...
                                        node.setName(((INamedElement) element).getElementType());
                                }
                            }
                        }
                        else if (element instanceof IDiagram)
                        {
                            node.setName(((IDiagram) element).getName());
                        }
                    }
                    else if (curSelected instanceof ModelElementBridge)
                    {
                        ModelElementBridge bridge = (ModelElementBridge) curSelected;
                        node = new DiagramModelElementNode(getDiagramDO());
                        
                        node.setElement(bridge.getElement());
                        node.setScene(scene);
                    }

                    if(node != null)
                    {
                        nodeList.add(node);
                    }
                }
                
                final DiagramModelElementNode[] nodes = new DiagramModelElementNode[nodeList.size()];
                nodeList.toArray(nodes);
                SwingUtilities.invokeLater(new Runnable() {

                    public void run()
                    {
                        setActivatedNodes(nodes);                        
                    }
                });
                
            }
            else
            {
                // disable copy/cut/delete action
                updateActions(false);
                
//                DiagramModelElementNode node = new DiagramModelElementNode(getDiagramDO());
//                node.setElement(getDiagram().getDiagram());
//                node.setScene(scene);
                
                final Node[] nodes = { explorerManager.getRootContext() };
                
                SwingUtilities.invokeLater(new Runnable() {

                    public void run()
                    {
                        setActivatedNodes(nodes); 
                    }
                });
                
            }
        }                 
    }
    
     private class ClipboardChangesListener implements ClipboardListener {
        public void clipboardChanged(ClipboardEvent ev) {
            if (!ev.isConsumed())
            {             
                if (scene != null)
                {
                    Set selected=scene.getSelectedObjects();
                    if(selected.size()==1)
                    {
                        //it may be special e;ement on the scene (usually only one like interaction boundary)
                        Widget selW=scene.findWidget(selected.iterator().next());
                        if(selW instanceof UMLNodeWidget)
                        {
                            if(!((UMLNodeWidget)selW).isCopyCutDeletable())selected=Collections.emptySet();//it will not be possible to copy or cut special element
                        }
                    }                    
                    updateActions(selected.size()>0);
                }
            }
        }
    }
    
     
   
        // performer for DeleteAction
    private class DeleteActionPerformer extends javax.swing.AbstractAction
                                        implements Mutex.Action
    {
        private ArrayList<Node> nodesToDestroy;

        public void actionPerformed(ActionEvent ev) {
            Node[] selected = getActivatedNodes();

            if (selected == null || selected.length == 0)
                return;
            
            nodesToDestroy=new ArrayList<Node> ();

            for (int i=0; i < selected.length; i++)
            {
                Node.Cookie cookie = selected[i].getCookie(IPresentationElement.class);
                if (cookie == null)
                    continue;
                cookie = selected[i].getCookie(IDiagram.class);
                if (cookie != null)
                    continue;
                nodesToDestroy.add(selected[i]);
            }
            if(nodesToDestroy.size()==0)return;
            if (EventQueue.isDispatchThread())
                doDelete();
            else // reinvoke synchronously in AWT thread
                Mutex.EVENT.readAccess(this);
        }

        public Object run() {
            doDelete();
            return null;
        }

        private void doDelete()
        {
            if (nodesToDestroy.size() > 0)
            {
                String title = NbBundle.getMessage(ElementDeletePanel.class,
                                                   "DELETE_QUESTIONDIALOGTITLE"); // NO18N

                boolean displayRemove = false;
                List<Node> a = nodesToDestroy;
                // if there is one node in the selection that is imported element we display 
                // checkbox to allow user to remove it from imported list 
                for (Node node : a)
                {
                    IPresentationElement pe = node.getCookie(IPresentationElement.class);
                    if (pe.getFirstSubject().getProject() != getDiagram().getDiagram().getProject())
                    {
                        displayRemove = true;
                        break;
                    }
                }
                ElementDeletePanel panel = new ElementDeletePanel(displayRemove);
                DialogDescriptor dialogDescriptor = new DialogDescriptor(panel,
                                                                         title,
                                                                         true,
                                                                         NotifyDescriptor.YES_NO_OPTION,
                                                                         NotifyDescriptor.YES_OPTION,
                                                                         null);
                dialogDescriptor.setMessageType(NotifyDescriptor.QUESTION_MESSAGE);

                Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
                dialog.getAccessibleContext().setAccessibleDescription(title);

                dialog.setVisible(true);
                try
                {
                    if (dialogDescriptor.getValue() == DialogDescriptor.YES_OPTION)
                    {
                        for (Node node : a)
                        {
                            IPresentationElement pe = node.getCookie(IPresentationElement.class);
                            Widget widget = scene.findWidget(pe);
                            if (widget instanceof UMLNodeWidget && !((UMLNodeWidget) widget).isCopyCutDeletable())
                            {
                                continue;
                            }
                            Widget sourceEnd = null;
                            Widget targetEnd = null;
                            if (widget instanceof UMLWidget)
                            {
                                if (widget instanceof ConnectionWidget)
                                {
                                    ConnectionWidget edge = (ConnectionWidget) widget;
                                    sourceEnd = edge.getSourceAnchor().getRelatedWidget();
                                    targetEnd = edge.getTargetAnchor().getRelatedWidget();
                                }
                                ((UMLWidget) widget).remove();
                            }

                            boolean deleteFromModel = panel.getDeleteFromOriginal();//result.isChecked();

                            DiagramEngine engine = scene.getEngine();
                            if (engine != null)
                            {

                                RelationshipFactory factory = engine.getRelationshipFactory(pe.getFirstSubjectsType());
                                if ((factory != null) && (sourceEnd != null) && (targetEnd != null))
                                {
                                    IPresentationElement sourceElement = (IPresentationElement) scene.findObject(sourceEnd);
                                    IPresentationElement targetElement = (IPresentationElement) scene.findObject(targetEnd);
                                    factory.delete(deleteFromModel, pe,
                                                   sourceElement.getFirstSubject(),
                                                   targetElement.getFirstSubject());
                                } else if (deleteFromModel == true)
                                {
                                    // element will also be deleted from imported list from other projects
                                    IElement data = node.getCookie(IElement.class);
                                    if (data != null)
                                    {
                                        data.delete();
                                    }
                                } else if (panel.getRemoveFromImport())
                                {
                                    IElement e = node.getCookie(IElement.class);
                                    getDiagram().getDiagram().getProject().removeElementImport(e);
                                }

                                pe.delete();

                                // We need to clear the clipboard even if the 
                                // factory was used to delete the model element.
                                // Therefore I am going to have to make the check
                                // again.
                                if (deleteFromModel == true)
                                {
                                    clearClipBoard();
                                }
                            }
                        }
                    }
                } finally
                {
                    dialog.dispose();
                    nodesToDestroy = null;
                }
            }
        }
    }
    
    // performer for CopyAction and CutAction
    private class CopyCutActionPerformer extends javax.swing.AbstractAction                                     
    {
        private boolean copy;

        public CopyCutActionPerformer(boolean copy) {
            this.copy = copy;
        }

        public void actionPerformed(ActionEvent e) {       
            ADTransferable trans;
            Node[] selected = getActivatedNodes();
            
            if(selected.length==1)
            {
                //it may be special e;ement on the scene (usually only one like interaction boundary)
                Widget selW=scene.findWidget(selected[0]);
                if(selW instanceof UMLNodeWidget)
                {
                    if(!((UMLNodeWidget)selW).isCopyCutDeletable())selected=new Node[0];//it will not be possible to copy or cut special element
                }
            }

            if (selected == null || selected.length == 0)
                trans = null;
     
            trans = getTransferable(selected);

            if (trans != null) {
                Clipboard clipboard = CopyPasteSupport.getClipboard();
                clipboard.setContents(trans, new StringSelection("")); // NOI18N
            }
            if (!copy)
            {
                trans.setTransferType(ADTransferable.CUT);
            }
                
        }

        private ADTransferable getTransferable(Node[] nodes)
        {
            ADTransferable retVal = new ADTransferable("DRAGGEDITEMS"); // NOI18N
            retVal.setDiagramEngine(scene.getEngine());
            for (Node n : nodes)
            {
                IPresentationElement e = n.getCookie(IPresentationElement.class);
                if (e != null)
                {
                    retVal.addPresentationElement(e);
                }
            }
            
            return retVal;
        }
    }

    private class PasteActionPerformer extends javax.swing.AbstractAction     
    {
        
        public void actionPerformed(ActionEvent e)     
        {         
            scene.setCursor(CopyPasteSupport.PasteCursor);
            scene.getView().setCursor(CopyPasteSupport.PasteCursor);
        }
    }
    

     
    private class EngineDrawingAreaSink extends DrawingAreaEventsAdapter
    {

        @Override
        public void onDrawingAreaPostPropertyChange(IProxyDiagram pProxyDiagram,
                                                    int pPropertyKindChanged,
                                                    IResultCell cell)
        {
            if ( pProxyDiagram != null)
            {
                IDiagram diagram = pProxyDiagram.getDiagram();
                IDiagram thisDiagram = getAssociatedDiagram();

                if (pPropertyKindChanged == DiagramAreaEnumerations.DAPK_NAME &&
                        diagram != null && diagram.isSame(thisDiagram))
                {
                    String newName = diagram.getName();
                    if (newName != null && newName.trim().length() > 0 )
                    {
                        setName();
                        setDiagramDirty(true);
                    }
                }
            }
        }
    }

    public void mouseClicked(MouseEvent e)
    {      
    }

    public void mousePressed(MouseEvent e)
    {       
    }

    public void mouseReleased(MouseEvent e)
    {        
    }

    public void mouseEntered(MouseEvent e)
    {        
    }

    public void mouseExited(MouseEvent e)
    {
        scene.removeBackgroundWidget();
    }
}
