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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;

/**
 * Adding ability for a NetBeans Suite modules to provide a GUI customizer.
 *
 * @author Martin Krauskopf
 */
public final class SuiteCustomizer implements CustomizerProvider {
    
    public static final ErrorManager err = ErrorManager.getDefault().getInstance(
            "org.netbeans.modules.apisupport.project.ui.suite.customizer"); // NOI18N
    
    private final Project project;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    
    private final Map/*<ProjectCustomizer.Category, JPanel>*/ panels = new HashMap();
    
    private SuiteProperties suiteProps;
    private ProjectCustomizer.Category categories[];
    private ProjectCustomizer.CategoryComponentProvider panelProvider;
    
    /** Keeps reference to a dialog for this customizer. */
    private Dialog dialog;
    
    public SuiteCustomizer(Project project, AntProjectHelper helper,
            PropertyEvaluator evaluator) {
        this.project = project;
        this.helper = helper;
        this.evaluator = evaluator;
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
        if (dialog != null) {
            dialog.setVisible(true);
            return;
        } else {
            SubprojectProvider spp = (SubprojectProvider) project.getLookup().lookup(SubprojectProvider.class);
            Set/*<Project>*/ subModules = spp.getSubprojects();
            if (suiteProps == null) { // first initialization
                this.suiteProps = new SuiteProperties(project, helper, evaluator, subModules);
                init();
            }
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
                    panelProvider, preselectedCategory, listener,
                    new HelpCtx(SuiteCustomizer.class));
            dialog.addWindowListener(listener);
            dialog.setTitle(MessageFormat.format(
                    NbBundle.getMessage(SuiteCustomizer.class, "LBL_SuiteCustomizerTitle"), // NOI18N
                    new Object[] { ProjectUtils.getInformation(project).getDisplayName() }));
                    
            dialog.setVisible(true);
        }
    }
    
    static interface SubCategoryProvider {
        public void showSubCategory(String name);
    }
    
    // Programmatic names of categories
    private static final String SOURCES = "Sources"; // NOI18N
    private static final String LIBRARIES = "Libraries"; // NOI18N
    private static final String MODULE_LIST = "ModuleList"; // NOI18N
    private static final String BUILD = "Build"; // NOI18N    
    private static final String BASIC_BRANDING = "BasicBranding"; // NOI18N

    private void init() {
        ResourceBundle bundle = NbBundle.getBundle(SuiteCustomizer.class);
        
        ProjectCustomizer.Category sources = createCategory(SOURCES,
                bundle.getString("LBL_ConfigSources")); // NOI18N
        ProjectCustomizer.Category libraries = createCategory(LIBRARIES,
                bundle.getString("LBL_ConfigLibraries")); // NOI18N
        ProjectCustomizer.Category moduleList = createCategory(MODULE_LIST,
                bundle.getString("LBL_ConfigModuleList")); // NOI18N

        ProjectCustomizer.Category basicBranding = createCategory(BASIC_BRANDING,
                bundle.getString("LBL_BasicBranding")); // NOI18N
                
        ProjectCustomizer.Category build = ProjectCustomizer.Category.create(
                BUILD,
                bundle.getString( "LBL_Build" ), // NOI18N
                null,
                new ProjectCustomizer.Category[] {basicBranding}
        );
        
        
        
        
        categories = new ProjectCustomizer.Category[] {
            sources, libraries, moduleList, build
        };
        
        panels.put(sources, new SuiteCustomizerSources(suiteProps));
        panels.put(libraries, new SuiteCustomizerLibraries(suiteProps));
        panels.put(moduleList, new SuiteCustomizerModuleList(suiteProps));
        panels.put(basicBranding, new SuiteCustomizerBasicBranding(suiteProps));        
        
        
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
            
            // Close & dispose the dialog
            if (dialog != null) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
        
        // remove dialog for this customizer's project
        public void windowClosed(WindowEvent e) {
            dialog = null;
        }
        
        public void windowClosing(WindowEvent e) {
            // Dispose the dialog otherwise the
            // {@link WindowAdapter#windowClosed} may not be called
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
                    suiteProps.storeProperties();
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
}
