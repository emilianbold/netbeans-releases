/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.makeproject.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.utils.FileFilterFactory;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;

public class SelectExecutablePanel extends javax.swing.JPanel {

    private JList exeList;
    private FileFilter elfExecutableFileFilter = FileFilterFactory.getElfExecutableFileFilter();
    private FileFilter exeExecutableFileFilter = FileFilterFactory.getPeExecutableFileFilter();
    private FileFilter machOExecutableFileFilter = FileFilterFactory.getMacOSXExecutableFileFilter();
    private DocumentListener documentListener;
    private DialogDescriptor dialogDescriptor;
    private MakeConfiguration conf;

    /** Creates new form SelectExecutable */
    public SelectExecutablePanel(MakeConfiguration conf) {
        this.conf = conf;
        initComponents();
        instructionsTextArea.setBackground(getBackground());

        File root = new File(conf.getMakefileConfiguration().getAbsBuildCommandWorkingDir());
        String[] executables = findAllExecutables(root);
        exeList = new JList(executables);
        executableList.setViewportView(exeList);

        exeList.addListSelectionListener(new MyListSelectionListener());

        if (executables.length > 0) {
            exeList.setSelectedIndex(0);
        }

        documentListener = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                validateExe();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                validateExe();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validateExe();
            }
        };
        executableTextField.getDocument().addDocumentListener(documentListener);

        setPreferredSize(new java.awt.Dimension(600, 300));

        validateExe();
    }

    public void setDialogDescriptor(DialogDescriptor dialogDescriptor) {
        this.dialogDescriptor = dialogDescriptor;
        validateExe();
    }

    private final class MyListSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting() == false) {
                int i = exeList.getSelectedIndex();
                if (i >= 0) {
                    executableTextField.setText((String) exeList.getSelectedValue());
                    validateExe();
                }
            }
        }
    }

    private void validateExe() {
        String errorText = null;
        File exe = new File(executableTextField.getText());
        if (executableTextField.getText().length() == 0) {
            errorText = getString("NO_EXE_ERROR");
        } else if (!exe.exists()) {
            errorText = getString("EXE_DOESNT_EXISTS");
        } else if (exe.isDirectory() || (!elfExecutableFileFilter.accept(exe) && !exeExecutableFileFilter.accept(exe) && !machOExecutableFileFilter.accept(exe))) {
            errorText = getString("FILE_NOT_AN_EXECUTABLE");
        }
        if (errorText != null) {
            errorLabel.setText(errorText);
            if (dialogDescriptor != null) {
                dialogDescriptor.setValid(false);
            }
        } else {
            errorLabel.setText(" "); // NOI18N
            if (dialogDescriptor != null) {
                dialogDescriptor.setValid(true);
            }
        }
    }

    public String getExecutable() {
        return executableTextField.getText();
    }

    private String[] findAllExecutables(File root) {
        if (!root.exists() || !root.isDirectory()) {
            // Something is wrong
            return new String[]{};
        }
        ArrayList<String> aLlist = new ArrayList<String>();
        addExecutables(root, aLlist);
        return aLlist.toArray(new String[aLlist.size()]);
    }

    private void addExecutables(File dir, List<String> filesAdded) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                // FIXUP: is this the best way to deal with files under SCCS?
                // Unfortunately the SCCS directory contains data files with the same
                // suffixes as the the source files, and a simple file filter based on
                // a file's suffix cannot see the difference between the source file and
                // the data file. Only the source file should be added.
                if (files[i].getName().equals("SCCS")) // NOI18N
                {
                    continue;
                }
                addExecutables(files[i], filesAdded);
            } else {
                if (FileFilterFactory.getAllFileFilter().accept(files[i])) {
                    continue;
                }
                if (conf.getDevelopmentHost().getBuildPlatform() == PlatformTypes.PLATFORM_WINDOWS) {
                    if (exeExecutableFileFilter.accept(files[i])) {
                        filesAdded.add(files[i].getPath());
                    }
                } else if (conf.getDevelopmentHost().getBuildPlatform() == PlatformTypes.PLATFORM_MACOSX) {
                    if (machOExecutableFileFilter.accept(files[i])) {
                        filesAdded.add(files[i].getPath());
                    }
                } else {
                    if (elfExecutableFileFilter.accept(files[i])) {
                        filesAdded.add(files[i].getPath());
                    }
                }
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        instructionsTextArea = new javax.swing.JTextArea();
        ExecutableListLabel = new javax.swing.JLabel();
        executableList = new javax.swing.JScrollPane();
        list = new javax.swing.JList();
        executableLabel = new javax.swing.JLabel();
        executableTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        errorLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        instructionsTextArea.setEditable(false);
        instructionsTextArea.setLineWrap(true);
        instructionsTextArea.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/Bundle").getString("GUIDANCE_TEXT"));
        instructionsTextArea.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(instructionsTextArea, gridBagConstraints);

        ExecutableListLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/Bundle").getString("LIST_LABEL_MN").charAt(0));
        ExecutableListLabel.setLabelFor(executableList);
        ExecutableListLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/Bundle").getString("LIST_LABEL_TEXT"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 12, 0, 12);
        add(ExecutableListLabel, gridBagConstraints);

        executableList.setViewportView(list);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 12);
        add(executableList, gridBagConstraints);

        executableLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/Bundle").getString("EXECUTABLE_MN").charAt(0));
        executableLabel.setLabelFor(executableTextField);
        executableLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/Bundle").getString("EXECUTABLE_TEXT"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 12, 0, 12);
        add(executableLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(executableTextField, gridBagConstraints);

        browseButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/Bundle").getString("BROWSE_BUTTON_MN").charAt(0));
        browseButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/Bundle").getString("BROWSE_BUTTON_TEXT"));
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 12);
        add(browseButton, gridBagConstraints);

        errorLabel.setForeground(new java.awt.Color(255, 51, 51));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 12, 0, 0);
        add(errorLabel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        String seed;
        if (executableTextField.getText().length() > 0) {
            seed = executableTextField.getText();
        } else if (FileChooser.getCurrectChooserFile() != null) {
            seed = FileChooser.getCurrectChooserFile().getPath();
        } else {
            seed = System.getProperty("user.home"); // NOI18N
        }
        FileFilter[] filters;
        if (conf.getDevelopmentHost().getBuildPlatform() == PlatformTypes.PLATFORM_WINDOWS) {
            filters = new FileFilter[]{FileFilterFactory.getPeExecutableFileFilter()};
        } else if (conf.getDevelopmentHost().getBuildPlatform() == PlatformTypes.PLATFORM_MACOSX) {
            filters = new FileFilter[]{FileFilterFactory.getMacOSXExecutableFileFilter()};
        } else {
            filters = new FileFilter[]{FileFilterFactory.getElfExecutableFileFilter()};
        }
        JFileChooser fileChooser = new FileChooser(
                getString("CHOOSER_TITLE_TXT"),
                getString("CHOOSER_BUTTON_TXT"),
                JFileChooser.FILES_ONLY,
                filters,
                seed,
                false);
        int ret = fileChooser.showOpenDialog(this);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }

        String path = CndPathUtilitities.normalize(fileChooser.getSelectedFile().getPath());
        executableTextField.setText(path);
    }//GEN-LAST:event_browseButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel ExecutableListLabel;
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JLabel executableLabel;
    private javax.swing.JScrollPane executableList;
    private javax.swing.JTextField executableTextField;
    private javax.swing.JTextArea instructionsTextArea;
    private javax.swing.JList list;
    // End of variables declaration//GEN-END:variables

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(SelectExecutablePanel.class, s);
    }
}
