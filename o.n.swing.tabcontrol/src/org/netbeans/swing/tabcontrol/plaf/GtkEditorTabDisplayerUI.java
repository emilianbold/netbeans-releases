/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.swing.tabcontrol.plaf;

import org.netbeans.swing.tabcontrol.TabDisplayer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseWheelEvent;
import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * A provisional look and feel for GTK.
 *
 * @author Tim Boudreau
 */
public class GtkEditorTabDisplayerUI extends BasicScrollingTabDisplayerUI {
    private Insets taInsets = new Insets(0, 0, 1, 55);
    /** Color used in drawing the line behind the tabs */
    private Color lineMiddleColor = null;
    /** Color used in drawing the line behind the tabs */
    private Color lineHlColor = null;


    public GtkEditorTabDisplayerUI(TabDisplayer displayer) {
        super (displayer);
    }

    public void install() {
        super.install();
        displayer.setOpaque(false);
    }
    
    protected boolean isAntialiased() {
        return true;
    }
    
    protected Font createFont() {
        //Easiest way to track theme font changes - let it get the font from
        //its parent
        return null;
    }
    
    protected TabCellRenderer createDefaultRenderer() {
        return new GtkEditorTabCellRenderer();
    }

    protected LayoutManager createLayout() {
        return new OSXTabLayout();
    }

    public Insets getTabAreaInsets() {
        return taInsets;
    }
    
    public static ComponentUI createUI(JComponent c) {
        return new GtkEditorTabDisplayerUI ((TabDisplayer) c);
    }

    protected int createRepaintPolicy () {
        return TabState.REPAINT_ALL_TABS_ON_ACTIVATION_CHANGE
                | TabState.REPAINT_ON_SELECTION_CHANGE
                | TabState.REPAINT_ON_MOUSE_ENTER_TAB
                | TabState.REPAINT_ON_MOUSE_PRESSED;
    }

    protected void processMouseWheelEvent(MouseWheelEvent e) {
        //overridden to repaint the arrow buttons if the selected tab moves into
        //or out of view
        boolean wasShowing = selectionModel.getSelectedIndex()
                >= getFirstVisibleTab() && selectionModel.getSelectedIndex()
                <= getLastVisibleTab();

        super.processMouseWheelEvent(e);

        boolean stillShowing = selectionModel.getSelectedIndex()
                >= getFirstVisibleTab() && selectionModel.getSelectedIndex()
                <= getLastVisibleTab();

        if (wasShowing != stillShowing) {
            Component[] c = displayer.getComponents();
            for (int i = 0; i < c.length; i++) {
                c[i].repaint();
            }
        }
    }

    public Dimension getPreferredSize(JComponent c) {
        int prefHeight = 28;
        //Never call getGraphics() on the control, it resets in-process
        //painting on OS-X 1.4.1 and triggers gratuitous repaints
        Graphics g = TabListPopup.getOffscreenGraphics();
        if (g != null) {
            FontMetrics fm = g.getFontMetrics(displayer.getFont());
            Insets ins = getTabAreaInsets();
            prefHeight = fm.getHeight() + ins.top + ins.bottom + 7;
        }
        if (prefHeight % 2 == 0) {
            prefHeight += 1;
        }
        return new Dimension(displayer.getWidth(), prefHeight);
    }

    private static final int BUTTON_SIZE = 16;

    protected AbstractButton[] createControlButtons() {
        JButton[] result = new JButton[3];
        result[0] = new TimerButton(scroll().getBackwardAction());
        result[1] = new TimerButton(scroll().getForwardAction());
        result[2] = new OnPressButton(new TabListPopupAction(displayer));
        configureButton(result[0], new LeftIcon());
        configureButton(result[1], new RightIcon());
        configureButton(result[2], new DownIcon());

      result[0].setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        result[1].setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));
        result[2].setPreferredSize(new Dimension(BUTTON_SIZE, BUTTON_SIZE));

        scroll().getBackwardAction().putValue("control", displayer); //NOI18N
        scroll().getForwardAction().putValue("control", displayer); //NOI18N

        return result;
    }

    private static void configureButton(JButton button, Icon icon) {
        //        button.setMargin(null);
        button.setText(null);
        //undocumented (?) call to hide action text - see JButton line 234
        button.putClientProperty("hideActionText", Boolean.TRUE); //NOI18N
        button.setFocusable(false);
        button.setRolloverEnabled(false);
        button.setIcon(icon);
    }

    private Rectangle scratch = new Rectangle();
    protected void paintAfterTabs(Graphics g) {
        //int idx = getLastVisibleTab();
        //getTabRect(idx, scratch);
        //int xpos = scratch.x + scratch.width;
        int xpos = 0;
        int ypos = displayer.getHeight() - 1;
        g.setColor (UIManager.getColor("controlShadow")); //NOI18N
        g.drawLine (xpos, ypos, displayer.getWidth() - 5, ypos); //-4 so we're not over the drop shadow
    }
    
    private class OSXTabLayout implements LayoutManager {

        public void addLayoutComponent(String name, java.awt.Component comp) {
        }

        public void layoutContainer(java.awt.Container parent) {
            Insets in = getTabAreaInsets();
            Component[] c = parent.getComponents();
            int x = parent.getWidth() - in.right + 3;
            int y = 0;
            Dimension psize;

            int centerY = (((displayer.getHeight() - (GtkEditorTabCellRenderer.TOP_INSET
                    + GtkEditorTabCellRenderer.BOTTOM_INSET)) / 2) + GtkEditorTabCellRenderer.TOP_INSET)
                    + getTabAreaInsets().top;

            for (int i = 0; i < c.length; i++) {
                psize = c[i].getPreferredSize();
                y = centerY - (psize.height / 2);
                int w = Math.min(psize.width, parent.getWidth() - x);
                c[i].setBounds(x, y, w, Math.min(psize.height,
                                                 parent.getHeight()));
                x += psize.width;
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
    private static final int ICON_HEIGHT = 8;
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
            y-=2;
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

    private static class RightIcon extends LeftIcon {
        public int getIconWidth() {
            return ICON_WIDTH;
        }

        public int getIconHeight() {
            return ICON_HEIGHT - 1;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            y-=2;
            g.setColor(c.isEnabled() ?
                       c.getForeground() :
                       UIManager.getColor("controlShadow")); //NOI18N
            int wid = getIconWidth();
            int hi = getIconHeight() + 1;
            xpoints[0] = x + 3; //x + (wid-4);
            ypoints[0] = y;

            xpoints[1] = x + 3;
            ypoints[1] = y + hi + 1;

            xpoints[2] = x + (wid - 4) + 1;//x+2;
            ypoints[2] = y + (hi / 2);

            g.fillPolygon(xpoints, ypoints, 3);

        }
    }

    private static class DownIcon extends LeftIcon {
        public int getIconHeight() {
            return ICON_HEIGHT + 2;
        }

        public int getIconWidth() {
            return ICON_WIDTH;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            y-=2;

            int wid = getIconWidth();
            if (wid % 2 == 0)
                wid--; //guarantee an odd number so lines are smooth
            int hi = getIconHeight();
            xpoints[0] = x;
            ypoints[0] = y + (hi / 2);

            xpoints[1] = (x + wid) - 2;
            ypoints[1] = y + (hi / 2);


            xpoints[2] = x + (wid / 2) - 1;
            ypoints[2] = (y + hi);
            g.setColor(c.isEnabled() ?
                       c.getForeground() :
                       UIManager.getColor("controlShadow")); //NOI18N
            g.fillPolygon(xpoints, ypoints, 3);

        }
    }
}
