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
/*
 * NbTestConfig.java
 *
 * Created on March 28, 2001, 2:25 PM
 */

package org.netbeans.xtest;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

import java.util.LinkedList;
import java.util.Hashtable;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.io.File;

/**
 *
 * @author  vs124454
 * @version 
 */
public class NbTestConfig extends Task {
    LinkedList      configs         = new LinkedList();
    String          activeConfig    = null;
    private Vector  allModules = new Vector();
    private Vector  allTests   = new Vector();
    
    private static XConfig xconfig;
    
    private static final PatternSet DEFAULT_PATTERN;
    private static final PatternSet DEFAULT_PATTERN_UNIT;

    static {
        DEFAULT_PATTERN = new PatternSet();
        DEFAULT_PATTERN.setIncludes("**/*.class");
        
        DEFAULT_PATTERN_UNIT = DEFAULT_PATTERN;
    }
    
    public static XConfig getXConfig() {
        return xconfig;
    }
    
    public void setConfig(String config) {
        activeConfig = config;
    }
    
    public Config createConfig() {
        Config c = new Config(project);
        configs.add(c);
        return c;
    }

    public static class PatternSetContainer {
        private PatternSet defaultPatterns = new PatternSet();
        private LinkedList additionalPatterns = new LinkedList();
        protected Project project;
        
        public PatternSetContainer(Project p) {
            this.project = p;
        }
        
        public PatternSet createPatternSet() {
            PatternSet patterns = new PatternSet();
            additionalPatterns.add(patterns);
            return patterns;
        }

        public PatternSet.NameEntry createInclude() {
            return defaultPatterns.createInclude();
        }

        public PatternSet.NameEntry createExclude() {
            return defaultPatterns.createExclude();
        }

        public void setIncludes(String includes) {
            defaultPatterns.setIncludes(includes);
        }

        public void setExcludes(String excludes) {
            defaultPatterns.setExcludes(excludes);
        }

        public void setIncludesfile(File incl) throws BuildException {
            defaultPatterns.setIncludesfile(incl);
        }

        public void setExcludesfile(File excl) throws BuildException {
            defaultPatterns.setExcludesfile(excl);
        }
        
        public PatternSet getPatternSet() {
            PatternSet p = new PatternSet();
            Iterator i = additionalPatterns.iterator();
            
            p.append(defaultPatterns, project);
            while (i.hasNext()) {
                PatternSet src = (PatternSet)i.next();
                p.append(src, project);
            }
            
            return p;
        }
    }
    
    public static class Config extends PatternSetContainer {
        LinkedList  modulesList = new LinkedList();
        LinkedList  testsList = new LinkedList();
        String      name        = null;
        String      modules     = "";
        String      tests       = "";
        
        public Config(Project p) {
            super(p);
        }

        public void setName(String name) {
            this.name = name;
        }
        
        public void setModules(String modules) {
            this.modules = modules;
        }
        
        public void setTestTypes(String tests) {
            this.tests = tests;
        }
        
        public Module createModule() {
            Module m = new Module(project);
            modulesList.add(m);
            return m;
        }
        
        public TestType createTestType() {
            TestType t = new TestType(project);
            testsList.add(t);
            return t;
        }
        
        public static class Module extends PatternSetContainer {
            String  name = null;
            String  tests = null;
            
            public Module(Project p) {
                super(p);
            }

            public void setName(String name) {
                this.name = name;
            }
            
            public void setTestTypes(String tests) {
                this.tests = tests;
            }
        }
        
        public static class TestType extends PatternSetContainer {
            String  name = null;
            String  modules = null;
            
            public TestType(Project p) {
                super(p);
            }
            
            public void setName(String name) {
                this.name = name;
            }
            
            public void setModules(String modules) {
                this.modules = modules;
            }
        }
    }

    public void execute () throws BuildException {
        Hashtable   cfg = new Hashtable();
        Enumeration modules;
        Iterator    i = configs.iterator();
        Config      activeCfg = null;
        
        if (null == activeConfig)
            throw new BuildException("Attribute config has to be set.");
        
        while (i.hasNext()) {
            Config c = (Config)i.next();
            if (activeConfig.equalsIgnoreCase(c.name)) {
                activeCfg = c;
                break;
            }
        }
        
        if (null == activeCfg)
            throw new BuildException("Configuration '" + activeConfig + "' not found.");

        // fill table by module-test specific info
        fillTable(cfg, activeCfg.modules, activeCfg.tests, activeCfg.getPatternSet());
        
        i = activeCfg.modulesList.iterator();
        while(i.hasNext()) {
            Config.Module m = (Config.Module)i.next();
            appendList(allModules, m.name);
            if (null != m.tests)
                updateTableByModule(cfg, m);
        }
        
        i = activeCfg.testsList.iterator();
        while(i.hasNext()) {
            Config.TestType t = (Config.TestType)i.next();
            appendList(allTests, t.name);
            if (null != t.modules)
                updateTableByTest(cfg, t);
        }

        // fill table by module specific info
        i = activeCfg.modulesList.iterator();
        while(i.hasNext()) {
            Config.Module m = (Config.Module)i.next();
            if (null == m.tests)
                updateTableByModule(cfg, m);
        }
        
        // fill table by test specific info
        i = activeCfg.testsList.iterator();
        while(i.hasNext()) {
            Config.TestType t = (Config.TestType)i.next();
            if (null == t.modules)
                updateTableByTest(cfg, t);
        }
        
        // fill table by defaults
        i = cfg.keySet().iterator();
        while (i.hasNext()) {
            Vector v = (Vector)cfg.get(i.next());
            Iterator j = v.iterator();
            while(j.hasNext()) {
                XConfig.Test t = (XConfig.Test)j.next();
                if (null == t.getPattern()) {
                    t.setPattern(getDefaultPattern(t.getType()));
                }
            }
        }

        // set config object
        xconfig = new XConfig(cfg);
    }
    
    private void fillTable(Hashtable tbl, String modules, String tests, PatternSet pattern) {
        StringTokenizer tt = new StringTokenizer(tests, ",");
        String tst [] = new String [tt.countTokens()];
        int i = 0;
        
        while(tt.hasMoreTokens()) {
            String test = tt.nextToken();
            appendList(allTests, test);
            tst[i++] = test;
        }
        
        StringTokenizer tm = new StringTokenizer(modules, ",");
        while(tm.hasMoreTokens()) {
            String module = tm.nextToken();
            appendList(allModules, module);
            for(i = 0; i < tst.length; i++) {
                updateTable(tbl, module, tst[i], pattern);
            }
        }
    }
    
    private void updateTableByModule(Hashtable tbl, Config.Module m) {
        Enumeration t;
        
        if (null == m.tests)
            t = allTests.elements();
        else
            t = new StringTokenizer(m.tests, ",");
        
        while(t.hasMoreElements()) {
            updateTable(tbl, m.name, (String)t.nextElement(), m.getPatternSet());
        }
    }

    private void updateTableByTest(Hashtable tbl, Config.TestType t) {
        Enumeration m;
        
        if (null == t.modules)
            m = allModules.elements();
        else
            m = new StringTokenizer(t.modules, ",");
            
        while(m.hasMoreElements()) {
            updateTable(tbl, (String)m.nextElement(), t.name, t.getPatternSet());
        }
    }
    
    private void updateTable(Hashtable tbl, String module, String test, PatternSet pattern) {
        Vector          v = (Vector)tbl.get(module);
        XConfig.Test    t;
        
        if (null == v) {
            v = new Vector();
            tbl.put(module, v);
        }
        
        t = findTest(v, test);
        
        if (null == t) {
            t = new XConfig.Test(test);
            v.add(t);
        }
        
        if (null == t.getPattern()) {
            if (isPatternSetEmpty(pattern))
                pattern = null;
            
            t.setPattern(pattern);
        }
    }

    private XConfig.Test findTest(Vector tests, String type) {
        Iterator i = tests.iterator();
        while (i.hasNext()) {
            XConfig.Test t = (XConfig.Test)i.next();
            if (type.equals(t.getType())) {
                return t;
            }
        }
        return null;
    }
    
    private boolean isPatternSetEmpty(PatternSet p) {
        return  null == p ||
                ((null == p.getIncludePatterns(project) || 0 == p.getIncludePatterns(project).length) && 
                 (null == p.getExcludePatterns(project) || 0 == p.getExcludePatterns(project).length));
    }
    
    private PatternSet getDefaultPattern(String testtype) {
        PatternSet p = null;
        
        if (testtype.equals("unit"))
            p = DEFAULT_PATTERN_UNIT;
        else
            p = DEFAULT_PATTERN;
        
        return p;
    }
    
    private void appendList(List list, String element) {
        if (!list.contains(element))
            list.add(element);
    }
}
