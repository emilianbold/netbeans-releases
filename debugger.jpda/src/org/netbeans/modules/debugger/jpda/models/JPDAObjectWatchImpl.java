/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.api.debugger.jpda.ObjectVariable;


/**
 * Represents watch in JPDA debugger.
 *
 * @author   Jan Jancura
 */

public class JPDAObjectWatchImpl extends AbstractVariable implements JPDAWatch,
ObjectVariable {

    private WatchesModel        model;
    private Watch               watch;
    private String              exceptionDescription;
    
    
    JPDAObjectWatchImpl (WatchesModel model, Watch watch, ObjectReference v) {
        super (
            model.getLocalsTreeModel (), 
            v, 
            "" + watch +
                (v instanceof ObjectReference ? "^" : "")
        );
        this.model = model;
        this.watch = watch;
    }
    
    JPDAObjectWatchImpl (WatchesModel model, Watch watch, String exceptionDescription) {
        super (
            model.getLocalsTreeModel (), 
            null, 
            "" + watch
        );
        this.model = model;
        this.watch = watch;
        this.exceptionDescription = exceptionDescription;
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
    
    protected void setValue (final Value value) 
    throws InvalidExpressionException {
        
        // 1) get frame
        CallStackFrameImpl frame = (CallStackFrameImpl) model.getDebugger ().
            getCurrentCallStackFrame ();
        if (frame == null)
            throw new InvalidExpressionException ("No curent frame.");
        
        // 2) try to set as a local variable value
        try {
            LocalVariable local = frame.getStackFrame ().visibleVariableByName 
                (getExpression ());
            if (local != null)
                try {
                    frame.getStackFrame ().setValue (local, value);
                    return;
                } catch (InvalidTypeException ex) {
                    throw new InvalidExpressionException (ex);
                } catch (ClassNotLoadedException ex) {
                    throw new InvalidExpressionException (ex);
                }
        } catch (AbsentInformationException ex) {
            // no local variable visible in this case
        }
        
        // 3) try tu set as a field
        ObjectReference thisObject = frame.getStackFrame ().thisObject ();
        if (thisObject == null)
            throw new InvalidExpressionException 
                ("Can not set value to expression.");
        Field field = thisObject.referenceType ().fieldByName 
            (getExpression ());
        if (field == null)
            throw new InvalidExpressionException 
                ("Can not set value to expression.");
        try {
            thisObject.setValue (field, value);
        } catch (InvalidTypeException ex) {
            throw new InvalidExpressionException (ex);
        } catch (ClassNotLoadedException ex) {
            throw new InvalidExpressionException (ex);
        }
    }
    
    protected void setInnerValue (Value v) {
        super.setInnerValue (v);
        exceptionDescription = null;
    }
    
    void setException (String exceptionDescription) {
        super.setInnerValue (null);
        this.exceptionDescription = exceptionDescription;
    }
    
    boolean isPrimitive () {
        return !(getInnerValue () instanceof ObjectReference);
    }
}

