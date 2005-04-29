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

import com.sun.jdi.*;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.This;


/**
* Class representating one line of callstack.
*/
public class CallStackFrameImpl implements CallStackFrame {
    
    private ThreadReference     thread;
    private int                 index;
    private CallStackTreeModel  ctm;
    private String              id;
    
    
    public CallStackFrameImpl (
        ThreadReference     thread,
        StackFrame          sf,
        CallStackTreeModel  ctm,
        String              id,
        int                 index
    ) {
        this.thread = thread;
        this.index = index;
        this.ctm = ctm;
        this.id = id;
    }

    // public interface ........................................................
        
    /**
    * Returns line number of this frame in this callstack.
    *
    * @return Returns line number of this frame in this callstack.
    */
    public int getLineNumber (String struts) {
        try {
            return getStackFrame().location ().lineNumber (struts);
        } catch (Exception ex) {
            // this stack frame is not available or information in it is not available
        }
        return 0;
    }

    /**
    * Returns method name of this frame in this callstack.
    *
    * @return Returns method name of this frame in this callstack.
    */
    public String getMethodName () {
        try {
            return getStackFrame().location ().method ().name ();
        } catch (Exception ex) {
            // this stack frame is not available or information in it is not available
        }
        return "";
    }

    /**
    * Returns class name of this frame in this callstack.
    *
    * @return class name of this frame in this callstack
    */
    public String getClassName () {
        try {
            return getStackFrame().location ().declaringType ().name ();
        } catch (Exception ex) {
            // this stack frame is not available or information in it is not available
        }
        return "";
    }

    /**
    * Returns name of default stratumn.
    *
    * @return name of default stratumn
    */
    public String getDefaultStratum () {
        try {
            return getStackFrame().location ().declaringType ().defaultStratum ();
        } catch (Exception ex) {
            // this stack frame is not available or information in it is not available
        }
        return "";
    }

    /**
    * Returns name of default stratumn.
    *
    * @return name of default stratumn
    */
    public List getAvailableStrata () {
        try {
            return getStackFrame().location ().declaringType ().availableStrata ();
        } catch (Exception ex) {
            // this stack frame is not available or information in it is not available
        }
        return new ArrayList ();
    }

    /**
    * Returns name of file of this frame.
    *
    * @return name of file of this frame
    * @throws NoInformationException if informations about source are not included or some other error
    *   occurres.
    */
    public String getSourceName (String stratum) throws AbsentInformationException {
        try {
            return getStackFrame().location ().sourceName (stratum);
        } catch (Exception ex) {
            // this stack frame is not available or information in it is not available
        }
        return "";
    }
    
    /**
     * Returns source path of file this frame is stopped in or null.
     *
     * @return source path of file this frame is stopped in or null
     */
    public String getSourcePath (String stratum) throws AbsentInformationException {
        try {
            return getStackFrame().location ().sourcePath (stratum);
        } catch (Exception ex) {
        // this stack frame is not available or information in it is not available
        }
        return "";
    }
    
    /**
     * Returns local variables.
     *
     * @return local variables
     */
    public org.netbeans.api.debugger.jpda.LocalVariable[] getLocalVariables () 
    throws AbsentInformationException {
        LocalsTreeModel ltm = ctm.getLocalsTreeModel ();
        int count = getStackFrame ().visibleVariables ().size ();
        AbstractVariable vs[] = ltm.getLocalVariables 
            (this, getStackFrame (), 0, count);
        org.netbeans.api.debugger.jpda.LocalVariable[] var = new
            org.netbeans.api.debugger.jpda.LocalVariable [vs.length];
        System.arraycopy (vs, 0, var, 0, vs.length);
        return var;
    }
    
    /**
     * Returns object reference this frame is associated with or null (
     * frame is in static method).
     *
     * @return object reference this frame is associated with or null
     */
    public This getThisVariable () {
        ObjectReference thisR = getStackFrame().thisObject ();
        if (thisR == null) return null;
        LocalsTreeModel ltm = ctm.getLocalsTreeModel ();
        return ltm.getThis (thisR, "");
    }
    
    /**
     * Sets this frame current.
     *
     * @see org.netbeans.api.debugger.jpda.JPDADebugger#getCurrentCallStackFrame
     */
    public void makeCurrent () {
        ctm.getDebugger ().setCurrentCallStackFrame (this);
    }
    
    /**
     * Returns <code>true</code> if this frame is obsoleted.
     *
     * @return <code>true</code> if this frame is obsoleted
     */
    public boolean isObsolete () {
        return getStackFrame ().location ().method ().isObsolete ();
    }
    
    /**
     * Pop stack frames. All frames up to and including the frame 
     * are popped off the stack. The frame previous to the parameter 
     * frame will become the current frame.
     */
    public void popFrame () {
        ctm.getDebugger ().popFrames (thread, getStackFrame ());
    }
    
    /**
     * Returns thread.
     *
     * @return thread
     */
    public JPDAThread getThread () {
        return ctm.getDebugger ().getThread (thread);
    }

    
    // other methods............................................................

    /**
     * Get the JDI stack frame.
     * @throws IllegalStateException when the associated thread is not suspended.
     */
    public StackFrame getStackFrame () {
        try {
            return thread.frame (index);
        } catch (IncompatibleThreadStateException e) {
            // There is a lot of calls like "getStackFrame().<something>
            // therefore it's better not to return null.
            // The caller should know that this can not be called while the
            // thread is running
            IllegalStateException isex = new IllegalStateException(e.getLocalizedMessage());
            isex.initCause(e);
            throw isex;
        }
    }

    public boolean equals (Object o) {
        return  (o instanceof CallStackFrameImpl) &&
                (id.equals (((CallStackFrameImpl) o).id));
    }
    
    public int hashCode () {
        return id.hashCode ();
    }
}

