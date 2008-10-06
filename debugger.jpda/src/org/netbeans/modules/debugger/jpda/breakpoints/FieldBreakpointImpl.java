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

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.WatchpointEvent;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.request.AccessWatchpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.ModificationWatchpointRequest;
import com.sun.jdi.request.WatchpointRequest;

import org.netbeans.api.debugger.Breakpoint.VALIDITY;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;

import org.openide.util.NbBundle;

/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public class FieldBreakpointImpl extends ClassBasedBreakpoint {

    
    private FieldBreakpoint breakpoint;
    
    
    public FieldBreakpointImpl (FieldBreakpoint breakpoint, JPDADebuggerImpl debugger, Session session) {
        super (breakpoint, debugger, session);
        this.breakpoint = breakpoint;
        set ();
    }
    
    protected void setRequests () {
        boolean access = (breakpoint.getBreakpointType () & 
                          FieldBreakpoint.TYPE_ACCESS) != 0;
        if (access && !getVirtualMachine().canWatchFieldAccess()) {
            setValidity(VALIDITY.INVALID,
                    NbBundle.getMessage(FieldBreakpointImpl.class, "MSG_NoFieldAccess"));
            return ;
        }
        boolean modification = (breakpoint.getBreakpointType () & 
                                FieldBreakpoint.TYPE_MODIFICATION) != 0;
        if (modification && !getVirtualMachine().canWatchFieldModification()) {
            setValidity(VALIDITY.INVALID,
                    NbBundle.getMessage(FieldBreakpointImpl.class, "MSG_NoFieldModification"));
            return ;
        }
        setClassRequests (
            new String[] {breakpoint.getClassName ()},
            new String[0],
            ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED
        );
        checkLoadedClasses (breakpoint.getClassName (), null);
    }
    
    protected void classLoaded (ReferenceType referenceType) {
        Field f = referenceType.fieldByName (breakpoint.getFieldName ());
        if (f == null) {
            setValidity(VALIDITY.INVALID,
                    NbBundle.getMessage(FieldBreakpointImpl.class, "MSG_NoField", referenceType.name(), breakpoint.getFieldName ()));
            return ;
        }
        try {
            if ( (breakpoint.getBreakpointType () & 
                  FieldBreakpoint.TYPE_ACCESS) != 0
            ) {
                AccessWatchpointRequest awr = getEventRequestManager ().
                    createAccessWatchpointRequest (f);
                setFilters(awr);
                addEventRequest (awr);
            }
            if ( (breakpoint.getBreakpointType () & 
                  FieldBreakpoint.TYPE_MODIFICATION) != 0
            ) {
                ModificationWatchpointRequest mwr = getEventRequestManager ().
                    createModificationWatchpointRequest (f);
                setFilters(mwr);
                addEventRequest (mwr);
            }
            setValidity(VALIDITY.VALID, null);
        } catch (VMDisconnectedException e) {
        }
    }
    
    protected EventRequest createEventRequest(EventRequest oldRequest) {
        if (oldRequest instanceof AccessWatchpointRequest) {
            Field field = ((AccessWatchpointRequest) oldRequest).field();
            WatchpointRequest awr = getEventRequestManager ().createAccessWatchpointRequest (field);
            setFilters(awr);
            return awr;
        }
        if (oldRequest instanceof ModificationWatchpointRequest) {
            Field field = ((ModificationWatchpointRequest) oldRequest).field();
            WatchpointRequest mwr = getEventRequestManager ().createModificationWatchpointRequest (field);
            setFilters(mwr);
            return mwr;
        }
        return null;
    }

    private void setFilters(WatchpointRequest wr) {
        JPDAThread[] threadFilters = breakpoint.getThreadFilters(getDebugger());
        if (threadFilters != null && threadFilters.length > 0) {
            for (JPDAThread t : threadFilters) {
                wr.addThreadFilter(((JPDAThreadImpl) t).getThreadReference());
            }
        }
        ObjectVariable[] varFilters = breakpoint.getInstanceFilters(getDebugger());
        if (varFilters != null && varFilters.length > 0) {
            for (ObjectVariable v : varFilters) {
                wr.addInstanceFilter((ObjectReference) ((JDIVariable) v).getJDIValue());
            }
        }
    }
    
    public boolean processCondition(Event event) {
        ThreadReference thread;
        if (event instanceof ModificationWatchpointEvent) {
            thread = ((ModificationWatchpointEvent) event).thread();
        } else if (event instanceof AccessWatchpointEvent) {
            thread = ((AccessWatchpointEvent) event).thread();
        } else {
            return true; // Empty condition, always satisfied.
        }
        return processCondition(event, breakpoint.getCondition (), thread, null);
    }

    public boolean exec (Event event) {
        if (event instanceof ModificationWatchpointEvent)
            return perform (
                event,
                ((WatchpointEvent) event).thread (),
                ((LocatableEvent) event).location ().declaringType (),
                ((ModificationWatchpointEvent) event).valueToBe ()
            );
        if (event instanceof AccessWatchpointEvent)
            return perform (
                event,
                ((WatchpointEvent) event).thread (),
                ((LocatableEvent) event).location ().declaringType (),
                ((AccessWatchpointEvent) event).valueCurrent ()
            );
        return super.exec (event);
    }
}

