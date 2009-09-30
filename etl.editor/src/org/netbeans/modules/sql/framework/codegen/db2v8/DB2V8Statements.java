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
package org.netbeans.modules.sql.framework.codegen.db2v8;

import org.apache.velocity.VelocityContext;
import org.netbeans.modules.sql.framework.codegen.AbstractDB;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.codegen.TemplateBuilder;
import org.netbeans.modules.sql.framework.codegen.db2v7.DB2V7Statements;
import org.netbeans.modules.sql.framework.model.TargetTable;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.jdbc.SQLPart;

/**
 * @author Jonathan Giron
 * @version $Revision$
 */
public class DB2V8Statements extends DB2V7Statements {

    public DB2V8Statements(AbstractDB database) {
        super(database);
    }

    @Override
    public SQLPart getMergeStatement(TargetTable targetTable, StatementContext context) throws BaseException {
        VelocityContext vContext = new VelocityContext();
        populateAnsiMergeStatement(targetTable, context, vContext);
        String result = TemplateBuilder.generateSql(this.db.getTemplateFileName("merge"), vContext); // NOI18N
        return createSQLPart(result, SQLPart.STMT_MERGE); // NOI18N
    }
}
