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