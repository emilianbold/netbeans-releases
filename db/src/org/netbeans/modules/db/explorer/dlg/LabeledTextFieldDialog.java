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
	
	public LabeledTextFieldDialog(String title, String lab, String notes)
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
                
			// Textfield
		
            con.fill = GridBagConstraints.HORIZONTAL;
            con.weightx = 1.0;
            con.gridx = 1;
            con.gridy = 0;
			con.insets = new java.awt.Insets (2, 2, 2, 2);
            field = new JTextField(35);
            layout.setConstraints(field, con);
            pane.add(field);
            
            // Notes
            
			JTextArea notesarea = new JTextArea(notes, 5, 50);
			notesarea.setEditable(false);
			notesarea.setLineWrap(true);
			notesarea.setWrapStyleWord(true);
			notesarea.setFont(label.getFont());
			notesarea.setBackground(label.getBackground());
            con.weightx = 1.0;
            con.weighty = 1.0;
            con.gridwidth = 2;
            con.fill = GridBagConstraints.BOTH;
			con.insets = new java.awt.Insets (10, 0, 0, 0);
            con.gridx = 0;
            con.gridy = 1;
			notesarea.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
            JScrollPane spane = new JScrollPane(notesarea);
			layout.setConstraints(spane, con);
			pane.add(spane);
                
			ActionListener listener = new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					boolean dispcond = true;
					if (event.getSource() == DialogDescriptor.OK_OPTION) {
						result = true;
						dispcond = (getStringValue().length()!=0);
					} else result = false;
					
					if (dispcond) {
						dialog.setVisible(false);
						dialog.dispose();
					} else Toolkit.getDefaultToolkit().beep();
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
