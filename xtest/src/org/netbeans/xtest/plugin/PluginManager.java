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
 * PluginManager.java
 * 
 * Class used for registering plugins and checking their dependencies. This
 * class is serializable, because it have to used from several ant tasks and 
 * contain the same date without requirement to prepare it each time.
 *
 * Created on July 18, 2003, 12:27 PM
 */

package org.netbeans.xtest.plugin;

import java.io.*;
import java.util.*;
import org.netbeans.xtest.util.FileUtils;
import org.netbeans.xtest.util.SerializeDOM;
import org.netbeans.xtest.xmlserializer.*;
import org.netbeans.xtest.XTestVersion;
import java.util.jar.Manifest;
/**
 *
 * @author  mb115822
 */
public class PluginManager implements java.io.Serializable {
    
    private static final String PLUGIN_DESCRIPTOR_FILENAME = "plugin_descriptor.xml";
    
    
    // singleton instance
    private static PluginManager pluginManager;
    
    // private constructor
    private PluginManager() {
        try {
            Class.forName("org.netbeans.xtest.plugin.PluginDescriptor");
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Cannot load  org.netbeans.xtest.plugin.PluginDescriptor class, Reason = "+cnfe);
        }
    }
    
    /** Creates a new instance of PluginManager */
    public static PluginManager getPluginManager()  {        
        if (pluginManager == null) {
            pluginManager = new PluginManager();
        }
        return pluginManager;
    }
    
    
    private File pluginsHome;    
    private PluginDescriptor[] pluginDescriptors;
    private PluginDescriptor[] preferredPlugins = new PluginDescriptor[0];
    private HashMap pluginsHashMap;
    
    
    // registers plugins
    public void registerPlugins(File xtestHome, File pluginsHome) throws IOException, PluginConfigurationException {
        if (pluginsHome == null) {
            throw new NullPointerException("agument cannot be null");
        }
        if (!pluginsHome.isDirectory()) {
            throw new IOException("argument is not a valid directory");
        }
        this.pluginsHome = pluginsHome;
        
        
        // now get the descriptors and 
        pluginDescriptors = getPluginDescriptors();
        

        // get XTest's version
        Manifest mf = XTestVersion.getManifest(xtestHome.getAbsolutePath());
        String xtestVersion = XTestVersion.getMajorVersion(mf)+"."+XTestVersion.getMinorVersion(mf);     
        
        // check them for compatibility/dependency        
        checkForPluginCompatibility(pluginDescriptors, xtestVersion);
        
        // performPostInitialization
        PluginDescriptor.performPostInitialization(pluginDescriptors);
        
        // create quick access hashamp
        pluginsHashMap = createQuickAccessHashMap(pluginDescriptors);        
    }
    
    // register preferred plugins
    public void registerPreferredPlugins(String[] preferredPluginNames) throws PluginConfigurationException {
        
        if (preferredPluginNames != null) {
            PluginDescriptor[] preferredPluginDescriptors = new PluginDescriptor[preferredPluginNames.length];
            for (int i=0; i < preferredPluginNames.length; i++) {
                try {
                    preferredPluginDescriptors[i] = findPluginDescriptor(preferredPluginNames[i]);
                } catch (PluginResourceNotFoundException prnfe) {
                    throw new PluginConfigurationException("Cannot register preferred plugin "+preferredPluginNames[i]
                        +", because:"+prnfe.getMessage(),prnfe);
                }
            }
            // everything is done 
            this.preferredPlugins = preferredPluginDescriptors;
        }
    }
    
    private static HashMap createQuickAccessHashMap(PluginDescriptor[] descriptors) {
        // create hashmap for quick access by plugin name
        HashMap pluginsHashMap = new HashMap(descriptors.length);
        for (int i=0; i < descriptors.length; i++) {
            // it is in lower case !!!!
            pluginsHashMap.put(descriptors[i].getName().toLowerCase(), descriptors[i]);
        }
        return pluginsHashMap;
    }
    
    
    
    // check whether plugins are registered
    private void checkForPluginRegistration() throws PluginResourceNotFoundException {
        if ((pluginDescriptors == null)|(pluginsHashMap == null)) {
            throw new PluginResourceNotFoundException("Plugins were not registered");
        }
    }
    
    // find the pluginDescriptor - uses pluginsHashMap for faster access
    private PluginDescriptor findPluginDescriptor(String pluginName) throws PluginResourceNotFoundException {
        checkForPluginRegistration();
        PluginDescriptor descriptor = (PluginDescriptor)pluginsHashMap.get(pluginName.toLowerCase());
        if (descriptor != null) {
            return descriptor;
        } else {
            throw new PluginResourceNotFoundException("Plugin '"+pluginName+"' not found. Please install it.");
        }
    }
    
    
    // get preferred plugin for the base plugin (if available)
    public PluginDescriptor getPreferredPluginDescriptor(String basePluginName) throws PluginResourceNotFoundException {
        PluginDescriptor basePlugin = findPluginDescriptor(basePluginName);
        // now for each preferred plugin check whether is descendant
        for (int i=0; i < preferredPlugins.length; i++) {
            if (basePlugin.isAscendant(preferredPlugins[i])) {
                return preferredPlugins[i];
            }
        }        
        return basePlugin;
    }
    
    // get plugin version
    public String getPluginVersion(String pluginName) throws PluginResourceNotFoundException {
        return findPluginDescriptor(pluginName).getVersion();
    }
    
    // get plugin home directory
    public File getPluginHomeDirectory(String pluginName) throws PluginResourceNotFoundException {
        return findPluginDescriptor(pluginName).getPluginHomeDirectory();
    }
    
    // get plugin executor - describes ant file ant target to be executed
    /* not required !!!
    public PluginDescriptor.Executor getPluginExecutor(String pluginName, String executorID) throws PluginResourceNotFoundException {
        return findPluginDescriptor(pluginName).getPluginExecutor(executorID);
    }
     **/
    
    protected PluginDescriptor[] getPluginDescriptors() throws PluginConfigurationException {
        // in each subdirectory of pluginsHome get plugin_descriptor.xml
        File[] subDirs = FileUtils.listSubdirectories(pluginsHome);
        ArrayList descriptors = new ArrayList();
        for (int i=0; i < subDirs.length; i++) {
            try {
                File pluginDescriptorFile = new File(subDirs[i],PLUGIN_DESCRIPTOR_FILENAME);
                // #58189 - ignore system dirs like CVS, .svn which can exist when XTest binaries are in versioned directories
                if(pluginDescriptorFile.exists()) {
                    XMLSerializable xmlSerializable = XMLSerializer.getXMLSerializable(SerializeDOM.parseFile(pluginDescriptorFile));
                    if (xmlSerializable instanceof PluginDescriptor) {
                        ((PluginDescriptor)xmlSerializable).setPluginHomeDirectory(subDirs[i]);
                        descriptors.add(xmlSerializable);
                    } else {
                        throw new PluginConfigurationException("Directory "+subDirs[i].getPath()+" does not contain a valid XTest Plugin");
                    }
                }
            } catch (XMLSerializeException xse) {
                throw new PluginConfigurationException("Unable to parse "+subDirs[i].getPath()+File.separator+PLUGIN_DESCRIPTOR_FILENAME+" plugin configuration file",xse);
            } catch (IOException ioe) {
                throw new PluginConfigurationException("Unable to load "+subDirs[i].getPath()+File.separator+PLUGIN_DESCRIPTOR_FILENAME+" plugin configuration file",ioe);
            }
        }
        return (PluginDescriptor[])descriptors.toArray(new PluginDescriptor[0]);
    }
    
    
    private static void checkForPluginCompatibility(PluginDescriptor[] descriptors,  String xtestVersion) throws PluginConfigurationException {
        for (int i=0; i < descriptors.length; i++) {
            PluginDescriptor descriptor = descriptors[i];
            // check whether it is compatible with this XTest
            if (xtestVersion.compareTo(descriptor.getRequiredXTestVersion()) < 0) {
                // plugin is not compatible
                throw new PluginConfigurationException("Plugin "+descriptor.getName()+" version "+descriptor.getVersion()
                    +" requires XTest version at least "+descriptor.getRequiredXTestVersion()+". Current XTest version is "+xtestVersion);
            }
            // check whether dependency is compatible with other plugins
            checkPluginDependency(descriptor, descriptors);
        }
    }
    
    private static void checkPluginDependency(PluginDescriptor checkedPlugin, PluginDescriptor[] installedPlugins) throws PluginConfigurationException {
        // for each dependency whether the plugin is intalled
        // check also if the plugin extends another plugin if it depends on it

        for (int i=0; i < checkedPlugin.getDependencies().length; i++) {
            PluginDescriptor.PluginDependency pluginDependency = checkedPlugin.getDependencies()[i];
            String requiredPluginName = pluginDependency.getName();
            String requiredPluginVersion = pluginDependency.getVersion();            
            // check whether required plugin exists in installed Plugin            
            boolean dependencyOK = false;
            for (int j = 0; j < installedPlugins.length; j++) {
                if (installedPlugins[j].isPlugin(requiredPluginName)) {
                    // check the version
                    if (requiredPluginVersion.compareTo(installedPlugins[j].getVersion()) > 0)  {
                        // not compatible
                        throw new PluginConfigurationException("Plugin "+checkedPlugin.getName()+" version "+checkedPlugin.getVersion()
                            +" requires Plugin "+requiredPluginName+" version "+requiredPluginVersion
                            +". Installed version is "+installedPlugins[j].getVersion());
                    } else {
                        dependencyOK = true;
                        break;
                    }
                }
            }
            if (!dependencyOK) {
                // plugin not found
                throw new PluginConfigurationException("Plugin "+checkedPlugin.getName()+" version "+checkedPlugin.getVersion()
                            +" requires Plugin "+requiredPluginName+" version "+requiredPluginVersion
                            +", which is not installed");
            }
        }
        // extension have to be checked more thoroughly
        //  - executors supplied by the extension have to be superset of its parent
        /* should not be required - postInitialization should perform it bu itself 
        if (checkedPlugin.getParentPlugin() != null) {
            boolean extensionOK = false;
            PluginDescriptor parentPlugin = checkedPlugin.getParentPlugin();
            PluginDescriptor.Executor[] parentExecutors = parentPlugin.getPluginExecutors();
            for (int i = 0; i < parentExecutors.length; i++) {
                String parentExecutorID = parentExecutors[i].getExecutorID();
                try {
                    checkedPlugin.getPluginExecutor(parentExecutorID);
                } catch (PluginResourceNotFoundException prnfe) {
                    //  we have a problem
                    throw new PluginConfigurationException("Plugin "+checkedPlugin.getName()
                        +" extends "+parentPlugin.getClass()+", but does not implement executor "
                        +parentExecutorID);
                }
            }
            // also check the default executor - they have to be equal !!!
            if (!checkedPlugin.getPluginDefaultExecutor().equals(parentPlugin.getPluginDefaultExecutor())) {
                throw new PluginConfigurationException("Plugin "+checkedPlugin.getName()
                        +" extends "+parentPlugin.getClass()+", but does have the same default executor as parent");
            }
        }
        */
        /*
        if (!extensionOK) {
            // plugin to extend not found
            throw new PluginConfigurationException("Plugin "+checkedPlugin.getName()+" version "+checkedPlugin.getVersion()
                +" extends plugin "+checkedPlugin.getParentPluginName()+", which is not included in dependecies!!!");
        }
         */
    }
    
}
