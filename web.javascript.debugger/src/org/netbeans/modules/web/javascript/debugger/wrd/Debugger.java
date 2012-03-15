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
package org.netbeans.modules.web.javascript.debugger.wrd;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.chromium.sdk.*;
import org.chromium.sdk.Breakpoint;
import org.chromium.sdk.Breakpoint.Target;
import org.chromium.sdk.wip.*;
import org.netbeans.api.debugger.*;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.browser.api.BrowserDebugger;
import org.netbeans.modules.web.javascript.debugger.DebuggerConstants;
import org.netbeans.modules.web.javascript.debugger.DebuggerEngineProviderImpl;
import org.netbeans.modules.web.javascript.debugger.breakpoints.AbstractBreakpoint;
import org.netbeans.modules.web.javascript.debugger.breakpoints.LineBreakpoint;
import org.netbeans.modules.web.javascript.debugger.callstack.CallStackModel;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.viewmodel.TreeModel;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Debugger based on WebKit Remote Debugging.
 */
@NbBundle.Messages({
    "ConnectingTo=Connecting to {0}:{1} ...",
    "ConnectionEstablished=Connection established",
    "ConnectionNotEstablished=Connection was not established",
    "ConnectionNotEstablished2=Connection was not established. Cause: {0}",
    "NewBreakpoint=Breakpoint set for {0}:{1}",
    "FailedBreakpoint=Could not set breakpoint. Reason: {0}",
    "StateChanged=Debugger state changed to {0}"
})
public class Debugger {

    private WipBrowser browser;
    private WipBackend backend;
    private JavascriptVm javascriptVm;
    private TabListener listener;
    private DebugListener debugListener;
    private List<DebuggerListener> listeners;
    private String urlToDebug;
    private Session session;
    private DebuggerEngine debuggerEngine;
    private LoggerFactoryImpl logger;
    
    public static Debugger createConnection(String host, int port, String urlToDebug) {
        Debugger d = new Debugger();
        try {
            if (!d.connect(host, port, urlToDebug)) {
                return null;
            }
        } catch (IOException ex) {
            return null;
        }
        return d;
    }
    
    private Debugger() {
        listeners = new CopyOnWriteArrayList<DebuggerListener>();
    }
    
    public void addListener(DebuggerListener l) {
        listeners.add(l);
    }

    public void removeListener(DebuggerListener l) {
        listeners.remove(l);
    }
    
    private void fireStateChange() {
        for (DebuggerListener l : listeners) {
            l.stateChanged(this);
        }
        BrowserDebugger.getOutputLogger().getOut().println(Bundle.StateChanged(getState().getName()));
    }

    public String getUrlToDebug() {
        return urlToDebug;
    }
    
    private boolean connect(String host, int port, String urlToDebug) throws IOException {
        this.urlToDebug = urlToDebug;
        logger = new LoggerFactoryImpl();
        BrowserDebugger.getOutputLogger().getOut().println(Bundle.ConnectingTo(host, String.valueOf(port)));
        try {
            browser = WipBrowserFactory.INSTANCE.createBrowser(
                    new InetSocketAddress(host, port), logger);
            WipBackendFactory backendFactory = new WipBackendFactory(); // Same class name in each backend .jar 
            backend = backendFactory.create();
            urlToDebug = reformatFileURL(urlToDebug);
            List<? extends WipBrowser.WipTabConnector> tabs = browser.getTabs(backend);
            for (WipBrowser.WipTabConnector tabConnector : tabs) {
                if (tabConnector.getUrl().equals(urlToDebug)) {
                    BrowserDebugger.getOutputLogger().getOut().println(Bundle.ConnectionEstablished());
                    listener = new TabListener();
                    debugListener = listener.getDebugEventListenerImpl();
                    WipBrowserTab tab = tabConnector.attach(listener);
                    javascriptVm = tab.getJavascriptVm();
                    DebuggerInfo di = DebuggerInfo.create(DebuggerConstants.DEBUGGER_INFO, new Object[]{this});
                    DebuggerEngine[] engines = DebuggerManager.getDebuggerManager().startDebugging(di);
                    debuggerEngine = engines[0];
                    session = debuggerEngine.lookupFirst(null, Session.class);
                    activateBreakpoints();

                    // consider reloading the page here to trigger breakpoints which were set
                    // before debugging session was established

                    return true;
                }
            }
            BrowserDebugger.getOutputLogger().getErr().println(Bundle.ConnectionNotEstablished());
            return false;
        } catch (Throwable t) {
            BrowserDebugger.getOutputLogger().getErr().println(Bundle.ConnectionNotEstablished2(t.getMessage()));
            Exceptions.printStackTrace(t);
            return false;
        }
    }
    
    public void stopDebugger() {
        javascriptVm.detach();
        javascriptVm = null;
    }

    private void activateBreakpoints() {
        for (org.netbeans.api.debugger.Breakpoint b : DebuggerManager.getDebuggerManager().getBreakpoints()) {
            addBreakpoint(b);
        }
    }
    
    public void addBreakpoint(org.netbeans.api.debugger.Breakpoint b) {
        if (b instanceof LineBreakpoint) {
            addLineBreakpoint((LineBreakpoint)b);
        }
    }
    
    public void addLineBreakpoint(LineBreakpoint b) {
        FileObject fo = b.getLine().getLookup().lookup(FileObject.class);
        String file = reformatFileURL(fo.toURL().toExternalForm());
        JavascriptVm.BreakpointCallback bc = new JavascriptVm.BreakpointCallback() {

            @Override
            public void success(Breakpoint breakpoint) {
                String target = breakpoint.getTarget().accept(new Breakpoint.Target.Visitor<String>() {

                    @Override
                    public String visitScriptName(String scriptName) {
                        return scriptName;
                    }

                    @Override
                    public String visitScriptId(Object scriptId) {
                        return scriptId.toString();
                    }

                    @Override
                    public String visitUnknown(Target target) {
                        return target.toString();
                    }
                });
                BrowserDebugger.getOutputLogger().getErr().println(Bundle.NewBreakpoint(target, breakpoint.getLineNumber()));
            }

            @Override
            public void failure(String errorMessage) {
                BrowserDebugger.getOutputLogger().getErr().println(Bundle.FailedBreakpoint(errorMessage));
            }
        };
        javascriptVm.setBreakpoint(new Breakpoint.Target.ScriptName(file),
                b.getLine().getLineNumber(), // line
                1, // column
                true, null, bc, null); // other parameters.
    }
    
    public void removeBreakpoint(org.netbeans.api.debugger.Breakpoint b) {
        
    }
    
    public List<? extends CallFrame> getCurrentStackTrace() {
        DebugContext debugContext = debugListener.getSavedDebugContext();
        return debugContext.getCallFrames();
    }
    
    public void stepInto() {
        debugListener.getSavedDebugContext().continueVm(DebugContext.StepAction.IN, 1, null);
    }
    
    public void stepOver() {
        debugListener.getSavedDebugContext().continueVm(DebugContext.StepAction.OVER, 1, null);
    }

    public void stepOut() {
        debugListener.getSavedDebugContext().continueVm(DebugContext.StepAction.OUT, 1, null);
    }
    
    public void resume() {
        debugListener.getSavedDebugContext().continueVm(DebugContext.StepAction.CONTINUE, 1, null);
    }

    public DebuggerState getState() {
        if (javascriptVm == null) {
            return DebuggerState.DISCONNECTED;
        }
        return debugListener.getState();
    }

    // changes "file:/some" to "file:///some"
    private String reformatFileURL(String tabToDebug) {
        if (!tabToDebug.startsWith("file:")) {
            return tabToDebug;
        }
        tabToDebug = tabToDebug.substring(5);
        while (tabToDebug.length() > 0 && tabToDebug.startsWith("/")) {
            tabToDebug = tabToDebug.substring(1);
        }
        return "file:///"+tabToDebug;
    }

    private class TabListener implements TabDebugEventListener {

        private DebugListener debugEvenListener;

        public TabListener() {
            this.debugEvenListener = new DebugListener();
        }

        @Override
        public DebugEventListener getDebugEventListener() {
            return debugEvenListener;
        }
        
        public DebugListener getDebugEventListenerImpl() {
            return debugEvenListener;
        }

        @Override
        public void navigated(String string) {
        }

        @Override
        public void closed() {
            session.kill();
            ((DebuggerEngineProviderImpl) debuggerEngine.lookupFirst(null,
                    DebuggerEngineProvider.class)).getDestructor().killEngine();
            debuggerEngine = null;
            session = null;
            fireStateChange();
        }
    }

    private class DebugListener implements DebugEventListener {

        private DebugContext savedDebugContext = null;
        private final Semaphore semaphore = new Semaphore(0);
        private DebuggerState state = DebuggerState.RUNNING;

        private VmStatusListener list;
        
        public DebugListener() {
            list = new VmStatusListener() {
                @Override
                public void busyStatusChanged(String currentRequest, int numberOfEnqueued) {
                    //System.err.println("currentRequest="+currentRequest+" count="+numberOfEnqueued);
                }
            };
        }
        
        @Override
        public void suspended(DebugContext debugContext) {
            state = DebuggerState.SUSPENDED;
            savedDebugContext = debugContext;
            semaphore.release();
            fireStateChange();
        }

        public DebugContext getSavedDebugContext() {
            return savedDebugContext;
        }

        public Semaphore getSemaphore() {
            return semaphore;
        }
        
        @Override
        public void scriptLoaded(Script script) {
        }

        public DebuggerState getState() {
            return state;
        }

        @Override
        public void resumed() {
            state = DebuggerState.RUNNING;
            fireStateChange();
        }

        @Override
        public void disconnected() {
            state = DebuggerState.DISCONNECTED;
            fireStateChange();
        }

        @Override
        public void scriptCollected(Script script) {
        }

        @Override
        public VmStatusListener getVmStatusListener() {
            return list;
        }

        @Override
        public void scriptContentChanged(Script script) {
        }
    }
    
    private class LoggerFactoryImpl implements WipBrowserFactory.LoggerFactory {

        private ConnectionLoggerImpl logger = new ConnectionLoggerImpl();
        
        @Override
        public ConnectionLogger newBrowserConnectionLogger() {
            return logger;
        }

        @Override
        public ConnectionLogger newTabConnectionLogger() {
            return logger;
        }
        
    }
    
    private class ConnectionLoggerImpl implements ConnectionLogger {

        private ConnectionLogger.ConnectionCloser connectionCloser;
        
        @Override
        public ConnectionLogger.StreamListener getIncomingStreamListener() {
            return new StreamListenerImpl();
        }

        @Override
        public ConnectionLogger.StreamListener getOutgoingStreamListener() {
            return new StreamListenerImpl();
        }

        @Override
        public void setConnectionCloser(ConnectionLogger.ConnectionCloser connectionCloser) {
            this.connectionCloser = connectionCloser;
        }

        @Override
        public void start() {
        }

        @Override
        public void handleEos() {
        }
        
    }
    
    private class StreamListenerImpl implements ConnectionLogger.StreamListener {

        @Override
        public void addContent(CharSequence text) {
        //    System.err.println(""+text);
        }

        @Override
        public void addSeparator() {
//            System.err.println("   ------ ");
        }
        
    }
}
