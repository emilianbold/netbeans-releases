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

package org.netbeans.modules.apisupport.project.ui.wizard;

import java.awt.Component;
import org.openide.WizardDescriptor;

/**
 * Second panel of <code>NewNbModuleWizardIterator</code>. Allow user to enter
 * basic configuration:
 *
 * <ul>
 *  <li>Code Name Base</li>
 *  <li>Module Display Name</li>
 *  <li>Localizing Bundle</li>
 *  <li>XML Layer</li>
 *  <li>NetBeans Platform (for standalone modules)</li>
 *  <li>Module Suite (for suite modules)</li>
 * </ul>
 *
 * @author mkrauskopf
 */
final class BasicConfWizardPanel extends BasicWizardPanel {
    
    /** Representing visual component for this step. */
    private BasicConfVisualPanel visualPanel;
    
    private WizardDescriptor settings;
    
    /** Creates a new instance of BasicConfWizardPanel */
    public BasicConfWizardPanel(WizardDescriptor settings) {
        this.settings = settings;
    }
    
    public void readSettings(Object settings) {
        visualPanel.refreshData();
    }
    
    public void storeSettings(Object settings) {
        visualPanel.storeData();
    }
    
    public Component getComponent() {
        if (visualPanel == null) {
            visualPanel = new BasicConfVisualPanel(settings);
            visualPanel.addPropertyChangeListener(this);
            visualPanel.setName(getMessage("LBL_BasicConfigPanel_Title")); // NOI18N
        }
        return visualPanel;
    }
}
