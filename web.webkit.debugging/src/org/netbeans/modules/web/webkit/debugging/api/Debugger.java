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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.netbeans.modules.web.webkit.debugging.APIFactory;
import org.netbeans.modules.web.webkit.debugging.LiveHTML;
import org.netbeans.modules.web.webkit.debugging.TransportHelper;
import org.netbeans.modules.web.webkit.debugging.api.debugger.Breakpoint;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.modules.web.webkit.debugging.api.debugger.Script;
import org.netbeans.modules.web.webkit.debugging.api.dom.Node;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.LiveHTMLImplementation;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;
import org.openide.util.RequestProcessor;

/**
 * See Debugger section of WebKit Remote Debugging Protocol for more details.
 */
public final class Debugger {

    private TransportHelper transport;
    private boolean enabled = false;
    private boolean suspended = false;
    private Callback callback;
    private boolean initDOMLister = true;
    private List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
    private Map<String, Script> scripts = new HashMap<String, Script>();
    private WebKitDebugging webkit;
    private List<CallFrame> currentCallStack = new ArrayList<CallFrame>();
    private List<Breakpoint> currentBreakpoints = new ArrayList<Breakpoint>();
    private LiveHTMLImplementation.Listener liveHTMLListener;
    private Boolean liveHTMLStopped = null;
    private Runnable stopCallback;
    private boolean ignoreFirstStopNotification = false;

    Debugger(TransportHelper transport, WebKitDebugging webkit) {
        this.transport = transport;
        this.webkit = webkit;
        this.callback = new Callback();
        this.transport.addListener(callback);
    }
    
    public boolean enable() {
        Response response = transport.sendBlockingCommand(new Command("Debugger.enable"));

        liveHTMLListener = new LiveHTMLListener(this);
        LiveHTML.getDefault().addListener(liveHTMLListener);

        // always enable Page and Network; at the moment only Live HTML is using them
        // but I expect that soon it will be used somewhere else as well
        webkit.getPage().enable();
        webkit.getNetwork().enable();
        
        if (LiveHTML.getDefault().isEnabledFor(transport.getConnectionURL())) {
            liveHTMLStopped  = Boolean.FALSE;
            addEventBreakpoint("DOMContentLoaded");
            addEventBreakpoint("load");
        }
        
        // TODO: what will response contain if there was an error enabling debuggger in the page?
        enabled = true;
        return true;
    }

    public void disable() {
        assert enabled;
        webkit.getPage().disable();
        webkit.getNetwork().disable();
        transport.sendCommand(new Command("Debugger.disable"));
        enabled = false;
        initDOMLister = true;
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
    
    private List<CallFrame> createCallStack(JSONArray callFrames) {
        List<CallFrame> callStack = new ArrayList<CallFrame>();
        for (Object cf : callFrames) {
            callStack.add(APIFactory.createCallFrame((JSONObject)cf, webkit, transport));
        }
        return callStack;
    }
    
    private void notifyPaused(JSONArray callFrames, String reason, JSONObject data) {
        suspended = true;
        List<CallFrame> callStack = createCallStack(callFrames);
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

    private JSONArray normalizeStackTrace(JSONArray callStack) {
        JSONArray res = new JSONArray();
        for (Object o : callStack) {
            JSONObject cf = (JSONObject)o;
            JSONObject ncf = new JSONObject();
            ncf.put("lineNumber", ((JSONObject)cf.get("location")).get("lineNumber"));
            ncf.put("columnNumber", ((JSONObject)cf.get("location")).get("columnNumber"));
            ncf.put("function", cf.get("functionName"));
            Script sc = getScript((String)((JSONObject)(cf.get("location"))).get("scriptId"));
            if (sc == null) {
                continue;
            }
            ncf.put("script", sc.getURL());
            res.add(ncf);
        }
        return res;
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
        if (resp != null) {
            Breakpoint b = APIFactory.createBreakpoint((JSONObject)resp.getResponse().get("result"), webkit);
            currentBreakpoints.add(b);
            return b;
        }
        return null;
    }

    @SuppressWarnings("unchecked")    
    public void removeLineBreakpoint(Breakpoint b) {
        JSONObject params = new JSONObject();
        params.put("breakpointId", b.getBreakpointID());
        transport.sendBlockingCommand(new Command("Debugger.removeBreakpoint", params));
        currentBreakpoints.remove(b);
    }
    
    // TODO: this method is used only internally so far and it needs to be revisisted
    public void addDOMBreakpoint(Node node, String type) {
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId());
        params.put("type", type);
        Response resp = transport.sendBlockingCommand(new Command("DOMDebugger.setDOMBreakpoint", params));
    }
    
    public void removeDOMBreakpoint(Node node, String type) {
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId());
        params.put("type", type);
        Response resp = transport.sendBlockingCommand(new Command("DOMDebugger.removeDOMBreakpoint", params));
    }
    
    // TODO: this method is used only internally so far and it needs to be revisisted
    public void addXHRBreakpoint(String urlSubstring) {
        JSONObject params = new JSONObject();
        params.put("url", urlSubstring);
        Response resp = transport.sendBlockingCommand(new Command("DOMDebugger.setXHRBreakpoint", params));
    }
    
    public static final String DOM_BREAKPOINT_SUBTREE = "subtree-modified";
    public static final String DOM_BREAKPOINT_ATTRIBUTE = "attribute-modified";
    public static final String DOM_BREAKPOINT_NODE = "node-removed";

    // TODO: this method is used only internally so far and it needs to be revisisted
    public void addEventBreakpoint(String event) {
        JSONObject params = new JSONObject();
        params.put("eventName", event);
        Response resp = transport.sendBlockingCommand(new Command("DOMDebugger.setEventListenerBreakpoint", params));
    }
    
    // TODO: this method is used only internally so far and it needs to be revisisted
    public void removeEventBreakpoint(String event) {
        JSONObject params = new JSONObject();
        params.put("eventName", event);
        Response resp = transport.sendBlockingCommand(new Command("DOMDebugger.removeEventListenerBreakpoint", params));
    }
    
    private void recordDocumentChange(long timeStamp, JSONArray callStack, boolean attachDOMListeners) {
        assert LiveHTML.getDefault().isEnabledFor(transport.getConnectionURL());
        
        Node n = webkit.getDOM().getDocument();
        if (attachDOMListeners) {
            addDOMBreakpoint(n, Debugger.DOM_BREAKPOINT_SUBTREE);
            removeEventBreakpoint("DOMContentLoaded");
            removeEventBreakpoint("load");
        }
        String content = webkit.getDOM().getNodeHTML(n);
        JSONArray callStack2 = normalizeStackTrace(callStack);
        LiveHTML.getDefault().storeDocumentVersion(transport.getConnectionURL(), timeStamp, content, callStack2.toJSONString());
        resume();
    }
    
    private void startRecordingLiveHTMLChange(Runnable stopCallback) {
        if (liveHTMLStopped == Boolean.TRUE || liveHTMLStopped == null) {
            liveHTMLStopped = Boolean.FALSE;
            // set events breakpoint to init DOM breakpoint
            initDOMLister = true;
            addEventBreakpoint("DOMContentLoaded");
            addEventBreakpoint("load");
            this.stopCallback = stopCallback;
            this.ignoreFirstStopNotification = true;
        }
    }
 
    private void stopRecordingLiveHTMLChange() {
        // disable DOM breakpoint; the rest is handled by LiveHTML.getDefault().isEnabledFor() method
        liveHTMLStopped = Boolean.TRUE;
        stopCallback = null;
        Node n = webkit.getDOM().getDocument();
        removeDOMBreakpoint(n, Debugger.DOM_BREAKPOINT_SUBTREE);
    }
 
    private class Callback implements ResponseCallback {

        @Override
        public void handleResponse(Response response) {
            if ("Debugger.resumed".equals(response.getMethod())) {
                notifyResumed();
                webkit.getRuntime().releaseNetBeansObjectGroup();
            } else if ("Debugger.paused".equals(response.getMethod())) {
                JSONObject params = (JSONObject)response.getResponse().get("params");

                if (LiveHTML.getDefault().isEnabledFor(transport.getConnectionURL())) {
                    final long timestamp = System.currentTimeMillis();
                    final JSONObject data = (JSONObject)params.get("data");

                    boolean internalSuspend = false;
                    boolean attachDOMListeners = false;
                    if ("DOM".equals(params.get("reason"))) {
                        internalSuspend = true;
                    }
                    if ("EventListener".equals(params.get("reason"))) {
                        if (data != null && ("listener:DOMContentLoaded".equals(data.get("eventName")) ||
                                "listener:load".equals(data.get("eventName")))) {
                            internalSuspend = true;
                            if (initDOMLister) {
                                attachDOMListeners = true;
                            }
                            initDOMLister = false;
                        }
                    }
                    if (internalSuspend) {
                        final JSONArray callStack = (JSONArray)params.get("callFrames");
                        final boolean finalAttachDOMListeners = attachDOMListeners;
                        transport.getRequestProcessor().post(new Runnable() {
                            @Override
                            public void run() {
                                recordDocumentChange(timestamp, callStack, finalAttachDOMListeners);
                            }
                        });
                        return;
                    }
                }
                notifyPaused((JSONArray)params.get("callFrames"), (String)params.get("reason"), (JSONObject)params.get("data"));
            } else if ("Debugger.globalObjectCleared".equals(response.getMethod())) {
                notifyReset();
                // double check this is right event to notify that live HTML should stop:
                if (LiveHTML.getDefault().isEnabledFor(transport.getConnectionURL())) {
                    transport.getRequestProcessor().post(new Runnable() {
                        @Override
                        public void run() {
                            if (stopCallback != null) {
                                if (ignoreFirstStopNotification) {
                                    ignoreFirstStopNotification = false;
                                } else {
                                    stopCallback.run();
                                    stopCallback = null;
                                }
                            }
                            stopRecordingLiveHTMLChange();
                        }
                    });
                }
            } else if ("Debugger.scriptParsed".equals(response.getMethod())) {
                addScript(response.getParams());
            }
        }
        
    }
    
    private static class LiveHTMLListener implements LiveHTMLImplementation.Listener {

        private Debugger debugger;

        public LiveHTMLListener(Debugger debugger) {
            this.debugger = debugger;
        }
        
        @Override
        public void startRecordingChange(URL connectionURL, final Runnable stopCallback) {
            // this is here intentionally so that code does not run in AWT thread;
            // however if even producer wants to make sure event handler was executed synchronously
            // they can achieve that by firing event outside of AWT thread:
            if (SwingUtilities.isEventDispatchThread()) {
                debugger.transport.getRequestProcessor().post(new Runnable() {
                    @Override
                    public void run() {
                        debugger.startRecordingLiveHTMLChange(stopCallback);
                    }
                });
            } else {
                debugger.startRecordingLiveHTMLChange(stopCallback);
            }
        }
        
        @Override
        public void stopRecordingChange(URL connectionURL) {
            // this is here intentionally so that code does not run in AWT thread;
            // however if even producer wants to make sure event handler was executed synchronously
            // they can achieve that by firing event outside of AWT thread:
            if (SwingUtilities.isEventDispatchThread()) {
                debugger.transport.getRequestProcessor().post(new Runnable() {
                    @Override
                    public void run() {
                        debugger.stopRecordingLiveHTMLChange();
                    }
                });
            } else {
                debugger.stopRecordingLiveHTMLChange();
            }
        }
        
    }

}
