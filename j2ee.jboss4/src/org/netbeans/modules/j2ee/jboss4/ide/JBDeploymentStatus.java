/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.jboss4.ide;

import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.status.DeploymentStatus;

/**
 * An implementation of the DeploymentStatus interface used to track the
 * server start/stop progress.
 *
 * @author Kirill Sorokin
 */
public class JBDeploymentStatus implements DeploymentStatus {
    
    private ActionType action;
    private CommandType command;
    private StateType state;
    
    private String message;
    
    /** Creates a new instance of JBDeploymentStatus */
    public JBDeploymentStatus(ActionType action, CommandType command, StateType state, String message) {
        
        this.action = action;
        this.command = command;
        this.state = state;
        
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public StateType getState() {
        return state;
    }

    public CommandType getCommand() {
        return command;
    }

    public ActionType getAction() {
        return action;
    }
    
    public boolean isRunning() {
        return StateType.RUNNING.equals(state);
    }

    public boolean isFailed() {
        return StateType.FAILED.equals(state);
    }

    public boolean isCompleted() {
        return StateType.COMPLETED.equals(state);
    }

}