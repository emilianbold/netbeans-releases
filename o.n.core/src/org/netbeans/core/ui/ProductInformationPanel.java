package org.netbeans.core.ui;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.util.NbBundle;

import org.netbeans.core.TopLogging;

public class ProductInformationPanel extends JPanel {

    private static final float FONT_SIZE_PLUS = 5f;

    private static final Color COLOR = Color.black;

    private static Dialog dialog;
    private static ResourceBundle bundle;

    public ProductInformationPanel() {
        dialog = null;
        bundle = null;
        initComponents();
        updateLabelFont(productInformationLabel, Font.BOLD, FONT_SIZE_PLUS, COLOR);
        updateLabelFont(productVersionLabel, Font.BOLD, COLOR);
        updateLabelFont(ideVersioningLabel, Font.BOLD, COLOR);
        updateLabelFont(operatingSystemLabel, Font.BOLD, COLOR);
        updateLabelFont(javaLabel, Font.BOLD, COLOR);
        updateLabelFont(vmLabel, Font.BOLD, COLOR);
        updateLabelFont(vendorLabel, Font.BOLD, COLOR);
        updateLabelFont(javaHomeLabel, Font.BOLD, COLOR);
        updateLabelFont(systemLocaleLabel, Font.BOLD, COLOR);
        updateLabelFont(homeDirLabel, Font.BOLD, COLOR);
        updateLabelFont(currentDirLabel, Font.BOLD, COLOR);
        updateLabelFont(ideInstallLabel, Font.BOLD, COLOR);
        updateLabelFont(userDirLabel, Font.BOLD, COLOR);
        updateLabelFont(productVersionValueLabel, COLOR);
        updateLabelFont(ideVersioningValueLabel, COLOR);
        updateLabelFont(operatingSystemValueLabel, COLOR);
        updateLabelFont(javaValueLabel, COLOR);
        updateLabelFont(vmValueLabel, COLOR);
        updateLabelFont(vendorValueLabel, COLOR);
        updateLabelFont(javaHomeValueLabel, COLOR);
        updateLabelFont(systemLocaleValueLabel, COLOR);
        updateLabelFont(homeDirValueLabel, COLOR);
        updateLabelFont(currentDirValueLabel, COLOR);
        updateLabelFont(ideInstallValueLabel, COLOR);
        updateLabelFont(userDirValueLabel, COLOR);
    }

    private void initComponents() {//GEN-BEGIN:initComponents
        ideImageLabel = new javax.swing.JLabel();
        productInformationLabel = new javax.swing.JLabel();
        productVersionLabel = new javax.swing.JLabel();
        productVersionValueLabel = new javax.swing.JLabel();
        ideVersioningLabel = new javax.swing.JLabel();
        ideVersioningValueLabel = new javax.swing.JLabel();
        operatingSystemLabel = new javax.swing.JLabel();
        operatingSystemValueLabel = new javax.swing.JLabel();
        javaLabel = new javax.swing.JLabel();
        javaValueLabel = new javax.swing.JLabel();
        vmLabel = new javax.swing.JLabel();
        vmValueLabel = new javax.swing.JLabel();
        vendorLabel = new javax.swing.JLabel();
        vendorValueLabel = new javax.swing.JLabel();
        javaHomeLabel = new javax.swing.JLabel();
        javaHomeValueLabel = new javax.swing.JLabel();
        systemLocaleLabel = new javax.swing.JLabel();
        systemLocaleValueLabel = new javax.swing.JLabel();
        homeDirLabel = new javax.swing.JLabel();
        homeDirValueLabel = new javax.swing.JLabel();
        currentDirLabel = new javax.swing.JLabel();
        currentDirValueLabel = new javax.swing.JLabel();
        ideInstallLabel = new javax.swing.JLabel();
        ideInstallValueLabel = new javax.swing.JLabel();
        userDirLabel = new javax.swing.JLabel();
        userDirValueLabel = new javax.swing.JLabel();
        
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        ideImageLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/core/resources/ideImage.gif")));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.insets = new java.awt.Insets(6, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(ideImageLabel, gridBagConstraints1);
        
        productInformationLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString("LBL_ProductInformation"));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.gridwidth = 2;
        gridBagConstraints1.insets = new java.awt.Insets(6, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(productInformationLabel, gridBagConstraints1);
        
        productVersionLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString("LBL_ProductVersion"));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.insets = new java.awt.Insets(0, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(productVersionLabel, gridBagConstraints1);
        
        productVersionValueLabel.setText(getProductVersionValue());
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.insets = new java.awt.Insets(0, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(productVersionValueLabel, gridBagConstraints1);
        
        ideVersioningLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString("LBL_IDEVersioning"));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.insets = new java.awt.Insets(6, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(ideVersioningLabel, gridBagConstraints1);
        
        ideVersioningValueLabel.setText(getIDEVersioningValue());
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.insets = new java.awt.Insets(6, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(ideVersioningValueLabel, gridBagConstraints1);
        
        operatingSystemLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString("LBL_OperationgSystem"));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.insets = new java.awt.Insets(6, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(operatingSystemLabel, gridBagConstraints1);
        
        operatingSystemValueLabel.setText(getOperatingSystemValue());
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.insets = new java.awt.Insets(6, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(operatingSystemValueLabel, gridBagConstraints1);
        
        javaLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString("LBL_Java"));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 4;
        gridBagConstraints1.insets = new java.awt.Insets(6, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(javaLabel, gridBagConstraints1);
        
        javaValueLabel.setText(getJavaValue());
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 4;
        gridBagConstraints1.insets = new java.awt.Insets(6, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(javaValueLabel, gridBagConstraints1);
        
        vmLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString("LBL_VM"));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 5;
        gridBagConstraints1.insets = new java.awt.Insets(6, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(vmLabel, gridBagConstraints1);
        
        vmValueLabel.setText(getVMValue());
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 5;
        gridBagConstraints1.insets = new java.awt.Insets(6, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(vmValueLabel, gridBagConstraints1);
        
        vendorLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString("LBL_Vendor"));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 6;
        gridBagConstraints1.insets = new java.awt.Insets(6, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(vendorLabel, gridBagConstraints1);
        
        vendorValueLabel.setText(getVendorValue());
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 6;
        gridBagConstraints1.insets = new java.awt.Insets(6, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(vendorValueLabel, gridBagConstraints1);
        
        javaHomeLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString("LBL_JavaHome"));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 7;
        gridBagConstraints1.insets = new java.awt.Insets(6, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(javaHomeLabel, gridBagConstraints1);
        
        javaHomeValueLabel.setText(getJavaHomeValue());
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 7;
        gridBagConstraints1.insets = new java.awt.Insets(6, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(javaHomeValueLabel, gridBagConstraints1);
        
        systemLocaleLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString("LBL_SystemLocale"));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 8;
        gridBagConstraints1.insets = new java.awt.Insets(6, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(systemLocaleLabel, gridBagConstraints1);
        
        systemLocaleValueLabel.setText(getSystemLocaleValue());
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 8;
        gridBagConstraints1.insets = new java.awt.Insets(6, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(systemLocaleValueLabel, gridBagConstraints1);
        
        homeDirLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString("LBL_HomeDir"));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 9;
        gridBagConstraints1.insets = new java.awt.Insets(6, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(homeDirLabel, gridBagConstraints1);
        
        homeDirValueLabel.setText(getHomeDirValue());
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 9;
        gridBagConstraints1.insets = new java.awt.Insets(6, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(homeDirValueLabel, gridBagConstraints1);
        
        currentDirLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString("LBL_CurrentDir"));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 10;
        gridBagConstraints1.insets = new java.awt.Insets(6, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(currentDirLabel, gridBagConstraints1);
        
        currentDirValueLabel.setText(getCurrentDirValue());
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 10;
        gridBagConstraints1.insets = new java.awt.Insets(6, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(currentDirValueLabel, gridBagConstraints1);
        
        ideInstallLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString("LBL_IDEInstall"));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 11;
        gridBagConstraints1.insets = new java.awt.Insets(6, 12, 0, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(ideInstallLabel, gridBagConstraints1);
        
        ideInstallValueLabel.setText(getIDEInstallValue());
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 11;
        gridBagConstraints1.insets = new java.awt.Insets(6, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(ideInstallValueLabel, gridBagConstraints1);
        
        userDirLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString("LBL_UserDir"));
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 12;
        gridBagConstraints1.insets = new java.awt.Insets(6, 12, 5, 0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(userDirLabel, gridBagConstraints1);
        
        userDirValueLabel.setText(getUserDirValue());
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 2;
        gridBagConstraints1.gridy = 12;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(6, 12, 5, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints1.weightx = 1.0;
        add(userDirValueLabel, gridBagConstraints1);
        
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel ideImageLabel;
    private javax.swing.JLabel productInformationLabel;
    private javax.swing.JLabel productVersionLabel;
    private javax.swing.JLabel productVersionValueLabel;
    private javax.swing.JLabel ideVersioningLabel;
    private javax.swing.JLabel ideVersioningValueLabel;
    private javax.swing.JLabel operatingSystemLabel;
    private javax.swing.JLabel operatingSystemValueLabel;
    private javax.swing.JLabel javaLabel;
    private javax.swing.JLabel javaValueLabel;
    private javax.swing.JLabel vmLabel;
    private javax.swing.JLabel vmValueLabel;
    private javax.swing.JLabel vendorLabel;
    private javax.swing.JLabel vendorValueLabel;
    private javax.swing.JLabel javaHomeLabel;
    private javax.swing.JLabel javaHomeValueLabel;
    private javax.swing.JLabel systemLocaleLabel;
    private javax.swing.JLabel systemLocaleValueLabel;
    private javax.swing.JLabel homeDirLabel;
    private javax.swing.JLabel homeDirValueLabel;
    private javax.swing.JLabel currentDirLabel;
    private javax.swing.JLabel currentDirValueLabel;
    private javax.swing.JLabel ideInstallLabel;
    private javax.swing.JLabel ideInstallValueLabel;
    private javax.swing.JLabel userDirLabel;
    private javax.swing.JLabel userDirValueLabel;
    // End of variables declaration//GEN-END:variables

    private String getBundleString (String key) {
        if(bundle == null) {
            bundle = NbBundle.getBundle("org/netbeans/core/ui/Bundle");
        }
        return bundle.getString(key);
    }

    private void updateLabelFont (JLabel label, Color color) {
        updateLabelFont(label, 0, 0f, color);
    }

    private void updateLabelFont (JLabel label, int style, Color color) {
        updateLabelFont(label, style, 0f, color);
    }

    private void updateLabelFont (JLabel label, int style, float plusSize, Color color) {
        Font font = label.getFont();
        if(style != 0) {
            label.setFont(font = label.getFont().deriveFont(Font.BOLD));
        }
        if(plusSize != 0f) {
            label.setFont(font = font.deriveFont(font.getSize() + plusSize));
        }
        if(color != null) {
            label.setForeground(color);
        }
    }

    private String getProductVersionValue () {
        return new MessageFormat(
                NbBundle.getBundle("org.netbeans.core.Bundle",
                                   Locale.getDefault(),
                                   TopLogging.class.getClassLoader()
                ).getString("currentVersion")
            ).format(
                new Object[] {
            System.getProperty("netbeans.buildnumber")});
    }

    private String getIDEVersioningValue () {
        return new MessageFormat(
                NbBundle.getBundle("org.netbeans.core.ui.Bundle",
                                   Locale.getDefault(),
                                   ProductInformationPanel.class.getClassLoader()
                ).getString("Format_IdeVersioning_Value")
            ).format(
                new Object[] {
                    System.getProperty ("org.openide.major.version"),
                    System.getProperty("org.openide.specification.version"),
                    System.getProperty("org.openide.version")});
    }

    private String getOperatingSystemValue () {
        return new MessageFormat(
                NbBundle.getBundle("org.netbeans.core.ui.Bundle",
                                   Locale.getDefault(),
                                   ProductInformationPanel.class.getClassLoader()
                ).getString("Format_OperatingSystem_Value")
            ).format(
                new Object[] {
                    System.getProperty("os.name", "unknown"),
                    System.getProperty("os.version", "unknown"),
                    System.getProperty("os.arch", "unknown")});
    }

    private String getJavaValue () {
        return System.getProperty("java.version", "unknown");
    }

    private String getVMValue () {
        return System.getProperty("java.vm.name", "unknown") + " " + System.getProperty("java.vm.version", "");
    }

    private String getVendorValue () {
        return System.getProperty("java.vendor", "unknown");
    }

    private String getJavaHomeValue () {
        return System.getProperty("java.home", "unknown");
    }

    private String getSystemLocaleValue () {
        String branding;
        return Locale.getDefault().toString() + ((branding = NbBundle.getBranding()) == null ? "" : (" (" + branding + ")")); // NOI18N
    }

    private String getHomeDirValue () {
        return System.getProperty("user.home", "unknown");
    }

    private String getCurrentDirValue () {
        return System.getProperty("user.dir", "unknown");
    }

    private String getIDEInstallValue () {
        return System.getProperty("netbeans.home");
    }

    private String getUserDirValue () {
        return System.getProperty("netbeans.user");
    }
}
