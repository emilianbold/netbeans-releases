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
 * PrefetchDisabled.java
 *
 * Created on November 18, 2004, 3:48 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author Nitya Doraisamy
 */
public interface PrefetchDisabled extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String QUERY_METHOD = "QueryMethod";	// NOI18N
    
    public QueryMethod[] getQueryMethod(); 
    public QueryMethod getQueryMethod(int index); 
    public void setQueryMethod(QueryMethod[] value); 
    public void setQueryMethod(int index, QueryMethod value); 
    public int addQueryMethod(QueryMethod value); 
    public int removeQueryMethod(QueryMethod value); 
    public int sizeQueryMethod();
    public QueryMethod newQueryMethod();  
}
