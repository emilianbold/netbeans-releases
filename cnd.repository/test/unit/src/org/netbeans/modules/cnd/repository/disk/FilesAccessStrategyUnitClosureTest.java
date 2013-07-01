/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.repository.disk;

import java.io.File;
import java.util.Collection;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelBase;
import org.netbeans.modules.cnd.repository.access.RepositoryAccessTestBase;

/**
 * Test for proper processing of unit closure 
 * from FilesAccessStrategy point of view
 * 
 * @author Vladimir Kvashin
 */
public class FilesAccessStrategyUnitClosureTest extends RepositoryAccessTestBase {

    static {
	//System.setProperty("cnd.modelimpl.timing.per.file.flat", "true");
	//System.setProperty("cnd.repository.listener.trace", "true");
	//System.setProperty("cnd.repository.mf.stat", "true");
    }
       
    public FilesAccessStrategyUnitClosureTest(String testName) {
        super(testName);
    }

    private static final boolean TRACE = true;
    
    public void testClosure() throws Exception {
                        
        //assertTrue("Cache should be empty at that time", getFileNames(strategy).isEmpty());
        
	final TraceModelBase traceModel = new TraceModelBase(true);
	traceModel.setUseSysPredefined(true);
        
        long waitAfterParseTimeout = 30000;

        {
            // Open a project, make sure cache is NOT empty
            ProjectBase projectRoot1 = createProject(traceModel, "project-1", "file1.cpp", "int foo1");
            projectRoot1.waitParse();

            final FilesAccessStrategyImpl strategy1 = FilesAccessStrategyImpl.testGetStrategy(
                    ProjectBase.getCacheLocation((NativeProject) projectRoot1.getPlatformProject()));

            waitCondition(new Condition("Wrong cache size at moment A") {
                @Override
                boolean check() {
                    Collection<String> fileNames = getFileNames(strategy1);
                    int size = fileNames.size();
                    if (size < 7) {
                        return true;
                    } else {
                        failureMessageArg = "" + size + ": " + fileNames;
                        return false;
                    }
                }
            }, waitAfterParseTimeout);

            // Close the project, make sure cache IS empty
            traceModel.getModel().closeProjectBase(projectRoot1);
            if (TRACE) {
                System.out.println("Closed project " + projectRoot1.getName());
            }
            assertTrue("Cache should be empty after project closure", getFileNames(strategy1).isEmpty());
        }

        {
            // Open the 2-nd project, make sure cache is NOT empty
            ProjectBase projectRoot2 = createProject(traceModel, "project-2", "file2.cpp", "int foo2");

            final FilesAccessStrategyImpl strategy2 = FilesAccessStrategyImpl.testGetStrategy(
                    ProjectBase.getCacheLocation((NativeProject) projectRoot2.getPlatformProject()));

            waitCondition(new Condition("Wrong cache size at moment B") {
                @Override
                boolean check() {
                    Collection<String> fileNames = getFileNames(strategy2);
                    int size = fileNames.size();
                    if (size < 7) {
                        return true;
                    } else {
                        failureMessageArg = "" + size + ": " + fileNames;
                        return false;
                    }
                }
            }, waitAfterParseTimeout);
        }

        {
            // Open the 3-rd project, make sure cache has changed and contains all previous keys
            ProjectBase projectRoot3 = createProject(traceModel, "project-3", "file3.cpp", "int foo3");

            final FilesAccessStrategyImpl strategy3 = FilesAccessStrategyImpl.testGetStrategy(
                    ProjectBase.getCacheLocation((NativeProject) projectRoot3.getPlatformProject()));

            final Collection<String> setTwo = strategy3.testGetCacheFileNames();

            waitCondition(new Condition("Cache should change at that time") {
                @Override
                boolean check() {
                    Collection<String> newKeys = getFileNames(strategy3);
                    boolean result = true;
                    failureMessageArg = "";
                    final int ref = 12;
                    int size = newKeys.size();
                    if (size != ref) {
                        failureMessageArg += "newKeys.size() is " + size + " instead of " + ref;
                        result = false;
                    }
                    if (!newKeys.containsAll(setTwo)) {
                        failureMessageArg += " newKeys " + newKeys + " do not contain " + setTwo;
                        result = false;
                    }
                    return result; // newKeys.size() == 8 && newKeys.containsAll(setTwo);
                }
            }, waitAfterParseTimeout);

            // Close the 3-rd project, make sure cache is the same as befor it was open
            traceModel.getModel().closeProjectBase(projectRoot3);
            if (TRACE) {
                System.out.println("Closed project " + projectRoot3.getName());
            }
        }
        //assertTrue("The set of the cached files should be the same as before", getFileNames(strategy).equals(setTwo));
    }
    
    private ProjectBase createProject(TraceModelBase traceModel, String projectName, String fileName, String fileContent) throws Exception {
        File projectRoot = new File(getWorkDir(), projectName);
        projectRoot.mkdirs();
        File sourceFile = new File(projectRoot, fileName);
        writeFile(sourceFile, fileContent);
        ProjectBase project = createExtraProject(traceModel, projectRoot, projectName);
        project.waitParse();
        if (TRACE) {
            System.out.println("Created project " + projectName);
        }
        return project;
    }

    private Collection<String> getFileNames(FilesAccessStrategyImpl strategy) {
        Collection<String> res = strategy.testGetCacheFileNames();
        if (TRACE) {
            System.out.printf("%d %s\n", res.size(), res);
        }
        return res;
    }

    private abstract class Condition {

        private String failureMessage;
        protected String failureMessageArg;

        public Condition(String failureMessage) {
            this.failureMessage = failureMessage;
            this.failureMessageArg = "";
        }
        public String getFailureMessage() {
            return (failureMessageArg == null) ?
                    failureMessage :
                    (failureMessage + ": " + failureMessageArg);
        }

        abstract boolean check();
    }

    private void waitCondition(Condition condition, long timeout) throws Exception {
        long now = System.currentTimeMillis();
        long end = now + timeout;
        while (now < end && !condition.check()) {
            sleep(1000);
            now = System.currentTimeMillis();
        }
        if (now >= end) {
            fail(condition.getFailureMessage());
        }
    }
}
