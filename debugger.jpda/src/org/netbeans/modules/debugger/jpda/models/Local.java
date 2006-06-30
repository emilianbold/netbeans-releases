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
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;


/**
 * @author   Jan Jancura
 */
class Local extends AbstractVariable implements 
org.netbeans.api.debugger.jpda.LocalVariable {
        
    protected LocalVariable     local;
    private JPDAThread          thread;
    private int                 depth;
    private String              className;

    
    Local (
        JPDADebuggerImpl debugger,
        Value value, 
        String className,
        LocalVariable local,
        CallStackFrameImpl frame
    ) {
        super (
            debugger, 
            value, 
            local.name () + local.hashCode() +
                (value instanceof ObjectReference ? "^" : "")
        );
        this.local = local;
        if (frame != null) {
            this.thread = frame.getThread();
            this.depth = frame.getFrameDepth();
        }
        this.className = className;
    }

    Local (
        JPDADebuggerImpl debugger, 
        Value value, 
        String className, 
        LocalVariable local, 
        String genericSignature,
        CallStackFrameImpl frame
    ) {
        super (
            debugger, 
            value, 
            genericSignature, 
            local.name () + local.hashCode() +
                (value instanceof ObjectReference ? "^" : "")
        );
        this.local = local;
        if (frame != null) {
            this.thread = frame.getThread();
            this.depth = frame.getFrameDepth();
        }
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
    
    protected final void setClassName(String className) {
        this.className = className;
    }

    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getDeclaredType () {
        return local.typeName ();
    }
    
    protected final void setValue (Value value) throws InvalidExpressionException {
        try {
            StackFrame sf = ((CallStackFrameImpl) thread.getCallStack(depth, depth + 1)[0]).getStackFrame();
            sf.setValue (local, value);
        } catch (AbsentInformationException aiex) {
            throw new InvalidExpressionException(aiex);
        } catch (InvalidTypeException ex) {
            throw new InvalidExpressionException (ex);
        } catch (ClassNotLoadedException ex) {
            throw new InvalidExpressionException (ex);
        }
    }
    
    // other methods ...........................................................
    
    final void setFrame(CallStackFrameImpl frame) {
        this.thread = frame.getThread();
        this.depth = frame.getFrameDepth();
    }
    
    public String toString () {
        return "LocalVariable " + local.name ();
    }
}
