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
public class Oracle9DateDiffOperatorGenerator extends AbstractGenerator {
    private static final String SEC_N_MIN_UNIT_CALC = "( ( EXTRACT(SECOND FROM (TO_TIMESTAMP($timestamp1) -  TO_TIMESTAMP($timestamp2)) DAY TO SECOND) * $secfact) + " +
  " ( EXTRACT(MINUTE FROM (TO_TIMESTAMP($timestamp1) -  TO_TIMESTAMP($timestamp2)) DAY TO SECOND) * $minfact) + " +
  " ( EXTRACT(HOUR FROM (TO_TIMESTAMP($timestamp1) -  TO_TIMESTAMP($timestamp2)) DAY TO SECOND) * $hrfact) + " +
  " ( EXTRACT(DAY FROM (TO_TIMESTAMP($timestamp1) -  TO_TIMESTAMP($timestamp2)) DAY TO SECOND) * $dayfact) ) " ;

    private static final String HR_UNIT_CALC = "( ( EXTRACT(MINUTE FROM (TO_TIMESTAMP($timestamp1) -  TO_TIMESTAMP($timestamp2)) DAY TO SECOND) * $minfact) + " +
    " ( EXTRACT(HOUR FROM (TO_TIMESTAMP($timestamp1) -  TO_TIMESTAMP($timestamp2)) DAY TO SECOND) * $hrfact) + " +
    " ( EXTRACT(DAY FROM (TO_TIMESTAMP($timestamp1) -  TO_TIMESTAMP($timestamp2)) DAY TO SECOND) * $dayfact) ) " ;
    
    private static final String DAY_UNIT_CALC = "( ( EXTRACT(HOUR FROM (TO_TIMESTAMP($timestamp1) -  TO_TIMESTAMP($timestamp2)) DAY TO SECOND) * $hrfact) + " +
    " ( EXTRACT(DAY FROM (TO_TIMESTAMP($timestamp1) -  TO_TIMESTAMP($timestamp2)) DAY TO SECOND) * $dayfact) ) " ;

    private static final String MONTH_N_YEAR_UNIT_CALC = "( (EXTRACT(MONTH FROM (TO_TIMESTAMP($timestamp1) -  TO_TIMESTAMP($timestamp2)) YEAR TO MONTH) * $monthfact) + " +
    " (EXTRACT(YEAR FROM (TO_TIMESTAMP($timestamp1) -  TO_TIMESTAMP($timestamp2)) YEAR TO MONTH) * $yearfact) )" ;
    
    private static final String[] SEC_2_DAY_UNIT_CALC_KEYS = new String[] { "secfact",
            "minfact", "hrfact", "dayfact" };

    private static final String[] MONTH_N_YEAR_UNIT_CALC_KEYS = new String[] { "monthfact", "yearfact"};
    
    private static final String[] SEC_UNIT_CALC_FACT_VALS = new String[] { "1", "60",
            "3600", // 60 * 60
            "86400" // 24 * 3600
    };

    private static final String[] MIN_UNIT_CALC_FACT_VALS = new String[] { "0.0167",
            "1", "60", "1440" // 24 * 60
    };

    private static final String[] HR_UNIT_CALC_FACT_VALS = new String[] { "0",
            "0.0167", "1", "24" // 24 * 60
    };

    private static final String[] DAY_UNIT_CALC_FACT_VALS = new String[] { "0",
            "0", "0.0415", "1" // 24 * 60
    };

    private static final String[] MONTH_UNIT_CALC_FACT_VALS = new String[] { "1", "12" };
    
    private static final String[] YEAR_UNIT_CALC_FACT_VALS = new String[] { "0.084", "1" };    
    
    public String generate(SQLObject obj, StatementContext context) throws BaseException {
        String result;
        SQLGenericOperator operator = (SQLGenericOperator) obj;
        SQLLiteral literal = (SQLLiteral) operator.getArgumentValue("type");
        if (literal == null) {
            throw new BaseException("Failed to evaluate " + operator.getOperatorType() + ", \"type\" is null.");
        }
        // get all necessary data.
        Map params = operator.getSQLObjectMap();
        String intervalType = literal.getValue();
        intervalType = intervalType.toLowerCase();
        
        // allow overridable operator factory so other db's can override date add
        SQLOperatorDefinition defn = this.getDB().getOperatorFactory().getSQLOperatorDefinition(operator.getOperatorType());
        String script = defn.getScript();
        String[] factorKeys = null;
        String[] factorValues = null;
        
        //DATEADD: $date + numtodsinterval($interval, $interval_type)
        //DATEDIFF: extract($interval from $date1 - $date2) day to second  
        //DATEDIFF: extract($interval from $date1 - $date2) year to month

        if (intervalType.equals("second")){
            script = SEC_N_MIN_UNIT_CALC;
            factorKeys = SEC_2_DAY_UNIT_CALC_KEYS;
            factorValues = SEC_UNIT_CALC_FACT_VALS;
        } else if (intervalType.equals("minute")){
            script = SEC_N_MIN_UNIT_CALC;
            factorKeys = SEC_2_DAY_UNIT_CALC_KEYS;
            factorValues = MIN_UNIT_CALC_FACT_VALS;
        } else if (intervalType.equals("hour")){
            script = HR_UNIT_CALC;
            factorKeys = SEC_2_DAY_UNIT_CALC_KEYS;
            factorValues = HR_UNIT_CALC_FACT_VALS;            
        } else if (intervalType.equals("day") || intervalType.equals("week")){
            script = DAY_UNIT_CALC;
            factorKeys = SEC_2_DAY_UNIT_CALC_KEYS;
            factorValues = DAY_UNIT_CALC_FACT_VALS;                        
        } else if (intervalType.equals("month")|| intervalType.equals("quarter")){
            script = MONTH_N_YEAR_UNIT_CALC;
            factorKeys = MONTH_N_YEAR_UNIT_CALC_KEYS;
            factorValues = MONTH_UNIT_CALC_FACT_VALS;
        } else if (intervalType.equals("year")){
            script = MONTH_N_YEAR_UNIT_CALC;
            factorKeys = MONTH_N_YEAR_UNIT_CALC_KEYS;
            factorValues = YEAR_UNIT_CALC_FACT_VALS;
        }

        Map resolvedparams = new HashMap();
        List args = defn.getArgList();

        for (int i = 0; i < args.size(); i++) {
            String key = ((SQLOperatorArg) args.get(i)).getArgName();
            SQLObject val = (SQLObject) params.get(key);
            if (val != null) {
                String eval = this.getGeneratorFactory().generate(val, context);
                resolvedparams.put(key, eval);
            }
        }

        if (factorKeys != null){
            for (int i=0; i < factorKeys.length; i++){
                resolvedparams.put(factorKeys[i], factorValues[i]);
            }
        }

        result = StringUtil.replace(script, resolvedparams, SQLConstants.OPERATOR_VARIABLE_PREFIX);

        // replace any other strings as needed

        if (intervalType.equalsIgnoreCase("week")) {
            // use week as 7 days
            result = "ROUND(" + result + " / 7, 3)" ;
        } else if (intervalType.equalsIgnoreCase("quarter")) {
            // use quarter as 3 months
            result = "ROUND(" + result + " /3, 3)" ;
        }

        return result;
    }
}