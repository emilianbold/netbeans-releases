/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.debugger.jpda.ui.debugging;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.Utilities;

public final class FiltersDescriptor {

    public static final String SUSPEND_SORT = "suspend_sort";
    public static final String NATURAL_SORT = "natural_sort";
    public static final String ALPHABETIC_SORT = "alphabetic_sort";
    public static final String SHOW_FQN = "show_fqn";
    public static final String SHOW_MONITORS = "show_monitors"; // [TODO]
    public static final String SHOW_SYSTEM_THREADS = "show_system_threads";
    public static final String SHOW_SUSPEND_TABLE = "show_suspend_table";
    public static final String THREAD_GROUP = "thread_group"; // [TODO]
    
    /** List of <Item> describing filters properties */
    private List<Item> filters;

    /** Creates a new instance of FiltersDescriptor */
    public FiltersDescriptor() {
        filters = new ArrayList<Item>();
    }
    
    public void addFilter (String name, String displayName, String tooltip,
            boolean isSelected, Icon icon) {
        Item newItem = new Item(name, displayName, tooltip, isSelected, icon);
        filters.add(newItem);
    }
    
    public int getFilterCount () {
        return filters.size();
    }
    
    public String getName (int index) {
        return filters.get(index).name;
    }
    
    public String getDisplayName (int index) {
        return filters.get(index).displayName;
    }
    
    public String getTooltip (int index) {
        return filters.get(index).tooltip;
    }
    
    public Icon getSelectedIcon (int index) {
        return filters.get(index).selectedIcon;
    }
    
    public boolean isSelected (int index) {
        return filters.get(index).isSelected;
    }
    
    // **************************************************************************
    
    static FiltersDescriptor createDebuggingViewFilters() {
        FiltersDescriptor desc = new FiltersDescriptor();
        desc.addFilter(THREAD_GROUP, THREAD_GROUP, "", false, loadIcon("thread_group_mixed_16.png"));
        desc.addFilter(SHOW_SUSPEND_TABLE, SHOW_SUSPEND_TABLE, "", false, loadIcon("show_suspend_table_option_16.png"));
        desc.addFilter(SHOW_SYSTEM_THREADS, SHOW_SYSTEM_THREADS, "", false, loadIcon("show_system_threads_option_16.png"));
        desc.addFilter(SHOW_MONITORS, SHOW_MONITORS, "", false, loadIcon("monitor_acquired_16.png"));
        desc.addFilter(SHOW_FQN, SHOW_FQN, "", false, loadIcon("show_fqn_option_16.png"));
        desc.addFilter(SUSPEND_SORT, SUSPEND_SORT, "", false, loadIcon("suspend_property_sort_order_16.png"));
        desc.addFilter(ALPHABETIC_SORT, ALPHABETIC_SORT, "", false, loadIcon("alphabetic_sort_order_16.png"));
        desc.addFilter(NATURAL_SORT, NATURAL_SORT, "", false, loadIcon("natural_sort_order_16.png"));
        return desc;
    }
    
    private static Icon loadIcon(String iconName) {
        return new ImageIcon(Utilities.loadImage("org/netbeans/modules/debugger/jpda/resources/" + iconName));
    }
    
    // **************************************************************************
    //     filter Item
    // **************************************************************************
    static class Item {
        String name;
        String displayName;
        String tooltip;
        Icon selectedIcon;
        boolean isSelected;
        
        Item (String name, String displayName, String tooltip,
                boolean isSelected, Icon selectedIcon) {
            this.name = name;
            this.displayName = displayName;
            this.tooltip = tooltip;
            this.selectedIcon = selectedIcon;
            this.isSelected = isSelected;
        }
        
    }
    
}
