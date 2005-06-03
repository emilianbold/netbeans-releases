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

/**
 * A contributor to the aggragete progress indication.
 * <b> This class is not threadsafe, you should access the contributor from
 * one thread only. </b> 
 * @see AggregateProgressHandle#addContributor(ProgressContributor)
 * 
 * @author mkleint
 */
public final class ProgressContributor {
    private String id;
    private int workunits;
    private int current;
    private int parentUnits;
    private int lastParentedUnit; 
    private AggregateProgressHandle parent;
    
    /** Creates a new instance of ProgressContributor */
    ProgressContributor(String id) {
        this.id = id;
        workunits = 0;
        current = 0;
        lastParentedUnit = 0;
    }
    
    /**
     * an id that allows identification of the progress contributor by the monitor.
     */
    public String getTrackingId() {
        return id;
    }
    
    void setParent(AggregateProgressHandle par) {
        parent = par;
    }
    
    int getWorkUnits() {
        return workunits;
    }
    
    int getRemainingParentWorkUnits() {
        return parentUnits;
    }
    
    void setAvailableParentWorkUnits(int newCount) {
        parentUnits = newCount;
    }
    
    double getCompletedRatio() {
        return workunits == 0 ? 0 : (double)(current / workunits);
    }
    
    /**
     * start the progress indication for a task with known number of steps.
     * @param workunits a total number of workunits that this contributor will process.
     */
    public void start(int workunits) {
        if (parent == null) {
            return;
        }
        this.workunits = workunits;
        parent.processContributorStart(this, null);
    }
    
    
    /**
     * finish the contributor, possibly also the whole task if this instance was
     * the last one to finish.
     */
    public void finish() {
        if (parent == null) {
            return;
        }
        if (current < workunits) {
            progress(null, workunits);
        }
        parent.processContributorFinish(this);
    }
    
    
    /**
     * Notify the user about completed workunits.
     * @param workunit a cumulative number of workunits completed so far
     */
    public void progress(int workunit) {
        progress(null, workunit);
    }
    
    /**
     * Notify the user about progress by showing message with details.
     * @param message detailed info about current progress
     */
    public void progress(String message) {
        progress(message, current);
    }
    
    /**
     * Notify the user about completed workunits.
     * @param message detailed info about current progress
     * @param unit a cumulative number of workunits completed so far
     */
    public void progress(String message, int unit) {
        if (parent == null) {
            return;
        }
        assert unit >= current && unit <= workunits;
        if (message != null && unit == current) {
            // we need to process the message in any case..
            parent.processContributorStep(this, message, 0);
            return;
        }
        current = unit;
        int delta = current - lastParentedUnit;
        double step = (1 / ((double)parentUnits / (double)(workunits - lastParentedUnit)));
//        System.out.println("progress.. current=" + current + " latparented=" + lastParentedUnit);
//        System.out.println("           parent units=" + parentUnits);
//        System.out.println("           delta=" + delta);
//        System.out.println("           step=" + step);
        if (delta >= step) {
            int count = (int) (delta / step);
            lastParentedUnit = lastParentedUnit + (int)(count * step);
            parentUnits = parentUnits - count;
//            System.out.println("   count=" + count);
//            System.out.println("   newparented=" + lastParentedUnit);
//            System.out.println("   parentUnits=" + parentUnits);
            // call parent..
            parent.processContributorStep(this, message, count);
        }
    }    
}
