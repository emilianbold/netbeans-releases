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
package org.netbeans.modules.vmd.api.flow.visual;

import org.netbeans.api.visual.action.*;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.graph.GraphPinScene;
import org.netbeans.api.visual.graph.layout.GridGraphLayout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.*;
import org.netbeans.modules.vmd.api.flow.FlowPinOrderPresenter;
import org.netbeans.modules.vmd.api.flow.FlowPresenter;
import org.netbeans.modules.vmd.api.flow.FlowScenePresenter;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionsSupport;
import org.netbeans.modules.vmd.api.palette.PaletteSupport;
import org.netbeans.modules.vmd.flow.FlowViewController;
import org.openide.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * @author David Kaspar
 */
// TODO - should badges be a part of selection?
// TODO - maybe updateBadges method should not be called from inside the attach*Widget, but it should be called from each particular presenter instead
public final class FlowScene extends GraphPinScene<FlowNodeDescriptor, FlowEdgeDescriptor, FlowPinDescriptor> {

    private static Paint PAINT_BACKGROUND;

    static {
        Image sourceImage = Utilities.loadImage ("org/netbeans/modules/vmd/flow/resources/paper_grid.png"); // NOI18N
        int width = sourceImage.getWidth (null);
        int height = sourceImage.getHeight (null);
        BufferedImage image = new BufferedImage (width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics ();
        graphics.drawImage (sourceImage, 0, 0, null);
        graphics.dispose ();
        PAINT_BACKGROUND = new TexturePaint (image, new Rectangle (0, 0, width, height));
//        PAINT_BACKGROUND = Color.WHITE;
    }

    private DesignDocument document;

    private LayerWidget backgroundLayer;
    private LayerWidget mainLayer;
    private LayerWidget connectionLayer;
    private LayerWidget interractionLayer;

    private WidgetAction moveAction;
    private WidgetAction acceptAction;
    private WidgetAction connectAction;
    private WidgetAction reconnectAction;
    private WidgetAction moveControlPointAction;
    private WidgetAction renameAction;
    private WidgetAction popupMenuAction;
    private WidgetAction editAction;
    private WidgetAction sceneSelectAction;
    private WidgetAction sceneKeyAction;

    private Router edgeRouter;

    private HashMap<CacheNodeDescriptor, Point> preferredNodeLocationCache = new HashMap<CacheNodeDescriptor, Point> ();
    private HashMap<Long,Point> preferredNodeLocationMap = new HashMap<Long, Point> ();
    private Long eventID;

    private HashMap<FlowNodeDescriptor, FlowPresenter.FlowUIResolver> nodeUIregistry = new HashMap<FlowNodeDescriptor, FlowPresenter.FlowUIResolver> ();
    private HashMap<FlowPinDescriptor, FlowPresenter.FlowUIResolver> pinUIregistry = new HashMap<FlowPinDescriptor, FlowPresenter.FlowUIResolver> ();
    private HashMap<FlowEdgeDescriptor, FlowPresenter.FlowUIResolver> edgeUIregistry = new HashMap<FlowEdgeDescriptor, FlowPresenter.FlowUIResolver> ();
    private HashMap<FlowBadgeDescriptor, FlowPresenter.FlowUIResolver> pinBadgeUIregistry = new HashMap<FlowBadgeDescriptor, FlowPresenter.FlowUIResolver> ();
    private HashMap<FlowDescriptor, ArrayList<FlowBadgeDescriptor>> badges = new HashMap<FlowDescriptor, ArrayList<FlowBadgeDescriptor>> ();
    private HashSet<FlowNodeDescriptor> nodesForOrdering = new HashSet<FlowNodeDescriptor> ();

    private GridGraphLayout<FlowNodeDescriptor,FlowEdgeDescriptor> graphLayout;
    private SceneLayout sceneLayout;

    public FlowScene (DesignDocument document) {
        this.document = document;

        setOpaque (true);
        setBackground (PAINT_BACKGROUND);

        setKeyEventProcessingType (EventProcessingType.FOCUSED_WIDGET_AND_ITS_PARENTS);

        addChild (backgroundLayer = new LayerWidget (this));
        addChild (mainLayer = new LayerWidget (this));
        addChild (connectionLayer = new LayerWidget (this));
        addChild (interractionLayer = new LayerWidget (this));

        moveAction = ActionFactory.createMoveAction (null, new FlowMoveProvider ());
        acceptAction = ActionFactory.createAcceptAction (new FlowAcceptProvider ());
        FlowConnectDecoratorProvider flowConnectDecoratorProvider = new FlowConnectDecoratorProvider ();
        connectAction = ActionFactory.createConnectAction (flowConnectDecoratorProvider, interractionLayer, flowConnectDecoratorProvider);
        FlowReconnectDecoratorProvider flowReconnectDecoratorProvider = new FlowReconnectDecoratorProvider ();
        reconnectAction = ActionFactory.createReconnectAction (flowReconnectDecoratorProvider, flowReconnectDecoratorProvider);
        moveControlPointAction = ActionFactory.createMoveControlPointAction (ActionFactory.createOrthogonalMoveControlPointProvider (), ConnectionWidget.RoutingPolicy.DISABLE_ROUTING_UNTIL_END_POINT_IS_MOVED);
        renameAction = ActionFactory.createInplaceEditorAction (new FlowRenameEditor ());
        editAction = ActionFactory.createEditAction (new FlowEditProvider ());
        popupMenuAction = ActionFactory.createPopupMenuAction (new FlowPopupMenuProvider ());
        sceneSelectAction = new FlowSceneSelectAction ();
        sceneKeyAction = new FlowSceneKeyAction (); // HINT - FlowDescriptor.KeyActionBehaviour is used for FlowScene/rootComponent only

        edgeRouter = RouterFactory.createOrthogonalSearchRouter (mainLayer, connectionLayer);

        getActions ().addAction (ActionFactory.createZoomAction ());
        getActions ().addAction (ActionFactory.createPanAction ());
        getActions ().addAction (sceneSelectAction);
        getActions ().addAction (createSelectAction ());
        getActions ().addAction (createAcceptAction ());
        getActions ().addAction (popupMenuAction);
        getActions ().addAction (ActionFactory.createRectangularSelectAction (this, backgroundLayer));
        getActions ().addAction (ActionFactory.createCycleObjectSceneFocusAction ());
        getActions ().addAction (sceneKeyAction);

        graphLayout = new GridGraphLayout<FlowNodeDescriptor, FlowEdgeDescriptor> ().setChecker (true);
        sceneLayout = LayoutFactory.createSceneGraphLayout (this, graphLayout);

        addObjectSceneListener (new FlowObjectSceneListener (), ObjectSceneEventType.OBJECT_SELECTION_CHANGED);
    }

    public DesignDocument getDocument () {
        return document;
    }

    // TODO - should not be in API
    public void setCurrentEventIDForPreferredNodeLocationProcessing (Long eventID) {
        this.eventID = eventID;
        if (eventID != null) {
            Iterator<Map.Entry<Long, Point>> iterator = preferredNodeLocationMap.entrySet ().iterator ();
            while (iterator.hasNext ()) {
                Map.Entry<Long, Point> entry = iterator.next ();
                if (entry.getKey () < eventID)
                    iterator.remove ();
            }
        }
    }

    protected Widget attachNodeWidget (FlowNodeDescriptor node) {
        Widget widget = getDecorator (node).createWidget (node, this);
        Point preferredLocation = preferredNodeLocationCache.get (new CacheNodeDescriptor (node));
        if (preferredLocation == null)
            preferredLocation = eventID != null ? preferredNodeLocationMap.get (eventID) : null;
        if (preferredLocation == null)
            preferredLocation = new Point (50, 50);
        widget.setPreferredLocation (preferredLocation);
        mainLayer.addChild (widget);
        return widget;
    }

    protected void detachNodeWidget (FlowNodeDescriptor descriptor, Widget widget) {
        if (widget != null)
            preferredNodeLocationCache.put (new CacheNodeDescriptor (descriptor), widget.getPreferredLocation ());
        super.detachNodeWidget (descriptor, widget);
    }

    protected Widget attachPinWidget (FlowNodeDescriptor node, FlowPinDescriptor pin) {
        FlowPinDescriptor.PinDecorator decorator = getDecorator (pin);
        Widget widget = decorator.createWidget (pin, this);
        if (widget != null)
            getDecorator ((FlowNodeDescriptor) findStoredObject (node)).attachPinWidget (node, this, widget);
        return widget;
    }

    protected Widget attachEdgeWidget (FlowEdgeDescriptor edge) {
        Widget widget = getDecorator (edge).create (edge, this);
        assert widget instanceof ConnectionWidget;
        connectionLayer.addChild (widget);
        return widget;
    }

    protected void attachEdgeSourceAnchor (FlowEdgeDescriptor edge, FlowPinDescriptor oldSourcePin, FlowPinDescriptor sourcePin) {
        Anchor anchor = null;
        if (sourcePin != null)
            anchor = getDecorator (sourcePin).createAnchor (sourcePin, this);
        getDecorator (edge).setSourceAnchor (edge, this, anchor);
    }

    protected void attachEdgeTargetAnchor (FlowEdgeDescriptor edge, FlowPinDescriptor oldTargetPin, FlowPinDescriptor targetPin) {
        Anchor anchor = null;
        if (targetPin != null)
            anchor = getDecorator (targetPin).createAnchor (targetPin, this);
        getDecorator (edge).setTargetAnchor (edge, this, anchor);
    }

    public Router createEdgeRouter () {
        return edgeRouter;
    }

    public WidgetAction createMoveAction () {
        return moveAction;
    }

    public WidgetAction createAcceptAction () {
        return acceptAction;
    }

    public WidgetAction createMoveControlPointAction () {
        return moveControlPointAction;
    }

    public WidgetAction createRenameAction () {
        return renameAction;
    }

    public WidgetAction createConnectAction () {
        return connectAction;
    }

    public WidgetAction createReconnectAction () {
        return reconnectAction;
    }

    public WidgetAction createPopupMenuAction () {
        return popupMenuAction;
    }

    public WidgetAction createEditAction () {
        return editAction;
    }

    public void userSelectionSuggested (final Set<? extends Object> suggestedSelectedObjects, boolean invertSelection) {
        super.userSelectionSuggested (suggestedSelectedObjects, invertSelection);

        document.getTransactionManager ().writeAccess (new Runnable() {
            public void run () {
                ArrayList<DesignComponent> selection = new ArrayList<DesignComponent> ();
                for (Object object : getSelectedObjects ()) {
                    DesignComponent component = object instanceof FlowDescriptor ? ((FlowDescriptor) object).getRepresentedComponent () : null;
                    if (component != null)
                        selection.add (component);
                }
                if (selection.isEmpty ()) {
                    DesignComponent rootComponent = document.getRootComponent ();
                    if (rootComponent != null)
                        selection.add (rootComponent);
                }
                document.setSelectedComponents (FlowViewController.FLOW_ID, selection);
            }
        });
    }

    public void addNodeCommonActions (Widget widget) {
        WidgetAction.Chain actions = widget.getActions ();
        actions.addAction (createObjectHoverAction ());
        actions.addAction (createSelectAction ());
        actions.addAction (createMoveAction ());
        actions.addAction (createAcceptAction ());
        actions.addAction (createPopupMenuAction ());
        actions.addAction (createEditAction ());
    }

    public void addEdgeCommonActions (ConnectionWidget widget) {
        WidgetAction.Chain actions = widget.getActions ();
        actions.addAction (createObjectHoverAction ());
        actions.addAction (createSelectAction ());
        actions.addAction (createReconnectAction ());
        actions.addAction (createPopupMenuAction ());
        actions.addAction (createEditAction ());
    }

    public void addPinCommonActions (Widget widget) {
        WidgetAction.Chain actions = widget.getActions ();
        actions.addAction (createObjectHoverAction ());
        widget.getActions ().addAction (createSelectAction ());
        widget.getActions ().addAction (createAcceptAction ());
        widget.getActions ().addAction (createConnectAction ());
        actions.addAction (createPopupMenuAction ());
        actions.addAction (createEditAction ());
    }

    public void addBadge (FlowDescriptor descriptor, FlowBadgeDescriptor badge) {
        if (badge == null)
            return;
        ArrayList<FlowBadgeDescriptor> list = badges.get (descriptor);
        if (list == null)
            badges.put (descriptor, list = new ArrayList<FlowBadgeDescriptor> ());
        list.add (badge);
    }

    public void removeBadge (FlowDescriptor descriptor, FlowBadgeDescriptor badge) {
        ArrayList<FlowBadgeDescriptor> list = badges.get (descriptor);
        if (list == null)
            return;
        list.remove (badge);
        if (list.isEmpty ())
            badges.remove (descriptor);
    }

    public void updateBadges (FlowDescriptor descriptor) {
        FlowDescriptor.BadgeDecorator decorator = getAbstractDecorator (descriptor, FlowDescriptor.BadgeDecorator.class);
        if (decorator == null)
            return;
        ArrayList<FlowBadgeDescriptor> list = badges.get (descriptor);
        if (list != null) {
            Collections.sort (list, new Comparator<FlowBadgeDescriptor> () {
                public int compare (FlowBadgeDescriptor o1, FlowBadgeDescriptor o2) {
                    return getDecorator (o1).getOrder (o1) - getDecorator (o2).getOrder (o2);
                }
            });
            decorator.updateBadges (descriptor, this, list);
        } else {
            decorator.updateBadges (descriptor, this, Collections.<FlowBadgeDescriptor>emptyList ());
        }
    }

    public void registerUI (FlowNodeDescriptor descriptor, FlowPresenter.FlowUIResolver resolver) {
        assert resolver != null;
        nodeUIregistry.put (descriptor, resolver);
    }

    public void unregisterUI (FlowNodeDescriptor descriptor, FlowPresenter.FlowUIResolver resolver) {
        FlowPresenter.FlowUIResolver resolver2 = nodeUIregistry.remove (descriptor);
        assert resolver == resolver2;
    }

    public FlowNodeDescriptor.NodeDecorator getDecorator (FlowNodeDescriptor node) {
        return (FlowNodeDescriptor.NodeDecorator) nodeUIregistry.get (node).getDecorator ();
    }

    public FlowNodeDescriptor.NodeBehaviour getBehaviour (FlowNodeDescriptor node) {
        return (FlowNodeDescriptor.NodeBehaviour) nodeUIregistry.get (node).getBehaviour ();
    }

    public void registerUI (FlowPinDescriptor descriptor, FlowPresenter.FlowUIResolver resolver) {
        assert resolver != null;
        pinUIregistry.put (descriptor, resolver);
    }

    public void unregisterUI (FlowPinDescriptor descriptor, FlowPresenter.FlowUIResolver resolver) {
        FlowPresenter.FlowUIResolver resolver2 = pinUIregistry.remove (descriptor);
        assert resolver == resolver2;
    }

    public FlowPinDescriptor.PinDecorator getDecorator (FlowPinDescriptor pin) {
        return (FlowPinDescriptor.PinDecorator) pinUIregistry.get (pin).getDecorator ();
    }

    public FlowPinDescriptor.PinBehaviour getBehaviour (FlowPinDescriptor pin) {
        return (FlowPinDescriptor.PinBehaviour) pinUIregistry.get (pin).getBehaviour ();
    }

    public void registerUI (FlowEdgeDescriptor descriptor, FlowPresenter.FlowUIResolver resolver) {
        assert resolver != null;
        edgeUIregistry.put (descriptor, resolver);
    }

    public void unregisterUI (FlowEdgeDescriptor descriptor, FlowPresenter.FlowUIResolver resolver) {
        FlowPresenter.FlowUIResolver resolver2 = edgeUIregistry.remove (descriptor);
        assert resolver == resolver2;
    }

    public FlowEdgeDescriptor.EdgeDecorator getDecorator (FlowEdgeDescriptor edge) {
        return (FlowEdgeDescriptor.EdgeDecorator) edgeUIregistry.get (edge).getDecorator ();
    }

    public FlowEdgeDescriptor.EdgeBehaviour getBehaviour (FlowEdgeDescriptor edge) {
        return (FlowEdgeDescriptor.EdgeBehaviour) edgeUIregistry.get (edge).getBehaviour ();
    }

    public void registerUI (FlowBadgeDescriptor descriptor, FlowPresenter.FlowUIResolver resolver) {
        assert resolver != null;
        pinBadgeUIregistry.put (descriptor, resolver);
    }

    public void unregisterUI (FlowBadgeDescriptor descriptor, FlowPresenter.FlowUIResolver resolver) {
        FlowPresenter.FlowUIResolver resolver2 = pinBadgeUIregistry.remove (descriptor);
        assert resolver == resolver2;
    }

    public FlowBadgeDescriptor.BadgeDecorator getDecorator (FlowBadgeDescriptor pinBadge) {
        return (FlowBadgeDescriptor.BadgeDecorator) pinBadgeUIregistry.get (pinBadge).getDecorator ();
    }

    public FlowBadgeDescriptor.BadgeBehaviour getBehaviour (FlowBadgeDescriptor pinBadge) {
        return (FlowBadgeDescriptor.BadgeBehaviour) pinBadgeUIregistry.get (pinBadge).getBehaviour ();
    }

    private <T extends FlowDescriptor.Decorator> T getAbstractDecorator (FlowDescriptor descriptor, Class<T> clazz) {
        if (descriptor == null)
            return null;
        FlowPresenter.FlowUIResolver presenter = nodeUIregistry.get (descriptor);
        if (presenter == null) {
            presenter = pinUIregistry.get (descriptor);
            if (presenter == null) {
                presenter = edgeUIregistry.get (descriptor);
                if (presenter == null) {
                    presenter = pinBadgeUIregistry.get (descriptor);
                    if (presenter == null)
                        return null;
                }
            }
        }
        FlowDescriptor.Decorator decorator = presenter.getDecorator ();
        return clazz.isInstance (decorator) ? (T) decorator : null;
    }

    private <T extends FlowDescriptor.Behaviour> T getAbstractBehaviour (FlowDescriptor descriptor, Class<T> clazz) {
        if (descriptor == null)
            return null;
        FlowPresenter.FlowUIResolver presenter = nodeUIregistry.get (descriptor);
        if (presenter == null) {
            presenter = pinUIregistry.get (descriptor);
            if (presenter == null) {
                presenter = edgeUIregistry.get (descriptor);
                if (presenter == null) {
                    presenter = pinBadgeUIregistry.get (descriptor);
                    if (presenter == null)
                        return null;
                }
            }
        }
        FlowDescriptor.Behaviour behaviour = presenter.getBehaviour ();
        return clazz.isInstance (behaviour) ? (T) behaviour : null;
    }

    public void scheduleNodeDescriptorForOrdering (FlowNodeDescriptor node) {
        nodesForOrdering.add (node);
    }

    public void resolveOrderInNodeDescriptors () {
        for (FlowNodeDescriptor node : nodesForOrdering) {
            if (! isNode (node))
                continue;
            FlowNodeDescriptor.NodeDecorator decorator = getDecorator (node);

            DesignComponent orderComponent = decorator.getComponentWithPinOrderPresenters ();
            if (orderComponent == null) {
                decorator.orderPins (node, this, Collections.<String, List<FlowPinDescriptor>>emptyMap ());
                continue;
            }
            Collection<? extends FlowPinOrderPresenter> presenters = orderComponent.getPresenters (FlowPinOrderPresenter.class);

            if (presenters.isEmpty ()) {
                decorator.orderPins (node, this, Collections.<String, List<FlowPinDescriptor>>emptyMap ());
                continue;
            }

            HashMap<String, ArrayList<FlowPinDescriptor>> categories = new HashMap<String, ArrayList<FlowPinDescriptor>> ();

            for (FlowPinDescriptor pin : getNodePins (node)) {
                String category = getDecorator (pin).getOrderCategory (pin);
                ArrayList<FlowPinDescriptor> list = categories.get (category);
                if (list == null) {
                    list = new ArrayList<FlowPinDescriptor> ();
                    categories.put (category, list);
                }
                list.add (pin);
            }

            HashMap<String, List<FlowPinDescriptor>> order = new HashMap<String, List<FlowPinDescriptor>> ();
            for (FlowPinOrderPresenter presenter : presenters) {
                String category = presenter.getCategoryID ();
                ArrayList<FlowPinDescriptor> descriptors = categories.get (category);
                if (descriptors != null)
                    order.put (presenter.getCategoryDisplayName (), presenter.sortCategory (descriptors));
            }

            decorator.orderPins (node, this, order);
        }
        nodesForOrdering.clear ();
    }

    /**
     * Invokes layout of the scene.
     */
    public void layoutScene () {
        sceneLayout.invokeLayout ();
    }

    private Anchor createAnchorForDescriptor (FlowDescriptor descriptor) {
        Anchor anchor = null;
        if (descriptor != null) {
            if (descriptor instanceof FlowNodeDescriptor) {
                FlowNodeDescriptor node = (FlowNodeDescriptor) descriptor;
                FlowNodeDescriptor.NodeDecorator decorator = getDecorator (node);
                if (decorator != null)
                    anchor = decorator.createAnchor (node, this);
            } else if (descriptor instanceof FlowPinDescriptor) {
                FlowPinDescriptor pin = (FlowPinDescriptor) descriptor;
                FlowPinDescriptor.PinDecorator decorator = getDecorator (pin);
                if (decorator != null)
                    anchor = decorator.createAnchor (pin, this);
            }
        }
        return anchor;
    }

    private class FlowObjectSceneListener implements ObjectSceneListener {

        public void objectAdded (ObjectSceneEvent event, Object addedObject) {
        }

        public void objectRemoved (ObjectSceneEvent event, Object removedObject) {
        }

        public void objectStateChanged (ObjectSceneEvent event, Object changedObject, ObjectState previousState, ObjectState newState) {
        }

        public void selectionChanged (ObjectSceneEvent event, Set<Object> previousSelection, Set<Object> newSelection) {
            HashSet<Object> set = new HashSet<Object> ();
            for (Object object : newSelection) {
                if (isNode (object)) {
                    for (FlowPinDescriptor pin : getNodePins ((FlowNodeDescriptor) object))
                        set.addAll (findPinEdges (pin, true, true));
                } else if (isPin (object))
                    set.addAll (findPinEdges ((FlowPinDescriptor) object, true, true));
            }
            setHighlightedObjects (set);
        }

        public void highlightingChanged (ObjectSceneEvent event, Set<Object> previousHighlighting, Set<Object> newHighlighting) {
        }

        public void hoverChanged (ObjectSceneEvent event, Object previousHoveredObject, Object newHoveredObject) {
        }

        public void focusChanged (ObjectSceneEvent event, Object previousFocusedObject, Object newFocusedObject) {
        }

    }

    private class FlowAcceptProvider implements AcceptProvider {

        public ConnectorState isAcceptable (final Widget widget, Point point, final Transferable transferable) {
            if (widget == null)
                return ConnectorState.REJECT_AND_STOP;
            final ConnectorState[] ret  = new ConnectorState[] { ConnectorState.REJECT };
            document.getTransactionManager ().readAccess (new Runnable() {
                public void run () {
                    if (widget == FlowScene.this) {
                        DesignComponent rootComponent = document.getRootComponent ();
                        FlowDescriptor rootDescriptor = new FlowDescriptor (rootComponent, "accept") {}; // NOI18N
                        Collection<? extends FlowScenePresenter> presenters = rootComponent.getPresenters (FlowScenePresenter.class);
                        for (FlowScenePresenter presenter : presenters) {
                            FlowDescriptor.Behaviour behavior = presenter.getBehavior ();
                            if (behavior instanceof FlowDescriptor.AcceptActionBehaviour) {
                                FlowDescriptor.AcceptActionBehaviour acceptBehaviour = (FlowDescriptor.AcceptActionBehaviour) behavior;
                                if (acceptBehaviour.isAcceptable (rootDescriptor, transferable)) {
                                    ret[0] = ConnectorState.ACCEPT;
                                    break;
                                }
                            }
                        }
                    } else {
                        FlowDescriptor descriptor = (FlowDescriptor) findObject (widget);
                        FlowDescriptor.AcceptActionBehaviour behaviour = getAbstractBehaviour (descriptor, FlowDescriptor.AcceptActionBehaviour.class);
                        if (behaviour != null)
                            if (behaviour.isAcceptable (descriptor, transferable))
                                ret[0] = ConnectorState.ACCEPT;
                    }
                }
            });
            return ret[0];
        }

        public void accept (final Widget widget, Point point, final Transferable transferable) {
            if (widget == null)
                return;
            long eventID = document.getTransactionManager ().writeAccess (new Runnable() {
                public void run () {
                    if (widget == FlowScene.this) {
                        DesignComponent rootComponent = document.getRootComponent ();
                        FlowDescriptor rootDescriptor = new FlowDescriptor(rootComponent, "accept") {}; // NOI18N
                        Collection<? extends FlowScenePresenter> presenters = rootComponent.getPresenters (FlowScenePresenter.class);
                        for (FlowScenePresenter presenter : presenters) {
                            FlowDescriptor.Behaviour behavior = presenter.getBehavior ();
                            if (behavior instanceof FlowDescriptor.AcceptActionBehaviour) {
                                FlowDescriptor.AcceptActionBehaviour acceptBehaviour = (FlowDescriptor.AcceptActionBehaviour) behavior;
                                if (acceptBehaviour.isAcceptable (rootDescriptor, transferable))
                                    acceptBehaviour.accept (rootDescriptor, transferable);
                            }
                        }
                    } else {
                        FlowDescriptor descriptor = (FlowDescriptor) findObject (widget);
                        FlowDescriptor.AcceptActionBehaviour behaviour = getAbstractBehaviour (descriptor, FlowDescriptor.AcceptActionBehaviour.class);
                        if (behaviour != null)
                            behaviour.accept (descriptor, transferable);
                    }
                }
            });
            preferredNodeLocationMap.put (eventID, widget.convertLocalToScene (point));
            PaletteSupport.getPaletteController (document).clearSelection ();
        }

    }

    private class FlowSceneSelectAction extends WidgetAction.Adapter {

        public State mousePressed (final Widget widget, final WidgetMouseEvent event) {
            final boolean[] ret = new boolean[] { false };
            long eventID = document.getTransactionManager ().writeAccess (new Runnable() {
                public void run () {
                    if (widget == FlowScene.this) {
                        DesignComponent rootComponent = document.getRootComponent ();
                        if (rootComponent == null)
                            return;
                        FlowDescriptor rootDescriptor = new FlowDescriptor(rootComponent, "select") {}; // NOI18N
                        Collection<? extends FlowScenePresenter> presenters = rootComponent.getPresenters (FlowScenePresenter.class);
                        for (FlowScenePresenter presenter : presenters) {
                            FlowDescriptor.Behaviour behavior = presenter.getBehavior ();
                            if (behavior instanceof FlowDescriptor.SelectActionBehaviour) {
                                FlowDescriptor.SelectActionBehaviour selectBehaviour = (FlowDescriptor.SelectActionBehaviour) behavior;
                                if (selectBehaviour.select (rootDescriptor, event.getModifiers ())) {
                                    ret[0] = true;
                                    return;
                                }
                            }
                        }
                    }
                }
            });
            preferredNodeLocationMap.put (eventID, widget.convertLocalToScene (event.getPoint ()));
            
            return ret[0] ? State.CONSUMED : State.REJECTED;
        }

    }

    private class FlowConnectDecoratorProvider implements ConnectDecorator, ConnectProvider {

        private FlowPinDescriptor source = null;
        private FlowDescriptor target = null;

        public ConnectionWidget createConnectionWidget (Scene scene) {
            return ActionFactory.createDefaultConnectDecorator ().createConnectionWidget (scene);
        }

        public Anchor createSourceAnchor (Widget sourceWidget) {
            return createAnchorForDescriptor (source);
        }

        public Anchor createTargetAnchor (Widget targetWidget) {
            return createAnchorForDescriptor (target);
        }

        public Anchor createFloatAnchor (Point point) {
            return ActionFactory.createDefaultConnectDecorator ().createFloatAnchor (point);
        }

        public boolean isSourceWidget (final Widget sourceWidget) {
            final boolean[] ret = new boolean[]{false};
            document.getTransactionManager ().readAccess (new Runnable() {
                public void run () {
                    Object object = findObject (sourceWidget);
                    if (isPin (object)) {
                        source = (FlowPinDescriptor) object;
                        ret[0] = getBehaviour (source).isConnectionSource (source);
                    } else
                        ret[0] = false;
                }
            });
            return ret[0];
        }

        public ConnectorState isTargetWidget (Widget sourceWidget, final Widget targetWidget) {
            final ConnectorState[] ret = new ConnectorState[] { ConnectorState.REJECT };
            document.getTransactionManager ().readAccess (new Runnable() {
                public void run () {
                    target = (FlowDescriptor) findObject (targetWidget);
                    ret[0] = getBehaviour (source).isConnectionTarget (source, target) ? ConnectorState.ACCEPT : ConnectorState.REJECT;
                }
            });
            return ret[0];
        }

        public boolean hasCustomTargetWidgetResolver (Scene scene) {
            return false;
        }

        public Widget resolveTargetWidget (Scene scene, Point point) {
            return null;
        }

        public void createConnection (Widget sourceWidget, Widget targetWidget) {
            document.getTransactionManager ().writeAccess (new Runnable() {
                public void run () {
                    FlowPinDescriptor.PinBehaviour behaviour = getBehaviour (source);
                    if (behaviour.isConnectionTarget (source, target))
                        behaviour.createConnection (source, target);
                }
            });
        }

    }

    private class FlowReconnectDecoratorProvider implements ReconnectDecorator, ReconnectProvider {

        private FlowDescriptor replacement = null;

        public Anchor createReplacementWidgetAnchor (Widget replacementWidget) {
            return createAnchorForDescriptor (replacement);
        }

        public Anchor createFloatAnchor (Point point) {
            return ActionFactory.createDefaultReconnectDecorator ().createFloatAnchor (point);
        }

        public boolean isSourceReconnectable (final ConnectionWidget connectionWidget) {
            final boolean[] ret = new boolean[] { false };
            document.getTransactionManager ().readAccess (new Runnable() {
                public void run () {
                    Object object = findObject (connectionWidget);
                    if (! isEdge (object))
                        return;
                    FlowEdgeDescriptor edge = (FlowEdgeDescriptor) object;
                    ret[0] = getBehaviour (edge).isSourceReconnectable (edge);
                }
            });
            return ret[0];
        }

        public boolean isTargetReconnectable (final ConnectionWidget connectionWidget) {
            final boolean[] ret = new boolean[] { false };
            document.getTransactionManager ().readAccess (new Runnable() {
                public void run () {
                    Object object = findObject (connectionWidget);
                    if (! isEdge (object))
                        return;
                    FlowEdgeDescriptor edge = (FlowEdgeDescriptor) object;
                    ret[0] = getBehaviour (edge).isTargetReconnectable (edge);
                }
            });
            return ret[0];
        }

        public void reconnectingStarted (ConnectionWidget connectionWidget, boolean b) {
        }

        public void reconnectingFinished (ConnectionWidget connectionWidget, boolean b) {
        }

        public ConnectorState isReplacementWidget (final ConnectionWidget connectionWidget, final Widget replacementWidget, final boolean reconnectingSource) {
            final ConnectorState[] ret = new ConnectorState[1];
            document.getTransactionManager ().readAccess (new Runnable() {
                public void run () {
                    Object object = findObject (connectionWidget);
                    if (! isEdge (object))
                        return;
                    FlowEdgeDescriptor edge = (FlowEdgeDescriptor) object;
                    replacement = (FlowDescriptor) findObject (replacementWidget);
                    ret[0] = getBehaviour (edge).isReplacement (edge, replacement, reconnectingSource) ? ConnectorState.ACCEPT : ConnectorState.REJECT;
                }
            });
            return ret[0];
        }

        public boolean hasCustomReplacementWidgetResolver (Scene scene) {
            return false;
        }

        public Widget resolveReplacementWidget (Scene scene, Point point) {
            return null;
        }

        public void reconnect (final ConnectionWidget connectionWidget, final Widget replacementWidget, final boolean reconnectingSource) {
            document.getTransactionManager ().writeAccess (new Runnable() {
                public void run () {
                    Object object = findObject (connectionWidget);
                    if (! isEdge (object))
                        return;
                    FlowEdgeDescriptor edge = (FlowEdgeDescriptor) object;
                    replacement = (FlowDescriptor) findObject (replacementWidget);
                    FlowEdgeDescriptor.EdgeBehaviour behaviour = getBehaviour (edge);
                    if (behaviour.isReplacement (edge, replacement, reconnectingSource))
                        behaviour.setReplacement (edge, replacement, reconnectingSource);
                }
            });
        }

    }

    private class FlowPopupMenuProvider implements PopupMenuProvider {

        public JPopupMenu getPopupMenu (Widget widget, Point localLocation) {
            JComponent component = FlowScene.this.getView ();

            if (widget == FlowScene.this) {
                final DesignComponent[] ret = new DesignComponent[1];
                document.getTransactionManager ().readAccess (new Runnable() {
                    public void run () {
                        ret[0] = document.getRootComponent ();
                    }
                });

                if (! getSelectedObjects ().isEmpty ())
                    userSelectionSuggested (Collections.emptySet (), false);

                return Utilities.actionsToPopup (ActionsSupport.createActionsArray (ret[0]), component);
            }

            FlowDescriptor descriptor = (FlowDescriptor) findObject (widget);
            if (descriptor == null)
                return null;

            setFocusedObject (descriptor);
            if (! getSelectedObjects ().contains (descriptor))
                userSelectionSuggested (Collections.singleton (descriptor), false);

            return Utilities.actionsToPopup (ActionsSupport.createActionsArray (descriptor.getRepresentedComponent ()), component);
        }
    }

    private class FlowSceneKeyAction extends WidgetAction.Adapter {

        public State keyPressed (Widget widget, WidgetKeyEvent event) {
            // HINT - FlowDescriptor.KeyActionBehaviour is used for FlowScene/rootComponent only
            final FlowDescriptor.KeyActionBehaviour[] ret = new FlowDescriptor.KeyActionBehaviour[1];
            document.getTransactionManager ().readAccess (new Runnable() {
                public void run () {
                    DesignComponent root = document.getRootComponent ();
                    if (root != null) {
                        FlowScenePresenter presenter = root.getPresenter (FlowScenePresenter.class);
                        if (presenter != null) {
                            FlowDescriptor.Behaviour behavior = presenter.getBehavior ();
                            if (behavior instanceof FlowDescriptor.KeyActionBehaviour)
                                ret[0] = (FlowDescriptor.KeyActionBehaviour) behavior;
                        }
                    }
                }
            });
            return ret[0] != null  &&  ret[0].keyPressed (event)  ? State.CONSUMED : State.REJECTED;
        }
    }

    private class FlowRenameEditor implements TextFieldInplaceEditor {

        public boolean isEnabled (final Widget widget) {
            final FlowDescriptor descriptor = (FlowDescriptor) findObject (widget);
            if (descriptor == null)
                return false;
            final boolean[] ret = new boolean[] { false };
            document.getTransactionManager ().readAccess (new Runnable() {
                public void run () {
                    FlowDescriptor descriptor = (FlowDescriptor) findObject (widget);
                    FlowDescriptor.RenameActionBehaviour behaviour = getAbstractBehaviour (descriptor, FlowDescriptor.RenameActionBehaviour.class);
                    if (behaviour != null)
                        ret[0] = behaviour.isEditable (descriptor);
                }
            });
            return ret[0];
        }

        public String getText (final Widget widget) {
            final FlowDescriptor descriptor = (FlowDescriptor) findObject (widget);
            if (descriptor == null)
                return null;
            final String[] ret = new String[] { null };
            document.getTransactionManager ().readAccess (new Runnable() {
                public void run () {
                    FlowDescriptor descriptor = (FlowDescriptor) findObject (widget);
                    FlowDescriptor.RenameActionBehaviour behaviour = getAbstractBehaviour (descriptor, FlowDescriptor.RenameActionBehaviour.class);
                    if (behaviour != null)
                        ret[0] = behaviour.getText (descriptor);
                }
            });
            return ret[0];
        }

        public void setText (final Widget widget, final String text) {
            final FlowDescriptor descriptor = (FlowDescriptor) findObject (widget);
            if (descriptor == null)
                return;
            document.getTransactionManager ().writeAccess (new Runnable() {
                public void run () {
                    FlowDescriptor descriptor = (FlowDescriptor) findObject (widget);
                    FlowDescriptor.RenameActionBehaviour behaviour = getAbstractBehaviour (descriptor, FlowDescriptor.RenameActionBehaviour.class);
                    if (behaviour != null)
                        behaviour.setText (descriptor, text);
                }
            });
        }

    }

    private class FlowEditProvider implements EditProvider {

        public void edit (final Widget widget) {
            final FlowDescriptor descriptor = (FlowDescriptor) findObject (widget);
            if (descriptor == null)
                return;
            document.getTransactionManager ().writeAccess (new Runnable() {
                public void run () {
                    FlowDescriptor descriptor = (FlowDescriptor) findObject (widget);
                    FlowDescriptor.EditActionBehaviour behaviour = getAbstractBehaviour (descriptor, FlowDescriptor.EditActionBehaviour.class);
                    if (behaviour != null)
                        behaviour.edit (descriptor);
                }
            });
        }
    }

    private class FlowMoveProvider implements MoveProvider {

        private HashMap<Widget, Point> originals = new HashMap<Widget, Point> ();
        private Point original;

        public void movementStarted (Widget widget) {
            Object object = findObject (widget);
            if (isNode (object)) {
                for (Object o : getSelectedObjects ())
                    if (isNode (o)) {
                        Widget w = findWidget (o);
                        if (w != null)
                            originals.put (w, w.getPreferredLocation ());
                    }
            } else {
                originals.put (widget, widget.getPreferredLocation ());
            }
        }

        public void movementFinished (Widget widget) {
            originals.clear ();
            original = null;
        }

        public Point getOriginalLocation (Widget widget) {
            original = widget.getPreferredLocation ();
            return original;
        }

        public void setNewLocation (Widget widget, Point location) {
            int dx = location.x - original.x;
            int dy = location.y - original.y;
            for (Map.Entry<Widget, Point> entry : originals.entrySet ()) {
                Point point = entry.getValue ();
                entry.getKey ().setPreferredLocation (new Point (point.x + dx, point.y + dy));
            }
        }

    }

    private final static class CacheNodeDescriptor {

        private long componentid;
        private String descriptorid;

        public CacheNodeDescriptor (FlowNodeDescriptor node) {
            componentid = node.getRepresentedComponent ().getComponentID ();
            descriptorid = node.getDescriptorID ();
        }

        public boolean equals (Object o) {
            if (this == o)
                return true;
            if (o == null  ||  getClass () != o.getClass ())
                return false;
            final CacheNodeDescriptor desc = (CacheNodeDescriptor) o;
            return componentid == desc.componentid  &&  descriptorid.equals (desc.descriptorid);
        }

        public int hashCode () {
            int result;
            result = (int) (componentid ^ (componentid >>> 32));
            result = 29 * result + (descriptorid != null ? descriptorid.hashCode () : 0);
            return result;
        }

    }

}
