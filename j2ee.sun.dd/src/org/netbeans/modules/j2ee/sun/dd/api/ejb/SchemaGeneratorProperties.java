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
 * SchemaGeneratorProperties.java
 *
 * Created on November 18, 2004, 10:47 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface SchemaGeneratorProperties extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String PROPERTY = "PropertyElement";	// NOI18N
    
    public PropertyElement[] getPropertyElement();
    public PropertyElement getPropertyElement(int index);
    public void setPropertyElement(PropertyElement[] value);
    public void setPropertyElement(int index, PropertyElement value);
    public int addPropertyElement(PropertyElement value);
    public int removePropertyElement(PropertyElement value); 
    public int sizePropertyElement();
    public PropertyElement newPropertyElement();
}
