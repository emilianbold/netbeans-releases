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


import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.openide.util.Utilities;

import javax.swing.*;
import java.awt.*;

/**
 * Constants in window system.
 *
 * @author  Peter Zavadsky
 */
public abstract class Constants {
    
    /** Constant that identifies state of editor area */
    public static final int EDITOR_AREA_JOINED    = 0;
    public static final int EDITOR_AREA_SEPARATED = 1;
    
    /** Constant that identifies mode state. */
    public static final int MODE_STATE_JOINED    = 0;
    public static final int MODE_STATE_SEPARATED = 1;
    
    /** Constant that identifies mode kind */
    public static final int MODE_KIND_EDITOR = TabbedContainer.TYPE_EDITOR;
    public static final int MODE_KIND_VIEW   = TabbedContainer.TYPE_VIEW;
    public static final int MODE_KIND_SLIDING = TabbedContainer.TYPE_SLIDING;

    /** Vertical orientation constant used in constraint. */
    public static final int VERTICAL   = JSplitPane.VERTICAL_SPLIT;
    /** Horizontal orientation constant used in constraint. */
    public static final int HORIZONTAL = JSplitPane.HORIZONTAL_SPLIT;
    
    /** Sides of attaching, used both for regular modes and sliding modes */
    public static final String TOP    = JSplitPane.TOP;
    public static final String BOTTOM = JSplitPane.BOTTOM;
    public static final String LEFT   = JSplitPane.LEFT;
    public static final String RIGHT  = JSplitPane.RIGHT;
    
    public static final int DIVIDER_SIZE_VERTICAL   = 4;
    public static final int DIVIDER_SIZE_HORIZONTAL = 5;

    /** Sets size of drop area (when splitting mode and around area). */
    public static final int DROP_AREA_SIZE = 20;
    
    /** How many pixels is necessary to drag to start the DnD. */ 
    public static final int DRAG_GESTURE_START_DISTANCE = 10;
    /** What time inmilliseconds is necessary to hold dragging mouse button for 
     & DnD to be started */
    public static final int DRAG_GESTURE_START_TIME = 200;
    
    // DnD drop ratios.
    /** How big portion of the original mode has to be taken (range from 0.0 to 1.0). */
    public static final double DROP_TO_SIDE_RATIO = 0.25D;
    /** How big portion should take the new mode from each one (between which is dropped) (range from 0.0 to 1.0). */
    public static final double DROP_BETWEEN_RATIO = 1.0D/3;
    /** How big portion of entire area should take the dropped mode (range from 0.0 to 1.0). */
    public static final double DROP_AROUND_RATIO = 0.25D;
    
    // XXX
    /** Size of new separated mode when creting during DnD (separeted mode). */
    public static final Dimension DROP_NEW_MODE_SIZE = new Dimension(300, 200);

    
    /** Name of client property (of Boolean type) which says whether the TopComponent is allowed
     * to be docked anywhere (even crossing view-editor border). */
    public static final String TOPCOMPONENT_ALLOW_DOCK_ANYWHERE = "TopComponentAllowDockAnywhere"; // NOI18N


    // System properties (switches):
    /** Allows user to move <code>TopComponent</code>S between document and view modes, 
     * which is restricted otherwise. */
    public static final boolean SWITCH_MODE_ADD_NO_RESTRICT = Boolean.getBoolean("netbeans.winsys.allow.dock.anywhere"); // NOI18N
    /** Disables DnD of <code>TopComponent</code>S. */
    public static final boolean SWITCH_DND_DISABLE          = Boolean.getBoolean("netbeans.winsys.disable_dnd"); // NOI18N
    /** During DnD it provides nicer feedback (fading of possible drop), however performance is worsen in that case. */
    public static final boolean SWITCH_DROP_INDICATION_FADE = Boolean.getBoolean("netbeans.winsys.dndfade.on"); //NOI18N
    /** Uses the old tabbed components as container. */
    public static final boolean SWITCH_OLD_TABS             = Boolean.getBoolean("netbeans.winsys.oldtabs"); // NOI18N
    /** Uses the old tabbed components as copntainers for view modes. */
    public static final boolean SWITCH_OLD_TABS_VIEW        = Boolean.getBoolean("netbeans.winsys.oldtabs.view"); // NOI18N
    /** Uses the old tabbed components as copntainers for editor modes. */
    public static final boolean SWITCH_OLD_TABS_EDITOR      = Boolean.getBoolean("netbeans.winsys.oldtabs.editor"); // NOI18N
    /** Enables close button on old tabbed component, relevant only together with switches enabling the old tabs.. */
    public static final boolean SWITCH_CLOSE_BUTTON_TAB     =  System.getProperty("netbeans.tab.close.button.enabled") == null // NOI18N
                                                               ? true : Boolean.getBoolean("netbeans.tab.close.button.enabled"); // NOI18N
    /** Shows the status line at the end of menu bar instead of at the bottom of main window. */
    public static final boolean SWITCH_STATUSLINE_IN_MENUBAR = Boolean.getBoolean("netbeans.winsys.statusLine.in.menuBar"); // NOI18N

    /** Gets the image resource to be used in the empty editor area. */
    public static final String  SWITCH_IMAGE_SOURCE         = System.getProperty("netbeans.winsys.imageSource"); // NOI18N
    
    // XXX #37999
    /** For view, do not show emty documents area, i.e. when no document is opened. */
    public static final boolean SWITCH_HIDE_EMPTY_DOCUMENT_AREA = Boolean.getBoolean("netbeans.winsys.hideEmptyDocArea"); // NOI18N
    
    // XXX #32920 Older switch, comaptibility.
    public static final boolean SWITCH_START_IN_SEPARATE_MODE = "sdi".equals(System.getProperty("netbeans.windows")); // NOI18N

    //Issue 39166, OS-X will display a strange gray rectangle while dragging - they
    //really require an image to be suppled.  Leaving this off for other platforms pending
    //HIE approval and performance impact evaluation - probably safe for Windows, probably
    //a big performance hit on Linux.  Appears that XP L&F will not display images even if
    //supplied.
    public static final boolean SWITCH_USE_DRAG_IMAGES = Boolean.getBoolean("netbeans.winsys.dragimage") || Utilities.getOperatingSystem() == Utilities.OS_MAC;
   
    /** Allowing complete removal of toolbars. */
    public static final boolean NO_TOOLBARS = Boolean.getBoolean("netbeans.winsys.no_toolbars"); // NOI18N

    /** File name whose InstanceCookie can contain custom menu bar component.*/
    public static final String CUSTOM_MENU_BAR_PATH = System.getProperty("netbeans.winsys.menu_bar.path"); // NOI18N

    /** File name whose InstanceCookie can contain custom status line component.*/
    public static final String CUSTOM_STATUS_LINE_PATH = System.getProperty("netbeans.winsys.status_line.path"); // NOI18N
    
    private Constants() {}
}

