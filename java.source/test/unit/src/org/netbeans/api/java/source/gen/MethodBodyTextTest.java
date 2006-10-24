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
package org.netbeans.api.java.source.gen;

import com.sun.corba.se.impl.util.Utility;
import com.sun.org.apache.bcel.internal.classfile.JavaClass;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.tools.javac.code.TypeTags;
import javax.lang.model.element.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import junit.textui.TestRunner;
import org.netbeans.jackpot.transform.Transformer;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.classfile.Method;
import org.openide.filesystems.FileStateInvalidException;

/**
 * Tests indentation of newly generated body text in method.
 *
 * @author Pavel Flaska
  */
public class MethodBodyTextTest extends GeneratorTest {
    
    /** Creates a new instance of MethodBodyTextTest */
    public MethodBodyTextTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(MethodBodyTextTest.class);
        return suite;
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "MethodBodyText.java");
    }
    
    public void testSetBodyText() throws java.io.IOException, FileStateInvalidException {
        System.err.println("testSetBodyText");
        process(
            new MutableTransformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("method".contentEquals(node.getName())) {
                       setMethodBody(node, "{ System.err.println(\"Nothing.\"); }");
                    }
                    return null;
                }
            }
        );
        assertFiles("testSetBodyText_MethodBodyTextTest.pass");
    }
    
    public void testCreateWithBodyText() throws java.io.IOException, FileStateInvalidException {
        process(
            new MutableTransformer<Void, Object>() {
                public Void visitClass(ClassTree node, Object p) {
                    super.visitClass(node, p);
                    StringBuffer body = new StringBuffer();
                    body.append("{ System.out.println(\"Again Nothing\"); }");
                    MethodTree method = Method(
                            make.Modifiers(Collections.singleton(Modifier.PUBLIC)),
                            "method2",
                            make.PrimitiveType(TypeKind.VOID),
                            Collections.EMPTY_LIST,
                            Collections.EMPTY_LIST,
                            Collections.EMPTY_LIST,
                            body.toString(),
                            null
                            );
                    ClassTree clazz = make.addClassMember(node, method);
                    changes.rewrite(node, clazz);
                    return null;
                }
            }
        );
        assertFiles("testCreateWithBodyText_MethodBodyTextTest.pass");
    }
    
    public void testModifyBodyText() throws java.io.IOException, FileStateInvalidException {
        System.err.println("testModifyBodyText");
        process(
            new MutableTransformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("method2".equals(node.getName().toString())) {
                        String body = "{ List l; }";
                        setMethodBody(node, body);
                    }
                    return null;
                }
            }
        );
        assertFiles("testModifyBodyText_MethodBodyTextTest.pass");
    }
    
    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    String getSourcePckg() {
        return "org/netbeans/test/codegen/indent/";
    }

    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/indent/MethodBodyTextTest/";
    }

}
