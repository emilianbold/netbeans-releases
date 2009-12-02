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

package org.netbeans.modules.java.source.parsing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;


/**
 *
 * @author Tomas Zezula
 */
public class NullWriteAptSourceFileManager extends SourceFileManager {

    public NullWriteAptSourceFileManager (final @NonNull ClassPath aptRoots, final boolean ignoreExcludes) {
        super(aptRoots,ignoreExcludes);
    }

    @Override
    public javax.tools.FileObject getFileForOutput(Location l, String pkgName, String relativeName, javax.tools.FileObject sibling)
        throws IOException, UnsupportedOperationException, IllegalArgumentException {
        if (l != StandardLocation.SOURCE_OUTPUT) {
            throw new UnsupportedOperationException("Only apt output is supported."); // NOI18N
        }
        final String ext = FileObjects.getExtension(relativeName);
        final String name = FileObjects.stripExtension(relativeName);
        return new NullFileObject(relativeName, pkgName+'.'+name, FileObjects.getKind(ext));
    }


    @Override
    public JavaFileObject getJavaFileForOutput (Location l, String className, JavaFileObject.Kind kind, javax.tools.FileObject sibling) {
        if (l != StandardLocation.SOURCE_OUTPUT) {
            throw new UnsupportedOperationException("Only apt output is supported."); // NOI18N
        }
        return new NullFileObject(className,FileObjects.getBaseName(className, '.'),kind);
    }


    static class NullFileObject implements FileObjects.InferableJavaFileObject {

        private final JavaFileObject.Kind kind;
        private final String name;
        private final String binaryName;


        NullFileObject (final String name, final String binaryName, final JavaFileObject.Kind kind) {
            this.name = name;
            this.binaryName = binaryName;
            this.kind = kind;
        }


        public String inferBinaryName() {
            return binaryName;
        }

        public Kind getKind() {
            return kind;
        }

        public boolean isNameCompatible(String simpleName, Kind kind) {
            return this.kind == kind && name.equals(simpleName);
        }

        public NestingKind getNestingKind() {
            return null;
        }

        public Modifier getAccessLevel() {
            return null;
        }

        public URI toUri() {
            //todo: implement this if needed
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public String getName() {
            return name;
        }

        public InputStream openInputStream() throws IOException {
            throw new UnsupportedOperationException("Read not supported");
        }

        public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
            throw new UnsupportedOperationException("Read not supported");
        }

        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            throw new UnsupportedOperationException("Read not supported");
        }

        public OutputStream openOutputStream() throws IOException {
            return new NullOutputStream ();
        }


        public Writer openWriter() throws IOException {
            return new OutputStreamWriter(openOutputStream());
        }

        public long getLastModified() {
            return System.currentTimeMillis();
        }

        public boolean delete() {
            return true;
        }

    }

    static class NullOutputStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {
            //pass
        }

    }

}
