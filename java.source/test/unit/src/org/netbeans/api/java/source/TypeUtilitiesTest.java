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
package org.netbeans.api.java.source;

import java.io.File;
import java.net.URL;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

/**
 *
 * @author Jan Lahoda
 */
public class TypeUtilitiesTest extends NbTestCase {
    
    public TypeUtilitiesTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        super.setUp();
        this.clearWorkDir();
        File workDir = getWorkDir();
        File cacheFolder = new File (workDir, "cache"); //NOI18N
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testIsCastable() throws Exception {
        JavaSource js = JavaSource.create(ClasspathInfo.create(ClassPathSupport.createClassPath(SourceUtilsTestUtil.getBootClassPath().toArray(new URL[0])), ClassPathSupport.createClassPath(new URL[0]), ClassPathSupport.createClassPath(new URL[0])));
        
        js.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {}
            public void run(CompilationController info)  {
                TypeElement jlStringElement = info.getElements().getTypeElement("java.lang.String");
                TypeMirror jlString = info.getTypes().getDeclaredType(jlStringElement);
                TypeElement jlIntegerElement = info.getElements().getTypeElement("java.lang.Integer");
                TypeMirror jlInteger = info.getTypes().getDeclaredType(jlIntegerElement);
                TypeElement juListElement = info.getElements().getTypeElement("java.util.List");
                TypeMirror juListString = info.getTypes().getDeclaredType(juListElement, jlString);
                TypeMirror juListInteger = info.getTypes().getDeclaredType(juListElement, jlInteger);
                TypeElement jlObjectElement = info.getElements().getTypeElement("java.lang.Object");
                TypeMirror jlObject = info.getTypes().getDeclaredType(jlObjectElement);
                TypeMirror primitiveChar = info.getTypes().getPrimitiveType(TypeKind.CHAR);
                
                TypeUtilities u = info.getTypeUtilities();
                
                assertTrue(u.isCastable(jlObject, jlString));
                assertTrue(u.isCastable(jlObject, jlInteger));
                assertTrue(u.isCastable(jlObject, juListString));
                
                assertFalse(u.isCastable(jlString, jlInteger));
                assertFalse(u.isCastable(jlInteger, jlString));
                assertFalse(u.isCastable(juListString, juListInteger));
                assertFalse(u.isCastable(juListInteger, juListString));
                
                //verify that the order of arguments is understood correctly:
                //(requires 1.5):
                assertFalse(u.isCastable(jlObject, primitiveChar));
                assertTrue(u.isCastable(primitiveChar, jlObject));
            }
        }, true);
        
    }

}
