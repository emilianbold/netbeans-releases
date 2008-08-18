/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.web.client.tools.javascript.debugger.spi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;

import java.util.logging.Logger;
import org.netbeans.api.debugger.Breakpoint.HIT_COUNT_FILTERING_STYLE;
import org.netbeans.modules.web.client.tools.common.dbgp.DbgpUtils;
import org.netbeans.modules.web.client.tools.common.dbgp.DebuggerProxy;
import org.netbeans.modules.web.client.tools.common.dbgp.DebuggerServer;
import org.netbeans.modules.web.client.tools.common.dbgp.Feature;
import org.netbeans.modules.web.client.tools.common.dbgp.HttpMessage;
import org.netbeans.modules.web.client.tools.common.dbgp.Message;
import org.netbeans.modules.web.client.tools.common.dbgp.ResponseMessage;
import org.netbeans.modules.web.client.tools.common.dbgp.SourcesMessage;
import org.netbeans.modules.web.client.tools.common.dbgp.Status.StatusResponse;
import org.netbeans.modules.web.client.tools.common.dbgp.StreamMessage;
import org.netbeans.modules.web.client.tools.common.dbgp.UnsufficientValueException;
import org.netbeans.modules.web.client.tools.common.dbgp.WindowsMessage;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSBreakpoint;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSCallStackFrame;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerConsoleEvent;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerEvent;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerState;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSHttpMessage;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSProperty;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSURILocation;
import org.netbeans.modules.web.client.tools.javascript.debugger.impl.JSFactory;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;

/**
 *
 * @author Sandip V. Chitale <sandipchitale@netbeans.org>, jdeva
 */
public abstract class JSAbstractExternalDebugger extends JSAbstractDebugger {

    protected String ID;
    protected DebuggerProxy proxy;
    protected SuspensionPointHandler suspensionPointHandler;
    protected HttpMessageHandler httpMessageHandler;

    public JSAbstractExternalDebugger(URI uri, HtmlBrowser.Factory browser) {
        super(uri, browser);
    }
    
    @Override
    protected void startDebuggingImpl() {
        DebuggerServer server = new DebuggerServer(getID());
        int port = -1;
        try {
            port = server.createSocket();
        } catch (IOException ioe) {
            Log.getLogger().log(Level.SEVERE, "Unable to create server socket", ioe);
            return;
        }

        launchImpl(port);
        
        try {
            //Wait for debugger engine to connect back
            proxy = server.getDebuggerProxy();
        } catch (IOException ioe) {
            Log.getLogger().log(Level.SEVERE, "Unable to get the debugger proxy", ioe); //NOI18N
            return;
        }
        proxy.startDebugging();

        //Start the suspension point handler thread
        suspensionPointHandler = new SuspensionPointHandler(proxy, getID());
        httpMessageHandler = new HttpMessageHandler(proxy, getID());
        suspensionPointHandler.start();
        httpMessageHandler.start();
    }

    protected abstract void launchImpl(int port);

    public void setBooleanFeature(Feature.Name featureName, boolean featureValue) {
        proxy.setBooleanFeature(featureName, featureValue);
    }

    public void openURI(URI uri) throws URISyntaxException {
        // Now enable the debugger
        setBooleanFeature(Feature.Name.ENABLE, true);

        // Now ask to open the URI
        proxy.openURI(uri);
        fireJSDebuggerEvent(new JSDebuggerEvent(JSAbstractExternalDebugger.this, JSDebuggerState.STARTING_READY));
    }

    public void resume() {
        proxy.run();
    }

    public void pause() {
        proxy.pause();
    }

    public void stepInto() {
        proxy.stepInto();
    }

    public void stepOver() {
        proxy.stepOver();
    }

    public void stepOut() {
        proxy.stepOut();
    }

    private void fireDebuggerEvent(StatusResponse response) {
        if (DbgpUtils.isStepSuccessfull(response)) {
            JSBreakpoint breakpoint = DbgpUtils.getBreakpoint(response);
            JSDebuggerState.Reason reason = JSDebuggerState.Reason.STEP;
            if (breakpoint.getId() != null) {
                reason = JSDebuggerState.Reason.BREAKPOINT;
            }
            JSDebuggerState state = JSDebuggerState.getDebuggerState(breakpoint, reason);
            fireJSDebuggerEvent(new JSDebuggerEvent(this, state));
        }
    }

    public boolean isRunningTo(URI uri, int line) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void finishImpl(boolean terminate) {
        if (getDebuggerState() == JSDebuggerState.NOT_CONNECTED) {
            return;
        }
        if (terminate) {
            // Disable the debugger
            setBooleanFeature(Feature.Name.ENABLE, false);

            if (proxy != null) {
                proxy.stopDebugging();
            }

            if (suspensionPointHandler != null) {
                suspensionPointHandler.interrupt();
                suspensionPointHandler = null;
            }
            
            if (httpMessageHandler != null) {
                httpMessageHandler.interrupt();
                httpMessageHandler = null;
            }
            
            setDebuggerState(JSDebuggerState.DISCONNECTED_USER);
        }
    }
    
    public void runToCursor(JSURILocation location) {
        proxy.runToCursor(DbgpUtils.getDbgpBreakpointCommand(proxy, location, true));
    }    

    public String setBreakpoint(JSBreakpoint breakpoint) {
        return proxy.setBreakpoint(DbgpUtils.getDbgpBreakpointCommand(proxy, breakpoint));
    }

    public boolean removeBreakpoint(String id) {
        return proxy.removeBreakpoint(id);
    }

    public boolean updateBreakpoint(String id, Boolean enabled, int line, int hitValue, HIT_COUNT_FILTERING_STYLE hitCondition, String condition) {
        return proxy.updateBreakpoint(id,
                enabled,
                line,
                hitValue,
                hitCondition,
                condition);
    }

    public List<JSBreakpoint> getBreakpoints(List<String> breakpointIds) {
        return DbgpUtils.getJSBreakpoints(proxy.getBreakpoints(breakpointIds));
    }

    public JSBreakpoint getBreakpoint(String breakpointId) {
        return DbgpUtils.getJSBreakpoint(proxy.getBreakpoint(breakpointId));
    }

    public List<JSBreakpoint> getBreakpoints() {
        return DbgpUtils.getJSBreakpoints(proxy.getBreakpoints());
    }

    public JSCallStackFrame getCallStackFrame() {
        return getCallStackFrame(-1);
    }

    public JSCallStackFrame getCallStackFrame(int depth) {
        return DbgpUtils.getJSCallStackFrame(this, proxy.getCallStack(depth));
    }

    @Override
    protected JSCallStackFrame[] getCallStackFramesImpl() {
        return DbgpUtils.getJSCallStackFrames(this, proxy.getCallStacks()).toArray(JSCallStackFrame.EMPTY_ARRAY);
    }

    @Override
    protected JSProperty getScopeImpl(JSCallStackFrame callStackFrame) {
        return getPropertyImpl(callStackFrame, ".");
    }

    @Override
    protected JSProperty getThisImpl(JSCallStackFrame callStackFrame) {
        return getPropertyImpl(callStackFrame, "this");
    }

    @Override
    protected JSProperty evalImpl(JSCallStackFrame callStackFrame, String expression) {
        return DbgpUtils.getJSProperty(callStackFrame, proxy.eval(expression, callStackFrame.getDepth()));
    }

    @Override
    protected JSProperty getPropertyImpl(JSCallStackFrame callStackFrame, String fullName) {
        return DbgpUtils.getJSProperty(callStackFrame, proxy.getProperty(fullName, callStackFrame.getDepth()));
    }
    
    @Override
    protected boolean setPropertyImpl(JSCallStackFrame callStackFrame, String fullName, String value) {
        return proxy.setProperty(fullName, value, callStackFrame.getDepth());
    }    

    @Override
    protected JSProperty[] getPropertiesImpl(JSCallStackFrame callStackFrame, String fullName) {
        return DbgpUtils.getJSProperties(callStackFrame, proxy.getProperty(fullName, callStackFrame.getDepth()));
    }
    
    protected InputStream getInputStreamForURLImpl(URL url) {
        if (proxy != null && url != null) {
            try {
                byte[] bytes = proxy.getSource(url.toURI());
                if (bytes != null) {
                    return new ByteArrayInputStream(bytes);
                }
            } catch (URISyntaxException use) {
                Log.getLogger().log(Level.INFO, use.getMessage(), use);
            }
        }
        return null;
    }    

    private void handleSourcesMessage(SourcesMessage sourcesMessage) {
        setSources(JSFactory.getJSSources(sourcesMessage.getSources()));
    }

    private void handleWindowsMessage(WindowsMessage windowsMessage) {
        setWindows(JSFactory.getJSWindows(windowsMessage.getWindows()));
    }

    private void handleStreamMessage(StreamMessage streamMessage) {
        JSDebuggerConsoleEvent consoleEvent = null;
        try {
            consoleEvent = new JSDebuggerConsoleEvent(this,
                    JSDebuggerConsoleEvent.ConsoleType.valueOf(streamMessage.getType().toUpperCase()),
                    streamMessage.getStringValue());
        } catch (UnsufficientValueException ex) {
            Log.getLogger().log(Level.INFO, "Unable to get the console message", ex);   //NOI18N
        }
        fireJSDebuggerConsoleEvent(consoleEvent);
    }

    private void handleHttpMessage(HttpMessage httpMessage) {
        JSHttpMessage jsHttpMessage = JSFactory.createJSHttpMessage(httpMessage);
        setHttpMessage(jsHttpMessage);
    }

    protected class HttpMessageHandler extends Thread {
        DebuggerProxy proxy;

        HttpMessageHandler(DebuggerProxy proxy, String id) {
            super("Http Message Handler");  //NOI18N
            this.setDaemon(true);
            this.proxy = proxy;
        }

        @Override
        public void run() {
            Log.getLogger().log(Level.FINEST, "Starting " + getName()); //NOI18N
            while (proxy.isHttpQueueActive()) {
                Message message = getNextMessage();
                if (message != null) {
                    handle(message);
                }
            }
            Log.getLogger().log(Level.FINEST, "Ending " + getName());   //NOI18N
        }
        
        private Message getNextMessage() {
            return  proxy.getHttpMessage();
        }

        private void handle(Message message) {
            // Spontaneous messages
            if (message instanceof HttpMessage) {
                handleHttpMessage((HttpMessage) message);
                return;
            } else if (message instanceof ResponseMessage) {
                return;
            } else {
                Logger.getLogger(this.getName()).info("Something Seems Wrong");
            }
        }

    }

    protected class SuspensionPointHandler extends Thread {

        DebuggerProxy proxy;

        SuspensionPointHandler(DebuggerProxy proxy, String id) {
            super("Suspension point handler");  //NOI18N
            this.setDaemon(true);
            this.proxy = proxy;
        }

        @Override
        public void run() {
            Log.getLogger().log(Level.FINEST, "Starting " + getName()); //NOI18N
            while (proxy.isSuspensionQueueActive()) {
                Message message = getNextMessage();
                if (message != null) {
                    handle(message);
                }
            }
            Log.getLogger().log(Level.FINEST, "Ending " + getName());   //NOI18N
        }

        private Message getNextMessage() {
            return proxy.getSuspensionPoint();
        }

        private void handle(Message message) {
            // Spontaneous messages
            if (message instanceof SourcesMessage) {
                handleSourcesMessage((SourcesMessage) message);
                return;
            } else if (message instanceof WindowsMessage) {
                handleWindowsMessage((WindowsMessage) message);
                return;
            } else if (message instanceof StreamMessage) {
                handleStreamMessage((StreamMessage) message);
            } 
            // State oriented
            JSDebuggerState messageDebuggerState = DbgpUtils.getDebuggerState(message);
            if(messageDebuggerState != null) {
                setDebuggerState(messageDebuggerState);
                if (messageDebuggerState.getReason().equals(JSDebuggerState.Reason.INIT)) {
                    // Now request to open the debug URI
                    try {
                        openURI(getURI());
                    } catch (URISyntaxException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }
}
