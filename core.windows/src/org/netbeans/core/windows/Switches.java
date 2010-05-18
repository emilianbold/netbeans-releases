/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.util.MissingResourceException;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Window system switches
 *
 * @author  S. Aubrecht
 */
public final class Switches {

    /**
     * 
     * @return True if TopComponent Drag'n'Drop is enabled.
     */
    public static boolean isTopComponentDragAndDropEnabled() {
        return getSwitchValue( "TopComponent.DragAndDrop.Enabled", true ); //NOI18N
    }

    /**
     * @param tc
     * @return True if given TopComponent can be dragged and dropped to a different location.
     */
    public static boolean isDraggingEnabled( TopComponent tc ) {
        return !Boolean.TRUE.equals(tc.getClientProperty(TopComponent.PROP_DRAGGING_DISABLED));
    }
    
    /**
     * 
     * @return True if undocking of TopComponents is allowed.
     */
    public static boolean isTopComponentUndockingEnabled() {
        return getSwitchValue( "TopComponent.Undocking.Enabled", true ); //NOI18N
    }

    /**
     * @param tc
     * @return True if given TopComponent can be undocked.
     */
    public static boolean isUndockingEnabled( TopComponent tc ) {
        return !Boolean.TRUE.equals(tc.getClientProperty(TopComponent.PROP_UNDOCKING_DISABLED));
    }
    
    /**
     * 
     * @return True if TopComponents can be minimized (slided out)
     */
    public static boolean isTopComponentSlidingEnabled() {
        return getSwitchValue( "TopComponent.Sliding.Enabled", true ); //NOI18N
    }

    /**
     * @param tc
     * @return True if given TopComponent can be slided out.
     */
    public static boolean isSlidingEnabled( TopComponent tc ) {
        return !Boolean.TRUE.equals(tc.getClientProperty(TopComponent.PROP_SLIDING_DISABLED));
    }
    
    /**
     * 
     * @return True if TopComponents can be resized by moving splitter bars with mouse.
     */
    public static boolean isTopComponentResizingEnabled() {
        return getSwitchValue( "TopComponent.Resizing.Enabled", true ); //NOI18N
    }
    
    /**
     * 
     * @return True if view-like TopComponents (e.g. navigator, projects view etc) can be closed.
     */
    public static boolean isViewTopComponentClosingEnabled() {
        return getSwitchValue( "View.TopComponent.Closing.Enabled", true ); //NOI18N
    }
    
    /**
     * 
     * @return True if editor TopComponents can be closed.
     */
    public static boolean isEditorTopComponentClosingEnabled() {
        return getSwitchValue( "Editor.TopComponent.Closing.Enabled", true ); //NOI18N
    }

    /**
     * @param tc
     * @return True if given TopComponent is closeable.
     */
    public static boolean isClosingEnabled( TopComponent tc ) {
        return !Boolean.TRUE.equals(tc.getClientProperty(TopComponent.PROP_CLOSING_DISABLED));
    }
    
    /**
     * 
     * @return True if TopComponents can be maximized.
     */
    public static boolean isTopComponentMaximizationEnabled() {
        return getSwitchValue( "TopComponent.Maximization.Enabled", true ); //NOI18N
    }

    /**
     * @param tc
     * @return True if given TopComponent can be maximized.
     */
    public static boolean isMaximizationEnabled( TopComponent tc ) {
        return !Boolean.TRUE.equals(tc.getClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED));
    }
    
    /**
     * 
     * @return True if the resizing splitter bar respects the minimum size of TopComponents.
     */
    public static boolean isSplitterRespectMinimumSizeEnabled() {
        return getSwitchValue( "Splitter.Respect.MinimumSize.Enabled", true ); //NOI18N
    }
    
    private static boolean getSwitchValue( String switchName, boolean defaultValue ) {
        boolean result = defaultValue;
        try {
            String resValue = NbBundle.getMessage(Switches.class, switchName );
            result = "true".equals( resValue.toLowerCase() ); //NOI18N
        } catch( MissingResourceException mrE ) {
            //ignore
        }
        return result;
    }
    
    private Switches() {}
}
