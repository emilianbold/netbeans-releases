/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.client.tools.javascript.debugger.api;

/**
 *
 * @author Sandip V. Chitale <sandipchitale@netbeans.org>
 */
public final class JSDebuggerState {

    public static enum State {
        NOT_CONNECTED,
        STARTING,
        RUNNING,
        SUSPENDED,
        DISCONNECTED
    }

    public static enum Reason {
        NONE,
        INIT,
        READY,
        
        FIRST_LINE,
        BREAKPOINT,
        STEP,
        DEBUGGER,
        
        ERROR,
        EXCEPTION,
        USER
    }

    private State state;
    private Reason reason;
    private JSBreakpoint breakpoint;

    public static final JSDebuggerState NOT_CONNECTED =
        new JSDebuggerState(State.NOT_CONNECTED);

    public static final JSDebuggerState STARTING_INIT =
                new JSDebuggerState(State.STARTING, Reason.INIT);
    public static final JSDebuggerState STARTING_READY =
            new JSDebuggerState(State.STARTING, Reason.READY);
    
    public static final JSDebuggerState RUNNING =
                new JSDebuggerState(State.RUNNING);
    
    public static final JSDebuggerState SUSPENDED_FIRST_LINE =
            new JSDebuggerState(State.SUSPENDED, Reason.FIRST_LINE);
    
    public static JSDebuggerState getDebuggerState(JSBreakpoint breakpoint) {
        JSDebuggerState debuggerState =
                new JSDebuggerState(State.SUSPENDED, Reason.BREAKPOINT);
        debuggerState.setBreakpoint(breakpoint);
        return debuggerState;
    }

    public static JSDebuggerState getDebuggerState(JSBreakpoint breakpoint, Reason reason) {
        JSDebuggerState debuggerState =
                new JSDebuggerState(State.SUSPENDED, reason);
        debuggerState.setBreakpoint(breakpoint);
        return debuggerState;
    }

    public static JSDebuggerState getDebuggerState(JSErrorInfo errorInfo) {
        JSDebuggerState debuggerState =
                new JSDebuggerState(State.SUSPENDED, Reason.EXCEPTION);        
        return debuggerState;
    }
    
    public static final JSDebuggerState SUSPENDED_STEP =
            new JSDebuggerState(State.SUSPENDED, Reason.STEP);
    
    public static final JSDebuggerState SUSPENDED_DEBUGGER =
            new JSDebuggerState(State.SUSPENDED, Reason.DEBUGGER);

    public static final JSDebuggerState DISCONNECTED_USER =
        new JSDebuggerState(State.DISCONNECTED, Reason.USER);
    public static final JSDebuggerState DISCONNECTED =
        new JSDebuggerState(State.DISCONNECTED);

    private JSDebuggerState(State state) {
        this(state, Reason.NONE);
    }

    private JSDebuggerState(State state, Reason reason) {
        this.state = state;
        this.reason = reason;
    }

    public State getState() {
        return state;
    }

    public Reason getReason() {
        return reason;
    }

    public JSBreakpoint getBreakpoint() {
        return breakpoint;
    }

    private void setBreakpoint(JSBreakpoint breakpoint) {
        assert (state == State.SUSPENDED && reason == Reason.BREAKPOINT);
        this.breakpoint = breakpoint;
    }

    @Override
    public String toString() {
        return "State: " + getState() + " Reason: " + getReason();
    }

}
