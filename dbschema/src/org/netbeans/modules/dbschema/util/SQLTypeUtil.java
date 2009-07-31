/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.dbschema.util;

import java.util.ResourceBundle;
import java.sql.Types;
import java.util.logging.Level;
import java.util.logging.Logger;

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
                        case TypesJDBC4.NCHAR:
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
                        case TypesJDBC4.LONGNVARCHAR:
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
                        case TypesJDBC4.NVARCHAR:
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
                        case TypesJDBC4.NCLOB:
			case Types.CLOB:
				return getString("SQL_CLOB"); //NOI18N
			case Types.REF:
				return getString("SQL_REF"); //NOI18N
			default:
                                Logger.getLogger(SQLTypeUtil.class.getName()).log(Level.WARNING, "Unknown JDBC column type: " + sqlType + ". Returns null.");
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
                        case TypesJDBC4.NCHAR:
                        case TypesJDBC4.NVARCHAR:
                        case TypesJDBC4.LONGNVARCHAR:
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
               case TypesJDBC4.NCLOB:
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
