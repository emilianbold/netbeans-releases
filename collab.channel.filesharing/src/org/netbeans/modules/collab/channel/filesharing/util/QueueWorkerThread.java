/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
