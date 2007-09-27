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
package org.netbeans.modules.groovy.grails.settings;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author schmidtm
 */
public class Settings {
    
    private static Settings instance = null;
    private static final String GRAILS_HOME_KEY = "grailsHome";
    
    protected Settings () {}
    
    public static Settings getInstance() {
        if(instance == null){
            instance = new Settings();
        }
        
        return instance;
    }
    
    
    private Preferences getPreferences() {
        return NbPreferences.forModule( Settings.class );
    }
    
    public String getGrailsBase() {
        return getPreferences().get(GRAILS_HOME_KEY, null);
        }

    public void setGrailsBase(String path) {
        getPreferences().put(GRAILS_HOME_KEY, path);
        }

}
