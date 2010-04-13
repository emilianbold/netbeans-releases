/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.repository.disk;

import java.io.File;
import java.util.Collection;
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
    
    public void testClosure() throws Exception {
        
        final FilesAccessStrategyImpl strategy = (FilesAccessStrategyImpl) FilesAccessStrategyImpl.getInstance();
                
        Collection<String> setZero = strategy.testGetCacheFileNames();
        assertTrue("Cache should be empty at that time", setZero.isEmpty());
        
	final TraceModelBase traceModel = new  TraceModelBase(true);
	traceModel.setUseSysPredefined(true);
        
        long waitAfterParse = 6000;
        long waitAfterClose = 2000;
        
        // Open a project, make sure cache is NOT empty
        ProjectBase projectRoot1 = createProject(traceModel, "project-1", "file1.cpp", "int foo1");
        waitCondition(new Condition("Cache should not be empty at that time") {
            @Override
            boolean check() {
                return ! strategy.testGetCacheFileNames().isEmpty();
            }
        }, waitAfterParse);
        
        // Close the project, make sure cache IS empty
        traceModel.getModel().closeProjectBase(projectRoot1);
        waitCondition(new Condition("Cache should be empty after project closure") {
            @Override
            boolean check() {
                return strategy.testGetCacheFileNames().isEmpty();
            }
        }, waitAfterClose);

        // Open the 2-nd project, make sure cache is NOT empty
        ProjectBase projectRoot2 = createProject(traceModel, "project-2", "file2.cpp", "int foo2");
        waitCondition(new Condition("Cache should not be empty at that time") {
            @Override
            boolean check() {
                return ! strategy.testGetCacheFileNames().isEmpty();
            }
        }, waitAfterParse);
        final Collection<String> setTwo = strategy.testGetCacheFileNames();
        
        // Open the 3-rd project, make sure cache is NOT empty
        ProjectBase projectRoot3 = createProject(traceModel, "project-3", "file3.cpp", "int foo3");
        waitCondition(new Condition("Cache should not be empty at that time") {
            @Override
            boolean check() {
                return ! strategy.testGetCacheFileNames().isEmpty();
            }
        }, waitAfterParse);
        
        // Close the 3-rd project, make sure cache is the same as befor it was open
        traceModel.getModel().closeProjectBase(projectRoot3);
        waitCondition(new Condition("The set of the cached files should be the same as before") {
            @Override
            boolean check() {
                return strategy.testGetCacheFileNames().equals(setTwo);
            }
        }, waitAfterClose);
    }
    
    private ProjectBase createProject(TraceModelBase traceModel, String projectName, String fileName, String fileContent) throws Exception {
        File projectRoot = new File(getWorkDir(), projectName);
        projectRoot.mkdirs();
        File sourceFile = new File(projectRoot, fileName);
        writeFile(sourceFile, fileContent);
        ProjectBase project = createExtraProject(traceModel, projectRoot, projectName);
        project.waitParse();
        return project;
    }

    private abstract class Condition {
        public final String failureMessage;
        public Condition(String failureMessage) {
            this.failureMessage = failureMessage;
        }
        abstract boolean check();
    }

    private void waitCondition(Condition condition, long timeout) throws Exception {
        long time = System.currentTimeMillis();
        while (!condition.check() && System.currentTimeMillis() < time + timeout) {
            sleep(50);
        }
        assertTrue(condition.failureMessage, condition.check());
    }
}
