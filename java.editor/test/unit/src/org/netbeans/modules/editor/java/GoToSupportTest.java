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
package org.netbeans.modules.editor.java;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.java.GoToSupport.UiUtilsCaller;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class GoToSupportTest extends NbTestCase {
    
    /** Creates a new instance of GoToSupportTest */
    public GoToSupportTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/editor/resources/layer.xml"}, new Object[0]);
    }
    
    public void testGoToMethod() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        
        performTest("package test; public class Test { public void test() {} public static void main(String[] args) {test();}}", 97, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(34, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);
        
        assertTrue(wasCalled[0]);
    }
    
    public void testGoToClass() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        
        performTest("package test; public class Test { public static void main(String[] args) {TT tt} } class TT { }", 75, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(83, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);
        
        assertTrue(wasCalled[0]);
    }
    
    public void testGoToConstructor() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        
        performTest("package test; public class Test { public Test() {} public static void main(String[] args) {new Test();}}", 97, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(34, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);
        
        assertTrue(wasCalled[0]);
    }
    
    public void testGoToGenerifiedConstructor() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        
        performTest("package test; public class Test<T> { public Test() {} public static void main(String[] args) {new Test<String>();}}", 100, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(37, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);
        
        assertTrue(wasCalled[0]);
    }
    
    public void testGoToSuperConstructor() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        
        performTest("package test; public class Test extends Base { public Test() {super(1);} } class Base {public Base() {} public Base(int i) {}}", 64, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(104, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);
        
        assertTrue(wasCalled[0]);
    }
    
    public void testGoToThisConstructor() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        
        performTest("package test; public class Test { public Test() {this(1);} public Test(int i) {}}", 50, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(59, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);
        
        assertTrue(wasCalled[0]);
    }
    
    public void testGoToSuperMethod1() throws Exception {
        //try to go to "super" in super.methodInParent():
        final boolean[] wasCalled = new boolean[1];
        
        performTest("package test; public class Test extends Base { public void test() {super.methodInParent();} } class Base {public void methodInParent() {}}", 75, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(106, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);
        
        assertTrue(wasCalled[0]);
    }
    
    public void testGoToSuperMethod2() throws Exception {
        //try to go to "super" in super.methodInParent():
        final boolean[] wasCalled = new boolean[1];
        
        performTest("package test; public class Test extends Base { public void test() {super.methodInParent();} } class Base {public void methodInParent() {}}", 70, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called.");
            }
            public void beep() {
                wasCalled[0] = true;
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);
        
        assertTrue(wasCalled[0]);
    }
    
    public void testGoToGarbage() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        
        performTest("package test; public class Test {ddddddddd public void test() {super.methodInParent();} }", 36, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called.");
            }
            public void beep() {
                wasCalled[0] = true;
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);
        
        assertTrue(wasCalled[0]);
    }
    
    public void testTooltipForGarbage() throws Exception {
        String tooltip = performTest("package test; public class Test {ddddddddd public void test() {super.methodInParent();} }", 36, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called.");
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, true);
    }
    
    public void testGoToIntoAnnonymous() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        
        performTest("package test; public class Test {public void test() {new Runnable() {int var; public void run() {var = 0;}};} }", 99, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                wasCalled[0] = true;
                assertTrue(source == fo);
                assertEquals(69, pos);
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);
        
        assertTrue(wasCalled[0]);
    }
    
    public void testGoToString() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        
        performTest("package test; public class Test {public void test() {String s = null;} }", 56, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called.");
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                assertEquals(ElementKind.CLASS, el.getKind());
                assertEquals("java.lang.String", ((TypeElement) el).getQualifiedName().toString());
                wasCalled[0] = true;
            }
        }, false);
        
        assertTrue(wasCalled[0]);
    }
    
    public void testGoToAnnonymousInnerClass() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        
        performTest("package test; public class Test {public void test() {new Runnable() {public void run(){}};} }", 61, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called.");
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                assertEquals(ElementKind.INTERFACE, el.getKind());
                assertEquals("java.lang.Runnable", ((TypeElement) el).getQualifiedName().toString());
                wasCalled[0] = true;
            }
        }, false);
        
        assertTrue(wasCalled[0]);
    }
    
    public void testGoToAnnonymousInnerClass2() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        
        performTest("package test; public class Test {public void test() {new java.util.ArrayList(c) {public void run(){}};} java.util.Collection c;}", 70, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called.");
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                assertEquals(ElementKind.CONSTRUCTOR, el.getKind());
                assertEquals("java.util.ArrayList", ((TypeElement) el.getEnclosingElement()).getQualifiedName().toString());
                
                ExecutableElement ee = (ExecutableElement) el;
                
                assertEquals(1, ee.getParameters().size());
                
                TypeMirror paramType = ee.getParameters().get(0).asType();
                
                assertEquals(TypeKind.DECLARED, paramType.getKind());
                assertEquals("java.util.Collection", ((TypeElement) ((DeclaredType) paramType).asElement()).getQualifiedName().toString());
                
                wasCalled[0] = true;
            }
        }, false);
        
        assertTrue(wasCalled[0]);
    }
    
    public void testGoToAnnonymousInnerClass3() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        
        performTest("package test; public class Test {java.util.List l = new java.util.ArrayList() {public void run(){}};}", 70, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called.");
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                assertEquals(ElementKind.CONSTRUCTOR, el.getKind());
                assertEquals("java.util.ArrayList", ((TypeElement) el.getEnclosingElement()).getQualifiedName().toString());
                
                ExecutableElement ee = (ExecutableElement) el;
                
                assertEquals(0, ee.getParameters().size());
                
                wasCalled[0] = true;
            }
        }, false);
        
        assertTrue(wasCalled[0]);
    }
    
    public void testGoToParameter() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        
        performTest("package test; public class Test {public void test(int xx) {xx = 0;}}", 60, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(50, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);
        
        assertTrue(wasCalled[0]);
    }
    
    public void testGoToLocalVariable() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        
        performTest("package test; public class Test {public void test() {int xx;xx = 0;}}", 61, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(53, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);
        
        assertTrue(wasCalled[0]);
    }
    
    public void testGoToTypeVariable() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        
        performTest("package test; public class Test<TTT> {public void test() {TTT t;}}", 60, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(32, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);
        
        assertTrue(wasCalled[0]);
    }
    
    public void testGoToSynteticConstructorInDifferentClass() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        
        performTest("package test; public class Test {public void test() {new Auxiliary();}}", 62, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called.");
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                assertEquals(ElementKind.CLASS, el.getKind());
                assertEquals("test.Auxiliary", ((TypeElement) el).getQualifiedName().toString());
                wasCalled[0] = true;
            }
        }, false);
        
        assertTrue(wasCalled[0]);
    }
    
    public void testGoToCArray90875() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        
        performTest("package test; public class Test {public void test() {int ar[][] = null; System.err.println(ar);}}", 92, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(53, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);
        
        assertTrue(wasCalled[0]);
    }
    
    public void testNewClass91637() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        
        performTest("package test; public class Test {public Test(int x){} public void test() {int ii = 0; new Test(ii);}}", 96, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(74, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);
        
        assertTrue(wasCalled[0]);
        
        wasCalled[0] = false;
        
        performTest("package test; public class Test<T> {public Test(int x){} public void test() {int ii = 0; new Test<Object>(ii);}}", 107, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(77, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);
        
        assertTrue(wasCalled[0]);
        
        wasCalled[0] = false;
        
        performTest("package test; public class Test<T> {public Test(int x){} public void test() {int ii = 0; new Test<Object>(ii);}}", 100, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called.");
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                assertEquals(ElementKind.CLASS, el.getKind());
                assertEquals("java.lang.Object", ((TypeElement) el).getQualifiedName().toString());
                wasCalled[0] = true;
            }
        }, false);
        
        assertTrue(wasCalled[0]);
    }
    
    public void testNewClass91769() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        
        performTest("package test; public class Test {public void test() {new AB(name);} private static class AB {public AB(String n){}}}", 58, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(68, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);
        
        assertTrue(wasCalled[0]);
        
        wasCalled[0] = false;
        
        performTest("package test; public class Test {public void test() {new AB<Object>(name);} private static class AB<T> {public AB(String n){}}}", 58, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(76, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);
        
        assertTrue(wasCalled[0]);
        
        wasCalled[0] = false;
        
        performTest("package test; public class Test {public void test() {new AB(name);} private static class AB {public AB(String n){}}}", 62, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called.");
            }
            public void beep() {
                wasCalled[0] = true;
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);
        
        assertTrue(wasCalled[0]);
        
        wasCalled[0] = false;
        
        performTest("package test; public class Test {public void test() {new AB<Object>(name);} private static class AB<T> {public AB(String n){}}}", 63, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called.");
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                assertEquals(ElementKind.CLASS, el.getKind());
                assertEquals("java.lang.Object", ((TypeElement) el).getQualifiedName().toString());
                wasCalled[0] = true;
            }
        }, false);
        
        assertTrue(wasCalled[0]);
    }
    
    public void testBeepOnDeclarations() throws Exception {
        String code = "package test; public class Test {public void test(String s) {} public String test2(String s) {} public void test3() {} private static class AB {} private String FIELD; private void test4(String name1, String name2) {}}";
        final boolean[] wasCalled = new boolean[1];
        
        for (final int pos : new int[] {53, 71, 103, 134, 165, 187, 220, 234}) {
            performTest(code, pos - 24, new UiUtilsCaller() {
                public void open(FileObject fo, int pos) {
                    fail("Should not be called, position= " + pos + ".");
                }
                public void beep() {
                    wasCalled[0] = true;
                }
                public void open(ClasspathInfo info, Element el) {
                    fail("Should not be called, position= " + pos + ".");
                }
            }, false);
            
            assertTrue(wasCalled[0]);
            
            wasCalled[0] = false;
        }
        
        for (final int pos : new int[] {77, 97, 109, 181, 214, 228}) {
            performTest(code, pos - 24, new UiUtilsCaller() {
                public void open(FileObject fo, int pos) {
                    fail("Should not be called, position= " + pos + ".");
                }
                public void beep() {
                    fail("Should not be called, position= " + pos + ".");
                }
                public void open(ClasspathInfo info, Element el) {
                    assertEquals(ElementKind.CLASS, el.getKind());
                    assertEquals("java.lang.String", ((TypeElement) el).getQualifiedName().toString());
                    wasCalled[0] = true;
                }
            }, false);
            
            assertTrue(wasCalled[0]);
            
            wasCalled[0] = false;
        }
    }
    
    public void test113474() throws Exception {
        String code = "package test; public class Test {}whatever";
        final boolean[] wasCalled = new boolean[1];
        
        performTest(code, 63 - 24, new UiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called, position= " + pos + ".");
            }
            public void beep() {
                wasCalled[0] = true;
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called, element= " + el + ".");
            }
        }, false);
        
        wasCalled[0] = false;
    }
    
    private void writeIntoFile(FileObject file, String what) throws Exception {
        FileLock lock = file.lock();
        OutputStream out = file.getOutputStream(lock);
        
        try {
            out.write(what.getBytes());
        } finally {
            out.close();
            lock.releaseLock();
        }
    }
    
    private FileObject source;
    
    private String performTest(String sourceCode, final int offset, final UiUtilsCaller validator, boolean tooltip) throws Exception {
        GoToSupport.CALLER = validator;
        
        FileObject root = makeScratchDir(this);
        
        FileObject sourceDir = root.createFolder("src");
        FileObject buildDir = root.createFolder("build");
        FileObject cacheDir = root.createFolder("cache");
        FileObject testDir  = sourceDir.createFolder("test");
        
        source = testDir.createData("Test.java");
        
        FileObject auxiliarySource = testDir.createData("Auxiliary.java");
        
        writeIntoFile(source, sourceCode);
        writeIntoFile(auxiliarySource, "package test; public class Auxiliary {}"); //test go to "syntetic" constructor
        
        SourceUtilsTestUtil.prepareTest(sourceDir, buildDir, cacheDir, new FileObject[0]);
        SourceUtilsTestUtil.compileRecursively(sourceDir);
        
        DataObject od = DataObject.find(source);
        EditorCookie ec = od.getCookie(EditorCookie.class);
        Document doc = ec.openDocument();
        
        doc.putProperty(Language.class, JavaTokenId.language());
        
        if (tooltip)
            return GoToSupport.getGoToElementTooltip(doc, offset, false);
        else
            GoToSupport.goTo(doc, offset, false);
        
        return null;
    }
    
    /**Copied from org.netbeans.api.project.
     * Create a scratch directory for tests.
     * Will be in /tmp or whatever, and will be empty.
     * If you just need a java.io.File use clearWorkDir + getWorkDir.
     */
    public static FileObject makeScratchDir(NbTestCase test) throws IOException {
        test.clearWorkDir();
        File root = test.getWorkDir();
        assert root.isDirectory() && root.list().length == 0;
        FileObject fo = FileUtil.toFileObject(root);
        if (fo != null) {
            // Presumably using masterfs.
            return fo;
        } else {
            // For the benefit of those not using masterfs.
            LocalFileSystem lfs = new LocalFileSystem();
            try {
                lfs.setRootDirectory(root);
            } catch (PropertyVetoException e) {
                assert false : e;
            }
            Repository.getDefault().addFileSystem(lfs);
            return lfs.getRoot();
        }
    }
    
}
