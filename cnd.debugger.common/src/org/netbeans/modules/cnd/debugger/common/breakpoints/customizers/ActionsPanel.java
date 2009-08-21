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

package org.netbeans.modules.cnd.debugger.common.breakpoints.customizers;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JPanel;
import org.netbeans.modules.cnd.debugger.common.breakpoints.CndBreakpoint;
import org.openide.util.NbBundle;

/**
 * Panel for customizing breakpoints. 
 * This panel is a part of "New Breakpoint" dialog.
 *
 * @author Nik Molchanov (copied and modified from JDPA debugger).
 */
public class ActionsPanel extends JPanel implements ItemListener {
    
    private CndBreakpoint  breakpoint;
    
    /** Creates new form LineBreakpointPanel */
    public ActionsPanel(CndBreakpoint b) {
        breakpoint = b;
        initComponents();
        
        cbSuspend.addItem(NbBundle.getMessage(ActionsPanel.class, "LBL_CB_Actions_Panel_Suspend_None")); // NOI18N
        cbSuspend.addItem(NbBundle.getMessage(ActionsPanel.class, "LBL_CB_Actions_Panel_Suspend_Current")); // NOI18N
        cbSuspend.addItem(NbBundle.getMessage(ActionsPanel.class, "LBL_CB_Actions_Panel_Suspend_All")); // NOI18N
        tfThreadID.setText(b.getThreadID());
        
        switch (b.getSuspend()) {
        case CndBreakpoint.SUSPEND_NONE:
            cbSuspend.setSelectedIndex(0);
            tfThreadID.setEnabled(false);
            lThreadID.setEnabled(false);
            break;
        case CndBreakpoint.SUSPEND_THREAD:
            cbSuspend.setSelectedIndex(1);
            tfThreadID.setEnabled(true);
            lThreadID.setEnabled(true);
            break;
        case CndBreakpoint.SUSPEND_ALL:
        default:
            cbSuspend.setSelectedIndex(2);
            tfThreadID.setEnabled(false);
            lThreadID.setEnabled(false);
            break;
        }
        if (b.getPrintText() != null) {
            tfPrintText.setText(b.getPrintText());
        }
    }
    
    /**
     * Called when "Ok" button is pressed.
     */
    public void ok() {
        String printText = tfPrintText.getText();
        if (printText.trim().length () > 0) {
            breakpoint.setPrintText(printText.trim());
        } else {
            breakpoint.setPrintText(null);
        }
        
        switch (cbSuspend.getSelectedIndex()) {
        case 0:
            breakpoint.setSuspend(CndBreakpoint.SUSPEND_NONE);
            break;
        case 1:
            breakpoint.setSuspend(CndBreakpoint.SUSPEND_THREAD, tfThreadID.getText());
            break;
        case 2:
            breakpoint.setSuspend(CndBreakpoint.SUSPEND_ALL);
            break;
        }
    }
    
    public void itemStateChanged(ItemEvent ev) {
        if (ev.getStateChange() == ItemEvent.SELECTED && ev.getSource() == cbSuspend) {
            int idx = cbSuspend.getSelectedIndex();
            tfThreadID.setEnabled(idx == CndBreakpoint.SUSPEND_THREAD);
            lThreadID.setEnabled(idx == CndBreakpoint.SUSPEND_THREAD);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lSuspend = new javax.swing.JLabel();
        cbSuspend = new javax.swing.JComboBox();
        cbSuspend.addItemListener(this);
        lThreadID = new javax.swing.JLabel();
        tfThreadID = new javax.swing.JTextField();
        tfPrintText = new javax.swing.JTextField();
        lPrintText = new javax.swing.JLabel();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common/breakpoints/customizers/Bundle"); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("L_Actions_Panel_BorderTitle"))); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        lSuspend.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common/breakpoints/customizers/Bundle").getString("MN_L_Actions_Panel_Suspend").charAt(0));
        lSuspend.setLabelFor(cbSuspend);
        lSuspend.setText(bundle.getString("L_Actions_Panel_Suspend")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(lSuspend, gridBagConstraints);
        lSuspend.getAccessibleContext().setAccessibleName(bundle.getString("ACSD_L_Actions_Panel_Suspend")); // NOI18N
        lSuspend.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_L_Actions_Panel_Suspend")); // NOI18N

        cbSuspend.setToolTipText(bundle.getString("TTT_CB_Actions_Panel_Suspend")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(cbSuspend, gridBagConstraints);
        cbSuspend.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_CB_Actions_Panel_Suspend")); // NOI18N
        cbSuspend.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CB_Actions_Panel_Suspend")); // NOI18N

        lThreadID.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common/breakpoints/customizers/Bundle").getString("MN_ThreadID").charAt(0));
        lThreadID.setLabelFor(tfThreadID);
        lThreadID.setText(org.openide.util.NbBundle.getMessage(ActionsPanel.class, "L_Actions_Panel_ThreadID")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 6, 3, 3);
        add(lThreadID, gridBagConstraints);

        tfThreadID.setColumns(4);
        tfThreadID.setToolTipText(org.openide.util.NbBundle.getMessage(ActionsPanel.class, "TT_ThreadID")); // NOI18N
        tfThreadID.setMinimumSize(new java.awt.Dimension(8, 21));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(tfThreadID, gridBagConstraints);

        tfPrintText.setToolTipText(bundle.getString("TTT_TF_Actions_Panel_Print_Text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(tfPrintText, gridBagConstraints);
        tfPrintText.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_TF_Actions_Panel_Print_Text")); // NOI18N
        tfPrintText.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_TF_Actions_Panel_Print_Text")); // NOI18N

        lPrintText.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common/breakpoints/customizers/Bundle").getString("MN_L_Actions_Panel_Print_Text").charAt(0));
        lPrintText.setLabelFor(tfPrintText);
        lPrintText.setText(bundle.getString("L_Actions_Panel_Print_Text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(lPrintText, gridBagConstraints);
        lPrintText.getAccessibleContext().setAccessibleName(bundle.getString("ACSD_L_Actions_Panel_Print_Text")); // NOI18N
        lPrintText.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_L_Actions_Panel_Print_Text")); // NOI18N

        getAccessibleContext().setAccessibleName(bundle.getString("ACSN_Actions_Panel")); // NOI18N
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Actions_Panel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbSuspend;
    private javax.swing.JLabel lPrintText;
    private javax.swing.JLabel lSuspend;
    private javax.swing.JLabel lThreadID;
    private javax.swing.JTextField tfPrintText;
    private javax.swing.JTextField tfThreadID;
    // End of variables declaration//GEN-END:variables
    
}
