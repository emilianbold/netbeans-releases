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
 * PluginExecutorTask.java
 * This task server for execution of executors (and soon also compilers/result processors)
 * supplied by plugins
 * The task simply executes supplied ant target with additional properties (xtest.plugin.name,
 *   xtest.plugin.home, xtest.plugin.version)
 *   for plugin developers).
 *
 * 
 *
 * Created on July 22, 2003, 5:25 PM
 */

package org.netbeans.xtest.plugin;


import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.taskdefs.Property;
import java.io.File;


/**
 * @author mb115822
 */
public class PluginExecuteTask extends Task {
    
    /* execution types */
    public static final String EXECUTE_COMPILER="compiler";
    public static final String EXECUTE_EXECUTOR="executor";
    public static final String EXECUTE_RESULT_PROCESSOR="result_processor";
    
    /* property names */
    public static final String XTEST_PLUGIN_HOME_PROPERTY_NAME="xtest.plugin.home";
    public static final String XTEST_PLUGIN_NAME_PROPERTY_NAME="xtest.plugin.name";
    public static final String XTEST_PLUGIN_VERSION_PROPERTY_NAME="xtest.plugin.version";
    
    private String pluginName;
    private String executeType;
    private String actionID;
    
    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }
    
    public void setExecuteType(String executeType) {
        this.executeType = executeType;
    }
    
    public void setExecuteAction(String actionID) {
        this.actionID = actionID;
    }    

    // backward compatibility methods
    public static void pluginExecute(PluginDescriptor pluginDescriptor, PluginDescriptor.Action pluginAction, Task issuingTask) throws BuildException { 
        pluginExecute(pluginDescriptor, pluginAction,issuingTask, null);
    }
    
    public static void pluginExecute(PluginDescriptor pluginDescriptor, PluginDescriptor.Action pluginAction,
                                        Task issuingTask, Ant newAnt) throws BuildException {
        
        File pluginHomeDirectory = pluginDescriptor.getPluginHomeDirectory();
        String antTarget = pluginAction.getTarget();
        
        File antFile = new File(pluginHomeDirectory, pluginAction.getAntFile());        
        
        Ant ant = newAnt;
        if (ant == null) {
            ant = new Ant();
        }
        if (issuingTask != null) {
            ant.setProject(issuingTask.getProject());
            ant.setOwningTarget(issuingTask.getOwningTarget());
            issuingTask.log("Plugin will execute target '"+antTarget+"' in file '"+antFile+"'.");
        }
        
        ant.setAntfile(antFile.getAbsolutePath());
        ant.setTarget(antTarget);
        
        Property xtestPluginHome = ant.createProperty();
        xtestPluginHome.setName(XTEST_PLUGIN_HOME_PROPERTY_NAME);
        xtestPluginHome.setValue(pluginHomeDirectory.getAbsolutePath());
        
        Property xtestPluginName = ant.createProperty();
        xtestPluginName.setName(XTEST_PLUGIN_NAME_PROPERTY_NAME);
        xtestPluginName.setValue(pluginDescriptor.getName());
        
        Property xtestPluginVersion = ant.createProperty();
        xtestPluginVersion.setName(XTEST_PLUGIN_VERSION_PROPERTY_NAME);
        xtestPluginVersion.setValue(pluginDescriptor.getVersion());
        
        // now execute the target
        ant.execute();      
    }
    
    public void execute() throws BuildException {
        PluginManager pluginManager = PluginManagerStorageSupport.retrievePluginManager();
        if (pluginManager == null) {
            // something went wrong !!!!
            throw new BuildException("Fatal error - cannot find plugin manager");
        }
        // check for supplied properties
        if (pluginName == null) {
            throw new BuildException("pluginName attribute not specified");
        }
        if (executeType == null) {
            throw new BuildException("executeType attribute not specified");
        }
        if (actionID == null) {
            throw new BuildException("executeAction attribute not specified");
        }
        try {
            // plugin manager found - let's execute the stuff
            PluginDescriptor pluginDescriptor = pluginManager.getPreferredPluginDescriptor(pluginName);
            PluginDescriptor.Action pluginAction = null;
            // depending on execute type get appropriate action
            if (executeType.equalsIgnoreCase(EXECUTE_COMPILER)) {
                pluginAction=pluginDescriptor.getCompiler(actionID);
            } else if (executeType.equalsIgnoreCase(EXECUTE_EXECUTOR)) {
                pluginAction=pluginDescriptor.getExecutor(actionID); 
            } else if (executeType.equalsIgnoreCase(EXECUTE_RESULT_PROCESSOR)) {
                pluginAction=pluginDescriptor.getResultProcessor(actionID); 
            } else {
                throw new BuildException("executeType "+executeType+" is not supported");
            }
            
            PluginExecuteTask.pluginExecute(pluginDescriptor, pluginAction, this);
            
            
        } catch (PluginResourceNotFoundException prnfe) {
            throw new BuildException("Cannot find plugin resource. Reason: "+prnfe.getMessage(),prnfe);
        }
    }
    
}
