/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.ModuleList;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;

/**
 * Adding ability for a NetBeans modules to provide a GUI customizer.
 *
 * @author mkrauskopf
 */
public final class CustomizerProviderImpl implements CustomizerProvider {
    
    public static final ErrorManager err = ErrorManager.getDefault().getInstance(
        "org.netbeans.modules.apisupport.project.ui.customizer"); // NOI18N

    private static final JPanel EMPTY_PANEL = new JPanel();
    
    private final Project project;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final String locBundlePropsPath;
    private ProjectXMLManager projectXMLManipulator;
    
    private Set/*<String>*/ modCategories;
    private Set/*<ModuleList.Entry>*/ universeModules;
    private Set/*<ModuleList.Entry>*/ subModules;
    
    private final Map/*<ProjectCustomizer.Category, JPanel>*/ panels = new HashMap();
    
    private NbModuleProperties moduleProps;
    private EditableProperties locBundleProps;
    
    private ProjectCustomizer.Category categories[];
    private ProjectCustomizer.CategoryComponentProvider panelProvider;
    
    // Option indexes
    private static final int OPTION_OK = 0;
    
    // Keeps already displayed dialogs
    private static Map/*<Project,Dialog>*/ displayedDialogs = new HashMap();

    // models
    private ComponentFactory.DependencyListModel subModulesListModel;
    private ComponentFactory.DependencyListModel universeModulesListModel;

    public CustomizerProviderImpl(Project project, AntProjectHelper helper,
            PropertyEvaluator evaluator, String locBundlePropsPath) {
        this.project = project;
        this.helper = helper;
        this.evaluator = evaluator;
        this.locBundlePropsPath = locBundlePropsPath;
    }
    
    /** Returns all known subModules in the project's universe. */
    private Set/*<ModuleList.Entry>*/ getSubModules() {
        if (subModules == null) {
            try {
                subModules = getProjectXMLManipulator().getDirectDependencies();
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ioe);
            }
        }
        return subModules;
//        if (subModules == null) {
//            try {
//                SubprojectProvider spp = (SubprojectProvider) project.getLookup().
//                        lookup(SubprojectProvider.class);
//                Set/*<Project>*/ subProjects = spp.getSubprojects();
//                if (subProjects == null || subProjects.isEmpty()) {
//                    subModules = Collections.EMPTY_SET;
//                    return subModules;
//                }
//                
//                Map/*<String, ModuleList.Entry>*/ sortedModules = new TreeMap();
//                for (Iterator it = subProjects.iterator(); it.hasNext(); ) {
//                    Project subProject = (Project) it.next();
//                    ProjectInformation info = (ProjectInformation) subProject.
//                            getLookup().lookup(ProjectInformation.class);
//                    ModuleList.Entry me = getModuleList().getEntry(info.getName());
//                    // XXX should instead reset ModuleList and try again
//                    // -- see NbModuleProject constructor
//                    assert me != null : "Cannot find Entry for " + info.getName(); // NOI18N
//                    sortedModules.put(me.getLocalizedName(), me);
//                }
//                Set ordered = new LinkedHashSet(sortedModules.size());
//                for (Iterator it = sortedModules.values().iterator(); it.hasNext(); ) {
//                    ordered.add(it.next());
//                }
//                subModules = Collections.unmodifiableSet(ordered);
//            } catch (IOException e) {
//                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
//            }
//        }
//        return subModules;
    }
    
    /** Returns all known modules in the project's universe. */
    private Set/*<ModuleList.Entry>*/ getUniverseModules() {
        if (universeModules == null) {
            loadModuleListInfo();
        }
        return universeModules;
    }
    
    /** Returns all known categories in the project's universe. */
    private Set/*<String>*/ getModuleCategories() {
        if (modCategories == null) {
            loadModuleListInfo();
        }
        return modCategories;
    }
    
    private ModuleList getModuleList() throws IOException {
        return ModuleList.getModuleList(FileUtil.toFile(project.getProjectDirectory()));
    }
    
    /**
     * Prepare all ModuleList.Entries from this module's universe. Also prepare 
     * all categories.
     */
    private void loadModuleListInfo() {
        try {
            SortedSet/*<String>*/ allCategories = new TreeSet();
            SortedSet/*<ModuleDependency>*/ allDependencies = new TreeSet();
            for (Iterator it = getModuleList().getAllEntries().iterator(); it.hasNext(); ) {
                ModuleList.Entry me = (ModuleList.Entry) it.next();
                allDependencies.add(new ModuleDependency(me));
                String cat = me.getCategory();
                if (cat != null) {
                    allCategories.add(cat);
                }
            }
            modCategories = Collections.unmodifiableSortedSet(allCategories);
            universeModules = Collections.unmodifiableSortedSet(new TreeSet(allDependencies));
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    public void showCustomizer() {
        showCustomizer(null);
    }

    /** Show customizer with preselected category. */
    public void showCustomizer(String preselectedCategory) {
        showCustomizer(preselectedCategory, null);
    }
    
    /** Show customizer with preselected category and subcategory. */
    public void showCustomizer(String preselectedCategory, String preselectedSubCategory) {
        Dialog dialog = (Dialog) displayedDialogs.get(project);
        if (dialog != null) {
            dialog.setVisible(true);
            return;
        } else {
            this.moduleProps = new NbModuleProperties(helper);
            // XXX check if the locBundlePropsPath was found (i.e. != null)
            // what to do then --> UI spec should be updated?
            this.locBundleProps = helper.getProperties(locBundlePropsPath); // NOI18N
            init();
            if (preselectedCategory != null && preselectedSubCategory != null) {
                for (int i = 0; i < categories.length; i++) {
                    if (preselectedCategory.equals(categories[i].getName())) {
                        JComponent component = panelProvider.create(categories[i]);
                        if (component instanceof SubCategoryProvider) {
                            ((SubCategoryProvider)component).showSubCategory(
                                    preselectedSubCategory);
                        }
                        break;
                    }
                }
            }
            OptionListener listener = new OptionListener();
            dialog = ProjectCustomizer.createCustomizerDialog(categories, 
                    panelProvider, preselectedCategory, listener, null);
            dialog.addWindowListener(listener);
            dialog.setTitle(MessageFormat.format(
                    NbBundle.getMessage(CustomizerProviderImpl.class, "LBL_CustomizerTitle"), // NOI18N
                    new Object[] { ProjectUtils.getInformation(project).getDisplayName() }));
                    
            displayedDialogs.put(project, dialog);
            dialog.setVisible(true);
        }
    }
    
    static interface SubCategoryProvider {
        public void showSubCategory(String name);
    }
    
    // Programmatic names of categories
    private static final String SOURCES = "Sources"; // NOI18N
    private static final String DISPLAY = "Display"; // NOI18N
    private static final String LIBRARIES = "Libraries"; // NOI18N
    
    private void init() {
        ResourceBundle bundle = NbBundle.getBundle(CustomizerProviderImpl.class);
        
        ProjectCustomizer.Category sources = createCategory(SOURCES,
                bundle.getString("LBL_ConfigSources")); // NOI18N
        ProjectCustomizer.Category display = createCategory(DISPLAY,
                bundle.getString("LBL_ConfigDisplay")); // NOI18N
        ProjectCustomizer.Category libraries = createCategory(LIBRARIES,
                bundle.getString("LBL_ConfigLibraries")); // NOI18N

        categories = new ProjectCustomizer.Category[] {
            sources, display, libraries
        };
        
        // sources customizer
        panels.put(sources, new CustomizerSources(moduleProps,
            FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath()));
        
        // display customizer
        panels.put(display, new CustomizerDisplay(locBundleProps, getModuleCategories()));
        
        // libraries customizer
        subModulesListModel = ComponentFactory.createModuleListModel(new TreeSet(getSubModules()));
        universeModulesListModel = ComponentFactory.createModuleListModel(getUniverseModules());
        panels.put(libraries, new CustomizerLibraries(moduleProps,
                subModulesListModel, universeModulesListModel));

        panelProvider = new ProjectCustomizer.CategoryComponentProvider() {
            public JComponent create(ProjectCustomizer.Category category) {
                JComponent panel = (JComponent) panels.get(category);
                return panel == null ? EMPTY_PANEL : panel;
            }
        };
    }
    
    /** Creates a category without subcategories. */
    private ProjectCustomizer.Category createCategory(
            String progName, String displayName) {
        return ProjectCustomizer.Category.create(
                progName, displayName, null, null);
    }

    /** Listens to the actions on the Customizer's option buttons */
    private class OptionListener extends WindowAdapter implements ActionListener {

        // Listening to OK button ----------------------------------------------
        public void actionPerformed(ActionEvent e) {
            // Store the properties into project
            for (Iterator it = panels.values().iterator(); it.hasNext(); ) {
                Object panel = (Object) it.next();
                if (panel instanceof ComponentFactory.StoragePanel) {
                    ((ComponentFactory.StoragePanel) panel).store();
                }
            }
            save();
            
            // Close & dispose the the dialog
            Dialog dialog = (Dialog) displayedDialogs.get(project);
            if (dialog != null) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
        
        // remove dialog for this customizer's project
        public void windowClosed(WindowEvent e) {
            displayedDialogs.remove(project);
        }
        
        public void windowClosing(WindowEvent e) {
            // Dispose the dialog otherwsie the 
            // {@link WindowAdapter#windowClosed} may not be called
            Dialog dialog = (Dialog) displayedDialogs.get(project);
            if (dialog != null) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
    }
    
    private ProjectXMLManager getProjectXMLManipulator() {
        if (projectXMLManipulator == null) {
            projectXMLManipulator = new ProjectXMLManager(helper, project);
        }
        return projectXMLManipulator;
    }
    
    public void save() {
        try {
            // Store properties
            Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    moduleProps.storeProperties();
                    // store localized info
                    helper.putProperties(locBundlePropsPath,  locBundleProps);
                    // store module dependencies
                    if (subModulesListModel.isChanged()) {
                        ProjectXMLManager pxm = getProjectXMLManipulator();
                        
                        // process removed modules
                        Set toDelete = subModulesListModel.getRemovedDependencies();
                        Set cnbsToDelete = new HashSet(toDelete.size());
                        for (Iterator it = toDelete.iterator(); it.hasNext(); ) {
                            cnbsToDelete.add(((ModuleDependency) it.next()).
                                    getModuleEntry().getCodeNameBase());
                        }
                        // XXX should accept ModuleDependency collection
                        pxm.removeDependencies(cnbsToDelete);
                        
                        // process added modules
                        pxm.addDependencies(subModulesListModel.getAddedDependencies());
                        
                        // process edited modules
                        Map/*<ModuleDependency, ModuleDependency>*/ toEdit 
                                = subModulesListModel.getEditedDependencies();
                        for (Iterator it = toEdit.entrySet().iterator(); it.hasNext(); ) {
                            Map.Entry entry = (Map.Entry) it.next();
                            pxm.editDependency(
                                    (ModuleDependency) entry.getKey(), // orig
                                    (ModuleDependency) entry.getValue()); // new
                        }
                    }
                    return Boolean.TRUE;
                }
            });
            // and save the project
            if (result == Boolean.TRUE) {
                ProjectManager.getDefault().saveProject(project);
            }
            // reset
            this.subModules = null;
            this.projectXMLManipulator = null;
        } catch (MutexException e) {
            ErrorManager.getDefault().notify((IOException)e.getException());
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
}
