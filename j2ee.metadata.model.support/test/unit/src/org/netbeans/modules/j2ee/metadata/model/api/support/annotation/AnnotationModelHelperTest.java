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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.metadata.model.api.support.annotation;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndexListener;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.RootsEvent;
import org.netbeans.api.java.source.TypesEvent;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.j2ee.metadata.model.support.PersistenceTestCase;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;

/**
 *
 * @author Andrei Badea
 */
public class AnnotationModelHelperTest extends PersistenceTestCase {

    public AnnotationModelHelperTest(String testName) {
        super(testName);
    }

    public void testUserActionTask() throws Exception {
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        final String expected = "foo";
        String returned = helper.runJavaSourceTask(new Callable<String>() {
            public String call() throws Exception {
                assertNotNull(helper.getCompilationController());
                return expected;
            }
        });
        assertEquals(expected, returned);
    }

    public void testGetSuperclasses() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "Person.java",
                "public interface Person {" +
                "   String getName();" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "Employee.java",
                "@javax.persistence.MappedSuperclass()" +
                "public class Employee implements Person {" +
                "   public String getName() {" +
                "       return null;" +
                "   }" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "PartTimeEmployee.java",
                "@javax.persistence.Entity()" +
                "public class PartTimeEmployee extends Employee {" +
                "   public int getHoursPerDay() {" +
                "       return 0;" +
                "   }" +
                "}");
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                TypeElement pte = helper.getCompilationController().getElements().getTypeElement("PartTimeEmployee");
                List<? extends TypeElement> superclasses = helper.getSuperclasses(pte);
                assertEquals(1, superclasses.size());
                assertTrue(superclasses.get(0).getQualifiedName().contentEquals("Employee"));
            }
        });
    }

    public void testJavaContextListener() throws Exception {
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        final boolean[] contextLeft = { false };
        JavaContextListener listener = new JavaContextListener() {
            public void javaContextLeft() {
                contextLeft[0] = true;
            }
        };
        helper.addJavaContextListener(listener);
        Callable<Void> empty = new Callable<Void>() {
            public Void call() {
                return null;
            }
        };
        helper.runJavaSourceTask(empty);
        assertTrue(contextLeft[0]);
        contextLeft[0] = false;
        helper.runJavaSourceTask(empty, false);
        assertFalse(contextLeft[0]);
        WeakReference<JavaContextListener> listenerRef = new WeakReference<JavaContextListener>(listener);
        listener = null;
        assertGC("Should be possible to GC listener", listenerRef);
    }

    public void testRecursiveUserActionTask() throws Exception {
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        helper.runJavaSourceTask(new Callable<Void>() {
            public Void call() throws IOException {
                final JavaSource js1 = helper.javaSource;
                helper.runJavaSourceTask(new Callable<Void>() {
                    public Void call() {
                        JavaSource js2 = helper.javaSource;
                        assertSame(js1, js2);
                        return null;
                    }
                });
                return  null;
            }
        });
    }

    public void testGetCompilationControllerFromAnotherThread() throws Exception {
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        final CountDownLatch latch1 = new CountDownLatch(1);
        final CountDownLatch latch2 = new CountDownLatch(1);
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    latch1.await();
                } catch (InterruptedException e) {}
                try {
                    helper.getCompilationController();
                    fail();
                } catch (IllegalStateException e) {}
                latch2.countDown();
            }
        });
        t.start();
        helper.runJavaSourceTask(new Callable<Void>() {
            public Void call() throws Exception {
                latch1.countDown();
                try {
                    latch2.await();
                } catch (InterruptedException e) {}
                return null;
            }
        });
        t.join();
    }

    public void testUserActionTaskSingleThread() throws Exception {
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        final CountDownLatch latch = new CountDownLatch(1);
        final Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    latch.await();
                } catch (InterruptedException e) {}
                try {
                    helper.runJavaSourceTask(new Callable<Void>() {
                        public Void call() throws Exception {
                            fail();
                            return null;
                        }
                    });
                } catch (IOException e) {}
            }
        });
        t.start();
        helper.runJavaSourceTask(new Callable<Void>() {
            public Void call() throws Exception {
                latch.countDown();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {}
                t.interrupt();
                return null;
            }
        });
    }

    public void testWhenScanFinished() throws Exception {
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        ClassIndex index = cpi.getClassIndex();
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch scanBlockingLatch = new CountDownLatch(1);
        index.addClassIndexListener(new ClassIndexAdapter() {
            public void typesAdded(TypesEvent event) {
                // called as a result of scheduleCompilationAndWait()
                startLatch.countDown();
                try {
                    scanBlockingLatch.await();
                } catch (InterruptedException e) {}
            }
        });
        final String result = "result";
        final AtomicReference<Future<String>> futureRef = new AtomicReference<Future<String>>();
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    startLatch.await();
                } catch (InterruptedException e) {}
                try {
                    futureRef.set(helper.runJavaSourceTaskWhenScanFinished(new Callable<String>() {
                        public String call() throws Exception {
                            return result;
                        }
                    }));
                } catch (IOException e) {
                    throw new Error(e);
                }
                assertFalse(futureRef.get().isDone());
                assertFalse(futureRef.get().isCancelled());
                scanBlockingLatch.countDown();
            }
        });
        t.start();
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        TestUtilities.copyStringToFileObject(srcFO, "Person.java",
                "public interface Person {" +
                "   String getName();" +
                "}");
        scanBlockingLatch.await(5, TimeUnit.SECONDS);
        assertSame(result, futureRef.get().get());
    }
}
