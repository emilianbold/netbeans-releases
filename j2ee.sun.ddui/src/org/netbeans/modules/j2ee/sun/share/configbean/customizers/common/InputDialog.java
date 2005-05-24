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
 * InputDialog.java
 *
 * Created on October 4, 2003, 7:34 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

// AWT
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// Swing
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

// NetBeans
import org.openide.util.HelpCtx;

import org.netbeans.modules.j2ee.sun.share.configbean.Utils;

/* A modal dialog object with Ok, Cancel, Help buttons and an optional required
 * field notation.
 *
 * This object supports inline error message reporting.
 *
 * @author  Rajeshwar Patil
 * Enhancements by Peter Williams
 * @version %I%, %G%
 */

public abstract class InputDialog extends JDialog implements HelpCtx.Provider {

	/** Represents clicking on the Cancel button or closing the dialog 
	 */
	public static final int CANCEL_OPTION = 0;
	
	/** Represents clicking on the OK button 
	 */
	public static final int OK_OPTION = 1;
	
	/** Represents clicking on the HELP button 
	 */
	public static final int HELP_OPTION = 2;

	private static final ResourceBundle bundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle"); //NOI18N
	
	private int chosenOption;
	private JPanel buttonPanel;
	private JButton okButton;
	private JButton helpButton;
	private JTextArea errorTextArea;
	private List errorList;

	/** Creates a new instance of modal InputDialog
	 *  @param panel the panel from this dialog is opened
	 *  @param title title for the dialog
	 */
	public InputDialog(JPanel panel, String title) {
		this(panel, title, false);
	}

	/** Creates a new instance of modal InputDialog
	 *  @param panel the panel from this dialog is opened
	 *  @param title title for the dialog
	 *  @param showRequiredNote set this if you want a '* denotes required field'
	 *   message in the lower left hand corner.
	 */
	public InputDialog(JPanel panel, String title, boolean showRequiredNote) {
		super(getFrame(panel), title, true);

		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				chosenOption = CANCEL_OPTION;
			}
		});

		getContentPane().setLayout(new BorderLayout());

		// Create button panel -- using gridbaglayout now due to possible 
		// message label on left hand side (in addition to buttons on right
		// hand side) and error text area above the buttons.
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridBagLayout());

		errorTextArea = new JTextArea();
		errorTextArea.setEditable(false);
		errorTextArea.setOpaque(false);
		errorTextArea.setForeground(BeanCustomizer.ErrorTextForegroundColor);
		errorTextArea.setRows(2);	// Two lines, just because.
		errorTextArea.setLineWrap(true);
		errorTextArea.setWrapStyleWord(true);
		errorTextArea.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_ErrorTextArea"));	// NOI18N
		errorTextArea.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ErrorTextArea"));	// NOI18N

		JScrollPane errorScrollPane = new JScrollPane();
		errorScrollPane.setBorder(null);
		errorScrollPane.setViewportView(errorTextArea);
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.insets = new Insets(4, 4, 0, 4);
		gridBagConstraints.weightx = 1.0;
		buttonPanel.add(errorScrollPane, gridBagConstraints);

		if(showRequiredNote) {
			JLabel requiredNote = new JLabel();
			requiredNote.setText(bundle.getString("LBL_RequiredMessage"));	// NOI18N
			gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new Insets(4, 4, 4, 4);
			gridBagConstraints.anchor = GridBagConstraints.SOUTHWEST;
			buttonPanel.add(requiredNote, gridBagConstraints);
		}

		okButton = new JButton(bundle.getString("LBL_OK"));    // NOI18N
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		gridBagConstraints.weightx = 1.0;
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				chosenOption = OK_OPTION;
				actionOk();
			}
		});
		okButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_OK"));	// NOI18N
		okButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_OK"));	// NOI18N
		buttonPanel.add(okButton, gridBagConstraints);

		JButton cancelButton = new JButton(bundle.getString("LBL_Cancel")); // NOI18N
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				chosenOption = CANCEL_OPTION;
				actionCancel();
			}
		});
		cancelButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_Cancel"));	// NOI18N
		cancelButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Cancel"));	// NOI18N
		buttonPanel.add(cancelButton, gridBagConstraints);

		helpButton = new JButton(bundle.getString("LBL_Help")); // NOI18N
		helpButton.setMnemonic(bundle.getString("MNE_Help").charAt(0));	// NOI18N
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagConstraints.insets = new Insets(4, 4, 4, 4);
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		helpButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				chosenOption = HELP_OPTION;
				actionHelp();
			}
		});
		helpButton.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_Help"));	// NOI18N
		helpButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Help"));	// NOI18N
		buttonPanel.add(helpButton, gridBagConstraints);

		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		getRootPane().setDefaultButton(okButton);
	}
	
	/** Displays the dialog.
	 *  @return identifies whether the OK button or Cancel button was selected
	 */        
	public int display() {
		super.show();
		return chosenOption;
	}

	protected void actionOk() {
		super.dispose();
	}

	protected void actionCancel() {
		super.dispose();
	}

	protected void actionHelp() {
		Utils.invokeHelp(getHelpId());
	}
	
	protected abstract String getHelpId();

	/** Seeks for the nearest Frame containg this component.
	 */
	public static Frame getFrame(Component component) {
		while (!(component instanceof Frame)){
			component = component.getParent();
		}
		return ((Frame) component);
	}


	/** Enables to place this dialog in the middle of the given panel.
	 *  @param panel where the dialog should be placed
	 */
	protected void setLocationInside(JPanel panel) {
		java.awt.Rectangle rect = this.getBounds();
		int width = rect.width;
		int height = rect.height;
		java.awt.Rectangle panelRect = panel.getBounds();
		if (width> panelRect.width || height> panelRect.height) {
			setLocationRelativeTo(panel);
		} else {
			java.awt.Point location = panel.getLocationOnScreen();
			setLocation(location.x + (panelRect.width-width)/2, 
				location.y + (panelRect.height-height)/2);
		}
	}

	/** Simple enable/disable mechanism for the ok button - this should be improved
	 *  to allow specification of which button, or a range of buttons, where the
	 *  buttons are specified by enums (using typesafe enum pattern).
	 */
	protected void setOkEnabled(boolean flag) {
		okButton.setEnabled(flag);
	}


	/** Shows the errors at the top of dialog panel.
	 *  Set focus to the focused component.
	 */    
	public void showErrors() {
		boolean hasErrors;
		String errorString;

		if(errorList != null && errorList.size() > 0) {
			StringBuffer errorBuf = new StringBuffer(100 * errorList.size());
			for(Iterator iter = errorList.iterator(); iter.hasNext();) {
				errorBuf.append(iter.next().toString());
				if(iter.hasNext()) {
					errorBuf.append("\n"); // NOI18N
				}
			}
			
			hasErrors = true;
			errorString = errorBuf.toString();
		} else {
			hasErrors = false;
			errorString = "";	// NOI18N
		}
		
		// Display errors (if any) and disable/enable the OkButton as appropriate
		errorTextArea.setText(errorString);
		setOkEnabled(!hasErrors);
	}

	
	/** Sets the existing error list to the collection of errors passed in.
	 *  @param errors Collection of error messages.
	 */
	protected void setErrors(Collection errors) {
		errorList = new ArrayList(errors);
		showErrors();
	}

	
	/** Adds an error string to the error list.
	 *  @param error error message
	 */
	public void addError(String error) {
		if(errorList == null) {
			errorList = new ArrayList();
		}
		
		errorList.add(error);
		showErrors();
	}
	
	
	/** Adds a collection of errors to the error list.
	 *  @param errors Collection of error messages.
	 */
	protected void addErrors(Collection errors) {
		if(errorList == null) {
			errorList = new ArrayList(errors);
		} else {
			errorList.addAll(errors);
		}
		
		showErrors();
	}
	
	
	/** Clears out all error messages.
	 */
	protected void clearErrors() {
		errorList = null;
		showErrors();
	}

	
	/** Test if the error list is filled or not. 
	 *  @return true if there are errors, false if not.
	 */
	public boolean hasErrors() {
		boolean result = false;
		
		if(errorList != null && errorList.size() > 0) {
			result = true;
		}
		
		return result;
	}
	
	/** ----------------------------------------------------------------------- 
	 *  Implementation of HelpCtx.Provider interface
	 */
	public HelpCtx getHelpCtx() {
		return new HelpCtx(getHelpId());
	}	


        protected void setButtonPanelPreferredSize(Dimension dimension){
            buttonPanel.setMinimumSize(dimension);
            buttonPanel.setPreferredSize(dimension);
        }
}
