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

package org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;

/**
 *
 * @author Andrei Badea
 */
public class EntityMappingsUtilitiesTest extends EntityMappingsTestCase {

    public EntityMappingsUtilitiesTest(String testName) {
        super(testName);
    }

    public void testIsCollectionType() throws Exception {
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                Elements elements = helper.getCompilationController().getElements();
                Types types = helper.getCompilationController().getTypes();
                TypeElement typeElement = elements.getTypeElement("java.util.Collection");
                // Collection<E>
                assertTrue(EntityMappingsUtilities.isCollectionType(helper, typeElement.asType()));
                // Collection
                assertTrue(EntityMappingsUtilities.isCollectionType(helper, types.erasure(typeElement.asType())));
                // Collection<String>
                TypeMirror stringType = elements.getTypeElement("java.lang.String").asType();
                assertTrue(EntityMappingsUtilities.isCollectionType(helper, types.getDeclaredType(typeElement, stringType)));
                typeElement = elements.getTypeElement("java.util.ArrayList");
                // ArrayList<E>
                assertFalse(EntityMappingsUtilities.isCollectionType(helper, typeElement.asType()));
            }
        });
    }

    public void testGetFirstTypeArgument() throws Exception {
        ClasspathInfo cpi = ClasspathInfo.create(srcFO);
        final AnnotationModelHelper helper = AnnotationModelHelper.create(cpi);
        helper.runJavaSourceTask(new Runnable() {
            public void run() {
                Elements elements = helper.getCompilationController().getElements();
                Types types = helper.getCompilationController().getTypes();
                TypeElement typeElement = elements.getTypeElement("java.util.Collection");
                // Collection<E>
                assertNull(EntityMappingsUtilities.getFirstTypeArgument(typeElement.asType()));
                // Collection
                assertNull(EntityMappingsUtilities.getFirstTypeArgument(types.erasure(typeElement.asType())));
                // Collection<String>
                TypeMirror stringType = elements.getTypeElement("java.lang.String").asType();
                TypeElement argTypeElement = EntityMappingsUtilities.getFirstTypeArgument(types.getDeclaredType(typeElement, stringType));
                assertTrue(argTypeElement.getQualifiedName().contentEquals("java.lang.String"));
            }
        });
    }
}
