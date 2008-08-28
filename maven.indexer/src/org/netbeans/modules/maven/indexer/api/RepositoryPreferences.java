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
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.filesystems.FileObject;
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
    //---------------------------------------------------------------------------
    private RepositoryPreferences() {
    }

    private Preferences getPreferences() {
        return NbPreferences.root().node("org/netbeans/modules/maven/nexus/indexing"); //NOI18N
    }

    //#138102
    private FileObject getSystemFsRoot() {
        return org.openide.filesystems.Repository.getDefault().getDefaultFileSystem().findResource("Projects/org-netbeans-modules-maven/Repositories"); //NOI18N
    }

    public synchronized static RepositoryPreferences getInstance() {
        if (instance == null) {
            instance = new RepositoryPreferences();
        }
        return instance;
    }

    public RepositoryInfo getRepositoryInfoById(String id) {
        assert getSystemFsRoot() != null;
        FileObject fo = getSystemFsRoot().getFileObject(id);
        if (fo != null) {
            return RepositoryInfo.createRepositoryInfo(fo);
        }
        for (RepositoryInfo ri : getRepositoryInfos()) {
            if (ri.getId().equals(id)) {
                return ri;
            }
        }
        return null;
    }

    public List<RepositoryInfo> getRepositoryInfos() {
        List<RepositoryInfo> toRet = new ArrayList<RepositoryInfo>();
        for (FileObject fo : getSystemFsRoot().getChildren()) {
            RepositoryInfo ri = RepositoryInfo.createRepositoryInfo(fo);
            toRet.add(ri);
        }
        return toRet;
    }

    /**
     * 
     * @param info
     */
    public synchronized void addOrModifyRepositoryInfo(RepositoryInfo info) {
        try {
            FileObject fo = getSystemFsRoot().getFileObject(info.getId());
            if (fo == null) {
                fo = getSystemFsRoot().createData(info.getId());
            }
            fo.setAttribute(KEY_TYPE, info.getType());
            fo.setAttribute(KEY_PATH, info.getRepositoryPath());
            fo.setAttribute(KEY_REPO_URL, info.getRepositoryUrl());
            fo.setAttribute(KEY_INDEX_URL, info.getIndexUpdateUrl());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    
    public void removeRepositoryInfo(RepositoryInfo info) {
        FileObject fo = getSystemFsRoot().getFileObject(info.getId());
        if (fo != null) {
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

}
