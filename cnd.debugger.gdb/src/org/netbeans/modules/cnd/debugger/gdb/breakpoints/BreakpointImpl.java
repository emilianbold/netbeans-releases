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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import java.util.ArrayList;
import java.util.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.netbeans.api.debugger.Breakpoint;

import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.InvalidExpressionException;
import org.netbeans.modules.cnd.debugger.gdb.event.GdbBreakpointEvent;

import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.expr.Expression;
import org.netbeans.modules.cnd.debugger.gdb.expr.ParseException;


/**
 *
 * @author   Jan Jancura and Gordon Prieur
 */
public abstract class BreakpointImpl implements PropertyChangeListener {
    
    private static boolean verbose = System.getProperty("netbeans.debugger.breakpoints") != null;

    private GdbDebugger     debugger;
    private GdbBreakpoint       breakpoint;
    private BreakpointsReader   reader;
    private final Session       session;
    private List                requests = new ArrayList();
    private Expression          compiledCondition;


    protected BreakpointImpl(GdbBreakpoint breakpoint, BreakpointsReader reader,
                                GdbDebugger debugger, Session session) {
        this.debugger = debugger;
        this.reader = reader;
        this.breakpoint = breakpoint;
        this.session = session;
    }

    /**
     * Called from XXXBreakpointImpl constructor only.
     */
    final void set() {
	breakpoint.setDebugger(debugger);
        breakpoint.addPropertyChangeListener(this);
        update();
    }
    
    protected abstract void setRequests();
    
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
        if (getDebugger().getState() != GdbDebugger.STATE_NONE) {
            setRequests();
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (Breakpoint.PROP_DISPOSED.equals(evt.getPropertyName())) {
            remove();
	}
	update();
    }

    //protected abstract void setRequests();
    
    protected final void remove() {
        breakpoint.removePropertyChangeListener(this);
	breakpoint.setState(GdbBreakpoint.DELETION_PENDING);
    }

    protected GdbBreakpoint getBreakpoint() {
        return breakpoint;
    }

    protected GdbDebugger getDebugger() {
        return debugger;
    }

    public boolean perform(String condition) {
        boolean resume = false;
        
        if (condition == null || condition.equals("")) { // NOI18N
            GdbBreakpointEvent e = new GdbBreakpointEvent(getBreakpoint(), debugger,
                            GdbBreakpointEvent.CONDITION_NONE, null);
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

//    private boolean evaluateCondition(String condition, Value value) {
//
//        try {
//            try {
//                boolean result;
//                GdbBreakpointEvent ev;
//                synchronized (debugger.LOCK) {
//                    StackFrame sf = thread.frame (0);
//                    result = evaluateConditionIn (condition, sf);
//                    ev = new GdbBreakpointEvent (
//                        getBreakpoint (),
//                        debugger,
//                        result ? 
//                            GdbBreakpointEvent.CONDITION_TRUE : 
//                            GdbBreakpointEvent.CONDITION_FALSE,
//                        debugger.getThread (thread), 
//                        referenceType, 
//                        debugger.getVariable (value)
//                    );
//                }
//                getDebugger().fireBreakpointEvent(getBreakpoint(), ev);
//                            
//                // condition true => stop here (do not resume)
//                // condition false => resume
//                if (verbose)
//                    System.out.println ("B perform breakpoint (condition = " + result + "): " + this + " resume: " + (!result || ev.getResume ()));
//                return !result || ev.getResume ();
//            } catch (ParseException ex) {
//                GdbBreakpointEvent ev = new GdbBreakpointEvent (
//                    getBreakpoint (),
//                    debugger,
//                    ex,
//                    debugger.getThread (thread), 
//                    referenceType, 
//                    debugger.getVariable (value)
//                );
//                getDebugger().fireBreakpointEvent(getBreakpoint(), ev);
//                return ev.getResume ();
//            } catch (InvalidExpressionException ex) {
//                GdbBreakpointEvent ev = new GdbBreakpointEvent (
//                    getBreakpoint (),
//                    debugger,
//                    ex,
//                    debugger.getThread (thread), 
//                    referenceType, 
//                    debugger.getVariable (value)
//                );
//                getDebugger ().fireBreakpointEvent (
//                    getBreakpoint (),
//                    ev
//                );
//                return ev.getResume ();
//            }
//        } catch (IncompatibleThreadStateException ex) {
//             should not occurre
//            ex.printStackTrace ();
//        }
//        // some error occured during evaluation of expression => do not resume
//            
//
//        return false; // do not resume
//    }
    
    /**
     * Evaluates given condition. Returns value of condition evaluation. 
     * Returns true othervise (bad expression).
     */
    /*
    private boolean evaluateConditionIn(String condExpr, Object frame) 
                        throws ParseException, InvalidExpressionException {
        // 1) compile expression
        if (compiledCondition == null || !compiledCondition.getExpression().equals(condExpr)) {
            compiledCondition = Expression.parse(condExpr, Expression.LANGUAGE_CPLUSPLUS);
        }
        
        // 2) evaluate expression
        // already synchronized (debugger.LOCK)
        Boolean value = getDebugger().evaluateIn(compiledCondition, frame);
        try {
            return value.booleanValue();
        } catch (ClassCastException e) {
            throw new InvalidExpressionException(e);
        }
    }
     */
    
    /**
     * Support method for simple patterns.
     */
    static boolean match(String name, String pattern) {
        String star = "*"; // NOI18N
        if (pattern.startsWith(star))
            return name.endsWith(pattern.substring(1));
        else
        if (pattern.endsWith(star)) {
            return name.startsWith(pattern.substring(0, pattern.length() - 1));
        }
        return name.equals(pattern);
    }
}
