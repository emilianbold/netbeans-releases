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


import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;

import org.openide.windows.TopComponent;


/**
 * Model which stored TopComponents in one mode. It manages opened, closed
 * and selected TopComponent.
 * This sub model is not thread safe. It is supposed to be just part of DefaultModeModel
 * which is responsible for the synch.
 *
 * @author  Peter Zavadsky
 */
final class TopComponentSubModel {
    
    // XXX PENDING This strange structure (List + WeakHashMap) is here due to 'ugly"
    // spec (undocummented probably) comming from old wisys, that if component is closed
    // and released all its client references it will be garbeged!! Thus the cloesed ones
    // could be kept only via weak references.
    private final List openedTopComponents = new ArrayList(10);
    /** Maps TopComponent ID to index. */
    private final Map tcID2index = new HashMap(10);
    
    /** Selected TopComponent ID. Has to be present between openedTopComponenets. */
    private String selectedTopComponentID;

    
    public TopComponentSubModel() {
    }

    
    public List getTopComponents() {
        List l = new ArrayList(openedTopComponents);
        
        Set ids = tcID2index.keySet();
        Set s = new HashSet(ids.size());
        for(Iterator it = ids.iterator(); it.hasNext(); ) {
            String tcID = (String)it.next();
            TopComponent tc = getTopComponent(tcID);
            if(tc != null) {
                s.add(tc);
            } else {
                // XXX TopComponent was garbaged, remove its ID.
                tcID2index.remove(tcID);
            }
        }
        s.removeAll(openedTopComponents);
        l.addAll(s);
        
        return l;
    }
    
    public List getOpenedTopComponents() {
        return new ArrayList(openedTopComponents);
    }

    public boolean addOpenedTopComponent(TopComponent tc) {
        if(openedTopComponents.contains(tc)) {
            return false;
        }
        
        Integer index = (Integer)tcID2index.get(getID(tc));
        
        int position;
        if(index != null) {
            position = index.intValue();
            if(position < 0) {
                position = 0;
            } else if(position > openedTopComponents.size()) {
                position = openedTopComponents.size();
            }
        } else {
            position = openedTopComponents.size();
        }
        
        openedTopComponents.add(position, tc);
        
        if(selectedTopComponentID == null) {
            selectedTopComponentID = getID(tc);
        }
        
        return true;
    }
    
    public boolean insertOpenedTopComponent(TopComponent tc, int index) {
        if(index >= 0
        && !openedTopComponents.isEmpty()
        && openedTopComponents.size() > index
        && openedTopComponents.get(index) == tc) {
            return false;
        }
        
        // Remove from previous index
        openedTopComponents.remove(tc);
        
        int position = index;
        if(position < 0) {
            position = 0;
        } else if(position > openedTopComponents.size()) {
            position = openedTopComponents.size();
        }

        tcID2index.remove(getID(tc));
        openedTopComponents.add(position, tc);
        tcID2index.put(getID(tc), new Integer(position));
        
        if(selectedTopComponentID == null) {
            selectedTopComponentID = getID(tc);
        }
        
        return true;
    }
    
    public boolean addClosedTopComponent(TopComponent tc) {
        int index = openedTopComponents.indexOf(tc);
        
        if(index == -1) {
            if(!tcID2index.containsKey(getID(tc))) {
                tcID2index.put(getID(tc), null);
            }
        } else {
            openedTopComponents.remove(tc);
            if(!tcID2index.containsKey(getID(tc))) {
                tcID2index.put(getID(tc), new Integer(index));
            }
            if(selectedTopComponentID != null && selectedTopComponentID.equals(getID(tc))) {
                adjustSelectedTopComponent(index);
            }
        }
        
        return true;
    }
    
    public boolean addUnloadedTopComponent(String tcID) {
        if(!tcID2index.containsKey(tcID)) {
            tcID2index.put(tcID, null);
        }
        
        return true;
    }
    
    public boolean removeTopComponent(TopComponent tc) {
        boolean res;
        if(openedTopComponents.contains(tc)) {
            if(selectedTopComponentID != null && selectedTopComponentID.equals(getID(tc))) {
                int index = openedTopComponents.indexOf(getTopComponent(selectedTopComponentID));
                openedTopComponents.remove(tc);
                adjustSelectedTopComponent(index);
            } else {
                openedTopComponents.remove(tc);
            }
            tcID2index.remove(getID(tc));
            
            res = true;
        } else if(tcID2index.containsKey(getID(tc))) {
            tcID2index.remove(getID(tc));
            res = true;
        } else {
            res = false;
        }

        return res;
    }

    public boolean containsTopComponent(TopComponent tc) {
        return openedTopComponents.contains(tc) || tcID2index.keySet().contains(getID(tc));
    }
    
    public boolean isEmpty() {
        return tcID2index.isEmpty();
    }
    
    private void adjustSelectedTopComponent(int index) {
        if(openedTopComponents.isEmpty()) {
            selectedTopComponentID = null;
            return;
        }
        
        if(index > openedTopComponents.size() - 1) {
            index = openedTopComponents.size() - 1;
        }
        
        selectedTopComponentID = getID((TopComponent)openedTopComponents.get(index));
    }
    
    public void setSelectedTopComponent(TopComponent tc) {
        if(tc != null && !openedTopComponents.contains(tc)) {
            return;
        }
        
        selectedTopComponentID = getID(tc);
    }
    
    public void setUnloadedSelectedTopComponent(String tcID) {
        if(tcID != null && !getOpenedIDs().contains(tcID)) {
            return;
        }
        
        selectedTopComponentID = tcID;
    }
    
    private Set getOpenedIDs() {
        Set s = new HashSet(openedTopComponents.size());
        for(Iterator it = openedTopComponents.iterator(); it.hasNext(); ) {
            s.add(getID((TopComponent)it.next()));
        }
        return s;
    }
    
    public TopComponent getSelectedTopComponent() {
        return getTopComponent(selectedTopComponentID);
    }

    private static TopComponent getTopComponent(String tcID) {
        return WindowManagerImpl.getInstance().getTopComponentForID(tcID);
    }
    
    private static String getID(TopComponent tc) {
        return WindowManagerImpl.getInstance().findTopComponentID(tc);
    }
}
