/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.ui;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;
import java.awt.Toolkit;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.util.NbBundle;

import org.netbeans.core.TopLogging;

import org.netbeans.core.ui.NbLabelA11y;

public class ProductInformationPanel extends JPanel {

    private static final float FONT_SIZE_PLUS = 5f;

    private static final Color COLOR = Color.black;

    private static Dialog dialog;

    public ProductInformationPanel() {
        dialog = null;
        initComponents();
        if (Boolean.getBoolean("netbeans.accessibility"))
        {
            ((NbLabelA11y)productInformationLabel).setA11yFocus(true);
            ((NbLabelA11y)productVersionValueLabel).setA11yAll(true, fromBundle("LBL_ProductVersion") + getProductVersionValue(), "");
            ((NbLabelA11y)ideVersioningValueLabel).setA11yAll(true, fromBundle("LBL_IDEVersioning") + getIDEVersioningValue(), "");
            ((NbLabelA11y)operatingSystemValueLabel).setA11yAll(true, fromBundle("LBL_OperationgSystem") + getOperatingSystemValue(), "");
            ((NbLabelA11y)javaValueLabel).setA11yAll(true, fromBundle("LBL_Java") + getJavaValue(), "");
            ((NbLabelA11y)vmValueLabel).setA11yAll(true, fromBundle("LBL_VM") + getVMValue(), "");
            ((NbLabelA11y)vendorValueLabel).setA11yAll(true, fromBundle("LBL_Vendor") + getVendorValue(), "");
            ((NbLabelA11y)javaHomeValueLabel).setA11yAll(true, fromBundle("LBL_JavaHome") + getJavaHomeValue(), "");
            ((NbLabelA11y)systemLocaleValueLabel).setA11yAll(true, fromBundle("LBL_SystemLocale") + getSystemLocaleValue(), "");
            ((NbLabelA11y)homeDirValueLabel).setA11yAll(true, fromBundle("LBL_HomeDir") + getHomeDirValue(), "");
            ((NbLabelA11y)currentDirValueLabel).setA11yAll(true, fromBundle("LBL_CurrentDir") + getCurrentDirValue(), "");
            ((NbLabelA11y)ideInstallValueLabel).setA11yAll(true, fromBundle("LBL_IDEInstall") + getIDEInstallValue(), "");
            ((NbLabelA11y)userDirValueLabel).setA11yAll(true, fromBundle("LBL_UserDir") + getUserDirValue(), "");
        }
  
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
        java.awt.GridBagConstraints gridBagConstraints;

        ideImageLabel = new javax.swing.JLabel();
        productVersionLabel = new javax.swing.JLabel();
        ideVersioningLabel = new javax.swing.JLabel();
        operatingSystemLabel = new javax.swing.JLabel();
        javaLabel = new javax.swing.JLabel();
        vmLabel = new javax.swing.JLabel();
        vendorLabel = new javax.swing.JLabel();
        javaHomeLabel = new javax.swing.JLabel();
        systemLocaleLabel = new javax.swing.JLabel();
        homeDirLabel = new javax.swing.JLabel();
        currentDirLabel = new javax.swing.JLabel();
        ideInstallLabel = new javax.swing.JLabel();
        userDirLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        productInformationLabel = new fLabel();
        productVersionValueLabel = new fLabel();
        ideVersioningValueLabel = new fLabel();
        operatingSystemValueLabel = new fLabel();
        javaValueLabel = new fLabel();
        vmValueLabel = new fLabel();
        vendorValueLabel = new fLabel();
        javaHomeValueLabel = new fLabel();
        systemLocaleValueLabel = new fLabel();
        homeDirValueLabel = new fLabel();
        currentDirValueLabel = new fLabel();
        ideInstallValueLabel = new fLabel();
        userDirValueLabel = new fLabel();

        setLayout(new java.awt.GridBagLayout());

        ideImageLabel.setIcon(getIcon());
        ideImageLabel.setLabelFor(productInformationLabel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(ideImageLabel, gridBagConstraints);

        productVersionLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString("LBL_ProductVersion"));
        productVersionLabel.setLabelFor(productVersionValueLabel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(productVersionLabel, gridBagConstraints);

        ideVersioningLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString("LBL_IDEVersioning"));
        ideVersioningLabel.setLabelFor(ideVersioningValueLabel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(ideVersioningLabel, gridBagConstraints);

        operatingSystemLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString("LBL_OperationgSystem"));
        operatingSystemLabel.setLabelFor(operatingSystemValueLabel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(operatingSystemLabel, gridBagConstraints);

        javaLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString("LBL_Java"));
        javaLabel.setLabelFor(javaValueLabel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(javaLabel, gridBagConstraints);

        vmLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString("LBL_VM"));
        vmLabel.setLabelFor(vmValueLabel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(vmLabel, gridBagConstraints);

        vendorLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString("LBL_Vendor"));
        vendorLabel.setLabelFor(vendorValueLabel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(vendorLabel, gridBagConstraints);

        javaHomeLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString("LBL_JavaHome"));
        javaHomeLabel.setLabelFor(javaHomeValueLabel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(javaHomeLabel, gridBagConstraints);

        systemLocaleLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString("LBL_SystemLocale"));
        systemLocaleLabel.setLabelFor(systemLocaleValueLabel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(systemLocaleLabel, gridBagConstraints);

        homeDirLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString("LBL_HomeDir"));
        homeDirLabel.setLabelFor(homeDirValueLabel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(homeDirLabel, gridBagConstraints);

        currentDirLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString("LBL_CurrentDir"));
        currentDirLabel.setLabelFor(currentDirValueLabel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(currentDirLabel, gridBagConstraints);

        ideInstallLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString("LBL_IDEInstall"));
        ideInstallLabel.setLabelFor(ideInstallValueLabel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(ideInstallLabel, gridBagConstraints);

        userDirLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString("LBL_UserDir"));
        userDirLabel.setLabelFor(userDirValueLabel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 11, 0);
        add(userDirLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

        productInformationLabel.setText(getProductInformationTitle ());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 11);
        add(productInformationLabel, gridBagConstraints);

        productVersionValueLabel.setText(getProductVersionValue());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 11);
        add(productVersionValueLabel, gridBagConstraints);

        ideVersioningValueLabel.setText(getIDEVersioningValue());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 11);
        add(ideVersioningValueLabel, gridBagConstraints);

        operatingSystemValueLabel.setText(getOperatingSystemValue());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 11);
        add(operatingSystemValueLabel, gridBagConstraints);

        javaValueLabel.setText(getJavaValue());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 11);
        add(javaValueLabel, gridBagConstraints);

        vmValueLabel.setText(getVMValue());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 11);
        add(vmValueLabel, gridBagConstraints);

        vendorValueLabel.setText(getVendorValue());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 11);
        add(vendorValueLabel, gridBagConstraints);

        javaHomeValueLabel.setText(getJavaHomeValue());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 11);
        add(javaHomeValueLabel, gridBagConstraints);

        systemLocaleValueLabel.setText(getSystemLocaleValue());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 11);
        add(systemLocaleValueLabel, gridBagConstraints);

        homeDirValueLabel.setText(getHomeDirValue());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 11);
        add(homeDirValueLabel, gridBagConstraints);

        currentDirValueLabel.setText(getCurrentDirValue());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 11);
        add(currentDirValueLabel, gridBagConstraints);

        ideInstallValueLabel.setText(getIDEInstallValue());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 11);
        add(ideInstallValueLabel, gridBagConstraints);

        userDirValueLabel.setText(getUserDirValue());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 11, 11);
        add(userDirValueLabel, gridBagConstraints);

    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel ideVersioningValueLabel;
    private javax.swing.JLabel javaHomeValueLabel;
    private javax.swing.JLabel operatingSystemLabel;
    private javax.swing.JLabel vmValueLabel;
    private javax.swing.JLabel homeDirLabel;
    private javax.swing.JLabel javaHomeLabel;
    private javax.swing.JLabel productVersionLabel;
    private javax.swing.JLabel systemLocaleValueLabel;
    private javax.swing.JLabel productVersionValueLabel;
    private javax.swing.JLabel javaLabel;
    private javax.swing.JLabel currentDirLabel;
    private javax.swing.JLabel vendorValueLabel;
    private javax.swing.JLabel userDirValueLabel;
    private javax.swing.JLabel homeDirValueLabel;
    private javax.swing.JLabel ideInstallLabel;
    private javax.swing.JLabel javaValueLabel;
    private javax.swing.JLabel operatingSystemValueLabel;
    private javax.swing.JLabel ideImageLabel;
    private javax.swing.JLabel ideInstallValueLabel;
    private javax.swing.JLabel ideVersioningLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel systemLocaleLabel;
    private javax.swing.JLabel vmLabel;
    private javax.swing.JLabel currentDirValueLabel;
    private javax.swing.JLabel vendorLabel;
    private javax.swing.JLabel productInformationLabel;
    private javax.swing.JLabel userDirLabel;
    // End of variables declaration//GEN-END:variables
    
    private String fromBundle (String bundleString) {
        return java.util.ResourceBundle.getBundle("org/netbeans/core/ui/Bundle").getString(bundleString);
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

    private ImageIcon getIcon () {
        return new ImageIcon(Toolkit.getDefaultToolkit().getImage(
            NbBundle.getLocalizedFile(
                "org.netbeans.core.resources.frames.ide48", // NOI18N
                "gif", // NOI18N
                Locale.getDefault(),
                ProductInformationPanel.class.getClassLoader())));
    }

    private String getProductInformationTitle () {
        return NbBundle.getBundle("org.netbeans.core.ui.Bundle", // NOI18N
                                   Locale.getDefault(),
                                   ProductInformationPanel.class.getClassLoader()
                ).getString("LBL_ProductInformation");
    }

    private String getProductVersionValue () {
        return new MessageFormat(
                NbBundle.getBundle("org.netbeans.core.Bundle", // NOI18N
                                   Locale.getDefault(),
                                   TopLogging.class.getClassLoader()
                ).getString("currentVersion")
            ).format(
                new Object[] {
            System.getProperty("netbeans.buildnumber")});
    }

    private String getIDEVersioningValue () {
        return new MessageFormat(
                NbBundle.getBundle("org.netbeans.core.ui.Bundle", // NOI18N
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
                NbBundle.getBundle("org.netbeans.core.ui.Bundle", // NOI18N
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
    
    /**
     */
    class fLabel extends JLabel {
    
        private boolean isFocused = false;

        public fLabel(){
            addFocusListener(
            new java.awt.event.FocusListener() {
                public void focusGained(java.awt.event.FocusEvent event) {
                    isFocused = true;
                    repaint();
                }
                public void focusLost(java.awt.event.FocusEvent event) {
                    isFocused = false;
                    repaint();
                }
            });
            setLabelFor(this);
            getAccessibleContext().setAccessibleDescription(getText());
        }
        
        public boolean isFocusTraversable() {
            return (true);
        }
        
        public void paintComponent(java.awt.Graphics g) {
            if (ui != null) {
                try {
                    ui.update(g, this);
                }
                finally {
                    g.dispose();
                }
            }
        
            if (isFocused) {
                java.awt.Dimension size = getSize();
                g.setColor(javax.swing.UIManager.getColor("Button.focus")); // NOI18N
                g.drawRect(0, 0, size.width-1, size.height-1);
            }
        }
        
    }
}