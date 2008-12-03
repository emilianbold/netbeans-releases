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

/**
 * Instance of the the Hudson Server
 *
 * @author Michal Mocnak
 */
public interface HudsonInstance extends Comparable<HudsonInstance> {
    
    /**
     * Name of the Hudson instance
     *
     * @return instance name
     */
    public String getName();
    
    /**
     * Returns version of the hudson instance
     *
     * @return hudson version
     */
    public HudsonVersion getVersion();
    
    /**
     * Returns state of the hudson instance
     * 
     * @return true if the instance is connected
     */
    public boolean isConnected();
    
    /**
     * URL of the Hudson instance
     *
     * @return instance url
     */
    public String getUrl();
    
    /**
     * Returns all Hudson jobs from registered instance
     *
     * @return collection of all jobs
     */
    public Collection<HudsonJob> getJobs();

    /**
     * Returns preferred, watched Hudson jobs from registered instance
     *
     * @return collection of all jobs
     */
    public Collection<HudsonJob> getPreferredJobs();
    
    /**
     * Returns all Hudson views from registered instance
     *
     * @return collection of all views
     */
    public Collection<HudsonView> getViews();
    
    /**
     * Register HudsonChangeListener
     *
     * @param l HudsonChangeListener
     */
    public void addHudsonChangeListener(HudsonChangeListener l);
    
    /**
     * Unregister HudsonChangeListener
     *
     * @param l HudsonChangeListener
     */
    public void removeHudsonChangeListener(HudsonChangeListener l);

    /**
     *
     * @return
     */
    boolean isPersisted();
}