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
    
    public static synchronized void checkCaseSensitivity(final FileNaming childName, final File f) throws IOException {
        if (!childName.getFile().getName().equals(f.getName())) {
            boolean isCaseSensitive = !new File(f,"a").equals(new File(f,"A"));//NOI18N
            if (!isCaseSensitive) {
                    NamingFactory.rename(childName,f.getName());
            }
        }                        
    }

    private static synchronized FileNaming[] rename (FileNaming fNaming, String newName) throws IOException {
        return rename(fNaming, newName, null);
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

    public static Integer createID(final File file) {
        return new Integer(file.hashCode());
    }
    private static FileNaming registerInstanceOfFileNaming(final FileNaming parentName, final File file, FileType type) {
        return NamingFactory.registerInstanceOfFileNaming(parentName, file, null,false, type);       
    }

    private static FileNaming registerInstanceOfFileNaming(final FileNaming parentName, final File file, final FileNaming newValue,boolean ignoreCache, FileType type) {
        FileNaming retVal;

        assert Thread.holdsLock(NamingFactory.class);
        final Object value = nameMap.get(new Integer(file.hashCode()));
        Reference ref = (Reference) (value instanceof Reference ? value : null);
        ref = (ref == null && value instanceof List ? NamingFactory.getReference((List) value, file) : ref);

        FileNaming cachedElement = (ref != null) ? (FileNaming) ref.get() : null;
        if (ignoreCache && cachedElement != null && cachedElement.isDirectory() != file.isDirectory()) {
            cachedElement = null;
        }

        if (cachedElement != null && cachedElement.getFile().compareTo(file) == 0) {
            retVal = cachedElement;
        } else {
            retVal = (newValue == null) ? NamingFactory.createFileNaming(file, parentName, type) : newValue;
            final WeakReference refRetVal = new WeakReference(retVal);

            final boolean isList = (value instanceof List);
            if (cachedElement != null || isList) {
                // List impl.
                if (isList) {
                    ((List) value).add(refRetVal);
                } else {
                    final List l = new ArrayList();
                    l.add(refRetVal);
                    NamingFactory.nameMap.put(retVal.getId(), l);
                }
            } else {
                // Reference impl.
                Reference r = (Reference)NamingFactory.nameMap.put(retVal.getId(), refRetVal);
            }
        }

        assert retVal != null;

        return retVal;
    }
    
    private static Reference getReference(final List list, final File f) {
        Reference retVal = null;
        for (int i = 0; retVal == null && i < list.size(); i++) {
            final Reference ref = (Reference) list.get(i);
            final FileNaming cachedElement = (ref != null) ? (FileNaming) ref.get() : null;
            if (cachedElement != null && cachedElement.getFile().compareTo(f) == 0) {
                retVal = ref;
            }
        }
        return retVal;
    }

    static enum FileType {file, directory, unknown}
    
    private static FileNaming createFileNaming(final File f, final FileNaming parentName, FileType type) {
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
                retVal = new FileName(parentName, f);
                break;
            case directory:
                retVal = new FolderName(parentName, f);
                break;
        }
        return retVal;
    }
}
