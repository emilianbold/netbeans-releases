/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.dlg;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.openide.util.NbBundle;

public class ConnectPanel extends JPanel {
    static ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N
    boolean remember = true;
    JTextField userfield, pwdfield;
    JCheckBox rememberbox = null;

    public ConnectPanel(String loginname)
    {
        try {
            JLabel label;
            setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints con = new GridBagConstraints ();
            setLayout (layout);
            ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N

            // Username field

            label = new JLabel(bundle.getString("ConnectDialogUserName")); //NOI18N
            label.setDisplayedMnemonic(bundle.getString("ConnectDialogUserName_Mnemonic").charAt(0));
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_ConnectDialogUserNameA11yDesc"));
            con.anchor = GridBagConstraints.WEST;
            con.weightx = 0.0;
            con.fill = GridBagConstraints.NONE;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 2;
            layout.setConstraints(label, con);
            add(label);

            con.fill = GridBagConstraints.HORIZONTAL;
            con.weightx = 1.0;
            con.gridx = 1;
            con.gridy = 2;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            userfield = new JTextField(35);
            userfield.setText(loginname);
            userfield.setToolTipText(bundle.getString("ACS_ConnectDialogUserNameTextFieldA11yDesc"));
            userfield.getAccessibleContext().setAccessibleName(bundle.getString("ACS_ConnectDialogUserNameTextFieldA11yName"));
            label.setLabelFor(userfield);
            layout.setConstraints(userfield, con);
            add(userfield);

            // Password field

            label = new JLabel(bundle.getString("ConnectDialogPassword")); //NOI18N
            label.setDisplayedMnemonic(bundle.getString("ConnectDialogPassword_Mnemonic").charAt(0));
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_ConnectDialogPasswordA11yDesc"));
            con.anchor = GridBagConstraints.WEST;
            con.weightx = 0.0;
            con.fill = GridBagConstraints.NONE;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 3;
            layout.setConstraints(label, con);
            add(label);

            con.fill = GridBagConstraints.HORIZONTAL;
            con.weightx = 1.0;
            con.gridx = 1;
            con.gridy = 3;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            pwdfield = new JPasswordField(35);
            pwdfield.setToolTipText(bundle.getString("ACS_ConnectDialogPasswordTextFieldA11yDesc"));
            pwdfield.getAccessibleContext().setAccessibleName(bundle.getString("ACS_ConnectDialogPasswordTextFieldA11yName"));
            label.setLabelFor(pwdfield);
            layout.setConstraints(pwdfield, con);
            add(pwdfield);

            // Remember password checkbox

            rememberbox = new JCheckBox(bundle.getString("ConnectDialogRememberPassword")); //NOI18N
            rememberbox.setMnemonic(bundle.getString("ConnectDialogRememberPassword_Mnemonic").charAt(0));
            rememberbox.setToolTipText(bundle.getString("ACS_ConnectDialogRememberPasswordA11yDesc"));
            con.anchor = GridBagConstraints.WEST;
            con.weightx = 0.0;
            con.fill = GridBagConstraints.NONE;
            con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 1;
            con.gridy = 4;
            layout.setConstraints(rememberbox, con);
            add(rememberbox);

        } catch (MissingResourceException e) {
            e.printStackTrace();
        }
    }

    public String getUser()
    {
        return userfield.getText();
    }

    public String getPassword()
    {
        return pwdfield.getText();
    }

    public boolean rememberPassword()
    {
        return rememberbox.isSelected();
    }
    
    public String getTitle() {
        return bundle.getString("ConnectDialogTitle"); // NOI18N
    }

}
