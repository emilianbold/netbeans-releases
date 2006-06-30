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
