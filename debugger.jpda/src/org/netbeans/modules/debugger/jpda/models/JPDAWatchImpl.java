/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

class JPDAWatchImpl extends AbstractVariable implements JPDAWatch {

    private JPDADebuggerImpl    debugger;
    private Watch               watch;
    private String              exceptionDescription;
    private java.lang.ref.Reference nodeRef;
    
    
    JPDAWatchImpl (JPDADebuggerImpl debugger, Watch watch, Value v, Object node) {
        super (
            debugger,
            v, 
            "" + watch +
                (v instanceof ObjectReference ? "^" : "")
        );
        this.debugger = debugger;
        this.watch = watch;
        this.nodeRef = new java.lang.ref.WeakReference(node);
    }
    
    JPDAWatchImpl (
        JPDADebuggerImpl debugger, 
        Watch watch, 
        Exception exception,
        Object node
    ) {
        super (
            debugger, 
            null, 
            "" + watch
        );
        this.debugger = debugger;
        this.watch = watch;
        this.exceptionDescription = exception.getLocalizedMessage ();
        if (exceptionDescription == null)
            exceptionDescription = exception.getMessage ();
        this.nodeRef = new java.lang.ref.WeakReference(node);
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
    *
    public void setValue (String expression) throws InvalidExpressionException {
        // evaluate expression to Value
        Value value = model.getDebugger ().evaluateIn (expression);
        // set new value to remote veriable
        setValue (value);
        // set new value to this model
        setInnerValue (value);
        // refresh tree
        Object node = nodeRef.get();
        if (node != null) {
            model.fireTableValueChangedChanged (node, null);
        }
    }
     */
    
    protected void setInnerValue (Value v) {
        super.setInnerValue (v);
        exceptionDescription = null;
    }
    
    protected void setValue (Value value) throws InvalidExpressionException {
        CallStackFrameImpl frame = (CallStackFrameImpl) debugger.
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

