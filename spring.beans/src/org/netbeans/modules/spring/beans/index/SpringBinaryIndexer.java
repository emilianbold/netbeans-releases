/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.spring.beans.index;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexer;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.netbeans.modules.spring.api.SpringUtilities;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.JarFileSystem;
import org.openide.util.Exceptions;

/**
 *
 * @author alexeybutenko
 */

public class SpringBinaryIndexer extends BinaryIndexer {

    private static final Logger LOGGER = Logger.getLogger(SpringBinaryIndexer.class.getSimpleName());

    static final String INDEXER_NAME = "SpringBinary"; //NOI18N
    static final int INDEX_VERSION = 1;
    private static final String XSD_SUFFIX = ".xsd";    //NOI18N
    static final String LIBRARY_MARK_KEY = "xsdSpringSchema";   //NOI18N
    static final String NAMESPACE_MARK_KEY = "namespace";   //NOI18N


    private String version;

    @Override
    protected void index(Context context) {
        LOGGER.log(Level.FINE, "indexing " + context.getRoot()); //NOI18N

        if (context.getRoot() == null) {
            return;
        }
        version = findVersion(context.getRoot());
        if (version !=null) {
            processXsds(context);
        }
    }

    private void processXsds(Context context) {
        FileObject root = context.getRoot();
        for (FileObject fileObject : findSpringLibraryDescriptors(root, XSD_SUFFIX)) {
            try {
                ModelSource source = Utilities.getModelSource(fileObject, true);
                SchemaModel model = SchemaModelFactory.getDefault().getModel(source);


                Schema schema = model.getSchema();
                String targetNamespace = schema.getTargetNamespace();
                if (targetNamespace !=null) {
                    IndexingSupport sup = IndexingSupport.getInstance(context);
                    IndexDocument doc = sup.createDocument(fileObject);
                    doc.addPair(NAMESPACE_MARK_KEY, targetNamespace, true, true);
                    doc.addPair(LIBRARY_MARK_KEY, Boolean.TRUE.toString(), true, true);
                    sup.addDocument(doc);

                    LOGGER.log(Level.INFO, "The file " + fileObject + " indexed as a XSD (namespace=" + targetNamespace + ")"); //NOI18N

                }
            }catch(Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private Collection<FileObject> findSpringLibraryDescriptors(FileObject classpathRoot, String suffix) {
        Collection<FileObject> files = new ArrayList<FileObject>();
        if (classpathRoot.getFileObject("org/springframework") == null) {   //NOI18N
            return files;
        }
        Enumeration<? extends FileObject> fos = classpathRoot.getChildren(true); //scan all files in the jar
        while (fos.hasMoreElements()) {
            FileObject file = fos.nextElement();
            //XXX Version??? spring 2-5 has some xsd's with non 2.5 version
            String fileName = file.getNameExt().toLowerCase();
            if (fileName !=null && fileName.endsWith(suffix) && version.startsWith(findXsdVersion(file))) { //NOI18N
                //found library, create a new instance and cache it
                files.add(file);
            }
        }
        return files;
    }

    private String findXsdVersion(FileObject file) {
        String v = file.getName();
        v = v.substring(v.lastIndexOf("-")+1);
        return v;
    }

    private String findVersion(FileObject classpathRoot) {
        ClassPath cp = ClassPath.getClassPath(classpathRoot, ClassPath.COMPILE);
        String classRelativePath = SpringUtilities.SPRING_CLASS_NAME.replace('.', '/') + ".class"; //NOI18N
        try {
            FileObject resource = cp.findResource(classRelativePath);  //NOI18N
            if (resource==null) {
                return null;
            }
            FileObject ownerRoot = cp.findOwnerRoot(resource);

            if (ownerRoot !=null) { //NOI18N
                if (ownerRoot.getFileSystem() instanceof JarFileSystem) {
                    JarFileSystem jarFileSystem = (JarFileSystem) ownerRoot.getFileSystem();
                    return SpringUtilities.getImplementationVersion(jarFileSystem);
                }
            }
        } catch (FileStateInvalidException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }

    public static class Factory extends BinaryIndexerFactory {

        @Override
        public BinaryIndexer createIndexer() {
            return new SpringBinaryIndexer();
        }

        @Override
        public void rootsRemoved(Iterable<? extends URL> removedRoots) {
//            throw new UnsupportedOperationException("Not supported yet.");
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
