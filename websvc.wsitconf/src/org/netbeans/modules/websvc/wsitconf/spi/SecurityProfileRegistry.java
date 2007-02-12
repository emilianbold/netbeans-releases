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

package org.netbeans.modules.websvc.wsitconf.spi;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Martin Grebac
 */
public class SecurityProfileRegistry {
    
    private static SecurityProfileRegistry instance;
    
    private Map<String, SecurityProfile> profiles = 
            Collections.synchronizedMap(new HashMap<String, SecurityProfile>());
    
    /**
     * Creates a new instance of SecurityProfileRegistry
     */
    private SecurityProfileRegistry() {}

    /**
     * Returns default singleton instance of registry
     */
    public static SecurityProfileRegistry getDefault(){
        if (instance == null) {
            instance = new SecurityProfileRegistry();
        }
        return instance;
    }
    
    /** 
     * Returns profile from list based on it's display name
     */
    public SecurityProfile getProfile(String displayName) {
        return profiles.get(displayName);
    }
    
    /**
     * Registers profile to the list
     */
    public void register(SecurityProfile profile){
        profiles.put(profile.getDisplayName(), profile);
    }
    
    /**
     * Unregisters profile from the list
     */
    public void unregister(SecurityProfile profile){
        profiles.remove(profile.getDisplayName());
    }
    
    public void unregister(String profile){
        profiles.remove(profile);
    }
    
    public Set<SecurityProfile> getSecurityProfiles() {
        
        TreeSet<SecurityProfile> set = new TreeSet(new Comparator() {
            public int compare(Object obj1, Object obj2) {
                SecurityProfile p1 = (SecurityProfile)obj1;
                SecurityProfile p2 = (SecurityProfile)obj2;
                Integer i1 = p1.getId();
                Integer i2 = p2.getId();
                return i1.compareTo(i2);
            }
        });
        set.addAll(profiles.values());
        return Collections.synchronizedSortedSet(set);
    }
    
}
