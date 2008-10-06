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

import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.Action;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

import org.netbeans.modules.debugger.jpda.ui.models.DebuggingMonitorModel;
import org.netbeans.modules.debugger.jpda.ui.models.DebuggingNodeModel;
import org.netbeans.modules.debugger.jpda.ui.models.DebuggingTreeModel;
import org.openide.util.actions.Presenter;


public final class FiltersDescriptor {

    public static final String SUSPEND_SORT = "suspend_sort";
    public static final String NATURAL_SORT = "natural_sort";
    public static final String ALPHABETIC_SORT = "alphabetic_sort";
    public static final String SHOW_QUALIFIED_NAMES = "show_fqn";
    public static final String SHOW_MONITORS = "show_monitors";
    public static final String SHOW_SYSTEM_THREADS = "show_system_threads";
    public static final String SHOW_SUSPEND_TABLE = "show_suspend_table";
    public static final String SHOW_THREAD_GROUPS = "thread_group";
    public static final String SHOW_SUSPENDED_THREADS_ONLY = "suspended_threads_only";

    private static FiltersDescriptor instance;

    /** List of <Item> describing filters properties */
    private List<Item> filters;

    private Action[] filterActions;

    /** Creates a new instance of FiltersDescriptor */
    private FiltersDescriptor() {
        filters = new ArrayList<Item>();
    }

    public synchronized static FiltersDescriptor getInstance() {
        if (instance == null) {
            instance = createDebuggingViewFilters();
        }
        return instance;
    }
    
    public synchronized Action[] getFilterActions() {
        if (filterActions == null) {
            List<Action> list = new ArrayList<Action>();
            for (Item item : filters) {
                if (item.getGroup() != null) {
                    SortAction action = new SortAction(item);
                    list.add(action);
                }
            } // for
            int size = list.size();
            filterActions = new Action[size + 2];
            for (int x = 0; x < size; x++) {
                filterActions[x] = list.get(x);
            }
            filterActions[size] = null; // separator
            filterActions[size + 1] = new FilterSubmenuAction(this);
        } // if
        return filterActions;
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
    
    public void setSelected(String filterName, boolean selected) {
        for (Item item : filters) {
            if (item.getName().equals(filterName)) {
                item.setSelected(selected);
                break;
            }
        }
    }
    
    public void connectToggleButton(int index, JToggleButton button) {
        filters.get(index).setToggleButton(button);
    }
    
    // **************************************************************************
    
    private static FiltersDescriptor createDebuggingViewFilters() {
        FiltersDescriptor desc = new FiltersDescriptor();
        desc.addItem(new Item(SHOW_SUSPENDED_THREADS_ONLY, getString("LBL_SUPSENDED_THREADS_ONLY"), getString("LBL_SUPSENDED_THREADS_ONLY_TIP"),
                false, loadIcon("show_suspended_threads_option_16.png")));
        desc.addItem(new Item(SHOW_THREAD_GROUPS, getString("LBL_THREAD_GROUPS"), getString("LBL_THREAD_GROUPS_TIP"),
                false, loadIcon("thread_group_mixed_16.png")));
        desc.addItem(new Item(SHOW_SUSPEND_TABLE, getString("LBL_SUSPEND_TABLE"), getString("LBL_SUSPEND_TABLE_TIP"),
                false, loadIcon("show_suspend_table_option_16.png")));
        desc.addItem(new Item(SHOW_SYSTEM_THREADS, getString("LBL_SYSTEM_THREADS"), getString("LBL_SYSTEM_THREADS_TIP"),
                false, loadIcon("show_system_threads_option_16.png")));
        desc.addItem(new Item(SHOW_MONITORS, getString("LBL_MONITORS"), getString("LBL_MONITORS_TIP"),
                false, loadIcon("monitor_acquired_16.png")));
        desc.addItem(new Item(SHOW_QUALIFIED_NAMES, getString("LBL_QUALIFIED_NAMES"), getString("LBL_QUALIFIED_NAMES_TIP"),
                false, loadIcon("show_fqn_option_16.png")));
        
        List<Item> groupMembers = new ArrayList<Item>();
        Group group = new Group();
        Item item;
        
        item = new Item(SUSPEND_SORT, getString("LBL_SUSPEND_SORT"), getString("LBL_SUSPEND_SORT_TIP"),
                false, loadIcon("suspend_property_sort_order_16.png"));
        groupMembers.add(item);
        desc.addItem(item);
        item.setGroup(group);
        
        item = new Item(ALPHABETIC_SORT, getString("LBL_ALPHABETIC_SORT"), getString("LBL_ALPHABETIC_SORT_TIP"),
                false, loadIcon("alphabetic_sort_order_16.png"));
        groupMembers.add(item);
        desc.addItem(item);
        item.setGroup(group);
        
        item = new Item(NATURAL_SORT, getString("LBL_NATURAL_SORT"), getString("LBL_NATURAL_SORT_TIP"),
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

        private Group getGroup() {
            return group;
        }
        
        public void setGroup(Group group) {
            this.group = group;
        }

        public String getName() {
            return name;
        }
        
        public String getDisplayName() {
            return displayName;
        }

        public Icon getIcon() {
            return selectedIcon;
        }

        public boolean isSelected() {
            return isSelected;
        }
        
        public void setSelected(boolean state) {
            if (isSelected == state) {
                return;
            }
            isSelected = state;
            toggleButton.setSelected(state);
            if (state && group != null) {
                for (Item item : group.getItems()) {
                    if (item != this && item.isSelected()) {
                        JToggleButton tb = item.getToggleButton();
                        item.isSelected = false; // do not call item.setSelected() here
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
            } else if (name.equals(SHOW_SUSPENDED_THREADS_ONLY)) {
                isSelected = preferences.getBoolean(DebuggingTreeModel.SHOW_SUSPENDED_THREADS_ONLY, false);
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
            } else if (name.equals(SHOW_SUSPENDED_THREADS_ONLY)) {
                keyName = DebuggingTreeModel.SHOW_SUSPENDED_THREADS_ONLY;
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
    
    // **************************************************************************
    //     Filter Actions Support
    // **************************************************************************
    
    private static final class SortAction extends AbstractAction implements Presenter.Popup {
    
        private Item filterItem;

        /** Creates a new instance of SortByNameAction */
        SortAction (Item item) {
            this.filterItem = item;
            putValue(Action.NAME, item.getDisplayName());
            putValue(Action.SMALL_ICON, item.getIcon());
        }

        public final JMenuItem getPopupPresenter() {
            JMenuItem result = obtainMenuItem();
            return result;
        }

        protected final JRadioButtonMenuItem obtainMenuItem () {
            JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem((String)getValue(Action.NAME));
            menuItem.setAction(this);
            menuItem.addHierarchyListener(new ParentChangeListener(menuItem));
            menuItem.setSelected(filterItem.isSelected);
            return menuItem;
        }

        public void actionPerformed(ActionEvent e) {
            filterItem.setSelected(!filterItem.isSelected);
        }

        private class ParentChangeListener implements HierarchyListener {

            private JRadioButtonMenuItem menuItem;

            public ParentChangeListener(JRadioButtonMenuItem menuItem) {
                this.menuItem = menuItem;
            }

            public void hierarchyChanged(HierarchyEvent e) {
                JComponent parent = (JComponent) e.getChangedParent();
                if (parent == null) {
                    return ;
                }
                ButtonGroup group = (ButtonGroup) parent.getClientProperty(getClass().getName()+" buttonGroup");
                if (group == null) {
                    group = new ButtonGroup();
                }
                group.add(menuItem);
                menuItem.removeHierarchyListener(this);
            }

        }

    }

    static final class FilterSubmenuAction extends AbstractAction implements Presenter.Popup {
    
        private static final String PROP_FILTER_NAME = "nbFilterName";

        private FiltersDescriptor filtersDesc;

        public FilterSubmenuAction(FiltersDescriptor filters) {
            this.filtersDesc = filters;
        }

        public void actionPerformed(ActionEvent ev) {
            Object source = ev.getSource();
            // react just on submenu items, not on submenu click itself
            if (source instanceof JCheckBoxMenuItem) {
                JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)source;
                String filterName = (String)(menuItem.getClientProperty(PROP_FILTER_NAME));
                filtersDesc.setSelected(filterName, menuItem.isSelected());
            }
        }

        public final JMenuItem getPopupPresenter() {
            return createSubmenu();
        }

        private JMenuItem createSubmenu () {
            JMenuItem menu = new JMenu(NbBundle.getMessage(FiltersDescriptor.class, "LBL_FilterSubmenu")); //NOI18N
            JMenuItem menuItem;
            String filterName;
            for (Item item : filtersDesc.filters) {
                if (item.getGroup() != null) {
                    continue;
                }
                filterName = item.getName();
                menuItem = new JCheckBoxMenuItem(item.getDisplayName(), item.isSelected());
                menuItem.addActionListener(this);
                menuItem.putClientProperty(PROP_FILTER_NAME, filterName);
                menu.add(menuItem);
            }
            return menu;
        }

    }
    
}