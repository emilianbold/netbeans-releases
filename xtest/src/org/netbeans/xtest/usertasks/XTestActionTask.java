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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
 * XTestTask.java
 * for now using only static configuration hardcoded in this class
 * This is clearly far from the best solution, but it works for now
 * configuration should be based on a xml file ...
 *
 * Created on 22.9. 2003, 17:04
 */

package org.netbeans.xtest.usertasks;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.taskdefs.Ant;
//import org.apache.tools.ant.taskdefs.Property;
import java.io.File;

/**
 * @author mb115822
 */
public class XTestActionTask extends Task {

    // action names - should be loaded from the config file
    private static final String ACTION_BUILD_TESTS = "buildTests";
    private static final String ACTION_RUN_TESTS = "runTests";
    private static final String ACTION_CLEAN_TESTS = "cleanTests";
    private static final String ACTION_CLEAN_RESULTS = "cleanResults";
    /* Deprecated - use clean instead. */
    private static final String ACTION_CLEAN_ALL = "cleanAll";
    private static final String ACTION_CLEAN = "clean";
    private static final String ACTION_VERSION = "version";
    private static final String ACTION_GENERATE_FAILED_CONFIG = "generateFailedConfig";
    private static final String ACTION_GENERATE_EXCLUDED_CONFIG = "generateExcludedConfig";
    
    // this is not public action
    private static final String ACTION_PREPARE_TESTS ="prepare-tests";
    
    // targets to be executed -> for now all will be executed from 
    // ${xtest.home}/lib/module_harness.xml, but it might change in the future
    private static final String ACTIONS_SCRIPT_FILENAME = "lib/module_harness.xml";
    
    // target names
    private static final String TARGET_BUILD_TESTS = "compiler-launcher"; // weird naming - should be named buildtests, but
                                                                   // this is used by the deprecated compilers
    private static final String TARGET_RUN_TESTS = "runtests";
    private static final String TARGET_CLEAN_TESTS = "cleantests";
    private static final String TARGET_CLEAN_RESULTS = "cleanresults";
    /* Deprecated - use clean instead. */
    private static final String TARGET_CLEAN_ALL    = "realclean";
    private static final String TARGET_CLEAN    = "clean";
    private static final String TARGET_VERSION      = "version";
    private static final String TARGET_GENERATE_FAILED_CONFIG = "generateFailedConfig";
    private static final String TARGET_GENERATE_EXCLUDED_CONFIG = "generateExcludedConfig";
    
    // non-public target
    private static final String TARGET_PREPARE_TESTS      = "prepare-tests";
    
    private static final String[][] ACTION_MATRIX = {
        {ACTION_BUILD_TESTS, TARGET_BUILD_TESTS},
        {ACTION_RUN_TESTS, TARGET_RUN_TESTS},
        {ACTION_CLEAN_TESTS, TARGET_CLEAN_TESTS},
        {ACTION_CLEAN_RESULTS, TARGET_CLEAN_RESULTS},
        {ACTION_CLEAN_ALL, TARGET_CLEAN_ALL},
        {ACTION_CLEAN, TARGET_CLEAN},
        {ACTION_VERSION, TARGET_VERSION},
        {ACTION_PREPARE_TESTS, TARGET_PREPARE_TESTS},
        {ACTION_GENERATE_FAILED_CONFIG, TARGET_GENERATE_FAILED_CONFIG},
        {ACTION_GENERATE_EXCLUDED_CONFIG, TARGET_GENERATE_EXCLUDED_CONFIG}
    };
    
    
    private static String getTargetForAction(String action) {
        for (int i=0; i < ACTION_MATRIX.length; i++) {
            if (ACTION_MATRIX[i][0].equalsIgnoreCase(action)) {
                return ACTION_MATRIX[i][1];
            }
        }
        return null;
    }
    
    private String xtestHome;
    private static final String XTEST_HOME_PROPERTY_NAME = "xtest.home";
    
    private String executeAction;
    
    public void setXTestHome(String xtestHome) {
        this.xtestHome = xtestHome;
    }
    
    public void setExecuteAction(String executeAction) {
        this.executeAction = executeAction;
    }
    
    private void setXTestHome() throws BuildException {
        if (this.getProject().getProperty(XTEST_HOME_PROPERTY_NAME) == null) {
            if (this.xtestHome != null) {
                this.getProject().setProperty(XTEST_HOME_PROPERTY_NAME, xtestHome);
            } else {
                throw new BuildException("Cannot continue - "+XTEST_HOME_PROPERTY_NAME+" is not set");
            }
        } else {
            xtestHome = this.getProject().getProperty(XTEST_HOME_PROPERTY_NAME);
        }
    }
    
    private void executeAction() throws BuildException {
        String target = getTargetForAction(executeAction);
        if (target != null) {
            File antFile = (new File(new File(xtestHome),ACTIONS_SCRIPT_FILENAME));
            Ant newAnt = new Ant();
            newAnt.setProject(this.getProject());
            newAnt.setOwningTarget(this.getOwningTarget());
            newAnt.setAntfile(antFile.getAbsolutePath());
            newAnt.setTarget(target);
            log("XTest will execute action "+executeAction);
            // For tests run from binary tests distribution store output messages to a log file
            // (see nbbuild/templates/xtest.xml#runtestsdist)
            if(executeAction.equals(ACTION_RUN_TESTS) && getProject().getProperty("xtest.distexec") != null) {
                newAnt.setOutput(getLogFile());
            }
            newAnt.execute();
        } else {
            throw new BuildException("Cannot execute unknown action '"+executeAction+"'");
        }
    }

    /** Returns path to log file composed from module name the following way
     * ${xtest.results.testrun.dir}/logs/${xtest.module}_{xtest.testtype}.log
     * (e.g. results/testrun_070306-115318/logs/ant_unit.log).
     * @return path to log file.
     */
    private String getLogFile() {
        String logsDir = getProject().getProperty("xtest.results.testrun.dir")+"/logs";
        File logsDirFile = getProject().resolveFile(logsDir);
        log("logsDirFile="+logsDirFile, Project.MSG_DEBUG);
        if (!logsDirFile.exists()) {
            logsDirFile.mkdirs();
        }
        String prefix = getProject().getProperty("xtest.module")+"_"+getProject().getProperty("xtest.testtype");
        log("prefix="+prefix, Project.MSG_DEBUG);
        String newPrefix = prefix.replace('/','_');
        File logFile = new File(logsDirFile, newPrefix+".log");
        int c = 1;
        while (logFile.exists()) {
            logFile = new File(logsDirFile, newPrefix+"_"+c+".log");
            c++;
        }
        log("Output logFile="+logFile, Project.MSG_VERBOSE);
        return logFile.getAbsolutePath();
    }
    
    public void execute() throws BuildException {
        setXTestHome();
        executeAction();       
    }
    
}
