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
package org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints.customizer;

import java.awt.Dimension;

import org.netbeans.api.debugger.Breakpoint.HIT_COUNT_FILTERING_STYLE;
import org.netbeans.modules.web.client.javascript.debugger.ui.NbJSDialogUtil;
import org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints.NbJSBreakpoint;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Panel for breakpoint conditions
 * 
 * @author Joelle Lam
 */
public class NbJSBreakpointConditionsPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    private String orCondition;
    private int orFilter;
    private HIT_COUNT_FILTERING_STYLE orFilterStyle;
    private NbJSBreakpoint breakpoint;

    public NbJSBreakpointConditionsPanel() {
        setupPanel();

        orCondition = "";
        orFilterStyle = HIT_COUNT_FILTERING_STYLE.GREATER;
        orFilter = 0;
        initConditionPanel(orCondition, orFilterStyle, orFilter);
    }

    /** Creates new form ConditionsPanel */
    public NbJSBreakpointConditionsPanel(NbJSBreakpoint breakpoint) {
        setupPanel();
        this.breakpoint = breakpoint;
        FileObject fo = null;
        if (breakpoint != null) {
            orCondition = breakpoint.getCondition();
            orFilterStyle = breakpoint.getHitCountFilteringStyle();
            orFilter = breakpoint.getHitCountFilter();
            fo = breakpoint.getFileObject();
        } else {
            orCondition = "";
            orFilterStyle = HIT_COUNT_FILTERING_STYLE.GREATER;
            orFilter = 0;
        }

        initConditionPanel(orCondition, orFilterStyle, orFilter);
        if (fo != null) {
            setupConditionPaneContext(fo.getPath(), breakpoint.getLineNumber());
        }
    }

    private void setupPanel() {
        initComponents();

        tfConditionFieldForUI = new javax.swing.JTextField();
        tfConditionFieldForUI.setEnabled(false);
        tfConditionFieldForUI.setToolTipText(tfCondition.getToolTipText());
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(tfConditionFieldForUI, gridBagConstraints);

        conditionCheckBoxActionPerformed(null);
        cbWhenHitCountActionPerformed(null);
        int preferredHeight = tfConditionFieldForUI.getPreferredSize().height;
        if (spCondition.getPreferredSize().height > preferredHeight) {
            preferredHeight = spCondition.getPreferredSize().height;
            tfConditionFieldForUI.setPreferredSize(new java.awt.Dimension(
                    tfConditionFieldForUI.getPreferredSize().width,
                    preferredHeight));
        }
        tfHitCountFilter.setPreferredSize(new Dimension(8 * tfHitCountFilter.getFontMetrics(tfHitCountFilter.getFont()).charWidth('8'),
                tfHitCountFilter.getPreferredSize().height));
        cbHitStyle.setModel(new javax.swing.DefaultComboBoxModel(new String[]{
                    NbBundle.getMessage(NbJSBreakpointConditionsPanel.class,
                    "ConditionsPanel.cbWhenHitCount.equals"), // NOI18N
                    NbBundle.getMessage(NbJSBreakpointConditionsPanel.class,
                    "ConditionsPanel.cbWhenHitCount.greaterThanOrEquals"), // NOI18N
                    NbBundle.getMessage(NbJSBreakpointConditionsPanel.class,
                    "ConditionsPanel.cbWhenHitCount.multiple") // NOI18N
                // NOI18N
                }));
    }

    private void initConditionPanel(String condition,
            HIT_COUNT_FILTERING_STYLE filterStyle, int filter) {
        setCondition(condition);
        setHitCount(filter);
        setHitCountFilteringStyle(filterStyle);
    }

    // Data Show:
    public void showCondition(boolean show) {
        conditionCheckBox.setVisible(show);
        if (show) {
            conditionCheckBoxActionPerformed(null);
        } else {
            spCondition.setVisible(show);
            tfCondition.setVisible(show);
            tfConditionFieldForUI.setVisible(show);
        }
    }

    public void setCondition(String condition) {
        String strCondition = (condition != null) ? condition : "";
        tfCondition.setText(strCondition);
        conditionCheckBox.setSelected(strCondition.length() > 0);
        conditionCheckBoxActionPerformed(null);
    }

    public void setHitCountFilteringStyle(HIT_COUNT_FILTERING_STYLE style) {
        cbHitStyle.setSelectedIndex((style != null) ? style.ordinal() : 0);
    }

    public void setHitCount(int hitCount) {
        if (hitCount != 0) {
            cbWhenHitCount.setSelected(true);
            tfHitCountFilter.setText(Integer.toString(hitCount));
        } else {
            cbWhenHitCount.setSelected(false);
            tfHitCountFilter.setText("1");
        }
        cbWhenHitCountActionPerformed(null);
    }

    public void setupConditionPaneContext(String url, int line) {
        NbJSDialogUtil.setupContext(tfCondition, url, line);
    }

    public String getCondition() {
        if (conditionCheckBox.isSelected()) {
            return tfCondition.getText().trim();
        } else {
            return "";
        }
    }

    public HIT_COUNT_FILTERING_STYLE getHitCountFilteringStyle() {
        return HIT_COUNT_FILTERING_STYLE.values()[cbHitStyle.getSelectedIndex()];
    }

    public int getHitCount() {
        if (!cbWhenHitCount.isSelected()) {
            return 0;
        }
        String hcfStr = tfHitCountFilter.getText().trim();
        try {
            int hitCount = Integer.parseInt(hcfStr);
            return hitCount;
        } catch (NumberFormatException nfex) {
            return 0;
        }
    }

    public String valiadateMsg() {
        String hcfStr = tfHitCountFilter.getText().trim();
        if (cbWhenHitCount.isSelected()) {
            if (hcfStr.length() > 0) {
                int hitCountFilter;
                try {
                    hitCountFilter = Integer.parseInt(hcfStr);
                } catch (NumberFormatException e) {
                    return NbBundle.getMessage(
                            NbJSBreakpointConditionsPanel.class,
                            "MSG_Bad_Hit_Count_Filter_Spec", hcfStr);
                }
                if (hitCountFilter <= 0) {
                    return NbBundle.getMessage(
                            NbJSBreakpointConditionsPanel.class,
                            "MSG_NonPositive_Hit_Count_Filter_Spec");
                }
            } else {
                return NbBundle.getMessage(NbJSBreakpointConditionsPanel.class,
                        "MSG_No_Hit_Count_Filter_Spec");
            }
        }
        if (conditionCheckBox.isSelected() && tfCondition.getText().trim().length() == 0) {
            return NbBundle.getMessage(NbJSBreakpointConditionsPanel.class,
                    "MSG_No_Condition_Spec");
        }
        return null;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        classIncludeFilterLabel = new javax.swing.JLabel();
        conditionCheckBox = new javax.swing.JCheckBox();
        panelHitCountFilter = new javax.swing.JPanel();
        tfHitCountFilter = new javax.swing.JTextField();
        cbHitStyle = new javax.swing.JComboBox();
        cbWhenHitCount = new javax.swing.JCheckBox();
        spCondition = new javax.swing.JScrollPane();
        tfCondition = new javax.swing.JEditorPane();

        setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(NbJSBreakpointConditionsPanel.class, "L_Conditions_Breakpoint_BorderTitle"))); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(classIncludeFilterLabel, null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 18, 3, 3);
        add(classIncludeFilterLabel, gridBagConstraints);
        classIncludeFilterLabel.getAccessibleContext().setAccessibleDescription("null");

        org.openide.awt.Mnemonics.setLocalizedText(conditionCheckBox, org.openide.util.NbBundle.getMessage(NbJSBreakpointConditionsPanel.class, "ACSD_L_Line_Breakpoint_Condition")); // NOI18N
        conditionCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(NbJSBreakpointConditionsPanel.class, "TTT_CB_ConditionsPanel_Condition")); // NOI18N
        conditionCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                conditionCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(conditionCheckBox, gridBagConstraints);
        conditionCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NbJSBreakpointConditionsPanel.class, "ASCD_Condition_CKBX")); // NOI18N
        conditionCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbJSBreakpointConditionsPanel.class, "ASCD_Condition_CKBX_Desc")); // NOI18N

        panelHitCountFilter.setLayout(new java.awt.GridBagLayout());

        tfHitCountFilter.setToolTipText(org.openide.util.NbBundle.getMessage(NbJSBreakpointConditionsPanel.class, "TTT_TF_Hit_Count")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        panelHitCountFilter.add(tfHitCountFilter, gridBagConstraints);
        tfHitCountFilter.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NbJSBreakpointConditionsPanel.class, "ACSN_HitCountTF")); // NOI18N
        tfHitCountFilter.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbJSBreakpointConditionsPanel.class, "ACSD_TF_HitCount_Desc")); // NOI18N

        cbHitStyle.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "equals to", "is greater than or equals to", "is multiple of" }));
        cbHitStyle.setSelectedIndex(1);
        cbHitStyle.setEnabled(false);
        cbHitStyle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbHitStyleActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        panelHitCountFilter.add(cbHitStyle, gridBagConstraints);
        cbHitStyle.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NbJSBreakpointConditionsPanel.class, "ACSN_CB_HitCount_Qualifier")); // NOI18N
        cbHitStyle.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbJSBreakpointConditionsPanel.class, "ACSD_CB_HitCount_Desc")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbWhenHitCount, org.openide.util.NbBundle.getMessage(NbJSBreakpointConditionsPanel.class, "ConditionsPanel.cbWhenHitCount.text")); // NOI18N
        cbWhenHitCount.setToolTipText(org.openide.util.NbBundle.getMessage(NbJSBreakpointConditionsPanel.class, "TTT_CB_CondtionsPanel_HitFilterWhen")); // NOI18N
        cbWhenHitCount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbWhenHitCountActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        panelHitCountFilter.add(cbWhenHitCount, gridBagConstraints);
        cbWhenHitCount.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NbJSBreakpointConditionsPanel.class, "ASCD_Break_hitCount")); // NOI18N
        cbWhenHitCount.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbJSBreakpointConditionsPanel.class, "ACSD_Break_HitCount_Desc")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(panelHitCountFilter, gridBagConstraints);

        spCondition = NbJSDialogUtil.createScrollableLineEditor(tfCondition);
        spCondition.setToolTipText(org.openide.util.NbBundle.getMessage(NbJSBreakpointConditionsPanel.class, "TTT_TF_Line_Breakpoint_Condition")); // NOI18N

        tfCondition.setContentType(org.openide.util.NbBundle.getMessage(NbJSBreakpointConditionsPanel.class, "NbJSBreakpointConditionsPanel.tfCondition.contentType")); // NOI18N
        tfCondition.setToolTipText(org.openide.util.NbBundle.getMessage(NbJSBreakpointConditionsPanel.class, "TTT_TF_Line_Breakpoint_Condition")); // NOI18N
        spCondition.setViewportView(tfCondition);
        tfCondition.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NbJSBreakpointConditionsPanel.class, "ACSN_ConditionTF")); // NOI18N
        tfCondition.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbJSBreakpointConditionsPanel.class, "ACSN_ConditionTF_Desc")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(spCondition, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbJSBreakpointConditionsPanel.class, "ACSD_Conditions")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void cbWhenHitCountActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cbWhenHitCountActionPerformed
        boolean isSelected = cbWhenHitCount.isSelected();
        cbHitStyle.setEnabled(isSelected);
        tfHitCountFilter.setEnabled(isSelected);
    }// GEN-LAST:event_cbWhenHitCountActionPerformed

    private void conditionCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_conditionCheckBoxActionPerformed
        boolean isSelected = conditionCheckBox.isSelected();
        // System.err.println("Initial TF Background =
        // "+tfConditionFieldForUI.getBackground());
        spCondition.setEnabled(isSelected);
        tfCondition.setEnabled(isSelected);

        if (isSelected) {
            tfCondition.setVisible(true);
            spCondition.setVisible(true);
            tfConditionFieldForUI.setVisible(false);
            if (spCondition.getPreferredSize().height > tfCondition.getPreferredSize().height) {
                final int shift = -(spCondition.getPreferredSize().height - tfCondition.getPreferredSize().height) / 2;
                javax.swing.SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        spCondition.getViewport().setViewPosition(
                                new java.awt.Point(0, shift));
                    }
                });
            }
            tfCondition.requestFocusInWindow();
        } else {
            tfCondition.setVisible(false);
            spCondition.setVisible(false);
            tfConditionFieldForUI.setText(tfCondition.getText());
            tfConditionFieldForUI.setVisible(true);
        }
        revalidate();
        repaint();

    // tfConditionFieldForUI.setEnabled(isSelected);
    // System.err.println("TF Background =
    // "+tfConditionFieldForUI.getBackground());
    // tfCondition.setBackground(tfConditionFieldForUI.getBackground());
    // spCondition.setBorder(tfConditionFieldForUI.getBorder());
    }// GEN-LAST:event_conditionCheckBoxActionPerformed

    private void cbHitStyleActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cbHitStyleActionPerformed
    }// GEN-LAST:event_cbHitStyleActionPerformed
    private javax.swing.JTextField tfConditionFieldForUI;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbHitStyle;
    private javax.swing.JCheckBox cbWhenHitCount;
    private javax.swing.JLabel classIncludeFilterLabel;
    private javax.swing.JCheckBox conditionCheckBox;
    private javax.swing.JPanel panelHitCountFilter;
    private javax.swing.JScrollPane spCondition;
    private javax.swing.JEditorPane tfCondition;
    private javax.swing.JTextField tfHitCountFilter;
    // End of variables declaration//GEN-END:variables

    public void ok() {

        final String newCondition = getCondition();
        final int newFilter = getHitCount();
        final HIT_COUNT_FILTERING_STYLE newFilterStyle = getHitCountFilteringStyle();
        boolean updated = false;
        if (!newCondition.equals(orCondition)) {
            breakpoint.setCondition(newCondition);
            updated = true;
        }

        if (newFilter != orFilter || newFilterStyle != orFilterStyle) {
            breakpoint.setHitCountFilter(newFilter, newFilterStyle);
            updated = true;
        }
        if (updated) {
            breakpoint.notifyUpdated(this);
        }
    }

    public void setBreakpoint(NbJSBreakpoint b) {
        breakpoint = b;
    }
}
