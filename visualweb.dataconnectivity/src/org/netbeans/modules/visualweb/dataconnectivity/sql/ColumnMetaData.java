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
package org.netbeans.modules.visualweb.dataconnectivity.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * just in time Column data used by servernavigator and other clients
 * cached after retrieved
 *
 * @author John Kline
 */
public class ColumnMetaData {
/*

    private static ResourceBundle rb = ResourceBundle.getBundle("org.netbeans.modules.visualweb.dataconnectivity.sql.Bundle",
        Locale.getDefault());
 */

    public static class ColIndex implements ColumnMetaDataHelper.MetaIndex {
        private String name;
        private int    index;
        private ColIndex(String name, int index) {this.name = name; this.index = index;}
        public String getName() {return name;};
        public int getIndex() {return index;};
    }

    public static ColIndex TABLE_CAT   = new ColIndex("TABLE_CAT", 0);
    public static ColIndex TABLE_SCHEM = new ColIndex("TABLE_SCHEM", 1);
    public static ColIndex TABLE_NAME  = new ColIndex("TABLE_NAME", 2);
    public static ColIndex COLUMN_NAME  = new ColIndex("COLUMN_NAME", 3);
    public static ColIndex DATA_TYPE  = new ColIndex("DATA_TYPE", 4);
    public static ColIndex TYPE_NAME  = new ColIndex("TYPE_NAME", 5);
    public static ColIndex COLUMN_SIZE  = new ColIndex("COLUMN_SIZE", 6);
    public static ColIndex BUFFER_LENGTH  = new ColIndex("BUFFER_LENGTH", 7);
    public static ColIndex DECIMAL_DIGITS  = new ColIndex("DECIMAL_DIGITS", 8);
    public static ColIndex NUM_PREC_RADIX  = new ColIndex("NUM_PREC_RADIX", 9);
    public static ColIndex NULLABLE  = new ColIndex("NULLABLE", 10);
    public static ColIndex REMARKS  = new ColIndex("REMARKS", 11);
    public static ColIndex COLUMN_DEF  = new ColIndex("COLUMN_DEF", 12);
    public static ColIndex SQL_DATA_TYPE  = new ColIndex("SQL_DATA_TYPE", 13);
    public static ColIndex SQL_DATETIME_SUB  = new ColIndex("SQL_DATETIME_SUB", 14);
    public static ColIndex CHAR_OCTET_LENGTH  = new ColIndex("CHAR_OCTET_LENGTH", 15);
    public static ColIndex ORDINAL_POSITION  = new ColIndex("ORDINAL_POSITION", 16);
    public static ColIndex IS_NULLABLE  = new ColIndex("IS_NULLABLE", 17);
    public static ColIndex SCOPE_CATALOG  = new ColIndex("SCOPE_CATALOG", 18);
    public static ColIndex SCOPE_SCHEMA  = new ColIndex("SCOPE_SCHEMA", 19);
    public static ColIndex SCOPE_TABLE  = new ColIndex("SCOPE_TABLE", 20);
    public static ColIndex SOURCE_DATA_TYPE  = new ColIndex("SOURCE_DATA_TYPE", 21);

    private static ColIndex[] metaIndicies = { TABLE_CAT, TABLE_SCHEM, TABLE_NAME, COLUMN_NAME,
        DATA_TYPE, TYPE_NAME, COLUMN_SIZE, BUFFER_LENGTH, DECIMAL_DIGITS, NUM_PREC_RADIX, NULLABLE,
        REMARKS, COLUMN_DEF, SQL_DATA_TYPE, SQL_DATETIME_SUB, CHAR_OCTET_LENGTH, ORDINAL_POSITION,
        IS_NULLABLE, SCOPE_CATALOG, SCOPE_SCHEMA, SCOPE_TABLE, SOURCE_DATA_TYPE };

    private ColumnMetaDataHelper helper = null;

    ColumnMetaData(ResultSet resultSet) throws SQLException {
        helper = new ColumnMetaDataHelper(metaIndicies, resultSet);
    }

    public Object getMetaInfo(ColIndex metaIndex) throws SQLException {
        return helper.getMetaInfo(metaIndex);
    }

    public String getMetaInfoAsString(ColIndex metaIndex) throws SQLException {
        return helper.getMetaInfoAsString(metaIndex);
    }
}
