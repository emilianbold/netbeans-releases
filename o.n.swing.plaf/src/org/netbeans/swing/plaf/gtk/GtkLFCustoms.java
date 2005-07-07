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

package org.netbeans.swing.plaf.gtk;

import org.netbeans.swing.plaf.LFCustoms;
import org.netbeans.swing.plaf.metal.MetalLFCustoms;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import org.netbeans.swing.plaf.util.UIBootstrapValue;
import org.netbeans.swing.plaf.util.UIUtils;

/** UI customizations for GTK look and feel
 *
 * @author  Tim Boudreau  
 */
public class GtkLFCustoms extends LFCustoms {
    private Object light = new ThemeValue (ThemeValue.REGION_PANEL, ThemeValue.WHITE, Color.GRAY);
    private Object control = new ThemeValue (ThemeValue.REGION_PANEL, ThemeValue.MID, Color.GRAY);
    private Object controlFont = new ThemeValue (ThemeValue.REGION_TAB, new FontUIResource ("Dialog", Font.PLAIN, 11)); //NOI18N
    

    //Background colors for winsys tabs


    public Object[] createApplicationSpecificKeysAndValues () {
        Border lowerBorder = new AdaptiveMatteBorder (false, true, true, true, 3);
        //Avoid using ThemeValue if it can't work - mainly due to testing issues when trying to run GTK UI customizations
        //on the Mac, which doesn't have a GTKLookAndFeel

        Object selBg = ThemeValue.functioning() ? new ThemeValue (ThemeValue.REGION_BUTTON, ThemeValue.DARK, Color.CYAN) : (Object) Color.CYAN;
        Object selFg = ThemeValue.functioning() ? new ThemeValue (ThemeValue.REGION_BUTTON, ThemeValue.TEXT_FOREGROUND, Color.BLACK) : (Object) Color.BLACK;

        Object fb = new Color (144, 144, 255);
        Object tabBg = ThemeValue.functioning() ? new ThemeValue (ThemeValue.REGION_INTFRAME, ThemeValue.DARK, fb) : (Object) fb;
        
        if (!ThemeValue.functioning()) {
            Integer i = (Integer) UIManager.get("customFontSize"); //NOI18N
            int sz = 11;
            if (i != null) {
                sz = i.intValue();
            }
            controlFont = new Font ("Dialog", Font.PLAIN, sz); //NOI18N
        }

        Object[] result = {
            PROPSHEET_SELECTION_BACKGROUND, selBg,
            PROPSHEET_SELECTION_FOREGROUND, selFg,
            PROPSHEET_SELECTED_SET_BACKGROUND, selBg,
            PROPSHEET_SELECTED_SET_FOREGROUND, selFg,
            PROPSHEET_BUTTON_COLOR, selFg,
            
            PROPSHEET_SET_BACKGROUND, ThemeValue.functioning() ? (Object) control : (Object) Color.CYAN,
            PROPSHEET_DISABLED_FOREGROUND, new Color(161,161,146),
            "Table.selectionBackground", selBg, //NOI18N
            "Table.selectionForeground", selFg, //NOI18N
            PROPSHEET_BACKGROUND, Color.WHITE,
            "window", light,
            
            VIEW_TAB_OUTER_BORDER, BorderFactory.createEmptyBorder(),
            VIEW_TAB_TABS_BORDER, new PartialEdgeBorder(4),
            VIEW_TAB_CONTENT_BORDER, lowerBorder,
            EDITOR_TAB_OUTER_BORDER, BorderFactory.createEmptyBorder(),
            EDITOR_TAB_CONTENT_BORDER, lowerBorder,
            EDITOR_TAB_TABS_BORDER, BorderFactory.createEmptyBorder(),
            
            EDITOR_STATUS_LEFT_BORDER, new InsetBorder (false, true),
            EDITOR_STATUS_RIGHT_BORDER, new InsetBorder (true, false),
            EDITOR_STATUS_ONLYONEBORDER, new InsetBorder (true, true),
            EDITOR_STATUS_INNER_BORDER, BorderFactory.createEmptyBorder(),
            
            
            OUTPUT_BACKGROUND, control,
            OUTPUT_HYPERLINK_FOREGROUND, selFg,
            OUTPUT_SELECTION_BACKGROUND, selBg,
            
            "controlFont", controlFont, //NOI18N

            //UI Delegates for the tab control
            EDITOR_TAB_DISPLAYER_UI, 
                "org.netbeans.swing.tabcontrol.plaf.WinClassicEditorTabDisplayerUI", //NOI18N
            VIEW_TAB_DISPLAYER_UI, 
                "org.netbeans.swing.tabcontrol.plaf.WinClassicViewTabDisplayerUI", //NOI18N
            SLIDING_TAB_BUTTON_UI, "org.netbeans.swing.tabcontrol.plaf.SlidingTabDisplayerButtonUI", //NOI18N
            SLIDING_BUTTON_UI, "org.netbeans.swing.tabcontrol.plaf.GtkSlidingButtonUI", //NOI18N

            DESKTOP_BACKGROUND, ThemeValue.functioning() ? new ThemeValue (ThemeValue.REGION_BUTTON, ThemeValue.LIGHT, Color.GRAY) : (Object) Color.GRAY,
            EXPLORER_MINISTATUSBAR_BORDER, BorderFactory.createEmptyBorder(),

            TOOLBAR_UI, "org.netbeans.swing.plaf.gtk.GtkToolbarUI", //NOI18N
                    
            PROGRESS_CANCEL_BUTTON_ICON, UIUtils.loadImage("org/netbeans/swing/plaf/resources/cancel_task_linux_mac.png"),
            "winclassic_tab_sel_gradient", tabBg,
        };
        return result;
    }
    
    public Object[] createLookAndFeelCustomizationKeysAndValues() {
        if (ThemeValue.functioning()) {
            return new Object[] {
                //XXX once the JDK team has integrated support for standard
                //UIManager keys into 1.5 (not there as of b47), these can 
                //probably be deleted, resulting in a performance improvement:
                "control", control,
                "controlHighlight", new ThemeValue (ThemeValue.REGION_PANEL, ThemeValue.LIGHT, Color.LIGHT_GRAY), //NOI18N
                "controlShadow", new ThemeValue (ThemeValue.REGION_PANEL, ThemeValue.DARK, Color.DARK_GRAY), //NOI18N
                "controlDkShadow", new ThemeValue (ThemeValue.REGION_PANEL, ThemeValue.BLACK, Color.BLACK), //NOI18N
                "controlLtHighlight", new ThemeValue (ThemeValue.REGION_PANEL, ThemeValue.WHITE, Color.WHITE), //NOI18N
                "textText", new ThemeValue (ThemeValue.REGION_PANEL, ThemeValue.TEXT_FOREGROUND, Color.BLACK), //NOI18N
                "text", new ThemeValue (ThemeValue.REGION_PANEL, ThemeValue.TEXT_BACKGROUND, Color.GRAY), //NOI18N
                
                "tab_unsel_fill", control, //NOI18N
                 
                "SplitPane.dividerSize", new Integer (2),  //NOI18N
                
                SYSTEMFONT, controlFont, //NOI18N
                USERFONT, controlFont, //NOI18N
                MENUFONT, controlFont, //NOI18N
                LISTFONT, controlFont, //NOI18N
                "Label.font", controlFont, //NOI18N
                "Panel.font", controlFont, //NOI18N

                "Tree.expandedIcon", new GTKExpandedIcon(), //NOI18N
                "Tree.collapsedIcon", new GTKCollapsedIcon() //NOI18N
            };
        } else {
            Object[] result = new Object[] {
                TOOLBAR_UI, new UIDefaults.ProxyLazyValue("org.netbeans.swing.plaf.gtk.GtkToolbarUI"), //NOI18N
            };
            return result;
        }
    }

    /** Temporary workaround for GTK L&F */
    private static abstract class GTKIcon implements Icon {
        private static final int SIZE = 11;
        public int getIconWidth() {
            return GTKIcon.SIZE;
        }
        
        public int getIconHeight() {
            return GTKIcon.SIZE;
        }
    }

    /**
     * Temporary workaround for GTK L&F - they provide an icon which does not
     * know its width or height until it has been painted.  So for it to work
     * correctly, we have this silliness.
     */
    private static final class GTKCollapsedIcon extends GTKIcon {
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.translate(x, y);
            int mid, height, thick, i, j, up, down;
            int size = Math.min(getIconWidth(),getIconHeight());
            mid = (size / 2);
            height = size / 2 + 1;
            thick = Math.max(1, size / 7);
            
            i = size / 2 - height / 2 - 1;
            
            // Fill in the background of the expander icon.
            g.setColor((Color) UIManager.get("Button.background"));
            for (j = height - 1; j > 0; j--) {
                g.drawLine(i, mid - j + 1, i, mid + j - 1);
                i++;
            }
            
            g.setColor((Color) UIManager.get("Button.foreground"));
            i = size / 2 - height / 2 - 1;
            down = thick - 1;
            // Draw the base of the triangle.
            for (up = 0; up < thick; up++) {
                g.drawLine(i + up, 0 - down, i + up, size + down);
                down--;
            }
            i++;
            
            // Paint sides of triangle.
            for (j = height - 1; j > 0; j--) {
                for (up = 0; up < thick; up++) {
                    g.drawLine(i, mid - j + 1 - up, i, mid - j + 1 - up);
                    g.drawLine(i, mid + j - 1 + up, i, mid + j - 1 + up);
                }
                i++;
            }
            
            // Paint remainder of tip if necessary.
            if (thick > 1) {
                for (up = thick - 2; up >= 0; up--) {
                    g.drawLine(i, mid - up, i, mid + up);
                    i++;
                }
            }
            
            g.translate(-x, -y);
        }
    }

    /**
     * Temporary workaround for GTK L&F - they provide an icon which does not
     * know its width or height until it has been painted.  So for it to work
     * correctly, we have this silliness.
     */
    private static final class GTKExpandedIcon extends GTKIcon {
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.translate(x, y);
            int mid, height, thick, i, j, up, down;
            int size = Math.min(getIconWidth(),getIconHeight());
            mid = (size / 2);
            height = size / 2 + 1;
            thick = Math.max(1, size / 7);
 
            j = size / 2 - height / 2 - 1;
            // Fill in the background of the expander icon.
            g.setColor((Color) UIManager.get("Button.background"));
            for (i = height - 1; i > 0; i--) {
                g.drawLine(mid - i + 1, j, mid + i - 1, j);
                j++;
            }

            g.setColor((Color) UIManager.get("Button.foreground"));
            j = size / 2 - height / 2 - 1;
            down = thick - 1;
            // Draw the base of the triangle.
            for (up = 0; up < thick; up++) {
                g.drawLine(0 - down, j + up, size + down, j + up);
                down--;
            }
            j++;

            // Paint sides of triangle.
            for (i = height - 1; i > 0; i--) {
                for (up = 0; up < thick; up++ ) {
                    g.drawLine(mid - i + 1 - up, j, mid - i + 1 - up, j);
                    g.drawLine(mid + i - 1 + up, j, mid + i - 1 + up, j);
                }
                j++;
            }

            // Paint remainder of tip if necessary.
            if (thick > 1) {
                for (up = thick - 2; up >= 0; up--) {
                    g.drawLine(mid - up, j, mid + up, j);
                    j++;
                }
            }
             
            g.translate(-x, -y);
        }
    }
}
