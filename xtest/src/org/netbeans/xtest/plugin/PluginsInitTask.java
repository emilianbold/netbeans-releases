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
 * PluginsInitTask.java
 *
 * Takes care of plugin initialization - this task initializes PluginManager and
 * because of the way how Ant creates tasks (each task has it's own classloader),
 * it stored it as a serialized byte stream in system properties (for usage by other
 * tasks ...)
 *
 *
 * Created on July 22, 2003, 5:25 PM
 */

package org.netbeans.xtest.plugin;


import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

import java.io.*;

/**
 * @author mb115822
 */
public class PluginsInitTask extends Task {
    
    

    
    private File pluginsHome;
    
    public void setPluginsHome(File pluginsHome) {
        this.pluginsHome = pluginsHome;
    }
    
   
    
    
    
    public void execute() throws BuildException {
        // load PluginManager task by system classloader !!!
        /*
        try {
            Class.forName("org.netbeans.xtest.plugin.PluginManager");
            Thread.currentThread().getContextClassLoader().getSystemClassLoader().loadClass("org.netbeans.xtest.plugin.PluginManager");
            AntClassLoader.getSystemClassLoader().loadClass("org.netbeans.xtest.plugin.PluginManager");
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            throw new BuildException("Cannot load PM class!!! ",cnfe);
        }
         **/
        
        // because of the way how Ant treats tasks - PluginManager have to be stored
        // in system properties (which is just a hashtable, so it can store an object
        PluginManager mgr = PluginManagerStorageSupport.retrievePluginManager();
        //System.out.println("MGR is "+mgr);
        if (mgr != null) {
            //System.out.println("Got PluginManager !!!");
            return;
        }
        // 
        log("Creating XTest plugin manager");
        PluginManager pluginManager = PluginManager.getPluginManager();
        // get xtest.home ...
        String xtestHomeProperty = this.getProject().getProperty("xtest.home");
        File xtestHome = new File(xtestHomeProperty);        
        try {            
            
            log("Registering XTest plugins in "+pluginsHome);
            pluginManager.registerPlugins(xtestHome, pluginsHome);
            // ok everything should be initialized - let's store plugin manager in a safe place
            PluginManagerStorageSupport.storePluginManager(pluginManager);            
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new BuildException("Cannot register XTest plugins. Reason: "+ioe.getMessage(),ioe);
        } catch (PluginConfigurationException pce) {
            pce.printStackTrace();
            throw new BuildException("Cannot register XTest plugins. Reason: "+pce.getMessage(),pce);
        }
    }
    
}
