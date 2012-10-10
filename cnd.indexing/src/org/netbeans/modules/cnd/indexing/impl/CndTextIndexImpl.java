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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.lucene.support.DocumentIndex;
import org.netbeans.modules.parsing.lucene.support.IndexDocument;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.netbeans.modules.parsing.lucene.support.Queries;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Egor Ushakov <gorrus@netbeans.org>
 */
public class CndTextIndexImpl {
    private final DocumentIndex index;
    private final HashMap<String, Set<String>> unsaved = new HashMap<String, Set<String>>();
    private static final RequestProcessor RP = new RequestProcessor("Index saver", 1); //NOI18N
    private final RequestProcessor.Task storeTask = RP.create(new Runnable() {
        @Override
        public void run() {
            store();
        }
    });
    private static final int STORE_DELAY = 3000;

    public CndTextIndexImpl(DocumentIndex index) {
        this.index = index;
    }

    public void put(String key, Collection<String> values) {
        synchronized (unsaved) {
            unsaved.put(key, new HashSet<String>(values));
        }
        storeTask.schedule(STORE_DELAY);
    }

    void store() {
        synchronized (unsaved) {
            if (unsaved.isEmpty()) {
                return;
            }
            long start = System.currentTimeMillis();
            for (Map.Entry<String, Set<String>> entry : unsaved.entrySet()) {
                final String key = entry.getKey();
                IndexDocument doc = IndexManager.createDocument(key);
                for (String id : entry.getValue()) {
                    doc.addPair(CndTextIndexManager.FIELD_IDS, id, true, true);
                }
                index.addDocument(doc);
            }
            try {
                index.store(false);
                unsaved.clear();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            Logger.getLogger(CndTextIndexImpl.class.getName()).log(Level.INFO, 
                    "Cnd Text Index store took {0}ms", System.currentTimeMillis() - start); //NOI18N
        }
    }

    public Collection<String> query(String value) {
        // force store
        store();
        
        try {
            Collection<? extends IndexDocument> queryRes = index.query(CndTextIndexManager.FIELD_IDS, value, Queries.QueryKind.EXACT, null);
            HashSet<String> res = new HashSet<String>(queryRes.size());
            for (IndexDocument doc : queryRes) {
                res.add(doc.getPrimaryKey());
            }
            return res;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.emptySet();
    }
}