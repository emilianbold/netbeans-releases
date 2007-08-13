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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.dlg;

import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.util.*;
import java.text.MessageFormat;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.impl.CreateTable;
import org.netbeans.lib.ddl.util.CommandBuffer;
import org.netbeans.lib.ddl.impl.CreateIndex;
import org.netbeans.lib.ddl.util.PListReader;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.util.TextFieldValidator;
import org.netbeans.modules.db.util.ValidableTextField;
import org.openide.awt.Mnemonics;
import org.openide.util.Utilities;

public class CreateTableDialog {
    boolean result = false;
    Dialog dialog = null;
    JTextField dbnamefield, dbownerfield;
    JTable table;
    JComboBox ownercombo;
    JButton addbtn, delbtn;
    Specification spec;
    private Vector ttab;

    private static Map dlgtab = null;
    private static final String filename = "org/netbeans/modules/db/resources/CreateTableDialog.plist"; // NOI18N
    private ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); // NOI18N
    private static Logger LOGGER = Logger.getLogger(
            CreateTableDialog.class.getName());

    public static final Map getProperties() {
        if (dlgtab == null) try {
            ClassLoader cl = CreateTableDialog.class.getClassLoader();
            InputStream stream = cl.getResourceAsStream(filename);
            if (stream == null) {
                String message = MessageFormat.format(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("EXC_UnableToOpenStream"), new String[] {filename}); // NOI18N
                throw new Exception(message);
            }
            PListReader reader = new PListReader(stream);
            dlgtab = reader.getData();
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
            dlgtab = null;
        }

        return dlgtab;
    }

    public CreateTableDialog(final Specification spe, DatabaseNodeInfo nfo) throws java.sql.SQLException {
        spec = spe;
        try {
            JLabel label;
            JPanel pane = new JPanel();
            pane.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints constr = new GridBagConstraints();
            pane.setLayout(layout);
            pane.setMinimumSize(new Dimension(200,100));
     
            // Table name field

            label = new JLabel();
            Mnemonics.setLocalizedText(label, bundle.getString("CreateTableName")); // NOI18N
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CreateTableNameA11yDesc"));
            constr.anchor = GridBagConstraints.WEST;
            constr.weightx = 0.0;
            constr.weighty = 0.0;
            constr.fill = GridBagConstraints.NONE;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            constr.gridx = 0;
            constr.gridy = 0;
            layout.setConstraints(label, constr);
            pane.add(label);

            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.weightx = 1.0;
            constr.weighty = 0.0;
            constr.gridx = 1;
            constr.gridy = 0;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            dbnamefield = new JTextField(bundle.getString("CreateTableUntitledName"), 10); // NOI18N
            dbnamefield.setToolTipText(bundle.getString("ACS_CreateTableNameTextFieldA11yDesc"));
            dbnamefield.getAccessibleContext().setAccessibleName(bundle.getString("ACS_CreateTableNameTextFieldA11yName"));
            label.setLabelFor(dbnamefield);
            layout.setConstraints(dbnamefield, constr);
            pane.add(dbnamefield);

            // Table owner combo

            label = new JLabel();
            Mnemonics.setLocalizedText(label, bundle.getString("CreateTableOwner")); // NOI18N
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CreateTableOwnerA11yDesc"));
            constr.anchor = GridBagConstraints.WEST;
            constr.weightx = 0.0;
            constr.weighty = 0.0;
            constr.fill = GridBagConstraints.NONE;
            constr.insets = new java.awt.Insets (2, 10, 2, 2);
            constr.gridx = 2;
            constr.gridy = 0;
            layout.setConstraints(label, constr);
            pane.add(label);

            Vector users = new Vector();
            String schema = nfo.getDriverSpecification().getSchema();
            if (schema != null && schema.length() > 0)
                users.add(schema);
            else
                users.add(" "); //NOI18N

            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.weightx = 0.0;
            constr.weighty = 0.0;
            constr.gridx = 3;
            constr.gridy = 0;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            ownercombo = new JComboBox(users);
            ownercombo.setSelectedIndex(0);
            ownercombo.setRenderer(new ListCellRendererImpl());
            ownercombo.setToolTipText(bundle.getString("ACS_CreateTableOwnerComboBoxA11yDesc"));
            ownercombo.getAccessibleContext().setAccessibleName(bundle.getString("ACS_CreateTableOwnerComboBoxA11yName"));
            label.setLabelFor(ownercombo);
            layout.setConstraints(ownercombo, constr);
            pane.add(ownercombo);

            // Table columns in scrollpane

            constr.fill = GridBagConstraints.BOTH;
            constr.weightx = 1.0;
            constr.weighty = 1.0;
            constr.gridx = 0;
            constr.gridy = 1;
            constr.gridwidth = 4;
            constr.gridheight = 3;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            table = new DataTable(new DataModel());
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setToolTipText(bundle.getString("ACS_CreateTableColumnTableA11yDesc"));
            table.getAccessibleContext().setAccessibleName(bundle.getString("ACS_CreateTableColumnTableA11yName"));
            table.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CreateTableColumnTableA11yDesc"));
            JScrollPane scrollpane = new JScrollPane(table);
            scrollpane.setBorder(new BevelBorder(BevelBorder.LOWERED));
            layout.setConstraints(scrollpane, constr);
            pane.add(scrollpane);

            // Setup cell editors for table

            Map tmap = spec.getTypeMap();
            ttab = new Vector(tmap.size());
            Iterator iter = tmap.keySet().iterator();
            while (iter.hasNext()) {
                String iterkey = (String)iter.next();
                String iterval = (String)tmap.get(iterkey);
                ttab.add(new TypeElement(iterkey, iterval));
            }

            final JComboBox combo = new JComboBox(ttab);
            combo.setSelectedIndex(0);
            table.setDefaultEditor(String.class, new DataCellEditor(new JTextField()));
            table.getColumn("type").setCellEditor(new ComboBoxEditor(combo)); // NOI18N
            table.getColumn("size").setCellEditor(new DataCellEditor(new ValidableTextField(new TextFieldValidator.integer()))); // NOI18N
            table.getColumn("scale").setCellEditor(new DataCellEditor(new ValidableTextField(new TextFieldValidator.integer()))); // NOI18N
            table.setRowHeight(combo.getPreferredSize().height);

            // Button pane

            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.anchor = GridBagConstraints.NORTH;
            constr.weightx = 0.0;
            constr.weighty = 0.0;
            constr.gridx = 4;
            constr.gridy = 1;
            constr.insets = new java.awt.Insets (2, 8, 2, 2);
            JPanel btnpane = new JPanel();
            GridLayout btnlay = new GridLayout(2,1,0,5);
            btnpane.setLayout(btnlay);
            layout.setConstraints(btnpane, constr);
            pane.add(btnpane);

            // Button add column

            addbtn = new JButton();
            Mnemonics.setLocalizedText(addbtn, bundle.getString("CreateTableAddButtonTitle")); // NOI18N
            addbtn.setToolTipText(bundle.getString("ACS_CreateTableAddButtonTitleA11yDesc"));
            btnpane.add(addbtn);
            addbtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (table.getCellEditor() != null) {
                        table.getCellEditor().stopCellEditing();
                    }
                    DataModel model = (DataModel)table.getModel();
                    ColumnItem item = new ColumnItem();
                    item.setProperty(ColumnItem.TYPE, ttab.elementAt(0));
                    model.addRow(item);
                }
            });

            // Button delete column

            delbtn = new JButton();
            Mnemonics.setLocalizedText(delbtn, bundle.getString("CreateTableRemoveButtonTitle")); // NOI18N
            delbtn.setToolTipText(bundle.getString("ACS_CreateTableRemoveButtonTitleA11yDesc"));
            btnpane.add(delbtn);
            delbtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (table.getCellEditor() != null) {
                        table.getCellEditor().stopCellEditing();
                    }
                    int idx = table.getSelectedRow();
                    if (idx != -1)
                        ((DataModel)table.getModel()).removeRow(idx);
                }
            });

            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    final ActionEvent evt = event;
                    if (table.getCellEditor() != null) {
                        table.getCellEditor().stopCellEditing();
                    }
                    RequestProcessor.getDefault().post(new Runnable() {
                        public void run () {
                          if (evt.getSource() == DialogDescriptor.OK_OPTION) {
                              result = validate();

                              CommandBuffer cbuff = new CommandBuffer();
                              Vector idxCommands = new Vector();

                              if (! result) {
                                  String msg = bundle.getString("EXC_InsufficientCreateTableInfo");
                                  DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
                              }
                                  try {
                                      String tablename = getTableName();
                                      DataModel dataModel = (DataModel)table.getModel();
                                      Vector data = dataModel.getData();
                                      String owner = ((String)ownercombo.getSelectedItem()).trim();
                                      
                                      CreateTableDDL ddl = new CreateTableDDL(
                                              spec, owner, tablename);
                                      
                                      boolean wasException =
                                          ddl.execute(data, dataModel.getTablePrimaryKeys());

                                      //bugfix for #31064
                                      combo.setSelectedItem(combo.getSelectedItem());

                                      // was execution of commands with or without exception?
                                      if(!wasException) {
                                          // dialog is closed after successfully create table
                                          dialog.setVisible(false);
                                          dialog.dispose();
                                      }
                                      //dialog is not closed after unsuccessfully create table

                                  } catch (Exception e) {
                                      e.printStackTrace();

                                  }
                              } else {
                              }
                          }
                    }, 0);       
               }
            };

            pane.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CreateTableDialogA11yDesc"));

            addbtn.doClick();
            DialogDescriptor descriptor = new DialogDescriptor(pane, bundle.getString("CreateTableDialogTitle"), true, listener); // NOI18N
            // inbuilt close of the dialog is only after CANCEL button click
            // after OK button is dialog closed by hand
            Object [] closingOptions = {DialogDescriptor.CANCEL_OPTION};
            descriptor.setClosingOptions(closingOptions);
            dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setResizable(true);
        } catch (MissingResourceException ex) {
            ex.printStackTrace();
        }
    }

    public boolean run() {
        if (dialog != null) dialog.setVisible(true);
        return result;
    }

    public String getTableName() {
        return dbnamefield.getText();
    }

    private boolean validate() {
        String tname = getTableName();
        if (tname == null || tname.length()<1)
            return false;

        Vector cols = ((DataModel)table.getModel()).getData();
        Enumeration colse = cols.elements();
        while(colse.hasMoreElements())
            if (!((ColumnItem)colse.nextElement()).validate())
                return false;

        return true;
    }

    class DataTable extends JTable {
        static final long serialVersionUID =1222037401669064863L;
        public DataTable(TableModel model) {
            super(model);
            setSurrendersFocusOnKeystroke(true);
            TableColumnModel cmodel = getColumnModel();
            int i;
            int ccount = model.getColumnCount();
            int columnWidth;
            int preferredWidth;
            String columnName;
            int width = 0;
            for (i = 0; i < ccount; i++) {
                TableColumn col = cmodel.getColumn(i);
                Map cmap = ColumnItem.getColumnProperty(i);
                col.setIdentifier(cmap.get("name")); //NOI18N
                columnName = bundle.getString("CreateTable_" + i); //NOI18N
                columnWidth = (new Double(getFontMetrics(getFont()).getStringBounds(columnName, getGraphics()).getWidth())).intValue() + 20;
                if (cmap.containsKey("width")) { // NOI18N
                    if (((Integer)cmap.get("width")).intValue() < columnWidth)
                        col.setPreferredWidth(columnWidth);
                    else
                        col.setPreferredWidth(((Integer)cmap.get("width")).intValue()); // NOI18N
                    preferredWidth = col.getPreferredWidth();
                }
                if (cmap.containsKey("minwidth")) // NOI18N
                    if (((Integer)cmap.get("minwidth")).intValue() < columnWidth)
                        col.setMinWidth(columnWidth);
                    else
                        col.setMinWidth(((Integer)cmap.get("minwidth")).intValue()); // NOI18N
                //				if (cmap.containsKey("alignment")) {}
                //				if (cmap.containsKey("tip")) ((JComponent)col.getCellRenderer()).setToolTipText((String)cmap.get("tip"));
                if (i < 7) { // the first 7 columns should be visible
                    width += col.getPreferredWidth();
                }
            }
            width = Math.min(Math.max(width, 380), Toolkit.getDefaultToolkit().getScreenSize().width - 100);
            setPreferredScrollableViewportSize(new Dimension(width, 150));
        }
    }

    class FocusInvoker implements Runnable {
        private JTextField xxx;
        public FocusInvoker(JTextField fld) {
            xxx=fld;
        }

        public void run() {
            xxx.selectAll();
        }
    }

    class DataCellEditor extends DefaultCellEditor {
        static final long serialVersionUID =3855371868128838794L;

        public DataCellEditor(final JTextField x) {
            super(x);
            setClickCountToStart(1);
        }
    }
    
    class PopupInvoker implements Runnable {
        private JComboBox jComboBox;

        public PopupInvoker(JComboBox aJComboBox) {
            jComboBox = aJComboBox;
        }

        public void run() {
            try {
                jComboBox.showPopup();
            } catch (IllegalComponentStateException icse) {
                // This is a valid exception that occurs
                // if the jComboBox is somehow hide. Do nothing.
            }
        }

    }

    class ComboBoxEditor extends DefaultCellEditor {
        public ComboBoxEditor(final JComboBox jComboBox) {
            super(jComboBox);
            jComboBox.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                    SwingUtilities.invokeLater(new PopupInvoker(jComboBox));
                }

                public void focusLost(FocusEvent e) {}
            });
        }
    }

    private static final class ListCellRendererImpl extends DefaultListCellRenderer {
        
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            // hack to fix issue 65759
            if (Utilities.isWindows()) {
                size.width += 4;
            }
            return size;
        }
    }
}
