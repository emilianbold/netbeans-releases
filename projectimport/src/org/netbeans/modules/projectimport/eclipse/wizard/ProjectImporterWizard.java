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

package org.netbeans.modules.projectimport.eclipse.wizard;

import java.awt.Dialog;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.projectimport.eclipse.EclipseProject;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
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
    private boolean recursively;
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
        dialog.setVisible(true);
        dialog.toFront();
        cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            projects = iterator.getProjects();
            showAdditionalInfo(projects);
            destination = iterator.getDestination();
            recursively = iterator.getRecursively();
            numberOfImportedProjects = iterator.getNumberOfImportedProject();
        }
    }
    
    private void showAdditionalInfo(Set projects) {
        StringBuffer messages = null;
        for (Iterator it = projects.iterator(); it.hasNext(); ) {
            EclipseProject prj = (EclipseProject) it.next();
            Set natures = prj.getOtherNatures();
            if (natures != null && !natures.isEmpty()) {
                if (messages == null) {
                    messages = new StringBuffer(
                            getMessage("MSG_CreatedByPlugin") + "\n\n"); // NOI18N
                }
                messages.append(" - " + prj.getName()); // NOI18N
            }
        }
        if (messages != null) {
            NotifyDescriptor d = new DialogDescriptor.Message(
                    messages.toString(), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
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
    
    public boolean getRecursively() {
        return recursively;
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
