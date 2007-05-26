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
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ClassIndexListener;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TypesEvent;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.support.PersistenceTestCase;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;

/**
 * Tests if PersistentObjectManager caches multiple objects based on
 * the same TypeElement correctly.
 *
 * @author Andrei Badea
 */
public class PersistentObjectManagerMultipleTest extends PersistenceTestCase {

    private PersistentObjectManager<ResourceImpl> manager;

    public PersistentObjectManagerMultipleTest(String name) {
        super(name);
    }

    public void testChangedFiles() throws Exception {
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                manager = helper.createPersistentObjectManager(new ResourceProvider(helper));
            }
        });
        // adding a class with two resources
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
                "import javax.annotation.*;" +
                "public class Department {" +
                "   @Resource(name = \"foo\")" +
                "   private Object foo;" +
                "   @Resource(name = \"bar\")" +
                "   private Object bar;" +
                "}");
        addedLatch.await(10, TimeUnit.SECONDS);
        assertTrue("Should have got a typesAdded event for Department", departmentAdded.get());
        cpi.getClassIndex().removeClassIndexListener(listener);
        SourceUtils.waitScanFinished(); // otherwise the PMO will initialize temporarily
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                assertEquals(2, manager.getObjects().size());
                assertFalse(manager.temporary);
            }
        });
        // removing one of the resources
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
                "import javax.annotation.*;" +
                "public class Department {" +
                "   @Resource(name = \"bar\")" +
                "   private Object bar;" +
                "}");
        changedLatch.await(10, TimeUnit.SECONDS);
        assertTrue("Should have got a typesChanged event for Department", departmentChanged.get());
        cpi.getClassIndex().removeClassIndexListener(listener);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                assertEquals(1, manager.getObjects().size());
            }
        });
    }

    private static final class ResourceProvider implements ObjectProvider<ResourceImpl> {

        private final AnnotationModelHelper helper;

        public ResourceProvider(AnnotationModelHelper helper) {
            this.helper = helper;
        }

        public List<ResourceImpl> createInitialObjects() {
            final List<ResourceImpl> result = new ArrayList<ResourceImpl>();
            TypeElement departmentType = helper.getCompilationController().getElements().getTypeElement("foo.Department");
            for (Element element : departmentType.getEnclosedElements()) {
                String elementName = element.getSimpleName().toString();
                if ("foo".equals(elementName) || "bar".equals(elementName)) {
                    result.add(new ResourceImpl(helper, departmentType, element));
                }
            }
            return result;
        }

        public List<ResourceImpl> createObjects(TypeElement type) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class ResourceImpl extends PersistentObject {

        private final ElementHandle<Element> resourceElement;
        private String name;

        public ResourceImpl(AnnotationModelHelper helper, TypeElement typeElement, Element element) {
            super(helper, typeElement);
            resourceElement = ElementHandle.create(element);
        }

        protected boolean sourceElementChanged() {
            return readPersistentData(getSourceElement());
        }

        private boolean readPersistentData(TypeElement typeElement) {
            Element element = resourceElement.resolve(getHelper().getCompilationController());
            if (element == null) {
                return false;
            }
            List<? extends AnnotationMirror> annotations = element.getAnnotationMirrors();
            if (annotations.size() == 0) {
                return false;
            }
            AnnotationParser parser = AnnotationParser.create(getHelper());
            parser.expectString("name", parser.defaultValue(typeElement.getSimpleName()));
            name = parser.parse(annotations.get(0)).get("name", String.class);
            return true;
        }

        public String getName() {
            return name;
        }
    }
}
