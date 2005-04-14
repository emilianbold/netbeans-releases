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

package org.netbeans.swing.plaf.winxp;

import java.awt.GraphicsEnvironment;
import java.util.Locale;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import org.netbeans.swing.plaf.LFCustoms;
import org.netbeans.swing.plaf.util.GuaranteedValue;
import org.netbeans.swing.plaf.util.UIBootstrapValue;
import org.netbeans.swing.plaf.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;


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
    
    private static final String TAHOMA_FONT_NAME = "Tahoma";
    
    static final String SCROLLPANE_BORDER_COLOR = "scrollpane_border"; //NOI18N

    public Object[] createLookAndFeelCustomizationKeysAndValues() {
        int fontsize = 11;
        Integer in = (Integer) UIManager.get(CUSTOM_FONT_SIZE); //NOI18N
        if (in != null) {
            fontsize = in.intValue();
        }
        
        Object[] result;

        if (shouldWeUseTahoma()) {
            result = new Object[] {
                //Changes default fonts to Tahoma based ones, for consistent Windows look
                // XXX Note that manual Tahoma setting is workaround for JDK bug 5079742.
                // Please remove when JDK 1.4 is no longer supported,
                // when only JDK 1.5_02 and higher are supported 
                "Button.font", patch2Tahoma("Button.font"), //NOI18N
                "CheckBox.font", patch2Tahoma("CheckBox.font"), //NOI18N
                "ComboBox.font", patch2Tahoma("ComboBox.font"), //NOI18N
                "EditorPane.font", patch2Tahoma("EditorPane.font"), //NOI18N
                "Label.font", patch2Tahoma("Label.font"), //NOI18N
                "List.font", patch2Tahoma("List.font"), //NOI18N
                "RadioButton.font", patch2Tahoma("RadioButton.font"), //NOI18N
                "Panel.font", patch2Tahoma("Panel.font"), //NOI18N
                "PasswordField.font", patch2Tahoma("PasswordField.font"), //NOI18N
                "ProgressBar.font", patch2Tahoma("ProgressBar.font"), //NOI18N
                "ScrollPane.font", patch2Tahoma("ScrollPane.font"), //NOI18N
                "Spinner.font", patch2Tahoma("Spinner.font"), //NOI18N
                "TabbedPane.font", patch2Tahoma("TabbedPane.font"), //NOI18N
                "Table.font", patch2Tahoma("Table.font"), //NOI18N
                "TableHeader.font", patch2Tahoma("TableHeader.font"), //NOI18N
                "TextField.font", patch2Tahoma("TextField.font"), //NOI18N
                "TextPane.font", patch2Tahoma("TextPane.font"), //NOI18N
                "TitledBorder.font", patch2Tahoma("TitledBorder.font"), //NOI18N
                "ToggleButton.font", patch2Tahoma("ToggleButton.font"), //NOI18N
                "Tree.font", patch2Tahoma("Tree.font"), //NOI18N
                "Viewport.font", patch2Tahoma("Viewport.font"), //NOI18N
                "windowTitleFont", patch2Tahoma("windowTitleFont"), //NOI18N
                "controlFont", patch2Tahoma("controlFont"), //NOI18N

                //Work around a bug in windows which sets the text area font to
                //"MonoSpaced", causing all accessible dialogs to have monospaced text
                "TextArea.font", new GuaranteedValue ("Label.font", new Font("Dialog", Font.PLAIN, fontsize))
            };
        } else {
            result = new Object[] {
                //Work around a bug in windows which sets the text area font to
                //"MonoSpaced", causing all accessible dialogs to have monospaced text
                "TextArea.font", new GuaranteedValue ("Label.font", new Font("Dialog", Font.PLAIN, fontsize))
            };
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


        }; //NOI18N
        
        //Workaround for JDK 1.5.0 bug 5080144 - Disabled JTextFields stay white
        //XPTheme uses Color instead of ColorUIResource
        convert ("TextField.background"); //NOI18N
        convert ("TextField.inactiveBackground"); //NOI18N
        convert ("TextField.disabledBackground");  //NOI18N

        return uiDefaults;
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
    
    // XXX Note this is workaround for JDK bug 5079742.
    // Please remove when JDK 1.4 is no longer supported,
    // when only JDK 1.5_02 and higher are supported 
    /** Takes given font uiresource and creates Tahoma based font of same style
     * and size.
     */
    private FontUIResource patch2Tahoma(String uiResource) {
        Font originalFont = UIManager.getFont(uiResource);
        FontUIResource result;
        if (originalFont != null) {
            result = new FontUIResource(TAHOMA_FONT_NAME, originalFont.getStyle(), originalFont.getSize());
        } else {
            result = new FontUIResource(TAHOMA_FONT_NAME, Font.PLAIN, 11);
        }
        return result;
    }     
    
    /** Finds out if tahoma font is proper to use on current system (locale, availability)
     * @return true if tahoma font is available, false otherwise
     */
    private static boolean shouldWeUseTahoma () {
        // don't try to use Tahoma for East Asian languages
        Locale curLocale = Locale.getDefault();
        if (Locale.JAPANESE.getLanguage().equals(curLocale.getLanguage()) ||
            Locale.KOREAN.getLanguage().equals(curLocale.getLanguage()) ||
            Locale.CHINESE.getLanguage().equals(curLocale.getLanguage())) {
            return false;
        }

        // check if Tahoma is really available
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] familyNames = env.getAvailableFontFamilyNames();
        
        for (int i = 0; i < familyNames.length; i++) {
            if (TAHOMA_FONT_NAME.equals(familyNames[i])) {
                return true;
            }
        }
        
        return false;
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
