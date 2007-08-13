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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.source.ClassIndexListener;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.RootsEvent;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TypesEvent;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.j2ee.metadata.model.support.PersistenceTestCase;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.test.MockChangeListener;

/**
 *
 * @author Andrei Badea
 */
public class PersistentObjectManagerTest extends PersistenceTestCase {
    
    private static final int EVENT_TIMEOUT = 10; // seconds

    private PersistentObjectManager<EntityImpl> manager;

    public PersistentObjectManagerTest(String testName) {
        super(testName);
    }

    protected void tearDown() {
        manager = null;
    }

    public void testBasic() throws Exception {
        FileObject employeeFO = TestUtilities.copyStringToFileObject(srcFO, "foo/Employee.java",
                "package foo;" +
                "@javax.persistence.Entity(name = \"Employee\")" +
                "public class Employee {" +
                "}");
        FileObject addressFO = TestUtilities.copyStringToFileObject(srcFO, "foo/Address.java",
                "package foo;" +
                "@javax.persistence.Entity(name = \"Address\")" +
                "public class Address {" +
                "}");
        GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, new ClassPath[] { ClassPath.getClassPath(srcFO, ClassPath.SOURCE) });
        GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, new ClassPath[] { ClassPath.getClassPath(srcFO, ClassPath.COMPILE) });
        GlobalPathRegistry.getDefault().register(ClassPath.BOOT, new ClassPath[] { ClassPath.getClassPath(srcFO, ClassPath.BOOT) });
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        final MockChangeListener changeListener = new MockChangeListener();
        final EntityImpl[] employeeEntity = { null };
        final EntityImpl[] addressEntity = { null };
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                manager = helper.createPersistentObjectManager(new EntityProvider(helper));
                manager.addChangeListener(changeListener);
                for (EntityImpl entity : manager.getObjects()) {
                    if ("Employee".equals(entity.getName())) {
                        employeeEntity[0] = entity;
                    } else if ("Address".equals(entity.getName())) {
                        addressEntity[0] = entity;
                    }
                }
                assertNotNull(employeeEntity[0]);
                assertNotNull(addressEntity[0]);
                assertFalse(manager.temporary); // we are testing events, so we don't want a temporary manager
            }
        });
        // adding, removing and changing some types
        final AtomicBoolean departmentAdded = new AtomicBoolean();
        final AtomicBoolean addressRemoved = new AtomicBoolean();
        final AtomicBoolean employeeChanged = new AtomicBoolean();
        final AtomicBoolean departmentChanged = new AtomicBoolean();
        final CountDownLatch typesLatch = new CountDownLatch(4);
        ClassIndexListener listener = new ClassIndexAdapter() {
            public void typesAdded(TypesEvent event) {
                for (ElementHandle<TypeElement> type : event.getTypes()) {
                    if ("foo.Department".equals(type.getQualifiedName())) {
                        departmentAdded.set(true);
                        typesLatch.countDown();
                    }
                }
                assertTrue("Should not have got an empty added event ", event.getTypes().iterator().hasNext());
            }
            public void typesChanged(TypesEvent event) {
                for (ElementHandle<TypeElement> type : event.getTypes()) {
                    if ("foo.Employee".equals(type.getQualifiedName())) {
                        employeeChanged.set(true);
                        typesLatch.countDown();
                    }
                    if ("foo.Department".equals(type.getQualifiedName())) {
                        departmentChanged.set(true);
                        typesLatch.countDown();
                    }
                }
                assertTrue("Should not have got an empty changed event ", event.getTypes().iterator().hasNext());
            }
            public void typesRemoved(TypesEvent event) {
                for (ElementHandle<TypeElement> type : event.getTypes()) {
                    if ("foo.Address".equals(type.getQualifiedName())) {
                        addressRemoved.set(true);
                        typesLatch.countDown();
                    }
                }
                assertTrue("Should not have got an empty removed event ", event.getTypes().iterator().hasNext());
            }
        };
        cpi.getClassIndex().addClassIndexListener(listener);
        TestUtilities.copyStringToFileObject(employeeFO,
                "package foo;" +
                "@javax.persistence.Entity(name = \"NewEmployee\")" +
                "public class Employee {" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Department.java",
                "package foo;" +
                "@javax.persistence.Entity(name=\"Department\")" +
                "public class Department {" +
                "}");
        addressFO.delete();
        typesLatch.await(EVENT_TIMEOUT, TimeUnit.SECONDS);
        assertTrue("Should have got a typesAdded event for Department", departmentAdded.get());
        assertTrue("Should have got a typesRemoved event for Address ", addressRemoved.get());
        assertTrue("Should have got a typesChanged event for Employee", employeeChanged.get());
        assertTrue("Should have got a typesChanged event for Department", departmentChanged.get());
        cpi.getClassIndex().removeClassIndexListener(listener);
        changeListener.assertEvent();
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                Collection<EntityImpl> entities = manager.getObjects();
                assertEquals(2, entities.size());
                assertTrue(entities.contains(employeeEntity[0]));
                assertFalse(entities.contains(addressEntity[0]));
                assertEquals("NewEmployee", employeeEntity[0].getName());
                boolean hasDepartmentEntity = false;
                for (EntityImpl entity : entities) {
                    if ("Department".equals(entity.getName())) {
                        hasDepartmentEntity = true;
                    }
                }
                assertTrue(hasDepartmentEntity);
            }
        });
        // adding a new root
        final FileObject src2FO = FileUtil.toFileObject(getWorkDir()).createFolder("src2");
        FileObject productFO = TestUtilities.copyStringToFileObject(src2FO, "foo/Product.java",
                "package foo;" +
                "@javax.persistence.Entity(name = \"Product\")" +
                "public class Product {" +
                "}");
        FileObject orderFO = TestUtilities.copyStringToFileObject(src2FO, "foo/Order.java",
                "package foo;" +
                "@javax.persistence.Entity(name = \"Order\")" +
                "public class Order {" +
                "}");
        final AtomicBoolean rootAdded = new AtomicBoolean();
        final CountDownLatch rootAddedLatch = new CountDownLatch(1);
        listener = new ClassIndexAdapter() {
            public void rootsAdded(RootsEvent event) {
                URL src2URL = URLMapper.findURL(src2FO, URLMapper.INTERNAL);
                for (URL url : event.getRoots()) {
                    if (src2URL.equals(url)) {
                        rootAdded.set(true);
                        rootAddedLatch.countDown();
                    }
                }
                assertTrue("Should not have got an empty roots added event", event.getRoots().iterator().hasNext());
            }
        };
        cpi.getClassIndex().addClassIndexListener(listener);
        addSourceRoots(Collections.singletonList(src2FO));
        rootAddedLatch.await(EVENT_TIMEOUT, TimeUnit.SECONDS);
        assertTrue("Should have got a rootsAdded event", rootAdded.get());
        cpi.getClassIndex().removeClassIndexListener(listener);
        changeListener.assertEvent();
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                Collection<EntityImpl> entities = manager.getObjects();
                boolean hasProductEntity = false;
                boolean hasOrderEntity = false;
                for (EntityImpl entity : entities) {
                    if ("Product".equals(entity.getName())) {
                        hasProductEntity = true;
                    } else if ("Order".equals(entity.getName())) {
                        hasOrderEntity = true;
                    }
                }
                assertTrue(hasProductEntity);
                assertTrue(hasOrderEntity);
            }
        });
        // removing a root
        final AtomicBoolean rootRemoved = new AtomicBoolean();
        final CountDownLatch rootRemovedLatch = new CountDownLatch(1);
        listener = new ClassIndexAdapter() {
            public void rootsRemoved(RootsEvent event) {
                URL src2URL = URLMapper.findURL(src2FO, URLMapper.INTERNAL);
                for (URL url : event.getRoots()) {
                    if (src2URL.equals(url)) {
                        rootRemoved.set(true);
                        rootRemovedLatch.countDown();
                    }
                }
                assertTrue("Should not have got an empty roots removed event", event.getRoots().iterator().hasNext());
            }
        };
        cpi.getClassIndex().addClassIndexListener(listener);
        removeSourceRoots(Collections.singletonList(src2FO));
        rootRemovedLatch.await(EVENT_TIMEOUT, TimeUnit.SECONDS);
        assertTrue("Should have got a rootsRemoved event", rootRemoved.get());
        cpi.getClassIndex().removeClassIndexListener(listener);
        changeListener.assertEvent();
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                Collection<EntityImpl> entities = manager.getObjects();
                boolean hasProductEntity = false;
                boolean hasOrderEntity = false;
                for (EntityImpl entity : entities) {
                    if ("Product".equals(entity.getName())) {
                        hasProductEntity = true;
                    } else if ("Order".equals(entity.getName())) {
                        hasOrderEntity = true;
                    }
                }
                assertFalse(hasProductEntity);
                assertFalse(hasOrderEntity);
            }
        });
    }

    public void testChangedFiles() throws Exception {
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        final MockChangeListener changeListener = new MockChangeListener();
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                manager = helper.createPersistentObjectManager(new EntityProvider(helper));
                manager.addChangeListener(changeListener);
            }
        });
        // adding a class which is not an entity class
        final AtomicBoolean departmentAdded = new AtomicBoolean();
        final CountDownLatch addedLatch = new CountDownLatch(1);
        ClassIndexListener listener = new ClassIndexAdapter() {
            public void typesAdded(TypesEvent event) {
                for (ElementHandle<TypeElement> type : event.getTypes()) {
                    if ("foo.Department".equals(type.getQualifiedName())) {
                        departmentAdded.set(true);
                        addedLatch.countDown();
                    }
                }
            }
        };
        cpi.getClassIndex().addClassIndexListener(listener);
        TestUtilities.copyStringToFileObject(srcFO, "foo/Department.java",
                "package foo;" +
                "public class Department {" +
                "}");
        addedLatch.await(EVENT_TIMEOUT, TimeUnit.SECONDS);
        assertTrue("Should have got a typesAdded event for Department", departmentAdded.get());
        cpi.getClassIndex().removeClassIndexListener(listener);
        SourceUtils.waitScanFinished(); // otherwise the PMO will initialize temporarily
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                assertEquals(0, manager.getObjects().size());
                assertFalse(manager.temporary);
            }
        });
        // modifying the class to be an entity class
        final AtomicBoolean departmentChanged = new AtomicBoolean();
        final CountDownLatch changedLatch = new CountDownLatch(1);
        listener = new ClassIndexAdapter() {
            public void typesChanged(TypesEvent event) {
                for (ElementHandle<TypeElement> type : event.getTypes()) {
                    if ("foo.Department".equals(type.getQualifiedName())) {
                        departmentChanged.set(true);
                        changedLatch.countDown();
                    }
                }
            }
        };
        cpi.getClassIndex().addClassIndexListener(listener);
        TestUtilities.copyStringToFileObject(srcFO, "foo/Department.java",
                "package foo;" +
                "@javax.persistence.Entity(name=\"Department\")" +
                "public class Department {" +
                "}");
        changedLatch.await(EVENT_TIMEOUT, TimeUnit.SECONDS);
        assertTrue("Should have got a typesChanged event for Department", departmentChanged.get());
        cpi.getClassIndex().removeClassIndexListener(listener);
        changeListener.assertEvent();
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                assertEquals(1, manager.getObjects().size());
            }
        });
        // modifying the class to be an non-entity class
        final AtomicBoolean departmentChanged2 = new AtomicBoolean();
        final CountDownLatch changedLatch2 = new CountDownLatch(1);
        listener = new ClassIndexAdapter() {
            public void typesChanged(TypesEvent event) {
                for (ElementHandle<TypeElement> type : event.getTypes()) {
                    if ("foo.Department".equals(type.getQualifiedName())) {
                        departmentChanged2.set(true);
                        changedLatch2.countDown();
                    }
                }
            }
        };
        cpi.getClassIndex().addClassIndexListener(listener);
        TestUtilities.copyStringToFileObject(srcFO, "foo/Department.java",
                "package foo;" +
                "public class Department {" +
                "}");
        changedLatch2.await(EVENT_TIMEOUT, TimeUnit.SECONDS);
        assertTrue("Should have got a typesChanged event for Department", departmentChanged2.get());
        cpi.getClassIndex().removeClassIndexListener(listener);
        changeListener.assertEvent();
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                assertEquals(0, manager.getObjects().size());
            }
        });
    }

    private static final class EntityProvider implements ObjectProvider<EntityImpl> {

        private final AnnotationModelHelper helper;

        public EntityProvider(AnnotationModelHelper helper) {
            this.helper = helper;
        }

        public List<EntityImpl> createInitialObjects() throws InterruptedException {
            final List<EntityImpl> result = new ArrayList<EntityImpl>();
            helper.getAnnotationScanner().findAnnotations("javax.persistence.Entity", AnnotationScanner.TYPE_KINDS, new AnnotationHandler() {
                public void handleAnnotation(TypeElement type, Element element, AnnotationMirror annotation) {
                    result.add(new EntityImpl(helper, type));
                }
            });
            return result;
        }

        public List<EntityImpl> createObjects(TypeElement type) {
            if (helper.hasAnnotation(type.getAnnotationMirrors(), "javax.persistence.Entity")) {
                return Collections.singletonList(new EntityImpl(helper, type));
            }
            return Collections.emptyList();
        }

        public boolean modifyObjects(TypeElement type, List<EntityImpl> objects) {
            assert objects.size() == 1;
            EntityImpl entity = objects.get(0);
            if (!entity.refresh(type)) {
                objects.remove(0);
                return true;
            }
            return false;
        }
    }

    private static final class EntityImpl extends PersistentObject {

        private String name;

        public EntityImpl(AnnotationModelHelper helper, TypeElement typeElement) {
            super(helper, typeElement);
            boolean valid = refresh(typeElement);
            assert valid;
        }

        private boolean refresh(TypeElement typeElement) {
            AnnotationParser parser = AnnotationParser.create(getHelper());
            parser.expectString("name", parser.defaultValue(typeElement.getSimpleName()));
            List<? extends AnnotationMirror> annotations = typeElement.getAnnotationMirrors();
            if (annotations.size() == 0) {
                return false;
            }
            name = parser.parse(annotations.get(0)).get("name", String.class);
            return true;
        }

        public String getName() {
            return name;
        }
    }
}
