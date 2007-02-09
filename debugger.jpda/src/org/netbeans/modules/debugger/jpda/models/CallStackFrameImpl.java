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
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.NativeMethodException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.This;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;


/**
* Class representating one line of callstack.
*/
public class CallStackFrameImpl implements CallStackFrame {
    
    private StackFrame          sf;
    private int                 depth;
    private JPDADebuggerImpl    debugger;
    //private AST                 ast;
    private Operation           currentOperation;
    private boolean             valid;
    
    public CallStackFrameImpl (
        StackFrame          sf,
        int                 depth,
        JPDADebuggerImpl    debugger
    ) {
        this.sf = sf;
        this.depth = depth;
        this.debugger = debugger;
        this.valid = true; // suppose we're valid when we're new
    }

    // public interface ........................................................
        
    /**
    * Returns line number of this frame in this callstack.
    *
    * @return Returns line number of this frame in this callstack.
    */
    public synchronized int getLineNumber (String struts) {
        if (!valid) return 0;
        try {
            return getStackFrame().location ().lineNumber (struts);
        } catch (InvalidStackFrameException isfex) {
            // this stack frame is not available or information in it is not available
            valid = false;
            return 0;
        } catch (VMDisconnectedException ex) {
            return 0;
        }
    }
    
    public synchronized Operation getCurrentOperation(String struts) {
        return currentOperation;
    }
    
    public synchronized void setCurrentOperation(Operation operation) {
        this.currentOperation = operation;
    }

    /**
    * Returns method name of this frame in this callstack.
    *
    * @return Returns method name of this frame in this callstack.
    */
    public synchronized String getMethodName () {
        if (!valid) return "";
        try {
            return getStackFrame().location ().method ().name ();
        } catch (InvalidStackFrameException ex) {
            // this stack frame is not available or information in it is not available
            valid = false;
            return "";
        } catch (VMDisconnectedException ex) {
            return "";
        }
    }

    /**
    * Returns class name of this frame in this callstack.
    *
    * @return class name of this frame in this callstack
    */
    public synchronized String getClassName () {
        if (!valid) return "";
        try {
            return getStackFrame().location ().declaringType ().name ();
        } catch (InvalidStackFrameException ex) {
            // this stack frame is not available or information in it is not available
            valid = false;
            return "";
        } catch (VMDisconnectedException ex) {
            return "";
        }
    }

    /**
    * Returns name of default stratumn.
    *
    * @return name of default stratumn
    */
    public synchronized String getDefaultStratum () {
        if (!valid) return "";
        try {
            return getStackFrame().location ().declaringType ().defaultStratum ();
        } catch (InvalidStackFrameException ex) {
            // this stack frame is not available or information in it is not available
            valid = false;
            return "";
        } catch (VMDisconnectedException ex) {
            return "";
        }
    }

    /**
    * Returns name of default stratumn.
    *
    * @return name of default stratumn
    */
    public synchronized List<String> getAvailableStrata () {
        if (!valid) return Collections.emptyList();
        try {
            return getStackFrame().location ().declaringType ().availableStrata ();
        } catch (InvalidStackFrameException ex) {
            // this stack frame is not available or information in it is not available
            valid = false;
            return Collections.emptyList();
        } catch (VMDisconnectedException ex) {
            return Collections.emptyList();
        }
    }

    /**
    * Returns name of file of this frame.
    *
    * @return name of file of this frame
    * @throws NoInformationException if informations about source are not included or some other error
    *   occurres.
    */
    public synchronized String getSourceName (String stratum) throws AbsentInformationException {
        if (!valid) return "";
        try {
            return getStackFrame().location ().sourceName (stratum);
        } catch (InvalidStackFrameException ex) {
            // this stack frame is not available or information in it is not available
            valid = false;
            return "";
        } catch (VMDisconnectedException ex) {
            return "";
        }
    }
    
    /**
     * Returns source path of file this frame is stopped in or null.
     *
     * @return source path of file this frame is stopped in or null
     */
    public synchronized String getSourcePath (String stratum) throws AbsentInformationException {
        if (!valid) return "";
        try {
            return getStackFrame().location ().sourcePath (stratum);
        } catch (InvalidStackFrameException ex) {
            // this stack frame is not available or information in it is not available
            valid = false;
            return "";
        } catch (VMDisconnectedException ex) {
            return "";
        }
    }
    
    /**
     * Returns local variables.
     *
     * @return local variables
     */
    public org.netbeans.api.debugger.jpda.LocalVariable[] getLocalVariables () 
    throws AbsentInformationException {
        try {
            String className = getStackFrame ().location ().declaringType ().name ();
            List l = getStackFrame ().visibleVariables ();
            int n = l.size();
            LocalVariable[] locals = new LocalVariable [n];
            for (int i = 0; i < n; i++) {
                com.sun.jdi.LocalVariable lv = (com.sun.jdi.LocalVariable) l.get (i);
                Value v = getStackFrame ().getValue (lv);
                Local local = (Local) debugger.getLocalVariable(lv, v);
                local.setFrame(this);
                local.setInnerValue(v);
                local.setClassName(className);
                locals[i] = local;
            }
            return locals;
        } catch (NativeMethodException ex) {
            throw new AbsentInformationException ("native method");
        } catch (InvalidStackFrameException ex) {
            throw new AbsentInformationException ("thread is running");
        } catch (VMDisconnectedException ex) {
            return new LocalVariable [0];
        }
    }
    
    /**
     * Returns object reference this frame is associated with or null (
     * frame is in static method).
     *
     * @return object reference this frame is associated with or null
     */
    public synchronized This getThisVariable () {
        if (!valid) return null;
        ObjectReference thisR;
        try {
            thisR = getStackFrame().thisObject ();
        } catch (InvalidStackFrameException ex) {
            valid = false;
            return null;
        }
        if (thisR == null) return null;
        return new ThisVariable (debugger, thisR, "");
    }
    
    /**
     * Sets this frame current.
     *
     * @see org.netbeans.api.debugger.jpda.JPDADebugger#getCurrentCallStackFrame
     */
    public void makeCurrent () {
        debugger.setCurrentCallStackFrame (this);
    }
    
    /**
     * Returns <code>true</code> if the method in this frame is obsoleted.
     *
     * @return <code>true</code> if the method in this frame is obsoleted
     * @throws InvalidStackFrameException when this stack frame becomes invalid
     */
    public synchronized boolean isObsolete () {
        return getStackFrame ().location ().method ().isObsolete ();
    }
    
    public boolean canPop() {
        if (!debugger.canPopFrames()) return false;
        ThreadReference t = getStackFrame().thread();
        try {
            if (t.frameCount() <= 1) { // Nowhere to pop
                return false;
            }
            List topFrames = t.frames(0, 2);
            if (((StackFrame) topFrames.get(0)).location().method().isNative() ||
                ((StackFrame) topFrames.get(1)).location().method().isNative()) {
                // Have native methods on the stack - can not pop
                return false;
            }
        } catch (IncompatibleThreadStateException itsex) {
            return false;
        }
        // Looks like we should be able to pop...
        return true;
    }
    
    /**
     * Pop stack frames. All frames up to and including the frame 
     * are popped off the stack. The frame previous to the parameter 
     * frame will become the current frame.
     *
     * @throws InvalidStackFrameException when this stack frame becomes invalid
     */
    public void popFrame () {
        debugger.popFrames (sf.thread(), getStackFrame ());
    }
    
    /**
     * Returns thread.
     *
     * @return thread
     * @throws InvalidStackFrameException when this stack frame becomes invalid
     */
    public JPDAThread getThread () {
        return debugger.getThread (sf.thread());
    }

    
    // other methods............................................................

    /**
     * Get the JDI stack frame.
     * @throws IllegalStateException when the associated thread is not suspended.
     */
    public StackFrame getStackFrame () {
        return sf;
    }
    
    /**
     * Get the depth of this stack frame in the thread stack.
     */
    public int getFrameDepth() {
        return depth;
    }

    public boolean equals (Object o) {
        try {
            return  (o instanceof CallStackFrameImpl) &&
                    (sf.equals (((CallStackFrameImpl) o).sf));
        } catch (InvalidStackFrameException isfex) {
            return sf == ((CallStackFrameImpl) o).sf;
        }
    }
    
    private Integer hashCode;
    
    public synchronized int hashCode () {
        if (hashCode == null) {
            try {
                hashCode = new Integer(sf.hashCode());
            } catch (InvalidStackFrameException isfex) {
                valid = false;
                hashCode = new Integer(super.hashCode());
            }
        }
        return hashCode.intValue();
    }
}

