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


import java.awt.Frame;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.ModeStructureSnapshot;
import org.netbeans.core.windows.TopComponentGroupImpl;
import org.netbeans.core.windows.SplitConstraint;
import org.netbeans.core.windows.WindowSystemSnapshot;

import org.openide.ErrorManager;
import org.openide.windows.TopComponent;


/**
 *
 * @author  Peter Zavadsky
 */
final class DefaultModel implements Model {

    /** ModeImpl to ModeModel. */
    private final Map mode2model = new WeakHashMap(10);
    /** TopComponentGroup to TopComponentGroupModel. */
    private final Map group2model = new WeakHashMap(10);
    
    /** Whether the current winsys is visible on the screen.
     * 'The most important' property of all winsys. */
    private boolean visible = false;

    /** Bounds of main window when the editor area state is joined (tiled). */
    private final Rectangle mainWindowBoundsJoined = new Rectangle();
    /** Bounds of main window when the editor area state is separated. */
    private final Rectangle mainWindowBoundsSeparated = new Rectangle();
    /** Keeps initial value of separated bounds (helper non-serializable variable). */
    private final Rectangle mainWindowBoundsSeparatedHelp = new Rectangle();

    /** */
    private int mainWindowFrameStateJoined = Frame.NORMAL;
    /** */
    private int mainWindowFrameStateSeparated = Frame.NORMAL;
    
    /** State of editor area. 1 = joined, 2 = separated */
    private int editorAreaState = Constants.EDITOR_AREA_JOINED;
    /** Bounds of editor area. */
    private final Rectangle editorAreaBounds = new Rectangle();
    /** Keeps initial value of editor area (helper non-serializable variable). */
    private final Rectangle editorAreaBoundsHelp = new Rectangle();
    /** */
    private int editorAreaFrameState = Frame.NORMAL;
    /** Name of toolbars configuration. */
    private String toolbarConfigName = "Standard"; // NOI18N

    
    /** Modes structure. */
    private final ModesSubModel modesSubModel = new ModesSubModel(this);

    /** Set of TopComponentGroup's. */
    private final Set topComponentGroups = new HashSet(5);
    
    // Locks.
    /** Lock for visible property. */
    private final Object LOCK_VISIBLE = new Object();
    /** Lock for mainWindowBounds property -> joined(tiled) state. */
    private final Object LOCK_MAIN_WINDOW_BOUNDS_JOINED = new Object();
    /** Lock for mainWindowBounds property -> separated state. */
    private final Object LOCK_MAIN_WINDOW_BOUNDS_SEPARATED = new Object();
    /** */
    private final Object LOCK_MAIN_WINDOW_BOUNDS_SEPARATED_HELP = new Object();
    /** */
    private final Object LOCK_MAIN_WINDOW_FRAME_STATE_JOINED = new Object();
    /** */
    private final Object LOCK_MAIN_WINDOW_FRAME_STATE_SEPARATED = new Object();
    /** Lock for editor area state. */
    private final Object LOCK_EDITOR_AREA_STATE = new Object();
    /** */
    private final Object LOCK_EDITOR_AREA_FRAME_STATE = new Object();
    /** Lock for editor area bounds. */
    private final Object LOCK_EDITOR_AREA_BOUNDS = new Object();
    /** */
    private final Object LOCK_EDITOR_AREA_BOUNDS_HELP = new Object();
    /** Lock for toolbarConfigName property. */
    private final Object LOCK_TOOLBAR_CONFIG = new Object();
    /** Locks for modes sub model. */
    private final Object LOCK_MODES = new Object();
    /** Lock for topComponentGroups property. */
    private final Object LOCK_TOPCOMPONENT_GROUPS = new Object();

    
    public DefaultModel() {
    }
    

    /////////////////////////////////////
    // Mutator methods >>
    /////////////////////////////////////
    /** Sets visibility status. */
    public void setVisible(boolean visible) {
        synchronized(LOCK_VISIBLE) {
            this.visible = visible;
        }
    }
    
    /** Setter for mainWindowBoundsJoined property. */
    public void setMainWindowBoundsJoined(Rectangle mainWindowBoundsJoined) {
        if(mainWindowBoundsJoined == null) {
            return;
        }
        
        synchronized(LOCK_MAIN_WINDOW_BOUNDS_JOINED) {
            this.mainWindowBoundsJoined.setBounds(mainWindowBoundsJoined);
        }
    }
    
    /** Setter for mainWindowBoundsSeparated property. */
    public void setMainWindowBoundsSeparated(Rectangle mainWindowBoundsSeparated) {
        if(mainWindowBoundsSeparated == null) {
            return;
        }
        
        synchronized(LOCK_MAIN_WINDOW_BOUNDS_SEPARATED) {
            this.mainWindowBoundsSeparated.setBounds(mainWindowBoundsSeparated);
        }
    }
    
    /** Sets frame state of main window when editor area is in tiled(joined) state. */
    public void setMainWindowFrameStateJoined(int frameState) {
        synchronized(LOCK_MAIN_WINDOW_FRAME_STATE_JOINED) {
            this.mainWindowFrameStateJoined = frameState;
        }
    }
    
    /** Sets frame state of main window when editor area is in separated state. */
    public void setMainWindowFrameStateSeparated(int frameState) {
        synchronized(LOCK_MAIN_WINDOW_FRAME_STATE_SEPARATED) {
            this.mainWindowFrameStateSeparated = frameState;
        }
    }

    /** Setter of editorAreaBounds property. */
    public void setEditorAreaBounds(Rectangle editorAreaBounds) {
        if(editorAreaBounds == null) {
            return;
        }
        
        synchronized(LOCK_EDITOR_AREA_BOUNDS) {
            this.editorAreaBounds.setBounds(editorAreaBounds);
        }
    }
 
    /** Setter of editorAreaState property. */
    public void setEditorAreaState(int editorAreaState) {
        synchronized(LOCK_EDITOR_AREA_STATE) {
            this.editorAreaState = editorAreaState;
        }
    }

    /** */
    public void setEditorAreaFrameState(int frameState) {
        synchronized(LOCK_EDITOR_AREA_FRAME_STATE) {
            this.editorAreaFrameState = frameState;
        }
    }
    
    /** Sets editor area constraints. */
    public void setEditorAreaConstraints(SplitConstraint[] editorAreaConstraints) {
        synchronized(LOCK_MODES) {
            modesSubModel.setEditorAreaConstraints(editorAreaConstraints);
        }
    }

    public void setModeConstraints(ModeImpl mode, SplitConstraint[] constraints) {
        synchronized(LOCK_MODES) {
            // PENDING create changeMode method?
            modesSubModel.removeMode(mode);
            modesSubModel.addMode(mode, constraints);
        }
    }
    
    
    /** Adds mode. */ 
    public void addMode(ModeImpl mode, SplitConstraint[] constraints) {
        synchronized(LOCK_MODES) {
            modesSubModel.addMode(mode, constraints);
        }
    }
    
    public void addMode(ModeImpl mode, SplitConstraint[] constraints, boolean adjustToAllWeights) {
        synchronized(LOCK_MODES) {
            modesSubModel.addMode(mode, constraints, adjustToAllWeights);
        }
    }
    
    // XXX
    public void addModeToSide(ModeImpl mode, ModeImpl attachMode, String side) {
        synchronized(LOCK_MODES) {
            modesSubModel.addModeToSide(mode, attachMode, side);
        }
    }
    
    // XXX
    public void addModeBetween(ModeImpl mode, ModelElement firstElement, ModelElement secondElement) {
        synchronized(LOCK_MODES) {
            modesSubModel.addModeBetween(mode, firstElement, secondElement);
        }
    }
    
    // XXX
    public void addModeAround(ModeImpl mode, String side) {
        synchronized(LOCK_MODES) {
            modesSubModel.addModeAround(mode, side);
        }
    }
    
    // XXX
    public void addModeAroundEditor(ModeImpl mode, String side) {
        synchronized(LOCK_MODES) {
            modesSubModel.addModeAroundEditor(mode, side);
        }
    }
    
    /** Removes mode. */
    public void removeMode(ModeImpl mode) {
        synchronized(LOCK_MODES) {
            modesSubModel.removeMode(mode);
        }
    }

    /** Sets active mode. */
    public void setActiveMode(ModeImpl activeMode) {
        synchronized(LOCK_MODES) {
            modesSubModel.setActiveMode(activeMode);
        }
    }
    
    /** Sets maximized mode. */
    public void setMaximizedMode(ModeImpl maximizedMode) {
        synchronized(LOCK_MODES) {
            modesSubModel.setMaximizedMode(maximizedMode);
        }
    }

    /** Setter for toolbarConfigName property. */
    public void setToolbarConfigName(String toolbarConfigName) {
        synchronized(LOCK_TOOLBAR_CONFIG) {
            this.toolbarConfigName = toolbarConfigName;
        }
    }

    
    public void addTopComponentGroup(TopComponentGroupImpl tcGroup) {
        synchronized(LOCK_TOPCOMPONENT_GROUPS) {
            topComponentGroups.add(tcGroup);
        }
    }
    
    public void removeTopComponentGroup(TopComponentGroupImpl tcGroup) {
        synchronized(LOCK_TOPCOMPONENT_GROUPS) {
            topComponentGroups.remove(tcGroup);
        }
    }
    /////////////////////////////////////
    // Mutator methods <<
    /////////////////////////////////////

    /////////////////////////////////////
    // Accessor methods >>
    /////////////////////////////////////
    /** Gets visibility status. */
    public boolean isVisible() {
        synchronized(LOCK_VISIBLE) {
            return this.visible;
        }
    }

    /** Getter for mainWindowBoundsJoined property. */
    public Rectangle getMainWindowBoundsJoined() {
        synchronized(LOCK_MAIN_WINDOW_BOUNDS_JOINED) {
            return (Rectangle)mainWindowBoundsJoined.clone();
        }
    }
    
    /** Getter for mainWindowBoundsSeparated property. */
    public Rectangle getMainWindowBoundsSeparated() {
        synchronized(LOCK_MAIN_WINDOW_BOUNDS_SEPARATED) {
            return (Rectangle)mainWindowBoundsSeparated.clone();
        }
    }
    
    public Rectangle getMainWindowBoundsSeparatedHelp() {
        synchronized(LOCK_MAIN_WINDOW_BOUNDS_SEPARATED_HELP) {
            return (Rectangle)mainWindowBoundsSeparatedHelp.clone();
        }
    }
    
    /** Gets frame state of main window when editor area is in tiled(joined) state. */
    public int getMainWindowFrameStateJoined() {
        synchronized(LOCK_MAIN_WINDOW_FRAME_STATE_JOINED) {
            return mainWindowFrameStateJoined;
        }
    }
    
    /** Gets frame state of main window when editor area is in separated state. */
    public int getMainWindowFrameStateSeparated() {
        synchronized(LOCK_MAIN_WINDOW_FRAME_STATE_SEPARATED) {
            return mainWindowFrameStateSeparated;
        }
    }
    
    /** Getter of editorAreaState property. */
    public int getEditorAreaState() {
        synchronized(LOCK_EDITOR_AREA_STATE) {
            return this.editorAreaState;
        }
    }
    
    /** */
    public int getEditorAreaFrameState() {
        synchronized(LOCK_EDITOR_AREA_FRAME_STATE) {
            return this.editorAreaFrameState;
        }
    }
    
    /** Getter of editorAreaBounds property. */
    public Rectangle getEditorAreaBounds() {
        synchronized(LOCK_EDITOR_AREA_BOUNDS) {
            return (Rectangle)this.editorAreaBounds.clone();
        }
    }
    
    public Rectangle getEditorAreaBoundsHelp() {
        synchronized(LOCK_EDITOR_AREA_BOUNDS_HELP) {
            return (Rectangle)this.editorAreaBoundsHelp.clone();
        }
    }

    /** Gets editor area constraints. */
    public SplitConstraint[] getEditorAreaConstraints() {
        synchronized(LOCK_MODES) {
            return modesSubModel.getEditorAreaConstraints();
        }
    }

    /** Gets set of modes. */
    public Set getModes() {
        synchronized(LOCK_MODES) {
            return modesSubModel.getModes();
        }
    }
    
    public SplitConstraint[] getModeConstraints(ModeImpl mode) {
        synchronized(LOCK_MODES) {
            return modesSubModel.getModeConstraints(mode);
        }
    }
    
    public SplitConstraint[] getModelElementConstraints(ModelElement element) {
        synchronized(LOCK_MODES) {
            return modesSubModel.getModelElementConstraints(element);
        }
    }
    
    /** Gets active mode. */
    public ModeImpl getActiveMode() {
        synchronized(LOCK_MODES) {
            return modesSubModel.getActiveMode();
        }
    }
    
    /** Gets maximized mode. */
    public ModeImpl getMaximizedMode() {
        synchronized(LOCK_MODES) {
            return modesSubModel.getMaximizedMode();
        }
    }
    
    /** Getter for toolbarConfigName property. */
    public String getToolbarConfigName() {
        synchronized(LOCK_TOOLBAR_CONFIG) {
            return this.toolbarConfigName;
        }
    }
    /////////////////////////////////////
    // Accessor methods <<
    /////////////////////////////////////
    
    
    ///////////////////
    // Mode specific >>
    public void createModeModel(ModeImpl mode, String name, int state, int kind, boolean permanent) {
        synchronized(mode2model) {
            ModeModel mm = new DefaultModeModel(name, state, kind, permanent);
            mode2model.put(mode, mm);
        }
    }

    private ModeModel getModelForMode(ModeImpl mode) {
        synchronized(mode2model) {
            return (ModeModel)mode2model.get(mode);
        }
    }
    
    // Mutators
    /** Sets state. */
    public void setModeState(ModeImpl mode, int state) {
        getModelForMode(mode).setState(state);
    }
    
    /** Sets bounds. */
    public void setModeBounds(ModeImpl mode, Rectangle bounds) {
        getModelForMode(mode).setBounds(bounds);
    }
    
    /** Sets frame state. */
    public void setModeFrameState(ModeImpl mode, int frameState) {
        getModelForMode(mode).setFrameState(frameState);
    } 
    
    /** Sets seleted TopComponent. */
    public void setModeSelectedTopComponent(ModeImpl mode, TopComponent selected) {
        getModelForMode(mode).setSelectedTopComponent(selected);
    }
    
    /** Adds opened TopComponent. */
    public void addModeOpenedTopComponent(ModeImpl mode, TopComponent tc) {
        getModelForMode(mode).addOpenedTopComponent(tc);
    }

    /** Inserts opened TopComponent. */
    public void insertModeOpenedTopComponent(ModeImpl mode, TopComponent tc, int index) {
        getModelForMode(mode).insertOpenedTopComponent(tc, index);
    }
    
    /** Adds closed TopComponent. */
    public void addModeClosedTopComponent(ModeImpl mode, TopComponent tc) {
        getModelForMode(mode).addClosedTopComponent(tc);
    }
    
    // XXX
    public void addModeUnloadedTopComponent(ModeImpl mode, String tcID) {
        getModelForMode(mode).addUnloadedTopComponent(tcID);
    }
    
    // XXX
    public void setModeUnloadedSelectedTopComponent(ModeImpl mode, String tcID) {
        getModelForMode(mode).setUnloadedSelectedTopComponent(tcID);
    }
    
    /** */
    public void removeModeTopComponent(ModeImpl mode, TopComponent tc) {
        getModelForMode(mode).removeTopComponent(tc);
    }
    
    // Accessors
    /** Gets programatic name of mode. */
    public String getModeName(ModeImpl mode) {
        return getModelForMode(mode).getName();
    }
    
    /** Gets bounds. */
    public Rectangle getModeBounds(ModeImpl mode) {
        return getModelForMode(mode).getBounds();
    }
    
    public Rectangle getModeBoundsSeparatedHelp(ModeImpl mode) {
        return getModelForMode(mode).getBoundsSeparatedHelp();
    }
    
    /** Gets state. */
    public int getModeState(ModeImpl mode) {
        return getModelForMode(mode).getState();
    }
    
    /** Gets kind. */
    public int getModeKind(ModeImpl mode) {
        return getModelForMode(mode).getKind();
    }
    
    /** Gets frame state. */
    public int getModeFrameState(ModeImpl mode) {
        return getModelForMode(mode).getFrameState();
    }
    
    /** Gets used defined. */
    public boolean isModePermanent(ModeImpl mode) {
        return getModelForMode(mode).isPermanent();
    }
    
    /** Indicates whether the mode is empty. */
    public boolean isModeEmpty(ModeImpl mode) {
        return getModelForMode(mode).isEmpty();
    }
    
    /** Indicates whether the mode contains the TopComponent. */
    public boolean containsModeTopComponent(ModeImpl mode, TopComponent tc) {
        return getModelForMode(mode).containsTopComponent(tc);
    }
    
    /** Gets selected TopComponent. */
    public TopComponent getModeSelectedTopComponent(ModeImpl mode) {
        return getModelForMode(mode).getSelectedTopComponent();
    }
    
    /** Gets list of top components. */
    public List getModeTopComponents(ModeImpl mode) {
        return getModelForMode(mode).getTopComponents();
    }
    
    /** Gets list of top components. */
    public List getModeOpenedTopComponents(ModeImpl mode) {
        return getModelForMode(mode).getOpenedTopComponents();
    }
    // End of mode specific.

    
    ////////////////////////////////////
    // TopComponentGroup specific >>
    public void createGroupModel(TopComponentGroupImpl tcGroup, String name, boolean opened) {
        synchronized(group2model) {
            TopComponentGroupModel tcgm = new DefaultTopComponentGroupModel(name, opened);
            group2model.put(tcGroup, tcgm);
        }
    }

    private TopComponentGroupModel getModelForGroup(TopComponentGroupImpl tcGroup) {
        synchronized(group2model) {
            return (TopComponentGroupModel)group2model.get(tcGroup);
        }
    }

    
    public Set getTopComponentGroups() {
        synchronized(LOCK_TOPCOMPONENT_GROUPS) {
            return new HashSet(topComponentGroups);
        }
    }

    /** Gets programatic name of mode. */
    public String getGroupName(TopComponentGroupImpl tcGroup) {
        return getModelForGroup(tcGroup).getName();
    }
    
    public void openGroup(TopComponentGroupImpl tcGroup, Collection openedTopComponents) {
        getModelForGroup(tcGroup).open(openedTopComponents);
    }
    
    public void closeGroup(TopComponentGroupImpl tcGroup) {
        getModelForGroup(tcGroup).close();
    }
    
    public boolean isGroupOpened(TopComponentGroupImpl tcGroup) {
        return getModelForGroup(tcGroup).isOpened();
    }
    
    public Set getGroupTopComponents(TopComponentGroupImpl tcGroup) {
        return getModelForGroup(tcGroup).getTopComponents();
    }
    
    public Set getGroupOpenedTopComponents(TopComponentGroupImpl tcGroup) {
        return getModelForGroup(tcGroup).getOpenedTopComponents();
    }
    
    public Set getGroupOpeningTopComponents(TopComponentGroupImpl tcGroup) {
        return getModelForGroup(tcGroup).getOpeningTopComponents();
    }
    
    public Set getGroupClosingTopComponents(TopComponentGroupImpl tcGroup) {
        return getModelForGroup(tcGroup).getClosingTopComponents();
    }

    public boolean addGroupUnloadedTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        return getModelForGroup(tcGroup).addUnloadedTopComponent(tcID);
    }
    
    public boolean removeGroupUnloadedTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        return getModelForGroup(tcGroup).removeUnloadedTopComponent(tcID);
    }
    
    public boolean addGroupOpeningTopComponent(TopComponentGroupImpl tcGroup, TopComponent tc) {
        return getModelForGroup(tcGroup).addOpeningTopComponent(tc);
    }
    
    public boolean removeGroupOpeningTopComponent(TopComponentGroupImpl tcGroup, TopComponent tc) {
        return getModelForGroup(tcGroup).removeOpeningTopComponent(tc);
    }
    
    public boolean addGroupUnloadedOpeningTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        return getModelForGroup(tcGroup).addUnloadedOpeningTopComponent(tcID);
    }
    
    public boolean removeGroupUnloadedOpeningTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        return getModelForGroup(tcGroup).removeUnloadedOpeningTopComponent(tcID);
    }
    
    public boolean addGroupUnloadedClosingTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        return getModelForGroup(tcGroup).addUnloadedClosingTopComponent(tcID);
    }
    public boolean removeGroupUnloadedClosingTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        return getModelForGroup(tcGroup).removeUnloadedClosingTopComponent(tcID);
    }
    // XXX
    public boolean addGroupUnloadedOpenedTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        return getModelForGroup(tcGroup).addUnloadedOpenedTopComponent(tcID);
    }
    // TopComponentGroup specific <<
    ////////////////////////////////////
    
    
    /////////////////////////
    // Controller updates >>
    public void setActiveModeForOriginator(ModelElement originator) {
        synchronized(LOCK_MODES) {
            ModeImpl mode = modesSubModel.getModeForOriginator(originator);
            setActiveMode(mode);
        }
    }
    
    public void setModeSelectedTopComponentForOriginator(ModelElement originator, TopComponent tc) {
        ModeImpl mode;
        synchronized(LOCK_MODES) {
            mode = modesSubModel.getModeForOriginator(originator);
        }
        
        setModeSelectedTopComponent(mode, tc);
    }
    
    public void setMainWindowBoundsUserSeparatedHelp(Rectangle bounds) {
        if(bounds == null) {
            return;
        }
        
        synchronized(LOCK_MAIN_WINDOW_BOUNDS_SEPARATED_HELP) {
            this.mainWindowBoundsSeparatedHelp.setBounds(bounds);
        }
    }
    
    public void setEditorAreaBoundsUserHelp(Rectangle bounds) {
        if(bounds == null) {
            return;
        }
        
        synchronized(LOCK_EDITOR_AREA_BOUNDS_HELP) {
            this.editorAreaBoundsHelp.setBounds(bounds);
        }
    }
    
    public void setModeBoundsSeparatedHelp(ModeImpl mode, Rectangle bounds) {
        getModelForMode(mode).setBoundsSeparatedHelp(bounds);
    }
    
    public void setSplitWeights(ModelElement firstElement, double firstSplitWeight,
    ModelElement secondElement, double secondSplitWeight) {
        synchronized(LOCK_MODES) {
            modesSubModel.setSplitWeights(firstElement, firstSplitWeight, secondElement, secondSplitWeight);
        }
    }
    // Controller updates <<
    /////////////////////////


    public WindowSystemSnapshot createWindowSystemSnapshot() {
        WindowSystemSnapshot wsms = new WindowSystemSnapshot();

        // PENDING
        ModeStructureSnapshot mss = createModeStructureSnapshot();
        wsms.setModeStructureSnapshot(mss);
        
        ModeImpl activeMode = getActiveMode();
        wsms.setActiveModeSnapshot(activeMode == null ? null : mss.findModeSnapshot(activeMode.getName()));
        
        ModeImpl maximizedMode = getMaximizedMode();
        wsms.setMaximizedModeSnapshot(maximizedMode == null ? null : mss.findModeSnapshot(maximizedMode.getName()));

        wsms.setMainWindowBoundsJoined(getMainWindowBoundsJoined());
        wsms.setMainWindowBoundsSeparated(getMainWindowBoundsSeparated());
        wsms.setEditorAreaBounds(getEditorAreaBounds());
        wsms.setEditorAreaState(getEditorAreaState());
        wsms.setMainWindowFrameStateJoined(getMainWindowFrameStateJoined());
        wsms.setMainWindowFrameStateSeparated(getMainWindowFrameStateSeparated());
        wsms.setToolbarConfigurationName(getToolbarConfigName());
        return wsms;
    }

    /** Creates modes snapshot.. */
    private ModeStructureSnapshot createModeStructureSnapshot() {
        ModeStructureSnapshot.ElementSnapshot splitRoot;
        Set separateModes;
        synchronized(LOCK_MODES) {
            splitRoot = modesSubModel.createSplitSnapshot();
            separateModes = modesSubModel.createSeparateModeSnapshots();
        }
        
        ModeStructureSnapshot ms =  new ModeStructureSnapshot(splitRoot, separateModes);
        return ms;
    }
    ///////////////////////////////////////////////////
    
    /** Checks whether the mode isn't null.  */
    private static boolean validateAddingMode(ModeImpl mode) {
        if(mode == null) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                new NullPointerException("Not allowed null mode")); // NOI18N
            return false;
        }
        
        return true;
    }

}

