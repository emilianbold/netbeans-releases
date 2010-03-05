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

package org.netbeans.modules.j2ee.persistence.indexing;

import java.io.IOException;
import java.util.Date;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.api.PersistenceLocation;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public class CopyResourcesIndexer extends CustomIndexer {

    private static final String NAME = "CopyResourcesIndexer";  //NOI18N
    private static final int VERSION = 1;
    private static final String MIME_JAVA = "text/x-java";  //NOI18N
    private static final String JAVA_NAME = "java"; //NOI18N
    private static final String PATH_TEMPLATE = "%s/%d/classes/META-INF";    //NOI18N

    private final Factory factory;

    private CopyResourcesIndexer(final Factory factory) {
        this.factory = factory;
    }

    @Override
    protected void index(Iterable<? extends Indexable> files, Context context) {
        final FileObject root = context.getRoot();
        if (root != null) {
            final Project owner = FileOwnerQuery.getOwner(root);
            if (owner != null) {
                FileObject persistenceXmlLocation = PersistenceLocation.getLocation(owner);
                if( persistenceXmlLocation!=null ) {
                    final FileObject persistenceXML = persistenceXmlLocation.getFileObject("persistence.xml");//NOI18N
                    if (persistenceXML != null) {
                        final Date cts = persistenceXML.lastModified();
                        synchronized (factory) {
                            if (cts.equals(factory.timestamp)) {
                                //Nothing changed.
                                return;
                            }
                            factory.timestamp = cts;
                        }
                        try {
                            final String path = getCachePath();
                            if (path != null) {
                                final FileObject cacheRoot = context.getIndexFolder().getParent().getParent();
                                final FileObject cacheDir = FileUtil.createFolder(cacheRoot,path);
                                if (cacheDir != null) {
                                    final FileObject toDelete = cacheDir.getFileObject(persistenceXML.getName(), persistenceXML.getExt());
                                    if (toDelete != null) {
                                        toDelete.delete();
                                    }
                                    FileUtil.copyFile(persistenceXML, cacheDir, persistenceXML.getName());
                                }
                            }
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        }
    }

    private String getCachePath() {
        String path = factory.cachedPath;
        if (path != null) {
            return path;
        }
        CustomIndexerFactory jif = null;
        final Iterable<? extends CustomIndexerFactory> factories = MimeLookup.getLookup(MIME_JAVA).lookupAll(CustomIndexerFactory.class);
        for (CustomIndexerFactory fact : factories) {
            if (JAVA_NAME.equals(fact.getIndexerName())) {
                jif = fact;
                break;
            }
        }
        if (jif == null) {
            return null;
        }
        synchronized (factory) {
            factory.cachedPath = String.format(PATH_TEMPLATE, jif.getIndexerName(), jif.getIndexVersion()); //NOI18N
            return factory.cachedPath;
        }
    }

    public static class Factory extends CustomIndexerFactory {

        private volatile String cachedPath;
        private Date timestamp;

        @Override
        public CustomIndexer createIndexer() {
            return new CopyResourcesIndexer(this);
        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return true;
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            //pass
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
            //pass
        }

        @Override
        public String getIndexerName() {
            return NAME;
        }

        @Override
        public int getIndexVersion() {
            return VERSION;
        }
    }
}
