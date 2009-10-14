/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.sql.framework.codegen.oracle9;

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
 * @version $Revision$
 */
public class Oracle9DateAddOperatorGenerator extends AbstractGenerator {

    public String generate(SQLObject obj, StatementContext context) throws BaseException {
        String result;
        SQLGenericOperator operator = (SQLGenericOperator) obj;
        SQLLiteral literal = (SQLLiteral) operator.getArgumentValue("type");
        if (literal == null) {
            throw new BaseException("Failed to evaluate " + operator.getOperatorType() + ", \"type\" is null.");
        }

        String intervalType = literal.getValue();
        intervalType = intervalType.toLowerCase();

        // DATEADD: $date + numtodsinterval($interval, $interval_type)
        // DATEDIFF: extract($interval from $date1 - $date2)

        // Get all necessary data.
        Map params = operator.getSQLObjectMap();

        // Allow overridable operator factory so other db's can override date add
        SQLOperatorDefinition defn = this.getDB().getOperatorFactory().getSQLOperatorDefinition(operator.getOperatorType());

        Map resolvedparams = new HashMap();
        List args = defn.getArgList();

        for (int i = 0; i < args.size(); i++) {
            String key = ((SQLOperatorArg) args.get(i)).getArgName();
            SQLObject val = (SQLObject) params.get(key);
            if (val != null) {
                String eval = this.getGeneratorFactory().generate(val, context);

                if (intervalType.equalsIgnoreCase("week")) {
                    // use week as 7 days
                    eval += "*7";
                }
                if (intervalType.equalsIgnoreCase("quarter")) {
                    // use quarter as 3 months
                    eval += "*3";
                }

                resolvedparams.put(key, eval);
            }
        }

        resolvedparams.put("type", translateIntervalTypeToLiteral(intervalType));

        result = StringUtil.replace(defn.getScript(), resolvedparams, SQLConstants.OPERATOR_VARIABLE_PREFIX);

        // replace any other strings as needed
        if (intervalType.equalsIgnoreCase("year") || (intervalType.equalsIgnoreCase("month")) || (intervalType.equalsIgnoreCase("quarter"))) {
            // replace all day-second intervals with year-month intervals
            result = result.replaceFirst("NUMTODSINTERVAL", "NUMTOYMINTERVAL");
            result = result.replaceFirst("DAY TO SECOND", "YEAR TO MONTH");
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
        String literal = intervalType.replaceFirst("week", "day");
        literal = literal.replaceFirst("quarter", "month");
        return literal;
    }

}
