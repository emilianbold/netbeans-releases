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

package org.netbeans.modules.search;

import java.lang.reflect.Field;
import junit.framework.TestCase;
import junit.framework.*;

/**
 *
 *
 * @author  Marian Petras
 */
public final class ResultModelTest extends TestCase {
    
    private static final String FIELD_SEARCH_TYPE_PACKAGE = "DEF_SEARCH_TYPES_PACKAGE";
    private static final String FIELD_FULLTEXT_SEARCH_TYPE = "FULLTEXT_SEARCH_TYPE";
    
    public ResultModelTest(String testName) {
        super(testName);
    }
    
    /**
     */
    public void testConstants() throws Exception {
        Class clazz = ResultModel.class;
        String pkg = getStaticString(clazz, FIELD_SEARCH_TYPE_PACKAGE);
        String className = getStaticString(clazz, FIELD_FULLTEXT_SEARCH_TYPE);
        String fullClassName = pkg + '.' + className;
        try {
            Class.forName(fullClassName, false, getClass().getClassLoader());
        } catch (ClassNotFoundException ex) {
            fail("class " + fullClassName + " does not exist" +
                 " (wrong values of fields " + FIELD_SEARCH_TYPE_PACKAGE +
                 " and/or " + FIELD_FULLTEXT_SEARCH_TYPE + ")");
        }
    }
    
    /**
     */
    private static String getStaticString(Class clazz,
                                          String fieldName) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (String) field.get(null);
    }
    
}
