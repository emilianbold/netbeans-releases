/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
    
    private final Map/*<ProjectCustomizer.Category, NbPropertyPanel>*/ panels = new HashMap();
    
    protected BasicCustomizer(final Project project) {
        this.project = project;
    }
    
    /**
     * All changes should be store at this point. Is called under the write
     * access from {@link ProjectManager#mutex}.
     */
    abstract void storeProperties() throws IOException;
    
    /**
     * Gives a chance to do some work after all the changes in a customizer
     * were successfully saved. Is called under the write access from {@link
     * ProjectManager#mutex}.
     */
    abstract void postSave() throws IOException;
    
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
    
    /** Show customizer with the first category selected. */
    public void showCustomizer() {
        showCustomizer(null);
    }
    
    /** Show customizer with preselected category. */
    public void showCustomizer(String preselectedCategory) {
        showCustomizer(preselectedCategory, null);
    }
    
    public void showCustomizer(String preselectedCategory, String preselectedSubCategory) {
        if (dialog != null) {
            dialog.setVisible(true);
            return;
        } else {
            prepareData();
            OptionListener listener = new OptionListener();
            if (preselectedCategory == null) {
                preselectedCategory = findLastSelectedCategory();
            }
            if (categories == null) {
                // Error interrupted some previous call to prepareData() -> init()?
                return;
            }
            dialog = ProjectCustomizer.createCustomizerDialog(categories,
                    getPanelProvider(), preselectedCategory, listener,
                    null);
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
        // check panels validity - gives them a chance to set an error message or a warning
        for (Iterator it = panels.values().iterator(); it.hasNext();) {
            NbPropertyPanel panel = (NbPropertyPanel) it.next();
            panel.checkForm();
        }
    }
    
    protected void createCategoryPanel(final String progName,
            final String displayNameKey, final NbPropertyPanel panel) {
        ProjectCustomizer.Category category = ProjectCustomizer.Category.create(
                progName, NbBundle.getMessage(getClass(), displayNameKey), null, null);
        createPanel(category, panel);
    }
    
    /** Creates a category without subcategories. */
    protected ProjectCustomizer.Category createCategory(
            final String progName, final String displayNameKey) {
        return ProjectCustomizer.Category.create(
                progName, NbBundle.getMessage(getClass(), displayNameKey), null, null);
    }
    
    protected void createPanel(final Category category, final NbPropertyPanel panel) {
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
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    storeProperties();
                    ProjectManager.getDefault().saveProject(project);
                    postSave();
                    return null;
                }
            });
        } catch (MutexException e) {
            ErrorManager.getDefault().notify((IOException)e.getException());
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
        }
        
        // remove dialog for this customizer's project
        public void windowClosed(WindowEvent e) {
            doClose();
        }
        
        public void windowClosing(WindowEvent e) {
            // Dispose the dialog otherwise the
            // {@link WindowAdapter#windowClosed} may not be called
            doClose();
        }
        
        public void doClose() {
            if (dialog != null) {
                dialog.removeWindowListener(this);
                dialog.setVisible(false);
                dialog.dispose();
            }
            dialog = null;
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

