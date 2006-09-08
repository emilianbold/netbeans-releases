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

package org.netbeans.core.windows.model;


import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.windows.TopComponent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 *
 * @author  Peter Zavadsky
 */
final class DefaultTopComponentGroupModel implements TopComponentGroupModel {

    /** Programatic name of group. */
    private final String name;

    /** The opening state of this group */
    private boolean opened;

    /** All TopComponent IDs belonging to this group. */
    private final Set<String> topComponents = new HashSet<String>(3);
    // XXX Helper
    /** All TopComponent IDs which were opened by this group (at the moment
     * when group was opening). When group is closed this set should be emtpy. */
    private final Set<String> openedTopComponents = new HashSet<String>(3);
    
    /** All TopComponent IDs which were already opened before this group was 
     * opened (at the moment when group was opening). When group is closed this 
     * set should be emtpy. */
    private final Set<String> openedBeforeTopComponents = new HashSet<String>(3);
    
    /** TopComponent IDs with opening flag. */
    private final Set<String> openingTopComponents = new HashSet<String>(3);
    /** TopComponent IDs with closing flag. */
    private final Set<String> closingTopComponents = new HashSet<String>(3);
    
    private final Object LOCK_OPENED = new Object();
    
    private final Object LOCK_TOPCOMPONENTS = new Object();

    
    public DefaultTopComponentGroupModel(String name, boolean opened) {
        this.name = name;
        this.opened = opened;
    }
    
    
    public String getName() {
        return name;
    }
    
    public void open(
            Collection<TopComponent> openedTopComponents, 
            Collection<TopComponent> openedBeforeTopComponents) {
        synchronized(LOCK_OPENED) {
            this.opened = true;
            this.openedTopComponents.clear();
            for(TopComponent tc: openedTopComponents) {
                String tcID = getID(tc);
                if(tcID != null) {
                    this.openedTopComponents.add(tcID);
                }
            }
            this.openedBeforeTopComponents.clear();
            for(TopComponent tc: openedBeforeTopComponents) {
                String tcID = getID(tc);
                if(tcID != null) {
                    this.openedBeforeTopComponents.add(tcID);
                }
            }
        }
    }
    
    public void close() {
        synchronized(LOCK_OPENED) {
            this.opened = false;
            this.openedTopComponents.clear();
            this.openedBeforeTopComponents.clear();
        }
    }
    
    public boolean isOpened() {
        synchronized(LOCK_OPENED) {
            return this.opened;
        }
    }
    
    public Set<TopComponent> getTopComponents() {
        Set<String> s;
        synchronized(LOCK_TOPCOMPONENTS) {
            s = new HashSet<String>(topComponents);
        }
        
        Set<TopComponent> result = new HashSet<TopComponent>(s.size());
        for(String tcId: s) {
            TopComponent tc = getTopComponent(tcId);
            if(tc != null) {
                result.add(tc);
            }
        }
        
        return result;
    }
    
    public Set<TopComponent> getOpenedTopComponents() {
        Set<String> s;
        synchronized(LOCK_OPENED) {
            s = new HashSet<String>(openedTopComponents);
        }
        
        Set<TopComponent> result = new HashSet<TopComponent>(s.size());
        for(String tcId: s) {
            TopComponent tc = getTopComponent(tcId);
            if(tc != null) {
                result.add(tc);
            }
        }
        
        return result;
    }
    
    public Set<TopComponent> getOpenedBeforeTopComponents() {
        Set<String> s;
        synchronized(LOCK_OPENED) {
            s = new HashSet<String>(openedBeforeTopComponents);
        }
        
        Set<TopComponent> result = new HashSet<TopComponent>(s.size());
        for(String tcId: s) {
            TopComponent tc = getTopComponent(tcId);
            if(tc != null) {
                result.add(tc);
            }
        }
        
        return result;
    }
    
    public Set<TopComponent> getOpeningTopComponents() {
        Set<String> s;
        synchronized(LOCK_TOPCOMPONENTS) {
            s = new HashSet<String>(openingTopComponents);
        }
        
        Set<TopComponent> result = new HashSet<TopComponent>(s.size());
        for(String tcId: s) {
            TopComponent tc = getTopComponent(tcId);
            if(tc != null) {
                result.add(tc);
            }
        }
        
        return result;
    }
    
    public Set<TopComponent> getClosingTopComponents() {
        Set<String> s;
        synchronized(LOCK_TOPCOMPONENTS) {
            s = new HashSet<String>(closingTopComponents);
        }
        
        Set<TopComponent> result = new HashSet<TopComponent>(s.size());
        for(String tcId: s) {
            TopComponent tc = getTopComponent(tcId);
            if(tc != null) {
                result.add(tc);
            }
        }
        
        return result;
    }

    public boolean addUnloadedTopComponent(String tcID) {
        synchronized(LOCK_TOPCOMPONENTS) {
            return topComponents.add(tcID);
        }
    }
    
    public boolean removeUnloadedTopComponent(String tcID) {
        synchronized(LOCK_TOPCOMPONENTS) {
            if(openingTopComponents.contains(tcID)) {
                openingTopComponents.remove(tcID);
            }
            if(closingTopComponents.contains(tcID)) {
                closingTopComponents.remove(tcID);
            }
            return topComponents.remove(tcID);
        }
    }
    
    public boolean addOpeningTopComponent(TopComponent tc) {
        return addUnloadedOpeningTopComponent(getID(tc));
    }
    
    public boolean addUnloadedOpeningTopComponent(String tcID) {
        synchronized(LOCK_TOPCOMPONENTS) {
            if(!topComponents.contains(tcID)) {
                topComponents.add(tcID);
            }
            return openingTopComponents.add(tcID);
        }
    }
    
    public boolean removeOpeningTopComponent(TopComponent tc) {
        return removeUnloadedOpeningTopComponent(getID(tc));
    }
    
    public boolean removeUnloadedOpeningTopComponent(String tcID) {
        synchronized(LOCK_TOPCOMPONENTS) {
            return openingTopComponents.remove(tcID);
        }
    }
    
    public boolean addUnloadedClosingTopComponent(String tcID) {
        synchronized(LOCK_TOPCOMPONENTS) {
            if(!topComponents.contains(tcID)) {
                topComponents.add(tcID);
            }
            return closingTopComponents.add(tcID);
        }
    }
    
    public boolean removeUnloadedClosingTopComponent(String tcID) {
        synchronized(LOCK_TOPCOMPONENTS) {
            return closingTopComponents.remove(tcID);
        }
    }
    
    // XXX
    public boolean addUnloadedOpenedTopComponent(String tcID) {
        synchronized(LOCK_OPENED) {
            if(!this.opened) {
                return false;
            }
            this.openedTopComponents.add(tcID);
        }
        return true;
    }

    private static TopComponent getTopComponent(String tcID) {
        return WindowManagerImpl.getInstance().getTopComponentForID(tcID);
    }
    
    private static String getID(TopComponent tc) {
        return WindowManagerImpl.getInstance().findTopComponentID(tc);
    }

    
    // XXX>>
    public Set<String> getTopComponentsIDs() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return new HashSet<String>(topComponents);
        }
    }
    
    public Set<String> getOpeningSetIDs() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return new HashSet<String>(openingTopComponents);
        }
    }
    public Set<String> getClosingSetIDs() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return new HashSet<String>(closingTopComponents);
        }
    }
    public Set<String> getOpenedTopComponentsIDs() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return new HashSet<String>(openedTopComponents);
        }
    }
    // XXX<<

}

