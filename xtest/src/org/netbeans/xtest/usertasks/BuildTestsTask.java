/*
 * BuildTestsTask.java
 *
 * Created on February 2, 2004, 4:35 PM
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
