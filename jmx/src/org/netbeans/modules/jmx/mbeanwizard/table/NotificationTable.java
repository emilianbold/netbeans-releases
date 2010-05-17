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
package org.netbeans.modules.jmx.mbeanwizard.table;
import org.netbeans.modules.jmx.common.WizardConstants;
import org.netbeans.modules.jmx.mbeanwizard.editor.JComboBoxCellEditor;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.MBeanNotificationTableModel;
import org.netbeans.modules.jmx.mbeanwizard.editor.JTextFieldCellEditor;
import org.netbeans.modules.jmx.mbeanwizard.renderer.ComboBoxRenderer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.jmx.mbeanwizard.editor.NotificationPanelEditor;
import org.netbeans.modules.jmx.MBeanNotificationType;
import org.netbeans.modules.jmx.mbeanwizard.popup.NotificationTypePopup;
import org.netbeans.modules.jmx.mbeanwizard.renderer.NotificationPanelRenderer;


/**
 * Class responsible for the notification table in the Notification Panel
 *
 */
public class NotificationTable extends JTable {
    
    /*******************************************************************/
    // here we use raw model calls (i.e getValueAt and setValueAt) to
    // access the model data because the inheritance pattern
    // makes it hard to type these calls and to use the object model
    /********************************************************************/
    
    private JPanel ancestorPanel = null;
    
    /**
     * Constructor
     * @param ancestorPanel the parent panel
     * @param model the table model of this table
     */
    public NotificationTable(JPanel ancestorPanel, AbstractTableModel model) {
        super(model);
        this.setRowHeight(25);
        this.setPreferredScrollableViewportSize(new Dimension(500, 70));
        this.setRowSelectionAllowed(true);
        this.setColumnSelectionAllowed(false);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.ancestorPanel = ancestorPanel;
    }
    
    /**
     * Returns the cell editor for the table according to the column
     * @param row the row to be considered
     * @param column the column to be considered
     * @return TableCellEditor the cell editor
     */
    public TableCellEditor getCellEditor(final int row, int column) {
        
        if(row >= getRowCount())
            return null;
        
        final JTable table = this;
        final MBeanNotificationTableModel model = 
                (MBeanNotificationTableModel)this.getModel();
        
        if (column == 0) {
            JComboBox nameField = new JComboBox();
            nameField.addItem(WizardConstants.NOTIFICATION);
            nameField.addItem(WizardConstants.ATTRIBUTECHANGE_NOTIFICATION);
            nameField.setName("notifClassBox");// NOI18N
            nameField.setEditable(true);
            nameField.setEnabled(true);
            Object o = getModel().getValueAt(row,column);
            nameField.setSelectedItem(o);
            final org.netbeans.modules.jmx.MBeanNotification notif = 
                    (org.netbeans.modules.jmx.MBeanNotification) model.getNotification(row);
            nameField.addItemListener(new ItemListener() {
                private String notifType;
          
                public void itemStateChanged(ItemEvent evt) {
                    if (evt.getStateChange() == evt.DESELECTED)
                        notifType = evt.getItem().toString();
                    else {
                        ArrayList<MBeanNotificationType> array  =
                                new ArrayList<MBeanNotificationType>();

                        if (evt.getItem().toString().equals(
                                WizardConstants.ATTRIBUTECHANGE_NOTIFICATION)) {
                            array.add(new MBeanNotificationType(
                                    WizardConstants.NOTIF_TYPE_ATTRIBUTE_CHANGE));
                            notif.setNotificationTypeList(array);
                        } else if (notifType.equals(
                                WizardConstants.ATTRIBUTECHANGE_NOTIFICATION)) {
                            notif.setNotificationTypeList(array);
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                model.fireTableDataChanged();
                            }
                        });
                    }
                }
            });
            
            return new JComboBoxCellEditor(nameField, this);
        } else {
            if (column == 1) {
                JTextField descrField = new JTextField();
                String o = ((String)getModel().getValueAt(row,column));
                descrField.setText(o);
                return new JTextFieldCellEditor(descrField, this);
            } else {
                if (column == 2) {
                    final JTextField typeField = new JTextField();
                    typeField.setEditable(false);
                    typeField.setName("typeTextField");// NOI18N
                    JButton typeButton = new JButton(
                            WizardConstants.MBEAN_POPUP_EDIT_BUTTON);
                    typeButton.setName("notifTypePopupJButton");// NOI18N
                    typeButton.setMargin(new java.awt.Insets(2,2,2,2));
                    typeButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            NotificationTypePopup notifPopup = 
                                    new NotificationTypePopup(
                                    ancestorPanel, model, typeField, row);
                        }
                    });
                    
                    //gets all the notification types of the current 
                    //notification
                    ArrayList<MBeanNotificationType> notifType = 
                            (ArrayList<MBeanNotificationType>)
                                    getModel().getValueAt(row,column);
                    
                    // if the notif type list size contains only one element 
                    // and this
                    // element is Attribute Change the button and the textfield
                    // are disabled
                    if ((notifType.size() == 1) &&
                            (notifType.get(0).getNotificationType().equals(
                            WizardConstants.NOTIF_TYPE_ATTRIBUTE_CHANGE))) {
                        typeButton.setEnabled(false);
                        typeField.setEditable(false);
                    }
                    
                    JPanel panel = new JPanel(new BorderLayout());
                    panel.add(typeField, BorderLayout.CENTER);
                    panel.add(typeButton, BorderLayout.EAST);
                    return new NotificationPanelEditor(model, panel, 
                            typeField, typeButton, row);
                }
            }
            return super.getCellEditor(row,column);
        }
    }
    
    /**
     * Returns the cell renderer for the table according to the column
     * @param row the row to be considered
     * @param column the column to be considered
     * @return TableCellRenderer the cell renderer
     */
    public TableCellRenderer getCellRenderer(int row, int column) {
        
        if(row >= getRowCount())
            return null;
        
        if (column == 0) {
            JComboBox nameField = new JComboBox();
            nameField.addItem(getModel().getValueAt(row,column));
            return new ComboBoxRenderer(nameField,true,true);
        } else {
            if (column == 2) {
                JTextField typeField = new JTextField();
                typeField.setEditable(false);
                typeField.setName("typeTextField");// NOI18N
                JButton typeButton = 
                        new JButton(WizardConstants.MBEAN_POPUP_EDIT_BUTTON);
                typeButton.setMargin(new java.awt.Insets(2,2,2,2));
                
                //gets all the notification types of the current notification
                ArrayList<MBeanNotificationType> notifType = 
                        (ArrayList<MBeanNotificationType>)
                getModel().getValueAt(row,column);
                
                // if the notif type list size contains only one element 
                // and this
                // element is Attribute Change the button and the textfield 
                // are disabled
                if ((notifType.size() == 1) &&
                        (notifType.get(0).getNotificationType().equals(
                        WizardConstants.NOTIF_TYPE_ATTRIBUTE_CHANGE))) {
                    typeButton.setEnabled(false);
                    typeField.setEditable(false);
                }
                
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(typeField, BorderLayout.CENTER);
                panel.add(typeButton, BorderLayout.EAST);
                return new NotificationPanelRenderer(panel, typeField);
            }
        }
        return super.getCellRenderer(row,column);
    }
}
