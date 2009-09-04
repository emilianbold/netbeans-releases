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

package org.netbeans.modules.hudson.ui.notification;

import java.awt.Cursor;
import javax.swing.JPanel;

class ProblemPanel extends JPanel {

    private final ProblemNotification notification;

    public ProblemPanel(ProblemNotification notification) {
        this.notification = notification;
        // UI roughly copied from org.netbeans.core.ui.notifications.NotificationDisplayerImpl
        // XXX could add links to show changes, etc.
        initComponents();
        showFailure.setText("<html><u>" + notification.showFailureText());
        // XXX #171445: not available from form editor
        showFailure.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ignore.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        showFailure = new javax.swing.JButton();
        ignore = new javax.swing.JButton();

        setOpaque(false);

        showFailure.setForeground(java.awt.Color.blue);
        showFailure.setText("<html><u>Show [something]"); // NOI18N
        showFailure.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        showFailure.setBorderPainted(false);
        showFailure.setContentAreaFilled(false);
        showFailure.setFocusPainted(false);
        showFailure.setFocusable(false);
        showFailure.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        showFailure.setOpaque(false);
        showFailure.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showFailureActionPerformed(evt);
            }
        });

        ignore.setForeground(java.awt.Color.blue);
        ignore.setText(org.openide.util.NbBundle.getMessage(ProblemPanel.class, "ProblemPanel.ignore.text")); // NOI18N
        ignore.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        ignore.setBorderPainted(false);
        ignore.setContentAreaFilled(false);
        ignore.setFocusPainted(false);
        ignore.setFocusable(false);
        ignore.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ignore.setOpaque(false);
        ignore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ignoreActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(showFailure)
            .add(ignore)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(showFailure)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ignore))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void showFailureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showFailureActionPerformed
        notification.showFailure();
    }//GEN-LAST:event_showFailureActionPerformed

    private void ignoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ignoreActionPerformed
        notification.ignore();
    }//GEN-LAST:event_ignoreActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ignore;
    private javax.swing.JButton showFailure;
    // End of variables declaration//GEN-END:variables

}
