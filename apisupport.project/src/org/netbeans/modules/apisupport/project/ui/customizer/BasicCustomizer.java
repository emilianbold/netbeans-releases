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

import java.awt.Component;
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
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;

/**
 * Convenient class to be used by {@link CustomizerProvider} implementations.
 *
 * @author Martin Krauskopf
 */
abstract class BasicCustomizer implements CustomizerProvider, PropertyChangeListener {
    
    static final String LAST_SELECTED_PANEL = "lastSelectedPanel"; // NOI18N
    
    /** Project <code>this</code> customizer customizes. */
    private final Project project;
    
    /** Keeps reference to a dialog representing <code>this</code> customizer. */
    private Dialog dialog;
    
    private ProjectCustomizer.CategoryComponentProvider panelProvider;
    private ProjectCustomizer.Category categories[];
    private Component lastSelectedPanel;
    
    private final Map/*<ProjectCustomizer.Category, JPanel>*/ panels = new HashMap();
    
    protected BasicCustomizer(final Project project) {
        this.project = project;
    }
    
    abstract void storeProperties() throws IOException;
    
    /**
     * Be sure that you will prepare all the data (typically subclass of {@link
     * ModuleProperties}) needed by a customizer and its panels and that the
     * data is always up-to-date after this method was called.
     */
    abstract void prepareData();
    
    protected void setCategories(ProjectCustomizer.Category[] categories) {
        this.categories = categories;
    }
    
    protected Project getProject() {
        return project;
    }
    
    protected Dialog getDialog() {
        return dialog;
    }
    
    /** Show customizer with the first category selected. */
    public void showCustomizer() {
        showCustomizer(null);
    }
    
    /** Show customizer with preselected category. */
    public void showCustomizer(String preselectedCategory) {
        showCustomizer(preselectedCategory, null);
    }
    
    public void showCustomizer(String preselectedCategory, String preselectedSubCategory) {
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setVisible(true);
            return;
        } else {
            prepareData();
            OptionListener listener = new OptionListener();
            if (preselectedCategory == null) {
                preselectedCategory = findLastSelectedCategory();
            }
            dialog = ProjectCustomizer.createCustomizerDialog(categories,
                    getPanelProvider(), preselectedCategory, listener,
                    new HelpCtx(getClass()));
            dialog.addWindowListener(listener);
            dialog.setTitle(NbBundle.getMessage(getClass(), "LBL_CustomizerTitle",
                    ProjectUtils.getInformation(getProject()).getDisplayName()));
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
    
    protected void createCategoryPanel(final String progName,
            final String displayNameKey, final JPanel panel) {
        ProjectCustomizer.Category cateogry = ProjectCustomizer.Category.create(
                progName, NbBundle.getMessage(getClass(), displayNameKey), null, null);
        panels.put(cateogry, panel);
    }
    
    /** Creates a category without subcategories. */
    protected ProjectCustomizer.Category createCategory(
            final String progName, final String displayNameKey) {
        return ProjectCustomizer.Category.create(
                progName, NbBundle.getMessage(getClass(), displayNameKey), null, null);
    }
    
    protected void createPanel(final Category category, final JPanel panel) {
        panels.put(category, panel);
    }
    
    protected void listenToPanels() {
        for (Iterator it = panels.values().iterator(); it.hasNext(); ) {
            ((Component) it.next()).addPropertyChangeListener(this);
        }
    }
    
    private ProjectCustomizer.Category findCategory(final Object panel) {
        for (Iterator it = panels.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            if (panel.equals(entry.getValue())) {
                return (ProjectCustomizer.Category) entry.getKey();
            }
        }
        throw new IllegalArgumentException(panel + " panel is not known in this customizer"); // NOI18N
    }
    
    public void save() {
        try {
            // Store properties
            Boolean result = (Boolean) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    storeProperties();
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
    
    /** Listens to the actions on the Customizer's option buttons */
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (propertyName == NbPropertyPanel.VALID_PROPERTY) {
            findCategory(evt.getSource()).setValid(((Boolean) evt.getNewValue()).booleanValue());
        } else if (propertyName == NbPropertyPanel.ERROR_MESSAGE_PROPERTY) {
            findCategory(evt.getSource()).setErrorMessage((String) evt.getNewValue());
        } else if (propertyName == BasicCustomizer.LAST_SELECTED_PANEL) {
            lastSelectedPanel = (Component) evt.getSource();
        }
    }
    
    private ProjectCustomizer.CategoryComponentProvider getPanelProvider() {
        if (panelProvider == null) {
            panelProvider = new ProjectCustomizer.CategoryComponentProvider() {
                public JComponent create(ProjectCustomizer.Category category) {
                    JComponent panel = (JComponent) panels.get(category);
                    return panel == null ? new JPanel() : panel;
                }
            };
            return panelProvider;
        }
        return panelProvider;
    }
    
    private String findLastSelectedCategory() {
        String preselectedCategory = null;
        for (Iterator it = panels.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            Component panel = (Component) entry.getValue();
            if (panel == lastSelectedPanel) {
                preselectedCategory = ((ProjectCustomizer.Category) entry.getKey()).getName();
                break;
            }
        }
        return preselectedCategory;
    }
    
    protected class OptionListener extends WindowAdapter implements ActionListener {
        
        // Listening to OK button ----------------------------------------------
        public void actionPerformed(ActionEvent e) {
            // Store the properties into project
            for (Iterator it = panels.values().iterator(); it.hasNext(); ) {
                Object panel = (Object) it.next();
                if (panel instanceof LazyStorage) {
                    ((LazyStorage) panel).store();
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
    
    /**
     * Implement this interface when you want your panel to be told that the
     * properties/customizer are going to be saved.
     */
    static interface LazyStorage {
        
        /** Called when user pressed <em>ok</em>. */
        void store();
        
    }
    
    static interface SubCategoryProvider {
        public void showSubCategory(String name);
    }
    
}

