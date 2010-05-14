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
package org.netbeans.modules.edm.codegen;

import java.sql.Types;
import org.apache.velocity.VelocityContext;
import org.netbeans.modules.edm.model.SQLLiteral;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.editor.utils.StringUtil;

/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 */
public class BaseLiteralGenerator extends AbstractGenerator {

    // TODO: this logic of varchar_unquoted type is mixed with jdbc type
    // in literal. we should have special liteal to handle this.
    public String generate(SQLObject obj, StatementContext context) {
        SQLLiteral literal = (SQLLiteral) obj;

        String result;
        switch (literal.getJdbcType()) {
            case Types.CHAR:
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
            case Types.VARCHAR:
                result = "'" + StringUtil.replaceInString(literal.getValue(), "'", "''") + "'";
                break;
            case SQLLiteral.VARCHAR_UNQUOTED:
                result = literal.getValue();
                break;
            default:
                result = literal.getValue();
                break;
        }

        VelocityContext vContext = new VelocityContext();
        vContext.put("literal", result);

        return TemplateBuilder.generateSql(this.getDB().getTemplateFileName("literal"), vContext);
    }
}