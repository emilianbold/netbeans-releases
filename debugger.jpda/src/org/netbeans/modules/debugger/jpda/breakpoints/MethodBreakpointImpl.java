/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.breakpoints;

import com.sun.jdi.Location;
import com.sun.jdi.Method;
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
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
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
        checkLoadedClasses (breakpoint.getClassFilters () [0]);
    }

    public boolean exec (Event event) {
        if (event instanceof BreakpointEvent)
            return perform (
                breakpoint.getCondition (),
                ((BreakpointEvent) event).thread (),
                ((LocatableEvent) event).location ().declaringType (),
                null
            );
        if (event instanceof MethodEntryEvent) {
            String methodName = ((MethodEntryEvent) event).method().name();
            Set methodNames = (Set) event.request().getProperty("methodNames");
            if (methodNames == null || methodNames.contains(methodName)) {
                ReferenceType refType = null;
                if (((LocatableEvent) event).location() != null) {
                    refType = ((LocatableEvent) event).location().declaringType();
                }
                return perform (
                    breakpoint.getCondition (),
                    ((MethodEntryEvent) event).thread (),
                    refType,
                    null
                );
            }
        }
        if (event instanceof MethodExitEvent) {
            String methodName = ((MethodExitEvent) event).method().name();
            Set methodNames = (Set) event.request().getProperty("methodNames");
            if (methodNames == null || methodNames.contains(methodName)) {
                ReferenceType refType = null;
                if (((LocatableEvent) event).location() != null) {
                    refType = ((LocatableEvent) event).location().declaringType();
                }
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
                return perform (
                    breakpoint.getCondition (),
                    ((MethodExitEvent) event).thread (),
                    refType,
                    returnValue
                );
            }
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
        while (methods.hasNext ()) {
            Method method = (Method) methods.next ();
            if (methodName.equals("") || match (method.name (), methodName)) {
                
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
                            entryMethodNames = new HashSet<String>();
                            entryReq.putProperty("methodNames", entryMethodNames);
                        }
                        entryMethodNames.add(method.name ());
                    }
                }
                if ((breakpoint.getBreakpointType() & breakpoint.TYPE_METHOD_EXIT) != 0) {
                    if (exitReq == null) {
                        exitReq = getEventRequestManager().
                                createMethodExitRequest();
                        exitReq.addClassFilter(referenceType);
                        exitMethodNames = new HashSet<String>();
                        exitReq.putProperty("methodNames", exitMethodNames);
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
            setValidity(VALIDITY.INVALID,
                    NbBundle.getMessage(MethodBreakpointImpl.class, "MSG_NoMethod", referenceType.name(), methodName));
        }
    }
}

