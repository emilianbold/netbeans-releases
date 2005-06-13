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

package org.netbeans.modules.apisupport.project.ui.platform;

import java.awt.Component;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardPanel;
import org.openide.WizardDescriptor;

/**
 * Second panel from <em>Adding New Platform</em> wizard panels. Allows user to
 * add additional info about a selected platform.
 *
 * @author Martin Krauskopf
 */
final class PlatformInfoWizardPanel extends BasicWizardPanel {
    
    /** Representing visual component for this step. */
    private PlatformInfoVisualPanel visualPanel;
    
    /** Creates a new instance of BasicInfoWizardPanel */
    public PlatformInfoWizardPanel(WizardDescriptor settings) {
        super(settings);
    }

    public void readSettings(Object settings) {
        ((PlatformInfoVisualPanel) getComponent()).refreshData();
    }
    
    public void storeSettings(Object settings) {
        ((PlatformInfoVisualPanel) getComponent()).storeData();
    }
    
    public Component getComponent() {
        if (visualPanel == null) {
            visualPanel = new PlatformInfoVisualPanel(getSettings());
            visualPanel.addPropertyChangeListener(this);
        }
        return visualPanel;
    }
}
