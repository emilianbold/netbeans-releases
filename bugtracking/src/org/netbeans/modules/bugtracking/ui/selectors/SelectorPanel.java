/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.ui.selectors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class SelectorPanel extends javax.swing.JPanel implements PropertyChangeListener {
    private Repository currentRepo;
    private DialogDescriptor dd;
    private ImageIcon errorIcon;

    /** Creates new form ConnectorPanel */
    public SelectorPanel() {
        initComponents();
        errorLabel.setForeground(new Color(153,0,0));
        errorIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/bugtracking/ui/resources/error.gif")); //NOI18N
        
        final ListCellRenderer lcr = connectorCbo.getRenderer();
        connectorCbo.setRenderer(new ListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if(value != null) {
                    BugtrackingConnector rc = (BugtrackingConnector) value;
                    value = rc.getDisplayName();
                }
                return lcr.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
    }

    boolean open() {
        String title = NbBundle.getMessage(SelectorPanel.class, "CTL_CreateTitle");
        dd = new DialogDescriptor(this, title);
        validateController();
        boolean ret = DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION;
        return ret;
    }

    boolean edit(Repository repository, String errorMessage) {
        connectorCbo.setVisible(false);
        connectorLabel.setVisible(false);
        currentRepo = repository;
        setRepoPanel(repository);
        String title = NbBundle.getMessage(SelectorPanel.class, "CTL_EditTitle");
        dd = new DialogDescriptor(this, title);
        validateController();
        updateErrorLabel(errorMessage);
        boolean ret = DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION;
        return ret;
    }

    Repository getRepository() {
        return currentRepo;
    }

    void setConnectors(BugtrackingConnector[] connectors) {
        connectorCbo.setModel(new DefaultComboBoxModel(connectors));
        updateRepoPanel((BugtrackingConnector) connectorCbo.getSelectedItem());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                connectorLabel = new javax.swing.JLabel();
                errorLabel = new javax.swing.JLabel();

                connectorLabel.setText(org.openide.util.NbBundle.getMessage(SelectorPanel.class, "SelectorPanel.connectorLabel.text")); // NOI18N

                connectorCbo.addItemListener(new java.awt.event.ItemListener() {
                        public void itemStateChanged(java.awt.event.ItemEvent evt) {
                                connectorCboItemStateChanged(evt);
                        }
                });

                repoPanel.setLayout(new java.awt.BorderLayout());

                errorLabel.setText(org.openide.util.NbBundle.getMessage(SelectorPanel.class, "SelectorPanel.errorLabel.text")); // NOI18N

                org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
                this.setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(layout.createSequentialGroup()
                                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                        .add(layout.createSequentialGroup()
                                                                .add(repoPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                                        .add(layout.createSequentialGroup()
                                                                .add(connectorLabel)
                                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                                .add(connectorCbo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                                .add(14, 14, 14))
                                        .add(layout.createSequentialGroup()
                                                .add(errorLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addContainerGap())))
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        .add(connectorLabel)
                                        .add(connectorCbo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(repoPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(errorLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );
        }// </editor-fold>//GEN-END:initComponents

    private void connectorCboItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_connectorCboItemStateChanged
        if(evt.getStateChange() != ItemEvent.SELECTED) return;
        updateRepoPanel((BugtrackingConnector) evt.getItem());
    }//GEN-LAST:event_connectorCboItemStateChanged

        // Variables declaration - do not modify//GEN-BEGIN:variables
        final javax.swing.JComboBox connectorCbo = new javax.swing.JComboBox();
        private javax.swing.JLabel connectorLabel;
        private javax.swing.JLabel errorLabel;
        final javax.swing.JPanel repoPanel = new javax.swing.JPanel();
        // End of variables declaration//GEN-END:variables


    private void updateRepoPanel(BugtrackingConnector connector) {
        if (connector == null) {
            return;
        }
        currentRepo = connector.createRepository();
        if (currentRepo == null) {
            return;
        }
        setRepoPanel(currentRepo);
        return;
    }

    private void setRepoPanel(Repository repository) {
        BugtrackingController controller = repository.getController();
        controller.addPropertyChangeListener(this);
        JComponent comp = controller.getComponent();
        repoPanel.removeAll();
        repoPanel.add(comp, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(BugtrackingController.EVENT_COMPONENT_DATA_CHANGED)) {
            validateController();
        }
    }

    private void validateController() {
        if (dd != null && currentRepo != null) {
            BugtrackingController controller = currentRepo.getController();
            boolean valid = controller.isValid();
            dd.setValid(valid);
            String msg = controller.getErrorMessage();
            updateErrorLabel(msg);
        }
    }

    private void updateErrorLabel(String msg) {
        if(msg == null) {
            errorLabel.setText("");
            errorLabel.setIcon(null);
        } else {
            errorLabel.setText(msg);
            errorLabel.setIcon(errorIcon);
        }
    }
}
