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
import org.openide.util.NbBundle;
import javax.swing.border.EmptyBorder;
import org.openide.DialogDescriptor;
import org.openide.TopManager;

import org.netbeans.modules.db.explorer.*;

/**
* xxx
*
* @author Slavek Psenicka
*/
public class LabeledTextFieldDialog
{
    boolean result = false;
    Dialog dialog = null;
    Object combosel = null;
    JTextField field;
    JTextArea notesarea;
    JButton edButton;
    JLabel label;
    final String original_notes;
    static private ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N

    public LabeledTextFieldDialog(String title, String lab, String notes)
    {
        original_notes = notes;
        try {
            JPanel pane = new JPanel();
            pane.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints con = new GridBagConstraints ();
            pane.setLayout (layout);

            // Title

            label = new JLabel(lab);
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 0;
            layout.setConstraints(label, con);
            pane.add(label);

            // Textfield

            con.fill = GridBagConstraints.HORIZONTAL;
            con.weightx = 1.0;
            con.gridx = 1;
            con.gridy = 0;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            field = new JTextField(35);
            layout.setConstraints(field, con);
            pane.add(field);

            // Descr.

            JLabel desc = new JLabel(bundle.getString("RecreateTableRenameNotes")); // NOI18N
            con.anchor = GridBagConstraints.WEST;
            con.gridx = 0;
            con.gridy = 2;
            con.weighty = 2.0;
            layout.setConstraints(desc, con);
            pane.add(desc);

            // Notes

            notesarea = new JTextArea(notes, 10, 50);
            notesarea.setEditable(false);
            notesarea.setLineWrap(true);
            notesarea.setWrapStyleWord(true);
            notesarea.setFont(label.getFont());
            notesarea.setBackground(label.getBackground()); // grey
            con.weightx = 1.0;
            con.weighty = 1.0;
            con.gridwidth = 2;
            con.fill = GridBagConstraints.BOTH;
            con.insets = new java.awt.Insets (10, 0, 0, 0);
            con.gridx = 0;
            con.gridy = 3;
            notesarea.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            JScrollPane spane = new JScrollPane(notesarea);
            layout.setConstraints(spane, con);
            pane.add(spane);
            
            // edit button
            edButton = new JButton(bundle.getString("EditCommand")); // NOI18N
            con.fill = GridBagConstraints.WEST;
            con.weighty = 0.0;
            con.weightx = 0.0;
            con.gridx = 0;
            con.gridy = 5;
            layout.setConstraints(edButton, con);
            pane.add(edButton);

            edButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if(edButton.getText().startsWith(bundle.getString("EditCommand"))) { // NOI18N
                        // set to edit 
                        edButton.setText(bundle.getString("ReloadCommand")); // NOI18N
                        notesarea.setEditable( true );
                        notesarea.setBackground(field.getBackground()); // white
                        notesarea.requestFocus();
                        field.setEditable( false );
                        field.setBackground(label.getBackground()); // grey
                    } else {
                        // reload script from file
                        edButton.setText(bundle.getString("EditCommand")); // NOI18N
                        notesarea.setText(original_notes);
                        notesarea.setEditable( false );
                        field.setEditable( true );
                        field.setBackground(notesarea.getBackground()); // grey
                        notesarea.setBackground(label.getBackground()); // white
                        field.requestFocus();
                    }
                }
            });

            ActionListener listener = new ActionListener() {
                                          public void actionPerformed(ActionEvent event) {
                                              if (event.getSource() == DialogDescriptor.OK_OPTION) {
                                                  result = true;
                                              } else
                                                  result = false;;
                                          }
                                      };

            DialogDescriptor descriptor = new DialogDescriptor(pane, title, true, listener);
            dialog = TopManager.getDefault().createDialog(descriptor);
            dialog.setResizable(true);
        } catch (MissingResourceException ex) {
            ex.printStackTrace();
        }
    }

    public boolean run()
    {
        if (dialog != null) dialog.setVisible(true);
        return result;
    }

    public String getStringValue()
    {
        return field.getText();
    }

    public String getEditedCommand()
    {
        return notesarea.getText();
    }

    public boolean isEditable()
    {
        return notesarea.isEditable();
    }

    public void setStringValue(String val)
    {
        field.setText(val);
    }
}
/*
 * <<Log>>
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         9/8/99   Slavek Psenicka adaptor changes
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/21/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         5/14/99  Slavek Psenicka 
 * $
 */
