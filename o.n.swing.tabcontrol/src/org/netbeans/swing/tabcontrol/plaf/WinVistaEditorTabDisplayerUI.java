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

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.netbeans.swing.tabcontrol.TabDisplayer;

import javax.swing.plaf.ComponentUI;

/**
 * Windows Vista impl of tabs ui
 *
 * @author S. Aubrecht
 */
public final class WinVistaEditorTabDisplayerUI extends BasicScrollingTabDisplayerUI {
    
    private static final Rectangle scratch5 = new Rectangle();
    private static Map<Integer, String[]> buttonIconPaths;

    public WinVistaEditorTabDisplayerUI(TabDisplayer displayer) {
        super (displayer);
    }

    public static ComponentUI createUI(JComponent c) {
        return new WinVistaEditorTabDisplayerUI ((TabDisplayer) c);
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
    
    protected Rectangle getTabRectForRepaint( int tab, Rectangle rect ) {
        Rectangle res = super.getTabRectForRepaint( tab, rect );
        //we need to repaint extra vertical lines on both sides when mouse-over 
        //or selection changes
        res.x--;
        res.width += 2;
        return res;
    }

    private static void initIcons() {
        if( null == buttonIconPaths ) {
            buttonIconPaths = new HashMap<Integer, String[]>(7);
            
            //left button
            String[] iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/vista_scrollleft_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/vista_scrollleft_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/vista_scrollleft_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/vista_scrollleft_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SCROLL_LEFT_BUTTON, iconPaths );
            
            //right button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/vista_scrollright_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/vista_scrollright_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/vista_scrollright_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/vista_scrollright_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SCROLL_RIGHT_BUTTON, iconPaths );
            
            //drop down button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/vista_popup_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/vista_popup_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/vista_popup_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_DROP_DOWN_BUTTON, iconPaths );
            
            //maximize/restore button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/vista_maximize_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/vista_maximize_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/vista_maximize_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_MAXIMIZE_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/vista_restore_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = iconPaths[TabControlButton.STATE_DEFAULT];
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/vista_restore_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/vista_restore_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_RESTORE_BUTTON, iconPaths );
        }
    }

    public Icon getButtonIcon(int buttonId, int buttonState) {
        Icon res = null;
        initIcons();
        String[] paths = buttonIconPaths.get( buttonId );
        if( null != paths && buttonState >=0 && buttonState < paths.length ) {
            res = TabControlButtonFactory.getIcon( paths[buttonState] );
        }
        return res;
    }
}
