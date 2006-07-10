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
package org.netbeans.modules.collab.channel.filesharing.util;

import com.sun.collablet.CollabException;

import org.openide.*;

import java.util.*;

import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 * @author  Ayub Khan ayub.khan@sun.com
 */
public class QueueWorkerThread extends TimerTask {
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    ////////////////////////////////////////////////////////////////////////////
    public final static int MAX_RUNS_ALLOWED = 1;

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* context */
    private FilesharingContext context = null;

    /* item */
    private QueueItem item = null;
    private int noOfRuns = 0;

    /**
     *
     *
     */
    public QueueWorkerThread(CollabContext context, QueueItem item) {
        super();
        this.item = item;
        this.context = (FilesharingContext) context;
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////                       

    /**
     * getContext
     *
     */
    public FilesharingContext getContext() {
        return this.context;
    }

    /**
     * getCollab
     *
     */
    public CCollab getCollab() {
        return this.item.getCollab();
    }

    /**
     * schedule
     *
     * @param delay
     */
    public void schedule(long delay) {
        getContext().schedule(this, delay);
    }

    /**
     * scheduleAtFixedRate
     *
     * @param delay
     * @param rate
     */
    public void scheduleAtFixedRate(long delay, long rate) {
        getContext().scheduleAtFixedRate(this, delay, rate);
    }

    /**
     * run
     *
     */
    public void run() {
        try {
            getContext().sendMessage(getCollab());

            //getContext().getQueue().removeItem(this.item);			
            noOfRuns++;

            if (noOfRuns >= MAX_RUNS_ALLOWED) {
                cancel();
            }
        } catch (CollabException ce) {
            ErrorManager.getDefault().notify(ce);
        }
    }

    /**
     * cancel
     *
     */
    public boolean cancel() {
        Debug.log("CollabQueue", "QueueWorkerThread, cancel: " + this.item.getName()); //NoI18n		

        boolean status = super.cancel();
        getContext().getQueue().removeItem(this.item);

        return status;
    }
}
