/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * MTestConfig.java
 *
 * Created on March 25, 2003, 2:36 PM
 */

package org.netbeans.xtest.harness;

import org.netbeans.xtest.xmlserializer.*;
import java.io.*;
import java.util.Hashtable;
import org.netbeans.xtest.pe.server.*;
import org.netbeans.xtest.pe.*;
import java.util.logging.Level;
import org.w3c.dom.Document;

/**
 *
 * @author  mb115822
 */
public class MTestConfig implements XMLSerializable {
    
    private String name;
    private Testbag testbags[];
    private AntExecType executors[];
    private AntExecType compilers[];
    private AntExecType resultsprocessors[];
    
    private AntExecType defaultExecutor;
    private AntExecType defaultCompiler;
    private AntExecType defaultResultsprocessor;
    
    private Hashtable executors_table = new Hashtable();
    private Hashtable compilers_table = new Hashtable();
    private Hashtable resultsprocessors_table = new Hashtable();
    
    static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(MTestConfig.class);
    static {
        try {
            // load global registry
            GlobalMappingRegistry.registerClassForElementName("mconfig", MTestConfig.class);
            // register this classMTestConfig
            classMappingRegistry.registerSimpleField("name",ClassMappingRegistry.ATTRIBUTE,"name");
            classMappingRegistry.registerContainerField("testbags","testbag",ClassMappingRegistry.DIRECT);
            classMappingRegistry.registerContainerField("executors","executor",ClassMappingRegistry.DIRECT);
            classMappingRegistry.registerContainerField("compilers","compiler",ClassMappingRegistry.DIRECT);
            classMappingRegistry.registerContainerField("resultsprocessors","resultsprocessor",ClassMappingRegistry.DIRECT);
        } catch (MappingException me) {
            me.printStackTrace();
            classMappingRegistry = null;
        }
    }
    
    public ClassMappingRegistry registerXMLMapping() {
        return classMappingRegistry;
    }        
    
    // empty constructor - required by XMLSerializer
    public MTestConfig() {}
    
    public String getName() {
        return name;
    }  
    
    public Testbag[] getTestbags() {
        return testbags;
    }

    
    public static MTestConfig loadConfig(File configFile) throws XMLSerializeException {        
        if (!configFile.isFile()) {
            throw new XMLSerializeException("Cannot load config from file "+configFile.getPath()+", file does not exist");
        }
        try {
            Document doc = SerializeDOM.parseFile(configFile);
            XMLSerializable xmlObject = XMLSerializer.getXMLSerializable(doc);
            if (xmlObject instanceof MTestConfig) {
                MTestConfig config = (MTestConfig)xmlObject;
                config.validateAndSetup();
                return config;
            }
        } catch (IOException ioe) {
            throw new XMLSerializeException("IOException caught when loading config file:"+configFile.getPath(),ioe);
        }
        // xmlobject is not of required type
        throw new XMLSerializeException("Loaded xml document is not MTestConfig");
    }
    
    private void validateAndSetup() throws XMLSerializeException {
        if (getName() == null)
            throw new XMLSerializeException("Attribute name is required for root element mconfig.");

        if (executors == null || executors.length == 0)
            throw new XMLSerializeException("At least one element executor is required.");
        if (compilers == null || compilers.length == 0)
            throw new XMLSerializeException("At least one element compiler is required.");
        if (resultsprocessors == null || resultsprocessors.length == 0)
            throw new XMLSerializeException("At least one element resultsprocessor is required.");
        
        if (testbags == null || testbags.length == 0)
            throw new XMLSerializeException("At least one element testbag is required.");
        
        defaultExecutor = processAntExecTypes(executors, executors_table, defaultExecutor, "executor");
        defaultCompiler = processAntExecTypes(compilers, compilers_table, defaultCompiler, "compiler");
        defaultResultsprocessor = processAntExecTypes(resultsprocessors, resultsprocessors_table, defaultResultsprocessor, "resultsprocessor");

        for (int i=0; i<testbags.length; i++) {
            testbags[i].setAntExecutor(getTestbagAntExecType(testbags[i].getExecutorName(), defaultExecutor, executors_table, "executor"));
            testbags[i].setAntCompiler(getTestbagAntExecType(testbags[i].getCompilerName(), defaultCompiler, compilers_table, "compiler"));            
            testbags[i].setAntResultsprocessor(getTestbagAntExecType(testbags[i].getResultsprocessorName(), defaultResultsprocessor, resultsprocessors_table, "resultsprocessor"));
            testbags[i].validate();
        }
        
    }
 
    /** Validate all AntExecTypes, find ones with duplicate names and create hastable where key is name.
     */
    private AntExecType processAntExecTypes(AntExecType types[], Hashtable types_table, AntExecType defaultType, String name) throws XMLSerializeException {
        for (int i=0; i<types.length; i++) {
            types[i].validate();
            AntExecType duplicate = (AntExecType)types_table.get(types[i].getName());
            if (duplicate != null) 
                throw new XMLSerializeException("Name of "+name+" must be unique. Found duplicate name "+types[i].getName()+".");
            types_table.put(types[i].getName(), types[i]);
            if (types[i].isDefault()) {
                if (defaultType != null)
                    throw new XMLSerializeException("Only one "+name+" can be set as default.");
                defaultType = types[i];
            }
        }
        return defaultType;
    }
    
    /** Find AntExecType for given type_name or return default i type_name is null.
     */
    private AntExecType getTestbagAntExecType(String type_name, AntExecType defaultType, Hashtable types_table, String name) throws XMLSerializeException {
        if (type_name == null) {
            if (defaultType == null)
                throw new XMLSerializeException("No default "+name+" was found.");
            else 
                return defaultType;
        } else {
            AntExecType type = (AntExecType)types_table.get(type_name);
            if (type == null)
                throw new XMLSerializeException("No "+name+" with name "+type_name+" was found.");    
            else
                return type;
        }
    }
    
    // public inner classes    
    
    public static class AntExecType implements XMLSerializable {
        private String name;
        private String antfile;
        private String dir;
        private String target;
        private String def;

        static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(MTestConfig.AntExecType.class);
        static {
            try {
                // register this class
                classMappingRegistry.registerSimpleField("name",ClassMappingRegistry.ATTRIBUTE,"name");
                classMappingRegistry.registerSimpleField("antfile",ClassMappingRegistry.ATTRIBUTE,"antfile");
                classMappingRegistry.registerSimpleField("dir",ClassMappingRegistry.ATTRIBUTE,"dir");
                classMappingRegistry.registerSimpleField("target",ClassMappingRegistry.ATTRIBUTE,"target");
                classMappingRegistry.registerSimpleField("def",ClassMappingRegistry.ATTRIBUTE,"default");
            } catch (MappingException me) {
                me.printStackTrace();
                classMappingRegistry = null;
            }
        }

        public ClassMappingRegistry registerXMLMapping() {
            return classMappingRegistry;
        }
        
        protected void validate() throws XMLSerializeException {
            if (name == null)
                throw new XMLSerializeException("Attribute name is required for element executor/compiler/resultsprocessor.");
            if (def != null && !(def.equalsIgnoreCase("false") || def.equalsIgnoreCase("no") || def.equalsIgnoreCase("0") ||
                                 def.equalsIgnoreCase("true") || def.equalsIgnoreCase("yes") || def.equalsIgnoreCase("1")))
                throw new XMLSerializeException("Invalid value "+def+" in attribute default in element executor/compiler/resultsprocessor. Valid values are true, false, yes, no, 0 or 1.");
        }
        
        public boolean isDefault() {
            if (def != null && (def.equalsIgnoreCase("true") || def.equalsIgnoreCase("yes") || def.equalsIgnoreCase("1")))
                return true;
            return false;
        }
        
        public String getName() {
            return name;
        }
    }
    
    
    public static void main(String[] args) throws Exception {
        MTestConfig mconfig = loadConfig(new File("D:\\cvs\\nb_all\\xtest\\src\\org\\netbeans\\xtest\\harness\\cfg.xml"));
        System.out.println("Name="+mconfig.getName());
        Testbag testbags[] = mconfig.getTestbags();
        if (testbags != null)
            for (int i=0; i<testbags.length;i++) {
                System.out.println("Testbags["+i+"].name="+testbags[i].getName());
                System.out.println("Testbags["+i+"].prio="+testbags[i].getPrio());
            }
        AntExecType executors[] = mconfig.executors;
        if (executors != null)
            for (int i=0; i<executors.length;i++)
                System.out.println("Executors["+i+"].name="+executors[i].getName());
    }
    
}
