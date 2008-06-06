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
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import org.netbeans.modules.debugger.jpda.ui.models.DebuggingMonitorModel;
import org.netbeans.modules.debugger.jpda.ui.models.DebuggingNodeModel;
import org.netbeans.modules.debugger.jpda.ui.models.DebuggingTreeModel;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

public final class FiltersDescriptor {

    public static final String SUSPEND_SORT = "suspend_sort";
    public static final String NATURAL_SORT = "natural_sort";
    public static final String ALPHABETIC_SORT = "alphabetic_sort";
    public static final String SHOW_QUALIFIED_NAMES = "show_fqn";
    public static final String SHOW_MONITORS = "show_monitors";
    public static final String SHOW_SYSTEM_THREADS = "show_system_threads";
    public static final String SHOW_SUSPEND_TABLE = "show_suspend_table";
    public static final String SHOW_THREAD_GROUPS = "thread_group";

    /** List of <Item> describing filters properties */
    private List<Item> filters;

    /** Creates a new instance of FiltersDescriptor */
    public FiltersDescriptor() {
        filters = new ArrayList<Item>();
    }
    
    public int getFilterCount() {
        return filters.size();
    }
    
    public String getName(int index) {
        return filters.get(index).name;
    }
    
    public String getDisplayName(int index) {
        return filters.get(index).displayName;
    }
    
    public String getTooltip(int index) {
        return filters.get(index).tooltip;
    }
    
    public Icon getSelectedIcon(int index) {
        return filters.get(index).selectedIcon;
    }
    
    public boolean isSelected(int index) {
        return filters.get(index).isSelected;
    }

    public void setSelected(int index, boolean selected) {
        filters.get(index).setSelected(selected);
    }
    
    public void connectToggleButton(int index, JToggleButton button) {
        filters.get(index).setToggleButton(button);
    }
    
    // **************************************************************************
    
    static FiltersDescriptor createDebuggingViewFilters() {
        FiltersDescriptor desc = new FiltersDescriptor();
        desc.addItem(new Item(SHOW_THREAD_GROUPS, SHOW_THREAD_GROUPS, getString("LBL_THREAD_GROUPS_TIP"),
                false, loadIcon("thread_group_mixed_16.png")));
        desc.addItem(new Item(SHOW_SUSPEND_TABLE, SHOW_SUSPEND_TABLE, getString("LBL_SUSPEND_TABLE_TIP"),
                false, loadIcon("show_suspend_table_option_16.png")));
        desc.addItem(new Item(SHOW_SYSTEM_THREADS, SHOW_SYSTEM_THREADS, getString("LBL_SYSTEM_THREADS_TIP"),
                false, loadIcon("show_system_threads_option_16.png")));
        desc.addItem(new Item(SHOW_MONITORS, SHOW_MONITORS, getString("LBL_MONITORS_TIP"),
                false, loadIcon("monitor_acquired_16.png")));
        desc.addItem(new Item(SHOW_QUALIFIED_NAMES, SHOW_QUALIFIED_NAMES, getString("LBL_QUALIFIED_NAMES_TIP"),
                false, loadIcon("show_fqn_option_16.png")));
        
        List<Item> groupMembers = new ArrayList<Item>();
        Group group = new Group();
        Item item;
        
        item = new Item(SUSPEND_SORT, SUSPEND_SORT, getString("LBL_SUSPEND_SORT_TIP"),
                false, loadIcon("suspend_property_sort_order_16.png"));
        groupMembers.add(item);
        desc.addItem(item);
        item.setGroup(group);
        
        item = new Item(ALPHABETIC_SORT, ALPHABETIC_SORT, getString("LBL_ALPHABETIC_SORT_TIP"),
                false, loadIcon("alphabetic_sort_order_16.png"));
        groupMembers.add(item);
        desc.addItem(item);
        item.setGroup(group);
        
        item = new Item(NATURAL_SORT, NATURAL_SORT, getString("LBL_NATURAL_SORT_TIP"),
                true, loadIcon("natural_sort_order_16.png"));
        groupMembers.add(item);
        desc.addItem(item);
        item.setGroup(group);
        
        group.setItems(groupMembers);
        return desc;
    }

    // **************************************************************************
    
    private void addItem (Item newItem) {
        filters.add(newItem);
    }
    
    private static Icon loadIcon(String iconName) {
        return new ImageIcon(Utilities.loadImage("org/netbeans/modules/debugger/jpda/resources/" + iconName));
    }
    
    private static String getString(String label) {
        return NbBundle.getMessage(FiltersDescriptor.class, label);
    }
    
    // **************************************************************************
    //     filter Item
    // **************************************************************************
    static class Item {
        String name;
        String displayName;
        String tooltip;
        Icon selectedIcon;
        
        private boolean isSelected;
        private Group group;
        private JToggleButton toggleButton; // [TODO]
        
        Item (String name, String displayName, String tooltip,
                boolean isSelected, Icon selectedIcon) {
            this.name = name;
            this.displayName = displayName;
            this.tooltip = tooltip;
            this.selectedIcon = selectedIcon;
            this.isSelected = isSelected;
            
            readValue();
        }
        
        public void setGroup(Group group) {
            this.group = group;
        }

        public boolean isSelected() {
            return isSelected;
        }
        
        public void setSelected(boolean state) {
            if (isSelected == state) {
                return;
            }
            if (isSelected && group != null) {
                toggleButton.setSelected(true);
                return;
            }
            isSelected = state;
            if (state && group != null) {
                for (Item item : group.getItems()) {
                    if (item != this) {
                        JToggleButton tb = item.getToggleButton();
                        item.isSelected = false; // do not use item.setSelected() here
                        item.writeValue();
                        tb.setSelected(false);
                    } // if
                } // for
            } // if
            writeValue();
        }

        public JToggleButton getToggleButton() {
            return toggleButton;
        }
        
        public void setToggleButton(JToggleButton button) {
            toggleButton = button;
//            if (group != null && isSelected) {
//                toggleButton.setEnabled(false);
//            }
        }

        private void readValue() {
            Preferences preferences = NbPreferences.forModule(getClass()).node("debugging"); // NOI18N
            if (name.equals(SHOW_SYSTEM_THREADS)) {
                isSelected = preferences.getBoolean(DebuggingTreeModel.SHOW_SYSTEM_THREADS, false);
            } else if (name.equals(SHOW_THREAD_GROUPS)) {
                isSelected = preferences.getBoolean(DebuggingTreeModel.SHOW_THREAD_GROUPS, false);
            } else if (name.equals(ALPHABETIC_SORT)) {
                isSelected = preferences.getBoolean(DebuggingTreeModel.SORT_ALPHABET, true);
            } else if (name.equals(SUSPEND_SORT)) {
                isSelected = preferences.getBoolean(DebuggingTreeModel.SORT_SUSPEND, false);
            } else if (name.equals(NATURAL_SORT)) {
                isSelected = !preferences.getBoolean(DebuggingTreeModel.SORT_ALPHABET, true) &&
                        !preferences.getBoolean(DebuggingTreeModel.SORT_SUSPEND, false); // [TODO]
            } else if (name.equals(SHOW_MONITORS)) {
                isSelected = preferences.getBoolean(DebuggingMonitorModel.SHOW_MONITORS, false);
            } else if (name.equals(SHOW_QUALIFIED_NAMES)) {
                isSelected = preferences.getBoolean(DebuggingNodeModel.SHOW_PACKAGE_NAMES, false);
            } else if (name.equals(SHOW_SUSPEND_TABLE)){
                isSelected = preferences.getBoolean(SHOW_SUSPEND_TABLE, true);
            } else {
                isSelected = false;
            }
        }

        private void writeValue() {
            String keyName = null;
            Preferences preferences = NbPreferences.forModule(getClass()).node("debugging"); // NOI18N
            if (name.equals(SHOW_SYSTEM_THREADS)) {
                keyName = DebuggingTreeModel.SHOW_SYSTEM_THREADS;
            } else if (name.equals(SHOW_THREAD_GROUPS)) {
                keyName = DebuggingTreeModel.SHOW_THREAD_GROUPS;
            } else if (name.equals(ALPHABETIC_SORT)) {
                keyName = DebuggingTreeModel.SORT_ALPHABET;
            } else if (name.equals(SUSPEND_SORT)) {
                keyName = DebuggingTreeModel.SORT_SUSPEND;
            } else if (name.equals(SHOW_MONITORS)) {
                keyName = DebuggingMonitorModel.SHOW_MONITORS;
            } else if (name.equals(SHOW_QUALIFIED_NAMES)) {
                keyName = DebuggingNodeModel.SHOW_PACKAGE_NAMES;
            } else if (name.equals(SHOW_SUSPEND_TABLE)) {
                keyName = SHOW_SUSPEND_TABLE;
            }
            if (keyName != null) {
                preferences.putBoolean(keyName, isSelected);
            }
        }
        
    }
    
    // **************************************************************************
    //     Group of Items
    // **************************************************************************
    static class Group {
        List<Item> items = Collections.EMPTY_LIST;
        
        public void setItems(List<Item> items) {
            this.items = items;
            for (Item item : items) {
                item.setGroup(this);
            }
        }
        
        public List<Item> getItems() {
            return items;
        }
        
    }
    
}
