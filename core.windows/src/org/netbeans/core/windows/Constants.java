/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.core.windows;

import java.awt.Dimension;
import javax.swing.JSplitPane;
import org.netbeans.swing.tabcontrol.TabbedContainer;
import org.openide.util.Utilities;

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

    /** Client property to distinguish JWindows/JDialogs used as ui elements
     * for separate modes - floating windows.
     */
    public static final String SEPARATE_WINDOW_PROPERTY = "SeparateWindow";

    // System properties (switches):
    /** Allows user to move <code>TopComponent</code>S between document and view modes, 
     * which is restricted otherwise. */
    public static final boolean SWITCH_MODE_ADD_NO_RESTRICT = Boolean.getBoolean("netbeans.winsys.allow.dock.anywhere"); // NOI18N
    /** Disables DnD of <code>TopComponent</code>S. */
    public static final boolean SWITCH_DND_DISABLE          = Boolean.getBoolean("netbeans.winsys.disable_dnd"); // NOI18N
    /** During DnD it provides nicer feedback (fading of possible drop), however performance is worsen in that case. */
    public static final boolean SWITCH_DROP_INDICATION_FADE = Boolean.getBoolean("netbeans.winsys.dndfade.on"); //NOI18N
    /** Shows the status line at the end of menu bar instead of at the bottom of main window. */
    public static final boolean SWITCH_STATUSLINE_IN_MENUBAR = Boolean.getBoolean("netbeans.winsys.statusLine.in.menuBar"); // NOI18N

    /** Gets the image resource to be used in the empty editor area. */
    public static final String  SWITCH_IMAGE_SOURCE         = System.getProperty("netbeans.winsys.imageSource"); // NOI18N
    
    // XXX #37999
    /** For view, do not show emty documents area, i.e. when no document is opened. */
    public static final boolean SWITCH_HIDE_EMPTY_DOCUMENT_AREA = Boolean.getBoolean("netbeans.winsys.hideEmptyDocArea"); // NOI18N
    
    /** Allowing complete removal of toolbars. */
    public static final boolean NO_TOOLBARS = Boolean.getBoolean("netbeans.winsys.no_toolbars"); // NOI18N

    /** File name whose InstanceCookie can contain custom menu bar component.*/
    public static final String CUSTOM_MENU_BAR_PATH = System.getProperty("netbeans.winsys.menu_bar.path"); // NOI18N

    /** File name whose InstanceCookie can contain custom status line component.*/
    public static final String CUSTOM_STATUS_LINE_PATH = System.getProperty("netbeans.winsys.status_line.path"); // NOI18N

    /** If set to true the help button will not be shown in the dialogs.*/
    public static final boolean DO_NOT_SHOW_HELP_IN_DIALOGS = Boolean.getBoolean("netbeans.winsys.no_help_in_dialogs"); // NOI18N
    
    /** True means automatic iconification/deiconification of all separate frames if main window is iconified/deiconified */ 
    public static final boolean AUTO_ICONIFY = Boolean.getBoolean("netbeans.winsys.auto_iconify"); // NOI18N
    
    private Constants() {}
}
