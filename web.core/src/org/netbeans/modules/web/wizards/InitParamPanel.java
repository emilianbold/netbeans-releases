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

package org.netbeans.modules.web.wizards;

import java.awt.Dimension; 
import java.awt.GridBagConstraints; 
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent; 
import javax.swing.event.TableModelListener; 


import org.openide.util.NbBundle;

/* 
 * Wizard panel that collects deployment data for Servlets and Filters
 * @author Ana von Klopp 
 */

class InitParamPanel extends JPanel implements ActionListener, 
						ListSelectionListener { 

    private final static String ADD = "add"; 
    private final static String EDIT = "edit"; 
    private final static String REMOVE = "remove"; 
    private ServletData deployData; 
    private BaseWizardPanel parent; 
    private boolean edited = false; 

    private JLabel jLinitparams;
    private DDTable table; 
    private JButton jBnew; 
    private JButton jBedit; 
    private JButton jBdelete; 
    private JScrollPane scrollP; 

    private static final boolean debug = false; 

    private static final long serialVersionUID = -5803905591685582710L;
    
    public InitParamPanel(ServletData deployData, BaseWizardPanel parent) { 
	this.deployData = deployData; 
	this.parent = parent; 
	initComponents ();
    }

    private void initComponents () {

	// Layout description
	setLayout(new java.awt.GridBagLayout());
	setPreferredSize(new java.awt.Dimension(400, 150));

	// Entity covers entire row
	GridBagConstraints fullRowC = new GridBagConstraints();
	fullRowC.gridx = 0;
        fullRowC.gridy = 0;
	fullRowC.gridwidth = 2;
	fullRowC.anchor = GridBagConstraints.WEST;
	fullRowC.fill = GridBagConstraints.HORIZONTAL;
	fullRowC.insets = new Insets(4, 0, 4, 60);

	// Button
	GridBagConstraints bC = new GridBagConstraints();
	bC.gridx = 1;
        bC.gridy = 1;
	bC.weightx = 0.1;
	bC.fill = GridBagConstraints.HORIZONTAL;
	bC.insets = new Insets(4, 20, 4, 60);

	// Table panel 
	GridBagConstraints tableC = new GridBagConstraints();
	tableC.gridx = 0;
        tableC.gridy = 1; 
	tableC.gridheight = 4;
	tableC.fill = GridBagConstraints.BOTH; 
	tableC.weightx = 0.9;
	tableC.weighty = 1.0; 
	tableC.anchor = GridBagConstraints.WEST; 
	tableC.insets = new Insets(4, 0, 4, 0);

	// Filler panel 
	GridBagConstraints fillerC = new GridBagConstraints();
	fillerC.gridx = 1;
        fillerC.gridy = GridBagConstraints.RELATIVE; 
	fillerC.fill = GridBagConstraints.BOTH; 
	fillerC.weighty = 1.0; 
	fillerC.insets = new Insets(4, 0, 4, 0);
        
	// Component Initialization by row
	// 1. Init parameter
	jLinitparams = new JLabel(NbBundle.getMessage(InitParamPanel.class, "LBL_initparamsL"));
	jLinitparams.setDisplayedMnemonic(NbBundle.getMessage (InitParamPanel.class, "LBL_initparams_mnemonic").charAt(0));
	// PENDING 
	this.add(jLinitparams, fullRowC); 

	// 2. Table row

	String[] headers = { "paramname", "paramvalue" };
	table = new DDTable(headers, 
			    "LBL_initparams", 
			    Editable.BOTH); 

	jLinitparams.setLabelFor(table);

	// Enable the buttons according to the row selected
	table.getSelectionModel().addListSelectionListener(this); 
	table.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InitParamPanel.class, "ACSD_initparams_desc")); // NOI18N
	table.getAccessibleContext().setAccessibleName(NbBundle.getMessage(InitParamPanel.class, "ACSD_initparams")); // NOI18N


	table.getModel().addTableModelListener(new TableModelListener() { 
	    public void tableChanged(TableModelEvent evt) {
		if(debug) log("\ttable model changed"); //NOI18N
		updateInitParams();
		//table.revalidate(); 
		//scrollP.revalidate(); 
		//scrollP.repaint(); 
	    }}); 

	scrollP = new JScrollPane(table); 
	table.setPreferredScrollableViewportSize(new Dimension(300, 200));
	this.add(scrollP, tableC); 

	jBnew = new JButton(); 
	jBnew.setText(NbBundle.getMessage(InitParamPanel.class, 
					  "LBL_new")); 
	jBnew.setMnemonic(NbBundle.getMessage(InitParamPanel.class, "LBL_new_mnemonic").charAt(0));
	jBnew.addActionListener(this); 
	jBnew.setActionCommand(ADD); 
	jBnew.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InitParamPanel.class, "ACSD_initparam_new")); // NOI18N
	this.add(jBnew, bC); 

	bC.gridy++; 
	jBedit = new JButton(); 
	jBedit.setText(NbBundle.getMessage(InitParamPanel.class,
					     "LBL_edit")); 
	jBedit.setMnemonic(NbBundle.getMessage(InitParamPanel.class, "LBL_edit_mnemonic").charAt(0));
	jBedit.addActionListener(this);
	jBedit.setActionCommand(EDIT); 
	jBedit.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InitParamPanel.class, "ACSD_initparam_edit")); // NOI18N
	jBedit.setEnabled(false); 
	this.add(jBedit, bC); 

	bC.gridy++; 
	jBdelete = new JButton(); 
	jBdelete.setText(NbBundle.getMessage(InitParamPanel.class,
					     "LBL_delete")); 
	jBdelete.setMnemonic(NbBundle.getMessage(InitParamPanel.class, "LBL_delete_mnemonic").charAt(0));
	jBdelete.addActionListener(this);
	jBdelete.setActionCommand(REMOVE); 
	jBdelete.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(InitParamPanel.class, "ACSD_initparam_delete")); // NOI18N
	jBdelete.setEnabled(false); 
	this.add(jBdelete, bC);
        
        this.add(new javax.swing.JPanel(),fillerC);

    }

    public void setEnabled() { 
	
	boolean enable = deployData.makeEntry(); 
    
	jLinitparams.setEnabled(enable);
	jBnew.setEnabled(enable); 
	if(enable) { 
	    if(debug) { 
		log("\tnumRows: " + table.getRowCount()); //NOI18N
		log("\tselected row: " + table.getSelectedRow()); //NOI18N
	    }
	    ListSelectionModel lsm = table.getSelectionModel(); 
	    if (lsm.isSelectionEmpty()) {
		jBdelete.setEnabled(false); 
		jBedit.setEnabled(false); 
	    }
	    else { 
		jBdelete.setEnabled(true);
		jBedit.setEnabled(true);  
	    }
	}
	else { 
	    jBdelete.setEnabled(false); 
	    jBedit.setEnabled(false); 
	}
	table.setEditable(enable ? Editable.BOTH : Editable.NEITHER); 
    } 

    public void actionPerformed(ActionEvent evt) { 

	if(debug) log("::actionPerformed()"); //NOI18n
	int row = -1; 
	if(evt.getSource() instanceof JButton) { 
	    if(evt.getActionCommand() == ADD) { 
		if(debug) log("\tAdding row"); //NOI18N
		String[] values= { 
		    NbBundle.getMessage(InitParamPanel.class, "LBL_paramname"), 
		    NbBundle.getMessage(InitParamPanel.class, "LBL_paramvalue"), 
		}; 
		row = table.addRow(values); 
		table.setRowSelectionInterval(row, row);
		if(debug) log("\tAdded row " + row); 
	    } 
	    else if (evt.getActionCommand() == REMOVE) { 
		if(debug) log("\tDeleting row"); //NOI18N
		row = table.getSelectedRow(); 
		table.removeRow(row); 
                setEnabled();
	    }
	    else if (evt.getActionCommand() == EDIT) { 
		if(debug) log("\tEditing row"); //NOI18N
		//table.stopEditing(); 
		row = table.getSelectedRow(); 
		String name = (String)(table.getValueAt(row, 0)); 
		String value = (String)(table.getValueAt(row, 1)); 
		String title =  NbBundle.getMessage(DDTable.class, 
						    "LBL_initparams_edit"); //NOI18N
		TableRowDialog d =
		    new TableRowDialog(name, value, Editable.BOTH, 
				       TableRowDialog.Condition.NONE, 
				       title);
		d.showDialog();
		if(d.getDialogOK()) {
		    if(debug) log("Dialog is OK"); 
		    table.setData(d.getName(), d.getValue(), row); 
		}
		else 
		    table.setData(name, value, row); 
	    }
	    scrollP.revalidate(); 
	} 
    }

    public void valueChanged(ListSelectionEvent e) {
	if(debug) log("::valueChanged()"); //NOI18N
	//Ignore extra messages.
	if (e.getValueIsAdjusting()) return;
	setEnabled(); 
	updateInitParams(); 
    } 

    private void updateInitParams() { 

	if(debug) log("::updateInitParams()"); 
	edited = true; 

	if(deployData.makeEntry()) { 

	    if(debug) { 
		log("\tnumRows: " + table.getRowCount()); //NOI18N
		log("\tselected row: " + table.getSelectedRow()); //NOI18N
	    }

	    int numInitParams = table.getRowCount(); 
	    String[][] param = new String[numInitParams][2]; 

	    if(debug) log("\tnum params " + //NOI18N
			  String.valueOf(numInitParams)); 

	    boolean isOK = true; 
	    for(int i=0; i<numInitParams; ++i) { 
		param[i][0] = (String)(table.getModel().getValueAt(i,0)); 
		if(debug) log("\tname is " + param[i][0]); //NOI18N
		if(param[i][0].length() == 0) isOK = false; 
		param[i][1] = (String)(table.getModel().getValueAt(i,1)); 
		if(debug) log("\tname is " + param[i][1]); //NOI18N
	    } 
	    deployData.setInitParams(param, isOK); 
	    parent.fireChangeEvent();
	}
    }	    
    
    private void log(String s) { 
	System.out.println("InitParamPanel" + s); 
    }
}