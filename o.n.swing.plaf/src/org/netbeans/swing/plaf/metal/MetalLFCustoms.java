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

package org.netbeans.swing.plaf.metal;

import org.netbeans.swing.plaf.LFCustoms;
import org.netbeans.swing.plaf.util.UIBootstrapValue;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import org.netbeans.swing.plaf.util.UIUtils;

/** Default system-provided customizer for Metal LF
 * Public only to be accessible by ProxyLazyValue, please don't abuse.
 */
public final class MetalLFCustoms extends LFCustoms {



    public Object[] createLookAndFeelCustomizationKeysAndValues() {
        int fontsize = 11;
        Integer in = (Integer) UIManager.get(CUSTOM_FONT_SIZE); //NOI18N
        if (in != null) {
            fontsize = in.intValue();
        }
        
        //XXX fetch the custom font size here instead
        Font controlFont = new Font("Dialog", Font.PLAIN, fontsize); //NOI18N
        Object[] result = {
            //The assorted standard NetBeans metal font customizations
            CONTROLFONT, controlFont,
            SYSTEMFONT, controlFont,
            USERFONT, controlFont,
            MENUFONT, controlFont,
            WINDOWTITLEFONT, controlFont,
            LISTFONT, controlFont,
            TREEFONT, controlFont,
            PANELFONT, controlFont,
            SUBFONT, new Font ("Dialog", Font.PLAIN, Math.min(fontsize - 1, 6)),
            //Bug in JDK 1.5 thru b59 - pale blue is incorrectly returned for this
            "textInactiveText", Color.GRAY, //NOI18N
            // #61395        
            SPINNERFONT, controlFont,        
            EDITOR_ERRORSTRIPE_SCROLLBAR_INSETS, new Insets(16, 0, 16, 0),
        }; 
        return result;
    }

    public Object[] createApplicationSpecificKeysAndValues () {
        Border outerBorder = BorderFactory.createLineBorder(UIManager.getColor("controlShadow")); //NOI18N
        Object propertySheetColorings = new MetalPropertySheetColorings();
        Color unfocusedSelBg = UIManager.getColor("controlShadow");
        if (!Color.WHITE.equals(unfocusedSelBg.brighter())) { // #57145
            unfocusedSelBg = unfocusedSelBg.brighter();
        }

        Object[] result = {
            DESKTOP_BORDER, new EmptyBorder(1, 1, 1, 1),
            SCROLLPANE_BORDER, new MetalScrollPaneBorder(),
            EXPLORER_STATUS_BORDER, new StatusLineBorder(StatusLineBorder.TOP),
            EDITOR_STATUS_LEFT_BORDER, new StatusLineBorder(StatusLineBorder.TOP | StatusLineBorder.RIGHT),
            EDITOR_STATUS_RIGHT_BORDER, new StatusLineBorder(StatusLineBorder.TOP | StatusLineBorder.LEFT),
            EDITOR_STATUS_INNER_BORDER, new StatusLineBorder(StatusLineBorder.TOP | StatusLineBorder.LEFT | StatusLineBorder.RIGHT),
            EDITOR_STATUS_ONLYONEBORDER, new StatusLineBorder(StatusLineBorder.TOP),
            EDITOR_TOOLBAR_BORDER, new EditorToolbarBorder(),

            PROPERTYSHEET_BOOTSTRAP, propertySheetColorings,

            //UI Delegates for the tab control
            EDITOR_TAB_DISPLAYER_UI, "org.netbeans.swing.tabcontrol.plaf.MetalEditorTabDisplayerUI",
            VIEW_TAB_DISPLAYER_UI, "org.netbeans.swing.tabcontrol.plaf.MetalViewTabDisplayerUI",
            SLIDING_BUTTON_UI, "org.netbeans.swing.tabcontrol.plaf.MetalSlidingButtonUI",

            EDITOR_TAB_OUTER_BORDER, outerBorder,
            VIEW_TAB_OUTER_BORDER, outerBorder,

            EXPLORER_MINISTATUSBAR_BORDER, BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("controlShadow")),
            
            //#48951 invisible unfocused selection background in Metal L&F
            "nb.explorer.unfocusedSelBg", unfocusedSelBg,
                    
            PROGRESS_CANCEL_BUTTON_ICON, UIUtils.loadImage("org/netbeans/swing/plaf/resources/cancel_task_win_linux_mac.png"),
                    

            // progress component related
//            "nbProgressBar.Foreground", new Color(49, 106, 197),
//            "nbProgressBar.Background", Color.WHITE,
            "nbProgressBar.popupDynaText.foreground", new Color(115, 115, 115),
//            "nbProgressBar.popupText.background", new Color(231, 249, 249),        
            "nbProgressBar.popupText.foreground", UIManager.getColor("TextField.foreground"),
            "nbProgressBar.popupText.selectBackground", UIManager.getColor("List.selectionBackground"),
            "nbProgressBar.popupText.selectForeground", UIManager.getColor("List.selectionForeground"),                    

        }; //NOI18N

        //#108517 - turn off ctrl+page_up and ctrl+page_down mapping
        return UIUtils.addInputMapsWithoutCtrlPageUpAndCtrlPageDown( result );
    }

    private class MetalPropertySheetColorings extends UIBootstrapValue.Lazy {
        public MetalPropertySheetColorings () {
            super (null);
        }

        public Object[] createKeysAndValues() {
            return new Object[] {
                //Property sheet settings as defined by HIE
                 PROPSHEET_SELECTION_BACKGROUND, new Color(204,204,255),
                 PROPSHEET_SELECTION_FOREGROUND, Color.BLACK,
                 PROPSHEET_SET_BACKGROUND, new Color(224,224,224),
                 PROPSHEET_SET_FOREGROUND, Color.BLACK,
                 PROPSHEET_SELECTED_SET_BACKGROUND, new Color(204,204,255),
                 PROPSHEET_SELECTED_SET_FOREGROUND, Color.BLACK,
                 PROPSHEET_DISABLED_FOREGROUND, new Color(153,153,153),
            };
        }
    }

}
