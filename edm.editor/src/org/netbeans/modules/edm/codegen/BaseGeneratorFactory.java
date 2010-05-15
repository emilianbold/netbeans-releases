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
package org.netbeans.modules.edm.codegen;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.edm.model.SQLConstants;


/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 */
public class BaseGeneratorFactory extends AbstractGeneratorFactory {

    public BaseGeneratorFactory(AbstractDB database) {
        super(database);
    }

    public Map<Integer, String> initializeObjectToGeneratorMap() {
        Map<Integer, String> map = new HashMap<Integer, String>();
        map.put(new Integer(SQLConstants.SOURCE_COLUMN), "org.netbeans.modules.edm.codegen.BaseColumnGenerator");
        map.put(new Integer(SQLConstants.COLUMN_REF), "org.netbeans.modules.edm.codegen.BaseConditionColumnGenerator");
        map.put(new Integer(SQLConstants.SOURCE_TABLE), "org.netbeans.modules.edm.codegen.BaseSourceTableGenerator");
        map.put(new Integer(SQLConstants.JOIN), "org.netbeans.modules.edm.codegen.BaseJoinGenerator");
        map.put(new Integer(SQLConstants.VISIBLE_PREDICATE), "org.netbeans.modules.edm.codegen.BaseOperatorGenerator");
        map.put(new Integer(SQLConstants.PREDICATE), "org.netbeans.modules.edm.codegen.BaseOperatorGenerator");
        map.put(new Integer(SQLConstants.VISIBLE_LITERAL), "org.netbeans.modules.edm.codegen.BaseLiteralGenerator");
        map.put(new Integer(SQLConstants.LITERAL), "org.netbeans.modules.edm.codegen.BaseLiteralGenerator");
        map.put(new Integer(SQLConstants.GENERIC_OPERATOR), "org.netbeans.modules.edm.codegen.BaseOperatorGenerator");
        map.put(new Integer(SQLConstants.CUSTOM_OPERATOR), "org.netbeans.modules.edm.codegen.BaseOperatorGenerator");
        map.put(new Integer(SQLConstants.DATE_ARITHMETIC_OPERATOR), "org.netbeans.modules.edm.codegen.BaseOperatorGenerator");
        map.put(new Integer(SQLConstants.CASE), "org.netbeans.modules.edm.codegen.BaseCaseGenerator");
        map.put(new Integer(SQLConstants.DATE_ADD_OPERATOR), "org.netbeans.modules.edm.codegen.BaseDateArithmeticOperatorGenerator");
        map.put(new Integer(SQLConstants.DATE_DIFF_OPERATOR), "org.netbeans.modules.edm.codegen.BaseDateArithmeticOperatorGenerator");
        map.put(new Integer(SQLConstants.CAST_OPERATOR), "org.netbeans.modules.edm.codegen.BaseCastAsOperatorGenerator");
        return map;
    }
}