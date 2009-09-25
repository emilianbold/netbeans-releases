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

import org.apache.velocity.VelocityContext;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.codegen.TemplateBuilder;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SourceTable;
import com.sun.sql.framework.utils.StringUtil;

/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class BaseSourceTableGenerator extends BaseTableGenerator {

    public String generate(SQLObject obj, StatementContext context) {
        SourceTable table = (SourceTable) obj;

        String tableName = null;

        if (context.isUsingTempTableName(table)) {
            tableName = this.getDB().getEscapedName(table.getTemporaryTableName());
        } else {
            String prefix = "";
            if (table.isUsingFullyQualifiedName() && context.isUsingFullyQualifiedTablePrefix()) {
                prefix = getFullyQualifiedTablePrefix(table);
            }

            //Ensure order of precedence for table name is followed.
            tableName = table.getUniqueTableName();
            if (StringUtil.isNullString(tableName) || !(context.isUsingUniqueTableName() || context.isUsingUniqueTableName(table))) {
                tableName = table.getUserDefinedTableName();
                if (StringUtil.isNullString(tableName) || context.isUsingOriginalSourceTableName()) {
                    tableName = table.getName();
                }
            }

            StringBuilder result = new StringBuilder(50);
            result.append(prefix);
            result.append(this.getDB().getEscapedName(table.getTablePrefix() + tableName));

            tableName = result.toString();
        }

        String aliasName = "";
        if (context.isUseSourceTableAliasName()) {
            aliasName = table.getAliasName();
        }

        VelocityContext vContext = new VelocityContext();
        vContext.put("tableName", tableName.trim());
        vContext.put("aliasName", aliasName.trim());

        return TemplateBuilder.generateSql(this.getDB().getTemplateFileName("sourceTable"), vContext);
    }
}
