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

package org.netbeans.modules.j2ee.sun.ide.j2ee.ui;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.sun.api.ServerLocationManager;
import org.netbeans.modules.j2ee.sun.ide.dm.SunDeploymentManager;
import org.netbeans.modules.j2ee.sun.ide.j2ee.PluginProperties;
import org.netbeans.modules.j2ee.sun.ide.j2ee.Utils;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.sun.ide.j2ee.PlatformValidator;

/**
 * Helper class which serves to create and register a new domain without
 * any use of the ui.
 *
 * @author Michal Mocnak, 
 * @author vkraemer
 */
public final class DomainCreator {
    
    //  default values
    
    private final static String DEFAULT_SERVER_NAME = 
            NbBundle.getMessage(SunDeploymentManager.class, "FACTORY_DISPLAYNAME"); // NOI18N
    private final static String DEFAULT_USERNAME = "admin";                             // NOI18N
    private final static String DEFAULT_PASSWORD = "adminadmin";                        // NOI18N
    private final static String DEFAULT_HOST = "localhost";                             // NOI18N
    private final static String DEFAULT_DOMAIN_NAME = "personalDomain";                // NOI18N
    private final static int DEFAULT_ADMIN_PORT = 4848;
    private final static int DEFAULT_HTTP_PORT = 8080;
    private final static int DEFAULT_JMX_PORT = 8686;
    private final static int DEFAULT_JMS_PORT = 7676;
    private final static int DEFAULT_ORB_PORT = 3700;
    private final static int DEFAULT_HTTPS_PORT = 8181;
    private final static int DEFAULT_ORB_SSL_PORT = 3820;
    private final static int DEFAULT_ORB_MUTUALAUTH_PORT = 3920;
    private static int DEFAULT_SIP_PORT = 5060;
    private static int DEFAULT_SIP_SSL_PORT = 5061;
    
    // properties
    
    private final static String PROP_USER_NAME = "username";                            // NOI18N
    private final static String PROP_PASSWORD = "password";                             // NOI18N
    private final static String PROP_HOST = "host";                                     // NOI18N
    private final static String PROP_PORT = "port";                                     // NOI18N
    private final static String PROP_PLATFORM_LOCATION = "platform_location";           // NOI18N
    private final static String PROP_INSTALL_LOCATION = "install_location";             // NOI18N
    private final static String PROP_DOMAIN = "domain";                                 // NOI18N
    private final static String PROP_INSTANCE_PORT = "instance_port";                   // NOI18N
    private final static String PROP_JMS_PORT = "jms_port";                             // NOI18N
    private final static String PROP_ORB_LISTENER_PORT = "orb_listener_port";           // NOI18N
    private final static String PROP_ORB_SSL_PORT = "orb_ssl_port";                     // NOI18N
    private final static String PROP_HTTP_SSL_PORT = "http_ssl_port";                   // NOI18N
    private final static String PROP_ORB_MUTUAL_AUTH_PORT = "orb_mutual_auth_port";     // NOI18N
    private final static String PROP_ADMIN_JMX_PORT = "admin_jmx_port";                 // NOI18N
    private final static String PROP_SIP_PORT = "sip_port";                             // NOI18N
    private final static String PROP_SIP_SSL_PORT = "sip_ssl_port";                     // NOI18N
    private final static String PROP_TYPE = "type";                                     // NOI18N
    private final static String PROP_DISPLAY_NAME = "ServInstWizard_displayName";       // NOI18N
    private WizardDescriptor wizardDescriptor;
    private AddDomainWizardIterator wizard;
    
    /**
     *  Do not allow to create instances of this class.
     */
    private DomainCreator() {
        wizard = new AddDomainWizardIterator(new PlatformValidator());
        wizardDescriptor = new WizardDescriptor(new Panel[] {});
    }
    
    /**
     * Creates a default domain (.personalDomain) in user's home dir
     *
     * @param serverPath full path to the SJSAS
     * @return the InstanceProperties object, null if domain is not created
     *          or if already registered.
     */
    public static InstanceProperties createPersonalDefaultDomain(String serverPath) {
        String homeDir = System.getProperty("user.home");                       // NOI18N
        return createPersonalDomain(serverPath, 
                homeDir + File.separator + "." +DEFAULT_DOMAIN_NAME);           // NOI18N
    }
    
    /**
     * Creates a specific domain
     *
     * @param serverPath absolute path to the SJSAS root
     * @param domainPath absolute path to the domain root
     * @return the InstanceProperties object, null if domain is not created
     *          or if already registered.
     */
    public static InstanceProperties createPersonalDomain(String serverPath, 
            String domainPath) {
        File serverRoot = new File(serverPath);
        File domainRoot = new File(domainPath);
        InstanceProperties retVal = null;
        
        // Check if serverRoot location is valid glassfish instance
        if (ServerLocationManager.isGoodAppServerLocation(serverRoot)) {
            
            // If any instance of the server is registered return InstanceProperties
            // of that glassfish server instance
            String serverID = "[" + serverRoot.getAbsolutePath() + "]deployer:Sun:AppServer::" + DEFAULT_HOST + ":" + // NOI18N
                    DEFAULT_ADMIN_PORT;
            retVal = InstanceProperties.getInstanceProperties(serverID);
            
            if(null == retVal) {                
                // If domain already exists create another one
                int counter = 1;
                
                while (domainRoot.exists()) {
                    domainRoot = new File(domainPath + counter++);
                }
                
                // if domainRoot parent is not writable inform user and return null
                if(Utils.canWrite(domainRoot.getParentFile())) {
                    
                    // Creates domain factory
                    DomainCreator factory = new DomainCreator();
                    
                    // try to find a good spot for the ports that won't clash 
                    // with another user... this can never be completely correct.
                    int bucket = 1;
                    String home = System.getProperty("user.home");              // NOI18N
                    if (null != home && home.trim().length() > 0) {
                        bucket = home.hashCode() % 10000;
                    }
                    
                    if (bucket < 0) {
                        bucket = -bucket;
                    } else if (0 == bucket) {
                        bucket = 1;
                    }
                    
                    // Checks ports availability
                    int adminPort = DEFAULT_ADMIN_PORT + bucket;
                    int httpPort = DEFAULT_HTTP_PORT + bucket;
                    int jmsPort = DEFAULT_JMS_PORT + bucket;
                    int orbPort = DEFAULT_ORB_PORT + bucket;
                    int httpsPort = DEFAULT_HTTPS_PORT + bucket;
                    int orbSSLPort = DEFAULT_ORB_SSL_PORT + bucket;
                    int orbMAPort = DEFAULT_ORB_MUTUALAUTH_PORT + bucket;
                    int jmxPort = DEFAULT_JMX_PORT + bucket;
                    int sipPort = DEFAULT_SIP_PORT + bucket;
                    int sipSslPort = DEFAULT_SIP_SSL_PORT + bucket;
                    
                    while (!isPortAvailable(adminPort) || !isPortAvailable(httpPort) 
                            || !isPortAvailable(jmsPort) || !isPortAvailable(orbPort)
                            || !isPortAvailable(httpsPort) || !isPortAvailable(orbSSLPort)
                            || !isPortAvailable(orbMAPort) || !isPortAvailable(jmxPort)
                            || !isPortAvailable(sipPort) || !isPortAvailable(sipSslPort)) {
                        int incr = (new Random()).nextInt(100)+1;
                        adminPort += incr;
                        httpPort += incr;
                        jmsPort += incr;
                        orbPort += incr;
                        httpsPort += incr;
                        orbSSLPort += incr;
                        orbMAPort += incr;
                        jmxPort += incr;
                        if (jmxPort >= 0xffff) {
                            // we ran out of ports
                            break;
                        }
                    }
                    
                    // Set all needed default properties
                    int versionId = ServerLocationManager.getAppServerPlatformVersion(serverRoot);
                    if (versionId == ServerLocationManager.GF_V1) {
                        factory.setProperty(PROP_DISPLAY_NAME,
                                NbBundle.getMessage(PluginProperties.class, "LBL_GLASSFISH_V1"));   // NOI18N
                    } else if (versionId == ServerLocationManager.GF_V2) {
                        factory.setProperty(PROP_DISPLAY_NAME, NbBundle.getMessage(PluginProperties.class, "LBL_GLASSFISH_V2"));  // NOI18N
                    } else if (versionId == ServerLocationManager.GF_V2point1) {
                        factory.setProperty(PROP_DISPLAY_NAME, NbBundle.getMessage(PluginProperties.class, "LBL_GLASSFISH_V2point1"));  // NOI18N
                    } else if (versionId == ServerLocationManager.GF_V2point1point1) {
                        factory.setProperty(PROP_DISPLAY_NAME, NbBundle.getMessage(PluginProperties.class, "LBL_GLASSFISH_V2point1point1"));  // NOI18N
                    } else {
                        factory.setProperty(PROP_DISPLAY_NAME, DEFAULT_SERVER_NAME);
                    }
                    factory.setProperty(PROP_USER_NAME, DEFAULT_USERNAME);
                    factory.setProperty(PROP_PASSWORD, DEFAULT_PASSWORD);
                    factory.setProperty(PROP_HOST, DEFAULT_HOST);
                    factory.setProperty(PROP_INSTALL_LOCATION, domainRoot.getAbsolutePath());
                    factory.setProperty(PROP_PLATFORM_LOCATION, serverRoot);
                    factory.setProperty(PROP_PORT, String.valueOf(adminPort));
                    factory.setProperty(PROP_DOMAIN, DEFAULT_DOMAIN_NAME);
                    factory.setProperty(PROP_INSTANCE_PORT, String.valueOf(httpPort));
                    factory.setProperty(PROP_JMS_PORT, String.valueOf(jmsPort));
                    factory.setProperty(PROP_ORB_LISTENER_PORT, String.valueOf(orbPort));
                    factory.setProperty(PROP_HTTP_SSL_PORT, String.valueOf(httpsPort));
                    factory.setProperty(PROP_ORB_SSL_PORT, String.valueOf(orbSSLPort));
                    factory.setProperty(PROP_ORB_MUTUAL_AUTH_PORT, String.valueOf(orbMAPort));
                    factory.setProperty(PROP_ADMIN_JMX_PORT, String.valueOf(jmxPort));
                    factory.setProperty(PROP_SIP_PORT, String.valueOf(sipPort));
                    factory.setProperty(PROP_SIP_SSL_PORT, String.valueOf(sipSslPort));
                    factory.setProperty(PROP_TYPE, AddDomainWizardIterator.PERSONAL);
                    
                    if (jmxPort < 0xffff) {
                        // Creates and register domain
                        retVal = factory.createInstance();
                    }
                } else {
                    ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL,
                            NbBundle.getMessage(DomainCreator.class, "MSG_DomainDirWriteProtect",   // NOI18N
                            domainRoot.getParentFile().getAbsolutePath()));
                }
            }
        }
        return retVal;
    }
    
    /**
     *  Sets property in WizardDescriptor
     */
    private void setProperty(String property, Object value) {
        wizardDescriptor.putProperty(property, value);
    }
    
    /**
     *  Initializes WizardDescriptor into WizardIterator and calls
     *  createInstance() method on WizardIterator which creates and register
     *  personal domain.
     */
    private InstanceProperties createInstance() {
        wizard.initialize(wizardDescriptor);
        return wizard.createInstance();
    }
    
    
    /**
     *  Checks if the port is available
     *
     *  @return true if the port is free or false when is being used
     */
    private static boolean isPortAvailable(int port) {
        String host = DEFAULT_HOST;
        boolean retVal = true;
        Socket socket = null;
        try {
            InetSocketAddress isa = new InetSocketAddress(host, port);
            socket = new Socket();
            socket.connect(isa, 1);
            retVal = false;
        } catch (IOException e) {
            // TODO avoid the exception as flow of control
            retVal = true;
            //return true;
        } finally {
            if (socket != null && !retVal) {
                try {
                    socket.close();
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                            ioe);
                }
            }            
        }
        return retVal;
    }
}
