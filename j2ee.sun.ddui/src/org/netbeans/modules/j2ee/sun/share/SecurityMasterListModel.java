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
 * SecurityMasterListModel.java
 *
 * Created on January 22, 2004, 12:39 PM
 */

package org.netbeans.modules.j2ee.sun.share;

import java.util.Arrays;
import java.util.Vector;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.text.MessageFormat;

import java.beans.PropertyChangeSupport;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 *
 * @author Peter Williams
 */
public class SecurityMasterListModel extends AbstractListModel {

	private static final ResourceBundle customizerBundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.Bundle");	// NOI18N
	
	public static final String DUPLICATE_PRINCIPAL = customizerBundle.getString("ERR_PrincipalExists");	// NOI18N
	public static final String DUPLICATE_GROUP = customizerBundle.getString("ERR_GroupExists");	// NOI18N
	
	// !PW FIXME will likely have to replace this with LinkedHashMap to have
	//     decent performance adding and editing entries in large lists (> 25)
	private Vector masterList;
	
	private String duplicateErrorPattern;
	
	/** Creates a new instance of SecurityMasterListModel */
	private SecurityMasterListModel(String dupErrorPattern) {
		duplicateErrorPattern = dupErrorPattern;
		masterList = new Vector();
	}
	
	private SecurityMasterListModel(String dupErrorPattern, Object [] objects) {
		duplicateErrorPattern = dupErrorPattern;
		masterList = new Vector(Arrays.asList(objects));
	}
	
	/** Manipulation methods
	 */
	/** add element
	 */
	public void addElement(Object obj) {
		int index = masterList.size();
		masterList.add(obj);	
		fireIntervalAdded(this, index, index);
	}
	
	/** remove element
	 */
	public boolean removeElement(Object obj) {
		int index = masterList.indexOf(obj);
		boolean result = masterList.removeElement(obj);
		if(index >= 0) {
			fireIntervalRemoved(this, index, index);
		}
		return result;
	}
	
	public void removeElementAt(int index) {
		if(index >= 0 || index < masterList.size())  {
			masterList.removeElementAt(index);
			fireIntervalRemoved(this, index, index);
		}
	}
	
	public void removeElements(int[] indices) {
		if(indices.length > 0) {
			for(int i = indices.length-1; i >= 0; i--) {
				if(indices[i] >= 0 || indices[i] < masterList.size())  {
					masterList.removeElementAt(indices[i]);
				}
			}
			fireContentsChanged(this, indices[0], indices[indices.length-1]);
		}
	}
	
	/** replace element
	 */
	public boolean replaceElement(Object oldObj, Object newObj) {
		boolean result = false;
		int index = masterList.indexOf(oldObj);
		if(index != -1) {
			masterList.setElementAt(newObj, index);
			fireContentsChanged(this, index, index);			
		}
		return result;
	}
	
	/**
	 * Implementation of missing pieces of ListModel interface
	 */
	public Object getElementAt(int param) {
		if(param < 0 || param >= masterList.size()) {
			return null;
		}
		
		return masterList.get(param);
	}
	
	public int getSize() {
		return masterList.size();
	}
	
	/** Other public access methods
	 */
	public boolean contains(Object obj) {
		return masterList.contains(obj);
	}
	
	public String getDuplicateErrorMessage(String roleName) {
		Object [] args = { roleName };
		return MessageFormat.format(duplicateErrorPattern, args);		
	}
	
	/** Principal Name List
	 */
	private static SecurityMasterListModel principalMaster = new SecurityMasterListModel(DUPLICATE_PRINCIPAL);
	
	/** Retrieves the principal role name ListModel
	 * @return The ListModel representing the global principal role list.
	 */
	public static SecurityMasterListModel getPrincipalMasterModel() {
		return principalMaster;
	}

	/** Group Name List
	 */
	private static SecurityMasterListModel groupMaster = new SecurityMasterListModel(DUPLICATE_GROUP);

	/** Retrieves the group role name ListModel
	 * @return The ListModel representing the global group role list.
	 */
 	public static SecurityMasterListModel getGroupMasterModel() {
		return groupMaster;
	}
}
