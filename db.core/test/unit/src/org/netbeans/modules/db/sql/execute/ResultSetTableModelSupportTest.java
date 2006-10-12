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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.sql.execute;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Types;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Andrei Badea
 */
public class ResultSetTableModelSupportTest extends NbTestCase {

    public ResultSetTableModelSupportTest(String testName) {
        super(testName);
    }

    /**
     * Tests that a ColumnTypeDef is defined for each type from java.sql.Types
     * in the TYPE_TO_DEF map.
     */
    public void testAllTypes() throws IllegalAccessException {
        Class clazz = Types.class;
        Field[] fields = clazz.getFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (field.getType() == int.class && Modifier.isStatic(field.getModifiers())) {
                int type = field.getInt(clazz);
                if (ResultSetTableModelSupport.TYPE_TO_DEF.get(new Integer(type)) == null) {
                    fail("No ColumnTypeDef for java.Types." + field.getName());
                }
            }
        }
    }
    
    /**
     * Tests that a ColumnTypeDef is defined even for types not in 
     * java.sql.Types.
     */
    public void testUnknownTypesIssue71040() throws IllegalAccessException {
        // for the test to be meaningful we have to ensure
        // our type is not in java.sql.Types
        int type = Integer.MAX_VALUE;
        Class clazz = Types.class;
        Field[] fields = clazz.getFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (field.getType() == int.class && Modifier.isStatic(field.getModifiers())) {
                if (type == field.getInt(clazz)) {
                    fail("Type " + type + " already defined in java.sql.Types as " + field.getName());
                }
            }
        }
        assertNotNull(ResultSetTableModelSupport.getColumnTypeDef(type));
    }
}
