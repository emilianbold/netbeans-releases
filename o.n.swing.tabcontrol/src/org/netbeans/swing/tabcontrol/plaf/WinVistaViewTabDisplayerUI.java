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
 * Win Vista-like user interface of view type tabs.
 *
 * @author S. Aubrecht
 */
public final class WinVistaViewTabDisplayerUI extends AbstractViewTabDisplayerUI {

    /*********** constants *************/

    /**
     * Space between text and left side of the tab
     */
    private static final int TXT_X_PAD = 9;
    private static final int TXT_Y_PAD = 3;

    private static final int ICON_X_PAD = 1;
    private static final int ICON_Y_PAD = 7;

    private static final int BUMP_X_PAD = 3;
    private static final int BUMP_Y_PAD_UPPER = 6;
    private static final int BUMP_Y_PAD_BOTTOM = 3;

    /*********** static fields **********/
    
    /**
     * True when colors were already initialized, false otherwise
     */
    private static boolean colorsReady = false;

    private static Color 
            unselFillBrightUpperC, 
            unselFillDarkUpperC, 
            unselFillBrightLowerC, 
            unselFillDarkLowerC, 
            selFillC, 
            focusFillUpperC, 
            focusFillBrightLowerC, 
            focusFillDarkLowerC, 
            mouseOverFillBrightUpperC, 
            mouseOverFillDarkUpperC, 
            mouseOverFillBrightLowerC, 
            mouseOverFillDarkLowerC, 
            txtC, 
            borderC, 
            selBorderC, 
            borderInnerC;

    private static AbstractViewTabDisplayerUI.IconLoader closeIcon;

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
    private WinVistaViewTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
        prefSize = new Dimension(100, 17);
    }

    public static ComponentUI createUI(JComponent c) {
        return new WinVistaViewTabDisplayerUI((TabDisplayer)c);
    }
     
    public void installUI (JComponent c) {
        super.installUI(c);
        initColors();
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
        if( 0 == index )
            x++;
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
                txtWidth = width - TXT_X_PAD - space4Icon;
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
        ColorUtil.paintVistaTabDragTexture(getDisplayer(), g, x + BUMP_X_PAD, y
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

        Color borderColor = isHighlighted ? selBorderC : borderC;
        g.setColor(borderColor);
        int left = 0;
        //left
        if (isFirst )
            g.drawLine(0, 0, 0, height - 2);
        //top
        g.drawLine(0, 0, width - 1, 0);
        //right
        if( index < getDataModel().size()-1 && isTabHighlighted(index+1) )
            g.setColor( selBorderC );
        g.drawLine(width - 1, 0, width - 1, height - 2);
        //bottom
        g.setColor(borderC);
        g.drawLine(0, height - 1, width - 1, height - 1);
        
        //inner white border
        g.setColor(borderInnerC);
        //left
        if (isFirst)
            g.drawLine(1, 1, 1, height - 2);
        else
            g.drawLine(0, 1, 0, height - 2);
        //right
        g.drawLine(width-2, 1, width-2, height - 2);
        //top
        g.drawLine(1, 1, width-2, 1);

        g.translate(-x, -y);
    }

    protected void paintTabBackground(Graphics g, int index, int x, int y,
                                      int width, int height) {
        // shrink rectangle - don't affect border and tab header
        y += 2;
        height -= 2;
        // background body, colored according to state
        boolean selected = isSelected(index);
        boolean focused = selected && isActive();
        boolean attention = isAttention(index);
        boolean mouseOver = isMouseOver(index);
        if (focused && !attention) {
            ColorUtil.vistaFillRectGradient((Graphics2D) g, x, y, width, height,
                                         focusFillUpperC,  
                                         focusFillBrightLowerC, focusFillDarkLowerC );
        } else if (selected && !attention) {
            g.setColor(selFillC);
            g.fillRect(x, y, width, height);
        } else if (mouseOver && !attention) {
            ColorUtil.vistaFillRectGradient((Graphics2D) g, x, y, width, height,
                                         mouseOverFillBrightUpperC, mouseOverFillDarkUpperC, 
                                         mouseOverFillBrightLowerC, mouseOverFillDarkLowerC );
        } else if (attention) {
            Color a = new Color (255, 255, 128);
            Color b = new Color (230, 200, 64);
            ColorUtil.xpFillRectGradient((Graphics2D) g, x, y, width, height, a, b);         
        } else {
            ColorUtil.vistaFillRectGradient((Graphics2D) g, x, y, width, height,
                                         unselFillBrightUpperC, unselFillDarkUpperC, 
                                         unselFillBrightLowerC, unselFillDarkLowerC );
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
        if (((OwnController) getController()).isClosePressed()
                == index) {
            return "org/netbeans/swing/tabcontrol/resources/vista_close_pressed.png"; // NOI18N
        }
        if (((OwnController) getController()).isMouseInCloseButton()
                == index) {
            return "org/netbeans/swing/tabcontrol/resources/vista_close_over.png"; // NOI18N
        }
        return "org/netbeans/swing/tabcontrol/resources/vista_close_enabled.png"; // NOI18N       
    }

    /**
     * @return true if tab with given index should have highlighted border, false otherwise.
     */
    private boolean isTabHighlighted(int index) {
        if (((OwnController) getController()).getMouseIndex() == index) {
            return true;
        }
        return isTabInFront(index) && isMoreThanOne();
    }

    /**
     * @return true if tab with given index has mouse cursor above and is not
     * the selected one, false otherwise.
     */
    private boolean isMouseOver(int index) {
        return ((OwnController) getController()).getMouseIndex() == index
                && !isTabInFront(index);
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
            
            selFillC = UIManager.getColor("tab_sel_fill"); // NOI18N
            
            focusFillUpperC = UIManager.getColor("tab_focus_fill_upper"); // NOI18N
            focusFillBrightLowerC = UIManager.getColor("tab_focus_fill_bright_lower"); // NOI18N
            focusFillDarkLowerC = UIManager.getColor("tab_focus_fill_dark_lower"); // NOI18N
            
            unselFillBrightUpperC = UIManager.getColor("tab_unsel_fill_bright_upper"); // NOI18N
            unselFillDarkUpperC = UIManager.getColor("tab_unsel_fill_dark_upper"); // NOI18N
            unselFillBrightLowerC = UIManager.getColor("tab_unsel_fill_bright_lower"); // NOI18N
            unselFillDarkLowerC = UIManager.getColor("tab_unsel_fill_dark_lower"); // NOI18N
            
            mouseOverFillBrightUpperC = UIManager.getColor("tab_mouse_over_fill_bright_upper"); // NOI18N
            mouseOverFillDarkUpperC = UIManager.getColor("tab_mouse_over_fill_dark_upper"); // NOI18N
            mouseOverFillBrightLowerC = UIManager.getColor("tab_mouse_over_fill_bright_lower"); // NOI18N
            mouseOverFillDarkLowerC = UIManager.getColor("tab_mouse_over_fill_dark_lower"); // NOI18N
            
            borderC = UIManager.getColor("tab_border"); // NOI18N
            selBorderC = UIManager.getColor("tab_sel_border"); // NOI18N
            borderInnerC = UIManager.getColor("tab_border_inner"); // NOI18N
            
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
        normalIcons.put(TabDisplayer.ORIENTATION_EAST, "org/netbeans/swing/tabcontrol/resources/vista_slideright_enabled.png");
        normalIcons.put(TabDisplayer.ORIENTATION_WEST, "org/netbeans/swing/tabcontrol/resources/vista_slideleft_enabled.png");
        normalIcons.put(TabDisplayer.ORIENTATION_SOUTH, "org/netbeans/swing/tabcontrol/resources/vista_slidedown_enabled.png");
        normalIcons.put(TabDisplayer.ORIENTATION_CENTER, "org/netbeans/swing/tabcontrol/resources/vista_pin_enabled.png");
        Map pressedIcons = new HashMap(6);
        pressedIcons.put(TabDisplayer.ORIENTATION_EAST, "org/netbeans/swing/tabcontrol/resources/vista_slideright_pressed.png");
        pressedIcons.put(TabDisplayer.ORIENTATION_WEST, "org/netbeans/swing/tabcontrol/resources/vista_slideleft_pressed.png");
        pressedIcons.put(TabDisplayer.ORIENTATION_SOUTH, "org/netbeans/swing/tabcontrol/resources/vista_slidedown_pressed.png");
        pressedIcons.put(TabDisplayer.ORIENTATION_CENTER, "org/netbeans/swing/tabcontrol/resources/vista_pin_pressed.png");
        Map rolloverIcons = new HashMap(6);
        rolloverIcons.put(TabDisplayer.ORIENTATION_EAST, "org/netbeans/swing/tabcontrol/resources/vista_slideright_over.png");
        rolloverIcons.put(TabDisplayer.ORIENTATION_WEST, "org/netbeans/swing/tabcontrol/resources/vista_slideleft_over.png");
        rolloverIcons.put(TabDisplayer.ORIENTATION_SOUTH, "org/netbeans/swing/tabcontrol/resources/vista_slidedown_over.png");
        rolloverIcons.put(TabDisplayer.ORIENTATION_CENTER, "org/netbeans/swing/tabcontrol/resources/vista_pin_over.png");
        Map focusNormalIcons = new HashMap(6);
        focusNormalIcons.put(TabDisplayer.ORIENTATION_EAST, "org/netbeans/swing/tabcontrol/resources/vista_slideright_enabled.png");
        focusNormalIcons.put(TabDisplayer.ORIENTATION_WEST, "org/netbeans/swing/tabcontrol/resources/vista_slideleft_enabled.png");
        focusNormalIcons.put(TabDisplayer.ORIENTATION_SOUTH, "org/netbeans/swing/tabcontrol/resources/vista_slidedown_enabled.png");
        focusNormalIcons.put(TabDisplayer.ORIENTATION_CENTER, "org/netbeans/swing/tabcontrol/resources/vista_pin_enabled.png");
        Map focusPressedIcons = new HashMap(6);
        focusPressedIcons.put(TabDisplayer.ORIENTATION_EAST, "org/netbeans/swing/tabcontrol/resources/vista_slideright_pressed.png");
        focusPressedIcons.put(TabDisplayer.ORIENTATION_WEST, "org/netbeans/swing/tabcontrol/resources/vista_slideleft_pressed.png");
        focusPressedIcons.put(TabDisplayer.ORIENTATION_SOUTH, "org/netbeans/swing/tabcontrol/resources/vista_slidedown_pressed.png");
        focusPressedIcons.put(TabDisplayer.ORIENTATION_CENTER, "org/netbeans/swing/tabcontrol/resources/vista_pin_pressed.png");
        Map focusRolloverIcons = new HashMap(6);
        focusRolloverIcons.put(TabDisplayer.ORIENTATION_EAST, "org/netbeans/swing/tabcontrol/resources/vista_slideright_over.png");
        focusRolloverIcons.put(TabDisplayer.ORIENTATION_WEST, "org/netbeans/swing/tabcontrol/resources/vista_slideleft_over.png");
        focusRolloverIcons.put(TabDisplayer.ORIENTATION_SOUTH, "org/netbeans/swing/tabcontrol/resources/vista_slidedown_over.png");
        focusRolloverIcons.put(TabDisplayer.ORIENTATION_CENTER, "org/netbeans/swing/tabcontrol/resources/vista_pin_over.png");
        
        return new VistaPinButton(displayer, focusNormalIcons, focusPressedIcons, focusRolloverIcons, normalIcons, pressedIcons, rolloverIcons);
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
        // TBD - should be part of model, not controller
        private int lastIndex = -1;

        /**
         * @return Index of tab in which mouse pointer is currently located.
         */
        public int getMouseIndex() {
            return lastIndex;
        }

        protected int inCloseIconRect(Point point) {
            if (!displayer.isShowCloseButton()) {
                return -1;
            }
            int index = getLayoutModel().indexOfPoint(point.x, point.y);
            if (index < 0 || !isSelected(index)) {
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
            // #72459: don't reset highlight if mouse exited into pin button
            if (!inPinButtonRect(e.getPoint())) {
                updateHighlight(-1);
            }
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
                x = tlm.getX(curIndex)-1;
                y = tlm.getY(curIndex);
                w = tlm.getW(curIndex)+2;
                h = tlm.getH(curIndex);
                repaintRect = new Rectangle(x, y, w, h);
            }
            // due to model changes, lastIndex may become invalid, so check
            if ((lastIndex != -1) && (lastIndex < getDataModel().size())) {
                x = tlm.getX(lastIndex)-1;
                y = tlm.getY(lastIndex);
                w = tlm.getW(lastIndex)+2;
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
    
    protected static class VistaPinButton extends PinButton {
        private Map focusedNormal;
        private Map focusedRollover;
        private Map focusedPressed;

        private TabDisplayer displayer;
        
        
        protected VistaPinButton(TabDisplayer displayer,
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
