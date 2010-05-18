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

/*
 * 
 * Copyright 2005 Sun Microsystems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.netbeans.modules.jdbcwizard.builder;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;

/**
 * Captures database foreign key metadata associated with a specific database table column.
 * 
 * @author
 */
public class ForeignKeyColumn extends KeyColumn {
    private static final String RS_PK_NAME = "PK_NAME"; // NOI18N

    private static final String RS_PKCATALOG_NAME = "PKTABLE_CAT"; // NOI18N

    private static final String RS_PKSCHEMA_NAME = "PKTABLE_SCHEM"; // NOI18N

    private static final String RS_PKTABLE_NAME = "PKTABLE_NAME"; // NOI18N

    private static final String RS_PKCOLUMN_NAME = "PKCOLUMN_NAME"; // NOI18N

    private static final String RS_FK_NAME = "FK_NAME"; // NOI18N

    private static final String RS_FKCOLUMN_NAME = "FKCOLUMN_NAME"; // NOI18N

    private static final String RS_UPDATE_RULE = "UPDATE_RULE"; // NOI18N

    private static final String RS_DELETE_RULE = "DELETE_RULE"; // NOI18N

    private static final String RS_DEFERRABILITY = "DEFERRABILITY"; // NOI18N

    /*
     * name of catalog containing foreign table whose primary key column is associated with this
     * foreign key
     */
    private String importCatalogName;

    /*
     * name of schema referencing foreign table whose primary key column is associated with this
     * foreign key
     */
    private String importSchemaName;

    /*
     * name of foreign table whose primary key column is associated with this foreign key
     */
    private String importTableName;

    /* name of primary key column assocaited with this foreign key */
    private String importColumnName;

    /* name of import (primary) key associated with this foreign key */
    private String importKeyName;

    /* short flag indicating applicable update rule for this constraint */
    private short updateRule;

    /* short flag indicating applicable delete rule for this constraint */
    private short deleteRule;

    /* short flag indicating policy on evaluation of this constraint */
    private short deferrability;

    /**
     * Creates a List of ForeignKeyColumn instances from the given ResultSet.
     * 
     * @param rs ResultSet containing foreign key metadata as obtained from DatabaseMetaData
     * @return List of ForeignKeyColumn instances based from metadata in rs
     * @throws SQLException if SQL error occurs while reading in data from given ResultSet
     */
    public static List createForeignKeyColumnList(final ResultSet rs) throws SQLException {
        if (rs == null) {
            final ResourceBundle cMessages = NbBundle.getBundle(ForeignKeyColumn.class);// NO i18n
            throw new IllegalArgumentException(cMessages.getString("ERROR_NULL_RS") + "(ERROR_NULL_RS)");
        }

        List fkColumns = Collections.EMPTY_LIST;

        if (rs != null && rs.next()) {
            fkColumns = new ArrayList();

            do {
                fkColumns.add(new ForeignKeyColumn(rs));
            } while (rs.next());
        }

        return fkColumns;
    }

    /**
     * Creates an instance of ForeignKeyColumn with the given values.
     * 
     * @param fkName name of FK
     * @param fkColumn name of column assocaited with FK
     * @param pkName name of PK that this FK imports
     * @param pkColumn name of column that this FK imports
     * @param pkTable name of table containing column that this FK imports
     * @param pkSchema name of schema containing table with PK that this FK imports
     * @param pkCatalog name of catalog containing table with PK that this FK imports
     * @param colSequence sequence of this column within (composite) primary key
     * @param updateFlag applicable update rule for this FK; one of
     *            java.sql.DatabaseMetaData.importedKeyNoAction,
     *            java.sql.DatabaseMetaData.importedKeyCascade,
     *            java.sql.DatabaseMetaData.importedKeySetNull,
     *            java.sql.DatabaseMetaData#importedKeySetDefault, or
     *            java.sql.DatabaseMetaData#importedKeyRestrict
     * @param deleteFlag applicable delete rule for this FK; one of
     *            java.sql.DatabaseMetaData.importedKeyNoAction,
     *            java.sql.DatabaseMetaData.importedKeyCascade,
     *            java.sql.DatabaseMetaData.importedKeySetNull,
     *            java.sql.DatabaseMetaData.importedKeyRestrict, or
     *            java.sql.DatabaseMetaData.importedKeySetDefault
     * @param deferFlag deferrability flag for this FK; one of
     *            java.sql.DatabaseMetaData.importedKeyInitiallyDeferred,
     *            java.sql.DatabaseMetaData.importedKeyInitiallyImmediate, or
     *            java.sql.DatabaseMetaData.importedKeyNotDeferrable
     * @see java.sql.DatabaseMetaData#importedKeyCascade
     * @see java.sql.DatabaseMetaData#importedKeyInitiallyDeferred
     * @see java.sql.DatabaseMetaData#importedKeyInitiallyImmediate
     * @see java.sql.DatabaseMetaData#importedKeyNoAction
     * @see java.sql.DatabaseMetaData#importedKeyNotDeferrable
     * @see java.sql.DatabaseMetaData#importedKeyRestrict
     * @see java.sql.DatabaseMetaData#importedKeySetNull
     * @see java.sql.DatabaseMetaData#importedKeySetDefault
     */
    public ForeignKeyColumn(final String fkName, final String fkColumn, final String pkName, final String pkColumn, final String pkTable,
            final String pkSchema, final String pkCatalog, final short colSequence, final short updateFlag, final short deleteFlag, final short deferFlag) {
        super(fkName, fkColumn, colSequence);

        this.importKeyName = pkName;
        this.importCatalogName = pkCatalog;
        this.importSchemaName = pkSchema;
        this.importTableName = pkTable;
        this.importColumnName = pkColumn;

        this.setUpdateRule(updateFlag);
        this.setDeleteRule(deleteFlag);
        this.setDeferrability(deferFlag);
    }

    public ForeignKeyColumn(final ForeignKeyColumn fkCol) {
        super(fkCol.getName(), fkCol.getColumnName(), fkCol.getColumnSequence());

        this.importKeyName = fkCol.getImportKeyName();
        this.importCatalogName = fkCol.getImportCatalogName();
        this.importSchemaName = fkCol.getImportSchemaName();
        this.importTableName = fkCol.getImportTableName();
        this.importColumnName = fkCol.getImportColumnName();

        this.setUpdateRule(fkCol.getUpdateRule());
        this.setDeleteRule(fkCol.getDeleteRule());
        this.setDeferrability(fkCol.getDeferrability());
    }

    private ForeignKeyColumn(final ResultSet rs) throws SQLException {
        if (rs == null) {
            final ResourceBundle cMessages = NbBundle.getBundle(ForeignKeyColumn.class);// NO i18n
            throw new IllegalArgumentException(cMessages.getString("ERROR_VALID_RS") + "(ERROR_VALID_RS)");
        }

        this.importCatalogName = rs.getString(ForeignKeyColumn.RS_PKCATALOG_NAME);
        this.importSchemaName = rs.getString(ForeignKeyColumn.RS_PKSCHEMA_NAME);
        this.importTableName = rs.getString(ForeignKeyColumn.RS_PKTABLE_NAME);
        this.importColumnName = rs.getString(ForeignKeyColumn.RS_PKCOLUMN_NAME);
        this.importKeyName = rs.getString(ForeignKeyColumn.RS_PK_NAME);

        this.columnName = rs.getString(ForeignKeyColumn.RS_FKCOLUMN_NAME);
        this.keyName = rs.getString(ForeignKeyColumn.RS_FK_NAME);

        this.sequenceNum = rs.getShort(KeyColumn.RS_SEQUENCE_NUM);

        this.updateRule = rs.getShort(ForeignKeyColumn.RS_UPDATE_RULE);
        this.deleteRule = rs.getShort(ForeignKeyColumn.RS_DELETE_RULE);
        this.deferrability = rs.getShort(ForeignKeyColumn.RS_DEFERRABILITY);
    }

    /**
     * Gets name of catalog containing the import table which, in turn, contains the imported
     * (primary) key associated with this foreign key.
     * 
     * @return name of catalog containing the imported primary key's encapsulating table
     */
    public String getImportCatalogName() {
        return this.importCatalogName;
    }

    /**
     * Gets name of schema containing the import table which, in turn, contains the imported
     * (primary) key associated with this foreign key.
     * 
     * @return name of schema containing the imported primary key's encapsulating table
     */
    public String getImportSchemaName() {
        return this.importSchemaName;
    }

    /**
     * Gets name of import table containing imported (primary) key associated with this foreign key.
     * 
     * @return name of table containing imported primary key
     */
    public String getImportTableName() {
        return this.importTableName;
    }

    /**
     * Gets name of import column contained within imported (primary) key associated with this
     * foreign key.
     * 
     * @return name of imported column
     */
    public String getImportColumnName() {
        return this.importColumnName;
    }

    /**
     * Gets key name of imported (primary) key associated with this foreign key.
     * 
     * @return name of imported primary key
     */
    public String getImportKeyName() {
        return this.importKeyName;
    }

    /**
     * Gets update rule.
     * 
     * @return update rule; one of java.sql.DatabaseMetaData.importedKeyNoAction,
     *         java.sql.DatabaseMetaData.importedKeyCascade,
     *         java.sql.DatabaseMetaData.importedKeySetNull,
     *         java.sql.DatabaseMetaData.importedKeyRestrict, or
     *         java.sql.DatabaseMetaData.importedKeySetDefault.
     * @see java.sql.DatabaseMetaData#importedKeyNoAction
     * @see java.sql.DatabaseMetaData#importedKeyCascade
     * @see java.sql.DatabaseMetaData#importedKeySetNull
     * @see java.sql.DatabaseMetaData#importedKeyRestrict
     * @see java.sql.DatabaseMetaData#importedKeySetDefault
     */
    public short getUpdateRule() {
        return this.updateRule;
    }

    /**
     * Gets delete rule.
     * 
     * @return update rule; one of java.sql.DatabaseMetaData.importedKeyNoAction,
     *         java.sql.DatabaseMetaData.importedKeyCascade,
     *         java.sql.DatabaseMetaData.importedKeySetNull,
     *         java.sql.DatabaseMetaData.importedKeyRestrict, or
     *         java.sql.DatabaseMetaData.importedKeySetDefault.
     * @see java.sql.DatabaseMetaData#importedKeyNoAction
     * @see java.sql.DatabaseMetaData#importedKeyCascade
     * @see java.sql.DatabaseMetaData#importedKeySetNull
     * @see java.sql.DatabaseMetaData#importedKeyRestrict
     * @see java.sql.DatabaseMetaData#importedKeySetDefault
     */
    public short getDeleteRule() {
        return this.deleteRule;
    }

    /**
     * Gets deferrability flag.
     * 
     * @return deferrability flag; one of java.sql.DatabaseMetaData.importedKeyInitiallyDeferred,
     *         java.sql.DatabaseMetaData.importedKeyInitiallyImmediate, or
     *         java.sql.DatabaseMetaData.importedKeyNotDeferrable
     * @see java.sql.DatabaseMetaData#importedKeyInitiallyDeferred,
     * @see java.sql.DatabaseMetaData#importedKeyInitiallyImmediate, or
     * @see java.sql.DatabaseMetaData#importedKeyNotDeferrable
     */
    public short getDeferrability() {
        return this.deferrability;
    }

    private void setUpdateRule(final short newRule) {
        switch (newRule) {
        case DatabaseMetaData.importedKeyNoAction:
        case DatabaseMetaData.importedKeyCascade:
        case DatabaseMetaData.importedKeySetNull:
        case DatabaseMetaData.importedKeySetDefault:
        case DatabaseMetaData.importedKeyRestrict:
            this.updateRule = newRule;
            break;

        default:
            final ResourceBundle cMessages = NbBundle.getBundle(ForeignKeyColumn.class);// NO i18n

            throw new IllegalArgumentException(cMessages.getString("ERROR_VALID_RULE") + "(ERROR_VALID_RULE)");
        }
    }

    private void setDeleteRule(final short newRule) {
        switch (newRule) {
        case DatabaseMetaData.importedKeyNoAction:
        case DatabaseMetaData.importedKeyCascade:
        case DatabaseMetaData.importedKeySetNull:
        case DatabaseMetaData.importedKeySetDefault:
        case DatabaseMetaData.importedKeyRestrict:
            this.deleteRule = newRule;
            break;

        default:
            final ResourceBundle cMessages = NbBundle.getBundle(ForeignKeyColumn.class);// NO i18n

            throw new IllegalArgumentException(cMessages.getString("ERROR_VALID_RULE") + "(ERROR_VALID_RULE)");
        }
    }

    private void setDeferrability(final short newFlag) {
        switch (newFlag) {
        case DatabaseMetaData.importedKeyInitiallyDeferred:
        case DatabaseMetaData.importedKeyInitiallyImmediate:
        case DatabaseMetaData.importedKeyNotDeferrable:
            this.deferrability = newFlag;
            break;

        default:
            // System.err.println(
            // "Received unrecognized value for newFlag, but carrying on with it anyway.");
            this.deferrability = newFlag;
            break;
        }
    }
}
