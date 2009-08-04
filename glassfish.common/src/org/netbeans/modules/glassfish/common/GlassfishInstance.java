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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.glassfish.common.nodes.Hk2InstanceNode;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.glassfish.common.ui.InstanceCustomizer;
import org.netbeans.modules.glassfish.common.ui.VmCustomizer;
import org.netbeans.modules.glassfish.spi.CustomizerCookie;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.GlassfishModuleFactory;
import org.netbeans.modules.glassfish.spi.GlassfishModule.OperationState;
import org.netbeans.modules.glassfish.spi.GlassfishModule.ServerState;
import org.netbeans.modules.glassfish.spi.RemoveCookie;
import org.netbeans.spi.server.ServerInstanceFactory;
import org.netbeans.spi.server.ServerInstanceImplementation;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.windows.InputOutput;

/**
 *
 * @author Peter Williams
 */
public class GlassfishInstance implements ServerInstanceImplementation, LookupListener {

    // !PW FIXME Can we extract the server name from the install?  That way,
    // perhaps we can distinguish between GF V3 and Sun AS 10.0
    private static final String GLASSFISH_PRELUDE_SERVER_NAME = "GlassFish v3 Prelude"; // NOI18N
    private static final String GLASSFISH_SERVER_NAME = "GlassFish v3"; // NOI18N

    // Reasonable default values for various server parameters.  Note, don't use
    // these unless the server's actual setting cannot be determined in any way.
    public static final String DEFAULT_HOST_NAME = "localhost"; // NOI18N
    public static final String DEFAULT_ADMIN_NAME = "admin"; // NOI18N
    public static final String DEFAULT_ADMIN_PASSWORD = "adminadmin"; // NOI18N
    public static final int DEFAULT_HTTP_PORT = 8080;
    public static final int DEFAULT_HTTPS_PORT = 8181;
    public static final int DEFAULT_ADMIN_PORT = 4848;
    public static final String DEFAULT_DOMAINS_FOLDER = "domains"; //NOI18N
    public static final String DEFAULT_DOMAIN_NAME = "domain1"; // NOI18N

    
    // Server properties
    private boolean removable = true;
    
    // Implementation details
    private transient CommonServerSupport commonSupport;
    private transient InstanceContent ic;
    private transient Lookup lookup;
    private transient Lookup.Result<GlassfishModuleFactory> lookupResult;
    private transient Collection<? extends GlassfishModuleFactory> currentFactories;
    
    // api instance
    private ServerInstance commonInstance;
    private GlassfishInstanceProvider instanceProvider;
    
    private GlassfishInstance(Map<String, String> ip, GlassfishInstanceProvider instanceProvider) {
        String deployerUri = null;
        try {
            ic = new InstanceContent();
            lookup = new AbstractLookup(ic);
            this.instanceProvider = instanceProvider;
            commonSupport = new CommonServerSupport(lookup, ip, instanceProvider);
            ic.add(this); // Server instance in lookup (to find instance from node lookup)

            ic.add(commonSupport); // Common action support, e.g start/stop, etc.

            // Flag this server URI as under construction
            deployerUri = commonSupport.getDeployerUri();
            GlassfishInstanceProvider.activeRegistrationSet.add(deployerUri);

            commonInstance = ServerInstanceFactory.createServerInstance(this);
            updateModuleSupport();
            
            // make this instance publicly accessible
            instanceProvider.addServerInstance(this);
        } finally {
            if(deployerUri != null) {
                GlassfishInstanceProvider.activeRegistrationSet.remove(deployerUri);
            }
        }
    }

    private void updateFactories() {
        // !PW FIXME should read asenv.bat on windows.
        Properties asenvProps = new Properties();
        String homeFolder = commonSupport.getGlassfishRoot();
        File asenvConf = new File(homeFolder, "config/asenv.conf"); // NOI18N
        if(asenvConf.exists()) {
            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(asenvConf));
                asenvProps.load(is);
            } catch(FileNotFoundException ex) {
                Logger.getLogger("glassfish").log(Level.WARNING, null, ex); // NOI18N
            } catch(IOException ex) {
                Logger.getLogger("glassfish").log(Level.WARNING, null, ex); // NOI18N
                asenvProps.clear();
            } finally {
                if(is != null) {
                    try { is.close(); } catch (IOException ex) { }
                }
            }
        } else {
            Logger.getLogger("glassfish").log(Level.WARNING, asenvConf.getAbsolutePath() + " does not exist"); // NOI18N
        }
        Set<GlassfishModuleFactory> added = new HashSet<GlassfishModuleFactory>();
        //Set<GlassfishModuleFactory> removed = new HashSet<GlassfishModuleFactory>();
        added.addAll(lookupResult.allInstances());
        added.removeAll(currentFactories);
        currentFactories = lookupResult.allInstances();
        for (GlassfishModuleFactory moduleFactory : added) {
            if(moduleFactory.isModuleSupported(homeFolder, asenvProps)) {
                Object t = moduleFactory.createModule(lookup);
                if (null == t) {
                    Logger.getLogger("glassfish").log(Level.WARNING, moduleFactory+" created a null module"); // NOI18N
                } else {
                    ic.add(t);
                }
            }
        }
    }
    
    private void updateModuleSupport() {
        // Find all modules that have NetBeans support, add them to lookup if server
        // supports them.
        currentFactories = Collections.emptyList();
        lookupResult = Lookups.forPath(Util.GF_LOOKUP_PATH).lookupResult(GlassfishModuleFactory.class);
        updateFactories();
        lookupResult.addLookupListener(this);
    }

    public void resultChanged(LookupEvent ev) {
        updateFactories();
    }

    /** 
     * Creates a GlassfishInstance object for a server installation.  This
     * instance should be added to the the provider registry if the caller wants
     * it to be persisted for future sessions or searchable.
     * 
     * @param displayName display name for this server instance.
     * @param homeFolder install folder where server code is located.
     * @param httpPort http port for this server instance.
     * @param adminPort admin port for this server instance.
     * @return GlassfishInstance object for this server instance.
     */
    public static GlassfishInstance create(String displayName, String installRoot, 
            String glassfishRoot, String domainsDir, String domainName, int httpPort, 
            int adminPort,String url, String uriFragment, GlassfishInstanceProvider gip) {
        Map<String, String> ip = new HashMap<String, String>();
        ip.put(GlassfishModule.DISPLAY_NAME_ATTR, displayName);
        ip.put(GlassfishModule.INSTALL_FOLDER_ATTR, installRoot);
        ip.put(GlassfishModule.GLASSFISH_FOLDER_ATTR, glassfishRoot);
        ip.put(GlassfishModule.DOMAINS_FOLDER_ATTR, domainsDir);
        ip.put(GlassfishModule.DOMAIN_NAME_ATTR, domainName);
        ip.put(GlassfishModule.HTTPPORT_ATTR, Integer.toString(httpPort));
        ip.put(GlassfishModule.ADMINPORT_ATTR, Integer.toString(adminPort));
        ip.put(GlassfishModule.URL_ATTR, url);
        // extract the host from the URL
        String[] bigUrlParts = url.split("]");
        if (null != bigUrlParts && bigUrlParts.length > 1) {
            String[] urlParts = bigUrlParts[1].split(":"); // NOI18N
            if (null != urlParts && urlParts.length > 2) {
                ip.put(GlassfishModule.HOSTNAME_ATTR, urlParts[2]);
            }
        }
        GlassfishInstance result = new GlassfishInstance(ip, gip);
        return result;
    }
    
    public static GlassfishInstance create(Map<String, String> ip,GlassfishInstanceProvider gip) {
        GlassfishInstance result = new GlassfishInstance(ip, gip);
        return result;
    }
    
    public ServerInstance getCommonInstance() {
        return commonInstance;
    }
        
    public CommonServerSupport getCommonSupport() {
        return commonSupport;
    }
    
    public String getDeployerUri() {
        return commonSupport.getDeployerUri();
    }
    
    public String getInstallRoot() {
        return commonSupport.getInstallRoot();
    }
    
    public String getGlassfishRoot() {
        return commonSupport.getGlassfishRoot();
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    public void addChangeListener(final ChangeListener listener) {
        commonSupport.addChangeListener(listener);
    }

    public void removeChangeListener(final ChangeListener listener) {
        commonSupport.removeChangeListener(listener);
    }
    
    public ServerState getServerState() {
        return commonSupport.getServerState();
    }

    void stopIfStartedByIde(long timeout) {
        if(commonSupport.isStartedByIde()) {
            ServerState state = commonSupport.getServerState();
            if(state == ServerState.STARTING ||
                    (state == ServerState.RUNNING && commonSupport.isReallyRunning())) {
                try {
                    Future<OperationState> stopServerTask = commonSupport.stopServer(null);
                    if(timeout > 0) {
                        OperationState opState = stopServerTask.get(timeout, TimeUnit.MILLISECONDS);
                        if(opState != OperationState.COMPLETED) {
                            Logger.getLogger("glassfish").info("Stop server failed..."); // NOI18N
                        }
                    }
                } catch(TimeoutException ex) {
                    Logger.getLogger("glassfish").fine("Server " + getDeployerUri() + " timed out sending stop-domain command."); // NOI18N
                } catch(Exception ex) {
                    Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex); // NOI18N
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // ServerInstance interface implementation
    // ------------------------------------------------------------------------
    public String getDisplayName() {
        return commonSupport.getDisplayName();
    }

    // TODO -- this should be done differently
    public String getServerDisplayName() {
        File f = new File(commonSupport.getGlassfishRoot(), "lib"+File.separator+"schemas"+File.separator+"web-app_3_0.xsd"); // NOI18N
        if (f.exists()) {
            return GLASSFISH_SERVER_NAME;
        } else {
            return GLASSFISH_PRELUDE_SERVER_NAME;
        }
    }

    public Node getFullNode() {
        Logger.getLogger("glassfish").finer("Creating GF Instance node [FULL]"); // NOI18N
        return new Hk2InstanceNode(this, true);
    }

    public Node getBasicNode() {
        Logger.getLogger("glassfish").finer("Creating GF Instance node [BASIC]"); // NOI18N
        return new Hk2InstanceNode(this, false);
    }
    
    public JComponent getCustomizer() {
        JPanel commonCustomizer = new InstanceCustomizer(commonSupport);
        JPanel vmCustomizer = new VmCustomizer(commonSupport);

        Collection<JPanel> pages = new LinkedList<JPanel>();
        Collection<? extends CustomizerCookie> lookupAll = lookup.lookupAll(CustomizerCookie.class);
        for(CustomizerCookie cookie : lookupAll) {
            pages.addAll(cookie.getCustomizerPages());
        }
        pages.add(vmCustomizer);

        JTabbedPane tabbedPane = null;
        for(JPanel page : pages) {
            if(tabbedPane == null) {
                tabbedPane = new JTabbedPane();
                tabbedPane.add(commonCustomizer);
            }
            
            tabbedPane.add(page);
        }
        
        return tabbedPane != null ? tabbedPane : commonCustomizer;
    }

    public boolean isRemovable() {
        return removable;
    }

    public void remove() {
        // Just in case...
        if(!removable) {
            return;
        }
        
        // !PW FIXME Remove debugger hooks, if any
//        DebuggerManager.getDebuggerManager().removeDebuggerListener(debuggerStateListener);

        stopIfStartedByIde(3000L);
        
        // close the server io window
        String uri = commonSupport.getDeployerUri();
        InputOutput io = LogViewMgr.getServerIO(uri);
        if(io != null && !io.isClosed()) {
            io.closeInputOutput();
        }

        Collection<? extends RemoveCookie> lookupAll = lookup.lookupAll(RemoveCookie.class);
        for(RemoveCookie cookie: lookupAll) {
            cookie.removeInstance(getDeployerUri());
        }

        instanceProvider.removeServerInstance(this);
    }

    //
    // watch out for the localhost alias.
    //
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GlassfishInstance)) {
            return false;
        }
        GlassfishInstance other = (GlassfishInstance) obj;
        return getDeployerUri().replace("127.0.0.1", "localhost").equals(other.getDeployerUri().replace("127.0.0.1", "localhost")) &&
                commonSupport.getDomainName().equals(other.getCommonSupport().getDomainName()) &&
                commonSupport.getDomainsRoot().equals(other.getCommonSupport().getDomainsRoot()) &&
                commonSupport.getHttpPort().equals(other.getCommonSupport().getHttpPort());
    }

    @Override
    public int hashCode() {
        String tmp = getDeployerUri().replace("127.0.0.1", "localhost")+commonSupport.getHttpPort()+
                commonSupport.getDomainsRoot()+commonSupport.getDomainName();
        return tmp.hashCode();
    }

}
