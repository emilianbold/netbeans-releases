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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.ResourceBundle;
import org.openide.util.NbBundle;

/**
 * @author Jonathan Giron
 */
public class KeyColumn {
    /**
     * DatabaseMetaData ResultSet column name used to decode name of associated primary key
     */
    protected static final String RS_KEY_NAME = "PK_NAME"; // NOI18N

    private static final String RS_COLUMN_NAME = "COLUMN_NAME"; // NOI18N

    /**
     * DatabaseMetaData ResultSet column name used to decode key sequence number
     */
    protected static final String RS_SEQUENCE_NUM = "KEY_SEQ"; // NOI18N

    /** Name of column */
    protected String columnName;

    /** Name of key associated with this column */
    protected String keyName;

    /** For composite keys, sequence of this column for the associated key */
    protected int sequenceNum;

    /**
     * Creates a List of (primary) KeyColumn instances from the given ResultSet.
     * 
     * @param rs ResultSet containing primary key metadata as obtained from DatabaseMetaData
     * @return List of KeyColumn instances based from metadata in rs
     * @throws SQLException if SQL error occurs while reading in data from given ResultSet
     */
    public static List createPrimaryKeyColumnList(final ResultSet rs, boolean odbcflag) throws SQLException {
        if (rs == null) {
            final ResourceBundle cMessages = NbBundle.getBundle(KeyColumn.class);// NO i18n
            throw new IllegalArgumentException(cMessages.getString("ERROR_NULL_RS") + "(ERROR_NULL_RS)");// NO
            // i18n
        }

        List pkColumns = Collections.EMPTY_LIST;

        if (rs != null && rs.next()) {
            pkColumns = new ArrayList();

            do {
                pkColumns.add(new KeyColumn(rs, odbcflag));
            } while (rs.next());
        }

        return pkColumns;
    }
    
    /**
     * Creates an instance of KeyColumn with the given values.
     * 
     * @param name name of key
     * @param column name of column
     * @param colSequence sequence of this column within (composite) primary key
     */
    public KeyColumn(final String name, final String column, final int colSequence) {
        this.keyName = name;
        this.columnName = column;
        this.sequenceNum = colSequence;
    }

    /** Creates a new instance of KeyColumn */
    protected KeyColumn() {
    }

    private KeyColumn(final ResultSet rs, boolean odbcflag) throws SQLException {
		if (rs == null) {
			final ResourceBundle cMessages = NbBundle
					.getBundle(KeyColumn.class);// NO i18n
			throw new IllegalArgumentException(cMessages
					.getString("ERROR_VALID_RS")
					+ "(ERROR_VALID_RS)");// NO
			// i18n
		}
		// Order of the result set for odbc driver
		// {6=PK_NAME, 5=KEY_SEQ, 4=COLUMN_NAME, 3=TABLE_NAME, 2=TABLE_SCHEM,
		// 1=TABLE_CAT}
		if (odbcflag) {
			String tablecat = rs.getString(1);
			String tablesch = rs.getString(2);
			String tablename = rs.getString(3);
			this.columnName = rs.getString(4);
			this.sequenceNum = rs.getShort(5);
			this.keyName = rs.getString(6);
		} else {
			this.keyName = rs.getString(KeyColumn.RS_KEY_NAME);
			this.columnName = rs.getString(KeyColumn.RS_COLUMN_NAME);
			this.sequenceNum = rs.getShort(KeyColumn.RS_SEQUENCE_NUM);
		}
	}

    /**
	 * Gets name of column name associate with this primary key.
	 * 
	 * @return name of column
	 */
    public String getColumnName() {
        return this.columnName;
    }

    /**
     * Gets name of primary key with which this column is associated.
     * 
     * @return name of associated PK
     */
    public String getName() {
        return this.keyName;
    }

    /**
     * Gets sequence of this column within the (composite) primary key.
     * 
     * @return column sequence
     */
    public int getColumnSequence() {
        return this.sequenceNum;
    }
}
