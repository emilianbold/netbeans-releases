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
package org.netbeans.modules.visualweb.web.ui.dt.component.vforms;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.Result;
import com.sun.rave.designtime.ext.componentgroup.ComponentGroupHolder;
import com.sun.rave.designtime.ext.componentgroup.impl.ColorWrapperImpl;
import com.sun.rave.designtime.ext.componentgroup.util.ComponentGroupHelper;
import com.sun.rave.web.ui.component.Form;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import org.netbeans.modules.visualweb.web.ui.dt.component.FormDesignInfo;


public class VirtualFormsCustomizerPanel extends JPanel {

    protected VirtualFormsCustomizer customizer;
    private ArrayList vformsList = new ArrayList();
    private FormsTableModel vformsTableModel = new FormsTableModel();
    private HashMap colorMap = new HashMap();

    public VirtualFormsCustomizerPanel(VirtualFormsCustomizer customizer) {
        this.customizer = customizer;
        initComponents();
        readVFormInfo();
    }

    public Result applyChanges() {
        // store the virtual forms config
        Form.VirtualFormDescriptor[] vforms = (Form.VirtualFormDescriptor[])
        vformsList.toArray(new Form.VirtualFormDescriptor[vformsList.size()]);
        String vfConfig = Form.generateVirtualFormsConfig(vforms);
        DesignProperty vfcProp = customizer.getDesignBean().getProperty("virtualFormsConfig"); // NOI18N
        vfcProp.setValue(vfConfig);

        // store off the form colors
        DesignContext context = customizer.getDesignBean().getDesignContext();
        for (int i = 0; vforms != null && i < vforms.length; i++) {
            String vfName = vforms[i].getName();
            String key = getColorKey(vfName);
            Color c = (Color)colorMap.get(key);
            if (c != null) {
                context.setContextData(key, new ColorWrapperImpl(c));
            }
        }

        // reset the customizer modified state
        customizer.setModified(false);
        return null;
    }

    class FormsTableModel extends AbstractTableModel {
        public FormsTableModel() {}
        public int getRowCount() {
            return vformsList.size();
        }
        public int getColumnCount() {
            return 2;
        }
        public String getColumnName(int columnIndex) {
            switch(columnIndex) {
                case 0: // color
                    return ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("colorHeader"); // NOI18N
                case 1: // virtual form name
                    return ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("nameHeader"); // NOI18N
            }
            return null;
        }
        public Class getColumnClass(int columnIndex) {
            switch(columnIndex) {
                case 0: // color
                    return Color.class;
                case 1: // virtual form name
                    return String.class;
            }
            return null;
        }
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }
        public Object getValueAt(int rowIndex, int columnIndex) {
            Form.VirtualFormDescriptor vform = (Form.VirtualFormDescriptor)vformsList.get(rowIndex);
            if (vform != null) {
                switch (columnIndex) {
                    case 0: // color
                        String vfName = vform.getName();
                        String key = getColorKey(vfName);
                        return ComponentGroupHelper.getMappedColor(key, colorMap);
                    case 1: // virtual form name
                        return vform.getName();
                }
            }
            return null;
        }
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            Form.VirtualFormDescriptor vform = (Form.VirtualFormDescriptor)vformsList.get(rowIndex);
            if (vform != null) {
                String vfName, colorKey;
                switch (columnIndex) {
                    case 0: // color
                        vfName = vform.getName();
                        colorKey = getColorKey(vfName);
                        colorMap.put(colorKey, aValue);
                        customizer.setModified(true);
                        return;
                    case 1: // virtual form name
                        String name = aValue.toString();
                        name = name.trim();
                        name = name.replaceAll("\\|", "_"); // NOI18N
                        name = name.replaceAll(",", "_"); // NOI18N
                        if (name.length() < 1) {
                            name = VirtualFormsHelper.getNewVirtualFormName(vformsList);
                        }
                        vfName = vform.getName();
                        colorKey = getColorKey(vfName);
                        Color c = (Color)colorMap.get(colorKey);
                        colorMap.remove(colorKey);
                        vform.setName(name);
                        colorKey = getColorKey(name);
                        colorMap.put(colorKey, c);
                        customizer.setModified(true);
                        return;
                }
            }
        }
    }
    
    private void readVFormInfo() {
        DesignBean formBean = customizer.getDesignBean();
        DesignContext dcontext = formBean.getDesignContext();
        ComponentGroupHolder[] holders = ComponentGroupHelper.getComponentGroupHolders(dcontext);
        ComponentGroupHelper.populateColorMap(dcontext, holders, colorMap);
        Form form = (Form)formBean.getInstance();
        Form.VirtualFormDescriptor[] vforms = form.getVirtualForms();
        for (int i = 0; vforms != null && i < vforms.length; i++) {
            Form.VirtualFormDescriptor vformCopy = new Form.VirtualFormDescriptor(vforms[i].getName());
            vformCopy.setParticipatingIds(vforms[i].getParticipatingIds());
            vformCopy.setSubmittingIds(vforms[i].getSubmittingIds());
            vformsList.add(vformCopy);
        }
        
        vformsTable.setModel(vformsTableModel);
        vformsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        TableColumn colorCol = vformsTable.getColumnModel().getColumn(0);
        colorCol.setCellRenderer(new ColorCellRenderer());
        colorCol.setCellEditor(new DefaultCellEditor(new ColorComboBox()));
        
        // Takes two click to have the list popup
        ((DefaultCellEditor)colorCol.getCellEditor()).setClickCountToStart(2);
        
        // Have the first row selected by default
        if( vformsTableModel.getRowCount() > 0 ) 
            vformsTable.changeSelection( 0, 0, false, false );
    }
    
    private String getColorKey(String vfName) {
        DesignBean formBean = customizer.getDesignBean();
        String formBeanId = formBean.getInstanceName();
        String formIdDotVfName = formBeanId + "." + vfName;
        return ComponentGroupHelper.getComponentGroupColorKey(FormDesignInfo.VIRTUAL_FORM_HOLDER_NAME, formIdDotVfName);   
    }
    
    class ColorCellRenderer extends DefaultTableCellRenderer {
        
        Color SELECTION_BACKGROUND =
            UIManager.getDefaults().getColor("TextField.selectionBackground");
    
        Color SELECTION_FOREGROUND =
            UIManager.getDefaults().getColor("TextField.selectionForeground");
    
        Color BACKGROUND =
            UIManager.getDefaults().getColor("TextField.background");
    
        Color FOREGROUND =
            UIManager.getDefaults().getColor("TextField.foreground");
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (isSelected) {
                c.setBackground(SELECTION_BACKGROUND);
                c.setForeground(SELECTION_FOREGROUND);
            }
            else {
                c.setBackground(BACKGROUND);
                c.setForeground(FOREGROUND);
            }
                
            Color color = (Color)value;
            setIcon(new ColorIcon(color));
            setText(null);
            
            return c;
        }
    }

    class ColorListRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean hasFocus) {
            
            super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
            
            Color c = (Color)value;
            setIcon(new ColorIcon(c));
            setText(null);
            
            return this;
        }
    }
    
    class ColorIcon implements Icon {
        private Color color;
        public ColorIcon(Color color) {
            this.color = color;
        }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillRect(x, y, 8, 8);
            g.setColor(Color.black);
            g.drawRect(x, y, 8, 8);
        }
        public int getIconWidth() {
            return 8;
        }
        public int getIconHeight() {
            return 8;
        }
    }
    
    class ColorComboBox extends JComboBox {
        public ColorComboBox() {
            super();
            DefaultComboBoxModel cbm = new DefaultComboBoxModel();
            for (int i = 0; i < ComponentGroupHelper.DEFAULT_COLOR_SET.length; i++) {
                Color c = ComponentGroupHelper.DEFAULT_COLOR_SET[i];
                cbm.addElement(c);
            }
            setModel(cbm);
            setRenderer(new ColorListRenderer());
            getAccessibleContext().setAccessibleName(ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("colorComboAccessibleName")); // NOI18N
            getAccessibleContext().setAccessibleDescription(ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("colorComboAccessibleDescription")); // NOI18N
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        tableLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        vformsTable = new javax.swing.JTable();

        // Only one row selectable at a time
        vformsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Create a default cell editor for String values that forces a stop
        // editing event whenever focus is lost.
        JTextField textField = new JTextField();
        final TableCellEditor cellEditor = new TextFieldCellEditor(vformsTable, textField);
        vformsTable.setDefaultEditor(String.class, cellEditor);
        vformsTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); //NOI18N

        // Single click to start editing cells with String
        ((DefaultCellEditor)vformsTable.getDefaultEditor(String.class)).setClickCountToStart( 1 );

        // Create a default cell renderer for String values that consistently renders
        // background colors.
        vformsTable.setDefaultRenderer(String.class, new HomogonousCellRenderer());

        // Stop the editing when the table lost its focus
        vformsTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        jPanel1 = new javax.swing.JPanel();
        btnNew = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        tableLabel.setLabelFor(vformsTable);
        org.openide.awt.Mnemonics.setLocalizedText(tableLabel, org.openide.util.NbBundle.getMessage(VirtualFormsCustomizerPanel.class, "customizeLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 10);
        add(tableLabel, gridBagConstraints);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/sun/rave/web/ui/component/vforms/Bundle"); // NOI18N
        tableLabel.getAccessibleContext().setAccessibleName(bundle.getString("tableLabelAccessibleName")); // NOI18N
        tableLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("tableLabelAccessibleDescription")); // NOI18N

        jScrollPane1.setBackground(java.awt.SystemColor.window);

        vformsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        vformsTable.setShowVerticalLines(false);
        jScrollPane1.setViewportView(vformsTable);
        vformsTable.getAccessibleContext().setAccessibleName(bundle.getString("vformsTableAccessibleName")); // NOI18N
        vformsTable.getAccessibleContext().setAccessibleDescription(bundle.getString("vformsTableAccessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 17, 10);
        add(jScrollPane1, gridBagConstraints);
        jScrollPane1.getAccessibleContext().setAccessibleName(bundle.getString("vformsTableAccessibleName")); // NOI18N
        jScrollPane1.getAccessibleContext().setAccessibleDescription(bundle.getString("vformsTableAccessibleDescription")); // NOI18N

        jPanel1.setLayout(new java.awt.GridLayout(2, 1, 0, 5));

        java.util.ResourceBundle bundle1 = java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle"); // NOI18N
        btnNew.setText(bundle1.getString("newButton")); // NOI18N
        btnNew.setToolTipText(bundle1.getString("NewVf")); // NOI18N
        btnNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewActionPerformed(evt);
            }
        });
        jPanel1.add(btnNew);
        btnNew.getAccessibleContext().setAccessibleName(bundle.getString("btnNewAccessibleName")); // NOI18N
        btnNew.getAccessibleContext().setAccessibleDescription(bundle.getString("btnNewAccessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btnDelete, bundle1.getString("deleteButton")); // NOI18N
        btnDelete.setToolTipText(bundle1.getString("DeleteVf")); // NOI18N
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });
        jPanel1.add(btnDelete);
        btnDelete.getAccessibleContext().setAccessibleName(bundle.getString("btnDeleteAccessibleName")); // NOI18N
        btnDelete.getAccessibleContext().setAccessibleDescription(bundle.getString("btnDeleteAccessibleDescription")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 10);
        add(jPanel1, gridBagConstraints);

        getAccessibleContext().setAccessibleName("Edit Virtual Forms dialog box");
        getAccessibleContext().setAccessibleDescription("Use this table to view and edit properties of the virutal forms defined on this page.");
    }// </editor-fold>//GEN-END:initComponents
    
    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        int vfidx = vformsTable.getSelectedRow();
        if (vfidx > -1 && vfidx < vformsList.size()) {
            Form.VirtualFormDescriptor vform = (Form.VirtualFormDescriptor)vformsList.get(vfidx);
            String title = ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("confirmDeleteTitle"); // NOI18N
            String msg = java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("confirmDeleteMessage"), new Object[] {vform.getName()});  // NOI18N
            if (JOptionPane.showConfirmDialog(this, msg, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                vformsList.remove(vfidx);
                vformsTableModel.fireTableDataChanged();
                if (vformsList.size() <= vfidx) {
                    vfidx--;
                }
                if (vfidx >= 0) {
                    vformsTable.getSelectionModel().setSelectionInterval(vfidx, vfidx);
                }
            }
        }
    }//GEN-LAST:event_btnDeleteActionPerformed
    
    private void btnNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewActionPerformed
        String name = VirtualFormsHelper.getNewVirtualFormName(vformsList);
        
        Form.VirtualFormDescriptor vform = new Form.VirtualFormDescriptor(name);
        vformsList.add(vform);
        
        vformsTableModel.fireTableDataChanged();
        vformsTable.getSelectionModel().setSelectionInterval(vformsList.size() - 1, vformsList.size() - 1);
    }//GEN-LAST:event_btnNewActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnNew;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel tableLabel;
    private javax.swing.JTable vformsTable;
    // End of variables declaration//GEN-END:variables
}
