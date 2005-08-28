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

package org.netbeans.swing.plaf.aqua;

import org.netbeans.swing.plaf.LFCustoms;
import org.netbeans.swing.plaf.util.GuaranteedValue;
import org.netbeans.swing.plaf.util.UIUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;
import java.awt.*;


/** Default system-provided customizer for Windows XP LF 
 * Public only to be accessible by ProxyLazyValue, please don't abuse.
 */
public final class AquaLFCustoms extends LFCustoms {
    
    
    public Object[] createLookAndFeelCustomizationKeysAndValues() {
        Integer cus = (Integer) UIManager.get("customFontSize"); //NOI18N
        Object[] result;
        if (cus != null) {
            int uiFontSize = cus.intValue();
            Font controlFont = new GuaranteedValue (new String[] {"controlFont", "Tree.font", "Label.font"},
                                                new FontUIResource("Dialog", Font.PLAIN, uiFontSize)).getFont(); //NOI18N
            result = new Object[] {
                "Button.font", controlFont,
                "Tree.font", controlFont,
                "ToggleButton.font", controlFont,
                "Menu.font", controlFont,
                "MenuBar.font", controlFont,
                "MenuItem.font", controlFont,
                "CheckBoxMenuItem.font", controlFont,
                "RadioButtonMenuItem.font", controlFont,
                "PopupMenu.font", controlFont,
                "List.font", controlFont,
                "Label.font", controlFont,
                "ComboBox.font", controlFont, 
                "PopupMenuSeparatorUI", "org.netbeans.swing.plaf.aqua.AquaSeparatorUI",
                "SeparatorUI", "org.netbeans.swing.plaf.aqua.AquaSeparatorUI",
                 SLIDING_BUTTON_UI, "org.netbeans.swing.tabcontrol.plaf.AquaSlidingButtonUI",

            }; //NOI18N
        } else {
            result = new Object[] {
                "controlFont", new GuaranteedValue (new String[] {"Label.font", "Tree.font"}, new FontUIResource("Dialog", Font.PLAIN, 14)).getFont(),
                "PopupMenuSeparatorUI", "org.netbeans.swing.plaf.aqua.AquaSeparatorUI",
                "SeparatorUI", "org.netbeans.swing.plaf.aqua.AquaSeparatorUI",
                 SLIDING_BUTTON_UI, "org.netbeans.swing.tabcontrol.plaf.AquaSlidingButtonUI",
            }; 
        }
        return result;
    }

    public Object[] createApplicationSpecificKeysAndValues () {
        Border topOnly = BorderFactory.createMatteBorder(1, 0, 0, 0,
            UIManager.getColor("controlShadow").brighter()); //NOI18N
        Border bottomOnly = BorderFactory.createMatteBorder(0, 0, 1, 0,
            UIManager.getColor("controlShadow").brighter()); //NOI18N

        Border empty = BorderFactory.createEmptyBorder();

        Image explorerIcon = UIUtils.loadImage(
            "org/netbeans/swing/plaf/resources/osx-folder.png"); //NOI18N

        Border lowerBorder = new AquaRoundedLowerBorder();
        Border tabsBorder = new AquaEditorTabControlBorder();

        Object[] result = {
            TOOLBAR_UI, "org.netbeans.swing.plaf.aqua.PlainAquaToolbarUI",

            // XXX  - EXPLORER_STATUS_BORDER,
            DESKTOP_BACKGROUND, new Color(226, 223, 214), //NOI18N
            SCROLLPANE_BORDER_COLOR, new Color(127, 157, 185),
            EXPLORER_FOLDER_ICON ,explorerIcon,
            EXPLORER_FOLDER_OPENED_ICON, explorerIcon,
            DESKTOP_BORDER, empty,
            SCROLLPANE_BORDER, topOnly,
            EXPLORER_STATUS_BORDER, topOnly,
            EDITOR_STATUS_LEFT_BORDER, topOnly,
            EDITOR_STATUS_RIGHT_BORDER, topOnly,
            EDITOR_STATUS_INNER_BORDER, topOnly,
            EDITOR_STATUS_ONLYONEBORDER, topOnly,
            EDITOR_TOOLBAR_BORDER, new PlainAquaToolbarUI.AquaTbBorder(),

            EDITOR_TAB_OUTER_BORDER, BorderFactory.createEmptyBorder(),
            EDITOR_TAB_CONTENT_BORDER, lowerBorder,
            EDITOR_TAB_TABS_BORDER, tabsBorder,

            VIEW_TAB_OUTER_BORDER, BorderFactory.createEmptyBorder(),
            VIEW_TAB_TABS_BORDER, BorderFactory.createEmptyBorder(),
            VIEW_TAB_CONTENT_BORDER, lowerBorder,


            //UI Delegates for the tab control
            EDITOR_TAB_DISPLAYER_UI, "org.netbeans.swing.tabcontrol.plaf.AquaEditorTabDisplayerUI",
            VIEW_TAB_DISPLAYER_UI, "org.netbeans.swing.tabcontrol.plaf.AquaViewTabDisplayerUI",
            SLIDING_TAB_BUTTON_UI, "org.netbeans.swing.tabcontrol.plaf.SlidingTabDisplayerButtonUI$Aqua",

            EXPLORER_MINISTATUSBAR_BORDER, BorderFactory.createEmptyBorder(),
            
            "floatingBorder", new DropShadowBorder(),
                    
            TAB_ACTIVE_SELECTION_FOREGROUND, new GuaranteedValue ("textText", Color.BLACK),
                    
            // progress component related
            "nbProgressBar.Foreground", new Color(49, 106, 197),
            "nbProgressBar.Background", Color.WHITE,
            "nbProgressBar.popupDynaText.foreground", new Color(141, 136, 122),
            "nbProgressBar.popupText.background", new Color(249, 249, 249),        
            "nbProgressBar.popupText.foreground", UIManager.getColor("TextField.foreground"),
            "nbProgressBar.popupText.selectBackground", UIManager.getColor("List.selectionBackground"),
            "nbProgressBar.popupText.selectForeground", UIManager.getColor("List.selectionForeground"),                    
            PROGRESS_CANCEL_BUTTON_ICON, UIUtils.loadImage("org/netbeans/swing/plaf/resources/cancel_task_linux_mac.png"),
                    
        }; //NOI18N
        return result;
    }
    
    
}
