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
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.ImageIcon;
import javax.swing.event.DocumentEvent;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteCustomizerBasicBranding.ImagePreview;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;


/**
 * Represents <em>Splash branding parameters</em> panel in Suite customizer.
 *
 * @author Radek Matous
 */
public class SuiteCustomizerSplashBranding extends NbPropertyPanel.Suite {
    SplashComponentPreview splashImage;
    private JFormattedTextField fontSize;
    private JFormattedTextField runningTextBounds;
    private JFormattedTextField progressBarBounds;
    private SplashUISupport.ColorComboBox textColor;
    private SplashUISupport.ColorComboBox barColor;
    private SplashUISupport.ColorComboBox edgeColor;
    private SplashUISupport.ColorComboBox cornerColor;
    private JCheckBox progressBarEnabled;
    
    private URL splashSource = null;
    
    /**
     * Creates new form SuiteCustomizerLibraries
     */
    public SuiteCustomizerSplashBranding(final SuiteProperties suiteProps) {
        super(suiteProps);
        BasicBrandingModel branding = getBrandingModel();
        
        splashImage =new SplashComponentPreview();
        fontSize = SplashUISupport.getIntegerField();
        runningTextBounds = SplashUISupport.getBoundsField();
        progressBarBounds = SplashUISupport.getBoundsField();
        textColor = SplashUISupport.getColorComboBox();
        barColor = SplashUISupport.getColorComboBox();
        edgeColor = SplashUISupport.getColorComboBox();
        cornerColor = SplashUISupport.getColorComboBox();
        
        progressBarEnabled = new JCheckBox("is progress bar enabled");
        
        initComponents();
        refresh();
        
        PropertyChangeListener pL = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName() != SplashUISupport.ColorComboBox.PROP_COLOR) return;
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
        //edgeColor.setColor(SplashUISupport.stringToColor(branding.getSplashProgressBarEdgeColor().getValue()));
        //cornerColor.setColor(SplashUISupport.stringToColor(branding.getSplashProgressBarCornerColor().getValue()));
        
        branding.getSplashShowProgressBar().setValue(Boolean.toString(progressBarEnabled.isSelected()));        
        branding.getSplash().setBrandingSource(splashSource);
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
        progressBarEnabled.setSelected(getBrandingModel().getSplashShowProgressBar().getValue().trim().toLowerCase().equals("true"));
        
        splashSource = getBrandingModel().getSplash().getBrandingSource();
        splashLocation.setText(getBrandingModel().getSplashLocation());
        resetSplashPreview();
        
        splashImage.setMaxSteps(10);
        splashImage.increment(10);
        splashImage.setText("This is a sample text");
        
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
        java.awt.GridBagConstraints gridBagConstraints;
        javax.swing.JCheckBox jCheckBox1;
        javax.swing.JTextField jTextField1;
        javax.swing.JTextField jTextField4;
        javax.swing.JTextField jTextField6;
        javax.swing.JComboBox textColor;

        jLabel1 = splashImage;
        textColor = this.barColor;
        jTextField1 = this.progressBarBounds;
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        barColor = this.textColor;
        jTextField4 = this.runningTextBounds;
        splashLocation = new javax.swing.JTextField();
        jTextField6 = fontSize;
        jCheckBox1 = this.progressBarEnabled;
        jButton1 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 23);
        add(textColor, gridBagConstraints);

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

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_BarColor"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(jLabel2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_BarBounds"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(jLabel3, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_TextColor"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(jLabel4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 23);
        add(barColor, gridBagConstraints);

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

        jTextField6.setInputVerifier(jTextField1.getInputVerifier());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 6, 0, 0);
        add(jTextField6, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_ProgressBarEnabled"));
        jCheckBox1.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jCheckBox1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_Browse"));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(jButton1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_TextFontSize"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(jLabel5, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_TextBounds"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(jLabel6, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_Splash"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(jLabel7, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
// TODO add your handling code here:
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
        
    }//GEN-LAST:event_jButton1ActionPerformed
    
    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        resetSplashPreview();
    }//GEN-LAST:event_jCheckBox1ActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JTextField splashLocation;
    // End of variables declaration//GEN-END:variables
    
    private BasicBrandingModel getBrandingModel() {
        return getProperties().getBrandingModel();
    }
}
