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
import java.util.ArrayList;
import org.netbeans.core.windows.view.ui.slides.SlideOperation;
import org.openide.windows.TopComponent;


/**
 * Window system controller declaration.
 *
 * @author  Peter Zavadsky
 */
public interface Controller {

    public void userActivatedModeView(ModeView modeView);
    
    public void userActivatedModeWindow(ModeView modeView);
    
    public void userActivatedEditorWindow();
    
    public void userSelectedTab(ModeView modeView, TopComponent selected);
    
    public void userClosingMode(ModeView modeView);
    
    public void userResizedMainWindow(Rectangle bounds);
    
    public void userMovedMainWindow(Rectangle bounds);
    
    public void userResizedEditorArea(Rectangle bounds);
    
    public void userChangedFrameStateMainWindow(int frameState);
    
    public void userChangedFrameStateEditorArea(int frameState);
    
    public void userChangedFrameStateMode(ModeView modeView, int frameState);
    
    public void userResizedModeBounds(ModeView modeView, Rectangle bounds);
    
    public void userMovedSplit(SplitView splitView, ViewElement[] childrenViews, double[] splitWeights);
    
    public void userClosedTopComponent(ModeView modeView, TopComponent tc);
    
    // DnD
    public void userDroppedTopComponents(ModeView modeView, TopComponent[] tcs);
    
    public void userDroppedTopComponents(ModeView modeView, TopComponent[] tcs, int index);
    
    public void userDroppedTopComponents(ModeView modeView, TopComponent[] tcs, String side);
    
    public void userDroppedTopComponentsIntoEmptyEditor(TopComponent[] tcs);
    
    public void userDroppedTopComponentsAround(TopComponent[] tcs, String side);
    
    public void userDroppedTopComponentsAroundEditor(TopComponent[] tcs, String side);
    
    public void userDroppedTopComponentsIntoFreeArea(TopComponent[] tcs, Rectangle bounds);
    
    // Sliding
    public void userEnabledAutoHide(ModeView modeView, TopComponent tc);
    
    public void userDisabledAutoHide(ModeView modeView, TopComponent tc);
    
    public void userTriggeredSlideIn(ModeView modeView, SlideOperation operation);

    public void userTriggeredSlideOut(ModeView modeView, SlideOperation operation);
    
    public void userTriggeredSlideIntoEdge(ModeView modeView, SlideOperation operation);
    
    public void userTriggeredSlideIntoDesktop(ModeView modeView, SlideOperation operation);

    public void userResizedSlidingWindow(ModeView modeView, SlideOperation operation);

}

