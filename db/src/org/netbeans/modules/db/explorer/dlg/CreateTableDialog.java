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
import javax.swing.border.*;
import javax.swing.table.*;
import com.netbeans.ide.DialogDescriptor;
import com.netbeans.ide.TopManager;
import com.netbeans.ide.util.NbBundle;
import com.netbeans.enterprise.modules.db.explorer.*;

/** 
* xxx
*
* @author Slavek Psenicka
*/
public class CreateTableDialog
{
	boolean result = false;
	Dialog dialog = null;
	JTextField dbnamefield, dbownerfield;
	JTable table;
	JComboBox ownercombo;
	JButton addbtn, delbtn;
	
	public CreateTableDialog()	
	{  
		try {
			JLabel label;
			JPanel pane = new JPanel();
			pane.setBorder(new EmptyBorder(new Insets(5,5,5,5)));
			GridBagLayout layout = new GridBagLayout();
			GridBagConstraints constr = new GridBagConstraints();
			pane.setLayout(layout);
			pane.setMinimumSize(new Dimension(400,250));
			ResourceBundle bundle = NbBundle.getBundle("com.netbeans.enterprise.modules.db.resources.Bundle");
         
			// Table name field
		
			label = new JLabel(bundle.getString("CreateTableName"));
			constr.anchor = GridBagConstraints.WEST;
            constr.weightx = 0.0;
            constr.weighty = 0.0;
            constr.fill = GridBagConstraints.NONE;
			constr.insets = new java.awt.Insets (2, 2, 2, 2);
            constr.gridx = 0;
            constr.gridy = 0;
			layout.setConstraints(label, constr);
			pane.add(label);
        
            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.weightx = 1.0;
            constr.weighty = 0.0;
            constr.gridx = 1;
            constr.gridy = 0;
			constr.insets = new java.awt.Insets (2, 2, 2, 2);
            dbnamefield = new JTextField(50);
            layout.setConstraints(dbnamefield, constr);
            pane.add(dbnamefield);
         
			// Table owner combo
		
			label = new JLabel(bundle.getString("CreateTableOwner"));
			constr.anchor = GridBagConstraints.WEST;
            constr.weightx = 0.0;
            constr.weighty = 0.0;
            constr.fill = GridBagConstraints.NONE;
			constr.insets = new java.awt.Insets (2, 10, 2, 2);
            constr.gridx = 2;
            constr.gridy = 0;
			layout.setConstraints(label, constr);
			pane.add(label);
        
            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.weightx = 0.0;
            constr.weighty = 0.0;
            constr.gridx = 3;
            constr.gridy = 0;
			constr.insets = new java.awt.Insets (2, 2, 2, 2);
            ownercombo = new JComboBox();
            layout.setConstraints(ownercombo, constr);
            pane.add(ownercombo);

			// Table columns in scrollpane

            constr.fill = GridBagConstraints.BOTH;
            constr.weightx = 1.0;
            constr.weighty = 1.0;
            constr.gridx = 0;
            constr.gridy = 1;
            constr.gridwidth = 4;
            constr.gridheight = 3;
			constr.insets = new java.awt.Insets (2, 2, 2, 2);
			table = new JTable(new DataModel());
	        JScrollPane scrollpane = new JScrollPane(table);
			scrollpane.setBorder(new BevelBorder(BevelBorder.LOWERED));
			scrollpane.setPreferredSize(new Dimension(200,150));
            layout.setConstraints(scrollpane, constr);
			pane.add(scrollpane);
       
       		// Button pane
       		
            constr.fill = GridBagConstraints.HORIZONTAL;
            constr.anchor = GridBagConstraints.NORTH;
            constr.weightx = 0.0;
            constr.weighty = 0.0;
            constr.gridx = 4;
            constr.gridy = 1;
			constr.insets = new java.awt.Insets (2, 8, 2, 2);
			JPanel btnpane = new JPanel();
			GridLayout btnlay = new GridLayout(2,1,0,5);
			btnpane.setLayout(btnlay);
            layout.setConstraints(btnpane, constr);
			pane.add(btnpane);
       
       		// Button add column
       
			addbtn = new JButton(bundle.getString("CreateTableAddButtonTitle"));
			btnpane.add(addbtn);
			addbtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					System.out.println(event.getSource());
				}
			});

       		// Button delete column
       
			delbtn = new JButton(bundle.getString("CreateTableRemoveButtonTitle"));
			btnpane.add(delbtn);
			delbtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					System.out.println(event.getSource());
				}
			});
              
			ActionListener listener = new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					boolean disres = true;
					if (event.getSource() == DialogDescriptor.OK_OPTION) {
						result = true;
						try {
						} catch (Exception e) {
							System.out.println(e);
							disres = false;
						}
					} else {
						System.out.println(event.getSource());
						result = false;
					}
					
					if (disres) {
						dialog.setVisible(false);
						dialog.dispose();
					}
				}
			};				

			DialogDescriptor descriptor = new DialogDescriptor(pane, bundle.getString("CreateTableDialogTitle"), true, listener);
			dialog = TopManager.getDefault().createDialog(descriptor);
			dialog.setResizable(true);
		} catch (MissingResourceException ex) {
			System.out.println("Missing resource "+ex.getKey());
		}
    }
    
    public boolean run()
    {
    	if (dialog != null) dialog.setVisible(true);
    	return result;
	}

	class DataModel extends AbstractTableModel
	{
        public int getColumnCount() 
        { 
        	return 10; 
        }
        public int getRowCount() 
        { 
        	return 5;
        }
        
        public Object getValueAt(int row, int col) 
        {
        	return "Cell "+row+col;
        }
        
        public String getColumnName(int column) 
        {
        	return "Column "+column;
        }
/*        
        public Class getColumnClass(int c) 
        {
        }
*/        
        public boolean isCellEditable(int row, int col) 
        {
        	return true;
        }
        
        public void setValueAt(Object val, int row, int col) 
        {
			System.out.println("set ["+row+","+col+"] = "+val);
		}
	}
}