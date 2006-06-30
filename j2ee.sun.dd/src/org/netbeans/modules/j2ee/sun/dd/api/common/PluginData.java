/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
