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
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.sql.project.dbmodel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//Internationalization
import java.util.Locale;
import java.text.MessageFormat;
import java.util.ResourceBundle;


/**
 *
 * @author Jonathan Giron
 * @version 
 */
public class KeyColumn {
    /**
     * DatabaseMetaData ResultSet column name used to decode name of associated 
     * primary key
     */
    protected static final String
        RS_KEY_NAME = "PK_NAME"; // NOI18N
    
    private static final String
        RS_COLUMN_NAME = "COLUMN_NAME"; // NOI18N
    
    /**
     * DatabaseMetaData ResultSet column name used to decode key sequence number 
     */
    protected static final String
        RS_SEQUENCE_NUM = "KEY_SEQ"; // NOI18N
    
    /** Name of column */
    protected String columnName;
    
    /** Name of key associated with this column */
    protected String keyName;
    
    /** For composite keys, sequence of this column for the associated key */
    protected int sequenceNum;


    /**
     * Creates a List of (primary) KeyColumn instances from the given ResultSet.
     *
     * @param rs ResultSet containing primary key metadata as obtained from 
     * DatabaseMetaData
     * @return List of KeyColumn instances based from metadata in rs
     *
     * @throws SQLException if SQL error occurs while reading in data from
     * given ResultSet
     */    
    public static List createPrimaryKeyColumnList(ResultSet rs) 
            throws SQLException {
        if (rs == null) {
            Locale locale = Locale.getDefault();
            ResourceBundle cMessages = ResourceBundle.getBundle("com/stc/oracle/builder/Bundle", locale); // NO i18n            
            throw new IllegalArgumentException(cMessages.getString("ERROR_NULL_RS")+"(ERROR_NULL_RS)");// NO i18n
        }
        
        List pkColumns = Collections.EMPTY_LIST;
        
        if (rs != null && rs.next()) {
            pkColumns = new ArrayList();
            
            do {
                pkColumns.add(new KeyColumn(rs));
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
    public KeyColumn(String name, String column, int colSequence) {
        keyName = name;
        columnName = column;
        sequenceNum = colSequence;
    }

    /** Creates a new instance of KeyColumn */
    protected KeyColumn() {
    }
    
    private KeyColumn(ResultSet rs) throws SQLException {
        if (rs == null) {
            Locale locale = Locale.getDefault();
            ResourceBundle cMessages = ResourceBundle.getBundle("com/stc/oracle/builder/Bundle", locale); // NO i18n            
            throw new IllegalArgumentException(
                cMessages.getString("ERROR_VALID_RS")+"(ERROR_VALID_RS)");// NO i18n
        }
        
        keyName = rs.getString(RS_KEY_NAME);
        columnName = rs.getString(RS_COLUMN_NAME);
        sequenceNum = rs.getShort(RS_SEQUENCE_NUM);
    }        


    /**
     * Gets name of column name associate with this primary key.
     *
     * @return name of column
     */
    public String getColumnName() {
        return columnName;
    }
    
    
    /**
     * Gets name of primary key with which this column is associated.
     *
     * @return name of associated PK
     */
    public String getName() {
        return keyName;
    }
    
    /**
     * Gets sequence of this column within the (composite) primary key.
     *
     * @return column sequence
     */
    public int getColumnSequence() {
        return sequenceNum;
    }
}
