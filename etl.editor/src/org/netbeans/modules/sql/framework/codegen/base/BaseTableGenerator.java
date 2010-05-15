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
package org.netbeans.modules.sql.framework.codegen.base;

import org.netbeans.modules.sql.framework.codegen.AbstractGenerator;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import com.sun.etl.utils.StringUtil;

/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public abstract class BaseTableGenerator extends AbstractGenerator {

    /**
     * Gets fully-qualified table name prefix for the given SQLDBTable instance.
     *
     * @param table SQLDBTable for which to generate a fully-qualified table name prefix
     * @return fully-qualified table name prefix
     */
    String getFullyQualifiedTablePrefix(SQLDBTable table) {
        StringBuilder strBuf = new StringBuilder(25);

        if (table.isUsingFullyQualifiedName()) {
            // Ensure order of precedence for catalog name is followed.
            String catalogName = table.getUserDefinedCatalogName();
            if (StringUtil.isNullString(catalogName)) {
                catalogName = table.getCatalog();
            }

            if (!StringUtil.isNullString(catalogName)) {
                strBuf.append(this.getDB().getEscapedCatalogName(catalogName)).append(".");
            }

            // Ensure order of precedence for schema name is followed.
            String schemaName = table.getUserDefinedSchemaName();
            if (StringUtil.isNullString(schemaName)) {
                schemaName = table.getSchema();
            }
            if (!StringUtil.isNullString(schemaName)) {
                strBuf.append(this.getDB().getEscapedSchemaName(schemaName)).append(".");
            }
        }

        return strBuf.toString().trim();
    }
}