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

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.status.DeploymentStatus;

//The tomcat team will split the tomcat module in 2, so that this type of behaviour can be shared
// between web/app server plugins. This is really a shared utility class.
//

/** Implementation of DeploymentStatus
 *
 * @@author  Radim Kubacki
 */
public class Status implements DeploymentStatus {
    
    /** Value of action type. */
    private ActionType at;
    
    /** Executed command. */
    private CommandType ct;
    
    /** Status message. */
    private String msg;
    
    /** Current state. */
    private StateType state;
    
    public Status (ActionType at, CommandType ct, String msg, StateType state) {
        this.at = at;
        this.ct = ct;
        this.msg = msg;
        this.state = state;
    }
    
    public ActionType getAction () {
        return at;
    }
    
    public CommandType getCommand () {
        return ct;
    }
    
    public String getMessage () {
        return msg;
    }
    
    public StateType getState () {
        return state;
    }
    
    public boolean isCompleted () {
        return StateType.COMPLETED.equals (state);
    }
    
    public boolean isFailed () {
        return StateType.FAILED.equals (state);
    }
    
    public boolean isRunning () {
        return StateType.RUNNING.equals (state);
    }
    
    public String toString () {
        return "A="+getAction ()+" S="+getState ()+" "+getMessage ();   // NOI18N
    }
}

