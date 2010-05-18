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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import org.netbeans.modules.sql.framework.codegen.OperatorGenerator;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLOperatorDefinition;
import com.sun.etl.exception.BaseException;

/**
 * Handles evaluation of User function AKA generic function of format userFx(arg1, arg2...).
 *
 * @author Girish Patil
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class BaseUserFxGenerator extends OperatorGenerator {

    /**
     * genVariableExpression
     *
     * @param params - SequencedHashMap
     * @param defn - SQLOperatorDefinition
     * @return String
     * @throws BaseException - exception
     */
    @Override
    protected String genVariableExpression(LinkedHashMap params, SQLOperatorDefinition defn, StatementContext context) throws BaseException {
        TreeMap<String, String> resolvedparams = new TreeMap<String, String>();

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

        String functionName = (String) context.getClientProperty(StatementContext.USER_FUNCTION_NAME);
        if ((functionName == null) || ("".equals(functionName))) {
            functionName = "userFx";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(functionName);
        sb.append("(");
        Iterator i = (resolvedparams.keySet()).iterator();
        int count = 0;
        while (i.hasNext()) {
            sb.append((count > 0) ? "," : "");
            sb.append(resolvedparams.get(i.next()));
            count++;
        }
        sb.append(")");
        return sb.toString();
    }
}