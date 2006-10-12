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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * ErrorSupport.java
 *
 * Created on November 14, 2003, 3:56 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Iterator;
import java.util.ResourceBundle;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class ErrorSupport implements ErrorSupportClient{

	private static final ResourceBundle bundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle");	// NOI18N
    
	private java.util.List errorList;
	private javax.swing.JPanel errorPanel;
	private javax.swing.JComponent focusedComponent;

	private ErrorSupportClient client;

	/** Creates a new instance of ErrorSupport */
	public ErrorSupport() {
		initialize();
		this.client = (ErrorSupportClient) this;
	}


	public ErrorSupport(ErrorSupportClient client) {
		initialize();
		this.client = client;
	}


	private void initialize(){
		errorList = new java.util.ArrayList();
		errorPanel = null;
		focusedComponent = null;
	}


	/** Shows the errors Panel.
	 *  Set focus to the focusedComponent.
	 */    
	public void showErrors(){
		//Should be called Called on the following --
		//  on <enter> or item selection of each field
		//  on focus gain of each field
		removeAllErrors();
		java.util.Collection errors = client.getErrors();
		if(errors != null){
			java.util.Iterator iterator = errors.iterator();
			while(iterator.hasNext()){
				Object object  = iterator.next();
				if(object instanceof java.lang.String){
					addError((String)object);
				}
			}
		}

		Container parentPanel = client.getErrorPanelParent();
		if(parentPanel != null){
			if(errorList.size() != 0){
                if(errorPanel == null) {
                    errorPanel = new JPanel(new GridBagLayout());
                    parentPanel.add(errorPanel, client.getErrorPanelConstraints());
                } else {
                    errorPanel.removeAll();
                }
              
                for(Iterator iter = errorList.iterator(); iter.hasNext(); ) {
                    String message = (String) iter.next();

                    // Add error message
                    JLabel label = new JLabel();
                    label.setIcon(BaseCustomizer.errorMessageIcon);
                    label.setText(message);
                    label.getAccessibleContext().setAccessibleName(bundle.getString("ASCN_ErrorMessage")); // NOI18N
                    label.getAccessibleContext().setAccessibleDescription(message);
                    label.setForeground(getMessageForegroundColor());

                    GridBagConstraints constraints = new GridBagConstraints();
                    constraints.gridwidth = GridBagConstraints.REMAINDER;
                    constraints.fill = GridBagConstraints.HORIZONTAL;
                    constraints.weightx = 1.0;
                    errorPanel.add(label, constraints);
                }
                
//				if (focusedComponent != null) {
//                    focusedComponent.requestFocus();
//                }
			} else {
                if(errorPanel != null) {
                    parentPanel.remove(errorPanel);
                    errorPanel = null;
                }
			}
            
			parentPanel.validate();
		}
	}


	/** Test if the error list is filled or not. 
	* @return true if list has errors, false if not.
	*/
	public boolean hasErrors(){
		return (errorList.size()==0?false:true);
	}


	/** Adds an error string to the error list.
	* @param error error message
	*/
	public void addError(String error){
		errorList.add(error);
	}


	/** Adds an error string to the error list and sets the component
	* that should gain the focus.
	* @param error error message
	* @param focusedComponent component(JTextField) where the error should be fixed
	*/
	public void addError(javax.swing.JComponent focusedComponent, String error){
		setFocusedComponent(focusedComponent);
		errorList.add(error);
	}


	/** Setting focus to the selected component when showing the dialog.
	* Using in the showErrors() method.
	* @param comp component that need to be focused
	*/
	public void setFocusedComponent(javax.swing.JComponent comp){
		focusedComponent = comp;
	}


	/**
	* @return the focused component
	*/
	public javax.swing.JComponent getFocusedComponent(){
		return focusedComponent;
	}


	/**
	* Removes all the errors
	*/
	public void removeAllErrors(){
		errorList.clear();
		focusedComponent = null;
	}


	public java.util.Collection getErrors(){
		return null;
	}


	public java.awt.Container getErrorPanelParent(){
		return null;
	}


	public java.awt.GridBagConstraints getErrorPanelConstraints(){
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.insets = new Insets(6,12,11,11);
		return gridBagConstraints;
	}

	public java.awt.Color getMessageForegroundColor() {
        return BaseCustomizer.getErrorForegroundColor();
	}
}
