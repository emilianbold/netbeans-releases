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
 * ExternalJndiResource.java
 *
 * Created on November 21, 2004, 2:47 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.serverresources;
/**
 * @author Nitya Doraisamy
 */
public interface ExternalJndiResource {
    
        public static final String JNDINAME = "JndiName";	// NOI18N
	public static final String JNDILOOKUPNAME = "JndiLookupName";	// NOI18N
	public static final String RESTYPE = "ResType";	// NOI18N
	public static final String FACTORYCLASS = "FactoryClass";	// NOI18N
	public static final String ENABLED = "Enabled";	// NOI18N
	public static final String DESCRIPTION = "Description";	// NOI18N
	public static final String PROPERTY = "PropertyElement";	// NOI18N
        
        /** Setter for jndi-name property
        * @param value property value
        */
	public void setJndiName(java.lang.String value);
        /** Getter for jndi-name property
        * @return property value
        */
	public java.lang.String getJndiName();
        /** Setter for jndi-lookup-name property
        * @param value property value
        */
	public void setJndiLookupName(java.lang.String value);
        /** Getter for jndi-lookup-name property
        * @param value property value
        */
	public java.lang.String getJndiLookupName();
        /** Setter for res-type property
        * @param value property value
        */
	public void setResType(java.lang.String value);
        /** Getter for res-type property
        * @return property value
        */
	public java.lang.String getResType();
        /** Setter for factory-class property
        * @param value property value
        */
	public void setFactoryClass(java.lang.String value);
        /** Getter for factory-class property
        * @return property value
        */
	public java.lang.String getFactoryClass();
        /** Setter for enabled property
        * @param value property value
        */
	public void setEnabled(java.lang.String value);
        /** Getter for enabled property
        * @return property value
        */
	public java.lang.String getEnabled();
        /** Setter for description attribute
        * @param value attribute value
        */
	public void setDescription(String value);
        /** Getter for description attribute
        * @return attribute value
        */
	public String getDescription();

	public void setPropertyElement(int index, PropertyElement value);
	public PropertyElement getPropertyElement(int index);
	public int sizePropertyElement();
	public void setPropertyElement(PropertyElement[] value);
	public PropertyElement[] getPropertyElement();
	public int addPropertyElement(PropertyElement value);
	public int removePropertyElement(PropertyElement value);
	public PropertyElement newPropertyElement();

}
