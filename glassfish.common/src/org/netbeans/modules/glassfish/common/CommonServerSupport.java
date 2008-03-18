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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.common;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.glassfish.AppDesc;
import org.netbeans.spi.glassfish.GlassfishModule;
import org.netbeans.spi.glassfish.GlassfishModule.OperationState;
import org.netbeans.spi.glassfish.GlassfishModule.ServerState;
import org.netbeans.spi.glassfish.OperationStateListener;
import org.openide.util.ChangeSupport;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Peter Williams
 */
public class CommonServerSupport implements GlassfishModule {

    public static final String URI_PREFIX = "deployer:gfv3";
    
    private Map<String, String> properties = 
            Collections.synchronizedMap(new HashMap<String, String>(37));
    
    private volatile ServerState serverState;
    private final Object stateMonitor = new Object();
    
    private ChangeSupport changeSupport = new ChangeSupport(this);
    
    CommonServerSupport(final String displayName, final String homeFolder, int httpPort, int adminPort) {
        final String hostName = GlassfishInstance.DEFAULT_HOST_NAME;
        if(adminPort < 0) {
            adminPort = GlassfishInstance.DEFAULT_ADMIN_PORT;
        }
        if(httpPort < 0) {
            httpPort = GlassfishInstance.DEFAULT_HTTP_PORT;
        }
        
        properties.put(DISPLAY_NAME_ATTR, displayName);
        properties.put(HOME_FOLDER_ATTR, homeFolder);
        
        String deployerUrl = "[" + homeFolder + "]" + URI_PREFIX + ":" + 
                hostName + ":" + httpPort;
        properties.put(URL_ATTR, deployerUrl);
        
        properties.put(USERNAME_ATTR, GlassfishInstance.DEFAULT_ADMIN_NAME);
        properties.put(PASSWORD_ATTR, GlassfishInstance.DEFAULT_ADMIN_PASSWORD);
        properties.put(ADMINPORT_ATTR, Integer.toString(adminPort));
        properties.put(HTTPPORT_ATTR, Integer.toString(httpPort));
        properties.put(HOSTNAME_ATTR, hostName);
        
        boolean isRunning = isRunning(hostName, httpPort);
        setServerState(isRunning ? ServerState.RUNNING : ServerState.STOPPED);
    }
    
    public String getHomeFolder() {
        return properties.get(HOME_FOLDER_ATTR);
    }
    
    public String getDisplayName() {
        return properties.get(DISPLAY_NAME_ATTR);
    }
    
    public String getDeployerUri() {
        return properties.get(URL_ATTR);
    }
    
    public String getUserName() {
        return properties.get(USERNAME_ATTR);
    }
    
    public String getPassword() {
        return properties.get(PASSWORD_ATTR);
    }
    
    public String getAdminPort() {
        return properties.get(ADMINPORT_ATTR);
    }
    
    public String getHttpPort() {
        return properties.get(HTTPPORT_ATTR);
    }
    
    public String getHostName() {
        return properties.get(HOSTNAME_ATTR);
    }
    
    public void setServerState(final ServerState newState) {
        // Synchronized on private monitor to serialize changes in state.
        // Storage of serverState is volatile to facilitate readability of
        // current state regardless of lock status.
        boolean fireChange = false;
        
        synchronized (stateMonitor) {
            if(serverState != newState) {
                serverState = newState;
                fireChange = true;
            }
        }
        
        if(fireChange) {
            changeSupport.fireChange();
        }
    }
    
    // ------------------------------------------------------------------------
    // GlassfishModule interface implementation
    // ------------------------------------------------------------------------
    public Map<String, String> getInstanceProperties() {
        return Collections.unmodifiableMap(properties);
    }
    
    public Future<OperationState> startServer(final OperationStateListener stateListener) {
        Logger.getLogger("glassfish").log(Level.FINEST, 
                "CSS.startServer called on thread \"" + Thread.currentThread().getName() + "\"");
        OperationStateListener startServerListener = new OperationStateListener() {
            public void operationStateChanged(OperationState newState, String message) {
                if(newState == OperationState.RUNNING) {
                    setServerState(ServerState.STARTING);
                } else if(newState == OperationState.COMPLETED) {
                    setServerState(ServerState.RUNNING);
                } else if(newState == OperationState.FAILED) {
                    setServerState(ServerState.STOPPED);
                }
            }
        };
        FutureTask<OperationState> task = new FutureTask<OperationState>(
                new StartTask(properties, null, startServerListener, stateListener));
        RequestProcessor.getDefault().post(task);
        return task;
    }

    public Future<OperationState> stopServer(final OperationStateListener stateListener) {
        Logger.getLogger("glassfish").log(Level.FINEST, 
                "CSS.stopServer called on thread \"" + Thread.currentThread().getName() + "\"");
        OperationStateListener stopServerListener = new OperationStateListener() {
            public void operationStateChanged(OperationState newState, String message) {
                if(newState == OperationState.RUNNING) {
                    setServerState(ServerState.STOPPING);
                } else if(newState == OperationState.COMPLETED) {
                    setServerState(ServerState.STOPPED);
                } else if(newState == OperationState.FAILED) {
                    setServerState(ServerState.RUNNING);
                }
            }
        };
        FutureTask<OperationState> task = new FutureTask<OperationState>(
                new StopTask(properties, stopServerListener, stateListener));
        RequestProcessor.getDefault().post(task);
        return task;
    }
    
    public Future<OperationState> deploy(final OperationStateListener stateListener, 
            final File application, final String name) {
        return deploy(stateListener, application, name, null);
    }

    public Future<OperationState> deploy(final OperationStateListener stateListener, 
            final File application, final String name, final String contextRoot) {
        CommandRunner mgr = new CommandRunner(getInstanceProperties(), stateListener);
        return mgr.deploy(application, name, contextRoot);
    }
    
    public Future<OperationState> undeploy(final OperationStateListener stateListener, final String name) {
        CommandRunner mgr = new CommandRunner(getInstanceProperties(), stateListener);
        return mgr.undeploy(name);
    }
    
    public AppDesc [] getModuleList() {
        CommandRunner mgr = new CommandRunner(getInstanceProperties());
        int total = 0;
        Map<String, List<AppDesc>> appMap = mgr.getApplications();
        Collection<List<AppDesc>> appLists = appMap.values();
        for(List<AppDesc> appList: appLists) {
            total += appList.size();
        }
        AppDesc [] result = new AppDesc[total];
        int index = 0;
        for(List<AppDesc> appList: appLists) {
            for(AppDesc app: appList) {
                result[index++] = app;
            }
        }
        return result;
    }

    public ServerState getServerState() {
        return serverState;
    }
    
    public void addChangeListener(final ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(final ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }
    
    public String setEnvironmentProperty(final String name, final String value) {
        String result = null;

        synchronized (properties) {
            result = properties.get(name);
            if(result == null) {
                properties.put(name, value);
                result = value;
            }
        }
        
        return result;
    }

    // ------------------------------------------------------------------------
    // bookkeeping & impl managment, not exposed via interface.
    // ------------------------------------------------------------------------
    void setProperty(final String key, final String value) {
        properties.put(key, value);
    }
    
    void getProperty(String key) {
        properties.get(key);
    }

    public static boolean isRunning(final String host, final int port) {
        if(null == host)
            return false;
        
        try {
            InetSocketAddress isa = new InetSocketAddress(host, port);
            Socket socket = new Socket();
            socket.connect(isa, 1);
            socket.close();
            return true;
        } catch(IOException ex) {
            return false;
        }
    }

    /**
     * !PW XXX Is there a more efficient way to implement a failed future object? 
     * 
     * @return Future object that represents an immediate failed operation
     */
    private static Future<OperationState> failedOperation() {
        return new Future<OperationState>() {

            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            public boolean isCancelled() {
                return false;
            }

            public boolean isDone() {
                return true;
            }

            public OperationState get() throws InterruptedException, ExecutionException {
                return OperationState.FAILED;
            }

            public OperationState get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return OperationState.FAILED;
            }
        };
    }
    
    /**
     * !PW XXX Is there a more efficient way to implement a failed future object? 
     * 
     * @return Future object that represents an immediate failed operation
     */
    private static Future<OperationState> successfulOperation() {
        return new Future<OperationState>() {

            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            public boolean isCancelled() {
                return false;
            }

            public boolean isDone() {
                return true;
            }

            public OperationState get() throws InterruptedException, ExecutionException {
                return OperationState.COMPLETED;
            }

            public OperationState get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return OperationState.COMPLETED;
            }
        };
    }
    
}
