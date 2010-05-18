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
package com.sun.rave.web.ui.theme;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;


/**
 * <p>The ThemeManager manages the locale specific
 * versions of each Theme. The ThemeManager is
 * created by the ThemeConfigurationListener
 * and placed in an application parameter
 * of the same name as the theme itself.<p>
 * <p>Components do not need to interact
 * with the ThemeManager directly - use
 * <code>com.sun.rave.web.ui.util.ThemeUtilities</code>
 * instead.</p>
 * <p>To specify a default theme, set it in a context
 * init parameter in the deployment descriptor. Use
 * the parameter name <code>com.sun.rave.web.ui.DefaultTheme</code>.
 * @author avk
 */
public class ThemeManager {

    /**
     * The context attribute name used to 
     * place/retrieve the ThemeManager.
     */
    public final static String THEME_MANAGER =
            "com.sun.rave.web.ui.ThemeManager"; //NOI18N

    
    private static String defaultTheme = null;
    private Locale defaultLocale = null;
    private HashMap themes;
    private boolean populated = false; 
    private ThemeManager themeManager = null;
    private static final boolean DEBUG = false; 
    private String messageResource = null; 
    
    /**
     * Constructor for the ThemeManager
     */
    protected ThemeManager() {       
        themes = new HashMap();
    }
        
    /**
     * This method adds a named Theme to the ThemeManager.
     * @param name The name for which this Theme was created
     * @param themeMap The mapObject of themes
     */
    protected void addThemeMap(String name, Map themeMap) { 
        if(DEBUG) log("\tNow adding theme map for " + name);
        themes.put(name, themeMap);    
    }
    
    /**
     * <p>Retrieve a Theme instance for a Theme and for a specified locale.</p>
     * <p>If no Theme instances can be found for the specified name, the method 
     * uses the name of the default theme instead. If no default theme has been 
     * specified, any available theme name will be used. If none is found, 
     * a ThemeConfigurationException is thrown.</p>
     * <p> If no Theme instance can be found for the locale, the default locale
     * from the <code>faces-config.xml</code> file is used. If no default theme
     * was specified, any theme instance will be used. If no theme instances 
     * are found, a ThemeConfigurationException is thrown.</p>
     * 
     * @param name The for which this Theme was created
     * @param locale The locale for which the Theme instance is needed
     * @return The Theme for the locale
     */
    public Theme getTheme(String name, Locale locale) {
        
        // "themes" is a map which manages the individual theme maps. 
        // First see if there is a value for the name specified in the 
        // method parameter. (themes is not null, it is created on 
        // startup). 
        
        Object mapObject = themes.get(name);
        
        // If there is no value, try to find another theme map. 
        if(mapObject== null) {
            
            // Check if there are no themes (we don't want to do perform this
            // test at the start of this method for performance reasons - it is 
            // invoked a lot and this fallback mechanism is only used when there
            // is a misconfiguration. 
            if(themes.isEmpty()) {
                String message =
                        "CONFIGURATION ERROR: no theme resources library available";
                throw new ThemeConfigurationException(message);
            }
            
            log("WARNING: theme " + name + " has not been initialized.");
          
            // If a default theme name was specified, try to get the theme map
            // for the default theme.
            if(defaultTheme != null) {
                mapObject = themes.get(defaultTheme);
                if(mapObject != null) { 
                    log("Using the default theme " + defaultTheme);
                } 
            } else {
                log("WARNING: no default theme name available either, using any theme!");
            }
            
            if(mapObject == null) { 
                mapObject = themes.values().iterator().next();
            } 
        }
        
        Map themeMap = (Map)mapObject;     
        Theme theme = null;
        Object object = themeMap.get(locale);
        
        if(object == null) {
            log("No theme instance found for locale " + locale.getDisplayName()); 
     
            if(defaultLocale != null) {
                log("Trying to use the default locale " + 
                     defaultLocale.getDisplayName()); 
                object = themeMap.get(defaultLocale);
            }
            if(object == null && !themeMap.isEmpty()) {
                 log("Trying to use any theme instance"); 
                object = themeMap.values().iterator().next();
            }
            if(object == null) {
                String message =
                        "CONFIGURATION ERROR: no theme resources library available";
                throw new ThemeConfigurationException(message);
            }
        }
        return (Theme)object;
    }
    
    /**
     * Use this method to specify the default theme for 
     * the web application
     * @param name The name of the default Theme
     */
    protected void setDefaultThemeName(String name) { 
       if(name != null && name.length() > 0) { 
           defaultTheme = name;
       }
       checkDefaultThemeName();
    }
    
     /**
     * Use this method to specify the default locale for 
     * the web application
     * @param defaultLocale The defaultLocale
     */
    protected void setDefaultLocale(Locale defaultLocale) { 
        this.defaultLocale = defaultLocale; 
    }
    
    /**
     * Use this method to retrieve the name of the default
     * Theme for the locale.
     * @return The default Theme's name
     */
    public String getDefaultThemeName() { 
        return defaultTheme; 
    }
     
    
    /**
     * String representation of this class
     * @return The string representation of this class
     */
    public String toString() { 
        StringBuffer buffer = 
            new StringBuffer("ThemeManager for Sun Web Componenents."); 
        buffer.append("\nDefault theme is "); 
        buffer.append(defaultTheme);
        buffer.append(".\nAvailable themes: "); 
        Iterator iterator = themes.keySet().iterator(); 
        while(iterator.hasNext()) { 
            buffer.append(iterator.next()); 
            buffer.append(" ");
        }
        buffer.append(".\nAvailable locales: "); 
        iterator = ((Map)(themes.get(defaultTheme))).keySet().iterator(); 
        while(iterator.hasNext()) { 
            buffer.append(iterator.next()); 
            buffer.append(" ");
        }
        return buffer.toString();
    } 
   
    /**
     * Use this method to check if name is a valid themename
     * @param name A name
     * @return true if the manager has a theme of the name, false otherwise
     */
    private void checkDefaultThemeName() { 
        boolean warn = false; 
        if(defaultTheme != null) { 
            if(themes.containsKey(defaultTheme)) { 
                return;
            }
            else { 
                warn = true;
                log("WARNING: default theme name " + defaultTheme + " is invalid"); 
            }
        }
        
        Iterator keys = themes.keySet().iterator();
        if(keys.hasNext()) {
            defaultTheme = keys.next().toString();
        }   
        
        if(warn) { 
           log("Using defaultTheme " + defaultTheme + "instead");
            
        }
    }
    
    private void log(String s) { 
        System.out.println(this.getClass().getName() + "::" +s);
    }
}
