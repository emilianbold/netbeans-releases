/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.debugger.common;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.windows.InputOutput;

/**
 * This class defines some methods which should allow a *Debugger class to be
 * passed to classes who don't know the implementation of the Debugger. For instance,
 * if GdbDebugger implements this class then I can pass the GdbDebugger as a CndDebugger
 * to a class/method which doesn't depend on the Gdb module.
 *
 * @author gordonp
 */
public abstract class CndDebugger implements PropertyChangeListener {
    
    public static final String PROP_STATE = "state"; // NOI18N
    public static final String PROP_CURRENT_THREAD = "currentThread"; // NOI18N
    public static final String PROP_CURRENT_CALL_STACK_FRAME = "currentCallStackFrame"; // NOI18N

    public static enum State { NONE, STARTING, LOADING, READY, RUNNING, STOPPED, SILENT_STOP, EXITED; }

    public ContextProvider lookupProvider;
    protected ExecutionEnvironment execEnv;
    protected int platform;
    private String exeName;
    protected State state = State.NONE;

    public CndDebugger(ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
    }

    public abstract void propertyChange(PropertyChangeEvent evt);
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {}
    public void addPropertyChangeListener(PropertyChangeListener l) {}
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener l) {}
    public void removePropertyChangeListener(PropertyChangeListener l) {}
    public abstract void firePropertyChange(String name, Object o, Object n);
    public abstract void finish(boolean killTerm);
    public abstract InputOutput getIOTab();
    public abstract String getRunDirectory();

    public ContextProvider getContextProvider() {
        return lookupProvider;
    }

    public String getExecutableName() {
        return exeName;
    }

    public void setExecutableName(String exeName) {
        this.exeName = exeName;
    }

    public ExecutionEnvironment getHostExecutionEnvironment() {
        return execEnv;
    }

    public void setHostExecutionEnvironment(ExecutionEnvironment execEnv) {
        this.execEnv = execEnv;
    }
    
    public static boolean isUnitTest() {
        return false;
    }

    public int getPlatform() {
        return platform;
    }

    public void setPlatform(int platform) {
        this.platform = platform;
    }

    /**
     * Returns current state of gdb debugger.
     *
     * @return current state of gdb debugger
     */
    public State getState() {
        return state;
    }

    /*
     * MCFIX - The gdb module makes heavy use of debugger state. A lot of that has to do with
     * the fact that I don't really know whats going on in the gdb binary unless I somehow
     * remember it. So the GdbDebugger state is part of how I remember whats going on.
     *
     * In your case, you probably have a state, but it will probably be significantly different
     * than the gdb module's state. For now, I've copied most of the states and their set methods.
     * Change them as needed!
     */
    public void setState(State state) {
        if (state == this.state) {
            return;
        }
        State oldState = this.state;
        this.state = state;
        firePropertyChange(PROP_STATE, oldState, state);
    }

    public void setStarting() {
        setState(State.STARTING);
    }

    public void setLoading() {
        setState(State.LOADING);
    }

    public void setReady() {
        setState(State.READY);
    }

    public void setRunning() {
        setState(State.RUNNING);
    }

    public void setStopped() {
        setState(State.STOPPED);
    }

    public void setSilentStop() {
        setState(State.SILENT_STOP);
    }

    public void setExited() {
        setState(State.EXITED);
    }

    public boolean isStopped() {
        return state == State.STOPPED;
    }
}
