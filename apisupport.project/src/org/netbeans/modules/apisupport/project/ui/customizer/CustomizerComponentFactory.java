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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.Component;
import java.io.CharConversionException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.table.AbstractTableModel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.openide.awt.HtmlRenderer;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;

/**
 * @author mkrauskopf
 */
public final class CustomizerComponentFactory {
    
    /** Generally usable in conjuction with {@link #createComboWaitModel}. */
    public static final String WAIT_VALUE =
            NbBundle.getMessage(CustomizerComponentFactory.class, "ComponentFactory_please_wait");
    
    /** Generally usable in conjuction with {@link #createComboEmptyModel}. */
    public static final String EMPTY_VALUE =
            NbBundle.getMessage(CustomizerComponentFactory.class, "LBL_Empty");
    
    static DependencyListModel INVALID_DEP_LIST_MODEL;
    
    private static final String INVALID_PLATFORM =
            "<html><font color=\"!nb.errorForeground\">&lt;" // NOI18N
            + NbBundle.getMessage(CustomizerComponentFactory.class, "MSG_InvalidPlatform")
            + "&gt;</font></html>"; // NOI18N
    
    private CustomizerComponentFactory() {
        // don't allow instances
    }
    
    /**
     * Use this model in situation when you need to populate combo in the
     * background. The only item in this model is {@link #WAIT_VALUE}.
     */
    public static ComboBoxModel createComboWaitModel() {
        return new DefaultComboBoxModel(new Object[] { WAIT_VALUE });
    }
    
    /** The only item in this model is {@link #EMPTY_VALUE}. */
    public static ComboBoxModel createComboEmptyModel() {
        return new DefaultComboBoxModel(new Object[] { EMPTY_VALUE });
    }
    
    /**
     * Conveninent method which delegates to {@link #hasOnlyValue} passing a
     * given model and {@link #WAIT_VALUE} as a value.
     */
    public static boolean isWaitModel(final ListModel model) {
        return hasOnlyValue(model, CustomizerComponentFactory.WAIT_VALUE);
    }
    
    /**
     * Returns true if the given model is not <code>null</code> and contains
     * only the given value.
     */
    public static boolean hasOnlyValue(final ListModel model, final Object value) {
        return model != null && model.getSize() == 1 && model.getElementAt(0) == value;
    }
    
    /**
     * Use this model in situation when you need to populate list in the
     * background. The only item in this model is {@link #WAIT_VALUE}.
     *
     * @see #isWaitModel
     */
    public static ListModel createListWaitModel() {
        DefaultListModel listWaitModel = new DefaultListModel();
        listWaitModel.addElement(WAIT_VALUE);
        return listWaitModel;
    }
    
    /**
     * Creates a list model for a set of module dependencies.
     * The dependencies will be sorted by module display name.
     */
    static CustomizerComponentFactory.DependencyListModel createSortedDependencyListModel(
            final Set<ModuleDependency> deps) {
        assert deps != null;
        return new CustomizerComponentFactory.DependencyListModel(deps, true);
    }
    
    /**
     * Creates a list model for a set of module dependencies.
     * The dependencies will be left in the order given.
     */
    static CustomizerComponentFactory.DependencyListModel createDependencyListModel(
            final Set<ModuleDependency> deps) {
        assert deps != null;
        return new CustomizerComponentFactory.DependencyListModel(deps, false);
    }
    
    static synchronized CustomizerComponentFactory.DependencyListModel getInvalidDependencyListModel() {
        if (INVALID_DEP_LIST_MODEL == null) {
            INVALID_DEP_LIST_MODEL = new DependencyListModel();
        }
        return INVALID_DEP_LIST_MODEL;
    }
    
    static ListCellRenderer/*<ModuleDependency|WAIT_VALUE>*/ getDependencyCellRenderer(boolean boldfaceApiModules) {
        return new DependencyListCellRenderer(boldfaceApiModules);
    }
    
    static ListCellRenderer/*<Project>*/ getModuleCellRenderer() {
        return new ProjectListCellRenderer();
    }
    static ListCellRenderer/*<ModuleEntry>*/ getModuleEntryCellRenderer() {
        return new ModuleEntryListCellRenderer();
    }
    
    static final class DependencyListModel extends AbstractListModel {
        
        private final Set<ModuleDependency> currentDeps;
        
        private boolean changed;
        private final boolean invalid;
        
        DependencyListModel(Set<ModuleDependency> deps, boolean sorted) {
            if (sorted) {
                currentDeps = new TreeSet<ModuleDependency>(ModuleDependency.LOCALIZED_NAME_COMPARATOR);
                currentDeps.addAll(deps);
            } else {
                currentDeps = deps;
            }
            invalid = false;
        }
        
        DependencyListModel() {
            currentDeps = Collections.emptySet();
            invalid = true;
        }
        
        public int getSize() {
            return invalid ? 1 : currentDeps.size();
        }
        
        public Object getElementAt(int i) {
            return invalid ? INVALID_PLATFORM : currentDeps.toArray()[i];
        }
        
        ModuleDependency getDependency(int i) {
            return (ModuleDependency) getElementAt(i);
        }
        
        void addDependency(ModuleDependency dep) {
            if (!currentDeps.contains(dep)) {
                int origSize = currentDeps.size();
                currentDeps.add(dep);
                changed = true;
                this.fireContentsChanged(this, 0, origSize);
            }
        }
        
        void removeDependencies(Collection<ModuleDependency> deps) {
            int origSize = currentDeps.size();
            currentDeps.removeAll(deps);
            changed = true;
            this.fireContentsChanged(this, 0, origSize);
        }
        
        void editDependency(ModuleDependency origDep, ModuleDependency newDep) {
            currentDeps.remove(origDep);
            currentDeps.add(newDep);
            changed = true;
            this.fireContentsChanged(this, 0, currentDeps.size());
        }
        
        Set<ModuleDependency> getDependencies() {
            return Collections.unmodifiableSet(currentDeps);
        }
        
        boolean isChanged() {
            return changed;
        }
    }
    
    private static final class DependencyListCellRenderer implements ListCellRenderer {
        
        private final HtmlRenderer.Renderer renderer = HtmlRenderer.createRenderer();
        private final boolean boldfaceApiModules;
        
        public DependencyListCellRenderer(boolean boldfaceApiModules) {
            this.boldfaceApiModules = boldfaceApiModules;
        }
        
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            String text;
            if (value == WAIT_VALUE) {
                text = WAIT_VALUE;
            } else if (value == INVALID_PLATFORM) {
                text = INVALID_PLATFORM;
                renderer.setHtml(true);
            } else {
                ModuleDependency md = (ModuleDependency) value;
                // XXX the following is wrong; spec requires additional logic:
                boolean bold = boldfaceApiModules && md.getModuleEntry().getPublicPackages().length > 0;
                boolean deprecated = md.getModuleEntry().isDeprecated();
                renderer.setHtml(bold || deprecated);
                String locName = md.getModuleEntry().getLocalizedName();
                text = locName;
                if (bold || deprecated) {
                    try {
                        text = "<html>" + (bold ? "<b>" : "") + (deprecated ? "<s>" : "") + XMLUtil.toElementContent(locName); // NOI18N
                    } catch (CharConversionException e) {
                        // forget it
                    }
                }
            }
            return renderer.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
        }
        
    }
    
    private static class ProjectListCellRenderer extends DefaultListCellRenderer {
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(
                    list, ProjectUtils.getInformation((Project) value).getDisplayName(),
                    index, isSelected, cellHasFocus);
            return c;
        }
        
    }
    
    private static class ModuleEntryListCellRenderer extends DefaultListCellRenderer {
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            ModuleEntry me = (ModuleEntry)value;
            Component c = super.getListCellRendererComponent(
                    list, me.getLocalizedName(), index, isSelected, cellHasFocus);
            return c;
        }
        
    }
    
    static final class PublicPackagesTableModel extends AbstractTableModel {
        
        private Boolean[] selected;
        private Boolean[] originalSelected;
        private String[] pkgNames;
        
        PublicPackagesTableModel(Map<String, Boolean> publicPackages) {
            reloadData(publicPackages);
        }
        
        void reloadData(Map<String, Boolean> publicPackages) {
            selected = new Boolean[publicPackages.size()];
            publicPackages.values().toArray(selected);
            if (originalSelected == null) {
                originalSelected = new Boolean[publicPackages.size()];
                System.arraycopy(selected, 0, originalSelected, 0, selected.length);
            }
            pkgNames = new String[publicPackages.size()];
            publicPackages.keySet().toArray(pkgNames);
            fireTableDataChanged();
        }
        
        public int getRowCount() {
            return pkgNames.length;
        }
        
        public int getColumnCount() {
            return 2;
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return selected[rowIndex];
            } else {
                return pkgNames[rowIndex];
            }
        }
        
        @Override
        public Class getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            assert columnIndex == 0 : "Who is trying to modify second column?"; // NOI18N
            selected[rowIndex] = (Boolean) aValue;
            fireTableCellUpdated(rowIndex, 0);
        }

        /**
         * Returns a (sorted) set of selected packages.
         * Set is newly created each time the method gets called
         * @return set of selected packages
         */
        Set<String> getSelectedPackages() {
            Set<String> s = new TreeSet<String>();
            for (int i = 0; i < pkgNames.length; i++) {
                if (selected[i]) {
                    s.add(pkgNames[i]);
                }
            }
            return s;
        }
        
        public boolean isChanged() {
            return !Arrays.asList(selected).equals(Arrays.asList(originalSelected));
        }
        
    }
    
    static final class FriendListModel extends AbstractListModel {
        
        private final Set<String> friends = new TreeSet<String>();
        
        private boolean changed;
        
        FriendListModel(String[] friends) {
            if (friends != null) {
                this.friends.addAll(Arrays.asList(friends));
            }
        }
        
        public Object getElementAt(int index) {
            if (index >= friends.size()) {
                return null;
            } else {
                return friends.toArray()[index];
            }
        }
        
        public int getSize() {
            return friends.size();
        }
        
        void addFriend(String friend) {
            friends.add(friend);
            changed = true;
            super.fireIntervalAdded(this, 0, friends.size());
        }
        
        void removeFriend(String friend) {
            friends.remove(friend);
            changed = true;
            super.fireIntervalRemoved(this, 0, friends.size());
        }
        
        Set<String> getFriends() {
            return Collections.unmodifiableSet(friends);
        }
        
        boolean isChanged() {
            return changed;
        }
    }
    
    static final class RequiredTokenListModel extends AbstractListModel {
        
        private final SortedSet<String> tokens;
        private boolean changed;
        
        RequiredTokenListModel(final SortedSet<String> tokens) {
            this.tokens = new TreeSet<String>(tokens);
        }
        
        public Object getElementAt(int index) {
            return index >= tokens.size() ? null : tokens.toArray()[index];
        }
        
        public int getSize() {
            return tokens.size();
        }
        
        void addToken(String token) {
            tokens.add(token);
            changed = true;
            super.fireIntervalAdded(this, 0, tokens.size());
        }
        
        void removeToken(String token) {
            tokens.remove(token);
            changed = true;
            super.fireIntervalRemoved(this, 0, tokens.size());
        }
        
        String[] getTokens() {
            String[] result = new String[tokens.size()];
            return tokens.toArray(result);
        }
        
        boolean isChanged() {
            return changed;
        }
        
    }
    
    static final class SuiteSubModulesListModel extends AbstractListModel {
        
        private final SortedSet<NbModuleProject> subModules;
        
        private boolean changed;
        
        SuiteSubModulesListModel(Set<NbModuleProject> subModules) {
            this.subModules = new TreeSet<NbModuleProject>(Util.projectDisplayNameComparator());
            this.subModules.addAll(subModules);
        }
        
        public int getSize() {
            return subModules.size();
        }
        
        public Object getElementAt(int i) {
            return subModules.toArray()[i];
        }
        
        boolean contains(Project module) {
            return subModules.contains(module);
        }
        
        void removeModules(Collection modules) {
            int origSize = subModules.size();
            subModules.removeAll(modules);
            changed = true;
            this.fireContentsChanged(this, 0, origSize);
        }
        
        boolean addModule(NbModuleProject module) {
            int origSize = subModules.size();
            boolean added = subModules.add(module);
            changed = true;
            this.fireContentsChanged(this, 0, origSize + 1);
            return added;
        }
        
        public Set<NbModuleProject> getSubModules() {
            return Collections.unmodifiableSortedSet(subModules);
        }
        
        public boolean isChanged() {
            return changed;
        }
    }
    
}
