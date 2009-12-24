/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jackpot.file.test;

import org.netbeans.modules.java.hints.jackpot.file.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.java.hints.jackpot.file.test.TestParser.TestCase;
import org.netbeans.modules.java.hints.jackpot.spi.HintDescription;
import org.netbeans.modules.java.hints.jackpot.spi.HintsRunner;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 *
 * @author lahvac
 */
public class TestPerformer {

    public static Map<TestCase, Collection<String>> performTest(FileObject ruleFile, FileObject test, TestCase[] tests, AtomicBoolean cancel) throws Exception {
        try {
            return performTestImpl(ruleFile, test, tests, cancel);
        } finally {
            setData(null, null);
        }
    }

    public static String normalize(String text) {
        return text.replaceAll("[ \t\n]+", " ");
    }
    
    private static File createScratchpadDir() throws IOException {
        String userdir = System.getProperty("netbeans.user");

        assert userdir != null;

        File varTmp = new File(new File(new File(userdir), "var"), "tmp");

        varTmp.mkdirs();

        assert varTmp.isDirectory();

        File sp = File.createTempFile("jackpot", "", varTmp);

        sp.delete();

        sp.mkdir();

        assert sp.isDirectory();

        return sp;
    }

    private static Map<TestCase, Collection<String>> performTestImpl(FileObject ruleFile, FileObject test, TestCase[] tests, final AtomicBoolean cancel) throws Exception {
        final List<HintDescription> hints = DeclarativeHintRegistry.parseHintFile(ruleFile);
        FileObject scratchPad = FileUtil.toFileObject(createScratchpadDir());
        Map<TestCase, Collection<String>> result = new HashMap<TestCase, Collection<String>>();

        for (int cntr = 0; cntr < tests.length; cntr++) {
            FileObject srcRoot = scratchPad.createFolder("src" + cntr);
            FileObject src = FileUtil.createData(srcRoot, "test/Test.java");

            setData(test, srcRoot);

            copyStringToFile(src, tests[cntr].getCode());

            final List<ErrorDescription> errors = new LinkedList<ErrorDescription>();

            JavaSource.forFileObject(src).runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController parameter) throws Exception {
                    parameter.toPhase(Phase.RESOLVED);

                    errors.addAll(HintsRunner.computeErrors(parameter, hints, cancel));
                }
            }, true);

            LinkedList<String> currentResults = new LinkedList<String>();

            result.put(tests[cntr],currentResults);

            for (ErrorDescription ed : errors) {
                if (!ed.getFixes().isComputed()) {
                    throw new UnsupportedOperationException();
                }

                for (Fix f : ed.getFixes().getFixes()) {
                    currentResults.add(getFixResult(src, f));
                }

                if (ed.getFixes().getFixes().isEmpty()) {
                    currentResults.add(ed.getDescription() + ":" + ed.getRange().getText() + "\n");
                }
            }
        }

        //intentionally keeping the directory in case an exception occurs, to
        //simplify error diagnostics
        scratchPad.delete();

        return result;
    }

    /**
     * Copies a string to a specified file.
     *
     * @param f the {@link FilObject} to use.
     * @param content the contents of the returned file.
     * @return the created file
     */
    private final static FileObject copyStringToFile (FileObject f, String content) throws Exception {
        OutputStream os = f.getOutputStream();
        InputStream is = new ByteArrayInputStream(content.getBytes("UTF-8"));
        FileUtil.copy(is, os);
        os.close ();
        is.close();

        return f;
    }

    private static void setData(FileObject from, FileObject sourceRoot) {
        for (ClassPathProvider cpp : Lookup.getDefault().lookupAll(ClassPathProvider.class)) {
            if (cpp instanceof TestClassPathProvider) {
                ((TestClassPathProvider) cpp).setData(from, sourceRoot);
            }
        }
    }

    private static String getFixResult(FileObject src, Fix fix) throws Exception {
        String original = getText(src);

        fix.implement();

        String nue = getText(src);

        copyStringToFile(src, original);

        return nue;
    }

    private static String getText(FileObject file) throws IOException {
        Charset encoding = FileEncodingQuery.getEncoding(file);

        return new String(file.asBytes(), encoding.displayName());
    }

    @ServiceProviders({
        @ServiceProvider(service=ClassPathProvider.class),
        @ServiceProvider(service=SourceLevelQueryImplementation.class)
    })
    public static final class TestClassPathProvider implements ClassPathProvider, SourceLevelQueryImplementation {

        private FileObject from;
        private FileObject sourceRoot;

        public synchronized ClassPath findClassPath(FileObject file, String type) {
            if (from == null) {
                return null;
            }

            if (sourceRoot.equals(file) || FileUtil.isParentOf(sourceRoot, file)) {
                return ClassPath.getClassPath(from, type);
            }

            return null;
        }

        synchronized void setData(FileObject from, FileObject sourceRoot) {
            this.from = from;
            this.sourceRoot = sourceRoot;
        }

        public String getSourceLevel(FileObject file) {
            if (from == null) {
                return null;
            }

            if (sourceRoot.equals(file) || FileUtil.isParentOf(sourceRoot, file)) {
                return "1.5"; //TODO
            }

            return null;
        }
        
    }
}
