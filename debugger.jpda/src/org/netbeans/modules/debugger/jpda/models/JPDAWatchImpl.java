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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;


/**
 * Represents watch in JPDA debugger.
 *
 * @author   Jan Jancura
 */

public class JPDAWatchImpl extends AbstractVariable implements JPDAWatch {

    private WatchesModel        model;
    private Watch               watch;
    private String              exceptionDescription;
    
    
    JPDAWatchImpl (WatchesModel model, Watch watch, Value v) {
        super (
            model.getLocalsTreeModel (), 
            v, 
            "" + watch +
                (v instanceof ObjectReference ? "^" : "")
        );
        this.model = model;
        this.watch = watch;
    }
    
    JPDAWatchImpl (
        WatchesModel model, 
        Watch watch, 
        Exception exception
    ) {
        super (
            model.getLocalsTreeModel (), 
            null, 
            "" + watch
        );
        this.model = model;
        this.watch = watch;
        this.exceptionDescription = exception.getLocalizedMessage ();
        if (exceptionDescription == null)
            exceptionDescription = exception.getMessage ();
    }
    
    /**
     * Watched expression.
     *
     * @return watched expression
     */
    public String getExpression () {
        return watch.getExpression ();
    }

    /**
     * Sets watched expression.
     *
     * @param expression a expression to be watched
     */
    public void setExpression (String expression) {
        watch.setExpression (expression);
    }
    
    /**
     * Remove the watch from the list of all watches in the system.
     */
    public void remove () {
        watch.remove ();
    }
    
    /**
     * Returns description of problem is this watch can not be evaluated
     * in current context.
     *
     * @return description of problem
     */
    public String getExceptionDescription () {
        return exceptionDescription;
    }

    /**
    * Sets string representation of value of this variable.
    *
    * @param value string representation of value of this variable.
    */
    public void setValue (String expression) throws InvalidExpressionException {
        // evaluate expression to Value
        Value value = model.getDebugger ().evaluateIn (expression);
        // set new value to remote veriable
        setValue (value);
        // set new value to this model
        setInnerValue (value);
        // refresh tree
        model.fireTableValueChangedChanged (this, null);
    }
    
    protected void setInnerValue (Value v) {
        super.setInnerValue (v);
        exceptionDescription = null;
    }
    
    protected void setValue (Value value) throws InvalidExpressionException {
        CallStackFrameImpl frame = (CallStackFrameImpl) model.getDebugger ().
            getCurrentCallStackFrame ();
        if (frame == null)
            throw new InvalidExpressionException ("No curent frame.");
        LocalVariable local = null;
        try {
            local = frame.getStackFrame ().visibleVariableByName 
                (getExpression ());
        } catch (AbsentInformationException ex) {
            throw new InvalidExpressionException ("Can not set value to expression.");
        }
        if (local == null)
            throw new InvalidExpressionException ("Can not set value to expression.");
        try {
            frame.getStackFrame ().setValue (local, value);
        } catch (InvalidTypeException ex) {
            throw new InvalidExpressionException (ex);
        } catch (ClassNotLoadedException ex) {
            throw new InvalidExpressionException (ex);
        }
    }
    
    void setException (String exceptionDescription) {
        setInnerValue (null);
        this.exceptionDescription = exceptionDescription;
    }
    
    boolean isPrimitive () {
        return !(getInnerValue () instanceof ObjectReference);
    }
}

