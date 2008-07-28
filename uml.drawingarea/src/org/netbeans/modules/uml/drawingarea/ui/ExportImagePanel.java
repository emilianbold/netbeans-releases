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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.uml.drawingarea.ui;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.uml.drawingarea.image.DiagramImageWriter;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author  Sheryl Su
 */
public class ExportImagePanel extends javax.swing.JPanel implements DocumentListener, ChangeListener, ItemListener {

    private DialogDescriptor descriptor;
    private DesignerScene scene;
    private double ratio = 1.0;

    public ExportImagePanel() {
        initComponents();

        imageTypeComboBox.setModel(new DefaultComboBoxModel(
                new Object[]{DiagramImageWriter.ImageType.png, DiagramImageWriter.ImageType.jpg}));


        qualitySlider.setMaximum(100);
        qualitySlider.setMinimum(1);
        qualitySlider.setValue(100);
        qualitySlider.addChangeListener(this);
        qualityField.setText(Integer.toString(100));

        widthField.getDocument().addDocumentListener(this);
        heightField.getDocument().addDocumentListener(this);
        qualityField.getDocument().addDocumentListener(this);

        setQualityComponentsEnabled(false);

        actualSizeBtn.addItemListener(this);
        fitInWindowBtn.addItemListener(this);
        currentZoomLevelBtn.addItemListener(this);
        customBtn.addItemListener(this);
    }

    private void setQualityComponentsEnabled(boolean enabled) {
        qualitySlider.setEnabled(enabled);
        qualityField.setEnabled(enabled);
        qualityLbl.setEnabled(enabled);
        highLbl.setEnabled(enabled);
        lowLbl.setEnabled(enabled);
    }

    public void setDialogDescriptor(DialogDescriptor d) {
        descriptor = d;
    }

    public void initValue(DesignerScene scene) {
        this.scene = scene;
        Rectangle sceneRec = scene.getPreferredBounds();
        Rectangle viewRect = scene.getView().getVisibleRect();

        widthField.getDocument().removeDocumentListener(this);
        heightField.getDocument().removeDocumentListener(this);
        if (fitInWindowBtn.isSelected()) {
            double scale = Math.min((double) viewRect.width / (double) sceneRec.width,
                    (double) viewRect.height / (double) sceneRec.height);
            widthField.setText(Integer.toString((int) ((double) sceneRec.width * scale)));
            heightField.setText(Integer.toString((int) ((double) sceneRec.height * scale)));
        } else if (actualSizeBtn.isSelected()) {
            widthField.setText(Integer.toString(sceneRec.width));
            heightField.setText(Integer.toString(sceneRec.height));
        } else if (currentZoomLevelBtn.isSelected()) {
            widthField.setText(Integer.toString((int) ((double) sceneRec.width * scene.getZoomFactor())));
            heightField.setText(Integer.toString((int) ((double) sceneRec.height * scene.getZoomFactor())));
        }
        widthField.getDocument().addDocumentListener(this);
        heightField.getDocument().addDocumentListener(this);

        String fileName = scene.getDiagram().getFilename();
        File file = new File(fileName);
        String imageFile = file.getParent() + File.separator + scene.getDiagram().getName() + "." +
                ((DiagramImageWriter.ImageType)imageTypeComboBox.getSelectedItem()).getName();
        fileNameField.setText(imageFile);
        fileNameField.setCaretPosition(0);

        ratio = (double) sceneRec.height / (double) sceneRec.width;
    }

    private void setFileName(String ext) {
        String f = fileNameField.getText();
        int i = f.lastIndexOf(".");
        if (i > 0) {
            f = f.substring(0, i);
        }
        fileNameField.setText(f + "." + ext);
    }

    public void exportImage() {
        try {
//            File file = new File(fileNameField.getText());
            FileImageOutputStream os = new FileImageOutputStream(new File(fileNameField.getText()));
//            SceneExporter.ImageType sel = (ImageType) imageTypeComboBox.getSelectedItem();

            int zoomType = DiagramImageWriter.ACTUAL_SIZE;
//            SceneExporter.ZoomType zoomType = SceneExporter.ZoomType.ACTUAL_SIZE;
            if (currentZoomLevelBtn.isSelected()) {
                zoomType = DiagramImageWriter.CURRENT_ZOOM_LEVEL;
//                zoomType = SceneExporter.ZoomType.CURRENT_ZOOM_LEVEL;
            } else if (actualSizeBtn.isSelected()) {
                zoomType = DiagramImageWriter.ACTUAL_SIZE;
//                zoomType = SceneExporter.ZoomType.ACTUAL_SIZE;
            } else if (customBtn.isSelected()) {
                zoomType = DiagramImageWriter.CUSTOM_SIZE;
//                zoomType = SceneExporter.ZoomType.CUSTOM_SIZE;
            } else if (fitInWindowBtn.isSelected()) {
                zoomType = DiagramImageWriter.FIT_IN_WINDOW;
//                zoomType = SceneExporter.ZoomType.FIT_IN_WINDOW;
            }

//            boolean selectedOnly = selectedOnlyCheckBox.isSelected();
            boolean visibleAreaOnly = visibleOnlyCheckBox.isSelected();
            int quality = Integer.valueOf(qualityField.getText());
            int width = Integer.valueOf(widthField.getText());
            int height = Integer.valueOf(heightField.getText());

            DiagramImageWriter.write(scene, 
                    ((DiagramImageWriter.ImageType)imageTypeComboBox.getSelectedItem()), 
                    os, visibleAreaOnly, zoomType, false, quality, width, height);

//            SceneExporter.createImage(scene, file, sel, zoomType, visibleAreaOnly, selectedOnly, quality, width, height);
        } catch (IOException e) {
        }
    }

    private void setValid(boolean valid) {
        descriptor.setValid(valid);
    }

    public void insertUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    public void removeUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    public void changedUpdate(DocumentEvent e) {
        if (e.getDocument() == widthField.getDocument()) {
            try {
                int w = Integer.parseInt(widthField.getText());
                heightField.setText(Integer.toString((int) (w * ratio)));
                setValid(true);
            } catch (Exception ex) {
                setValid(false);
            }
        } else if (e.getDocument() == heightField.getDocument()) {
            try {
                int h = Integer.parseInt(heightField.getText());
                widthField.setText(Integer.toString((int) (h / ratio)));
                setValid(true);
            } catch (Exception ex) {
                setValid(false);
            }
        } else if (e.getDocument() == qualityField.getDocument()) {
            try {
                int quality = Integer.parseInt(qualityField.getText());
                if (quality < 1 || quality > 100) {
                    setValid(false);
                    return;
                }
                qualitySlider.removeChangeListener(this);
                qualitySlider.setValue(quality);
                qualitySlider.addChangeListener(this);
                setValid(true);
            } catch (Exception ex) {
                setValid(false);
            }
        }
    }

    public void stateChanged(ChangeEvent event) {
        if (event.getSource() == qualitySlider) {
            qualityField.getDocument().removeDocumentListener(this);
            qualityField.setText(Integer.toString(qualitySlider.getValue()));
            qualityField.getDocument().addDocumentListener(this);
        }
    }

    public void itemStateChanged(ItemEvent event) {
        Rectangle sceneRec = scene.getPreferredBounds();
        Rectangle viewRect = scene.getView().getVisibleRect();

        widthField.getDocument().removeDocumentListener(this);
        heightField.getDocument().removeDocumentListener(this);
        if (event.getSource() == customBtn) {
            widthField.setEditable(customBtn.isSelected());
            heightField.setEditable(customBtn.isSelected());
        } else if (event.getSource() == fitInWindowBtn) {
            double scale = Math.min((double) viewRect.width / (double) sceneRec.width,
                    (double) viewRect.height / (double) sceneRec.height);
            widthField.setText(Integer.toString((int) ((double) sceneRec.width * scale)));
            heightField.setText(Integer.toString((int) ((double) sceneRec.height * scale)));
        } else if (event.getSource() == actualSizeBtn) {
            widthField.setText(Integer.toString(sceneRec.width));
            heightField.setText(Integer.toString(sceneRec.height));
        } else if (event.getSource() == currentZoomLevelBtn) {
            widthField.setText(Integer.toString((int) ((double) sceneRec.width * scene.getZoomFactor())));
            heightField.setText(Integer.toString((int) ((double) sceneRec.height * scene.getZoomFactor())));
        }
        widthField.getDocument().addDocumentListener(this);
        heightField.getDocument().addDocumentListener(this);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        imageTypeLbl = new javax.swing.JLabel();
        fileNameLbl = new javax.swing.JLabel();
        imageTypeComboBox = new javax.swing.JComboBox();
        fileNameField = new javax.swing.JTextField();
        browseBtn = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        visibleOnlyCheckBox = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        lowLbl = new javax.swing.JLabel();
        qualitySlider = new javax.swing.JSlider();
        highLbl = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        qualityLbl = new javax.swing.JLabel();
        qualityField = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        currentZoomLevelBtn = new javax.swing.JRadioButton();
        actualSizeBtn = new javax.swing.JRadioButton();
        fitInWindowBtn = new javax.swing.JRadioButton();
        customBtn = new javax.swing.JRadioButton();
        widthLbl = new javax.swing.JLabel();
        heightLbl = new javax.swing.JLabel();
        widthField = new javax.swing.JTextField();
        heightField = new javax.swing.JTextField();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ExportImagePanel.class, "LBL_ExportImagePanel_Image"))); // NOI18N

        imageTypeLbl.setLabelFor(imageTypeComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(imageTypeLbl, org.openide.util.NbBundle.getMessage(ExportImagePanel.class, "ExportImagePanel.imageTypeLbl.text")); // NOI18N

        fileNameLbl.setLabelFor(fileNameField);
        org.openide.awt.Mnemonics.setLocalizedText(fileNameLbl, org.openide.util.NbBundle.getMessage(ExportImagePanel.class, "ExportImagePanel.fileNameLbl.text")); // NOI18N

        imageTypeComboBox.setMaximumSize(new java.awt.Dimension(300, 30));
        imageTypeComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                imageTypeComboBoxItemStateChanged(evt);
            }
        });
        imageTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                imageTypeComboBoxActionPerformed(evt);
            }
        });

        fileNameField.setColumns(20);

        org.openide.awt.Mnemonics.setLocalizedText(browseBtn, org.openide.util.NbBundle.getMessage(ExportImagePanel.class, "ExportImagePanel.browseBtn.text")); // NOI18N
        browseBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseBtnActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(fileNameLbl)
                    .add(imageTypeLbl))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(imageTypeComboBox, 0, 218, Short.MAX_VALUE)
                    .add(fileNameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(browseBtn)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(8, 8, 8)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(imageTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(imageTypeLbl))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fileNameLbl)
                    .add(browseBtn)
                    .add(fileNameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        imageTypeLbl.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ExportImagePanel.class, "ExportImagePanel.imageTypeLbl.AccessibleContext.accessibleName")); // NOI18N
        imageTypeLbl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ExportImagePanel.class, "ExportImagePanel.imageTypeLbl.text")); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ExportImagePanel.class, "LBL_ExportImagePanel_ImageContent"))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(visibleOnlyCheckBox, org.openide.util.NbBundle.getMessage(ExportImagePanel.class, "ExportImagePanel.visibleOnlyCheckBox.text")); // NOI18N
        visibleOnlyCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                visibleOnlyCheckBoxStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(visibleOnlyCheckBox)
                .addContainerGap(250, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(visibleOnlyCheckBox)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        visibleOnlyCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ExportImagePanel.class, "ExportImagePanel.visibleOnlyCheckBox.AccessibleContext.accessibleName")); // NOI18N
        visibleOnlyCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ExportImagePanel.class, "ExportImagePanel.visibleOnlyCheckBox.AccessibleContext.accessibleDescription")); // NOI18N

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ExportImagePanel.class, "LBL_ExportImagePanel_ImageQuality"))); // NOI18N

        jPanel5.setLayout(new java.awt.GridBagLayout());

        lowLbl.setLabelFor(qualitySlider);
        org.openide.awt.Mnemonics.setLocalizedText(lowLbl, org.openide.util.NbBundle.getMessage(ExportImagePanel.class, "ExportImagePanel.lowLbl.text")); // NOI18N
        jPanel5.add(lowLbl, new java.awt.GridBagConstraints());

        qualitySlider.setMajorTickSpacing(5);
        qualitySlider.setPaintTicks(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel5.add(qualitySlider, gridBagConstraints);

        highLbl.setText(org.openide.util.NbBundle.getMessage(ExportImagePanel.class, "ExportImagePanel.highLbl.text")); // NOI18N
        highLbl.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jPanel5.add(highLbl, new java.awt.GridBagConstraints());

        jPanel6.setLayout(new java.awt.GridBagLayout());

        qualityLbl.setLabelFor(qualityField);
        org.openide.awt.Mnemonics.setLocalizedText(qualityLbl, org.openide.util.NbBundle.getMessage(ExportImagePanel.class, "ExportImagePanel.qualityLbl.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        jPanel6.add(qualityLbl, gridBagConstraints);

        qualityField.setMinimumSize(new java.awt.Dimension(30, 19));
        qualityField.setPreferredSize(new java.awt.Dimension(30, 19));
        jPanel6.add(qualityField, new java.awt.GridBagConstraints());

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                    .add(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 13, Short.MAX_VALUE)
                .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 44, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ExportImagePanel.class, "LBL_ExportImagePanel_ImageSize"))); // NOI18N

        buttonGroup1.add(currentZoomLevelBtn);
        org.openide.awt.Mnemonics.setLocalizedText(currentZoomLevelBtn, org.openide.util.NbBundle.getMessage(ExportImagePanel.class, "ExportImagePanel.currentZoomLevelBtn.text")); // NOI18N

        buttonGroup1.add(actualSizeBtn);
        actualSizeBtn.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(actualSizeBtn, org.openide.util.NbBundle.getMessage(ExportImagePanel.class, "ExportImagePanel.actualSizeBtn.text")); // NOI18N

        buttonGroup1.add(fitInWindowBtn);
        org.openide.awt.Mnemonics.setLocalizedText(fitInWindowBtn, org.openide.util.NbBundle.getMessage(ExportImagePanel.class, "ExportImagePanel.fitInWindowBtn.text")); // NOI18N

        buttonGroup1.add(customBtn);
        org.openide.awt.Mnemonics.setLocalizedText(customBtn, org.openide.util.NbBundle.getMessage(ExportImagePanel.class, "ExportImagePanel.customBtn.text")); // NOI18N

        widthLbl.setLabelFor(widthField);
        org.openide.awt.Mnemonics.setLocalizedText(widthLbl, org.openide.util.NbBundle.getMessage(ExportImagePanel.class, "ExportImagePanel.widthLbl.text")); // NOI18N

        heightLbl.setLabelFor(heightField);
        org.openide.awt.Mnemonics.setLocalizedText(heightLbl, org.openide.util.NbBundle.getMessage(ExportImagePanel.class, "ExportImagePanel.heightLbl.text")); // NOI18N

        widthField.setColumns(5);
        widthField.setEditable(false);

        heightField.setEditable(false);

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(customBtn)
                    .add(currentZoomLevelBtn)
                    .add(jPanel4Layout.createSequentialGroup()
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(actualSizeBtn)
                            .add(fitInWindowBtn))
                        .add(86, 86, 86)
                        .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(heightLbl)
                            .add(widthLbl))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(widthField, 0, 0, Short.MAX_VALUE)
                    .add(heightField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 66, Short.MAX_VALUE))
                .addContainerGap(90, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel4Layout.createSequentialGroup()
                .add(currentZoomLevelBtn)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(actualSizeBtn)
                    .add(widthLbl)
                    .add(heightField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fitInWindowBtn)
                    .add(heightLbl)
                    .add(widthField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(customBtn)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void browseBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseBtnActionPerformed
        JFileChooser chooser = new JFileChooser(scene.getDiagram().getFilename());
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);

        chooser.setDialogTitle(NbBundle.getMessage(ExportImagePanel.class, "LBL_Export_Image_Location")); // NOI18N
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        String path = fileNameField.getText();

        if (path.length() > 0) {
            File f = new File(path);
            chooser.setSelectedFile(f);
        }

        FileFilter filter = new ImageFilter(
                ((DiagramImageWriter.ImageType)imageTypeComboBox.getSelectedItem()).getName()) ;
        chooser.setFileFilter(filter);
        chooser.setFileHidingEnabled(true);
        chooser.setApproveButtonText("Set");
        
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            File imageFile = chooser.getSelectedFile();
            fileNameField.setText(imageFile.getAbsolutePath());
        }
        
    }//GEN-LAST:event_browseBtnActionPerformed

    private void imageTypeComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_imageTypeComboBoxItemStateChanged

        setQualityComponentsEnabled(imageTypeComboBox.getSelectedItem() == DiagramImageWriter.ImageType.jpg);

        setFileName(((DiagramImageWriter.ImageType)imageTypeComboBox.getSelectedItem()).getName());
    }//GEN-LAST:event_imageTypeComboBoxItemStateChanged

    private void visibleOnlyCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_visibleOnlyCheckBoxStateChanged
        if (visibleOnlyCheckBox.isSelected())
            currentZoomLevelBtn.setSelected(true);
        for (Component c : jPanel4.getComponents()) {
            c.setEnabled(!visibleOnlyCheckBox.isSelected());
        }
        
    }//GEN-LAST:event_visibleOnlyCheckBoxStateChanged

    private void imageTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_imageTypeComboBoxActionPerformed
    {//GEN-HEADEREND:event_imageTypeComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_imageTypeComboBoxActionPerformed

    class ImageFilter extends FileFilter {

        private String imageType = null ;

        public ImageFilter(String imageType) {
            this.imageType = imageType ;
        }
        
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String extension = getExtension(f);
            if (extension != null) {
                if ((imageType.equalsIgnoreCase("jpg") && 
                        (extension.equals("jpeg") || extension.equals("jpg"))))
                    return true;
                else if (imageType.equalsIgnoreCase("png") && (extension.equals("png"))) 
                    return true;
                
            }

            return false;
        }

        private String getExtension(File f) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');

            if (i > 0 && i < s.length() - 1) {
                ext = s.substring(i + 1).toLowerCase();
            }
            return ext;
        }
        
        
        //The description of this filter
        public String getDescription() {
            return imageType;
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton actualSizeBtn;
    private javax.swing.JButton browseBtn;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JRadioButton currentZoomLevelBtn;
    private javax.swing.JRadioButton customBtn;
    private javax.swing.JTextField fileNameField;
    private javax.swing.JLabel fileNameLbl;
    private javax.swing.JRadioButton fitInWindowBtn;
    private javax.swing.JTextField heightField;
    private javax.swing.JLabel heightLbl;
    private javax.swing.JLabel highLbl;
    private javax.swing.JComboBox imageTypeComboBox;
    private javax.swing.JLabel imageTypeLbl;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JLabel lowLbl;
    private javax.swing.JTextField qualityField;
    private javax.swing.JLabel qualityLbl;
    private javax.swing.JSlider qualitySlider;
    private javax.swing.JCheckBox visibleOnlyCheckBox;
    private javax.swing.JTextField widthField;
    private javax.swing.JLabel widthLbl;
    // End of variables declaration//GEN-END:variables
}