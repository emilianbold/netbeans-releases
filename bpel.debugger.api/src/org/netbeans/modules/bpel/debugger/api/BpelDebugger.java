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

package org.netbeans.modules.bpel.debugger.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Properties;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.bpel.debugger.api.breakpoints.LineBreakpoint;
import org.netbeans.spi.debugger.ContextProvider;

/**
 *
 * @author Alexander Zgursky
 */
public abstract class BpelDebugger {

    /** Name of property for state of debugger. */
    public static final String          PROP_STATE = "state"; // NOI18N
    
    /** Name of property for current process instance. */
    public static final String          PROP_CURRENT_PROCESS_INSTANCE = 
            "currentProcessInstance";   //NOI18N
    
    /** Name of property for current process instance. */
    public static final String          PROP_CURRENT_PROCESS_INSTANCE_STATE = 
            "currentProcessInstanceState";  //NOI18N
    
    /** Name of property for current position. */
    public static final String          PROP_CURRENT_POSITION = 
            "currentPosition";  //NOI18N
    
    /** Debugger state constant. */
    public static final int             STATE_STARTING = 1;
    /** Debugger state constant. */
    public static final int             STATE_RUNNING = 2;
    /** Debugger state constant. */
    public static final int             STATE_DISCONNECTED = 4;
    
    private PropertyChangeSupport         myPcs;
    private ContextProvider               myLookupProvider;
    private Tracer mTracer;

        
    protected BpelDebugger(ContextProvider lookupProvider) {
        myLookupProvider = lookupProvider;
        myPcs = new PropertyChangeSupport(this);
    }
    
    /**
     * Returns tracer to be used for the current debug session.
     * 
     * @return tracer to be used for the current debug session
     */
    public Tracer getTracer() {
        if (mTracer == null) {
            mTracer = TracerAccess.getTracer(getLookupProvider());
        }
        return mTracer;
    }

    /**
     * Starts BPEL debug session.
     * @param props properties to be used to start a debug session
     */
    public static void start(Properties props) {
        ProcessDICookie cookie = ProcessDICookie.create(props);
        
        DebuggerManager.getDebuggerManager().startDebugging(
                DebuggerInfo.create(ProcessDICookie.ID,
                    new Object [] {cookie}));
    }
    
    
    /**
     * Stops the debugger and terminates the debugging session.
     */
    public abstract void finish();
    
    /**
     * Causes current process instance to do a step into or does nothing if
     * there's no current process instance or it's not in the suspended state.
     */
    public abstract void stepInto();
    
    /**
     * Causes current process instance to do a step over or does nothing if
     * there's no current process instance or it's not in the suspended state.
     */
    //public abstract void stepOver();
    
    /**
     * Resumes the execution of the current process instance or does nothing if
     * there's no current process instance or it's not in the suspended state.
     */
    public abstract void resume();
    
    /**
     * Returns current state of BPEL debugger.
     *
     * @return current state of BPEL debugger
     * @see #STATE_STARTING
     * @see #STATE_RUNNING
     * @see #STATE_DISCONNECTED
     */
    public abstract int getState();
    
    public abstract ProcessInstancesModel getProcessInstancesModel();
    
    /**
     * Returns state of the current process instance.
     *
     * @return state of the the current process instance or
     *         {@link ProcessInstance#STATE_UNKNOWN} if there's no
     *         current instance
     *
     * @see ProcessInstance#STATE_UNKNOWN
     * @see ProcessInstance#STATE_RUNNING
     * @see ProcessInstance#STATE_SUSPENDED
     * @see ProcessInstance#STATE_COMPLETED
     * @see ProcessInstance#STATE_FAILED
     * @see ProcessInstance#STATE_TERMINATED
     */
    public abstract int getCurrentProcessInstanceState();
    
    /**
     * Returns current process instance.
     *
     * @return current process instance
     *         or null if there's no current process instance
     */
    public abstract ProcessInstance getCurrentProcessInstance();
    
    /**
     * Sets the current process instance. Does nothing if given process instance
     * doesn't exist in the model.
     *
     * @param processInstance process instance to set as current
     */
    public abstract void setCurrentProcessInstance(
            ProcessInstance processInstance);
    
    /**
     * Returns position at which current process instance has been suspended.
     *
     * @return  current position at which current process instance has been
     *          suspended or <code>null</code> if there's no current process
     *          instance or it's not suspended
     *
     * @see #getCurrentProcessInstance()
     * @see ProcessInstance#getState()
     * @see ProcessInstance#getCurrentPosition
     */
    public abstract Position getCurrentPosition();
    
    /**
     * Returns an exception that caused debugger to disconnect.
     *
     * @return an exception that caused debugger to disconnect or null if
     *          there was no exception
     */
    public abstract Exception getException();
    
    public abstract void runToCursor(LineBreakpoint breakpoint);
    
    /**
     * Fires property change.
     */
    protected final void firePropertyChange(String name, Object o, Object n) {
        myPcs.firePropertyChange(name, o, n);
    }
    
    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        myPcs.addPropertyChangeListener(l);
    }
    
    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        myPcs.removePropertyChangeListener(l);
    }
    
    /**
     * Adds property change listener.
     *
     * @param propertyName property name to add listener for
     * @param l new listener.
     */
    public final void addPropertyChangeListener(
            String propertyName, PropertyChangeListener l)
    {
        myPcs.addPropertyChangeListener(propertyName, l);
    }
    
    /**
     * Removes property change listener.
     *
     * @param propertyName property name to remove listener for
     * @param l listener to remove
     */
    public final void removePropertyChangeListener(
            String propertyName, PropertyChangeListener l)
    {
        myPcs.removePropertyChangeListener(propertyName, l);
    }
    
    /**
     * Returns the context of the debugger.
     *
     * @return the context of the debugger
     */
    public final ContextProvider getLookupProvider() {
        return myLookupProvider;
    }
}
