/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.subversion.remote.versioning.util;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.queries.VersioningQuery;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.netbeans.modules.versioning.core.spi.VersioningSystem;
import org.netbeans.modules.versioning.util.VCSKenaiAccessor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;

/**
 *
 * @author Alexander Simon
 */
public class Utils {
    private static final Logger LOG = Logger.getLogger(Utils.class.getName());
    // -----
    // Usages logging based on repository URL (for Kenai)

    private static VCSKenaiAccessor kenaiAccessor;
    private static final LinkedList<VCSFileProxy> loggedRoots = new LinkedList<>();
    private static final List<VCSFileProxy> foldersToCheck = new LinkedList<>();
    private static Runnable loggingTask = null;

    /*
     * Makes sure repository of given versioned folder is logged for usage
     * (if on Kenai). Versioned folders are collected and a task invoked in 2s
     * to process them. Roots are remembered so no subfolder is processed again
     * (it's enough to log one usage per repository). Called from annotators so
     * all user visible repositories are logged.
     */
    public static void addFolderToLog(VCSFileProxy folder) {
        if (!checkFolderLogged(folder, false)) {
            synchronized(foldersToCheck) {
                foldersToCheck.add(folder);
                if (loggingTask == null) {
                    loggingTask = new LogTask();
                    org.netbeans.modules.versioning.util.Utils.postParallel(loggingTask, 2000);
                }
            }
        }
    }
    
    private static boolean checkFolderLogged(VCSFileProxy folder, boolean add) {
        synchronized(loggedRoots) {
            for (VCSFileProxy f : loggedRoots) {
                String ancestorPath = f.getPath();
                String folderPath = folder.getPath();
                if (folderPath.startsWith(ancestorPath)
                        && (folderPath.length() == ancestorPath.length()
                             || folderPath.charAt(ancestorPath.length()) == '/')) {
                    // folder is the same or subfolder of already logged one
                    return true;
                }
            }
            if (add) {
                loggedRoots.add(folder);
            }
        }
        return false;
    }

    private static class LogTask implements Runnable {
        @Override
        public void run() {
            VCSFileProxy[] folders;
            synchronized (foldersToCheck) {
                folders = foldersToCheck.toArray(new VCSFileProxy[foldersToCheck.size()]);
                foldersToCheck.clear();
                loggingTask = null;
            }
            for (VCSFileProxy f : folders) {
                if (!checkFolderLogged(f, false)) { // if other task has not processed the root yet
                    VersioningSystem vs = VersioningSupport.getOwner(f);
                    if (vs != null) {
                        VCSFileProxy root = vs.getTopmostManagedAncestor(f);
                        if (root != null) {
                            checkFolderLogged(root, true); // remember the root
                            FileObject rootFO = root.toFileObject();
                            if (rootFO != null) {
                                String url = VersioningQuery.getRemoteLocation(rootFO.toURI());
                                if (url != null) {
                                    logVCSKenaiUsage("Remote Subversion", url);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static void logVCSKenaiUsage(String vcs, String repositoryUrl) {
        VCSKenaiAccessor kenaiSup = getKenaiAccessor();
        if (kenaiSup != null) {
            kenaiSup.logVcsUsage(vcs, repositoryUrl);
        }
    }

    private static VCSKenaiAccessor getKenaiAccessor() {
        if (kenaiAccessor == null) {
            kenaiAccessor = Lookup.getDefault().lookup(VCSKenaiAccessor.class);
        }
        return kenaiAccessor;
    }
    
        /**
     * Splits files/folders into 2 groups: flat folders and other files
     *
     * @param files array of files to split
     * @return File[][] the first array File[0] contains flat folders (
     * @see #flatten for their direct descendants), File[1] contains all other
     * files
     */
    public static VCSFileProxy[][] splitFlatOthers(VCSFileProxy[] files) {
        Set<VCSFileProxy> flat = new HashSet<>(1);
        for (int i = 0; i < files.length; i++) {
            if (VersioningSupport.isFlat(files[i])) {
                flat.add(files[i]);
            }
        }
        if (flat.isEmpty()) {
            return new VCSFileProxy[][]{new VCSFileProxy[0], files};
        } else {
            Set<VCSFileProxy> allFiles = new HashSet<>(Arrays.asList(files));
            allFiles.removeAll(flat);
            return new VCSFileProxy[][]{
                        flat.toArray(new VCSFileProxy[flat.size()]),
                        allFiles.toArray(new VCSFileProxy[allFiles.size()])
                    };
        }
    }

    /**
     * Tests whether all files belong to the same data object.
     *
     * @param files array of Files
     * @return true if all files share common DataObject (even null), false
     * otherwise
     */
    public static boolean shareCommonDataObject(VCSFileProxy[] files) {
        if (files == null || files.length < 2) {
            return true;
        }
        DataObject common = findDataObject(files[0]);
        for (int i = 1; i < files.length; i++) {
            DataObject dao = findDataObject(files[i]);
            if (dao != common && (dao == null || !dao.equals(common))) {
                return false;
            }
        }
        return true;
    }

    private static DataObject findDataObject(VCSFileProxy file) {
        FileObject fo = file.toFileObject();
        if (fo != null) {
            try {
                return DataObject.find(fo);
            } catch (DataObjectNotFoundException e) {
                // ignore
            }
        }
        return null;
    }
    
    /**
     * Opens a file in the editor area.
     *
     * @param file a File to open
     */
    public static void openFile(VCSFileProxy file) {
        FileObject fo = file.toFileObject();
        if (fo != null) {
            try {
                DataObject dao = DataObject.find(fo);
                OpenCookie oc = dao.getLookup().lookup(OpenCookie.class);
                if (oc != null) {
                    oc.open();
                }
            } catch (DataObjectNotFoundException e) {
                // nonexistent DO, do nothing
            }
        }
    }
    
    /**
     * Checks if the file is to be considered as textuall.
     *
     * @param file file to check
     * @return true if the file can be edited in NetBeans text editor, false otherwise
     */
    public static boolean isFileContentText(VCSFileProxy file) {
        FileObject fo = file.toFileObject();
        if (fo == null) {
            return false;
        }
        if (fo.getMIMEType().startsWith("text")) { // NOI18N
            return true;
        }
        try {
            DataObject dao = DataObject.find(fo);
            return dao.getLookup().lookupItem(new Lookup.Template<>(EditorCookie.class)) != null;
        } catch (DataObjectNotFoundException e) {
            // not found, continue
        }
        return false;
    }
    
    /**
     * Searches for common filesystem parent folder for given files.
     *
     * @param a first file
     * @param b second file
     * @return File common parent for both input files with the longest
     * filesystem path or null of these files have not a common parent
     */
    public static VCSFileProxy getCommonParent(VCSFileProxy a, VCSFileProxy b) {
        for (;;) {
            if (a.equals(b)) {
                return a;
            } else if (a.getPath().length() > b.getPath().length()) {
                a = a.getParentFile();
                if (a == null) {
                    return null;
                }
            } else {
                b = b.getParentFile();
                if (b == null) {
                    return null;
                }
            }
        }
    }
    
    private static Map<VCSFileProxy, Charset> fileToCharset;

    /**
     * Retrieves the Charset for the referenceFile and associates it weakly with
     * the given file. A following getAssociatedEncoding() call for the file
     * will then return the referenceFile-s Charset.
     *
     * @param referenceFile the file which charset has to be used when encoding
     * file
     * @param file file to be encoded with the referenceFile-s charset
     *
     */
    public static void associateEncoding(VCSFileProxy referenceFile, VCSFileProxy file) {
        FileObject fo = referenceFile.toFileObject();
        if (fo == null || fo.isFolder()) {
            return;
        }
        Charset c = FileEncodingQuery.getEncoding(fo);
        if (c == null) {
            return;
        }
        if (fileToCharset == null) {
            fileToCharset = new WeakHashMap<>();
        }
        synchronized (fileToCharset) {
            fileToCharset.put(file, c);
        }
    }

    /**
     * Returns a charset for the given file if it was previously registered via
     * associateEncoding()
     *
     * @param fo file for which the encoding has to be retrieved
     * @return the charset the given file has to be encoded with
     */
    public static Charset getAssociatedEncoding(FileObject fo) {
        try {
            if (fileToCharset == null || fileToCharset.isEmpty() || fo == null || fo.isFolder()) {
                return null;
            }
            VCSFileProxy file = VCSFileProxy.createFileProxy(fo);
            if (file == null) {
                return null;
            }
            synchronized (fileToCharset) {
                return fileToCharset.get(file);
            }
        } catch (Throwable t) {
            LOG.log(Level.INFO, null, t);

            return null;
        }
    }

    /**
     * @param file
     * @return Set<File> all files that belong to the same DataObject as the
     * argument
     */
    public static Set<VCSFileProxy> getAllDataObjectFiles(VCSFileProxy file) {
        Set<VCSFileProxy> filesToCheckout = new HashSet<>(2);
        filesToCheckout.add(file);
        FileObject fo = file.toFileObject();
        if (fo != null) {
            try {
                DataObject dao = DataObject.find(fo);
                Set<FileObject> fileObjects = dao.files();
                for (FileObject fileObject : fileObjects) {
                    filesToCheckout.add(VCSFileProxy.createFileProxy(fileObject));
                }
            } catch (DataObjectNotFoundException e) {
                // no dataobject, never mind
            }
        }
        return filesToCheckout;
    }
    
    /**
     * Helper method to get an array of Strings from preferences.
     *
     * @param prefs storage
     * @param key key of the String array
     * @return List<String> stored List of String or an empty List if the key was not found (order is preserved)
     */
    public static List<String> getStringList (Preferences prefs, String key) {
        List<String> retval = new ArrayList<String>();
        try {
            String[] keys = prefs.keys();
            for (int i = 0; i < keys.length; i++) {
                String k = keys[i];
                if (k != null && k.startsWith(key)) {
                    int idx = Integer.parseInt(k.substring(k.lastIndexOf('.') + 1)); //NOI18N
                    retval.add(idx + "." + prefs.get(k, null)); //NOI18N
                }
            }
            List<String> rv = new ArrayList<String>(retval.size());
            rv.addAll(retval);
            for (String s : retval) {
                int pos = s.indexOf('.');
                int index = Integer.parseInt(s.substring(0, pos));
                rv.set(index, s.substring(pos + 1));
            }
            return rv;
        } catch (Exception ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.INFO, null, ex);
            return new ArrayList<String>(0);
        }
    }

    
    /**
     * Stores a List of Strings into Preferences node under the given key.
     *
     * @param prefs storage
     * @param key key of the String array
     * @param value List of Strings to write (order will be preserved)
     */
    public static void put (Preferences prefs, String key, List<String> value) {
        try {
            String[] keys = prefs.keys();
            for (int i = 0; i < keys.length; i++) {
                String k = keys[i];
                if (k != null && k.startsWith(key + ".")) { //NOI18N
                    prefs.remove(k);
                }
            }
            int idx = 0;
            for (String s : value) {
                prefs.put(key + "." + idx++, s); //NOI18N
            }
        } catch (BackingStoreException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.INFO, null, ex);
        }
    }
    
    public static boolean isAncestorOrEqual(VCSFileProxy ancestor, VCSFileProxy file) {
        String ancestorPath = ancestor.getPath();
        String filePath = file.getPath();
        if (VCSFileProxySupport.isMac(ancestor)) {
            // Mac is not case sensitive, cannot use the else statement
            if(filePath.length() < ancestorPath.length()) {
                return false;
            }
        } else {
            if(!filePath.startsWith(ancestorPath)) {
                return false;
            }
        }

        // get sure as it still could be something like:
        // ancestor: /home/dil
        // file:     /home/dil1/dil2
        for (; file != null; file = file.getParentFile()) {
            if(ancestor == null) {
                // XXX have to rely on path because of fileproxy being created from 
                // io.file even if it was originaly stored from a remote
                if (file.getPath().equals(ancestorPath)) {
                    return true;
                } 
            } else {
                if (file.equals(ancestor)) {
                    return true;
                } 
            }
        }
        return false;
    }

}
