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
import org.netbeans.lib.ddl.*;
import org.openide.util.NbBundle;
import org.netbeans.modules.db.explorer.*;

public class NewConnectionPanel extends JPanel {
    static ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N
    DatabaseConnection con;
    boolean result = false;
    Dialog dialog = null;
    JComboBox drvfield;
    JTextField dbfield, userfield, drvClassField;
    JPasswordField pwdfield;
    JCheckBox rememberbox;

    public NewConnectionPanel(Vector drivervec,String driver,String database,String loginname)
    {
        this(drivervec, new DatabaseConnection(driver, database, loginname, null));
    }

    public NewConnectionPanel(Vector drivervec, DatabaseConnection xcon)
    {
        con = (DatabaseConnection)xcon;
        try {
            JLabel label;
            setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints constr = new GridBagConstraints ();
            setLayout (layout);
            ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N

            // Driver name field

            label = new JLabel(bundle.getString("NewConnectionDriverName")); //NOI18N
            label.setDisplayedMnemonic(bundle.getString("NewConnectionDriverName_Mnemonic").charAt(0));
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_NewConnectionDriverNameA11yDesc"));
            constr.anchor = GridBagConstraints.WEST;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            constr.gridx = 0;
            constr.gridy = 0;
            layout.setConstraints(label, constr);
            add(label);

            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.weightx = 1.0;
            constr.gridx = 1;
            constr.gridy = 0;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            drvfield = new JComboBox(drivervec);
            drvfield.setEditable(false);
            drvfield.setToolTipText(bundle.getString("ACS_NewConnectionDriverNameComboBoxA11yDesc"));
            drvfield.getAccessibleContext().setAccessibleName(bundle.getString("ACS_NewConnectionDriverNameComboBoxA11yName"));
            label.setLabelFor(drvfield);
            drvfield.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                   JComboBox combo = (JComboBox)e.getSource();
                   Object drv = combo.getSelectedItem();
                   String dbprefix = null;
                   String driver = null;
                   if (drv != null && drv instanceof DatabaseDriver) {
                       dbprefix = ((DatabaseDriver)drv).getDatabasePrefix();
                       driver = ((DatabaseDriver)drv).getURL();
                   }
                   if (dbprefix != null)
                       dbfield.setText(dbprefix);
                   if (driver != null)
                       drvClassField.setText(driver);
               }
           });

            layout.setConstraints(drvfield, constr);
            add(drvfield);
            
            // Driver class field

            label = new JLabel(bundle.getString("NewConnectionDriverClass")); //NOI18N
            label.setDisplayedMnemonic(bundle.getString("NewConnectionDriverClass_Mnemonic").charAt(0));
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_NewConnectionDriverClassA11yDesc"));
            constr.anchor = GridBagConstraints.WEST;
            constr.weightx = 0.0;
            constr.fill = GridBagConstraints.NONE;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            constr.gridx = 0;
            constr.gridy = 1;
            layout.setConstraints(label, constr);
            add(label);

            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.weightx = 1.0;
            constr.gridx = 1;
            constr.gridy = 1;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            drvClassField = new JTextField(35);
            drvClassField.setText(xcon.getDriver());
            drvClassField.setEditable(false);
            drvClassField.setToolTipText(bundle.getString("ACS_NewConnectionDriverClassComboBoxA11yDesc"));
            drvClassField.getAccessibleContext().setAccessibleName(bundle.getString("ACS_NewConnectionDriverClassComboBoxA11yName"));
            label.setLabelFor(drvClassField);
            layout.setConstraints(drvClassField, constr);
            add(drvClassField);

            // Database field

            label = new JLabel(bundle.getString("NewConnectionDatabaseURL")); //NOI18N
            label.setDisplayedMnemonic(bundle.getString("NewConnectionDatabaseURL_Mnemonic").charAt(0));
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_NewConnectionDatabaseURLA11yDesc"));
            constr.anchor = GridBagConstraints.WEST;
            constr.weightx = 0.0;
            constr.fill = GridBagConstraints.NONE;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            constr.gridx = 0;
            constr.gridy = 2;
            layout.setConstraints(label, constr);
            add(label);

            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.weightx = 1.0;
            constr.gridx = 1;
            constr.gridy = 2;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            dbfield = new JTextField(35);
            dbfield.setText(xcon.getDatabase());
            dbfield.setToolTipText(bundle.getString("ACS_NewConnectionDatabaseURLTextFieldA11yDesc"));
            dbfield.getAccessibleContext().setAccessibleName(bundle.getString("ACS_NewConnectionDatabaseURLTextFieldA11yName"));
            label.setLabelFor(dbfield);
            layout.setConstraints(dbfield, constr);
            add(dbfield);

            // Setup driver if found

            String drv = xcon.getDriver();
            String drvname = xcon.getDriverName();
            if (drv != null && drvname != null) {

                for (int i = 0; i < drivervec.size(); i++) {
                    DatabaseDriver dbdrv = (DatabaseDriver)drivervec.elementAt(i);
                    if (dbdrv.getURL().equals(drv) && dbdrv.getName().equals(drvname)) {
                        drvfield.setSelectedIndex(i);
                    }
                }
            }

            // Username field

            label = new JLabel(bundle.getString("NewConnectionUserName")); //NOI18N
            label.setDisplayedMnemonic(bundle.getString("NewConnectionUserName_Mnemonic").charAt(0));
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_NewConnectionUserNameA11yDesc"));
            constr.anchor = GridBagConstraints.WEST;
            constr.weightx = 0.0;
            constr.fill = GridBagConstraints.NONE;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            constr.gridx = 0;
            constr.gridy = 3;
            layout.setConstraints(label, constr);
            add(label);

            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.weightx = 1.0;
            constr.gridx = 1;
            constr.gridy = 3;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            userfield = new JTextField(35);
            userfield.setText(xcon.getUser());
            userfield.setToolTipText(bundle.getString("ACS_NewConnectionUserNameTextFieldA11yDesc"));
            userfield.getAccessibleContext().setAccessibleName(bundle.getString("ACS_NewConnectionUserNameTextFieldA11yName"));
            label.setLabelFor(userfield);
            layout.setConstraints(userfield, constr);
            add(userfield);

            // Password field

            label = new JLabel(bundle.getString("NewConnectionPassword")); //NOI18N
            label.setDisplayedMnemonic(bundle.getString("NewConnectionPassword_Mnemonic").charAt(0));
            label.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_NewConnectionPasswordA11yDesc"));
            constr.anchor = GridBagConstraints.WEST;
            constr.weightx = 0.0;
            constr.fill = GridBagConstraints.NONE;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            constr.gridx = 0;
            constr.gridy = 4;
            layout.setConstraints(label, constr);
            add(label);

            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.weightx = 1.0;
            constr.gridx = 1;
            constr.gridy = 4;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            pwdfield = new JPasswordField(35);
            pwdfield.setToolTipText(bundle.getString("ACS_NewConnectionPasswordTextFieldA11yDesc"));
            pwdfield.getAccessibleContext().setAccessibleName(bundle.getString("ACS_NewConnectionPasswordTextFieldA11yName"));
            label.setLabelFor(pwdfield);
            layout.setConstraints(pwdfield, constr);
            add(pwdfield);

            // Remember password checkbox

            rememberbox = new JCheckBox(bundle.getString("NewConnectionRememberPassword")); //NOI18N
            rememberbox.setMnemonic(bundle.getString("NewConnectionRememberPassword_Mnemonic").charAt(0));
            rememberbox.setToolTipText(bundle.getString("ACS_NewConnectionRememberPasswordA11yDesc"));
            constr.anchor = GridBagConstraints.WEST;
            constr.weightx = 0.0;
            constr.fill = GridBagConstraints.NONE;
            constr.insets = new java.awt.Insets (2, 2, 2, 2);
            constr.gridx = 1;
            constr.gridy = 5;
            layout.setConstraints(rememberbox, constr);
            add(rememberbox);

        } catch (MissingResourceException ex) {
            ex.printStackTrace();
        }
    }

    String getSelectedDriver() {
        String drvval;
        int idx = drvfield.getSelectedIndex();
        if (idx != -1) {
          drvval = ((DatabaseDriver)drvfield.getItemAt(idx)).getURL();
        } else drvval = (String)drvfield.getSelectedItem();
        return drvval;
    }

    public void setConnectionInfo() {
        String drvval, pwd;
        con.setDriver(getSelectedDriver());
        con.setDatabase(dbfield.getText());
        con.setUser(userfield.getText());
        String tmppwd = new String(pwdfield.getPassword());
        if (tmppwd.length() > 0) pwd = tmppwd;
        else pwd = null;
        con.setPassword(pwd);
        con.setRememberPassword(rememberbox.isSelected());
    }
    
    public DBConnection getConnection()
    {
        return con;
    }

    public String getDriver()
    {
        return getSelectedDriver();
    }

    public String getDatabase()
    {
        return dbfield.getText();
    }

    public String getUser()
    {
        return userfield.getText();
    }

    public String getPassword()
    {
        String pword;
        String tmppwd = new String(pwdfield.getPassword());
        if (tmppwd.length() > 0) pword = tmppwd;
        else pword = null;
        return pword;
    }

    public boolean rememberPassword()
    {
        return rememberbox.isSelected();
    }
    
    public String getTitle() {
        return bundle.getString("NewConnectionDialogTitle"); // NOI18N
    }
}
