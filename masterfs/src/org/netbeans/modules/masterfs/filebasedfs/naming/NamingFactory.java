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
import java.lang.ref.WeakReference;
import java.util.*;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;

/**
 * @author Radek Matous
 */
public final class NamingFactory {
    private static final Map nameMap = new WeakHashMap();

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
        return nameMap.size();
    }
    
    public static synchronized FileNaming fromFile(final FileNaming parentFn, final File file, boolean ignoreCache) {
        return NamingFactory.registerInstanceOfFileNaming(parentFn, file, null, ignoreCache, FileType.unknown);
    }
    
    public static synchronized FileNaming checkCaseSensitivity(final FileNaming childName, final File f) throws IOException {
        if (!childName.getFile().getName().equals(f.getName())) {
            boolean isCaseSensitive = !new File(f,"a").equals(new File(f,"A"));//NOI18N
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
        for (Iterator iterator = nameMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            
            List list;
            Object value = entry.getValue();
            if (value instanceof Reference) {
                list = Collections.singletonList((Reference)value);
            } else if (value instanceof List) {
                list = (List)value;
            } else {
                list = Collections.emptyList();
            }
            
            for (int i = 0; i < list.size(); i++) {
                FileNaming fN = (FileNaming)((Reference) list.get(i)).get();
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
            }
        }
    }

    public static synchronized Integer createID(final File file) {
        assert Thread.holdsLock(NamingFactory.class);
        
        Integer[] theKey = { file.hashCode() };
        final Object value = nameMap.get(theKey[0]);
        Reference ref = getReference(value, file, theKey);
        return theKey[0];
    }
    private static FileNaming registerInstanceOfFileNaming(final FileNaming parentName, final File file, FileType type) {
        return NamingFactory.registerInstanceOfFileNaming(parentName, file, null,false, type);       
    }

    private static FileNaming registerInstanceOfFileNaming(final FileNaming parentName, final File file, final FileNaming newValue,boolean ignoreCache, FileType type) {
        assert Thread.holdsLock(NamingFactory.class);
        
        FileNaming retVal;
        Integer[] theKey = { file.hashCode() };
        final Object value = nameMap.get(theKey[0]);
        Reference ref = getReference(value, file, theKey);

        FileNaming cachedElement = (ref != null) ? (FileNaming) ref.get() : null;
        if (ignoreCache && cachedElement != null && (
            cachedElement.isDirectory() != file.isDirectory() || !cachedElement.getName().equals(file.getName())
        )) {
            cachedElement = null;
        }

        if (cachedElement != null && cachedElement.getFile().equals(file)) {
            retVal = cachedElement;
        } else {
            retVal = (newValue == null) ? NamingFactory.createFileNaming(file, theKey[0], parentName, type) : newValue;
            final WeakReference refRetVal = new WeakReference(retVal);
            assert theKey[0] == retVal.getId();

            final boolean isList = (value instanceof List);
            if (cachedElement != null || isList) {
                // List impl.
                if (isList) { // more than one preexisting entry, just add another one
                    ((List) value).add(refRetVal);
                } else { // just one entry, convert from entry to list of entries
                    final List l = new ArrayList();
                    l.add(ref); // add the original one
                    l.add(refRetVal); // add the new one
                    NamingFactory.nameMap.put(theKey[0], l); // replace the direct entry with the list
                }
            } else {
                // Reference impl.
                Reference r = (Reference)NamingFactory.nameMap.put(theKey[0], refRetVal);
                if (r != null && !retVal.equals(r.get())) {
                    final List l = new ArrayList();
                    l.add(r);
                    l.add(refRetVal);
                    NamingFactory.nameMap.put(theKey[0], l);
                }
            }
        }

        assert retVal != null;

        return retVal;
    }
    
    private static Reference getReference(Object value, File f, Integer[] theKey) {
        List list;
        boolean modify;
        if (value instanceof List) {
            list = (List)value;
            modify = true;
        } else if (value instanceof Reference) {
            list = Collections.nCopies(1, value);
            modify = false;
        } else {
            list = Collections.emptyList();
            modify = false;
        }
            
        boolean initial = true;
        for (Iterator it = list.iterator(); it.hasNext();) {
            Reference ref = (Reference) it.next();
            if (ref == null) {
                if (modify) {
                    it.remove();
                }
                continue;
            }
            FileNaming cachedElement = (FileNaming)ref.get();
            if (cachedElement == null) {
                if (modify) {
                    it.remove();
                }
                continue;
            }
            assert initial || theKey[0] == cachedElement.getId() :
               "Integer keys shall be shared";
            theKey[0] = cachedElement.getId();
            initial = false;
            
            if (cachedElement.getFile().equals(f)) {
                return ref;
            }
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
        
        Object value = nameMap.get(id);
        if (value instanceof Reference) {
            sb.append("One reference to ").
               append(hex).append("\n");
            dumpFileNaming(sb, ((Reference)value).get());
        } else if (value instanceof List) {
            int cnt = 0;
            List arr = (List)value;
            sb.append("There is ").append(arr.size()).append(" references to ").append(hex);
            for (Object o : arr) {
                sb.append(++cnt).append(" = ");
                dumpFileNaming(sb, ((Reference)o).get());
            }
        } else {
            sb.append("For ").append(hex).append(" there is just ").append(value);
        }
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
}
