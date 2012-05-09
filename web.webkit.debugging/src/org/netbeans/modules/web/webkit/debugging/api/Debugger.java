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
package org.netbeans.modules.web.webkit.debugging.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.netbeans.modules.web.webkit.debugging.APIFactory;
import org.netbeans.modules.web.webkit.debugging.TransportHelper;
import org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.modules.web.webkit.debugging.api.debugger.Script;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;
import org.openide.util.Exceptions;

/**
 * See Debugger section of WebKit Remote Debugging Protocol for more details.
 */
public final class Debugger {

    private TransportHelper transport;
    private boolean enabled = false;
    private boolean suspended = false;
    private Callback callback;
    private List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
    private Map<String, Script> scripts = new HashMap<String, Script>();
    private WebKitDebugging webkit;
    private List<CallFrame> currentCallStack = new ArrayList<CallFrame>();
    private List<Breakpoint> currentBreakpoints = new ArrayList<Breakpoint>();

    Debugger(TransportHelper transport, WebKitDebugging webkit) {
        this.transport = transport;
        this.webkit = webkit;
        this.callback = new Callback();
        this.transport.addListener(callback);
    }
    
    public boolean enable() {
        Response response = transport.sendBlockingCommand(new Command("Debugger.enable"));
        // TODO: what will response contain if there was an error enabling debuggger in the page?
        enabled = true;
        return true;
    }

    public void disable() {
        transport.sendBlockingCommand(new Command("Debugger.disable"));
        enabled = false;
    }

    public void stepOver() {
        doStep("Debugger.stepOver");
    }
    
    public void stepInto() {
        doStep("Debugger.stepInto");
    }
    
    public void stepOut() {
        doStep("Debugger.stepOut");
    }
    
    public void resume() {
        doStep("Debugger.resume");
    }
    
    public void pause() {
        doStep("Debugger.pause");
    }
    
    private void doStep(String name) {
        transport.sendCommand(new Command(name));
    }
    
    public boolean isSuspended() {
        return suspended;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void addListener(Listener l) {
        listeners.add(l);
    }
    
    public void removeListener(Listener l) {
        listeners.remove(l);
    }
    
    private void notifyResumed() {
        suspended = false;
        for (Listener l : listeners ) {
            l.resumed();
        }
    }

    private void notifyPaused(JSONArray callFrames, String reason, JSONObject data) {
        suspended = true;
        List<CallFrame> callStack = new ArrayList<CallFrame>();
        for (Object cf : callFrames) {
            callStack.add(APIFactory.createCallFrame((JSONObject)cf, webkit, transport));
        }
        setCurrentCallStack(callStack);
        for (Listener l : listeners ) {
            l.paused(callStack, reason);
        }
    }
    
    private void notifyReset() {
        for (Listener l : listeners ) {
            l.reset();
        }
    }
    
    /**
     * Debugger state listener.
     */
    public interface Listener {
        
        /**
         * Execution was suspended.
         * @param callStack current callstack
         * @param reason what triggered this suspense
         */
        void paused(List<CallFrame> callStack, String reason);
        
        /**
         * Exeuction was resumed.
         */
        void resumed();
        
        /**
         * Object state was reset due to page reload.
         */
        void reset();
    }
    
    private synchronized void addScript(JSONObject data) {
        Script script = APIFactory.createScript(data, webkit);
        scripts.put(script.getID(), script);
    }
    
    public synchronized Script getScript(String scriptID) {
        return scripts.get(scriptID);
    }

    public synchronized List<CallFrame> getCurrentCallStack() {
        assert isSuspended();
        return currentCallStack;
    }
    
    private synchronized void setCurrentCallStack(List<CallFrame> callstack) {
        this.currentCallStack = callstack;
    }
    
    @SuppressWarnings("unchecked")    
    public Breakpoint addLineBreakpoint(String url, int lineNumber, int columnNumber) {
        JSONObject params = new JSONObject();
        params.put("lineNumber", lineNumber);
        params.put("url", url.replaceAll("/", "\\/")); //  XXX: not sure why but using backslash is necesssary here
        params.put("columnNumber", columnNumber);
        Response resp = transport.sendBlockingCommand(new Command("Debugger.setBreakpointByUrl", params));
        Breakpoint b = APIFactory.createBreakpoint(resp.getResponse(), webkit);
        currentBreakpoints.add(b);
        return b;
    }

    @SuppressWarnings("unchecked")    
    public void removeLineBreakpoint(Breakpoint b) {
        JSONObject params = new JSONObject();
        params.put("breakpointId", b.getBreakpointID());
        transport.sendCommand(new Command("Debugger.removeBreakpoint", params));
        currentBreakpoints.remove(b);
    }
    
    private class Callback implements ResponseCallback {

        @Override
        public void handleResponse(Response response) {
            if ("Debugger.resumed".equals(response.getMethod())) {
                notifyResumed();
                webkit.getRuntime().releaseNetBeansObjectGroup();
            } else if ("Debugger.paused".equals(response.getMethod())) {
                JSONObject params = (JSONObject)response.getResponse().get("params");
                notifyPaused((JSONArray)params.get("callFrames"), (String)params.get("reason"), (JSONObject)params.get("data"));
            } else if ("Debugger.globalObjectCleared".equals(response.getMethod())) {
                notifyReset();
            } else if ("Debugger.scriptParsed".equals(response.getMethod())) {
                addScript(response.getParams());
            }
        }
        
    }
}
