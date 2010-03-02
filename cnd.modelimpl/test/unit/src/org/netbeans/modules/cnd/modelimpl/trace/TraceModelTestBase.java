/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.modelimpl.trace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.test.ModelImplBaseTestCase;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;

/**
 *
 * @author Vladimir Voskresensky
 */
public class TraceModelTestBase extends ModelImplBaseTestCase {

    private TestModelHelper helper;
    protected boolean cleanCache = true;

    public TraceModelTestBase(String testName) {
        super(testName);
    }

    protected TraceModel getTraceModel() {
        assert helper != null;
        return helper.getTraceModel();
    }

    protected TestModelHelper getTestModelHelper(){
        return helper;
    }

    protected void performTest(String source) throws Exception {
        performTest(source, source + ".dat", source + ".err"); // NOI18N
    }

    protected final ProjectBase getProject() {
        return helper.getProject();
    }

    protected final CsmProject getCsmProject() {
        return helper.getProject();
    }

    protected final void resetProject() {
        helper.resetProject();
    }

    protected final CsmModel getModel() {
        return helper.getModel();
    }

    protected void preSetUp() throws Exception {
        // init flags needed for file model tests before creating TraceModel
    }

    protected void postSetUp() throws Exception {
        // init flags needed for file model tests
    }

    protected final void initParsedProject() throws Exception {
        File projectDir = getTestCaseDataDir();
        helper.initParsedProject(projectDir.getAbsolutePath());
    }

    protected final FileImpl getFileImpl(File file) {
        return helper.getProject().getFile(file, true);
    }

    protected final void reparseFile(CsmFile file) {
        if (file instanceof FileImpl) {
            ((FileImpl) file).markReparseNeeded(true);
            try {
                file.scheduleParsing(true);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    protected final FileImpl findFile(String name) throws Exception{
        ProjectBase project = this.getProject();
        if (project != null) {
            String toCompare = File.separator + name;
            for (FileImpl file : project.getAllFileImpls()) {
                if (file.getAbsolutePath().toString().endsWith(toCompare)) {
                    return file;
                }
            }
        }
        assertTrue("CsmFile not found for " + name, false);
        return null;
    }
    
    @Override
    protected void setUp() throws Exception {
        preSetUp();
        super.setUp();
        super.clearWorkDir();
        helper = new TestModelHelper(cleanCache);
        assertNotNull("Model must be valid", getTraceModel().getModel()); // NOI18N
        postSetUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        helper.shutdown(true);
    }

    protected final void performModelTest(File testFile, PrintStream streamOut, PrintStream streamErr) throws Exception {
        performModelTest(new String[]{testFile.getAbsolutePath()}, streamOut, streamErr);
    }

    protected final void performModelTest(String[] args, PrintStream streamOut, PrintStream streamErr) throws Exception {
        getTraceModel().test(args, streamOut, streamErr);
    }

    protected void doTest(String[] args, PrintStream streamOut, PrintStream streamErr, Object... params) throws Exception {
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        try {
            // redirect output and err
            System.setOut(streamOut);
            System.setErr(streamErr);
            performModelTest(args, streamOut, streamErr);
            postTest(args, params);
        } finally {
            // restore err and out
            System.setOut(oldOut);
            System.setErr(oldErr);
        }
    }

    /*
     * Used to filter out messages that may differ on different machines
     */
    protected static class FilteredPrintStream extends PrintStream {
        public FilteredPrintStream(File file) throws FileNotFoundException {
            super(file);
        }

        @Override
        public void println(String s) {
            if (s==null || !s.startsWith("Java Accessibility Bridge for GNOME loaded.")) {
                super.println(s);
            }
        }
    }
    
    protected void postTest(String[] args, Object... params) {
        
    }

    protected void performPreprocessorTest(String source) throws Exception {
        performPreprocessorTest(source, source + ".dat", source + ".err");
    }

    protected void performPreprocessorTest(String source, String goldenDataFileName, String goldenErrFileName, Object... params) throws Exception {
        String flags = "-oG"; // NOI18N
        File testFile = getDataFile(source);
        performTest(new String[]{flags, testFile.getAbsolutePath()}, goldenDataFileName, goldenErrFileName, params);
    }

    protected void performTest(String[] source, String goldenNameBase, Object... params) throws Exception {
        String[] absFiles = new String[source.length];
        for (int i = 0; i < source.length; i++) {
            absFiles[i] = getDataFile(source[i]).getAbsolutePath();            
        }
        String goldenDataFileName = goldenNameBase + ".dat";
        String goldenErrFileName = goldenNameBase + ".err";
        performTest(absFiles, goldenDataFileName, goldenErrFileName, params);
    }

    protected void performTest(String source, String goldenDataFileName, String goldenErrFileName, Object... params) throws Exception {
        File testFile = getDataFile(source);
        assertTrue("no test file " + testFile.getAbsolutePath(), testFile.exists());
        performTest(new String[]{testFile.getAbsolutePath()}, goldenDataFileName, goldenErrFileName, params);
    }

    protected void performTest(String[] args, String goldenDataFileName, String goldenErrFileName, Object... params) throws Exception {
        File workDir = getWorkDir();

        File output = new File(workDir, goldenDataFileName);
        PrintStream streamOut = new PrintStream(output);
        File error = goldenErrFileName == null ? null : new File(workDir, goldenErrFileName);
        PrintStream streamErr = goldenErrFileName == null ? null : new FilteredPrintStream(error);
        try {
            doTest(args, streamOut, streamErr, params);
        } finally {
            // restore err and out
            streamOut.close();
            if (streamErr != null) {
                streamErr.close();
            }
        }
        //System.out.println("finished testing " + testFile);
        boolean errTheSame = true;
        File goldenErrFile = null;
        File goldenErrFileCopy = null;
        File diffErrorFile = null;
        // first of all check err, because if not failed (often) => dat diff will be created
        if (goldenErrFileName != null) {
            goldenErrFile = getGoldenFile(goldenErrFileName);
            if (goldenErrFile.exists()) {
                if (CndCoreTestUtils.diff(error, goldenErrFile, null)) {
                    errTheSame = false;
                    // copy golden
                    goldenErrFileCopy = new File(workDir, goldenErrFileName + ".golden");
                    CndCoreTestUtils.copyToWorkDir(goldenErrFile, goldenErrFileCopy); // NOI18N
                    diffErrorFile = new File(workDir, goldenErrFileName + ".diff");
                    CndCoreTestUtils.diff(error, goldenErrFile, diffErrorFile);
                }
            } else {
                // golden err.file doesn't exist => err.file should be empty
                errTheSame = (error.length() == 0);
            }
        }

        boolean outTheSame = true;
        File goldenDataFile = getGoldenFile(goldenDataFileName);
        File goldenDataFileCopy = null;
        File diffOutputFile = null;
        if (CndCoreTestUtils.diff(output, goldenDataFile, null)) {
            outTheSame = false;
            // copy golden
            goldenDataFileCopy = new File(workDir, goldenDataFileName + ".golden");
            CndCoreTestUtils.copyToWorkDir(goldenDataFile, goldenDataFileCopy); // NOI18N
            diffOutputFile = new File(workDir, goldenDataFileName + ".diff");
            CndCoreTestUtils.diff(output, goldenDataFile, diffOutputFile);
        }
        if (outTheSame) {
            if (!errTheSame) {
                if (goldenErrFile.exists()) {
                    StringBuilder buf = new StringBuilder("ERR Difference - check: diff " + error + " " + goldenErrFileCopy);
                    showDiff(diffErrorFile, buf);
                    assertTrue(buf.toString(), false); // NOI18N
                } else {
                    assertTrue("ERR Difference - error should be emty: " + error, false); // NOI18N
                }
            }
        } else if (errTheSame) {
            StringBuilder buf = new StringBuilder("OUTPUT Difference - check: diff " + output + " " + goldenDataFileCopy);
            showDiff(diffOutputFile, buf);
            assertTrue(buf.toString(), outTheSame); // NOI18N
        } else {
            StringBuilder buf = new StringBuilder("ERR and OUTPUT are different, see content of folder " + workDir);
            showDiff(diffErrorFile, buf);
            showDiff(diffOutputFile, buf);
            assertTrue(buf.toString(), false); // NOI18N
        }
    }
}
