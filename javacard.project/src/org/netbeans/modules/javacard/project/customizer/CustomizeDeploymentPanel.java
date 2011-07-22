/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.javacard.project.customizer;

import com.sun.javacard.AID;
import com.sun.javacard.filemodels.DeploymentXmlAppletEntry;
import com.sun.javacard.filemodels.DeploymentXmlInstanceEntry;
import java.awt.event.ActionListener;
import org.netbeans.modules.javacard.common.Utils;
import org.openide.awt.HtmlRenderer;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.javacard.common.GuiUtils;
import org.netbeans.modules.javacard.project.ui.FileModelFactory;
import org.openide.util.HelpCtx;

/**
 *
 * @author Tim Boudreau
 */
public class CustomizeDeploymentPanel extends javax.swing.JPanel implements ChangeListener, ListSelectionListener, DocumentListener, ListCellRenderer, ActionListener {

    private final ChangeSupport supp = new ChangeSupport(this);
    private DeploymentXmlAppletEntry entry;
    private AID appletAid;
    private String classname;
    private Node node;

    public CustomizeDeploymentPanel(Node n) {
        this.node = n;
        initComponents();
        instancesList.setCellRenderer(this);
        instancesList.getSelectionModel().addListSelectionListener(this);
        classname = n.getLookup().lookup(String.class);
        if (classname != null) {
            aidPnl.setClassFqn(classname);
            titleLabel.setText(NbBundle.getMessage(CustomizeDeploymentPanel.class,
                    "CUSTOMIZE_INSTANCES", classname)); //NOI18N
        }
        appletAid = (AID) n.getValue(FileModelFactory.APPLET_AID);
        GuiUtils.prepareContainer(this);
        GuiUtils.filterNonHexadecimalKeys(deploymentParamsField);
        deploymentParamsField.getDocument().addDocumentListener(this);
        DeploymentXmlAppletEntry e = (DeploymentXmlAppletEntry) 
                n.getValue(FileModelFactory.DEPLOYMENT_ENTRY);
        if (e != null) {
            //Make a clone we can change freely
            setEntry((DeploymentXmlAppletEntry) e.clone());
        }
        aidPnl.addChangeListener(this);
        HelpCtx.setHelpIDString(this, "org.netbeans.modules.javacard.ClassicAppletAddInstanceApplet"); //NOI18N
        instructionsPane.getViewport().setOpaque(false);
        instructionsTextArea.getCaret().setVisible(false);
    }

    public void saveChanges() {
        if (this.entry != null && this.node != null) {
            node.setValue(FileModelFactory.DEPLOYMENT_ENTRY, entry);
        }
    }

    private void change() {
        if (inSetInstance) {
            return;
        }
        String deployParams = deploymentParamsField.getText().trim();
        if (deployParams.length() > 0) {
            if (deployParams.length() % 2 != 0) {
                setProblem(NbBundle.getMessage(CustomizeDeploymentPanel.class,
                        "ODD_NUMBER_OF_DIGITS")); //NOI18N
                return;
            }
            try {
                byte[] b = new byte[(deployParams.length() / 2) + 1];
                Utils.getByteArrayForString(deployParams, b, 0);
            } catch (Exception e) {
                setProblem(NbBundle.getMessage(CustomizeDeploymentPanel.class,
                        "ILLEGAL_HEX", deployParams)); //NOI18N
                return;
            }
        }
        AID aid = aidPnl.getAID();
        if (entry != null) {
            Set<AID> aids = new HashSet<AID>();
            for (DeploymentXmlInstanceEntry e : entry.getData()) {
                if (e == instance) continue;
                AID a = e.getInstanceAID();
                if (a != null) {
                    aids.add(e.getInstanceAID());
                }
            }
            if (aids.contains(aid)) {
                setProblem (NbBundle.getMessage(CustomizeDeploymentPanel.class,
                        "AID_IN_USE", aid));
                return;
            }
        }

        String prb = aidPnl.getProblem();
        if (prb == null && instance != null && aid != null) {
            instance.setDeploymentParams(deployParams);
            instance.setInstanceAID(aid);
        }
        setProblem(prb);
    }
    private String problem;

    public String getProblem() {
        return problem;
    }

    private void setProblem(String problem) {
        this.problem = problem;
        boolean isProblem = isProblem();
        //don't allow changing away with invalid values
        addButton.setEnabled(!isProblem);
        removeButton.setEnabled(!isProblem);
        instancesList.setEnabled(!isProblem);
        fireChange();
    }

    public boolean isProblem() {
        return problem != null && problem.trim().length() > 0;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        titleLabel = new javax.swing.JLabel();
        instructionsPane = new javax.swing.JScrollPane();
        instructionsTextArea = new javax.swing.JTextArea();
        instancesPane = new javax.swing.JScrollPane();
        instancesList = new javax.swing.JList();
        aidPnl = new org.netbeans.modules.javacard.common.AIDPanel();
        appletInstanceLabel = new javax.swing.JLabel();
        deploymentParamsLabel = new javax.swing.JLabel();
        deploymentParamsField = new javax.swing.JTextField();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();

        titleLabel.setFont(titleLabel.getFont().deriveFont(titleLabel.getFont().getStyle() | java.awt.Font.BOLD));
        titleLabel.setLabelFor(instructionsTextArea);
        titleLabel.setText(org.openide.util.NbBundle.getMessage(CustomizeDeploymentPanel.class, "CustomizeDeploymentPanel.titleLabel.text", new Object[] {})); // NOI18N
        titleLabel.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0, 0, 0)));

        instructionsPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        instructionsPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        instructionsPane.setViewportBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        instructionsPane.setOpaque(false);

        instructionsTextArea.setBackground(javax.swing.UIManager.getDefaults().getColor("control"));
        instructionsTextArea.setColumns(20);
        instructionsTextArea.setEditable(false);
        instructionsTextArea.setFont(aidPnl.getFont());
        instructionsTextArea.setLineWrap(true);
        instructionsTextArea.setRows(5);
        instructionsTextArea.setText(org.openide.util.NbBundle.getMessage(CustomizeDeploymentPanel.class, "CustomizeDeploymentPanel.instructionsTextArea.text", new Object[] {})); // NOI18N
        instructionsTextArea.setWrapStyleWord(true);
        instructionsTextArea.setOpaque(false);
        instructionsPane.setViewportView(instructionsTextArea);

        instancesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        instancesList.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizeDeploymentPanel.class, "CustomizeDeploymentPanel.instancesList.toolTipText", new Object[] {})); // NOI18N
        instancesList.setEnabled(false);
        instancesPane.setViewportView(instancesList);

        aidPnl.setEnabled(false);
        aidPnl.setTitle(org.openide.util.NbBundle.getMessage(CustomizeDeploymentPanel.class, "CustomizeDeploymentPanel.aidPnl.title", new Object[] {})); // NOI18N

        appletInstanceLabel.setFont(appletInstanceLabel.getFont().deriveFont(appletInstanceLabel.getFont().getStyle() | java.awt.Font.BOLD));
        appletInstanceLabel.setLabelFor(instancesList);
        appletInstanceLabel.setText(org.openide.util.NbBundle.getMessage(CustomizeDeploymentPanel.class, "CustomizeDeploymentPanel.appletInstanceLabel.text", new Object[] {})); // NOI18N
        appletInstanceLabel.setEnabled(false);

        deploymentParamsLabel.setFont(deploymentParamsLabel.getFont().deriveFont(deploymentParamsLabel.getFont().getStyle() | java.awt.Font.BOLD));
        deploymentParamsLabel.setLabelFor(deploymentParamsField);
        deploymentParamsLabel.setText(org.openide.util.NbBundle.getMessage(CustomizeDeploymentPanel.class, "CustomizeDeploymentPanel.deploymentParamsLabel.text", new Object[] {})); // NOI18N
        deploymentParamsLabel.setEnabled(false);

        deploymentParamsField.setText(org.openide.util.NbBundle.getMessage(CustomizeDeploymentPanel.class, "CustomizeDeploymentPanel.deploymentParamsField.text", new Object[] {})); // NOI18N
        deploymentParamsField.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizeDeploymentPanel.class, "CustomizeDeploymentPanel.deploymentParamsField.toolTipText", new Object[] {})); // NOI18N
        deploymentParamsField.setEnabled(false);

        addButton.setText(org.openide.util.NbBundle.getMessage(CustomizeDeploymentPanel.class, "CustomizeDeploymentPanel.addButton.text", new Object[] {})); // NOI18N
        addButton.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizeDeploymentPanel.class, "CustomizeDeploymentPanel.addButton.toolTipText", new Object[] {})); // NOI18N
        addButton.setEnabled(false);
        addButton.addActionListener(this);

        removeButton.setText(org.openide.util.NbBundle.getMessage(CustomizeDeploymentPanel.class, "CustomizeDeploymentPanel.removeButton.text", new Object[] {})); // NOI18N
        removeButton.setToolTipText(org.openide.util.NbBundle.getMessage(CustomizeDeploymentPanel.class, "CustomizeDeploymentPanel.removeButton.toolTipText", new Object[] {})); // NOI18N
        removeButton.setEnabled(false);
        removeButton.addActionListener(this);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(instructionsPane, javax.swing.GroupLayout.DEFAULT_SIZE, 615, Short.MAX_VALUE)
                    .addComponent(titleLabel)
                    .addComponent(appletInstanceLabel)
                    .addComponent(deploymentParamsLabel)
                    .addComponent(deploymentParamsField, javax.swing.GroupLayout.DEFAULT_SIZE, 615, Short.MAX_VALUE)
                    .addComponent(aidPnl, javax.swing.GroupLayout.DEFAULT_SIZE, 615, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(instancesPane, javax.swing.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(removeButton)
                            .addComponent(addButton))))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addButton, removeButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(titleLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(instructionsPane, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(appletInstanceLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(instancesPane, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deploymentParamsLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deploymentParamsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(aidPnl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == addButton) {
            CustomizeDeploymentPanel.this.addButtonActionPerformed(evt);
        }
        else if (evt.getSource() == removeButton) {
            CustomizeDeploymentPanel.this.removeButtonActionPerformed(evt);
        }
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        AID aid = nextUniqueAid();
        String params = deploymentParamsField.getText().trim();
        int sz = instancesList.getModel().getSize();
        DeploymentXmlInstanceEntry e = new DeploymentXmlInstanceEntry(aid, params,
                sz);
        entry.add(e);
        ((DefaultListModel) instancesList.getModel()).addElement(e);
        instancesList.setSelectedIndex(sz);
        deploymentParamsField.requestFocus();
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        Object o = instancesList.getSelectedValue();
        if (o != null && o instanceof DeploymentXmlInstanceEntry) {
            DeploymentXmlInstanceEntry e = (DeploymentXmlInstanceEntry) o;
            int ix = ((DefaultListModel) instancesList.getModel()).indexOf(e);
            entry.remove(e);
            ((DefaultListModel) instancesList.getModel()).removeElement(e);
            int size = instancesList.getModel().getSize();
            if (size > 0) {
                if (ix < size - 1) {
                    instancesList.setSelectedIndex(ix);
                } else {
                    instancesList.setSelectedIndex(size - 1);
                }
                instancesList.requestFocus();
            } else {
                removeButton.setEnabled(false);
            }
        }
    }//GEN-LAST:event_removeButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private org.netbeans.modules.javacard.common.AIDPanel aidPnl;
    private javax.swing.JLabel appletInstanceLabel;
    private javax.swing.JTextField deploymentParamsField;
    private javax.swing.JLabel deploymentParamsLabel;
    private javax.swing.JList instancesList;
    private javax.swing.JScrollPane instancesPane;
    private javax.swing.JScrollPane instructionsPane;
    private javax.swing.JTextArea instructionsTextArea;
    private javax.swing.JButton removeButton;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables

    private void enabled(boolean val) {
        addButton.setEnabled(val);
        removeButton.setEnabled(val);
        appletInstanceLabel.setEnabled(val);
        instancesList.setEnabled(val);
        aidPnl.setEnabled(val);
        deploymentParamsField.setEnabled(val);
        deploymentParamsLabel.setEnabled(val);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        instancesList.requestFocusInWindow();
    }

    public void removeChangeListener(ChangeListener arg0) {
        supp.removeChangeListener(arg0);
    }

    private void fireChange() {
        supp.fireChange();
    }

    public void addChangeListener(ChangeListener arg0) {
        supp.addChangeListener(arg0);
    }
    boolean inSetEntry;

    private void setEntry(DeploymentXmlAppletEntry entry) {
        inSetEntry = true;
        try {
            this.entry = entry;
            DefaultListModel mdl = new DefaultListModel();
            for (DeploymentXmlInstanceEntry e : entry.getData()) {
                mdl.addElement(e);
            }
            instancesList.setEnabled (entry != null);
            instancesList.setModel(mdl);
            if (instancesList.getSelectedValue() == null && mdl.getSize() > 0) {
                instancesList.setSelectedIndex(0);
            }
            instancesList.requestFocus();
        } finally {
            inSetEntry = false;
        }
        addButton.setEnabled (true);
    }

    public void stateChanged(ChangeEvent e) {
        change();
    }

    public void valueChanged(ListSelectionEvent e) {
        Object o = instancesList.getSelectedValue();
        if (o instanceof DeploymentXmlInstanceEntry) {
            enabled(true);
            setInstance((DeploymentXmlInstanceEntry) o);
            DefaultListModel mdl = (DefaultListModel) instancesList.getModel();
            int ix = mdl.indexOf(o);
            aidPnl.setTitle(NbBundle.getMessage(CustomizeDeploymentPanel.class,
                    "SPECIFIC_INSTANCE_AID", ix + 1)); //NOI18N
        } else {
            enabled(false);
            addButton.setEnabled(true);
        }
    }
    private DeploymentXmlInstanceEntry instance;

    boolean inSetInstance;
    private void setInstance(DeploymentXmlInstanceEntry i) {
        inSetInstance = true;
        try {
            this.instance = i;
            aidPnl.setAID(i.getInstanceAID());
            deploymentParamsField.setText(i.getDeploymentParams());
        } finally {
            inSetInstance = false;
        }
        change();
    }

    public void insertUpdate(DocumentEvent e) {
        change();
    }

    public void removeUpdate(DocumentEvent e) {
        change();
    }

    public void changedUpdate(DocumentEvent e) {
        change();
    }

    private AID findLastAID() {
        AID aid = null;
        if (instancesList.getModel().getSize() > 0) {
            Object o = instancesList.getModel().getElementAt(instancesList.getModel().getSize() - 1);
            if (o instanceof DeploymentXmlInstanceEntry) {
                aid = ((DeploymentXmlInstanceEntry) o).getInstanceAID();
            }
        }
        if (aid == null) {
            aid = appletAid;
        }
        if (aid == null) {
            //well, do something...
            aid = Utils.generateInstanceAid(classname, classname);
        }
        return aid;
    }

    private AID nextUniqueAid() {
        AID aid = findLastAID();
        Set<AID> used = new HashSet<AID>();
        if (entry != null) {
            for (DeploymentXmlInstanceEntry e : entry.getData()) {
                AID usedAid = e.getInstanceAID();
                if (usedAid != null) {
                    used.add(usedAid);
                }
            }
        }
        while (used.contains(aid)) {
            aid = aid.increment();
        }
        return aid;
    }
    private final HtmlRenderer.Renderer r = HtmlRenderer.createRenderer();

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String val;
        if (value instanceof DeploymentXmlInstanceEntry) {
            DeploymentXmlInstanceEntry e = (DeploymentXmlInstanceEntry) value;
            AID aid = e.getInstanceAID();
            String params = e.getDeploymentParams();
            StringBuilder sb = new StringBuilder("<html>"); //NOI18N
            if (aid == null) {
                sb.append("<s><font color='!nb.errorForeground'>"); //NOI18N
                sb.append(NbBundle.getMessage(AppletCustomizer.class,
                        "LBL_INVALID_AID")); //NOI18N
                sb.append("</s></font>"); //NOI18N
            } else {
                sb.append(aid.toString());
            }
            if (params != null && params.length() > 0) {
                sb.append("<font color='!controlShadow'>"); //NOI18N
                sb.append(" ("); //NOI18N
                sb.append(params);
                sb.append(')'); //NOI18N
            }
            val = sb.toString();
        } else {
            val = value == null ? "" : value.toString();
        }
        return r.getListCellRendererComponent(list, val, index, isSelected,
                cellHasFocus);
    }
}
