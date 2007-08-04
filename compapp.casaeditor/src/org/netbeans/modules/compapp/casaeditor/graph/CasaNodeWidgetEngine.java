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
import org.netbeans.api.visual.action.ActionFactory;
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
public class CasaNodeWidgetEngine extends CasaNodeWidget implements StateModel.Listener, CasaMinimizable {

    public static final int ARROW_PIN_WIDTH = 25;
    public static final int MARGIN_SE_ROUNDED_RECTANGLE = ARROW_PIN_WIDTH * 77 / 100;
    private static final int PIN_VERTICAL_GAP = 5;

    // The amount of space below the widget,
    // to help visually separate it from other widgets that might
    // be immediately below it.
    private static final int TRAILING_VERTICAL_GAP = 4;

    private CasaEngineTitleWidget mTitleWidget;
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
        setLayout(LayoutFactory.createVerticalFlowLayout());

        mStateModel = new StateModel();
        mStateModel.addListener(this);

        mTitleWidget = new CasaEngineTitleWidget(scene, mStateModel);

        final BorderedRectangularProvider provider = new BorderedRectangularProvider() {

            public Color getBorderColor() {
                if (getState().isSelected() || getState().isFocused()) {
                    return CasaFactory.getCasaCustomizer().getCOLOR_SELECTION();
                } else {
                    return CasaFactory.getCasaCustomizer().getCOLOR_SU_INTERNAL_BORDER();
                }
            }

            public Color getBackgroundColor() {
                return CasaFactory.getCasaCustomizer().getCOLOR_SU_INTERNAL_BACKGROUND();
            }

            public Rectangle getHeaderRect() {
                Rectangle titleBounds = mTitleWidget.getBounds();
                if (titleBounds != null) {
                    Rectangle clipRect = getClipRect();
                    clipRect.height = titleBounds.height;
                    return clipRect;
                }
                return null;
            }

            public Color getHeaderColor() {
                return CasaFactory.getCasaCustomizer().getCOLOR_BC_TITLE_BACKGROUND();
            }

            public Rectangle getClipRect() {
                Rectangle clientArea = mContainerWidget.getClientArea();
                Rectangle gradientRect = new Rectangle();

                gradientRect.x = clientArea.x + MARGIN_SE_ROUNDED_RECTANGLE;
                gradientRect.y = clientArea.y;
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
                Rectangle rect = provider.getHeaderRect();
                if (rect != null) {
                    if (getState().isSelected() || getState().isFocused()) {
                        g.setColor(CasaFactory.getCasaCustomizer().getCOLOR_SELECTION());
                    } else {
                        g.setColor(CasaFactory.getCasaCustomizer().getCOLOR_SU_INTERNAL_BORDER());
                    }
                    g.drawLine(rect.x, rect.y + rect.height, rect.x + rect.width, rect.y + rect.height);
                }
                if (isHighlighted()) {
                    InnerGlowBorderDrawer.paintInnerGlowBorder(getGraphics(), provider.getClipRect(), CasaFactory.getCasaCustomizer().getCOLOR_SELECTION(), 0.6f, 10);
                }
            }
        };

        mContainerWidget = new PainterWidget(scene, new BorderedRectangularPainter(provider, customWidgetPainter));
        mContainerWidget.setOpaque(false);
        mContainerWidget.addChild(mTitleWidget);

        mContainerWidget.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.LEFT_TOP, PIN_VERTICAL_GAP));
        addChild(mContainerWidget);
        //getActions().addAction(ActionFactory.createCycleObjectSceneFocusAction());
    }


    protected void notifyAdded() {
        super.notifyAdded();

        notifyStateChanged(ObjectState.createNormal(), ObjectState.createNormal());
    }

    public Rectangle getEntireBounds() {
        Dimension d = getBounds().getSize();
        return new Rectangle(getLocation(), new Dimension(d.width, d.height + TRAILING_VERTICAL_GAP));
    }

    /**
     * Sets all node properties at once.
     */
    public void setNodeProperties(String nodeName, String nodeType) {
        boolean hasNodeName = nodeName != null && nodeName.length() >= 0;
        boolean hasNodeType = nodeType != null && nodeType.length() > 0;
        if (hasNodeType && hasNodeName) {
            mTitleWidget.setLabel("(" + nodeType + ") " + nodeName); // NOI18N
        } else if (hasNodeType) {
            mTitleWidget.setLabel("(" + nodeType + ")"); // NOI18N
        } else if (hasNodeName) {
            mTitleWidget.setLabel(nodeName); // NOI18N
        }
        readjustBounds();
    }

    public void stateChanged() {
        setMinimized(mStateModel.getBooleanState());
    }

    public void setMinimized(boolean isMinimized) {
        for (Widget child : mContainerWidget.getChildren()) {
            if (child instanceof CasaMinimizable) {
                ((CasaMinimizable) child).setMinimized(isMinimized);
            }
        }
        mContainerWidget.setPreferredBounds(isMinimized ? mTitleWidget.getPreferredBounds() : null);
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
        readjustBounds();
    }

    public void setTitleColor(Color color) {
        mTitleWidget.setTitleColor(color);
    }


    public void initializeGlassLayer(LayerWidget layer) {
    }

    protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
        super.notifyStateChanged(previousState, state);
        if ((previousState.isSelected() != state.isSelected()) ||
            (previousState.isFocused() != state.isFocused())){
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
        mContainerWidget.setPreferredBounds(null);
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
        if (mContainerWidget.getChildren().size() <= 1) {
            //Add cushoningWidget only once!
            Widget cushioningWidget = new Widget(getScene()); //mTitlewidget is already present and thus checking the size with "1"
            cushioningWidget.setPreferredBounds(new Rectangle(0, PIN_VERTICAL_GAP));
            mContainerWidget.addChild(cushioningWidget);
        }
        int insertionIndex = 1; //TitleWidget is already there
        boolean isProvides = false;
        if (widget instanceof CasaPinWidgetEngineProvides) {
            isProvides = true;
        }
        for (Widget child : mContainerWidget.getChildren()) {
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
        mContainerWidget.addChild(insertionIndex, widget);
    }

    /**
     * Creates an extended pin anchor with an ability of reconnecting to the node anchor when the node is minimized.
     * @param anchor the original pin anchor from which the extended anchor is created
     * @return the extended pin anchor
     */
    public Anchor createAnchorPin(Anchor pinAnchor) {
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

    public boolean getConfigurationStatus() {
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
        for (Widget child : mContainerWidget.getChildren()) {
            if (child instanceof CasaPinWidgetEngine) {
                ((CasaPinWidgetEngine) child).setLabelFont(font);
            }
        }
    }

    public void setPinColor(Color color) {
        for (Widget child : mContainerWidget.getChildren()) {
            if (child instanceof CasaPinWidgetEngine) {
                ((CasaPinWidgetEngine) child).setLabelColor(color);
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
