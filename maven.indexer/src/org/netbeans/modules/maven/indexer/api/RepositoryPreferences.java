/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s): theanuradha@netbeans.org
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.indexer.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author Anuradha G
 */
public final class RepositoryPreferences {

    private static RepositoryPreferences instance;
    /**
     * index of local repository
     */
    public static final String LOCAL_REPO_ID = "local";//NOI18N
    
    //TODO - move elsewhere, implementation detail??
    public static final String TYPE_NEXUS = "nexus"; //NOI18N
    

    static final String KEY_TYPE = "provider";//NOI18N
    static final String KEY_PATH = "path";//NOI18N
    static final String KEY_INDEX_URL = "repoIndexUrl";//NOI18N
    static final String KEY_REPO_URL = "repoUrl";//NOI18N
    /*index settings */
    public static final String PROP_INDEX_FREQ = "indexUpdateFrequency"; //NOI18N
    public static final String PROP_LAST_INDEX_UPDATE = "lastIndexUpdate"; //NOI18N
    public static final String PROP_SNAPSHOTS = "includeSnapshots"; //NOI18N
    public static final int FREQ_ONCE_WEEK = 0;
    public static final int FREQ_ONCE_DAY = 1;
    public static final int FREQ_STARTUP = 2;
    public static final int FREQ_NEVER = 3;
    private final Map<FileObject, RepositoryInfo> infoCache = new TreeMap<FileObject, RepositoryInfo>(new Comp());
    private static final String REPO_FOLDER = "Projects/org-netbeans-modules-maven/Repositories";

    //---------------------------------------------------------------------------
    private RepositoryPreferences() {
    }


    private Preferences getPreferences() {
        return NbPreferences.root().node("org/netbeans/modules/maven/nexus/indexing"); //NOI18N
    }

    //#138102
    private FileObject getRepoFolder() {
        FileObject repo = FileUtil.getConfigFile(REPO_FOLDER);
        if (repo == null) {
            Logger.getLogger(RepositoryPreferences.class.getName()).warning(
                    "Maven Repository root folder " + REPO_FOLDER + //NOI18N
                    " was deleted somehow, creating dummy (empty) one."); //NOI18N
            try {
                repo = FileUtil.createFolder(FileUtil.getConfigRoot(), REPO_FOLDER);
            } catch (IOException ex) {
                // what to do? config file system probably totally broken here...
                Exceptions.printStackTrace(ex);
            }
        }
        return repo;
    }

    public synchronized static RepositoryPreferences getInstance() {
        if (instance == null) {
            instance = new RepositoryPreferences();
        }
        return instance;
    }

    public RepositoryInfo getRepositoryInfoById(String id) {
        for (RepositoryInfo ri : getRepositoryInfos()) {
            if (ri.getId().equals(id)) {
                return ri;
            }
        }
        return null;
    }

    public List<RepositoryInfo> getRepositoryInfos() {
        final FileObject repoFolder = getRepoFolder();
        List<RepositoryInfo> toRet = new ArrayList<RepositoryInfo>();
        if (repoFolder != null) {
            synchronized (infoCache) {
                for (FileObject fo : repoFolder.getChildren()) {
                    if (!infoCache.containsKey(fo)) {
                        RepositoryInfo ri = RepositoryInfo.createRepositoryInfo(fo);
                        infoCache.put(fo, ri);
                    }
                }
                HashSet<FileObject> gone = new HashSet<FileObject>(infoCache.keySet());
                gone.removeAll(Arrays.asList(repoFolder.getChildren()));
                for (FileObject g : gone) {
                    infoCache.remove(g);
                }
                toRet.addAll(infoCache.values());
            }
        }
        return toRet;
    }

    /**
     * 
     * @param info
     */
    public synchronized void addOrModifyRepositoryInfo(RepositoryInfo info) {
        try {
            FileObject fo = getRepoFolder().getFileObject(getFileObjectName(info.getId()));
            if (fo == null) {
                int position = calculatePosition();
                fo = getRepoFolder().createData(getFileObjectName(info.getId()));
                fo.setAttribute("position", position); //NOI18N
            }
            fo.setAttribute(KEY_TYPE, info.getType());
            if (info.getRepositoryPath() != null) {
                fo.setAttribute(KEY_PATH, info.getRepositoryPath());
            }
            if (info.getRepositoryUrl() != null) {
                fo.setAttribute(KEY_REPO_URL, info.getRepositoryUrl());
                fo.setAttribute(KEY_INDEX_URL, info.getIndexUpdateUrl());
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private int calculatePosition() {
        int customStart = 5000;
        for (FileObject fo : getRepoFolder().getChildren()) {
            Integer attr = (Integer) fo.getAttribute("position"); //NOI18N
            if (attr != null && attr.intValue() > customStart) {
                customStart = attr.intValue();
            }
        }
        return customStart + 1;
    }


    private String getFileObjectName(String id) {
        String toRet = id;
        if (toRet.contains(".")) { //NOI18N
            toRet = toRet + ".ext"; //NOI18N
        }
        return toRet;
    }
    
    public void removeRepositoryInfo(RepositoryInfo info) {
        FileObject fo = getRepoFolder().getFileObject(getFileObjectName(info.getId()));
        if (fo != null) {
            synchronized (infoCache) {
                infoCache.remove(fo);
            }
            try {
                fo.delete();
            } catch (IOException x) {
                Logger.getLogger(RepositoryPreferences.class.getName()).log(Level.FINE, "Cannot delete repository in system filesystem", x); //NOI18N
            }
        }
    }

    public void setIndexUpdateFrequency(int fr) {
        getPreferences().putInt(PROP_INDEX_FREQ, fr);
    }

    public int getIndexUpdateFrequency() {
        return getPreferences().getInt(PROP_INDEX_FREQ, FREQ_ONCE_WEEK);
    }

    public Date getLastIndexUpdate(String repoId) {
        return new Date(getPreferences().getLong(PROP_LAST_INDEX_UPDATE + "."+repoId, 0));
    }

    public void setLastIndexUpdate(String repoId,Date date) {
        getPreferences().putLong(PROP_LAST_INDEX_UPDATE + "." + repoId, date.getTime());
    }

    public boolean isIncludeSnapshots() {
        return getPreferences().getBoolean(PROP_SNAPSHOTS, true);
    }

    public void setIncludeSnapshots(boolean includeSnapshots) {
        getPreferences().putBoolean(PROP_SNAPSHOTS, includeSnapshots);
    }


    private static class Comp implements Comparator<FileObject> {

        public int compare(FileObject o1, FileObject o2) {
            if (!o1.isValid() && !o2.isValid()) {
                return 0;
            }
            if (!o1.isValid()) {
                return 1;
            }
            if (!o2.isValid()) {
                return -1;
            }
            Integer pos1 = (Integer)o1.getAttribute("position");
            if (pos1 == null) {
                Logger.getLogger(RepositoryPreferences.class.getName()).warning("FileObject " + o1.getPath() + " doesn't have attribute 'position' defined.");
                pos1 = o1.hashCode();
            }
            Integer pos2 = (Integer)o2.getAttribute("position");
            if (pos2 == null) {
                Logger.getLogger(RepositoryPreferences.class.getName()).warning("FileObject " + o1.getPath() + " doesn't have attribute 'position' defined.");
                pos2 = o1.hashCode();
            }
            if (pos2 > pos1) {
                return -1;
            }
            if (pos2 < pos1) {
                return 1;
            }
            return 0;
        }

    }

}
