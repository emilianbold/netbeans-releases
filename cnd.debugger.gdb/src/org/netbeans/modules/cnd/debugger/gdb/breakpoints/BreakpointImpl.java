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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.gdb.event.GdbBreakpointEvent;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;

/**
 *
 * @author   Jan Jancura and Gordon Prieur
 */
public abstract class BreakpointImpl implements PropertyChangeListener {
    
    /* valid breakpoint states */
    public static final String BPSTATE_UNVALIDATED = "BpState_Unvalidated"; // NOI18N
    public static final String BPSTATE_REVALIDATE = "BpState_Revalidate"; // NOI18N
    public static final String BPSTATE_VALIDATION_PENDING = "BpState_ValidationPending"; // NOI18N
    public static final String BPSTATE_VALIDATION_FAILED = "BpState_ValidationFailed"; // NOI18N
    public static final String BPSTATE_VALIDATED = "BpState_Validated"; // NOI18N
    public static final String BPSTATE_DELETION_PENDING = "BpState_DeletionPending"; // NOI18N
    
    private GdbDebugger debugger;
    private String state;
    private int breakpointNumber;
    private GdbBreakpoint breakpoint;
    private BreakpointsReader reader;
    private final Session session;
    private String err;
    private boolean runWhenValidated;
    
    protected BreakpointImpl(GdbBreakpoint breakpoint, BreakpointsReader reader, GdbDebugger debugger, Session session) {
        this.debugger = debugger;
        this.reader = reader;
        this.breakpoint = breakpoint;
        this.session = session;
        this.state = BPSTATE_UNVALIDATED;
        this.breakpointNumber = -1;
        this.err = null;
        runWhenValidated = false;
    }

    public void completeValidation(Map<String, String> map) {
        String number;
        
        if (!getState().equals(BPSTATE_DELETION_PENDING)) {
            assert getState().equals(BPSTATE_VALIDATION_PENDING) : getState();
            if (map != null) {
                number = map.get("number"); // NOI18N
            } else {
                number = null;
            }
            if (number != null) {
                breakpointNumber = Integer.parseInt(number);
                setState(BPSTATE_VALIDATED);
                if (!breakpoint.isEnabled()) {
                    getDebugger().getGdbProxy().break_disable(breakpointNumber);
                }
                String condition = breakpoint.getCondition();
                if (condition.length() > 0) {
                    getDebugger().getGdbProxy().break_condition(breakpointNumber, condition);
                }
                int skipCount = breakpoint.getSkipCount();
                if (skipCount > 0) {
                    getDebugger().getGdbProxy().break_after(breakpointNumber, Integer.toString(skipCount));
                }
                if (this instanceof FunctionBreakpointImpl) {
                    try {
                        breakpoint.setURL(map.get("fullname")); // NOI18N
                        breakpoint.setLineNumber(Integer.parseInt(map.get("line"))); // NOI18N
                    } catch (Exception ex) {
                    }
                }
                breakpoint.setValid();
            } else {
                breakpoint.setInvalid(err);
                setState(BPSTATE_VALIDATION_FAILED);
            }
            getDebugger().getBreakpointList().put(number, this);
        }
    }
    
    public void addError(String err) {
        if (this.err != null) {
            this.err = this.err + err;
        } else {
            this.err = err;
        }
    }

    /**
     * Get the state of this breakpoint
     */
    protected String getState() {
        return state;
    }

    /** Set the state of this breakpoint */
    protected void setState(String state) {
        if (!state.equals(this.state) &&
                (state.equals(BPSTATE_UNVALIDATED) ||
                 state.equals(BPSTATE_REVALIDATE) ||
                 state.equals(BPSTATE_VALIDATION_PENDING) ||
                 state.equals(BPSTATE_VALIDATION_FAILED) ||
                 state.equals(BPSTATE_VALIDATED) ||
                 state.equals(BPSTATE_DELETION_PENDING))) {
            this.state = state;
            if (state.equals(BPSTATE_UNVALIDATED)) {
                setBreakpointNumber(-1);
            }
        }
    }

    public int getBreakpointNumber() {
        return breakpointNumber;
    }

    protected void setBreakpointNumber(int breakpointNumber) {
        this.breakpointNumber = breakpointNumber;
    }

    /**
     * Called from XXXBreakpointImpl constructor only.
     */
    final void set() {
        breakpoint.setDebugger(getDebugger());
        breakpoint.addPropertyChangeListener(this);
        update();
    }

    protected abstract void setRequests();
    
    protected void suspend() {
        getDebugger().getGdbProxy().break_delete(getBreakpointNumber());
        setState(BPSTATE_UNVALIDATED);
        setRequests();
    }

    /**
     * Called when Fix&Continue is invoked. Reqritten in LineBreakpointImpl.
     */
    void fixed() {
        update();
    }

    /**
     * Called from set () and propertyChanged.
     */
    final void update() {
        if (!getDebugger().getState().equals(GdbDebugger.STATE_NONE)) {
            setRequests();
        }
    }
    
    protected final void setValidity(Breakpoint.VALIDITY validity, String reason) {
        if (breakpoint instanceof ChangeListener) {
            ((ChangeListener) breakpoint).stateChanged(new ValidityChangeEvent(validity, reason));
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String pname = evt.getPropertyName();
        if (pname.equals(Breakpoint.PROP_DISPOSED)) {
            remove();
        }
        if (pname.equals(GdbBreakpoint.PROP_CONDITION)) {
            getDebugger().getGdbProxy().break_condition(breakpointNumber, evt.getNewValue().toString());
        } else if (pname.equals(GdbBreakpoint.PROP_SKIP_COUNT)) {
            getDebugger().getGdbProxy().break_after(breakpointNumber, evt.getNewValue().toString());
        } else if (pname.equals(GdbBreakpoint.PROP_SUSPEND)) {
            suspend();
        } else if (pname.equals(GdbBreakpoint.PROP_LINE_NUMBER) && getState().equals(BPSTATE_VALIDATED)) {
            setState(BPSTATE_REVALIDATE);
            update();
        } else if (pname.equals(GdbBreakpoint.PROP_FUNCTION_NAME) && getState().equals(BPSTATE_VALIDATED)) {
            setState(BPSTATE_REVALIDATE);
            update();
        } else if (!pname.equals(GdbBreakpoint.PROP_LINE_NUMBER) && !AddressBreakpoint.PROP_REFRESH.equals(pname)) {
            update();
        }
    }

    protected final void remove() {
        breakpoint.removePropertyChangeListener(this);
        setState(BPSTATE_DELETION_PENDING);
        setValidity(Breakpoint.VALIDITY.UNKNOWN, null);
        if (breakpointNumber > 0) {
            getDebugger().getBreakpointList().remove(breakpointNumber);
        }
    }

    public GdbBreakpoint getBreakpoint() {
        return breakpoint;
    }

    protected GdbDebugger getDebugger() {
        return debugger;
    }

    public boolean perform(String condition) {
        boolean resume = false;

        if (condition == null || condition.equals("")) { // NOI18N
            GdbBreakpointEvent e = new GdbBreakpointEvent(getBreakpoint(), getDebugger(), GdbBreakpointEvent.CONDITION_NONE, null);
            getDebugger().fireBreakpointEvent(getBreakpoint(), e);
            //resume = getBreakpoint().getSuspend() == GdbBreakpoint.SUSPEND_NONE || e.getResume();
        } else {
            //resume = evaluateCondition(condition, thread, referenceType, value);
            //PATCH 48174
            //resume = getBreakpoint().getSuspend() == GdbBreakpoint.SUSPEND_NONE || resume;
        }
        if (!resume) {
            DebuggerManager.getDebuggerManager().setCurrentSession(session);
            //getDebugger().setStoppedState(thread);
        }
        return resume;
    }
    
    public void setRunWhenValidated(boolean runWhenValidated) {
        this.runWhenValidated = runWhenValidated;
    }
    
    public boolean isRunWhenValidated() {
        return runWhenValidated;
    }
        
    private static final class ValidityChangeEvent extends ChangeEvent {
        
        private String reason;
        
        public ValidityChangeEvent(Breakpoint.VALIDITY validity, String reason) {
            super(validity);
            this.reason = reason;
        }
        
        @Override
        public String toString() {
            return reason;
        }
    }
}