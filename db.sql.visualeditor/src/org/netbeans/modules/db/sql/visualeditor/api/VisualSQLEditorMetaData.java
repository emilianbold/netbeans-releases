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

package org.netbeans.modules.db.sql.visualeditor.api;

import java.sql.SQLException;
import java.util.List;

/**
 * Implements a cache for metadata that is supplied to the Visual SQL Editor
 *
 * @author Joel Brown, Jim Davidson
 */
public interface VisualSQLEditorMetaData {

    /**
     * Returns the schemas that are included in this DataSource/DatabaseConnection
     * Used during Add Table and similar operations.
     *
     * @return the List of schema names
     */
    public List<String> getSchemas() ;

    /**
     * Returns the tables (and views) in this DataSource/DatabaseConnection
     * 
     * @return the List of tables/views, each in the form of a {@literal List<schema, table>}
     */
    // public List<List<String>> getTables(String schema) throws SQLException ;
    public List<List<String>> getTables() throws SQLException ;

    /**
     * Returns the columns in the specified schema/table.
     * @return a List of column names
     */
    public List<String> getColumns(String schema, String table) throws SQLException ;

    /****
     * Returns the primary key columns for the given schema/table combination.
     *
     * @return the List of columns
     */
    public List<String> getPrimaryKeys(String schema, String table) throws SQLException ;

    /***
     * Returns the imported keys for the given schema/table.
     * @return the List of imported Keys.  Each key is a List of the form
     * <br> {@literal <foreign schema, foreign table, foreign column, primary schema, primary table, primary column>}
     */
    public List<List<String>> getImportedKeys(String schema, String table) throws SQLException ;

    /***
     * Returns the exported keys for the given schema/table.
     * @return the List of exported keys.  Each key is a List of the form
     * <br> {@literal <foreign schema, foreign table, foreign column, primary schema, primary table, primary column>}
     */
    public List<List<String>> getExportedKeys(String schema, String table) throws SQLException ;

}
