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


import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.SplitConstraint;
import org.netbeans.core.windows.TopComponentGroupImpl;
import org.netbeans.core.windows.WindowSystemSnapshot;
import org.openide.windows.TopComponent;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Set;


/**
 * Represents model of this window system implementation.
 *
 * @author  Peter Zavadsky
 */
public interface Model {

    ////////////////////////////////
    // Global (the highest) level >>
    /////////////////////////////
    // Mutators (global level) >>
    /** Sets visibility status. */
    public void setVisible(boolean visible);
    /** Sets main window bounds when the editor area is in joined(tiled) state. */
    public void setMainWindowBoundsJoined(Rectangle bounds);
    /** Sets main window bounds when the editor area is in separated state. */
    public void setMainWindowBoundsSeparated(Rectangle bounds);
    /** Sets frame state of main window when editor area is in tiled(joined) state. */
    public void setMainWindowFrameStateJoined(int frameState);
    /** Sets frame state of main window when editor area is in separated state. */
    public void setMainWindowFrameStateSeparated(int frameState);
    /** Sets editor area state. */
    public void setEditorAreaState(int editorAreaState);
    /** Sets editor area frame state when in separate state. */
    public void setEditorAreaFrameState(int frameState);
    /** Sets editor area bounds. */
    public void setEditorAreaBounds(Rectangle editorAreaBounds);
    /** Sets editor area constraints. */
    public void setEditorAreaConstraints(SplitConstraint[] editorAreaConstraints);
    /** Sets toolbar configuration name. */
    public void setToolbarConfigName(String toolbarConfigName);
    /** Sets active mode. */
    public void setActiveMode(ModeImpl mode);
    /** Sets maximized mode. */
    public void setMaximizedMode(ModeImpl maximizedMode);
    /** Adds mode. */ 
    public void addMode(ModeImpl mode, SplitConstraint[] constraints);
    /** Adds mode. */
    // XXX
    public void addModeToSide(ModeImpl mode, ModeImpl attachMode, String side);
    // XXX
    /** Adds mode between two mode elements ('into split') */
    public void addModeBetween(ModeImpl mode, ModelElement firstElement, ModelElement secondElement);
    // XXX
    /** Adds mode around (attaches from side). */
    public void addModeAround(ModeImpl mode, String side);
    // XXX
    /** Adds mode around editor area (attaches from side). */
    public void addModeAroundEditor(ModeImpl mode, String side);
    /** Removes mode. */
    public void removeMode(ModeImpl mode);
    /** Sets mode constraints. */
    public void setModeConstraints(ModeImpl mode, SplitConstraint[] constraints);
    /** Adds top component group. */
    public void addTopComponentGroup(TopComponentGroupImpl tcGroup);
    /** Removes top component group. */
    public void removeTopComponentGroup(TopComponentGroupImpl tcGroup);
    // XXX 
    public void setProjectName(String projectName);
    /** Adds sliding mode into specific side */ 
    public void addSlidingMode(ModeImpl mode, String side);
    // Mutators (global level) <<
    /////////////////////////////

    //////////////////////////////   
    // Accessors (global level) >>
    /** Gets visibility status. */
    public boolean isVisible();
    /** Gets main window bounds for the joined (tiled) editor area state. */
    public Rectangle getMainWindowBoundsJoined(); 
    /** Gets main window bounds for the separated editor area state. */
    public Rectangle getMainWindowBoundsSeparated(); 
    /** Gets frame state of main window when editor area is in tiled(joined) state. */
    public int getMainWindowFrameStateJoined();
    /** Gets frame state of main window when editor area is in separated state. */
    public int getMainWindowFrameStateSeparated();
    /** Gets main window bounds for separated state (helper initial value). */
    public Rectangle getMainWindowBoundsSeparatedHelp();
    /** Gets editor area state. */
    public int getEditorAreaState();
    /** Gets editor area frame state when in serparate state. */
    public int getEditorAreaFrameState();
    /** Gets editor area bounds. */
    public Rectangle getEditorAreaBounds();
    /** Gets editor area bounds for separated state (helper initial value). */
    public Rectangle getEditorAreaBoundsHelp();
    /** Gets editor area constraints. */
    public SplitConstraint[] getEditorAreaConstraints();
    /** Gets toolbar configuration name. */
    public String getToolbarConfigName();
    /** Gets active mode. */
    public ModeImpl getActiveMode();
    /** Gets maximized mode. */
    public ModeImpl getMaximizedMode();
    /** Gets set of modes. */
    public Set getModes();
    /** Gets mode constraints. */
    public SplitConstraint[] getModeConstraints(ModeImpl mode);
    // XXX
    /** Gets model element constraints. */
    public SplitConstraint[] getModelElementConstraints(ModelElement element);
    /** Gets constraints (its side) for sliding mode */
    public String getSlidingModeConstraints(ModeImpl mode);
    /** Gets constraints (its side) for sliding mode */
    public ModeImpl getSlidingMode(String side);
    
    // Accessors (global level) >>
    //////////////////////////////   
    // Global (the highest) level <<
    ////////////////////////////////

    
    ////////////////
    // Mode level >>
    ///////////////////////////
    // Mutators (mode level) >>
    /** Sets state. */
    public void setModeState(ModeImpl mode, int state);
    /** Sets bounds. */
    public void setModeBounds(ModeImpl mode, Rectangle bounds);
    /** Sets frame state. */
    public void setModeFrameState(ModeImpl mode, int frameState);
    /** Sets selected TopComponent. */
    public void setModeSelectedTopComponent(ModeImpl mode, TopComponent selected);
    /** Adds opened TopComponent. */
    public void addModeOpenedTopComponent(ModeImpl mode, TopComponent tc);
    /** Inserts opened TopComponent. */
    public void insertModeOpenedTopComponent(ModeImpl mode, TopComponent tc, int index);
    /** Adds closed TopComponent. */
    public void addModeClosedTopComponent(ModeImpl mode, TopComponent tc);
    // XXX
    /** Adds unloaded TopComponent. */
    public void addModeUnloadedTopComponent(ModeImpl mode, String tcID);
    // XXX
    public void setModeUnloadedSelectedTopComponent(ModeImpl mode, String tcID);
    /** */
    public void removeModeTopComponent(ModeImpl mode, TopComponent tc);
    // XXX
    public void removeModeClosedTopComponentID(ModeImpl mode, String tcID);

    // Info about previous top component context, used by sliding kind of modes
    
    /** Sets information of previous mode top component was in. */
    public void setModeTopComponentPreviousMode(ModeImpl mode, TopComponent tc, ModeImpl previousMode);
    /** Sets information of previous constraints of mode top component was in. */
    public void setModeTopComponentPreviousConstraints(ModeImpl mode, TopComponent tc, SplitConstraint[] constraints);
    
    
    // Mutators (mode level) <<
    ///////////////////////////

    ////////////////////////////
    // Accessors (mode level) >>
    /** Gets programatic name of mode. */
    public String getModeName(ModeImpl mode);
    /** Gets bounds. */
    public Rectangle getModeBounds(ModeImpl mode);
    /** Gets mode bounds for separated state (helper initial value). */
    public Rectangle getModeBoundsSeparatedHelp(ModeImpl mode);
    /** Gets state. */
    public int getModeState(ModeImpl mode);
    /** Gets kind. */
    public int getModeKind(ModeImpl mode);
    /** Gets side. */
    public String getModeSide(ModeImpl mode);
    /** Gets frame state. */
    public int getModeFrameState(ModeImpl mode);
    /** Gets whether it is permanent. */
    public boolean isModePermanent(ModeImpl mode);
    /** */
    public boolean isModeEmpty(ModeImpl mode);
    /** Indicates whether the mode contains the TopComponent. */
    public boolean containsModeTopComponent(ModeImpl mode, TopComponent tc);
    /** Gets selected TopComponent. */
    public TopComponent getModeSelectedTopComponent(ModeImpl mode);
    /** Gets list of top components in this workspace. */
    public List getModeTopComponents(ModeImpl mode);
    /** Gets list of top components in this workspace. */
    public List getModeOpenedTopComponents(ModeImpl mode);
    // XXX
    public List getModeOpenedTopComponentsIDs(ModeImpl mode);
    public List getModeClosedTopComponentsIDs(ModeImpl mode);
    public List getModeTopComponentsIDs(ModeImpl mode);
    
    // Info about previous top component context, used by sliding kind of modes
    
    public ModeImpl getModeTopComponentPreviousMode(ModeImpl mode, TopComponent tc);
    public SplitConstraint[] getModeTopComponentPreviousConstraints(ModeImpl mode, TopComponent tc);
    
    // Accessors (mode level) <<
    ////////////////////////////
    // Mode level <<
    ////////////////


    ///////////////////////////
    // TopComponentGroup level >>
    public Set getTopComponentGroups();

    /** Gets programatic name of goup. */
    public String getGroupName(TopComponentGroupImpl tcGroup);

    public void openGroup(TopComponentGroupImpl tcGroup, Collection openedTopComponents, Collection openedBeforeTopComponenets);
    public void closeGroup(TopComponentGroupImpl tcGroup);
    public boolean isGroupOpened(TopComponentGroupImpl tcGroup);
    
    public Set getGroupTopComponents(TopComponentGroupImpl tcGroup);
    
    public Set getGroupOpenedTopComponents(TopComponentGroupImpl tcGroup);
    public Set getGroupOpenedBeforeTopComponents(TopComponentGroupImpl tcGroup);
    
    public Set getGroupOpeningTopComponents(TopComponentGroupImpl tcGroup);
    public Set getGroupClosingTopComponents(TopComponentGroupImpl tcGroup);

    public boolean addGroupUnloadedTopComponent(TopComponentGroupImpl tcGroup, String tcID);
    public boolean removeGroupUnloadedTopComponent(TopComponentGroupImpl tcGroup, String tcID);
    
    public boolean addGroupOpeningTopComponent(TopComponentGroupImpl tcGroup, TopComponent tc);
    public boolean removeGroupOpeningTopComponent(TopComponentGroupImpl tcGroup, TopComponent tc);

    public boolean addGroupUnloadedOpeningTopComponent(TopComponentGroupImpl tcGroup, String tcID);
    public boolean removeGroupUnloadedOpeningTopComponent(TopComponentGroupImpl tcGroup, String tcID);
    
    public boolean addGroupUnloadedClosingTopComponent(TopComponentGroupImpl tcGroup, String tcID);
    public boolean removeGroupUnloadedClosingTopComponent(TopComponentGroupImpl tcGroup, String tcID);
    
    // XXX
    public boolean addGroupUnloadedOpenedTopComponent(TopComponentGroupImpl tcGroup, String tcID);
    
    // XXX>>
    public Set getGroupTopComponentsIDs(TopComponentGroupImpl tcGroup);
    public Set getGroupOpeningSetIDs(TopComponentGroupImpl tcGroup);
    public Set getGroupClosingSetIDs(TopComponentGroupImpl tcGroup);
    public Set getGroupOpenedTopComponentsIDs(TopComponentGroupImpl tcGroup);
    // XXX<<
    // TopComponentGroup level <<
    ///////////////////////////

    //////////////////////////////////////    
    // Other methods, creating sub-models.
    /** Creates mode sub model. */
    public void createModeModel(ModeImpl mode, String name, int state, int kind, boolean permanent);
    /** Creates top component group subg  model. */
    public void createGroupModel(TopComponentGroupImpl tcGroup, String name, boolean opened);

    /////////////////////////
    // snapshot
    public WindowSystemSnapshot createWindowSystemSnapshot();

    
    ////////////////////////
    // controller updates >>
    // Helper values.
    public void setMainWindowBoundsUserSeparatedHelp(Rectangle bounds);
    public void setEditorAreaBoundsUserHelp(Rectangle bounds);
    public void setModeBoundsSeparatedHelp(ModeImpl mode, Rectangle bounds);
    
    public void setSplitWeights(ModelElement firstElement, double firstSplitWeight,
    ModelElement secondElement, double secondSplitWeight);
    // controller updates <<
    ////////////////////////

    
}

