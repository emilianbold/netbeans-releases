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
import java.awt.Point;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import javax.swing.JComponent;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.router.Router;
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
    public static String SceneDefaultWidgetID = "default";

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
            System.out.println("***WARNING: can't create "+node.getFirstSubjectsType());
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
        
        connectionLayer.addChild(connection);
        engine.setActions(connection,edge);
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
                    Object instance=null;
                    try
                    {
                        instance = ic.instanceClass().getConstructor(DesignerScene.class).newInstance(this) ;
                    }
                    catch (IOException e)
                    {
                        // ignore
                        e.printStackTrace();
                        continue;
                    }
                    catch (ClassNotFoundException e)
                    {
                        // ignore
                        e.printStackTrace();
                        continue;
                    }
                    catch(NoSuchMethodException ex)
                    {
                        ex.printStackTrace();
                        continue;
                    }
                    catch(InstantiationException ex)
                    {
                        ex.printStackTrace();
                        continue;
                    }
                    catch(IllegalAccessException ex)
                    {
                        ex.printStackTrace();
                        continue;
                    }
                     catch(InvocationTargetException ex)
                    {
                        ex.printStackTrace();
                        continue;
                    }
                    //
                    if (instance instanceof DiagramEngine)
                    {
                        ret=(DiagramEngine)instance;
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

    
}
