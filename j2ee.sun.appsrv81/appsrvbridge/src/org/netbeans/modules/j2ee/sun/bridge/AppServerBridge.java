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


package org.netbeans.modules.j2ee.sun.bridge;

import com.sun.enterprise.admin.jmx.remote.DefaultConfiguration;
import com.sun.enterprise.admin.jmx.remote.SunOneHttpJmxConnectorFactory;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.deployapi.SunDeploymentManager;
import com.sun.enterprise.deployapi.SunTargetModuleID;
import com.sun.enterprise.deployment.client.ServerConnectionEnvironment;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.ModuleType;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

/*
 * wrap some App Server internal APIS calls
 * for the netbeans plugin
 * to compile this class, you need some App Server jar files like appsrv-rt.jar and appsrv-admin.jar
 * @author: Ludovic Champenois
 *
 **/


public class AppServerBridge {

    public static    java.io.File getDirLocation( TargetModuleID tmid){
        java.io.File dirLocation=null;
        String modulePart = null;
        try{
            SunTargetModuleID ddd = (SunTargetModuleID)tmid;

            String earPart = null;

            String mid = ddd.getModuleID();
            int dex = mid.indexOf('#');
            if (dex > -1) {
                earPart = mid.substring(0, dex);
                modulePart = mid.substring(dex+1);
                mid = earPart;
            }

            if (ddd.getModuleType().equals(ModuleType.WAR)) {
                if (null == earPart) {
                    ObjectName aaaa = new ObjectName("com.sun.appserv:type=web-module,name="+mid+",category=config");
                    dirLocation =new java.io.File(""+ddd.getMBeanServerConnection().getAttribute(aaaa,"location"));
                } else {
                    // watch out for a context root of "/" in a web app deployed
                    // as part of an ear.
                    if (!modulePart.startsWith("/"))
                        modulePart = "/"+modulePart;
                    ObjectName aaaa = new ObjectName("com.sun.appserv:j2eeType=WebModule,name=//server"+
                            modulePart+",J2EEApplication="+earPart+",J2EEServer=server");
                    dirLocation = new java.io.File(""+ddd.getMBeanServerConnection().getAttribute(aaaa,"docBase"));
                    modulePart = null;
                }
            } else if (mid == earPart) { // yes I want to use == for this compare
                ObjectName aaaa = new ObjectName("com.sun.appserv:type=j2ee-application,name="+mid+",category=config");
                dirLocation =new java.io.File(""+ddd.getMBeanServerConnection().getAttribute(aaaa,"location"));
            } else if (ddd.getModuleType().equals(ModuleType.EJB)){
                ObjectName aaaa = new ObjectName("com.sun.appserv:type=ejb-module,name="+mid+",category=config");
                dirLocation =new java.io.File(""+ddd.getMBeanServerConnection().getAttribute(aaaa,"location"));
            }  else if (ddd.getModuleType().equals(ModuleType.EAR)){
                ObjectName aaaa = new ObjectName("com.sun.appserv:type=j2ee-application,name="+mid+",category=config");
                dirLocation =new java.io.File(""+ddd.getMBeanServerConnection().getAttribute(aaaa,"location"));
            } else if (ddd.getModuleType().equals(ModuleType.CAR)){
                ObjectName aaaa = new ObjectName("com.sun.appserv:type=appclient-module,name="+mid+",category=config");
                dirLocation =new java.io.File(""+ddd.getMBeanServerConnection().getAttribute(aaaa,"location"));
            } else{
                if (null == earPart) {
                    ObjectName aaaa = new ObjectName("com.sun.appserv:type=extension-module,name="+mid+",category=config");
                    dirLocation =new java.io.File(""+ddd.getMBeanServerConnection().getAttribute(aaaa,"location"));
                } else {
                     if (!modulePart.startsWith("/"))
                        modulePart = "/"+modulePart;
                    ObjectName aaaa = new ObjectName("com.sun.appserv:j2eeType=WebModule,name=//server"+
                            modulePart+",J2EEApplication="+earPart+",J2EEServer=server");
                    dirLocation = new java.io.File(""+ddd.getMBeanServerConnection().getAttribute(aaaa,"docBase"));
                    modulePart = null;
                }
            }
            
        } catch(Exception e) {
            e.printStackTrace();
            IllegalStateException ise = new IllegalStateException(e.getMessage());
            ise.initCause(e);
            throw ise;
        }
        if (null!=modulePart) {
            dirLocation = new File(dirLocation.getAbsolutePath(),DirectoryDeployment.transform(modulePart));
        }
        return dirLocation;
    }

    public static boolean isApp(TargetModuleID tmid) {
        ModuleType mt = ((SunTargetModuleID)tmid).getModuleType();
        return mt.equals(ModuleType.EAR);
    }

    public static boolean isWar(TargetModuleID tmid) {
        ModuleType mt = ((SunTargetModuleID)tmid).getModuleType();
        return mt.equals(ModuleType.WAR);
    }

    public static Boolean isCar(TargetModuleID tmid) {
        ModuleType mt = ((SunTargetModuleID)tmid).getModuleType();
        return Boolean.valueOf(mt.equals(ModuleType.CAR));
    }

    /**
     * Get the URI pointing to location of child module inside a application archive.
     * For a root module, service provider does not need to override this method.
     *
     * @param module TargetModuleID of the child module
     * @return its relative path within application archive, returns null by
     * default (for standalone module)
     */
    public static String getModuleUrl(TargetModuleID module){
        ModuleType mt = ((SunTargetModuleID)module).getModuleType();
        String suffix = null;
        String moduleID = module.getModuleID();
        int i = moduleID.indexOf('#');
        if (i > -1) {
            moduleID = moduleID.substring(i+1);
        }
        if (ModuleType.EAR.equals(mt)){
            suffix = ".ear";
        }
        if (ModuleType.RAR.equals(mt)){
            suffix = ".rar";
        }
        if (ModuleType.EJB.equals(mt) || ModuleType.CAR.equals(mt)){
            suffix = ".jar";
        }
        if (null == suffix){
            suffix = ".war";
            java.io.File dirLocation = null;
            String modulePart = null;
            if (!moduleID.toLowerCase(Locale.ENGLISH).endsWith(suffix)) 
                try {
                    SunTargetModuleID ddd = (SunTargetModuleID) module;

                    String earPart = null;

                    String mid = ddd.getModuleID();
                    int dex = mid.indexOf('#');
                    if (dex > -1) {
                        earPart = mid.substring(0, dex);
                        modulePart = mid.substring(dex + 1);
                        mid = earPart;
                    }
                    if (!modulePart.startsWith("/"))
                        modulePart = "/"+modulePart;

                    if (ddd.getModuleType().equals(ModuleType.WAR)) {
                        if (null == earPart) {
                            ObjectName aaaa = new ObjectName("com.sun.appserv:type=web-module,name=" + mid + ",category=config");
                            dirLocation = new java.io.File("" + ddd.getMBeanServerConnection().getAttribute(aaaa, "location"));
                        } else {
                            ObjectName aaaa = new ObjectName("com.sun.appserv:j2eeType=WebModule,name=//server" + modulePart + ",J2EEApplication=" + earPart + ",J2EEServer=server");
                            dirLocation = new java.io.File("" + ddd.getMBeanServerConnection().getAttribute(aaaa, "docBase"));
                            String t = dirLocation.getName();
                            moduleID = t.substring(0, t.length() - 4);
                            modulePart = null;
                        }
                    } else {
                        if (null == earPart) {
                            ObjectName aaaa = new ObjectName("com.sun.appserv:type=extension-module,name=" + mid + ",category=config");
                            dirLocation = new java.io.File("" + ddd.getMBeanServerConnection().getAttribute(aaaa, "location"));
                        } else {
                            ObjectName aaaa = new ObjectName("com.sun.appserv:j2eeType=WebModule,name=//server" + modulePart + ",J2EEApplication=" + earPart + ",J2EEServer=server");
                            dirLocation = new java.io.File("" + ddd.getMBeanServerConnection().getAttribute(aaaa, "docBase"));
                            String t = dirLocation.getName();
                            moduleID = t.substring(0, t.length() - 4);
                            modulePart = null;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    IllegalStateException ise = new IllegalStateException(e.getMessage());
                    ise.initCause(e);
                    throw ise;
                }
        }

        if (moduleID.endsWith(suffix) || moduleID.endsWith(suffix.toUpperCase(Locale.ENGLISH))) {
            return moduleID;
        }
        return moduleID + suffix;
    }


    public static String getHostPort(File domainXml, File platformDir){
        String hostPort = null;
        InputStream inFile = null;
        try {
            inFile = new BufferedInputStream(new FileInputStream(domainXml));
            Domain domain = Domain.createGraph(inFile);
            String domainSysID = domain.graphManager().getXmlDocument().getDoctype().getSystemId();

            // make sure the platform will support this domain..
            
            // unknown domain.xml content.. we don't support that
            if (null == domainSysID) {
                return null;
            }

            // the sys ID doesn't contain the content we expect... we don't support that
            int dtdsDex = domainSysID.indexOf("dtds/");                         // NOI18N
            if (-1 == dtdsDex) {
                return null;
            }

            File domainDtd = new File(platformDir, "lib/"+                      // NOI18N
                    domainSysID.substring(dtdsDex));

            // the installation doesn't have the dtd where we expect it... we don't support that
            if (!domainDtd.exists()) {
                return null;
            }

            Config conf = domain.getConfigs().getConfigByName("server-config"); //NOI18N
            HttpListener list = conf.getHttpService().getHttpListenerById("admin-listener"); //NOI18N
            hostPort = "localhost:" + list.getPort(); //NOI18N
        } catch (FileNotFoundException ex) {
            Logger.getLogger("org.netbeans.modules.j2ee.sun.bridge").log(Level.FINER,"",ex); // NOI18N
        } catch (IOException ex) {
            Logger.getLogger("org.netbeans.modules.j2ee.sun.bridge").log(Level.FINER,"",ex); // NOI18N
        } catch (RuntimeException re) {
            Logger.getLogger("org.netbeans.modules.j2ee.sun.bridge").log(Level.FINER,"",re); // NOI18N
        } finally {
            if (null!=inFile)
                try {
                    inFile.close();
                } catch(IOException ioe) {
                    // what about this???
                    Logger.getLogger("org.netbeans.modules.j2ee.sun.bridge").log(Level.FINEST,"",ioe); // NOI18N
                }
            }
        return hostPort;
    }

    /* return the port number used bu the server instance (usually, it is the 8080...
     * This is not the admin port number which is usally 4848
     **/
    public String getNonAdminPortNumber(File domainXml){
        String port = null;
        try{
            InputStream inFile = new FileInputStream(domainXml);
            Domain domain = Domain.createGraph(inFile);
            Config conf = domain.getConfigs().getConfigByName("server-config"); //NOI18N
            HttpListener list = conf.getHttpService().getHttpListenerById("http-listener-1"); //NOI18N
            port =  list.getPort(); //NOI18N
            inFile.close();
        }catch(Exception ex){
            return null;
            //Suppressing exception while trying to get admin port.
            //Null port value is handled in AddServerChoiceVisualPanel
        }
        return port;
    }
    public     static MBeanServerConnection getJMXConnector(String host, int port, String username, String password,boolean secure) throws java.net.MalformedURLException, java.io.IOException{
        MBeanServerConnection serverConn = null;
        String mode=null;
        if (secure)
            mode="s1ashttps";
        else
            mode = "s1ashttp";
        JMXServiceURL serverUrl = new JMXServiceURL(mode, host, port); //NOI18N
        final JMXConnector connector = SunOneHttpJmxConnectorFactory.connect(serverUrl, username, password);
        serverConn = connector.getMBeanServerConnection();

        return serverConn;
    }
    /**
     *	This method initializes a newly DeploymentManager by creating and
     *	setting a ServerConnectionEnvironement with Deploytool's
     *	X509TrustManager.  This method will case the given DeploymentManager
     *	to a SunDeploymentManager in order to make
     *	invoke the appropriate setter for the ServerConnectionEnvironment.
     */
    public static void setServerConnectionEnvironment(DeploymentManager dm) {
        ServerConnectionEnvironment env = new ServerConnectionEnvironment();
        env.put(DefaultConfiguration.TRUST_MANAGER_PROPERTY_NAME, new X509TrustManager());
        if (dm instanceof SunDeploymentManager) {
            ((SunDeploymentManager)dm).setServerConnectionEnvironment(env);
        }// else {
        //  Print.dprintStackTrace(null, new IllegalArgumentException(
        //		"Unsupported DeploymentManager type: '"+
        //		dm.getClass().getName()+"'."));
        //	}
    }
    
    
}
