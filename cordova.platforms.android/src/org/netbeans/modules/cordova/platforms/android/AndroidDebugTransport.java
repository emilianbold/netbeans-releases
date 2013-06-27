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
package org.netbeans.modules.cordova.platforms.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.modules.cordova.platforms.spi.MobileDebugTransport;
import org.netbeans.modules.cordova.platforms.api.PlatformManager;
import org.netbeans.modules.cordova.platforms.api.ProcessUtilities;
import org.netbeans.modules.cordova.platforms.api.WebKitDebuggingSupport;
import org.netbeans.modules.netserver.api.ProtocolDraft;
import org.netbeans.modules.netserver.api.WebSocketClient;
import org.netbeans.modules.netserver.api.WebSocketReadHandler;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Becicka
 */
public class AndroidDebugTransport extends MobileDebugTransport implements WebSocketReadHandler {

    private WebSocketClient webSocket;
    private static final Logger LOGGER = Logger.getLogger(AndroidDebugTransport.class.getName());

    @Override
    public boolean detach() {
        if (webSocket != null) {
            webSocket.stop();
        }
        return true;
    }

    @Override
    public void sendCommandImpl(Command command) {
        String toString = translate(command.toString());
        webSocket.sendMessage(toString);
    }
    
    @Override
    public void accepted(SelectionKey key) {
        synchronized(webSocket) {
            webSocket.notifyAll();
        }
    }

    @Override
    public void read(SelectionKey key, byte[] message, Integer dataType) {
        final String string;
        string = new String(message, Charset.forName("UTF-8")).trim(); //NOI18N
        try {
            final Object parse = JSONValue.parseWithException(string);
            if (callBack == null) {
                LOGGER.info("callBack is null. Ignoring response: " + string);
            } else {
                callBack.handleResponse(new Response((JSONObject) parse));
            }
        } catch (ParseException ex) {
            Exceptions.attachMessage(ex, string);
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void closed(SelectionKey key) {
        WebKitDebuggingSupport.getDefault().stopDebugging(false);
    }

    public String getConnectionName() {
        return "Android"; //NOI18N
    }

    public WebSocketClient createWebSocket(WebSocketReadHandler handler) throws IOException {
        return new WebSocketClient(getURI(), ProtocolDraft.getRFC(), handler);
    }

    @Override
    public boolean attach() {
        try {
            String s = ProcessUtilities.callProcess(
                    ((AndroidPlatform) AndroidPlatform.getDefault()).getAdbCommand(), 
                    true, 
                    AndroidPlatform.DEFAULT_TIMEOUT, 
                    "forward", // NOI18N
                    "tcp:9222", // NOI18N
                    "localabstract:chrome_devtools_remote"); //NOI18N
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        for (long stop = System.nanoTime() + TimeUnit.MINUTES.toNanos(2); stop > System.nanoTime();) {
            try {
                Socket socket = new Socket("localhost", 9222); // NOI18N
                break;
            } catch (java.net.ConnectException ex) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex1) {
                    Exceptions.printStackTrace(ex1);
                }
                continue;
            } catch (UnknownHostException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        try {
            webSocket = createWebSocket(this);
            webSocket.start();
            return true;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return false;
        }
    }
    
    

    @Override
    public String getVersion() {
        return "1.0"; //NOI18N
    }

    private URI getURI() {
        JSONArray array;
        try {
            JSONParser parser = new JSONParser();

            URL oracle = new URL("http://localhost:9222/json");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(oracle.openStream()))) {
                Object obj = parser.parse(reader);
                array = (JSONArray) obj;
                if (array.size()==0) {
                    try (BufferedReader r = new BufferedReader(new InputStreamReader(oracle.openStream()))) {
                        while (r.ready()) {
                            LOGGER.info(r.readLine());
                        }
                    }
                }
                for (int i = 0; i < array.size(); i++) {
                    JSONObject object = (JSONObject) array.get(i);
                    String urlFromBrowser = object.get("url").toString(); // NOI18N
                    int hash = urlFromBrowser.indexOf("#"); // NOI18N
                    if (hash != -1) {
                        urlFromBrowser = urlFromBrowser.substring(0, hash);
                    }
                    if (urlFromBrowser.endsWith("/")) { // NOI18N
                        urlFromBrowser = urlFromBrowser.substring(0, urlFromBrowser.length() - 1);
                    }
                    final String connectionUrl = getConnectionURL().toExternalForm();
                    final String shortenedUrl = connectionUrl.replace(":80/", "/"); // NOI18N

                    if (connectionUrl.equals(urlFromBrowser) || shortenedUrl.equals(urlFromBrowser)) {
                        return new URI(object.get("webSocketDebuggerUrl").toString()); // NOI18N
                    }
                }
            }
        } catch (IOException | ParseException | URISyntaxException ex) {
            throw new IllegalStateException("Cannot get websocket address", ex); // NOI18N
        }
        LOGGER.info(array.toJSONString());
        throw new IllegalStateException("Cannot get websocket address"); // NOI18N
    }

}
