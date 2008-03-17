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
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.glassfish.common.nodes.Hk2InstanceNode;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.spi.glassfish.GlassfishModuleFactory;
import org.netbeans.spi.glassfish.GlassfishModule.OperationState;
import org.netbeans.spi.glassfish.GlassfishModule.ServerState;
import org.netbeans.spi.glassfish.RemoveCookie;
import org.netbeans.spi.server.ServerInstanceFactory;
import org.netbeans.spi.server.ServerInstanceImplementation;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.windows.InputOutput;

/**
 *
 * @author Peter Williams
 */
public class GlassfishInstance implements ServerInstanceImplementation {

    // !PW FIXME Can we extrace the server name from the install?  That way,
    // perhaps we can distinguish between GF V3 and Sun AS 10.0
    public static final String GLASSFISH_SERVER_NAME = "GlassFish V3 New Preview";

    // Reasonable default values for various server parameters.  Note, don't use
    // these unless the server's actual setting cannot be determined in any way.
    public static final String DEFAULT_HOST_NAME = "localhost"; // NOI18N
    public static final String DEFAULT_ADMIN_NAME = "admin"; // NOI18N
    public static final String DEFAULT_ADMIN_PASSWORD = "adminadmin"; // NOI18N
    public static final int DEFAULT_HTTP_PORT = 8080;
    public static final int DEFAULT_HTTPS_PORT = 8181;
    public static final int DEFAULT_ADMIN_PORT = 4848;

    
    // Server properties
    private boolean removable = true;
    
    // Implementation details
    private transient CommonServerSupport commonSupport;
    private transient InstanceContent ic;
    private transient Lookup lookup;
    
    // api instance
    private ServerInstance commonInstance;
    

    GlassfishInstance(String displayName, String homeFolder, int httpPort, int adminPort) {
        commonSupport = new CommonServerSupport(displayName, homeFolder, httpPort, adminPort);

        ic = new InstanceContent();
        lookup = new AbstractLookup(ic);
        ic.add(this); // Server instance in lookup (to find instance from node lookup)
        ic.add(commonSupport); // Common action support, e.g start/stop, etc.

        updateModuleSupport();
    }
    
    private void updateModuleSupport() {
        // !PW FIXME this is unstable
        Properties asenvProps = null;
        String homeFolder = commonSupport.getHomeFolder();
        File asenvConf = new File(homeFolder, "config/asenv.conf");
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(asenvConf));
            asenvProps = new Properties();
            asenvProps.load(is);
        } catch(FileNotFoundException ex) {
            Logger.getLogger("glassfish").log(Level.WARNING, null, ex);
            asenvProps = null;
        } catch(IOException ex) {
            Logger.getLogger("glassfish").log(Level.WARNING, null, ex);
            asenvProps = null;
        } finally {
            if(is != null) {
                try { is.close(); } catch (IOException ex) { }
            }
        }
        
        // No asenv properties, or unreadable ==> no module specific server
        // support for now.
        if(asenvProps == null) {
            return;
        }
        
        // Find all modules that have NetBeans support, add them to lookup if server
        // supports them.
        for (GlassfishModuleFactory moduleFactory : 
                Lookups.forPath("Servers/GlassFish").lookupAll(GlassfishModuleFactory.class)) {
            if(moduleFactory.isModuleSupported(homeFolder, asenvProps)) {
                ic.add(moduleFactory.createModule(lookup));
            }
        }
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
    public static GlassfishInstance create(String displayName, String homeFolder, int httpPort, int adminPort) {
        GlassfishInstance result = new GlassfishInstance(displayName, homeFolder, httpPort, adminPort);
        result.commonInstance = ServerInstanceFactory.createServerInstance(result);
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
    
    public String getHomeFolder() {
        return commonSupport.getHomeFolder();
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
    
    // ------------------------------------------------------------------------
    // ServerInstance interface implementation
    // ------------------------------------------------------------------------
    public String getDisplayName() {
        return commonSupport.getDisplayName();
    }

    public String getServerDisplayName() {
        return GLASSFISH_SERVER_NAME;
    }

    public Node getFullNode() {
        Logger.getLogger("glassfish").log(Level.INFO, "Creating GF Instance node [FULL]");
        return new Hk2InstanceNode(this, true);
    }

    public Node getBasicNode() {
        Logger.getLogger("glassfish").log(Level.INFO, "Creating GF Instance node [BASIC]");
        return new Hk2InstanceNode(this, false);
    }
    
    public JComponent getCustomizer() {
        // !PW FIXME use real customizer
        javax.swing.JPanel customizer = new javax.swing.JPanel();
        customizer.add(new javax.swing.JLabel("TODO V3 Server Customizer"));
        return customizer;
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

        // !PW FIXME Stop server, if running & started by IDE
//        stopIfStartedByIde();
        
        try {
            Future<OperationState> stopServerTask = commonSupport.stopServer(null);
            OperationState opState = stopServerTask.get(3000, TimeUnit.MILLISECONDS);
            if(opState != OperationState.COMPLETED) {
                Logger.getLogger("glassfish").info("Stop server failed...");
            }
        } catch(TimeoutException ex) {
            Logger.getLogger("glassfish").fine("Server " + getDeployerUri() + " timed out sending stop-domain command.");
        } catch(Exception ex) {
            Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex);
        }
        
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

        GlassfishInstanceProvider.getDefault().removeServerInstance(this);
    }

}
