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

package org.netbeans.modules.tomcat5.progress;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressEvent;
import javax.enterprise.deploy.spi.status.ProgressListener;
import org.netbeans.modules.tomcat5.TomcatFactory;

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
        if (listeners == null) {
            listeners = new java.util.Vector();
        }
        listeners.addElement(lsnr);
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
        Logger.getLogger(ProgressEventSupport.class.getName()).log(Level.FINE, "progress event from " + obj + " status " + sCode); // NOI18N
        synchronized (this) {
            status = sCode;
        }
        ProgressEvent evt = new ProgressEvent (obj, targetModuleID, sCode);
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
    public synchronized DeploymentStatus getDeploymentStatus () {
        return status;
    }
}
