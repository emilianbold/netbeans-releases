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
/*
 * MetalEditorTabDisplayerUI.java
 *
 * Created on December 2, 2003, 9:40 PM
 */

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import javax.swing.plaf.ComponentUI;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;

/**
 * A provisional look and feel for OS-X, round 2, using Java2d to emulate the
 * aqua look.
 *
 * @author Tim Boudreau
 */
public class AquaEditorTabDisplayerUI extends BasicScrollingTabDisplayerUI {

    /** Color used in drawing the line behind the tabs */
    private Color lineMiddleColor = null;
    /** Color used in drawing the line behind the tabs */
    private Color lineHlColor = null;

    private static Map<Integer, String[]> buttonIconPaths;

    public AquaEditorTabDisplayerUI (TabDisplayer displayer) {
        super (displayer);
    }

    public void install() {
        super.install();
        scroll().setMinimumXposition(9);
    }

    protected TabCellRenderer createDefaultRenderer() {
        return new AquaEditorTabCellRenderer();
    }

    public Insets getTabAreaInsets() {
        Insets result = super.getTabAreaInsets();
        result.bottom = 2;
        return result;
    }
    
    public static ComponentUI createUI(JComponent c) {
        return new AquaEditorTabDisplayerUI ((TabDisplayer) c);
    }

    protected boolean isAntialiased() {
        return true;
    }
    
    protected Font createFont() {
        return UIManager.getFont("Label.font"); //NOI18N
    }    
    
    protected int createRepaintPolicy () {
        return TabState.REPAINT_SELECTION_ON_ACTIVATION_CHANGE
                | TabState.REPAINT_ON_SELECTION_CHANGE
                | TabState.REPAINT_ALL_ON_MOUSE_ENTER_TABS_AREA
                | TabState.REPAINT_ON_MOUSE_ENTER_CLOSE_BUTTON
                | TabState.REPAINT_ON_CLOSE_BUTTON_PRESSED
                | TabState.REPAINT_ON_MOUSE_PRESSED;
    }
    
    public Dimension getPreferredSize(JComponent c) {
        int prefHeight = 28;
        //Never call getGraphics() on the control, it resets in-process
        //painting on OS-X 1.4.1 and triggers gratuitous repaints
        Graphics g = BasicScrollingTabDisplayerUI.getOffscreenGraphics();
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

    protected void paintAfterTabs(Graphics g) {
        //Draw the continuation of the rounded border behind the buttons
        //and tabs
        
        int centerY = (((displayer.getHeight() - 
            (AquaEditorTabCellRenderer.TOP_INSET + AquaEditorTabCellRenderer.BOTTOM_INSET)) / 2) 
            + AquaEditorTabCellRenderer.TOP_INSET - 1) + getTabAreaInsets().top + 1;
        
        if (lineMiddleColor == null) {
            lineMiddleColor = ColorUtil.getMiddle(UIManager.getColor("controlShadow"), 
            UIManager.getColor("control")); //NOI18N
        }
        g.setColor (lineMiddleColor);
        
        int rightLineStart = getTabsAreaWidth() - 13;
        int rightLineEnd = displayer.getWidth() - 9;
        
        if (displayer.getModel().size() > 0 && !scroll().isLastTabClipped()) {
            //Extend the line out to the edge of the last visible tab
            //if none are clipped
            int idx = scroll().getLastVisibleTab(displayer.getWidth());
            rightLineStart = scroll().getX(idx) + scroll().getW(idx);
        } else if (displayer.getModel().size() == 0) {
            rightLineStart = 6;
        }
        
        if (scroll().getOffset() >= 0) {
            //fill the left edge notch
            g.drawLine(6, centerY, 11, centerY);
        }
        g.drawLine(rightLineStart, centerY, rightLineEnd, centerY);
        
        if (lineHlColor == null) {
            lineHlColor = ColorUtil.getMiddle (lineMiddleColor, 
            UIManager.getColor("control"));
        }
        
        g.setColor (lineHlColor); //NOI18N
        g.drawLine(rightLineStart, centerY+1, rightLineEnd, centerY+1);
        if (scroll().getOffset() > 0) {
            //fill the left edge notch
            g.drawLine(6, centerY+1, 11, centerY+1);
        }
    }
    

    private static void initIcons() {
        if( null == buttonIconPaths ) {
            buttonIconPaths = new HashMap<Integer, String[]>(7);
            
            //left button
            String[] iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_scrollleft_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/mac_scrollleft_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_scrollleft_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_scrollleft_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SCROLL_LEFT_BUTTON, iconPaths );
            
            //right button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_scrollright_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/mac_scrollright_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_scrollright_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_scrollright_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_SCROLL_RIGHT_BUTTON, iconPaths );
            
            //drop down button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_popup_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/mac_popup_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_popup_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_popup_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_DROP_DOWN_BUTTON, iconPaths );
            
            //maximize/restore button
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_maximize_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/mac_maximize_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_maximize_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_maximize_pressed.png"; // NOI18N
            buttonIconPaths.put( TabControlButton.ID_MAXIMIZE_BUTTON, iconPaths );
            
            iconPaths = new String[4];
            iconPaths[TabControlButton.STATE_DEFAULT] = "org/netbeans/swing/tabcontrol/resources/mac_restore_enabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_DISABLED] = "org/netbeans/swing/tabcontrol/resources/mac_restore_disabled.png"; // NOI18N
            iconPaths[TabControlButton.STATE_ROLLOVER] = "org/netbeans/swing/tabcontrol/resources/mac_restore_rollover.png"; // NOI18N
            iconPaths[TabControlButton.STATE_PRESSED] = "org/netbeans/swing/tabcontrol/resources/mac_restore_pressed.png"; // NOI18N
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

    protected Rectangle getControlButtonsRectangle(Container parent) {
        int centerY = (((displayer.getHeight() - (AquaEditorTabCellRenderer.TOP_INSET
                + AquaEditorTabCellRenderer.BOTTOM_INSET)) / 2) + AquaEditorTabCellRenderer.TOP_INSET)
                + getTabAreaInsets().top;
        
        int width = parent.getWidth();
        int height = parent.getHeight();
        int buttonsWidth = getControlButtons().getWidth();
        int buttonsHeight = getControlButtons().getHeight();
        return new Rectangle( width-buttonsWidth-3, centerY-buttonsHeight/2, buttonsWidth, buttonsHeight );
    }
}
