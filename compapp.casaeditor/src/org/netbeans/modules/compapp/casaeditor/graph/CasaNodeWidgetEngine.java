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

package org.netbeans.modules.compapp.casaeditor.graph;

import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.*;

import java.awt.*;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.model.StateModel;
import org.netbeans.modules.compapp.casaeditor.graph.awt.BorderedRectangularPainter;
import org.netbeans.modules.compapp.casaeditor.graph.awt.BorderedRectangularProvider;
import org.netbeans.modules.compapp.casaeditor.graph.awt.InnerGlowBorderDrawer;
import org.netbeans.modules.compapp.casaeditor.graph.awt.Painter;
import org.netbeans.modules.compapp.casaeditor.graph.awt.PainterWidget;

/**
 *
 * @author rdara
 */
public class CasaNodeWidgetEngine extends CasaNodeWidget implements StateModel.Listener {
    
    public static final int ARROW_PIN_WIDTH           = 25;
    public static final int MARGIN_SE_ROUNDED_RECTANGLE = ARROW_PIN_WIDTH * 77 / 100;
    
    private static final int PIN_VERTICAL_GAP         = 5;
    
    private static final int INVISIBLE_WRAPPER_OFFSET = 10;
    private static final int MINIMUM_SE_NODE_HEIGHT   = 20;
    private static final int MINIMUM_SE_NODE_WIDTH    = 120;
    
    private Widget mTopWidgetHolder;
    private Widget mPinsHolderWidget;
    private CasaEngineTitleWidget mTitleWidget;
    private String mNodeType;
    private Widget mMinimizedMoveCleanerWidget;
    
    private Dimension mPreviousHolderSize = new Dimension();
    private StateModel mStateModel = new StateModel(2);
    private Anchor mNodeAnchor = new CasaNodeAnchor(this);

    private boolean mIsHighlighted;
    
    
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
        
        final BorderedRectangularProvider provider = new BorderedRectangularProvider() {
            public Color getBorderColor() {
                if (getState().isSelected()) {
                    return CasaFactory.getCasaCustomizer().getCOLOR_SELECTION();
                } else {
                    return CasaFactory.getCasaCustomizer().getCOLOR_SU_INTERNAL_BORDER();
                }
            }
            public Color getBackgroundColor() {
                return CasaFactory.getCasaCustomizer().getCOLOR_SU_INTERNAL_BACKGROUND();
            }
            public Rectangle getHeaderRect() {
                if (mTitleWidget.getBounds() != null) {
                    return getClipRect().intersection(mTitleWidget.getBounds());
                }
                return null;
            }
            public Color getHeaderColor() {
                return CasaFactory.getCasaCustomizer().getCOLOR_BC_TITLE_BACKGROUND();
            }
            public Rectangle getClipRect() {
                Rectangle clientArea = mPinsHolderWidget.getClientArea();
                Rectangle gradientRect = new Rectangle();

                gradientRect.x = clientArea.x + MARGIN_SE_ROUNDED_RECTANGLE;
                gradientRect.y = clientArea.y ;
                gradientRect.width = clientArea.width - MARGIN_SE_ROUNDED_RECTANGLE - MARGIN_SE_ROUNDED_RECTANGLE;
                gradientRect.height = clientArea.height;

                return gradientRect;
            }
            public boolean isRounded() {
                return true;
            }
        };
        
        Painter customWidgetPainter = new Painter() {
            public void paint(Graphics2D g) {
                // Draw a line below the title widget to separate
                // the title from the pin area.
                Rectangle rect = mTitleWidget.getBounds();
                if (rect != null) {
                    if (getState().isSelected()) {
                        g.setColor(CasaFactory.getCasaCustomizer().getCOLOR_SELECTION());
                    } else {
                        g.setColor(CasaFactory.getCasaCustomizer().getCOLOR_SU_INTERNAL_BORDER());
                    }
                    g.drawLine(0, rect.height, rect.width, rect.height);
                }
                if (isHighlighted()) {
                    InnerGlowBorderDrawer.paintInnerGlowBorder(
                            getGraphics(),
                            provider.getClipRect(),
                            CasaFactory.getCasaCustomizer().getCOLOR_SELECTION(),
                            0.6f,
                            10);
                }
            }
        };

        mPinsHolderWidget = new PainterWidget(scene, new BorderedRectangularPainter(provider, customWidgetPainter));
        mPinsHolderWidget.setOpaque(false);
        
        mPinsHolderWidget.addChild(mTitleWidget);
        mPinsHolderWidget.setLayout(LayoutFactory.createVerticalLayout(
                LayoutFactory.SerialAlignment.LEFT_TOP, 
                PIN_VERTICAL_GAP));
        
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
                
                Rectangle bounds = mPinsHolderWidget.getClientArea().getBounds();
                if (!bounds.getSize().equals(mPreviousHolderSize))
                {
                    mPreviousHolderSize = bounds.getSize();
                    
                    if (bounds.width < MINIMUM_SE_NODE_WIDTH) {
                        mPreviousHolderSize.width = MINIMUM_SE_NODE_WIDTH;
                        bounds.width = MINIMUM_SE_NODE_WIDTH;
                    }
                    if (bounds.height < MINIMUM_SE_NODE_HEIGHT) {
                        mPreviousHolderSize.height = MINIMUM_SE_NODE_HEIGHT;
                        bounds.height = MINIMUM_SE_NODE_HEIGHT;
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

    /**
     * Sets all node properties at once.
     */
    public void setNodeProperties(String nodeName, String nodeType) {
        mNodeType = nodeType;
        boolean hasNodeName = nodeName != null && nodeName.length() > 0;
        boolean hasNodeType = nodeType != null && nodeType.length() > 0;
        if (hasNodeType && hasNodeName) {
            mTitleWidget.setLabel("(" + nodeType + ") " + nodeName);    // NOI18N
        } else if (hasNodeType) {
            mTitleWidget.setLabel("(" + nodeType + ")");                // NOI18N
        } else if (hasNodeName) {
            mTitleWidget.setLabel(nodeName);                            // NOI18N
        }
        
        if (getBounds() != null) {
            readjustBounds();
        }
    }
    
    public void stateChanged() {
        readjustBounds();
    }
    
    public void readjustBounds() {
        boolean isMinimized = mStateModel.getBooleanState();
        for (Widget child : mPinsHolderWidget.getChildren()) {
            if (child instanceof CasaPinWidget) {
                ((CasaPinWidget) child).updateBounds(isMinimized);
            }
        }
        mTitleWidget.updateBounds(isMinimized);
        mPinsHolderWidget.setPreferredBounds(isMinimized ? mTitleWidget.getPreferredBounds() : null);
        mPreviousHolderSize = new Dimension();
        getScene().revalidate();
        getScene().validate();
        invokeDependencies();
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
        if (previousState.isSelected() != state.isSelected()) {
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
        setPinFont(getPinFont());
        setPinColor(getPinColor());
        mPinsHolderWidget.setPreferredBounds(null);
    }

    /*
     * mPinsHolderWidget is having a mTitleWidget followed by Provide and Consume pin widgets, will be ended by cushioning 
     * widget. CasaPinWidets need to be from 2nd position to the last but one position.
     * 
     * Provide need to be added at the last of provide pins and same is true for consume pins.
     * 
     * CshioningWidget will be added only when there is a CasaPinWidget, to avoid an empty space when there are no pins.
     *  
     */
    private void addPinWithOrdering(CasaPinWidget widget) {
        if(mPinsHolderWidget.getChildren().size() <= 1) {       //Add cushoningWidget only once!
            Widget cushioningWidget = new Widget(getScene());   //mTitlewidget is already present and thus checking the size with "1"
            cushioningWidget.setPreferredBounds(new Rectangle(0,PIN_VERTICAL_GAP));
            mPinsHolderWidget.addChild(cushioningWidget);
        }
        int insertionIndex = 1;    //TitleWidget is already there
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
                //insertionIndex++;
            }
        }
        mPinsHolderWidget.addChild(insertionIndex, widget);
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

    public Font getPinFont() {
        return null;
    }
    public Color getPinColor() {
        return null;
    }
    public void setPinFont(Font font) {
        for (Widget child : mPinsHolderWidget.getChildren()) {
            if (child instanceof CasaPinWidgetEngine) {
                ((CasaPinWidgetEngine)child).setLabelFont(font);
            }
        }
    }
    public void setPinColor(Color color) {
        for (Widget child : mPinsHolderWidget.getChildren()) {
            if (child instanceof CasaPinWidgetEngine) {
                ((CasaPinWidgetEngine)child).setLabelColor(color);
            }
        }
    }
    
    public void setHighlighted(boolean isHighlighted) {
        if (mIsHighlighted != isHighlighted) {
            mIsHighlighted = isHighlighted;
            repaint();
        }
    }

    public boolean isHighlighted() {
        return mIsHighlighted;
    }
}
