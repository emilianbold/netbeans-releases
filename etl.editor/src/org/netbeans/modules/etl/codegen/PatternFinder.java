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
package org.netbeans.modules.etl.codegen;

import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.etl.codegen.impl.OnePassETLStrategyBuilderImpl;
import org.netbeans.modules.etl.codegen.impl.PipelinedStrategyBuilderImpl;
import org.netbeans.modules.etl.codegen.impl.SimpleETLStrategyBuilderImpl;
import org.netbeans.modules.etl.codegen.impl.StagingStrategyBuilder;
import org.netbeans.modules.etl.codegen.impl.ValidatingStrategyBuilderImpl;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.TargetTable;
import com.sun.etl.exception.BaseException;
import com.sun.etl.utils.StringUtil;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.model.DBTable;

/**
 * Factory for ETLStrategyBuilder. Based a matching pattern I will create an appropriate
 * ETLStrategyBuilder. This allow us to optimize the ETL process based on a given
 * situation.
 *
 * @author Ahimanikya Satapathy
 * @author Jonathan Giron
 * @author Girish Patil
 * @version $Revision$
 */
public class PatternFinder {

    private static transient final Logger mLogger = Logger.getLogger(PatternFinder.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    public static boolean allDBTablesAreInternal(Iterator tableIterator) throws BaseException {
        while (tableIterator.hasNext()) {
            DBTable srcTable = (DBTable) tableIterator.next();
            // as soon as you find a non-axion db , return false.
            if (!isInternalDBTable(srcTable)) {
                return false;
            }
        }
        return true;
    }

    public static ETLStrategyBuilder createETLStrategyBuilder(TargetTable tt, ETLScriptBuilderModel model) throws BaseException {
        // Optimization: Depending on the pattern decide which strategy to use
        List sourceTables = tt.getSourceTableList();
        ETLStrategyBuilder builder = null;
        SQLDefinition definition = model.getSqlDefinition();
        int strategyOverride = SQLDefinition.EXECUTION_STRATEGY_BEST_FIT;
        strategyOverride = definition.getExecutionStrategyCode().intValue();

        if (strategyOverride == SQLDefinition.EXECUTION_STRATEGY_PIPELINE) {
            // Force pipeline execution
            builder = (definition.hasValidationConditions()) ? new ValidatingStrategyBuilderImpl(model) : new PipelinedStrategyBuilderImpl(model);
        } else if (strategyOverride == SQLDefinition.EXECUTION_STRATEGY_STAGING) {
            String nbBundle1 = mLoc.t("BUND001: Cannot execute in Staging mode, choose Best-fit or Pipeline.");
            if (definition.requiresPipelineProcess()) {
                String desc = nbBundle1.substring(15);
                throw new BaseException(desc);
            }

            if (PatternFinder.isSourceAndTargetAreInternalButDifferent(definition)) {
                String desc = nbBundle1.substring(15);
                throw new BaseException(desc);
            }

            if (isInternalDBTable(tt) && allDBTablesAreInternal(sourceTables.iterator())) {
                String desc = nbBundle1.substring(15);
                throw new BaseException(desc);
            }

            // Extract and execute at target
            StagingStrategyBuilder stgBuilder = new StagingStrategyBuilder(model);
            stgBuilder.setForceStaging(true);
            builder = stgBuilder;
        } else {
            // strategyOverride == SQLDefinition.EXECUTION_STRATEGY_BEST_FIT
            if (definition.requiresPipelineProcess()) {
                builder = (definition.hasValidationConditions()) ? new ValidatingStrategyBuilderImpl(model) : new PipelinedStrategyBuilderImpl(model);
            } else if (isInternalDBTable(tt) && allDBTablesAreInternal(sourceTables.iterator())) {
                builder = new SimpleETLStrategyBuilderImpl(model);
            } else if (isSourceTargetFromSameDB(sourceTables, tt, model)) {
                // If Source(s) and Target are both from the same DB
                builder = new SimpleETLStrategyBuilderImpl(model);
            } else if (isSameDBTables(sourceTables.iterator(), model) && tt.getStatementType() == SQLConstants.INSERT_STATEMENT && (!tt.getJoinCondition().isConditionDefined()) && (!tt.getFilterCondition().isConditionDefined())) {
                // If all source table are from same DB and statement type is Insert
                //builder = new OnePassETLStrategyBuilderImpl();
                builder = new OnePassETLStrategyBuilderImpl(model);
            } else {
                // Default strategy
                builder = new StagingStrategyBuilder(model);
            }
        }

        return builder;
    }

    /**
     * @param sqlDefinition
     * @return
     */
    public static boolean hasAllInternalDBTables(SQLDefinition sqlDefinition) throws BaseException {
        List targetTables = sqlDefinition.getTargetTables();
        Iterator iter = targetTables.iterator();
        while (iter.hasNext()) {
            TargetTable tt = (TargetTable) iter.next();
            if (!isInternalDBTable(tt)) {
                return false;
            }

            Iterator srcIter = tt.getSourceTableList().iterator();
            if (!allDBTablesAreInternal(srcIter)) {
                return false;
            }
        }

        return true;
    }

    public static boolean isFromSameDB(DBTable table1, DBTable table2, ETLScriptBuilderModel model) throws BaseException {
        DBConnectionDefinition conDef1 = model.getConnectionDefinition(table1);
        DBConnectionDefinition conDef2 = model.getConnectionDefinition(table2);
        return isIdenticalDBConDef(conDef1, conDef2);
    }

    public static boolean isInternalDBConnection(DBConnectionDefinition dbConnDef) {
        boolean ret = false;
        if (dbConnDef != null) {
            if (dbConnDef.getDBType().equalsIgnoreCase("AXION") || dbConnDef.getDBType().equalsIgnoreCase("Internal")) {
                ret = true;
            }
        }
        return ret;
    }

    public static boolean isInternalDBTable(DBTable table) throws BaseException {
        DBConnectionDefinition newConDef = table.getParent().getConnectionDefinition();
        if (newConDef == null) {
            throw new BaseException("DBConnectionDefinition is null for Table: " + table.getName());
        }
        return isInternalDBConnection(newConDef);
    }

    /**
     * @param sqlDefinition
     * @return true only if Target is internal and All its source are also internal but
     *         belong to database other than Target Table's.
     * @throws BaseException
     */
    public static boolean isSourceAndTargetAreInternalButDifferent(SQLDefinition sqlDefinition) throws BaseException {
        boolean ret = false;
        boolean internalTargetTableFound = false;
        boolean allSourcesInternal = false;
        boolean dbDifferent = false;

        List targetTables = sqlDefinition.getTargetTables();
        Iterator ttIter = targetTables.iterator();
        Iterator srcIter = null;
        DBConnectionDefinition tgtConnDef = null;
        DBConnectionDefinition srcConnDef = null;

        while (ttIter.hasNext()) {
            TargetTable tt = (TargetTable) ttIter.next();
            tgtConnDef = tt.getParent().getConnectionDefinition();

            internalTargetTableFound = false;
            allSourcesInternal = false;

            if (isInternalDBConnection(tgtConnDef)) {
                internalTargetTableFound = true;
                allSourcesInternal = true;
                dbDifferent = false;

                srcIter = tt.getSourceTableList().iterator();
                while (srcIter.hasNext()) {
                    DBTable srcTable = (DBTable) srcIter.next();
                    srcConnDef = srcTable.getParent().getConnectionDefinition();
                    if (isInternalDBConnection(srcConnDef)) {
                        if (!isIdenticalDBConDef(srcConnDef, tgtConnDef)) {
                            dbDifferent = true;
                        }
                    } else {
                        allSourcesInternal = false;
                        break;
                    }
                }

                if (internalTargetTableFound && allSourcesInternal && dbDifferent) {
                    ret = true;
                    break;
                }
            }
        }

        return ret;
    }

    // This will ignore the optional attribute "AlternateId" for datadirect db2 url
    private static String ignoreOptional(String url) {
        int start = url.toUpperCase().indexOf("ALTERNATEID");
        if (start != -1) {
            int end = url.indexOf(';', start);
            StringBuilder buf = new StringBuilder(60);
            buf.append(url.substring(0, start));
            if (end != -1) {
                buf.append(url.substring(end + 1));
            }
            return buf.toString();
        } else {
            return url;
        }
    }

    private static boolean isIdenticalDBConDef(DBConnectionDefinition c1, DBConnectionDefinition c2) {
        boolean identical = false;

        if (c1 != null && c2 != null) {
            identical = StringUtil.isIdenticalIgnoreCase(ignoreOptional(c1.getConnectionURL()), ignoreOptional(c2.getConnectionURL())) && StringUtil.isIdentical(c1.getUserName(), c2.getUserName()) && StringUtil.isIdentical(c1.getPassword(), c2.getPassword());
        }
        return identical;
    }

    private static boolean isSameDBTables(Iterator tableIterator, ETLScriptBuilderModel model) throws BaseException {
        DBConnectionDefinition conDef = null;
        while (tableIterator.hasNext()) {
            DBTable srcTable = (DBTable) tableIterator.next();
            DBConnectionDefinition newConDef = model.getConnectionDefinition(srcTable);

            if (newConDef == null) {
                throw new BaseException("DBConnectionDefinition is null for Source Table: " + srcTable.getName());
            }

            if (conDef == null) {
                conDef = newConDef;
            } else if (!isIdenticalDBConDef(conDef, newConDef)) {
                // as soon as you find a diff db , return false.
                return false;
            }
        }
        return true;
    }

    private static boolean isSourceTargetFromSameDB(List srcTableList, DBTable trgtTable, ETLScriptBuilderModel model) throws BaseException {
        // Make sure all tables in list are themselves from the same DB.
        if (!isSameDBTables(srcTableList.iterator(), model)) {
            return false;
        }

        if (srcTableList == null || srcTableList.isEmpty()) {
            // There is no source table, so we don't need to build extractor:
            // use the default strategy
            return true;
        }

        // Get the first source table and compare its DB against that of the target table
        DBTable srcTable = (DBTable) srcTableList.get(0);
        return isFromSameDB(srcTable, trgtTable, model);
    }
}
