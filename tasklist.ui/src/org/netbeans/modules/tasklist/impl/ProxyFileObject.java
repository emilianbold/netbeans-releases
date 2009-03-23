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

package org.netbeans.modules.tasklist.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Enumerations;

/**
 *
 * @author sa
 */
public class ProxyFileObject extends FileObject {

    private final FileObject root;
    private final Indexable idx;
    private FileObject proxy;

    ProxyFileObject( FileObject root, Indexable idx ) {
        this.root = root;
        this.idx = idx;
    }

    private FileObject getProxy() {
        synchronized( this ) {
            if( null == proxy ) {
                proxy = root.getFileObject(idx.getRelativePath());
            }
        }
        return proxy;
    }

    @Override
    public String getName() {
        return idx.getName();
    }

    @Override
    public String getExt() {
        return FileUtil.getExtension(idx.getRelativePath());
    }

    @Override
    public void rename(FileLock lock, String name, String ext) throws IOException {
        throw new IOException("operation not supported");
    }

    @Override
    public FileSystem getFileSystem() throws FileStateInvalidException {
        return getProxy().getFileSystem();
    }

    @Override
    public FileObject getParent() {
        return getProxy().getParent();
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    @Override
    public Date lastModified() {
        return new Date(idx.getLastModified());
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public boolean isData() {
        return true;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void delete(FileLock lock) throws IOException {
        throw new IOException("operation not supported");
    }

    @Override
    public Object getAttribute(String attrName) {
        return null;
    }

    @Override
    public void setAttribute(String attrName, Object value) throws IOException {
    }

    @Override
    public Enumeration<String> getAttributes() {
        return Enumerations.empty();
    }

    @Override
    public void addFileChangeListener(FileChangeListener fcl) {
    }

    @Override
    public void removeFileChangeListener(FileChangeListener fcl) {
    }

    @Override
    public long getSize() {
        return getProxy().getSize();
    }

    @Override
    public InputStream getInputStream() throws FileNotFoundException {
        try {
            return idx.openInputStream();
        } catch( FileNotFoundException ex ) {
            throw ex;
        } catch( IOException ex ) {
            throw new RuntimeException(ex);
//            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public OutputStream getOutputStream(FileLock lock) throws IOException {
        throw new IOException("operation not supported");
    }

    @Override
    public FileLock lock() throws IOException {
        throw new IOException("operation not supported");
    }

    @Override
    public void setImportant(boolean b) {
    }

    @Override
    public FileObject[] getChildren() {
        return new FileObject[0];
    }

    @Override
    public FileObject getFileObject(String name, String ext) {
        return getProxy().getFileObject(name, ext);
    }

    @Override
    public FileObject createFolder(String name) throws IOException {
        return getProxy().createFolder(name);
    }

    @Override
    public FileObject createData(String name, String ext) throws IOException {
        return getProxy().createData(name, ext);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if( obj instanceof ProxyFileObject )
            obj = ((ProxyFileObject)obj).getProxy();
        return getProxy().equals(obj);
    }

    @Override
    public int hashCode() {
        return getProxy().hashCode();
    }
}
