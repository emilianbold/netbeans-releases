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

package org.netbeans.core.execution;

/** Informs about process state
*
* @author Ales Novak
* @version 0.10 Mar 04, 1998
*/
public class ExecutionEvent extends java.util.EventObject {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -9181112840849353114L;
    /** the process that the event notifies about*/
    private DefaultSysProcess proc;
    /** true if process is important for end user, false otherwise (compilation) */
    private boolean isUserImportant;
    
    /**
    * @param source is a source of the event
    * @param proc is a Process that this event notifies about
    */
    public ExecutionEvent(Object source, DefaultSysProcess proc, boolean isUserImportant) {
        super(source);
        this.proc = proc;
        this.isUserImportant = isUserImportant;
    }

    /**
    * @return Process from the event
    */
    public DefaultSysProcess getProcess() {
        return proc;
    }
    
    /** @return true if executed process is important for user and should
     * trigger automatic open/close of execution view */
    public boolean isUserImportant() {
        return isUserImportant;
    }
    
}
