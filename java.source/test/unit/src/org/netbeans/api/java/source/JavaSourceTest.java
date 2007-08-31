/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.java.source;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.TreeScanner;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.lang.model.element.Modifier;
import javax.swing.text.StyledDocument;
import javax.tools.JavaFileObject;
import junit.framework.*;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.swing.text.BadLocationException;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.NameKind;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.parsing.SourceFileObject;
import org.netbeans.modules.java.source.usages.ClassIndexImpl.UsageType;
import org.netbeans.modules.java.source.usages.Index;
import org.netbeans.modules.java.source.usages.Index;
import org.netbeans.modules.java.source.usages.Pair;
import org.netbeans.modules.java.source.usages.ResultConvertor;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.classpath.CacheClassPath;
import org.netbeans.modules.java.source.usages.IndexFactory;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.modules.java.source.usages.PersistentClassIndex;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.netbeans.spi.java.classpath.ClassPathProvider;
/**
 *
 * @author Tomas Zezula
 */
public class JavaSourceTest extends NbTestCase {
    
    static {
        JavaSourceTest.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", JavaSourceTest.Lkp.class.getName());
        Assert.assertEquals(JavaSourceTest.Lkp.class, Lookup.getDefault().getClass());
    }   
    
    public static class Lkp extends ProxyLookup {
        
        private static Lkp DEFAULT;
        
        public Lkp () {
            Assert.assertNull(DEFAULT);
            DEFAULT = this;
            ClassLoader l = Lkp.class.getClassLoader();
            this.setLookups(
                 new Lookup [] {
                    Lookups.metaInfServices(l),
                    Lookups.singleton(l),
            });
        }
        
        public void setLookupsWrapper(Lookup... l) {
            setLookups(l);
        }
        
    }
        
    private static final String REPLACE_PATTERN = "/*TODO:Changed-by-test*/";
    private static final String TEST_FILE_CONTENT=
                "public class {0} '{\n"+
                "   public static void main (String[] args) {\n"+
                "       javax.swing.JTable table = new javax.swing.JTable ();\n"+
                "       Class c = table.getModel().getClass();\n"+
                "       "+REPLACE_PATTERN+"\n"+
                "   }\n"+
                "}'\n";

    
    
    public JavaSourceTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        this.clearWorkDir();
        File workDir = getWorkDir();
        File cacheFolder = new File (workDir, "cache"); //NOI18N
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new NbTestSuite(JavaSourceTest.class);        
//        TestSuite suite = new NbTestSuite ();
//        suite.addTest(new JavaSourceTest("testMultiJavaSource"));
        return suite;
    }
    
    
    public void testPhaseCompletionTask () throws MalformedURLException, InterruptedException, IOException {
        FileObject test = createTestFile ("Test1");
        ClassPath bootPath = createBootPath ();
        ClassPath compilePath = createCompilePath ();        
        JavaSource js = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, null), test);
        DataObject dobj = DataObject.find(test);
        EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);                        
        final StyledDocument doc = ec.openDocument();
        Thread.sleep(500);
        CountDownLatch[] latches1 = new CountDownLatch[] {
            new CountDownLatch (1),
            new CountDownLatch (1)
        };
        CountDownLatch[] latches2 = new CountDownLatch[] {
            new CountDownLatch (1),
            new CountDownLatch (1)
        };        
        AtomicInteger counter = new AtomicInteger (0);
        CancellableTask<CompilationInfo> task1 = new DiagnosticTask(latches1, counter, Phase.RESOLVED);
        CancellableTask<CompilationInfo> task2 =  new DiagnosticTask(latches2, counter, Phase.PARSED);
        js.addPhaseCompletionTask (task1,Phase.RESOLVED,Priority.HIGH);
        js.addPhaseCompletionTask(task2,Phase.PARSED,Priority.LOW);
        assertTrue ("Time out",waitForMultipleObjects(new CountDownLatch[] {latches1[0], latches2[0]}, 15000)); 
        assertEquals ("Called more times than expected",2,counter.getAndSet(0));        
        Thread.sleep(500);  //Making test a more deterministic, when the task is cancelled by DocListener, it's hard for test to recover from it
        NbDocument.runAtomic (doc,
            new Runnable () {
                public void run () {                        
                    try {
                        String text = doc.getText(0,doc.getLength());
                        int index = text.indexOf(REPLACE_PATTERN);
                        assertTrue (index != -1);
                        doc.remove(index,REPLACE_PATTERN.length());
                        doc.insertString(index,"System.out.println();",null);
                    } catch (BadLocationException ble) {
                        ble.printStackTrace(System.out);
                    }                 
                }
        });        
        assertTrue ("Time out",waitForMultipleObjects(new CountDownLatch[] {latches1[1], latches2[1]}, 15000)); 
        assertEquals ("Called more times than expected",2,counter.getAndSet(0));
        js.removePhaseCompletionTask (task1);
        js.removePhaseCompletionTask (task2);
    }
    
    public void testCompileControlJob () throws MalformedURLException, IOException, InterruptedException {
        FileObject test = createTestFile ("Test1");
        ClassPath bootPath = createBootPath ();
        ClassPath compilePath = createCompilePath ();
        JavaSource js = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, null), test);
        CountDownLatch latch = new CountDownLatch (1);
        js.runUserActionTask(new CompileControlJob(latch),true);
        assertTrue ("Time out",latch.await(15,TimeUnit.SECONDS));
    }
    
    public void testModificationJob () throws Exception {
        final FileObject testFile1 = createTestFile("Test1");
        final FileObject testFile2 = createTestFile("Test2");
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = createCompilePath();
        final JavaSource js = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, null), testFile1, testFile2);
        CountDownLatch latch = new CountDownLatch (2);
        js.runModificationTask(new WorkingCopyJob (latch)).commit();
        assertTrue ("Time out",latch.await(15,TimeUnit.SECONDS));
    }

    public void testInterference () throws MalformedURLException, IOException, InterruptedException {
        FileObject testFile1 = createTestFile ("Test1");
        FileObject testFile2 = createTestFile ("Test2");
        ClassPath bootPath = createBootPath ();
        ClassPath compilePath = createCompilePath ();
        JavaSource js1 = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, null), testFile1);
        JavaSource js2 = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, null), testFile2);
        DataObject dobj = DataObject.find(testFile1);
        EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);                        
        final StyledDocument doc = ec.openDocument();
        Thread.sleep(500);
        CountDownLatch[] latches1 = new CountDownLatch[] {
            new CountDownLatch (1),
            new CountDownLatch (1),
        };
        CountDownLatch[] latches2 = new CountDownLatch[] {
            new CountDownLatch (1),
        };
        CountDownLatch latch3 = new CountDownLatch (1);        
        AtomicInteger counter = new AtomicInteger (0);
        
        DiagnosticTask task1 = new DiagnosticTask(latches1, counter, Phase.RESOLVED);
        CancellableTask<CompilationInfo> task2 = new DiagnosticTask(latches2, counter, Phase.RESOLVED);
        
        js1.addPhaseCompletionTask(task1,Phase.RESOLVED,Priority.HIGH);
        Thread.sleep(500);  //Making test a more deterministic, when the task is cancelled by DocListener, it's hard for test to recover from it
        js2.runUserActionTask(new CompileControlJob(latch3),true);
        js2.addPhaseCompletionTask(task2,Phase.RESOLVED,Priority.MAX);        
        boolean result = waitForMultipleObjects (new CountDownLatch[] {latches1[0], latches2[0], latch3}, 15000);
        if (!result) {
            assertTrue (String.format("Time out, latches1[0]: %d latches2[0]: %d latches3: %d",latches1[0].getCount(), latches2[0].getCount(), latch3.getCount()), false);
        }        
        assertEquals ("Called more times than expected",2,counter.getAndSet(0));        
                
        Thread.sleep(500);  //Making test a more deterministic, when the task is cancelled by DocListener, it's hard for test to recover from it
        NbDocument.runAtomic (doc,
            new Runnable () {
                public void run () {                        
                    try {
                        String text = doc.getText(0,doc.getLength());
                        int index = text.indexOf(REPLACE_PATTERN);
                        assertTrue (index != -1);
                        doc.remove(index,REPLACE_PATTERN.length());
                        doc.insertString(index,"System.out.println();",null);
                    } catch (BadLocationException ble) {
                        ble.printStackTrace(System.out);
                    }                 
                }
        });        
        assertTrue ("Time out",waitForMultipleObjects(new CountDownLatch[] {latches1[1]}, 15000)); 
        assertEquals ("Called more times than expected",1,counter.getAndSet(0));        
        js1.removePhaseCompletionTask(task1);
        js2.removePhaseCompletionTask(task2);
    }
    
    public void testDocumentChanges () throws Exception {
        FileObject testFile1 = createTestFile ("Test1");
        ClassPath bootPath = createBootPath ();
        ClassPath compilePath = createCompilePath ();
        JavaSource js1 = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, null), testFile1);

        final CountDownLatch start = new CountDownLatch (1);
        final CountDownLatch stop =  new CountDownLatch (1);
        final AtomicBoolean last = new AtomicBoolean (false);
        final AtomicInteger counter = new AtomicInteger (0);
        
        CancellableTask<CompilationInfo> task = new CancellableTask<CompilationInfo>() {
            
            private int state = 0;

            public void cancel() {
            }

            public void run(CompilationInfo ci) throws Exception {
                switch (state) {
                    case 0:
                        state = 1;
                        start.countDown();
                        break;
                    case 1:
                        counter.incrementAndGet();
                        if (last.get()) {
                            stop.countDown();
                        }
                        break;
                }
            }
        };        
        js1.addPhaseCompletionTask(task,Phase.PARSED,Priority.HIGH);
        start.await();       
        Thread.sleep(500);
        final DataObject dobj = DataObject.find(testFile1);        
        final EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);                        
        final StyledDocument doc = ec.openDocument();                
        for (int i=0; i<10; i++) {
            if (i == 9) {
                last.set(true);
            }
            NbDocument.runAtomic (doc,
                new Runnable () {
                    public void run () {                        
                        try {                                                
                            doc.insertString(0," ",null);
                        } catch (BadLocationException ble) {
                            ble.printStackTrace(System.out);
                        }                 
                    }
            });
            Thread.sleep(100);
        }
        assertTrue ("Time out",stop.await(15000, TimeUnit.MILLISECONDS));
        assertEquals("Called more time than expected",1,counter.get());
        js1.removePhaseCompletionTask(task);
    }
    
    
//    public void testDataFlow () throws MalformedURLException, IOException, InterruptedException {
//        FileObject testFile = createTestFile ("Test");
//        ClassPath bootPath = createBootPath ();
//        ClassPath compilePath = createCompilePath ();
//        JavaSource js = new JavaSource (testFile, bootPath, compilePath);
//        CountDownLatch latch = new CountDownLatch (1);
//        CancellableTask<CompilationInfo> task = new DiagnosticTask(new CountDownLatch[] {latch}, null, Phase.DATA_FLOW_CHECKED);
//        js.addPhaseCompletionTask(task,Phase.DATA_FLOW_CHECKED, 10);        
//        latch.await(15000,TimeUnit.MILLISECONDS);
//        js.removePhaseCompletionTask(task);
//    }
        
    public void testParsingDelay() throws MalformedURLException, InterruptedException, IOException, BadLocationException {
        FileObject test = createTestFile ("Test1");
        ClassPath bootPath = createBootPath ();
        ClassPath compilePath = createCompilePath ();        
        JavaSource js = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, null), test);
        DataObject dobj = DataObject.find(test);
        EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);                        
        final StyledDocument doc = ec.openDocument();
        Thread.sleep(500);  //It may happen that the js is invalidated before the dispatch of task is done and the test of timers may fail        
        CountDownLatch[] latches = new CountDownLatch[] {
            new CountDownLatch (1),
            new CountDownLatch (1)
        };
        long[] timers = new long[2];
        AtomicInteger counter = new AtomicInteger (0);
        CancellableTask<CompilationInfo> task = new DiagnosticTask(latches, timers, counter, Phase.PARSED);
        js.addPhaseCompletionTask (task,Phase.PARSED, Priority.HIGH);
        assertTrue ("Time out",waitForMultipleObjects(new CountDownLatch[] {latches[0]}, 15000));
        assertEquals ("Called more times than expected",1,counter.getAndSet(0));                
        long start = System.currentTimeMillis();
        Thread.sleep(500);  //Making test a more deterministic, when the task is cancelled by DocListener, it's hard for test to recover from it
        NbDocument.runAtomic (doc,
            new Runnable () {
                public void run () {                        
                    try {
                        String text = doc.getText(0,doc.getLength());
                        int index = text.indexOf(REPLACE_PATTERN);
                        assertTrue (index != -1);
                        doc.remove(index,REPLACE_PATTERN.length());
                        doc.insertString(index,"System.out.println();",null);
                    } catch (BadLocationException ble) {
                        ble.printStackTrace(System.out);
                    }                 
                }
        });        
        assertTrue ("Time out",waitForMultipleObjects(new CountDownLatch[] {latches[1]}, 15000)); 
        assertEquals ("Called more times than expected",1,counter.getAndSet(0));
        assertTrue("Took less time than expected time=" + (timers[1] - start), (timers[1] - start) >= js.getReparseDelay());
        js.removePhaseCompletionTask (task);
    }
    
    public void testJavaSourceIsReclaimable() throws MalformedURLException, InterruptedException, IOException, BadLocationException {
        FileObject test = createTestFile ("Test1");
        ClassPath bootPath = createBootPath ();
        ClassPath compilePath = createCompilePath ();        
        JavaSource js = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, null), test);
        DataObject dobj = DataObject.find(test);
        EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);                        
        final StyledDocument[] doc = new StyledDocument[] {ec.openDocument()};
        Thread.sleep(500);
        CountDownLatch[] latches = new CountDownLatch[] {
            new CountDownLatch (1),
            new CountDownLatch (1)
        };
        AtomicInteger counter = new AtomicInteger (0);
        CancellableTask<CompilationInfo> task = new DiagnosticTask(latches, counter, Phase.PARSED);
        js.addPhaseCompletionTask (task,Phase.PARSED,Priority.HIGH);
        assertTrue ("Time out",waitForMultipleObjects(new CountDownLatch[] {latches[0]}, 15000));
               
        Thread.sleep(500);  //Making test a more deterministic, when the task is cancelled by DocListener, it's hard for test to recover from it
        NbDocument.runAtomic (doc[0],
            new Runnable () {
                public void run () {                        
                    try {
                        String text = doc[0].getText(0,doc[0].getLength());
                        int index = text.indexOf(REPLACE_PATTERN);
                        assertTrue (index != -1);
                        doc[0].remove(index,REPLACE_PATTERN.length());
                        doc[0].insertString(index,"System.out.println();",null);
                    } catch (BadLocationException ble) {
                        ble.printStackTrace(System.out);
                    }                 
                }
        });
        
        assertTrue ("Time out",waitForMultipleObjects(new CountDownLatch[] {latches[1]}, 15000)); 
        js.removePhaseCompletionTask (task);
        
        Reference jsWeak = new WeakReference(js);
        Reference testWeak = new WeakReference(test);
        
        SaveCookie sc = (SaveCookie) dobj.getCookie(SaveCookie.class);
        
        sc.save();
        
        sc = null;
        
        js = null;
        test = null;
        dobj = null;
        ec = null;
        doc[0] = null;
        
        //give the worker thread chance to remove the task:
        //if the tests starts to fail randomly, try to increment the timeout
        Thread.sleep(1000);
        
        assertGC("JavaSource is reclaimable", jsWeak);
        //the file objects is held by the timers component
        //and maybe others:
        assertGC("FileObject is reclaimable", testWeak);
    }
    
    public void testChangeInvalidates() throws MalformedURLException, InterruptedException, IOException, BadLocationException {
        FileObject test = createTestFile ("Test1");
        ClassPath bootPath = createBootPath ();
        ClassPath compilePath = createCompilePath ();
        JavaSource js = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, null), test);
        int originalReparseDelay = js.getReparseDelay();
        
        try {
            js.setReparseDelay(Integer.MAX_VALUE, false); //never automatically reparse                        
            CountDownLatch latch1 = new CountDownLatch (1);
            final CountDownLatch latch2 = new CountDownLatch (1);
            AtomicInteger counter = new AtomicInteger (0);
            CancellableTask<CompilationInfo> task = new DiagnosticTask(new CountDownLatch[] {latch1}, counter, Phase.PARSED);
            js.addPhaseCompletionTask (task,Phase.PARSED,Priority.HIGH);
            assertTrue ("Time out",waitForMultipleObjects(new CountDownLatch[] {latch1}, 15000));

            DataObject dobj = DataObject.find(test);
            EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
            final StyledDocument[] doc = new StyledDocument[] {ec.openDocument()};
            Thread.sleep(500);  //Making test a more deterministic, when the task is cancelled by DocListener, it's hard for test to recover from it
            NbDocument.runAtomic (doc[0],
                    new Runnable () {
                public void run () {
                    try {
                        String text = doc[0].getText(0,doc[0].getLength());
                        int index = text.indexOf(REPLACE_PATTERN);
                        assertTrue (index != -1);
                        doc[0].remove(index,REPLACE_PATTERN.length());
                        doc[0].insertString(index,"System.out.println();",null);
                    } catch (BadLocationException ble) {
                        ble.printStackTrace(System.out);
                    }
                }
            });

            final boolean[] contentCorrect = new boolean[1];

            js.runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController controler) {
                    try {
                        controler.toPhase(Phase.PARSED);
                        contentCorrect[0] = controler.getText().contains("System.out.println");
                        latch2.countDown();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            },true);

            assertTrue("Time out",waitForMultipleObjects(new CountDownLatch[] {latch2}, 15000));
            assertTrue("Content incorrect", contentCorrect[0]);

            js.removePhaseCompletionTask (task);
        } finally {
            if (js != null) {
                js.setReparseDelay(originalReparseDelay, true);
            }
        }
    }
    
    //this test is quite unreliable (it often passes even in cases it should fail):
    public void testInvalidatesCorrectly() throws MalformedURLException, InterruptedException, IOException, BadLocationException {
        FileObject test = createTestFile ("Test1");
        ClassPath bootPath = createBootPath ();
        ClassPath compilePath = createCompilePath ();
        JavaSource js = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, null), test);
        DataObject dobj = DataObject.find(test);
        EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);
        final StyledDocument[] doc = new StyledDocument[] {ec.openDocument()};
        Thread.sleep (500);
        CountDownLatch[] latches = new CountDownLatch[] {
            new CountDownLatch (1),
            new CountDownLatch (1),
            new CountDownLatch (1),
        };
        AtomicInteger counter = new AtomicInteger (0);
        DiagnosticTask task = new DiagnosticTask(latches, counter, Phase.PARSED);
        js.addPhaseCompletionTask (task,Phase.PARSED,Priority.HIGH);
        assertTrue ("Time out",waitForMultipleObjects(new CountDownLatch[] {latches[0]}, 15000));                
        final int[] index = new int[1];
        Thread.sleep(500);  //Making test a more deterministic, when the task is cancelled by DocListener, it's hard for test to recover from it
        NbDocument.runAtomic (doc[0],
                new Runnable () {
            public void run () {
                try {
                    String text = doc[0].getText(0,doc[0].getLength());
                    index[0] = text.indexOf(REPLACE_PATTERN);
                    assertTrue (index[0] != -1);
                    doc[0].remove(index[0],REPLACE_PATTERN.length());
                    doc[0].insertString(index[0],"System.out.println();",null);
                } catch (BadLocationException ble) {
                    ble.printStackTrace(System.out);
                }
            }
        });
        
        assertTrue ("Time out",waitForMultipleObjects(new CountDownLatch[] {latches[1]}, 15000));
        Thread.sleep(500);  //Making test a more deterministic, when the task is cancelled by DocListener, it's hard for test to recover from it
        NbDocument.runAtomic(doc[0],
                new Runnable() {
            public void run() {
                try {
                    doc[0].insertString(index[0],"System.out.println();",null);
                } catch (BadLocationException ble) {
                    ble.printStackTrace(System.out);
                }
            }
        });
        //not sure how to make this 100% reliable.
        //this task has to be the first run after the previous change to document.
        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controler) {
                try {
                    controler.toPhase(Phase.PARSED);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        },true);
        
        assertTrue("Time out",waitForMultipleObjects(new CountDownLatch[] {latches[2]}, 15000));
        
        js.removePhaseCompletionTask (task);
    }
    
    
    /**
     * Tests deadlock
     */
    public void testRTB_005 () throws Exception {
        try {
            Object lock = new String ("Lock");
            JavaSource.jfoProvider = new TestProvider (lock);
            FileObject test = createTestFile ("Test1");
            ClassPath bootPath = createBootPath ();
            ClassPath compilePath = createCompilePath ();
            JavaSource js = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, null), test);
            js.addPhaseCompletionTask( new CancellableTask<CompilationInfo>() {
                public void run (CompilationInfo info) {
                    
                }
                
                public void cancel () {
                    
                }
            },Phase.PARSED,Priority.NORMAL);
            
            synchronized (lock) {
                js.revalidate();
                Thread.sleep(2000);
                js.runUserActionTask(new Task<CompilationController> () {
                    public void run (CompilationController c) {
                        
                    }                    
                },true);
            }
            
        } finally {
            JavaSource.jfoProvider = new JavaSource.DefaultJavaFileObjectProvider ();
        }
    }
    
    
    public void testCancelCall () throws Exception {
        FileObject test = createTestFile ("Test1");
        ClassPath bootPath = createBootPath ();
        ClassPath compilePath = createCompilePath ();        
        JavaSource js = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, null), test);
        WaitTask wt = new WaitTask (3000);
        js.addPhaseCompletionTask(wt, Phase.PARSED, Priority.BELOW_NORMAL);
        Thread.sleep(1000);
        WaitTask wt2 = new WaitTask (0);
        js.addPhaseCompletionTask(wt2, Phase.PARSED,Priority.MAX);
        Thread.sleep(10000);
        int cancelCount = wt.getCancelCount();
        assertEquals(1,cancelCount);
        int runCount = wt.getRunCount();
        assertEquals(2,runCount);
        js.removePhaseCompletionTask(wt);
        js.removePhaseCompletionTask(wt2);
    }
    
    public void testMultiJavaSource () throws Exception {
        final FileObject testFile1 = createTestFile("Test1");
        final FileObject testFile2 = createTestFile("Test2");
        final FileObject testFile3 = createTestFile("Test3");
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = createCompilePath();
        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath,compilePath,null);
        final JavaSource js = JavaSource.create(cpInfo,testFile1, testFile2, testFile3);
        CountDownLatch latch = new CountDownLatch (3);
        CompileControlJob ccj = new CompileControlJob (latch);
        ccj.multiSource = true;
        js.runUserActionTask(ccj,true);
        assertTrue(waitForMultipleObjects(new CountDownLatch[] {latch},10000));
        
        latch = new CountDownLatch (4);
        CompileControlJobWithOOM ccj2 = new CompileControlJobWithOOM (latch,1);
        js.runUserActionTask(ccj2,true);
        assertTrue(waitForMultipleObjects(new CountDownLatch[] {latch},10000));
    }
    
    public void testEmptyJavaSource () throws Exception {        
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = createCompilePath();
        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath,compilePath,null);
        final JavaSource js = JavaSource.create(cpInfo);
        CountDownLatch latch = new CountDownLatch (1);
        EmptyCompileControlJob ccj = new EmptyCompileControlJob (latch);
        js.runUserActionTask(ccj,true);
        assertTrue(waitForMultipleObjects(new CountDownLatch[] {latch},10000));        
    }
    
    public void testCancelDeadLock () throws Exception {
        final FileObject testFile1 = createTestFile("Test1");
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = createCompilePath();
        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath,compilePath,null);
        final JavaSource js = JavaSource.create(cpInfo,testFile1);
        js.runUserActionTask(
                new Task<CompilationController>() {            
                    
                    public void run(CompilationController parameter) throws Exception {
                        final Thread t = new Thread (new Runnable() {
                            public void run () {
                                try {
                                js.runUserActionTask(new Task<CompilationController>() {                            

                                    public void run(CompilationController parameter) throws Exception {
                                    }
                                },true);
                                } catch (IOException e) {
                                    AssertionError er = new AssertionError ();
                                    e.initCause(e);
                                    throw er;
                                }
                            }
                        });
                        t.start();
                        Thread.sleep(1000);
                        js.runUserActionTask (new Task<CompilationController>() {
                            
                            public void run(CompilationController parameter) throws Exception {
                            }
                        },true);
                    }
                },true 
        );
    }
    
    public void testCompileTaskStartedFromPhaseTask () throws Exception {
        final FileObject testFile1 = createTestFile("Test1");
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = createCompilePath();
        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath,compilePath,null);
        final JavaSource js = JavaSource.create(cpInfo,testFile1);
        final AtomicBoolean canceled = new AtomicBoolean (false);
        final CountDownLatch latch = new CountDownLatch (1);
        js.addPhaseCompletionTask(new CancellableTask<CompilationInfo> () {
            
            private boolean called = false;
            
            public void cancel() {
                canceled.set(true);
            }
            
            public void run(CompilationInfo parameter) throws Exception {
                if (!called) {
                    js.runUserActionTask(new Task<CompilationController>() {
                        public void run(CompilationController parameter) throws Exception {
                        }
                    },true);
                    called = true;
                    latch.countDown();
                }
            }
        }, Phase.PARSED, Priority.NORMAL);
        assertTrue (waitForMultipleObjects(new CountDownLatch[] {latch}, 10000));
        assertFalse ("Cancel called even for JavaSource dispatch thread!",canceled.get());
    }
    
    public void testUnsharedUserActionTask () throws IOException {
        final FileObject testFile1 = createTestFile("Test1");
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = createCompilePath();
        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath,compilePath,null);
        final JavaSource js = JavaSource.create(cpInfo,testFile1);
        final int[] identityHashCodes = new int[3];
        js.runUserActionTask(new Task<CompilationController> () {
            
            public void run (CompilationController c) {
                identityHashCodes[0] = System.identityHashCode(c.delegate);
            }
            
        },true);
        
        js.runUserActionTask(new Task<CompilationController> () {
            
            public void run (CompilationController c) {
                identityHashCodes[1] = System.identityHashCode(c.delegate);
            }
            
        },false);
        
        
        js.runUserActionTask(new Task<CompilationController> () {
            
            public void run (CompilationController c) {
                identityHashCodes[2] = System.identityHashCode(c.delegate);
            }
            
        },false);
        
        assertEquals(identityHashCodes[0], identityHashCodes[1]);
        assertFalse(identityHashCodes[1] == identityHashCodes[2]);
    }
    
    public void testRescheduleDoesNotStore() throws IOException, InterruptedException {
        final FileObject testFile1 = createTestFile("Test1");
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = createCompilePath();
        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath,compilePath,null);
        final JavaSource js = JavaSource.create(cpInfo,testFile1);
        final CountDownLatch waitFor = new CountDownLatch (1);
        final CountDownLatch second = new CountDownLatch (3);
        final int[] count = new int[1];
        final CancellableTask<CompilationInfo> task = new CancellableTask<CompilationInfo> () {
            public void cancel() { }
            public void run(CompilationInfo parameter) throws Exception {
                count[0]++;
                second.countDown();
            }
        };
        js.addPhaseCompletionTask(new CancellableTask<CompilationInfo> () {
            public void cancel() { }
            public void run(CompilationInfo parameter) throws Exception {
                waitFor.await();
            }
        }, Phase.PARSED, Priority.NORMAL);
        js.addPhaseCompletionTask(task, Phase.PARSED, Priority.NORMAL);
        js.rescheduleTask(task);
        js.rescheduleTask(task);
        waitFor.countDown();
        second.await(10, TimeUnit.SECONDS);
        assertEquals(1, count[0]);
    }
    
    
    public void testNestedActions () throws Exception {
        final FileObject testFile1 = createTestFile("Test1");
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = createCompilePath();
        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath,compilePath,null);
        final JavaSource js = JavaSource.create(cpInfo,testFile1);
        final Object[] delegateRef = new Object[1];
        // 1)  Two consequent shared tasks have to share CompilationInfo
        js.runUserActionTask(new Task<CompilationController>() {            
            
            public void run (CompilationController control) {
                delegateRef[0] = control.delegate;
            }            
        }, true);
        
        js.runUserActionTask(new Task<CompilationController>() {            
            
            public void run (CompilationController control) {
                assertTrue(delegateRef[0] == control.delegate);
            }            

        }, true);
        
        //2) Task following the unshared task has to have new CompilationInfo
        js.runUserActionTask(new Task<CompilationController>() {            
            
            public void run (CompilationController control) {
                delegateRef[0] = control.delegate;
            }            

        }, false);
        
        js.runUserActionTask(new Task<CompilationController>() {            
            
            public void run (CompilationController control) {
                assertTrue(delegateRef[0] != control.delegate);
            }            

        }, true);
        
        //3) Shared task started from shared task has to have CompilationInfo from the parent
        //   The shared task follong these tasks has to have the same CompilationInfo
        js.runUserActionTask(new CancellableTask<CompilationController>() {            
            
            public void run (CompilationController control) {
                delegateRef[0] = control.delegate;
                final Object[] delegateRef2 = new Object[] {control.delegate};
                try {
                    js.runUserActionTask(new Task<CompilationController> () {
                        public void run (CompilationController control) {
                            assertTrue (delegateRef2[0] == control.delegate);
                        }

                    }, true);
                } catch (IOException ioe) {
                    RuntimeException re = new RuntimeException ();
                    re.initCause(ioe);
                    throw re;
                }
            }            
            public void cancel () {}
        }, true);
        
        js.runUserActionTask(new Task<CompilationController>() {            
            
            public void run (CompilationController controll) {
                assertTrue(delegateRef[0] == controll.delegate);
            }            
        }, true);
        
        //4) Shared task started from unshared task has to have CompilationInfo from the parent (unshared task)
        //   The shared task follong these tasks has to have new CompilationInfo
        js.runUserActionTask(new Task<CompilationController>() {            
            
            public void run (CompilationController control) {
                delegateRef[0] = control.delegate;
                final Object[] delegateRef2 = new Object[] {control.delegate};
                try {
                    js.runUserActionTask(new CancellableTask<CompilationController> () {
                        public void run (CompilationController control) {
                            assertTrue (delegateRef2[0] == control.delegate);
                        }

                        public void cancel () {}
                    }, true);
                } catch (IOException ioe) {
                    RuntimeException re = new RuntimeException ();
                    re.initCause(ioe);
                    throw re;
                }
            }
        }, false);
        
        js.runUserActionTask(new Task<CompilationController>() {            
            
            public void run (CompilationController controll) {
                assertTrue(delegateRef[0] != controll.delegate);
            }            
        }, true);
        
        //5) Unshared task started from unshared task has to have new CompilationInfo
        //   The shared task following these tasks has to also have new CompilationInfo
        final Object[] delegateRef2 = new Object[1];
        js.runUserActionTask(new Task<CompilationController>() {                        
            public void run (CompilationController control) {
                delegateRef[0] = control.delegate;
                try {
                    js.runUserActionTask(new Task<CompilationController> () {
                        public void run (CompilationController control) {
                            assertTrue (delegateRef[0] != control.delegate);
                            delegateRef2[0] = control.delegate;
                        }

                    }, false);
                } catch (IOException ioe) {
                    RuntimeException re = new RuntimeException ();
                    re.initCause(ioe);
                    throw re;
                }
            }            
            public void cancel () {}
        }, false);
        
        js.runUserActionTask(new Task<CompilationController>() {            
            
            public void run (CompilationController controll) {
                assertTrue(delegateRef[0] != controll.delegate);
                assertTrue(delegateRef2[0] != controll.delegate);
            }            

        }, true);
        
        //6)Shared task(3) started from unshared task(2) which is started from other unshared task (1)
        //  has to see the CompilationInfo from the task (2) which is not equal to CompilationInfo from (1)
        js.runUserActionTask(new Task<CompilationController>() {                        
            public void run (CompilationController control) {
                delegateRef[0] = control.delegate;
                try {
                    js.runUserActionTask(new Task<CompilationController> () {
                        public void run (CompilationController control) {
                            assertTrue (delegateRef[0] != control.delegate);
                            delegateRef2[0] = control.delegate;
                            try {
                                js.runUserActionTask(new Task<CompilationController> () {
                                    public void run (CompilationController control) {
                                        assertTrue (delegateRef[0] != control.delegate);
                                        assertTrue (delegateRef2[0] == control.delegate);                            
                                    }
                                }, true);
                            } catch (IOException ioe) {
                                RuntimeException re = new RuntimeException ();
                                re.initCause(ioe);
                                throw re;
                            }
                        }
                    }, false);
                } catch (IOException ioe) {
                    RuntimeException re = new RuntimeException ();
                    re.initCause(ioe);
                    throw re;
                }
            }            
        }, false);
        
        //6)Task(4) started after unshared task(3) started from shared task(2) which is started from other shared task (1)
        //  has to have new CompilationInfo but the task (1) (2) (3) have to have the same CompilationInfo.
        js.runUserActionTask(new Task<CompilationController>() {                        
            public void run (CompilationController control) {
                delegateRef[0] = control.delegate;
                try {
                    js.runUserActionTask(new Task<CompilationController> () {
                        public void run (CompilationController control) {
                            assertTrue (delegateRef[0] == control.delegate);
                            try {
                                js.runUserActionTask(new Task<CompilationController> () {
                                    public void run (CompilationController control) {
                                        assertTrue (delegateRef[0] == control.delegate);
                                    }
                                }, false);
                                js.runUserActionTask(new Task<CompilationController> () {
                                    public void run (CompilationController control) {
                                        assertTrue (delegateRef[0] != control.delegate);
                                    }
                                }, true);
                            } catch (IOException ioe) {
                                RuntimeException re = new RuntimeException ();
                                re.initCause(ioe);
                                throw re;
                            }
                        }
                    }, true);
                } catch (IOException ioe) {
                    RuntimeException re = new RuntimeException ();
                    re.initCause(ioe);
                    throw re;
                }
            }            
        }, true);
        
    }
    
    public void testCouplingErrors() throws Exception {
        File workdir = this.getWorkDir();
        File src1File = new File (workdir, "src1");
        src1File.mkdir();
        final FileObject src1 = FileUtil.toFileObject(src1File);
        
        File src2File = new File (workdir, "src2");
        src2File.mkdir();
        final FileObject src2 = FileUtil.toFileObject(src2File);
        
        createTestFile(src1, "test/Test.java", "package test; public class Test {private long x;}");
        
        final FileObject test = createTestFile(src2, "test/Test.java", "package test; public class Test {private int x;}");
        final FileObject test2 = createTestFile(src2, "test/Test2.java", "package test; public class Test2 {private Test x;}");
        
        File cache = new File(workdir, "cache");
        
        cache.mkdirs();
        
        SourceUtilsTestUtil2.disableLocks();
        IndexUtil.setCacheFolder(cache);

        ClassLoader l = JavaSourceTest.class.getClassLoader();
        Lkp.DEFAULT.setLookupsWrapper(
                Lookups.metaInfServices(l),
                Lookups.singleton(l),
                Lookups.singleton(new ClassPathProvider() {
            public ClassPath findClassPath(FileObject file, String type) {
                try {
                    if (ClassPath.BOOT == type) {
                        return createBootPath();
                    }
                    
                    if (ClassPath.SOURCE == type) {
                        return ClassPathSupport.createClassPath(new FileObject[] {
                            src1
                        });
                    }
                    
                    if (ClassPath.COMPILE == type) {
                        return createCompilePath();
                    }
                    
                    if (ClassPath.EXECUTE == type) {
                        return ClassPathSupport.createClassPath(new FileObject[] {
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
            
        }));
        
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(src1, src1).await();
        
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = CacheClassPath.forSourcePath(ClassPathSupport.createClassPath(new FileObject[] {src1}));
        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath,compilePath,null);
        final JavaSource js = JavaSource.create(cpInfo, test2, test);
        
        final List<FileObject> files = new ArrayList<FileObject>();
        
        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController cc) throws IOException {
                files.add(cc.getPositionConverter().getFileObject());
                cc.toPhase(Phase.RESOLVED);
            }
        }, true);
        
        assertEquals(Arrays.asList(test2, test, test), files);
        
        files.clear();
        
        js.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy cc) throws IOException {
                files.add(cc.getPositionConverter().getFileObject());
                cc.toPhase(Phase.RESOLVED);
            }
        });
        
        assertEquals(Arrays.asList(test2, test, test), files);
    }
    
    
    public void testRunWhenScanFinished () throws Exception {        
        final FileObject testFile1 = createTestFile("Test1");
        CountDownLatch startL = RepositoryUpdater.getDefault().scheduleCompilationAndWait(testFile1.getParent(), testFile1.getParent());
        startL.await();        
        Thread.sleep (1000); //RU task already finished, but we want to wait until JS working thread is waiting on task to dispatch 
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = createCompilePath();
        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath,compilePath,null);
        final JavaSource js = JavaSource.create(cpInfo,testFile1);        
        
        class T implements Task<CompilationController> {
            
            private final CountDownLatch latch;
            
            public T (final CountDownLatch latch) {
                assert latch != null;
                this.latch = latch;
            }           

            public void run(CompilationController parameter) throws Exception {
                this.latch.countDown();
            }
            
        };
        
        class RUT implements CancellableTask<CompilationInfo> {
            private final CountDownLatch start;
            private final CountDownLatch latch;
            
            public RUT (final CountDownLatch start, final CountDownLatch latch) {
                assert start != null;
                assert latch != null;
                this.start = start;
                this.latch = latch;
            }

            public void cancel() {
            }

            public void run(CompilationInfo parameter) throws Exception {
                this.start.countDown();
                this.latch.await();
            }
            
        };
        
        CountDownLatch latch = new CountDownLatch (1);
        Future<Void> res = js.runWhenScanFinished(new T(latch), true);
        assertEquals(0,latch.getCount());
        res.get(1,TimeUnit.SECONDS);
        assertTrue(res.isDone());
        assertFalse (res.isCancelled());
        
        CountDownLatch rutLatch = new CountDownLatch (1);
        CountDownLatch rutStart = new CountDownLatch (1);        
        RUT rut = new RUT (rutStart, rutLatch);
        JavaSourceAccessor.INSTANCE.runSpecialTask(rut, JavaSource.Priority.MAX);
        latch = new CountDownLatch (1);
        rutStart.await();
        res = js.runWhenScanFinished(new T(latch), true);
        assertEquals(1,latch.getCount());
        res.get(1,TimeUnit.SECONDS);
        assertFalse(res.isDone());
        assertFalse (res.isCancelled());
        rutLatch.countDown();
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        res.get(1,TimeUnit.SECONDS);
        assertTrue(res.isDone());
        assertFalse (res.isCancelled());
        
        rutLatch = new CountDownLatch (1);
        rutStart = new CountDownLatch (1);
        rut = new RUT (rutStart, rutLatch);
        JavaSourceAccessor.INSTANCE.runSpecialTask(rut, JavaSource.Priority.MAX);
        latch = new CountDownLatch (1);
        rutStart.await();
        res = js.runWhenScanFinished(new T(latch), true);
        assertEquals(1,latch.getCount());
        res.get(1,TimeUnit.SECONDS);
        assertFalse(res.isDone());
        assertFalse (res.isCancelled());
        assertTrue (res.cancel(false));
        rutLatch.countDown();
        assertFalse(latch.await(3, TimeUnit.SECONDS));
        res.get(1,TimeUnit.SECONDS);
        assertFalse(res.isDone());
        assertTrue (res.isCancelled());
    }
    
    
    public void testNested2 () throws Exception {
        final FileObject testFile1 = createTestFile("Test1");
        final ClassPath bootPath = createBootPath();
        final ClassPath compilePath = createCompilePath();
        final ClasspathInfo cpInfo = ClasspathInfo.create(bootPath,compilePath,null);
        final JavaSource js = JavaSource.create(cpInfo,testFile1);
        js.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController c) throws Exception {
                c.toPhase(Phase.RESOLVED);
                CompilationUnitTree ct = c.getCompilationUnit();
                List <? extends Tree> trees = ct.getTypeDecls();
                assertEquals (1,trees.size());
                                
                js.runModificationTask(new Task<WorkingCopy>() {

                    public void run(WorkingCopy c) throws Exception {
                        c.toPhase(Phase.RESOLVED);
                        CompilationUnitTree oldTree = c.getCompilationUnit();
                        TreeMaker tm = c.getTreeMaker();
                        ClassTree cls = tm.Class(tm.Modifiers(EnumSet.of(Modifier.STATIC)), "NewClass", Collections.<TypeParameterTree>emptyList(),
                                null, Collections.<Tree>emptyList(), Collections.<Tree>emptyList());
                        List<Tree> decls = new LinkedList<Tree> ();
                        decls.addAll (oldTree.getTypeDecls());
                        decls.add (cls);
                        CompilationUnitTree newTree = tm.CompilationUnit(oldTree.getPackageName(), oldTree.getImports(),decls, oldTree.getSourceFile());
                        c.rewrite(oldTree, newTree);
                    }
                }).commit();
                                
                c.toPhase(Phase.RESOLVED);
                ct = c.getCompilationUnit();
                trees = ct.getTypeDecls();
                assertEquals (1, trees.size());
                                               
                js.runUserActionTask(new Task<CompilationController>() {

                    public void run(CompilationController c) throws Exception {
                        c.toPhase(Phase.RESOLVED);
                        CompilationUnitTree ct = c.getCompilationUnit();
                        List <? extends Tree> trees = ct.getTypeDecls();
                        assertEquals (2, trees.size());
                    }
                    
                }, true);
            }
            
        }, true);
    }
    
    
    public void testIndexCancel() throws Exception {
        PersistentClassIndex.setIndexFactory(new TestIndexFactory());
        try {
            FileObject test = createTestFile ("Test1");
            final ClassPath bootPath = createBootPath ();
            final ClassPath compilePath = createCompilePath ();        
            final ClassPath sourcePath = createSourcePath ();
            
            
            ClassLoader l = JavaSourceTest.class.getClassLoader();
            Lkp.DEFAULT.setLookupsWrapper(
                Lookups.metaInfServices(l),
                Lookups.singleton(l),
                Lookups.singleton(new ClassPathProvider() {
                public ClassPath findClassPath(FileObject file, String type) {
                    if (ClassPath.BOOT == type) {
                        return bootPath;
                    }

                    if (ClassPath.SOURCE == type) {
                        return sourcePath;
                    }

                    if (ClassPath.COMPILE == type) {
                        return compilePath;
                    }                    
                    return null;
                }            
            }));
            
            
            JavaSource js = JavaSource.create(ClasspathInfo.create(bootPath, compilePath, sourcePath), test);
            CountDownLatch rl = RepositoryUpdater.getDefault().scheduleCompilationAndWait(sourcePath.getRoots()[0], sourcePath.getRoots()[0]);
            rl.await();
            DataObject dobj = DataObject.find(test);
            EditorCookie ec = (EditorCookie) dobj.getCookie(EditorCookie.class);                        
            final StyledDocument doc = ec.openDocument();
            Thread.sleep(500);  //It may happen that the js is invalidated before the dispatch of task is done and the test of timers may fail        

            final CountDownLatch[] end = new CountDownLatch[]{new CountDownLatch (1)};
            final Object[] result = new Object[1];
            
            CancellableTask<CompilationInfo> task = new CancellableTask<CompilationInfo>() {

                public void cancel() {                
                }

                public void run(CompilationInfo p) throws Exception {
                    ClassIndex index = p.getClasspathInfo().getClassIndex();
                    result[0] = index.getPackageNames("javax", true, EnumSet.allOf(ClassIndex.SearchScope.class));                    
                    end[0].countDown();
                }

            };
            js.addPhaseCompletionTask (task,Phase.PARSED, Priority.HIGH);
            Thread.sleep(500);  //Making test a more deterministic, when the task is cancelled by DocListener, it's hard for test to recover from it
            NbDocument.runAtomic (doc,
                new Runnable () {
                    public void run () {                        
                        try {
                            String text = doc.getText(0,doc.getLength());
                            int index = text.indexOf(REPLACE_PATTERN);
                            assertTrue (index != -1);
                            doc.remove(index,REPLACE_PATTERN.length());
                            doc.insertString(index,"System.out.println();",null);
                        } catch (BadLocationException ble) {
                            ble.printStackTrace(System.out);
                        }                 
                    }
            });               
            end[0].await();
            assertNull(result[0]);
            js.removePhaseCompletionTask (task);
        } finally {
            PersistentClassIndex.setIndexFactory(null);
        }
    }
    
    
    private static class TestProvider implements JavaSource.JavaFileObjectProvider {
        
        private Object lock;
        
        public TestProvider (Object lock) {
            assert lock != null;
            this.lock = lock;
        }
        
        public JavaFileObject createJavaFileObject(FileObject fo, FileObject root, JavaFileFilterImplementation filter) throws IOException {
            return new TestJavaFileObject (fo, root, lock);
        }        
    }
    
    private static class TestJavaFileObject extends SourceFileObject {
        
        public TestJavaFileObject (FileObject fo, FileObject root, Object lock) throws IOException {            
            super (fo, root, null, true);
            //Deadlock
            synchronized (lock) {
                lock.toString();
            }
        }
    }
    
    private static class CompileControlJob implements Task<CompilationController> {
        
        private final CountDownLatch latch;
        boolean multiSource;
        
        public CompileControlJob (CountDownLatch latch) {
            this.latch = latch;
        }        
        
        public void run (CompilationController controler) {
            try {
                controler.toPhase(Phase.PARSED);
                if (!controler.delegate.needsRestart) {
                    assertTrue (Phase.PARSED.compareTo(controler.getPhase())<=0);
                    assertNotNull("No ComplationUnitTrees after parse",controler.getCompilationUnit());
                    controler.toPhase(Phase.RESOLVED);     
                    if (multiSource) {
                        if (controler.delegate.needsRestart) {
                            return;
                        }
                    }
                    assertTrue (Phase.RESOLVED.compareTo(controler.getPhase())<=0);

                    //all elements should be resolved now:
                    new ScannerImpl(controler).scan(controler.getCompilationUnit(), null);

                    controler.toPhase(Phase.PARSED);
                    //Was not modified should stay in {@link Phase#RESOLVED}
                    assertTrue (Phase.RESOLVED.compareTo(controler.getPhase())<=0);
                    this.latch.countDown();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    
    private static class ScannerImpl extends TreePathScanner<Void, Void> {
        
        private CompilationInfo info;
        
        public ScannerImpl(CompilationInfo info) {
            this.info = info;
        }
        
        public Void visitIdentifier(IdentifierTree node, Void p) {
            assertNotNull(info.getTrees().getElement(getCurrentPath()));
            return super.visitIdentifier(node, p);
        }

    }
    
    private static class WorkingCopyJob implements Task<WorkingCopy> {
        
        private final CountDownLatch latch;
        
        public WorkingCopyJob (CountDownLatch latch) {
            this.latch = latch;
        }
        
        public void run (WorkingCopy copy) throws IOException {
            copy.toPhase(Phase.RESOLVED);
            assertTrue (Phase.RESOLVED.compareTo(copy.getPhase())<=0);
            assertNotNull("No ComplationUnitTrees after parse",copy.getCompilationUnit());
            
            new TransformImpl(copy).scan(copy.getCompilationUnit(), null);

            this.latch.countDown();
        }
    }
    
    private static class TransformImpl extends TreeScanner<Void, Object> {

        private WorkingCopy copy;
        
        public TransformImpl(WorkingCopy copy) {
            this.copy = copy;
        }
        
        public Void visitClass(ClassTree node, Object p) {
            TreeMaker make = copy.getTreeMaker();
            ClassTree newNode = make.addClassMember(node, make.Variable(make.Modifiers(Collections.singleton(Modifier.PUBLIC)), "field", make.Identifier("int"), null));
            
            copy.rewrite(node, newNode);
            return null;
        }

    }
    
    private static class CompileControlJobWithOOM implements Task<CompilationController> {
        
        private final CountDownLatch latch;
        private final int oomFor;
        private int currentIndex;
        private URI uri;
        
        public CompileControlJobWithOOM (CountDownLatch latch, int oomFor) {
            this.latch = latch;
            this.oomFor = oomFor;
        }        
        
        public void run (CompilationController controler) {
            try {
                controler.toPhase(Phase.PARSED);
                assertTrue (Phase.PARSED.compareTo(controler.getPhase())<=0);
                CompilationUnitTree cut = controler.getCompilationUnit();
                assertNotNull("No ComplationUnitTree after parse",cut);            
                controler.toPhase(Phase.RESOLVED);           
                assertTrue (Phase.RESOLVED.compareTo(controler.getPhase())<=0);
                controler.toPhase(Phase.PARSED);
                //Was not modified should stay in {@link Phase#RESOLVED}
                assertTrue (Phase.RESOLVED.compareTo(controler.getPhase())<=0);                
                if (currentIndex == oomFor) {
                    Class<CompilationController> clazz = CompilationController.class;
                    Field delegateField = clazz.getDeclaredField("delegate");
                    delegateField.setAccessible(true);
                    CompilationInfo delegate = (CompilationInfo) delegateField.get(controler);
                    delegate.needsRestart = true;                    
                    uri = cut.getSourceFile().toUri();
                }
                if (currentIndex == oomFor+1) {
                    assertNotNull (uri);
                    assertEquals(uri,cut.getSourceFile().toUri());
                    uri = null;
                }
                this.latch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                this.currentIndex++;
            }
        }
    }
    
    
    private static class EmptyCompileControlJob implements Task<CompilationController> {
        
        private final CountDownLatch latch;
        
        public EmptyCompileControlJob (CountDownLatch latch) {
            this.latch = latch;
        }       
        
        public void run (CompilationController controler) {
            try {
                try {
                    //Should throw exception
                    assertEquals(Phase.PARSED, controler.toPhase(Phase.PARSED));
                } catch (IllegalStateException e) {                    
                }
                try {
                    //Should throw exception
                    controler.getCompilationUnit();
                    throw new AssertionError ();
                } catch (IllegalStateException e) {                    
                }
                controler.getPhase();
                controler.getTypes();
                controler.getTrees();
                controler.getElements();
                controler.getClasspathInfo();
                controler.getJavaSource();
                this.latch.countDown();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
    
    
    
    private static boolean waitForMultipleObjects (CountDownLatch[] objects, int timeOut) throws InterruptedException {
        for (CountDownLatch latch : objects) {
            long ctms = System.currentTimeMillis();
            if (!latch.await(timeOut, TimeUnit.MILLISECONDS)) {
                return false;
            }
            long ctme = System.currentTimeMillis ();
            timeOut -= (ctme - ctms);
        }
        return true;
    }
    
    
    private static class DiagnosticTask implements CancellableTask<CompilationInfo> {
        
        private final Phase expectedPhase;
        private final CountDownLatch[] latches;
        private final long[] times;
        private final AtomicInteger counter;
        private int currentLatch;
        private int cancelCount;
        boolean verbose;
        
        public DiagnosticTask (final CountDownLatch[] latches, final AtomicInteger counter, final Phase expectedPhase) {
            this(latches, new long[latches.length], counter, expectedPhase);
        }
        
        public DiagnosticTask (final CountDownLatch[] latches, final long[] times, final AtomicInteger counter, final Phase expectedPhase) {
            assertEquals(times.length, latches.length);
            this.latches = latches;
            this.times = times;            
            this.counter = counter;
            this.expectedPhase = expectedPhase;
        }
        
        public synchronized void run(CompilationInfo parameter) {
            if (verbose) {
                System.out.println("run called");
            }
            if (this.cancelCount>0) {
                this.cancelCount--;
                if (verbose) {
                    System.out.println("Cancel count: " + cancelCount);
                }
                return;
            }
            if (this.counter != null) {
                int current = this.counter.incrementAndGet();
                if (verbose) {
                    System.out.println("counter="+current);
                }
            }
            if (this.currentLatch < this.times.length) {
                if (verbose) {
                    System.out.println("Firing current latch: " + this.currentLatch);
                }
                this.times[this.currentLatch] = System.currentTimeMillis();
                this.latches[this.currentLatch++].countDown();
            }
            assertNotNull (parameter);
            assertTrue (String.format("Got wrong state, expected: %s got: %s", expectedPhase.name(), parameter.getPhase().name()), this.expectedPhase.compareTo(parameter.getPhase())<=0);
        }

        public synchronized void cancel() {             
            this.cancelCount++;
            if (verbose) {
                System.out.println("cancel called: " + cancelCount);
            }
        }
        
    }
    
    
    private static class WaitTask implements CancellableTask<CompilationInfo> {
        
        private long milisToWait;
        private int cancelCount;
        private int runCount;
        
        public WaitTask (long milisToWait) {
            this.milisToWait = milisToWait;
            this.cancelCount = 0;
            this.runCount = 0;
        }
        
        public void run (CompilationInfo info) {
            this.runCount++;
            if (this.milisToWait>0) {
                try {
                    Thread.sleep(this.milisToWait);
                } catch (InterruptedException ie) {}
            }
        }
        
        public void cancel () {
            this.cancelCount++;
        }
        
        public int getCancelCount () {
            return this.cancelCount;
        }
        
        public int getRunCount () {
            return this.runCount;
        }
        
    }
    
    private static class TestIndexFactory implements IndexFactory {        
        
        public Index create(File cacheRoot) throws IOException {
            return new TestIndex ();
        }
        
    }
    
    private static class TestIndex extends Index {
        
        
        public TestIndex () {
        }

        public boolean isValid(boolean tryOpen) throws IOException {
            return true;
        }

        public List<String> getUsagesFQN(String resourceName, Set<UsageType> mask, BooleanOperator operator) throws IOException, InterruptedException {
            await ();
            return Collections.<String>emptyList();
        }

        public <T> void getDeclaredTypes(String simpleName, NameKind kind, ResultConvertor<T> convertor, Set<? super T> result) throws IOException, InterruptedException {
            await ();
        }

        public void getPackageNames(String prefix, boolean directOnly, Set<String> result) throws IOException, InterruptedException {
            await ();
        }
        
        public String getSourceName (final String binaryName) {
            return null;
        }

        public void store(Map<String, Pair<String,List<String>>> refs, Set<String> toDelete) throws IOException {            
        }

        public void store(Map<String, Pair<String,List<String>>> refs, List<String> topLevels) throws IOException {            
        }

        public boolean isUpToDate(String resourceName, long timeStamp) throws IOException {
            return true;
        }

        public void clear() throws IOException {            
        }

        public void close() throws IOException {            
        }
        
        private void await () throws InterruptedException {
            AtomicBoolean cancel = this.cancel.get();
            while (true) {
                if (cancel.get()) {
                    throw new InterruptedException ();
                }
                Thread.sleep(100);
            }
        }
        
    }
    
    private FileObject createTestFile (String className) {
        try {
            File workdir = this.getWorkDir();
            File root = new File (workdir, "src");
            root.mkdir();
            File data = new File (root, className+".java");
        
            PrintWriter out = new PrintWriter (new FileWriter (data));
            try {
                out.println(MessageFormat.format(TEST_FILE_CONTENT, new Object[] {className}));
            } finally {
                out.close ();
            }
            return FileUtil.toFileObject(data);
        } catch (IOException ioe) {
            return null;
        }
    }
    
    private ClassPath createBootPath () throws MalformedURLException {
        String bootPath = System.getProperty ("sun.boot.class.path");
        String[] paths = bootPath.split(File.pathSeparator);
        List<URL>roots = new ArrayList<URL> (paths.length);
        for (String path : paths) {
            File f = new File (path);            
            if (!f.exists()) {
                continue;
            }
            URL url = f.toURI().toURL();
            if (FileUtil.isArchiveFile(url)) {
                url = FileUtil.getArchiveRoot(url);
            }
            roots.add (url);
        }
        return ClassPathSupport.createClassPath(roots.toArray(new URL[roots.size()]));
    }
    
    private ClassPath createCompilePath () {
        return ClassPathSupport.createClassPath(Collections.EMPTY_LIST);
    }
    
    private ClassPath createSourcePath () throws IOException {
        File workdir = this.getWorkDir();
        File root = new File (workdir, "src");
        if (!root.exists()) {
            root.mkdirs();
        }
        return ClassPathSupport.createClassPath(new URL[] {root.toURI().toURL()});
    }
    
    private FileObject createTestFile (FileObject srcRoot, String relativeName, String content) throws IOException {
        FileObject f = FileUtil.createData(srcRoot, relativeName);
        Writer out = new OutputStreamWriter(f.getOutputStream());
        
        try {
            out.write(content);
        } finally {
            out.close();
        }
        
        return f;
    }
}
