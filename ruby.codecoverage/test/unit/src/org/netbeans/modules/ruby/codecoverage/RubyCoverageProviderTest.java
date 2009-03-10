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
package org.netbeans.modules.ruby.codecoverage;

import java.io.File;
import java.util.concurrent.Future;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.project.Project;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.codecoverage.CodeCoverageTestHelper;
import org.netbeans.modules.ruby.RubyTestBase;
import org.netbeans.modules.ruby.platform.execution.RubyExecutionDescriptor;
import org.netbeans.modules.ruby.platform.execution.RubyProcessCreator;
import org.netbeans.modules.ruby.rubyproject.RubyBaseProject;
import org.netbeans.modules.ruby.rubyproject.RubyProjectUtil;
import org.netbeans.modules.ruby.rubyproject.SharedRubyProjectProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tor Norbye
 */
public class RubyCoverageProviderTest extends RubyTestBase {
    public RubyCoverageProviderTest(String name) {
        super(name);
    }

    private void recordCoverage(String projectPath, String exeFile) throws Exception {
        Project project = getTestProject(projectPath);
        assertNotNull(project);
        RubyCoverageProvider provider = RubyCoverageProvider.get(project);
        assertNotNull(provider);
        provider.clear();

        provider.setEnabled(true);

        String relFilePath = projectPath + "/" + exeFile; // NOI18N
        FileObject fo = getTestFile(relFilePath);
        assertNotNull(fo);
        BaseDocument doc = getDocument(fo);
        assertNotNull(doc);

        RubyPlatform platform = RubyPlatform.platformFor(project);
        String displayName = "test";

        File pwd = FileUtil.toFile(project.getProjectDirectory()).getAbsoluteFile();
        String target = FileUtil.toFile(fo).getAbsolutePath();

        String rubyOptions = SharedRubyProjectProperties.getRubyOptions((RubyBaseProject) project);

        String includePath = RubyProjectUtil.getLoadPath(project);
        if (rubyOptions != null) {
            rubyOptions = includePath + " " + rubyOptions; // NOI18N
        } else {
            rubyOptions = includePath;
        }

        RubyExecutionDescriptor desc = new RubyExecutionDescriptor(platform, displayName, pwd, target);
        desc.showSuspended(false);
        desc.showProgress(false);
        desc.frontWindow(false);
        desc.allowInput();
        desc.fileObject(fo);
        desc.initialArgs(rubyOptions);

        desc = provider.wrapWithCoverage(desc, false, null);
        RubyProcessCreator rpc = new RubyProcessCreator(desc, null);
        if (rpc.isAbleToCreateProcess()) {
            ExecutionService service = ExecutionService.newService(rpc, desc.toExecutionDescriptor(), displayName);
            Future<Integer> run = service.run();
            run.get();
        } else {
            fail("Can't create process");
        }
    }

//    private void recordCoverageWithRake(String projectPath, String rakeTarget, String exeFile) throws Exception {
//        Project project = getTestProject(projectPath);
//        assertNotNull(project);
//        RubyCoverageProvider provider = RubyCoverageProvider.get(project);
//        assertNotNull(provider);
//        provider.clear();
//
//        provider.setEnabled(true);
//
//        RakeSupport.refreshTasks(project, false);
//
////
////        RakeRunner runner = new RakeRunner(project);
////
////        RakeTask rakeTask = RakeSupport.getRakeTask(project, rakeTarget);
////        assertNotNull(rakeTask);
////
////        List<ExecutionService> services = runner.getExecutionServices(Collections.singletonList(rakeTask));
////
//
//        List<Future<Integer>> futures = new RakeRunner(project).run(rakeTarget); // NOI18N
//        assertNotNull(futures);
//        assertTrue(futures.size() > 0);
//        for (Future<Integer> future : futures) {
//            future.get();
//        }
//
//        String relFilePath = projectPath + "/" + exeFile; // NOI18N
//        FileObject fo = getTestFile(relFilePath);
//        assertNotNull(fo);
//        BaseDocument doc = getDocument(fo);
//        assertNotNull(doc);
//    }

    private void checkCoverage(String projectPath, String exeFile) throws Exception {
        recordCoverage(projectPath, exeFile);
        // TODO - assert that we have coverage data run at this point... might be delayed..
        // Perhaps add a little delay
        try {
            Thread.sleep(4000);
        } catch (InterruptedException ire) {
            fail();
        }

//        CodeCoverageTestHelper.checkCoverage(this, projectPath, exeFile);
        fail("Codecoverage has to be rewritten for CSL/ParsingAPI");
    }

//    private void checkCoverageWithRake(String projectPath, String target, String exeFile) throws Exception {
//        recordCoverageWithRake(projectPath, target, exeFile);
//        // TODO - assert that we have coverage data run at this point... might be delayed..
//        // Perhaps add a little delay
//        try {
//            Thread.sleep(4000);
//        } catch (InterruptedException ire) {
//            fail();
//        }
//
//        CodeCoverageTestHelper.checkCoverage(this, projectPath, exeFile);
//    }
//
//    public void testCoverage0() throws Exception {
//        checkCoverageWithRake("testfiles/CoveragePrj", "test", "test/simple_number_test.rb");
//    }

    public void testCoverage1() throws Exception {
        checkCoverage("testfiles/CoveragePrj", "lib/simple_number.rb");
    }

    public void testCoverage2() throws Exception {
        checkCoverage("testfiles/CoveragePrj", "test/simple_number_test.rb");
    }

}