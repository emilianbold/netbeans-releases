/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
    private int numberOfNeededProjects;
    
    public void start() {
        final EclipseWizardIterator iterator = new EclipseWizardIterator();
        final WizardDescriptor wizardDescriptor = new WizardDescriptor(iterator);
        iterator.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                wizardDescriptor.putProperty("WizardPanel_errorMessage",
                        iterator.getErrorMessage());
            }
        });
        wizardDescriptor.setTitleFormat(new java.text.MessageFormat(" {1}"));  // NOI18N
        wizardDescriptor.setTitle(NbBundle.getMessage(
                ProjectImporterWizard.class, "CTL_WizardTitle")); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.show();
        dialog.toFront();
        cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            projects = iterator.getProjects();
            destination = iterator.getDestination();
            numberOfNeededProjects = iterator.getNumberOfNeededProjects();
        }
    }
    
    public Set getProjects() {
        return projects;
    }
    
    public int getNumberOfNeededProjects() {
        return numberOfNeededProjects;
    }
    
    public String getDestination() {
        return destination;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
}
