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
package org.netbeans.modules.python.editor.codecoverage;

import java.util.concurrent.Future;
import org.netbeans.api.project.Project;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.codecoverage.CodeCoverageTestHelper;
import org.netbeans.modules.python.api.PythonExecution;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.api.PythonPlatformManager;
import org.netbeans.modules.python.editor.PythonTestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tor Norbye
 */
public class PythonCoverageProviderTest extends PythonTestBase {
    public PythonCoverageProviderTest(String name) {
        super(name);
    }

    private void recordCoverage(String projectPath, String exeFile) throws Exception {
        Project project = getTestProject(projectPath);
        assertNotNull(project);
        PythonCoverageProvider provider = PythonCoverageProvider.get(project);
        assertNotNull(provider);

        provider.setEnabled(true);

        String relFilePath = projectPath + "/" + exeFile; // NOI18N
        FileObject fo = getTestFile(relFilePath);
        assertNotNull(fo);
        BaseDocument doc = getDocument(fo);
        assertNotNull(doc);

        String script = FileUtil.toFile(fo).getAbsolutePath();
        PythonExecution pyexec = new PythonExecution();
        pyexec.setDisplayName(fo.getName());
        String path = FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath();
        pyexec.setWorkingDirectory(path);
        PythonPlatformManager manager = PythonPlatformManager.getInstance();
        PythonPlatform platform = manager.getPlatform(manager.getDefaultPlatform());
        pyexec.setCommand(platform.getInterpreterCommand());
        pyexec.setScript(script);
        pyexec.setCommandArgs(platform.getInterpreterArgs());
        pyexec.setShowControls(false);
        pyexec.setShowInput(false);
        pyexec.setShowWindow(false);
        pyexec.addStandardRecognizers();

        pyexec = provider.wrapWithCoverage(pyexec);
        Future<Integer> run = pyexec.run();
        run.get();
    }

    private void checkCoverage(String projectPath, String exeFile) throws Exception {
        checkCoverage(projectPath, exeFile, exeFile);
    }

    private void checkCoverage(String projectPath, String exeFile, String checkFile) throws Exception {
        recordCoverage(projectPath, exeFile);
        // TODO - assert that we have coverage data run at this point... might be delayed..
        // Perhaps add a little delay
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ire) {
            fail();
        }

        CodeCoverageTestHelper.checkCoverage(this, projectPath, checkFile);
    }

    public void testCoverage1() throws Exception {
        checkCoverage("testfiles/codecoverage/CoveragePrj", "src/coverageprj.py");
    }

    public void testCoverage2() throws Exception {
        checkCoverage("testfiles/codecoverage/CoveragePrj", "src/md5driver.py");
    }

    public void testCoverage3() throws Exception {
        checkCoverage("testfiles/codecoverage/CoveragePrj", "src/coverageprj2.py");
    }

    public void testCoverage4() throws Exception {
        checkCoverage("testfiles/codecoverage/CoveragePrj", "src/coverageprj3.py");
    }

    public void testCoverage5() throws Exception {
        checkCoverage("testfiles/codecoverage/CoveragePrj2", "src/romantest9.py", "src/roman9.py");
    }

}
