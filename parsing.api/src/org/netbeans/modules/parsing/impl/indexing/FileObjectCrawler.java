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
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.queries.VisibilityQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
public final class FileObjectCrawler extends Crawler {

    private static final Logger LOG = Logger.getLogger(FileObjectCrawler.class.getName());
    private static final boolean isUnix = Utilities.isUnix();
    private static final boolean isMac = Utilities.isMac();
    /*test*/ static Map<FileObject,LinkType> mockLinkTypes;
    
    private final FileObject root;
    private final ClassPath.Entry entry;
    private final FileObject[] files;

    public FileObjectCrawler(FileObject root, boolean checkTimeStamps, ClassPath.Entry entry, CancelRequest cancelRequest) throws IOException {
        super (root.getURL(), checkTimeStamps, true, true, cancelRequest);
        this.root = root;
        this.entry = entry;
        this.files = null;
    }

    public FileObjectCrawler(FileObject root, FileObject[] files, boolean checkTimeStamps, ClassPath.Entry entry, CancelRequest cancelRequest) throws IOException {
        super (root.getURL(), checkTimeStamps, false, supportsAllFiles(root, files), cancelRequest);
        this.root = root;
        this.entry = entry;
        this.files = files;
    }

    @Override
    protected boolean collectResources(Collection<IndexableImpl> resources, Collection<IndexableImpl> allResources) {
        boolean finished = true;
        final long tm1 = System.currentTimeMillis();
        final Stats stats = LOG.isLoggable(Level.FINE) ? new Stats() : null;

        if (files != null) {
            if (files.length > 1) {
                Map<FileObject, Set<FileObject>> clusters = new HashMap<FileObject, Set<FileObject>>();
                for(FileObject f : files) {
                    FileObject parent = f.getParent();
                    Set<FileObject> cluster = clusters.get(parent);
                    if (cluster == null) {
                        cluster = new HashSet<FileObject>();
                        clusters.put(parent, cluster);
                    }
                    cluster.add(f);
                }
                for(FileObject parent : clusters.keySet()) {
                    Set<FileObject> cluster = clusters.get(parent);
                    StringBuilder relativePath = getRelativePath(root, parent);
                    if (relativePath != null) {
                        finished = collect(
                                cluster.toArray(new FileObject[cluster.size()]),
                                root,
                                resources,
                                allResources,
                                stats,
                                entry,
                                null,
                                relativePath);
                        if (!finished) {
                            break;
                        }
                    } // else invalid (eg. deleted) FileObject encountered
                }
            } else if (files.length == 1) {
                StringBuilder relativePath = getRelativePath(root, files[0].getParent());
                if (relativePath != null) {
                    finished = collect(files, root, resources, allResources, stats, entry, null, relativePath);
                } // else invalid (eg. deleted) FileObject encountered
            }
        } else {
            finished = collect(root.getChildren(), root, resources, allResources, stats, entry, null, new StringBuilder());
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
            LOG.log(Level.FINE,
                    "Symlink tests took {0}ms, {1} symlinks into root found.",    //NOI18N
                    new Object[] {
                        stats.linkCheckTime,
                        stats.linkCount
                    });
        }

        return finished;
    }

    private boolean collect (
            final @NonNull FileObject[] fos,
            final @NonNull FileObject root,
            final @NonNull Collection<IndexableImpl> resources,
            final @NonNull Collection<IndexableImpl> allResources,
            final @NullAllowed Stats stats,
            final @NullAllowed ClassPath.Entry entry,
            final @NullAllowed LinkType parentLinkType,
            final @NonNull StringBuilder relativePathBuilder)
    {
        int parentPathEnd = relativePathBuilder.length();

        for (FileObject fo : fos) {
            //keep the same logic like in RepositoryUpdater
            if (isCancelled()) {
                return false;
            }
            if (!fo.isValid() || !isVisible(fo)) {
                continue;
            }

            relativePathBuilder.append(fo.getNameExt());
            boolean folder = fo.isFolder();
            if (folder) relativePathBuilder.append('/');
            String relativePath = relativePathBuilder.toString();
            try {
                if (entry != null && !entry.includes(relativePath)) {
                    continue;
                }
                if (folder) {
                    final LinkType linkType = getLinkType(fo, root, parentLinkType, stats);
                    if (linkType != LinkType.IN) {
                        if (!collect(fo.getChildren(), root, resources, allResources, stats, entry, linkType, relativePathBuilder)) {
                            return false;
                        }
                    }
                } else {
                    if (stats != null) {
                        stats.filesCount++;
                        Stats.inc(stats.extensions, fo.getExt());
                    }

                    FileObjectIndexable indexable = new FileObjectIndexable(root, relativePath);
                    allResources.add(indexable);
                    if (!isUpToDate(fo, relativePath)) {
                        resources.add(indexable);
                    }
                }
            } finally {
                relativePathBuilder.delete(parentPathEnd, relativePathBuilder.length());
            }
        }

        return true;
    }

    private StringBuilder getRelativePath(FileObject folder, FileObject fo) {
        String rp = FileUtil.getRelativePath(folder, fo);
        if (rp != null) {
            StringBuilder relativePath = new StringBuilder(rp);
            if (relativePath.length() > 0) {
                relativePath.append('/'); //NOI18N
            }
            return relativePath;
        } else {
            return null;
        }
    }

    private boolean isVisible (final @NonNull FileObject fo) {
        try {
            return VisibilityQuery.getDefault().isVisible(fo);
        } finally {
            setListenOnVisibility(true);
        }
    }
    
    //Todo: Not exaclty correct. The correct implementation should find if whole root content
    //is covered by files. But correct implementation will be very very slow and probably no one
    //calls it with such params.
    private static boolean supportsAllFiles(final FileObject root, final FileObject... files) {
        for (FileObject file : files) {
            if (root == file) {
                return true;
            }
        }
        return false;
    }

    private static @NonNull LinkType getLinkType(
            final @NonNull FileObject folder,
            final @NonNull FileObject root,
            final @NullAllowed LinkType parentLinkType,
            final @NullAllowed Stats stats) {
        final long st = System.currentTimeMillis();
        boolean inLink = false;
        try {
            if (parentLinkType == LinkType.OUT) {
                return parentLinkType;
            }
            if (mockLinkTypes != null) {
                return mockLinkTypes.get(folder) != null ?
                        mockLinkTypes.get(folder) : LinkType.NO;
            }
            if (!isUnix && !isMac) {
                return LinkType.NO;
            }            
            final File directory = FileUtil.toFile(folder);
            if (directory == null) {
                return LinkType.NO;
            }
            final File canDirectory;
            try {
                canDirectory = directory.getCanonicalFile();
            } catch (IOException ioe) {
                return LinkType.NO;
            }
            final String dirPath = directory.getAbsolutePath();
            final String canDirPath = canDirectory.getAbsolutePath();
            final boolean notLink = isMac ? dirPath.equalsIgnoreCase(canDirPath) :
                    dirPath.equals(canDirPath);
            if (notLink) {
                return LinkType.NO;
            }
            final File rootFile = FileUtil.toFile(root);
            if (rootFile == null) {
                return LinkType.OUT;
            }
            if (Util.isParentOf(rootFile, canDirectory)) {
                inLink = true;
                return LinkType.IN;
            } else {
                return LinkType.OUT;
            }
        } finally {
            if (stats != null) {
                if (inLink) {
                    stats.linkCount++;
                }
                stats.linkCheckTime+=(System.currentTimeMillis()-st);
            }
        }
    }

    /*test*/ static enum LinkType {
        NO,
        IN,
        OUT
    }
    
    private static final class Stats {
        public int filesCount;
        public long linkCheckTime;
        public int linkCount;
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
