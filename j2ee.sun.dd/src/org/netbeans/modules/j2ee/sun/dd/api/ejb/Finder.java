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
 * Finder.java
 *
 * Created on November 18, 2004, 11:59 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface Finder extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

     public static final String METHOD_NAME = "MethodName";	// NOI18N
     public static final String QUERY_PARAMS = "QueryParams";	// NOI18N
     public static final String QUERY_FILTER = "QueryFilter";	// NOI18N
     public static final String QUERY_VARIABLES = "QueryVariables";	// NOI18N
     public static final String QUERY_ORDERING = "QueryOrdering";	// NOI18N
        
    /** Setter for method-name property
     * @param value property value
     */
    public void setMethodName(java.lang.String value);
    /** Getter for method-name property.
     * @return property value
     */
    public java.lang.String getMethodName();
    /** Setter for query-params property
     * @param value property value
     */
    public void setQueryParams(java.lang.String value);
    /** Getter for query-params property.
     * @return property value
     */
    public java.lang.String getQueryParams();
    /** Setter for query-filter property
     * @param value property value
     */
    public void setQueryFilter(java.lang.String value);
    /** Getter for query-filter property.
     * @return property value
     */
    public java.lang.String getQueryFilter();
    /** Setter for query-variables property
     * @param value property value
     */
    public void setQueryVariables(java.lang.String value);
    /** Getter for query-variables property.
     * @return property value
     */
    public java.lang.String getQueryVariables();
    /** Setter for query-ordering property
     * @param value property value
     */
    public void setQueryOrdering(java.lang.String value);
    /** Getter for query-ordering property.
     * @return property value
     */
    public java.lang.String getQueryOrdering();
    
}
