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

import org.netbeans.modules.etl.codegen.impl.DefaultProcessFlowGenerator;
import org.netbeans.modules.etl.codegen.impl.PipelinedFlowGenerator;
import org.netbeans.modules.etl.codegen.impl.PipelinedStrategyBuilderImpl;
import org.netbeans.modules.etl.codegen.impl.ValidatingStrategyBuilderImpl;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.model.visitors.SQLValidationVisitor;
import org.openide.util.NbBundle;
import com.sun.sql.framework.exception.BaseException;

/**
 * Builds ETL process flow and delegates to appropriate ETLStrategyBuilder as required.
 *
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class ETLProcessFlowGeneratorFactory {

    public static ETLProcessFlowGenerator getCollabFlowGenerator(SQLDefinition sqlDefinition, boolean overridingConnDefs) throws BaseException {
        ETLProcessFlowGenerator generator = null;
        validateExecutionMode(sqlDefinition);

        if (usePipelineFlowGenerator(sqlDefinition, overridingConnDefs)) {
            generator = new PipelinedFlowGenerator(sqlDefinition);
        } else {
            generator = new DefaultProcessFlowGenerator(sqlDefinition);
        }

        return generator;
    }

    public static PipelinedStrategyBuilderImpl getPipelinedTargetTableScriptBuilder(ETLScriptBuilderModel model) throws BaseException {
        return new PipelinedStrategyBuilderImpl(model);
    }

    public static ETLStrategyBuilder getTargetTableScriptBuilder(ETLStrategyBuilderContext context) throws BaseException {
        return PatternFinder.createETLStrategyBuilder(context.getTargetTable(), context.getModel());
    }

    public static ETLStrategyBuilder getTargetTableScriptBuilder(ETLScriptBuilderModel model, TargetTable tt) throws BaseException {
        return PatternFinder.createETLStrategyBuilder(tt, model);
    }

    public static ValidatingStrategyBuilderImpl getValidatingTargetTableScriptBuilder(ETLScriptBuilderModel model) throws BaseException {
        return new ValidatingStrategyBuilderImpl(model);
    }

    /**
     * @param sqlDefintion
     * @param connDefsOverrideAvailable i.e Run Time, where  Connection definition's
     *        needs to be overridden.
     * @return
     * @throws BaseException
     */
    private static boolean usePipelineFlowGenerator(SQLDefinition sqlDefintion, boolean connDefsOverrideAvailable) throws BaseException {
        boolean ret = false;
        if ((sqlDefintion.requiresPipelineProcess()) || (sqlDefintion.getExecutionStrategyCode().intValue() == SQLDefinition.EXECUTION_STRATEGY_PIPELINE)) {
            ret = true;
        } else if ((sqlDefintion.getExecutionStrategyCode().intValue() == SQLDefinition.EXECUTION_STRATEGY_BEST_FIT) && (PatternFinder.isSourceAndTargetAreInternalButDifferent(sqlDefintion))) {
            ret = true;
        }
        return ret;
    }

    public static void validateExecutionMode(SQLDefinition sqlDef) throws BaseException {
        if (sqlDef.getExecutionStrategyCode().intValue() == SQLDefinition.EXECUTION_STRATEGY_STAGING) {

            if (sqlDef.requiresPipelineProcess()) {
                String desc = NbBundle.getMessage(SQLValidationVisitor.class, "MSG_Staging_mode_not_allowed");
                throw new BaseException(desc);
            }

            if (PatternFinder.isSourceAndTargetAreInternalButDifferent(sqlDef)) {
                String desc = NbBundle.getMessage(SQLValidationVisitor.class, "MSG_Staging_mode_not_allowed");
                throw new BaseException(desc);
            }
        }
    }

    private ETLProcessFlowGeneratorFactory() {
    }
}
