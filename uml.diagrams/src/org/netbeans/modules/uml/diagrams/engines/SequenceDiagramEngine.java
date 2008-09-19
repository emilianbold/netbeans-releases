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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JToolBar;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.AlignWithMoveDecorator;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.ReconnectDecorator;
import org.netbeans.api.visual.action.ReconnectProvider;
import org.netbeans.api.visual.action.TwoStateHoverProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.BaseElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IRelationship;
import org.netbeans.modules.uml.core.metamodel.core.foundation.Namespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationProxy;
import org.netbeans.modules.uml.core.metamodel.core.foundation.RelationValidator;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.dynamics.CombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.ICombinedFragment;
import org.netbeans.modules.uml.core.metamodel.dynamics.IInteraction;
import org.netbeans.modules.uml.core.metamodel.dynamics.ILifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.metamodel.dynamics.Lifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.Message;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.structure.IComment;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.diagrams.UMLRelationshipDiscovery;
import org.netbeans.modules.uml.diagrams.actions.sqd.AddCarFprPresentationElementAction;
import org.netbeans.modules.uml.diagrams.actions.sqd.AlignWithMoveStrategyProvider;
import org.netbeans.modules.uml.diagrams.actions.sqd.ArrangeMoveWithBumping;
import org.netbeans.modules.uml.diagrams.actions.sqd.CombinedFragmentMoveProvider;
import org.netbeans.modules.uml.diagrams.actions.sqd.LifelineMoveAction;
import org.netbeans.modules.uml.diagrams.actions.sqd.LifelineMoveProvider;
import org.netbeans.modules.uml.diagrams.actions.sqd.LifelineMoveStrategy;
import org.netbeans.modules.uml.diagrams.actions.sqd.MessageMoveProvider;
import org.netbeans.modules.uml.diagrams.actions.sqd.MessageMoveStrategy;
import org.netbeans.modules.uml.diagrams.actions.sqd.MessagesConnectProvider;
import org.netbeans.modules.uml.diagrams.anchors.TargetMessageAnchor;
import org.netbeans.modules.uml.diagrams.edges.factories.MessageFactory;
import org.netbeans.modules.uml.diagrams.edges.sqd.MessageLabelManager;
import org.netbeans.modules.uml.diagrams.edges.sqd.MessageWidget;
import org.netbeans.modules.uml.diagrams.edges.sqd.ResultMessageConnectionWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.CombinedFragmentWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.ExecutionSpecificationThinWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.InteractionBoundaryWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.LifelineLineWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.LifelineWidget;
import org.netbeans.modules.uml.diagrams.nodes.sqd.MessagePinWidget;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.RelationshipDiscovery;
import org.netbeans.modules.uml.drawingarea.SQDDiagramTopComponent;
import org.netbeans.modules.uml.drawingarea.actions.ActionProvider;
import org.netbeans.modules.uml.drawingarea.actions.AfterValidationExecutor;
import org.netbeans.modules.uml.drawingarea.actions.DiagramPopupMenuProvider;
import org.netbeans.modules.uml.drawingarea.actions.DiscoverRelationshipAction;
import org.netbeans.modules.uml.drawingarea.actions.EdgeLabelIteratorAction;
import org.netbeans.modules.uml.drawingarea.actions.HierarchicalLayoutAction;
import org.netbeans.modules.uml.drawingarea.actions.MoveNodeKeyAction;
import org.netbeans.modules.uml.drawingarea.actions.NavigateLinkAction;
import org.netbeans.modules.uml.drawingarea.actions.OrthogonalLayoutAction;
import org.netbeans.modules.uml.drawingarea.actions.SQDMessageConnectProvider;
import org.netbeans.modules.uml.drawingarea.engines.DiagramEngine;
import org.netbeans.modules.uml.drawingarea.palette.RelationshipFactory;
import org.netbeans.modules.uml.drawingarea.palette.context.SwingPaletteManager;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceUtil;
import org.netbeans.modules.uml.drawingarea.ui.addins.diagramcreator.SQDDiagramEngineExtension;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;
import org.netbeans.modules.uml.drawingarea.view.GraphSceneNodeAlignCollector;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.drawingarea.widgets.MessagePin.PINKIND;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author sp153251
 */
public class SequenceDiagramEngine extends DiagramEngine implements SQDDiagramEngineExtension {

    
    protected MoveStrategy DEFAULT_MOVE_STRATEGY = null;
    protected MoveProvider DEFAULT_MOVE_PROVIDER = null;
    //private Layout defaultlayout=new SQDLayout();
    private InteractionBoundaryWidget sqdBoundary;
    private PopupMenuProvider menuProvider = new DiagramPopupMenuProvider();
    private IInteraction interaction;
    private RelationshipDiscovery relDiscovery = null;
    private boolean trackbarusage=true;
    
    public SequenceDiagramEngine(DesignerScene scene) {
        super(scene);
         //need to check if there interaction, it's specific to diagram so is handed in engine
         IDiagram dgr=getScene().getDiagram();
         INamespace ns=dgr.getNamespace();
         String type=ns.getExpandedElementType();
         if(! type.equalsIgnoreCase("interaction"))
         {
             //create interaction
             Object value = FactoryRetriever.instance().createType("Interaction",
             ns);

             INamedElement inter = null;
             if(value instanceof INamedElement)
             {
                 inter = (INamedElement)value;
             }
             //
             interaction=(IInteraction) inter;
             //
             inter.setName(dgr.getName());//by default the same as diagram
             //inter.setNamespace(ns);
             ns.addOwnedElement(inter);
             dgr.setNamespace((Namespace) inter);
         }
         else
         {
             interaction=(IInteraction) ns;
         }
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
                    new org.netbeans.modules.uml.drawingarea.view.AlignWithMoveStrategyProvider(new GraphSceneNodeAlignCollector (scene),
                                                      scene.getInterractionLayer(),
                                                      scene.getMainLayer(),
                                                      decorator,
                                                      false);
            DEFAULT_MOVE_PROVIDER = (MoveProvider) DEFAULT_MOVE_STRATEGY;
        }
        //fill settings, where to get default? some should be from preferences
        setSettingValue(SHOW_MESSAGE_NUMBERS, Boolean.FALSE);
        setSettingValue(SHOW_RETURN_MESSAGES, Boolean.TRUE);
        setSettingValue(SHOW_INTERACTION_BOUNDARY, Boolean.FALSE);
        //
        scene.addObjectSceneListener(new SQDChangeListener(), ObjectSceneEventType.values());
        
        relDiscovery = new UMLRelationshipDiscovery(scene);
    }
    
    

    /**
     * used to attach actions to scene
     * currenly is used for events handler too(may need to be separated?)
     * @param scene
     */
    @Override
    public void setActions(DesignerScene scene) {
        super.setActions(scene);
    }

    public void setSelectionManager(DesignerScene scene) {
        scene.setContextPaletteManager(new SwingPaletteManager(scene));
    }

    public Layout getDefaultLayout() {
        return null;//defaultlayout;
    }

    public Widget createWidget(IPresentationElement node) {
        String diagramType = getScene().getDiagram().getDiagramKindAsString().replaceAll(" ", "");
        String elementType = node.getFirstSubject().getExpandedElementType();
        
        String path="UML/" + diagramType + "/Nodes/" + elementType;
        UMLNodeWidget ret = getWidget(getScene(),path);
        if(ret == null)
        {
            // Next check if there is a widget specified in the general 
            // section.
            path= "UML/Nodes/" + elementType;
            ret = getWidget(getScene(),path);
        }
        return ret;
    }
    
    public Widget addWidget(final IPresentationElement presentation,Point pnt)
    {
        Widget newWidget = null;
        boolean callChanged=true;
        Point point=new Point(pnt);
        newWidget = getScene().addNode(presentation);
        getScene().validate();
        INamedElement element=(INamedElement) presentation.getFirstSubject();
        if(element instanceof ILifeline && point!=null)
        {
                LifelineWidget l=(LifelineWidget)newWidget;
                ILifeline ll=(ILifeline) element;
                //all simple lifelines(not created) should be on top, ???TBD check for created???
                int y_border_shift=0;
                if(!l.isCreated())
                {
                    if(point.y!=(DEFAULT_LIFELINE_Y-y_border_shift) && !ll.getIsActorLifeline())
                    {
                        point.y=DEFAULT_LIFELINE_Y-y_border_shift;
                    }
                    else if(point.y!=(DEFAULT_ACTORLIFELINE_Y-y_border_shift) && ll.getIsActorLifeline())
                    {
                       point.y=DEFAULT_ACTORLIFELINE_Y-y_border_shift;
                    }
                }
        }
        
        if(point!=null)
        {
            Point location = point;
//            if(!(element instanceof ILifeline))location.y-=newWidget.getPreferredBounds().height / 2;
//            location.x -= newWidget.getPreferredBounds().width / 2;
            newWidget.setPreferredLocation(newWidget.convertLocalToScene(location));
        
            //
            if(element instanceof ILifeline && trackbarusage)
            {
                //set properties if actor
                ILifeline ll=(ILifeline) element;
                //add to trackbar
                if(tc instanceof SQDDiagramTopComponent)
                {
                    new AfterValidationExecutor(new AddCarFprPresentationElementAction((SQDDiagramTopComponent) tc,(LifelineWidget) newWidget, presentation), getScene());
                }
            }
            trackbarusage=true;
        }
        //
        if(element instanceof ICombinedFragment || element instanceof IInteraction)
        {
            //
            if(element instanceof IInteraction && element==getScene().getDiagram().getNamespace())//interaction boundary
            {
                sqdBoundary=  (InteractionBoundaryWidget) newWidget;
                setSettingValue(SHOW_INTERACTION_BOUNDARY, Boolean.TRUE);
                callChanged=false;
            }
            else
            {
                //
                List<Widget> children=newWidget.getParentWidget().getChildren();
                for(int i=0;i<children.size();i++)
                {
                    Widget w=children.get(i);
                    if(!(w instanceof CombinedFragmentWidget))
                    {
                        Widget par=newWidget.getParentWidget();
                        par.removeChild(newWidget);
                        par.addChild(i, newWidget);
                        break;
                    }
                }
            }
            //TBD, check existance of combined fragments and go above existent one if any cover
            //it will not be enouth to use containers for combinedfragments because it should be behind lifelines which are not covered by cf fully
        }
        if(callChanged)diagramChanged();
        //
//        getScene().validate();
        return newWidget;
    }
    public boolean isDropPossible(IPresentationElement node) {
        return true;
    }

    public boolean isDropPossible(INamedElement node) {
        String type0=node.getExpandedElementType();
        if(node instanceof ILifeline || node instanceof IComment)
        {
            //accept as is (TBD may be need to check if exists in "current" interaction
            return true;
        }
        else if(node instanceof ICombinedFragment)
        {
            if(node.getPresentationElements().size()==0)return true;//only once drop should be possible
            //but it may be dropped to another diagram
            for(IPresentationElement pe:node.getPresentationElements())
            {
                if(getScene().findWidget(pe)!=null)return false;
            }
            return true;
        }
        else
        {
            //TBD, extend more types
            if(node instanceof IClassifier)return true;
            else return false;
        }
    }

    public INamedElement processDrop(INamedElement elementToDrop) {
        
        if ( elementToDrop == null)
        {
            return null;
        }
        String type0=elementToDrop.getExpandedElementType();
        if(type0==null)type0=elementToDrop.getElementType();
        INamedElement ret=elementToDrop;
        INamespace ns=getScene().getDiagram().getNamespace();
        if(ret.getNamespace()==null && ret.getOwner()==null)ns.addOwnedElement(ret);//need to add here as a fix for combined fragment initialization problem below caused by not set of namespace in accept on containers before call to processing, set default ns to diagram ns
        if(type0.equals("Lifeline") || type0.equals("Comment"))
        {
            //accept as is (TBD may be need to check if exists in "current" interaction
        }
        else if (type0.equals("CombinedFragment"))
        {
            // by default combined fragment should have at least one InteractionOperand
            CombinedFragment fr=(CombinedFragment) elementToDrop;
            if(fr.getOperands().size()==0)fr.createOperand();
        }
        else if(type0.equals("Interaction"))
        {
            if(ns instanceof IInteraction)
            {
                IInteraction inter=(IInteraction) ns;
                if(inter.equals(elementToDrop))
                {
                    //can't drop interaction to itself
                    ret=null;
                }
                else
                {
                    //can drop as is
                }
            }
            else
            {
                ret=null;
            }
        }
        else if(elementToDrop instanceof IClassifier)
        {
            String newObjectType="Lifeline";
            ret = null;
            if(newObjectType!=null)
            {
                //create interaction
                Object value = FactoryRetriever.instance().createType(newObjectType, 
                                                                       null);

                if(value instanceof INamedElement)
                {
                    ret = (INamedElement)value;
                    getScene().getDiagram().getNamespace().addElement(ret);
                }
                //set names etc
                if(value instanceof Lifeline)
                {
                    Lifeline ll=(Lifeline) value;
                    if(type0.equals("Actor"))
                    {
                        ll.setIsActorLifeline(true);
                    }
                    ll.setRepresentingClassifier((IClassifier) elementToDrop);
                }
            }
        }
        else
        {
            ret=null;
        }
        //
        return ret;
    }

    public RelationshipDiscovery getRelationshipDiscovery()
    {
        return relDiscovery;
    }
    
    public void setActions(Widget widget,IPresentationElement node) {
        DesignerScene scene=(DesignerScene) widget.getScene();
        
        AlignWithMoveStrategyProvider provider = 
                new AlignWithMoveStrategyProvider(new GraphSceneNodeAlignCollector(scene),
                                                  scene.getInterractionLayer(), 
                                                  scene.getMainLayer(),
                                                  ActionFactory.createDefaultAlignWithMoveDecorator(),
                                                  false);
        //
        WidgetAction.Chain selectTool = widget.createActions(DesignerTools.SELECT);
        
        selectTool.addAction(DiagramEngine.lockSelectionAction);
        selectTool.addAction(mouseHoverAction);
        selectTool.addAction(sceneSelectAction);
        selectTool.addAction(ActionFactory.createPopupMenuAction(menuProvider));
        IElement  element=node.getFirstSubject();
        if(node.getFirstSubject().getExpandedElementType().equals("Lifeline"))//works for both Lifeline and ActorLifeline
        {
            WidgetAction lifelineMoveAction=new LifelineMoveAction(new LifelineMoveStrategy(), new LifelineMoveProvider(provider));
            selectTool.addAction(new MoveNodeKeyAction(new LifelineMoveStrategy(), new LifelineMoveProvider(provider)));
            selectTool.addAction(lifelineMoveAction);
        }
        else if(widget instanceof CombinedFragmentWidget)//covers combinedfragments, references, interaction boundary
        {
            //check if it's interaction for this diagram
            if(element instanceof IInteraction && element==getScene().getDiagram().getNamespace())//interaction boundary
            {
                //do not move interaction boundary
            }
            else 
            {
                CombinedFragmentMoveProvider cfMoveProvider = new CombinedFragmentMoveProvider(provider);
                selectTool.addAction(ActionFactory.createMoveAction(provider, cfMoveProvider));
                selectTool.addAction(new MoveNodeKeyAction(provider, cfMoveProvider));
            }
        }
        else
        {
           selectTool.addAction(ActionFactory.createMoveAction(provider, provider));
        }
        
        WidgetAction.Chain navigateLinkTool = widget.createActions(DesignerTools.NAVIGATE_LINK);
        navigateLinkTool.addAction(new NavigateLinkAction());
        navigateLinkTool.addAction(ActionFactory.createZoomAction());
        navigateLinkTool.addAction(ActionFactory.createPopupMenuAction(menuProvider));
        
        WidgetAction.Chain readOnly = widget.createActions(DesignerTools.READ_ONLY);
        readOnly.addAction(mouseHoverAction);
        readOnly.addAction(sceneSelectAction);
        readOnly.addAction(ActionFactory.createPopupMenuAction(menuProvider));
        readOnly.addAction(mouseHoverAction);
    }

    public void setActions(ConnectionWidget widget,IPresentationElement edge) {
        WidgetAction.Chain selectTool = widget.createActions(DesignerTools.SELECT);
        selectTool.addAction(new MoveNodeKeyAction(DEFAULT_MOVE_STRATEGY, DEFAULT_MOVE_PROVIDER));
        IElement el=edge.getFirstSubject();
        String edgeKind=null;
        if(el instanceof IMessage)
        {
            IMessage msg=(IMessage) edge.getFirstSubject();
            edgeKind=getMessageKindAsString(msg.getKind());
        }
                
        selectTool.addAction(DiagramEngine.lockSelectionAction);
        selectTool.addAction(sceneSelectAction);
        selectTool.addAction(ActionFactory.createPopupMenuAction(menuProvider));
        if("Synchronous".equals(edgeKind) || "Asynchronous".equals(edgeKind))selectTool.addAction(ActionFactory.createReconnectAction(new MessageReconnectDecorator(widget),new MessagesReconnectProvider()));//only these messages was possible to reconnect in 6.0
        else if (edgeKind==null)selectTool.addAction(ActionFactory.createReconnectAction(new SceneReconnectProvider()));//reconnection of non-message edges
        if(edgeKind!=null)
        {
            selectTool.addAction(ActionFactory.createHoverAction(new TwoStateHoverProvider()
            {
                private java.awt.Cursor old;
                public void unsetHovering(Widget widget) {
                    if(old==null)
                    {
                        old=java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR);
                    }
                    widget.setCursor(old);
                }

                public void setHovering(Widget widget) {
                     old=widget.getCursor();
                     widget.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.N_RESIZE_CURSOR));
                }
            }));
            selectTool.addAction(ActionFactory.createMoveAction(new MessageMoveStrategy(),new MessageMoveProvider()));
        }
        selectTool.addAction(new EdgeLabelIteratorAction());
        
        WidgetAction.Chain navigateLinkTool = widget.createActions(DesignerTools.NAVIGATE_LINK);
        navigateLinkTool.addAction(new NavigateLinkAction());
        navigateLinkTool.addAction(ActionFactory.createZoomAction());
        navigateLinkTool.addAction(ActionFactory.createPopupMenuAction(menuProvider));
        
        WidgetAction.Chain readOnly = widget.createActions(DesignerTools.READ_ONLY);      
        readOnly.addAction(sceneSelectAction);
        readOnly.addAction(ActionFactory.createPopupMenuAction(menuProvider));
    }
    
    /**
     * Retrieves the edge router to use when creating new edges.
     * 
     * @param layers The layers that can contain connection widgets.
     */
    public Router getEdgeRouter(LayerWidget... layers)
    {
        return RouterFactory.createDirectRouter();
    }
    
    public ConnectionWidget createConnectionWidget(DesignerScene scene, 
                                                   IPresentationElement edge)
    {
        ConnectionWidget retVal = null;
        
        String edgeType = edge.getFirstSubject().getExpandedElementType();
        String edgeKind=null;
        if(edgeType.equals("Message"))
        {
            Message msg=(Message) edge.getFirstSubject();
            edgeKind=getMessageKindAsString(msg.getKind());
        }
        
        edgeType = edgeType + (edgeKind != null ? "/" + edgeKind : "");
        String path="UML/" + getDiagramKindName(scene) +
                    "/Connectors/" + edgeType;
        
        ConnectionWidget ret = getConnectorWidget(scene,path);
        if(ret == null)
        {
            
            // Next check if there is a widget specified in the general 
            // section.
            path = "UML/Connectors/" + edgeType;
            ret = getConnectorWidget(scene,path);
        }
        //        
        return ret;
    }
    
    private String getMessageKindAsString(int kind)
    {
        String ret=null;
            switch(kind)
            {
            case BaseElement.MK_SYNCHRONOUS:
                ret="Synchronous";
                break;
            case BaseElement.MK_RESULT:
                ret="Result";
                break;
            case BaseElement.MK_ASYNCHRONOUS:
                ret="Asynchronous";
                break;
            case BaseElement.MK_CREATE:
                ret="Create";
                break;
            }
        return ret;
    }
    
    
    private class MessageReconnectDecorator implements ReconnectDecorator
    {

        private ConnectionWidget connection;
        
        public MessageReconnectDecorator(ConnectionWidget connection)
        {
            this.connection=connection;
        }
        
        public Anchor createReplacementWidgetAnchor(Widget replacementWidget) {
            return new TargetMessageAnchor(replacementWidget);
        }

        public Anchor createFloatAnchor(Point location) {
            return AnchorFactory.createFixedAnchor (location); 
        }
        
    }
    //////////
    private class MessagesReconnectProvider implements ReconnectProvider
    {
        private IPresentationElement originalSource;
        private IPresentationElement originalTarget;
        private IPresentationElement connectionPE;
        
        public void reconnectingStarted(ConnectionWidget connectionWidget, 
                                        boolean reconnectingSource)
        {
            Widget sourceWidget = connectionWidget.getSourceAnchor().getRelatedWidget();
            originalSource = (IPresentationElement) getScene().findObject(sourceWidget);
            
            Widget targetWidget = connectionWidget.getTargetAnchor().getRelatedWidget();
            originalTarget = (IPresentationElement) getScene().findObject(targetWidget);
            connectionPE = (IPresentationElement) getScene().findObject(connectionWidget);
        }

        public void reconnectingFinished(ConnectionWidget connectionWidget, 
                                         boolean reconnectingSource)
        {
        }

        public boolean isSourceReconnectable(ConnectionWidget connectionWidget)
        {
            //source for messages can't be reconnected
            return false;
        }

        public boolean isTargetReconnectable(ConnectionWidget connectionWidget)
        {
            boolean retVal = false;
            IPresentationElement element = (IPresentationElement) getScene().findObject(connectionWidget);
            //it wasn't possible to reconnect links to combined fragment
            Widget targetWidget = connectionWidget.getTargetAnchor().getRelatedWidget();
            IElement targetElement=((IPresentationElement) getScene().findObject(targetWidget)).getFirstSubject();
            IMessage actual =null;
            if(targetElement instanceof ICombinedFragment || targetElement instanceof IInteraction)retVal=false;
            //
            else if(element != null)
            {
               actual = (IMessage) element.getFirstSubject();
               if(getRelationshipFactory(getMessageKindAsString(actual.getKind())+actual.getElementType()) != null)
                {
                    retVal = true;
                }
            }
            if(retVal)
            {
                //reconnection of message to self to create normal message isn't supported in 6.0 and will not be implemented for now
                Widget sourceWidget = connectionWidget.getSourceAnchor().getRelatedWidget();
                retVal=!(targetWidget.getParentWidget().getParentWidget()==sourceWidget.getParentWidget());
            }
            if(retVal)
            {
                //moreverifications
                //-check if target have childs
                if(actual!=null)
                {
                    MessagePinWidget pin1=(MessagePinWidget) connectionWidget.getTargetAnchor().getRelatedWidget();
                    ExecutionSpecificationThinWidget exSpec=(ExecutionSpecificationThinWidget) pin1.getParentWidget();
                    retVal=(pin1.getKind()==MessagePinWidget.PINKIND.SYNCHRONOUS_CALL_IN && exSpec.getChildren().size()==2) || (pin1.getKind()==MessagePinWidget.PINKIND.ASYNCHRONOUS_CALL_IN && exSpec.getChildren().size()==1);
                    //DO not support move of child messages for now, TBD investigate what to do
                }
            }
            return retVal;
        }

        /**
         * Called to check for possible replacement of a connection source/target.
         * Called only when the hasCustomReplacementWidgetResolver method return false.
         * @param connectionWidget the connection widget
         * @param replacementWidget the replacement widget
         * @param reconnectingSource if true, then source is being reconnected; if false, then target is being reconnected
         */
        public ConnectorState isReplacementWidget(ConnectionWidget connectionWidget, 
                                                  Widget replacementWidget, 
                                                  boolean reconnectingSource)
        {
            ConnectorState retVal = ConnectorState.REJECT;
            Object suggested = getScene().findObject(replacementWidget);

            MessageWidget messageW=(MessageWidget) connectionWidget;
            
            IPresentationElement sourceElement = null;
            IPresentationElement targetElement = null;
            boolean sameElement = true;
            if (reconnectingSource)
            {
                //case unsupported for messages (source reconnection)
            }
            else//target reconnection
            {
                targetElement = getScene().isNode(suggested) ? (IPresentationElement) suggested : null;
                sourceElement = originalSource;
                
                if(targetElement != originalTarget)
                {
                    sameElement = false;
                }
            }
            if(sourceElement==targetElement)
            {
                //disable creation of message to self by reconnection, isn't in 6.0 but may be enhanced later, currenly unsupported and cause troubles
                retVal=ConnectorState.REJECT;
            }
            else if ((sourceElement != null) && (targetElement != null) && !sameElement)
            {
                Widget sourceWidget=getScene().findWidget(sourceElement);
                MessagesConnectProvider connectProvider=new MessagesConnectProvider("Message", ((IMessage) connectionPE.getFirstSubject()).getKind(), null);
                //
                Point sourcePoint=messageW.getSourceAnchor().getRelatedWidget().getParentWidget().convertLocalToScene(messageW.getSourceAnchor().getRelatedWidget().getPreferredLocation());
                //
                retVal=connectProvider.isTargetWidget(sourceWidget, replacementWidget, sourcePoint, sourcePoint);
                if(retVal==ConnectorState.ACCEPT)
                {
                    if(targetElement.getFirstSubject() instanceof ICombinedFragment || targetElement.getFirstSubject() instanceof IInteraction)retVal=ConnectorState.REJECT;//can't reconnect from lifeline to an interaction or combined fragment
                }
            }
            else if(sameElement)
            {
                //return to the same target
                retVal=ConnectorState.ACCEPT;
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
            
            IPresentationElement replacementNode = (IPresentationElement)getScene().findObject(replacementWidget);
            if(replacementNode==originalTarget)return;//do not need to reconnect
            IPresentationElement edge = (IPresentationElement)getScene().findObject(connectionWidget);
            IMessage message = (IMessage) edge.getFirstSubject();
            if (reconnectingSource)
            {
                //unsupported
            }
            else
            {
                //getScene().setEdgeTarget(edge, replacementNode);
                MessageFactory factory = (MessageFactory) getRelationshipFactory(getMessageKindAsString(((IMessage)message).getKind())+message.getElementType());
                if(factory != null)
                {
                    new MessagesConnectProvider("Message", message.getKind(), null).reconnectMessage(getScene(),factory,edge,replacementNode);
                }
            }
            originalSource = null;
            originalTarget = null;
            connectionPE = null;
        }
        
        protected RelationshipFactory getRelationshipFactory(String type)
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
    }
    //////////
    private class SceneReconnectProvider implements ReconnectProvider
    {
        private IPresentationElement originalSource = null;
        private IPresentationElement originalTarget = null;
        private RelationValidator validator = new RelationValidator();

        public void reconnectingStarted(ConnectionWidget connectionWidget, 
                                        boolean reconnectingSource)
        {
            Widget sourceWidget = connectionWidget.getSourceAnchor().getRelatedWidget();
            originalSource = (IPresentationElement) getScene().findObject(sourceWidget);
            
            Widget targetWidget = connectionWidget.getTargetAnchor().getRelatedWidget();
            originalTarget = (IPresentationElement) getScene().findObject(targetWidget);
        }

        public void reconnectingFinished(ConnectionWidget connectionWidget, 
                                         boolean reconnectingSource)
        {
            originalSource = null;
            originalTarget = null;
        }

        public boolean isSourceReconnectable(ConnectionWidget connectionWidget)
        {
            boolean retVal = false;
            
            IPresentationElement element = (IPresentationElement) getScene().findObject(connectionWidget);
            if(element != null)
            {
                IElement actual = element.getFirstSubject();
                if(getRelationshipFactory(actual.getElementType()) != null)
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
                IElement actual = element.getFirstSubject();
                if(getRelationshipFactory(actual.getElementType()) != null)
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

            Object suggested = getScene().findObject(replacementWidget);

            IPresentationElement sourceElement = null;
            IPresentationElement targetElement = null;
            boolean sameElement = true;
            if (reconnectingSource == true)
            {
                sourceElement = getScene().isNode(suggested) ? (IPresentationElement) suggested : null;
                targetElement = originalTarget;
                
                if(sourceElement != originalSource)
                {
                    sameElement = false;
                }
            }
            else
            {
                targetElement = getScene().isNode(suggested) ? (IPresentationElement) suggested : null;
                sourceElement = originalSource;
                
                if(targetElement != originalTarget)
                {
                    sameElement = false;
                }
            }

            if ((sourceElement != null) && (targetElement != null) && (sameElement == false))
            {
                RelationProxy relationshipProxy = new RelationProxy();
                relationshipProxy.setFrom(sourceElement.getFirstSubject());
                relationshipProxy.setTo(targetElement.getFirstSubject());

                IPresentationElement relPresenation = (IPresentationElement) getScene().findObject(connectionWidget);
                IElement relElement = relPresenation.getFirstSubject();
                relationshipProxy.setConnectionElementType(relElement.getElementType());

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
                retVal=ConnectorState.ACCEPT;
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
            IPresentationElement replacementNode = (IPresentationElement)getScene().findObject(replacementWidget);
            if(reconnectingSource && replacementNode==originalSource)return;
            else if(!reconnectingSource && replacementNode==originalTarget)return;
            IPresentationElement edge = (IPresentationElement)getScene().findObject(connectionWidget);
            IRelationship relationship = (IRelationship)edge.getFirstSubject();
            
            if (replacementWidget == null)
            {
                getScene().removeEdge(edge);
                relationship.delete();
            }
            else if (reconnectingSource)
            {
                getScene().setEdgeSource(edge, replacementNode);
                RelationshipFactory factory = getRelationshipFactory(relationship.getElementType());
                if(factory != null)
                {
                    factory.reconnectSource(relationship, 
                                            originalSource.getFirstSubject(),
                                            replacementNode.getFirstSubject(),
                                            originalTarget.getFirstSubject());
                }
            }
            else
            {
                getScene().setEdgeTarget(edge, replacementNode);
                RelationshipFactory factory = getRelationshipFactory(relationship.getElementType());
                if(factory != null)
                {
                    factory.reconnectTarget(relationship, 
                                            originalTarget.getFirstSubject(),
                                            replacementNode.getFirstSubject(),
                                            originalSource.getFirstSubject());
                }
            }
        }
        
        
        protected RelationshipFactory getRelationshipFactory(String type)
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
    }

    /**
     * create new message between elements before specified message, expected to be used by a11y on selected elements
     * if no message is specified new message is added to the bottom
     * method is expected to be called in validated scene state, so all sized are defined and valid
     * @param fromW
     * @param toW
     * @param beforeMsg
     * @param msgKind
     */
    public ArrayList<ConnectionWidget> createMessageOnSelection(Widget fromW,Widget toW,MessageWidget beforeMsgW,int msgKind)
    {
        IPresentationElement fromPE=(IPresentationElement) getScene().findObject(fromW);
        IPresentationElement toPE=(IPresentationElement) getScene().findObject(toW);
        IPresentationElement nxtMsgPE=null;
        if(beforeMsgW!=null)nxtMsgPE=(IPresentationElement) getScene().findObject(beforeMsgW);
        MessagesConnectProvider msgConnProvider=new MessagesConnectProvider("Message", msgKind, "Lifeline");
        int x0=0,x1=0,y=0;
        Point sourcePoint=null;
        Point targetPoint=null;
        if(fromW instanceof LifelineWidget && toW instanceof LifelineWidget)//Lifeline to Lifeline case
        {
            //find position for a new message
            LifelineWidget fromLLW=(LifelineWidget) fromW;
            LifelineWidget toLLW=(LifelineWidget) toW;
            Rectangle rec0=fromLLW.convertSceneToLocal(fromLLW.getBounds());
            x0=rec0.x+rec0.width/2;
            Rectangle rec1=toLLW.convertLocalToScene(toLLW.getBounds());
            x1=rec1.x+rec1.width/2;
            int ymin=0;
                //lighter logic so separate it
                if(fromLLW.getLine().getChildren()!=null && fromLLW.getLine().getChildren().size()>0)
                {
                    Widget last=fromLLW.getLine().getChildren().get(fromLLW.getLine().getChildren().size()-1);
                    Rectangle bnd=last.convertLocalToScene(last.getBounds());
                    y=bnd.y+bnd.height+25;
                }
                else //no children
                {
                    Rectangle bnd=fromLLW.getLine().convertLocalToScene(fromLLW.getLine().getBounds());
                    y=bnd.y+15;
                    ymin=y;
                }
                if(toLLW.getLine().getChildren()!=null && toLLW.getLine().getChildren().size()>0)
                {
                    Widget last=toLLW.getLine().getChildren().get(toLLW.getLine().getChildren().size()-1);
                    Rectangle bnd=last.convertLocalToScene(last.getBounds());
                    y=Math.max(y, bnd.y+bnd.height+25);
                }
                else //no children
                {
                    Rectangle bnd=toLLW.getLine().convertLocalToScene(toLLW.getLine().getBounds());
                    y=Math.max(y,bnd.y+15);
                    ymin=Math.max(ymin,bnd.y+5);
                }
            if(beforeMsgW!=null)
            {
                MessagePinWidget sourcePin=(MessagePinWidget) beforeMsgW.getSourceAnchor().getRelatedWidget();
                MessagePinWidget targetPin=(MessagePinWidget) beforeMsgW.getTargetAnchor().getRelatedWidget();
                //default logic
                y=beforeMsgW.getSourceAnchor().getRelatedSceneLocation().y;
                //check if it's possible to draw because may be one of lifelines is created and do not allow to draw before some message
                if(y<ymin)return null;
                //
                int dy0=sourcePin.getMarginBefore()+1;
                int dy1=targetPin.getMarginBefore()+1;
                int dy=Math.max(dy0, dy1);
                bumpMessage(beforeMsgW, dy);//move selected down a bit, this way we will be sure there is free space and no issue as in 6.1. yet a better logic may be implemented to bump only if necessary
            }
        }
        else if((fromW instanceof CombinedFragmentWidget && toW instanceof LifelineWidget) || (toW instanceof CombinedFragmentWidget && fromW instanceof LifelineWidget))
        {
            CombinedFragmentWidget fromCfW=(CombinedFragmentWidget) ((fromW instanceof CombinedFragmentWidget) ? fromW : toW);
            LifelineWidget toLLW=(LifelineWidget) ((fromW instanceof CombinedFragmentWidget) ? toW : fromW);
            Rectangle rec1=toLLW.convertLocalToScene(toLLW.getBounds());
            x1=rec1.x+rec1.width/2;
            //look for closest border of cf
            Rectangle rec0=fromCfW.convertLocalToScene(fromCfW.getBounds());
            x0=rec0.x+(Math.abs(rec0.x-x1)<Math.abs(rec0.x+rec0.width-x1) ? 0 : rec0.width);
                y=rec0.y+15;
                int ymin=y;//can't draw new message above cf
                for(Widget w:fromCfW.getMainWidget().getChildren())
                {
                    if(w instanceof MessagePinWidget)
                    {
                        Rectangle pinBnd=w.convertLocalToScene(w.getBounds());
                        if(Math.abs(pinBnd.x-x0)<5)//on expected side, do not comparea with == because of possible 1-2px shifts
                        {
                            y=Math.max(pinBnd.y+5, y);
                        }
                    }
                }
                if(toLLW.getLine().getChildren()!=null && toLLW.getLine().getChildren().size()>0)
                {
                    Widget last=toLLW.getLine().getChildren().get(toLLW.getLine().getChildren().size()-1);
                    Rectangle bnd=last.convertLocalToScene(last.getBounds());
                    y=Math.max(y, bnd.y+bnd.height+25);
                }
                else //no children
                {
                    Rectangle bnd=toLLW.getLine().convertLocalToScene(toLLW.getLine().getBounds());
                    y=Math.max(y,bnd.y+15);
                    ymin=Math.max(ymin, bnd.y+5);
                }
            if(beforeMsgW!=null)
            {
                MessagePinWidget sourcePin=(MessagePinWidget) beforeMsgW.getSourceAnchor().getRelatedWidget();
                MessagePinWidget targetPin=(MessagePinWidget) beforeMsgW.getTargetAnchor().getRelatedWidget();
                //default logic
                y=beforeMsgW.getSourceAnchor().getRelatedSceneLocation().y;
                //check if it's possible to draw because may be one of lifelines is created and do not allow to draw before some message
                if(y<ymin)return null;
                //
                int dy0=sourcePin.getMarginBefore()+1;
                int dy1=targetPin.getMarginBefore()+1;
                int dy=Math.max(dy0, dy1);
                bumpMessage(beforeMsgW, dy);//move selected down a bit, this way we will be sure there is free space and no issue as in 6.1. yet a better logic may be implemented to bump only if necessary
            }
        }
        else
        {
            return null;//cf to cf or elements like Comment etc
        }
        sourcePoint=new Point(x0,y-new MessagePinWidget(getScene(), PINKIND.ASYNCHRONOUS_CALL_OUT).getMarginBefore()-1);//10 use common margine on op, may be bertter to use pin api to gt value
        targetPoint=new Point(x1,y-new MessagePinWidget(getScene(), PINKIND.ASYNCHRONOUS_CALL_OUT).getMarginBefore()-1);
        if(msgConnProvider.isTargetWidget(fromW, toW, sourcePoint, targetPoint)==ConnectorState.ACCEPT)//need to verify, for example second create message shouldn't be allowed
        {
             msgConnProvider.setRelationshipFactory(new MessageFactory());
             return msgConnProvider.createConnection(fromW, toW, sourcePoint, targetPoint);
        }
        return null;
    }
    @Override
    protected void setingValueChanged(String key, Object oldValue, Object newValue) {
        if(newValue!=oldValue)
        if(SHOW_RETURN_MESSAGES.equals(key))
        {
            LayerWidget layer=getScene().getConnectionLayer();
            boolean visible=newValue==Boolean.TRUE;
            for(Widget w:layer.getChildren())
            {
                if(w instanceof ResultMessageConnectionWidget)
                {
                    w.setVisible(visible);
                }
            }
        }
        else if(SHOW_INTERACTION_BOUNDARY.equals(key))
        {
            if(Boolean.TRUE.equals(newValue))
            {
                if(sqdBoundary==null)
                {
                    //
                    IDiagram diagram=getScene().getDiagram();
                    IInteraction inter=(IInteraction) diagram.getNamespace();
                    IPresentationElement presentation = Util.createNodePresentationElement();
                    presentation.addSubject(inter);
                    //
                    addWidget(presentation,new Point(0,0));
                    new AfterValidationExecutor(new ActionProvider()
                                                    {
                                                        public void perfomeAction() {
                                                            setInteractionBounds();
                                                        }
                                                    }
                                                    , getScene()
                                                    );
                    sqdBoundary.getParentWidget().revalidate();
                    getScene().revalidate();
                    getScene().validate();
                }
            }
            else if(sqdBoundary!=null)
            {
                sqdBoundary.remove();
                sqdBoundary=null;
                getScene().validate();
            }
        }
        else if(SHOW_MESSAGE_NUMBERS.equals(key))
        {
            LayerWidget layer=getScene().getConnectionLayer();
            boolean visible=newValue==Boolean.TRUE;
            if(visible)
            {
                for(Widget w:layer.getChildren())
                {
                    if(w instanceof MessageWidget)
                    {
                        IPresentationElement pE=(IPresentationElement) getScene().findObject(w);
                        Message mesg=(Message) (IMessage) pE.getFirstSubject();
                        MessageLabelManager labelManager=w.getLookup().lookup(MessageLabelManager.class);
                        if(labelManager!=null)
                        {
                            if(labelManager.isVisible(MessageLabelManager.NAME))
                            {
                                 labelManager.hideLabel(MessageLabelManager.NAME);
                                 labelManager.showLabel(MessageLabelManager.NAME);
                               //labelManager.propertyChange(new PropertyChangeEvent(getScene(), ModelElementChangedKind.NAME_MODIFIED.toString(), null, mesg.getName()));
                            }
                            else if(labelManager.isVisible(MessageLabelManager.OPERATION))
                            {
                                if(mesg.getKind()!=BaseElement.MK_RESULT)
                                {
                                     labelManager.hideLabel(MessageLabelManager.OPERATION);
                                     labelManager.showLabel(MessageLabelManager.OPERATION);
                                }
                            }
                            else if(mesg.getKind()!=BaseElement.MK_RESULT)
                            {
                                labelManager.showLabel(MessageLabelManager.NAME);
                            }
                        }
                    }
                }
            }
            else
            {
                for(Widget w:layer.getChildren())
                {
                    if(w instanceof MessageWidget)
                    {
                        IPresentationElement pE=(IPresentationElement) getScene().findObject(w);
                        Message mesg=(Message) (IMessage) pE.getFirstSubject();
                        MessageLabelManager labelManager=w.getLookup().lookup(MessageLabelManager.class);
                        if(labelManager!=null)
                        {
                            if(labelManager.isVisible(MessageLabelManager.NAME))
                            {
                                if(mesg.getNameWithAlias()==null || mesg.getNameWithAlias().length()<1)labelManager.hideLabel(MessageLabelManager.NAME);
                                else 
                                {
                                     labelManager.hideLabel(MessageLabelManager.NAME);
                                     labelManager.showLabel(MessageLabelManager.NAME);
                                }
                            }
                            else if(labelManager.isVisible(MessageLabelManager.OPERATION))if(mesg.getKind()!=BaseElement.MK_RESULT)
                            {
                                     labelManager.hideLabel(MessageLabelManager.OPERATION);
                                     labelManager.showLabel(MessageLabelManager.OPERATION);
                            }
                         }
                    }
                }
            }
        }
    }
    
    
    private void setInteractionBounds()
    {
        if(PersistenceUtil.isDiagramLoading())return;
        Widget widget=getScene().getMainLayer();
        Collection<Widget> children = widget.getChildren ();
        Rectangle bounds=null;
        for(Widget w:children)
        {
            Point loc=w.getLocation();
            if(w instanceof InteractionBoundaryWidget && ((InteractionBoundaryWidget)w).isBoundary())
            {
                //
            }
            else if(w.isVisible())//calculate only for visible widgets, for example do not calculate for invisible labels
            {
                Rectangle bnd=w.getBounds();
                //
                Insets ins=w.getBorder().getInsets();
                bnd.x+=ins.left+loc.x;
                bnd.y+=ins.bottom+loc.y;
                bnd.width-=ins.left+ins.right;
                bnd.height-=ins.top+ins.bottom;
                //
                if(bounds==null)
                {
                    bounds=new Rectangle(bnd);
                }
                else
                {
                    bounds.add(bnd);
                }
            }
        }
        boolean hide=getScene().getInterractionLayer().getChildren().size()>0;
        //but they are moved to this layer and also may not be yet validated etc, so easier to hide border instead of dinamic calculation
        if(sqdBoundary!=null)
        {
            if(bounds==null)//i.e. wasn't chnaged (no children)
            {
                bounds=new Rectangle(0,0,getScene().getView().getVisibleRect().width-20,getScene().getView().getVisibleRect().height/2);
            }
            
            if(hide)
            {
                //just do not update during action
            }
            else
            {
                boolean revalidateWithParent=false;
                Point loc=bounds.getLocation();
                bounds.x=0;
                bounds.y=0;
                loc.translate(-20,-20);
                if(loc.y<0)loc.y=0;
                bounds.width+=2*20;if(bounds.width<100)bounds.width=100;
                bounds.height+=2*20;
                Insets insets=sqdBoundary.getBorder().getInsets();
                Dimension toSet=new Dimension(bounds.width+insets.left+insets.right,bounds.height+insets.top+insets.bottom);
                if(!toSet.equals(sqdBoundary.getMinimumSize()))
                {
                    revalidateWithParent=true;
                    Dimension size=sqdBoundary.getMinimumSize();
                    if(size==null)size=new Dimension();
                    int dx=toSet.width-size.width;
                    int oldHalf=size.width/2;
                    sqdBoundary.setMinimumSize(toSet);
                    sqdBoundary.setPreferredBounds(null);//name of sqd may be long, so better to use min size
                    for(Widget w:sqdBoundary.getMainWidget().getChildren())//correct pins(messages to the boundary)
                    {
                        if(w instanceof MessagePinWidget)
                        {
                            Point pinLoc=w.getPreferredLocation();
                            if(pinLoc.x>oldHalf)
                            {
                                pinLoc.x+=dx;
                                w.setPreferredLocation(pinLoc);
                            }
                        }
                    }
                }
                if(!loc.equals(sqdBoundary.getPreferredLocation()))
                {
                    revalidateWithParent=true;
                    sqdBoundary.setPreferredLocation(loc);
                }
                if(sqdBoundary.getParentWidget().getChildren().indexOf(sqdBoundary)!=0)
                {
                    revalidateWithParent=true;
                    sqdBoundary.bringToBack();
                }
                if(revalidateWithParent)
                {
                    sqdBoundary.getParentWidget().revalidate();
                    getScene().validate();
                }
            }
        }
        
    }

    @Override
    public void buildToolBar(JToolBar bar, Lookup lookup) {
        super.buildToolBar(bar, lookup);
        //made some corrections
        JToolBar.Separator lastseparator=null;
        for(Component cmp:bar.getComponents())
        {
           if(cmp instanceof JButton)
            {
                JButton btn=(JButton) cmp;
                 if(btn.getAction() instanceof DiscoverRelationshipAction)
                {
                    //need to corret rel discovery button and remove all layout buttons
                    //btn.setAction(new SQDRelationshipDisovery(getScene()));
                     bar.remove(btn);
                }
                else if(btn.getAction() instanceof OrthogonalLayoutAction || btn.getAction() instanceof HierarchicalLayoutAction)
                {
                    //remove orthogonal and hierarchical layouts
                    bar.remove(btn);
                    if(lastseparator!=null)bar.remove(lastseparator);
                    lastseparator=null;
                    //TBD later may be added sqd layout here at this index
                }
            }
            else if(cmp instanceof JToolBar.Separator)
            {
                lastseparator=(JToolBar.Separator) cmp;
            }
        }
//        bar.add(new JButton(new ToolbarTestMessageCreateAction(getScene(), BaseElement.MK_ASYNCHRONOUS)));
//        bar.add(new JButton(new ToolbarTestMessageCreateAction(getScene(), BaseElement.MK_SYNCHRONOUS)));
//        bar.add(new JButton(new ToolbarTestMessageCreateAction(getScene(), BaseElement.MK_CREATE)));
    }
    
    public void diagramChanged()
    {
        if(Boolean.TRUE.equals(getSettingValue(SHOW_INTERACTION_BOUNDARY)) && !PersistenceUtil.isDiagramLoading())
        {
            new AfterValidationExecutor(new ActionProvider()
            {
                public void perfomeAction() {
                    setInteractionBounds();
                }
            },getScene());
            getScene().validate();
        }
    }
    
    public void bumpMessage(ConnectionWidget msg, int dy) {
        if(msg instanceof MessageWidget)
        {
            new ArrangeMoveWithBumping((MessageWidget)msg, new Point(0,0), new Point(0,dy)).perfomeAction();
        }
    }


    public SQDMessageConnectProvider getConnectProvider(IMessage call, IMessage result) {
            MessagesConnectProvider pr=new MessagesConnectProvider(call,result);
            pr.setRelationshipFactory(new MessageFactory());
        return pr;
    }

    @Override
    public void layout(boolean save) {
        revalidateSceneWithWait();
        ArrayList<LifelineWidget> lifelines=new ArrayList<LifelineWidget>();
        Collection<IPresentationElement> pesTmp=getScene().getNodes();
        for(IPresentationElement pe:pesTmp)
        {
            if(pe.getFirstSubject() instanceof ILifeline)
            {
                lifelines.add((LifelineWidget) getScene().findWidget(pe));
            }
        }
        final ArrayList<LifelineWidget> finlifelines=lifelines;
        Thread start=new Thread()
        {
            @Override
            public void run()
            {
                layoutLifelines(finlifelines);
            }
        };
        start.start();
        try {
            start.join(120000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private void layoutLifelines(ArrayList<LifelineWidget> lifelines)
    {
        //may distribute lifelines so lables on messages fit between lifelines
        //what else can be done?
        if(lifelines.size()>0)
        {
            Collections.sort(lifelines,new ComparatorLifelinesX());
            //
            for(int i=0;i<lifelines.size();i++)
            {
                LifelineWidget curLL=lifelines.get(i);
                IPresentationElement curPE=(IPresentationElement) getScene().findObject(curLL);
                Collection<IPresentationElement> edgesPE=getScene().findNodeEdges(curPE, true, true);
                for(IPresentationElement edgePE:edgesPE)
                {
                    IPresentationElement nxtPE=curPE;
                    if(getScene().getEdgeSource(edgePE)!=curPE)
                    {
                        nxtPE=getScene().getEdgeSource(edgePE);
                    }
                    else
                    {
                        nxtPE=getScene().getEdgeTarget(edgePE);
                    }
                    Widget ll1=getScene().findWidget(curPE);
                    Widget ll2=null;
                    if(nxtPE!=curPE)//not to self
                    {
                        ll2=getScene().findWidget(nxtPE);
                    }
                    else
                    {
                        if(i<(lifelines.size()-1))ll2=lifelines.get(i+1);
                        nxtPE=(IPresentationElement) getScene().findObject(ll2);
                    }
                    if(ll1!=ll2 && ll2!=null)
                    {
                        int curSpace=ll2.getPreferredLocation().x-ll1.getPreferredLocation().x;
                        int setSpace=curSpace;
                        if(ll2 instanceof LifelineWidget && curSpace>0)//nxt right to cur
                        {
                            if(edgePE.getFirstSubject() instanceof IMessage)
                            {
                                MessageWidget mW=(MessageWidget) getScene().findWidget(edgePE);
                                LabelManager lm=mW.getLookup().lookup(LabelManager.class);
                                Collection<Widget> labelsW=lm.getLabelMap().values();
                                for(Widget lW:labelsW)
                                {
                                    if(lW.isVisible())
                                    {
                                        if((lW.getBounds().width+50)>setSpace)
                                        {
                                            setSpace=lW.getBounds().width+50;
                                        }
                                    }
                                }
                                if(setSpace>curSpace)
                                {
                                    int dx=setSpace-curSpace;
                                    Point nxtLoc=ll2.getPreferredLocation();
                                    nxtLoc.x+=dx;
                                    ll2.setPreferredLocation(nxtLoc);
                                    if(tc instanceof SQDDiagramTopComponent)
                                    {
                                        ((SQDDiagramTopComponent)tc).getTrackBar().moveObject(nxtPE);
                                    }
                                    Widget prevLL=ll2;
                                    for(int k=lifelines.indexOf(ll2)+1;k<lifelines.size();k++)//move olso all lifelines right to moved, TBD: may be move only if necessary?
                                    {
                                        LifelineWidget moreLL=lifelines.get(k);
                                        nxtLoc=moreLL.getPreferredLocation();
                                        nxtLoc.x+=dx;
                                        moreLL.setPreferredLocation(nxtLoc);
                                        if(tc instanceof SQDDiagramTopComponent)
                                        {
                                            ((SQDDiagramTopComponent)tc).getTrackBar().moveObject((IPresentationElement) getScene().findObject(moreLL));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //
            revalidateSceneWithWait();
            //
        }
        layoutCombinedFragments();
    }
    
    private void layoutCombinedFragments()
    {
        //cover messages and covered lifelines, should works good for cf created by re operation
        //and support unconnected cf too, for example stack them on right side after lifelines
        //
        IElementLocator locator = new ElementLocator();
        ETList < IElement > pElements = locator.findElementsByQuery(interaction, ".//UML:CombinedFragment");
        if (pElements != null)
        {
            revalidateSceneWithWait();
            //first posiiton/sizes
            for(int i=pElements.size()-1;i>=0;i--)
            {
                IElement element=pElements.get(i);
                ETList<IPresentationElement> pes=element.getPresentationElements();
                for(int j=0;j<pes.size();j++)
                {
                    CombinedFragmentWidget cfW=(CombinedFragmentWidget) getScene().findWidget(pes.get(j));
                    if(cfW!=null)
                    {
                        cfW.resizeToModelContent();
                        revalidateSceneWithWait();
                        break;//layout only one of the same model combined fragments, TBD may it have sense to remove duplicates in layout?
                    }
                }
            }
            //second containment??
            for(int i=pElements.size()-1;i>=0;i--)
            {
                IElement element=pElements.get(i);
                ETList<IPresentationElement> pes=element.getPresentationElements();
                for(int j=0;j<pes.size();j++)
                {
                    CombinedFragmentWidget cfW=(CombinedFragmentWidget) getScene().findWidget(pes.get(j));
                    if(cfW!=null)
                    {
                        if(cfW.getContainer()!=null)cfW.getContainer().calculateChildren(true);
                        revalidateSceneWithWait();
                        break;//layout only one of the same model combined fragments, TBD may it have sense to remove duplicates in layout?
                    }
                }
            }
        }
        //
        layoutComments();
    }
    
    private void layoutComments()
    {
        //need to stack unconnected comments at some location
        //need to show near connected element
    }
    
    private void revalidateSceneWithWait()
    {
            Thread waitValidated=new Thread()
            {
                @Override
                public void run()
                {
                    getScene().revalidate();
                    getScene().validate();
                    while(!getScene().isValidated())
                    {
                        try {
                            sleep(10);
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            };
            waitValidated.start();
            try {
                waitValidated.join(60000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
    }

    private static class ComparatorLifelinesX implements Comparator<LifelineWidget> {

        public ComparatorLifelinesX() {
        }

        public int compare(LifelineWidget o1, LifelineWidget o2) {
            return o1.getPreferredLocation().x - o2.getPreferredLocation().x;
        }
    }
   
    /**
     * make all lifeline the same length, check min lenth fist also
     * lifeline need to be validated to have proper sizes
     * lifelines with destroy event will be increased to minimum only but based on 2nd parameter also
     * @param byLongers if true all will be align to the longerst one if false to the shortest one
     * @param destroyedFitToo if true destroyed lifelines will "forget" they are with destoy event
     * @param lflns list of lifelines to use in caclulations if null, all lifelines will be found and adjusted
     */
    public void normalizeLifelines(final boolean byLongers,final boolean destroyedFitToo,final ArrayList<LifelineWidget> lflns) {
        ArrayList<LifelineWidget> lifelines=lflns;
        if(lifelines==null)
        {
            lifelines=new ArrayList<LifelineWidget>();
            for(IPresentationElement pe:getScene().getNodes())
            {
                if(pe.getFirstSubject() instanceof ILifeline)
                {
                    LifelineWidget llW=(LifelineWidget) getScene().findWidget(pe);
                    if(llW!=null)
                    {
                        if(llW.getPreferredLocation()==null)
                        {
                            //not yet properly added lifeline, need to wait one more validation
                            new AfterValidationExecutor(new ActionProvider() {
                                public void perfomeAction() {
                                    normalizeLifelines(byLongers, destroyedFitToo, null);
                                }
                            }, getScene());
                            return;
                        }
                        lifelines.add(llW);
                    }
                }
            }
        }
        if(lifelines.size()<2)return;//have no sense to adjust one lifeline
        int maxY=0;
        int minY=Integer.MAX_VALUE;
        for(LifelineWidget llW:lifelines)
        {
            if(llW.getParentWidget()==null)continue;
            Rectangle llBnd=llW.getBounds();
            Insets llIns=llW.getBorder().getInsets();
            llBnd.x+=llIns.left;
            llBnd.width-=llIns.right+llIns.left;
            llBnd.y+=llIns.top;
            llBnd.height-=llIns.top+llIns.bottom;
            Point llLoc=llW.getPreferredLocation();
            if(llLoc==null)
            {
                llLoc=llW.getLocation();
            }
            llLoc=llW.getParentWidget().convertLocalToScene(llLoc);
            int botY=(llBnd.y+llBnd.height+llLoc.y);
            if(botY>maxY)
            {
                maxY=botY;
            }
            if(botY<minY)
            {
                minY=botY;
            }
        }
        //TBD take into account min possible size
        int minLen=getMimimumLifelinesBottom(lifelines);
        //
        int toSet=Math.max(minLen, byLongers ? maxY : minY);
        //
        for(LifelineWidget llW:lifelines)
        {
            Dimension minSize=llW.getMinimumSize();
            minSize.height=toSet-llW.getPreferredLocation().y;
            Insets llIns=llW.getBorder().getInsets();
            minSize.height+=llIns.top+llIns.bottom;
            if(destroyedFitToo || !llW.isDestroyed())llW.setMinimumSize(minSize);
        }
    }
    /**
     * 
     * @param lflns lflns list of lifelines to use in caclulations if null, all lifelines will be found and adjusted
     * @return
     */
    public int getMimimumLifelinesBottom(ArrayList<LifelineWidget> lflns)
    {
        ArrayList<LifelineWidget> lifelines=lflns;
        if(lifelines==null)
        {
            lifelines=new ArrayList<LifelineWidget>();
            for(IPresentationElement pe:getScene().getNodes())
            {
                if(pe.getFirstSubject() instanceof ILifeline)
                {
                    LifelineWidget llW=(LifelineWidget) getScene().findWidget(pe);
                    if(llW!=null)lifelines.add(llW);
                }
            }
        }

        int maxY=0;
        for(LifelineWidget llW:lifelines)
        {
            LifelineLineWidget lllW=llW.getLine();
            Widget lastCh=null;
            if(lllW.getChildren().size()>0)
            {
                lastCh=lllW.getChildren().get(lllW.getChildren().size()-1);
            }
            int tmpBot=0;
            if(lastCh!=null)
            {
                Rectangle tmp=lastCh.convertLocalToScene(lastCh.getBounds());
                tmpBot=tmp.y+tmp.height+10;
            }
            else
            {
                if(llW.getLine().getBounds()!=null)
                {
                    Rectangle tmp=llW.getLine().convertLocalToScene(llW.getLine().getBounds());
                    tmpBot=tmp.y+100;
                }
            }
            if(tmpBot>maxY)maxY=tmpBot;
        }
        return maxY;
    }

    private class SQDChangeListener implements ObjectSceneListener
    {

        public void objectAdded(ObjectSceneEvent event, Object addedObject) {
        }

        public void objectRemoved(ObjectSceneEvent event, Object removedObject) {
             diagramChanged();
        }

        public void objectStateChanged(ObjectSceneEvent event, Object changedObject, ObjectState previousState, ObjectState newState) {
        }

        public void selectionChanged(ObjectSceneEvent event, Set<Object> previousSelection, Set<Object> newSelection) {
             diagramChanged();
        }

        public void highlightingChanged(ObjectSceneEvent event, Set<Object> previousHighlighting, Set<Object> newHighlighting) {
        }

        public void hoverChanged(ObjectSceneEvent event, Object previousHoveredObject, Object newHoveredObject) {
             diagramChanged();
        }

        public void focusChanged(ObjectSceneEvent event, Object previousFocusedObject, Object newFocusedObject) {
        }
        
    }

    public void doNotUseTrackbar() {
        this.trackbarusage=false;
    }
}
