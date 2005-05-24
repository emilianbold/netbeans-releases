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
 * ConnectorConnectionPool.java
 *
 * Created on November 21, 2004, 2:33 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.serverresources;
/**
 * @author Nitya Doraisamy
 */
public interface ConnectorConnectionPool {
    
        public static final String NAME = "Name";	// NOI18N
	public static final String RESOURCEADAPTERNAME = "ResourceAdapterName";	// NOI18N
	public static final String CONNECTIONDEFINITIONNAME = "ConnectionDefinitionName";	// NOI18N
	public static final String STEADYPOOLSIZE = "SteadyPoolSize";	// NOI18N
	public static final String MAXPOOLSIZE = "MaxPoolSize";	// NOI18N
	public static final String MAXWAITTIMEINMILLIS = "MaxWaitTimeInMillis";	// NOI18N
	public static final String POOLRESIZEQUANTITY = "PoolResizeQuantity";	// NOI18N
	public static final String IDLETIMEOUTINSECONDS = "IdleTimeoutInSeconds";	// NOI18N
	public static final String FAILALLCONNECTIONS = "FailAllConnections";	// NOI18N
	public static final String DESCRIPTION = "Description";	// NOI18N
	public static final String SECURITY_MAP = "SecurityMap";	// NOI18N
	public static final String PROPERTY = "PropertyElement";	// NOI18N
        
	public void setName(java.lang.String value);

	public java.lang.String getName();

	public void setResourceAdapterName(java.lang.String value);

	public java.lang.String getResourceAdapterName();

	public void setConnectionDefinitionName(java.lang.String value);

	public java.lang.String getConnectionDefinitionName();

	public void setSteadyPoolSize(java.lang.String value);

	public java.lang.String getSteadyPoolSize();

	public void setMaxPoolSize(java.lang.String value);

	public java.lang.String getMaxPoolSize();

	public void setMaxWaitTimeInMillis(java.lang.String value);

	public java.lang.String getMaxWaitTimeInMillis();

	public void setPoolResizeQuantity(java.lang.String value);

	public java.lang.String getPoolResizeQuantity();

	public void setIdleTimeoutInSeconds(java.lang.String value);

	public java.lang.String getIdleTimeoutInSeconds();

	public void setFailAllConnections(java.lang.String value);

	public java.lang.String getFailAllConnections();

	public void setDescription(String value);

	public String getDescription();

	public void setSecurityMap(int index, SecurityMap value);

	public SecurityMap getSecurityMap(int index);

	public int sizeSecurityMap();

	public void setSecurityMap(SecurityMap[] value);

	public SecurityMap[] getSecurityMap();

	public int addSecurityMap(SecurityMap value);

	public int removeSecurityMap(SecurityMap value);

	public SecurityMap newSecurityMap();

	public void setPropertyElement(int index, PropertyElement value);
	public PropertyElement getPropertyElement(int index);
	public int sizePropertyElement();
	public void setPropertyElement(PropertyElement[] value);
	public PropertyElement[] getPropertyElement();
	public int addPropertyElement(PropertyElement value);
	public int removePropertyElement(PropertyElement value);
	public PropertyElement newPropertyElement();
}
