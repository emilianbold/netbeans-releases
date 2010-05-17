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
/*
 * ComponentsPanel.java
 *
 * Created on March 8, 2005, 11:41 AM
 */

package org.netbeans.modules.visualweb.complib.ui;

import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.netbeans.modules.visualweb.complib.Complib;
import org.netbeans.modules.visualweb.complib.ComplibServiceProvider;
import org.netbeans.modules.visualweb.complib.IdeUtil;
import org.netbeans.modules.visualweb.complib.ComplibServiceProvider.ComponentInfo;
import org.netbeans.modules.visualweb.complib.api.ComplibException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author jhoff
 */
public class ComponentsPanel extends javax.swing.JPanel {

    private Complib complib;

    /**
     * Encapsulates info for IconCellRenderer
     *
     * @author Edwin Goei
     */
    private static class IconCellInfo {

        private Icon icon;

        private String displayName;

        /**
         * @param icon
         * @param displayName
         */
        private IconCellInfo(Icon icon, String displayName) {
            this.icon = icon;
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Icon getIcon() {
            return icon;
        }

        public String toString() {
            return displayName;
        }
    }

    /**
     * Custom TableModel
     *
     * @author Edwin Goei
     */
    private static class ComponentTableModel extends AbstractTableModel {
        // Init the column header labels
        private static ResourceBundle rb = NbBundle
                .getBundle(ComponentTableModel.class);

        private static final String[] columnNames = {
                rb.getString("manager.componentTable.displayName"),
                rb.getString("manager.componentTable.categories"),
                rb.getString("manager.componentTable.className") };

        private static Class[] types = new Class[] { IconCellInfo.class,
                Set.class, java.lang.String.class };

        private ComponentInfo[] compInfos;

        private ComponentTableModel(ComponentInfo[] compInfos) {
            this.compInfos = compInfos;
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public Class<?> getColumnClass(int columnIndex) {
            return types[columnIndex];
        }

        public String getColumnName(int column) {
            return columnNames[column];
        }

        public int getRowCount() {
            return compInfos.length;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            ComponentInfo compInfo = compInfos[rowIndex];

            switch (columnIndex) {
            case 0:
                return new IconCellInfo(compInfo.getIcon(), compInfo
                        .getDisplayName());
            case 1:
                return compInfo.getInitialCategories();
            case 2:
                return compInfo.getClassName();
            default:
                return "unknown column index"; // NOI18N
            }
        }
    }

    /**
     * Creates new form ComponentsPanel
     *
     * @param complib
     */
    public ComponentsPanel(Complib complib) {
        initComponents();

        this.complib = complib;

        try {
            initComponentListJtable();
        } catch (ComplibException e) {
            IdeUtil.logError(e);
            // TODO handle error here instead
            return;
        }
    }

    /**
     * Update the JTable model from the complib. Note that the JTable should
     * already exist.
     * 
     * @throws ComplibException
     */
    private void initComponentListJtable()
            throws ComplibException {
        // Update component list
        ComponentInfo[] compInfos = mgr.getComponentInfos(complib);

        // Handle sorted columns
        ComponentTableModel ctm = new ComponentTableModel(compInfos);
        TableSorter tableSorter = new TableSorter(ctm);
        tblCompList.setModel(tableSorter);
        tableSorter.setTableHeader(tblCompList.getTableHeader());

        // Apparently, changing the TableModel requires updating the ColumnModel
        // too
        TableColumnModel tcm = tblCompList.getColumnModel();

        // Icon and name
        tcm.getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            protected void setValue(Object value) {
                ComponentsPanel.IconCellInfo iconCellInfo = (IconCellInfo) value;
                if (iconCellInfo == null) {
                    return;
                }
                setHorizontalAlignment(SwingConstants.LEFT);
                setIcon(iconCellInfo.getIcon());
                setText(iconCellInfo.getDisplayName());
            }
        });
        tcm.getColumn(0).setPreferredWidth(170);

        // Categories
        tcm.getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            protected void setValue(Object value) {
                // Render set of categories as a list of comma separated values
                Set categories = (Set) value;
                if (categories == null) {
                    return;
                }

                StringBuffer buf = new StringBuffer();
                Iterator iter = categories.iterator();
                boolean hasNext = iter.hasNext();
                while (hasNext) {
                    String category = (String) iter.next();
                    buf.append(category);
                    hasNext = iter.hasNext();
                    if (hasNext) {
                        buf.append(", ");
                    }
                }

                setText(buf.toString());
            }

        });
        tcm.getColumn(1).setPreferredWidth(170);

        // Class name
        tcm.getColumn(2).setPreferredWidth(260);

        tblCompList.setRowHeight(18);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblCompList = new javax.swing.JLabel();
        scrollCompList = new javax.swing.JScrollPane();
        tblCompList = new javax.swing.JTable();
        btnDefaultPaletteSettings = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        lblCompList.setLabelFor(tblCompList);
        org.openide.awt.Mnemonics.setLocalizedText(lblCompList, org.openide.util.NbBundle.getBundle("org/netbeans/modules/visualweb/complib/ui/Bundle").getString("manager.ComponentList"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        add(lblCompList, gridBagConstraints);
        lblCompList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentsPanel.class, "manager.ComponentListA11yDescription"));

        tblCompList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "On Palette", "Name", "Category", "Class Name"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblCompList.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tblCompList.setIntercellSpacing(new java.awt.Dimension(2, 2));
        tblCompList.setShowVerticalLines(false);
        scrollCompList.setViewportView(tblCompList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 10, 10, 10);
        add(scrollCompList, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(btnDefaultPaletteSettings, org.openide.util.NbBundle.getMessage(ComponentsPanel.class, "manager.DefaultPaletteSettingsButton"));
        btnDefaultPaletteSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDefaultPaletteSettingsActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 10);
        add(btnDefaultPaletteSettings, gridBagConstraints);
        btnDefaultPaletteSettings.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ComponentsPanel.class, "manager.DefaultPaletteSettingsButtonA11yDescription"));

    }// </editor-fold>//GEN-END:initComponents

    private void btnDefaultPaletteSettingsActionPerformed(
        java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnDefaultPaletteSettingsActionPerformed
        String message = NbBundle.getMessage(ComponentsPanel.class,
                "manager.DefaultPaletteSettingsMessage"); // NOI18N
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(message,
                NotifyDescriptor.OK_CANCEL_OPTION);
        Object result = DialogDisplayer.getDefault().notify(nd);
        if (NotifyDescriptor.OK_OPTION == result) {
            try {
                mgr.resetToInitialPalette(complib);

                // Update the component list also
                initComponentListJtable();
            } catch (ComplibException e) {
                IdeUtil.logError(e);
            }
        }
    }// GEN-LAST:event_btnDefaultPaletteSettingsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDefaultPaletteSettings;
    private javax.swing.JLabel lblCompList;
    private javax.swing.JScrollPane scrollCompList;
    private javax.swing.JTable tblCompList;
    // End of variables declaration//GEN-END:variables
    private ComplibServiceProvider mgr = ComplibServiceProvider.getInstance();
}
