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
package org.netbeans.modules.compapp.casaeditor.api;

import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.compapp.projects.jbi.api.InternalProjectTypePlugin;
import org.netbeans.modules.compapp.projects.jbi.api.InternalProjectTypePluginWizardIterator;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;

/**
 * Integrates Composite Application Project Type Plugins into
 * the CASA palette.
 * 
 * @author jsandusky
 */
public class InternalProjectTypePalettePlugin 
implements CasaPalettePlugin {

    private InternalProjectTypePlugin mProjectTypePlugin;
    
    
    public InternalProjectTypePalettePlugin(InternalProjectTypePlugin projectTypePlugin) {
        mProjectTypePlugin = projectTypePlugin;
    }

    
    public CasaPaletteItemID[] getItemIDs() {
        CasaPaletteItemID itemID = new CasaPaletteItemID(
                this,
                mProjectTypePlugin.getCategoryName(),
                mProjectTypePlugin.getPluginName(),
                mProjectTypePlugin.getIconFileBase());
        return new CasaPaletteItemID[] { itemID };
    }

    public REGION getDropRegion(CasaPaletteItemID itemID) {
        return CasaPalettePlugin.REGION.JBI_MODULES;
    }

    public void handleDrop(PluginDropHandler dropHandler, CasaPaletteItemID itemID) throws IOException {
        InternalProjectTypePluginWizardIterator wizardIterator = mProjectTypePlugin.getWizardIterator();
        WizardDescriptor descriptor = new WizardDescriptor(wizardIterator);
        Project project = null;
        
        // Set up project name and location
        String projectName = getProjectCount(
                ProjectChooser.getProjectsFolder(),
                mProjectTypePlugin.getCategoryName() + "_" +
                mProjectTypePlugin.getPluginName());
        File projectFolder = new File(ProjectChooser.getProjectsFolder(), projectName);
        descriptor.putProperty(WizardPropertiesTemp.PROJECT_DIR, projectFolder);
        descriptor.putProperty(WizardPropertiesTemp.NAME, projectName);
        descriptor.putProperty(WizardPropertiesTemp.J2EE_LEVEL, "1.4");
        descriptor.putProperty(WizardPropertiesTemp.SET_AS_MAIN, new Boolean(false));
        
        if (wizardIterator.hasContent()) {
            descriptor.setModal(true);
            Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setVisible(true);
        } else {
            wizardIterator.instantiate();
        }
        
        project = wizardIterator.getProject();
        if (project != null) {
            dropHandler.addInternalJBIModule(project);
        }
    }
    
    private String getProjectCount(File parentFolder, String baseName) {
        File file = null;
        int baseCount = 0;
        do {
            baseCount++;
            file = new File(parentFolder, baseName + baseCount);
        } while (file.exists());
        return file.getName();
    }

}
