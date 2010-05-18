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
package org.netbeans.modules.sql.framework.codegen;

import java.util.Map;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;
import com.sun.etl.exception.BaseException;
import com.sun.etl.jdbc.SQLPart;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;

/**
 * Statments provide a single access point to all the suported statements in various DBs.
 *
 * @author ritesh Adval
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public interface Statements {

    public SQLPart getOnePassSelectStatement(TargetTable targetTable, StatementContext context) throws BaseException;

    public SQLPart getSelectStatement(SourceTable sourceTable, StatementContext context) throws BaseException;

    public SQLPart getSelectStatement(TargetTable targetTable, StatementContext context) throws BaseException;

    public SQLPart getSelectStatement(SQLJoinView joinView, StatementContext context) throws BaseException;

    public SQLPart getSelectStatement(SQLJoinOperator joinOp, StatementContext context) throws BaseException;

    public SQLPart getPreparedInsertStatement(SQLDBTable table, StatementContext context) throws BaseException;

    public SQLPart getStaticInsertStatement(TargetTable targetTable, StatementContext context) throws BaseException;

    public SQLPart getUpdateStatement(TargetTable targetTable, StatementContext context) throws BaseException;

    public SQLPart getMergeStatement(TargetTable TargetTable, StatementContext context) throws BaseException;

    public SQLPart getCreateStatement(SQLDBTable table, StatementContext context) throws BaseException;

    public SQLPart getDeleteStatement(SQLDBTable table, StatementContext context) throws BaseException;

    public SQLPart getTruncateStatement(SQLDBTable targetTable, StatementContext context) throws BaseException;
        
    public SQLPart getDropStatement(SQLDBTable table, StatementContext context) throws BaseException;

    public SQLPart getInsertSelectStatement(TargetTable targetTable, StatementContext context) throws BaseException;

    public SQLPart getRowCountStatement(SQLDBTable table, StatementContext context) throws BaseException;

    public SQLPart getTableExistsStatement(SQLDBTable table, StatementContext context) throws BaseException;

    public SQLPart getDefragStatement(SQLDBTable table, StatementContext context) throws BaseException;

    public SQLPart getInitializationStatements(StatementContext context) throws BaseException;

    public SQLPart getCreateLogSummaryTableStatement(boolean useMemoryTable) throws BaseException;

    public SQLPart getDeleteInvalidRowFromSummaryTableStatement(TargetTable table) throws BaseException;

    public SQLPart getInsertStartDateIntoSummaryTableStatement(TargetTable table, StatementContext context) throws BaseException;

    public SQLPart getSelectExecutionIdFromSummaryTableStatement(TargetTable table, StatementContext context) throws BaseException;

    public String getSummaryTableName();

    public SQLPart getUpdateEndDateInSummaryTableStatement(TargetTable table, StatementContext context) throws BaseException;

    public SQLPart normalizeSQLForExecution(SQLPart rawSQLPart);

    // TODO Voilates Statements interface pattern, need to redesign the interfaces.
    public Map getCorrelatedUpdateStatement(TargetTable targetTable, StatementContext context) throws BaseException;
}