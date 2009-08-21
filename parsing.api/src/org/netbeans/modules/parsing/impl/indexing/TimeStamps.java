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
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Tomas Zezula
 */
public final class TimeStamps {

    private static final Logger LOG = Logger.getLogger(TimeStamps.class.getName());
    private static final String TIME_STAMPS_FILE = "timestamps.properties"; //NOI18N

    private final URL root;

    private final Properties props = new Properties();
    private final Set<String> unseen;

    private FileObject rootFoCache;
    private boolean changed;

    private TimeStamps(final URL root, boolean detectDeletedFiles) throws IOException {
        assert root != null;
        this.root = root;
        this.unseen = detectDeletedFiles ? new HashSet<String>() : null;
        load();
    }

    //where
    private void load () throws IOException {
        FileObject cacheDir = CacheFolder.getDataFolder(root);
        FileObject f = cacheDir.getFileObject(TIME_STAMPS_FILE);
        if (f != null) {
            try {
                final InputStream in = f.getInputStream();
                try {
                    props.load(in);
                } finally {
                    in.close();
                }

                if (unseen != null) {
                    for (Object k : props.keySet()) {
                        unseen.add((String)k);
                    }
                }
            } catch (IOException e) {
                //In case of IOException props are empty, everything is scanned
                LOG.log(Level.FINE, null, e);
            }
        }
    }

    public void store () throws IOException {
        if (true) {
            FileObject cacheDir = CacheFolder.getDataFolder(root);
            FileObject f = FileUtil.createData(cacheDir, TIME_STAMPS_FILE);
            assert f != null;
            try {
                final OutputStream out = f.getOutputStream();
                try {
                    if (unseen != null) {
                        props.keySet().removeAll(unseen);
                    }
                    
                    props.store(out, ""); //NOI18N
                } finally {
                    out.close();
                }
            } catch (IOException e) {
                //In case of IOException props are not stored, everything is scanned next time
                LOG.log(Level.FINE, null, e);
            }
        }
        changed = false;
    }

    public Set<String> getUnseenFiles() {
        return unseen;
    }

    public boolean checkAndStoreTimestamp(final FileObject f) {
        if (rootFoCache == null) {
            rootFoCache = URLMapper.findFileObject(root);
        }
        String relative = FileUtil.getRelativePath(rootFoCache, f);
        String fileId = relative != null ? relative : URLMapper.findURL(f, URLMapper.EXTERNAL).toExternalForm();
        long fts = f.lastModified().getTime();
        String value = (String) props.setProperty(fileId, Long.toString(fts));
        if (value == null) {
            changed|=true;
            LOG.log(Level.FINE, "{0}: lastTimeStamp=null, fileTimeStamp={1} is out of date", new Object [] { f.getPath(), fts }); //NOI18N
            return false;
        }

        if (unseen != null) {
            unseen.remove(fileId);
        }
        long lts = 0L;
        boolean isUpToDate;
        try {
            lts = Long.parseLong(value);
            isUpToDate = lts == fts;
        } catch (NumberFormatException nfe) {
            LOG.warning("Invalid timestamp: " + value + " for file: " + FileUtil.getFileDisplayName(f));   //NOI18N
            isUpToDate = false;
        }
        if (!isUpToDate) {
            LOG.log(Level.FINE, "{0}: lastTimeStamp={1}, fileTimeStamp={2} is out of date", new Object [] { f.getPath(), lts, fts }); //NOI18N
        }

        changed|=!isUpToDate;
        return isUpToDate;
    }    

    public static TimeStamps forRoot(final URL root, boolean detectDeletedFiles) throws IOException {
        return new TimeStamps(root, detectDeletedFiles);
    }

    public static boolean existForRoot (final URL root) throws IOException {
        assert root != null;

        FileObject cacheDir = CacheFolder.getDataFolder(root, true);
        if (cacheDir != null) {
            FileObject f = cacheDir.getFileObject(TIME_STAMPS_FILE);
            if (f != null) {
                return true;
            }
        }
        
        return false;
    }
}
