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
package org.netbeans.modules.identity.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 *
 */
public class AMClassPathSetup extends Task {
    private static final String J2EE_PLATFORM_CLASSPATH_PROP = "j2ee.platform.classpath";   //NOI18N
    
    private static final String JAR_LOCATION = File.separator + "addons" + 
            File.separator + "accessmanager" + File.separator; //NOI18N
    
    private static final String CONFIG_LOCATION = File.separator + "domains" + 
            File.separator + "domain1" + File.separator + "config";        //NOI18N
    
    private static final String AM_WEB_SERVICES_PROVIDER_JAR = JAR_LOCATION + "amWebservicesProvider.jar";     //NOI18N
    
    private static final String AM_CLIENT_SDK_JAR = JAR_LOCATION + "amclientsdk.jar";      //NOI18N
  
    private static final String CLASSPATH_SEPARATOR = ":";  //NOI18N
    
    private static final String AM_SUFFIX = "_am";       //NOI18N
    
    private String propertiesFile; 
    private String asRoot;
    
    public void setPropertiesfile(String path) {
        this.propertiesFile = path;
    }
    
    public void setAsroot(String asRoot) {
        this.asRoot = asRoot;
    }
    
    public void execute() throws BuildException {
        Properties properties = new Properties();
        FileInputStream is = null;
        FileOutputStream os = null;
        
        try {
            is = new FileInputStream(propertiesFile);
            properties.load(is);
            
            String classPath = properties.getProperty(J2EE_PLATFORM_CLASSPATH_PROP);
            
            classPath = classPath + CLASSPATH_SEPARATOR + asRoot + AM_WEB_SERVICES_PROVIDER_JAR +
                    CLASSPATH_SEPARATOR + asRoot + AM_CLIENT_SDK_JAR +
                    CLASSPATH_SEPARATOR + asRoot + CONFIG_LOCATION;
            
            //System.out.println("classPath = " + classPath);
            properties.setProperty(J2EE_PLATFORM_CLASSPATH_PROP, classPath);
            
            os = new FileOutputStream(propertiesFile + AM_SUFFIX);
           
            properties.store(os, "");   //NOI18N
            
        } catch (IOException ex) {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex2) {
                    // ignore
                }
            }
            
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ex2) {
                    // ignore
                }
            }
        }
   
    }
}
