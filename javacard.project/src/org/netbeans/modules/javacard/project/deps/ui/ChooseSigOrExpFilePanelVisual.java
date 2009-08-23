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
package org.netbeans.modules.javacard.project.deps.ui;

import java.io.File;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Tim Boudreau
 */
public class ChooseSigOrExpFilePanelVisual extends javax.swing.JPanel implements DocumentListener {

    private final WizardDescriptor wiz;
    private final ChangeSupport supp = new ChangeSupport(this);

    public ChooseSigOrExpFilePanelVisual(WizardDescriptor desc) {
        this.wiz = desc;
        initComponents();
        fileField.getDocument().addDocumentListener(this);
    }
    private IntermediatePanelKind kind;

    IntermediatePanelKind getKind() {
        return kind;
    }

    public void removeChangeListener(ChangeListener listener) {
        supp.removeChangeListener(listener);
    }

    public void addChangeListener(ChangeListener listener) {
        supp.addChangeListener(listener);
    }

    @Override
    public String getName() {
        String key = kind == null ? "NAME_EXP_FILE_PANEL" : //NOI18N
            kind == IntermediatePanelKind.EXP_FILE ?
            "NAME_EXP_FILE_PANEL" : "NAME_SIG_FILE_PANEL"; //NOI18N
        return NbBundle.getMessage(ChooseSigOrExpFilePanelVisual.class, key);
    }

    void setKind(IntermediatePanelKind kind) {
        this.kind = kind;
        if (kind != null) {
            descriptionLabel.setText (kind.getDescription());
            switch (kind) {
                case EXP_FILE:
                    fileLabel.setText(NbBundle.getMessage(ChooseSigOrExpFilePanelVisual.class,
                            "LBL_EXP_FILE")); //NOI18N
                    break;
                case SIG_FILE:
                    fileLabel.setText(NbBundle.getMessage(ChooseSigOrExpFilePanelVisual.class,
                            "LBL_SIG_FILE")); //NOI18N
            }
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        fileLabel = new javax.swing.JLabel();
        fileField = new javax.swing.JTextField();
        browseFileButton = new javax.swing.JButton();
        descriptionLabel = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setLayout(new java.awt.GridBagLayout());

        fileLabel.setText(org.openide.util.NbBundle.getMessage(ChooseSigOrExpFilePanelVisual.class, "ChooseSigOrExpFilePanelVisual.fileLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 12, 0);
        add(fileLabel, gridBagConstraints);

        fileField.setText(org.openide.util.NbBundle.getMessage(ChooseSigOrExpFilePanelVisual.class, "ChooseSigOrExpFilePanelVisual.fileField.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 140;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 5, 12, 5);
        add(fileField, gridBagConstraints);

        browseFileButton.setText(org.openide.util.NbBundle.getMessage(ChooseSigOrExpFilePanelVisual.class, "ChooseSigOrExpFilePanelVisual.browseFileButton.text")); // NOI18N
        browseFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseFileButtononBrowseOrigin(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 12, 0);
        add(browseFileButton, gridBagConstraints);

        descriptionLabel.setText(org.openide.util.NbBundle.getMessage(ChooseSigOrExpFilePanelVisual.class, "ChooseSigOrExpFilePanelVisual.descriptionLabel.text")); // NOI18N
        descriptionLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 200;
        gridBagConstraints.ipady = 120;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        add(descriptionLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    boolean valid() {
        String s = fileField.getText().trim();
        boolean result = s.length() == 0 && kind == IntermediatePanelKind.EXP_FILE;
        if (!result) {
            switch (kind) {
                case EXP_FILE:
                    File f = new File(s);
                    if (!f.exists()) {
                        result = false;
                        wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                                NbBundle.getMessage(ChooseSigOrExpFilePanelVisual.class,
                                "ERR_FILE_DOES_NOT_EXIST", kind)); //NOI18N
                    } else if (f.isDirectory()) {
                        result = false;
                        wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                                NbBundle.getMessage(ChooseSigOrExpFilePanelVisual.class,
                                "ERR_FILE_IS_A_DIRECTORY", kind)); //NOI18N
                    } else {
                        result = true;
                    }
                    break;
                case SIG_FILE:
                    if (s.length() == 0) {
                        result = false;
                        wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                                NbBundle.getMessage(ChooseSigOrExpFilePanelVisual.class,
                                "ERR_SIG_FILE_NOT_SET", kind)); //NOI18N
                    } else {
                        f = new File(s);
                        if (!f.exists()) {
                            result = false;
                            wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                                    NbBundle.getMessage(ChooseSigOrExpFilePanelVisual.class,
                                    "ERR_FILE_DOES_NOT_EXIST", kind)); //NOI18N
                        } else if (f.isDirectory()) {
                            result = false;
                            wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                                    NbBundle.getMessage(ChooseSigOrExpFilePanelVisual.class,
                                    "ERR_FILE_IS_A_DIRECTORY", kind)); //NOI18N
                        } else {
                            result = true;
                        }
                    }
                    break;
                default:
                    throw new AssertionError();
            }
        }
        if (result) {
            wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        }
        return result;
    }

    public File getFile() {
        String s = fileField.getText().trim();
        if (s.length() == 0) {
            return null;
        }
        return new File(s);
    }

    public void setFile (File f) {
        fileField.setText(f == null ? "" : f.getAbsolutePath());
    }

    private void browseFileButtononBrowseOrigin(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseFileButtononBrowseOrigin
        File f;
        if ((f = new FileChooserBuilder(ChooseOriginPanelVisual.class).setFileFilter(kind == IntermediatePanelKind.EXP_FILE ? new ExpFileFilter() : new SigFilter()).
                setTitle(kind.toString()).
                setFilesOnly(true).
                showOpenDialog()) != null) {
            fileField.setText(f.getAbsolutePath());
        }
}//GEN-LAST:event_browseFileButtononBrowseOrigin
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseFileButton;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JTextField fileField;
    private javax.swing.JLabel fileLabel;
    // End of variables declaration//GEN-END:variables

    public void insertUpdate(DocumentEvent e) {
        supp.fireChange();
    }

    public void removeUpdate(DocumentEvent e) {
        insertUpdate(e);
    }

    public void changedUpdate(DocumentEvent e) {
        insertUpdate(e);
    }
}
