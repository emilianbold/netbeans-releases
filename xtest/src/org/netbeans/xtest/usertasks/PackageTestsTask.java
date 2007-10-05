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
