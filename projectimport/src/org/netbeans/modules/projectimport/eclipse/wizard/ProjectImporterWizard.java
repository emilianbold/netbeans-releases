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

package org.netbeans.modules.projectimport.eclipse.wizard;

import java.awt.Dialog;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * Wizard for importing Eclipse project.
 *
 * @author mkrauskopf
 */
public final class ProjectImporterWizard {
    
    private Set projects;
    private String destination;
    private boolean cancelled;
    private int numberOfImportedProjects;
    
    /** Starts Eclipse importer wizard. */
    public void start() {
        final EclipseWizardIterator iterator = new EclipseWizardIterator();
        final WizardDescriptor wizardDescriptor = new WizardDescriptor(iterator);
        iterator.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                wizardDescriptor.putProperty("WizardPanel_errorMessage", // NOI18N
                        iterator.getErrorMessage());
            }
        });
        wizardDescriptor.setTitleFormat(new java.text.MessageFormat("{1}")); // NOI18N
        wizardDescriptor.setTitle(
                ProjectImporterWizard.getMessage("CTL_WizardTitle")); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.show();
        dialog.toFront();
        cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            projects = iterator.getProjects();
            destination = iterator.getDestination();
            numberOfImportedProjects = iterator.getNumberOfImportedProject();
        }
    }
    
    /** Returns project selected by user with the help of the wizard. */
    public Set getProjects() {
        return projects;
    }
    
    /**
     * Returns number of projects which will be imported (including both
     * required and selected projects)
     */
    public int getNumberOfImportedProject() {
        return numberOfImportedProjects;
    }
    
    /**
     * Returns destination directory where new NetBeans projects will be stored.
     */
    public String getDestination() {
        return destination;
    }
    
    /**
     * Returns whether user canceled the wizard.
     */
    public boolean isCancelled() {
        return cancelled;
    }
    
    /** Gets message from properties bundle for this package. */
    static String getMessage(String key) {
        return NbBundle.getMessage(ProjectImporterWizard.class, key);
    }
    
    /** Gets message from properties bundle for this package. */
    static String getMessage(String key, Object param1) {
        return NbBundle.getMessage(ProjectImporterWizard.class, key, param1);
    }
}
