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

package org.netbeans.modules.j2ee.metadata.model.api.support.annotation;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.TypeAnnotationHandler;
import org.netbeans.modules.j2ee.metadata.model.support.PersistenceTestCase;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.openide.util.MapFormat;

/**
 *
 * @author Andrei Badea
 */
public class AnnotationScannerTest extends PersistenceTestCase {

    public AnnotationScannerTest(String testName) {
        super(testName);
    }

    protected Level logLevel() {
        // enabling logging
        return Level.INFO;
    }

    public PrintStream getLog() {
        return System.err;
    }

    public void testScanTypes() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "Customer.java",
                "@javax.persistence.Entity()" +
                "public class Customer { }");
        TestUtilities.copyStringToFileObject(srcFO, "Foo.java",
                "public class Foo { }");
        TestUtilities.copyStringToFileObject(srcFO, "Item.java",
                "@javax.persistence.Entity()" +
                "public class Item { }");
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        final Set<String> types = new HashSet<String>();
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                helper.getAnnotationScanner().findAnnotatedTypes("javax.persistence.Entity", new TypeAnnotationHandler() {
                    public void typeAnnotation(TypeElement typeElement, AnnotationMirror annotationMirror) {
                        types.add(typeElement.getQualifiedName().toString());
                    }
                });
            }
        });
        assertEquals(2, types.size());
        assertTrue(types.contains("Customer"));
        assertTrue(types.contains("Item"));
    }

    public void testPerformance() throws Exception {
        // Logger.getLogger(AnnotationScanner.class.getName()).setLevel(Level.FINEST);
        final int ENTITY_COUNT = 500;
        String template = TestUtilities.copyStreamToString(getClass().getResourceAsStream("Table.javax"));
        Map<String, String> args = new HashMap<String, String>();
        MapFormat format = new MapFormat(args);
        format.setLeftBrace("__");
        format.setRightBrace("__");
        for (int i = 0; i < ENTITY_COUNT; i++) {
            String name = "Table" + i;
            args.put("NUM", Integer.toString(i));
            args.put("NAME", name);
            String contents = format.format(template);
            TestUtilities.copyStringToFileObject(srcFO, name + ".java", contents);
        }
        long startTime = System.nanoTime();
        RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcFO, srcFO).await();
        long compilationDoneTime = System.nanoTime();
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        final int[] entityCount = { 0 };
        final long initialScanDoneTime[] = { 0L };
        final List<ElementHandle<TypeElement>> typeHandles  = new ArrayList<ElementHandle<TypeElement>>();
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                helper.getAnnotationScanner().findAnnotatedTypes("javax.persistence.Entity", new TypeAnnotationHandler() {
                    public void typeAnnotation(TypeElement typeElement, AnnotationMirror annotationMirror) {
                        typeHandles.add(ElementHandle.create(typeElement));
                        entityCount[0]++;
                    }
                });
                initialScanDoneTime[0] = System.nanoTime();
            }
        });
        assertEquals(ENTITY_COUNT, entityCount[0]);
        System.out.println("Compilation time (ms): " + (compilationDoneTime - startTime) / 1e6);
        System.out.println("Initial annotation scan time (ms): " + (initialScanDoneTime[0] - compilationDoneTime) / 1e6);
    }
}
