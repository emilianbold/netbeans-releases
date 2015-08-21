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
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.breakpoints.BreakpointsEngineListener;
import org.netbeans.modules.debugger.jpda.breakpoints.MethodBreakpointImpl;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.IntegerValueWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LongValueWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.truffle.RemoteServices;
import org.netbeans.modules.debugger.jpda.truffle.TruffleDebugManager;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackInfo;
import org.netbeans.modules.debugger.jpda.truffle.source.Source;
import org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleSlotVariable;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin
 */
public class TruffleAccess implements JPDABreakpointListener {
    
    private static final Logger LOG = Logger.getLogger(TruffleAccess.class.getName());
    
    public static final String BASIC_CLASS_NAME = "org.netbeans.modules.debugger.jpda.backend.truffle.JPDATruffleAccessor";    // NOI18N
    private static final String HALTED_CLASS_NAME = "com.oracle.truffle.api.vm.TruffleVM";  // NOI18N
    
    private static final String METHOD_EXEC_HALTED = "dispatchSuspendedEvent";  // NOI18N
    private static final String METHOD_EXEC_STEP_INTO = "executionStepInto";    // NOI18N
    private static final String METHOD_DEBUGGER_ACCESS = "debuggerAccess";      // NOI18N
    
    private static final String METHOD_GET_SOURCE_POSITION = "getSourcePosition";   // NOI18N
    //private static final String METHOD_GET_SOURCE_POSITION_SGN = "(Ljava/lang/Object;)Lorg/netbeans/modules/debugger/jpda/backend/truffle/Sourceposition;"; // NOI18N
    private static final String METHOD_GET_SOURCE_POSITION_SGN = "(Ljava/lang/Object;)Ljava/lang/Object;"; // NOI18N
    private static final String METHOD_GET_FRAME_INFO = "getFrameInfo";         // NOI18N
    //private static final String METHOD_GET_FRAME_INFO_SGN = "(Ljava/lang/Object;)Lorg/netbeans/modules/debugger/jpda/backend/truffle/FrameInfo;";   // NOI18N
    private static final String METHOD_GET_FRAME_INFO_SGN = "(Ljava/lang/Object;)Ljava/lang/Object;";   // NOI18N
    private static final String METHOD_GET_SLOT_VALUE = "getSlotValue";         // NOI18N
    
    private static final String VAR_NODE = "astNode";                           // NOI18N
    private static final String VAR_FRAME = "frame";                            // NOI18N
    private static final String VAR_SRC_ID = "srcId";
    private static final String VAR_SRC_NAME = "srcName";
    private static final String VAR_SRC_PATH = "srcPath";
    private static final String VAR_SRC_LINE = "line";
    private static final String VAR_SRC_CODE = "code";
    private static final String VAR_FRAME_SLOTS = "frameSlots";
    private static final String VAR_SLOT_NAMES = "slotNames";
    private static final String VAR_SLOT_TYPES = "slotTypes";                   // NOI18N
    private static final String VAR_STACK_TRACE = "stackTrace";
    private static final String VAR_TOP_FRAME = "topFrame";                     // NOI18N
    private static final String VAR_THIS_OBJECT = "thisObject";                 // NOI18N
    
    private static final String METHOD_GET_FRAME_SLOTS = "getFrameSlots";       // NOI18N
    private static final String METHOD_GET_FRAME_SLOTS_SGN = "(Lcom/oracle/truffle/api/frame/FrameInstance;)[Ljava/lang/Object;";   // NOI18N
    
    private static final Map<JPDADebugger, CurrentPCInfo> currentPCInfos = new WeakHashMap<>();
    
    private static final TruffleAccess DEFAULT = new TruffleAccess();

    private JPDABreakpoint execHaltedBP;
    private JPDABreakpoint execStepIntoBP;
    private JPDABreakpoint dbgAccessBP;
    
    private final Object methodCallAccessLock = new Object();//new ReentrantReadWriteLock(true).writeLock();
    private MethodCallsAccess methodCallsRunnable;
    private static final MethodCallsAccess METHOD_CALLS_SUCCESSFUL = new MethodCallsAccess(){@Override public void callMethods(JPDAThread thread) {}};
    
    private TruffleAccess() {}
    
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
        execHaltedBP = createBP(HALTED_CLASS_NAME, METHOD_EXEC_HALTED);
        execStepIntoBP = createBP(METHOD_EXEC_STEP_INTO);
        dbgAccessBP = createBP(METHOD_DEBUGGER_ACCESS);
    }
    
    private JPDABreakpoint createBP(String methodName) {
        return createBP(BASIC_CLASS_NAME, methodName);
    }
    
    private JPDABreakpoint createBP(String className, String methodName) {
        final MethodBreakpoint mb = MethodBreakpoint.create(className, methodName);
        mb.setBreakpointType(MethodBreakpoint.TYPE_METHOD_ENTRY);
        mb.setHidden(true);
        //mb.setSession( );
        mb.addJPDABreakpointListener(this);
        DebuggerManager.getDebuggerManager().addBreakpoint(mb);
        /*
        mb.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                System.err.println(mb+" has changed: "+evt);
                System.err.println("  prop name = "+evt.getPropertyName()+", new value = "+evt.getNewValue());
                Thread.dumpStack();
            }
        });
        */
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
            LOG.log(Level.FINE, "TruffleAccessBreakpoints.breakpointReached({0}), exec halted.", event);
            CurrentPCInfo cpci = getCurrentPosition(event.getDebugger(), event.getThread());
            synchronized (currentPCInfos) {
                currentPCInfos.put(event.getDebugger(), cpci);
            }
        } else if (execStepIntoBP == bp) {
            LOG.log(Level.FINE, "TruffleAccessBreakpoints.breakpointReached({0}), exec step into.", event);
            CurrentPCInfo cpci = getCurrentPosition(event.getDebugger(), event.getThread());
            synchronized (currentPCInfos) {
                currentPCInfos.put(event.getDebugger(), cpci);
            }
        } else if (dbgAccessBP == bp) {
            LOG.log(Level.FINE, "TruffleAccessBreakpoints.breakpointReached({0}), debugger access.", event);
            try {
                synchronized (methodCallAccessLock) {
                    if (methodCallsRunnable != null) {
                        invokeMethodCalls(event.getThread(), methodCallsRunnable);
                    }
                    methodCallsRunnable = METHOD_CALLS_SUCCESSFUL;
                    methodCallAccessLock.notifyAll();
                }
            } finally {
                event.resume();
            }
        }
    }
    
    private CurrentPCInfo getCurrentPosition(JPDADebugger debugger, JPDAThread thread) {
        try {
            CallStackFrame csf = thread.getCallStack(0, 1)[0];
            LocalVariable[] localVariables = csf.getLocalVariables();
            if (localVariables.length < 1) {
                throw new IllegalStateException("No local vars when searching for the current position.");
            }
            Variable suspendedInfo = localVariables[0];
            JPDAClassType debugAccessor = TruffleDebugManager.getDebugAccessorJPDAClass(debugger);
            ObjectVariable sourcePositionVar = (ObjectVariable) debugAccessor.invokeMethod(
                    METHOD_GET_SOURCE_POSITION, METHOD_GET_SOURCE_POSITION_SGN,
                    new Variable[] { suspendedInfo });
            long id = (Long) sourcePositionVar.getField("id").createMirrorObject();
            int line = (Integer) sourcePositionVar.getField("line").createMirrorObject();
            Source src = Source.getExistingSource(debugger, id);
            if (src == null) {
                String name = (String) sourcePositionVar.getField("name").createMirrorObject();
                String path = (String) sourcePositionVar.getField("path").createMirrorObject();
                //String code = (String) sourcePositionVar.getField("code").createMirrorObject();
                StringReference codeRef = (StringReference) ((JDIVariable) sourcePositionVar.getField("code")).getJDIValue();
                src = Source.getSource(debugger, id, name, path, codeRef);
            }
            SourcePosition sp = new SourcePosition(debugger, id, src, line);
            
            ObjectVariable frameInfoVar = (ObjectVariable) debugAccessor.invokeMethod(
                    METHOD_GET_FRAME_INFO, METHOD_GET_FRAME_INFO_SGN,
                    new Variable[] { suspendedInfo });
            ObjectVariable frame = (ObjectVariable) frameInfoVar.getField("frame");
            Variable[] frameSlots = ((ObjectVariable) frameInfoVar.getField("slots")).getFields(0, Integer.MAX_VALUE);
            String[] slotNames = (String[]) frameInfoVar.getField("slotNames").createMirrorObject();
            String[] slotTypes = (String[]) frameInfoVar.getField("slotTypes").createMirrorObject();
            TruffleSlotVariable[] vars = createVars(debugger, frame, frameSlots, slotNames, slotTypes);
            ObjectVariable stackTrace = (ObjectVariable) frameInfoVar.getField("stackTrace");
            String topFrameDescription = (String) frameInfoVar.getField("topFrame").createMirrorObject();
            ObjectVariable thisObject = (ObjectVariable) frameInfoVar.getField("thisObject");
            TruffleStackFrame topFrame = new TruffleStackFrame(debugger, 0, stackTrace, topFrameDescription, null/*code*/, vars, thisObject);
            TruffleStackInfo stack = new TruffleStackInfo(debugger, frameSlots, stackTrace);
            return new CurrentPCInfo(suspendedInfo, thread, sp, vars, topFrame, stack);
        } catch (AbsentInformationException | IllegalStateException |
                 InvalidExpressionException | NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        //} catch (AbsentInformationException | InternalExceptionWrapper | VMDisconnectedExceptionWrapper ex) {
            return null;
        }
    }
    
    /*
    private CurrentPCInfo getCurrentPosition_OLD(JPDADebugger debugger, JPDAThread thread) {
        //executionHalted(Node astNode, MaterializedFrame frame,
        //                long srcId, String srcName, String srcPath, int line, String code,
        //                FrameSlot[] frameSlots, String[] slotNames, String[] slotTypes,
        //                FrameInstance[] stackTrace, String topFrame) 
        try {
            CallStackFrame csf = thread.getCallStack(0, 1)[0];
            LocalVariable[] localVariables = csf.getLocalVariables();
            long id = -1;
            int line = -1;
            Variable[] frameSlots = null;
            String[] slotNames = null;
            String[] slotTypes = null;
            //Variable[] stackTrace = null;
            ObjectVariable stackTrace = null;
            /*
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
                TruffleStackInfo stack = new TruffleStackInfo(frameSlots, stackTrace);
                return new CurrentPCInfo(thread, sp, stack);
            } else {
                return null;
            }
            *//*
            ObjectVariable frame = null;
            StringReference name = null;
            StringReference path = null;
            StringReference code = null;
            String topFrameDescription = null;
            ObjectVariable thisObject = null;
            for (LocalVariable lv : localVariables) {
                switch (lv.getName()) {
                    case VAR_FRAME:     frame = (ObjectVariable) lv;
                                        break;
                    case VAR_SRC_ID:    Value jdiValue = ((JDIVariable) lv).getJDIValue();
                                        if (jdiValue instanceof LongValue) {
                                            id = LongValueWrapper.value((LongValue) jdiValue);
                                        }
                                        break;
                    case VAR_SRC_NAME:  name = (StringReference) ((JDIVariable) lv).getJDIValue();
                                        break;
                    case VAR_SRC_PATH:  path = (StringReference) ((JDIVariable) lv).getJDIValue();
                                        break;
                    case VAR_SRC_CODE:  code = (StringReference) ((JDIVariable) lv).getJDIValue();
                                        break;
                    case VAR_SRC_LINE:  jdiValue = ((JDIVariable) lv).getJDIValue();
                                        if (jdiValue instanceof IntegerValue) {
                                            line = IntegerValueWrapper.value((IntegerValue) jdiValue);
                                        }
                                        break;
                    case VAR_FRAME_SLOTS:frameSlots = ((ObjectVariable) lv).getFields(0, Integer.MAX_VALUE);
                                        break;
                    case VAR_SLOT_NAMES:slotNames = (String[]) lv.createMirrorObject();
                                        break;
                    case VAR_SLOT_TYPES:slotTypes = (String[]) lv.createMirrorObject();
                                        break;
                    case VAR_STACK_TRACE:stackTrace = (ObjectVariable) lv;//((ObjectVariable) lv).getFields(0, Integer.MAX_VALUE);
                                        break;
                    case VAR_TOP_FRAME: topFrameDescription = (String) lv.createMirrorObject();
                                        break;
                    case VAR_THIS_OBJECT:thisObject = (ObjectVariable) lv;
                                        break;
                }
            }
            if (id >= 0 && line >= 0) {
                Source src = Source.getExistingSource(debugger, id);
                if (src == null) {
                    src = Source.getSource(debugger, id, name, path, code);
                }
                SourcePosition sp = new SourcePosition(debugger, id, src, line);
                if (frameSlots == null) {
                    frameSlots = new Variable[]{};
                }
                TruffleSlotVariable[] vars = createVars(debugger, frame, frameSlots, slotNames, slotTypes);
                TruffleStackFrame topFrame = new TruffleStackFrame(debugger, 0, stackTrace, topFrameDescription, code, vars, thisObject);
                TruffleStackInfo stack = new TruffleStackInfo(debugger, frameSlots, stackTrace);
                return new CurrentPCInfo(thread, sp, vars, topFrame, stack);
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
    */

    private static TruffleSlotVariable[] createVars(JPDADebugger debugger,
                                                    ObjectVariable frame,
                                                    Variable[] frameSlots,
                                                    String[] slotNames,
                                                    String[] slotTypes) {
        int n = frameSlots.length;
        TruffleSlotVariable[] vars = new TruffleSlotVariable[n];
        for (int i = 0; i < n; i++) {
            vars[i] = new TruffleSlotVariable(debugger, frame, (ObjectVariable) frameSlots[i],
                                              slotNames[i], slotTypes[i]);
        }
        return vars;
    }
    
    /*
    public static TruffleSlotVariable[] createVars(final JPDADebugger debugger, final Variable frameInstance) {
        final TruffleSlotVariable[][] varsPtr = new TruffleSlotVariable[][] { null };
        methodCallingAccess(debugger, new MethodCallsAccess() {
            @Override
            public void callMethods(JPDAThread thread) {
                JPDAClassType debugAccessor = TruffleDebugManager.getDebugAccessorJPDAClass(debugger);
                try {
                    Variable frameSlotsVar = debugAccessor.invokeMethod(METHOD_GET_FRAME_SLOTS,
                                                                        METHOD_GET_FRAME_SLOTS_SGN,
                                                                        new Variable[] { frameInstance });
                    Field[] slots = ((ObjectVariable) frameSlotsVar).getFields(0, Integer.MAX_VALUE);
                    /*
                    slots[0] = frame;
                    slots[1] = frameSlots;
                    slots[2] = slotNames;
                    slots[3] = slotTypes;
                    *//*
                    TruffleSlotVariable[] vars =
                            createVars(debugger, (ObjectVariable) slots[0],
                                       ((ObjectVariable) slots[1]).getFields(0, Integer.MAX_VALUE),
                                       (String[]) slots[2].createMirrorObject(),
                                       (String[]) slots[3].createMirrorObject());
                    varsPtr[0] = vars;
                } catch (InvalidExpressionException | NoSuchMethodException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        TruffleSlotVariable[] vars = varsPtr[0];
        if (vars == null) {
            return new TruffleSlotVariable[] {};
        } else {
            return vars;
        }
    }
    */
    
    public static TruffleSlotVariable[] createVars(final JPDADebugger debugger, final Variable frameInstance) {
        JPDAClassType debugAccessor = TruffleDebugManager.getDebugAccessorJPDAClass(debugger);
        try {
            Variable frameSlotsVar = debugAccessor.invokeMethod(METHOD_GET_FRAME_SLOTS,
                                                                METHOD_GET_FRAME_SLOTS_SGN,
                                                                new Variable[] { frameInstance });
            Field[] slots = ((ObjectVariable) frameSlotsVar).getFields(0, Integer.MAX_VALUE);
            /*
            slots[0] = frame;
            slots[1] = frameSlots;
            slots[2] = slotNames;
            slots[3] = slotTypes;
            */
            TruffleSlotVariable[] vars =
                    createVars(debugger, (ObjectVariable) slots[0],
                               ((ObjectVariable) slots[1]).getFields(0, Integer.MAX_VALUE),
                               (String[]) slots[2].createMirrorObject(),
                               (String[]) slots[3].createMirrorObject());
            return vars;
        } catch (InvalidExpressionException | NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
            return new TruffleSlotVariable[] {};
        }
    }
    
    /**
     * Safe access to method calls in the backend accessor class.
     * @param methodCalls The runnable, that is called under write lock on the current thread.
     * @return <code>true</code> when the runnable with method calls is executed,
     *         <code>false</code> when method execution is not possible.
     */
    public static boolean methodCallingAccess(final JPDADebugger debugger, MethodCallsAccess methodCalls) {
        synchronized (DEFAULT.methodCallAccessLock) {
            while (DEFAULT.methodCallsRunnable != null) {
                // we're already processing some method calls...
                try {
                    DEFAULT.methodCallAccessLock.wait();
                } catch (InterruptedException ex) {
                    return false;
                }
            }
            CurrentPCInfo currentPCInfo = getCurrentPCInfo(debugger);
            if (currentPCInfo != null) {
                JPDAThread thread = currentPCInfo.getThread();
                if (thread != null) {
                    boolean success = invokeMethodCalls(thread, methodCalls);
                    if (success) {
                        return true;
                    }
                }
            }
            // Was not able to invoke methods
            boolean interrupted = RemoteServices.interruptServiceAccessThread(debugger);
            if (!interrupted) {
                return false;
            }
            PropertyChangeListener finishListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (JPDADebugger.STATE_DISCONNECTED == debugger.getState()) {
                        synchronized (DEFAULT.methodCallAccessLock) {
                            DEFAULT.methodCallAccessLock.notifyAll();
                        }
                    }
                }
            };
            debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE, finishListener);
            DEFAULT.methodCallsRunnable = methodCalls;
            try {
                DEFAULT.methodCallAccessLock.wait();
            } catch (InterruptedException ex) {
                return false;
            } finally {
                debugger.removePropertyChangeListener(JPDADebugger.PROP_STATE, finishListener);
            }
            boolean success = (DEFAULT.methodCallsRunnable == METHOD_CALLS_SUCCESSFUL);
            DEFAULT.methodCallsRunnable = null;
            return success;
        }
    }
    
    private static boolean invokeMethodCalls(JPDAThread thread, MethodCallsAccess methodCalls) {
        assert Thread.holdsLock(DEFAULT.methodCallAccessLock);
        boolean invoking = false;
        try {
            ((JPDAThreadImpl) thread).notifyMethodInvoking();
            invoking = true;
            methodCalls.callMethods(thread);
            return true;
        } catch (PropertyVetoException pvex) {
            return false;
        } finally {
            if (invoking) {
                ((JPDAThreadImpl) thread).notifyMethodInvokeDone();
            }
        }
    }
    
    public static interface MethodCallsAccess {
        
        void callMethods(JPDAThread thread);
        
    }
}
