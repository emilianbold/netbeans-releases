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
import java.text.MessageFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.util.NbBundle;

public class SchemaPanel extends JPanel {
    boolean result = false;
    Dialog dialog;
    JComboBox schemas;
    String schema;
    JTextArea comment;

    public SchemaPanel(Vector items, String user) {
        try {
            JLabel label;
            setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            GridBagLayout layout = new GridBagLayout();
            GridBagConstraints con = new GridBagConstraints ();
            setLayout (layout);
            ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N

            label = new JLabel(bundle.getString("SchemaDialogText")); //NOI18N
            con.gridy = 1;
            con.insets = new Insets(12, 12, 0, 11);
            con.anchor = GridBagConstraints.WEST;
            layout.setConstraints(label, con);
            add(label);

            con = new GridBagConstraints();
            con.gridy = 1;
            con.fill = GridBagConstraints.HORIZONTAL;
            con.insets = new Insets(12, 12, 0, 11);
            con.weightx = 1.0;
            schemas = new JComboBox();
            setSchemas(items, user);
            layout.setConstraints(schemas, con);
            add(schemas);

            comment = new JTextArea();
            comment.setLineWrap(true);
            comment.setEditable(false);
            comment.setBackground(getBackground());
            comment.setWrapStyleWord(true);
            comment.setFont(label.getFont());

            String commentMsg = bundle.getString("MSG_SchemaPanelComment"); //NOI18N
            comment.setText(commentMsg);

            con = new GridBagConstraints();
            con.gridx = 0;
            con.gridy = 0;
            con.gridwidth = 2;
            con.fill = GridBagConstraints.BOTH;
            con.insets = new Insets(12, 12, 11, 11);
            con.weightx = 1.0;
            con.weighty = 1.0;

            layout.setConstraints(comment, con);
            add(comment);
            
            JLabel gap = new JLabel();
            con = new GridBagConstraints();
            con.gridx = 0;
            con.gridy = 2;
            con.fill = GridBagConstraints.BOTH;
            con.insets = new Insets(12, 12, 0, 0);
            con.weightx = 1.0;
            con.weighty = 1.0;

            layout.setConstraints(gap, con);
            add(gap);

        } catch (MissingResourceException e) {
            System.out.println(e.getMessage());;
        }
    }

    public String getSchema() {
        if(schemas.getSelectedItem()!=null) {
            return schemas.getSelectedItem().toString();
        }
        else {
            return null;
        }
    }
    
    public boolean setSchemas(Vector items, String schema) {
        schemas.removeAllItems();
        for(int i=0;i<items.size();i++){
            schemas.addItem(items.elementAt(i));
        }
        if((items.size()==0)||(items.size()==1)){
            // schema in the items is not or is only one
            return true;
        }
        int idx = items.indexOf(schema);
        if (idx == -1)
            idx = items.indexOf(schema.toLowerCase());
        if (idx == -1)
            idx = items.indexOf(schema.toUpperCase());
        if (idx != -1)
            schemas.setSelectedIndex(idx);
        if (idx == 1)
            // schema has been found in the items
            return true;
        // schema has not been found in the items then index is set at first item
        return false;
    }

    public void setComment(String msg) {
        comment.setText(msg);
    }
}
