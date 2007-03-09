/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/**
 * @author Simran Gleason
 * @author Ana von Klopp
 */


package org.netbeans.modules.web.monitor.client;

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

public class ParamEditor extends javax.swing.JPanel {

    private static final Dimension valueSize = new Dimension(400, 150);

    // Do we need this to close it?
    private Dialog dialog = null;
    private Dialog d2 = null;
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

    public ParamEditor(String name, String value, Editable e, Condition c, 
		       String title) { 
	this.name = name; 
	this.value = value; 
	this.editable = e; 
	this.condition = c; 
	this.title = title;
	initialize();
    }

    public boolean getDialogOK() {
	return dialogOK; 
    }

    public String getName() {
	return name; 
    }

    public String getValue() {
	return value; 
    }
    
    public void initialize() {

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
        taC.weightx = 1.0;
        taC.weighty = 1.0;
	taC.fill = GridBagConstraints.BOTH; 
	taC.anchor = GridBagConstraints.WEST; 
	taC.insets = new Insets(0, 15, 4, 15);

        JLabel nameLabel = new JLabel(); 
        nameLabel.setDisplayedMnemonic(NbBundle.getMessage(ParamEditor.class, "MON_Name_Mnemonic").charAt(0));
        nameLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ParamEditor.class, "ACS_MON_NameA11yDesc"));
        if (name == null || name.length()==0) 
            getAccessibleContext().setAccessibleDescription(title);
        else 
             getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ParamEditor.class,
                "ACS_ParamEditorA11yDesc",title, name)); //NOI18N
	String text = NbBundle.getMessage(ParamEditor.class, "MON_Name");
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
	    nameText.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ParamEditor.class, "ACS_MON_NameTextFieldA11yName"));
	    nameText.setToolTipText(NbBundle.getMessage(ParamEditor.class, "ACS_MON_NameTextFieldA11yDesc"));
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
	valueLabel.setText(NbBundle.getMessage(ParamEditor.class, "MON_Value").concat(":"));
        valueLabel.setDisplayedMnemonic(NbBundle.getMessage(ParamEditor.class, "MON_Value_Mnemonic").charAt(0));
        valueLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ParamEditor.class, "ACS_MON_ValueA11yDesc"));
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
        valueText.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ParamEditor.class, "ACS_MON_ValueTextAreaA11yName"));
        valueText.setToolTipText(NbBundle.getMessage(ParamEditor.class, "ACS_MON_ValueTextAreaA11yDesc"));
	if(editable == Editable.NEITHER) {
	    valueText.setEditable(false);
	    valueText.setBackground(this.getBackground());
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
	    dialog.setVisible(true);
	    this.repaint();
	}
    }

    /**
     * Handle user input...
     */

    public void evaluateInput() {

	if (editDialog.getValue().equals(NotifyDescriptor.CANCEL_OPTION)) { 
	    dialog.dispose();
	    dialogOK = false; 
	    return; 
	}

	if(editable == Editable.NEITHER) {
	    dialog.dispose();
	    dialogOK = false; 
	    return;
	}

	errorMessage = null; 

	if(name.equals("")) 
	    errorMessage = NbBundle.getMessage(ParamEditor.class, 
					       "MSG_no_name"); 
	else if(condition == Condition.COOKIE && name.equalsIgnoreCase("jsessionid"))
	    errorMessage = NbBundle.getMessage(ParamEditor.class, 
					       "MSG_no_jsession"); 
	else if(condition == Condition.VALUE && value.equals("")) 
	    errorMessage = NbBundle.getMessage(ParamEditor.class, 
					       "MSG_no_value"); 
	else if(condition == Condition.HEADER && 
		name.equalsIgnoreCase("cookie") &&
		(value.indexOf("jsessionid") > -1 ||
		 value.indexOf("JSESSIONID") > -1))
	    errorMessage = NbBundle.getMessage(ParamEditor.class, 
					       "MSG_no_jsession"); 

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

    static class Editable { 
	private String editable; 

	private Editable(String editable) { 
	    this.editable = editable; 
	}
    
	public String toString() { return editable; } 

	public static final Editable BOTH = new Editable("both"); 
	public static final Editable VALUE = new Editable("value"); 
	public static final Editable NEITHER = new Editable("neither"); 
    } 

    static class Condition { 
	private String condition; 

	private Condition(String condition) { 
	    this.condition = condition; 
	}
    
	public String toString() { return condition; } 

	public static final Condition NONE = new Condition("none"); 
	public static final Condition VALUE = new Condition("value"); 
	public static final Condition COOKIE = new Condition("cookie"); 
	public static final Condition HEADER = new Condition("header"); 
    } 

} // ParamEditor
