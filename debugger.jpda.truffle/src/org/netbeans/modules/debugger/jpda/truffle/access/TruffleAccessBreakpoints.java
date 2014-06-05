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

package org.netbeans.modules.debugger.jpda.truffle.access;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassType;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.LongValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.breakpoints.BreakpointsEngineListener;
import org.netbeans.modules.debugger.jpda.breakpoints.MethodBreakpointImpl;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.jdi.IntegerValueWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LongValueWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.truffle.source.Source;
import org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition;

/**
 *
 * @author Martin
 */
public class TruffleAccessBreakpoints implements JPDABreakpointListener {
    
    public static final String BASIC_CLASS_NAME = "org.netbeans.modules.debugger.jpda.backend.truffle.JPDATruffleAccessor";    // NOI18N
    
    private static final String METHOD_EXEC_HALTED = "executionHalted";         // NOI18N
    private static final String METHOD_EXEC_STEP_INTO = "executionStepInto";    // NOI18N
    private static final String METHOD_DEBUGGER_ACCESS = "debuggerAccess";      // NOI18N
    private static final String VAR_NODE = "astNode";                           // NOI18N
    private static final String VAR_SRC_ID = "srcId";
    private static final String VAR_SRC_NAME = "srcName";
    private static final String VAR_SRC_PATH = "srcPath";
    private static final String VAR_SRC_LINE = "line";
    private static final String VAR_SRC_CODE = "code";
    
    private static final Map<JPDADebugger, CurrentPCInfo> currentPCInfos = new WeakHashMap<>();
    
    private static final TruffleAccessBreakpoints DEFAULT = new TruffleAccessBreakpoints();

    private JPDABreakpoint execHaltedBP;
    private JPDABreakpoint execStepIntoBP;
    private JPDABreakpoint dbgAccessBP;
    
    private TruffleAccessBreakpoints() {}
    
    public static void init() {
        DEFAULT.initBPs();
    }
    
    public static void assureBPSet(JPDADebugger debugger, ClassType accessorClass) {
        if (DEFAULT.execHaltedBP.getValidity() != Breakpoint.VALIDITY.VALID) {
            BreakpointsEngineListener breakpointsEngineListener = getBreakpointsEngineListener(debugger);
            List<ReferenceType> classes = Collections.singletonList((ReferenceType) accessorClass);
            MethodBreakpointImpl mbimpl = (MethodBreakpointImpl) breakpointsEngineListener.getBreakpointImpl(DEFAULT.execHaltedBP);
            assureClassLoaded(mbimpl, classes);
            mbimpl = (MethodBreakpointImpl) breakpointsEngineListener.getBreakpointImpl(DEFAULT.execStepIntoBP);
            assureClassLoaded(mbimpl, classes);
            mbimpl = (MethodBreakpointImpl) breakpointsEngineListener.getBreakpointImpl(DEFAULT.dbgAccessBP);
            assureClassLoaded(mbimpl, classes);
        }
    }
    
    private static void assureClassLoaded(MethodBreakpointImpl mbimpl, List<ReferenceType> classes) {
        try {
            Method classLoadedMethod = MethodBreakpointImpl.class.getDeclaredMethod("classLoaded", List.class);
            classLoadedMethod.setAccessible(true);
            classLoadedMethod.invoke(mbimpl, classes);
        } catch (Exception ex) {}
    }
    
    private static BreakpointsEngineListener getBreakpointsEngineListener(JPDADebugger debugger) {
        List<? extends LazyActionsManagerListener> lamls =
                ((JPDADebuggerImpl) debugger).getSession().lookup(null, LazyActionsManagerListener.class);
        for (LazyActionsManagerListener laml : lamls) {
            if (laml instanceof BreakpointsEngineListener) {
                return (BreakpointsEngineListener) laml;
            }
        }
        return null;
    }
    
    private void initBPs() {
        execHaltedBP = createBP(METHOD_EXEC_HALTED);
        execStepIntoBP = createBP(METHOD_EXEC_STEP_INTO);
        dbgAccessBP = createBP(METHOD_DEBUGGER_ACCESS);
    }
    
    private JPDABreakpoint createBP(String methodName) {
        final MethodBreakpoint mb = MethodBreakpoint.create(BASIC_CLASS_NAME, methodName);
        mb.setBreakpointType(MethodBreakpoint.TYPE_METHOD_ENTRY);
        mb.setHidden(true);
        //mb.setSession( );
        mb.addJPDABreakpointListener(this);
        DebuggerManager.getDebuggerManager().addBreakpoint(mb);
        mb.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                System.err.println(mb+" has changed: "+evt);
                System.err.println("  prop name = "+evt.getPropertyName()+", new value = "+evt.getNewValue());
                Thread.dumpStack();
            }
        });
        
        return mb;
    }
    
    public static CurrentPCInfo getCurrentPCInfo(JPDADebugger dbg) {
        synchronized (currentPCInfos) {
            return currentPCInfos.get(dbg);
        }
    }

    @Override
    public void breakpointReached(JPDABreakpointEvent event) {
        Object bp = event.getSource();
        if (execHaltedBP == bp) {
            System.err.println("TruffleAccessBreakpoints.breakpointReached("+event+"), exec halted.");
            SourcePosition sp = getPosition(event.getDebugger(), event.getThread());
            CurrentPCInfo cpci = new CurrentPCInfo(event.getThread(), sp);
            synchronized (currentPCInfos) {
                currentPCInfos.put(event.getDebugger(), cpci);
            }
        } else if (execStepIntoBP == bp) {
            System.err.println("TruffleAccessBreakpoints.breakpointReached("+event+"), exec step into.");
            SourcePosition sp = getPosition(event.getDebugger(), event.getThread());
            CurrentPCInfo cpci = new CurrentPCInfo(event.getThread(), sp);
            synchronized (currentPCInfos) {
                currentPCInfos.put(event.getDebugger(), cpci);
            }
        } else if (dbgAccessBP == bp) {
            System.err.println("TruffleAccessBreakpoints.breakpointReached("+event+"), debugger access.");
        }
    }
    
    private SourcePosition getPosition(JPDADebugger debugger, JPDAThread thread) {
        try {
            CallStackFrame csf = thread.getCallStack(0, 1)[0];
            LocalVariable[] localVariables = csf.getLocalVariables();
            long id = -1;
            int line = -1;
            for (LocalVariable lv : localVariables) {
                String name = lv.getName();
                if (VAR_SRC_ID.equals(name)) {
                    Value jdiValue = ((JDIVariable) lv).getJDIValue();
                    if (jdiValue instanceof LongValue) {
                        id = LongValueWrapper.value((LongValue) jdiValue);
                        if (line >= 0) {
                            break;
                        }
                    }
                } else if (VAR_SRC_LINE.equals(name)) {
                    line = getInt((JDIVariable) lv);
                    if (id >= 0) {
                        break;
                    }
                }
            }
            if (id >= 0 && line >= 0) {
                Source src = Source.getExistingSource(debugger, id);
                //SourcePosition sp = SourcePosition.getExisting(debugger, id);
                if (src == null) {
                    StringReference name = null;
                    StringReference path = null;
                    StringReference code = null;
                    for (LocalVariable lv : localVariables) {
                        switch (lv.getName()) {
                            case VAR_SRC_NAME: name = (StringReference) ((JDIVariable) lv).getJDIValue();
                                               break;
                            case VAR_SRC_PATH: path = (StringReference) ((JDIVariable) lv).getJDIValue();
                                               break;
                            case VAR_SRC_CODE: code = (StringReference) ((JDIVariable) lv).getJDIValue();
                                               break;
                        }
                    }
                    src = Source.getSource(debugger, id, name, path, code);
                }
                SourcePosition sp = new SourcePosition(debugger, id, src, line);
                return sp;
            } else {
                return null;
            }
        } catch (AbsentInformationException | InternalExceptionWrapper | VMDisconnectedExceptionWrapper ex) {
            return null;
        }
    }
    
    private static int getInt(JDIVariable var) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        Value jdiValue = var.getJDIValue();
        return IntegerValueWrapper.value((IntegerValue) jdiValue);
    }
}
