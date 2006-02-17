/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard;

import java.awt.Component;
import java.io.File;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

/**
 * First panel of <code>NewNbModuleWizardIterator</code>. Allow user to enter
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
        visualPanel.refreshData();
    }
    
    public void storeData() {
        visualPanel.storeData();
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
    
    public HelpCtx getHelp() {
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
                throw new WizardValidationException(visualPanel.nameValue, message, message);
            }
        }
    }
    
}
