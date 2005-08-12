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

package org.netbeans.api.debugger.jpda;

import com.sun.jdi.AbsentInformationException;
import java.beans.PropertyChangeListener;
import java.util.List;


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
    public abstract List getAvailableStrata ();

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
 