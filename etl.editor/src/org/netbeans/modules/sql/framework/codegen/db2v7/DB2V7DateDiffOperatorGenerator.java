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
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLGenericOperator;
import org.netbeans.modules.sql.framework.model.SQLLiteral;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLOperatorArg;
import org.netbeans.modules.sql.framework.model.SQLOperatorDefinition;
import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.StringUtil;

/**
 * @author Rupesh Ramachandran
 * @author Ritesh Adval
 */
public class DB2V7DateDiffOperatorGenerator extends AbstractGenerator {

    public String generate(SQLObject obj, StatementContext context) throws BaseException {
        String result;
        SQLGenericOperator operator = (SQLGenericOperator) obj;
        SQLLiteral literal = (SQLLiteral) operator.getArgumentValue("type");
        if (literal == null) {
            throw new BaseException("Failed to evaluate " + operator.getOperatorType() + ", \"type\" is null.");
        }

        String intervalType = literal.getValue();
        intervalType = intervalType.toLowerCase();

        // get all necessary data.
        Map params = operator.getSQLObjectMap();

        // allow overridable operator factory so other db's can override date add
        SQLOperatorDefinition defn = this.getDB().getOperatorFactory().getSQLOperatorDefinition(operator.getOperatorType());

        Map<String, String> resolvedparams = new HashMap<String, String>();
        List args = defn.getArgList();

        for (int i = 0; i < args.size(); i++) {
            String key = ((SQLOperatorArg) args.get(i)).getArgName();
            SQLObject val = (SQLObject) params.get(key);
            if (val != null) {
                String eval = this.getGeneratorFactory().generate(val, context);
                resolvedparams.put(key, eval);
            }
        }

        // use numeric constants for datediff
        resolvedparams.put("type", translateIntervalTypeToLiteral(intervalType));

        result = StringUtil.replace(defn.getScript(), resolvedparams, SQLConstants.OPERATOR_VARIABLE_PREFIX);

        return result;
    }

    /**
     * converts the string interval type to the appropriate literal representation of the
     * interval in that DB
     *
     * @param intervalType interval type selected from operator drop-down menu
     * @return interval literal string.
     */
    protected String translateIntervalTypeToLiteral(String intervalType) {
        String literal = "<UNKNOWN_INTERVAL_TYPE>";
        if (intervalType.equals("second")) {
            literal = "2";
        } else if (intervalType.equals("minute")) {
            literal = "4";
        } else if (intervalType.equals("hour")) {
            literal = "8";
        } else if (intervalType.equals("day")) {
            literal = "16";
        } else if (intervalType.equals("week")) {
            literal = "32";
        } else if (intervalType.equals("month")) {
            literal = "64";
        } else if (intervalType.equals("quarter")) {
            literal = "128";
        } else if (intervalType.equals("year")) {
            literal = "256";
        }
        return literal;
    }
}