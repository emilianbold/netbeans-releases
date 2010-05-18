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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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
package org.netbeans.modules.jmx.configwizard;

import java.awt.Component;
import java.util.Iterator;
import java.util.Vector;
import java.util.ResourceBundle;
import java.io.File;
import java.awt.Dimension;

import java.util.ArrayList;

import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;

import javax.swing.ListSelectionModel;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;
import org.openide.awt.Mnemonics;

import org.netbeans.modules.jmx.common.WizardConstants;
import org.netbeans.modules.jmx.common.GenericWizardPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JTextField;
import org.openide.util.HelpCtx;

/**
 *
 * Class handling the graphical part of the standard Options wizard panel
 *
 */
public class RMIPanel extends javax.swing.JPanel {
    
    private RMIWizardPanel wiz;
    private ResourceBundle bundle;
    private AuthTable authTable;
    private AuthTableModel tableModel;
    
    private boolean rMISelected = false;
    private boolean sslSelected = false;
    private boolean authSelected = false;
    
    /**
     * Access renderer for authenticate table.
     */
    class AccessRenderer extends  DefaultTableCellRenderer {
        private JComboBox comp;
        /**
         * Constructor of access renderer for authenticate table.
         * @param comp <CODE>JComboBox</CODE>
         */
        AccessRenderer(JComboBox comp) {
            this.comp = comp;
        }
        
	public Component getTableCellRendererComponent(JTable table,
						       Object value,
						       boolean isSelected,
						       boolean hasFocus,
						       int row,
						       int column) {
            Object obj = table.getModel().getValueAt(row,column);
            if(obj == null) 
                comp.setSelectedIndex(0);
            else
                comp.setSelectedItem(obj);
	    return comp;
	}
        
        public Component getComponent() {
	    return comp;
	}
    }
    
    /**
     * Name renderer for authenticate table.
     */
    class NameRenderer extends  DefaultTableCellRenderer {
        private JTextField comp;
        /**
         * Constructor of name cell renderer in authenticate table.
         * @param comp <CODE>JTextField</CODE>
         */
        NameRenderer(JTextField comp) {
            this.comp = comp;
        }
        
	public Component getTableCellRendererComponent(JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {
            String obj = (String) table.getModel().getValueAt(row,column);
            
            comp.setText(obj);
            
            return comp;
        }
        
        public Component getComponent() {
            return comp;
        }
    }
    
    private class ValidatorListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            wiz.event();
        }
    }
    
    private class AuthTable extends JTable {
        /** Creates a new instance of AttributeTable */
        public AuthTable(AbstractTableModel model) {
            super(model);
        }
        
        public TableCellEditor getCellEditor(int row, int column) {
            //In case we have a repaint thread that is in the process of
            //repainting an obsolete table, just ignore the call.
            //It can happen when MBean selection is switched at a very quick rate
            if(row >= getRowCount())
                return null;
            switch(column) {
                case 0: {
                    JTextField name = new JTextField();
                    name.addKeyListener(new KeyListener() {                   
                    public void keyTyped(KeyEvent e) {
                        char c = e.getKeyChar();
                        /*
                        if (!(Character.i ||
                                c == KeyEvent.VK_BACK_SPACE ||
                                c == KeyEvent.VK_DELETE)) {
                            authTable.getToolkit().beep();
                            e.consume();
                        }*/
                    }
                    
                    public void keyPressed(KeyEvent e) {
                    }
                    
                    public void keyReleased(KeyEvent e) {
                        
                    }
                });
                    name.addActionListener(new ValidatorListener());
                    String obj = (String) getModel().getValueAt(row,column);
                    name.setText(obj);
                    return new DefaultCellEditor(name);
                }
                case 1: {
                    JTextField password = new JTextField();
                    password.addActionListener(new ValidatorListener());
                    String obj = (String) getModel().getValueAt(row,column);
                    password.setText(obj);
                    return new DefaultCellEditor(password);
                }
                    
                case 2: {
                    JComboBox access = new JComboBox();
                    access.setName( "access");// NOI18N
                    access.setModel(new javax.swing.DefaultComboBoxModel(new String[] {  "readonly",  "readwrite" }));// NOI18N
                    Object obj = getModel().getValueAt(row,column);
                    if(obj == null)
                        access.setSelectedIndex(0);
                    else
                        access.setSelectedItem(obj);
                    access.setEditable(false);
                    return new DefaultCellEditor(access);
                }
            }
            
            return super.getCellEditor(row, column);
        }
        
        public TableCellRenderer getCellRenderer(int row, int column) {
            //In case we have a repaint thread that is in the process of
            //repainting an obsolete table, just ignore the call.
            //It can happen when MBean selection is switched at a very quick rate
            if(row >= getRowCount())
                return null;
            
            if (column == 2) {
                JComboBox access = new JComboBox();
                access.setModel(new javax.swing.DefaultComboBoxModel(new String[] {  "readonly",  "readwrite" }));// NOI18N
                access.setEditable(false);
                access.setSelectedIndex(0);
                return new AccessRenderer(access);
            }
            
            return super.getCellRenderer(row, column);
        }
    }
    
    private class AuthTableModel extends DefaultTableModel {
        
        private ArrayList data;
        
        /** Creates a new instance of AbstractJMXTableModel */
        public AuthTableModel() {
        }
        
        public int getSize() {
            return data.size();
        }
        
        public void addRow() {
            RMIAuthenticatedUser row = new RMIAuthenticatedUser();
            data.add(row);
            this.fireTableDataChanged();
        }
        
        public void removeRow(int i) {
            if(authTable.isEditing())
                authTable.getCellEditor().stopCellEditing();
            data.remove(i);
            this.fireTableDataChanged();
        }
        
        public int getRowCount() {
            //retourne la taille de ArrayList
            if(data == null)
                data = new ArrayList();
            
            return data.size();
        }
        
        public int getColumnCount() {
            return 3;
        }
        
        public Object getValueAt(int rowIndex, int columnIndex) {
            RMIAuthenticatedUser row = (RMIAuthenticatedUser) data.get(rowIndex);
            return row.getValueAt(columnIndex);
        }
        
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            RMIAuthenticatedUser row = (RMIAuthenticatedUser) data.get(rowIndex);
            row.setValueAt(aValue, columnIndex);
            wiz.event();
        }
        
        public boolean isCellEditable(int r, int c) {
            return true;
        }
        
        ArrayList getRawData() {
            return data;
        }
        
        public boolean isValid() {
            for(int i = 0; i < data.size(); i++) {
                RMIAuthenticatedUser row = (RMIAuthenticatedUser) data.get(i);
                if(!row.isValid())
                    return false;
            }
            return true;
        }
    }
    
    /**
     * Create the wizard panel component and set up some basic properties.
     * @param wiz <CODE>WizardDescriptor</CODE> the wizard
     */
    public RMIPanel(RMIWizardPanel wiz) {
        this.wiz = wiz;
        bundle = NbBundle.getBundle(ConfigPanel.class);
        initComponents();
        
        //Table
        final String[] columnNames = { bundle.getString("LBL_TABLE_ROLE"),// NOI18N
                                       bundle.getString("LBL_TABLE_PASSWORD"),// NOI18N
                                       bundle.getString("LBL_TABLE_ACCESS") };// NOI18N
        tableModel = new AuthTableModel();
        authTable = new AuthTable(tableModel);
        authTable.setName("authTable");// NOI18N
        authTable.setPreferredSize(new Dimension(400, 100));
        authTable.setRowSelectionAllowed(true);
        authTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        authTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane jscp = new JScrollPane(authTable);
        jscp.setPreferredSize(new Dimension(400,100));
        java.awt.GridBagConstraints tablegridBagConstraints = new java.awt.GridBagConstraints();
        tablegridBagConstraints.gridx = 0;
        tablegridBagConstraints.gridy = 4;
        tablegridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        tablegridBagConstraints.gridheight = 1;
        
        tablegridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        tablegridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        
        tablegridBagConstraints.weightx = 1.0;
        tablegridBagConstraints.weighty = 1.0;
        tablegridBagConstraints.insets = new java.awt.Insets(0,30,0,0);
        jPanel2.add(jscp,tablegridBagConstraints);
        ((DefaultTableModel)authTable.getModel()).setColumnIdentifiers(columnNames);
        //AddAuthTableListener listener = new AddAuthTableListener(tableModel, removeAuth);
        //addAuth.addActionListener(listener);
        
        Mnemonics.setLocalizedText(tableNameJLabel, bundle.getString("LBL_Authenticate_Table_Name"));// NOI18N
        Mnemonics.setLocalizedText(addAuth, bundle.getString("LBL_RMI_ADD_AUTH"));// NOI18N
        Mnemonics.setLocalizedText(removeAuth, bundle.getString("LBL_RMI_REMOVE_AUTH"));// NOI18N
        
        removeAuth.setEnabled(false);
        addAuth.setEnabled(false);
        tableNameJLabel.setEnabled(false);
        
        Mnemonics.setLocalizedText(rMIJCheckBox,
                bundle.getString("LBL_RMI"));//NOI18N
        Mnemonics.setLocalizedText(rMIPortJLabel,
                bundle.getString("LBL_RMI_Port"));//NOI18N
        Mnemonics.setLocalizedText(sslJCheckBox,
                bundle.getString("LBL_SSL"));//NOI18N
        Mnemonics.setLocalizedText(sslClientAuthJCheckBox,
                bundle.getString("LBL_SSL_Required_Auth"));//NOI18N
        Mnemonics.setLocalizedText(sslCipherJLabel,
                bundle.getString("LBL_SSL_Cipher"));//NOI18N
        Mnemonics.setLocalizedText(sslProtocolJLabel,
                bundle.getString("LBL_SSL_Protocol_Version"));//NOI18N
        Mnemonics.setLocalizedText(authJCheckBox,
                bundle.getString("LBL_Authenticate"));//NOI18N
        //Mnemonics.setLocalizedText(authJButton,
        //                         bundle.getString("LBL_Authenticate_Edit"));//NOI18N
        
        //Set tooltips
        
        rMIJCheckBox.setToolTipText(bundle.getString("TLTP_RMI"));//NOI18N
        rMIPortJLabel.setToolTipText(bundle.getString("TLTP_RMI_PORT"));//NOI18N
        sslJCheckBox.setToolTipText(bundle.getString("TLTP_SSL"));//NOI18N
        sslClientAuthJCheckBox.setToolTipText(bundle.getString("TLTP_SSL_Client_Auth"));//NOI18N
        sslCipherJLabel.setToolTipText(bundle.getString("TLTP_SSL_Cipher_Suite"));//NOI18N
        sslProtocolJLabel.setToolTipText(bundle.getString("TLTP_SSL_Protocol"));//NOI18N
        authJCheckBox.setToolTipText(bundle.getString("TLTP_RMI_Auth"));//NOI18N
        
        // init flags
        rMIJCheckBox.setSelected(true);
        updateSelected();
        setRMIPanelEnabled(true);
        setSSLPanelEnabled(false);
        updateSelected();
        rMIPortJTextField.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!(Character.isDigit(c) ||
                        c == KeyEvent.VK_BACK_SPACE ||
                        c == KeyEvent.VK_DELETE)) {
                    authTable.getToolkit().beep();
                    e.consume();
                }
            }
            
            public void keyPressed(KeyEvent e) {
            }
            
            public void keyReleased(KeyEvent e) {
                
            }
        });
        
        // Provide a name in the title bar.
        //setName(NbBundle.getMessage(ConfigPanel.class,
          //       "LBL_RMI_Panel"));
        setName(bundle.getString("ACCESS_RMI_ENABLE_RMI")); // NOI18N
        
         //Accessibility
        rMIJCheckBox.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_RMI_ENABLE_RMI")); // NOI18N
        rMIJCheckBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_RMI_ENABLE_RMI_DESCRIPTION"));// NOI18N
        
        rMIPortJTextField.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_RMI_PORT")); // NOI18N
        rMIPortJTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_RMI_PORT_DESCRIPTION"));// NOI18N
        
        rMIPortJLabel.setLabelFor(rMIPortJTextField);
        
        authJCheckBox.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_RMI_AUTHENTICATION")); // NOI18N
        authJCheckBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_RMI_AUTHENTICATION_DESCRIPTION")); // NOI18N
        
        authTable.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_RMI_AUTHENTICATION_TABLE")); // NOI18N
        authTable.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_RMI_AUTHENTICATION_TABLE_DESCRIPTION")); // NOI18N
        
        tableNameJLabel.setLabelFor(authTable);
        
        addAuth.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_RMI_AUTHENTICATION_ADD")); // NOI18N
        addAuth.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_RMI_AUTHENTICATION_ADD_DESCRIPTION")); // NOI18N
        
        removeAuth.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_RMI_AUTHENTICATION_REMOVE")); // NOI18N
        removeAuth.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_RMI_AUTHENTICATION_REMOVE_DESCRIPTION")); // NOI18N
        
        sslJCheckBox.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_RMI_SSL")); // NOI18N
        sslJCheckBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_RMI_SSL_DESCRIPTION")); // NOI18N
        
        sslProtocolJTextField.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_RMI_SSL_PROTOCOL")); // NOI18N
        sslProtocolJTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_RMI_SSL_PROTOCOL_DESCRIPTION")); // NOI18N
        
        sslProtocolJLabel.setLabelFor(sslProtocolJTextField);
        
        sslCipherJTextField.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_RMI_SSL_CIPHER")); // NOI18N
        sslCipherJTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_RMI_SSL_CIPHER_DESCRIPTION")); // NOI18N
        
        sslCipherJLabel.setLabelFor(sslCipherJTextField);
        
        sslClientAuthJCheckBox.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_RMI_SSL_CLIENT")); // NOI18N
        sslClientAuthJCheckBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_RMI_SSL_CLIENT_DESCRIPTION")); // NOI18N
        
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_PANEL"));// NOI18N
    }
    
    /*
     * update all the selected flags
     */
    private void updateSelected() {
        sslSelected = sslJCheckBox.isSelected();
        rMISelected = rMIJCheckBox.isSelected();
        authSelected = authJCheckBox.isSelected();
    }
    
    /*
     * calls setEnabled(enable) method of all components included in SSLPanel
     */
    private void setSSLPanelEnabled(boolean enable) {
        Vector<JComponent> jcVector = new Vector<JComponent>();
        jcVector.add(sslClientAuthJCheckBox);
        jcVector.add(sslProtocolJLabel);
        jcVector.add(sslProtocolJTextField);
        jcVector.add(sslCipherJLabel);
        jcVector.add(sslCipherJTextField);
        for (Iterator<JComponent> it = jcVector.iterator();it.hasNext();) {
            JComponent jc = it.next();
            jc.setEnabled(enable);
        }
    }
    
    /*
     * calls setEnabled(enable) method of all components included in RMIPanel
     */
    private void setRMIPanelEnabled(boolean enable) {
        Vector<JComponent> jcVector = new Vector<JComponent>();
        jcVector.add(rMIPortJLabel);
        jcVector.add(rMIPortJTextField);
        jcVector.add(authJCheckBox);
        jcVector.add(sslJCheckBox);
        for (Iterator<JComponent> it = jcVector.iterator();it.hasNext();) {
            JComponent jc = it.next();
            jc.setEnabled(enable);
        }
        if (enable && sslSelected) {
            setSSLPanelEnabled(true);
        } else {
            setSSLPanelEnabled(false);
        }
        updateSelected();
        
        if (enable) {
            authTable.setEnabled(authSelected);
            addAuth.setEnabled(authSelected);
            removeAuth.setEnabled(tableModel.getSize() >0);
            tableNameJLabel.setEnabled(authSelected);
        } else {
            authTable.setEnabled(false);
            addAuth.setEnabled(false);
            removeAuth.setEnabled(false);
            tableNameJLabel.setEnabled(false);
        }
        
    }
    
    private void checkPortValue(javax.swing.JTextField jt) {
        if (Integer.getInteger(jt.getText()) > 65536) {
            jt.setText(new Integer(65536).toString());
        } else if (Integer.getInteger(jt.getText()) < 0) {
            jt.setText(new Integer(0).toString());
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

        managementFilesGroup = new javax.swing.ButtonGroup();
        jTable1 = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        rMIPortJLabel = new javax.swing.JLabel();
        rMIPortJTextField = new javax.swing.JTextField();
        rMIJCheckBox = new javax.swing.JCheckBox();
        authJCheckBox = new javax.swing.JCheckBox();
        sslJCheckBox = new javax.swing.JCheckBox();
        tableNameJLabel = new javax.swing.JLabel();
        sslProtocolJLabel = new javax.swing.JLabel();
        sslProtocolJTextField = new javax.swing.JTextField();
        sslCipherJLabel = new javax.swing.JLabel();
        sslCipherJTextField = new javax.swing.JTextField();
        sslClientAuthJCheckBox = new javax.swing.JCheckBox();
        addAuth = new javax.swing.JButton();
        removeAuth = new javax.swing.JButton();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));

        setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.GridBagLayout());

        rMIPortJLabel.setName("rMIPortJLabel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 0, 0);
        jPanel2.add(rMIPortJLabel, gridBagConstraints);

        rMIPortJTextField.setMinimumSize(new java.awt.Dimension(55, 20));
        rMIPortJTextField.setName("rMIPortJTextField");
        rMIPortJTextField.setPreferredSize(new java.awt.Dimension(55, 20));
        rMIPortJTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rMIPortJTextFieldActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        jPanel2.add(rMIPortJTextField, gridBagConstraints);

        rMIJCheckBox.setName("rMIJCheckBox");
        rMIJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rMIJCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel2.add(rMIJCheckBox, gridBagConstraints);

        authJCheckBox.setName("authJCheckBox");
        authJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                authJCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 13, 0, 0);
        jPanel2.add(authJCheckBox, gridBagConstraints);

        sslJCheckBox.setName("sslJCheckBox");
        sslJCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sslJCheckBoxActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 13, 0, 0);
        jPanel2.add(sslJCheckBox, gridBagConstraints);

        tableNameJLabel.setName("tableNameJLabel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 30, 5, 0);
        jPanel2.add(tableNameJLabel, gridBagConstraints);

        sslProtocolJLabel.setName("sslProtocolJLabel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 30, 0, 0);
        jPanel2.add(sslProtocolJLabel, gridBagConstraints);

        sslProtocolJTextField.setMinimumSize(new java.awt.Dimension(50, 20));
        sslProtocolJTextField.setName("sslProtocolJTextField");
        sslProtocolJTextField.setPreferredSize(new java.awt.Dimension(65, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        jPanel2.add(sslProtocolJTextField, gridBagConstraints);

        sslCipherJLabel.setName("sslCipherJLabel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 30, 0, 0);
        jPanel2.add(sslCipherJLabel, gridBagConstraints);

        sslCipherJTextField.setMinimumSize(new java.awt.Dimension(50, 20));
        sslCipherJTextField.setName("sslCipherJTextField");
        sslCipherJTextField.setPreferredSize(new java.awt.Dimension(65, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        jPanel2.add(sslCipherJTextField, gridBagConstraints);

        sslClientAuthJCheckBox.setName("sslClientAuthJCheckBox");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 28, 0, 0);
        jPanel2.add(sslClientAuthJCheckBox, gridBagConstraints);

        addAuth.setName("addAuth");
        addAuth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addAuthActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 30, 0, 5);
        jPanel2.add(addAuth, gridBagConstraints);

        removeAuth.setName("removeAuth");
        removeAuth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAuthActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 5);
        jPanel2.add(removeAuth, gridBagConstraints);

        add(jPanel2, java.awt.BorderLayout.NORTH);

    }
    // </editor-fold>//GEN-END:initComponents

    private void removeAuthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAuthActionPerformed
        final int row = authTable.getSelectedRow();
        if (row == -1) return;
        
        tableModel.removeRow(row);
        removeAuth.setEnabled(tableModel.getSize() > 0);
        if(tableModel.getSize() > 0) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if(row == 0)
                        authTable.setRowSelectionInterval(0,0);
                    else
                        if(row == tableModel.getSize())
                            authTable.setRowSelectionInterval(row -1, row -1);
                        else
                            authTable.setRowSelectionInterval(row, row);
                }
            });
        }
        wiz.event();
    }//GEN-LAST:event_removeAuthActionPerformed

    private void addAuthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAuthActionPerformed
        tableModel.addRow();
        removeAuth.setEnabled(tableModel.getSize() > 0);
        wiz.event();
    }//GEN-LAST:event_addAuthActionPerformed

    private void rMIPortJTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rMIPortJTextFieldActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_rMIPortJTextFieldActionPerformed

    private void authJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_authJCheckBoxActionPerformed
        updateSelected();
        authTable.setEnabled(authSelected);
        addAuth.setEnabled(authSelected);
        removeAuth.setEnabled(authSelected && tableModel.getSize() > 0);
        tableNameJLabel.setEnabled(authSelected);
        wiz.event();
    }//GEN-LAST:event_authJCheckBoxActionPerformed

    private void sslJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sslJCheckBoxActionPerformed
        updateSelected();
        if (rMISelected) {
            setSSLPanelEnabled(sslSelected);
        } else {
            setSSLPanelEnabled(false);
        }
        //wiz.fireChangeEvent (); 
    }//GEN-LAST:event_sslJCheckBoxActionPerformed

    private void rMIJCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rMIJCheckBoxActionPerformed
        updateSelected();
        setRMIPanelEnabled(rMISelected);
        //wiz.fireChangeEvent ();
    }//GEN-LAST:event_rMIJCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addAuth;
    private javax.swing.JCheckBox authJCheckBox;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTable jTable1;
    private javax.swing.ButtonGroup managementFilesGroup;
    private javax.swing.JCheckBox rMIJCheckBox;
    private javax.swing.JLabel rMIPortJLabel;
    private javax.swing.JTextField rMIPortJTextField;
    private javax.swing.JButton removeAuth;
    private javax.swing.JLabel sslCipherJLabel;
    private javax.swing.JTextField sslCipherJTextField;
    private javax.swing.JCheckBox sslClientAuthJCheckBox;
    private javax.swing.JCheckBox sslJCheckBox;
    private javax.swing.JLabel sslProtocolJLabel;
    private javax.swing.JTextField sslProtocolJTextField;
    private javax.swing.JLabel tableNameJLabel;
    // End of variables declaration//GEN-END:variables
    
    /**
     *
     * Class handling the standard RMI wizard panel
     *
     */
    public static class RMIWizardPanel extends GenericWizardPanel 
            implements org.openide.WizardDescriptor.FinishablePanel
    {    
        private RMIPanel panel = null;
        private String projectLocation   = null;
        private WizardDescriptor wiz;
        
        public boolean isValid () {
            boolean val = true;
            if(getPanel().authSelected)
                val = getPanel().tableModel.isValid();
            
            String msg =  "";// NOI18N
            if(!val)
                msg = getPanel().bundle.getString("LBL_State_Invalid_User");// NOI18N
            
            setErrorMsg(msg);
            return val;
        }
        
        /**
         * Displays the given message in the wizard's message area.
         *
         * @param  message  message to be displayed, or <code>null</code>
         *                  if the message area should be cleared
         */
        private void setErrorMsg(String message) {
            if (wiz != null) {
                wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message); 
            }
        }
        
        /**
         * Fire a change event (designed to be call from out of this class).
         */
        public void event() {
            fireChangeEvent();
        }
        
        public Component getComponent () { return getPanel(); }
        
        private RMIPanel getPanel()  
        {
            if (panel == null) {
                panel = new RMIPanel(this);
            }
            return panel;
        }
        
        public boolean isFinishPanel() { return false;}

        //=====================================================================
        // Called to read information from the wizard map in order to populate
        // the GUI correctly.
        //=====================================================================
        public void readSettings (Object settings) 
        {
            wiz = (WizardDescriptor) settings;
            
            // store project location to detect existing mbeans 
            String location = (String)wiz.getProperty(WizardConstants.PROP_PROJECT_LOCATION);
            projectLocation = location + File.separatorChar + WizardConstants.SRC_DIR; 
            
            getPanel().rMIPortJTextField.setText(
                    wiz.getProperty(WizardConstants.RMI_PORT).toString());
        }

        //=====================================================================
        // Called to store information from the GUI into the wizard map.
        //=====================================================================
        public void storeSettings (Object settings) 
        { 
            getPanel().updateSelected();
            wiz = (WizardDescriptor) settings;
            wiz.putProperty(WizardConstants.RMI_SELECTED, 
                    new Boolean(getPanel().rMISelected));
            wiz.putProperty(WizardConstants.RMI_AUTHENTICATE, 
                    new Boolean(getPanel().authSelected));
            wiz.putProperty(WizardConstants.RMI_AUTHENTICATED_USERS, 
                    getPanel().tableModel.getRawData());
            wiz.putProperty(WizardConstants.RMI_PORT, 
                    Integer.valueOf(getPanel().rMIPortJTextField.getText()));
            wiz.putProperty(WizardConstants.SSL_SELECTED, 
                    getPanel().sslSelected);
            wiz.putProperty(WizardConstants.RMI_SSL_PROTOCOLS, 
                    getPanel().sslProtocolJTextField.getText());
            wiz.putProperty(WizardConstants.RMI_SSL_TLS_CIPHER, 
                    getPanel().sslCipherJTextField.getText());
            Boolean sslClientAuth = 
                    getPanel().sslClientAuthJCheckBox.isSelected();
            wiz.putProperty(WizardConstants.RMI_SSL_CLIENT_AUTHENTICATE, 
                    new Boolean(sslClientAuth));
        }
        public HelpCtx getHelp() {
           return new HelpCtx( "mgt_properties");  // NOI18N
        }   
    }
}
