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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.hudson.api;

import java.util.Collection;
import org.openide.util.Lookup;

/**
 * Instance of the Hudson Job in specified instance
 *
 * @author Michal Mocnak
 */
public interface HudsonJob extends Lookup.Provider, Comparable<HudsonJob> {
    
    /**
     * Describes state of the Hudson Job
     */
    public enum Color {
        aborted, aborted_anime,
        blue, blue_anime,
        disabled,
        red, red_anime, yellow, yellow_anime, grey, grey_anime
    }
    
    /**
     * Display name of the Hudson Job
     *
     * @return job display name
     */
    public String getDisplayName();
    
    /**
     * Name of the Hudson Job
     *
     * @return job's name
     */
    public String getName();
    
    /**
     * Description of the Hudson Job
     *
     * @return job's description
     */
    public String getDescription();
    
    /**
     * URL of the Hudson Job
     *
     * @return job url
     */
    public String getUrl();
    
    /**
     * Views where the job is situated
     * 
     * @return views
     */
    public Collection<HudsonView> getViews();
    
    /**
     * Color of the Hudson Job's state
     *
     * @return job color (state)
     */
    public Color getColor();
    
    /**
     * Returns job's queue state
     *
     * @return true if the job is in queue
     */
    public boolean isInQueue();
    
    /**
     * Returns job's buildable state
     *
     * @return true if the job is buildable
     */
    public boolean isBuildable();
    
    /**
     * Returns number of the last build
     * 
     * @return last build number
     */
    public int getLastBuild();
    
    /**
     * Returns number of the last stable build
     * 
     * @return last stable build number
     */
    public int getLastStableBuild();
    
    /**
     * Returns number of the last successful build
     * 
     * @return last successful build number
     */
    public int getLastSuccessfulBuild();
    
    /**
     * Returns number of the last failed build
     * 
     * @return last failed build number
     */
    public int getLastFailedBuild();
    
    /**
     * Starts Hudson job
     */
    public void start();
    
    /**
     * Returns default job lookup
     * 
     * @return default job lookup
     */
    public Lookup getLookup();
}