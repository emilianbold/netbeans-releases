/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.recovery;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.TreeMap;
import junit.framework.Test;
import static junit.framework.TestSuite.warning;
import org.netbeans.junit.NbTestSuite;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexander Simon
 */
public class RecoverySuiteTestBase extends NbTestSuite {

    public RecoverySuiteTestBase(String name) {
        super(name);
    }

    /**
     * Adds a test.
     *
     * @param testClass test class to add. The <? extends
     * NativeExecutionBaseTestCase> is probably too strong - <? extends
     * TestCase> would be sufficient (the only check is the search for
     * 2-parammeter constructor that takes String and ExecutinEnvironmant); the
     * intention was rather to explain what it's used for than to restrict.
     */
    protected final void addTest(Class<?> testClass) {

        TestClassData testData = findTestData(testClass);
        if (testData.testMethods.isEmpty()) {
            addTest(warning("Class " + testClass.getName() + " has no runnable test metods"));
        }

        for (String name : testData.testMethods.keySet()) {
            addTest(createTest(testData.ordinaryConstructor,name));
        }
    }

    private Test createTest(Constructor<?> ctor, Object... parameters) {
        assert parameters != null;
        assert parameters.length > 0;
        String name = (String) parameters[0];
        try {
            return (Test) ctor.newInstance(parameters);
        } catch (InstantiationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private TestClassData findTestData(Class<?> testClass) {

        TestClassData result = new TestClassData();

        for (Class<?> superClass = testClass; Test.class.isAssignableFrom(superClass);
                superClass = superClass.getSuperclass()) {
            for (Method method : superClass.getDeclaredMethods()) {
                if (!result.containsMethod(method.getName())) {
                    if (method.getName().startsWith("test") || method.getAnnotation(org.junit.Test.class) != null) {
                        if (!Modifier.isPublic(method.getModifiers())) {
                            addTest(warning("Method " + testClass.getName() + '.' + method.getName() + " should be public"));
                        } else if (!method.getReturnType().equals(Void.TYPE)) {
                            addTest(warning("Method " + testClass.getName() + '.' + method.getName() + " should be void"));
                        } else if (method.getParameterTypes().length > 0) {
                            addTest(warning("Method " + testClass.getName() + '.' + method.getName() + " should have no parameters"));
                        } else {
                            result.testMethods.put(method.getName(), method.getName());
                        }
                    }
                }
            }
        }

        for (Constructor<?> ctor : testClass.getConstructors()) {
            Class<?>[] parameters = ctor.getParameterTypes();
            if (parameters.length == 1 && parameters[0].equals(String.class)) {
                result.ordinaryConstructor = ctor;
            }
        }

        return result;
    }

    private static class TestClassData {

        public Map<String, String> testMethods = new TreeMap<String, String>();
        public Constructor<?> ordinaryConstructor = null;
        public Constructor<?> forAllEnvConstructor = null;

        public boolean containsMethod(String name) {
            return testMethods.containsKey(name);
        }
    }
}
