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


package org.netbeans.xtest.usertasks;


import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.netbeans.xtest.plugin.*;
import org.apache.tools.ant.taskdefs.Property;

/**
 * @author mb115822
 */
public class PackageTestsTask extends TestsActionTask {
    // property names - should be public

    /**
     * files included to tests packaging
     */
    public static final String PACKAGE_INCLUDES = "package.tests.includes";
    /**
     * files excluded from tests packaging
     */
    public static final String PACKAGE_EXCLUDES = "package.tests.excludes";
    
    
    // for packaging
    protected String packageIncludes;
    protected String packageExcludes;
    
    
    // ant script setters  
   
    
    // for packaging
    public void setIncludes(String packageIncludes) {
        this.packageIncludes = packageIncludes;
    }    
    
    public void setExcludes(String packageExcludes) {
        this.packageExcludes = packageExcludes;
    }
    
    
    
    
    // action for TestsActionTask
    protected PluginDescriptor.Action getSelectedAction(PluginDescriptor pluginDescriptor)  throws PluginResourceNotFoundException {
        return pluginDescriptor.getPackager(actionID);        
    }
    
    // prepare properties for compile target in plugin 
    //  - in future it might be a simple java method call in a plugin
    protected void runPackageAction() {
        
        if (packageIncludes != null) {
            addProperty(PACKAGE_INCLUDES, packageIncludes);
        }
        
        if (packageExcludes != null) {
            addProperty(PACKAGE_EXCLUDES, packageExcludes);
        }
        
        // finally execute the parent task
        super.execute();
    }
    
    public void execute () throws BuildException {
        log("XTest: packaging tests.");
        if (getPluginName() == null) {
            // there is no need to specify plugin for compilation
            // when not specified, base compiler is used
            setPluginName("base");
        }        
        // run the the plugin action
        runPackageAction();

    }
    
}
