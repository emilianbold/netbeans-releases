/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.masterfs.filebasedfs.children;

import org.netbeans.modules.masterfs.filebasedfs.naming.FileName;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;

import java.io.File;
import java.util.*;

/**
 * @author Radek Matous
 */
public final class ChildrenSupport {
    
    private static final int NO_CHILDREN_CACHED = 0;
    private static final int SOME_CHILDREN_CACHED = 1;
    private static final int ALL_CHILDREN_CACHED = 2;
    
    private Set childrenCache;
    private int status = ChildrenSupport.NO_CHILDREN_CACHED;
    
    public ChildrenSupport() {
    }
    
    public synchronized Set getChildren(final FileNaming folderName, final boolean rescan) {
        if (rescan) {
            switch (getStatus()) {
                case ChildrenSupport.NO_CHILDREN_CACHED:
                case ChildrenSupport.SOME_CHILDREN_CACHED:
                    final Set newChildrenCache = ChildrenSupport.rescanChildren(folderName);
                    //TODO: UNCPath workaround
                    final boolean isUNCHack = getStatus() == ChildrenSupport.SOME_CHILDREN_CACHED && newChildrenCache.isEmpty() && new FileInfo(folderName.getFile()).isUNCFolder();
                    if (!isUNCHack) {
                        setChildrenCache(newChildrenCache);
                    }
                    
                    break;
            }
            
            status = ChildrenSupport.ALL_CHILDREN_CACHED;
        }
        return (childrenCache != null) ? getChildrenCache() : new HashSet();
    }
    
    public synchronized FileNaming getChild(final String childName, final FileNaming folderName, final boolean rescan) {
        FileNaming retVal = null;
        switch (getStatus()) {
            case ChildrenSupport.ALL_CHILDREN_CACHED:
                retVal = lookupChildInCache(folderName, childName);
                if (!rescan) break;
            case ChildrenSupport.SOME_CHILDREN_CACHED:
                if (getStatus() != ChildrenSupport.ALL_CHILDREN_CACHED) retVal = lookupChildInCache(folderName, childName);
            case ChildrenSupport.NO_CHILDREN_CACHED:
                if (retVal == null || rescan) {
                    final FileNaming original = retVal;
                    retVal = ChildrenSupport.rescanChild(folderName, childName);
                    if (retVal != null) {
                        getChildrenCache().add(retVal);
                    } else {
                        getChildrenCache().remove(original);
                    }
                }
                if (retVal != null && getStatus() == ChildrenSupport.NO_CHILDREN_CACHED) {
                    status = (ChildrenSupport.SOME_CHILDREN_CACHED);
                }
                break;
        }
        
        return retVal;
    }
    
    public boolean existsldInCache(final FileNaming folder, final String childName) {    
        return lookupChildInCache(folder, childName) != null;
    }
    
    private FileName lookupChildInCache(final FileNaming folder, final String childName) {
        final File f = new File(folder.getFile(), childName);
        final Integer id = NamingFactory.createID(f);
        
        class FakeNaming implements FileNaming {
            public FileName lastEqual;
            
            public  String getName() {
                return childName;
            }
            public FileNaming getParent() {
                return folder;
            }
            public boolean isRoot() {
                return false;
            }
            
            public File getFile() {
                return f;
            }
            
            public Integer getId() {
                return id;
            }
            public boolean rename(String name) {
                // not implemented, as it will not be called
                throw new IllegalStateException();
            }
            
            public boolean equals(Object obj) {
                if (hashCode() == obj.hashCode()) {
                    assert lastEqual == null : "Just one can be there"; // NOI18N
                    lastEqual = (FileName)obj;
                    return true;
                }
                return false;
            }
            
            public int hashCode() {
                return id.intValue();
            }
            
            public Integer getId(boolean recompute) {
                return id;
            }
            
            public boolean isFile() {
                return this.getFile().isFile();
            }
        }
        FakeNaming fake = new FakeNaming();
        
        final Set cache = getChildrenCache();
        if (cache.contains(fake)) {
            assert fake.lastEqual != null : "If cache contains the object, we set lastEqual"; // NOI18N
            return fake.lastEqual;
        } else {
            return null;
        }
    }
    
    private static FileNaming rescanChild(final FileNaming folderName, final String childName) {
        final File folder = folderName.getFile();
/*
java.lang.AssertionError: E:\work\nb_all8\openide\masterfs\build
        at org.netbeans.modules.masterfs.filebasedfs.children.ChildrenSupport.rescanChild(ChildrenSupport.java:101)
        at org.netbeans.modules.masterfs.filebasedfs.children.ChildrenSupport.getChild(ChildrenSupport.java:70)
        at org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj$FolderChildrenCache.getChild(FolderObj.java:327)
        at org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory.findFileObjectImpl(FileObjectFactory.java:76)
        at org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory.findFileObject(FileObjectFactory.java:31)
        at org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem.findFileObject(FileBasedFileSystem.java:69)
        at org.netbeans.modules.masterfs.filebasedfs.FileBasedURLMapper.getFileObjects(FileBasedURLMapper.java:103)
        at org.openide.filesystems.URLMapper.findFileObject(URLMapper.java:181)
        at org.openide.filesystems.FileUtil.toFileObject(FileUtil.java:354)
        at org.netbeans.modules.apisupport.project.ClassPathProviderImpl.findClassPath(ClassPathProviderImpl.java:74)
        at org.netbeans.modules.java.project.ProjectClassPathProvider.findClassPath(ProjectClassPathProvider.java:36)
        at org.netbeans.api.java.classpath.ClassPath.getClassPath(ClassPath.java:396)
        at org.netbeans.modules.java.JavaNode.resolveIcons(JavaNode.java:552)
        at org.netbeans.modules.java.JavaNode$4.run(JavaNode.java:769)
        at org.openide.util.Task.run(Task.java:136)
        at org.openide.util.RequestProcessor$Task.run(RequestProcessor.java:330)
        at org.openide.util.RequestProcessor$Processor.run(RequestProcessor.java:686)
 */
        //assert folder.isDirectory() : folder.getAbsolutePath();
        
        final File child = new File(folder, childName);
        final FileInfo fInfo = new FileInfo(child);
        return (fInfo.isConvertibleToFileObject()) ? NamingFactory.fromFile(folderName, child) : null;
    }
    
    private static Set rescanChildren(final FileNaming folderName) {
        final Set retVal = new LinkedHashSet();
        
        final File folder = folderName.getFile();
        assert folderName.getFile().getAbsolutePath ().equals (folderName.toString ());
        
        final File[] childs = folder.listFiles();
        //assert childs != null : folder.getAbsolutePath();
        if (childs != null) {
            for (int i = 0; i < childs.length; i++) {
                final FileInfo fInfo = new FileInfo(childs[i]);
                if (fInfo.isConvertibleToFileObject()) {
                    FileNaming child = NamingFactory.fromFile(folderName, childs[i]);
                    assert child.getParent() == folderName;
                    retVal.add(child);
                } 
            }
        }
        
        return retVal;
    }
    
    private synchronized Set getChildrenCache() {
        if (childrenCache == null) {
            setChildrenCache(new LinkedHashSet());
        }
        return childrenCache;
    }
    
    private void setChildrenCache(final Set childrenCache) {
        //FileInfo fileInfo = new FileInfo (getFileName().getFile());
        this.childrenCache = childrenCache;
    }
    
    public Map refresh(final FileNaming folderName) {
        final Map retVal = new HashMap();
        final Set oldChildren = new HashSet(getChildrenCache());
        final Set newChildren = (getStatus() == ChildrenSupport.ALL_CHILDREN_CACHED) ? 
                ChildrenSupport.rescanChildren(folderName) : ChildrenSupport.getSubsetOfExisting (oldChildren);

        if (status == ChildrenSupport.SOME_CHILDREN_CACHED && newChildren.size() < oldChildren.size()) {
            setChildrenCache(ChildrenSupport.rescanChildren(folderName));
            status = ChildrenSupport.ALL_CHILDREN_CACHED;
        } else {                     
            setChildrenCache(newChildren);
        }
        
        final Set removed = new HashSet(oldChildren);
        removed.removeAll(newChildren);
        
        for (Iterator iterator = removed.iterator(); iterator.hasNext();) {
            final FileName removedItem = (FileName) iterator.next();
            retVal.put(removedItem, ChildrenCache.REMOVED_CHILD);
        }
        
        if (getStatus() == ChildrenSupport.ALL_CHILDREN_CACHED) {
            final Set added = new HashSet(newChildren);
            added.removeAll(oldChildren);
            
            for (Iterator iterator = added.iterator(); iterator.hasNext();) {
                final FileName addedItem = (FileName) iterator.next();
                retVal.put(addedItem, ChildrenCache.ADDED_CHILD);
            }
        }
        return retVal;
    }

    private static Set getSubsetOfExisting(Set oldChildren) {
        Set retVal = new HashSet ();
        for (Iterator iterator = oldChildren.iterator(); iterator.hasNext();) {
            final FileName fileName  = (FileName) iterator.next();
            File f = fileName.getFile();
            if (f.exists()) {
                retVal.add(fileName);
            }
            
        }
        return retVal;
    }

    public String toString() {
        return childrenCache.toString();
    }

    private int getStatus() {
        return status;
    }
    
}
