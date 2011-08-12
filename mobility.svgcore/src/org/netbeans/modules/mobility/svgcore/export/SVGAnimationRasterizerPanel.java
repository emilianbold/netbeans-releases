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
 * SVGAnimationRasterizer.java
 *
 * Created on November 30, 2005, 10:53 AM
 */

package org.netbeans.modules.mobility.svgcore.export;

import java.awt.Dimension;
import java.io.IOException;
import java.util.logging.Level;
import javax.microedition.m2g.SVGImage;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.composer.SceneManager;
import org.netbeans.modules.mobility.svgcore.export.AnimationRasterizer.ColorReductionMethod;
import org.netbeans.modules.mobility.svgcore.export.AnimationRasterizer.ImageType;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Pavel Benes
 */
public final class SVGAnimationRasterizerPanel extends SVGRasterizerPanel {
    private          SpinnerNumberModel m_previewSpinnerModel;
    private final    ComponentGroup     m_startTime;    
    private final    ComponentGroup     m_stopTime;    
    private          Thread             m_sizeCalculationThread;
    private volatile boolean            m_processingStopped = false;

    /** Creates new form SVGAnimationRasterizer */
    public SVGAnimationRasterizerPanel(SVGDataObject dObj) throws IOException, BadLocationException {
        super(dObj, null);
        initComponents();

        createCompressionGroup( compressionLevelCombo, compressionQualitySpinner);
        
        final float duration = m_dObj.getSceneManager().getAnimationDuration();
        m_startTime = createTimeGroup( startTimeSpinner, startTimeSlider, duration, true);
        m_stopTime  = createTimeGroup( stopTimeSpinner, stopTimeSlider, duration, false);
                
        radioExportAll.setEnabled(isInProject());
        m_ratio = m_dim.getHeight() / m_dim.getWidth();
        spinnerHeight.setModel(new SpinnerNumberModel((int)m_dim.getHeight(), 1, 2048, 1));
        spinnerWidth.setModel(new SpinnerNumberModel((int)m_dim.getWidth(), 1, 2048, 1));
        
        framesPerSecSpinner.setModel( new SpinnerNumberModel( 2, 0.1, 30, 1));
        previewFrameSpinner.setModel( m_previewSpinnerModel = new SpinnerNumberModel(1, 1, 10, 1));
        
        spinnerWidth.getModel().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (keepRatio.isSelected()){
                    spinnerHeight.setValue(new Integer((int)(((Integer)spinnerWidth.getValue()).doubleValue() * m_ratio)));
                }
                updateImage(spinnerWidth, true);
            }
        });

        m_previewSpinnerModel.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateImage(previewFrameSpinner, false);
            }
        });

        framesPerSecSpinner.getModel().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                updateImage(framesPerSecSpinner, true);
            }
        });

        updateImage(null, true);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        sizePanel = new javax.swing.JPanel();
        javax.swing.JLabel jLabel11 = new javax.swing.JLabel();
        spinnerWidth = new javax.swing.JSpinner();
        javax.swing.JLabel jLabel12 = new javax.swing.JLabel();
        spinnerHeight = new javax.swing.JSpinner();
        keepRatio = new javax.swing.JCheckBox();
        optionsPanel = new javax.swing.JPanel();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        formatComboBox = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        progressiveCheckBox = new javax.swing.JCheckBox();
        compressionLabel = new javax.swing.JLabel();
        compressionLevelCombo = new JComboBox( AnimationRasterizer.CompressionLevel.values());
        compressionQualityLabel = new javax.swing.JLabel();
        compressionQualitySpinner = new javax.swing.JSpinner();
        reductionLabel = new javax.swing.JLabel();
        reductionCombo = new JComboBox( AnimationRasterizer.ColorReductionMethod.values());
        transparentCheckBox = new javax.swing.JCheckBox();
        timeLinePanel = new javax.swing.JPanel();
        startTimeSpinner = new JSpinner( new SpinnerNumberModel( (double)0.0, (double)0.0, (double)30.0, (double)1.0));
        javax.swing.JLabel startTimeLabel = new javax.swing.JLabel();
        javax.swing.JLabel stopTimeLabel = new javax.swing.JLabel();
        stopTimeSpinner = new JSpinner( new SpinnerNumberModel( (double)30.0, (double)0.0, (double)30.0, (double)1.0));
        startTimeSlider = new javax.swing.JSlider();
        stopTimeSlider = new javax.swing.JSlider();
        javax.swing.JLabel framesPerSecLabel = new javax.swing.JLabel();
        framesPerSecSpinner = new javax.swing.JSpinner();
        exportPanel = new javax.swing.JPanel();
        radioExportCurrent = new javax.swing.JRadioButton();
        radioExportAll = new javax.swing.JRadioButton();
        allFramesCheckBox = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        previewFormatText = new javax.swing.JTextField();
        previewFrameSizeText = new javax.swing.JTextField();
        previewFileText = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        previewAnimationSizeText = new javax.swing.JTextField();
        imageHolder = new javax.swing.JScrollPane();
        javax.swing.JLabel previewLabel = new javax.swing.JLabel();
        previewFrameSpinner = new javax.swing.JSpinner();
        previewMaxFrameText = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        previewCurrentTimeText = new javax.swing.JTextField();
        javax.swing.JLabel jLabel8 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel9 = new javax.swing.JLabel();
        previewEndTimeText = new javax.swing.JTextField();
        javax.swing.JLabel jLabel10 = new javax.swing.JLabel();

        setOpaque(false);

        sizePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_ImageSize"))); // NOI18N

        jLabel11.setLabelFor(spinnerWidth);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel11, org.openide.util.NbBundle.getBundle(SVGAnimationRasterizerPanel.class).getString("LBL_AnimationImageWidth")); // NOI18N

        jLabel12.setLabelFor(spinnerHeight);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel12, org.openide.util.NbBundle.getBundle(SVGAnimationRasterizerPanel.class).getString("LBL_AnimationImageHeight")); // NOI18N

        spinnerHeight.setEnabled(false);

        keepRatio.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(keepRatio, org.openide.util.NbBundle.getBundle(SVGAnimationRasterizerPanel.class).getString("LBL_AnimationKeepRatio")); // NOI18N
        keepRatio.setToolTipText("Images for other configurations are transformed using screen ratio.");
        keepRatio.setMargin(new java.awt.Insets(0, 0, 0, 0));
        keepRatio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keepRatioActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout sizePanelLayout = new javax.swing.GroupLayout(sizePanel);
        sizePanel.setLayout(sizePanelLayout);
        sizePanelLayout.setHorizontalGroup(
            sizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sizePanelLayout.createSequentialGroup()
                .addGroup(sizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(sizePanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(sizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(sizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(spinnerHeight, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spinnerWidth, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(sizePanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(keepRatio)))
                .addContainerGap(200, Short.MAX_VALUE))
        );
        sizePanelLayout.setVerticalGroup(
            sizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sizePanelLayout.createSequentialGroup()
                .addGroup(sizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(spinnerWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(sizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(spinnerHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(keepRatio)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel11.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_jLabel11_name")); // NOI18N
        jLabel11.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_jLabel11_description")); // NOI18N
        spinnerWidth.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_jSpinner_name")); // NOI18N
        spinnerWidth.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_jSpinner_descriprion")); // NOI18N
        jLabel12.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_jSpinner_height")); // NOI18N
        jLabel12.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_jSpinner_description")); // NOI18N
        spinnerHeight.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_spinnerHeight_name")); // NOI18N
        spinnerHeight.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_spinnerHeight_description")); // NOI18N
        keepRatio.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_keepRation_name")); // NOI18N
        keepRatio.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_keepRation_description")); // NOI18N

        optionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_ImageOptions"))); // NOI18N

        jLabel2.setLabelFor(formatComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_OptionsFormat")); // NOI18N

        formatComboBox.setModel(createImageTypeComboBoxModel());
        formatComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formatComboBoxActionPerformed(evt);
            }
        });

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(progressiveCheckBox, org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_OptionsProgressive")); // NOI18N
        progressiveCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        compressionLabel.setLabelFor(compressionLevelCombo);
        org.openide.awt.Mnemonics.setLocalizedText(compressionLabel, org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_OptionsQuality")); // NOI18N

        compressionQualityLabel.setLabelFor(compressionQualitySpinner);
        org.openide.awt.Mnemonics.setLocalizedText(compressionQualityLabel, org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_OptionsRate")); // NOI18N

        reductionLabel.setLabelFor(reductionCombo);
        org.openide.awt.Mnemonics.setLocalizedText(reductionLabel, org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_OptionsColorReduction")); // NOI18N

        reductionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorReductionChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(transparentCheckBox, org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_OptionsTransparent")); // NOI18N
        transparentCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        transparentCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transparentCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout optionsPanelLayout = new javax.swing.GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(optionsPanelLayout.createSequentialGroup()
                        .addGap(84, 84, 84)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE))
                    .addGroup(optionsPanelLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(formatComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(optionsPanelLayout.createSequentialGroup()
                        .addComponent(progressiveCheckBox)
                        .addContainerGap(212, Short.MAX_VALUE))
                    .addGroup(optionsPanelLayout.createSequentialGroup()
                        .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(compressionLabel)
                            .addGroup(optionsPanelLayout.createSequentialGroup()
                                .addComponent(compressionLevelCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(compressionQualityLabel)
                                .addGap(5, 5, 5)
                                .addComponent(compressionQualitySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(40, 40, 40))
                    .addGroup(optionsPanelLayout.createSequentialGroup()
                        .addComponent(transparentCheckBox)
                        .addContainerGap(206, Short.MAX_VALUE))
                    .addGroup(optionsPanelLayout.createSequentialGroup()
                        .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(reductionCombo, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(reductionLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap(144, Short.MAX_VALUE))))
        );
        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(formatComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressiveCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(transparentCheckBox)
                .addGap(14, 14, 14)
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(reductionLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reductionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addComponent(compressionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(compressionLevelCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(compressionQualityLabel)
                    .addComponent(compressionQualitySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jLabel2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_jLabel2_name")); // NOI18N
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_jLabel2_description")); // NOI18N
        formatComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_formatComboBox_name")); // NOI18N
        formatComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_jLabel2_descritpion")); // NOI18N
        progressiveCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_progressiveCheckBox_name")); // NOI18N
        progressiveCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_progressiveCheckBox_description")); // NOI18N
        compressionLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_compressionLabel_name")); // NOI18N
        compressionLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_compressionLabel_description")); // NOI18N
        compressionLevelCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_compressionLevelCombo_name")); // NOI18N
        compressionLevelCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_compressionLevelCombo_description")); // NOI18N
        compressionQualityLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_compressionQuailityLabel_name")); // NOI18N
        compressionQualityLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_compressionQuailityLabel_description")); // NOI18N
        compressionQualitySpinner.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_compressionQuailitySpinner_name")); // NOI18N
        compressionQualitySpinner.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_compressionQuailitySpinner_description")); // NOI18N
        reductionLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_reductionLabel_name")); // NOI18N
        reductionLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_reductionLabel_descritpion")); // NOI18N
        reductionCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_reductionCombo_name")); // NOI18N
        reductionCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_reductionCombo_description")); // NOI18N
        transparentCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_transpartenCheckBox_name")); // NOI18N
        transparentCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_transpartenCheckBox_description")); // NOI18N

        timeLinePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_AnimationFrameTime"))); // NOI18N

        startTimeLabel.setLabelFor(startTimeSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(startTimeLabel, org.openide.util.NbBundle.getBundle(SVGAnimationRasterizerPanel.class).getString("LBL_AnimationTime")); // NOI18N

        stopTimeLabel.setLabelFor(stopTimeSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(stopTimeLabel, org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_AnimationEndTime")); // NOI18N

        framesPerSecLabel.setLabelFor(framesPerSecSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(framesPerSecLabel, org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_AnimationFramePerSec")); // NOI18N

        javax.swing.GroupLayout timeLinePanelLayout = new javax.swing.GroupLayout(timeLinePanel);
        timeLinePanel.setLayout(timeLinePanelLayout);
        timeLinePanelLayout.setHorizontalGroup(
            timeLinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(timeLinePanelLayout.createSequentialGroup()
                .addGroup(timeLinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(timeLinePanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(framesPerSecLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 124, Short.MAX_VALUE)
                        .addComponent(framesPerSecSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(startTimeSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                    .addComponent(stopTimeSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                    .addGroup(timeLinePanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(timeLinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(timeLinePanelLayout.createSequentialGroup()
                                .addComponent(stopTimeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 155, Short.MAX_VALUE)
                                .addComponent(stopTimeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(timeLinePanelLayout.createSequentialGroup()
                                .addComponent(startTimeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 147, Short.MAX_VALUE)
                                .addComponent(startTimeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        timeLinePanelLayout.setVerticalGroup(
            timeLinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(timeLinePanelLayout.createSequentialGroup()
                .addGroup(timeLinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startTimeLabel)
                    .addComponent(startTimeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(startTimeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(timeLinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stopTimeLabel)
                    .addComponent(stopTimeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(stopTimeSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(timeLinePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(framesPerSecLabel)
                    .addComponent(framesPerSecSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24))
        );

        startTimeSpinner.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_startTimeSpinner_name")); // NOI18N
        startTimeSpinner.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_startTimeSpinner_description")); // NOI18N
        startTimeLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_startTimeLabel_name")); // NOI18N
        startTimeLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_startTimeLabel_description")); // NOI18N
        stopTimeLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_stopTimeLabel_name")); // NOI18N
        stopTimeLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_stopTimeLabel_description")); // NOI18N
        stopTimeSpinner.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_stopTimeSpinner_name")); // NOI18N
        stopTimeSpinner.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_stopTimeSpinner_description")); // NOI18N
        startTimeSlider.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_startTimeSlider_name")); // NOI18N
        startTimeSlider.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_startTimeSlider_descritpion")); // NOI18N
        stopTimeSlider.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_stopTimeSlider_name")); // NOI18N
        stopTimeSlider.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_stopTimeSlider_description")); // NOI18N
        framesPerSecLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_framePerSecLabel_name")); // NOI18N
        framesPerSecLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_framePerSecLabel_description")); // NOI18N
        framesPerSecSpinner.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_framePerSecSpinner_name")); // NOI18N
        framesPerSecSpinner.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_framePerSecSpinner_description")); // NOI18N

        exportPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_ExportLabel"))); // NOI18N

        buttonGroup1.add(radioExportCurrent);
        radioExportCurrent.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(radioExportCurrent, org.openide.util.NbBundle.getBundle(SVGAnimationRasterizerPanel.class).getString("LBL_AnimationOnlyActiveConfiguration")); // NOI18N
        radioExportCurrent.setMargin(new java.awt.Insets(0, 0, 0, 0));

        buttonGroup1.add(radioExportAll);
        org.openide.awt.Mnemonics.setLocalizedText(radioExportAll, org.openide.util.NbBundle.getBundle(SVGAnimationRasterizerPanel.class).getString("LBL_AnimationAllConfigurations")); // NOI18N
        radioExportAll.setMargin(new java.awt.Insets(0, 0, 0, 0));

        allFramesCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(allFramesCheckBox, org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_AnimationInSingleFile")); // NOI18N
        allFramesCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        allFramesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allFramesInSingleFileChanged(evt);
            }
        });

        javax.swing.GroupLayout exportPanelLayout = new javax.swing.GroupLayout(exportPanel);
        exportPanel.setLayout(exportPanelLayout);
        exportPanelLayout.setHorizontalGroup(
            exportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(exportPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(exportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(radioExportAll)
                    .addComponent(allFramesCheckBox)
                    .addComponent(radioExportCurrent))
                .addContainerGap(89, Short.MAX_VALUE))
        );
        exportPanelLayout.setVerticalGroup(
            exportPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(exportPanelLayout.createSequentialGroup()
                .addComponent(radioExportCurrent)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(radioExportAll)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
                .addComponent(allFramesCheckBox))
        );

        radioExportCurrent.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_radioExportCurrent_name")); // NOI18N
        radioExportCurrent.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_radioExportCurrent_description")); // NOI18N
        radioExportAll.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_radioExportAll_name")); // NOI18N
        radioExportAll.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_radioExportAll_description")); // NOI18N
        allFramesCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_allFramesCheckBox_name")); // NOI18N
        allFramesCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_allFramesCheckBox_name")); // NOI18N

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_PreviewTitle"))); // NOI18N

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel3.setLabelFor(previewFormatText);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_PreviewFormat")); // NOI18N

        jLabel1.setLabelFor(previewFrameSizeText);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_PreviewSize")); // NOI18N

        jLabel4.setLabelFor(previewFileText);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_PreviewFile")); // NOI18N

        previewFormatText.setEditable(false);
        previewFormatText.setText("JPEG");
        previewFormatText.setToolTipText("Image format");

        previewFrameSizeText.setEditable(false);
        previewFrameSizeText.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        previewFrameSizeText.setText("5.6KBytes");
        previewFrameSizeText.setToolTipText("Single frame size");

        previewFileText.setEditable(false);
        previewFileText.setText("C:\\Program Files\\about.svg");
        previewFileText.setToolTipText("File location");

        jLabel5.setText("/");

        previewAnimationSizeText.setEditable(false);
        previewAnimationSizeText.setText("132.5 KBytes");
        previewAnimationSizeText.setToolTipText("Whole animation size");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel1)
                    .addComponent(jLabel4))
                .addGap(13, 13, 13)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(previewFrameSizeText, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(previewAnimationSizeText, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(previewFileText, javax.swing.GroupLayout.DEFAULT_SIZE, 446, Short.MAX_VALUE)
                    .addComponent(previewFormatText, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(previewFormatText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel5)
                    .addComponent(previewFrameSizeText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(previewAnimationSizeText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(previewFileText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jLabel3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_jlabel3_name")); // NOI18N
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_jlabel3_descritpion")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_previewSizelabel_name")); // NOI18N
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_previewSizelabel_description")); // NOI18N
        jLabel4.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_previewFileLabel_name")); // NOI18N
        jLabel4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_previewFileLabel_description")); // NOI18N
        previewFormatText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_previewFormatlabel_name")); // NOI18N
        previewFormatText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_previewFormatlabel_description")); // NOI18N
        previewFrameSizeText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_previewFrameSizeText_name")); // NOI18N
        previewFrameSizeText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_previewFrameSizeText_description")); // NOI18N
        previewFileText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_previewFileLabel_name")); // NOI18N
        previewFileText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_previewFileLabel_description")); // NOI18N
        jLabel5.getAccessibleContext().setAccessibleDescription("/");
        previewAnimationSizeText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_previewAnimationSizeText_name")); // NOI18N
        previewAnimationSizeText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_previewAnimationSizeText_description")); // NOI18N

        imageHolder.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        imageHolder.setPreferredSize(new java.awt.Dimension(300, 300));

        previewLabel.setLabelFor(previewFrameSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(previewLabel, org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_AnimationPreviewFrame")); // NOI18N

        previewMaxFrameText.setEditable(false);
        previewMaxFrameText.setText("30");
        previewMaxFrameText.setToolTipText("Number of frames");

        jLabel7.setLabelFor(previewCurrentTimeText);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_AnimationPreviewTime")); // NOI18N

        previewCurrentTimeText.setEditable(false);
        previewCurrentTimeText.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        previewCurrentTimeText.setText("0");
        previewCurrentTimeText.setToolTipText("Current time");

        jLabel8.setLabelFor(previewEndTimeText);
        jLabel8.setText("/");

        jLabel9.setLabelFor(previewMaxFrameText);
        jLabel9.setText("/");

        previewEndTimeText.setEditable(false);
        previewEndTimeText.setText("60");
        previewEndTimeText.setToolTipText("Duration");

        jLabel10.setLabelFor(previewEndTimeText);
        jLabel10.setText("[s]");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(imageHolder, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addComponent(previewLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(previewFrameSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(previewMaxFrameText, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 155, Short.MAX_VALUE)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(previewCurrentTimeText, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(previewEndTimeText, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel10)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(previewLabel)
                    .addComponent(previewFrameSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(jLabel8)
                    .addComponent(jLabel10)
                    .addComponent(jLabel7)
                    .addComponent(previewMaxFrameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(previewCurrentTimeText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(previewEndTimeText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(imageHolder, javax.swing.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel4.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_jPanel4_name")); // NOI18N
        jPanel4.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_jPanel4_description")); // NOI18N
        imageHolder.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_imageHolder_name")); // NOI18N
        imageHolder.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_imageHolder_description")); // NOI18N
        previewLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_Frame_Label")); // NOI18N
        previewLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_Frame_Label_Description")); // NOI18N
        previewFrameSpinner.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_Frame_spin_name")); // NOI18N
        previewFrameSpinner.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_Frame_spin_description")); // NOI18N
        previewMaxFrameText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "AY11Y_Max_frame_name")); // NOI18N
        previewMaxFrameText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_Frame_spin_description")); // NOI18N
        jLabel7.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_LBL_Time_name")); // NOI18N
        jLabel7.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_LBL_Time_description")); // NOI18N
        previewCurrentTimeText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_LBL_CurrentTimetext_name")); // NOI18N
        previewCurrentTimeText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_CurrentTimeText_description")); // NOI18N
        jLabel8.getAccessibleContext().setAccessibleDescription("/");
        jLabel9.getAccessibleContext().setAccessibleDescription("/");
        previewEndTimeText.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_EndTimeText_name")); // NOI18N
        previewEndTimeText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_EndTimeText_description")); // NOI18N
        jLabel10.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_jLable10_name")); // NOI18N
        jLabel10.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_jLable10_description")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(timeLinePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(optionsPanel, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sizePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(exportPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(sizePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(optionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(timeLinePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(exportPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        sizePanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_sizePanel_name")); // NOI18N
        sizePanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_sizePanel_description")); // NOI18N
        optionsPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_optionsPanel_name")); // NOI18N
        optionsPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_optionsPanel_description")); // NOI18N
        timeLinePanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_timeLinePanel_name")); // NOI18N
        timeLinePanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_timeLinePanel_description")); // NOI18N
        exportPanel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_exportPanel_name")); // NOI18N
        exportPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_exportPanel_description")); // NOI18N
        jPanel3.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_Preview_Panel")); // NOI18N
        jPanel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "LBL_Preview_Descripton")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_Panel_name")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "A11Y_Panel_description")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void transparentCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transparentCheckBoxActionPerformed
    updateImage((JComponent)evt.getSource(), true);
}//GEN-LAST:event_transparentCheckBoxActionPerformed

private void colorReductionChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorReductionChanged
    updateImage((JComponent)evt.getSource(), true);
}//GEN-LAST:event_colorReductionChanged

private void allFramesInSingleFileChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allFramesInSingleFileChanged
    updateImage((JComponent) evt.getSource(), true);
}//GEN-LAST:event_allFramesInSingleFileChanged

private void formatComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formatComboBoxActionPerformed
     updateImage((JComponent) evt.getSource(), true);
}//GEN-LAST:event_formatComboBoxActionPerformed

    private void keepRatioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keepRatioActionPerformed
     spinnerHeight.setEnabled( !keepRatio.isSelected());   
}//GEN-LAST:event_keepRatioActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox allFramesCheckBox;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel compressionLabel;
    private javax.swing.JComboBox compressionLevelCombo;
    private javax.swing.JLabel compressionQualityLabel;
    private javax.swing.JSpinner compressionQualitySpinner;
    private javax.swing.JPanel exportPanel;
    private javax.swing.JComboBox formatComboBox;
    private javax.swing.JSpinner framesPerSecSpinner;
    private javax.swing.JScrollPane imageHolder;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JCheckBox keepRatio;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JTextField previewAnimationSizeText;
    private javax.swing.JTextField previewCurrentTimeText;
    private javax.swing.JTextField previewEndTimeText;
    private javax.swing.JTextField previewFileText;
    private javax.swing.JTextField previewFormatText;
    private javax.swing.JTextField previewFrameSizeText;
    private javax.swing.JSpinner previewFrameSpinner;
    private javax.swing.JTextField previewMaxFrameText;
    private javax.swing.JCheckBox progressiveCheckBox;
    private javax.swing.JRadioButton radioExportAll;
    private javax.swing.JRadioButton radioExportCurrent;
    private javax.swing.JComboBox reductionCombo;
    private javax.swing.JLabel reductionLabel;
    private javax.swing.JPanel sizePanel;
    private javax.swing.JSpinner spinnerHeight;
    private javax.swing.JSpinner spinnerWidth;
    private javax.swing.JSlider startTimeSlider;
    private javax.swing.JSpinner startTimeSpinner;
    private javax.swing.JSlider stopTimeSlider;
    private javax.swing.JSpinner stopTimeSpinner;
    private javax.swing.JPanel timeLinePanel;
    private javax.swing.JCheckBox transparentCheckBox;
    // End of variables declaration//GEN-END:variables
    
    public int getImageWidth(){
        return m_overrideWidth != -1 ? m_overrideWidth : ((Integer)spinnerWidth.getValue()).intValue();
    }
    
    public int getImageHeight(){
        return m_overrideHeight != -1 ? m_overrideHeight : ((Integer)spinnerHeight.getValue()).intValue();
    }
        
    public float getStartTime(){
        return ((Double)startTimeSpinner.getValue()).floatValue();
    }

    @Override
    public float getEndTime(){
        return ((Double)stopTimeSpinner.getValue()).floatValue();
    }
    
    @Override
    public float getFramesPerSecond(){
        return ((Double)framesPerSecSpinner.getValue()).floatValue();
    }
    
    public boolean isForAllConfigurations(){
        return radioExportAll.isSelected();
    }
        
    public float getCompressionQuality() {
        int value = ((Integer)compressionQualitySpinner.getValue()).intValue();
        return ((float)value)/100f;
    }
    
    public boolean isProgressive() {
        return progressiveCheckBox.isSelected();
    }
    
    public boolean isTransparent() {
        return transparentCheckBox.isSelected();
    }
    
    public boolean isInSingleImage() {
        return allFramesCheckBox.isSelected();
    }
    
    public int getNumberFrames() {
        float duration = getEndTime() - getStartTime();
        return 1 + (duration <= 0 ? 0 : (int) ((duration) * getFramesPerSecond()));
    }
        
    public AnimationRasterizer.ImageType getImageType() {
        return (AnimationRasterizer.ImageType)formatComboBox.getSelectedItem();
    }    

    public ColorReductionMethod getColorReductionMethod() {
        return (ColorReductionMethod) reductionCombo.getSelectedItem();
    }
    
    public void stopProcessing() {
        m_processingStopped = true;
        if ( m_sizeCalculationThread != null) {
            m_sizeCalculationThread.interrupt();
        }
    }
    
    protected String getPreviewFileName(){
        return previewFileText.getText();
    }
    
    protected void updateImage(JComponent source, boolean isOutputChanged) {
        if ( !m_updateInProgress && !m_processingStopped) {
            m_updateInProgress = true;
            //System.out.println("Updating model " + source);
            final JLabel label = new JLabel( "Updating image...");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setVerticalAlignment(SwingConstants.CENTER);
            label.setMinimumSize( new Dimension( 100, 100));
            imageHolder.setViewportView(label);

            updateTimelineBounds(source);

            ImageType imgType = getImageType();
            boolean supportsCompression = imgType.supportsCompression();

            compressionQualitySpinner.setEnabled(supportsCompression);
            compressionLevelCombo.setEnabled(supportsCompression);
            compressionLabel.setEnabled(supportsCompression);
            compressionQualityLabel.setEnabled(supportsCompression);

            boolean needsColorReduction = imgType.needsColorReduction();
            reductionCombo.setEnabled(needsColorReduction);
            reductionLabel.setEnabled(needsColorReduction);
            
            if (imgType.supportsTransparency()) {
                transparentCheckBox.setEnabled(true);
            } else {
                transparentCheckBox.setEnabled(false);
                transparentCheckBox.setSelected(false);
            }

            float startTime    = getStartTime(),
                  endTime      = getEndTime(),
                  framesPerSec = getFramesPerSecond();
            
            final float duration      = endTime - startTime;
            final int   totalFrameNum = getNumberFrames();
            
            m_previewSpinnerModel.setMaximum(totalFrameNum);
            if ( m_startTime.findWrapper(source) != null) {
                previewFrameSpinner.setValue( new Integer(1));
            } else if ( m_stopTime.findWrapper(source) != null) {
                previewFrameSpinner.setValue( new Integer(totalFrameNum));
            }
            final int currentFrame = ((Integer) previewFrameSpinner.getValue()).intValue() - 1;
            assert currentFrame >= 0;
            final float frameTime  = startTime + currentFrame / framesPerSec;

            String filenameRoot = AnimationRasterizer.createFileNameRoot(m_dObj, this, null, true);
            final String fileName = AnimationRasterizer.createFileName(filenameRoot, this, currentFrame, totalFrameNum);

            if ( isOutputChanged) {
                previewAnimationSizeText.setText("Calculating animation size ...");

                if ( m_sizeCalculationThread != null) {
                    m_sizeCalculationThread.interrupt();
                }
                final boolean [] blockUpdate = { false};
                m_sizeCalculationThread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            SVGImage svgImage = getSVGImage();
                            if (svgImage != null) {
                                AnimationRasterizer.calculateAnimationSize(svgImage,
                                        SVGAnimationRasterizerPanel.this, new AnimationRasterizer.ProgressUpdater() {

                                    public void updateProgress(final String text) {
                                        if (!blockUpdate[0]) {
                                            updatePreviewText(text);
                                        }
                                    }
                                });
                                updateSafeValues();
                            }
                        } catch( InterruptedException e) {
                        } catch( javax.imageio.IIOException e) {
                            blockUpdate[0] = true;
                            if ( !m_processingStopped) {
                                String msg = NbBundle.getMessage(SVGAnimationRasterizerPanel.class, "MSG_IMG_ENCODING_ERROR");   
                                updatePreviewText(msg + ".");
                                msg += ": " + e.getLocalizedMessage();
                                DialogDisplayer.getDefault().notify(
                                        new NotifyDescriptor.Message( msg, NotifyDescriptor.ERROR_MESSAGE));
                                restoreSafeValues();
                            }
                        } catch (Exception ex) {
                            SceneManager.log(Level.INFO, ex.getMessage(), ex);
                            restoreSafeValues();
                        }
                    }
                };
                m_sizeCalculationThread.setDaemon(true);
                m_sizeCalculationThread.setPriority(Thread.MIN_PRIORITY);
                m_sizeCalculationThread.setName("AnimationSizeCalculationThread");
                m_sizeCalculationThread.start();
            }
            
            RequestProcessor.getDefault().post( new Runnable() {
                public void run() {
                    try {
                        SVGImage svgImage = getSVGImage();
                        if (svgImage != null) {
                            final AnimationRasterizer.PreviewInfo preview = AnimationRasterizer.previewFrame(getSVGImage(), SVGAnimationRasterizerPanel.this, currentFrame, frameTime);
                            SwingUtilities.invokeLater(new Runnable() {

                                public void run() {
                                    previewFrameSizeText.setText(AnimationRasterizer.getSizeText(preview.m_imageSize));
                                    previewFormatText.setText(preview.m_imageFormat);
                                    previewFileText.setText(fileName);
                                    previewMaxFrameText.setText(String.valueOf(totalFrameNum));
                                    previewCurrentTimeText.setText(String.valueOf(roundTime(frameTime)));
                                    previewEndTimeText.setText(String.valueOf(roundTime(duration)));
                                    //TODO Handle images not saved yet
                                    label.setText(null);
                                    label.setIcon(new ImageIcon(preview.m_image));
                                    label.invalidate();
                                    imageHolder.validate();
                                    imageHolder.repaint();
                                }
                            });
                        } else {
                            label.setText("Load of SVG image failed");  //NOI18N
                            label.setIcon(null);
                            label.invalidate();
                            imageHolder.validate();
                            imageHolder.repaint();
                        }
                    } catch (Exception ex) {
                        SceneManager.log(Level.INFO, ex.getMessage(), ex);
                    } finally {
                        m_updateInProgress = false;
                    }
                }
            });
        }
    }
    
    private void updatePreviewText(final String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                previewAnimationSizeText.setText(msg);
            }
        });
    }
    
    private void updateTimelineBounds(JComponent source) {
        ComponentGroup.ComponentWrapper wrapper;
        if ( (wrapper = m_startTime.findWrapper(source)) != null) {
            float value = wrapper.getValue();
            stopTimeSlider.setExtent( Math.round( value * 100));
            ((SpinnerNumberModel)stopTimeSpinner.getModel()).setMinimum( new Double(value));
        } else if ((wrapper = m_stopTime.findWrapper(source)) != null) {
            float value = wrapper.getValue();
            startTimeSlider.setExtent( startTimeSlider.getMaximum() - Math.round( value * 100));
            ((SpinnerNumberModel)startTimeSpinner.getModel()).setMaximum( new Double(value));
        }
    }
    
    private double  m_backupFramesPerSec;
    private boolean m_backupAllFramesInSingleImage;
    private double  m_backupStartTime;
    private double  m_backupStopTime;
    
    private void updateSafeValues() {
        m_backupStartTime              = getStartTime();
        m_backupStopTime               = getEndTime();
        m_backupFramesPerSec           = getFramesPerSecond();
        m_backupAllFramesInSingleImage = isInSingleImage();
    }
    
    private void restoreSafeValues() {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                framesPerSecSpinner.setValue( Double.valueOf(m_backupFramesPerSec));
                allFramesCheckBox.setSelected(m_backupAllFramesInSingleImage);
                startTimeSpinner.setValue( Double.valueOf(m_backupStartTime));
                stopTimeSpinner.setValue( Double.valueOf(m_backupStopTime));
            };
        });
    }
}
