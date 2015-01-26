/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.api.codemodel.providers;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.api.codemodel.CMDiagnostic;
import org.netbeans.modules.cnd.api.codemodel.CMIndex;
import org.netbeans.modules.cnd.api.codemodel.visit.CMDeclaration;
import org.netbeans.modules.cnd.api.codemodel.visit.CMEntityReference;
import org.netbeans.modules.cnd.api.codemodel.visit.CMInclude;
import org.netbeans.modules.cnd.api.codemodel.visit.CMVisitQuery;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.codemodel.bridge.impl.NativeProjectCompilationDataBase;
import org.netbeans.modules.cnd.codemodel.storage.api.CMStorageManager;
import org.netbeans.modules.cnd.codemodel.storage.spi.CMStorage;
import org.netbeans.modules.cnd.spi.codemodel.providers.CMCompilationDataBase;
import org.netbeans.modules.cnd.spi.codemodel.support.SPIUtilities;
import org.netbeans.modules.cnd.spi.codemodel.trace.CMTraceUtils;
import org.netbeans.modules.cnd.test.CndBaseTestCase;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author mtishkov
 */
public class MashaTest extends CndBaseTestCase {
    private static final CMVisitQuery.VisitOptions INDEX_OPTIONS = CMVisitQuery.VisitOptions.valueOf(
            CMVisitQuery.VisitOptions.SuppressWarnings,
            CMVisitQuery.VisitOptions.SkipParsedBodiesInSession ,
            CMVisitQuery.VisitOptions.None);
    
    static {
        System.setProperty("cnd.codemodel.storage.trace", "false");
        System.setProperty("cnd.codemodel.storage.timing", "true");
        //System.setProperty("cm.index.sourcefile", "true"); // CMTraceUtils.INDEX_SOURCE_FILE
    }

    public MashaTest(String test) {
        super(test);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown(); 
    }

    @Override
    protected int timeOut() {
        return Integer.MAX_VALUE;
    }

    private static final String TMP_NET_BEANS_PROJECTS_QUOTE_1 = "/tmp/NetBeansProjects/Quote_1";
    private static final String TMP_NET_BEANS_PROJECTS_QUOTE_NOSYS = "/tmp/NetBeansProjects/QuoteNoSys";
    private static final String TEST_PROJECT = TMP_NET_BEANS_PROJECTS_QUOTE_1;
    private static final boolean printOut = false;
    private static final boolean storeInDatabase = Boolean.valueOf(System.getProperty("cnd.codemodel.storage.test.db", "false"));
    
    public void testIndexOnParseH2() throws Exception {
        doTestIndexProject("berkeley", TEST_PROJECT, true, printOut, storeInDatabase);
    }

//    public void testIndexAfterParseH2() throws Exception {
//        doTestIndexProject("jdbc:h2", TEST_PROJECT, false, printOut, storeInDatabase);
//    }
//
//    public void testH2() throws Exception {
//        doTestProject("jdbc:h2");
//    }
//
//    public void testHSQL() throws Exception {
//        doTestProject("jdbc:hsqldb");
//    }
//
//    public void testProject() throws Exception {
//        String odbcUrl = NativeExecutionTestSupport.getRcFile().get("new_codemodel", "cursor_visitor_test_odbc_url", "jdbc:hsqldb");
//        doTestProject(odbcUrl);
//    }

    public void doTestProject(String odbcUrl) throws Exception {
        System.out.printf("\n=== perfornming test for %s \n", odbcUrl);
        //CMDataBase.compact();       
        String nbProject = NativeExecutionTestSupport.getRcFile().get("new_codemodel", "cursor_visitor_test_project", "/tmp/NetBeansProjects/Quote_1");
        //String nbProject = "/export/home/masha/ssd/work/clucene-core-0.9.21b/";        
        final File file = new File(nbProject);
        assertTrue("File " + file.getAbsolutePath() + " does not exist", file.exists());

        final FileObject toFileObject = FileUtil.toFileObject(file);
        assertNotNull(toFileObject);
        Project findProject = ProjectManager.getDefault().findProject(toFileObject);
        assertNotNull(findProject);

        NativeProject nativeProject = findProject.getLookup().lookup(NativeProject.class);
        assertNotNull(nativeProject);
        long startTime = System.currentTimeMillis();
        NativeProjectCompilationDataBase nativeProjectCompilationDataBase = new NativeProjectCompilationDataBase(nativeProject);
        CMIndex idx = SPIUtilities.parse(nativeProjectCompilationDataBase);
        final Object key = findProject.getProjectDirectory().getName();
        SPIUtilities.registerIndex(key, idx);
        Collection<CMCompilationDataBase.Entry> entries = nativeProjectCompilationDataBase.getEntries();
        long endIndexingPart = System.currentTimeMillis();
        System.out.println("indexing time " + toTimeString(endIndexingPart, startTime));
        final String storageName = "CursorVisitorProjectTest.References." + key;        
        final CMStorage storage = CMStorageManager.getInstance(storageName, odbcUrl);
        final AtomicInteger counter = new AtomicInteger(0);
        //CMVisitQuery.visitReferences(null, Arrays.asList(idx), new CMVisitQuery.IndexCallback() {
        CMVisitQuery.visitIndex(idx, new CMVisitQuery.IndexCallback() {
            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public void onDiagnostics(Iterable<CMDiagnostic> diagnostics) {
            }

            @Override
            public void onIndclude(CMInclude include) {
                //System.out.println("new include:" + include.toString());
            }

            @Override
            public void onTranslationUnit() {
                //  System.out.println("new translation unit");
            }

            @Override
            public void onDeclaration(CMDeclaration declaration) {
                counter.incrementAndGet();
                storage.addDeclaration(declaration);
            }

            @Override
            public void onReference(CMEntityReference entityReference) {
                //   System.out.println("On reference : " + counter.incrementAndGet());
                counter.incrementAndGet();
                storage.addEntityReference(entityReference);
            }
        }, INDEX_OPTIONS);
        long endObjectCreation = System.currentTimeMillis();
        long callbackDuration = endObjectCreation - startTime;
        long startDatabase = System.currentTimeMillis();
        System.out.println("Will start database flush, "
                + "      callbackDuration=" + toTimeString(callbackDuration, 0));
        storage.flush();
        long endTime = System.currentTimeMillis();

        //long objectCreationDuration = startDatabase -startObjectCreation ;
        long databaseFlush = endTime - startDatabase;
        long totalTime = endTime - startTime;
        System.out.println("name=" + key + " decl count=" + counter.get() + "\n"
                + "      totalTime=" + toTimeString(totalTime, 0)
                + "      callbackDuration=" + toTimeString(callbackDuration, 0)
                //                + "      objectCreationDuration=" + objectCreationDuration +                 
                //                " ms which is " + (objectCreationDuration/1000) + "." + (objectCreationDuration%1000) + " s " 
                + "      flush duration=" + toTimeString(databaseFlush, 0));
        //System.out.println("name=" + key + " cursors count=" + counterCursors.get() + " time=" + duration + " ms which is " + (duration/1000) + "." + (duration%1000) + " s");
        //	c:quote.cc@1751@aN@customers
        long startQuertyTime = System.currentTimeMillis();
        storage.query("test");

        try {
//            String strUri = NativeExecutionTestSupport.getRcFile().get("new_codemodel", "cursor_visitor_test_uri");
//            String strLine = NativeExecutionTestSupport.getRcFile().get("new_codemodel", "cursor_visitor_test_line");
//            String strCol = NativeExecutionTestSupport.getRcFile().get("new_codemodel", "cursor_visitor_test_column");
//            if (strUri != null && strLine!= null && strCol!= null) {
//                URI uri = new URI(strUri);
//                int line = Integer.parseInt(strLine);
//                int col = Integer.parseInt(strCol);
//                long time = System.currentTimeMillis();
//                CMReference ref = CMReferenceQuery.findReference(uri, line, col);
//                time = System.currentTimeMillis() - time;
//                if (ref == null) {
//                    String message = String.format("[%s] Reference at %s %d:%d not found", odbcUrl, uri, line, col, time);
//                    System.out.println(message);
//                    fail(message);
//                } else {
//                    System.out.printf("[%s] Reference at %s %d:%d found! time=%d ms\n", odbcUrl, uri, line, col, time);
//                    time = System.currentTimeMillis();
//                    final AtomicInteger refCount = new AtomicInteger(0);
//                    CMReferenceQuery.visitReferences(
//                            Arrays.asList(ref.getReferencedEntity().getUSR()),
//                            CMModel.getIndices(), CMReferenceQuery.QueryFlags.Usages,
//                            new CMReferenceQuery.ReferenceCallback() {
//
//                        @Override
//                        public boolean isCancelled() {
//                            return false;
//                        }
//
//                        @Override
//                        public void onIndex(CMIndex index) {
//                        }
//
//                        @Override
//                        public void onReference(CMReference reference) {
//                            refCount.incrementAndGet();
//                            System.out.printf("\t%s\n", CMTraceUtils.toString(reference));
//                        }
//                    });
//                    time = System.currentTimeMillis() - time;
//                    System.out.printf("%d references found; time=%d\n", refCount.get(), time);
//                    int refCnt = NativeExecutionTestSupport.getRcFile().get("new_codemodel", "cursor_visitor_test_occurrences", -1);
//                    if (refCnt >= 0) {
//                        assertEquals("found references count", refCnt, refCount.get());
//                    }
//                }
//            }
        } finally {

            //storage.query("c:quote.cc@1751@aN@customers");
    //        try{
    //            storage.query("c.*FieldsWriter");
    //        }catch (Exception ex) {
    //            ex.printStackTrace();
    //        }
            long endQuertyTime = System.currentTimeMillis();
            long queryDuration = endQuertyTime - startQuertyTime;
            System.out.println("query duration=" + toTimeString(queryDuration, 0));
            SPIUtilities.unregisterIndex(key);

    //            CMDataBase.getInstance("CursorVisitorProjectTest.Cursors").shutdown();
            storage.shutdown();
            CMStorageManager.testShutdown(storage);
        }
    }
    
    public void doTestIndexProject(String odbcUrl, String projectPath, boolean indexOnParse, boolean printOut, boolean storeInDatabase) throws Exception {
        String nbProject = NativeExecutionTestSupport.getRcFile().get("new_codemodel", "cursor_visitor_test_project", projectPath);
        System.out.printf("\n=== perfornming %s test for %s \n", getName(), nbProject);
        //String nbProject = "/export/home/masha/ssd/work/clucene-core-0.9.21b/";        
        final File file = new File(nbProject);
        assertTrue("File " + file.getAbsolutePath() + " does not exist", file.exists());

        final FileObject toFileObject = FileUtil.toFileObject(file);
        assertNotNull(toFileObject);
        Project findProject = ProjectManager.getDefault().findProject(toFileObject);
        assertNotNull("No IDE project for " + toFileObject, findProject);

        NativeProject nativeProject = findProject.getLookup().lookup(NativeProject.class);
        assertNotNull("No Native project for " + findProject, nativeProject);
        NativeProjectCompilationDataBase cdb = new NativeProjectCompilationDataBase(nativeProject);
        final AtomicInteger refCounter = new AtomicInteger(0);
        final AtomicInteger declCounter = new AtomicInteger(0);
        final Object key = findProject.getProjectDirectory().getName();
        final String storageName = "CursorVisitorProjectTest.References." + key;        
        final CMStorage storage = storeInDatabase ? CMStorageManager.getInstance(storageName, odbcUrl) : null;                
        boolean indexRegistered = false;
        if ((storeInDatabase && storage.needsIndexing(cdb)) || !storeInDatabase) {
            IndexCallbackImpl callback = new IndexCallbackImpl(storage, declCounter, refCounter, printOut, storeInDatabase);
            CMIndex idx;
            long startIndexTime;
            long endIndexingPart;
            long startParseTime = System.currentTimeMillis();
            long endParsingPart;
            indexRegistered = true;
            if (indexOnParse) {
                endParsingPart = startParseTime;
                startIndexTime = startParseTime;
                idx = SPIUtilities.createIndex(cdb, callback, INDEX_OPTIONS);
                endIndexingPart = System.currentTimeMillis();
                SPIUtilities.registerIndex(key, idx);
            } else {
                idx = SPIUtilities.parse(cdb);
                endParsingPart = System.currentTimeMillis();
                SPIUtilities.registerIndex(key, idx);
                startIndexTime = System.currentTimeMillis();
                CMVisitQuery.visitIndex(idx, callback, INDEX_OPTIONS);
                endIndexingPart = System.currentTimeMillis();
            }
            System.out.println("Indexed Decls=" + declCounter.get() + " Refs=" + refCounter.get() + " All = " + (refCounter.get() + declCounter.get()));
            System.out.println("Parsing time " + toTimeString(endParsingPart, startParseTime));
            System.out.println("Indexing time " + toTimeString(endIndexingPart, startIndexTime));
            System.out.println("Total time " + toTimeString(endIndexingPart, startParseTime));
        }        
        if (storeInDatabase) {
            storage.flush();
            storage.query("test");
            storage.shutdown();
        }
        if (indexRegistered) {
            SPIUtilities.unregisterIndex(key);
        }
    }

    private String toTimeString(long end, long start) {
        return (end - start)+ "ms which is " + 
                ((end - start) / 1000) + "." + ((end - start) % 1000) + "s";
    }
    
    private static class IndexCallbackImpl implements CMVisitQuery.IndexCallback {

        private final AtomicInteger declCounter;
        private final AtomicInteger refCounter;
        private final boolean printOut;
        private final boolean storeInDatabase;       
        private final CMStorage cmStorage;
        private int countTU = 0;

        private IndexCallbackImpl(CMStorage cmStorage, AtomicInteger declCounter, AtomicInteger refCounter, boolean printOut, boolean storeInDatabase) {
            this.cmStorage = cmStorage;
            this.declCounter = declCounter;
            this.refCounter = refCounter;
            this.printOut = printOut;
            this.storeInDatabase = storeInDatabase;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public void onDiagnostics(Iterable<CMDiagnostic> diagnostics) {
        }

        @Override
        public void onIndclude(CMInclude include) {
            //System.out.println("new include:" + include.toString());
        }

        @Override
        public void onTranslationUnit() {
            System.out.println("[" + (++countTU) + "] so far Decls=" + declCounter.get() + " Refs=" + refCounter.get() + " All = " + (refCounter.get() + declCounter.get()));
//            if (storeInDatabase && countTU % 10 == 0) {
//                cmStorage.waitQueueIdle();
//            }
        }

        @Override
        public void onDeclaration(CMDeclaration declaration) {
            declCounter.incrementAndGet();
            if (printOut) {
                System.out.println(CMTraceUtils.toString(declaration));
            }
            if (storeInDatabase) {
                cmStorage.addDeclaration(declaration);
            }
        }

        @Override
        public void onReference(CMEntityReference entityReference) {
            refCounter.incrementAndGet();
            if (printOut) {
                System.out.println(CMTraceUtils.toString(entityReference));
            }
            if (storeInDatabase) {
                cmStorage.addEntityReference(entityReference);
            }
        }
    }
}
