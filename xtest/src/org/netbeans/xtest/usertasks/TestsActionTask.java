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
 * TestsActionTask.java
 *
 * Created on August 28, 2003, 5:43 PM
 */

package org.netbeans.xtest.usertasks;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.taskdefs.*;
import org.netbeans.xtest.plugin.*;
import java.util.*;

/**
 * Abstract class serving as a base for compile/execute tests tasks
 * @author mb115822
 */
public abstract class TestsActionTask extends Task {

    protected String actionID;

    protected String pluginName;

    protected Vector properties = new Vector();
    
    public void setActionID(String actionID) {
        this.actionID = actionID;
    }
    
    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }
    
    public String getPluginName() {
        return pluginName;
    }
    
    public void addProperty(Property property) {
        if (property != null) {
            log("using property name:"+property.getName()+", value:"+property.getValue(),Project.MSG_VERBOSE);
            properties.addElement(property);
        }
    }
    
    // to be used by extending tasks
    protected void addProperty(String name, String value) {        
        Property property = new Property();
        property.setName(name);
        property.setValue(value);
        log("property "+name+" addded by action", Project.MSG_VERBOSE);
        this.addProperty(property);
    }
    
    protected abstract PluginDescriptor.Action getSelectedAction(PluginDescriptor pluginDescriptor) throws PluginResourceNotFoundException;
    
    public void execute () throws BuildException {
        PluginManager pluginManager = PluginManagerStorageSupport.retrievePluginManager();
        if (pluginManager == null) {
            // something went wrong !!!!
            throw new BuildException("Fatal error - cannot find plugin manager");
        }        
        // have to somehow get the name of the plugin to be used
        if (pluginName == null) {
            throw new BuildException("Task has to have pluginName attribute specified!");                               
        }
        // get the appropriate plugin descriptor
        try {
            PluginDescriptor pluginDescriptor = pluginManager.getPreferredPluginDescriptor(pluginName);
            PluginDescriptor.Action action = getSelectedAction(pluginDescriptor);
            PluginExecuteTask.pluginExecute(pluginDescriptor, action, this, null, properties);
        } catch (PluginResourceNotFoundException prnfe) {
            throw new BuildException("Cannot find plugin resource. Reason: "+prnfe.getMessage(),prnfe);
        }
    }
    
}
