/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.masterfs.filebasedfs.children;

import org.netbeans.modules.masterfs.filebasedfs.naming.FileName;
import org.netbeans.modules.masterfs.filebasedfs.naming.FileNaming;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;
import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;

import java.io.File;
import java.util.*;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.openide.util.Mutex;

/**
 * @author Radek Matous
 */
public class ChildrenSupport {

    static final int NO_CHILDREN_CACHED = 0;
    static final int SOME_CHILDREN_CACHED = 1;
    static final int ALL_CHILDREN_CACHED = 2;

    private Set<FileNaming> notExistingChildren;
    private Set<FileNaming> existingChildren;
    private int status = ChildrenSupport.NO_CHILDREN_CACHED;
    private static Mutex.Privileged mutexPrivileged = new Mutex.Privileged();
    private static Mutex mutex = new Mutex(mutexPrivileged);

    public ChildrenSupport() {
    }

    public final Mutex.Privileged getMutexPrivileged() {
        return mutexPrivileged;
    }

    public Set<FileNaming> getCachedChildren() {
        return getExisting(false);
    }

    public synchronized Set<FileNaming> getChildren(final FileNaming folderName, final boolean rescan) {
        if (rescan || !isStatus(ChildrenSupport.ALL_CHILDREN_CACHED))  {
            rescanChildren(folderName);
            setStatus(ChildrenSupport.ALL_CHILDREN_CACHED);
        } /*else if (!isStatus(ChildrenSupport.ALL_CHILDREN_CACHED)) {

        }*/

        //assert status == ChildrenSupport.ALL_CHILDREN_CACHED;
        return getExisting(false);
    }

    public boolean isCacheInitialized() {
        return !isStatus(ChildrenSupport.NO_CHILDREN_CACHED);
    }

    public synchronized FileNaming getChild(final String childName, final FileNaming folderName, final boolean rescan) {
        FileNaming retval = null;
        if (rescan || isStatus(ChildrenSupport.NO_CHILDREN_CACHED)) {
            retval = rescanChild(folderName, childName);
        } else if (isStatus(ChildrenSupport.SOME_CHILDREN_CACHED)) {
            retval = lookupChildInCache(folderName, childName, true);
            if (retval == null && lookupChildInCache(folderName, childName, false) == null) {
                retval = rescanChild(folderName, childName);
            }
        } else if (isStatus(ChildrenSupport.ALL_CHILDREN_CACHED)) {
            retval = lookupChildInCache(folderName, childName, true);
        }
        setStatus(ChildrenSupport.SOME_CHILDREN_CACHED);
        return retval;
    }

    /*public boolean existsldInCache(final FileNaming folder, final String childName) {
        return lookupChildInCache(folder, childName) != null;
    }*/

    public synchronized void removeChild(final FileNaming folderName, final FileNaming childName) {
        assert childName != null;
        assert childName.getParent().equals(folderName);
        getExisting().remove(childName);
        getNotExisting().add(childName);
    }

    private synchronized void addChild(final FileNaming folderName, final FileNaming childName) {
        assert childName != null;
        assert childName.getParent().equals(folderName);
        getExisting().add(childName);
        getNotExisting().remove(childName);
    }



    public synchronized Map<FileNaming, Integer> refresh(final FileNaming folderName) {
        Map<FileNaming, Integer> retVal = new HashMap<FileNaming, Integer>();
        Set<FileNaming> e = new HashSet<FileNaming>(getExisting(false));
        Set<FileNaming> nE = new HashSet<FileNaming>(getNotExisting(false));

        if (isStatus(ChildrenSupport.SOME_CHILDREN_CACHED)) {
            Set<FileNaming> existingToCheck = new HashSet<FileNaming>(e);
            for (FileNaming fnToCheck : existingToCheck) {
                FileNaming fnRescanned = rescanChild(folderName, fnToCheck.getName());
                if (fnRescanned == null) {
                    retVal.put(fnToCheck, ChildrenCache.REMOVED_CHILD);
                }
            }

            Set<FileNaming> notExistingToCheck = new HashSet<FileNaming>(nE);
            for (FileNaming fnToCheck : notExistingToCheck) {
                assert fnToCheck != null;
                FileNaming fnRescanned = rescanChild(folderName, fnToCheck.getName());
                if (fnRescanned != null) {
                    retVal.put(fnToCheck, ChildrenCache.ADDED_CHILD);
                }
            }
        } else if (isStatus(ChildrenSupport.ALL_CHILDREN_CACHED)) {
            retVal = rescanChildren(folderName);
        }
        return retVal;
    }

    @Override
    public String toString() {
        return getExisting(false).toString();
    }

    boolean isStatus(int status) {
        return this.status == status;
    }

    private void setStatus(int status) {
        if (this.status < status) {
            this.status = status;
        }
    }


    private FileNaming rescanChild(final FileNaming folderName, final String childName) {
        final File folder = folderName.getFile();
        final File child = new File(folder, childName);
        final FileInfo fInfo = new FileInfo(child);

        FileNaming retval = (fInfo.isConvertibleToFileObject()) ? NamingFactory.fromFile(folderName, child) : null;
        if (retval != null) {
            addChild(folderName, retval);
        } else {
            FileName fChild = new FileName(folderName, child) {

                @Override
                public boolean isDirectory() {
                    return false;
                }

                @Override
                public boolean isFile() {
                    return false;
                }
            };

            removeChild(folderName,  fChild);
        }

        return retval;
    }

    private Map<FileNaming, Integer> rescanChildren(final FileNaming folderName) {
        final Map<FileNaming, Integer> retval = new HashMap<FileNaming, Integer>();
        final Set<FileNaming> newChildren = new LinkedHashSet<FileNaming>();

        final File folder = folderName.getFile();
        assert folderName.getFile().getAbsolutePath().equals(folderName.toString());

        final File[] children = folder.listFiles();
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                final FileInfo fInfo = new FileInfo(children[i],1);
                if (fInfo.isConvertibleToFileObject()) {
                    FileNaming child = NamingFactory.fromFile(folderName, children[i]);
                    newChildren.add(child);
                }
            }
        } else if (folder.exists()) { // #150009 - children == null -> folder does not exists, or an I/O error occurs
            // folder.listFiles() failed with I/O exception - do not remove children
            return retval;
        }

        Set<FileNaming> deleted = new HashSet<FileNaming>(getExisting(false));
        deleted.removeAll(newChildren);
        for (FileNaming fnRem : deleted) {
            removeChild(folderName, fnRem);
            retval.put(fnRem, ChildrenCache.REMOVED_CHILD);
        }

        Set<FileNaming> added = new HashSet<FileNaming>(newChildren);
        added.removeAll(getExisting(false));
        for (FileNaming fnAdd : added) {
            addChild(folderName, fnAdd);
            retval.put(fnAdd, ChildrenCache.ADDED_CHILD);
        }

        return retval;
    }

    private FileNaming lookupChildInCache(final FileNaming folder, final String childName, boolean lookupExisting) {
        final File f = new File(folder.getFile(), childName);
        final Integer id = NamingFactory.createID(f);

        class FakeNaming implements FileNaming {
            public FileNaming lastEqual;

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
            public boolean rename(String name, ProvidedExtensions.IOHandler h) {
                // not implemented, as it will not be called
                throw new IllegalStateException();
            }

            @Override
            public boolean equals(Object obj) {
                if (hashCode() == obj.hashCode()) {
                    assert lastEqual == null : "Just one can be there"; // NOI18N
                    if (obj instanceof FileNaming) {
                        lastEqual = (FileNaming)obj;
                    }
                    return true;
                }
                return false;
            }

            @Override
            public int hashCode() {
                return id.intValue();
            }

            public Integer getId(boolean recompute) {
                return id;

            }

            public boolean isFile() {
                return this.getFile().isFile();
            }

            public boolean isDirectory() {
                return !isFile();
            }
        }
        FakeNaming fake = new FakeNaming();

        final Set<FileNaming> cache = (lookupExisting) ? getExisting(false) : getNotExisting(false);
        if (cache.contains(fake)) {
            assert fake.lastEqual != null : "If cache contains the object, we set lastEqual"; // NOI18N
            return fake.lastEqual;
        } else {
            return null;
        }
    }

    private synchronized Set<FileNaming> getExisting() {
        return getExisting(true);
    }

    private synchronized Set<FileNaming> getExisting(boolean init) {
        if (init && existingChildren == null) {
            existingChildren = new HashSet<FileNaming>();
        }
        return existingChildren != null ? existingChildren : new HashSet<FileNaming>();
    }

    private synchronized Set<FileNaming> getNotExisting() {
        return getNotExisting(true);
    }

    private synchronized Set<FileNaming> getNotExisting(boolean init) {
        if (init && notExistingChildren == null) {
            notExistingChildren = new HashSet<FileNaming>();
        }
        return notExistingChildren != null ? notExistingChildren : new HashSet<FileNaming>();
    }
}
