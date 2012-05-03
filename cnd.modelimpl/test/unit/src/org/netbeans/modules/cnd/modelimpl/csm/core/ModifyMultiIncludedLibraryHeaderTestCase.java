/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmProject;

/**
 *
 * @author Vladimir Voskresensky
 */
public class ModifyMultiIncludedLibraryHeaderTestCase extends ModifyDocumentTestCaseBase {
    public ModifyMultiIncludedLibraryHeaderTestCase(String testName) {
        super(testName);
//        System.setProperty("DeepReparsingUtils.level", "FINEST");
//        System.setProperty("cnd.modelimpl.use.deep.reparsing.trace", "true");
//        System.setProperty("cnd.modelimpl.timing.per.file.flat", "true");
//        System.setProperty("cnd.modelimpl.timing", "true");
    }

    @Override
    protected File[] changeDefProjectDirBeforeParsingProjectIfNeeded(File projectDir) {
        // we have following structure for this test
        // test-folder
        //  --mainSources\
        //        main.cpp
        //  --libraryFolder\
        //        hello1.h
        //        hello1.cpp
        //
        // so, adjust used folders

        File libraryDir = new File(projectDir, "libraryFolder");
        File srcDir = new File(projectDir, "mainSources");
        checkDir(libraryDir);
        checkDir(srcDir);
        super.setSysIncludes(srcDir.getAbsolutePath(), Collections.singletonList(libraryDir.getAbsolutePath()));
        super.setLibProjectsPaths(srcDir.getAbsolutePath(), Collections.singletonList(libraryDir.getAbsolutePath()));
        return new File[]{libraryDir, srcDir};
    }

    public void test210816() throws Exception {
        // #210816 - unresolved identifiers due to partial reparse
        ProjectBase libProject = (ProjectBase) super.getProject("project_libraryFolder");
        assertNotNull(libProject);
        assertEquals("two files are expected " + libProject.getAllFiles(), 2, libProject.getAllFiles().size());
        ProjectBase srcProject = (ProjectBase) super.getProject("project_mainSources");
        assertNotNull(srcProject);
        assertEquals("only one file is expected " + srcProject.getAllFiles(), 1, srcProject.getAllFiles().size());
        List<CsmProject> srcLibs = srcProject.getLibraries();
        Collection<ProjectBase> dependentProjects = libProject.getDependentProjects();
        assertEquals("one dependent Project is expected " + dependentProjects, 1, dependentProjects.size());
        assertEquals("one library is expected " + srcLibs, 1, srcLibs.size());
        assertEquals("mainSources project have to be in dependent list of library" + dependentProjects, srcProject, dependentProjects.iterator().next());
        assertEquals("library is expected to be in list of mainSources dependencies" + srcLibs, libProject, srcLibs.iterator().next());
        final File sourceFile = getDataFile("mainSources/main.cpp");
        final File file2Check = getDataFile("libraryFolder/hello1.h");
        super.insertTextThenSaveAndCheck(sourceFile, 1, "#define EMPTY_MACRO\n",
                file2Check, new IZ210816Checker(), true);
    }

    private static final class IZ210816Checker implements Checker {

        @Override
        public void checkBeforeModifyingFile(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException {
            checkDeclarationNumber(0, fileToCheck);
            checkDeadBlocks(fileToCheck.getProject(), fileToCheck, "1. text before:", doc, "File must have " + 1 + " dead code block ", 1);
        }

        @Override
        public void checkAfterModifyingFile(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException {
            // here state can be intermediate, we will not check it
        }

        @Override
        public void checkAfterParseFinished(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException {
            CsmProject libProject = fileToCheck.getProject();
            libProject.waitParse();
            checkDeclarationNumber(1, fileToCheck);
            checkDeadBlocks(fileToCheck.getProject(), fileToCheck, "2. text after:", doc, "File must have " + 0 + " dead code block ", 0);
        }

        @Override
        public void checkAfterUndo(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException {
            // here state can be intermediate, we will not check it
        }

        @Override
        public void checkAfterUndoAndParseFinished(FileImpl modifiedFile, FileImpl fileToCheck, CsmProject project, BaseDocument doc) throws BadLocationException {
            // should be the same as before start of modifications
            checkBeforeModifyingFile(modifiedFile, fileToCheck, project, doc);
        }
    }
}
