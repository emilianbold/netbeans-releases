/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.tomcat5;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.shared.factories.DeploymentFactoryManager;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.tomcat5.TomcatManager.TomcatVersion;
import org.openide.util.NbBundle;

/** 
 * Factory capable to create DeploymentManager that can deploy to Tomcat 5 and 6.
 *
 * Tomcat URI has following format:
 * <PRE><CODE>tomcat[55|60]:[home=&lt;home_path&gt;:[base=&lt;base_path&gt;:]]&lt;manager_app_url&gt;</CODE></PRE>
 * for example
 * <PRE><CODE>tomcat:http://localhost:8080/manager/</CODE></PRE>
 * where paths values will be used as CATALINA_HOME and CATALINA_BASE properties and manager_app_url
 * denotes URL of manager application configured on this server and has to start with <CODE>http:</CODE>.
 * @author Radim Kubacki
 */
public final class TomcatFactory implements DeploymentFactory {
    
    public static final String SERVER_ID_50 = "Tomcat";     // NOI18N
    public static final String SERVER_ID_55 = "Tomcat55";   // NOI18N
    public static final String SERVER_ID_60 = "Tomcat60";   // NOI18N
    
    public static final String TOMCAT_URI_PREFIX_50 = "tomcat:";    // NOI18N
    public static final String TOMCAT_URI_PREFIX_55 = "tomcat55:";  // NOI18N
    public static final String TOMCAT_URI_PREFIX_60 = "tomcat60:";  // NOI18N
    
    public static final String TOMCAT_URI_HOME_PREFIX = "home=";    // NOI18N
    public static final String TOMCAT_URI_BASE_PREFIX = ":base=";   // NOI18N

    private static final String DISCONNECTED_URI_50 = "tomcat:jakarta-tomcat-5.0.x";    // NOI18N
    private static final String DISCONNECTED_URI_55 = "tomcat55:jakarta-tomcat-5.5.x";  // NOI18N
    private static final String DISCONNECTED_URI_60 = "tomcat60:apache-tomcat-6.0.x";   // NOI18N
    
    private static TomcatFactory instance;
    private static TomcatFactory instance55;
    private static TomcatFactory instance60;
    
    private static final WeakHashMap managerCache = new WeakHashMap();
    
    private static Logger err = Logger.getLogger("org.netbeans.modules.tomcat5");  // NOI18N
    
    private final String tomcatUriPrefix;
    private final String disconnectedUri;
    private final TomcatVersion version;
            
    private TomcatFactory(TomcatVersion version) {
        this.version = version;
        switch (version) {
            case TOMCAT_50 :
                tomcatUriPrefix = TOMCAT_URI_PREFIX_50;
                disconnectedUri = DISCONNECTED_URI_50;
                break;
            case TOMCAT_55 :
                tomcatUriPrefix = TOMCAT_URI_PREFIX_55;
                disconnectedUri = DISCONNECTED_URI_55;
                break;
            case TOMCAT_60 :
            default:
                tomcatUriPrefix = TOMCAT_URI_PREFIX_60;
                disconnectedUri = DISCONNECTED_URI_60;
                break;
        }
    }
    
    /** 
     * Factory method to create DeploymentFactory for Tomcat 5.0.x
     */
    public static synchronized TomcatFactory create50() {
        if (instance == null) {
            if (err.isLoggable(Level.FINE)) err.log(Level.FINE, "Creating TomcatFactory"); // NOI18N
            instance = new TomcatFactory(TomcatVersion.TOMCAT_50);
            DeploymentFactoryManager.getInstance().registerDeploymentFactory(instance);
        }
        return instance;
    }
    
    /** 
     * Factory method to create DeploymentFactory for Tomcat 5.5.x
     */
    public static synchronized TomcatFactory create55() {
        if (instance55 == null) {
            if (err.isLoggable(Level.FINE)) err.log(Level.FINE, "Creating TomcatFactory"); // NOI18N
            instance55 = new TomcatFactory(TomcatVersion.TOMCAT_55);
            DeploymentFactoryManager.getInstance().registerDeploymentFactory(instance55);
        }
        return instance55;
    }
    
    /** 
     * Factory method to create DeploymentFactory for Tomcat 6.0.x
     */
    public static synchronized TomcatFactory create60() {
        if (instance60 == null) {
            if (err.isLoggable(Level.FINE)) err.log(Level.FINE, "Creating TomcatFactory"); // NOI18N
            instance60 = new TomcatFactory(TomcatVersion.TOMCAT_60);
            DeploymentFactoryManager.getInstance().registerDeploymentFactory(instance60);
        }
        return instance60;
    }
    
    /** Factory method to create DeploymentManager.
     * @param uri URL of configured manager application.
     * @param uname user with granted manager role
     * @param passwd user's password
     * @throws DeploymentManagerCreationException
     * @return {@link TomcatManager}
     */
    public synchronized DeploymentManager getDeploymentManager(String uri, String uname, String passwd) 
    throws DeploymentManagerCreationException {
        if (!handlesURI (uri)) {
            throw new DeploymentManagerCreationException ("Invalid URI:" + uri); // NOI18N
        }
        // Lets reuse the same instance of TomcatManager for each server instance
        // during the IDE session, j2eeserver does not ensure this. Without it,
        // however, we could not rely on keeping data in the member variables.
        InstanceProperties ip = InstanceProperties.getInstanceProperties(uri);
        if (ip == null) {
            // null ip either means that the instance is not registered, or that this is the disconnected URL
            if (!disconnectedUri.equals(uri)) {
                throw new DeploymentManagerCreationException("Tomcat instance: " + uri + " is not registered in the IDE."); // NOI18N
            }
        }
        TomcatManager tm = (TomcatManager)managerCache.get(ip);
        if (tm == null) {
            try {
                tm = new TomcatManager(true, uri.substring(tomcatUriPrefix.length()), version);
                managerCache.put(ip, tm);
            } catch (IllegalArgumentException iae) {
                Throwable t = new DeploymentManagerCreationException("Cannot create deployment manager for Tomcat instance: " + uri + "."); // NOI18N
                throw (DeploymentManagerCreationException)(t.initCause(iae));
            }
        }
        return tm;
    }
    
    public DeploymentManager getDisconnectedDeploymentManager(String uri) 
    throws DeploymentManagerCreationException {
        // no need to distinguish beetween the connected and disconnected DM for Tomcat
        return getDeploymentManager(uri, null, null);
    }
    
    public String getDisplayName() {
        switch (version) {
            case TOMCAT_50 :
                return NbBundle.getMessage(TomcatFactory.class, "LBL_TomcatFactory");
            case TOMCAT_55 :
                return NbBundle.getMessage(TomcatFactory.class, "LBL_TomcatFactory55");
            case TOMCAT_60 :
            default:
                return NbBundle.getMessage(TomcatFactory.class, "LBL_TomcatFactory60");
        }
    }
    
    public String getProductVersion() {
        return NbBundle.getMessage(TomcatFactory.class, "LBL_TomcatFactoryVersion");
    }
    
    /**
     * @param str
     * @return <CODE>true</CODE> for URIs beggining with <CODE>tomcat[55|60]:</CODE> prefix
     */    
    public boolean handlesURI(String str) {
        return str != null && str.startsWith (tomcatUriPrefix);
    }
    
    /** 
     * Retrieve the tomcat version e.g. '6.0.10'
     * 
     * @throws IllegalStateException if the version information cannot be retrieved 
     */
    public static String getTomcatVersion(File catalinaHome) throws IllegalStateException {
        File catalinaJar = new File(catalinaHome, "lib/catalina.jar"); // NOI18N
        if (!catalinaJar.exists()) {
            catalinaJar = new File(catalinaHome, "server/lib/catalina.jar"); // NOI18N
        }
        try {
            URLClassLoader loader = new URLClassLoader(new URL[] { catalinaJar.toURL() });
            Class serverInfo = loader.loadClass("org.apache.catalina.util.ServerInfo"); // NOI18N
            Method method = serverInfo.getMethod("getServerInfo", new Class[] {}); // NOI18N
            String version = (String) method.invoke(serverInfo, new Object[] {});
            int idx = version.indexOf('/');
            if (idx > 0) {
                return version.substring(idx + 1);
            }
            throw new IllegalStateException("Cannot identify the version of the server."); // NOI18N
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }   
}
