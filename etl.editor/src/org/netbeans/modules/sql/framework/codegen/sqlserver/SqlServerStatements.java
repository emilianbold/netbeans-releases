/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.sql.framework.codegen.sqlserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.netbeans.modules.sql.framework.codegen.AbstractDB;
import org.netbeans.modules.sql.framework.codegen.ResolvedMapping;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.codegen.base.BaseStatements;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.TargetTable;

import com.sun.etl.exception.BaseException;
import com.sun.etl.jdbc.SQLPart;
import net.java.hulp.i18n.Logger;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class SqlServerStatements extends BaseStatements {
    //Loggers
    private static transient final Logger mLogger = Logger.getLogger(SqlServerStatements.class.getName());

    public SqlServerStatements(AbstractDB database) {
        super(database);
    }

    @Override
    protected void populateContextForUpdate(TargetTable targetTable, StatementContext context, VelocityContext vContext) throws BaseException {
        StatementContext localContext = new StatementContext();
        if (context != null) {
            localContext.putAll(context);
        }

        vContext.put("targetTable", this.genFactory.generate(targetTable, localContext));

        //SELECT START
        localContext.putClientProperty(StatementContext.USE_SOURCE_TABLE_ALIAS_NAME, Boolean.TRUE);

        final boolean useKeyColumns = false;
        List rMappings = createResolvedMappings(targetTable, useKeyColumns, localContext);
        ArrayList selectIdentifiers = new ArrayList();

        Iterator it = rMappings.iterator();
        while (it.hasNext()) {
            ResolvedMapping rm = (ResolvedMapping) it.next();
            selectIdentifiers.add(rm.getSource());
            selectIdentifiers.add(rm.getTarget());
        }

        localContext.putClientProperty(StatementContext.USE_TARGET_TABLE_ALIAS_NAME, Boolean.TRUE);
        vContext.put("fromContent", getFromStatementContentForTarget(targetTable, SQLConstants.INNER_JOIN, localContext));

        vContext.put("useUpdateWhere", Boolean.FALSE);

        String condition = getWhereCondition(targetTable, localContext);
        if (condition != null && !condition.equals("")) {
            vContext.put("useUpdateWhere", Boolean.TRUE);
            vContext.put("condition", condition);
        }
        //SELECT END

        vContext.put("mappings", rMappings);
        
        vContext.put("statementSeparator", Character.toString(SQLPart.STATEMENT_SEPARATOR));
        vContext.put("isDisableConstraint", targetTable.isDisableConstraints());        
    }
}
