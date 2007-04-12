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

import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.ui.nodes.OpenableInBrowser;

/**
 * Implementation of the HudsonJob
 *
 * @author pblaha
 */
public class HudsonJobImpl implements HudsonJob, OpenableInBrowser {
    
    private String name;
    private String url;
    private Color color;
    private int lastBuild;
    
    private HudsonInstanceImpl instance;
    
    /** Creates a new instance of Job
     * @param name
     * @param url
     * @param color
     * @param lastBuild
     */
    public HudsonJobImpl(String name, String url, Color color, int lastBuild, HudsonInstanceImpl instance) {
        this.name = name;
        this.url = url;
        this.color = color;
        this.lastBuild = lastBuild;
        
        this.instance = instance;
    }
    
    public String getName() {
        return name;
    }
    
    public String getUrl() {
        return url;
    }
    
    public Color getColor() {
        return color;
    }
    
    public int getLastBuild() {
        return lastBuild;
    }
    
    public void start() {
        instance.getConnector().startJob(this);
    }
    
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final HudsonJobImpl other = (HudsonJobImpl) obj;
        
        if (this.name != other.name &&
                (this.name == null || !this.name.equals(other.name)))
            return false;
        if (this.url != other.url &&
                (this.url == null || !this.url.equals(other.url)))
            return false;
        if (this.color != other.color &&
                (this.color == null || !this.color.equals(other.color)))
            return false;
        if (this.lastBuild != other.lastBuild)
            return false;
        return true;
    }
    
    public int hashCode() {
        int hash = 3;
        
        hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 79 * hash + (this.url != null ? this.url.hashCode() : 0);
        hash = 79 * hash + (this.color != null ? this.color.hashCode() : 0);
        hash = 79 * hash + this.lastBuild;
        
        return hash;
    }
    
    public int compareTo(HudsonJob o) {
        return getName().compareTo(o.getName());
    }
}