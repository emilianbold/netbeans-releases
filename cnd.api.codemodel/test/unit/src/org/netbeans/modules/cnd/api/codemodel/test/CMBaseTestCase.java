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
package org.netbeans.modules.cnd.api.codemodel.test;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import org.netbeans.modules.cnd.api.codemodel.CMCursor;
import org.netbeans.modules.cnd.api.codemodel.CMDiagnostic;
import org.netbeans.modules.cnd.api.codemodel.CMFile;
import org.netbeans.modules.cnd.api.codemodel.CMIndex;
import org.netbeans.modules.cnd.api.codemodel.CMSourceLocation;
import org.netbeans.modules.cnd.api.codemodel.CMSourceRange;
import org.netbeans.modules.cnd.api.codemodel.CMTranslationUnit;
import org.netbeans.modules.cnd.api.codemodel.CMUnifiedSymbolResolution;
import org.netbeans.modules.cnd.api.codemodel.providers.VisitQueryTest;
import org.netbeans.modules.cnd.api.codemodel.visit.CMDeclaration;
import org.netbeans.modules.cnd.api.codemodel.visit.CMEntityReference;
import org.netbeans.modules.cnd.api.codemodel.visit.CMInclude;
import org.netbeans.modules.cnd.api.codemodel.visit.CMVisitQuery;
import org.netbeans.modules.cnd.spi.codemodel.providers.CMCompilationDataBase;
import org.netbeans.modules.cnd.spi.codemodel.support.SPIUtilities;
import org.netbeans.modules.cnd.spi.codemodel.support.SimpleCompilationDataBase;
import org.netbeans.modules.cnd.spi.codemodel.trace.CMTraceUtils;
import org.netbeans.modules.cnd.test.CndBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.nativeexecution.test.RcFile;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author Vladimir Kvashin
 */
public abstract class CMBaseTestCase extends CndBaseTestCase {

    public static interface TestPerformer {
        public void perform(File... sources) throws Exception;
    }

    public CMBaseTestCase(String testName) {
        super(testName);
    }

    protected void performTest(TestPerformer testPerformer, String... sources) throws Exception {
        SimpleCompilationDataBase.Builder builder = new SimpleCompilationDataBase.Builder();
        builder.setBaseDir(getTestCaseDataDir());
        builder.setDefaultCompileCommand(getDefaultCompileCommand());
        for (String path : sources) {
            builder.addEntry(path);
        }
        SimpleCompilationDataBase cmdb = builder.createDataBase();
        performTest(testPerformer, cmdb);
    }

    protected void performTest(TestPerformer testPerformer, CMCompilationDataBase cdb) throws Exception {
        File[] sourceFiles = new File[cdb.getEntries().size()];
        int i = 0;
        for (CMCompilationDataBase.Entry entry : cdb.getEntries()) {
            sourceFiles[i++] = Utilities.toFile(entry.getFile());
        }
        try {
            CMIndex index = SPIUtilities.parse(cdb);
            SPIUtilities.registerIndex(cdb, index);
            testPerformer.perform(sourceFiles);
        } finally {
            SPIUtilities.unregisterIndex(cdb);
        }
    }

    protected String getDefaultCompileCommand() {
        try {
            RcFile rcFile = NativeExecutionTestSupport.getRcFile();
            String compileCommand = rcFile.get("clang", "defaultCompileCommand", "");
            compileCommand += " -I" + getTestCaseDataDir();
            return compileCommand;
        } catch (IOException | RcFile.FormatException ex) {
            Exceptions.printStackTrace(ex);
        }
        return ""; //  -I/usr/include";
    }

    protected void printTestIndexCallback(VisitQueryTest.TestIndexCallback callback, boolean printToRef) {
        PrintStream ps;
        if (printToRef) {
            ps = getRef();
        } else {
            ps = System.out;
        }
        List<CMDiagnostic> diagnostics = callback.getDiagnostics();
        if (!diagnostics.isEmpty()) {
            ps.printf("Diagnostics:\n");
            for (CMDiagnostic diag : diagnostics) {
                ps.printf("%s\n", CMTraceUtils.toString(diag));
            }
        }
        List<CMInclude> includes = callback.getIncludes();
        if (!includes.isEmpty()) {
            ps.printf("Includes:\n");
            for (CMInclude inc : includes) {
                ps.printf("%s\n", CMTraceUtils.toString(inc));
            }
        }
        List<CMDeclaration> declarations = callback.getDeclarations();
        if (!declarations.isEmpty()) {
            ps.printf("Declarations:\n");
            for (CMDeclaration decl : declarations) {
                ps.printf("%s\n", CMTraceUtils.toString(decl));
            }
        }
        List<CMEntityReference> references = callback.getReferences();
        if (!references.isEmpty()) {
            ps.printf("References:\n");
            for (CMEntityReference ref : references) {
                ps.printf("%s\n", CMTraceUtils.toString(ref));
            }
        }
    }

    protected static CMUnifiedSymbolResolution findReferencedUSR(File sourceFile, int lineIndex, int colIndex) {

        assertTrue("File " + sourceFile.getAbsolutePath() + " does not exist", sourceFile.exists());
        assertTrue("File " + sourceFile.getAbsolutePath() + " is not a plain file", sourceFile.isFile());

        URI uri = Utilities.toURI(sourceFile);

        Collection<CMTranslationUnit> translationUnits = SPIUtilities.getTranslationUnits(uri);
        assertFalse("empty translation units", translationUnits.isEmpty());

        CMTranslationUnit tu = translationUnits.iterator().next();
        assertNotNull("Null translation unit", tu);

        CMFile file = tu.getFile(uri);
        assertNotNull("Null CMFile", file);

        CMSourceLocation loc = tu.getLocation(file, lineIndex, colIndex);
        assertNotNull("Null location", loc);

        CMCursor cursor = tu.getCursor(loc);
        assertNotNull("Null cursor", cursor);

        CMCursor referencedEntityCursor = cursor.getReferencedEntityCursor();
        assertNotNull("Null referenced entity cursor", referencedEntityCursor);

        CMUnifiedSymbolResolution usr = referencedEntityCursor.getUSR();
        assertNotNull("Null USR", cursor);

        return usr;
    }

    public static class TestPrintingCursorAndRangeVisitor implements CMVisitQuery.CursorAndRangeVisitor {

        private final PrintStream out;
        private final CMVisitQuery.CursorAndRangeVisitor visitor;

        public TestPrintingCursorAndRangeVisitor(CMVisitQuery.CursorAndRangeVisitor testVisitior, PrintStream out) {
            this.out = out;
            this.visitor = testVisitior;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean visit(CMCursor cur, CMSourceRange curRange) {
            if (visitor != null) {
                visitor.visit(cur, curRange);
            }
            out.printf("%s [range=%s]\n", 
                    CMTraceUtils.toString(cur),
                    CMTraceUtils.toString(curRange));
            return true;
        }

    }
    public static class TestIndexCallback implements CMVisitQuery.IndexCallback {

        private List<CMDiagnostic> diagnostics = new ArrayList<>();
        private List<CMInclude> includes = new ArrayList<>();
        private List<CMDeclaration> declarations = new ArrayList<>();
        private List<CMEntityReference> references = new ArrayList<>();

        public List<CMDeclaration> getDeclarations() {
            return declarations;
        }

        public List<CMDiagnostic> getDiagnostics() {
            return diagnostics;
        }

        public List<CMInclude> getIncludes() {
            return includes;
        }

        public List<CMEntityReference> getReferences() {
            return references;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public void onDiagnostics(Iterable<CMDiagnostic> diagnostics) {
            for (CMDiagnostic d : diagnostics) {
                this.diagnostics.add(d);
            }
        }

        @Override
        public void onIndclude(CMInclude include) {
            this.includes.add(include);
        }

        @Override
        public void onTranslationUnit() {
        }

        @Override
        public void onDeclaration(CMDeclaration decl) {
            declarations.add(decl);
        }

        @Override
        public void onReference(CMEntityReference ref) {
            references.add(ref);
        }
    }

}
