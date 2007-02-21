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

/*
 * CasaNodeWidgetEngine.java
 *
 * Created on January 12, 2007, 9:56 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.compapp.casaeditor.graph;

import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.*;

import java.awt.*;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.model.StateModel;

/**
 *
 * @author rdara
 */
public class CasaNodeWidgetEngine extends CasaNodeWidget implements StateModel.Listener,  CasaGradientInterface {
    
    private static final int INVISIBLE_WRAPPER_OFFSET = 10;
    private static final int MINIMUM_SE_NODE_HEIGHT   = 20;
    private static final int MINIMUM_SE_NODE_WIDTH    = 120;
    
    private Widget mTopWidgetHolder;
    private CasaGradientWidget mPinsHolderWidget;
    private CasaEngineTitleWidget mTitleWidget;
    private String mNodeType;
    private int mPreviousHolderHeight;
    private Widget mMinimizedMoveCleanerWidget;
    
    private StateModel mStateModel = new StateModel(2);
    private Anchor mNodeAnchor = new CasaNodeAnchor(this);

    /**
     * Creates a node widget.
     * @param scene the scene
     */
    public CasaNodeWidgetEngine(Scene scene) {
        super(scene);
        setOpaque(false);
        setLayout(LayoutFactory.createVerticalLayout());
        
        mStateModel = new StateModel();
        mStateModel.addListener(this);
        
        mHeader = new Widget(scene);
        mHeader.setOpaque(false);
        mHeader.setLayout(LayoutFactory.createVerticalLayout(LayoutFactory.SerialAlignment.CENTER, 0));
        addChild(mHeader);
        
        mTitleWidget = new CasaEngineTitleWidget(scene, mStateModel);
        
        mPinsHolderWidget = new CasaGradientWidget(
                scene,
                // Draw a line below the title widget to separate 
                // the title from the pin area.
                new GradientRectangleDrawer.CustomPainter() {
                    public void paint(Graphics g) {
                        Rectangle rect = mTitleWidget.getBounds();
                        if (rect != null) {
                            if (getState().isSelected()) {
                                g.setColor(CasaFactory.getCasaCustomizer().getCOLOR_SELECTED_BORDER());
                            } else {
                                g.setColor(CasaFactory.getCasaCustomizer().getCOLOR_SU_INTERNAL_BORDER());
                            }
                            g.drawLine(0, rect.height, rect.width, rect.height);
                        }
                    }
                }, this);

        mPinsHolderWidget.setOpaque(false);
        mPinsHolderWidget.addChild(mTitleWidget);
        mPinsHolderWidget.setLayout(LayoutFactory.createVerticalLayout(LayoutFactory.SerialAlignment.LEFT_TOP, 2));
        
        mTopWidgetHolder = new Widget(scene);
        mTopWidgetHolder.setOpaque(false);
        mTopWidgetHolder.addChild(mPinsHolderWidget);
        
        mHeader.addChild(mTopWidgetHolder);
        
        notifyStateChanged(ObjectState.createNormal(), ObjectState.createNormal());
        
        final Widget.Dependency pinSizer = new Widget.Dependency() {
            // Maintains the height of the vertical text bar.
            public void revalidateDependency() {
                if (
                        getScene().getGraphics() == null || 
                        getBounds() == null || 
                        getParentWidget() == null) {
                    return;
                }
                if (mPinsHolderWidget.getClientArea().getBounds().height != mPreviousHolderHeight)
                {
                    Rectangle bounds = mPinsHolderWidget.getClientArea().getBounds();
                    mPreviousHolderHeight = mPinsHolderWidget.getClientArea().getBounds().height;
                    if (bounds.height < MINIMUM_SE_NODE_HEIGHT) {
                        mPreviousHolderHeight = MINIMUM_SE_NODE_HEIGHT;
                        bounds.height = MINIMUM_SE_NODE_HEIGHT;
                    }
                    if (bounds.width < MINIMUM_SE_NODE_WIDTH) {
                        bounds.width = MINIMUM_SE_NODE_WIDTH;
                    }
                    
                    mPinsHolderWidget.setPreferredBounds(bounds);

                    /* All pins bounds need to be set so that the Anchor will be calculated correctly */
                    Rectangle childBounds;
                    for (Widget child : mPinsHolderWidget.getChildren ()) {
                        if (child.getBounds() != null) {
                            childBounds = child.getPreferredBounds();
                            childBounds.width = bounds.width;
                            child.setPreferredBounds(childBounds);
                        }
                    }
                }
            }
        };
        
        mDependencies.add(pinSizer);
        addDependency(pinSizer);
    }

    public void readjustBounds() {
        boolean isMinimized = false;
        Rectangle rectangle = null;
        for (Widget child : mPinsHolderWidget.getChildren()) {
            if (child instanceof CasaPinWidget) {
                ((CasaPinWidget) child).readjustBounds(rectangle);
            } else if(child instanceof CasaEngineTitleWidget) {
                ((CasaEngineTitleWidget) child).readjustBounds(rectangle);
            }
        }
        mPinsHolderWidget.setPreferredBounds(rectangle);
        getScene().revalidate();
        getScene().validate();
    }

    protected Color getBackgroundColor() {
        return CasaFactory.getCasaCustomizer().getCOLOR_REGION_ENGINE();
    }
    protected Color getPinHolderBorderColor() {
        return CasaFactory.getCasaCustomizer().getCOLOR_SU_INTERNAL_BORDER();
    }
    protected Color getPinHolderTitleColor() {
        return CasaFactory.getCasaCustomizer().getCOLOR_SU_INTERNAL_TITLE();
    }
    protected Color getPinHolderBackgroundColor() {
        return CasaFactory.getCasaCustomizer().getCOLOR_SU_INTERNAL_BACKGROUND();
    }
    
    public void setTitleFont(Font font) {
        mTitleWidget.setTitleFont(font);
        if (getBounds() != null) {
            readjustBounds();
        }
    }
    
    public void setTitleColor(Color color) {
        mTitleWidget.setTitleColor(color);
    }


    public void paintWidget() {
        mPinsHolderWidget.paintWidget();
    }
    
    public void initializeGlassLayer(LayerWidget layer) {
        mMinimizedMoveCleanerWidget = new Widget(getScene());
        mMinimizedMoveCleanerWidget.setOpaque(false);
        layer.addChild(mMinimizedMoveCleanerWidget);
        
        // The minimized move cleaner has the magical ability to 
        // remove painting artifacts created when widget is moved.
        // It ensures that the area around the top/bottom edges 
        // of the widget will need to be repainted.
        // This is somewhat of a temporary hack until enough
        // time exists to determine the exact reason for the glitch.
        Widget.Dependency minimizedMoveCleaner = new Widget.Dependency() {
            public void revalidateDependency() {
                if (
                        getScene().getGraphics() == null || 
                        getBounds() == null ||
                        getParentWidget() == null) {
                    return;
                }
                Rectangle myRect = getBounds();
                mMinimizedMoveCleanerWidget.setPreferredBounds(new Rectangle(
                        0, 
                        0, 
                        myRect.width  + INVISIBLE_WRAPPER_OFFSET * 2, 
                        myRect.height + INVISIBLE_WRAPPER_OFFSET * 2));
                Point point = convertLocalToScene(new Point(
                        -INVISIBLE_WRAPPER_OFFSET,
                        -INVISIBLE_WRAPPER_OFFSET));
                mMinimizedMoveCleanerWidget.setPreferredLocation(point);
            }
        };
        mDependencies.add(minimizedMoveCleaner);
        addDependency(minimizedMoveCleaner);
    }

    public void removeAllDependencies() {
        super.removeAllDependencies();
        mMinimizedMoveCleanerWidget.removeFromParent();
    }
    
    protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
        super.notifyStateChanged(previousState, state);
        if (!previousState.isSelected() && state.isSelected()) {
            //mPinsHolderWidget.setSelected(true);
            repaint();
        } else if (previousState.isSelected() && !state.isSelected()) {
            //mPinsHolderWidget.setSelected(false);
            repaint();
        }
    }
    
    /**
     * Attaches a pin widget to the node widget.
     * @param widget the pin widget
     */
    public void attachPinWidget(CasaPinWidget widget) {
        // All Provides pins should come before Consumes pins.
        addPinWithOrdering(widget);
        mPinsHolderWidget.setPreferredBounds(null);
    }
    
    private void addPinWithOrdering(CasaPinWidget widget) {
        int insertionIndex = 0;
        boolean isProvides = false;
        if (widget instanceof CasaPinWidgetEngineProvides) {
            isProvides = true;
        }
        for (Widget child : mPinsHolderWidget.getChildren()) {
            if (child instanceof CasaPinWidget) {
                if (child instanceof CasaPinWidgetEngineProvides) {
                    insertionIndex++;
                } else {
                    if (isProvides) {
                        break;
                    }
                    insertionIndex++;
                }
            } else {
                insertionIndex++;
            }
        }
        if (insertionIndex > mPinsHolderWidget.getChildren().size()) {
            mPinsHolderWidget.addChild(widget);
        } else {
            mPinsHolderWidget.addChild(insertionIndex, widget);
        }
    }
    
    /**
     * Creates an extended pin anchor with an ability of reconnecting to the node anchor when the node is minimized.
     * @param anchor the original pin anchor from which the extended anchor is created
     * @return the extended pin anchor
     */
    public Anchor createAnchorPin (Anchor pinAnchor) {
        return AnchorFactory.createProxyAnchor(mStateModel, pinAnchor, mNodeAnchor);
    }
    
    /**
     * If no pin anchor is found, return the node anchor.
     */
    public Anchor getPinAnchor(Widget pinMainWidget) {
        Anchor anchor = super.getPinAnchor(pinMainWidget);
        if (anchor == null) {
            anchor = mNodeAnchor;
        }
        return anchor;
    }

    /**
     * Sets all node properties at once.
     */
    public void setNodeProperties(String nodeName, String nodeType) {
        mNodeType = nodeType;
        boolean hasNodeName = nodeName != null && nodeName.length() > 0;
        boolean hasNodeType = nodeType != null && nodeType.length() > 0;
        if (hasNodeType && hasNodeName) {
            mTitleWidget.setLabel("(" + nodeType + ") " + nodeName);
        } else if (hasNodeName) {
            mTitleWidget.setLabel(nodeName);
        } else if (hasNodeType) {
            mTitleWidget.setLabel("(" + nodeName + ")");
        }
        
        if (getBounds() != null) {
            readjustBounds();
        }
    }
    
    public void stateChanged() {
        boolean isMinimized = mStateModel.getBooleanState();
        Rectangle rectangle = isMinimized ? new Rectangle() : null;
        for (Widget child : mPinsHolderWidget.getChildren()) {
            if (child instanceof CasaPinWidget) {
                ((CasaPinWidget) child).setMinimized(isMinimized);
            }
        }
        mTitleWidget.setMinimized(isMinimized);
        mPinsHolderWidget.setPreferredBounds(isMinimized ? mTitleWidget.getPreferredBounds() : null);
        getScene().revalidate();
        getScene().validate();
        invokeDependencies();
    }
    
    public boolean getConfigurationStatus(){
        return mTitleWidget.getConfigurationStatus();
    }
    
    public void setConfigurationStatus(boolean bConfStatus) {
        mTitleWidget.setConfigurationStatus(bConfStatus);
    }
    
    public void setEditable(boolean bValue) {
        super.setEditable(bValue);
        mTitleWidget.setEditable(bValue);
    }
    
    public GradientRectangleColorScheme getGradientColorSceheme() {
        return null;
    }
    
    public boolean isBorderShown() {
        return true;
    }

    public Rectangle getRectangleToBePainted() {
        Rectangle clientArea = mPinsHolderWidget.getClientArea();
        Rectangle gradientRect = new Rectangle();
        //X_MARGIN_SE_ROUNDED_RECTANGLE = RegionUtilities.MARGIN_SE_ROUNDED_RECTANGLE; 
        //Y_MARGIN_ROUNDED_RECTANGLE = 0;

        gradientRect.x = clientArea.x + RegionUtilities.MARGIN_SE_ROUNDED_RECTANGLE;
        gradientRect.y = clientArea.y ;
        gradientRect.width = clientArea.width - RegionUtilities.MARGIN_SE_ROUNDED_RECTANGLE - RegionUtilities.MARGIN_SE_ROUNDED_RECTANGLE;
        gradientRect.height = clientArea.height ;
        
        return gradientRect;
    }
}
