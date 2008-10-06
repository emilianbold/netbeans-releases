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


import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.ThreadDeathRequest;
import com.sun.jdi.request.ThreadStartRequest;

import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.ThreadBreakpoint;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

import org.netbeans.modules.debugger.jpda.util.Executor;


/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public class ThreadBreakpointImpl extends BreakpointImpl implements Executor {

    // variables ...............................................................

    private ThreadBreakpoint              breakpoint;


    // init ....................................................................
    
    public ThreadBreakpointImpl (ThreadBreakpoint presenter, JPDADebuggerImpl debugger, Session session) {
        super (presenter, null, debugger, session);
        breakpoint = presenter;
        set ();
    }
            
       
    // Event impl ..............................................................

    protected void setRequests () {
        try {
            if ( (breakpoint.getBreakpointType () & 
                  breakpoint.TYPE_THREAD_STARTED ) != 0
            ) {
                ThreadStartRequest tsr = getEventRequestManager ().
                    createThreadStartRequest ();
                addEventRequest (tsr);
            }
            if ( (breakpoint.getBreakpointType () & 
                  breakpoint.TYPE_THREAD_DEATH) != 0
            ) {
                VirtualMachine vm = getVirtualMachine();
                if (vm != null) {
                    ThreadDeathRequest tdr = vm.eventRequestManager().
                        createThreadDeathRequest();
                    addEventRequest (tdr);
                }
            }
        } catch (VMDisconnectedException e) {
        }
    }
    
    protected EventRequest createEventRequest(EventRequest oldRequest) {
        if (oldRequest instanceof ThreadStartRequest) {
            return getEventRequestManager ().createThreadStartRequest ();
        }
        if (oldRequest instanceof ThreadDeathRequest) {
            return getEventRequestManager().createThreadDeathRequest();
        }
        return null;
    }

    public boolean processCondition(Event event) {
        return true; // Empty condition, always satisfied.
    }

    public boolean exec (Event event) {
        ThreadReference thread = null;
        if (event instanceof ThreadStartEvent)
            thread = ((ThreadStartEvent) event).thread ();
        else
        if (event instanceof ThreadDeathEvent)
            thread = ((ThreadDeathEvent) event).thread ();

        return perform (
            event,
            thread,
            null,
            thread
        );
    }

    public void removed(EventRequest eventRequest) {
    }
}
