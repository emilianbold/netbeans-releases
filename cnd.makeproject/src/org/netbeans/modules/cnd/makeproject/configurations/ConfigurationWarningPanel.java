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

/*
 * ConfigurationWarningPanel.java
 *
 * Created on Dec 16, 2009, 11:39:20 AM
 */

package org.netbeans.modules.cnd.makeproject.configurations;

import java.awt.Color;
import org.netbeans.modules.cnd.makeproject.MakeOptions;

/**
 *
 * @author thp
 */
public class ConfigurationWarningPanel extends javax.swing.JPanel {

    /** Creates new form ConfigurationWarningPanel */
    public ConfigurationWarningPanel(String msg) {
        initComponents();
        messageTextarea.setBackground(new Color(getBackground().getRGB()));
        messageTextarea.setText(msg);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        messageTextarea = new javax.swing.JTextArea();
        doNotShowAginCheckBox = new javax.swing.JCheckBox();

        jScrollPane1.setBorder(null);

        messageTextarea.setColumns(20);
        messageTextarea.setEditable(false);
        messageTextarea.setLineWrap(true);
        messageTextarea.setRows(5);
        messageTextarea.setWrapStyleWord(true);
        messageTextarea.setBorder(null);
        jScrollPane1.setViewportView(messageTextarea);

        doNotShowAginCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/configurations/Bundle").getString("ConfigurationWarningPanel.doNotShowAginCheckBox.mn").charAt(0));
        doNotShowAginCheckBox.setText(org.openide.util.NbBundle.getMessage(ConfigurationWarningPanel.class, "ConfigurationWarningPanel.doNotShowAginCheckBox.text")); // NOI18N
        doNotShowAginCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doNotShowAginCheckBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(20, 20, 20)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(doNotShowAginCheckBox)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(doNotShowAginCheckBox))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void doNotShowAginCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doNotShowAginCheckBoxActionPerformed
        MakeOptions.getInstance().setShowConfigurationWarning(!doNotShowAginCheckBox.isSelected());
    }//GEN-LAST:event_doNotShowAginCheckBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox doNotShowAginCheckBox;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea messageTextarea;
    // End of variables declaration//GEN-END:variables

}
