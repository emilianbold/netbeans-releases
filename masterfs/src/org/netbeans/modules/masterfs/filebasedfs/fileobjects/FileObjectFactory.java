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
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.netbeans.modules.masterfs.filebasedfs.naming.FolderName;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.openide.filesystems.FileObject;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileName;

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

    public final FileObject findFileObject(final FileInfo fInfo) {
        FileObject retVal = null;
        File f = fInfo.getFile();
        
        synchronized (allInstances) {
            retVal = this.get(f);
            if (retVal != null) {
                ((BaseFileObj)retVal).isValid(true, f);
            } else {
                final File parent = f.getParentFile();
                if (parent != null) {
                    retVal = this.create(fInfo);
                } else {
                    retVal = this.getRoot();
                }
                
            }
            
            return retVal;
        }
    }


    private BaseFileObj create(final FileInfo fInfo) {
        if (fInfo.isWindowsFloppy()) {
            return null;
        }

        if (!fInfo.isConvertibleToFileObject()) {
            return null;
        }

        final File file = fInfo.getFile();
        FileNaming name = fInfo.getFileNaming();
        name = (name == null) ? NamingFactory.fromFile(file) : name;
        
        if (name == null) return null;

        if (name.isFile()) {
            final FileObj realRoot = new FileObj(file, name);
            return putInCache(realRoot, realRoot.getFileName().getId());
        }
        
        if (!name.isFile() || fInfo.isUNCFolder()) {            
            final FolderObj realRoot = new FolderObj(file, name);
            return putInCache(realRoot, realRoot.getFileName().getId());
        }

        if (fInfo.isUnixSpecialFile()) {
            final FileObj realRoot = new FileObj(file, name);
            return putInCache(realRoot, realRoot.getFileName().getId());
        }

        assert false;
        return null;
    }
    
    public final void refreshAll(final boolean expected) {
        final Set all2Refresh = new HashSet();
        synchronized (allInstances) {
            final Iterator it = allInstances.values().iterator();
            while (it.hasNext()) {
                final Object obj = it.next();
                if (obj instanceof List) {
                    for (Iterator iterator = ((List)obj).iterator(); iterator.hasNext();) {
                        WeakReference ref = (WeakReference) iterator.next();
                        final BaseFileObj fo = (BaseFileObj) ((ref != null) ? ref.get() : null);
                        if (fo != null && (fo.isFolder() || fo.getExistingParent() == null))  {
                            all2Refresh.add(fo);
                        }                                            
                    }
                } else {
                    final WeakReference ref = (WeakReference) obj;
                    final BaseFileObj fo = (BaseFileObj) ((ref != null) ? ref.get() : null);
                    if (fo != null && (fo.isFolder() || fo.getExistingParent() == null))  {
                        all2Refresh.add(fo);
                    }                    
                }
            }
        }


        for (Iterator iterator = all2Refresh.iterator(); iterator.hasNext();) {
            final FileObject fo = (FileObject) iterator.next();
            fo.refresh(expected);
        }
    }

    public final void rename () {
        final Map toRename = new HashMap();
        synchronized (allInstances) {
            final Iterator it = allInstances.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry)it.next();
                final Object obj = entry.getValue();
                final Integer key = (Integer)entry.getKey();
                if (!(obj instanceof List)) {
                    final WeakReference ref = (WeakReference) obj;
                
                    final BaseFileObj fo = (BaseFileObj) ((ref != null) ? ref.get() : null);

                    if (fo != null) {
                        Integer computedId = fo.getFileName().getId();
                        if (!key.equals(computedId)) {
                          toRename.put(key,fo);      
                        }
                    }
                } else {
                    for (Iterator iterator = ((List)obj).iterator(); iterator.hasNext();) {
                        WeakReference ref = (WeakReference) iterator.next();
                        final BaseFileObj fo = (BaseFileObj) ((ref != null) ? ref.get() : null);
                        if (fo != null) {
                            Integer computedId = fo.getFileName().getId();
                            if (!key.equals(computedId)) {
                              toRename.put(key,ref);      
                            }
                        }                        
                    }
                    
                }
            }
            
            for (Iterator iterator = toRename.entrySet().iterator(); iterator.hasNext();) {
                final Map.Entry entry = (Map.Entry ) iterator.next();
                Object key = entry.getKey();
                Object previous = allInstances.remove(key);
                if (previous instanceof List) {
                    List list = (List)previous;
                    list.remove(entry.getValue());
                    allInstances.put(key, previous);
                } else {
                    BaseFileObj bfo = (BaseFileObj )entry.getValue();
                    putInCache(bfo, bfo.getFileName().getId());
                }
            }            
        }
    }    
    
    public final  BaseFileObj get(final File file) {
        final Object o;
        synchronized (allInstances) {
            final Object value = allInstances.get(NamingFactory.createID(file));
            Reference ref = null;
            ref = (Reference) (value instanceof Reference ? value : null);
            ref = (ref == null && value instanceof List ? FileObjectFactory.getReference((List) value, file) : ref);

            o = (ref != null) ? ref.get() : null;
            assert (o == null || o instanceof BaseFileObj);
        }

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

    private FileObjectFactory(final FileInfo fInfo) {
        final File rootFile = fInfo.getFile();
        assert rootFile.getParentFile() == null;

        final BaseFileObj realRoot = create(fInfo);
        root = new RootObj(realRoot);
    }


    private BaseFileObj putInCache(final BaseFileObj realRoot, final Integer id) {
        synchronized (allInstances) {
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
        }

        return realRoot;
    }


}
