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

package org.netbeans.modules.tomcat5.progress;

import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import org.netbeans.modules.tomcat5.TomcatFactory;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;

/** 
 * This is a utility class that can be used by ProgressObject's,
 * You can use an instance of this class as a member field
 * of your ProgressObject and delegate various work to it.
 *
 * @author  Radim Kubacki
 */
public class ProgressEventSupport {

    /** Source object. */
    private Object obj;
    
    private java.util.Vector listeners;
    
    private DeploymentStatus status;
    
    private TargetModuleID tmID;
    
    /**
     * Constructs a <code>ProgressEventSupport</code> object.
     *
     * @param o Source for any events.
     */
    public ProgressEventSupport (Object o) {
        if (o == null) {
            throw new NullPointerException ();
        }
        obj = o;
    }
    
    /** Add a ProgressListener to the listener list. */
    public synchronized void addProgressListener (ProgressListener lsnr) {
        boolean notify = false;
        if (listeners == null) {
            listeners = new java.util.Vector();
        }
        listeners.addElement(lsnr);
        if (status != null && !status.isRunning ()) {
            notify = true;
        }
        if (notify) {
            // not to miss completion event
            RequestProcessor.getDefault ().post (new Runnable () {
                public void run () {
                    fireHandleProgressEvent (tmID, status);
                }
            });
        }
    }
    
    /** Remove a ProgressListener from the listener list. */
    public synchronized void removeProgressListener (ProgressListener lsnr) {
        if (listeners == null) {
            return;
        }
        listeners.removeElement(lsnr);
    }

    /** Report event to any registered listeners. */
    public void fireHandleProgressEvent (TargetModuleID targetModuleID, DeploymentStatus sCode) {
        if (TomcatFactory.getEM ().isLoggable (ErrorManager.INFORMATIONAL)) {
            TomcatFactory.getEM ().log ("progress event from "+obj+" status "+sCode); // NOI18N
        }
        ProgressEvent evt = new ProgressEvent (obj, targetModuleID, sCode);
        status = sCode;
        tmID = targetModuleID;
        
	java.util.Vector targets = null;
	synchronized (this) {
	    if (listeners != null) {
	        targets = (java.util.Vector) listeners.clone();
	    }
	}

	if (targets != null) {
	    for (int i = 0; i < targets.size(); i++) {
	        ProgressListener target = (ProgressListener)targets.elementAt(i);
	        target.handleProgressEvent (evt);
	    }
	}
    }
    
    /** Returns last DeploymentStatus notified by {@link fireHandleProgressEvent}
     */
    public DeploymentStatus getDeploymentStatus () {
        return status;
    }
}
