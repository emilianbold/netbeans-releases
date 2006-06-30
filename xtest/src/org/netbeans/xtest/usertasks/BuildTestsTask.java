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
