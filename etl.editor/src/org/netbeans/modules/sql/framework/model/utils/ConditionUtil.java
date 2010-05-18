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
package org.netbeans.modules.sql.framework.model.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.sql.framework.model.SQLCanvasObject;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConnectableObject;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.parser.conditionparser.SQLConditionParser;

import com.sun.etl.exception.BaseException;

/**
 * @author Ritesh Adval
 */
public class ConditionUtil {

    /**
     * Parses into a SQLObject the given condition string using information from the given
     * SQLDefinition.
     * 
     * @param text condition text to be parsed
     * @param def SQLDefinition
     * @return SQLObject modeling the parsed condition string in <code>text</code>
     * @throws Exception if error occurs during parsing
     */
    public static SQLObject parseCondition(String text, SQLDefinition def) throws Exception {
        SQLConditionParser p = new SQLConditionParser();
        SQLParserUtil helper = new SQLParserUtil(def);
        p.setSQLParserUtil(helper);
        SQLObject obj = null;
        if (text != null && !text.trim().equals("")) {
            obj = p.parse(text);
        }
        return obj;
    }

    /**
     * Populates the given SQLCondition object using information from the given condition
     * string and SQLDefinition.
     * 
     * @param condition SQLCondition object to be populated
     * @param def SQLDefinition
     * @param text condition text to be parsed
     * @throws Exception if error occurs during parsing or object population
     */
    public static void populateCondition(SQLCondition condition, SQLDefinition def, String text) throws Exception {
        SQLObject rootObj = parseCondition(text, def);
        if (rootObj != null && rootObj instanceof SQLConnectableObject) {
            SQLConnectableObject expObj = (SQLConnectableObject) rootObj;
            populateExpressionObject(expObj, condition);
        }
    }

    /**
     * Populates the given SQLCondition object using information from the given SQLObject
     * root.
     * 
     * @param condition SQLCondition object to be populated
     * @param rootObj root expression object
     * @throws Exception if error occurs during parsing or object population
     */
    public static void populateCondition(SQLCondition condition, SQLObject rootObj) throws Exception {
        if (rootObj != null && rootObj instanceof SQLConnectableObject) {
            SQLConnectableObject expObj = (SQLConnectableObject) rootObj;
            populateExpressionObject(expObj, condition);
        }
    }

    private static void populateChildObject(SQLObject obj, SQLCondition condition) throws BaseException {
        List childList = obj.getChildSQLObjects();
        Iterator it = childList.iterator();

        while (it.hasNext()) {
            SQLObject chldObj = (SQLObject) it.next();
            if (chldObj instanceof SQLConnectableObject) {
                populateExpressionObject((SQLConnectableObject) chldObj, condition);
            } else {
                populateLeafObject(chldObj, condition);
            }
        }
    }

    private static void populateExpressionObject(SQLConnectableObject expObj, SQLCondition condition) throws BaseException {
        // add the expression object to condition
        condition.addObject(expObj);

        // now go through the inputs of expression object and add them also in condition
        Map inputMap = expObj.getInputObjectMap();
        Iterator it = inputMap.keySet().iterator();

        while (it.hasNext()) {
            String argName = (String) it.next();
            SQLInputObject inputObj = (SQLInputObject) inputMap.get(argName);
            SQLObject srcObj = inputObj.getSQLObject();
            // if srcObj is a SQLCanvasObject then only we can add
            // it to the condition. condition holds all SQLCanvasObject
            // which can be linked.
            // This srcObj may not be a SQLCanvasObject if it is
            // a part of object like SQLLiteral for literal values
            if (srcObj instanceof SQLCanvasObject) {
                if (srcObj instanceof SQLConnectableObject) {
                    populateExpressionObject((SQLConnectableObject) srcObj, condition);
                } else {
                    populateLeafObject(srcObj, condition);
                }
            }
        }

        populateChildObject(expObj, condition);
    }

    private static void populateLeafObject(SQLObject obj, SQLCondition condition) throws BaseException {
        condition.addObject(obj);
    }

    /** Creates a new instance of ConditionUtil */
    private ConditionUtil() {
    }
}

