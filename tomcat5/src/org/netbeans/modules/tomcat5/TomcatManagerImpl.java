/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import org.openide.util.RequestProcessor;

/** Implemtation of management task that provides info about progress
 *
 * @author  Radim Kubacki
 */
class TomcatManagerImpl implements ProgressObject, Runnable {
    
    /** RequestProcessor processor that serializes management tasks. */
    private static RequestProcessor rp;
    
    /** Returns shared RequestProcessor. */
    private static synchronized RequestProcessor rp () {
        if (rp == null) {
            rp = new RequestProcessor ("Tomcat management", 1);
        }
        return rp;
    }
    
    /** List of ProgressListener s. */
    private List lsnrs = new ArrayList ();
    
    /** Command that is executed on running server. */
    private String command;
    
    private TomcatManager tm;
    
    /** TargetModuleID of module that is managed. */
    private TargetModuleID tmId;

    public TomcatManagerImpl (TomcatManager tm) {
        this.tm = tm;
    }

    public void deploy (Target t, InputStream is, InputStream deplPlan) {
    }
    
    /** Deploys WAR file or directory to Tomcat using deplPlan as source 
     * of conetx configuration data.
     */
    public void install (Target t, File wmfile, File deplPlan) {
        String path = "/test";     // PENDING: get path from deplPlan or wmfile
        command = "install";
        tmId = new TomcatModule (t, path);
        rp ().post (this, 0, Thread.NORM_PRIORITY);
    }
    
    /** JSR88 method. */
    public ClientConfiguration getClientConfiguration (TargetModuleID targetModuleID) {
        return null; // PENDING
    }
    
    /** JSR88 method. */
    public DeploymentStatus getDeploymentStatus () {
        return null; // PENDING
    }
    
    /** JSR88 method. */
    public TargetModuleID[] getResultTargetModuleIDs () {
        return null; // PENDING
    }
    
    /** JSR88 method. */
    public boolean isCancelSupported () {
        return false;
    }
    
    /** JSR88 method. */
    public void cancel () 
    throws OperationUnsupportedException {
        throw new OperationUnsupportedException ("cancel not supported in Tomcat deployment");
    }
    
    /** JSR88 method. */
    public boolean isStopSupported () {
        return false;
    }
    
    /** JSR88 method. */
    public void stop () throws OperationUnsupportedException {
        throw new OperationUnsupportedException ("stop not supported in Tomcat deployment");
    }
    
    /** JSR88 method. */
    public void addProgressListener (ProgressListener l) {
        lsnrs.add (l);
    }
    
    /** JSR88 method. */
    public void removeProgressListener (ProgressListener l) {
        lsnrs.remove (l);
    }
    
    private void fireProgressEvent (ProgressEvent e) {
        Iterator it = lsnrs.iterator ();
        while (it.hasNext ()) {
            ProgressListener l = (ProgressListener)it.next ();
            l.handleProgressEvent (e);
        }
    }
    
    /** Executes one management task. */
    public void run () {
        fireProgressEvent (new ProgressEvent (this, tmId, null)); // PENDING
    }
    
}
