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
package org.netbeans.modules.css.indexing;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.css.gsf.CssLanguage;
import org.netbeans.modules.css.gsf.api.CssParserResult;
import org.netbeans.modules.css.refactoring.api.Entry;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Css content indexer
 *
 * @author mfukala@netbeans.org
 */
public class CssIndexer extends EmbeddingIndexer {

    private static final Logger LOGGER = Logger.getLogger(CssIndexer.class.getSimpleName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);

    public static final String CSS_CONTENT_KEY = "cssContent"; //NOI18N
    public static final String IMPORTS_KEY = "imports"; //NOI18N
    public static final String IDS_KEY = "ids"; //NOI18N
    public static final String CLASSES_KEY = "classes"; //NOI18N
    public static final String HTML_ELEMENTS_KEY = "htmlElements"; //NOI18N
    public static final String COLORS_KEY = "colors"; //NOI18N

//    static {
//	LOG.setLevel(Level.ALL);
//    }
    @Override
    protected void index(Indexable indexable, Result parserResult, Context context) {
        try {
            if(LOG) {
                FileObject fo = parserResult.getSnapshot().getSource().getFileObject();
                LOGGER.log(Level.FINE, "indexing " + fo.getPath()); //NOI18N
            }

            CssFileModel model = new CssFileModel((CssParserResult) parserResult);
            IndexingSupport support = IndexingSupport.getInstance(context);
            IndexDocument document = support.createDocument(indexable);

            storeEntries(model.getIds(), document, IDS_KEY);
            storeEntries(model.getClasses(), document, CLASSES_KEY);
            storeEntries(model.getHtmlElements(), document, HTML_ELEMENTS_KEY);
            storeEntries(model.getImports(), document, IMPORTS_KEY);
            storeEntries(model.getColors(), document, COLORS_KEY);

            //this is a marker key so it's possible to find
            //all stylesheets easily
            document.addPair(CSS_CONTENT_KEY, Boolean.TRUE.toString(), true, true);

            support.addDocument(document);

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void storeEntries(Collection<Entry> entries, IndexDocument doc, String key) {
        if (!entries.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            Iterator<Entry> i = entries.iterator();
            while (i.hasNext()) {
                sb.append(i.next().getName());
                if (i.hasNext()) {
                    sb.append(','); //NOI18N
                }
            }
            sb.append(';'); //end of string
            doc.addPair(key, sb.toString(), true, true);
        }
    }

    public static class Factory extends EmbeddingIndexerFactory {

        static final String NAME = "css"; //NOI18N
        static final int VERSION = 1;

        @Override
        public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
            if (isIndexable(snapshot)) {
                return new CssIndexer();
            } else {
                return null;
            }
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            try {
                IndexingSupport is = IndexingSupport.getInstance(context);
                for(Indexable i : deleted) {
                    is.removeDocuments(i);
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, null, ioe);
            }
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
            try {
                IndexingSupport is = IndexingSupport.getInstance(context);
                for(Indexable i : dirty) {
                    is.markDirtyDocuments(i);
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, null, ioe);
            }
        }

        @Override
        public String getIndexerName() {
            return NAME;
        }

        @Override
        public int getIndexVersion() {
            return VERSION;
        }

        private boolean isIndexable(Snapshot snapshot) {
            //index all files possibly containing css
            return CssLanguage.CSS_MIME_TYPE.equals(snapshot.getMimeType());
        }
    }
}
