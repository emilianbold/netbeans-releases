/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.Component;
import java.io.CharConversionException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
            NbBundle.getMessage(CustomizerDisplay.class, "ComponentFactory_please_wait");
    
    /** Generally usable in conjuction with {@link #createComboEmptyModel}. */
    public static final String EMPTY_VALUE =
            NbBundle.getMessage(CustomizerDisplay.class, "LBL_Empty");
    
    static DependencyListModel INVALID_DEP_LIST_MODEL;
    
    private static final String INVALID_PLATFORM =
            "<html><font color=\"!nb.errorForeground\">&lt;" // NOI18N
            + NbBundle.getMessage(CustomizerComponentFactory.class, "MSG_InvalidPlatform")
            + "&gt;</font></html>"; // NOI18N
    
    private CustomizerComponentFactory(final Project project) {
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
    static CustomizerComponentFactory.DependencyListModel createDependencyListModel(
            final Set/*<ModuleDependency>*/ deps) {
        assert deps != null;
        return new CustomizerComponentFactory.DependencyListModel(deps);
    }
    
    /**
     * Creates a list model for a set of module dependencies.
     * The dependencies will be left in the order given.
     */
    static CustomizerComponentFactory.DependencyListModel createDependencyListModel(
            final List/*<ModuleDependency>*/ deps) {
        assert deps != null;
        return new CustomizerComponentFactory.DependencyListModel(deps);
    }
    
    static CustomizerComponentFactory.DependencyListModel getInvalidDependencyListModel() {
        if (INVALID_DEP_LIST_MODEL == null) {
            Set s = new HashSet();
            s.add(CustomizerComponentFactory.INVALID_PLATFORM);
            INVALID_DEP_LIST_MODEL = createDependencyListModel(s);
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
        
        private final Collection/*<ModuleDependency>*/ currentDeps;
        private Set/*<ModuleDependency>*/ addedDeps = new HashSet();
        private Set/*<ModuleDependency>*/ removedDeps = new HashSet();
        private Map/*<ModuleDependency, ModuleDependency>*/ editedDeps = new HashMap();
        
        private boolean changed;
        
        DependencyListModel(Set/*<ModuleDependency>*/ deps) {
            currentDeps = new TreeSet(ModuleDependency.LOCALIZED_NAME_COMPARATOR);
            currentDeps.addAll(deps);
        }
        
        DependencyListModel(List/*<ModuleDependency>*/ deps) {
            currentDeps = deps;
        }
        
        public int getSize() {
            return currentDeps.size();
        }
        
        public Object getElementAt(int i) {
            return currentDeps.toArray()[i];
        }
        
        ModuleDependency getDependency(int i) {
            return (ModuleDependency) getElementAt(i);
        }
        
        void addDependency(ModuleDependency dep) {
            int origSize = currentDeps.size();
            currentDeps.add(dep);
            boolean added = addedDeps.add(dep);
            assert added : "It shouldnt be possible to add the same " + // NOI18N
                    "module dependency twice!"; // NOI18N
            removedDeps.remove(dep); // be sure it won't get removed
            changed = true;
            this.fireContentsChanged(this, 0, origSize);
        }
        
        void removeDependencies(Collection deps) {
            int origSize = currentDeps.size();
            currentDeps.removeAll(deps);
            for (Iterator it = deps.iterator(); it.hasNext(); ) {
                Object entry = it.next();
                if (!addedDeps.remove(entry)) {
                    removedDeps.add(entry);
                }
            }
            changed = true;
            this.fireContentsChanged(this, 0, origSize);
        }
        
        void editDependency(ModuleDependency origDep, ModuleDependency newDep) {
            editedDeps.put(origDep, newDep);
            currentDeps.remove(origDep);
            currentDeps.add(newDep);
            changed = true;
            this.fireContentsChanged(this, 0, currentDeps.size());
        }
        
        Set/*<ModuleDependency>*/ getDependencies() {
            return new HashSet(currentDeps);
        }
        
        Set/*<ModuleDependency>*/ getRemovedDependencies() {
            return removedDeps;
        }
        
        Set/*<ModuleDependency>*/ getAddedDependencies() {
            return addedDeps;
        }
        
        Map/*<ModuleDependency, ModuleDependency>*/ getEditedDependencies() {
            return editedDeps;
        }
        
        /**
         * Tries to find if a given dependency has already been edited. If yes,
         * returns the edited counterpart; <code>null</code> otherwise.
         */
        ModuleDependency findEdited(ModuleDependency toFind) {
            for (Iterator it = editedDeps.values().iterator(); it.hasNext(); ) {
                ModuleDependency curr = (ModuleDependency) it.next();
                if (curr.getModuleEntry().getCodeNameBase().equals(
                        toFind.getModuleEntry().getCodeNameBase())) {
                    return curr;
                }
            }
            return null;
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
                boolean bold = boldfaceApiModules && (
                        // XXX the following is wrong; spec requires additional logic:
                        md.getModuleEntry().getPublicPackages() != null &&
                        md.getModuleEntry().getPublicPackages().length > 0);
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
        
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(
                    list, ProjectUtils.getInformation((Project) value).getDisplayName(),
                    index, isSelected, cellHasFocus);
            return c;
        }
        
    }
    
    private static class ModuleEntryListCellRenderer extends DefaultListCellRenderer {
        
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
        
        PublicPackagesTableModel(Map/*<String, Boolean>*/ publicPackages) {
            reloadData(publicPackages);
        }
        
        void reloadData(Map/*<String, Boolean>*/ publicPackages) {
            selected = new Boolean[publicPackages.size()];
            publicPackages.values().toArray(selected);
            originalSelected = new Boolean[publicPackages.size()];
            System.arraycopy(selected, 0, originalSelected, 0, selected.length);
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
        
        public Class getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }
        
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            assert columnIndex == 0 : "Who is trying to modify second column?"; // NOI18N
            selected[rowIndex] = (Boolean) aValue;
            fireTableCellUpdated(rowIndex, 0);
        }
        
        String[] getSelectedPackages() {
            Set s = new TreeSet();
            for (int i = 0; i < pkgNames.length; i++) {
                if (Boolean.TRUE == selected[i]) {
                    s.add(pkgNames[i]);
                }
            }
            String[] result = new String[s.size()];
            s.toArray(result);
            return result;
        }
        
        public boolean isChanged() {
            return !Arrays.asList(selected).equals(Arrays.asList(originalSelected));
        }
        
    }
    
    static final class FriendListModel extends AbstractListModel {
        
        private Set/*<String>*/ friends = new TreeSet();
        
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
        
        String[] getFriends() {
            String[] result = new String[friends.size()];
            return (String[]) friends.toArray(result);
        }
        
        boolean isChanged() {
            return changed;
        }
    }
    
    static final class RequiredTokenListModel extends AbstractListModel {
        
        private SortedSet/*<String>*/ tokens;
        private boolean changed;
        
        RequiredTokenListModel(final SortedSet tokens) {
            this.tokens = new TreeSet(tokens);
        }
        
        public Object getElementAt(int index) {
            return tokens.toArray()[index];
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
            return (String[]) tokens.toArray(result);
        }
        
        boolean isChanged() {
            return changed;
        }
        
    }
    
    static final class SuiteSubModulesListModel extends AbstractListModel {
        
        private Set/*<Project>*/ subModules;
        
        private boolean changed;
        
        SuiteSubModulesListModel(Set/*<Project>*/ subModules) {
            this.subModules = new TreeSet(Util.projectDisplayNameComparator());
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
        
        boolean addModule(Project module) {
            int origSize = subModules.size();
            boolean added = subModules.add(module);
            changed = true;
            this.fireContentsChanged(this, 0, origSize + 1);
            return added;
        }
        
        public Set/*<Project>*/ getSubModules() {
            return subModules;
        }
        
        public boolean isChanged() {
            return changed;
        }
    }
    
}
