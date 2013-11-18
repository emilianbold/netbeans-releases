/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.repository.layering;

import java.io.File;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;
import org.netbeans.junit.Manager;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;
import org.netbeans.modules.cnd.repository.util.RepositoryTestSupport;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;
import static org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase.createTempFile;

/**
 *
 * @author vkvashin
 */
public class RepositoryRelocationFinal extends TraceModelTestBase {
    
    protected @Override void setUp() throws Exception {
        cleanCache = false;
        super.setUp();
    }

    public RepositoryRelocationFinal(String testName) {
        super(testName);
    }

//    private void setCache(File cacheFile) throws Exception {
//        synchronized (lock) {
//            FileUtil.refreshFor(cacheFile.getParentFile());
//            cacheFO = FileUtil.toFileObject(cacheFile);
//            assertNotNull("Null file object for cache file " + cacheFile, cacheFO);
//        }
//    }

    @Override
    protected File getTestCaseDataDir() {
        String dataPath = convertToModelImplDataDir("repository");
        String filePath = "common";
        return Manager.normalizeFile(new File(dataPath, filePath));
    }

    private void parseProject(File projectRoot, File dump) throws Exception {
        
        File err = new File(getWorkDir(), getName() + ".err");
        File cacheFile = new File(projectRoot, "nbproject/cache");
        cacheFile.mkdirs();
        assertTrue("Can't create cache file " + cacheFile, cacheFile.exists());

        PrintStream streamOut = new PrintStream(dump);
        final PrintStream streamErr = new FilteredPrintStream(err);

        DiagnosticExceptoins.Hook hook = new DiagnosticExceptoins.Hook() {
            @Override
            public void exception(Throwable thr) {
                thr.printStackTrace(streamErr);
            }
        };
        DiagnosticExceptoins.setHook(hook);

        doTest(new String[]{projectRoot.getAbsolutePath()}, streamOut, streamErr);

        StringBuilder fileContent = new StringBuilder();
        boolean res = RepositoryTestSupport.grep("(AssertionError)|(Exception)", err, fileContent);
        if (res) {
            assertFalse("Errors in " + err.getAbsolutePath() + "\n" + fileContent, true);
        }
        RepositoryTestSupport.dumpCsmProject(getCsmProject(), streamOut, true);
        streamOut.close();
        streamErr.close();
        err.delete(); // in case of error, don't delete file => delete here, not in finally
    }

    public void testRelocationFinal() throws Exception {

        final AtomicInteger parseCount = new AtomicInteger(0);
        CsmProgressListener progressListener = new CsmProgressAdapter() {
            @Override
            public void fileAddedToParse(CsmFile file) {
                parseCount.incrementAndGet();
            }
        };
        CsmListeners.getDefault().addProgressListener(progressListener);

        File tempBaseDir = createTempFile("test_relocation", "", true);

        try {
            File projectSrcRoot1 = getDataFile("quote_nosyshdr_1/src");
            File projectSrcRoot2 = getDataFile("quote_nosyshdr_2/src");

            File dump1 = new File(projectSrcRoot1.getParentFile(), "dump.dat");
            File dump2 = new File(projectSrcRoot2.getParentFile(), "dump.dat");

            removeDirectory(projectSrcRoot2.getParentFile());
            copyDirectory(projectSrcRoot1.getParentFile(), projectSrcRoot2.getParentFile());

            parseProject(projectSrcRoot2, dump2);
            assertEquals("Parse count after reloaction ", 0, parseCount.get());

            if (CndCoreTestUtils.diff(dump1, dump2, null)) {
                fail("OUTPUT Difference between diff " + dump1 + " " + dump2); // NOI18N
            }

            dump1.delete();
            dump2.delete();
        } finally {
            removeDirectory(tempBaseDir);
        }
    }


}
