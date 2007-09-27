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
     * @param connDefsOverrideAvailable i.e Run Time, where OTD Connection definition's
     *        needs to be overridden.
     * @return
     * @throws BaseException
     */
    private static boolean usePipelineFlowGenerator(SQLDefinition sqlDefintion, boolean connDefsOverrideAvailable) throws BaseException {
        boolean ret = false;
        if ((sqlDefintion.requiresPipelineProcess())
            || (sqlDefintion.getExecutionStrategyCode().intValue() == SQLDefinition.EXECUTION_STRATEGY_PIPELINE)) {
            ret = true;
        } else if (((sqlDefintion.getExecutionStrategyCode().intValue() == SQLDefinition.EXECUTION_STRATEGY_BEST_FIT)
                    && (PatternFinder.isSourceAndTargetAreInternalButDifferentOtd(sqlDefintion)))) {
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

            if (PatternFinder.isSourceAndTargetAreInternalButDifferentOtd(sqlDef)) {
                String desc = NbBundle.getMessage(SQLValidationVisitor.class, "MSG_Staging_mode_not_allowed");
                throw new BaseException(desc);
            }
        }
    }


    private ETLProcessFlowGeneratorFactory() {
    }
}
