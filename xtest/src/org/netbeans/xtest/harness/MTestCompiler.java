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
 * NbExecutor.java
 *
 * Created on March 28, 2001, 6:57 PM
 */

package org.netbeans.xtest.harness;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.taskdefs.*;

import java.util.*;
import java.io.File;

/**
 *
 * @author  mk97936
 * @version
 */
public class MTestCompiler extends Task {

    String targetName = null;
    String targetParamClasspathProp = null;
    String targetParamTestConfigProp = "tbag.testtype";
    String targetParamNameProp = null;
    String targetParamExecutorProp = "tbag.executor";
    
    
    public void setTargetName(String name) {
        this.targetName = name;
    }
    
    public void setParamClasspathProp(String param) {
        this.targetParamClasspathProp = param;
    }
    
    public void setParamTestConfigProp(String param) {
        this.targetParamTestConfigProp = param;
    }
    
    public void setParamNameProp(String param) {
        this.targetParamNameProp = param;
    }
    
    public void setParamExecutorProp(String param) {
        this.targetParamExecutorProp = param;
    }
    
    
    public void executeDefinedCompiler(Testbag testbag, Ant antTask) throws BuildException{
        
        MTestConfig.AntExecType compiler = testbag.getCompiler();
        
        antTask.setAntfile( compiler.getAntFile() );
        antTask.setTarget( compiler.getTarget() );
        
        if (compiler.getDir() != null) {
            antTask.setDir( antTask.getProject().resolveFile( compiler.getDir()));
        }
        
        //ant_new.init();
        
        /// ??????? add xtest.userdata| prefix ?????
        // add all test properties for given testbag
                /*
                Testbag.TestProperty properties[] = testbag.getTestProperties();
                if (properties != null)
                    for (int j=0; j<properties.length; j++) {
                        Property ant_prop = ant_new.createProperty();
                        ant_prop.setName( properties[j].getName() );
                        ant_prop.setValue( properties[j].getValue() );
                    }
                 
                 */
        // set name of executed test config
        Property cttprop = antTask.createProperty();
        cttprop.setName( targetParamTestConfigProp );
        cttprop.setValue( MTestConfigTask.getMTestConfig().getTesttype() );
        
        // set name of executed testbag
        Property nameprop = antTask.createProperty();
        nameprop.setName( targetParamNameProp );
        nameprop.setValue( testbag.getName() );
        
        // set name of executor for executed testbags
                /*
                Property execprop = ant_new.createProperty();
                execprop.setName( targetParamExecutorProp );
                execprop.setValue( testbag.getExecutor().getName() );
                 */
        
        // execute tests
        antTask.execute();
    }
    
    /*
    public void executePluginCompiler(String pluginName, Ant antTask) throws BuildException {
    }
     */
    
    public void execute() throws BuildException {
        
        if (null == targetParamClasspathProp || 0 == targetParamClasspathProp.length())
            throw new BuildException("Attribute 'targetParamClasspathProp' has to be set.");
        
        Testbag testbags[] = MTestConfigTask.getTestbags();
        if (null == testbags)
            throw new BuildException("TestBag configuration wasn't chosen.", getLocation());

        for (int i=0; i<testbags.length; i++) {
                // get TestBag 
                Testbag testbag = testbags[i];

                if (testbag.getCompiler() == null) {
                    throw new BuildException("Testbag "+testbag.getName()+" has not a defined compiler.");
                    // check whether there is plugin
                    /*
                    if (testbag.getPluginName() == null) {
                        throw new BuildException("Testbag "+testbag.getName()+" has not a compiler, nor plugin defined.");
                    }
                     */
                } 

                //Ant ant_new = (Ant) getProject().createTask( "ant" );
                Ant ant = new Ant();
                ant.setOwningTarget( this.getOwningTarget() ); 
                ant.setProject(getProject());
                executeDefinedCompiler(testbag, ant);
            }
        
    }
    
}
