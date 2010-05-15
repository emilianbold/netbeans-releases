/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.project.anttasks;

import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Logger;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
/**
 * Generates client WSDL
 * @author Sreenivasan Genipudi
 */
public class IDEXFormGenerationTask extends Task {
    //Member variable representing source directory

    //Member variable representing source dir
    private File mSourceDir;
    //Member variable representing build dir
    private File mBuildDir;
    private boolean mAlways;
    private boolean mGenerate = false;
    /**
     * Logger instance
     */
    private Logger logger = Logger.getLogger(IDEXFormGenerationTask.class.getName());

    /**
     * Constructor
     */
    public IDEXFormGenerationTask() {
    }


    /**
     * Set the source directory
     * @param srcDir Source directory
     */
    public void setSourceDirectory(String srcDir) {
  	  this.mSourceDir = new File(srcDir);
    }

    /**
     * Set the build directory
     * @param buildDir build directory
     */
    public void setBuildDirectory(String buildDir) {
    	this.mBuildDir = new File(buildDir);
    }
    

    public void setAlways(String always) {
        if (always.endsWith("true")) {
            mAlways = true;
        } else {
            mAlways = false;
        }
    }

    public void setGenerate(String always) {
        if (always.endsWith("true")) {
            mGenerate = true;
        } else {
            mGenerate = false;
        }
    }
    /**

    /**
     * Generate client WSDL
     */
    public void execute() throws BuildException {

        if(this.mSourceDir == null) {
                throw new BuildException("No directory is set for source files.");
        }

        if(this.mBuildDir == null) {
                throw new BuildException("No build directory is set.");
        }

        ClassLoader oldcl = Thread.currentThread().getContextClassLoader();
        XformGenerator xformGen = new XformGenerator();
        try {            
            Thread.currentThread().setContextClassLoader(XformGenerator.class.getClassLoader());            
            xformGen.generate(this.mSourceDir,this.mBuildDir,this.mAlways, this.mGenerate);
        } catch (Exception e) {
            throw new BuildException (e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldcl);
        }

    }
    
    public static void main(String[] args) throws Exception {
        //final String PRJ = "/home/mei/NetBeansProjects/WorklistApp2";
       // final String PRJ = "/home/mei/temp/WLMPOWorklistApp";
        final String PRJ = "/home/mei/NetBeansProjects/AcceptPOWorklistApp";
       // final String PRJ = "/home/mei/NetBeansProjects/demoIssue/BBDemoWLM";
       // final String PRJ = "/home/mei/temp/HelloworldWLMApplication/WLMModule";
        //final String PRJ ="/home/mei/work/open-jbi-components/ojc-core/workflowse/sample/purchaseOrderReview/PurchaseOrderWorklistApp";
        IDEXFormGenerationTask xformGenTask = new IDEXFormGenerationTask ();
        xformGenTask.setAlways("true");
        xformGenTask.setBuildDirectory(PRJ + "/build");
        xformGenTask.setSourceDirectory(PRJ + "/src");
        xformGenTask.execute();
    }
    
}
