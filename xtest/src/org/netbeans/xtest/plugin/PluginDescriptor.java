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
    
    // get action for defined plugin with defined ID
    // returns null if the action hasn't been found
    private static Action getAction(Action[] availableActions, String actionID) {
        for (int i=0; i < availableActions.length; i++) {
            if (actionID.equals(availableActions[i].actionID)) {
                return availableActions[i];
            }
        }
        return null;
    }
    
    // get the default action in a set
    // returns null if the default action hasn't been found
    private static Action getDefaultAction(Action[] availableActions) {
        for (int i=0; i < availableActions.length; i++) {
            if (availableActions[i].isDefault()) {
                return availableActions[i];
            }
        }        
        return null;
    }
    
    // has default action ...
    private static boolean hasDefaultAction(Action[] availableActions) {
        for (int i=0; i < availableActions.length; i++) {
            if (availableActions[i].isDefault()) {
                return true;
            }
        }
        return false;
    }
    
    
    // executor stuff !!!    
    // get executor for defined plugin with defined ID
    public Executor getExecutor(String executorID) throws PluginResourceNotFoundException {
        Action action = getAction(availableExecutors, executorID);
        if (action != null) {
            return (Executor)action;
        } else if (hasParentPlugin()) {
            return getParentPlugin().getExecutor(executorID);
        }
        
        throw new PluginResourceNotFoundException("Cannot find executor "+executorID+" for plugin "+getName());        
    }
    
    // get default plugin executor
    public Executor getDefaultExecutor() throws PluginResourceNotFoundException {
        Action action = getDefaultAction(availableExecutors);
        if (action != null) {
            return (Executor)action;
        } else if (hasParentPlugin()) {
            return getParentPlugin().getDefaultExecutor();
        }
        throw new PluginResourceNotFoundException("Cannot find default executor for plugin "+getName());
    }
    
    public boolean hasDefaultExecutor() {
        if (hasDefaultAction(availableExecutors)) {
            return true;
        } else if (hasParentPlugin()) {
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
        Action action = getDefaultAction(availableCompilers);
        if (action != null) {
            return (Compiler)action;
        } else if (hasParentPlugin()) {
            return getParentPlugin().getCompiler(compilerID);
        }        
        throw new PluginResourceNotFoundException("Cannot find compiler "+compilerID+" for plugin "+getName());
    }
    
    // get default plugin executor
    public Compiler getDefaultCompiler() throws PluginResourceNotFoundException {
        Action action = getDefaultAction(availableExecutors);
        if (action != null) {
            return (Compiler)action;
        } else if (hasParentPlugin()) {
            return getParentPlugin().getDefaultCompiler();
        }
        throw new PluginResourceNotFoundException("Cannot find default compiler for plugin "+getName());        
    }
    
    public boolean hasDefaultCompiler() {
        if (hasDefaultAction(availableCompilers)) {
            return true;
        } else if (hasParentPlugin()) {
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
        Action action = getDefaultAction(availableResultProcessors);
        if (action != null) {
            return (ResultProcessor)action;
        } else if (hasParentPlugin()) {
            return getParentPlugin().getResultProcessor(rpID);
        }        
        throw new PluginResourceNotFoundException("Cannot find result processor "+rpID+" for plugin "+getName());
    }
    
    // get default plugin executor
    public ResultProcessor getDefaultResultProcessor() throws PluginResourceNotFoundException {
        Action action = getDefaultAction(availableResultProcessors);
        if (action != null) {
            return (ResultProcessor)action;
        } else if (hasParentPlugin()) {
            return getParentPlugin().getDefaultResultProcessor();
        }
        throw new PluginResourceNotFoundException("Cannot find default result processor for plugin "+getName());
    }
    
    public boolean hasDefaultResultProcessor() {
        if (hasDefaultAction(availableResultProcessors)) {
            return true;
        } else if (hasParentPlugin()) {
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
    
    
    // abstract class as a base for executor/compiler/result processor
    // only have methods/fields - each implementation have to create classMapping registry
    // on its own
    public static abstract class Action implements java.io.Serializable {

        private String actionID;
        private String target;
        private String antFile;
        private boolean defaultAction = false;
        
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
            return defaultAction;
        }
        
        // returns executorID - package private, used only by PluginManager
        String getID() {
            return actionID;
        }        
    }
    
    
    public static class Executor extends Action implements XMLSerializable, java.io.Serializable {
        static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(PluginDescriptor.Executor.class);
        static {
            try {
                // register this class
                classMappingRegistry.registerSimpleField("actionID",ClassMappingRegistry.ATTRIBUTE,"id");
                classMappingRegistry.registerSimpleField("target",ClassMappingRegistry.ATTRIBUTE,"target");
                classMappingRegistry.registerSimpleField("antFile",ClassMappingRegistry.ATTRIBUTE,"antfile");
                classMappingRegistry.registerSimpleField("defaultAction",ClassMappingRegistry.ATTRIBUTE,"default");
                
            } catch (MappingException me) {
                me.printStackTrace();
                classMappingRegistry = null;
            }
        }
        
        public ClassMappingRegistry registerXMLMapping() {
            return classMappingRegistry;
        }    
    }
    
    public static class Compiler extends Action implements XMLSerializable, java.io.Serializable {
        static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(PluginDescriptor.Compiler.class);
        static {
            try {
                // register this class
                classMappingRegistry.registerSimpleField("actionID",ClassMappingRegistry.ATTRIBUTE,"id");
                classMappingRegistry.registerSimpleField("target",ClassMappingRegistry.ATTRIBUTE,"target");
                classMappingRegistry.registerSimpleField("antFile",ClassMappingRegistry.ATTRIBUTE,"antfile");
                classMappingRegistry.registerSimpleField("defaultAction",ClassMappingRegistry.ATTRIBUTE,"default");
                
            } catch (MappingException me) {
                me.printStackTrace();
                classMappingRegistry = null;
            }
        }
        
        public ClassMappingRegistry registerXMLMapping() {
            return classMappingRegistry;
        }        
    }    
    
    public static class ResultProcessor extends Action implements XMLSerializable, java.io.Serializable {
        static ClassMappingRegistry classMappingRegistry = new ClassMappingRegistry(PluginDescriptor.ResultProcessor.class);
        static {
            try {
                // register this class
                classMappingRegistry.registerSimpleField("actionID",ClassMappingRegistry.ATTRIBUTE,"id");
                classMappingRegistry.registerSimpleField("target",ClassMappingRegistry.ATTRIBUTE,"target");
                classMappingRegistry.registerSimpleField("antFile",ClassMappingRegistry.ATTRIBUTE,"antfile");
                classMappingRegistry.registerSimpleField("defaultAction",ClassMappingRegistry.ATTRIBUTE,"default");
                
            } catch (MappingException me) {
                me.printStackTrace();
                classMappingRegistry = null;
            }
        }
        
        public ClassMappingRegistry registerXMLMapping() {
            return classMappingRegistry;
        }   
    }
}
