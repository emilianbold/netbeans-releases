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
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.util.NbBundle;

import org.netbeans.modules.db.explorer.*;

public class AddDriverDialog {
    boolean result = false;
    Dialog dialog = null;
    String drv = null, name = null, prefix = null;
    JTextField namefield, drvfield, prefixfield;

    public AddDriverDialog()
    {
        try {
            JLabel label;
            JPanel pane = new JPanel();
            pane.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints con = new GridBagConstraints ();
            pane.setLayout (layout);
            ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle");

            // Driver name

            label = new JLabel(bundle.getString("AddDriverDriverName"));
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 0;
            layout.setConstraints(label, con);
            pane.add(label);

            con.fill = GridBagConstraints.HORIZONTAL;
            con.weightx = 1.0;
            con.gridx = 1;
            con.gridy = 0;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            namefield = new JTextField(35);
            layout.setConstraints(namefield, con);
            pane.add(namefield);

            // Driver label and field

            label = new JLabel(bundle.getString("AddDriverDriverURL"));
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 1;
            layout.setConstraints(label, con);
            pane.add(label);

            con.fill = GridBagConstraints.HORIZONTAL;
            con.weightx = 1.0;
            con.gridx = 1;
            con.gridy = 1;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            drvfield = new JTextField(35);
            layout.setConstraints(drvfield, con);
            pane.add(drvfield);

            // Database prefix title and field

            label = new JLabel(bundle.getString("AddDriverDatabasePrefix"));
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 2;
            layout.setConstraints(label, con);
            pane.add(label);

            con.fill = GridBagConstraints.HORIZONTAL;
            con.weightx = 1.0;
            con.gridx = 1;
            con.gridy = 2;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            prefixfield = new JTextField(35);
            layout.setConstraints(prefixfield, con);
            pane.add(prefixfield);

            // Blah blah about driver accessibility

            JTextArea notes = new JTextArea(bundle.getString("AddDriverURLNotes"), 2, 50);
            notes.setLineWrap(true);
            notes.setWrapStyleWord(true);
            notes.setFont(label.getFont());
            notes.setEditable(false);
            notes.setBackground(label.getBackground());
            con.weightx = 1.0;
            con.gridwidth = 2;
            con.fill = GridBagConstraints.HORIZONTAL;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 3;
            layout.setConstraints(notes, con);
            pane.add(notes);

            ActionListener listener = new ActionListener() {
                                          public void actionPerformed(ActionEvent event) {
                                              boolean dispcond = true;
                                              if (event.getSource() == DialogDescriptor.OK_OPTION) {
                                                  result = true;
                                                  name = namefield.getText();
                                                  drv = drvfield.getText();
                                                  prefix = prefixfield.getText();
                                                  if (prefix == null) prefix = "";
                                                  dispcond = (drv != null && drv.length() > 0 && name != null && name.length() > 0);
                                              } else result = false;

                                              if (dispcond) {
                                                  dialog.setVisible(false);
                                                  dialog.dispose();
                                              } else Toolkit.getDefaultToolkit().beep();
                                          }
                                      };

            DialogDescriptor descriptor = new DialogDescriptor(pane, bundle.getString("AddDriverDialogTitle"), true, listener);
            dialog = TopManager.getDefault().createDialog(descriptor);
            dialog.setResizable(false);
        } catch (MissingResourceException e) {
            e.printStackTrace();
        }
    }

    public boolean run()
    {
        if (dialog != null) dialog.setVisible(true);
        return result;
    }

    public DatabaseDriver getDriver()
    {
        return new DatabaseDriver(name, drv, prefix);
    }
}
