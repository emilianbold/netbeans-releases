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
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;

/**
 *
 * @author Adam Sotona
 */
public class ConfigurationsSelectionPanel implements WizardDescriptor.FinishablePanel {
    
    public static final String CONFIGURATION_TEMPLATES = "configuration_templates"; //NOI18N
    
    private ConfigurationsSelectionPanelGUI gui = null;
    
    public void readSettings(final Object settings) {
        final List l = (List)((TemplateWizard)settings).getProperty(CONFIGURATION_TEMPLATES);
        getComponent();
        gui.setSelectedTemplates(l == null ? Collections.EMPTY_LIST : l);
    }
    
    public void storeSettings(final Object settings) {
        getComponent();
        ((TemplateWizard)settings).putProperty(CONFIGURATION_TEMPLATES, gui.getSelectedTemplates());
    }
    
    public void addChangeListener(@SuppressWarnings("unused")
	final ChangeListener l) {
    }
    
    public void removeChangeListener(@SuppressWarnings("unused")
	final ChangeListener l) {
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
        return true;
    }
}
