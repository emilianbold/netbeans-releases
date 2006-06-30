/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * Testbag.java
 *
 * Created on 25th March 2003, 15:46
 */

package org.netbeans.xtest.harness;

import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Vector;
import org.netbeans.xtest.xmlserializer.*;

/**
 *
 * @author  lm97939
 */

public class Testbag implements XMLSerializable {

    private String name;
    private String testattribs;
    private String executor;
    private String compiler;

    private String pluginName;

    private TestSetup testSetup;

    private String resultsprocessor;
    private Integer prio;
    private Integer timeout;
    
    private Testset testsets[];
    private TestProperty testProperties[];
    
    private MTestConfig.AntExecType ant_executor;
    private MTestConfig.AntExecType ant_compiler;
    private MTestConfig.AntExecType ant_resultsprocessor;
    
    private MTestConfig parent;
    
    static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(Testbag.class);
    static {
        try {
            // register this class
            classMappingRegistry.registerSimpleField("name",ClassMappingRegistry.ATTRIBUTE,"name");
            classMappingRegistry.registerSimpleField("testattribs",ClassMappingRegistry.ATTRIBUTE,"testattribs");
            classMappingRegistry.registerSimpleField("executor",ClassMappingRegistry.ATTRIBUTE,"executor");
            classMappingRegistry.registerSimpleField("compiler",ClassMappingRegistry.ATTRIBUTE,"compiler");
            classMappingRegistry.registerSimpleField("pluginName",ClassMappingRegistry.ATTRIBUTE,"plugin");
            classMappingRegistry.registerSimpleField("resultsprocessor",ClassMappingRegistry.ATTRIBUTE,"resultsprocessor");
            classMappingRegistry.registerSimpleField("prio",ClassMappingRegistry.ATTRIBUTE,"prio"); 
            classMappingRegistry.registerSimpleField("timeout",ClassMappingRegistry.ATTRIBUTE,"timeout");
            classMappingRegistry.registerContainerField("testsets","testset",ClassMappingRegistry.DIRECT);
            classMappingRegistry.registerContainerField("testProperties","testproperty",ClassMappingRegistry.DIRECT);
            //classMappingRegistry.registerContainerField("setupAndTeardown","setup", ClassMappingRegistry.DIRECT);
            classMappingRegistry.registerSimpleField("testSetup",ClassMappingRegistry.ELEMENT, "testsetup");
            
        } catch (MappingException me) {
            me.printStackTrace();
            classMappingRegistry = null;
        }
    }
    
    public ClassMappingRegistry registerXMLMapping() {
        return classMappingRegistry;
    }

    public String getName() {
        return name;
    }
    
    protected void setParent(MTestConfig p) {
        parent = p;
    }
    
    public MTestConfig getParent() {
        return parent;
    }
    
    protected void setName(String name) {
        this.name = name;
    }
    
    public String getTestattribs() {
        return testattribs;
    }
    
    public Integer getPrio() {
        return prio;
    }
    
    public Integer getTimeout() {
        return timeout;
    }
    
    public TestProperty[] getTestProperties() {
        return testProperties;
    }

    public String getExecutorName() {
        return executor;
    }

    public String getCompilerName() {
        return compiler;
    }

    public String getResultsprocessorName() {
        return resultsprocessor;
    }
    
    public MTestConfig.AntExecType getExecutor() {
        return ant_executor;   
    }
    
    public MTestConfig.AntExecType getCompiler() {
        return ant_compiler;   
    }

    public MTestConfig.AntExecType getResultsprocessor() {
        return ant_resultsprocessor;   
    }
    
    public String getPluginName() {
        return pluginName;
    }
    
    public String getSetupDir() {
        if (testSetup != null) {
            return testSetup.dir;
        }
        return null;
    }
    
    public String getSetUpClassName() {
        if (testSetup != null) {
            if (testSetup.setupDescriptor != null) {
                return testSetup.setupDescriptor.className;
            }
        }
        // else
        return null;
    }
    
    public String getSetUpMethodName() {
        if (testSetup != null) {
             if (testSetup.setupDescriptor != null) {
                return testSetup.setupDescriptor.methodName;
             }
        }
        // else
        return null;        
    }
    
    public String getTearDownClassName() {
        if (testSetup != null) {
            if (testSetup.teardownDescriptor != null) {
                return testSetup.teardownDescriptor.className;
            }
        }
        // else
        return null;        
    }
    
    public String getTearDownMethodName() {
        if (testSetup != null) {
            if (testSetup.teardownDescriptor != null) {
                return testSetup.teardownDescriptor.methodName;
            }
        }
        // else
        return null;        
    }   
    
    public Testset[] getTestsets() {
        return testsets;
    }
    
    public void setTestsets(Testset[] testsets) {
        this.testsets = testsets;
    }
    
    protected void setAntExecutor(MTestConfig.AntExecType type) {
        ant_executor = type;
    }

    protected void setAntCompiler(MTestConfig.AntExecType type) {
        ant_compiler = type;
    }

    protected void setAntResultsprocessor(MTestConfig.AntExecType type) {
        ant_resultsprocessor = type;
    }
    
    protected void validate(HashSet passed_patternset) throws XMLSerializeException {
        if (name == null)
            throw new XMLSerializeException("Attribute name is required for element testbag.");
        if (testattribs == null)
            throw new XMLSerializeException("Attribute testattribs is required for element testbag.");
        if (testsets == null || testsets.length ==0)
            throw new XMLSerializeException("Al least one element testset is required under element testbag.");
        if (timeout != null && timeout.intValue() < 0)
            throw new XMLSerializeException("Value of testbag's attribute timeout can't be negative.");
        if (prio != null && prio.intValue() < 0)
            throw new XMLSerializeException("Value of testbag's attribute prio must be positive.");
        for (int i=0; i<testsets.length; i++) {
            testsets[i].setParent(this);
            testsets[i].validate();   
            testsets[i].filterPatternsets(passed_patternset);   
        }
        if (testProperties != null) {
            for (int i=0; i<testProperties.length; i++)  {
                testProperties[i].validate();
            }
        }
        // validate testsetup
        if (testSetup != null) {
            testSetup.validate();
        }
        // deprecate resultprocessor
        /*
        if (getResultsprocessorName() != null) {
            System.out.println("!!! Attribute resultprocessor is no longer in use. Please don't use it.");
        }
         */
    }

    
    // public inner classes    
    
    public static class TestProperty implements XMLSerializable {
        private String name;
        private String value;

        static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(Testbag.TestProperty.class);
        static {
            try {
                // register this class
                classMappingRegistry.registerSimpleField("name",ClassMappingRegistry.ATTRIBUTE,"name");
                classMappingRegistry.registerSimpleField("value",ClassMappingRegistry.ATTRIBUTE,"value");
            } catch (MappingException me) {
                me.printStackTrace();
                classMappingRegistry = null;
            }
        }

        public ClassMappingRegistry registerXMLMapping() {
            return classMappingRegistry;
        }
        
        public String getName() {
            return name;
        }
        
        public String getValue() {
            return value;
        }
        
        protected void validate() throws XMLSerializeException {
            if (name == null)
                throw new XMLSerializeException("Attribute name is required for element testproperty");
            if (name == null)
                throw new XMLSerializeException("Attribute value is required for element testproperty");
        }
    }
    
    public static class Testset implements XMLSerializable {
        private String dir;
        private Patternset patternsets[];
        private Patternset filtered_patternsets[];
        private Testbag parent;

        static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(Testbag.Testset.class);
        static {
            try {
                // register this class
                classMappingRegistry.registerSimpleField("dir",ClassMappingRegistry.ATTRIBUTE,"dir");
                classMappingRegistry.registerContainerField("patternsets","patternset",ClassMappingRegistry.DIRECT);
            } catch (MappingException me) {
                me.printStackTrace();
                classMappingRegistry = null;
            }
        }

        public ClassMappingRegistry registerXMLMapping() {
            return classMappingRegistry;
        }
        
        public String getDir() {
            return dir;
        }
        
        protected void setDir(String dir) {
            this.dir = dir;
        }
        
        protected void setParent(Testbag p) {
            parent = p;
        }

        public Testbag getParent() {
            return parent;
        }

        
        public Patternset[] getAllPatternset() {
            return patternsets;
        }

        public Patternset[] getPatternset() {
            return filtered_patternsets;
        }

        public void setPatternsets(Patternset[] patternsets) {
            this.patternsets = patternsets;
        }
        
        public String[][] getIncludes() {
           Vector vector = new Vector();
           if (patternsets != null) {
               for (int j=0; j<patternsets.length; j++) {
                   InExclude includes[] = patternsets[j].getIncludes();
                   if (includes != null) {
                       for (int k=0; k<includes.length; k++) {
                           vector.add(new String[] {includes[k].getName(), includes[k].getExpectedFail()});
                       }
                   }
               }
           }
           /*
           System.out.println("include Parent"+getParent());
           System.out.println("include grand Parent"+getParent().getParent());
           System.out.println("include grand Parent additional includes"+getParent().getParent().getAdditionalIncludes());
            */
           if (getParent().getParent().getAdditionalIncludes() != null)
             for (int i=0; i<getParent().getParent().getAdditionalIncludes().length; i++) 
               vector.add(new String[] { getParent().getParent().getAdditionalIncludes()[i], null});
           return (String[][])vector.toArray(new String[0][0]);
        }
    
        public String[][] getExcludes() {
           Vector vector = new Vector();
           if (patternsets != null) {
               for (int j=0; j<patternsets.length; j++) {
                   InExclude excludes[] = patternsets[j].getExcludes();
                   if (excludes != null) {
                       for (int k=0; k<excludes.length; k++) {
                           vector.add(new String[] {excludes[k].getName(), excludes[k].getExpectedFail()});
                       }
                   }
               }
           }
           if (getParent().getParent().getAdditionalExcludes() != null)
             for (int i=0; i<getParent().getParent().getAdditionalExcludes().length; i++)
               vector.add(new String[] { getParent().getParent().getAdditionalExcludes()[i], null});
           return (String[][])vector.toArray(new String[0][0]);
        }    

        protected void validate() throws XMLSerializeException {
            if (dir == null)
                throw new XMLSerializeException("Attribute dir is required for element testset");
            if (patternsets != null) 
                for (int i=0; i<patternsets.length; i++) 
                    patternsets[i].validate();
        }
        
        protected void filterPatternsets(HashSet passed_patternset) {
            if (patternsets != null) {
                if (passed_patternset == null || passed_patternset.isEmpty()) {
                    filtered_patternsets = patternsets;
                } else {
                    Vector patterns = new Vector();
                    for (int i=0; i<patternsets.length; i++) {
                        boolean include = false;
                        if (patternsets[i].getPatternattribs() != null) {
                            StringTokenizer tokenizer = new StringTokenizer(patternsets[i].getPatternattribs(),",");
                            while (tokenizer.hasMoreTokens()) {
                               String attr = tokenizer.nextToken();
                               if (passed_patternset.contains(attr))
                                   include = true;
                               if (passed_patternset.contains("!"+attr)) {
                                   include = false;
                                   break;
                               }
                            }
                            if (include)
                                patterns.add(patternsets[i]);
                        }
                    }
                    filtered_patternsets = (Patternset[])patterns.toArray(new Patternset[0]);
                }
            }
        }
        
    }
    
    
    public static class Patternset implements XMLSerializable {
        private InExclude includes[];
        private InExclude excludes[];
        private String patternattribs;

        static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(Testbag.Patternset.class);
        static {
            try {
                // register this class
                classMappingRegistry.registerSimpleField("patternattribs",ClassMappingRegistry.ATTRIBUTE,"attribs");
                classMappingRegistry.registerContainerField("includes","include",ClassMappingRegistry.DIRECT);
                classMappingRegistry.registerContainerField("excludes","exclude",ClassMappingRegistry.DIRECT);
            } catch (MappingException me) {
                me.printStackTrace();
                classMappingRegistry = null;
            }
        }

        public ClassMappingRegistry registerXMLMapping() {
            return classMappingRegistry;
        }
        
        public InExclude[] getIncludes() {
            return includes;
        }
        
        public void setIncludes(InExclude[] includes) {
            this.includes = includes;
        }
        
        public InExclude[] getExcludes() {
            return excludes;
        }
        
        public void setExcludes(InExclude[] excludes) {
            this.excludes = excludes;
        }

        public String getPatternattribs() {
            return patternattribs;
        }
        
        protected void validate() throws XMLSerializeException {
            if (includes != null) 
               for (int i=0; i<includes.length; i++) 
                    includes[i].validate();
            if (excludes != null) 
               for (int i=0; i<excludes.length; i++) 
                    excludes[i].validate();
        }
    }
    
    public static class InExclude implements XMLSerializable {
        private String name;
        private String expectedFail;

        static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(Testbag.InExclude.class);
        static {
            try {
                // register this class
                classMappingRegistry.registerSimpleField("expectedFail",ClassMappingRegistry.ATTRIBUTE,"expectedFail");
                classMappingRegistry.registerSimpleField("name",ClassMappingRegistry.ATTRIBUTE,"name");
            } catch (MappingException me) {
                me.printStackTrace();
                classMappingRegistry = null;
            }
        }

        public ClassMappingRegistry registerXMLMapping() {
            return classMappingRegistry;
        }
        
        public String getName() {
            return name;
        }
        
        public String getExpectedFail() {
            return expectedFail;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        protected void validate() throws XMLSerializeException {
            if (name == null)
                throw new XMLSerializeException("Attribute name is required for element include/exclude.");
        }
        
    }
    
    // TestBag setup inner class
    public static class TestSetup implements XMLSerializable {
        
        static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(Testbag.TestSetup.class);
        static {
            try {
                // register this class
                classMappingRegistry.registerSimpleField("dir",ClassMappingRegistry.ATTRIBUTE,"dir");
                classMappingRegistry.registerSimpleField("setupDescriptor",ClassMappingRegistry.ELEMENT,"setup");
                classMappingRegistry.registerSimpleField("teardownDescriptor",ClassMappingRegistry.ELEMENT,"teardown");
            } catch (MappingException me) {
                me.printStackTrace();
                classMappingRegistry = null;
            }
        }
        
        public ClassMappingRegistry registerXMLMapping() {
            return classMappingRegistry;
        }
        
        private String dir;
        private SetupDescriptor setupDescriptor;
        private SetupDescriptor teardownDescriptor;
        
        protected void validate() throws XMLSerializeException {
            if ((dir == null)) {
                throw new XMLSerializeException("When using testsetup, dir attribute have to be defined."); 
            }
            if ((setupDescriptor == null) && (teardownDescriptor == null)) {
                throw new XMLSerializeException("Testsetup have to contain either setup or teardown elements."); 
            }
            if (setupDescriptor != null) {
                setupDescriptor.validate();
            }
            if (teardownDescriptor != null) {
                teardownDescriptor.validate();
            }
        }
    }
    
    public static class SetupDescriptor implements XMLSerializable {
        
        static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(Testbag.SetupDescriptor.class);
        
        static {
            try {
                classMappingRegistry.registerSimpleField("className",ClassMappingRegistry.ATTRIBUTE,"class");
                classMappingRegistry.registerSimpleField("methodName",ClassMappingRegistry.ATTRIBUTE,"method");
            } catch (MappingException me) {
                me.printStackTrace();
                classMappingRegistry = null;
            }
        }
        public ClassMappingRegistry registerXMLMapping() {
            return classMappingRegistry;
        }
        
        private String className;
        private String methodName;
        
        protected void validate() throws XMLSerializeException {
            if ((className == null) || (methodName == null))
                throw new XMLSerializeException("class and method attributes are required for setup/teardown.");
        }        
    }
}
