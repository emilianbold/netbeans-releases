/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.netbeans.modules.masterfs.filebasedfs.utils.FSException;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Enumerations;
import org.openide.util.Utilities;

/**
 * @author rm111737
 */
public class FileObj extends BaseFileObj {
    static final long serialVersionUID = -1133540210876356809L;
    private long lastModified = -1;
    private boolean realLastModifiedCached;
    private static final Logger LOGGER = Logger.getLogger(FileObj.class.getName());


    FileObj(final File file, final FileNaming name) {
        super(file, name);
        setLastModified(System.currentTimeMillis());        
    }

    public OutputStream getOutputStream(final FileLock lock) throws IOException {
        ProvidedExtensions extensions = getProvidedExtensions();
        File file = getFileName().getFile();
        if (!Utilities.isWindows() && !file.isFile()) {
            throw new IOException(file.getAbsolutePath());
        }
        return getOutputStream(lock, extensions, this);
    }
    
    public OutputStream getOutputStream(final FileLock lock, ProvidedExtensions extensions, FileObject mfo) throws IOException {
        if (!isValid()) {
            throw new FileNotFoundException("FileObject " + this + " is not valid."); //NOI18N
        }

        final File f = getFileName().getFile();

        if (!Utilities.isWindows() && !f.isFile()) {
            throw new IOException(f.getAbsolutePath());
        }
        
        if (extensions != null) {
            extensions.beforeChange(mfo);
        }        
        final MutualExclusionSupport.Closeable closable = MutualExclusionSupport.getDefault().addResource(this, false);
        
        FileOutputStream retVal = null;
        try {
            retVal = new FileOutputStream(f) {

                @Override
                public void close() throws IOException {
                    if (!closable.isClosed()) {
                        super.close();
                        closable.close();
                        setLastModified(f.lastModified());
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
        if (!isValid()) {
            throw new FileNotFoundException("FileObject " + this + " is not valid.");  //NOI18N
        }
        LOGGER.log(Level.FINEST,"FileObj.getInputStream_after_is_valid");   //NOI18N - Used by unit test
        final File f = getFileName().getFile();
        if (!f.exists()) {
            throw new FileNotFoundException();
        }
        InputStream inputStream;
        MutualExclusionSupport.Closeable closeableReference = null;
        
        try {
            if (Utilities.isWindows()) {
                // #157056 - don't try to open locked windows files (ntuser.dat, ntuser.dat.log1, ...)
                if (getNameExt().toLowerCase().startsWith("ntuser.dat")) {  //NOI18N
                    return new ByteArrayInputStream(new byte[] {});
                }
            } else if (!f.isFile()) {
                return new ByteArrayInputStream(new byte[] {});
            }
            final MutualExclusionSupport.Closeable closable = MutualExclusionSupport.getDefault().addResource(this, true);
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
        if (!Utilities.isWindows() && !f.isFile()) {
            return true;
        }        
        return super.isReadOnly();
    }

    @Override
    public boolean canWrite() {
        final File f = getFileName().getFile();        
        if (!Utilities.isWindows() && !f.isFile()) {
            return false;
        }                
        return super.canWrite();
    }
        
    final void setLastModified(long lastModified) {
        if (this.lastModified != 0) { // #130998 - don't set when already invalidated
            if (this.lastModified != -1 && !realLastModifiedCached) {
                realLastModifiedCached = true;
            }
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.log(Level.FINEST, "setLastModified: " + this.lastModified + " -> " + lastModified + " (" + this + ")", new Exception("Stack trace"));  //NOI18N
            }
            this.lastModified = lastModified;
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

    public boolean isValid() {
        //0 - because java.io.File.lastModififed returns 0 for not existing files        
        boolean retval = lastModified != 0;
        //assert checkCacheState(retval, getFileName().getFile());
        return retval;
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
            lastModified = 0;
        }        
    }

    public final boolean isFolder() {
        return false;
    }

    public void refreshImpl(final boolean expected, boolean fire) {
        final long oldLastModified = lastModified;
        boolean isReal = realLastModifiedCached;
        setLastModified(getFileName().getFile().lastModified());
        boolean isModified = (isReal) ? (oldLastModified != lastModified) : (oldLastModified < lastModified);
        if (fire && oldLastModified != -1 && lastModified != -1 && lastModified != 0 && isModified) {
            fireFileChangedEvent(expected);
        }
        if (fire && lastModified != 0) {
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


    public final FileLock lock() throws IOException {
        final File me = getFileName().getFile();
        try {            
            final FileLock result = LockForFile.tryLock(me);
            getProvidedExtensions().fileLocked(this);
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

    final boolean checkLock(final FileLock lock) throws IOException {
        final File f = getFileName().getFile();
        return ((lock instanceof LockForFile) && (((LockForFile) lock).getFile().equals(f)));
    }

    @Override
    public void rename(final FileLock lock, final String name, final String ext, ProvidedExtensions.IOHandler handler) throws IOException {
        super.rename(lock, name, ext, handler);
        setLastModified(getFileName().getFile().lastModified());
    }    
}
