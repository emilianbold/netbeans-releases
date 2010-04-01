/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.api.debugger.Breakpoint.VALIDITY;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidRequestStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocatableWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocationWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MethodWrapper;
import org.netbeans.modules.debugger.jpda.jdi.MirrorWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeComponentWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.EventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.LocatableEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.MethodEntryEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.MethodExitEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.BreakpointRequestWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.MethodEntryRequestWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.MethodExitRequestWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
* Implementation of method breakpoint.
*
* @author   Jan Jancura
*/
public class MethodBreakpointImpl extends ClassBasedBreakpoint {
    
    private MethodBreakpoint breakpoint;
    
    
    public MethodBreakpointImpl (MethodBreakpoint breakpoint, JPDADebuggerImpl debugger, Session session) {
        super (breakpoint, debugger, session);
        this.breakpoint = breakpoint;
        set ();
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
    
    protected EventRequest createEventRequest(EventRequest oldRequest) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        if (oldRequest instanceof BreakpointRequest) {
            return EventRequestManagerWrapper.createBreakpointRequest(getEventRequestManager (),
                    BreakpointRequestWrapper.location((BreakpointRequest) oldRequest));
        }
        if (oldRequest instanceof MethodEntryRequest) {
            MethodEntryRequest entryReq = EventRequestManagerWrapper.
                    createMethodEntryRequest(getEventRequestManager());
            ReferenceType referenceType = (ReferenceType) EventRequestWrapper.getProperty(oldRequest, "ReferenceType");
            MethodEntryRequestWrapper.addClassFilter(entryReq, referenceType);
            JPDAThread[] threadFilters = breakpoint.getThreadFilters(getDebugger());
            if (threadFilters != null && threadFilters.length > 0) {
                for (JPDAThread t : threadFilters) {
                    MethodEntryRequestWrapper.addThreadFilter(entryReq, ((JPDAThreadImpl) t).getThreadReference());
                }
            }
            ObjectVariable[] varFilters = breakpoint.getInstanceFilters(getDebugger());
            if (varFilters != null && varFilters.length > 0) {
                for (ObjectVariable v : varFilters) {
                    MethodEntryRequestWrapper.addInstanceFilter(entryReq, (ObjectReference) ((JDIVariable) v).getJDIValue());
                }
            }
            Object entryMethodNames = EventRequestWrapper.getProperty(oldRequest, "methodNames");
            EventRequestWrapper.putProperty(entryReq, "methodNames", entryMethodNames);
            EventRequestWrapper.putProperty(entryReq, "ReferenceType", referenceType);
            return entryReq;
        }
        if (oldRequest instanceof MethodExitRequest) {
            MethodExitRequest exitReq = EventRequestManagerWrapper.
                    createMethodExitRequest(getEventRequestManager());
            ReferenceType referenceType = (ReferenceType) EventRequestWrapper.getProperty(oldRequest, "ReferenceType");
            MethodExitRequestWrapper.addClassFilter(exitReq, referenceType);
            JPDAThread[] threadFilters = breakpoint.getThreadFilters(getDebugger());
            if (threadFilters != null && threadFilters.length > 0) {
                for (JPDAThread t : threadFilters) {
                    MethodExitRequestWrapper.addThreadFilter(exitReq, ((JPDAThreadImpl) t).getThreadReference());
                }
            }
            ObjectVariable[] varFilters = breakpoint.getInstanceFilters(getDebugger());
            if (varFilters != null && varFilters.length > 0) {
                for (ObjectVariable v : varFilters) {
                    MethodExitRequestWrapper.addInstanceFilter(exitReq, (ObjectReference) ((JDIVariable) v).getJDIValue());
                }
            }
            Object exitMethodNames = EventRequestWrapper.getProperty(oldRequest, "methodNames");
            EventRequestWrapper.putProperty(exitReq, "methodNames", exitMethodNames);
            EventRequestWrapper.putProperty(exitReq, "ReferenceType", referenceType);
            return exitReq;
        }
        return null;
    }

    private final Map<Event, Value> returnValueByEvent = new WeakHashMap<Event, Value>();

    public boolean processCondition(Event event) {
        try {
            if (event instanceof BreakpointEvent) {
                return processCondition(event, breakpoint.getCondition (),
                        LocatableEventWrapper.thread((BreakpointEvent) event), null);
            }
            if (event instanceof MethodEntryEvent) {
                String methodName = TypeComponentWrapper.name(MethodEntryEventWrapper.method((MethodEntryEvent) event));
                Set methodNames = (Set) EventRequestWrapper.getProperty(EventWrapper.request(event), "methodNames");
                if (methodNames == null || methodNames.contains(methodName)) {
                    return processCondition(event, breakpoint.getCondition (),
                            LocatableEventWrapper.thread((MethodEntryEvent) event), null);
                } else {
                    return false;
                }
            }
            if (event instanceof MethodExitEvent) {
                String methodName = TypeComponentWrapper.name(MethodExitEventWrapper.method((MethodExitEvent) event));
                Set methodNames = (Set) EventRequestWrapper.getProperty(EventWrapper.request(event), "methodNames");
                if (methodNames == null || methodNames.contains(methodName)) {
                    Value returnValue = null;
                    VirtualMachine vm = MirrorWrapper.virtualMachine(event);
                    if (vm.canGetMethodReturnValues()) {
                        returnValue = ((MethodExitEvent) event).returnValue();
                    }
                    boolean success = processCondition(event, breakpoint.getCondition (),
                                LocatableEventWrapper.thread((MethodExitEvent) event), returnValue);
                    if (success) {
                        returnValueByEvent.put(event, returnValue);
                    }
                    return success;
                } else {
                    return false;
                }
            } else {
                return true; // Empty condition, always satisfied.
            }
        } catch (InternalExceptionWrapper e) {
            return true;
        } catch (VMDisconnectedExceptionWrapper e) {
            return true;
        }
    }

    @Override
    public boolean exec (Event event) {
        try {
            if (event instanceof BreakpointEvent) {
                return perform (
                    event,
                    LocatableEventWrapper.thread((BreakpointEvent) event),
                    LocationWrapper.declaringType(LocatableWrapper.location((LocatableEvent) event)),
                    null
                );
            }
            if (event instanceof MethodEntryEvent) {
                MethodEntryEvent me = (MethodEntryEvent) event;
                ReferenceType refType = null;
                if (LocatableWrapper.location(me) != null) {
                    refType = LocationWrapper.declaringType(LocatableWrapper.location(me));
                }
                return perform (
                    event,
                    LocatableEventWrapper.thread(me),
                    refType,
                    null
                );
            }
            if (event instanceof MethodExitEvent) {
                MethodExitEvent me = (MethodExitEvent) event;
                ReferenceType refType = null;
                if (LocatableWrapper.location(me) != null) {
                    refType = LocationWrapper.declaringType(LocatableWrapper.location(me));
                }
                Value returnValue = returnValueByEvent.remove(event);
                return perform (
                    event,
                    LocatableEventWrapper.thread(me),
                    refType,
                    returnValue
                );
            }
        } catch (InternalExceptionWrapper ex) {
            return false;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return false;
        }
        return super.exec (event);
    }
    
    @Override
    protected void classLoaded (List<ReferenceType> referenceTypes) {
        boolean submitted = false;
        String invalidMessage = null;
        for (ReferenceType referenceType : referenceTypes) {
            Iterator methods;
            try {
                methods = ReferenceTypeWrapper.methods0(referenceType).iterator();
            } catch (ClassNotPreparedExceptionWrapper ex) {
                Exceptions.printStackTrace(ex);
                continue ;
            }
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
                if (MethodWrapper.isBridge0(method)) {
                    continue; // see issue #172027
                }
                try {
                    if (methodName.equals("") || match (TypeComponentWrapper.name (method), methodName) &&
                                                 (signature == null ||
                                                  egualMethodSignatures(signature, TypeComponentWrapper.signature(method)))) {

                        if ((breakpoint.getBreakpointType() & breakpoint.TYPE_METHOD_ENTRY) != 0) {
                            if (MethodWrapper.location(method) != null && !MethodWrapper.isNative(method)) {
                                Location location = MethodWrapper.location(method);
                                BreakpointRequest br = EventRequestManagerWrapper.
                                    createBreakpointRequest (getEventRequestManager (), location);
                                addEventRequest (br);
                                locationEntry = true;
                            } else {
                                if (entryReq == null) {
                                    entryReq = EventRequestManagerWrapper.
                                            createMethodEntryRequest(getEventRequestManager());
                                    MethodEntryRequestWrapper.addClassFilter(entryReq, referenceType);
                                    JPDAThread[] threadFilters = breakpoint.getThreadFilters(getDebugger());
                                    if (threadFilters != null && threadFilters.length > 0) {
                                        for (JPDAThread t : threadFilters) {
                                            MethodEntryRequestWrapper.addThreadFilter(entryReq, ((JPDAThreadImpl) t).getThreadReference());
                                        }
                                    }
                                    ObjectVariable[] varFilters = breakpoint.getInstanceFilters(getDebugger());
                                    if (varFilters != null && varFilters.length > 0) {
                                        for (ObjectVariable v : varFilters) {
                                            MethodEntryRequestWrapper.addInstanceFilter(entryReq, (ObjectReference) ((JDIVariable) v).getJDIValue());
                                        }
                                    }
                                    entryMethodNames = new HashSet<String>();
                                    EventRequestWrapper.putProperty(entryReq, "methodNames", entryMethodNames);
                                    EventRequestWrapper.putProperty(entryReq, "ReferenceType", referenceType);
                                }
                                entryMethodNames.add(TypeComponentWrapper.name (method));
                            }
                        }
                        if ((breakpoint.getBreakpointType() & breakpoint.TYPE_METHOD_EXIT) != 0) {
                            if (exitReq == null) {
                                exitReq = EventRequestManagerWrapper.
                                        createMethodExitRequest(getEventRequestManager());
                                MethodExitRequestWrapper.addClassFilter(exitReq, referenceType);
                                JPDAThread[] threadFilters = breakpoint.getThreadFilters(getDebugger());
                                if (threadFilters != null && threadFilters.length > 0) {
                                    for (JPDAThread t : threadFilters) {
                                        MethodExitRequestWrapper.addThreadFilter(exitReq, ((JPDAThreadImpl) t).getThreadReference());
                                    }
                                }
                                ObjectVariable[] varFilters = breakpoint.getInstanceFilters(getDebugger());
                                if (varFilters != null && varFilters.length > 0) {
                                    for (ObjectVariable v : varFilters) {
                                        MethodExitRequestWrapper.addInstanceFilter(exitReq, (ObjectReference) ((JDIVariable) v).getJDIValue());
                                    }
                                }
                                exitMethodNames = new HashSet<String>();
                                EventRequestWrapper.putProperty(exitReq, "methodNames", exitMethodNames);
                                EventRequestWrapper.putProperty(exitReq, "ReferenceType", referenceType);
                            }
                            exitMethodNames.add(TypeComponentWrapper.name (method));
                        }
                    }
                } catch (InternalExceptionWrapper e) {
                } catch (ObjectCollectedExceptionWrapper e) {
                } catch (InvalidRequestStateExceptionWrapper irse) {
                    Exceptions.printStackTrace(irse);
                } catch (VMDisconnectedExceptionWrapper e) {
                    return ;
                }
            }
            try {
                if (entryReq != null) {
                    try {
                        addEventRequest(entryReq);
                    } catch (InternalExceptionWrapper e) {
                        entryReq = null;
                    } catch (ObjectCollectedExceptionWrapper e) {
                        entryReq = null;
                    } catch (InvalidRequestStateExceptionWrapper irse) {
                        Exceptions.printStackTrace(irse);
                        entryReq = null;
                    }
                }
                if (exitReq != null) {
                    try {
                        addEventRequest(exitReq);
                    } catch (InternalExceptionWrapper e) {
                        exitReq = null;
                    } catch (ObjectCollectedExceptionWrapper e) {
                        exitReq = null;
                    } catch (InvalidRequestStateExceptionWrapper irse) {
                        Exceptions.printStackTrace(irse);
                        exitReq = null;
                    }
                }
            } catch (VMDisconnectedExceptionWrapper e) {
                return ;
            }
            if (locationEntry || entryReq != null || exitReq != null) {
                submitted = true;
            } else {
                if (signature == null) {
                    invalidMessage =
                            NbBundle.getMessage(MethodBreakpointImpl.class, "MSG_NoMethod", referenceType.name(), methodName);
                } else {
                    invalidMessage =
                            NbBundle.getMessage(MethodBreakpointImpl.class, "MSG_NoMethodSign", referenceType.name(), methodName, signature);
                }
            }
        }
        if (submitted) {
            setValidity(VALIDITY.VALID, null);
        } else {
            setValidity(VALIDITY.INVALID, invalidMessage);
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

