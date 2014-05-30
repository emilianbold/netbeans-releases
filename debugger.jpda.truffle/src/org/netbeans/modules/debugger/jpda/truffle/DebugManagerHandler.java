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

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.InvocationException;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.This;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.modules.debugger.jpda.expr.InvocationExceptionTranslated;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ClassTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.PrimitiveValueWrapper;
import org.netbeans.modules.debugger.jpda.jdi.UnsupportedOperationExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import static org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccessBreakpoints.BASIC_CLASS_NAME;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.openide.util.Exceptions;

/**
 *
 * @author martin
 */
class DebugManagerHandler implements JPDABreakpointListener {
    
    private static final Logger LOG = Logger.getLogger(DebugManagerHandler.class.getName());
    
    private static final String TRUFFLE_JE_ENGINE_CLASS_NAME = "com.oracle.truffle.js.engine.TruffleJSEngine"; // NOI18N
    private static final String ACCESSOR_START_ACCESS_LOOP = "startAccessLoop"; // NOI18N
    private static final String ACCESSOR_STOP_ACCESS_LOOP = "stopAccessLoop";   // NOI18N
    private static final String ACCESSOR_SET_UP_DEBUG_MANAGER = "setUpDebugManager";    // NOI18N
    private static final String ACCESSOR_SET_UP_DEBUG_MANAGER_FOR = "setUpDebugManagerFor"; // NOI18N
    private static final String ACCESSOR_DEBUGGER_ACCESS = "debuggerAccess";    // NOI18N
    private static final String ACCESSOR_EXECUTION_HALTED = "executionHalted";  // NOI18N
    
    private final JPDADebugger debugger;
    private final AtomicBoolean inited = new AtomicBoolean(false);
    private ClassType accessorClass;
    private ObjectReference debugManager;
    
    public DebugManagerHandler(JPDADebugger debugger) {
        this.debugger = debugger;
    }

    @Override
    public void breakpointReached(JPDABreakpointEvent event) {
        try {
            if (debugger.equals(event.getDebugger())) {
                if (inited.compareAndSet(false, true)) {
                    initDebuggerRemoteService(event.getThread());
                }
                //event.getThread();
                JPDAThreadImpl thread = (JPDAThreadImpl) event.getThread();
                InvocationExceptionTranslated iextr = null;
                try {
                    thread.notifyMethodInvoking();
                    This engine = thread.getCallStack(0, 1)[0].getThisVariable();
                    Value engineValue = ((JDIVariable) engine).getJDIValue();
                    if (!(engineValue instanceof ObjectReference)) {
                        return ;
                    }
                    ReferenceType engineClasstype = ((ObjectReference) engineValue).referenceType();
                    if (!TRUFFLE_JE_ENGINE_CLASS_NAME.equals(engineClasstype.name())) {
                        return ;
                    }
                    Method debugManagerMethod = ClassTypeWrapper.concreteMethodByName(
                            accessorClass,
                            ACCESSOR_SET_UP_DEBUG_MANAGER_FOR,
                            "(L"+TRUFFLE_JE_ENGINE_CLASS_NAME.replace('.', '/')+";)Lorg/netbeans/modules/debugger/jpda/backend/truffle/JPDATruffleDebugManager;");
                    ThreadReference tr = thread.getThreadReference();
                    Object ret = ClassTypeWrapper.invokeMethod(accessorClass, tr, debugManagerMethod, Collections.singletonList(engineValue), ObjectReference.INVOKE_SINGLE_THREADED);
                    if (!(ret instanceof ObjectReference)) {
                        LOG.log(Level.WARNING, "Could not start up debugger manager for "+engineValue);
                        return ;
                    }
                    debugManager = (ObjectReference) ret;
                } catch (AbsentInformationException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (VMDisconnectedExceptionWrapper vmd) {
                } catch (InvocationException iex) {
                    iextr = new InvocationExceptionTranslated(iex, thread.getDebugger());
                    Exceptions.printStackTrace(iex);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    thread.notifyMethodInvokeDone();
                }
                if (iextr != null) {
                    initException(iextr, thread);
                    Exceptions.printStackTrace(iextr);
                }
            }
        } finally {
            event.resume();
        }
    }
    
    private void initDebuggerRemoteService(JPDAThread thread) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "initDebuggerRemoteService({0})", thread);
        }
        JPDAThreadImpl t = (JPDAThreadImpl) thread;
        Lock writeLock = t.accessLock.writeLock();
        writeLock.lock();
        try {
            ClassObjectReference cor = null;
            try {
                cor = RemoteServices.uploadBasicClasses(t, BASIC_CLASS_NAME);
            } catch (PropertyVetoException pvex) {
                Exceptions.printStackTrace(pvex);
            } catch (InvalidTypeException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ClassNotLoadedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IncompatibleThreadStateException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationException ex) {
                Exceptions.printStackTrace(ex);
                final InvocationExceptionTranslated iextr = new InvocationExceptionTranslated(ex, t.getDebugger());
                initException(iextr, t);
                Exceptions.printStackTrace(iextr);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Uploaded class = {0}", cor);
            }
            if (cor == null) {
                return ;
            }
            ThreadReference tr = t.getThreadReference();
            
            ClassType serviceClass = (ClassType) ClassObjectReferenceWrapper.reflectedType(cor);//RemoteServices.getClass(vm, "org.netbeans.modules.debugger.jpda.visual.remote.RemoteService");

            InvocationExceptionTranslated iextr = null;
            Method startMethod = ClassTypeWrapper.concreteMethodByName(serviceClass, ACCESSOR_START_ACCESS_LOOP, "()Z");
            try {
                t.notifyMethodInvoking();
                Value ret = ClassTypeWrapper.invokeMethod(serviceClass, tr, startMethod, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
                if (ret instanceof PrimitiveValue) {
                    boolean success = PrimitiveValueWrapper.booleanValue((PrimitiveValue) ret);
                    RemoteServices.setAccessLoopStarted(t.getDebugger(), success);
                    if (!success) {
                        LOG.log(Level.WARNING, "Could not start the access loop of "+serviceClass);
                        return ;
                    }
                }
                accessorClass = serviceClass;
                Method debugManagerMethod = ClassTypeWrapper.concreteMethodByName(serviceClass, ACCESSOR_SET_UP_DEBUG_MANAGER, "()Lorg/netbeans/modules/debugger/jpda/backend/truffle/JPDATruffleDebugManager;");
                ret = ClassTypeWrapper.invokeMethod(serviceClass, tr, debugManagerMethod, Collections.EMPTY_LIST, ObjectReference.INVOKE_SINGLE_THREADED);
                if (!(ret instanceof ObjectReference)) {
                    LOG.log(Level.WARNING, "Could not start up debugger manager of "+serviceClass);
                    return ;
                }
                debugManager = (ObjectReference) ret;
            } catch (VMDisconnectedExceptionWrapper vmd) {
            } catch (InvocationException iex) {
                iextr = new InvocationExceptionTranslated(iex, t.getDebugger());
                Exceptions.printStackTrace(iex);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                t.notifyMethodInvokeDone();
                ObjectReferenceWrapper.enableCollection(cor);
            }
            if (iextr != null) {
                initException(iextr, t);
                Exceptions.printStackTrace(iextr);
            }
        } catch (InternalExceptionWrapper iex) {
        } catch (ClassNotPreparedExceptionWrapper cnpex) {
            Exceptions.printStackTrace(cnpex);
        } catch (ObjectCollectedExceptionWrapper collex) {
        } catch (UnsupportedOperationExceptionWrapper uex) {
            LOG.log(Level.INFO, uex.getLocalizedMessage(), uex);
        } catch (VMDisconnectedExceptionWrapper vmd) {
        } finally {
            writeLock.unlock();
        }
        if (LOG.isLoggable(Level.FINE)) {
            try {
                LOG.fine("The JPDATruffleAccessor is there: "+
                            RemoteServices.getClass(t.getThreadReference().virtualMachine(),
                         BASIC_CLASS_NAME));
            } catch (Exception ex) {
                LOG.log(Level.FINE, "", ex);
            }
        }
    }
    
    private void initException(InvocationExceptionTranslated iextr, JPDAThreadImpl t) {
        iextr.setPreferredThread(t);
        iextr.getMessage();
        iextr.getLocalizedMessage();
        Throwable cause = iextr.getCause();
        iextr.getStackTrace();
        if (cause instanceof InvocationExceptionTranslated) {
            initException((InvocationExceptionTranslated) cause, t);
        }
    }
    

    
    void destroy() {
        if (accessorClass == null) {
            return ;
        }
        try {
            Field accessLoopRunning = accessorClass.fieldByName("accessLoopRunning");
            if (accessLoopRunning != null) {
                accessorClass.setValue(accessLoopRunning, accessorClass.virtualMachine().mirrorOf(false));
            }
            Field accessThreadField = accessorClass.fieldByName("accessLoopThread");
            if (accessThreadField != null) {
                Value ret = accessorClass.getValue(accessThreadField);
                if (ret instanceof ThreadReference) {
                    ((ThreadReference) ret).interrupt();
                }
            }
        } catch (VMDisconnectedException vdex) {
            // Ignore
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        /*
        ClassObjectReference serviceClass = RemoteServices.getServiceClass(debugger);
        if (serviceClass == null) {
            return ;
        }
        try {
            ReferenceType serviceType = serviceClass.reflectedType();
            Field awtAccessLoop = serviceType.fieldByName("awtAccessLoop"); // NOI18N
            if (awtAccessLoop != null) {
                ((ClassType) serviceType).setValue(awtAccessLoop, serviceClass.virtualMachine().mirrorOf(false));
            }
            serviceClass.enableCollection();
        } catch (VMDisconnectedException vdex) {
            // Ignore
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
                */
    }

    void breakpointAdded(JSLineBreakpoint jsLineBreakpoint) {
        
    }

    void breakpointRemoved(JSLineBreakpoint jsLineBreakpoint) {
        
    }
}
