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
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.modules.cnd.api.compilers.PlatformTypes;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.proxy.MICommand;

/**
 *
 * @author   Jan Jancura and Gordon Prieur
 */
public abstract class BreakpointImpl implements PropertyChangeListener {

    /* valid breakpoint states */
    private static final String BPSTATE_UNVALIDATED = "BpState_Unvalidated"; // NOI18N
    protected static final String BPSTATE_REVALIDATE = "BpState_Revalidate"; // NOI18N
    private static final String BPSTATE_VALIDATION_PENDING = "BpState_ValidationPending"; // NOI18N
    private static final String BPSTATE_VALIDATION_FAILED = "BpState_ValidationFailed"; // NOI18N
    private static final String BPSTATE_VALIDATED = "BpState_Validated"; // NOI18N
    private static final String BPSTATE_DELETION_PENDING = "BpState_DeletionPending"; // NOI18N

    protected final GdbDebugger debugger;
    private final GdbBreakpoint breakpoint;
    private String state = BPSTATE_UNVALIDATED;
    protected String err = null;
    private int breakpointNumber = -1;
    private boolean runWhenValidated = false;

    protected BreakpointImpl(GdbBreakpoint breakpoint, GdbDebugger debugger) {
        this.debugger = debugger;
        this.breakpoint = breakpoint;
    }

    public final void completeValidation(Map<String, String> map) {
        if (getState().equals(BPSTATE_DELETION_PENDING)) {
            return;
        }
        
        String number = (map != null) ? map.get("number") : null; // NOI18N
        if (number != null) {
            String fullname = map.get("fullname"); // NOI18N
            
            // Note: The following test is appropriate only for line breakpoints...
            if (this instanceof LineBreakpointImpl && fullname != null) {
                // We set a valid breakpoint, but its not in the exact source file we meant it to
                // be. This can happen when a source path has embedded spaces and we shorten the
                // path to a possiby non-unique relative path and another project has a similar
                // relative path. See IZ #151761.
                String path = getBreakpoint().getPath();
                // fix for IZ 157752, we need to resolve sym links
                // TODO: what about remote?
                if (debugger.getHostExecutionEnvironment().isLocal()) {
                    path = canonicalPath(path);
                    fullname = canonicalPath(fullname);
                }
                
                if (debugger.getPlatform() == PlatformTypes.PLATFORM_MACOSX) {
                    // See IZ 151577 - do some magic to ensure equivalent paths really do match
                    path = path.toLowerCase();
                    fullname = fullname.toLowerCase();
                }
                // go through path map
                path = debugger.getPathMap().getRemotePath(path);
                
                if (!debugger.comparePaths(path, fullname)) {
                    debugger.getGdbProxy().getLogger().logMessage(
                            "IDE: incorrect breakpoint file: requested " + path + " found " + fullname); // NOI18N
                    debugger.getGdbProxy().break_deleteCMD(number).send();
                    breakpoint.setInvalid(err);
                    setState(BPSTATE_VALIDATION_FAILED);
                    return;
                }
            }
            breakpointNumber = Integer.parseInt(number);
            setState(BPSTATE_VALIDATED);
            if (!breakpoint.isEnabled()) {
                enableCMD(false).send();
            }
            String condition = breakpoint.getCondition();
            if (condition.length() > 0) {
                MICommand command = requestConditionCMD(condition);
                if (isRunWhenValidated()) {
                    debugger.addRunAfterToken(command.getToken());
                }
                command.send();
            }
            int skipCount = breakpoint.getSkipCount();
            if (skipCount > 0) {
                MICommand command = requestBreakAfterCMD(skipCount);
                if (isRunWhenValidated()) {
                    debugger.addRunAfterToken(command.getToken());
                }
                command.send();
            }

            //TODO: not good to check for child type here
            if (this instanceof FunctionBreakpointImpl) {
                try {
                    breakpoint.setURL(map.get("fullname")); // NOI18N
                    breakpoint.setLineNumber(Integer.parseInt(map.get("line"))); // NOI18N
                } catch (Exception ex) {
                }
            }
            breakpoint.setValid();
            debugger.getBreakpointList().put(breakpointNumber, this);
            setRunWhenValidated(false);
        } else {
	    if (alternateSourceRootAvailable()) {
		setState(BPSTATE_UNVALIDATED);
		setRequests();
	    } else {
		breakpoint.setInvalid(err);
		setState(BPSTATE_VALIDATION_FAILED);
	    }
        }
    }

    private static String canonicalPath(String path) {
        try {
            return new File(path).getCanonicalPath();
        } catch (IOException ex) {
            // do nothing
        }
        return path;
    }

    protected boolean alternateSourceRootAvailable() {
	return false;
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
                this.breakpointNumber = -1;
            }
        }
    }

    /**
     * Called from XXXBreakpointImpl constructor only.
     */
    final void set() {
        breakpoint.setDebugger(debugger);
        breakpoint.addPropertyChangeListener(this);
        update();
    }

    protected abstract String getBreakpointCommand();

    protected void setRequests() {
        String st = getState();
        if (debugger.getState() == GdbDebugger.State.RUNNING && !st.equals(BPSTATE_REVALIDATE)) {
            debugger.setSilentStop();
            setRunWhenValidated(true);
        }
        if (st.equals(BPSTATE_UNVALIDATED) || st.equals(BPSTATE_REVALIDATE)) {
            if (st.equals(BPSTATE_REVALIDATE) && breakpointNumber > 0) {
                requestDelete();
            }
            setState(BPSTATE_VALIDATION_PENDING);
	    String bpcmd = getBreakpointCommand();
	    if (bpcmd != null) {
		MICommand command = debugger.getGdbProxy().break_insertCMD(
                        getBreakpoint().getSuspend(),
			getBreakpoint().isTemporary(), 
                        bpcmd,
                        getBreakpoint().getThreadID());
		debugger.addPendingBreakpoint(command.getToken(), this);
                if (isRunWhenValidated()) {
                    debugger.addRunAfterToken(command.getToken());
                }
                command.send();
	    } else {
		breakpoint.setInvalid(err);
		setState(BPSTATE_VALIDATION_FAILED);
	    }
	} else {
            if (breakpointNumber > 0) { // bnum < 0 for breakpoints from other projects...
                MICommand command = null;
                if (st.equals(BPSTATE_DELETION_PENDING)) {
                    command = debugger.getGdbProxy().break_deleteCMD(breakpointNumber);
                } else if (st.equals(BPSTATE_VALIDATED)) {
                    command = enableCMD(getBreakpoint().isEnabled());
                }
                if (command != null) {
                    if (isRunWhenValidated()) {
                        setRunWhenValidated(false);
                        debugger.addRunAfterToken(command.getToken());
                    }
                    command.send();
                }
            }
	}
    }

    private MICommand enableCMD(boolean enable) {
        if (enable) {
            return debugger.getGdbProxy().break_enableCMD(breakpointNumber);
        } else {
            return debugger.getGdbProxy().break_disableCMD(breakpointNumber);
        }
    }

    protected void suspend() {
        requestDelete();
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
        if (debugger.getState() != GdbDebugger.State.EXITED) {
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
        if (pname.equals(GdbBreakpoint.PROP_CONDITION)) {
            // TODO: not capable of setting on the fly
            requestConditionCMD(evt.getNewValue().toString()).send();
        } else if (pname.equals(GdbBreakpoint.PROP_SKIP_COUNT)) {
            // TODO: not capable of setting on the fly
            requestBreakAfterCMD((Integer)evt.getNewValue()).send();
        } else if (pname.equals(GdbBreakpoint.PROP_ENABLED)) {
            update();
        } else if (pname.equals(GdbBreakpoint.PROP_SUSPEND)) {
            suspend();
        } else if (pname.equals(GdbBreakpoint.PROP_LINE_NUMBER) && getState().equals(BPSTATE_VALIDATED) && !(getBreakpoint() instanceof FunctionBreakpoint)) {
            setState(BPSTATE_REVALIDATE);
            update();
        } else if (pname.equals(GdbBreakpoint.PROP_FUNCTION_NAME) && getState().equals(BPSTATE_VALIDATED)) {
            setState(BPSTATE_REVALIDATE);
            update();
        } else if (pname.equals(AddressBreakpoint.PROP_ADDRESS_VALUE) && getState().equals(BPSTATE_VALIDATED)) {
            setState(BPSTATE_REVALIDATE);
            update();
        }
    }

    private MICommand requestConditionCMD(String condition) {
        return debugger.getGdbProxy().break_conditionCMD(breakpointNumber, condition);
    }

    private MICommand requestBreakAfterCMD(int skipCount) {
        return debugger.getGdbProxy().break_afterCMD(breakpointNumber, skipCount);
    }

    private void requestDelete() {
        debugger.getGdbProxy().break_deleteCMD(breakpointNumber).send();
    }

    protected final void remove() {
        breakpoint.removePropertyChangeListener(this);
        setState(BPSTATE_DELETION_PENDING);
        setValidity(Breakpoint.VALIDITY.UNKNOWN, null);
        if (breakpointNumber > 0) {
            debugger.getBreakpointList().remove(breakpointNumber);
        }
        update();
    }

    public GdbBreakpoint getBreakpoint() {
        return breakpoint;
    }

    /*public boolean perform(String condition) {
        boolean resume = false;

        if (condition == null || condition.equals("")) { // NOI18N
            GdbBreakpointEvent e = new GdbBreakpointEvent(getBreakpoint(), debugger, GdbBreakpointEvent.CONDITION_NONE, null);
            debugger.fireBreakpointEvent(getBreakpoint(), e);
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
    }*/

    private void setRunWhenValidated(boolean runWhenValidated) {
        this.runWhenValidated = runWhenValidated;
    }

    private boolean isRunWhenValidated() {
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
