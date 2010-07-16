/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.compapp.casaeditor.graph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.*;
import javax.swing.border.LineBorder;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.model.StateModel;
import org.netbeans.modules.compapp.casaeditor.design.CasaModelGraphScene;
import org.netbeans.modules.compapp.casaeditor.graph.actions.AcceptProviderEngineNode;
import org.netbeans.modules.compapp.casaeditor.graph.awt.BorderedRectangularPainter;
import org.netbeans.modules.compapp.casaeditor.graph.awt.BorderedRectangularProvider;
import org.netbeans.modules.compapp.casaeditor.graph.awt.InnerGlowBorderDrawer;
import org.netbeans.modules.compapp.casaeditor.graph.awt.Painter;
import org.netbeans.modules.compapp.casaeditor.graph.awt.PainterWidget;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;

/**
 *
 * @author rdara
 * @author jqian
 */
public abstract class CasaNodeWidgetEngine extends CasaNodeWidget
        implements StateModel.Listener, CasaMinimizable {

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
    /** Whether the connections involving this SE SU widget are hidden. */
    private boolean mIsConnectionHidden;
    private static final boolean DEBUG = false;
    private static Color CONNECTION_HIDDEN_INDICATION_COLOR =
            new Color(128, 128, 128, 128); // make it customizable

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
                    g.setColor(provider.getBorderColor());
                    g.drawLine(rect.x, rect.y + rect.height, rect.x + rect.width, rect.y + rect.height);
                }
                if (isHighlighted()) {
                    InnerGlowBorderDrawer.paintInnerGlowBorder(getGraphics(), provider.getClipRect(), CasaFactory.getCasaCustomizer().getCOLOR_SELECTION(), 0.6f, 10);
                }

                if (isConnectionHidden()) {
                    g.setColor(CONNECTION_HIDDEN_INDICATION_COLOR);
                    Rectangle containerRect = mContainerWidget.getBounds();
                    g.fillRect(containerRect.x, containerRect.y, containerRect.width, containerRect.height);
                }
            }
        };

        mContainerWidget = new PainterWidget(scene, new BorderedRectangularPainter(provider, customWidgetPainter));
        mContainerWidget.setOpaque(false);
        mContainerWidget.addChild(mTitleWidget);

        mContainerWidget.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.LEFT_TOP, PIN_VERTICAL_GAP));
        addChild(mContainerWidget);
        //getActions().addAction(ActionFactory.createCycleObjectSceneFocusAction());

        if (DEBUG) {
            setBorder(new LineBorder(Color.green));
        }
    }

    @Override
    protected int getErrorBadgeDeltaX() {
        return getBounds().width - 15;
    }

    @Override
    protected void notifyAdded() {
        super.notifyAdded();

        notifyStateChanged(ObjectState.createNormal(), ObjectState.createNormal());
    }

    @Override
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

//    /**
//     * Sets all node properties at once.
//     * 
//     * @param unitName  service unit name
//     * @param compName  component name, e.x., sun-bpel-engine
//     */
//    public void setNodeProperties(String unitName, String compName) {
//        mTitleWidget.setLabel(unitName);  
//        mTitleWidget.setComponentName(compName);
//        readjustBounds();
//    }
    public void stateChanged() {
        setMinimized(mStateModel.getBooleanState());
    }

    public void updatePinImage() {
        for (Widget child : mContainerWidget.getChildren()) {
            if (child instanceof CasaPinWidget) {
                ((CasaPinWidget) child).updatePinImage();
            }
        }
    }

    public void setMinimized(boolean isMinimized) {
        ObjectScene scene = (ObjectScene) getScene();

        for (Widget child : mContainerWidget.getChildren()) {
            if (child instanceof CasaMinimizable) {
                ((CasaMinimizable) child).setMinimized(isMinimized);
            }

            if (child instanceof CasaPinWidget) {
                CasaPinWidget pinWidget = (CasaPinWidget) child;
                for (CasaComponent connection : pinWidget.getConnections()) {
                    CasaConnectionWidget connectionWidget =
                            (CasaConnectionWidget) scene.findWidget(connection);
                    if (connectionWidget instanceof CasaMinimizable) {
                        ((CasaMinimizable) connectionWidget).setMinimized(isMinimized);
                    }
                }
            }
        }

        mContainerWidget.setPreferredBounds(isMinimized ? mTitleWidget.getPreferredBounds() : null);
        getScene().validate();
    }

    public boolean isMinimized() {
        return mStateModel.getBooleanState();
    }

    public void setTitleFont(Font font) {
        mTitleWidget.setTitleFont(font);
        readjustBounds();
    }

    public void setTitleColor(Color color) {
        mTitleWidget.setTitleColor(color);
    }

    @Override
    public void initializeGlassLayer(LayerWidget layer) {
    }

    @Override
    protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
        super.notifyStateChanged(previousState, state);
        if ((previousState.isSelected() != state.isSelected()) ||
                (previousState.isFocused() != state.isFocused())) {
            repaint();
        }
    }

    /**
     * Attaches a pin widget to the node widget.
     * @param widget the pin widget
     */
    public void attachPinWidget(CasaPinWidget widget) {
        mContainerWidget.addChild(widget);
        setPinFont(getPinFont());
        setPinColor(getPinColor());
        mContainerWidget.setPreferredBounds(null);
    }

    public void attachProcessWidget(CasaProcessTitleWidget widget) {
        mContainerWidget.addChild(widget);
        mContainerWidget.setPreferredBounds(null);
    }

    public void doneAddingWidget() {
        //Add a cushoning widget only if there are other widgets than the title.
        if (mContainerWidget.getChildren().size() > 1) {
            Widget cushioningWidget = new Widget(getScene());
            cushioningWidget.setPreferredBounds(new Rectangle(2, PIN_VERTICAL_GAP));
            mContainerWidget.addChild(cushioningWidget);
            if (DEBUG) {
                cushioningWidget.setBorder(new LineBorder(Color.BLACK));
            }
        }
    }

    /**
     * Creates an extended pin anchor with an ability of reconnecting to the 
     * node anchor when the node is minimized.
     * 
     * @param anchor the original pin anchor from which the extended anchor 
     *               is created
     * 
     * @return the extended pin anchor
     */
    public Anchor createAnchorPin(Anchor pinAnchor) {
        return AnchorFactory.createProxyAnchor(mStateModel, pinAnchor, mNodeAnchor);
    }

    /**
     * If no pin anchor is found, return the node anchor.
     */
    @Override
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

    @Override
    public void setEditable(boolean bValue) {
        super.setEditable(bValue);
        mTitleWidget.setEditable(bValue);
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

    public void setConnectionHidden(boolean isConnectionHidden) {
        if (mIsConnectionHidden != isConnectionHidden) {
            mIsConnectionHidden = isConnectionHidden;
            repaint();
        }
    }

    public boolean isConnectionHidden() {
        return mIsConnectionHidden;
    }    
    
    protected abstract Color getBackgroundColor();

    protected abstract Color getPinHolderBorderColor();

    protected abstract Color getPinHolderTitleColor();

    protected abstract Color getPinHolderBackgroundColor();
    
    protected abstract Font getPinFont();

    protected abstract Color getPinColor();


    /** 
     * Internal Service Engine Service Unit Widget.
     */
    public static class Internal extends CasaNodeWidgetEngine {

        public Internal(Scene scene) {
            this(scene, true);
        }

        public Internal(Scene scene, boolean confStatus) {
            super(scene);
            setConfigurationStatus(confStatus);
            setTitleFont(CasaFactory.getCasaCustomizer().getFONT_SU_HEADER());
            setTitleColor(CasaFactory.getCasaCustomizer().getCOLOR_SU_INTERNAL_TITLE());
        }

        @Override
        protected Color getBackgroundColor() {
            return CasaFactory.getCasaCustomizer().getCOLOR_REGION_ENGINE();
        }

        @Override
        protected Color getPinHolderBorderColor() {
            return CasaFactory.getCasaCustomizer().getCOLOR_SU_INTERNAL_BORDER();
        }

        @Override
        protected Color getPinHolderTitleColor() {
            return CasaFactory.getCasaCustomizer().getCOLOR_SU_INTERNAL_TITLE();
        }

        @Override
        protected Color getPinHolderBackgroundColor() {
            return CasaFactory.getCasaCustomizer().getCOLOR_SU_INTERNAL_BACKGROUND();
        }

        @Override
        public Font getPinFont() {
            return CasaFactory.getCasaCustomizer().getFONT_SU_PIN();
        }

        @Override
        public Color getPinColor() {
            return CasaFactory.getCasaCustomizer().getCOLOR_SU_INTERNAL_PIN();
        }
    }

    /**
     * External Service Engine Service Unit Widget.
     */
    public static class External extends CasaNodeWidgetEngine {

        public External(Scene scene) {
            this(scene, true);
        }

        public External(Scene scene, boolean confStatus) {
            super(scene);
            getActions().addAction(CasaFactory.createAcceptAction(new AcceptProviderEngineNode((CasaModelGraphScene) scene)));
            setConfigurationStatus(confStatus);
            setTitleFont(CasaFactory.getCasaCustomizer().getFONT_EXT_SU_HEADER());
            setTitleColor(CasaFactory.getCasaCustomizer().getCOLOR_SU_EXTERNAL_TITLE());
        }

        @Override
        protected Color getBackgroundColor() {
            return Color.WHITE;
        }

        @Override
        protected Color getPinHolderBorderColor() {
            return CasaFactory.getCasaCustomizer().getCOLOR_SU_EXTERNAL_BORDER();
        }

        @Override
        protected Color getPinHolderTitleColor() {
            return CasaFactory.getCasaCustomizer().getCOLOR_SU_EXTERNAL_TITLE();
        }

        @Override
        protected Color getPinHolderBackgroundColor() {
            return CasaFactory.getCasaCustomizer().getCOLOR_SU_EXTERNAL_BACKGROUND();
        }

        @Override
        public Font getPinFont() {
            return CasaFactory.getCasaCustomizer().getFONT_EXT_SU_PIN();
        }

        @Override
        public Color getPinColor() {
            return CasaFactory.getCasaCustomizer().getCOLOR_SU_EXTERNAL_PIN();
        }
    }
}
