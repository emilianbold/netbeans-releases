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
public class ProcedureColumnMetaData {

    private static ResourceBundle rb = ResourceBundle.getBundle("org.netbeans.modules.visualweb.dataconnectivity.sql.Bundle",
        Locale.getDefault());

    public static class ColIndex implements ColumnMetaDataHelper.MetaIndex {
        private String name;
        private int    index;
        private ColIndex(String name, int index) {this.name = name; this.index = index;}
        public String getName() {return name;};
        public int getIndex() {return index;};
    }

    public static ColIndex PROCEDURE_CAT   = new ColIndex("PROCEDURE_CAT", 0); //NOI18N
    public static ColIndex PROCEDURE_SCHEM = new ColIndex("PROCEDURE_SCHEM", 1); //NOI18N
    public static ColIndex PROCEDURE_NAME  = new ColIndex("PROCEDURE_NAME", 2); //NOI18N
    public static ColIndex COLUMN_NAME  = new ColIndex("COLUMN_NAME", 3); //NOI18N
    public static ColIndex COLUMN_TYPE  = new ColIndex("COLUMN_TYPE", 4); //NOI18N
    public static ColIndex DATA_TYPE  = new ColIndex("DATA_TYPE", 5); //NOI18N
    public static ColIndex TYPE_NAME  = new ColIndex("TYPE_NAME", 6); //NOI18N
    public static ColIndex PRECISION  = new ColIndex("PRECISION", 7); //NOI18N
    public static ColIndex LENGTH  = new ColIndex("LENGTH", 8); //NOI18N
    public static ColIndex SCALE  = new ColIndex("SCALE", 9); //NOI18N
    public static ColIndex RADIX  = new ColIndex("RADIX", 10); //NOI18N
    public static ColIndex NULLABLE  = new ColIndex("NULLABLE", 11); //NOI18N
    public static ColIndex REMARKS  = new ColIndex("REMARKS", 12); //NOI18N

    private static ColIndex[] metaIndicies = { PROCEDURE_CAT, PROCEDURE_SCHEM, PROCEDURE_NAME,
        COLUMN_NAME, COLUMN_TYPE, DATA_TYPE, TYPE_NAME, PRECISION, LENGTH, SCALE, RADIX,
        NULLABLE, REMARKS};

    private ColumnMetaDataHelper helper = null;

    ProcedureColumnMetaData(ResultSet resultSet) throws SQLException {
        helper = new ColumnMetaDataHelper(metaIndicies, resultSet);
    }

    public Object getMetaInfo(ColIndex metaIndex) throws SQLException {
        return helper.getMetaInfo(metaIndex);
    }

    public String getMetaInfoAsString(ColIndex metaIndex) throws SQLException {
        return helper.getMetaInfoAsString(metaIndex);
    }
}
