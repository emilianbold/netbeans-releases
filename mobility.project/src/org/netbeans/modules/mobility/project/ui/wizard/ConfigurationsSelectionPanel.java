/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
 * @author Adam Sotona, Petr Somol
 */
public class ConfigurationsSelectionPanel implements WizardDescriptor.FinishablePanel {
    
    public static final String CONFIGURATION_TEMPLATES = "configuration_templates"; // NOI18N
    
    private ConfigurationsSelectionPanelGUI gui = null;
    private boolean embedded;
    private TemplateWizard wiz;
    
    @Override
    public void readSettings(final Object settings) {
        wiz = (TemplateWizard)settings;
        embedded = wiz.getProperty(Utils.IS_EMBEDDED) == null ? false : (Boolean)wiz.getProperty(Utils.IS_EMBEDDED);
        Set s = (Set<ConfigurationTemplateDescriptor>)wiz.getProperty(CONFIGURATION_TEMPLATES);
        getComponent();
        gui.setSelectedTemplates(s == null ? new HashSet() : s);
    }
    
    @Override
    public void storeSettings(final Object settings) {
        getComponent();
        ((TemplateWizard)settings).putProperty(CONFIGURATION_TEMPLATES, gui.getSelectedTemplates());
    }
    
    @Override
    public void addChangeListener(final ChangeListener l) {
        getComponent();
        gui.addChangeListener(l);
    }
    
    @Override
    public void removeChangeListener(final ChangeListener l) {
        if (gui != null) {
            gui.removeChangeListener(l);
        }
    }
    
    @Override
    public Component getComponent() {
        if (gui == null) {
            gui = new ConfigurationsSelectionPanelGUI();
        }
        return gui;
    }
    
    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(ConfigurationsSelectionPanel.class.getName() + (embedded ? "Embedded" : "") ); // NOI18N
    }
    
    @Override
    public boolean isFinishPanel() {
        return true;
    }
    
    @Override
    public boolean isValid() {
        getComponent();
        boolean valid = gui.valid();
        if (wiz != null) {
            wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, valid ? null : NbBundle.getMessage(ConfigurationsSelectionPanel.class, "ERR_CfgSelPanel_NameCollision")); // NOI18N
        }
        return valid;
    }
}
