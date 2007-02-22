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

public interface TableGenerator {
    
    public void setName(String value);
    
    public String getName();
    
    public void setTable(String value);
    
    public String getTable();
    
    public void setCatalog(String value);
    
    public String getCatalog();
    
    public void setSchema(String value);
    
    public String getSchema();
    
    public void setPkColumnName(String value);
    
    public String getPkColumnName();
    
    public void setValueColumnName(String value);
    
    public String getValueColumnName();
    
    public void setPkColumnValue(String value);
    
    public String getPkColumnValue();
    
    public void setInitialValue(int value);
    
    public int getInitialValue();
    
    public void setAllocationSize(int value);
    
    public int getAllocationSize();
    
    public void setUniqueConstraint(int index, UniqueConstraint value);
    
    public UniqueConstraint getUniqueConstraint(int index);
    
    public int sizeUniqueConstraint();
    
    public void setUniqueConstraint(UniqueConstraint[] value);
    
    public UniqueConstraint[] getUniqueConstraint();
    
    public int addUniqueConstraint(UniqueConstraint value);
    
    public int removeUniqueConstraint(UniqueConstraint value);
    
    public UniqueConstraint newUniqueConstraint();
    
}
