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

package org.netbeans.modules.j2ee.common.project.ui;


import java.awt.Dialog;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public final class ProjectImportLocationWizardPanel implements WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel {

    public static final String SOURCE_ROOT = "sourceRoot"; //NOI18N
    
    private ProjectImportLocationPanel panel;
    private WizardDescriptor wizardDescriptor;

    private String buildFile;
    private Object j2eeModuleType;
    private String defaultNameFormatter;
    private String importLabel;
    private String name;
    private String title;
    
    public ProjectImportLocationWizardPanel (Object j2eeModuleType, String name, String title,
            String defaultNameFormatter, String importLabel) {
        this.j2eeModuleType = j2eeModuleType;
        this.defaultNameFormatter = defaultNameFormatter;
        this.importLabel = importLabel;
        this.name = name;
        this.title = title;
    }

    public java.awt.Component getComponent () {
        if (panel == null) {
            panel = new ProjectImportLocationPanel(j2eeModuleType, name, title, this, defaultNameFormatter, importLabel);
        }
        return panel;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(ProjectImportLocationWizardPanel.class);
    }

    public boolean isValid () {
        getComponent();
        return panel.valid(wizardDescriptor);
    }

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    protected void fireChangeEvent() {
        changeSupport.fireChange();
    }
    public void readSettings (Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;        
        panel.read(wizardDescriptor);

        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = ((JComponent) panel).getClientProperty("NewProjectWizard_Title"); //NOI18N
        if (substitute != null)
            wizardDescriptor.putProperty("NewProjectWizard_Title", substitute); //NOI18N
    }

    public void storeSettings (Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        panel.store(d);
        ((WizardDescriptor) d).putProperty ("NewProjectWizard_Title", null); //NOI18N
    }

    public String getBuildFile() {
        if (buildFile == null) {
            return GeneratedFilesHelper.BUILD_XML_PATH;
        }
        return buildFile;
    }

    private void setBuildFile(String buildFile) {
        this.buildFile = buildFile;
    }
    
    public void validate() throws WizardValidationException {
        File dirF = new File(panel.projectLocationTextField.getText());
        if (new File(dirF, getBuildFile()).exists()) {
            File bf = new File(dirF, getBuildFile());
            if (bf.exists()) {
                JButton ok = createButton(
                        "LBL_IW_Buildfile_OK", "ACS_IW_BuildFileDialog_OKButton_LabelMnemonic", //NOI18N
                        "LBL_IW_BuildFileDialog_OK_LabelMnemonic"); //NOI18N
                JButton cancel = createButton(
                        "LBL_IW_Buildfile_Cancel", "ACS_IW_BuildFileDialog_CancelButton_LabelMnemonic", //NOI18N
                        "LBL_IW_BuildFileDialog_Cancel_LabelMnemonic"); //NOI18N
                final ImportBuildfile ibf = new ImportBuildfile(bf, ok);
                DialogDescriptor descriptor = new DialogDescriptor(ibf,
                        NbBundle.getMessage(ProjectImportLocationWizardPanel.class, "LBL_IW_BuildfileTitle"), //NOI18N
                        true, new Object[]{ok, cancel}, DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN,
                        null, null);
                Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
                dialog.setVisible(true);
                if (descriptor.getValue() != ok) {
                    throw new WizardValidationException(panel.projectLocationTextField, "", "");
                }
                setBuildFile(ibf.getBuildName());
            }
        }
    }

    private JButton createButton(String labelId, String labelMnemonicId, String mnemonicId) {
        JButton button = new JButton(NbBundle.getMessage(ProjectImportLocationWizardPanel.class, labelId));
        button.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(ProjectImportLocationWizardPanel.class, labelMnemonicId));
        button.setMnemonic(NbBundle.getMessage(ProjectImportLocationWizardPanel.class, mnemonicId).charAt(0));
        return button;
    }

    public boolean isFinishPanel() {
        return false;
    }

}
