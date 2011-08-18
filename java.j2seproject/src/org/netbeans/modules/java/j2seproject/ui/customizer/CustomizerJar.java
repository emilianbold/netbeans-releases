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

package org.netbeans.modules.java.j2seproject.ui.customizer;

import java.awt.Dimension;
import java.util.LinkedList;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.J2SEProject;
import org.openide.util.HelpCtx;
import org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider;

/** Customizer for general project attributes.
 */
public class CustomizerJar extends JPanel implements HelpCtx.Provider {

    private J2SEProject project;
    private java.util.List<J2SECategoryExtensionProvider> compProviders = new LinkedList<J2SECategoryExtensionProvider>();
    
    public CustomizerJar( J2SEProjectProperties uiProperties ) {
        initComponents();

        int nextExtensionYPos = 0;
        this.project = uiProperties.getProject();
        for (J2SECategoryExtensionProvider compProvider : project.getLookup().lookupAll(J2SECategoryExtensionProvider.class)) {
            if( compProvider.getCategory() == J2SECategoryExtensionProvider.ExtensibleCategory.PACKAGING ) {
                if( addExtPanel(project,compProvider,nextExtensionYPos) ) {
                    compProviders.add(compProvider);
                    nextExtensionYPos++;
                }
            }
        }
        addPanelFiller(nextExtensionYPos);
        
        distDirField.setDocument(uiProperties.DIST_JAR_MODEL);
        excludeField.setDocument(uiProperties.BUILD_CLASSES_EXCLUDES_MODEL);

        uiProperties.JAR_COMPRESS_MODEL.setMnemonic(compressCheckBox.getMnemonic());
        compressCheckBox.setModel(uiProperties.JAR_COMPRESS_MODEL);

        uiProperties.DO_JAR_MODEL.setMnemonic(doJarCheckBox.getMnemonic());
        doJarCheckBox.setModel(uiProperties.DO_JAR_MODEL);

        uiProperties.COPY_LIBS_MODEL.setMnemonic(copyLibs.getMnemonic());
        copyLibs.setModel(uiProperties.COPY_LIBS_MODEL);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx( CustomizerJar.class );
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        distDirLabel = new javax.swing.JLabel();
        distDirField = new javax.swing.JTextField();
        excludeLabel = new javax.swing.JLabel();
        excludeField = new javax.swing.JTextField();
        excludeMessage = new javax.swing.JLabel();
        compressCheckBox = new javax.swing.JCheckBox();
        doJarCheckBox = new javax.swing.JCheckBox();
        copyLibs = new javax.swing.JCheckBox();
        extPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        distDirLabel.setLabelFor(distDirField);
        org.openide.awt.Mnemonics.setLocalizedText(distDirLabel, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "LBL_CustomizeJar_DistDir_JTextField")); // NOI18N

        distDirField.setEditable(false);

        excludeLabel.setLabelFor(excludeField);
        org.openide.awt.Mnemonics.setLocalizedText(excludeLabel, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "LBL_CustomizeJar_Excludes_JTextField")); // NOI18N

        excludeMessage.setLabelFor(excludeField);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/java/j2seproject/ui/customizer/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(excludeMessage, bundle.getString("LBL_CustomizerJar_ExcludeMessage")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(compressCheckBox, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "LBL_CustomizeJar_Commpres_JCheckBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(doJarCheckBox, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "CustomizerJar.doJarCheckBox")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(copyLibs, org.openide.util.NbBundle.getMessage(CustomizerJar.class, "TXT_CopyLibraries")); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 404, Short.MAX_VALUE)
            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(mainPanelLayout.createSequentialGroup()
                    .addGap(0, 0, 0)
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(compressCheckBox)
                        .addGroup(mainPanelLayout.createSequentialGroup()
                            .addComponent(doJarCheckBox)
                            .addGap(245, 245, 245))
                        .addComponent(copyLibs)
                        .addGroup(mainPanelLayout.createSequentialGroup()
                            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(excludeLabel)
                                .addComponent(distDirLabel))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(excludeMessage)
                                .addComponent(excludeField, javax.swing.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE)
                                .addComponent(distDirField, javax.swing.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE))))
                    .addGap(0, 0, 0)))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 161, Short.MAX_VALUE)
            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(mainPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(distDirLabel)
                        .addComponent(distDirField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(excludeLabel)
                        .addComponent(excludeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(excludeMessage)
                    .addGap(8, 8, 8)
                    .addComponent(compressCheckBox)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(doJarCheckBox)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(copyLibs)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        distDirField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(CustomizerJar.class).getString("AD_jTextFieldDistDir")); // NOI18N
        excludeField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(CustomizerJar.class).getString("AD_jTextFieldExcludes")); // NOI18N
        compressCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(CustomizerJar.class).getString("AD_jCheckBoxCompress")); // NOI18N
        doJarCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CustomizerJar.class, "ACSD_BuildJarAfterCompile")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(mainPanel, gridBagConstraints);

        extPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        add(extPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox compressCheckBox;
    private javax.swing.JCheckBox copyLibs;
    private javax.swing.JTextField distDirField;
    private javax.swing.JLabel distDirLabel;
    private javax.swing.JCheckBox doJarCheckBox;
    private javax.swing.JTextField excludeField;
    private javax.swing.JLabel excludeLabel;
    private javax.swing.JLabel excludeMessage;
    private javax.swing.JPanel extPanel;
    private javax.swing.JPanel mainPanel;
    // End of variables declaration//GEN-END:variables

    private boolean addExtPanel(Project p, J2SECategoryExtensionProvider compProvider, int gridY) {
        if (compProvider != null) {
            JComponent comp = compProvider.createComponent(p, null);
            if (comp != null) {
                java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
                constraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                constraints.gridx = 0;
                constraints.gridy = gridY;
                constraints.weightx = 1.0;
                extPanel.add(comp, constraints);
                return true;
            }
        }
        return false;
    }

    private void addPanelFiller(int gridY) {
        java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
        constraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        constraints.gridx = 0;
        constraints.gridy = gridY;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        extPanel.add( new Box.Filler(
                new Dimension(), 
                new Dimension(),
                new Dimension(10000,10000) ),
                constraints);
    }

}
