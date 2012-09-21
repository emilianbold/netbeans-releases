/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.ui.issue.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.ui.issue.cache.StorageUtils.FileLocks;
import org.netbeans.modules.bugtracking.ui.issue.cache.StorageUtils.FileLocks.FileLock;

/**
 *
 * @author Tomas Stupka
 */
public class IssueSettingsStorage {
    
    private static final String PROP_COLLAPSED_COMMENT_PREFIX = "collapsed.comment";   // NOI18N
    
    private static IssueSettingsStorage instance;
    private final File storage;

    private IssueSettingsStorage() { 
        storage = getStorageRootFile();
        if(!storage.exists()) {
            storage.mkdirs();
        }
    }
    
    public synchronized static IssueSettingsStorage getInstance() {
        if(instance == null) {
            instance = new IssueSettingsStorage();
        }
        return instance;
    }
    
    private File getStorageRootFile() {
        String userDir = System.getProperty("netbeans.user");                   // NOI18N
        return new File(new File(userDir, "config"), "issue-tracking");         // NOI18N
    }

    public Collection<Long> loadCollapsedCommenst(String repoUrl, String id) {
        File file = getIssuePropertiesFile(repoUrl, id);
        FileLock l = FileLocks.getLock(file);
        try {
            Properties p = load(file, repoUrl, id);
            Set<Long> s = new HashSet<Long>();
            for(Object k : p.keySet()) {
                String key = k.toString();
                if(key.startsWith(PROP_COLLAPSED_COMMENT_PREFIX) && "true".equals(p.get(key))) {
                    s.add(Long.parseLong(key.substring(PROP_COLLAPSED_COMMENT_PREFIX.length())));
                }
            }
            return s;
        } catch (IOException ex) {
            BugtrackingManager.LOG.log(Level.WARNING, repoUrl + " " + id, ex);
        } finally {
            l.release();
        }
                
        return Collections.EMPTY_SET;
    }
    
    private Properties load(File file, String repoUrl, String id) throws IOException {
        Properties p = new Properties();
        if(!file.exists()) {
            file.createNewFile();
        }
        FileInputStream fis = new FileInputStream(file);
        try {
            p.load(fis);
        } finally {
            fis.close();
        }
        return p;
        }
    
    public void storeCollapsedComments(Collection<Long> collapsedComments, String repoUrl, String id) {
        File file = getIssuePropertiesFile(repoUrl, id);
        FileLock l = FileLocks.getLock(file);
        try {
            Properties p = load(file, repoUrl, id);
            clear(p, PROP_COLLAPSED_COMMENT_PREFIX);
            for (Long i : collapsedComments) {
                p.put(PROP_COLLAPSED_COMMENT_PREFIX + i, "true");
            }
            FileOutputStream fos = new FileOutputStream(file);
            try {
                p.store(fos, "");
            } finally {
                fos.close();
            }
        } catch (IOException ex) {
            BugtrackingManager.LOG.log(Level.WARNING, repoUrl + " " + id, ex);
        } finally {
            l.release();
        }
    }

    private void clear(Properties p, String keyPrefix) {
        Iterator<Object> it = p.keySet().iterator();
        while(it.hasNext()) {
            String key = it.next().toString();
            if(key.startsWith(keyPrefix)) {
                it.remove();
            }
        }
    }
    
    private File getIssuePropertiesFile(String repoUrl, String id) {
        return new File(StorageUtils.getNameSpaceFolder(storage, repoUrl), id);
    }
}
