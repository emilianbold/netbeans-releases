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
package org.netbeans.modules.cordova.platforms;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;
import java.util.Enumeration;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.netbeans.modules.netserver.api.WebSocketClient;
import org.netbeans.modules.netserver.api.WebSocketReadHandler;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;
import org.netbeans.modules.web.webkit.debugging.spi.TransportImplementation;
import org.netbeans.modules.web.webkit.debugging.spi.netbeansdebugger.NetBeansJavaScriptDebuggerFactory;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Becicka
 */
public abstract class MobileDebugTransport implements TransportImplementation, WebSocketReadHandler {

    private WebSocketClient webSocket;
    private ResponseCallback callBack;
    private String indexHtmlLocation;
    
    @Override
    public boolean attach() {
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
    public boolean detach() {
        webSocket.stop();
        return true;
    }

    @Override
    public void sendCommand(Command command) {
        String toString = translate(command.toString());
        webSocket.sendMessage(toString);
    }

    @Override
    public void registerResponseCallback(ResponseCallback callback) {
        this.callBack = callback;
    }

    @Override
    public URL getConnectionURL() {
        try {
            return new URL(indexHtmlLocation);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
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
            callBack.handleResponse(new Response((JSONObject) parse));
        } catch (ParseException ex) {
            Exceptions.attachMessage(ex, string);
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void closed(SelectionKey key) {
        Lookup.getDefault().lookup(BuildPerformer.class).stopDebugging();
    }


    public abstract WebSocketClient createWebSocket(WebSocketReadHandler handler) throws IOException;

    public String translate(String toString) {
        //TODO: hack to workaround #221791
        return toString.replaceAll("localhost", getLocalhostInetAddress().getHostAddress());
    }

    public void setBaseUrl(String documentURL) {
        this.indexHtmlLocation = documentURL;
    }
    
    /**
     * Returns IP address of localhost in local network
     * @return 
     */
    public static InetAddress getLocalhostInetAddress() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            if (!localHost.isLoopbackAddress()) {
                return localHost;
            }
            //workaround for strange behavior on debian, see #226087
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                final NetworkInterface netInterface = networkInterfaces.nextElement();
                if (netInterface.isUp()) {
                    Enumeration<InetAddress> inetAddresses = netInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress nextElement = inetAddresses.nextElement();
                        if (!nextElement.isLoopbackAddress() && nextElement.isSiteLocalAddress()) {
                            return nextElement;
                        }

                    }
                }
            }
            return localHost;
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }    
    
}
