/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.swing.tabcontrol.plaf;

import org.netbeans.swing.tabcontrol.TabDisplayer;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;


import org.openide.awt.HtmlRenderer;

/**
 * User interface of view type tabs designed to be consistent with Swing metal
 * look and feel.
 *
 * @author Dafe Simonek
 */
public final class MetalViewTabDisplayerUI extends AbstractViewTabDisplayerUI {

    /**
     * *********** constants ******************
     */
    private static final int TXT_X_PAD = 5;

    private static final int ICON_X_LEFT_PAD = 5;
    private static final int ICON_X_RIGHT_PAD = 2;

    private static final int BUMP_X_PAD = 5;
    private static final int BUMP_Y_PAD = 4;

    /**
     * ****** static fields **********
     */

    private static IconLoader closeIcon;

    private static Color inactBgColor, actBgColor, borderHighlight, borderShadow;

    /**
     * ******* instance fields *********
     */

    private Dimension prefSize;

    /**
     * Reusable Rectangle to optimize rectangle creation/garbage collection
     * during paints
     */
    private Rectangle tempRect = new Rectangle();

    /**
     * Should be constructed only from createUI method.
     */
    private MetalViewTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
        prefSize = new Dimension(100, 19);
    }

    public static ComponentUI createUI(JComponent c) {
        return new MetalViewTabDisplayerUI((TabDisplayer) c);
    }

    public void installUI(JComponent c) {
        super.installUI(c);
        if (closeIcon == null) {
            closeIcon = new IconLoader();
        }
    }

    protected AbstractViewTabDisplayerUI.Controller createController() {
        return new OwnController();
    }

    public Dimension getPreferredSize(JComponent c) {
        FontMetrics fm = getTxtFontMetrics();
        int height = fm == null ?
                21 : fm.getAscent() + 2 * fm.getDescent() + 4;
        Insets insets = c.getInsets();
        prefSize.height = height + insets.bottom + insets.top;
        return prefSize;
    }

    /**
     * Overrides basic paint mathod, adds painting of overall blue or gray
     * bottom area, depending on activation status value
     */
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        paintBottomBorder(g, c);
    }

    /**
     * Paints bottom "activation" line
     */
    private void paintBottomBorder(Graphics g, JComponent c) {
        Color color = isActive() ? getActBgColor() : getInactBgColor();
        g.setColor(color);
        Rectangle bounds = c.getBounds();
        g.fillRect(1, bounds.height - 3, bounds.width - 1, 2);
        g.setColor(getBorderShadow());
        g.drawLine(1, bounds.height - 1, bounds.width - 1, bounds.height - 1);
    }

    protected void paintTabContent(Graphics g, int index, String text, int x,
                                   int y, int width, int height) {
        FontMetrics fm = getTxtFontMetrics();
        // setting font already here to compute string width correctly
        g.setFont(getTxtFont());
        int txtWidth = width;
        if (isSelected(index)) {
            PinButton pin = configurePinButton(index);
            boolean showPin = pin != null && pin.getOrientation() != TabDisplayer.ORIENTATION_INVISIBLE;
            int space4Pin = showPin ? pinButton.getWidth() + 1 : 0;
            int space4Icon = 0;
            if (displayer.isShowCloseButton()) {
                // selected one is trickier, paint text, bump and close icon
                // close icon has the biggest space priority, bump the smallest one
                String iconPath = findIconPath(index);
                Icon icon = closeIcon.obtainIcon(iconPath);
                int iconWidth = icon.getIconWidth();
                space4Icon = iconWidth + ICON_X_LEFT_PAD + ICON_X_RIGHT_PAD + space4Pin;
                txtWidth = width - 2 * TXT_X_PAD - space4Icon;
                getCloseIconRect(tempRect, index);
                icon.paintIcon(getDisplayer(), g, tempRect.x, tempRect.y);
            } else {
                tempRect.x = x + (width - 2);
                
                tempRect.y = !showPin ? 0 : ((displayer.getHeight() / 2) -
                    (pinButton.getPreferredSize().height / 2));
                int pinWidth = showPin ? 0 : pinButton.getPreferredSize().width;
                txtWidth = (width - 2 * TXT_X_PAD) - pinWidth;
                space4Icon = pinWidth + 5;
            }
            txtWidth = (int)HtmlRenderer.renderString(text, g, x + TXT_X_PAD, height - 
                    fm.getDescent() - 4, txtWidth, height, getTxtFont(),
                    UIManager.getColor("textText"),
                    HtmlRenderer.STYLE_TRUNCATE, true);
            int bumpWidth = width
                    - (TXT_X_PAD + txtWidth + BUMP_X_PAD + space4Icon);
            if (bumpWidth > 0) {
                paintBump(index, g, x + TXT_X_PAD + txtWidth + BUMP_X_PAD,
                          y + BUMP_Y_PAD, bumpWidth, height - 2 * BUMP_Y_PAD);
            }
            if (showPin) {
                // don't activate and draw pin button if tab is too narrow
                if (tempRect.x - space4Pin < x + TXT_X_PAD - 1) {
                    pinButton.setVisible(false);
                } else {
                    pinButton.setVisible(true);
                    pinButton.setLocation(tempRect.x - space4Pin, tempRect.y);
                }
            } else {
                pinButton.setVisible(false);
            }
        } else {
            txtWidth = width - 2 * TXT_X_PAD;
            HtmlRenderer.renderString(text, g, x + TXT_X_PAD, height - 
                fm.getDescent() - 4, txtWidth, height, getTxtFont(),
                UIManager.getColor("textText"),
                HtmlRenderer.STYLE_TRUNCATE, true);
        }
    }

    protected void paintTabBorder(Graphics g, int index, int x, int y,
                                  int width, int height) {
        Color highlight = getBorderHighlight();
        Color shadow = getBorderShadow();
        boolean isSelected = isSelected(index);

        boolean isFirst = index == 0;
        boolean isLast = index == getDataModel().size() - 1;

        g.translate(x, y);
        
        // paint darker lines
        g.setColor(shadow);
        if (!isFirst) {
            g.drawLine(0, 0, 0, height - 5);
        }
        if (!isSelected) {
            g.drawLine(1, height - 5, isLast ? width - 1 : width, height - 5);
        }
        // paint brighter lines
        g.setColor(highlight);
        g.drawLine(1, 0, width - 1, 0);
        if (isFirst) {
            g.drawLine(0, 0, 0, height - 2);
        }
        if (!isSelected) {
            g.drawLine(0, height - 4, isLast ? width - 1 : width, height - 4);
        }

        g.translate(-x, -y);
    }

    protected void paintTabBackground(Graphics g, int index, int x, int y,
                                      int width, int height) {
        boolean selected = isSelected(index);
        boolean highlighted = selected && isActive();
        boolean attention = isAttention (index);
        if (highlighted && !attention) {
            g.setColor(getActBgColor());
            g.fillRect(x, y, width, height - 3);
        } else if (attention) {
            g.setColor(MetalEditorTabCellRenderer.ATTENTION_COLOR);
            g.fillRect(x, y, width, height - 3);
        } else {
            g.setColor(getInactBgColor());
            g.fillRect(x, y, width, height - 3);
        }
    }

    private void paintBump(int index, Graphics g, int x, int y, int width,
                           int height) {
        ColorUtil.paintViewTabBump(g, x, y, width, height, isFocused(index) ?
                                                           ColorUtil.FOCUS_TYPE :
                                                           ColorUtil.UNSEL_TYPE);
    }
    
    static Color getInactBgColor() {
        if (inactBgColor == null) {
            inactBgColor = (Color) UIManager.get("inactiveCaption");
            if (inactBgColor == null) {
                inactBgColor = new Color(204, 204, 204);
            }
        }
        return inactBgColor;
    }

    static Color getActBgColor() {
        if (actBgColor == null) {
            actBgColor = (Color) UIManager.get("activeCaption");
            if (actBgColor == null) {
                actBgColor = new Color(204, 204, 255);
            }
        }
        return actBgColor;
    }

    private Color getBorderHighlight() {
        if (borderHighlight == null) {
            borderHighlight = getInactBgColor().brighter();
        }
        return borderHighlight;
    }

    private Color getBorderShadow() {
        if (borderShadow == null) {
            borderShadow = getInactBgColor().darker();
        }
        return borderShadow;
    }

    /**
     * Returns path of icon which is correct for currect state of tab at given
     * index
     */
    private String findIconPath(int index) {
        if (controller.isClosePressed() == index) {
            return "org/netbeans/swing/tabcontrol/resources/met-bigclose-pressed.gif";
        }
        return isFocused(index) ?
                "org/netbeans/swing/tabcontrol/resources/met-bigclose-focus.gif" :
                "org/netbeans/swing/tabcontrol/resources/met-bigclose-normal.gif";
    }

    /**
     * Computes rectangle occupied by close icon and fill values in given
     * rectangle.
     */
    private Rectangle getCloseIconRect(Rectangle rect, int index) {
        TabLayoutModel tlm = getLayoutModel();
        int x = tlm.getX(index);
        int w = tlm.getW(index);
        String iconPath = findIconPath(index);
        if (closeIcon == null) {
            //Tab control can be asked to process mouse motion events that
            //occured during startup - this causes an NPE here
            closeIcon = new IconLoader();
        }
        Icon icon = closeIcon.obtainIcon(iconPath);
        int iconWidth = icon.getIconWidth();
        int iconHeight = icon.getIconHeight();
        rect.x = x + w - iconWidth - ICON_X_RIGHT_PAD;
        rect.y = getCenteredIconY(icon, index);
        rect.width = iconWidth;
        rect.height = iconHeight;
        return rect;
    }

    public Controller getController() {
        return controller;
    }

    protected PinButton createPinButton() {
        Map normalIcons = new HashMap(6);
        normalIcons.put(TabDisplayer.ORIENTATION_EAST, "org/netbeans/swing/tabcontrol/resources/met-pin-normal-select-east.gif");
        normalIcons.put(TabDisplayer.ORIENTATION_WEST, "org/netbeans/swing/tabcontrol/resources/met-pin-normal-select-west.gif");
        normalIcons.put(TabDisplayer.ORIENTATION_SOUTH, "org/netbeans/swing/tabcontrol/resources/met-pin-normal-select-south.gif");
        normalIcons.put(TabDisplayer.ORIENTATION_CENTER, "org/netbeans/swing/tabcontrol/resources/met-pin-normal-select-center.gif");
        Map pressedIcons = new HashMap(6);
        pressedIcons.put(TabDisplayer.ORIENTATION_EAST, "org/netbeans/swing/tabcontrol/resources/met-pin-pressed-select-east.gif");
        pressedIcons.put(TabDisplayer.ORIENTATION_WEST, "org/netbeans/swing/tabcontrol/resources/met-pin-pressed-select-west.gif");
        pressedIcons.put(TabDisplayer.ORIENTATION_SOUTH, "org/netbeans/swing/tabcontrol/resources/met-pin-pressed-select-south.gif");
        pressedIcons.put(TabDisplayer.ORIENTATION_CENTER, "org/netbeans/swing/tabcontrol/resources/met-pin-pressed-select-center.gif");
        Map rolloverIcons = new HashMap(6);
        rolloverIcons.put(TabDisplayer.ORIENTATION_EAST, "org/netbeans/swing/tabcontrol/resources/met-pin-normal-focus-east.gif");
        rolloverIcons.put(TabDisplayer.ORIENTATION_WEST, "org/netbeans/swing/tabcontrol/resources/met-pin-normal-focus-west.gif");
        rolloverIcons.put(TabDisplayer.ORIENTATION_SOUTH, "org/netbeans/swing/tabcontrol/resources/met-pin-normal-focus-south.gif");
        rolloverIcons.put(TabDisplayer.ORIENTATION_CENTER, "org/netbeans/swing/tabcontrol/resources/met-pin-normal-focus-center.gif");
        return new PinButton(normalIcons, pressedIcons, rolloverIcons);
    }
    
    /**
     * Own close icon button controller
     */
    private class OwnController extends Controller {
        //TODO - add a method to AbstractViewTabDisplayerUI to get the close button rect and implement everything
        //on the parent class

        protected int inCloseIconRect(Point point) {
            if (!displayer.isShowCloseButton()) {
                return -1;
            }
            int index = getLayoutModel().indexOfPoint(point.x, point.y);
            if (index < 0) {
                return -1;
            }
            return getCloseIconRect(tempRect, index).contains(point) ?
                    index : -1;
        }
        
        protected boolean inPinButtonRect(Point p) {
            if (!pinButton.isVisible()) {
                return false;
            }
            Point p2 = SwingUtilities.convertPoint(displayer, p, pinButton);
            return pinButton.contains(p2);
        }
        

    } // end of OwnController

}
