/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cordova.platforms.ios;

import com.dd.plist.Base64;
import com.dd.plist.BinaryPropertyListWriter;
import com.dd.plist.NSData;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.NSString;
import com.dd.plist.XMLPropertyListParser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.netbeans.modules.cordova.platforms.spi.MobileDebugTransport;
import org.netbeans.modules.cordova.platforms.api.WebKitDebuggingSupport;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;
import org.netbeans.modules.web.webkit.debugging.spi.TransportImplementation;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jan Becicka
 */
public abstract class IOSDebugTransport extends MobileDebugTransport implements TransportImplementation {
    
    private final RequestProcessor RP = new RequestProcessor(IOSDebugTransport.class);
    private RequestProcessor.Task socketListener;
    protected volatile boolean keepGoing = true;
    private Tabs tabs = new IOSDebugTransport.Tabs();
    private final Object init = new Object();
    private static final Logger LOGGER = Logger.getLogger(IOSDebugTransport.class.getName());
    
    

    public IOSDebugTransport() {
        setBundleIdentifier("com.apple.mobilesafari"); // NOI18N
    }
    
    @Override
    public boolean attach() {
        try {
            init();
            socketListener = RP.post(new Runnable() {
                @Override
                public void run() {
                    while (keepGoing) {
                        try {
                            process();
                        } catch (SocketException e) {
                            Logger.getLogger(IOSDebugTransport.class.getName()).log(Level.FINE, "Debugging Connection Closed", e);
                            return;
                        } catch (Exception exception) {
                            Exceptions.printStackTrace(exception);
                        }
                    }
                }
            });
            sendInitCommands();

            return true;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }
    
    private void process() throws Exception {
        NSObject object = readData();
        if (object == null) {
            return;
        }

        if ((object instanceof NSDictionary)) {
            NSString selector = (NSString) ((NSDictionary) object).objectForKey("__selector"); // NOI18N
            if (selector!=null && selector.toString().equals("_rpc_applicationConnected:")) { // NOI18N
                synchronized(init) {
                    init.notify();
                }
            }
            
        }
        //System.out.println("receiving " + object.toXMLPropertyList());
        JSONObject jmessage = extractResponse(object);
        if (jmessage != null) {
            if (callBack == null) {
                LOGGER.info("callBack is null. Ignoring response: " + jmessage.toString());
            } else {
                callBack.handleResponse(new Response(jmessage));
            }
        } else {
            if (!tabs.update(object)) {
                checkClose(object);
            }
        }
    }
    
    protected abstract NSObject readData() throws Exception;
    
    private String getCommand(String name, boolean replace) {
        try {
            Properties props = new Properties();
            props.load(IOSDebugTransport.class.getResourceAsStream("Command.properties"));
            final String cmd = props.getProperty(name).replace("$bundleId", getBundleIdentifier());
            if (!replace) {
                return cmd;
            }
            return cmd.replace("$tabIdentifier", getBundleIdentifier().equals("com.apple.mobilesafari")?tabs.getActive():"1"); // NOI18N
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected final String createJSONCommand(JSONObject command) throws IOException {
        String json = translate(command.toString());
        String s = Base64.encodeBytes(json.getBytes());
        String res = getCommand("sendJSONCommand", true).replace("$json_encoded", s); // NOI18N
        return res;
    }

    protected final byte[] plistXmlToBinary(String msg) throws Exception {
        NSObject object = XMLPropertyListParser.parse(msg.getBytes());
        return BinaryPropertyListWriter.writeToArray(object);

    }
    
    private void checkClose(NSObject r) throws Exception {
        if (r == null) {
            return;
        }
        if (!(r instanceof NSDictionary)) {
            return;
        }
        NSDictionary root = (NSDictionary) r;
        NSString selector = (NSString) root.objectForKey("__selector"); // NOI18N
        if (selector != null) {
            if ("_rpc_reportConnectedApplicationList:".equals(selector.toString())) { // NOI18N
                NSDictionary argument = (NSDictionary) root.objectForKey("__argument"); // NOI18N
                if (argument == null) {
                    return;
                }
                NSDictionary applications = (NSDictionary) argument.objectForKey("WIRApplicationDictionaryKey"); // NOI18N
                if (applications.count() == 0) {
                    WebKitDebuggingSupport.getDefault().stopDebugging(false);
                }

            } else if ("rpc_applicationDisconnected:".equals(selector.toString())) { // NOI18N
                NSDictionary argument = (NSDictionary) root.objectForKey("__argument"); // NOI18N
                if (argument == null) {
                    return;
                }
                NSDictionary applications = (NSDictionary) argument.objectForKey("WIRApplicationIdentifierKey"); // NOI18N
                if (applications.objectForKey("WIRApplicationIdentifierKey").toString().equals("com.apple.mobilesafari")) { // NOI18N
                    WebKitDebuggingSupport.getDefault().stopDebugging(false);
                }
            }
        }
    }
    
    

    private JSONObject extractResponse(NSObject r) throws Exception {
        if (r == null) {
            return null;
        }
        if (!(r instanceof NSDictionary)) {
            return null;
        }
        NSDictionary root = (NSDictionary) r;
        NSDictionary argument = (NSDictionary) root.objectForKey("__argument"); // NOI18N
        if (argument == null) {
            return null;
        }
        NSData data = (NSData) argument.objectForKey("WIRMessageDataKey"); // NOI18N
        if (data == null) {
            return null;
        }
        byte[] bytes = data.bytes();
        String s = new String(bytes);
        JSONObject o = (JSONObject) JSONValue.parseWithException(s);
        return o;
    }

    protected static InputStream fromString(String str) {
        try {
            byte[] bytes = str.getBytes("UTF-8"); // NOI18N
            return new ByteArrayInputStream(bytes);
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    protected void stop() {
        keepGoing = false;
        if (socketListener != null) {
            socketListener.cancel();
        }
    }

    @Override
    public boolean detach() {
        stop();
        return true;
    }

    @Override
    public final void sendCommandImpl(Command command) {
        try {
            sendCommand(command.getCommand());
        } catch (Exception ex) {
            boolean s = keepGoing;
            stop();
            if (s) {
                WebKitDebuggingSupport.getDefault().stopDebugging(false);
            }
        }
    }

    protected void sendInitCommands() throws Exception {
        sendCommand(getCommand("setConnectionKey", false)); // NOI18N
        if (getConnectionURL() == null && "iOS Simulator".equals(getConnectionName())) { // NOI18N
            //phonegap
            synchronized (init) {
                init.wait();
            }
        }
        sendCommand(getCommand("connectToApp", false)); // NOI18N
        sendCommand(getCommand("setSenderKey", true)); // NOI18N
    }
    
    protected abstract void sendCommand(String command) throws Exception;
    protected abstract void sendCommand(JSONObject command) throws Exception;

    protected abstract void init() throws Exception;

    private class Tabs {

        private HashMap<String, TabDescriptor> map = new HashMap();
        private Object monitor = new Object();
        private boolean inited = false;

        public boolean update(NSObject r) throws Exception {
            if (r ==  null) {
                return false;
            }
            if (!(r instanceof NSDictionary)) {
                return false;
            }
            NSDictionary root = (NSDictionary) r;
            NSDictionary argument = (NSDictionary) root.objectForKey("__argument"); // NOI18N
            if (argument == null) {
                return false;
            }
            NSDictionary listing = (NSDictionary) argument.objectForKey("WIRListingKey"); // NOI18N
            if (listing == null) {
                return false;
            }
            map.clear();
            for (String s : listing.allKeys()) {
                NSDictionary o = (NSDictionary) listing.objectForKey(s);
                NSObject identifier = o.objectForKey("WIRPageIdentifierKey"); // NOI18N
                NSObject url = o.objectForKey("WIRURLKey"); // NOI18N
                NSObject title = o.objectForKey("WIRTitleKey"); // NOI18N
                if (getConnectionURL()==null) {
                    //auto setup for phonegap. There is always on tab
                    setBaseUrl(url.toString());
                }
                map.put(s, new TabDescriptor(url.toString(), title.toString(), identifier.toString()));
            }
            synchronized (monitor) {
                inited = true;
                monitor.notifyAll();
            }
            
            if (getTabForUrl() == null) {
                WebKitDebuggingSupport.getDefault().stopDebugging(false);
            }
                   
            return true;
        }

        public TabDescriptor get(String key) {
            return map.get(key);
        }

        private String getActive() {
                synchronized(monitor) {
                    if (!inited) {
                        try {
                            monitor.wait(2*60*1000);
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            final String tabForUrl = getTabForUrl();
            if (tabForUrl !=null) {
                return tabForUrl;
            }
            return map.entrySet().iterator().next().getKey();
        }

        private String getTabForUrl() {
            for (Map.Entry<String, TabDescriptor> entry : map.entrySet()) {
                String urlFromBrowser = entry.getValue().getUrl();
                if (urlFromBrowser.startsWith("file:/")) { // NOI18N
                    //phonegap
                    return "1"; // NOI18N
                }
                int hash = urlFromBrowser.indexOf("#"); // NOI18N
                if (hash != -1) {
                    urlFromBrowser = urlFromBrowser.substring(0, hash); 
                }
                if (urlFromBrowser.endsWith("/")) { // NOI18N
                    urlFromBrowser = urlFromBrowser.substring(0, urlFromBrowser.length()-1); 
                }
                if (getConnectionURL().toString().equals(urlFromBrowser.replaceAll("file:///", "file:/"))) {
                    return entry.getKey();
                }                        
            }
            return null;
        }

        private class TabDescriptor {

            String url;
            String title;
            String identifier;

            public TabDescriptor(String url, String title, String identifier) {
                this.url = url;
                this.title = title;
                this.identifier = identifier;
            }

            public String getUrl() {
                return url;
            }

            public String getTitle() {
                return title;
            }

            public String getIdentifier() {
                return identifier;
            }

            @Override
            public String toString() {
                return "TabDescriptor{" + "url=" + url + ", title=" + title + ", identifier=" + identifier + '}'; // NOI18N
            }
        }
    }
}
         
