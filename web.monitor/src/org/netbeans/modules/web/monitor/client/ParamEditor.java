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
    private static final ResourceBundle msgs =
	NbBundle.getBundle(TransactionView.class);
    
    private static final Dimension size = new Dimension(400, 300);
    private static final Dimension valueSize = new Dimension(380, 100);

    // Do we need this to close it?
    private Dialog dialog = null;
    private DialogDescriptor editDialog = null;
    private boolean inputOK = false;

    //
    // Hold the name and value until the widgets are created.
    //
    private boolean nameEditable;
    private boolean valueRequired = true;
    private String name = "";
    private String value = "";
    
    //
    // Widgets
    //
    private JTextField nameText;
    private JTextArea  valueText;

    private String title = "";

    //private static boolean repainting = false;
    private boolean repainting = false;

    public ParamEditor(String name, String value, boolean nameEditable,
		       String title) { 
	this(name, value, nameEditable, title, true); 
    }

    public ParamEditor(String name, String value, boolean nameEditable,
		       String title, boolean valueRequired) { 
	super();
	this.nameEditable = nameEditable;
	this.valueRequired = valueRequired;
	this.title = title;
	setName(name);
	setValue(value);
	createPanel();
    }

    public boolean getDialogOK() {
	return inputOK; 
    }

    public String getName() {
	return nameText.getText().trim();
    }
    public void setName(String val) {
	name = val;
	if (nameText != null) {
	    nameText.setText(val);
	}
    }

    public String getValue() {
	return valueText.getText().trim();
    }
    public void setValue(String val) {
	value = val;
	if (valueText != null) {
	    valueText.setText(val);
	}
    }
	
    
    public void createPanel() {

	if(debug) System.out.println("in (new) ParamEditor.createPanel()");

	this.setLayout(new GridBagLayout());
	int gridy = -1;
	Insets zeroInsets = new Insets(0,0,0,0);
	Insets textInsets = new Insets(0,5,0,5);
	int fullGridWidth = java.awt.GridBagConstraints.REMAINDER;

	addGridBagComponent(this, new JLabel(msgs.getString("MON_Name")), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    zeroInsets,
			    0, 0);
	
	nameText = new JTextField();
	nameText.setText(name);
	nameText.setEnabled(nameEditable);
	addGridBagComponent(this, nameText, 0, ++gridy,
			    fullGridWidth, 1, 0, 0,
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.HORIZONTAL,
			    textInsets,
			    0, 0);

	addGridBagComponent(this, new JLabel(msgs.getString("MON_Value")), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    zeroInsets,
			    0, 0);

	valueText = new JTextArea();
	valueText.setText(value);
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

	if (dialog == null) {
	    editDialog = 
		new DialogDescriptor(this,
				     title,
				     modal, 
				     DialogDescriptor.OK_CANCEL_OPTION,
				     DialogDescriptor.CANCEL_OPTION,
				     DialogDescriptor.BOTTOM_ALIGN,
				     null,
				     this);
	    
	    dialog = TopManager.getDefault().createDialog(editDialog);
	}
	
	dialog.pack();
	dialog.setSize(size);
	dialog.show();
	this.repaint();
    }
    
    

    /**
     * Handle user input...
     */

    public void actionPerformed(ActionEvent e) {
	if (debug)System.out.println("Got action from the dialog");
	if(editDialog.getValue().equals(DialogDescriptor.OK_OPTION)) {

	    inputOK = true;
	    String str = getName(); 
	    if(str.equals("")) inputOK = false;
	    if(inputOK && valueRequired) {
		str = getValue();
		if(str.equals("")) inputOK = false;
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
	if(both) msg = msgs.getString("MON_ParamEditor_NameValue_required"); 
	else msg = msgs.getString("MON_ParamEditor_Name_required"); 

	Object[] options = { NotifyDescriptor.OK_OPTION };
	NotifyDescriptor badInputDialog = 
	    new NotifyDescriptor(msg,
				 msgs.getString("MON_ParamEditor_Input_required"),
				 NotifyDescriptor.DEFAULT_OPTION,
				 NotifyDescriptor.ERROR_MESSAGE,
				 options,
				 options[0]);
	TopManager.getDefault().notify(badInputDialog);
    }


    public void repaint() {
	super.repaint();
	if (dialog != null && !repainting) {
	    repainting = true;
	    dialog.repaint(); // hopefully this won't get us into an infinte loop?
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
