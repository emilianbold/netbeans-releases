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
 * ValidationError.java
 *
 * Created on March 3, 2004, 2:15 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean;

/**
 *
 * @author Peter Williams
 */
public final class ValidationError implements Comparable {
	
	private final Partition partition;
	private final String fieldId;
	private final String message;
	
	/** Creates a new instance of ValidationError */
	private ValidationError(String fieldId, String message) {
		this(PARTITION_GLOBAL, fieldId, message);
	}
	
	/** Creates a new instance of ValidationError */
	private ValidationError(Partition partition, String fieldId, String message) {
		this.partition = partition;
		this.fieldId = fieldId;
		this.message = message;
	}
	
	/** Returns the partition.  This features is to allow us to partition error
	 *  messages by subpanel of a customizer and only display error messages
	 *  associated with the current panel.  It could have other uses as well.
	 *
	 *  @return the partition.
	 */
	public Partition getPartition() {
		return partition;
	}
	
	/** Returns the field Id, which is the absolute xpath describing this fieldId.
	 *
	 *  @return the field Id.
	 */
	public String getFieldId() {
		return fieldId;
	}
	
	/** Returns the validation message describing the error that this field
	 *  contains.
	 *
	 *  @return the validation error message.
	 */
	public String getMessage() {
		return message;
	}

	/** Two ValidationError's are equal if they hvae the same partition and
	 *  fieldId.
	 *
	 *  @param obj ValidationError to compare equality with.
	 *  @return true if equal, false otherwise.
	 */
	public boolean equals(Object obj) {
		boolean result;
		
		if(this == obj) {
			result = true;
		} else {
			ValidationError target = (ValidationError) obj;
			result = partition.equals(target.partition) && fieldId.equals(target.fieldId);
		}
		
		return result;
	}
	
	private volatile int hashCode = 0;
	
	/** Hashcode for a ValidationError object.  Overridden for consistency with
	 *  equals.
	 *
	 *  @return integer hashcode
	 */
	public int hashCode() {
		if(hashCode == 0) {
			int result = fieldId.hashCode();
			if(partition != null) {
				result = 37*result + partition.hashCode();
			}
			hashCode = result;
		}
		return hashCode;
	}
	
	/** Compare this instance of ValidationError with the target instance.
	 *  We index by partition first, then fieldId.  Partition ordering doesn't
	 *  really matter as long as members of a partition are grouped.
	 *
	 * @param Instance of ValidationError to compare with.
	 */
	public int compareTo(Object obj) {
		int result;
		
		if(this == obj) {
			result = 0;
		} else {
			ValidationError target = (ValidationError) obj;
			result = partition.compareTo(target.partition);

			if(result == 0) {
				result = fieldId.compareTo(target.fieldId);
			}
		}

		return result;
	}
	
	/** Creates a new ValidationError Object
	 *
	 *  @param fieldId Absolute Xpath of the field this messages applies to
	 *  @param message Error message describing the error in this field.
	 */
	public static ValidationError getValidationError(String fieldId, String message) {
		return new ValidationError(fieldId, message);
	}
	
	public static ValidationError getValidationErrorMask(String fieldId) {
		return new ValidationError(fieldId, "");
	}
	
	/** Creates a new ValidationError Object
	 *
	 *  @param fieldId Absolute Xpath of the field this messages applies to
	 *  @param panelId1 Customizer panel ID this field is displayed
	 *  @param message Error message describing the error in this field.
	 */
	public static ValidationError getValidationError(Partition partition, String fieldId, String message) {
		return new ValidationError(partition, fieldId, message);
	}
	
	public static ValidationError getValidationErrorMask(Partition partition, String fieldId) {
		return new ValidationError(partition, fieldId, "");
	}
	
	/** -----------------------------------------------------------------------
	 *  Partitions defined for customizer ui.
	 */
	
	// Global partition
	public static final Partition PARTITION_GLOBAL = 
		new Partition("Global");
	
	// Partitions for sun-web-app
	public static final Partition PARTITION_WEB_GENERAL = 
		new Partition("WebGeneral", 0);	// NOI18N
	public static final Partition PARTITION_SESSION_MANAGER = 
		new Partition("SessionManager", 1, 0);	// NOI18N
	public static final Partition PARTITION_SESSION_STORE = 
		new Partition("SessionStore", 1, 1);	// NOI18N
	public static final Partition PARTITION_SESSION_SESSION = 
		new Partition("SessionSession", 1, 2);	// NOI18N
	public static final Partition PARTITION_SESSION_COOKIE = 
		new Partition("SessionCookie", 1, 3);	// NOI18N
	public static final Partition PARTITION_WEB_SERVICES = 
		new Partition("WebServices", 2);	// NOI18N
	public static final Partition PARTITION_WEB_MESSAGES = 
		new Partition("WebMessages", 3);	// NOI18N
	public static final Partition PARTITION_WEB_LOCALE = 
		new Partition("WebLocale", 4);	// NOI18N
	public static final Partition PARTITION_CACHE_GENERAL = 
		new Partition("CacheGeneral", 5, 0);	// NOI18N
	public static final Partition PARTITION_CACHE_HELPERS = 
		new Partition("CacheHelpers", 5, 1);	// NOI18N
	public static final Partition PARTITION_CACHE_CONSTRAINTS = 
		new Partition("CacheConstraints", 5, 2);	// NOI18N
	
	// Partitions for SecurityRoleMapping
	public static final Partition PARTITION_SECURITY_ASSIGN = 
		new Partition("SecurityAssign", 0);	// NOI18N
	public static final Partition PARTITION_SECURITY_MASTER = 
		new Partition("SecurityMaster", 1);	// NOI18N
	
	// Partitions for ServiceRef
	public static final Partition PARTITION_SERVICEREF_GENERAL = 
		new Partition("ServiceRefGeneral", 0);	// NOI18N
	public static final Partition PARTITION_SERVICEREF_PORTINFO = 
		new Partition("ServiceRefPortInfo", 1);	// NOI18N
	
	// Partitions for ConnectorRoot
	public static final Partition PARTITION_CONNECTOR_ADAPTER = 
		new Partition("ConnectorAdapter", 0);	// NOI18N
	public static final Partition PARTITION_CONNECTOR_ROLES = 
		new Partition("ConnectorRoles", 1);	// NOI18N

	
	public static final class Partition implements Comparable {
		private final String partitionName;
		private int tabIndex;
		private int subTabIndex;

		private Partition(final String name) {
			this(name, -1, -1);
		}
		
		private Partition(final String name, final int index) {
			this(name, index, -1);
		}
		
		private Partition(final String name, final int index, final int subIndex) {
			partitionName = name;
			tabIndex = index;
			subTabIndex = subIndex;
		}

		public String toString() {
			return partitionName;
		}
		
		public int getTabIndex() {
			return tabIndex;
		}
		
		public int getSubTabIndex() {
			return subTabIndex;
		}
		
		public int compareTo(Object obj) {
			Partition target = (Partition) obj;
			return partitionName.compareTo(target.partitionName);
		}
	}
}

