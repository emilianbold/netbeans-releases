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

import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.model.ModelElement;

import org.openide.windows.TopComponent;


/**
 * Class which handles controller requests.
 *
 * @author  Peter Zavadsky
 */
public interface ControllerHandler {
    
    public void userActivatedMode(ModeImpl mode);
    
    public void userActivatedTopComponent(ModeImpl mode, TopComponent selected);
    
    public void userResizedMainWindow(Rectangle bounds);
    
    public void userResizedEditorArea(Rectangle bounds);
    
    public void userResizedModeBounds(ModeImpl mode, Rectangle bounds);
    
    public void userChangedFrameStateMainWindow(int frameState);
    
    public void userChangedFrameStateEditorArea(int frameState);
    
    public void userChangedFrameStateMode(ModeImpl mode, int frameState);
    
    public void userChangedSplit(ModelElement firstElement, double firstSplitWeight,
    ModelElement secondElement, double secondSplitWeight);
    
    public void userClosedTopComponent(ModeImpl mode, TopComponent tc);
    
    public void userClosedMode(ModeImpl mode);
    
    // Helpers>>
    public void userResizedMainWindowBoundsSeparatedHelp(Rectangle bounds);
    
    public void userResizedEditorAreaBoundsHelp(Rectangle bounds);
    
    public void userResizedModeBoundsSeparatedHelp(ModeImpl mode, Rectangle bounds);
    // Helpers<<
    
    // DnD>>
    public void userDroppedTopComponents(ModeImpl mode, TopComponent[] tcs);
    
    public void userDroppedTopComponents(ModeImpl mode, TopComponent[] tcs, int index);
    
    public void userDroppedTopComponents(ModeImpl mode, TopComponent[] tcs, String side);
    
    public void userDroppedTopComponentsIntoEmptyEditor(TopComponent[] tcs);
    
    public void userDroppedTopComponentsAround(TopComponent[] tcs, String side);
    
    public void userDroppedTopComponentsIntoSplit(ModelElement splitElement,
    ModelElement firstElementr, ModelElement secondElement, TopComponent[] tcs);
    
    public void userDroppedTopComponentsAroundEditor(TopComponent[] tcs, String side);
    
    public void userDroppedTopComponentsIntoFreeArea(TopComponent[] tcs, Rectangle bounds);
    // DnD<<
}

