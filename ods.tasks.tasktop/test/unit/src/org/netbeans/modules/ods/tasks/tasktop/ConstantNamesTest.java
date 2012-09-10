/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ods.tasks.tasktop;

import com.tasktop.c2c.internal.client.tasks.core.C2CConstants;
import com.tasktop.c2c.internal.client.tasks.core.data.C2CTaskAttribute;
import java.lang.reflect.Field;
import junit.framework.Test;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.ods.tasks.spi.C2CData;


/** Verifies the values of C2C constants are the same as the ones used
 * by C2CData.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ConstantNamesTest extends NbTestCase {
    private final Class<?> c2;
    private final Class<?> c1;
    private final String n2, n1;
    public ConstantNamesTest(String field, Class<?> c1, Class<?> c2, String n1, String n2) {
        super(field);
        this.c1 = c1;
        this.c2 = c2;
        this.n1 = n1;
        this.n2 = n2;
    }
    
    public static Test suite() {
        NbTestSuite s = new NbTestSuite();
        int cnt = 0;
        for (Field f : C2CData.class.getDeclaredFields()) {
            if (f.getName().startsWith("ATTR_")) {
                s.addTest(new ConstantNamesTest(
                    "test" + f.getName(), C2CData.class, 
                    C2CTaskAttribute.class, f.getName(), f.getName().substring(5)
                ));
                cnt++;
            }
        }
        if (cnt < 5) {
            fail("There should be at least 5 attributes: " + cnt);
        }
        s.addTest(new ConstantNamesTest(
            "CUSTOM_FIELD_PREFIX", C2CData.class, C2CConstants.class, 
            "CUSTOM_FIELD_PREFIX", "CUSTOM_FIELD_PREFIX"
        ));
        cnt = 0;
        for (Field orig : C2CTaskAttribute.class.getFields()) {
            try {
                Field f = C2CData.class.getField("ATTR_" + orig.getName());
                continue;
            } catch (Exception ex) {
                // OK, go on we don't have an override of the attribute
            }
            
            try {
                Field f = TaskAttribute.class.getField(orig.getName());
                s.addTest(new ConstantNamesTest(
                    "testC2CAndTaskAttributeConsistency" + f.getName(), 
                    TaskAttribute.class, C2CTaskAttribute.class, f.getName(), f.getName()
                ));
                cnt++;
            } catch (Exception ex) {
                continue;
            }
        }
        if (cnt < 5) {
            fail("There should be at least 5 attributes: " + cnt);
        }
        return s;
    }

    @Override
    protected void runTest() throws Throwable {
        Field f1 = c1.getField(n1);
        Field f2 = c2.getField(n2);
        Object v1 = f1.get(null);
        Object v2 = f2.get(null);
        if (v2 instanceof C2CTaskAttribute) {
            C2CTaskAttribute ta = (C2CTaskAttribute)v2;
            v2 = ta.getKey();
        }
        
        assertEquals("Values for " + getName() + " are OK", v1, v2);
    }
    
    
}
