/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 * 
 * Portions Copyrighted 2007-2013 Sun Microsystems, Inc.
 */

package org.netbeans.api.java.source;
import java.io.File;
import java.io.IOException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class ElementUtilitiesTest extends NbTestCase {

    public ElementUtilitiesTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
    }
    
    private FileObject sourceRoot;
    private FileObject testFO;
        
    private void prepareTest() throws Exception {
        File work = getWorkDir();
        FileObject workFO = FileUtil.toFileObject(work);
        
        assertNotNull(workFO);
        
        sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);
        
        testFO = sourceRoot.createData("Test.java");
    }
    
    public void testI18N() throws Exception {
        prepareTest();
        
        TestUtilities.copyStringToFile(FileUtil.toFile(testFO),
                "public class Test {" +
                "}");
        JavaSource javaSource = JavaSource.forFileObject(testFO);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                
                {
                    Element wait = controller.getElementUtilities().findElement("java.lang.Object.wait(long)");
                    assertNotNull(wait);
                    assertEquals(ElementKind.METHOD, wait.getKind());
                    ExecutableElement waitMethod = (ExecutableElement) wait;
                    assertEquals("wait", waitMethod.getSimpleName().toString());
                    assertEquals(1, waitMethod.getParameters().size());
                    assertEquals(TypeKind.LONG, waitMethod.getParameters().get(0).asType().getKind());
                    assertEquals(controller.getElements().getTypeElement("java.lang.Object"), waitMethod.getEnclosingElement());
                }
                
                {
                    Element arrayListInit = controller.getElementUtilities().findElement("java.util.ArrayList.ArrayList(java.util.Collection)");
                    assertNotNull(arrayListInit);
                    assertEquals(ElementKind.CONSTRUCTOR, arrayListInit.getKind());
                    ExecutableElement arrayListInitMethod = (ExecutableElement) arrayListInit;
                    assertEquals("<init>", arrayListInitMethod.getSimpleName().toString());
                    assertEquals(1, arrayListInitMethod.getParameters().size());
                    assertEquals("java.util.Collection", controller.getTypes().erasure(arrayListInitMethod.getParameters().get(0).asType()).toString());
                    assertEquals(controller.getElements().getTypeElement("java.util.ArrayList"), arrayListInitMethod.getEnclosingElement());
                }
                
                {
                    Element arrayListAdd = controller.getElementUtilities().findElement("java.util.ArrayList.add(int, Object)");
                    assertNotNull(arrayListAdd);
                    assertEquals(ElementKind.METHOD, arrayListAdd.getKind());
                    ExecutableElement arrayListAddMethod = (ExecutableElement) arrayListAdd;
                    assertEquals("add", arrayListAddMethod.getSimpleName().toString());
                    assertEquals(2, arrayListAddMethod.getParameters().size());
                    assertEquals(TypeKind.INT, arrayListAddMethod.getParameters().get(0).asType().getKind());
                    assertEquals("java.lang.Object", controller.getTypes().erasure(arrayListAddMethod.getParameters().get(1).asType()).toString());
                    assertEquals(controller.getElements().getTypeElement("java.util.ArrayList"), arrayListAddMethod.getEnclosingElement());
                }
                
                {
                    Element arraysAsList = controller.getElementUtilities().findElement("java.util.Arrays.asList(Object...)");
                    assertNotNull(arraysAsList);
                    assertEquals(ElementKind.METHOD, arraysAsList.getKind());
                    ExecutableElement arraysAsListMethod = (ExecutableElement) arraysAsList;
                    assertEquals("asList", arraysAsListMethod.getSimpleName().toString());
                    assertEquals(1, arraysAsListMethod.getParameters().size());
                    assertEquals(TypeKind.ARRAY, arraysAsListMethod.getParameters().get(0).asType().getKind());
                    assertEquals(controller.getElements().getTypeElement("java.util.Arrays"), arraysAsListMethod.getEnclosingElement());
                }
                
                {
                    Element hashCode = controller.getElementUtilities().findElement("java.lang.Object.hashCode()");
                    assertNotNull(hashCode);
                    assertEquals(ElementKind.METHOD, hashCode.getKind());
                    ExecutableElement hashCodeMethod = (ExecutableElement) hashCode;
                    assertEquals("hashCode", hashCodeMethod.getSimpleName().toString());
                    assertEquals(0, hashCodeMethod.getParameters().size());
                    assertEquals(controller.getElements().getTypeElement("java.lang.Object"), hashCodeMethod.getEnclosingElement());
                }
                
                {
                    Element bigIntegerOne = controller.getElementUtilities().findElement("java.math.BigInteger.ONE");
                    assertNotNull(bigIntegerOne);
                    assertEquals(ElementKind.FIELD, bigIntegerOne.getKind());
                    assertEquals("ONE", bigIntegerOne.getSimpleName().toString());
                    assertEquals(controller.getElements().getTypeElement("java.math.BigInteger"), bigIntegerOne.getEnclosingElement());
                }
                
                {
                    Element bigInteger = controller.getElementUtilities().findElement("java.math.BigInteger");
                    assertEquals(controller.getElements().getTypeElement("java.math.BigInteger"), bigInteger);
                }
            }
        }, true);
    }

}
