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

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.Set;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.modules.web.jsf.navigation.graph.actions.LinkCreateProvider;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.graph.GraphPinScene;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.EventProcessingType;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;
import org.netbeans.api.visual.action.EditProvider;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.action.WidgetAction.Chain;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.vmd.VMDConnectionWidget;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.modules.web.jsf.navigation.NavigationCaseNode;
import org.netbeans.modules.web.jsf.navigation.PageFlowNode;
import org.netbeans.modules.web.jsf.navigation.PageFlowView;
import org.netbeans.modules.web.jsf.navigation.PinNode;
import org.netbeans.modules.web.jsf.navigation.graph.actions.MapActionUtility;
import org.netbeans.modules.web.jsf.navigation.graph.actions.MapActionUtility.HandleDeleteAction2;
import org.netbeans.modules.web.jsf.navigation.graph.actions.PageFlowAcceptProvider;
import org.netbeans.modules.web.jsf.navigation.graph.actions.PageFlowPopupProvider;
import org.openide.actions.DeleteAction;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;

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
public class PageFlowScene extends GraphPinScene<PageFlowNode, NavigationCaseNode, PinNode> {
    
    private LayerWidget backgroundLayer = new LayerWidget(this);
    private LayerWidget mainLayer = new LayerWidget(this);
    private LayerWidget connectionLayer = new LayerWidget(this);
    private LayerWidget upperLayer = new LayerWidget(this);
    
    private Router router;
    
    private WidgetAction moveControlPointAction = ActionFactory.createOrthogonalMoveControlPointAction();
    //    private WidgetAction popupNodeAction = ActionFactory.createPopupMenuAction (new NodePopupMenuProvider(this));
    private WidgetAction popupGraphAction;
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
        
        popupGraphAction = ActionFactory.createPopupMenuAction(new PageFlowPopupProvider(this, tc));
        
        Chain actions = getActions();
        actions.addAction(ActionFactory.createZoomAction());
        actions.addAction(ActionFactory.createPanAction());
        actions.addAction(ActionFactory.createRectangularSelectAction(this, backgroundLayer));
        actions.addAction(popupGraphAction);        
        actions.addAction(createActionMap());
        addObjectSceneListener(new MyObjectSceneListener(), ObjectSceneEventType.OBJECT_SELECTION_CHANGED, ObjectSceneEventType.OBJECT_REMOVED);
        //        actions.addAction(dragNdropAction);
        //        actions.addAction(selectAction);
        
        
        
    }
    
    
    
    private WidgetAction createActionMap() {
        
        ActionMap actionMap = tc.getActionMap();
        CallbackSystemAction a = (CallbackSystemAction)SystemAction.get(DeleteAction.class);
        Action action = new HandleDeleteAction2(this);
        actionMap.put(a.getActionMapKey(), new MapActionUtility.HandleDeleteAction2(this));
        
        return ActionFactory.createActionMapAction(MapActionUtility.initInputMap(), MapActionUtility.initActionMap());
        
    }
    
    
    
    /**
     *
     * @return
     */
    public PageFlowView getPageFlowView(){
        return tc;
    }
    
    
    private final LabelWidget malFormedLabel = new LabelWidget(this, "Mal Formed Event Received. - http://www.netbeans.org/issues/show_bug.cgi?id=98570");
    /**
     * To show a mal formed page.
     */
    public void createMalFormedWidget() {
        List<Widget> widgets = getChildren();
        if( !widgets.contains(malFormedLabel)); {
            addChild(malFormedLabel);
            validate();
        }
        
    }
    
    /**
     * Removed the mal formed notes on the screen.
     */
    public void removeMalFormedWidget() {
        List<Widget> widgets = getChildren();
        if( widgets.contains(malFormedLabel)); {
            removeChild(malFormedLabel);
            validate();
        }
    }
    
    //    public boolean replacePageFlowNode( PageFlowNode oldNode, PageFlowNode newNode ){
    //        VMDNodeWidget widget = (VMDNodeWidget) findWidget(oldNode);
    //        widget.
    //    }
    
    /**
     * Implements attaching a widget to a node. The widget is VMDNodeWidget and has object-hover, select, popup-menu and move actions.
     * @param node the node
     * @return the widget attached to the node, will return null if
     */
    protected Widget attachNodeWidget(PageFlowNode node) {
        assert node != null;
        VMDNodeWidget nodeWidget = new VMDNodeWidget(this);
        nodeWidget.setNodeName(node.getDisplayName());
        
        Widget header = nodeWidget.getHeader();
        ImageWidget imageWidget = new DefaultAnchorWidget(this, Utilities.loadImage("org/netbeans/modules/visual/resources/vmd-pin.png"));
        imageWidget.getActions().addAction(connectAction);
        imageWidget.getActions().addAction(createWidgetHoverAction());
        header.addChild(imageWidget);
        header.getActions().addAction(createWidgetHoverAction());
        
        LabelWidget lblWidget = nodeWidget.getNodeNameWidget();
        
        lblWidget.getActions().addAction(
                ActionFactory.createInplaceEditorAction( new PageNodeTextFieldInplaceEditor(nodeWidget) ));
        
        
        
        mainLayer.addChild(nodeWidget);
        
        nodeWidget.getHeader().getActions().addAction(createObjectHoverAction());
        nodeWidget.getActions().addAction(selectAction);
        nodeWidget.getActions().addAction(moveAction);
        nodeWidget.setMinimized(true);
        Point point = PageFlowLayoutUtilities.getPreferredNodePosition(this,true);
        //        nodeWidget2Point.put(nodeWidget, point);
        nodeWidget.setPreferredLocation(point);
        
        //        nodeWidget.getActions().addAction(createActionMap());
        //        nodeWidget.getActions ().addAction (popupGraphAction);
        //        imageWidget.getActions().addAction(connectAction);
        
        return nodeWidget;
    }
    
    private Map<VMDNodeWidget,Point> nodeWidget2Point = new HashMap<VMDNodeWidget,Point>();
    
    /* This is needed by PageFlowLayoutUtilities*/
    public Rectangle getVisibleRect() {
        return tc.getVisibleRect();
    }
    
    
    private Queue emptyPositions = new LinkedList();
    
    @Override
    protected void detachNodeWidget(PageFlowNode node,
            Widget widget) {
        //        Point p = widget.getPreferredLocation();
        //        if ( (p.getX() - BORDER_OFFSET) %
        super.detachNodeWidget(node, widget);
        
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
     *
     * @param pageNode
     * @return
     */
    public PinNode getDefaultPin( PageFlowNode pageNode ){
        Collection<PinNode> pins = getNodePins(pageNode);
        for ( PinNode pin : pins ){
            if( pin.isDefault())
                return pin;
        }
        System.err.println("Some reason this node: " + pageNode + " does not have a pin.");
        return null;
    }
    
    /**
     * Implements attaching a widget to a pin. The widget is VMDPinWidget and has object-hover and select action.
     * The the node id ends with "#default" then the pin is the default pin of a node and therefore it is non-visual.
     * @param node the node
     * @param pinNode
     * @return the widget attached to the pin, null, if it is a default pin
     */
    protected Widget attachPinWidget(PageFlowNode node, PinNode pinNode) {
        assert node != null;
        
        if( pinNode.isDefault() ){
            return null;
        }
        
        VMDPinWidget widget = new VMDPinWidget(this);
        VMDNodeWidget nodeWidget = ((VMDNodeWidget) findWidget(node));
        nodeWidget.attachPinWidget(widget);
        
        widget.getActions().addAction(createObjectHoverAction());
        widget.getActions().addAction(createSelectAction());
        
        return widget;
    }
    
    //    public void replaceWidgetNode( PageFlowNode oldNode, PageFlowNode newNode ) {
    //        VMDNodeWidget widget = (VMDNodeWidget)findWidget(oldNode);
    //        oldNode = newNode;
    //        //        if ( widget != null ){
    //        //            widget.setNodeName(newNode.getDisplayName());
    //        //        }
    //        removeObject(oldNode);
    //        addObject(newNode, widget, widget.getChildren().get(0));
    //    }
    
    /**
     * Implements attaching a widget to an edge. the widget is ConnectionWidget and has object-hover, select and move-control-point actions.
     * @param edge
     * @return the widget attached to the edge
     */
    protected Widget attachEdgeWidget(NavigationCaseNode edge) {
        assert edge != null;
        
        VMDConnectionWidget connectionWidget = new VMDConnectionWidget(this, router);
        
        
        LabelWidget label = new LabelWidget(this, edge.getName());
        label.setOpaque(true);
        label.getActions().addAction(
                ActionFactory.createInplaceEditorAction(new CaseNodeTextFieldInplaceEditor()));
        
        connectionWidget.addChild(label);
        connectionWidget.setConstraint(label, LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_RIGHT, 10);
        
        connectionLayer.addChild(connectionWidget);
        
        connectionWidget.getActions().addAction(createObjectHoverAction());
        connectionWidget.getActions().addAction(selectAction);
        connectionWidget.getActions().addAction(moveControlPointAction);
        //        connectionWidget.getActions().addAction(createActionMap());
        
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
    protected void attachEdgeSourceAnchor(NavigationCaseNode edge, PinNode oldSourcePin, PinNode sourcePin) {
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
    protected void attachEdgeTargetAnchor(NavigationCaseNode edge, PinNode oldTargetPin, PinNode targetPin) {
        ((ConnectionWidget) findWidget(edge)).setTargetAnchor(getPinAnchor(targetPin));
    }
    
    /*
     * Returns the Anchor for a given pin
     * @param pin The Pin
     * @return Anchor the anchor location
     */
    private Anchor getPinAnchor(PinNode pin) {
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
            } else {
                userSelectionSuggested(Collections.emptySet(), invertSelection);
            }
        }
        
        
    }
    
    
    private final class CaseNodeTextFieldInplaceEditor implements TextFieldInplaceEditor {
        
        
        public boolean isEnabled(Widget arg0) {
            return true;
        }
        
        public String getText(Widget widget) {
            Node caseNode = (Node)findObject(widget.getParentWidget());
            return ((LabelWidget)widget).getLabel();
        }
        
        public void setText(Widget widget, String newName) {
            
            
            Node caseNode = (Node)findObject(widget.getParentWidget());
            String oldName = caseNode.getName();
            
            if ( caseNode.canRename() ) {
                caseNode.setName(newName);
            }
            
            ((LabelWidget)widget).setLabel(newName);
                        
        }
    }
    
    
    private final class PageNodeTextFieldInplaceEditor implements TextFieldInplaceEditor {
        private VMDNodeWidget nodeWidget;
        
        public PageNodeTextFieldInplaceEditor(VMDNodeWidget nodeWidget ) {
            this.nodeWidget = nodeWidget;
        }
        
        public boolean isEnabled(Widget widget) {
            return true;
        }
        public String getText(Widget widget) {
            Node pageNode = (Node)findObject(nodeWidget);
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
                
                //                if( oldName != newName ) {
                //                    renamePin(pageNode, oldName + "pin", newName + "pin");
                //                }
                
                ((LabelWidget) widget).setLabel(newName);
                validate();
            }
            
        }
        
        
        
    }
    
    
    
    private class MyObjectSceneListener implements ObjectSceneListener{
        public void objectAdded(ObjectSceneEvent event, Object addedObject) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void objectRemoved(ObjectSceneEvent event, Object removedObject) {
            
            /* Workaround for issue: 100275 
             * selectionChanged should have been sufficient to take care of the case when a selected object is removed */
            if ( getSelectedObjects().size() == 0 ){
                tc.setDefaultActivatedNode();
            }
        }
        
        public void objectStateChanged(ObjectSceneEvent event,
                Object changedObject,
                ObjectState previousState,
                ObjectState newState) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void selectionChanged(ObjectSceneEvent event,
                Set<Object> previousSelection,
                Set<Object> newSelection) {
            
            Set<Node> selected = new HashSet<Node>();
            for( Object obj : newSelection ){
                if( obj instanceof Node ) {
                    selected.add((Node)obj);
                }
            }
            
            if( selected.size() == 0 ){
                tc.setDefaultActivatedNode();
            } else {
                tc.setActivatedNodes(selected.toArray(new Node[selected.size()]));
            }
            
        }
        
        public void highlightingChanged(ObjectSceneEvent event,
                Set<Object> previousHighlighting,
                Set<Object> newHighlighting) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void hoverChanged(ObjectSceneEvent event,
                Object previousHoveredObject,
                Object newHoveredObject) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
        public void focusChanged(ObjectSceneEvent event,
                Object previousFocusedObject,
                Object newFocusedObject) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    //    private void renamePin( Node pageNode, PinNode oldPinName, PinNode newPinName ){
    //        assert pageNode != null;
    //        assert oldPinName != null;
    //        assert newPinName != null;
    //
    //        Collection<NavigationCaseNode> navSourceCases;
    //        Collection<NavigationCaseNode> navTargetCases;
    //
    //        if( oldPinName.equals(newPinName) ){
    //            //Don't do anything if they have the same name.
    //            return;
    //        }
    //
    //
    //        //Workaround: http://www.netbeans.org/issues/show_bug.cgi?id=98742
    //        try {
    //            navSourceCases = findPinEdges(oldPinName, true, false);
    //        } catch(NullPointerException npe) {
    //            npe.printStackTrace();
    //            System.err.println("Null Pointer Caught: ");
    //            System.err.println("http://www.netbeans.org/issues/show_bug.cgi?id=98742");
    //            navSourceCases = new ArrayList();
    //        }
    //
    //        //Workaround: http://www.netbeans.org/issues/show_bug.cgi?id=98742
    //        try {
    //            navTargetCases = findPinEdges(oldPinName, false, true);
    //        } catch(NullPointerException npe) {
    //            npe.printStackTrace();
    //            System.err.println("Null Pointer Caught: ");
    //            System.err.println("http://www.netbeans.org/issues/show_bug.cgi?id=98742");
    //            navTargetCases = new ArrayList();
    //        }
    //
    //        removePin(oldPinName);
    //        addPin(pageNode, newPinName);
    //
    //        //Doing this to make sure the associate pins are taken care of.
    //        for( NavigationCaseNode navSourceCase : navSourceCases){
    //            attachEdgeSourceAnchor(navSourceCase, oldPinName, newPinName);
    //        }
    //
    //        for( NavigationCaseNode navTargetCase : navTargetCases){
    //            attachEdgeTargetAnchor(navTargetCase, oldPinName, newPinName);
    //        }
    //
    //
    //    }
    
    
}


