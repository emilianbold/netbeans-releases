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
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.File;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.modelimpl.test.ModelImplBaseTestCase;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelBase;
import org.netbeans.modules.cnd.support.Interrupter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test for reaction for external modifications
 */
public class ExternalModificationTest extends ModelImplBaseTestCase {

    private final static boolean verbose;


    static {
        verbose = Boolean.getBoolean("test.external.modification.verbose");
        if (verbose) {
            System.setProperty("cnd.modelimpl.trace.external.changes", "true");

            System.setProperty("cnd.modelimpl.timing", "true");
            System.setProperty("cnd.modelimpl.timing.per.file.flat", "true");
            System.setProperty("cnd.repository.listener.trace", "true");
            System.setProperty("cnd.trace.close.project", "true");
        }
    }

    public ExternalModificationTest(String testName) {
        super(testName);
    }

    public void testSimple() throws Exception {
        File workDir = getWorkDir();
        File sourceFile = new File(workDir, "test1.cc");
        String oldName = "foo1";
        String newName = "foo2";

        writeFile(sourceFile, "void " + oldName + "() {};");

        final TraceModelBase traceModel = new TraceModelBase(true);
        //traceModel.setUseSysPredefined(true);
        traceModel.processArguments(sourceFile.getAbsolutePath());
        //ModelImpl model = traceModel.getModel();
        //ModelSupport.instance().setModel(model);
        final CsmProject project = traceModel.getProject();

        project.waitParse();
        assertNotNull(oldName + " should be found", findDeclaration(oldName, project));

        overwriteFile(sourceFile, "void " + newName + "() {};");
        fireFileChanged(project, FileUtil.toFileObject(FileUtil.normalizeFile(sourceFile)));

        project.waitParse();
        assertNotNull(newName + " should be found", findDeclaration(newName, project));
        assertNull(oldName + " is found, while it should be absent", findDeclaration(oldName, project));
    }

    public void testReparseOfBrokenInclude() throws Exception {
        File workDir = getWorkDir();
        File sourceFile = new File(workDir, "test.cc");
        writeFile(sourceFile, "#include \"test.h\"\n");
        File headerFile = new File(workDir, "test.h");
        headerFile.delete();

        final TraceModelBase traceModel = new TraceModelBase(true);
        traceModel.processArguments(sourceFile.getAbsolutePath());
        final CsmProject project = traceModel.getProject();

        project.waitParse();
        CsmFile csmFile = project.findFile(sourceFile.getAbsolutePath(), true, false);
        assertNotNull(csmFile);
        assertEquals(1, csmFile.getIncludes().size());
        assertNull(csmFile.getIncludes().iterator().next().getIncludeFile());

        overwriteFile(headerFile, "void foo();\n");
        fireFileAdded(project, FileUtil.toFileObject(FileUtil.normalizeFile(headerFile)));

        project.waitParse();

        assertTrue("CsmFile is invalid", csmFile.isValid());
        assertFalse(csmFile.getIncludes().isEmpty());
        assertNotNull(csmFile.getIncludes().iterator().next().getIncludeFile());
    }

    public void testReparseOfBrokenInclude2() throws Exception {
        File workDir = getWorkDir();
        File sourceFile = new File(workDir, "test.cc");
        writeFile(sourceFile, "#include \"test1.h\"\n");
        File headerFile1 = new File(workDir, "test1.h");
        writeFile(headerFile1, "#include \"test2.h\"\n");
        File headerFile2 = new File(workDir, "test2.h");
        headerFile2.delete();

        final TraceModelBase traceModel = new TraceModelBase(true);
        traceModel.processArguments(sourceFile.getAbsolutePath());
        final CsmProject project = traceModel.getProject();

        project.waitParse();
        CsmFile csmFile = project.findFile(headerFile1.getAbsolutePath(), true, false);
        assertNotNull(csmFile);
        assertEquals(1, csmFile.getIncludes().size());
        assertNull(csmFile.getIncludes().iterator().next().getIncludeFile());

        writeFile(headerFile2, "void foo();\n");
        fireFileAdded(project, FileUtil.toFileObject(FileUtil.normalizeFile(headerFile2)));

        project.waitParse();

        assertTrue("CsmFile is invalid", csmFile.isValid());
        assertFalse(csmFile.getIncludes().isEmpty());
        assertNotNull(csmFile.getIncludes().iterator().next().getIncludeFile());
    }

    public void testDeadCodeBlocks() throws Exception {
        File workDir = getWorkDir();
        File sourceFile = new File(workDir, "test1.cc");
        String visibleFunction = "visibleFunction";
        String hiddenFunction = "hiddenFunction";
        String oldText =
                "\n" +
                "void " + visibleFunction + "() {\n" +
                "   int a;\n" +
                "#ifdef MACRO\n" +
                "   int hiddenVar = 0;\n" +
                "#endif\n" +
                "   int b;\n" +
                "}\n" +
                "\n" +
                "" +
                "#ifdef MACRO\n" +
                "void " + hiddenFunction + "() {\n" +
                "}\n" +
                "#endif\n";
        String newText =
                "#define MACRO\n" +
                "\n" +
                oldText;

        writeFile(sourceFile, oldText);

        final TraceModelBase traceModel = new TraceModelBase(true);
        traceModel.processArguments(sourceFile.getAbsolutePath());
        final CsmProject project = traceModel.getProject();

        project.waitParse();
        assertNotNull(visibleFunction + " should be found", findDeclaration(visibleFunction, project));
        assertNull(hiddenFunction + " should not be found", findDeclaration(hiddenFunction, project));
        CsmFile fileImpl = getCsmFile(sourceFile);
        checkDeadBlocks(project, fileImpl, "File must have one dead code block ", new int[][] {{4, 13, 5, 22}, {10, 13, 12, 2}});
        overwriteFile(sourceFile, newText);
        fireFileChanged(project, FileUtil.toFileObject(FileUtil.normalizeFile(sourceFile)));

        project.waitParse();
        assertNotNull(visibleFunction + " should be found", findDeclaration(visibleFunction, project));
        assertNotNull(hiddenFunction + " should be found", findDeclaration(hiddenFunction, project));
        checkDeadBlocks(project, fileImpl, "File must have one dead code block ", new int[][]{});

    }
    
    private void fireFileChanged(final CsmProject project, FileObject sourceFileObject) {
        ModelImplTest.fireFileChanged(project, sourceFileObject);
        sleep(500);
    }

    private void fireFileAdded(final CsmProject project, FileObject sourceFileObject) {
        ModelImplTest.fireFileAdded(project, sourceFileObject);
    }

    private void checkDeadBlocks(final CsmProject project, final CsmFile csmFile, String msg, int[][] expectedDeadBlocks) throws BadLocationException {
        // test for #185712: external modifications breaks dead blocks information in editor
        project.waitParse();
        List<CsmOffsetable> unusedCodeBlocks = CsmFileInfoQuery.getDefault().getUnusedCodeBlocks(csmFile, Interrupter.DUMMY);
        if (unusedCodeBlocks.isEmpty()) {
            System.err.println("NO DEAD BLOCKS");
        } else {
            for (int i = 0; i < unusedCodeBlocks.size(); i++) {
                CsmOffsetable csmOffsetable = unusedCodeBlocks.get(i);
                System.err.printf("DEAD BLOCK %d: [%d:%d-%d:%d]\n", i, csmOffsetable.getStartPosition().getLine(), csmOffsetable.getStartPosition().getColumn(),
                                                                        csmOffsetable.getEndPosition().getLine(), csmOffsetable.getEndPosition().getColumn());
                if (i < expectedDeadBlocks.length) {
                    assertEquals("different dead blocks start line ", expectedDeadBlocks[i][0], csmOffsetable.getStartPosition().getLine());
                    assertEquals("different dead blocks start column ", expectedDeadBlocks[i][1], csmOffsetable.getStartPosition().getColumn());
                    assertEquals("different dead blocks end line ", expectedDeadBlocks[i][2], csmOffsetable.getEndPosition().getLine());
                    assertEquals("different dead blocks end column ", expectedDeadBlocks[i][3], csmOffsetable.getEndPosition().getColumn());
                }
            }
        }
        assertEquals(msg + csmFile.getAbsolutePath(), expectedDeadBlocks.length, unusedCodeBlocks.size());
    }
}
