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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor.index;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.netbeans.modules.web.jsf.editor.JsfUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Looks for composite component libraries on source classpath.
 *
 * @todo possibly join with JsfCustomIndexer and use explicit parse on .xhtml files like JsfBinaryIndexer does
 *
 * @author mfukala@netbeans.org
 */
public class JsfIndexer extends EmbeddingIndexer {

    public static final Logger LOG = Logger.getLogger(JsfIndexer.class.getName());

    @Override
    protected void index(Indexable indexable, Result parserResult, Context context) {
        try {
            FileObject fo = parserResult.getSnapshot().getSource().getFileObject();
            List<IndexDocument> documents = new LinkedList<IndexDocument>();
            IndexingSupport support = IndexingSupport.getInstance(context);

            //get JSF models and index them
            Collection<JsfPageModel> models = JsfPageModelFactory.getModels((HtmlParserResult) parserResult);
            for (JsfPageModel model : models) {
                IndexDocument document = support.createDocument(indexable);
                model.storeToIndex(document);
                documents.add(document);
            }
            LOG.log(Level.FINE, "indexing {0}, found {1} document.", new Object[]{fo.getPath(), documents.size()}); //NOI18N


            //add the documents to the index
            for (IndexDocument d : documents) {
                support.addDocument(d);
            }

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static class Factory extends EmbeddingIndexerFactory {

        static final String NAME = "jsf"; //NOI18N
        static final int VERSION = 4;

        @Override
        public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
            if (isIndexable(snapshot)) {
                return new JsfIndexer();
            } else {
                return null;
            }
        }

	@Override
	public boolean scanStarted(Context context) {
            try {
                LOG.log(Level.FINE, "scanning of {0} started", context.getRoot()); //NOI18N
                return IndexingSupport.getInstance(context).isValid();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return false;
            }
	}

	@Override
	public void scanFinished(Context context) {
	    super.scanFinished(context);
            LOG.log(Level.FINE, "scanning of {0} finished", context.getRoot()); //NOI18N
	}
	
        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            //no-op
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
            //no-op
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
            //index all text/xhtml files, not only in web projects since there might be java library with facelets components
            FileObject fo = snapshot.getSource().getFileObject();
            String sourceFileMimeType = fo.getMIMEType();
            return JsfUtils.XHTML_MIMETYPE.equals(sourceFileMimeType);
        }
        
    } //end of Factory class
}
