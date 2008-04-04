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

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import org.netbeans.api.debugger.Session;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;

/**
* Implementation of breakpoint on method.
*
* @author   Gordon Prieur (copied from Jan Jancura's JPDA implementation)
*/
public class LineBreakpointImpl extends BreakpointImpl {

    private LineBreakpoint      breakpoint;
    private int                 lineNumber;
    private BreakpointsReader   reader;

    
    public LineBreakpointImpl(LineBreakpoint breakpoint, BreakpointsReader reader,
                GdbDebugger debugger, Session session) {
        super(breakpoint, reader, debugger, session);
        this.reader = reader;
        this.breakpoint = breakpoint;
        lineNumber = breakpoint.getLineNumber();
        set();
    }
    
    @Override
    void fixed() {
        lineNumber = breakpoint.getLineNumber();
        super.fixed();
    }
    
    protected void setRequests() {
        String st = getState();
        if (getDebugger().getState().equals(GdbDebugger.STATE_RUNNING) && !st.equals(BPSTATE_REVALIDATE)) {
            getDebugger().setSilentStop();
            setRunWhenValidated(true);
        }
        if (st.equals(BPSTATE_UNVALIDATED) || st.equals(BPSTATE_REVALIDATE)) {
            if (st.equals(BPSTATE_REVALIDATE) && getBreakpointNumber() > 0) {
                getDebugger().getGdbProxy().break_delete(getBreakpointNumber());
            }
            setState(BPSTATE_VALIDATION_PENDING);
            lineNumber = breakpoint.getLineNumber();
            String path = getDebugger().getBestPath(breakpoint.getPath());
            int token;
            if (getBreakpoint().getSuspend() == GdbBreakpoint.SUSPEND_THREAD) {
                token = getDebugger().getGdbProxy().break_insert(GdbBreakpoint.SUSPEND_THREAD,
                        path + ':' + lineNumber, getBreakpoint().getThreadID());
            } else {
                token = getDebugger().getGdbProxy().break_insert(path + ':' + lineNumber);
            }
            getDebugger().addPendingBreakpoint(token, this);
	} else {
            int bnum = getBreakpointNumber();
            if (bnum > 0) { // bnum < 0 for breakpoints from other projects...
                if (st.equals(BPSTATE_DELETION_PENDING)) {
                    getDebugger().getGdbProxy().break_delete(bnum);
                } else if (st.equals(BPSTATE_VALIDATED)) {
                    if (breakpoint.isEnabled()) {
                        getDebugger().getGdbProxy().break_enable(bnum);
                    } else {
                        getDebugger().getGdbProxy().break_disable(bnum);
                    }
                }
                if (isRunWhenValidated()) {
                    getDebugger().setRunning();
                    setRunWhenValidated(false);
                }
            }
	}
    }
}

