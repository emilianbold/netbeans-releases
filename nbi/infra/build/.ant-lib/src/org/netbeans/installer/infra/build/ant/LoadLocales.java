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
package org.netbeans.installer.infra.build.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.installer.infra.build.ant.utils.AntUtils;

public class LoadLocales extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private String basename = null;
    private String localesList = null;
    
    // setters //////////////////////////////////////////////////////////////////////
    public void setBasename(final String basename) {
        this.basename = basename;
    }
    
    public void setList(final String localesList) {
        this.localesList = localesList;
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    public void execute() throws BuildException {
        AntUtils.setProject(getProject());
        
        String     locales = "";
        File       file    = null;
        Properties props   = null;
        
        try {
            // handle the default locale
            file  = new File(basename + ".properties");
            props = new Properties();
            
            props.load(new FileInputStream(file));
            for (Object key: props.keySet()) {
                getProject().setProperty("" + key + ".default", "" + props.get(key));
            }
            
            for (Locale locale: Locale.getAvailableLocales()) {
                file = new File(basename + "_" + locale + ".properties");
                if (file.exists()) {
                    locales += " " + locale;
                    props = new Properties();
                    props.load(new FileInputStream(file));
                    
                    for (Object key: props.keySet()) {
                        getProject().setProperty("" + key + "." + locale, AntUtils.toAscii("" + props.get(key)));
                    }
                }
            }
        } catch (IOException e) {
            throw new BuildException(e);
        }
        
        getProject().setProperty(localesList, locales.trim());
    }
}
