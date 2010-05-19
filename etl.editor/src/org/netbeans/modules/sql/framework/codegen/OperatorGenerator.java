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

package org.netbeans.modules.sql.framework.codegen;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLOperator;
import org.netbeans.modules.sql.framework.model.SQLOperatorArg;
import org.netbeans.modules.sql.framework.model.SQLOperatorDefinition;
import com.sun.etl.exception.BaseException;
import com.sun.etl.utils.StringUtil;
import java.util.Map;

/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class OperatorGenerator extends AbstractGenerator {

    public String generate(SQLObject obj, StatementContext context) throws BaseException {
        String result;
        SQLOperator operator = (SQLOperator) obj;
        //String defaultCustomFuncId = "userFx";
        // get all necessary data.
        LinkedHashMap params = (LinkedHashMap) operator.getSQLObjectMap();

        //String operatorType  = (operator.isCustomOperator()) ? defaultCustomFuncId : operator.getOperatorType();
        SQLOperatorDefinition defn = this.getDB().getOperatorFactory().getSQLOperatorDefinition(operator.getOperatorType());

        if (defn.getArgCountType() == SQLConstants.OPERATOR_ARGS_VARIABLE) {
            if (operator.isCustomOperator()) {
                context.putClientProperty(StatementContext.USER_FUNCTION_NAME, operator.getCustomOperatorName());
            }
            result = genVariableExpression(params, defn, context);
        } else {
            result = genFixedExpression(params, defn, context);
        }

        boolean appendParenthesis = operator.isShowParenthesis();

        result = (appendParenthesis ? "(" : "") + result + (appendParenthesis ? ")" : "");

        return result;
    }

    /**
     * genFixedExpression
     *
     * @param params - SequencedHashMap
     * @param defn - SQLOperatorDefinition
     * @return String
     * @throws BaseException - exception
     */
    protected String genFixedExpression(LinkedHashMap params, SQLOperatorDefinition defn, StatementContext context) throws BaseException {
        String result;
        List args = defn.getArgList();
        if (params.size() != args.size()) {
            throw new BaseException("Operator " + defn.getOperatorName() + " requires " + args.size() + " number of input arguments.");
        }

        Map<String, String> resolvedparams = new LinkedHashMap<String, String>();
        for (int i = 0; i < args.size(); i++) {
            SQLOperatorArg arg = (SQLOperatorArg) args.get(i);
            String key = arg.getArgName();

            Object obj = params.get(key);
            if (obj instanceof SQLObject) {
                SQLObject val = (SQLObject) obj;
                if (val == null) {
                    throw new BaseException("No input argument for variable " + key);
                }

                resolvedparams.put(key, this.getGeneratorFactory().generate(val, context));
            }
        }

        result = StringUtil.replace(defn.getScript(), resolvedparams, SQLConstants.OPERATOR_VARIABLE_PREFIX);
        return result;
    }

    /**
     * genVariableExpression
     *
     * @param params - SequencedHashMap
     * @param defn - SQLOperatorDefinition
     * @return String
     * @throws BaseException - exception
     */
    protected String genVariableExpression(LinkedHashMap params, SQLOperatorDefinition defn, StatementContext context) throws BaseException {
        String result = "";
        Map<String, String> resolvedparams = new LinkedHashMap<String, String>();

        for (Iterator iter = params.keySet().iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            SQLObject val = (SQLObject) params.get(key);

            if (val == null) {
                throw new BaseException("No input arguments for variable " + key);
            }

            resolvedparams.put(key, this.getGeneratorFactory().generate(val, context));
        }

        String script = defn.getScript();
        if (script.indexOf("[") == -1 || script.indexOf("]") == -1) {
            throw new BaseException("Bad variable operator script.");
        }

        String operator = script.substring(script.indexOf("]") + 1);

        Iterator i = (resolvedparams.values()).iterator();
        int count = 0;
        while (i.hasNext()) {
            result += (count > 0) ? " " + operator + " " : "";
            result += (String) i.next();
            count++;
        }
        return result + "";
    }
}