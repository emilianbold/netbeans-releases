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
 * MetalEditorTabDisplayerUI.java
 *
 * Created on December 2, 2003, 9:40 PM
 */

package org.netbeans.swing.tabcontrol.plaf;

import org.netbeans.swing.tabcontrol.TabDisplayer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import java.awt.*;

/**
 * Tab displayer UI for Metal look and feel
 *
 * @author Tim Boudreau
 */
public final class MetalEditorTabDisplayerUI extends BasicScrollingTabDisplayerUI {
    private Rectangle scratch = new Rectangle();

    /**
     * Creates a new instance of MetalEditorTabDisplayerUI
     */
    public MetalEditorTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
    }

    protected TabCellRenderer createDefaultRenderer() {
        return new MetalEditorTabCellRenderer();
    }

    public static ComponentUI createUI(JComponent c) {
        return new MetalEditorTabDisplayerUI((TabDisplayer) c);
    }

    public Dimension getMinimumSize(JComponent c) {
        return new Dimension (80, 28);
    }
    
    public Dimension getPreferredSize(JComponent c) {
        int prefHeight = 28;
        Graphics g = TabListPopup.getOffscreenGraphics();
        if (g != null) {
            FontMetrics fm = g.getFontMetrics(displayer.getFont());
            Insets ins = getTabAreaInsets();
            prefHeight = fm.getHeight() + ins.top + ins.bottom + 9;
        }
        return new Dimension(displayer.getWidth(), prefHeight);
    }

    protected int createRepaintPolicy () {
        return TabState.REPAINT_ALL_TABS_ON_ACTIVATION_CHANGE
                | TabState.REPAINT_ALL_TABS_ON_SELECTION_CHANGE
                | TabState.REPAINT_ON_MOUSE_ENTER_CLOSE_BUTTON;
    }


    public Insets getTabAreaInsets() {
        return new Insets(0, 0, 4, 57);
    }

    public void install() {
        super.install();
        displayer.setBackground(UIManager.getColor("control")); //NOI18N
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
            getTabRect(i, scratch);
            if (scratch.width != 0) {
                if (r.x < scratch.x) {
                    g.drawLine(r.x, displayer.getHeight() - ins.bottom,
                               scratch.x - 1,
                               displayer.getHeight() - ins.bottom);
                }
                if (scratch.x + scratch.width < r.x + r.width) {
                    selEnd = scratch.x + scratch.width;
                    g.drawLine(selEnd, displayer.getHeight() - ins.bottom,
                               r.x + r.width,
                               displayer.getHeight() - ins.bottom);
                }
            } else {
                //The selected tab is scrolled offscreen
                g.drawLine (0, displayer.getHeight() - ins.bottom,
                        displayer.getWidth(), displayer.getHeight() - ins.bottom);
            }
            g.setColor(UIManager.getColor("controlDkShadow")); //NOI18N
            g.drawLine(selEnd, displayer.getHeight() - 5, displayer.getWidth(),
                       displayer.getHeight() - 5);
            return;
        }

        g.drawLine(r.x, displayer.getHeight() - ins.bottom, r.x + r.width,
                   displayer.getHeight() - ins.bottom);

        g.setColor(UIManager.getColor("controlDkShadow")); //NOI18N
        g.drawLine(0, displayer.getHeight() - 5, displayer.getWidth(),
                   displayer.getHeight() - 5);
    }

    protected LayoutManager createLayout() {
        return new MetalTabLayout();
    }

    protected AbstractButton[] createControlButtons() {
        JButton[] result = new JButton[3];
        result[0] = new TimerButton(scroll().getBackwardAction());
        result[1] = new TimerButton(scroll().getForwardAction());
        result[2] = new OnPressButton(new TabListPopupAction(displayer));
        configureButton(result[0], new LeftIcon());
        configureButton(result[1], new RightIcon());
        configureButton(result[2], new DownIcon());
        result[0].setPreferredSize(new Dimension(15, 17));
        //This button draws no left/right side border, so make it wider
        result[1].setPreferredSize(new Dimension(17, 17));
        result[2].setPreferredSize(new Dimension(17, 17));
        result[0].setBorder(new PartialEtchedBorder(false));
        result[1].setBorder(new SpecEtchedBorder(true, true));
        result[2].setBorder(new SpecEtchedBorder(true, true));
        scroll().getBackwardAction().putValue("control", displayer); //NOI18N //XXX huh?
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

    private class MetalTabLayout implements LayoutManager {

        public void addLayoutComponent(String name, java.awt.Component comp) {
        }

        public void layoutContainer(java.awt.Container parent) {
            Insets in = getTabAreaInsets();
            Component[] c = parent.getComponents();
            int x = parent.getWidth() - in.right + 3;
            int y = 0;
            Dimension psize;
            for (int i = 0; i < c.length; i++) {
                psize = c[i].getPreferredSize();
                y = in.top + 3; //hardcoded to spec
                int w = Math.min(psize.width, parent.getWidth() - x);
                c[i].setBounds(x, y, w, Math.min(psize.height,
                                                 parent.getHeight()));
                x += psize.width;
                if (i == 1) {
                    x += 3;
                }
            }
        }

        public Dimension minimumLayoutSize(java.awt.Container parent) {
            return getPreferredSize((JComponent) parent);
        }

        public Dimension preferredLayoutSize(java.awt.Container parent) {
            return getPreferredSize((JComponent) parent);
        }

        public void removeLayoutComponent(java.awt.Component comp) {
        }

    }

    private static final int ICON_WIDTH = 11;
    private static final int ICON_HEIGHT = 11;
    private static final int[] xpoints = new int[20];
    private static final int[] ypoints = new int[20];

    private static class LeftIcon implements Icon {
        public int getIconHeight() {
            return ICON_HEIGHT;
        }

        public int getIconWidth() {
            return ICON_WIDTH;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            y -= 2;
            g.setColor(c.isEnabled() ?
                       c.getForeground() :
                       UIManager.getColor("controlShadow")); //NOI18N
            int wid = getIconWidth();
            int hi = getIconHeight() + 1;
            xpoints[0] = x + (wid - 4);
            ypoints[0] = y + 1;

            xpoints[1] = xpoints[0];
            ypoints[1] = y + hi + 1;

            xpoints[2] = x + 2;
            ypoints[2] = y + (hi / 2) + 1;

            g.fillPolygon(xpoints, ypoints, 3);
        }
    }

    private static class RightIcon implements Icon {
        public int getIconWidth() {
            return ICON_WIDTH;
        }

        public int getIconHeight() {
            return ICON_HEIGHT - 2;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            y -= 2;
            g.setColor(c.isEnabled() ?
                       c.getForeground() :
                       UIManager.getColor("controlShadow")); //NOI18N
            int wid = getIconWidth();
            int hi = getIconHeight() + 1;
            xpoints[0] = x + 3; //x + (wid-4);
            ypoints[0] = y + 1;

            xpoints[1] = x + 3;
            ypoints[1] = y + hi + 1;

            xpoints[2] = x + (wid - 4) + 1;//x+2;
            ypoints[2] = y + (hi / 2) + 1;

            g.fillPolygon(xpoints, ypoints, 3);
        }
    }

    private static class DownIcon implements Icon {
        public int getIconHeight() {
            return ICON_HEIGHT;
        }

        public int getIconWidth() {
            return ICON_WIDTH + 3;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            y -= 2;
            x += 1;
            int wid = getIconWidth() - 2;
            if (wid % 2 == 0)
                wid--; //guarantee an odd number so lines are smooth
            int hi = getIconHeight();
            xpoints[0] = x + 1;
            ypoints[0] = y + (hi / 2);

            xpoints[1] = (x + wid) - 1;
            ypoints[1] = ypoints[0];

            xpoints[2] = (x + wid);
            ypoints[2] = ypoints[0];


            xpoints[3] = x + (wid / 2);
            ypoints[3] = (y + hi) - 1;
            g.setColor(c.isEnabled() ?
                       c.getForeground() :
                       UIManager.getColor("controlShadow")); //NOI18N
            g.fillPolygon(xpoints, ypoints, 4);
        }
    }

    private static class PartialEtchedBorder implements Border {
        private boolean leftSide = false;

        public PartialEtchedBorder(boolean leftSide) {
            this.leftSide = leftSide;
        }

        public Insets getBorderInsets(Component c) {
            Insets ins = new Insets(2, leftSide ? 0 : 2, 2, leftSide ? 2 : 0);
            return ins;
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            height--;
            g.setColor(UIManager.getColor("controlHighlight"));
            g.drawLine(leftSide ? x : x + 1, y + 1,
                       leftSide ? x + width - 3 : x + width - 1, y + 1);
            g.drawLine(leftSide ? x : x + 1, y + height, x + width, y + height);
            if (leftSide) {
                g.drawLine(x + width - 1, y + 1, x + width - 1, y + height);
            } else {
                g.drawLine(x + 1, y + 1, x + 1, y + height - 2);
            }
            g.setColor(UIManager.getColor("controlDkShadow"));
            g.drawLine(x, y, leftSide ? x + width - 2 : x + width - 1, y);
            g.drawLine(leftSide ? x : x + 2, y + height - 1,
                       leftSide ? x + width - 2 : x + width, y + height - 1);
            if (leftSide) {
                g.drawLine(x + width - 2, y + 2, x + width - 2, y + height - 2);
            } else {
                g.drawLine(x, y, x, y + height - 1);
            }
        }
    }

    /**
     * A slightly silly etched border class.  The UI spec leaves a few pixels
     * *un*painted which EtchedBorder paints by default, so in the interest if
     * pixel-for-pixel accuracy...
     */
    private static class SpecEtchedBorder implements Border {
        private boolean fillRight;
        private boolean fillLeft;

        public SpecEtchedBorder(boolean fillRight, boolean fillLeft) {
            this.fillRight = fillRight;
            this.fillLeft = fillLeft;
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(2, 2, 2, 2);
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            g.setColor(UIManager.getColor("controlHighlight")); //NOI18N
            g.drawLine(x + 1, y + 1, x + 1, y + height - 3);
            g.drawLine(x + 1, y + 1, x + width - 3, y + 1);
            g.drawLine(fillLeft ? x + 1 : x, y + height - 1, x + width,
                       y + height - 1);
            g.drawLine(x + width - 1, y + 1, x + width - 1,
                       y + height - (fillRight ? 0 : 3));

            g.setColor(UIManager.getColor("controlDkShadow")); //NOI18N
            g.drawLine(x, y, x + width + (fillRight ? -2 : -1), y);
            g.drawLine(x, y + (fillLeft ? 1 : 2), x,
                       y + height - (fillLeft ? 2 : 3));
            g.drawLine(x + 2, y + height - 2,
                       x + width - (fillRight ? 2 : 3), y + height - 2);
            g.drawLine(x + width - 2, y + 2, x + width - 2, y + height - 2);
        }
    }

}
