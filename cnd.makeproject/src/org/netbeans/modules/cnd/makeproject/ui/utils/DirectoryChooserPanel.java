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
package org.netbeans.modules.cnd.makeproject.ui.utils;

import org.netbeans.modules.cnd.utils.ui.ListEditorPanel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.util.Vector;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.utils.ui.FileChooser;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class DirectoryChooserPanel extends javax.swing.JPanel implements HelpCtx.Provider, PropertyChangeListener {

    private MyListEditorPanel myListEditorPanel;
    private String baseDir;
    private boolean addPathPanel;
    private BooleanConfiguration inheritValues;
    private PropertyEditorSupport editor;
    private HelpCtx helpCtx;

    public DirectoryChooserPanel(String baseDir, List<String> data, boolean addPathPanel, BooleanConfiguration inheritValues, String inheritText, PropertyEditorSupport editor, PropertyEnv env, HelpCtx helpCtx) {
        this.baseDir = baseDir;
        this.addPathPanel = addPathPanel;
        this.inheritValues = inheritValues;
        this.editor = editor;
        this.helpCtx = helpCtx;
        initComponents();
        myListEditorPanel = new MyListEditorPanel(data);
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        listPanel.add(myListEditorPanel, gridBagConstraints);
        if (inheritValues != null) {
            inheritTextArea.setBackground(inheritPanel.getBackground());
            inheritTextArea.setText(inheritText);
            setPreferredSize(new java.awt.Dimension(450, 330));
            inheritCheckBox.setSelected(inheritValues.getValue());
        } else {
            remove(inheritPanel);
            //setPreferredSize(new java.awt.Dimension(450, 350));
            setPreferredSize(new java.awt.Dimension(450, 220));
        }

        env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        env.addPropertyChangeListener(this);

        // Accessibility
        inheritCheckBox.getAccessibleContext().setAccessibleDescription(getString("INHERIT_CHECKBOX_AD"));
    }

    public void setInstructionsText(String txt) {
        //instructionsTextArea.setText(txt);
    }

    public void setListData(List<String> data) {
        myListEditorPanel.setListData(data);
    }

    public List<String> getListData() {
        return myListEditorPanel.getListData();
    }

    private Object getPropertyValue() throws IllegalStateException {
        return new ArrayList<String>(getListData());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID) {
            editor.setValue(getPropertyValue());
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return helpCtx;
//        return new HelpCtx("RuntimeSearchDirectories"); // NOI18N
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        listPanel = new javax.swing.JPanel();
        inheritPanel = new javax.swing.JPanel();
        scrollPanel = new javax.swing.JScrollPane();
        panel = new javax.swing.JPanel();
        inheritLabel = new javax.swing.JLabel();
        inheritTextArea = new javax.swing.JTextArea();
        inheritCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(323, 223));
        listPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(listPanel, gridBagConstraints);

        inheritPanel.setLayout(new java.awt.GridBagLayout());

        scrollPanel.setBorder(null);
        panel.setLayout(new java.awt.GridBagLayout());

        inheritLabel.setLabelFor(inheritTextArea);
        inheritLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/utils/Bundle").getString("INHERITED_VALUES_LBL"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        panel.add(inheritLabel, gridBagConstraints);

        inheritTextArea.setEditable(false);
        inheritTextArea.setLineWrap(true);
        inheritTextArea.setWrapStyleWord(true);
        inheritTextArea.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        panel.add(inheritTextArea, gridBagConstraints);

        inheritCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/utils/Bundle").getString("INHERIT_CHECKBOX_MN").charAt(0));
        inheritCheckBox.setSelected(true);
        inheritCheckBox.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/utils/Bundle").getString("INHERIT_CHECKBOX_LBL"));
        inheritCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inheritCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        panel.add(inheritCheckBox, gridBagConstraints);

        scrollPanel.setViewportView(panel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        inheritPanel.add(scrollPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(inheritPanel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void inheritCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inheritCheckBoxActionPerformed
        inheritValues.setValue(inheritCheckBox.isSelected());
    }//GEN-LAST:event_inheritCheckBoxActionPerformed

    private class MyListEditorPanel extends ListEditorPanel<String> {

        public MyListEditorPanel(List<String> objects) {
            super(objects);
            getDefaultButton().setVisible(false);
        }

        @Override
        public String addAction() {
            String seed = null;
            if (FileChooser.getCurrectChooserFile() != null) {
                seed = FileChooser.getCurrectChooserFile().getPath();
            }
            if (seed == null) {
                seed = baseDir;
            }
            FileChooser fileChooser = new FileChooser(getString("ADD_DIRECTORY_DIALOG_TITLE"), getString("ADD_DIRECTORY_BUTTON_TXT"), JFileChooser.DIRECTORIES_ONLY, null, seed, true);
            PathPanel pathPanel = null;
            if (addPathPanel) {
                pathPanel = new PathPanel();
            }
            fileChooser.setAccessory(pathPanel);
            int ret = fileChooser.showOpenDialog(this);
            if (ret == JFileChooser.CANCEL_OPTION) {
                return null;
            }
            String itemPath = fileChooser.getSelectedFile().getPath();
            itemPath = CndPathUtilitities.naturalize(itemPath);
            String bd = baseDir;
            bd = CndPathUtilitities.naturalize(bd);
            if (pathPanel != null && MakeProjectOptions.getPathMode() == MakeProjectOptions.REL_OR_ABS) {
                itemPath = CndPathUtilitities.toAbsoluteOrRelativePath(bd, itemPath);
            } else if (pathPanel != null && MakeProjectOptions.getPathMode() == MakeProjectOptions.REL) {
                itemPath = CndPathUtilitities.toRelativePath(bd, itemPath);
            }
            itemPath = CndPathUtilitities.normalize(itemPath);
            return itemPath;
        }

        @Override
        public String getListLabelText() {
            return getString("DIRECTORIES_LABEL_TXT");
        }

        @Override
        public char getListLabelMnemonic() {
            return getString("DIRECTORIES_LABEL_MN").charAt(0);
        }

        @Override
        public String getAddButtonText() {
            return getString("ADD_BUTTON_TXT");
        }

        @Override
        public char getAddButtonMnemonics() {
            return getString("ADD_BUTTON_MN").charAt(0);
        }

        @Override
        public String getRenameButtonText() {
            return getString("EDIT_BUTTON_TXT");
        }

        @Override
        public char getRenameButtonMnemonics() {
            return getString("EDIT_BUTTON_MN").charAt(0);
        }

        @Override
        public String getDownButtonText() {
            return getString("DOWN_BUTTON_TXT");
        }

        @Override
        public char getDownButtonMnemonics() {
            return getString("DOWN_BUTTON_MN").charAt(0);
        }

        @Override
        public String copyAction(String o) {
            return o;
        }

        @Override
        public void editAction(String o) {
            String s = o;

            NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine(getString("EDIT_DIALOG_LABEL_TXT"), getString("EDIT_DIALOG_TITLE_TXT"));
            notifyDescriptor.setInputText(s);
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            if (notifyDescriptor.getValue() != NotifyDescriptor.OK_OPTION) {
                return;
            }
            String newS = notifyDescriptor.getInputText();
            List<String> vector = super.getListData();
            Object[] arr = vector.toArray();
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == o) {
                    vector.remove(i);
                    vector.add(i, newS);
                    break;
                }
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox inheritCheckBox;
    private javax.swing.JLabel inheritLabel;
    private javax.swing.JPanel inheritPanel;
    private javax.swing.JTextArea inheritTextArea;
    private javax.swing.JPanel listPanel;
    private javax.swing.JPanel panel;
    private javax.swing.JScrollPane scrollPanel;
    // End of variables declaration//GEN-END:variables

    private static String getString(String key) {
        return NbBundle.getMessage(DirectoryChooserPanel.class, key);
    }
}
