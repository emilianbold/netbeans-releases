/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.web.ui.dt.component.vforms;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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
import com.sun.rave.web.ui.component.Form;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;


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
            Color c = (Color)colorMap.get(vforms[i].getName());
            if (c != null) {
                context.setContextData(
                        VirtualFormsHelper.VFORMS_COLOR_KEY_PREFIX + vforms[i].getName(),
                        new VirtualFormsHelper.FormColor(c));
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
                        return VirtualFormsHelper.getFormColor(vform.getName(), colorMap);
                    case 1: // virtual form name
                        return vform.getName();
                }
            }
            return null;
        }
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            Form.VirtualFormDescriptor vform = (Form.VirtualFormDescriptor)vformsList.get(rowIndex);
            if (vform != null) {
                switch (columnIndex) {
                    case 0: // color
                        colorMap.put(vform.getName(), aValue);
                        customizer.setModified(true);
                        return;
                    case 1: // virtual form name
                        String name = aValue.toString();
                        name = name.trim();
                        name = name.replaceAll("\\|", "_"); // NOI18N
                        name = name.replaceAll(",", "_"); // NOI18N
                        if (name.length() < 1) name = VirtualFormsHelper.getNewVirtualFormName(vformsList);
                        Color c = (Color)colorMap.get(vform.getName());
                        colorMap.remove(vform.getName());
                        vform.setName(name);
                        colorMap.put(vform.getName(), c);
                        customizer.setModified(true);
                        return;
                }
            }
        }
    }
    
    private void readVFormInfo() {
        DesignBean formBean = customizer.getDesignBean();
        VirtualFormsHelper.fillColorMap(formBean, colorMap);
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
            for (int i = 0; i < VirtualFormsHelper.VFORM_DEFAULT_COLOR_SET.length; i++) {
                Color c = VirtualFormsHelper.VFORM_DEFAULT_COLOR_SET[i];
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
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

        getAccessibleContext().setAccessibleName("Edit Virtual Forms dialog box");
        getAccessibleContext().setAccessibleDescription("Use this table to view and edit properties of the virutal forms defined on this page.");
        tableLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("labelMnemonic").charAt(0));
        tableLabel.setLabelFor(vformsTable);
        tableLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("customizeLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 5, 10);
        add(tableLabel, gridBagConstraints);
        tableLabel.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("tableLabelAccessibleName"));
        tableLabel.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("tableLabelAccessibleDescription"));

        jScrollPane1.setBackground(java.awt.SystemColor.window);
        vformsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        vformsTable.setShowVerticalLines(false);
        jScrollPane1.setViewportView(vformsTable);
        vformsTable.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("vformsTableAccessibleName"));
        vformsTable.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("vformsTableAccessibleDescription"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 17, 10);
        add(jScrollPane1, gridBagConstraints);
        jScrollPane1.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("vformsTableAccessibleName"));
        jScrollPane1.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("vformsTableAccessibleDescription"));

        jPanel1.setLayout(new java.awt.GridLayout(2, 1, 0, 5));

        btnNew.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("newButtonMnemonic").charAt(0));
        btnNew.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("newButton"));
        btnNew.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("NewVf"));
        btnNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewActionPerformed(evt);
            }
        });

        jPanel1.add(btnNew);
        btnNew.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("btnNewAccessibleName"));
        btnNew.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("btnNewAccessibleDescription"));

        btnDelete.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("deleteButtonMnemonic").charAt(0));
        btnDelete.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("deleteButton"));
        btnDelete.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("DeleteVf"));
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        jPanel1.add(btnDelete);
        btnDelete.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("btnDeleteAccessibleName"));
        btnDelete.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/visualweb/web/ui/dt/component/vforms/Bundle").getString("btnDeleteAccessibleDescription"));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 10);
        add(jPanel1, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
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
