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
package org.netbeans.modules.uml.drawingarea.engines;
import java.awt.BasicStroke;
import java.awt.Cursor;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.animator.AnimatorEvent;
import org.netbeans.api.visual.animator.AnimatorListener;
import org.netbeans.api.visual.graph.layout.GraphLayout;
import org.netbeans.api.visual.graph.layout.GraphLayoutFactory;
import org.netbeans.api.visual.router.Router;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.drawingarea.actions.CycleObjectSceneSelectProvider;
import org.netbeans.modules.uml.drawingarea.actions.LockSelectionAction;
import org.netbeans.modules.uml.drawingarea.palette.RelationshipFactory;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.drawingarea.ConnectionWidgetFactory;
import org.netbeans.modules.uml.drawingarea.NodeWidgetFactory;
import org.netbeans.modules.uml.drawingarea.RelationshipDiscovery;
import org.netbeans.modules.uml.drawingarea.UMLDiagramTopComponent;
import org.netbeans.modules.uml.drawingarea.ZoomManager;
import org.netbeans.modules.uml.drawingarea.actions.DiagramPopupMenuProvider;
import org.netbeans.modules.uml.drawingarea.actions.DiagramSceneAcceptAction;
import org.netbeans.modules.uml.drawingarea.actions.DiagramSelectToolAction;
import org.netbeans.modules.uml.drawingarea.actions.DiscoverRelationshipAction;
import org.netbeans.modules.uml.drawingarea.actions.ExportImageAction;
import org.netbeans.modules.uml.drawingarea.actions.HierarchicalLayoutAction;
import org.netbeans.modules.uml.drawingarea.actions.InteractiveZoomAction;
import org.netbeans.modules.uml.drawingarea.actions.PanAction;
import org.netbeans.modules.uml.drawingarea.actions.SceneAcceptProvider;
import org.netbeans.modules.uml.drawingarea.actions.SyncDiagramAction;
import org.netbeans.modules.uml.drawingarea.actions.UMLHoverProvider;
import org.netbeans.modules.uml.drawingarea.actions.WidgetMoveActionMenu;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.resources.images.ImageUtil;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author sp153251
 */
abstract public class DiagramEngine {
    //
    public static final BasicStroke ALIGN_STROKE = new BasicStroke (1.0f, 
                                                               BasicStroke.JOIN_BEVEL, 
                                                               BasicStroke.CAP_BUTT, 
                                                               5.0f, 
                                                               new float[] { 6.0f, 3.0f }, 0.0f);
    
    public final static LockSelectionAction lockSelectionAction = new LockSelectionAction();
    
    // Actions
    protected WidgetAction mouseHoverAction;
    //
    protected WidgetAction sceneSelectAction = ActionFactory.createSelectAction(new DesignSelectProvider(),true);
    private DesignerScene scene;
    protected UMLDiagramTopComponent tc;
    private HashMap<String,Object> diagramSettings=new HashMap<String,Object>();//diagram specific settings, should be filled with default on diagram creation
    private PopupMenuProvider menuProvider = new DiagramPopupMenuProvider();
    
    public DiagramEngine(DesignerScene scene)
    {
        this.scene=scene;
        mouseHoverAction = ActionFactory.createHoverAction(new UMLHoverProvider());
        sceneSelectAction = scene.createSelectAction();
    }
    
    public DesignerScene getScene()
    {
        return scene;
    }
    public void setTopComponent(UMLDiagramTopComponent tc)
    {
        this.tc=tc;
    }
    public UMLDiagramTopComponent getTopComponent( )
    {
        return tc;
    }
    /**
     * Add set of actions to scene 
     */
    public void setActions(DesignerScene scene)
    {
        INamespace diagramNamespace = scene.getDiagram().getNamespaceForCreatedElements();
        AcceptProvider provider = new SceneAcceptProvider(diagramNamespace, false);
        WidgetAction acceptAction = new DiagramSceneAcceptAction(provider);
                
        WidgetAction.Chain selectTool = scene.createActions(DesignerTools.SELECT);
        selectTool.addAction(lockSelectionAction);
        selectTool.addAction(sceneSelectAction);
        selectTool.addAction(scene.createRectangularSelectAction());
        selectTool.addAction(ActionFactory.createZoomAction());
        selectTool.addAction(scene.createWidgetHoverAction());
        selectTool.addAction(acceptAction);
        selectTool.addAction(ActionFactory.createPopupMenuAction(menuProvider));
        selectTool.addAction(ActionFactory.createCycleFocusAction(new CycleObjectSceneSelectProvider()));

        WidgetAction.Chain panTool = scene.createActions(DesignerTools.PAN);
        panTool.addAction(new PanAction());
        panTool.addAction(ActionFactory.createZoomAction());
        panTool.addAction(ActionFactory.createPopupMenuAction(menuProvider));

        WidgetAction.Chain marqueeZoomTool = scene.createActions(DesignerTools.MARQUEE_ZOOM);
        marqueeZoomTool.addAction(ActionFactory.createZoomAction());
        marqueeZoomTool.addAction(scene.createMarqueeSelectAction());
        marqueeZoomTool.addAction(ActionFactory.createPopupMenuAction(menuProvider));

        WidgetAction.Chain interactiveZoomTool = scene.createActions(DesignerTools.INTERACTIVE_ZOOM);
        interactiveZoomTool.addAction(new InteractiveZoomAction());
        interactiveZoomTool.addAction(ActionFactory.createZoomAction());
        interactiveZoomTool.addAction(ActionFactory.createPopupMenuAction(menuProvider));
        
        WidgetAction.Chain contextPalette = scene.createActions(DesignerTools.CONTEXT_PALETTE);
        contextPalette.addAction(acceptAction);
        
        WidgetAction.Chain readOnly = scene.createActions(DesignerTools.READ_ONLY);
        readOnly.addAction(sceneSelectAction);
        readOnly.addAction(scene.createRectangularSelectAction());
        readOnly.addAction(ActionFactory.createZoomAction());
        readOnly.addAction(scene.createWidgetHoverAction());
        readOnly.addAction(ActionFactory.createPopupMenuAction(menuProvider));
        readOnly.addAction(ActionFactory.createCycleFocusAction(new CycleObjectSceneSelectProvider()));
    }
    /**
     * Add set of coomon actions to edges except scene, maty be specific to edge type 
     */
    abstract public void setActions(ConnectionWidget widget,IPresentationElement node);
    /**
     * Add set of coomon actions to all widgets except scene and edges, maty be specific to widget type 
     */
    abstract public void setActions(Widget widget,IPresentationElement node);
    /**
     * Add listener of selections to show context  palette
     */
    abstract public void setSelectionManager(DesignerScene scene);
    /**
     * 
     */
    abstract public Layout getDefaultLayout();
    /**
     * Create widget linket to presentation element
     */
    abstract public Widget createWidget(IPresentationElement node);
    /**
     * Adds widget with corresponding presentation element to the scene at suggested point (may place in corrected point)
     **/
    abstract public Widget addWidget(IPresentationElement presentation,Point point);
    /**
     * Show if it's possible to drop presentation element, if element requires transformation??? tbd deside
     */
    abstract public boolean isDropPossible(IPresentationElement node);
    /**
     * Show if it's possible to drop model element, cover both cases drop as is and with appropriate transform
     */
    abstract public boolean isDropPossible(INamedElement node);
    /**
     * process model element and return new element if element should be changed for example from class to lifeline
     */
    abstract public INamedElement processDrop(INamedElement elementToDrop);
    
    /**
     * Retreives the relationship discovery component used to build relationships
     * between nodes.
     */
    abstract public RelationshipDiscovery getRelationshipDiscovery();
        
    /**
     * Called after objects have been dropped onto the diagram perform
     * relationship discovery.
     *
     * @param pMEs The elements that just got dropped.
     * @param bAutoRouteEdges Should we autoroute the edges that get created during relationship
     *                        discovery
     *
     * @see org.netbeans.modules.uml.ui.swing.drawingarea.IDiagramEngine#postOnDrop(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement[], boolean)
     */
    public void postDrop(List<IElement> elements)
    {
        DesignerScene diagramScene = getScene();
            
        if ((diagramScene != null) && (elements != null))
        {
            Collection<IPresentationElement> nodes = diagramScene.getNodes();
            
            List<IElement> elementsOnDiagram = new ArrayList < IElement >();
            for(IPresentationElement node : nodes)
            {
                elementsOnDiagram.add(node.getFirstSubject());
            }

            RelationshipDiscovery relDescovery = getRelationshipDiscovery();
            if (elementsOnDiagram != null && elementsOnDiagram.size() > 0)
            {
                relDescovery.discoverCommonRelations(elements, 
                                                     elementsOnDiagram); 
            } 
            else
            {
                relDescovery.discoverCommonRelations(elements);
            }
            
            diagramScene.validate();
        }
    } 
    
    /**
     * Retrieves the edge router to use when creating new edges.
     * 
     * @param layers The layers that can contain connection widgets.
     */
    public abstract Router getEdgeRouter(LayerWidget... layers);
    
    /**
     */
    public void buildToolBar(JToolBar bar, Lookup lookup)
    {
        ZoomManager manager = lookup.lookup(ZoomManager.class);
        
        // The current API does not allow use to listen to keystrokes when the 
        // Bird Eye view is open.  Therefore we can not make the escape key work.
        // So, I am removing this feature for now.
//        BirdToggleViewAction birdAction = new BirdToggleViewAction(getScene(), manager);
//        JToggleButton birdsEyeViewBtn = new JToggleButton(birdAction);
//        bar.add(birdsEyeViewBtn);
        
        ButtonGroup selectToolBtnGroup = new ButtonGroup();
        
        JToggleButton selectToolButton = new JToggleButton(
                new DiagramSelectToolAction(getScene(), DesignerTools.SELECT, 
                    ImageUtil.instance().getIcon("selection-arrow.png"),
                    NbBundle.getMessage(DiagramSelectToolAction.class, "LBL_SelectToolAction"),
                    Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR),
                    KeyStroke.getKeyStroke("ctrl alt shift S"),
                    KeyStroke.getKeyStroke("meta ctrl shift S")));
        selectToolButton.setName(DesignerTools.SELECT);  // need a name to later identify the button
        
        JToggleButton handToolButton = new JToggleButton(
                new DiagramSelectToolAction(getScene(),
                    DesignerTools.PAN, ImageUtil.instance().getIcon("pan.png"),
                    NbBundle.getMessage(DiagramSelectToolAction.class, "LBL_HandToolAction"),
                    Utilities.createCustomCursor(scene.getView(), 
                    ImageUtilities.icon2Image(ImageUtil.instance().getIcon("pan-open-hand.gif")), "PanOpenedHand"),
                    KeyStroke.getKeyStroke("ctrl alt shift N"),
                    KeyStroke.getKeyStroke("meta ctrl shift N")));
        handToolButton.setName(DesignerTools.PAN);
        
        JToggleButton marqueeZoomButton = new JToggleButton(
                new DiagramSelectToolAction(getScene(),
                    DesignerTools.MARQUEE_ZOOM, ImageUtil.instance().getIcon("magnify.png"),  
                    NbBundle.getMessage(DiagramSelectToolAction.class, "LBL_MarqueeZoomAction"),
                    Utilities.createCustomCursor(scene.getView(), 
                    ImageUtilities.icon2Image(ImageUtil.instance().getIcon("marquee-zoom.gif")), "MarqueeZoom"),
                    KeyStroke.getKeyStroke("ctrl alt shift Z"),
                     KeyStroke.getKeyStroke("meta ctrl shift Z")));
        marqueeZoomButton.setName(DesignerTools.MARQUEE_ZOOM);
        
        JToggleButton interactiveZoomButton = new JToggleButton(
                new DiagramSelectToolAction(getScene(),
                    DesignerTools.INTERACTIVE_ZOOM, ImageUtil.instance().getIcon("interactive-zoom.png"),  
                    NbBundle.getMessage(DiagramSelectToolAction.class, "LBL_InteractiveZoomAction"),
                    Utilities.createCustomCursor(scene.getView(), 
                    ImageUtilities.icon2Image(ImageUtil.instance().getIcon("interactive-zoom.gif")), "InteractiveZoom"),
                    KeyStroke.getKeyStroke("ctrl alt shift I"),
                    KeyStroke.getKeyStroke("meta ctrl shift I")));
        interactiveZoomButton.setName(DesignerTools.INTERACTIVE_ZOOM);
                    
        JToggleButton navigateLinkButton = new JToggleButton(
                new DiagramSelectToolAction(getScene(),
                    DesignerTools.NAVIGATE_LINK, ImageUtil.instance().getIcon("navigate-link.png"),  
                    NbBundle.getMessage(DiagramSelectToolAction.class, "LBL_NavigateLinkAction"),
                    Utilities.createCustomCursor(scene.getView(), 
                    ImageUtilities.icon2Image(ImageUtil.instance().getIcon("link-navigation.gif")), "NavigateLink"),
                    KeyStroke.getKeyStroke("ctrl alt shift L"),
                    KeyStroke.getKeyStroke("meta ctrl shift L")));
        navigateLinkButton.setName(DesignerTools.NAVIGATE_LINK);
        
        selectToolBtnGroup.add(selectToolButton);
        selectToolBtnGroup.add(handToolButton);
        selectToolBtnGroup.add(marqueeZoomButton);
        selectToolBtnGroup.add(interactiveZoomButton);
        selectToolBtnGroup.add(navigateLinkButton);
        selectToolButton.setSelected(true);
        
        JButton relButtion = new JButton (new DiscoverRelationshipAction(getScene()));
        JButton syncButton = new JButton (new SyncDiagramAction(getScene()));
        JButton exportImageButton = new JButton (new ExportImageAction(getScene()));
        JButton hierarchicalLayoutButton = new JButton (new HierarchicalLayoutAction(getScene()));
        //Kris - out until layout is better
        //JButton orthogonalLayoutButton = new JButton (new OrthogonalLayoutAction(getScene()));
        
        bar.add(selectToolButton);
        bar.add(handToolButton);
        bar.add(marqueeZoomButton);
        bar.add(interactiveZoomButton);
        bar.add(navigateLinkButton);
        bar.add(new JToolBar.Separator());
        
        bar.add(relButtion);
        bar.add(syncButton);
        
        bar.add(new JToolBar.Separator());
        
        bar.add(exportImageButton);
        bar.add(new JToolBar.Separator());
        manager.addToolbarActions(bar);
        
        bar.add(new JToolBar.Separator());
        bar.add(new WidgetMoveActionMenu.MoveForward(getScene()));
        bar.add(new WidgetMoveActionMenu.MoveBackward(getScene()));
        bar.add(new WidgetMoveActionMenu.MoveToFront(getScene()));
        bar.add(new WidgetMoveActionMenu.MoveToBack(getScene()));

        bar.add(new JToolBar.Separator());
        
        bar.add(hierarchicalLayoutButton) ;
        //bar.add(orthogonalLayoutButton) ;
    }
    
    /**
     * Create a connection widget for a specific presentation element.  The
     * resulting connection widget will have the preferred edge router set as well.
     */
    abstract public ConnectionWidget createConnectionWidget(DesignerScene scene, IPresentationElement node);
    
    /**
     * 
     * @param key
     * @return
     */
    public Object getSettingValue(String key)
    {
        return diagramSettings.get(key);
    }
    
    /**
     * 
     * @param key
     * @param value
     * @return old value for the key
     */
    public Object setSettingValue(String key,Object value)
    {
        Object ret=diagramSettings.put(key, value);
        setingValueChanged(key,ret,value);
        return ret;
    }
    
    abstract protected void setingValueChanged(String key, Object oldValue, Object newValue);
    
    public static class DesignSelectProvider implements SelectProvider
    {

        public boolean isAimingAllowed(Widget widget, Point localLocation, boolean invertSelection)
        {
            return false;
        }

        public boolean isSelectionAllowed(Widget widget, Point localLocation, boolean invertSelection)
        {
            boolean retVal = false;
            if (widget instanceof Scene)
            {
                retVal = true;
            }
            else
            {
                retVal = ((DesignerScene) widget.getScene()).findObject(widget) != null;
            }
            return retVal;
        }

        public void select(Widget widget, Point localLocation, boolean invertSelection)
        {
            DesignerScene scene=((DesignerScene) widget.getScene());
            Object object = scene.findObject(widget);

            scene.setFocusedObject(object);
            if (object != null)
            {
                if (!invertSelection && scene.getSelectedObjects().contains(object))
                {
                    return;
                }
                scene.userSelectionSuggested(Collections.singleton(object), invertSelection);
            }
            else
            {
                scene.userSelectionSuggested(Collections.emptySet(), invertSelection);
            }

            if (scene.getContextPaletteManager() != null)
            {
                scene.getContextPaletteManager().selectionChanged(widget.convertLocalToScene(localLocation));
            }
        }
    }

    protected ConnectionWidget getConnectorWidget(Scene scene,String path)
    {
        ConnectionWidget retVal = null;
        
        Object instance = getInstanceFromFilesSystem(scene, path);
        if (instance instanceof ConnectionWidget)
        {
            retVal = (ConnectionWidget)instance;
        }
        
        return retVal;
               
    }
    
    protected UMLNodeWidget getWidget(Scene scene,String path) {
        UMLNodeWidget ret=null;
        
        Object instance = getInstanceFromFilesSystem(scene, path);
        if (instance instanceof UMLNodeWidget)
        {
            ret = (UMLNodeWidget)instance;
        }

        return ret;
    }

    protected Object getInstanceFromFilesSystem(Scene scene, String path)
    {
        Object retVal = null;
        
        FileSystem system = Repository.getDefault().getDefaultFileSystem();
        
        if (system != null)
        {
            FileObject fo = system.findResource(path);
            DataFolder df = fo != null ? DataFolder.findFolder(fo) : null;
            if (df != null)
            {
                DataObject[] engineObjects = df.getChildren();
                for (int i = 0; i < engineObjects.length; i++)
                {
                    InstanceCookie ic = engineObjects[i].getCookie(org.openide.cookies.InstanceCookie.class);
                    if(ic != null)
                    {
                        try
                        {
                            Object obj = ic.instanceCreate();
                            if (obj instanceof NodeWidgetFactory)
                            {
                                NodeWidgetFactory factory = (NodeWidgetFactory) obj;
                                retVal = factory.createNode(scene);
                                break;
                            }
                            else if (obj instanceof ConnectionWidgetFactory)
                            {
                                ConnectionWidgetFactory factory = (ConnectionWidgetFactory) obj;
                                retVal = factory.createConnection(scene);
                                break;
                            }
                        }
                        catch(Exception ex)
                        {
                            Exceptions.printStackTrace(ex);
                        }
                        
//                        try
//                        {
//                            Class cl = ic.instanceClass();
//                            if(cl != null)
//                            {
//                                Constructor constructor = cl.getConstructor(Scene.class);
//                                if(constructor != null)
//                                {
//                                    retVal = constructor.newInstance(scene);
//                                }
//                            }
//                        }
//                        catch (Exception e)
//                        {
//                            Exceptions.printStackTrace(e);
//                            continue;
//                        }
                    }
                    else
                    {
                        
                    }
                }
            }
        }
        
        return retVal;
    }
    
    
    
    protected String getDiagramKindName(DesignerScene scene)
    {
        IDiagram diagram = scene.getDiagram();
        String name = diagram.getDiagramKindAsString();
        return name.replaceAll(" ", "");
    }
    
    /**
     * Retrieves the RelationshipFactory for a model element the.  The 
     * relationship factory is used to control a relationships lifecycle.
     * 
     * @param type The model element type.
     * @return The relationship factory
     */
    public RelationshipFactory getRelationshipFactory(String type)
    {

        RelationshipFactory retVal = null;

        FileSystem system = Repository.getDefault().getDefaultFileSystem();

        if (system != null)
        {
            String path = "modeling/relationships/" + type + ".context_palette_item";
            FileObject fo = system.findResource(path);
            if(fo != null)
            {
                retVal = (RelationshipFactory)fo.getAttribute("factory");
            }
        }

        return retVal;
    }
    
    public void layout(boolean save)
    {
        DesignerScene diagramScene = getScene();
        GraphLayout gLayout = GraphLayoutFactory.createHierarchicalGraphLayout(diagramScene, true);
//        GraphLayout gLayout = GraphLayoutFactory.createOrthogonalGraphLayout(scene, true);
        if (save)
        {
            diagramScene.getSceneAnimator().getPreferredLocationAnimator().addAnimatorListener(new MyAnimatorListener());
        }
        gLayout.layoutGraph(diagramScene);

    }

    private class MyAnimatorListener implements AnimatorListener
    {

        public void animatorStarted(AnimatorEvent event)
        {            
        }

        public void animatorReset(AnimatorEvent event)
        {            
        }

        public void animatorFinished(AnimatorEvent event)
        {
            saveDiagram();
            scene.getSceneAnimator().getPreferredLocationAnimator().removeAnimatorListener(this);
        }

        public void animatorPreTick(AnimatorEvent event)
        {            
        }

        public void animatorPostTick(AnimatorEvent event)
        {           
        }

    }

    private void saveDiagram()
    {
        DesignerScene scene = getScene();
        if (scene != null)
        {
            IDiagram diagram = scene.getDiagram();
            if (diagram != null)
            {
                try
                {
                    diagram.setDirty(true);
                    diagram.save();
                }
                catch (IOException ex)
                {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
