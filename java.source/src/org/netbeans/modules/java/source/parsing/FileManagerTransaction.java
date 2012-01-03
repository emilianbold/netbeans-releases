/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.parsing;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.indexing.TransactionContext;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomas Zezula
 */
public abstract class FileManagerTransaction extends TransactionContext.Service {

    protected FileManagerTransaction() {}

    public abstract void delete (@NonNull final File file);

    @NonNull
    abstract Iterable<JavaFileObject> filter (
        @NonNull String packageName,
        @NonNull Iterable<JavaFileObject> files);

    /**
     * Creates fileobject suitable for output. 
     * @param file file to produce
     * @param root root directory for the package structure
     * @param filter output filter
     * @param encoding desired file encoding
     * @return 
     */
    abstract JavaFileObject  createFileObject(final @NonNull File file, final @NonNull File root,
        final @NullAllowed JavaFileFilterImplementation filter, final @NullAllowed Charset encoding);

    /**
     * Looks up a FileObject suitable for reading. The method MAY return {@code null}, if the FileObject
     * is not part of the transaction.
     * 
     * @param dirName
     * @param relativeName
     * @return 
     */
    JavaFileObject  readFileObject(String dirName, String relativeName) {
        return null;
    }
    
    public static FileManagerTransaction writeBack(FileObject root) {
        return new WriteBackTransaction(root);
    }

    public static FileManagerTransaction writeThrough() {
        return new WriteThrogh();
    }

    public static FileManagerTransaction read() {
        return new Read();
    }
    
    static class JFOContent {
        JavaFileObject  jfo;
        byte[] content;

        public JFOContent(JavaFileObject jfo, byte[] content) {
            this.jfo = jfo;
            this.content = content;
        }
        
        
    }
    

            chain.add(deleted.isEmpty()?
                res:
                Iterators.filter (
                            return deleted.contains(f) ? 0 : -1;
    private static class WriteThrogh extends FileManagerTransaction {

        @Override
        public void delete (@NonNull final File file) {
            assert file != null;
            file.delete();
        }

        @Override
        @NonNull
        Iterable<JavaFileObject> filter(String packageName, @NonNull final Iterable<JavaFileObject> files) {
            return files;
        }

        @Override
        protected void commit() throws IOException {
        }

        @Override
        protected void rollBack() throws IOException {
            throw new UnsupportedOperationException("RollBack is unsupported"); //NOI18N
        }

        @Override
        JavaFileObject createFileObject(File file, File root, JavaFileFilterImplementation filter, Charset encoding) {
            return FileObjects.fileFileObject(file, root, filter, encoding);
        }
    }

    private static class Read extends FileManagerTransaction {

        @Override
        public void delete (@NonNull final File file) {
            throw new UnsupportedOperationException ("Delete not supported, read-only.");   //NOI18N
        }

        @Override
        @NonNull
        Iterable<JavaFileObject> filter(String packageName, @NonNull final Iterable<JavaFileObject> files) {
            return files;
        }

        @Override
        protected void commit() throws IOException {
            throw new UnsupportedOperationException ("Commit not supported, read-only.");   //NOI18N
        }

        @Override
        protected void rollBack() throws IOException {
            throw new UnsupportedOperationException ("RollBack not supported, read-only."); //NOi18N
        }

        @Override
        JavaFileObject createFileObject(File file, File root, JavaFileFilterImplementation filter, Charset encoding) {
            return FileObjects.fileFileObject(file, root, filter, encoding);
        }

    }
}
