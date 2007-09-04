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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package com.sun.rave.web.ui.util;

import java.util.Locale;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import com.sun.rave.web.ui.theme.Theme;
import com.sun.rave.web.ui.theme.ThemeFactory;
import com.sun.rave.web.ui.theme.ThemeManager;

/**
 * Utilities needed by Sun Web Components to
 * retrieve an appropriate Theme.
 * @author avk
 */
public class ThemeUtilities {

    private final static boolean DEBUG = false;

    /**
     * Use this method to retrieve a named Theme.
     * @param context The FacesContext for the request.
     * @return A Locale-specific Theme
     */
    public static Theme getTheme(FacesContext context) {

        if(DEBUG) log("getTheme()");

        // First, get the ThemeManager
        ThemeManager manager = null;
      
        ExternalContext appContext = context.getExternalContext(); 
  
        Object themeManagerObject = appContext.getApplicationMap().
            get(ThemeManager.THEME_MANAGER);
        if(themeManagerObject != null) {
	    if(DEBUG) log("Found ThemeManager"); //NOI18N
            if(themeManagerObject instanceof ThemeManager) {
                manager =  (ThemeManager)themeManagerObject;
            } 
            else {
                StringBuffer msgBuffer = new StringBuffer("ERROR WHILE RETRIEVING THEME: ");
                msgBuffer.append(" Session attribute of name ");
                msgBuffer.append(Theme.THEME_ATTR);
                msgBuffer.append(" is not of type Theme");
                throw new RuntimeException(msgBuffer.toString());
            }
        } 
//        else {
//	    if(DEBUG) log("Initializing ThemeManager"); //NOI18N
//            manager = 
//                ThemeFactory.initializeThemeManager(appContext);
//        }
        
        Map sessionAttributes = appContext.getSessionMap(); 
     
        String themeName = null;     
        Object themeObject = sessionAttributes.get(Theme.THEME_ATTR);
        if(themeObject != null) { 
	    if(DEBUG) log("Found themeName in session"); //NOI18N
            themeName = themeObject.toString(); 
	    // Need to check this for Creator
	    if(themeName.length() == 0) { 
		themeName = manager.getDefaultThemeName(); 
		sessionAttributes.put(Theme.THEME_ATTR, themeName);
	    }
        }
        else { 
	    if(DEBUG) log("Getting default themename"); //NOI18N
            themeName = manager.getDefaultThemeName(); 
            sessionAttributes.put(Theme.THEME_ATTR, themeName);
        }
        if(DEBUG) log("\tTheme name is " + themeName);
        Locale locale = context.getViewRoot().getLocale();
       
        if(locale == null) { 
            locale = Locale.getDefault();
            log("\tWARNING: couldn't get locale from the view root.");
            log("\tUsing system locale " + locale.toString());
        }
        else if(DEBUG) { 
            log("\tUsing locale from viewroot " + locale.toString());
        }
        return manager.getTheme(themeName, locale);
       	
    }
    private static void log(String s) { 
        System.out.println(ThemeUtilities.class.getName() + "::" + s);
    }
}
