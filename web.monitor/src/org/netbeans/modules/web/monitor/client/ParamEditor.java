/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 * ParamEditor.java
 *
 *
 * Created: Thursday Feb  15 
 *
 * @author Simran Gleason
 * @author Ana von Klopp 
 * @version
 */


package org.netbeans.modules.web.monitor.client; 

import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.*;

import org.openide.TopManager;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;

import org.openide.util.NbBundle;

public class ParamEditor extends javax.swing.JPanel implements ActionListener {

    private final static boolean debug = false;
    
    private static final Dimension size = new Dimension(400, 300);
    private static final Dimension valueSize = new Dimension(380, 100);

    // Do we need this to close it?
    private Dialog dialog = null;
    private DialogDescriptor editDialog = null;
    private boolean inputOK = false;

    //
    // Hold the name and value until the widgets are created.
    //
    private boolean editable = true;
    private boolean nameEditable;
    private boolean valueRequired = true;
    private String name = ""; //NOI18N
    private String value = ""; //NOI18N
    
    //
    // Widgets
    //
    private JTextField nameText;
    private JTextArea  valueText;

    private String title = ""; //NOI18N

    //private static boolean repainting = false;
    private boolean repainting = false;

    public ParamEditor(String name, 
		       String value, 
		       boolean nameEditable,
		       boolean valueEditable,
		       String title) { 
	this(name, value, nameEditable, valueEditable, title, true); 
    }

    public ParamEditor(String name, 
		       String value, 
		       boolean nameEditable,
		       boolean valueEditable,		       String title, 
		       boolean valueRequired) { 
	super();
	if(valueEditable) { 
	    editable = true;
	    this.nameEditable = nameEditable;
	    this.valueRequired = valueRequired;
	}
	else { 
	    editable = false;
	    this.nameEditable = false;
	    this.valueRequired = false;
	}
	this.title = title;
	setName(name);
	setValue(value);
	createPanel();
    }

    public boolean getDialogOK() {
	return inputOK; 
    }

    public String getName() {
	
	if(debug) System.out.println("Value of name text field: "); //NOI18N
	if(debug) System.out.println(nameText.getText().trim()); //NOI18N
	return nameText.getText().trim();
    }
    public void setName(String val) {
	name = val;
	if (nameText != null) {
	    nameText.setText(val);
	}
    }

    public String getValue() {
	if(debug) System.out.println("Value of value textarea: "); //NOI18N
	if(debug) System.out.println(valueText.getText().trim()); //NOI18N
	return valueText.getText().trim();
    }
    public void setValue(String val) {
	value = val;
	if (valueText != null) {
	    valueText.setText(val);
	}
    }
	
    
    public void createPanel() {

	if(debug) System.out.println("in (new) ParamEditor.createPanel()"); //NOI18N

	this.setLayout(new GridBagLayout());
	int gridy = -1;
	Insets zeroInsets = new Insets(0,0,0,0);
	Insets textInsets = new Insets(0,5,0,5);
	int fullGridWidth = java.awt.GridBagConstraints.REMAINDER;

        JLabel nameLabel = new JLabel(NbBundle.getBundle(ParamEditor.class).getString("MON_Name"));
        nameLabel.setDisplayedMnemonic(NbBundle.getBundle(ParamEditor.class).getString("MON_Name_Mnemonic").charAt(0));
        nameLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ParamEditor.class).getString("ACS_MON_NameA11yDesc"));
	addGridBagComponent(this, nameLabel, 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    zeroInsets,
			    0, 0);
	
	nameText = new JTextField();
        nameLabel.setLabelFor(nameText);
        nameText.getAccessibleContext().setAccessibleName(NbBundle.getBundle(ParamEditor.class).getString("ACS_MON_NameTextFieldA11yName"));
        nameText.setToolTipText(NbBundle.getBundle(ParamEditor.class).getString("ACS_MON_NameTextFieldA11yDesc"));
	nameText.setText(name);
	nameText.setBackground(java.awt.Color.white);
	nameText.setEditable(nameEditable);
	addGridBagComponent(this, nameText, 0, ++gridy,
			    fullGridWidth, 1, 0, 0,
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.HORIZONTAL,
			    textInsets,
			    0, 0);

        JLabel valueLabel = new JLabel(NbBundle.getBundle(ParamEditor.class).getString("MON_Value"));
        valueLabel.setDisplayedMnemonic(NbBundle.getBundle(ParamEditor.class).getString("MON_Value_Mnemonic").charAt(0));
        valueLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ParamEditor.class).getString("ACS_MON_ValueA11yDesc"));
	addGridBagComponent(this, valueLabel, 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    zeroInsets,
			    0, 0);

	valueText = new JTextArea();
	valueLabel.setLabelFor(valueText);
        valueText.getAccessibleContext().setAccessibleName(NbBundle.getBundle(ParamEditor.class).getString("ACS_MON_ValueTextAreaA11yName"));
        valueText.setToolTipText(NbBundle.getBundle(ParamEditor.class).getString("ACS_MON_ValueTextAreaA11yDesc"));
	valueText.setText(value);
	if(!editable) {
	    valueText.setEditable(false);
	    valueText.setLineWrap(true);
	    //valueText.setColumns(40);
	    valueText.setWrapStyleWord(false); 
	}
	
	JScrollPane scrollpane = new JScrollPane(valueText);
	scrollpane.setMinimumSize(valueSize);

	addGridBagComponent(this, scrollpane, 0, ++gridy,
			    fullGridWidth, 1, 1.0, 1.0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.BOTH,
			    textInsets,
			    0, 0);

	// Housekeeping
	this.setMaximumSize(this.getPreferredSize()); 
	this.repaint();
    }

    public void showDialog() {
	boolean modal = true;
	showDialog(modal);
    }
    public void showDialog(boolean modal) {

	if(editable) {
	    editDialog = new DialogDescriptor(this,
				     title,
				     modal, 
				     DialogDescriptor.OK_CANCEL_OPTION,
				     DialogDescriptor.CANCEL_OPTION,
				     DialogDescriptor.BOTTOM_ALIGN,
				     null,
				     this);
	}
	else {
	    editDialog = new DialogDescriptor(this,
				     title,
				     modal, 
				     DialogDescriptor.PLAIN_MESSAGE, 
				     DialogDescriptor.OK_OPTION,
				     DialogDescriptor.BOTTOM_ALIGN,
				     null,
				     this);
	}
	dialog = TopManager.getDefault().createDialog(editDialog);
	dialog.pack();
	dialog.setSize(size);
	dialog.show();
	this.repaint();
    }
    
    

    /**
     * Handle user input...
     */

    public void actionPerformed(ActionEvent e) {
	if (debug)System.out.println("Got action from the dialog");//NOI18N
	if(editDialog.getValue().equals(DialogDescriptor.OK_OPTION)) {
	    if(!editable) {
		dialog.dispose();
		return;
	    }
	    
	    inputOK = true;
	    String str = getName(); 
	    if(str.equals("")) inputOK = false; //NOI18N
	    if(inputOK && valueRequired) {
		str = getValue();
		if(str.equals("")) inputOK = false; //NOI18N
	    }
	    if(inputOK) dialog.dispose();
	    else {
		editDialog.setValue(NotifyDescriptor.CLOSED_OPTION);
		notifyBadInput(true);
	    }
	}
	else if (editDialog.getValue().equals(DialogDescriptor.CANCEL_OPTION)) 
	    dialog.dispose();
    }
	

    private void notifyBadInput(boolean both) { 

	String msg = null; 
	if(both) msg = NbBundle.getBundle(ParamEditor.class).getString("MON_ParamEditor_NameValue_required"); 
	else msg = NbBundle.getBundle(ParamEditor.class).getString("MON_ParamEditor_Name_required"); 

	Object[] options = { NotifyDescriptor.OK_OPTION };
	NotifyDescriptor badInputDialog = 
	    new NotifyDescriptor(msg,
				 NbBundle.getBundle(ParamEditor.class).getString("MON_ParamEditor_Input_required"),
				 NotifyDescriptor.DEFAULT_OPTION,
				 NotifyDescriptor.ERROR_MESSAGE,
				 options,
				 options[0]);
	TopManager.getDefault().notify(badInputDialog);
    }


    // Do we need this?
    public void repaint() {
	super.repaint();
	if (dialog != null && !repainting) {
	    repainting = true;
	    dialog.repaint(); 
	    repainting = false;
	}
    }

    private void addGridBagComponent(Container parent,
				     Component comp,
				     int gridx, int gridy,
				     int gridwidth, int gridheight,
				     double weightx, double weighty,
				     int anchor, int fill,
				     Insets insets,
				     int ipadx, int ipady) {
	GridBagConstraints cons = new GridBagConstraints();
	cons.gridx = gridx;
	cons.gridy = gridy;
	cons.gridwidth = gridwidth;
	cons.gridheight = gridheight;
	cons.weightx = weightx;
	cons.weighty = weighty;
	cons.anchor = anchor;
	cons.fill = fill;
	cons.insets = insets;
	cons.ipadx = ipadx;
	cons.ipady = ipady;
	parent.add(comp,cons);
    }

} // ParamEditor
