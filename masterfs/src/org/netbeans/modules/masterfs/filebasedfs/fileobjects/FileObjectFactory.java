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

import org.netbeans.modules.masterfs.filebasedfs.children.ChildrenCache;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * 
 */
public final class FileObjectFactory {
    private final Map allInstances = Collections.synchronizedMap(new WeakHashMap());
    private RootObj root;

    public static FileObjectFactory getInstance(final FileInfo fInfo) {
        return new FileObjectFactory(fInfo);
    }

    public final RootObj getRoot() {
        return root;
    }

    public final FileObject findFileObject(final File f) {
        final FileObject retVal = this.findFileObjectImpl(f, new ArrayList());
        assert (retVal != null || !new FileInfo(f).isConvertibleToFileObject()) : f.getAbsolutePath();

        return retVal;
    }

    public final void refreshAll(final boolean expected) {
        final Set all2Refresh = new HashSet();
        synchronized (allInstances) {
            final Iterator it = allInstances.values().iterator();
            while (it.hasNext()) {
                final Object obj = it.next();
                //TODO: handle possible List
                assert obj instanceof Reference;
                final WeakReference ref = (WeakReference) obj;
                final FileObject fo = (FileObject) ((ref != null) ? ref.get() : null);
                if (fo != null && fo.isValid() && fo.isFolder()) {
                    all2Refresh.add(fo);
                }
            }
        }


        for (Iterator iterator = all2Refresh.iterator(); iterator.hasNext();) {
            final FileObject fo = (FileObject) iterator.next();
            fo.refresh(expected);
        }


    }

    private synchronized FileObject findFileObjectImpl(final File file, final List keepIt) {
        final FileInfo fInfo = new FileInfo(file);
        FileObject retVal = this.get(file);

        if (retVal == null) {
            final File parent = file.getParentFile();
            if (parent != null) {
                FileObject fileObjectImpl = findFileObjectImpl(parent, keepIt);
                if (!(fileObjectImpl instanceof FolderObj)) return null;
                assert (fileObjectImpl instanceof FolderObj) : fileObjectImpl.getClass().toString() + " file: " + file.getAbsolutePath() + " parent: " + parent.getAbsolutePath();
                final FolderObj parentFo = ((FolderObj) fileObjectImpl);
                if (parentFo != null) {
                    final ChildrenCache parentChildrenCache = parentFo.getChildrenCache();
                    final Mutex.Privileged mutexPrivileged = parentChildrenCache.getMutexPrivileged();

                    mutexPrivileged.enterReadAccess();
                    try {
                        final FileNaming child = parentChildrenCache.getChild(file.getName(), true);

                        if (child != null) {
                            assert child.getFile().equals(file) : (child.getFile().getAbsolutePath() + " | " + file.getAbsolutePath());//NOI18N
                            retVal = this.create(fInfo);
                            assert retVal != null : parent.getAbsolutePath();
                        } else {
                            //TODO: find out why is this code here
                            parentChildrenCache.getChild(file.getName(), false);
                        }
                    } finally {
                        mutexPrivileged.exitReadAccess();
                    }
                }

                assert retVal != null || !fInfo.isConvertibleToFileObject() : (fInfo.getFile().getAbsolutePath() + " isConvertible:   " + fInfo.isConvertibleToFileObject()) ;//NOI18N
                //return null;

            } else {
                retVal = this.getRoot();
            }
            assert retVal != null || !fInfo.isConvertibleToFileObject() : (file.getAbsolutePath() + " isConvertible:   " + !fInfo.isConvertibleToFileObject());//NOI18N
        }
        keepIt.add(retVal);
        return retVal;
    }


    public final synchronized BaseFileObj get(final File file) {
        final Object value = allInstances.get(NamingFactory.createID(file));
        Reference ref = null;
        ref = (Reference) (value instanceof Reference ? value : null);
        ref = (ref == null && value instanceof List ? FileObjectFactory.getReference((List) value, file) : ref);

        final Object o = (ref != null) ? ref.get() : null;
        assert (o == null || o instanceof BaseFileObj);

        return (BaseFileObj) o;
    }

    private static Reference getReference(final List list, final File file) {
        Reference retVal = null;
        for (int i = 0; retVal == null && i < list.size(); i++) {
            final Reference ref = (Reference) list.get(i);
            final BaseFileObj cachedElement = (ref != null) ? (BaseFileObj) ref.get() : null;
            if (cachedElement != null && cachedElement.getFileName().getFile().compareTo(file) == 0) {
                retVal = ref;
            }
        }
        return retVal;
    }

    private BaseFileObj create(final FileInfo fInfo) {
        if (fInfo.isWindowsFloppy()) {
            return null;
        }

        final File file = fInfo.getFile();

        if (!fInfo.isConvertibleToFileObject()) {
            return null;
        }

        if (fInfo.isDirectory() || fInfo.isUNCFolder()) {
            final FolderObj realRoot = new FolderObj(file);
            return putInCache(realRoot, realRoot.getFileName().getId());
        }

        if (fInfo.isFile()) {
            final FileObj realRoot = new FileObj(file);
            return putInCache(realRoot, realRoot.getFileName().getId());
        }

        if (fInfo.isUnixSpecialFile()) {
            final FileObj realRoot = new FileObj(file);
            return putInCache(realRoot, realRoot.getFileName().getId());
        }

        assert false;
        return null;
    }


    private FileObjectFactory(final FileInfo fInfo) {
        final File rootFile = fInfo.getFile();
        assert rootFile.getParentFile() == null;

        final BaseFileObj realRoot = create(fInfo);
        root = new RootObj(realRoot);
    }


    private synchronized BaseFileObj putInCache(final BaseFileObj realRoot, final Integer id) {
        final WeakReference value = new WeakReference(realRoot);
        final Object instanceInCache = allInstances.put(id, value);

        final boolean isList = (instanceInCache instanceof List);
        if (instanceInCache != null) {
            if (!isList) {
                assert (instanceInCache instanceof WeakReference);
                final Reference ref = (Reference) ((instanceInCache instanceof WeakReference) ? instanceInCache : null);
                final boolean keepRef = (ref != null && ref.get() == null);
                if (!keepRef) {
                    final List l = new ArrayList();
                    l.add(instanceInCache);
                    l.add(value);
                    allInstances.put(id, l);
                }
            } else {
                ((List) instanceInCache).add(value);
            }
        }

        return realRoot;
    }


}
