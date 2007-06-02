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
import java.awt.Font;
import java.awt.Window;
import java.io.File;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLEditorKit;
import org.openide.filesystems.FileUtil;
import org.openide.util.Enumerations;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class ProductInformationPanel extends JPanel {

    public ProductInformationPanel() {
        initComponents();
        
        description.setText(org.openide.util.NbBundle.getMessage(ProductInformationPanel.class, 
                "LBL_Description", new Object[] {getProductVersionValue(), getJavaValue(), getVMValue(), getOperatingSystemValue(), getEncodingValue(), getSystemLocaleValue()}));
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code  (57:314)">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        copyright = new javax.swing.JTextPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        description = new javax.swing.JTextPane();

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/core/resources/about.png"))); // NOI18N

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jButton2.setMnemonic(NbBundle.getMessage(ProductInformationPanel.class, "MNE_Close").charAt(0));
        jButton2.setText(NbBundle.getMessage(ProductInformationPanel.class, "LBL_Close")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(jButton2, gridBagConstraints);

        jScrollPane3.setBorder(null);

        copyright.setBackground(new java.awt.Color(236, 233, 216));
        copyright.setBorder(null);
        copyright.setContentType("text/html");
        copyright.setEditable(false);
        copyright.setFont(copyright.getFont().deriveFont(copyright.getFont().getSize()+2f));
        copyright.setText(org.openide.util.NbBundle.getBundle(ProductInformationPanel.class).getString("LBL_Copyright")); // NOI18N
        jScrollPane3.setViewportView(copyright);

        description.setContentType("text/html");
        description.setFont(description.getFont().deriveFont(description.getFont().getSize()+1f));
        jScrollPane2.setViewportView(description);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 67, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
    closeDialog();    
}//GEN-LAST:event_jButton2ActionPerformed

    // Variables declaration - do not modify (57:311)//GEN-BEGIN:variables
    private javax.swing.JTextPane copyright;
    private javax.swing.JTextPane description;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    // End of variables declaration (57:312)//GEN-END:variables
    
    private void closeDialog() {
        Window w = SwingUtilities.getWindowAncestor(this);
        w.setVisible(false);
        w.dispose();
    }

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
     
}
