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
 * ConstraintFieldValueEntry.java
 *
 * Created on December 12, 2003, 6:28 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp;

import java.util.List;
import java.util.ResourceBundle;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.web.ConstraintField;

import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.BeanListMapping;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableModel;

/**
 *
 * @author  Peter Williams
 */
public class ConstraintFieldValueEntry extends GenericTableModel.TableEntry {

	private static final ResourceBundle webappBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.customizers.webapp.Bundle");	// NOI18N
	
	public ConstraintFieldValueEntry() {
		super(ConstraintField.CONSTRAINT_FIELD_VALUE, 
			webappBundle.getString("LBL_ConstraintFieldValues"));	// NOI18N
	}
	
	/**	The parent is the ConstraintField being edited.  The expected return
	 *  is a ConstraintField that has the array of ConstraintFieldValues.
	 *  We can't just return the parent because if it gets changed and the user
	 *  hits cancel, the changes will still be commited (bad!).
	 *
	 *  @param parent The real ConstraintField that provides initialization data
	 *     for the returned bean.
	 *  @return A new ConstraintField that has any existing data saved into it,
	 *     but can be edited and changed at will independent of the parent.
	 */
	public Object getEntry(CommonDDBean parent) {
		return new BeanListMapping((CommonDDBean) parent.clone(), getPropertyName());
	}

	/** Value is type ConstraintField but contains only an array of 
	 *  ConstraintFieldValues so we merge this into the parent ConstraintField
	 *  which already contains the other stuff.
	 *
	 *  @param parent The real ConstraintField that is to be saved (merged) into.
	 *  @param value The temporary ConstraintField that has been used for storage
	 *     during editing.
	 */
	public void setEntry(CommonDDBean parent, Object value) {
		parent.merge(((BeanListMapping) value).getBean(), CommonDDBean.MERGE_UPDATE);
	}
	
	public Object getEntry(CommonDDBean parent, int row) {
		throw new UnsupportedOperationException();
	}
	
	public void setEntry(CommonDDBean parent, int row, Object value) {
		throw new UnsupportedOperationException();
	}
	
}
