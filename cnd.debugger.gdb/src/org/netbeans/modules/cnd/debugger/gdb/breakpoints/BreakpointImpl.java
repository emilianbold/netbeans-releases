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

import org.netbeans.modules.cnd.debugger.common.breakpoints.FunctionBreakpoint;
import org.netbeans.modules.cnd.debugger.common.breakpoints.AddressBreakpoint;
import org.netbeans.modules.cnd.debugger.common.breakpoints.CndBreakpoint;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.netbeans.modules.cnd.api.compilers.PlatformTypes;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.proxy.MICommand;

/**
 *
 * @author   Jan Jancura and Gordon Prieur
 */
public abstract class BreakpointImpl implements PropertyChangeListener {

    /* valid breakpoint states */
    private static enum State {
        UNVALIDATED,
        REVALIDATE,
        VALIDATION_PENDING,
        VALIDATION_FAILED,
        VALIDATED,
        DELETED
    }

    protected final GdbDebugger debugger;
    private final CndBreakpoint breakpoint;
    private State state = State.UNVALIDATED;
    protected String err = null;
    private int breakpointNumber = -1;
    private boolean runWhenValidated = false;

    protected BreakpointImpl(CndBreakpoint breakpoint, GdbDebugger debugger) {
        this.debugger = debugger;
        this.breakpoint = breakpoint;
    }

    public final void completeValidation(Map<String, String> map) {
        if (state == State.DELETED) {
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
                path = debugger.getPathMap().getRemotePath(path,true);
                
                if (!debugger.comparePaths(path, fullname)) {
                    debugger.getGdbProxy().getLogger().logMessage(
                            "IDE: incorrect breakpoint file: requested " + path + " found " + fullname); // NOI18N
                    debugger.getGdbProxy().break_deleteCMD(number).send();
                    breakpoint.setInvalid(err);
                    setState(State.VALIDATION_FAILED);
                    return;
                }
            }
            breakpointNumber = Integer.parseInt(number);
            setState(State.VALIDATED);
            if (!breakpoint.isEnabled()) {
                MICommand command = enableCMD(false);
                if (isRunWhenValidated()) {
                    debugger.addRunAfterToken(command.getToken());
                }
                command.send();
            }
            String condition = breakpoint.getCondition();
            if (condition.length() > 0) {
                MICommand command = conditionCMD(condition);
                if (isRunWhenValidated()) {
                    debugger.addRunAfterToken(command.getToken());
                }
                command.send();
            }
            int skipCount = breakpoint.getSkipCount();
            if (skipCount > 0) {
                MICommand command = breakAfterCMD(skipCount);
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
		setState(State.UNVALIDATED);
		setRequests();
	    } else {
		breakpoint.setInvalid(err);
		setState(State.VALIDATION_FAILED);
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

    // Overrided in LineBreakpointImpl
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

    /** Set the state of this breakpoint */
    private void setState(State state) {
        this.state = state;
        if (state == State.UNVALIDATED) {
            this.breakpointNumber = -1;
        }
    }

    /**
     * Called from XXXBreakpointImpl constructor only.
     */
    protected final void set() {
        breakpoint.addPropertyChangeListener(this);
        update();
    }

    protected abstract String getBreakpointCommand();

    private void setRequests() {
        if (state == State.UNVALIDATED || state == State.REVALIDATE) {
            if (debugger.getState() == GdbDebugger.State.RUNNING) {
                setRunWhenValidated(true);
            }
            if (state == State.REVALIDATE && breakpointNumber > 0) {
                send(deleteCMD());
            }
            setState(State.VALIDATION_PENDING);
	    String bpcmd = getBreakpointCommand();
	    if (bpcmd != null) {
		MICommand command = debugger.getGdbProxy().break_insertCMD(
                        getBreakpoint().getSuspend(),
			getBreakpoint().isTemporary(), 
                        bpcmd,
                        getBreakpoint().getThreadID());
		debugger.addPendingBreakpoint(command.getToken(), this);
                send(command);
	    } else {
		breakpoint.setInvalid(err);
		setState(State.VALIDATION_FAILED);
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

    void revalidate() {
        setState(State.REVALIDATE);
        update();
    }

    /**
     * Called from set () and propertyChanged.
     */
    private void update() {
        if (debugger.getState() != GdbDebugger.State.EXITED) {
            setRequests();
        }
    }

    // Support sending on the fly (when debugger is in running state)
    private void send(MICommand command) {
        if (debugger.getState() == GdbDebugger.State.RUNNING) {
            debugger.addRunAfterToken(command.getToken());
            debugger.setSilentStop();
        }
        command.send();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String pname = evt.getPropertyName();
        if (pname.equals(CndBreakpoint.PROP_CONDITION)) {
            if (breakpointNumber > 0) {
                send(conditionCMD(evt.getNewValue().toString()));
            }
        } else if (pname.equals(CndBreakpoint.PROP_SKIP_COUNT)) {
            if (breakpointNumber > 0) {
                send(breakAfterCMD((Integer)evt.getNewValue()));
            }
        } else if (pname.equals(CndBreakpoint.PROP_ENABLED)) {
            if (breakpointNumber > 0) {
                send(enableCMD(breakpoint.isEnabled()));
            }
        } else if (pname.equals(CndBreakpoint.PROP_SUSPEND)) {
            revalidate();
        } else if (pname.equals(CndBreakpoint.PROP_LINE_NUMBER) &&
                state == State.VALIDATED &&
                !(getBreakpoint() instanceof FunctionBreakpoint) &&
                // see IZ:165386, null in old value if update is from line translations
                evt.getOldValue() != null) {
            revalidate();
        } else if (pname.equals(FunctionBreakpoint.PROP_FUNCTION_NAME) && state == State.VALIDATED) {
            revalidate();
        } else if (pname.equals(AddressBreakpoint.PROP_ADDRESS_VALUE) && state == State.VALIDATED) {
            revalidate();
        }
    }

    private MICommand conditionCMD(String condition) {
        return debugger.getGdbProxy().break_conditionCMD(breakpointNumber, condition);
    }

    private MICommand breakAfterCMD(int skipCount) {
        return debugger.getGdbProxy().break_afterCMD(breakpointNumber, skipCount);
    }

    private MICommand deleteCMD() {
        return debugger.getGdbProxy().break_deleteCMD(breakpointNumber);
    }

    final void remove() {
        breakpoint.removePropertyChangeListener(this);
        setState(State.DELETED);
        if (breakpointNumber > 0) {
            debugger.getBreakpointList().remove(breakpointNumber);
            send(deleteCMD());
        }
    }

    public CndBreakpoint getBreakpoint() {
        return breakpoint;
    }

    private void setRunWhenValidated(boolean runWhenValidated) {
        this.runWhenValidated = runWhenValidated;
    }

    private boolean isRunWhenValidated() {
        return runWhenValidated;
    }
}
