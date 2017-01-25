/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.java.source.parsing;

import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.util.Options;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.indexing.TransactionContext;
import org.netbeans.modules.java.source.usages.ClassIndexEventsTransaction;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Zezula
 */
public class ModuleOraculumTest extends NbTestCase {

    private FileObject wd;
    private FileObject moduleFile;
    private FileObject javaFile;

    public ModuleOraculumTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        moduleFile = createModule(wd, "Test");  //NOI18N
        javaFile = createClass(wd, "org.nb.Test");  //NOI18N
        MockServices.setServices(CPP.class, COQ.class);
    }

    public void testOraculumLibrarySourceWithRoot() {
        final ClasspathInfo cpInfo = new ClasspathInfo.Builder(ClassPath.EMPTY).build();
        final JavacParser parser = new JavacParser(Collections.emptyList(), true);
        final JavacTaskImpl impl = JavacParser.createJavacTask(
                javaFile,
                wd,
                cpInfo,
                parser,
                null,
                null,
                false);
        assertNotNull(impl);
        final Options opts = Options.instance(impl.getContext());
        assertNotNull(opts);
        assertEquals("Test", opts.get("-Xmodule:"));    //NOI18N
    }

    public void testOraculumLibrarySourceWithoutRootWithSourcePath() {
        Lookup.getDefault().lookup(CPP.class).add(
                wd,
                ClassPath.SOURCE,
                ClassPathSupport.createClassPath(wd));
        final ClasspathInfo cpInfo = new ClasspathInfo.Builder(ClassPath.EMPTY).build();
        final JavacParser parser = new JavacParser(Collections.emptyList(), true);
        final JavacTaskImpl impl = JavacParser.createJavacTask(
                javaFile,
                null,
                cpInfo,
                parser,
                null,
                null,
                false);
        assertNotNull(impl);
        final Options opts = Options.instance(impl.getContext());
        assertNotNull(opts);
        assertEquals("Test", opts.get("-Xmodule:"));    //NOI18N
    }

    public void testOraculumLibrarySourceWithoutRootWithoutSourcePath() {
        final ClasspathInfo cpInfo = new ClasspathInfo.Builder(ClassPath.EMPTY).build();
        final JavacParser parser = new JavacParser(Collections.emptyList(), true);
        final JavacTaskImpl impl = JavacParser.createJavacTask(
                javaFile,
                null,
                cpInfo,
                parser,
                null,
                null,
                false);
        assertNotNull(impl);
        final Options opts = Options.instance(impl.getContext());
        assertNotNull(opts);
        assertNull(opts.get("-Xmodule:"));    //NOI18N
    }

    public void testOraculumLibrarySourceNoModuleInfo() throws IOException {
        moduleFile.delete();
        final ClasspathInfo cpInfo = new ClasspathInfo.Builder(ClassPath.EMPTY).build();
        final JavacParser parser = new JavacParser(Collections.emptyList(), true);
        final JavacTaskImpl impl = JavacParser.createJavacTask(
                javaFile,
                wd,
                cpInfo,
                parser,
                null,
                null,
                false);
        assertNotNull(impl);
        final Options opts = Options.instance(impl.getContext());
        assertNotNull(opts);
        assertNull(opts.get("-Xmodule:"));    //NOI18N
    }

    public void testOraculumProjectSource() throws IOException {
        scan(wd);
        final ClasspathInfo cpInfo = new ClasspathInfo.Builder(ClassPath.EMPTY).build();
        final JavacParser parser = new JavacParser(Collections.emptyList(), true);
        final JavacTaskImpl impl = JavacParser.createJavacTask(
                javaFile,
                wd,
                cpInfo,
                parser,
                null,
                null,
                false);
        assertNotNull(impl);
        final Options opts = Options.instance(impl.getContext());
        assertNotNull(opts);
        assertNull(opts.get("-Xmodule:"));    //NOI18N
    }

    public void testOraculumLibrarySourceWithRootExpliciteXModule() throws IOException {
        Lookup.getDefault().lookup(COQ.class)
                .forRoot(wd)
                .apply("-Xmodule:SomeModule");  //NOI18N
        final ClasspathInfo cpInfo = new ClasspathInfo.Builder(ClassPath.EMPTY).build();
        final JavacParser parser = new JavacParser(Collections.emptyList(), true);
        final JavacTaskImpl impl = JavacParser.createJavacTask(
                javaFile,
                wd,
                cpInfo,
                parser,
                null,
                null,
                false);
        assertNotNull(impl);
        final Options opts = Options.instance(impl.getContext());
        assertNotNull(opts);
        assertEquals("SomeModule", opts.get("-Xmodule:"));    //NOI18N
    }

    private static void scan(@NonNull final FileObject root) throws IOException {
        final TransactionContext ctx = TransactionContext.beginTrans()
                .register(ClassIndexEventsTransaction.class, ClassIndexEventsTransaction.create(true));
        try {
            ClassIndexImpl ci = ClassIndexManager.getDefault().createUsagesQuery(root.toURL(), true);
            if (ci == null) {
                throw new IllegalStateException();
            }
            ci.setState(ClassIndexImpl.State.INITIALIZED);
        } finally {
            ctx.commit();
        }
    }

    @NonNull
    private static FileObject createModule(
            @NonNull final FileObject root,
            @NonNull final String moduleName) throws IOException {
        final String content = String.format(
                "module %s {}", //NOI18N
                moduleName);
        return createFile(
                root,
                "",
                "module-info.java",
                content);
    }

    @NonNull
    private static FileObject createClass(
            @NonNull final FileObject root,
            @NonNull final String clzName) throws IOException {
        final String[] pnp = FileObjects.getPackageAndName(clzName);
        final String content = String.format(
                "package %s;\nclass %s {}", //NOI18N
                pnp[0],
                pnp[1]);
        return createFile(
                root,
                FileObjects.convertPackage2Folder(pnp[0]),
                pnp[1],
                content);
    }

    @NonNull
    private static FileObject createFile(
            @NonNull final FileObject root,
            @NonNull final String folder,
            @NonNull final String name,
            @NonNull final String content) throws IOException {
        final FileObject fld;
        if (!folder.isEmpty()) {
            fld = FileUtil.createFolder(root, folder);
        } else {
            fld = root;
        }
        final FileObject file = FileUtil.createData(fld, name);
        try(PrintWriter out = new PrintWriter(new OutputStreamWriter(file.getOutputStream(), "UTF-8"))) {   //NOI18N
            out.println(content);
        }
        return file;
    }

    public static final class CPP implements ClassPathProvider {
        private final Map<FileObject,Map<String,ClassPath>> paths = new HashMap<>();

        @NonNull
        CPP clear() {
            paths.clear();
            return this;
        }

        @NonNull
        public CPP add(
                @NonNull final FileObject root,
                @NonNull final String type,
                @NonNull final ClassPath cp) {
            paths.computeIfAbsent(root, (r) -> new HashMap<>())
                    .put(type,cp);
            return this;
        }

        @Override
        public ClassPath findClassPath(FileObject file, String type) {
            for (Map.Entry<FileObject,Map<String,ClassPath>> e : paths.entrySet()) {
                final FileObject root = e.getKey();
                if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                    return e.getValue().get(type);
                }
            }
            return null;
        }
    }

    public static final class COQ implements CompilerOptionsQueryImplementation {
        private final Map<FileObject,List<String>> options = new HashMap<>();

        @NonNull
        <T extends Function<String,T>> Function<String,T> forRoot(@NonNull FileObject root) {
            return new Function<String,T>() {
                @Override
                public T apply(String t) {
                    options.computeIfAbsent(root, (r) -> new ArrayList<>())
                            .add(t);
                    return (T) this;
                }
            };
        }

        @Override
        public Result getOptions(FileObject file) {
            for (Map.Entry<FileObject,List<String>> e : options.entrySet()) {
                final FileObject root = e.getKey();
                if (root.equals(file) || FileUtil.isParentOf(root, file)) {
                    return new Result() {
                        @Override
                        public List<? extends String> getArguments() {
                            return e.getValue();
                        }

                        @Override
                        public void addChangeListener(ChangeListener listener) {
                        }

                        @Override
                        public void removeChangeListener(ChangeListener listener) {
                        }
                    };
                }
            }
            return null;
        }
    }
}
