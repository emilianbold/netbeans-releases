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
import org.netbeans.modules.projectimport.eclipse.EclipseUtils;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;

/**
 * Selection wizard panel for Eclipse Wizard importer.
 *
 * @author mkrauskopf
 */
final class SelectionWizardPanel extends ImporterWizardPanel implements
        PropertyChangeListener, WizardDescriptor.ValidatingPanel {
    
    private SelectionPanel panel;
    
    /** Creates a new instance of WorkspaceWizardPanel */
    SelectionWizardPanel() {
        panel = new SelectionPanel();
        panel.addPropertyChangeListener(this);
        initPanel(panel, 0);
    }
    
    public Component getComponent() {
        return panel;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if ("errorMessage".equals(propName)) { //NOI18N
            setErrorMessage((String) evt.getNewValue());
        } else if ("workspaceChoosen".equals(propName)) { // NOI18N
            String[] steps;
            if (((Boolean) evt.getNewValue()).booleanValue()) {
                steps = new String[] { WORKSPACE_LOCATION_STEP, PROJECTS_SELECTION_STEP };
            } else {
                steps = new String[] { PROJECT_SELECTION_STEP };
            }
            panel.putClientProperty("WizardPanel_contentData", steps); // NOI18N
        }
    }
    
    // ==== delegate methods ==== //

    boolean isWorkspaceChosen() {
        return panel.isWorkspaceChosen();
    }
    
    /** Returns project directory of single-selected project. */
    String getProjectDir() {
        return panel.getProjectDir();
    }
    
    /** Returns destination directory for single-selected project. */
    public String getProjectDestinationDir() {
        return panel.getProjectDestinationDir();
    }
    
    /** Returns workspace directory choosed by user. */
    public String getWorkspaceDir() {
        return panel.getWorkspaceDir();
    }
    
    public void validate() throws WizardValidationException {
        if (!panel.isWorkspaceChosen()) {
            String dest = getProjectDestinationDir();

            String message = null;
            if ((!new File(dest).isAbsolute()) || !EclipseUtils.isWritable(dest)) {
                message = ProjectImporterWizard.getMessage(
                        "MSG_CannotCreateProjectInFolder", dest); // NOI18N
            } else if (!EclipseUtils.isRegularJavaProject(getProjectDir())) {
                message = ProjectImporterWizard.getMessage(
                        "MSG_CannotImportNonJavaProject"); // NOI18N
            }
            if (message != null) {
                setErrorMessage(message);
                throw new WizardValidationException(panel, message, null);
            }
        }
    }
}
