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
/*
 * WinClassicEditorTabDisplayerUI.java
 *
 * Created on 09 December 2003, 16:53
 */

package org.netbeans.swing.tabcontrol.plaf;

import org.netbeans.swing.tabcontrol.TabDisplayer;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

/**
 * Windows classic impl of tabs ui
 *
 * @author Tim Boudreau
 */
public final class WinClassicEditorTabDisplayerUI extends BasicScrollingTabDisplayerUI {
    private static final int[] xpoints = new int[20];
    private static final int[] ypoints = new int[20];
    private static final Rectangle scratch5 = new Rectangle();
    
    private static boolean isGenericUI = !"Windows".equals( //NOI18N
        UIManager.getLookAndFeel().getID());

    /**
     * Creates a new instance of WinClassicEditorTabDisplayerUI
     */
    public WinClassicEditorTabDisplayerUI(TabDisplayer displayer) {
        super (displayer);
    }
    
    public static ComponentUI createUI(JComponent c) {
        return new WinClassicEditorTabDisplayerUI ((TabDisplayer) c);
    }

    public Rectangle getTabRect(int idx, Rectangle rect) {
        Rectangle r = super.getTabRect (idx, rect);
        //For win classic, take up the full space, even the insets, to match
        //earlier appearance
        r.y = 0;
        r.height = displayer.getHeight();
        return r;
    }  

    public void install() {
        super.install();
        Color dark = UIManager.getColor("controlShadow"); //NOI18N
        Color light = UIManager.getColor("control"); //NOI18N
        if (!isGenericUI) {
            displayer.setBackground(ColorUtil.adjustTowards(dark, 35, light));
            displayer.setOpaque(true);
        }
    }

    public Dimension getPreferredSize(JComponent c) {
        int prefHeight = 28;
        Graphics g = TabListPopup.getOffscreenGraphics();
        if (g != null) {
            FontMetrics fm = g.getFontMetrics(displayer.getFont());
            Insets ins = getTabAreaInsets();
            prefHeight = fm.getHeight() + ins.top + ins.bottom + (isGenericUI ? 5 : 6);
        }
        return new Dimension(displayer.getWidth(), prefHeight);
    }

    protected AbstractButton[] createControlButtons() {
        JButton[] result = new JButton[3];
        result[0] = new TimerButton(scroll().getBackwardAction());
        result[1] = new TimerButton(scroll().getForwardAction());
        result[2] = new OnPressButton(new TabListPopupAction(displayer));
        configureButton(result[0], new LeftIcon());
        configureButton(result[2], new DownIcon());
        configureButton(result[1], new RightIcon());
        result[0].setPreferredSize(new Dimension(15, 14));
        result[2].setPreferredSize(new Dimension(16, 14));
        result[1].setPreferredSize(new Dimension(15, 14));

        scroll().getBackwardAction().putValue("control", displayer); //NOI18N
        scroll().getForwardAction().putValue("control", displayer); //NOI18N

        return result;
    }

    private static void configureButton(JButton button, Icon icon) {
        button.setIcon(icon);
        button.setMargin(null);
        button.setText(null);
        //undocumented (?) call to hide action text - see JButton line 234
        button.putClientProperty("hideActionText", Boolean.TRUE); //NOI18N
        button.setFocusable(false);
    }

    private void genericPaintAfterTabs (Graphics g) {
        g.setColor (UIManager.getColor("controlShadow")); //NOI18N
        Insets ins = displayer.getInsets();
//        g.drawLine (ins.left, displayer.getHeight()-1, displayer.getWidth() - (ins.right + ins.left),
//            displayer.getHeight()-1);  
        Rectangle r = new Rectangle();
        getTabsVisibleArea(r);
        r.width = displayer.getWidth();
        int selEnd = 0;
        int last = getLastVisibleTab();
        if (last > -1) {
            getTabRect (last, scratch5);
            g.drawLine (scratch5.x + scratch5.width, displayer.getHeight() -1, 
                displayer.getWidth() - (ins.left + ins.right) - 4, 
                displayer.getHeight() - 1);
            g.drawLine (0, displayer.getHeight() - 2, 2, displayer.getHeight() -2);
            if ("GTK".equals(UIManager.getLookAndFeel().getID())) {
                boolean sel = last == displayer.getSelectionModel().getSelectedIndex();
                //paint a fading shadow to match the view tabs
                int x = scratch5.x + scratch5.width;
                g.setColor (sel ? UIManager.getColor("controlShadow") :
                    ColorUtil.adjustTowards(g.getColor(), 20,
                    UIManager.getColor("control"))); //NOI18N
                g.drawLine (x, 
                    scratch5.y + 5, x,
                    scratch5.y + scratch5.height -2);
                g.setColor (ColorUtil.adjustTowards(g.getColor(), 20,
                    UIManager.getColor("control"))); //NOI18N
                g.drawLine (x + 1, 
                    scratch5.y + 6, x + 1,
                    scratch5.y + scratch5.height -2);
            }
            if ((tabState.getState(getFirstVisibleTab()) & TabState.CLIP_LEFT)
                !=0 && getFirstVisibleTab() != 
                displayer.getSelectionModel().getSelectedIndex()) {
                    //Draw a small gradient line continuing the left edge of
                    //the displayer up the left side of a left clipped tab
                GradientPaint gp = ColorUtil.getGradientPaint(
                    0, displayer.getHeight() / 2, UIManager.getColor("control"),
                    0, displayer.getHeight(), UIManager.getColor("controlShadow"));
                ((Graphics2D) g).setPaint(gp);
                g.drawLine (0, displayer.getHeight() / 2, 0, displayer.getHeight());
            } else {
                //Fill the small gap between the top of the content displayer
                //and the bottom of the tabs, caused by the tab area bottom inset
                g.setColor (UIManager.getColor("controlShadow"));
                g.drawLine (0, displayer.getHeight(), 0, displayer.getHeight() - 2);
            }
            if ((tabState.getState(getLastVisibleTab()) & TabState.CLIP_RIGHT) != 0
                && getLastVisibleTab() != 
                displayer.getSelectionModel().getSelectedIndex()) {
                GradientPaint gp = ColorUtil.getGradientPaint(
                    0, displayer.getHeight() / 2, UIManager.getColor("control"),
                    0, displayer.getHeight(), UIManager.getColor("controlShadow"));
                ((Graphics2D) g).setPaint(gp);
                getTabRect (getLastVisibleTab(), scratch5);
                g.drawLine (scratch5.x + scratch5.width, displayer.getHeight() / 2, 
                    scratch5.x + scratch5.width, displayer.getHeight());
            }
            
        } else {
            g.drawLine(r.x, displayer.getHeight() - ins.bottom, r.x + r.width - 4,
                       displayer.getHeight() - ins.bottom);
        }
    }
    
    protected void paintAfterTabs(Graphics g) {
        if (isGenericUI) {
            genericPaintAfterTabs(g);
            return;
        }
        Rectangle r = new Rectangle();
        getTabsVisibleArea(r);
        r.width = displayer.getWidth();
        g.setColor(displayer.isActive() ?
                   defaultRenderer.getSelectedActivatedBackground() :
                   defaultRenderer.getSelectedBackground());

        Insets ins = getTabAreaInsets();
        g.fillRect(r.x, r.y + r.height, r.x + r.width,
                   displayer.getHeight() - (r.y + r.height));

        g.setColor(UIManager.getColor("controlHighlight")); //NOI18N

        int selEnd = 0;
        int i = selectionModel.getSelectedIndex();
        if (i != -1) {
            getTabRect(i, scratch5);
            if (scratch5.width != 0) {
                if (r.x < scratch5.x) {
                    g.drawLine(r.x, displayer.getHeight() - ins.bottom,
                               scratch5.x - 1,
                               displayer.getHeight() - ins.bottom);
                }
                if (scratch5.x + scratch5.width < r.x + r.width) {
                    selEnd = scratch5.x + scratch5.width;
                    g.drawLine(selEnd, displayer.getHeight() - ins.bottom,
                               r.x + r.width,
                               displayer.getHeight() - ins.bottom);
                }
            }
            return;
        }

        g.drawLine(r.x, displayer.getHeight() - ins.bottom, r.x + r.width,
                   displayer.getHeight() - ins.bottom);
    }

    protected TabCellRenderer createDefaultRenderer() {
        return new WinClassicEditorTabCellRenderer();
    }

    protected LayoutManager createLayout() {
        return new WCLayout();
    }

    public java.awt.Insets getTabAreaInsets() {
        return new Insets(0, 0, 0, 55);
    }
    
    private class WCLayout implements LayoutManager {

        public void addLayoutComponent(String name, Component comp) {
        }

        public void layoutContainer(java.awt.Container parent) {
            Insets in = getTabAreaInsets();
            Component[] c = parent.getComponents();
            int x = parent.getWidth() - in.right + 4;
            int y = 0;
            Dimension psize;
            for (int i = 0; i < c.length; i++) {
                psize = c[i].getPreferredSize();
                y = in.top + 4; //hardcoded to spec
                int w = Math.min(psize.width, parent.getWidth() - x);
                if (i == 2) {
                    x += 2;
                }
                c[i].setBounds(x, y, w, Math.min(psize.height,
                                                 parent.getHeight()));
                x += psize.width;
            }
        }

        public Dimension minimumLayoutSize(Container parent) {
            return getPreferredSize((JComponent) parent);
        }

        public Dimension preferredLayoutSize(Container parent) {
            return getPreferredSize((JComponent) parent);
        }

        public void removeLayoutComponent(java.awt.Component comp) {
        }
    }

    private static final int ICON_WIDTH = 17;
    private static final int ICON_HEIGHT = 14;

    private static class LeftIcon implements Icon {
        protected boolean enabled = false;

        public int getIconHeight() {
            return ICON_HEIGHT;
        }

        public int getIconWidth() {
            return ICON_WIDTH;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            y += 1;
            x -= 1;
            xpoints[0] = x + 5;
            ypoints[0] = y + 6;

            xpoints[1] = x + 9;
            ypoints[1] = y + 2;

            xpoints[2] = x + 9;
            ypoints[2] = y + 10;
            g.setColor(c.isEnabled() ?
                       c.getForeground() :
                       UIManager.getColor("controlShadow")); //NOI18N
            g.fillPolygon(xpoints, ypoints, 3);
        }
    }

    private static class RightIcon extends LeftIcon {
        public int getIconHeight() {
            return ICON_HEIGHT;
        }

        public int getIconWidth() {
            return ICON_WIDTH;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            y += 1;

            xpoints[0] = x + 5;
            ypoints[0] = y + 2;

            xpoints[1] = x + 5;
            ypoints[1] = y + 10;

            xpoints[2] = x + 9;
            ypoints[2] = y + 6;
            g.setColor(c.isEnabled() ?
                       c.getForeground() :
                       UIManager.getColor("controlShadow")); //NOI18N
            g.fillPolygon(xpoints, ypoints, 3);
        }
    }

    private static class DownIcon extends LeftIcon {
        public int getIconHeight() {
            return ICON_HEIGHT;
        }

        public int getIconWidth() {
            return ICON_WIDTH;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            y += 1;
            xpoints[0] = x + 4;
            ypoints[0] = y + 5;

            xpoints[1] = x + 11;
            ypoints[1] = y + 5;

            xpoints[2] = x + 7;
            ypoints[2] = y + 9;
            g.setColor(c.isEnabled() ?
                       c.getForeground() :
                       UIManager.getColor("controlShadow")); //NOI18N
            g.fillPolygon(xpoints, ypoints, 3);

        }
    }
}
