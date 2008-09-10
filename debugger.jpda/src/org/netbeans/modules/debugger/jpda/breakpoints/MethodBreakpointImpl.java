/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.debugger.jpda.breakpoints;

import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.debugger.Breakpoint.VALIDITY;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.openide.util.NbBundle;

/**
* Implementation of method breakpoint.
*
* @author   Jan Jancura
*/
public class MethodBreakpointImpl extends ClassBasedBreakpoint {
    
    private static final boolean IS_JDK_16 = !System.getProperty("java.version").startsWith("1.5"); // NOI18N

    
    private MethodBreakpoint breakpoint;
    
    
    public MethodBreakpointImpl (MethodBreakpoint breakpoint, JPDADebuggerImpl debugger, Session session) {
        super (breakpoint, debugger, session);
        this.breakpoint = breakpoint;
        set ();
    }
    
    public static boolean canGetMethodReturnValues(VirtualMachine vm) {
        if (!IS_JDK_16) return false;
        boolean canGetMethodReturnValues = false;
        java.lang.reflect.Method m = null;
        try {
            m = vm.getClass().getMethod("canGetMethodReturnValues", new Class[] {});
        } catch (Exception ex) {
        }
        if (m != null) {
            try {
                m.setAccessible(true);
                Object ret = m.invoke(vm, new Object[] {});
                canGetMethodReturnValues = Boolean.TRUE.equals(ret);
            } catch (Exception ex) {
            }
        }
        return canGetMethodReturnValues;
    }
    
    protected void setRequests () {
        setClassRequests (
            breakpoint.getClassFilters (), 
            breakpoint.getClassExclusionFilters (), 
            ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED
        );
        for(String filter : breakpoint.getClassFilters()) {
            checkLoadedClasses (filter, breakpoint.getClassExclusionFilters());
        }
    }
    
    protected EventRequest createEventRequest(EventRequest oldRequest) {
        if (oldRequest instanceof BreakpointRequest) {
            return getEventRequestManager ().
                    createBreakpointRequest(((BreakpointRequest) oldRequest).location());
        }
        if (oldRequest instanceof MethodEntryRequest) {
            MethodEntryRequest entryReq = getEventRequestManager().
                    createMethodEntryRequest();
            ReferenceType referenceType = (ReferenceType) oldRequest.getProperty("ReferenceType");
            entryReq.addClassFilter(referenceType);
            JPDAThread[] threadFilters = breakpoint.getThreadFilters(getDebugger());
            if (threadFilters != null && threadFilters.length > 0) {
                for (JPDAThread t : threadFilters) {
                    entryReq.addThreadFilter(((JPDAThreadImpl) t).getThreadReference());
                }
            }
            ObjectVariable[] varFilters = breakpoint.getInstanceFilters(getDebugger());
            if (varFilters != null && varFilters.length > 0) {
                for (ObjectVariable v : varFilters) {
                    entryReq.addInstanceFilter((ObjectReference) ((JDIVariable) v).getJDIValue());
                }
            }
            Object entryMethodNames = oldRequest.getProperty("methodNames");
            entryReq.putProperty("methodNames", entryMethodNames);
            entryReq.putProperty("ReferenceType", referenceType);
            return entryReq;
        }
        if (oldRequest instanceof MethodExitRequest) {
            MethodExitRequest exitReq = getEventRequestManager().
                    createMethodExitRequest();
            ReferenceType referenceType = (ReferenceType) oldRequest.getProperty("ReferenceType");
            exitReq.addClassFilter(referenceType);
            JPDAThread[] threadFilters = breakpoint.getThreadFilters(getDebugger());
            if (threadFilters != null && threadFilters.length > 0) {
                for (JPDAThread t : threadFilters) {
                    exitReq.addThreadFilter(((JPDAThreadImpl) t).getThreadReference());
                }
            }
            ObjectVariable[] varFilters = breakpoint.getInstanceFilters(getDebugger());
            if (varFilters != null && varFilters.length > 0) {
                for (ObjectVariable v : varFilters) {
                    exitReq.addInstanceFilter((ObjectReference) ((JDIVariable) v).getJDIValue());
                }
            }
            Object exitMethodNames = oldRequest.getProperty("methodNames");
            exitReq.putProperty("methodNames", exitMethodNames);
            exitReq.putProperty("ReferenceType", referenceType);
            return exitReq;
        }
        return null;
    }

    private Value[] returnValuePtr;

    public boolean processCondition(Event event) {
        if (event instanceof BreakpointEvent) {
            return processCondition(event, breakpoint.getCondition (),
                    ((BreakpointEvent) event).thread (), null);
        }
        if (event instanceof MethodEntryEvent) {
            String methodName = ((MethodEntryEvent) event).method().name();
            Set methodNames = (Set) event.request().getProperty("methodNames");
            if (methodNames == null || methodNames.contains(methodName)) {
                return processCondition(event, breakpoint.getCondition (),
                        ((MethodEntryEvent) event).thread (), null);
            } else {
                return false;
            }
        }
        if (event instanceof MethodExitEvent) {
            String methodName = ((MethodExitEvent) event).method().name();
            Set methodNames = (Set) event.request().getProperty("methodNames");
            if (methodNames == null || methodNames.contains(methodName)) {
                Value returnValue = null;
                /* JDK 1.6.0 code */
                if (IS_JDK_16) { // Retrieval of the return value
                    VirtualMachine vm = event.virtualMachine();
                    // vm.canGetMethodReturnValues();
                    if (canGetMethodReturnValues(vm)) {
                        //Value returnValue = ((MethodExitEvent) event).returnValue();
                        java.lang.reflect.Method m = null;
                        try {
                            m = event.getClass().getDeclaredMethod("returnValue", new Class[] {});
                        } catch (Exception ex) {
                            m = null;
                        }
                        if (m != null) {
                            try {
                                m.setAccessible(true);
                                Object ret = m.invoke(event, new Object[] {});
                                returnValue = (Value) ret;
                            } catch (Exception ex) {
                            }
                        }
                    }
                }
                boolean success = processCondition(event, breakpoint.getCondition (),
                            ((MethodExitEvent) event).thread (), returnValue);
                if (success) {
                    returnValuePtr = new Value[] { returnValue };
                }
                return success;
            } else {
                return false;
            }
        } else {
            return true; // Empty condition, always satisfied.
        }
    }

    public boolean exec (Event event) {
        if (event instanceof BreakpointEvent)
            return perform (
                event,
                ((BreakpointEvent) event).thread (),
                ((LocatableEvent) event).location ().declaringType (),
                null
            );
        if (event instanceof MethodEntryEvent) {
            ReferenceType refType = null;
            if (((LocatableEvent) event).location() != null) {
                refType = ((LocatableEvent) event).location().declaringType();
            }
            return perform (
                event,
                ((MethodEntryEvent) event).thread (),
                refType,
                null
            );
        }
        if (event instanceof MethodExitEvent) {
            ReferenceType refType = null;
            if (((LocatableEvent) event).location() != null) {
                refType = ((LocatableEvent) event).location().declaringType();
            }
            Value returnValue;
            if (returnValuePtr != null) {
                returnValue = returnValuePtr[0];
                returnValuePtr = null;
            } else {
                returnValue = null;
            }
            return perform (
                event,
                ((MethodExitEvent) event).thread (),
                refType,
                returnValue
            );
        }
        return super.exec (event);
    }
    
    protected void classLoaded (ReferenceType referenceType) {
        Iterator methods = referenceType.methods ().iterator ();
        MethodEntryRequest entryReq = null;
        MethodExitRequest exitReq = null;
        Set<String> entryMethodNames = null;
        Set<String> exitMethodNames = null;
        boolean locationEntry = false;
        String methodName = breakpoint.getMethodName();
        String constructorName = referenceType.name();
        int index = constructorName.lastIndexOf('.');
        if (index > 0) constructorName = constructorName.substring(index + 1);
        if (methodName.equals(constructorName)) {
            methodName = "<init>"; // Constructor
        }
        String signature = breakpoint.getMethodSignature();
        while (methods.hasNext ()) {
            Method method = (Method) methods.next ();
            if (methodName.equals("") || match (method.name (), methodName) &&
                                         (signature == null ||
                                          egualMethodSignatures(signature, method.signature()))) {
                
                if ((breakpoint.getBreakpointType() & breakpoint.TYPE_METHOD_ENTRY) != 0) {
                    if (method.location () != null && !method.isNative()) {
                        Location location = method.location ();
                        try {
                            BreakpointRequest br = getEventRequestManager ().
                                createBreakpointRequest (location);
                            addEventRequest (br);
                        } catch (VMDisconnectedException e) {
                        }
                        locationEntry = true;
                    } else {
                        if (entryReq == null) {
                            entryReq = getEventRequestManager().
                                    createMethodEntryRequest();
                            entryReq.addClassFilter(referenceType);
                            JPDAThread[] threadFilters = breakpoint.getThreadFilters(getDebugger());
                            if (threadFilters != null && threadFilters.length > 0) {
                                for (JPDAThread t : threadFilters) {
                                    entryReq.addThreadFilter(((JPDAThreadImpl) t).getThreadReference());
                                }
                            }
                            ObjectVariable[] varFilters = breakpoint.getInstanceFilters(getDebugger());
                            if (varFilters != null && varFilters.length > 0) {
                                for (ObjectVariable v : varFilters) {
                                    entryReq.addInstanceFilter((ObjectReference) ((JDIVariable) v).getJDIValue());
                                }
                            }
                            entryMethodNames = new HashSet<String>();
                            entryReq.putProperty("methodNames", entryMethodNames);
                            entryReq.putProperty("ReferenceType", referenceType);
                        }
                        entryMethodNames.add(method.name ());
                    }
                }
                if ((breakpoint.getBreakpointType() & breakpoint.TYPE_METHOD_EXIT) != 0) {
                    if (exitReq == null) {
                        exitReq = getEventRequestManager().
                                createMethodExitRequest();
                        exitReq.addClassFilter(referenceType);
                        JPDAThread[] threadFilters = breakpoint.getThreadFilters(getDebugger());
                        if (threadFilters != null && threadFilters.length > 0) {
                            for (JPDAThread t : threadFilters) {
                                exitReq.addThreadFilter(((JPDAThreadImpl) t).getThreadReference());
                            }
                        }
                        ObjectVariable[] varFilters = breakpoint.getInstanceFilters(getDebugger());
                        if (varFilters != null && varFilters.length > 0) {
                            for (ObjectVariable v : varFilters) {
                                exitReq.addInstanceFilter((ObjectReference) ((JDIVariable) v).getJDIValue());
                            }
                        }
                        exitMethodNames = new HashSet<String>();
                        exitReq.putProperty("methodNames", exitMethodNames);
                        exitReq.putProperty("ReferenceType", referenceType);
                    }
                    exitMethodNames.add(method.name());
                }
            }
        }
        if (entryReq != null) {
            addEventRequest(entryReq);
        }
        if (exitReq != null) {
            addEventRequest(exitReq);
        }
        if (locationEntry || entryReq != null || exitReq != null) {
            setValidity(VALIDITY.VALID, null);
        } else {
            if (signature == null) {
                setValidity(VALIDITY.INVALID,
                        NbBundle.getMessage(MethodBreakpointImpl.class, "MSG_NoMethod", referenceType.name(), methodName));
            } else {
                setValidity(VALIDITY.INVALID,
                        NbBundle.getMessage(MethodBreakpointImpl.class, "MSG_NoMethodSign", referenceType.name(), methodName, signature));
            }
        }
    }
    
    private static boolean egualMethodSignatures(String s1, String s2) {
        int i = s1.lastIndexOf(")");
        if (i > 0) s1 = s1.substring(0, i);
        i = s2.lastIndexOf(")");
        if (i > 0) s2 = s2.substring(0, i);
        return s1.equals(s2);
    }
    
}

