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

import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.filebasedfs.Statistics;
import org.netbeans.modules.masterfs.filebasedfs.children.ChildrenCache;
import org.netbeans.modules.masterfs.filebasedfs.children.ChildrenSupport;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileName;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.netbeans.modules.masterfs.filebasedfs.utils.FSException;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;

import java.io.*;
import java.util.*;

/**
 * @author rm111737
 */
public final class FolderObj extends BaseFileObj {    
    static final long serialVersionUID = -1022430210876356809L;
    private static final Mutex.Privileged mp = new Mutex.Privileged();
    private static final Mutex mutex = new Mutex(FolderObj.mp);

    private final FolderChildrenCache folderChildren = new FolderChildrenCache();
    boolean valid = true;


    /**
     * Creates a new instance of FolderImpl
     */
    public FolderObj(final File file, final FileNaming name) {
        super(file, name);
        //valid = true;
    }

    public final boolean isFolder() {
        return true;
    }


    public final FileObject getFileObject(final String name, final String ext) {
        FileObject retVal = null;
        final File f = getFileName().getFile();
        final ChildrenCache childrenCache = getChildrenCache();        
        FileNaming child;
        File file;
        final Mutex.Privileged mutexPrivileged = childrenCache.getMutexPrivileged();

        mutexPrivileged.enterReadAccess();
        try {
            file = BaseFileObj.getFile(f, name, ext);
            final String nameExt = BaseFileObj.getNameExt(file);
            child = childrenCache.getChild(nameExt, false);
        } finally {
            mutexPrivileged.exitReadAccess();
        }

        final FileBasedFileSystem lfs = getLocalFileSystem();
        assert lfs != null;

        if (child != null) {
            retVal = lfs.findFileObject(file);
        }

        return retVal;
    }

    public final FileObject[] getChildren() {
        final List results = new ArrayList();

        final ChildrenCache childrenCache = getChildrenCache();
        final Mutex.Privileged mutexPrivileged = childrenCache.getMutexPrivileged();

        mutexPrivileged.enterWriteAccess();

        Set fileNames;
        try {
            fileNames = new HashSet(childrenCache.getChildren(true));
        } finally {
            mutexPrivileged.exitWriteAccess();
        }

        final FileBasedFileSystem lfs = getLocalFileSystem();        
        for (Iterator iterator = fileNames.iterator(); iterator.hasNext();) {
            final FileNaming fileName = (FileNaming) iterator.next();
            FileInfo fInfo = new FileInfo (fileName.getFile());
            fInfo.setFileNaming(fileName);
            fInfo.setValueForFlag(FileInfo.FLAG_exists, true);
            
            final FileObject fo = lfs.findFileObject(fInfo);
            if (fo != null) {
                results.add(fo);
            }
        }
        return (FileObject[]) results.toArray(new FileObject[0]);
    }

    public final FileObject createFolder(final String name) throws java.io.IOException {
        if (name.indexOf('\\') != -1 || name.indexOf('/') != -1) {//NOI18N
            throw new IllegalArgumentException(name);
        }
        
        FolderObj retVal = null;
        File folder2Create;
        final ChildrenCache childrenCache = getChildrenCache();
        
        final Mutex.Privileged mutexPrivileged = childrenCache.getMutexPrivileged();

        mutexPrivileged.enterWriteAccess();

        try {
            folder2Create = BaseFileObj.getFile(getFileName().getFile(), name, null);
            if (folder2Create.exists()) {
                FSException.io("EXC_CannotCreateFolder", name, getPath());// NOI18N   
            }

            if (!BaseFileObj.createRecursiveFolder(folder2Create)) {
                FSException.io("EXC_CannotCreateFolder", getNameExt(), getPath());// NOI18N   
            }

            final FileNaming childName = this.getChildrenCache().getChild(folder2Create.getName(), true);
            assert childName != null;
        } finally {
            mutexPrivileged.exitWriteAccess();
        }

        final FileBasedFileSystem localFileBasedFileSystem = getLocalFileSystem();
        if (localFileBasedFileSystem != null) {
            retVal = (FolderObj) localFileBasedFileSystem.findFileObject(folder2Create);
        }
        assert retVal != null : folder2Create.getAbsolutePath();
        retVal.fireFileFolderCreatedEvent(false);

        return retVal;
    }

    public final FileObject createData(final String name, final String ext) throws java.io.IOException {
        if (name.indexOf('\\') != -1 || name.indexOf('/') != -1) {//NOI18N
            throw new IllegalArgumentException(name);
        }
        
        final ChildrenCache childrenCache = getChildrenCache();        
        final Mutex.Privileged mutexPrivileged = childrenCache.getMutexPrivileged();
        
        mutexPrivileged.enterWriteAccess();

        FileObj retVal;
        File f;
        try {
            f = BaseFileObj.getFile(getFileName().getFile(), name, ext);
            boolean isError = f.createNewFile() ? false : true;
            isError = isError ? true : !f.exists();

            if (isError) {
                FSException.io("EXC_CannotCreateData", f.getName(), getPath());// NOI18N
            }

            final FileNaming childName = getChildrenCache().getChild(f.getName(), true);
            assert childName != null;

        } finally {
            mutexPrivileged.exitWriteAccess();
        }

        final FileBasedFileSystem localFileBasedFileSystem = getLocalFileSystem();
        retVal = null;
        if (localFileBasedFileSystem != null) {
            retVal = (FileObj) localFileBasedFileSystem.findFileObject(f);
        }

        assert retVal != null;
        retVal.fireFileDataCreatedEvent(false);

        return retVal;
    }

    public final void delete(final FileLock lock) throws IOException {
        final LinkedList all = new LinkedList();

        final File file = getFileName().getFile();
        if (!deleteFile(file, all, getLocalFileSystem().getFactory())) {
            FileObject parent = getExistingParent();
            String parentPath = (parent != null) ? parent.getPath() : file.getParentFile().getAbsolutePath();
            FSException.io("EXC_CannotDelete", file.getName(), parentPath);// NOI18N            
        }

        BaseFileObj.attribs.deleteAttributes(file.getAbsolutePath().replace('\\', '/'));//NOI18N

        for (int i = 0; i < all.size(); i++) {
            final BaseFileObj toDel = (BaseFileObj) all.get(i);            
            final FolderObj existingParent = toDel.getExistingParent();            
            final ChildrenCache childrenCache = (existingParent != null) ? existingParent.getChildrenCache() : null;            
            if (childrenCache != null) {
                final Mutex.Privileged mutexPrivileged = (childrenCache != null) ? childrenCache.getMutexPrivileged() : null;
                if (mutexPrivileged != null) mutexPrivileged.enterWriteAccess();
                try {                
                    childrenCache.getChild(BaseFileObj.getNameExt(file), true);
                } finally {
                    if (mutexPrivileged != null) mutexPrivileged.exitWriteAccess();                    
                }
            }                
            toDel.setValid(false);
            toDel.fireFileDeletedEvent(false);
        }        
    }


    public final void refresh(final boolean expected) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.REFRESH_FOLDER);
        stopWatch.start();

        if (isValid()) {
            boolean isFileCreatedFired = false;
            final ChildrenCache cache = getChildrenCache();
            final Mutex.Privileged mutexPrivileged = cache.getMutexPrivileged();

            Set oldChildren = null;
            Map refreshResult = null;
            mutexPrivileged.enterWriteAccess();
            try {
                oldChildren = cache.getChildren(false);
                refreshResult = cache.refresh();
            } finally {
                mutexPrivileged.exitWriteAccess();
            }

            oldChildren.removeAll(refreshResult.keySet());
            for (Iterator iterator = oldChildren.iterator(); iterator.hasNext();) {
                final FileName child = (FileName) iterator.next();
                final BaseFileObj childObj = getLocalFileSystem().getFactory().get(child.getFile());
                if (childObj != null && childObj.isData()) {
                    ((FileObj)childObj).refresh(expected);
                }
            }

            final FileBasedFileSystem localFileSystem = this.getLocalFileSystem();
            final FileObjectFactory factory = localFileSystem.getFactory();

            final Iterator iterator = refreshResult.entrySet().iterator();
            while (iterator.hasNext()) {
                final Map.Entry entry = (Map.Entry) iterator.next();
                final FileName child = (FileName) entry.getKey();
                final Integer operationId = (Integer) entry.getValue();

                BaseFileObj newChild = (operationId == ChildrenCache.ADDED_CHILD) ? (BaseFileObj)
                    factory.findFileObject(new FileInfo(child.getFile())): factory.get(child.getFile());
                newChild = (BaseFileObj) ((newChild != null) ? newChild : getFileObject(child.getName()));
                if (operationId == ChildrenCache.ADDED_CHILD && newChild != null) {

                    if (newChild.isFolder()) {
                        isFileCreatedFired = true;
                        newChild.fireFileFolderCreatedEvent(expected);
                    } else {
                        isFileCreatedFired = true;
                        newChild.fireFileDataCreatedEvent(expected);
                    }

                } else if (operationId == ChildrenCache.REMOVED_CHILD) {
                    if (newChild != null) {
                        if (newChild.isValid()) {
                            newChild.setValid(false);
                            newChild.fireFileDeletedEvent(expected);
                        }
                    } else {
                        //TODO: should be rechecked
                        //assert false;
                        final File f = child.getFile();
                        if (!(new FileInfo(f).isConvertibleToFileObject())) {
                            final BaseFileObj fakeInvalid;
                            if (child.isFile()) {
                                fakeInvalid = new FileObj(f, child);
                            } else {
                                fakeInvalid = new FolderObj(f, child);                            
                            }

                            fakeInvalid.setValid(false);
                            fakeInvalid.fireFileDeletedEvent(expected);
                        }
                    }

                } else {
                    assert !(new FileInfo(child.getFile()).isConvertibleToFileObject());
                }

            }
            boolean validityFlag = getFileName().getFile().exists();                                
            if (!validityFlag) {
                //fileobject is invalidated                
                setValid(false);                       
                fireFileDeletedEvent(expected);    
            }
        }         
        stopWatch.stop();        
    }
    
    //TODO: rewrite partly and check FileLocks for existing FileObjects
    private boolean deleteFile(final File file, final LinkedList all, final FileObjectFactory factory) throws IOException {
        final boolean ret = file.delete();

        if (ret) {
            final FileObject aliveFo = factory.get(file);
            if (aliveFo != null) {
                all.addFirst(aliveFo);
            }
            return true;
        }

        if (!file.exists()) {
            return false;
        }

        if (file.isDirectory()) {
            // first of all delete whole content
            final File[] arr = file.listFiles();
            for (int i = 0; i < arr.length; i++) {
                final File f2Delete = arr[i];
                if (!deleteFile(f2Delete, all, factory)) {
                    return false;
                }
            }
        } 
        
        // delete the file itself
        //super.delete(lock());
        

        final boolean retVal = file.delete();
        if (retVal) {
            final FileObject aliveFo = factory.get(file);
            if (aliveFo != null) {
                all.addFirst(aliveFo);
            }
        }


        return true;
    }

    protected void setValid(final boolean valid) {
        if (valid) {
            //I can't make valid fileobject when it was one invalidated
            assert isValid() : this.toString();
        } else {
            this.valid = false;
        }        
        
    }

    public boolean isValid() {
        return valid;
    }

    public final InputStream getInputStream() throws FileNotFoundException {
        throw new FileNotFoundException(getPath());
    }

    public final OutputStream getOutputStream(final FileLock lock) throws IOException {
        throw new IOException(getPath());
    }

    public final java.util.Date lastModified() {
        final File f = getFileName().getFile();
        return new Date(f.lastModified());
    }

    public final FileLock lock() throws IOException {
        return new FileLock();
    }

    final boolean checkLock(final FileLock lock) throws IOException {
        return true;
    }

    public final ChildrenCache getChildrenCache() {
        //assert getFileName().getFile().isDirectory() || !getFileName().getFile().exists();
        return folderChildren;
    }

    public final class FolderChildrenCache implements ChildrenCache {
        final ChildrenSupport ch = new ChildrenSupport();


        public final Set getChildren(final boolean rescan) {
            return ch.getChildren(getFileName(), rescan);
        }

        public final FileNaming getChild(final String childName, final boolean rescan) {
            return ch.getChild(childName, getFileName(), rescan);
        }

        public final Map refresh() {
            return ch.refresh(getFileName());
        }

        public final Mutex.Privileged getMutexPrivileged() {
            return FolderObj.mp;
        }

        public final String toString() {
            return getFileName().toString();
        }

        public boolean existsInCache(String childName) {
            return ch.existsldInCache(getFileName(), childName);
        }
    }

}
