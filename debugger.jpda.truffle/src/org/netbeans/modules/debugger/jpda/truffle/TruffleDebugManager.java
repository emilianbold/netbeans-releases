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

package org.netbeans.modules.debugger.jpda.truffle;

import com.sun.jdi.ClassType;
import com.sun.jdi.request.EventRequest;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngineFactory;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;

/**
 *
 * @author Martin
 */
@DebuggerServiceRegistration(types=LazyDebuggerManagerListener.class)
public class TruffleDebugManager extends DebuggerManagerAdapter {
    
    private static final Logger LOG = Logger.getLogger(TruffleDebugManager.class.getName());
    
    //public static final String TRUFFLE_CLASS_DebugManager = "com.oracle.truffle.debug.DebugManager";
    //public static final String TRUFFLE_CLASS_DebugManager = "com.oracle.truffle.js.engine.TruffleJSEngine";
    //private static final String SCRIPT_CREATION_BP_CLASS = "com.oracle.truffle.api.script.TruffleScriptEngineFactory";
    //private static final String SCRIPT_CREATION_BP_METHOD = "engineCreated";
    private static final String SESSION_CREATION_BP_CLASS = "com.oracle.truffle.api.vm.PolyglotEngine";
    private static final String SESSION_CREATION_BP_METHOD = "dispatchExecutionEvent";
    
    private JPDABreakpoint debugManagerLoadBP;
    private static final Map<JPDADebugger, DebugManagerHandler> dmHandlers = new HashMap<>();
    
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
//        debugManagerLoadBP = ClassLoadUnloadBreakpoint.create(TRUFFLE_CLASS_DebugManager,
//                                                    false,
//                                                    ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED);
        //debugManagerLoadBP = MethodBreakpoint.create(TRUFFLE_CLASS_DebugManager, "<init>");
        debugManagerLoadBP = MethodBreakpoint.create(SESSION_CREATION_BP_CLASS, SESSION_CREATION_BP_METHOD);
        ((MethodBreakpoint) debugManagerLoadBP).setBreakpointType(MethodBreakpoint.TYPE_METHOD_ENTRY);
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
        LOG.log(Level.FINE, "TruffleDebugManager.sessionAdded({0}), adding BP listener to {1}", new Object[]{session, debugManagerLoadBP});
        DebugManagerHandler dmh = new DebugManagerHandler(debugger);
        debugManagerLoadBP.addJPDABreakpointListener(dmh);
        /*debugManagerLoadBP.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                System.err.println(debugManagerLoadBP+" has changed: "+evt);
                System.err.println("  prop name = "+evt.getPropertyName()+", new value = "+evt.getNewValue());
                Thread.dumpStack();
            }
        });*/
        synchronized (dmHandlers) {
            dmHandlers.put(debugger, dmh);
        }
    }

    @Override
    public void sessionRemoved(Session session) {
        JPDADebugger debugger = session.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) {
            return ;
        }
        DebugManagerHandler dmh;
        synchronized (dmHandlers) {
            dmh = dmHandlers.remove(debugger);
        }
        if (dmh != null) {
            LOG.log(Level.FINE, "TruffleDebugManager.engineRemoved({0}), removing BP listener from {1}", new Object[]{session, debugManagerLoadBP});
            debugManagerLoadBP.removeJPDABreakpointListener(dmh);
            dmh.destroy();
        }
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
