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
import java.util.Iterator;
import javax.swing.JDialog;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.ErrorManager;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Convenient class to be used by {@link CustomizerProvider} implementations.
 *
 * @author Martin Krauskopf
 */
abstract class BasicCustomizer implements CustomizerProvider {
    
    static final String LAST_SELECTED_PANEL = "lastSelectedPanel"; // NOI18N
    
    /** Project <code>this</code> customizer customizes. */
    private final Project project;
    
    /** Keeps reference to a dialog representing <code>this</code> customizer. */
    private Dialog dialog;
    
    private String lastSelectedCategory;
    
    
    private String layerPath;
    
    protected BasicCustomizer(final Project project, String path) {
        this.project = project;
        layerPath = path;
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
    abstract Lookup prepareData();
    
    abstract void dialogCleanup();
    
    
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
            Lookup context = prepareData();
            if (preselectedCategory == null) {
                preselectedCategory = lastSelectedCategory;
            }
            context = new ProxyLookup(context, Lookups.fixed(new SubCategoryProvider(preselectedCategory, preselectedSubCategory)));
            OptionListener listener = new OptionListener();
            dialog = ProjectCustomizer.createCustomizerDialog(layerPath, context, 
                    preselectedCategory, listener,
                    null);
            dialog.addWindowListener(listener);
            dialog.setTitle(NbBundle.getMessage(getClass(), "LBL_CustomizerTitle",
                    ProjectUtils.getInformation(getProject()).getDisplayName()));
            dialog.setVisible(true);
        }
    }
    
    
    public final void save() {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws IOException {
                    storeProperties();
                    ProjectManager.getDefault().saveProject(project);
                    return null;
                }
            });
        } catch (MutexException e) {
            ErrorManager.getDefault().notify((IOException)e.getException());
        }
    }
    
    private String findLastSelectedCategory() {
        if (dialog != null && dialog instanceof JDialog) {
            return (String)((JDialog)dialog).getRootPane().getClientProperty(BasicCustomizer.LAST_SELECTED_PANEL);
        }
        return null;
    }
    
    protected class OptionListener extends WindowAdapter implements ActionListener {
        
        // Listening to OK button ----------------------------------------------
        public void actionPerformed(ActionEvent e) {
            save();
        }
        
        // remove dialog for this customizer's project
        @Override
        public void windowClosed(WindowEvent e) {
            doClose();
        }
        
        @Override
        public void windowClosing(WindowEvent e) {
            // Dispose the dialog otherwise the
            // {@link WindowAdapter#windowClosed} may not be called
            doClose();
        }
        
        public void doClose() {
            if (dialog != null) {
                lastSelectedCategory = findLastSelectedCategory();
                dialog.removeWindowListener(this);
                dialog.setVisible(false);
                dialog.dispose();
                dialogCleanup();
            }
            dialog = null;
        }
        
    }
    

    
    static final class SubCategoryProvider {

        private String subcategory;

        private String category;

        SubCategoryProvider(String category, String subcategory) {
            this.category = category;
            this.subcategory = subcategory;
        }
        public String getCategory() {
            return category;
        }
        public String getSubcategory() {
            return subcategory;
        }
    }
    
}

