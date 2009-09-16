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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.platform;

import java.awt.event.ActionListener;
import org.netbeans.modules.javacard.constants.CommonSystemFilesystemPaths;
import org.netbeans.modules.javacard.constants.JCConstants;
import org.netbeans.modules.javacard.constants.PlatformTemplateWizardKeys;
import org.netbeans.validation.api.Problem;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.ListView;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.validation.adapters.DialogBuilder;
import org.netbeans.modules.javacard.api.CardCustomizer;
import org.netbeans.modules.javacard.api.CardCustomizerProvider;
import org.netbeans.modules.javacard.api.ValidationGroupProvider;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationUI;

/**
 * Panel which shows the available servers and their customizers
 *
 * @author Tim Boudreau
 */
public final class ServersPanel extends javax.swing.JPanel implements ExplorerManager.Provider, PropertyChangeListener, ActionListener {
    private final ExplorerManager mgr = new ExplorerManager();
    private final VUI vui = new VUI();
    private final ValidationGroup group = ValidationGroup.create(vui);

    public ServersPanel(Node root) {
        initComponents();
        listView().setPopupAllowed(false);
        mgr.setRootContext(root);
        jLabel1.setText("   ");
        mgr.addPropertyChangeListener(this);
        Node[] n = root.getChildren().getNodes(true);
        if (n != null && n.length > 0) {
            try {
                mgr.setSelectedNodes(new Node[]{n[0]});
            } catch (PropertyVetoException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public void showDialog() {
        DialogBuilder b = new DialogBuilder(ServersPanel.class).setModal(true).
                setContent(this).setValidationGroup(group);
        if (b.showDialog(DialogDescriptor.OK_OPTION)) {
            save();
        }
    }

    ListView listView() {
        return (ListView) jScrollPane1;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new ListView();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        jScrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("controlShadow")));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(100, 100));

        jButton1.setText(org.openide.util.NbBundle.getMessage(ServersPanel.class, "ServersPanel.jButton1.text")); // NOI18N
        jButton1.addActionListener(this);

        jButton2.setText(org.openide.util.NbBundle.getMessage(ServersPanel.class, "ServersPanel.jButton2.text")); // NOI18N
        jButton2.addActionListener(this);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText(org.openide.util.NbBundle.getMessage(ServersPanel.class, "ServersPanel.jLabel2.text")); // NOI18N
        jPanel1.add(jLabel2, java.awt.BorderLayout.CENTER);

        jLabel1.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.errorForeground"));
        jLabel1.setText(org.openide.util.NbBundle.getMessage(ServersPanel.class, "ServersPanel.jLabel1.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jButton1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButton2))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton1)
                    .add(jButton2)
                    .add(jLabel1))
                .add(12, 12, 12))
        );
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == jButton1) {
            ServersPanel.this.jButton1ActionPerformed(evt);
        }
        else if (evt.getSource() == jButton2) {
            ServersPanel.this.jButton2ActionPerformed(evt);
        }
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String title = NbBundle.getMessage(ServersPanel.class, "TTL_NEW_SERVER"); //NOI18N
        String msg = NbBundle.getMessage(ServersPanel.class, "MSG_NEW_SERVER"); //NOI18N

        NotifyDescriptor.InputLine in = new NotifyDescriptor.InputLine(msg, title);
        if (DialogDisplayer.getDefault().notify(in) == DialogDescriptor.OK_OPTION) {
            String txt = in.getInputText().trim();
            if (txt.length() == 0) {
                JOptionPane.showMessageDialog(this, NbBundle.getMessage(ServersPanel.class,
                        "ERR_EMPTY_NAME")); //NOI18N
                return;
            }
            if (txt.indexOf('/') > 0 || txt.indexOf('\\') > 0 || txt.indexOf(':') > 0 || txt.indexOf(';') > 0) { //NOI18N
                JOptionPane.showMessageDialog(this, NbBundle.getMessage(ServersPanel.class,
                        "ERR_BAD_NAME")); //NOI18N
                return;
            }

            String deviceTemplateName = CommonSystemFilesystemPaths.SFS_DEVICE_TEMPLATE_PATH;
            try {
                DataObject deviceTemplate = DataObject.find(FileUtil.getConfigFile(deviceTemplateName));
                DataFolder fld = mgr.getRootContext().getLookup().lookup(DataFolder.class);
                String name;
                if (fld.getChildren().length == 0) {
                    name = JCConstants.TEMPLATE_DEFAULT_DEVICE_NAME; //NOI18N
                } else {
                    name = txt;
                }
                Map<String, String> substitutions = new HashMap<String, String>();
                substitutions.put(PlatformTemplateWizardKeys.PROJECT_TEMPLATE_DEVICE_NAME_KEY, txt);
                deviceTemplate.createFromTemplate(fld, name, substitutions);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }

    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        Node[] nodes = mgr.getSelectedNodes();
        for (Node n : nodes) {
            DataObject dob = n.getLookup().lookup(DataObject.class);
            if (dob != null) {
                try {
                    dob.delete();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        try {
            mgr.setSelectedNodes(new Node[0]);
        } catch (PropertyVetoException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_jButton2ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public ExplorerManager getExplorerManager() {
        return mgr;
    }

    private void save() {
        for (CardCustomizer c : toSave.values()) {
            if (c.isContentValid()) {
                c.save();
            }
        }
        toSave.clear();
    }


    private final Map<Node, CardCustomizer> toSave = new HashMap<Node, CardCustomizer>();
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            Node[] n = mgr.getSelectedNodes();
            if (n != null && n.length > 0) {
                jPanel1.removeAll();
                CardCustomizer cust = toSave.get(n[0]);
                if (cust == null) {
                    CardCustomizerProvider prov = n[0].getLookup().lookup(CardCustomizerProvider.class);
                    if (prov != null) {
                        cust = prov.getCardCustomizer();
                        if (cust == null) {
                            throw new NullPointerException ("CardCustomizerProvider " + prov //NOI18N
                                    + " provides a null CardCustomizer"); //NOI18N
                        }
                        toSave.put(n[0], cust);
                    }
                }

                if (cust == null) {
                    Component customizer = n[0].getCustomizer();
                    if (customizer != null) { //hack - we return false from hasCustomizer() to avoid getting an extra Customize action
                        if (customizer != null) {
                            if (customizer instanceof ValidationGroupProvider) {
                                ValidationGroup g = ((ValidationGroupProvider) customizer).getValidationGroup();
                                this.group.addValidationGroup(g, true);
                            }
                            jPanel1.add(customizer, BorderLayout.CENTER);
                        }
                    } else {
                        JLabel lbl = new JLabel(NbBundle.getMessage(ServersPanel.class,
                                "MSG_NO_CUSTOMIZER")); //NOI18N
                        lbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                        jPanel1.add(lbl, BorderLayout.CENTER);
                    }
                } else {
                    Component customizer = cust.getComponent();
                    if (customizer == null) {
                        throw new NullPointerException ("CardCustomizer " + cust //NOI18N
                                + " provides a null component"); //NOI18N
                    }
                    ValidationGroup g = cust.getValidationGroup();
                    if (g == null) {
                        throw new NullPointerException ("CardCustomizer " + cust //NOI18N
                                + " provides a null validation group"); //NOI18N
                    }
                    this.group.addValidationGroup(g, true);
                    jPanel1.add(customizer, BorderLayout.CENTER);
                }
                invalidate();
                revalidate();
                repaint();
                repack();
            }
        }
    }

    private void repack() {
        JDialog dlg = (JDialog) SwingUtilities.getAncestorOfClass(JDialog.class, this);
        if (dlg != null) {
            dlg.pack();
        }
    }

    private final class VUI implements ValidationUI {

        public void clearProblem() {
            jScrollPane1.setEnabled(true);
            jScrollPane1.getViewport().getView().setEnabled(true);
        }

        public void setProblem(Problem arg0) {
            jScrollPane1.setEnabled(false);
            jScrollPane1.getViewport().getView().setEnabled(false);
        }
    }
}
