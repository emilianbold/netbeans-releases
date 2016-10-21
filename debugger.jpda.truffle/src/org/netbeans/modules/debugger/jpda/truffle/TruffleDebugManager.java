/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.truffle;

import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidRequestStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MethodWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StackFrameWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.LocatableEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAClassTypeImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.util.Executor;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin
 */
@DebuggerServiceRegistration(types=LazyDebuggerManagerListener.class)
public class TruffleDebugManager extends DebuggerManagerAdapter {
    
    private static final Logger LOG = Logger.getLogger(TruffleDebugManager.class.getName());
    
    private static final String SESSION_CREATION_BP_CLASS = "com.oracle.truffle.api.vm.PolyglotEngine";
    
    private JPDABreakpoint debugManagerLoadBP;
    private static final Map<JPDADebugger, DebugManagerHandler> dmHandlers = new HashMap<>();
    private static final Map<JPDADebugger, JPDABreakpointListener> debugBPListeners = new HashMap<>();
    
    public TruffleDebugManager() {
    }
    
    @Override
    public Breakpoint[] initBreakpoints() {
        initLoadBP();
        return new Breakpoint[] { debugManagerLoadBP };
    }
    
    private synchronized void initLoadBP() {
        if (debugManagerLoadBP != null) {
            return ;
        }
        /* Must NOT use a method exit breakpoint! I cause a massive degradation of application performance.
        debugManagerLoadBP = MethodBreakpoint.create(SESSION_CREATION_BP_CLASS, SESSION_CREATION_BP_METHOD);
        ((MethodBreakpoint) debugManagerLoadBP).setBreakpointType(MethodBreakpoint.TYPE_METHOD_EXIT);
        */
        debugManagerLoadBP = ClassLoadUnloadBreakpoint.create(SESSION_CREATION_BP_CLASS, false, ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED);
        debugManagerLoadBP.setHidden(true);
        
        LOG.log(Level.FINE, "TruffleDebugManager.initBreakpoints(): submitted BP {0}", debugManagerLoadBP);
        TruffleAccess.init();
    }

    @Override
    public void sessionAdded(Session session) {
        JPDADebugger debugger = session.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) {
            return ;
        }
        synchronized (dmHandlers) {
            if (dmHandlers.containsKey(debugger)) {
                // A new session for the same debugger?
                return ;
            }
        }
        initLoadBP();
        JPDABreakpointListener bpl = addPolyglotEngineCreationBP(debugger);
        LOG.log(Level.FINE, "TruffleDebugManager.sessionAdded({0}), adding BP listener to {1}", new Object[]{session, debugManagerLoadBP});
        synchronized (debugBPListeners) {
            debugBPListeners.put(debugger, bpl);
        }
    }

    @Override
    public void sessionRemoved(Session session) {
        JPDADebugger debugger = session.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) {
            return ;
        }
        JPDABreakpointListener bpl;
        synchronized (debugBPListeners) {
            bpl = debugBPListeners.remove(debugger);
        }
        if (bpl != null) {
            LOG.log(Level.FINE, "TruffleDebugManager.engineRemoved({0}), removing BP listener from {1}", new Object[]{session, debugManagerLoadBP});
            debugManagerLoadBP.removeJPDABreakpointListener(bpl);
        }
        DebugManagerHandler dmh;
        synchronized (dmHandlers) {
            dmh = dmHandlers.remove(debugger);
        }
        if (dmh != null) {
            LOG.log(Level.FINE, "TruffleDebugManager.engineRemoved({0}), destroying {1}", new Object[]{session, dmh});
            dmh.destroy();
        }
    }

    private JPDABreakpointListener addPolyglotEngineCreationBP(final JPDADebugger debugger) {
        JPDABreakpointListener bpl = new JPDABreakpointListener() {
            @Override
            public void breakpointReached(JPDABreakpointEvent event) {
                try {
                    submitPECreationBP(debugger, event.getReferenceType());
                } finally {
                    event.resume();
                }
            }
        };
        debugManagerLoadBP.addJPDABreakpointListener(bpl);
        List<JPDAClassType> polyglotEngines = debugger.getClassesByName(SESSION_CREATION_BP_CLASS);
        if (!polyglotEngines.isEmpty()) {
            for (JPDAClassType pe : polyglotEngines) {
                submitPECreationBP(debugger, ((JPDAClassTypeImpl) pe).getType());
            }
        }
        return bpl;
    }

    private void submitPECreationBP(final JPDADebugger debugger, ReferenceType type) {
        try {
            List<Method> constructors = ReferenceTypeWrapper.methodsByName(type, "<init>");
            for (Method c : constructors) {
                if (!c.argumentTypeNames().isEmpty()) {
                    Location lastLocation = null;
                    Location l;
                    int i = 0;
                    // Search for the last (return) statement:
                    while ((l = MethodWrapper.locationOfCodeIndex(c, i)) != null) {
                        lastLocation = l;
                        i++;
                    }
                    BreakpointRequest bp = EventRequestManagerWrapper.createBreakpointRequest(lastLocation.virtualMachine().eventRequestManager(), lastLocation);
                    EventRequestWrapper.setSuspendPolicy(bp, EventRequest.SUSPEND_EVENT_THREAD);
                    ((JPDADebuggerImpl) debugger).getOperator().register(bp, new Executor() {
                        @Override
                        public boolean exec(Event event) {
                            try {
                                ThreadReference threadReference = LocatableEventWrapper.thread((LocatableEvent) event);
                                JPDAThreadImpl thread = ((JPDADebuggerImpl) debugger).getThread(threadReference);
                                StackFrame topFrame = ThreadReferenceWrapper.frame(threadReference, 0);
                                ObjectReference engine = StackFrameWrapper.thisObject(topFrame);
                                haveNewPE(debugger, thread, engine);
                            } catch (InternalExceptionWrapper | VMDisconnectedExceptionWrapper |
                                     ObjectCollectedExceptionWrapper ex) {
                            } catch (IllegalThreadStateExceptionWrapper |
                                     IncompatibleThreadStateException |
                                     InvalidStackFrameExceptionWrapper ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            return true;
                        }

                        @Override
                        public void removed(EventRequest eventRequest) {
                        }

                    });
                    try {
                        EventRequestWrapper.enable(bp);
                    } catch (InvalidRequestStateExceptionWrapper irsx) {
                    }
                }
            }
        } catch (InternalExceptionWrapper | VMDisconnectedExceptionWrapper |
                 ObjectCollectedExceptionWrapper | ClassNotPreparedExceptionWrapper ex) {
        }
    }

    private void haveNewPE(JPDADebugger debugger, JPDAThreadImpl thread, ObjectReference engine) {
        DebugManagerHandler dmh;
        synchronized (dmHandlers) {
            dmh = dmHandlers.get(debugger);
            if (dmh == null) {
                dmh = new DebugManagerHandler(debugger);
                dmHandlers.put(debugger, dmh);
            }
        }
        dmh.newPolyglotEngineInstance(engine, thread);
    }

    @Override
    public void breakpointAdded(Breakpoint breakpoint) {
        if (breakpoint instanceof JSLineBreakpoint) {
            Collection<DebugManagerHandler> handlers;
            synchronized (dmHandlers) {
                handlers = new ArrayList(dmHandlers.values());
            }
            for (DebugManagerHandler dmh : handlers) {
                dmh.breakpointAdded((JSLineBreakpoint) breakpoint);
            }
        }
    }

    @Override
    public void breakpointRemoved(Breakpoint breakpoint) {
        if (breakpoint instanceof JSLineBreakpoint) {
            Collection<DebugManagerHandler> handlers;
            synchronized (dmHandlers) {
                handlers = new ArrayList(dmHandlers.values());
            }
            for (DebugManagerHandler dmh : handlers) {
                dmh.breakpointRemoved((JSLineBreakpoint) breakpoint);
            }
        }
    }
    
    public static ClassType getDebugAccessorClass(JPDADebugger debugger) {
        synchronized (dmHandlers) {
            DebugManagerHandler dmh = dmHandlers.get(debugger);
            if (dmh != null) {
                return dmh.getAccessorClass();
            } else {
                return null;
            }
        }
    }
    
    public static JPDAClassType getDebugAccessorJPDAClass(JPDADebugger debugger) {
        synchronized (dmHandlers) {
            DebugManagerHandler dmh = dmHandlers.get(debugger);
            if (dmh != null) {
                return dmh.getAccessorJPDAClass();
            } else {
                return null;
            }
        }
    }

}
