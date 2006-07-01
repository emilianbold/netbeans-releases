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
    
    // XXX
    public String getProjectName();
}

