/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

	private final ResourceBundle bundle = ResourceBundle.getBundle(
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
