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
import org.netbeans.core.windows.model.ModelElement;



/**
 * This class converts snapshot to accessor structure, which is a 'model'
 * of view (GUI) structure window system has to display to user.
 * It reflects the specific view implementation (the difference from snapshot)
 * e.g. merging of split panes with the same orientation, accomodates for split
 * panes with only one visible child etc.
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
                //the split has only one visible child, so create an accessor for this child
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
        
        ArrayList children = new ArrayList( visibleChildren.size() );
        ArrayList weights = new ArrayList( visibleChildren.size() );
        
        int index = 0;
        for( Iterator i=visibleChildren.iterator(); i.hasNext(); index++ ) {
            ModeStructureSnapshot.ElementSnapshot child = (ModeStructureSnapshot.ElementSnapshot)i.next();
            ElementAccessor childAccessor = createVisibleAccessor( child );
            double weight = splitSnapshot.getChildSnapshotSplitWeight( child );
            //double weight = getSplitWeight( splitSnapshot, child );
            if( childAccessor instanceof SplitAccessor 
                && ((SplitAccessor)childAccessor).getOrientation() == splitSnapshot.getOrientation() ) {
                //merge nested splits with the same orientation into one big split
                //e.g. (A | B | C) where B is a nested split (X | Y | Z) 
                //will be merged into -> (A | X | Y | Z | C)
                SplitAccessor subSplit = (SplitAccessor)childAccessor;
                ElementAccessor[] childrenToMerge = subSplit.getChildren();
                double[] weightsToMerge = subSplit.getSplitWeights();
                for( int j=0; j<childrenToMerge.length; j++ ) {
                    children.add( childrenToMerge[j] );
                    weights.add( new Double( weightsToMerge[j] * weight ) );
                }
            } else {
                children.add( childAccessor );
                weights.add( new Double( weight ) );
            }
        }
        
        ElementAccessor[] childrenAccessors = new ElementAccessor[children.size()];
        double[] splitWeights = new double[children.size()];
        for( int i=0; i<children.size(); i++ ) {
            ElementAccessor ea = (ElementAccessor)children.get( i );
            Double weight = (Double)weights.get( i );
            childrenAccessors[i] = ea;
            splitWeights[i] = weight.doubleValue();
        }
        
        return new ModeStructureAccessorImpl.SplitAccessorImpl( 
                splitSnapshot.getOriginator(), 
                splitSnapshot, 
                splitSnapshot.getOrientation(), 
                splitWeights,
                childrenAccessors, 
                splitSnapshot.getResizeWeight());
    }
    
    /**
     * Update the model with new split weights when user moved a splitter bar or
     * when the main window has been resized.
     *
     * @param splitAccessor The split parent that has been modified.
     * @param children Split children.
     * @param splitWeights New split weights.
     * @param controllerHandler
     *
     */
    public static void setSplitWeights(SplitAccessor splitAccessor,
        ElementAccessor[] children, double[] splitWeights, ControllerHandler controllerHandler) {
        
        ModeStructureSnapshot.SplitSnapshot splitSnapshot = (ModeStructureSnapshot.SplitSnapshot)splitAccessor.getSnapshot();

        if(splitSnapshot == null) {
            return;
        }
        
        ModelElement[] elements = new ModelElement[ children.length ];
        for( int i=0; i<children.length; i++ ) {
            //the split child may belong to another splitter that has only one visible child
            //so we must set new split weight for its actual parent
            //e.g. the 'S' = (A | X | C) and X's parent is B -> (X | Y | Z) but Y and Z are not visible
            //so instead of setting split weight of X in splitter B we must set split weight of B in 'S'
            //see also method createVisibleAccessor()
            ModeStructureSnapshot.ElementSnapshot snapshot = findVisibleSplitSnapshot( children[i].getSnapshot() );
            elements[ i ] = snapshot.getOriginator();
            //if this splitter has been merged with a nested splitter with the same orientation
            //then the nested split child split weight must be corrected according to its parent
            splitWeights[ i ] = correctNestedSplitWeight( children[i].getSnapshot().getParent(), splitWeights[i] );
        }

        controllerHandler.userChangedSplit( elements, splitWeights );
    }
    
    /**
     * Recalculate the given child split weight if the given split is nested in a parent
     * split with the same orientation.
     * e.g. If this split X is (A | B) with weights 0.5 and 0.5 and is nested in a parent
     * split (X | Y | Z) with split weights 0.5, 0.25 and 0.25 then the accessors
     * look like this (A | B | Y | Z) with split weights 0.25, 0.25, 0.25, and 0.25
     * If the new split weight for A being corrected is 0.3 then the corrected value will be (0.3 / 0.5)
     *
     * @return New split weight corrected as described above or the original value if the
     * split does not have a parent split with the same orientation.
     */
    private static double correctNestedSplitWeight( ModeStructureSnapshot.SplitSnapshot split, double splitWeight ) {
        ModeStructureSnapshot.SplitSnapshot parentSplit = split.getParent();
        if( null != parentSplit && parentSplit.getOrientation() == split.getOrientation() ) {
            double parentSplitWeight = parentSplit.getChildSnapshotSplitWeight( split );
            if( parentSplit.getVisibleChildSnapshots().size() > 1 && parentSplitWeight > 0.0 )
                splitWeight /= parentSplitWeight;
            
            return correctNestedSplitWeight( parentSplit, splitWeight );
        }
        return splitWeight;
    }
    
    /**
     * Find the topmost split parent of the given snapshot that is visible in split hierarchy 
     * (i.e. has at least two visible children).
     */
    private static ModeStructureSnapshot.ElementSnapshot findVisibleSplitSnapshot( ModeStructureSnapshot.ElementSnapshot snapshot ) {
        ModeStructureSnapshot.SplitSnapshot parent = snapshot.getParent();
        if( null != parent ) {
            List visibleChildren = parent.getVisibleChildSnapshots();
            if( visibleChildren.size() == 1 ) {
                return findVisibleSplitSnapshot( parent );
            }
        }
        return snapshot;
    }

    private static void debugLog(String message) {
        Debug.log(ViewHelper.class, message);
    }

}

