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

public interface SecondaryTable extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String TABLE_NAME = "TableName"; // NOI18N
    public static final String COLUMN_PAIR = "ColumnPair"; // NOI18N

    public void setTableName(String value);
    public String getTableName();

    public void setColumnPair(int index, ColumnPair value);
    public ColumnPair getColumnPair(int index);
    public int sizeColumnPair();
    public void setColumnPair(ColumnPair[] value);
    public ColumnPair[] getColumnPair();
    public int addColumnPair(ColumnPair value);
    public int removeColumnPair(ColumnPair value);
    public ColumnPair newColumnPair();

}
