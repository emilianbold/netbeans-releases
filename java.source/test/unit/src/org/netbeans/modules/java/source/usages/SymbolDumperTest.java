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
package org.netbeans.modules.java.source.usages;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.jvm.ClassReader;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.CouplingAbort;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Name.Table;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.tools.Diagnostic;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.TestUtil;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.usages.ClassIndexImpl.UsageType;
import org.netbeans.modules.java.source.usages.Pair;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class SymbolDumperTest extends NbTestCase {
    
    public SymbolDumperTest(String testName) {
        super(testName);
    }
    
//    public static TestSuite suite() {
//        NbTestSuite result = new NbTestSuite();
//        
//        result.addTest(new SymbolDumperTest("testCyclicTypeArgumentDependency"));
//        
//        return result;
//    }
        
    private FileObject sourceRoot;
    
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        
        File work = TestUtil.createWorkFolder();
        FileObject workFO = FileUtil.toFileObject(work);
        
        assertNotNull(workFO);
        
        sourceRoot = workFO.createFolder("src");
        
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");
        
        //FIXME:
        File jarWithAnnotations = new File(getDataDir(), "Annotations.jar");
        FileObject jarWithAnnotationsFO = FileUtil.toFileObject(jarWithAnnotations);
        
        assertNotNull(jarWithAnnotationsFO);
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache, new FileObject[] {FileUtil. getArchiveRoot(jarWithAnnotationsFO)});
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
    
//    public void testDumpMethod() throws Exception {
//        perform("package test;\npublic class test {\npublic <T> T test(T t) {return null;}\n}\n",
//                "E<TT;>Ntest;(TT;Nt;)TT;",
//                new Finder() {
//            public Element findElement(CompilationInfo info, CompilationUnitTree unit) {
//                Tree main = unit.getTypeDecls().iterator().next();
//                TypeElement el = (TypeElement) info.getTrees().getElement(new TreePath(new TreePath(unit), main));
//                
//                return ElementFilter.methodsIn(el.getEnclosedElements()).get(0);
//            }
//        });
//    }
//    
//    public void testDumpClassSimple() throws Exception {
//        perform("package test;\npublic class test {\n}\n",
//                "GNtest.test;Ljava.lang.Object;",
//                new Finder() {
//            public Element findElement(CompilationInfo info, CompilationUnitTree unit) {
//                Tree main = unit.getTypeDecls().iterator().next();
//                return (TypeElement) info.getTrees().getElement(new TreePath(new TreePath(unit), main));
//            }
//        });
//    }
//
//    public void testDumpClassComplex1() throws Exception {
//        perform("package test;\npublic class test<T> {\n}\n",
//                "G<T:Ljava.lang.Object;;>Ntest.test;Ljava.lang.Object;",
//                new Finder() {
//            public Element findElement(CompilationInfo info, CompilationUnitTree unit) {
//                Tree main = unit.getTypeDecls().iterator().next();
//                return (TypeElement) info.getTrees().getElement(new TreePath(new TreePath(unit), main));
//            }
//        });
//    }
//    
//    public void testDumpClassComplex2() throws Exception {
//        perform("package test;\npublic abstract class test<T> extends java.util.AbstractList<String> {\n}\n",
//                "G<TT;>Ntest.test;Ljava.util.AbstractList<Ljava.lang.String;>;",
//                new Finder() {
//            public Element findElement(CompilationInfo info, CompilationUnitTree unit) {
//                Tree main = unit.getTypeDecls().iterator().next();
//                return (TypeElement) info.getTrees().getElement(new TreePath(new TreePath(unit), main));
//            }
//        });
//    }
//    
//    public void testDumpClassComplex3() throws Exception {
//        perform("package test;\npublic abstract class test<T> implements java.util.List<String>, java.util.Set<String> {\n}\n",
//                "G<TT;>Ntest.test;Ljava.lang.Object;Ljava.util.List<Ljava.lang.String;>;Ljava.util.Map<Ljava.lang.String;TT;>;",
//                new Finder() {
//            public Element findElement(CompilationInfo info, CompilationUnitTree unit) {
//                Tree main = unit.getTypeDecls().iterator().next();
//                return (TypeElement) info.getTrees().getElement(new TreePath(new TreePath(unit), main));
//            }
//        });
//    }
//    
//    public void testDumpClassComplex4() throws Exception {
//        perform("package test;\npublic abstract class test<T extends java.util.List> {\n}\n",
//                "G<T:Ljava.util.List;;>Ntest.test;Ljava.lang.Object;",
//                new Finder() {
//            public Element findElement(CompilationInfo info, CompilationUnitTree unit) {
//                Tree main = unit.getTypeDecls().iterator().next();
//                return (TypeElement) info.getTrees().getElement(new TreePath(new TreePath(unit), main));
//            }
//        });
//    }
//    
//    public void testDumpClassComplex5() throws Exception {
//        perform("package test;\npublic abstract class test<T extends java.util.List&java.util.Set> {\n}\n",
//                "G<T:Ljava.lang.Object;Ljava.util.List;Ljava.util.Set;;>Ntest.test;Ljava.lang.Object;", 
//                new Finder() {
//            public Element findElement(CompilationInfo info, CompilationUnitTree unit) {
//                Tree main = unit.getTypeDecls().iterator().next();
//                
//                return (TypeElement) info.getTrees().getElement(new TreePath(new TreePath(unit), main));
//            }
//        });
//    }
//
//    
//    public void testDumpMethodComplex1() throws Exception {
//        perform("package test;\npublic class test<T> {\npublic T test(T t) {return null;}\n}\n",
//                "ENtest;(TT;Nt;)TT;",
//                new Finder() {
//            public Element findElement(CompilationInfo info, CompilationUnitTree unit) {
//                Tree main = unit.getTypeDecls().iterator().next();
//                TypeElement el = (TypeElement) info.getTrees().getElement(new TreePath(new TreePath(unit), main));
//                
//                return ElementFilter.methodsIn(el.getEnclosedElements()).get(0);
//            }
//        });
//    }
    
    private static interface Finder {
        
        public Element findElement(CompilationInfo info, CompilationUnitTree unit);
        
    }
    
    protected void perform(String source, String signature, Finder finder) throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject file = fs.getRoot().createData("test.java");
        
        writeIntoFile(file, source);
        
        JavaSource js = JavaSource.forFileObject(file);
        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        CompilationUnitTree unit = info.getCompilationUnit();
        assertTrue(info.getDiagnostics().toString(), info.getDiagnostics().isEmpty());
        Element el = finder.findElement(info, unit);
        
        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);
        
        JavacTaskImpl jt = (JavacTaskImpl)SourceUtilsTestUtil.getJavacTaskFor(info);
        SymbolDumper.dump(pw, Types.instance(jt.getContext()), (TypeElement) el, null);
        
        pw.close();
        
//        System.err.println(w.toString());
        assertEquals(signature, w.toString());
    }
    
    public void testReadWriteSimple1() throws Exception {
        performReadWrite("package test;\npublic class test {\n}\n");
    }
    
    public void testReadWriteSimple2() throws Exception {
        performReadWrite("package test;\npublic class test {\npublic void testMethod(int a) {}\n}\n");
    }
    
    public void testReadWriteSimple3() throws Exception {
        performReadWrite("package test;\npublic class test {\npublic test(int y){} public void testMethod(int a) {}\n}\n");
    }
    
    public void testReadWriteSimple4() throws Exception {
        performReadWrite("package test;\npublic class test {\npublic void testMethod(String x) {}\n}\n");
    }
    
    public void testReadWriteSimple5() throws Exception {
        performReadWrite("package test;\npublic class test {\nprivate int x;\n}\n");
    }
    
    public void testReadWriteSimple6() throws Exception {
        performReadWrite("package test;\npublic class test {\nprivate int x;java.util.Map y;\n}\n");
    }
    
    public void testReadWriteSimple7() throws Exception {
        performReadWrite("package test;\npublic class test {\nprivate int x;java.util.Map y;\npublic void testMethod(String x) {}\n}\n");
    }
    
    public void testReadWriteSimple8() throws Exception {
        performReadWrite("package test;\npublic class test {\njava.util.Map y;\n}\n");
    }
    
    public void testReadWriteSimple9() throws Exception {
        performReadWrite("package test;\npublic class test {\njava.util.Map<String, String> y;\n}\n");
    }
    
    public void testReadWriteSimple10() throws Exception {
        performReadWrite("package test;\npublic class test {\njava.util.Map<String, String> y;\npublic java.util.Map<String, Integer> testMethod(java.util.Set<Long> set){return null;}\n}\n");
    }
    
    public void testReadWriteSimple11() throws Exception {
        performReadWrite("package test;\npublic abstract class test extends java.util.AbstractList {}\n");
    }
    
    public void testReadWriteSimple12() throws Exception {
        performReadWrite("package test;\npublic abstract class test extends java.util.AbstractList implements java.util.Set, java.util.List {}\n");
    }
    
    public void testReadWriteArray1() throws Exception {
        performReadWrite("package test;\npublic class test {\nprivate String[] f;}\n");
    }
    
    public void testReadWriteArray2() throws Exception {
        performReadWrite("package test;\npublic class test {\nprivate int[] f;}\n");
    }
    
    public void testReadWriteThrows1() throws Exception {
        performReadWrite("package test;\npublic class test {\nprivate void testMethod() throws Exception {}\n}\n");
    }
    
    
    public void testCyclicTypeArgumentDependency() throws Exception {
        performReadWrite("package test;\npublic class test<T extends java.util.List<T>> {\npublic T test(T t) {return null;}\n}\n");
    }
    
    
    //TODO: test error type
    
    public void testReadWriteGenerics1() throws Exception {
        performReadWrite("package test;\npublic class test<T> {\npublic T test(T t) {return null;}\n}\n");
    }
    
    public void testReadWriteGenerics2() throws Exception {
        performReadWrite("package test;\npublic class test<T extends java.util.List> {\npublic T test(T t) {return null;}\n}\n");
    }
    
    public void testReadWriteGenerics3() throws Exception {
        performReadWrite("package test;\npublic class test<T extends java.util.List&java.util.Set> {\npublic T test(T t) {return null;}\n}\n");
    }
    
    public void testReadWriteGenerics4() throws Exception {
        performReadWrite("package test;\npublic class test {\npublic <T> T test(T t) {return null;}\n}\n");
    }
    
    public void testReadWriteGenerics5() throws Exception {
        performReadWrite("package test;\npublic class test {\npublic <T extends java.util.List> T test(T t) {return null;}\n}\n");
    }
    
    public void testReadWriteGenerics6() throws Exception {
        performReadWrite("package test;\npublic class test {\npublic <T extends java.util.List&Comparable> T test(T t) {return null;}\n}\n");
    }
    
    public void testReadWriteGenerics7() throws Exception {
        performReadWrite("package test;\npublic class test {\npublic <T extends java.util.Map<? extends Number, ? super Comparable>> T test(T t) {return null;}\n}\n");
    }
    
    public void testReadWriteGenerics8() throws Exception {
        performReadWrite("package test;\npublic class test <T extends java.util.Map<? extends Number, ? super Comparable>> {\npublic  T test(T t) {return null;}\n}\n");
    }
    
    public void testReadWriteGenerics9() throws Exception {
        performReadWrite("package test; public class test {\npublic void testMethod() {\njava.util.Map<String, java.io.PrintWriter> m = new java.util.HashMap<String, java.io.PrintWriter>();}\n}");
    }
    
    public void testReadWriteGenerics10() throws Exception {
        performReadWrite("package test; public class test {\njava.util.Map<String, java.io.PrintWriter> m = new java.util.HashMap<String, java.io.PrintWriter>();\n}");
    }
    
    public void testReadWriteGenerics11() throws Exception {
        performReadWrite("package test; public class test<T> {\njava.util.Map<String, java.io.PrintWriter> m = new java.util.HashMap<String, java.io.PrintWriter>();\n}");
    }
    
    private static class ConstantValidator implements Validator {
        private Object value;
        public ConstantValidator(Object value) {
            this.value = value;
        }
        public void validate(CompilationInfo info, Element t) {
            assertEquals(ElementKind.CLASS, t.getKind());
            
            VariableElement constant = ElementFilter.fieldsIn(t.getEnclosedElements()).get(0);
            Object constantValue = constant.getConstantValue();
            
            assertEquals(value, constantValue);
        }
    }
    
    public void testReadWriteConstantInt() throws Exception {
        performReadWrite("package test;\npublic class test {\npublic static final int X = 13;\n}\n", new ConstantValidator(Integer.valueOf(13)));
    }
    
    //does not work:
//    public void testReadWriteConstantBooleanTRUE() throws Exception {
//        performReadWrite("package test;\npublic class test {\npublic static final boolean X = Boolean.TRUE;\n}\n", new ConstantValidator(Boolean.TRUE));
//    }
//    
//    public void testReadWriteConstantBooleanFALSE() throws Exception {
//        performReadWrite("package test;\npublic class test {\npublic static final boolean X = Boolean.FALSE;\n}\n", new ConstantValidator(Boolean.FALSE));
//    }
    
    public void testReadWriteConstantByte() throws Exception {
        performReadWrite("package test;\npublic class test {\npublic static final byte X = 13;\n}\n", new ConstantValidator(Byte.valueOf("13")));
    }
    
    public void testReadWriteConstantShort() throws Exception {
        performReadWrite("package test;\npublic class test {\npublic static final short X = 13;\n}\n", new ConstantValidator(Short.valueOf("13")));
    }
    
    public void testReadWriteConstantLong() throws Exception {
        performReadWrite("package test;\npublic class test {\npublic static final long X = 13;\n}\n", new ConstantValidator(Long.valueOf(13)));
    }
    
    public void testReadWriteConstantFloat() throws Exception {
        performReadWrite("package test;\npublic class test {\npublic static final float X = 13.98F;\n}\n", new ConstantValidator(Float.valueOf("13.98")));
    }
    
    public void testReadWriteConstantDouble() throws Exception {
        performReadWrite("package test;\npublic class test {\npublic static final double X = 13.98;\n}\n", new ConstantValidator(Double.valueOf(13.98)));
    }
    
    public void testReadWriteConstantChar() throws Exception {
        performReadWrite("package test;\npublic class test {\npublic static final char X = 'a';\n}\n", new ConstantValidator(Character.valueOf('a')));
    }
    
    public void testReadWriteConstantCharEscape1() throws Exception {
        performReadWrite("package test;\npublic class test {\npublic static final char X = '@';\n}\n", new ConstantValidator(Character.valueOf('@')));
    }
    
    public void testReadWriteConstantCharEscape2() throws Exception {
        performReadWrite("package test;\npublic class test {\npublic static final char X = ';';\n}\n", new ConstantValidator(Character.valueOf(';')));
    }
    
    public void testReadWriteConstantCharEscape3() throws Exception {
        performReadWrite("package test;\npublic class test {\npublic static final char X = '\\\\';\n}\n", new ConstantValidator(Character.valueOf('\\')));
    }
    
    public void testReadWriteConstantCharEscape4() throws Exception {
        performReadWrite("package test;\npublic class test {\npublic static final char X = '\\n';\n}\n", new ConstantValidator(Character.valueOf('\n')));
    }
    
    public void testReadWriteConstantString() throws Exception {
        performReadWrite("package test;\npublic class test {\npublic static final String X = \"test@\\\\sd@;@\\n@''';\";\n}\n", new ConstantValidator("test@\\sd@;@\n@''';"));
    }
    
    public void testReadWriteConstantString2() throws Exception {
        performReadWrite("package test;\npublic class test {\npublic static final String X = \"d\\1x\";\n}\n", new ConstantValidator("d\1x"));
    }
    
    public void testReadWriteConstantString3() throws Exception {
        performReadWrite("package test;\npublic class test {\npublic static final String X = \"d\\\\1x\";\n}\n", new ConstantValidator("d\\1x"));
    }
    
    public void testReadWriteConstantString4() throws Exception {
        performReadWrite("package test;\npublic class test {\npublic static final String X = \"d\\\\\\1x\";\n}\n", new ConstantValidator("d\\\1x"));
    }
    
    public void testReadWriteConstantString5() throws Exception {
        performReadWrite("package test;\npublic class test {\npublic static final String X = \"d\\\\\\\\1x\";\n}\n", new ConstantValidator("d\\\\1x"));
    }
    
    public void testReadWriteConstantString6() throws Exception {
        performReadWrite("package test;\npublic class test {\npublic static final String X = \"d\\\\\";\n}\n", new ConstantValidator("d\\"));
    }
    
    public void testReadWriteConstructors() throws Exception {
        performReadWrite("package test; public class test {\npublic test(Class clazz){}\n}");
    }
    
    public void testReadWriteNoArgAnnotation() throws Exception {
        performReadWrite("package test; import annotations.NoArgAnnotation; @NoArgAnnotation public class test {\npublic test(Class clazz){}\n}", new Validator() {
            public void validate(CompilationInfo info, Element t) {
                List<? extends AnnotationMirror> annotations = t.getAnnotationMirrors();
                
                boolean found = false;
                
                for (AnnotationMirror m : annotations) {
                    if ("annotations.NoArgAnnotation".equals(((TypeElement) m.getAnnotationType().asElement()).getQualifiedName().toString())) {
                        found = true;
                        break;
                    }
                }
                
                assertTrue(found);
            }
        });
    }
    
    //we intentionaly store also the annotations with retention source:
    public void testReadWriteNoArgAnnotationSource() throws Exception {
        performReadWrite("package test; import annotations.NoArgAnnotationSource; @NoArgAnnotationSource public class test {\npublic test(Class clazz){}\n}", new Validator() {
            public void validate(CompilationInfo info, Element t) {
                List<? extends AnnotationMirror> annotations = t.getAnnotationMirrors();
                
                boolean found = false;
                
                for (AnnotationMirror m : annotations) {
                    if ("annotations.NoArgAnnotationSource".equals(((TypeElement) m.getAnnotationType().asElement()).getQualifiedName().toString())) {
                        found = true;
                        break;
                    }
                }
                
                assertTrue(found);
            }
        });
    }
    
    public void testReadWriteNoArgAnnotationOnMethod() throws Exception {
        performReadWrite("package test; import annotations.NoArgAnnotation; public class test {\npublic @NoArgAnnotation test(Class clazz){}\n}", new Validator() {
            public void validate(CompilationInfo info, Element t) {
                ExecutableElement ee = ElementFilter.constructorsIn(t.getEnclosedElements()).get(0);
                List<? extends AnnotationMirror> annotations = ee.getAnnotationMirrors();
                boolean found = false;
                
                for (AnnotationMirror m : annotations) {
                    if ("annotations.NoArgAnnotation".equals(((TypeElement) m.getAnnotationType().asElement()).getQualifiedName().toString())) {
                        found = true;
                        break;
                    }
                }
                
                assertTrue(found);
            }
        });
    }
    
    public void testReadWriteNoArgAnnotationOnField() throws Exception {
        performReadWrite("package test; import annotations.NoArgAnnotation; public class test {\npublic @NoArgAnnotation int test = 1;\n}", new Validator() {
            public void validate(CompilationInfo info, Element t) {
                VariableElement ve = ElementFilter.fieldsIn(t.getEnclosedElements()).get(0);
                List<? extends AnnotationMirror> annotations = ve.getAnnotationMirrors();
                boolean found = false;
                
                for (AnnotationMirror m : annotations) {
                    if ("annotations.NoArgAnnotation".equals(((TypeElement) m.getAnnotationType().asElement()).getQualifiedName().toString())) {
                        found = true;
                        break;
                    }
                }
                
                assertTrue(found);
            }
        });
    }
    
    private static AnnotationValue findValue(AnnotationMirror m, String attributeName) {
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : m.getElementValues().entrySet()) {
            if (attributeName.equals(entry.getKey().getSimpleName().toString())) {
                return entry.getValue();
            }
        }
        
        fail("required attribute not found");
        throw new AssertionError("cannot happen");
    }
    
    private static class ValidateAnnotationWithPrimitiveTypeValue implements Validator {
        private String annotationName;
        private Object value;
        
        public ValidateAnnotationWithPrimitiveTypeValue(String annotationName, Object value) {
            this.annotationName = annotationName;
            this.value = value;
        }
        
        public void validate(CompilationInfo info, Element t) {
            List<? extends AnnotationMirror> annotations = t.getAnnotationMirrors();
            boolean found = false;
            
            for (AnnotationMirror m : annotations) {
                if (("annotations." + annotationName).equals(((TypeElement) m.getAnnotationType().asElement()).getQualifiedName().toString())) {
                    AnnotationValue v = findValue(m, "value");
                    
                    Object proposedValue = v.getValue();
                    
                    if (this.value instanceof Collection) {
                        List proposed = new ArrayList();
                        
                        for (AnnotationValue value : (Collection<AnnotationValue>) proposedValue) {
                            proposed.add(value.getValue());
                        }
                        
                        assertEquals(new ArrayList((Collection) this.value), proposed);
                    } else {
                        assertEquals(this.value, proposedValue);
                    }
                    
                    found = true;
                    break;
                }
            }
            
            assertTrue(found);
        }
    }
    
    public void testReadWriteAnnotationBooleanArg() throws Exception {
        performReadWrite("package test; import annotations.BooleanArgAnnotation; public @BooleanArgAnnotation(true) class test {\n}",
            new ValidateAnnotationWithPrimitiveTypeValue("BooleanArgAnnotation", Boolean.TRUE));
    }
    
    public void testReadWriteAnnotationByteArg() throws Exception {
        performReadWrite("package test; import annotations.ByteArgAnnotation; public @ByteArgAnnotation(2) class test {\n}", 
            new ValidateAnnotationWithPrimitiveTypeValue("ByteArgAnnotation", Byte.valueOf((byte) 2)));
    }
    
    public void testReadWriteAnnotationShortArg() throws Exception {
        performReadWrite("package test; import annotations.ShortArgAnnotation; public @ShortArgAnnotation(2) class test {\n}",
            new ValidateAnnotationWithPrimitiveTypeValue("ShortArgAnnotation", Short.valueOf((short) 2)));
    }
    
    public void testReadWriteAnnotationIntArg() throws Exception {
        performReadWrite("package test; import annotations.IntArgAnnotation; public @IntArgAnnotation(2) class test {\n}",
            new ValidateAnnotationWithPrimitiveTypeValue("IntArgAnnotation", Integer.valueOf(2)));
    }
    
    public void testReadWriteAnnotationLongArg() throws Exception {
        performReadWrite("package test; import annotations.LongArgAnnotation; public @LongArgAnnotation(2) class test {\n}",
            new ValidateAnnotationWithPrimitiveTypeValue("LongArgAnnotation", Long.valueOf(2)));
    }
        
    public void testReadWriteAnnotationFloatArg() throws Exception {
        performReadWrite("package test; import annotations.FloatArgAnnotation; public @FloatArgAnnotation(2.45F) class test {\n}",
            new ValidateAnnotationWithPrimitiveTypeValue("FloatArgAnnotation", Float.valueOf(2.45F)));
    }
        
    public void testReadWriteAnnotationDoubleArg() throws Exception {
        performReadWrite("package test; import annotations.DoubleArgAnnotation; public @DoubleArgAnnotation(2.45) class test {\n}",
            new ValidateAnnotationWithPrimitiveTypeValue("DoubleArgAnnotation", Double.valueOf(2.45)));
    }
        
    public void testReadWriteAnnotationCharArg() throws Exception {
        performReadWrite("package test; import annotations.CharArgAnnotation; public @CharArgAnnotation('@') class test {\n}",
            new ValidateAnnotationWithPrimitiveTypeValue("CharArgAnnotation", Character.valueOf('@')));
    }
    
    public void testReadWriteAnnotationStringArg() throws Exception {
        performReadWrite("package test; import annotations.StringArgAnnotation; public @StringArgAnnotation(\"test@;;\\\\\") class test {\n}",
            new ValidateAnnotationWithPrimitiveTypeValue("StringArgAnnotation", "test@;;\\"));
    }
    
    public void testReadWriteAnnotationArrayOfStringArg() throws Exception {
        performReadWrite("package test; import annotations.*; public @AnnotationArgAnnotation(@ArrayOfStringArgAnnotation(\"test@;;\\\\\")) class test {\n}", new Validator() {
            public void validate(CompilationInfo info, Element t) {
                List<? extends AnnotationMirror> annotations = t.getAnnotationMirrors();
                boolean found = false;
                
                for (AnnotationMirror m : annotations) {
                    if (("annotations.AnnotationArgAnnotation").equals(((TypeElement) m.getAnnotationType().asElement()).getQualifiedName().toString())) {
                        AnnotationValue v = findValue(m, "value");
                        Object proposedValue = v.getValue();
                        
                        assertTrue(proposedValue instanceof AnnotationMirror);
                        
                        found = true;
                        break;
                    }
                }
                
                assertTrue(found);
            }
        });
    }
    
    public void testReadWriteAnnotationEnumArg() throws Exception {
        performReadWrite("package test; import annotations.*; public @EnumArgAnnotation(TestEnum.X) class test {\n}", new Validator() {
            public void validate(CompilationInfo info, Element t) {
                List<? extends AnnotationMirror> annotations = t.getAnnotationMirrors();
                boolean found = false;
                
                for (AnnotationMirror m : annotations) {
                    if (("annotations.EnumArgAnnotation").equals(((TypeElement) m.getAnnotationType().asElement()).getQualifiedName().toString())) {
                        AnnotationValue v = findValue(m, "value");
                        Object proposedValue = v.getValue();
                        VariableElement value = (VariableElement) proposedValue;
                        
                        assertEquals("X", value.getSimpleName().toString());
                        
                        assertEquals("annotations.TestEnum", ((TypeElement) value.getEnclosingElement()).getQualifiedName().toString());
                        
                        found = true;
                        break;
                    }
                }
                
                assertTrue(found);
            }
        });
    }
    
    public void testReadWriteAnnotationWithAnnotation() throws Exception {
        performReadWrite("package test; import annotations.ArrayOfStringArgAnnotation; public @ArrayOfStringArgAnnotation(\"test@;;\\\\\") class test {\n}",
            new ValidateAnnotationWithPrimitiveTypeValue("ArrayOfStringArgAnnotation", Arrays.asList(new String[] {"test@;;\\"})));
    }
    
    public void testReadWriteRecursiveAnnotation() throws Exception {
        performReadWrite("package test; @test(\"test\")public @interface test {public String value();\n}");
    }
    
    public void testReadWriteAnnotationWithDefault() throws Exception {
        performReadWrite("package test; import annotations.ArrayOfStringArgAnnotation; public @interface test {ArrayOfStringArgAnnotation value() default @ArrayOfStringArgAnnotation(\"test@;;\\\\\");\n}",new Validator() {
            public void validate(CompilationInfo info, Element t) {
                assertTrue(t.getKind() == ElementKind.ANNOTATION_TYPE);
                
                ExecutableElement method = ElementFilter.methodsIn(t.getEnclosedElements()).get(0);
                
                AnnotationValue value = method.getDefaultValue();
                final boolean[] found = new boolean[1];
                
                value.accept(new SimpleAnnotationValueVisitor6() {
                    public @Override Object visitAnnotation(AnnotationMirror a, Object p) {
                        if ("annotations.ArrayOfStringArgAnnotation".equals(((TypeElement) a.getAnnotationType().asElement()).getQualifiedName().toString())) {
                            found[0] = true;
                            AnnotationValue v = findValue(a, "value");
                            Object proposedValue = v.getValue();
                            
                            List proposed = new ArrayList();
                            
                            for (AnnotationValue value : (Collection<AnnotationValue>) proposedValue) {
                                proposed.add(value.getValue());
                            }
                            
                            assertEquals(Arrays.asList(new String[] {"test@;;\\"}), proposed);
                        }
                        
                        return null;
                    }
                }, null);
                
                assertTrue(found[0]);
            }
        });
    }
    
    public void testReadWriteEnum1() throws Exception {
        performReadWrite("package test; public enum test {VALUE1, VALUE2;\n}", new Validator() {
            public void validate(CompilationInfo info, Element t) {
                assertEquals(ElementKind.ENUM, t.getKind());
                assertEquals(2, ElementFilter.fieldsIn(t.getEnclosedElements()).size());
                assertEquals(ElementKind.ENUM_CONSTANT, ElementFilter.fieldsIn(t.getEnclosedElements()).get(0).getKind());
                assertEquals(ElementKind.ENUM_CONSTANT, ElementFilter.fieldsIn(t.getEnclosedElements()).get(1).getKind());
            }
        });
    }
    
    public void testReadWriteEnum2() throws Exception {
        performReadWrite("package test; public enum test {\n" + "AUTO,\n" + "YES,\n" + "NO;\n" + "\n" + "public String getDisplayName() {\n" + "    return null;\n" + "}\n}", new Validator() {
            public void validate(CompilationInfo info, Element t) {
                assertEquals(ElementKind.ENUM, t.getKind());
                assertEquals(3, ElementFilter.fieldsIn(t.getEnclosedElements()).size());
                assertEquals(ElementKind.ENUM_CONSTANT, ElementFilter.fieldsIn(t.getEnclosedElements()).get(0).getKind());
                assertEquals(ElementKind.ENUM_CONSTANT, ElementFilter.fieldsIn(t.getEnclosedElements()).get(1).getKind());
                assertEquals(ElementKind.ENUM_CONSTANT, ElementFilter.fieldsIn(t.getEnclosedElements()).get(2).getKind());
            }
        });
    }
    
    public void testReadWriteAnonymousInnerClasses1() throws Exception {
        performReadWrite("package test; public class test {public void testMethod() {new Runnable() {public void run(){}};}}\n", new Validator() {
            public void validate(CompilationInfo info, Element t) {
                assertEquals(ElementKind.CLASS, t.getKind());
                
                JavacElements jels = (JavacElements) info.getElements();
                
                TypeElement anonymous = jels.getTypeElementByBinaryName("test.test$1");
                
                assertEquals(1, ElementFilter.methodsIn(t.getEnclosedElements()).size());
                
                ExecutableElement ee = ElementFilter.methodsIn(t.getEnclosedElements()).get(0);
                
                assertEquals(ee, anonymous.getEnclosingElement());
                
                assertEquals("java.lang.Runnable", ((TypeElement) ((DeclaredType) anonymous.getInterfaces().get(0)).asElement()).getQualifiedName().toString());
            }
        }, false);
    }
    
    public void testReadWriteAnonymousInnerClasses2() throws Exception {
        performReadWrite("package test; public class test {public void testMethod() {new Runnable() {public void run(){}}; new Runnable() {public void run(){}};}}\n", new Validator() {
            public void validate(CompilationInfo info, Element t) {
                assertEquals(ElementKind.CLASS, t.getKind());
                
                JavacElements jels = (JavacElements) info.getElements();
                
                TypeElement anonymous = jels.getTypeElementByBinaryName("test.test$1");
                
                assertEquals(1, anonymous.getInterfaces().size());
                
                assertEquals("java.lang.Runnable", ((TypeElement) ((DeclaredType) anonymous.getInterfaces().get(0)).asElement()).getQualifiedName().toString());
                
                anonymous = jels.getTypeElementByBinaryName("test.test$2");
                
                assertEquals(1, anonymous.getInterfaces().size());
                
                assertEquals("java.lang.Runnable", ((TypeElement) ((DeclaredType) anonymous.getInterfaces().get(0)).asElement()).getQualifiedName().toString());
            }
        }, false);
    }
    
    public void testReadWriteLocalClasses1() throws Exception {
        performReadWrite("package test; public class test {public void testMethod() {class Test implements Runnable {public void run(){}};}}\n", new Validator() {
            public void validate(CompilationInfo info, Element t) {
                assertEquals(ElementKind.CLASS, t.getKind());
                
                JavacElements jels = (JavacElements) info.getElements();
                
                TypeElement anonymous = jels.getTypeElementByBinaryName("test.test$1Test");
                
                assertEquals(1, anonymous.getInterfaces().size());
                
                assertEquals("java.lang.Runnable", ((TypeElement) ((DeclaredType) anonymous.getInterfaces().get(0)).asElement()).getQualifiedName().toString());
            }
        }, false);
    }
    
    private static interface Validator {
        public void validate(CompilationInfo info, Element t);
    }
    
    private String dump(Types types, TypeElement type) {
        //!!!well:
        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);
        
        SymbolDumper.dump(pw, types, type, null);
        
        pw.close();
        return w.toString();
    }
    
    private Map<String, String> dumpIncludingInnerClasses(CompilationInfo info, TypeElement type) {
        SourceAnalyserImpl sa = new SourceAnalyserImpl(
            (JavacTaskImpl) SourceUtilsTestUtil.getJavacTaskFor(info),
            info.getCompilationUnit(),
            ClasspathInfoAccessor.INSTANCE.getFileManager(info.getClasspathInfo()),           
            info.getCompilationUnit().getSourceFile()
            );
        
        info.getCompilationUnit().accept(sa, new HashMap<String,Pair<String,Map<String,Set<UsageType>>>>());
        
        return sa.class2Sig;
    }
    
    protected void performReadWrite(String source) throws Exception {
        performReadWrite(source, null);
    }
    
    protected void performReadWrite(String source, Validator validator) throws Exception {
        performReadWrite(source, validator, true);
    }
    
    protected void performReadWrite(String source, Validator validator, boolean verifySignatures) throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject file = fs.getRoot().createData("test.java");
        
        writeIntoFile(file, source);
        
        JavaSource js = JavaSource.forFileObject(file);
        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        CompilationUnitTree unit = info.getCompilationUnit();
        assertTrue(info.getDiagnostics().toString(), info.getDiagnostics().isEmpty());
        
        Tree main = unit.getTypeDecls().iterator().next();
        TypeElement type = (TypeElement) info.getTrees().getElement(new TreePath(new TreePath(unit), main));
        
        final Map<String, String> signatures = dumpIncludingInnerClasses(info, type);
        
        System.err.println("sig=" + signatures);
        
        FileObject file2 = fs.getRoot().createData("test2.java");
        ClasspathInfo cpInfo = ClasspathInfo.create(file2);
        JavaSource js2 = JavaSource.create(cpInfo);
        final CountDownLatch l = new CountDownLatch(1);
        
        final String[] newSig = new String[1];
        final Symbol[] symbol = new Symbol[1];
        final CompilationInfo[] infoOut = new CompilationInfo[1];
        
        js2.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController parameter) {
                try {
                    JavacTaskImpl task = (JavacTaskImpl) SourceUtilsTestUtil.getJavacTaskFor(parameter);
                    Context context = task.getContext();
                    SymbolClassReader reader = (SymbolClassReader) ClassReader.instance(context);
                    Name className = Name.Table.instance(context).fromString("test");
                    
                    PackageSymbol pack = reader.enterPackage(Name.Table.instance(context).fromString("test"));
                    
                    assertNotNull(pack);
                    
                    pack.complete();
                    
                    for (Map.Entry<String, String> entry : signatures.entrySet()) {
                        reader.includeClassFile(pack, FileObjects.memoryFileObject(entry.getValue(), entry.getKey() + ".sig"));
                    }
                    
                    ClassSymbol cs = reader.enterClass(className, pack);
                    
                    cs.complete();
                    
                    symbol[0] = cs;
                    JavacTaskImpl jt = (JavacTaskImpl)SourceUtilsTestUtil.getJavacTaskFor(parameter);
                    newSig[0] = dump(Types.instance(jt.getContext()), cs);
                    infoOut[0] = parameter;
                } finally {
                    l.countDown();
                }
            }
        },true);
        
        l.await();
        
        System.err.println("newSig=" + newSig[0]);
        if (verifySignatures)
            assertEquals(signatures.get("test"), newSig[0]);
        
        if (validator != null)
            validator.validate(infoOut[0], symbol[0]);
    }
    
    public void testCompileAgainstSimple() throws Exception {
        performCompileAgainst("package test; public class test2 {\npublic void testMethod2() {test.method();}\n}", "package test; public class test {\npublic static void method() {}\n}");
    }
    
    public void testCompileAgainstWithThrows() throws Exception {
        performCompileAgainst("package test; public class test2 {\npublic void testMethod2() {try {test.method();} catch (Exception e) {}}\n}", "package test; public class test {\npublic static void method() throws Exception {}\n}");
    }
    
    public void testCompileAgainstConstantInt() throws Exception {
        performCompileAgainst("package test; public class test2 {\npublic static final int x = test.C + 1;\n}", "package test; public class test {\npublic static final int C = 0;\n}");
    }
    
    public void testCompileAgainstConstantString() throws Exception {
        performCompileAgainst("package test; public class test2 {\npublic static final String x = test.C;\n}", "package test; public class test {\npublic static final String C = \"\";\n}");
    }
    
    //TODO: produces (intentional) errors, the infrastructure needs to be extended to handle this:
//    public void testCompileAgainstWithThrowsUnknownException() throws Exception {
//        performCompileAgainst("package test; public class test2 {\nString x() {return \"\";} public void testMethod2() {try {test t = new test(); XXX x; x = t.method(x(), x(), null);} catch (Exception e) {}}\n}", "package test; public class test {\npublic XXX method(String sd, GGG g, HHH[] h) throws YYY {}\n}");
//    }
    
    public void testCompileAgainstWithConstructors() throws Exception {
        performCompileAgainst("package test; public class test2 {\npublic void testMethod2() {new test(test2.class);}\n}", "package test; public class test {\npublic test(Class clazz){}\n}");
    }
    
    public void testCompileAgainstWithGenerics1() throws Exception {
        performCompileAgainst("package test; public class test2 {\npublic void testMethod2() {test t;}\n}", "package test; public class test<T> {\npublic T testMethod(T t) {return null;}\n}");
    }
    
    public void testCompileAgainstWithGenerics2() throws Exception {
        performCompileAgainst("package test; public class test2 {\npublic void testMethod2() {test<String> t = new test<String>();}\n}", "package test; public class test<T> {\npublic T testMethod(T t) {return null;}\n}");
    }
    
    public void testCompileAgainstWithGenerics3() throws Exception {
        performCompileAgainst("package test; public class test2 {\npublic void testMethod2() {test t = new test();}\n}", "1.4", "package test; public class test<T> {\njava.util.Map<String, java.io.PrintWriter> m = new java.util.HashMap<String, java.io.PrintWriter>();\n}", "1.5", false);
    }

    public void testCompileAgainstInnerClassWithGenerics1() throws Exception {
        performCompileAgainst("package test; public class test2 {\npublic void testMethod2() {test.Inner t;}\n}", "1.4", "package test; public class test {\npublic static class Inner<T> {}\n}", "1.5", false);
    }
    
    public void testCompileAgainstInnerClassWithGenerics2() throws Exception {
        performCompileAgainst("package test; public class test2 {\npublic void testMethod2() {test.Inner2 t2;test.Inner1 t1;}\n}", "1.4", "package test; public class test {\npublic static class Inner1<T> {} public static class Inner2<T> {}\n}", "1.5", false);
    }
    
    public void testCompileAgainstInnerClassWithGenerics3() throws Exception {
        performCompileAgainst("package test; public class test2 {\npublic void testMethod2() {test.Inner2 t2;test.Inner1 t1;}\n}", "1.4", "package test; public class test {\npublic static class Inner1<T> {private Inner2<T> i1;} public static class Inner2<T> {}\n}", "1.5", false);
    }
    
    public void testCompileAgainstInnerClassWithGenerics4() throws Exception {
        performCompileAgainst("package test; public class test2 {\npublic void testMethod2() {test.Inner2 t2;test.Inner1 t1;}\n}", "1.5", "package test; public class test {\npublic static class Inner1<T> {private Inner2<T> i1;} public static class Inner2<T> {}\n}", "1.5", true);
    }
    
    public void testCompileAgainstInnerClassWithGenerics5() throws Exception {
        performCompileAgainst("package test; public class test2 {\npublic void testMethod2() {test.Inner2 t2;test.Inner1 t1;}\n}", "1.5", "package test; public class test {\npublic static class Inner0 {} public static class Inner1<T> {private Inner2<T> i1;} public static class Inner2<T> {}\n}", "1.5", true);
    }
    
    public void testCompileAgainstInnerClassWithGenerics6() throws Exception {
        performCompileAgainst("package test; public class test2 {\npublic void testMethod2() {test.Inner1 t1;}\n}", "1.5", "package test; public class test {\npublic static abstract class Inner1<T> {}\n}", "1.5", true);
    }
    
    public void testCompileAgainstInnerClassWithGenerics7() throws Exception {
        performCompileAgainst("package test; public class test2 {\npublic void testMethod2() {test.Inner1 t1;}\n}", "1.5", "package test; public class test {\npublic static abstract class Inner1<T> {} public <T> Inner1<T> ret() {return null;}\n}", "1.5", true);
    }
    
    public void testCompileAgainstInnerClassWithGenerics8() throws Exception {
        performCompileAgainst("package test; public class test2 {\npublic void testMethod2() {test.Inner1 t1;}\n}", "1.5", "package test; public class test {\npublic static abstract class Inner1<T> {} public <T> Inner1<T> ret() {return null;}\n}", "1.5", true, "test.test$Inner1");
    }
    
    public void testCompileAgainstInnerClassWithGenerics9() throws Exception {
        performCompileAgainst("package test; public class test2 {\npublic void testMethod2() {test<String>.Inner1<java.util.Comparator> t1;}\n}", "1.5", "package test; public class test<E> {\npublic abstract class Inner1<T> {} public <T> Inner1<T> ret() {return null;}\n}", "1.5", true, "test.test$Inner1");
    }
    
    public void testCompileAgainstLocalClassWithGenerics1() throws Exception {
        performCompileAgainst("package test; public class test2 {\npublic void testMethod2() {test.Inner1<java.util.Comparator> t1;}\n}", "1.5", "package test; public class test {\npublic static abstract class Inner1<T> {} public <T> Inner1<T> ret() {class X extends Inner1<T>{} return null;}\n}", "1.5", false, "test.test$Inner1");
    }
    
    public void testCompileAgainstInnerClass1() throws Exception {
        performCompileAgainst("package test; public class test2 {\npublic void testMethod2() {test.Inner t = new test.Inner(null);}\n}", "1.5", "package test; public class test {\npublic static class Inner {Inner(Runnable r){}}\n}", "1.5", false);
    }
    
    public void testCompileAgainstInnerClass2() throws Exception {
        performCompileAgainst("package test; import test.test.Inner; public class test2 {\npublic void testMethod2() {test t = new test(); Inner i = t.new Inner(null);}\n}", "1.5", "package test; public class test {\npublic class Inner {Inner(Runnable r){}}\n}", "1.5", false);
    }
    
    public void testCompileAgainstEnum1() throws Exception {
        performCompileAgainst("package test; public class test2 {\npublic void testMethod2() {test t = null; switch (t) {case VALUE1: break;case VALUE2: break;}}}", "package test; public enum test {VALUE1, VALUE2; public int test() {return 0;}\n}");
    }
    
    public void testCompileAgainstEnum2() throws Exception {
        performCompileAgainst("package test; public class test2 {\npublic test get() {return null;} public void testMethod2() {switch (get()) {case VALUE1: break;case VALUE2: break;}}}", "package test; public enum test {VALUE1, VALUE2; public int test() {return 0;}\n}");
    }
    
    public void testCompileAgainstEnum3() throws Exception {
        performCompileAgainst("package test; public class test2 {\npublic void testMethod2() {for (test t : test.values()) ;}}", "package test; public enum test {VALUE1, VALUE2; public int test() {return 0;}\n}");
    }
    
    public void testCompileAgainstAnnotation1() throws Exception {
        performCompileAgainst("package test; @test(review=2) public class test2 {}", "package test; public @interface test {\n    String author() default \"unknown\";\n    int review();\n}");
    }
    
    public void testCompileAgainstAnnotation2() throws Exception {
        performCompileAgainst("package test; @test(review=2) public class test2 {}", "package test; public @interface test {\n    Class x() default Object.class;\n    int review();\n}");
    }
    
    public void testCompileAgainstAnnotation3() throws Exception {
        performCompileAgainst("package test; @test(review=2) public class test2 {}", "package test; public @interface test {\n    Class x() default int.class;\n    int review();\n}");
    }
    
    public void testCompileAgainstAnnotation4() throws Exception {
        performCompileAgainst("package test; @test(review=2) public class test2 {}", "package test; public @interface test {\n    Class x() default java.util.List.class;\n    int review();\n}");
    }
    
    public void testCompileAgainstInterface1() throws Exception {
        performCompileAgainst("package test; public class test2 {public void x() {java.util.Set<test> s;}}", "package test; public interface test {\n}");
    }
    
    public void testCompileAgainstInterface2() throws Exception {
        performCompileAgainst("package test; public class test2 {public <T> T x(Class<T> c) {return null;} public void xx() {x(test.class);}}", "package test; public interface test {\n}");
    }
    
    public void testCompileAgainstInterface3() throws Exception {
        performCompileAgainst("package test; public class test2 {public void x() {test s = null; if (s == null) return;}}", "package test; public interface test {\n}");
    }
    
    protected void performCompileAgainst(String what, String against) throws Exception {
        performCompileAgainst(what, "1.5", against, "1.5", true);
    }
    
    protected void performCompileAgainst(String what, String whatSourceLevel, String against, String againstLevel, boolean compareSignatures) throws Exception {
        performCompileAgainst(what, whatSourceLevel, against, againstLevel, compareSignatures, null);
    }
    
    protected void performCompileAgainst(String what, String whatSourceLevel, String against, String againstLevel, boolean compareSignatures, final String firstToComplete) throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject file = fs.getRoot().createData("test.java");
        
        writeIntoFile(file, against);
        
        SourceUtilsTestUtil.setSourceLevel(file, againstLevel);
        
        JavaSource js = JavaSource.forFileObject(file);
        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        CompilationUnitTree unit = info.getCompilationUnit();
        assertTrue(info.getDiagnostics().toString(), info.getDiagnostics().isEmpty());
        
        Tree main = unit.getTypeDecls().iterator().next();
        TypeElement type = (TypeElement) info.getTrees().getElement(new TreePath(new TreePath(unit), main));
        
        final Map<String, String> signatures = dumpIncludingInnerClasses(info, type);
        
        System.err.println("sig=" + signatures);
        
        FileObject file2 = fs.getRoot().createData("test2.java");
        
        writeIntoFile(file2, what);
        
        SourceUtilsTestUtil.setSourceLevel(file2, whatSourceLevel);
        
        JavaSource js2 = JavaSource.forFileObject(file2);
        final CountDownLatch l = new CountDownLatch(1);
        
        final String[] newSig = new String[1];
        final List[] errors = new List[1];
        
        js2.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController parameter) {
                try {
                    parameter.toPhase(Phase.PARSED);
                    JavacTaskImpl task = (JavacTaskImpl) SourceUtilsTestUtil.getJavacTaskFor(parameter);
                    Context context = task.getContext();
                    Table table = Table.instance(context);
                    SymbolClassReader reader = (SymbolClassReader) ClassReader.instance(context);
                    Name className = Name.Table.instance(context).fromString("test");
                    
                    PackageSymbol pack = reader.enterPackage(Name.Table.instance(context).fromString("test"));
                    
                    assertNotNull(pack);
                    
                    pack.complete();
                    
                    for (Map.Entry<String, String> entry : signatures.entrySet()) {
                        reader.includeClassFile(pack, FileObjects.memoryFileObject(entry.getValue(), entry.getKey() + ".sig"));
                    }
                    
                    if (firstToComplete != null) {
                        Name flatName = table.fromString(firstToComplete);
                        
                        reader.enterClass(flatName).complete();
                    }
                    
                    ClassSymbol cs = reader.enterClass(className, pack);
                    
                    cs.complete();
                    
                    parameter.toPhase(Phase.RESOLVED);
                    
                    JavacTaskImpl jt = (JavacTaskImpl)SourceUtilsTestUtil.getJavacTaskFor(parameter);
                    newSig[0] = dump(Types.instance(jt.getContext()), cs);
                    
                    errors[0] = parameter.getDiagnostics();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                l.countDown();
            }
        },false);
        
        l.await();
        
                    
        assertTrue(errors[0].toString(), errors[0].isEmpty());
        
        if (compareSignatures)
            assertEquals(signatures.get("test"), newSig[0]);
    }

    protected void performCompileAgainstSignature(String what, final String againstSignature) throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject file2 = fs.getRoot().createData("test2.java");
        
        writeIntoFile(file2, what);
        
        SourceUtilsTestUtil.setSourceLevel(file2, "1.5");
        
        JavaSource js2 = JavaSource.forFileObject(file2);
        final CountDownLatch l = new CountDownLatch(1);
        
        final Map<String, String>[] newSig = new Map[1];
        final List[] errors = new List[1];
        
        js2.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController parameter) {
                try {
                    parameter.toPhase(Phase.PARSED);
                    JavacTaskImpl task = (JavacTaskImpl) SourceUtilsTestUtil.getJavacTaskFor(parameter);
                    Context context = task.getContext();
                    Table table = Table.instance(context);
                    SymbolClassReader reader = (SymbolClassReader) ClassReader.instance(context);
                    Name className = Name.Table.instance(context).fromString("test");
                    
                    PackageSymbol pack = reader.enterPackage(Name.Table.instance(context).fromString("test"));
                    
                    assertNotNull(pack);
                    
                    pack.complete();
                    
                    reader.includeClassFile(pack, FileObjects.memoryFileObject(againstSignature, "test.sig"));
                    
                    ClassSymbol cs = reader.enterClass(className, pack);
                    
                    cs.complete();
                    
                    parameter.toPhase(Phase.RESOLVED);
                    
                    newSig[0] = dumpIncludingInnerClasses(parameter, cs);
                    
                    errors[0] = parameter.getDiagnostics();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                l.countDown();
            }
        },false);
        
        l.await();
        
                    
        assertTrue(errors[0].toString(), errors[0].isEmpty());
//        System.err.println("newSig=" + newSig[0]);
    }
    
    public void testNarážečka() throws Exception {
        performTestNarážečka("package test; public class test {public void testMethod() {new Runnable() {public void run(){}};}}\n", "test.test$1");
    }
    
    public void testNarážečka2() throws Exception {
        performTestNarážečka("package test; public class test<I, T> {private java.util.List<Snap> snaps; private class Snap {}}\n", "test.test$Snap");
    }
    
    public void testNarážečka3() throws Exception {
        performTestNarážečka("package test; public class test {private annotations.RequestProcessor.Task task; public static class O {}}\n", "test.test$O");
    }
    
    public void testNarážečka4() throws Exception {
        performTestNarážečka("package test;import java.util.Collection;" +
                "    abstract class Lookup {" +
                "    public static final Lookup EMPTY = null;" +
                "    private static Lookup defaultLookup;" +
                "    public Lookup() {}" +
                "    public static synchronized Lookup getDefault() {return null;}" +
                "    private static final class DefLookup {}" +
                "    private static void resetDefaultLookup() {}" +
                "    public abstract <T> T lookup(Class<T> clazz);" +
                "    public abstract <T> Result<T> lookup(Template<T> template);" +
                "    public <T> Item<T> lookupItem(Template<T> template) {return null;}" +
                "    public <T> Lookup.Result<T> lookupResult(Class<T> clazz) {return null;}" +
                "    public <T> Collection<? extends T> lookupAll(Class<T> clazz) {return null;}" +
                "    public interface Provider {}" +
                "    public static final class Template<T> extends Object {}" +
                "    public static abstract class Result<T> extends Object {}" +
                "    public static abstract class Item<T> extends Object {}" +
                "    private static abstract class Empty extends Lookup {}" +
                "}", "test.Lookup$Template");
    }

    public void testNarážečka5() throws Exception {
        performTestNarážečka("package test; public class test {private static Deleg cur; private static class Deleg extends test {}}\n", "test.test$Deleg");
    }
    
    public void testNarážečka6() throws Exception {
        performTestNarážečka("package test; public class test {public final class Deleg {Deleg(Runnable r) {} Deleg(Runnable r, int x) {}}}\n", "test.test$Deleg");
    }
    
    public void testNarážečka7() throws Exception {
        performTestNarážečka("package test; public class test {public <T> Two<T> lookup(One<T> o){return null;} public static class One<T> {}public static class Two<T> {}}\n", "test.test$One");
    }
    
    private void performTestNarážečka(String what, final String innerclassName) throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject file = fs.getRoot().createData("test.java");
        
        writeIntoFile(file, what);
        
        JavaSource js = JavaSource.forFileObject(file);
        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        CompilationUnitTree unit = info.getCompilationUnit();
        assertTrue(info.getDiagnostics().toString(), info.getDiagnostics().isEmpty());
        
        Tree main = unit.getTypeDecls().iterator().next();
        TypeElement type = (TypeElement) info.getTrees().getElement(new TreePath(new TreePath(unit), main));
        
        final Map<String, String> signatures = dumpIncludingInnerClasses(info, type);
        
        System.err.println("sig=" + signatures);
        
        FileObject file2 = fs.getRoot().createData("test2.java");
        
        writeIntoFile(file2, "package test; public class test2{}");
        
        JavaSource js2 = JavaSource.create(ClasspathInfo.create(file), file2, file);
        
        final List[] errors = new List[1];
        
        js2.runUserActionTask(new Task<CompilationController>() {
            private TypeElement firstClass;
            private TypeElement innerClass;

            public void run(CompilationController parameter) throws Exception {
                CouplingAbort.wasCouplingError = false;
                try {
                    JavacTaskImpl task = (JavacTaskImpl) SourceUtilsTestUtil.getJavacTaskFor(parameter);
                    Context context = task.getContext();
                    Table table = Table.instance(context);
                    SymbolClassReader reader = (SymbolClassReader) ClassReader.instance(context);
                    Name className = Name.Table.instance(context).fromString("test");
                    
                    PackageSymbol pack = reader.enterPackage(Name.Table.instance(context).fromString("test"));
                    
                    assertNotNull(pack);
                    
                    pack.complete();
                    
                    for (Map.Entry<String, String> entry : signatures.entrySet()) {
                        reader.includeClassFile(pack, FileObjects.memoryFileObject(entry.getValue(), entry.getKey() + ".sig"));
                    }
                    
                    parameter.toPhase(Phase.RESOLVED);
                    
                    assertFalse(CouplingAbort.wasCouplingError);
                    
                    if (firstClass == null) {
                        JavacElements jels = (JavacElements) parameter.getElements();
                        
                        firstClass = jels.getTypeElementByBinaryName("test.test");
                        innerClass = jels.getTypeElementByBinaryName(innerclassName);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                assertFalse(CouplingAbort.wasCouplingError);
            }
        },true);
    }
    
//    public void testNarážečka8() throws Exception {
//        String what = "package test; public class test {public static class One<T, E> extends Two <T> {public One(Class<T> t, Class<E> e) {super(t);}} public static class Two<T> {public Two(Class<T> t) {}}}\n";
//        final String innerclassName = "test.test$One";
//        FileSystem fs = FileUtil.createMemoryFileSystem();
//        FileObject file = fs.getRoot().createData("test.java");
//        
//        writeIntoFile(file, what);
//        
//        JavaSource js = JavaSource.forFileObject(file);
//        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
//        CompilationUnitTree unit = info.getCompilationUnit();
//        assertTrue(info.getDiagnostics().toString(), info.getDiagnostics().isEmpty());
//        
//        Tree main = unit.getTypeDecls().iterator().next();
//        TypeElement type = (TypeElement) info.getTrees().getElement(new TreePath(new TreePath(unit), main));
//        
//        final Map<String, String> signatures = dumpIncludingInnerClasses(info, type);
//        
//        System.err.println("sig=" + signatures);
//        
//        FileObject file2 = fs.getRoot().createData("test2.java");
//        
//        writeIntoFile(file2, "package test; public class test2<T, E> extends test.One<T, E> {public test2(Class<T> t, Class<E> e) {super(t, e);}}");
//        
//        JavaSource js2 = JavaSource.create(ClasspathInfo.create(file), file2, file);
//        
//        js2.runUserActionTask(new CancellableTask<CompilationController>() {
//            private TypeElement firstClass;
//            private TypeElement innerClass;
//            public void cancel() {
//            }
//            public void run(CompilationController parameter) throws Exception {
//                CouplingAbort.wasCouplingError = false;
//                try {
//                    JavacTaskImpl task = (JavacTaskImpl) SourceUtilsTestUtil.getJavacTaskFor(parameter);
//                    Context context = task.getContext();
//                    Table table = Table.instance(context);
//                    SymbolClassReader reader = (SymbolClassReader) ClassReader.instance(context);
//                    Name className = Name.Table.instance(context).fromString("test");
//                    
//                    PackageSymbol pack = reader.enterPackage(Name.Table.instance(context).fromString("test"));
//                    
//                    assertNotNull(pack);
//                    
//                    pack.complete();
//                    
//                    for (Map.Entry<String, String> entry : signatures.entrySet()) {
//                        reader.includeClassFile(pack, FileObjects.memoryFileObject(entry.getValue(), entry.getKey() + ".sig"));
//                    }
//                    
//                    parameter.toPhase(Phase.RESOLVED);
//                    
//                    List<Diagnostic> errors = parameter.getDiagnostics();
//                    
//                    assertTrue(errors.toString(), errors.isEmpty());
//                    
//                    if (firstClass == null) {
//                        JavacElements jels = (JavacElements) parameter.getElements();
//                        
//                        firstClass = jels.getTypeElementByBinaryName("test.test");
//                        innerClass = jels.getTypeElementByBinaryName(innerclassName);
//                        return;
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                
//                assertFalse(CouplingAbort.wasCouplingError);
//            }
//        },true);
//    }
    
//    public void testCompileAgainstSignature1() throws Exception {
//        performCompileAgainstSingature("package test; public class test2 {\npublic void testMethod2() {try {org.openide.nodes.Node.Property p = null; Long.parseLong(p.toString());} catch (Exception e) {}}\n}",
//            "GM1;<T:Ljava.lang.Object;;>Ntest.test;Ljava.lang.Object;@\n" +
//            "EM1040000001;N<init>;()()V\n" + 
//            "AM40000;Lorg.openide.nodes.Node;Nm;X;"
//        );
//    }
    
    private static class SourceAnalyserImpl extends SourceAnalyser.UsagesVisitor {
        
        private Map<String, String> class2Sig = new TreeMap<String, String>();
        
        public SourceAnalyserImpl(JavacTaskImpl jt, CompilationUnitTree cu, JavaFileManager manager, JavaFileObject sibling) {
            super(jt, cu, manager, sibling, (List<String>)null);
        }
        
        @Override void dump(TypeElement clazz, String className, Element enclosingMethod) {
            StringWriter w = new StringWriter();
            PrintWriter pw = new PrintWriter(w);
            
            SymbolDumper.dump(pw, getTypes(), clazz, enclosingMethod);
            
            pw.close();
            
            String flatName = ((ClassSymbol) clazz).flatname.toString();
            String fileName = flatName.substring(flatName.lastIndexOf('.') + 1);
            
            class2Sig.put(fileName, w.toString());
        }
        
        @Override protected boolean shouldGenerate (final String binaryName, ClassSymbol sym) {
            return true;
        }
        
    }

    public void testLazyAnnotationsAddition() throws Exception {
        performLazyAnnotationsAdditionTest("package test; public @interface annotation {String value();}", "package test; public class usage {public @annotation(\"test\") void test() {}}", "package test; public class verifier {usage u; private void test() {u.hashCode();}}");
    }
    
    protected void performLazyAnnotationsAdditionTest(String annotationText, String usageText, String verifierText) throws Exception {
        clearWorkDir();
        
        FileObject pack = FileUtil.createFolder(sourceRoot, "test");
        FileObject annotation = pack.createData("annotation.java");
        FileObject usage = pack.createData("usage.java");
        FileObject verifier = pack.createData("verifier.java");
        
        writeIntoFile(annotation, annotationText);
        writeIntoFile(usage, usageText);
        writeIntoFile(verifier, verifierText);
        
        JavaSource js = JavaSource.forFileObject(usage);
        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        CompilationUnitTree unit = info.getCompilationUnit();
        assertTrue(info.getDiagnostics().toString(), info.getDiagnostics().isEmpty());
        
        Tree main = unit.getTypeDecls().iterator().next();
        TypeElement type = (TypeElement) info.getTrees().getElement(new TreePath(new TreePath(unit), main));
        
        final Map<String, String> signatures = dumpIncludingInnerClasses(info, type);
        
        System.err.println("sig=" + signatures);
        
        ClasspathInfo cpInfo = ClasspathInfo.create(verifier);
        JavaSource js2 = JavaSource.create(cpInfo, verifier);
        
        js2.runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController parameter) throws Exception {
                JavacTaskImpl task = (JavacTaskImpl) SourceUtilsTestUtil.getJavacTaskFor(parameter);
                Context context = task.getContext();
                SymbolClassReader reader = (SymbolClassReader) ClassReader.instance(context);
                
                PackageSymbol pack = reader.enterPackage(Name.Table.instance(context).fromString("test"));
                
                assertNotNull(pack);
                
                pack.complete();
                
                for (Map.Entry<String, String> entry : signatures.entrySet()) {
                    reader.includeClassFile(pack, FileObjects.memoryFileObject(entry.getValue(), entry.getKey() + ".sig"));
                }
                
                parameter.toPhase(Phase.RESOLVED);
            }
        },true);
    }
    
    //XXX: test for writing and reading java.lang.Object:
//    public void testJavaLangObject() throws Exception {
//        FileSystem fs = FileUtil.createMemoryFileSystem();
//        FileObject obj = FileUtil.createData(fs.getRoot(), "java/lang/Object.java");
//        
//        writeIntoFile(obj, "package java.lang; public class Object {public Object(){} public Object get() {return null;}}");
//        
//        JavaSource js = JavaSource.forFileObject(obj);
//        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
//        CompilationUnitTree unit = info.getCompilationUnit();
//        assertTrue(info.getDiagnostics().toString(), info.getDiagnostics().isEmpty());
//        
//        Tree main = unit.getTypeDecls().iterator().next();
//        TypeElement type = (TypeElement) info.getTrees().getElement(new TreePath(new TreePath(unit), main));
//        
//        final Map<String, String> signatures = dumpIncludingInnerClasses(info, type);
//        
//        System.err.println("sig=" + signatures);
//        
//        FileObject file2 = fs.getRoot().createData("test2.java");
//        
//        writeIntoFile(file2, "package test; public class test2{private Object x = get();}");
//        
//        JavaSource js2 = JavaSource.create(ClasspathInfo.create(obj), file2, obj);
//        
//        final List[] errors = new List[1];
//        
//        js2.runUserActionTask(new CancellableTask<CompilationController>() {
//            public void cancel() {
//            }
//            public void run(CompilationController parameter) {
//                try {
//                    JavacTaskImpl task = (JavacTaskImpl) SourceUtilsTestUtil.getJavacTaskFor(parameter);
//                    Context context = task.getContext();
//                    Table table = Table.instance(context);
//                    SymbolClassReader reader = (SymbolClassReader) ClassReader.instance(context);
//                    Name className = Name.Table.instance(context).fromString("Object");
//                    
//                    PackageSymbol pack = reader.enterPackage(Name.Table.instance(context).fromString("java.lang"));
//                    
//                    assertNotNull(pack);
//                    
//                    pack.complete();
//                    
//                    for (Map.Entry<String, String> entry : signatures.entrySet()) {
//                        reader.includeClassFile(pack, FileObjects.memoryFileObject(entry.getValue(), entry.getKey() + ".sig"));
//                    }
//                    
//                    parameter.toPhase(Phase.RESOLVED);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        },true);
//    }
}
