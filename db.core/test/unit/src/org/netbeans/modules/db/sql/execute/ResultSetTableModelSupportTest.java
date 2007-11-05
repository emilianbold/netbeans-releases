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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.db.sql.execute;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.sql.DatabaseMetaData;
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
        DatabaseMetaData dmd = (DatabaseMetaData)Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] { DatabaseMetaData.class}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return null;
            }
        });
        assertNotNull(ResultSetTableModelSupport.getColumnTypeDef(dmd, type));
    }
}
