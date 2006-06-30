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
import java.util.*;

/**
 * @author mb115822
 */
public class PluginExecuteTask extends Task {
    
    /* execution types */
    public static final String EXECUTE_COMPILER="compiler";
    public static final String EXECUTE_PACKAGER="packager";
    public static final String EXECUTE_EXECUTOR="executor";
    public static final String EXECUTE_RESULT_PROCESSOR="result_processor";
    
    /* property names */
    public static final String XTEST_PLUGIN_HOME_PROPERTY_NAME="xtest.plugin.home";
    public static final String XTEST_PLUGIN_NAME_PROPERTY_NAME="xtest.plugin.name";
    public static final String XTEST_PLUGIN_VERSION_PROPERTY_NAME="xtest.plugin.version";
    public static final String XTEST_DEPENDING_PLUGIN_HOME_PROPERTY_NAME_PREFIX="xtest.";
    public static final String XTEST_DEPENDING_PLUGIN_HOME_PROPERTY_NAME_SUFFIX=".plugin.home";
    
    // system property name used to store name of the plugin used for test execution
    // required when running result_processor (by default base plugin will be used?)
    private static final String PLUGIN_USED_FOR_EXECUTION = "_xtest.plugin.used.for.test.execution";
    private static final String EXECUTOR_USED_FOR_EXECUTION = "_xtest.plugin.executor.used.for.test.execution";
    
    private String pluginName;
    private String executeType;
    private String actionID;
    
    private Vector properties = new Vector();
    
    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }
    
    public void setExecuteType(String executeType) {
        this.executeType = executeType;
    }
    
    public void setExecuteAction(String actionID) {
        this.actionID = actionID;
    }
    
    public void addProperty(Property property) {
        if (property != null) {
            properties.addElement(property);
        }
    }

    // backw
    /*ard compatibility methods
    public static void pluginExecute(PluginDescriptor pluginDescriptor, 
                PluginDescriptor.Action pluginAction, Task issuingTask) throws BuildException { 
        pluginExecute(pluginDescriptor, pluginAction,issuingTask, null, null);
    }
     **/
    
    // set depending plugins home dir ...
    private static void setPluginDependencyProperties(PluginDescriptor pluginDescriptor, Ant ant) throws BuildException {
        
        PluginDescriptor.PluginDependency dependencies[] = pluginDescriptor.getDependencies();
        for (int i=0; i < dependencies.length; i++) {
            PluginDescriptor.PluginDependency dependency = dependencies[i];
            // set home dir of this dependency
            File dependencyHome = dependency.getPluginDescriptor().getPluginHomeDirectory();
            Property dependencyHomeProperty = ant.createProperty();
            dependencyHomeProperty.setName(XTEST_DEPENDING_PLUGIN_HOME_PROPERTY_NAME_PREFIX
                                            +dependency.getName()
                                            +XTEST_DEPENDING_PLUGIN_HOME_PROPERTY_NAME_SUFFIX);
            dependencyHomeProperty.setValue(dependencyHome.getAbsolutePath());
        }
    }
    
    // method used to execute appropriate result processor (based on executor used to run tests)
    public static void executeCorrespondingResultProcessor(Task issuingTask) throws BuildException {
        // get the plugin name
        String pluginName = System.getProperty(PLUGIN_USED_FOR_EXECUTION);
        String executorID = System.getProperty(EXECUTOR_USED_FOR_EXECUTION);
        if (pluginName == null) {
            throw new BuildException("No tests were executed, cannot run result processor");
        }
        // find the plugin
        PluginManager pluginManager = getPluginManager();
        // find the plugin
        try {
            PluginDescriptor requiredPlugin = pluginManager.getPreferredPluginDescriptor(pluginName);
            PluginDescriptor.Action pluginAction = requiredPlugin.getCorrespondingResultProcessor(executorID);
            pluginExecute(requiredPlugin, pluginAction, issuingTask, null, null);
        } catch (PluginResourceNotFoundException prnfe) {
            // bad thing happened
            throw new BuildException("Fatal error - Cannot find plugin "+pluginName);
        }
        
    }
    
    public static void pluginExecute(PluginDescriptor pluginDescriptor, PluginDescriptor.Action pluginAction,
                                        Task issuingTask, Ant newAnt, Vector properties) throws BuildException {
        
        File pluginHomeDirectory = pluginDescriptor.getActionOwner(pluginAction).getPluginHomeDirectory();
        String antTarget = pluginAction.getTarget();
        
        File antFile = new File(pluginHomeDirectory, pluginAction.getAntFile());
        
        Ant ant = newAnt;
        if (ant == null) {
            ant = new Ant();
        }
        if (issuingTask != null) {
            ant.setProject(issuingTask.getProject());
            ant.setOwningTarget(issuingTask.getOwningTarget());            
        }
        
        // set plugin used for execution system property, so result processor know which plugin was used
        // if applicable 
        if (pluginAction instanceof PluginDescriptor.Executor) {
            System.setProperty(PLUGIN_USED_FOR_EXECUTION, pluginDescriptor.getName());
            System.setProperty(EXECUTOR_USED_FOR_EXECUTION, pluginAction.getID());
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
        
        // now set properties for depending plugins ....
        setPluginDependencyProperties(pluginDescriptor, ant);
        
        // finally set user supplied properties
        if (properties != null) {
            Enumeration en = properties.elements();
            while (en.hasMoreElements()) {
                Property variable = (Property)en.nextElement();
                Property newProperty = ant.createProperty();
                newProperty.setName(variable.getName());
                newProperty.setValue(variable.getValue());
            }
        }
        
        // now execute the target
        ant.execute();      
    }
    
    /// helper to get PluginManager (just throws BuildException if there is any problem)
    private static PluginManager getPluginManager() {
        PluginManager pluginManager = PluginManagerStorageSupport.retrievePluginManager();
        if (pluginManager == null) {
            // something went wrong !!!!
            throw new BuildException("Fatal error - cannot find plugin manager");
        }
        return pluginManager;
    }
    
    public void execute() throws BuildException {
        PluginManager pluginManager = getPluginManager();
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
            } else if (executeType.equalsIgnoreCase(EXECUTE_PACKAGER)) {
                pluginAction=pluginDescriptor.getPackager(actionID); 
            } else if (executeType.equalsIgnoreCase(EXECUTE_EXECUTOR)) {
                pluginAction=pluginDescriptor.getExecutor(actionID); 
            } else if (executeType.equalsIgnoreCase(EXECUTE_RESULT_PROCESSOR)) {
                pluginAction=pluginDescriptor.getResultProcessor(actionID); 
            } else {
                throw new BuildException("executeType "+executeType+" is not supported");
            }
            log("Executing plugin '"+pluginName+"', action '"+actionID+"' (execution type = '"+executeType+"').");
            PluginExecuteTask.pluginExecute(pluginDescriptor, pluginAction, this, null, properties);
            
            
        } catch (PluginResourceNotFoundException prnfe) {
            throw new BuildException("Cannot find plugin resource. Reason: "+prnfe.getMessage(),prnfe);
        }
    }
    
}
