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

package org.netbeans.core.windows;


import javax.swing.SwingUtilities;
import org.netbeans.core.windows.model.Model;
import org.netbeans.core.windows.model.ModelElement;
import org.netbeans.core.windows.model.ModelFactory;
import org.netbeans.core.windows.view.ControllerHandler;
import org.netbeans.core.windows.view.View;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import java.awt.*;
import java.util.*;
import java.util.List;
import org.netbeans.core.windows.persistence.PersistenceManager;


/**
 * This class is a central unit of window system. It controls communication
 * flows to model, to view, from controller and from API calls.
 *
 * @author Peter Zavadsky
 */
final class Central implements ControllerHandler {
    
    /** Model of window system. */
    private final Model model = ModelFactory.createWindowSystemModel();
    
    /** Helper class for managing requests to view. */
    private final ViewRequestor viewRequestor = new ViewRequestor(this);
    
    
    /** Constructor. */
    public Central() {
    }

    
    public void topComponentRequestAttention (ModeImpl mode, TopComponent tc) {
        String modeName = getModeName(mode);
        viewRequestor.scheduleRequest (
            new ViewRequest(modeName, View.TOPCOMPONENT_REQUEST_ATTENTION, tc, tc));
    }
    
    public void topComponentCancelRequestAttention (ModeImpl mode, TopComponent tc) {
        String modeName = getModeName(mode);
        viewRequestor.scheduleRequest (
            new ViewRequest(modeName, View.TOPCOMPONENT_CANCEL_REQUEST_ATTENTION, tc, tc));
    }    
    
    /////////////////////
    // Mutators >>
    /** Sets visible or invisible window system and requests view accordingly. */
    public void setVisible(boolean visible) {
        if(isVisible() == visible) {
            return;
        }
        
        model.setVisible(visible);
        
        viewRequestor.scheduleRequest(
            new ViewRequest(null, View.CHANGE_VISIBILITY_CHANGED, null, Boolean.valueOf(visible)));
    }
    
    /** Sets main window bounds (joined[tiled] state) into model and requests view (if needed). */
    public void setMainWindowBoundsJoined(Rectangle mainWindowBoundsJoined) {
        if(mainWindowBoundsJoined == null) {
            return;
        }
        
        Rectangle old = getMainWindowBoundsJoined();
        if(old.equals(mainWindowBoundsJoined)) {
            return;
        }
        
        model.setMainWindowBoundsJoined(mainWindowBoundsJoined);
        
        if(isVisible()) {
            viewRequestor.scheduleRequest(
                new ViewRequest(null, View.CHANGE_MAIN_WINDOW_BOUNDS_JOINED_CHANGED,
                    old, mainWindowBoundsJoined));
        }
    }
    
    /** Sets main window bounds (separated state) into model and requests view (if needed). */
    public void setMainWindowBoundsSeparated(Rectangle mainWindowBoundsSeparated) {
        if(mainWindowBoundsSeparated == null) {
            return;
        }
        
        Rectangle old = getMainWindowBoundsSeparated();
        if(old.equals(mainWindowBoundsSeparated)) {
            return;
        }
        
        model.setMainWindowBoundsSeparated(mainWindowBoundsSeparated);
        
        if(isVisible()) {
            viewRequestor.scheduleRequest(
                new ViewRequest(null, View.CHANGE_MAIN_WINDOW_BOUNDS_SEPARATED_CHANGED,
                    old, mainWindowBoundsSeparated));
        }
    }
    
    public void setMainWindowFrameStateJoined(int frameState) {
        int old = getMainWindowFrameStateJoined();
        if(old == frameState) {
            return;
        }
        
        model.setMainWindowFrameStateJoined(frameState);
        
        if(isVisible()) {
            viewRequestor.scheduleRequest(new ViewRequest(null, View.CHANGE_MAIN_WINDOW_FRAME_STATE_JOINED_CHANGED,
                new Integer(old), new Integer(frameState)));
        }
    }
    
    public void setMainWindowFrameStateSeparated(int frameState) {
        int old = getMainWindowFrameStateSeparated();
        if(old == frameState) {
            return;
        }
        
        model.setMainWindowFrameStateSeparated(frameState);
        
        if(isVisible()) {
            viewRequestor.scheduleRequest(new ViewRequest(null, View.CHANGE_MAIN_WINDOW_FRAME_STATE_SEPARATED_CHANGED,
                new Integer(old), new Integer(frameState)));
        }
    }
    
    /** Sets active mode into model and requests view (if needed). */
    public void setActiveMode(final ModeImpl activeMode) {
        final ModeImpl old = getActiveMode();
        if(activeMode == old) {
            // kind of workaround to the scenario when a window slides out automatically
            // and user clicks in the currently active mode, not allow to exit in such case and fire changes to
            // force the slided-out window to disappear.
            ModeImpl impl = model.getSlidingMode(Constants.BOTTOM);
            boolean bottom = (impl == null || impl.getSelectedTopComponent() == null);
            impl = model.getSlidingMode(Constants.LEFT);
            boolean left = (impl == null || impl.getSelectedTopComponent() == null);
            impl = model.getSlidingMode(Constants.RIGHT);
            boolean right = (impl == null || impl.getSelectedTopComponent() == null);
            if (bottom && left && right) {
                return;
            }
        }
        
        model.setActiveMode(activeMode);
        
        if(isVisible()) {
            viewRequestor.scheduleRequest(
                new ViewRequest(null, View.CHANGE_ACTIVE_MODE_CHANGED,
                    old, activeMode));
        }

        
        
        WindowManagerImpl.getInstance().doFirePropertyChange(
            WindowManagerImpl.PROP_ACTIVE_MODE, old, activeMode);
        
        // Notify registry.
        // active mode can be null, Active mode info is stored in winsys config (system layer) and modes in
        // project layer, that can cause out of synch state when switching projects.
        // all subsequent calls should handle the null value correctly.
        if (activeMode != null) {
            WindowManagerImpl.notifyRegistryTopComponentActivated(
            activeMode.getSelectedTopComponent());
        } else {
            WindowManagerImpl.notifyRegistryTopComponentActivated(null);
        }
    }

    /** Sets editor area bounds into model and requests view (if needed). */
    public void setEditorAreaBounds(Rectangle editorAreaBounds) {
        if(editorAreaBounds == null) {
            return;
        }
        
        Rectangle old = getEditorAreaBounds();
        if(old.equals(editorAreaBounds)) {
            return;
        }
        
        model.setEditorAreaBounds(editorAreaBounds);
        
        if(isVisible()) {
            viewRequestor.scheduleRequest(
                new ViewRequest(null, View.CHANGE_EDITOR_AREA_BOUNDS_CHANGED,
                            old, editorAreaBounds));
        }
    }

    /** Sets editor area constraints into model and requests view (if needed). */
    public void setEditorAreaConstraints(SplitConstraint[] editorAreaConstraints) {
        SplitConstraint[] old = getEditorAreaConstraints();
        if(Arrays.equals(old, editorAreaConstraints)) {
            return;
        }
        
        model.setEditorAreaConstraints(editorAreaConstraints);

        if(isVisible()) {
            viewRequestor.scheduleRequest(
                new ViewRequest(null, View.CHANGE_EDITOR_AREA_CONSTRAINTS_CHANGED,
                                old, editorAreaConstraints));
        }
    }

    /** Sets editor area state into model and requests view (if needed). */
    public void setEditorAreaState(int editorAreaState) {
        int old = getEditorAreaState();
        if(editorAreaState == old) {
            return;
        }
        
        int requiredState = editorAreaState == Constants.EDITOR_AREA_JOINED
                                                ? Constants.MODE_STATE_JOINED
                                                : Constants.MODE_STATE_SEPARATED;
                                                
        for(Iterator it = getModes().iterator(); it.hasNext(); ) {
            ModeImpl mode = (ModeImpl)it.next();
            if(mode.getKind() == Constants.MODE_KIND_VIEW
            && mode.getState() != requiredState) {
                model.setModeState(mode, requiredState);
                // Adjust bounds if necessary.
                if(editorAreaState == Constants.EDITOR_AREA_SEPARATED) {
                    Rectangle bounds = model.getModeBounds(mode);
                    if(bounds.isEmpty()) {
                        model.setModeBounds(mode, model.getModeBoundsSeparatedHelp(mode));
                    }
                }
            }
            // when switching to SDI, undock sliding windows
            // #51992 -start
            if (mode.getKind() == Constants.MODE_KIND_SLIDING && editorAreaState == Constants.EDITOR_AREA_SEPARATED) {
                TopComponent[] tcs = mode.getTopComponents();
                for (int i = 0; i < tcs.length;i++) {
                    ModeImpl targetMode = model.getModeTopComponentPreviousMode(mode, tcs[i]);
                    if ((targetMode == null) || !model.getModes().contains(targetMode)) {
                        SplitConstraint[] constraints = model.getModeTopComponentPreviousConstraints(mode, tcs[i]);
                        constraints = constraints == null ? new SplitConstraint[0] : constraints;
                        // create mode to dock topcomponent back into
                        targetMode = WindowManagerImpl.getInstance().createModeImpl(
                            ModeImpl.getUnusedModeName(), Constants.MODE_KIND_VIEW, false);
                        model.setModeState(targetMode, requiredState);
                        model.addMode(targetMode, constraints);
                    }
                    moveTopComponentsIntoMode(targetMode, new TopComponent[] { tcs[i] } );                    
                }
            }
            // #51992 - end
        }
                                                
        if(editorAreaState == Constants.EDITOR_AREA_SEPARATED) {
            Rectangle editorAreaBounds = model.getEditorAreaBounds();
            // Adjust bounds if necessary.
            if(editorAreaBounds.isEmpty()) {
                model.setEditorAreaBounds(model.getEditorAreaBoundsHelp());
            }
            
            // Adjust bounds if necessary.
            Rectangle mainWindowBoundsSeparated = model.getMainWindowBoundsSeparated();
            if(mainWindowBoundsSeparated.isEmpty()) {
                model.setMainWindowBoundsSeparated(model.getMainWindowBoundsSeparatedHelp());
            }
        }
        
        model.setEditorAreaState(editorAreaState);
        
        if(isVisible()) {
            viewRequestor.scheduleRequest(
                new ViewRequest(null, View.CHANGE_EDITOR_AREA_STATE_CHANGED,
                        new Integer(old), new Integer(editorAreaState)));
        }
        
        WindowManagerImpl.getInstance().doFirePropertyChange(
            WindowManagerImpl.PROP_EDITOR_AREA_STATE, new Integer(old), new Integer(editorAreaState));
    }

    public void setEditorAreaFrameState(int frameState) {
        int old = getEditorAreaFrameState();
        if(old == frameState) {
            return;
        }
        model.setEditorAreaFrameState(frameState);
        
        if(isVisible()) {
            viewRequestor.scheduleRequest(new ViewRequest(null, View.CHANGE_EDITOR_AREA_FRAME_STATE_CHANGED,
                new Integer(old), new Integer(frameState)));
        }
    }
    
    /** Sets maximized mode into model and requests view (if needed). */
    public void setMaximizedMode(ModeImpl maximizedMode) {
        ModeImpl old = getMaximizedMode();
        if(maximizedMode == old) {
            return;
        }

        model.setMaximizedMode(maximizedMode);
        
        if(isVisible()) {
            viewRequestor.scheduleRequest(
                new ViewRequest(null, View.CHANGE_MAXIMIZED_MODE_CHANGED,
                    old, maximizedMode));
        }
        
        WindowManagerImpl.getInstance().doFirePropertyChange(
            WindowManagerImpl.PROP_MAXIMIZED_MODE, old, maximizedMode);
    }

    /** Sets constraints for mode into model and requests view (if needed). */
    public void setModeConstraints(ModeImpl mode, SplitConstraint[] modeConstraints) {
        SplitConstraint[] old = getModeConstraints(mode);
        if(Arrays.equals(modeConstraints, old)) {
            return;
        }
        
        model.setModeConstraints(mode, modeConstraints);
        
        if(isVisible()) {
            viewRequestor.scheduleRequest(
                new ViewRequest(null, View.CHANGE_MODE_CONSTRAINTS_CHANGED,
                        old, modeConstraints));
        }
    }

    /** Adds mode into model and requests view (if needed). */
    public void addMode(ModeImpl mode, SplitConstraint[] modeConstraints) {
        // PENDING which one to use?
//        if(getModes().contains(mode)) {
//            return;
//        }
        SplitConstraint[] old = getModeConstraints(mode);
        if((modeConstraints == null && old == null)
        || modeConstraints != null && modeConstraints.equals(old)) {
            return;
        }
        
        model.addMode(mode, modeConstraints);
        
        if(isVisible()) {
            viewRequestor.scheduleRequest(
                new ViewRequest(null, View.CHANGE_MODE_ADDED, null, mode));
        }
        
        WindowManagerImpl.getInstance().doFirePropertyChange(
            WindowManager.PROP_MODES, null, null);
    }

    /** Removes mode from model and requests view (if needed). */
    public void removeMode(ModeImpl mode) {
        if(!getModes().contains(mode)) {
            return;
        }
//        debugLog("removeMode()=" + mode.getDisplayName());
        model.removeMode(mode);
        if(isVisible()) {
            viewRequestor.scheduleRequest(
                new ViewRequest(null, View.CHANGE_MODE_REMOVED, null, mode));
        }
        WindowManagerImpl.getInstance().doFirePropertyChange(
            WindowManager.PROP_MODES, null, null);
    }

    /** Sets toolbar configuration name and requests view (if needed). */
    public void setToolbarConfigName(String toolbarConfigName) {
        String old = getToolbarConfigName();
        if(old.equals(toolbarConfigName)) {
            return;
        }
            
        model.setToolbarConfigName(toolbarConfigName);
        
        if(isVisible()) {
            viewRequestor.scheduleRequest(
                new ViewRequest(null, View.CHANGE_TOOLBAR_CONFIGURATION_CHANGED,
                            old, toolbarConfigName));
        }
    }

    /** Updates UI. */
    public void updateUI() {
        // Pure request, no model change.
        if(isVisible()) {
            viewRequestor.scheduleRequest(new ViewRequest(null, View.CHANGE_UI_UPDATE, null, null));
        }
    }
    
    // XXX
    public void setProjectName(String projectName) {
        model.setProjectName(projectName);
        
        if(isVisible()) {
            viewRequestor.scheduleRequest(new ViewRequest(null, View.CHANGE_PROJECT_NAME, null, projectName));
        }
    }
    
    ////////////////////////////
    // Mode specific >>
    private void closeMode(ModeImpl mode) {
        if(mode == null) {
            return;
        }
        
        TopComponent[] tcs = (TopComponent[])getModeOpenedTopComponents(mode).toArray(new TopComponent[0]);
        
        for(int i = 0; i < tcs.length; i++) {
            TopComponent tc = tcs[i];
            if(WindowManagerImpl.getInstance().isTopComponentPersistentWhenClosed(tc)) {
                model.addModeClosedTopComponent(mode, tc);
            } else {
                //mkleint since one cannot close the sliding mode just like that, we don't need to check the previous mode of the tc.
                model.removeModeTopComponent(mode, tc);
                String id = WindowManagerImpl.getInstance().findTopComponentID(tc);
                PersistenceManager.getDefault().removeGlobalTopComponentID(id);
            }
        }
        
        ModeImpl oldActive = getActiveMode();
        ModeImpl newActive;
        if(mode == oldActive) {
            newActive = setSomeModeActive();
        } else {
            newActive = oldActive;
        }
//        debugLog("closeMode()");
        
        // Remove mode from model if is not permanennt and emptied.
        boolean modeRemoved = false;
        if(!mode.isPermanent() && model.getModeTopComponents(mode).isEmpty()) {
            // only if no sliding modes' tc points to this mode, then it's ok to remove it.
            if (doCheckSlidingModes(mode)) {
//                debugLog("do close mode=" + mode.getDisplayName());
                model.removeMode(mode);
                modeRemoved = true;
            }
        }
        
        if(isVisible()) {
            viewRequestor.scheduleRequest(new ViewRequest(mode, View.CHANGE_MODE_CLOSED, null, null));
        }

        // Notify closed.
        for(int i = 0; i < tcs.length; i++) {
            // Notify TopComponent was closed.
            WindowManagerImpl.getInstance().notifyTopComponentClosed(tcs[i]);
        }
        
        if(oldActive != newActive) {
            WindowManagerImpl.getInstance().doFirePropertyChange(
                WindowManagerImpl.PROP_ACTIVE_MODE, oldActive, newActive);
        }
    
        if(modeRemoved) {
            WindowManagerImpl.getInstance().doFirePropertyChange(
                WindowManager.PROP_MODES, null, null);
        }
        
        // Notify new active.
        if(newActive != null) {
            // Notify registry.
            WindowManagerImpl.notifyRegistryTopComponentActivated(
                newActive.getSelectedTopComponent());
        } else {
            WindowManagerImpl.notifyRegistryTopComponentActivated(null);
        }
    }
    
    // XXX TODO Model should handle this on its own.
    private ModeImpl setSomeModeActive() {
        for(Iterator it = getModes().iterator(); it.hasNext(); ) {
            ModeImpl mode = (ModeImpl)it.next();
            if(!mode.getOpenedTopComponents().isEmpty() && Constants.MODE_KIND_SLIDING != mode.getKind()) {
                model.setActiveMode(mode);
                return mode;
            }
        }
        model.setActiveMode(null);
        return model.getActiveMode();
    }

    
    /** Sets bounds into model and requests view (if needed). */
    public void setModeBounds(ModeImpl mode, Rectangle bounds) {
        if(bounds == null) {
            return;
        }
        
        Rectangle old = getModeBounds(mode);
        if(old.equals(bounds)) {
            return;
        }
        
        model.setModeBounds(mode, bounds);
        
        if(isVisible() && getEditorAreaState() == Constants.EDITOR_AREA_SEPARATED) {
            viewRequestor.scheduleRequest(new ViewRequest(
                mode, View.CHANGE_MODE_BOUNDS_CHANGED, old, bounds));
        }
        
        mode.doFirePropertyChange(ModeImpl.PROP_BOUNDS, old, bounds);
    }
    
    /** Sets frame state. */
    public void setModeFrameState(ModeImpl mode, int frameState) {
        int old = getModeFrameState(mode);
        if(frameState == old) {
            return;
        }
        
        model.setModeFrameState(mode, frameState);
        
        if(isVisible() && getEditorAreaState() == Constants.EDITOR_AREA_SEPARATED) {
            viewRequestor.scheduleRequest(new ViewRequest(
                mode, View.CHANGE_MODE_FRAME_STATE_CHANGED,
                new Integer(old), new Integer(frameState)));
        }
    }
    
    /** Sets seleted TopComponent into model and requests view (if needed). */
    public void setModeSelectedTopComponent(ModeImpl mode, TopComponent selected) {
        // don't apply check for sliding kind when clearing selection to null
        if (mode.getKind() != Constants.MODE_KIND_SLIDING || selected != null) {
            if(!getModeOpenedTopComponents(mode).contains(selected)) {
                return;
            }
        }
        
        TopComponent old = getModeSelectedTopComponent(mode);
        if(selected == old) {
            return;
        }
        
        model.setModeSelectedTopComponent(mode, selected);
        
        if(isVisible()) {
            viewRequestor.scheduleRequest(
                new ViewRequest(mode, View.CHANGE_MODE_SELECTED_TOPCOMPONENT_CHANGED,
                old, selected));
        }
        
        // Notify registry.
        if(mode == getActiveMode()) {
            WindowManagerImpl.notifyRegistryTopComponentActivated(selected);
        }
    }
    
    /** Adds opened TopComponent into model and requests view (if needed). */
    public void addModeOpenedTopComponent(ModeImpl mode, TopComponent tc) {
        if(getModeOpenedTopComponents(mode).contains(tc)) {
            return;
        }

        // Validate the TopComponent was removed from other modes.
        removeTopComponentFromOtherModes(mode, tc);
        
        model.addModeOpenedTopComponent(mode, tc);
        
        if(isVisible()) {
            viewRequestor.scheduleRequest(
                new ViewRequest(mode, View.CHANGE_MODE_TOPCOMPONENT_ADDED,
                null, tc));
        }
        
        // Notify opened.
        WindowManagerImpl.getInstance().notifyTopComponentOpened(tc);
    }
    
    public void insertModeOpenedTopComponent(ModeImpl mode, TopComponent tc, int index) {
        List openedTcs = getModeOpenedTopComponents(mode);
        if(index >= 0 && !openedTcs.isEmpty()
        && openedTcs.size() > index && openedTcs.get(index) == tc) {
            return;
        }
        
        // Validate the TopComponent was removed from other modes.
        removeTopComponentFromOtherModes(mode, tc);
        
        model.insertModeOpenedTopComponent(mode, tc, index);
        
        if(isVisible()) {
            viewRequestor.scheduleRequest(
                new ViewRequest(mode, View.CHANGE_MODE_TOPCOMPONENT_ADDED, // PENDING inserted?
                null, tc));
        }
    }
    
    public void addModeClosedTopComponent(ModeImpl mode, TopComponent tc) {
        boolean opened = getModeOpenedTopComponents(mode).contains(tc);
        
        if(opened && !tc.canClose()) {
            return;
        }
        
        if(getModeTopComponents(mode).contains(tc) && !opened) {
            return;
        }
        
        // Validate the TopComponent was removed from other modes.
        removeTopComponentFromOtherModes(mode, tc);
        
        model.addModeClosedTopComponent(mode, tc);

        ModeImpl oldActive = getActiveMode();
        ModeImpl newActive;
        if(model.getModeOpenedTopComponents(mode).isEmpty() && mode == oldActive) {
            newActive = setSomeModeActive();
        } else {
            newActive = oldActive;
        }

        // Unmaximize mode if necessary.
        if(model.getMaximizedMode() == mode && model.getModeOpenedTopComponents(mode).isEmpty()) {
            model.setMaximizedMode(null);
        }
        
        if(isVisible() && opened) {
            viewRequestor.scheduleRequest(
                new ViewRequest(mode, View.CHANGE_MODE_TOPCOMPONENT_REMOVED,
                null, tc));
        }

        // Notify closed.
        if(opened) {
            WindowManagerImpl.getInstance().notifyTopComponentClosed(tc);
        }
        
        if(oldActive != newActive) {
            WindowManagerImpl.getInstance().doFirePropertyChange(
                WindowManagerImpl.PROP_ACTIVE_MODE, oldActive, newActive);
        }

        if(newActive != null) {
            // Notify registry.
            WindowManagerImpl.notifyRegistryTopComponentActivated(
                newActive.getSelectedTopComponent());
        } else {
            WindowManagerImpl.notifyRegistryTopComponentActivated(null);
        }
    }

    // XXX Could be called only during load phase of window system.
    public void addModeUnloadedTopComponent(ModeImpl mode, String tcID) {
        model.addModeUnloadedTopComponent(mode, tcID);
    }
    
    // XXX
    public void setUnloadedSelectedTopComponent(ModeImpl mode, String tcID) {
        model.setModeUnloadedSelectedTopComponent(mode, tcID);
    }

    // XXX
    public List getModeOpenedTopComponentsIDs(ModeImpl mode) {
        return model.getModeOpenedTopComponentsIDs(mode);
    }
    // XXX
    public List getModeClosedTopComponentsIDs(ModeImpl mode) {
        return model.getModeClosedTopComponentsIDs(mode);
    }
    // XXX
    public List getModeTopComponentsIDs(ModeImpl mode) {
        return model.getModeTopComponentsIDs(mode);
    }
    
    /** Helper validation. */
    private boolean removeTopComponentFromOtherModes(ModeImpl mode, TopComponent tc) {
        boolean tcRemoved = false;
        for(Iterator it = model.getModes().iterator(); it.hasNext(); ) {
            ModeImpl m = (ModeImpl)it.next();
            if(m == mode) {
                continue;
            }
            
            if(model.containsModeTopComponent(m, tc)) {
                tcRemoved = true;
                model.removeModeTopComponent(m, tc);
//                debugLog("removeTopComponentFromOtherModes()");

                // Remove mode from model if is not permanennt and emptied.
                boolean modeRemoved = false;
                if(!m.isPermanent() && m.isEmpty() && doCheckSlidingModes(m) 
                    // now the tc is not added to the sliding mode yet, but is *somehow* expected to be..
                    // maybe needs redesign..
                        && mode.getKind() != Constants.MODE_KIND_SLIDING) {
//                    debugLog("removeTopComponentFromOtherModes() - really removing=" + m.getDisplayName());
                    model.removeMode(m);
                    modeRemoved = true;
                }
            
                if(modeRemoved) {
                    WindowManagerImpl.getInstance().doFirePropertyChange(
                        WindowManager.PROP_MODES, null, null);
                }
            }
        }
        
        return tcRemoved;
    }
    
    /** Removed top component from model and requests view (if needed). */
    public void removeModeTopComponent(ModeImpl mode, TopComponent tc) {
        if(!containsModeTopComponent(mode, tc)) {
            return;
        }
        
        boolean viewChange = getModeOpenedTopComponents(mode).contains(tc);
        
        if(viewChange && !tc.canClose()) {
            return;
        }
        
        model.removeModeTopComponent(mode, tc);
        String id = WindowManagerImpl.getInstance().findTopComponentID(tc);
        PersistenceManager.getDefault().removeGlobalTopComponentID(id);

        ModeImpl oldActive = getActiveMode();
        ModeImpl newActive;
        if(model.getModeOpenedTopComponents(mode).isEmpty() && mode == oldActive) {
            newActive = setSomeModeActive();
        } else {
            newActive = oldActive;
        }

        // Unmaximize mode if necessary.
        if(model.getMaximizedMode() == mode && model.getModeOpenedTopComponents(mode).isEmpty()) {
            model.setMaximizedMode(null);
        }
        
//        debugLog("removeModeTopComponent()");
        // Remove mode from model if is not permanennt and emptied.
        boolean modeRemoved = false;
        if(!mode.isPermanent() && model.getModeTopComponents(mode).isEmpty()) {
            // remove only if there's no other component in sliding modes that has this one as the previous mode.
            //TODO
            if (doCheckSlidingModes(mode)) {
//                debugLog("removeModeTopComponent() -removing " + mode.getDisplayName());
                model.removeMode(mode);
                modeRemoved = true;
            }
        }
        
        
        
        if(viewChange && isVisible()) {
            viewRequestor.scheduleRequest(new ViewRequest(
                mode, View.CHANGE_MODE_TOPCOMPONENT_REMOVED,
                null, tc));
        }
        
        // Notify closed.
        if(viewChange) {
            WindowManagerImpl.getInstance().notifyTopComponentClosed(tc);
        }
        
        if(oldActive != newActive) {
            WindowManagerImpl.getInstance().doFirePropertyChange(
                WindowManagerImpl.PROP_ACTIVE_MODE, oldActive, newActive);
        }
    
        if(modeRemoved) {
            WindowManagerImpl.getInstance().doFirePropertyChange(
                WindowManager.PROP_MODES, null, null);
        }
            
        if(newActive != null) {
            // Notify registry.
            WindowManagerImpl.notifyRegistryTopComponentActivated(
                newActive.getSelectedTopComponent());
        } else {
            WindowManagerImpl.notifyRegistryTopComponentActivated(
                null);
        }
    }
    
   // remove the mode only if there's no other component in sliding modes that has this one as the previous mode.
    private boolean doCheckSlidingModes(ModeImpl mode) {
        ModeImpl slid = model.getSlidingMode(Constants.BOTTOM);
        if (slid != null) {
            TopComponent[] tcs = slid.getTopComponents();
            for (int i = 0; i < tcs.length; i++) {
                ModeImpl impl = model.getModeTopComponentPreviousMode(slid, tcs[i]);
                if (impl == mode) {
                    return false;
                }
            }
        }
        slid = model.getSlidingMode(Constants.LEFT);
        if (slid != null) {
            TopComponent[] tcs = slid.getTopComponents();
            for (int i = 0; i < tcs.length; i++) {
                ModeImpl impl = model.getModeTopComponentPreviousMode(slid, tcs[i]);
                if (impl == mode) {
                    return false;
                }
            }
        }
        slid = model.getSlidingMode(Constants.RIGHT);
        if (slid != null) {
            TopComponent[] tcs = slid.getTopComponents();
            for (int i = 0; i < tcs.length; i++) {
                ModeImpl impl = model.getModeTopComponentPreviousMode(slid, tcs[i]);
                if (impl == mode) {
                    return false;
                }
            }
        }        
        return true;
    }
    
    // XXX
    public void removeModeClosedTopComponentID(ModeImpl mode, String tcID) {
        // It is silent now, has to be used only for closed yet unloaded components!
        model.removeModeClosedTopComponentID(mode, tcID);
    }
    /// << Mode specific    
    //////////////////////////////
    
    // TopComponentGroup>>
    public boolean isGroupOpened(TopComponentGroupImpl tcGroup) {
        return model.isGroupOpened(tcGroup);
    }
    
    /** Opens TopComponentGroup. */
    public void openGroup(TopComponentGroupImpl tcGroup) {
        if(isGroupOpened(tcGroup)) {
            return;
        }

        Set openedBeforeTopComponents = new HashSet();
        Set tcs = tcGroup.getTopComponents();
        for(Iterator it = tcs.iterator(); it.hasNext(); ) {
            TopComponent tc = (TopComponent)it.next();
            if( tc.isOpened() ) {
                openedBeforeTopComponents.add( tc );
            }
        }
        
        tcs = tcGroup.getOpeningSet();
        List openedTcs = new ArrayList();
        for(Iterator it = tcs.iterator(); it.hasNext(); ) {
            TopComponent tc = (TopComponent)it.next();
            if(!tc.isOpened()) {
                WindowManagerImpl wm = WindowManagerImpl.getInstance();
                ModeImpl mode = (ModeImpl)wm.findMode(tc);
                if(mode == null) {
                    // Only view TopComponent is in group.
                    mode = wm.getDefaultViewMode();
                }
                model.addModeOpenedTopComponent(mode, tc);
                openedTcs.add(tc);
            }
        }

        
        model.openGroup(tcGroup, new HashSet(openedTcs), openedBeforeTopComponents);
        
        if(isVisible()) {
            viewRequestor.scheduleRequest(new ViewRequest(tcGroup, 
                View.CHANGE_TOPCOMPONENT_ARRAY_ADDED, null,
                (TopComponent[])openedTcs.toArray(new TopComponent[0])));
        }

        // Notify oepned.
        for(Iterator it = openedTcs.iterator(); it.hasNext(); ) {
            TopComponent tc = (TopComponent)it.next();
            WindowManagerImpl.getInstance().notifyTopComponentOpened(tc);
        }
    }
    
    /** Closes TopComponentGroup. */
    public void closeGroup(TopComponentGroupImpl tcGroup) {
        if(!isGroupOpened(tcGroup)) {
            return;
        }
        
        Set tcs = tcGroup.getClosingSet();
        List closedTcs = new ArrayList();
        
        Set openedTcsByGroup = model.getGroupOpenedTopComponents(tcGroup);
        
        // Find out TC which were opened before the group was opened.
        Set openedTcsBefore = model.getGroupOpenedBeforeTopComponents(tcGroup);

        // Adjust opening flags.
        for(Iterator it = model.getGroupTopComponents(tcGroup).iterator(); it.hasNext(); ) {
            TopComponent tc = (TopComponent)it.next();
            boolean wasOpenedBefore = openedTcsBefore.contains(tc);
            boolean openedByGroup = openedTcsByGroup.contains(tc);
            
            if(tc.isOpened()) {
                if(!wasOpenedBefore && !openedByGroup) {
                    // Open by group next time, user opened it while group was opened.
                    model.addGroupOpeningTopComponent(tcGroup, tc);
                }
            } else {
                if(wasOpenedBefore || openedByGroup) {
                    // Don't open by group next time, user closed it while group was opened.
                    model.removeGroupOpeningTopComponent(tcGroup, tc);
                }
            }
        }

        // Now close those which needed.
        for(Iterator it = tcs.iterator(); it.hasNext(); ) {
            TopComponent tc = (TopComponent)it.next();
            if(tc.isOpened()) {
                // Whether to ignore closing flag.
                if(openedTcsBefore.contains(tc)) {
                    continue;
                }
                
                boolean ignore = false;
                for(Iterator it2 = model.getTopComponentGroups().iterator(); it2.hasNext(); ) {
                    TopComponentGroupImpl group = (TopComponentGroupImpl)it2.next();
                    if(group == tcGroup) {
                        continue;
                    }
                    if(group.isOpened() && group.getOpeningSet().contains(tc)) {
                        ignore = true;
                        break;
                    }
                }
                if(ignore) {
                    continue;
                }
                
                // Now you can close it.
                ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(tc);
                if(mode != null) {
                    if(WindowManagerImpl.getInstance().isTopComponentPersistentWhenClosed(tc)) {
                        model.addModeClosedTopComponent(mode, tc);
                    } else {
                        model.removeModeTopComponent(mode, tc);
                        String id = WindowManagerImpl.getInstance().findTopComponentID(tc);
                        PersistenceManager.getDefault().removeGlobalTopComponentID(id);
                    }
                    closedTcs.add(tc);
                }
            }
        }

        model.closeGroup(tcGroup);
        
        if(isVisible()) {
            viewRequestor.scheduleRequest(new ViewRequest(tcGroup, 
                View.CHANGE_TOPCOMPONENT_ARRAY_REMOVED, null,
                (TopComponent[])closedTcs.toArray(new TopComponent[0])));
        }
        
        // Notify closed.
        for(Iterator it = closedTcs.iterator(); it.hasNext(); ) {
            TopComponent tc = (TopComponent)it.next();
            WindowManagerImpl.getInstance().notifyTopComponentClosed(tc);
        }
    }
    
    /** Adds TopComponentGroup into model. */
    public void addTopComponentGroup(TopComponentGroupImpl tcGroup) {
        model.addTopComponentGroup(tcGroup);
    }
    
    /** Removes TopComponentGroup from model. */
    public void removeTopComponentGroup(TopComponentGroupImpl tcGroup) {
        model.removeTopComponentGroup(tcGroup);
    }
    
    public boolean addGroupUnloadedTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        return model.addGroupUnloadedTopComponent(tcGroup, tcID);
    }
    
    public boolean removeGroupUnloadedTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        return model.removeGroupUnloadedTopComponent(tcGroup, tcID);
    }
    
    /** Adds opening top component for set into model. */
    public boolean addGroupUnloadedOpeningTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        return model.addGroupUnloadedOpeningTopComponent(tcGroup, tcID);
    }
    
    /** Removes opening top component from model. */
    public boolean removeGroupUnloadedOpeningTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        return model.removeGroupUnloadedOpeningTopComponent(tcGroup, tcID);
    }
    
    /** Adds closing top component for set into model. */
    public boolean addGroupUnloadedClosingTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        return model.addGroupUnloadedClosingTopComponent(tcGroup, tcID);
    }
    
    /** Removes closing top component for set from model. */
    public boolean removeGroupUnloadedClosingTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        return model.removeGroupUnloadedClosingTopComponent(tcGroup, tcID);
    }
    
    // XXX Just helper for persistence.
    public boolean addGroupUnloadedOpenedTopComponent(TopComponentGroupImpl tcGroup, String tcID) {
        if(!isGroupOpened(tcGroup)) {
            return false;
        }
        
        return model.addGroupUnloadedOpenedTopComponent(tcGroup, tcID);
    }
    
    // XXX Just helper for persistence
    public Set getGroupOpenedTopComponents(TopComponentGroupImpl tcGroup) {
        return model.getGroupOpenedTopComponents(tcGroup);
    }
    
    // XXX>>
    public Set getGroupTopComponentsIDs(TopComponentGroupImpl tcGroup) {
        return model.getGroupTopComponentsIDs(tcGroup);
    }
    
    public Set getGroupOpeningSetIDs(TopComponentGroupImpl tcGroup) {
        return model.getGroupOpeningSetIDs(tcGroup);
    }
    
    public Set getGroupClosingSetIDs(TopComponentGroupImpl tcGroup) {
        return model.getGroupClosingSetIDs(tcGroup);
    }
    
    public Set getGroupOpenedTopComponentsIDs(TopComponentGroupImpl tcGroup) {
        return model.getGroupOpenedTopComponentsIDs(tcGroup);
    }
    // XXX<<
    // TopComponentGroup<<
    //////////////////////////////
    
    // Mutators <<
    /////////////////////

    
    /////////////////////
    // Accessors>>
    
    /** Indicates whether windows system shows GUI. */
    public boolean isVisible() {
        return model.isVisible();
    }
    
    /** Gets <code>Set</code> of all <code>Mode</code>'s. */
    public Set getModes () {
        return model.getModes();
    }

    /** Gets main window bounds for joined(tiled) state. */
    public Rectangle getMainWindowBoundsJoined() {
        return model.getMainWindowBoundsJoined();
    }
    
    /** Gets main window bounds for separated state. */
    public Rectangle getMainWindowBoundsSeparated() {
        return model.getMainWindowBoundsSeparated();
    }
    
    public int getMainWindowFrameStateJoined() {
        return model.getMainWindowFrameStateJoined();
    }
    
    public int getMainWindowFrameStateSeparated() {
        return model.getMainWindowFrameStateSeparated();
    }

    /** Gets active mode from model. */
    public ModeImpl getActiveMode () {
        return model.getActiveMode();
    }

    /** Gets editor area bounds from model. */
    public Rectangle getEditorAreaBounds() {
        return model.getEditorAreaBounds();
    }

    /** Gets editor area constraints from model. */
    public SplitConstraint[] getEditorAreaConstraints() {
        return model.getEditorAreaConstraints();
    }

    /** Gets editor area state from model. */
    public int getEditorAreaState() {
        return model.getEditorAreaState();
    }
    
    public int getEditorAreaFrameState() {
        return model.getEditorAreaFrameState();
    }

    /** Gets maximized mode from model. */
    public ModeImpl getMaximizedMode() {
        return model.getMaximizedMode();
    }

    
    /** Gets constraints for mode from model. */
    public SplitConstraint[] getModeConstraints(ModeImpl mode) {
        return model.getModeConstraints(mode);
    }


    /** Gets toolbar configuration name from model. */
    public String getToolbarConfigName () {
        return model.getToolbarConfigName();
    }

    ////////////////////////////////
    /// >> Mode specific
    /** Gets programatic name of mode. */
    public String getModeName(ModeImpl mode) {
        return model.getModeName(mode);
    }
    /** Gets bounds. */
    public Rectangle getModeBounds(ModeImpl mode) {
        return model.getModeBounds(mode);
    }
    /** Gets State. */
    public int getModeState(ModeImpl mode) {
        return model.getModeState(mode);
    }
    /** Gets kind. */
    public int getModeKind(ModeImpl mode) {
        return model.getModeKind(mode);
    }
    
    /** Gets side. */
    public String getModeSide(ModeImpl mode) {
        return model.getModeSide(mode);
    }
    
    /** Gets frame state. */
    public int getModeFrameState(ModeImpl mode) {
        return model.getModeFrameState(mode);
    }
    /** Gets used defined. */
    public boolean isModePermanent(ModeImpl mode) {
        return model.isModePermanent(mode);
    }
    public boolean isModeEmpty(ModeImpl mode) {
        return model.isModeEmpty(mode);
    }
    /** */
    public boolean containsModeTopComponent(ModeImpl mode, TopComponent tc) {
        return model.containsModeTopComponent(mode, tc);
    }
    /** Gets selected TopComponent. */
    public TopComponent getModeSelectedTopComponent(ModeImpl mode) {
        return model.getModeSelectedTopComponent(mode);
    }
    /** Gets list of top components in this workspace. */
    public List getModeTopComponents(ModeImpl mode) {
        return model.getModeTopComponents(mode);
    }
    /** Gets list of top components in this workspace. */
    public List getModeOpenedTopComponents(ModeImpl mode) {
        return model.getModeOpenedTopComponents(mode);
    }
    /// << Mode specific
    ////////////////////////////////
    
    ////////////////////////////////////
    // TopComponentGroup specific >>
    public Set getTopComponentGroups() {
        return model.getTopComponentGroups();
    }
    
    public String getGroupName(TopComponentGroupImpl tcGroup) {
        return model.getGroupName(tcGroup);
    }
    
    public Set getGroupTopComponents(TopComponentGroupImpl tcGroup) {
        return model.getGroupTopComponents(tcGroup);
    }
    
    /** Gets opening top components for group from model. */
    public Set getGroupOpeningTopComponents(TopComponentGroupImpl tcGroup) {
        return model.getGroupOpeningTopComponents(tcGroup);
    }
    
    /** Gets closing top components for group from model. */
    public Set getGroupClosingTopComponents(TopComponentGroupImpl tcGroup) {
        return model.getGroupClosingTopComponents(tcGroup);
    }
    // TopComponentGroup specific <<
    ////////////////////////////////////

    // Accessors<<
    /////////////////////
    
    
    // Others>>
    // PENDING>>
    public void topComponentDisplayNameChanged(ModeImpl mode, TopComponent tc) {
        if(isVisible()) {
            viewRequestor.scheduleRequest(new ViewRequest(
                mode, View.CHANGE_TOPCOMPONENT_DISPLAY_NAME_CHANGED, null, tc));
        }
    }

    public void topComponentDisplayNameAnnotation(ModeImpl mode, TopComponent tc) {
        if(isVisible()) {
            viewRequestor.scheduleRequest(new ViewRequest(
                mode, View.CHANGE_TOPCOMPONENT_DISPLAY_NAME_ANNOTATION_CHANGED, null, tc));
        }
    }
    // PENDING<<
    
    public void topComponentToolTipChanged(ModeImpl mode, TopComponent tc) {
        if(isVisible()) {
            viewRequestor.scheduleRequest(new ViewRequest(
                mode, View.CHANGE_TOPCOMPONENT_TOOLTIP_CHANGED, null, tc));
        }
    }
    
    public void topComponentIconChanged(ModeImpl mode, TopComponent tc) {
        if(isVisible()) {
            viewRequestor.scheduleRequest(new ViewRequest(
                mode, View.CHANGE_TOPCOMPONENT_ICON_CHANGED, null, tc));
        }
    }
    // Others<<
    
    
    // Compound ones>>
    public void attachTopComponentsToSide(TopComponent[] tcs, ModeImpl attachMode, String side) {
        attachTopComponentsToSide(tcs, attachMode, side, true);
    }
    
    /** Creates new mode on side of specified one and puts there the TopComponentS. */
    private void attachTopComponentsToSide(TopComponent[] tcs, ModeImpl attachMode, String side, boolean fireEvents) {
        if(tcs == null || tcs.length == 0) {
            return;
        }
        
        // New mode. It is necessary to add it yet.
        ModeImpl newMode = WindowManagerImpl.getInstance().createModeImpl(
            ModeImpl.getUnusedModeName(), attachMode.getKind(), false);

        // XXX All others should have the same restriction.
        if(!newMode.canContain(tcs[0])) {
            return;
        }
        model.addModeToSide(newMode, attachMode, side);
        
        attachTopComponentsHelper(tcs, newMode, fireEvents);
    }

    /** Creates new mode on side of desktops and puts there the TopComponentS. */
    private void attachTopComponentsAroundDesktop(TopComponent[] tcs, String side, boolean fireEvents) {
        if(tcs == null || tcs.length == 0) {
            return;
        }
        
        // New mode. It is necessary to add it yet.
        ModeImpl newMode = WindowManagerImpl.getInstance().createModeImpl(
            ModeImpl.getUnusedModeName(), Constants.MODE_KIND_VIEW, false);

        // XXX All others should have the same restriction.
        if(!newMode.canContain(tcs[0])) {
            return;
        }
        model.addModeAround(newMode, side);
        
        attachTopComponentsHelper(tcs, newMode, fireEvents);
    }

    /** Creates new mode at split position and puts there the TopComponentS. */
    private void attachTopComponentsIntoSplit(ModelElement firstElement, ModelElement secondElement,
    TopComponent[] tcs, boolean fireEvents) {
        if(tcs == null || tcs.length == 0) {
            return;
        }

        ModeImpl newMode = WindowManagerImpl.getInstance().createModeImpl(
            ModeImpl.getUnusedModeName(), Constants.MODE_KIND_VIEW, false);
  
        // XXX All others should have the same restriction.
        if(!newMode.canContain(tcs[0])) {
            return;
        }
        model.addModeBetween(newMode, firstElement, secondElement);
        
        attachTopComponentsHelper(tcs, newMode, fireEvents);
    }
    
    /** Creates new mode on side of editor area and puts there the TopComponentS. */
    private void attachTopComponentsAroundEditor(TopComponent[] tcs, String side, boolean fireEvents) {
        if(tcs == null || tcs.length == 0) {
            return;
        }
        
        // New mode. It is necessary to add it yet.
        ModeImpl newMode = WindowManagerImpl.getInstance().createModeImpl(
            ModeImpl.getUnusedModeName(), Constants.MODE_KIND_VIEW, false);

        // XXX All others should have the same restriction.
        if(!newMode.canContain(tcs[0])) {
            return;
        }
        model.addModeAroundEditor(newMode, side);
        
        attachTopComponentsHelper(tcs, newMode, fireEvents);
    }
    
    private void attachTopComponentsIntoNewMode(TopComponent[] tcs, Rectangle bounds) {
        if(tcs == null || tcs.length == 0) {
            return;
        }
        
        // New mode
        // New mode. It is necessary to add it yet.
        ModeImpl newMode = WindowManagerImpl.getInstance().createModeImpl(
            ModeImpl.getUnusedModeName(), Constants.MODE_KIND_VIEW, false);
        newMode.setBounds(bounds);
        
        // XXX All others should have the same restriction.
        if(!newMode.canContain(tcs[0])) {
            return;
        }
        
        model.addMode(newMode, new SplitConstraint[] {new SplitConstraint(Constants.HORIZONTAL, 100, 0.5f)});
        
        attachTopComponentsHelper(tcs, newMode, true);
    }

    /** Helper method. */
    private void attachTopComponentsHelper(TopComponent[] tcs, ModeImpl newMode, boolean fireEvents) {
        for(int i = 0; i < tcs.length; i++) {
            TopComponent tc = tcs[i];
            removeTopComponentFromOtherModes(newMode, tc);
            model.addModeOpenedTopComponent(newMode, tc);
        }
        
        ModeImpl oldActiveMode = getActiveMode();
        
        model.setActiveMode(newMode);
        model.setModeSelectedTopComponent(newMode, tcs[0]);

        if(isVisible()) {
            viewRequestor.scheduleRequest(new ViewRequest(tcs[0],
                View.CHANGE_TOPCOMPONENT_ATTACHED, null, newMode));
        }

        if(!fireEvents) {
            return;
        }
        

        // Notify activated.
        WindowManagerImpl.notifyRegistryTopComponentActivated(tcs[0]);
        
        WindowManagerImpl.getInstance().doFirePropertyChange(
            WindowManager.PROP_MODES, null, null);
        
        if(oldActiveMode != newMode) {
            WindowManagerImpl.getInstance().doFirePropertyChange(
                WindowManagerImpl.PROP_ACTIVE_MODE, oldActiveMode, newMode);
        }
        
        
    }
    
    /** */
    public void activateModeTopComponent(ModeImpl mode, TopComponent tc) {
        if(!getModeOpenedTopComponents(mode).contains(tc)) {
            return;
        }
        
        ModeImpl oldActiveMode = getActiveMode();
        //#45650 -some API users call the activation all over again all the time on one item.
        // improve performance for such cases.
        if (oldActiveMode != null && oldActiveMode.equals(mode)) {
            if (tc != null && tc.equals(model.getModeSelectedTopComponent(mode))) {
                return;
            }
        }
        model.setActiveMode(mode);
        model.setModeSelectedTopComponent(mode, tc);
        
        if(isVisible()) {
            viewRequestor.scheduleRequest(new ViewRequest(mode, 
                View.CHANGE_TOPCOMPONENT_ACTIVATED, null, tc));
        }
        
        // Notify registry.
        WindowManagerImpl.notifyRegistryTopComponentActivated(tc);
        
        if(oldActiveMode != mode) {
            WindowManagerImpl.getInstance().doFirePropertyChange(
                WindowManagerImpl.PROP_ACTIVE_MODE, oldActiveMode, mode);
        }
    }
    // Compound ones<<
    

    // Other >>
    public boolean isDragInProgress() {
        // XXX
        return viewRequestor.isDragInProgress();
    }
    
    public Frame getMainWindow() {
        // XXX
        return viewRequestor.getMainWindow(); 
    }
    
    public String guessSlideSide(TopComponent tc) {
        return viewRequestor.guessSlideSide(tc);
    }
    
    // Other <<
    
    // Helper methods
    /** Creates model for mode, used internally. */
    public void createModeModel(ModeImpl mode, String name, int state, int kind, boolean permanent) {
        model.createModeModel(mode, name, state, kind, permanent); 
    }
    
    
    /** Creates model for top component group, used internally. */
    public void createGroupModel(TopComponentGroupImpl tcGroup, String name, boolean opened) {
        model.createGroupModel(tcGroup, name, opened);
    }
    
    // snapshot
    /** Creates window system model snapshot, used for requesting view. */
    public WindowSystemSnapshot createWindowSystemSnapshot() {
        return model.createWindowSystemSnapshot();
    }
    
    
    ///////////////////////////
    // ControllerHandler>>
    public void userActivatedMode(ModeImpl mode) {
        if(mode != null) {
            setActiveMode(mode);
        }
    }
    
    public void userActivatedModeWindow(ModeImpl mode) {
        if(mode != null) {
            setActiveMode(mode);
        }
    }
    
    public void userActivatedEditorWindow() {
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        TopComponent[] tcs = wm.getRecentViewList(); 
        for(int i = 0; i < tcs.length; i++) {
            TopComponent tc = tcs[i];
            ModeImpl mode = (ModeImpl)wm.findMode(tc);
            if(mode != null 
            && mode.getKind() == Constants.MODE_KIND_EDITOR
            && !mode.getOpenedTopComponents().isEmpty()) {
                setActiveMode(mode);
                return;
            }
        }
        
        ModeImpl mode = wm.getDefaultEditorMode();
        if(mode != null && !mode.getOpenedTopComponents().isEmpty()) {
            setActiveMode(mode);
        } else {
            // when someone calls this as a matter of activating editor mode as a fallback, but none is opened,
            // do unactivate the current selection.
            // #44389
            setActiveMode(null);
        }
    }
    
    public void userActivatedTopComponent(ModeImpl mode, TopComponent selected) {
        if(mode != null) {
            setModeSelectedTopComponent(mode, selected);
        }
    }
    
    public void userResizedMainWindow(Rectangle bounds) {
        if(getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
            model.setMainWindowBoundsJoined(bounds);
        } else {
            model.setMainWindowBoundsSeparated(bounds);
        }
    }
    
    public void userResizedMainWindowBoundsSeparatedHelp(Rectangle bounds) {
        if(getEditorAreaState() == Constants.EDITOR_AREA_JOINED
        && getMainWindowBoundsSeparated().isEmpty()) {
            model.setMainWindowBoundsUserSeparatedHelp(bounds);
        }
    }
    
    public void userResizedEditorArea(Rectangle bounds) {
        model.setEditorAreaBounds(bounds);
    }
    
    public void userResizedEditorAreaBoundsHelp(Rectangle bounds) {
        if(getEditorAreaState() == Constants.EDITOR_AREA_JOINED
        && getEditorAreaBounds().isEmpty()) {
            model.setEditorAreaBoundsUserHelp(bounds);
        }
    }

    public void userResizedModeBounds(ModeImpl mode, Rectangle bounds) {
        Rectangle old = model.getModeBounds(mode);
        model.setModeBounds(mode, bounds);
        
        mode.doFirePropertyChange(ModeImpl.PROP_BOUNDS, old, bounds);
    }
    
    public void userResizedModeBoundsSeparatedHelp(ModeImpl mode, Rectangle bounds) {
        model.setModeBoundsSeparatedHelp(mode, bounds);
    }
    
    public void userChangedFrameStateMainWindow(int frameState) {
        if(getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
            model.setMainWindowFrameStateJoined(frameState);
        } else {
            model.setMainWindowFrameStateSeparated(frameState);
        }
    }
    
    public void userChangedFrameStateEditorArea(int frameState) {
        model.setEditorAreaFrameState(frameState);
    }
    
    public void userChangedFrameStateMode(ModeImpl mode, int frameState) {
        model.setModeFrameState(mode, frameState);
    }
    
    public void userChangedSplit(ModelElement firstElement, double firstSplitWeight,
    ModelElement secondElement, double secondSplitWeight) {
        model.setSplitWeights(firstElement, firstSplitWeight, secondElement, secondSplitWeight);
    }

    public void userClosedTopComponent(ModeImpl mode, TopComponent tc) {
//        debugLog("userClosedTopComponent");
        if(WindowManagerImpl.getInstance().isTopComponentPersistentWhenClosed(tc)) {
            addModeClosedTopComponent(mode, tc);
        } else {
            removeModeTopComponent(mode, tc);
        }

        // Unmaximize if necessary.
        if(mode.getOpenedTopComponents().isEmpty()
        && mode == getMaximizedMode()) {
            setMaximizedMode(null);
        }
    }
    
    public void userClosedMode(ModeImpl mode) {
        if(mode != null) {
            closeMode(mode);
            // Unmaximize if necessary.
            if(mode.getOpenedTopComponents().isEmpty()
                && mode == getMaximizedMode()) 
            {
                setMaximizedMode(null);
            }
        }
        
    }
    
    
    // DnD
    public void userDroppedTopComponents(ModeImpl mode, TopComponent[] tcs) {
        updateViewAfterDnD(moveTopComponentsIntoMode(mode, tcs));
    }
    
    public void userDroppedTopComponents(ModeImpl mode, TopComponent[] tcs, int index) {
        updateViewAfterDnD(moveTopComponentsIntoMode(mode, tcs, index));
    }
    
    public void userDroppedTopComponents(ModeImpl mode, TopComponent[] tcs, String side) {
        attachTopComponentsToSide(tcs, mode, side, false);
        
        updateViewAfterDnD(true);
    }
    
    public void userDroppedTopComponentsIntoEmptyEditor(TopComponent[] tcs) {
        // PENDING
        ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode("editor"); // NOI18N
        moveTopComponentsIntoMode(mode, tcs);
        updateViewAfterDnD(true);
    }
    
    public void userDroppedTopComponentsAround(TopComponent[] tcs, String side) {
        attachTopComponentsAroundDesktop(tcs, side, false);

        updateViewAfterDnD(true);
    }
    
    public void userDroppedTopComponentsIntoSplit(ModelElement splitElement,
    ModelElement firstElement, ModelElement secondElement, TopComponent[] tcs) {
        attachTopComponentsIntoSplit(firstElement, secondElement, tcs, false);

        updateViewAfterDnD(true);
    }
    
    public void userDroppedTopComponentsAroundEditor(TopComponent[] tcs, String side) {
        attachTopComponentsAroundEditor(tcs, side, false);

        updateViewAfterDnD(true);
    }
    
    public void userDroppedTopComponentsIntoFreeArea(TopComponent[] tcs, Rectangle bounds) {
        if(getEditorAreaState() == Constants.EDITOR_AREA_JOINED) {
            return; // There is not possible to drop outside when in compact mode.
        }
        
        attachTopComponentsIntoNewMode(tcs, bounds);
        
        updateViewAfterDnD(true);
    }
    
    private boolean moveTopComponentsIntoMode(ModeImpl mode, TopComponent[] tcs) {
        boolean moved = false;
        for(int i = 0; i < tcs.length; i++) {
            TopComponent tc = tcs[i];
            // XXX
            if(!mode.canContain(tc)) {
                continue;
            }
            if(removeTopComponentFromOtherModes(mode, tc)) {
                moved = true;
            }
            model.addModeOpenedTopComponent(mode, tc);
        }
        model.setActiveMode(mode);
        model.setModeSelectedTopComponent(mode, tcs[tcs.length - 1]);
        return moved;
    }
    
    private boolean moveTopComponentsIntoMode(ModeImpl mode, TopComponent[] tcs, int index) {
        boolean moved = false;
        for(int i = 0; i < tcs.length; i++) {
            TopComponent tc = tcs[i];
            if(!mode.canContain(tc)) {
                continue;
            }
            if(removeTopComponentFromOtherModes(mode, tc)) {
                moved = true;
            }
            model.insertModeOpenedTopComponent(mode, tc, index);
        }
        model.setActiveMode(mode);
        model.setModeSelectedTopComponent(mode, tcs[tcs.length - 1]);
        return moved;
    }

    
    private void updateViewAfterDnD(boolean unmaximize) {
        if(unmaximize && getMaximizedMode() != null) {
            model.setMaximizedMode(null);
        }
        
        if(isVisible()) {
            viewRequestor.scheduleRequest(
                new ViewRequest(null, View.CHANGE_DND_PERFORMED, null, null));
        }
    }

    
    // Sliding
    
   /** Adds mode into model and requests view (if needed). */
    public void addSlidingMode(ModeImpl mode, ModeImpl original, String side) {
        ModeImpl targetMode = model.getSlidingMode(side);
        if (targetMode != null) {
            //TODO what to do here.. something there already
            return;
        }
            targetMode = WindowManagerImpl.getInstance().createModeImpl(
                ModeImpl.getUnusedModeName(), Constants.MODE_KIND_SLIDING, false);
        
        model.addSlidingMode(mode, side);
        
        if(isVisible()) {
            viewRequestor.scheduleRequest(
                new ViewRequest(null, View.CHANGE_MODE_ADDED, null, mode));
        }
        
        WindowManagerImpl.getInstance().doFirePropertyChange(
            WindowManager.PROP_MODES, null, null);
    }    
    
    public void userEnabledAutoHide(TopComponent tc, ModeImpl source, String targetSide) {
        ModeImpl targetMode = model.getSlidingMode(targetSide);
        if (targetMode == null) {
            targetMode = WindowManagerImpl.getInstance().createModeImpl(
                ModeImpl.getUnusedModeName(), Constants.MODE_KIND_SLIDING, false);
            model.addSlidingMode(targetMode, targetSide);
            model.setModeBounds(targetMode, new Rectangle(tc.getBounds()));
        }
        moveTCWithoutActivation(targetMode, tc);
        // remember previous mode and constraints for precise de-auto-hide
        model.setModeTopComponentPreviousMode(targetMode, tc, source);
        model.setModeTopComponentPreviousConstraints(targetMode, tc, model.getModeConstraints(source));
        ModeImpl oldActive = getActiveMode();
        ModeImpl newActive;
        if(source == oldActive && source.getOpenedTopComponents().isEmpty()) {
            newActive = setSomeModeActive();
        } else {
            newActive = oldActive;
        }        
        
        if(isVisible()) {
            viewRequestor.scheduleRequest(
                new ViewRequest(null, View.CHANGE_TOPCOMPONENT_AUTO_HIDE_ENABLED, null, null));
        }
        
        if(oldActive != newActive) {
            WindowManagerImpl.getInstance().doFirePropertyChange(
                WindowManagerImpl.PROP_ACTIVE_MODE, oldActive, newActive);
        }
    
        // Notify new active.
        if(newActive != null) {
            // Notify registry.
            WindowManagerImpl.notifyRegistryTopComponentActivated(
                newActive.getSelectedTopComponent());
        } else {
            WindowManagerImpl.notifyRegistryTopComponentActivated(null);
        }        
        
    }
    
    public void userResizedSlidingMode(ModeImpl mode, Rectangle rect) {
        model.setModeBounds(mode, new Rectangle(rect));
    }
    
    
    public void userDisabledAutoHide(TopComponent tc, ModeImpl source) {
        // unmaximize if needed
        if(getMaximizedMode() != null) {
            model.setMaximizedMode(null);
        }
        
        ModeImpl targetMode = getModeTopComponentPreviousMode(tc, source);
//        debugLog("userDisabledAutoHide()=" + targetMode);
        
        if ((targetMode == null) || !model.getModes().contains(targetMode)) {
//            debugLog("userDisabledAutoHide- previous one doesn't exist");
            // mode to return to isn't valid anymore, try constraints
            SplitConstraint[] constraints = model.getModeTopComponentPreviousConstraints(source, tc);
            constraints = constraints == null ? new SplitConstraint[0] : constraints;
            // create mode to dock topcomponent back into
            targetMode = WindowManagerImpl.getInstance().createModeImpl(
                    ModeImpl.getUnusedModeName(), Constants.MODE_KIND_VIEW, false);
            model.addMode(targetMode, constraints);
        }
        
        moveTopComponentsIntoMode(targetMode, new TopComponent[] { tc } );
        
        if (source.isEmpty()) {
//            debugLog("userDisabledAutoHide- removing " + source.getDisplayName());
            model.removeMode(source);
        }
        //set active mode, to avoid stuff like issue #50767
        setActiveMode(targetMode);
        
        if(isVisible()) {
            viewRequestor.scheduleRequest(
                new ViewRequest(null, View.CHANGE_TOPCOMPONENT_AUTO_HIDE_DISABLED, null, null));
        }
        WindowManagerImpl.getInstance().doFirePropertyChange(
            WindowManagerImpl.PROP_ACTIVE_MODE, null, getActiveMode());
    }

    /** Moves given top component into given mode, but does not activate it
     * or select it, in contrary to moveTopComponentsIntoMode methods
     */
    private boolean moveTCWithoutActivation(ModeImpl mode, TopComponent tc) {
        if(!mode.canContain(tc)) {
            return false;
        }
        if(!removeTopComponentFromOtherModes(mode, tc)) {
            return false;
        }
        model.addModeOpenedTopComponent(mode, tc);
        return true;
    }
    
    
    public ModeImpl getModeTopComponentPreviousMode(TopComponent tc, ModeImpl currentSlidingMode) {
        return  model.getModeTopComponentPreviousMode(currentSlidingMode, tc);
    }
    
    public void setModeTopComponentPreviousMode(TopComponent tc, ModeImpl currentSlidingMode, ModeImpl prevMode) {
        model.setModeTopComponentPreviousMode(currentSlidingMode, tc, prevMode);
    }
    
    // ControllerHandler <<
    ////////////////////////////
    
    private static void debugLog(String message) {
        Debug.log(Central.class, message);
    }    

}
