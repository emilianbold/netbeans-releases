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

package org.netbeans.modules.debugger.jpda.truffle.actions;

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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.netbeans.modules.debugger.jpda.truffle.TruffleDebugManager;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleStrataProvider;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * Currently JavaScript-specific Run to cursor action provider.
 * 
 * @author Martin Entlicher
 */
@ActionsProvider.Registration(path="netbeans-JPDASession/"+TruffleStrataProvider.TRUFFLE_STRATUM,
                              actions={"runToCursor"}, activateForMIMETypes={"text/javascript"})
public class RunToCursorActionProvider extends ActionsProviderSupport {
    
    private static final String ACCESSOR_SET_ONE_SHOT_LINE_BREAKPOINT = "setOneShotLineBreakpoint"; // NOI18N
    private static final String ACCESSOR_SET_ONE_SHOT_LINE_BREAKPOINT_SIGNAT =
            "(L"+String.class.getName().replace('.', '/')+";I)Lcom/oracle/truffle/api/debug/Breakpoint;";   // NOI18N
    
    private final JPDADebugger debugger;
    private final Session session;
    private final PropertyChangeListener stateChangeListener;
    private volatile ObjectReference oneShotBreakpoint;
    
    public RunToCursorActionProvider(ContextProvider context) {
        debugger = context.lookupFirst(null, JPDADebugger.class);
        session = context.lookupFirst(null, Session.class);
        stateChangeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                checkEnabled();
            }
        };
        debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE, stateChangeListener);
        EditorContextDispatcher.getDefault().addPropertyChangeListener("text/javascript", stateChangeListener);
        checkEnabled();
    }
    
    private void checkEnabled() {
        DebuggerEngine truffleDbgEngine = session.getEngineForLanguage(TruffleStrataProvider.TRUFFLE_STRATUM);
        if (truffleDbgEngine == null) {
            setEnabled(ActionsManager.ACTION_RUN_TO_CURSOR, false);
            return ;
        }
        ActionsManager actionsManager = truffleDbgEngine.getActionsManager();
        int debuggerState = debugger.getState();
        setEnabled (
            ActionsManager.ACTION_RUN_TO_CURSOR,
            actionsManager.isEnabled(ActionsManager.ACTION_CONTINUE) &&
            (debuggerState== JPDADebugger.STATE_STOPPED) &&
            (EditorContextBridge.getContext().getCurrentLineNumber () >= 0) && 
            (EditorContextBridge.getContext().getCurrentURL ().endsWith (".js"))
        );
        if (debuggerState == JPDADebugger.STATE_DISCONNECTED) {
            debugger.removePropertyChangeListener (JPDADebugger.PROP_STATE, stateChangeListener);
            EditorContextDispatcher.getDefault().removePropertyChangeListener(stateChangeListener);
        }
    }

    @Override
    public Set getActions() {
        return Collections.singleton (ActionsManager.ACTION_RUN_TO_CURSOR);
    }
    
    @Override
    public void doAction(Object action) {
        if (oneShotBreakpoint != null) {
            removeBreakpoint(oneShotBreakpoint);
            oneShotBreakpoint = null;
        }
        FileObject fo = EditorContextDispatcher.getDefault().getCurrentFile();
        if (fo == null) {
            return ;
        }
        File file = FileUtil.toFile(fo);
        if (file == null) {
            return ;
        }
        final String path = file.getAbsolutePath();
        int line = EditorContextDispatcher.getDefault().getCurrentLineNumber ();
        submitOneShotBreakpoint(path, line);
        JPDAThread currentThread = debugger.getCurrentThread();
        if (currentThread != null) {
            currentThread.resume();
        }
        
    }
    
    private void submitOneShotBreakpoint(final String path, final int line) {
        final ClassType debugAccessor = TruffleDebugManager.getDebugAccessorClass(debugger);
        try {
            final Method setLineBreakpointMethod = ClassTypeWrapper.concreteMethodByName(
                    debugAccessor,
                    ACCESSOR_SET_ONE_SHOT_LINE_BREAKPOINT,
                    ACCESSOR_SET_ONE_SHOT_LINE_BREAKPOINT_SIGNAT);
            TruffleAccess.methodCallingAccess(debugger, new TruffleAccess.MethodCallsAccess() {
                @Override
                public void callMethods(JPDAThread thread) {
                    ThreadReference tr = ((JPDAThreadImpl) thread).getThreadReference();
                    StringReference pathRef = tr.virtualMachine().mirrorOf(path);
                    IntegerValue lineRef = tr.virtualMachine().mirrorOf(line);
                    List<? extends Value> args = Arrays.asList(new Value[] { pathRef, lineRef });
                    try {
                        ObjectReference ret = (ObjectReference) ClassTypeWrapper.invokeMethod(
                                debugAccessor,
                                tr,
                                setLineBreakpointMethod,
                                args,
                                ObjectReference.INVOKE_SINGLE_THREADED);
                        oneShotBreakpoint = ret;
                    } catch (InvalidTypeException | ClassNotLoadedException |
                             IncompatibleThreadStateException | InvocationException |
                             InternalExceptionWrapper | VMDisconnectedExceptionWrapper |
                             ObjectCollectedExceptionWrapper ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        } catch (ClassNotPreparedExceptionWrapper | InternalExceptionWrapper |
                 VMDisconnectedExceptionWrapper ex) {
        }
    }
    
    private void removeBreakpoint(final ObjectReference bp) {
        final ClassType debugAccessor = TruffleDebugManager.getDebugAccessorClass(debugger);
        try {
            final Method removeLineBreakpointMethod = ClassTypeWrapper.concreteMethodByName(
                    debugAccessor,
                    //ACCESSOR_REMOVE_LINE_BREAKPOINT,
                    //ACCESSOR_REMOVE_LINE_BREAKPOINT_SIGNAT);
                    "removeBreakpoint",
                    //"(Lcom/oracle/truffle/debug/Breakpoint;)V");
                    "(Ljava/lang/Object;)V");
            TruffleAccess.methodCallingAccess(debugger, new TruffleAccess.MethodCallsAccess() {
                @Override
                public void callMethods(JPDAThread thread) {
                    ThreadReference tr = ((JPDAThreadImpl) thread).getThreadReference();
                    List<? extends Value> args = Arrays.asList(new Value[] { bp });
                    try {
                        ClassTypeWrapper.invokeMethod(
                                debugAccessor,
                                tr,
                                removeLineBreakpointMethod,
                                args,
                                ObjectReference.INVOKE_SINGLE_THREADED);
                        //successPtr[0] = true;
                    } catch (InvalidTypeException | ClassNotLoadedException |
                             IncompatibleThreadStateException | InvocationException |
                             InternalExceptionWrapper | VMDisconnectedExceptionWrapper |
                             ObjectCollectedExceptionWrapper ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        } catch (ClassNotPreparedExceptionWrapper | InternalExceptionWrapper |
                 VMDisconnectedExceptionWrapper ex) {
        }
        
    }

}
