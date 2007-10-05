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
package org.netbeans.xtest.pe;

import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.xtest.harness.MTestConfig;
import org.netbeans.xtest.harness.Testbag;
import org.netbeans.xtest.harness.Testbag.InExclude;
import org.netbeans.xtest.harness.Testbag.Patternset;
import org.netbeans.xtest.pe.xmlbeans.TestBag;
import org.netbeans.xtest.pe.xmlbeans.TestRun;
import org.netbeans.xtest.pe.xmlbeans.UnitTestCase;
import org.netbeans.xtest.pe.xmlbeans.UnitTestSuite;
import org.netbeans.xtest.pe.xmlbeans.XTestResultsReport;
import org.netbeans.xtest.util.FileUtils;
import org.netbeans.xtest.xmlserializer.XMLSerializer;
import org.netbeans.xtest.util.SerializeDOM;

/** It modifies config file that failed tests from the last test run are excluded.
 * <br>
 * Usage in test/build.xml:
 * <pre>
 * <taskdef name="excludeFailed" classpath="${xtest.home}/lib/xtest.jar"
 *          classname="org.netbeans.xtest.pe.ExcludeFailedTask"/>
 *
 * <target name="all">
 *      <antcall target="cleanresults"/>
 *      <antcall target="runtests"/>
 *      <excludeFailed resultsDir="results" allPassed="xtest.allPassed" passCount="3"/>
 *      <antcall target="checkAllPassed"/>
 * </target>
 *
 * <target name="checkAllPassed" unless="xtest.allPassed">
 *      <antcall target="all"/>
 * </target>
 * </pre>
 * @author Jiri.Skrivanek@sun.com
 */
public class ExcludeFailedTask extends Task {

    private String allPassedPropertyName;
    private String count;
    /** Results directory. */
    private File resultsDir;

    public void setAllPassed(String name) {
        allPassedPropertyName = name;
    }

    public void setPassCount(String count) {
        this.count = count;
    }

    public void setResultsDir(File resultsDir) {
        this.resultsDir = resultsDir;
    }
    
    public void execute() throws BuildException {
        log("Excludes failed test cases from config");
        int inputDirType = ResultsUtils.resolveResultsDir(resultsDir);
        if(inputDirType != ResultsUtils.TESTREPORT_DIR) {
            throw new BuildException("Not a suitable results dir: "+resultsDir);
        }
        if(!addFailed(resultsDir)) {
            // if all tests passed, decrease counter
            setCounter(getCounter()-1);
            if(getCounter() < 1) {
                // all tests passed count-times
                getProject().setProperty(allPassedPropertyName, "true");
            }
        } else {
            // reset counter
            setCounter(Integer.parseInt(this.count));
        }
    }

    private int getCounter() {
        String counter = getProject().getProperty("xtest.passed.counter");
        if(counter == null) {
            // initialize counter to given value
            counter = this.count;
        }
        return Integer.parseInt(counter);
    }

    private void setCounter(int counter) {
        getProject().setProperty("xtest.passed.counter", String.valueOf(counter));
    }

    /** Adds first failed test from last testrun to confing. Returns true if
     * at least one test failed.
     */
    private boolean addFailed(File reportRoot) {
        try {
            // get the root XTestResultsReport
            File xtrFile = new File(ResultsUtils.getXMLResultDir(reportRoot, false),PEConstants.TESTREPORT_FAILURES_XML_FILE);
            FileUtils.checkFileIsFile(xtrFile);
            // get the results report
            XTestResultsReport report = XTestResultsReport.loadFromFile(xtrFile);
            TestRun[] testRuns = report.xmlel_TestRun;
            if(testRuns == null) {
                log("No failed tests found.");
                return false;
            }
            // find the last test run
            String lastTestRunID = "";
            int lastTestRunIDIndex = 0;
            for(int i=0;i<testRuns.length;i++) {
                if(testRuns[i].xmlat_runID.compareTo(lastTestRunID) > 0) {
                    lastTestRunID = testRuns[i].xmlat_runID;
                    lastTestRunIDIndex = i;
                }
            }
            log("lastTestRunID="+lastTestRunID, getProject().MSG_VERBOSE);
            TestRun testRun = testRuns[lastTestRunIDIndex];
            TestBag[] testBags = testRun.xmlel_TestBag;
            if(testBags == null) {
                log("No failed test bags.");
                return false;
            }
            UnitTestSuite[] testSuites = testBags[0].xmlel_UnitTestSuite;
            UnitTestCase[] testCases = testSuites[0].xmlel_UnitTestCase;
            String failedTestCase = testCases[0].getClassName().replace('.', '/')+".class/"+testCases[0].getName();
            log("First failed TestCase: "+failedTestCase);

            // get original test config file (expects test type is the same for all test bags)
            File configFile = new File(reportRoot.getParentFile(), "cfg-"+testBags[0].getTestType()+".xml");
            log("Config file: "+configFile);
            MTestConfig mconfig = MTestConfig.loadConfig(configFile);
            // find test bag within original config
            Testbag configTestbag = null;
            Testbag[] configTestbags = mconfig.getTestbags();
            for(int i=0;i<configTestbags.length;i++) {
                if(configTestbags[i].getName().equals(testBags[0].getName())) {
                    configTestbag = configTestbags[i];
                    break;
                }
            }
            if(isSame("["+configTestbag.getName()+"]"+failedTestCase)) {
                throw new BuildException("Test case "+failedTestCase+" from testbag "+configTestbag.getName()+
                        " should already be excluded. Probably class doesn't extend NbTestCase which is required to omit tests excluded in config.");
            }
            Patternset patternset = configTestbag.getTestsets()[0].getPatternset()[0];
            InExclude[] excludes = patternset.getExcludes();
            if(excludes == null) {
                excludes = new InExclude[0];
            }
            InExclude newExclude = new InExclude();
            newExclude.setName(failedTestCase);
            InExclude[] newExcludes = new InExclude[excludes.length+1];
            for(int i=0;i<excludes.length;i++) {
                newExcludes[i] = excludes[i];
            }
            newExcludes[newExcludes.length-1] = newExclude;
            patternset.setExcludes(newExcludes);
            // save modified config file
            SerializeDOM.serializeToFile(XMLSerializer.toDOMDocument(mconfig), configFile);
        } catch (Exception e) {
            throw new BuildException(e.getMessage(), e);
        }
        return true;
    }

    /** Returns true if last failed test case was the same as current one.
     * It set current failed as last one.
     **/
    private boolean isSame(String failed) {
        String lastFailed = getProject().getProperty("xtest.last.failed");
        getProject().setProperty("xtest.last.failed", failed);
        return failed.equals(lastFailed);
    }
}