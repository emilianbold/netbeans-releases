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

package org.netbeans.modules.j2ee.ejbjarproject.ui.wizards;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Enumerations;

final class FileSearchUtility {
    
    /** Creates a new instance of FileSearchUtility. */
    private FileSearchUtility() {
    }
        
   /** Recursively enumerate all children of this folder to some specified depth.
    *  All direct children are listed; then children of direct subfolders; and so on.
    *
    * @param root the starting directory
    * @param depth the search limit
    * @param onlyWritables only recurse into wriable directories
    * @return enumeration of type <code>FileObject</code>
    */
    static Enumeration getChildrenToDepth(final FileObject root, final int depth, final boolean onlyWritables) {
        class WithChildren implements Enumerations.Processor {
            private int rootDepth;
            public WithChildren(final int rootDepth) {
                this.rootDepth = rootDepth;
            }
            public Object process(final Object obj, final Collection toAdd) {
                FileObject fo = (FileObject)obj;
                if (!onlyWritables || (onlyWritables && fo.canWrite())) {
                    if (fo.isFolder() && (getDepth(fo) - rootDepth) < depth) {
                        toAdd.addAll(Arrays.asList(fo.getChildren()));
                    }
                }
                return fo;
            }
        }

        return Enumerations.queue(
            Enumerations.array(root.getChildren()),
            new WithChildren(getDepth(root))
        );
    }

    static FileObject[] guessJavaRoots(final FileObject dir) {
        List foundRoots = new ArrayList();
        Enumeration ch = FileSearchUtility.getChildrenToDepth(dir, 10, true); // .getChildren(true);
        try {
            // digging through 10 levels exhaustively is WAY TOO EXPENSIVE
            while (ch.hasMoreElements () && foundRoots.isEmpty()) {
                FileObject f = (FileObject) ch.nextElement ();
                if (f.getExt().equals("java") && !f.isFolder()) { //NOI18N
                    String pckg = guessPackageName(f);
                    String pkgPath = f.getParent().getPath(); 
                    if (pckg != null && pkgPath.endsWith(pckg.replace('.', '/'))) {
                        String rootName = pkgPath.substring(0, pkgPath.length() - pckg.length());
                        FileObject fr = f.getFileSystem().findResource(rootName);
                        if (!foundRoots.contains(fr)) {
                            foundRoots.add(fr);
                        }
                    }
                }
            }
        } catch (FileStateInvalidException fsie) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, fsie);
        }
        if (foundRoots.size() == 0) {
            return null;
        } else {
            FileObject[] resultArr = new FileObject[foundRoots.size()];
            for (int i = 0; i < foundRoots.size(); i++) {
                resultArr[i] = (FileObject) foundRoots.get(i);
            }
            return resultArr;
        }
    }
    
    static   FileObject guessConfigFilesPath(final FileObject dir) {
        Enumeration ch = FileSearchUtility.getChildrenToDepth(dir, 3, true); //getChildren(true);
        try {
            while (ch.hasMoreElements()) {
                FileObject f = (FileObject) ch.nextElement();
                if (f.getNameExt().equals("ejb-jar.xml")) { //NOI18N
                    String rootName = f.getParent().getPath();
                    return f.getFileSystem().findResource(rootName);
                }
            }
        } catch (FileStateInvalidException fsie) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, fsie);
        }
        return null;
    }


    static  File[] guessJavaRootsAsFiles(final FileObject dir) {
        FileObject[] rootsFOs = guessJavaRoots(dir);
        if (rootsFOs == null) {
            return new File[0];
        }
        File[] resultArr = new File[rootsFOs.length];
        for (int i = 0; i < resultArr.length; i++) {
            resultArr[i] = FileUtil.toFile(rootsFOs[i]);
        }
        return resultArr;
    }

    private static String guessPackageName(final FileObject f) {
        java.io.Reader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(f.getInputStream(), "utf-8")); // NOI18N
            StringBuffer sb = new StringBuffer();
            final char[] buffer = new char[4096];
            int len;

            for (;;) {
                len = r.read(buffer);
                if (len == -1) { break; }
                sb.append(buffer, 0, len);
            }
            int idx = sb.indexOf("package"); // NOI18N
            if (idx >= 0) {
                int idx2 = sb.indexOf(";", idx);  // NOI18N
                if (idx2 >= 0) {
                    return sb.substring(idx + "package".length(), idx2).trim();
                }
            }
        } catch (java.io.IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
        } finally {
            try { if (r != null) { r.close(); }} catch (java.io.IOException ioe) { ; // ignore this 
            }
        }
        // AB: fix for #56160: assume the class is in the default package
        return ""; // NOI18N
    }

    static FileObject guessLibrariesFolder(final FileObject dir) {
        if (dir != null) {
            FileObject lib = dir.getFileObject("lib"); //NOI18N
            if (lib != null) {
                return lib;
            }
        }
        Enumeration ch = FileSearchUtility.getChildrenToDepth(dir, 3, true);
        while (ch.hasMoreElements()) {
            FileObject f = (FileObject) ch.nextElement();
            if (f.getExt().equals("jar")) { //NOI18N
                return f.getParent();
            }
        }
        return null;
    }

    private static int getDepth(final FileObject fo) {
        String path = FileUtil.toFile(fo).getAbsolutePath();
        StringTokenizer toker = new StringTokenizer(path, File.separator);
        return toker.countTokens();
    }
}
