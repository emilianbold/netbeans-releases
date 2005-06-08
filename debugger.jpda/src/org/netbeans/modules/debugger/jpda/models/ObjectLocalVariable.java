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

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;


/**
 * @author   Jan Jancura
 */
public class ObjectLocalVariable extends Local implements 
org.netbeans.api.debugger.jpda.ObjectVariable {
    
    
    private CallStackFrameImpl  frame;
    
    ObjectLocalVariable (
        LocalsTreeModel model, 
        Value value, 
        String className, 
        LocalVariable local, 
        String genericSignature,
        CallStackFrameImpl frame
    ) {
        super (model, value, className, local, genericSignature);
        this.frame = frame;
    }
    
    
    // LocalVariable impl.......................................................
    
    protected void setValue (Value value) throws InvalidExpressionException {
        try {
            frame.getStackFrame ().setValue (local, value);
        } catch (InvalidTypeException ex) {
            throw new InvalidExpressionException (ex);
        } catch (ClassNotLoadedException ex) {
            throw new InvalidExpressionException (ex);
        }
    }
    
    void setFrame(CallStackFrameImpl frame) {
        this.frame = frame;
    }
    
    
    // other methods ...........................................................
    
    public String toString () {
        return "ObjectLocalVariable " + local.name ();
    }
}
