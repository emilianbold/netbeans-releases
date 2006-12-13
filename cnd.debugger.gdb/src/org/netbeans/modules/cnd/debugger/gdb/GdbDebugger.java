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

package org.netbeans.modules.cnd.debugger.gdb;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Map;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.GdbBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.event.GdbBreakpointEvent;
//import org.netbeans.modules.cnd.debugger.Variable;
//import org.netbeans.modules.cnd.debugger.event.CndBreakpointEvent;


/**
 * Represents one Cnd debugger session.
 * 
 * <br><br>
 * <b>How to obtain it from DebuggerEngine:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    GdbDebugger cndDebugger = (GdbDebugger) debuggerEngine.lookup 
 *        (GdbDebugger.class);</pre>
 */
public abstract class GdbDebugger {

    /** Name of property for state of debugger. */
    public static final String          PROP_STATE = "state"; // NOI18N
    /** Name of property for current thread. */
    public static final String          PROP_CURRENT_THREAD = "currentThread"; // NOI18N
    /** Name of property for current stack frame. */
    public static final String          PROP_CURRENT_CALL_STACK_FRAME = "currentCallStackFrame"; // NOI18N
    /** Property name constant. */
    public static final String          PROP_SUSPEND = "suspend"; // NOI18N

    /** Suspend property value constant. */
    //public static final int             SUSPEND_ALL = EventRequest.SUSPEND_ALL;
    /** Suspend property value constant. */
    //public static final int             SUSPEND_EVENT_THREAD = EventRequest.SUSPEND_EVENT_THREAD;
   
    public static final String             STATE_NONE = "state_none"; // NOI18N
    public static final String             STATE_STARTING = "state_starting"; // NOI18N
    public static final String             STATE_LOADING = "state_loading"; // NOI18N
    public static final String             STATE_RUNNING = "state_running"; // NOI18N
    public static final String             STATE_STOPPED = "state_stopped"; // NOI18N
    public static final String             STATE_EXITED  = "state_exited"; // NOI18N
    
    /* Some breakpoint flags used only on Windows XP (with Cygwin) */
    public static final int                GDB_TMP_BREAKPOINT = 1;
    public static final int                GDB_INVISIBLE_BREAKPOINT = 2;
    
    /** ID of GDB Debugger Engine for C */
    public static final String          ENGINE_ID = "netbeans-cnd-GdbSession/C"; // NOI18N

    /** ID of GDB Debugger Session */
    public static final String          SESSION_ID = "netbeans-cnd-GdbSession"; // NOI18N

    /** ID of GDB Debugger SessionProvider */
    public static final String          SESSION_PROVIDER_ID = "netbeans-cnd-GdbSessionProvider"; // NOI18N
    

    
// XXX - I don't think this is used (in JPDA which is where I copied it from)
//    /**
//     * This utility method helps to start a new Cnd debugger session. 
//     * Its implementation use {@link LaunchingDICookie} and 
//     * {@link org.netbeans.api.debugger.DebuggerManager#getDebuggerManager}.
//     *
//     * @param mainClassName a name or main class
//     * @param args command line arguments
//     * @param classPath a classPath
//     * @param suspend if true session will be suspended
//     */
//    public static void launch(String progName, String[] args, boolean suspend) {
//        DebuggerManager.getDebuggerManager().startDebugging(
//            DebuggerInfo.create(SESSION_ID, new Object[] {})
//        );
//    }


    /**
     * This utility method helps to start a new Cnd debugger session. 
     *
     * @param hostName a name of computer to attach to
     * @param portNumber a port number
     */
    public static GdbDebugger attach(String hostName, int pid, Object[] services)
		    throws DebuggerStartException {
        DebuggerEngine[] es = DebuggerManager.getDebuggerManager().startDebugging(
                DebuggerInfo.create(SESSION_ID, null));
	int k = es.length;

        for (int i = 0; i < k; i++) {
            GdbDebugger d = (GdbDebugger) es [i].lookupFirst(null, GdbDebugger.class);
            if (d == null) {
		continue;
	    }
            d.waitRunning();
            return d;
        }
        throw new DebuggerStartException(new InternalError());
    }

    /**
     * Returns current state of Cnd debugger.
     *
     * @return current state of Cnd debugger
     * @see #STATE_STARTING
     * @see #STATE_RUNNING
     * @see #STATE_STOPPED
     * @see #STATE_DISCONNECTED
     */
    public abstract String getState();
    
    /**
     * Gets value of suspend property.
     *
     * @return value of suspend property
     */
    public abstract int getSuspend();

    /**
     * Sets value of suspend property.
     *
     * @param s a new value of suspend property
     */
    public abstract void setSuspend(int s);
    
    
    /**
     *  Get the directory we run in.
     */
    public abstract String getRunDirectory();
    
    /**
     * Returns current thread or null.
     *
     * @return current thread or null
     */
    //public abstract CndThread getCurrentThread();
    
    /**
     * Returns current stack frame or null.
     *
     * @return current stack frame or null
     */
    //public abstract CallStackFrame getCurrentCallStackFrame();
    
    /**
     * Helper method that fires JPDABreakpointEvent on JPDABreakpoints.
     *
     * @param breakpoint a breakpoint to be changed
     * @param event a event to be fired
     */
    protected void fireBreakpointEvent(GdbBreakpoint breakpoint, GdbBreakpointEvent event) {
        breakpoint.fireGdbBreakpointChange(event);
    }
    
    /**
     * Evaluates given expression in the current context.
     *
     * @param expression a expression to be evaluated
     *  
     * @return current value of given expression
     */
    public abstract Variable evaluate(String expression) throws InvalidExpressionException;

    /**
     * Returns variable type as String.
     *
     * @param expression A variable name or an expression
     * @return variable type
     */
    public abstract String getVariableType(String expression);

    /**
     * Returns variable value as String.
     *
     * @param expression A variable name or an expression
     * @return variable value
     */
    public abstract String getVariableValue(String expression);

    /**
     * Returns variable's number of children as String.
     *
     * @param expression A variable name or an expression
     * @return number of children
     */
    public abstract String getVariableNumChild(String expression);
    
    /**
     * Waits till the Virtual Machine is started and returns 
     * {@link DebuggerStartException} if some problem occurres.
     *
     * @throws DebuggerStartException is some problems occurres during debugger 
     *         start
     *
     * @see AbstractDICookie#getVirtualMachine()
     */
    public abstract void waitRunning() throws DebuggerStartException;
    
    /**
     * Returns call stack for this debugger.
     *
     * @return call stack
     */
    public abstract ArrayList getCallStack();
    
    /**
     * Returns call stack for this debugger.
     *
     * @param from Starting frame
     * @param to Ending frame
     * @return call stack
     */
    public abstract CallStackFrame[] getCallStackFrames(int from, int too);
    
    public abstract int getStackDepth();
    
    /**
     * Returns current stack frame or null.
     *
     * @return current stack frame or null
     */
    public abstract CallStackFrame getCurrentCallStackFrame();
    
    /**
     * Sets a stack frame as current.
     *
     * @param Frame to make current (or null)
     */
    public abstract void setCurrentCallStackFrame(CallStackFrame frame);

    /**
     * Returns <code>true</code> if this debugger supports fix & continue 
     * (HotSwap).
     *
     * @return <code>true</code> if this debugger supports fix & continue
     */
    public abstract boolean canFixClasses();

    /**
     * Returns <code>true</code> if this debugger supports Pop action.
     *
     * @return <code>true</code> if this debugger supports Pop action
     */
    public abstract boolean canPopFrames();
    
    /**
     * Determines if the target debuggee can be modified.
     *
     * @return <code>true</code> if the target debuggee can be modified or when
     *         this information is not available (on JDK 1.4).
     * @since 2.3
     */
    public boolean canBeModified() {
        return true;
    }

    /**
     * Implements fix & continue (HotSwap). Map should contain class names
     * as a keys, and byte[] arrays as a values.
     *
     * @param classes a map from class names to be fixed to byte[] 
     */
    public abstract void fixClasses(Map classes);
    
    /** 
     * Returns instance of SmartSteppingFilter.
     *
     * @return instance of SmartSteppingFilter
     */
    //public abstract SmartSteppingFilter getSmartSteppingFilter();

    /**
     * Helper method that fires CndBreakpointEvent on CndBreakpoints.
     *
     * @param breakpoint a breakpoint to be changed
     * @param event a event to be fired
    protected void fireBreakpointEvent(CndBreakpoint breakpoint, CndBreakpointEvent event) {
        breakpoint.fireCndBreakpointChange(event);
    }
     */
    
    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
    public abstract void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
    public abstract void removePropertyChangeListener(PropertyChangeListener l);

    /**
     * Adds property change listener.
     *
     * @param propertyName a name of property to listen on
     * @param l new listener.
     */
    public abstract void addPropertyChangeListener(String propertyName, 
		PropertyChangeListener l);
    
    /**
     * Removes property change listener.
     *
     * @param propertyName a name of property to listen on
     * @param l removed listener.
     */
    public abstract void removePropertyChangeListener(String propertyName, 
		PropertyChangeListener l);
    
    /**
     * Creates a new {@link CndStep}. 
     * Parameters correspond to {@link CndStep} constructor.
     * 
     * @return {@link CndStep} 
     * @throws {@link java.lang.UnsupportedOperationException} If not overridden
    public CndStep createCndStep(int size, int depth) {
        throw new UnsupportedOperationException("This method must be overridden."); 
    }
     */
}
