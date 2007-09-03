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

package org.netbeans.swing.plaf.winxp;

import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import org.netbeans.swing.plaf.LFCustoms;
import org.netbeans.swing.plaf.util.GuaranteedValue;
import org.netbeans.swing.plaf.util.UIBootstrapValue;
import org.netbeans.swing.plaf.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ResourceBundle;


/** Default system-provided customizer for Windows XP LF 
 * Public only to be accessible by ProxyLazyValue, please don't abuse.
 */
public final class XPLFCustoms extends LFCustoms {
    private static final String TAB_FOCUS_FILL_DARK = "tab_focus_fill_dark"; //NOI18N    
    private static final String TAB_FOCUS_FILL_BRIGHT = "tab_focus_fill_bright"; //NOI18N 
    private static final String TAB_UNSEL_FILL_DARK = "tab_unsel_fill_dark"; //NOI18N
    private static final String TAB_UNSEL_FILL_BRIGHT = "tab_unsel_fill_bright"; //NOI18N  
    private static final String TAB_SEL_FILL = "tab_sel_fill"; //NOI18N
    private static final String TAB_SEL_FILL_BRIGHT = "tab_sel_fill_bright"; //NOI18N
    private static final String TAB_SEL_FILL_DARK = "tab_sel_fill_dark"; //NOI18N 
    private static final String TAB_BORDER = "tab_border"; //NOI18N      
    private static final String TAB_BOTTOM_BORDER = "tab_bottom_border"; //NOI18N 
    private static final String TAB_SEL_BORDER = "tab_sel_border"; //NOI18N
    private static final String TAB_HIGHLIGHT_HEADER = "tab_highlight_header"; //NOI18N
    private static final String TAB_HIGHLIGHT_HEADER_FILL = "tab_highlight_header_fill"; //NOI18N  
    private static final String STANDARD_BORDER = "standard_border"; //NOI18N  
    private static final String TAB_CLOSE_BUTTON = "close_button"; //NOI18N
    private static final String TAB_CLOSE_BUTTON_HIGHLIGHT = "close_button_highlight"; //NOI18N 
    private static final String TAB_CLOSE_BUTTON_BORDER_FOCUS = "close_button_border_focus"; //NOI18N   
    private static final String TAB_CLOSE_BUTTON_BORDER_SELECTED = "close_button_border_selected"; //NOI18N
    private static final String TAB_CLOSE_BUTTON_BORDER_UNSEL = "close_button_border_unsel"; //NOI18N
    private static final String TAB_SEL_BOTTOM_BORDER = "tab_sel_bottom_border"; //NOI18N
    
    static final String SCROLLPANE_BORDER_COLOR = "scrollpane_border"; //NOI18N

    public Object[] createLookAndFeelCustomizationKeysAndValues() {
        int fontsize = 11;
        Integer in = (Integer) UIManager.get(CUSTOM_FONT_SIZE); //NOI18N
        if (in != null) {
            fontsize = in.intValue();
        }
        
        Object[] result = new Object[] {
                //Work around a bug in windows which sets the text area font to
                //"MonoSpaced", causing all accessible dialogs to have monospaced text
                "TextArea.font", new GuaranteedValue ("Label.font", new Font("Dialog", Font.PLAIN, fontsize)),
                
                EDITOR_ERRORSTRIPE_SCROLLBAR_INSETS, new Insets(17, 0, 17, 0),
            };
            
        tahomaWarning();    
            
        String version = System.getProperty("java.version");
        if( version.startsWith("1.5") ) {
            //#112473 - wrong password text field height
            UIManager.put("PasswordField.font", UIManager.get("TextField.font") );
        }
        return result;
    }

    public Object[] createApplicationSpecificKeysAndValues () {
        UIBootstrapValue editorTabsUI = new XPEditorColorings (
                "org.netbeans.swing.tabcontrol.plaf.WinXPEditorTabDisplayerUI");

        Object viewTabsUI = editorTabsUI.createShared("org.netbeans.swing.tabcontrol.plaf.WinXPViewTabDisplayerUI");

        Image explorerIcon = UIUtils.loadImage("org/netbeans/swing/plaf/resources/xp-explorer-folder.gif");

        Object propertySheetValues = new XPPropertySheetColorings();

        Object[] uiDefaults = {
            EDITOR_TAB_DISPLAYER_UI, editorTabsUI,
            VIEW_TAB_DISPLAYER_UI, viewTabsUI,
            
            DESKTOP_BACKGROUND, new Color(226, 223, 214), //NOI18N
            SCROLLPANE_BORDER_COLOR, new Color(127, 157, 185),
            DESKTOP_BORDER, new EmptyBorder(6, 5, 4, 6),
            SCROLLPANE_BORDER, UIManager.get("ScrollPane.border"),
            EXPLORER_STATUS_BORDER, new StatusLineBorder(StatusLineBorder.TOP),
            EXPLORER_FOLDER_ICON , explorerIcon,
            EXPLORER_FOLDER_OPENED_ICON, explorerIcon,
            EDITOR_STATUS_LEFT_BORDER, new StatusLineBorder(StatusLineBorder.TOP | StatusLineBorder.RIGHT),
            EDITOR_STATUS_RIGHT_BORDER, new StatusLineBorder(StatusLineBorder.TOP | StatusLineBorder.LEFT),
            EDITOR_STATUS_INNER_BORDER, new StatusLineBorder(StatusLineBorder.TOP | StatusLineBorder.LEFT | StatusLineBorder.RIGHT),
            EDITOR_STATUS_ONLYONEBORDER, new StatusLineBorder(StatusLineBorder.TOP),
            EDITOR_TOOLBAR_BORDER, new EditorToolbarBorder(),
            OUTPUT_SELECTION_BACKGROUND, new Color (164, 180, 255),

            PROPERTYSHEET_BOOTSTRAP, propertySheetValues,

            WORKPLACE_FILL, new Color(226, 223, 214),

            DESKTOP_SPLITPANE_BORDER, BorderFactory.createEmptyBorder(4, 0, 0, 0),
            SLIDING_BUTTON_UI, "org.netbeans.swing.tabcontrol.plaf.WinXPSlidingButtonUI",

            // progress component related
            "nbProgressBar.Foreground", new Color(49, 106, 197),
            "nbProgressBar.Background", Color.WHITE,
            "nbProgressBar.popupDynaText.foreground", new Color(141, 136, 122),
            "nbProgressBar.popupText.background", new Color(249, 249, 249),        
            "nbProgressBar.popupText.foreground", UIManager.getColor("TextField.foreground"),
            "nbProgressBar.popupText.selectBackground", UIManager.getColor("List.selectionBackground"),
            "nbProgressBar.popupText.selectForeground", UIManager.getColor("List.selectionForeground"),                    
            PROGRESS_CANCEL_BUTTON_ICON, UIUtils.loadImage("org/netbeans/swing/plaf/resources/cancel_task_win_xp.png")

        }; //NOI18N
        
        //Workaround for JDK 1.5.0 bug 5080144 - Disabled JTextFields stay white
        //XPTheme uses Color instead of ColorUIResource
        convert ("TextField.background"); //NOI18N
        convert ("TextField.inactiveBackground"); //NOI18N
        convert ("TextField.disabledBackground");  //NOI18N

        //#108517 - turn off ctrl+page_up and ctrl+page_down mapping
        return UIUtils.addInputMapsWithoutCtrlPageUpAndCtrlPageDown( uiDefaults );
    }
    
    /**
     * Takes a UIManager color key and ensures that it is stored as a 
     * ColorUIResource, not a Color. 
     */
    private static final void convert (String key) {
        Color c = UIManager.getColor(key);
        if (c != null && !(c instanceof ColorUIResource)) {
            UIManager.put (key, new ColorUIResource(c));
        }
    }
    
    protected Object[] additionalKeys() {
        Object[] kv = new XPEditorColorings("").createKeysAndValues();
        Object[] kv2 = new XPPropertySheetColorings().createKeysAndValues();
        Object[] result = new Object[(kv.length / 2) + (kv2.length / 2)];
        int ct = 0;
        for (int i=0; i < kv.length; i+=2) {
            result[ct] = kv[i];
            ct++;
        }
        for (int i=0; i < kv2.length; i+=2) {
            result[ct] = kv2[i];
            ct++;
        }
        return result;
    }   

    /** Prints warning of JDK bug if jdk 1.5.0 or 1.5.0_01 is used - 
     * fonts aren't set to Tahoma, which looks bad.
     */
    private void tahomaWarning () {
        String version = System.getProperty("java.version");
        if ("1.5.0".equals(version) || version.startsWith("1.5.0_01")) {
            Logger.getLogger(XPLFCustoms.class.getName()).log(Level.WARNING,
                    ResourceBundle.getBundle("org.netbeans.swing.plaf.winxp.Bundle").getString("MSG_TahomaWarning"));
        }
    }
    
    
    private class XPEditorColorings extends UIBootstrapValue.Lazy {
        public XPEditorColorings (String name) {
            super (name);
        }

        public Object[] createKeysAndValues() {
            return new Object[] {
            //Tab control - XXX REPLACE WITH RelativeColor - need to figure out base
            //colors for each color
            TAB_FOCUS_FILL_DARK, new Color(210, 220, 243),
            TAB_FOCUS_FILL_BRIGHT, new Color(238, 242, 253),
            TAB_UNSEL_FILL_DARK, new Color(236, 235, 229),
            TAB_UNSEL_FILL_BRIGHT, new Color(252,251,246),
            TAB_SEL_FILL, Color.white,
            TAB_SEL_FILL_BRIGHT, Color.white,
            TAB_SEL_FILL_DARK, new Color(243, 241, 224),
            TAB_BORDER, new Color(145, 167, 180),
            TAB_BOTTOM_BORDER, new Color(127, 157, 185),
            TAB_SEL_BORDER, new Color(145, 155, 156),
            TAB_HIGHLIGHT_HEADER, new Color(230, 139, 44),
            TAB_HIGHLIGHT_HEADER_FILL, new Color(255, 199, 60),
            STANDARD_BORDER, new Color(127, 157, 185),
            TAB_CLOSE_BUTTON, Color.black,
            TAB_CLOSE_BUTTON_HIGHLIGHT, new Color(172,57,28),
            TAB_CLOSE_BUTTON_BORDER_FOCUS, new Color(181,201,243),
            TAB_CLOSE_BUTTON_BORDER_SELECTED, new Color(203,202,187),
            TAB_CLOSE_BUTTON_BORDER_UNSEL, new Color(200,201,192),
            TAB_SEL_BOTTOM_BORDER, new Color(238,235,218),

            //Borders for the tab control
            EDITOR_TAB_OUTER_BORDER, BorderFactory.createEmptyBorder(),
            EDITOR_TAB_CONTENT_BORDER,
                new MatteBorder(0, 1, 1, 1, new Color(127, 157, 185)),
            EDITOR_TAB_TABS_BORDER, BorderFactory.createEmptyBorder(),

            VIEW_TAB_OUTER_BORDER, BorderFactory.createEmptyBorder(),
            VIEW_TAB_CONTENT_BORDER,
                new MatteBorder(0, 1, 1, 1, new Color(127, 157, 185)),
            VIEW_TAB_TABS_BORDER, BorderFactory.createEmptyBorder(),
            };
        }
    }

    private class XPPropertySheetColorings extends UIBootstrapValue.Lazy {
        public XPPropertySheetColorings () {
            super ("propertySheet");  //NOI18N
        }

        public Object[] createKeysAndValues() {
            return new Object[] {
                PROPSHEET_SET_BACKGROUND, new Color(49,106,197),
                PROPSHEET_SELECTION_FOREGROUND, Color.WHITE,
                PROPSHEET_SET_BACKGROUND, new Color(212,208,200),
                PROPSHEET_SET_FOREGROUND, Color.BLACK,
                PROPSHEET_SELECTED_SET_BACKGROUND, new Color(49,106,197),
                PROPSHEET_SELECTED_SET_FOREGROUND, Color.WHITE,
                PROPSHEET_DISABLED_FOREGROUND, new Color(161,161,146),
                PROPSHEET_BUTTON_FOREGROUND, Color.BLACK,
            };
        }

    }
}
