/*
 *                 Sun Public License Notice
 *
 * The contents of thisfile are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.tests.j2eeserver.plugin.jsr88;

import javax.enterprise.deploy.spi.*;
import javax.enterprise.deploy.shared.*;
import javax.enterprise.deploy.model.*;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.*;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerProgress;
import javax.enterprise.deploy.shared.*;

import java.io.*;
import java.util.*;

/**
 *
 * @author  gfink
 * @author nn136682
 */
public class ProgObject extends  ServerProgress {

    private TargetModuleID[] tmIDs;
    
    public ProgObject(DeploymentManager dm, Target[] targets, File archive, Object plan) {
        this(dm, createTargetModuleIDs(targets, archive));
    }

    public ProgObject(DeploymentManager dm, Target[] targets, Object archive, Object plan) {
        this(dm, new TargetModuleID[0]);
    }
    
    public ProgObject(DeploymentManager dm, TargetModuleID[] modules) {
        super(dm);
        tmIDs = modules;
    }
    
    public static TargetModuleID[] createTargetModuleIDs(Target[] targets, File archive) {
        TargetModuleID [] ret = new TargetModuleID[targets.length];
        for (int i=0; i<ret.length; i++) {
            ret[i] = new TestTargetMoid(targets[i], archive.getName(), getType(archive.getName()));
            ((Targ)targets[i]).add(ret[i]);
        }
        return ret;
    }
    static ModuleType getType(String name) {
        if (name.endsWith(".ear")) return ModuleType.EAR;
        else if (name.endsWith(".jar")) return ModuleType.EJB; //PENDING: libraries and client
        else if (name.endsWith(".war")) return ModuleType.WAR;
        else if (name.endsWith(".rar")) return ModuleType.RAR;
        else throw new IllegalArgumentException("Invalid archive name: " + name);
    }
    public void setStatusDistributeRunning(String message) {
        notify(createRunningProgressEvent(CommandType.DISTRIBUTE, message));
    }
    public void setStatusDistributeFailed(String message) {
        notify(createFailedProgressEvent(CommandType.DISTRIBUTE, message));
    }
    public void setStatusDistributeCompleted(String message) {
        notify(createCompletedProgressEvent(CommandType.DISTRIBUTE, message)); 
    }

    public void setStatusRedeployRunning(String message) {
        notify(createRunningProgressEvent(CommandType.REDEPLOY, message));
    }
    public void setStatusRedeployFailed(String message) {
        notify(createFailedProgressEvent(CommandType.REDEPLOY, message));
    }
    public void setStatusRedeployCompleted(String message) {
        notify(createCompletedProgressEvent(CommandType.REDEPLOY, message)); 
    }

    public void cancel() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("Test plugin does not support cancel!");
    }
    
    public void stop() throws OperationUnsupportedException {
        throw new OperationUnsupportedException("Test plugin does not support stop!");
    }
    
    public boolean isCancelSupported() {
        return false;  // PENDING parameterize?
    }
    
    public boolean isStopSupported() {
        return false; // PENDING see above
    }
    
    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID) {
        return null; // PENDING client support
    }
    
    public DeploymentStatus getDeploymentStatus() {
        return super.getDeploymentStatus();
    }
    
    public TargetModuleID[] getResultTargetModuleIDs() {
        List ret = new Vector();
        for (int i=0; i<tmIDs.length; i++) {
            if (tmIDs[i].getChildTargetModuleID() != null)
                ret.addAll(Arrays.asList(tmIDs[i].getChildTargetModuleID()));
            ret.add(tmIDs[i]);
        }
        return (TargetModuleID[]) ret.toArray(new TargetModuleID[ret.size()]);
    }
}
