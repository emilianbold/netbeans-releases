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
/*
 * GtkLfCustoms.java
 *
 * Created on March 14, 2004, 1:13 AM
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
        
        Border lowerBorder = new AdaptiveMatteBorder (false, true, true, true, 4);
        boolean blueprint = UIUtils.isBlueprintTheme();

        //Avoid using ThemeValue if it can't work - mainly due to testing issues when trying to run GTK UI customizations
        //on the Mac, which doesn't have a GTKLookAndFeel

        Object selBg = ThemeValue.functioning() ? new ThemeValue (ThemeValue.REGION_BUTTON, ThemeValue.DARK, Color.CYAN) : (Object) Color.CYAN;
        Object selFg = ThemeValue.functioning() ? new ThemeValue (ThemeValue.REGION_BUTTON, ThemeValue.TEXT_FOREGROUND, Color.BLACK) : (Object) Color.BLACK;

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
            PROPSHEET_BACKGROUND, light,
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

            //Use ProxyLazyValue so we don't have to load the class and all of
            //its gunk unless it's really needed
            "gtkColorsFailover", new UIDefaults.ProxyLazyValue("org.netbeans.swing.plaf.gtk.GtkLFCustoms$ChicletDefaults"), //NOI18N

            //UI Delegates for the tab control
            EDITOR_TAB_DISPLAYER_UI, /* blueprint ? "org.netbeans.swing.tabcontrol.plaf.GtkEditorTabDisplayerUI" : */
                "org.netbeans.swing.tabcontrol.plaf.WinClassicEditorTabDisplayerUI", //NOI18N
            VIEW_TAB_DISPLAYER_UI, /* blueprint ? "org.netbeans.swing.tabcontrol.plaf.GtkViewTabDisplayerUI" : */
                "org.netbeans.swing.tabcontrol.plaf.WinClassicViewTabDisplayerUI", //NOI18N
            SLIDING_TAB_BUTTON_UI, "org.netbeans.swing.tabcontrol.plaf.SlidingTabDisplayerButtonUI", //NOI18N
            SLIDING_BUTTON_UI, "org.netbeans.swing.tabcontrol.plaf.GtkSlidingButtonUI", //NOI18N


            DESKTOP_BACKGROUND, ThemeValue.functioning() ? new ThemeValue (ThemeValue.REGION_BUTTON, ThemeValue.LIGHT, Color.GRAY) : (Object) Color.GRAY,
            EXPLORER_MINISTATUSBAR_BORDER, BorderFactory.createEmptyBorder(),

            TOOLBAR_UI, "org.netbeans.swing.plaf.gtk.GtkToolbarUI", //NOI18N
            
        };
        return result;
    }
    
    public Object[] createLookAndFeelCustomizationKeysAndValues() {
        boolean blueprint = UIUtils.isBlueprintTheme();
        
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
                
                "tab_unsel_fill", control,
                 
                "SplitPane.dividerSize", new Integer (2),
                
                SYSTEMFONT, controlFont, //NOI18N
                USERFONT, controlFont, //NOI18N
                MENUFONT, controlFont, //NOI18N
                LISTFONT, controlFont, //NOI18N
                "Label.font", controlFont, //NOI18N
                "Panel.font", controlFont //NOI18N
            };
        } else {
            Object[] result = new Object[] {
                TOOLBAR_UI, new UIDefaults.ProxyLazyValue("org.netbeans.swing.plaf.gtk.GtkToolbarUI"), //NOI18N
            };
            return result;
        }
    }
    
    
    public static class ChicletDefaults {
        public static final String CHICLET_SELECTEDPRESSED_UPPERTOP = "nb.gradient.selectedpressedactive.uppertop"; //NOI18N
        public static final String CHICLET_SELECTEDPRESSED_UPPERBOTTOM = "nb.gradient.selectedpressedactive.upperbottom"; //NOI18N
        public static final String CHICLET_SELECTEDPRESSED_LOWERTOP = "nb.gradient.selectedpressedactive.lowertop"; //NOI18N
        public static final String CHICLET_SELECTEDPRESSED_LOWERBOTTOM = "nb.gradient.selectedpressedactive.lowerbottom"; //NOI18N

        public static final String CHICLET_SELECTED_UPPERTOP = "nb.gradient.selectedactive.uppertop"; //NOI18N
        public static final String CHICLET_SELECTED_UPPERBOTTOM = "nb.gradient.selectedactive.upperbottom"; //NOI18N
        public static final String CHICLET_SELECTED_LOWERTOP = "nb.gradient.selectedactive.lowertop"; //NOI18N
        public static final String CHICLET_SELECTED_LOWERBOTTOM = "nb.gradient.selectedactive.lowerbottom"; //NOI18N

        public static final String CHICLET_INACTIVE_UPPERTOP = "nb.gradient.inactive.uppertop"; //NOI18N
        public static final String CHICLET_INACTIVE_UPPERBOTTOM = "nb.gradient.inactive.upperbottom"; //NOI18N
        public static final String CHICLET_INACTIVE_LOWERTOP = "nb.gradient.inactive.lowertop"; //NOI18N
        public static final String CHICLET_INACTIVE_LOWERBOTTOM = "nb.gradient.inactive.lowerbottom"; //NOI18N

        public static final String CHICLET_SELECTEDINACTIVE_UPPERTOP = "nb.gradient.selectedinactive.uppertop"; //NOI18N
        public static final String CHICLET_SELECTEDINACTIVE_UPPERBOTTOM = "nb.gradient.selectedinactive.upperbottom"; //NOI18N
        public static final String CHICLET_SELECTEDINACTIVE_LOWERTOP = "nb.gradient.selectedinactive.lowertop"; //NOI18N
        public static final String CHICLET_SELECTEDINACTIVE_LOWERBOTTOM = "nb.gradient.selectedinactive.lowerbottom"; //NOI18N

        public static final String CHICLET_CLOSING_UPPERTOP = "nb.gradient.closing.uppertop"; //NOI18N
        public static final String CHICLET_CLOSING_UPPERBOTTOM = "nb.gradient.closing.upperbottom"; //NOI18N
        public static final String CHICLET_CLOSING_LOWERTOP = "nb.gradient.closing.lowertop"; //NOI18N
        public static final String CHICLET_CLOSING_LOWERBOTTOM = "nb.gradient.closing.lowerbottom"; //NOI18N

        public static final String CHICLET_ACTIVE_UPPERTOP = "nb.gradient.active.uppertop"; //NOI18N
        public static final String CHICLET_ACTIVE_UPPERBOTTOM = "nb.gradient.active.upperbottom"; //NOI18N
        public static final String CHICLET_ACTIVE_LOWERTOP = "nb.gradient.active.lowertop"; //NOI18N
        public static final String CHICLET_ACTIVE_LOWERBOTTOM = "nb.gradient.active.lowerbottom"; //NOI18N

    //These are the default colors for the GTK blueprint theme
        Color[] selectedActive = new Color[]{
            new Color(130, 90, 233), new Color(105, 40, 175),
            new Color(100, 40, 175), new Color(80, 0, 130)};

        Color[] selectedPressedActive = new Color[]{
            new Color(100, 40, 175), new Color(80, 0, 130),
            new Color(105, 40, 175), new Color(130, 90, 233)};

        Color[] inactive = new Color[]{
            new Color(150, 150, 244), new Color(136, 117, 198),
            new Color(136, 117, 198), new Color(108, 58, 180)};

        Color[] selectedInactive = new Color[]{
            new Color(150, 150, 244), new Color(136, 117, 198),
            new Color(136, 117, 198), new Color(108, 58, 180)};

        Color[] closing = new Color[]{
            new Color(233, 0, 130), new Color(175, 0, 105),
            new Color(175, 0, 100), new Color(130, 0, 80)};
            
        public ChicletDefaults() {
            System.err.println("Creating chiclet defaults");
            UIManager.getDefaults().putDefaults(createKeysAndValues());
        }
            
        
        public Object[] createKeysAndValues() {
            Object[] result = new Object[] {
            //GTK Editor UI in case getting the theme colors by reflection fails
                CHICLET_INACTIVE_UPPERTOP, inactive[0],
                CHICLET_INACTIVE_UPPERBOTTOM, inactive[1],
                CHICLET_INACTIVE_LOWERTOP, inactive[2],
                CHICLET_INACTIVE_LOWERBOTTOM, inactive[3],

                CHICLET_SELECTEDINACTIVE_UPPERTOP, selectedInactive[0],
                CHICLET_SELECTEDINACTIVE_UPPERBOTTOM, selectedInactive[1],
                CHICLET_SELECTEDINACTIVE_LOWERTOP, selectedInactive[2],
                CHICLET_SELECTEDINACTIVE_LOWERBOTTOM, selectedInactive[3],

                CHICLET_SELECTED_UPPERTOP, selectedActive[0],
                CHICLET_SELECTED_UPPERBOTTOM, selectedActive[1],
                CHICLET_SELECTED_LOWERTOP, selectedActive[2],
                CHICLET_SELECTED_LOWERBOTTOM, selectedActive[3],

                CHICLET_ACTIVE_UPPERTOP, selectedActive[0],
                CHICLET_ACTIVE_UPPERBOTTOM, selectedActive[1],
                CHICLET_ACTIVE_LOWERTOP, selectedActive[2],
                CHICLET_ACTIVE_LOWERBOTTOM, selectedActive[3],

                CHICLET_SELECTEDPRESSED_UPPERTOP, selectedPressedActive[0],
                CHICLET_SELECTEDPRESSED_UPPERBOTTOM, selectedPressedActive[1],
                CHICLET_SELECTEDPRESSED_LOWERTOP, selectedPressedActive[2],
                CHICLET_SELECTEDPRESSED_LOWERBOTTOM, selectedPressedActive[3],

                CHICLET_CLOSING_UPPERTOP, closing[0],
                CHICLET_CLOSING_UPPERBOTTOM, closing[1],
                CHICLET_CLOSING_LOWERTOP, closing[2],
                CHICLET_CLOSING_LOWERBOTTOM, closing[3],
            };
            return result;
        }
        
    }
}
