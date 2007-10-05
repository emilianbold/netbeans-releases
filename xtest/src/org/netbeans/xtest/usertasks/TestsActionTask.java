/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
