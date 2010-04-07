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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
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

    // -J-Dorg.netbeans.modules.parsing.impl.indexing.TimeStamps.level=FINE
    private static final Logger LOG = Logger.getLogger(TimeStamps.class.getName());
    private static final String TIME_STAMPS_FILE = "timestamps.properties"; //NOI18N
    private static final String VERSION = "#v2"; //NOI18N

    private final URL root;

    private final LongHashMap<String> timestamps = new LongHashMap<String>();
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
                boolean readOldPropertiesFormat = false;
                {
                    final BufferedReader in = new BufferedReader(new InputStreamReader(f.getInputStream(), "UTF-8")); //NOI18N
                    try {
                        String line = in.readLine();
                        if (line != null && line.startsWith(VERSION)) {
                            // it's the new format
                            LOG.log(Level.FINE, "{0}: reading {1} timestamps", new Object [] { f.getPath(), VERSION }); //NOI18N

                            while (null != (line = in.readLine())) {
                                int idx = line.indexOf('='); //NOI18N
                                if (idx != -1) {
                                    try {
                                        long ts = Long.parseLong(line.substring(idx + 1));
                                        timestamps.put(line.substring(0, idx), ts);
                                    } catch (NumberFormatException nfe) {
                                        LOG.log(Level.FINE, "Invalid timestamp: line={0}, timestamps={1}, exception={2}", new Object[] { line, f.getPath(), nfe }); //NOI18N
                                    }
                                }
                            }
                        } else {
                            // it's the old format from Properties.store()
                            readOldPropertiesFormat = true;
                        }
                    } finally {
                        in.close();
                    }
                }

                if (readOldPropertiesFormat) {
                    LOG.log(Level.FINE, "{0}: reading old Properties timestamps", f.getPath()); //NOI18N
                    final Properties p = new Properties();
                    final InputStream in = f.getInputStream();
                    try {
                        p.load(in);
                    } finally {
                        in.close();
                    }

                    for(Map.Entry<Object, Object> entry : p.entrySet()) {
                        try {
                            timestamps.put((String) entry.getKey(), Long.parseLong((String) entry.getValue()));
                        } catch (NumberFormatException nfe) {
                            LOG.log(Level.FINE, "Invalid timestamp: key={0}, value={1}, timestamps={2}, exception={3}", //NOI18N
                                    new Object[] { entry.getKey(), entry.getValue(), f, nfe });
                        }
                    }
                }
                
                if (unseen != null) {
                    for (Object k : timestamps.keySet()) {
                        unseen.add((String)k);
                    }
                }

                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.log(Level.FINEST, "Timestamps loaded from {0}:", f.getPath()); //NOI18N
                    for(LongHashMap.Entry<String> entry : timestamps.entrySet()) {
                        LOG.log(Level.FINEST, "{0}={1}", new Object [] { entry.getKey(), Long.toString(entry.getValue()) }); //NOI18N
                    }
                    LOG.log(Level.FINEST, "---------------------------"); //NOI18N
                }
            } catch (Exception e) {
                // #176001: catching all exceptions, because j.u.Properties can throw IllegalArgumentException
                // from its load() method
                // In case of any exception props are empty, everything is scanned
                timestamps.clear();
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
                final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(f.getOutputStream(), "UTF-8")); //NOI18N
                try {
                    if (unseen != null) {
                        timestamps.keySet().removeAll(unseen);
                    }

                    // write version
                    out.write(VERSION); //NOI18N
                    out.newLine();

                    // write data
                    for(LongHashMap.Entry<String> entry : timestamps.entrySet()) {
                        out.write(entry.getKey());
                        out.write('='); //NOI18N
                        out.write(Long.toString(entry.getValue()));
                        out.newLine();
                    }

                    out.flush();
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

    public boolean checkAndStoreTimestamp(FileObject f, String relativePath) {
        if (rootFoCache == null) {
            rootFoCache = URLMapper.findFileObject(root);
        }
        String fileId = relativePath != null ? relativePath : URLMapper.findURL(f, URLMapper.EXTERNAL).toExternalForm();
        long fts = f.lastModified().getTime();
        long lts = timestamps.put(fileId, fts);
        if (lts == LongHashMap.NO_VALUE) {
            changed|=true;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "{0}: lastTimeStamp=null, fileTimeStamp={1} is out of date", new Object [] { f.getPath(), fts }); //NOI18N
            }
            return false;
        }

        if (unseen != null) {
            unseen.remove(fileId);
        }
        boolean isUpToDate = lts == fts;
        if (!isUpToDate) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "{0}: lastTimeStamp={1}, fileTimeStamp={2} is out of date", new Object [] { f.getPath(), lts, fts }); //NOI18N
            }
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
