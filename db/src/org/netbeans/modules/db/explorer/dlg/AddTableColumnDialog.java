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

import java.sql.*;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.util.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.*;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

import org.openide.*;
import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.util.NbBundle;

import org.netbeans.lib.ddl.impl.*;
import org.netbeans.lib.ddl.util.*;
import org.netbeans.modules.db.explorer.*;
import org.netbeans.modules.db.util.*;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.nodes.*;

public class AddTableColumnDialog {
    boolean result = false;
    Dialog dialog = null;
    Specification spec;
    Map ixmap;
    Map ix_uqmap;
    String colname = null;
    transient private static final String tempStr = new String();
    JTextField colnamefield, colsizefield, colscalefield, defvalfield;
    JTextArea checkfield;
    JComboBox coltypecombo, idxcombo;
    JCheckBox pkcheckbox, ixcheckbox, checkcheckbox, nullcheckbox, uniquecheckbox;
    DataModel dmodel = new DataModel();
    private static ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N

    public AddTableColumnDialog(final Specification spe, final DatabaseNodeInfo nfo) {
        spec = spe;
        try {
            JLabel label;
            JPanel pane = new JPanel();
            pane.setBorder(new EmptyBorder(new Insets(12, 12, 5, 11)));
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints con;
            pane.setLayout (layout);

            TextFieldListener fldlistener = new TextFieldListener(dmodel);
            IntegerFieldListener intfldlistener = new IntegerFieldListener(dmodel);

            // Column name

            label = new JLabel(bundle.getString("AddTableColumnName")); //NOI18N
            con = new GridBagConstraints ();
            con.gridx = 0;
            con.gridy = 0;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (0, 0, 0, 0);
            con.weightx = 0.0;
            con.weighty = 0.0;
            pane.add(label, con);

            con = new GridBagConstraints ();
            con.gridx = 1;
            con.gridy = 0;
            con.gridwidth = 3;
            con.gridheight = 1;
            con.fill = GridBagConstraints.HORIZONTAL;
            con.insets = new java.awt.Insets (0, 12, 0, 0);
            con.weightx = 1.0;
            con.weighty = 0.0;
            colnamefield = new JTextField(35);
            colnamefield.setName(ColumnItem.NAME);
            colnamefield.addFocusListener(fldlistener);
            pane.add(colnamefield, con);

            // Column type

            Map tmap = spec.getTypeMap();
            Vector ttab = new Vector(tmap.size());
            Iterator iter = tmap.keySet().iterator();
            while (iter.hasNext()) {
                String iterkey = (String)iter.next();
                String iterval = (String)tmap.get(iterkey);
                ttab.add(new TypeElement(iterkey, iterval));
            }

            ColumnItem item = new ColumnItem();
            item.setProperty(ColumnItem.TYPE, ttab.elementAt(0));
            dmodel.addRow(item);

            label = new JLabel(bundle.getString("AddTableColumnType")); //NOI18N
            con = new GridBagConstraints ();
            con.gridx = 0;
            con.gridy = 1;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (12, 0, 0, 0);
            con.weightx = 0.0;
            con.weighty = 0.0;
            pane.add(label, con);

            con = new GridBagConstraints ();
            con.gridx = 1;
            con.gridy = 1;
            con.gridwidth = 3;
            con.gridheight = 1;
            con.fill = GridBagConstraints.HORIZONTAL;
            con.insets = new java.awt.Insets (12, 12, 0, 0);
            con.weightx = 1.0;
            con.weighty = 0.0;
            coltypecombo = new JComboBox(ttab);
            coltypecombo.addActionListener(new ComboBoxListener(dmodel));
            coltypecombo.setName(ColumnItem.TYPE);
            pane.add(coltypecombo, con);

            // Column size

            label = new JLabel(bundle.getString("AddTableColumnSize")); //NOI18N
            con = new GridBagConstraints ();
            con.gridx = 0;
            con.gridy = 2;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (12, 0, 0, 0);
            con.weightx = 0.0;
            con.weighty = 0.0;
            pane.add(label, con);

            con = new GridBagConstraints ();
            con.gridx = 1;
            con.gridy = 2;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.fill = GridBagConstraints.HORIZONTAL;
            con.insets = new java.awt.Insets (12, 12, 0, 0);
            con.weightx = 1.0;
            con.weighty = 0.0;
            colsizefield = new ValidableTextField(new TextFieldValidator.integer());
            colsizefield.setName(ColumnItem.SIZE);
            colsizefield.addFocusListener(intfldlistener);
            pane.add(colsizefield, con);

            // Column scale

            label = new JLabel(bundle.getString("AddTableColumnScale")); //NOI18N
            con = new GridBagConstraints ();
            con.gridx = 2;
            con.gridy = 2;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (12, 12, 0, 0);
            con.weightx = 0.0;
            con.weighty = 0.0;
            pane.add(label, con);

            con = new GridBagConstraints ();
            con.gridx = 3;
            con.gridy = 2;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.fill = GridBagConstraints.HORIZONTAL;
            con.insets = new java.awt.Insets (12, 12, 0, 0);
            con.weightx = 1.0;
            con.weighty = 0.0;
            colscalefield = new ValidableTextField(new TextFieldValidator.integer());
            colscalefield.setName(ColumnItem.SCALE);
            colscalefield.addFocusListener(intfldlistener);
            pane.add(colscalefield, con);

            // Column default value

            label = new JLabel(bundle.getString("AddTableColumnDefault")); //NOI18N
            con = new GridBagConstraints ();
            con.gridx = 0;
            con.gridy = 3;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (12, 0, 0, 0);
            con.weightx = 0.0;
            con.weighty = 0.0;
            pane.add(label, con);

            con = new GridBagConstraints ();
            con.gridx = 1;
            con.gridy = 3;
            con.gridwidth = 3;
            con.gridheight = 1;
            con.fill = GridBagConstraints.HORIZONTAL;
            con.insets = new java.awt.Insets (12, 12, 0, 0);
            con.weightx = 1.0;
            con.weighty = 0.0;
            defvalfield = new JTextField(35);
            defvalfield.setName(ColumnItem.DEFVAL);
            defvalfield.addFocusListener(fldlistener);
            layout.setConstraints(defvalfield, con);
            pane.add(defvalfield);

            // Check subpane

            JPanel subpane = new JPanel();
            subpane.setBorder(new TitledBorder(bundle.getString("AddTableColumnConstraintsTitle"))); //NOI18N
            GridBagLayout sublayout = new GridBagLayout();
            subpane.setLayout(sublayout);

            ActionListener cbxlistener = new CheckBoxListener(dmodel);

            con = new GridBagConstraints ();
            con.gridx = 0;
            con.gridy = 0;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (0, 0, 0, 0);
            con.weightx = 0.0;
            con.weighty = 0.0;
            pkcheckbox = new JCheckBox(" "+bundle.getString("AddTableColumnConstraintPKTitle")); //NOI18N
            pkcheckbox.setName(ColumnItem.PRIMARY_KEY);
            pkcheckbox.addActionListener(cbxlistener);
            subpane.add(pkcheckbox, con);

            con = new GridBagConstraints ();
            con.gridx = 1;
            con.gridy = 0;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (0, 12, 0, 0);
            con.weightx = 0.0;
            con.weighty = 0.0;
            uniquecheckbox = new JCheckBox(" "+bundle.getString("AddTableColumnConstraintUniqueTitle")); //NOI18N
            uniquecheckbox.setName(ColumnItem.UNIQUE);
            uniquecheckbox.addActionListener(cbxlistener);
            subpane.add(uniquecheckbox, con);

            con = new GridBagConstraints ();
            con.gridx = 2;
            con.gridy = 0;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (0, 12, 0, 0);
            con.weightx = 0.0;
            con.weighty = 0.0;
            nullcheckbox = new JCheckBox(" "+bundle.getString("AddTableColumnConstraintNullTitle")); //NOI18N
            nullcheckbox.setName(ColumnItem.NULLABLE);
            nullcheckbox.addActionListener(cbxlistener);
            subpane.add(nullcheckbox, con);

            // Insert subpane

            con = new GridBagConstraints ();
            con.gridx = 0;
            con.gridy = 4;
            con.gridwidth = 4;
            con.gridheight = 1;
            con.fill = GridBagConstraints.HORIZONTAL;
            con.insets = new java.awt.Insets (12, 0, 0, 0);
            con.weightx = 1.0;
            con.weighty = 0.0;
            pane.add(subpane, con);

            // are there primary keys?
            boolean isPK = false;
            try {
                String table = (String)nfo.get(DatabaseNode.TABLE);
                DriverSpecification drvSpec = nfo.getDriverSpecification();

                drvSpec.getPrimaryKeys(table);
                ResultSet rs = drvSpec.getResultSet();

                if( rs != null ) {
                    if(rs.next())
                        isPK = true;
                    rs.close();
                }
                
            } catch (Exception e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) //NOI18N
                    e.printStackTrace();
            }

            // Index name combo

            con = new GridBagConstraints ();
            con.gridx = 0;
            con.gridy = 5;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.anchor = GridBagConstraints.NORTHWEST;
            con.insets = new java.awt.Insets (12, 0, 0, 0);
            con.weightx = 0.0;
            con.weighty = 0.0;
            ixcheckbox = new JCheckBox(" "+bundle.getString("AddTableColumnConstraintIXTitle")); //NOI18N
            ixcheckbox.setName(ColumnItem.INDEX);
            ixcheckbox.addActionListener(cbxlistener);
            pane.add(ixcheckbox, con);

            try {
                String table = (String)nfo.get(DatabaseNode.TABLE);
                DriverSpecification drvSpec = nfo.getDriverSpecification();

                drvSpec.getIndexInfo(table, false, false);
                ResultSet rs = drvSpec.getResultSet();
                HashMap rset = new HashMap();
                
                ixmap = new HashMap();
                ix_uqmap = new HashMap();
                String ixname;
                while (rs.next()) {
                    rset = drvSpec.getRow();
                    ixname = (String) rset.get(new Integer(6));
                    if (ixname != null) {
                        Vector ixcols = (Vector)ixmap.get(ixname);
                        if (ixcols == null) {
                            ixcols = new Vector();
                            ixmap.put(ixname,ixcols);
                            boolean uq = !Boolean.valueOf( (String)rset.get( new Integer(4) ) ).booleanValue();
                            if(uq)
                                ix_uqmap.put( ixname, ColumnItem.UNIQUE );
                        }

                        ixcols.add((String) rset.get(new Integer(9)));
                    }
                    rset.clear();
                }
                rs.close();
            } catch (Exception e) {
                //
            }

            con = new GridBagConstraints ();
            con.gridx = 1;
            con.gridy = 5;
            con.gridwidth = 3;
            con.gridheight = 1;
            con.fill = GridBagConstraints.HORIZONTAL;
            con.insets = new java.awt.Insets (12, 12, 0, 0);
            con.weightx = 1.0;
            con.weighty = 0.0;
            idxcombo = new JComboBox(new Vector(ixmap.keySet()));
            //idxcombo.setSelectedIndex(0);
            pane.add(idxcombo, con);

            // Check title and textarea

            con = new GridBagConstraints ();
            con.gridx = 0;
            con.gridy = 6;
            con.gridwidth = 1;
            con.gridheight = 1;
            con.anchor = GridBagConstraints.NORTHWEST;
            con.insets = new java.awt.Insets (12, 0, 0, 0);
            con.weightx = 0.0;
            con.weighty = 0.0;
            checkcheckbox = new JCheckBox(" "+bundle.getString("AddTableColumnConstraintCheckTitle")); //NOI18N
            checkcheckbox.setName(ColumnItem.CHECK);
            checkcheckbox.addActionListener(cbxlistener);
            pane.add(checkcheckbox, con);

            con = new GridBagConstraints ();
            con.gridx = 1;
            con.gridy = 6;
            con.gridwidth = 3;
            con.gridheight = 1;
            con.fill = GridBagConstraints.BOTH;
            con.insets = new java.awt.Insets (12, 12, 0, 0);
            con.weightx = 1.0;
            con.weighty = 1.0;
            checkfield = new JTextArea(3, 35);
            checkfield.setName(ColumnItem.CHECK_CODE);
            checkfield.addFocusListener(fldlistener);
            JScrollPane spane = new JScrollPane(checkfield);
            pane.add(spane, con);

            checkcheckbox.setSelected(false);
            checkcheckbox.setSelected(false);
            nullcheckbox.setSelected(false);
            uniquecheckbox.setSelected(false);
            pkcheckbox.setEnabled(!isPK);
            idxcombo.setEnabled(idxcombo.getItemCount()>0);
            ixcheckbox.setEnabled(idxcombo.isEnabled());
            
            item.addPropertyChangeListener(new PropertyChangeListener() {
                                               public void propertyChange(PropertyChangeEvent evt) {
                                                   String pname = evt.getPropertyName();
                                                   Object nval = evt.getNewValue();
                                                   if (nval instanceof Boolean) {
                                                       boolean set = ((Boolean)nval).booleanValue();
                                                       if (pname.equals(ColumnItem.PRIMARY_KEY)) {
                                                           pkcheckbox.setSelected(set);
                                                           //idxcombo.setEnabled(!set);
                                                           //ixcheckbox.setEnabled(!set);
                                                           //ixcheckbox.setSelected(set);
                                                       } else if (pname.equals(ColumnItem.INDEX)) {
                                                           ixcheckbox.setSelected(set);
                                                       } else if (pname.equals(ColumnItem.UNIQUE)) {
                                                           uniquecheckbox.setSelected(set);
                                                           idxcombo.setEnabled(!set);
                                                           ixcheckbox.setEnabled(!set);
                                                           ixcheckbox.setSelected(set);
                                                           if(set) {
                                                               idxcombo.addItem(tempStr);
                                                               idxcombo.setSelectedItem(tempStr);
                                                           } else {
                                                               idxcombo.removeItem(tempStr);
                                                               idxcombo.setEnabled(idxcombo.getItemCount()>0);
                                                               ixcheckbox.setEnabled(idxcombo.isEnabled());
                                                           }
                                                       } else if (pname.equals(ColumnItem.NULLABLE)) {
                                                           nullcheckbox.setSelected(set);
                                                       }
                                                   }
                                               }
                                           });

            ActionListener listener = new ActionListener() {
                      public void actionPerformed(ActionEvent event) {
                          if (event.getSource() == DialogDescriptor.OK_OPTION) {
                              result = validate();

                              CommandBuffer cbuff = new CommandBuffer();

                              if (result) {
                                  try {
                                      boolean use_idx = false;
                                      String tablename = nfo.getTable();
                                      colname = colnamefield.getText();
                                      ColumnItem citem = (ColumnItem)dmodel.getData().elementAt(0);
                                      AddColumn cmd = spec.createCommandAddColumn(tablename);
                                      cmd.setObjectOwner((String)nfo.get(DatabaseNodeInfo.SCHEMA));
                                      org.netbeans.lib.ddl.impl.TableColumn col = null;
                                      if (citem.isPrimaryKey()) {
                                          col = (org.netbeans.lib.ddl.impl.TableColumn)cmd.createPrimaryKeyColumn(colname);
                                      } else if (citem.isUnique()) {
                                          col = (org.netbeans.lib.ddl.impl.TableColumn)cmd.createUniqueColumn(colname);
                                      } else col = (org.netbeans.lib.ddl.impl.TableColumn)cmd.createColumn(colname);
                                      if (citem.isIndexed()) use_idx = true;
                                      col.setColumnType(Specification.getType(citem.getType().getType()));
                                      col.setColumnSize(citem.getSize());
                                      col.setDecimalSize(citem.getScale());
                                      col.setNullAllowed(citem.allowsNull());
                                      if (citem.hasDefaultValue()) col.setDefaultValue(citem.getDefaultValue());

                                      if (citem.hasCheckConstraint()) {
                                          // add COLUMN constraint (without constraint name)
                                          col.setCheckCondition(citem.getCheckConstraint());
                                      }
                                      cbuff.add(cmd);

                                      if (use_idx) {

                                          String idxname = (String)idxcombo.getSelectedItem();
                                          String isUQ = new String();
                                          if (ixmap.containsKey(idxname)) {
                                              if(ix_uqmap.containsKey(idxname))
                                                                isUQ = ColumnItem.UNIQUE;
                                              DropIndex dropIndexCmd = spec.createCommandDropIndex(idxname);
                                              dropIndexCmd.setTableName(tablename);
                                              dropIndexCmd.setObjectOwner((String)nfo.get(DatabaseNodeInfo.SCHEMA));
                                              cbuff.add(dropIndexCmd);
                                          }

                                          CreateIndex xcmd = spec.createCommandCreateIndex(tablename);
                                          xcmd.setIndexName(idxname);
                                          xcmd.setIndexType(isUQ);
                                          xcmd.setObjectOwner((String)nfo.get(DatabaseNodeInfo.SCHEMA));
                                          Enumeration enu = ((Vector)ixmap.get(idxname)).elements();
                                          while (enu.hasMoreElements()) {
                                              xcmd.specifyColumn((String)enu.nextElement());
                                          }
                                          xcmd.specifyColumn(citem.getName());
                                          cbuff.add(xcmd);
                                      }

                                      cbuff.execute();

                                      // was execution of commands with or without exception?
                                      if(!cbuff.wasException()) {
                                          // dialog is closed after successfully add column
                                          dialog.setVisible(false);
                                          dialog.dispose();
                                      }
                                      //dialog is not closed after unsuccessfully add column

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                              } else {
                                  String msg = bundle.getString("EXC_InsufficientAddColumnInfo");
                                  TopManager.getDefault().notify(
                                    new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
                              }
                          }
                      }
                  };

            DialogDescriptor descriptor = new DialogDescriptor(pane, bundle.getString("AddColumnDialogTitle"), true, listener); //NOI18N
            // inbuilt close of the dialog is only after CANCEL button click
            // after OK button is dialog closed by hand
            Object [] closingOptions = {DialogDescriptor.CANCEL_OPTION};
            descriptor.setClosingOptions(closingOptions);
            dialog = TopManager.getDefault().createDialog(descriptor);
            dialog.setResizable(true);
        } catch (MissingResourceException e) {
            e.printStackTrace();
        }
    }

    public boolean run() {
        if (dialog != null) dialog.setVisible(true);
        return result;
    }

    private boolean validate() {
        Vector cols = dmodel.getData();
        String colname = colnamefield.getText();
        if (colname == null || colname.length()<1)
            return false;

        Enumeration colse = cols.elements();
        while(colse.hasMoreElements())
            if (!((ColumnItem)colse.nextElement()).validate())
                return false;

        return true;
    }

    public String getColumnName() {
        return colname;
    }

    class CheckBoxListener implements ActionListener {
        private DataModel data;

        CheckBoxListener(DataModel data) {
            this.data = data;
        }

        public void actionPerformed(ActionEvent event) {
            JCheckBox cbx = (JCheckBox)event.getSource();
            String code = cbx.getName();
            data.setValue(new Boolean(cbx.isSelected()), code, 0);
        }
    }

    class ComboBoxListener implements ActionListener {
        private DataModel data;

        ComboBoxListener(DataModel data) {
            this.data = data;
        }

        public void actionPerformed(ActionEvent event) {
            JComboBox cbx = (JComboBox)event.getSource();
            String code = cbx.getName();
            data.setValue(cbx.getSelectedItem(), code, 0);
        }
    }

    class TextFieldListener implements FocusListener {
        private DataModel data;

        TextFieldListener(DataModel data) {
            this.data = data;
        }

        public void focusGained(FocusEvent event) {
        }

        public void focusLost(FocusEvent event) {
            JTextComponent fld = (JTextComponent)event.getSource();
            String code = fld.getName();
            data.setValue(fld.getText(), code, 0);
        }
    }

    class IntegerFieldListener implements FocusListener {
        private DataModel data;

        IntegerFieldListener(DataModel data) {
            this.data = data;
        }

        public void focusGained(FocusEvent event) {
        }

        public void focusLost(FocusEvent event) {
            JTextComponent fld = (JTextComponent)event.getSource();
            String code = fld.getName();
            String numero = fld.getText();
            Integer ival;
            if (numero == null || numero.length()==0) numero = "0"; //NOI18N
            ival = new Integer(numero);
            data.setValue(ival, code, 0);
        }
    }
}
