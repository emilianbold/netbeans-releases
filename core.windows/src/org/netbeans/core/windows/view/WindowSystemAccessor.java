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


/**
 * Class which is used as an access point to data wchih View is responsible
 * to process.
 *
 * @author  Peter Zavadsky
 */
interface WindowSystemAccessor {

    /** Gets bounds of main window for joined(tiled) state. */
    public Rectangle getMainWindowBoundsJoined();
    
    /** Gets bounds of main window for separated state. */
    public Rectangle getMainWindowBoundsSeparated();
    
    /** Gets frame state of main window when in joined state. */
    public int getMainWindowFrameStateJoined();
    
    /** Gets frame state of main window when in separated state. */
    public int getMainWindowFrameStateSeparated();
    
    /** Gets editor area bounds. */
    public Rectangle getEditorAreaBounds();
    
    /** Gets editor area state. */
    public int getEditorAreaState();
    
    /** Gets frame state of editor area when in separated state. */
    public int getEditorAreaFrameState();
    
    /** Toolbar config name. */
    public String getToolbarConfigurationName();
    
    /** Gets active mode. */
    public ModeAccessor getActiveModeAccessor();
    
    /** Gets maximized mode. */
    public ModeAccessor getMaximizedModeAccessor();
    
    public ModeStructureAccessor getModeStructureAccessor();
    
    public ModeAccessor findModeAccessor(String modeName);
}

