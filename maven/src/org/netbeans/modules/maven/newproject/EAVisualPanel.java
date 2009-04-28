/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.newproject;

import java.awt.Color;
import java.io.CharConversionException;
import java.io.File;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;

public final class EAVisualPanel extends JPanel implements DocumentListener {

    private static final String ERROR_MSG = WizardDescriptor.PROP_ERROR_MESSAGE;

    private EAWizardPanel panel;

    private Color origEarC, origWebC, origEjbC;

    /** Creates new form EAVisualPanel */
    public EAVisualPanel(EAWizardPanel panel) {
        this.panel = panel;
        initComponents();

        origEarC = tfEar.getForeground();
        origEjbC = tfEjb.getForeground();
        origWebC = tfWeb.getForeground();

        tfEar.getDocument().addDocumentListener(this);
        tfEjb.getDocument().addDocumentListener(this);
        tfWeb.getDocument().addDocumentListener(this);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(EAWizardPanel.class, "LBL_EESettings");
    }

    void readSettings(WizardDescriptor wizardDescriptor) {
        fillTextFields(wizardDescriptor);
    }

    void storeSettings(WizardDescriptor d) {
        int eeLevelIdx = Math.max(cmbEEVersion.getSelectedIndex(), 0);

        File parent = (File) d.getProperty("projdir");
        String earText = tfEar.getText().trim();
        d.putProperty("ear_projdir", new File(parent, earText));
        ProjectInfo pi = new ProjectInfo();
        pi.groupId = (String)d.getProperty("groupId");
        pi.artifactId = earText;
        pi.version = (String)d.getProperty("version");
        d.putProperty("ear_versionInfo", pi);
        d.putProperty("ear_archetype", ArchetypeWizardUtils.EAR_ARCHS[eeLevelIdx]);

        if (chkEjb.isSelected()) {
            String ejbText = tfEjb.getText().trim();
            d.putProperty("ejb_projdir", new File(parent, ejbText));
            pi = new ProjectInfo();
            pi.groupId = (String)d.getProperty("groupId");
            pi.artifactId = ejbText;
            pi.version = (String)d.getProperty("version");
            d.putProperty("ejb_versionInfo", pi);
            d.putProperty("ejb_archetype", ArchetypeWizardUtils.EJB_ARCHS[eeLevelIdx]);
        } else {
            d.putProperty("ejb_projdir", null);
            d.putProperty("ejb_versionInfo", null);
            d.putProperty("ejb_archetype", null);
        }

        if (chkWeb.isSelected()) {
            String webText = tfWeb.getText().trim();
            d.putProperty("web_projdir", new File(parent, webText));
            pi = new ProjectInfo();
            pi.groupId = (String)d.getProperty("groupId");
            pi.artifactId = webText;
            pi.version = (String)d.getProperty("version");
            d.putProperty("web_versionInfo", pi);
            d.putProperty("web_archetype", ArchetypeWizardUtils.WEB_APP_ARCHS[eeLevelIdx]);

        } else {
            d.putProperty("web_projdir", null);
            d.putProperty("web_versionInfo", null);
            d.putProperty("web_archetype", null);
        }
    }

    boolean valid(WizardDescriptor wizDesc) {
        tfEar.setForeground(origEarC);
        tfEjb.setForeground(origEjbC);
        tfWeb.setForeground(origWebC);
        if (!validateProjDir(tfEar.getText(), wizDesc)
                || !validateCoordinate(tfEar.getText(), wizDesc)) {
            tfEar.setForeground(Color.RED);
            return false;
        }
        if (chkEjb.isSelected()) {
            if (!validateProjDir(tfEjb.getText(), wizDesc) ||
                !validateCoordinate(tfEjb.getText(), wizDesc)) {
                tfEjb.setForeground(Color.RED);
                return false;
            }
        }
        if (chkWeb.isSelected()) {
            if (!validateProjDir(tfWeb.getText(), wizDesc) ||
                !validateCoordinate(tfWeb.getText(), wizDesc)) {
                tfWeb.setForeground(Color.RED);
                return false;
            }
        }
        wizDesc.putProperty(ERROR_MSG, ""); //NOI18N
        return true;
    }

    private static boolean validateProjDir (String dirName, WizardDescriptor wizDesc) {
        if (dirName.length() == 0) {
            wizDesc.putProperty(ERROR_MSG,
                    NbBundle.getMessage(EAVisualPanel.class, "ERR_Project_Name_is_not_valid"));
            return false;
        }

        if(dirName.indexOf(File.separatorChar) != -1) {
            wizDesc.putProperty(ERROR_MSG,
                    NbBundle.getMessage(EAVisualPanel.class, "ERR_Project_Name_has_slash"));
            return false;
        }

        File parent = (File) wizDesc.getProperty("projdir");
        File projLoc = FileUtil.normalizeFile(new File(parent, dirName));
        File f = projLoc;
        while (f != null && !f.exists()) {
            f = f.getParentFile();
        }
        if (f == null || !f.canWrite()) {
            wizDesc.putProperty(ERROR_MSG, //NOI18N
                    NbBundle.getMessage(EAVisualPanel.class, "ERR_Project_Folder_cannot_be_created"));
            return false;
        }

        if (FileUtil.toFileObject(f) == null) {
            String message = NbBundle.getMessage(EAVisualPanel.class, "ERR_Project_Folder_is_not_valid_path");
            wizDesc.putProperty(ERROR_MSG, message); //NOI18N
            return false;
        }

        File[] kids = projLoc.listFiles();
        if (projLoc.exists() && kids != null && kids.length > 0) {
            // Folder exists and is not empty
            wizDesc.putProperty(ERROR_MSG,
                    NbBundle.getMessage(EAVisualPanel.class, "ERR_Project_Folder_exists"));
            return false;
        }

        wizDesc.putProperty(ERROR_MSG, null);
        return true;
    }

    static boolean validateCoordinate (String coord, WizardDescriptor wizDesc) {
        boolean result = false;
        try {
            String escaped = XMLUtil.toAttributeValue(coord);
            result = escaped.length() == coord.length() && coord.indexOf(">") == -1;
        } catch (CharConversionException ex) {
            // ignore this one
        }
        wizDesc.putProperty(ERROR_MSG, result ? null :
            NbBundle.getMessage(EAVisualPanel.class, "ERR_Coord_breaks_pom"));

        return result;
    }

    private void fillTextFields(WizardDescriptor wiz) {
        String artifId = (String)wiz.getProperty("artifactId");
        tfEar.setText(artifId + "-ear");
        tfWeb.setText(artifId + "-web");
        tfEjb.setText(artifId + "-ejb");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cmbEEVersion = new javax.swing.JComboBox();
        lblEEVersion = new javax.swing.JLabel();
        chkEjb = new javax.swing.JCheckBox();
        chkWeb = new javax.swing.JCheckBox();
        lblEar = new javax.swing.JLabel();
        tfWeb = new javax.swing.JTextField();
        tfEjb = new javax.swing.JTextField();
        tfEar = new javax.swing.JTextField();

        cmbEEVersion.setModel(new DefaultComboBoxModel(ArchetypeWizardUtils.EE_LEVELS));

        lblEEVersion.setLabelFor(cmbEEVersion);
        org.openide.awt.Mnemonics.setLocalizedText(lblEEVersion, org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.lblEEVersion.text")); // NOI18N

        chkEjb.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(chkEjb, org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.chkEjb.text")); // NOI18N
        chkEjb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkEjbActionPerformed(evt);
            }
        });

        chkWeb.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(chkWeb, org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.chkWeb.text")); // NOI18N
        chkWeb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkWebActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblEar, org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.lblEar.text")); // NOI18N

        tfWeb.setText(org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.tfWeb.text")); // NOI18N

        tfEjb.setText(org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.tfEjb.text")); // NOI18N

        tfEar.setText(org.openide.util.NbBundle.getMessage(EAVisualPanel.class, "EAVisualPanel.tfEar.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(lblEEVersion)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cmbEEVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(chkWeb)
                            .add(chkEjb)
                            .add(lblEar))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(tfEjb, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                            .add(tfWeb, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                            .add(tfEar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lblEEVersion)
                    .add(cmbEEVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tfEjb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(chkEjb))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(chkWeb)
                    .add(tfWeb, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tfEar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lblEar))
                .addContainerGap(174, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void chkEjbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkEjbActionPerformed
        // TODO add your handling code here:
        tfEjb.setEnabled(chkEjb.isSelected());
        panel.fireChangeEvent();
    }//GEN-LAST:event_chkEjbActionPerformed

    private void chkWebActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkWebActionPerformed
        // TODO add your handling code here:
        tfWeb.setEnabled(chkWeb.isSelected());
        panel.fireChangeEvent();
    }//GEN-LAST:event_chkWebActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox chkEjb;
    private javax.swing.JCheckBox chkWeb;
    private javax.swing.JComboBox cmbEEVersion;
    private javax.swing.JLabel lblEEVersion;
    private javax.swing.JLabel lblEar;
    private javax.swing.JTextField tfEar;
    private javax.swing.JTextField tfEjb;
    private javax.swing.JTextField tfWeb;
    // End of variables declaration//GEN-END:variables

    /******** Document listener implementation, reacting on text field changes ***/

    public void insertUpdate(DocumentEvent e) {
        panel.fireChangeEvent();
    }

    public void removeUpdate(DocumentEvent e) {
        panel.fireChangeEvent();
    }

    public void changedUpdate(DocumentEvent e) {
        panel.fireChangeEvent();
    }
}

