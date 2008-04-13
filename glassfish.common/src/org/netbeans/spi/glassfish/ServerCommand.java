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

package org.netbeans.spi.glassfish;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Abstraction of commands for V3 server administration
 *
 * @author Peter Williams
 */
public abstract class ServerCommand {

    public ServerCommand() {
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
     * If the response for this command is in Manifest format (most or all
     * server commands use this), then override {@link #readManifest(Manifest)} 
     * instead.
     * <br>&nbsp;<br>
     * Override to read the response data sent by the server.  Do not close
     * the stream parameter when finished.  Caller will take care of that.
     * 
     * @param in Stream to read data from.
     * @return true if response was read correctly.
     * @throws java.io.IOException in case of stream error.
     */
    public boolean readResponse(InputStream in) throws IOException {
        boolean result = false;

        Manifest m = new Manifest();
        m.read(in);
        String outputCode = m.getMainAttributes().getValue("exit-code"); // NOI18N
        if(outputCode.equalsIgnoreCase("Success")) { // NOI18N
            readManifest(m);
            result = true;
        } else {
            // !PW FIXME Need to pass this message back.  Need <Result> object?
            String message = m.getMainAttributes().getValue("message"); // NOI18N
            Logger.getLogger("glassfish").log(Level.WARNING, message);
        }

        return result;
    }
    
    /**
     * Override to interpret the manifest result returned from the server.
     * This method is only called if the manifest is successfully read and 
     * the exit-code field indicates the command was successful.
     * 
     * @param manifest Result returned by the server for this command in
     * manifest format.  The actual fields present depend on the command sent.
     * 
     * @throws java.io.IOException
     */
    public void readManifest(Manifest manifest) throws IOException {
    }
    
    /**
     * Override to parse, validate, and/or format any data read from the 
     * server in readResponse().
     * 
     * @return true if data was processed correctly.
     */
    public boolean processResponse() {
        return true;
    }
    
    /**
     * Command string for this command.
     * 
     * @return Command string for this command.
     */
    @Override
    public String toString() {
        return getCommand();
    }
    
    /**
     * Command to list resources of various types currently available on the server.
     */
    public static final class ListResourcesCommand extends ServerCommand {

        private final String command;
        private Manifest list;
        private List<ResourceDesc> resList;

        public ListResourcesCommand(String resourceCommandSuffix) {
            command = "list-" + resourceCommandSuffix + "s"; // NOI18N
        }
        
        public List<ResourceDesc> getResourceList() {
            if(resList != null) {
                return Collections.unmodifiableList(resList);
            } else {
                return Collections.emptyList();
            }
        }
        
        @Override
        public String getCommand() { 
            return command;
        }
        
        @Override
        public void readManifest(Manifest manifest) throws IOException {
            list = manifest;
        }
        
        @Override
        public boolean processResponse() {
            if(list == null) {
                return false;
            }
            
            String resourceList = list.getMainAttributes().getValue("children"); // NOI18N
            if(resourceList == null || resourceList.length() == 0) {
                // no resources running...
                return true;
            }

            String [] resources = resourceList.split("[,;]"); // NOI18N
            for(String r: resources) {
                if(skipResource(r)) {
                    continue;
                }

                // get container attributes
                Attributes resourceAttr = list.getAttributes(r);
                if(resourceAttr != null) {
                    String name = resourceAttr.getValue("message"); // NOI18N

                    if(name != null && name.length() > 0) {
                        if(resList == null) {
                            resList = new ArrayList<ResourceDesc>();
                        }

                        resList.add(new ResourceDesc(name));
                    }
                } else {
                    Logger.getLogger("glassfish").log(Level.FINE, "No resource attributes returned for " + r);
                }
            }
            
            return true;
        }

        private boolean skipResource(String r) {
            return false;
        }
    
    };
}
