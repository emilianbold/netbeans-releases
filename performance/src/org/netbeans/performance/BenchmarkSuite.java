/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.performance;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Enumeration;
import java.util.StringTokenizer;

import junit.framework.Test;
import junit.framework.TestResult;

import org.netbeans.performance.bde.TestSpecBuilder;
import org.netbeans.performance.bde.TestDefinition;
import org.netbeans.performance.bde.ArgumentSeries;
import org.netbeans.performance.bde.Interval;

/**
 * A suite for Benchmarks;
 */
public final class BenchmarkSuite implements Test {
    
    public static final String TESTS_SPECS = "tests.specs";
    
    private ArrayList benchmarks;
    private TestsSpecifications testSpecs;
    
    /** New Benchmark suite */
    public BenchmarkSuite(String testSpecs) {
        this.benchmarks = new ArrayList(20);
        this.testSpecs = new TestsSpecifications(testSpecs);
    }

    /** New Benchmark suite */
    public BenchmarkSuite() {
        this(System.getProperty(TESTS_SPECS));
    }
    
    /** Adds a Test to this suite */
    public void addBenchmarkClass(Class klass) {
        try {
            if (testSpecs.implies(klass)) {
                benchmarks.addAll(createTestsForClass(klass));
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
    
    /** Counts no of tests */
    public int countTestCases() {
        return benchmarks.size();
    }
    
    /** Run this suite */
    public void run(TestResult result) {
        for (Iterator iter = benchmarks.iterator(); iter.hasNext(); ) {
            ((Test) iter.next()).run(result);
        }
    }
    
    //----------------- impl methods -------------------
    /** Creates tests for given Class */
    private Collection createTestsForClass(Class klass) throws Exception {
        ArrayList list = new ArrayList(50);
        Method[] methods = klass.getMethods();
        Constructor constructor = klass.getConstructor(new Class[] { String.class });
        
        for (int i = 0; i < methods.length; i++) {
            String method = methods[i].getName();
            if (isTestMethod(methods[i]) && testSpecs.implies(klass, method)) {
                Benchmark test = (Benchmark) constructor.newInstance(new Object[] { method });
                testSpecs.setArguments(test);
                list.add(test);
            }
        }
        
        return list;
    }
    
    /** Does m comply to a definition public, no args, test, ... */
    private static boolean isTestMethod(Method m) {
        int mod = m.getModifiers();
        return  Modifier.isPublic(mod) && (m.getReturnType() == Void.TYPE) &&
                (m.getParameterTypes().length == 0) && m.getName().startsWith("test");
    }

    /** Filters tests to run */
    static final class TestsSpecifications {
        
        private Map testsSpecifications;
        
        /** New TestsSpecifications */
        public TestsSpecifications(String spec) {
            testsSpecifications = new HashMap();
            try {
                List list = TestSpecBuilder.parse(spec);
                for (Iterator iter = list.iterator(); iter.hasNext(); ) {
                    TestDefinition testDef = (TestDefinition) iter.next();
                    Class klass = Class.forName(testDef.getClassName());
                    testsSpecifications.put(klass, new TestSpecification(testDef));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        /** Whether given class should be included */
        public boolean implies(Class klass) throws Exception {
            boolean ret = staticTest(klass);
            
            if (ret && (testsSpecifications.size() != 0)) {
                ret = (testsSpecifications.get(klass) != null);
            }
            
            return ret;
        }
        
        /** Whether given method should be included */
        public boolean implies(Class klass, String method) {
            TestSpecification spec = (TestSpecification) testsSpecifications.get(klass);
            if (spec == null) {
                return true;
            } else {
                return spec.implies(method);
            }
        }
        
        public void setArguments(Benchmark test) {
            TestSpecification spec = (TestSpecification) testsSpecifications.get(test.getClass());
            if (spec != null) {
                spec.setArguments(test);
            }
        }
        
        /** Tests needed conditions */
        private static boolean staticTest(Class klass) throws NoSuchMethodException {
            int mod = klass.getModifiers();
            return (Benchmark.class.isAssignableFrom(klass) && Modifier.isPublic(mod) &&
                    !Modifier.isAbstract(mod) && (klass.getConstructor(new Class[] { String.class }) != null)
            );
        }
    }
    
    /** Filters test methods */
    static final class TestSpecification {
        
        private List methodMatchers;
        private List argSeries;
        
        /** New TestsSpecifications */
        public TestSpecification(TestDefinition testDef) {
            setMethodMatchers(testDef.getMethodPatterns());
            setArgSeries(testDef.getArgumentSeries());
        }
        
        /** Creates MethodMetchers */
        private void setMethodMatchers(Iterator it) {
            methodMatchers = new ArrayList();
            while (it.hasNext()) {
                String pattern = (String) it.next();
                methodMatchers.add(MethodMatcher.create(pattern));
            }
        }
        
        /** Creates a List of ArgumentSeries */
        private void setArgSeries(Iterator it) {
            argSeries = new ArrayList();
            while (it.hasNext()) {
                argSeries.add(it.next());
            }
        }
        
        /** Whether given method should be included */
        public boolean implies(String method) {
            if (methodMatchers.size() == 0) {
                return true;
            }
            
            for (Iterator iter = methodMatchers.iterator(); iter.hasNext(); ) {
                MethodMatcher matcher = (MethodMatcher) iter.next();
                if (matcher.matches(method)) {
                    return true;
                }
            }
            
            return false;
        }
        
        /** Creates arguments for given params */
        private static void createArgs(Iterator keys, ArgumentSeries as, MapArgBenchmark mab, List mapList) {
            if (keys.hasNext()) {
                String key = (String) keys.next();
                Iterator vals = as.getValues(key);
                while (vals.hasNext()) {
                    List localMapList = new ArrayList(11);
                    Object next = vals.next();
                    
                    if (next instanceof Interval) {
                        Interval interval = (Interval) next;
                        int i = interval.getStart();
                        List localMapList2 = new ArrayList(11);
                        for (; i < interval.getEnd(); i += interval.getStep()) {
                            createArgsAndPut(keys, as, mab, localMapList2, key, new Integer(i));
                            localMapList.addAll(localMapList2);
                            localMapList2.clear();
                        }
                        if (i >= interval.getEnd()) {
                            createArgsAndPut(keys, as, mab, localMapList2, key, new Integer(interval.getEnd()));
                            localMapList.addAll(localMapList2);
                        }
                    } else {
                        createArgsAndPut(keys, as, mab, localMapList, key, next);
                    }
                    mapList.addAll(localMapList);
                }
            } else {
                mapList.add(mab.createDefaultMap());
            }
        }
            
        private static void createArgsAndPut(Iterator keys, ArgumentSeries as, MapArgBenchmark mab, List mapList, Object key, Object val) {
            createArgs(keys, as, mab, mapList);
            int size = mapList.size();
            for (int i = 0; i < size; i++) {
                Map map = (Map) mapList.get(i);
                map.put(key, val);
            }
        }
        
        /** Create a Collection of Benchmarks */
        public void setArguments(Benchmark bench) {
            if (bench instanceof MapArgBenchmark) {
                MapArgBenchmark test = (MapArgBenchmark) bench;
                List args = new ArrayList(20);
                
                final int size = argSeries.size();
                for (int i = 0; i < size; i++) {
                    ArgumentSeries as = (ArgumentSeries) argSeries.get(i);
                    createArgs(as.getKeys(), as, test, args);
                }
                
                test.setArgumentArray((Map[]) args.toArray(new Map[args.size()]));
            }
        }
    }
    
    /** Simple matcher */
    static class MethodMatcher {
        
        private static final char WILDCARD = '*';
        
        private int type;
        private String prefix;
        private String suffix;
        
        private MethodMatcher() {
        }
        
        /** Matches given name? */
        public boolean matches(String method) {
            switch (type) {
                case 0: return prefix.equals(method);
                case 1: return method.endsWith(suffix);
                case 2: return method.startsWith(prefix);
                case 3: return method.endsWith(suffix) && method.startsWith(prefix);
                default : return false;
            }
        }
        
        /** Creates new Matcher */
        public static final MethodMatcher create(String pattern) {
            MethodMatcher matcher = new MethodMatcher();
            if (pattern.charAt(0) == WILDCARD) {
                matcher.type = 1;
                matcher.suffix = pattern.substring(1);
            } else if (pattern.charAt(pattern.length() - 1) == WILDCARD) {
                matcher.type = 2;
                matcher.prefix = pattern.substring(0, pattern.length() - 1);
            } else {
                int idx = pattern.indexOf(WILDCARD);
                if (idx < 0) {
                    matcher.type = 0;
                    matcher.prefix = pattern;
                } else {
                    matcher.type = 3;
                    matcher.prefix = pattern.substring(0, idx);
                    matcher.suffix = pattern.substring(idx + 1);
                }
            }
            return matcher;
        }
    }
}
