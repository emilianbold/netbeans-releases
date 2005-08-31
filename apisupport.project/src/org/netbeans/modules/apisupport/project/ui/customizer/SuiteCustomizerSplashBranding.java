/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
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
    public SuiteCustomizerSplashBranding(final SuiteProperties suiteProps) {
        super(suiteProps, SuiteCustomizerSplashBranding.class);
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
                try {
                    fontSize.commitEdit();
                    resetSplashPreview();
                } catch (ParseException ex) {
                    //user's invalide input
                }
            }
        });
        
        runningTextBounds.getDocument().addDocumentListener(new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                try {
                    runningTextBounds.commitEdit();
                    resetSplashPreview();
                } catch (ParseException ex) {
                    //user's invalide input
                }
            }
        });
        
        progressBarBounds.getDocument().addDocumentListener(new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                try {
                    progressBarBounds.commitEdit();
                    resetSplashPreview();
                } catch (ParseException ex) {
                    //user's invalide input
                }
            }
        });
        
    }
    
    
    public void store() {
        BasicBrandingModel branding = getBrandingModel();
        
        branding.getSplashRunningTextFontSize().setValue(SplashUISupport.integerToString(((Number)fontSize.getValue()).intValue()));
        branding.getSplashRunningTextBounds().setValue(SplashUISupport.boundsToString((Rectangle)runningTextBounds.getValue()));
        branding.getSplashProgressBarBounds().setValue(SplashUISupport.boundsToString((Rectangle)progressBarBounds.getValue()));        
        branding.getSplashRunningTextColor().setValue(SplashUISupport.colorToString(textColor.getColor()));        
        branding.getSplashProgressBarColor().setValue(SplashUISupport.colorToString(barColor.getColor()));                        
        //these colors below has a little effect on resulting branded splash 
        //then user can't adjust it from UI 
        //edgeColor.setColor(SplashUISupport.stringToColor(branding.getSplashProgressBarEdgeColor().getValue()));
        //cornerColor.setColor(SplashUISupport.stringToColor(branding.getSplashProgressBarCornerColor().getValue()));
        
        branding.getSplashShowProgressBar().setValue(Boolean.toString(progressBarEnabled.isSelected()));        
        branding.getSplash().setBrandingSource(splashSource);
        
        //hardcoded size of splash If current UI isn't sufficient then must be edited directly in generated
        //branding files.
        branding.getSplashWidth().setValue(Integer.toString(BasicBrandingModel.SPLASH_WIDTH,10));        
        branding.getSplashHeight().setValue(Integer.toString(BasicBrandingModel.SPLASH_HEIGHT,10));
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
        progressBarEnabled.setSelected(getBrandingModel().getSplashShowProgressBar().getValue().trim().toLowerCase().equals("true")); // NOI18N
        
        splashSource = getBrandingModel().getSplash().getBrandingSource();
        splashLocation.setText(getBrandingModel().getSplashLocation());
        resetSplashPreview();
        
        splashImage.setMaxSteps(10);
        splashImage.increment(10);
        splashImage.setText(NbBundle.getMessage(getClass(),"TEXT_SplashSample"));
        
        enableDisableComponents();
        
    }
    
    private void enableDisableComponents() {
        final BasicBrandingModel branding = getBrandingModel();
        fontSize.setEnabled(branding.isBrandingEnabled());
        runningTextBounds.setEnabled(branding.isBrandingEnabled());
        progressBarBounds.setEnabled(branding.isBrandingEnabled());
        textColor.setEnabled(branding.isBrandingEnabled());
        barColor.setEnabled(branding.isBrandingEnabled());
        edgeColor.setEnabled(branding.isBrandingEnabled());
        cornerColor.setEnabled(branding.isBrandingEnabled());
        progressBarEnabled.setEnabled(branding.isBrandingEnabled());        
        splashLocation.setEnabled(branding.isBrandingEnabled());
        splashImage.setEnabled(branding.isBrandingEnabled());
        barBoundsLabel.setEnabled(branding.isBrandingEnabled());
        barColorLabel.setEnabled(branding.isBrandingEnabled());
        browse.setEnabled(branding.isBrandingEnabled());
        splashLabel.setEnabled(branding.isBrandingEnabled());
        splashLocation.setEnabled(branding.isBrandingEnabled());
        splashPreview.setEnabled(branding.isBrandingEnabled());
        textBoundsLabel.setEnabled(branding.isBrandingEnabled());
        textColorLabel.setEnabled(branding.isBrandingEnabled());
        textFontSizeLabel.setEnabled(branding.isBrandingEnabled());                
    }
    
    private void resetSplashPreview() throws NumberFormatException {        
        splashImage.setSplashImageIcon(splashSource);
        splashImage.setTextColor(textColor.getColor());
        splashImage.setColorBar(barColor.getColor());
        splashImage.setColorEdge(edgeColor.getColor());
        splashImage.setColorEdge(cornerColor.getColor());
        splashImage.setFontSize(((Number)fontSize.getValue()).intValue());
        splashImage.setRunningTextBounds((Rectangle)runningTextBounds.getValue());
        splashImage.setProgressBarBounds((Rectangle)progressBarBounds.getValue());               
        splashImage.setProgressBarEnabled(progressBarEnabled.isSelected());
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

        splashPreview = splashImage;
        barColor = this.barColor;
        jTextField1 = this.progressBarBounds;
        barColorLabel = new javax.swing.JLabel();
        barBoundsLabel = new javax.swing.JLabel();
        textColorLabel = new javax.swing.JLabel();
        textColor = this.textColor;
        jTextField4 = this.runningTextBounds;
        splashLocation = new javax.swing.JTextField();
        fontSize = this.fontSize;
        progressBarEnabled = new javax.swing.JCheckBox();
        browse = new javax.swing.JButton();
        textFontSizeLabel = new javax.swing.JLabel();
        textBoundsLabel = new javax.swing.JLabel();
        splashLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(splashPreview, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 23);
        add(barColor, gridBagConstraints);

        jTextField1.setInputVerifier(jTextField1.getInputVerifier());
        jTextField1.setVerifyInputWhenFocusTarget(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 23);
        add(jTextField1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(barColorLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_BarColor"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(barColorLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(barBoundsLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_BarBounds"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(barBoundsLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(textColorLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_TextColor"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(textColorLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 23);
        add(textColor, gridBagConstraints);

        jTextField4.setInputVerifier(jTextField1.getInputVerifier());
        jTextField4.setVerifyInputWhenFocusTarget(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 23);
        add(jTextField4, gridBagConstraints);

        splashLocation.setEditable(false);
        splashLocation.setInputVerifier(jTextField1.getInputVerifier());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(splashLocation, gridBagConstraints);

        fontSize.setInputVerifier(jTextField1.getInputVerifier());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 0);
        add(fontSize, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(progressBarEnabled, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_ProgressBarEnabled"));
        progressBarEnabled.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        progressBarEnabled.setMargin(new java.awt.Insets(0, 0, 0, 0));
        progressBarEnabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                progressBarEnabledActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(progressBarEnabled, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browse, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_Browse"));
        browse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(browse, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(textFontSizeLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_TextFontSize"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(textFontSizeLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(textBoundsLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_TextBounds"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(textBoundsLabel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(splashLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_Splash"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(splashLabel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    private void browseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseActionPerformed
        JFileChooser chooser = UIUtil.getIconFileChooser();
        int ret = chooser.showDialog(this, NbBundle.getMessage(getClass(), "LBL_Select")); // NOI18N
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file =  chooser.getSelectedFile();
            try {
                splashSource = file.toURI().toURL();
            } catch (MalformedURLException ex) {
                ErrorManager.getDefault().notify(ex);
            }
            //splashImage.setSplashImageIcon(splashSource);
            resetSplashPreview();
        }
        
    }//GEN-LAST:event_browseActionPerformed
    
    private void progressBarEnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_progressBarEnabledActionPerformed
        resetSplashPreview();
    }//GEN-LAST:event_progressBarEnabledActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel barBoundsLabel;
    private javax.swing.JLabel barColorLabel;
    private javax.swing.JButton browse;
    private javax.swing.JCheckBox progressBarEnabled;
    private javax.swing.JLabel splashLabel;
    private javax.swing.JTextField splashLocation;
    private javax.swing.JLabel splashPreview;
    private javax.swing.JLabel textBoundsLabel;
    private javax.swing.JLabel textColorLabel;
    private javax.swing.JLabel textFontSizeLabel;
    // End of variables declaration//GEN-END:variables
    
    private BasicBrandingModel getBrandingModel() {
        return getProperties().getBrandingModel();
    }
}
