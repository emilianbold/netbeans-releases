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
package org.netbeans.modules.web.webkit.debugging.api.network;

import org.json.simple.JSONObject;
import org.netbeans.modules.web.webkit.debugging.LiveHTML;
import org.netbeans.modules.web.webkit.debugging.TransportHelper;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class Network {
    private TransportHelper transport;
    private boolean enabled;
    private Callback callback;
    private WebKitDebugging webKit;
    private int numberOfClients = 0;
    private boolean inLiveHTMLMode = false;
    
    public Network(TransportHelper transport, WebKitDebugging webKit) {
        this.transport = transport;
        this.callback = new Callback();
        this.transport.addListener(callback);
        this.webKit = webKit;
    }

    public void enable() {
        numberOfClients++;
        if (!enabled) {
            enabled = true;
            transport.sendBlockingCommand(new Command("Network.enable"));
        }
        inLiveHTMLMode = webKit.getDebugger().isInLiveHTMLMode();
    }

    public void disable() {
        assert numberOfClients > 0;
        numberOfClients--;
        if (numberOfClients == 0) {
            transport.sendCommand(new Command("Network.disable"));
            enabled = false;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
    
    private String getReponseBody(String requestId) {
        JSONObject params = new JSONObject();
        params.put("requestId", requestId);
        Response res = transport.sendBlockingCommand(new Command("Network.getResponseBody", params));
        String data = (String)res.getResult().get("body");
        return data;
    }
    
    private void recordDataEvent(long timeStamp, String id, String request, String mime) {
        assert inLiveHTMLMode;
        // TODO: fetch request here as well
        String response = getReponseBody(id);
        LiveHTML.getDefault().storeDataEvent(transport.getConnectionURL(), timeStamp, response, request, mime);
    }

    private class Callback implements ResponseCallback {

        @Override
        public void handleResponse(Response response) {
            if ("Network.responseReceived".equals(response.getMethod())) {
                if (inLiveHTMLMode && 
                        "XHR".equals(response.getParams().get("type"))) {
                    final long timeStamp = System.currentTimeMillis();
                    final String id = (String)response.getParams().get("requestId");
                    final String request = (String)((JSONObject)response.getParams().get("response")).get("url");
                    final String mime = (String)((JSONObject)response.getParams().get("response")).get("mimeType");
                    transport.getRequestProcessor().post(new Runnable() {
                        @Override
                        public void run() {
                            recordDataEvent(timeStamp, id, request, mime);
                        }
                    });
                }
//            } else if ("Network.dataReceived".equals(response.getMethod())) {
            }
        }
        
    }
    
    
}
