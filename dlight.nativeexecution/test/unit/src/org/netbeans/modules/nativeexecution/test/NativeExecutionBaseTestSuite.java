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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.RcFile.FormatException;

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
     * Constructs TestSuite that takes platforms (mspecs) from the given section,
     * and performs tests specified by classes parameters for each of them
     * @param name suite name
     * @param mspecSection section of the .cndtestrc that contains platforms as keys
     * @param testClasses test classes
     */
    public NativeExecutionBaseTestSuite(String name, String mspecSection, Class... testClasses) {
        super(name);
        try {
            for (Class testClass : testClasses) {
                try {
                    RcFile rcFile = NativeExecutionTestSupport.getRcFile();
                    Collection<String> t = rcFile.getKeys(mspecSection);
                    String[] mspecs = t.toArray(new String[t.size()]);
                    addTest(testClass, mspecs);
                } catch (FileNotFoundException ex) {
                    // rcfile does not exists - no tests to run
                }
            }
        } catch (IOException ex) {
            addTest(warning("Cannot get execution environment: " + exceptionToString(ex)));
        } catch (FormatException ex) {
            addTest(warning("Cannot get execution environment: " + exceptionToString(ex)));
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
     * @param environments execution environments to test against
     */
    public void addTest(Class<? extends NativeExecutionBaseTestCase> testClass, ExecutionEnvironment... environments) {
        TestMethodData testData = findTestData(testClass);
        if (testData.ordinaryMethodNames.isEmpty() && testData.forEachEnvironmentMethodNames.isEmpty()) {
            addTest(warning("Class " + testClass.getName() + " has no runnable test metods"));
        }

        for (String methodName : testData.ordinaryMethodNames) {
            if (testData.ordinaryConstructor == null) {
                addTest(warning("Class " + testClass.getName() +
                        " does not have a constructor with 1 parameter of String type"));
                break;
            }
            addTest(createTest(testData.ordinaryConstructor, methodName));
        }
        for (ExecutionEnvironment env : environments) {
            if (testData.forAllEnvConstructor == null) {
                addTest(warning("Class " + testClass.getName() +
                        " does not have a constructor with 2 parameters: String and ExecutionEnvironment"));
                break;
            }
            for (String methodName : testData.forEachEnvironmentMethodNames) {
                addTest(createTest(testData.forAllEnvConstructor, methodName, env));
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

        TestMethodData testData = findTestData(testClass);
        if (testData.ordinaryMethodNames.isEmpty() && testData.forEachEnvironmentMethodNames.isEmpty()) {
            addTest(warning("Class " + testClass.getName() + " has no runnable test metods"));
        }

        for (String methodName : testData.ordinaryMethodNames) {
            if (testData.ordinaryConstructor == null) {
                addTest(warning("Class " + testClass.getName() +
                        " does not have a constructor with 1 parameter of String type"));
                break;
            }
            addTest(createTest(testData.ordinaryConstructor, methodName));
        }

        for (String mspec : mspecs) {
            if (testData.forAllEnvConstructor == null) {
                addTest(warning("Class " + testClass.getName() +
                        " does not have a constructor with 2 parameters: String and ExecutionEnvironment"));
                break;
            }
            ExecutionEnvironment execEnv = NativeExecutionTestSupport.getTestExecutionEnvironment(mspec);
            if (execEnv != null) {
                for (String methodName : testData.forEachEnvironmentMethodNames) {
                    addTest(createTest(testData.forAllEnvConstructor, methodName, execEnv));
                }
            } else {
                for (String methodName : testData.forEachEnvironmentMethodNames) {
                    addTest(warning(methodName + " [" + mspec + "]", "Got null execution environment for " + mspec));
                }
            }
        }
    }

    private Test createTest(Constructor<?> ctor, Object... parameters) {
        assert parameters != null;
        assert parameters.length > 0;
        String name = (String) parameters[0];
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

    private static class TestMethodData {

        // making collections public would be unsafe if this class wasn't private static :-)
        public final List<String> ordinaryMethodNames;
        public final List<String> forEachEnvironmentMethodNames;

        public final Constructor<?> ordinaryConstructor;
        public final Constructor<?> forAllEnvConstructor;

        public TestMethodData(List<String> ordinaryMethodNames, List<String> forEachEnvironmentMethodNames, Constructor<?> ordinaryConstructor, Constructor<?> forAllEnvConstructor) {
            this.ordinaryMethodNames = ordinaryMethodNames;
            this.forEachEnvironmentMethodNames = forEachEnvironmentMethodNames;
            this.ordinaryConstructor = ordinaryConstructor;
            this.forAllEnvConstructor = forAllEnvConstructor;
        }
    }

    /**
     * Searches for 
     * - test methods
     * - constructors
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
    private TestMethodData findTestData(Class testClass) {

        List<String> ordinaryMethodNames = new ArrayList<String>();
        List<String> forEachEnvironmentMethodNames = new ArrayList<String>();

        Constructor<?> ordinaryConstructor = null;
        Constructor<?> forAllEnvConstructor = null;

        for(Class<?> superClass = testClass; Test.class.isAssignableFrom(superClass);
        superClass = superClass.getSuperclass()) {
            for (Method method : superClass.getDeclaredMethods()) {
                if (!ordinaryMethodNames.contains(method.getName())
                && !forEachEnvironmentMethodNames.contains(method.getName())) {
                    //Class<?> returnType = m.getReturnType();
                    //returnType.equals(Void.TYPE)
                    if (method.getName().startsWith("test") 
                            || method.getAnnotation(org.junit.Test.class) != null
                            || method.getAnnotation(ForAllEnvironments.class) != null) {
                        if (!Modifier.isPublic(method.getModifiers())) {
                            addTest(warning("Method " + testClass.getName() + '.' + method.getName() + " should be public"));
                        } else if (! method.getReturnType().equals(Void.TYPE)) {
                            addTest(warning("Method " + testClass.getName() + '.' + method.getName() + " should be void"));
                        } else if (method.getParameterTypes().length > 0) {
                            addTest(warning("Method " + testClass.getName() + '.' + method.getName() + " should have no parameters"));
                        } else {
                            // OK! The last thing to check is @ignore
                            if (method.getAnnotation(org.junit.Ignore.class) == null) {
                                if (method.getAnnotation(ForAllEnvironments.class) != null) {
                                    forEachEnvironmentMethodNames.add(method.getName());
                                } else {
                                    ordinaryMethodNames.add(method.getName());
                                }
                            }
                        }
                    }
                }
            }
        }

        for (Constructor<?> ctor : testClass.getConstructors()) {
            Class<?>[] parameters = ctor.getParameterTypes();
            if (parameters.length == 1 && parameters[0].equals(String.class)) {
                ordinaryConstructor = ctor;
            }
            if (parameters.length == 2
                    && parameters[0].equals(String.class)
                    && parameters[1].equals(ExecutionEnvironment.class)) {
                forAllEnvConstructor = ctor;
            }
        }

        return new TestMethodData(ordinaryMethodNames, forEachEnvironmentMethodNames,
                ordinaryConstructor, forAllEnvConstructor);
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
