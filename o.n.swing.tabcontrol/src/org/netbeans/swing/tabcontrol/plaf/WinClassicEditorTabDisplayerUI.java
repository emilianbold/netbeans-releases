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
        displayer.setBackground(ColorUtil.adjustTowards(dark, 35, light));
        displayer.setOpaque(true);
    }

    public Dimension getPreferredSize(JComponent c) {
        int prefHeight = 28;
        Graphics g = TabListPopup.getOffscreenGraphics();
        if (g != null) {
            FontMetrics fm = g.getFontMetrics(displayer.getFont());
            Insets ins = getTabAreaInsets();
            prefHeight = fm.getHeight() + ins.top + ins.bottom + 6;
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

    protected void paintAfterTabs(Graphics g) {
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
