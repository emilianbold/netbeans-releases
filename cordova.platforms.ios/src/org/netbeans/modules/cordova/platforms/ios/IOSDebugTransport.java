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
import com.dd.plist.BinaryPropertyListParser;
import com.dd.plist.BinaryPropertyListWriter;
import com.dd.plist.NSData;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.XMLPropertyListParser;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.netbeans.modules.cordova.platforms.MobileDebugTransport;
import org.netbeans.modules.netserver.api.WebSocketClient;
import org.netbeans.modules.netserver.api.WebSocketReadHandler;
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
public class IOSDebugTransport extends MobileDebugTransport implements TransportImplementation {
    
    private final RequestProcessor RP = new RequestProcessor(IOSDebugTransport.class);
    private Socket socket;
    private ByteArrayOutputStream buf = new ByteArrayOutputStream();
    private final static String LOCALHOST_IPV6 = "::1";
    private final static int port = 27753;
    private RequestProcessor.Task socketListener;
    private volatile boolean keepGoing = true;
    private ResponseCallback callBack;
    private Tabs tabs;

    public IOSDebugTransport() {
    }
    
    @Override
    public boolean attach() {
        try {
            init();
            socketListener = RP.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        InputStream is = socket.getInputStream();
                        while (keepGoing) {
                            try {
                                process(is);
                            } catch (Exception exception) {
                                Exceptions.printStackTrace(exception);
                            }
                        }
                    } catch (IOException e) {
                        Exceptions.printStackTrace(e);
                    }
                }});
            return true;
        }  catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }

    public static void runWhenReady(Runnable run, long timeout) {
        long time;
        long started = System.currentTimeMillis();
        do {
            try {
                //Socket socket = new Socket(LOCALHOST_IPV6, port);
                //socket.close();
                Thread.sleep(5000);
                run.run();
            //} catch (UnknownHostException ex) {
            //    Exceptions.printStackTrace(ex);
            //} catch (IOException ex) {
            //    Exceptions.printStackTrace(ex);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            time = System.currentTimeMillis() - started;
        } while (time < timeout);
        
    }
    @Override
    public boolean detach() {
        stop();
        return true;
    }

    @Override
    public void sendCommand(Command command) {
        try {
            sendCommand(command.getCommand());
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void registerResponseCallback(ResponseCallback callback) {
        this.callBack = callback;
    }

    private String getCommand(String name) {
        try {
            Properties props = new Properties();
            props.load(IOSDebugTransport.class.getResourceAsStream("Command.properties"));
            return props.getProperty(name).replace("$bundleId", "com.apple.mobilesafari").replace("$tabIdentifier", tabs.getActive());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String createJSONCommand(JSONObject command) throws IOException {
        String json = command.toString();
        String s = Base64.encodeBytes(json.getBytes());
        String res = getCommand("sendJSONCommand").replace("$json_encoded", s);
        //System.out.println("sending " + res);
        return res;
    }

    public byte[] plistXmlToBinary(String msg) throws Exception {
        NSObject object = XMLPropertyListParser.parse(msg.getBytes());
        return BinaryPropertyListWriter.writeToArray(object);

    }

    public void init() throws Exception {
        if (socket != null && (socket.isConnected() || !socket.isClosed())) {
            socket.close();
        }
        tabs = new Tabs();
        socket = new Socket(LOCALHOST_IPV6, port);
        sendCommand(getCommand("setConnectionKey"));
        sendCommand(getCommand("connectToApp"));
        sendCommand(getCommand("setSenderKey"));
    }

    public void sendCommand(JSONObject command) throws Exception {
        sendBinaryMessage(plistXmlToBinary(createJSONCommand(command)));
    }

    private void sendCommand(String command) throws Exception {
        //System.out.println("sending " + command);
        sendBinaryMessage(plistXmlToBinary(command));
    }

    private void sendBinaryMessage(byte[] bytes) throws IOException {
        OutputStream os = socket.getOutputStream();
        byte[] lenght = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(bytes.length).array();
        os.write(lenght);
        os.write(bytes);
    }

    private void process(InputStream is) throws Exception {
        byte sizeBuffer[] = new byte[4];
        int count = is.read(sizeBuffer);
        while (count < 4) {
            count += is.read(sizeBuffer, count, 4 - count);
        }
        int size = ByteBuffer.wrap(sizeBuffer, 0, 4).getInt();
        byte[] content = new byte[size];
        count = is.read(content);
        while (count < size) {
            count += is.read(content, count, size - count);
        }
        assert count == size;

        NSObject object = BinaryPropertyListParser.parse(content);

        String message = object.toXMLPropertyList();
        //System.out.println("receiving " + object.toXMLPropertyList());
        JSONObject jmessage = extractResponse(object);
        if (jmessage != null) {
            callBack.handleResponse(new Response(jmessage));
        } else {
            tabs.update(object);
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
        NSDictionary argument = (NSDictionary) root.objectForKey("__argument");
        if (argument == null) {
            return null;
        }
        NSData data = (NSData) argument.objectForKey("WIRMessageDataKey");
        if (data == null) {
            return null;
        }
        byte[] bytes = data.bytes();
        String s = new String(bytes);
        JSONObject o = (JSONObject) JSONValue.parseWithException(s);
        return o;
    }

    public static InputStream fromString(String str) {
        try {
            byte[] bytes = str.getBytes("UTF-8");
            return new ByteArrayInputStream(bytes);
        } catch (UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    public void stop() {
        try {
            socket.close();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        keepGoing = false;
        if (socketListener != null) {
            socketListener.cancel();
        }
        keepGoing = true;

    }

    @Override
    public String getConnectionName() {
        return "iOS";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public WebSocketClient createWebSocket(WebSocketReadHandler handler) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    class Tabs {

        private HashMap<String, TabDescriptor> map = new HashMap();

        public void update(NSObject r) throws Exception {
            if (r ==  null) {
                return;
            }
            if (!(r instanceof NSDictionary)) {
                return;
            }
            NSDictionary root = (NSDictionary) r;
            NSDictionary argument = (NSDictionary) root.objectForKey("__argument");
            if (argument == null) {
                return;
            }
            NSDictionary listing = (NSDictionary) argument.objectForKey("WIRListingKey");
            if (listing == null) {
                return;
            }
            map.clear();
            for (String s : listing.allKeys()) {
                NSDictionary o = (NSDictionary) listing.objectForKey(s);
                NSObject identifier = o.objectForKey("WIRPageIdentifierKey");
                NSObject url = o.objectForKey("WIRURLKey");
                NSObject title = o.objectForKey("WIRTitleKey");
                map.put(s, new TabDescriptor(url.toString(), title.toString(), identifier.toString()));
            }
        }

        public TabDescriptor get(String key) {
            return map.get(key);
        }

        private CharSequence getActive() {
            for (Map.Entry<String, TabDescriptor> entry: map.entrySet()) {
                String urlFromBrowser = entry.getValue().getUrl();
                int hash = urlFromBrowser.indexOf("#");
                if (hash != -1) {
                    urlFromBrowser = urlFromBrowser.substring(0, hash); 
                }
                if (urlFromBrowser.endsWith("/")) {
                    urlFromBrowser = urlFromBrowser.substring(0, urlFromBrowser.length()-1); 
                }
                
                if (getConnectionURL().toString().equals(urlFromBrowser)) {
                    return entry.getKey();
                }                        
            }
            return "1";
        }

        public class TabDescriptor {

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
                return "TabDescriptor{" + "url=" + url + ", title=" + title + ", identifier=" + identifier + '}';
            }
        }
    }
}
         
