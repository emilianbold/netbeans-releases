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
import org.openide.DialogDescriptor;
import org.openide.TopManager;

import com.netbeans.enterprise.modules.db.explorer.*;

/** 
* xxx
*
* @author Slavek Psenicka
*/
public class LabeledComboDialog
{
	boolean result = false;
	Dialog dialog = null;
	Object combosel = null;
	JComboBox combo;
	
	public LabeledComboDialog(String title, String lab, Collection items)
	{  
		try {
			JPanel pane = new JPanel();
			pane.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
			GridBagLayout layout = new GridBagLayout();
			GridBagConstraints con = new GridBagConstraints ();
			pane.setLayout (layout);

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
            layout.setConstraints(combo, con);
            pane.add(combo);
                
			ActionListener listener = new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					boolean dispcond = true;
					if (event.getSource() == DialogDescriptor.OK_OPTION) {
						result = true;
						combosel = combo.getSelectedItem();
					} else result = false;
					
					if (dispcond) {
						dialog.setVisible(false);
						dialog.dispose();
					} else Toolkit.getDefaultToolkit().beep();
				}
			};				
				      
			DialogDescriptor descriptor = new DialogDescriptor(pane, title, true, listener);
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

	public Object getSelectedItem()
	{
		return combosel;
	}
}