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

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;


/**
 * @author   Jan Jancura
 */
public class Local extends AbstractVariable implements 
org.netbeans.api.debugger.jpda.LocalVariable {
        
    protected LocalVariable     local;
    private CallStackFrameImpl  frame;
    private String              className;

    
    Local (
        LocalsTreeModel model,
        Value value, 
        String className,
        LocalVariable local,
        CallStackFrameImpl frame
    ) {
        super (
            model, 
            value, 
            local.name () + local.hashCode() +
                (value instanceof ObjectReference ? "^" : "")
        );
        this.local = local;
        this.frame = frame;
        this.className = className;
    }

    Local (
        LocalsTreeModel model, 
        Value value, 
        String className, 
        LocalVariable local, 
        String genericSignature
    ) {
        super (
            model, 
            value, 
            genericSignature, 
            local.name () + local.hashCode() +
                (value instanceof ObjectReference ? "^" : "")
        );
        this.local = local;
        this.className = className;
    }

    // LocalVariable impl.......................................................
    

    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getName () {
        return local.name ();
    }

    /**
     * Returns name of enclosing class.
     *
     * @return name of enclosing class
     */
    public String getClassName () {
        return className;
    }

    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getDeclaredType () {
        return local.typeName ();
    }
    
    protected void setValue (Value value) throws InvalidExpressionException {
        try {
            frame.getStackFrame ().setValue (local, value);
        } catch (InvalidTypeException ex) {
            throw new InvalidExpressionException (ex);
        } catch (ClassNotLoadedException ex) {
            throw new InvalidExpressionException (ex);
        }
    }
    
    // other methods ...........................................................
    
    void setFrame(CallStackFrameImpl frame) {
        this.frame = frame;
    }
    
    void setLocalVariable(LocalVariable local) {
        this.local = local;
    }
    
    public String toString () {
        return "LocalVariable " + local.name ();
    }
}
