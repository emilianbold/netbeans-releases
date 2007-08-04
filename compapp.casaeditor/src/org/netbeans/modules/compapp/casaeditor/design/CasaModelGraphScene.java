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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.casaeditor.design;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.visual.action.RectangularSelectDecorator;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.modules.compapp.casaeditor.CasaDataEditorSupport;
import org.netbeans.modules.compapp.casaeditor.CasaDataObject;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.graph.*;
import org.netbeans.modules.compapp.casaeditor.graph.actions.CasaBadgeEditAction;
import org.netbeans.modules.compapp.casaeditor.graph.actions.CasaConnectAction;
import org.netbeans.modules.compapp.casaeditor.graph.actions.CasaPaletteAcceptProvider;
import org.netbeans.modules.compapp.casaeditor.graph.actions.CasaRemoveAction;
import org.netbeans.modules.compapp.casaeditor.graph.actions.CasaPopupMenuAction;
import org.netbeans.modules.compapp.casaeditor.graph.actions.CasaPopupMenuProvider;
import org.netbeans.modules.compapp.casaeditor.graph.actions.MouseWheelScrollAction;
import org.netbeans.modules.compapp.casaeditor.graph.actions.RegionResizeAction;
import org.netbeans.modules.compapp.casaeditor.graph.layout.CasaCollisionCollector;
import org.netbeans.modules.compapp.casaeditor.graph.layout.CasaRectangularSelectAction;
import org.netbeans.modules.compapp.casaeditor.graph.layout.CustomizableDevolveLayout;
import org.netbeans.modules.compapp.casaeditor.graph.layout.LayoutBindings;
import org.netbeans.modules.compapp.casaeditor.graph.layout.LayoutEngines;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConnection;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaConsumes;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaServiceEngineServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaProvides;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaPort;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaRegion;
import org.netbeans.modules.compapp.casaeditor.multiview.CasaGraphMultiViewElement;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNodeFactory;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * 
 * @author Josh Sandusky
 */
public class CasaModelGraphScene 
extends CasaGraphAbstractScene<CasaComponent, CasaComponent, CasaComponent>
implements PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(CasaModelGraphScene.class.getName());
    
    private LayerWidget mMainLayer       = new LayerWidget(this);
    private LayerWidget mConnectionLayer = new LayerWidget(this);
    private LayerWidget mGlassLayer      = new LayerWidget(this);
    
    private LayerWidget mDragLayer = new LayerWidget(this);

    private Router mOrthogonalRouter;
    private Router mDirectRouter = RouterFactory.createDirectRouter();
    private Router mCurrentRouter = mDirectRouter;

    private WidgetAction mPopupMenuAction = new CasaPopupMenuAction(new CasaPopupMenuProvider());
    private WidgetAction mMoveControlPointAction = ActionFactory.createOrthogonalMoveControlPointAction ();
    private WidgetAction mMoveActionBindingRegion;
    private WidgetAction mMoveActionEngineRegion;
    private WidgetAction mMoveActionExternalRegion;

    private CasaRegionWidget mBindingRegion;
    private CasaRegionWidget mEngineRegion;
    private CasaRegionWidget mExternalRegion;
    private int mMinimumWidthUnit = 100;

    private Widget mLeftResizer;
    private Widget mMiddleResizer;

    private CustomizableDevolveLayout mBindingAutoLayout;
    private CustomizableDevolveLayout mEngineAutoLayout;
    private CustomizableDevolveLayout mExternalAutoLayout;
    
    private CasaDataObject mDataObject;
    private CasaWrapperModel mModel;
    private CasaDesignModelListener mModelListener;
    private CasaNodeFactory mNodeFactory;
    private boolean mIsInternalNodeChange;
    
    private boolean mIsModelPositionsFinalized = false;
    private boolean mIsAdjusting = false;
    
    
    public CasaModelGraphScene(CasaDataObject dataObject, CasaWrapperModel model, CasaNodeFactory nodeFactory) {
        mDataObject = dataObject;
        mModel = model;
        mNodeFactory = nodeFactory;
        
        CasaFactory.getCasaCustomizerRegistor().addPropertyListener(this);
                
        // disallow negative coordinates to prevent the scene from shifting.
        super.setMaximumBounds(new Rectangle(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE));
        
        // standard setup
        addChild(mMainLayer);
        addChild(mConnectionLayer);
        addChild(mGlassLayer);
        addChild(mDragLayer);

        mLeftResizer   = new Widget(this);
        mMiddleResizer = new Widget(this);
        initializeResizer(mMainLayer, mLeftResizer);
        initializeResizer(mMainLayer, mMiddleResizer);

        WidgetMover moverBindingRegion  = new WidgetMover(mModel, false, false);
        WidgetMover moverEngineRegion   = new WidgetMover(mModel, true,  false);
        WidgetMover moverExternalRegion = new WidgetMover(mModel, true,  true);
        mMoveActionBindingRegion  = ActionFactory.createMoveAction(moverBindingRegion,  moverBindingRegion);
        mMoveActionEngineRegion   = ActionFactory.createMoveAction(moverEngineRegion,   moverEngineRegion);
        mMoveActionExternalRegion = ActionFactory.createMoveAction(moverExternalRegion, moverExternalRegion);
        
        TopComponent.getRegistry().addPropertyChangeListener(this);
        
        initializeSceneActions();

        ToolTipManager.sharedInstance().setInitialDelay(Constants.TOOLTIP_INITIAL_DELAY);
    }

    
    // Our layout is finalized if every node widget's location matches its model location.
    public boolean isModelPositionsFinalized() {
        return mIsModelPositionsFinalized;
    }
    
    // Our layout is finalized if every node widget's location matches its model location.
    public void setModelPositionsFinalized(boolean isFinalized) {
        mIsModelPositionsFinalized = isFinalized;
    }
    
    public void persistLocation(CasaNodeWidget widget, Point location) {
        if (location != null && widget.getBounds() != null) {
            if (!isAdjusting()) {
                if (!mIsModelPositionsFinalized) {
                    finalizeModelPositions();
                } else {
                    CasaModelGraphUtilities.updateModelPosition(
                            this,
                            (CasaComponent) findObject(widget), 
                            location);
                }
            }
        }
    }
    
    public void persistWidth(CasaRegionWidget widget) {
        if (!isAdjusting()) {
            if (!mIsModelPositionsFinalized) {
                finalizeModelPositions();
            }
            CasaModelGraphUtilities.updateWidth(this, widget);
        }
    }
    
    private void finalizeModelPositions() {
        boolean anyBadPositions = false;
        for (CasaComponent component : getNodes()) {
            CasaNodeWidget nodeWidget = (CasaNodeWidget) findWidget(component);
            if (nodeWidget != null) {
                Point preferredLocation = nodeWidget.getPreferredLocation();
                if (preferredLocation != null) {
                    CasaModelGraphUtilities.updateModelPosition(
                            this,
                            component, 
                            preferredLocation);
                } else {
                    anyBadPositions = true;
                }
            }
        }
        
        if (anyBadPositions) {
            LOGGER.log(Level.WARNING, NbBundle.getMessage(getClass(), "Warning_Null_Position"));
        } else {
            mIsModelPositionsFinalized = true;
        }
    }
    
    public void setIsAdjusting(boolean isAdjusting) {
        mIsAdjusting = isAdjusting;
    }
    
    public boolean isAdjusting() {
        return mIsAdjusting;
    }
    
    private static void initializeResizer(Widget layer, Widget resizer) {
        layer.addChild(resizer);
        resizer.setOpaque(false);
        resizer.setBackground(RegionUtilities.RESIZER_COLOR);
        resizer.setPreferredBounds(new Rectangle(
            new Dimension(RegionUtilities.RESIZER_WIDTH, RegionUtilities.DEFAULT_HEIGHT)));
    }
    
    private void initializeSceneActions() {
        // Allow the mouse scroll wheel to affect the containing scroll pane.
        getActions().addAction(new MouseWheelScrollAction());
        
        getActions().addAction(ActionFactory.createPanAction());
        
        RegionResizeHandler resizeHandler = new RegionResizeHandler(
                this, 
                mLeftResizer, 
                mMiddleResizer);
        getActions().addAction(new RegionResizeAction(this, mLeftResizer, mMiddleResizer, resizeHandler));
        
        // Add the rectangular selection action last.
        getActions().addAction(new CasaRectangularSelectAction(
                new RectangularSelectDecorator() {
                    public Widget createSelectionWidget() {
                        Widget widget = new Widget(CasaModelGraphScene.this);
                        widget.setBorder(org.netbeans.api.visual.border.BorderFactory.createDashedBorder(
                                Color.DARK_GRAY,
                                4,
                                3,
                                true));
                        widget.setOpaque(false);
                        return widget;
                    }
                },
                mGlassLayer,
                ActionFactory.createObjectSceneRectangularSelectProvider(this)));
        
        getActions().addAction(mPopupMenuAction);
        getActions().addAction(new CasaRemoveAction(this, mModel));
        getActions().addAction(CasaFactory.createAcceptAction(new CasaPaletteAcceptProvider(this, mModel)));
        getActions().addAction(CasaFactory.createCycleCasaSceneSelectAction());        
        
        addSceneListener(new BannerSceneListener(this));
    }
    
    public void registerModelListener(CasaDesignModelListener listener) {
        mModelListener = listener;
    }
    
    public CasaNodeFactory getNodeFactory() {
        return mNodeFactory;
    }
    
    public LayerWidget getDragLayer() {
        return mDragLayer;
    }
    
    public LayerWidget getConnectionLayer() {
        return mConnectionLayer;
    }

    public Widget getLeftResizer() {
        return mLeftResizer;
    }
    
    public Widget getMiddleResizer() {
        return mMiddleResizer;
    }
    
    public CasaWrapperModel getModel() {
        return mModel;
    }
    
    public void updateEdgeRouting(ConnectionWidget singleWidgetUpdate) {
        boolean updateAll = false;
        if (
                getEdges().size() <= CasaCollisionCollector.MAX_ORTHOGONAL_CONNECTIONS &&
                getNodes().size() <= CasaCollisionCollector.MAX_ORTHOGONAL_NODES) {
            if (mCurrentRouter != mOrthogonalRouter) {
                mCurrentRouter = mOrthogonalRouter;
                updateAll = true;
            }
        } else {
            if (mCurrentRouter != mDirectRouter) {
                mCurrentRouter = mDirectRouter;
                updateAll = true;
            }
        }
        
        if (updateAll) {
            for (CasaComponent component : getEdges()) {
                ConnectionWidget connectionWidget = (ConnectionWidget) findWidget(component);
                connectionWidget.setRouter(mCurrentRouter);
            }
        } else if (singleWidgetUpdate != null) {
            singleWidgetUpdate.setRouter(mCurrentRouter);
        }
    }
    
    public void setOrthogonalRouter(Router router) {
        mOrthogonalRouter = router;
    }
    
    /**
     * Performs a layout on the scene.
     * Pre-existing positions are mostly ignored, except that any pre-existing y locations
     * are used as a guide when determining new widget locations.
     */
    public void autoLayout(boolean isAnimating) {
        doLayout(false, isAnimating, 
                getBindingRegion(), getEngineRegion(), getExternalRegion());
    }
    
    /**
     * Performs a layout on the scene.
     * Pre-existing positions are not altered, except in the case of collisions.
     */
    public void progressiveLayout(boolean isAnimating) {
        doLayout(true, isAnimating, getBindingRegion(), getEngineRegion(), getExternalRegion());
    }

    /**
     * Performs a layout on the region.
     * Pre-existing positions are not altered, except in the case of collisions.
     */
    public void progressiveRegionLayout(CasaRegionWidget regionWidget, boolean isAnimating) {
        doLayout(true, isAnimating, regionWidget);
    }

    private void doLayout(
            boolean isProgressive,
            boolean isAnimating,
            CasaRegionWidget ... regionWidgets)
    {
        if (regionWidgets.length == 0) {
            return;
        }
        for (CasaRegionWidget regionWidget : regionWidgets) {
            CustomizableDevolveLayout layout = null;
            if        (regionWidget == mBindingRegion) {
                layout = mBindingAutoLayout;
            } else if (regionWidget == mEngineRegion) {
                layout = mEngineAutoLayout;
            } else if (regionWidget == mExternalRegion) {
                layout = mExternalAutoLayout;
            }
            layout.setIsAdjustingForOverlapOnly(isProgressive);
            layout.setIsAnimating(isAnimating);
            layout.invokeLayout();
        }
        // trigger the layout to actually occur
        validate();
    }
    
    @Override
    protected Widget attachRegionWidget(CasaComponent node) {
        CasaRegionWidget regionWidget = null;
        String regionName = ((CasaRegion) node).getName();
        if        (CasaRegion.Name.WSDL_ENDPOINTS.getName().equals(regionName)) {
            regionWidget = CasaRegionWidget.createBindingRegion(this);
            mBindingRegion = regionWidget;
            mBindingAutoLayout = new CustomizableDevolveLayout(
                    mBindingRegion,
                    new LayoutBindings());
        } else if (CasaRegion.Name.JBI_MODULES.getName().equals(regionName)) {
            regionWidget = CasaRegionWidget.createEngineRegion(this);
            mEngineRegion = regionWidget;
            mEngineAutoLayout = new CustomizableDevolveLayout(
                    mEngineRegion,
                    new LayoutEngines());
        } else if (CasaRegion.Name.EXTERNAL_MODULES.getName().equals(regionName)) {
            regionWidget = CasaRegionWidget.createExternalRegion(this);
            mExternalRegion = regionWidget;
            mExternalAutoLayout = new CustomizableDevolveLayout(
                    mExternalRegion, 
                    new LayoutEngines());
        }
        
        if (regionWidget != null) {
            mMainLayer.addChild(regionWidget);
            regionWidget.bringToBack();
        }
        return regionWidget;
    }
    
    @Override
    protected Widget attachNodeWidget (CasaComponent node) {
        
        CasaNodeWidget widget = null;
        WidgetAction moveAction = null;
        
        if (node instanceof CasaPort) {
            CasaPort port = (CasaPort) node;
            widget = new CasaNodeWidgetBinding(this);
            CasaModelGraphUtilities.updateNodeProperties(mModel, port, widget);
            
            widget.setEditable(mModel.isEditable(port));
//            widget.setWSPolicyAttached(true);
            widget.initializeGlassLayer(mGlassLayer);
            mBindingRegion.addChild(widget);
            moveAction = mMoveActionBindingRegion;
            
            widget.getContainerWidget().getActions().addAction(createObjectHoverAction());
            widget.getActions().addAction(new CasaBadgeEditAction(this));

        } else if (node instanceof CasaServiceEngineServiceUnit) {
            CasaServiceEngineServiceUnit su = (CasaServiceEngineServiceUnit) node;
            CasaRegionWidget region = null;
            
            if (!su.isInternal()) {
                widget = new CasaNodeWidgetEngineExternal(this);
                region = mExternalRegion;
                moveAction = mMoveActionExternalRegion;
            } else {
                widget = new CasaNodeWidgetEngineInternal(this);
                region = mEngineRegion;
                moveAction = mMoveActionEngineRegion;
            }
            CasaModelGraphUtilities.updateNodeProperties(mModel, su, widget);
            
            region.addChild(widget);
            widget.setEditable(mModel.isEditable(su));
            ((CasaNodeWidgetEngine) widget).setConfigurationStatus(su.isDefined());
            widget.initializeGlassLayer(mGlassLayer);
            
            widget.getContainerWidget().getActions().addAction(createObjectHoverAction());
        }
        
        widget.getActions().addAction(createSelectAction());
        widget.getActions().addAction(mPopupMenuAction);
        widget.getActions().addAction(moveAction);
        
        return widget;
    }

    @Override
    protected Widget attachPinWidget (CasaComponent node, CasaComponent pin) {
        
        assert (pin instanceof CasaConsumes) || (pin instanceof CasaProvides);
        
        CasaPinWidget widget = null;
        boolean isConsumes = pin instanceof CasaConsumes;
        boolean isBinding = node instanceof CasaPort; 
        
        if        (isBinding && isConsumes) {
            widget = new CasaPinWidgetBindingConsumes(this);
        } else if (isBinding && !isConsumes) {
            widget = new CasaPinWidgetBindingProvides(this);
        } else if (!isBinding && isConsumes) {
            widget = new CasaPinWidgetEngineConsumes(this);
        } else if (!isBinding && !isConsumes) {
            widget = new CasaPinWidgetEngineProvides(this);
        }
        ((CasaNodeWidget) findWidget(node)).attachPinWidget(widget);
        
        widget.getActions().addAction(createObjectHoverAction());
        widget.getActions().addAction(createSelectAction());
        widget.getActions().addAction(mPopupMenuAction);
        widget.getActions().addAction(new CasaConnectAction(this, mConnectionLayer));

        return widget;
    }

    @Override
    protected Widget attachEdgeWidget (CasaComponent edge) {
        CasaConnectionWidget connectionWidget = new CasaConnectionWidget(this);
        mConnectionLayer.addChild (connectionWidget);

        connectionWidget.getActions().addAction(createObjectHoverAction());
        connectionWidget.getActions().addAction(createSelectAction());
        connectionWidget.getActions().addAction(mPopupMenuAction);
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
    protected void attachEdgeSourceAnchor (CasaComponent edge, CasaComponent oldSourcePin, CasaComponent sourcePin) {
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
    protected void attachEdgeTargetAnchor (CasaComponent edge, CasaComponent oldTargetPin, CasaComponent targetPin) {
        ((ConnectionWidget) findWidget(edge)).setTargetAnchor(getPinAnchor(targetPin));
    }

    private Anchor getPinAnchor (CasaComponent pin) {
        if (pin == null) {
            return null;
        }
        CasaNodeWidget nodeWidget = (CasaNodeWidget) findWidget(getPinNode(pin));
        return nodeWidget.getPinAnchor(findWidget(pin));
    }
    
    @Override
    protected void detachRegionWidget (CasaComponent node, Widget widget) {
        super.detachRegionWidget(node, widget);
        fireSelectionChanged();
    }
    
    @Override
    protected void detachNodeWidget (CasaComponent node, Widget widget) {
        super.detachNodeWidget(node, widget);
        fireSelectionChanged();
    }

    @Override
    protected void detachEdgeWidget (CasaComponent edge, Widget widget) {
        super.detachEdgeWidget(edge, widget);
        fireSelectionChanged();
    }

    @Override
    protected void detachPinWidget (CasaComponent pin, Widget widget) {
        super.detachPinWidget(pin, widget);
        fireSelectionChanged();
    }
    
    protected TopComponent findTopComponent() {
        if (getView() != null) {
            return (TopComponent) SwingUtilities.getAncestorOfClass(
                    TopComponent.class, getView());
        }
        return null;
    }
    
    public void updateSelectionAndRequestFocus(final CasaComponent ... modelObjects) {
        // Select only the given objects, this changes all selected context
        // to apply to just the parameters and no other objects.
        // Properties and actions will apply solely to these objects.
        Set<CasaComponent> objectsToSelect = new HashSet<CasaComponent>();
        for (CasaComponent modelObject : modelObjects) {
            objectsToSelect.add(modelObject);
        }
        userSelectionSuggested(objectsToSelect, false);
        
        transferFocusToGraph();
    }
    
    private void transferFocusToGraph() {
        try {
            mIsInternalNodeChange = true;
            // A scene selection should also cause the graph to have focus.
            // By default, when items are added piece-meal to the graph,
            // we want to ensure that all mouse actions are automatically
            // fully usable - and this requires the graph to have focus.
            // Otherwise, the user will first need to transfer focus to the graph
            // explicitly, and then the user will be able to access all mouse
            // actions (such as creating connections).
            Mode editorMode = WindowManager.getDefault().findMode(CasaDataEditorSupport.EDITOR_MODE);
            for (TopComponent tc : editorMode.getTopComponents()) {
                DataObject dataObject = tc.getLookup().lookup(DataObject.class);
                if (dataObject == mDataObject) {
                    tc.requestActive();
                    break;
                }
            }
            if (!getView().hasFocus()) {
                getView().requestFocusInWindow();
            }
        } finally {
            mIsInternalNodeChange = false;
        }
    }
    
    protected void fireSelectionChanged() {
        if (getView() == null || mModelListener == null) {
            return;
        }
        final TopComponent tc = findTopComponent();
        if (tc == null) {
            return;
        }
        
        final Set<?> selectedObjects = getSelectedObjects();
        
        // Allow the current visual operation to continue.
        // Listeners can be informed later.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Node activeNode = null;
                
                if (selectedObjects.size() < 1) {
                    activeNode = mNodeFactory.createModelNode(mModel);
                } else {
                    CasaComponent component = (CasaComponent) selectedObjects.iterator().next();
                    activeNode = mNodeFactory.createNodeFor(component);
                    if (activeNode == null) {
                        activeNode = mNodeFactory.createModelNode(mModel);
                    }
                }
                
                // Tie-in to Node selection mechanism. This will cause the
                // Navigator and Property Sheet to change context.
                
                if (activeNode != null) {
                    Node[] nodes = new Node[] { activeNode };
                    try {
                        mIsInternalNodeChange = true;
                        tc.setActivatedNodes(nodes);
                    } finally {
                        mIsInternalNodeChange = false;
                    }
                }
            }
        });
    }
    
    public void cleanup() {
        // scene is not used anymore, free up resources
        TopComponent.getRegistry().removePropertyChangeListener(this);
        CasaFactory.getCasaCustomizerRegistor().removePropertyListerner(this);
    }

    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if (mIsInternalNodeChange || getView() == null) {
            return;
        }
            
        String propertyName = propertyChangeEvent.getPropertyName();
        if (propertyName.equals(TopComponent.Registry.PROP_ACTIVATED)) {
            // Do not update during a top component change.
            // We do not want another top component's casa node to cause
            // us to select that other node when we may be attempting to
            // select a new node in our current top component.
        } else if (propertyName.equals(TopComponent.Registry.PROP_ACTIVATED_NODES)) {
            if (getView().isShowing()){
                updateFromExternalNodeChange();
            }
        } else if (propertyName.equals(CasaCustomizer.PROPERTY_CHANGE)) {
            if (getView() != null) {
                CasaGraphMultiViewElement tc =
                       (CasaGraphMultiViewElement) SwingUtilities.getAncestorOfClass(CasaGraphMultiViewElement.class, getView());
                if (!getView().isShowing()) {
                    tc.scheduleLookAndFeelRender();
                } else {
                    CasaFactory.getCasaCustomizer().renderCasaDesignView(this);
               }
           }
       }
   }

    
    // We basically update our selection state when an external component

    
    // We basically update our selection state when an external component
    // fires a Node change event.
    private void updateFromExternalNodeChange() {
        Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
        if (nodes == null || nodes.length == 0) {
            return;
        }
        
        List<CasaComponent> modelComponents = new ArrayList<CasaComponent>();
        for (Node elem : nodes) {
            if (elem instanceof CasaNode) {
                Object data = ((CasaNode) elem).getData();
                if (data instanceof CasaComponent) {
                    modelComponents.add( (CasaComponent) data );
                }
            }
        }
        
        Set<CasaComponent> objectsToSelect = new HashSet<CasaComponent>();
        for (CasaComponent component : modelComponents) {
            if (
                    component instanceof CasaPort ||
                    component instanceof CasaServiceEngineServiceUnit ||
                    component instanceof CasaConsumes ||
                    component instanceof CasaProvides ||
                    component instanceof CasaConnection) {
                objectsToSelect.add(component);
            }
        }
        
        // Safely set our selected objects. Do not fire selection events.
        Set<CasaComponent> validObjectsToSelect = new HashSet<CasaComponent>();
        for (CasaComponent component : objectsToSelect) {
            if (getObjectState(component) != null) {
                validObjectsToSelect.add(component);
            }
        }
        if (validObjectsToSelect.size() > 0) {
            setSelectedObjects(validObjectsToSelect);
            getView().repaint();
        }
    }

    public JComponent getViewComponent() {
        if (getView() == null) {
            // Create the scene component.
            createView();
            // Special mouse listener that ensures scene component
            // has focus when it is clicked.
            getView().addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if (!getView().hasFocus()) {
                        getView().requestFocusInWindow();
                    }
                }
            });
        }
        return getView();
    }
    
    public CasaRegionWidget getBindingRegion() {
        return mBindingRegion;
    }
    
    public CasaRegionWidget getEngineRegion() {
        return mEngineRegion;
    }
    
    public CasaRegionWidget getExternalRegion() {
        return mExternalRegion;
    }
    
    public boolean isModified() {
        return mDataObject.isModified();
    }
    
    public boolean canEdit() {
        return !mDataObject.isBuilding();
    }
    
    /**
     * Obtains the location of the src/ directory within
     * the Composite Application project.
     */
    public String getSourcesPath() {
        return mDataObject.getFolder().getPrimaryFile().getParent().getPath();
    }
        /**
     * This method returns an identity code. It should be unique for each object in the scene.
     * The identity code is a Comparable and could be used for sorting.
     * The method implementation should be fast.
     * @param object the object
     * @return the identity code of the object; null, if the object is null
     */
    public Comparable getIdentityCode (Object object) {
        if(object == null || object instanceof CasaRegion) {
            return null;
        }
        String idCode = "";
        if(object instanceof CasaPort) {
            idCode = getIdentityCode((CasaPort) object).toString();
        } else if(object instanceof CasaConnection) {
            idCode = getIdentityCode((CasaConnection) object).toString();
        } else if(object instanceof CasaServiceEngineServiceUnit) {
            idCode = getIdentityCode((CasaServiceEngineServiceUnit) object).toString();
        } else if(object instanceof CasaConsumes) {
            idCode = getIdentityCode((CasaConsumes) object).toString();
        } else if(object instanceof CasaProvides) {
            idCode = getIdentityCode((CasaProvides) object).toString();
        } else {
        }
        return idCode;
    }
    
    private StringBuffer getIdentityCode(CasaPort casaPort) {
        StringBuffer retString = new StringBuffer("A");
        retString.append(String.format("%010d",casaPort.getX()));
        retString.append(String.format("%010d",casaPort.getY()));
        return retString;
    }
    
    private StringBuffer getIdentityCode(CasaServiceEngineServiceUnit casaSU) {
        String code = casaSU.isInternal() ? "C" : "E";
        StringBuffer retString = new StringBuffer(code);
        retString.append(String.format("%010d",casaSU.getY()));
        retString.append(String.format("%010d",casaSU.getX()));
        return retString;
    }
    private StringBuffer getIdentityCode(CasaConsumes casaConsumes) {
        StringBuffer retString = new StringBuffer("");
        CasaComponent parent = casaConsumes.getParent();
        if(parent instanceof CasaPort) {
            retString = getIdentityCode((CasaPort) parent);
        } else if(parent instanceof CasaServiceEngineServiceUnit) {
            retString = getIdentityCode((CasaServiceEngineServiceUnit) parent);
        }
        retString.append("C");
        retString.append(String.format("%010d",casaConsumes.findPosition()));
        return retString;
    }
    private StringBuffer getIdentityCode(CasaProvides casaProvides) {
        StringBuffer retString = new StringBuffer("");
        CasaComponent parent = casaProvides.getParent();
        if(parent instanceof CasaPort) {
            retString = getIdentityCode((CasaPort) parent);
        } else if(parent instanceof CasaServiceEngineServiceUnit) {
            retString = getIdentityCode((CasaServiceEngineServiceUnit) parent);
        }
        retString.append("P");
        retString.append(String.format("%010d",casaProvides.findPosition()));
        return retString;
    }
    private StringBuffer getIdentityCode(CasaConnection casaConnection) {
        StringBuffer retString = new StringBuffer("G");
        retString.append(String.format("%010d",casaConnection.findPosition()));
        return retString;
    }
}
