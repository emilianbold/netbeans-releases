/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javacard.wizard;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalBorders.PaletteBorder;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.javacard.Installer;
import org.netbeans.modules.javacard.spi.JavacardPlatform;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author suchys
 */
public final class PlatformInstallerWizardPanel implements
        WizardDescriptor.Panel<WizardDescriptor>, PropertyChangeListener, ChangeListener {

    private ChangeSupport supp = new ChangeSupport(this);
    private boolean valid = validatePlatform();
    private JPanel component;
        
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new PlatformInstallPanel(this);
            component.setName(NbBundle.getMessage(
                    ProjectDefinitionWizardPanel.class,
                    "LBL_InstallPlatformStep")); //NOI18N
            String stepName = NbBundle.getMessage(PlatformInstallerWizardPanel.class,
                "WIZARD_STEP_INSTALL_PLATFORM"); //NOI18N
            // Sets step number of a component
            component.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(0)); //NOI18N
            // Turn on subtitle creation on each step
            component.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); //NOI18N
            // Show steps on the left side with the image on the background
            component.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE); //NOI18N
            // Turn on numbering of all steps
            component.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE); //NOI18N
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public void readSettings(WizardDescriptor settings) {        
        JavaPlatformManager.getDefault().addPropertyChangeListener(this);
        if(!validatePlatform()){
            Installer.install();
        }
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        JavaPlatformManager.getDefault().removePropertyChangeListener(this);
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    public final void addChangeListener(ChangeListener l) {
        supp.addChangeListener(l);
    }


    public final void removeChangeListener(ChangeListener l) {
        supp.removeChangeListener(l);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        validatePlatform();
    }

    private boolean validatePlatform() {
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
        for (JavaPlatform javaPlatform : platforms) {
            if (javaPlatform instanceof JavacardPlatform && ((JavacardPlatform)javaPlatform).isValid()) {
                valid = true;
                if (supp != null){                    
                    supp.fireChange();
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        supp.fireChange();
    }
    
    private static class PlatformInstallPanel extends JPanel {
        private JProgressBar jProgressBar;
        private JLabel jLabel;

        public PlatformInstallPanel(PlatformInstallerWizardPanel aThis) {
            initComponents();
        }

        private void initComponents() {
            java.awt.GridBagConstraints gridBagConstraints;

            jProgressBar = new javax.swing.JProgressBar();
            jLabel = new javax.swing.JLabel();

            setLayout(new java.awt.GridBagLayout());

            jProgressBar.setIndeterminate(true);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 1;
            gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.weighty = 1.0;
            add(jProgressBar, gridBagConstraints);

            jLabel.setLabelFor(jProgressBar);
            jLabel.setText(NbBundle.getMessage(ProjectDefinitionWizardPanel.class,
                    "LBL_InstallingPlatform")); //NOI18N
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
            add(jLabel, gridBagConstraints);            
        }
    }    
}
