/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger.jpda;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.ListeningConnector;
import com.sun.jdi.request.EventRequest;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.Variable;


/**
 * Represents one JPDA debugger session (one 
 * {@link com.sun.jdi.VirtualMachine}). 
 *
 * <br><br>
 * <b>How to obtain it from DebuggerEngine:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    JPDADebugger jpdaDebugger = (JPDADebugger) debuggerEngine.lookup 
 *        (JPDADebugger.class);</pre>
 *
 * @author Jan Jancura
 */
public abstract class JPDADebugger {

    /** Name of property for state of debugger. */
    public static final String          PROP_STATE = "state";
    /** Name of property for current thread. */
    public static final String          PROP_CURRENT_THREAD = "currentThread";
    /** Name of property for current stack frame. */
    public static final String          PROP_CURRENT_CALL_STACK_FRAME = "currentCallStackFrame";
    /** Property name constant. */
    public static final String          PROP_SUSPEND = "suspend"; // NOI18N

    /** Suspend property value constant. */
    public static final int             SUSPEND_ALL = EventRequest.SUSPEND_EVENT_THREAD;
    /** Suspend property value constant. */
    public static final int             SUSPEND_EVENT_THREAD = EventRequest.SUSPEND_EVENT_THREAD;
    
    /** Debugger state constant. */
    public static final int             STATE_STARTING = 1;
    /** Debugger state constant. */
    public static final int             STATE_RUNNING = 2;
    /** Debugger state constant. */
    public static final int             STATE_STOPPED = 3;
    /** Debugger state constant. */
    public static final int             STATE_DISCONNECTED = 4;

    /** ID of JPDA Debugger Engine. */
    public static final String          ENGINE_ID = "debuggerjpda.netbeans.org";
    

    
    /**
     * Utility method returns instance of JPDADebugger for given 
     * {@link org.netbeans.api.debugger.DebuggerEngine}.
     *
     * @return instance of JPDADebugger for given 
     * {@link org.netbeans.api.debugger.DebuggerEngine}
     */
    public static JPDADebugger getJPDADebugger (DebuggerEngine engine) {
        return (JPDADebugger) engine.lookupFirst 
            (JPDADebugger.class);
    }
    
    /**
     * This utility method helps to start a new JPDA debugger session. 
     * Its implementation use {@link LaunchingDICookie} and 
     * {@link org.netbeans.api.debugger.DebuggerManager#getDebuggerManager}.
     *
     * @param mainClassName a name or main class
     * @param args command line arguments
     * @param classPath a classPath
     * @param suspend if true session will be suspended
     */
    public static void launch (
        String          mainClassName,
        String[]        args,
        String          classPath,
        boolean         suspend
    ) {
        DebuggerManager.getDebuggerManager ().startDebugging (
            DebuggerInfo.create (
                LaunchingDICookie.ID,
                new Object[] {
                    LaunchingDICookie.create (
                        mainClassName,
                        args,
                        classPath,
                        suspend
                    )
                }
            )
        );
    }
    
    /**
     * This utility method helps to start a new JPDA debugger session. 
     * Its implementation use {@link AttachingDICookie} and 
     * {@link org.netbeans.api.debugger.DebuggerManager#getDebuggerManager}.
     *
     * @param name a name of shared memory block
     */
    public static void attach (
        String          name
    ) {
        DebuggerManager.getDebuggerManager ().startDebugging (
            DebuggerInfo.create (
                AttachingDICookie.ID,
                new Object[] {
                    AttachingDICookie.create (
                        name
                    )
                }
            )
        );
    }
    
    /**
     * This utility method helps to start a new JPDA debugger session. 
     * Its implementation use {@link ListeningDICookie} and 
     * {@link org.netbeans.api.debugger.DebuggerManager#getDebuggerManager}.
     *
     * @param portNumber a number of port to listen on
     */
    public static void listen (
        int          portNumber
    ) {
        DebuggerManager.getDebuggerManager ().startDebugging (
            DebuggerInfo.create (
                ListeningDICookie.ID,
                new Object[] {
                    ListeningDICookie.create (
                        portNumber
                    )
                }
            )
        );
    }
    
    /**
     * This utility method helps to start a new JPDA debugger session. 
     * Its implementation use {@link ListeningDICookie} and 
     * {@link org.netbeans.api.debugger.DebuggerManager#getDebuggerManager}.
     *
     * @param name a name of shared memory block
     */
    public static void listen (
        String          name
    ) {
        DebuggerManager.getDebuggerManager ().startDebugging (
            DebuggerInfo.create (
                ListeningDICookie.ID,
                new Object[] {
                    ListeningDICookie.create (
                        name
                    )
                }
            )
        );
    }
    
    /**
     * This utility method helps to start a new JPDA debugger session. 
     * Its implementation use {@link AttachingDICookie} and 
     * {@link org.netbeans.api.debugger.DebuggerManager#getDebuggerManager}.
     *
     * @param hostName a name of computer to attach to
     * @param portNumber a potr number
     */
    public static void attach (
        String          hostName,
        int             portNumber
    ) {
        DebuggerManager.getDebuggerManager ().startDebugging (
            DebuggerInfo.create (
                AttachingDICookie.ID,
                new Object[] {
                    AttachingDICookie.create (
                        hostName,
                        portNumber
                    )
                }
            )
        );
    }

    /**
     * Returns current state of JPDA debugger.
     *
     * @return current state of JPDA debugger
     * @see #STATE_STARTING
     * @see #STATE_RUNNING
     * @see #STATE_STOPPED
     * @see #STATE_DISCONNECTED
     */
    public abstract int getState ();
    
    /**
     * Gets value of suspend property.
     *
     * @return value of suspend property
     */
    public abstract int getSuspend ();

    /**
     * Sets value of suspend property.
     *
     * @param s a new value of suspend property
     */
    public abstract void setSuspend (int s);
    
    /**
     * Returns current thread or null.
     *
     * @return current thread or null
     */
    public abstract JPDAThread getCurrentThread ();
    
    /**
     * Returns current stack frame or null.
     *
     * @return current stack frame or null
     */
    public abstract CallStackFrame getCurrentCallStackFrame ();
    
    /**
     * {@link org.netbeans.api.debugger.DebuggerEngine}
     *
     * @return DebuggerEngine
     */
//    public abstract DebuggerEngine getDebuggerEngine ();
    
    /**
     * Evaluates given expression in the current context.
     *
     * @param expression a expression to be evaluated
     *  
     * @return current value of given expression
     */
    public abstract Variable evaluate (String expression) 
    throws InvalidExpressionException;

    /**
     * Returns excerption if initialization of VirtualMachine has failed.
     *
     * @returns excerption if initialization of VirtualMachine has failed
     * @see AbstractDICookie#getVirtualMachine()
     */
    public abstract Exception getException ();
    
    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
    public abstract void addPropertyChangeListener (PropertyChangeListener l);

    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
    public abstract void removePropertyChangeListener (
        PropertyChangeListener l
    );

    /**
     * Adds property change listener.
     *
     * @param propertyName a name of property to listen on
     * @param l new listener.
     */
    public abstract void addPropertyChangeListener (
        String propertyName, 
        PropertyChangeListener l
    );
    
    /**
     * Removes property change listener.
     *
     * @param propertyName a name of property to listen on
     * @param l removed listener.
     */
    public abstract void removePropertyChangeListener (
        String propertyName, 
        PropertyChangeListener l
    );
}
