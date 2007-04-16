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

package org.netbeans.modules.hudson.impl;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonView;
import org.netbeans.modules.hudson.ui.nodes.OpenableInBrowser;

/**
 * Implementation of the HudsonJob
 *
 * @author pblaha
 */
public class HudsonJobImpl implements HudsonJob, OpenableInBrowser {
    
    private String displayName;
    private String name;
    private String description;
    private String url;
    private Color color;
    private boolean isInQueue;
    private boolean isBuildable;
    
    private Collection<HudsonView> views = new ArrayList<HudsonView>();
    
    private HudsonInstanceImpl instance;
    
    /**
     * Creates a new instance of Job
     *
     * @param name
     * @param url
     * @param color
     */
    public HudsonJobImpl(String displayName, String name, String description, String url, Color color,
            boolean isInQueue, boolean isBuidable, HudsonInstanceImpl instance) {
        this.displayName = displayName;
        this.name = name;
        this.url = url;
        this.description = description;
        this.color = color;
        this.isInQueue = isInQueue;
        this.isBuildable = isBuidable;
        
        this.instance = instance;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getUrl() {
        return url;
    }
    
    public Color getColor() {
        return color;
    }
    
    public boolean isInQueue() {
        return isInQueue;
    }
    
    public boolean isBuildable() {
        return isBuildable;
    }
    
    public synchronized Collection<HudsonView> getViews() {
        return views;
    }
    
    public synchronized void addView(HudsonView view) {
        views.add(view);
    }
    
    public void start() {
        // Start job
        instance.getConnector().startJob(this);
        
        // Synchronize jobs
        instance.synchronize();
    }
    
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        
        final HudsonJobImpl other = (HudsonJobImpl) obj;
        
        if (this.displayName != other.displayName &&
                (this.displayName == null || !this.displayName.equals(other.displayName)))
            return false;
        if (this.name != other.name &&
                (this.name == null || !this.name.equals(other.name)))
            return false;
        if (this.url != other.url &&
                (this.url == null || !this.url.equals(other.url)))
            return false;
        if (this.color != other.color &&
                (this.color == null || !this.color.equals(other.color)))
            return false;
        if (this.isInQueue != other.isInQueue)
            return false;
        if (this.isBuildable != other.isBuildable)
            return false;
        
        return true;
    }
    
    public int compareTo(HudsonJob o) {
        return getDisplayName().compareTo(o.getDisplayName());
    }
}