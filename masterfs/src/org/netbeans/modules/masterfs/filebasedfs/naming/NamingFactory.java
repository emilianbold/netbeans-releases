/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.masterfs.filebasedfs.naming;

import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;

import java.io.File;
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
        final LinkedList list = new LinkedList();
        File current = file;
        while (current != null) {
            list.addFirst(current);
            current = current.getParentFile();
        }

        FileNaming fileName = null;
        for (int i = 0; i < list.size(); i++) {
            fileName = NamingFactory.registerInstanceOfFileNaming(fileName, (File) list.get(i));
        }

        return fileName;
    }

    public static synchronized int getSize () {
        return nameMap.size();
    }
    
    public static synchronized FileNaming fromFile(final FileNaming parentFn, final File file) {            
        return NamingFactory.registerInstanceOfFileNaming(parentFn, file);
    }
    
    public static synchronized void checkCaseSensitivity(final FileNaming childName, final File f) {
        if (!childName.getFile().getName().equals(f.getName())) {
            boolean isCaseSensitive = !new File(f,"a").equals(new File(f,"A"));//NOI18N
            if (!isCaseSensitive) {
                    NamingFactory.rename(childName,f.getName());
            }
        }                        
    }

    private static synchronized FileNaming[] rename (FileNaming fNaming, String newName) {        
        return rename(fNaming, newName, null);
    }
    
    public static synchronized FileNaming[] rename (FileNaming fNaming, String newName, ProvidedExtensions.IOHandler handler) {
        final ArrayList all = new ArrayList();
        boolean retVal = false;
        remove(fNaming, null);
        retVal = fNaming.rename(newName, handler);
        all.add(fNaming);
        NamingFactory.registerInstanceOfFileNaming(fNaming.getParent(), fNaming.getFile(), fNaming);
        renameChildren(all);
        return (retVal) ? ((FileNaming[])all.toArray(new FileNaming[all.size()])) : null;
    }

    private static void renameChildren(final ArrayList all) {
        HashMap toRename = new HashMap ();
        for (Iterator iterator = nameMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Integer id = (Integer)entry.getKey();
            
            List list = new ArrayList();
            
            //handle possible List
            Object value = entry.getValue();
            if (value instanceof Reference) {
                list.add(value);
            } else if (value instanceof List) {
                list.addAll((List) value);
            }
            
            for (int i = 0; i < list.size(); i++) {
                FileNaming fN = (FileNaming)((Reference) list.get(i)).get();
                if (fN == null) continue;
                Integer computedId = NamingFactory.createID(fN.getFile());
                
                boolean isRenamed = (!computedId.equals(id));
                if (isRenamed) {
                    toRename.put(id, fN);
                }        
            }
        }
        
        for (Iterator iterator = toRename.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Integer id = (Integer)entry.getKey();
            FileNaming fN = (FileNaming)entry.getValue(); 
            all.add(fN);    
            remove(fN, id);
            fN.getId(true);
            NamingFactory.registerInstanceOfFileNaming(fN.getParent(), fN.getFile(), fN);            
        }
    }

    private static void remove(final FileNaming fNaming, Integer id) {
        id = (id != null) ? id : fNaming.getId();         
        Object value = NamingFactory.nameMap.get(id);
        if (value instanceof List) {
            Reference ref = NamingFactory.getReference((List) value, fNaming.getFile());
            if (ref != null) {
                ((List) value).remove(ref);                
            }            
        } else {
            NamingFactory.nameMap.remove(id);
        }
    }

    public static Integer createID(final File file) {
        return new Integer(file.hashCode());
    }
    private static FileNaming registerInstanceOfFileNaming(final FileNaming parentName, final File file) {
        return NamingFactory.registerInstanceOfFileNaming(parentName, file, null);       
    }

    private static FileNaming registerInstanceOfFileNaming(final FileNaming parentName, final File file, final FileNaming newValue) {
        FileNaming retVal;
        
        final Object value = NamingFactory.nameMap.get(new Integer(file.hashCode()));
        Reference ref = (Reference) (value instanceof Reference ? value : null);
        ref = (ref == null && value instanceof List ? NamingFactory.getReference((List) value, file) : ref);

        final FileNaming cachedElement = (ref != null) ? (FileNaming) ref.get() : null;

        if (cachedElement != null && cachedElement.getFile().compareTo(file) == 0) {
            retVal = cachedElement;
        } else {
            retVal = (newValue == null) ? NamingFactory.createFileNaming(file, parentName) : newValue;
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
                assert r == null || r.get() == null;
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

    private static FileNaming createFileNaming(final File f, final FileNaming parentName) {
        FileName retVal = null;
        //TODO: check all tests for isFile & isDirectory
        final FileInfo fInfo = new FileInfo(f);

        if (f.isFile()) {
            retVal = new FileName(parentName, f);
        } else {
            if (f.isDirectory()) {
                retVal = new FolderName(parentName, f);
            } else {
                if (fInfo.isUNCFolder()) {
                    retVal = new UNCName(parentName, f);
                }
            }
        }

        if (retVal == null /*&& new FileInfo(f).isUnixSpecialFile()*/) {
            // broken symlinks and other for me unknown files (sockets or whatever it is)
            retVal = new FileName(parentName, f) {
                public boolean isDirectory() {
                    return false;
                }

                public boolean isFile() {
                    return false;
                }                
            };

        }

        assert retVal != null /*|| !fInfo.isConvertibleToFileObject()*/ : f.getAbsolutePath() + " isDirectory: " + f.isDirectory() + " isFile: " + f.isFile() + " exists: " + f.exists();//NOI18N
        return retVal;
    }

}
