/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.spi.java.project.support.ui;

import java.io.File;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;


final class MakeSharableVisualPanel1 extends JPanel {

    private AntProjectHelper helper;
    private ChangeSupport support;
    private WizardDescriptor settings;
    private DocumentListener docListener;

    /** Creates new form MakeSharableVisualPanel1 */
    public MakeSharableVisualPanel1(ChangeSupport supp) {
        initComponents();
        this.support = supp;
        docListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { 
                support.fireChange();
            }

            public void removeUpdate(DocumentEvent e) {
                support.fireChange();
            }

            public void changedUpdate(DocumentEvent e) {
                support.fireChange();
            }
            
        };
        txtDefinition.getDocument().addDocumentListener(docListener);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(MakeSharableVisualPanel1.class, "TIT_LibraryDefinitionSelection"); //NOI18N
    }

    boolean isValidPanel() {
        String location = getLibraryLocation();
        boolean wrong = false;
        if (new File(location).isAbsolute()) {
            settings.putProperty("WizardPanel_errorMessage", // NOI18N
                    org.openide.util.NbBundle.getMessage(MakeSharableVisualPanel1.class, "WARN_MakeSharable.absolutePath"));
            wrong = true;
        } else {
            File projectLoc = helper.resolveFile(location);
            File libLoc = PropertyUtils.resolveFile(projectLoc, location);
            if (!CollocationQuery.areCollocated(projectLoc, libLoc)) {
                settings.putProperty("WizardPanel_errorMessage", // NOI18N
                        org.openide.util.NbBundle.getMessage(MakeSharableVisualPanel1.class, "WARN_makeSharable.relativePath"));
                wrong = true;
            }
        }
        if (!wrong) {
            settings.putProperty("WizardPanel_errorMessage", null);// NOI18N
        }

        return true;
    }

    private String getLibraryLocation() {
        return txtDefinition.getText().trim();
    }

    private void setLibraryLocation(String loc) {
        txtDefinition.setText(loc);
    }

    void readSettings(WizardDescriptor wiz) {
        settings = wiz;
        String loc = (String) wiz.getProperty(SharableLibrariesUtils.PROP_LOCATION);
        helper = (AntProjectHelper) wiz.getProperty(SharableLibrariesUtils.PROP_HELPER);
        if (loc == null) {
            loc = "." + File.separator + "lib"; //NOI18N
        } else {
            loc = loc.substring(0, loc.length() - SharableLibrariesUtils.DEFAULT_LIBRARIES_FILENAME.length());
        }
        setLibraryLocation(loc);
        support.fireChange();
    }

    void storeSettings(WizardDescriptor wiz) {
        String librariesDefinition = getLibraryLocation();
        if (librariesDefinition != null) {
            if (!librariesDefinition.endsWith(File.separator)) {
                librariesDefinition += File.separatorChar;
            }
            librariesDefinition += SharableLibrariesUtils.DEFAULT_LIBRARIES_FILENAME;
        }

        wiz.putProperty(SharableLibrariesUtils.PROP_LOCATION, librariesDefinition);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblDefinition = new javax.swing.JLabel();
        txtDefinition = new javax.swing.JTextField();
        btnDefinition = new javax.swing.JButton();
        lblNote = new javax.swing.JLabel();

        lblDefinition.setLabelFor(txtDefinition);
        org.openide.awt.Mnemonics.setLocalizedText(lblDefinition, org.openide.util.NbBundle.getMessage(MakeSharableVisualPanel1.class, "MakeSharableVisualPanel1.lblDefinition.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnDefinition, org.openide.util.NbBundle.getMessage(MakeSharableVisualPanel1.class, "MakeSharableVisualPanel1.btnDefinition.text")); // NOI18N
        btnDefinition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDefinitionActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lblNote, org.openide.util.NbBundle.getMessage(MakeSharableVisualPanel1.class, "MakeSharableVisualPanel1.lblNote.text")); // NOI18N
        lblNote.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(lblDefinition)
                .add(10, 10, 10)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lblNote, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(txtDefinition, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(btnDefinition))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(btnDefinition)
                    .add(lblDefinition)
                    .add(txtDefinition, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblNote, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 129, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(116, Short.MAX_VALUE))
        );

        lblDefinition.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MakeSharableVisualPanel1.class, "ACSD_lblDefinition")); // NOI18N
        txtDefinition.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MakeSharableVisualPanel1.class, "ACSD_lblDefinition")); // NOI18N
        btnDefinition.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MakeSharableVisualPanel1.class, "ACSD_btnDefinition")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void btnDefinitionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDefinitionActionPerformed
        File f = FileUtil.toFile(helper.getProjectDirectory()); // NOI18N
        String curr = SharableLibrariesUtils.browseForLibraryLocation(getLibraryLocation(), this, f);
        if (curr != null) {
            setLibraryLocation(curr);
        }

}//GEN-LAST:event_btnDefinitionActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDefinition;
    private javax.swing.JLabel lblDefinition;
    private javax.swing.JLabel lblNote;
    private javax.swing.JTextField txtDefinition;
    // End of variables declaration//GEN-END:variables
}

