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

package org.netbeans.modules.masterfs.filebasedfs.naming;

import org.netbeans.modules.masterfs.filebasedfs.utils.FileInfo;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

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

    public static int getSize () {
        return nameMap.size();
    }
    
    public static synchronized FileNaming fromFile(final FileNaming parentFn, final File file) {            
        return NamingFactory.registerInstanceOfFileNaming(parentFn, file);
    }
    
    public static synchronized boolean rename (FileNaming fNaming, String newName) {        
        boolean retVal = false;
        remove(fNaming, null);
        retVal = fNaming.rename(newName);
        NamingFactory.registerInstanceOfFileNaming(fNaming.getParent(), fNaming.getFile(), fNaming);
        renameChildren();
        return retVal;
    }

    private static void renameChildren() {
        HashMap toRename = new HashMap ();
        for (Iterator iterator = nameMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Integer id = (Integer)entry.getKey();
            //TODO: handle possible List            
            FileNaming fN = (FileNaming)((Reference)entry.getValue()).get(); 
            
            Integer computedId = NamingFactory.createID(fN.getFile()); 
                    

            boolean isRenamed = (!computedId.equals (id));
            if (isRenamed) {
                toRename.put(id, fN);
            }

        }
        
        for (Iterator iterator = toRename.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Integer id = (Integer)entry.getKey();
            FileNaming fN = (FileNaming)entry.getValue(); 

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
            retVal = new FileName(parentName, f);

        }

        assert retVal != null /*|| !fInfo.isConvertibleToFileObject()*/ : f.getAbsolutePath() + " isDirectory: " + f.isDirectory() + " isFile: " + f.isFile() + " exists: " + f.exists();//NOI18N
        return retVal;
    }

}
