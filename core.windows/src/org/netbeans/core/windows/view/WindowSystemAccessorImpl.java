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


package org.netbeans.core.windows.view;


import java.awt.*;


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
    private String projectName;
    
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
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    public String getProjectName() {
        return projectName;
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

