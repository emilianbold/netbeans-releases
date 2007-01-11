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

package org.netbeans.core.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.FocusEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.netbeans.core.actions.CopyProductInfoAction;
import org.openide.filesystems.FileUtil;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.ExClipboard;

public class ProductInformationPanel extends JPanel {

    private static final int FONT_SIZE_PLUS = 5;

    private static final Color COLOR = Color.black;

    /** holds system clipboard */
    private static Clipboard clipboard;
    /** holds instance of action to copy product info into clipboard */
    private static Action copyAction;

    public ProductInformationPanel() {
        initComponents();
        
        initAccessibility();
        
        updateLabelFont(productInformationLabel, Font.BOLD, FONT_SIZE_PLUS, COLOR);
        updateLabelFont(productVersionLabel, Font.BOLD, COLOR);
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
        updateLabelFont(operatingSystemValueLabel, COLOR);
        updateLabelFont(javaValueLabel, COLOR);
        updateLabelFont(vmValueLabel, COLOR);
        updateLabelFont(vendorValueLabel, COLOR);
        updateLabelFont(javaHomeValueLabel, COLOR);
        updateLabelFont(systemLocaleValueLabel, COLOR);
        updateLabelFont(homeDirValueLabel, COLOR);
        updateLabelFont(currentDirValueLabel, COLOR);
        updateLabelFont(ideInstallValueField, COLOR);
        updateLabelFont(userDirValueLabel, COLOR);
        
        if (ideInstallValueField.getText().length() == 0) {
            // Running in JNLP mode. This field is irrelevant.
            ideInstallLabel.setVisible(false);
            ideInstallValueField.setVisible(false);
            // XXX should currentDirValueLabel be hidden too? not much use...
        }
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        ideImageLabel = new javax.swing.JLabel();
        productVersionLabel = new javax.swing.JLabel();
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
        productInformationLabel = new FocusableLabel();
        ideInstallValueField = new javax.swing.JTextArea();
        copyButton = new javax.swing.JButton();
        productVersionValueLabel = new javax.swing.JTextArea();
        operatingSystemValueLabel = new javax.swing.JTextArea();
        javaValueLabel = new javax.swing.JTextArea();
        vmValueLabel = new javax.swing.JTextArea();
        vendorValueLabel = new javax.swing.JTextArea();
        javaHomeValueLabel = new javax.swing.JTextArea();
        systemLocaleValueLabel = new javax.swing.JTextArea();
        homeDirValueLabel = new javax.swing.JTextArea();
        currentDirValueLabel = new javax.swing.JTextArea();
        userDirValueLabel = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        ideImageLabel.setIcon(getIcon());
        ideImageLabel.setLabelFor(productInformationLabel);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(ideImageLabel, gridBagConstraints);

        productVersionLabel.setLabelFor(productVersionValueLabel);
        productVersionLabel.setText(org.openide.util.NbBundle.getMessage(ProductInformationPanel.class, "LBL_ProductVersion")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(productVersionLabel, gridBagConstraints);

        operatingSystemLabel.setLabelFor(operatingSystemValueLabel);
        operatingSystemLabel.setText(org.openide.util.NbBundle.getMessage(ProductInformationPanel.class, "LBL_OperationgSystem")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(operatingSystemLabel, gridBagConstraints);

        javaLabel.setLabelFor(javaValueLabel);
        javaLabel.setText(org.openide.util.NbBundle.getMessage(ProductInformationPanel.class, "LBL_Java")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(javaLabel, gridBagConstraints);

        vmLabel.setLabelFor(vmValueLabel);
        vmLabel.setText(org.openide.util.NbBundle.getMessage(ProductInformationPanel.class, "LBL_VM")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(vmLabel, gridBagConstraints);

        vendorLabel.setLabelFor(vendorValueLabel);
        vendorLabel.setText(org.openide.util.NbBundle.getMessage(ProductInformationPanel.class, "LBL_Vendor")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(vendorLabel, gridBagConstraints);

        javaHomeLabel.setLabelFor(javaHomeValueLabel);
        javaHomeLabel.setText(org.openide.util.NbBundle.getMessage(ProductInformationPanel.class, "LBL_JavaHome")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(javaHomeLabel, gridBagConstraints);

        systemLocaleLabel.setLabelFor(systemLocaleValueLabel);
        systemLocaleLabel.setText(org.openide.util.NbBundle.getMessage(ProductInformationPanel.class, "LBL_SystemLocale")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(systemLocaleLabel, gridBagConstraints);

        homeDirLabel.setLabelFor(homeDirValueLabel);
        homeDirLabel.setText(org.openide.util.NbBundle.getMessage(ProductInformationPanel.class, "LBL_HomeDir")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(homeDirLabel, gridBagConstraints);

        currentDirLabel.setLabelFor(currentDirValueLabel);
        currentDirLabel.setText(org.openide.util.NbBundle.getMessage(ProductInformationPanel.class, "LBL_CurrentDir")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(currentDirLabel, gridBagConstraints);

        ideInstallLabel.setLabelFor(ideInstallValueField);
        ideInstallLabel.setText(org.openide.util.NbBundle.getMessage(ProductInformationPanel.class, "LBL_IDEInstall")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 5, 0);
        add(ideInstallLabel, gridBagConstraints);

        userDirLabel.setLabelFor(userDirValueLabel);
        userDirLabel.setText(org.openide.util.NbBundle.getMessage(ProductInformationPanel.class, "LBL_UserDir")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(userDirLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.gridwidth = 2;
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

        ideInstallValueField.setEditable(false);
        ideInstallValueField.setText(getIDEInstallValue ());
        ideInstallValueField.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 5, 11);
        add(ideInstallValueField, gridBagConstraints);

        copyButton.setAction(CopyProductInfoAction.findObject(CopyProductInfoAction.class, true));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        add(copyButton, gridBagConstraints);

        productVersionValueLabel.setEditable(false);
        productVersionValueLabel.setText(getProductVersionValue());
        productVersionValueLabel.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 11);
        add(productVersionValueLabel, gridBagConstraints);

        operatingSystemValueLabel.setEditable(false);
        operatingSystemValueLabel.setText(getOperatingSystemValue());
        operatingSystemValueLabel.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 11);
        add(operatingSystemValueLabel, gridBagConstraints);

        javaValueLabel.setEditable(false);
        javaValueLabel.setText(getJavaValue());
        javaValueLabel.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 11);
        add(javaValueLabel, gridBagConstraints);

        vmValueLabel.setEditable(false);
        vmValueLabel.setText(getVMValue());
        vmValueLabel.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 11);
        add(vmValueLabel, gridBagConstraints);

        vendorValueLabel.setEditable(false);
        vendorValueLabel.setText(getVendorValue());
        vendorValueLabel.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 11);
        add(vendorValueLabel, gridBagConstraints);

        javaHomeValueLabel.setEditable(false);
        javaHomeValueLabel.setText(getJavaHomeValue());
        javaHomeValueLabel.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 11);
        add(javaHomeValueLabel, gridBagConstraints);

        systemLocaleValueLabel.setEditable(false);
        systemLocaleValueLabel.setText(getSystemLocaleValue());
        systemLocaleValueLabel.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 11);
        add(systemLocaleValueLabel, gridBagConstraints);

        homeDirValueLabel.setEditable(false);
        homeDirValueLabel.setText(getHomeDirValue());
        homeDirValueLabel.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 11);
        add(homeDirValueLabel, gridBagConstraints);

        currentDirValueLabel.setEditable(false);
        currentDirValueLabel.setText(getCurrentDirValue());
        currentDirValueLabel.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 11);
        add(currentDirValueLabel, gridBagConstraints);

        userDirValueLabel.setEditable(false);
        userDirValueLabel.setText(getUserDirValue());
        userDirValueLabel.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 11);
        add(userDirValueLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton copyButton;
    private javax.swing.JLabel currentDirLabel;
    private javax.swing.JTextArea currentDirValueLabel;
    private javax.swing.JLabel homeDirLabel;
    private javax.swing.JTextArea homeDirValueLabel;
    private javax.swing.JLabel ideImageLabel;
    private javax.swing.JLabel ideInstallLabel;
    private javax.swing.JTextArea ideInstallValueField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel javaHomeLabel;
    private javax.swing.JTextArea javaHomeValueLabel;
    private javax.swing.JLabel javaLabel;
    private javax.swing.JTextArea javaValueLabel;
    private javax.swing.JLabel operatingSystemLabel;
    private javax.swing.JTextArea operatingSystemValueLabel;
    private javax.swing.JLabel productInformationLabel;
    private javax.swing.JLabel productVersionLabel;
    private javax.swing.JTextArea productVersionValueLabel;
    private javax.swing.JLabel systemLocaleLabel;
    private javax.swing.JTextArea systemLocaleValueLabel;
    private javax.swing.JLabel userDirLabel;
    private javax.swing.JTextArea userDirValueLabel;
    private javax.swing.JLabel vendorLabel;
    private javax.swing.JTextArea vendorValueLabel;
    private javax.swing.JLabel vmLabel;
    private javax.swing.JTextArea vmValueLabel;
    // End of variables declaration//GEN-END:variables
    
    private void updateLabelFont (javax.swing.JComponent label, Color color) {
        updateLabelFont(label, 0, 0, color);
    }

    private void updateLabelFont (javax.swing.JComponent label, int style, Color color) {
        updateLabelFont(label, style, 0, color);
    }

    private void updateLabelFont (javax.swing.JComponent label, int style, int plusSize, Color color) {
        Font font = label.getFont();
        if(style != 0) {
            // don't use deriveFont() - see #49973 for details
            if (Utilities.isMac()) {
                font = new Font(label.getFont().getName(), Font.BOLD, label.getFont().getSize());
            } else {
                font = label.getFont().deriveFont(Font.BOLD);
            }
            label.setFont(font);
        }
        if(plusSize != 0f) {
            // don't use deriveFont() - see #49973 for details
            font = new Font(font.getName(), font.getStyle(), font.getSize() + plusSize);
            label.setFont(font);
        }
        if(color != null) {
            label.setForeground(color);
        }
    }

    private ImageIcon getIcon () {
        return new ImageIcon(Utilities.loadImage("org/netbeans/core/startup/frame48.gif", true));
    }

    private String getProductInformationTitle () {
        return NbBundle.getMessage(ProductInformationPanel.class, "LBL_ProductInformation");
    }

    public static String getProductVersionValue () {
        return MessageFormat.format(
            NbBundle.getBundle("org.netbeans.core.startup.Bundle").getString("currentVersion"),
            new Object[] {System.getProperty("netbeans.buildnumber")});
    }

    public static String getOperatingSystemValue () {
        return NbBundle.getMessage(ProductInformationPanel.class, "Format_OperatingSystem_Value",
            System.getProperty("os.name", "unknown"),
            System.getProperty("os.version", "unknown"),
            System.getProperty("os.arch", "unknown"));
    }

    public static String getJavaValue () {
        return System.getProperty("java.version", "unknown");
    }

    public static String getVMValue () {
        return System.getProperty("java.vm.name", "unknown") + " " + System.getProperty("java.vm.version", "");
    }

    private String getVendorValue () {
        return System.getProperty("java.vendor", "unknown");
    }

    private String getJavaHomeValue () {
        return System.getProperty("java.home", "unknown");
    }

    public static String getSystemLocaleValue () {
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
        String nbhome = System.getProperty("netbeans.home");
        String nbdirs = System.getProperty("netbeans.dirs");
        
        Enumeration<Object> more;
        if (nbdirs != null) {
            more = new StringTokenizer(nbdirs, File.pathSeparator);
        } else {
            more = Enumerations.empty();
        }
            
        Enumeration<Object> all = Enumerations.concat(Enumerations.singleton(nbhome), more);
        
        Set<File> files = new HashSet<File>();
        StringBuilder sb = new StringBuilder ();
        String prefix = "";
        while (all.hasMoreElements ()) {
            String s = (String)all.nextElement ();
            if (s == null) {
                continue;
            }
            File f = FileUtil.normalizeFile(new File(s));
            if (files.add (f)) {
                // new file
                sb.append (prefix);
                sb.append(f.getAbsolutePath());
                prefix = "\n";
            }
        }
        
        return sb.toString ();
    }

    private String getUserDirValue () {
        return System.getProperty("netbeans.user");
    }

    public static String getEncodingValue() {
        return System.getProperty("file.encoding", "unknown");
    }
     
    public static Clipboard getExClipboard() {
       if (clipboard == null) {
           clipboard = Lookup.getDefault().lookup(ExClipboard.class);
           if (clipboard == null) {
               clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
           }
       }
        return clipboard;
    }

    /**
     */
    static class FocusableLabel extends JLabel {
    
        private boolean isFocused = false;

        public FocusableLabel(){
            addFocusListener(
            new java.awt.event.FocusListener() {
                public void focusGained(FocusEvent event) {
                    isFocused = true;
                    repaint();
                }
                public void focusLost(FocusEvent event) {
                    isFocused = false;
                    repaint();
                }
            });

            getAccessibleContext().setAccessibleDescription(getText());
            getAccessibleContext().setAccessibleName(getText());
        }
        
        public boolean isFocusable() {
            return (true);
        }
        
        public void paintComponent(Graphics g) {
            if (ui != null) {
                try {
                    ui.update(g, this);
                }
                finally {
                    g.dispose();
                }
            }
        
            if (isFocused) {
                Dimension size = getSize();
                g.setColor(UIManager.getColor("Button.focus")); // NOI18N
                g.drawRect(0, 0, size.width-1, size.height-1);
            }
        }
        
    }

    
   /** Initilizes accessible contexts
     */
    private void initAccessibility(){
        
        //java.util.ResourceBundle bundle;
        //bundle = org.openide.util.NbBundle.getBundle(this.getClass());

        productVersionValueLabel.getAccessibleContext().setAccessibleName(productVersionLabel.getText() + " " + productVersionValueLabel.getText());
        productVersionValueLabel.getAccessibleContext().setAccessibleDescription(productVersionValueLabel.getText());
        
        operatingSystemValueLabel.getAccessibleContext().setAccessibleName(operatingSystemLabel.getText() + " " + operatingSystemValueLabel.getText());
        operatingSystemValueLabel.getAccessibleContext().setAccessibleDescription(operatingSystemValueLabel.getText());
        
        javaValueLabel.getAccessibleContext().setAccessibleName(javaLabel.getText() + " " + javaValueLabel.getText());
        javaValueLabel.getAccessibleContext().setAccessibleDescription(javaValueLabel.getText());
        
        vmValueLabel.getAccessibleContext().setAccessibleName(vmLabel.getText() + " " + vmValueLabel.getText());
        vmValueLabel.getAccessibleContext().setAccessibleDescription(vmValueLabel.getText());
        
        vendorValueLabel.getAccessibleContext().setAccessibleName(vendorLabel.getText() + " " + vendorValueLabel.getText());
        vendorValueLabel.getAccessibleContext().setAccessibleDescription(vendorValueLabel.getText());
        
        javaHomeValueLabel.getAccessibleContext().setAccessibleName(javaHomeLabel.getText() + " " + javaHomeValueLabel.getText());
        javaHomeValueLabel.getAccessibleContext().setAccessibleDescription(javaHomeValueLabel.getText());
        
        systemLocaleValueLabel.getAccessibleContext().setAccessibleName(systemLocaleLabel.getText() + " " + systemLocaleValueLabel.getText());
        systemLocaleValueLabel.getAccessibleContext().setAccessibleDescription(systemLocaleValueLabel.getText());
        
        homeDirValueLabel.getAccessibleContext().setAccessibleName(homeDirLabel.getText() + " " + homeDirValueLabel.getText());
        homeDirValueLabel.getAccessibleContext().setAccessibleDescription(homeDirValueLabel.getText());
        
        currentDirValueLabel.getAccessibleContext().setAccessibleName(currentDirLabel.getText() + " " + currentDirValueLabel.getText());
        currentDirValueLabel.getAccessibleContext().setAccessibleDescription(currentDirValueLabel.getText());
        
        ideInstallValueField.getAccessibleContext().setAccessibleName(ideInstallLabel.getText() + " " + ideInstallValueField.getText());
        ideInstallValueField.getAccessibleContext().setAccessibleDescription(ideInstallValueField.getText());
        
        userDirValueLabel.getAccessibleContext().setAccessibleName(userDirLabel.getText() + " " + userDirValueLabel.getText());
        userDirValueLabel.getAccessibleContext().setAccessibleDescription(userDirValueLabel.getText());
    }
}
