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
package org.netbeans.modules.cnd.modelimpl.recovery.base;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import static junit.framework.TestSuite.warning;
import org.netbeans.junit.NbTestSuite;
import org.openide.util.Exceptions;

/**
 *
 */
public class RecoverySuiteTestBase extends NbTestSuite {
    private final AtomicInteger newGrammar = new AtomicInteger(0);
    private final AtomicInteger oldGrammar = new AtomicInteger(0);

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

        for (Map.Entry<String, Method> entry : testData.testMethods.entrySet()) {
            List<Test> tests = createTests(testData.constructor, entry.getKey(), entry.getValue());
            for(Test t : tests) {
                addTest(t);
            }
        }
    }

    @Override
    public void runTest(Test test, TestResult result) {
        try {
            super.runTest(test, result);
        } finally {
            if (test instanceof RecoveryTestCaseBase) {
                if (!((RecoveryTestCaseBase)test).isGolden()) {
                    if (((RecoveryTestCaseBase)test).isNewGramma()) {
                        newGrammar.incrementAndGet();
                    } else {
                        oldGrammar.incrementAndGet();
                    }
                }
            }
            if (result.runCount() == this.testCount()) {
                Enumeration<TestFailure> failures = result.failures();
                int newGrammarFail = 0;
                int oldGrammarFail = 0;
                while(failures.hasMoreElements()) {
                    TestFailure next = failures.nextElement();
                    Test failedTest = next.failedTest();
                    if (failedTest instanceof RecoveryTestCaseBase) {
                        if (((RecoveryTestCaseBase)failedTest).isNewGramma()) {
                            newGrammarFail++;
                        } else {
                            oldGrammarFail++;
                        }
                    }
                }
                if (newGrammar.get() > 0) {
                    System.err.println("New Grammar recovery tests "+newGrammar.get()+" fail "+newGrammarFail+" ("+(newGrammarFail*100/newGrammar.get())+"%)");
                }
                if (oldGrammar.get() > 0) {
                    System.err.println("Old Grammar recovery tests "+oldGrammar.get()+" fail "+oldGrammarFail+" ("+(oldGrammarFail*100/oldGrammar.get())+"%)");
                }
            }
        }
    }
    
    private List<Test> createTests(Constructor<?> ctor, String name, Method method) {
        List<Test> res = new ArrayList<>();
        List<Grammar> gList = new ArrayList<>();
        List<Diff> dList = new ArrayList<>();
        Grammars grammars = method.getAnnotation(Grammars.class);
        if (grammars != null) {
            gList.addAll(Arrays.asList(grammars.value()));
        } else {
            Grammar g = method.getAnnotation(Grammar.class);
            if (g != null) {
                gList.add(g);
            }
        }
        if (gList.isEmpty()) {
            System.err.println("Empty list of grammars "+name);
            return res;
        }
        Golden golden = method.getAnnotation(Golden.class);
        Diffs diffs = method.getAnnotation(Diffs.class);
        if (diffs != null) {
            dList.addAll(Arrays.asList(diffs.value()));
        } else {
            Diff diff = method.getAnnotation(Diff.class);
            if (diff != null) {
                dList.add(diff);
            }
        }
        if (golden == null) {
            if (dList.isEmpty()) {
                System.err.println("Empty list of diffs "+name);
                return res;
            }
        } else {
            if (gList.size() > 1) {
                System.err.println("Golden test invoked for several grammar "+name);
                return res;
            }
            if (dList.size() > 1) {
                System.err.println("Golden test invoked for several diffs "+name);
                return res;
            }
        }
        try {
            if (golden != null) {
                if (dList.isEmpty()) {
                    res.add((Test) ctor.newInstance(name, gList.get(0), null, golden));
                } else {
                    res.add((Test) ctor.newInstance(name, gList.get(0), dList.get(0), golden));
                }
            } else {
                for(Diff d : dList) {
                    String type = d.type();
                    if (type.isEmpty()) {
                        for(Grammar g : gList) {
                            res.add((Test) ctor.newInstance(name, g, d, golden));
                        }
                    } else {
                        for(String s:balancedType(type)) {
                            Diff current = new MyDiff(d, s);
                            for(Grammar g : gList) {
                                res.add((Test) ctor.newInstance(name, g, current, golden));
                            }
                        }
                    }
                }
            }
            return res;
        } catch (InstantiationException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.<Test>emptyList();
    }
    
    private List<String> balancedType(String type) {
        List<String> res = new ArrayList<>();
        for(int i = 1; i < type.length(); i++) {
            String beg = type.substring(0, i);
            String rest = type.substring(i);
            if (beg.indexOf('{')>=0 && rest.indexOf('}')>=0) {
                res.add(beg+"}");
            } else {
                if ("}".equals(rest) && beg.indexOf('{')>=0) {
                } else {
                    res.add(beg);
                }
            }
        }
        return res;
    }

    private List<String> simpleType(String type) {
        List<String> res = new ArrayList<>();
        for(int i = 1; i < type.length(); i++) {
            res.add(type.substring(0, i));
        }
        return res;
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
                            result.testMethods.put(method.getName(), method);
                        }
                    }
                }
            }
        }

        for (Constructor<?> ctor : testClass.getConstructors()) {
            Class<?>[] parameters = ctor.getParameterTypes();
            if (parameters.length == 4 &&
                parameters[0].equals(String.class) &&
                parameters[1].equals(Grammar.class) &&
                parameters[2].equals(Diff.class) &&
                parameters[3].equals(Golden.class) ) {
                result.constructor = ctor;
            }
        }

        return result;
    }

    private static class TestClassData {

        public Map<String, Method> testMethods = new TreeMap<>();
        public Constructor<?> constructor = null;

        public boolean containsMethod(String name) {
            return testMethods.containsKey(name);
        }
    }
    
    private final class MyDiff implements Diff {
        private final Diff delegate;
        private final String type;
        
        private MyDiff(Diff delegate, String type) {
            this.delegate = delegate;
            this.type = type;
        }

        @Override
        public String file() {
            return delegate.file();
        }

        @Override
        public int line() {
            return delegate.line();
        }

        @Override
        public int column() {
            return delegate.column();
        }

        @Override
        public int length() {
            return delegate.length();
        }

        @Override
        public String insert() {
            return type;
        }

        @Override
        public String type() {
            return "";
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return delegate.annotationType();
        }        
    }
}
