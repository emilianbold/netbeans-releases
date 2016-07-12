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

package org.netbeans.modules.debugger.jpda.truffle.breakpoints;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.source.Source;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin
 */
public class TruffleBreakpointsHandler {
    
    private static final Logger LOG = Logger.getLogger(TruffleBreakpointsHandler.class.getName());
    
    private static final String ACCESSOR_SET_LINE_BREAKPOINT = "setLineBreakpoint"; // NOI18N
    private static final String ACCESSOR_SET_LINE_BREAKPOINT_SIGNAT =
            "(Ljava/lang/String;IILjava/lang/String;)[Lcom/oracle/truffle/api/debug/Breakpoint;";   // NOI18N
    private static final String ACCESSOR_SET_LINE_BREAKPOINT_MGR_SIGNAT =
            "(Lorg/netbeans/modules/debugger/jpda/backend/truffle/JPDATruffleDebugManager;Ljava/lang/String;IILjava/lang/String;)Lcom/oracle/truffle/api/debug/Breakpoint;";   // NOI18N
    private static final String ACCESSOR_REMOVE_LINE_BREAKPOINT = "removeLineBreakpoint"; // NOI18N
    private static final String ACCESSOR_REMOVE_LINE_BREAKPOINT_SIGNAT = "(Lcom/oracle/truffle/tools/debug/engine/LineBreakpoint;)V";    // NOI18N
    
    private final JPDADebugger debugger;
    private ClassType accessorClass;
    
    private volatile boolean initialBreakpointsSubmitted = false;
    private final Map<JSLineBreakpoint, Set<ObjectReference>> breakpointsMap = new HashMap<>();
    private final JSBreakpointPropertyChangeListener breakpointsChangeListener = new JSBreakpointPropertyChangeListener();
    
    public TruffleBreakpointsHandler(JPDADebugger debugger) {
        this.debugger = debugger;
    }

    public void destroy() {
        synchronized (breakpointsMap) {
            for (JSLineBreakpoint jsbp : breakpointsMap.keySet()) {
                jsbp.removePropertyChangeListener(breakpointsChangeListener);
            }
        }
    }
    
    /**
     * Call in method invoking
     */
    public void submitBreakpoints(ClassType accessorClass, ObjectReference debugManager, JPDAThreadImpl t) throws InvocationException {
        assert t.isMethodInvoking();
        this.accessorClass = accessorClass;
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        initialBreakpointsSubmitted = true;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "DebugManagerHandler: Breakpoints to submit = {0}", breakpoints);
        }
        Map<JSLineBreakpoint, ObjectReference> bpsMap = new HashMap<>();
        for (Breakpoint breakpoint : breakpoints) {
            if (!(breakpoint instanceof JSLineBreakpoint)) {
                continue;
            }
            JSLineBreakpoint bp = (JSLineBreakpoint) breakpoint;
            FileObject fileObject = bp.getFileObject();
            if (fileObject == null) {
                continue;
            }
            URI uri = Source.getTruffleInternalURI(fileObject);
            if (uri == null) {
                uri = fileObject.toURI();
            }
            ObjectReference bpImpl;
            if (bp.isEnabled()) {
                bpImpl = setLineBreakpoint(debugManager, t, uri, bp.getLineNumber(),
                                           getIgnoreCount(bp), bp.getCondition());
            } else {
                bpImpl = null;
            }
            bpsMap.put(bp, bpImpl);
            bp.addPropertyChangeListener(breakpointsChangeListener);
        }
        synchronized (breakpointsMap) {
            for (Map.Entry<JSLineBreakpoint, ObjectReference> bpEntry : bpsMap.entrySet()) {
                Set<ObjectReference> impls = breakpointsMap.get(bpEntry.getKey());
                if (impls == null) {
                    impls = new HashSet<ObjectReference>();
                    breakpointsMap.put(bpEntry.getKey(), impls);
                }
                impls.add(bpEntry.getValue());
            }
        }
    }
    
    private static int getIgnoreCount(JSLineBreakpoint bp) {
        int ignoreCount = 0;
        if (Breakpoint.HIT_COUNT_FILTERING_STYLE.GREATER.equals(bp.getHitCountFilteringStyle())) {
            ignoreCount = bp.getHitCountFilter();
        }
        return ignoreCount;
    }
    
    private ObjectReference setLineBreakpoint(ObjectReference debugManager,
                                              JPDAThreadImpl t, URI uri, int line,
                                              int ignoreCount, String condition) throws InvocationException {
        assert t.isMethodInvoking();
        ThreadReference tr = t.getThreadReference();
        VirtualMachine vm = tr.virtualMachine();
        try {
            Method setLineBreakpointMethod = ClassTypeWrapper.concreteMethodByName(
                    accessorClass,
                    ACCESSOR_SET_LINE_BREAKPOINT,
                    ACCESSOR_SET_LINE_BREAKPOINT_MGR_SIGNAT);
            //if (path.indexOf("/src") > 0) {
            //    path = path.substring(path.indexOf("/src") + 1);
            //}
            if (setLineBreakpointMethod == null) {
                throw new IllegalStateException("Method "+ACCESSOR_SET_LINE_BREAKPOINT+" with signature:\n"+ACCESSOR_SET_LINE_BREAKPOINT_MGR_SIGNAT+"\nis not present in accessor class "+accessorClass);
            }
            //Variable uriVar = debugger.createMirrorVar(uri);
            Value uriRef = vm.mirrorOf(uri.toString());// ((JDIVariable) uriVar).getJDIValue();
            IntegerValue lineRef = vm.mirrorOf(line);
            IntegerValue icRef = vm.mirrorOf(ignoreCount);
            StringReference conditionRef = (condition != null) ? vm.mirrorOf(condition) : null;
            List<? extends Value> args = Arrays.asList(new Value[] { debugManager, uriRef, lineRef, icRef, conditionRef });
            ObjectReference ret = (ObjectReference) ClassTypeWrapper.invokeMethod(
                    accessorClass,
                    tr,
                    setLineBreakpointMethod,
                    args,
                    ObjectReference.INVOKE_SINGLE_THREADED);
            return ret;
        } catch (VMDisconnectedExceptionWrapper | InternalExceptionWrapper |
                 ClassNotLoadedException | ClassNotPreparedExceptionWrapper |
                 IncompatibleThreadStateException | InvalidTypeException |
                 ObjectCollectedExceptionWrapper ex) {
            Exceptions.printStackTrace(Exceptions.attachMessage(ex, "Setting breakpoint to "+uri+":"+line));
            return null;
        }
    }
    
    private void submitBP(JSLineBreakpoint bp) {
        FileObject fileObject = bp.getFileObject();
        if (fileObject == null) {
            return ;
        }
        final URI uri;
        URI tiuri = Source.getTruffleInternalURI(fileObject);
        if (tiuri != null) {
            uri = tiuri;
        } else {
            uri = fileObject.toURI();
        }
        final int line = bp.getLineNumber();
        final int ignoreCount = getIgnoreCount(bp);
        final String condition = bp.getCondition();
        final ArrayReference[] bpRef = new ArrayReference[] { null };
        if (bp.isEnabled()) {
            try {
                final Method setLineBreakpointMethod = ClassTypeWrapper.concreteMethodByName(
                        accessorClass,
                        ACCESSOR_SET_LINE_BREAKPOINT,
                        ACCESSOR_SET_LINE_BREAKPOINT_SIGNAT);
                if (setLineBreakpointMethod == null) {
                    throw new IllegalStateException("Method "+ACCESSOR_SET_LINE_BREAKPOINT+" with signature:\n"+ACCESSOR_SET_LINE_BREAKPOINT_SIGNAT+"\nis not present in accessor class "+accessorClass);
                }
                TruffleAccess.methodCallingAccess(debugger, new TruffleAccess.MethodCallsAccess() {
                    @Override
                    public void callMethods(JPDAThread thread) throws InvocationException {
                        ThreadReference tr = ((JPDAThreadImpl) thread).getThreadReference();
                        VirtualMachine vm = tr.virtualMachine();
                        StringReference uriRef = vm.mirrorOf(uri.toString());
                        IntegerValue lineRef = vm.mirrorOf(line);
                        IntegerValue icRef = vm.mirrorOf(ignoreCount);
                        StringReference conditionRef = (condition != null) ? vm.mirrorOf(condition) : null;
                        List<? extends Value> args = Arrays.asList(new Value[] { uriRef, lineRef, icRef, conditionRef });
                        try {
                            ArrayReference ret = (ArrayReference) ClassTypeWrapper.invokeMethod(
                                    accessorClass,
                                    tr,
                                    setLineBreakpointMethod,
                                    args,
                                    ObjectReference.INVOKE_SINGLE_THREADED);
                            bpRef[0] = ret;
                        } catch (InvalidTypeException | ClassNotLoadedException |
                                 IncompatibleThreadStateException |
                                 InternalExceptionWrapper | VMDisconnectedExceptionWrapper |
                                 ObjectCollectedExceptionWrapper ex) {
                            Exceptions.printStackTrace(Exceptions.attachMessage(ex, "Setting breakpoint to "+uri+":"+line));
                        }
                    }
                });
            } catch (ClassNotPreparedExceptionWrapper | InternalExceptionWrapper |
                     VMDisconnectedExceptionWrapper ex) {
            }
        }
        bp.addPropertyChangeListener(breakpointsChangeListener);
        List<Value> values = bpRef[0].getValues();
        Set<ObjectReference> breakpoints = new HashSet<>();
        for (Value v : values) {
            if (v instanceof ObjectReference) {
                breakpoints.add((ObjectReference) v);
            }
        }
        if (!breakpoints.isEmpty()) {
            synchronized (breakpointsMap) {
                breakpointsMap.put(bp, breakpoints);
            }
        }
    }
    
    private boolean removeBP(final JSLineBreakpoint bp) {
        bp.removePropertyChangeListener(breakpointsChangeListener);
        final Set<ObjectReference> bpImpls;
        synchronized (breakpointsMap) {
            bpImpls = breakpointsMap.remove(bp);
        }
        if (bpImpls == null) {
            return false;
        }
        final boolean[] successPtr = new boolean[] { false };
        try {
            final Method removeLineBreakpointMethod = ClassTypeWrapper.concreteMethodByName(
                    accessorClass,
                    //ACCESSOR_REMOVE_LINE_BREAKPOINT,
                    //ACCESSOR_REMOVE_LINE_BREAKPOINT_SIGNAT);
                    "removeBreakpoint",
                    //"(Lcom/oracle/truffle/debug/Breakpoint;)V");
                    "(Ljava/lang/Object;)V");
            TruffleAccess.methodCallingAccess(debugger, new TruffleAccess.MethodCallsAccess() {
                @Override
                public void callMethods(JPDAThread thread) throws InvocationException {
                    ThreadReference tr = ((JPDAThreadImpl) thread).getThreadReference();
                    try {
                        for (ObjectReference bpImpl : bpImpls) {
                            List<? extends Value> args = Arrays.asList(new Value[] { bpImpl });
                            try {
                                ClassTypeWrapper.invokeMethod(
                                        accessorClass,
                                        tr,
                                        removeLineBreakpointMethod,
                                        args,
                                        ObjectReference.INVOKE_SINGLE_THREADED);
                                successPtr[0] = true;
                            } catch (InvalidTypeException | ClassNotLoadedException |
                                     IncompatibleThreadStateException |
                                     InternalExceptionWrapper |
                                     ObjectCollectedExceptionWrapper ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    } catch (VMDisconnectedExceptionWrapper ex) {}
                }
            });
        } catch (ClassNotPreparedExceptionWrapper | InternalExceptionWrapper |
                 VMDisconnectedExceptionWrapper ex) {
        }
        return successPtr[0];
    }
    
    private boolean setBreakpointProperty(JSLineBreakpoint bp,
                                          TruffleBPMethods method,
                                          final List<? extends Value> args) {
        final Set<ObjectReference> bpImpls;
        synchronized (breakpointsMap) {
            bpImpls = breakpointsMap.get(bp);
        }
        if (bpImpls == null) {
            if (bp.isEnabled()) {
                submitBP(bp);
                return true;
            } else {
                return false;
            }
        }
        final boolean[] successPtr = new boolean[] { false };
        try {
            final Method setBreakpointPropertyMethod = ClassTypeWrapper.concreteMethodByName(
                    (ClassType) ObjectReferenceWrapper.referenceType(bpImpls.iterator().next()),
                    method.getMethodName(),
                    method.getMethodSignature());
            TruffleAccess.methodCallingAccess(debugger, new TruffleAccess.MethodCallsAccess() {
                @Override
                public void callMethods(JPDAThread thread) throws InvocationException {
                    ThreadReference tr = ((JPDAThreadImpl) thread).getThreadReference();
                    try {
                        for (ObjectReference bpImpl : bpImpls) {
                            try {
                                ObjectReferenceWrapper.invokeMethod(
                                        bpImpl,
                                        tr,
                                        setBreakpointPropertyMethod,
                                        args,
                                        ObjectReference.INVOKE_SINGLE_THREADED);
                                successPtr[0] = true;
                            } catch (InvalidTypeException | ClassNotLoadedException |
                                     IncompatibleThreadStateException |
                                     InternalExceptionWrapper |
                                     ObjectCollectedExceptionWrapper ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    } catch (VMDisconnectedExceptionWrapper ex) {}
                }
            });
        } catch (ClassNotPreparedExceptionWrapper | InternalExceptionWrapper |
                 ObjectCollectedExceptionWrapper | VMDisconnectedExceptionWrapper ex) {
        }
        return successPtr[0];
    }

    public void breakpointAdded(JSLineBreakpoint jsLineBreakpoint) {
        if (initialBreakpointsSubmitted) {
            // Breakpoints were submitted already, submit this as well.
            submitBP(jsLineBreakpoint);
        }
    }

    public void breakpointRemoved(JSLineBreakpoint jsLineBreakpoint) {
        if (initialBreakpointsSubmitted) {
            // Breakpoints were submitted already, remove this.
            removeBP(jsLineBreakpoint);
        }
    }
    
    private static enum TruffleBPMethods {
        setEnabled,
        setIgnoreCount,
        setCondition;
        
        public String getMethodName() {
            return name();
        }
        
        public String getMethodSignature() {
            switch (this) {
                case setEnabled:
                    return "(Z)V";
                case setIgnoreCount:
                    return "(I)V";
                case setCondition:
                    return "(Ljava/lang/String;)V";
                default:
                    throw new IllegalStateException(this.name());
            }
        }
    }
    
    private class JSBreakpointPropertyChangeListener implements PropertyChangeListener {
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final JSLineBreakpoint jsbp = (JSLineBreakpoint) evt.getSource();
            String propertyName = evt.getPropertyName();
            final TruffleBPMethods method;
            final List<? extends Value> args;
            final VirtualMachine vm = ((JPDADebuggerImpl) debugger).getVirtualMachine();
            if (vm == null) {
                return ;
            }
            switch (propertyName) {
                case JSLineBreakpoint.PROP_ENABLED:
                    method = TruffleBPMethods.setEnabled;
                    args = Collections.singletonList(vm.mirrorOf(jsbp.isEnabled()));
                    break;
                case JSLineBreakpoint.PROP_CONDITION:
                    method = TruffleBPMethods.setCondition;
                    String condition = jsbp.getCondition();
                    StringReference conditionRef = (condition != null) ? vm.mirrorOf(condition) : null;
                    args = Collections.singletonList(conditionRef);
                    break;
                case Breakpoint.PROP_HIT_COUNT_FILTER:
                    method = TruffleBPMethods.setIgnoreCount;
                    args = Collections.singletonList(vm.mirrorOf(getIgnoreCount(jsbp)));
                    break;
                default:
                    return ;
            }
            ((JPDADebuggerImpl) debugger).getRequestProcessor().post(new Runnable() {
                @Override
                public void run() {
                    setBreakpointProperty(jsbp, method, args);
                }
            });
        }
        
    }
}
