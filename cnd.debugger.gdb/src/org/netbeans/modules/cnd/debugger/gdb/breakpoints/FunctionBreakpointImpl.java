/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.debugger.Session;

import org.netbeans.modules.cnd.debugger.gdb.GdbDebuggerImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 * Implementation of breakpoint on function.
 *
 * @author Nik Molchanov (copied from Jan Jancura's JPDA implementation)
 */
public class FunctionBreakpointImpl extends BreakpointImpl {
    
    private FunctionBreakpoint  breakpoint;
    private FunctionBreakpoint  latestBreakpoint = null;
    private String              functionName;
    private BreakpointsReader   reader;
    
    
    public FunctionBreakpointImpl(FunctionBreakpoint breakpoint, BreakpointsReader reader,
            GdbDebuggerImpl debugger, Session session) {
        super(breakpoint, reader, debugger, session);
        this.reader = reader;
        this.breakpoint = breakpoint;
        functionName = breakpoint.getFunctionName();
        set();
    }
    
//    void fixed() {
//        functionName = breakpoint.getFunctionName();
//        super.fixed();
//    }
    
    protected void setRequests() {
        synchronized (breakpoint) {
            int bps = breakpoint.getState();
            // If state is UNVALIDATED - set breakpoint
            if (bps == GdbBreakpoint.UNVALIDATED) {
                //Performance measurements: 91-107 mls (2006/08/29)
                //getDebugger().getGdbProxy().globalStartTimeSetBreakpoint = System.currentTimeMillis(); // DEBUG
                functionName = breakpoint.getFunctionName();
                getDebugger().getGdbProxy().break_insert(breakpoint.getID(), functionName);
                breakpoint.setPending();
                return;
            }
            // Actions below require a valid breakpoint number ( > 0 )
            int bpn = breakpoint.getBreakpointNumber();
            if (bpn <= 0) return;
	    if (bps == GdbBreakpoint.DELETION_PENDING) {
		getDebugger().getGdbProxy().break_delete(bpn);
                return;
	    }
            if (breakpoint.isEnabled()) {
		getDebugger().getGdbProxy().break_enable(bpn);
	    } else {
		getDebugger().getGdbProxy().break_disable(bpn);
            }
        }
    }
}

