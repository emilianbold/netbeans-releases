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
 * ResourceAdapterConfig.java
 *
 * Created on November 21, 2004, 4:47 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.serverresources;
/**
 * @author Nitya Doraisamy
 */
public interface ResourceAdapterConfig {
    
        public static final String NAME = "Name";	// NOI18N
	public static final String THREADPOOLIDS = "ThreadPoolIds";	// NOI18N
	public static final String RESOURCEADAPTERNAME = "ResourceAdapterName";	// NOI18N
	public static final String PROPERTY = "PropertyElement";	// NOI18N
        
        /** Setter for name property
        * @param value property value
        */
	public void setName(java.lang.String value);
        /** Getter for name property
        * @return property value
        */
	public java.lang.String getName();
        /** Setter for thread-pool-ids property
        * @param value property value
        */
	public void setThreadPoolIds(java.lang.String value);
        /** Getter for thread-pool-ids property
        * @return property value
        */
	public java.lang.String getThreadPoolIds();
        /** Setter for resource-adapter-name property
        * @param value property value
        */
	public void setResourceAdapterName(java.lang.String value);
        /** Getter for resource-adapter-name property
        * @return property value
        */
	public java.lang.String getResourceAdapterName();

	public void setPropertyElement(int index, PropertyElement value);
	public PropertyElement getPropertyElement(int index);
	public int sizePropertyElement();
	public void setPropertyElement(PropertyElement[] value);
	public PropertyElement[] getPropertyElement();
	public int addPropertyElement(PropertyElement value);
	public int removePropertyElement(PropertyElement value);
	public PropertyElement newPropertyElement();

}
