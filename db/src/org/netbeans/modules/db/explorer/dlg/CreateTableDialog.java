/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.dlg;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.MessageFormat;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.text.DefaultCaret;
import java.io.InputStream;
import javax.swing.event.TableModelEvent;

import org.openide.*;
import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.util.NbBundle;

import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.impl.CreateTable;
import org.netbeans.lib.ddl.util.CommandBuffer;
import org.netbeans.lib.ddl.impl.CreateIndex;
import org.netbeans.lib.ddl.util.PListReader;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.util.*;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;

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
    private static ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); // NOI18N

    public static final Map getProperties() {
        if (dlgtab == null) try {
            ClassLoader cl = CreateTableDialog.class.getClassLoader();
            InputStream stream = cl.getResourceAsStream(filename);
            if (stream == null) {
                String message = MessageFormat.format(bundle.getString("EXC_UnableToOpenStream"), new String[] {filename}); // NOI18N
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
            pane.setPreferredSize(new Dimension(502,200));
     
            // Table name field

            label = new JLabel(bundle.getString("CreateTableName")); // NOI18N
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
            dbnamefield = new JTextField(bundle.getString("CreateTableUntitledName"), 50); // NOI18N
            layout.setConstraints(dbnamefield, constr);
            pane.add(dbnamefield);

            // Table owner combo

            label = new JLabel(bundle.getString("CreateTableOwner")); // NOI18N
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
            if (schema != null)
                users.add(schema);
            else
                users.add(""); //NOI18N

            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.weightx = 0.0;
            constr.weighty = 0.0;
            constr.gridx = 3;
            constr.gridy = 0;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            ownercombo = new JComboBox(users);
            ownercombo.setSelectedIndex(0);
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
            JScrollPane scrollpane = new JScrollPane(table);
            scrollpane.setBorder(new BevelBorder(BevelBorder.LOWERED));
            scrollpane.setPreferredSize(new Dimension(300,150));
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

            JComboBox combo = new JComboBox(ttab);
            combo.setSelectedIndex(0);
            table.setDefaultEditor(String.class, new DataCellEditor(new JTextField()));
            table.getColumn("type").setCellEditor(new DefaultCellEditor(combo)); // NOI18N
            table.getColumn("size").setCellEditor(new DataCellEditor(new ValidableTextField(new TextFieldValidator.integer()))); // NOI18N
            table.getColumn("scale").setCellEditor(new DataCellEditor(new ValidableTextField(new TextFieldValidator.integer()))); // NOI18N

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

            addbtn = new JButton(bundle.getString("CreateTableAddButtonTitle")); // NOI18N
            btnpane.add(addbtn);
            addbtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    DataModel model = (DataModel)table.getModel();
                    ColumnItem item = new ColumnItem();
                    item.setProperty(ColumnItem.TYPE, ttab.elementAt(0));
                    model.addRow(item);
                }
            });

            // Button delete column

            delbtn = new JButton(bundle.getString("CreateTableRemoveButtonTitle")); // NOI18N
            btnpane.add(delbtn);
            delbtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    int idx = table.getSelectedRow();
                    if (idx != -1)
                        ((DataModel)table.getModel()).removeRow(idx);
                }
            });

            ActionListener listener = new ActionListener() {
              public void actionPerformed(ActionEvent event) {
                  if (event.getSource() == DialogDescriptor.OK_OPTION) {
                      result = validate();

                      CommandBuffer cbuff = new CommandBuffer();
                      Vector idxCommands = new Vector();

                      if (result) {
                          try {
                              String tablename = getTableName();
                              DataModel dataModel = (DataModel)table.getModel();
                              Vector data = dataModel.getData();
                              Vector icols = new Vector();
                              CreateTable cmd = spec.createCommandCreateTable(tablename);
                              
                              cmd.setObjectOwner((String)ownercombo.getSelectedItem());
                              
                              /* this variables and operation provide support for
                               * creating indexes for primary or unique keys,
                               * most of database are creating indexes by myself,
                               * support was removed */
                              org.netbeans.lib.ddl.impl.TableColumn cmdcol = null;
                              CreateIndex xcmd = null;
                              Enumeration enu = data.elements();
                              while (enu.hasMoreElements()) {
                                  ColumnItem enuele = (ColumnItem)enu.nextElement();
                                  String name = enuele.getName();
                                  if (enuele.isPrimaryKey()&&!dataModel.isTablePrimaryKey())
                                      cmdcol = (org.netbeans.lib.ddl.impl.TableColumn)cmd.createPrimaryKeyColumn(name);
                                  else if (enuele.isUnique()&&!enuele.isPrimaryKey())
                                      cmdcol = (org.netbeans.lib.ddl.impl.TableColumn)cmd.createUniqueColumn(name);
                                  else cmdcol = (org.netbeans.lib.ddl.impl.TableColumn)cmd.createColumn(name);
                                  cmdcol.setColumnType(Specification.getType(enuele.getType().getType()));
                                  cmdcol.setColumnSize(enuele.getSize());
                                  cmdcol.setDecimalSize(enuele.getScale());
                                  cmdcol.setNullAllowed(enuele.allowsNull());
                                  String defval = enuele.getDefaultValue();
                                  if (defval != null && defval.length() > 0)
                                      cmdcol.setDefaultValue(defval);
                                  if (enuele.hasCheckConstraint())
                                      // add the TABLE check constraint
                                      cmd.createCheckConstraint(name, enuele.getCheckConstraint());
                                  if (enuele.isIndexed()&&!enuele.isPrimaryKey()&&!enuele.isUnique()) {
                                      xcmd = spec.createCommandCreateIndex(tablename);
                                      xcmd.setIndexName(tablename+ "_" + name + "_idx"); // NOI18N
                                      xcmd.setIndexType(new String());
                                      xcmd.setObjectOwner((String)ownercombo.getSelectedItem());
                                      xcmd.specifyColumn(name);
                                      idxCommands.add(xcmd);
                                  }
                              }
                              if(dataModel.isTablePrimaryKey()) {
                                  cmdcol = (org.netbeans.lib.ddl.impl.TableColumn)cmd.createPrimaryKeyConstraint(tablename);
                                  cmdcol.setTableConstraintColumns(dataModel.getTablePrimaryKeys());
                                  cmdcol.setColumnType(0);
                                  cmdcol.setColumnSize(0);
                                  cmdcol.setDecimalSize(0);
                                  cmdcol.setNullAllowed(true);

                              }
                              cbuff.add(cmd);
                              for(int i=0;i<idxCommands.size();i++)
                                  cbuff.add((CreateIndex)idxCommands.elementAt(i));
                              // index support removed!
                              //if (icmd.getColumns().size()>0) cbuff.add(icmd);

                              //execute DDL command
                              cbuff.execute();

                              // was execution of commands with or without exception?
                              if(!cbuff.wasException()) {
                                  // dialog is closed after successfully create table
                                  dialog.setVisible(false);
                                  dialog.dispose();
                              }
                              //dialog is not closed after unsuccessfully create table

                          } catch (Exception e) {
                              e.printStackTrace();
                              
                          }
                      } else {
                          String msg = bundle.getString("EXC_InsufficientCreateTableInfo");
                          TopManager.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
                      }
                  }
               }
            };

            addbtn.doClick();
            DialogDescriptor descriptor = new DialogDescriptor(pane, bundle.getString("CreateTableDialogTitle"), true, listener); // NOI18N
            // inbuilt close of the dialog is only after CANCEL button click
            // after OK button is dialog closed by hand
            Object [] closingOptions = {DialogDescriptor.CANCEL_OPTION};
            descriptor.setClosingOptions(closingOptions);
            dialog = TopManager.getDefault().createDialog(descriptor);
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
            TableColumnModel cmodel = getColumnModel();
            int i, ccount = model.getColumnCount();
            for (i=0;i<ccount;i++) {
                TableColumn col = cmodel.getColumn(i);
                Map cmap = ColumnItem.getColumnProperty(i);
                col.setIdentifier((String)cmap.get("name")); // NOI18N
                if (cmap.containsKey("width")) // NOI18N
                    col.setPreferredWidth(((Integer)cmap.get("width")).intValue()); // NOI18N
                if (cmap.containsKey("minwidth")) // NOI18N
                    col.setMinWidth(((Integer)cmap.get("minwidth")).intValue()); // NOI18N
                //				if (cmap.containsKey("alignment")) {}
                //				if (cmap.containsKey("tip")) ((JComponent)col.getCellRenderer()).setToolTipText((String)cmap.get("tip"));
            }
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
            x.addFocusListener(new FocusListener() {
                public void focusGained(FocusEvent e) {
                    SwingUtilities.invokeLater(new FocusInvoker(x));
                }
                
                public void focusLost(FocusEvent e) {
                    table.setValueAt(x.getText(), table.getEditingRow(), table.getEditingColumn());
                }
            });
        }
    }
}
