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

/** 
* xxx
*
* @author Slavek Psenicka
*/
public class AddIndexDialog
{
	boolean result = false;
	Dialog dialog = null;
	JTextField namefld;
	CheckBoxListener cbxlistener;
	
	public AddIndexDialog(Collection columns)
	{  
		try {
			ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle");
			JPanel pane = new JPanel();
			pane.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
			GridBagLayout layout = new GridBagLayout();
			GridBagConstraints con = new GridBagConstraints ();
			pane.setLayout (layout);
			
			// Index name
		
			JLabel label = new JLabel(bundle.getString("AddIndexName"));
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
            layout.setConstraints(namefld, con);
            pane.add(namefld);
            
			// Items list title
		
			label = new JLabel(bundle.getString("AddIndexLabel"));
            con.weightx = 0.0;
			con.anchor = GridBagConstraints.WEST;
			con.insets = new java.awt.Insets (2, 2, 2, 2);
            con.gridx = 0;
            con.gridy = 1;
            con.gridwidth = 2;
			layout.setConstraints(label, con);
			pane.add(label);
                
			// Items list

			JPanel subpane = new JPanel();
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
	            cbx.addActionListener(cbxlistener);
           		subpane.add(cbx);
			} 

            con.weightx = 1.0;
            con.weighty = 1.0;
            con.gridwidth = 2;
            con.fill = GridBagConstraints.BOTH;
			con.insets = new java.awt.Insets (0, 0, 0, 0);
            con.gridx = 0;
            con.gridy = 2;
            JScrollPane spane = new JScrollPane(subpane);
			layout.setConstraints(spane, con);
			pane.add(spane);

			ActionListener listener = new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					boolean candismiss = false;
					if (event.getSource() == DialogDescriptor.OK_OPTION) {
						candismiss = (getIndexName().length() != 0);
						result = true;
					} else candismiss = true;
					
					if (candismiss) {
						dialog.setVisible(false);
						dialog.dispose();
					} else Toolkit.getDefaultToolkit().beep();
				}
			};				
				      
			DialogDescriptor descriptor = new DialogDescriptor(pane, bundle.getString("AddIndexTitle"), true, listener);
			dialog = TopManager.getDefault().createDialog(descriptor);
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
/*
 * <<Log>>
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         9/8/99   Slavek Psenicka adaptor changes
 *  4    Gandalf   1.3         6/15/99  Slavek Psenicka debug prints
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/21/99  Slavek Psenicka new version
 *  1    Gandalf   1.0         5/14/99  Slavek Psenicka 
 * $
 */
