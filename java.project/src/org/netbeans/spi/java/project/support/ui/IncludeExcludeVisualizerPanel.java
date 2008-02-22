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

import java.awt.Component;
import java.awt.EventQueue;
import java.io.File;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

class IncludeExcludeVisualizerPanel extends JPanel {

    private final IncludeExcludeVisualizer handle;
    private final DocumentListener listener = new DocumentListener() {
        private void changes() {
            handle.changedPatterns(includes.getText(), excludes.getText());
        }
        public void insertUpdate(DocumentEvent e) {
            changes();
        }
        public void removeUpdate(DocumentEvent e) {
            changes();
        }
        public void changedUpdate(DocumentEvent e) {}
    };
    private final DefaultListModel includedListModel = new DefaultListModel();
    private final DefaultListModel excludedListModel = new DefaultListModel();
    private String rootPrefix;

    public IncludeExcludeVisualizerPanel(IncludeExcludeVisualizer handle) {
        this.handle = handle;
        initComponents();
        includes.getDocument().addDocumentListener(listener);
        excludes.getDocument().addDocumentListener(listener);
        includedList.setModel(includedListModel);
        excludedList.setModel(excludedListModel);
        ListCellRenderer renderer = new DefaultListCellRenderer() {
            public @Override Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                File f = (File) value;
                // #99401: just use relative path when possible.
                String label = f.getAbsolutePath();
                if (rootPrefix != null) {
                    assert label.startsWith(rootPrefix) : "Expected " + label + " to start with '" + rootPrefix + "'";
                    label = label.substring(rootPrefix.length()).replace(File.separatorChar, '/');
                }
                return super.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
            }
        };
        includedList.setCellRenderer(renderer);
        excludedList.setCellRenderer(renderer);
    }

    void setFields(String includes, String excludes) {
        assert EventQueue.isDispatchThread();
        this.includes.getDocument().removeDocumentListener(listener);
        this.includes.setText(includes);
        this.includes.getDocument().addDocumentListener(listener);
        this.excludes.getDocument().removeDocumentListener(listener);
        this.excludes.setText(excludes);
        this.excludes.getDocument().addDocumentListener(listener);
    }

    void setFiles(File[] included, File[] excluded, boolean busy, File singleRoot) {
        assert EventQueue.isDispatchThread();
        includedListModel.clear();
        for (File f : included) {
            includedListModel.addElement(f);
        }
        excludedListModel.clear();
        for (File f : excluded) {
            excludedListModel.addElement(f);
        }
        scanningLabel.setVisible(busy);
        if (singleRoot == null) {
            rootPrefix = null;
        } else {
            assert singleRoot.isDirectory() : singleRoot;
            rootPrefix = singleRoot.getAbsolutePath() + File.separatorChar;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        includedListLabel = new javax.swing.JLabel();
        includedListPane = new javax.swing.JScrollPane();
        includedList = new javax.swing.JList();
        excludedListLabel = new javax.swing.JLabel();
        excludedListPane = new javax.swing.JScrollPane();
        excludedList = new javax.swing.JList();
        scanningLabel = new javax.swing.JLabel();
        includesLabel = new javax.swing.JLabel();
        includes = new javax.swing.JTextField();
        excludesLabel = new javax.swing.JLabel();
        excludes = new javax.swing.JTextField();
        explanation = new javax.swing.JLabel();

        includedListLabel.setLabelFor(includedList);
        org.openide.awt.Mnemonics.setLocalizedText(includedListLabel, org.openide.util.NbBundle.getMessage(IncludeExcludeVisualizerPanel.class, "IncludeExcludeVisualizerPanel.includedListLabel.text")); // NOI18N

        includedListPane.setViewportView(includedList);
        includedList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(IncludeExcludeVisualizerPanel.class, "ACSN_includedList")); // NOI18N
        includedList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IncludeExcludeVisualizerPanel.class, "ACSD_includedList")); // NOI18N

        excludedListLabel.setLabelFor(excludedList);
        org.openide.awt.Mnemonics.setLocalizedText(excludedListLabel, org.openide.util.NbBundle.getMessage(IncludeExcludeVisualizerPanel.class, "IncludeExcludeVisualizerPanel.excludedListLabel.text")); // NOI18N

        excludedListPane.setViewportView(excludedList);
        excludedList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(IncludeExcludeVisualizerPanel.class, "ACSN_excludedList")); // NOI18N
        excludedList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IncludeExcludeVisualizerPanel.class, "ACSD_excludedList")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(scanningLabel, org.openide.util.NbBundle.getMessage(IncludeExcludeVisualizerPanel.class, "IncludeExcludeVisualizerPanel.scanningLabel.text")); // NOI18N

        includesLabel.setLabelFor(includes);
        org.openide.awt.Mnemonics.setLocalizedText(includesLabel, org.openide.util.NbBundle.getMessage(IncludeExcludeVisualizerPanel.class, "IncludeExcludeVisualizerPanel.includesLabel.text")); // NOI18N

        excludesLabel.setLabelFor(excludes);
        org.openide.awt.Mnemonics.setLocalizedText(excludesLabel, org.openide.util.NbBundle.getMessage(IncludeExcludeVisualizerPanel.class, "IncludeExcludeVisualizerPanel.excludesLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(explanation, org.openide.util.NbBundle.getMessage(IncludeExcludeVisualizerPanel.class, "IncludeExcludeVisualizerPanel.explanation.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(includesLabel)
                            .add(excludesLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(excludes, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
                            .add(includes, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)))
                    .add(scanningLabel)
                    .add(includedListLabel)
                    .add(excludedListLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, includedListPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, explanation, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, excludedListPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(includesLabel)
                    .add(includes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(excludesLabel)
                    .add(excludes, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(scanningLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(includedListLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(includedListPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(excludedListLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(excludedListPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 143, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(explanation, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                .addContainerGap())
        );

        includesLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IncludeExcludeVisualizerPanel.class, "ACSD_includesLabel")); // NOI18N
        excludesLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IncludeExcludeVisualizerPanel.class, "ACSD_excludesLabel")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(IncludeExcludeVisualizerPanel.class, "ACSN_IncludeExcludeVisualizerPanel")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(IncludeExcludeVisualizerPanel.class, "ACSD_IncludeExcludeVisualizerPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList excludedList;
    private javax.swing.JLabel excludedListLabel;
    private javax.swing.JScrollPane excludedListPane;
    private javax.swing.JTextField excludes;
    private javax.swing.JLabel excludesLabel;
    private javax.swing.JLabel explanation;
    private javax.swing.JList includedList;
    private javax.swing.JLabel includedListLabel;
    private javax.swing.JScrollPane includedListPane;
    private javax.swing.JTextField includes;
    private javax.swing.JLabel includesLabel;
    private javax.swing.JLabel scanningLabel;
    // End of variables declaration//GEN-END:variables

}
