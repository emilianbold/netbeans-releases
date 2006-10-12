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

package org.netbeans.modules.dbschema.util;

import java.util.ResourceBundle;
import java.sql.Types;

public class SQLTypeUtil extends Object {

	// ===================== i18n utilities ===========================

	/** Computes the localized string for the key.
	 * @param key The key of the string.
	 * @return the localized string.
	 */
	public static String getString (String key) {
		return ResourceBundle.getBundle("org.netbeans.modules.dbschema.resources.Bundle").getString(key);
	}

	// ===================== sql type utilities ===========================

	/** Convert sql types to String for display
     * @param sqlType the type number from java.sql.Types
     * @return the type name
	 */
	static public String getSqlTypeString (int sqlType) {
		switch(sqlType) {
			case Types.BIGINT:
				return getString("SQL_BIGINT"); //NOI18N
			case Types.BINARY:
				return getString("SQL_BINARY"); //NOI18N
			case Types.BIT:
				return getString("SQL_BIT"); //NOI18N
			case Types.CHAR:
				return getString("SQL_CHAR"); //NOI18N
			case Types.DATE:
				return getString("SQL_DATE"); //NOI18N
			case Types.DECIMAL:
				return getString("SQL_DECIMAL"); //NOI18N
			case Types.DOUBLE:
				return getString("SQL_DOUBLE"); //NOI18N
			case Types.FLOAT:
				return getString("SQL_FLOAT"); //NOI18N
			case Types.INTEGER:
				return getString("SQL_INTEGER"); //NOI18N
			case Types.LONGVARBINARY:
				return getString("SQL_LONGVARBINARY"); //NOI18N
			case Types.LONGVARCHAR:
				return getString("SQL_LONGVARCHAR"); //NOI18N
			case Types.NULL:
				return getString("SQL_NULL"); //NOI18N
			case Types.NUMERIC:
				return getString("SQL_NUMERIC"); //NOI18N
			case Types.OTHER:
				return getString("SQL_OTHER"); //NOI18N
			case Types.REAL:
				return getString("SQL_REAL"); //NOI18N
			case Types.SMALLINT:
				return getString("SQL_SMALLINT"); //NOI18N
			case Types.TIME:
				return getString("SQL_TIME"); //NOI18N
			case Types.TIMESTAMP:
				return getString("SQL_TIMESTAMP"); //NOI18N
			case Types.TINYINT:
				return getString("SQL_TINYINT"); //NOI18N
			case Types.VARBINARY:
				return getString("SQL_VARBINARY"); //NOI18N
			case Types.VARCHAR:
				return getString("SQL_VARCHAR"); //NOI18N
			case Types.JAVA_OBJECT:
				return getString("SQL_JAVA_OBJECT"); //NOI18N
			case Types.DISTINCT:
				return getString("SQL_DISTINCT"); //NOI18N
			case Types.STRUCT:
				return getString("SQL_STRUCT"); //NOI18N
			case Types.ARRAY:
				return getString("SQL_ARRAY"); //NOI18N
			case Types.BLOB:
				return getString("SQL_BLOB"); //NOI18N
			case Types.CLOB:
				return getString("SQL_CLOB"); //NOI18N
			case Types.REF:
				return getString("SQL_REF"); //NOI18N
			default:
				return getString("SQL_UNKNOWN"); //NOI18N
		}
	}

    /** Returns if the given data type is numeric type or not.
     * @param type the type from java.sql.Types
     * @return true if the given type is numeric type; false otherwise
     */
	static public boolean isNumeric (int type) {
		switch (type) {
			case Types.BIGINT:
			case Types.BIT:
			case Types.DECIMAL:
			case Types.DOUBLE:
			case Types.FLOAT:
			case Types.INTEGER:
			case Types.NUMERIC:
			case Types.REAL:
			case Types.SMALLINT:
			case Types.TINYINT:
				return true;
		}

		return false;
	}

    /** Returns if the given data type is character type or not.
     * @param type the type from java.sql.Types
     * @return true if the given type is character type; false otherwise
     */
	static public boolean isCharacter (int type) {
		switch (type) {
			case Types.BINARY:
			case Types.CHAR:
			case Types.LONGVARCHAR:
			case Types.VARCHAR:
			case Types.VARBINARY:
				return true;
		}

		return false;
	}
        
    /** Return if a given data type is blob type or not.
     * Note: CLOB should really not be in this list, use isLob method for that.
     * @param type the type from java.sql.Types
     * return true if the give type is blob type; false otherwise
     */
       static public boolean isBlob (int type) {
           switch (type) {
               case Types.BLOB:
               case Types.CLOB:
               case Types.BINARY:
               case Types.VARBINARY:
               case Types.LONGVARBINARY:
               case Types.OTHER:
                   return true;
           }

           return false;
       }

    /** Return if a given data type is LOB (large object) type or not.
     * Note: Implementation of this method uses isBlob method but also 
     * duplicates the check of CLOB because CLOB should really not return 
     * true from isBlob.  However, there might be other non-IDE callers of 
     * the isBlob method (like appserver) so the CLOB check is left in both
     * places for now.
     * @param type the type from java.sql.Types
     * return true if the give type is lob type; false otherwise
     */
       static public boolean isLob (int type) {
           return (isBlob(type) || (Types.CLOB == type) || 
                   (Types.LONGVARCHAR == type));
       }

    /** Returns if two data types are compatible or not.
     * @param type1 first type to compare
     * @param type2 second type to compare
     * @return true if the types are compatible; false otherwise
     */
	static public boolean isCompatibleType (int type1, int type2) {
		return ((type1 == type2)
                     || (isCharacter(type1) && isCharacter(type2))
                     || (isNumeric(type1) && isNumeric(type2))
                     || (isBlob(type1) && isBlob(type2))
                );
	}
}
