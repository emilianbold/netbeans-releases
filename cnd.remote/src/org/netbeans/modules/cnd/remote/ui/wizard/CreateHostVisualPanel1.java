/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.cnd.remote.ui.wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;

/*package*/ final class CreateHostVisualPanel1 extends JPanel {

    private final HostsListTableModel tableModel = new HostsListTableModel();
    private final CreateHostData data;

    public CreateHostVisualPanel1(CreateHostData data, final ChangeListener listener) {
        this.data = data;
        initComponents();
        lblUser.setVisible(data.isManagingUser());
        textUser.setVisible(data.isManagingUser());
        textPort.setText(Integer.toString(22));
        textHostname.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                listener.stateChanged(null);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                listener.stateChanged(null);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                listener.stateChanged(null);
            }
        });
        pbarStatusPanel.removeAll();
        pbarStatusPanel.add(ProgressHandleFactory.createProgressComponent(tableModel.getProgressHandle()), BorderLayout.CENTER);
        pbarStatusPanel.validate();

        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                tableModel.start(new Runnable() {
                    @Override
                    public void run() {
                        pbarStatusPanel.setVisible(false);
                    }
                });
            }
            @Override
            public void ancestorRemoved(AncestorEvent event) {
                tableModel.stop();
            }
            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        textHostname.requestFocus();
                    }
                });
            }
        });
    }

    void init() {
        textUser.setText(data.getUserName());
        textPort.setText(Integer.toString(data.getPort()));
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(getClass(), "CreateHostVisualPanel1.Title");
    }

    public String getHostname() {
        return textHostname.getText().trim();
    }

    public String getUser() {
        return textUser.getText().trim();
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        textUser.setEnabled(enabled);
        textHostname.setEnabled(enabled);
        textHostname.setEditable(enabled);
        textPort.setEnabled(enabled);
        textPort.setEditable(enabled);
        lblUser.setEnabled(enabled);
        lblHostName.setEnabled(enabled);
        lblNeighbouthood.setEnabled(enabled);
        lblPort.setEnabled(enabled);
        tableHostsList.setEnabled(enabled);
    }

    public Integer getPort() {
        if ("".equals(textPort.getText())) { // NOI18N
            return 22;
        }

        try {
            return Integer.valueOf(Integer.parseInt(textPort.getText().trim()));
        } catch(NumberFormatException e) {
            //return Integer.valueOf(ExecutionEnvironmentFactory.DEFAULT_PORT);
            return null;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblHostName = new javax.swing.JLabel();
        textHostname = new javax.swing.JTextField();
        lblNeighbouthood = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableHostsList = new HostTable();
        pbarStatusPanel = new javax.swing.JPanel();
        lblPort = new javax.swing.JLabel();
        textPort = new org.netbeans.modules.cnd.remote.ui.wizard.PortTextField();
        lblUser = new javax.swing.JLabel();
        textUser = new javax.swing.JTextField();

        setPreferredSize(new java.awt.Dimension(534, 409));
        setRequestFocusEnabled(false);

        lblHostName.setLabelFor(textHostname);
        org.openide.awt.Mnemonics.setLocalizedText(lblHostName, org.openide.util.NbBundle.getMessage(CreateHostVisualPanel1.class, "CreateHostVisualPanel1.lblHostName.text")); // NOI18N

        textHostname.setText(org.openide.util.NbBundle.getMessage(CreateHostVisualPanel1.class, "CreateHostVisualPanel1.textHostname.text")); // NOI18N

        lblNeighbouthood.setLabelFor(tableHostsList);
        org.openide.awt.Mnemonics.setLocalizedText(lblNeighbouthood, org.openide.util.NbBundle.getMessage(CreateHostVisualPanel1.class, "CreateHostVisualPanel1.lblNeighbouthood.text")); // NOI18N

        tableHostsList.setModel(tableModel);
        tableHostsList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableHostsListMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tableHostsList);
        tableHostsList.getColumnModel().getColumn(0).setHeaderValue(org.openide.util.NbBundle.getMessage(CreateHostVisualPanel1.class, "CreateHostVisualPanel1.tableHostsList.columnModel.title0")); // NOI18N
        tableHostsList.getColumnModel().getColumn(1).setHeaderValue(org.openide.util.NbBundle.getMessage(CreateHostVisualPanel1.class, "CreateHostVisualPanel1.tableHostsList.columnModel.title1")); // NOI18N

        pbarStatusPanel.setMaximumSize(new java.awt.Dimension(2147483647, 10));
        pbarStatusPanel.setMinimumSize(new java.awt.Dimension(100, 10));
        pbarStatusPanel.setLayout(new java.awt.BorderLayout());

        lblPort.setLabelFor(textPort);
        org.openide.awt.Mnemonics.setLocalizedText(lblPort, org.openide.util.NbBundle.getMessage(CreateHostVisualPanel1.class, "CreateHostVisualPanel1.lblPort.text")); // NOI18N

        textPort.setText(org.openide.util.NbBundle.getMessage(CreateHostVisualPanel1.class, "CreateHostVisualPanel1.textPort.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lblUser, org.openide.util.NbBundle.getMessage(CreateHostVisualPanel1.class, "CreateHostVisualPanel1.lblUser.text")); // NOI18N

        textUser.setText(org.openide.util.NbBundle.getMessage(CreateHostVisualPanel1.class, "CreateHostVisualPanel1.textUser.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pbarStatusPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 534, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(lblNeighbouthood)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblUser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblHostName))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(textUser, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                            .addComponent(textHostname, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblPort)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textPort, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUser)
                    .addComponent(textUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textHostname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblHostName)
                    .addComponent(lblPort)
                    .addComponent(textPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblNeighbouthood)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(pbarStatusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tableHostsListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableHostsListMouseClicked
        // TODO add your handling code here:
        if (evt.getClickCount() == 2) {
            int row = tableHostsList.rowAtPoint(evt.getPoint());
            textHostname.setText(((HostsListTableModel) tableHostsList.getModel()).getHostName(row));
        }
}//GEN-LAST:event_tableHostsListMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblHostName;
    private javax.swing.JLabel lblNeighbouthood;
    private javax.swing.JLabel lblPort;
    private javax.swing.JLabel lblUser;
    private javax.swing.JPanel pbarStatusPanel;
    private javax.swing.JTable tableHostsList;
    private javax.swing.JTextField textHostname;
    private org.netbeans.modules.cnd.remote.ui.wizard.PortTextField textPort;
    private javax.swing.JTextField textUser;
    // End of variables declaration//GEN-END:variables

    private class HostTable extends JTable {

        private final Color stdBackground;
        private final Color stdForeground;
        private final Color stdGrid;
        private final Color stdHeader;

        public HostTable() {
            stdBackground = getBackground();
            stdForeground = getForeground();
            stdGrid = getGridColor();
            stdHeader = getTableHeader().getForeground();
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            if (enabled) {
                setForeground(stdForeground);
                setBackground(stdBackground);
                setGridColor(stdGrid);
                getTableHeader().setForeground(stdHeader);
            } else {
                Color back = textPort.getBackground();
                Color fore = Color.lightGray; // textPort.getForeground();
                setBackground(back);
                setForeground(fore);
                setGridColor(fore);
                getTableHeader().setForeground(fore);
            }
            invalidate();
            repaint();
        }
    }
}

