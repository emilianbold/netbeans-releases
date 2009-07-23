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

package org.netbeans.modules.java.source.indexing;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.java.source.parsing.CachingArchiveProvider;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.usages.BinaryAnalyser;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
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
                            //todo: may also need interruption.
                            try {
                                BinaryAnalyser.Result finished = ba.start(context.getRootURI(), new AtomicBoolean(false), new AtomicBoolean(false));
                                while (finished == BinaryAnalyser.Result.CANCELED) {
                                    finished = ba.resume();
                                }
                            } finally {
                                ba.finish();
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
    }
}
