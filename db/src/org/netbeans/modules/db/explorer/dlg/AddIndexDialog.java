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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.lib.ddl.*;
import org.netbeans.modules.db.explorer.nodes.DatabaseNode;
import org.netbeans.modules.db.explorer.infos.DatabaseNodeInfo;
import org.netbeans.modules.db.explorer.*;
import org.openide.awt.Mnemonics;

public class AddIndexDialog {
    boolean result = false;
    Dialog dialog = null;
    JTextField namefld;
    CheckBoxListener cbxlistener;
    JCheckBox cbx_uq;
    private static Logger LOGGER = Logger.getLogger(AddIndexDialog.class.getName());
    private final ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N

    
    public AddIndexDialog(Collection columns, final Specification spec, final DatabaseNodeInfo info) {
        try {
            JPanel pane = new JPanel();
            pane.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints con = new GridBagConstraints ();
            pane.setLayout (layout);

            // Index name

            JLabel label = new JLabel();
            Mnemonics.setLocalizedText(label, bundle.getString("AddIndexName")); //NOI18N
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddIndexNameA11yDesc"));
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 0;
            layout.setConstraints(label, con);
            pane.add(label);

            // Index name field

            con.fill = GridBagConstraints.HORIZONTAL;
            con.weightx = 1.0;
            con.gridx = 1;
            con.gridy = 0;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            namefld = new JTextField(35);
            namefld.setToolTipText(bundle.getString("ACS_AddIndexNameTextFieldA11yDesc"));
            namefld.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AddIndexNameTextFieldA11yName"));
            label.setLabelFor(namefld);
            layout.setConstraints(namefld, con);
            pane.add(namefld);

            // Unique/Non-unique

            JLabel label_uq = new JLabel(bundle.getString("AddUniqueIndex")); //NOI18N
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddUniqueIndexA11yDesc"));
            con.weightx = 0.0;
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 1;
            layout.setConstraints(label_uq, con);
            pane.add(label_uq);

            con.fill = GridBagConstraints.HORIZONTAL;
            con.weightx = 1.0;
            con.gridx = 1;
            con.gridy = 1;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            cbx_uq = new JCheckBox();
            Mnemonics.setLocalizedText(cbx_uq, bundle.getString("Unique"));
            cbx_uq.setToolTipText(bundle.getString("ACS_UniqueA11yDesc"));
            label_uq.setLabelFor(cbx_uq);
            layout.setConstraints(cbx_uq, con);
            pane.add(cbx_uq);

            // Items list title

            label = new JLabel(bundle.getString("AddIndexLabel")); //NOI18N
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddIndexLabelA11yDesc"));
            con.weightx = 0.0;
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 2;
            con.gridwidth = 2;
            layout.setConstraints(label, con);
            pane.add(label);

            // Items list

            JPanel subpane = new JPanel();
            label.setLabelFor(subpane);
            int colcount = columns.size();
            colcount = (colcount%2==0?colcount/2:colcount/2+1);
            GridLayout sublayout = new GridLayout(colcount,2);
            subpane.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            subpane.setLayout(sublayout);

            cbxlistener = new CheckBoxListener(columns);
            Iterator iter = columns.iterator();
            while(iter.hasNext()) {
                String colname = (String)iter.next();
                JCheckBox cbx = new JCheckBox(colname);
                cbx.setName(colname);
                cbx.setToolTipText(colname);
                cbx.addActionListener(cbxlistener);
                subpane.add(cbx);
            }

            con.weightx = 1.0;
            con.weighty = 1.0;
            con.gridwidth = 2;
            con.fill = GridBagConstraints.BOTH;
            con.insets = new java.awt.Insets (0, 0, 0, 0);
            con.gridx = 0;
            con.gridy = 3;
            JScrollPane spane = new JScrollPane(subpane);
            layout.setConstraints(spane, con);
            pane.add(spane);
            pane.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AddIndexDialogA11yName"));  // NOI18N
            pane.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddIndexDialogA11yDesc"));  // NOI18N
            
            final String tablename = (String)info.get(DatabaseNode.TABLE);

            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    
                    if (event.getSource() == DialogDescriptor.OK_OPTION) {
                        
                        try {
                            result = false;
                            AddIndexDDL ddl = new AddIndexDDL(spec,
                                    ((String)info.get(DatabaseNodeInfo.SCHEMA)),
                                      tablename);
                            
                            boolean wasException = ddl.execute(getIndexName(),
                                    cbx_uq.isSelected(),
                                    getSelectedColumns());

                            if (!wasException) {
                                dialog.setVisible(false);
                                dialog.dispose();
                            }
                            result = true;
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "Unable to create index", e);
                                                      
                            DbUtilities.reportError(bundle.getString(
                                "ERR_UnableToAddIndex"), e.getMessage());
                            
                            return;
                        }
                    }
                }
            };

            DialogDescriptor descriptor = new DialogDescriptor(pane, bundle.getString("AddIndexTitle"), true, listener); //NOI18N
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

    public boolean run()
    {
        if (dialog != null) dialog.setVisible(true);
        return result;
    }

    public Set getSelectedColumns()
    {
        return cbxlistener.getSelectedColumns();
    }

    public void setIndexName(String name)
    {
        namefld.setText(name);
    }

    public String getIndexName()
    {
        return namefld.getText();
    }

    class CheckBoxListener implements ActionListener
    {
        private HashSet set;

        CheckBoxListener(Collection columns)
        {
            set = new HashSet();
        }

        public void actionPerformed(ActionEvent event)
        {
            JCheckBox cbx = (JCheckBox)event.getSource();
            String name = cbx.getName();
            if (cbx.isSelected()) set.add(name);
            else set.remove(name);
        }

        public Set getSelectedColumns()
        {
            return set;
        }
    }
}
