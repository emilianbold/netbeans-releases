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
import org.netbeans.modules.hudson.impl.HudsonManagerImpl;
import org.openide.util.Lookup;

/**
 * Registry for Hudson Instances
 *
 * @author Michal Mocnak
 */
public abstract class HudsonManager {
    
    /** The only instance of the hudson manager implementation in the system */
    private static HudsonManagerImpl nonLookupInstance;
    
    /**
     * Singleton instance accessor method for hudson manager
     *
     * @return instance of hudson manager installed in the system
     */
    public static final HudsonManager getDefault() {
        HudsonManager hmInstance = Lookup.getDefault().lookup(HudsonManager.class);
        
        return (hmInstance != null) ? hmInstance : getNonLookupInstance();
    }
    
    private static final HudsonManager getNonLookupInstance() {
        if (null == nonLookupInstance)
            nonLookupInstance = new HudsonManagerImpl();
        
        return nonLookupInstance;
    }
    
    /**
     * Returns URL specified HudsonInstance
     *
     * @param url
     * @return hudson instance
     */
    public abstract HudsonInstance getInstance(String url);
    
    /**
     * Returns all of the Hudson instances
     *
     * @return all of hudson instances
     */
    public abstract Collection<HudsonInstance> getInstances();
    
    /**
     * Register HudsonChangeListener
     *
     * @param l HudsonChangeListener
     */
    public abstract void addHudsonChangeListener(HudsonChangeListener l);
    
    /**
     * Unregister HudsonChangeListener
     *
     * @param l HudsonChangeListener
     */
    public abstract void removeHudsonChangeListener(HudsonChangeListener l);
}