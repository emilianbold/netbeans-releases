/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cordova.project;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.prefs.Preferences;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cordova.CordovaPerformer;
import org.netbeans.modules.cordova.CordovaPlatform;
import org.netbeans.modules.cordova.wizard.CordovaProjectExtender;
import org.netbeans.modules.cordova.updatetask.SourceConfig;
import org.netbeans.modules.cordova.wizard.CordovaTemplate;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Becicka
 */
public class CordovaCustomizerPanel extends javax.swing.JPanel implements ActionListener, HelpCtx.Provider {

    private Project project;
    private SourceConfig config;
    private final Category cat;
    /**
     * Creates new form CordovaCustomizerPanel
     */
    public CordovaCustomizerPanel(Project p, Category cat) {
        this.project = p;
        this.cat = cat;
        if (!CordovaPlatform.getDefault().isReady()) {
            setLayout(new BorderLayout());
            add(new PhoneGapNotFound(), BorderLayout.CENTER);
            validate();
            CordovaPlatform.getDefault().addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (CordovaPlatform.getDefault().isReady()) {
                        if (CordovaPlatform.isCordovaProject(project)) {
                            config = CordovaPerformer.getConfig(project);
                        }
                        removeAll();
                        initControls();
                    }
                }
            });
            
        } else {
            if (CordovaPlatform.isCordovaProject(project) && CordovaPlatform.getDefault().isReady()) {
                config = CordovaPerformer.getConfig(project);
            }
            initControls();
        }
        cat.setStoreListener(this);
    }
    

    private void createMobileConfigs() {
        try {
            CordovaProjectExtender.createMobileConfigs(project.getProjectDirectory());
            CordovaPerformer.getDefault().createPlatforms(project).waitFinished();
            config = CordovaPerformer.getConfig(project);
            setVisibility();
            CordovaTemplate.CordovaExtender.setPhoneGapBrowser(project);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cordovaPanel = new org.netbeans.modules.cordova.project.CordovaPanel();
        generatePanel = new javax.swing.JPanel();
        createConfigs = new javax.swing.JButton();
        createConfigsLabel = new javax.swing.JLabel();
        mobilePlatformsSetup = new javax.swing.JButton();
        editPlugins = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(createConfigs, org.openide.util.NbBundle.getMessage(CordovaCustomizerPanel.class, "CordovaPanel.createConfigs.text")); // NOI18N
        createConfigs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createConfigsActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(createConfigsLabel, org.openide.util.NbBundle.getMessage(CordovaCustomizerPanel.class, "CordovaCustomizerPanel.createConfigsLabel.text")); // NOI18N

        javax.swing.GroupLayout generatePanelLayout = new javax.swing.GroupLayout(generatePanel);
        generatePanel.setLayout(generatePanelLayout);
        generatePanelLayout.setHorizontalGroup(
            generatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generatePanelLayout.createSequentialGroup()
                .addComponent(createConfigs)
                .addContainerGap(314, Short.MAX_VALUE))
            .addGroup(generatePanelLayout.createSequentialGroup()
                .addComponent(createConfigsLabel)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        generatePanelLayout.setVerticalGroup(
            generatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generatePanelLayout.createSequentialGroup()
                .addComponent(createConfigsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(createConfigs)
                .addContainerGap())
        );

        org.openide.awt.Mnemonics.setLocalizedText(mobilePlatformsSetup, org.openide.util.NbBundle.getMessage(CordovaCustomizerPanel.class, "CordovaCustomizerPanel.mobilePlatformsSetup.text")); // NOI18N
        mobilePlatformsSetup.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mobilePlatformsSetupActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(editPlugins, org.openide.util.NbBundle.getMessage(CordovaCustomizerPanel.class, "CordovaCustomizerPanel.editPlugins.text")); // NOI18N
        editPlugins.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editPluginsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(mobilePlatformsSetup))
            .addGroup(layout.createSequentialGroup()
                .addComponent(editPlugins)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(generatePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(cordovaPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(generatePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cordovaPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editPlugins)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(mobilePlatformsSetup))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void createConfigsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createConfigsActionPerformed
        createConfigs.setVisible(false);
        createConfigsLabel.setVisible(false);
        editPlugins.setVisible(true);
        mobilePlatformsSetup.setVisible(true);
        cordovaPanel.setVisible(true);
        ProgressUtils.showProgressDialogAndRun(new Runnable() {

            @Override
            public void run() {
                createMobileConfigs();
            }
        }, NbBundle.getMessage(CordovaCustomizerPanel.class, "CordovaPanel.createConfigsProgress.text"));
        validate();
    }//GEN-LAST:event_createConfigsActionPerformed

    private void mobilePlatformsSetupActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mobilePlatformsSetupActionPerformed
        OptionsDisplayer.getDefault().open("Advanced/MobilePlatforms");//NOI18N
    }//GEN-LAST:event_mobilePlatformsSetupActionPerformed

    private void editPluginsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editPluginsActionPerformed
        try {
            CordovaPerformer.createScript(project, "plugins.properties", "nbproject/plugins.properties", false);
            FileObject fileObject = this.project.getProjectDirectory().getFileObject("nbproject/plugins.properties");
            DataObject dob = DataObject.find(fileObject);
            dob.getCookie(EditCookie.class).edit();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_editPluginsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.netbeans.modules.cordova.project.CordovaPanel cordovaPanel;
    private javax.swing.JButton createConfigs;
    private javax.swing.JLabel createConfigsLabel;
    private javax.swing.JButton editPlugins;
    private javax.swing.JPanel generatePanel;
    private javax.swing.JButton mobilePlatformsSetup;
    // End of variables declaration//GEN-END:variables

    private void initControls() {
        initComponents();
        cordovaPanel.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                checkIdValid(cordovaPanel.getPackageName());
            }
        });
        setVisibility();
    }
    
    
    @NbBundle.Messages({
            "ERR_InvalidAppId={0} is not a valid Application ID"
        })
    private void checkIdValid(String packageName) {
        if (SourceConfig.isValidId(packageName)) {
            cat.setValid(true);
            cat.setErrorMessage("");
        } else {
            cat.setValid(false);
            cat.setErrorMessage(Bundle.ERR_InvalidAppId(packageName));
        }
    }

    @Override
    /**
     * Store listener
     */
    public void actionPerformed(ActionEvent e) {
        if (cordovaPanel == null) {
            return;
        }
        Preferences preferences = ProjectUtils.getPreferences(project, CordovaPlatform.class, true);
        preferences.put("phonegap", Boolean.toString(cordovaPanel.isPanelEnabled())); // NOI18N
        
        try {
            cordovaPanel.save(config);
        } catch (IOException iOException) {
            Exceptions.printStackTrace(iOException);
        }
        if (cordovaPanel.isPanelEnabled()) {
            CordovaPerformer.getDefault().createPlatforms(project);
        }
    }

    public void setVisibility() {
        boolean platformsReady = CordovaPlatform.getDefault().isReady();
        boolean isCordovaProject = CordovaPlatform.isCordovaProject(project);
        
        createConfigs.setVisible(!isCordovaProject && platformsReady);
        createConfigsLabel.setVisible(!isCordovaProject && platformsReady);
        cordovaPanel.setVisible(isCordovaProject && platformsReady);
        editPlugins.setVisible(isCordovaProject && platformsReady);
        mobilePlatformsSetup.setVisible(isCordovaProject && platformsReady);

        cordovaPanel.update();
        if (config!=null)
            cordovaPanel.load(config);
        validate();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.cordova.project.CordovaCustomizerPanel");
    }
}
