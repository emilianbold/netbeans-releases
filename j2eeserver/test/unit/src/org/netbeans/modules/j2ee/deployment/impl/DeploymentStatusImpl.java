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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.impl;

import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.status.DeploymentStatus;


/**
 *
 * @author sherold
 */
public class DeploymentStatusImpl implements DeploymentStatus {
    
    private final ActionType action;
    private final CommandType command;
    private final String message;
    private final StateType state;

    /**
     * Creates a new instance of Status
     */
    public DeploymentStatusImpl(CommandType command, String message, StateType state) {
        this.action = ActionType.EXECUTE;
        this.command = command;
        this.message = message;
        this.state = state;
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

    public String getMessage() {
        return message;
    }

    public boolean isCompleted () {
        return StateType.COMPLETED.equals(state);
    }
    
    public boolean isFailed () {
        return StateType.FAILED.equals(state);
    }
    
    public boolean isRunning () {
        return StateType.RUNNING.equals(state);
    }
    
    public String toString () {
        return "action=" + action + " command=" + command + " state=" + state + "message=" + message;   // NOI18N
    }
    
}
