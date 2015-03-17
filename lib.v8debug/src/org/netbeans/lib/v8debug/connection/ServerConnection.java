/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */

package org.netbeans.lib.v8debug.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.netbeans.lib.v8debug.JSONReader;
import org.netbeans.lib.v8debug.JSONWriter;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Event;
import org.netbeans.lib.v8debug.V8Request;
import org.netbeans.lib.v8debug.V8Response;
import static org.netbeans.lib.v8debug.connection.DebuggerConnection.*;

/**
 *
 * @author Martin Entlicher
 */
public final class ServerConnection {
    
    private static final Logger LOG = Logger.getLogger(ServerConnection.class.getName());
    
    private static final String SERVER_PROTOCOL_VERSION = "1";
    
    private final ServerSocket server;
    private Socket currentSocket;
    private InputStream clientIn;
    private OutputStream clientOut;
    private final Object outLock = new Object();
    private final byte[] buffer = new byte[BUFFER_SIZE];
    private final ContainerFactory containerFactory = new LinkedJSONContainterFactory();
    private final Set<IOListener> ioListeners = new CopyOnWriteArraySet<>();
    
    public ServerConnection() throws IOException {
        server = new ServerSocket(0);
    }
    
    public ServerConnection(int serverPort) throws IOException {
        server = new ServerSocket(serverPort);
    }
    
    public void runConnectionLoop(Map<String, String> properties, Listener listener) throws IOException {
        Socket socket = server.accept();
        socket.setTcpNoDelay(true);
        currentSocket = socket;
        clientIn = socket.getInputStream();
        clientOut = socket.getOutputStream();
        sendProperties(properties);
        runEventLoop(listener);
    }
    
    public int getPort() {
        return server.getLocalPort();
    }
    
    private void runEventLoop(Listener listener) throws IOException {
        int n;
        int contentLength = -1;
        int[] beginPos = new int[] { 0 };
        int[] fromPtr = new int[] { 0 };
        int readOffset = 0;
        String tools = null;
        StringBuilder message = new StringBuilder();
        while ((n = clientIn.read(buffer, readOffset, BUFFER_SIZE - readOffset)) > 0) {
            n += readOffset;
            int from = 0;
            do {
                if (contentLength < 0) {
                    fromPtr[0] = from;
                    
                    contentLength = readContentLength(buffer, fromPtr, n, beginPos);
                    if (contentLength < 0) {
                        break;
                    }
                    from = fromPtr[0];
                }
                if (tools == null) {
                    fromPtr[0] = from;
                    tools = readTools(buffer, fromPtr, n);
                    if (tools == null) {
                        break;
                    } else {
                        from = fromPtr[0];
                    }
                }
                int length = Math.min(contentLength - message.length(), n - from);
                message.append(new String(buffer, from, length, CHAR_SET));
                from += length;
                if (message.length() == contentLength) {
                    try {
                        received(listener, tools, message.toString());
                    } catch (ThreadDeath td) {
                        throw td;
                    } catch (ParseException pex) {
                        throw new IOException(pex.getLocalizedMessage(), pex);
                    } catch (Throwable t) {
                        LOG.log(Level.SEVERE, message.toString(), t);
                    }
                    contentLength = -1;
                    tools = null;
                    message.delete(0, message.length());
                }
            } while (from < n);
            if (from < n) {
                System.arraycopy(buffer, from, buffer, 0, n - from);
                readOffset = n - from;
            } else {
                readOffset = 0;
            }
        }
        
    }
    
    private void received(Listener listener, String tools, String message) throws ParseException, IOException {
        //System.out.println("RECEIVED: tools: '"+tools+"', message: '"+message+"'");
        fireReceived(message);
        LOG.log(Level.FINE, "RECEIVED: {0}, {1}", new Object[]{tools, message});
        if (message.isEmpty()) {
            return ;
        }
        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject) parser.parse(message, containerFactory);
        V8Request request = JSONReader.getRequest(obj);
        ResponseProvider rp = listener.request(request);
        if (V8Command.Disconnect.equals(request.getCommand())) {
            try {
                closeCurrentConnection();
            } catch (IOException ioex) {}
        }
        if (rp != null) {
            rp.sendTo(this);
        }
    }
    
    private void sendProperties(Map<String, String> properties) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> prop : properties.entrySet()) {
            sb.append(prop.getKey());
            sb.append(": ");
            sb.append(prop.getValue());
            sb.append(EOL_STR);
        }
        if (!properties.keySet().contains(HeaderProperties.PROTOCOL_VERSION)) {
            sb.append(HeaderProperties.PROTOCOL_VERSION +
                      ": "+SERVER_PROTOCOL_VERSION +
                      EOL_STR);
        }
        sb.append(CONTENT_LENGTH_STR+"0" + EOL_STR + EOL_STR);
        byte[] bytes = sb.toString().getBytes(CHAR_SET);
        synchronized (outLock) {
            clientOut.write(bytes);
        }
    }
    
    public void send(V8Response response) throws IOException {
        JSONObject obj = JSONWriter.store(response);
        sendJSON(obj);
    }
    
    public void send(V8Event event) throws IOException {
        JSONObject obj = JSONWriter.store(event);
        sendJSON(obj);
    }
    
    private void sendJSON(JSONObject obj) throws IOException {
        String text = obj.toJSONString();
        text = text.replace("\\/", "/"); // Replace escaped slash "\/" with shash "/". Unescape slashes.
        //System.out.println("SEND: "+text);
        fireSent(text);
        LOG.log(Level.FINE, "SEND: {0}", text);
        byte[] bytes = text.getBytes(CHAR_SET);
        String contentLength = CONTENT_LENGTH_STR+bytes.length + EOL_STR + EOL_STR;
        synchronized (outLock) {
            clientOut.write(contentLength.getBytes(CHAR_SET));
            clientOut.write(bytes);
        }
    }
    
    public void closeCurrentConnection() throws IOException {
        if (currentSocket != null) {
            currentSocket.close();
            currentSocket = null;
        }
    }

    public void closeServer() throws IOException {
        if (server != null) {
            server.close();
        }
        fireClosed();
    }
    
    public void addIOListener(IOListener iol) {
        ioListeners.add(iol);
    }
    
    public void removeIOListener(IOListener iol) {
        ioListeners.remove(iol);
    }
    
    private void fireSent(String str) {
        for (IOListener iol : ioListeners) {
            iol.sent(str);
        }
    }
    
    private void fireReceived(String str) {
        for (IOListener iol : ioListeners) {
            iol.received(str);
        }
    }
    
    private void fireClosed() {
        for (IOListener iol : ioListeners) {
            iol.closed();
        }
    }
    
    public interface Listener {
        
        ResponseProvider request(V8Request request);
        
    }
    
    public static final class ResponseProvider {
        
        private V8Response response;
        private ServerConnection sc;
        
        private ResponseProvider(V8Response response) {
            this.response = response;
        }
        
        public static ResponseProvider create(V8Response response) {
            return new ResponseProvider(response);
        }
        
        public static ResponseProvider createLazy() {
            return new ResponseProvider(null);
        }
        
        public void setResponse(V8Response response) throws IOException {
            ServerConnection sc;
            synchronized (this) {
                if (this.response != null) {
                    throw new IllegalStateException("Response has been set already.");
                }
                this.response = response;
                sc = this.sc;
            }
            if (sc != null) {
                sc.send(response);
            }
        }
        
        void sendTo(ServerConnection sc) throws IOException {
            V8Response response;
            synchronized (this) {
                response = this.response;
                this.sc = sc;
            }
            if (response != null) {
                sc.send(response);
            }
        }
    }
}
