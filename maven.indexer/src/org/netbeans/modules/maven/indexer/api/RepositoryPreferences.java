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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import static org.netbeans.modules.maven.indexer.api.Bundle.*;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;

/**
 * List of Maven repositories of interest.
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
    

    private static final String KEY_TYPE = "provider";//NOI18N
    private static final String KEY_DISPLAY_NAME = "name";//NOI18N
    private static final String KEY_PATH = "path";//NOI18N
    private static final String KEY_INDEX_URL = "index";//NOI18N
    private static final String KEY_REPO_URL = "url";//NOI18N
    /*index settings */
    public static final String PROP_INDEX_FREQ = "indexUpdateFrequency"; //NOI18N
    public static final String PROP_LAST_INDEX_UPDATE = "lastIndexUpdate"; //NOI18N
    public static final int FREQ_ONCE_WEEK = 0;
    public static final int FREQ_ONCE_DAY = 1;
    public static final int FREQ_STARTUP = 2;
    public static final int FREQ_NEVER = 3;
    private final Map<String,RepositoryInfo> infoCache = new HashMap<String,RepositoryInfo>();
    private final Map<Object,List<RepositoryInfo>> transients = new LinkedHashMap<Object,List<RepositoryInfo>>();
    private final RepositoryInfo local;
    private final ChangeSupport cs = new ChangeSupport(this);

    @Messages("local=Local")
    private RepositoryPreferences() {
        try {
            local = new RepositoryInfo(LOCAL_REPO_ID, TYPE_NEXUS, local(), EmbedderFactory.getProjectEmbedder().getLocalRepository().getBasedir(), null);
        } catch (URISyntaxException x) {
            throw new AssertionError(x);
        }
    }

    private Preferences getPreferences() {
        return NbPreferences.root().node("org/netbeans/modules/maven/nexus/indexing"); //NOI18N
    }

    private Preferences storage() {
        return NbPreferences.root().node("org/netbeans/modules/maven/repositories");
    }

    public synchronized static RepositoryPreferences getInstance() {
        if (instance == null) {
            instance = new RepositoryPreferences();
        }
        return instance;
    }

    private static @CheckForNull RepositoryInfo createRepositoryInfo(Preferences p) throws URISyntaxException {
        String type = p.get(KEY_TYPE, TYPE_NEXUS);
        String id = p.name();
        String name = p.get(KEY_DISPLAY_NAME, null);
        if (name == null) {
            return null;
        }
        String path = p.get(KEY_PATH, null);
        String repourl = p.get(KEY_REPO_URL, null);
        String indexurl = p.get(KEY_INDEX_URL, null);
        return new RepositoryInfo(id, type, name, path, repourl, indexurl);
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
        List<RepositoryInfo> toRet = new ArrayList<RepositoryInfo>();
        toRet.add(local);
        Set<String> ids = new HashSet<String>();
        ids.add(LOCAL_REPO_ID);
        Set<String> urls = new HashSet<String>();
        synchronized (infoCache) {
            Preferences storage = storage();
            try {
                Set<String> gone = new HashSet<String>(infoCache.keySet());
                for (String c : storage.childrenNames()) {
                    RepositoryInfo ri = infoCache.get(c);
                    if (ri == null) {
                        Preferences child = storage.node(c);
                        try {
                            ri = createRepositoryInfo(child);
                            if (ri == null) {
                                continue;
                            }
                            infoCache.put(c, ri);
                        } catch (/*IllegalArgument,URISyntax*/Exception x) {
                            LOG.log(Level.INFO, c, x);
                            try {
                                child.removeNode();
                            } catch (BackingStoreException x2) {
                                LOG.log(Level.INFO, null, x2);
                            }
                            continue;
                        }
                    }
                    toRet.add(ri);
                    gone.remove(c);
                    ids.add(ri.getId());
                    urls.add(ri.getRepositoryUrl());
                }
                for (String g : gone) {
                    infoCache.remove(g);
                }
            } catch (BackingStoreException x) {
                LOG.log(Level.INFO, null, x);
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

    public void addOrModifyRepositoryInfo(RepositoryInfo info) {
        String id = info.getId();
        synchronized (infoCache) {
            infoCache.put(id, info);
            Preferences p = storage().node(id);
            put(p, KEY_TYPE, info.getType().equals(TYPE_NEXUS) ? null : info.getType());
            p.put(KEY_DISPLAY_NAME, info.getName());
            put(p, KEY_PATH, info.getRepositoryPath());
            put(p, KEY_REPO_URL, info.getRepositoryUrl());
            if (info.getRepositoryUrl() != null) {
                put(p, KEY_INDEX_URL, info.getIndexUpdateUrl().equals(info.getRepositoryUrl() + RepositoryInfo.DEFAULT_INDEX_SUFFIX) ? null : info.getIndexUpdateUrl());
            }
        }
        cs.fireChange();
    }
    private static void put(@NonNull Preferences p, @NonNull String key, @NullAllowed String value) {
        if (value != null) {
            p.put(key, value);
        } else {
            p.remove(key);
        }
    }

    /**
     * Checks whether a given repository is persisted.
     * @param id the repository's ID
     * @return true if it is persistent (custom), false if it is the local repository or was added transiently
     * @since 2.1
     */
    public boolean isPersistent(String id) {
        return storage().node(id).get(KEY_DISPLAY_NAME, null) != null;
    }
    
    public void removeRepositoryInfo(RepositoryInfo info) {
        synchronized (infoCache) {
            String id = info.getId();
            infoCache.remove(id);
            try {
                storage().node(id).removeNode();
            } catch (BackingStoreException x) {
                LOG.log(Level.INFO, null, x);
            }
        }
        cs.fireChange();
    }

    public void setIndexUpdateFrequency(int fr) {
        getPreferences().putInt(PROP_INDEX_FREQ, fr);
    }

    public int getIndexUpdateFrequency() {
        return getPreferences().getInt(PROP_INDEX_FREQ, Boolean.getBoolean("netbeans.full.hack") ? FREQ_NEVER : FREQ_ONCE_WEEK);
    }

    public Date getLastIndexUpdate(String repoId) {
        long old = getPreferences().getLong(PROP_LAST_INDEX_UPDATE + "." + repoId, 0); // compatibility
        if (old != 0) { // upgrade it
            getPreferences().remove(PROP_LAST_INDEX_UPDATE + "." + repoId);
            storage().node(repoId).putLong(PROP_LAST_INDEX_UPDATE, old);
        }
        return new Date(storage().node(repoId).getLong(PROP_LAST_INDEX_UPDATE, 0));
    }

    public void setLastIndexUpdate(String repoId,Date date) {
        getPreferences().remove(PROP_LAST_INDEX_UPDATE + "." + repoId);
        storage().node(repoId).putLong(PROP_LAST_INDEX_UPDATE, date.getTime());
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
