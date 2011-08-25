/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s): theanuradha@netbeans.org
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.indexer.api;

import java.io.IOException;
import java.io.SyncFailedException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author Anuradha G
 */
public final class RepositoryPreferences {

    private static final Logger LOG = Logger.getLogger(RepositoryPreferences.class.getName());

    private static RepositoryPreferences instance;
    /**
     * index of local repository
     */
    public static final String LOCAL_REPO_ID = "local";//NOI18N

    /** location of Maven Central */
    public static final String REPO_CENTRAL = "http://repo1.maven.org/maven2/"; // NOI18N

    //TODO - move elsewhere, implementation detail??
    public static final String TYPE_NEXUS = "nexus"; //NOI18N
    

    static final String KEY_TYPE = "provider";//NOI18N
    static final String KEY_DISPLAY_NAME = "displayName";//NOI18N
    static final String KEY_PATH = "path";//NOI18N
    static final String KEY_INDEX_URL = "repoIndexUrl";//NOI18N
    static final String KEY_REPO_URL = "repoUrl";//NOI18N
    /*index settings */
    public static final String PROP_INDEX_FREQ = "indexUpdateFrequency"; //NOI18N
    public static final String PROP_LAST_INDEX_UPDATE = "lastIndexUpdate"; //NOI18N
    public static final int FREQ_ONCE_WEEK = 0;
    public static final int FREQ_ONCE_DAY = 1;
    public static final int FREQ_STARTUP = 2;
    public static final int FREQ_NEVER = 3;
    private final Map<FileObject, RepositoryInfo> infoCache = new HashMap<FileObject, RepositoryInfo>();
    private final Map<Object,List<RepositoryInfo>> transients = new LinkedHashMap<Object,List<RepositoryInfo>>();
    private final ChangeSupport cs = new ChangeSupport(this);
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
            LOG.warning(
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
        Set<String> ids = new HashSet<String>();
        Set<String> urls = new HashSet<String>();
        synchronized (infoCache) {
            if (repoFolder != null) {
                List<FileObject> repos = FileUtil.getOrder(Arrays.asList(repoFolder.getChildren()), false);
                HashSet<FileObject> gone = new HashSet<FileObject>(infoCache.keySet());
                for (FileObject fo : repos) {
                    RepositoryInfo ri = infoCache.get(fo);
                    if (ri == null) {
                        try {
                            ri = RepositoryInfo.createRepositoryInfo(fo);
                            infoCache.put(fo, ri);
                        } catch (/*IllegalArgument,URISyntax*/Exception x) {
                            LOG.log(Level.INFO, fo.getPath(), x);
                            try {
                                fo.delete();
                            } catch (IOException x2) {
                                LOG.log(Level.INFO, null, x2);
                            }
                            continue;
                        }
                    }
                    toRet.add(ri);
                    gone.remove(fo);
                    ids.add(ri.getId());
                    urls.add(ri.getRepositoryUrl());
                }
                for (FileObject g : gone) {
                    infoCache.remove(g);
                }
            }
            for (List<RepositoryInfo> infos : transients.values()) {
                for (RepositoryInfo info : infos) {
                    if (ids.add(info.getId()) && urls.add(info.getRepositoryUrl())) {
                        toRet.add(info);
                    }
                }
            }
        }
        return toRet;
    }

    public synchronized void addOrModifyRepositoryInfo(RepositoryInfo info) {
        try {
            FileObject fo = getRepoFolder().getFileObject(getFileObjectName(info.getId()));
            if (fo == null) {
                List<FileObject> kids = new ArrayList<FileObject>(FileUtil.getOrder(Arrays.asList(getRepoFolder().getChildren()), true));
                fo = getRepoFolder().createData(getFileObjectName(info.getId()));
                kids.add(fo);
                FileUtil.setOrder(kids);
            } else {
                if (infoCache.containsKey(fo)) {
                    infoCache.put(fo, info);
                }
            }
            fo.setAttribute(KEY_TYPE, info.getType());
            fo.setAttribute(KEY_DISPLAY_NAME, info.getName());
            fo.setAttribute(KEY_PATH, info.getRepositoryPath());
            fo.setAttribute(KEY_REPO_URL, info.getRepositoryUrl());
            if (info.getRepositoryUrl() != null) {
                fo.setAttribute(KEY_INDEX_URL, info.getIndexUpdateUrl().equals(info.getRepositoryUrl() + RepositoryInfo.DEFAULT_INDEX_SUFFIX) ? null : info.getIndexUpdateUrl());
            }
        } catch (SyncFailedException x) {
            LOG.log(Level.INFO, "#185147: possible race condition updating " + info.getId(), x);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        cs.fireChange();
    }

    private static final char[] forbiddenChars =
            new char[] {'/', '\\', '?', '%', '*', ':', '|', '"', '<', '>' };
    private static final char replaceChar = '-';


    private String getFileObjectName(String id) {
        char[] chars = id.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            for (int j = 0; j < forbiddenChars.length; j++) {
                if (chars[i] == forbiddenChars[j]) {
                    chars[i] = replaceChar;
                }
            }
        }

        String toRet = String.valueOf(chars);
        if (toRet.contains(".")) { //NOI18N
            toRet = toRet + ".ext"; //NOI18N
        }
        return toRet;
    }

    /**
     * Checks whether a given repository is persisted.
     * @param id the repository's ID
     * @return true if it is persistent (custom), false if it is the local repository or was added transiently
     * @since 2.1
     */
    public boolean isPersistent(String id) {
        return !id.equals("local") && getRepoFolder().getFileObject(getFileObjectName(id)) != null;
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
                LOG.log(Level.FINE, "Cannot delete repository in system filesystem", x); //NOI18N
            }
            cs.fireChange();
        }
    }

    public void setIndexUpdateFrequency(int fr) {
        getPreferences().putInt(PROP_INDEX_FREQ, fr);
    }

    public int getIndexUpdateFrequency() {
        return getPreferences().getInt(PROP_INDEX_FREQ, Boolean.getBoolean("netbeans.full.hack") ? FREQ_NEVER : FREQ_ONCE_WEEK);
    }

    public Date getLastIndexUpdate(String repoId) {
        return new Date(getPreferences().getLong(PROP_LAST_INDEX_UPDATE + "."+repoId, 0));
    }

    public void setLastIndexUpdate(String repoId,Date date) {
        getPreferences().putLong(PROP_LAST_INDEX_UPDATE + "." + repoId, date.getTime());
    }

    /**
     * Register a transient repository.
     * Its definition will not be persisted.
     * Repositories whose ID or URL duplicate that of a persistent repository,
     * or previously registered transient repository, will be ignored
     * (unless and until that repository is removed).
     * {@link #TYPE_NEXUS} is assumed.
     * @param key an arbitrary key for use with {@link #removeTransientRepositories}
     * @param id the repository ID
     * @param displayName a display name (may just be {@code id})
     * @param url the remote URL (prefer the canonical public URL to that of a mirror)
     * @throws URISyntaxException in case the URL is malformed
     * @since 2.1
     */
    public void addTransientRepository(Object key, String id, String displayName, String url) throws URISyntaxException {
        synchronized (infoCache) {
            List<RepositoryInfo> infos = transients.get(key);
            if (infos == null) {
                infos = new ArrayList<RepositoryInfo>();
                transients.put(key, infos);
            }
            infos.add(new RepositoryInfo(id, RepositoryPreferences.TYPE_NEXUS, displayName, null, url));
        }
        cs.fireChange();
    }

    /**
     * Remote all transient repositories associated with a given ID.
     * @param key a key as with {@link #addTransientRepository}
     * @since 2.1
     */
    public void removeTransientRepositories(Object key) {
        synchronized (infoCache) {
            transients.remove(key);
        }
        cs.fireChange();
    }

    /**
     * @since 2.1
     */
    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    /**
     * @since 2.1
     */
    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }

}
