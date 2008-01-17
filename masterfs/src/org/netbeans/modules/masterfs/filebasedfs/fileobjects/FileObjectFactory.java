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
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;
import org.openide.filesystems.FileObject;

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
    public int getSize() {        
        synchronized (allInstances) {
            return allInstances.size();
        }
    }

    public final FileObject findFileObject(final FileInfo fInfo) {
        FileObject retVal = null;
        File f = fInfo.getFile();
        
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
     
            assert retVal == null || retVal.isValid() : retVal.toString();
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
            /*FolderObj par = (FolderObj)realRoot.getExistingParent();
            if (par != null && par.getChildrenCache().getChild(name.getName(), false) == null) {
                //par.refresh(true);
                //par.getChildrenCache().getChild(name.getName(), true);
            }*/
            return putInCache(realRoot, realRoot.getFileName().getId());
        }
        
        if (!name.isFile() && name.isDirectory()) {            
            final FolderObj realRoot = new FolderObj(file, name);
            /*FolderObj par = (FolderObj)realRoot.getExistingParent();
            if (par != null && par.getChildrenCache().getChild(name.getName(), false) == null) {
                //par.refresh(true);
                //par.getChildrenCache().getChild(name.getName(), true); 
            }*/
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
}
