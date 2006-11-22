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


import java.util.ArrayList;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.model.ModelElement;
import org.netbeans.core.windows.view.ui.slides.SlideOperation;
import org.openide.windows.TopComponent;

import java.awt.*;


/**
 * Class which handles controller requests.
 *
 * @author  Peter Zavadsky
 */
public interface ControllerHandler {

    public void userActivatedMode(ModeImpl mode);

    public void userActivatedModeWindow(ModeImpl mode);

    public void userActivatedEditorWindow();
    
    public void userActivatedTopComponent(ModeImpl mode, TopComponent selected);
    
    public void userResizedMainWindow(Rectangle bounds);
    
    public void userResizedEditorArea(Rectangle bounds);
    
    public void userResizedModeBounds(ModeImpl mode, Rectangle bounds);
    
    public void userChangedFrameStateMainWindow(int frameState);
    
    public void userChangedFrameStateEditorArea(int frameState);
    
    public void userChangedFrameStateMode(ModeImpl mode, int frameState);
    
    public void userChangedSplit( ModelElement[] snapshots, double[] splitWeights );
    
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
    
    public void userDroppedTopComponentsAroundEditor(TopComponent[] tcs, String side, int modeKind);
    
    public void userDroppedTopComponentsIntoFreeArea(TopComponent[] tcs, Rectangle bounds, int modeKind);
    // DnD<<

    // undock/dock
    public void userUndockedTopComponent(TopComponent tc, int modeKind);

    public void userDockedTopComponent(TopComponent tc, int modeKind);

    // Sliding>>
    public void userEnabledAutoHide(TopComponent tc, ModeImpl source, String target);
    
    public void userDisabledAutoHide(TopComponent tc, ModeImpl source);
    
    public void userResizedSlidingMode(ModeImpl mode, Rectangle rect);
    // Sliding<<
    
}

