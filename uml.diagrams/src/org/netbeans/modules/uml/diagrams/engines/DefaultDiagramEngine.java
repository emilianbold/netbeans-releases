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
package org.netbeans.modules.uml.diagrams.engines;

import java.awt.Color;
import java.awt.Point;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.AlignWithMoveDecorator;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.ReconnectProvider;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationProxy;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationValidator;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.drawingarea.actions.MoveNodeKeyAction;
import org.netbeans.modules.uml.drawingarea.engines.DiagramEngine;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IStateVertex;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.DiagramTypesManager;
import org.netbeans.modules.uml.diagrams.UMLRelationshipDiscovery;
import org.netbeans.modules.uml.diagrams.actions.AssociationConnectorIteratorAction;
import org.netbeans.modules.uml.diagrams.actions.NodeLabelIteratorAction;
import org.netbeans.modules.uml.diagrams.edges.AssociationConnector;
import org.netbeans.modules.uml.diagrams.nodes.CompositeNodeWidget;
import org.netbeans.modules.uml.drawingarea.actions.IterateSelectAction;
import org.netbeans.modules.uml.drawingarea.RelationshipDiscovery;
import org.netbeans.modules.uml.drawingarea.actions.DiagramPopupMenuProvider;
import org.netbeans.modules.uml.drawingarea.actions.EdgeLabelIteratorAction;
import org.netbeans.modules.uml.drawingarea.actions.MoveControlPointAction;
import org.netbeans.modules.uml.drawingarea.actions.NavigateLinkAction;
import org.netbeans.modules.uml.drawingarea.palette.RelationshipFactory;
import org.netbeans.modules.uml.drawingarea.palette.context.SwingPaletteManager;
import org.netbeans.modules.uml.drawingarea.support.ValidDropTargets;
import org.netbeans.modules.uml.drawingarea.view.AlignWithMoveStrategyProvider;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;
import org.netbeans.modules.uml.drawingarea.view.GraphSceneNodeAlignCollector;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.ui.support.diagramsupport.IDiagramTypesManager;
import org.openide.util.Lookup;


/**
 * 
 * @author sp153251
 */
public class DefaultDiagramEngine extends  DiagramEngine {

    protected MoveStrategy DEFAULT_MOVE_STRATEGY = null;
    protected MoveProvider DEFAULT_MOVE_PROVIDER = null;
    
    private final static PopupMenuProvider CONTEXT_MENU_PROVIDER = new DiagramPopupMenuProvider();
    public final static WidgetAction POPUP_ACTION = ActionFactory.createPopupMenuAction(CONTEXT_MENU_PROVIDER);
    
    private RelationshipDiscovery relDiscovery = null;
    
    public DefaultDiagramEngine(DesignerScene scene) {
        super(scene);
        
        if(DEFAULT_MOVE_STRATEGY == null)
        {
            AlignWithMoveDecorator decorator = new AlignWithMoveDecorator()
            {
                public ConnectionWidget createLineWidget(Scene scene)
                {
                    ConnectionWidget widget = new ConnectionWidget(scene);
                    widget.setStroke(ALIGN_STROKE);
                    widget.setForeground(Color.BLUE);
                    return widget;
                }
            };
            DEFAULT_MOVE_STRATEGY = 
                    new AlignWithMoveStrategyProvider(new GraphSceneNodeAlignCollector (scene),
                                                      scene.getInterractionLayer(),
                                                      scene.getMainLayer(),
                                                      decorator,
                                                      false);
            DEFAULT_MOVE_PROVIDER = (MoveProvider) DEFAULT_MOVE_STRATEGY;
        }
        
        relDiscovery = new UMLRelationshipDiscovery(scene);
    }

    
    public void setSelectionManager(DesignerScene scene) {
        scene.setContextPaletteManager(new SwingPaletteManager(scene));
    }

    public Layout getDefaultLayout() {
        return null;
    }

    public Widget createWidget(IPresentationElement node) {
        
        String diagramType = getScene().getDiagram().getDiagramKindAsString().replaceAll(" ", "");
        String elementType = node.getFirstSubjectsType();

        if (elementType != null && elementType.equals("PartFacade")) 
        {
            IElement first = node.getFirstSubject();
            if (first instanceof IParameterableElement)
            {
                IParameterableElement pParamEle = (IParameterableElement)first;
                String constraint = pParamEle.getTypeConstraint();
                if (constraint != null) 
                {
                    elementType += "/" + constraint;
                }
            }
        }
        
        if (node.getFirstSubject() instanceof IStateVertex)
        {
            elementType += "/" + node.getFirstSubject().getExpandedElementType() ;
        }
        
        // First see if there is a widget that is designed specifically for 
        // this diagram.
        String path="UML/" + diagramType + "/Nodes/" + elementType;
        UMLNodeWidget ret = getWidget(getScene(),path);
        
        if(ret == null)
        {
            // Next check if there is a widget specified in the general 
            // section.
            path="UML/Nodes/" + elementType;
            ret = getWidget(getScene(),path);
        }
        
        return ret;
    }

    public boolean isDropPossible(IPresentationElement node) {
        return true;
    }

    public boolean isDropPossible(INamedElement node) {
        boolean okToDrop = true;
        try
        {
            IDiagramTypesManager pManager = DiagramTypesManager.instance();
            if (node != null && pManager != null)
            {
                String sElementType = node.getElementType();
                String diagramKindName = getScene().getDiagram().getDiagramKindAsString();
                String sShortDisplayName = pManager.getShortDiagramTypeName(diagramKindName);

                // Make sure our diagram type is in the ok-to-drop list, or ALL is in the list.
                okToDrop = ValidDropTargets.instance().isValidDropTarget(sElementType, sShortDisplayName);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            okToDrop = false;
        }

        return okToDrop;
    }

    public INamedElement processDrop(INamedElement elementToDrop) {
        //default engine do not transform, it is required on sqd only (may be will changed later)
        return elementToDrop;
    }

    public RelationshipDiscovery getRelationshipDiscovery()
    {
        return relDiscovery;
    }

    public void setActions(Widget widget,IPresentationElement node) {
        DesignerScene scene=(DesignerScene) widget.getScene();
        
        MoveStrategy moveStrategy = DEFAULT_MOVE_STRATEGY;
        MoveProvider moveProvider = DEFAULT_MOVE_PROVIDER;
        WidgetAction selectAction = sceneSelectAction;
        
        if(widget.getLookup() != null)
        {
            Lookup lookup = widget.getLookup();
            
            if(lookup.lookup(MoveStrategy.class) != null)
            {
                moveStrategy = lookup.lookup(MoveStrategy.class);
            }
            
            if(lookup.lookup(MoveProvider.class) != null)
            {
                moveProvider = lookup.lookup(MoveProvider.class);
            }
            
            if(lookup.lookup(SelectProvider.class) != null)
            {
                SelectProvider selectProvider = lookup.lookup(SelectProvider.class);
                selectAction = ActionFactory.createSelectAction(selectProvider, true);
            }
        }
        
        WidgetAction.Chain selectTool = widget.createActions(DesignerTools.SELECT);      
                
        selectTool.addAction(DiagramEngine.lockSelectionAction);
        selectTool.addAction(new MoveNodeKeyAction(moveStrategy, moveProvider));
        selectTool.addAction(selectAction);
        selectTool.addAction(POPUP_ACTION);
        selectTool.addAction(mouseHoverAction);
        selectTool.addAction(ActionFactory.createMoveAction(moveStrategy, moveProvider));        
        if (widget instanceof CompositeNodeWidget)
        {
            selectTool.addAction(new NodeLabelIteratorAction());
        }
        else
        {
            selectTool.addAction(new IterateSelectAction());
        }
        WidgetAction.Chain navigateLinkTool = widget.createActions(DesignerTools.NAVIGATE_LINK);
        navigateLinkTool.addAction(new NavigateLinkAction());
        navigateLinkTool.addAction(ActionFactory.createZoomAction());
        navigateLinkTool.addAction(POPUP_ACTION);
        
        WidgetAction.Chain readOnly = widget.createActions(DesignerTools.READ_ONLY);
        readOnly.addAction(selectAction);
        readOnly.addAction(POPUP_ACTION);
        readOnly.addAction(mouseHoverAction);        
        if (widget instanceof CompositeNodeWidget)
        {
            readOnly.addAction(new NodeLabelIteratorAction());
        }
        else
        {
            readOnly.addAction(new IterateSelectAction());
        }
    }

    public void setActions(ConnectionWidget widget,IPresentationElement edge) {
        WidgetAction.Chain selectTool = widget.createActions(DesignerTools.SELECT); 
        
        selectTool.addAction(new MoveNodeKeyAction(DEFAULT_MOVE_STRATEGY, DEFAULT_MOVE_PROVIDER));
        selectTool.addAction(DiagramEngine.lockSelectionAction);
        selectTool.addAction (ActionFactory.createAddRemoveControlPointAction ());
        selectTool.addAction(sceneSelectAction);
        
        widget.setPaintControlPoints (true);
        widget.setControlPointShape (PointShape.SQUARE_FILLED_BIG);
        
        selectTool.addAction( DiagramEngine.lockSelectionAction);
        selectTool.addAction(POPUP_ACTION);
        selectTool.addAction(ActionFactory.createReconnectAction(new SceneReconnectProvider()));
        
        WidgetAction.Chain navigateLinkTool = widget.createActions(DesignerTools.NAVIGATE_LINK);
        navigateLinkTool.addAction(new NavigateLinkAction());
        navigateLinkTool.addAction(ActionFactory.createZoomAction());
        navigateLinkTool.addAction(POPUP_ACTION);
        selectTool.addAction (new MoveControlPointAction(ActionFactory.createFreeMoveControlPointProvider (), null));
        selectTool.addAction(new EdgeLabelIteratorAction());
        if (widget instanceof AssociationConnector)
        {
            selectTool.addAction(new AssociationConnectorIteratorAction());
        }
        
        WidgetAction.Chain readOnly = widget.createActions(DesignerTools.READ_ONLY);      
        readOnly.addAction(sceneSelectAction);
        readOnly.addAction(POPUP_ACTION);
        readOnly.addAction(new EdgeLabelIteratorAction());
    }
    
    /**
     * Retrieves the edge router to use when creating new edges.
     * 
     * @param layers The layers that can contain connection widgets.
     * @return the edge router
     */
    public Router getEdgeRouter(LayerWidget... layers)
    {
//        return RouterFactory.createOrthogonalSearchRouter(layers);
        return RouterFactory.createFreeRouter() ;
    }
    
    public ConnectionWidget createConnectionWidget(DesignerScene scene, 
                                                   IPresentationElement edge)
    {
        String edgeType = edge.getFirstSubjectsType();
     
        // First see if there is a widget that is designed specifically for 
        // this diagram.
        String path ="UML/" + getDiagramKindName(scene) +
                     "/Connectors/" + edgeType;
        
        ConnectionWidget retVal = getConnectorWidget(scene,path);
        if(retVal == null)
        {
            
            // Next check if there is a widget specified in the general 
            // section.
            path = "UML/Connectors/" + edgeType;
            retVal = getConnectorWidget(scene,path);
        }
        
        // special case for nested link
        if (retVal == null)
        {
            if (edge.getSubjectCount() > 1)
            {
                path = "UML/Connectors/NestedLink";
                retVal = getConnectorWidget(scene,path);
            }
        }

        return retVal;
    }

    public Widget addWidget(IPresentationElement presentation, Point point)
    {
        Widget newWidget = null;
        newWidget = getScene().addNode(presentation);
        if(newWidget!=null)
        {
            getScene().validate();
            Point location = point;

            // see comment in SceneAcceptAction.moved(), 
    //            location = new Point(point.x - newWidget.getPreferredBounds().width / 2,
    //                                   point.y - newWidget.getPreferredBounds().height / 2);

            newWidget.setPreferredLocation(location);
        }
        return newWidget;
    }
    
    private class SceneReconnectProvider implements ReconnectProvider
    {

        private IPresentationElement originalSource = null;
        private IPresentationElement originalTarget = null;
        private Anchor originalSourceAnchor = null;
        private Anchor originalTargetAnchor = null;
        private RelationValidator validator = new RelationValidator();

        public void reconnectingStarted(ConnectionWidget connectionWidget, 
                                        boolean reconnectingSource)
        {
            Widget sourceWidget = connectionWidget.getSourceAnchor().getRelatedWidget();
            originalSource = (IPresentationElement) getScene().findObject(sourceWidget);
            originalSourceAnchor= connectionWidget.getSourceAnchor();
            
            Widget targetWidget = connectionWidget.getTargetAnchor().getRelatedWidget();
            originalTarget = (IPresentationElement) getScene().findObject(targetWidget);
            originalTargetAnchor = connectionWidget.getTargetAnchor();
        }

        public void reconnectingFinished(ConnectionWidget connectionWidget, 
                                         boolean reconnectingSource)
        {
        }

        public boolean isSourceReconnectable(ConnectionWidget connectionWidget)
        {
            boolean retVal = false;
            
            IPresentationElement element = (IPresentationElement) getScene().findObject(connectionWidget);
            if(element != null)
            {
                String type = element.getFirstSubjectsType();
                if(getRelationshipFactory(type) != null)
                {
                    retVal = true;
                }
            }
            
            return retVal;
        }

        public boolean isTargetReconnectable(ConnectionWidget connectionWidget)
        {
            boolean retVal = false;
            
            IPresentationElement element = (IPresentationElement) getScene().findObject(connectionWidget);
            if(element != null)
            {
                String type = element.getFirstSubjectsType();
                if(getRelationshipFactory(type) != null)
                {
                    retVal = true;
                }
            }
            
            return retVal;
        }

        public ConnectorState isReplacementWidget(ConnectionWidget connectionWidget, 
                                                  Widget replacementWidget, 
                                                  boolean reconnectingSource)
        {
            ConnectorState retVal = ConnectorState.REJECT;

            DesignerScene scene = getScene();
            Object suggested = scene.findObject(replacementWidget);

            IPresentationElement sourceElement = null;
            IPresentationElement targetElement = null;
            boolean sameElement = true;
            if (reconnectingSource == true)
            {
                sourceElement = scene.isNode(suggested) ? (IPresentationElement) suggested : null;
                targetElement = originalTarget;
                
                if(sourceElement != originalSource)
                {
                    sameElement = false;
                }
            }
            else
            {
                targetElement = scene.isNode(suggested) ? (IPresentationElement) suggested : null;
                sourceElement = originalSource;
                
                if(targetElement != originalTarget)
                {
                    sameElement = false;
                }
            }

            if (!sameElement && (sourceElement != null) && (targetElement != null))
            {
                RelationProxy relationshipProxy = new RelationProxy();
                relationshipProxy.setFrom(sourceElement.getFirstSubject());
                relationshipProxy.setTo(targetElement.getFirstSubject());

                IPresentationElement relPresenation = (IPresentationElement) scene.findObject(connectionWidget);
                IElement relElement = relPresenation.getFirstSubject();
                relationshipProxy.setConnectionElementType(relElement.getElementType());
                
                // indicates that this relationship is a reconnected one
                relationshipProxy.setReconnectionFlag(true);

                // Verify the relation
                validator.validateRelation(relationshipProxy);

                INamedElement source = (INamedElement)sourceElement.getFirstSubject();
                INamedElement target = (INamedElement)targetElement.getFirstSubject();
                if (relationshipProxy.getRelationValidated() == true)
                {
                    // TODO: I also have to send our the correct events to see if
                    // we can create the relationship.
                    retVal = ConnectorState.ACCEPT;
                }
            }
            else if(sameElement)
            {
                //reconnection back to the same widget should be lways allowed
                retVal = ConnectorState.ACCEPT;
            }
            
            return retVal;
        }

        public boolean hasCustomReplacementWidgetResolver(Scene scene)
        {
            return false;
        }

        public Widget resolveReplacementWidget(Scene scene, Point sceneLocation)
        {
            return null;
        }

        public void reconnect(ConnectionWidget connectionWidget, 
                              Widget replacementWidget, boolean reconnectingSource)
        {
            if (replacementWidget == null)
            {
                return;//do not remove but restore to old place
            }
            DesignerScene scene = getScene();
            IPresentationElement replacementNode = (IPresentationElement)scene.findObject(replacementWidget);
            if(reconnectingSource && replacementNode==originalSource)return;
            else if(!reconnectingSource && replacementNode==originalTarget)return;
            IPresentationElement edge = (IPresentationElement)scene.findObject(connectionWidget);
            IElement relationship = edge.getFirstSubject();
            
            // By this time reconnectFinished has already been called.  Therefore
            // the originalSource and originalFinished is already null.  I 
            // do not understand why reconnectFinished is called before the 
            // reconnect method is called, but such is life.
            Widget sourceWidget = connectionWidget.getSourceAnchor().getRelatedWidget();
            IPresentationElement sourceElement = (IPresentationElement)scene.findObject(sourceWidget);
            
            Widget targeteWidget = connectionWidget.getTargetAnchor().getRelatedWidget();
            IPresentationElement targetElement = (IPresentationElement)scene.findObject(targeteWidget);
            
            if (replacementWidget == null)
            {
                getScene().removeEdge(edge);
                relationship.delete();
            }
            else if (reconnectingSource)
            {
                getScene().setEdgeSource(edge, replacementNode);
                RelationshipFactory factory = getRelationshipFactory(edge.getFirstSubjectsType());
                if(factory != null)
                {
                    factory.reconnectSource(relationship, 
                                            sourceElement.getFirstSubject(),
                                            replacementNode.getFirstSubject(),
                                            targetElement.getFirstSubject());
                }
            }
            else
            {
                getScene().setEdgeTarget(edge, replacementNode);
                RelationshipFactory factory = getRelationshipFactory(edge.getFirstSubjectsType());
                if(factory != null)
                {
                    factory.reconnectTarget(relationship, 
                                            targetElement.getFirstSubject(),
                                            replacementNode.getFirstSubject(),
                                            sourceElement.getFirstSubject());
                }
            }
            originalSource = null;
            originalTarget = null;
            originalSourceAnchor = null;
            originalTargetAnchor = null;
        }
    }

    @Override
    protected void setingValueChanged(String key, Object ret, Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
