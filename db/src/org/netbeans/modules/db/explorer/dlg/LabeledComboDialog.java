/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.dlg;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

import org.netbeans.modules.db.explorer.*;

/**
* @author Slavek Psenicka
*/
public class LabeledComboDialog {
    boolean result = false;
    Dialog dialog = null;
    Object combosel = null;
    JComboBox combo;

    public LabeledComboDialog(String title, String lab, Collection items) {
        try {
            JPanel pane = new JPanel();
            pane.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints con = new GridBagConstraints ();
            pane.setLayout (layout);

            ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N

            pane.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddToIndexDialogA11yDesc")); //NOI18N
            
            // Title

            JLabel label = new JLabel(lab);
            con.anchor = GridBagConstraints.WEST;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 0;
            layout.setConstraints(label, con);
            pane.add(label);

            // Combo

            con.fill = GridBagConstraints.HORIZONTAL;
            con.weightx = 1.0;
            con.gridx = 1;
            con.gridy = 0;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            combo = new JComboBox((items instanceof Vector) ? (Vector)items : new Vector(items));
            combo.getAccessibleContext().setAccessibleName(bundle.getString("ACS_AddToIndexComboA11yName")); //NOI18N
            combo.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_AddToIndexComboA11yDesc")); //NOI18N
            combo.setToolTipText(bundle.getString("ACS_AddToIndexComboA11yDesc")); //NOI18N
            layout.setConstraints(combo, con);
            pane.add(combo);

            ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    boolean dispcond = true;
                    if (event.getSource() == DialogDescriptor.OK_OPTION) {
                        result = true;
                        combosel = combo.getSelectedItem();
                    } else
                        result = false;

                    if (dispcond) {
                        dialog.setVisible(false);
                        dialog.dispose();
                    } else
                        Toolkit.getDefaultToolkit().beep();
                }
            };

            DialogDescriptor descriptor = new DialogDescriptor(pane, title, true, listener);
            dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setResizable(false);
        } catch (MissingResourceException ex) {
            ex.printStackTrace();
        }
    }

    public boolean run() {
        if (dialog != null)
            dialog.setVisible(true);
        
        return result;
    }

    public Object getSelectedItem() {
        return combosel;
    }
}
