/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.compapp.javaee.sunresources.ui;

import java.awt.Component;
import java.awt.Dialog;
import javax.swing.JDialog;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.netbeans.modules.compapp.javaee.sunresources.tool.JavaEETool;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.filesystems.FileObject;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author echou
 */
@SuppressWarnings("serial")
public class ResourcesPanel extends JPanel {
    
    private JDialog parent;
    private JavaEETool javaEETool;
    private ResourcesTableModel tableModel;
    private JTable table;
    private JButton editButton;
    private JButton deleteButton;
    private JButton usageButton;
    private JButton closeButton;
    
    /** Creates a new instance of ResourcesPanel */
    public ResourcesPanel(JDialog parent, JavaEETool javaEETool) {
        super(new BorderLayout());
        this.parent = parent;
        this.javaEETool = javaEETool;
        initComponents();
    }
    
    private void initComponents() {
        this.setPreferredSize(new Dimension(600, 400));
        
        // create tabbedPane
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // create table tab
        tableModel = new ResourcesTableModel(javaEETool.getResourceAggregator());
        
        table = new ResourcesTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        TableCellRenderer cellRenderer = new CustomTableCellRenderer();
        table.setDefaultRenderer(Object.class, cellRenderer);
        // set initial size of column
        for (int i = 0; i < 4; i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            if (i == 0 || i == 1) { // first 2 columns are wider
                column.setPreferredWidth(column.getPreferredWidth() + 40);
            } else {  // second 2 columns are narrower
                column.setPreferredWidth(column.getPreferredWidth() - 40);
            }
        }
        
        JScrollPane tableScrollPane = new JScrollPane(table);
        PopupListener popupListener = new PopupListener();
        table.addMouseListener(popupListener);
        tableScrollPane.addMouseListener(popupListener);
        
        // create graph tab
        /*
        JComponent graphView = this.javaEETool.getGraphView();
        graphView.addMouseListener(new PopupListener());
        JScrollPane viewScrollPane = new JScrollPane(graphView);
        */
        
        tabbedPane.addTab(
                NbBundle.getMessage(ResourcesPanel.class, "LBL_table"), 
                tableScrollPane);
        /*
        tabbedPane.addTab(
                NbBundle.getMessage(ResourcesPanel.class, "LBL_graph"), 
                viewScrollPane);
         */
        
        // create bottom panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        editButton = new JButton(
                NbBundle.getMessage(ResourcesPanel.class, "LBL_edit"));
        editButton.setEnabled(false);
        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editButton_actionPerformed(e);
            }
        });
        deleteButton = new JButton(
                NbBundle.getMessage(ResourcesPanel.class, "LBL_delete"));
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteButton_actionPerformed(e);
            }
        });
        usageButton = new JButton(
                NbBundle.getMessage(ResourcesPanel.class, "LBL_view_usage"));
        usageButton.setEnabled(false);
        usageButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                usageButton_actionPerformed(e);
            }
        });
        closeButton = new JButton(
                NbBundle.getMessage(ResourcesPanel.class, "LBL_close"));
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                closeButton_actionPerformed(e);
            }
        });
        bottomPanel.add(editButton, BorderLayout.EAST);
        bottomPanel.add(deleteButton, BorderLayout.EAST);
        bottomPanel.add(usageButton, BorderLayout.EAST);
        bottomPanel.add(closeButton, BorderLayout.EAST);
        
        //add(toolBar, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private void editButton_actionPerformed(ActionEvent e) {
        try {
            int row = table.getSelectedRow();
            if (row == -1) {
                return;
            }
            if (tableModel.isEditButton(row)) {
                // this is an Edit button
                Object bean = tableModel.getBean(table.getSelectedRow());
                FileObject fo = tableModel.getBeanFileObject(table.getSelectedRow());
                PropertySheet ps = new PropertySheet();
                ps.setNodes(new Node[] { BeanNodeFactory.getBeanNode(bean, fo, tableModel) });
                String title = NbBundle.getMessage(ResourcesPanel.class, "LBL_properties");
                String closeOption = NbBundle.getMessage(ResourcesPanel.class, "LBL_close");
                Object[] options = new Object[] { closeOption };
                DialogDescriptor dd = new DialogDescriptor(
                        ps, 
                        title, 
                        true,
                        options,
                        closeOption,
                        DialogDescriptor.BOTTOM_ALIGN,
                        null,
                        null
                );
                Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
                dialog.setVisible(true);
                
                //  update to file
                tableModel.writeToFile(row);
            } else {
                // this is a Create button
                FileObject resourceDir = javaEETool.getResourceDir();
                Object bean = tableModel.getBean(table.getSelectedRow());
                String resourceFileName = BeanNodeFactory.generateResourceFileName(bean) + ".sun-resource"; // NOI18N
                int count = 1;
                while(resourceDir.getFileObject(resourceFileName + ".sun-resource") != null) { // NOI18N
                    resourceFileName = resourceFileName + Integer.toString(count);
                    count++;
                }
                File resourceFile = new File(
                        org.openide.filesystems.FileUtil.toFile(resourceDir), 
                        resourceFileName);
                tableModel.writeToFile(table.getSelectedRow(), resourceFile);
                
                NotifyDescriptor d =
                    new NotifyDescriptor.Message(
                        NbBundle.getMessage(ResourcesPanel.class, "MSG_res_created", resourceFile.getName()),
                        NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        tableModel.fireTableDataChanged();
    }
    
    private void deleteButton_actionPerformed(ActionEvent e) {
        try {
            int row = table.getSelectedRow();
            if (row == -1) {
                return;
            }
            tableModel.deleteResourceFile(row);
            NotifyDescriptor d =
                new NotifyDescriptor.Message(
                    NbBundle.getMessage(ResourcesPanel.class, "MSG_res_deleted"),
                    NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        } catch (Exception ex) {
            NotifyDescriptor d =
                new NotifyDescriptor.Message(
                    NbBundle.getMessage(ResourcesPanel.class, "MSG_res_cannot_delete"),
                    NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
        tableModel.fireTableDataChanged();
    }
    
    private void usageButton_actionPerformed(ActionEvent e) {
        try {
            int row = table.getSelectedRow();
            if (row == -1) {
                return;
            }
            String title = NbBundle.getMessage(ResourcesPanel.class, "LBL_res_usage");
            String closeOption = NbBundle.getMessage(ResourcesPanel.class, "LBL_close");
            Object[] options = new Object[] { closeOption };
            ResourceUsagePanel usagePanel = 
                    new ResourceUsagePanel(tableModel.getProject(), tableModel.getBeanUsages(row));
            DialogDescriptor dd = new DialogDescriptor(
                    usagePanel,
                    title,
                    true,
                    options,
                    closeOption,
                    DialogDescriptor.BOTTOM_ALIGN,
                    null,
                    null
            );
            Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
            usagePanel.setRootDialog(this.parent);
            usagePanel.setParentDialog(dialog);
            
            dialog.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void closeButton_actionPerformed(ActionEvent e) {
        this.parent.setVisible(false);
        this.parent.dispose();
    }
    
    class ResourcesTable extends JTable {
        
        ResourcesTable(ResourcesTableModel model) {
            super(model);
        }
        
        public void valueChanged(ListSelectionEvent e) {
            super.valueChanged(e);
            if (getSelectedRow() == -1) {
                editButton.setEnabled(false);
                deleteButton.setEnabled(false);
                usageButton.setEnabled(false);
            } else {
                editButton.setEnabled(true);
                if (tableModel.isEditButton(getSelectedRow())) {
                    editButton.setText(NbBundle.getMessage(ResourcesPanel.class, "LBL_edit"));
                } else {
                    editButton.setText(NbBundle.getMessage(ResourcesPanel.class, "LBL_create"));
                }
                if (tableModel.canDelete(getSelectedRow())) {
                    deleteButton.setEnabled(true);
                } else {
                    deleteButton.setEnabled(false);
                }
                usageButton.setEnabled(true);
            }
        }
    }
    
    class CustomTableCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
		boolean isSelected,
		boolean hasFocus,
		int row,
		int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(LEFT);
            return this;
        }
    }
    
    class PopupListener extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            createPopup(e);
        }
        public void mouseReleased(MouseEvent e) {
            createPopup(e);
        }
        private void createPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                JPopupMenu popup = new JPopupMenu();
                JMenuItem editItem = new JMenuItem(
                        NbBundle.getMessage(ResourcesPanel.class, "LBL_edit"));
                editItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        editButton_actionPerformed(e);
                    }
                });
                JMenuItem deleteItem = new JMenuItem(
                        NbBundle.getMessage(ResourcesPanel.class, "LBL_delete"));
                deleteItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        deleteButton_actionPerformed(e);
                    }
                });
                JMenuItem usageItem = new JMenuItem(
                        NbBundle.getMessage(ResourcesPanel.class, "LBL_view_usage"));
                usageItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        usageButton_actionPerformed(e);
                    }
                });
                if (table.getSelectedRow() == -1) {
                    editItem.setEnabled(false);
                    deleteItem.setEnabled(false);
                    usageItem.setEnabled(false);
                } else {
                    if (tableModel.isEditButton(table.getSelectedRow())) {
                        editItem.setText(NbBundle.getMessage(ResourcesPanel.class, "LBL_edit"));
                    } else {
                        editItem.setText(NbBundle.getMessage(ResourcesPanel.class, "LBL_create"));
                    }
                    if (tableModel.canDelete(table.getSelectedRow())) {
                        deleteItem.setEnabled(true);
                    } else {
                        deleteItem.setEnabled(false);
                    }
                }
                popup.add(editItem);
                popup.addSeparator();
                popup.add(deleteItem);
                popup.addSeparator();
                popup.add(usageItem);
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
    
    public static void main(String[] args) {
        /*
        try {
            JavaEETool earTool = new JavaEETool(new File("C:/TEMP/EnterpriseApplication3"));
            ResourcesPanel panel = new ResourcesPanel(earTool);
            
            JFrame frame = new JFrame("Server resources ...");
            frame.add(panel);
            
            int width = 600;
            int height = 300;
            frame.add (panel, BorderLayout.CENTER);
            frame.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);
            java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            frame.setBounds((screenSize.width-width)/2, (screenSize.height-height)/2, width, height);
            frame.setVisible (true);
            
            earTool.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }

}
