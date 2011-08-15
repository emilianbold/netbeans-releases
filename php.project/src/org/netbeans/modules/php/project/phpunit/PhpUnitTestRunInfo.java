/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates. Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General Public License Version 2 only ("GPL") or the Common Development and Distribution License("CDDL")
 * (collectively, the "License"). You may not use this file except in compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header Notice in each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates
 * this particular file as subject to the "Classpath" exception as provided by Oracle in the GPL Version 2 section of the License file that accompanied this code. If
 * applicable, add the following below the License Header, with the fields enclosed by brackets [] replaced by your own identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only the GPL Version 2, indicate your decision by adding "[Contributor] elects to include this
 * software in this distribution under the [CDDL or GPL Version 2] license." If you do not indicate a single choice of license, a recipient has the option to distribute your
 * version of this file under either the CDDL, the GPL Version 2 or to extend the choice of license to its licensees as provided above. However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies only if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.phpunit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.openide.filesystems.FileObject;

/**
 * Class holding information about a test run.
 * <p>
 * This class is thread-safe.
 */
public final class PhpUnitTestRunInfo {

    private final FileObject workingDirectory;
    private final FileObject startFile;
    private final String testName;
    private final List<Testcase> customTests = new CopyOnWriteArrayList<Testcase>();

    private volatile boolean rerun = false;
    private volatile String testGroups;


    /**
     * Create new info about test run.
     * @param workingDirectory working directory
     * @param startFile start file (can be directory)
     * @param testName test name, can be {@code null}
     */
    public PhpUnitTestRunInfo(FileObject workingDirectory, FileObject startFile, String testName) {
        assert workingDirectory != null;
        assert startFile != null;

        this.workingDirectory = workingDirectory;
        this.startFile = startFile;
        this.testName = testName;
    }

    /**
     * Get initial file or directory of test run.
     * @return initial file or directory of test run
     */
    public FileObject getStartFile() {
        return startFile;
    }

    /**
     * Get name of the test to be run, can be {@code null}.
     * @return name of the test to be run, can be {@code null}
     */
    public String getTestName() {
        return testName;
    }

    /**
     * Return {@code true} if all tests are to be run.
     * @return {@code true} if all tests are to be run
     */
    public boolean allTests() {
        return testName == null;
    }

    /**
     * Get working directory of tests.
     * @return working directory of tests
     */
    public FileObject getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Get custom tests to be run or empty list, never {@code null}.
     * @return custom tests to be run or empty list, never {@code null}
     */
    public List<Testcase> getCustomTests() {
        return new ArrayList<Testcase>(customTests);
    }

    /**
     * Reset custom tests.
     * @see #getCustomTests()
     * @see #setCustomTests(java.util.Collection)
     */
    public void resetCustomTests() {
        customTests.clear();
    }

    /**
     * Set custom tests to be run.
     * @param tests custom tests to be run or empty list, never {@code null}
     */
    public void setCustomTests(Collection<Testcase> tests) {
        resetCustomTests();
        customTests.addAll(tests);
    }

    /**
     * Check whether this test run is rerun.
     * @return {@code true} if this test run is rerun
     */
    public boolean isRerun() {
        return rerun;
    }

    /**
     * Set whether this test run is rerun.
     * @param rerun {@code true} for rerun, {@code false} otherwise
     */
    public void setRerun(boolean rerun) {
        this.rerun = rerun;
    }

    /**
     * Get test groups of this test run.
     * @return test groups of this test run, can be {@code null}
     */
    public String getTestGroups() {
        return testGroups;
    }

    /**
     * Set test groups of this test run.
     * @param testGroups test groups of this test run, can be {@code null}
     */
    public void setTestGroups(String testGroups) {
        this.testGroups = testGroups;
    }

}
