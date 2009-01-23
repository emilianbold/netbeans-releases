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
package org.netbeans.modules.uml.drawingarea.view;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JViewport;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.EventProcessingType;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.drawingarea.ShapeUniqueAnchor;
import org.netbeans.modules.uml.drawingarea.UIDiagram;
import org.netbeans.modules.uml.drawingarea.UMLDiagramTopComponent;
import org.netbeans.modules.uml.drawingarea.actions.DiagramRectangularSelectDecorator;
import org.netbeans.modules.uml.drawingarea.actions.DiagramSceneRectangularSelectProvider;
import org.netbeans.modules.uml.drawingarea.actions.MarqueeZoomSelectProvider;
import org.netbeans.modules.uml.drawingarea.actions.UMLRectangularSelectAction;
import org.netbeans.modules.uml.drawingarea.engines.DiagramEngine;
import org.netbeans.modules.uml.drawingarea.engines.DiagramEngineFactory;
import org.netbeans.modules.uml.drawingarea.palette.context.ContextPaletteManager; 
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramNodeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.NodeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceUtil;
import org.netbeans.modules.uml.drawingarea.ui.addins.diagramcreator.SQDDiagramEngineExtension;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jyothi
 */
public class DesignerScene extends GraphScene<IPresentationElement, IPresentationElement> implements DiagramNodeWriter//, DiagramNodeReader 
{
    
    
    private static final BasicStroke ALIGN_STROKE = new BasicStroke (1.0f, 
                                                               BasicStroke.JOIN_BEVEL, 
                                                               BasicStroke.CAP_BUTT, 
                                                               5.0f, 
                                                               new float[] { 6.0f, 3.0f }, 0.0f);
    
    private LayerWidget mainLayer;
    private LayerWidget connectionLayer;
    private LayerWidget interractionLayer;
    private LayerWidget rapidButtonsLayer;
    private LayerWidget backgroundLayer;
    private JComponent satelliteView;
    private DiagramEngine engine;
    private UMLNodeWidget backgroundWidget;
    
    private TopComponent topcomponent;
    
    private HashMap < Widget, Anchor > anchorMap = new HashMap < Widget, Anchor >();
    private boolean groupedEdges = false;


    // Data
    private ContextPaletteManager paletteManager = null;

    // Lookup Data
    private InstanceContent lookupContent = new InstanceContent();
    private AbstractLookup lookup = new AbstractLookup(lookupContent);

    private IDiagram diagram = null;
    
    private Router edgeRouter;
    private Router selfLinkRouter;
    public static String SceneDefaultWidgetID = "default";
    
    private ArrayList < IPresentationElement > lockedSelected = 
            new ArrayList < IPresentationElement >();
    
    /**
     * The visual library uses a HashSet to manage the selected objects.  The 
     * problem is that the HashSet uses the hash code to determine the order of 
     * the elements in the list.  Therefore, the getSelectedObject does not 
     * return a list in the order in which they where selected.  
     * 
     * Since some operations require the list to be in the order of the selection
     * I am going to create a second list that can be accessed for the 
     * special operations that need a list that contains the selected order.
     */
    private ArrayList < IPresentationElement > selectedElements = 
            new ArrayList < IPresentationElement >();
            

    public DesignerScene(IDiagram diagram,UMLDiagramTopComponent topcomponent)
    {
        setResourceTable(ResourceValue.createChildResourceTable());
        ResourceValue.initResources(SceneDefaultWidgetID, this);
        
        this.topcomponent=topcomponent;
        mainLayer = new LayerWidget(this);
        connectionLayer = new LayerWidget(this);
        interractionLayer = new LayerWidget(this);
        rapidButtonsLayer = new LayerWidget(this);
        backgroundLayer = new LayerWidget(this);
        
        initDefaultResources();
        
        if(diagram instanceof UIDiagram)
        {
            ((UIDiagram)diagram).setScene(this);
        }

        addChild(mainLayer);
        addChild(interractionLayer);
        addChild(connectionLayer);
        addChild(rapidButtonsLayer);
        addChild(backgroundLayer);

        satelliteView = this.createSatelliteView();
        
        initLookup();
        
        this.diagram = diagram;

        engine=attachEngine(diagram);
        engine.setActions(this);
        engine.setSelectionManager(this);
        engine.setTopComponent(topcomponent);
        edgeRouter = engine.getEdgeRouter(connectionLayer);
        
        if(engine.getDefaultLayout()!=null)
        {
            //all meaningful widgeta are on main layer
            getMainLayer().setLayout(engine.getDefaultLayout());
        }
        
        addToLookup(diagram);
        addToLookup(engine);
        if(paletteManager != null)
        {
            addToLookup(paletteManager);
        }
        
        setActiveTool(DesignerTools.SELECT);
        setKeyEventProcessingType (EventProcessingType.FOCUSED_WIDGET_AND_ITS_CHILDREN_AND_ITS_PARENTS);
        
        setAccessibleContext(new UMLWidgetAccessibleContext(this));
    }
    
    
    public void initDefaultResources()
    {
//        ResourceValue.initResources(SceneDefaultWidgetID, this);
        revalidate();
        repaint();
        validate();
    }

    public WidgetAction createRectangularSelectAction()
    {
        return ActionFactory.createRectangularSelectAction
                (new DiagramRectangularSelectDecorator(this, DesignerTools.SELECT), 
                backgroundLayer, new DiagramSceneRectangularSelectProvider(this));
    }
    
    public WidgetAction createMarqueeSelectAction()
    {
        return new UMLRectangularSelectAction(
                new DiagramRectangularSelectDecorator(this, DesignerTools.MARQUEE_ZOOM), 
                backgroundLayer, new MarqueeZoomSelectProvider(this));
    }
    
    
    
    ///////////////////////////////////////////////////////////////
    // Lookup Methods
    
    @Override
    public Lookup getLookup()
    {
        return lookup;
    }

    protected void initLookup()
    {
    }
    
    ///////////////////////////////////////////////////////////////
    // Data Access Methods
    
    public IDiagram getDiagram()
    {
        return diagram;
    }
    
    public boolean isReadOnly()
    {
        return diagram.getReadOnly();
    }
    
    public LayerWidget getMainLayer()
    {
        return mainLayer;
    }

    public LayerWidget getConnectionLayer()
    {
        return connectionLayer;
    }

    public LayerWidget getInterractionLayer()
    {
        return interractionLayer;
    }

    public LayerWidget getRapidButtonsLayer()
    {
        return rapidButtonsLayer;
    }

    public JComponent getSatelliteView()
    {
        return satelliteView;
    }

    public void setContextPaletteManager(ContextPaletteManager manager)
    {
        if(paletteManager != null)
        {
            removeFromLookup(paletteManager);
        }
        
        paletteManager = manager;
        addToLookup(paletteManager);
    }

    public ContextPaletteManager getContextPaletteManager()
    {
        return paletteManager;
    }
    
    public DiagramEngine getEngine()
    {
        return engine;
    }

    public TopComponent getTopComponent()
    {
        return topcomponent;
    }
    ///////////////////////////////////////////////////////////////
    // Graph Scene Implementation
    static int number = 0;

    protected Widget attachNodeWidget(IPresentationElement node)
    {
        UMLNodeWidget widget = (UMLNodeWidget) engine.createWidget(node);

        if(widget!=null)
        {
            mainLayer.addChild(widget);

            engine.setActions(widget, node);

            widget.initializeNode(node);
        }
        else
        {
//            System.out.println("***WARNING: can't create "+node.getFirstSubjectsType());
        }
        
        return widget;
    }

    protected Widget attachEdgeWidget(IPresentationElement edge)
    {
        UMLEdgeWidget connection = (UMLEdgeWidget)engine.createConnectionWidget(this, edge);
        
        if(edgeRouter != null && connection != null)
        {
                connection.setRouter(edgeRouter);
                connection.setRoutingPolicy (ConnectionWidget.RoutingPolicy.ALWAYS_ROUTE);
        }
        if(connection!=null)
        {
            connectionLayer.addChild(connection);
            engine.setActions(connection,edge);
            connection.initialize(edge);
        }
        return connection;
    }

    protected void attachEdgeSourceAnchor(IPresentationElement edge, 
                                          IPresentationElement oldSourceNode, 
                                          IPresentationElement sourceNode)
    {
        ConnectionWidget widget = (ConnectionWidget) findWidget(edge);
        //widget.setSourceAnchor(AnchorFactory.createRectangularAnchor(findWidget(sourceNode)));
        
        Widget sourceWidget = findWidget(sourceNode);
        if(sourceWidget != null)
        {
            Anchor anchor = getAnchorFor(sourceWidget);
            widget.setSourceAnchor(anchor);
        }
        if (isSelfLink(widget))
            setSelfLinkRouter(widget);
    }

    protected void attachEdgeTargetAnchor(IPresentationElement edge, 
                                          IPresentationElement oldTargetNode, 
                                          IPresentationElement targetNode)
    {
        ConnectionWidget widget = (ConnectionWidget) findWidget(edge);
//        widget.setTargetAnchor(AnchorFactory.createRectangularAnchor(findWidget(targetNode)));
        
        Widget targetWidget = findWidget(targetNode);
        if(targetWidget != null)
        {
            Anchor anchor = getAnchorFor(targetWidget);
            widget.setTargetAnchor(anchor);
        }
        if (isSelfLink(widget))
            setSelfLinkRouter(widget);
    }

    
    private boolean isSelfLink(ConnectionWidget connection)
    {
        Anchor sourceAnchor = connection.getSourceAnchor();
        Anchor targetAnchor = connection.getTargetAnchor();
        if (sourceAnchor != null && targetAnchor != null && sourceAnchor.getRelatedWidget() == targetAnchor.getRelatedWidget())
            return true;
        else
            return false;
    }
    
    private void setSelfLinkRouter(ConnectionWidget connection)
    {
        if (selfLinkRouter == null)
        {
            //selfLinkRouter = RouterFactory.createOrthogonalSearchRouter(connectionLayer);
            selfLinkRouter = new SelfLinkRouter();
        }
        connection.setRouter(selfLinkRouter);
        connection.setRoutingPolicy(ConnectionWidget.RoutingPolicy.ALWAYS_ROUTE);
        WidgetAction.Chain chain = connection.getActions(DesignerTools.SELECT);
        chain.removeAction(ActionFactory.createFreeMoveControlPointAction());
    }
    
    
    protected Anchor getAnchorFor(Widget widget)
    {
        Anchor retVal = anchorMap.get(widget);
        
        if((retVal == null) && (isEdgesGrouped() == true))
        {
            //retVal = new RectangularUniqueAnchor(widget, false);
            retVal = new ShapeUniqueAnchor(widget, false);
            anchorMap.put(widget, retVal);
        }
        else if(retVal == null)
        {
            retVal = AnchorFactory.createRectangularAnchor(widget);
            anchorMap.put(widget, retVal);
        }
        
        return retVal;
    }

    public boolean isEdgesGrouped()
    {
        return groupedEdges;
    }

    public void setEdgesGrouped(boolean groupedEdges)
    {
        this.groupedEdges = groupedEdges;
    }
    
    
    
    public Router createEdgeRouter()
    {
        return edgeRouter;
    }

    protected void addToLookup(Object item)
    {
        lookupContent.add(item);
    }
    
    protected void removeFromLookup(Object item)
    {
        lookupContent.remove(item);
    }
    
    private DiagramEngine attachEngine(IDiagram diagram) {
        String path="UML/DiagramEngines/"+diagram.getDiagramKindAsString().replaceAll(" ", "");
        DiagramEngine ret=null;
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
                    //if (ic == null) continue;
                    Object instance;
                    try
                    {
                        instance = ic.instanceCreate();
                    }
                    catch (IOException ex)
                    {
                        ex.printStackTrace();
                        continue;
                    }
                    catch (ClassNotFoundException ex)
                    {
                        ex.printStackTrace();
                        continue;
                    }
                    
                    if (instance instanceof DiagramEngineFactory)
                    {
                        DiagramEngineFactory factory = (DiagramEngineFactory)instance;
                        ret = factory.createEngine(this);
                        break;
                    }
                 }
            }
        }
        //which way to use? assign here or use return value
        this.engine=ret;

        return ret;
    }


    public void save(NodeWriter nodeWriter) {
        nodeWriter.setRootNode(true); //This IS a DIAGRAM/SCENE
        nodeWriter.setZoom(this.getZoomFactor());
        nodeWriter.setViewport(new Point(this.getView().getX(), this.getView().getY()));
        nodeWriter.setPresentation("");
        nodeWriter.setTypeInfo(this.getDiagram().getDiagramKindAsString());
        nodeWriter.setPEID(this.getDiagram().getXMIID());
        nodeWriter.setDiagramName(this.getDiagram().getName());
        nodeWriter.setDiagramNamespace(this.getDiagram().getNamespace().getXMIID());
        nodeWriter.setProjectID(this.getDiagram().getProject().getXMIID());
        nodeWriter.setLocation(this.getLocation());
        nodeWriter.setSize(this.getBounds().getSize());
        nodeWriter.setHasPositionSize(true);
        //populate properties key/val
        HashMap<String, String> properties = new HashMap();
        if (getEngine() instanceof SQDDiagramEngineExtension) {
            String msgNumKey = ((SQDDiagramEngineExtension) getEngine()).SHOW_MESSAGE_NUMBERS;
            Boolean showMsgNumbers = (Boolean) getEngine().getSettingValue(msgNumKey);
            properties.put(msgNumKey, showMsgNumbers.toString());

            String retMsgKey = ((SQDDiagramEngineExtension) getEngine()).SHOW_RETURN_MESSAGES;
            Boolean showReturnMsgKey = (Boolean) getEngine().getSettingValue(retMsgKey);
            properties.put(retMsgKey, showReturnMsgKey.toString());
        }
        nodeWriter.setProperties(properties);
        PersistenceUtil.populateProperties(nodeWriter, this);
        nodeWriter.beginWriting();
    }

    public void saveChildren(Widget widget, NodeWriter nodeWriter) {
        //not applicable
    }
    
    public void setBackgroundWidget(IPresentationElement element, Point point, String view)
    {
        removeBackgroundWidget();
        
        Widget w = engine.createWidget(element);

        if (w instanceof UMLNodeWidget)
        {
            backgroundWidget = (UMLNodeWidget) w;
            backgroundLayer.addChild(backgroundWidget);
            backgroundWidget.initializeNode(element);
            WidgetViewManager viewManager = backgroundWidget.getLookup().lookup(WidgetViewManager.class);
            if (viewManager != null)
            {
                if (!view.equals(""))
                    viewManager.switchViewTo(view);
            }
            backgroundWidget.setPreferredLocation(point);
            validate();
        }
    }
    
    public Widget getBackgroundWidget()
    {
        return backgroundWidget;
    }
    
    public void removeBackgroundWidget()
    {
        if (backgroundWidget == null)
            return;
        backgroundWidget.removeFromParent();
        backgroundWidget = null;
        validate();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Helper Methods
    
    /**
     * Retrieves all of the edges that are in a specified area.  The area is 
     * specified in screen coordinates.
     * 
     * @param sceneSelection The area that must contain the edges.
     * @return The edges in the specified area.
     */
    public Set <IPresentationElement> getEdgesInRectangle(Rectangle sceneSelection,
                                                          boolean canIntersect)
    {
        Set < IPresentationElement > retVal = new HashSet < IPresentationElement >();
        for(IPresentationElement item : getGraphObjectInRectangle(sceneSelection, 
                                                                  true, 
                                                                  canIntersect))
        {
            if(isEdge(item) == true)
            {
                retVal.add(item);
            }
        }
        
        return retVal;
    }
    
    /**
     * Retrieves all of the Nodes that are in a specified area.  The area is 
     * specified in screen coordinates.
     * 
     * @param sceneSelection The area that must contain the nodes.
     * @return The edges in the specified nodes.
     */
    public Set <IPresentationElement> getNodesInRectangle(Rectangle sceneSelection)
    {
        Set < IPresentationElement > retVal = new HashSet < IPresentationElement >();
        for(IPresentationElement item : getGraphObjectInRectangle(sceneSelection, 
                                                                  true, false))
        {
            if(isNode(item) == true)
            {
                retVal.add(item);
            }
        }
        
        return retVal;
    }
    
    /**
     * Retrieves all of the nodes and edges that are in a specified area.  The area is 
     * specified in screen coordinates.
     * 
     * @param sceneSelection The area that must contain the nodes and edges.
     * @param containedOnly Only select nodes and edges that are fully contained.
     * 
     * @return The nodes and edges in the specified area.
     */
    public Set <IPresentationElement> getGraphObjectInRectangle(Rectangle sceneSelection,
                                                                boolean intersectNodes,
                                                                boolean intersectEdges)
    {
        //boolean entirely = sceneSelection.width > 0;
        int w = sceneSelection.width;
        int h = sceneSelection.height;
        Rectangle rect = new Rectangle(w >= 0 ? 0 : w, h >= 0 ? 0 : h, w >= 0 ? w : -w, h >= 0 ? h : -h);
        rect.translate(sceneSelection.x, sceneSelection.y);

        HashSet<IPresentationElement> set = new HashSet<IPresentationElement>();
        Set<?> objects = getObjects();
        for (Object object : objects)
        {
            boolean isEdge = isEdge(object);
            boolean isNode = isNode(object);
            if((isEdge == false) && (isNode == false))
            {
                continue;
            }
            
            Widget widget = findWidget(object);
            
            if(widget==null)continue;

            if (((isNode == true) && (intersectNodes == false)) || 
                ((isEdge == true) && (intersectEdges == false)))
            {
                // The node or edge must be entirely contained.  
                Rectangle widgetRect = widget.convertLocalToScene(widget.getBounds());
                if (rect.contains(widgetRect) && (object instanceof IPresentationElement))
                {
                    set.add((IPresentationElement)object);
                }
            }
            else
            {
                // The node or edge can intersect the rectangle.
                if (widget instanceof ConnectionWidget)
                {
                    ConnectionWidget conn = (ConnectionWidget) widget;
                    java.util.List<Point> points = conn.getControlPoints();
                    for (int i = points.size() - 2; i >= 0; i--)
                    {
                        Point p1 = widget.convertLocalToScene(points.get(i));
                        Point p2 = widget.convertLocalToScene(points.get(i + 1));
                        if (new Line2D.Float(p1, p2).intersects(rect))
                        {
                            set.add((IPresentationElement)object);
                        }
                    }
                }
                else
                {
                    Rectangle widgetRect = widget.convertLocalToScene(widget.getBounds());
                    if (rect.intersects(widgetRect))
                    {
                        set.add((IPresentationElement)object);
                    }
                }
            }
        }
        
        return set;
    }
    
    public void addLockedSelected(IPresentationElement element)
    {
        lockedSelected.add(element);
    }
    
    public void removeLockSelected(IPresentationElement element)
    {
        lockedSelected.remove(element);
    }
    
    public List < IPresentationElement > getLockedSelected()
    {
        return Collections.unmodifiableList(lockedSelected);
    }
    
    public void clearLockedSelected()
    {
        lockedSelected.clear();
    }
    
    public List < IPresentationElement > getOrderedSelection()
    {
        return selectedElements;
    }
    
    public void userSelectionSuggested (Set<?> suggestedSelectedObjects, boolean invertSelection)
    {
        List < IPresentationElement > lockedSet = getLockedSelected();

        // Build the set needed by the visual library.  Also build the ordered 
        // set at the same time.  The invert selection means to add the new
        // selection to the old selection.  So I need to keep a clone of the 
        // original order list of selected elements so they can be put back into
        // the selected elements list.
        ArrayList < IPresentationElement > oldSelection = 
                new ArrayList < IPresentationElement >(selectedElements);
        
        selectedElements.clear();
        
        HashSet < Object > selection = new HashSet < Object >();
        if(lockedSet.size() > 0)
        {
            selection.addAll(lockedSet);
            selectedElements.addAll(lockedSet);
        }
        
        selection.addAll(suggestedSelectedObjects);
        if(invertSelection == true)
        {
            selectedElements.addAll(oldSelection);
        }
        selectedElements.addAll((Collection<? extends IPresentationElement>) suggestedSelectedObjects);
        
        super.userSelectionSuggested(selection, invertSelection);
    }


}
