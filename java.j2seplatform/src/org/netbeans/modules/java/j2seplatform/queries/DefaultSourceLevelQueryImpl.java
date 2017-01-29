/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.java.j2seplatform.queries;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Parameters;

/**
 * Returns the source level for the non projectized java/class files (those
 * file for which the classpath is provided by the {@link DefaultClassPathProvider}
 * @author Tomas Zezula
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.queries.SourceLevelQueryImplementation.class, position=10000)
public class DefaultSourceLevelQueryImpl implements SourceLevelQueryImplementation {

    private static final String JAVA_EXT = "java";  //NOI18N
    private static final String MODULE_INFO = "module-info";  //NOI18N
    private static final SpecificationVersion JDK9 = new SpecificationVersion("9");

    public DefaultSourceLevelQueryImpl() {}

    public String getSourceLevel(final FileObject javaFile) {
        assert javaFile != null : "javaFile has to be non null";   //NOI18N
        String ext = javaFile.getExt();
        if (JAVA_EXT.equalsIgnoreCase (ext)) {
            final JavaPlatform jp = JavaPlatformManager.getDefault().getDefaultPlatform();
            assert jp != null : "JavaPlatformManager.getDefaultPlatform returned null";     //NOI18N
            SpecificationVersion ver = jp.getSpecification().getVersion();
            if (JDK9.compareTo(ver) > 0 && isModular(javaFile)) {
                return JDK9.toString();
            } else {
                return ver.toString();
            }
        }
        return null;
    }

    private static boolean isModular(final FileObject javaFile) {
        //Module-info always modular
        if (MODULE_INFO.equals(javaFile.getName())) {
            return true;
        }
        //Try to find module-info
        FileObject root = Optional.ofNullable(ClassPath.getClassPath(javaFile, ClassPath.SOURCE))
                .map((scp) -> scp.findOwnerRoot(javaFile))
                .orElseGet(() -> {
                    final String pkg = parsePackage(javaFile);
                    final String[] pkgElements = pkg.isEmpty() ?
                            new String[0] :
                            pkg.split("\\.");   //NOI18N
                    FileObject owner = javaFile.getParent();
                    for (int i = 0; owner != null && i < pkgElements.length; i++) {
                        owner = owner.getParent();
                    }
                    return owner;
                });
        return root != null &&
                root.getFileObject(MODULE_INFO, JAVA_EXT) != null;
    }

    @NonNull
    private static String parsePackage(@NonNull final FileObject javaFile) {
        String pkg = "";    //NOI18N
        try {
            JavacTask jt = (JavacTask) ToolProvider.getSystemJavaCompiler().getTask(
                    null,
                    null,
                    null,
                    Collections.EMPTY_LIST,
                    Collections.EMPTY_LIST,
                    Collections.singleton(new JFO(javaFile)));
            final Iterator<? extends CompilationUnitTree> cus = jt.parse().iterator();
            if (cus.hasNext()) {
                pkg = Optional.ofNullable(cus.next().getPackage())
                    .map((pt) -> pt.getPackageName())
                    .map((xt) -> xt.toString())
                    .orElse(pkg);
            }
        } catch (IOException ioe) {
            //TODO: Log & pass
        }
        return pkg;
    }

    private static final class JFO implements JavaFileObject {
        private final FileObject delegate;

        JFO(@NonNull final FileObject delegate) {
            Parameters.notNull("delegate", delegate);   //NOI18N
            this.delegate = delegate;
        }

        @Override
        public Kind getKind() {
            return Kind.SOURCE;
        }

        @Override
        public boolean isNameCompatible(String simpleName, Kind kind) {
            return delegate.getName().equals(simpleName) && getKind() == kind;
        }

        @Override
        public NestingKind getNestingKind() {
            return NestingKind.TOP_LEVEL;
        }

        @Override
        public Modifier getAccessLevel() {
            return null;
        }

        @Override
        public URI toUri() {
            return delegate.toURI();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public InputStream openInputStream() throws IOException {
            return delegate.getInputStream();
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            return delegate.getOutputStream();
        }

        @Override
        public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
            return new InputStreamReader(openInputStream(), encoding());
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            final StringBuilder content = new StringBuilder();
            char[] data = new char[1<<12];
            try(Reader in = openReader(ignoreEncodingErrors)) {
                for (int len = in.read(data, 0, data.length); len > 0; len = in.read(data, 0, data.length)) {
                    content.append(data, 0, len);
                }
            }
            return content;
        }

        @Override
        public Writer openWriter() throws IOException {
            return new OutputStreamWriter(openOutputStream(), encoding());
        }

        @Override
        public long getLastModified() {
            return delegate.lastModified().getTime();
        }

        @Override
        public boolean delete() {
            try {
                delegate.delete();
                return true;
            } catch (IOException ioe) {
                return false;
            }
        }

        private Charset encoding() {
            return FileEncodingQuery.getEncoding(delegate);
        }
    }
}
