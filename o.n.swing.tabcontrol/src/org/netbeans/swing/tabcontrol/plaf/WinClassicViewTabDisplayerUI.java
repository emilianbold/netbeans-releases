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
 * Windows classic-like user interface of view type tabs. Implements Border just
 * to save one class, change if apropriate.
 *
 * @author Dafe Simonek
 */
public final class WinClassicViewTabDisplayerUI extends AbstractViewTabDisplayerUI {
    private static final boolean isGenericUI = 
        !"Windows".equals(UIManager.getLookAndFeel().getID()); //NOI18N

    /**
     * ******** constants ************
     */

    private static final int BUMP_X_PAD = isGenericUI ? 0 : 3;
    private static final int BUMP_WIDTH = isGenericUI ? 0 : 3;
    private static final int TXT_X_PAD = isGenericUI ? 3 : BUMP_X_PAD + BUMP_WIDTH + 5;
    private static final int TXT_Y_PAD = 3;

    private static final int ICON_X_PAD = 1;


    /**
     * ******** static fields *********
     */

    private static Color borderC, bgFillC;

    private static IconLoader closeIcon;

    /**
     * ******** instance fields ********
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
    private WinClassicViewTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
        prefSize = new Dimension(100, 19); 
    }

    public static ComponentUI createUI(JComponent c) {
        return new WinClassicViewTabDisplayerUI((TabDisplayer) c);
    }

    protected AbstractViewTabDisplayerUI.Controller createController() {
        return new OwnController();
    }

    public Dimension getPreferredSize(JComponent c) {
        FontMetrics fm = getTxtFontMetrics();
        int height = fm == null ?
                19 : fm.getAscent() + 2 * fm.getDescent() + 2;
        Insets insets = c.getInsets();
        prefSize.height = height + insets.bottom + insets.top;
        return prefSize;
    }

    /**
     * adds painting of overall border
     */
    public void paint(Graphics g, JComponent c) {
        if (ColorUtil.shouldAntialias()) {
            ColorUtil.setupAntialiasing(g);
        }
        Color col = c.getBackground();
        if (col != null) {
            g.setColor (col);
            g.fillRect (0, 0, c.getWidth(), c.getHeight());
        }
        paintOverallBorder(g, c);
        super.paint(g, c);
    }

    /**
     * Paints lower border, bottom line, separating tabs from content
     */
    protected void paintOverallBorder(Graphics g, JComponent c) {
        if (isGenericUI) {
            return;
        }
        Rectangle r = c.getBounds();
        g.setColor(UIManager.getColor("InternalFrame.borderDarkShadow")); //NOI18N
        g.drawLine(0, r.height - 1, r.width - 1, r.height - 1);
    }
    
    protected Font getTxtFont() {
        if (isGenericUI) {
            Font result = UIManager.getFont("controlFont");
            if (result != null) {
                return result;
            }
        }
        return super.getTxtFont();
    }     

    protected void paintTabContent(Graphics g, int index, String text, int x,
                                   int y, int width, int height) {
        // substract lower border
        height--;
        y -= 2; //align to center
        FontMetrics fm = getTxtFontMetrics();
        // setting font already here to compute string width correctly
        g.setFont(getTxtFont());
        int txtWidth = width;
        if (isSelected(index)) {
            // paint text and close icon
            // close icon has the bigger space priority then text
            PinButton pin = configurePinButton (index);
            boolean showPin = pin != null && pin.getOrientation() != TabDisplayer.ORIENTATION_INVISIBLE;
            int space4Pin = showPin ? pinButton.getWidth() + 1 : 0;
            if (displayer.isShowCloseButton()) {
                if (closeIcon == null) {
                    closeIcon = new IconLoader();
                }
                String iconPath = findIconPath(index);
                Icon icon = closeIcon.obtainIcon(iconPath);
                int iconWidth = icon.getIconWidth();
                int space4Icon = iconWidth + (2 * ICON_X_PAD) + space4Pin + 4;
                txtWidth = width - space4Icon;
                getCloseIconRect(tempRect, index);
                icon.paintIcon(getDisplayer(), g, tempRect.x, tempRect.y);
            } else {
                tempRect.x = x + (width - 2);
                
                tempRect.y = !showPin ? 0 : ((displayer.getHeight() / 2) -
                    (pinButton.getPreferredSize().height / 2));
                txtWidth = width - 2 * TXT_X_PAD;
                
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
        }
        // draw bump (dragger)
        drawBump(g, index, x + 4, y + 6, BUMP_WIDTH, height - 8);
        
        // draw text in right color
        Color txtC = UIManager.getColor("TabbedPane.foreground"); //NOI18N
        
        HtmlRenderer.renderString(text, g, x + TXT_X_PAD, y + fm.getAscent()
            + TXT_Y_PAD,
            txtWidth, height, getTxtFont(),
            txtC,
            HtmlRenderer.STYLE_TRUNCATE, true);
    }

    protected void paintTabBorder(Graphics g, int index, int x, int y,
                                  int width, int height) {
                                      
        // subtract lower border
        height--;
        boolean isSelected = isSelected(index);
        boolean isLast = index == getDataModel().size() - 1;

        g.translate(x, y);

        g.setColor(UIManager.getColor("InternalFrame.borderShadow")); //NOI18N
        g.drawLine(0, height - 1, width - 2, height - 1);
        g.drawLine(width - 1, height - 1, width - 1, 0);

        g.setColor(isSelected ? UIManager.getColor(
                "InternalFrame.borderHighlight") //NOI18N
                   : UIManager.getColor("InternalFrame.borderLight")); //NOI18N
        g.drawLine(0, 0, 0, height - 1);
        g.drawLine(1, 0, width - 2, 0);

        g.translate(-x, -y);
    }

    protected void paintTabBackground(Graphics g, int index, int x, int y,
                                      int width, int height) {
        // substract lower border
        height--;
        ((Graphics2D) g).setPaint(
                getBackgroundPaint(g, index, x, y, width, height));
        if (isFocused(index)) {
            g.fillRect(x, y, width, height);
        } else {
            g.fillRect(x + 1, y + 1, width - 2, height - 2);
        }
    }

    private Paint getBackgroundPaint(Graphics g, int index, int x, int y,
                                     int width, int height) {
        // background body, colored according to state
        boolean selected = isSelected(index);
        boolean focused = isFocused(index);
        boolean attention = isAttention(index);
        
        Paint result = null;
        if (focused && !attention) {
            result =
                    ColorUtil.getGradientPaint(x, y, getSelGradientColor(),
                                               //NOI18N
                                               x + width, y,
                                               UIManager.getColor(
                                                       "TabbedPane.background"));
        } else if (selected && !attention) {
            result = UIManager.getColor("TabbedPane.background"); //NOI18N
        } else if (attention) {
            result = WinClassicEditorTabCellRenderer.ATTENTION_COLOR;
        } else {
            result = UIManager.getColor("tab_unsel_fill");
        }
        return result;
    }

    /**
     * Returns path of icon which is correct for currect state of tab at given
     * index
     */
    private String findIconPath(int index) {
        if (controller.isClosePressed() == index) {
            return "org/netbeans/swing/tabcontrol/resources/win_bigclose-pressed.gif"; // NOI18N
        }
        if (controller.isMouseInCloseButton() == index) {
            return "org/netbeans/swing/tabcontrol/resources/win_bigclose-rollover.gif"; // NOI18N
        }
        /*if (isFocused(index)) {
            return "org/netbeans/swing/tabcontrol/resources/win_bigclose-focus.gif"; // NOI18N
        }*/
        return "org/netbeans/swing/tabcontrol/resources/win_bigclose-normal.gif"; // NOI18N
    }

    /**
     * Paints dragger in given rectangle
     */
    private void drawBump(Graphics g, int index, int x, int y, int width,
                          int height) {
        if (isGenericUI) {
            //This look and feel is also used as the default UI on non-JDS
            //GTK L&F
            return;
        }
                              
        // prepare colors
        Color highlightC, bodyC, shadowC;
        if (isFocused(index)) {
            bodyC = new Color(210, 220, 243); //XXX
            highlightC = bodyC.brighter();
            shadowC = bodyC.darker();
        } else if (isSelected(index)) {
            highlightC =
                    UIManager.getColor("InternalFrame.borderHighlight"); //NOI18N
            bodyC = UIManager.getColor("InternalFrame.borderLight"); //NOI18N
            shadowC = UIManager.getColor("InternalFrame.borderShadow"); //NOI18N
        } else {
            highlightC = UIManager.getColor("InternalFrame.borderLight"); //NOI18N
            bodyC = UIManager.getColor("tab_unsel_fill");
            shadowC = UIManager.getColor("InternalFrame.borderShadow"); //NOI18N
        }
        // draw
        for (int i = 0; i < width / 3; i++, x += 3) {
            g.setColor(highlightC);
            g.drawLine(x, y, x, y + height - 1);
            g.drawLine(x, y, x + 1, y);
            g.setColor(bodyC);
            g.drawLine(x + 1, y + 1, x + 1, y + height - 2);
            g.setColor(shadowC);
            g.drawLine(x + 2, y, x + 2, y + height - 1);
            g.drawLine(x, y + height - 1, x + 1, y + height - 1);
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
        rect.y = getCenteredIconY(icon, index) - 1;
        rect.width = iconWidth;
        rect.height = iconHeight;
        return rect;
    }

    /** Creates pin button with specialized icons for Win classic LF */
    protected PinButton createPinButton() {
        Map normalIcons = new HashMap(6);
        normalIcons.put(TabDisplayer.ORIENTATION_EAST, "org/netbeans/swing/tabcontrol/resources/win-pin-normal-east.gif");
        normalIcons.put(TabDisplayer.ORIENTATION_WEST, "org/netbeans/swing/tabcontrol/resources/win-pin-normal-west.gif");
        normalIcons.put(TabDisplayer.ORIENTATION_SOUTH, "org/netbeans/swing/tabcontrol/resources/win-pin-normal-south.gif");
        normalIcons.put(TabDisplayer.ORIENTATION_CENTER, "org/netbeans/swing/tabcontrol/resources/win-pin-normal-center.gif");
        Map pressedIcons = new HashMap(6);
        pressedIcons.put(TabDisplayer.ORIENTATION_EAST, "org/netbeans/swing/tabcontrol/resources/win-pin-pressed-east.gif");
        pressedIcons.put(TabDisplayer.ORIENTATION_WEST, "org/netbeans/swing/tabcontrol/resources/win-pin-pressed-west.gif");
        pressedIcons.put(TabDisplayer.ORIENTATION_SOUTH, "org/netbeans/swing/tabcontrol/resources/win-pin-pressed-south.gif");
        pressedIcons.put(TabDisplayer.ORIENTATION_CENTER, "org/netbeans/swing/tabcontrol/resources/win-pin-pressed-center.gif");
        Map rolloverIcons = new HashMap(6);
        rolloverIcons.put(TabDisplayer.ORIENTATION_EAST, "org/netbeans/swing/tabcontrol/resources/win-pin-rollover-east.gif");
        rolloverIcons.put(TabDisplayer.ORIENTATION_WEST, "org/netbeans/swing/tabcontrol/resources/win-pin-rollover-west.gif");
        rolloverIcons.put(TabDisplayer.ORIENTATION_SOUTH, "org/netbeans/swing/tabcontrol/resources/win-pin-rollover-south.gif");
        rolloverIcons.put(TabDisplayer.ORIENTATION_CENTER, "org/netbeans/swing/tabcontrol/resources/win-pin-rollover-center.gif");
        return new PinButton(normalIcons, pressedIcons, rolloverIcons);
    }
    
    private static final Color getSelGradientColor() {
        return UIManager.getColor("winclassic_tab_sel_gradient");
    }

    /**
     * Own close icon button controller
     */
    private class OwnController extends Controller {
        //TODO - add a method to AbstractViewTabDisplayerUI to get the close button rect and implement everything
        //on the parent class

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
        

    } // end of OwnController

}
