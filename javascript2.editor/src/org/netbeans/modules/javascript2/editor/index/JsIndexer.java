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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.javascript2.editor.api.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.model.JsFunction;
import org.netbeans.modules.javascript2.editor.model.JsObject;
import org.netbeans.modules.javascript2.editor.model.Model;
import org.netbeans.modules.javascript2.editor.model.TypeUsage;
import org.netbeans.modules.javascript2.editor.model.impl.ModelUtils;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.util.Parameters;

/**
 *
 * @author Petr Pisl
 */
public class JsIndexer extends EmbeddingIndexer {

    private static final Logger LOG = Logger.getLogger(JsIndexer.class.getName());

    private static final Collection<String> INDEXABLE_EXTENSIONS = Arrays.asList("js", "sdoc", "html");

    @Override
    protected void index(Indexable indexable, Result result, Context context) {
        LOG.log(Level.FINE, "Indexing: {0}, fullPath: {1}", new Object[]{indexable.getRelativePath(), result.getSnapshot().getSource().getFileObject().getPath()});
        
        if (!(result instanceof JsParserResult)) {
            return;
        }
        
        if (!context.checkForEditorModifications()) {
            JsIndex.changeInIndex();
        }
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
            if (object.getParent() != null) {
                storeObject(object, object.getName(), support, indexable);
            }
        }
    }

    private void storeObject(JsObject object, String fqn, IndexingSupport support, Indexable indexable) {
        if (!isInvisibleFunction(object)) {
            if (object.isDeclared() || ModelUtils.PROTOTYPE.equals(object.getName())) {
                // if it's delcared, then store in the index as new document.
                IndexDocument document = IndexedElement.createDocument(object, fqn, support, indexable);
                support.addDocument(document);
            }
            // look for all other properties. Even if the object doesn't have to be delcared in the file
            // there can be declared it's properties or methods
            for (JsObject property : object.getProperties().values()) {
                storeObject(property, fqn + '.' + property.getName(), support, indexable);
            }
            if (object instanceof JsFunction) {
                // store parameters
                for (JsObject parameter : ((JsFunction)object).getParameters()) {
                    storeObject(parameter, fqn + '.' + parameter.getName(), support, indexable);
                }
            }
        }
    }
    
    private boolean isInvisibleFunction(JsObject object) {
        if (object.getJSKind().isFunction() && (object.isAnonymous() || object.getModifiers().contains(Modifier.PRIVATE))) {
            Collection<? extends TypeUsage> returnTypes = ((JsFunction) object).getReturnTypes();
            if (returnTypes.size() == 1 && (returnTypes.iterator().next()).getType().equals("undefined")) {
                return true;
            }
        }
        return false;
    }
    
    public static final class Factory extends EmbeddingIndexerFactory {

        public static final String NAME = "js"; // NOI18N
        public static final int VERSION = 11;

        private static final ThreadLocal<Collection<Runnable>> postScanTasks = new ThreadLocal<Collection<Runnable>>();

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
            return JsTokenId.JAVASCRIPT_MIME_TYPE.equals(snapshot.getMimeType());
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

        @Override
        public boolean scanStarted(Context context) {
            postScanTasks.set(new LinkedList<Runnable>());
            return super.scanStarted(context);
        }

        @Override
        public void scanFinished(Context context) {
            try {
                for (Runnable task : postScanTasks.get()) {
                    task.run();
                }
            } finally {
                postScanTasks.remove();
                super.scanFinished(context);
            }
        }

        public static boolean isScannerThread() {
            return postScanTasks.get() != null;
        }

        public static void addPostScanTask(@NonNull final Runnable task) {
            Parameters.notNull("task", task);   //NOI18N
            final Collection<Runnable> tasks = postScanTasks.get();
            if (tasks == null) {
                throw new IllegalStateException("JsIndexer.postScanTask can be called only from scanner thread.");  //NOI18N
            }                        
            tasks.add(task);
        }

    } // End of Factory class
}
