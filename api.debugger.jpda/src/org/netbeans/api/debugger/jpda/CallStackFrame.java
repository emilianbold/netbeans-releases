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

package org.netbeans.api.debugger.jpda;

import java.beans.PropertyChangeListener;
import java.util.List;

import org.netbeans.spi.viewmodel.NoInformationException;


/**
 * Represents one stack frame.
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
    throws NoInformationException;
    
    /**
     * Returns source path of file this frame is stopped in or null.
     *
     * @return source path of file this frame is stopped in or null
     */
    public abstract String getSourcePath (String stratum) 
    throws NoInformationException;
    
    /**
     * Returns local variables.
     *
     * @return local variables
     */
    public abstract LocalVariable[] getLocalVariables () 
    throws NoInformationException;

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
}
 