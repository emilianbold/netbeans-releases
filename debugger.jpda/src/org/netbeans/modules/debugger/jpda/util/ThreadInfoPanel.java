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

package org.netbeans.modules.debugger.jpda.util;

import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
/**
 * An informational panel, that is opened in Threads View.
 * 
 * @author  Martin Entlicher
 */
public class ThreadInfoPanel extends javax.swing.JPanel {
    
    private TopComponent lastActiveTC;
    private int buttonPressed = 0;
    private ButtonListener bl;
    
    /** Creates new form ThreadInfoPanel */
    private ThreadInfoPanel(String message, String btn1, String tip1, String btn2, String tip2) {
        initComponents();
        messageLabel.setText("<html>"+message+"</html>");
        button1.setText(btn1);
        button1.setToolTipText(tip1);
        button2.setText(btn2);
        button2.setToolTipText(tip2);
    }
    
    public static ThreadInfoPanel create(String message, String btn1, String tip1, String btn2, String tip2) {
        TopComponent tc = WindowManager.getDefault().findTopComponent("threadsView"); // NOI18N
        ThreadInfoPanel panel = new ThreadInfoPanel(message, btn1, tip1, btn2, tip2);
        Mode tcMode = WindowManager.getDefault().findMode(tc);
        addPanel(tcMode, tc, panel);
        if (tcMode != null) {
            TopComponent activeTC = tcMode.getSelectedTopComponent();
            if (activeTC != null && !activeTC.equals(tc)) {
                panel.lastActiveTC = activeTC;
            }
        }
        if (!tc.isOpened()) {
            tc.open();
        }
        tc.requestVisible();
        tc.requestAttention(true);
        return panel;
    }
    
    private static void addPanel(final Mode mode, final TopComponent tc, final java.awt.Component panel) {
        if (tc.getComponentCount() > 0) {//tc.isOpened() && mode.getSelectedTopComponent() == tc) {
            tc.add(panel, java.awt.BorderLayout.NORTH);
        } else {
            tc.addContainerListener(new java.awt.event.ContainerAdapter() {
                public void componentAdded(java.awt.event.ContainerEvent ce) {
                    tc.removeContainerListener(this);
                    addPanel(mode, tc, panel);
                }
            });
        }
    }
    
    public void setButtonListener(ButtonListener bl) {
        this.bl = bl;
    }
    
    public void dismiss() {
        if (!javax.swing.SwingUtilities.isEventDispatchThread()) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    dismiss();
                }
            });
            return ;
        }
        TopComponent tc = WindowManager.getDefault().findTopComponent("threadsView"); // NOI18N
        tc.remove(this);
        if (lastActiveTC != null /*&& buttonPressed == 0*/) {
            lastActiveTC.requestVisible();
        }
        lastActiveTC = null;
        bl = null;
    }
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        button1 = new javax.swing.JButton();
        button2 = new javax.swing.JButton();
        messageLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        button1.setText("jButton1");
        button1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button1ActionPerformed(evt);
            }
        });

        button2.setText("jButton2");
        button2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button2ActionPerformed(evt);
            }
        });

        messageLabel.setText("<html>Text</html>");

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/debugger/jpda/util/information_16.png")));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(button1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(button2))
                    .add(messageLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(layout.createSequentialGroup()
                        .add(messageLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(button1)
                            .add(button2))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void button1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button1ActionPerformed
    buttonPressed = 1;
    if (bl != null) {
        bl.buttonPressed(1);
    }
    dismiss();
}//GEN-LAST:event_button1ActionPerformed

    private void button2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button2ActionPerformed
    buttonPressed = 2;
    if (bl != null) {
        bl.buttonPressed(2);
    }
    dismiss();
}//GEN-LAST:event_button2ActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button1;
    private javax.swing.JButton button2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel messageLabel;
    // End of variables declaration//GEN-END:variables
    
    public static interface ButtonListener {
        public void buttonPressed(int n);
    }
}
