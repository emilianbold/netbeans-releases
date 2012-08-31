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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;
import org.openide.util.RequestProcessor;

/**
 * See Debugger section of WebKit Remote Debugging Protocol for more details.
 */
public final class Debugger {
    
    public static final String PROP_CURRENT_FRAME = "currentFrame";     // NOI18N
    public static final String PROP_BREAKPOINTS_ACTIVE = "breakpointsActive"; // NOI18N
    
    private static final Logger LOG = Logger.getLogger(Debugger.class.getName());

    private static boolean lastBreakpointsActive = true;
    
    private TransportHelper transport;
    private boolean enabled = false;
    private boolean suspended = false;
    private Callback callback;
    private boolean initDOMLister = true;
    private List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
    private PropertyChangeSupport pchs = new PropertyChangeSupport(this);
    private Map<String, Script> scripts = new HashMap<String, Script>();
    private WebKitDebugging webkit;
    private List<CallFrame> currentCallStack = new ArrayList<CallFrame>();
    private CallFrame currentCallFrame = null;
    private boolean breakpointsActive = lastBreakpointsActive;
    private final Object breakpointsActiveLock = new Object();
    private boolean inLiveHTMLMode = false;
    private RequestProcessor.Task latestSnapshotTask;    

    Debugger(TransportHelper transport, WebKitDebugging webkit) {
        this.transport = transport;
        this.webkit = webkit;
        this.callback = new Callback();
        this.transport.addListener(callback);
    }
    
    public boolean enable() {
        transport.sendBlockingCommand(new Command("Debugger.enable"));

        // always enable Page and Network; at the moment only Live HTML is using them
        // but I expect that soon it will be used somewhere else as well
        webkit.getPage().enable();
        webkit.getNetwork().enable();
        webkit.getConsole().enable();
        webkit.getCSS().enable();

        enabled = true;
        
        return true;
    }

    public void enableDebuggerInLiveHTMLMode() {
        inLiveHTMLMode = true;
        
        enable();
        
        latestSnapshotTask = transport.getRequestProcessor().create(new Runnable() {
            @Override
            public void run() {
                recordDocumentChange(System.currentTimeMillis(), null, false, false);
            }
        });
        
        initDOMLister = true;
        addEventBreakpoint("DOMContentLoaded");
        addEventBreakpoint("load");
    }

    public boolean isInLiveHTMLMode() {
        return inLiveHTMLMode;
    }
    
    public void disable() {
        assert enabled;
        webkit.getPage().disable();
        webkit.getNetwork().disable();
        webkit.getConsole().disable();
        webkit.getCSS().disable();
        transport.sendCommand(new Command("Debugger.disable"));
        enabled = false;
        initDOMLister = true;
    }

    public void stepOver() {
        doCommand("Debugger.stepOver");
    }
    
    public void stepInto() {
        doCommand("Debugger.stepInto");
    }
    
    public void stepOut() {
        doCommand("Debugger.stepOut");
    }
    
    public void resume() {
        doCommand("Debugger.resume");
    }
    
    public void pause() {
        doCommand("Debugger.pause");
    }
    
    private void doCommand(String name) {
        transport.sendCommand(new Command(name));
    }
    
    public boolean isSuspended() {
        return suspended;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Add a listener for debugger state changes.
     * @param l a state change listener
     */
    public void addListener(Listener l) {
        listeners.add(l);
    }
    
    /**
     * Remove a listener for debugger state changes.
     * @param l a state change listener
     */
    public void removeListener(Listener l) {
        listeners.remove(l);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pchs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pchs.removePropertyChangeListener(l);
    }
    
    private void notifyResumed() {
        suspended = false;
        setCurrentCallFrame(null);
        for (Listener l : listeners ) {
            l.resumed();
        }
    }
    
    public URL getConnectionURL() {
        return transport.getConnectionURL();
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
        if (!callStack.isEmpty()) {
            setCurrentCallFrame(callStack.get(0));
        } else {
            setCurrentCallFrame(null);
        }
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
    
    public synchronized CallFrame getCurrentCallFrame() {
        return currentCallFrame;
    }
    
    /**
     * Set the current call frame.
     * @param frame the actual call frame
     * @throws IllegalArgumentException when the frame is not on the current call stack.
     */
    public void setCurrentCallFrame(CallFrame frame) {
        CallFrame lastFrame;
        synchronized (this) {
            if (frame != null) {
                assert isSuspended();
                if (!currentCallStack.contains(frame)) {
                    throw new IllegalArgumentException("Unknown frame: "+frame);
                }
            }
            lastFrame = this.currentCallFrame;
            this.currentCallFrame = frame;
        }
        pchs.firePropertyChange(PROP_CURRENT_FRAME, lastFrame, frame);
    }
    
    /* not tested yet
    public void restartFrame(CallFrame frame) {
        JSONObject params = new JSONObject();
        params.put("callFrameId", frame);
        Response resp = transport.sendBlockingCommand(new Command("Debugger.restartFrame", params));
        if (resp != null) {
            notifyPaused((JSONArray)resp.getResponse().get("callFrames"), "", null);
        }
    }*/
    
    @SuppressWarnings("unchecked")    
    public Breakpoint addLineBreakpoint(String url, int lineNumber, int columnNumber) {
        if (inLiveHTMLMode) {
            // ignore line breakpoints when in Live HTML mode
            return null;
        }
        JSONObject params = new JSONObject();
        params.put("lineNumber", lineNumber);
        params.put("url", url.replaceAll("/", "\\/")); //  XXX: not sure why but using backslash is necesssary here
        params.put("columnNumber", columnNumber);
        Response resp = transport.sendBlockingCommand(new Command("Debugger.setBreakpointByUrl", params));
        if (resp != null) {
            JSONObject result = (JSONObject) resp.getResponse().get("result");
            if (result != null) {
                Breakpoint b = APIFactory.createBreakpoint(result, webkit);
                return b;
            } else {
                // What can we do when we have no results?
                LOG.log(Level.WARNING, "No result in setBreakpointByUrl response: {0}", resp);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")    
    public void removeLineBreakpoint(Breakpoint b) {
        JSONObject params = new JSONObject();
        params.put("breakpointId", b.getBreakpointID());
        transport.sendBlockingCommand(new Command("Debugger.removeBreakpoint", params));
    }
    
    // TODO: this method is used only internally so far and it needs to be revisisted
    public Breakpoint addDOMBreakpoint(Node node, String type) {
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId());
        params.put("type", type);
        Response resp = transport.sendBlockingCommand(new Command("DOMDebugger.setDOMBreakpoint", params));
        if (resp != null) {
            JSONObject result = (JSONObject) resp.getResponse().get("result");
            if (result != null) {
                Breakpoint b = APIFactory.createBreakpoint(result, webkit);
                return b;
            } else {
                // What can we do when we have no results?
                LOG.log(Level.WARNING, "No result in setDOMBreakpoint response: {0}", resp);
            }
        }
        return null;
    }
    
    public void removeDOMBreakpoint(Node node, String type) {
        JSONObject params = new JSONObject();
        params.put("nodeId", node.getNodeId());
        params.put("type", type);
        Response resp = transport.sendBlockingCommand(new Command("DOMDebugger.removeDOMBreakpoint", params));
    }
    
    // TODO: this method is used only internally so far and it needs to be revisisted
    public Breakpoint addXHRBreakpoint(String urlSubstring) {
        JSONObject params = new JSONObject();
        params.put("url", urlSubstring);
        Response resp = transport.sendBlockingCommand(new Command("DOMDebugger.setXHRBreakpoint", params));
        if (resp != null) {
            JSONObject result = (JSONObject) resp.getResponse().get("result");
            if (result != null) {
                Breakpoint b = APIFactory.createBreakpoint(result, webkit);
                return b;
            } else {
                // What can we do when we have no results?
                LOG.log(Level.WARNING, "No result in setXHRBreakpoint response: {0}", resp);
            }
        }
        return null;
    }
    
    public static final String DOM_BREAKPOINT_SUBTREE = "subtree-modified";
    public static final String DOM_BREAKPOINT_ATTRIBUTE = "attribute-modified";
    public static final String DOM_BREAKPOINT_NODE = "node-removed";

    // TODO: this method is used only internally so far and it needs to be revisisted
    public Breakpoint addEventBreakpoint(String event) {
        JSONObject params = new JSONObject();
        params.put("eventName", event);
        Response resp = transport.sendBlockingCommand(new Command("DOMDebugger.setEventListenerBreakpoint", params));
        if (resp != null) {
            JSONObject result = (JSONObject) resp.getResponse().get("result");
            if (result != null) {
                Breakpoint b = APIFactory.createBreakpoint(result, webkit);
                return b;
            } else {
                // What can we do when we have no results?
                LOG.log(Level.WARNING, "No result in setEventListenerBreakpoint response: {0}", resp);
            }
        }
        return null;
    }
    
    // TODO: this method is used only internally so far and it needs to be revisisted
    public void removeEventBreakpoint(String event) {
        JSONObject params = new JSONObject();
        params.put("eventName", event);
        Response resp = transport.sendBlockingCommand(new Command("DOMDebugger.removeEventListenerBreakpoint", params));
    }
    
    public boolean areBreakpointsActive() {
        return breakpointsActive;
    }
    
    public void setBreakpointsActive(boolean active) {
        boolean oldActive;
        synchronized (breakpointsActiveLock) {
            oldActive = breakpointsActive;
            if (oldActive != active) {
                JSONObject params = new JSONObject();
                params.put("active", active);
                Response resp = transport.sendBlockingCommand(new Command("Debugger.setBreakpointsActive", params));
                breakpointsActive = active;
                lastBreakpointsActive = active;
            }
        }
        if (oldActive != active) {
            pchs.firePropertyChange(PROP_BREAKPOINTS_ACTIVE, oldActive, active);
        }
    }
    
    private void recordDocumentChange(long timeStamp, JSONArray callStack, boolean attachDOMListeners, boolean realChange) {
        assert inLiveHTMLMode;
        
        Node n = webkit.getDOM().getDocument();
        if (attachDOMListeners) {
            addDOMBreakpoint(n, Debugger.DOM_BREAKPOINT_SUBTREE);
            removeEventBreakpoint("DOMContentLoaded");
            removeEventBreakpoint("load");
        }
        String content = webkit.getDOM().getOuterHTML(n);
        JSONArray callStack2 = callStack != null ? normalizeStackTrace(callStack) : null;
        if (realChange) {
            LiveHTML.getDefault().storeDocumentVersionBeforeChange(transport.getConnectionURL(), 
                    timeStamp, content, callStack2 != null ? callStack2.toJSONString() : null);
            resume();
            latestSnapshotTask.schedule(345);
        } else {
            LiveHTML.getDefault().storeDocumentVersionAfterChange(transport.getConnectionURL(), 
                    timeStamp, content);
        }
    }
    
    private class Callback implements ResponseCallback {

        @Override
        public void handleResponse(Response response) {
            if ("Debugger.resumed".equals(response.getMethod())) {
                notifyResumed();
                webkit.getRuntime().releaseNetBeansObjectGroup();
            } else if ("Debugger.paused".equals(response.getMethod())) {
                JSONObject params = (JSONObject)response.getResponse().get("params");

                if (inLiveHTMLMode) {
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
                                recordDocumentChange(timestamp, callStack, finalAttachDOMListeners, true);
                            }
                        });
                    }
                } else {
                    JSONArray frames = (JSONArray)params.get("callFrames");
                    //TODO: workaround for mobile safari
                    if (frames == null) {
                        frames = (JSONArray) ((JSONObject) params.get("details")).get("callFrames");
                    }
                    notifyPaused(frames, (String)params.get("reason"), (JSONObject)params.get("data"));
                }
            } else if ("Debugger.globalObjectCleared".equals(response.getMethod())) {
                notifyReset();
            } else if ("Debugger.scriptParsed".equals(response.getMethod())) {
                addScript(response.getParams());
            }
        }
        
    }

    /**
     * Debugger state listener.
     */
    public interface Listener extends EventListener {
        
        /**
         * Execution was suspended.
         * @param callStack current call stack
         * @param reason what triggered this suspense
         */
        void paused(List<CallFrame> callStack, String reason);
        
        /**
         * Execution was resumed.
         */
        void resumed();
        
        /**
         * Object state was reset due to page reload.
         */
        void reset();
    }
    
}
