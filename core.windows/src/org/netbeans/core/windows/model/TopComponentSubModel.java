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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.core.windows.Constants;

/**
 * Model which stored TopComponents in one mode. It manages opened, closed
 * and selected TopComponent.
 * This sub model is not thread safe. It is supposed to be just part of DefaultModeModel
 * which is responsible for the synch.
 *
 * @author  Peter Zavadsky
 */
final class TopComponentSubModel {

    /** List of opened TopComponents. */
    private final List<TopComponent> openedTopComponents = new ArrayList<TopComponent>(10);
    /** List of all TopComponent IDs (both opened and closed). */
    private final List<String> tcIDs = new ArrayList<String>(10);
    /** kind of mode model this sub model is part of */
    private final int kind;
    /** Selected TopComponent ID. Has to be present in openedTopComponenets. */
    private String selectedTopComponentID;
    /** ID of top component that was the selected one before switching to/from maximized mode */
    private String previousSelectedTopComponentID;

    public TopComponentSubModel(int kind) {
        this.kind = kind;
    }
    
    public List<TopComponent> getTopComponents() {
        List<TopComponent> l = new ArrayList<TopComponent>(openedTopComponents);
        
        List<String> ids = new ArrayList<String>(tcIDs);
        List<TopComponent> ll = new ArrayList<TopComponent>(ids.size());
        for(Iterator<String> it = ids.iterator(); it.hasNext(); ) {
            String tcID = it.next();
            TopComponent tc = getTopComponent(tcID);
            if(tc != null) {
                ll.add(tc);
            } else {
                // XXX TopComponent was garbaged, remove its ID.
                it.remove();
            }
        }
        ll.removeAll(openedTopComponents);
        l.addAll(ll);
        
        return l;
    }
    
    public List<TopComponent> getOpenedTopComponents() {
        return new ArrayList<TopComponent>(openedTopComponents);
    }

    public boolean addOpenedTopComponent(TopComponent tc) {
        if(openedTopComponents.contains(tc)) {
            return false;
        }

        String tcID = getID(tc);
        int index = tcIDs.indexOf(tcID);
        
        int position = openedTopComponents.size();
        if( index >= 0 ) {
            for( TopComponent otc : openedTopComponents ) {
                String otcID = getID(otc);
                int openedIndex = tcIDs.indexOf( otcID );
                if( openedIndex >= index ) {
                    position = openedTopComponents.indexOf( otc );
                    break;
                }
            }
        }
        // additional double check if we got the same instance of topcomponent
        //#39914 + #43401 - no need to remove this one without fixing the inconsistency, it will fail later on TabbedAdapter.
        TopComponent persTC = getTopComponent(tcID);
        if (persTC != tc) {
            String message = "Model in inconsistent state, generated TC ID=" + tcID + " for " + tc.getClass() + ":" + tc.hashCode() + " but" +
            " that ID is reserved for TC=" + persTC.getClass() + ":" + persTC.hashCode();
            assert false : message;
        }
        //-- end of check..
        openedTopComponents.add(position, tc);
        if(!tcIDs.contains(tcID)) {
            tcIDs.add(tcID);
        }
        
        if(selectedTopComponentID == null && !isNullSelectionAllowed()) {
            selectedTopComponentID = tcID;
        }
        
        // XXX - should be deleted after TopComponent.isSliding is introduced
        if (kind == Constants.MODE_KIND_SLIDING) {
            setSlidingProperty(tc);
        } else {
            clearSlidingProperty(tc);
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

        String tcID = getID(tc);
        tcIDs.remove(tcID);
        openedTopComponents.add(position, tc);
        if(position == 0) {
            tcIDs.add(0, tcID);
        } else {
            TopComponent previous = (TopComponent)openedTopComponents.get(position - 1);
            int previousIndex = tcIDs.indexOf(getID(previous));
            tcIDs.add(previousIndex + 1, tcID);
        }
        
        if(selectedTopComponentID == null && !isNullSelectionAllowed()) {
            selectedTopComponentID = getID(tc);
        }
        
        // XXX - should be deleted after TopComponent.isSliding is introduced
        if (kind == Constants.MODE_KIND_SLIDING) {
            setSlidingProperty(tc);
        } else {
            clearSlidingProperty(tc);
        }
        
        return true;
    }
    
    public boolean addClosedTopComponent(TopComponent tc) {
        int index = openedTopComponents.indexOf(tc);
        String tcID = getID(tc);
        if (!tcIDs.contains(tcID)) {
            tcIDs.add(tcID);
        }
        if(index != -1) {
            openedTopComponents.remove(tc);
            if (selectedTopComponentID != null && selectedTopComponentID.equals(getID(tc))) {
                adjustSelectedTopComponent(index);
            }
        } 
        
        
        // XXX - should be deleted after TopComponent.isSliding is introduced
        if (kind == Constants.MODE_KIND_SLIDING) {
            setSlidingProperty(tc);
        } else {
            clearSlidingProperty(tc);
        }
        
        return true;
    }
    
    public boolean addUnloadedTopComponent(String tcID) {
        if(!tcIDs.contains(tcID)) {
            tcIDs.add(tcID);
        }
        
        return true;
    }
    
    public boolean removeTopComponent(TopComponent tc) {
        boolean res;
        String tcID = getID(tc);
        if(openedTopComponents.contains(tc)) {
            if(selectedTopComponentID != null && selectedTopComponentID.equals(tcID)) {
                int index = openedTopComponents.indexOf(getTopComponent(selectedTopComponentID));
                openedTopComponents.remove(tc);
                adjustSelectedTopComponent(index);
            } else {
                openedTopComponents.remove(tc);
            }
            tcIDs.remove(tcID);
            
            res = true;
        } else if(tcIDs.contains(tcID)) {
            tcIDs.remove(tcID);
            res = true;
        } else {
            res = false;
        }

        // XXX - should be deleted after TopComponent.isSliding is introduced
        clearSlidingProperty(tc);
        
        return res;
    }

    public boolean containsTopComponent(TopComponent tc) {
        return openedTopComponents.contains(tc) || tcIDs.contains(getID(tc));
    }
    
    public boolean isEmpty() {
        return tcIDs.isEmpty();
    }
    
    private void adjustSelectedTopComponent(int index) {
        if(openedTopComponents.isEmpty() || isNullSelectionAllowed()) {
            selectedTopComponentID = null;
            return;
        }
        
        if(index > openedTopComponents.size() - 1) {
            index = openedTopComponents.size() - 1;
        }
        
        selectedTopComponentID = getID((TopComponent)openedTopComponents.get(index));
    }

    /** @return true for sliding kind of model, false otherwise. It means that
     * null selection is valid only in sliding kind of model.
     */
    private boolean isNullSelectionAllowed() {
        return kind == Constants.MODE_KIND_SLIDING;
    }

    /** Sets selected component. Note that for sliding kind null selection
     * is allowed
     */
    public void setSelectedTopComponent(TopComponent tc) {
        if(tc != null && !openedTopComponents.contains(tc)) {
            return;
        }
        
        if (tc == null && isNullSelectionAllowed()) {
            selectedTopComponentID = null;
        } else {
            selectedTopComponentID = getID(tc);
        }
    }
    
    public void setPreviousSelectedTopComponent(TopComponent tc) {
        if(tc != null )
            previousSelectedTopComponentID = getID(tc);
        else
            previousSelectedTopComponentID = null;
    }
    
    public void setUnloadedSelectedTopComponent(String tcID) {
        if(tcID != null && !getOpenedTopComponentsIDs().contains(tcID)) {
            return;
        }
        
        selectedTopComponentID = tcID;
    }
    
    public void setUnloadedPreviousSelectedTopComponent(String tcID) {
        previousSelectedTopComponentID = tcID;
    }
    
    public List<String> getOpenedTopComponentsIDs() {
        List<String> l = new ArrayList<String>(openedTopComponents.size());
        for(Iterator<TopComponent> it = openedTopComponents.iterator(); it.hasNext(); ) {
            l.add(getID(it.next()));
        }
        return l;
    }
    
    // XXX
    public List<String> getClosedTopComponentsIDs() {
        List<String> closedIDs = new ArrayList<String>(tcIDs);
        closedIDs.removeAll(getOpenedTopComponentsIDs());
        return closedIDs;
    }

    // XXX
    public List<String> getTopComponentsIDs() {
        return new ArrayList<String>(tcIDs);
    }
    
    // XXX
    public void removeClosedTopComponentID(String tcID) {
        tcIDs.remove(tcID);
    }
    
    public TopComponent getSelectedTopComponent() {
        return getTopComponent(selectedTopComponentID);
    }
    
    public TopComponent getPreviousSelectedTopComponent() {
        if( null != previousSelectedTopComponentID )
            return getTopComponent(previousSelectedTopComponentID);
        return null;
    }

    private static TopComponent getTopComponent(String tcID) {
        return WindowManagerImpl.getInstance().getTopComponentForID(tcID);
    }
    
    private static String getID(TopComponent tc) {
        return WindowManagerImpl.getInstance().findTopComponentID(tc);
    }

    
    // XXX - should be deleted after TopComponent.isSliding is introduced
    private static final String IS_SLIDING = "isSliding";
    
    // XXX - should be deleted after TopComponent.isSliding is introduced
    private void setSlidingProperty(TopComponent tc) {
        tc.putClientProperty(IS_SLIDING, Boolean.TRUE);
    }

    // XXX - should be deleted after TopComponent.isSliding is introduced
    private void clearSlidingProperty(TopComponent tc) {
        tc.putClientProperty(IS_SLIDING, null);
    }
    
}
