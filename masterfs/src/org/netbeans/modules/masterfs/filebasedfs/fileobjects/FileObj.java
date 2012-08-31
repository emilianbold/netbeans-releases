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

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.awt.EventQueue;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.netbeans.modules.masterfs.filebasedfs.utils.FSException;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.netbeans.modules.masterfs.filebasedfs.utils.Utils;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Enumerations;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

/**
 * @author rm111737
 */
public class FileObj extends BaseFileObj {
    static final long serialVersionUID = -1133540210876356809L;
    private static final MutualExclusionSupport<FileObj> MUT_EXCL_SUPPORT = new MutualExclusionSupport<FileObj>();
    private long lastModified = -1;
    private boolean realLastModifiedCached;
    private static final Logger LOGGER = Logger.getLogger(FileObj.class.getName());


    FileObj(final File file, final FileNaming name) {
        super(file, name);
        setLastModified(System.currentTimeMillis(), null, false);
    }
    @Override
    protected boolean noFolderListeners() {
        FolderObj p = getExistingParent();
        return p == null ? true : p.noFolderListeners();
    }

    public OutputStream getOutputStream(final FileLock lock) throws IOException {
        ProvidedExtensions extensions = getProvidedExtensions();
        File file = getFileName().getFile();
        if (!Utilities.isWindows() && !file.isFile()) {
            throw new IOException(file.getAbsolutePath());
        }
        return getOutputStream(lock, extensions, this);
    }
    
    @Messages(
        "EXC_INVALID_FILE=File {0} is not valid"
    )
    public OutputStream getOutputStream(final FileLock lock, ProvidedExtensions extensions, FileObject mfo) throws IOException {
        if (LOGGER.isLoggable(Level.FINE) && EventQueue.isDispatchThread()) {
            LOGGER.log(Level.WARNING, "writing " + this, new IllegalStateException("getOutputStream invoked in AWT"));
        }
        final File f = getFileName().getFile();
        if (!isValid()) {
            FileNotFoundException fnf = new FileNotFoundException("FileObject " + this + " is not valid; isFile=" + f.isFile()); //NOI18N
            Exceptions.attachLocalizedMessage(fnf, Bundle.EXC_INVALID_FILE(this));
            throw fnf;
        }

        if (!Utilities.isWindows() && !f.isFile()) {
            throw new IOException(f.getAbsolutePath());
        }
        
        final MutualExclusionSupport<FileObj>.Closeable closable = MUT_EXCL_SUPPORT.addResource(this, false);

        if (extensions != null) {
            extensions.beforeChange(mfo);
        }
        
        FileOutputStream retVal = null;
        try {
            retVal = new FileOutputStream(f) {

                @Override
                public void close() throws IOException {
                    if (!closable.isClosed()) {
                        super.close();
                        LOGGER.log(Level.FINEST, "getOutputStream-close");
                        setLastModified(f.lastModified(), f, false);
                        closable.close();
                        fireFileChangedEvent(false);
                    }
                }
            };
        } catch (FileNotFoundException e) {
            if (closable != null) {
                closable.close();
            }
            FileNotFoundException fex = e;                        
            if (!FileChangedManager.getInstance().exists(f)) {
                fex = (FileNotFoundException)new FileNotFoundException(e.getLocalizedMessage()).initCause(e);
            } else if (!f.canWrite()) {
                fex = (FileNotFoundException)new FileNotFoundException(e.getLocalizedMessage()).initCause(e);
            } else if (f.getParentFile() == null) {
                fex = (FileNotFoundException)new FileNotFoundException(e.getLocalizedMessage()).initCause(e);
            } else if (!FileChangedManager.getInstance().exists(f.getParentFile())) {
                fex = (FileNotFoundException)new FileNotFoundException(e.getLocalizedMessage()).initCause(e);
            } 
            FSException.annotateException(fex);            
            throw fex;
        }
        return retVal;
    }

    public InputStream getInputStream() throws FileNotFoundException {
        if (LOGGER.isLoggable(Level.FINE) && EventQueue.isDispatchThread()) {
            LOGGER.log(Level.WARNING, "reading " + this, new IllegalStateException("getInputStream invoked in AWT"));
        }
        if (!isValid()) {
            throw new FileNotFoundException("FileObject " + this + " is not valid.");  //NOI18N
        }
        LOGGER.log(Level.FINEST,"FileObj.getInputStream_after_is_valid");   //NOI18N - Used by unit test
        final File f = getFileName().getFile();
        if (!f.exists()) {
            FileNotFoundException ex = new FileNotFoundException("Can't read " + f); // NOI18N
            dumpFileInfo(f, ex);
            throw ex;
        }
        InputStream inputStream;
        MutualExclusionSupport<FileObj>.Closeable closeableReference = null;
        
        try {
            if (Utilities.isWindows()) {
                // #157056 - don't try to open locked windows files (ntuser.dat, ntuser.dat.log1, ...)
                if (getNameExt().toLowerCase().startsWith("ntuser.dat")) {  //NOI18N
                    return new ByteArrayInputStream(new byte[] {});
                }
            } else if (!f.isFile()) {
                return new ByteArrayInputStream(new byte[] {});
            }
            final MutualExclusionSupport<FileObj>.Closeable closable = MUT_EXCL_SUPPORT.addResource(this, true);
            closeableReference = closable;            
            inputStream = new FileInputStream(f) {

                @Override
                public void close() throws IOException {
                    super.close();
                    closable.close();
                }
            };
        } catch (IOException e) {
            if (closeableReference != null) {
                closeableReference.close();    
            }
            
            FileNotFoundException fex = null;                        
            if (!FileChangedManager.getInstance().exists(f)) {
                fex = (FileNotFoundException)new FileNotFoundException(e.getLocalizedMessage()).initCause(e);
            } else if (!f.canRead()) {
                fex = (FileNotFoundException)new FileNotFoundException(e.getLocalizedMessage()).initCause(e);
            } else if (f.getParentFile() == null) {
                fex = (FileNotFoundException)new FileNotFoundException(e.getLocalizedMessage()).initCause(e);
            } else if (!FileChangedManager.getInstance().exists(f.getParentFile())) {
                fex = (FileNotFoundException)new FileNotFoundException(e.getLocalizedMessage()).initCause(e);
            } else if ((new FileInfo(f)).isUnixSpecialFile()) {
                fex = (FileNotFoundException) new FileNotFoundException(e.toString()).initCause(e);
            } else {
                fex = (FileNotFoundException) new FileNotFoundException(e.toString()).initCause(e);
            }                        
            FSException.annotateException(fex);
            throw fex;
        }
        assert inputStream != null;
        return inputStream;
    }

    @Override
    public boolean isReadOnly() {
        final File f = getFileName().getFile();
        boolean res;
        if (!Utilities.isWindows() && !f.isFile()) {
            res = true;
        } else {
            res = super.isReadOnly();
        }
        markReadOnly(res);
        return res;
    }

    @Override
    public boolean canWrite() {
        final File f = getFileName().getFile();        
        if (!Utilities.isWindows() && !f.isFile()) {
            return false;
        }                
        return super.canWrite();
    }
        
    final void setLastModified(long lastModified, File forFile, boolean readOnly) {
        if (this.getLastModified() != 0) { // #130998 - don't set when already invalidated
            if (this.getLastModified() != -1 && !realLastModifiedCached) {
                realLastModifiedCached = true;
            }
            if (LOGGER.isLoggable(Level.FINER)) {
                Exception trace = LOGGER.isLoggable(Level.FINEST) ? new Exception("StackTrace") : null; // NOI18N
                LOGGER.log(Level.FINER, "setLastModified: " + this.getLastModified() + " -> " + lastModified + " (" + this + ") on " + forFile, trace);  //NOI18N
            }
            this.setLastModified(lastModified, readOnly);
        }
    }
    
    
    public final FileObject createFolder(final String name) throws IOException {
        throw new IOException(getPath());//isn't directory - cannot create neither file nor folder
    }

    public final FileObject createData(final String name, final String ext) throws IOException {
        throw new IOException(getPath());//isn't directory - cannot create neither file nor folder
    }


    public final FileObject[] getChildren() {
        return new FileObject[]{};//isn't directory - no children
    }

    public final FileObject getFileObject(final String name, final String ext) {
        return null;
    }

    @Override
    public boolean isValid() {
        //0 - because java.io.File.lastModififed returns 0 for not existing files        
        boolean retval = getLastModified() != 0;
        //assert checkCacheState(retval, getFileName().getFile());
        return retval && super.isValid();
    }

    protected void setValid(boolean valid) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "setValid: " + valid + " (" + this + ")", new Exception("Stack trace"));  //NOI18N
        }
        if (valid) {
            //I can't make valid fileobject when it was one invalidated
            assert isValid() : this.toString();
        } else {
            //0 - because java.io.File.lastModififed returns 0 for not existing files
            setLastModified(0, true);
        }        
    }

    public final boolean isFolder() {
        return false;
    }

    @Override
    public void refreshImpl(final boolean expected, boolean fire) {
        final long oldLastModified = getLastModified();
        final boolean isReadOnly = thinksReadOnly();
        boolean isReal = realLastModifiedCached;
        final File file = getFileName().getFile();
        setLastModified(file.lastModified(), file, !file.canWrite());
        boolean isModified = (isReal) ? (oldLastModified != getLastModified()) : (oldLastModified < getLastModified());
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.log(
                Level.FINER,
                "refreshImpl for {0} isReal: {1} isModified: {2} oldLastModified: {3} lastModified: {4}",
                new Object[]{
                    this, isReal, isModified, oldLastModified, getLastModified()}
            );
        }
        if (fire && oldLastModified != -1 && getLastModified() != -1 && getLastModified() != 0 && isModified) {
            if (!MUT_EXCL_SUPPORT.isBeingWritten(this)) {
                fireFileChangedEvent(expected);
            }
        }
        if (fire && isReal && isReadOnly != thinksReadOnly() && getLastModified() != 0) {
            // #129178 - event consumed in org.openide.text.DataEditorSupport and used to change editor read-only state
            fireFileAttributeChangedEvent("DataEditorSupport.read-only.refresh", null, null);  //NOI18N
        }
    }
    
    @Override
    public final void refresh(final boolean expected) {
        refresh(expected, true);
    }
    

    

    @Override
    public final Enumeration<FileObject> getChildren(final boolean rec) {
        return Enumerations.empty();
    }

    @Override
    public final Enumeration<FileObject> getFolders(final boolean rec) {
        return Enumerations.empty();
    }

    @Override
    public final Enumeration<FileObject> getData(final boolean rec) {
        return Enumerations.empty();
    }


    @Override
    public final FileLock lock() throws IOException {
        final File me = getFileName().getFile();
        if (!getProvidedExtensions().canWrite(me)) {
            FSException.io("EXC_CannotLock", me);
        }
        try {            
            LockForFile result = LockForFile.tryLock(me);
            try {
                getProvidedExtensions().fileLocked(this);
            } catch (IOException ex) {
                result.releaseLock(false);
                throw ex;
            }
            return result;
        } catch (FileNotFoundException ex) {
            FileNotFoundException fex = ex;                        
            if (!FileChangedManager.getInstance().exists(me)) {
                fex = (FileNotFoundException)new FileNotFoundException(ex.getLocalizedMessage()).initCause(ex);
            } else if (!me.canRead()) {
                fex = (FileNotFoundException)new FileNotFoundException(ex.getLocalizedMessage()).initCause(ex);
            } else if (!me.canWrite()) {
                fex = (FileNotFoundException)new FileNotFoundException(ex.getLocalizedMessage()).initCause(ex);
            } else if (me.getParentFile() == null) {
                fex = (FileNotFoundException)new FileNotFoundException(ex.getLocalizedMessage()).initCause(ex);
            } else if (!FileChangedManager.getInstance().exists(me.getParentFile())) {
                fex = (FileNotFoundException)new FileNotFoundException(ex.getLocalizedMessage()).initCause(ex);
            }                                                             
            FSException.annotateException(fex);            
            throw fex;
        }
    }

    @Override
    public final boolean isLocked() {
        final File me = getFileName().getFile();
        final LockForFile l = LockForFile.findValid(me);
        return l != null && l.isValid();
    }

    final boolean checkLock(final FileLock lock) throws IOException {
        final File f = getFileName().getFile();
        return ((lock instanceof LockForFile) && Utils.equals(((LockForFile) lock).getFile(), f));
    }

    @Override
    public void rename(final FileLock lock, final String name, final String ext, ProvidedExtensions.IOHandler handler) throws IOException {
        super.rename(lock, name, ext, handler);
        final File rename = getFileName().getFile();
        setLastModified(rename.lastModified(), rename, !rename.canWrite());
    }

    private long getLastModified() {
        long l = lastModified;
        if (l < -10) {
            return -l;
        }
        return l;
    }

    private void setLastModified(long lastModified, boolean readOnly) {
        if (lastModified >= -10 && lastModified < 10) {
            this.lastModified = lastModified;
            return;
        }
        this.lastModified = readOnly ? -lastModified : lastModified;
    }
    
    private boolean thinksReadOnly() {
        return lastModified < -10;
    }
    
    private void markReadOnly(boolean readOnly) {
        if (thinksReadOnly() != readOnly) {
            setLastModified(getLastModified(), readOnly);
        }
    }
}
