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

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Set;
import org.netbeans.modules.projectimport.eclipse.EclipseUtils;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;

/**
 * Workspace panel for Eclipse Wizard importer.
 *
 * @author mkrauskopf
 */
final class ProjectWizardPanel extends ImporterWizardPanel implements
        PropertyChangeListener, WizardDescriptor.ValidatingPanel {
    
    private ProjectSelectionPanel panel;
    
    /** Creates a new instance of WorkspaceWizardPanel */
    ProjectWizardPanel() {
        panel = new ProjectSelectionPanel();
        panel.addPropertyChangeListener(this);
        initPanel(panel, 1);
    }
    
    public Component getComponent() {
        return panel;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if ("errorMessage".equals(evt.getPropertyName())) { //NOI18N
            setErrorMessage((String) evt.getNewValue());
        }
    }
    
    // ==== delegate methods ==== //
    Set getProjects() {
        return panel.getProjects();
    }
    
    int getNumberOfImportedProject() {
        return panel.getNumberOfImportedProject();
    }
    
    String getDestination() {
        return panel.getDestination();
    }
    
    void loadProjects(String workspaceDir) {
        panel.loadProjects(workspaceDir);
    }
    
    public void validate() throws WizardValidationException {
        String dest = panel.getDestination();
        if (!new File(dest).isAbsolute() || !EclipseUtils.isWritable(dest)) {
            String message = ProjectImporterWizard.getMessage(
                    "MSG_CannotCreateProjectInFolder", dest); // NOI18N
            setErrorMessage(message);
            throw new WizardValidationException(panel, message, null);
        }
    }
}
