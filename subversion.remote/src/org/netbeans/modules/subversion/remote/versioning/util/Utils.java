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

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.queries.VersioningQuery;
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
    // -----
    // Usages logging based on repository URL (for Kenai)

    private static VCSKenaiAccessor kenaiAccessor;
    private static final LinkedList<VCSFileProxy> loggedRoots = new LinkedList<VCSFileProxy>();
    private static final List<VCSFileProxy> foldersToCheck = new LinkedList<VCSFileProxy>();
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
        Set<VCSFileProxy> flat = new HashSet<VCSFileProxy>(1);
        for (int i = 0; i < files.length; i++) {
            if (VersioningSupport.isFlat(files[i])) {
                flat.add(files[i]);
            }
        }
        if (flat.isEmpty()) {
            return new VCSFileProxy[][]{new VCSFileProxy[0], files};
        } else {
            Set<VCSFileProxy> allFiles = new HashSet<VCSFileProxy>(Arrays.asList(files));
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
            return dao.getLookup().lookupItem(new Lookup.Template<EditorCookie>(EditorCookie.class)) != null;
        } catch (DataObjectNotFoundException e) {
            // not found, continue
        }
        return false;
    }

}
