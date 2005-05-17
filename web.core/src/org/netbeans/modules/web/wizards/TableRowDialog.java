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

/**
 * @author Ana von Klopp 
 */

package org.netbeans.modules.web.wizards;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

public class TableRowDialog extends javax.swing.JPanel {

    private final static boolean debug = false;    
    private static final Dimension valueSize = new Dimension(300, 50);

    // Do we need this to close it?
    private Dialog dialog = null;
    private DialogDescriptor editDialog = null;
    private String errorMessage = null; 
    private boolean dialogOK = false; 

    // Hold the name and value until the widgets are created.
    private String name = ""; //NOI18N
    private String value = ""; //NOI18N
    private Editable editable; 
    private Condition condition; 
    private String title = ""; //NOI18N
    
    //private static boolean repainting = false;
    private boolean repainting = false;

    private static final long serialVersionUID = -855447534116444417L;
    
    public TableRowDialog(String name, String value, Editable e, Condition c, 
		       String title) { 
	if(debug) log("::CONSTRUCTOR"); 
	this.name = name; 
	this.value = value; 
	this.editable = e; 
	if(debug) log("\tEditable: " + e.toString()); 
	this.condition = c; 
	if(debug) log("\tCondition: " + c.toString()); 
	this.title = title;
	initialize();
    }

    public boolean getDialogOK() {
	if(debug) { 
	    log("::getDialogOK()"); //NOI18N
	    log("\tdialogOK = " + String.valueOf(dialogOK)); //NOI18N
	} 
	return dialogOK; 
    }

    public String getName() {
	return name; 
    }

    public String getValue() {
	return value; 
    }
    
    public void initialize() {

	if(debug) System.out.println("in (new) TableRowDialog.initialize()"); //NOI18N
	this.setLayout(new GridBagLayout());

	// Entity covers entire row
	GridBagConstraints labelC = new GridBagConstraints();
	labelC.gridx = 0;                               
	labelC.gridy = GridBagConstraints.RELATIVE;     
	labelC.anchor = GridBagConstraints.WEST;         
	labelC.fill = GridBagConstraints.HORIZONTAL; 
	labelC.insets = new Insets(4, 15, 4, 15);

	// Initial label
	GridBagConstraints firstC = new GridBagConstraints();
	firstC.gridx = 0;
	firstC.gridy = GridBagConstraints.RELATIVE;     
	firstC.gridwidth = 1; 
	firstC.anchor = GridBagConstraints.WEST; 
	firstC.insets = new Insets(4, 15, 4, 0);

	// Text field
	GridBagConstraints tfC = new GridBagConstraints();
	tfC.gridx = GridBagConstraints.RELATIVE;
	tfC.gridy = 0; 
	tfC.gridwidth = 7; 
	tfC.fill = GridBagConstraints.HORIZONTAL;     
	tfC.insets = new Insets(4, 0, 4, 15);

	// Text area
	GridBagConstraints taC = new GridBagConstraints();
	taC.gridx = 0;
	taC.gridy = GridBagConstraints.RELATIVE;     
	taC.gridheight = 3; 
	taC.gridwidth = 8; 
	taC.fill = GridBagConstraints.BOTH; 
	taC.anchor = GridBagConstraints.WEST; 
	taC.insets = new Insets(0, 15, 4, 15);

        JLabel nameLabel = new JLabel(); 
        nameLabel.setDisplayedMnemonic(NbBundle.getMessage(TableRowDialog.class, "LBL_paramname_mnemonic").charAt(0));
        nameLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TableRowDialog.class, "ACSD_paramname"));

	String text = NbBundle.getMessage(TableRowDialog.class, 
					  "LBL_paramname");
	text = text.concat(": "); //NOI18N
	
	if(editable == Editable.BOTH) { 
	    final JTextField nameText = new JTextField(25);
	    nameText.addFocusListener(new FocusListener() {
		public void focusGained(FocusEvent evt) {
		}
		public void focusLost(FocusEvent evt) {
		    name = nameText.getText(); 
		}
		}); 
	    nameLabel.setLabelFor(nameText);
	    nameText.getAccessibleContext().setAccessibleName(NbBundle.getMessage(TableRowDialog.class, "ACSD_paramname"));
	    nameText.setText(name);
	    nameText.setBackground(java.awt.Color.white);
	    nameText.setEditable(editable == Editable.BOTH);

	    this.add(nameLabel, firstC); 
	    this.add(nameText, tfC); 
	}
	else { 
	    this.add(nameLabel, labelC); 
	    text = text.concat(name); 
	}
	nameLabel.setText(text); 
	    
	JLabel valueLabel = new JLabel(); 
	valueLabel.setText(NbBundle.getMessage(TableRowDialog.class, "LBL_paramvalue").concat(":"));
        valueLabel.setDisplayedMnemonic(NbBundle.getMessage(TableRowDialog.class, "LBL_paramvalue_mnemonic").charAt(0));
        valueLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TableRowDialog.class, "ACSD_paramvalue"));
	firstC.gridy++; 
	this.add(valueLabel, labelC); 

	final JTextArea valueText = new JTextArea();
	valueText.addFocusListener(new FocusListener() {
		public void focusGained(FocusEvent evt) {
		}
		public void focusLost(FocusEvent evt) {
		    value = valueText.getText();
		}
	    }); 
	valueLabel.setLabelFor(valueText);
        valueText.getAccessibleContext().setAccessibleName(NbBundle.getMessage(TableRowDialog.class, "ACSD_paramvalue"));

	if(editable == Editable.NEITHER) {
	    valueText.setEditable(false);
	    valueText.setBackground(this.getBackground().darker()); 
	    valueText.setForeground(Color.BLACK); 
	    valueText.setBorder(BorderFactory.createLoweredBevelBorder());
	}
	valueText.setText(value);
	valueText.setLineWrap(true);
	valueText.setWrapStyleWord(false); 
	
	JScrollPane scrollpane = new JScrollPane(valueText);
	scrollpane.setPreferredSize(valueSize);
	//scrollpane.setViewportBorder(BorderFactory.createLoweredBevelBorder());
	this.add(scrollpane, taC); 

	// Housekeeping
	// this.setMaximumSize(this.getPreferredSize()); 
	this.repaint();
    }

    public void showDialog() {

	if(editable == Editable.NEITHER) {
	    if(debug) log("Non-modal dialog, OK option only"); 
	    NotifyDescriptor d = 
		new NotifyDescriptor(this, title, 
				     NotifyDescriptor.DEFAULT_OPTION,
				     NotifyDescriptor.PLAIN_MESSAGE, 
				     new Object[] { NotifyDescriptor.OK_OPTION },
				     NotifyDescriptor.OK_OPTION); 
	    DialogDisplayer.getDefault().notify(d);
	}
	else {
	    editDialog = new DialogDescriptor
		(this, title, true, DialogDescriptor.OK_CANCEL_OPTION,
		 DialogDescriptor.CANCEL_OPTION,
		 new ActionListener() {
		     public void actionPerformed(ActionEvent e) {
			 evaluateInput(); 
		     }
		 }); 

	    dialog = DialogDisplayer.getDefault().createDialog(editDialog);
	    dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(TableRowDialog.class, "ACSD_initparam_edit")); // NOI18N

	    dialog.show();
	    this.repaint();
	}
    }

    /**
     * Handle user input...
     */

    public void evaluateInput() {

	if (debug) { 
	    log("::evaluateInput()");//NOI18N
	} 

	if (editDialog.getValue().equals(NotifyDescriptor.CANCEL_OPTION)) { 
	    if(debug) log("\tGot cancel"); //NOI18N
	    dialog.dispose();
	    dialogOK = false; 
	    return; 
	}

	if(debug) log("\tGot OK"); //NOI18N
	if(editable == Editable.NEITHER) {
	    if(debug) log("\tNot editable"); //NOI18N
	    dialog.dispose();
	    dialogOK = false; 
	    return;
	}
	if(debug) log("Name is " + name); 
	if(debug) log("Value is " + value); 

	errorMessage = null; 

	if(name.equals("")) 
	    errorMessage = NbBundle.getMessage(TableRowDialog.class, 
					       "MSG_no_name"); 

	else if(condition == Condition.VALUE && value.equals("")) 
	    errorMessage = NbBundle.getMessage(TableRowDialog.class, 
					       "MSG_no_value"); 

	if(debug) log("ErrorMessage: " + errorMessage); 

	if(errorMessage == null) { 
	    dialog.dispose();
	    dialogOK = true; 
	} 
	else {
	   editDialog.setValue(NotifyDescriptor.CLOSED_OPTION);
	   NotifyDescriptor nd = new NotifyDescriptor.Message
	       (errorMessage, NotifyDescriptor.ERROR_MESSAGE); 
	    DialogDisplayer.getDefault().notify(nd);
	}
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

    private void log(String s) { 
	System.out.println("TableRowDialog" + s);
    } 

    static class Condition { 
	private String condition; 

	private Condition(String condition) { 
	    this.condition = condition; 
	}
    
	public String toString() { return condition; } 

	public static final Condition NONE = new Condition("none"); 
	public static final Condition VALUE = new Condition("value"); 
    } 

} // TableRowDialog
