/*
 * StartServerProgress.java
 *
 * Created on September 29, 2003, 12:17 PM
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
 * This is an utility implement class to avoid exposing JSR-88
 * ProgressObject to server management SPI: StartServer.
 *
 * Typical usage is for plugin StartServer implementation to create
 * instance of ServerProgress and return it to caller of 
 * startDeploymentManager, stopDeploymentManager and startDebugging.  
 * Plugin will update caller on progress of the operation through
 * the ServerProgress.setStatus().
 *
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
            public boolean isCompleted() { return false; }
            public boolean isFailed() { return true; }
            public boolean isRunning() { return false; }
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

