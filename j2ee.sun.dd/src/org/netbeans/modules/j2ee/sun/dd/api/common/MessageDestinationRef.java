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
 * MessageDestinationRef.java
 *
 * Created on August 02, 2005, 5:19 PM
 */
package org.netbeans.modules.j2ee.sun.dd.api.common;

public interface MessageDestinationRef extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean { 
    
        public static final String MESSAGE_DESTINATION_REF_NAME = "MessageDestinationRefName";	// NOI18N
	public static final String JNDI_NAME = "JndiName";	// NOI18N

	public void setMessageDestinationRefName(String value);

	public String getMessageDestinationRefName();

	public void setJndiName(String value);

	public String getJndiName();

}
