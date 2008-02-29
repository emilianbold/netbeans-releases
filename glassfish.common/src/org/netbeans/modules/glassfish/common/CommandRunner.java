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
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.glassfish.GlassfishModule;
import org.netbeans.spi.glassfish.GlassfishModule.OperationState;
import org.netbeans.spi.glassfish.OperationStateListener;


/** 
 * Implementation of management task that provides info about progress
 *
 * @author Peter Williams
 */
public class CommandRunner extends BasicTask<OperationState> {
    
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
        serverCmd = ServerCommand.STOP;
        fireOperationStateChanged(OperationState.RUNNING, "MSG_STOP_SERVER_IN_PROGRESS", instanceName);
        return executor().submit(this);
    }
    
    /**
     * Sends list-applications command to server (synchronous)
     * 
     * @return String array of names of deployed applications.
     */
    public String [] getApplications() {
        String[] result = new String[0];
        try {
            ServerCommand.ListCommand cmd = new ServerCommand.ListCommand();
            serverCmd = cmd;
            Future<OperationState> task = executor().submit(this);
            OperationState state = task.get();
            if (state == OperationState.COMPLETED) {
                result = cmd.getApplications();
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
//        try {
//            //file is someting like /Users/ludo/WebApplication91/build/web
//            String docBaseURI = URLEncoder.encode(dir.getAbsoluteFile().toURI().toASCIIString(),"UTF-8");
//            String docBase = moduleName;
//            String ctxPath = docBase;///ctx.getAttributeValue ("path");
//            this.tmId = new Hk2TargetModuleID(t, ctxPath, docBase); //NOI18N
//            
////            command = "deploy?path=" + dir.getAbsoluteFile()+"?name="+docBaseURI; // NOI18N
//            command = "deploy?path=" + dir.getAbsoluteFile()+"?name="+docBase; // NOI18N
//            
//            serverCmd = ServerCommand.DISTRIBUTE;
////            System.out.println("deploy command="+command);
//            String msg = NbBundle.getMessage(Manager.class, "MSG_DeploymentInProgress");
//            fireOperationStateChanged(null, new Status(ActionType.EXECUTE, serverCmd, msg, StateType.RUNNING));
//            rp().post(this, 0, Thread.NORM_PRIORITY);
//        } catch (UnsupportedEncodingException ex) {
//            ex.printStackTrace();
//            String msg = NbBundle.getMessage(Manager.class, "MSG_DeployBrokenContextXml");
//            fireOperationStateChanged(null, new Status(ActionType.EXECUTE, serverCmd, msg, StateType.FAILED));
//        } catch (RuntimeException e) {
//            String msg = NbBundle.getMessage(Manager.class, "MSG_DeployBrokenContextXml");
//            fireOperationStateChanged(null, new Status(ActionType.EXECUTE, serverCmd, msg, StateType.FAILED));
//        }
        
        ServerCommand.DeployCommand cmd = new ServerCommand.DeployCommand(dir.getAbsolutePath(), moduleName, contextRoot);
        serverCmd = cmd;
        return executor().submit(this);
    }
    
    public Future<OperationState> redeploy(String moduleName)  {
//        try {
//            this.tmId = (Hk2TargetModuleID) targetModuleID;
//            command = "redeploy?name=" +targetModuleID.getModuleID(); // NOI18N
//            serverCmd = ServerCommand.DISTRIBUTE;
////            System.out.println("redeploy command="+command);
//            String msg = NbBundle.getMessage(Manager.class, "MSG_DeploymentInProgress");
//            fireOperationStateChanged(null, new Status(ActionType.EXECUTE, serverCmd, msg, StateType.RUNNING));
//            rp().post(this, 0, Thread.NORM_PRIORITY);
//            
//        } catch (RuntimeException e) {
//            String msg = NbBundle.getMessage(Manager.class, "MSG_DeployBrokenContextXml");
//            fireOperationStateChanged(null, new Status(ActionType.EXECUTE, serverCmd, msg, StateType.FAILED));
//        }
        
        ServerCommand.RedeployCommand cmd = new ServerCommand.RedeployCommand(moduleName);
        serverCmd = cmd;
        return executor().submit(this);
    }
    
//    public  TargetModuleID[] getTargetModuleID(Target t){
//        command = "list-applications"; // NOI18N
//        cmdType = CommandType.DISTRIBUTE;
//        run();
//        if (tmidNames==null){
//            return null;
//        }
//        TargetModuleID ret[] = new TargetModuleID[tmidNames.size()];
//        for (int i=0;i< tmidNames.size();i++){
//            ret[i]=new Hk2TargetModuleID(t,tmidNames.get(i),tmidNames.get(i));
//        }
//        return ret;
//    }
    
    public Future<OperationState> undeploy(String moduleName) {
//        this.tmId = tmId;
//        command = "undeploy?name="+tmId.getModuleID(); // NOI18N
//        serverCmd = ServerCommand.UNDEPLOY;
//        String msg = NbBundle.getMessage(Manager.class, "MSG_UndeploymentInProgress");
//        fireOperationStateChanged(null, new Status(ActionType.EXECUTE, serverCmd, msg, StateType.RUNNING));
//        rp().post(this, 0, Thread.NORM_PRIORITY);
        
        ServerCommand.UndeployCommand cmd = new ServerCommand.UndeployCommand(moduleName);
        serverCmd = cmd;
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
        
        int retries = 3;
        boolean succeeded = false;
        URL urlToConnectTo = null;
        URLConnection conn = null;
        String commandUrl = constructCommandUrl(true);
        
        System.out.println("CommandRunner.call(" + commandUrl + ") called on thread \"" + 
                Thread.currentThread().getName() + "\"");
        
        // Create a connection for this command
        try {
            urlToConnectTo = new URL(commandUrl);
            Logger.getLogger("glassfish").log(Level.INFO, "V3 HTTP Command: " + commandUrl);

            while(retries-- > 0) {
                try {
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
                                    "MSG_AuthorizationFailed", serverCmd.toString(), instanceName);
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
                            succeeded = serverCmd.processResponse();
                        }
                    } else {
                        Logger.getLogger("glassfish").log(Level.INFO, "Unexpected connection type: " + 
                                urlToConnectTo);
                    }
                } catch(ProtocolException ex) {
                    fireOperationStateChanged(OperationState.FAILED, "MSG_Exception", 
                            ex.getLocalizedMessage());
                    retries = 0;
                } catch(IOException ex) {
                    if(retries <= 0) {
                        fireOperationStateChanged(OperationState.FAILED, "MSG_Exception", 
                                ex.getLocalizedMessage());
                    }
                }

                if(!succeeded && retries > 0) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {}
                }
            } // while
        } catch(MalformedURLException ex) {
            Logger.getLogger("glassfish").log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }
        
        if(succeeded) {
            return fireOperationStateChanged(OperationState.COMPLETED, "MSG_ServerCmdCompleted", 
                    serverCmd.toString(), instanceName);
        } else {
            return fireOperationStateChanged(OperationState.FAILED, "MSG_ServerCmdFailed", 
                    serverCmd.toString(), instanceName);
        }
    }
    
    private String constructCommandUrl(final boolean encodeSpaces) {
        StringBuilder builder = new StringBuilder(256);
        builder.append("http://"); // NOI18N
        builder.append(ip.get(GlassfishModule.HOSTNAME_ATTR));
        builder.append(":"); // NOI18N
        builder.append(ip.get(GlassfishModule.HTTPPORT_ATTR));
        builder.append("/__asadmin/");
        builder.append(serverCmd.getCommand());
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
