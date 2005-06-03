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

package org.netbeans.api.progress.aggregate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.Action;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Cancellable;

/**
 * a progress handle that allows aggregation of progress indication from multiple 
 * independant sources. All of the progress contributors are considered equal and are given 
 * equal share of the global progress.
 * The task progress contributors can be added dynamically and
 * the progress bar adjusts accordingly, never stepping back though.
 *
 * For a more simple version of progress indication, see {@link org.netbeans.api.progress.ProgressHandle}
 * @author Milos Kleint (mkleint@netbeans.org)
 */
public final class AggregateProgressHandle {
    private ProgressMonitor monitor;
    private ProgressHandle handle;
    static final int WORKUNITS = 10000;
    private boolean finished;
    private Collection contributors;
    private int current;
    
    /** Creates a new instance of AggregateProgressHandle */
    AggregateProgressHandle(String displayName, ProgressContributor[] contribs, Cancellable cancellable, Action listAction, boolean systemtask) {
        handle = ProgressHandleFactory.createHandle(displayName, cancellable, listAction);
        finished = false;
        contributors = new ArrayList();
        if (contribs != null) {
            for (int i = 0; i < contribs.length; i++) {
                addContributor(contribs[i]);
            }
        }
    }
    

    /**
     * start the progress indication for the task, shows the progress in the UI, events from the contributors are
     *  expected after this call.
     */
    public void start() {
        start(-1);
    }

    /**
     * start the progress indication for the task with an initial time estimate, shows the progress in the UI, events from the contributors are
     * expected after this call.
     * @param estimate estimated time to process the task in seconds
     */
    public synchronized void start(long estimate) {
        handle.start(WORKUNITS, estimate);
        current = 0;
    }  
    
    /**
     * finish the task, remove the task's component from the progress bar UI, any additional incoming events from the 
     * contributors will be ignored.
     */
    public synchronized void finish() {
        if (finished) {
            return;
        }
        finished = true;
        handle.finish();
    }    
    
    /**
     * add a contributor to the global, aggregated progress.
     * Adding makes sense only if the task is still in progress.
     */
    public synchronized void addContributor(ProgressContributor contributor) {
        if (finished) {
            return;
        }
//        System.out.println("adding contributor=" + contributor.getTrackingId());
        int length = contributors.size();
        int remainingUnits = 0;
        double completedRatio = 0;
        Iterator it;
        if (length > 0) {
            it = contributors.iterator();
            while (it.hasNext()) {
                ProgressContributor cont = (ProgressContributor)it.next();
                remainingUnits = remainingUnits + cont.getRemainingParentWorkUnits();
                completedRatio = completedRatio + (1 - cont.getCompletedRatio());
            }
        } else {
            remainingUnits = WORKUNITS;
            completedRatio = 0;
        }

//        int idealShare = WORKUNITS / (length + 1);
        int currentShare = (int)(remainingUnits / (completedRatio + 1));
//        System.out.println("ideal share=" + idealShare);
//        System.out.println("current share=" + currentShare);
        it = contributors.iterator();
        while (it.hasNext()) {
            ProgressContributor cont = (ProgressContributor)it.next();
            int newshare = (int)((1 - cont.getCompletedRatio()) * currentShare);
//            System.out.println(" new share for " + cont.getTrackingId() + " is " + newshare);
            remainingUnits = remainingUnits - newshare;
            cont.setAvailableParentWorkUnits(newshare);
        }
//        System.out.println("new contributor share is=" + remainingUnits);
        contributor.setAvailableParentWorkUnits(remainingUnits);
        contributors.add(contributor);
        contributor.setParent(this);
        
    }
    
    /**
     * @deprecated do, not use, for tests only
     */
    int getCurrentProgress() {
        return current;
    }
    
    
    void processContributorStep(ProgressContributor contributor, String message, int delta) {
        synchronized (this) {
            if (finished) {
                return;
            }
            current = current + delta;
            handle.progress(message, current);
        }
        //shall we sychronize the monitor calls? since it calls out to client code,
        // cannot guarantee how long it will last..
        if (monitor != null) {
            monitor.progressed(contributor);
        }
        
    }
    
    void processContributorStart(ProgressContributor contributor, String message) {
        synchronized (this) {
            if (finished) {
                return;
            }
            if (message != null) {
                handle.progress(message);
            }
        }
        //shall we sychronize the monitor calls? since it calls out to client code,
        // cannot guarantee how long it will last..
        if (monitor != null) {
            monitor.started(contributor);
        }
    }
    
    void processContributorFinish(ProgressContributor contributor) {
        synchronized (this) {
            if (finished) {
                return;
            }
            contributors.remove(contributor);
            if (contributors.size() == 0) {
                finish();
            }
        }
        //shall we sychronize the monitor calls? since it calls out to client code,
        // cannot guarantee how long it will last..
        if (monitor != null) {
            monitor.finished(contributor);
        }
    }
    
    
    /**
     * allow to watch the incoming events from the individual progress contributors.
     */
    public void setMonitor(ProgressMonitor monitor) {
        this.monitor = monitor;
    }
}
