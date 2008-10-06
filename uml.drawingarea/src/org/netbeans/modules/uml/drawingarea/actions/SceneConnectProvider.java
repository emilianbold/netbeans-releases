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
/*
 * SceneConnectProvider.java
 *
 * Created on May 29, 2007, 11:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.uml.drawingarea.actions;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.WidgetDropTargetDropEvent;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ICreationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationProxy;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationValidator;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.RelationshipDiscovery;
import org.netbeans.modules.uml.drawingarea.engines.DiagramEngine;
import org.netbeans.modules.uml.drawingarea.palette.NodeInitializer;
import org.netbeans.modules.uml.drawingarea.palette.RelationshipFactory;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.MoveDropTargetDropEvent;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.openide.util.Lookup;


/**
 *
 * @author Jyothi
 */
public class SceneConnectProvider implements ExConnectProvider
{
    private static int eventID = 0;
    
    private String defaultTargetType;
    private RelationValidator validator = new RelationValidator();
    private RelationshipFactory factory = null;
    private String stereotype = null;
    private NodeInitializer defaultnodeinitializer;

    /**
     * Creates a new instance of SceneConnectProvider.  The provider will create
     * an edge of type edgeType.  If the user drops the edge on the diagram as
     * opposed to a node, the targetType will be used to create a new node.
     *
     * @param edgeType the type of edge to create.
     * @param targetType the type of node to create if the user releases over
     *                   the diagram.
     */
    public SceneConnectProvider( String targetType,
                                 String stereotype)
    {
        this.defaultTargetType = targetType;
        this.stereotype = stereotype;
    }

    public boolean isSourceWidget(Widget sourceWidget)
    {
        return sourceWidget instanceof UMLNodeWidget;
    }

    public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget)
    {
        ConnectorState retVal = ConnectorState.REJECT;

        if (sourceWidget.getScene() instanceof ObjectScene)
        {
            ObjectScene scene = (ObjectScene) sourceWidget.getScene();
            // Verify that this relationship is ok
            IPresentationElement source = getElement(sourceWidget);
            IPresentationElement target = getElement(targetWidget);

            if (target != null)
            {
                if (scene.findWidget(target) != targetWidget)
                {
                    target = null;
                }
            }

            if ((source != null) && (target != null))
            {
                
                IElement from = source.getFirstSubject();
                IElement to = target.getFirstSubject();
                String type = getRelationshipFactory().getElementType();
                if (isValidRelationship(from, to, type, true) == true)
                {
                    retVal = ConnectorState.ACCEPT;
                }
            }
            
            if(retVal == ConnectorState.ACCEPT)
            {
                // self link
                if (sourceWidget == targetWidget)
                    return ConnectorState.ACCEPT;
                
                if(isSourceParent(sourceWidget, targetWidget) == true)
                {
                    retVal = ConnectorState.REJECT;
                }
            }
        }

        return retVal;
    }

    protected boolean isValidRelationship(IElement from, IElement to, String type, boolean silent)
    {
        boolean oldValue = ProductHelper.getMessenger().getDisableMessaging();
        if((oldValue == false) && (silent == true))
        {
            ProductHelper.getMessenger().setDisableMessaging(true);
        }
                
        RelationProxy relationshipProxy = new RelationProxy();
        relationshipProxy.setFrom(from);
        relationshipProxy.setTo(to);
        relationshipProxy.setConnectionElementType(type);
        
        boolean retVal = validator.validateRelation(relationshipProxy);
        
        ProductHelper.getMessenger().setDisableMessaging(oldValue);
        return retVal;
    }

    public boolean hasCustomTargetWidgetResolver(Scene scene)
    {
        return false;
    }

    public Widget resolveTargetWidget(Scene scene, Point sceneLocation)
    {
        return null;
    }

    public void createConnection(Widget sourceWidget, Widget targetWidget)
    {
        if ((validator != null) && (sourceWidget.getScene() instanceof GraphScene))
        {
            GraphScene scene = (GraphScene) sourceWidget.getScene();
            IPresentationElement sourceElement = getElement(sourceWidget);
            IPresentationElement targetElement = getElement(targetWidget);
            createConnection(scene, sourceElement, targetElement);
        }
    }

    public void createConnection(GraphScene scene, 
                                 IPresentationElement sourceElement, 
                                 IPresentationElement targetElement)
    {
        List<IPresentationElement> conn = new ArrayList<IPresentationElement>();

        if (getRelationshipFactory() != null)
        {
            IElement rel = getRelationshipFactory().create(sourceElement.getFirstSubject(), targetElement.getFirstSubject());
            if (rel == null)
            {
                return;
            }
            
            if((stereotype != null) && (stereotype.length() > 0))
            {
                rel.applyStereotype2(stereotype);
            }
            
            IPresentationElement edge = createEdgePresentationElement(rel);
            conn.add(edge);
        } 
        else
        {
            ETArrayList<IElement> list = new ETArrayList<IElement>();
            list.add(sourceElement.getFirstSubject());
            list.add(targetElement.getFirstSubject());
            
            DiagramEngine engine = scene.getLookup().lookup(DiagramEngine.class);
            RelationshipDiscovery relDiscovery = engine.getRelationshipDiscovery();
        
            conn = relDiscovery.discoverCommonRelations(list);
        }

        if (conn == null)
        {
            return;
        }
        
        for (IPresentationElement edge : conn)
        {
            Widget w = scene.addEdge(edge);

            scene.setEdgeSource(edge, sourceElement);
            scene.setEdgeTarget(edge, targetElement);

            Lookup lookup = w.getLookup();
            if (lookup != null)
            {
                LabelManager manager = lookup.lookup(LabelManager.class);
                if (manager != null)
                {
                    manager.createInitialLabels();
                }
            }
            scene.validate();
        }

        if(conn.size() > 0)
        {
            scene.setFocusedObject(conn.get(0));
        }
        HashSet < IPresentationElement > selected = new HashSet < IPresentationElement>(conn);
        scene.userSelectionSuggested(selected, false);  
    }

    public boolean hasTargetWidgetCreator()
    {
        boolean retVal = false;

        if ((defaultTargetType != null) && (defaultTargetType.length() > 0))
        {
            retVal = true;
        }

        return retVal;
    }

    /**
     * Create a new node widget to be used as the target widget for the connector.
     * This provider expects the scene to be a graph scene.
     *
     * @param targetScene The scene that will contain the node.  The scene must be a
     *                    GraphScene.
     * @param sourceWidget The source end of the connection
     * @param location the location of the new widget.  The coordinates are in 
     *                 scene coordinates.
     * @return The new node that was created.
     */
    public Widget createTargetWidget(Scene targetScene, 
                                     Widget sourceWidget,
                                     Point location)
    {
        Widget retVal = null;
        
        IPresentationElement sourceElement = getElement(sourceWidget);
        if ((targetScene instanceof DesignerScene) && (sourceElement != null))
        {
            DesignerScene scene = (DesignerScene) targetScene;
            Object value = FactoryRetriever.instance().createType(defaultTargetType, null);

            if (value instanceof INamedElement)
            {
                INamedElement namedElement = (INamedElement) value;
                
                IElement from = sourceElement.getFirstSubject();
                String type = getRelationshipFactory().getElementType();
                if(isValidRelationship(from, namedElement, type, false) == true)
                {   
                    if(defaultnodeinitializer!=null)
                    {
                        defaultnodeinitializer.initialize(namedElement);
                    }
                    IPresentationElement element = createNodePresentationElement(namedElement);
                    retVal = scene.addNode(element);
                    
                    // In order to check if the widget is able to fit in the 
                    // container, the bounds needs to be resolved.
                    retVal.setPreferredLocation(location);
                    
                    scene.validate();
                    
                    IDiagram diagram = scene.getDiagram();
                    if (diagram != null)
                    {
                        INamespace space = diagram.getNamespaceForCreatedElements();
                        space.addOwnedElement(namedElement);
                    }

                    MoveDropTargetDropEvent dropEvent = new MoveDropTargetDropEvent(retVal, location);
                    WidgetDropTargetDropEvent event = new WidgetDropTargetDropEvent (1, dropEvent);

                    processLocationOperator(scene, event, location);
                }
                else
                {
                    namedElement.delete();
                }
            }
         
            // I do not know why we are making this call.  I am assuming it is
            // to make sure that the widget will layout for the first time.
            // Therefore I am first verifing that the widget is valid.
            if(retVal != null)
            {
                retVal.getPreferredBounds();
            }
            scene.validate();
        }
        
        return retVal;
    }

    private boolean processLocationOperator(Widget widget,
                                         WidgetAction.WidgetDropTargetDropEvent event,
                                         Point cursorSceneLocation)
    {
        Scene scene = widget.getScene();
        Point location = scene.getLocation();
        return processLocationOperator2(scene, event, new Point(cursorSceneLocation.x + location.x, cursorSceneLocation.y + location.y));
    }

    private boolean processLocationOperator2(Widget widget,
                                            WidgetAction.WidgetDropTargetDropEvent event, 
                                            Point point)
    {
        boolean retVal = false;
        
        if (!widget.isVisible())
        {
            return false;
        }

        Point location = widget.getLocation();
        point.translate(-location.x, -location.y);

        Rectangle bounds = widget.getBounds();
        if ((bounds != null) && (bounds.contains(point) == true))
        {
            List<Widget> children = widget.getChildren();
            Widget[] childrenArray = children.toArray(new Widget[children.size()]);

            for (int i = childrenArray.length - 1; i >= 0; i--)
            {
                if(processLocationOperator2(childrenArray[i], event, point) == true)
                {
                    retVal = true;
                    break;
                }
            }

            if ((retVal == false) && (widget.isHitAt(point) == true))
            {
                retVal = sendEvents(widget, event, point);
            }
        }

        point.translate(location.x, location.y);
        return retVal;
    }
    
    private boolean sendEvents(Widget target,
                               WidgetAction.WidgetDropTargetDropEvent event,
                               Point pt)
    {
        boolean retVal = false;
        
        if(target != null)
        {
            if(sendEvents(target.getActions(), target, event) == false)
            {
                String tool = target.getScene().getActiveTool();
                retVal = sendEvents(target.getActions(tool), target, event);
            }
            else
            {
                retVal = true;
            }
        }
        
        return retVal;
    }
    
    private boolean sendEvents(WidgetAction.Chain actions,
                                    Widget target,
                                    WidgetAction.WidgetDropTargetDropEvent event)
    {
        boolean retVal = false;
        
        if(actions != null)
        {
            for(WidgetAction action :actions.getActions())
            {
                if(action.drop(target, event) == WidgetAction.State.CONSUMED)
                {
                    retVal = true;
                    break;
                }
            }
        }
        
        return retVal;
    }
    
    public RelationshipFactory getRelationshipFactory()
    {
        return factory;
    }

    public void setRelationshipFactory(RelationshipFactory factory)
    {
        this.factory = factory;
    }
    
    public void setDefaultNodeInitializer(NodeInitializer initializer)
    {
        this.defaultnodeinitializer=initializer;
    }

    protected IPresentationElement createNodePresentationElement(INamedElement element)
    {
        IPresentationElement retVal = null;

        ICreationFactory creationFactory = FactoryRetriever.instance().getCreationFactory();
        if (creationFactory != null)
        {
            Object presentationObj = creationFactory.retrieveMetaType("NodePresentation", null);
            if (presentationObj instanceof IPresentationElement)
            {
                retVal = (IPresentationElement) presentationObj;
                retVal.addSubject(element);
            }
        }

        return retVal;
    }

    private IPresentationElement getElement(Widget widget)
    {
        IPresentationElement retVal = null;

        Scene widgetScene = widget.getScene();
        if (widgetScene instanceof GraphScene)
        {
            GraphScene objScene = (GraphScene) widgetScene;
            Object value = objScene.findObject(widget);
            if (value instanceof IPresentationElement)
            {
                retVal = findNode(objScene, (IPresentationElement) value);
            }
        }

        return retVal;
    }

    protected IPresentationElement createEdgePresentationElement(IElement element)
    {
        IPresentationElement retVal = null;

        ICreationFactory creationFactory = FactoryRetriever.instance().getCreationFactory();
        if (creationFactory != null)
        {
            Object presentationObj = creationFactory.retrieveMetaType("NodePresentation", null);
            if (presentationObj instanceof IPresentationElement)
            {
                retVal = (IPresentationElement) presentationObj;
                retVal.addSubject(element);
            }
        }

        return retVal;
    }
    
    protected IPresentationElement findNode(GraphScene scene, 
                                            Object target)
    {
        IPresentationElement retVal = null;
        
        if((scene != null) && (target != null))
        {
            if((scene.isNode(target) == true) && 
               (target instanceof IPresentationElement))
            {
                retVal = (IPresentationElement)target;
            }
            else
            {
                Widget targetWidget = scene.findWidget(target);
                retVal = findNode(scene, scene.findObject(targetWidget.getParentWidget()));
            }
        }
        
        return retVal;
    }

    private boolean isSourceParent(Widget sourceWidget, Widget targetWidget)
    {
        boolean retVal = false;
        
        if((sourceWidget != null) && (targetWidget != null))
        {
            if(sourceWidget.equals(targetWidget) == false)
            {
                retVal = isSourceParent(sourceWidget.getParentWidget(), targetWidget);
            }
            else
            {
                retVal = true;
            }
        }
        
        return retVal;
    }
}