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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexer;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibraryDescriptor;
import org.netbeans.modules.web.jsf.editor.tld.TldLibrary;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author marekfukala
 */
public class JsfBinaryIndexer extends BinaryIndexer {

    static final String INDEXER_NAME = "jsfBinary"; //NOI18N
    static final int INDEX_VERSION = 5;
    static final String TLD_LIBRARY_MARK_KEY = "tagLibraryDescriptor"; //NOI18N
    static final String FACELETS_LIBRARY_MARK_KEY = "faceletsLibraryDescriptor"; //NOI18N
    static final String LIBRARY_NAMESPACE_KEY = "namespace"; //NOI18N

    private static final String TLD_LIB_SUFFIX = ".tld"; //NOI18N
    private static final String FACELETS_LIB_SUFFIX = ".taglib.xml"; //NOI18N

    private static final Logger LOGGER = Logger.getLogger(JsfBinaryIndexer.class.getSimpleName());

    @Override
    protected void index(Context context) {
        LOGGER.log(Level.FINE, "indexing " + context.getRoot()); //NOI18N

        if (context.getRoot() == null) {
            return;
        }

        processTlds(context);

        processFaceletsLibraryDescriptors(context);

        processFaceletsCompositeLibraries(context);

    }

    private void processTlds(Context context) {
        FileObject root = context.getRoot();
        //find all TLDs in the jar file
        for (FileObject file : findLibraryDescriptors(root, TLD_LIB_SUFFIX)) {
            try {
                String namespace = TldLibrary.parseNamespace(file.getInputStream());
                if (namespace != null) {
                    IndexingSupport sup = IndexingSupport.getInstance(context);
                    IndexDocument doc = sup.createDocument(file);
                    doc.addPair(LIBRARY_NAMESPACE_KEY, namespace, true, true);
                    doc.addPair(TLD_LIBRARY_MARK_KEY, Boolean.TRUE.toString(), true, true);
                    sup.addDocument(doc);

                    LOGGER.log(Level.FINE, "The file " + file + " indexed as a TLD (namespace=" + namespace + ")"); //NOI18N
                }
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

    private void processFaceletsLibraryDescriptors(Context context) {
        FileObject root = context.getRoot();
        for (FileObject file : findLibraryDescriptors(root, FACELETS_LIB_SUFFIX)) {
            try {
                String namespace = FaceletsLibraryDescriptor.parseNamespace(file.getInputStream());
                if(namespace != null) {
                    IndexingSupport sup = IndexingSupport.getInstance(context);
                    IndexDocument doc = sup.createDocument(file);
                    doc.addPair(LIBRARY_NAMESPACE_KEY, namespace, true, true);
                    doc.addPair(FACELETS_LIBRARY_MARK_KEY, Boolean.TRUE.toString(), true, true);
                    sup.addDocument(doc);

                    LOGGER.log(Level.FINE, "The file " + file + " indexed as a Facelets Library Descriptor"); //NOI18N
                }
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

    private void processFaceletsCompositeLibraries(Context context) {
        //look for /META-INF/resources/<folder>/*.xhtml
        //...and index as normal composite library
        FileObject resourcesFolder = context.getRoot().getFileObject("META-INF/resources"); //NOI18N //can it be stored in META-INF.* ????
        if(resourcesFolder != null) {
            LOGGER.log(Level.FINE, "Composite Libraries Scan: META-INF/resources folder found"); //NOI18N
            try {
                Enumeration<? extends FileObject> folders = resourcesFolder.getFolders(false);
                final IndexingSupport sup = IndexingSupport.getInstance(context);
                final JsfPageModelFactory compositeComponentModelFactory = JsfPageModelFactory.getFactory(CompositeComponentModel.Factory.class);
                while (folders.hasMoreElements()) {
                    FileObject folder = folders.nextElement();
                    //process all xhtml files
                    for (final FileObject file : folder.getChildren()) {
                        if (file.getExt().equalsIgnoreCase("xhtml")) {
                            //NOI18N
                            //parse && index the html content of the file
                            LOGGER.log(Level.FINE, "Composite Libraries Scan: found " + file); //NOI18N
                            Source source = Source.create(file);
                            try {
                                ParserManager.parse(Collections.singleton(source), new UserTask() {
                                    @Override
                                    public void run(ResultIterator resultIterator) throws Exception {
                                        for (Embedding e : resultIterator.getEmbeddings()) {
                                            if (e.getMimeType().equals("text/html")) {
                                                //NOI18N
                                                HtmlParserResult result = (HtmlParserResult) resultIterator.getResultIterator(e).getParserResult();
                                                CompositeComponentModel ccmodel = (CompositeComponentModel) compositeComponentModelFactory.getModel(result);
                                                if (ccmodel != null) {
                                                    //looks like a composite component
                                                    IndexDocument doc = sup.createDocument(file);
                                                    ccmodel.storeToIndex(doc);
                                                    sup.addDocument(doc);

                                                    LOGGER.log(Level.FINE, "Composite Libraries Scan: Model created for file " + file); //NOI18N
                                                }
                                            }
                                        }
                                    }
                                });
                            } catch (ParseException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

    public static Collection<FileObject> findLibraryDescriptors(FileObject classpathRoot, String suffix) {
        Collection<FileObject> files = new ArrayList<FileObject>();
        Enumeration<? extends FileObject> fos = classpathRoot.getChildren(true); //scan all files in the jar
        while (fos.hasMoreElements()) {
            FileObject file = fos.nextElement();
            if (file.getNameExt().toLowerCase(Locale.US).endsWith(suffix)) { //NOI18N
                //found library, create a new instance and cache it
                files.add(file);
            }
        }
        return files;
    }

    public static class Factory extends BinaryIndexerFactory {

        @Override
        public BinaryIndexer createIndexer() {
            return new JsfBinaryIndexer();
        }

        @Override
        public void rootsRemoved(Iterable<? extends URL> removedRoots) {
//            System.out.println("JsfBinaryIndexer: roots removed");
        }

        @Override
        public String getIndexerName() {
            return INDEXER_NAME;
        }

        @Override
        public int getIndexVersion() {
            return INDEX_VERSION;
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
    }
}
