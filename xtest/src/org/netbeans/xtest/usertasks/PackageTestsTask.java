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
