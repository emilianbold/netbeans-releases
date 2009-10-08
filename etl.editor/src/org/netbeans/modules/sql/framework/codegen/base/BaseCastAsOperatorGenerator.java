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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.codegen.TypeGenerator;
import org.netbeans.modules.sql.framework.model.SQLCastOperator;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLOperatorArg;
import org.netbeans.modules.sql.framework.model.SQLOperatorDefinition;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.StringUtil;

/**
 * Handles evaluation of cast-as SQL operator.
 *
 * @author Ritesh Adval
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class BaseCastAsOperatorGenerator extends BaseOperatorGenerator {

    /**
     * Overrides parent implementation to correctly generate a cast-as operator expression
     * from the given SQLObject, using the given StatementContext
     *
     * @param obj SQLObject (SQLCastOperator) to be evaluated
     * @param context StatementContext to use in generating expression
     * @return cast-as operator expression
     */
    @Override
    public String generate(SQLObject obj, StatementContext context) throws BaseException {
        String result;
        SQLCastOperator operator = (SQLCastOperator) obj;

        // Get all necessary data.
        Map params = operator.getSQLObjectMap();

        // Allow overrideable operator factory so other db's can override date add
        SQLOperatorDefinition defn = this.getDB().getOperatorFactory().getSQLOperatorDefinition(operator.getOperatorType());

        Map<String, String> resolvedparams = new HashMap<String, String>();
        List args = defn.getArgList();

        for (int i = 0; i < args.size(); i++) {
            String key = ((SQLOperatorArg) args.get(i)).getArgName();
            SQLObject val = (SQLObject) params.get(key);

            if (val != null) {
                resolvedparams.put(key, this.getGeneratorFactory().generate(val, context));
            }
        }

        int jdbcTypeInt = operator.getJdbcType();
        int precision = operator.getPrecision();
        int scale = operator.getScale();

        TypeGenerator typeGenerator = this.getDB().getTypeGenerator();
        resolvedparams.put("type", typeGenerator.generate(jdbcTypeInt, precision, scale));

        result = StringUtil.replace(defn.getScript(), resolvedparams, SQLConstants.OPERATOR_VARIABLE_PREFIX);
        return result;
    }
}
