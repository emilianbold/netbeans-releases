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

package org.netbeans.modules.java.source.indexing;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.source.parsing.CachingArchiveProvider;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.usages.BinaryAnalyser;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.parsing.impl.indexing.friendapi.IndexingController;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexer;
import org.netbeans.modules.parsing.spi.indexing.BinaryIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
public class JavaBinaryIndexer extends BinaryIndexer {

    static final Logger LOG = Logger.getLogger(JavaBinaryIndexer.class.getName());

    @Override
    protected void index(final Context context) {
        LOG.log(Level.FINE, "index({0})", context.getRootURI());
        try {
            final ClassIndexManager cim = ClassIndexManager.getDefault();
            cim.prepareWriteLock(new ClassIndexManager.ExceptionAction<Void>() {
                public Void run() throws IOException, InterruptedException {
                    CachingArchiveProvider.getDefault().clearArchive(context.getRootURI());
                    File cacheFolder = JavaIndex.getClassFolder(context.getRootURI());
                    FileObjects.deleteRecursively(cacheFolder);
                    ClassIndexImpl uq = cim.createUsagesQuery(context.getRootURI(), false);
                    if (uq == null) {
                        return null; //IDE is exiting, indeces are already closed.
                    }
                    if (context.isAllFilesIndexing()) {
                        final BinaryAnalyser ba = uq.getBinaryAnalyser();
                        if (ba != null) { //ba == null => IDE is exiting, indexing will be done on IDE restart
                            BinaryAnalyser.Result finished = null;
                            try {
                                finished = ba.start(context);
                                while (finished == BinaryAnalyser.Result.CANCELED) {
                                    finished = ba.resume();
                                }
                            } finally {
                                if (finished == BinaryAnalyser.Result.FINISHED) {
                                    final BinaryAnalyser.Changes changes = ba.finish();
                                    final Map<URL, List<URL>> binDeps = IndexingController.getDefault().getBinaryRootDependencies();
                                    final Map<URL, List<URL>> srcDeps = IndexingController.getDefault().getRootDependencies();
                                    final List<ElementHandle<TypeElement>> changed = new ArrayList<ElementHandle<TypeElement>>(changes.changed.size()+changes.removed.size());
                                    changed.addAll(changes.changed);
                                    changed.addAll(changes.removed);
                                    final Map<URL,Set<URL>> toRebuild = JavaCustomIndexer.findDependent(context.getRootURI(), srcDeps, binDeps, changed, !changes.added.isEmpty(), false);
                                    for (Map.Entry<URL, Set<URL>> entry : toRebuild.entrySet()) {
                                        context.addSupplementaryFiles(entry.getKey(), entry.getValue());
                                    }
                                }
                            }
                        }
                    }
                    return null;
                }
            });
        } catch (IllegalArgumentException iae) {
            Exceptions.printStackTrace(iae);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        } catch (InterruptedException ie) {
            Exceptions.printStackTrace(ie);
        }
    }

    public static class Factory extends BinaryIndexerFactory {

        @Override
        public BinaryIndexer createIndexer() {
            return new JavaBinaryIndexer();
        }

        @Override
        public String getIndexerName() {
            return JavaIndex.NAME;
        }

        @Override
        public int getIndexVersion() {
            return JavaIndex.VERSION;
        }

        @Override
        public void rootsRemoved (final Iterable<? extends URL> removedRoots) {
            assert removedRoots != null;
            final ClassIndexManager cim = ClassIndexManager.getDefault();
            try {
                cim.prepareWriteLock(new ClassIndexManager.ExceptionAction<Void>() {
                    public Void run() throws IOException, InterruptedException {
                        //todo:
                        for (URL removedRoot : removedRoots) {
                            cim.removeRoot(removedRoot);
                        }
                        return null;
                    }
                });
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            } catch (InterruptedException e) {
                Exceptions.printStackTrace(e);
            }
        }

        @Override
        public boolean scanStarted(final Context context) {
            try {
                return ClassIndexManager.getDefault().prepareWriteLock(new ClassIndexManager.ExceptionAction<Boolean>() {
                    public Boolean run() throws IOException, InterruptedException {
                        return ClassIndexManager.getDefault().takeWriteLock(new ClassIndexManager.ExceptionAction<Boolean>() {
                            public Boolean run() throws IOException, InterruptedException {
                                final ClassIndexImpl uq = ClassIndexManager.getDefault().createUsagesQuery(context.getRootURI(), true);
                                if (uq == null) {
                                    //Closing...
                                    return true;
                                }
                                if (uq.getState() != ClassIndexImpl.State.NEW) {
                                    //Already checked
                                    return true;
                                }
                                try {
                                    return uq.getBinaryAnalyser().isValid();
                                } finally {
                                    uq.setState(ClassIndexImpl.State.INITIALIZED);
                                }
                            }
                        });
                    }
                });
            } catch (IOException ioe) {
                JavaIndex.LOG.log(Level.WARNING, "Exception while checking cache validity for root: "+context.getRootURI(), ioe); //NOI18N
                return false;
            } catch (InterruptedException ie) {
                JavaIndex.LOG.log(Level.WARNING, "Exception while checking cache validity for root: "+context.getRootURI(), ie); //NOI18N
                return false;
            }
        }
    }
}
