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
import java.io.ByteArrayOutputStream;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.openide.filesystems.FileObject;

import java.io.File;
import java.io.PrintStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.filebasedfs.children.ChildrenCache;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.Utilities;

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
    public int getSize() {        
        int retval = 0;
    
        List list = new ArrayList();
        synchronized (allInstances) {
            list.addAll(allInstances.values());
        }
        List list2 = new ArrayList();

        
        for (Iterator it = list.iterator(); it.hasNext();) {
            Object obj = it.next();
            if (obj instanceof Reference) {
                list2.add(obj);
            } else {
                list2.addAll((List)obj);
            }
        }
        
        for (Iterator it = list2.iterator(); it.hasNext();) {
            Reference ref = (Reference) it.next();
            FileObject fo = (ref != null) ? (FileObject) ref.get() : null;
            if (fo != null) {
                retval++;
            }
        }

        return retval;
    }
    public static enum Caller {
        ToFileObject,GetFileObject,GetChildern,GetParent, Others
    }
    private static int[] compatibleExistsCalls = new int[Caller.values().length];
    private static int[] optimizedExistsCalls = new int[compatibleExistsCalls.length];    
    
    public FileObject findFileObject(final File file, FileBasedFileSystem lfs, Caller caller) {
        return findFileObject(new FileInfo(file), lfs, caller);
    }

    
    public FileObject findFileObject(FileInfo fInfo, FileBasedFileSystem lfs, Caller caller) {        
        return findFileObject(fInfo, lfs, caller, true);
    }

    public FileObject findFileObject(FileInfo fInfo, FileBasedFileSystem lfs, Caller caller, boolean warningOn) {        
        assert lfs != null;        
        File file = fInfo.getFile();
        FileObject retVal = null;
        FolderObj parent = BaseFileObj.getExistingParentFor(file, lfs); 
        FileNaming child = null;
        if (parent != null) {
            final ChildrenCache childrenCache = parent.getChildrenCache();
            final Mutex.Privileged mutexPrivileged = childrenCache.getMutexPrivileged();
            mutexPrivileged.enterReadAccess();
            try {
                final String nameExt = BaseFileObj.getNameExt(file);
                child = childrenCache.getChild(nameExt, false);
            } finally {
                mutexPrivileged.exitReadAccess();
            }
        }
        assert printWarning(file, caller, parent, child, warningOn);
        incrementFor(caller, compatibleExistsCalls);
        boolean exists = optimizedExists(file, caller, parent, child);
        
        printCalls(caller);
        if (parent != null) {
            if (child != null) {
                if (exists) {
                    retVal = getOrCreate(new FileInfo(file, 1));
                } else {
                    parent.refresh(true);
                }
            } else {
                if (exists) {
                    parent.refresh(true);
                    retVal = getOrCreate(new FileInfo(file, 1));
                } 
            }
        } else {
            retVal = exists ? getOrCreate(new FileInfo(file, 1)) : null;
        }
                
        return retVal;        
    }

    private boolean isWarning(File file, Caller caller, FileObject parent, FileNaming child, boolean warningOn) {
        boolean warning = false;
        BaseFileObj foForFile = null;
        if (FileBasedFileSystem.WARNINGS) {
            warning = file.exists() != optimizedExists(file, caller, parent, child);
            warning = warning && warningOn && !WriteLockUtils.hasActiveLockFileSigns(file.getAbsolutePath());
        }
        return warning;
    }
    
    private boolean optimizedExists(File file, Caller caller, FileObject parent, FileNaming child) {
        boolean exist = false;
        FileObject foForFile = null;
        switch(caller) {
            case GetParent:
                exist = true;
                break;
            case ToFileObject:
                foForFile = get(file);
                exist = (foForFile != null && foForFile.isValid()) || (child != null && foForFile == null) ? true : touchExists(file, caller);
                break;
            case GetChildern:                
            case Others:                                    
            case GetFileObject:
                exist = (parent != null) ? child != null : (((foForFile = get(file)) != null && foForFile.isValid()) || touchExists(file, caller));
                break;
        }
        return exist;
    }

    
    private static boolean touchExists(File f, Caller caller) {
        incrementFor(caller, optimizedExistsCalls);
        return f.exists();
    }
    
    private static int incrementFor(Caller caller, int[] where) {        
        return where[indexFor(caller)] += 1;        
    }
    
    private void printCalls(Caller caller) {
        if (FileBasedFileSystem.PERF_PRINTING) {
            boolean print = false;
            assert print = true;
            if (print) {
                if ((compatibleExistsCalls[indexFor(caller)] % 1000) == 0) {
                    int totalRC = 0;
                    int totalNO = 0;
                    Caller[] callers = Caller.values();
                    for (int i = 0; i < callers.length; i++) {
                        FileObjectFactory.Caller caller2 = callers[i];
                        int idx = indexFor(caller2);
                        int rC = optimizedExistsCalls[idx];
                        int nO = compatibleExistsCalls[idx];
                        totalRC += rC;
                        totalNO += nO;
                        System.out.println(caller2.name() + " comp: " + nO + "  optim: " + rC);
                    }                
                    System.out.println("Total: " + " comp: " + totalNO + "  optim: " + totalRC);
                }
            }
        }
    }
    
    private static int indexFor(Caller caller) {
        return Arrays.binarySearch(Caller.values(), caller);
    }                        
            
    private final FileObject getOrCreate(final FileInfo fInfo) {        
        FileObject retVal = null;
        File f = fInfo.getFile();

        boolean issue45485 = fInfo.isWindows() && f.getName().endsWith(".");//NOI18N        
        if (issue45485) {
            File f2 = FileUtil.normalizeFile(f);
            issue45485 = !f2.getName().endsWith(".");
            if (issue45485) return null;
        }
        if (fInfo.getFile().getParentFile() == null) {
            return getRoot();
        }
        
        
        synchronized (allInstances) {
            retVal = this.get(f);
            if (retVal == null || !retVal.isValid()) {
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

        if (name.isFile() && !name.isDirectory()) {
            final FileObj realRoot = new FileObj(file, name);
            return putInCache(realRoot, realRoot.getFileName().getId());
        }
        
        if (!name.isFile() && name.isDirectory()) {            
            final FolderObj realRoot = new FolderObj(file, name);
            return putInCache(realRoot, realRoot.getFileName().getId());
        }

        assert false;
        return null;
    }
    
    public final void refreshAll(final boolean expected) {
        Set all2Refresh = collectForRefresh();
        refresh(all2Refresh, expected);
    }

    public void refreshFor(File f) {
        Set all2Refresh = collectForRefresh();
        refresh(all2Refresh, f);        
    }    
        
    private Set collectForRefresh() {
        final Set all2Refresh = new HashSet();
        synchronized (allInstances) {
            final Iterator it = allInstances.values().iterator();
            while (it.hasNext()) {
                final Object obj = it.next();
                if (obj instanceof List) {
                    for (Iterator iterator = ((List) obj).iterator(); iterator.hasNext();) {
                        WeakReference ref = (WeakReference) iterator.next();
                        final BaseFileObj fo = (BaseFileObj) ((ref != null) ? ref.get() : null);
                        if (fo != null) {
                            all2Refresh.add(fo);
                        }
                    }
                } else {
                    final WeakReference ref = (WeakReference) obj;
                    final BaseFileObj fo = (BaseFileObj) ((ref != null) ? ref.get() : null);
                    if (fo != null) {
                        all2Refresh.add(fo);
                    }
                }
            }
        }
        return all2Refresh;
    }

    private boolean printWarning(File file, Caller caller, FolderObj parent, FileNaming child, boolean warningOn) {
        if (isWarning(file, caller, parent, child, warningOn)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(bos);
            new Exception().printStackTrace(ps);
            ps.close();
            String h = file.exists() ? "WARNING: externally created " : "WARNING: externally deleted "; //NOI18N
            h += (file.isDirectory() ? "folder: " : "file: ") + file.getAbsolutePath(); //NOI18N
            h += "  - please report. (For additional information see: http://wiki.netbeans.org/wiki/view/FileSystems)";
            if (Utilities.isWindows()) {
                h = h.replace('\\', '/');
            }
            Logger.getLogger("org.netbeans.modules.masterfs.filebasedfs.fileobjects.FolderObj").log(Level.WARNING, bos.toString().replaceAll("java[.]lang[.]Exception", h));
        }
        return true;
    }
    
    private void refresh(final Set all2Refresh, File file) {
        for (Iterator iterator = all2Refresh.iterator(); iterator.hasNext();) {
            final BaseFileObj fo = (BaseFileObj) iterator.next();
            if (isParentOf(file, fo.getFileName().getFile())) {
                fo.refresh(true);
            }
        }
    }    
       
    private void refresh(final Set all2Refresh, final boolean expected) {
        for (Iterator iterator = all2Refresh.iterator(); iterator.hasNext();) {
            final BaseFileObj fo = (BaseFileObj) iterator.next();
            fo.refresh(expected);
        }
    }    
    
    public static boolean isParentOf(final File dir, final File file) {
        Stack stack = new Stack();
        File tempFile = file;
        while (tempFile != null && !tempFile.equals(dir)) {
            stack.push(tempFile.getName());
            tempFile = tempFile.getParentFile();
        }
        return tempFile != null;
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
        BaseFileObj retval = (BaseFileObj) o;
        if (retval != null) {
            if (!file.getName().equals(retval.getNameExt())) {
                if (!file.equals(retval.getFileName().getFile())) {
                    retval = null;
                }
            }
        }
        return retval;
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


    private BaseFileObj putInCache(final BaseFileObj newValue, final Integer id) {
        synchronized (allInstances) {
            final WeakReference newRef = new WeakReference(newValue);
            final Object listOrReference = allInstances.put(id, newRef);

            if (listOrReference != null) {                
                if (listOrReference instanceof List) {
                    ((List) listOrReference).add(newRef);                    
                    allInstances.put(id, listOrReference);
                } else {
                    assert (listOrReference instanceof WeakReference);
                    final Reference oldRef = (Reference) listOrReference;
                    BaseFileObj oldValue = (oldRef != null) ? (BaseFileObj)oldRef.get() : null;
                    
                    if (oldValue != null && !newValue.getFileName().equals(oldValue.getFileName())) {
                        final List l = new ArrayList();
                        l.add(oldRef);
                        l.add(newRef);
                        allInstances.put(id, l);
                    }                    
                }
            }
        }

        return newValue;
    }

    @Override
    public String toString() {
        List list = new ArrayList();
        synchronized (allInstances) {
            list.addAll(allInstances.values());
        }
        List l2 = new ArrayList();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Reference ref = (Reference) it.next();
            FileObject fo = (ref != null) ? (FileObject) ref.get() : null;
            if (fo != null) {
                l2.add(fo.getPath());
            }
        }

        
        return l2.toString();
    }        
}
