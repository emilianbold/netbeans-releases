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

package org.netbeans.jellytools.modules.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.netbeans.jellytools.Bundle;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.sun.ide.j2ee.PlatformValidator;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.AddDomainWizardIterator;
import org.openide.WizardDescriptor;

/**
 *
 * @author Michal Mocnak
 */
public class Util {
    
    public static final int SLEEP = 15000;
    
    // SERVER PROPERTIES FOR TESTS
    public static final String _SEP = System.getProperty("file.separator");
    public static final String _DISPLAY_NAME = Bundle.getString("org.netbeans.modules.j2ee.sun.ide.Bundle",
                "LBL_GlassFishV2");
    public static final String _PLATFORM_LOCATION = "/space/hudson/glassfish";
    public static final String _INSTALL_LOCATION = _PLATFORM_LOCATION+_SEP+"domains";
    public static final String _DOMAIN = "domain1";
    public static final String _HOST = "localhost";
    public static final String _PORT = getPort(new File(_INSTALL_LOCATION+_SEP+_DOMAIN+_SEP+"config"+_SEP+"domain.xml"));
    public static final String _USER_NAME = "admin";
    public static final String _PASSWORD = System.getProperty("sjsas.server.password","adminadmin");
    public static final String _URL = "["+_PLATFORM_LOCATION+"]deployer:Sun:AppServer::"+_HOST+":"+_PORT;
    
    // SERVER PROPERTIES FOR APP SERVER REGISTRATION
    public static final String USER_NAME = "username";
    public static final String PASSWORD = "password";
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String PLATFORM_LOCATION = "platform_location";
    public static final String INSTALL_LOCATION = "install_location";
    public static final String DOMAIN = "domain";
    public static final String TYPE = "type";
    public static final String PROP_DISPLAY_NAME = "ServInstWizard_displayName";
    
    /**
     * It returns admin port number if the server.
     */
    public static String getPort(File domainXml){
        String adminPort = null;
        String buffer = null;
        
        try {
            FileReader reader = new FileReader(domainXml);
            BufferedReader br = new BufferedReader(reader);
            
            while((buffer = br.readLine()) != null) {
                if(buffer.indexOf("admin-listener") > -1) {
                    int x = buffer.indexOf(34, buffer.indexOf("port"));
                    int y = buffer.indexOf(34, ++x);
                    adminPort = buffer.substring(x, y);
                    break;
                }
            }
            
            br.close();
            reader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return adminPort;
    }
    
    public static void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch(Exception e) {
            // Nothing to do
        }
    }
    
   public static void addSjsasInstance() throws Exception {
        
            AddDomainWizardIterator inst = new AddDomainWizardIterator(new PlatformValidator());
            WizardDescriptor wizard = new WizardDescriptor(new WizardDescriptor.Panel[] {});
            wizard.putProperty(Util.PLATFORM_LOCATION, new File(Util._PLATFORM_LOCATION));
            wizard.putProperty(Util.INSTALL_LOCATION, Util._INSTALL_LOCATION);
            wizard.putProperty(Util.PROP_DISPLAY_NAME, Util._DISPLAY_NAME);
            wizard.putProperty(Util.HOST, Util._HOST);
            wizard.putProperty(Util.PORT, Util._PORT);
            wizard.putProperty(Util.DOMAIN, Util._DOMAIN);
            wizard.putProperty(Util.USER_NAME, Util._USER_NAME);
            wizard.putProperty(Util.PASSWORD, Util._PASSWORD);

            inst.initialize(wizard);
            inst.instantiate();

            ServerRegistry.getInstance().checkInstanceExists(Util._URL);

            Util.sleep(SLEEP);       
    }

    public static void removeSjsasInstance() throws Exception {

        Util.sleep(SLEEP);

        ServerInstance inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
        boolean wasRunning = inst.isRunning();

        inst.remove();

        if (wasRunning) {
            Util.sleep(SLEEP);
        }

        try {
            ServerRegistry.getInstance().checkInstanceExists(Util._URL);
        } catch(Exception e) {
            if (wasRunning && inst.isRunning())
                throw new Exception("remove did not stop the instance");
            String instances[] = ServerRegistry.getInstance().getInstanceURLs();
            if (null != instances)
                if (instances.length > 1)
                    throw new Exception("too many instances");
            return;
        }

        throw new Exception("Sjsas instance still exists !");
    }
    
}

