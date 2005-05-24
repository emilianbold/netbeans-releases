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
 * PluginData.java
 *
 * Created on November 18, 2004, 12:20 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface PluginData extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String AUTO_GENERATE_SQL = "AutoGenerateSql";	// NOI18N
    public static final String CLIENT_JAR_PATH = "ClientJarPath";	// NOI18N
    public static final String CLIENT_ARGS = "ClientArgs";	// NOI18N

        
    /** Setter for auto-generate-sql property
     * @param value property value
     */
    public void setAutoGenerateSql(java.lang.String value);
    /** Getter for auto-generate-sql property.
     * @return property value
     */
    public java.lang.String getAutoGenerateSql();
    /** Setter for client-jar-path property
     * @param value property value
     */
    public void setClientJarPath(java.lang.String value);
    /** Getter for client-jar-path property.
     * @return property value
     */
    public java.lang.String getClientJarPath();
    /** Setter for client-args property
     * @param value property value
     */
    public void setClientArgs(java.lang.String value);
    /** Getter for client-args property.
     * @return property value
     */
    public java.lang.String getClientArgs();
    
}
