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
package org.netbeans.modules.javascript2.editor.index;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.javascript2.editor.model.JsObject;
import org.netbeans.modules.javascript2.editor.model.Model;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class JsIndexer extends EmbeddingIndexer {

    private static final Logger LOG = Logger.getLogger(JsIndexer.class.getName());

    private static final Collection<String> INDEXABLE_EXTENSIONS = Arrays.asList("js", "sdoc");

    @Override
    protected void index(Indexable indexable, Result result, Context context) {
        LOG.log(Level.FINE, "Indexing: {0}, fullPath: {1}", new Object[]{indexable.getRelativePath(), result.getSnapshot().getSource().getFileObject().getPath()});
        JsParserResult parserResult = (JsParserResult) result;
        Model model = parserResult.getModel();

        IndexingSupport support;
        try {
            support = IndexingSupport.getInstance(context);
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
            return;
        }

        JsObject globalObject = model.getGlobalObject();
        for(JsObject object : globalObject.getProperties().values()) {
            storeObject(object, support, indexable);
        }
    }

    private void storeObject(JsObject object, IndexingSupport support, Indexable indexable) {
        if (object.isDeclared()) {
            // if it's delcared, then store in the index as new document.
            support.addDocument(IndexedElement.createDocument(object, support, indexable));
        }
        // look for all other properties. Even if the object doesn't have to be delcared in the file
        // there can be declared it's properties or methods
        for (JsObject property : object.getProperties().values()) {
            storeObject(property, support, indexable);
        }
    }
   
    
    
    public static final class Factory extends EmbeddingIndexerFactory {

        public static final String NAME = "js"; // NOI18N
        public static final int VERSION = 2;

        @Override
        public EmbeddingIndexer createIndexer(final Indexable indexable, final Snapshot snapshot) {
            if (isIndexable(indexable, snapshot)) {
                return new JsIndexer();
            } else {
                return null;
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

        private boolean isIndexable(Indexable indexable, Snapshot snapshot) {
            // Cannot call file.getFileObject().getMIMEType() here for several reasons:
            // (1) when cleaning up the index for deleted files, file.getFileObject().getMIMEType()
            //   may return "content/unknown", and in some cases, file.getFileObject() returns null
            // (2) file.getFileObject() can be expensive during startup indexing when we're
            //   rapidly scanning through lots of directories to determine which files are
            //   indexable. This is done using the java.io.File API rather than the more heavyweight
            //   FileObject, and each file.getFileObject() will perform a FileUtil.toFileObject() call.
            // Since the mime resolver for PHP is simple -- it's just based on the file extension,
            // we perform the same check here:
            //if (PHPLanguage.PHP_MIME_TYPE.equals(file.getFileObject().getMIMEType())) { // NOI18N

            FileObject fileObject = snapshot.getSource().getFileObject();

            if (INDEXABLE_EXTENSIONS.contains(fileObject.getExt().toLowerCase())) {
                return true;
            }

            return false;
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            try {
                IndexingSupport is = IndexingSupport.getInstance(context);
                for (Indexable i : deleted) {
                    is.removeDocuments(i);
                }
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
            }
        }

        @Override
        public void rootsRemoved(final Iterable<? extends URL> removedRoots) {
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
            try {
                IndexingSupport is = IndexingSupport.getInstance(context);
                for (Indexable i : dirty) {
                    is.markDirtyDocuments(i);
                }
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
            }
        }
    } // End of Factory class
}
