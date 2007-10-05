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
