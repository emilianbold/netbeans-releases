/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem.FSCallable;
import org.netbeans.modules.masterfs.filebasedfs.Statistics;
import org.netbeans.modules.masterfs.filebasedfs.children.ChildrenCache;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;
import org.netbeans.modules.masterfs.filebasedfs.utils.FSException;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.netbeans.modules.masterfs.providers.Attributes;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions.IOHandler;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;
import org.openide.util.Mutex;

import javax.swing.event.EventListenerList;
import java.io.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.openide.util.Enumerations;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;


/**
 * Implements FileObject methods as simple as possible.
 *
 * @author Radek Matous
 */
//TODO: listeners still kept in EventListenerList

public abstract class BaseFileObj extends FileObject {
    //constants
    private static final String PATH_SEPARATOR = File.separator;//NOI18N
    private static final char EXT_SEP = '.';//NOI18N
    private FileChangeListener versioningWeakListener;    
    private final FileChangeListener versioningListener = new FileChangeListenerForVersioning();

    //static fields 
    static final long serialVersionUID = -1244650210876356809L;
    static final Attributes attribs;
    static {
        final BridgeForAttributes attrBridge = new BridgeForAttributes();
        attribs = new Attributes(attrBridge, attrBridge, attrBridge);
    }

    //private fields
    private EventListenerList eventSupport;
    private final FileNaming fileName;


    protected BaseFileObj(final File file) {
        this(file, NamingFactory.fromFile(file));
    }
    
    protected BaseFileObj(final File file, final FileNaming name) {
        this.fileName = name;
        versioningWeakListener = (FileChangeListener) WeakListeners.create(FileChangeListener.class, FileChangeListener.class, versioningListener, this);
        addFileChangeListener(versioningWeakListener);

    }
       
    @Override
    public final String toString() {
        return getFileName().toString();
    }

    @Override
    public final String getNameExt() {
        final File file = getFileName().getFile();
        final String retVal = BaseFileObj.getNameExt(file);
        return retVal;

    }

    /** Returns true is file is \\ComputerName\sharedFolder. */
    private static boolean isUncRoot(final File file) {
        if(file.getPath().startsWith("\\\\")) { //NOI18N
            File parent = file.getParentFile();
            if(parent != null) {
                parent = parent.getParentFile();
                if(parent != null) {
                    return parent.getPath().equals("\\\\"); // NOI18N
                }
            }
        }
        return false;
    }
    
    static String getNameExt(final File file) {
        String retVal = (file.getParent() == null || isUncRoot(file)) ? file.getAbsolutePath() : file.getName();
        if (retVal.endsWith(PATH_SEPARATOR)) {//NOI18N
            final boolean isPermittedToStripSlash = !(file.getParent() == null && new FileInfo(file).isUNCFolder());
            if (isPermittedToStripSlash) {
                retVal = retVal.substring(0, retVal.length() - 1);
            }
        }
        return retVal;
    }

    @Override
    public boolean canRead() {
        final File file = getFileName().getFile();        
        return file.canRead();
    }

    @Override
    public boolean canWrite() {
        final File file = getFileName().getFile();        
        ProvidedExtensions extension = getProvidedExtensions();
        return extension.canWrite(file);
    }

    public final boolean isData() {
        return !isFolder();
    }

    public final String getName() {
        return FileInfo.getName(getNameExt());
    }

    public final String getExt() {
        return FileInfo.getExt(getNameExt());
    }

    @Override
    public final String getPath() {
        FileNaming fileNaming = getFileName();
        LinkedList<String> stack = new LinkedList<String>();
        while (fileNaming != null) {
            stack.addFirst(fileNaming.getName());
            fileNaming = fileNaming.getParent();
        }
        String rootName = stack.removeFirst();
        if (Utilities.isWindows()) {
            rootName = rootName.replace(File.separatorChar, '/');
            if(rootName.startsWith("//")) {  //NOI18N
                // UNC root like //computer/sharedFolder
                rootName += "/";  //NOI18N
            }
        }
        StringBuilder path = new StringBuilder();
        path.append(rootName);
        while (!stack.isEmpty()) {
            path.append(stack.removeFirst());
            if (!stack.isEmpty()) {
                path.append('/');  //NOI18N
            }
        }
        return path.toString();
    }

    public final FileSystem getFileSystem() throws FileStateInvalidException {
        return FileBasedFileSystem.getInstance();
    }

    public final boolean isRoot() {
        return false;
    }
     
    @Override
    public final FileObject move(FileLock lock, FileObject target, String name, String ext) throws IOException {
        if (!checkLock(lock)) {
            FSException.io("EXC_InvalidLock", lock, getPath()); // NOI18N
        }
        ProvidedExtensions extensions =  getProvidedExtensions();
        FileObject result = null;
        File to = (target instanceof FolderObj) ? new File(((BaseFileObj) target).getFileName().getFile(), FileInfo.composeName(name, ext)) :
            new File(FileUtil.toFile(target), FileInfo.composeName(name, ext));            
        final IOHandler moveHandler = extensions.getMoveHandler(getFileName().getFile(), to);
        if (moveHandler != null) {
            if (target instanceof FolderObj) {
                result = move(lock, (FolderObj)target, name, ext,moveHandler);
            } else {
                moveHandler.handle();
                refresh(true);
                //perfromance bottleneck to call refresh on folder
                //(especially for many files to be moved)
                target.refresh(true);
                result = target.getFileObject(name, ext);
                assert (result != null);                        
            }
        } else {
            result = super.move(lock, target, name, ext);
        }
        
        FileUtil.copyAttributes(this, result);
        return result;                        
    }
    
    public BaseFileObj move(FileLock lock, FolderObj target, String name, String ext, ProvidedExtensions.IOHandler moveHandler) throws IOException {
        moveHandler.handle();
        String nameExt = FileInfo.composeName(name,ext);
        target.getChildrenCache().getChild(nameExt, true);
        //TODO: review
        BaseFileObj result = (BaseFileObj)FileBasedFileSystem.getFileObject(
                new File(target.getFileName().getFile(),nameExt));
        assert result != null;
        result.fireFileDataCreatedEvent(false);
        FolderObj parent = getExistingParent();
        if (parent != null) {
            parent.refresh(true);
        } else {
            refresh(true);
        }
        //fireFileDeletedEvent(false);
        return result;
    }


    void rename(final FileLock lock, final String name, final String ext, final ProvidedExtensions.IOHandler handler) throws IOException {
        if (!checkLock(lock)) {
            FSException.io("EXC_InvalidLock", lock, getPath()); // NOI18N
        }

        final File file = getFileName().getFile();
        final File parent = file.getParentFile();

        final String newNameExt = FileInfo.composeName(name, ext);
        final File file2Rename = new File(parent, newNameExt);
        if (parent == null || !FileChangedManager.getInstance().exists(parent) ||
                // #128818 - slash or backslash not allowed in name
                newNameExt.contains("/") || newNameExt.contains("\\")) {  //NOI18N
            FileObject parentFo = getExistingParent();
            String parentPath = (parentFo != null) ? parentFo.getPath() : file.getParentFile().getAbsolutePath();
            FSException.io("EXC_CannotRename", file.getName(), parentPath, newNameExt);// NOI18N
        }
        boolean targetFileExists = FileChangedManager.getInstance().exists(file2Rename) && !file2Rename.equals(file);
        //#108690
        if (targetFileExists && Utilities.isMac()) {
            final File parentFile2 = file2Rename.getParentFile();
            final File parentFile = file.getParentFile();
            if (parentFile2 != null && parentFile != null && parentFile.equals(parentFile2)) {
                if (file2Rename.getName().equalsIgnoreCase(file.getName())) {
                    targetFileExists = false;
                }
            }
        }

        if (targetFileExists) {
            FileObject parentFo = getExistingParent();
            String parentPath = (parentFo != null) ? parentFo.getPath() : file.getParentFile().getAbsolutePath();
            FSException.io("EXC_CannotRename", file.getName(), parentPath, newNameExt);// NOI18N
        }

        final String originalName = getName();
        final String originalExt = getExt();

        //TODO: no lock used
        FileObjectFactory fs = getFactory();

        synchronized (fs.AllFactories) {
            FileNaming[] allRenamed = NamingFactory.rename(getFileName(), newNameExt, handler);
            if (allRenamed == null) {
                FileObject parentFo = getExistingParent();
                String parentPath = (parentFo != null) ? parentFo.getPath() : file.getParentFile().getAbsolutePath();
                FSException.io("EXC_CannotRename", file.getName(), parentPath, newNameExt);// NOI18N
            }
            fs.rename();
            BaseFileObj.attribs.renameAttributes(file.getAbsolutePath().replace('\\', '/'), file2Rename.getAbsolutePath().replace('\\', '/'));//NOI18N
            for (int i = 0; i < allRenamed.length; i++) {
                FolderObj par = (allRenamed[i].getParent() != null) ? (FolderObj) fs.getCachedOnly(allRenamed[i].getParent().getFile()) : null;
                if (par != null) {
                    ChildrenCache childrenCache = par.getChildrenCache();
                    final Mutex.Privileged mutexPrivileged = (childrenCache != null) ? childrenCache.getMutexPrivileged() : null;
                    if (mutexPrivileged != null) {
                        mutexPrivileged.enterWriteAccess();
                    }
                    try {
                        childrenCache.removeChild(allRenamed[i]);
                        childrenCache.getChild(allRenamed[i].getName(), true);
                    } finally {
                        if (mutexPrivileged != null) {
                            mutexPrivileged.exitWriteAccess();
                        }
                    }
                }
            }
        }
        //TODO: RELOCK
        LockForFile.relock(file, file2Rename);

        fireFileRenamedEvent(originalName, originalExt);    
    }


    public final void rename(final FileLock lock, final String name, final String ext) throws IOException {
        FSCallable<Boolean> c = new FSCallable<Boolean>() {
            public Boolean call() throws IOException {
                ProvidedExtensions extensions = getProvidedExtensions();
                rename(lock, name, ext, extensions.getRenameHandler(getFileName().getFile(), FileInfo.composeName(name, ext)));
                return true;
            }
        };        
        FileBasedFileSystem.runAsInconsistent(c);
    }
    
      
    public Object getAttribute(final String attrName) {
        if (attrName.equals("FileSystem.rootPath")) {
            return "";//NOI18N
        } else if (attrName.equals("java.io.File")) {
            File file = getFileName().getFile();
            if (file != null && FileChangedManager.getInstance().exists(file)) {
                return file;
            }
        } else if (attrName.equals("ExistsParentNoPublicAPI")) {
            return getExistingParent() != null;
        } else if (attrName.startsWith("ProvidedExtensions")) {  //NOI18N
            // #158600 - delegate to ProvidedExtensions if attrName starts with ProvidedExtensions prefix
            ProvidedExtensions extension = getProvidedExtensions();
            return extension.getAttribute(getFileName().getFile(), attrName);
        }
   
        return BaseFileObj.attribs.readAttribute(getFileName().getFile().getAbsolutePath().replace('\\', '/'), attrName);//NOI18N
    }

    public final void setAttribute(final String attrName, final Object value) throws java.io.IOException {
        final Object oldValue = BaseFileObj.attribs.readAttribute(getFileName().getFile().getAbsolutePath().replace('\\', '/'), attrName);//NOI18N
        BaseFileObj.attribs.writeAttribute(getFileName().getFile().getAbsolutePath().replace('\\', '/'), attrName, value);//NOI18N
        fireFileAttributeChangedEvent(attrName, oldValue, value);
    }

    public final java.util.Enumeration getAttributes() {
        return BaseFileObj.attribs.attributes(getFileName().getFile().getAbsolutePath().replace('\\', '/'));//NOI18N
    }

    public final void addFileChangeListener(final org.openide.filesystems.FileChangeListener fcl) {
        getEventSupport().add(FileChangeListener.class, fcl);
    }

    public final void removeFileChangeListener(final org.openide.filesystems.FileChangeListener fcl) {
        getEventSupport().remove(FileChangeListener.class, fcl);
    }

    private Enumeration getListeners() {
        if (eventSupport == null) {
            return Enumerations.empty();
        }
        return org.openide.util.Enumerations.array(getEventSupport().getListeners(FileChangeListener.class));
    }


    public final long getSize() {
        return getFileName().getFile().length();
    }

    public final void setImportant(final boolean b) {
    }


    public boolean isReadOnly() {
        final File f = getFileName().getFile();
        ProvidedExtensions extension = getProvidedExtensions();
        return !extension.canWrite(f) && FileChangedManager.getInstance().exists(f);
    }

    public final FileObject getParent() {
        FileObject retVal = null;
        if (!isRoot()) {
            final FileNaming parent = getFileName().getParent();
            if (Utilities.isWindows()) {
                if (parent == null) {
                    retVal = FileBasedFileSystem.getInstance().getRoot();
                } else {
                    final FileObjectFactory factory = getFactory();
                    final File file = parent.getFile();
                    retVal = factory.getCachedOnly(file);
                    retVal = (retVal == null) ? factory.getFileObject(new FileInfo(file), FileObjectFactory.Caller.GetParent) : retVal;
                }
            } else if ((parent != null)) {
                final FileObjectFactory factory = getFactory();
                final File file = parent.getFile();
                if (file.getParentFile() == null) {
                    retVal = FileBasedFileSystem.getInstance().getRoot();
                } else {
                    retVal = factory.getCachedOnly(file);
                    retVal = (retVal == null) ? factory.getFileObject(new FileInfo(file), FileObjectFactory.Caller.GetParent) : retVal;
                }
            }
        }
        return retVal;
    }
        
    static File getFile(final File f, final String name, final String ext) {
        File retVal;

        final StringBuffer sb = new StringBuffer();
        sb.append(name);
        if (ext != null && ext.length() > 0) {
            sb.append(BaseFileObj.EXT_SEP);
            sb.append(ext);
        }
        retVal = new File(f, sb.toString());
        return retVal;
    }

    public final FileObjectFactory getFactory() {
        return FileObjectFactory.getInstance(getFileName().getFile());
    }

    final void fireFileDataCreatedEvent(final boolean expected) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.LISTENERS_CALLS);
        stopWatch.start();

        final BaseFileObj parent = getExistingParent();
        Enumeration pListeners = (parent != null) ? parent.getListeners() : null;
        
        assert this.isValid() : this.toString();
        FileEventImpl parentFe = null;
        if (parent != null && pListeners != null) {
            parentFe = new FileEventImpl(parent, this, expected);
        }
        if (parentFe != null) {
            final FileEventImpl fe = new FileEventImpl(this, parentFe);
            fireFileDataCreatedEvent(getListeners(), fe);
            parent.fireFileDataCreatedEvent(pListeners, parentFe);
        } else {
            final FileEventImpl fe = new FileEventImpl(this, this, expected);
            fireFileDataCreatedEvent(getListeners(), fe);
        }
        stopWatch.stop();
    }


    final void fireFileFolderCreatedEvent(final boolean expected) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.LISTENERS_CALLS);
        stopWatch.start();
        
        
        final BaseFileObj parent = getExistingParent();
        Enumeration pListeners = (parent != null) ? parent.getListeners() : null;

        FileEventImpl parentFe = null;
        if (parent != null && pListeners != null) {
            parentFe = new FileEventImpl(parent, this, expected);
        }
        if (parentFe != null) {
            final FileEventImpl fe = new FileEventImpl(this, parentFe);
            fireFileFolderCreatedEvent(getListeners(), fe);
            parent.fireFileFolderCreatedEvent(pListeners, parentFe);
        } else {
            final FileEventImpl fe = new FileEventImpl(this, this, expected);
            fireFileFolderCreatedEvent(getListeners(), fe);
        }
        stopWatch.stop();
    }

    public final void fireFileChangedEvent(final boolean expected) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.LISTENERS_CALLS);
        stopWatch.start();
        
        FileObject p = getExistingParent();
        final BaseFileObj parent = (BaseFileObj)((p instanceof BaseFileObj) ? p : null);//getExistingParent();
        Enumeration pListeners = (parent != null) ? parent.getListeners() : null;
        
        FileEventImpl parentFe = null;
        if (parent != null && pListeners != null) {
            parentFe = new FileEventImpl(parent, this, expected);
        }
        if (parentFe != null) {
            final FileEventImpl fe = new FileEventImpl(this, parentFe);
            fireFileChangedEvent(getListeners(), fe);
            parent.fireFileChangedEvent(pListeners, parentFe);
        } else {
            final FileEventImpl fe = new FileEventImpl(this, this, expected);
            fireFileChangedEvent(getListeners(), fe);
        }
        stopWatch.stop();
    }


    final void fireFileDeletedEvent(final boolean expected) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.LISTENERS_CALLS);
        stopWatch.start();
        FileObject p = getExistingParent();
        final BaseFileObj parent = (BaseFileObj)((p instanceof BaseFileObj) ? p : null);//getExistingParent();
        Enumeration pListeners = (parent != null) ?parent.getListeners() : null;        
        
        FileEventImpl parentFe = null;
        if (parent != null && pListeners != null) {
            parentFe = new FileEventImpl(parent, this, expected);
        }
        if (parentFe != null) {
            final FileEventImpl fe = new FileEventImpl(this, parentFe);
            fireFileDeletedEvent(getListeners(), fe);
            parent.fireFileDeletedEvent(pListeners, parentFe);
        } else {
            final FileEventImpl fe = new FileEventImpl(this, this, expected);
            fireFileDeletedEvent(getListeners(), fe);
        }
        stopWatch.stop();
    }


    private void fireFileRenamedEvent(final String originalName, final String originalExt) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.LISTENERS_CALLS);
        stopWatch.start();
        
        final BaseFileObj parent = getExistingParent();
        Enumeration pListeners = (parent != null) ?parent.getListeners() : null;        

        fireFileRenamedEvent(getListeners(), new FileRenameEvent(this, originalName, originalExt));

        if (parent != null && pListeners != null) {
            parent.fireFileRenamedEvent(pListeners, new FileRenameEvent(parent, this, originalName, originalExt));
        }
        
        stopWatch.stop();
    }

    final void fireFileAttributeChangedEvent(final String attrName, final Object oldValue, final Object newValue) {
        final BaseFileObj parent = getExistingParent();
        Enumeration pListeners = (parent != null) ?parent.getListeners() : null;        

        fireFileAttributeChangedEvent(getListeners(), new FileAttributeEvent(this, this, attrName, oldValue, newValue));

        if (parent != null && pListeners != null) {
            parent.fireFileAttributeChangedEvent(pListeners, new FileAttributeEvent(parent, this, attrName, oldValue, newValue));
        }
    }


    public final FileNaming getFileName() {
        return fileName;
    }
    
    public final void delete(final FileLock lock) throws IOException {
        FSCallable<Boolean> c = new FSCallable<Boolean>() {
            public Boolean call() throws IOException {
                ProvidedExtensions pe = getProvidedExtensions();
                pe.beforeDelete(BaseFileObj.this);
                try {
                    delete(lock, pe.getDeleteHandler(getFileName().getFile()));
                } catch (IOException iex) {
                    getProvidedExtensions().deleteFailure(BaseFileObj.this);
                    throw iex;
                }
                return true;
            }            
        };
        FileBasedFileSystem.runAsInconsistent(c);
    }    

    public void delete(final FileLock lock, ProvidedExtensions.DeleteHandler deleteHandler) throws IOException {        
        final File f = getFileName().getFile();

        final FolderObj existingParent = getExistingParent();
        final ChildrenCache childrenCache = (existingParent != null) ? existingParent.getChildrenCache() : null;
        final Mutex.Privileged mutexPrivileged = (childrenCache != null) ? childrenCache.getMutexPrivileged() : null;

        if (mutexPrivileged != null) mutexPrivileged.enterWriteAccess();
        try {
            if (!checkLock(lock)) {
                FSException.io("EXC_InvalidLock", lock, getPath()); // NOI18N                
            }

            boolean deleteStatus = (deleteHandler != null) ? deleteHandler.delete(f) : f.delete();
            if (!deleteStatus) {
                FileObject parent = getExistingParent();
                String parentPath = (parent != null) ? parent.getPath() : f.getParentFile().getAbsolutePath();
                FSException.io("EXC_CannotDelete", f.getName(), parentPath);// NOI18N            
            } 
            BaseFileObj.attribs.deleteAttributes(f.getAbsolutePath().replace('\\', '/'));//NOI18N
            if (childrenCache != null) {
                if (deleteHandler != null) {
                    childrenCache.removeChild(getFileName());
                } else {
                    childrenCache.getChild(BaseFileObj.getNameExt(f), true);
                }
            }
        } finally {
            if (mutexPrivileged != null) {
                mutexPrivileged.exitWriteAccess();
            }
        }

        setValid(false);
        fireFileDeletedEvent(false);

    }
    
    abstract boolean checkLock(FileLock lock) throws IOException;

    public Object writeReplace() {
        return new ReplaceForSerialization(getFileName().getFile());
    }

    abstract protected void setValid(boolean valid);
    abstract void refreshImpl(final boolean expected, boolean fire);

    public final void refresh(final boolean expected, boolean fire) {
        Statistics.StopWatch stopWatch = Statistics.getStopWatch(Statistics.REFRESH_FILE);
        stopWatch.start();
        try {   
            if (isValid()) {
                refreshImpl(expected, fire);
                if (isData()) {
                    refreshExistingParent(expected, fire);
                }
            }
        } finally {
            stopWatch.stop();
        }
    }

    void refreshExistingParent(final boolean expected, boolean fire) {
        boolean validityFlag = FileChangedManager.getInstance().exists(getFileName().getFile());
        if (!validityFlag) {
            //fileobject is invalidated
            FolderObj parent = getExistingParent();
            if (parent != null) {
                ChildrenCache childrenCache = parent.getChildrenCache();
                final Mutex.Privileged mutexPrivileged = (childrenCache != null) ? childrenCache.getMutexPrivileged() : null;
                if (mutexPrivileged != null) {
                    mutexPrivileged.enterWriteAccess();
                }
                try {
                    childrenCache.getChild(getFileName().getFile().getName(), true);
                } finally {
                    if (mutexPrivileged != null) {
                        mutexPrivileged.exitWriteAccess();
                    }
                }
            }
            setValid(false);
            if (fire) {
                fireFileDeletedEvent(expected);
            }
        } 
    }
    

    //TODO: attributes written by VCS must be readable by FileBaseFS and vice versa  
/**
 * FileBaseFS 
 * <fileobject name="E:\work\nb_all8\openide\masterfs\src\org\netbeans\modules\masterfs">
 *      <attr name="OpenIDE-Folder-SortMode" stringvalue="S"/>
 *
 * VCS FS
 * </fileobject>
 * <fileobject name="e:|work|nb_all8openide|masterfs|src|org|netbeans|modules|masterfs">
 *      <attr name="OpenIDE-Folder-SortMode" stringvalue="F"/>
 *  
 */    
    private static final class BridgeForAttributes implements AbstractFileSystem.List, AbstractFileSystem.Change, AbstractFileSystem.Info {
        public final Date lastModified(final String name) {
            final File file = new File(name);
            return new Date(file.lastModified());
        }

        public final boolean folder(final String name) {
            final File file = new File(name);
            return file.isDirectory();
        }

        public final boolean readOnly(final String name) {
            final File file = new File(name);
            return !file.canWrite();

        }

        public final String mimeType(final String name) {
            return "content/unknown"; // NOI18N;
        }

        public final long size(final String name) {
            final File file = new File(name);
            return file.length();
        }

        public final InputStream inputStream(final String name) throws FileNotFoundException {
            final File file = new File(name);
            return new FileInputStream(file);

        }

        public final OutputStream outputStream(final String name) throws IOException {
            final File file = new File(name);
            return new FileOutputStream(file);
        }

        public final void lock(final String name) throws IOException {
        }

        public final void unlock(final String name) {
        }

        public final void markUnimportant(final String name) {
        }

        public final String[] children(final String f) {
            final File file = new File(f);
            return file.list();
        }

        public final void createFolder(final String name) throws IOException {
            final File file = new File(name);
            if (!file.mkdirs()) {
                final IOException ioException = new IOException(name);
                throw ioException;
            }
        }

        public final void createData(final String name) throws IOException {
            final File file = new File(name);
            if (!file.createNewFile()) {
                throw new IOException(name);
            }
        }

        public final void rename(final String oldName, final String newName) throws IOException {
            final File file = new File(oldName);
            final File dest = new File(newName);

            if (!file.renameTo(dest)) {
                FSException.io("EXC_CannotRename", file.getName(), "", dest.getName()); // NOI18N                
            }
        }

        public final void delete(final String name) throws IOException {
            final File file = new File(name);
            final boolean isDeleted = (file.isFile()) ? file.delete() : deleteFolder(file);
            if (isDeleted) {
                FSException.io("EXC_CannotDelete", file.getName(), ""); // NOI18N                                
            }
        }

        private boolean deleteFolder(final File file) throws IOException {
            final boolean ret = file.delete();

            if (ret) {
                return true;
            }

            if (!FileChangedManager.getInstance().exists(file)) {
                return false;
            }

            if (file.isDirectory()) {
                // first of all delete whole content
                final File[] arr = file.listFiles();
                if (arr != null) {  // check for null in case of I/O errors
                    for (int i = 0; i < arr.length; i++) {
                        final File f2Delete = arr[i];
                        if (!deleteFolder(f2Delete)) {
                            return false;
                        }
                    }
                }
            }

            return file.delete();
        }

    }

    private synchronized EventListenerList getEventSupport() {
        if (eventSupport == null) {
            eventSupport = new EventListenerList();
        }
        return eventSupport;
    }

    final ProvidedExtensions getProvidedExtensions() {
        FileBasedFileSystem.StatusImpl status = (FileBasedFileSystem.StatusImpl) FileBasedFileSystem.getInstance().getStatus();
        ProvidedExtensions extensions = status.getExtensions();
        return extensions;
    }

    public static FolderObj getExistingFor(File f, FileObjectFactory fbs) {         
        FileObject retval = fbs.getCachedOnly(f);
        return (FolderObj) ((retval instanceof FolderObj) ? retval : null);
    }
    
    public static FolderObj getExistingParentFor(File f, FileObjectFactory fbs) {         
        final File parentFile = f.getParentFile();
        return (parentFile == null) ? null : getExistingFor(parentFile, fbs);
    }
    
    FolderObj getExistingParent() {         
        return getExistingParentFor(getFileName().getFile(), getFactory());
    }
    
    private final class FileChangeListenerForVersioning extends FileChangeAdapter {
        @Override
        public void fileDataCreated(FileEvent fe) {
            if (fe.getFile() == BaseFileObj.this) {
                getProvidedExtensions().createSuccess(fe.getFile());
            }
        }

        /**
         * Implements FileChangeListener.fileFolderCreated(FileEvent fe)
         */
        @Override
        public void fileFolderCreated(FileEvent fe) {
            if (fe.getFile() == BaseFileObj.this) {            
                getProvidedExtensions().createSuccess(fe.getFile());
            }
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            if (fe.getFile() == BaseFileObj.this) {
                getProvidedExtensions().deleteSuccess(fe.getFile());
            }
        }        
    }    
        
    private static class FileEventImpl extends FileEvent implements Enumeration<FileEvent> {
        private FileEventImpl next;
        public boolean hasMoreElements() {
            return next != null;
        }

        public FileEvent nextElement() {
            if (next == null) {
                throw new NoSuchElementException(); 
            }
            return next;
        }        
        
        public FileEventImpl(FileObject src, FileObject file, boolean expected) {
            super(src, file, expected);
        }
        
        public FileEventImpl(FileObject src, FileEventImpl next) {
            super(src, next.getFile(), next.isExpected());
            this.next = next;
        }        
    }  
    
    /*private static class FileRenameEventImpl extends FileRenameEvent implements Enumeration<FileEvent> {
        private FileRenameEventImpl next;
        public boolean hasMoreElements() {
            return next != null;
        }

        public FileEvent nextElement() {
            if (next == null) {
                throw new NoSuchElementException(); 
            }
            return next;
        }        
        
        public FileRenameEventImpl(FileObject src, FileRenameEventImpl next) {
            this(src, next.getFile(), next.getName(), next.getExt());
            this.next = next;            
            
        }
        
        public FileRenameEventImpl(FileObject src, String name, String ext) {
            super(src, name, ext);
        }
        public FileRenameEventImpl(FileObject src, FileObject file, String name, String ext) {
            super(src, file, name, ext, false);
        }
    }*/
}
