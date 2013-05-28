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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.prep;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author mfukala@netbeans.org
 */
public class CPCustomIndexer extends CustomIndexer {

    public static final Logger LOGGER = Logger.getLogger(CPCustomIndexer.class.getSimpleName());
    
    static final String INDEXER_NAME = "cpCustomIndexer"; //NOI18N
    static final int INDEXER_VERSION = 1;
    static final String CP_TYPE_KEY = "cpType"; //NOI18N
    
    private static final Map<String, CssPreprocessorType> mime2type;
    static {
        mime2type = new HashMap<String, CssPreprocessorType>();
        for(CssPreprocessorType type : CssPreprocessorType.values()) {
            for(String mime : type.getMimeTypes()) {
                mime2type.put(mime, type);
            }
        }
    }
    
    @Override
    protected void index(Iterable<? extends Indexable> files, Context context) {
        for (Indexable i : files) {
            URL indexableURL = i.getURL();
            if (indexableURL == null) {
                continue;
            }
            FileObject file = URLMapper.findFileObject(indexableURL);
            if (file == null) {
                continue;
            }

            String fileMimeType = file.getMIMEType();
            CssPreprocessorType type = mime2type.get(fileMimeType);
            if (type != null) {
                try {
                    IndexingSupport sup = IndexingSupport.getInstance(context);
                    IndexDocument doc = sup.createDocument(file);
                    doc.addPair(CP_TYPE_KEY, type.name(), true, true);
                    sup.addDocument(doc);
                    LOGGER.log(Level.FINE, "File {0} marked as CSS preprocessor file with {1} mimetype.", new Object[]{file.getNameExt(), fileMimeType});
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

//    @MimeRegistrations({
//        @MimeRegistration(mimeType = "text/less", service = CustomIndexerFactory.class),
//        @MimeRegistration(mimeType = "text/scss", service = CustomIndexerFactory.class),
//        @MimeRegistration(mimeType = "text/sass", service = CustomIndexerFactory.class)
//    })
    @MimeRegistration(mimeType = "", service = CustomIndexerFactory.class)
    public static class Factory extends CustomIndexerFactory {

        @Override
        public CustomIndexer createIndexer() {
            return new CPCustomIndexer();
        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return false;
        }

        @Override
        public boolean scanStarted(Context context) {
            try {
                return IndexingSupport.getInstance(context).isValid();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return false;
            }
        }

        @Override
        public void scanFinished(Context context) {
            super.scanFinished(context);
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
            return INDEXER_NAME;
        }

        @Override
        public int getIndexVersion() {
            return INDEXER_VERSION;
        }
    }
}
