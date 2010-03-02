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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.netbeans.modules.glassfish.spi.AppDesc;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.GlassfishModule.OperationState;
import org.netbeans.modules.glassfish.spi.OperationStateListener;
import org.netbeans.modules.glassfish.spi.ResourceDesc;
import org.netbeans.modules.glassfish.spi.ServerCommand;
import org.netbeans.modules.glassfish.spi.ServerCommand.GetPropertyCommand;
import org.netbeans.modules.glassfish.spi.ServerCommand.SetPropertyCommand;
import org.netbeans.modules.glassfish.spi.CommandFactory;
import org.netbeans.modules.glassfish.spi.Utils;


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

    private static Authenticator AUTH = new AdminAuthenticator();
    
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
    private final CommandFactory cf;
    private final boolean isReallyRunning;
    
    
    public CommandRunner(boolean isReallyRunning, CommandFactory cf, Map<String, String> properties, OperationStateListener... stateListener) {
        super(properties, stateListener);
        this.cf =cf;
        this.isReallyRunning = isReallyRunning;
    }
    
    /**
     * Sends stop-domain command to server (asynchronous)
     * 
     */
    public Future<OperationState> stopServer() {
        return execute(Commands.STOP, "MSG_STOP_SERVER_IN_PROGRESS"); // NOI18N
        
    }
    
    /**
     * Sends list-applications command to server (synchronous)
     * 
     * @return String array of names of deployed applications.
     */
    public Map<String, List<AppDesc>> getApplications(String container) {
        Map<String, List<AppDesc>> result = Collections.emptyMap();
        try {
            Map<String, List<String>> apps = Collections.emptyMap();
            Commands.ListComponentsCommand cmd = new Commands.ListComponentsCommand(container);
            serverCmd = cmd;
            Future<OperationState> task = executor().submit(this);
            OperationState state = task.get();
            if (state == OperationState.COMPLETED) {
                apps = cmd.getApplicationMap();
            }
            ServerCommand.GetPropertyCommand getCmd = new ServerCommand.GetPropertyCommand("applications.application.*");
            serverCmd = getCmd;
            task = executor().submit(this);
            state = task.get();
            if (state == OperationState.COMPLETED) {
                result = processApplications(apps, getCmd.getData());
            }
        } catch (InterruptedException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, ex.getMessage(), ex);  // NOI18N
        } catch (ExecutionException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, ex.getMessage(), ex);  // NOI18N
        }
        return result;
    }

    private Map<String, List<AppDesc>> processApplications(Map<String, List<String>> appsList, Map<String, String> properties){
        Map<String, List<AppDesc>> result = new HashMap<String, List<AppDesc>>();
        Iterator<String> appsItr = appsList.keySet().iterator();
        while (appsItr.hasNext()) {
            String engine = appsItr.next();
            List<String> apps = appsList.get(engine);
            for (int i = 0; i < apps.size(); i++) {
                String name = apps.get(i).trim();
                String appname = "applications.application." + name; // NOI18N
                String contextKey = appname + ".context-root"; // NOI18N
                String pathKey = appname + ".location"; // NOI18N

                String contextRoot = properties.get(contextKey);
                if (contextRoot == null) {
                    contextRoot = name;
                }
                if (contextRoot.startsWith("/")) {  // NOI18N
                    contextRoot = contextRoot.substring(1);
                }

                String path = properties.get(pathKey);
                if (path == null) {
                    path = "unknown"; //NOI18N
                }
                if (path.startsWith("file:")) {  // NOI18N
                    path = path.substring(5);
                }

                List<AppDesc> appList = result.get(engine);
                if(appList == null) {
                    appList = new ArrayList<AppDesc>();
                    result.put(engine, appList);
                }
                appList.add(new AppDesc(name, path, contextRoot));
            }
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
            Logger.getLogger("glassfish").log(Level.INFO, ex.getMessage(), ex);  // NOI18N
        } catch (ExecutionException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, ex.getMessage(), ex);  // NOI18N
        }
        return result;
    }
    
    public Map<String, String> getResourceData(String name) {
        try {
            GetPropertyCommand cmd;
            String query;
            // see https:/glassfish.dev.java.net/issues/show_bug.cgi?id=7296
            // revert this, when the server side of issue is resolved
            //if (null != name) {
            //    query = "resources.*."+name+".*"; // NOI18N
            //} else {
                query = "resources.*"; // NOI18N
            //}
            cmd = new ServerCommand.GetPropertyCommand(query); // NOI18N
            serverCmd = cmd;
            Future<OperationState> task = executor().submit(this);
            OperationState state = task.get();
            if (state == OperationState.COMPLETED) {
                Map<String,String> retVal = cmd.getData();
                if (retVal.isEmpty())
                    Logger.getLogger("glassfish").log(Level.INFO, null, new IllegalStateException(query+" has no data"));  // NOI18N
                return retVal;
            }
        } catch (InterruptedException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, ex.getMessage(), ex);  // NOI18N
        } catch (ExecutionException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, ex.getMessage(), ex);  // NOI18N
        }
        return new HashMap<String,String>();
    }
    
    public void putResourceData(Map<String, String> data) throws PartialCompletionException {
        Set<String> keys = data.keySet();
        String itemsNotUpdated = null;
        Throwable lastEx = null;
        for (String k : keys) {
            String compName = k;
            String compValue = data.get(k);

            try {
                SetPropertyCommand cmd = cf.getSetPropertyCommand(compName, compValue);
                serverCmd = cmd;
                Future<OperationState> task = executor().submit(this);
                OperationState state = task.get();
                if (state == OperationState.COMPLETED) {
                    cmd.processResponse();
                //return cmd.getData();
                }
            } catch (InterruptedException ex) {
                lastEx = ex;
                Logger.getLogger("glassfish").log(Level.INFO, ex.getMessage(), ex);  // NOI18N
                itemsNotUpdated = addName(compName, itemsNotUpdated);
            } catch (ExecutionException ex) {
                lastEx = ex;
                Logger.getLogger("glassfish").log(Level.INFO, ex.getMessage(), ex);  // NOI18N
                itemsNotUpdated = addName(compName, itemsNotUpdated);
            }
        //return new HashMap<String,String>();
        }
        if (null != itemsNotUpdated) {
            PartialCompletionException pce = new PartialCompletionException(itemsNotUpdated);
            if (null != lastEx) {
                pce.initCause(lastEx);
            }
            throw pce;
        }
    }

    public Future<OperationState> deploy(File dir) {
        return deploy(dir, dir.getParentFile().getName(), null);
    }

    public Future<OperationState> deploy(File dir, String moduleName) {
        return deploy(dir, moduleName, null);
    }
    
    public Future<OperationState> deploy(File dir, String moduleName, String contextRoot)  {
        return deploy(dir, moduleName, contextRoot, null);
    }
    
    Future<OperationState> deploy(File dir, String moduleName, String contextRoot, Map properties) {
        return execute(new Commands.DeployCommand(dir, moduleName,
                contextRoot, computePreserveSessions(ip), properties));
    }
        public Future<OperationState> redeploy(String moduleName, String contextRoot)  {
        return execute(new Commands.RedeployCommand(moduleName, contextRoot, 
                computePreserveSessions(ip)));
    }

    private static Boolean computePreserveSessions(Map<String,String> ip) {
        // prefer the value stored in the instance properties for a domain.
        String sessionPreservationFlag = ip.get(GlassfishModule.SESSION_PRESERVATION_FLAG);
        if (null == sessionPreservationFlag) {
            // if there isn't a value stored for the instance, use the value of
            // the command-line flag.
            sessionPreservationFlag = System.getProperty("glassfish.session.preservation.enabled","false"); // NOI18N
        }
        return Boolean.parseBoolean(sessionPreservationFlag);
    }
    
    public Future<OperationState> undeploy(String moduleName) {
        return execute(new Commands.UndeployCommand(moduleName));
    }
    
    public Future<OperationState> unregister(String resourceName, String suffix, String cmdPropName, boolean cascade) {
        return execute(new Commands.UnregisterCommand(resourceName, suffix, cmdPropName, cascade));
    }

    /**
     * Execute an abitrary server command.
     */
    public Future<OperationState> execute(ServerCommand command) {
        return execute(command, null);
    }

    private String addName(final String compName, final String itemsNotUpdated) {
        String retVal = itemsNotUpdated;
        if (null != itemsNotUpdated) {
            retVal += ", "+compName; // NOI18N
        } else {
            retVal = compName;
        }
        return retVal;
    }
    
    private Future<OperationState> execute(ServerCommand command, String msgResId) {
        serverCmd = command;
        if(msgResId != null) {
            fireOperationStateChanged(OperationState.RUNNING, msgResId, instanceName);
        }
        return executor().submit(this);
    }
    
    /** Executes one management task. 
     */
    @Override
    public OperationState call() {
        fireOperationStateChanged(OperationState.RUNNING, "MSG_ServerCmdRunning", // NOI18N
                serverCmd.toString(), instanceName);

        if (!isReallyRunning) {
            return fireOperationStateChanged(OperationState.FAILED, "MSG_ServerCmdFailedIncorrectInstance", // NOI18N
                    serverCmd.toString(), instanceName);
        }
        
        boolean httpSucceeded = false;
        boolean commandSucceeded = false;
        URL urlToConnectTo = null;
        URLConnection conn = null;
        String commandUrl;
        
        try {
            commandUrl = constructCommandUrl(serverCmd.getCommand(), serverCmd.getQuery());
        } catch (URISyntaxException ex) {
            return fireOperationStateChanged(OperationState.FAILED, "MSG_ServerCmdException",  // NOI18N
                    serverCmd.toString(), instanceName, ex.getLocalizedMessage());
        }

        int retries = 1; // disable ("version".equals(cmd) || "__locations".equals(cmd)) ? 1 : 3;
        Logger.getLogger("glassfish").log(Level.FINEST, "CommandRunner.call(" + commandUrl + ") called on thread \"" + Thread.currentThread().getName() + "\""); // NOI18N
        
        // Create a connection for this command
        try {
            urlToConnectTo = new URL(commandUrl);

            while(!httpSucceeded && retries-- > 0) {
                try {
                    Logger.getLogger("glassfish").log(Level.FINE, "HTTP Command: " + commandUrl ); // NOI18N

                    conn = urlToConnectTo.openConnection();
                    if(conn instanceof HttpURLConnection) {
                        HttpURLConnection hconn = (HttpURLConnection) conn;

                        if (conn instanceof HttpsURLConnection) {
                            // let's just trust any server that we connect to...
                            // we aren't send them money or secrets...
                            TrustManager[] tm = new TrustManager[]{
                                new X509TrustManager() {

                                    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                                        return;
                                    }

                                    public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                                        return;
                                    }

                                    public X509Certificate[] getAcceptedIssuers() {
                                        return null;
                                    }
                                }
                            };

                            SSLContext context = null;
                            try {
                                context = SSLContext.getInstance("SSL");
                                context.init(null, tm, null);
                                ((HttpsURLConnection)hconn).setSSLSocketFactory(context.getSocketFactory());
                            } catch (Exception ex) {
                                // if there is an issue here... there will be another exception later
                                // which will take care of the user interaction...
                                Logger.getLogger("glassfish").log(Level.INFO, "trust manager problem: " + urlToConnectTo,ex); // NOI18N
                            }

                        }

                        // Set up standard connection characteristics
                        hconn.setAllowUserInteraction(false);
                        hconn.setDoInput(true);
                        hconn.setUseCaches(false);
                        hconn.setRequestMethod(serverCmd.getRequestMethod());
                        hconn.setDoOutput(serverCmd.getDoOutput());
                        String contentType = serverCmd.getContentType();
                        if(contentType != null && contentType.length() > 0) {
                            hconn.setRequestProperty("Content-Type", contentType); // NOI18N
                        }
                        hconn.setRequestProperty("User-Agent", "hk2-agent"); // NOI18N

//                        // Set up an authorization header with our credentials
//                        Hk2Properties tp = tm.getHk2Properties();
//                        String input = tp.getUsername () + ":" + tp.getPassword ();
//                        String auth = new String(Base64.encode(input.getBytes()));
//                        hconn.setRequestProperty("Authorization", // NOI18N
//                                                 "Basic " + auth); // NOI18N

                        // Establish the connection with the server
                        Authenticator.setDefault(AUTH);
                        hconn.connect();
                        // Send data to server if necessary
                        handleSend(hconn);

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
                            Logger.getLogger("glassfish").log(Level.FINE, "  receiving response, code: " + respCode); // NOI18N
                        }

                        // Process the response message
                        if(handleReceive(hconn)) {
                            commandSucceeded = serverCmd.processResponse();
                        }
                        
                        httpSucceeded = true;
                    } else {
                        Logger.getLogger("glassfish").log(Level.INFO, "Unexpected connection type: " + urlToConnectTo); // NOI18N
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
            Logger.getLogger("glassfish").log(Level.WARNING, ex.getLocalizedMessage(), ex); // NOI18N
        }
        
        if(commandSucceeded) {
            return fireOperationStateChanged(OperationState.COMPLETED, "MSG_ServerCmdCompleted", // NOI18N
                    serverCmd.toString(), instanceName);
        } else {
            return fireOperationStateChanged(OperationState.FAILED, "MSG_ServerCmdFailed", // NOI18N
                    serverCmd.toString(), instanceName);
        }
    }
    
    private String constructCommandUrl(final String cmd, final String query) throws URISyntaxException {
        String host = ip.get(GlassfishModule.HOSTNAME_ATTR);
        boolean useAdminPort = !"false".equals(System.getProperty("glassfish.useadminport")); // NOI18N
        int port = Integer.parseInt(ip.get(useAdminPort ? GlassfishModule.ADMINPORT_ATTR : GlassfishModule.HTTPPORT_ATTR));
        URI uri = new URI(Utils.getHttpListenerProtocol(host,port), null, host, port, "/__asadmin/" + cmd, query, null); // NOI18N
        return uri.toASCIIString().replace("+", "%2b"); // these characters don't get handled by GF correctly... best I can tell.
    }
    

    /*
     * Note: this is based on reading the code of CLIRemoteCommand.java
     * from the server's code repository... Since some asadmin commands
     * need to send multiple files, the server assumes the input is a ZIP
     * stream.
     */
    private void handleSend(HttpURLConnection hconn) throws IOException {
        InputStream istream = serverCmd.getInputStream();
        if(istream != null) {
            ZipOutputStream ostream = null;
            try {
                ostream = new ZipOutputStream(new BufferedOutputStream(hconn.getOutputStream(), 1024*1024));
                ZipEntry e = new ZipEntry(serverCmd.getInputName());
                e.setExtra(getExtraProperties());
                ostream.putNextEntry(e);
                byte buffer[] = new byte[1024*1024];
                while (true) {
                    int n = istream.read(buffer);
                    if (n < 0) {
                        break;
                    }
                    ostream.write(buffer, 0, n);
                }
                ostream.closeEntry();
                ostream.flush();
            } finally {
                try {
                    istream.close();
                } catch(IOException ex) {
                    Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex); // NOI18N
                }
                if(ostream != null) {
                    try { 
                        ostream.close(); 
                    } catch(IOException ex) {
                        Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex);  // NOI18N
                    }
                    ostream = null;
                }
            }
        } else if("POST".equalsIgnoreCase(serverCmd.getRequestMethod())) { // NOI18N
            Logger.getLogger("glassfish").log(Level.INFO, "HTTP POST request but no data stream provided"); // NOI18N
        }
    }
    
    private byte [] getExtraProperties() {
        boolean success = false;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            Properties props = new Properties();
            props.setProperty("data-request-type", "file-xfer"); // NOI18N
            props.setProperty("data-request-name", serverCmd.getInputName()); // NOI18N
            props.setProperty("last-modified", serverCmd.getLastModified()); // NOI18N
            oos = new ObjectOutputStream(baos);
            oos.writeObject(props);
        } catch(IOException ex) {
            Logger.getLogger("glassfish").log(Level.WARNING, // NOI18N
                    "Error writing zip properties for archive deployment of " + serverCmd.getInputName(), ex); // NOI18N
        } finally {
            if(oos != null) {
                try {
                    oos.close();
                    success = true;
                } catch(IOException ex) {
                    Logger.getLogger("glassfish").log(Level.WARNING, // NOI18N
                            "Error writing zip properties for archive deployment of " + serverCmd.getInputName(), ex); // NOI18N
                }
            }
        }
        return success ? baos.toByteArray() : new byte [0];
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
                Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex);  // NOI18N
            }
        }
        return result;
    }

}
