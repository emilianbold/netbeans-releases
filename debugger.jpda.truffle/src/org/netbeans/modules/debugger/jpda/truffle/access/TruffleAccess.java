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
import com.sun.jdi.InvocationException;
import com.sun.jdi.StringReference;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.DebuggerManager;
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
import org.netbeans.modules.debugger.jpda.expr.InvocationExceptionTranslated;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.truffle.RemoteServices;
import org.netbeans.modules.debugger.jpda.truffle.TruffleDebugManager;
import org.netbeans.modules.debugger.jpda.truffle.actions.StepActionProvider;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackInfo;
import org.netbeans.modules.debugger.jpda.truffle.source.Source;
import org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleStackVariable;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleVariable;
import org.netbeans.modules.debugger.jpda.util.WeakHashMapActive;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin
 */
public class TruffleAccess implements JPDABreakpointListener {
    
    private static final Logger LOG = Logger.getLogger(TruffleAccess.class.getName());
    
    public static final String BASIC_CLASS_NAME = "org.netbeans.modules.debugger.jpda.backend.truffle.JPDATruffleAccessor";    // NOI18N
    private static final String HALTED_CLASS_NAME = BASIC_CLASS_NAME;//"com.oracle.truffle.api.vm.PolyglotEngine";  // NOI18N
    
    private static final String METHOD_EXEC_HALTED = "executionHalted";         // NOI18N
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
    private static final String VAR_SRC_ID = "id";                              // NOI18N
    private static final String VAR_SRC_URI = "uri";                            // NOI18N
    private static final String VAR_SRC_NAME = "name";                          // NOI18N
    private static final String VAR_SRC_PATH = "path";                          // NOI18N
    private static final String VAR_SRC_LINE = "line";                          // NOI18N
    private static final String VAR_SRC_CODE = "code";
    private static final String VAR_FRAME_SLOTS = "frameSlots";
    private static final String VAR_SLOT_NAMES = "slotNames";
    private static final String VAR_SLOT_TYPES = "slotTypes";                   // NOI18N
    private static final String VAR_STACK_TRACE = "stackTrace";
    private static final String VAR_TOP_FRAME = "topFrame";                     // NOI18N
    private static final String VAR_TOP_VARS = "topVariables";                  // NOI18N
    private static final String VAR_THIS_OBJECT = "thisObject";                 // NOI18N
    
    private static final String METHOD_GET_FRAME_SLOTS = "getFrameSlots";       // NOI18N
    private static final String METHOD_GET_FRAME_SLOTS_SGN = "(Lcom/oracle/truffle/api/frame/FrameInstance;)[Ljava/lang/Object;";   // NOI18N
    private static final String METHOD_GET_VARIABLES = "getVariables";          // NOI18N
    private static final String METHOD_GET_VARIABLES_SGN = "(Lcom/oracle/truffle/api/debug/DebugStackFrame;)[Ljava/lang/Object;";  // NOI18N
    
    private static final Map<JPDADebugger, CurrentPCInfo> currentPCInfos = new WeakHashMap<>();
    
    private static final TruffleAccess DEFAULT = new TruffleAccess();

    private final Map<JPDADebugger, JPDABreakpoint> execHaltedBP = new WeakHashMapActive<>();
    private final Map<JPDADebugger, JPDABreakpoint> execStepIntoBP = new WeakHashMapActive<>();
    private final Map<JPDADebugger, JPDABreakpoint> dbgAccessBP = new WeakHashMapActive<>();
    
    private final Object methodCallAccessLock = new Object();//new ReentrantReadWriteLock(true).writeLock();
    private MethodCallsAccess methodCallsRunnable;
    private static final MethodCallsAccess METHOD_CALLS_SUCCESSFUL = new MethodCallsAccess(){@Override public void callMethods(JPDAThread thread) {}};
    
    private TruffleAccess() {}
    
    public static void init() {
        DEFAULT.initBPs();
    }
    
    public static void assureBPSet(JPDADebugger debugger, ClassType accessorClass) {
        DEFAULT.execHaltedBP.put(debugger, DEFAULT.createBP(accessorClass.name(), METHOD_EXEC_HALTED, debugger));
        DEFAULT.execStepIntoBP.put(debugger, DEFAULT.createBP(accessorClass.name(), METHOD_EXEC_STEP_INTO, debugger));
        DEFAULT.dbgAccessBP.put(debugger, DEFAULT.createBP(accessorClass.name(), METHOD_DEBUGGER_ACCESS, debugger));
    }
    
    private void initBPs() {
        // Init debugger session-independent breakpoints
    }
    
    private JPDABreakpoint createBP(String className, String methodName, JPDADebugger debugger) {
        final MethodBreakpoint mb = MethodBreakpoint.create(className, methodName);
        mb.setBreakpointType(MethodBreakpoint.TYPE_METHOD_ENTRY);
        mb.setHidden(true);
        mb.setSession(debugger);
        mb.addJPDABreakpointListener(this);
        DebuggerManager.getDebuggerManager().addBreakpoint(mb);
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
        JPDADebugger debugger = event.getDebugger();
        if (execHaltedBP.get(debugger) == bp) {
            LOG.log(Level.FINE, "TruffleAccessBreakpoints.breakpointReached({0}), exec halted.", event);
            StepActionProvider.killJavaStep(debugger);
            CurrentPCInfo cpci = getCurrentPosition(debugger, event.getThread());
            synchronized (currentPCInfos) {
                currentPCInfos.put(debugger, cpci);
            }
        } else if (execStepIntoBP.get(debugger) == bp) {
            LOG.log(Level.FINE, "TruffleAccessBreakpoints.breakpointReached({0}), exec step into.", event);
            StepActionProvider.killJavaStep(debugger);
            CurrentPCInfo cpci = getCurrentPosition(debugger, event.getThread());
            synchronized (currentPCInfos) {
                currentPCInfos.put(debugger, cpci);
            }
        } else if (dbgAccessBP.get(debugger) == bp) {
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
            ExecutionHaltedInfo haltedInfo = ExecutionHaltedInfo.get(localVariables);
            //JPDAClassType debugAccessor = TruffleDebugManager.getDebugAccessorJPDAClass(debugger);
            ObjectVariable sourcePositionVar = haltedInfo.sourcePositions;
            SourcePosition sp = getSourcePosition(debugger, sourcePositionVar);
            
            ObjectVariable frameInfoVar = haltedInfo.frameInfo;
            ObjectVariable frame = (ObjectVariable) frameInfoVar.getField(VAR_FRAME);
            ObjectVariable topVars = (ObjectVariable) frameInfoVar.getField(VAR_TOP_VARS);
            TruffleVariable[] vars = createVars(debugger, topVars);
            ObjectVariable stackTrace = (ObjectVariable) frameInfoVar.getField(VAR_STACK_TRACE);
            String topFrameDescription = (String) frameInfoVar.getField(VAR_TOP_FRAME).createMirrorObject();
            ObjectVariable thisObject = null;// TODO: (ObjectVariable) frameInfoVar.getField("thisObject");
            TruffleStackFrame topFrame = new TruffleStackFrame(debugger, 0, frame, topFrameDescription, null/*code*/, vars, thisObject, true);
            TruffleStackInfo stack = new TruffleStackInfo(debugger, stackTrace);
            return new CurrentPCInfo(haltedInfo.stepCmd, thread, sp, vars, topFrame, stack);
        } catch (AbsentInformationException | IllegalStateException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    public static SourcePosition getSourcePosition(JPDADebugger debugger, ObjectVariable sourcePositionVar) {
        long id = (Long) sourcePositionVar.getField(VAR_SRC_ID).createMirrorObject();
        int line = (Integer) sourcePositionVar.getField(VAR_SRC_LINE).createMirrorObject();
        Source src = Source.getExistingSource(debugger, id);
        if (src == null) {
            String name = (String) sourcePositionVar.getField(VAR_SRC_NAME).createMirrorObject();
            String path = (String) sourcePositionVar.getField(VAR_SRC_PATH).createMirrorObject();
            URI uri = (URI) sourcePositionVar.getField(VAR_SRC_URI).createMirrorObject();
            StringReference codeRef = (StringReference) ((JDIVariable) sourcePositionVar.getField(VAR_SRC_CODE)).getJDIValue();
            src = Source.getSource(debugger, id, name, path, uri, codeRef);
        }
        return new SourcePosition(debugger, id, src, line);
    }
    
    private static TruffleVariable[] createVars(JPDADebugger debugger, ObjectVariable varsArrVar) {
        Field[] varsArr = varsArrVar.getFields(0, Integer.MAX_VALUE);
        int n = varsArr.length/9;
        TruffleVariable[] vars = new TruffleVariable[n];
        for (int i = 0; i < n; i++) {
            int vi = 9*i;
            String name = (String) varsArr[vi].createMirrorObject();
            String type = (String) varsArr[vi + 1].createMirrorObject();
            boolean writable = (Boolean) varsArr[vi + 2].createMirrorObject();
            String valueStr = (String) varsArr[vi + 3].createMirrorObject();
            Supplier<SourcePosition> valueSource = parseSourceLazy(debugger,
                                                                   varsArr[vi + 4],
                                                                   (JDIVariable) varsArr[vi + 5]);
            Supplier<SourcePosition> typeSource = parseSourceLazy(debugger,
                                                                  varsArr[vi + 6],
                                                                  (JDIVariable) varsArr[vi + 7]);
            ObjectVariable value = (ObjectVariable) varsArr[vi + 8];
            vars[i] = new TruffleStackVariable(debugger, name, type, writable, valueStr,
                                               valueSource, typeSource, value);
        }
        return vars;
    }
    
    private static Supplier<SourcePosition> parseSourceLazy(JPDADebugger debugger, Variable sourceDefVar, JDIVariable codeRefVar) {
        return () -> parseSource(debugger,
                                 (String) sourceDefVar.createMirrorObject(),
                                 (StringReference) codeRefVar.getJDIValue());
    }
    
    private static SourcePosition parseSource(JPDADebugger debugger, String sourceDef, StringReference codeRef) {
        if (sourceDef == null) {
            return null;
        }
        int sourceId;
        String sourceName;
        String sourcePath;
        URI sourceURI;
        int sourceLine;
        try {
            int i1 = 0;
            int i2 = sourceDef.indexOf('\n', i1);
            sourceId = Integer.parseInt(sourceDef.substring(i1, i2));
            i1 = i2 + 1;
            i2 = sourceDef.indexOf('\n', i1);
            sourceName = sourceDef.substring(i1, i2);
            i1 = i2 + 1;
            i2 = sourceDef.indexOf('\n', i1);
            sourcePath = sourceDef.substring(i1, i2);
            i1 = i2 + 1;
            i2 = sourceDef.indexOf('\n', i1);
            try {
                sourceURI = new URI(sourceDef.substring(i1, i2));
            } catch (URISyntaxException usex) {
                throw new IllegalStateException("Bad URI: "+sourceDef.substring(i1, i2), usex);
            }
            i1 = i2 + 1;
            sourceLine = Integer.parseInt(sourceDef.substring(i1));
        } catch (IndexOutOfBoundsException ioob) {
            throw new IllegalStateException("var source definition='"+sourceDef+"'", ioob);
        }
        Source src = Source.getSource(debugger, sourceId, sourceName, sourcePath, sourceURI, codeRef);
        return new SourcePosition(debugger, sourceId, src, sourceLine);
    }
    
    public static TruffleVariable[] createFrameVars(final JPDADebugger debugger,
                                                    //final Variable suspendedInfo,
                                                    final Variable frameInstance) {
        JPDAClassType debugAccessor = TruffleDebugManager.getDebugAccessorJPDAClass(debugger);
        try {
            Variable frameVars = debugAccessor.invokeMethod(METHOD_GET_VARIABLES,
                                                            METHOD_GET_VARIABLES_SGN,
                                                            new Variable[] { frameInstance });
            TruffleVariable[] vars = createVars(debugger, (ObjectVariable) frameVars);
            return vars;
        } catch (InvalidExpressionException | NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
            return new TruffleVariable[] {};
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
        InvocationException iex = null;
        try {
            ((JPDAThreadImpl) thread).notifyMethodInvoking();
            invoking = true;
            methodCalls.callMethods(thread);
            return true;
        } catch (PropertyVetoException pvex) {
            return false;
        } catch (InvocationException ex) {
            iex = ex;
        } finally {
            if (invoking) {
                ((JPDAThreadImpl) thread).notifyMethodInvokeDone();
            }
        }
        if (iex != null) {
            Throwable ex = new InvocationExceptionTranslated(iex, ((JPDAThreadImpl) thread).getDebugger()).preload((JPDAThreadImpl) thread);
            Exceptions.printStackTrace(Exceptions.attachMessage(ex, "Invoking "+methodCalls));
        }
        return false;
    }

    public static interface MethodCallsAccess {
        
        void callMethods(JPDAThread thread) throws InvocationException;
        
    }
}
