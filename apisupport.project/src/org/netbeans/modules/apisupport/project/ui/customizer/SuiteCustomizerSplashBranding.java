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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.Image;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Locale;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 * Represents <em>Splash branding parameters</em> panel in Suite customizer.
 *
 * @author Radek Matous
 */
public class SuiteCustomizerSplashBranding extends NbPropertyPanel.Suite {
    
    private SplashComponentPreview splashImage;
    private JFormattedTextField fontSize;
    private JFormattedTextField runningTextBounds;
    private JFormattedTextField progressBarBounds;
    private SplashUISupport.ColorComboBox textColor;
    private SplashUISupport.ColorComboBox barColor;
    private SplashUISupport.ColorComboBox edgeColor;
    private SplashUISupport.ColorComboBox cornerColor;
    
    private URL splashSource;
    /**
     * Creates new form SuiteCustomizerLibraries
     */
    public SuiteCustomizerSplashBranding(final SuiteProperties suiteProps, ProjectCustomizer.Category cat) {
        super(suiteProps, SuiteCustomizerSplashBranding.class, cat);
        BasicBrandingModel branding = getBrandingModel();
        branding.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                enableDisableComponents();
            }
        });
        
        splashImage =new SplashComponentPreview();
        fontSize = SplashUISupport.getIntegerField();
        runningTextBounds = SplashUISupport.getBoundsField();
        progressBarBounds = SplashUISupport.getBoundsField();
        textColor = SplashUISupport.getColorComboBox();
        barColor = SplashUISupport.getColorComboBox();
        edgeColor = SplashUISupport.getColorComboBox();
        cornerColor = SplashUISupport.getColorComboBox();
        splashImage.setDropHandletForProgress(new DragManager.DropHandler(){
            public void dragAccepted(Rectangle original, Rectangle afterDrag) {
                progressBarBounds.setValue(afterDrag);
            }            
        });
        
        splashImage.setDropHandletForText(new DragManager.DropHandler(){
            public void dragAccepted(Rectangle original, Rectangle afterDrag) {
                runningTextBounds.setValue(afterDrag);
                double ratio = ((double)afterDrag.height)/original.height;
                int size = (int)((((Number)fontSize.getValue()).intValue()*ratio));
                size = (size > 0) ? size : 3;
                fontSize.setValue(new Integer(size));
            }
        });
        
        initComponents();
        refresh();
        
        PropertyChangeListener pL = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName() != SplashUISupport.ColorComboBox.PROP_COLOR) {
                    return;
                }
                resetSplashPreview();
            }
        };
        textColor.addPropertyChangeListener(pL);
        barColor.addPropertyChangeListener(pL);
        edgeColor.addPropertyChangeListener(pL);
        cornerColor.addPropertyChangeListener(pL);
        
        fontSize.getDocument().addDocumentListener(new UIUtil.DocumentAdapter() {
            
            public void insertUpdate(DocumentEvent e) {
                if (e != null || fontSize.isFocusOwner()) {
                    try {
                        fontSize.commitEdit();
                        ((Number) fontSize.getValue()).intValue();
                        category.setErrorMessage(null);
                        category.setValid(true);
                        resetSplashPreview();
                    } catch (ParseException ex) {
                        //user's invalide input
                        category.setErrorMessage(NbBundle.getMessage(SuiteCustomizerSplashBranding.class, "ERR_InvalidFontSize"));
                        category.setValid(false);
                    }
                }
            }
        });
        
        runningTextBounds.getDocument().addDocumentListener(new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                if (e != null || runningTextBounds.isFocusOwner()) {
                    try {
                        runningTextBounds.commitEdit();
                        category.setErrorMessage(null);
                        category.setValid(true);
                        resetSplashPreview();
                    } catch (ParseException ex) {
                        //user's invalide input
                        category.setErrorMessage(NbBundle.getMessage(SuiteCustomizerSplashBranding.class, "ERR_InvalidTextBounds"));
                        category.setValid(false);
                    }
                }
            }
        });
        
        progressBarBounds.getDocument().addDocumentListener(new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                if (e != null || progressBarBounds.isFocusOwner()) {
                    try {
                        progressBarBounds.commitEdit();
                        category.setErrorMessage(null);
                        category.setValid(true);
                        resetSplashPreview();
                    } catch (ParseException ex) {
                        //user's invalide input
                        category.setErrorMessage(NbBundle.getMessage(SuiteCustomizerSplashBranding.class, "ERR_InvalidProgressBarBounds"));
                        category.setValid(false);
                    }
                }
            }
        });
        
    }
    
    
    public void store() {
        BasicBrandingModel branding = getBrandingModel();
        
        branding.getSplashRunningTextFontSize().setValue(SplashUISupport.integerToString(((Number)fontSize.getValue()).intValue()));
        branding.getSplashRunningTextBounds().setValue(SplashUISupport.boundsToString((Rectangle)runningTextBounds.getValue()));
        branding.getSplashProgressBarBounds().setValue(SplashUISupport.boundsToString((Rectangle)progressBarBounds.getValue()));
        if (textColor.getColor() != null) {
            branding.getSplashRunningTextColor().setValue(SplashUISupport.colorToString(textColor.getColor()));
        }
        if (barColor.getColor() != null) {
            branding.getSplashProgressBarColor().setValue(SplashUISupport.colorToString(barColor.getColor()));
        }
        //these colors below has a little effect on resulting branded splash
        //then user can't adjust it from UI
        //edgeColor.setColor(SplashUISupport.stringToColor(branding.getSplashProgressBarEdgeColor().getValue()));
        //cornerColor.setColor(SplashUISupport.stringToColor(branding.getSplashProgressBarCornerColor().getValue()));
        
        branding.getSplashShowProgressBar().setValue(Boolean.toString(progressBarEnabled.isSelected()));
        branding.getSplash().setBrandingSource(splashSource);
        
        branding.getSplashWidth().setValue(Integer.toString(splashImage.image.getWidth(null),10));
        branding.getSplashHeight().setValue(Integer.toString(splashImage.image.getHeight(null),10));
    }
    
    
    void refresh() {
        BasicBrandingModel branding = getBrandingModel();
        
        fontSize.setValue(new Integer(SplashUISupport.stringToInteger(branding.getSplashRunningTextFontSize().getValue())));
        runningTextBounds.setValue(SplashUISupport.stringToBounds(branding.getSplashRunningTextBounds().getValue()));
        progressBarBounds.setValue(SplashUISupport.stringToBounds(branding.getSplashProgressBarBounds().getValue()));
        textColor.setColor(SplashUISupport.stringToColor(branding.getSplashRunningTextColor().getValue()));
        barColor.setColor(SplashUISupport.stringToColor(branding.getSplashProgressBarColor().getValue()));//
        edgeColor.setColor(SplashUISupport.stringToColor(branding.getSplashProgressBarEdgeColor().getValue()));
        cornerColor.setColor(SplashUISupport.stringToColor(branding.getSplashProgressBarCornerColor().getValue()));
        progressBarEnabled.setSelected(getBrandingModel().getSplashShowProgressBar().getValue().trim().toLowerCase(Locale.ENGLISH).equals("true")); // NOI18N
        
        splashSource = getBrandingModel().getSplash().getBrandingSource();
        resetSplashPreview();
        
        splashImage.setMaxSteps(10);
        //splashImage.increment(10);
        splashImage.resetSteps();
        splashImage.setText(NbBundle.getMessage(getClass(),"TEXT_SplashSample"));
        
        enableDisableComponents();
        
    }
    
    private void enableDisableComponents() {
        final BasicBrandingModel branding = getBrandingModel();
        jLabel1.setEnabled(branding.isBrandingEnabled());
        jLabel2.setEnabled(branding.isBrandingEnabled());
        fontSize.setEnabled(branding.isBrandingEnabled());
        runningTextBounds.setEnabled(branding.isBrandingEnabled());
        progressBarBounds.setEnabled(branding.isBrandingEnabled());
        textColor.setEnabled(branding.isBrandingEnabled());
        barColor.setEnabled(branding.isBrandingEnabled());
        edgeColor.setEnabled(branding.isBrandingEnabled());
        cornerColor.setEnabled(branding.isBrandingEnabled());
        progressBarEnabled.setEnabled(branding.isBrandingEnabled());
        splashImage.setEnabled(branding.isBrandingEnabled());
        barBoundsLabel.setEnabled(branding.isBrandingEnabled());
        barColorLabel.setEnabled(branding.isBrandingEnabled());
        browse.setEnabled(branding.isBrandingEnabled());
        splashLabel.setEnabled(branding.isBrandingEnabled());
        splashPreview.setEnabled(branding.isBrandingEnabled());
        textBoundsLabel.setEnabled(branding.isBrandingEnabled());
        textColorLabel.setEnabled(branding.isBrandingEnabled());
        textFontSizeLabel.setEnabled(branding.isBrandingEnabled());
        splashImage.setEnabled(branding.isBrandingEnabled());
    }
    
    private void resetSplashPreview() throws NumberFormatException {
        splashImage.setSplashImageIcon(splashSource);
        Rectangle tRectangle = (Rectangle)runningTextBounds.getValue();
        Rectangle pRectangle = (Rectangle)progressBarBounds.getValue();
        splashImage.setTextColor(textColor.getColor());
        splashImage.setColorBar(barColor.getColor());
        splashImage.setColorEdge(edgeColor.getColor());
        splashImage.setColorEdge(cornerColor.getColor());
        splashImage.setFontSize(((Number)fontSize.getValue()).intValue());
        splashImage.setRunningTextBounds(tRectangle);
        splashImage.setProgressBarBounds(pRectangle);
        splashImage.setProgressBarEnabled(progressBarEnabled.isSelected());
        splashImage.resetSteps();
        splashImage.setText(NbBundle.getMessage(getClass(),"TEXT_SplashSample"));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        javax.swing.JComboBox barColor;
        javax.swing.JTextField fontSize;
        java.awt.GridBagConstraints gridBagConstraints;
        javax.swing.JTextField jTextField1;
        javax.swing.JTextField jTextField4;
        javax.swing.JComboBox textColor;

        barColor = this.barColor;
        jTextField1 = this.progressBarBounds;
        barColorLabel = new javax.swing.JLabel();
        barBoundsLabel = new javax.swing.JLabel();
        textColorLabel = new javax.swing.JLabel();
        textColor = this.textColor;
        jTextField4 = this.runningTextBounds;
        fontSize = this.fontSize;
        progressBarEnabled = new javax.swing.JCheckBox();
        textFontSizeLabel = new javax.swing.JLabel();
        textBoundsLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        splashLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        splashPreview = splashImage;
        browse = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(barColor, gridBagConstraints);

        jTextField1.setInputVerifier(jTextField1.getInputVerifier());
        jTextField1.setVerifyInputWhenFocusTarget(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jTextField1, gridBagConstraints);

        barColorLabel.setLabelFor(barColor);
        org.openide.awt.Mnemonics.setLocalizedText(barColorLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_BarColor"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(barColorLabel, gridBagConstraints);
        barColorLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("ACS_BarColor"));

        barBoundsLabel.setLabelFor(jTextField1);
        org.openide.awt.Mnemonics.setLocalizedText(barBoundsLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_BarBounds"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(barBoundsLabel, gridBagConstraints);
        barBoundsLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("ACS_BarBounds"));

        textColorLabel.setLabelFor(textColor);
        org.openide.awt.Mnemonics.setLocalizedText(textColorLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_TextColor"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(textColorLabel, gridBagConstraints);
        textColorLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("ACS_TextColor"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(textColor, gridBagConstraints);

        jTextField4.setInputVerifier(jTextField1.getInputVerifier());
        jTextField4.setVerifyInputWhenFocusTarget(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jTextField4, gridBagConstraints);

        fontSize.setInputVerifier(jTextField1.getInputVerifier());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 12);
        add(fontSize, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(progressBarEnabled, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_ProgressBarEnabled"));
        progressBarEnabled.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        progressBarEnabled.setMargin(new java.awt.Insets(0, 0, 0, 0));
        progressBarEnabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                progressBarEnabledActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 1, 0, 12);
        add(progressBarEnabled, gridBagConstraints);
        progressBarEnabled.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("ACS_ProgressEnabled"));

        textFontSizeLabel.setLabelFor(fontSize);
        org.openide.awt.Mnemonics.setLocalizedText(textFontSizeLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_TextFontSize"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(textFontSizeLabel, gridBagConstraints);
        textFontSizeLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("ACS_FontSize"));

        textBoundsLabel.setLabelFor(jTextField4);
        org.openide.awt.Mnemonics.setLocalizedText(textBoundsLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_TextBounds"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(textBoundsLabel, gridBagConstraints);
        textBoundsLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("ACS_TextBounds"));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_ProgressBar"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(jLabel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_RunningText"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jLabel2, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(splashLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_Splash"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        jPanel1.add(splashLabel, gridBagConstraints);

        jScrollPane1.setBorder(null);
        jScrollPane1.setViewportView(splashPreview);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        jPanel1.add(jScrollPane1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browse, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_Browse"));
        browse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        jPanel1.add(browse, gridBagConstraints);
        browse.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("ACS_SplashBrowse"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jPanel1, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    private void browseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseActionPerformed
        JFileChooser chooser = UIUtil.getIconFileChooser();
        int ret = chooser.showDialog(this, NbBundle.getMessage(getClass(), "LBL_Select")); // NOI18N
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file =  chooser.getSelectedFile();
            try {
                splashSource = file.toURI().toURL();
                Image oldImage = splashImage.image;
                splashImage.setSplashImageIcon(splashSource);
                Image newImage = splashImage.image;
                int newWidth = newImage.getWidth(null);
                int newHeight = newImage.getHeight(null);
                int oldWidth = oldImage.getWidth(null);
                int oldHeight = oldImage.getHeight(null);
                if (newWidth != oldWidth || newHeight != oldHeight) {
                    double xRatio = newWidth / ((double) oldWidth);
                    double yRatio = newHeight / ((double) oldHeight);
                    Rectangle tRectangle = (Rectangle)runningTextBounds.getValue();
                    Rectangle pRectangle = (Rectangle)progressBarBounds.getValue();
                    
                    int x = ((int)(tRectangle.x*xRatio));
                    int y = ((int)(tRectangle.y*yRatio));
                    int width = ((int)(tRectangle.width*xRatio));
                    int height = ((int)(tRectangle.height*xRatio));
                    width = (width <= 0) ? 2 : width;
                    height = (height <= 0) ? 2 : height;
                    tRectangle.setBounds(x,y,width,height);
                    
                    x = ((int)(pRectangle.x*xRatio));
                    y = ((int)(pRectangle.y*yRatio));
                    width = ((int)(pRectangle.width*xRatio));
                    height = ((int)(pRectangle.height*xRatio));
                    width = (width <= 6) ? 6 : width;
                    height = (height <= 6) ? 6 : height;                    
                    pRectangle.setBounds(x,y,width,height);

                    runningTextBounds.setValue(tRectangle);
                    progressBarBounds.setValue(pRectangle);
                    int size = (int)((((Number)fontSize.getValue()).intValue()*yRatio));
                    size = (size <= 6) ? 6 : size;                    
                    fontSize.setValue(new Integer(size));                    
                } else {
                    resetSplashPreview();
                }
                
            } catch (MalformedURLException ex) {
                ErrorManager.getDefault().notify(ex);
            }
            
        }
        
    }//GEN-LAST:event_browseActionPerformed
    
    private void progressBarEnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_progressBarEnabledActionPerformed
        resetSplashPreview();
    }//GEN-LAST:event_progressBarEnabledActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel barBoundsLabel;
    private javax.swing.JLabel barColorLabel;
    private javax.swing.JButton browse;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JCheckBox progressBarEnabled;
    private javax.swing.JLabel splashLabel;
    private javax.swing.JLabel splashPreview;
    private javax.swing.JLabel textBoundsLabel;
    private javax.swing.JLabel textColorLabel;
    private javax.swing.JLabel textFontSizeLabel;
    // End of variables declaration//GEN-END:variables
    
    private BasicBrandingModel getBrandingModel() {
        return getProperties().getBrandingModel();
    }
}
