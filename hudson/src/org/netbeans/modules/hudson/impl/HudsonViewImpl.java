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

package org.netbeans.modules.hudson.impl;

import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonView;
import org.netbeans.modules.hudson.constants.HudsonViewConstants;
import org.netbeans.modules.hudson.ui.interfaces.OpenableInBrowser;
import org.netbeans.modules.hudson.util.HudsonPropertiesSupport;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Implementation of the HudsonView
 *
 * @author Michal Mocnak
 */
public class HudsonViewImpl implements HudsonView,
        HudsonViewConstants, OpenableInBrowser {
    
    private HudsonInstance instance;
    private HudsonPropertiesSupport properties = new HudsonPropertiesSupport();
    
    public HudsonViewImpl(HudsonInstance instance, String name, String description, String url) {
        properties.putProperty(HUDSON_VIEW_NAME, name);
        properties.putProperty(HUDSON_VIEW_DESCRIPTION, description);
        properties.putProperty(HUDSON_VIEW_URL, url);
        
        this.instance = instance;
    }
    
    public String getName() {
        return properties.getProperty(HUDSON_VIEW_NAME, String.class);
    }
    
    public String getDescription() {
        return properties.getProperty(HUDSON_VIEW_DESCRIPTION, String.class);
    }
    
    public String getUrl() {
        return properties.getProperty(HUDSON_VIEW_URL, String.class);
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof HudsonViewImpl))
            return false;
        
        HudsonViewImpl v = (HudsonViewImpl) o;
        
        return getName().equals(v.getName()) && getUrl().equals(v.getUrl());
    }
    
    public Lookup getLookup() {
        return Lookups.fixed(instance);
    }
}