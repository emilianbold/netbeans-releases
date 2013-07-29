/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.indexing.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.indexing.api.CndTextIndexKey;
import org.netbeans.modules.cnd.repository.api.CacheLocation;
import org.netbeans.modules.cnd.repository.relocate.api.RelocationSupport;
import org.netbeans.modules.cnd.repository.relocate.api.UnitCodec;
import org.netbeans.modules.parsing.lucene.support.DocumentIndex;
import org.netbeans.modules.parsing.lucene.support.IndexDocument;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.netbeans.modules.parsing.lucene.support.Queries;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Egor Ushakov <gorrus@netbeans.org>
 * @author Vladimir Voskresensky
 */
public final class CndTextIndexImpl {

    private static final String INDEX_FOLDER_NAME = "text_index"; //NOI18N
    private static final String INDEX_LOCK_FILE_NAME = "text_index.lock"; //NOI18N

    private final static Logger LOG = Logger.getLogger("CndTextIndexImpl"); // NOI18N
    private final File lockFile;
    private final DocumentIndex index;
    private final ConcurrentLinkedQueue<StoreQueueEntry> unsavedQueue = new ConcurrentLinkedQueue<StoreQueueEntry>();

    private static final RequestProcessor RP = new RequestProcessor("CndTextIndexImpl saver", 1); //NOI18N
    private final RequestProcessor.Task storeTask = RP.create(new Runnable() {
        @Override
        public void run() {
            store();
        }
    });
    private static final int STORE_DELAY = 3000;
    private final UnitCodec unitCodec;

    public static CndTextIndexImpl create(CacheLocation location) throws IOException {
        File indexRoot = new File(location.getLocation(), INDEX_FOLDER_NAME);
        indexRoot.mkdirs();
        File lock = getLockFile(location);
        lock.createNewFile();
        DocumentIndex index = IndexManager.createDocumentIndex(indexRoot);
        UnitCodec codec = RelocationSupport.get(location);
        CndTextIndexImpl impl = new CndTextIndexImpl(index, lock, codec);
        return impl;
    }

    private CndTextIndexImpl(DocumentIndex index, File lockFile, UnitCodec unitCodec) {
        this.index = index;
        this.lockFile = lockFile;
        assert unitCodec != null;
        this.unitCodec = unitCodec;
    }

    public void put(CndTextIndexKey key, Collection<CharSequence> values) {
        if (LOG.isLoggable(Level.FINE)) {
            if (key.getFileNameIndex() < 2) {
                LOG.log(Level.FINE, "Cnd Text Index put for {0}:\n\t{1}", new Object[]{key, values});
            } else {
                LOG.log(Level.FINE, "Cnd Text Index put for {0}:{1}", new Object[] {key, values.size()});
            }
        }
        unsavedQueue.add(new StoreQueueEntry(key, values));
        // Schedule only if task is not in the queue, see bug 227761
        if (storeTask.getDelay() == 0) {
            storeTask.schedule(STORE_DELAY);
        }
    }

    // the syntax is as in RepositoryListener, although it isn't a listener,
    // it's CnTextIndexManager who listens repository and calls this method
    public void unitRemoved(int unitId, CharSequence unitName) {
        if (unitId < 0) {
            return;
        }
        try {
            String unitPrefix = toPrimaryKeyPrefix(unitId);
            Collection<? extends IndexDocument> queryRes = index.query(CndTextIndexManager.FIELD_UNIT_ID, unitPrefix, Queries.QueryKind.EXACT, "_sn"); // NOI18N
            TreeSet<String> keys = new TreeSet<String>();
            for (IndexDocument doc : queryRes) {
                keys.add(doc.getPrimaryKey());
            }
            for (String pk : keys) {
                index.removeDocument(pk);
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        } catch (InterruptedException ex) {
            // don't report interrupted exception
        }
    }

    private static class StoreQueueEntry {
        private final CndTextIndexKey key;
        private final Collection<CharSequence> ids;

        public StoreQueueEntry(CndTextIndexKey key, Collection<CharSequence> ids) {
            this.key = key;
            this.ids = ids;
        }
    }

    void cleanup() {
        store();
        lockFile.delete();
    }

    synchronized void store() {
        if (unsavedQueue.isEmpty()) {
            return;
        }
        long start = System.currentTimeMillis();
        StoreQueueEntry entry = unsavedQueue.poll();
        while (entry != null) {
            final CndTextIndexKey key = entry.key;
            // use unitID+fileID for primary key, otherwise indexed files from different projects overwrite each others
            IndexDocument doc = IndexManager.createDocument(toPrimaryKey(key));
            doc.addPair(CndTextIndexManager.FIELD_UNIT_ID, toPrimaryKeyPrefix(key.getUnitId()), true, false);
            for (CharSequence id : entry.ids) {
                doc.addPair(CndTextIndexManager.FIELD_IDS, id.toString(), true, false);
            }
            index.addDocument(doc);
            entry = unsavedQueue.poll();
        }
        try {
            index.store(false);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        LOG.log(Level.FINE, "Cnd Text Index store took {0}ms", System.currentTimeMillis() - start); 
    }

    public Collection<CndTextIndexKey> query(String value) {
        // force store
        store();
        
        try {
            // load light weight document with primary key field _sn only
            // it's enough to restore CndTextIndexKey, but reduces memory by not loading FIELD_IDS set
            Collection<? extends IndexDocument> queryRes = index.query(CndTextIndexManager.FIELD_IDS, value, Queries.QueryKind.EXACT, "_sn"); // NOI18N
            HashSet<CndTextIndexKey> res = new HashSet<CndTextIndexKey>(queryRes.size());
            for (IndexDocument doc : queryRes) {
                res.add(fromPrimaryKey(doc.getPrimaryKey()));
            }
            LOG.log(Level.FINE, "Cnd Text Index query for {0}:\n\t{1}", new Object[] {value, res});
            return res;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.emptySet();
    }

    private String toPrimaryKeyPrefix(int unitId) {
        return String.valueOf(unitCodec.unmaskRepositoryID(unitId));
    }

    private String toPrimaryKey(CndTextIndexKey key) {
        return String.valueOf(((long) unitCodec.unmaskRepositoryID(key.getUnitId()) << 32) + (long) key.getFileNameIndex());
    }

    private CndTextIndexKey fromPrimaryKey(String ext) {
        long value = Long.parseLong(ext);
        int unitId = (int) (value >> 32);
        unitId = unitCodec.maskByRepositoryID(unitId);
        int fileNameIndex = (int) (value & 0xFFFFFFFF);
        return new CndTextIndexKey(unitId, fileNameIndex);
    }

    static boolean validate(CacheLocation cacheLocation) {
        /*
        Below is some comments concerning use of Parsing Lucene Support.
        TODO: use Index.getStatus(boolean force) mantioned below to check consistency

        Here is what Lucene developers claim:
        http://blog.mikemccandless.com/2012/03/transactional-lucene.html

        The Lucene is transactional with ACID so it should be always consistent, unfortunately this is different to behaviour
        we are getting. We have and 3.5 version of the Lucene which is quite outdated I want to updated it to 4.x but as the major
        Lucene versions are incompatible it's not trivial task and I don't know if I manage it to do it in NB 7.4.

        There is a method Index.getStatus(boolean force) which checks some problems.
        What it does:
        It checks if the index exists using IndexReader.indexExists(this.fsDir).
        When the force is true is even tries to open the index which sometimes throws Exception when index is broken.
        However sometimes no exception is thrown and it's thrown when you close the index for writing.
        */
        return !getLockFile(cacheLocation).exists();
    }

    private static File getLockFile(CacheLocation location) {
        return new File(location.getLocation(), INDEX_LOCK_FILE_NAME);
    }
}
