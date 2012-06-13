/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.glassfish.common;

import java.io.*;
import java.net.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map.Entry;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.net.ssl.*;
import org.glassfish.tools.ide.admin.*;
import org.netbeans.modules.glassfish.spi.GlassfishModule.OperationState;
import org.netbeans.modules.glassfish.spi.ServerCommand.GetPropertyCommand;
import org.netbeans.modules.glassfish.spi.ServerCommand.SetPropertyCommand;
import org.netbeans.modules.glassfish.spi.*;


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


    public CommandRunner(boolean isReallyRunning, CommandFactory cf,
            GlassfishInstance instance, OperationStateListener... stateListener) {
        super(instance, stateListener);
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
     * Sends restart-domain command to server (asynchronous)
     *
     */
    public Future<OperationState> restartServer(int debugPort, String query) {
        final String restartQuery = query; // cf.getRestartQuery(debugPort);
        if (-1 == debugPort || "".equals(restartQuery) ) {
            return execute(new ServerCommand("restart-domain") {

                @Override
                public String getQuery() {
                    return restartQuery;
                }
            }, "MSG_RESTART_SERVER_IN_PROGRESS"); // NOI18N
        }
        // force the options to be correct for remote debugging, then restart...
        CommandRunner inner = new CommandRunner(isReallyRunning, cf, instance,
                new OperationStateListener() {

            @Override
            public void operationStateChanged(OperationState newState, String message) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

        });

        // I wish that the server folks had let me add a port number to the
        // restart-domain --debug command... but this will have to do until then

        ServerCommand.GetPropertyCommand getCmd = new ServerCommand.GetPropertyCommand("configs.config.server-config.java-config.debug-options");

        OperationState state = null;
        try {
            state = inner.execute(getCmd).get();
        } catch (InterruptedException ie) {
            Logger.getLogger("glassfish").log(Level.INFO,debugPort+"",ie);
        } catch (ExecutionException ee) {
            Logger.getLogger("glassfish").log(Level.INFO,debugPort+"",ee);
        }
        String qs = null;
        if (state == OperationState.COMPLETED) {
            Map<String, String> data = getCmd.getData();
            if (!data.isEmpty()) {
                // now I can reset the debug data
                String oldValue = data.get("configs.config.server-config.java-config.debug-options");
                ServerCommand.SetPropertyCommand setCmd =
                        cf.getSetPropertyCommand("configs.config.server-config.java-config.debug-options",
                        oldValue.replace("transport=dt_shmem", "transport=dt_socket").
                        replace("address=[^,]+", "address=" + debugPort));
                //serverCmd = setCmd;
                //task = executor.submit(this);
                try {
                    state = inner.execute(setCmd).get();
                    qs = "debug=true";
                } catch (InterruptedException ie) {
                     Logger.getLogger("glassfish").log(Level.INFO,debugPort+"",ie);
                } catch (ExecutionException ee) {
                     Logger.getLogger("glassfish").log(Level.INFO,debugPort+"",ee);
                }
            }
        }
        if (null == qs) {
            qs = "debug=false";
        }
        final String fqs = qs;
        return execute(new ServerCommand("restart-domain") {

            @Override
            public String getQuery() {
                return fqs;
            }
        }, "MSG_RESTART_SERVER_IN_PROGRESS");
    }

    /**
     * Sends list-applications command to server (synchronous)
     *
     * @return String array of names of deployed applications.
     */
    public Map<String, List<AppDesc>> getApplications(String container) {
        CommandRunner inner = new CommandRunner(isReallyRunning, cf, instance,
                new OperationStateListener() {

            @Override
            public void operationStateChanged(OperationState newState, String message) {
                //throw new UnsupportedOperationException("Not supported yet.");
            }

        });
        Map<String, List<AppDesc>> result = Collections.emptyMap();
        try {
            Map<String, List<String>> apps = Collections.emptyMap();
            Command command = new CommandListComponents(
                    Util.computeTarget(instance.getProperties()));
            Future<ResultMap<String, List<String>>> future = 
                    ServerAdmin.<ResultMap<String,
                    List<String>>>exec(instance, command, null);
            ResultMap<String, List<String>> resultMap = future.get();
            TaskState state = resultMap.getState();
            if (state == TaskState.COMPLETED) {
                apps = resultMap.getValue();
            }
            if (null == apps || apps.isEmpty()) {
                return result;
            }
            ServerCommand.GetPropertyCommand getCmd = new ServerCommand.GetPropertyCommand("applications.application.*"); // NOI18N
            OperationState operationState = inner.execute(getCmd).get();
            if (operationState == OperationState.COMPLETED) {
                ServerCommand.GetPropertyCommand getRefs = new ServerCommand.GetPropertyCommand("servers.server.*.application-ref.*"); // NOI18N
                operationState = inner.execute(getRefs).get();
                if (OperationState.COMPLETED == operationState) {
                    result = processApplications(apps, getCmd.getData(),getRefs.getData());
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, ex.getMessage(), ex);  // NOI18N
        } catch (ExecutionException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, ex.getMessage(), ex);  // NOI18N
        }
        return result;
    }

    private Map<String, List<AppDesc>> processApplications(Map<String, List<String>> appsList, Map<String, String> properties, Map<String, String> refProperties){
        Map<String, List<AppDesc>> result = new HashMap<String, List<AppDesc>>();
        Iterator<String> appsItr = appsList.keySet().iterator();
        while (appsItr.hasNext()) {
            boolean enabled = false;
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
                    path = (new File(path)).getAbsolutePath();
                }

                String enabledKey = "servers.server.server.application-ref."+name+".enabled";  //NOI18N
                // XXX - this needs to be more focused... does it need to list of
                //  servers that are associated with the target?
                for (String possibleKey : refProperties.keySet()) {
                    if (possibleKey.endsWith(".application-ref."+name+".enabled")) { // NOI18N
                        enabledKey = possibleKey;
                    }
                }
                String enabledValue = refProperties.get(enabledKey);
                if (null != enabledValue) {
                    enabled = Boolean.parseBoolean(enabledValue);

                    List<AppDesc> appList = result.get(engine);
                    if(appList == null) {
                        appList = new ArrayList<AppDesc>();
                        result.put(engine, appList);
                    }
                    appList.add(new AppDesc(name, path, contextRoot, enabled));
                }
            }
        }
        return result;
    }

    /**
     * Sends list-web-services command to server (synchronous)
     *
     * @return String array of names of deployed applications.
     */
    public List<WSDesc> getWebServices() {
        List<WSDesc> result = Collections.emptyList();
        try {
            List<String> wss = Collections.emptyList();
            Commands.ListWebservicesCommand cmd = new Commands.ListWebservicesCommand();
            serverCmd = cmd;
            Future<OperationState> task = executor().submit(this);
            OperationState state = task.get();
            if (state == OperationState.COMPLETED) {
                wss = cmd.getWebserviceList();

                result = processWebServices(wss);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, ex.getMessage(), ex);  // NOI18N
        } catch (ExecutionException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, ex.getMessage(), ex);  // NOI18N
        }
        return result;
    }

    private List<WSDesc> processWebServices(List<String> wssList){
        List<WSDesc> result = new  ArrayList<WSDesc>();
        for (String a : wssList) {
            result.add(new WSDesc(a, a+"?wsdl", a+"?Tester")); // NOI18N
        }
        return result;
    }

    public List<ResourceDesc> getResources(String type) {
        List<ResourceDesc> result = Collections.emptyList();
        try {
            Commands.ListResourcesCommand cmd
                    = new Commands.ListResourcesCommand(
                    type, Util.computeTarget(instance.getProperties()));
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
        return deploy(dir, moduleName, contextRoot, null, new File[0]);
    }

    public Future<OperationState> deploy(File dir, String moduleName, String contextRoot, Map<String,String> properties, File[] libraries) {
        LogViewMgr.displayOutput(instance, null);
        return execute(new Commands.DeployCommand(dir, moduleName,
                contextRoot, computePreserveSessions(instance), properties, libraries,
                Util.computeTarget(instance.getProperties())));
    }

    public Future<OperationState> redeploy(String moduleName, String contextRoot, File[] libraries, boolean resourcesChanged)  {
        LogViewMgr.displayOutput(instance, null);
        boolean preserve = computePreserveSessions(instance).booleanValue();
        return execute(new Commands.RedeployCommand(moduleName, contextRoot,
                preserve, libraries, resourcesChanged,
                computeAdditionalParam(instance, preserve),
                Util.computeTarget(instance.getProperties())));
    }

    private static String computeAdditionalParam(GlassfishInstance instance, boolean preserve) {
        String retval = null;
        if (preserve) {
            String url = instance.getProperty(GlassfishModule.URL_ATTR);
            if (null != url && url.contains("ee6wc")) { // NOI18N
                retval = "keepState=true"; // NOI18N
            }
        }
        return retval;
    }

    private static Boolean computePreserveSessions(GlassfishInstance instance) {
        // prefer the value stored in the instance properties for a domain.
        String sessionPreservationFlag = instance.getProperty(
                GlassfishModule.SESSION_PRESERVATION_FLAG);
        if (null == sessionPreservationFlag) {
            // If there isn't a value stored for the instance, use the value of
            // the command-line flag.
            sessionPreservationFlag = System.getProperty(
                    "glassfish.session.preservation.enabled","false"); // NOI18N
        }
        return Boolean.parseBoolean(sessionPreservationFlag);
    }

    public Future<OperationState> undeploy(String moduleName) {
        LogViewMgr.displayOutput(instance, null);
        return execute(new Commands.UndeployCommand(moduleName,
                Util.computeTarget(instance.getProperties())));
    }

    public Future<OperationState> enable(String moduleName) {
        return execute(new Commands.EnableCommand(moduleName,
                Util.computeTarget(instance.getProperties())));
    }

    public Future<OperationState> disable(String moduleName) {
        return execute(new Commands.DisableCommand(moduleName,
                Util.computeTarget(instance.getProperties())));
    }

    public Future<OperationState> unregister(String resourceName, String suffix,
            String cmdPropName, boolean cascade) {
        return execute(new Commands.UnregisterCommand(resourceName, suffix,
                cmdPropName, cascade,
                Util.computeTarget(instance.getProperties())));
    }

    /**
     * Execute an arbitrary server command.
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
        HttpURLConnection hconn = null;
        String commandUrl;

        try {
            commandUrl = constructCommandUrl(serverCmd.getSrc(), serverCmd.getCommand(), serverCmd.getQuery());
        } catch (URISyntaxException ex) {
            return fireOperationStateChanged(OperationState.FAILED, "MSG_ServerCmdException",  // NOI18N
                    serverCmd.toString(), instanceName, ex.getLocalizedMessage());
        }

        int retries = 1; // disable ("version".equals(cmd) || "__locations".equals(cmd)) ? 1 : 3;
        Logger.getLogger("glassfish").log(Level.FINEST, "CommandRunner.call({0}) called on thread \"{1}\"", new Object[]{commandUrl, Thread.currentThread().getName()}); // NOI18N

        // Create a connection for this command
        try {
            urlToConnectTo = new URL(commandUrl);

            while(!httpSucceeded && retries-- > 0) {
                try {
                    Logger.getLogger("glassfish").log(Level.FINE, "HTTP Command: {0}", commandUrl); // NOI18N

                    conn = urlToConnectTo.openConnection();
                    if (conn instanceof HttpURLConnection) {
                        int respCode = 0;
                        URL oldUrlToConnectTo;
                        do { // deal with possible redirects from 3.1
                            oldUrlToConnectTo = urlToConnectTo;
                            hconn = (HttpURLConnection) conn;

                            if (conn instanceof HttpsURLConnection) {
                                // let's just trust any server that we connect to...
                                // we aren't send them money or secrets...
                                TrustManager[] tm = new TrustManager[]{
                                    new X509TrustManager() {

                                        @Override
                                        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                                            return;
                                        }

                                        @Override
                                        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                                            return;
                                        }

                                        @Override
                                        public X509Certificate[] getAcceptedIssuers() {
                                            return null;
                                        }
                                    }
                                };

                                SSLContext context = null;
                                try {
                                    context = SSLContext.getInstance("SSL");
                                    context.init(null, tm, null);
                                    ((HttpsURLConnection) hconn).setSSLSocketFactory(context.getSocketFactory());
                                    ((HttpsURLConnection) hconn).setHostnameVerifier(new HostnameVerifier() {

                                        @Override
                                        public boolean verify(String string, SSLSession ssls) {
                                            return true;
                                        }
                                    });
                                } catch (Exception ex) {
                                    // if there is an issue here... there will be another exception later
                                    // which will take care of the user interaction...
                                    Logger.getLogger("glassfish").log(Level.INFO, "trust manager problem: " + urlToConnectTo, ex); // NOI18N
                                }

                            }

                            // Set up standard connection characteristics
                            hconn.setAllowUserInteraction(false);
                            hconn.setDoInput(true);
                            hconn.setUseCaches(false);
                            hconn.setRequestMethod(serverCmd.getRequestMethod());
                            hconn.setDoOutput(serverCmd.getDoOutput());
                            String contentType = serverCmd.getContentType();
                            if (contentType != null && contentType.length() > 0) {
                                hconn.setRequestProperty("Content-Type", contentType); // NOI18N
                                hconn.setChunkedStreamingMode(0);
                            } else {
                                // work around that helps prevent tickling the
                                // GF issue that is the root cause of 195384.
                                //
                                // GF doesn't expect to get image content, so it doesn't
                                // try to handle the content... which prevents the
                                // exception, according to Tim Quinn.
                                hconn.setRequestProperty("Content-Type", "image/png");
                            }
                            hconn.setRequestProperty("User-Agent", "hk2-agent"); // NOI18N
                            if (serverCmd.acceptsGzip()) {
                                hconn.setRequestProperty("Accept-Encoding", "gzip");
                            }

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

                            respCode = hconn.getResponseCode();
                            if (respCode == HttpURLConnection.HTTP_UNAUTHORIZED
                                    || respCode == HttpURLConnection.HTTP_FORBIDDEN) {
                                // connection to manager has not been allowed
                                authorized = false;
                                String messageId = "MSG_AuthorizationFailed";  // NOI18N
                                if (instance.getProperty(GlassfishModule.DOMAINS_FOLDER_ATTR) == null) {
                                    messageId = "MSG_AuthorizationFailedRemote"; // NOI18N
                                }
                                return fireOperationStateChanged(OperationState.FAILED,
                                        messageId, serverCmd.toString(), instanceName);
                            } else if (respCode == HttpURLConnection.HTTP_MOVED_TEMP
                                    || respCode == HttpURLConnection.HTTP_MOVED_PERM) {
                                String newUrl = hconn.getHeaderField("Location");
                                if (null == newUrl || "".equals(newUrl.trim())) {
                                    Logger.getLogger("glassfish").log(Level.SEVERE,
                                            "invalid redirect for {0}", urlToConnectTo.toString());  //NOI18N
                                } else {
                                    Logger.getLogger("glassfish").log(Level.FINE, "  moved to {0}", newUrl); // NOI18N
                                    urlToConnectTo = new URL(newUrl);
                                    conn = urlToConnectTo.openConnection();
                                    hconn.disconnect();
                                }
                            }
                        } while (urlToConnectTo != oldUrlToConnectTo);

                        // !PW FIXME log status for debugging purposes
                        if(Boolean.getBoolean("org.netbeans.modules.hk2.LogManagerCommands")) { // NOI18N
                            Logger.getLogger("glassfish").log(Level.FINE, "  receiving response, code: {0}", respCode); // NOI18N
                        }

                        // Process the response message
                        if(handleReceive(hconn)) {
                            commandSucceeded = serverCmd.processResponse();
                        } else {
                            if (!serverCmd.isSilentFailureAllowed()) {
                                Logger.getLogger("glassfish").log(Level.WARNING, hconn.toString());
                                Logger.getLogger("glassfish").log(Level.WARNING, hconn.getContentType());
                                Logger.getLogger("glassfish").log(Level.WARNING, hconn.getContentEncoding());
                                Map<String,List<String>> ms2ls = hconn.getHeaderFields();
                                Logger.getLogger("glassfish").log(Level.WARNING, "Header Fields");
                                for (Entry<String,List<String>> e : ms2ls.entrySet()) {
                                    Logger.getLogger("glassfish").log(Level.WARNING, e.getKey()+" = ");
                                    for (String v : e.getValue()) {
                                        Logger.getLogger("glassfish").log(Level.WARNING, "     "+v);
                                    }
                                }
                            }
                        }

                        httpSucceeded = true;
                    } else {
                        Logger.getLogger("glassfish").log(Level.INFO, "Unexpected connection type: {0}", urlToConnectTo); // NOI18N
                    }
                } catch(ProtocolException ex) {
                    fireOperationStateChanged(OperationState.FAILED, "MSG_Exception", // NOI18N
                            ex.getLocalizedMessage());
                    retries = 0;
                } catch (ConnectException ce) {
                    return fireOperationStateChanged(OperationState.FAILED,"MSG_EmptyMessage"); // NOI18N
                } catch(IOException ex) {
                    if(retries <= 0) {
                        fireOperationStateChanged(OperationState.FAILED, "MSG_Exception", // NOI18N
                                ex.getLocalizedMessage());
                    }
                } finally {
                    if (null != hconn) hconn.disconnect();
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
                    serverCmd.toString(), instanceName, serverCmd.getServerMessage());
        }
    }

    private String constructCommandUrl(final String cmdSrc, final String cmd,
            final String query) throws URISyntaxException {
        String host = instance.getProperty(GlassfishModule.HOSTNAME_ATTR);
        boolean useAdminPort = !"false".equals(System.getProperty(
                "glassfish.useadminport")); // NOI18N
        int port = Integer.parseInt(instance.getProperty(useAdminPort
                ? GlassfishModule.ADMINPORT_ATTR
                : GlassfishModule.HTTPPORT_ATTR));
        String protocol = "http";
        String url = instance.getProperty(GlassfishModule.URL_ATTR);
        String domainsDir = instance.getProperty(
                GlassfishModule.DOMAINS_FOLDER_ATTR);
        if (null == url) {
            protocol = getHttpListenerProtocol(host,port, ":::"+cmd+"?"+query);
        } else if (!(url.contains("ee6wc"))) {
            protocol = getHttpListenerProtocol(host,port,url+":::"+cmd+"?"+query);
        } else if (url.contains("ee6wc")
                && (null == domainsDir || "".equals(domainsDir))) {
            protocol = "https";
        }
        URI uri = new URI(protocol, null, host, port, cmdSrc + cmd, query,
                null);
        // These characters don't get handled by GF correctly...
        // Best I can tell.
        return uri.toASCIIString().replace("+", "%2b");
    }


    private static String getHttpListenerProtocol(String hostname, int port, String url) {
        String retVal = "http";  // NOI18N
        try {
            if (Utils.isSecurePort(hostname, port)) {
                retVal = "https"; // NOI18N
            }
        } catch (ConnectException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, hostname + ":" + port + "::" + url, ex); // NOI18N
        } catch (SocketException ex) {
            Logger.getLogger("glassfish").log(Level.FINE, hostname + ":" + port + "::" + url, ex); // NOI18N
        } catch (SocketTimeoutException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, hostname + ":" + port + "::" + url, ex); // NOI18N
        } catch (IOException ex) {
            Logger.getLogger("glassfish").log(Level.INFO, hostname + ":" + port + "::" + url, ex); // NOI18N
        }
        return retVal;
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

    private byte[] getExtraProperties() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Properties props = new Properties();
        props.setProperty("data-request-type", "file-xfer"); // NOI18N
        props.setProperty("last-modified", serverCmd.getLastModified()); // NOI18N
        props.put("data-request-name", "DEFAULT");
        props.put("data-request-is-recursive", "true");
        props.put("Content-Type", "application/octet-stream");
        props.list(new java.io.PrintStream(baos));
        return baos.toByteArray();
    }

    private boolean handleReceive(HttpURLConnection hconn) throws IOException {
        boolean result = false;
        InputStream httpInputStream = hconn.getInputStream();
        try {
            result = serverCmd.readResponse(httpInputStream, hconn);
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
