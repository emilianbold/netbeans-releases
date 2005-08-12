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

package org.netbeans.api.debugger.jpda.event;

import com.sun.jdi.ReferenceType;
import java.util.EventObject;
import org.netbeans.api.debugger.jpda.*;

/**
 * JPDABreakpoint event notification.
 *
 * @author   Jan Jancura
 */
public final class JPDABreakpointEvent extends EventObject {
    
    /** Condition result constant. */
    public static final int CONDITION_NONE = 0;
    /** Condition result constant. */
    public static final int CONDITION_TRUE = 1;
    /** Condition result constant. */
    public static final int CONDITION_FALSE = 2;
    /** Condition result constant. */
    public static final int CONDITION_FAILED = 3;
    
    
    private int             conditionResult = CONDITION_FAILED;
    private Throwable       conditionException = null;
    private JPDADebugger    debugger;
    private JPDAThread      thread;
    private ReferenceType   referenceType;
    private Variable        variable;
    private boolean         resume = false;
    

    /**
     * Creates a new instance of JPDABreakpointEvent. This method should be
     * called from debuggerjpda module only. Do not create a new instances
     * of this class!
     *
     * @param sourceBreakpoint  a breakpoint
     * @param debugger          a debugger this 
     * @param conditionResult   a result of condition
     * @param thread            a context thread
     * @param referenceType     a context class
     * @param variable          a context variable
     */
    public JPDABreakpointEvent (
        JPDABreakpoint  sourceBreakpoint, 
        JPDADebugger    debugger,
        int             conditionResult,
        JPDAThread      thread,
        ReferenceType   referenceType,
        Variable        variable
    ) {
        super (sourceBreakpoint);
        this.conditionResult = conditionResult;
        this.thread = thread;
        this.debugger = debugger;
        this.referenceType = referenceType;
        this.variable = variable;
    }
    
    /**
     * Creates a new instance of JPDABreakpointEvent.
     *
     * @param sourceBreakpoint a breakpoint
     * @param conditionException result of condition
     * @param thread            a context thread
     * @param debugger          a debugger this 
     * @param referenceType     a context class
     * @param variable          a context variable
     */
    public JPDABreakpointEvent (
        JPDABreakpoint sourceBreakpoint, 
        JPDADebugger    debugger,
        Throwable conditionException,
        JPDAThread      thread,
        ReferenceType   referenceType,
        Variable        variable
    ) {
        super (sourceBreakpoint);
        this.conditionResult = CONDITION_FAILED;
        this.conditionException = conditionException;
        this.thread = thread;
        this.debugger = debugger;
        this.referenceType = referenceType;
        this.variable = variable;
    }
    
    /**
     * Returns result of condition evaluation.
     *
     * @return result of condition evaluation
     */
    public int getConditionResult () {
        return conditionResult;
    }
    
    /**
     * Returns result of condition evaluation.
     *
     * @return result of condition evaluation
     */
    public Throwable getConditionException () {
        return conditionException;
    }
    
    /**
     * Returns context thread - thread stopped on breakpoint. This parameter 
     * is defined by class prepared breakpoint, exception breakpoint, 
     * field breakpoint, line breakpoint, method breakpoint and 
     * thread breakpoint.
     *
     * @return thread context
     */
    public JPDAThread getThread () {
        return thread;
    }
    
    /**
     * Returns context class. It means loaded class for class load breakpoint 
     * and exception class for exception breakpoint.
     *
     * @return context class
     */
    public ReferenceType getReferenceType () {
        return referenceType;
    }
    
    /**
     * Returns JPDADebugger instance this breakpoint has been reached in.
     *
     * @return JPDADebugger instance this breakpoint has been reached in
     */
    public JPDADebugger getDebugger () {
        return debugger;
    }
    
    /**
     * Returns context variable. It contains new value for field modification
     * breakpoint and instance of exception for exception breakpoint.
     *
     * @return context variable
     */
    public Variable getVariable () {
        return variable;
    }
    
    /**
     * Call this method to resume debugger after all events will be notified.
     * You should not call JPDADebugger.resume () during breakpoint event 
     * evaluation!
     */
    public void resume () {
        resume = true;
    }
    
    /**
     * Returns resume value.
     */
    public boolean getResume () {
        return resume;
    }
}
