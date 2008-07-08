/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.ruby.railsprojects.ui.wizards;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.gems.Gem;
import org.netbeans.modules.ruby.platform.gems.GemInfo;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.netbeans.modules.ruby.railsprojects.ui.wizards.RailsInstallationValidator.RailsInstallationInfo;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Wizard panel to handle easy/convenient Rails installations
 *
 * @author  Tor Norbye
 */
public class RailsInstallationPanel extends JPanel {

    private Panel firer;
    private WizardDescriptor wizardDescriptor;
    
    RailsInstallationPanel(Panel panel) {
        initComponents();
        this.firer = panel;
        initComponents();
        this.setName(NbBundle.getMessage(RailsInstallationPanel.class,"LAB_InstallRails"));
        this.putClientProperty ("NewProjectWizard_Title", NbBundle.getMessage(RailsInstallationPanel.class,"TXT_NewRoRApp")); // NOI18N
    }
    
    private void initRailsVersionComboBox() {
        List<GemInfo> gemInfos = platform().getGemManager().getVersions("rails"); //NOI18N
        int size = gemInfos.size();
        railsVersionComboBox.setEnabled(size > 0);
        railsVersionLabel.setEnabled(size > 0);
        
        String[] versions = new String[size];
        for (int i = 0; i < size; i++) {
            versions[i] = gemInfos.get(i).getVersion();
        }
        railsVersionComboBox.setModel(new DefaultComboBoxModel(versions));
    }
    
    private RubyPlatform platform() {
        return (RubyPlatform) wizardDescriptor.getProperty("platform");
    }
    
    private GemManager gemManager() {
        return platform().getGemManager();
    }
    
    private void updateGemProblem() {
        String gemProblem = gemManager().getGemProblem();
        if (gemProblem != null) {
            String msg = NbBundle.getMessage(RailsInstallationPanel.class,"GemProblem");
            descLabel.setText(msg);
            railsButton.setEnabled(false);
            sslButton.setEnabled(false);
        } else {
            railsDetailButton.setVisible(false);
        }
    }
    
    private void updateLabel() {
        RailsInstallationInfo railsInfo = RailsInstallationValidator.getRailsInstallation(platform());
        if (railsInfo.isValid()) {
            descLabel.setText(railsInfo.getMessage());
            Mnemonics.setLocalizedText(railsButton, org.openide.util.NbBundle.getMessage(RailsInstallationPanel.class, "UpdateRails")); // NOI18N
//            installedLabel.setText(NbBundle.getMessage(RailsInstallationPanel.class, "RailsVersion", railsInfo.getVersion()));
        } else {
            descLabel.setText(railsInfo.getMessage());
            Mnemonics.setLocalizedText(railsButton, org.openide.util.NbBundle.getMessage(RailsInstallationPanel.class, "InstallRails")); // NOI18N
//            installedLabel.setText("");
        }
    }
    
    void read (WizardDescriptor settings) {
        this.wizardDescriptor = settings;
        
        updateLabel();
        updateGemProblem();
        
        // In case user went back to the previous panel and changed the ruby settings
        updateGemProblem();
        initRailsVersionComboBox();

    }

    void store(WizardDescriptor settings) {
        String version = (String) railsVersionComboBox.getSelectedItem();
        String latest = platform().getGemManager().getLatestVersion("rails");
        // specify the version only if not using the latest version
        if (version != null && !version.equals(latest)) {
            settings.putProperty(NewRailsProjectWizardIterator.RAILS_VERSION, railsVersionComboBox.getSelectedItem());
        }
    }
    boolean valid (WizardDescriptor settings) {
        if (!platform().isValidRuby(false)) {
            wizardDescriptor.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE, 
                    NbBundle.getMessage(RailsInstallationPanel.class, "NoRuby"));
            return false;
        }
        
        RailsInstallationInfo railsInfo = RailsInstallationValidator.getRailsInstallation(platform());
        // Make sure we have Rails (and possibly openssl as well)
        if (!railsInfo.isValid()) {
            wizardDescriptor.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE, railsInfo.getMessage());       //NOI18N
            return false;
        } 

        wizardDescriptor.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE,"");   //NOI18N
        return true;
    }
    
    void validate (WizardDescriptor d) throws WizardValidationException {
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        railsButton = new javax.swing.JButton();
        sslButton = new javax.swing.JButton();
        descLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jrubyLabel = new javax.swing.JLabel();
        jrubySslLabel = new javax.swing.JLabel();
        proxyButton = new javax.swing.JButton();
        railsDetailButton = new javax.swing.JButton();
        railsVersionLabel = new javax.swing.JLabel();
        railsVersionComboBox = new javax.swing.JComboBox();

        FormListener formListener = new FormListener();

        org.openide.awt.Mnemonics.setLocalizedText(railsButton, org.openide.util.NbBundle.getMessage(RailsInstallationPanel.class, "InstallRails")); // NOI18N
        railsButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(sslButton, org.openide.util.NbBundle.getMessage(RailsInstallationPanel.class, "RailsInstallationPanel.sslButton.text")); // NOI18N
        sslButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(descLabel, org.openide.util.NbBundle.getMessage(RailsInstallationPanel.class, "NoRails")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jrubyLabel, org.openide.util.NbBundle.getMessage(RailsInstallationPanel.class, "RailsInstallationPanel.jrubyLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jrubySslLabel, org.openide.util.NbBundle.getMessage(RailsInstallationPanel.class, "RailsInstallationPanel.jrubySslLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(proxyButton, org.openide.util.NbBundle.getMessage(RailsInstallationPanel.class, "RailsInstallationPanel.proxyButton.text")); // NOI18N
        proxyButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(railsDetailButton, org.openide.util.NbBundle.getMessage(RailsInstallationPanel.class, "RailsInstallationPanel.railsDetailButton.text")); // NOI18N
        railsDetailButton.addActionListener(formListener);

        railsVersionLabel.setLabelFor(railsVersionComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(railsVersionLabel, org.openide.util.NbBundle.getMessage(RailsInstallationPanel.class, "RailsInstallationPanel.railsVersionLabel.text")); // NOI18N

        railsVersionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(proxyButton)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(railsVersionLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(railsVersionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 87, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(descLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(railsDetailButton)
                            .add(railsButton))
                        .add(162, 162, 162))
                    .add(jrubyLabel)
                    .add(jrubySslLabel)
                    .add(sslButton)
                    .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(descLabel)
                    .add(railsDetailButton))
                .add(13, 13, 13)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(railsVersionLabel)
                    .add(railsVersionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(railsButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(23, 23, 23)
                .add(jrubyLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jrubySslLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sslButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 53, Short.MAX_VALUE)
                .add(proxyButton)
                .addContainerGap())
        );

        railsButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RailsInstallationPanel.class, "RailsInstallationPanel.railsButton.AccessibleContext.accessibleDescription")); // NOI18N
        sslButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RailsInstallationPanel.class, "RailsInstallationPanel.sslButton.AccessibleContext.accessibleDescription")); // NOI18N
        proxyButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RailsInstallationPanel.class, "RailsInstallationPanel.proxyButton.AccessibleContext.accessibleDescription")); // NOI18N
        railsDetailButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RailsInstallationPanel.class, "RailsInstallationPanel.railsDetailButton.AccessibleContext.accessibleDescription")); // NOI18N
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == railsButton) {
                RailsInstallationPanel.this.railsButtonActionPerformed(evt);
            }
            else if (evt.getSource() == sslButton) {
                RailsInstallationPanel.this.sslButtonActionPerformed(evt);
            }
            else if (evt.getSource() == proxyButton) {
                RailsInstallationPanel.this.proxyButtonActionPerformed(evt);
            }
            else if (evt.getSource() == railsDetailButton) {
                RailsInstallationPanel.this.railsDetailButtonActionPerformed(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void railsDetailButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_railsDetailButtonActionPerformed
        String gemProblem = gemManager().getGemProblem();
        assert gemProblem != null;
        NotifyDescriptor nd =
            new NotifyDescriptor.Message(gemProblem, NotifyDescriptor.Message.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(nd);
    }//GEN-LAST:event_railsDetailButtonActionPerformed

    private void proxyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_proxyButtonActionPerformed
        OptionsDisplayer.getDefault().open("General"); // NOI18N
    }//GEN-LAST:event_proxyButtonActionPerformed

    private void sslButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sslButtonActionPerformed
        Runnable asyncCompletionTask = new InstallationComplete();
        Gem gem = new Gem("jruby-openssl", null, null); // NOI18N
        gemManager().install(new Gem[] { gem }, this, false, false, null, true, true, asyncCompletionTask);

    }//GEN-LAST:event_sslButtonActionPerformed

    private class InstallationComplete implements Runnable {
        public void run() {
            platform().recomputeRoots();
            RailsInstallationPanel.this.updateLabel();
            RailsInstallationPanel.this.firer.fireChangeEvent();
            platform().recomputeRoots();
            initRailsVersionComboBox();
        }
    }

    private void railsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_railsButtonActionPerformed
        Runnable asyncCompletionTask = new InstallationComplete();
        Gem rails = new Gem("rails", null, null); // NOI18N
        Gem jdbc = new Gem("activerecord-jdbc-adapter", null, null); // NOI18N
        Gem[] gems = platform().isJRuby() ? new Gem[]{rails, jdbc} : new Gem[]{rails};
        RailsInstallationInfo railsInfo = RailsInstallationValidator.getRailsInstallation(platform());
        if (railsInfo.getVersion() == null) {
            gemManager().install(gems, this, false, false, null, true, true, asyncCompletionTask);
        } else {
            // Already installed: update (with dependencies)
            gemManager().update(gems, this, false, false, true, true, asyncCompletionTask);
        }
    }//GEN-LAST:event_railsButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descLabel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel jrubyLabel;
    private javax.swing.JLabel jrubySslLabel;
    private javax.swing.JButton proxyButton;
    private javax.swing.JButton railsButton;
    private javax.swing.JButton railsDetailButton;
    private javax.swing.JComboBox railsVersionComboBox;
    private javax.swing.JLabel railsVersionLabel;
    private javax.swing.JButton sslButton;
    // End of variables declaration//GEN-END:variables

    static class Panel implements WizardDescriptor.ValidatingPanel {

        private ArrayList<ChangeListener> listeners;        
        private RailsInstallationPanel component;
        private WizardDescriptor settings;

        public synchronized void removeChangeListener(ChangeListener l) {
            if (this.listeners == null) {
                return;
            }
            this.listeners.remove(l);
        }

        public void addChangeListener(ChangeListener l) {
            if (this.listeners == null) {
                this.listeners = new ArrayList<ChangeListener>();
            }
            this.listeners.add (l);
        }

        public void readSettings(Object settings) {
            // Nothing to store/restore here
            this.settings = (WizardDescriptor) settings;
            this.component.read(this.settings);
            // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
            // this name is used in NewProjectWizard to modify the title
            Object substitute = component.getClientProperty ("NewProjectWizard_Title"); // NOI18N
            if (substitute != null) {
                this.settings.putProperty ("NewProjectWizard_Title", substitute); // NOI18N
            }
        }

        public void storeSettings(Object settings) {
            this.component.store((WizardDescriptor) settings);
        }

        public void validate() throws WizardValidationException {
            this.component.validate(this.settings);
        }

        public boolean isValid() {
            return this.component.valid (this.settings);
        }

        public synchronized java.awt.Component getComponent() {
            if (this.component == null) {
                this.component = new RailsInstallationPanel(this);
            }
            return this.component;
        }

        public HelpCtx getHelp() {
            return new HelpCtx (RailsInstallationPanel.class);
        }        

        private void fireChangeEvent () {
            Iterator it = null;
            synchronized (this) {
                if (this.listeners == null) {
                    return;
                }
                it = ((ArrayList)this.listeners.clone()).iterator();
            }
            ChangeEvent event = new ChangeEvent (this);
            while (it.hasNext()) {
                ((ChangeListener)it.next()).stateChanged(event);
            }
        }                
    }    
}
