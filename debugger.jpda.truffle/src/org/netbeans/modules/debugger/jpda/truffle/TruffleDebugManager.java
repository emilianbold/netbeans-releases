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

import com.sun.jdi.request.EventRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;

/**
 *
 * @author Martin
 */
@DebuggerServiceRegistration(types=LazyDebuggerManagerListener.class)
public class TruffleDebugManager extends DebuggerManagerAdapter {
    
    //public static final String TRUFFLE_CLASS_DebugManager = "com.oracle.truffle.debug.DebugManager";
    public static final String TRUFFLE_CLASS_DebugManager = "com.oracle.truffle.js.engine.TruffleJSEngine";
    
    private JPDABreakpoint debugManagerLoadBP;
    private final Map<JPDADebugger, DebugManagerHandler> dmHandlers = new HashMap<>();
    
    public TruffleDebugManager() {
    }
    
    @Override
    public Breakpoint[] initBreakpoints() {
//        debugManagerLoadBP = ClassLoadUnloadBreakpoint.create(TRUFFLE_CLASS_DebugManager,
//                                                    false,
//                                                    ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED);
        debugManagerLoadBP = MethodBreakpoint.create(TRUFFLE_CLASS_DebugManager, "<init>");
        ((MethodBreakpoint) debugManagerLoadBP).setBreakpointType(MethodBreakpoint.TYPE_METHOD_EXIT);
        debugManagerLoadBP.setHidden(true);
        debugManagerLoadBP.setSuspend(EventRequest.SUSPEND_ALL);
        return new Breakpoint[] { debugManagerLoadBP };
    }
    
    @Override
    public void engineAdded(DebuggerEngine engine) {
        JPDADebugger debugger = engine.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) {
            return ;
        }
        DebugManagerHandler dmh = new DebugManagerHandler(debugger);
        debugManagerLoadBP.addJPDABreakpointListener(dmh);
        synchronized (dmHandlers) {
            dmHandlers.put(debugger, dmh);
        }
    }
    
    @Override
    public void engineRemoved(DebuggerEngine engine) {
        JPDADebugger debugger = engine.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) {
            return ;
        }
        DebugManagerHandler dmh;
        synchronized (dmHandlers) {
            dmh = dmHandlers.remove(debugger);
        }
        if (dmh != null) {
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
    
}
