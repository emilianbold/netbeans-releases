/*
 * NbTestConfig.java
 *
 * Created on March 28, 2001, 2:25 PM
 */

package org.netbeans.xtest;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

import java.util.*;

/**
 *
 * @author  vs124454
 * @version 
 */
public class NbTestConfig extends Task {
    LinkedList  configs         = new LinkedList();
    String      propModules     = "xtest.modules";
    String      propTests       = "xtest.testypes";
    String      propPrefix      = "xtest.";
    String      activeConfig    = null;
    String      allModules      = null;
    String      allTests        = null;
    
    public void setModulesProperty(String propModules) {
        this.propModules = propModules;
    }
    
    public void setTestsProperty(String propTests) {
        this.propTests = propTests;
    }
    
    public void setPrefix(String propPrefix) {
        this.propPrefix = propPrefix;
    }

    public void setConfig(String config) {
        activeConfig = config;
    }
    
    public void addConfig(Config c) {
        configs.add(c);
    }
    
    public static class Config {
        LinkedList  modulesList = new LinkedList();
        LinkedList  testsList = new LinkedList();
        String      name        = null;
        String      modules     = "";
        String      tests       = "";
        
        public void setName(String name) {
            this.name = name;
        }
        
        public void setModules(String modules) {
            this.modules = modules;
        }
        
        public void setTestTypes(String tests) {
            this.tests = tests;
        }
        
        public void addModule(Module m) {
            modulesList.add(m);
        }
        
        public void addTestType(TestType t) {
            testsList.add(t);
        }
        
        public static class Module {
            String  name = null;
            String  tests = null;
            
            public void setName(String name) {
                this.name = name;
            }
            
            public void setTestTypes(String tests) {
                this.tests = tests;
            }
        }
        
        public static class TestType {
            String  name = null;
            String  modules = null;
            
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
        
        fillTable(cfg, activeCfg.modules, activeCfg.tests);
        
        i = activeCfg.modulesList.iterator();
        while(i.hasNext()) {
            Config.Module m = (Config.Module)i.next();
            updateTableByModule(cfg, m.name, m.tests);
        }
        
        i = activeCfg.testsList.iterator();
        while(i.hasNext()) {
            Config.TestType t = (Config.TestType)i.next();
            updateTableByTest(cfg, t.name, t.modules);
        }

        // set properties named <prefix><module>
        modules = cfg.keys();
        while(modules.hasMoreElements()) {
            String module = (String)modules.nextElement();
            String tests = (String)cfg.get(module);
            getProject().setProperty(propPrefix + module, tests);
            
            if (null == allModules)
                allModules = module;
            else
                allModules += "," + module;
        }
        
        getProject().setProperty(propModules, allModules);
        getProject().setProperty(propTests, allTests);
    }
    
    private void fillTable(Hashtable tbl, String modules, String tests) {
        StringTokenizer t = new StringTokenizer(modules, ",");
        while(t.hasMoreTokens()) {
            updateTableByModule(tbl, t.nextToken(), tests);
        }
    }
    
    private void updateTableByModule(Hashtable tbl, String module, String tests) {
        if (null == tests || 0 == tests.length())
            return;
        
        StringTokenizer t = new StringTokenizer(tests, ",");
        while(t.hasMoreTokens()) {
            updateTable(tbl, module, t.nextToken());
        }
    }

    private void updateTableByTest(Hashtable tbl, String test, String modules) {
        if (null == modules || 0 == modules.length())
            return;
        
        StringTokenizer t = new StringTokenizer(modules, ",");
        while(t.hasMoreTokens()) {
            updateTable(tbl, t.nextToken(), test);
        }
    }
    
    private void updateTable(Hashtable tbl, String module, String test) {
        String      list = (String)tbl.get(module);
        Iterator    i;

        if (null == allTests)
            allTests = test;
        else {
            if (-1 == allTests.indexOf(test))
                allTests += "," + test;
        }
        
        if (null == list) {
            tbl.put(module, test);
            return;
        }
        
        if (-1 != list.indexOf(test))
            return;
        
        list += ",";
        list += test;
        tbl.put(module, list);
    }
}
