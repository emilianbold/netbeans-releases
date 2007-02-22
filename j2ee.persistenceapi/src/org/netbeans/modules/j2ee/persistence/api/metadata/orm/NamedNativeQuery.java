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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.persistence.api.metadata.orm;

public interface NamedNativeQuery {
    
    public void setName(String value);
    
    public String getName();
    
    public void setResultClass(String value);
    
    public String getResultClass();
    
    public void setResultSetMapping(String value);
    
    public String getResultSetMapping();
    
    public void setQuery(String value);
    
    public String getQuery();
    
    public void setHint(int index, QueryHint value);
    
    public QueryHint getHint(int index);
    
    public int sizeHint();
    
    public void setHint(QueryHint[] value);
    
    public QueryHint[] getHint();
    
    public int addHint(QueryHint value);
    
    public int removeHint(QueryHint value);
    
    public QueryHint newQueryHint();
    
}
