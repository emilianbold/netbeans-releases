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

package org.netbeans.modules.j2ee.deployment.plugins.api;

import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.shared.ActionType;
import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.exceptions.*;

import java.util.Iterator;

/**
 * This is an utility class to avoid exposing deployment interface
 * {@link javax.enterprise.deploy.spi.status.ProgressObject} directly in 
 * server management SPI {@link StartServer}.
 * <P>
 * Typical usage is for plugin StartServer implementation to create
 * instance of ServerProgress and return it to caller of 
 * startDeploymentManager, stopDeploymentManager and startDebugging.  
 * Plugin will update caller on progress of the operation through
 * method calls to set status.
 * <P>
 * @author  nn136682
 */

public class ServerProgress implements ProgressObject {
    private Object server;
    private java.util.Vector listeners = new java.util.Vector();
    private DeploymentStatus status;
    
    /** Creates a new instance of StartServerProgress */
    public ServerProgress(Object server) {
        this.server = server;
        createRunningProgressEvent(CommandType.START, ""); //NOI18N
    }

    public static final Command START_SERVER = new Command(25, "START SERVER"); //NOI18N
    public static final Command STOP_SERVER = new Command(26, "STOP SERVER"); //NOI18N
   
    public static class Command extends CommandType {
        String commandString;
        public Command(int val, String commandString) {
            super(val);
            this.commandString = commandString;
        }
        public String toString() {
            return commandString;
        }
    }
    
    public void setStatusStartRunning(String message) {
        notify(createRunningProgressEvent(START_SERVER, message));
    }
    public void setStatusStartFailed(String message) {
        notify(createFailedProgressEvent(START_SERVER, message));
    }
    public void setStatusStartCompleted(String message) {
        notify(createCompletedProgressEvent(START_SERVER, message)); 
    }
    public void setStatusStopRunning(String message) {
        notify(createRunningProgressEvent(STOP_SERVER, message));
    }
    public void setStatusStopFailed(String message) {
        notify(createFailedProgressEvent(STOP_SERVER, message));
    }
    public void setStatusStopCompleted(String message) {
        notify(createCompletedProgressEvent(CommandType.STOP, message)); 
    }
    protected synchronized void notify(ProgressEvent pe) {
        for (Iterator i=listeners.iterator(); i.hasNext();) {
            ProgressListener pol = (ProgressListener) i.next();
            pol.handleProgressEvent(pe);
        }
    } 

    protected DeploymentStatus createDeploymentStatus(final CommandType comtype, final String msg, final StateType state) {
        return new DeploymentStatus() {
            public ActionType getAction() { return ActionType.EXECUTE; }
            public CommandType getCommand() { return comtype; }
            public String getMessage() { return msg; }
            public StateType getState() { return state; }

            public boolean isCompleted () {
                return StateType.COMPLETED.equals(state);
            }

            public boolean isFailed () {
                return StateType.FAILED.equals(state);
            }

            public boolean isRunning () {
                return StateType.RUNNING.equals(state);
            }
        };
    }        
    protected ProgressEvent createCompletedProgressEvent(CommandType command, String message) {
        status = createDeploymentStatus(command, message, StateType.COMPLETED);
        return new ProgressEvent(server, null, status);
    }
    
    protected ProgressEvent createFailedProgressEvent(CommandType command, String message) {
        status = createDeploymentStatus(command, message, StateType.FAILED);
        return new ProgressEvent(server, null, status);
    }

    protected ProgressEvent createRunningProgressEvent(CommandType command, String message) {
        status = createDeploymentStatus(command, message, StateType.RUNNING);
        return new ProgressEvent(server, null, status);
    }    
//-------------- JSR88 ProgressObject -----------------
    public synchronized void addProgressListener(ProgressListener pol) {
        listeners.add(pol);
    }
    public synchronized void removeProgressListener(ProgressListener pol) {
        /*for (Iterator i=listeners.iterator(); i.hasNext();) {
            if(i.next().equals(pol))
                i.remove();
        }*/
        listeners.remove(pol);
    }
    
    public boolean isCancelSupported() { return true; }
    public void cancel() throws OperationUnsupportedException {
        //noop
    }
    public boolean isStopSupported() { return false; }
    public void stop() throws OperationUnsupportedException {
        //noop
    }
    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
        return null;
    }
    public DeploymentStatus getDeploymentStatus() {
        return status;
    }
    public TargetModuleID[] getResultTargetModuleIDs() {
        return new TargetModuleID[0];
    }   
}

