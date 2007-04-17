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
package org.netbeans.modules.j2ee.sun.dd.api.cmp;

public interface CmpFieldMapping extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String FIELD_NAME = "FieldName"; // NOI18N
    public static final String COLUMN_NAME = "ColumnName"; // NOI18N
    public static final String READ_ONLY = "ReadOnly"; // NOI18N
    public static final String FETCHED_WITH = "FetchedWith"; // NOI18N

    public void setFieldName(String value);
    public String getFieldName();

    public void setColumnName(int index, String value);
    public String getColumnName(int index);
    public int sizeColumnName();
    public void setColumnName(String[] value);
    public String[] getColumnName();
    public int addColumnName(String value);
    public int removeColumnName(String value);

    public void setReadOnly(boolean value);
    public boolean isReadOnly();

    public void setFetchedWith(FetchedWith value);
    public FetchedWith getFetchedWith();
    public FetchedWith newFetchedWith();

}
