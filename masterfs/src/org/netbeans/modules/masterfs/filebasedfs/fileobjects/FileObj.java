/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import org.netbeans.modules.masterfs.filebasedfs.Statistics;
import org.netbeans.modules.masterfs.filebasedfs.utils.FSException;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Enumerations;

import java.io.*;
import java.util.Date;
import java.util.Enumeration;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;

/**
 * @author rm111737
 */

final class FileObj extends BaseFileObj {
    static final long serialVersionUID = -1133540210876356809L;
    private long lastModified = -1;


    FileObj(final File file, final FileNaming name) {
        super(file, name);
        lastModified(null);
    }
    
    public final java.io.OutputStream getOutputStream(final org.openide.filesystems.FileLock lock) throws java.io.IOException {
        final File f = getFileName().getFile();
        assert f.exists() || !isValid(true, f) ;

        final MutualExclusionSupport.Closeable closable = MutualExclusionSupport.getDefault().addResource(this, false);
        FileOutputStream retVal = null;
        try {
            retVal = new FileOutputStream(getFileName().getFile()) {
                                public void close() throws IOException {
                                    if (!closable.isClosed()) {
                                        super.close();
                                        closable.close();
                                        lastModified();
                                        fireFileChangedEvent(false);
                                    }
                                }
                            };
        } catch (FileNotFoundException e) {
            if (closable != null) {
                closable.close();
            }
            throw e;
        }
        return retVal;
    }

    public final java.io.InputStream getInputStream() throws java.io.FileNotFoundException {
        final File f = getFileName().getFile();
        assert f.exists() || !isValid(true, f) ;
                        
        InputStream inputStream;
        MutualExclusionSupport.Closeable closeableReference = null;
        
        try {
            final MutualExclusionSupport.Closeable closable = MutualExclusionSupport.getDefault().addResource(this, true);
            closeableReference = closable;
            inputStream = new FileInputStream(getFileName().getFile()) {
                public void close() throws IOException {
                    super.close();
                    closable.close();
                }
            };
        } catch (IOException e) {
            if (closeableReference != null) {
                closeableReference.close();    
            }
            
            final FileNotFoundException fileNotFoundException = new FileNotFoundException(e.getLocalizedMessage());
            FSException.annotateException(fileNotFoundException);
            throw fileNotFoundException;
        }
        assert inputStream != null;
        lastModified ();
        return inputStream;
    }

    public final Date lastModified() {
        final File f = getFileName().getFile();
        return new Date(lastModified(f));
    }

    private final long lastModified(File f) {
        if (f == null) {
            lastModified = System.currentTimeMillis();
        } else {
            lastModified = (f.exists()) ? f.lastModified() : -1;
        }
        
        return (lastModified < 0) ? 0 : lastModified;
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
        return lastModified != -1;
    }

    protected void setValid(boolean valid) {
        if (!valid) {
            lastModified = -1;
        } else {
            if (!isValid()) lastModified();
        }
    }

    public final boolean isFolder() {
        return false;
    }


    public final void refresh(final boolean expected) {
//        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.REFRESH_FILE);
//        stopWatch.start();
        boolean isDeleted = (isValid() && !isValid(true));
        if (isValid()) {
            final long oldLastModified = lastModified;
            lastModified();

            if (oldLastModified != -1 && oldLastModified < lastModified) {
                fireFileChangedEvent(expected);
            }
        } else if (isDeleted && getExistingParent() == null) {            
            fireFileDeletedEvent(expected);    
        }
//        stopWatch.stop();
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
        return WriteLock.tryLock(me);
    }

    final boolean checkLock(final FileLock lock) throws IOException {
        final File f = getFileName().getFile();
        return ((lock instanceof WriteLock) && (((WriteLock) lock).isValid(f)));
    }
}
