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
 * GenericTableDialogPanelAccessor.java
 *
 * Created on January 2, 2004, 2:07 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.util.Collection;
import java.util.List;

/** Interface to be implemented by dialog subpanels that are to be used for data
 *  entry into a table managed by the GenericTablePanel/GenericTableModel pair.
 *
 *  Extend a JPanel that implements this interface to allow editing of complex
 *  fields.  (For simple text only input, the existing default panel should be
 *  sufficient.)
 *
 * @author Peter Williams
 */
public interface GenericTableDialogPanelAccessor {
	
	/** Initialization routine to handle any required building or initialization tasks.
	 *
	 * @param preferredWidth The calculated dynamic preferredWidth for the child dialog.
	 * @param entries List of field descriptions 
	 */
	public void init(int preferredWidth, List entries, Object data);
	
	/** Set the error manager for use by this panel.
	 */
//	public void setErrorManager(ErrorSupportManager errorMgr);
	
	/** Pass initial field values.
	 *
	 * @param values The list of initial values for the fields.
	 */
	public void setValues(Object [] values);
	
	/** Retrieve current field values.
	 *
	 * @return List of objects that represent the values entered by the user
	 */
	public Object [] getValues();
	
	/** Retrieve any errors in what the user has currently entered.
	 *
	 * @param validationSupport The validation support object
	 * @return Collection of error messages that tell the user why their current
	 *   input isn't acceptable.
	 */
	public Collection getErrors(ValidationSupport validationSupport);
	
	/** Determines if the required fields have appropriate values.
	 *
	 * @return True if the required fields are filled appropriately, false otherwise.
	 */
	public boolean requiredFieldsFilled();
}
