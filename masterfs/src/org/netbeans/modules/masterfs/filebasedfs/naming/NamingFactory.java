/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.masterfs.filebasedfs.naming;

import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;
import org.netbeans.modules.masterfs.filebasedfs.utils.Utils;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;

/**
 * @author Radek Matous
 */
public final class NamingFactory {
    private static NameRef[] names = new NameRef[2];
    private static int namesCount;

    public static synchronized FileNaming fromFile(final File file) {
        final LinkedList<File> list = new LinkedList<File>();
        File current = file;
        while (current != null) {
            list.addFirst(current);
            current = current.getParentFile();
        }

        FileNaming fileName = null;
        for (int i = 0; i < list.size(); i++) {
            File f = list.get(i);
            if("\\\\".equals(f.getPath())) {
                // UNC file - skip \\, \\computerName
                i++;
                continue;
            }
            // returns unknown if last in the list, otherwise directory
            FileType type = (i == list.size() - 1) ? FileType.unknown : FileType.directory;
            fileName = NamingFactory.registerInstanceOfFileNaming(fileName, f, type);
        }

        return fileName;
    }

    public static synchronized int getSize () {
        return namesCount;
    }
    
    public static synchronized FileNaming fromFile(final FileNaming parentFn, final File file, boolean ignoreCache) {
        return NamingFactory.registerInstanceOfFileNaming(parentFn, file, null, ignoreCache, FileType.unknown);
    }
    
    public static synchronized FileNaming checkCaseSensitivity(final FileNaming childName, final File f) throws IOException {
        if (!childName.getFile().getName().equals(f.getName())) {
            boolean isCaseSensitive = !Utils.equals(new File(f,"a"), new File(f,"A"));//NOI18N
            if (!isCaseSensitive) {
                FileName fn = (FileName)childName;
                fn.updateCase(f.getName());
            }
        }
        return childName;
    }

    public static FileNaming[] rename (FileNaming fNaming, String newName, ProvidedExtensions.IOHandler handler) throws IOException {
        final List<FileNaming> all = new ArrayList<FileNaming>();
        
        FileNaming newNaming = fNaming.rename(newName, handler);
        boolean retVal = newNaming != fNaming;
        
        synchronized(NamingFactory.class) {        
            all.add(newNaming);
            renameChildren(fNaming, all);
            return (retVal) ? ((FileNaming[]) all.toArray(new FileNaming[all.size()])) : null;
        }
    }

    private static void renameChildren(FileNaming root, List<FileNaming> all) {
        assert Thread.holdsLock(NamingFactory.class);
        for (int i = 0; i < names.length; i++) {
            NameRef value = names[i];
            while (value != null) {
                FileNaming fN = value.get();
                for (FileNaming up = fN;;) {
                    if (up == null) {
                        break;
                    }
                    if (root.equals(up)) {
                        all.add(fN);
                        break;
                    }
                    up = up.getParent();
                }
                value = value.next();
            }
        }
    }
    
    public static Integer createID(final File file) {
        return Utils.hashCode(file);
    }
    private static FileNaming registerInstanceOfFileNaming(final FileNaming parentName, final File file, FileType type) {
        return NamingFactory.registerInstanceOfFileNaming(parentName, file, null,false, type);       
    }
    
    private static void rehash(int newSize) {
        assert Thread.holdsLock(NamingFactory.class);
        NameRef[] arr = new NameRef[newSize];
        for (int i = 0; i < names.length; i++) {
            NameRef v = names[i];
            if (v == null) {
                continue;
            }
            List<NameRef> linked = new LinkedList<NameRef>();
            while (v != null) {
                linked.add(v);
                v = v.next();
            }
            for (NameRef nr : linked) {
                FileNaming fn = nr.get();
                if (fn == null) {
                    continue;
                }
                Integer id = createID(fn.getFile());
                int index = Math.abs(id) % arr.length;
                nr.next = arr[index];
                arr[index] = nr;
                if (nr.next == null) {
                    nr.next = index;
                }
            }
        }
        names = arr;
    }

    private static FileNaming registerInstanceOfFileNaming(final FileNaming parentName, final File file, final FileNaming newValue,boolean ignoreCache, FileType type) {
        assert Thread.holdsLock(NamingFactory.class);
        
        cleanQueue();
        
        FileNaming retVal;
        Integer key = createID(file);
        int index = Math.abs(key) % names.length;
        Reference ref = getReference(names[index], file);

        FileNaming cachedElement = (ref != null) ? (FileNaming) ref.get() : null;
        if (ignoreCache && cachedElement != null && (
            cachedElement.isDirectory() != file.isDirectory() || !cachedElement.getName().equals(file.getName())
        )) {
            cachedElement = null;
        }

        if (cachedElement != null && Utils.equals(cachedElement.getFile(), file)) {
            retVal = cachedElement;
        } else {
            retVal = (newValue == null) ? NamingFactory.createFileNaming(file, key, parentName, type) : newValue;
            NameRef refRetVal = new NameRef(retVal);
            
            refRetVal.next = names[index];
            names[index] = refRetVal;
            namesCount++;
            if (refRetVal.next == null) {
                refRetVal.next = index;
            }
            if (namesCount * 4 > names.length * 3) {
                rehash(names.length * 2);
            }
        }
        assert retVal != null;
        return retVal;
    }
    
    private static Reference getReference(NameRef value, File f) {
        while (value != null) {
            FileNaming fn = value.get();
            if (fn != null && Utils.equals(fn.getFile(), f)) {
                return value;
            }
            value = value.next();
        }
        return null;
    }

    static enum FileType {file, directory, unknown}
    
    private static FileNaming createFileNaming(
        final File f, Integer theKey, final FileNaming parentName, FileType type
    ) {
        FileName retVal = null;
        //TODO: check all tests for isFile & isDirectory
        if (type.equals(FileType.unknown)) {
            if (f.isDirectory()) {
                type = FileType.directory;
            } else {
                //important for resolving  named pipes
                 type = FileType.file;
            }            
        }
        switch(type) {
            case file:
                retVal = new FileName(parentName, f, theKey);
                break;
            case directory:
                retVal = new FolderName(parentName, f, theKey);
                break;
        }
        return retVal;
    }
    
    public synchronized static String dumpId(Integer id) {
        StringBuilder sb = new StringBuilder();
        final String hex = Integer.toHexString(id);

        sb.append("Showing references to ").append(hex).append("\n");
        int cnt = 0;
        NameRef value = names[id];
        while (value != null) {
            cnt++;
            dumpFileNaming(sb, value.get());
        } 
        sb.append("There was ").append(cnt).append(" references");
        return sb.toString();
    }
    private static void dumpFileNaming(StringBuilder sb, Object fn) {
        if (fn == null) {
            sb.append("null");
        }
        sb.append("FileName: ").append(fn).append("#").
           append(Integer.toHexString(fn.hashCode())).append("@").
           append(Integer.toHexString(System.identityHashCode(fn)))
           .append("\n");
    }
    
    private static final ReferenceQueue<FileNaming> QUEUE = new ReferenceQueue<FileNaming>();
    private static void cleanQueue() {
        assert Thread.holdsLock(NamingFactory.class);
        for (;;) {
            NameRef nr = (NameRef)QUEUE.poll();
            if (nr == null) {
                return;
            }
            int index = nr.getIndex();
            if (names[index] != null) {
                names[index] = names[index].remove(nr);
                namesCount--;
            }
        }
    }
    
    
    private static final class NameRef extends WeakReference<FileNaming> {
        /** either reference to NameRef or to Integer as an index to names array */
        Object next;
        
        public NameRef(FileNaming referent) {
            super(referent, QUEUE);
        }
        
        public Integer getIndex() {
            NameRef nr = this;
            for (;;) {
                if (nr.next instanceof Integer) {
                    return (Integer)nr.next;
                }
                nr = (NameRef) nr.next;
            }
        }
        
        public NameRef next() {
            if (next instanceof Integer) {
                return null;
            }
            return (NameRef)next;
        }
        
        public NameRef remove(NameRef what) {
            if (what == this) {
                return next();
            }
            NameRef me = this;
            while (me.next != what) {
                me = (NameRef)me.next;
            }
            me.next = ((NameRef)me.next).next;
            return this;
        }
    }
}
