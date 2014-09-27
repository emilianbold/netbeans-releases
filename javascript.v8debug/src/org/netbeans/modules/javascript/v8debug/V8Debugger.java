/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.v8debug;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.lib.v8debug.V8Event;
import org.netbeans.lib.v8debug.V8Response;
import org.netbeans.lib.v8debug.V8Script;
import org.netbeans.lib.v8debug.client.ClientConnection;
import org.netbeans.lib.v8debug.events.AfterCompileEventBody;
import org.netbeans.lib.v8debug.events.BreakEventBody;
import org.netbeans.lib.v8debug.events.ExceptionEventBody;
import org.netbeans.modules.javascript.v8debug.api.Connector;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin Entlicher
 */
public class V8Debugger {
    
    private static final Logger LOG = Logger.getLogger(V8Debugger.class.getName());
    
    private final String host;
    private final int port;
    private final ClientConnection connection;
    private final ScriptsHandler scriptsHandler;
    @NullAllowed
    private final Runnable finishCallback;
    private DebuggerEngine engine;
    private DebuggerEngine.Destructor engineDestructor;
    private final RequestProcessor rp = new RequestProcessor(V8Debugger.class);
    private final Set<Listener> listeners = new LinkedHashSet<>();
    
    private long requestSequence = 1l;
    private volatile boolean finished = false;
    
    public static DebuggerEngine startSession(Connector.Properties properties,
                                              @NullAllowed Runnable finishCallback) throws IOException {
        V8Debugger dbg = new V8Debugger(properties, finishCallback);
        DebuggerInfo dInfo = DebuggerInfo.create(V8DebuggerSessionProvider.DEBUG_INFO, new Object[]{ dbg });
        DebuggerEngine[] engines = DebuggerManager.getDebuggerManager().startDebugging(dInfo);
        if (engines.length > 0) {
            dbg.setEngine(engines[0]);
            return engines[0];
        } else {
            if (finishCallback != null) {
                finishCallback.run();
            }
            return null;
        }
    }

    private V8Debugger(Connector.Properties properties, Runnable finishCallback) throws IOException {
        this.host = properties.getHostName();
        this.port = properties.getPort();
        this.connection = new ClientConnection(properties.getHostName(), properties.getPort());
        this.scriptsHandler = new ScriptsHandler(properties.getLocalPath(), properties.getServerPath());
        this.finishCallback = finishCallback;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
    
    public void start() {
        spawnResponseLoop();
    }
    
    public void finish() {
        if (finished) {
            return ;
        }
        finished = true;
        try {
            connection.close();
        } catch (IOException ioex) {}
        engineDestructor.killEngine();
        if (finishCallback != null) {
            finishCallback.run();
        }
    }

    private void spawnResponseLoop() {
        new RequestProcessor("V8Debugger Response Loop").post(new Runnable() {
            @Override
            public void run() {
                responseLoop();
            }
        });
    }
    
    private void responseLoop() {
        try {
        connection.runEventLoop(new ClientConnection.Listener() {

            @Override
            public void header(Map<String, String> properties) {
                for (Map.Entry pe : properties.entrySet()) {
                    LOG.fine("  "+pe.getKey() + " = " + pe.getValue());
                }
            }
            
            @Override
            public void response(V8Response response) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("response: "+response+", success = "+response.isSuccess()+", running = "+response.isRunning()+", body = "+response.getBody());
                }
                handleResponse(response);
            }

            @Override
            public void event(V8Event event) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("event: "+event+", name = "+event.getKind());
                }
                handleEvent(event);
            }

        });
        } catch (IOException ioex) {
            LOG.log(Level.FINE, null, ioex);
        } finally {
            try {
                connection.close();
            } catch (IOException ioex) {
            } finally {
                finish();
            }
        }
    }
    
    private void handleResponse(V8Response response) {
    }

    private void handleEvent(V8Event event) {
        switch (event.getKind()) {
            case AfterCompile:
                AfterCompileEventBody aceb = (AfterCompileEventBody) event.getBody();
                V8Script script = aceb.getScript();
                scriptsHandler.add(script);
                break;
            case Break:
                BreakEventBody beb = (BreakEventBody) event.getBody();
                //System.out.println("stopped at "+beb.getScript().getName()+", line = "+(beb.getSourceLine()+1)+" : "+beb.getSourceColumn()+"\ntext = "+beb.getSourceLineText());
                break;
            case Exception:
                ExceptionEventBody eeb = (ExceptionEventBody) event.getBody();
                //System.out.println("exception '"+eeb.getException()+"' stopped in "+eeb.getScript().getName()+", line = "+(eeb.getSourceLine()+1)+" : "+eeb.getSourceColumn()+"\ntext = "+eeb.getSourceLineText());
                break;
            default:
                throw new IllegalStateException("Unknown event: "+event.getKind());
        }
    }
    
    public void addListener(Listener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    private void setEngine(DebuggerEngine debuggerEngine) {
        this.engine = debuggerEngine;
    }

    void setEngineDestructor(DebuggerEngine.Destructor destructor) {
        this.engineDestructor = destructor;
    }

    public interface Listener {
        
        void notifySuspended(boolean suspended);
        
        void notifyFinished();
        
    }

    
}
