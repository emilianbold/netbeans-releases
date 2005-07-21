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
import org.openide.util.HelpCtx;

/**
 * First panel from <em>Adding New Platform</em> wizard panels. Allows user to
 * choose platform directory.
 *
 * @author Martin Krauskopf
 */
final class PlatformChooserWizardPanel extends BasicWizardPanel {
    
    /** Representing visual component for this step. */
    private PlatformChooserVisualPanel visualPanel;
    
    /** Creates a new instance of BasicInfoWizardPanel */
    public PlatformChooserWizardPanel(WizardDescriptor settings) {
        super(settings);
    }
    
    public void storeSettings(Object settings) {
        visualPanel.storeData();
    }
    
    public Component getComponent() {
        if (visualPanel == null) {
            visualPanel = new PlatformChooserVisualPanel(getSettings());
            visualPanel.addPropertyChangeListener(this);
        }
        return visualPanel;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(PlatformChooserWizardPanel.class);
    }
    
}
