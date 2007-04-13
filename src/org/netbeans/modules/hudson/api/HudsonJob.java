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

package org.netbeans.modules.hudson.api;


/**
 * Instance of the Hudson Job in specified instance
 * 
 * @author Michal Mocnak
 */
public interface HudsonJob extends Comparable<HudsonJob> {
    
    /**
     * Describes state of the Hudson Job
     */
    public enum Color { 
        blue, blue_anime, red, red_anime, yellow, yellow_anime, grey, grey_anime
    }
    
    /**
     * Name of the Hudson Job
     * 
     * @return job name
     */
    public String getName();
    
    /**
     * URL of the Hudson Job
     * 
     * @return job url
     */
    public String getUrl();
    
    /**
     * Color of the Hudson Job's state
     * 
     * @return job color (state)
     */
    public Color getColor();
    
    /**
     * ID of the last build of the Hudson Job
     * 
     * @return id of the last build
     */
    public int getLastBuild();
    
    /**
     * Starts Hudson job
     */
    public void start();
}