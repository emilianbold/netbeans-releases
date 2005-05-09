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


import java.lang.ref.WeakReference;
import org.netbeans.core.windows.*;
import org.openide.ErrorManager;
import org.openide.windows.TopComponent;

import java.awt.*;
import java.util.*;
import java.util.List;


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
    
    // XXX
    private String projectName;
    
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
    
    private final Object LOCK_PROJECT_NAME = new Object();

    
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
    

    
    // XXX
    public void addModeToSide(ModeImpl mode, ModeImpl attachMode, String side) {
        synchronized(LOCK_MODES) {
            modesSubModel.addModeToSide(mode, attachMode, side);
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

    public void addSlidingMode(ModeImpl mode, String side) {
        synchronized(LOCK_MODES) {
            modesSubModel.addModeSliding(mode, side);
        }
    }
    
    
    /** Removes mode. */
    public void removeMode(ModeImpl mode) {
        synchronized(LOCK_MODES) {
            modesSubModel.removeMode(mode);
        }
    }

    /** Sets active mode. */
    private WeakReference lastActiveMode = null;
    public void setActiveMode(ModeImpl activeMode) {
        if (lastActiveMode != null && lastActiveMode.get() == activeMode) {
            return;
        } else {
            lastActiveMode = new WeakReference(activeMode);
        }
        synchronized(LOCK_MODES) {
            boolean success = modesSubModel.setActiveMode(activeMode);
            if (success) {
                updateSlidingSelections(activeMode);
            }
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
    
    // XXX
    public void setProjectName(String projectName) {
        synchronized(LOCK_PROJECT_NAME) {
            this.projectName = projectName;
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
    
    public String getSlidingModeConstraints(ModeImpl mode) {
        synchronized(LOCK_MODES) {
            return modesSubModel.getSlidingModeConstraints(mode);
        }
    }
    
    public ModeImpl getSlidingMode(String side) {
        synchronized(LOCK_MODES) {
            return modesSubModel.getSlidingMode(side);
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
    
    // XXX
    public String getProjectName() {
        synchronized(LOCK_PROJECT_NAME) {
            return this.projectName;
        }
    }
    /////////////////////////////////////
    // Accessor methods <<
    /////////////////////////////////////
    
    
    ///////////////////
    // Mode specific >>
    public void createModeModel(ModeImpl mode, String name, int state, int kind, boolean permanent) {
        synchronized(mode2model) {
            ModeModel mm;
            mm = new DefaultModeModel(name, state, kind, permanent);
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
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.setState(state);
        }
    }
    
    /** Sets bounds. */
    public void setModeBounds(ModeImpl mode, Rectangle bounds) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.setBounds(bounds);
        }
    }
    
    /** Sets frame state. */
    public void setModeFrameState(ModeImpl mode, int frameState) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.setFrameState(frameState);
        }
    } 
    
    /** Sets seleted TopComponent. */
    public void setModeSelectedTopComponent(ModeImpl mode, TopComponent selected) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.setSelectedTopComponent(selected);
        }
    }
    
    /** Adds opened TopComponent. */
    public void addModeOpenedTopComponent(ModeImpl mode, TopComponent tc) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.addOpenedTopComponent(tc);
        }
    }

    /** Inserts opened TopComponent. */
    public void insertModeOpenedTopComponent(ModeImpl mode, TopComponent tc, int index) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.insertOpenedTopComponent(tc, index);
        }
    }
    
    /** Adds closed TopComponent. */
    public void addModeClosedTopComponent(ModeImpl mode, TopComponent tc) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.addClosedTopComponent(tc);
        }
    }
    
    // XXX
    public void addModeUnloadedTopComponent(ModeImpl mode, String tcID) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.addUnloadedTopComponent(tcID);
        }
    }
    
    // XXX
    public void setModeUnloadedSelectedTopComponent(ModeImpl mode, String tcID) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.setUnloadedSelectedTopComponent(tcID);
        }
    }
    
    /** */
    public void removeModeTopComponent(ModeImpl mode, TopComponent tc) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.removeTopComponent(tc);
        }
    }
    
    // XXX
    public void removeModeClosedTopComponentID(ModeImpl mode, String tcID) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.removeClosedTopComponentID(tcID);
        }
    }

    /**
     * @param mode - sliding mode
     */
    
    public void setModeTopComponentPreviousConstraints(ModeImpl mode, TopComponent tc, SplitConstraint[] constraints) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.setTopComponentPreviousConstraints(tc, constraints);
        }
    }
    
    /**
     * @param mode - sliding mode
     * @param previousMode - the original mode.
     */
    public void setModeTopComponentPreviousMode(ModeImpl mode, TopComponent tc, ModeImpl previousMode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.setTopComponentPreviousMode(tc, previousMode);
        }
    }
    
    // Accessors
    /** Gets programatic name of mode. */
    public String getModeName(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getName();
        } else {
            return null;
        }
    }
    
    /** Gets bounds. */
    public Rectangle getModeBounds(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getBounds();
        } else {
            return null;
        }
    }
    
    public Rectangle getModeBoundsSeparatedHelp(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getBoundsSeparatedHelp();
        } else {
            return null;
        }
    }
    
    /** Gets state. */
    public int getModeState(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getState();
        } else {
            return -1;
        }
    }
    
    /** Gets kind. */
    public int getModeKind(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getKind();
        } else {
            return -1;
        }
    }

    /** Gets side. */
    public String getModeSide(ModeImpl mode) {
        String side = modesSubModel.getSlidingModeConstraints(mode);
        return side;
    }
    
    /** Gets frame state. */
    public int getModeFrameState(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getFrameState();
        } else {
            return -1;
        }
    }
    
    /** Gets used defined. */
    public boolean isModePermanent(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.isPermanent();
        } else {
            return false;
        }
    }
    
    /** Indicates whether the mode is empty. */
    public boolean isModeEmpty(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.isEmpty();
        } else {
            return false;
        }
    }
    
    /** Indicates whether the mode contains the TopComponent. */
    public boolean containsModeTopComponent(ModeImpl mode, TopComponent tc) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.containsTopComponent(tc);
        } else {
            return false;
        }
    }
    
    /** Gets selected TopComponent. */
    public TopComponent getModeSelectedTopComponent(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getSelectedTopComponent();
        } else {
            return null;
        }
    }
    
    /** Gets list of top components. */
    public List getModeTopComponents(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getTopComponents();
        } else {
            return Collections.EMPTY_LIST;
        }
    }
    
    /** Gets list of top components. */
    public List getModeOpenedTopComponents(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getOpenedTopComponents();
        } else {
            return Collections.EMPTY_LIST;
        }
    }
    
    // XXX
    public List getModeOpenedTopComponentsIDs(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getOpenedTopComponentsIDs();
        } else {
            return Collections.EMPTY_LIST;
        }
    }
    
    public List getModeClosedTopComponentsIDs(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getClosedTopComponentsIDs();
        } else {
            return Collections.EMPTY_LIST;
        }
    }
    
    public List getModeTopComponentsIDs(ModeImpl mode) {
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            return modeModel.getTopComponentsIDs();
        } else {
            return Collections.EMPTY_LIST;
        }
    }
    
    public SplitConstraint[] getModeTopComponentPreviousConstraints(ModeImpl mode, TopComponent tc) {
        ModeModel modeModel = getModelForMode(mode);
        return modeModel == null ? null : modeModel.getTopComponentPreviousConstraints(tc);
    }
    
    public ModeImpl getModeTopComponentPreviousMode(ModeImpl mode, TopComponent tc) {
        ModeModel modeModel = getModelForMode(mode);
        return modeModel == null ? null : modeModel.getTopComponentPreviousMode(tc);
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
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.getName();
        } else {
            return null;
        }
    }
    
    public void openGroup(TopComponentGroupImpl tcGroup, Collection openedTopComponents, Collection openedBeforeTopComponents) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            groupModel.open(openedTopComponents, openedBeforeTopComponents);
        }
    }
    
    public void closeGroup(TopComponentGroupImpl tcGroup) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            groupModel.close();
        } 
    }
    
    public boolean isGroupOpened(TopComponentGroupImpl tcGroup) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.isOpened();
        } else {
            return false;
        }
    }
    
    public Set getGroupTopComponents(TopComponentGroupImpl tcGroup) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.getTopComponents();
        } else {
            return Collections.EMPTY_SET;
        }
    }
    
    public Set getGroupOpenedTopComponents(TopComponentGroupImpl tcGroup) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.getOpenedTopComponents();
        } else {
            return Collections.EMPTY_SET;
        }
    }
    
    public Set getGroupOpenedBeforeTopComponents(TopComponentGroupImpl tcGroup) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.getOpenedBeforeTopComponents();
        } else {
            return Collections.EMPTY_SET;
        }
    }
    
    public Set getGroupOpeningTopComponents(TopComponentGroupImpl tcGroup) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.getOpeningTopComponents();
        } else {
            return Collections.EMPTY_SET;
        }
    }
    
    public Set getGroupClosingTopComponents(TopComponentGroupImpl tcGroup) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.getClosingTopComponents();
        } else {
            return Collections.EMPTY_SET;
        }
    }

    public boolean addGroupUnloadedTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.addUnloadedTopComponent(tcID);
        } else {
            return false;
        }
    }
    
    public boolean removeGroupUnloadedTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.removeUnloadedTopComponent(tcID);
        } else {
            return false;
        }
    }
    
    public boolean addGroupOpeningTopComponent(TopComponentGroupImpl tcGroup, TopComponent tc) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.addOpeningTopComponent(tc);
        } else {
            return false;
        }
    }
    
    public boolean removeGroupOpeningTopComponent(TopComponentGroupImpl tcGroup, TopComponent tc) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.removeOpeningTopComponent(tc);
        } else {
            return false;
        }
    }
    
    public boolean addGroupUnloadedOpeningTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.addUnloadedOpeningTopComponent(tcID);
        } else {
            return false;
        }
    }
    
    public boolean removeGroupUnloadedOpeningTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.removeUnloadedOpeningTopComponent(tcID);
        } else {
            return false;
        }
    }
    
    public boolean addGroupUnloadedClosingTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.addUnloadedClosingTopComponent(tcID);
        } else {
            return false;
        }
    }
    public boolean removeGroupUnloadedClosingTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.removeUnloadedClosingTopComponent(tcID);
        } else {
            return false;
        }
    }
    // XXX
    public boolean addGroupUnloadedOpenedTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.addUnloadedOpenedTopComponent(tcID);
        } else {
            return false;
        }
    }
    
    // XXX>>
    public Set getGroupTopComponentsIDs(TopComponentGroupImpl tcGroup) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.getTopComponentsIDs();
        } else {
            return Collections.EMPTY_SET;
        }
    }
    
    public Set getGroupOpeningSetIDs(TopComponentGroupImpl tcGroup) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.getOpeningSetIDs();
        } else {
            return Collections.EMPTY_SET;
        }
    }
    
    public Set getGroupClosingSetIDs(TopComponentGroupImpl tcGroup) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.getClosingSetIDs();
        } else {
            return Collections.EMPTY_SET;
        }
    }
    
    public Set getGroupOpenedTopComponentsIDs(TopComponentGroupImpl tcGroup) {
        TopComponentGroupModel groupModel = getModelForGroup(tcGroup);
        if(groupModel != null) {
            return groupModel.getOpenedTopComponentsIDs();
        } else {
            return Collections.EMPTY_SET;
        }
    }
    // XXX<<
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
        ModeModel modeModel = getModelForMode(mode);
        if(modeModel != null) {
            modeModel.setBoundsSeparatedHelp(bounds);
        }
    }
    
    public void setSplitWeights( ModelElement[] snapshots, double[] splitWeights ) {
        synchronized(LOCK_MODES) {
            modesSubModel.setSplitWeights(snapshots, splitWeights);
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
        wsms.setEditorAreaFrameState(getEditorAreaFrameState());
        wsms.setMainWindowFrameStateJoined(getMainWindowFrameStateJoined());
        wsms.setMainWindowFrameStateSeparated(getMainWindowFrameStateSeparated());
        wsms.setToolbarConfigurationName(getToolbarConfigName());
        wsms.setProjectName(getProjectName());
        return wsms;
    }

    /** Creates modes snapshot.. */
    private ModeStructureSnapshot createModeStructureSnapshot() {
        ModeStructureSnapshot.ElementSnapshot splitRoot;
        Set separateModes;
        Set slidingModes;
        synchronized(LOCK_MODES) {
            splitRoot = modesSubModel.createSplitSnapshot();
            separateModes = modesSubModel.createSeparateModeSnapshots();
            slidingModes = modesSubModel.createSlidingModeSnapshots();
        }
        
        ModeStructureSnapshot ms =  new ModeStructureSnapshot(splitRoot, separateModes, slidingModes);
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
    
    /** Keeps selected components of sliding modes in sync with given current
     * active mode. Sliding mode can have non-null selection (=slide) only if
     * it is active mode as well
     */   
    private void updateSlidingSelections (ModeImpl curActive) {
        Set slidingModes = modesSubModel.getSlidingModes();
        ModeImpl curSliding = null;
        for (Iterator iter = slidingModes.iterator(); iter.hasNext(); ) {
            curSliding = (ModeImpl)iter.next();
            if (!curSliding.equals(curActive)) {
                setModeSelectedTopComponent(curSliding, null);
            }
        }
    }

    
}

