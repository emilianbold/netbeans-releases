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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard;

import java.awt.Component;
import java.io.File;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

/**
 * First panel of <code>NewNbModuleWizardIterator</code>. Allows user to enter
 * basic module information:
 *
 * <ul>
 *  <li>Project name</li>
 *  <li>Project Location</li>
 *  <li>Project Folder</li>
 *  <li>If should be set as a Main Project</li>
 *  <li>NetBeans Platform (for standalone modules)</li>
 *  <li>Module Suite (for suite modules)</li>
 * </ul>
 *
 * @author Martin Krauskopf
 */
final class BasicInfoWizardPanel extends BasicWizardPanel.NewTemplatePanel implements WizardDescriptor.ValidatingPanel {
    
    /** Representing visual component for this step. */
    private BasicInfoVisualPanel visualPanel;
    
    /** Creates a new instance of BasicInfoWizardPanel */
    public BasicInfoWizardPanel(final NewModuleProjectData data) {
        super(data);
    }
    
    public void reloadData() {
        getVisualPanel().refreshData();
    }
    
    public void storeData() {
        getVisualPanel().storeData();
    }
    
    private BasicInfoVisualPanel getVisualPanel() {
        return (BasicInfoVisualPanel) getComponent();
    }
    
    public Component getComponent() {
        if (visualPanel == null) {
            visualPanel = new BasicInfoVisualPanel(getData());
            visualPanel.addPropertyChangeListener(this);
            visualPanel.setName(getMessage("LBL_BasicInfoPanel_Title"));
            visualPanel.updateAndCheck();
        }
        return visualPanel;
    }
    
    public @Override HelpCtx getHelp() {
        return new HelpCtx(BasicInfoWizardPanel.class.getName() + "_" + getWizardTypeString());
    }
    
    public void validate() throws WizardValidationException {
        // XXX this is little strange. Since this method is called first time the panel appears.
        // So we have to do this null check (data are uninitialized)
        String prjFolder = getData().getProjectFolder();
        if (prjFolder != null) {
            File prjFolderF = new File(prjFolder);
            if (prjFolderF.mkdir()) {
                prjFolderF.delete();
            } else {
                String message = getMessage("MSG_UnableToCreateProjectFolder");
                throw new WizardValidationException(getVisualPanel().nameValue, message, message);
            }
        }
    }
    
}
