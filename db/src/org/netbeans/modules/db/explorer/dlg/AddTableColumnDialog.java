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

import java.sql.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.beans.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.JTextComponent;
import org.netbeans.api.db.explorer.DatabaseException;
import org.openide.*;
import org.openide.util.NbBundle;
import org.netbeans.lib.ddl.impl.*;
import org.netbeans.lib.ddl.util.*;
import org.netbeans.modules.db.explorer.DbUtilities;
import org.netbeans.modules.db.util.*;
import org.netbeans.modules.db.explorer.infos.*;
import org.netbeans.modules.db.explorer.nodes.*;
import org.openide.awt.Mnemonics;

public class AddTableColumnDialog {
    static final Logger LOGGER = 
            Logger.getLogger(AddTableColumnDialog.class.getName());
    boolean result = false;
    Dialog dialog = null;
    Specification spec;
    AddTableColumnDDL ddl;
    Map ixmap;
    Map ix_uqmap;
    String colname = null;
    transient private static final String tempStr = new String();
    JTextField colnamefield, colsizefield, colscalefield, defvalfield;
    JTextArea checkfield;
    JComboBox coltypecombo, idxcombo;
    JCheckBox pkcheckbox, ixcheckbox, checkcheckbox, nullcheckbox, uniquecheckbox;
    DataModel dmodel = new DataModel();
    private ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N

    public AddTableColumnDialog(final Specification spe, final DatabaseNodeInfo nfo) throws DatabaseException {
        spec = spe;
        try {
            String table = (String)nfo.get(DatabaseNode.TABLE);
            String schema = (String)nfo.get(DatabaseNodeInfo.SCHEMA);
            DriverSpecification drvSpec = nfo.getDriverSpecification();
            ddl = new AddTableColumnDDL(spec, drvSpec, schema, table);

            JLabel label;
            JPanel pane = new JPanel();
            pane.setBorder(new EmptyBorder(new Insets(12, 12, 5, 11)));
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints con;
            pane.setLayout (layout);

            TextFieldListener fldlistener = new TextFieldListener(dmodel);
            IntegerFieldListener intfldlistener = new IntegerFieldListener(dmodel);

            // Column name

            label = new JLabel();
            Mnemonics.setLocalizedText(label, bundle.getString("AddTableColumnName")); //NOI18N
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddTableColumnNameA11yDesc"));
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
            colnamefield.setToolTipText(bundle.getString("ACS_AddTableColumnNameTextFieldA11yDesc"));
            colnamefield.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AddTableColumnNameTextFieldA11yName"));
            label.setLabelFor(colnamefield);
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

            label = new JLabel();
            Mnemonics.setLocalizedText(label, bundle.getString("AddTableColumnType")); //NOI18N
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddTableColumnTypeA11yDesc"));
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
            coltypecombo.setToolTipText(bundle.getString("ACS_AddTableColumnTypeComboBoxA11yDesc"));
            coltypecombo.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AddTableColumnTypeComboBoxA11yName"));
            label.setLabelFor(coltypecombo);
            pane.add(coltypecombo, con);

            // Column size

            label = new JLabel();
            Mnemonics.setLocalizedText(label, bundle.getString("AddTableColumnSize")); //NOI18N
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddTableColumnSizeA11yDesc"));
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
            colsizefield.setToolTipText(bundle.getString("ACS_AddTableColumnSizeTextFieldA11yDesc"));
            colsizefield.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AddTableColumnSizeTextFieldA11yName"));
            label.setLabelFor(colsizefield);
            pane.add(colsizefield, con);

            // Column scale

            label = new JLabel();
            Mnemonics.setLocalizedText(label, bundle.getString("AddTableColumnScale")); //NOI18N
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddTableColumnScaleA11yDesc"));
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
            colscalefield.setToolTipText(bundle.getString("ACS_AddTableColumnScaleTextFieldA11yDesc"));
            colscalefield.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AddTableColumnScaleTextFieldA11yName"));
            label.setLabelFor(colscalefield);
            pane.add(colscalefield, con);

            // Column default value

            label = new JLabel();
            Mnemonics.setLocalizedText(label, bundle.getString("AddTableColumnDefault")); //NOI18N
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddTableColumnDefaultA11yDesc"));
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
            defvalfield.setToolTipText(bundle.getString("ACS_AddTableColumnDefaultTextFieldA11yDesc"));
            defvalfield.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AddTableColumnDefaultTextFieldA11yName"));
            label.setLabelFor(defvalfield);
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
            pkcheckbox = new JCheckBox();
            Mnemonics.setLocalizedText(pkcheckbox, bundle.getString("AddTableColumnConstraintPKTitle")); //NOI18N
            pkcheckbox.setName(ColumnItem.PRIMARY_KEY);
            pkcheckbox.addActionListener(cbxlistener);
            pkcheckbox.setToolTipText(bundle.getString("ACS_AddTableColumnConstraintPKTitleA11yDesc"));
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
            uniquecheckbox = new JCheckBox();
            Mnemonics.setLocalizedText(uniquecheckbox, bundle.getString("AddTableColumnConstraintUniqueTitle")); //NOI18N
            uniquecheckbox.setName(ColumnItem.UNIQUE);
            uniquecheckbox.addActionListener(cbxlistener);
            uniquecheckbox.setToolTipText(bundle.getString("ACS_AddTableColumnConstraintUniqueTitleA11yDesc"));
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
            nullcheckbox = new JCheckBox();
            Mnemonics.setLocalizedText(nullcheckbox, bundle.getString("AddTableColumnConstraintNullTitle")); //NOI18N
            nullcheckbox.setName(ColumnItem.NULLABLE);
            nullcheckbox.addActionListener(cbxlistener);
            nullcheckbox.setToolTipText(bundle.getString("ACS_AddTableColumnConstraintNullTitleA11yDesc"));
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
                drvSpec.getPrimaryKeys(table);
                ResultSet rs = drvSpec.getResultSet();

                if( rs != null ) {
                    if(rs.next())
                        isPK = true;
                    rs.close();
                }
                
            } catch (Exception e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
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
            ixcheckbox = new JCheckBox();
            Mnemonics.setLocalizedText(ixcheckbox, bundle.getString("AddTableColumnIndexName")); //NOI18N
            ixcheckbox.setName(ColumnItem.INDEX);
            ixcheckbox.addActionListener(cbxlistener);
            ixcheckbox.setToolTipText(bundle.getString("ACS_AddTableColumnIndexNameA11yDesc"));
            pane.add(ixcheckbox, con);

            ixmap = ddl.getIndexMap();
            ix_uqmap = ddl.getUniqueIndexMap();
            
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
            idxcombo.setToolTipText(bundle.getString("ACS_AddTableColumnIndexNameComboBoxA11yDesc"));
            idxcombo.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AddTableColumnIndexNameComboBoxA11yName"));
            idxcombo.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddTableColumnIndexNameComboBoxA11yDesc"));
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
            checkcheckbox = new JCheckBox();
            Mnemonics.setLocalizedText(checkcheckbox, bundle.getString("AddTableColumnConstraintCheckTitle")); //NOI18N
            checkcheckbox.setName(ColumnItem.CHECK);
            checkcheckbox.addActionListener(cbxlistener);
            checkcheckbox.setToolTipText(bundle.getString("ACS_AddTableColumnCheckTitleA11yDesc"));
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
            checkfield.setToolTipText(bundle.getString("ACS_AddTableColumnCheckTextAreaA11yDesc"));
            checkfield.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AddTableColumnCheckTextAreaA11yName"));
            checkfield.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddTableColumnCheckTextAreaA11yDesc"));
            JScrollPane spane = new JScrollPane(checkfield);
            pane.add(spane, con);

            checkcheckbox.setSelected(false);
            checkcheckbox.setSelected(false);
            nullcheckbox.setSelected(true);
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
                      if (event.getSource() != DialogDescriptor.OK_OPTION) {
                          return;
                      }
                      result = validate();
                      if ( ! result ) {
                          String msg = bundle.getString(
                              "EXC_InsufficientAddColumnInfo");
                          DialogDisplayer.getDefault().notify(
                                new NotifyDescriptor.Message(msg, 
                                    NotifyDescriptor.ERROR_MESSAGE));
                          return;
                      }

                      colname = colnamefield.getText();
                      ColumnItem citem = (ColumnItem)dmodel.getData().elementAt(0);
                      String indexName = (String)idxcombo.getSelectedItem();
                      boolean wasException;
                      try {
                          wasException = ddl.execute(colname, citem, indexName);
                      } catch ( Exception e ) {
                        LOGGER.log(Level.WARNING, null, e);
                          
                        DbUtilities.reportError(bundle.getString(
                            "ERR_UnableToAddColumn"), e.getMessage());
                        return;
                      }

                      // was execution of commands with or without exception?
                      if( wasException ) {
                          return;
                      }
                      
                      // dialog is closed after successfully add column
                      dialog.setVisible(false);
                      dialog.dispose();
                  }
              };

            pane.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddTableColumnDialogA11yDesc"));
                  
            DialogDescriptor descriptor = new DialogDescriptor(pane, bundle.getString("AddColumnDialogTitle"), true, listener); //NOI18N
            // inbuilt close of the dialog is only after CANCEL button click
            // after OK button is dialog closed by hand
            Object [] closingOptions = {DialogDescriptor.CANCEL_OPTION};
            descriptor.setClosingOptions(closingOptions);
            dialog = DialogDisplayer.getDefault().createDialog(descriptor);
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
            data.setValue(cbx.isSelected() ? Boolean.TRUE : Boolean.FALSE, code, 0);
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
