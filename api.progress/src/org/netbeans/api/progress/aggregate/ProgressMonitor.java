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
 * Interface allowing to monitor the progress within the agrregated handle.
 * @see AggregateProgressHandle#setMonitor(ProgressMonitor)
 * @author mkleint
 */
public interface ProgressMonitor {
    
    /**
     * the given contributor started it's work.
     * @param contributor the part of the progress indication that started
     */
    void started(ProgressContributor contributor);
    /**
     * the given contributor finished it's work.
     * @param contributor the part of the progress indication that finished
     */
    void finished(ProgressContributor contributor);
    
    /**
     * the given contributor progressed in it's work.
     * @param contributor the part of the progress indication that showed progress
     */
    void progressed(ProgressContributor contributor);
    
}
