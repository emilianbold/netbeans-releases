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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.debugger.gdb;

/**
 * Represents one stack frame.
 *
 * <pre style="background-color: rgb(255, 255, 102);">
 * Since JDI interfaces evolve from one version to another, it's strongly recommended
 * not to implement this interface in client code. New methods can be added to
 * this interface at any time to keep up with the JDI functionality.</pre>
 *
 * @author Gordon Prieur (copied from Jan Jancura's JPDA implementation)
 */
public interface CallStackFrame {
    
    public static final int OBSOLETE = 1;
    public static final int VALID = 2;
    
    /**
     *  Set frame values.
     *
     *  @param func Function name from gdb
     *  @param file File name (basename) from gdb
     *  @param fullname Absolute path from gdb
     *  @param lnum Line number (as a String) from gdb
     *  @param addr Address (as a String) from gdb
     */
    public void set(String func, String file, String fullname, String lnum, String addr);
    
    /**
     *  Set frame number.
     *
     *  @param frameNumber Frame number in Call Stack ("0" means top)
     */
    public void setFrameNumber(String frameNumber);
    
    /**
     *  Returns frame number in Call Stack.
     *
     *  @return Frame nunmber in Call Stack ("0" means top)
     */
    public String getFrameNumber();
    
    /**
     * Returns line number associated with this stack frame.
     *
     * @return line number associated with this this stack frame
     */
    public abstract int getLineNumber();
    
    /**
     * Set the linenumber after a step operation
     */
    public abstract void setLineNumber(int lineNumber);

    /**
     * Returns method name associated with this stack frame.
     *
     * @return method name associated with this stack frame
     */
    public abstract String getFunctionName();

    /**
     * Returns name of file this stack frame is stopped in.
     *
     * @return name of file this stack frame is stopped in
     */
    public abstract String getFileName();

    /**
     * Returns name of file this stack frame is stopped in.
     *
     * @return name of file this stack frame is stopped in
     */
    public abstract String getFullname();

    /**
     * @return address this stack frame is stopped in
     */
    public abstract String getAddr();
    
    /**
     * Returns local variables.
     *
     * @return local variables
     */
   // public abstract LocalVariable[] getLocalVariables();

    /**
     * Returns object reference this frame is associated with or null (
     * frame is in static method).
     *
     * @return object reference this frame is associated with or null
     */
    //public abstract This getThisVariable();
    
    /**
     * Sets this frame current.
     *
     * @see JPDADebugger#getCurrentCallStackFrame
     */
    public abstract void makeCurrent();
    
    /**
     *  Set the state of this frame
     */
    public abstract void setState(int state);
    
    /**
     * Test if this frame is obsoleted.
     *
     * @return <code>true</code> if this frame is obsoleted
     */
    public abstract boolean isObsolete();
    
    /** UNCOMMENT WHEN THIS METHOD IS NEEDED. IT'S ALREADY IMPLEMENTED IN THE IMPL. CLASS.
     * Determine, if this stack frame can be poped off the stack.
     *
     * @return <code>true</code> if this frame can be poped
     *
    public abstract boolean canPop();
     */
    
    /**
     * Pop stack frames. All frames up to and including the frame 
     * are popped off the stack. The frame previous to the parameter 
     * frame will become the current frame.
     */
    public abstract void popFrame();
    
    /**
     * Returns thread.
     *
     * @return thread
     */
    //public abstract JPDAThread getThread();
}
 
