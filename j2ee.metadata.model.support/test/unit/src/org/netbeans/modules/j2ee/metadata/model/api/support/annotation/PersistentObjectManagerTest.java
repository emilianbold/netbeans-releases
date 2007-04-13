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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ClassIndexListener;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.RootsEvent;
import org.netbeans.api.java.source.TypesEvent;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.j2ee.metadata.model.support.PersistenceTestCase;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Andrei Badea
 */
public class PersistentObjectManagerTest extends PersistenceTestCase {

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
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        final EntityImpl[] employeeEntity = { null };
        final EntityImpl[] addressEntity = { null };
        helper.userActionTask(new Runnable() {
            public void run() {
                manager = helper.createPersistentObjectManager(new EntityProvider(helper));
                manager.initialize();
                for (EntityImpl entity : manager.getObjects()) {
                    if ("Employee".equals(entity.getName())) {
                        employeeEntity[0] = entity;
                    } else if ("Address".equals(entity.getName())) {
                        addressEntity[0] = entity;
                    }
                }
                assertNotNull(employeeEntity[0]);
                assertNotNull(addressEntity[0]);
            }
        });
        final AtomicBoolean departmentAdded = new AtomicBoolean();
        final AtomicBoolean addressRemoved = new AtomicBoolean();
        final AtomicBoolean employeeChanged = new AtomicBoolean();
        final AtomicBoolean departmentChanged = new AtomicBoolean();
        final CountDownLatch latch = new CountDownLatch(4);
        cpi.getClassIndex().addClassIndexListener(new ClassIndexListener() {
            public void rootsAdded(RootsEvent event) {
            }
            public void rootsRemoved(RootsEvent event) {
            }
            public void typesAdded(TypesEvent event) {
                for (ElementHandle<TypeElement> type : event.getTypes()) {
                    if ("foo.Department".equals(type.getQualifiedName())) {
                        departmentAdded.set(true);
                        latch.countDown();
                    }
                }
                assertTrue("Should not have got an empty added event ", event.getTypes().iterator().hasNext());
            }
            public void typesChanged(TypesEvent event) {
                for (ElementHandle<TypeElement> type : event.getTypes()) {
                    if ("foo.Employee".equals(type.getQualifiedName())) {
                        employeeChanged.set(true);
                        latch.countDown();
                    }
                    if ("foo.Department".equals(type.getQualifiedName())) {
                        departmentChanged.set(true);
                        latch.countDown();
                    }
                }
                assertTrue("Should not have got an empty changed event ", event.getTypes().iterator().hasNext());
            }
            public void typesRemoved(TypesEvent event) {
                for (ElementHandle<TypeElement> type : event.getTypes()) {
                    if ("foo.Address".equals(type.getQualifiedName())) {
                        addressRemoved.set(true);
                        latch.countDown();
                    }
                }
                assertTrue("Should not have got an empty removed event ", event.getTypes().iterator().hasNext());
            }
        });
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
        latch.await(5000, TimeUnit.MILLISECONDS);
        assertTrue("Should have got a typesAdded event for Department", departmentAdded.get());
        assertTrue("Should have got a typesRemoved event for Address ", addressRemoved.get());
        assertTrue("Should have got a typesChanged event for Employee", employeeChanged.get());
        assertTrue("Should have got a typesChanged event for Department", departmentChanged.get());
        helper.userActionTask(new Runnable() {
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
    }

    private static final class EntityProvider implements ObjectProvider<EntityImpl> {

        private final AnnotationModelHelper helper;

        public EntityProvider(AnnotationModelHelper helper) {
            this.helper = helper;
        }

        public List<EntityImpl> createInitialObjects() {
            final List<EntityImpl> result = new ArrayList<EntityImpl>();
            helper.getAnnotationScanner().findAnnotatedTypes("javax.persistence.Entity", new TypeAnnotationHandler() {
                public void typeAnnotation(TypeElement type, AnnotationMirror annotation) {
                    result.add(new EntityImpl(helper, type));
                }
            });
            return result;
        }

        public List<EntityImpl> createObjects(List<TypeElement> types) {
            List<EntityImpl> result = new ArrayList<EntityImpl>();
            for (TypeElement type : types) {
                if (helper.hasAnnotation(type.getAnnotationMirrors(), "javax.persistence.Entity")) {
                    result.add(new EntityImpl(helper, type));
                }
            }
            return result;
        }
    }

    private static final class EntityImpl extends PersistentObject {

        private String name;

        public EntityImpl(AnnotationModelHelper helper, TypeElement typeElement) {
            super(helper, typeElement);
            readPersistentData(typeElement);
        }

        protected void sourceElementChanged() {
            readPersistentData(getSourceElement());
        }

        private void readPersistentData(TypeElement typeElement) {
            AnnotationParser parser = AnnotationParser.create(getHelper());
            parser.expectString("name", parser.defaultValue(typeElement.getSimpleName()));
            name = parser.parse(typeElement.getAnnotationMirrors().get(0)).get("name", String.class);
        }

        public String getName() {
            return name;
        }
    }
}
