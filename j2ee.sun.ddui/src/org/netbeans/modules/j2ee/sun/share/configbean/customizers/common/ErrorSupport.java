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
 * ErrorSupport.java
 *
 * Created on November 14, 2003, 3:56 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class ErrorSupport implements ErrorSupportClient{

	private java.util.List errorList;
	private javax.swing.JTextArea errorArea;
	private javax.swing.JScrollPane errorPane;
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
		errorArea = null;
		errorPane = null;
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

		java.awt.Container parentPanel = client.getErrorPanelParent();
		if(parentPanel != null){
			if(errorList.size() != 0){
				String errorString = ""; // NOI18N
				for (int i=0;i<errorList.size();i++){
					errorString+=(i==0?(String)errorList.get(i):"\n"+  //NOI18N
						(String)errorList.get(i));
				}

				if (errorArea == null){
					errorArea = new javax.swing.JTextArea(errorString, 3, 50);
					errorArea.setEditable(false);
					errorArea.setForeground(client.getMessageForegroundColor());
					errorPane = new javax.swing.JScrollPane(errorArea);
					errorPane.setVerticalScrollBarPolicy(
						javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
					errorPane.setPreferredSize(new java.awt.Dimension(200, 20));

					errorPanel = 
						new javax.swing.JPanel(new java.awt.GridBagLayout()); 

					java.awt.GridBagConstraints gridBagConstraints = 
						new java.awt.GridBagConstraints();

					gridBagConstraints.anchor = gridBagConstraints.SOUTH;
					gridBagConstraints.fill = gridBagConstraints.HORIZONTAL;
					gridBagConstraints.gridheight = 1;
					gridBagConstraints.gridwidth = 1;
					gridBagConstraints.gridx = 0;
					gridBagConstraints.gridy = 0;
					gridBagConstraints.insets.top = 0;
					gridBagConstraints.insets.left = 0;
					gridBagConstraints.insets.bottom = 0;
					gridBagConstraints.insets.right = 0;
					///gridBagConstraints.ipadx = 240;
					gridBagConstraints.ipady = 30;
					gridBagConstraints.weightx = 1.0;
					gridBagConstraints.weighty = 0.0;

					errorPanel.add(errorPane, gridBagConstraints);
					parentPanel.add(errorPanel, 
						client.getErrorPanelConstraints());
				} else {
					errorArea.setText(errorString);
				}
				//parentPanel.pack();
				if (focusedComponent != null) focusedComponent.requestFocus();
			}else{
				if(errorArea != null){
					parentPanel.remove(errorPanel);
					errorArea = null;
				}
			}
			///parentPanel.validate();
			parentPanel.paintAll(parentPanel.getGraphics());
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

		java.awt.GridBagConstraints gridBagConstraints = 
			new java.awt.GridBagConstraints();

		gridBagConstraints.anchor = gridBagConstraints.CENTER;
		gridBagConstraints.fill = gridBagConstraints.BOTH;
		gridBagConstraints.gridheight = 1;
		gridBagConstraints.gridwidth = 1;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.insets.top = 0;
		gridBagConstraints.insets.left = 5;
		gridBagConstraints.insets.bottom = 5;
		gridBagConstraints.insets.right = 5;
		gridBagConstraints.ipadx = 0;
		gridBagConstraints.ipady = 0;
		gridBagConstraints.weightx = 0.0;
		gridBagConstraints.weighty = 0.0;

		return gridBagConstraints;
	}

	public java.awt.Color getMessageForegroundColor() {
		// default error message color to red.
		return java.awt.Color.red;
	}
}
