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


import java.awt.Rectangle;

import org.netbeans.core.windows.Debug;
import org.netbeans.core.windows.ModeImpl;


/**
 * Accessor impl of window system model, which is processed in view. 
 * It reflects the state of model in view convenient format, which is 
 * responsibility of view to present GUI according of state of this accessor.
 *
 * @author  Peter Zavadsky
 */
final class WindowSystemAccessorImpl implements WindowSystemAccessor {

    private Rectangle mainWindowBoundsJoined;
    private Rectangle mainWindowBoundsSeparated;
    private int mainWindowFrameStateJoined;
    private int mainWindowFrameStateSeparated;
    private String toolbarConfigurationName;
    private int editorAreaState;
    private int editorAreaFrameState;
    private Rectangle editorAreaBounds;
    private ModeAccessor activeMode;
    private ModeAccessor maximizedMode;
    private ModeStructureAccessor modeStructureAccessor;
    
    public WindowSystemAccessorImpl() {
    }

    
    public void setMainWindowBoundsJoined(Rectangle mainWindowBoundsJoined) {
        this.mainWindowBoundsJoined = mainWindowBoundsJoined;
    }

    public Rectangle getMainWindowBoundsJoined() {
        return mainWindowBoundsJoined;
    }
    
    public void setMainWindowBoundsSeparated(Rectangle mainWindowBoundsSeparated) {
        this.mainWindowBoundsSeparated = mainWindowBoundsSeparated;
    }

    public Rectangle getMainWindowBoundsSeparated() {
        return mainWindowBoundsSeparated;
    }
    
    public void setMainWindowFrameStateJoined(int mainWindowFrameStateJoined) {
        this.mainWindowFrameStateJoined = mainWindowFrameStateJoined;
    }
    
    public int getMainWindowFrameStateJoined() {
        return this.mainWindowFrameStateJoined;
    }
    
    public void setMainWindowFrameStateSeparated(int mainWindowFrameStateSeparated) {
        this.mainWindowFrameStateSeparated = mainWindowFrameStateSeparated;
    }
    
    public int getMainWindowFrameStateSeparated() {
        return this.mainWindowFrameStateSeparated;
    }
    
    public void setEditorAreaBounds(Rectangle editorAreaBounds) {
        this.editorAreaBounds = editorAreaBounds;
    }
    
    /** Gets editor area bounds. */
    public Rectangle getEditorAreaBounds() {
        return editorAreaBounds;
    }
    
    public void setEditorAreaState(int editorAreaState) {
        this.editorAreaState = editorAreaState;
    }
    
    /** Gets editor area state. */
    public int getEditorAreaState() {
        return editorAreaState;
    }
    
    public void setEditorAreaFrameState(int editorAreaFrameState) {
        this.editorAreaFrameState = editorAreaFrameState;
    }
    
    public int getEditorAreaFrameState() {
        return this.editorAreaFrameState;
    }
    
    /** */
    public void setActiveModeAccessor(ModeAccessor activeMode) {
        this.activeMode = activeMode;
    }
    
    /** Gets active mode. */
    public ModeAccessor getActiveModeAccessor() {
        return activeMode;
    }
    
    /** */
    public void setMaximizedModeAccessor(ModeAccessor maximizedMode) {
        this.maximizedMode = maximizedMode;
    }
    
    /** Gets maximized mode. */
    public ModeAccessor getMaximizedModeAccessor() {
        return maximizedMode;
    }

    public void setToolbarConfigurationName(String toolbarConfigurationName) {
        this.toolbarConfigurationName = toolbarConfigurationName;
    }
    /** Toolbar config name. */
    public String getToolbarConfigurationName() {
        return toolbarConfigurationName;
    }

    public void setModeStructureAccessor(ModeStructureAccessor modeStructureAccessor) {
        this.modeStructureAccessor = modeStructureAccessor;
    }
    
    public ModeStructureAccessor getModeStructureAccessor() {
        return modeStructureAccessor;
    }
    
    public ModeAccessor findModeAccessor(String modeName) {
        if(modeName == null) {
            return null;
        }
        
        if(modeStructureAccessor != null) {
            return ((ModeStructureAccessorImpl)modeStructureAccessor).findModeAccessor(modeName);
        }
        
        return null;
    }
    
    public String toString() {
        return super.toString() + "[modeStructure=" + modeStructureAccessor // NOI18N
            + ",\nactiveMode=" + activeMode  + ",\nmaximizedMode=" + maximizedMode + "]"; // NOI18N
    }
}

