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
