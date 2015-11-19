/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.docker.ui.run;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.MouseEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import org.netbeans.modules.docker.DockerImageInfo;
import org.netbeans.modules.docker.DockerTag;
import org.netbeans.modules.docker.NetworkPort;
import org.netbeans.modules.docker.NetworkPort.Type;
import org.netbeans.modules.docker.remote.DockerException;
import org.netbeans.modules.docker.remote.DockerRemote;
import org.netbeans.modules.docker.ui.UiUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
public class RunNetworkVisual extends javax.swing.JPanel {

    private final List<NetworkPort> exposed = new ArrayList<>();

    private final List<PortMapping> mapping = new ArrayList<>();
    /**
     * Creates new form RunNetworkVisual
     */
    public RunNetworkVisual(DockerTag tag) {
        initComponents();
        DockerRemote r = new DockerRemote(tag.getImage().getInstance());
        DockerImageInfo info;
        try {
            info = r.getInfo(tag.getImage());
            exposed.addAll(info.getExposedPorts());
        } catch (DockerException ex) {
        }

        addExposedButton.setEnabled(!exposed.isEmpty());
        portMappingTable.setModel(new PortMappingModel(mapping));
        JComboBox typeCombo = new JComboBox(NetworkPort.Type.values());
        portMappingTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(typeCombo));
        JComboBox adressCombo = new JComboBox();
        for (String addr : UiUtils.getAddresses(false, false)) {
            adressCombo.addItem(addr);
        }
        try {
            adressCombo.insertItemAt(InetAddress.getByAddress(new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0}).getHostAddress(), 0);
        } catch (UnknownHostException ex) {
            Exceptions.printStackTrace(ex);
        }
        portMappingTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(adressCombo));
        portMappingTable.getColumnModel().getColumn(0).setPreferredWidth(portMappingTable.getColumnModel().getColumn(0).getPreferredWidth() / 2);
//        portMappingTable.getColumnModel().getColumn(1).setPreferredWidth(portMappingTable.getPreferredSize().width / 6);
//        portMappingTable.getColumnModel().getColumn(2).setPreferredWidth(portMappingTable.getPreferredSize().width / 6);
        portMappingTable.getColumnModel().getColumn(3).setPreferredWidth(2 * portMappingTable.getPreferredSize().width / 3);
        portMappingTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    }

    public List<PortMapping> getPortMapping() {
        return mapping;
    }
    
    @NbBundle.Messages("LBL_RunNetwork=Network")
    @Override
    public String getName() {
        return Bundle.LBL_RunNetwork();
    }

    private static final class PortMappingModel extends AbstractTableModel {

        private final List<PortMapping> mappings;

        public PortMappingModel(List<PortMapping> mappings) {
            this.mappings = mappings;
        }

        @Override
        public int getRowCount() {
            return mappings.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            PortMapping single = mappings.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return single.getType();
                case 1:
                    return single.getPort();
                case 2:
                    return single.getHostPort();
                case 3:
                    return single.getHostAddress();
                default:
                    throw new IllegalStateException("Unknown column index: " + columnIndex);
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (rowIndex > mappings.size() - 1 || rowIndex < 0) {
                return;
            }
            PortMapping single = mappings.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    mappings.set(rowIndex, new PortMapping(
                            NetworkPort.Type.valueOf(aValue.toString()),
                            single.getPort(),
                            single.getHostPort(),
                            single.getHostAddress()));
                    break;
                case 1:
                    Integer val1 = null;
                    if (aValue != null) {
                        String str = aValue.toString();
                        if (!str.isEmpty()) {
                            val1 = Integer.parseInt(str);
                        }
                    }
                    mappings.set(rowIndex, new PortMapping(
                            single.getType(),
                            val1,
                            single.getHostPort(),
                            single.getHostAddress()));
                    break;
                case 2:
                    Integer val2 = null;
                    if (aValue != null) {
                        String str = aValue.toString();
                        if (!str.isEmpty()) {
                            val2 = Integer.parseInt(str);
                        }
                    }
                    mappings.set(rowIndex, new PortMapping(
                            single.getType(),
                            single.getPort(),
                            val2,
                            single.getHostAddress()));
                    break;
                case 3:
                    mappings.set(rowIndex, new PortMapping(
                            single.getType(),
                            single.getPort(),
                            single.getHostPort(),
                            aValue != null ? aValue.toString() : null));
                    break;
                default:
                    throw new IllegalStateException("Unknown column index: " + columnIndex);
            }
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        @NbBundle.Messages({
            "LBL_PortMappingType=Type",
            "LBL_PortMappingPort=Port",
            "LBL_PortMappingTargetPort=Host Port",
            "LBL_PortMappingTargetAddres=Host Address",
        })
        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return Bundle.LBL_PortMappingType();
                case 1:
                    return Bundle.LBL_PortMappingPort();
                case 2:
                    return Bundle.LBL_PortMappingTargetPort();
                case 3:
                    return Bundle.LBL_PortMappingTargetAddres();
            }
            throw new IllegalStateException("Unknown column index: " + columnIndex);
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return String.class;
                case 1:
                    return Integer.class;
                case 2:
                    return Integer.class;
                case 3:
                    return String.class;
            }
            throw new IllegalStateException("Unknown column index: " + columnIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        public void fireMappingsChange() {
            assert EventQueue.isDispatchThread();
            fireTableDataChanged();
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        networkDisabledCheckBox = new javax.swing.JCheckBox();
        portMappingLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        portMappingTable = new javax.swing.JTable();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        addExposedButton = new javax.swing.JButton();
        randomBindCheckBox = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(networkDisabledCheckBox, org.openide.util.NbBundle.getMessage(RunNetworkVisual.class, "RunNetworkVisual.networkDisabledCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(portMappingLabel, org.openide.util.NbBundle.getMessage(RunNetworkVisual.class, "RunNetworkVisual.portMappingLabel.text")); // NOI18N

        jScrollPane1.setViewportView(portMappingTable);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(RunNetworkVisual.class, "RunNetworkVisual.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(RunNetworkVisual.class, "RunNetworkVisual.removeButton.text")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(addExposedButton, org.openide.util.NbBundle.getMessage(RunNetworkVisual.class, "RunNetworkVisual.addExposedButton.text")); // NOI18N
        addExposedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addExposedButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(randomBindCheckBox, org.openide.util.NbBundle.getMessage(RunNetworkVisual.class, "RunNetworkVisual.randomBindCheckBox.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(networkDisabledCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(portMappingLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(addExposedButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(addButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(removeButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addComponent(randomBindCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(networkDisabledCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(randomBindCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(portMappingLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addExposedButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton)
                        .addContainerGap(64, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addExposedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addExposedButtonActionPerformed
        for (NetworkPort p : exposed) {
            mapping.add(new PortMapping(p.getType(), p.getPort(), null, null));
        }
        ((PortMappingModel) portMappingTable.getModel()).fireMappingsChange();
    }//GEN-LAST:event_addExposedButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        mapping.add(new PortMapping(Type.TCP, null, null, null));
        ((PortMappingModel) portMappingTable.getModel()).fireMappingsChange();
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int[] selectedRows = portMappingTable.getSelectedRows();
        for (int i = selectedRows.length - 1; i >= 0; --i) {
            mapping.remove(selectedRows[i]);
        }
        ((PortMappingModel) portMappingTable.getModel()).fireMappingsChange();
    }//GEN-LAST:event_removeButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton addExposedButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JCheckBox networkDisabledCheckBox;
    private javax.swing.JLabel portMappingLabel;
    private javax.swing.JTable portMappingTable;
    private javax.swing.JCheckBox randomBindCheckBox;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
}
