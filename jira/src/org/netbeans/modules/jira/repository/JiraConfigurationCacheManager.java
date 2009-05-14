/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jira.repository;

import java.awt.EventQueue;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.netbeans.libs.bugtracking.BugtrackingRuntime;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.repository.JiraConfiguration.ConfigurationData;
import org.netbeans.modules.jira.util.FileUtils;
import org.netbeans.modules.jira.util.JiraUtils;

/**
 *
 * @author Ondra Vrabec
 */
public final class JiraConfigurationCacheManager {

    private static JiraConfigurationCacheManager instance;
    private HashMap<String, ConfigurationData> cacheData;
    private static final String CACHE_DIR_NAME = "jiraconfiguration";   //NOI18N
    private static final String CACHE_FILE_NAME = "configurationcache"; //NOI18N
    private static final String VERSION = "0.12";                        //NOI18N
    private final Object DATA_LOCK = new Object();
    private static final Level LOG_LEVEL = JiraUtils.isAssertEnabled() ? Level.SEVERE : Level.INFO;
    

    private JiraConfigurationCacheManager () {
        
    }

    public static JiraConfigurationCacheManager getInstance() {
        if (instance == null) {
            instance = new JiraConfigurationCacheManager();
        }
        return instance;
    }

    /**
     * Stores cached configuration data to a disk
     */
    public void shutdown() {
        Jira.LOG.fine("shutdown: saving configuration data");                //NOI18N
        if (cacheData == null) {
            Jira.LOG.fine("shutdown: no data loaded, no data saved");
        }
        File f = new File(BugtrackingRuntime.getInstance().getCacheStore(), CACHE_DIR_NAME);
        f.mkdirs();
        if (!f.canWrite()) {
            Jira.LOG.warning("shutdown: Cannot create cache dirs");          //NOI18N
            return;
        }
        ObjectOutputStream out = null;
        File file = new File(f, CACHE_FILE_NAME + ".tmp");
        boolean success = false;
        try {
            // saving to a temp file
            out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            out.writeUTF(VERSION);
            out.writeInt(cacheData.size());
            for (Entry<String, ConfigurationData> entry : cacheData.entrySet()) {
                out.writeObject(entry.getKey());
                final ConfigurationData data = entry.getValue();
                // lock the data so it's refresh (in JiraConfiguration) does not corrupt it
                synchronized(data) {
                    out.writeObject(data);
                }
            }
            success = true;
        } catch (IOException ex) {
            Jira.LOG.log(LOG_LEVEL, null, ex);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        if (success) {
            // rename the temp file to the permanent one
            File newFile = new File(f, CACHE_FILE_NAME);
            try {
                FileUtils.renameFile(file, newFile);
                success = true;
            } catch (IOException ex) {
                Jira.LOG.log(LOG_LEVEL, null, ex);
            }
        }
        if (!success) {
            file.deleteOnExit();
        }
    }

    /**
     * Returns cached data for the given repository URL.
     * The first access to this cache manager WILL access I/O
     * @param repoUrl
     * @return cached data or null if it does not exist
     */
    ConfigurationData getCachedData(String repoUrl) {
        assert !EventQueue.isDispatchThread();
        return getCachedData().get(repoUrl);
    }

    /**
     * Sets the cached data for the given repository
     * The first access to this cache manager WILL access I/O
     * @param url not null
     * @param data not null
     */
    void setCachedData(String url, ConfigurationData data) {
        if (url == null || data == null) {
            return;
        }
        assert !EventQueue.isDispatchThread();
        getCachedData().put(url, data);
    }

    /**
     * Removes cached data for the given repository url
     * The first access to this cache manager WILL access I/O
     * @param url
     */
    void removeCachedData (String url) {
        if (url == null) {
            return;
        }
        getCachedData().remove(url);
    }

    private HashMap<String, ConfigurationData> getCachedData () {
        synchronized (DATA_LOCK) {
            if (cacheData == null) {
                loadConfigurationData();
            }
        }
        return cacheData;
    }

    private void loadConfigurationData () {
        Jira.LOG.fine("loadConfigurationData: loading configuration data"); //NOI18N

        File f = new File(BugtrackingRuntime.getInstance().getCacheStore(), CACHE_DIR_NAME);
        try {
            ObjectInputStream ois = null;
            File file = new File(f, CACHE_FILE_NAME);
            if (!file.canRead()) {
                Jira.LOG.info("loadConfigurationData: no cached data");       //NOI18N
                return;
            }
            try {
                ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
                String version = ois.readUTF();
                if (!VERSION.equals(version)) {
                    Jira.LOG.info("loadConfigurationData: old data format: " + version); //NOI18N
                    return;
                }
                int size = ois.readInt();
                Jira.LOG.fine("loadConfigurationData: loading " + size + " items"); //NOI18N
                cacheData = new HashMap<String, ConfigurationData>(size + 5);
                while (size-- > 0) {
                    String repoUrl = (String) ois.readObject();
                    Jira.LOG.fine("loadConfigurationData: loading data for " + repoUrl); //NOI18N
                    cacheData.put(repoUrl, (ConfigurationData) ois.readObject());
                }
            } catch (IOException ex) {
                Jira.LOG.log(LOG_LEVEL, null, ex);
            } catch (ClassNotFoundException ex) {
                Jira.LOG.log(LOG_LEVEL, null, ex);
            } finally {
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException e) {
                    }
                }
            }
        } finally {
            if (cacheData == null) {
                cacheData = new HashMap<String, ConfigurationData>(5);
            }
        }
    }
}