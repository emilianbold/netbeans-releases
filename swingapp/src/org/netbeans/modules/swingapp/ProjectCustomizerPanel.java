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

package org.netbeans.modules.swingapp;

import java.io.*;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.util.NbBundle;
import org.netbeans.modules.classfile.ClassFile;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;

public class ProjectCustomizerPanel extends javax.swing.JPanel implements HelpCtx.Provider {

    private static final String[] LAF_RESOURCE_NAMES = { "default", "system" }; // NOI18N
    private static final String[] LAF_DISPLAY_NAMES = {
             NbBundle.getMessage(ProjectCustomizerPanel.class, "LAF_Default"), // NOI18N
             NbBundle.getMessage(ProjectCustomizerPanel.class, "LAF_System") }; // NOI18N

    private FileObject lafJarRoot;

    static String fileChooserDir;

    public ProjectCustomizerPanel() {
        initComponents();
        lafCombo.setModel(lafComboModel());
    }

    void setVendorId(String text) {
        vendorIdTextField.setText(text);
    }

    String getVendorId() {
        return vendorIdTextField.getText();
    }

    void setApplicationId(String text) {
        appIdTextField.setText(text);
    }

    String getApplicationId() {
        return appIdTextField.getText();
    }

    void setLookAndFeel(String clsName) {
        for (int i=0; i < LAF_RESOURCE_NAMES.length; i++) {
            if (LAF_RESOURCE_NAMES[i].equalsIgnoreCase(clsName)) {
                lafCombo.setSelectedItem(LAF_DISPLAY_NAMES[i]);
                return;
            }
        }
        lafCombo.setSelectedItem(clsName);
    }

    String getLookAndFeel() {
        String lafText = ((String)lafCombo.getEditor().getItem()).trim();
        for (int i=0; i < LAF_RESOURCE_NAMES.length; i++) {
            if (LAF_DISPLAY_NAMES[i].equalsIgnoreCase(lafText)) {
                return LAF_RESOURCE_NAMES[i];
            }
        }
        return lafText;
    }

    FileObject getLookAndFeelJAR() {
        if (lafJarRoot != null) {
            String lafText = ((String)lafCombo.getEditor().getItem()).trim();
            for (int i=0; i < LAF_RESOURCE_NAMES.length; i++) {
                if (LAF_DISPLAY_NAMES[i].equalsIgnoreCase(lafText)
                        || LAF_RESOURCE_NAMES[i].equalsIgnoreCase(lafText)) {
                    return null; // custom LAF not selected
                }
            }
        }
        return lafJarRoot;
    }

    void setReadOnly() {
        vendorIdTextField.setEnabled(false);
        appIdTextField.setEnabled(false);
        lafCombo.setEnabled(false);
        browseButton.setVisible(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        vendorIdTextField = new javax.swing.JTextField();
        appIdTextField = new javax.swing.JTextField();
        lafCombo = new javax.swing.JComboBox();
        browseButton = new javax.swing.JButton();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(ProjectCustomizerPanel.class, "jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(ProjectCustomizerPanel.class, "jLabel2.text")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(ProjectCustomizerPanel.class, "jLabel3.text")); // NOI18N

        jLabel4.setText(org.openide.util.NbBundle.getMessage(ProjectCustomizerPanel.class, "jLabel4.text")); // NOI18N

        vendorIdTextField.setToolTipText(org.openide.util.NbBundle.getMessage(ProjectCustomizerPanel.class, "vendorIdTextField.toolTipText")); // NOI18N

        appIdTextField.setToolTipText(org.openide.util.NbBundle.getMessage(ProjectCustomizerPanel.class, "vendorIdTextField.toolTipText")); // NOI18N

        lafCombo.setEditable(true);

        browseButton.setText(org.openide.util.NbBundle.getMessage(ProjectCustomizerPanel.class, "browseButton.text")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel2)
                        .addComponent(jLabel3)
                        .addComponent(jLabel4))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(vendorIdTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
                        .addComponent(appIdTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
                        .addComponent(lafCombo, javax.swing.GroupLayout.Alignment.TRAILING, 0, 534, Short.MAX_VALUE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(browseButton)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(vendorIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(appIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(lafCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        JFileChooser fileChooser = new JFileChooser(fileChooserDir);
        fileChooser.setDialogTitle(NbBundle.getMessage(ProjectCustomizerPanel.class, "CTL_SelectJAR_Caption")); // NOI18N
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(true);
        fileChooser.setControlButtonsAreShown(true);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory()
                       || f.getName().toLowerCase().endsWith(".jar"); // NOI18N
            }
            @Override
            public String getDescription() {
                return NbBundle.getMessage(ProjectCustomizerPanel.class, "CTL_JarArchivesMask"); // NOI18N
            }
        });
        if (fileChooser.showOpenDialog(getTopLevelAncestor()) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file != null) {
                FileObject jar = FileUtil.toFileObject(file);
                if (jar != null && FileUtil.isArchiveFile(jar)) {
                    FileObject root = FileUtil.getArchiveRoot(jar);
                    List<String> lafList = new LinkedList<String>();
                    try {
                        scanFolderForLaF(root, lafList,
                                ClassPathSupport.createClassPath(new URL[] { root.getURL() }));
                    } catch(IOException ex) {
                        Logger.getLogger(ProjectCustomizerPanel.class.getClass().getName()).
                                log(Level.SEVERE, "JAR file scanning failed", ex); // NOI18N
                    }
                    if (!lafList.isEmpty()) {
                        for (int i = LAF_DISPLAY_NAMES.length-1; i >=0; i--) {
                            lafList.add(0, LAF_DISPLAY_NAMES[i]);
                        }
                        lafCombo.setModel(new DefaultComboBoxModel(lafList.toArray()));
                        lafCombo.setSelectedIndex(LAF_DISPLAY_NAMES.length);
                        lafJarRoot = root;
                    } else {
                        NotifyDescriptor d = new NotifyDescriptor.Message(
                                NbBundle.getMessage(ProjectCustomizerPanel.class, "MSG_NoLafFound")); // NOI18N
                        DialogDisplayer.getDefault().notify(d);
                    }
                }
                fileChooserDir = file.getParent();
            }
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    private static void scanFolderForLaF(FileObject folder, List<String> classList, ClassPath jarCP)
            throws IOException {
        for (FileObject fo : folder.getChildren()) {
            if (fo.isFolder()) {
                scanFolderForLaF(fo, classList, jarCP);
            } else if ("class".equals(fo.getExt()) && DataObject.find(fo) != null) { // NOI18N
                String lafClsName = scanClassFile(fo, classList, jarCP);
                if (lafClsName != null) {
                    classList.add(lafClsName);
                }
            }
        }
    }

    private static String scanClassFile(FileObject classFO, List<String> classList, ClassPath jarCP)
            throws IOException {
        // TODO rewrite this to use javax.lang.model.element.* as soon as JavaSource introduce .class files support
        InputStream is = null;
        ClassFile clazz;
        try {
            is = classFO.getInputStream();
            clazz = new ClassFile(is, false);
        } finally {
            if (is != null) {
                is.close();
            }
        }
        if (clazz != null) {
            int access = clazz.getAccess();
            if (Modifier.isPublic(access) && !Modifier.isAbstract(access) &&
                    !Modifier.isInterface(access) && !clazz.isAnnotation() &&
                    !clazz.isEnum() && !clazz.isSynthetic()
                    && (clazz.getSuperClass() != null)) {
                String superName = clazz.getSuperClass().getExternalName();
                FileObject fo = jarCP.findResource(superName.replace('.', '/') + ".class"); // NOI18N
                if (isStandardLAFSuperClass(superName)
                        || (fo != null && scanClassFile(fo, classList, jarCP) != null)) {
                    return clazz.getName().getExternalName();
                }
            }
        }
        return null;
    }

    private static boolean isStandardLAFSuperClass(String name) {
        return name.startsWith("javax.swing") // performance only // NOI18N
                && ("javax.swing.LookAndFeel".equals(name) // NOI18N
                    || "javax.swing.plaf.metal.MetalLookAndFeel".equals(name) // NOI18N
                    || "javax.swing.plaf.basic.BasicLookAndFeel".equals(name) // NOI18N
                    || "javax.swing.plaf.synth.SynthLookAndFeel".equals(name)); // NOI18N
    }

    private ComboBoxModel lafComboModel() {
        DefaultComboBoxModel model = new DefaultComboBoxModel(LAF_DISPLAY_NAMES);
        for (LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
            model.addElement(laf.getClassName());
        }
        return model;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField appIdTextField;
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JComboBox lafCombo;
    private javax.swing.JTextField vendorIdTextField;
    // End of variables declaration//GEN-END:variables

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.swingapp.ProjectCustomizerPanel"); // NOI18N
    }
    
}
