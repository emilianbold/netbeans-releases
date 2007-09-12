/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
