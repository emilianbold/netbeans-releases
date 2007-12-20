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

package org.netbeans.modules.sql.framework.codegen.db2v7;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.sql.framework.codegen.AbstractGenerator;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.codegen.TypeGenerator;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLGenericOperator;
import org.netbeans.modules.sql.framework.model.SQLLiteral;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLOperatorArg;
import org.netbeans.modules.sql.framework.model.SQLOperatorDefinition;
import org.netbeans.modules.sql.framework.model.SourceColumn;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.jdbc.SQLUtils;
import com.sun.sql.framework.utils.StringUtil;

/**
 * @author Rupesh Ramachandran
 * @author Ritesh Adval
 * @version $Revision$
 */
public class DB2V7CastAsOperatorGenerator extends AbstractGenerator {

    public String generate(SQLObject obj, StatementContext context) throws BaseException {
        String result;
        SQLGenericOperator operator = (SQLGenericOperator) obj;

        SQLLiteral literal = (SQLLiteral) operator.getArgumentValue("type");
        if (literal == null) {
            throw new BaseException("Failed to evaluate " + operator.getOperatorType() + ", \"type\" is null.");
        }

        String castType = literal.getValue();
        castType = castType.toLowerCase();

        // get all necessary data.
        Map params = operator.getSQLObjectMap();

        // allow overridable operator factory so other db's can override cast
        SQLOperatorDefinition defn = this.getDB().getOperatorFactory().getSQLOperatorDefinition(operator.getOperatorType());

        Map<String, String> resolvedparams = new HashMap<String, String>();
        List args = defn.getArgList();

        for (int i = 0; i < args.size(); i++) {
            String key = ((SQLOperatorArg) args.get(i)).getArgName();
            SQLObject val = (SQLObject) params.get(key);

            if (val != null) {
                // if casting from date/time to timestamp, use TIMESTAMP_ISO()
                // fn. instead
                if (val instanceof SourceColumn) {
                    if (((val.getJdbcType() == SQLUtils.getStdJdbcType("date")) || (val.getJdbcType() == SQLUtils.getStdJdbcType("time"))) && (castType.equals("timestamp"))) {
                        result = "TIMESTAMP_ISO(" + this.getGeneratorFactory().generate(val, context) + ")";
                        return result;
                    }
                }
                resolvedparams.put(key, this.getGeneratorFactory().generate(val, context));
            }
        }

        TypeGenerator typeGenerator = this.getDB().getTypeGenerator();
        int jdbcTypeInt = SQLUtils.getStdJdbcType(castType);
        resolvedparams.put("type", typeGenerator.generate(jdbcTypeInt, 255, 0));

        result = StringUtil.replace(defn.getScript(), resolvedparams, SQLConstants.OPERATOR_VARIABLE_PREFIX);
        return result;
    }
}