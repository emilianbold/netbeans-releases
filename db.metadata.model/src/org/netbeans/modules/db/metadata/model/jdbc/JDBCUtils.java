/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.metadata.model.jdbc;

import java.sql.Types;
import org.netbeans.modules.db.metadata.model.api.SQLType;

/**
 *
 * @author David
 */
public class JDBCUtils {
    /**
     * Get the SQLType for the given java.sql.Type type.
     *
     * @param type the java.sql.Type type specifier
     * @return SQLType.the SQLType for this java.sql.Type, or null if it's not recognized
     */
    static SQLType getSQLType(int type) {
        switch (type) {
            case Types.BIT: return SQLType.BIT;
            case Types.TINYINT: return SQLType.TINYINT;
            case Types.SMALLINT: return SQLType.SMALLINT;
            case Types.INTEGER: return SQLType.INTEGER;
            case Types.BIGINT: return SQLType.BIGINT;
            case Types.FLOAT: return SQLType.FLOAT;
            case Types.REAL: return SQLType.REAL;
            case Types.DOUBLE: return SQLType.DOUBLE;
            case Types.NUMERIC: return SQLType.NUMERIC;
            case Types.DECIMAL: return SQLType.DECIMAL;
            case Types.CHAR: return SQLType.CHAR;
            case Types.VARCHAR: return SQLType.VARCHAR;
            case Types.LONGVARCHAR: return SQLType.LONGVARCHAR;
            case Types.DATE: return SQLType.DATE;
            case Types.TIME: return SQLType.TIME;
            case Types.TIMESTAMP: return SQLType.TIMESTAMP;
            case Types.BINARY: return SQLType.BINARY;
            case Types.VARBINARY: return SQLType.VARBINARY;
            case Types.LONGVARBINARY: return SQLType.LONGVARBINARY;
            case Types.NULL: return SQLType.NULL;
            case Types.OTHER: return SQLType.OTHER;
            case Types.JAVA_OBJECT: return SQLType.JAVA_OBJECT;
            case Types.DISTINCT: return SQLType.DISTINCT;
            case Types.STRUCT: return SQLType.STRUCT;
            case Types.ARRAY: return SQLType.ARRAY;
            case Types.BLOB: return SQLType.BLOB;
            case Types.CLOB: return SQLType.CLOB;
            case Types.REF: return SQLType.REF;
            case Types.DATALINK: return SQLType.DATALINK;
            case Types.BOOLEAN: return SQLType.BOOLEAN;
            default:
                return null;
        }
    }

}
