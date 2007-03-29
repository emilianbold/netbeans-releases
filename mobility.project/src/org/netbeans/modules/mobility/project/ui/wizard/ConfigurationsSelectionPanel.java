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

/*
 * ConfigurationsSelectionPanel.java
 *
 * Created on 17. kveten 2005, 14:40
 *
 */
package org.netbeans.modules.mobility.project.ui.wizard;

import java.awt.Component;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.mobility.cfgfactory.ProjectConfigurationFactory.ConfigurationTemplateDescriptor;
import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Adam Sotona
 */
public class ConfigurationsSelectionPanel implements WizardDescriptor.FinishablePanel {
    
    public static final String CONFIGURATION_TEMPLATES = "configuration_templates"; //NOI18N
    
    private ConfigurationsSelectionPanelGUI gui = null;
    private TemplateWizard wiz;
    
    public void readSettings(final Object settings) {
        wiz = (TemplateWizard)settings;
        Set s = (Set<ConfigurationTemplateDescriptor>)wiz.getProperty(CONFIGURATION_TEMPLATES);
        getComponent();
        gui.setSelectedTemplates(s == null ? new HashSet() : s);
    }
    
    public void storeSettings(final Object settings) {
        getComponent();
        ((TemplateWizard)settings).putProperty(CONFIGURATION_TEMPLATES, gui.getSelectedTemplates());
    }
    
    public void addChangeListener(final ChangeListener l) {
        getComponent();
        gui.addChangeListener(l);
    }
    
    public void removeChangeListener(final ChangeListener l) {
        if (gui != null) gui.removeChangeListener(l);
    }
    
    public Component getComponent() {
        if (gui == null) gui = new ConfigurationsSelectionPanelGUI();
        return gui;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(ConfigurationsSelectionPanel.class);
    }
    
    public boolean isFinishPanel() {
        return true;
    }
    
    public boolean isValid() {
        getComponent();
        boolean valid = gui.isValid();
        if (wiz != null) wiz.putProperty("WizardPanel_errorMessage", valid ? null : NbBundle.getMessage(ConfigurationsSelectionPanel.class, "ERR_CfgSelPanel_NameCollision")); // NOI18N
        return valid;
    }
}
