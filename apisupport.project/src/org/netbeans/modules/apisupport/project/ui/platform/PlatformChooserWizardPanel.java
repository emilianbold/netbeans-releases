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

package org.netbeans.modules.apisupport.project.ui.platform;

import java.awt.Component;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardPanel;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * First panel from <em>Adding New Platform</em> wizard panels. Allows user to
 * choose platform directory.
 *
 * @author Martin Krauskopf
 */
final class PlatformChooserWizardPanel extends BasicWizardPanel implements WizardDescriptor.FinishablePanel {
    
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
            visualPanel.setName(NbPlatformCustomizer.CHOOSER_STEP);
        }
        return visualPanel;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(PlatformChooserWizardPanel.class);
    }
    
    public boolean isFinishPanel() {
        return NbPlatform.isLabelValid((String) getSettings().getProperty(NbPlatformCustomizer.PLAF_LABEL_PROPERTY));
    }
    
}
