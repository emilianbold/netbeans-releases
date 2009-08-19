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

package org.netbeans.modules.parsing.spi.indexing;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.CancelRequest;
import org.netbeans.modules.parsing.impl.indexing.IndexFactoryImpl;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 * Represents a context of indexing given root.
 * @author Tomas Zezula
 */
//@NotThreadSafe
public final class Context {

    private final URL rootURL;
    private final FileObject indexBaseFolder;
    private final FileObject indexFolder;
    private final String indexerName;
    private final int indexerVersion;
    private final boolean followUpJob;
    private final boolean checkForEditorModifications;
    private boolean allFilesJob;
    private final boolean sourceForBinaryRoot;
    private final CancelRequest cancelRequest;
    
    private FileObject root;
    private IndexingSupport indexingSupport;

    //unit test
    final IndexFactoryImpl factory;

    Context (final FileObject indexBaseFolder,
             final URL rootURL, final String indexerName, final int indexerVersion,
             final IndexFactoryImpl factory, boolean followUpJob,
             final boolean checkForEditorModifications,
             final boolean sourceForBinaryRoot,
             final CancelRequest cancelRequest
    ) throws IOException {
        assert indexBaseFolder != null;
        assert rootURL != null;
        assert indexerName != null;
        this.indexBaseFolder = indexBaseFolder;
        this.rootURL = rootURL;
        this.indexerName = indexerName;
        this.indexerVersion = indexerVersion;
        this.factory = factory;
        this.followUpJob = followUpJob;
        final String path = getIndexerPath(indexerName, indexerVersion); //NOI18N
        this.indexFolder = FileUtil.createFolder(this.indexBaseFolder,path);
        this.checkForEditorModifications = checkForEditorModifications;
        this.sourceForBinaryRoot = sourceForBinaryRoot;
        this.cancelRequest = cancelRequest;
    }

    /**
     * Returns the cache folder where the indexer may store language metadata.
     * For each root and indexer there exist a separate cache folder.
     * @return The cache folder
     */
    public FileObject getIndexFolder () {        
        return this.indexFolder;
    }

    /**
     * Return the {@link URL} of the processed root
     * @return the absolute URL
     */
    public URL getRootURI () {
        return this.rootURL;
    }

    /**
     * Return the processed root, may return null
     * when the processed root was deleted.
     * The {@link Context#getRootURI()} can be used in
     * this case.
     * @return the root or null when the root doesn't exist
     */
    public FileObject getRoot () {
        if (root == null) {            
            root = URLMapper.findFileObject(this.rootURL);
        }
        return root;
    }

    /**
     * Schedules additional files for reindexing. This method can be used for requesting
     * reindexing of additional files that an indexer discovers while indexing some
     * other files. The files passed to this method will be processed by a new
     * indexing job after the current indexing is finished.
     * That means that all the indexers appropriate
     * for each file will have a chance to update their index. No timestamp checks
     * are done on the additional files, which means that even files that have not been changed
     * since their last indexing will be reindexed again.
     *
     * @param root The common parent folder of the files that should be reindexed.
     * @param files The files to reindex. Can be <code>null</code> or an empty
     *   collection in which case <b>all</b> files under the <code>root</code> will
     *   be reindexed.
     *
     * @since 1.3
     */
    public void addSupplementaryFiles(URL root, Collection<? extends URL> files) {
        Logger repouLogger = Logger.getLogger(RepositoryUpdater.class.getName());
        if (repouLogger.isLoggable(Level.FINE)) {
            repouLogger.fine("addSupplementaryFiles: root=" + root + ", files=" + files); //NOI18N
        }
        RepositoryUpdater.getDefault().addIndexingJob(root, files, true, false, false, true);
    }

    /**
     * Indicates whether the current indexing job was requested by calling
     * {@link #addSupplementaryFiles(java.net.URL, java.util.Collection) } method.
     *
     * @return <code>true</code> if the indexing job was requested by <code>addSupplementaryFiles</code>,
     *   otherwise <code>false</code>.
     *
     * @since 1.3
     */
    public boolean isSupplementaryFilesIndexing() {
        return followUpJob;
    }

    /**
     * Indicates whether all files under the root are being indexed. In general indexing
     * jobs can either index selected files under a given root (eg. when scheduled
     * through {@link IndexingManager}) or they can index all files under the root. Some
     * indexers are interested in knowing this information in order to optimize their
     * indexing.
     *
     * @return <code>true</code> if indexing all files under the root.
     *
     * @since 1.6
     */
    public boolean isAllFilesIndexing() {
        return allFilesJob;
    }

    /**
     * Indicates whether sources of some binary library are being indexed. Some
     * indexers are interested in knowing this information in order to optimize their
     * indexing.
     *
     * @return <code>true</code> if indexing sources for binary root.
     *
     * @since 1.17
     */
    public boolean isSourceForBinaryRootIndexing() {
        return sourceForBinaryRoot;
    }

    /**
     * Notifies indexers whether they should use editor documents rather than just
     * files. This is mostly useful for <code>CustomIndexer</code>s that may optimize
     * their work and not try to find editor documents for their <code>Indexable</code>s.
     *
     * <p><code>EmbeddingIndexer</code>s can safely ignore this flag since they operate
     * on <code>Parser.Result</code>s and <code>Snapshot</code>s, which are guaranteed
     * to be in sync with editor documents or loaded efficiently from a file if the
     * file is not opened in the editor.
     *
     * @return <code>false</code> if indexers don't have to care about possible
     *   editor modifications or <code>true</code> otherwise.
     * 
     * @since 1.10
     */
    public boolean checkForEditorModifications() {
        return checkForEditorModifications;
    }

    /**
     * @return
     * @since 1.13
     */
    public boolean isCancelled() {
        return cancelRequest == null ? false : cancelRequest.isRaised();
    }

    String getIndexerName () {
        return this.indexerName;
    }

    int getIndexerVersion () {
        return this.indexerVersion;
    }

    void attachIndexingSupport(IndexingSupport support) {
        assert this.indexingSupport == null;
        this.indexingSupport = support;
    }

    IndexingSupport getAttachedIndexingSupport() {
        return this.indexingSupport;
    }

    void setAllFilesJob (final boolean allFilesJob) {
        this.allFilesJob = allFilesJob;
    }

    static String getIndexerPath (final String indexerName, final int indexerVersion) {
        return indexerName + "/" + indexerVersion; //NOI18N
    }
}
