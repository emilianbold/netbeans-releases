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
 * PluginDescriptor.java
 * Class wrapping a plugin configuration 
 * !!! need to add chech of configuration validity
 * Created on July 18, 2003, 12:31 PM
 */

package org.netbeans.xtest.plugin;

import org.netbeans.xtest.xmlserializer.*;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.ArrayList;

/**
 *
 * @author  mb115822
 */
public class PluginDescriptor implements XMLSerializable, Serializable {
    
    static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(PluginDescriptor.class);
    static {
        try {
            // load global registry
            GlobalMappingRegistry.registerClassForElementName("XTestPlugin", PluginDescriptor.class);
            // register this class
            classMappingRegistry.registerSimpleField("name",ClassMappingRegistry.ATTRIBUTE,"name");
            classMappingRegistry.registerSimpleField("version",ClassMappingRegistry.ATTRIBUTE,"version");
            classMappingRegistry.registerSimpleField("parentPluginName",ClassMappingRegistry.ATTRIBUTE,"extends");
            classMappingRegistry.registerSimpleField("dependencies",ClassMappingRegistry.ELEMENT,"Dependencies");
            classMappingRegistry.registerContainerField("availableExecutors", "AvailableExecutors", ClassMappingRegistry.SUBELEMENT);
            classMappingRegistry.registerContainerSubtype("availableExecutors", Executor.class,"Executor");
            classMappingRegistry.registerContainerField("availableCompilers", "AvailableCompilers", ClassMappingRegistry.SUBELEMENT);
            classMappingRegistry.registerContainerSubtype("availableCompilers", Compiler.class,"Compiler");
            classMappingRegistry.registerContainerField("availableResultProcessors", "AvailableResultProcessors", ClassMappingRegistry.SUBELEMENT);
            classMappingRegistry.registerContainerSubtype("availableResultProcessors", ResultProcessor.class,"ResultProcessor");
        } catch (MappingException me) {
            me.printStackTrace();
            classMappingRegistry = null;
        }
    }
    
    public ClassMappingRegistry registerXMLMapping() {
        return classMappingRegistry;
    }            
    /** Creates a new instance of PluginDescriptor */
    public PluginDescriptor() {
    }
    
    // xmlized fields
    private String name;
    private String version;    
    private Executor[] availableExecutors;
    private Compiler[] availableCompilers;
    private ResultProcessor[] availableResultProcessors;
    private Dependencies dependencies;
    private String parentPluginName;
    // dynamic variables - to be initialized later
    private PluginDescriptor parentPlugin;
    /* currently not requried 
    private PluginDescriptor[] childrenPlugins;
    */
    
    // nonxmlized field
    private File pluginHome;
    
    // get plugin home directory
    public File getPluginHomeDirectory() {
       return pluginHome; 
    }
    
    // set plugin home directory (package private is enough)
    void setPluginHomeDirectory(File dir) {
        pluginHome = dir;
    }
    
    // get Plugin name
    public String getName() {
        return name;
    }
    
    // return true if plugin is this plugin
    public boolean isPlugin(String pluginName) {
         return this.name.equalsIgnoreCase(pluginName);
    }
    
    // get plugin version
    public String getVersion() {
        return version;
    }
    
    // get XTest version
    public String getRequiredXTestVersion() {
        return dependencies.requiredXTestVersion;
    }
    
    // get plugin name which is being extended - naming analogy with superclass
    public String getParentPluginName() {
        return parentPluginName;
    }
    
    public PluginDescriptor getParentPlugin() {
        return parentPlugin;
    }
    
    public boolean hasParentPlugin() {
        return ( getParentPlugin() == null)? false : true;
    }
    
    /* currently not required 
    public PluginDescriptor[] getChildrenPlugins() {
        return childrenPlugins;
    }
     **/
       
    
    // is the supplied plugin descendant of this plugin?
    public boolean isAscendant(PluginDescriptor plugin) {
        if (plugin == null) {
            return false;
        }
        if (this == plugin.getParentPlugin()) {
            return true;
        } else {
            return isAscendant(plugin.getParentPlugin());
        }
    }

    
    // executors !!!
    
    // get all executors
    public Executor[] getExecutors() {
        return availableExecutors;
    }
    
    // get executor for defined plugin with defined ID
    public Executor getExecutor(String executorID) throws PluginResourceNotFoundException {
        for (int i=0; i < availableExecutors.length; i++) {
            if (executorID.equals(availableExecutors[i].executorID)) {
                return availableExecutors[i];
            }
        }
        if (hasParentPlugin()) {
            return getParentPlugin().getExecutor(executorID);
        }        
        throw new PluginResourceNotFoundException("Cannot find executor "+executorID+" for plugin "+getName());
    }
    
    // get default plugin executor
    public Executor getDefaultExecutor() throws PluginResourceNotFoundException {
        for (int i=0; i < availableExecutors.length; i++) {
            if (availableExecutors[i].isDefault()) {
                return availableExecutors[i];
            }
        }
        if (hasParentPlugin()) {
            return getParentPlugin().getDefaultExecutor();
        }
        throw new PluginResourceNotFoundException("Cannot find default executor for plugin "+getName());        
    }
    
    public boolean hasDefaultExecutor() {
        for (int i=0; i < availableExecutors.length; i++) {
            if (availableExecutors[i].isDefault()) {
                return true;
            }
        }
        if (hasParentPlugin()) {
            return getParentPlugin().hasDefaultExecutor();
        }
        return false;
    }
    
    
    // compilers !!!
    
    public Compiler[] getCompilers() {
        return availableCompilers;
    }
    
    // get compiler for defined plugin with defined ID
    public Compiler getCompiler(String compilerID) throws PluginResourceNotFoundException {
        for (int i=0; i < availableCompilers.length; i++) {
            if (compilerID.equals(availableCompilers[i].compilerID)) {
                return availableCompilers[i];
            }
        }
        if (hasParentPlugin()) {
            return getParentPlugin().getCompiler(compilerID);
        }        
        throw new PluginResourceNotFoundException("Cannot find compiler "+compilerID+" for plugin "+getName());
    }
    
    // get default plugin executor
    public Compiler getDefaultCompiler() throws PluginResourceNotFoundException {
        for (int i=0; i < availableCompilers.length; i++) {
            if (availableCompilers[i].isDefault()) {
                return availableCompilers[i];
            }
        }
        if (hasParentPlugin()) {
            return getParentPlugin().getDefaultCompiler();
        }
        throw new PluginResourceNotFoundException("Cannot find default compiler for plugin "+getName());        
    }
    
    public boolean hasDefaultCompiler() {
        for (int i=0; i < availableCompilers.length; i++) {
            if (availableCompilers[i].isDefault()) {
                return true;
            }
        }
        if (hasParentPlugin()) {
            return getParentPlugin().hasDefaultCompiler();
        }
        return false;
    }    
    
    
    // result processors
    
    public ResultProcessor[] getResultProcessors() {
        return availableResultProcessors;
    }
    
    // get compiler for defined plugin with defined ID
    public ResultProcessor getResultProcessor(String rpID) throws PluginResourceNotFoundException {
        for (int i=0; i < availableResultProcessors.length; i++) {
            if (rpID.equals(availableResultProcessors[i].rpID)) {
                return availableResultProcessors[i];
            }
        }
        if (hasParentPlugin()) {
            return getParentPlugin().getResultProcessor(rpID);
        }        
        throw new PluginResourceNotFoundException("Cannot find result processor "+rpID+" for plugin "+getName());
    }
    
    // get default plugin executor
    public ResultProcessor getDefaultResultProcessor() throws PluginResourceNotFoundException {
        for (int i=0; i < availableResultProcessors.length; i++) {
            if (availableResultProcessors[i].isDefault()) {
                return availableResultProcessors[i];
            }
        }
        if (hasParentPlugin()) {
            return getParentPlugin().getDefaultResultProcessor();
        }
        throw new PluginResourceNotFoundException("Cannot find default result processor for plugin "+getName());        
    }
    
    public boolean hasDefaultResultProcessor() {
        for (int i=0; i < availableResultProcessors.length; i++) {
            if (availableResultProcessors[i].isDefault()) {
                return true;
            }
        }
        if (hasParentPlugin()) {
            return getParentPlugin().hasDefaultResultProcessor();
        }
        return false;
    }    
    
    
    
    // get plugin dependency
    public PluginDependency[] getDependencies() {
        if (dependencies != null) {
            if (dependencies.pluginDependencies != null) {
                return dependencies.pluginDependencies;
            }
        }
        return new PluginDependency[0];
    }
    
    
    // static method for postInitialization things
    public static void performPostInitialization(PluginDescriptor[] descriptors) throws PluginConfigurationException {
        HashMap globalHashMap = new HashMap(descriptors.length);
        for (int i=0; i < descriptors.length; i++) {
            globalHashMap.put(descriptors[i].getName(), descriptors[i]);
        }
        // now set parent relationship
        for (int i=0; i < descriptors.length; i++) {
            PluginDescriptor descriptor = descriptors[i];
            if (descriptor.getParentPluginName() != null) {
                PluginDescriptor parentPlugin = (PluginDescriptor)globalHashMap.get(descriptor.getParentPluginName());
                if (parentPlugin != null) {
                    descriptor.parentPlugin = parentPlugin;
                } else {
                    // houston, we have a problem - plugin cannot extend a plugin, which is not installed
                    throw new PluginConfigurationException("Plugin "+descriptor.getName()+"cannot extend plugin "
                                    +descriptor.getParentPluginName()+", which is not installed");
                }
            }
        }
        // set children relationship -> this will be more tricky !!!
        
        // currently not required !!!
        /*
        for (int i=0; i < descriptors.length; i++) {
            PluginDescriptor currentDescriptor = descriptors[i];
            ArrayList childrenPlugins = new ArrayList();
            // find all children for this descriptor
            for (int j=0; j < descriptors.length; j++) {
                PluginDescriptor parentPlugin = descriptors[j].getParentPlugin();
                if (currentDescriptor == parentPlugin) {
                   childrenPlugins.add(parentPlugin);                                            
                }
            }
            // create array representation of this
            currentDescriptor.childrenPlugins = (PluginDescriptor[])childrenPlugins.toArray();            
        }
         **/
        // done !!!
    }
    
    
    // public inner classes
    public static class Dependencies implements XMLSerializable, Serializable {
        static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(PluginDescriptor.Dependencies.class);
        static {
            try {
                // register this class
                classMappingRegistry.registerSimpleField("requiredXTestVersion",ClassMappingRegistry.ATTRIBUTE,"requiredXTestVersion");
                classMappingRegistry.registerContainerField("pluginDependencies","PluginDependency",ClassMappingRegistry.SUBELEMENT);
                classMappingRegistry.registerContainerSubtype("pluginDependencies", PluginDescriptor.PluginDependency.class,"PluginDependency");
            } catch (MappingException me) {
                me.printStackTrace();
                classMappingRegistry = null;
            }
        }
        
        public ClassMappingRegistry registerXMLMapping() {
            return classMappingRegistry;
        }        
        
        
        private String requiredXTestVersion;
        private PluginDependency[] pluginDependencies;
    }
    
    public static class PluginDependency implements XMLSerializable, java.io.Serializable {
        static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(PluginDescriptor.PluginDependency.class);
        static {
            try {
                // register this class
                classMappingRegistry.registerSimpleField("name",ClassMappingRegistry.ATTRIBUTE,"name");
                classMappingRegistry.registerSimpleField("version",ClassMappingRegistry.ATTRIBUTE,"version");
                
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
        
        public String getVersion() {
            return version;
        }
        
        private String name;
        private String version;
    }
    
    
    
    public static class Executor implements XMLSerializable, java.io.Serializable {
        static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(PluginDescriptor.Executor.class);
        static {
            try {
                // register this class
                classMappingRegistry.registerSimpleField("executorID",ClassMappingRegistry.ATTRIBUTE,"id");
                classMappingRegistry.registerSimpleField("target",ClassMappingRegistry.ATTRIBUTE,"target");
                classMappingRegistry.registerSimpleField("antFile",ClassMappingRegistry.ATTRIBUTE,"antfile");
                classMappingRegistry.registerSimpleField("defaultExecutor",ClassMappingRegistry.ATTRIBUTE,"default");
                
            } catch (MappingException me) {
                me.printStackTrace();
                classMappingRegistry = null;
            }
        }
        
        public ClassMappingRegistry registerXMLMapping() {
            return classMappingRegistry;
        }   
        
        private String executorID;
        private String target;
        private String antFile;      
        private boolean defaultExecutor = false;
        
        // return target of this executor
        public String getTarget() {
            return target;
        }
        
        // return ant file where this executor is stored
        public String getAntFile() {
            return antFile;
        }
        
        // is this Executor defalt?
        public boolean isDefault() {
            return defaultExecutor;
        }
        
        // returns executorID - package private, used only by PluginManager
        String getExecutorID() {
            return executorID;
        }
    }
    
    public static class Compiler implements XMLSerializable, java.io.Serializable {
        static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(PluginDescriptor.Compiler.class);
        static {
            try {
                // register this class
                classMappingRegistry.registerSimpleField("compilerID",ClassMappingRegistry.ATTRIBUTE,"id");
                classMappingRegistry.registerSimpleField("target",ClassMappingRegistry.ATTRIBUTE,"target");
                classMappingRegistry.registerSimpleField("antFile",ClassMappingRegistry.ATTRIBUTE,"antfile");
                classMappingRegistry.registerSimpleField("defaultCompiler",ClassMappingRegistry.ATTRIBUTE,"default");
                
            } catch (MappingException me) {
                me.printStackTrace();
                classMappingRegistry = null;
            }
        }
        
        public ClassMappingRegistry registerXMLMapping() {
            return classMappingRegistry;
        }   
        
        private String compilerID;
        private String target;
        private String antFile;      
        private boolean defaultCompiler = false;
        
        // return target of this compiler
        public String getTarget() {
            return target;
        }
        
        // return ant file where this compiler is stored
        public String getAntFile() {
            return antFile;
        }
        
        // is this compiler defalt?
        public boolean isDefault() {
            return defaultCompiler;
        }
        
        // returns compilerID - package private, used only by PluginManager
        String getCompilerID() {
            return compilerID;
        }
    }    
    
    public static class ResultProcessor implements XMLSerializable, java.io.Serializable {
        static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(PluginDescriptor.ResultProcessor.class);
        static {
            try {
                // register this class
                classMappingRegistry.registerSimpleField("rpID",ClassMappingRegistry.ATTRIBUTE,"id");
                classMappingRegistry.registerSimpleField("target",ClassMappingRegistry.ATTRIBUTE,"target");
                classMappingRegistry.registerSimpleField("antFile",ClassMappingRegistry.ATTRIBUTE,"antfile");
                classMappingRegistry.registerSimpleField("defaultRP",ClassMappingRegistry.ATTRIBUTE,"default");
                
            } catch (MappingException me) {
                me.printStackTrace();
                classMappingRegistry = null;
            }
        }
        
        public ClassMappingRegistry registerXMLMapping() {
            return classMappingRegistry;
        }   
        
        private String rpID;
        private String target;
        private String antFile;      
        private boolean defaultRP = false;
        
        // return target of this result processor
        public String getTarget() {
            return target;
        }
        
        // return ant file where this result processor is stored
        public String getAntFile() {
            return antFile;
        }
        
        // is this result processor defalt?
        public boolean isDefault() {
            return defaultRP;
        }
        
        // returns result processor ID - package private, used only by PluginManager
        String getRpID() {
            return rpID;
        }
    }        
    
    
}
