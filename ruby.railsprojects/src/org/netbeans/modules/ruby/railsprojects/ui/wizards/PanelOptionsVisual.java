/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.PlatformComponentFactory;
import org.netbeans.modules.ruby.platform.RubyPlatformCustomizer;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.netbeans.modules.ruby.railsprojects.server.RailsServerUiUtils;
import org.netbeans.modules.ruby.rubyproject.Util;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class PanelOptionsVisual extends SettingsPanel implements PropertyChangeListener {
    
    private final PanelConfigureProject panel;
    /**
     * Keeps track of platforms for which local gems have already been reloaded 
     */
    private final Set<RubyPlatform> reloaded = new HashSet<RubyPlatform>();
    
    public PanelOptionsVisual(PanelConfigureProject panel) {
        this.panel = panel;
        initComponents();
        
        PlatformComponentFactory.addPlatformChangeListener(platforms, new PlatformComponentFactory.PlatformChangeListener() {
            @Override
            public void platformChanged() {
                initServerComboBox();
                fireChangeEvent();
                initWarCheckBox();
                reloadLocalGems();
            }
        });

        Util.preselectWizardPlatform(platforms);

        fireChangeEvent();
        initWarCheckBox();
        initServerComboBox();
        reloadLocalGems();
    }

    public @Override void removeNotify() {
        Util.storeWizardPlatform(platforms);
        super.removeNotify();
    }
    private void reloadLocalGems() {
        // reload local gems to pick up external changes in gems
        RubyPlatform platform = getPlatform();
        if (platform != null && !reloaded.contains(platform)) {
            GemManager gemManager = platform.getGemManager();
            if (gemManager != null) {
                gemManager.reloadLocalGems(true);
                reloaded.add(platform);
            }
        }
    }


    private void initWarCheckBox() {
        warCheckBox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                fireChangeEvent();
            }
        });
    }
    
    public void propertyChange(PropertyChangeEvent event) {
        if ("roots".equals(event.getPropertyName())) {
            fireChangeEvent();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        warCheckBox = new javax.swing.JCheckBox();
        rubyPlatformLabel = new javax.swing.JLabel();
        manageButton = new javax.swing.JButton();
        platforms = org.netbeans.modules.ruby.platform.PlatformComponentFactory.getRubyPlatformsComboxBox();
        serverLabel = new javax.swing.JLabel();
        serverComboBox = RailsServerUiUtils.getServerComboBox(getPlatform());

        setPreferredSize(new java.awt.Dimension(226, 100));

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
                    .add(serverLabel)
                    .add(rubyPlatformLabel))
                .add(20, 20, 20)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(serverComboBox, 0, 223, Short.MAX_VALUE)
                    .add(platforms, 0, 223, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(manageButton)
                .add(0, 0, 0))
            .add(layout.createSequentialGroup()
                .add(warCheckBox)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rubyPlatformLabel)
                    .add(manageButton)
                    .add(platforms, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(serverLabel)
                    .add(serverComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(warCheckBox)
                .addContainerGap())
        );

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
            RailsServerUiUtils.ServerListModel model = new RailsServerUiUtils.ServerListModel(getPlatform());
            RailsServerUiUtils.addDefaultGlassFishGem(model, platform);
            serverComboBox.setModel(model);
        } else {
            serverComboBox.setModel(new DefaultComboBoxModel(new Object[]{}));
        }
        serverComboBox.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                fireChangeEvent();
            }
        });
    }

    RubyPlatform getPlatform() {
        return PlatformComponentFactory.getPlatform(platforms);
    }

    boolean needWarSupport() {
        return warCheckBox.isSelected();
    }
    
    Object getServer() {
        return serverComboBox.getSelectedItem();
    }

    boolean valid(WizardDescriptor settings) {
        settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, "");
        if (warCheckBox.isSelected() && !isJdk()) {
            settings.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, NbBundle.getMessage(PanelOptionsVisual.class, "MSG_NoJDK"));
        }
        if (RailsServerUiUtils.isGlassFishGem(getServer()) && !isJdk6()) {
            settings.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(PanelOptionsVisual.class, "MSG_GfGemRequiresJDK6"));
            return false;
        }
        if (PlatformComponentFactory.getPlatform(platforms) == null) {
            return false;
        }
        return true;
    }
    
    void read(WizardDescriptor d) {
    }
    
    void validate (WizardDescriptor d) throws WizardValidationException {
        // nothing to validate
    }

    void store(WizardDescriptor d) {
        d.putProperty( /*XXX Define somewhere */ "setAsMain", Boolean.FALSE); // NOI18N
        d.putProperty(NewRailsProjectWizardIterator.WAR_SUPPORT, warCheckBox.isSelected() && warCheckBox.isVisible() ? Boolean.TRUE : Boolean.FALSE ); // NOI18N
        d.putProperty(NewRailsProjectWizardIterator.PLATFORM, platforms.getModel().getSelectedItem());
        d.putProperty(NewRailsProjectWizardIterator.SERVER_INSTANCE, serverComboBox.getModel().getSelectedItem());
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton manageButton;
    private javax.swing.JComboBox platforms;
    private javax.swing.JLabel rubyPlatformLabel;
    private javax.swing.JComboBox serverComboBox;
    private javax.swing.JLabel serverLabel;
    private javax.swing.JCheckBox warCheckBox;
    // End of variables declaration//GEN-END:variables
    
    private void fireChangeEvent() {
        this.panel.fireChangeEvent();
    }

    private boolean isJdk6() {
        // TODO: 
        // - does the gf gem run on jdk 7 or on JRE 6?
        // - the user can also specify jruby.java.home to run jruby on
        // a different jdk than the IDE, need to address this for FCS
        String javaVersion = System.getProperty("java.version"); //NOI18N
        return isJdk() && javaVersion.startsWith("1.6"); //NOI18N
    }

    private boolean isJdk() {
        String jdkHome = System.getProperty("jdk.home"); //NOI18N
        if (Utilities.isMac()) {
            return true; // AFAIK Macs can't have a JRE only
        }
        File jreDir = new File(jdkHome, "jre"); //NOI18N
        return jreDir.exists() && jreDir.isDirectory();
    }
}
