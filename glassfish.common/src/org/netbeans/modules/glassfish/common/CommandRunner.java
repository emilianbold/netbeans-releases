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

package org.netbeans.modules.glassfish.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.glassfish.AppDesc;
import org.netbeans.spi.glassfish.GlassfishModule;
import org.netbeans.spi.glassfish.GlassfishModule.OperationState;
import org.netbeans.spi.glassfish.OperationStateListener;
import org.netbeans.spi.glassfish.ResourceDesc;
import org.netbeans.spi.glassfish.ServerCommand;


/** 
 * Implementation of management task that provides info about progress
 *
 * @author Peter Williams
 */
public class CommandRunner extends BasicTask<OperationState> {
    
    public final int HTTP_RETRY_DELAY = 3000;
    
    /** Executor that serializes management tasks. 
     */
    private static ExecutorService executor;
    
    /** Returns shared executor.
     */
    private static synchronized ExecutorService executor() {
        if(executor == null) {
            executor = Executors.newFixedThreadPool(1);
        }
        return executor;
    }
    
    /** Command type used for events. */
    private ServerCommand serverCmd;
    
    /** Has been the last access to  manager web app authorized? */
    private boolean authorized;
    
    
    public CommandRunner(Map<String, String> properties, OperationStateListener... stateListener) {
        super(properties, stateListener);
    }
    
    /**
     * Sends stop-domain command to server (asynchronous)
     * 
     */
    public Future<OperationState> stopServer() {
        return execute(Commands.STOP, "MSG_STOP_SERVER_IN_PROGRESS");
        
    }
    
    /**
     * Sends list-applications command to server (synchronous)
     * 
     * @return String array of names of deployed applications.
     */
    public Map<String, List<AppDesc>> getApplications(String container) {
        Map<String, List<AppDesc>> result = Collections.emptyMap();
        try {
            Commands.ListAppsCommand cmd = new Commands.ListAppsCommand(container);
            serverCmd = cmd;
            Future<OperationState> task = executor().submit(this);
            OperationState state = task.get();
            if (state == OperationState.COMPLETED) {
                result = cmd.getApplicationMap();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, ex.getMessage(), ex);
        } catch (ExecutionException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, ex.getMessage(), ex);
        }
        return result;
    }
    
    public List<ResourceDesc> getResources(String type) {
        List<ResourceDesc> result = Collections.emptyList();
        try {
            Commands.ListResourcesCommand cmd = new Commands.ListResourcesCommand(type);
            serverCmd = cmd;
            Future<OperationState> task = executor().submit(this);
            OperationState state = task.get();
            if (state == OperationState.COMPLETED) {
                result = cmd.getResourceList();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, ex.getMessage(), ex);
        } catch (ExecutionException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, ex.getMessage(), ex);
        }
        return result;
    }
    
    public Future<OperationState> deploy(File dir) {
        return deploy(dir, dir.getParentFile().getName(), null);
    }

    public Future<OperationState> deploy(File dir, String moduleName) {
        return deploy(dir, moduleName, null);
    }
    
    public Future<OperationState> deploy(File dir, String moduleName, String contextRoot)  {
        return execute(new Commands.DeployCommand(dir.getAbsolutePath(), moduleName, contextRoot));
    }
    
    public Future<OperationState> redeploy(String moduleName, String contextRoot)  {
        return execute(new Commands.RedeployCommand(moduleName, contextRoot));
    }
    
    public Future<OperationState> undeploy(String moduleName) {
        return execute(new Commands.UndeployCommand(moduleName));
    }
    
    /**
     * Execute an abitrary server command.
     */
    public Future<OperationState> execute(ServerCommand command) {
        return execute(command, null);
    }
    
    private Future<OperationState> execute(ServerCommand command, String msgResId) {
        serverCmd = command;
        if(msgResId != null) {
            fireOperationStateChanged(OperationState.RUNNING, msgResId, instanceName);
        }
        return executor().submit(this);
    }
    
    /**
     * Translates a context path string into <code>application/x-www-form-urlencoded</code> format.
     */
    private static String encodePath(String str) {
        try {
            StringTokenizer st = new StringTokenizer(str, "/"); // NOI18N
            if (!st.hasMoreTokens()) {
                return str;
            }
            StringBuilder result = new StringBuilder();
            while (st.hasMoreTokens()) {
                result.append("/").append(URLEncoder.encode(st.nextToken(), "UTF-8")); // NOI18N
            }
            return result.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e); // this should never happen
        }
    }
    
    /** Executes one management task. 
     */
    @Override
    public OperationState call() {
        fireOperationStateChanged(OperationState.RUNNING, "MSG_ServerCmdRunning", 
                serverCmd.toString(), instanceName);
        
        boolean httpSucceeded = false;
        boolean commandSucceeded = false;
        URL urlToConnectTo = null;
        URLConnection conn = null;
        String cmd = serverCmd.getCommand();
        String commandUrl = constructCommandUrl(cmd, true);
        int retries = ("version".equals(cmd) || "__locations".equals(cmd)) ? 1 : 3;
        
        Logger.getLogger("glassfish").log(Level.FINEST, 
                "CommandRunner.call(" + commandUrl + ") called on thread \"" + 
                Thread.currentThread().getName() + "\"");
        
        // Create a connection for this command
        try {
            urlToConnectTo = new URL(commandUrl);

            while(!httpSucceeded && retries-- > 0) {
                try {
                    Logger.getLogger("glassfish").log(Level.FINE, "V3 HTTP Command: " + commandUrl );

                    conn = urlToConnectTo.openConnection();
                    if(conn instanceof HttpURLConnection) {
                        HttpURLConnection hconn = (HttpURLConnection) conn;

                        // Set up standard connection characteristics
                        hconn.setAllowUserInteraction(false);
                        hconn.setDoInput(true);
                        hconn.setUseCaches(false);
                        hconn.setRequestMethod(serverCmd.getRequestMethod());
                        hconn.setDoOutput(serverCmd.getDoOutput());
                        String contentType = serverCmd.getContentType();
                        if(contentType != null && contentType.length() > 0) {
                            hconn.setRequestProperty("Content-Type", contentType);
                        }
                        hconn.setRequestProperty("User-Agent", "hk2-agent"); // NOI18N

//                        // Set up an authorization header with our credentials
//                        Hk2Properties tp = tm.getHk2Properties();
//                        String input = tp.getUsername () + ":" + tp.getPassword ();
//                        String auth = new String(Base64.encode(input.getBytes()));
//                        hconn.setRequestProperty("Authorization", // NOI18N
//                                                 "Basic " + auth); // NOI18N

                        // Establish the connection with the server
                        hconn.connect();
                        int respCode = hconn.getResponseCode();
                        if(respCode == HttpURLConnection.HTTP_UNAUTHORIZED || 
                                respCode == HttpURLConnection.HTTP_FORBIDDEN) {
                            // connection to manager has not been allowed
                            authorized = false;
                            return fireOperationStateChanged(OperationState.FAILED, 
                                    "MSG_AuthorizationFailed", serverCmd.toString(), instanceName); // NOI18N
                        }

                        // !PW FIXME log status for debugging purposes
                        if(Boolean.getBoolean("org.netbeans.modules.hk2.LogManagerCommands")) { // NOI18N
                            Logger.getLogger("glassfish").log(Level.FINE, 
                                    "  receiving response, code: " + respCode);
                        }

                        // Send data to server if necessary
                        handleSend(hconn);

                        // Process the response message
                        if(handleReceive(hconn)) {
                            commandSucceeded = serverCmd.processResponse();
                        }
                        
                        httpSucceeded = true;
                    } else {
                        Logger.getLogger("glassfish").log(Level.INFO, "Unexpected connection type: " +
                                urlToConnectTo);
                    }
                } catch(ProtocolException ex) {
                    fireOperationStateChanged(OperationState.FAILED, "MSG_Exception", // NOI18N
                            ex.getLocalizedMessage());
                    retries = 0;
                } catch(IOException ex) {
                    if(retries <= 0) {
                        fireOperationStateChanged(OperationState.FAILED, "MSG_Exception", // NOI18N
                                ex.getLocalizedMessage());
                    }
                }
                
                if(!httpSucceeded && retries > 0) {
                    try {
                        Thread.sleep(HTTP_RETRY_DELAY);
                    } catch (InterruptedException e) {}
                }
            } // while
        } catch(MalformedURLException ex) {
            Logger.getLogger("glassfish").log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }
        
        if(commandSucceeded) {
            return fireOperationStateChanged(OperationState.COMPLETED, "MSG_ServerCmdCompleted", // NOI18N
                    serverCmd.toString(), instanceName);
        } else {
            return fireOperationStateChanged(OperationState.FAILED, "MSG_ServerCmdFailed", // NOI18N
                    serverCmd.toString(), instanceName);
        }
    }
    
    private String constructCommandUrl(final String cmd, final boolean encodeSpaces) {
        StringBuilder builder = new StringBuilder(256);
        builder.append("http://"); // NOI18N
        builder.append(ip.get(GlassfishModule.HOSTNAME_ATTR));
        builder.append(":"); // NOI18N
        if("true".equals(System.getProperty("glassfish.useadminport"))) {
            builder.append(ip.get(GlassfishModule.ADMINPORT_ATTR));
        } else {
            builder.append(ip.get(GlassfishModule.HTTPPORT_ATTR));
        }
        builder.append("/__asadmin/");
        builder.append(cmd);
        String commandUrl = builder.toString();
        return encodeSpaces ? commandUrl.replaceAll(" ", "%20") : commandUrl;
    }
    
    private void handleSend(HttpURLConnection hconn) throws IOException {
        InputStream istream = serverCmd.getInputStream();
        if(istream != null) {
            BufferedOutputStream ostream = null;
            try {
                ostream = new BufferedOutputStream(hconn.getOutputStream(), 1024);
                byte buffer[] = new byte[1024];
                while (true) {
                    int n = istream.read(buffer);
                    if (n < 0) {
                        break;
                    }
                    ostream.write(buffer, 0, n);
                }
                ostream.flush();
            } finally {
                try {
                    istream.close();
                } catch(IOException ex) {
                        Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex);
                }
                
                if(ostream != null) {
                    try { 
                        ostream.close(); 
                    } catch(IOException ex) {
                        Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex);
                    }
                    ostream = null;
                }
            }
        } else if("PUT".equalsIgnoreCase(serverCmd.getRequestMethod())) {
            Logger.getLogger("glassfish").log(Level.INFO, "HTTP PUT request but no data stream provided");
        }
    }
    
    private boolean handleReceive(HttpURLConnection hconn) throws IOException {
        boolean result = false;
        InputStream httpInputStream = hconn.getInputStream();
        try {
            result = serverCmd.readResponse(httpInputStream);
        } finally {
            try {
                httpInputStream.close();
            } catch (IOException ex) {
                Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
        }
        return result;
    }

}
