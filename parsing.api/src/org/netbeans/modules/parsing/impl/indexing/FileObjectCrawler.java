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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.queries.VisibilityQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Zezula
 */
public final class FileObjectCrawler extends Crawler {

    private static final Logger LOG = Logger.getLogger(FileObjectCrawler.class.getName());
    
    private final FileObject root;
    private final ClassPath.Entry entry;
    private final FileObject[] files;

    public FileObjectCrawler(FileObject root, boolean checkTimeStamps, ClassPath.Entry entry, CancelRequest cancelRequest) throws IOException {
        super (root.getURL(), checkTimeStamps, cancelRequest);
        this.root = root;
        this.entry = entry;
        this.files = null;
    }

    public FileObjectCrawler(FileObject root, FileObject[] files, boolean checkTimeStamps, ClassPath.Entry entry, CancelRequest cancelRequest) throws IOException {
        super (root.getURL(), checkTimeStamps, cancelRequest);
        this.root = root;
        this.entry = entry;
        this.files = files;
    }

    @Override
    protected boolean collectResources(Collection<IndexableImpl> result) {
        final boolean finished;
        final long tm1 = System.currentTimeMillis();
        final Stats stats = LOG.isLoggable(Level.FINE) ? new Stats() : null;

        if (files != null) {
            finished = collect(files, root, result, stats, entry);
        } else {
            finished = collect(root.getChildren(), root, result, stats, entry);
        }

        final long tm2 = System.currentTimeMillis();
        if (LOG.isLoggable(Level.FINE)) {
            String rootUrl;
            try {
                rootUrl = root.getURL().toString();
            } catch (FileStateInvalidException ex) {
                // ignore
                rootUrl = root.toString();
            }

            LOG.log(Level.FINE, String.format("Up-to-date check of %d files under %s took %d ms", stats.filesCount, rootUrl, tm2 - tm1 )); //NOI18N

            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "File extensions histogram for {0}:", rootUrl);
                Stats.logHistogram(Level.FINER, stats.extensions);
                LOG.finer("----");

// mimetypes histogram is no longer available after crawling the files
//                LOG.log(Level.FINER, "Mime types histogram for {0}:", rootUrl);
//                Stats.logHistogram(Level.FINER, stats.mimeTypes);
//                LOG.finer("----");
            }
        }

        return finished;
    }

    private boolean collect (FileObject[] fos, FileObject root,
            final Collection<IndexableImpl> cache,
            final Stats stats, final ClassPath.Entry entry) {
        for (FileObject fo : fos) {
            //keep the same logic like in RepositoryUpdater
            if (isCancelled()) {
                return false;
            }
            if (!fo.isValid() || !VisibilityQuery.getDefault().isVisible(fo)) {
                continue;
            }
            if (entry != null && !entry.includes(fo)) {
                continue;
            }
            if (fo.isFolder()) {
                if (!collect(fo.getChildren(), root, cache, stats, entry)) {
                    return false;
                }
            } else {
                if (stats != null) {
                    stats.filesCount++;
                    Stats.inc(stats.extensions, fo.getExt());
                }

                if (!isUpToDate(fo)) {
                    String relativePath = FileUtil.getRelativePath(root, fo);
                    cache.add(new FileObjectIndexable(root, relativePath));
                }
            }
        }

        return true;
    }

    private static final class Stats {
        public int filesCount;
        public Map<String, Integer> extensions = new HashMap<String, Integer>();
        public Map<String, Integer> mimeTypes = new HashMap<String, Integer>();
        public static void inc(Map<String, Integer> m, String k) {
            Integer i = m.get(k);
            if (i == null) {
                m.put(k, 1);
            } else {
                m.put(k, i.intValue() + 1);
            }
        }
        public static void logHistogram(Level level, Map<String, Integer> data) {
            Map<Integer, Set<String>> sortedMap = new TreeMap<Integer, Set<String>>(REVERSE);
            for(String item : data.keySet()) {
                Integer freq = data.get(item);
                Set<String> items = sortedMap.get(freq);
                if (items == null) {
                    items = new TreeSet<String>();
                    sortedMap.put(freq, items);
                }
                items.add(item);
            }
            for(Integer freq : sortedMap.keySet()) {
                for(String item : sortedMap.get(freq)) {
                    LOG.log(level, "{0}: {1}", new Object [] { item, freq }); //NOI18N
                }
            }
        }
        private static final Comparator<Integer> REVERSE = new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {
                return -1 * o1.compareTo(o2);
            }
        };
    } // End of Stats class
}
