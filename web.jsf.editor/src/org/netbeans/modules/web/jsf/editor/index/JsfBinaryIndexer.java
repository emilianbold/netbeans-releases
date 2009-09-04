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
package org.netbeans.modules.web.jsf.editor.index;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexer;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.netbeans.modules.web.jsf.editor.tld.TldLibrary;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author marekfukala
 */
public class JsfBinaryIndexer extends BinaryIndexer {

    static final String INDEXER_NAME = "jsfBinary"; //NOI18N
    static final int INDEX_VERSION = 3;
    static final String LIB_NAMESPACE_KEY = "namespace"; //NOI18N
    static final String LIB_FACELETS_KEY = "faceletsLibrary"; //NOI18N
    private static final String TLD_LIB_SUFFIX = ".tld"; //NOI18N
    private static final String FACELETS_LIB_SUFFIX = ".taglib.xml"; //NOI18N

    @Override
    protected void index(Context context) {
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
                    doc.addPair(LIB_NAMESPACE_KEY, namespace, true, true);
                    sup.addDocument(doc);
                }
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

    //just store a marker "faceletsLibrary=true" if facelets library descriptor
    private void processFaceletsLibraryDescriptors(Context context) {
        FileObject root = context.getRoot();
        for (FileObject file : findLibraryDescriptors(root, FACELETS_LIB_SUFFIX)) {
            try {
                IndexingSupport sup = IndexingSupport.getInstance(context);
                IndexDocument doc = sup.createDocument(file);
                doc.addPair(LIB_FACELETS_KEY, Boolean.TRUE.toString(), true, true);
                sup.addDocument(doc);
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
    }

    private Collection<FileObject> findLibraryDescriptors(FileObject classpathRoot, String suffix) {
        Collection<FileObject> files = new ArrayList<FileObject>();
        Enumeration<? extends FileObject> fos = classpathRoot.getFolders(false);
        while (fos.hasMoreElements()) {
            FileObject fo = fos.nextElement();
            if ("META-INF".equals(fo.getName())) { //NOI18N
                Enumeration<? extends FileObject> children = fo.getChildren(true); //get children recursively
                while(children.hasMoreElements()) {
                    FileObject file = children.nextElement();
                    if (file.getNameExt().toLowerCase(Locale.US).endsWith(suffix)) { //NOI18N
                        //found library, create a new instance and cache it
                        files.add(file);
                    }
                }
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
    }
}
