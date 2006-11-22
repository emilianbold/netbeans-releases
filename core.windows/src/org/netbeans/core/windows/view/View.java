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


import org.netbeans.core.windows.WindowSystemSnapshot;
import java.awt.*;
import org.openide.windows.TopComponent;


/**
 * Represents view part of window system, define types of changes which are relevant
 * for GUI, and method {@link #changeGUI} which implemenation does all the
 * view's task.
 *
 * @author  Peter Zavadsky
 */
public interface View {

    // Global (the highest) level changes.
    public int CHANGE_VISIBILITY_CHANGED                        = 0;
    public int CHANGE_MAIN_WINDOW_BOUNDS_JOINED_CHANGED         = 1;
    public int CHANGE_MAIN_WINDOW_BOUNDS_SEPARATED_CHANGED      = 2;
    public int CHANGE_MAIN_WINDOW_FRAME_STATE_JOINED_CHANGED    = 3;
    public int CHANGE_MAIN_WINDOW_FRAME_STATE_SEPARATED_CHANGED = 4;
    public int CHANGE_EDITOR_AREA_STATE_CHANGED                 = 5;
    public int CHANGE_EDITOR_AREA_FRAME_STATE_CHANGED           = 6;
    public int CHANGE_EDITOR_AREA_BOUNDS_CHANGED                = 7;
    public int CHANGE_EDITOR_AREA_CONSTRAINTS_CHANGED           = 8;
    public int CHANGE_ACTIVE_MODE_CHANGED                       = 9;
    public int CHANGE_TOOLBAR_CONFIGURATION_CHANGED             = 10;
    public int CHANGE_MAXIMIZED_MODE_CHANGED                    = 11;
    public int CHANGE_MODE_ADDED                                = 12;
    public int CHANGE_MODE_REMOVED                              = 13;
    public int CHANGE_MODE_CONSTRAINTS_CHANGED                  = 14;

    
    // Mode level changes
    public int CHANGE_MODE_BOUNDS_CHANGED                = 20;
    public int CHANGE_MODE_FRAME_STATE_CHANGED           = 21;
    public int CHANGE_MODE_SELECTED_TOPCOMPONENT_CHANGED = 22;
    public int CHANGE_MODE_TOPCOMPONENT_ADDED            = 23;
    public int CHANGE_MODE_TOPCOMPONENT_REMOVED          = 24;
    
    // TopComponent level changes
    public int CHANGE_TOPCOMPONENT_DISPLAY_NAME_CHANGED            = 31;
    public int CHANGE_TOPCOMPONENT_DISPLAY_NAME_ANNOTATION_CHANGED = 32;
    public int CHANGE_TOPCOMPONENT_TOOLTIP_CHANGED                 = 33;
    public int CHANGE_TOPCOMPONENT_ICON_CHANGED                    = 34;

    // Compound changes
    public int CHANGE_TOPCOMPONENT_ATTACHED            = 41;
    public int CHANGE_TOPCOMPONENT_ARRAY_ADDED         = 42;
    public int CHANGE_TOPCOMPONENT_ARRAY_REMOVED       = 43;
    public int CHANGE_TOPCOMPONENT_ACTIVATED           = 44;
    public int CHANGE_MODE_CLOSED                      = 45;
    public int CHANGE_DND_PERFORMED                    = 46;
    public int CHANGE_TOPCOMPONENT_AUTO_HIDE_ENABLED   = 47;
    public int CHANGE_TOPCOMPONENT_AUTO_HIDE_DISABLED  = 48;
    
    // Others
    public int CHANGE_UI_UPDATE    = 61;
    
    public int TOPCOMPONENT_REQUEST_ATTENTION = 63;
    public int TOPCOMPONENT_CANCEL_REQUEST_ATTENTION = 64;
    public int CHANGE_MAXIMIZE_TOPCOMPONENT_SLIDE_IN = 65;
    
    /** Provides GUI changes to manifest model changes to user. */
    public void changeGUI(ViewEvent[] viewEvents, WindowSystemSnapshot snapshot);
    
    // XXX
    public boolean isDragInProgress();
    // XXX
    public Frame getMainWindow();
    
    public Component getEditorAreaComponent();
    
    public String guessSlideSide(TopComponent tc);
   
    
}

