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
