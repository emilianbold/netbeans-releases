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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.PlatformComponentFactory;
import org.netbeans.modules.ruby.platform.RubyPlatformCustomizer;
import org.netbeans.modules.ruby.railsprojects.server.RailsServerManager;
import org.netbeans.modules.ruby.rubyproject.Util;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;

public class PanelOptionsVisual extends SettingsPanel implements PropertyChangeListener {
    
//    private static boolean lastMainClassCheck = true; // XXX Store somewhere

    private PanelConfigureProject panel;
//    private boolean valid;
    
    public PanelOptionsVisual(PanelConfigureProject panel, int type) {
        this.panel = panel;
        initComponents();
        
        PlatformComponentFactory.addPlatformChangeListener(platforms, new PlatformComponentFactory.PlatformChangeListener() {
            public void platformChanged() {
                fireChangeEvent();
                initServerComboBox();
                initWarCheckBox();
            }
        });

        Util.preselectWizardPlatform(platforms);




        fireChangeEvent();
        switch (type) {
//            case NewRailsProjectWizardIterator.TYPE_LIB:
//                setAsMainCheckBox.setVisible( false );
//                createMainCheckBox.setVisible( false );
//                mainClassTextField.setVisible( false );
//                break;
            case NewRailsProjectWizardIterator.TYPE_APP:
                //createMainCheckBox.addActionListener( this );
                //createMainCheckBox.setSelected( lastMainClassCheck );
                //mainClassTextField.setEnabled( lastMainClassCheck );
                break;
            case NewRailsProjectWizardIterator.TYPE_EXT:
                setAsMainCheckBox.setVisible( true );
                //createMainCheckBox.setVisible( false );
                //mainClassTextField.setVisible( false );
                break;
        }
        
        initWarCheckBox();
        
        //this.mainClassTextField.getDocument().addDocumentListener( new DocumentListener () {
        //   
        //    public void insertUpdate(DocumentEvent e) {
        //        mainClassChanged ();
        //    }
        //    
        //    public void removeUpdate(DocumentEvent e) {
        //        mainClassChanged ();
        //    }
        //    
        //    public void changedUpdate(DocumentEvent e) {
        //        mainClassChanged ();
        //    }
        //    
        //});
    }

    public @Override void removeNotify() {
        Util.storeWizardPlatform(platforms);
        super.removeNotify();
    }

    private void initWarCheckBox() {
        RubyPlatform platform = getPlatform();
        boolean jruby = platform != null ? platform.isJRuby() : false;
        warCheckBox.setEnabled(jruby);
        if (!jruby) {
            warCheckBox.setSelected(false);
        }
    }
    
    public void propertyChange(PropertyChangeEvent event) {
        if ("roots".equals(event.getPropertyName())) {
            fireChangeEvent();
        }
        //if (PanelProjectLocationVisual.PROP_PROJECT_NAME.equals(event.getPropertyName())) {
        //    String newProjectName = NewRailsProjectWizardIterator.getPackageName((String) event.getNewValue());
        //    if (!Utilities.isJavaIdentifier(newProjectName)) {
        //        newProjectName = NbBundle.getMessage (PanelOptionsVisual.class, "TXT_PackageNameSuffix", newProjectName); 
        //    }
        //    this.mainClassTextField.setText (MessageFormat.format(
        //        NbBundle.getMessage (PanelOptionsVisual.class,"TXT_ClassName"), new Object[] {newProjectName}
        //    ));
        //}
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setAsMainCheckBox = new javax.swing.JCheckBox();
        jrubyUsedLabel = new javax.swing.JLabel();
        warCheckBox = new javax.swing.JCheckBox();
        rubyPlatformLabel = new javax.swing.JLabel();
        manageButton = new javax.swing.JButton();
        platforms = org.netbeans.modules.ruby.platform.PlatformComponentFactory.getRubyPlatformsComboxBox();
        serverLabel = new javax.swing.JLabel();
        serverComboBox = RailsServerManager.getServerComboBox(getPlatform());

        setPreferredSize(new java.awt.Dimension(226, 100));

        setAsMainCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(setAsMainCheckBox, org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("LBL_setAsMainCheckBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jrubyUsedLabel, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "UsingRuby")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(warCheckBox, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "WarFile")); // NOI18N

        rubyPlatformLabel.setLabelFor(platforms);
        org.openide.awt.Mnemonics.setLocalizedText(rubyPlatformLabel, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "RubyPlatformLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(manageButton, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "RubyHomeBrowse")); // NOI18N
        manageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageButtonActionPerformed(evt);
            }
        });

        platforms.setMinimumSize(new java.awt.Dimension(27, 19));
        platforms.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                platformsActionPerformed(evt);
            }
        });

        serverLabel.setLabelFor(serverComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(serverLabel, org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "PanelOptionsVisual.Server")); // NOI18N

        serverComboBox.setMinimumSize(new java.awt.Dimension(27, 19));
        serverComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverComboBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(setAsMainCheckBox)
                    .add(warCheckBox)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(serverLabel)
                            .add(rubyPlatformLabel))
                        .add(20, 20, 20)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(serverComboBox, 0, 349, Short.MAX_VALUE)
                            .add(platforms, 0, 349, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(manageButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                    .add(jrubyUsedLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(setAsMainCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rubyPlatformLabel)
                    .add(manageButton)
                    .add(platforms, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(serverLabel)
                    .add(serverComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(19, 19, 19)
                .add(jrubyUsedLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(warCheckBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setAsMainCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ACSN_setAsMainCheckBox")); // NOI18N
        setAsMainCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(PanelOptionsVisual.class).getString("ACSD_setAsMainCheckBox")); // NOI18N
        warCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_WarFile")); // NOI18N
        rubyPlatformLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_RubyPlatformLabel")); // NOI18N
        manageButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ASCN_RubyHomeBrowse")); // NOI18N
        manageButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ASCD_RubyHomeBrowse")); // NOI18N
        serverLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_Server")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSN_PanelOptionsVisual")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PanelOptionsVisual.class, "ACSD_PanelOptionsVisual")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void manageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageButtonActionPerformed
        RubyPlatformCustomizer.manage(platforms);
        
    }//GEN-LAST:event_manageButtonActionPerformed

    private void serverComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_serverComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_serverComboBoxActionPerformed

    private void platformsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_platformsActionPerformed
    }//GEN-LAST:event_platformsActionPerformed
    
    private void initServerComboBox(){
        RubyPlatform platform = getPlatform();
        if (platform != null) {
            serverComboBox.setModel(new RailsServerManager.ServerListModel(getPlatform()));
        } else {
            serverComboBox.setModel(new DefaultComboBoxModel(new Object[]{}));
        }
    }
    
    RubyPlatform getPlatform() {
        return PlatformComponentFactory.getPlatform(platforms);
    }
    
    boolean valid(WizardDescriptor settings) {
        if (PlatformComponentFactory.getPlatform(platforms) == null) {
            return false;
        }
//        if (warCheckBox.isSelected() && !getPlatform().isJRuby()) {
//            settings.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE, 
//                    NbBundle.getMessage(PanelOptionsVisual.class, "JRubyRequired") ); //NOI18N
//            return false;
//        }
        //if (mainClassTextField.isVisible () && mainClassTextField.isEnabled ()) {
        //    if (!valid) {
        //        settings.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
        //            NbBundle.getMessage(PanelOptionsVisual.class,"ERROR_IllegalMainClassName")); //NOI18N
        //    }
        //    return this.valid;
        //}
        //else {
            return true;
        //}
    }
    
    void read(WizardDescriptor d) {
        // XXX
//        RubyInstallation.getInstance().addPropertyChangeListener(this);
    }
    
    void validate (WizardDescriptor d) throws WizardValidationException {
        // nothing to validate
    }

    void store(WizardDescriptor d) {
        d.putProperty( /*XXX Define somewhere */ "setAsMain", setAsMainCheckBox.isSelected() && setAsMainCheckBox.isVisible() ? Boolean.TRUE : Boolean.FALSE ); // NOI18N
        d.putProperty(NewRailsProjectWizardIterator.GOLDSPIKE_WN, warCheckBox.isSelected() && warCheckBox.isVisible() ? Boolean.TRUE : Boolean.FALSE ); // NOI18N
        d.putProperty(NewRailsProjectWizardIterator.PLATFORM, platforms.getModel().getSelectedItem());
        d.putProperty(NewRailsProjectWizardIterator.SERVER_INSTANCE, serverComboBox.getModel().getSelectedItem());
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jrubyUsedLabel;
    private javax.swing.JButton manageButton;
    private javax.swing.JComboBox platforms;
    private javax.swing.JLabel rubyPlatformLabel;
    private javax.swing.JComboBox serverComboBox;
    private javax.swing.JLabel serverLabel;
    private javax.swing.JCheckBox setAsMainCheckBox;
    private javax.swing.JCheckBox warCheckBox;
    // End of variables declaration//GEN-END:variables
    
    //private void mainClassChanged () {
    //    
    //    String mainClassName = this.mainClassTextField.getText ();
    //    StringTokenizer tk = new StringTokenizer (mainClassName, "."); //NOI18N
    //    boolean valid = true;
    //    while (tk.hasMoreTokens()) {
    //        String token = tk.nextToken();
    //        if (token.length() == 0 || /* !Utilities.isJavaIdentifier(token)*/ token.equals(" ")) {
    //            valid = false;
    //            break;
    //        }            
    //    }
    //    this.valid = valid;
    //    fireChangeEvent();
    //}
    
    private void fireChangeEvent() {
        this.panel.fireChangeEvent();
    }
    
}
