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


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.ModeStructureSnapshot;
import org.netbeans.core.windows.SplitConstraint;


/**
 * Sub-model which keeps modes data strucute, i.e. as split and also separate 
 * ones. Note, it keeps editor ones in another field of split model, for better manipulation.
 * See UI spec about editor area and editor/view component types.
 * Note: this instances aren't thread safe, thus they has to be properly synchronized.
 * The client has to synchronize it.
 * (see in DefaultModel, where it is only place supposed to be used.)
 *
 * @author  Peter Zavadsky
 */
final class ModesSubModel {

    /** Associated parent model. */
    private final Model parentModel;
    
    /** Set of modes. */
    private final Set modes = new HashSet(10);

    /** Represents split model of modes, also contains special editor area. */
    private final EditorSplitSubModel editorSplitSubModel;

    /** Active mode. */
    private ModeImpl activeMode;
    /** Maximized mode. */
    private ModeImpl maximizedMode;
     
    
    /** Creates a new instance of ModesModel */
    public ModesSubModel(Model parentModel) {
        this.parentModel = parentModel;

        this.editorSplitSubModel = new EditorSplitSubModel(parentModel, new SplitSubModel(parentModel));
    }

    
    public void setEditorAreaConstraints(SplitConstraint[] editorAreaConstraints) {
        editorSplitSubModel.setEditorNodeConstraints(editorAreaConstraints);
    }
    
    public SplitConstraint[] getModelElementConstraints(ModelElement element) {
        return editorSplitSubModel.getModelElementConstraints(element);
    }
    
    public SplitConstraint[] getEditorAreaConstraints() {
        return editorSplitSubModel.getEditorNodeConstraints();
    }
    
    public SplitConstraint[] getModeConstraints(ModeImpl mode) {
        if(mode.getKind() == Constants.MODE_KIND_EDITOR) {
            return editorSplitSubModel.getEditorArea().getModeConstraints(mode);
        } else {
            return editorSplitSubModel.getModeConstraints(mode);
        }
    }

    public boolean addMode(ModeImpl mode, SplitConstraint[] constraints) {
        return addMode(mode, constraints, false);
    }
    
    public boolean addMode(ModeImpl mode, SplitConstraint[] constraints, boolean adjustToAllWeights) {
        if(modes.contains(mode)) {
            return false;
        }

        boolean result;
        if(mode.getKind() == Constants.MODE_KIND_EDITOR) {
            result = editorSplitSubModel.getEditorArea().addMode(mode, constraints, adjustToAllWeights);
        } else {
            result = editorSplitSubModel.addMode(mode, constraints, adjustToAllWeights);
        }

        if(result) {
            modes.add(mode);
        }
        return result;
    }
    
    // XXX
    public boolean addModeToSide(ModeImpl mode, ModeImpl attachMode, String side) {
        if(modes.contains(mode)) {
            return false;
        }

        boolean result;
        // XXX PENDING
        if(mode.getKind() == Constants.MODE_KIND_EDITOR) {
            result = editorSplitSubModel.getEditorArea().addModeToSide(mode, attachMode, side);
        } else {
            result = editorSplitSubModel.addModeToSide(mode, attachMode, side);
        }

        if(result) {
            modes.add(mode);
        }
        return result;
    }
    
    // XXX
    public boolean addModeBetween(ModeImpl mode, ModelElement firstElement, ModelElement secondElement) {
        if(modes.contains(mode)) {
            return false;
        }

        boolean result;
        // XXX PENDING
        if(mode.getKind() == Constants.MODE_KIND_EDITOR) {
            result = editorSplitSubModel.getEditorArea().addModeBetween(mode, firstElement, secondElement);
        } else {
            result = editorSplitSubModel.addModeBetween(mode, firstElement, secondElement);
        }

        if(result) {
            modes.add(mode);
        }
        return result;
    }
    
    // XXX
    public boolean addModeAround(ModeImpl mode, String side) {
        if(modes.contains(mode)) {
            return false;
        }

        boolean result;
        // XXX PENDING
        if(mode.getKind() == Constants.MODE_KIND_EDITOR) {
            result = false;
        } else {
            result = editorSplitSubModel.addModeAround(mode, side);
        }

        if(result) {
            modes.add(mode);
        }
        return result;
    }
    
    // XXX
    public boolean addModeAroundEditor(ModeImpl mode, String side) {
        if(modes.contains(mode)) {
            return false;
        }

        boolean result;
        // XXX PENDING
        if(mode.getKind() == Constants.MODE_KIND_EDITOR) {
            result = false;
        } else {
            result = editorSplitSubModel.addModeAroundEditor(mode, side);
        }

        if(result) {
            modes.add(mode);
        }
        return result;
    }

    
    public boolean removeMode(ModeImpl mode) {
        modes.remove(mode);
        if(mode.getKind() == Constants.MODE_KIND_EDITOR) {
            return editorSplitSubModel.getEditorArea().removeMode(mode);
        } else {
            return editorSplitSubModel.removeMode(mode);
        }
    }
    
    /** Sets active mode. */
    public boolean setActiveMode(ModeImpl activeMode) {
        if(modes.contains(activeMode)) {
            this.activeMode = activeMode;
            return true;
        }
        
        return false;
    }
    
    /** Gets acitve mode. */
    public ModeImpl getActiveMode() {
        return this.activeMode;
    }
    
    /** Sets maximized mode. */
    public boolean setMaximizedMode(ModeImpl maximizedMode) {
        if(maximizedMode == null || modes.contains(maximizedMode)) {
            this.maximizedMode = maximizedMode;
            return true;
        }
        
        return false;
    }
    
    /** Gets maximized mode. */
    public ModeImpl getMaximizedMode() {
        return this.maximizedMode;
    }

    public Set getModes() {
        return new HashSet(modes);
    }
    
    public void setSplitWeights(ModelElement firstElement, double firstSplitWeight,
    ModelElement secondElement, double secondSplitWeight) {
        editorSplitSubModel.setSplitWeights(firstElement, firstSplitWeight,
            secondElement, secondSplitWeight);
    }
    
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode()) // NOI18N
            + "\n" + editorSplitSubModel; // NOI18N
    }

    /////////////////////////////////////////////
    // used when creating snapshot of this model.
    public ModeStructureSnapshot.ElementSnapshot createSplitSnapshot() {
        return editorSplitSubModel.createSplitSnapshot();
    }

    /** Set of mode element snapshots. */
    public Set createSeparateModeSnapshots() {
        Set s = new HashSet();
        
        // In joined mode no separate modes are allowed.
        if(parentModel.getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
            return s;
        }
        
        s.addAll(editorSplitSubModel.createSeparateSnapshots());
        
        return s;
    }
    ////////////////////////////////////////////

    
    //////////////////////////////
    // Controller udpates >>
    
    public ModeImpl getModeForOriginator(ModelElement originator) {
        ModeImpl mode = editorSplitSubModel.getModeForOriginator(originator);
        
        if(modes.contains(mode)) {
            return mode;
        } else {
            return null;
        }
    }
    
    
    // Controller updates <<
    ///////////////////////////////
}

