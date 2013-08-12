/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remote.impl.fs;

import java.awt.Image;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.ConnectionListener;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.api.ui.ConnectionNotifier;
import org.netbeans.modules.remote.impl.RemoteLogger;
import org.netbeans.modules.remote.impl.fileoperations.spi.AnnotationProvider;
import org.netbeans.modules.remote.impl.fileoperations.spi.FileOperationsProvider;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider;
import org.netbeans.modules.remote.impl.fileoperations.spi.FilesystemInterceptorProvider.FilesystemInterceptor;
import org.netbeans.modules.remote.spi.FileSystemCacheProvider;
import org.netbeans.modules.remote.spi.FileSystemProvider.FileSystemProblemListener;
import org.openide.filesystems.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.io.NbObjectInputStream;
import org.openide.windows.WindowManager;

/**
 * Remote file system:
 * gets files on demand from a remote host.
 * It is read-only
 * 
 * @author Vladimir Kvashin
 */
@org.netbeans.api.annotations.common.SuppressWarnings("Se") // is it ever serialized?
public final class RemoteFileSystem extends FileSystem implements ConnectionListener {

    private static final SystemAction[] NO_SYSTEM_ACTIONS = new SystemAction[]{};
    private static final boolean ATTR_STATS = Boolean.getBoolean("remote.attr.stats");
    
    public static final String ATTRIBUTES_FILE_NAME = ".rfs_attr"; // NOI18N
    public static final String CACHE_FILE_NAME = ".rfs_cache"; // NOI18N
    public static final String RESERVED_PREFIX = ".rfs_"; // NOI18N
    public static final String RESERVED_PREFIX_ESCAPED = "._rfs_"; // NOI18N
    
    private static final String READONLY_ATTRIBUTES = "readOnlyAttrs"; //NOI18N
    private final ExecutionEnvironment execEnv;
    private final String filePrefix;
    private final RemoteFileObject root;
    private final RemoteDirectory rootDelegate;
    private final RemoteFileSupport remoteFileSupport;
    private final RefreshManager refreshManager;
    private final File cache;
    private final RemoteFileObjectFactory factory;
    /** File transfer statistics */
    private final AtomicInteger fileCopyCount = new AtomicInteger(0);
    /** Directory synchronization statistics */
    private final AtomicInteger dirSyncCount = new AtomicInteger(0);
    private static final Object mainLock = new Object();
    private static final Map<File, WeakReference<ReadWriteLock>> locks = new HashMap<File, WeakReference<ReadWriteLock>>();
    private AtomicBoolean readOnlyConnectNotification = new AtomicBoolean(false);
    private final List<FileSystemProblemListener> problemListeners =
            new ArrayList<FileSystemProblemListener>();
    transient private final StatusImpl status = new StatusImpl();
    private final LinkedHashSet<String> deleteOnExitFiles = new LinkedHashSet<String>();

    /*package*/ RemoteFileSystem(ExecutionEnvironment execEnv) throws IOException {
        RemoteLogger.assertTrue(execEnv.isRemote());
        this.execEnv = execEnv;
        this.remoteFileSupport = new RemoteFileSupport();
        factory = new RemoteFileObjectFactory(this);
        refreshManager = new RefreshManager(execEnv, factory);
        // FIXUP: it's better than asking a compiler instance... but still a fixup.
        // Should be moved to a proper place
        this.filePrefix = FileSystemCacheProvider.getCacheRoot(execEnv);
        if (filePrefix == null) {
            throw new IllegalStateException("Can not find cache root for remote file system at " + execEnv); //NOI18N
        }
        cache = new File(filePrefix);
        if (!cache.exists() && !cache.mkdirs()) {
            throw new IOException(NbBundle.getMessage(getClass(), "ERR_CreateDir", cache.getAbsolutePath()));
        }
        this.rootDelegate = new RootFileObject(this.root = new RemoteFileObject(this), this, execEnv, cache); // NOI18N
        factory.register(rootDelegate);

        final WindowFocusListener windowFocusListener = new WindowFocusListener() {

            @Override
            public void windowGainedFocus(WindowEvent e) {
                if (e.getOppositeWindow() == null) {
                    if (ConnectionManager.getInstance().isConnectedTo(RemoteFileSystem.this.execEnv)) {
                        refreshManager.scheduleRefreshOnFocusGained(factory.getCachedFileObjects());
                    }
                }
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
            }
        };
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                //WindowManager.getDefault().getMainWindow().addWindowFocusListener(focusListener);
                WindowManager.getDefault().getMainWindow().addWindowFocusListener(windowFocusListener);
            }
        });
        ConnectionManager.getInstance().addConnectionListener(RemoteFileSystem.this);
    }

    /*package*/ void dispose() {
        ConnectionManager.getInstance().removeConnectionListener(this);
    }

    @Override
    public void connected(ExecutionEnvironment env) {
        readOnlyConnectNotification.compareAndSet(true, false);
        if (execEnv.equals(env)) {
            Collection<RemoteFileObjectBase> cachedFileObjects = factory.getCachedFileObjects();            
            refreshManager.scheduleRefreshOnConnect(cachedFileObjects);
            for (RemoteFileObjectBase fo : cachedFileObjects) {
                fo.connectionChanged();
            }
        }
    }
        
    @Override
    public void disconnected(ExecutionEnvironment env) {
        readOnlyConnectNotification.compareAndSet(true, false);
        if (execEnv.equals(env)) {
            for (RemoteFileObjectBase fo : factory.getCachedFileObjects()) {
                fo.connectionChanged();
            }
        }
        if (ATTR_STATS) { dumpAttrStat(); }
    }
    
    /*package*/ ExecutionEnvironment getExecutionEnvironment() {
        return execEnv;
    }

    public RemoteFileObjectFactory getFactory() {
        return factory;
    }

    public RefreshManager getRefreshManager() {
        return refreshManager;
    }
    
    public String normalizeAbsolutePath(String absPath) {
        //BZ#192265 as vkvashin stated the URI i sused to normilize the path
        //but URI is really very restrictive so let's use another way
        //will use the face that path is absolute and we have Unix like system
        //no special code for Windows
        return PathUtilities.normalizeUnixPath(absPath);
    }

    /*package-local, for testing*/
    File getCache() {
        return cache;
    }

    public static ReadWriteLock getLock(File file) {
        synchronized (mainLock) {
            WeakReference<ReadWriteLock> ref = locks.get(file);
            ReadWriteLock result = (ref == null) ? null : ref.get();
            if (result == null) {
                result = new ReentrantReadWriteLock();
                locks.put(file, new WeakReference<ReadWriteLock>(result));
            }
            return result;
        }
    }

    /*package-local test method*/ final void resetStatistic() {
        dirSyncCount.set(0);
        fileCopyCount.set(0);
    }

    /*package-local test method*/ final int getDirSyncCount() {
        return dirSyncCount.get();
    }

    /*package-local test method*/ final int getFileCopyCount() {
        return fileCopyCount.get();
    }

    /*package-local test method*/ final void incrementDirSyncCount() {
        dirSyncCount.incrementAndGet();
    }

    /*package-local test method*/ final void incrementFileCopyCount() {
        fileCopyCount.incrementAndGet();
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "RFS_DISPLAY_NAME", execEnv.getDisplayName());
    }

    @Override
    public boolean isReadOnly() {
        return !ConnectionManager.getInstance().isConnectedTo(execEnv);
    }

    @Override
    public RemoteFileObject getRoot() {
        return root;
    }

    @Override
    public RemoteFileObject findResource(String name) {
        return findResource(name, new HashSet<String>());
    }

    public RemoteFileObject findResource(String name, Set<String> antiLoop) {
        if (name.isEmpty() || name.equals("/")) {  // NOI18N
            return getRoot();
        } else {
            return getRoot().getFileObject(name, antiLoop);
        }
    }

    @Override
    public FileObject getTempFolder() throws IOException {
        try {
            String tmpName = HostInfoUtils.getHostInfo(execEnv).getTempDir();
            RemoteFileObject tmpDir = findResource(tmpName);
            if (tmpDir != null && tmpDir.isFolder() && tmpDir.isValid()) {
                return tmpDir;
            }
        } catch (CancellationException ex) {
            //
        }
        throw new IOException("Cannot find temporary folder"); // NOI18N
    }
    
    @Override
    public FileObject createTempFile(FileObject parent, String prefix, String suffix, boolean deleteOnExit) throws IOException {
        if (parent.isFolder() && parent.isValid()) {
            while(true) {
                File tmpFile = File.createTempFile(prefix, suffix);
                String tmpName = tmpFile.getName();
                tmpFile.delete();
                try {
                    FileObject fo = parent.createData(tmpName);
                    if (fo != null && fo.isData() && fo.isValid()) {
                        if (deleteOnExit) {
                            addDeleteOnExit(fo.getPath());
                        }
                        return fo;
                    }
                    break;   
                } catch (IOException ex) {
                    FileObject test = parent.getFileObject(tmpName);
                    if (test != null) {
                        continue;
                    }
                    throw ex;
                }
            }
        }
        throw new IOException("Cannot create temporary file"); // NOI18N
    }
    
    public RemoteFileObjectBase findResourceImpl(String name, Set<String> antiloop) {
        if (name.isEmpty() || name.equals("/")) {  // NOI18N
            return getRoot().getImplementor();
        } else {
            RemoteFileObject fo = rootDelegate.getFileObject(name, antiloop);
            return (fo == null) ? null : fo.getImplementor();
        }
    }
    

    @Override
    public SystemAction[] getActions() {
        return NO_SYSTEM_ACTIONS;
    }

    public void addPendingFile(RemoteFileObjectBase fo) {
        remoteFileSupport.addPendingFile(fo);
        fireProblemListeners(fo.getPath());
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    /*package*/ void setAttribute(RemoteFileObjectBase file, String attrName, Object value) {
        RemoteFileObjectBase parent = file.getParent();
        boolean hasParent = true;
        if (parent == null) {
            // root
            parent = file;
            hasParent = false;
        }
        File attr = getAttrFile(parent);
        Properties table = readProperties(attr);
        String translatedAttributeName = translateAttributeName(file, attrName);
        String encodedValue = encodeValue(value);
        Object oldValue = null;
        if (encodedValue == null) {
            table.remove(translatedAttributeName);
        } else {                
            oldValue = table.setProperty(translatedAttributeName, encodedValue);
        }
        FileOutputStream fileOtputStream = null;
        try {
            fileOtputStream = new FileOutputStream(attr);
            table.store(fileOtputStream, "Set attribute "+attrName); // NOI18N
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (fileOtputStream != null) {
                try {
                    fileOtputStream.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        if (hasParent) {
            file.fireFileAttributeChangedEvent(file.getListeners(), new FileAttributeEvent(file.getOwnerFileObject(), file.getOwnerFileObject(), attrName, oldValue, value));
            parent.fireFileAttributeChangedEvent(parent.getListeners(), new FileAttributeEvent(parent.getOwnerFileObject(), file.getOwnerFileObject(), attrName, oldValue, value));
        } else {
            file.fireFileAttributeChangedEvent(file.getListeners(), new FileAttributeEvent(file.getOwnerFileObject(), file.getOwnerFileObject(), attrName, oldValue, value));
        }
        logAttrName(attrName, true);
    }

    private static class AttrStat {
        public final String name;
        public int readCount = 0;
        public int writeCount = 0;
        public StackTraceElement[] firstReadStack;
        public StackTraceElement[] firstWriteStack;
        public AttrStat(String name) {
            this.name = name;
        }
    }

    private static final Map<String, AttrStat> attrStats = new TreeMap<String, AttrStat> ();

    private static void logAttrName(String name, boolean write) {
        synchronized(attrStats) {
            AttrStat stat  = attrStats.get(name);
            if (stat == null) {
                stat = new AttrStat(name);
                attrStats.put(name, stat);
            }
            if (write) {
                if (stat.writeCount++ == 0) {
                    stat.firstWriteStack = Thread.currentThread().getStackTrace();
                }
            } else {
                if (stat.readCount++ == 0) {
                    stat.firstReadStack = Thread.currentThread().getStackTrace();
                }
            }
        }
        System.out.printf("%sAttribute %s\n", write ? "set" : "get", name); // NOI18N
    }

    /*package*/ void dumpAttrStat() {
        Map<String, AttrStat> toDump = null;
        synchronized(attrStats) {
            toDump= new TreeMap<String, AttrStat>(attrStats);
        }
        System.out.printf("\n\nDumping attributes statistics (%d elements)\n\n", toDump.size()); // NOI18N
        for (Map.Entry<String, AttrStat> entry : toDump.entrySet()) {
            //String name = entry.getKey();
            AttrStat stat = entry.getValue();
            System.out.printf("%s %d %d\n", stat.name, stat.readCount, stat.writeCount); // NOI18N
            if (stat.firstReadStack != null) {
                System.out.printf("\t%s first read stack:\n", stat.name); // NOI18N
                for (StackTraceElement e : stat.firstReadStack) {
                    System.out.printf("\t\t%s\n", e); // NOI18N
                }
            }
            if (stat.firstWriteStack != null) {
                System.out.printf("\t%s first write stack:\n", stat.name); // NOI18N
                for (StackTraceElement e : stat.firstWriteStack) {
                    System.out.printf("\t\t%s\n", e); // NOI18N
                }
            }
        }
    }

    private File getAttrFile(RemoteFileObjectBase parent) {
        File attr = new File(parent.getCache(), ATTRIBUTES_FILE_NAME);
        return attr;
    }

    /*package*/ Object getAttribute(RemoteFileObjectBase file, String attrName) {
        RemoteFileObjectBase parent = file.getParent();
        if (parent == null) {
            // root
            parent = file;
        }
        if (attrName.equals(FileObject.DEFAULT_LINE_SEPARATOR_ATTR)) {
            return "\n"; // NOI18N
        } else if (attrName.equals(READONLY_ATTRIBUTES)) {
            return Boolean.FALSE;
        } else if (attrName.equals("isRemoteAndSlow")) { // NOI18N
            return Boolean.TRUE;
        } else if (attrName.equals("FileSystem.rootPath")) { //NOI18N
            return this.getRoot().getPath();
        } else if (attrName.equals("java.io.File")) { //NOI18N
            return null;
        } else if (attrName.equals("ExistsParentNoPublicAPI")) { //NOI18N
            return true;
        } else if (attrName.startsWith("ProvidedExtensions")) { //NOI18N
            // #158600 - delegate to ProvidedExtensions if attrName starts with ProvidedExtensions prefix
            if (RemoteFileObjectBase.USE_VCS) {
                FilesystemInterceptor interceptor = FilesystemInterceptorProvider.getDefault().getFilesystemInterceptor(this);
                if (interceptor != null) {
                    return interceptor.getAttribute(FilesystemInterceptorProvider.toFileProxy(file.getOwnerFileObject()), attrName);
                }
            }
        }
        if (ATTR_STATS) { logAttrName(attrName, false); }
        File attr = getAttrFile(parent);
        Properties table = readProperties(attr);
        return decodeValue(table.getProperty(translateAttributeName(file, attrName)));
    }

    /*package*/ Enumeration<String> getAttributes(RemoteFileObjectBase file) {
        RemoteFileObjectBase parent = file.getParent();
        if (parent != null) {
            File attr = getAttrFile(parent);
            Properties table = readProperties(attr);
            List<String> res = new ArrayList<String>();
            Enumeration<Object> keys = table.keys();
            String prefix = file.getNameExt()+"["; // NOI18N
            while(keys.hasMoreElements()) {
                String aKey = keys.nextElement().toString();
                if (aKey.startsWith(prefix)) {
                    aKey = aKey.substring(prefix.length(),aKey.length()-1);
                    res.add(aKey);
                }
            }
            return Collections.enumeration(res);
        }
        return Collections.enumeration(Collections.<String>emptyList());
    }

    private Properties readProperties(File attr) {
        Properties table = new Properties();
        if (attr.exists()) {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(attr);
                table.load(fileInputStream);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        return table;
    }

    private String translateAttributeName(RemoteFileObjectBase file, String attrName) {
        return file.getNameExt()+"["+attrName+"]"; // NOI18N
    }
    
    /**
     * Creates serialized object, which was encoded in HEX format
     * @param value Encoded serialized object in HEX format
     * @return Created object from encoded HEX format
     * @throws IOException
     */
    private Object decodeValue(String value) {
        if ((value == null) || (value.length() == 0)) {
            return null;
        }

        byte[] bytes = new byte[value.length() / 2];
        int tempI;
        int count = 0;

        for (int i = 0; i < value.length(); i += 2) {
            try {
                tempI = Integer.parseInt(value.substring(i, i + 2), 16);

                if (tempI > 127) {
                    tempI -= 256;
                }

                bytes[count++] = (byte) tempI;
            } catch (NumberFormatException e) {
            }
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(bytes, 0, count);

        try {
            ObjectInputStream ois = new NbObjectInputStream(bis);
            Object ret = ois.readObject();

            return ret;
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * Encodes Object into String encoded in HEX format
     * @param value Object, which will be encoded
     * @return  serialized Object in String encoded in HEX format
     * @throws IOException
     */
    private String encodeValue(Object value) {
        if (value == null) {
            return null;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(value);
            oos.close();
        } catch (Exception e) {
        }

        byte[] bArray = bos.toByteArray();
        StringBuilder strBuff = new StringBuilder(bArray.length * 2);

        for (int i = 0; i < bArray.length; i++) {
            if ((bArray[i] < 16) && (bArray[i] >= 0)) {
                strBuff.append("0"); // NOI18N
            }

            strBuff.append(Integer.toHexString((bArray[i] < 0) ? (bArray[i] + 256) : bArray[i]));
        }

        return strBuff.toString();
    }

    void addReadOnlyConnectNotification(RemoteFileObjectBase fo) {
        if (readOnlyConnectNotification.compareAndSet(false, true)) {
            remoteFileSupport.addPendingFile(fo);
        }
    }

    public void addFileSystemProblemListener(FileSystemProblemListener listener) {
        synchronized (problemListeners) {
            problemListeners.add(listener);
        }
    }

    public void removeFileSystemProblemListener(FileSystemProblemListener listener) {
        synchronized (problemListeners) {
            problemListeners.remove(listener);
        }
    }

    /**
     * @param path if null, this means recover
     */
    private void fireProblemListeners(String path) {
        List<FileSystemProblemListener> listenersCopy;
        synchronized (problemListeners) {
            listenersCopy = new ArrayList<FileSystemProblemListener>(problemListeners);
        }
        for (FileSystemProblemListener l : listenersCopy) {
            if (path == null) {
                l.recovered(this);
            } else {
                l.problemOccurred(this, path);
            }
        }
    }

    @Override
    public final SystemAction[] getActions(final Set<FileObject> foSet) {
        SystemAction[] some = status.getActions (foSet);
        if (some != null) {
            return some;
        }        
        return new SystemAction[] {};
    }

    @Override
    public Status getStatus() {
        return status;
    }
    
    private void addDeleteOnExit(String path) {
        synchronized(deleteOnExitFiles) {
            if (deleteOnExitFiles.isEmpty()) {
                Runtime.getRuntime().addShutdownHook(new Thread() {

                    @Override
                    public void run() {
                        releaseResources();
                    }

                });
            }
            deleteOnExitFiles.add(path);
        }
    }
    
    private void releaseResources() {
    	ArrayList<String> toBeDeleted;
        synchronized(deleteOnExitFiles) {
        	toBeDeleted = new ArrayList<String>(deleteOnExitFiles);
        }
    	Collections.reverse(toBeDeleted);
        for (String filename : toBeDeleted) {
            if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                 return;
            }
            CommonTasksSupport.rmFile(execEnv, filename, null);
        }
    }
    
    private RemoteFileObjectBase vcsSafeGetFileObject(String path) {
        return factory.getCachedFileObject(path);
        //RemoteFileObject fo = findResource(path);
        //return (fo == null) ? null : fo.getImplementor();
    }
    
    public Boolean vcsSafeIsDirectory(String path) {
        path = PathUtilities.normalizeUnixPath(path);
        RemoteFileObjectBase fo = vcsSafeGetFileObject(path);
        if (fo == null) {
            return null;
        } else {
            return fo.isFolder() ? Boolean.TRUE : Boolean.FALSE;
        }            
    }

    public Boolean vcsSafeIsFile(String path) {
        path = PathUtilities.normalizeUnixPath(path);
        RemoteFileObjectBase fo = vcsSafeGetFileObject(path);
        if (fo == null) {
            return null;
        } else {
            switch(fo.getType()) {
                // TODO: should we change isData() instead?
                case Regular:
                    return true;
                case SymbolicLink:
                    return fo.isData() ? Boolean.TRUE : Boolean.FALSE;
                default:
                    return false;
            }            
        }            
    }

    public Boolean vcsSafeExists(String path) {
        path = PathUtilities.normalizeUnixPath(path);
        RemoteFileObjectBase fo = vcsSafeGetFileObject(path);
        if (fo == null) {
            String parentPath = PathUtilities.getDirName(path);            
            if (parentPath != null) {
                RemoteFileObjectBase parentFO = vcsSafeGetFileObject(parentPath);
                if (parentFO != null) {
                    String childNameExt = PathUtilities.getBaseName(path);
                    RemoteFileObject childFO = parentFO.getFileObject(childNameExt, new HashSet<String>());
                    return (childFO != null && childFO.isValid()) ? Boolean.TRUE : Boolean.FALSE;
                }
            }
            return null;
        } else {
            return fo.isValid() ? Boolean.TRUE : Boolean.FALSE;
        }            
    }

    public Long vcsSafeLastModified(String path) {
        path = PathUtilities.normalizeUnixPath(path);
        RemoteFileObjectBase fo = vcsSafeGetFileObject(path);
        if (fo == null) {
            String parentPath = PathUtilities.getDirName(path);
            if (parentPath != null) {
                RemoteFileObjectBase parentFO = vcsSafeGetFileObject(parentPath);
                if (parentFO != null) {
                    String childNameExt = PathUtilities.getBaseName(path);
                    RemoteFileObject childFO = parentFO.getFileObject(childNameExt, new HashSet<String>());
                    if (childFO != null) {
                        fo = childFO.getImplementor();
                    }
                }
            }
        }
        if (fo == null) {
            return null;
        } else {
            return fo.lastModified().getTime();
        }
    }

    private final class StatusImpl implements FileSystem.HtmlStatus, LookupListener, FileStatusListener {

        /** result with providers */
        private Lookup.Result<AnnotationProvider> annotationProviders;
        private Collection<? extends AnnotationProvider> previousProviders;

        {
            annotationProviders = Lookup.getDefault().lookupResult(AnnotationProvider.class);
            annotationProviders.addLookupListener(this);
            resultChanged(null);
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            Collection<? extends AnnotationProvider> now = annotationProviders.allInstances();
            Collection<? extends AnnotationProvider> add;

            if (previousProviders != null) {
                add = new HashSet<AnnotationProvider>(now);
                add.removeAll(previousProviders);
                HashSet<AnnotationProvider> toRemove = new HashSet<AnnotationProvider>(previousProviders);
                toRemove.removeAll(now);
                for (AnnotationProvider ap : toRemove) {
                    ap.removeFileStatusListener(this);
                }
            } else {
                add = now;
            }

            for (AnnotationProvider ap : add) {
                try {
                    ap.addFileStatusListener(this);
                } catch (java.util.TooManyListenersException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            previousProviders = now;
        }

        public SystemAction[] getActions(Set<FileObject> foSet) {

            javax.swing.Action[] retVal = null;
            java.util.Iterator<? extends AnnotationProvider> it = annotationProviders.allInstances().iterator();
            while (retVal == null && it.hasNext()) {
                AnnotationProvider ap = it.next();
                retVal = ap.actions(foSet);
            }
            if (retVal != null) {
                // right now we handle just SystemAction, it can be changed if necessary
                SystemAction[] ret = new SystemAction[retVal.length];
                for (int i = 0; i < retVal.length; i++) {
                    if (retVal[i] instanceof SystemAction) {
                        ret[i] = (SystemAction) retVal[i];
                    }
                }
                return ret;
            }
            return null;
        }

        @Override
        public void annotationChanged(FileStatusEvent ev) {
            fireFileStatusChanged(ev);
        }

        @Override
        public Image annotateIcon(Image icon, int iconType, Set<? extends FileObject> files) {
            for(AnnotationProvider ap : annotationProviders.allInstances()) {
                Image retVal = ap.annotateIcon(icon, iconType, files);
                if (retVal != null) {
                    return retVal;
                }
            }
            return icon;
        }

        @Override
        public String annotateName(String name, Set<? extends FileObject> files) {
            for(AnnotationProvider ap : annotationProviders.allInstances()) {
                String retVal = ap.annotateName(name, files);
                if (retVal != null) {
                    return retVal;
                }
            }
            return name;
        }

        @Override
        public String annotateNameHtml(String name, Set<? extends FileObject> files) {
            for(AnnotationProvider ap : annotationProviders.allInstances()) {
                String retVal = ap.annotateNameHtml(name, files);
                if (retVal != null) {
                    return retVal;
                }
            }
            return null;
        }
    }
    
    private static class RootFileObject extends RemoteDirectory {

        private RootFileObject(RemoteFileObject wrapper, RemoteFileSystem fileSystem, ExecutionEnvironment execEnv, File cache) {
            super(wrapper, fileSystem, execEnv, null, "", cache);
        }

        @Override
        public boolean isRoot() {
            return true;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public RemoteDirectory getParent() {
            return null;
        }

        @Override
        public Object getAttribute(String attrName) {
            if (FileOperationsProvider.ATTRIBUTE.equals(attrName)) {
                if (USE_VCS) {
                    return FileOperationsProvider.getDefault().getFileOperations(getFileSystem());
                }
            }
            return super.getAttribute(attrName);
        }
    }
    
    private class RemoteFileSupport extends ConnectionNotifier.NamedRunnable {

        public RemoteFileSupport() {
            super(NbBundle.getMessage(RemoteFileSupport.class, "RemoteDownloadTask.TITLE", execEnv.getDisplayName()));
        }
        
        @Override
        protected void runImpl() {
            try {
                onConnect();
            } catch (ConnectException ex) {
                RemoteLogger.getInstance().log(Level.INFO, NbBundle.getMessage(getClass(), "RemoteFileSystemNotifier.ERROR", execEnv), ex);
                ConnectionNotifier.addTask(execEnv, this);
            } catch (InterruptedException ex) {
                RemoteLogger.finest(ex);
            } catch (InterruptedIOException ex) {
                RemoteLogger.finest(ex);
            } catch (IOException ex) {
                RemoteLogger.getInstance().log(Level.INFO, NbBundle.getMessage(getClass(), "RemoteFileSystemNotifier.ERROR", execEnv), ex);
                ConnectionNotifier.addTask(execEnv, this);
            } catch (ExecutionException ex) {
                RemoteLogger.getInstance().log(Level.INFO, NbBundle.getMessage(getClass(), "RemoteFileSystemNotifier.ERROR", execEnv), ex);
                ConnectionNotifier.addTask(execEnv, this);
            }
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(RemoteFileSupport.class, 
                readOnlyConnectNotification.get() ? "RemoteDownloadTask.TEXT_RO" : "RemoteDownloadTask.TEXT", // NOI18N
                execEnv.getDisplayName());
        }

        // NB: it is always called in a specially created thread
        private void onConnect() throws InterruptedException, ConnectException, InterruptedIOException, IOException, ExecutionException {
            fireProblemListeners(null);
        }

        public void addPendingFile(RemoteFileObjectBase fo) {
            RemoteLogger.getInstance().log(Level.FINEST, "Adding notification for {0}:{1}", new Object[]{execEnv, fo.getPath()}); //NOI18N
            ConnectionNotifier.addTask(execEnv, this);
        }
    }
}
