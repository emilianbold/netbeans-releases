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
import java.awt.event.MouseEvent;
import java.util.Collections;

import java.util.HashMap;
import java.util.Map;


import org.openide.awt.HtmlRenderer;

/**
 * Win XP-like user interface of view type tabs.
 *
 * @author Dafe Simonek
 */
public final class WinXPViewTabDisplayerUI extends AbstractViewTabDisplayerUI {

    /*********** constants *************/
    
    /**
     * Space between text and left side of the tab
     */
    private static final int TXT_X_PAD = 8;
    private static final int TXT_Y_PAD = 3;

    private static final int ICON_X_PAD = 2;
    private static final int ICON_Y_PAD = 7;

    private static final int BUMP_X_PAD = 3;
    private static final int BUMP_Y_PAD_UPPER = 5;
    private static final int BUMP_Y_PAD_BOTTOM = 3;

    private static final int HIGHLIGHTED_RAISE = 1;

    /*********** static fields **********/
    
    /**
     * True when colors were already initialized, false otherwise
     */
    private static boolean colorsReady = false;

    private static Color unselFillBrightC, unselFillDarkC, selFillC, focusFillBrightC, focusFillDarkC, txtC, borderC, bottomBorderC, selBorderC, bgFillC;

    private static IconLoader closeIcon;

    /**
     * ******** instance fields ********
     */

    private Dimension prefSize;

    /**
     * rectangle instance used to speedup recurring computations in painting
     * methods
     */
    private Rectangle tempRect = new Rectangle();

    /**
     * Should be constructed only from createUI method.
     */
    private WinXPViewTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
        prefSize = new Dimension(100, 17);
    }

    public static ComponentUI createUI(JComponent c) {
        return new WinXPViewTabDisplayerUI((TabDisplayer)c);
    }
     
    public void installUI (JComponent c) {
        super.installUI(c);
        initColors();
        c.setBackground(UIManager.getColor("nb_workplace_fill")); //NOI18N
        c.setOpaque(true);
    }

    protected AbstractViewTabDisplayerUI.Controller createController() {
        return new OwnController();
    }

    public Dimension getPreferredSize(JComponent c) {
        FontMetrics fm = getTxtFontMetrics();
        int height = fm == null ?
                17 : fm.getAscent() + 2 * fm.getDescent() + 3;
        Insets insets = c.getInsets();
        prefSize.height = height + insets.bottom + insets.top;
        return prefSize;
    }

    protected void paintTabContent(Graphics g, int index, String text, int x,
                                   int y, int width, int height) {
        FontMetrics fm = getTxtFontMetrics();
        // setting font already here to compute string width correctly
        g.setFont(getTxtFont());
        // highlighted one is higher then others
        if (!isTabInFront(index) && isMoreThanOne()) {
            y += HIGHLIGHTED_RAISE;
            height -= HIGHLIGHTED_RAISE;
        }
        int txtWidth = width;
        if (isSelected(index)) {
            // paint text, dragger and close icon
            // close icon has the biggest space priority, text the smallest one
            PinButton pin = configurePinButton(index);
            boolean showPin = pin != null && pin.getOrientation() != TabDisplayer.ORIENTATION_INVISIBLE;
            int space4pin = showPin ? pinButton.getWidth() + 1 : 0;
            if (displayer.isShowCloseButton()) {
                if (closeIcon == null) {
                    closeIcon = new IconLoader();
                }
                String iconPath = findIconPath(index);
                Icon icon = closeIcon.obtainIcon(iconPath);
                int iconWidth = icon.getIconWidth();
                int space4Icon = iconWidth + 2 * ICON_X_PAD + space4pin;
                txtWidth = width - 2 * TXT_X_PAD - space4Icon;
                getCloseIconRect(tempRect, index);
                icon.paintIcon(getDisplayer(), g, tempRect.x, tempRect.y);
            } else {
                txtWidth = width - 2 * TXT_X_PAD - space4pin;
                tempRect.x = x + (width - 2);
                tempRect.y = !showPin ? 0 : ((displayer.getHeight() / 2) -
                    (pinButton.getPreferredSize().height / 2));
                
            }
            
            if (showPin) {
                // don't activate and draw pin button if tab is too narrow
                if (tempRect.x - space4pin < x + TXT_X_PAD - 1) {
                    pinButton.setVisible(false);
                } else {
                    pinButton.setVisible(true);
                    pinButton.setLocation(tempRect.x - space4pin, tempRect.y);
                }
            } else {
                pinButton.setVisible(false);
            }
        } else {
            txtWidth = width - 2 * TXT_X_PAD;
        }
        // draw bump (dragger)
        ColorUtil.paintXpTabDragTexture(getDisplayer(), g, x + BUMP_X_PAD, y
                 + BUMP_Y_PAD_UPPER, height - (BUMP_Y_PAD_UPPER
                 + BUMP_Y_PAD_BOTTOM));
        HtmlRenderer.renderString(text, g, x + TXT_X_PAD, y + fm.getAscent()
                + TXT_Y_PAD, txtWidth, height, getTxtFont(),
                txtC,
                HtmlRenderer.STYLE_TRUNCATE, true);
    }

    protected void paintTabBorder(Graphics g, int index, int x, int y,
                                  int width, int height) {
        boolean isFirst = index == 0;
        boolean isHighlighted = isTabHighlighted(index);

        g.translate(x, y);

        ColorUtil.paintXpTabHeader(isHighlighted ?
                                   ColorUtil.XP_HIGHLIGHTED_TAB :
                                   ColorUtil.XP_REGULAR_TAB, g, 0,
                                   0, width); 
        Color borderColor = isHighlighted ? selBorderC : borderC;
        g.setColor(borderColor);
        if (isFirst) {
            g.drawLine(0, 3, 0, height - 2);
        }
        g.drawLine(width - 1, 3, width - 1, height - 2);
        g.setColor(bottomBorderC);
        g.drawLine(0, height - 1, width - 1, height - 1);

        g.translate(-x, -y);
    }

    protected void paintTabBackground(Graphics g, int index, int x, int y,
                                      int width, int height) {
        // shrink rectangle - don't affect border and tab header
        y += 3;
        width -= 1;
        height -= 4;
        // background body, colored according to state
        boolean selected = isSelected(index);
        boolean focused = selected && isActive();
        boolean attention = isAttention(index);
        if (focused && !attention) {
            ColorUtil.xpFillRectGradient((Graphics2D) g, x, y, width, height,
                                         focusFillBrightC, focusFillDarkC);
        } else if (selected && isMoreThanOne() && !attention) {
            g.setColor(selFillC);
            g.fillRect(x, y, width, height);
        } else if (attention) {
            Color a = new Color (255, 255, 128);
            Color b = new Color (230, 200, 64);
            ColorUtil.xpFillRectGradient((Graphics2D) g, x, y, width, height,
                                         a, b);         
        } else {
            ColorUtil.xpFillRectGradient((Graphics2D) g, x, y, width, height,
                                         unselFillBrightC, unselFillDarkC);
        }
    }

    /**
     * Override to bold font
     */
    protected Font getTxtFont() {
        Font font = super.getTxtFont();
        if (!font.isBold()) {
            font = font.deriveFont(Font.BOLD);
        }
        return font;
    }

    /**
     * Returns path of icon which is correct for currect state of tab at given
     * index
     */
    private String findIconPath(int index) {
        if (isFocused(index)) {
            if (((OwnController) getController()).isClosePressed()
                    == index) {
                return "org/netbeans/swing/tabcontrol/resources/xp-close-focus-pressed.gif"; // NOI18N
            }
            if (((OwnController) getController()).isMouseInCloseButton()
                    == index) {
                return "org/netbeans/swing/tabcontrol/resources/xp-close-focus-rollover.gif"; // NOI18N
            }
            return "org/netbeans/swing/tabcontrol/resources/xp-close-focus-normal.gif"; // NOI18N       
        }
        if (isSelected(index)) {
            if (((OwnController) getController()).isClosePressed()
                    == index) {
                return "org/netbeans/swing/tabcontrol/resources/xp-close-sel-pressed.gif"; // NOI18N
            }
            if (((OwnController) getController()).isMouseInCloseButton()
                    == index) {
                return "org/netbeans/swing/tabcontrol/resources/xp-close-sel-rollover.gif"; // NOI18N
            }
            return "org/netbeans/swing/tabcontrol/resources/xp-close-sel-normal.gif"; // NOI18N
        }
        if (((OwnController) getController()).isClosePressed() == index) {
            return "org/netbeans/swing/tabcontrol/resources/xp-close-unsel-pressed.gif"; // NOI18N
        }
        if (((OwnController) getController()).isMouseInCloseButton()
                == index) {
            return "org/netbeans/swing/tabcontrol/resources/xp-close-unsel-rollover.gif"; // NOI18N
        }
        return "org/netbeans/swing/tabcontrol/resources/xp-close-unsel-normal.gif"; // NOI18N
    }

    /**
     * @return true if tab with given index should be highlighted with XP
     *         highlight header, false otherwise.
     */
    private boolean isTabHighlighted(int index) {
        if (((OwnController) getController()).getMouseIndex() == index) {
            return true;
        }
        return isTabInFront(index) && isMoreThanOne();
    }

    /**
     * @return true if tab is selected in other tabs or selected and also
     *         active
     */
    private boolean isTabInFront(int index) {
        return isSelected(index) && (isActive() || isMoreThanOne());
    }

    /**
     * @return true if there is more then one tab, false otherwise
     */
    private boolean isMoreThanOne() {
        return getDataModel().size() > 1;
    }

    /**
     * Initialization of colors
     */
    private static void initColors() {
        if (!colorsReady) {
            txtC = UIManager.getColor("TabbedPane.foreground"); // NOI18N
            selFillC = UIManager.getColor("TabbedPane.highlight"); // NOI18N
            focusFillBrightC = UIManager.getColor("tab_focus_fill_bright"); // NOI18N
            focusFillDarkC = UIManager.getColor("tab_focus_fill_dark"); // NOI18N
            unselFillBrightC = UIManager.getColor("tab_unsel_fill_bright"); // NOI18N
            unselFillDarkC = UIManager.getColor("tab_unsel_fill_dark"); // NOI18N
            borderC = UIManager.getColor("tab_border"); // NOI18N
            bottomBorderC = UIManager.getColor("tab_bottom_border"); // NOI18N
            selBorderC = UIManager.getColor("tab_sel_border"); // NOI18N
            bgFillC = UIManager.getColor("workplace_fill"); // NOI18N
            colorsReady = true;
        }
    }

    /**
     * Computes rectangle occupied by close icon and fill values in given
     * rectangle.
     */
    private Rectangle getCloseIconRect(Rectangle rect, int index) {
        TabLayoutModel tlm = getLayoutModel();
        int x = tlm.getX(index);
        int y = tlm.getY(index);
        int w = tlm.getW(index);
        int h = tlm.getH(index);
        String iconPath = findIconPath(index);
        if (closeIcon == null) {
            //Tab control can be asked to process mouse motion events that
            //occured during startup - this causes an NPE here
            closeIcon = new IconLoader();
        }
        Icon icon = closeIcon.obtainIcon(iconPath);
        int iconWidth = icon.getIconWidth();
        int iconHeight = icon.getIconHeight();
        rect.x = x + w - iconWidth - 2 * ICON_X_PAD;
        rect.y = getCenteredIconY(icon, index);
        rect.width = iconWidth;
        rect.height = iconHeight;
        return rect;
    }

    protected PinButton createPinButton() {
        Map normalIcons = new HashMap(6);
        normalIcons.put(TabDisplayer.ORIENTATION_EAST, "org/netbeans/swing/tabcontrol/resources/xp-pin-select-normal-east.gif");
        normalIcons.put(TabDisplayer.ORIENTATION_WEST, "org/netbeans/swing/tabcontrol/resources/xp-pin-select-normal-west.gif");
        normalIcons.put(TabDisplayer.ORIENTATION_SOUTH, "org/netbeans/swing/tabcontrol/resources/xp-pin-select-normal-south.gif");
        normalIcons.put(TabDisplayer.ORIENTATION_CENTER, "org/netbeans/swing/tabcontrol/resources/xp-pin-select-normal-center.gif");
        Map pressedIcons = new HashMap(6);
        pressedIcons.put(TabDisplayer.ORIENTATION_EAST, "org/netbeans/swing/tabcontrol/resources/xp-pin-select-pressed-east.gif");
        pressedIcons.put(TabDisplayer.ORIENTATION_WEST, "org/netbeans/swing/tabcontrol/resources/xp-pin-select-pressed-west.gif");
        pressedIcons.put(TabDisplayer.ORIENTATION_SOUTH, "org/netbeans/swing/tabcontrol/resources/xp-pin-select-pressed-south.gif");
        pressedIcons.put(TabDisplayer.ORIENTATION_CENTER, "org/netbeans/swing/tabcontrol/resources/xp-pin-select-pressed-center.gif");
        Map rolloverIcons = new HashMap(6);
        rolloverIcons.put(TabDisplayer.ORIENTATION_EAST, "org/netbeans/swing/tabcontrol/resources/xp-pin-select-rollover-east.gif");
        rolloverIcons.put(TabDisplayer.ORIENTATION_WEST, "org/netbeans/swing/tabcontrol/resources/xp-pin-select-rollover-west.gif");
        rolloverIcons.put(TabDisplayer.ORIENTATION_SOUTH, "org/netbeans/swing/tabcontrol/resources/xp-pin-select-rollover-south.gif");
        rolloverIcons.put(TabDisplayer.ORIENTATION_CENTER, "org/netbeans/swing/tabcontrol/resources/xp-pin-select-rollover-center.gif");
        Map focusNormalIcons = new HashMap(6);
        focusNormalIcons.put(TabDisplayer.ORIENTATION_EAST, "org/netbeans/swing/tabcontrol/resources/xp-pin-focused-normal-east.gif");
        focusNormalIcons.put(TabDisplayer.ORIENTATION_WEST, "org/netbeans/swing/tabcontrol/resources/xp-pin-focused-normal-west.gif");
        focusNormalIcons.put(TabDisplayer.ORIENTATION_SOUTH, "org/netbeans/swing/tabcontrol/resources/xp-pin-focused-normal-south.gif");
        focusNormalIcons.put(TabDisplayer.ORIENTATION_CENTER, "org/netbeans/swing/tabcontrol/resources/xp-pin-focused-normal-center.gif");
        Map focusPressedIcons = new HashMap(6);
        focusPressedIcons.put(TabDisplayer.ORIENTATION_EAST, "org/netbeans/swing/tabcontrol/resources/xp-pin-focused-pressed-east.gif");
        focusPressedIcons.put(TabDisplayer.ORIENTATION_WEST, "org/netbeans/swing/tabcontrol/resources/xp-pin-focused-pressed-west.gif");
        focusPressedIcons.put(TabDisplayer.ORIENTATION_SOUTH, "org/netbeans/swing/tabcontrol/resources/xp-pin-focused-pressed-south.gif");
        focusPressedIcons.put(TabDisplayer.ORIENTATION_CENTER, "org/netbeans/swing/tabcontrol/resources/xp-pin-focused-pressed-center.gif");
        Map focusRolloverIcons = new HashMap(6);
        focusRolloverIcons.put(TabDisplayer.ORIENTATION_EAST, "org/netbeans/swing/tabcontrol/resources/xp-pin-focused-rollover-east.gif");
        focusRolloverIcons.put(TabDisplayer.ORIENTATION_WEST, "org/netbeans/swing/tabcontrol/resources/xp-pin-focused-rollover-west.gif");
        focusRolloverIcons.put(TabDisplayer.ORIENTATION_SOUTH, "org/netbeans/swing/tabcontrol/resources/xp-pin-focused-rollover-south.gif");
        focusRolloverIcons.put(TabDisplayer.ORIENTATION_CENTER, "org/netbeans/swing/tabcontrol/resources/xp-pin-focused-rollover-center.gif");
        
        return new XPPinButton(displayer, focusNormalIcons, focusPressedIcons, focusRolloverIcons, normalIcons, pressedIcons, rolloverIcons);
    }
    
    /**
     * Own close icon button controller
     */
    private class OwnController extends Controller {
        //TODO - add a method to AbstractViewTabDisplayerUI to get the close button rect and implement everything
        //on the parent class

        /**
         * holds index of tab in which mouse pointer was lastly located. -1
         * means mouse pointer is out of component's area
         */
        // TBD - should be part of model, not xontroller
        private int lastIndex = -1;

        /**
         * @return Index of tab in which mouse pointer is currently located.
         */
        public int getMouseIndex() {
            return lastIndex;
        }

        protected int inCloseIconRect(Point point) {
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
        
        /**
         * Triggers visual tab header change when mouse enters/leaves tab in
         * advance to superclass functionality.
         */
        public void mouseMoved(MouseEvent e) {
            super.mouseMoved(e);
            Point pos = e.getPoint();
            updateHighlight(getLayoutModel().indexOfPoint(pos.x, pos.y));
        }

        /**
         * Resets tab header in advance to superclass functionality
         */
        public void mouseExited(MouseEvent e) {
            super.mouseExited(e);
            updateHighlight(-1);
        }

        /**
         * Invokes repaint of dirty region if needed
         */
        private void updateHighlight(int curIndex) {
            if (curIndex == lastIndex) {
                return;
            }
            // compute region which needs repaint
            TabLayoutModel tlm = getLayoutModel();
            int x, y, w, h;
            Rectangle repaintRect = null;
            if (curIndex != -1) {
                x = tlm.getX(curIndex);
                y = tlm.getY(curIndex);
                w = tlm.getW(curIndex);
                h = tlm.getH(curIndex);
                repaintRect = new Rectangle(x, y, w, h);
            }
            // due to model changes, lastIndex may become invalid, so check
            if ((lastIndex != -1) && (lastIndex < getDataModel().size())) {
                x = tlm.getX(lastIndex);
                y = tlm.getY(lastIndex);
                w = tlm.getW(lastIndex);
                h = tlm.getH(lastIndex);
                if (repaintRect != null) {
                    repaintRect =
                            repaintRect.union(new Rectangle(x, y, w, h));
                } else {
                    repaintRect = new Rectangle(x, y, w, h);
                }
            }
            // trigger repaint if needed, update index
            if (repaintRect != null) {
                getDisplayer().repaint(repaintRect);
            }
            lastIndex = curIndex;
        }


    } // end of OwnController
    
    protected static class XPPinButton extends PinButton {
        private Map focusedNormal;
        private Map focusedRollover;
        private Map focusedPressed;

        private TabDisplayer displayer;
        
        
        protected XPPinButton(TabDisplayer displayer,
                              Map focusedNormal, Map focusedPressed, Map focusedRollover,
                              Map selectNormal, Map selectPressed, Map selectRollover) {
            super(selectNormal, selectPressed, selectRollover);
            this.focusedPressed = focusedPressed;
            this.focusedRollover = focusedRollover;
            this.focusedNormal = focusedNormal;
            this.displayer = displayer;
        }
        
        
        public Icon getIcon() {
            if (displayer == null) {
                //superclass constructor - UI is asking for icon
                return null;
            }
            if (displayer.isActive()) {
                return iconCache.obtainIcon((String)focusedNormal.get(getOrientation()));
            } else {
                return super.getIcon();
            }
        }

        public Icon getRolloverIcon() {
            if (displayer.isActive()) {
                return iconCache.obtainIcon((String)focusedRollover.get(getOrientation()));
            } else {
                return super.getRolloverIcon();
            }
            
        }
        
        public Icon getPressedIcon() {
            if (displayer.isActive()) {
                return iconCache.obtainIcon((String)focusedPressed.get(getOrientation()));
            } else {
                return super.getPressedIcon();
            }
        }
        
        
    }

}
