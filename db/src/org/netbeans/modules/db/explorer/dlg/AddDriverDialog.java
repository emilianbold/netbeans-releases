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

package com.netbeans.enterprise.modules.db.explorer.dlg;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.netbeans.ide.DialogDescriptor;
import com.netbeans.ide.TopManager;
import com.netbeans.ide.util.NbBundle;

import com.netbeans.enterprise.modules.db.explorer.*;

/** 
* xxx
*
* @author Slavek Psenicka
*/
public class AddDriverDialog
{
	boolean result = false;
	Dialog dialog = null;
	String drv = null, name = null;
	JTextField namefield, drvfield;
	
	public AddDriverDialog()
	{  
		try {
			JLabel label;
			JPanel pane = new JPanel();
			pane.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
			GridBagLayout layout = new GridBagLayout();
			GridBagConstraints con = new GridBagConstraints ();
			pane.setLayout (layout);
			ResourceBundle bundle = NbBundle.getBundle("com.netbeans.enterprise.modules.db.resources.Bundle");

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
         
			// Blah blah about driver accessibility
		
			JTextArea notes = new JTextArea(bundle.getString("AddDriverURLNotes"), 2, 50);
			notes.setLineWrap(true);
			notes.setWrapStyleWord(false);
			notes.setFont(label.getFont());
			notes.setBackground(label.getBackground());
            con.weightx = 1.0;
            con.gridwidth = 2;
            con.fill = GridBagConstraints.HORIZONTAL;
			con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 2;
			layout.setConstraints(notes, con);
			pane.add(notes);
                
			ActionListener listener = new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					boolean dispcond = true;
					if (event.getSource() == DialogDescriptor.OK_OPTION) {
						result = true;
						name = namefield.getText();
						drv = drvfield.getText();
						dispcond = (drv != null && drv.length() > 0);
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
		} catch (MissingResourceException ex) {
			System.out.println("missing resource "+ex.getKey()+"("+ex+")");
		}
    }
    
    public boolean run()
    {
    	if (dialog != null) dialog.setVisible(true);
    	return result;
	}

	public DatabaseDriver getDriver()
	{
		return new DatabaseDriver((name.length() > 0 ? name : null), drv);
	}
}