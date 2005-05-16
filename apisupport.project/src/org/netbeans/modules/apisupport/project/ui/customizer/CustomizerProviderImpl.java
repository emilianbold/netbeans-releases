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
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.ModuleList;
import org.netbeans.spi.project.SubprojectProvider;
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
    private Set/*<String>*/ modCategories;

    private NbModuleProperties moduleProps;
    private EditableProperties locBundleProps;
    
    private ProjectCustomizer.Category categories[];
    private ProjectCustomizer.CategoryComponentProvider panelProvider;
    
    // Option indexes
    private static final int OPTION_OK = 0;
    private static final int OPTION_CANCEL = OPTION_OK + 1;
    
    // Option command names
    private static final String COMMAND_OK = "OK"; // NOI18N
    private static final String COMMAND_CANCEL = "CANCEL"; // NOI18N
    
    private static Map/*<Project,Dialog>*/ project2Dialog = new HashMap();
    
    public CustomizerProviderImpl(Project project, AntProjectHelper helper,
            PropertyEvaluator evaluator, String locBundlePropsPath) {
        this.project = project;
        this.helper = helper;
        this.evaluator = evaluator;
        this.locBundlePropsPath = locBundlePropsPath;
    }
    
    /** Returns all known categories in the project's universe. */
    private Set getModuleCategories() {
        if (modCategories == null || modCategories.isEmpty()) {
            try {
                ModuleList ml = ModuleList.getModuleList(
                    FileUtil.toFile(project.getProjectDirectory()));
                TreeSet/*<String>*/ allCategories = new TreeSet();
                for (Iterator it = ml.getAllEntries().iterator(); it.hasNext(); ) {
                    String cat = ((ModuleList.Entry) it.next()).getCategory();
                    if (cat != null) {
                        allCategories.add(cat);
                    }
                }
                modCategories = Collections.unmodifiableSortedSet(allCategories);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return modCategories;
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
        Dialog dialog = (Dialog)project2Dialog.get(project);
        if (dialog != null) {
            dialog.setVisible(true);
            return;
        } else {
            this.moduleProps = new NbModuleProperties(helper);
            this.locBundleProps = helper.getProperties(locBundlePropsPath); // NOI18N
            init();
            OptionListener listener = new OptionListener();
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
            dialog = ProjectCustomizer.createCustomizerDialog(categories, 
                    panelProvider, preselectedCategory, listener, null);
            dialog.addWindowListener(listener);
            dialog.setTitle(MessageFormat.format(
                    NbBundle.getMessage(CustomizerProviderImpl.class, "LBL_CustomizerTitle"), // NOI18N
                    new Object[] { ProjectUtils.getInformation(project).getDisplayName() }));
                    
            project2Dialog.put(project, dialog);
            dialog.setVisible(true);
        }
    }
    
    static interface SubCategoryProvider {
        public void showSubCategory(String name);
    }
    
    // Names of categories
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

        Map/*<ProjectCustomizer.Category, JPanel>*/ panels = new HashMap();
        panels.put(sources, new CustomizerSources(moduleProps,
                FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath()));
        
        panels.put(display, new CustomizerDisplay(locBundleProps, getModuleCategories()));
        panels.put(libraries, new CustomizerLibraries(createSubModuleListModel()));
        panelProvider = new PanelProvider(panels);
    }
    
    public ProjectCustomizer.Category createCategory(
            String progName, String displayName) {
        return ProjectCustomizer.Category.create(
                progName, displayName, null, null);
    }
    
    private static class PanelProvider implements ProjectCustomizer.CategoryComponentProvider {
        private static final JPanel EMPTY_PANEL = new JPanel();
        
        private Map /*<Category,JPanel>*/ panels;
        
        PanelProvider(Map panels) {
            this.panels = panels;
        }
        
        public JComponent create(ProjectCustomizer.Category category) {
            JComponent panel = (JComponent) panels.get(category);
            return panel == null ? EMPTY_PANEL : panel;
        }
    }
    
    /** Listens to the actions on the Customizer's option buttons */
    private class OptionListener extends WindowAdapter implements ActionListener {

        // Listening to OK button ----------------------------------------------
        public void actionPerformed(ActionEvent e) {
            // Store the properties into project
            save();
            
            // Close & dispose the the dialog
            Dialog dialog = (Dialog)project2Dialog.get(project);
            if (dialog != null) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
        
        // Listening to window events ------------------------------------------
        public void windowClosed(WindowEvent e) {
            project2Dialog.remove(project);
        }
        
        public void windowClosing(WindowEvent e) {
            // Dispose the dialog otherwsie the 
            // {@link WindowAdapter#windowClosed} may not be called
            Dialog dialog = (Dialog)project2Dialog.get(project);
            if (dialog != null) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
    }
    
    public void save() {
        try {
            // Store properties
            Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    moduleProps.storeProperties();
                    helper.putProperties(locBundlePropsPath,  locBundleProps);
                    return Boolean.TRUE;
                }
            });
            // and save the project
            if (result == Boolean.TRUE) {
                ProjectManager.getDefault().saveProject(project);
            }
        } catch (MutexException e) {
            ErrorManager.getDefault().notify((IOException)e.getException());
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }

    // XXX simple info list - will move to clever dedicated (inner) class
    // XXX will be probably lazy loaded semi-cached model
    private DefaultListModel createSubModuleListModel() {
        SubprojectProvider spp = (SubprojectProvider) project.
            getLookup().lookup(SubprojectProvider.class);
        Set set = new TreeSet();
        for (Iterator it = spp.getSubprojects().iterator(); it.hasNext(); ) {
            Project subProject = (Project) it.next();
            ProjectInformation info = (ProjectInformation) subProject.
                getLookup().lookup(ProjectInformation.class);
            if (info != null) {
                set.add(info.getDisplayName());
            } else {
                err.log(ErrorManager.WARNING, "Project in " + project.getProjectDirectory() + // NOI18N
                    " doesn't register ProjectInformation in its lookup."); // NOI18N
                set.add(project.getProjectDirectory()); // better than nothing
            }
        }
        
        DefaultListModel modulesModel = new DefaultListModel();
        for (Iterator it = set.iterator(); it.hasNext(); ) {
            modulesModel.addElement(it.next());
        }
        return modulesModel;
    }
}
