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
 * ErrorMessageDB.java
 *
 * Created on March 4, 2004, 12:14 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean;

import java.util.Collections;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.HashMap;
import java.util.ArrayList;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


/** This is an error message database for storing partitioned error messages
 *  of class ValidationError (partition, fieldId (xpath), message).
 *
 * @author Peter Williams
 */
public final class ErrorMessageDB {
	
	/** The database.  This is a mapping of keys to collections, where the key
	 *  is a message partition (ValidationError.Partition) and the value is a
	 *  collection of error objects within that partition.  As validation requests
	 *  are made, errors are added and removed.  If a partition has no errors,
	 *  it is removed from the map so that the keyset of the map always represents
	 *  exactly the partitions that have errors.
	 */
	private Map errorSets = new HashMap(19);	// 19 = 2x max expected partitions
	
	
	/** Creates a new instance of ErrorMessageDB.
	 */
	private ErrorMessageDB() {
	}
	

	/** Set of partitions that currently have errors.
	 *
	 *  @return Set of ValidationError.Partition
	 */
	public Set getErrorPartitions() {
		// keySet() always returns non-null so we're ok just passing this on.
		return Collections.unmodifiableSet(errorSets.keySet());
	}

	
	/** Retrieves the list of errors associated with a particular partition
	 *
	 *  @param partition to get errors for.
	 *  @return List of ValidationErrors, null if no errors.
	 */
	public List getErrors(ValidationError.Partition partition) {
		// unmodifiableList does not support a null parameter, but if there are
		// no errors, the list will be null so we have to pass the null through.
		List errorList = (List) errorSets.get(partition);
		if(errorList != null) {
			errorList = Collections.unmodifiableList(errorList);
		}
		return errorList;
	}

	
	/** Adds a collection of errors to the error list.  Duplicates will overwrite
	 *  any matching existing error if the message is different.  (This allows
	 *  for a message to change as the user types, for example, from "null is 
	 *  invalid" to "invalid character".
	 *
	 *  @param errors Collection of error messages.
	 */
	public void addErrors(Collection errors) {
		for(Iterator iter = errors.iterator(); iter.hasNext(); ) {
			addErrorImpl((ValidationError) iter.next());
		}
	}
	
	
	/** Adds an error string to the error list.  Duplicates will overwrite
	 *  any matching existing error if the message is different.  (This allows
	 *  for a message to change as the user types, for example, from "null is 
	 *  invalid" to "invalid character".
	 *
	 *  @param error error message
	 */
	public void addError(ValidationError error) {
		addErrorImpl(error);
	}
	
	
	/** Remove the specified message from the database.  The error passed in
	 *  does not have to have a message field, only a partition and a fieldId,
	 *  If this error is the last one in it's partition, that partition will also
	 *  be removed.
	 *
	 *  @param error Matches the error to be removed using the algorithm provided
	 *    by ValidationError.equals().
	 */
	public void removeError(ValidationError error) {
		List errorList = (List) errorSets.get(error.getPartition());
		if(errorList != null) {
			int index = errorList.indexOf(error);
			if(index != -1) {
				errorList.remove(index);
				firePartitionStateChanged(error.getPartition(), true, (errorList.size() != 0));
			}

			if(errorList.size() == 0) {
				errorSets.remove(error.getPartition());
				
				// If we've removed all the partitions, then the bean is now valid,
				// (possibly temporarily, but we can batch things later if that is
				// a problem.
				if(errorSets.size() == 0) {
					fireValidationStateChanged(true);
				}
			}
		}
	}
	
	
	/** Update the message for this field.  This means add or change the message
	 *  if this error contains a message, or remove this message if it does not.
	 *
	 *  @param error Matches the error to be removed using the algorithm provided
	 *    by ValidationError.equals().
	 */
	public void updateError(ValidationError error) {
		if(Utils.notEmpty(error.getMessage())) {
			addErrorImpl(error);
		} else {
			removeError(error);
		}
	}
	
	
	private void addErrorImpl(ValidationError error) {
		// Save whether or not we were valid before this addition
		boolean oldIsValid = (errorSets.size() == 0);
		
		List errorList = getOrCreateErrorList(error.getPartition());
		
		// Save whether or not this partition had errors before this addition
		boolean oldHasMessages = (errorList.size() != 0);
		
		if(errorList.contains(error)) {
			errorList.remove(error);
		}
		
		errorList.add(error);
		
		// If we were valid before, we're not now.
		if(oldIsValid) {
			fireValidationStateChanged(false);
		}
		
		// We added or changed a message, thus the partition has changed
		firePartitionStateChanged(error.getPartition(), oldHasMessages, true);
	}
	
	
	private List getOrCreateErrorList(ValidationError.Partition partition) {
		List errorList = (List) errorSets.get(partition);

		if(errorList == null) {
			errorList = new ArrayList();
			errorSets.put(partition, errorList);
		}
		
		return errorList;
	}
	
	
	/** Clears out all error messages.
	 */
	public void clearErrors() {
		int numInvalidPartitions = errorSets.size();
		
		// Is there anything to do?
		if(numInvalidPartitions > 0) {
			Collection partitionList = new ArrayList(errorSets.keySet());
			errorSets.clear();
			
			// fire events to indicate that all that was invalid before is now valid.
			fireValidationStateChanged(true);
			for(Iterator iter = partitionList.iterator(); iter.hasNext(); ) {
				firePartitionStateChanged((ValidationError.Partition) iter.next(), true, false);
			}
		}
	}
	
	
	/** Test if there are currently any errors. 
	 *  @return true if there are errors of any kind, false if not.
	 */
	public boolean hasErrors() {
		return (errorSets.size() > 0);
	}
	
	
	/** Test if there are any errors in the specified partition
	 *  @return true if there are errors in the partition, false otherwise.
	 */
	public boolean hasErrors(ValidationError.Partition partition) {
		boolean result = false;
		
		List errorList = (List) errorSets.get(partition);
		if(errorList != null && errorList.size() > 0) {
			result = true;
		}
		
		return result;
	}

	
	/** -----------------------------------------------------------------------
	 *  Property change support
	 */
	public static final String VALIDATION_STATE_CHANGED = "validationStateChanged";
	public static final String PARTITION_STATE_CHANGED = "partitionStateChanged";
	
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	
	public void addPropertyChangeListener(PropertyChangeListener pCL) {
		propertyChangeSupport.addPropertyChangeListener(pCL);
	}	
	
	public void removePropertyChangeListener(PropertyChangeListener pCL) {
		propertyChangeSupport.removePropertyChangeListener(pCL);
	}
	
	private void fireValidationStateChanged(boolean newState) {
		propertyChangeSupport.firePropertyChange(VALIDATION_STATE_CHANGED, !newState, newState);
	}
	
	private void firePartitionStateChanged(ValidationError.Partition partition, boolean oldHasMessages, boolean newHasMessages) {
		PartitionState oldPartitionState = new PartitionState(partition, oldHasMessages);
		PartitionState newPartitionState = new PartitionState(partition, newHasMessages);
		propertyChangeSupport.firePropertyChange(PARTITION_STATE_CHANGED, oldPartitionState, newPartitionState);
	}
	
	public static class PartitionState {
		private final ValidationError.Partition thePartition;
		private final boolean hasMessages;
		
		private PartitionState(final ValidationError.Partition partition, final boolean hasMsgs) {
			thePartition = partition;
			hasMessages = hasMsgs;
		}
		
		public ValidationError.Partition getPartition() {
			return thePartition;
		}
		
		public boolean hasMessages() {
			return hasMessages;
		}
	};
	
	
	/** -----------------------------------------------------------------------
	 *  Public API to retrieve the error db for a bean.  Currently the DB
	 *  is a bean member, but having the API here allows us to change this
	 *  later if better optimized message storage is possible.
	 */
	public static ErrorMessageDB getMessageDB(Base bean) {
		ErrorMessageDB messageDB = null;
		
		// Sometimes this method is called with a null bean because a customizer
		// is validating but has not been initialized with a bean yet.  For such
		// cases, returning null is quite acceptable.
		if(bean != null) {
			messageDB = bean.getMessageDB();
		}
		
		return messageDB;
	}
        
	/** This is used by Base to create it's internal message DB.  Should not be public.
	 */
	static ErrorMessageDB createMessageDB() {
		return new ErrorMessageDB();
	}
}
