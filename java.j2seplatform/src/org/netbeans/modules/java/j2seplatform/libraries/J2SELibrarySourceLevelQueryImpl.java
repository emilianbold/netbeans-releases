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

package org.netbeans.modules.java.j2seplatform.libraries;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author  tom
 */
public class J2SELibrarySourceLevelQueryImpl implements SourceLevelQueryImplementation {
    
    private static final String JDK_12 = "1.2";     //NOI18N
    private static final String JDK_13 = "1.3";     //NOI18N
    private static final String JDK_14 = "1.4";     //NOI18N
    private static final String JDK_15 = "1.5";     //NOI18N
    private static final String JDK_UNKNOWN = "";   //NOI18N
    private static final String CLASS = "class";    //NOI18N
    private static final int CF_MAGIC = 0xCAFEBABE;
    private static final int CF_INVALID = -1;
    private static final int CF_11 = 0x2d;
    private static final int CF_12 = 0x2e;
    private static final int CF_13 = 0x2f;
    private static final int CF_14 = 0x30;
    private static final int CF_15 = 0x31;
    
    //Cache for source level
    private Map/*<Library, sourceLevel>*/ sourceLevelCache = new WeakHashMap ();
    
    //Cache for last used library, helps since queries are sequential
    private /*Soft*/Reference lastUsedRoot;
    private /*Weak*/Reference lastUsedLibrary;
    
    /** Creates a new instance of J2SELibrarySourceLevelQueryImpl */
    public J2SELibrarySourceLevelQueryImpl() {
    }
    
    public String getSourceLevel(org.openide.filesystems.FileObject javaFile) {        
        Library ll = this.isLastUsed (javaFile);
        if (ll != null) {
            return getSourceLevel (ll);
        }
        Library[] libraries = LibraryManager.getDefault().getLibraries();
        for (int i=0; i< libraries.length; i++) {
            if (!J2SELibraryTypeProvider.LIBRARY_TYPE.equalsIgnoreCase(libraries[i].getType())) { 
                continue;
            }
            List sourceRoots = libraries[i].getContent(J2SELibraryTypeProvider.VOLUME_TYPE_SRC);   //NOI18N
            if (sourceRoots.size() == 0) {
                continue;
            }            
            ClassPath cp = ClassPathSupport.createClassPath((URL[])sourceRoots.toArray(new URL[sourceRoots.size()]));
            FileObject root;
            if ((root = cp.findOwnerRoot(javaFile)) != null) {
                setLastUsedRoot (root, libraries[i]);
                return getSourceLevel(libraries[i]);
            }
        }
        return null;
    }    
    
    private String getSourceLevel (Library lib) {
        String slevel = (String)this.sourceLevelCache.get (lib);
        if (slevel == null) {
            slevel = getSourceLevel (lib.getContent(J2SELibraryTypeProvider.VOLUME_TYPE_CLASSPATH));
            this.sourceLevelCache.put (lib,slevel);
        }
        return slevel == JDK_UNKNOWN ? null : slevel;                
    }
    
    private String getSourceLevel (List cpRoots) {
        FileObject classFile = getClassFile (cpRoots);
        if (classFile == null) {
            return JDK_UNKNOWN;
        }
        int version = getClassFileMajorVersion (classFile);
        if (version == CF_11 || version == CF_12) {
            return JDK_12;
        }
        else if (version == CF_13) {
            return JDK_13;
        }
        else if (version == CF_14) {
            return JDK_14;
        }
        else if (version >= CF_15) {
            return JDK_15;
        }        
        return JDK_UNKNOWN;
    }
    
    private FileObject getClassFile (List cpRoots) {
        for (Iterator it = cpRoots.iterator(); it.hasNext();) {
            FileObject root = URLMapper.findFileObject((URL)it.next());
            if (root == null) {
                continue;
            }
            FileObject cf = findClassFile (root);
            if (cf != null) {
                return cf;
            }
        }
        return null;
    }
    
    private FileObject findClassFile (FileObject root) {
        if (root.isData()) {
            if (CLASS.equals(root.getExt())) {
                return root;
            }
            else {
                return null;
            }
        }
        else {
            FileObject[] children = root.getChildren();
            for (int i=0; i<children.length; i++) {
                FileObject result = findClassFile(children[i]);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }
    }
    
    private int getClassFileMajorVersion (FileObject classFile) {
        DataInputStream in = null;
        try {
            in = new DataInputStream (classFile.getInputStream());
            int magic = in.readInt();   
            if (CF_MAGIC != magic) {
                return CF_INVALID;
            }
            short minor = in.readShort(); //Ignore it
            short major = in.readShort();
            return major;
        } catch (IOException e) {
            return CF_INVALID;
        } finally {
            if (in != null) {
                try {
                    in.close ();
                } catch (IOException e) {
                    //Ignore it, can not recover
                }
            }
        }
    }
    
    private synchronized void setLastUsedRoot (FileObject root, Library lib) {
        this.lastUsedRoot = new SoftReference (root);
        this.lastUsedLibrary = new WeakReference (lib);
    }
    
    private synchronized Library isLastUsed (FileObject javaFile) {
        if (lastUsedRoot == null) {
            return null;
        }
        
        FileObject root = (FileObject) lastUsedRoot.get ();
        if (root == null) {
            return null;
        }
        
        if (root.equals(javaFile) || FileUtil.isParentOf(root,javaFile)) {
            return (Library) lastUsedLibrary.get ();
        }
        return null;
    }
    
}
