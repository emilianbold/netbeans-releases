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
 * WinXPEditorTabDisplayerUI.java
 *
 * Created on 09 December 2003, 16:53
 */

package org.netbeans.swing.tabcontrol.plaf;

import org.netbeans.swing.tabcontrol.TabDisplayer;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import org.netbeans.swing.tabcontrol.TabListPopupAction;

/**
 * Windows xp impl of tabs ui
 *
 * @author Tim Boudreau
 */
public final class WinXPEditorTabDisplayerUI extends BasicScrollingTabDisplayerUI {
    protected static final int[] xpoints = new int[20];
    protected static final int[] ypoints = new int[20];
    private static final Rectangle scratch5 = new Rectangle();

    public WinXPEditorTabDisplayerUI(TabDisplayer displayer) {
        super (displayer);
    }

    public static ComponentUI createUI(JComponent c) {
        return new WinXPEditorTabDisplayerUI ((TabDisplayer) c);
    }    

    public void install() {
        super.install();
//        Color col = UIManager.getColor("nb_workplace_fill"); //NOI18N
//        if (col == null) {
//            col = new Color(226, 223, 214);
//        }
//        displayer.setBackground(col);
    }

    private static final String[] iconNames = new String[]{
        "org/netbeans/swing/tabcontrol/resources/xp-right-enabled.gif",
        "org/netbeans/swing/tabcontrol/resources/xp-right-disabled.gif",
        "org/netbeans/swing/tabcontrol/resources/xp-right-enabled-selected.gif",
        "org/netbeans/swing/tabcontrol/resources/xp-left-enabled.gif",
        "org/netbeans/swing/tabcontrol/resources/xp-left-disabled.gif",
        "org/netbeans/swing/tabcontrol/resources/xp-left-enabled-selected.gif",
        "org/netbeans/swing/tabcontrol/resources/xp-down-enabled.gif",
        "org/netbeans/swing/tabcontrol/resources/xp-down-disabled.gif",
        "org/netbeans/swing/tabcontrol/resources/xp-down-enabled-selected.gif"}; //NOI18N

    protected AbstractButton[] createControlButtons() {
        //XXX probably this can be moved into superclass?
        JButton[] result = new JButton[3];
        result[0] = new TimerButton(scroll().getBackwardAction());
        result[1] = new TimerButton(scroll().getForwardAction());
        result[2] = new OnPressButton(new TabListPopupAction(displayer));
        configureButton(result[0], 0);
        configureButton(result[2], 1);
        configureButton(result[1], 2);
        result[0].setPreferredSize(new Dimension(15, 14));
        result[2].setPreferredSize(new Dimension(16, 14));
        result[1].setPreferredSize(new Dimension(15, 14));

        scroll().getBackwardAction().putValue("control", displayer); //NOI18N
        scroll().getForwardAction().putValue("control", displayer); //NOI18N

        return result;
    }

    public Dimension getPreferredSize(JComponent c) {
        int prefHeight = 24;
        Graphics g = BasicScrollingTabDisplayerUI.getOffscreenGraphics();
        if (g != null) {
            FontMetrics fm = g.getFontMetrics(displayer.getFont());
            Insets ins = getTabAreaInsets();
            prefHeight = fm.getHeight() + ins.top + ins.bottom + 8;
        }
        return new Dimension(displayer.getWidth(), prefHeight);
    }
    
    private static final Icon createIcon(int i) {
        return new ImageIcon(loadImage(iconNames[i]));
    }

    private static final Image loadImage(String id) {
        //XXX for testing, so we don't lug lookup, etc. into memory
        try {
            URL url = WinXPEditorTabDisplayerUI.class.getClassLoader()
                    .getResource(id);
            return ImageIO.read(url);
        } catch (Exception e) {
            e.printStackTrace();
            return new BufferedImage(12, 12, BufferedImage.TYPE_INT_ARGB);
        }
    }

    private static final Dimension controlButtonSize(int index) {
        Dimension result = new Dimension(index == 1 ? 13 : 14, 15);
        return result;
    }


    private static void configureButton(JButton button, int idx) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);

        Icon normal = createIcon((idx * 3));
        Icon disabled = createIcon((idx * 3) + 1);
        Icon rollover = createIcon((idx * 3) + 2);

        button.setIcon(normal);
        button.setRolloverEnabled(true);
        button.setRolloverIcon(rollover);
        button.setDisabledIcon(disabled);
        button.setPreferredSize(new Dimension(normal.getIconWidth() + 1,
                                              normal.getIconHeight() + 1));

        button.setMargin(null);
        button.setText(null);
        //undocumented (?) call to hide action text - see JButton line 234
        button.putClientProperty("hideActionText", Boolean.TRUE); //NOI18N
        button.setFocusable(false);
    }
    
    public void paintBackground (Graphics g) {
        g.setColor (displayer.getBackground());
        g.fillRect (0, 0, displayer.getWidth(), displayer.getHeight());
    }

    protected void paintAfterTabs(Graphics g) {
        Rectangle r = new Rectangle();
        getTabsVisibleArea(r);
        r.width = displayer.getWidth();

        Insets ins = getTabAreaInsets();

        int y = displayer.getHeight() - WinXPEditorTabCellRenderer.BOTTOM_INSET;
        //Draw the fill line that will be under the white highlight line - this
        //goes across the whole component.
        int selEnd = 0;
        int i = selectionModel.getSelectedIndex();
        g.setColor(WinXPEditorTabCellRenderer.getSelectedTabBottomLineColor());
        g.drawLine(0, y + 1, displayer.getWidth(), y + 1);
        
        //Draw the white highlight under all tabs but the selected one:
        
        //Check if we will need to draw a white line from the left edge to the 
        //selection, and another from the left edge of the selection to the
        //end of the control, skipping the selection area.  If the selection is
        //visible we should skip it.
        int tabsWidth = getTabsAreaWidth();
        boolean needSplitLine = i != -1 && ((i
                < scroll().getLastVisibleTab(tabsWidth) || i
                <= scroll().getLastVisibleTab(tabsWidth)
                && !scroll().isLastTabClipped())
                && i >= scroll().getFirstVisibleTab(tabsWidth));

        g.setColor(UIManager.getColor("controlLtHighlight"));
        if (needSplitLine) {
            //Find the rectangle of the selection to skip it
            getTabRect(i, scratch5);
            //Make sure it's not offscreen
            if (scratch5.width != 0) {
                //draw the first part of the line
                if (r.x < scratch5.x) {
                    g.drawLine(r.x, y, scratch5.x + 1, y);
                }
                //Now draw the second part out to the right edge
                if (scratch5.x + scratch5.width < r.x + r.width) {
                    //Find the right edge of the selected tab rectangle
                    selEnd = scratch5.x + scratch5.width;
                    //If the last tab is not clipped, the final tab is one
                    //pixel smaller; we need to overwrite one pixel of the
                    //border or there will be a small stub sticking down
                    if (!scroll().isLastTabClipped()) {
                        selEnd--;
                    }
                    //Really draw the second part, now that we know where to
                    //start
                    g.drawLine(selEnd, y, r.x + r.width, y);
                }
            }
        } else {
            //The selection is not visible - draw the white highlight line
            //across the entire width of the container
            g.drawLine(r.x, y, r.x + r.width, y);
        }

        //Draw the left and right edges so the area below the tabs looks 
        //closed
        g.setColor(WinXPEditorTabCellRenderer.getBorderColor());
        g.drawLine(0, y - 1, 0, displayer.getHeight());
        g.drawLine(displayer.getWidth() - 1, y - 1, displayer.getWidth() - 1,
                   displayer.getHeight());
        
        //Draw a line tracking the bottom of the tabs under the control
        //buttons, out to the right edge of the control
        
        //Find the last visible tab
        int last = scroll().getLastVisibleTab(tabsWidth);
        int l = 0;
        if (last >= 0) {
            //If it's onscreen (usually will be unless there are no tabs,
            //find the edge of the last tab - it may be scrolled)
            getTabRect(last, scratch5);
            last = scratch5.x + scratch5.width;
        }
        //Draw the dark line under the controls button area that closes the
        //tabs bottom margin on top
        g.drawLine(last, y - 1, displayer.getWidth(), y - 1);
    }

    protected TabCellRenderer createDefaultRenderer() {
        return new WinXPEditorTabCellRenderer();
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
            int x = parent.getWidth() - 49;
            int y = 0;
            Dimension psize;
            for (int i = 0; i < c.length; i++) {
                y = in.top;
                if (c[i] instanceof JButton) {
                    int w = Math.min(
                            ((JButton) c[i]).getIcon().getIconWidth(),
                            parent.getWidth() - x);
                    c[i].setBounds(x, y, w, Math.min(
                            ((JButton) c[i]).getIcon().getIconHeight(),
                            parent.getHeight()));
                    x += ((JButton) c[i]).getIcon().getIconWidth();
                    if (i == 1) {
                        x += 3;
                    }
                }
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
}
