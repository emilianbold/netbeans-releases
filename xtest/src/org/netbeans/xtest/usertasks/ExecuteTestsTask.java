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
import org.netbeans.xtest.testrunner.TestRunnerHarness;
/**
 * @author mb115822
 */
public class ExecuteTestsTask extends TestsActionTask {
     // property names - should be public
    /**
     * classpath used for test execution
     */
    public static final String EXECUTE_CLASSPATH  = "test.classpath";
    public static final String EXECUTE_TESTMODE = "xtest.testrunner.mode";

    //
    protected Path testClasspath;   
    // test mode
    protected String testMode;
    
    // setters
    public void setClasspath(Path testClasspath) {
        if (this.testClasspath == null) {
            this.testClasspath = testClasspath;
        } else {
            this.testClasspath.append(testClasspath);
        }
    }    
    
    public void setClasspathRef(Reference ref) {
        createClasspath().setRefid(ref);
    }
    
    public Path createClasspath() {
        if (testClasspath == null) {
            this.testClasspath = new Path(getProject());
        } 
        return testClasspath.createPath();
    }
    
    public void setTestMode(String testMode) {
        if (TestRunnerHarness.TESTRUN_MODE_TESTBAG.equalsIgnoreCase(testMode) 
            || (TestRunnerHarness.TESTRUN_MODE_TESTSUITE.equalsIgnoreCase(testMode))) {
            this.testMode = testMode;
        } else {
            throw new BuildException("Uknown testmode. Allowed values are :"
               +TestRunnerHarness.TESTRUN_MODE_TESTBAG+" or "
               +TestRunnerHarness.TESTRUN_MODE_TESTSUITE+".");
        }
    }
    
    
    protected PluginDescriptor.Action getSelectedAction(PluginDescriptor pluginDescriptor)  throws PluginResourceNotFoundException {
        return pluginDescriptor.getExecutor(actionID);        
    }
    
    
    // prepare properties for execute target in plugin 
    //  - in future it might be a simple java method call in a plugin
    protected void runExecuteAction() {
        if (testClasspath != null) {
            addProperty(EXECUTE_CLASSPATH,testClasspath.toString());
        }
        
        if (testMode != null) {
            addProperty(EXECUTE_TESTMODE,testMode);
        }

        // finally execute the parent task
        super.execute();
    }    
    
    public void execute () throws BuildException {
        log("XTest: executing tests in "+getPluginName()+".");
        runExecuteAction();
    }    
}
