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

package org.netbeans.modules.glassfish.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Abstraction of commands for V3 server administration
 *
 * @author Peter Williams
 */
public abstract class ServerCommand {

    public static final char QUERY_SEPARATOR = '?'; // NOI18N
    public static final char PARAM_SEPARATOR = '&'; // NOI18N

    protected final String command;
    protected String query = null;
    protected boolean retry = false;
    private String serverMessage = "";
    protected boolean silentFailureAllowed = false;

    public String getServerMessage() {
        return serverMessage;
    }

    public ServerCommand(String command) {
        this.command = command;
    }
    
    /**
     * Returns server command represented by this object.  Set in constructor.
     * e.g. "deploy", "list-applications", etc.
     * 
     * @return command string represented by this object.
     */
    public String getCommand() {
        return command;
    }

    /**
     * Returns the query string for this command.  Set in constructor.
     * 
     * @return query string for this command.
     */
    public String getQuery() {
        return query;
    }

    /**
     * Override to change the type of HTTP method used for this command.
     * Default is GET.
     * 
     * @return HTTP method (GET, POST, etc.)
     */
    public String getRequestMethod() {
        return "GET"; // NOI18N
    }
    
    /**
     * Override and return true to send information to the server (HTTP POST).
     * Default is false.
     * 
     * @return true if using HTTP POST to send to server, false otherwise
     */
    public boolean getDoOutput() {
        return false;
    }
    
    /**
     * Override to set the content-type of information sent to the server.
     * Default is null (not set).
     * 
     * @return content-type of data sent to server via HTTP POST
     */
    public String getContentType() {
        return null;
    }
    
    /**
     * Override to provide a data stream for POST requests.  Data will be read
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
     * Override to provide a name for the data source whose inputstream is
     * returned by getInputStream.  Must not return null if getInputStream()
     * does not return null;
     *
     * @return the name to associate with the input stream
     */
    public String getInputName() {
        return null;
    }

    /**
     * Override to provide the lastModified date for data source whose
     * inputstream is returned by getInputStream.  Must not return null if
     * getInputStream() does not return null;
     *
     * @return String format of long integer from lastModified date of source.
     */
    public String getLastModified() {
        return null;
    }

    /**
     * Sometimes (e.g. during startup), the server does not accept commands.  In
     * such cases, it will block for 20 seconds and then return with the message
     * " V3 cannot process this command at this time, please wait".
     *
     * In such cases, we set a flag and have the option to reissue the command.
     *
     * @return true if server responded with it's "please wait" message.
     */
    public boolean retry() {
        return retry;
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
    public boolean readResponse(InputStream in, HttpURLConnection hconn) throws IOException {
        boolean result = false;

        Manifest m = new Manifest();
        m.read(in);
        String outputCode = m.getMainAttributes().getValue("exit-code"); // NOI18N
        if(null == outputCode || !"FAILURE".equals(outputCode.toUpperCase(Locale.ENGLISH))) { // NOI18N
            readManifest(m);
            result = true;
        } else {
            // !PW FIXME Need to pass this message back.  Need <Result> object?
            String message = m.getMainAttributes().getValue("message"); // NOI18N

            // If server is not currently available for processing commands,
            // set the retry flag.
            if(message != null && message.contains("please wait")) {
                retry = true;
            } else {
                serverMessage = null != message ? message.replaceAll("%%%EOL%%%", "\n") : "";
            }
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
     * server in readResponse() / readManifest().
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
        return (query == null) ? command : command + QUERY_SEPARATOR + query;
    }

    public String getSrc() {
        return "/__asadmin/";
    }

    public boolean acceptsGzip() {
        return false;
    }

    /**
     * Command to get property information for a dotted name.
     */
    public static final class GetPropertyCommand extends ServerCommand {

        private Manifest info;
        private Map<String,String> propertyMap;

        public GetPropertyCommand(final String property, boolean allowSilentFail) {
            super("get"); // NOI18N
            
            this.query = "pattern=" + property; // NOI18N
            this.propertyMap = new HashMap<String, String>();
            this.silentFailureAllowed = allowSilentFail;
        }

        public GetPropertyCommand(final String property) {
            this(property,false);
        }


        @Override
        public void readManifest(Manifest manifest) throws IOException {
            info = manifest;
        }

        @Override
        public boolean processResponse() {
            if(info == null) {
                Logger.getLogger("glassfish").log(Level.WARNING,
                        "info is null for GetProperty command with \"{0}\"",query); // NOI18N
                return false;
            }

            for (String key : info.getEntries().keySet()) {
                int equalsIndex = key.indexOf('=');
                if(equalsIndex >= 0) {
                    String keyPart = key.substring(0, equalsIndex);
                    String valuePart = key.substring(equalsIndex + 1);
                    Level logLevel = Level.WARNING;
                    try {
                        // around Sept. 2008... 3.x servers were double encoding their
                        // responces.  It appears that has stopped (See http://netbeans.org/bugzilla/show_bug.cgi?id=195015)
                        // The open question is, "When did 3.x stop doing the double encode?"
                        // since we don't know... this strategy will work for us
                        //   Belt and suspenders, like
                        propertyMap.put(keyPart,valuePart); // raw form
                        propertyMap.put(keyPart, URLDecoder.decode(valuePart, "UTF-8")); // single decode
                        //
                        // If this next application of decoding generates an exception,
                        // don't make a big deal of it.
                        //
                        logLevel = Level.FINE;
                        propertyMap.put(keyPart, URLDecoder.decode(propertyMap.get(keyPart),"UTF-8"));
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger("glassfish").log(Level.WARNING,  // NOI18N
                                "UnsupportedEncodingException for value \"{0}\"",  // NOI18N
                                propertyMap.get(keyPart));
                    } catch (IllegalArgumentException iae) {
                        Logger.getLogger("glassfish").log(logLevel,  // NOI18N
                                "IllegalArgumentException for value \"{0}\" : inserted into propertyMap as \"{1}\"",  // NOI18N
                                new Object[] { valuePart, propertyMap.get(keyPart) });
                    }
                } else {
                        Logger.getLogger("glassfish").log(Level.WARNING,  // NOI18N
                                "Inserting empty string as value for key=\"{0}\"",key); // NOI18N
                    propertyMap.put(key, "");
                }
            }

            return true;
        }

        public Map<String, String> getData() {
            return propertyMap;
        }
    }

    /**
     * Command to set the value of a dotted name property.
     */
    public static final class SetPropertyCommand extends ServerCommand {

        private Manifest info;

        public SetPropertyCommand(final String property, final String value, String format) {
            super("set"); // NOI18N
            query = MessageFormat.format(format, property,value); 
        }

        @Override
        public void readManifest(Manifest manifest) throws IOException {
            info = manifest;
        }

        @Override
        public boolean processResponse() {
            if(info == null) {
                return false;
            }

            return true;
        }
    }

    /** Allow commands to fail without a big message
     *
     */
     public boolean isSilentFailureAllowed() {
         return silentFailureAllowed;
     }
}
