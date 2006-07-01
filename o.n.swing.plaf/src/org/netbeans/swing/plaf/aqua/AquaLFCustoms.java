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
                "PopupMenu.border", BorderFactory.createEmptyBorder(4, 0, 4, 0),
                 SLIDING_BUTTON_UI, "org.netbeans.swing.tabcontrol.plaf.AquaSlidingButtonUI",

                EDITOR_ERRORSTRIPE_SCROLLBAR_INSETS, new Insets(18, 0, 18, 0),

            }; //NOI18N
        } else {
            result = new Object[] {
                "controlFont", new GuaranteedValue (new String[] {"Label.font", "Tree.font"}, new FontUIResource("Dialog", Font.PLAIN, 14)).getFont(),
                "PopupMenuSeparatorUI", "org.netbeans.swing.plaf.aqua.AquaSeparatorUI",
                "SeparatorUI", "org.netbeans.swing.plaf.aqua.AquaSeparatorUI",
                "PopupMenu.border", BorderFactory.createEmptyBorder(4, 0, 4, 0),
                 SLIDING_BUTTON_UI, "org.netbeans.swing.tabcontrol.plaf.AquaSlidingButtonUI",
		
                EDITOR_ERRORSTRIPE_SCROLLBAR_INSETS, new Insets(18, 0, 18, 0),
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
            SCROLLPANE_BORDER, UIManager.get("ScrollPane.border"),
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
