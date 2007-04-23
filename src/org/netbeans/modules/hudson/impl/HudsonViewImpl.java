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

import org.netbeans.modules.hudson.api.HudsonView;
import org.netbeans.modules.hudson.ui.nodes.OpenableInBrowser;

/**
 * Implementation of the HudsonView
 *
 * @author Michal Mocnak
 */
public class HudsonViewImpl implements HudsonView, OpenableInBrowser {
    
    private String name;
    private String description;
    private String url;
    
    public HudsonViewImpl(String name, String description, String url) {
        this.name = name;
        this.description = description;
        this.url = url;
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
    
    public boolean equals(Object o) {
        if (!(o instanceof HudsonViewImpl))
            return false;
        
        HudsonViewImpl v = (HudsonViewImpl) o;
        
        return getName().equals(v.getName()) && getUrl().equals(v.getUrl());
    }
}