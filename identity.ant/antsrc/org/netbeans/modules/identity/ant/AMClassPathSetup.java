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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
