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

package org.netbeans.modules.parsing.impl.indexing;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.RequestProcessor;

/**
 *
 * @author vita
 */
public class RepositoryUpdater2Test extends NbTestCase {

    public RepositoryUpdater2Test(String name) {
        super(name);
    }

    private FileObject workDir = null;
    private RepositoryUpdaterTest.TestHandler ruSync;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        this.clearWorkDir();
        final File _wd = this.getWorkDir();
        workDir = FileUtil.toFileObject(_wd);

        FileObject cache = workDir.createFolder("cache");
        CacheFolder.setCacheFolder(cache);

        ruSync = new RepositoryUpdaterTest.TestHandler();
        final Logger logger = Logger.getLogger(RepositoryUpdater.class.getName() + ".tests");
        logger.setLevel(Level.FINEST);
        logger.addHandler(ruSync);

        RepositoryUpdaterTest.waitForRepositoryUpdaterInit();
    }

    public void testAddIndexingJob() throws Exception {
        FileUtil.setMIMEType("txt", "text/plain");
        final FileObject srcRoot1 = workDir.createFolder("src1");
        final FileObject file1 = srcRoot1.createData("file1.txt");

        final FileObject srcRoot2 = workDir.createFolder("src2");
        final FileObject file2 = srcRoot2.createData("file2.txt");

        final testAddIndexingJob_CustomIndexer indexer = new testAddIndexingJob_CustomIndexer(srcRoot2.getURL(), file2.getURL());
        final CustomIndexerFactory factory = new CustomIndexerFactory() {
            public @Override CustomIndexer createIndexer() {
                return indexer;
            }

            public @Override void filesDeleted(Collection<? extends Indexable> deleted, Context context) {
            }

            public @Override void filesDirty(Collection<? extends Indexable> dirty, Context context) {
            }

            public @Override String getIndexerName() {
                return indexer.getClass().getName();
            }

            public @Override boolean supportsEmbeddedIndexers() {
                return true;
            }

            public @Override int getIndexVersion() {
                return 0;
            }
        };
        MockServices.setServices(testAddIndexingJob_PathRecognizer.class);
        MockMimeLookup.setInstances(MimePath.parse("text/plain"), factory);
        Util.allMimeTypes = Collections.singleton("text/plain");

        ruSync.reset(RepositoryUpdaterTest.TestHandler.Type.FILELIST, 2);
        RepositoryUpdater.getDefault().addIndexingJob(srcRoot1.getURL(), Collections.singleton(file1.getURL()), false, false);
        ruSync.await();

        assertEquals("Wrong number of ordinary scans", 1, indexer.cnt);
        assertEquals("Wrong ordinary root", srcRoot1, indexer.indexedRoot);
        assertEquals("Wrong ordinary files", Collections.singleton(file1), indexer.indexedFiles);
        assertEquals("Wrong number of supplementary scans", 1, indexer.cntSupplementary);
        assertEquals("Wrong supplementary root", srcRoot2, indexer.indexedSupplementaryRoot);
        assertEquals("Wrong supplementary files", Collections.singleton(file2), indexer.indexedSupplementaryFiles);
    }

    protected final void assertEquals(String message, Iterable<?> expected, Iterable<?> actual) {
        assertNotNull(expected);
        
        if (actual == null) {
            assertEquals(message, expected, null);
        }

        Iterator<? extends Object> expectedI = expected.iterator();
        Iterator<? extends Object> actualI = actual.iterator();
        boolean failed = false;
        
        for( ; actualI.hasNext() ; ) {
            if (expectedI.hasNext()) {
                Object expectedO = expectedI.next();
                Object actualO = actualI.next();

                try {
                    assertEquals(expectedO, actualO);
                } catch (AssertionError e) {
                    failed = true;
                    break;
                }
            } else {
                failed = true;
                break;
            }
        }

        if (failed) {
            fail(message + "Expected <" + expected + ">, but was: <" + actual + ">");
        }
    }

    private static final class testAddIndexingJob_CustomIndexer extends CustomIndexer {
        public int cnt = 0;
        public FileObject indexedRoot;
        public Iterable<? extends FileObject> indexedFiles;
        
        public int cntSupplementary = 0;
        public FileObject indexedSupplementaryRoot;
        public Iterable<? extends FileObject> indexedSupplementaryFiles;

        private final URL supplementaryRoot;
        private final URL supplementaryFile;

        public testAddIndexingJob_CustomIndexer(URL supplementaryRoot, URL supplementaryFile) {
            this.supplementaryRoot = supplementaryRoot;
            this.supplementaryFile = supplementaryFile;
        }

        protected @Override void index(Iterable<? extends Indexable> files, Context context) {
            if (context.isSupplementaryFilesIndexing()) {
                indexedSupplementaryRoot = context.getRoot();
                indexedSupplementaryFiles = new Convertor(files);
                cntSupplementary++;
            } else {
                indexedRoot = context.getRoot();
                indexedFiles = new Convertor(files);
                cnt++;
                
                context.addSupplementaryFiles(supplementaryRoot, Collections.singleton(supplementaryFile));
            }
        }

        private static final class Convertor implements Iterable<FileObject> {
            private final Iterable<? extends Indexable> indexables;
            public Convertor(Iterable<? extends Indexable> indexables) {
                this.indexables = indexables;
            }
            public Iterator<FileObject> iterator() {
                final Iterator<? extends Indexable> i = indexables.iterator();
                return new Iterator<FileObject>() {
                    public boolean hasNext() {
                        return i.hasNext();
                    }

                    public FileObject next() {
                        return URLMapper.findFileObject(i.next().getURL());
                    }

                    public void remove() {
                        i.remove();
                    }
                };
            }
        }
    } // End of testAddIndexingJob_CustomIndexer class

    public static final class testAddIndexingJob_PathRecognizer extends PathRecognizer {
        public @Override Set<String> getSourcePathIds() {
            return null;
        }

        public @Override Set<String> getLibraryPathIds() {
            return null;
        }

        public @Override Set<String> getBinaryLibraryPathIds() {
            return null;
        }

        public @Override Set<String> getMimeTypes() {
            return Collections.singleton("text/plain");
        }
    } // End of testAddIndexingJob_PathRecognizer class

    public void testShuttdown() throws InterruptedException {
        testShuttdown_TimedWork work1 = new testShuttdown_TimedWork();
        testShuttdown_TimedWork work2 = new testShuttdown_TimedWork();
        testShuttdown_TimedWork work3 = new testShuttdown_TimedWork();
        testShuttdown_TimedWork work4 = new testShuttdown_TimedWork();
        testShuttdown_TimedWork work5 = new testShuttdown_TimedWork();
        RepositoryUpdater.getDefault().scheduleWork(work1, false);
        RepositoryUpdater.getDefault().scheduleWork(work2, false);
        RepositoryUpdater.getDefault().scheduleWork(work3, false);
        RepositoryUpdater.getDefault().scheduleWork(work4, false);
        RepositoryUpdater.getDefault().scheduleWork(work5, false);

        boolean successfullyStopped = RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                RepositoryUpdater.getDefault().stop();
            }
        }).waitFinished(5000);

        assertTrue("RepositoryUpdate.stop() timed out", successfullyStopped);
        assertTrue("work1's getDone method not called", work1.getDoneCalled);
        assertTrue("work1 was not cancelled", work1.workCancelled);
        assertFalse("work2's getDone method should not be called", work2.getDoneCalled);
        assertFalse("work2 was should not be cancelled", work2.workCancelled);
        assertFalse("work3's getDone method should not be called", work3.getDoneCalled);
        assertFalse("work3 was should not be cancelled", work3.workCancelled);
        assertFalse("work4's getDone method should not be called", work4.getDoneCalled);
        assertFalse("work4 was should not be cancelled", work4.workCancelled);
        assertFalse("work5's getDone method should not be called", work5.getDoneCalled);
        assertFalse("work5 was should not be cancelled", work5.workCancelled);
    }

    private static final class testShuttdown_TimedWork extends RepositoryUpdater.Work {
        public boolean getDoneCalled = false;
        public boolean workCancelled = false;
        
        public testShuttdown_TimedWork() {
            super(false);
        }

        protected @Override void getDone() {
            getDoneCalled = true;
            while(!isCancelled()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    //ignore
                }
            }
            workCancelled = isCancelled();
        }
    } // End of testShuttdown_TimedWork class
}
