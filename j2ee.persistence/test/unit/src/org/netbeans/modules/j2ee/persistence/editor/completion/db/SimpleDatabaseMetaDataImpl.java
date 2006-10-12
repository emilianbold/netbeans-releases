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

package org.netbeans.modules.j2ee.persistence.editor.completion.db;

import java.sql.ResultSet;
import org.netbeans.modules.db.test.jdbcstub.JDBCStubUtil;
import org.netbeans.test.stub.api.StubDelegate;

/**
 * A simple implementation of DatabaseMetaData which just return the properties
 * gived in the constructor or set using setters.
 *
 * @author Andrei Badea
 */
public class SimpleDatabaseMetaDataImpl extends StubDelegate {
    
    private String[] catalogNames;
    private String[] schemaNames;
    private String[][] tableNamesBySchema;

    public SimpleDatabaseMetaDataImpl(String[] catalogNames) {
        this.catalogNames = catalogNames;
    }
    
    public SimpleDatabaseMetaDataImpl(String[] catalogNames, String[] schemaNames, String[][] tableNamesBySchema) {
        this.catalogNames = catalogNames;
        this.schemaNames = schemaNames;
        this.tableNamesBySchema = tableNamesBySchema;
    }

    public ResultSet getCatalogs() {
        return JDBCStubUtil.catalogsResultSet(catalogNames);
    }
    
    public ResultSet getSchemas() {
        return JDBCStubUtil.schemasResultSet(schemaNames);
    }
    
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) {
        return JDBCStubUtil.tablesResultSet(tableNamesBySchema);
    }

    public void setCatalogs(String[] catalogNames) {
        this.catalogNames = catalogNames;
    }
    
    public void setTables(String[][] tableNamesBySchema) {
        this.tableNamesBySchema = tableNamesBySchema;
    }
}
