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

package org.netbeans.modules.j2ee.sun.share;

/** Constants used in through-out the deployment
 * module
 * @author vbk
 */
public interface Constants {
    
    /** The resource bundle that holds the localizable
     * strings that are used in the deployment
     * configuration.
     */    
    java.util.ResourceBundle bundle = 
        java.util.ResourceBundle.getBundle("org.netbeans.modules.j2ee.sun.share.NewBundle");
    
    java.util.logging.Logger jsr88Logger =
        java.util.logging.Logger.getLogger("com.sun.enterprise.tools.jsr88.spi");

	String DEFAULT_PMF_JNDINAME = "jdo/pmf"; //NOI18N
	
	String DEFAULT_PRINCIPAL_NAME = "defaultName"; //NOI18N
	String DEFAULT_PRINCIPAL_PASSWORD = "defaultPassword"; //NOI18N
    
    // Property event string
    public static final String USER_DATA_CHANGED = "UserDataChanged";  // NOI18N
	
	// Localize access to schema2beans Common object
	public static final int USE_DEFAULT_VALUES = org.netbeans.modules.schema2beans.Common.USE_DEFAULT_VALUES;
	public static final int NO_DEFAULT_VALUES = org.netbeans.modules.schema2beans.Common.NO_DEFAULT_VALUES;
        
        public static final String EMPTY_STRING = "";   //NOI18N
	
}
