/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hibernate.wizards;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.openide.WizardDescriptor;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;

/**
 *
 * @author gowri
 */
public class HibernateConfigurationWizardDescriptor implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel, ChangeListener {

    private HibernateConfigurationWizardPanel panel;
    private WizardDescriptor wizardDescriptor;
    private Project project;
    private static String ERROR_MSG_KEY = "WizardPanel_errorMessage";
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public HibernateConfigurationWizardDescriptor(Project project) {
        this.project = project;
    }

    public java.awt.Component getComponent() {
        if (panel == null) {
            panel = new HibernateConfigurationWizardPanel(this);
            panel.addChangeListener(this);
        }
        return panel;
    }

    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }

    public boolean isFinishPanel() {
        return isValid();
    }

    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    public boolean isValid() {
        if (wizardDescriptor == null) {
            return true;
        }
        if (panel != null && !panel.isValidPanel()) {
            try {
                
                if (!panel.isNameUnique()) {
                    wizardDescriptor.putProperty(ERROR_MSG_KEY,
                            NbBundle.getMessage(HibernateConfigurationWizard.class, "ERR_HibernateConfigurationNameNotUnique"));
                }
            } catch (Exception e) {
                wizardDescriptor.putProperty(ERROR_MSG_KEY,
                        NbBundle.getMessage(HibernateConfigurationWizard.class, "ERR_InvalidConfigurationXml"));
            }
            return false;
        }
        wizardDescriptor.putProperty(ERROR_MSG_KEY, " "); //NOI18N
        return true;
    }

    public void storeSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;       
        if (WizardDescriptor.PREVIOUS_OPTION.equals(wizardDescriptor.getValue())) {
            return;
        }
        wizardDescriptor.putProperty("NewFileWizard_Title", null); // NOI18N
    }

    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        project = Templates.getProject(wizardDescriptor);
        if (panel == null) {
            panel = (HibernateConfigurationWizardPanel) getComponent();
        }

        // Try to preselect a folder
        FileObject preSelectedTarget = Templates.getTargetFolder(wizardDescriptor);
        // Try to preserve already entered target name
        String targetName = Templates.getTargetName(wizardDescriptor);
        // Init values
        panel.initValues(Templates.getTemplate(wizardDescriptor), preSelectedTarget, targetName);

        wizardDescriptor.putProperty("WizardPanel_contentData", new String[]{
            NbBundle.getBundle(HibernateConfigurationWizardDescriptor.class).getString("LBL_TemplatesPanel_Name"), // NOI18N
            NbBundle.getBundle(HibernateConfigurationWizardDescriptor.class).getString("LBL_HibernateConfigurationPanel_Name")
        }); // NOI18N       

    }

    public HelpCtx getHelp() {
        return new HelpCtx(HibernateConfigurationWizardDescriptor.class);
    }

    Project getProject() {
        return project;
    }

    String getHibernateConfName() {
        return panel.getConfigurationFileName();
    }

    String getDialectName() {
        return panel == null ? null : panel.getSelectedDialect();
    }

    String getDriver() {
        return panel == null ? null : panel.getSelectedDriver();
    }

    String getURL() {
        return panel == null ? null : panel.getSelectedURL();
    }
}
