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

/**
 * A suite for Benchmarks;
 */
public final class BenchmarkSuite implements Test {
    
    public static final char LIST_START = '[';
    public static final char LIST_END = ']';
    public static final char CLASS_DELIM = ':';
    public static final char LIST_DELIM = '@';
    public static final char ITEM_DELIM = ',';
    public static final String TESTS_SPECS = "tests.specs";
    
    private TestsSpecifications testSpecs;
    private ArrayList benchmarks;
    
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
                parse(spec);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        
        /** parses "mypack.MyTest[testSet*, test*Print]@[KEY1=100, KEY2=2],[KEY1=1000, KEY2=2]:hispack.HisTest" */
        private void parse(String spec) throws ClassNotFoundException {
            if (spec == null || spec.length() == 0) {
                return;
            }
            spec = cleanWhiteSpaces(spec);
            int pos = 0;
            for (;;) {
                int idx = spec.indexOf(CLASS_DELIM, pos);
                String testSpec;
                if (idx < 0) {
                    testSpec = spec.substring(pos);
                    parseOneTest(testSpec);
                    break;
                } else {            
                    testSpec = spec.substring(pos, idx);
                    parseOneTest(testSpec);
                }
                pos = idx + 1;
            }
        }
        
        /** Removes all white spaces */
        private static String cleanWhiteSpaces(String spec) {
            StringBuffer buffer = new StringBuffer(spec.length());
            for (int i = 0; i < spec.length(); i++) {
                char c = spec.charAt(i);
                if (Character.isWhitespace(c)) {
                    continue;
                } else {
                    buffer.append(c);
                }
            }
            
            return buffer.toString();
        }
        
        /** Parses one token of class[methods]@[args] */
        private void parseOneTest(String spec) throws ClassNotFoundException {
            int idx = spec.indexOf(LIST_START);
            String klassName;
            TestSpecification testSpecification;
            if (idx >= 0) {
                if (spec.charAt(idx - 1) == LIST_DELIM) {
                    idx -= 1;
                }
                
                klassName = spec.substring(0, idx);
                testSpecification = new TestSpecification(spec.substring(idx));
            } else {
                klassName = spec;
                testSpecification = new TestSpecification();
            }
            
            testsSpecifications.put(Class.forName(klassName), testSpecification);
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
        private List argLists;
        
        /** New TestsSpecifications */
        public TestSpecification(String spec) {
            init();
            parse(spec);
        }
        
        /** New TestsSpecifications */
        public TestSpecification() {
            init();
        }
        
        /** Parses spec String */
        private void parse(String spec) {
            int idx = spec.indexOf(LIST_DELIM);
            parseMethods(spec.substring(0, idx));
            parseArgs(spec.substring(idx + 1));
        }
        
        /** Parses methods section */
        private void parseMethods(String methods) {
            Enumeration iter = listTokens(methods);
            while (iter.hasMoreElements()) {
                methodMatchers.add(MethodMatcher.create(iter.nextElement().toString()));
            }
        }
        
        /** Parses args section */
        private void parseArgs(String args) {
            Iterator lists = lists(args);
            while (lists.hasNext()) {
                String argList = (String) lists.next();
                Enumeration iter = listTokens(argList);
                List pairs = new ArrayList(20);
                while (iter.hasMoreElements()) {
                    String pair = iter.nextElement().toString();
                    int idx = pair.indexOf('=');
                    String key = pair.substring(0, idx);
                    Object val = convert(pair.substring(idx + 1));
                    pairs.add(new Object[] { key, val });
                }

                argLists.add(pairs);
            }
        }
        
        /** Parses argLists e.g. [KEY1=2,Key2=2],[KEY1=3,KEY3=5] 
         * into [KEY1=2,Key2=2] and [KEY1=3,KEY3=5].
         */
        private static Iterator lists(String argLists) {
            List argsLists = new ArrayList(10);
            int ptr = 0;
            do {
                int listEnd = argLists.indexOf(LIST_END, ptr);
                String token = argLists.substring(ptr, listEnd + 1);
                argsLists.add(token);
                ptr = listEnd + 2;
            } while (ptr < argLists.length());
            
            return argsLists.iterator();
        }
        
        /** Convert to Integer iff possible */
        private static Object convert(String s) {
            try {
                return Integer.valueOf(s);
            } catch (NumberFormatException e) {
                return s;
            }
        }
        
        /** @return tokens */
        private static Enumeration listTokens(String list) {
            return new StringTokenizer(list.substring(1, list.length() - 1), ",");
        }
        
        /** init */
        private void init() {
            methodMatchers = new ArrayList();
            argLists = new ArrayList();
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

        /** Create a Collection of Benchmarks */
        public void setArguments(Benchmark bench) {
            if (bench instanceof MapArgBenchmark) {
                MapArgBenchmark test = (MapArgBenchmark) bench;
                Map[] args = new Map[argLists.size()];
                int argsIdx = 0;
                for (Iterator aiter = argLists.iterator(); aiter.hasNext(); argsIdx++) {
                    List pairs = (List) aiter.next();
                    args[argsIdx] = test.createDefaultMap();
                    for (Iterator piter = pairs.iterator(); piter.hasNext(); ) {
                        Object[] entry = (Object[]) piter.next();
                        args[argsIdx].put(entry[0], entry[1]);
                    }
                }
                test.setArgumentArray(args);
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
