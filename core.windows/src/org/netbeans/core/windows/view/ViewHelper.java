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


package org.netbeans.core.windows.view;


import org.netbeans.core.windows.Debug;
import org.netbeans.core.windows.ModeStructureSnapshot;
import org.netbeans.core.windows.ModeStructureSnapshot.ElementSnapshot;
import org.netbeans.core.windows.WindowSystemSnapshot;

import java.util.*;



/**
 * This class converts snapshot to accessor structure, which is a 'model'
 * of view (GUI) structure window system has to display to user.
 * It reflects the specific view implementation (the difference from snapshot)
 * e.g. the nesting splitted panes, which imitates (yet nonexisiting) multi-split
 * component and also contains only visible elements in that structure.
 * It also provides computing of split weights.
 *
 * @author  Peter Zavadsky
 */
final class ViewHelper {
    
    /** Debugging flag. */
    private static final boolean DEBUG = Debug.isLoggable(ViewHelper.class);
    
    
    /** Creates a new instance of ViewHelper */
    private ViewHelper() {
    }
    
    
    public static WindowSystemAccessor createWindowSystemAccessor(
        WindowSystemSnapshot wss
    ) {
        // PENDING When hiding is null.
        if(wss == null) {
            return null;
        }
        
        WindowSystemAccessorImpl wsa = new WindowSystemAccessorImpl();

        ModeStructureAccessorImpl msa = createModeStructureAccessor(wss.getModeStructureSnapshot());
        wsa.setModeStructureAccessor(msa);

        ModeStructureSnapshot.ModeSnapshot activeSnapshot = wss.getActiveModeSnapshot();
        wsa.setActiveModeAccessor(activeSnapshot == null ? null : msa.findModeAccessor(activeSnapshot.getName()));
        
        ModeStructureSnapshot.ModeSnapshot maximizedSnapshot = wss.getMaximizedModeSnapshot();
        wsa.setMaximizedModeAccessor(maximizedSnapshot == null ? null : msa.findModeAccessor(maximizedSnapshot.getName()));

        wsa.setMainWindowBoundsJoined(wss.getMainWindowBoundsJoined());
        wsa.setMainWindowBoundsSeparated(wss.getMainWindowBoundsSeparated());
        wsa.setEditorAreaBounds(wss.getEditorAreaBounds());
        wsa.setEditorAreaState(wss.getEditorAreaState());
        wsa.setEditorAreaFrameState(wss.getEditorAreaFrameState());
        wsa.setMainWindowFrameStateJoined(wss.getMainWindowFrameStateJoined());
        wsa.setMainWindowFrameStateSeparated(wss.getMainWindowFrameStateSeparated());
        wsa.setToolbarConfigurationName(wss.getToolbarConfigurationName());
        wsa.setProjectName(wss.getProjectName());
        return wsa;
    }
    
    private static ModeStructureAccessorImpl createModeStructureAccessor(ModeStructureSnapshot mss) {
        ElementAccessor splitRoot = createVisibleAccessor(mss.getSplitRootSnapshot());
        Set separateModes = createSeparateModeAccessors(mss.getSeparateModeSnapshots());
        Set slidingModes = createSlidingModeAccessors(mss.getSlidingModeSnapshots());
        
        ModeStructureAccessorImpl msa =  new ModeStructureAccessorImpl(splitRoot, separateModes, slidingModes);
        return msa;
    }
    
    private static Set createSeparateModeAccessors(ModeStructureSnapshot.ModeSnapshot[] separateModeSnapshots) {
        Set s = new HashSet();
        for(int i = 0; i < separateModeSnapshots.length; i++) {
            ModeStructureSnapshot.ModeSnapshot snapshot = separateModeSnapshots[i];
            if(snapshot.isVisibleSeparate()) {
                s.add(new ModeStructureAccessorImpl.ModeAccessorImpl(
                    snapshot.getOriginator(),
                    snapshot));
            }
        }
        
        return s;
    }
    
    private static Set createSlidingModeAccessors(ModeStructureSnapshot.SlidingModeSnapshot[] slidingModeSnapshots) {
        Set s = new HashSet();
        ModeStructureSnapshot.SlidingModeSnapshot snapshot; 
        for(int i = 0; i < slidingModeSnapshots.length; i++) {
            snapshot = slidingModeSnapshots[i];
            s.add(new ModeStructureAccessorImpl.SlidingAccessorImpl(
                snapshot.getOriginator(),
                snapshot,
                snapshot.getSide()
            ));
        }
        
        return s;
    }

    /** */
    private static ElementAccessor createVisibleAccessor(ModeStructureSnapshot.ElementSnapshot snapshot) {
        if(snapshot == null) {
            return null;
        }

        if(snapshot instanceof ModeStructureSnapshot.EditorSnapshot) { // Is always visible.
            ModeStructureSnapshot.EditorSnapshot editorSnapshot = (ModeStructureSnapshot.EditorSnapshot)snapshot;
            return new ModeStructureAccessorImpl.EditorAccessorImpl(
                editorSnapshot.getOriginator(),
                editorSnapshot,
                createVisibleAccessor(editorSnapshot.getEditorAreaSnapshot()),
                editorSnapshot.getResizeWeight());
        }
        
        if(snapshot.isVisibleInSplit()) {
            if(snapshot instanceof ModeStructureSnapshot.SplitSnapshot) {
                ModeStructureSnapshot.SplitSnapshot splitSnapshot = (ModeStructureSnapshot.SplitSnapshot)snapshot;
                return createSplitAccessor(splitSnapshot);
            } else if(snapshot instanceof ModeStructureSnapshot.ModeSnapshot) {
                ModeStructureSnapshot.ModeSnapshot modeSnapshot = (ModeStructureSnapshot.ModeSnapshot)snapshot;
                return new ModeStructureAccessorImpl.ModeAccessorImpl(
                    modeSnapshot.getOriginator(),
                    modeSnapshot);
            }
        } else {
            if(snapshot instanceof ModeStructureSnapshot.SplitSnapshot) {
                ModeStructureSnapshot.SplitSnapshot splitSnapshot = (ModeStructureSnapshot.SplitSnapshot)snapshot;
                for(Iterator it = splitSnapshot.getChildSnapshots().iterator(); it.hasNext(); ) {
                    ModeStructureSnapshot.ElementSnapshot child = (ModeStructureSnapshot.ElementSnapshot)it.next();
                    if(child.hasVisibleDescendant()) {
                        return createVisibleAccessor(child);
                    }
                }
            }
        }
        
        return null;
    }
    
    private static ElementAccessor createSplitAccessor(ModeStructureSnapshot.SplitSnapshot splitSnapshot) {
        List visibleChildren = splitSnapshot.getVisibleChildSnapshots();
        
        List invisibleChildren = splitSnapshot.getChildSnapshots();
        invisibleChildren.removeAll(visibleChildren);
        double invisibleWeights = 0D;
        for(Iterator it = invisibleChildren.iterator(); it.hasNext(); ) {
            ModeStructureSnapshot.ElementSnapshot next = (ModeStructureSnapshot.ElementSnapshot)it.next();
            invisibleWeights += splitSnapshot.getChildSnapshotSplitWeight(next);
        }
        
        double delta = invisibleWeights;
        // Get the refined weights to work with.
        Map visibleChild2refinedWeight = new HashMap();
        for(Iterator it = visibleChildren.iterator(); it.hasNext(); ) {
            ModeStructureSnapshot.ElementSnapshot next = (ModeStructureSnapshot.ElementSnapshot)it.next();
            double refinedWeight = splitSnapshot.getChildSnapshotSplitWeight(next);
            if( !it.hasNext() )
                refinedWeight += delta; //add the weight of invisible children to the last element
            
            visibleChild2refinedWeight.put(next, new Double(refinedWeight));
        }
        
        // Begin from the end.
        // I.e. the splits are always nested the way,
        // the one at the LEFT (or TOP) side is the top level one.
        int orientation = splitSnapshot.getOrientation();
        // Group the split children into SplitSnapshots (-> corresponding to JSplitPanes)
        SplitAccessor se = null;
        List reversedVisibleChildren = new ArrayList(visibleChildren);
        Collections.reverse(reversedVisibleChildren);
        for(Iterator it = reversedVisibleChildren.iterator(); it.hasNext(); ) {
            ElementAccessor secondAccessor;
            if(se == null) {
                ModeStructureSnapshot.ElementSnapshot second = (ModeStructureSnapshot.ElementSnapshot)it.next();
                secondAccessor = createVisibleAccessor(second);
            } else {
                // There is nested split add that one to second place.
                secondAccessor = se;
            }

            if(!it.hasNext()) {
                // No other element present.
                return secondAccessor;
            }

            // Get first element.
            ModeStructureSnapshot.ElementSnapshot first = (ModeStructureSnapshot.ElementSnapshot)it.next();
            ElementAccessor firstAccessor = createVisibleAccessor(first);

            double firstSplitWeight = ((Double)visibleChild2refinedWeight.get(first)).doubleValue();
            
            // Find nextVisible weights.
            double nextVisibleWeights = 0D;
            List anotherReversedChildren = new ArrayList(visibleChildren);
            Collections.reverse(anotherReversedChildren);
            for(Iterator it2 = anotherReversedChildren.iterator(); it2.hasNext(); ) {
                ModeStructureSnapshot.ElementSnapshot next = (ModeStructureSnapshot.ElementSnapshot)it2.next();
                if(next == first) {
                    break;
                }
                nextVisibleWeights += ((Double)visibleChild2refinedWeight.get(next)).doubleValue();
            }
            
            if(DEBUG) {
                debugLog(""); // NOI18N
                debugLog("Computing split"); // NOI18N
                debugLog("firstSplitWeight=" + firstSplitWeight); // NOI18N
                debugLog("nextVisibleWeigths=" + nextVisibleWeights); // NOI18N
            }
            
            // Compute split position.
            double splitPosition = firstSplitWeight/(firstSplitWeight + nextVisibleWeights);
            if(DEBUG) {
                debugLog("splitPosition=" + splitPosition); // NOI18N
            }

            se = new ModeStructureAccessorImpl.SplitAccessorImpl(
                first.getOriginator(), splitSnapshot, orientation, splitPosition, firstAccessor, secondAccessor, splitSnapshot.getResizeWeight());
        }

        return se;
    }

    
    public static boolean computeSplitWeights(double location, SplitAccessor splitAccessor,
    ElementAccessor firstAccessor, ElementAccessor secondAccessor, ControllerHandler controllerHandler) {
        ModeStructureSnapshot.SplitSnapshot splitSnapshot = (ModeStructureSnapshot.SplitSnapshot)splitAccessor.getSnapshot();

        if(splitSnapshot == null) {
            return false;
        }
        
        ArrayList visibleChildren = new ArrayList( splitSnapshot.getVisibleChildSnapshots() );
        
        ElementSnapshot first = firstAccessor.getSnapshot();
        ElementSnapshot second = secondAccessor.getSnapshot();

        // XXX #36696 If it is 'nested-split' find the real element.
        if(first == splitSnapshot) {
            first = ((SplitAccessor)firstAccessor).getSecond().getSnapshot();
        }
        
        // Find the corresponding nodes in the split.
        while(first != null && !visibleChildren.contains(first)) {
            first = first.getParent();
        }
        if(first == null) {
            // Is not in this split.
            return false;
        }

        // XXX #36696 If it is 'nested-split' find the real element.
        if(second == splitSnapshot) {
            second = ((SplitAccessor)secondAccessor).getFirst().getSnapshot();
        }

        while(second != null && !visibleChildren.contains(second)) {
            second = second.getParent();
        }
        if(second == null) {
            // Is not in this split.
            return false;
        }
        
        double currentFirstWeight = splitSnapshot.getChildSnapshotSplitWeight(first);
        double currentSecondWeight = splitSnapshot.getChildSnapshotSplitWeight(second);
        
        double remainingWeights = 0.0D;
        ArrayList allChildren = new ArrayList( splitSnapshot.getChildSnapshots() );
        for( int i=allChildren.indexOf( second ); i<allChildren.size(); i++ ) {
            ElementSnapshot es = (ElementSnapshot)allChildren.get( i );
            double childWeight = splitSnapshot.getChildSnapshotSplitWeight(es);
            remainingWeights += childWeight;
        }
        
        double invisibleFirstWeight = 0.0D;
        int indexOfFirst = allChildren.indexOf( first );
        for( int i=0; i<indexOfFirst; i++ ) {
            ElementSnapshot es = (ElementSnapshot)allChildren.get( i );
            if( !visibleChildren.contains( es ) ) {
                double childWeight = splitSnapshot.getChildSnapshotSplitWeight(es);
                invisibleFirstWeight += childWeight;
            }
        }
        
        double firstWeight = location * (currentFirstWeight + invisibleFirstWeight + remainingWeights);

        double delta = currentFirstWeight - firstWeight;

        double secondWeight = currentSecondWeight + delta;

        //just a safeguard so that the model doesn't fall apart
        if( secondWeight <= 0.0 )
            secondWeight = 0.001;
        if( secondWeight >= 1.0 )
            secondWeight = 0.999;

        if( firstWeight <= 0.0 )
            firstWeight = 0.001;
        if( firstWeight >= 1.0 )
            firstWeight = 0.999;

        controllerHandler.userChangedSplit(first.getOriginator(), firstWeight, second.getOriginator(), secondWeight);

        return true;
    }

    private static void debugLog(String message) {
        Debug.log(ViewHelper.class, message);
    }

}

