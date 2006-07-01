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
