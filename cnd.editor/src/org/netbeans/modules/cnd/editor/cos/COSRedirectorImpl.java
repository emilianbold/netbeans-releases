package org.netbeans.modules.cnd.editor.cos;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.OpenSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.CloneableOpenSupport;
import org.openide.windows.CloneableOpenSupportRedirector;

/**
 *
 * @author inikiforov
 */
@ServiceProvider(service = CloneableOpenSupportRedirector.class, position = 1000)
public class COSRedirectorImpl extends CloneableOpenSupportRedirector {

    private static final Logger LOG = Logger.getLogger(COSRedirectorImpl.class.getName());
    private static final boolean ENABLED;
    private static final int L1_CACHE_SIZE = 10;

    private static final Method getDataObjectMethod;

    static {
        Method m = null;
        try {
           m = OpenSupport.Env.class.getDeclaredMethod("getDataObject", new Class[0]); //NOI18N
           m.setAccessible(true);
        } catch (NoSuchMethodException ex) {
            // ignoring
        } catch (SecurityException ex) {
            // ignoring
        } finally {
            getDataObjectMethod = m;
        }
    }

    static {
        String prop = System.getProperty("nb.cosredirector", "true");
        boolean enabled = true;
        try {
            enabled = Boolean.parseBoolean(prop);
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        }
        ENABLED = enabled;
    }
    private final Map<Long, COSRedirectorImpl.Storage> imap = new HashMap<Long, COSRedirectorImpl.Storage>();
    private final LinkedList<Long> cache = new LinkedList<Long>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    protected CloneableOpenSupport redirect(CloneableOpenSupport.Env env) {
        DataObject dobj = getDataObjectIfApplicable(env);
        if (dobj == null) {
            return null;
        }
        Lookup dobjLookup = dobj.getLookup();
        if (dobjLookup == null) {
            return null;
        }
        lock.readLock().lock();
        try {
            for (long n : cache) {
                COSRedirectorImpl.Storage storage = imap.get(n);
                if (storage != null) {
                    if (storage.hasDataObject(dobj)) {
                        CloneableOpenSupport aCes = storage.getCloneableOpenSupport(dobj, env.findCloneableOpenSupport());
                        if (aCes != null) {
                            return aCes;
                        }
                        break;
                    }
                }
            }
        } finally {
            lock.readLock().unlock();
        }
        Path path = FileSystems.getDefault().getPath(FileUtil.getFileDisplayName(dobj.getPrimaryFile()));
        BasicFileAttributes attrs = null;
        try {
            attrs = Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        Object key = null;
        if (attrs != null) {
            key = attrs.fileKey();
        }
        if (key == null) {
            return null;
        }       
        long inode = key.hashCode();
        { // update L1 cache
            lock.writeLock().lock();
            try {
                cache.remove(inode);
                cache.addFirst(inode);
                if (cache.size() > L1_CACHE_SIZE) {
                    cache.removeLast();
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
        COSRedirectorImpl.Storage list;
        lock.writeLock().lock();
        try {
            list = imap.get(inode);
            if (list == null) {
                list = new COSRedirectorImpl.Storage();
                imap.put(inode, list);
            }
        } finally {
            lock.writeLock().unlock();
        }
        if (list.addDataObject(dobj, env.findCloneableOpenSupport())) {
            return null;
        }
        return list.getCloneableOpenSupport(dobj, env.findCloneableOpenSupport());
    }

    @Override
    protected void opened(CloneableOpenSupport.Env env) {
        redirect(env);
    }

    @Override
    protected void closed(CloneableOpenSupport.Env env) {
        DataObject dobj = getDataObjectIfApplicable(env);
        if (dobj == null) {
            return;
        }
        lock.writeLock().lock();
        try {
            for (long n : cache) {
                COSRedirectorImpl.Storage storage = imap.get(n);
                if (storage != null) {
                    if (storage.hasDataObject(dobj)) {
                        CloneableOpenSupport aCes = storage.getCloneableOpenSupport(dobj, env.findCloneableOpenSupport());
                        if (aCes != null) {
                            storage.removeDataObject(dobj);
                            cache.remove((Long) n);
                        }
                        break;
                    }
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private DataObject getDataObjectIfApplicable(CloneableOpenSupport.Env env) {
        if (!ENABLED) {
            return null;
        }
        // disable on windows for now
        if (Utilities.isWindows()) {
            return null;
        }
        if (!(env instanceof OpenSupport.Env)) {
            return null;
        }
        DataObject dobj = null;
        if (getDataObjectMethod != null) {
            try {
                dobj = (DataObject) getDataObjectMethod.invoke(env);
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (dobj == null) { // See CR#7117235
            return null;
        }
        if (!dobj.isValid()) {
            return null;
        }
        FileObject primaryFile = dobj.getPrimaryFile();
        if (primaryFile == null) {
            return null;
        }
        try {
            if (!CndFileUtils.isLocalFileSystem(primaryFile.getFileSystem())) {
                return null;
            }
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
        return dobj;
    }

    private static final class Storage {

        private final List<COSRedirectorImpl.StorageItem> list = new LinkedList<COSRedirectorImpl.StorageItem>();
        private WeakReference<CloneableOpenSupport> cosRef;

        private Storage() {
        }

        private boolean addDataObject(DataObject dao, CloneableOpenSupport cos) {
            Iterator<COSRedirectorImpl.StorageItem> iterator = list.iterator();
            boolean found = false;
            while (iterator.hasNext()) {
                COSRedirectorImpl.StorageItem next = iterator.next();
                DataObject aDao = next.getValidDataObject();
                if (aDao == null) {
                    iterator.remove();
                } else if (aDao.equals(dao)) {
                    found = true;
                }
            }
            if (list.isEmpty()) {
                cosRef = null;
            }
            if (!found) {
                list.add(new COSRedirectorImpl.StorageItem(dao));
            }
            if (cosRef == null || cosRef.get() == null) {
                cosRef = new WeakReference<CloneableOpenSupport>(cos);
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Store SES for {0}", dao.getPrimaryFile().getPath());
                }
                return true;
            }
            return false;
        }

        private void removeDataObject(DataObject dao) {
            Iterator<COSRedirectorImpl.StorageItem> iterator = list.iterator();
            while (iterator.hasNext()) {
                COSRedirectorImpl.StorageItem next = iterator.next();
                DataObject aDao = next.getValidDataObject();
                if (aDao.equals(dao)) {
                    iterator.remove();
                    return;
                }
            }
        }

        private boolean hasDataObject(DataObject dao) {
            Iterator<COSRedirectorImpl.StorageItem> iterator = list.iterator();
            while (iterator.hasNext()) {
                COSRedirectorImpl.StorageItem next = iterator.next();
                DataObject aDao = next.getValidDataObject();
                if (aDao == null) {
                    iterator.remove();
                } else if (aDao.equals(dao)) {
                    return true;
                }
            }
            return false;
        }

        private CloneableOpenSupport getCloneableOpenSupport(DataObject dao, CloneableOpenSupport cos) {
            CloneableOpenSupport aCos = null;
            if (cosRef != null) {
                aCos = cosRef.get();
                if (aCos == null) {
                    list.clear();
                    cosRef = null;
                } else {
                    return aCos;
                }
            }
            Iterator<COSRedirectorImpl.StorageItem> iterator = list.iterator();
            while (iterator.hasNext()) {
                COSRedirectorImpl.StorageItem next = iterator.next();
                DataObject aDao = next.getValidDataObject();
                if (aDao == null) {
                    iterator.remove();
                } else if (aDao.equals(dao)) {
                    cosRef = new WeakReference<CloneableOpenSupport>(cos);
                    aCos = cos;
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "Store SES for {0}", dao.getPrimaryFile().getPath());
                    }
                    break;
                }
            }
            return aCos;
        }
    }

    private static final class StorageItem implements PropertyChangeListener, FileChangeListener {

        private final DataObject dao;
        private AtomicBoolean removed = new AtomicBoolean(false);

        private StorageItem(DataObject dao) {
            this.dao = dao;
            dao.addPropertyChangeListener(this);
            dao.getPrimaryFile().addFileChangeListener(this);
        }

        private DataObject getValidDataObject() {
            if (!removed.get() && dao.isValid()) {
                return dao;
            }
            return null;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (!removed.get()) {
                if (evt.getPropertyName().equals(DataObject.PROP_NAME) ||
                        evt.getPropertyName().equals(DataObject.PROP_VALID) ||
                        evt.getPropertyName().equals(DataObject.PROP_PRIMARY_FILE)) {
                    if (!(evt.getSource() instanceof DataObject)) {
                        return;
                    }
                    DataObject toBeRemoved = (DataObject) evt.getSource();
                    if (dao.equals(toBeRemoved)) {
                        removed.set(true);
                    }
                }
            }
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
        }

        @Override
        public void fileChanged(FileEvent fe) {
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            if (!removed.get()) {
                removed.set(true);
            }
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            if (!removed.get()) {
                removed.set(true);
            }
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
    }
}
