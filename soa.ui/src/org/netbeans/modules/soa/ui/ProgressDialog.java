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

package org.netbeans.modules.soa.ui;

import java.awt.CardLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;
import javax.swing.SwingUtilities;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;

/**
 * The dialog shows progress bar in modal dialog while a long running process 
 * is executed. The dialog is modal and such way it blocks other user's activities.
 *
 * @author Nikita Krjukov
 */
public class ProgressDialog extends javax.swing.JDialog {

    /** Creates new form ProgressDialog */
    public ProgressDialog(java.awt.Dialog parent) {
        super(parent, true);
        initComponents();
    }

    public void start(String processingText, final Callable<String> process,
            final Runnable afterFinish) {
        lblProcessing.setText(processingText);
        bntClose.setEnabled(false);
        this.getRootPane().setDefaultButton(bntClose);
        //
        // Set first card visible
        LayoutManager lm = this.getContentPane().getLayout();
        assert lm instanceof CardLayout;
        CardLayout.class.cast(lm).show(this.getContentPane(), "processing"); // NOI18N
        //
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                String result = null;
                try {
                    result = process.call();
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                } finally {
                    final String finalResult = result;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            String resultText = finalResult;
                            if (resultText == null || resultText.length() == 0) {
                                resultText = "Unknown result. Callable should return result text to show"; // NOI18N
                            }
                            lblFinished.setText(resultText);
                            //
                            bntClose.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    ProgressDialog.this.setVisible(false);
                                    afterFinish.run();
                                }
                            });
                            //
                            LayoutManager lm = ProgressDialog.this.getContentPane().getLayout();
                            assert lm instanceof CardLayout;
                            CardLayout.class.cast(lm).show(
                                    ProgressDialog.this.getContentPane(), "finished"); // NOI18N
                            bntClose.setEnabled(true);
                            bntClose.requestFocusInWindow();
                        }
                    });
                }
            }
        });
        //
        this.setLocationRelativeTo(getOwner());
        this.setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlResult = new javax.swing.JPanel();
        lblFinished = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        bntClose = new javax.swing.JButton();
        pnlProgress = new javax.swing.JPanel();
        lblProcessing = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(ProgressDialog.class, "TITL_ProgressDlg")); // NOI18N
        setModal(true);
        getContentPane().setLayout(new java.awt.CardLayout());

        lblFinished.setFocusable(false);

        jPanel1.setFocusable(false);

        bntClose.setText(org.openide.util.NbBundle.getMessage(ProgressDialog.class, "BNT_Close")); // NOI18N
        jPanel1.add(bntClose);

        org.jdesktop.layout.GroupLayout pnlResultLayout = new org.jdesktop.layout.GroupLayout(pnlResult);
        pnlResult.setLayout(pnlResultLayout);
        pnlResultLayout.setHorizontalGroup(
            pnlResultLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, pnlResultLayout.createSequentialGroup()
                .addContainerGap()
                .add(pnlResultLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, lblFinished, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlResultLayout.setVerticalGroup(
            pnlResultLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, pnlResultLayout.createSequentialGroup()
                .addContainerGap()
                .add(lblFinished, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        getContentPane().add(pnlResult, "finished");

        progressBar.setIndeterminate(true);

        org.jdesktop.layout.GroupLayout pnlProgressLayout = new org.jdesktop.layout.GroupLayout(pnlProgress);
        pnlProgress.setLayout(pnlProgressLayout);
        pnlProgressLayout.setHorizontalGroup(
            pnlProgressLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, pnlProgressLayout.createSequentialGroup()
                .addContainerGap()
                .add(pnlProgressLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, lblProcessing, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, progressBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlProgressLayout.setVerticalGroup(
            pnlProgressLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, pnlProgressLayout.createSequentialGroup()
                .addContainerGap()
                .add(lblProcessing, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        getContentPane().add(pnlProgress, "processing");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bntClose;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblFinished;
    private javax.swing.JLabel lblProcessing;
    private javax.swing.JPanel pnlProgress;
    private javax.swing.JPanel pnlResult;
    private javax.swing.JProgressBar progressBar;
    // End of variables declaration//GEN-END:variables

}
