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
import java.util.Map;
import java.util.Set;
import org.netbeans.api.visual.action.RectangularSelectDecorator;
import org.netbeans.api.visual.layout.LayoutFactory;
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
import org.netbeans.modules.compapp.casaeditor.graph.layout.CasaRectangularSelectAction;
import org.netbeans.modules.compapp.casaeditor.graph.layout.CustomizableDevolveLayout;
import org.netbeans.modules.compapp.casaeditor.graph.layout.LayoutBindings;
import org.netbeans.modules.compapp.casaeditor.graph.layout.LayoutEngines;
import org.netbeans.modules.compapp.casaeditor.graph.layout.LayoutModelLoad;
import org.netbeans.modules.compapp.casaeditor.graph.layout.ModelLoadLayoutInfo;
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
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

/**
 * 
 * @author Josh Sandusky (modified version of David Kaspar's VMDGraphScene)
 */
public class CasaModelGraphScene 
extends CasaGraphAbstractScene<CasaComponent, CasaComponent, CasaComponent>
implements PropertyChangeListener {

    private LayerWidget mMainLayer       = new LayerWidget(this);
    private LayerWidget mConnectionLayer = new LayerWidget(this);
    private LayerWidget mGlassLayer      = new LayerWidget(this);
    
    private LayerWidget mDragLayer = new LayerWidget(this);

    private Router mRouter;

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
    
    private CasaWrapperModel mModel;
    private CasaDesignController mController;
    private CasaNodeFactory mNodeFactory;
    private boolean mIsInternalNodeChange;
    
    
    public CasaModelGraphScene(CasaWrapperModel model, CasaNodeFactory nodeFactory) {
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

        WithinRegionMover moverBindingRegion  = new WithinRegionMover(mModel, false, false);
        WithinRegionMover moverEngineRegion   = new WithinRegionMover(mModel, true,  false);
        WithinRegionMover moverExternalRegion = new WithinRegionMover(mModel, true,  true);
        mMoveActionBindingRegion  = ActionFactory.createMoveAction(moverBindingRegion,  moverBindingRegion);
        mMoveActionEngineRegion   = ActionFactory.createMoveAction(moverEngineRegion,   moverEngineRegion);
        mMoveActionExternalRegion = ActionFactory.createMoveAction(moverExternalRegion, moverExternalRegion);
        
        TopComponent.getRegistry().addPropertyChangeListener(this);
        
        initializeSceneActions();

        ToolTipManager.sharedInstance().setInitialDelay(Constants.TOOLTIP_INITIAL_DELAY);
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
                                Color.BLACK,
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
        
        addSceneListener(new BannerSceneListener(this));
    }
    
    public void registerController(CasaDesignController controller) {
        mController = controller;
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
    
    public void setRouter(Router router) {
        mRouter = router;
    }
    
    public void autoLayout(boolean isPersistingLocations, boolean isAnimating) {
        if (
                mBindingAutoLayout  != null &&
                mEngineAutoLayout   != null &&
                mExternalAutoLayout != null) {
            mBindingAutoLayout.setIsAdjustingForOverlapOnly(false);
            mEngineAutoLayout.setIsAdjustingForOverlapOnly(false);
            mExternalAutoLayout.setIsAdjustingForOverlapOnly(false);
            mBindingAutoLayout.setIsAnimating(isAnimating);
            mEngineAutoLayout.setIsAnimating(isAnimating);
            mExternalAutoLayout.setIsAnimating(isAnimating);
            mBindingAutoLayout.setIsPersisting(isPersistingLocations);
            mEngineAutoLayout.setIsPersisting(isPersistingLocations);
            mExternalAutoLayout.setIsPersisting(isPersistingLocations);
            mBindingAutoLayout.invokeLayout();
            mEngineAutoLayout.invokeLayout();
            mExternalAutoLayout.invokeLayout();
        }
        // trigger the layout to actually occur
        validate();
    }
    
    /**
     * Performs an autolayout in only the specified region.
     * Specifying isPreserving to be false performs a instant autolayout from scratch.
     * Specifying isPreserving to be true only adjusts widgets (with animation) if an overlap occurs.
     * @param regionWidget  the region to perform the layout in
     * @param isPreserving  whether current widget locations should be preserved as best as possible
     */
    public void invokeRegionLayout(CasaRegionWidget regionWidget, boolean isPreserving) {
        CustomizableDevolveLayout layout = null;
        if        (regionWidget == mBindingRegion) {
            layout = mBindingAutoLayout;
        } else if (regionWidget == mEngineRegion) {
            layout = mEngineAutoLayout;
        } else if (regionWidget == mExternalRegion) {
            layout = mExternalAutoLayout;
        }
        if (layout != null) {
            layout.setIsAnimating(isPreserving);
            layout.setIsAdjustingForOverlapOnly(isPreserving);
            layout.setIsPersisting(isPreserving);
            layout.invokeLayout();
            // trigger the layout to actually occur
            validate();
        }
    }

    public void modelLoadLayout(Map<Widget, ModelLoadLayoutInfo> modelRestoreInfoMap) {
        LayoutFactory.createDevolveWidgetLayout(
                this,
                new LayoutModelLoad(modelRestoreInfoMap),
                false).invokeLayout();
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
            widget.setEditable(mModel.isEditable(port));
//            widget.setWSPolicyAttached(true);
            widget.initializeGlassLayer(mGlassLayer);
            mBindingRegion.addChild(widget);
            moveAction = mMoveActionBindingRegion;
            
            widget.getContainerWidget().getActions().addAction(createObjectHoverAction());
            widget.getActions().addAction(new CasaBadgeEditAction(this));

        } else if (node instanceof CasaServiceEngineServiceUnit) {
            CasaServiceEngineServiceUnit su = (CasaServiceEngineServiceUnit) node;
            
            if (!su.isInternal()) {
                widget = new CasaNodeWidgetEngineExternal(this);
                mExternalRegion.addChild(widget);
                moveAction = mMoveActionExternalRegion;
            } else {
                widget = new CasaNodeWidgetEngineInternal(this);
                mEngineRegion.addChild(widget);
                moveAction = mMoveActionEngineRegion;
            }
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
        CasaConnectionWidget connectionWidget = new CasaConnectionWidget(this, mRouter);
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
        if (widget instanceof CasaNodeWidget) {
            ((CasaNodeWidget) widget).removeAllDependencies();
        }
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

    protected void fireSelectionChanged() {
        if (getView() == null || mController == null) {
            return;
        }
        final TopComponent tc = findTopComponent();
        if (tc == null) {
            return;
        }
        
        // Allow the current visual operation to continue.
        // Listeners can be informed later.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Node activeNode = null;
                
                Set<?> selectedObjects = getSelectedObjects();
                if (selectedObjects.size() < 1) {
                    activeNode = mNodeFactory.createModelNode(mModel);
                } else {
                    CasaComponent component = (CasaComponent) selectedObjects.iterator().next();
                    activeNode = mNodeFactory.createNodeFor(component);
                }
                
                // Tie-in to Node selection mechanism. This will cause the
                // Navigator and Property Sheet to change context.
                
                if (activeNode != null) {
                    Node[] nodes = new Node[] { activeNode };
                    mIsInternalNodeChange = true;
                    tc.setActivatedNodes(nodes);
                    mIsInternalNodeChange = false;
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
    
    public void setCasaLocation(CasaServiceEngineServiceUnit su, int x, int y) {
        mModel.setServiceEngineServiceUnitLocation(su, x, y);
    }
    
    public void setCasaLocation(CasaPort port, int x, int y) {
        mModel.setCasaPortLocation(port, x, y);
    }
}
