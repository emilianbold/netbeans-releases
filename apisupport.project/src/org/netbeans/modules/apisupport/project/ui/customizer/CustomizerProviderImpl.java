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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
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
 * Adding ability for a NetBeans modules to provide a GUI customizer.
 *
 * @author mkrauskopf
 */
public final class CustomizerProviderImpl implements CustomizerProvider, PropertyChangeListener {
    
    public static final ErrorManager err = ErrorManager.getDefault().getInstance(
            "org.netbeans.modules.apisupport.project.ui.customizer"); // NOI18N
    
    private final Project project;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final LocalizedBundleInfo bundleInfo;
    private final boolean isStandalone;
    
    private final Map/*<ProjectCustomizer.Category, JPanel>*/ panels = new HashMap();
    
    private SingleModuleProperties moduleProps;
    
    private ProjectCustomizer.Category categories[];
    private ProjectCustomizer.CategoryComponentProvider panelProvider;
    
    /** Keeps reference to a dialog for this customizer. */
    private Dialog dialog;
    
    public CustomizerProviderImpl(Project project, AntProjectHelper helper,
            PropertyEvaluator evaluator, boolean isStandalone,
            LocalizedBundleInfo bundleInfo) {
        this.project = project;
        this.helper = helper;
        this.evaluator = evaluator;
        this.isStandalone = isStandalone;
        this.bundleInfo = bundleInfo;
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
            if (moduleProps == null) { // first initialization
                moduleProps = new SingleModuleProperties(helper, evaluator,
                        getSuiteProvider(), isStandalone, bundleInfo);
                init();
            }
            moduleProps.refresh();
            OptionListener listener = new OptionListener();
            dialog = ProjectCustomizer.createCustomizerDialog(categories,
                    panelProvider, preselectedCategory, listener,
                    new HelpCtx(CustomizerProviderImpl.class));
            dialog.addWindowListener(listener);
            dialog.setTitle(NbBundle.getMessage(CustomizerProviderImpl.class, "LBL_CustomizerTitle",
                                                ProjectUtils.getInformation(project).getDisplayName()));
            dialog.setVisible(true);
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
        }
    }
    
    static interface SubCategoryProvider {
        public void showSubCategory(String name);
    }
    
    // Programmatic names of categories
    private static final String CATEGORY_SOURCES = "Sources"; // NOI18N
    private static final String CATEGORY_DISPLAY = "Display"; // NOI18N
    private static final String CATEGORY_LIBRARIES = "Libraries"; // NOI18N
    public static final String CATEGORY_VERSIONING = "Versioning"; // NOI18N
    public static final String SUBCATEGORY_VERSIONING_PUBLIC_PACKAGES = "publicPackages"; // NOI18N
    private static final String CATEGORY_BUILD = "Build"; // NOI18N
    private static final String CATEGORY_COMPILING = "Compiling"; // NOI18N
    private static final String CATEGORY_PACKAGING = "Packaging"; // NOI18N
    private static final String CATEGORY_DOCUMENTING = "Documenting"; // NOI18N

    private SuiteProvider getSuiteProvider() {
        return (SuiteProvider) project.getLookup().lookup(SuiteProvider.class);
    }
    
    private void init() {
        ResourceBundle bundle = NbBundle.getBundle(CustomizerProviderImpl.class);
        
        ProjectCustomizer.Category sources = createCategory(CATEGORY_SOURCES,
                bundle.getString("LBL_ConfigSources")); // NOI18N
        ProjectCustomizer.Category display = createCategory(CATEGORY_DISPLAY,
                bundle.getString("LBL_ConfigDisplay")); // NOI18N
        ProjectCustomizer.Category libraries = createCategory(CATEGORY_LIBRARIES,
                bundle.getString("LBL_ConfigLibraries")); // NOI18N
        ProjectCustomizer.Category versioning = createCategory(CATEGORY_VERSIONING,
                bundle.getString("LBL_ConfigVersioning")); // NOI18N

        ProjectCustomizer.Category compiling = createCategory(CATEGORY_COMPILING,
                bundle.getString("LBL_ConfigCompiling")); // NOI18N
        ProjectCustomizer.Category packaging = createCategory(CATEGORY_PACKAGING,
                bundle.getString("LBL_ConfigPackaging")); // NOI18N
        ProjectCustomizer.Category documenting = createCategory(CATEGORY_DOCUMENTING,
                bundle.getString("LBL_ConfigDocumenting")); // NOI18N
        ProjectCustomizer.Category build = ProjectCustomizer.Category.create(
                CATEGORY_BUILD,
                bundle.getString( "LBL_ConfigBuild" ), // NOI18N
                null,
                new ProjectCustomizer.Category[] {compiling, packaging, documenting}
        );

        
        categories = new ProjectCustomizer.Category[] {
            sources, display, libraries, versioning, build
        };
        
        // sources customizer
        panels.put(sources, new CustomizerSources(moduleProps));
        
        // display customizer
        panels.put(display, new CustomizerDisplay(moduleProps));
        
        // libraries customizer
        panels.put(libraries, new CustomizerLibraries(moduleProps));
        
        // versioning customizer
        CustomizerVersioning versioningPanel = new CustomizerVersioning(moduleProps);
        versioningPanel.addPropertyChangeListener(this);
        versioning.setValid(versioningPanel.isCustomizerValid());
        panels.put(versioning, versioningPanel);
        
        // compiling customizer
        panels.put(compiling, new CustomizerCompiling(moduleProps));
        
        // packaging customizer
        panels.put(packaging, new CustomizerPackaging(moduleProps));
        
        // documenting customizer
        panels.put(documenting, new CustomizerDocumenting(moduleProps));
        
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
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == NbPropertyPanel.VALID_PROPERTY) {
            findCategory(evt.getSource()).setValid(((Boolean) evt.getNewValue()).booleanValue());
        } else if (evt.getPropertyName() == NbPropertyPanel.ERROR_MESSAGE_PROPERTY) {
            findCategory(evt.getSource()).setErrorMessage((String) evt.getNewValue());
        }
    }
    
    private ProjectCustomizer.Category findCategory(Object panel) {
        for (Iterator it = panels.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            if (panel.equals(entry.getValue())) {
                return (ProjectCustomizer.Category) entry.getKey();
            }
        }
        throw new IllegalArgumentException(panel + " panel is not known in this customizer"); // NOI18N
    }
    
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
                    moduleProps.storeProperties();
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
