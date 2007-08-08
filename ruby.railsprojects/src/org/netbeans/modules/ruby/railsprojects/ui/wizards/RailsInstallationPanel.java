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

package org.netbeans.modules.ruby.railsprojects.ui.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.modules.ruby.rubyproject.gems.Gem;
import org.netbeans.modules.ruby.rubyproject.gems.GemManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Wizard panel to handle easy/convenient Rails installations
 *
 * @author  Tor Norbye
 */
public class RailsInstallationPanel extends javax.swing.JPanel {
    private Panel firer;
    private WizardDescriptor wizardDescriptor;
    
    /** Creates new form RailsInstallationPanel */
    public RailsInstallationPanel(Panel panel) {
        initComponents();
        this.firer = panel;
        initComponents();
        this.setName(NbBundle.getMessage(RailsInstallationPanel.class,"LAB_InstallRails"));
        this.putClientProperty ("NewProjectWizard_Title", NbBundle.getMessage(RailsInstallationPanel.class,"TXT_RailsInstallation")); // NOI18N
        updateLabel();
        
        String gemProblem = GemManager.getGemProblem();
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
        if (RubyInstallation.getInstance().isValidRails(false)) {
            descLabel.setText(NbBundle.getMessage(RailsInstallationPanel.class, "RailsOk"));
            railsButton.setText(NbBundle.getMessage(RailsInstallationPanel.class, "UpdateRails"));
            String version = RubyInstallation.getInstance().getVersion("rails"); // NOI18N
            if (version == null) {
                version = "?";
            }
            installedLabel.setText(NbBundle.getMessage(RailsInstallationPanel.class, "RailsVersion", version));
        } else if (!RubyInstallation.getInstance().isValidRuby(false)) {
            descLabel.setText(NbBundle.getMessage(RailsInstallationPanel.class, "NoRuby"));
            railsButton.setText(NbBundle.getMessage(RailsInstallationPanel.class, "InstallRails"));
            installedLabel.setText("");
        } else {
            descLabel.setText(NbBundle.getMessage(RailsInstallationPanel.class, "NoRails"));
            railsButton.setText(NbBundle.getMessage(RailsInstallationPanel.class, "InstallRails"));
            installedLabel.setText("");
        }
    }
    
    void read (WizardDescriptor settings) {
        this.wizardDescriptor = settings;
    }
        
    boolean valid (WizardDescriptor settings) {
        if (!RubyInstallation.getInstance().isValidRuby(false)) {
            wizardDescriptor.putProperty( "WizardPanel_errorMessage", 
                    NbBundle.getMessage(RailsInstallationPanel.class, "NoRuby"));
            return false;
        }
        // Make sure we have Rails (and possibly openssl as well)
        String rails = RubyInstallation.getInstance().getRails();
        if (rails != null && !(new File(rails).exists())) {
            String msg = NbBundle.getMessage(RailsInstallationPanel.class, "NotFound", rails);
            wizardDescriptor.putProperty( "WizardPanel_errorMessage", msg);       //NOI18N
            return false;
        } else if (rails == null) {
            String msg = NbBundle.getMessage(RailsInstallationPanel.class, "NoRails");
            wizardDescriptor.putProperty( "WizardPanel_errorMessage", msg);       //NOI18N
            return false;
        }

        wizardDescriptor.putProperty( "WizardPanel_errorMessage","");   //NOI18N
        return true;
    }
    
    void validate (WizardDescriptor d) throws WizardValidationException {
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        railsButton = new javax.swing.JButton();
        sslButton = new javax.swing.JButton();
        descLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jrubyLabel = new javax.swing.JLabel();
        jrubySslLabel = new javax.swing.JLabel();
        proxyButton = new javax.swing.JButton();
        installedLabel = new javax.swing.JLabel();
        railsDetailButton = new javax.swing.JButton();

        FormListener formListener = new FormListener();

        railsButton.setText(org.openide.util.NbBundle.getMessage(RailsInstallationPanel.class, "InstallRails")); // NOI18N
        railsButton.addActionListener(formListener);

        sslButton.setText(org.openide.util.NbBundle.getMessage(RailsInstallationPanel.class, "RailsInstallationPanel.sslButton.text")); // NOI18N
        sslButton.addActionListener(formListener);

        descLabel.setText(org.openide.util.NbBundle.getMessage(RailsInstallationPanel.class, "NoRails")); // NOI18N

        jrubyLabel.setText(org.openide.util.NbBundle.getMessage(RailsInstallationPanel.class, "RailsInstallationPanel.jrubyLabel.text")); // NOI18N

        jrubySslLabel.setText(org.openide.util.NbBundle.getMessage(RailsInstallationPanel.class, "RailsInstallationPanel.jrubySslLabel.text")); // NOI18N

        proxyButton.setText(org.openide.util.NbBundle.getMessage(RailsInstallationPanel.class, "RailsInstallationPanel.proxyButton.text")); // NOI18N
        proxyButton.addActionListener(formListener);

        installedLabel.setText(org.openide.util.NbBundle.getMessage(RailsInstallationPanel.class, "RailsInstallationPanel.installedLabel.text")); // NOI18N

        railsDetailButton.setText(org.openide.util.NbBundle.getMessage(RailsInstallationPanel.class, "RailsInstallationPanel.railsDetailButton.text")); // NOI18N
        railsDetailButton.addActionListener(formListener);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(descLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(railsDetailButton))
                    .add(layout.createSequentialGroup()
                        .add(railsButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(installedLabel))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE)
                    .add(jrubyLabel)
                    .add(proxyButton)
                    .add(jrubySslLabel)
                    .add(sslButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(descLabel)
                    .add(railsDetailButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(railsButton)
                    .add(installedLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jrubyLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jrubySslLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(sslButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 63, Short.MAX_VALUE)
                .add(proxyButton)
                .addContainerGap())
        );
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
    String gemProblem = GemManager.getGemProblem();
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
    new GemManager().install(new Gem[] { gem }, this, null, false, false, null, true, true, asyncCompletionTask);
    
}//GEN-LAST:event_sslButtonActionPerformed

private class InstallationComplete implements Runnable {
    public void run() {
        RubyInstallation.getInstance().recomputeRoots();
        RailsInstallationPanel.this.updateLabel();
        RailsInstallationPanel.this.firer.fireChangeEvent();
        RubyInstallation.getInstance().recomputeRoots();
    }
}

private void railsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_railsButtonActionPerformed
    Runnable asyncCompletionTask = new InstallationComplete();
    Gem rails = new Gem("rails", null, null); // NOI18N
    Gem jdbc = new Gem("ActiveRecord-JDBC", null, null); // NOI18N
    Gem[] gems = new Gem[] { rails, jdbc };
    if (RubyInstallation.getInstance().isValidRails((false))) {
        // Already installed: update
        new GemManager().update(gems, this, null, false, false, true, asyncCompletionTask);
    } else {
        new GemManager().install(gems, this, null, false, false, null, true, true, asyncCompletionTask);
    }
}//GEN-LAST:event_railsButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descLabel;
    private javax.swing.JLabel installedLabel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel jrubyLabel;
    private javax.swing.JLabel jrubySslLabel;
    private javax.swing.JButton proxyButton;
    private javax.swing.JButton railsButton;
    private javax.swing.JButton railsDetailButton;
    private javax.swing.JButton sslButton;
    // End of variables declaration//GEN-END:variables
    
    static class Panel implements WizardDescriptor.ValidatingPanel {
        
        private ArrayList listeners;        
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
                this.listeners = new ArrayList ();
            }
            this.listeners.add (l);
        }

        public void readSettings(Object settings) {
            // Nothing to store/restore here
            this.settings = (WizardDescriptor) settings;
            this.component.read(this.settings);
        }

        public void storeSettings(Object settings) {
            // Nothing to store/restore here
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
