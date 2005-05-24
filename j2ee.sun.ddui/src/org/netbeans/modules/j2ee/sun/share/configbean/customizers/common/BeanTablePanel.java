/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * BeanTablePanel.java
 *
 * Created on October 4, 2003, 5:35 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.util.Collection;
import java.util.Iterator;
import java.util.ResourceBundle;

import javax.swing.table.AbstractTableModel;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;


/** Generic panel containing the table and NEW - EDIT - DELETE buttons.
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */

public abstract class BeanTablePanel extends javax.swing.JPanel {
	protected javax.swing.JTable table;
	protected JButton moveUpButton, moveDownButton, sourceButton;
	private boolean reordable;
	protected BeanTableModel model;
	private boolean isSource;

	static final ResourceBundle bundle =
		ResourceBundle.getBundle(
			"org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle"); //NOI18N

	/** Creates a new BeanTablePanel.
	* @param model AbstractTableModel for included table
	*/
	public BeanTablePanel(BeanTableModel model) {
		this(model, false,  false);
	}

	/** Creates a new BeanTablePanel.
	* @param model AbstractTableModel for included table
	* @param reordable specifies whether the order of the rows is
	* important(in DD filter-mappings for example the order of elements
	* is important)
	*/
	public BeanTablePanel(BeanTableModel model, boolean reordable) {
		this(model, reordable,  false);
	}

	/** Creates a new BeanTablePanel.
	* @param model AbstractTableModel for included table
	* @param reordable specifies whether the order of the rows is important
	* (in DD filter-mappings for example the order of elements is important)
	* @param isSource specifies if there is a reasonable source file/link
	* related to the table row
	*/
	public BeanTablePanel(BeanTableModel model, final boolean reordable,
			final boolean isSource) {
		this.model=model;
		this.reordable=reordable;
		this.isSource=isSource;
		initComponents();
		table = new javax.swing.JTable();
                table.getAccessibleContext().setAccessibleName(getAccessibleName());
                table.getAccessibleContext().setAccessibleDescription(getAccessibleDesc());
                getAccessibleContext().setAccessibleName(getAccessibleName());
                getAccessibleContext().setAccessibleDescription(getAccessibleDesc());
        
		//table = new SortableTable(new SortableTableModel(model));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setModel(model);
		table.getSelectionModel().addListSelectionListener(
					new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e){
				// ignore extra messages
				if (e.getValueIsAdjusting()){
					return;
				}

				if(((ListSelectionModel)e.getSource()).isSelectionEmpty() ||
					table.getModel().getRowCount() == 0 // BUG 5030679
				  ) {
					editButton.setEnabled(false);
					removeButton.setEnabled(false);
					if (reordable) {
						moveUpButton.setEnabled(false);
						moveDownButton.setEnabled(false);
					}
					if (isSource) {
						sourceButton.setEnabled(false);
					}
				} else {
					editButton.setEnabled(true);
					removeButton.setEnabled(true);
					if (reordable) {
						int row = table.getSelectedRow();
						if (row<table.getModel().getRowCount()-1){
							moveDownButton.setEnabled(true);
						}else{
							moveDownButton.setEnabled(false);
						}
						if (row>0){
							moveUpButton.setEnabled(true);
						}else{
							moveUpButton.setEnabled(false);
						}
					}
					if (isSource) {
						sourceButton.setEnabled(true);
					}
				}
			}
		});

		jScrollPane1.setViewportView(table);
		if (reordable) {
			moveUpButton = new JButton(bundle.getString("LBL_Move_Up"));        //NOI18N
			moveDownButton = new JButton(bundle.getString("LBL_Move_Down"));    //NOI18N
			moveUpButton.setEnabled(false);
			moveDownButton.setEnabled(false);
			buttonPanel.add(moveUpButton);
			buttonPanel.add(moveDownButton);
		}
		if (isSource) {
			sourceButton = new JButton(bundle.getString("LBL_Go_To_Source"));   //NOI18N
			sourceButton.setEnabled(false);
			rightPanel.add(sourceButton);
		}

		removeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				int row = table.getSelectedRow();
				//ddView.setModelChanged(true);
				if(row >= 0) {
					getModel().removeRow(row);

					if(row >= getModel().getRowCount()) {
						--row;
					}

					if(row >= 0) {
						table.getSelectionModel().setSelectionInterval(row, row);
					}
				}
			}
		});

		editButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				displayDialog(true);
			}
		});

		addButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				displayDialog(false);
			}
		});
	}


	public void setModel(Object parent, CommonDDBean[] children) {
		model.setData(parent, children);
	}


	protected void displayDialog(boolean isEditOperation){
		int row = -1;
		BeanInputDialog  dialog = null;

		if(isEditOperation) {
			row = table.getSelectedRow();
			if(row < 0) {
				return;
			}

			Object[] values = model.getValues(row);
			dialog = getInputDialog(values);
		} else {
			dialog = getInputDialog();
		}

		do {
			int result = dialog.display();
			if(result == dialog.CANCEL_OPTION) {
				break;
			}

			if(result == dialog.OK_OPTION) {
				if (dialog.hasErrors()) {
					dialog.showErrors();
				} else {
					///ddView.setModelChanged(true);
					if(isEditOperation){
						model.editRow(row, dialog.getValues());
					} else {
						model.addRow(dialog.getValues());
					}
				}
			}
		} while (dialog.hasErrors());
	}


	private void initComponents() {
		jScrollPane1 = new javax.swing.JScrollPane();
		southPanel = new javax.swing.JPanel();
		buttonPanel = new javax.swing.JPanel();
		addButton = new javax.swing.JButton();
		editButton = new javax.swing.JButton();
		removeButton = new javax.swing.JButton();
		rightPanel = new javax.swing.JPanel();

		setLayout(new java.awt.BorderLayout());

		add(jScrollPane1, java.awt.BorderLayout.CENTER);

		southPanel.setLayout(new java.awt.BorderLayout());

		addButton.setText(bundle.getString("LBL_New")); //NOI18N
                int index = bundle.getString("LBL_New").indexOf('w');  //NOI18N
                if(index < 0) index = 0;
                addButton.setDisplayedMnemonicIndex(index);
		addButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_New"));	// NOI18N
		addButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_New"));	// NOI18N
		buttonPanel.add(addButton);

		editButton.setText(bundle.getString("LBL_Edit")); //NOI18N
                index = bundle.getString("LBL_Edit").indexOf('e');  //NOI18N
                if(index < 0) index = 0;
                editButton.setDisplayedMnemonicIndex(index);
		editButton.setEnabled(false);
		editButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_Edit"));	// NOI18N
		editButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Edit"));	// NOI18N
		buttonPanel.add(editButton);

		removeButton.setText(bundle.getString("LBL_Delete")); //NOI18N
                index = bundle.getString("LBL_Delete").indexOf('l');  //NOI18N
                if(index < 0) index = 0;
                removeButton.setDisplayedMnemonicIndex(index);
		removeButton.setEnabled(false);
		removeButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_Delete"));	// NOI18N
		removeButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Delete"));	// NOI18N
		buttonPanel.add(removeButton);

		southPanel.add(buttonPanel, java.awt.BorderLayout.CENTER);

		southPanel.add(rightPanel, java.awt.BorderLayout.EAST);

		add(southPanel, java.awt.BorderLayout.SOUTH);

	}

	private javax.swing.JPanel southPanel;
	protected javax.swing.JButton addButton;
	private javax.swing.JPanel buttonPanel;
	private javax.swing.JScrollPane jScrollPane1;
	protected javax.swing.JButton editButton;
	protected javax.swing.JButton removeButton;
	private javax.swing.JPanel rightPanel;

	abstract public BeanInputDialog getInputDialog(Object[] values);
	abstract public BeanInputDialog getInputDialog();

        protected String getAccessibleName(){
            //default value; used in case derived class does not provide one
            return bundle.getString("ACSN_Table");               //NOI18N
        }

        protected String getAccessibleDesc(){
            //default value; used in case derived class does not provide one
            return bundle.getString("ACSD_Table");               //NOI18N
        }


	public BeanTableModel getModel() {
		return model;
	}


	public void setTitle(String title) {
		javax.swing.JLabel label = new javax.swing.JLabel(title);
		label.setFont(new java.awt.Font("Dialog", 0, 14)); // NOI18N
		label.setBorder(new javax.swing.border.EmptyBorder(5,5,5,0));
		add(label, java.awt.BorderLayout.NORTH);
	}


	public void setSelectedRow(int row) {
		table.setRowSelectionInterval(row,row);
	}


	public void setButtons(boolean b1, boolean b2, boolean b3) {
		addButton.setEnabled(b1);
		editButton.setEnabled(b2);
		removeButton.setEnabled(b3);
	}


	public void setButtons(boolean b1, boolean b2, boolean b3, boolean b4,
			boolean b5, boolean b6) {
		this.setButtons(b1,b2,b3);
		moveUpButton.setEnabled(b4);
		moveDownButton.setEnabled(b5);
	}


	public boolean isReordable() {
		return reordable;
	}


	public boolean isSource() {
		return isSource;
	}
}
