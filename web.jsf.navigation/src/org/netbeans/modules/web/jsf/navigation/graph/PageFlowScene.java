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

import org.netbeans.modules.web.jsf.navigation.graph.layout.FreePlaceNodesLayouter;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.modules.web.jsf.navigation.graph.actions.LinkCreateProvider;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.graph.GraphPinScene;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.EventProcessingType;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.Popup;
import javax.swing.border.Border;
import org.netbeans.api.visual.action.EditProvider;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.action.WidgetAction.Chain;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.vmd.VMDColorScheme;
import org.netbeans.api.visual.vmd.VMDConnectionWidget;
import org.netbeans.api.visual.vmd.VMDFactory;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.modules.web.jsf.navigation.NavigationCaseEdge;
import org.netbeans.modules.web.jsf.navigation.Page;
import org.netbeans.modules.web.jsf.navigation.PageFlowView;
import org.netbeans.modules.web.jsf.navigation.Pin;
import org.netbeans.modules.web.jsf.navigation.graph.actions.MapActionUtility;
import org.netbeans.modules.web.jsf.navigation.graph.actions.MyActionMapAction;
import org.netbeans.modules.web.jsf.navigation.graph.actions.PageFlowAcceptProvider;
import org.netbeans.modules.web.jsf.navigation.graph.actions.PageFlowDeleteAction;
import org.netbeans.modules.web.jsf.navigation.graph.actions.PageFlowPopupProvider;
import org.netbeans.modules.web.jsf.navigation.graph.layout.ConnectionWrapperLayout;
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
public class PageFlowScene extends GraphPinScene<Page, NavigationCaseEdge, Pin> {

    private static final VMDColorScheme scheme = VMDFactory.getNetBeans60Scheme();
    private final LayerWidget backgroundLayer = new LayerWidget(this);
    private final LayerWidget mainLayer = new LayerWidget(this);
    private final LayerWidget connectionLayer = new LayerWidget(this);
    private final LayerWidget upperLayer = new LayerWidget(this);

    private final Router router;
    /**
     * The maximum is used for determining which router to used.  If either
     * edges or pages exceed the max, the direct routing algorithm will be used
     **/
    private static final int MAX_EDGES = 20; 
    private static final int MAX_PAGES = 20;
    private final Router routerDirect = RouterFactory.createDirectRouter();

    private final WidgetAction moveControlPointAction = ActionFactory.createOrthogonalMoveControlPointAction();
    //    private WidgetAction popupNodeAction = ActionFactory.createPopupMenuAction (new NodePopupMenuProvider(this));
    private final WidgetAction moveAction = ActionFactory.createMoveAction();
    //private final WidgetAction dragNdropAction = ActionFactory.createAcceptAction(new PageFlowAcceptProvider());
    private final WidgetAction connectAction = ActionFactory.createConnectAction(connectionLayer, new LinkCreateProvider(this));
    private final WidgetAction selectAction = ActionFactory.createSelectAction(new PageFlowSelectProvider());
    private final WidgetAction doubleClickAction = ActionFactory.createEditAction(new PageNodeEditAction());

    private PageFlowView tc;
    private final PopupMenuProvider popupProvider; //Please see POPUP_HACK below.
    private static Paint PAINT_BACKGROUND;
    static {
        Image sourceImage = Utilities.loadImage("org/netbeans/modules/web/jsf/navigation/graph/resources/paper_grid.png"); // NOI18N
        int width = sourceImage.getWidth(null);
        int height = sourceImage.getHeight(null);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.drawImage(sourceImage, 0, 0, null);
        graphics.dispose();
        PAINT_BACKGROUND = new TexturePaint(image, new Rectangle(0, 0, width, height));
    }



    /**
     * Creates a VMD graph scene.
     * @param tc or TopComponent/container.
     */
    public PageFlowScene(PageFlowView tc) {
        super();
        this.tc = tc;

        setOpaque(true);
        setBackground(PAINT_BACKGROUND);

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
        /*** POPUP_HACK: I have no access to PopupAction so I can't look through the actions and determine which one is a popup.
         * In order to added accessibility to popup I need access to this provider unless an API is created
         * to figure this out another means.
         **/
        popupProvider = new PageFlowPopupProvider(this, tc);
        actions.addAction(ActionFactory.createPopupMenuAction(popupProvider));
        actions.addAction(createActionMap());
        addObjectSceneListener(new MyObjectSceneListener(), ObjectSceneEventType.OBJECT_SELECTION_CHANGED);


        /* Temporary workaround  ISSUE# 107506 Still an issue. */
        //InputMap inputMap = MapActionUtility.initInputMap();
        //ActionMap actionMap = MapActionUtility.initActionMap();
        //actions.addAction(ActionFactory.createActionMapAction(inputMap, actionMap));
        //MyActionMapAction action = new MyActionMapAction(null, null);
        FreePlaceNodesLayouter fpnl = new FreePlaceNodesLayouter(this, tc.getVisibleRect());
    }



    private WidgetAction createActionMap() {

        ActionMap actionMap = tc.getActionMap();
        CallbackSystemAction a = (CallbackSystemAction) SystemAction.get(DeleteAction.class);
        actionMap.put(a.getActionMapKey(), new PageFlowDeleteAction(this));

        //Temporary workaround  ISSUE# 107506
        return new MyActionMapAction(MapActionUtility.initInputMap(), MapActionUtility.initActionMap());
        //return ActionFactory.createActionMapAction(MapActionUtility.initInputMap(), MapActionUtility.initActionMap());
    }

    /**
     * Get the PageFlowView TopComponent
     * @return PageFlowView
     */
    public PageFlowView getPageFlowView() {
        return tc;
    }


    private final LabelWidget malFormedLabel = new LabelWidget(this, "Your XML is Malformed.");

    /**
     * To show a mal formed page.
     */
    public void createMalFormedWidget() {
        List<Widget> widgets = getChildren();
        if (!widgets.contains(malFormedLabel)) {
            addChild(malFormedLabel);
            validate();
        }
    }

    /**
     * Removed the mal formed notes on the screen.
     */
    public void removeMalFormedWidget() {
        List<Widget> widgets = getChildren();
        if (widgets.contains(malFormedLabel)) {
            removeChild(malFormedLabel); //Removed major bug... Not sure what I was doing before...
            validate();
        }
    }



    /**
     * Implements attaching a widget to a node. The widget is VMDNodeWidget and has object-hover, select, popup-menu and move actions.
     * @param node the node
     * @return the widget attached to the node, will return null if
     */
    protected Widget attachNodeWidget(Page page) {
        assert page != null;
        VMDNodeWidget nodeWidget = new VMDNodeWidget(this, scheme);
        String displayName = page.getDisplayName();
        nodeWidget.setNodeName(displayName);

        Widget header = nodeWidget.getHeader();
        ImageWidget imageWidget = new DefaultAnchorWidget(this, Utilities.loadImage("org/netbeans/modules/visual/resources/vmd-pin.png"));
        imageWidget.getActions().addAction(connectAction);
        imageWidget.getActions().addAction(createWidgetHoverAction());
        header.addChild(imageWidget);
        header.getActions().addAction(createWidgetHoverAction());

        LabelWidget lblWidget = nodeWidget.getNodeNameWidget();

        lblWidget.getActions().addAction(ActionFactory.createInplaceEditorAction(new PageNodeTextFieldInplaceEditor(nodeWidget)));

        mainLayer.addChild(nodeWidget);
        //updateNodeActions(nodeWidget);
        nodeWidget.getHeader().getActions().addAction(createObjectHoverAction());
        nodeWidget.getHeader().getActions().addAction(doubleClickAction); //not still the glory from pins.
        nodeWidget.getActions().addAction(selectAction);
        nodeWidget.getActions().addAction(moveAction);
        nodeWidget.setMinimized(true);

        /*
        if ( node.getPinNodes().size() == 0 ){
        nodeWidget.setMinimized(true);
        }
         */

        return nodeWidget;
    }
    private WidgetAction pageSpecificActionMapAction = null;

    public final void updateNodeWidgetActions(Page page) {
        Widget nodeWidget = findWidget(page);
        if (nodeWidget != null) {
            if (pageSpecificActionMapAction != null) {
                nodeWidget.getActions().removeAction(pageSpecificActionMapAction);
            }
            pageSpecificActionMapAction = createActionMapAction(page);
            if (pageSpecificActionMapAction != null) {
                nodeWidget.getActions().addAction(pageSpecificActionMapAction);
            }
        }
    }

    private WidgetAction createActionMapAction(Page page) {
        InputMap inputMap = new InputMap();
        ActionMap actionMap = new ActionMap();
        Action[] actions = page.getActions(true);
        for (Action action : actions) {
            KeyStroke keyStroke = (KeyStroke) action.getValue(javax.swing.Action.ACCELERATOR_KEY);
            if (keyStroke != null) {
                inputMap.put(keyStroke, action.toString());
                actionMap.put(action.toString(), action);
            }
        }
        if (actionMap.size() < 1) {
            return null;
        }
        /* Not sure if it is the right thing to create a new action map
         * should I be adding it?
         */
        return new MyActionMapAction(inputMap, actionMap);


        //return  ActionFactory.createActionMapAction(inputMap, actionMap);
    }

    //private Map<VMDNodeWidget, Point> nodeWidget2Point = new HashMap<VMDNodeWidget, Point>();

    /* This is needed by PageFlowLayoutUtilities*/
    public Rectangle getVisibleRect() {
        return tc.getVisibleRect();
    }


    //    private Queue emptyPositions = new LinkedList();
    @Override
    protected void detachNodeWidget(Page node, Widget widget) {
        //        Point p = widget.getPreferredLocation();
        //        if ( (p.getX() - BORDER_OFFSET) %
        super.detachNodeWidget(node, widget);
    }



    private static class DefaultAnchorWidget extends ImageWidget {

        public DefaultAnchorWidget(PageFlowScene scene, Image image) {
            super(scene, image);
        }

        @Override
        protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
            Border BORDER_HOVERED = (Border) javax.swing.BorderFactory.createLineBorder(java.awt.Color.BLACK);
            Border BORDER = BorderFactory.createEmptyBorder();
            if (previousState.isHovered() == state.isHovered()) {
                return;
            }
            setBorder(state.isHovered() ? BORDER_HOVERED : BORDER);
        }
    }


    /**
     *
     * @param pageNode
     * @return
     */
    public Pin getDefaultPin(Page pageNode) {
        Collection<Pin> pins = getNodePins(pageNode);
        if (pins == null) {
            System.err.println("Node is null?: " + pageNode);
        }
        for (Pin pin : pins) {
            if (pin.isDefault()) {
                return pin;
            }
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
    protected Widget attachPinWidget(Page node, Pin pinNode) {
        assert node != null;

        if (pinNode.isDefault()) {
            return null;
        }

        VMDPinWidget widget = new VMDPinWidget(this, scheme);
        VMDNodeWidget nodeWidget = (VMDNodeWidget) findWidget(node);
        if (nodeWidget != null) {
            nodeWidget.attachPinWidget(widget);
            widget.setProperties(pinNode.getName(), Arrays.asList(pinNode.getIcon(0)));


            Chain actions = widget.getActions();
            actions.addAction(createObjectHoverAction());
            actions.addAction(createSelectAction());
            actions.addAction(connectAction);
            actions.addAction(doubleClickAction);
        } else {
            System.err.println("Node widget should not be null.");
        }

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
    protected Widget attachEdgeWidget(NavigationCaseEdge edge) {
        assert edge != null;


        VMDConnectionWidget connectionWidget;

        if (edge.isModifiable()) {
            connectionWidget = new VMDConnectionWidget(this, scheme);
        } else {
            connectionWidget = new VMDConnectionWidget(this, new PFENotModifiableScheme());
        }
        if (getEdges().size() > MAX_EDGES || getNodes().size() > MAX_PAGES) {
            connectionWidget.setRouter(routerDirect);
        } else {
            connectionWidget.setRouter(router);
        }


        LabelWidget label = new LabelWidget(this, edge.getName());
        label.setOpaque(true);
        label.getActions().addAction(ActionFactory.createInplaceEditorAction(new PageFlowScene.CaseNodeTextFieldInplaceEditor()));

        connectionLayer.addChild(connectionWidget);

        connectionWidget.getActions().addAction(createObjectHoverAction());
        connectionWidget.getActions().addAction(selectAction);
        connectionWidget.getActions().addAction(moveControlPointAction);
        connectionWidget.getActions().addAction(doubleClickAction);

        connectionWidget.setLayout(new ConnectionWrapperLayout(connectionWidget, label));
        connectionWidget.setConstraint(label, LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_RIGHT, 10);
        connectionWidget.addChild(label);

        return connectionWidget;
    }

/*
    protected Widget attachEdgeWidget(NavigationCaseEdge edge, boolean directEdge){
    ConnectionWidget connectionWidget = (ConnectionWidget)attachEdgeWidget(edge);
    if( directEdge ){
    connectionWidget.setRouter(router);
    }
    }
     * */

    public void renameEdgeWidget(NavigationCaseEdge edge, String newName, String oldName) {
        VMDConnectionWidget edgeWidget = (VMDConnectionWidget) findWidget(edge);
        List<Widget> widgets = edgeWidget.getChildren();
        for (Widget widget : widgets) {
            if (widget instanceof LabelWidget && ((LabelWidget) widget).getLabel().equals(oldName)) {
                ((LabelWidget) widget).setLabel(newName);
                return;
            }
        }
    }

    /**
     * Attaches an anchor of a source pin an edge.
     * The anchor is a ProxyAnchor that switches between the anchor attached to the pin widget directly and
     * the anchor attached to the pin node widget based on the minimize-state of the node.
     * @param edge the edge
     * @param oldSourcePin the old source pin
     * @param sourcePin the new source pin
     */
    protected void attachEdgeSourceAnchor(NavigationCaseEdge edge, Pin oldSourcePin, Pin sourcePin) {
        ConnectionWidget connectionWidget = (ConnectionWidget) findWidget(edge);
        Anchor anchor = getPinAnchor(sourcePin);
        connectionWidget.setSourceAnchor(anchor);
    }

    /**
     * Attaches an anchor of a target pin an edge.
     * The anchor is a ProxyAnchor that switches between the anchor attached to the pin widget directly and
     * the anchor attached to the pin node widget based on the minimize-state of the node.
     * @param edge the edge
     * @param oldTargetPin the old target pin
     * @param targetPin the new target pin
     */
    protected void attachEdgeTargetAnchor(NavigationCaseEdge edge, Pin oldTargetPin, Pin targetPin) {
        ((ConnectionWidget) findWidget(edge)).setTargetAnchor(getPinAnchor(targetPin));
    }

    /*
     * Returns the Anchor for a given pin
     * @param pin The Pin
     * @return Anchor the anchor location
     */
    private Anchor getPinAnchor(Pin pin) {
        if (pin == null) {
            return null;
        }
        VMDNodeWidget nodeWidget = (VMDNodeWidget) findWidget(getPinNode(pin));
        Widget pinMainWidget = findWidget(pin);
        Anchor anchor;
        if (pinMainWidget != null) {
            anchor = AnchorFactory.createDirectionalAnchor(pinMainWidget, AnchorFactory.DirectionalAnchorKind.HORIZONTAL, 8);
            anchor = nodeWidget.createAnchorPin(anchor);
        } else {
            anchor = nodeWidget.getNodeAnchor();
        }
        return anchor;
    }

    public LayerWidget getConnectionLayer() {
        return connectionLayer;
    }

    private final class PageFlowSelectProvider implements SelectProvider {

        public boolean isAimingAllowed(Widget widget, Point localLocation, boolean invertSelection) {
            return false;
        }

        public boolean isSelectionAllowed(Widget widget, Point localLocation, boolean invertSelection) {
            Object object = findObject(widget);
            return object != null && (invertSelection || !getSelectedObjects().contains(object));
        }

        public void select(Widget widget, Point localLocation, boolean invertSelection) {
            Object object = findObject(widget);
            if (object != null) {
                setFocusedObject(object);
                if (getSelectedObjects().contains(object)) {
                    return;
                }
                userSelectionSuggested(Collections.singleton(object), invertSelection);
            } else {
                userSelectionSuggested(Collections.emptySet(), invertSelection);
            }
        }
    }


    public final class PageNodeEditAction implements EditProvider {

        public void edit(Widget widget) {
            PageFlowScene scene = (PageFlowScene) widget.getScene();
            PageFlowSceneElement element = (PageFlowSceneElement) scene.findObject(widget);
            MapActionUtility.openPageFlowSceneElement(element);
        }
    }

    public final class CaseNodeTextFieldInplaceEditor implements TextFieldInplaceEditor {

        public boolean isEnabled(Widget widget) {
            NavigationCaseEdge caseNode = (NavigationCaseEdge) findObject(widget.getParentWidget());
            return caseNode.isModifiable();
        }

        public String getText(Widget widget) {
            NavigationCaseEdge caseNode = (NavigationCaseEdge) findObject(widget.getParentWidget());
            return ((LabelWidget) widget).getLabel();
        }

        public void setText(Widget widget, String newName) {
            if (newName.equals("")) {
                return;
            }

            NavigationCaseEdge caseNode = (NavigationCaseEdge) findObject(widget.getParentWidget());
            String oldName = caseNode.getName();

            if (caseNode.canRename()) {
                //Pin pin = getEdgeSource(caseNode);
                //caseNode.setName(pin, newName);
                caseNode.setName(newName);
            }

            ((LabelWidget) widget).setLabel(newName);
        }
    }


    public final class PageNodeTextFieldInplaceEditor implements TextFieldInplaceEditor {

        private final VMDNodeWidget nodeWidget;

        public PageNodeTextFieldInplaceEditor(VMDNodeWidget nodeWidget) {
            this.nodeWidget = nodeWidget;
        }

        public boolean isEnabled(Widget widget) {
            return true;
        }

        public String getText(Widget widget) {
            Page pageNode = (Page) findObject(nodeWidget);
            return pageNode.getName();
        }

        public void setText(Widget widget, String text) {

            Page pageNode = (Page) findObject(nodeWidget);
            if (pageNode.canRename() && !text.equals(pageNode.getName())) {

                //Explicitly declared oldName and newName for ease of reading.
                String oldName = pageNode.getDisplayName();
                String newName;

                pageNode.setName(text);
                newName = pageNode.getDisplayName();

                //                if( oldName != newName ) {
                //                    renamePin(pageNode, oldName + "pin", newName + "pin");
                //                }
                if (widget instanceof LabelWidget) {
                    ((LabelWidget) widget).setLabel(newName);
                } else if (widget instanceof VMDNodeWidget) {
                    ((VMDNodeWidget) widget).getNodeNameWidget().setLabel(newName);
                }
                validate();
            }
        }
    }

    private final static UnsupportedOperationException uoe = new  UnsupportedOperationException("Not supported yet.");


    private class MyObjectSceneListener implements ObjectSceneListener {

        List<NavigationCaseEdge> directlyRoutedLinks = new ArrayList<NavigationCaseEdge>();

        public void objectAdded(ObjectSceneEvent event, Object addedObject) {
            throw uoe;
        }

        public void objectRemoved(ObjectSceneEvent event, Object removedObject) {
            throw uoe;
        }

        public void objectStateChanged(ObjectSceneEvent event, Object changedObject, ObjectState previousState, ObjectState newState) {
            throw uoe;
        }

        public void selectionChanged(ObjectSceneEvent event, Set<Object> previousSelection, Set<Object> newSelection) {

            Set<Node> selected = new HashSet<Node>();
            for (Object obj : newSelection) {
                if (obj instanceof PageFlowSceneElement) {
                    PageFlowSceneElement element = (PageFlowSceneElement) obj;

                    selected.add(element.getNode());
                }
            }

            if (selected.size() == 0) {
                tc.setDefaultActivatedNode();
            } else {
                tc.setActivatedNodes(selected.toArray(new Node[selected.size()]));
            }
        }

        public void highlightingChanged(ObjectSceneEvent event, Set<Object> previousHighlighting, Set<Object> newHighlighting) {
            throw uoe;
        }

        public void hoverChanged(ObjectSceneEvent event, Object previousHoveredObject, Object newHoveredObject) {
            throw uoe;
        }

        public void focusChanged(ObjectSceneEvent event, Object previousFocusedObject, Object newFocusedObject) {
            throw uoe;
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

    public PopupMenuProvider getPopupMenuProvider() {
        return popupProvider;
    }
}
