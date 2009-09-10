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

package org.netbeans.modules.parsing.api.indexing;

import java.net.URL;
import java.util.Collection;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Vita Stejskal
 * @since 1.6
 */
public final class IndexingManager {

    // -----------------------------------------------------------------------
    // public implementation
    // -----------------------------------------------------------------------

    public static synchronized IndexingManager getDefault() {
        if (instance == null) {
            instance = new IndexingManager();
        }
        return instance;
    }

    /**
     * Checks whether there are any indexing tasks running.
     *
     * @return <code>true</code> if there are indexing tasks running, otherwise <code>false</code>.
     */
    public boolean isIndexing() {
        return Utilities.isScanInProgress();
    }

    /**
     * Schedules new files for indexing. The set of files passed to this method
     * will be scheduled for reindexing. That means that all the indexers appropriate
     * for each file will have a chance to update their index. No timestamp checks
     * are doen for the files, which means that even files that have not been changed
     * since their last indexing will be reindexed again.
     *
     * <p>This method simply calls {@link #refreshIndex(java.net.URL, java.util.Collection, boolean)}
     * with <code>forceRefresh</code> set to <code>true</code>.
     *
     * <p>IMPORTANT: Please use this with extreme caution. Indexing is generally
     * very expensive operation and the more files you ask to reindex the longer the
     * job will take.
     *
     * @param root The common parent folder of the files that should be reindexed.
     * @param files The files to reindex. Can be <code>null</code> or an empty
     *   collection in which case <b>all</b> files under the <code>root</code> will
     *   be reindexed.
     */
    public void refreshIndex(URL root, Collection<? extends URL> files) {
        refreshIndex(root, files, true);
    }

    /**
     * Schedules new files for indexing. The set of files passed to this method
     * will be scheduled for reindexing. That means that all the indexers appropriate
     * for each file will have a chance to update their index.
     * 
     * <p>If <code>forceRefresh</code> parameter is set to <code>true</code>
     * no timestamp checks will be done for the files passed to this method.
     * This means that even files that have not been changed since their last indexing
     * will be reindexed again. On the other hand if <code>forceRefresh</code> is
     * <code>false</code> the infrastructure will check timestamps and will reindex
     * only files that have been changed since the last time they were indexed.
     *
     * <p>IMPORTANT: Please use this with extreme caution. Indexing is generally
     * very expensive operation and the more files you ask to reindex the longer the
     * job will take.
     *
     * @param root The common parent folder of the files that should be reindexed.
     * @param files The files to reindex. Can be <code>null</code> or an empty
     *   collection in which case <b>all</b> files under the <code>root</code> will
     *   be reindexed.
     * @param fullRescan If <code>true</code> no timestamps check will be done
     *   on the <code>files</code> passed in and they all will be reindexed. If
     *   <code>false</code> only changed files will be reindexed.
     *
     * @since 1.16
     */
    public void refreshIndex(URL root, Collection<? extends URL> files, boolean fullRescan) {
        RepositoryUpdater.getDefault().addIndexingJob(root, files, false, false, false, fullRescan);
    }

    /**
     * Schedules new files for indexing and blocks until they are reindexed. This
     * method does the same thing as {@link #refreshIndex(java.net.URL, java.util.Collection) },
     * but it will block the caller until the index refreshing is done.
     *
     * <p>IMPORTANT: Please use this with extreme caution. Indexing is generally
     * very expensive operation and the more files you ask to reindex the longer the
     * job will take.
     *
     * @param root The common parent folder of the files that should be reindexed.
     * @param files The files to reindex. Can be <code>null</code> or an empty
     *   collection in which case <b>all</b> files under the <code>root</code> will
     *   be reindexed.
     */
    public void refreshIndexAndWait(URL root, Collection<? extends URL> files) {
        refreshIndexAndWait(root, files, true);
    }

    /**
     * Schedules new files for indexing and blocks until they are reindexed. This
     * method does the same thing as {@link #refreshIndex(java.net.URL, java.util.Collection, boolean)  },
     * but it will block the caller until the index refreshing is done.
     *
     * <p>IMPORTANT: Please use this with extreme caution. Indexing is generally
     * very expensive operation and the more files you ask to reindex the longer the
     * job will take.
     *
     * @param root The common parent folder of the files that should be reindexed.
     * @param files The files to reindex. Can be <code>null</code> or an empty
     *   collection in which case <b>all</b> files under the <code>root</code> will
     *   be reindexed.
     * @param fullRescan If <code>true</code> no timestamps check will be done
     *   on the <code>files</code> passed in and they all will be reindexed. If
     *   <code>false</code> only changed files will be reindexed.
     *
     * @since 1.16
     */
    public void refreshIndexAndWait(URL root, Collection<? extends URL> files, boolean fullRescan) {
        RepositoryUpdater.getDefault().addIndexingJob(root, files, false, false, true, fullRescan);
    }

    /**
     * Schedules a new job for refreshing all indices created by the given indexer.
     * This method only works for <code>CustomIndexer</code>s. It is not possible to
     * refresh indices created by <code>EmbeddedIndexer</code>s or <code>BinaryIndexer</code>s.
     *
     * <p>IMPORTANT: Please use this with extreme caution. Indexing is generally
     * very expensive operation and the more files you ask to reindex the longer the
     * job will take.
     *
     * @param indexerName The name of the indexer, which indices should be refreshed.
     *   Can be <code>null</code> in which case all indices created by <b>all</b>
     *   indexers will be refreshed (ie. all types of indexers will used,
     *   not just <code>CustomIndexers</code>).
     *
     * @since 1.8
     */
    public void refreshAllIndices(String indexerName) {
        if (indexerName != null) {
            RepositoryUpdater.getDefault().addIndexingJob(indexerName);
        } else {
            RepositoryUpdater.getDefault().refreshAll(true, false, false);
        }
    }

    /**
     * Schedules a new job that will reindex all known roots thay lie under the
     * <code>folders</code>. This method does the same as {@link #refreshAllIndices(boolean, boolean, org.openide.filesystems.FileObject[])}
     * with <code>fullRescan == true</code> and <code>wait == false</code>.
     *
     * 
     * @param folders The list of folders that may contain some of previously
     *   indexed roots. Can be <code>null</code> in which case all indices for
     *   all roots will be refreshed.
     *
     * @since 1.11
     */
    public void refreshAllIndices(FileObject... folders) {
        refreshAllIndices(true, false, folders);
    }

    /**
     * Schedules a new job that will reindex all known roots thay lie under the
     * <code>folders</code>.
     *
     * <p>IMPORTANT: Please use this with extreme caution. Indexing is generally
     * very expensive operation and the more files you ask to reindex the longer the
     * job will take.
     *
     * @param fullRescan If <code>true</code> full rescan of roots under the
     *   <code>folders</code>. If <code>false</code> only modified, new or deleted
     *   files under the roots will be rescanned.
     * @param wait If <code>true</code> the method will block the caller until
     *   refreshing is done.
     * @param folders The list of folders that may contain some of previously
     *   indexed roots. Can be <code>null</code> in which case all indices for
     *   all roots will be refreshed.
     *
     * @since 1.23
     */
    public void refreshAllIndices(boolean fullRescan, boolean wait, FileObject... folders) {
        RepositoryUpdater.getDefault().refreshAll(fullRescan, wait, false, folders);
    }

    // -----------------------------------------------------------------------
    // private implementation
    // -----------------------------------------------------------------------

    private static IndexingManager instance;

    private IndexingManager() {
        // Start ReporistoryUpdater if it has not been already started
        RepositoryUpdater.getDefault().start(false);
    }

}
