/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.nativeexecution.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 * @author Vladimir Kvashin
 */
public class NativeExecutionBaseTestSuite extends NbTestSuite {
    /**
     * Constructs an empty TestSuite.
     */
    public NativeExecutionBaseTestSuite() {
        super();
    }

    /**
     * Constructs a TestSuite from the given class. Adds all the methods
     * starting with "test" as test cases to the suite.
     *
     */
    public NativeExecutionBaseTestSuite(Class<? extends TestCase> theClass) {
        super(theClass);
    }

    /**
     * Constructs an empty TestSuite.
     */
    public NativeExecutionBaseTestSuite(String name) {
        super(name);
    }

    /**
     * Adds a test that will be run with each specified execution environment
     * @param testClass test class to add.
     *
     * The <? extends NativeExecutionBaseTestCase> is probably too strong - <? extends TestCase> would be sufficient
     * (the only check is the search for 2-parammeter constructor that takes String and ExecutinEnvironmant);
     * the intention was rather to explain what it's used for than to restrict.
     *
     * @param environments execution environments to test against
     */
    public void addTest(Class<? extends NativeExecutionBaseTestCase> testClass, ExecutionEnvironment... environments) {
        Constructor<?> ctor = findConstructor(testClass);
        if (ctor == null) {
            return;
        }
        String[] testMethodNames = findTestMethods(testClass);
        if (testMethodNames.length == 0) {
            addTest(warning("Class " + testClass.getName() + " has no runnable test metods"));
        }

        for (ExecutionEnvironment env : environments) {
            for (String methodName : testMethodNames) {
                addTest(createTest(ctor, methodName, env));
            }
        }
    }

    /**
     * Adds a test that will be run with each specified execution environment
     * @param testClass test class to add.
     *
     * The <? extends NativeExecutionBaseTestCase> is probably too strong - <? extends TestCase> would be sufficient
     * (the only check is the search for 2-parammeter constructor that takes String and ExecutinEnvironmant);
     * the intention was rather to explain what it's used for than to restrict.
     *
     * @param mspecs Strings that represent configurations
     */
    public void addTest(Class<? extends NativeExecutionBaseTestCase> testClass, String... mspecs) throws IOException {
        Constructor<?> ctor = findConstructor(testClass);
        if (ctor == null) {
            return;
        }
        String[] testMethodNames = findTestMethods(testClass);
        if (testMethodNames.length == 0) {
            addTest(warning("Class " + testClass.getName() + " has no runnable test metods"));
        }

        for (String mspec : mspecs) {
            ExecutionEnvironment execEnv = NativeExecutionTestSupport.getTestExecutionEnvironment(mspec);
            if (execEnv != null) {
                for (String methodName : testMethodNames) {
                    addTest(createTest(ctor, methodName, execEnv));
                }
            } else {
                for (String methodName : testMethodNames) {
                    addTest(warning(methodName + " [" + mspec + "]", "Got null execution environment for " + mspec));
                }
            }
        }
    }

    private Test createTest(Constructor<?> ctor, String name, ExecutionEnvironment execEnv) {
        Object[] parameters = new Object[] { name, execEnv };
        try {
            return (Test) ctor.newInstance(parameters);
        } catch (InstantiationException e) {
			return warning("Cannot instantiate test case: "+name+" ("+exceptionToString(e)+")");
		} catch (InvocationTargetException e) {
			return warning("Exception in constructor: "+name+" ("+exceptionToString(e.getTargetException())+")");
		} catch (IllegalAccessException e) {
			return warning("Cannot access test case: "+name+" ("+exceptionToString(e)+")");
		}
    }

	protected static String exceptionToString(Throwable t) {
		StringWriter stringWriter= new StringWriter();
		PrintWriter writer= new PrintWriter(stringWriter);
		t.printStackTrace(writer);
		return stringWriter.toString();
	}

    /**
     * Searches for test methods.
     *
     * Test method is one that either is annotated with @Test
     * or its name starts with "test"
     *
     * NB: such method should be public and return type should be void
     * If it is not a warning is added to tests result
     *
     * @param testClass class to search methods in
     * @return an array of method names
     */
    private String[] findTestMethods(Class testClass) {
        List<String> testMethodNames = new ArrayList<String>();
        for(Class<?> superClass = testClass; Test.class.isAssignableFrom(superClass);
        superClass = superClass.getSuperclass()) {
            for (Method method : superClass.getDeclaredMethods()) {
                if (!testMethodNames.contains(method.getName())) {
                    //Class<?> returnType = m.getReturnType();
                    //returnType.equals(Void.TYPE)
                    if (method.getName().startsWith("test") || method.getAnnotation(org.junit.Test.class) != null) {
                        if (!Modifier.isPublic(method.getModifiers())) {
                            addTest(warning("Method " + testClass.getName() + '.' + method.getName() + " should be public"));
                        } else if (! method.getReturnType().equals(Void.TYPE)) {
                            addTest(warning("Method " + testClass.getName() + '.' + method.getName() + " should be void"));
                        } else if (method.getParameterTypes().length > 0) {
                            addTest(warning("Method " + testClass.getName() + '.' + method.getName() + " should have no parameters"));
                        } else {
                            // OK! The last thing to check is @ignore
                            if (method.getAnnotation(org.junit.Ignore.class) == null) {
                                testMethodNames.add(method.getName());
                            }
                        }
                    }
                }
            }
        }
        return testMethodNames.toArray(new String[testMethodNames.size()]);
    }

    /**
     * Searches for a constructor that takes two parameters:
     * String and ExecutionEnvironment
     * If no such constructor exists, a warning is added to tests result
     */
    private Constructor<?> findConstructor(Class testClass) {
        for (Constructor<?> ctor : testClass.getConstructors()) {
            Class<?>[] parameters = ctor.getParameterTypes();
            if (parameters.length == 2) {
                if (parameters[0].equals(String.class) && parameters[1].equals(ExecutionEnvironment.class)) {
                    return ctor;
                }
            }
        }
        addTest(warning("Class " + testClass.getName() +
                " does not have a constructor with 2 parameters: String and ExecutionEnvironment"));
        return null;
    }

	protected static Test warning(String testName, final String message) {
		return new TestCase(testName) {
			@Override
			protected void runTest() {
				fail(message);
			}
		};
	}

}
