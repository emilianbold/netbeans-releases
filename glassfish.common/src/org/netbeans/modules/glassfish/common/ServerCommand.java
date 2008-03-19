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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.netbeans.spi.glassfish.AppDesc;


/**
 * Abstraction of commands for V3 server administration
 *
 * @author Peter Williams
 */
public abstract class ServerCommand {

    private ServerCommand() {
    }
    
    /**
     * Override to provide the server command represented by this object.  Caller
     * will prefix with http://host:port/__asadmin/ and open the server connection.
     * 
     * @return suffix to append to [host]/__asadmin/ for server command.
     */
    public abstract String getCommand();

    /**
     * Override to change the type of HTTP method used for this command.
     * Default is GET.
     * 
     * @return HTTP method (GET, PUT, etc.)
     */
    public String getRequestMethod() {
        return "GET"; // NOI18N
    }
    
    /**
     * Override and return true to send information to the server (HTTP PUT).
     * Default is false.
     * 
     * @return HTTP method (GET, PUT, etc.)
     */
    public boolean getDoOutput() {
        return false;
    }
    
    /**
     * Override to set the content-type of information sent to the server.
     * Default is null (not set).
     * 
     * @return HTTP method (GET, PUT, etc.)
     */
    public String getContentType() {
        return null;
    }
    
    /**
     * Override to provide a data stream for PUT requests.  Data will be read
     * from this stream [until EOF?] and sent to the server.
     * 
     * @return a new InputStream derivative that provides the data to send
     *  to the server.  Caller is responsible for closing the stream.  Can
     *  return null, in which case no data will be sent.
     */
    public InputStream getInputStream() {
        return null;
    }
    
    /**
     * Override for command specific failure checking.
     * 
     * @param responseCode code returned by http request
     * @return true if response was acceptable (e.g. 200) and handling of result
     * should proceed.
     */
    public boolean handleResponse(int responseCode) {
        return responseCode == 200;
    }
    
    /**
     * Override this to read the response data sent by the server (e.g. list-applications
     * sends the currently deployed applications.)  Do not close the stream parameter
     * when finished.  Caller will take care of that.
     * 
     * @param in Stream to read data from.
     * @return true if response was read correctly.
     * @throws java.io.IOException in case of stream error.
     */
    public boolean readResponse(InputStream in) throws IOException {
        return true;
    }
    
    /**
     * Override this to parse, validate, and/or format any data read from the 
     * server in readResponse().
     * 
     * @return true if data was processed correctly.
     */
    public boolean processResponse() {
        return true;
    }
    
    // ------------------------------------------------------------------------
    // Specific server commands.
    // ------------------------------------------------------------------------
    
    /**
     * Command to start a server domain
     */
    public static final ServerCommand START = new ServerCommand() {
        
        @Override
        public String getCommand() { 
            return "start-domain"; // NOI18N
        } 
        
    };
    
    /**
     * Command to stop a server domain
     */
    public static final ServerCommand STOP = new ServerCommand() {
        
        @Override
        public String getCommand() { 
            return "stop-domain"; // NOI18N
        } 
        
    };
    
    /**
     * Command to list applications current deployed on the server.
     */
    public static final class ListCommand extends ServerCommand {
        
        private final String container;
        private Manifest list = null;
        private Map<String, List<AppDesc>> appMap = null; 
        
        public ListCommand() {
            this(null);
        }
        
        public ListCommand(final String container) {
            this.container = container;
        }
        
        public String [] getContainers() {
            String [] result = null;
            if(appMap != null && appMap.size() > 0) {
                Set<String> containers = appMap.keySet();
                result = containers.toArray(new String[containers.size()]);
            }
            return result != null ? result : new String[0];
        }
        
        public Map<String, List<AppDesc>> getApplicationMap() {
            // !PW Can still modify sublist... is there a better structure?
            if(appMap != null) {
                return Collections.unmodifiableMap(appMap);
            } else {
                return Collections.emptyMap();
            }
        }
        
        @Override
        public String getCommand() { 
            return "list-applications"; // NOI18N
        }
        
        @Override
        public boolean readResponse(InputStream in) throws IOException {
            boolean result = false;

            Manifest m = new Manifest();
            m.read(in);
            String outputCode = m.getMainAttributes().getValue("exit-code"); // NOI18N
            if(outputCode.equalsIgnoreCase("Success")) { // NOI18N
                list = m;
                result = true;
            }
            
            return result;
        }
        
        @Override
        public boolean processResponse() {
            if(list == null) {
                return false;
            }
            
            String containerDesc = list.getMainAttributes().getValue("children"); // NOI18N
            if(containerDesc == null || containerDesc.length() == 0) {
                // no containers running...
                return true;
            }

            String [] containers = containerDesc.split(","); // NOI18N
            for(String container: containers) {
                if(skipContainer(container)) {
                    continue;
                }
                
                // get container attributes
                Attributes contAttr = list.getAttributes(container);
                String appDesc = contAttr.getValue("children"); // NOI18N

                // !PW XXX Do we want/need to show empty containers?
                if(appDesc == null) {
                    // no apps currently deployed in this container
                    continue;
                }
                
                String [] apps = appDesc.split(","); // NOI18N
                List<AppDesc> appList = new ArrayList<AppDesc>(apps.length);
                for(String app: apps) {
                    Attributes appAttr = list.getAttributes(app);
                    String name = appAttr.getValue("message"); // NOI18N
                    String path = appAttr.getValue("Source_value"); // NOI18N
                    if(path.startsWith("file:")) {
                        path = new String(path.substring(5));
                    }
                    appList.add(new AppDesc(name, path));
                }
                
                if(appMap == null) {
                    appMap = new HashMap<String, List<AppDesc>>();
                }
                
                appMap.put(container, appList);
            }
            
            return true;
        }
    
        /**
         * For skipping containers we don't care about.
         * 
         * @param container
         * @return
         */
        private boolean skipContainer(String currentContainer) {
            return container != null ? !container.equals(currentContainer) :
                "security_ContractProvider".equals(currentContainer);
        }
    
        
    };
    
    /**
     * Command to deploy a directory
     */
    public static final class DeployCommand extends ServerCommand {
        
        private final String path;
        private final String name;
        private final String contextRoot;
        
        public DeployCommand(final String path, final String name, final String contextRoot) {
            this.path = path;
            this.name = name;
            this.contextRoot = contextRoot;
        }
        
        @Override
        public String getCommand() { 
            StringBuilder cmd = new StringBuilder(128);
            cmd.append("deploy?path="); // NOI18N
            cmd.append(path);
            if(name != null && name.length() > 0) {
                cmd.append("?name="); // NOI18N
                cmd.append(name);
            }
            if(contextRoot != null && contextRoot.length() > 0) {
                cmd.append("?contextroot="); // NOI18N
                cmd.append(contextRoot);
            }
            cmd.append("?force=true");
            return cmd.toString();
        } 
        
    }
    
    /**
     * Command to redeploy a directory deployed app that is already deployed.
     */
    public static final class RedeployCommand extends ServerCommand {
        
        private final String name;
        private final String contextRoot;
        
        public RedeployCommand(final String name, final String contextRoot) {
            this.name = name;
            this.contextRoot = contextRoot;
        }
        
        @Override
        public String getCommand() { 
            StringBuilder cmd = new StringBuilder(128);
            cmd.append("redeploy?name="); // NOI18N
            cmd.append(name);
            if(contextRoot != null && contextRoot.length() > 0) {
                cmd.append("?contextroot="); // NOI18N
                cmd.append(contextRoot);
            }
            return cmd.toString();
        }
        
    }
    
    /**
     * Command to undeploy a deployed application.
     */
    public static final class UndeployCommand extends ServerCommand {
        
        private final String name;
        
        public UndeployCommand(final String name) {
            this.name = name;
        }
        
        @Override
        public String getCommand() { 
            return "undeploy?name=" + name; // NOI18N
        }
        
    }
    
}
