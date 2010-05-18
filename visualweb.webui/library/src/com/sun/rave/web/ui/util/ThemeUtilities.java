/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        } else {
	    if(DEBUG) log("Initializing ThemeManager"); //NOI18N
            manager = 
                ThemeFactory.initializeThemeManager(appContext);
        }
        
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
