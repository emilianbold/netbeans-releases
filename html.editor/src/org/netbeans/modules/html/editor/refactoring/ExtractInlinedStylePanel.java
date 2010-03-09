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
package org.netbeans.modules.html.editor.refactoring;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.css.refactoring.api.CssRefactoring;
import org.netbeans.modules.html.editor.indexing.HtmlLinkEntry;
import org.netbeans.modules.html.editor.refactoring.api.ExtractInlinedStyleRefactoring.Mode;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.web.common.spi.ProjectWebRootQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * Extract Inlined Style Panel
 *
 * @author mfukala@netbeans.org
 */
public class ExtractInlinedStylePanel extends JPanel implements CustomRefactoringPanel, ItemListener {

    private List<FileObject> allStylesheets;
    private RefactoringContext context;
    private Mode selection;
    private FileObject newStyleSheet;

    /** Creates new form RenamePanelName */
    public ExtractInlinedStylePanel(RefactoringContext context) {
        this.context = context;
        this.allStylesheets = new ArrayList<FileObject>(CssRefactoring.findAllStyleSheets(context.getFile()));
        initComponents();
        initUI();
    }

    private void initUI() {
        buttonGroup1.getSelection();

        existingEmbeddedSectionRB.addItemListener(this);
        createNewEmbeddedSectionRB.addItemListener(this);
        usedExternalSheetRB.addItemListener(this);
        existingExternalSheetRB.addItemListener(this);
        newExternalSheetRB.addItemListener(this);

        //default button group selection
        if (!context.getExistingEmbeddedCssSections().isEmpty()) {
            setButtonsGroupSelection(Mode.refactorToExistingEmbeddedSection);
        } else {
            setButtonsGroupSelection(Mode.refactorToNewEmbeddedSection);
        }

    }

    private void setButtonsGroupSelection(Mode mode) {
        this.selection = mode;
        JRadioButton select;
        switch (selection) {
            case refactorToExistingEmbeddedSection:
                select = existingEmbeddedSectionRB;
                break;
            case refactorToNewEmbeddedSection:
                select = createNewEmbeddedSectionRB;
                break;
            case refactorToReferedExternalSheet:
                select = usedExternalSheetRB;
            case refactorToNewExternalSheet:
                select = newExternalSheetRB;
            case refactorToExistingExternalSheet:
                select = existingExternalSheetRB;
            default:
                select = null;
                assert false;
        }
        select.setSelected(true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Object s = e.getSource();
        if (e.getStateChange() == ItemEvent.SELECTED) {
            if (s == existingEmbeddedSectionRB) {
                selection = Mode.refactorToExistingEmbeddedSection;
            } else if (s == createNewEmbeddedSectionRB) {
                selection = Mode.refactorToNewEmbeddedSection;
            } else if (s == usedExternalSheetRB) {
                selection = Mode.refactorToReferedExternalSheet;
            } else if (s == existingExternalSheetRB) {
                selection = Mode.refactorToExistingExternalSheet;
            } else if (s == newExternalSheetRB) {
                selection = Mode.refactorToNewExternalSheet;
            }
        }
    }

    Mode getSelectedMode() {
        return selection;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        label = new javax.swing.JLabel();
        existingEmbeddedSectionRB = new javax.swing.JRadioButton();
        createNewEmbeddedSectionRB = new javax.swing.JRadioButton();
        usedExternalSheetRB = new javax.swing.JRadioButton();
        existingEmbeddedSectionsComboBox = new javax.swing.JComboBox();
        usedExternalSheetsComboBox = new javax.swing.JComboBox();
        newExternalSheetRB = new javax.swing.JRadioButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        existingExternalSheetRB = new javax.swing.JRadioButton();
        existingExternalStyleSheetsComboBox = new javax.swing.JComboBox();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 11, 11));
        setRequestFocusEnabled(false);

        label.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        org.openide.awt.Mnemonics.setLocalizedText(label, org.openide.util.NbBundle.getMessage(ExtractInlinedStylePanel.class, "LBL_ExtractInlinedStyleToLabel")); // NOI18N

        buttonGroup1.add(existingEmbeddedSectionRB);
        org.openide.awt.Mnemonics.setLocalizedText(existingEmbeddedSectionRB, org.openide.util.NbBundle.getMessage(ExtractInlinedStylePanel.class, "MSG_ExtractToEmbeddedSection")); // NOI18N
        existingEmbeddedSectionRB.setEnabled(!context.getExistingEmbeddedCssSections().isEmpty());

        buttonGroup1.add(createNewEmbeddedSectionRB);
        org.openide.awt.Mnemonics.setLocalizedText(createNewEmbeddedSectionRB, org.openide.util.NbBundle.getMessage(ExtractInlinedStylePanel.class, "MSG_createNewEmbeddedSection")); // NOI18N

        buttonGroup1.add(usedExternalSheetRB);
        org.openide.awt.Mnemonics.setLocalizedText(usedExternalSheetRB, org.openide.util.NbBundle.getMessage(ExtractInlinedStylePanel.class, "MSG_ExistingExternalStyleSheet")); // NOI18N
        usedExternalSheetRB.setEnabled(!context.getLinkedExternalStylesheets().isEmpty());

        existingEmbeddedSectionsComboBox.setModel(createExistingEmbeddedCssSectionsModel());
        existingEmbeddedSectionsComboBox.setEnabled(!context.getExistingEmbeddedCssSections().isEmpty());
        existingEmbeddedSectionsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                existingEmbeddedSectionsComboBoxActionPerformed(evt);
            }
        });

        usedExternalSheetsComboBox.setModel(createExternalStylesheetsModel());
        usedExternalSheetsComboBox.setEnabled(!context.getLinkedExternalStylesheets().isEmpty());
        usedExternalSheetsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usedExternalSheetsComboBoxActionPerformed(evt);
            }
        });

        buttonGroup1.add(newExternalSheetRB);
        org.openide.awt.Mnemonics.setLocalizedText(newExternalSheetRB, org.openide.util.NbBundle.getMessage(ExtractInlinedStylePanel.class, "MSG_NewExternalStyleSheet")); // NOI18N
        newExternalSheetRB.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, org.openide.util.NbBundle.getMessage(ExtractInlinedStylePanel.class, "MSG_PreferClassesUsages")); // NOI18N
        jCheckBox1.setEnabled(false);

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox2, org.openide.util.NbBundle.getMessage(ExtractInlinedStylePanel.class, "MSG_AllowUsingExistingClasses")); // NOI18N
        jCheckBox2.setEnabled(false);

        buttonGroup1.add(existingExternalSheetRB);
        org.openide.awt.Mnemonics.setLocalizedText(existingExternalSheetRB, org.openide.util.NbBundle.getMessage(ExtractInlinedStylePanel.class, "MSG_ChooseExternalStyleSheet")); // NOI18N

        existingExternalStyleSheetsComboBox.setModel(createExistingExternalSheetsModel());
        existingExternalStyleSheetsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                existingExternalStyleSheetsComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(label)
            .addGroup(layout.createSequentialGroup()
                .addComponent(existingEmbeddedSectionRB)
                .addGap(30, 30, 30)
                .addComponent(existingEmbeddedSectionsComboBox, 0, 173, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(createNewEmbeddedSectionRB)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(usedExternalSheetRB)
                        .addGap(42, 42, 42)
                        .addComponent(usedExternalSheetsComboBox, 0, 173, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox1)
                    .addComponent(jCheckBox2))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(existingExternalSheetRB)
                .addGap(22, 22, 22)
                .addComponent(existingExternalStyleSheetsComboBox, 0, 173, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(newExternalSheetRB)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(label)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(existingEmbeddedSectionRB)
                    .addComponent(existingEmbeddedSectionsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(createNewEmbeddedSectionRB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usedExternalSheetRB)
                    .addComponent(usedExternalSheetsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(existingExternalSheetRB)
                    .addComponent(existingExternalStyleSheetsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(newExternalSheetRB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addComponent(jCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBox2)
                .addGap(30, 30, 30))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void existingEmbeddedSectionsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_existingEmbeddedSectionsComboBoxActionPerformed
        existingEmbeddedSectionRB.setSelected(true);
    }//GEN-LAST:event_existingEmbeddedSectionsComboBoxActionPerformed

    private void usedExternalSheetsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usedExternalSheetsComboBoxActionPerformed
        usedExternalSheetRB.setSelected(true);
    }//GEN-LAST:event_usedExternalSheetsComboBoxActionPerformed

    private void existingExternalStyleSheetsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_existingExternalStyleSheetsComboBoxActionPerformed
        existingExternalSheetRB.setSelected(true);
    }//GEN-LAST:event_existingExternalStyleSheetsComboBoxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JRadioButton createNewEmbeddedSectionRB;
    private javax.swing.JRadioButton existingEmbeddedSectionRB;
    private javax.swing.JComboBox existingEmbeddedSectionsComboBox;
    private javax.swing.JRadioButton existingExternalSheetRB;
    private javax.swing.JComboBox existingExternalStyleSheetsComboBox;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JLabel label;
    private javax.swing.JRadioButton newExternalSheetRB;
    private javax.swing.JRadioButton usedExternalSheetRB;
    private javax.swing.JComboBox usedExternalSheetsComboBox;
    // End of variables declaration//GEN-END:variables

    private ComboBoxModel createExistingEmbeddedCssSectionsModel() {
        List<OffsetRange> ranges = context.getExistingEmbeddedCssSections();
        String[] values = new String[ranges.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = getRenderStringFromOffsetRange(ranges.get(i));
        }
        return new DefaultComboBoxModel(values) {
        };
    }

    private ComboBoxModel createExternalStylesheetsModel() {
        List<HtmlLinkEntry> links = context.getLinkedExternalStylesheets();
        String[] values = new String[links.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = links.get(i).getName();
        }
        return new DefaultComboBoxModel(values) {
        };
    }

    private ComboBoxModel createExistingExternalSheetsModel() {
        FileObject webRoot = ProjectWebRootQuery.getWebRoot(context.getFile());
        String[] values = new String[allStylesheets.size()];
        int index = 0;
        for (FileObject file : allStylesheets) {
            String relativePath = FileUtil.getRelativePath(webRoot, file);
            values[index++] = relativePath;
        }
        return new DefaultComboBoxModel(values);

    }

    OffsetRange getSelectedExistingEmbeddedSection() {
        int index = existingEmbeddedSectionsComboBox.getSelectedIndex();
        return context.getExistingEmbeddedCssSections().get(index);
    }

    FileObject getSelectedUsedExternalStylesheet() {
        int index = usedExternalSheetsComboBox.getSelectedIndex();
        return context.getLinkedExternalStylesheets().get(index).getFileReference().target();
    }

    FileObject getSelectedExternalStyleSheet() {
        int index = existingExternalStyleSheetsComboBox.getSelectedIndex();
        return allStylesheets.get(index);
    }

    FileObject getNewStyleSheet() {
        return newStyleSheet;
    }

    String getRenderStringFromOffsetRange(final OffsetRange range) {
        //compute lines for each offset
        final AtomicReference<OffsetRange> ret = new AtomicReference<OffsetRange>();
        context.getDocument().render(new Runnable() {

            @Override
            public void run() {
                try {
                    int firstLine = Utilities.getLineOffset((BaseDocument) context.getDocument(), range.getStart());
                    int lastLine = Utilities.getLineOffset((BaseDocument) context.getDocument(), range.getEnd());
                    ret.set(new OffsetRange(firstLine, lastLine));
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

        OffsetRange line = ret.get();

        return new StringBuilder().append("Section from line ").
                append(line.getStart() + 1). //lines in editor are counted from 1
                append(" to ").
                append(line.getEnd() + 1). //lines in editor are counted from 1
                toString();
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public void initialize() {
        //put initialization code here
        //when is this called???
    }
}
