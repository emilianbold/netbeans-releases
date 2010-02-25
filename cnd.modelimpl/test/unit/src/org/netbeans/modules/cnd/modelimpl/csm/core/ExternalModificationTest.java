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
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.netbeans.modules.cnd.modelimpl.test.ModelImplBaseTestCase;
import org.netbeans.modules.cnd.modelimpl.trace.NativeProjectProvider;
import org.netbeans.modules.cnd.modelimpl.trace.NativeProjectProvider.NativeProjectImpl;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelBase;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * Test for reaction for external modifications
 * @author Vladimir Kvashin
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

        writeFile(sourceFile, "void " + newName + "() {};");
        fireFileChanged(project, sourceFile);

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
        CsmFile csmFile = project.findFile(sourceFile.getAbsolutePath(), false);
        assertNotNull(csmFile);
        assertEquals(1, csmFile.getIncludes().size());
        assertNull(csmFile.getIncludes().iterator().next().getIncludeFile());

        writeFile(headerFile, "void foo();\n");
        fireFileAdded(project, headerFile);

        project.waitParse();

        assertTrue("CsmFile is invalid", csmFile.isValid());
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
        CsmFile csmFile = project.findFile(headerFile1.getAbsolutePath(), false);
        assertNotNull(csmFile);
        assertEquals(1, csmFile.getIncludes().size());
        assertNull(csmFile.getIncludes().iterator().next().getIncludeFile());

        writeFile(headerFile2, "void foo();\n");
        fireFileAdded(project, headerFile2);

        project.waitParse();

        assertTrue("CsmFile is invalid", csmFile.isValid());
        assertNotNull(csmFile.getIncludes().iterator().next().getIncludeFile());
    }

    private void fireFileChanged(final CsmProject project, File sourceFile) {
        Object platform = project.getPlatformProject();
        if (platform instanceof NativeProjectProvider.NativeProjectImpl) {
            NativeProjectProvider.NativeProjectImpl nativeProject = (NativeProjectImpl) platform;
            nativeProject.fireFileChanged(sourceFile);
        }
    }

    private void fireFileAdded(final CsmProject project, File sourceFile) {
        Object platform = project.getPlatformProject();
        if (platform instanceof NativeProjectProvider.NativeProjectImpl) {
            NativeProjectProvider.NativeProjectImpl nativeProject = (NativeProjectImpl) platform;
            nativeProject.fireFileAdded(sourceFile);
        }
    }
}
