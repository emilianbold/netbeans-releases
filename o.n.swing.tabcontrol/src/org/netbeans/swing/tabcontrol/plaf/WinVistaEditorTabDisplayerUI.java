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
import java.awt.image.BufferedImage;
import java.net.URL;
import org.netbeans.swing.tabcontrol.TabListPopupAction;
import org.openide.util.Utilities;

/**
 * Windows Vista impl of tabs ui
 *
 * @author S. Aubrecht
 */
public final class WinVistaEditorTabDisplayerUI extends BasicScrollingTabDisplayerUI {
    private static final Rectangle scratch5 = new Rectangle();

    public WinVistaEditorTabDisplayerUI(TabDisplayer displayer) {
        super (displayer);
    }

    public static ComponentUI createUI(JComponent c) {
        return new WinVistaEditorTabDisplayerUI ((TabDisplayer) c);
    }    

    private static final String[] iconNames = new String[]{
        "org/netbeans/swing/tabcontrol/resources/vista_left_enabled.png", //NOI18N
        "org/netbeans/swing/tabcontrol/resources/vista_left_disabled.png", //NOI18N
        "org/netbeans/swing/tabcontrol/resources/vista_left_over.png", //NOI18N
        "org/netbeans/swing/tabcontrol/resources/vista_left_pressed.png", //NOI18N
        "org/netbeans/swing/tabcontrol/resources/vista_right_enabled.png", //NOI18N
        "org/netbeans/swing/tabcontrol/resources/vista_right_disabled.png", //NOI18N
        "org/netbeans/swing/tabcontrol/resources/vista_right_over.png", //NOI18N
        "org/netbeans/swing/tabcontrol/resources/vista_right_pressed.png", //NOI18N
        "org/netbeans/swing/tabcontrol/resources/vista_popup_enabled.png", //NOI18N
        "org/netbeans/swing/tabcontrol/resources/vista_popup_enabled.png", //NOI18N
        "org/netbeans/swing/tabcontrol/resources/vista_popup_over.png", //NOI18N
        "org/netbeans/swing/tabcontrol/resources/vista_popup_pressed.png"}; //NOI18N

    protected AbstractButton[] createControlButtons() {
        //XXX probably this can be moved into superclass?
        JButton[] result = new JButton[3];
        result[0] = new TimerButton(scroll().getBackwardAction(), false);
        result[1] = new TimerButton(scroll().getForwardAction(), false);
        result[2] = new OnPressButton(new TabListPopupAction(displayer));
        configureButton(result[0], 0);
        configureButton(result[1], 1);
        configureButton(result[2], 2);
        result[0].setPreferredSize(new Dimension(15, 15));
        result[2].setPreferredSize(new Dimension(16, 15));
        result[1].setPreferredSize(new Dimension(15, 15));

        scroll().getBackwardAction().putValue("control", displayer); //NOI18N
        scroll().getForwardAction().putValue("control", displayer); //NOI18N

        return result;
    }

    public Dimension getPreferredSize(JComponent c) {
        int prefHeight = 22;
        Graphics g = BasicScrollingTabDisplayerUI.getOffscreenGraphics();
        if (g != null) {
            FontMetrics fm = g.getFontMetrics(displayer.getFont());
            Insets ins = getTabAreaInsets();
            prefHeight = fm.getHeight() + ins.top + ins.bottom + 6;
        }
        return new Dimension(displayer.getWidth(), prefHeight);
    }
    
    private static final Icon createIcon(int i) {
        return new ImageIcon(Utilities.loadImage(iconNames[i]));
    }

    private static void configureButton(JButton button, int idx) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder());

        Icon normal = createIcon((idx * 4));
        Icon disabled = createIcon((idx * 4) + 1);
        Icon rollover = createIcon((idx * 4) + 2);
        Icon pressed = createIcon((idx * 4) + 3);

        button.setIcon(normal);
        button.setRolloverEnabled(true);
        button.setRolloverIcon(rollover);
        button.setDisabledIcon(disabled);
        button.setPressedIcon(pressed);

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

        int y = displayer.getHeight() - WinVistaEditorTabCellRenderer.BOTTOM_INSET;
        int tabsWidth = getTabsAreaWidth();

        g.setColor(WinVistaEditorTabCellRenderer.getBorderColor());
        
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
        return new WinVistaEditorTabCellRenderer();
    }

    protected LayoutManager createLayout() {
        return new WCLayout();
    }

    public java.awt.Insets getTabAreaInsets() {
        return new Insets(0, 0, 0, 57);
    }
    
    protected Rectangle getTabRectForRepaint( int tab, Rectangle rect ) {
        Rectangle res = super.getTabRectForRepaint( tab, rect );
        //we need to repaint extra vertical lines on both sides when mouse-over 
        //or selection changes
        res.x--;
        res.width += 2;
        return res;
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
