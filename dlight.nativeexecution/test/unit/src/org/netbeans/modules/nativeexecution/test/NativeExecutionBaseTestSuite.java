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

    private final String defaultSection;

    /**
     * Constructs an empty TestSuite.
     */
    public NativeExecutionBaseTestSuite() {
        super();
        defaultSection = null;
    }

    /**
     * Constructs an empty TestSuite.
     * @param name suite name
     */
    public NativeExecutionBaseTestSuite(String name) {
        super(name);
        defaultSection = null;
    }

    /**
     * Constructs an empty TestSuite.
     * @param testClasses test classes to add.
     * The <? extends NativeExecutionBaseTestCase> is probably too strong - <? extends TestCase> would be sufficient
     * (the only check is the search for 2-parammeter constructor that takes String and ExecutinEnvironmant);
     * the intention was rather to explain what it's used for than to restrict.
     */
    public NativeExecutionBaseTestSuite(Class<? extends NativeExecutionBaseTestCase>... testClasses) {
        super();
        this.defaultSection = null;
        for (Class testClass : testClasses) {
            addTest(testClass);
        }
    }

    /**
     * Constructs an empty TestSuite.
     * @param name suite name
     * @param defaultSection default section for @ForAllEnvironments annotation
     */
    public NativeExecutionBaseTestSuite(String name, String defaultSection) {
        super(name);
        this.defaultSection = defaultSection;
    }

    /**
     * Constructs TestSuite that adds tests specified by classes parameters
     * @param name suite name
     * @param defaultSection default section for @ForAllEnvironments annotation
     * @param testClass test class to add.
     * The <? extends NativeExecutionBaseTestCase> is probably too strong - <? extends TestCase> would be sufficient
     * (the only check is the search for 2-parammeter constructor that takes String and ExecutinEnvironmant);
     * the intention was rather to explain what it's used for than to restrict.
     */
    public NativeExecutionBaseTestSuite(String name, String defaultSection, 
            Class<? extends NativeExecutionBaseTestCase>... testClasses) {

        this(name, defaultSection);
        for (Class testClass : testClasses) {
            addTest(testClass);
        }
    }

    private String[] getPlatforms(String section) {
        try {
            try {
                RcFile rcFile = NativeExecutionTestSupport.getRcFile();
                Collection<String> t = rcFile.getKeys(section);
                return t.toArray(new String[t.size()]);
            } catch (FileNotFoundException ex) {
                // rcfile does not exists - no tests to run
            }
        } catch (IOException ex) {
            addTest(warning("Cannot get execution environment: " + exceptionToString(ex)));
        } catch (FormatException ex) {
            addTest(warning("Cannot get execution environment: " + exceptionToString(ex)));
        }
        return new String[0];
    }

    /**
     * Adds a test.
     * @param testClass test class to add.
     * The <? extends NativeExecutionBaseTestCase> is probably too strong - <? extends TestCase> would be sufficient
     * (the only check is the search for 2-parammeter constructor that takes String and ExecutinEnvironmant);
     * the intention was rather to explain what it's used for than to restrict.
     */
    protected void addTest(Class<? extends NativeExecutionBaseTestCase> testClass)  {
        
        TestClassData testData = findTestData(testClass);
        if (testData.testMethods.isEmpty()) {
            addTest(warning("Class " + testClass.getName() + " has no runnable test metods"));
        }

        for (TestMethodData methodData : testData.testMethods) {
            if (!checkConditionals(methodData)) {
                continue;
            }
            if (methodData.isForAllEnvironments()) {
                String[] platforms = getPlatforms(methodData.envSection);
                for (String platform : platforms) {
                    if (testData.forAllEnvConstructor == null) {
                        addTest(warning("Class " + testClass.getName() +
                                " does not have a constructor with 2 parameters: String and ExecutionEnvironment"));
                        break;
                    }
                    try {
                        ExecutionEnvironment execEnv = NativeExecutionTestSupport.getTestExecutionEnvironment(platform);
                        if (execEnv != null) {
                            addTest(createTest(testData.forAllEnvConstructor, methodData.name, execEnv));
                        } else {
                            addTest(warning(methodData.name + " [" + platform + "]",
                                    "Got null execution environment for " + platform));
                        }
                    } catch (IOException ioe) {
                        addTest(warning(methodData.name + " [" + platform + "]",
                                "Error getting execution environment for " + platform + ": " + exceptionToString(ioe)));
                    }
                }
            } else {
                if (testData.ordinaryConstructor == null) {
                    addTest(warning("Class " + testClass.getName() +
                            " does not have a constructor with 1 parameter of String type"));
                    break;
                }
                addTest(createTest(testData.ordinaryConstructor, methodData.name));
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

        /** The name of the method */
        public final String name;

        /** 
         * In the case the method is annotated with @ForAllEnvironments, contains it's section
         * (or default one in the case it isn't specified in the annotation);
         * if the method is not annotated with @ForAllEnvironments, contains null
         */
        public final String envSection;
        public final String condSection;
        public final String condKey;
        public final boolean condDefault;

        public TestMethodData(String name, String envSection, String condSection, String condKey, boolean condDefault) {
            this.name = name;
            this.envSection = envSection;
            this.condSection = condSection;
            this.condKey = condKey;
            this.condDefault = condDefault;
        }


        public boolean isForAllEnvironments() {
            return envSection != null;
        }
    }

    private static class TestClassData {
        
        // making fields public would be unsafe if this class wasn't private static :-)
        public List<TestMethodData> testMethods = new ArrayList<TestMethodData>();
        public Constructor<?> ordinaryConstructor = null;
        public Constructor<?> forAllEnvConstructor = null;
        
        public boolean containsMethod(String name) {
            if (name != null) {
                for (TestMethodData md : testMethods) {
                    if (name.equals(md.name)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * Checking @conditional and @ignore annotations
     * @param method method to check
     * @return true in the case there are no @ignore annotation
     * and either there are no @conditional or it's condition is true
     */
    private boolean checkConditionals(TestMethodData methodData) {
        if (methodData.condSection == null || methodData.condSection.length() == 0) {
            return true; // no condition
        }
        if (methodData.condKey == null || methodData.condKey.length() == 0) {
            addTest(warning(methodData.name + " @condition does not specify key"));
            return false;
        }
        try {
            RcFile rcFile = NativeExecutionTestSupport.getRcFile();
            String value = rcFile.get(methodData.condSection, methodData.condKey);
            if (value == null) {
                return methodData.condDefault;
            } else {
                return Boolean.parseBoolean(value);
            }
        } catch (FileNotFoundException ex) {
            // silently: just no file => condition is false, that's it
            return false;
        } catch (IOException ex) {
            addTest(warning("Error getting condition for " + methodData.name + ": " + ex.getMessage()));
            return false;
        } catch (RcFile.FormatException ex) {
            addTest(warning("Error getting condition for " + methodData.name + ": " + ex.getMessage()));
            return false;
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
    private TestClassData findTestData(Class testClass) {

        TestClassData result = new TestClassData();

        for(Class<?> superClass = testClass; Test.class.isAssignableFrom(superClass);
        superClass = superClass.getSuperclass()) {
            for (Method method : superClass.getDeclaredMethods()) {
                if (!result.containsMethod(method.getName())) {
                    ForAllEnvironments forAllEnvAnnotation = method.getAnnotation(ForAllEnvironments.class);
                    
                    if (method.getName().startsWith("test") 
                            || method.getAnnotation(org.junit.Test.class) != null
                            || forAllEnvAnnotation != null) {
                        if (!Modifier.isPublic(method.getModifiers())) {
                            addTest(warning("Method " + testClass.getName() + '.' + method.getName() + " should be public"));
                        } else if (! method.getReturnType().equals(Void.TYPE)) {
                            addTest(warning("Method " + testClass.getName() + '.' + method.getName() + " should be void"));
                        } else if (method.getParameterTypes().length > 0) {
                            addTest(warning("Method " + testClass.getName() + '.' + method.getName() + " should have no parameters"));
                        } else {
                            if (method.getAnnotation(org.junit.Ignore.class) == null) {
                                Conditional conditionalAnnotation = method.getAnnotation(Conditional.class);
                                String condSection = (conditionalAnnotation == null) ? null : conditionalAnnotation.section();
                                String condKey = (conditionalAnnotation == null) ? null : conditionalAnnotation.key();
                                boolean condDefault = (conditionalAnnotation == null) ? false : conditionalAnnotation.defaultValue();
                                if (forAllEnvAnnotation != null) {
                                    String envSection = forAllEnvAnnotation.section();
                                    if (envSection == null || envSection.length() == 0) {
                                        envSection = defaultSection;
                                    }
                                    if (envSection != null && envSection.length() > 0) {
                                        result.testMethods.add(new TestMethodData(method.getName(), envSection, condSection, condKey, condDefault));
                                    } else {
                                        addTest(warning("@ForAllEnvironments annotation for method " + testClass.getName() + '.' + method.getName() + " does not specify section"));
                                    }
                                } else {
                                    result.testMethods.add(new TestMethodData(method.getName(), null, condSection, condKey, condDefault));
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
                result.ordinaryConstructor = ctor;
            }
            if (parameters.length == 2
                    && parameters[0].equals(String.class)
                    && parameters[1].equals(ExecutionEnvironment.class)) {
                result.forAllEnvConstructor = ctor;
            }
        }

        return result;
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
