/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */
package org.netbeans.installer.infra.build.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.Utils;

/**
 * This class is an ant task which is capable of loading localized properties data.
 * 
 * @author Kirill Sorokin
 */
public class LoadLocales extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * The basename of the .properties file.
     */
    private String basename;
    
    /**
     * Name of the property whose value should be set to the list of found locales.
     */
    private String localesList;
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the 'basename' property.
     * 
     * @param basename New value for the 'basename' property.
     */
    public void setBasename(final String basename) {
        this.basename = basename;
    }
    
    /**
     * Setter for the 'localesList' property.
     * 
     * @param localesList New value for the 'localesList' property.
     */
    public void setList(final String localesList) {
        this.localesList = localesList;
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    /**
     * Executes the task. The properties are loaded from the resource file with the
     * given base name and the corresponding project properties are set. 
     * 
     * @throws org.apache.tools.ant.BuildException if an I/O error occurs.
     */
    public void execute() throws BuildException {
        Utils.setProject(getProject());
        
        String locales = "";                                                // NOI18N
        
        try {
            // handle the default locale
            File file  = new File(basename + ".properties");                // NOI18N
            if (!file.equals(file.getAbsoluteFile())) {
                file = new File(
                        getProject().getBaseDir(), 
                        basename + ".properties");                          // NOI18N
            }
            
            Properties properties = new Properties();
            
            properties.load(new FileInputStream(file));
            for (Object key: properties.keySet()) {
                getProject().setProperty(
                        key + ".default",                                   // NOI18N
                        properties.get(key).toString());
            }
            
            for (Locale locale: Locale.getAvailableLocales()) {
                file = new File(basename + "_" + locale + ".properties");   // NOI18N
                if (file.exists()) {
                    locales += " " + locale;                                // NOI18N
                    properties = new Properties();
                    properties.load(new FileInputStream(file));
                    
                    for (Object key: properties.keySet()) {
                        getProject().setProperty(
                                "" + key + "." + locale,                    // NOI18N
                                Utils.toAscii(properties.get(key).toString()));
                    }
                }
            }
        } catch (IOException e) {
            throw new BuildException(e);
        }
        
        getProject().setProperty(localesList, locales.trim());
    }
}
