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
 * TestsActionTask.java
 *
 * Created on August 28, 2003, 5:43 PM
 */

package org.netbeans.xtest.usertasks;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

import org.netbeans.xtest.plugin.*;

/**
 * Abstract class serving as a base for compile/execute tests tasks
 * @author mb115822
 */
public abstract class TestsActionTask extends Task {
    
    protected String actionID;
    
    public void setActionID(String actionID) {
        this.actionID = actionID;
    }
    
    protected abstract PluginDescriptor.Action getSelectedAction(PluginDescriptor pluginDescriptor) throws PluginResourceNotFoundException;
    
    public void execute () throws BuildException {
        PluginManager pluginManager = PluginManagerStorageSupport.retrievePluginManager();
        if (pluginManager == null) {
            // something went wrong !!!!
            throw new BuildException("Fatal error - cannot find plugin manager");
        }        
        // have to somehow get the name of the plugin to be used
        String pluginName = "UNKONWN !!!!";
        // get the appropriate plugin descriptor
        try {
            PluginDescriptor pluginDescriptor = pluginManager.getPreferredPluginDescriptor(pluginName);
            PluginDescriptor.Action action = getSelectedAction(pluginDescriptor);
            PluginExecuteTask.pluginExecute(pluginDescriptor, action, this);
        } catch (PluginResourceNotFoundException prnfe) {
            throw new BuildException("Cannot find plugin resource. Reason: "+prnfe.getMessage(),prnfe);
        }
    }
    
}
