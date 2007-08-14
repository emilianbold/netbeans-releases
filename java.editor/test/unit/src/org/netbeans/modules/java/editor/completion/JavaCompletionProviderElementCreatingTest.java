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

package org.netbeans.modules.java.editor.completion;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
public class JavaCompletionProviderElementCreatingTest extends CompletionTestBase {

    public JavaCompletionProviderElementCreatingTest(String testName) {
        super(testName);
    }

    public void testUnimplementedMethod() throws Exception {
        performTest("UnimplementedMethod", 115, "", "UnimplementedMethod.pass");
    }
    
    public void testOverrideAbstractList() throws Exception {
        performTest("OverrideAbstractList", 146, "", "OverrideAbstractList.pass");
    }
    
    /** CC should not offer overriding private method from superclass */
    public void testOverridePrivateMethod() throws Exception {
        performTest("OverridePrivateMethod", 123, "cl", "OverridePrivateMethod.pass");
    }

    public void testOverrideAbstractListWithPrefix() throws Exception {
        performTest("OverrideAbstractList", 146, "to", "OverrideAbstractListWithPrefix.pass", "toSt.*override", "OverrideAbstractListWithPrefix.pass2");
    }
    
    public void testOverrideFinalize() throws Exception {
        performTest("OverrideAbstractList", 146, "fin", "OverrideFinalize.pass", "finali.*override", "OverrideFinalize.pass2");
    }
    
    public void testOverrideAbstractList2a() throws Exception {
        performTest("OverrideAbstractList2", 169, "ad", "OverrideAbstractList2a.pass", "add.*override", "OverrideAbstractList2a.pass2");
    }
    
    public void testOverrideAbstractList2b() throws Exception {
        performTest("OverrideAbstractList2", 169, "ge", "OverrideAbstractList2b.pass", "ge.*implement", "OverrideAbstractList2b.pass2");
    }
    
    public void testOverrideAbstractList3a() throws Exception {
        performTest("OverrideAbstractList3", 156, "ad", "OverrideAbstractList3a.pass", "add.*override", "OverrideAbstractList3a.pass2");
    }
    
    public void testOverrideAbstractList3b() throws Exception {
        performTest("OverrideAbstractList3", 156, "ge", "OverrideAbstractList3b.pass", "ge.*implement", "OverrideAbstractList3b.pass2");
    }
    
    public void testOverrideTypedException1() throws Exception {
        performTest("OverrideTypedException", 237, "tes", "OverrideTypedException1.pass", "tes.*override", "OverrideTypedException1.pass2");
    }
    
    public void testOverrideTypedException2() throws Exception {
        performTest("OverrideTypedException", 351, "tes", "OverrideTypedException2.pass", "tes.*override", "OverrideTypedException2.pass2");
    }
    
    public void testOverrideInInnerClass() throws Exception {
        performTest("OverrideInInnerClass", 217, "pai", "OverrideInInnerClass.pass", "paint\\(.*override", "OverrideInInnerClass.pass2");
    }
    
    public void testOverrideInInnerClassUnresolvable() throws Exception {
        performTest("OverrideInInnerClassUnresolvable", 213, "pai", "empty.pass");
    }
    
    public void testCreateConstructorTest() throws Exception {
        performTest("CreateConstructorTest", 266, "", "CreateConstructorTest.pass");
    }

    public void testCreateConstructorTestInnerClass() throws Exception {
        performTest("CreateConstructorTest", 451, "", "CreateConstructorTestInnerClass.pass");
    }

    public void testCreateConstructorWithConstructors() throws Exception {
        performTest("CreateConstructorWithConstructors", 487, "", "CreateConstructorWithConstructors.pass");
    }

    public void testCreateConstructorWithConstructorsInnerClass() throws Exception {
        performTest("CreateConstructorWithConstructors", 672, "", "CreateConstructorWithConstructorsInnerClass.pass");
    }

    public void testCreateConstructorWithDefaultConstructor() throws Exception {
        performTest("CreateConstructorWithDefaultConstructor", 365, "", "CreateConstructorWithDefaultConstructor.pass");
    }

    public void testCreateConstructorWithDefaultConstructorInnerClass() throws Exception {
        performTest("CreateConstructorWithDefaultConstructor", 607, "", "CreateConstructorWithDefaultConstructorInnerClass.pass");
    }

    public void testCreateConstructorNonDefaultConstructor() throws Exception {
        performTest("CreateConstructorNonDefaultConstructor", 364, "", "CreateConstructorNonDefaultConstructor.pass");
    }

    public void testCreateConstructorNonDefaultConstructorInnerClass() throws Exception {
        performTest("CreateConstructorNonDefaultConstructor", 632, "", "CreateConstructorNonDefaultConstructorInnerClass.pass");
    }
}
