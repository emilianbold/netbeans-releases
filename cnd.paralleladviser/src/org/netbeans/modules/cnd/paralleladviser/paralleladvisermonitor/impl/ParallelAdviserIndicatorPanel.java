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
package org.netbeans.modules.cnd.paralleladviser.paralleladvisermonitor.impl;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.paralleladviser.paralleladviserview.ParallelAdviserTopComponent;
import org.openide.util.Exceptions;

/**
 * Parallel Adviser indicator panel.
 *
 * @author Nick Krasilnikov
 */
public class ParallelAdviserIndicatorPanel extends JPanel {

    private Thread tread;

    private JButton jButton1;

    /** Creates new form MacroExpansionPanel. */
    public ParallelAdviserIndicatorPanel() {
        initComponents();

        setMaximumSize(new java.awt.Dimension(0, 0));
        setMinimumSize(new java.awt.Dimension(0, 0));
        setPreferredSize(new java.awt.Dimension(0, 0));
    }

    public void notifyUser() {

        removeAll();

        jButton1 = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(1000, 30));
        setMinimumSize(new java.awt.Dimension(220, 25));
        setPreferredSize(new java.awt.Dimension(250, 25));

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/paralleladviser/paralleladvisermonitor/resouces/notification.png"))); // NOI18N
        jButton1.setText(org.openide.util.NbBundle.getMessage(ParallelAdviserIndicatorPanel.class, "ParallelAdviserIndicatorPanel.jButton1.text")); // NOI18N
        jButton1.setActionCommand(org.openide.util.NbBundle.getMessage(ParallelAdviserIndicatorPanel.class, "ParallelAdviserIndicatorPanel.jButton1.actionCommand")); // NOI18N
        jButton1.setBorderPainted(false);
        jButton1.setMaximumSize(new java.awt.Dimension(1000, 30));
        jButton1.setMinimumSize(new java.awt.Dimension(100, 25));
        jButton1.setPreferredSize(new java.awt.Dimension(400, 25));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        add(jButton1, java.awt.BorderLayout.CENTER);

        ((JSplitPane)getParent()).resetToPreferredSizes();

        tread = new Thread(new Runnable() {

            boolean light = true;
            int turn = 10;

            public void run() {
                while (turn > 0) {
                    if (light) {
                        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/paralleladviser/paralleladvisermonitor/resouces/notification.png"))); // NOI18N
                    } else {
                        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/paralleladviser/paralleladvisermonitor/resouces/notification2.png"))); // NOI18N
                    }
                    light = !light;
                    turn--;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/paralleladviser/paralleladvisermonitor/resouces/notification.png"))); // NOI18N
            }
        });
        tread.start();
    }
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        Runnable openView = new Runnable() {

            public void run() {
                ParallelAdviserTopComponent view = ParallelAdviserTopComponent.findInstance();
                if (!view.isOpened()) {
                    view.open();
                }
                view.requestActive();
                view.updateTips();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            openView.run();
        } else {
            SwingUtilities.invokeLater(openView);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setMaximumSize(new java.awt.Dimension(1000, 30));
        setMinimumSize(new java.awt.Dimension(220, 25));
        setPreferredSize(new java.awt.Dimension(250, 25));
        setRequestFocusEnabled(false);
        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
