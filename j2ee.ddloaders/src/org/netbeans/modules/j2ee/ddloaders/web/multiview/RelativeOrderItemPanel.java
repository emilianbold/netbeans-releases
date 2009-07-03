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

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import javax.swing.JPanel;
import org.openide.util.NbBundle;

/**
 * @author Petr Slechta
 */
class RelativeOrderItemPanel extends JPanel {

    public static final String BEFORE = "before "; // NOI18N
    public static final String AFTER = "after "; // NOI18N
    public static final String BEFORE_OTHERS = "before <others>"; // NOI18N
    public static final String AFTER_OTHERS = "after <others>"; // NOI18N

    public RelativeOrderItemPanel(String item) {
        initComponents();
        cbOrderingType.addItem(NbBundle.getMessage(RelativeOrderItemPanel.class, "CB_Ordering_Before"));
        cbOrderingType.addItem(NbBundle.getMessage(RelativeOrderItemPanel.class, "CB_Ordering_After"));
        cbOrderingType.addItem(NbBundle.getMessage(RelativeOrderItemPanel.class, "CB_Ordering_Before_others"));
        cbOrderingType.addItem(NbBundle.getMessage(RelativeOrderItemPanel.class, "CB_Ordering_After_others"));
        if (item != null && item.length() > 0) {
            if (item.equals(BEFORE_OTHERS))
                cbOrderingType.setSelectedIndex(2);
            else if (item.equals(AFTER_OTHERS))
                cbOrderingType.setSelectedIndex(3);
            else if (item.startsWith(BEFORE)) {
                cbOrderingType.setSelectedIndex(0);
                tfNameRef.setText(item.substring(BEFORE.length()));
            }
            else if (item.startsWith(AFTER)) {
                cbOrderingType.setSelectedIndex(1);
                tfNameRef.setText(item.substring(AFTER.length()));
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        cbOrderingType = new javax.swing.JComboBox();
        tfNameRef = new javax.swing.JTextField();

        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(jList1);

        setMinimumSize(new java.awt.Dimension(250, 150));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(RelativeOrderingPanel.class, "LBL_RelativeOrder")); // NOI18N

        cbOrderingType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbOrderingTypeActionPerformed(evt);
            }
        });

        tfNameRef.setColumns(20);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbOrderingType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tfNameRef, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(104, 104, 104))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(cbOrderingType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(tfNameRef, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(115, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cbOrderingTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbOrderingTypeActionPerformed
        int x = cbOrderingType.getSelectedIndex();
        tfNameRef.setEnabled(x == 0 || x == 1);
    }//GEN-LAST:event_cbOrderingTypeActionPerformed

    public String getResult() {
        String name = tfNameRef.getText();
        switch (cbOrderingType.getSelectedIndex()) {
            case 0:
                return name.length() > 0 ? BEFORE+name : null;
            case 1:
                return name.length() > 0 ? AFTER+name : null;
            case 2:
                return BEFORE_OTHERS;
            case 3:
                return AFTER_OTHERS;
            default:
                return null;
        }
    }

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbOrderingType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField tfNameRef;
    // End of variables declaration//GEN-END:variables
 
}
