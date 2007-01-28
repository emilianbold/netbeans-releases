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
package org.netbeans.modules.db.sql.visualeditor.querymodel;

import java.util.List;

// A FROM clause
public interface From extends QueryItem {
    public void addTable(JoinTable joinTable);
    public String getPreviousTableFullName();
    public Table findTable(String tableSpec);
    public JoinTable findJoinTable(String table1, String column1, String table2, String column2);
    public String getFullTableName(String corrName);
    public String getTableSpec(String fullTableName);

    // verify
    public void setTableSpec(String oldTableSpec, String newTableSpec);
    public List getTableList();
}


