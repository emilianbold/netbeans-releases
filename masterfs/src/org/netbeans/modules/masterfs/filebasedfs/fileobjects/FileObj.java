/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import org.netbeans.modules.masterfs.filebasedfs.Statistics;
import org.netbeans.modules.masterfs.filebasedfs.children.ChildrenCache;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.netbeans.modules.masterfs.filebasedfs.utils.FSException;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Enumerations;
import org.openide.util.Mutex;

/**
 * @author rm111737
 */
public class FileObj extends BaseFileObj {
    static final long serialVersionUID = -1133540210876356809L;
    private long lastModified = -1;
    private boolean realLastModifiedCached;


    FileObj(final File file, final FileNaming name) {
        super(file, name);
        setLastModified(System.currentTimeMillis());        
    }

    public OutputStream getOutputStream(final FileLock lock) throws IOException {
        return getOutputStream(lock, null);
    }
    
    public OutputStream getOutputStream(final FileLock lock, ProvidedExtensions extensions) throws IOException {
        final File f = getFileName().getFile();

        if (extensions != null) {
            extensions.beforeChange(this);
        }        
        final MutualExclusionSupport.Closeable closable = MutualExclusionSupport.getDefault().addResource(this, false);
        
        FileOutputStream retVal = null;
        try {
            retVal = new FileOutputStream(f) {
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
            if (!f.exists()) {
                fex = (FileNotFoundException)new FileNotFoundException(e.getLocalizedMessage()).initCause(e);
            } else if (!f.canWrite()) {
                fex = (FileNotFoundException)new FileNotFoundException(e.getLocalizedMessage()).initCause(e);
            } else if (f.getParentFile() == null) {
                fex = (FileNotFoundException)new FileNotFoundException(e.getLocalizedMessage()).initCause(e);
            } else if (!f.getParentFile().exists()) {
                fex = (FileNotFoundException)new FileNotFoundException(e.getLocalizedMessage()).initCause(e);
            } 
            FSException.annotateException(fex);            
            throw fex;
        }
        return retVal;
    }

    public InputStream getInputStream() throws FileNotFoundException {
        final File f = getFileName().getFile();
                        
        InputStream inputStream;
        MutualExclusionSupport.Closeable closeableReference = null;
        
        try {
            final MutualExclusionSupport.Closeable closable = MutualExclusionSupport.getDefault().addResource(this, true);
            closeableReference = closable;
            inputStream = new FileInputStream(f) {
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
            if (!f.exists()) {
                fex = (FileNotFoundException)new FileNotFoundException(e.getLocalizedMessage()).initCause(e);
            } else if (!f.canRead()) {
                fex = (FileNotFoundException)new FileNotFoundException(e.getLocalizedMessage()).initCause(e);
            } else if (f.getParentFile() == null) {
                fex = (FileNotFoundException)new FileNotFoundException(e.getLocalizedMessage()).initCause(e);
            } else if (!f.getParentFile().exists()) {
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

    public final Date lastModified() {
        final File f = getFileName().getFile();
        return new Date(f.lastModified());
    }

    private final void setLastModified(long lastModified) {
        if (this.lastModified != -1 && !realLastModifiedCached) {
            realLastModifiedCached = true;
        }
        this.lastModified = lastModified;
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
        return lastModified != 0;
    }

    protected void setValid(boolean valid) {
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

    public void refresh(final boolean expected, boolean fire) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.REFRESH_FILE);
        stopWatch.start();                
        if (isValid()) {
            final long oldLastModified = lastModified;
            boolean isReal = realLastModifiedCached;
            setLastModified(getFileName().getFile().lastModified());
            boolean isModified = (isReal) ? 
                (oldLastModified != lastModified) : (oldLastModified < lastModified);
            if (fire && oldLastModified != -1 && lastModified != -1 && lastModified != 0 && isModified) {
                fireFileChangedEvent(expected);
            }
            
            boolean validityFlag = getFileName().getFile().exists();                    
            if (!validityFlag) {
                //fileobject is invalidated
                FolderObj parent = getExistingParent();
                if (parent != null) {
                    ChildrenCache childrenCache = parent.getChildrenCache();
                    final Mutex.Privileged mutexPrivileged = (childrenCache != null) ? childrenCache.getMutexPrivileged() : null;
                    if (mutexPrivileged != null) mutexPrivileged.enterWriteAccess();
                    try {
                        childrenCache.getChild(getFileName().getFile().getName(),true);
                    } finally {
                        if (mutexPrivileged != null) mutexPrivileged.exitWriteAccess();
                    }
                    
                }
                setValid(false);
                if (fire) {
                    fireFileDeletedEvent(expected);
                }                
            }            
        }                 
        stopWatch.stop();
    }        

    public final void refresh(final boolean expected) {
        refresh(expected, true);
    }
    

    

    public final Enumeration getChildren(final boolean rec) {
        return Enumerations.empty();
    }

    public final Enumeration getFolders(final boolean rec) {
        return Enumerations.empty();
    }

    public final Enumeration getData(final boolean rec) {
        return Enumerations.empty();
    }


    public final FileLock lock() throws IOException {
        final File me = getFileName().getFile();
        try {            
            boolean lightWeightLock = false;
            BaseFileObj bfo = getExistingParent();            
            if (bfo instanceof FolderObj) {
                lightWeightLock = ((FolderObj)bfo).isLightWeightLockRequired();
            }
            return WriteLockFactory.tryLock(me, lightWeightLock);
        } catch (FileNotFoundException ex) {
            FileNotFoundException fex = ex;                        
            if (!me.exists()) {
                fex = (FileNotFoundException)new FileNotFoundException(ex.getLocalizedMessage()).initCause(ex);
            } else if (!me.canRead()) {
                fex = (FileNotFoundException)new FileNotFoundException(ex.getLocalizedMessage()).initCause(ex);
            } else if (!me.canWrite()) {
                fex = (FileNotFoundException)new FileNotFoundException(ex.getLocalizedMessage()).initCause(ex);
            } else if (me.getParentFile() == null) {
                fex = (FileNotFoundException)new FileNotFoundException(ex.getLocalizedMessage()).initCause(ex);
            } else if (!me.getParentFile().exists()) {
                fex = (FileNotFoundException)new FileNotFoundException(ex.getLocalizedMessage()).initCause(ex);
            }                                                             
            FSException.annotateException(fex);            
            throw fex;
        }
    }

    final boolean checkLock(final FileLock lock) throws IOException {
        final File f = getFileName().getFile();
        return ((lock instanceof WriteLock) && (((WriteLock) lock).isValid(f)));
    }

    public void rename(final FileLock lock, final String name, final String ext, ProvidedExtensions.IOHandler handler) throws IOException {
        super.rename(lock, name, ext, handler);
        setLastModified(getFileName().getFile().lastModified());
    }
}
