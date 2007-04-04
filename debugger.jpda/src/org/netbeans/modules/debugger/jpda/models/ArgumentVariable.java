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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.Value;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

/**
 * @author   Martin Entlicher
 */
public class ArgumentVariable extends AbstractVariable implements org.netbeans.api.debugger.jpda.LocalVariable {
        
    //JPDAThread          thread;
    //int                 depth;
    String              name;
    String              className;
    String              genericSignature;
    
    public ArgumentVariable (
        JPDADebuggerImpl debugger,
        Value value,
        String name,
        String className
        //CallStackFrameImpl frame
    ) {
        super (
            debugger, 
            value, 
            name + className.hashCode() +
                (value instanceof ObjectReference ? "^" : "")
        );
        this.name = name;
        this.className = className;
    }

    // LocalVariable impl.......................................................
    

    /**
    * Returns string representation of type of this variable.
    *
    * @return string representation of type of this variable.
    */
    public String getName () {
        return name;
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
        return className;//local.typeName ();
    }
    
    public Value getInnerValue() {
        return super.getInnerValue();
    }
    
    /*
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
     */
    
    // other methods ...........................................................
    
    /*final void setFrame(CallStackFrameImpl frame) {
        this.thread = frame.getThread();
        this.depth = frame.getFrameDepth();
    }*/

    public ArgumentVariable clone() {
        ArgumentVariable clon;
        clon = new ArgumentVariable(getDebugger(), getJDIValue(), name, className);
        //clon.depth = this.depth;
        //clon.thread = this.thread;
        return clon;
    }
    
    public String toString () {
        return "ArgumentVariable " + name;
    }
}
