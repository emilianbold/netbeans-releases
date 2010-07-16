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

package org.netbeans.modules.sql.framework.codegen.oracle8;

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

import com.sun.etl.exception.BaseException;
import com.sun.etl.utils.StringUtil;

/**
 * @author Rupesh Ramachandran
 * @author Ritesh Adval
 * @version $Revision$
 */
public class Oracle8DateDiffOperatorGenerator extends AbstractGenerator {

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

        Map resolvedparams = new HashMap();
        List args = defn.getArgList();

        for (int i = 0; i < args.size(); i++) {
            String key = ((SQLOperatorArg) args.get(i)).getArgName();
            SQLObject val = (SQLObject) params.get(key);
            if (val != null) {
                resolvedparams.put(key, this.getGeneratorFactory().generate(val, context));
            }
        }

        resolvedparams.put("type", translateIntervalTypeToLiteral(intervalType));

        // for day, hour, minute, second, use:
        // DATEDIFF: ($timestamp1 - $timestamp2) * ($type)
        // DATEADD: $timestamp1 + ($interval / ($type))

        // for month,quarter,year:
        // DATEDIFF: MONTHS_BETWEEN($timestamp1, $timestamp2)/$type (type in months)
        // DATEADD: ADD_MONTHS($timestamp1, $interval*$type) (type in months)

        if (intervalType.equals("month") || intervalType.equals("quarter") || intervalType.equals("year")) {
            result = "MONTHS_BETWEEN(" + resolvedparams.get("timestamp1") + ", " + resolvedparams.get("timestamp2") + ") / "
                + resolvedparams.get("type");
        } else {
            // use operator script for day,hour,minute,second
            result = StringUtil.replace(defn.getScript(), resolvedparams, SQLConstants.OPERATOR_VARIABLE_PREFIX);
        }

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
        // convert interval to units within day.
        // e.g. 24 hours in 1 day, return '24' for 'hour'
        if (intervalType.equals("second"))
            literal = "86400";
        else if (intervalType.equals("minute"))
            literal = "1440";
        else if (intervalType.equals("hour"))
            literal = "24";
        else if (intervalType.equals("day"))
            literal = "1";
        else if (intervalType.equals("week"))
            literal = "1/7";
        // convert interval to months. e.g. quarter = 3 months
        else if (intervalType.equals("month"))
            literal = "1";
        else if (intervalType.equals("quarter"))
            literal = "3";
        else if (intervalType.equals("year"))
            literal = "12";
        return literal;
    }

}