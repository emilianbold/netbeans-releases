/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
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
    private final Set topComponents = new HashSet(3);
    // XXX Helper
    /** All TopComponent IDs which were opened by this group (at the moment
     * when group was opening). When group is closed this set should be emtpy. */
    private final Set openedTopComponents = new HashSet(3);
    
    /** All TopComponent IDs which were already opened before this group was 
     * opened (at the moment when group was opening). When group is closed this 
     * set should be emtpy. */
    private final Set openedBeforeTopComponents = new HashSet(3);
    
    /** TopComponent IDs with opening flag. */
    private final Set openingTopComponents = new HashSet(3);
    /** TopComponent IDs with closing flag. */
    private final Set closingTopComponents = new HashSet(3);
    
    private final Object LOCK_OPENED = new Object();
    
    private final Object LOCK_TOPCOMPONENTS = new Object();

    
    public DefaultTopComponentGroupModel(String name, boolean opened) {
        this.name = name;
        this.opened = opened;
    }
    
    
    public String getName() {
        return name;
    }
    
    public void open(Collection openedTopComponents, Collection openedBeforeTopComponents) {
        synchronized(LOCK_OPENED) {
            this.opened = true;
            this.openedTopComponents.clear();
            for(Iterator it = openedTopComponents.iterator(); it.hasNext(); ) {
                String tcID = getID((TopComponent)it.next());
                if(tcID != null) {
                    this.openedTopComponents.add(tcID);
                }
            }
            this.openedBeforeTopComponents.clear();
            for(Iterator it = openedBeforeTopComponents.iterator(); it.hasNext(); ) {
                String tcID = getID((TopComponent)it.next());
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
    
    public Set getTopComponents() {
        Set s;
        synchronized(LOCK_TOPCOMPONENTS) {
            s = new HashSet(topComponents);
        }
        
        Set result = new HashSet(s.size());
        for(Iterator it = s.iterator(); it.hasNext(); ) {
            TopComponent tc = getTopComponent((String)it.next());
            if(tc != null) {
                result.add(tc);
            }
        }
        
        return result;
    }
    
    public Set getOpenedTopComponents() {
        Set s;
        synchronized(LOCK_OPENED) {
            s = new HashSet(openedTopComponents);
        }
        
        Set result = new HashSet(s.size());
        for(Iterator it = s.iterator(); it.hasNext(); ) {
            TopComponent tc = getTopComponent((String)it.next());
            if(tc != null) {
                result.add(tc);
            }
        }
        
        return result;
    }
    
    public Set getOpenedBeforeTopComponents() {
        Set s;
        synchronized(LOCK_OPENED) {
            s = new HashSet(openedBeforeTopComponents);
        }
        
        Set result = new HashSet(s.size());
        for(Iterator it = s.iterator(); it.hasNext(); ) {
            TopComponent tc = getTopComponent((String)it.next());
            if(tc != null) {
                result.add(tc);
            }
        }
        
        return result;
    }
    
    public Set getOpeningTopComponents() {
        Set s;
        synchronized(LOCK_TOPCOMPONENTS) {
            s = new HashSet(openingTopComponents);
        }
        
        Set result = new HashSet(s.size());
        for(Iterator it = s.iterator(); it.hasNext(); ) {
            TopComponent tc = getTopComponent((String)it.next());
            if(tc != null) {
                result.add(tc);
            }
        }
        
        return result;
    }
    
    public Set getClosingTopComponents() {
        Set s;
        synchronized(LOCK_TOPCOMPONENTS) {
            s = new HashSet(closingTopComponents);
        }
        
        Set result = new HashSet(s.size());
        for(Iterator it = s.iterator(); it.hasNext(); ) {
            TopComponent tc = getTopComponent((String)it.next());
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
    public Set getTopComponentsIDs() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return new HashSet(topComponents);
        }
    }
    
    public Set getOpeningSetIDs() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return new HashSet(openingTopComponents);
        }
    }
    public Set getClosingSetIDs() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return new HashSet(closingTopComponents);
        }
    }
    public Set getOpenedTopComponentsIDs() {
        synchronized(LOCK_TOPCOMPONENTS) {
            return new HashSet(openedTopComponents);
        }
    }
    // XXX<<

}

