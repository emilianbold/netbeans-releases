/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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
package org.netbeans.modules.web.jsf.navigation.graph;

import java.awt.Image;
import java.awt.Point;
import java.util.Collection;
import org.netbeans.modules.web.jsf.navigation.graph.actions.DeleteAction;
import org.netbeans.modules.web.jsf.navigation.graph.actions.GraphPopupProvider;
import org.netbeans.modules.web.jsf.navigation.graph.actions.LinkCreateProvider;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.graph.GraphPinScene;
import org.netbeans.api.visual.graph.layout.GridGraphLayout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.EventProcessingType;
import java.util.Collections;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;
import org.netbeans.api.visual.action.EditProvider;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.action.WidgetAction.Chain;
import org.netbeans.api.visual.graph.layout.GridGraphLayout;
import org.netbeans.api.visual.graph.layout.GridGraphLayout;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.vmd.VMDConnectionWidget;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.modules.web.jsf.navigation.NavigationCaseNode;
import org.netbeans.modules.web.jsf.navigation.PageFlowView;
import org.netbeans.modules.web.jsf.navigation.graph.actions.PageFlowAcceptProvider;
import org.netbeans.modules.web.jsf.navigation.graph.actions.PageFlowPopupProvider;
import org.openide.nodes.Node;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

/**
 * This class represents a GraphPinScene for the Navigation Editor which is soon to be the Page Flow Editor.
 * Nodes are represented by a Page, Edges by a Link, and components by a Pin.
 * Graphics were taken from the VMDGraphScene designed by David Kaspar for mobility pack.
 * The visualization is done by: VMDNodeWidget for nodes, VMDPinWidget for pins, ConnectionWidget fro edges.
 * <p>
 * The scene has 4 layers: background, main, connection, upper.
 * <p>
 * The scene has following actions: zoom, panning, rectangular selection.
 *
 * @author Joelle Lam
 */
// TODO - remove popup menu action
public class PageFlowScene extends GraphPinScene<Node, NavigationCaseNode, String> {
    
    private LayerWidget backgroundLayer = new LayerWidget(this);
    private LayerWidget mainLayer = new LayerWidget(this);
    private LayerWidget connectionLayer = new LayerWidget(this);
    private LayerWidget upperLayer = new LayerWidget(this);
    
    private Router router;
    
    private WidgetAction moveControlPointAction = ActionFactory.createOrthogonalMoveControlPointAction();
    //    private WidgetAction popupNodeAction = ActionFactory.createPopupMenuAction (new NodePopupMenuProvider(this));
    private WidgetAction popupGraphAction = ActionFactory.createPopupMenuAction(new PageFlowPopupProvider(this));
    private WidgetAction moveAction = ActionFactory.createMoveAction();
    private WidgetAction dragNdropAction = ActionFactory.createAcceptAction(new PageFlowAcceptProvider());
    private WidgetAction connectAction = ActionFactory.createConnectAction(connectionLayer, new LinkCreateProvider(this));
    //    private WidgetAction deleteAction = new DeleteAction(this);
    private WidgetAction selectAction = ActionFactory.createSelectAction(new PageFlowSelectProvider());
    
    private SceneLayout sceneLayout;
    private PageFlowView tc;
    
    /**
     * Creates a VMD graph scene.
     * @param tc or TopComponent/container.
     */
    public PageFlowScene(PageFlowView tc) {
        this.tc = tc;
        
        setKeyEventProcessingType(EventProcessingType.FOCUSED_WIDGET_AND_ITS_PARENTS);
        
        addChild(backgroundLayer);
        addChild(mainLayer);
        addChild(connectionLayer);
        addChild(upperLayer);
        
        router = RouterFactory.createOrthogonalSearchRouter(mainLayer, connectionLayer);
        
        Chain actions = getActions();
        actions.addAction(ActionFactory.createZoomAction());
        actions.addAction(ActionFactory.createPanAction());
        actions.addAction(ActionFactory.createRectangularSelectAction(this, backgroundLayer));
        
        actions.addAction(popupGraphAction);
        actions.addAction(dragNdropAction);
        //        getActions ().addAction (deleteAction);
        
        
        GridGraphLayout gglayout = new GridGraphLayout<Node, NavigationCaseNode> ();
        gglayout.setChecker(true);
        
        sceneLayout = LayoutFactory.createSceneGraphLayout(this, gglayout);
        
        getActions().addAction(ActionFactory.createEditAction(new EditProvider() {
            public void edit(Widget widget) {
                sceneLayout.invokeLayout();
            }
        }));
    }
    
    /**
     *
     * @return
     */
    public PageFlowView getPageFlowView(){
        return tc;
    }
    
    //    protected PageFlowController getPageFlowController() {
    //        return tc.getPageFlowController();
    //    }
    //
    private static final Image POINT_SHAPE_IMAGE = Utilities.loadImage("org/netbeans/modules/visual/resources/vmd-pin.png"); // NOI18N
    
    private static final int PAGE_WIDGET_INDEX = 0;
    private static final int DEFAULT_PIN_WIDGET_INDEX = 1;
    
    /**
     * To show a mal formed page.
     */
    public void createMalFormedWidget() {
        //Not sure what to do yet.
    }
    
    /**
     * Implements attaching a widget to a node. The widget is VMDNodeWidget and has object-hover, select, popup-menu and move actions.
     * @param node the node
     * @return the widget attached to the node, will return null if
     */
    protected Widget attachNodeWidget(Node node) {
        assert node != null;
        VMDNodeWidget nodeWidget = new VMDNodeWidget(this);
        nodeWidget.setNodeName(node.getDisplayName());
        
        Widget header = nodeWidget.getHeader();
        ImageWidget imageWidget = new DefaultAnchorWidget(this, POINT_SHAPE_IMAGE);
        imageWidget.getActions().addAction(connectAction);
        //        imageWidget.getActions().addAction(createSelectAction());
        imageWidget.getActions().addAction(createWidgetHoverAction());
        header.addChild(imageWidget);
        header.getActions().addAction(createWidgetHoverAction());
        
        LabelWidget lblWidget = nodeWidget.getNodeNameWidget();
        lblWidget.getActions().addAction(
                ActionFactory.createInplaceEditorAction( new PageFlowTextFieldInplaceEditor(nodeWidget) ));
        
        
        //        Widget widget = new Widget(this);
        //        ImageWidget imageWidget = new ImageWidget(this, POINT_SHAPE_IMAGE);
        //        widget.setLayout(new NodePinLayout(0) );
        //
        //        widget.addChild(nodeWidget);
        //        widget.addChild(imageWidget);
        
        mainLayer.addChild(nodeWidget);
        
        
        //        nodeWidget.getActions().addAction(deleteAction);
        //        nodeWidget.getHeader  ().getActions().addAction(createObjectHoverAction());
        nodeWidget.getActions().addAction(selectAction);
        //        nodeWidget.getActions ().addAction (popupGraphAction);
        nodeWidget.getActions().addAction(moveAction);
        nodeWidget.setMinimized(true);
        //        imageWidget.getActions().addAction(connectAction);
        
        return nodeWidget;
    }
    
    
    private static class DefaultAnchorWidget extends ImageWidget{
        
        
        public DefaultAnchorWidget( PageFlowScene scene, Image image ){
            super(scene, image);
        }
        protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
            Border BORDER_HOVERED = (Border) javax.swing.BorderFactory.createLineBorder(java.awt.Color.BLACK);
            Border BORDER = BorderFactory.createEmptyBorder();
            if (previousState.isHovered()  == state.isHovered())
                return;
            setBorder(state.isHovered() ? BORDER_HOVERED : BORDER );
            
        }
        
        
    }
    
    /**
     * Implements attaching a widget to a pin. The widget is VMDPinWidget and has object-hover and select action.
     * The the node id ends with "#default" then the pin is the default pin of a node and therefore it is non-visual.
     * @param node the node
     * @param pin the pin
     * @return the widget attached to the pin, null, if it is a default pin
     */
    protected Widget attachPinWidget(Node node, String pin) {
        assert node != null;
        
        if( node.equals(pin)){
            return null;
        }
        
        VMDPinWidget widget = new VMDPinWidget(this);
        //        this.addObject(pin, widget);
        VMDNodeWidget nodeWidget = ((VMDNodeWidget) findWidget(node));
        nodeWidget.attachPinWidget(widget);
        
        //        widget.getActions().addAction(deleteAction);
        widget.getActions().addAction(createObjectHoverAction());
        widget.getActions().addAction(createSelectAction());
        //        widget.getActions().addAction(connectAction);
        
        return widget;
    }
    
    /**
     * Implements attaching a widget to an edge. the widget is ConnectionWidget and has object-hover, select and move-control-point actions.
     * @param edge
     * @return the widget attached to the edge
     */
    protected Widget attachEdgeWidget(NavigationCaseNode edge) {
        assert edge != null;
        
        VMDConnectionWidget connectionWidget = new VMDConnectionWidget(this, router);
        //Should this be moved to scene?
        LabelWidget label = new LabelWidget(this, edge.getFromOuctome());
        label.setOpaque(true);
        connectionWidget.addChild(label);
        connectionWidget.setConstraint(label, LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_RIGHT, 10);
        
        connectionLayer.addChild(connectionWidget);
        //        String navCaseName = link.getFromOutcome();
        
        //        connectionWidget.getActions().addAction(deleteAction);
        connectionWidget.getActions().addAction(createObjectHoverAction());
        //        connectionWidget.getActions().addAction(createSelectAction());
        connectionWidget.getActions().addAction(selectAction);
        connectionWidget.getActions().addAction(moveControlPointAction);
        //        connectionWidget.getActions ().addAction (deleteAction);
        return connectionWidget;
    }
    
    /**
     * Attaches an anchor of a source pin an edge.
     * The anchor is a ProxyAnchor that switches between the anchor attached to the pin widget directly and
     * the anchor attached to the pin node widget based on the minimize-state of the node.
     * @param edge the edge
     * @param oldSourcePin the old source pin
     * @param sourcePin the new source pin
     */
    protected void attachEdgeSourceAnchor(NavigationCaseNode edge, String oldSourcePin, String sourcePin) {
        ((ConnectionWidget) findWidget(edge)).setSourceAnchor(getPinAnchor(sourcePin));
    }
    
    /**
     * Attaches an anchor of a target pin an edge.
     * The anchor is a ProxyAnchor that switches between the anchor attached to the pin widget directly and
     * the anchor attached to the pin node widget based on the minimize-state of the node.
     * @param edge the edge
     * @param oldTargetPin the old target pin
     * @param targetPin the new target pin
     */
    protected void attachEdgeTargetAnchor(NavigationCaseNode edge, String oldTargetPin, String targetPin) {
        ((ConnectionWidget) findWidget(edge)).setTargetAnchor(getPinAnchor(targetPin));
    }
    
    /*
     * Returns the Anchor for a given pin
     * @param pin The Pin
     * @return Anchor the anchor location
     */
    private Anchor getPinAnchor(String pin) {
        if( pin == null ) {
            return null;
        }
        VMDNodeWidget nodeWidget = (VMDNodeWidget) findWidget(getPinNode(pin));
        Widget pinMainWidget = findWidget(pin);
        Anchor anchor;
        if (pinMainWidget != null) {
            anchor = AnchorFactory.createDirectionalAnchor(pinMainWidget, AnchorFactory.DirectionalAnchorKind.HORIZONTAL, 8);
            anchor = nodeWidget.createAnchorPin(anchor);
        } else
            anchor = nodeWidget.getNodeAnchor();
        return anchor;
    }
    
    /**
     * Invokes layout of the scene.
     */
    public void layoutScene() {
        sceneLayout.invokeLayout();
    }
    
    /**
     * Invokes the Layout Immediately.
     */
    public void layoutSceneImmediately() {
        sceneLayout.invokeLayoutImmediately();
    }
    
    private static class MyPopupMenuProvider implements PopupMenuProvider {
        
        public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
            JPopupMenu popupMenu = new JPopupMenu();
            popupMenu.add(new JMenuItem("Open " + ((VMDNodeWidget) widget).getNodeName()));
            return popupMenu;
        }
        
    }
    
    
    
    private final class PageFlowSelectProvider implements SelectProvider {
        
        public boolean isAimingAllowed(Widget widget, Point localLocation, boolean invertSelection) {
            return false;
        }
        
        public boolean isSelectionAllowed(Widget widget, Point localLocation, boolean invertSelection) {
            Object object = findObject(widget);
            return object != null  &&  (invertSelection  ||  ! getSelectedObjects().contains(object));
        }
        
        public void select(Widget widget, Point localLocation, boolean invertSelection) {
            Object object = findObject(widget);
            
            setFocusedObject(object);
            if (object != null) {
                if (getSelectedObjects().contains(object))
                    return;
                userSelectionSuggested(Collections.singleton(object), invertSelection);
                tc.setActivatedNodes(new Node[]{(Node)object});
            } else
                userSelectionSuggested(Collections.emptySet(), invertSelection);
        }
    }
    
    private final class PageFlowTextFieldInplaceEditor implements TextFieldInplaceEditor {
        private VMDNodeWidget nodeWidget;
        
        public PageFlowTextFieldInplaceEditor(VMDNodeWidget nodeWidget ) {
            this.nodeWidget = nodeWidget;
        }
        
        public boolean isEnabled(Widget widget) {
            return true;
        }
        public String getText(Widget widget) {
            Node pageNode = (Node)findObject(nodeWidget);
            //                return ((LabelWidget) widget).getLabel();
            //                if( pageNode instanceof DataNode ){
            return pageNode.getName();
        }
        public void setText(Widget widget, String text) {
            
            Node pageNode = (Node)findObject(nodeWidget);
            if ( pageNode.canRename() && !text.equals(pageNode.getName())) {
                
                //Explicitly declared oldName and newName for ease of reading.
                String oldName = pageNode.getDisplayName();
                String newName;
                
                pageNode.setName(text);
                newName = pageNode.getDisplayName();
                
                renamePin(pageNode, oldName + "pin", newName + "pin");
                
                ((LabelWidget) widget).setLabel(newName);
                validate();
            }
            
            
        }
        
    }
    
    private void renamePin( Node pageNode, String oldPinName, String newPinName ){
        if( oldPinName.equals(newPinName) ){
            //Don't do anything if they have the same name.
            return;
        }
        Collection<NavigationCaseNode> navSourceCases = findPinEdges(oldPinName, true, false);
        Collection<NavigationCaseNode> navTargetCases = findPinEdges(oldPinName, false, true);
        removePin(oldPinName);
        addPin(pageNode, newPinName);
        
        //Doing this to make sure the associate pins are taken care of.
        for( NavigationCaseNode navSourceCase : navSourceCases){
            attachEdgeSourceAnchor(navSourceCase, oldPinName, newPinName);
        }
        
        for( NavigationCaseNode navTargetCase : navTargetCases){
            attachEdgeTargetAnchor(navTargetCase, oldPinName, newPinName);
        }
        
        
    }
    
    
}


