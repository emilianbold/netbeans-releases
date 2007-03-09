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
import java.awt.image.BufferedImage;
import javax.swing.border.Border;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.model.ObjectState;
import org.openide.util.NbBundle;

/**
 *
 * @author Ramesh Dara
 */
public class CasaNodeWidgetBinding extends CasaNodeWidget {
    
    private static final int VERT_TEXT_BAR_WIDTH      = 20;
    private static final int VERT_TEXT_BAR_MIN_HEIGHT = 40;
    private static final int VERT_TEXT_BAR_MAX_CHAR   =  6;
    private static final int VERT_TEXT_BAR_SPACING    =  8;
    
    private static final int PIN_X_START              =  4;
    private static final int PIN_Y_CONSUMES_START     =  4;
    private static final int PIN_Y_PROVIDES_START     = 26;
    
    private static final int NAME_LEFT_EDGE_SPACING   =  0;
    
    private static final int BORDER_WIDTH             =  2;
    
    private ImageWidget mImageWidget;
    private Widget mIconsWidget;
    
    private LabelWidget mNameWidget;
    private Widget mTopWidgetHolder;
    private Widget mPinsHolderWidget;
    private String mVertTextBarText;
    
    private ImageWidget mEditWidget;
    private ImageWidget mWSPolicyWidget; 

    // Used for determining when we need to regenerate the vertical text image.
    // It must be regenerated any time we alter the node's height.
    private int mPreviousVertTextBarHeight;
    private Widget mHeaderHolder;

    
    public CasaNodeWidgetBinding(Scene scene) {
        super(scene);
        setOpaque(true);
        setBackground(CasaFactory.getCasaCustomizer().getCOLOR_BC_BACKGROUND());
        setLayout(LayoutFactory.createVerticalLayout());
        
        mEditWidget = new ImageWidget(scene);
        mWSPolicyWidget = new ImageWidget(scene);
        Widget emptyWidget = new Widget(scene);
        emptyWidget.setPreferredBounds(new Rectangle(16,0));
        
        mIconsWidget = new Widget(scene);
        mIconsWidget.setOpaque(false);
        mIconsWidget.setLayout(
                LayoutFactory.createVerticalLayout(LayoutFactory.SerialAlignment.LEFT_TOP, 1));
        mIconsWidget.addChild(emptyWidget);
        mIconsWidget.addChild(mEditWidget);
        mIconsWidget.addChild(mWSPolicyWidget);
        
        mImageWidget = new ImageWidget(scene);
        
        mPinsHolderWidget = new Widget(scene);
        mPinsHolderWidget.setLayout(new BindingPinsLayout());
        
        mImageWidget.setMinimumSize(new Dimension(VERT_TEXT_BAR_WIDTH, 0));
        mPinsHolderWidget.setMinimumSize(new Dimension(44, VERT_TEXT_BAR_MIN_HEIGHT));
        
        mTopWidgetHolder = new Widget(scene);
        mTopWidgetHolder.setOpaque(true);
        mTopWidgetHolder.setLayout(LayoutFactory.createHorizontalLayout(
                LayoutFactory.SerialAlignment.LEFT_TOP, 0));
        mTopWidgetHolder.setBackground(CasaFactory.getCasaCustomizer().getCOLOR_BC_BACKGROUND());
        
        regenerateHeaderBorder();
        
        mHeaderHolder = new Widget(scene);
        mHeaderHolder.setOpaque(true);
        
        mHeaderHolder.setBackground(CasaFactory.getCasaCustomizer().getCOLOR_BC_TITLE_BACKGROUND());
        mHeaderHolder.setLayout(LayoutFactory.createHorizontalLayout(
                LayoutFactory.SerialAlignment.LEFT_TOP, 0));
        mHeaderHolder.addChild(mIconsWidget);
        mHeaderHolder.addChild(mImageWidget);
        mTopWidgetHolder.addChild(mHeaderHolder);

        mTopWidgetHolder.addChild(mPinsHolderWidget);
        
        mHeader = new Widget(scene);
        mHeader.setBackground(CasaFactory.getCasaCustomizer().getCOLOR_BC_BACKGROUND());
        mHeader.setLayout(LayoutFactory.createVerticalLayout(
                LayoutFactory.SerialAlignment.CENTER, 0));
        
        mHeader.addChild(mTopWidgetHolder);
        
        addChild(mHeader);
        
        notifyStateChanged(ObjectState.createNormal(), ObjectState.createNormal());

        Widget.Dependency verticalTextizer = new Widget.Dependency() {
            public void revalidateDependency() {
                if (
                        getScene().getGraphics() == null || 
                        getBounds() == null ||
                        getParentWidget() == null) {
                    return;
                }
                // Maintain the height of the vertical text bar.
                if (mTopWidgetHolder.getClientArea().getBounds().height != mPreviousVertTextBarHeight) {
                    regenerateVerticalTextBarImage();
                    mPreviousVertTextBarHeight = mTopWidgetHolder.getClientArea().getBounds().height;
                }
            }
        };
        addDependency(verticalTextizer);
        mDependencies.add(verticalTextizer);
    }
    
    
    public void initializeGlassLayer(LayerWidget layer) {
        mNameWidget = new LabelWidget(getScene());
        mNameWidget.setFont(CasaFactory.getCasaCustomizer().getFONT_BC_LABEL());
        mNameWidget.setForeground(CasaFactory.getCasaCustomizer().getCOLOR_BC_LABEL());
        layer.addChild(mNameWidget);
        
        // Update the name label location if the widget moves.
        Widget.Dependency nameLabeler = new Widget.Dependency() {
            public void revalidateDependency() {
                if (
                        getScene().getGraphics() == null ||
                        getBounds() == null ||
                        getParentWidget() == null) {
                    return;
                }
                Rectangle nameRect = mNameWidget.getClientArea();
                Rectangle myRect = getBounds();
                int newX = (myRect.width - nameRect.width) / 2;
                Point point = convertPreferredLocalToScene(new Point(
                        newX,
                        myRect.height + nameRect.height));
                point.x = point.x < NAME_LEFT_EDGE_SPACING ?
                    NAME_LEFT_EDGE_SPACING :
                    point.x;
                mNameWidget.setPreferredLocation(point);
            }
        };
        mDependencies.add(nameLabeler);
        addDependency(nameLabeler);
    }
    
    public void setLabelFont(Font font) {
        mNameWidget.setFont(font);
        mNameWidget.revalidate();
        revalidate();
    }
    
    public void setLabelColor(Color color) {
        mNameWidget.setForeground(color);
    }

    public void removeAllDependencies() {
        super.removeAllDependencies();
        mNameWidget.removeFromParent();
    }
    
    protected Rectangle getNameWidgetBounds() {
        return mNameWidget.getBounds();
    }
    
    protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
        super.notifyStateChanged(previousState, state);
        if (!previousState.isSelected() && state.isSelected()) {
            regenerateHeaderBorder();
        } else if (previousState.isSelected() && !state.isSelected()) {
            regenerateHeaderBorder();
        }
    }
    
    /**
     * Attaches a pin widget to the node widget.
     * @param widget the pin widget
     */
    public void attachPinWidget(CasaPinWidget widget) {
        mPinsHolderWidget.addChild(widget);
    }
    
    /**
     * Returns an anchor for the given pin anchor.
     * Simply returns the same anchor passed-in.
     * @param anchor the original pin anchor
     * @return the extended pin anchor
     */
    protected Anchor createAnchorPin (Anchor pinAnchor) {
        return pinAnchor;
    }
    
    // Our getBounds() may not yet accurately reflect the actual
    // height of our component, so we calculate it here in case we need it now.
    public Rectangle getEntireBounds() {
        Rectangle bounds = new Rectangle(
                Math.min(getLocation().x, mNameWidget.getLocation().x),
                getLocation().y,
                Math.max(getBounds().width, mNameWidget.getBounds().width),
                getBounds().height + mNameWidget.getBounds().height);

        int bodyHeight = VERT_TEXT_BAR_MIN_HEIGHT;
        bodyHeight = Math.max(bodyHeight, mImageWidget.getPreferredBounds().height);
        if (mPinsHolderWidget.getChildren().size() == 2) {
            CasaPinWidgetBinding bindingPinChild = (CasaPinWidgetBinding) 
                mPinsHolderWidget.getChildren().get(0);
            bodyHeight = Math.max(bodyHeight, bindingPinChild.getPinWidgetBounds().height * 2);
        }
        bounds.height = 
                bodyHeight + 
                BORDER_WIDTH * 2 + 
                getNameWidgetBounds().height;
        return bounds;
    }

    public void setEndpointLabel(String nodeName) {
        mNameWidget.setToolTipText(nodeName);
        mNameWidget.setLabel(nodeName);
        // validate to trigger a bounds update
        getScene().validate();
        // once the bounds is updated, reposition the label
        invokeDependencies();
    }
    
    /**
     * Sets all node properties at once.
     * @param image the node image
     * @param nodeName the node name
     * @param nodeType the node type (secondary name)
     * @param glyphs the node glyphs
     */
    public void setNodeProperties(String nodeName, String nodeType) {
        mNameWidget.setToolTipText(nodeName);
        mNameWidget.setLabel(nodeName);
        mVertTextBarText = nodeType;
        revalidate();
        getScene().revalidate();
    }
    
    public void regenerateHeaderBorder() {
        BorderDefinition definition = null;
        if (getState().isSelected()) {
            definition = BorderDefinition.createSelectedDefinition();
            mTopWidgetHolder.setBorder(BorderFactory.createSwingBorder(
                    getScene(), 
                    definition.getBorder()));
            mImageWidget.setBorder(BorderFactory.createSwingBorder(
                    getScene(), 
                    javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 1, definition.getBorderColor())));
        } else {
            definition = BorderDefinition.createDefaultDefinition();
            mTopWidgetHolder.setBorder(BorderFactory.createSwingBorder(
                    getScene(), 
                    definition.getBorder()));
            mImageWidget.setBorder(BorderFactory.createSwingBorder(
                    getScene(), 
                    javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 1, definition.getBorderColor())));
        }
    }
    
    public void regenerateVerticalTextBarImage() {
        mImageWidget.setImage(getVerticalTextBarImage());
    }
    
    public BufferedImage getVerticalTextBarImage() {
        
        String displayedText = mVertTextBarText;
        if (mVertTextBarText.length() > VERT_TEXT_BAR_MAX_CHAR) {
            displayedText = displayedText.substring(0, VERT_TEXT_BAR_MAX_CHAR) + NbBundle.getMessage(getClass(), "ELLIPSIS");
        }
        
        Graphics2D sceneGraphics = getScene().getGraphics();
        sceneGraphics.setFont(CasaFactory.getCasaCustomizer().getFONT_BC_HEADER());
        FontMetrics fm = sceneGraphics.getFontMetrics(CasaFactory.getCasaCustomizer().getFONT_BC_HEADER());
        int fontLength = fm.stringWidth(displayedText);

        int barHeight = fontLength < VERT_TEXT_BAR_MIN_HEIGHT ? VERT_TEXT_BAR_MIN_HEIGHT : fontLength;
        barHeight += VERT_TEXT_BAR_SPACING;
        
        // This image must support the alpha channel, which provides transparency.
        BufferedImage image = new BufferedImage(
                VERT_TEXT_BAR_WIDTH, 
                barHeight, 
                BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D graphics = image.createGraphics();
        
        // The RGB values don't matter, as this color is 100% transparent.
        graphics.setColor(new Color(255, 255, 255, 0));
        
        graphics.fill(new Rectangle(0, 0, VERT_TEXT_BAR_WIDTH, barHeight));
        graphics.setFont(CasaFactory.getCasaCustomizer().getFONT_BC_HEADER());
        graphics.setPaint(CasaFactory.getCasaCustomizer().getCOLOR_BC_TITLE());
        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        int characterHeight = fm.getAscent() + fm.getDescent();
        int textOffset = textOffset = (barHeight - fontLength) / 2;
        
        graphics.translate(characterHeight, textOffset + fontLength);
        graphics.rotate(-Math.toRadians(90.0));
        graphics.drawString(displayedText, 0, -fm.getDescent());
        graphics.rotate(Math.toRadians(90.0));
        graphics.translate(-characterHeight, -(textOffset + fontLength));
        
        graphics.dispose();
        
        Rectangle iconsRect = mIconsWidget.getPreferredBounds();
        iconsRect.height = barHeight;
        mIconsWidget.setPreferredBounds(iconsRect);

        return image;
    }

    public void setEditable(boolean bValue) {
        super.setEditable(bValue);
        mEditWidget.setImage(bValue ? RegionUtilities.IMAGE_EDIT_16_ICON : null);
    }

    public void setWSPolicyAttached(boolean bValue) {
        super.setWSPolicyAttached(bValue);
        mWSPolicyWidget.setImage(bValue ? RegionUtilities.IMAGE_WS_POLICY_16_ICON : null);
    }

    public void setBackgroundColor(Color color) {
        setBackground(color);
        mTopWidgetHolder.setBackground(CasaFactory.getCasaCustomizer().getCOLOR_BC_BACKGROUND());
        mHeader.setBackground(CasaFactory.getCasaCustomizer().getCOLOR_BC_BACKGROUND());
    }
    
    public void setTitleBackgroundColor(Color color) {
        mHeaderHolder.setBackground(color);
        mHeaderHolder.repaint();
    }
    
    
        
    private static class BindingPinsLayout implements Layout {
        public void layout(Widget widget) {
            CasaPinWidgetBindingConsumes consumesPin = null;
            CasaPinWidgetBindingProvides providesPin = null;
            for (Widget child : widget.getChildren()) {
                if        (child instanceof CasaPinWidgetBindingConsumes) {
                    consumesPin = (CasaPinWidgetBindingConsumes) child;
                } else if (child instanceof CasaPinWidgetBindingProvides) {
                    providesPin = (CasaPinWidgetBindingProvides) child;
                }
            }
            if (consumesPin != null) {
                // The Consumes pin is on the top.
                consumesPin.resolveBounds(new Point(PIN_X_START, PIN_Y_CONSUMES_START), null);
            }
            if (providesPin != null) {
                // The Provides pin is on the bottom.
                providesPin.resolveBounds(new Point(PIN_X_START, PIN_Y_PROVIDES_START), null);
            }
        }
        public boolean requiresJustification(Widget widget) {
            return false;
        }
        public void justify(Widget widget) {}
    }
    
    
    private static class BorderDefinition {
        private Border mBorder;
        private Color mBorderColor;
        private BorderDefinition(Color borderColor) {
            mBorderColor = borderColor;
            mBorder = javax.swing.BorderFactory.createMatteBorder(
                    BORDER_WIDTH,
                    BORDER_WIDTH,
                    BORDER_WIDTH,
                    0,
                    borderColor);
        }
        public Border getBorder() {
            return mBorder;
        }
        public Color getBorderColor() {
            return mBorderColor;
        }
        public static BorderDefinition createDefaultDefinition() {
            return new BorderDefinition(
                CasaFactory.getCasaCustomizer().getCOLOR_BC_BORDER());
        }
        public static BorderDefinition createSelectedDefinition() {
            return new BorderDefinition(
                CasaFactory.getCasaCustomizer().getCOLOR_SELECTION());
        }
    }
}
