/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.compapp.projects.jbi;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;
import javax.management.MalformedObjectNameException;
import org.netbeans.modules.sun.manager.jbi.management.AdministrationService;
import org.netbeans.modules.sun.manager.jbi.util.ServerInstance;
import org.netbeans.modules.sun.manager.jbi.util.ServerInstanceReader;

/**
 *
 * @author jqian
 */
public class AdministrationServiceHelper {
    
    public static AdministrationService getAdminService(String j2eeServerInstance) 
            throws MalformedURLException, IOException, MalformedObjectNameException {
        String netBeansUserDir = System.getProperty("netbeans.user");  // NOI18N
        return getAdminService(netBeansUserDir, j2eeServerInstance);
    }
        
    public static AdministrationService getAdminService(String netBeansUserDir,
            String j2eeServerInstance)
            throws MalformedURLException, IOException, MalformedObjectNameException {
        
        ServerInstance instance = getServerInstance(netBeansUserDir, j2eeServerInstance);        
        return new AdministrationService(instance);
    }
    
    public static ServerInstance getServerInstance(String netBeansUserDir,
            String j2eeServerInstance) {
        
        ServerInstance instance = null;
        
        if (netBeansUserDir != null) {
            
            String settingsFileName = 
                    netBeansUserDir + ServerInstanceReader.RELATIVE_FILE_PATH;
            
            File settingsFile = new File(settingsFileName);
            
            if (!settingsFile.exists()) {
                throw new RuntimeException(
                        "The application server definition file "
                        + settingsFileName + " is missing.");
            }
            
//          System.out.println("Retrieving settings from " + settingsFileName);
            ServerInstanceReader reader = null;
            try {
                reader = new ServerInstanceReader(settingsFileName);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
            
            List<ServerInstance> list = reader.getServerInstances();
            Iterator<ServerInstance> iterator = list.iterator();
            
            String osName = System.getProperty("os.name");
            boolean isWindows = osName.indexOf("Windows") != -1;;
            
            while (iterator.hasNext()) {
                instance = iterator.next();
                // If j2eeServerInstance is not defined from ant,
                // we simply use the first server defined in the setting
                // file, as in the test driver case.
                if (j2eeServerInstance == null
                        || j2eeServerInstance.trim().length() == 0
                        || isWindows && j2eeServerInstance.equalsIgnoreCase(instance.getUrl())
                        || !isWindows && j2eeServerInstance.equals(instance.getUrl())
                        ) {
                    break;
                } else {
                    instance = null;
                }
            }
            
            if (instance == null) {
                String LINE_SEPARATOR = System.getProperty("line.separator");
                StringBuffer msgSB = new StringBuffer();
                msgSB.append("The application server definition file ");
                msgSB.append(settingsFileName);
                msgSB.append(" is corrupted or it doesn't contain the target server instance.");
                msgSB.append(LINE_SEPARATOR);
                
                if (j2eeServerInstance != null
                        && j2eeServerInstance.trim().length() > 0) {
                    msgSB.append("The target server instance is ");
                    msgSB.append(j2eeServerInstance);
                    msgSB.append(LINE_SEPARATOR);
                }
                
                msgSB.append("The application server definition file contains: ");
                msgSB.append(LINE_SEPARATOR);
                
                iterator = list.iterator();
                while (iterator.hasNext()) {
                    instance = iterator.next();
                    msgSB.append("    ");
                    msgSB.append(instance.getUrl());
                    msgSB.append(LINE_SEPARATOR);
                }
                
                throw new RuntimeException(msgSB.toString());
            }
        }
        
        return instance;
    }    
}
