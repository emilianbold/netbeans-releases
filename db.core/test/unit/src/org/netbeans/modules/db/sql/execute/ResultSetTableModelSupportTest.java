/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
                int type = field.getInt(Types.class);
                if (ResultSetTableModelSupport.TYPE_TO_DEF.get(new Integer(type)) == null) {
                    fail("No ColumnTypeDef for java.Types." + field.getName());
                }
            }
        }
    }
}
