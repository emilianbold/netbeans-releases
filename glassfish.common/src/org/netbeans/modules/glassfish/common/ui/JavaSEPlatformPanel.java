/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.common.ui;

import java.io.File;
import java.util.Iterator;
import javax.swing.JPanel;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.netbeans.modules.glassfish.common.utils.JavaUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Warning panel showing an information about unsupported Java SE platform used
 * and allowing user to select another one from Java SE platforms registered
 * in NetBeans.
 * <p/>
 * @author Tomas Kraus
 */
public class JavaSEPlatformPanel extends JPanel {


    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Display GlassFish Java SE selector to allow switch Java SE used
     * to run GlassFish.
     * <p/>
     * @param instance GlassFish server instance to be started.
     * @param javaHome Java SE home currently selected.
     */
    public static FileObject selectServerSEPlatform(
            GlassfishInstance instance, File javaHome) {
        FileObject selectedJavaHome = null;
        // Matching Java SE home installed platform if exists.
        JavaPlatform platform = JavaUtils.findInstalledPlatform(javaHome);
        String platformName = platform != null
                ? platform.getDisplayName() : javaHome.getAbsolutePath();
        String message = NbBundle.getMessage(
                JavaSEPlatformPanel.class,
                "JavaSEPlatformPanel.Warning", platformName);
        JavaSEPlatformPanel panel = new JavaSEPlatformPanel(instance, message);
        NotifyDescriptor notifyDescriptor = new NotifyDescriptor.Message(
                panel, NotifyDescriptor.PLAIN_MESSAGE);
        DialogDisplayer.getDefault().notify(notifyDescriptor);
        JavaPlatform selectedPlatform = panel.javaPlatform();
        if (selectedPlatform != null) {
            Iterator<FileObject> platformIterator
                    = selectedPlatform.getInstallFolders().iterator();
            if (platformIterator.hasNext()) {
                selectedJavaHome = (FileObject)platformIterator.next();
            }
        }
        if (selectedJavaHome != null && panel.updateProperties()) {
            instance.setJavaHome(
                    FileUtil.toFile(selectedJavaHome).getAbsolutePath());
        }
        return selectedJavaHome;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Warning message to be shown in the panel. */
    private final String message;

    /** Java SE JDK selection label. */
    private final String javaLabelText;

    /** update properties check box label. */
    private final String propertiesLabelText;

    /** Java SE JDK selection content. */
    JavaPlatform[] javaPlatforms;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates new Java SE platform selection panel with message.
     * <p/>
     * @param instance GlassFish server instance used to search
     *                 for supported platforms.
     * @param message  Warning text.
     */
    public JavaSEPlatformPanel(GlassfishInstance instance, String message) {
        this.message = message;
        this.javaLabelText = NbBundle.getMessage(
                JavaSEPlatformPanel.class,
                "JavaSEPlatformPanel.javaLabel");
        this.propertiesLabelText = NbBundle.getMessage(
                JavaSEPlatformPanel.class,
                "JavaSEPlatformPanel.propertiesLabel");
        javaPlatforms = JavaUtils.findSupportedPlatforms(instance);
        initComponents();
    }

    ////////////////////////////////////////////////////////////////////////////
    // GUI Getters                                                            //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Retrieve state of properties update check box.
     * <p/>
     * @return Returns <code>true</code> when properties update check box
     *         was selected or <code>false</code> otherwise.
     */
    boolean updateProperties() {
        return propertiesCheckBox.isSelected();
    }

    /**
     * Retrieve selected Java SE platform from java combo box.
     * <p/>
     * @return Returns {@see JavaPlatform} object of selected Java SE platform.
     */
    JavaPlatform javaPlatform() {
        JavaPlatformsComboBox.Platform platform =
                (JavaPlatformsComboBox.Platform)javaComboBox.getSelectedItem();
        return platform != null ? platform.getPlatform() : null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Generated GUI code                                                     //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        messageLabel = new javax.swing.JLabel();
        javaComboBox = new JavaPlatformsComboBox(javaPlatforms, false);
        javaLabel = new javax.swing.JLabel();
        propertiesLabel = new javax.swing.JLabel();
        propertiesCheckBox = new javax.swing.JCheckBox();

        setMaximumSize(new java.awt.Dimension(400, 200));
        setMinimumSize(new java.awt.Dimension(400, 150));
        setName(""); // NOI18N
        setPreferredSize(new java.awt.Dimension(400, 150));

        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, this.message);

        org.openide.awt.Mnemonics.setLocalizedText(javaLabel, this.javaLabelText);

        org.openide.awt.Mnemonics.setLocalizedText(propertiesLabel, this.propertiesLabelText);

        propertiesCheckBox.setSelected(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(messageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(javaLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(javaComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(propertiesLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(propertiesCheckBox)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(12, 12, 12))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(messageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(javaComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(javaLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(propertiesLabel)
                    .addComponent(propertiesCheckBox))
                .addContainerGap(18, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox javaComboBox;
    private javax.swing.JLabel javaLabel;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JCheckBox propertiesCheckBox;
    private javax.swing.JLabel propertiesLabel;
    // End of variables declaration//GEN-END:variables
}
