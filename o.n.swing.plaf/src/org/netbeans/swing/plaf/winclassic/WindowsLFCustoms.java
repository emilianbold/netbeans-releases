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

package org.netbeans.swing.plaf.winclassic;

import java.util.Locale;
import javax.swing.plaf.FontUIResource;
import org.netbeans.swing.plaf.LFCustoms;
import org.netbeans.swing.plaf.util.GuaranteedValue;
import org.netbeans.swing.plaf.util.RelativeColor;
import org.netbeans.swing.plaf.util.UIBootstrapValue;
import org.netbeans.swing.plaf.util.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


/** Default system-provided customizer for Windows LF
 * Public only to be accessible by ProxyLazyValue, please don't abuse.
 */
public final class WindowsLFCustoms extends LFCustoms {
    
    private static final String TAHOMA_FONT_NAME = "Tahoma";
    

    public Object[] createLookAndFeelCustomizationKeysAndValues() {
        int fontsize = 11;
        Integer in = (Integer) UIManager.get(CUSTOM_FONT_SIZE); //NOI18N
        if (in != null) {
            fontsize = in.intValue();
        }
        
        Object[] result;

        if (shouldWeUseTahoma()) {
            result = new Object[] {
                //Workaround for help window selection color
                "EditorPane.selectionBackground", new Color (157, 157, 255), //NOI18N

                //Changes default fonts to Tahoma based ones, for consistent Windows look
                // XXX Note this is workaround for JDK bug 5079742.
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
                "TextArea.font", new GuaranteedValue ("Label.font", new Font("Dialog", Font.PLAIN, fontsize)), //NOI18N
                        
                        
            };
        } else {
            result = new Object[] {
                //Workaround for help window selection color
                "EditorPane.selectionBackground", new Color (157, 157, 255), //NOI18N
                //Work around a bug in windows which sets the text area font to
                //"MonoSpaced", causing all accessible dialogs to have monospaced text
                "TextArea.font", new GuaranteedValue ("Label.font", new Font("Dialog", Font.PLAIN, fontsize)), //NOI18N
            };
        }
            
        return result;
    }

    public Object[] createApplicationSpecificKeysAndValues () {
        Object propertySheetColorings = new WinClassicPropertySheetColorings();
        Object[] result = {
            DESKTOP_BORDER, new EmptyBorder(4, 2, 1, 2),
            SCROLLPANE_BORDER, UIManager.get("ScrollPane.border"),
            EXPLORER_STATUS_BORDER, new StatusLineBorder(StatusLineBorder.TOP),
            EXPLORER_FOLDER_ICON , UIUtils.loadImage("org/netbeans/swing/plaf/resources/win-explorer-folder.gif"),
            EXPLORER_FOLDER_OPENED_ICON, UIUtils.loadImage("org/netbeans/swing/plaf/resources/win-explorer-opened-folder.gif"),
            EDITOR_STATUS_LEFT_BORDER, new StatusLineBorder(StatusLineBorder.TOP | StatusLineBorder.RIGHT),
            EDITOR_STATUS_RIGHT_BORDER, new StatusLineBorder(StatusLineBorder.TOP | StatusLineBorder.LEFT),
            EDITOR_STATUS_INNER_BORDER, new StatusLineBorder(StatusLineBorder.TOP | StatusLineBorder.LEFT | StatusLineBorder.RIGHT),
            EDITOR_TOOLBAR_BORDER, new EditorToolbarBorder(),
            EDITOR_STATUS_ONLYONEBORDER, new StatusLineBorder(StatusLineBorder.TOP),

            PROPERTYSHEET_BOOTSTRAP, propertySheetColorings,

            EDITOR_TAB_CONTENT_BORDER, new WinClassicCompBorder(),
            EDITOR_TAB_TABS_BORDER, new WinClassicTabBorder(),
            VIEW_TAB_CONTENT_BORDER, new WinClassicCompBorder(),
            VIEW_TAB_TABS_BORDER, new WinClassicTabBorder(),

            DESKTOP_SPLITPANE_BORDER, BorderFactory.createEmptyBorder(4, 2, 1, 2),

            //UI Delegates for the tab control
            EDITOR_TAB_DISPLAYER_UI, "org.netbeans.swing.tabcontrol.plaf.WinClassicEditorTabDisplayerUI",
            SLIDING_BUTTON_UI, "org.netbeans.swing.tabcontrol.plaf.WindowsSlidingButtonUI",
            VIEW_TAB_DISPLAYER_UI, "org.netbeans.swing.tabcontrol.plaf.WinClassicViewTabDisplayerUI",

            PROGRESS_CANCEL_BUTTON_ICON, UIUtils.loadImage("org/netbeans/swing/plaf/resources/cancel_task_win_classic.png"),
                    
            //XXX convert to derived colors
            "tab_unsel_fill", UIUtils.adjustColor (
                new GuaranteedValue("InternalFrame.inactiveTitleGradient",
                    Color.GRAY).getColor(),
                -12, -15, -22),

            "tab_sel_fill", new GuaranteedValue("text", Color.WHITE),

            "tab_bottom_border", UIUtils.adjustColor (
                new GuaranteedValue("InternalFrame.borderShadow",
                    Color.GRAY).getColor(),
                20, 17, 12),


             "winclassic_tab_sel_gradient",
                new RelativeColor (
                    new Color(7, 28, 95),
                    new Color(152, 177, 208),
                    "InternalFrame.activeTitleBackground"),

        }; //NOI18N

        return result;
    }
    
    public Object[] createGuaranteedKeysAndValues() {
        return new Object[] {
             "InternalFrame.activeTitleBackground",
                new GuaranteedValue("InternalFrame.activeTitleBackground",
                Color.BLUE),
                
            "InternalFrame.borderShadow",
                new GuaranteedValue("InternalFrame.borderShadow", Color.gray),

            "InternalFrame.borderHighlight",
                new GuaranteedValue("InternalFrame.borderHighlight",
                Color.white),

            "InternalFrame.borderDarkShadow",
                new GuaranteedValue("InternalFrame.borderDarkShadow",
                Color.darkGray),

            "InternalFrame.borderLight",
                new GuaranteedValue("InternalFrame.borderLight",
                Color.lightGray),

            "TabbedPane.background",
                new GuaranteedValue("TabbedPane.background", Color.LIGHT_GRAY),

            "TabbedPane.focus",
                new GuaranteedValue("TabbedPane.focus", Color.GRAY),

            "TabbedPane.highlight",
                new GuaranteedValue("TabbedPane.highlight", Color.WHITE) ,
             
            "Button.dashedRectGapX",
               new GuaranteedValue("Button.dashedRectGapX", new Integer(5)),
               
            "Button.dashedRectGapY",
               new GuaranteedValue("Button.dashedRectGapY", new Integer(4)),
               
            "Button.dashedRectGapWidth",
               new GuaranteedValue("Button.dashedRectGapWidth", new Integer(10)),
               
            "Button.dashedRectGapHeight",
               new GuaranteedValue("Button.dashedRectGapHeight", new Integer(8)),
                     
            "Tree.expandedIcon", new ExpandedIcon(),
            "Tree.collapsedIcon", new CollapsedIcon()
        };
    }

    private static class ExpandedIcon implements javax.swing.Icon {
        protected static final int HALF_SIZE = 4;
        protected static final int SIZE = 9;
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Color.WHITE);
            g.fillRect(x, y, SIZE-1, SIZE-1);
            g.setColor(Color.GRAY);
            g.drawRect(x, y, SIZE-1, SIZE-1);
            g.setColor(Color.BLACK);
            g.drawLine(x + 2, y + HALF_SIZE, x + (SIZE - 3), y + HALF_SIZE);
        }
        public int getIconWidth() { return SIZE; }
        public int getIconHeight() { return SIZE; }
    }

    private final static class CollapsedIcon extends ExpandedIcon {
        public void paintIcon(Component c, Graphics g, int x, int y) {
            super.paintIcon(c, g, x, y);
            g.drawLine(x + HALF_SIZE, y + 2, x + HALF_SIZE, y + (SIZE - 3));
        }
    }

    protected Object[] additionalKeys() {
        Object[] kv = new WinClassicPropertySheetColorings().createKeysAndValues();
        Object[] result = new Object[kv.length / 2];
        int ct = 0;
        for (int i=0; i < kv.length; i+=2) {
            result[ct] = kv[i];
            ct++;
        }
        return result;
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

    private class WinClassicPropertySheetColorings extends UIBootstrapValue.Lazy {
        public WinClassicPropertySheetColorings () {
            super (null);
        }

        public Object[] createKeysAndValues() {
            return new Object[] {
            //Property sheet settings as defined by HIE
            PROPSHEET_SELECTION_BACKGROUND, new Color(10,36,106),
            PROPSHEET_SELECTION_FOREGROUND, Color.WHITE,
            PROPSHEET_SET_BACKGROUND, new Color(237,233,225),
            PROPSHEET_SET_FOREGROUND, Color.BLACK,
            PROPSHEET_SELECTED_SET_BACKGROUND, new Color(10,36,106),
            PROPSHEET_SELECTED_SET_FOREGROUND, Color.WHITE,
            PROPSHEET_DISABLED_FOREGROUND, new Color(128,128,128),
            PROPSHEET_BUTTON_COLOR, UIManager.getColor("control"),
            };
        }
    }
}
