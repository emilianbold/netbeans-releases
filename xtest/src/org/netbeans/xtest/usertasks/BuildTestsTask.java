/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.xtest.usertasks;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.netbeans.xtest.plugin.*;
import org.apache.tools.ant.taskdefs.Property;

/**
 * This class is rather a hack than an example of a good design. Since
 * I'm quite lazy, I just extended the CompileTestsTask and used PackageTests. 
 * @author  mb115822
 */
public class BuildTestsTask extends CompileTestsTask {
    
    private PackageTestsTask packageTask;
    
    protected PackageTestsTask getPackageTestsTask() {
        if (packageTask == null) {
            // should instantiate task by this call, but it does not work for me
            // packageTask = (PackageTestsTask)this.getProject().createTask("org.netbeans.xtest.usertasks.PackageTestsTask");
            packageTask = new PackageTestsTask();
            // and properties
            packageTask.setProject(this.getProject());
            packageTask.setOwningTarget(this.getOwningTarget());
            // action task propertues
            packageTask.setPluginName(this.getPluginName());
            packageTask.setTaskName(this.getTaskName());
        }
        return packageTask;
    }
    
    // for packaging
    public void setPackageIncludes(String packageIncludes) {
        getPackageTestsTask().setIncludes(packageIncludes);
    }    
    
    public void setPackageExcludes(String packageExcludes) {
        getPackageTestsTask().setIncludes(packageExcludes);
    }

    
    public void execute () throws BuildException {
        log("XTest: building tests.");
        // run the the actions
        // first compile action
        super.execute();
        // then package action
        getPackageTestsTask().execute();
    }    
    
}
