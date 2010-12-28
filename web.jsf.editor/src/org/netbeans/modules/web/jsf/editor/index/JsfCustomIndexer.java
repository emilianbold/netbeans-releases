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
package org.netbeans.modules.web.jsf.editor.index;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.netbeans.modules.web.jsf.editor.JsfSupportImpl;
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibraryDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author marekfukala
 */
public class JsfCustomIndexer extends CustomIndexer {

    static final String INDEXER_NAME = "jsfCustomIndexer"; //NOI18N
    static final int INDEXER_VERSION = 1;

    public static final Logger LOGGER = Logger.getLogger(JsfCustomIndexer.class.getSimpleName());

    private AtomicBoolean changeFlag;

    public JsfCustomIndexer(AtomicBoolean changeFlag) {
        this.changeFlag = changeFlag;
    }


//    JSOU TU DVA PROBLEMY:
//        1) JsfIndex.getAllFaceletsLibraryDescriptors() vrati FO pro kazdy descriptor,
//           ale jednou se ten descriptor najde pres Custom indexer a jednou pres Binary,
//           pokud je zbuildovano. Je potreba zajistit, aby se library delal z toho custom
//           pokud jsou oba
//
//        2) poslouchani na zmenach knihoven - je treba dodelat do FaceletsLibrarySupport
//           mapu Library - timestamp source file a pokazdy kdyz se neco preindexuje, tak
//           to checkovat, aby se spravne projevily zmeny pri modifikacich descriptoru
//           

    @Override
    protected void index(Iterable<? extends Indexable> files, Context context) {
        for (Indexable i : files) {
            FileObject file = URLMapper.findFileObject(i.getURL());
            if (file != null) {
                if (JsfIndexSupport.isFaceletsLibraryDescriptor(file)) {
                    LOGGER.log(Level.FINE, "indexing {0}", file); //NOI18N

                    try {
                        String namespace = FaceletsLibraryDescriptor.parseNamespace(file.getInputStream());
                        if(namespace != null) {
                            IndexingSupport sup = IndexingSupport.getInstance(context);
                            IndexDocument doc = sup.createDocument(file);
                            doc.addPair(JsfBinaryIndexer.LIBRARY_NAMESPACE_KEY, namespace, true, true);
                            doc.addPair(JsfBinaryIndexer.FACELETS_LIBRARY_MARK_KEY, Boolean.TRUE.toString(), true, true);
                            sup.addDocument(doc);

                            changeFlag.set(true);

                            LOGGER.log(Level.FINE, "The file {0} indexed as a Facelets Library Descriptor", file); //NOI18N
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }

    public static class Factory extends CustomIndexerFactory {

        private AtomicBoolean changeFlag = new AtomicBoolean(false);

        @Override
        public CustomIndexer createIndexer() {
            return new JsfCustomIndexer(changeFlag);
        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return false;
        }

        @Override
	public boolean scanStarted(Context context) {
            try {
                this.changeFlag.set(false);
                return IndexingSupport.getInstance(context).isValid();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return false;
            }
	}

        @Override
	public void scanFinished(Context context) {
	    super.scanFinished(context);

            if(changeFlag.get()) {
                //there has been a change to a library, lets regenerate the descriptor models
                //TODO: do this selectively per changed library, so far the FaceletsLibrarySupport
                //doesn't support this.
                if(context.getRoot() != null) {
                    JsfSupportImpl jsfsupport = JsfSupportImpl.findFor(context.getRoot());
                    if(jsfsupport != null) {
                        jsfsupport.getFaceletsLibrarySupport().invalidateLibrariesCache();
                    }
                }
            }
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
