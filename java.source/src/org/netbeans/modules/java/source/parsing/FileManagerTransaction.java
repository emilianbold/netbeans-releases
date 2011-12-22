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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.indexing.TransactionContext;
import org.netbeans.modules.java.source.util.Iterators;

/**
 *
 * @author Tomas Zezula
 */
public abstract class FileManagerTransaction extends TransactionContext.Service {

    private FileManagerTransaction() {}

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
    
    public static FileManagerTransaction writeBack() {
        return new WriteBack();
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
    
    /**
     * Simple storage for cached file content, which can be flushed from time to time
     * when e.g. memory is exhausted, or too much content is generated.
     */
    static class Storage {
        Map<String, Map<File, CachedFileObject>> contentCache = new HashMap<String, Map<File, CachedFileObject>>();
        
        void addFile(String packageName, CachedFileObject fo) {
            
            Map<File, CachedFileObject> dirContent = contentCache.get(packageName);
            if (dirContent == null) {
                dirContent = new HashMap<File, CachedFileObject>();
                contentCache.put(packageName, dirContent);
            }
            dirContent.put(toFile(fo), fo);
        }
        
        Collection<File>  listDir(String dir) {
            Map<File, CachedFileObject> content = contentCache.get(dir);
            return content == null ? Collections.<File>emptyList() : content.keySet();
        }
        
        /**
         * This method makes a copy of the storage
         * @param dir
         * @return 
         */
        Collection<JavaFileObject> getFileObjects(String dir) {
            Map<File, CachedFileObject> content = contentCache.get(dir);
            return new ArrayList<JavaFileObject>(content.values());
        }
        
        void clear() {
            contentCache.clear();
        }
        
        /**
         * Flushes contents of the storage
         * @throws IOException 
         */
        void flush() throws IOException {
            for (Map<File, CachedFileObject> dirContent : contentCache.values()) {
                for (CachedFileObject cfo : dirContent.values()) {
                    cfo.flush();
                }
            }
            contentCache.clear();
        }
        
        CachedFileObject getFileObject(String dir, String file) {
            Map<File, CachedFileObject> content = contentCache.get(dir);
            for (Map.Entry<File, CachedFileObject> en : content.entrySet()) {
                if (file.equals(en.getKey().getName())) {
                    return en.getValue();
                }
            }
            return null;
        }
    }
    
    /**
     * Helper that extracts j.o.File from the JFO instance
     * @param o JFO instance
     * @return the File represented by the JFO
     */
    private static File toFile(JavaFileObject o) {
        File f = ((FileObjects.FileBase)o).f;
        return f;
    }

    private static class WriteBack extends FileManagerTransaction {

        private final Set<File> deleted;
        private final Storage   storage;

        private WriteBack () {
            deleted = new HashSet<File>();
            storage = new Storage();
        }

        @Override
        public void delete (@NonNull final File file) {
            assert file != null;
            deleted.add(file);
        }

        @Override
        @NonNull
        Iterable<JavaFileObject> filter(String packageName, Iterable<JavaFileObject> files) {
            final Collection<File>    added = storage.listDir(packageName);

            Iterable<JavaFileObject> res = files;
            if (deleted.isEmpty() && added.isEmpty()) {
                return res;
            }

            if (added.isEmpty()) {
                // just filter out the deleted files
                return Iterators.filter (
                    res,
                    new Comparable<JavaFileObject>() {
                        public int compareTo(@NonNull final JavaFileObject o) {
                            final File f = toFile(o);
                            return deleted.contains(f) ? 0 : -1;
                        }
                    }
                );
            }
            Collection<JavaFileObject> toAdd = storage.getFileObjects(packageName);
            Collection<Iterable<JavaFileObject>> chain = new ArrayList<Iterable<JavaFileObject>>(2);
            chain.add(toAdd);
            chain.add(deleted.isEmpty()?
                res:
                Iterators.filter (
                    res,
                    new Comparable<JavaFileObject>() {
                        public int compareTo(@NonNull final JavaFileObject o) {
                            final File f = toFile(o);
                            return deleted.contains(f) ? 0 : -1;
                        }
                    }
            ));
            return Iterators.chained(chain);
        }
        
        JavaFileObject  createFileObject(final @NonNull File file, final @NonNull File root,
            final @NullAllowed JavaFileFilterImplementation filter, final @NullAllowed Charset encoding) {
            final String[] pkgNamePair = FileObjects.getFolderAndBaseName(
                    FileObjects.getRelativePath(root,file),File.separatorChar);

            String pname = FileObjects.convertFolder2Package(pkgNamePair[0], File.separatorChar);
            CachedFileObject cfo = new CachedFileObject(
                    this, file, 
                    pname, pkgNamePair[1], filter, encoding);
            
            storage.addFile(pname, cfo);
            return cfo;
        }

        @Override
        protected void commit() throws IOException {
            try {
                for (File f : deleted) {
                    f.delete();
                }
                storage.flush();
            } finally {
                clean();
            }
        }

        @Override
        protected void rollBack() throws IOException {
            clean();
        }

        private void clean() {
            deleted.clear();
            storage.clear();
        }

        @Override
        JavaFileObject readFileObject(String dirName, String relativeName) {
            return storage.getFileObject(dirName, relativeName);
        }
    }

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
    
    /**
     * File object, whose contents is cached in memory and eventually flushed to the disk.
     * When flushed, methods will delegate to a plain regular FileObject over j.o.File,
     * so dangling references still work
     */
    static class CachedFileObject extends FileObjects.FileBase {
        static final byte[] NOTHING = new byte[0];
        
        byte[] content = NOTHING;
        WriteBack   writer;
        FileObjects.FileBase    delegate;
        
        public CachedFileObject(WriteBack wb, File file, String pkgName, String name, JavaFileFilterImplementation filter, Charset encoding) {
            super(file, pkgName, name, filter, encoding);
            this.writer = wb;
        }
        
        /**
         * Computes root of the file hierarchy given an output file and package name. Goes
         * up one directory for each package name segment (delimited by .)
         * 
         * @param startFrom the class/resource output file
         * @param pkgName package name of the class/resource
         * @return File representing the root of the package structure
         */
        private static File getRootFile(File startFrom, String pkgName) {
            int index = -1;
            
            while ((index = pkgName.indexOf('.', index + 1)) != -1) {
                startFrom = startFrom.getParentFile();
            }
            return startFrom;
        }

        /**
         * Flushes buffered content and releases this object. 
         * 
         * @throws IOException if file cannot be written, or the target directory created
         */
        void flush() throws IOException {
            // create directories up to the parent
            if (!f.getParentFile().mkdirs() && !f.getParentFile().exists()) {
                throw new IOException();
            }
            final FileOutputStream out = new FileOutputStream(f);
            try {
                out.write(content);
                release();
            } finally {
                out.close();
            }
        }
        
        /**
         * Releases data held by the cache, and redirects all calls to a regular
         * File-based FileObject
         */
        void release() {
            content = null;
            writer = null;
            delegate = (FileObjects.FileBase)FileObjects.fileFileObject(getFile(), getRootFile(getFile(), getPackage()), filter, encoding);
        }

        @Override
        public boolean delete() {
            if (delegate != null) {
                return delegate.delete();
            } else {
                writer.delete(toFile(this));
                return true;
            }
        }

        @Override
        public InputStream openInputStream() throws IOException {
            if (delegate != null) {
                return delegate.openInputStream();
            } else {
                return new ByteArrayInputStream(content);
            }
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            if (delegate != null) {
                return delegate.openOutputStream();
            } else {
                return new ByteArrayOutputStream() {
                    @Override
                    public void close() throws IOException {
                        super.close();
                        content = toByteArray();
                    }
                };
            }
        }
    }
}
