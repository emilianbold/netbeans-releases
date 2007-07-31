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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.lib.uihandler;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.JButton;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Tulach
 */
final class Decorations {
    
    private static final SimpleFormatter FORMATTER = new SimpleFormatter();
    
    public static void decorate(LogRecord r, Decorable d) {
        if (r.getMessage() == null) {
            d.setName("Seq: " + r.getSequenceNumber());
        } else {
            d.setName(r.getMessage());
        }
        if (r.getResourceBundle() != null) {
            try {
                String msg = r.getResourceBundle().getString(r.getMessage());
                if (r.getParameters() != null) {
                    msg = MessageFormat.format(msg, r.getParameters());
                }
                d.setDisplayName(msg);
            } catch (MissingResourceException ex) {
                Logger.getAnonymousLogger().log(Level.INFO, null, ex);
            }
            
            
            try {
                String iconBase = r.getResourceBundle().getString(r.getMessage() + "_ICON_BASE"); // NOI18N
                d.setIconBaseWithExtension(iconBase);
            } catch (MissingResourceException ex) {
                // ok, use default
                d.setIconBaseWithExtension("org/netbeans/lib/uihandler/def.png");
            }
        }
        
        
        String htmlKey;
        if (r.getThrown() != null) {
            d.setIconBaseWithExtension("org/netbeans/lib/uihandler/exception.gif");
            htmlKey = "HTML_exception";
        }
        
        if ("UI_ACTION_BUTTON_PRESS".equals(r.getMessage())) { // NOI18N
            d.setDisplayName(cutAmpersand(getParam(r, 4)));
            String thru = getParam(r, 1, String.class);
            if ((thru != null && thru.contains("Toolbar")) || getParam(r, 0, Object.class) instanceof JButton) {
                d.setIconBaseWithExtension("org/netbeans/lib/uihandler/toolbars.gif");
                htmlKey = "HTML_toolbar";
            } else if (thru != null && thru.contains("MenuItem")) {
                d.setIconBaseWithExtension("org/netbeans/lib/uihandler/menus.gif");
                htmlKey = "HTML_menu";
            }
        } else if ("UI_ACTION_KEY_PRESS".equals(r.getMessage())) { // NOI18N
            d.setDisplayName(cutAmpersand(getParam(r, 4)));
            d.setIconBaseWithExtension("org/netbeans/lib/uihandler/key.png");
            htmlKey = "HTML_key";
        } else if ("UI_ACTION_EDITOR".equals(r.getMessage())) { // NOI18N
            d.setDisplayName(cutAmpersand(getParam(r, 4)));
            d.setIconBaseWithExtension("org/netbeans/lib/uihandler/key.png");
            htmlKey = "HTML_key";
        } else if ("UI_ENABLED_MODULES".equals(r.getMessage())) { // NOI18N
            d.setDisplayName(NbBundle.getMessage(Decorations.class, "MSG_EnabledModules"));
            d.setIconBaseWithExtension("org/netbeans/lib/uihandler/module.gif");
            htmlKey = null;
        } else if ("UI_DISABLED_MODULES".equals(r.getMessage())) { // NOI18N
            d.setDisplayName(NbBundle.getMessage(Decorations.class, "MSG_DisabledModules"));
            d.setIconBaseWithExtension("org/netbeans/lib/uihandler/module.gif");
            htmlKey = null;
        } else if ("UI_USER_CONFIGURATION".equals(r.getMessage())){// NOI18N
            d.setDisplayName(NbBundle.getMessage(Decorations.class, "MSG_USER_CONFIGURATION"));
            htmlKey = null;
        }
        
        
        
        
        d.setShortDescription(FORMATTER.format(r));
        
    }

    private static <T> T getParam(LogRecord r, int index, Class<T> type) {
        if (r == null || r.getParameters() == null || r.getParameters().length <= index) {
            return null;
        }
        Object o = r.getParameters()[index];
        return type.isInstance(o) ? type.cast(o) : null;
    }
    private static String getParam(LogRecord r, int index) {
        Object[] arr = r.getParameters();
        if (arr == null || arr.length <= index || !(arr[index] instanceof String)) {
            return "";
        }
        return (String)arr[index];
    }
    static String cutAmpersand(String text) {
        // XXX should this also be deprecated by something in Mnemonics?
        int i;
        String result = text;

        /* First check of occurence of '(&'. If not found check
          * for '&' itself.
          * If '(&' is found then remove '(&??'.
          */
        i = text.indexOf("(&"); // NOI18N

        if ((i >= 0) && ((i + 3) < text.length()) && /* #31093 */
                (text.charAt(i + 3) == ')')) { // NOI18N
            result = text.substring(0, i) + text.substring(i + 4);
        } else {
            //Sequence '(&?)' not found look for '&' itself
            i = text.indexOf('&');

            if (i < 0) {
                //No ampersand
                result = text;
            } else if (i == (text.length() - 1)) {
                //Ampersand is last character, wrong shortcut but we remove it anyway
                result = text.substring(0, i);
            } else {
                //Remove ampersand from middle of string
                //Is ampersand followed by space? If yes do not remove it.
                if (" ".equals(text.substring(i + 1, i + 2))) {
                    result = text;
                } else {
                    result = text.substring(0, i) + text.substring(i + 1);
                }
            }
        }

        return result;
    }
}
