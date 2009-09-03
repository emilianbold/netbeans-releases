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
import java.net.URL;
import java.util.Collection;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexer;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.netbeans.modules.web.jsf.editor.tld.TldLibrary;
import org.netbeans.modules.web.jsf.editor.tld.TldLibraryGlobalCache;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.xml.sax.InputSource;

/**
 *
 * @author marekfukala
 */
public class JsfBinaryIndexer extends BinaryIndexer {

    private static final String INDEXER_NAME = "JSFBinaryIndexer";
    private static final int INDEX_VERSION = 1;

    @Override
    protected void index(Context context) {
//        System.out.println("JsfBinaryIndexer: scanning " + context.getRoot());

        processTlds(context);
    }


    private void processTlds(Context context) {
        FileObject root = context.getRoot();
        //find all TLDs in the jar file
        Collection<FileObject> tldDescriptors = TldLibraryGlobalCache.findLibraryDescriptors(root);
        for(FileObject file : tldDescriptors) {
            try {
                String namespace = TldLibrary.parseNamespace(file.getInputStream());
                if(namespace != null) {
//                    IndexingSupport.getInstance(context).createDocument()
                }
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

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
