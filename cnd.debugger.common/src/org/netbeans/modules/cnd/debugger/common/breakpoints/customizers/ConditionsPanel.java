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

package org.netbeans.modules.cnd.debugger.common.breakpoints.customizers;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.DialogBinding;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.debugger.common.breakpoints.CndBreakpoint;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Panel for breakpoint conditions
 * 
 * @author  Martin Entlicher
 */
public class ConditionsPanel extends JPanel {
    
    private final CndBreakpoint  breakpoint;
    private final JTextComponent conditionPane;
    private final JScrollPane conditionSP;
    
    /** Creates new form ConditionsPanel */
    public ConditionsPanel(final CndBreakpoint breakpoint) {
        this.breakpoint = breakpoint;
        initComponents();

        FileObject file = EditorContextDispatcher.getDefault().getMostRecentFile();
        int line = EditorContextDispatcher.getDefault().getMostRecentLineNumber();

        //Add JEditorPane and context
        JComponent [] editorComponents = Utilities.createSingleLineEditor(file.getMIMEType());
        conditionPane = (JTextComponent) editorComponents[1];

        DialogBinding.bindComponentToFile(file, line, 0, 0, conditionPane);

        conditionSP = (JScrollPane)editorComponents[0];
        conditionSP.setEnabled(false);
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(conditionSP, gridBagConstraints);

        setCondition(breakpoint.getCondition());
        setHitCount(breakpoint.getSkipCount());

        conditionPane.setText(breakpoint.getCondition());
    }
    
    public void ok() {
        if (cbCondition.isSelected()) {
            breakpoint.setCondition(conditionPane.getText());
        } else {
            breakpoint.setCondition("");
        }
        if (cbSkipCount.isSelected()) {
            try {
                breakpoint.setSkipCount(Integer.valueOf(tfSkipCount.getText()));
            } catch (Exception ex) {
              breakpoint.setSkipCount(0); // Just ignore an invalid value...  
            }
        } else {
            breakpoint.setSkipCount(0);
        }
    }
    
    public void showCondition(boolean show) {
        cbCondition.setVisible(show);
        if (show) {
            cbConditionActionPerformed(null);
        } else {
            conditionSP.setVisible(show);
        }
    }
    
    public final void setCondition(String condition) {
        conditionPane.setText(condition);
        cbCondition.setSelected(condition.length() > 0);
        cbConditionActionPerformed(null);
    }
    
    public final void setHitCount(int hitCount) {
        if (hitCount != 0) {
            cbSkipCount.setSelected(true);
            tfSkipCount.setText(Integer.toString(hitCount));
        } else {
            cbSkipCount.setSelected(false);
            tfSkipCount.setText("");
        }
        cbSkipCountActionPerformed(null);
    }
    
    public String getCondition() {
        if (cbCondition.isSelected()) {
            return conditionPane.getText().trim();
        } else {
            return "";
        }
    }
    
    public int getHitCount() {
        if (!cbSkipCount.isSelected()) {
            return 0;
        }
        String hcfStr = tfSkipCount.getText().trim();
        try {
            return Integer.parseInt(hcfStr);
        } catch (NumberFormatException nfex) {
            return 0;
        }
    }
    
    public String valiadateMsg () {
        String hcfStr = tfSkipCount.getText().trim();
        if (cbSkipCount.isSelected()) {
            if (hcfStr.length() > 0) {
                int hitCountFilter;
                try {
                    hitCountFilter = Integer.parseInt(hcfStr);
                } catch (NumberFormatException e) {
                    return NbBundle.getMessage(ConditionsPanel.class, "MSG_Bad_Hit_Count_Filter_Spec", hcfStr);
                }
                if (hitCountFilter <= 0) {
                    return NbBundle.getMessage(ConditionsPanel.class, "MSG_NonPositive_Hit_Count_Filter_Spec");
                }
            } else {
                return NbBundle.getMessage(ConditionsPanel.class, "MSG_No_Hit_Count_Filter_Spec");
            }
        }
        if (cbCondition.isSelected() && conditionPane.getText().trim().length() == 0) {
            return NbBundle.getMessage(ConditionsPanel.class, "MSG_No_Condition_Spec");
        }
        return null;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        cbCondition = new javax.swing.JCheckBox();
        panelHitCountFilter = new javax.swing.JPanel();
        cbSkipCount = new javax.swing.JCheckBox();
        tfSkipCount = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "L_Conditions_Breakpoint_BorderTitle"))); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(cbCondition, org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "L_cbCondition")); // NOI18N
        cbCondition.setToolTipText(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "TT_cbCondition")); // NOI18N
        cbCondition.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbCondition.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbConditionActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(cbCondition, gridBagConstraints);

        panelHitCountFilter.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(cbSkipCount, org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "L_SkipCount")); // NOI18N
        cbSkipCount.setToolTipText(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "TT_SkiptCount")); // NOI18N
        cbSkipCount.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbSkipCount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbSkipCountActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        panelHitCountFilter.add(cbSkipCount, gridBagConstraints);

        tfSkipCount.setToolTipText(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "TT_SkipCount")); // NOI18N
        tfSkipCount.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        panelHitCountFilter.add(tfSkipCount, gridBagConstraints);
        tfSkipCount.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ACSN_HitCountTF")); // NOI18N
        tfSkipCount.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ACSD_HitCountTF")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(panelHitCountFilter, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "L_Conditions_Breakpoint_BorderTitle")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ACSD_Conditions")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void cbSkipCountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbSkipCountActionPerformed
    boolean isSelected = cbSkipCount.isSelected();
    tfSkipCount.setEnabled(isSelected);
    if (isSelected) {
        tfSkipCount.requestFocusInWindow();
    }
}//GEN-LAST:event_cbSkipCountActionPerformed

private void cbConditionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbConditionActionPerformed
    boolean isSelected = cbCondition.isSelected();
    
    if (isSelected) {
        conditionPane.setEnabled(true);
        conditionPane.requestFocusInWindow();
    } else {
        conditionPane.setEnabled(false);
    }
    revalidate();
    repaint();
}//GEN-LAST:event_cbConditionActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbCondition;
    private javax.swing.JCheckBox cbSkipCount;
    private javax.swing.JPanel panelHitCountFilter;
    private javax.swing.JTextField tfSkipCount;
    // End of variables declaration//GEN-END:variables
    
}
