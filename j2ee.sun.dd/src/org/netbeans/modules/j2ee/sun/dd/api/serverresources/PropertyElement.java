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
 * PropertyElement.java
 *
 * Created on November 21, 2004, 4:47 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.serverresources;

public interface PropertyElement {
    
        public static final String NAME = "Name";	// NOI18N
	public static final String VALUE = "Value";	// NOI18N
	public static final String DESCRIPTION = "Description";	// NOI18N
        
	public void setName(java.lang.String value);

	public java.lang.String getName();

	public void setValue(java.lang.String value);

	public java.lang.String getValue();

	public void setDescription(String value);

	public String getDescription();

}
