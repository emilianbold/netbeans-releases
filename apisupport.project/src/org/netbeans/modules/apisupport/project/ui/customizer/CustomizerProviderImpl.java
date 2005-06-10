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
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.SuiteProvider;
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
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.spi.project.support.ant.PropertyUtils;

/**
 * Adding ability for a NetBeans modules to provide a GUI customizer.
 *
 * @author mkrauskopf
 */
public final class CustomizerProviderImpl implements CustomizerProvider {
    
    public static final ErrorManager err = ErrorManager.getDefault().getInstance(
            "org.netbeans.modules.apisupport.project.ui.customizer"); // NOI18N
    
    private final Project project;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final String locBundlePropsPath;
    private final boolean isStandalone;
    private ProjectXMLManager projectXMLManipulator;
    
    private Set/*<String>*/ modCategories;
    private Set/*<ModuleDependency>*/ universeDependencies;
    private Set/*<ModuleDependency>*/ moduleDependencies;
    
    /** package name / selected */
    private Map/*<String, Boolean>*/ publicPackages;
    
    private final Map/*<ProjectCustomizer.Category, JPanel>*/ panels = new HashMap();
    
    private NbModuleProperties moduleProps;
    private EditableProperties locBundleProps;
    
    private ProjectCustomizer.Category categories[];
    private ProjectCustomizer.CategoryComponentProvider panelProvider;
    
    // Keeps already displayed dialogs
    private static Map/*<Project,Dialog>*/ displayedDialogs = new HashMap();
    
    // models
    private ComponentFactory.DependencyListModel moduleDepsListModel;
    private ComponentFactory.DependencyListModel universeDepsListModel;
    private ComponentFactory.PublicPackagesTableModel publicPackagesModel;
    
    public CustomizerProviderImpl(Project project, AntProjectHelper helper,
            PropertyEvaluator evaluator, boolean isStandalone, String locBundlePropsPath) {
        this.project = project;
        this.helper = helper;
        this.evaluator = evaluator;
        this.isStandalone = isStandalone;
        this.locBundlePropsPath = locBundlePropsPath;
    }
    
    /**
     * Returns all known moduleDependencies in the project's universe.
     */
    private Set/*<ModuleDependency>*/ getModuleDependencies() {
        if (moduleDependencies == null) {
            try {
                moduleDependencies = getProjectXMLManipulator().getDirectDependencies();
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ioe);
            }
        }
        return moduleDependencies;
    }
    
    /**
     * Returns all the set of all available dependencies in the module's
     * universe.
     */
    private Set/*<ModuleDependency>*/ getUniverseDependencies() {
        // XXX may need to invalidate this cache in case a module has been added to a suite...
        if (universeDependencies == null) {
            loadModuleListInfo();
        }
        return universeDependencies;
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
                ModuleEntry me = (ModuleEntry) it.next();
                allDependencies.add(new ModuleDependency(me));
                String cat = me.getCategory();
                if (cat != null) {
                    allCategories.add(cat);
                }
            }
            modCategories = Collections.unmodifiableSortedSet(allCategories);
            universeDependencies = Collections.unmodifiableSortedSet(new TreeSet(allDependencies));
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    /** Show customizer with the first category selected. */
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
            this.moduleProps = new NbModuleProperties(helper, evaluator,
                    isStandalone, getProjectXMLManipulator().getCodeNameBase());
            // XXX may be temporary solution - there is not exact spec what should be done
            this.locBundleProps = locBundlePropsPath == null ?
                new EditableProperties() :
                helper.getProperties(locBundlePropsPath);
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
    private static final String VERSIONING = "Versioning"; // NOI18N

    private SuiteProvider getSuiteProvider() {
        return (SuiteProvider) project.getLookup().lookup(SuiteProvider.class);
    }
    
    private void init() {
        ResourceBundle bundle = NbBundle.getBundle(CustomizerProviderImpl.class);
        
        ProjectCustomizer.Category sources = createCategory(SOURCES,
                bundle.getString("LBL_ConfigSources")); // NOI18N
        ProjectCustomizer.Category display = createCategory(DISPLAY,
                bundle.getString("LBL_ConfigDisplay")); // NOI18N
        ProjectCustomizer.Category libraries = createCategory(LIBRARIES,
                bundle.getString("LBL_ConfigLibraries")); // NOI18N
        ProjectCustomizer.Category versioning = createCategory(VERSIONING,
                bundle.getString("LBL_ConfigVersioning")); // NOI18N
        
        categories = new ProjectCustomizer.Category[] {
            sources, display, libraries, versioning
        };
        
        // sources customizer
        panels.put(sources, new CustomizerSources(moduleProps, getSuiteProvider(),
                FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath()));
        
        // display customizer
        panels.put(display, new CustomizerDisplay(locBundleProps, getModuleCategories()));
        
        // libraries customizer
        moduleDepsListModel = ComponentFactory.createDependencyListModel(new TreeSet(getModuleDependencies()));
        universeDepsListModel = ComponentFactory.createDependencyListModel(getUniverseDependencies());
        panels.put(libraries, new CustomizerLibraries(moduleProps,
                moduleDepsListModel, universeDepsListModel));
        
        // versioning customizer
        publicPackagesModel = new ComponentFactory.PublicPackagesTableModel(
                getPublicPackages(Arrays.asList(getProjectXMLManipulator().getPublicPackages())));
        panels.put(versioning, new CustomizerVersioning(moduleProps, publicPackagesModel));
        
        panelProvider = new ProjectCustomizer.CategoryComponentProvider() {
            public JComponent create(ProjectCustomizer.Category category) {
                JComponent panel = (JComponent) panels.get(category);
                return panel == null ? new JPanel() : panel;
            }
        };
    }
    
    /** Creates a category without subcategories. */
    private ProjectCustomizer.Category createCategory(
            String progName, String displayName) {
        return ProjectCustomizer.Category.create(
                progName, displayName, null, null);
    }
    
    // XXX not done yet - also needs to consider <class-path-extension>
    private Map/*<String, Boolean>*/ getPublicPackages(
            Collection/*<String>*/ selectedPackages) {
        if (publicPackages == null) {
            Set/*<File>*/ pkgs = new TreeSet();
            File srcDir = helper.resolveFile(evaluator.getProperty("src.dir")); // NOI18N
            addNonEmptyPackages(pkgs, srcDir);
            publicPackages = new TreeMap();
            for (Iterator it = pkgs.iterator(); it.hasNext(); ) {
                File pkgDir = (File) it.next();
                String rel = PropertyUtils.relativizeFile(srcDir, pkgDir);
                String pkgName = rel.replace(File.separatorChar, '.');
                publicPackages.put(pkgName,
                        Boolean.valueOf(selectedPackages.contains(pkgName)));
            }
        }
        return publicPackages;
    }
    
    private void addNonEmptyPackages(Set/*<File>*/ pkgs, File root) {
        File[] kids = root.listFiles();
        boolean alreadyAdded = false;
        for (int i = 0; i < kids.length; i++) {
            File kid = kids[i];
            if (kid.isDirectory()) {
                addNonEmptyPackages(pkgs, kid);
            } else {
                if (!alreadyAdded && kid.getName().endsWith(".java")) { // NOI18N
                    pkgs.add(root);
                    alreadyAdded = true;
                }
            }
        }
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
            // Dispose the dialog otherwise the
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
                    if (locBundlePropsPath != null) {
                        helper.putProperties(locBundlePropsPath,  locBundleProps);
                    }
                    // store module dependencies
                    if (moduleDepsListModel.isChanged()) {
                        Set/*<ModuleDependency>*/ depsToSave =
                                new TreeSet(ModuleDependency.CODE_NAME_BASE_COMPARATOR);
                        depsToSave.addAll(moduleDepsListModel.getDependencies());
                        
                        // process removed modules
                        depsToSave.removeAll(moduleDepsListModel.getRemovedDependencies());
                        
                        // process added modules
                        depsToSave.addAll(moduleDepsListModel.getAddedDependencies());
                        
                        // process edited modules
                        Map/*<ModuleDependency, ModuleDependency>*/ toEdit
                                = moduleDepsListModel.getEditedDependencies();
                        for (Iterator it = toEdit.entrySet().iterator(); it.hasNext(); ) {
                            Map.Entry entry = (Map.Entry) it.next();
                            depsToSave.remove(entry.getKey());
                            depsToSave.add(entry.getValue());
                        }
                        getProjectXMLManipulator().replaceDependencies(depsToSave);
                    }
                    // XXX store only after real change
                    // store public packages
                    getProjectXMLManipulator().replacePublicPackages(
                            publicPackagesModel.getSelectedPackages());
                    return Boolean.TRUE;
                }
            });
            // and save the project
            if (result == Boolean.TRUE) {
                ProjectManager.getDefault().saveProject(project);
            }
            // reset
            this.publicPackages = null;
            this.moduleDependencies = null;
            this.projectXMLManipulator = null;
        } catch (MutexException e) {
            ErrorManager.getDefault().notify((IOException)e.getException());
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
}
