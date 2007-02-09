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

package org.netbeans.api.debugger.jpda;

import com.sun.jdi.AbsentInformationException;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;


/**
 * Represents one stack frame.
 *
 * <pre style="background-color: rgb(255, 255, 102);">
 * Since JDI interfaces evolve from one version to another, it's strongly recommended
 * not to implement this interface in client code. New methods can be added to
 * this interface at any time to keep up with the JDI functionality.</pre>
 *
 * @author Jan Jancura
 */
public interface CallStackFrame {
    
    /**
     * Returns line number associated with this stack frame.
     *
     * @param struts a language name or null for default language
     * @return line number associated with this this stack frame
     */
    public abstract int getLineNumber (String struts);

    /**
     * Returns the current operation (if any) at the location of this call stack frame.
     *
     * @param struts a language name or null for default language
     * @return  The operation at the frame location if available and this frame does not
     *          represent a native method invocation; <CODE>null</CODE> otherwise
     */
    public abstract Operation getCurrentOperation(String struts);

    /**
     * Returns method name associated with this stack frame.
     *
     * @return method name associated with this stack frame
     */
    public abstract String getMethodName ();

    /**
     * Returns class name of this stack frame.
     *
     * @return class name of this stack frame
     */
    public abstract String getClassName ();

    /**
    * Returns name of default stratumn.
    *
    * @return name of default stratumn
    */
    public abstract String getDefaultStratum ();

    /**
    * Returns name of default stratumn.
    *
    * @return name of default stratumn
    */
    public abstract List<String> getAvailableStrata ();

    /**
     * Returns name of file this stack frame is stopped in.
     *
     * @param struts a language name or null for default language
     * @return name of file this stack frame is stopped in
     * @throws NoInformationException if information about source is not 
     *   included in class file
     */
    public abstract String getSourceName (String struts) 
    throws AbsentInformationException;
    
    /**
     * Returns source path of file this frame is stopped in or null.
     *
     * @return source path of file this frame is stopped in or null
     */
    public abstract String getSourcePath (String stratum) 
    throws AbsentInformationException;
    
    /**
     * Returns local variables.
     *
     * @return local variables
     */
    public abstract LocalVariable[] getLocalVariables () 
    throws AbsentInformationException;

    /**
     * Returns object reference this frame is associated with or null (
     * frame is in static method).
     *
     * @return object reference this frame is associated with or null
     */
    public abstract This getThisVariable ();
    
    /**
     * Sets this frame current.
     *
     * @see JPDADebugger#getCurrentCallStackFrame
     */
    public abstract void makeCurrent ();
    
    /**
     * Returns <code>true</code> if this frame is obsoleted.
     *
     * @return <code>true</code> if this frame is obsoleted
     */
    public abstract boolean isObsolete ();
    
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
    public abstract void popFrame ();
    
    /**
     * Returns thread.
     *
     * @return thread
     */
    public abstract JPDAThread getThread ();
}
 