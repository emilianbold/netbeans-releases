/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.problems;

import org.netbeans.modules.maven.api.problem.ProblemReport;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.util.NbBundle;

/**
 *
 * @author  mkleint
 */
public class ProblemsPanel extends javax.swing.JPanel {
    private ProblemReporterImpl reporter;
    private ChangeListener change;
    private JButton button;
    private JButton close;
    /** Creates new form ProblemsPanel */
    public ProblemsPanel(ProblemReporterImpl report) {
        initComponents();
        this.reporter = report;
        taDescription.setLineWrap(true);
        taDescription.setWrapStyleWord(true);
        lstProblems.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                ProblemReport report = (ProblemReport)value;
                return super.getListCellRendererComponent(list, report.getShortDescription(), 
                        index, isSelected, cellHasFocus);
            }
        });
        lstProblems.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstProblems.addListSelectionListener(new ListSelectionListener() {
              public void valueChanged(ListSelectionEvent e) {
                  Object val = lstProblems.getSelectedValue();
                  if (val != null) {
                      ProblemReport rep = (ProblemReport)val;
                      taDescription.setText(rep.getLongDescription());
                      if (rep.getCorrectiveAction() != null) {
                          button.setAction(rep.getCorrectiveAction());
                      } else {
                          button.setAction(new NoopAction());
                      }
                  } else {
                      taDescription.setText(""); //NOI18N
                      button.setAction(new NoopAction());
                  }
              }
        });
        change = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        populateUI();
                    }
                });
            }
        };
    }
    
    public void setActionButton(JButton butt) {
        button = butt;
    }
    
    public void setCloseButton(JButton close) {
        this.close = close;
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        populateUI();
        reporter.addChangeListener(change);
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        reporter.removeChangeListener(change);
    }
    
    private void populateUI() {
        Iterator it = reporter.getReports().iterator();
        DefaultListModel model = new DefaultListModel();
        while (it.hasNext()) {
            model.addElement(it.next());
        }
        lstProblems.setModel(model);
        if (lstProblems.getModel().getSize() > 0) {
            lstProblems.setSelectedIndex(0);
        } else {
            close.doClick();
        }
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        lstProblems = new javax.swing.JList();
        lblDescription = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        taDescription = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();

        jScrollPane1.setViewportView(lstProblems);

        lblDescription.setLabelFor(taDescription);
        org.openide.awt.Mnemonics.setLocalizedText(lblDescription, org.openide.util.NbBundle.getMessage(ProblemsPanel.class, "LBL_Description")); // NOI18N

        taDescription.setColumns(20);
        taDescription.setRows(5);
        jScrollPane2.setViewportView(taDescription);

        jLabel1.setLabelFor(lstProblems);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ProblemsPanel.class, "LBL_Problems")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE)
                    .add(jLabel1)
                    .add(lblDescription)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 529, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lblDescription)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblDescription;
    private javax.swing.JList lstProblems;
    private javax.swing.JTextArea taDescription;
    // End of variables declaration//GEN-END:variables
    
    
    private class NoopAction extends AbstractAction {
        
        public NoopAction() {
            setEnabled(false);
            putValue(Action.NAME, NbBundle.getMessage(ProblemsPanel.class, "BTN_Correct"));
        }
        
        public void actionPerformed(ActionEvent e) {
        }
        
    }
}
