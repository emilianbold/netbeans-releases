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
import org.netbeans.modules.apisupport.project.Util;
import org.openide.util.HelpCtx;

/**
 * Second panel of <code>NewNbModuleWizardIterator</code>. Allow user to enter
 * basic configuration:
 *
 * <ul>
 *  <li>Code Name Base</li>
 *  <li>Module Display Name</li>
 *  <li>Localizing Bundle</li>
 *  <li>XML Layer</li>
 * </ul>
 *
 * @author Martin Krauskopf
 */
final class BasicConfWizardPanel extends BasicWizardPanel.NewTemplatePanel {
    
    /** Representing visual component for this step. */
    private BasicConfVisualPanel visualPanel;
    
    /** Creates a new instance of BasicConfWizardPanel */
    public BasicConfWizardPanel(final NewModuleProjectData data) {
        super(data);
    }
    
    public void reloadData() {
        NewModuleProjectData data = getData();
        if (data.getCodeNameBase() == null) {
            String dotName = BasicConfVisualPanel.EXAMPLE_BASE_NAME + data.getProjectName();
            data.setCodeNameBase(Util.normalizeCNB(dotName));
        }
        if (data.getProjectDisplayName() == null) {
            data.setProjectDisplayName(data.getProjectName());
        }
        visualPanel.refreshData();
    }
    
    public void storeData() {
        visualPanel.storeData();
    }
    
    public Component getComponent() {
        if (visualPanel == null) {
            visualPanel = new BasicConfVisualPanel(getData());
            visualPanel.addPropertyChangeListener(this);
            visualPanel.setName(getMessage("LBL_BasicConfigPanel_Title"));
        }
        return visualPanel;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(BasicConfWizardPanel.class);
    }
    
}
